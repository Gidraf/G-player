package gidraf.tiaplayer.adapters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import gidraf.tiaplayer.R;
import gidraf.tiaplayer.models.Song;
import gidraf.tiaplayer.models.database.HistoryModel;
import gidraf.tiaplayer.services.MusicService;
import gidraf.tiaplayer.utils.Constants;
import gidraf.tiaplayer.utils.listeners.BackgroungListener;
import gidraf.tiaplayer.utils.listeners.CurrentPositionListener;
import gidraf.tiaplayer.utils.listeners.ShowControlListener;

public class PlayListAdapter extends RecyclerView.Adapter<PlaylistHolder> implements CurrentPositionListener {
    List<Song> songs;
    Context context;
    MusicService musicService;
    boolean musicBound;
    Intent playIntent;
    ShowControlListener listener;
    CurrentPositionListener currentPositionListener;
    BackgroungListener backgroungListener;
    ShowControlListener controlListener;



    public PlayListAdapter(List<Song> songs, Context context, MusicService
            musicService, Intent playInten, ShowControlListener listenert,
                           CurrentPositionListener currentPositionListener,
                           BackgroungListener backgroungListener, ShowControlListener  controlerListener) {
        this.songs = songs;
        this.context = context;
        this.musicService = musicService;
        this.playIntent = playIntent;
        this.listener = listenert;
        this.currentPositionListener = currentPositionListener;
        this.backgroungListener =  backgroungListener;
        this.controlListener = controlerListener;

    }


    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicService = binder.getService();
            musicService.setPlayListSongs(songs);
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
        if (song.getSongImage()!=null){
            playlistHolder.songAlbum.setImageBitmap(song.getSongImage());
        }
        playlistHolder.songtitle.setText(song.getSongTitle());
        playlistHolder.songArtistName.setText(song.getArtist());
        playlistHolder.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.setSong(i);
                HistoryModel model = new HistoryModel();
                model.setSongId(song.getSongId());
                model.setAlbumId(song.getSongId());
                model.setSongName(song.getSongTitle());
                model.setSongArtist(song.getArtist());
                controlListener.getCurrentSongPlaying(false,model);
                controlListener.showControl(true);
                if(song.getSongImage() != null){
                backgroungListener.backgroundTobeDisplayed(song.getSongImage());
                }
                try {
                    musicService.playSong(10);
                    controlListener.setSessionid(musicService.getsessionId());
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
    public void setSongDuration(int duration) {
        currentPositionListener.setSongDuration(duration);
    }
}


class PlaylistHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView songtitle, songArtistName;
    public CircleImageView songAlbum;

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
        songAlbum = itemView.findViewById(R.id.image_album);
        songArtistName = itemView.findViewById(R.id.artist_name);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        clickListener.onClick(v);
    }
}
