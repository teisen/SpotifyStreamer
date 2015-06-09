package com.steelgirderdev.spotifystreamer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.R;
import com.steelgirderdev.spotifystreamer.model.TopTracks;
import com.steelgirderdev.spotifystreamer.model.TrackUiUpdate;
import com.steelgirderdev.spotifystreamer.ui.PlayerActivity;
import com.steelgirderdev.spotifystreamer.util.MediaPlayerStateWrapper;

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
            switch (intent.getAction()) {

                case Constants.ACTION_PLAY: {
                    try {
                        // load topTracks from the Intent
                        topTracks = (TopTracks) intent.getExtras().get(Constants.EXTRA_TOP_TRACKS);
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
                        topTracks = (TopTracks) intent.getExtras().get(Constants.EXTRA_TOP_TRACKS);
                        if(mMediaPlayer.isPlayingSave() == false) {
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
                        topTracks = (TopTracks) intent.getExtras().get(Constants.EXTRA_TOP_TRACKS);
                        topTracks.getNextTrack();
                        playCurrentTrack();
                    } catch (IllegalArgumentException iae) {
                        iae.printStackTrace();
                        //TODO UIUtil.toastIt(getActivity(), toast, getString(R.string.toast_could_not_play_file));
                    }
                    break;
                }
                case Constants.ACTION_PREVIOUS: {
                    try {
                        // load topTracks from the Intent
                        topTracks = (TopTracks) intent.getExtras().get(Constants.EXTRA_TOP_TRACKS);
                        topTracks.getPreviousTrack();
                        playCurrentTrack();
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
                        } else {
                            wifiLock.acquire();
                            mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition());
                            mMediaPlayer.start();
                            progressHandler = new ProgressHandler();
                            progressHandler.execute();
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

        // set the service in foreground
        //setForeground(topTracks.getCurrentTrack().trackname, topTracks.artist.artistname);
        createNotification(topTracks.getCurrentTrack().trackname, topTracks.artist.artistname);

        mMediaPlayer.prepareAsync(); // will call OnPreparedListener
    }

    /**
     *
     * @param mp
     */
    public void onPrepared(MediaPlayer mp) {
        Log.v(Constants.LOG_TAG, "on prepared called in MediaPLayerService");
        progressHandler = new ProgressHandler();
        progressHandler.execute();
    }

    public void onComplete(MediaPlayer mp) {

    }


    /**
     * Implementation from MediaPlayer.OnErrorListener - is called when the AsyncPrepare is failing
     * @param mp
     * @param what
     * @param extra
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

    private void setForeground(String songName, String artistname) {
        // assign the song name to songName
        Intent myIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        myIntent.putExtra(Constants.EXTRA_TOP_TRACKS, topTracks);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification();
        notification.tickerText = "abcd";
        notification.icon = R.drawable.ic_play_arrow_black_36dp;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(getApplicationContext(), artistname,
                songName, pi);

        startForeground(Constants.NOTIFICATION_ID_PLAYER, notification);
    }

    public void createNotification(String songName, String artistname) {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent restoreIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        TopTracks restoreTopTracks = topTracks.createClone();
        restoreTopTracks.command = Constants.ACTION_NONE;
        restoreIntent.putExtra(Constants.EXTRA_TOP_TRACKS, topTracks);
        restoreIntent.putExtra(Constants.EXTRA_PLAYER_COMMAND, Constants.ACTION_NONE);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, restoreIntent, 0);

        Intent prevIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        TopTracks prevTopTracks = topTracks.createClone();
        prevTopTracks.command = Constants.ACTION_PREVIOUS;
        prevIntent.putExtra(Constants.EXTRA_TOP_TRACKS, prevTopTracks);
        PendingIntent prevPi = PendingIntent.getActivity(getApplicationContext(), 1, prevIntent, 0);

        Intent playPauseIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        TopTracks playPauseTopTracks = topTracks.createClone();
        playPauseTopTracks.command = Constants.ACTION_PLAYPAUSETOGGLE;
        playPauseIntent.putExtra(Constants.EXTRA_TOP_TRACKS, playPauseTopTracks);
        PendingIntent playPausePi = PendingIntent.getActivity(getApplicationContext(), 2, playPauseIntent, 0);

        Intent nextIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        TopTracks nextTopTracks = topTracks.createClone();
        nextTopTracks.command = Constants.ACTION_NEXT;
        nextIntent.putExtra(Constants.EXTRA_TOP_TRACKS, nextTopTracks);
        PendingIntent nextPi = PendingIntent.getActivity(getApplicationContext(), 3, nextIntent, 0);

        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(this)
                .setContentTitle(songName)
                .setContentText(artistname)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.abc_ic_commit_search_api_mtrl_alpha))
                .setContentIntent(pi)
                .addAction(R.drawable.ic_skip_previous_white_48dp, "Prev", prevPi)
                .addAction(R.drawable.ic_pause_white_48dp, "Pause/Play", playPausePi)
                .addAction(R.drawable.ic_skip_next_white_48dp, "Next", nextPi)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(Constants.NOTIFICATION_ID_PLAYER, noti);

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
}