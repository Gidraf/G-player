package gidraf.tiaplayer.utils;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import gidraf.tiaplayer.R;

public class Constants {
    public static int POSITION = 0;
    public static String PLAYLIST ="playlist";

    public static Bitmap getRawByt(Context context, long albumId) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        byte[] rawArt;
        Bitmap art = null;
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,albumId);
        try{
        mmr.setDataSource(context, uri);
        rawArt = mmr.getEmbeddedPicture();
        if (null != rawArt){
            art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
        }
        }
        catch (Exception e){
            art = BitmapFactory.decodeResource(context.getResources(),R.drawable.holder);
//            Log.i("error", e.getMessage());
        }
        return art;
    }
}
