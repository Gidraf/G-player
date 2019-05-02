package gidraf.tiaplayer.models;

import android.graphics.Bitmap;
import android.net.Uri;

public class Song {
    private String songTitle, artist, album;
    private Bitmap songImage;
    private Bitmap songurl;
    private Uri songUri;
    private long songId;
    private byte[] artwork;
    private Bitmap albumId;

    public Song(String songTitle, String artist, String album,
                Bitmap songImage, Uri songUri, long songId, Bitmap albumId, Bitmap songurl, byte[] artwork) {
        this.songTitle = songTitle;
        this.artist = artist;
        this.album = album;
        this.songImage = songImage;
        this.songUri = songUri;
        this.songId = songId;
        this.albumId = albumId;
        this.songurl = songurl;
        this.artwork = artwork;
    }


    public Song() {
    }

    public Bitmap getSongurl() {
        return songurl;
    }

    public byte[] getArtwork() {
        return artwork;
    }

    public void setArtwork(byte[] artwork) {
        this.artwork = artwork;
    }
    public Bitmap getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Bitmap albumId) {
        this.albumId = albumId;
    }

    public void setSongurl(Bitmap songurl) {
        this.songurl = songurl;
    }

    public Uri getSongUri() {
        return songUri;
    }

    public void setSongUri(Uri songUri) {
        this.songUri = songUri;
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

    public Bitmap getSongImage() {
        return songImage;
    }

    public void setSongImage(Bitmap songImage) {
        this.songImage = songImage;
    }
}
