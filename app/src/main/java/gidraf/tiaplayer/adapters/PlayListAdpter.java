package gidraf.tiaplayer.adapters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chibde.visualizer.BarVisualizer;
import com.chibde.visualizer.CircleBarVisualizer;

import java.io.IOException;
import java.util.List;

import gidraf.tiaplayer.R;
import gidraf.tiaplayer.models.Song;
import gidraf.tiaplayer.models.database.HistoryModel;
import gidraf.tiaplayer.services.MusicService;
import gidraf.tiaplayer.utils.CurrentPositionListener;
import gidraf.tiaplayer.utils.ItemClickListener;
import gidraf.tiaplayer.utils.ShowControlListener;

public class PlayListAdpter  extends RecyclerView.Adapter<PlaylistHolder> implements ShowControlListener, CurrentPositionListener {
    List<Song> songs;
    Context context;
    MusicService musicService;
    boolean musicBound;
    Intent playIntent;
    ShowControlListener listener;
    CurrentPositionListener currentPositionListener;



    public PlayListAdpter(List<Song> songs, Context context, MusicService musicService, Intent playInten, ShowControlListener listenert,
                          CurrentPositionListener currentPositionListener) {
        this.songs = songs;
        this.context = context;
        this.musicService = musicService;
        this.playIntent = playIntent;
        this.listener = listenert;
        this.currentPositionListener = currentPositionListener;

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


    @NonNull
    @Override
    public PlaylistHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.play_list_holder, viewGroup,false);
        if(playIntent==null){
            playIntent = new Intent(context, MusicService.class);
            context.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
        return new PlaylistHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlaylistHolder playlistHolder, final int i) {
        final Song song = songs.get(i);

        playlistHolder.songtitle.setText(song.getSongTitle());
        playlistHolder.songArtistName.setText(song.getArtist());
        playlistHolder.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.setSong(i);
                HistoryModel model = new HistoryModel();
                model.setSongId(i);
                model.setSongName(song.getSongTitle());
                model.setSongArtist(song.getArtist());
                getCurrentSongPlaying(model);
                showControl(true);

                try {
                    musicService.playSong(1);
                    setSessionid(musicService.getsessionId());
                    setSongDuration(musicService.getSongDuration());
                } catch (IOException e) {
                    Toast.makeText(context, "Error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return songs.size();
    }


    @Override
    public void showControl(boolean show) {
        listener.showControl(show);
    }

    @Override
    public void setSessionid(int id) {
        listener.setSessionid(id);
    }

    @Override
    public void getCurrentSongPlaying(HistoryModel currentSon) {
        listener.getCurrentSongPlaying(currentSon);
    }

    @Override
    public void setSongDuration(int duration) {
        currentPositionListener.setSongDuration(duration);
    }
}


class PlaylistHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView songtitle, songArtistName;
    public ImageView favourite;

    View.OnClickListener clickListener;

    public PlaylistHolder(@NonNull View itemView, View.OnClickListener listener) {
        super(itemView);
        this.clickListener = listener;
    }

    public View.OnClickListener getItemClickListener() {
        return clickListener;
    }

    public void setItemClickListener(View.OnClickListener listener) {
        this.clickListener = listener;
    }

    public PlaylistHolder(@NonNull View itemView) {
        super(itemView);

        songtitle = itemView.findViewById(R.id.song_title_playlist_tv);
        favourite = itemView.findViewById(R.id.favourite_song_btn);
        songArtistName = itemView.findViewById(R.id.artist_name);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        clickListener.onClick(v);
    }
}
