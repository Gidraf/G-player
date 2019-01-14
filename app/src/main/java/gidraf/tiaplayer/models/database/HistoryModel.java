package gidraf.tiaplayer.models.database;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class HistoryModel  {
    @PrimaryKey(autoGenerate=true)
    private int  id;

    @ColumnInfo(name = "songName")
    private String songName;

    @ColumnInfo(name = "songArtist")
    private String songArtist;

    @ColumnInfo(name = "album")
    private String album;

    @ColumnInfo (name = "songid")
    long songId;

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }
}
