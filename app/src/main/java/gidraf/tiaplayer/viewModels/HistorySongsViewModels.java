package gidraf.tiaplayer.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import gidraf.tiaplayer.models.database.HistoryModel;
import gidraf.tiaplayer.models.database.HistorySongRepository;

public class HistorySongsViewModels extends AndroidViewModel {
    private HistorySongRepository historySongRepository;

    private LiveData<List<HistoryModel>> historySongs;

    public HistorySongsViewModels(@NonNull Application application) {
        super(application);
        historySongRepository = new HistorySongRepository(application);
        historySongs = historySongRepository.getHistorySongs();
    }

    public void addHstorySong(HistoryModel model){
        historySongRepository.addHistorySong(model);
    }

    public void removeHistorySong(HistoryModel model){
        historySongRepository.deleteHistorySong(model);
    }

    public void clearHistorySongs(){
        historySongRepository.clearHistorySongs();
    }

    public LiveData<List<HistoryModel>> getAllHistorySongs(){
        return historySongs;
    }
}
