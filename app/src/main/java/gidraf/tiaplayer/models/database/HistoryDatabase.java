package gidraf.tiaplayer.models.database;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.RoomDatabase;
import android.support.annotation.NonNull;

@Database(entities = HistoryModel.class, version = 1)
public abstract class HistoryDatabase extends RoomDatabase {
    public abstract HistoryModelDao historyModelDao();
}
