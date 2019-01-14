package gidraf.tiaplayer;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.stone.vega.library.VegaLayoutManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import gidraf.tiaplayer.adapters.PlayListAdpter;
import gidraf.tiaplayer.adapters.SongHistoryAdapter;
import gidraf.tiaplayer.models.Song;
import gidraf.tiaplayer.models.database.HistoryDatabase;
import gidraf.tiaplayer.models.database.HistoryModel;
import gidraf.tiaplayer.services.MusicService;
import gidraf.tiaplayer.utils.CurrentPositionListener;
import gidraf.tiaplayer.utils.ScrollTextView;
import gidraf.tiaplayer.utils.ShowControlListener;
import gidraf.tiaplayer.utils.Visualizer;

public class HomeActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        MediaController.MediaPlayerControl, View.OnClickListener, ShowControlListener, CurrentPositionListener{
    private TabLayout tabLayout;
    private RecyclerView songHistory_rv, songList_rv;
    private View tabView;
    private Toolbar toolbar;
    private SongHistoryAdapter adapter;
    PlayListAdpter songListAdapter;
    private List<HistoryModel> songs;
    private  HistoryDatabase database;
    VegaLayoutManager manager;
    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 ";
    Cursor cursor;
    RelativeLayout controllerContainer;
    List<HistoryModel> historySongs;
    private int currentSong = 0;
    RelativeLayout mediaController;
    ImageView playBtn, nextBtn, prevBtn, shuffle, imageBackground;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound=false;
    CarouselLayoutManager layoutManager;
    private boolean paused=false, playbackPaused=false;
    Visualizer visualizer;
    boolean is8D = false;
    int currentPosition = 0;
    private ScrollTextView currentSongTitle;
    SeekBar seekBar;
    TextView duration, progressTime, historyTextView;
    private Handler mHandler = new Handler();
    double leftVolume = 0.1;
    double rightVolume = 1;
    ImageView clearHistory;


    String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.home_toolbar);
        tabView = LayoutInflater.from(this).inflate(R.layout.handlers, toolbar);
        controllerContainer = findViewById(R.id.controller_container);
        songHistory_rv = findViewById(R.id.song_history_rv);
        songList_rv = findViewById(R.id.song_list);
        playBtn = findViewById(R.id.play_btn);
        nextBtn = findViewById(R.id.next_btn);
        prevBtn = findViewById(R.id.prev_btn);
        shuffle = findViewById(R.id.shuffle);
        historyTextView = findViewById(R.id.history_tv);
        clearHistory = findViewById(R.id.clear_history);
        progressTime = findViewById(R.id.progress_time);
        playBtn.setOnClickListener(this);
        prevBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        clearHistory.setOnClickListener(this);
        visualizer = findViewById(R.id.main_visualizer);
        currentSongTitle = tabView.findViewById(R.id.current_song);
        currentSongTitle.setSelected(true);
        currentSongTitle.startScroll();
        seekBar = tabView.findViewById(R.id.seek_bar);
        duration = tabView.findViewById(R.id.duration);
        imageBackground = findViewById(R.id.backgorund_image);
        visualizer.setColor(Color.RED);
        database = Room.databaseBuilder(this,HistoryDatabase.class,"historymodel").allowMainThreadQueries().build();
        layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, true);
        manager = new VegaLayoutManager();
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        songHistory_rv.setHasFixedSize(true);
        songHistory_rv.addOnScrollListener(new CenterScrollListener());
        songHistory_rv.setHasFixedSize(true);
        songHistory_rv.setLayoutManager(layoutManager);
        songList_rv.setLayoutManager(manager);

        if (Build.VERSION.SDK_INT >= 23) {
            if(checkPermissions()){
                try {
                    fetchSongs();
                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "error:: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

        } else {
            try {
                fetchSongs();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private void showVisualizer (int id ){
        try {
            visualizer.setPlayer(id);
        } catch (Exception e) {
            visualizer.setEnabled(false);
            visualizer.release();
            visualizer.setPlayer(id);
        }
    }


    @Override
    protected void onPause(){
       ShowController();
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        ShowController();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            playIntent.setAction(MusicService.ACTION_PLAY);
            startService(playIntent);
            musicService.requestAudioFocus();
        }
        super.onResume();

    }

    private void ShowController() {
        if(musicService!=null)
        {
            musicService.showControl=true;
        }
    }

    @Override
    protected void onStop() {
        ShowController();
        super.onStop();
    }

    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:PERMISSIONS) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),200 );
            return false;
        }
        return true;
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicService = binder.getService();
            musicService.setList(songs);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200:{
                if (grantResults.length > 0) {
                    String permissionsDenied = "";
                    for (String per : PERMISSIONS) {
                        if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                            permissionsDenied += "\n" + per;

                        }

                    }
                    // Show permissionsDenied
                    try {
                        fetchSongs();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, "error:: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                return;
            }
        }

            // other 'case' lines to check for other
            // permissions this app might request.

    }

    private void fetchSongs() throws FileNotFoundException {

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
        };

        cursor = this.managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);
        songs = new ArrayList<>();
        final List<Song> songs = new ArrayList<>();
        int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int songArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int songId = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
        while (cursor.moveToNext()) {
            Song song = new Song();
            song.setArtist(cursor.getString(songArtist));
            song.setSongId(cursor.getLong(songId));
            String title = cursor.getString(titleColumn);
            song.setSongTitle(truncate(title,20));
            songs.add(song);
        }
        songListAdapter = new PlayListAdpter(songs, this, musicService, playIntent, this,this);
        historySongs = database.historyModelDao().getall();
        if (historySongs.size()>0){
            historyTextView.setVisibility(View.VISIBLE);
            clearHistory.setVisibility(View.VISIBLE);
            songHistory_rv.setVisibility(View.VISIBLE);
        }
        else {
            historyTextView.setVisibility(View.GONE);
            clearHistory.setVisibility(View.GONE);
            songHistory_rv.setVisibility(View.GONE);
        }
        adapter = new SongHistoryAdapter(historySongs, HomeActivity.this, musicService, this, this);
        songHistory_rv.setAdapter(adapter);
        songHistory_rv.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 0 ||dy<0)
                {
                    showControl(false);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    showControl(false);
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        songList_rv.setAdapter(songListAdapter);
        songListAdapter.notifyDataSetChanged();
        adapter.notifyDataSetChanged();
    }




    private HistoryModel playPrev() throws IOException {
        return musicService.playPrev();

    }

    private HistoryModel playNext() throws IOException {
        return musicService.playNext();

    }

    public static String truncate(String value, int length) {
        // Ensure String length is longer than requested size.
        if (value.length() > length) {
            return value.substring(0, length);
        } else {
            return value;
        }
    }


    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicService=null;
        super.onDestroy();
    }

    @Override
    public void start() {
        musicService.go(10);
    }

    @Override
    public void pause() {
        musicService.pauseSoung(10);
        playbackPaused=true;
    }


    @Override
    public int getDuration() {
        return musicService.getSongDuration();
    }

    @Override
    public int getCurrentPosition() {
        return musicService != null ? 0 : musicService.getSongPosition();
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return currentSong;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return musicService.getsessionId();
    }

    @Override
    public void onClick(View v) {
        if (musicService != null) {
            if (v == playBtn) {
                if (musicService.isPng()) {
                    playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    musicService.pauseSoung(10);
                } else {
                    playBtn.setImageResource(R.drawable.ic_pause_black_24dp);
                    musicService.go(10);
                    showVisualizer(musicService.getsessionId());
                }
            } else if (v == nextBtn) {
                try {
                    getCurrentSongPlaying(playNext());
                    showVisualizer(musicService.getsessionId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (v == prevBtn) {
                try {
                    getCurrentSongPlaying(playPrev());
                    showVisualizer(musicService.getsessionId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(v == shuffle) {
                if( musicService.setShuffle()){
                    shuffle.setImageResource(R.drawable.is_shuffled);
                }
                else {
                    shuffle.setImageResource(R.drawable.ic_shuffle_black_24dp);
                }
            }
            else if (v == clearHistory){
                try {
                    database.historyModelDao().clearHistory();
                    fetchSongs();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            if(playIntent==null){
                playIntent = new Intent(this, MusicService.class);
                bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
                startService(playIntent);
            }
        }
    }


    @Override
    public void showControl(boolean show) {
        if (show) {
            controllerContainer.setVisibility(View.VISIBLE);
            playBtn.setImageResource(R.drawable.ic_pause_black_24dp);
        }
        else {
            controllerContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void setSessionid(int id) {
        showVisualizer(id);
    }

    @Override
    public void getCurrentSongPlaying(HistoryModel currentSon) {
        if(musicService != null && !musicService.automaticNex) {
            currentSongTitle.setText(currentSon.getSongName());
            historySongs.add(currentPosition,currentSon);
            try {
                fetchSongs();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setSongDuration(final int length) {
        HomeActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService != null){
                    progressTime.setText(getDurationBreakdown(musicService.getSongPosition()));
                    if(musicService.model!=null)
                    {
                        currentSongTitle.setText(musicService.model.getSongName());
                        duration.setText(getDurationBreakdown((musicService.getSongDuration())));
                        seekBar.setMax(musicService.getSongDuration());
                        seekBar.setProgress(musicService.getSongPosition());
                    }
                }
                mHandler.postDelayed(this, 1000);
            }
        });
        duration.setText(getDurationBreakdown((length)));
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("serviceStatus", musicBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        musicBound = savedInstanceState.getBoolean("serviceStatus");
    }

    public static String getDurationBreakdown(long millis) {
        if(millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(minutes);
        sb.append(":");
        sb.append(seconds);

        return(sb.toString());
    }
}
