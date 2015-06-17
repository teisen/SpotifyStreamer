package com.steelgirderdev.spotifystreamer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.R;
import com.steelgirderdev.spotifystreamer.model.TopTracks;
import com.steelgirderdev.spotifystreamer.model.TrackUiUpdate;
import com.steelgirderdev.spotifystreamer.ui.PlayerActivity;
import com.steelgirderdev.spotifystreamer.util.MediaPlayerStateWrapper;

import java.io.IOException;
import java.util.Set;

public class MediaPlayerService extends Service implements MediaPlayer.OnErrorListener {
    private LocalBroadcastManager broadcaster;
    private ProgressHandler progressHandler;
    private boolean stopProgressUpdate = false;

    public MediaPlayerService() {
    }


    MediaPlayerStateWrapper mMediaPlayer = null;
    TopTracks topTracks;
    WifiManager.WifiLock wifiLock;

    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
        getOrCreateMediaplayer();
        getOrCreateWifiLock();

    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent!=null && intent.getAction()!=null) {
            Log.v(Constants.LOG_TAG, "mp:" + mMediaPlayer.hashCode() + " action= " + intent.getAction());

            Set<String> keys = intent.getExtras().keySet();
            for(String key : keys) {
                Log.v(Constants.LOG_TAG, "Key:" + key);
            }

            switch (intent.getAction()) {

                case Constants.ACTION_PLAY: {
                    try {
                        // load topTracks from the Intent
                        if(intent.hasExtra(Constants.EXTRA_TOP_TRACKS)) {
                            topTracks = (TopTracks) intent.getExtras().get(Constants.EXTRA_TOP_TRACKS);
                        }
                        playCurrentTrack();
                    } catch (IllegalArgumentException iae) {
                        iae.printStackTrace();
                        //TODO UIUtil.toastIt(getActivity(), toast, getString(R.string.toast_could_not_play_file));
                    }
                    break;
                }
                case Constants.ACTION_PLAY_IF_NOT_PLAYING: {
                    try {
                        // load topTracks from the Intent
                        if(intent.hasExtra(Constants.EXTRA_TOP_TRACKS)) {
                            topTracks = (TopTracks) intent.getExtras().get(Constants.EXTRA_TOP_TRACKS);
                        }
                        if(!mMediaPlayer.isPlayingSave()) {
                            playCurrentTrack();
                        }
                    } catch (IllegalArgumentException iae) {
                        iae.printStackTrace();
                        //TODO UIUtil.toastIt(getActivity(), toast, getString(R.string.toast_could_not_play_file));
                    }
                    break;
                }
                case Constants.ACTION_NEXT: {
                    try {
                        // load topTracks from the Intent
                        if(intent.hasExtra(Constants.EXTRA_TOP_TRACKS)) {
                            topTracks = (TopTracks) intent.getExtras().get(Constants.EXTRA_TOP_TRACKS);
                        }
                        topTracks.getNextTrack();
                        playCurrentTrack();
                    } catch (IllegalArgumentException iae) {
                        iae.printStackTrace();
                        //TODO UIUtil.toastIt(getActivity(), toast, getString(R.string.toast_could_not_play_file));
                    }
                    break;
                }
                case Constants.ACTION_NEXT_FROM_NOTIFICATION: {
                    try {
                        topTracks.getNextTrack();
                        playCurrentTrack();
                        Log.v(Constants.LOG_TAG, intent.getAction() + " " + topTracks.toString());
                    } catch (IllegalArgumentException iae) {
                        iae.printStackTrace();
                        //TODO UIUtil.toastIt(getActivity(), toast, getString(R.string.toast_could_not_play_file));
                    }
                    break;
                }
                case Constants.ACTION_PREVIOUS: {
                    try {
                        // load topTracks from the Intent
                        if(intent.hasExtra(Constants.EXTRA_TOP_TRACKS)) {
                            topTracks = (TopTracks) intent.getExtras().get(Constants.EXTRA_TOP_TRACKS);
                        }
                        topTracks.getPreviousTrack();
                        playCurrentTrack();
                        Log.v(Constants.LOG_TAG, intent.getAction() + " " + topTracks.toString());
                    } catch (IllegalArgumentException iae) {
                        iae.printStackTrace();
                        //TODO UIUtil.toastIt(getActivity(), toast, getString(R.string.toast_could_not_play_file));
                    }
                    break;
                }
                case Constants.ACTION_PREVIOUS_FROM_NOTIFICATION: {
                    try {
                        topTracks.getPreviousTrack();
                        playCurrentTrack();
                        Log.v(Constants.LOG_TAG, intent.getAction() + " " + topTracks.toString());
                    } catch (IllegalArgumentException iae) {
                        iae.printStackTrace();
                        //TODO UIUtil.toastIt(getActivity(), toast, getString(R.string.toast_could_not_play_file));
                    }
                    break;
                }
                case Constants.ACTION_PLAYPAUSETOGGLE: {
                    try {
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.pause();
                            //When you pause or stop your media, or when you no longer need the network, you should release the lock:
                            wifiLock.release();
                            createNotification(false);
                        } else {
                            wifiLock.acquire();
                            mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition());
                            mMediaPlayer.start();
                            progressHandler = new ProgressHandler();
                            progressHandler.execute();
                            createNotification(true);
                        }

                    } catch (IllegalArgumentException iae) {
                        iae.printStackTrace();
                        //TODO UIUtil.toastIt(getActivity(), toast, getString(R.string.toast_could_not_play_file));
                    }
                    break;
                }
                case Constants.ACTION_STOP: {
                    try {
                        mMediaPlayer.stop();
                        //When you pause or stop your media, or when you no longer need the network, you should release the lock:
                        wifiLock.release();
                        // stop the foreground mode, but keep the notification active
                        stopForeground(false);
                    } catch (IllegalArgumentException iae) {
                        iae.printStackTrace();
                        //TODO UIUtil.toastIt(getActivity(), toast, getString(R.string.toast_could_not_play_file));
                    }
                    break;
                }
                case Constants.ACTION_STOP_PROGRESS_UPDATE: {
                    stopProgressUpdate = true;
                    break;
                }
                case Constants.ACTION_SEEK_TO: {
                    int millis = intent.getIntExtra(Constants.EXTRA_SEEK_TO_MILLIS, 0);
                    mMediaPlayer.seekTo(millis);
                    progressHandler = new ProgressHandler();
                    progressHandler.execute();
                    break;
                }
                default: {
                    new IllegalArgumentException("No handler for action " + intent.getAction()).printStackTrace();
                    break;
                }
            }
        }

        return START_STICKY;
    }

    private void playCurrentTrack() {
        // before playing a new song, stop the mp
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.reset();

        String url = topTracks.getCurrentTrack().urlPreview; // your URL here
        mMediaPlayer.setDataSource(url);
        // To ensure that the CPU continues running while your MediaPlayer is playing, call the setWakeMode() method when initializing your MediaPlayer. Once you do, the MediaPlayer holds the specified lock while playing and releases the lock when paused or stopped:
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        //However, the wake lock acquired in this example guarantees only that the CPU remains awake. If you are streaming media over the network and you are using Wi-Fi, you probably want to hold a WifiLock as well, which you must acquire and release manually. So, when you start preparing the MediaPlayer with the remote URL, you should create and acquire the Wi-Fi lock. For example:
        wifiLock.acquire();

        createNotification(true);

        // broadcast the playing track
        Intent intent = new Intent(Constants.BROADCAST_INTENT_TRACKSTARTED);
        intent.putExtra(Constants.PARCEL_KEY_TOPTRACKS, topTracks);
        broadcaster.sendBroadcast(intent);

        mMediaPlayer.prepareAsync(); // will call OnPreparedListener
    }

    public void onPrepared(MediaPlayer mp) {
        Log.v(Constants.LOG_TAG, "on prepared called in MediaPLayerService");
        progressHandler = new ProgressHandler();
        progressHandler.execute();
    }

    public void onComplete(MediaPlayer mp) {

    }


    /**
     * Implementation from MediaPlayer.OnErrorListener - is called when the AsyncPrepare is failing
     * @param mp The Mediaplayer
     * @param what what errored
     * @param extra which extra
     * @return True if the method handled the error, false if it didn't. Returning false, or not having an OnErrorListener at all, will cause the OnCompletionListener to be called.
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // The MediaPlayer has moved to the Error state, must be reset!
        mMediaPlayer.reset();
        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private MediaPlayerStateWrapper getOrCreateMediaplayer() {
        if(mMediaPlayer==null) {
            mMediaPlayer = new MediaPlayerStateWrapper(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        return mMediaPlayer;
    }

    private WifiManager.WifiLock getOrCreateWifiLock() {
        if(wifiLock==null) {
            wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, Constants.WIFI_LOCK_KEY);
        }
        return wifiLock;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        //When you pause or stop your media, or when you no longer need the network, you should release the lock:
        wifiLock.release();
        // stop the foreground mode and delete notification
        //Currently not foreground stopForeground(true);
    }

    public class ProgressHandler extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void result) {
            Log.d(Constants.LOG_TAG,"################### Destroyed ProgressHandler ##################");
            stopProgressUpdate = false;
            Intent intent = new Intent(Constants.BROADCAST_INTENT_TRACKUIUPDATE);
            intent.putExtra(Constants.PARCEL_KEY_TRACK_UI_UPDATE, new TrackUiUpdate(mMediaPlayer.getCurrentPosition(), topTracks.playerpos,  mMediaPlayer.isPlayingSave()));
            broadcaster.sendBroadcast(intent);
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //updates the seekBar in the fragment
            super.onProgressUpdate(values);
            Intent intent = new Intent(Constants.BROADCAST_INTENT_TRACKUIUPDATE);
            intent.putExtra(Constants.PARCEL_KEY_TRACK_UI_UPDATE, new TrackUiUpdate(mMediaPlayer.getCurrentPosition(), topTracks.playerpos, mMediaPlayer.isPlayingSave()));
            broadcaster.sendBroadcast(intent);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                Thread.sleep(500);
                while(mMediaPlayer.isPlaying() &! stopProgressUpdate) {
                    Thread.sleep(200);
                    onProgressUpdate();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

    }

    /**
     * Displays a notification with actions and image
     * @param isPlaying set to true to force the display of the pause icon, use when loading a new track
     */
    public void createNotification(final boolean isPlaying) {

        Target target2 = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                //customNotification();
                customNotification2(bitmap, topTracks.getCurrentTrack().trackname, topTracks.artist.artistname, isPlaying);
/*
                // Prepare intent which is triggered if the
                // notification is selected
                Intent restoreIntent = new Intent(getApplicationContext(), PlayerActivity.class);
                TopTracks restoremTopTracks = topTracks.createClone();
                restoremTopTracks.command = Constants.ACTION_NONE;
                restoreIntent.putExtra(Constants.EXTRA_TOP_TRACKS, topTracks);
                restoreIntent.putExtra(Constants.EXTRA_PLAYER_COMMAND, Constants.ACTION_NONE);
                final PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, restoreIntent, 0);

                Notification noti;
                if(forcePauseicon || mMediaPlayer.isPlayingSave()) {
                    // Build notification
                    noti = new Notification.Builder(getApplicationContext())
                            .setContentTitle(topTracks.getCurrentTrack().trackname)
                            .setContentText(topTracks.artist.artistname)
                            .setSmallIcon(android.R.drawable.ic_media_play)
                            .setLargeIcon(bitmap)
                            .setContentIntent(pi)
                            .addAction(R.drawable.ic_skip_previous_white_48dp, "Prev", getPendingIntent(Constants.ACTION_PREVIOUS_FROM_NOTIFICATION, 1))
                            .addAction(R.drawable.ic_pause_white_48dp, "Pause/Play", getPendingIntent(Constants.ACTION_PLAYPAUSETOGGLE, 2))
                            .addAction(R.drawable.ic_skip_next_white_48dp, "Next", getPendingIntent(Constants.ACTION_NEXT_FROM_NOTIFICATION, 3))
                            .build();

                } else {
                    // Build notification
                    noti = new Notification.Builder(getApplicationContext())
                            .setContentTitle(topTracks.getCurrentTrack().trackname)
                            .setContentText(topTracks.artist.artistname)
                            .setSmallIcon(R.drawable.ic_play_arrow_white_48dp)
                            .setLargeIcon(bitmap)
                            .setContentIntent(pi)
                            .addAction(R.drawable.ic_skip_previous_white_48dp, "Prev", getPendingIntent(Constants.ACTION_PREVIOUS_FROM_NOTIFICATION, 1))
                            .addAction(R.drawable.ic_play_arrow_white_48dp, "Pause/Play", getPendingIntent(Constants.ACTION_PLAYPAUSETOGGLE, 2))
                            .addAction(R.drawable.ic_skip_next_white_48dp, "Next", getPendingIntent(Constants.ACTION_NEXT_FROM_NOTIFICATION, 3))
                            .build();
                }
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                // hide the notification after its selected
                noti.flags |= Notification.FLAG_FOREGROUND_SERVICE;

                notificationManager.notify(Constants.NOTIFICATION_ID_PLAYER, noti);
*/

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.e(Constants.LOG_TAG, "Could not load Bitmap");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.v(Constants.LOG_TAG, "onPrepareLoad");
            }
        };

        Picasso.with(this)
                .load(topTracks.getCurrentTrack().urlHighres)
                .into(target2);

    }

    private PendingIntent getPendingIntent(String action, int requestCode) {
        Intent serviceIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
        serviceIntent.putExtra(Constants.EXTRA_TOP_TRACKS, topTracks);
        serviceIntent.setAction(action);
        return PendingIntent.getService(getApplicationContext(), requestCode, serviceIntent, 0);
    }

    public void customNotification2(Bitmap bigIcon, String trackname, String artistname, boolean isPlaying) {
        Notification foregroundNote;

        // Open NotificationView Class on Notification Click
        Intent restoreIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        TopTracks restoremTopTracks = topTracks.createClone();
        restoremTopTracks.command = Constants.ACTION_NONE;
        restoreIntent.putExtra(Constants.EXTRA_TOP_TRACKS, topTracks);
        restoreIntent.putExtra(Constants.EXTRA_PLAYER_COMMAND, Constants.ACTION_NONE);
        final PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, restoreIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews bigView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);

        if (android.os.Build.VERSION.SDK_INT > 20) {
            // I am not sure of this method
            //bigView.setTextColor(R.id.title, Color.BLACK);
            bigView.setTextColor(R.id.notification_trackname, Color.BLACK);
            bigView.setTextColor(R.id.notification_artistname, Color.BLACK);
        }

        bigView.setTextViewText(R.id.notification_trackname, trackname);

        bigView.setTextViewText(R.id.notification_artistname, artistname);

        bigView.setImageViewBitmap(R.id.notification_thumbnail, bigIcon);

        bigView.setImageViewResource(R.id.notification_prev, R.drawable.ic_skip_previous_black_24dp);
        bigView.setOnClickPendingIntent(R.id.notification_prev, getPendingIntent(Constants.ACTION_PREVIOUS_FROM_NOTIFICATION, 1));

        if(!isPlaying) {
            bigView.setImageViewResource(R.id.notification_playpause, R.drawable.ic_play_arrow_black_24dp);
        } else {
            bigView.setImageViewResource(R.id.notification_playpause, R.drawable.ic_pause_black_24dp);
        }
        bigView.setOnClickPendingIntent(R.id.notification_playpause, getPendingIntent(Constants.ACTION_PLAYPAUSETOGGLE, 2));

        bigView.setImageViewResource(R.id.notification_next, R.drawable.ic_skip_next_black_24dp);
        bigView.setOnClickPendingIntent(R.id.notification_next, getPendingIntent(Constants.ACTION_NEXT_FROM_NOTIFICATION, 3));

        bigView.setImageViewResource(R.id.notification_stop, android.R.drawable.ic_menu_close_clear_cancel);
        bigView.setOnClickPendingIntent(R.id.notification_stop, getPendingIntent(Constants.ACTION_STOP, 4));


        // bigView.setOnClickPendingIntent() etc..

        Notification.Builder mNotifyBuilder = new Notification.Builder(this);
        foregroundNote = mNotifyBuilder.setContentTitle(trackname)
                .setContentText(artistname)
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                .setLargeIcon(bigIcon)
                .build();

        foregroundNote.bigContentView = bigView;

        // now show notification..
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.notify(1, foregroundNote);
    }


}
