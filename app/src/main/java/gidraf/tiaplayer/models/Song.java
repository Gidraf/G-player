package gidraf.tiaplayer.models;

import android.graphics.Bitmap;
import android.net.Uri;

public class Song {
    private String songTitle, artist, album;
    private String songImage;
    private Uri songUri;
    private long songId;

    public Song(String songTitle, String artist, String album, String songImage, Uri songUri, long songId) {
        this.songTitle = songTitle;
        this.artist = artist;
        this.album = album;
        this.songImage = songImage;
        this.songUri = songUri;
        this.songId = songId;
    }


    public Song() {
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public String getSongTitle() {
        return songTitle;
    }


    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getSongImage() {
        return songImage;
    }

    public void setSongImage(String  songImage) {
        this.songImage = songImage;
    }
}
