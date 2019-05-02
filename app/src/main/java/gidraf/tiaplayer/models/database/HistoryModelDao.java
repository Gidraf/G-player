package gidraf.tiaplayer.models.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import gidraf.tiaplayer.models.Song;

@Dao
public interface HistoryModelDao {

    @Query("SELECT * FROM historymodel")
    LiveData<List<HistoryModel>> getall();

    @Delete
    void remove(HistoryModel historyModel);

    @Insert
    void addsong(HistoryModel model);

    @Query("DELETE FROM historymodel")
    void clearHistory();
}
