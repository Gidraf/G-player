package gidraf.tiaplayer.adapters;


import android.arch.persistence.room.Room;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import gidraf.tiaplayer.R;
import gidraf.tiaplayer.models.database.HistoryDatabase;
import gidraf.tiaplayer.models.database.HistoryModel;
import gidraf.tiaplayer.services.MusicService;
import gidraf.tiaplayer.utils.CurrentPositionListener;
import gidraf.tiaplayer.utils.ShowControlListener;

public class SongHistoryAdapter extends RecyclerView.Adapter<SongHistoryHolder> implements ShowControlListener,
        CurrentPositionListener {

    private List<HistoryModel> songsList;
    private Context context;
    MusicService musicService;
    boolean musicBound;
    Intent playIntent;
    HistoryDatabase database;
    ShowControlListener controlListener;
    CurrentPositionListener currentPositionListener;



    public SongHistoryAdapter(List<HistoryModel> songs, Context context, MusicService service, ShowControlListener controlListener,
                              CurrentPositionListener currentPositionListener) {
        this.songsList = songs;
        this.context = context;
        this.musicService = service;
        this.controlListener = controlListener;
        this.currentPositionListener = currentPositionListener;
    }


    @NonNull
    @Override
    public SongHistoryHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View songView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.song_holder,viewGroup, false);
        database = Room.databaseBuilder(context,HistoryDatabase.class, "historymodel").allowMainThreadQueries().build();
        if(playIntent==null){
            playIntent = new Intent(context, MusicService.class);
            context.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            context.startService(playIntent);
        }
        return new SongHistoryHolder(songView);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongHistoryHolder songHistoryHolder, final int i) {
        final HistoryModel song = songsList.get(songHistoryHolder.getAdapterPosition());
        songHistoryHolder.songTitle.setText(song.getSongName());
        songHistoryHolder.songImage.setImageResource(R.drawable.holder);
        songHistoryHolder.removeSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.historyModelDao().remove(song);
                songsList.remove(i);
                notifyDataSetChanged();
            }
        });

        songHistoryHolder.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showControl(true);
                controlListener.getCurrentSongPlaying(song);
                try {
                    musicService.setSong((int) song.getSongId());
                    musicService.playHistorySong(10);
                    setSongDuration(musicService.getSongDuration());
                    setSessionid(musicService.getsessionId());
                } catch (IOException e) {
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    if(playIntent==null){
                        playIntent = new Intent(context, MusicService.class);
                        context.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
                        context.startService(playIntent);
                    }
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return songsList.size();
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicService = binder.getService();
            musicService.setList(songsList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public void showControl(boolean show) {
        controlListener.showControl(show);
    }

    @Override
    public void setSessionid(int id) {
        controlListener.setSessionid(id);
    }

    @Override
    public void getCurrentSongPlaying(HistoryModel currentSon) {
        controlListener.getCurrentSongPlaying(currentSon);
    }

    @Override
    public void setSongDuration(int duration) {
        currentPositionListener.setSongDuration(duration);
    }

};


class SongHistoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public ImageView songImage, removeSong, play;
    public TextView songTitle, songArtist;
    View.OnClickListener clickListener;


    public SongHistoryHolder(@NonNull View itemView, View.OnClickListener clickListener) {
        super(itemView);
        this.clickListener = clickListener;
    }


    public void setClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public SongHistoryHolder(@NonNull View itemView) {
        super(itemView);
        songImage = itemView.findViewById(R.id.song_image);
        songTitle = itemView.findViewById(R.id.song_title);
        songArtist = itemView.findViewById(R.id.artist_name);
        removeSong = itemView.findViewById(R.id.remove_song_btn);
        itemView.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        clickListener.onClick(v);
    }
}


