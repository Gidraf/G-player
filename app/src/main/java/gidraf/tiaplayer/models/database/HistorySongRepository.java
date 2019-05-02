package gidraf.tiaplayer.models.database;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

public class HistorySongRepository {
    private HistoryDatabase database;
    public LiveData<List<HistoryModel>> historySongs;
    Context context;
    HistoryModelDao historyModelDao;

    public HistorySongRepository(Context context) {
        this.context = context;
        database = HistoryDatabase.getDatabase(context);
        historyModelDao = database.historyModelDao();
        historySongs = historyModelDao.getall();
    }

    public LiveData<List<HistoryModel>> getHistorySongs(){
        return historySongs;
    }

    public void addHistorySong (HistoryModel model) {
        new insertAsyncTask(historyModelDao).execute(model);
    }

    public  void clearHistorySongs(){
        new clearAllHistorySongAsyncTask(historyModelDao).execute();
    }

    public void deleteHistorySong(HistoryModel model){
        new deleteHistoryAsyncTask(historyModelDao).execute(model);
    }

    private static class insertAsyncTask extends AsyncTask<HistoryModel, Void, Void> {

        private HistoryModelDao mAsyncTaskDao;

        insertAsyncTask(HistoryModelDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final HistoryModel... params) {
            mAsyncTaskDao.addsong(params[0]);
            return null;
        }
    }

    private static class deleteHistoryAsyncTask extends AsyncTask<HistoryModel, Void, Void> {

        private HistoryModelDao mAsyncTaskDao;

        deleteHistoryAsyncTask(HistoryModelDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final HistoryModel... params) {
            mAsyncTaskDao.remove(params[0]);
            return null;
        }
    }

    private static class clearAllHistorySongAsyncTask extends AsyncTask<Void, Void, Void> {

        private HistoryModelDao mAsyncTaskDao;

        clearAllHistorySongAsyncTask(HistoryModelDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mAsyncTaskDao.clearHistory();
            return null;
        }
    }



}
