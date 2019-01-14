package gidraf.tiaplayer.utils;

import gidraf.tiaplayer.models.database.HistoryModel;

public interface ShowControlListener {
    void showControl(boolean show);
    void setSessionid(int id);
    void getCurrentSongPlaying(HistoryModel currentSon);
}
