package gidraf.tiaplayer;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
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

import gidraf.tiaplayer.adapters.EqualizerAdapter;
import gidraf.tiaplayer.adapters.PlayListAdapter;
import gidraf.tiaplayer.adapters.PresetAdapter;
import gidraf.tiaplayer.adapters.SongHistoryAdapter;
import gidraf.tiaplayer.models.EqualizerModal;
import gidraf.tiaplayer.models.Preset;
import gidraf.tiaplayer.models.Song;
import gidraf.tiaplayer.models.database.HistoryModel;
import gidraf.tiaplayer.models.database.HistorySongRepository;
import gidraf.tiaplayer.services.MusicService;
import gidraf.tiaplayer.utils.listeners.BackgroungListener;
import gidraf.tiaplayer.utils.Constants;
import gidraf.tiaplayer.utils.listeners.CurrentPositionListener;
import gidraf.tiaplayer.utils.listeners.EqualizerListener;
import gidraf.tiaplayer.utils.ScrollTextView;
import gidraf.tiaplayer.utils.listeners.PresetChangeListener;
import gidraf.tiaplayer.utils.listeners.ShowControlListener;
import gidraf.tiaplayer.utils.Visualizer;
import gidraf.tiaplayer.viewModels.HistorySongsViewModels;
import jp.wasabeef.blurry.Blurry;

public class HomeActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        MediaController.MediaPlayerControl, View.OnClickListener, ShowControlListener, PresetChangeListener,
        CurrentPositionListener, EqualizerListener,
        BackgroungListener{
    private RecyclerView songHistory_rv, songList_rv, equalizerRecycler, presetsRecyclerView;
    private View tabView;
    private Toolbar toolbar;
    private SongHistoryAdapter adapter;
    PlayListAdapter songListAdapter;
    private List<Song> songs;
    VegaLayoutManager manager;
    AppBarLayout homeappBar;
    Equalizer equalizer;
    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 ";
    Cursor cursor;
    RelativeLayout equalizerContainer;
    RelativeLayout controllerContainer;
    List<HistoryModel> historySongs;
    private int currentSong = 0;
    ImageView playBtn, nextBtn, prevBtn, shuffle, imageBackground, equalizerButton, imageBlur;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound=false;
    CarouselLayoutManager layoutManager, equalizerManager;
    Visualizer visualizer;
    int currentPosition = 0;
    private ScrollTextView currentSongTitle;
    SeekBar seekBar;
    TextView duration, progressTime, historyTextView, customTitle;
    private Handler mHandler = new Handler();
    ImageView clearHistory;
    private List<EqualizerModal> bandList;
    RecyclerView.LayoutManager presetsManager;
    Switch equalizerSwitch;
    List<Preset> presets = new ArrayList<>();
    HistorySongRepository historySongRepository;
    private HistorySongsViewModels historySongsViewModels;
    private HistoryModel currentModel;

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
        init();
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

    private void createEqaualizer() {
        if (musicService != null) {
            equalizer = musicService.getEqualizer();
            createPresets();
            equalizer.usePreset((short) 0);
            PresetAdapter presetAdapter = new PresetAdapter(presets, HomeActivity.this, this, this);
            presetsRecyclerView.setAdapter(presetAdapter);
            presetAdapter.notifyDataSetChanged();
            final short lowerEqualizerBandLevel = equalizer.getBandLevelRange()[0];
            final short upperEqualizerBandLevel = equalizer.getBandLevelRange()[1];

            for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
                final short equalizerBandIndex = i;
                EqualizerModal equalizerModal = new EqualizerModal();
                equalizerModal.setBand(i);
                equalizerModal.setLowerbandlevel(lowerEqualizerBandLevel);
                equalizerModal.setFrequencyheader(String.valueOf(equalizer.getCenterFreq(equalizerBandIndex) / 1000) + " Hz");
                equalizerModal.setLowerEqualizer(String.valueOf(lowerEqualizerBandLevel / 100) + " dB");
                equalizerModal.setLowerEqualizer(String.valueOf((upperEqualizerBandLevel / 100) + " dB"));
                equalizerModal.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);
                bandList.add(equalizerModal);
            }
            EqualizerAdapter equalizerAdapter = new EqualizerAdapter(HomeActivity.this,bandList,this);
            equalizerRecycler.setAdapter(equalizerAdapter);
            equalizerAdapter.notifyDataSetChanged();
        }
    }

    private void createPresets() {
        for (short i=0; i<equalizer.getNumberOfPresets();i++){
                Preset preset = new Preset();
                preset.setName(equalizer.getPresetName(i));
                preset.setColor("#F5F43131");
                presets.add(preset);

        }
    }

    private void init() {
        historySongsViewModels = ViewModelProviders.of(this).get(HistorySongsViewModels.class);
        toolbar = findViewById(R.id.home_toolbar);
        tabView = LayoutInflater.from(this).inflate(R.layout.handlers, toolbar);
        controllerContainer = findViewById(R.id.controller_container);
        songHistory_rv = findViewById(R.id.song_history_rv);
        songList_rv = findViewById(R.id.song_list);
        playBtn = findViewById(R.id.play_btn);
        nextBtn = findViewById(R.id.next_btn);
        prevBtn = findViewById(R.id.prev_btn);
        shuffle = findViewById(R.id.shuffle);

        equalizerButton = findViewById(R.id.equalizer_btn);
        historyTextView = findViewById(R.id.history_tv);
        clearHistory = findViewById(R.id.clear_history);
        progressTime = findViewById(R.id.progress_time);
        playBtn.setOnClickListener(this);
        prevBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        bandList = new ArrayList<>();
        equalizerButton.setOnClickListener(this);
        clearHistory.setOnClickListener(this);
        visualizer = findViewById(R.id.main_visualizer);
        currentSongTitle = tabView.findViewById(R.id.current_song);
        customTitle = findViewById(R.id.custom_preset_title);
        currentSongTitle.setSelected(true);
        currentSongTitle.startScroll();
        seekBar = tabView.findViewById(R.id.seek_bar);
        duration = tabView.findViewById(R.id.duration);
        imageBackground = findViewById(R.id.background_image);
        visualizer.setColor(Color.RED);
        homeappBar = findViewById(R.id.home_layout_appbar);
        imageBlur = findViewById(R.id.image_blur);
        equalizerContainer = findViewById(R.id.equalizer_container);
        layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, true);
        equalizerManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, true);
        equalizerSwitch = findViewById(R.id.equalizer_toggle_switch);
        equalizerSwitch.setChecked(false);


        manager = new VegaLayoutManager();
        equalizerRecycler = findViewById(R.id.equalizer_recycle_view);
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        equalizerManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        presetsRecyclerView = findViewById(R.id.preset_recycler_view);
        presetsRecyclerView.setAlpha(0.7f);
        equalizerRecycler.setAlpha(0.7f);
        presetsManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        presetsRecyclerView.setHasFixedSize(true);
        presetsRecyclerView.setLayoutManager(presetsManager);
        songHistory_rv.setHasFixedSize(true);
        songHistory_rv.addOnScrollListener(new CenterScrollListener());
        songHistory_rv.setLayoutManager(layoutManager);
        songList_rv.setLayoutManager(manager);
        equalizerRecycler.addOnScrollListener(new CenterScrollListener());
        equalizerRecycler.setLayoutManager(equalizerManager);
        equalizerRecycler.setHasFixedSize(true);

        equalizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    onActivateEqualizer(true);
                    equalizerRecycler.setEnabled(true);
                    equalizerRecycler.setAlpha(1);
                    presetsRecyclerView.setAlpha(1);
                    equalizerSwitch.setAlpha(1);
                    presetsRecyclerView.setEnabled(true);
                    return;
                }
                equalizerSwitch.setAlpha(0.7f);
                onActivateEqualizer(false);
                equalizerRecycler.setEnabled(false);
                equalizerRecycler.setAlpha(0.7f);
                presetsRecyclerView.setAlpha(0.7f);
                presetsRecyclerView.setEnabled(false);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(musicService!=null && fromUser){
                    musicService.seek(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


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
        super.onPause();
    }

    @Override
    protected void onResume(){
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            playIntent.setAction(MusicService.ACTION_PLAY);
            startService(playIntent);
            musicService.requestAudioFocus();
        }
        super.onResume();

    }

    @Override
    protected void onStop() {
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
            createEqaualizer();
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Toast.makeText(this, "Oops!", Toast.LENGTH_SHORT).show();
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        finish();
    }


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
                    try {
                        fetchSongs();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, "error:: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                return;
            }
        }


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
           Bitmap art = Constants.getRawByt(this,cursor.getLong(songId) );
            Song song = new Song();
            if (null != art){
                song.setSongImage(art);
            }
            song.setArtist(cursor.getString(songArtist));
            song.setSongId(cursor.getLong(songId));
            String title = cursor.getString(titleColumn);
            song.setSongTitle(title);
            songs.add(song);
        }
        songListAdapter = new PlayListAdapter(songs, this, musicService, playIntent,
                this,this, this, this);
        historyTextView.setVisibility(View.VISIBLE);
        clearHistory.setVisibility(View.VISIBLE);
        songHistory_rv.setVisibility(View.VISIBLE);
        adapter = new SongHistoryAdapter(this, musicService,
                this, this, this, historySongsViewModels);
        historySongsViewModels.getAllHistorySongs().observe(this, new Observer<List<HistoryModel>>() {
            @Override
            public void onChanged(@Nullable List<HistoryModel> models) {
                adapter.setSongsList(models);
            }
        });
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




    private void playPrev() throws IOException {
         musicService.playPrev();

    }

    private void playNext() throws IOException {
        musicService.playNext();


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
        unbindService(musicConnection);
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
                    playNext();
                    HistoryModel model = musicService.getModel();
                    getCurrentSongPlaying(false,model);
                    Bitmap bitmap = Constants.getRawByt(HomeActivity.this, model.getAlbumId());
                    backgroundTobeDisplayed(bitmap);
                    showVisualizer(musicService.getsessionId());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (v == prevBtn) {
                try {
                    playPrev();
                    HistoryModel model = musicService.getModel();
                    getCurrentSongPlaying(false,model);
                    Bitmap bitmap = Constants.getRawByt(HomeActivity.this, model.getAlbumId());
                    backgroundTobeDisplayed(bitmap);
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
                    historySongsViewModels.clearHistorySongs();
                    fetchSongs();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else if(v==equalizerButton){
                if(equalizerContainer.getVisibility() == View.VISIBLE){
                    equalizerContainer.setVisibility(View.GONE);
                    songHistory_rv.setVisibility(View.VISIBLE);
                    visualizer.setVisibility(View.VISIBLE);
                    historyTextView.setText("HISTORY");
                    showControl(true);
                    equalizerButton.setImageResource(R.drawable.ic_equalizer_black_24dp);
                    clearHistory.setVisibility(View.VISIBLE);
                    shuffle.setVisibility(View.VISIBLE);

                }
                else {
                    clearHistory.setVisibility(View.GONE);
                    songHistory_rv.setVisibility(View.GONE);
                    visualizer.setVisibility(View.GONE);
                    shuffle.setVisibility(View.GONE);
                    showControl(false);
                    equalizerButton.setImageResource(R.drawable.ic_close_black_24dp);
                    historyTextView.setText("");
                    equalizerContainer.setVisibility(View.VISIBLE);
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
    public void getCurrentSongPlaying(boolean isHistory, HistoryModel currentSon) {
        if(musicService != null) {
            if(!isHistory){
            historySongsViewModels.addHstorySong(currentSon);
            }
            Bitmap artwork = Constants.getRawByt(this, currentSon.getAlbumId());
            backgroundTobeDisplayed(artwork);
            currentSongTitle.setText(currentSon.getSongName());
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
                    if (musicService != null) {
                        if (musicService.getSongPosition() > 0) {
                            progressTime.setText(getDurationBreakdown(musicService.getSongPosition()));
                        }
                        if (musicService.getModel() != null) {
                            Bitmap bitmap = Constants.getRawByt(HomeActivity.this, musicService.getModel().getAlbumId());
                            backgroundTobeDisplayed(bitmap);
                            currentSongTitle.setText(musicService.getModel().getSongName());
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

    @Override
    public void onActivateEqualizer(boolean isActivated) {
        if(musicService!=null && equalizer!=null){
            equalizer.setEnabled(isActivated);
        }
    }

    @Override
    public void setEqualizerListener(short equalizerBandwidth, short lowerEqualizerBandwidth) {
        if(musicService!=null && equalizer!=null){
            equalizer.setBandLevel(equalizerBandwidth, lowerEqualizerBandwidth);
        }
    }


    @Override
    public void setEqualizerPresetListenser(short preset) {
        if(musicService!=null && equalizer!=null){
            equalizer.usePreset(preset);
        }
    }

    @Override
    public void backgroundTobeDisplayed(Bitmap bitmap) {
        if(bitmap!=null){
            Blurry.with(this).from(bitmap).into(imageBlur);
        }

    }

    @Override
    public void onPresetChangeListener(int index) {
        if(presets !=null && adapter !=null){
            presets.clear();
            createPresets();
            Preset preset = presets.get(index);
            preset.setColor("#35393e");
            presets.set(index, preset);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResetChangeListner() {
        if(presets !=null && adapter !=null){
            presets.clear();
            createPresets();
            adapter.notifyDataSetChanged();
        }
    }
}
