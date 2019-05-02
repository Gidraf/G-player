package gidraf.tiaplayer.adapters;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jackandphantom.paletteshadowview.PaletteShadowView;

import java.io.IOException;
import java.util.List;

import gidraf.tiaplayer.R;
import gidraf.tiaplayer.models.database.HistoryModel;
import gidraf.tiaplayer.services.MusicService;
import gidraf.tiaplayer.utils.Constants;
import gidraf.tiaplayer.utils.listeners.BackgroungListener;
import gidraf.tiaplayer.utils.listeners.CurrentPositionListener;
import gidraf.tiaplayer.utils.listeners.ShowControlListener;
import gidraf.tiaplayer.viewModels.HistorySongsViewModels;

public class SongHistoryAdapter extends RecyclerView.Adapter<SongHistoryHolder> implements
        CurrentPositionListener {

    private List<HistoryModel> songsList;
    private Context context;
    MusicService musicService;
    boolean musicBound;
    Intent playIntent;
    ShowControlListener controlListener;
    CurrentPositionListener currentPositionListener;
    BackgroungListener backgroungListener;
    HistorySongsViewModels historySongsViewModels;



    public SongHistoryAdapter(Context context, MusicService service, ShowControlListener controlListener,
                              CurrentPositionListener currentPositionListener, BackgroungListener backgroungListener,
                              HistorySongsViewModels historySongsViewModels) {
        this.context = context;
        this.musicService = service;
        this.controlListener = controlListener;
        this.currentPositionListener = currentPositionListener;
        this.backgroungListener = backgroungListener;
        this.historySongsViewModels = historySongsViewModels;
    }


    @NonNull
    @Override
    public SongHistoryHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View songView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.song_holder,viewGroup, false);
        if(playIntent==null){
            playIntent = new Intent(context, MusicService.class);
            context.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            context.startService(playIntent);
        }

        return new SongHistoryHolder(songView);
    }

   public void setSongsList(List<HistoryModel> models){
        songsList = models;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull final SongHistoryHolder songHistoryHolder, final int i) {
        final HistoryModel song = songsList.get(songHistoryHolder.getAdapterPosition());
        songHistoryHolder.songTitle.setText(song.getSongName());
        Bitmap art = Constants.getRawByt(context, song.getAlbumId());
        if(art!=null){
            songHistoryHolder.songImage.setImageBitmap(art);
        }
        songHistoryHolder.removeSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(songsList!=null || !songsList.isEmpty()) {
                    songsList.remove(songHistoryHolder.getAdapterPosition());
                    notifyDataSetChanged();
                    historySongsViewModels.removeHistorySong(song);
                }
            }
        });

        final Bitmap finalBitmap = art;
        songHistoryHolder.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.setSong(songHistoryHolder.getAdapterPosition());
                controlListener.showControl(true);
                if(finalBitmap !=null){
                backgroungListener.backgroundTobeDisplayed(finalBitmap);
                }
                try {
                    musicService.playHistorySong(10);
                    setSongDuration(musicService.getSongDuration());
                    controlListener.setSessionid(musicService.getsessionId());
                    controlListener.getCurrentSongPlaying(true,song);
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
        return songsList == null ? 0 :songsList.size();
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicService = binder.getService();
            musicService.setHistorySongs(songsList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public void setSongDuration(int duration) {
        currentPositionListener.setSongDuration(duration);
    }

};


class SongHistoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public ImageView removeSong, play;
    public TextView songTitle, songArtist;
    View.OnClickListener clickListener;
    public PaletteShadowView songImage;


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


