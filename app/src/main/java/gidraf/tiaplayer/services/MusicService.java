package gidraf.tiaplayer.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import gidraf.tiaplayer.R;
import gidraf.tiaplayer.models.Song;
import gidraf.tiaplayer.models.database.HistoryModel;
import gidraf.tiaplayer.utils.Constants;
import gidraf.tiaplayer.utils.listeners.BackgroungListener;
import gidraf.tiaplayer.utils.PlaybackStatus;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener, View.OnClickListener, BackgroungListener {


    private MediaPlayer mediaPlayer;
    private List<Song> playlistSongs;
    private List<HistoryModel> songhistorySongs;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;
    private boolean shuffle = false;
    private Random rand;
    private int songposition;
    public boolean showControl = false;
    Uri currentUri;
    public boolean automaticNex = true;
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_STOP = "ACTION_STOP";

    private final static int INT_VOLUME_MAX = 100;
    private final static int INT_VOLUME_MIN = 0;
    private final static float FLOAT_VOLUME_MAX = 1;
    private final static float FLOAT_VOLUME_MIN = 0;
    private int iVolume;
    AudioManager audioManager;
    Equalizer equalizer;
    Bitmap logoicon;
    private HistoryModel model;
    private String source;

    private static final IntentFilter AUDIO_NOISY_INTENT_FILTER =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private boolean mAudioNoisyReceiverRegistered = false;

    final BroadcastReceiver mAudioNoisyReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                        if (isPng()) {
                            pauseSoung(10);
                        }
                    }
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();
        songposition = 0;
        rand = new Random();
        initMediaPlayer();
    }

    private void handleIntent(Intent intent){
        String action = "";
        if(intent!=null){
            if(intent.getAction() == null){
                return;
            }
            action = intent.getAction();
        }
        else {
            return;
        }

        if(action.equalsIgnoreCase(ACTION_PLAY)){
            if(isPng()){
                pauseSoung(10);
            }
            else {
                go(10);
            }
        } else if (action.equalsIgnoreCase(ACTION_NEXT)){
            try {
                playNext();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            try {
                playPrev();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        updateMetaData(model == null ? null : model);

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();

                go(10);
                buildNotification(PlaybackStatus.PLAYING);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startMyOwnForeground(PlaybackStatus.PLAYING);
                }
            }

            @Override
            public void onPause() {
                super.onPause();

                pauseSoung(10);
                buildNotification(PlaybackStatus.PAUSED);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startMyOwnForeground(PlaybackStatus.PAUSED);
                }
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();

                try {
                    playNext();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                updateMetaData(model == null ? null : model);
                buildNotification(PlaybackStatus.PLAYING);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startMyOwnForeground(PlaybackStatus.PLAYING);
                }
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();

                try {
                    playNext();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                updateMetaData(model == null ? null : model);
                buildNotification(PlaybackStatus.PLAYING);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startMyOwnForeground(PlaybackStatus.PLAYING);
                }
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData(HistoryModel model) {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher); // todo replace with medias albumArt
        // Update the current metadata
        if (model != null) {
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, model.getSongArtist())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, model.getAlbum())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, model.getSongName())
                    .build());
        }
    }


    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseSoung(10);
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                go(10);
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }



    public void setSong(int songIndex) {
        songposition = songIndex;
    }

    public boolean setShuffle() {

        if (shuffle) shuffle = false;
        else
            shuffle = true;
        return shuffle;
    }

    public MusicService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    public void setPlayListSongs(List<Song> thesongs) {
        playlistSongs = thesongs;
    }

    public void setHistorySongs(List<HistoryModel> thesongs) {
        songhistorySongs = thesongs;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        mediaSession.release();
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    public void playSong(int fadeDuration) throws IOException {
        registerAudioNoisyReceiver();

        if (fadeDuration > 0)
            iVolume = INT_VOLUME_MIN;
        else
            iVolume = INT_VOLUME_MAX;

        updateVolume(0);
        if(mediaPlayer != null){
        mediaPlayer.reset();
        }
        else {
            initMediaPlayer();
        }
        long currentSong;
        Song playingSong = null;
        showControl = true;
        if(playlistSongs!=null) {
            playingSong = playlistSongs.get(songposition);
            songTitle = playingSong.getSongTitle();
            setModel(null,playingSong);
            currentSong = playingSong.getSongId();
            backgroundTobeDisplayed(playlistSongs.get(songposition).getSongImage());
            currentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);
        }

        try {
            mediaPlayer.setDataSource(getApplicationContext(), currentUri);
            mediaPlayer.prepare();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "MUSIC SERVICE" + " Error setting data source " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        // Start increasing volume in increments
        if (fadeDuration > 0) {
            final Timer timer = new Timer(true);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    updateVolume(1);
                    if (iVolume == INT_VOLUME_MAX) {
                        timer.cancel();
                        timer.purge();
                    }
                }
            };

            // calculate delay, cannot be zero, set to 1 if zero
            int delay = fadeDuration / INT_VOLUME_MAX;
            if (delay == 0)
                delay = 1;

            timer.schedule(timerTask, delay, delay);
        }
    }

    public void playHistorySong(int fadeDuration) throws IOException {
        registerAudioNoisyReceiver();

        if (fadeDuration > 0)
            iVolume = INT_VOLUME_MIN;
        else
            iVolume = INT_VOLUME_MAX;

        updateVolume(0);
        if(mediaPlayer != null){
            mediaPlayer.reset();
        }
        else {
            initMediaPlayer();
        }
        long currentSong;
        HistoryModel playingSong = null;
        showControl = true;
        if (songhistorySongs!=null  || !songhistorySongs.isEmpty()) {
            playingSong = songhistorySongs.get(songposition);
            songTitle = playingSong.getSongName();
            currentSong = playingSong.getSongId();
            currentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);
        }

        try {
            mediaPlayer.setDataSource(getApplicationContext(), currentUri);
            mediaPlayer.prepare();
        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "MUSIC SERVICE" + " Error setting data source " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        // Start increasing volume in increments
        if (fadeDuration > 0) {
            final Timer timer = new Timer(true);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    updateVolume(1);
                    if (iVolume == INT_VOLUME_MAX) {
                        timer.cancel();
                        timer.purge();
                    }
                }
            };

            // calculate delay, cannot be zero, set to 1 if zero
            int delay = fadeDuration / INT_VOLUME_MAX;
            if (delay == 0)
                delay = 1;

            timer.schedule(timerTask, delay, delay);
        }
    }



    public HistoryModel setModel(HistoryModel currentModel,Song playingSong) {
        if(currentModel==null) {
            model = new HistoryModel();
            model.setSongName(playingSong.getSongTitle());
            model.setSongArtist(playingSong.getArtist());
            model.setAlbum(playingSong.getAlbum());
            model.setSongId(songposition);
            model.setAlbumId(playingSong.getSongId());
            return  model;
        }
        model = currentModel;
        return model;

    }
    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) {
                    initMediaPlayer();
                }
                mediaPlayer.setVolume(1,1);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer != null && isPng()){
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;}
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (isPng()) pauseSoung(10);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (isPng()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    public boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    public boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.play_btn:
                if(isPng()){
                    pauseSoung(10);
                }
                else {
                    go(10);
                }

            case R.id.next_btn:
                try {
                    playNext();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            case R.id.prev_btn:

        }
    }

    @Override
    public void backgroundTobeDisplayed(Bitmap bitmap) {
        logoicon = bitmap;
    }


    public class MusicBinder extends Binder {

        public MusicService getService() {
            return MusicService.this;
        }


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        handleIntent(intent);
        callStateListener();
        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }



        if (mediaSessionManager == null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                initMediaSession();
                }
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);

    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        equalizer = new Equalizer(0,mediaPlayer.getAudioSessionId() );
    }

    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            getBaseContext().unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }

    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            getBaseContext().registerReceiver(mAudioNoisyReceiver, AUDIO_NOISY_INTENT_FILTER);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mediaPlayer != null && mediaPlayer.getCurrentPosition()>=0){
            mp.reset();
            try {
                playNext();
                automaticNex = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        buildNotification(PlaybackStatus.PLAYING);

    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MusicService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
            case 4:
                playbackAction.setAction("OPEN");
                break;
        }
        return null;
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }


    private void buildNotification(PlaybackStatus playbackStatus) {
        int notificationAction = android.R.drawable.ic_media_pause;
        PendingIntent play_pauseAction = null;

        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
        // Create a new Notification
             builder = new NotificationCompat.Builder(this)
                // Hide the timestamp
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compat view
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorAccent))
                // Set the large and small icons
                .setLargeIcon(logoicon == null ? BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher): logoicon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(songTitle)
                .setContentTitle("Playing")
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));
        }
        else{
                    builder.setShowWhen(false)
                    .setColor(getResources().getColor(R.color.colorAccent))
                    // Set the large and small icons
                    .setLargeIcon(logoicon == null ? BitmapFactory.decodeResource(getResources(),
                            R.mipmap.ic_launcher): logoicon)
                    .setSmallIcon(android.R.drawable.stat_sys_headset)
                    // Set Notification content information
                    .setContentText(songTitle)
                    .setContentTitle("Playing")
                    // Add playback actions
                    .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                    .addAction(notificationAction, "pause", play_pauseAction)
                    .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));
        }
        Notification not = builder.build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground(PlaybackStatus.PLAYING);
        else
        {startForeground(   NOTIFY_ID, not);}
    }


    public int getSongPosition(){

        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : -1;
    }

    public  int getsessionId(){
        return mediaPlayer!=null ? mediaPlayer.getAudioSessionId() : 1;
    }

    public int getSongDuration(){
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }


    public Uri getcurrentSong (){
        return  currentUri;
    }

    public void pauseSoung(int fadeDuration){
        unregisterAudioNoisyReceiver();
        if (fadeDuration > 0)
            iVolume = INT_VOLUME_MAX;
        else
            iVolume = INT_VOLUME_MIN;

        updateVolume(0);

        // Start increasing volume in increments
        if (fadeDuration > 0) {
            final Timer timer = new Timer(true);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    updateVolume(-1);
                    if (iVolume == INT_VOLUME_MIN) {
                        // Pause music
                        if (mediaPlayer != null && mediaPlayer.isPlaying())
                            mediaPlayer.pause();
                        timer.cancel();
                        timer.purge();
                    }
                }
            };

            // calculate delay, cannot be zero, set to 1 if zero
            int delay = fadeDuration / INT_VOLUME_MAX;
            if (delay == 0)
                delay = 1;

            timer.schedule(timerTask, delay, delay);
        }
    }

    public void seek(int songposition){
        mediaPlayer.seekTo(songposition);
    }

    public void go(int fadeDuration){
        registerAudioNoisyReceiver();
        if (fadeDuration > 0)
            iVolume = INT_VOLUME_MIN;
        else
            iVolume = INT_VOLUME_MAX;

        updateVolume(0);

        // Play music
        if (mediaPlayer!= null && !mediaPlayer.isPlaying())
            mediaPlayer.start();

        // Start increasing volume in increments
        if (fadeDuration > 0) {
            final Timer timer = new Timer(true);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    updateVolume(1);
                    if (iVolume == INT_VOLUME_MAX) {
                        timer.cancel();
                        timer.purge();
                    }
                }
            };

            // calculate delay, cannot be zero, set to 1 if zero
            int delay = fadeDuration / INT_VOLUME_MAX;
            if (delay == 0)
                delay = 1;

            timer.schedule(timerTask, delay, delay);
        }
    }

    public void playPrev() throws IOException {
        songposition--;
        if (mediaPlayer != null){
            mediaPlayer.setVolume(1,1);
        }
        if(songposition>=0){
            setSong(songposition);
        }
        else {
        setSong(playlistSongs.size()-1);
        }
        playSong(10);
    }

    public boolean isPng(){
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }



    public void playNext() throws IOException {
        if(shuffle){
            if (mediaPlayer != null){
                mediaPlayer.setVolume(1,1);
            }
            int newSong = songposition;
            while(newSong==songposition){
                newSong=rand.nextInt(playlistSongs.size());
            }

            songposition=newSong;
        }
        else
        songposition++;
        if(playlistSongs != null && songposition< playlistSongs.size()){
            setSong(songposition);
        }else {
            songposition = 0;
            setSong(0);
        }

        playSong(10);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(PlaybackStatus playbackStatus) {
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        int notificationAction = android.R.drawable.ic_media_pause;
        PendingIntent play_pauseAction = null;

        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }
        // Create a new Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                // Hide the timestamp
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compat view
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorAccent))
                // Set the large and small icons
                .setLargeIcon(logoicon == null ? BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher) : logoicon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(songTitle)
                .setContentTitle("Playing")
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));
        Notification notification = builder.build();
        startForeground(2, notification);

    }

    private void updateVolume(int change) {
        // increment or decrement depending on type of fade
        iVolume = iVolume + change;

        // ensure iVolume within boundaries
        if (iVolume < INT_VOLUME_MIN)
            iVolume = INT_VOLUME_MIN;
        else if (iVolume > INT_VOLUME_MAX)
            iVolume = INT_VOLUME_MAX;

        // convert to float value
        float fVolume = 1 - ((float) Math.log(INT_VOLUME_MAX - iVolume) / (float) Math.log(INT_VOLUME_MAX));

        // ensure fVolume within boundaries
        if (fVolume < FLOAT_VOLUME_MIN)
            fVolume = FLOAT_VOLUME_MIN;
        else if (fVolume > FLOAT_VOLUME_MAX)
            fVolume = FLOAT_VOLUME_MAX;
        if(mediaPlayer!=null) {
            try{
            mediaPlayer.setVolume(fVolume, fVolume);
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public Equalizer getEqualizer() {
        return equalizer;
    }

    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {

            }
        }
    }

    public HistoryModel getModel(){
        return model;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        unregisterAudioNoisyReceiver();
        mediaPlayer.release();
        mediaPlayer = null;
    }

}
