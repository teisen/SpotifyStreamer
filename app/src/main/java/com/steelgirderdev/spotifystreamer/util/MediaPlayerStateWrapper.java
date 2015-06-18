package com.steelgirderdev.spotifystreamer.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;

import com.steelgirderdev.spotifystreamer.service.MediaPlayerService;

import java.io.IOException;
import java.util.EnumSet;

/**
 * Source: https://gist.github.com/bitops/2523027
 * A wrapper class for {@link android.media.MediaPlayer}.
 * <p>
 * Encapsulates an instance of MediaPlayer, and makes a record of its internal state accessible via an
 * accessor. Most of the frequently used methods are available, but some still
 * need adding.
 * </p>
 */
public class MediaPlayerStateWrapper {

    private static String tag = "MediaPlayerWrapper";
    private MediaPlayer mPlayer;
    private State currentState;
    private MediaPlayerStateWrapper mWrapper;
    private MediaPlayerService mService;

    public MediaPlayerStateWrapper(MediaPlayerService service) {
        mWrapper = this;
        mService = service;
        mPlayer = new MediaPlayer();
        currentState = State.IDLE;
        mPlayer.setOnPreparedListener(mOnPreparedListener);
        mPlayer.setOnCompletionListener(mOnCompletionListener);
        mPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mPlayer.setOnErrorListener(mOnErrorListener);
        mPlayer.setOnInfoListener(mOnInfoListener);
    }

    public void setAudioStreamType(int streamMusic) {
        mPlayer.setAudioStreamType(streamMusic);
    }

    public void setWakeMode(Context applicationContext, int partialWakeLock) {
        mPlayer.setWakeMode(applicationContext, partialWakeLock);
    }

    /* METHOD WRAPPING FOR STATE CHANGES */
    public static enum State {
        IDLE, ERROR, INITIALIZED, PREPARING, PREPARED, STARTED, STOPPED, PLAYBACK_COMPLETE, PAUSED;
    }

    public void setDataSource(String path) {
        if (currentState == State.IDLE) {
            try {
                mPlayer.setDataSource(path);
                currentState = State.INITIALIZED;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException(currentState.name());
        }
    }

    public void prepareAsync() {
        Log.d(tag, "prepareAsync()");
        if (EnumSet.of(State.INITIALIZED, State.STOPPED).contains(currentState)) {
            mPlayer.prepareAsync();
            currentState = State.PREPARING;
        } else throw new RuntimeException(currentState.name());
    }

    public boolean isPlaying() {
        //Log.d(tag, "isPlaying()");
        if (currentState != State.ERROR) {
            return mPlayer.isPlaying();
        } else throw new RuntimeException(currentState.name());
    }

    public boolean isStopped() {
        return (currentState == State.STOPPED);
    }

    public boolean isPlaybackComplete() {
        return (currentState == State.PLAYBACK_COMPLETE);
    }

    public boolean isPlayingSave() {
        if (currentState != State.ERROR) {
            return mPlayer.isPlaying();
        } else {
            return false;
        }
    }

    public void seekTo(int msec) {
        Log.d(tag, "seekTo()");
        if (EnumSet.of(State.PREPARED, State.STARTED, State.PAUSED, State.PLAYBACK_COMPLETE).contains(currentState)) {
            mPlayer.seekTo(msec);
        } else {
            //throw new RuntimeException(currentState.name());
            Log.d(tag, "seekTo() was not done, as the transition was not allowed");
        }
    }

    public void pause() {
        Log.d(tag, "pause()");
        if (EnumSet.of(State.STARTED, State.PAUSED).contains(currentState)) {
            mPlayer.pause();
            currentState = State.PAUSED;
        } else throw new RuntimeException(currentState.name());
    }

    public void start() {
        Log.d(tag, "start()");
        if (EnumSet.of(State.PREPARED, State.STARTED, State.PAUSED, State.PLAYBACK_COMPLETE).contains(currentState)) {
            mPlayer.start();
            currentState = State.STARTED;
        } else {
            //throw new RuntimeException(currentState.name());
            Log.d(tag, "start() was not done, as the transition was not allowed");
        }
    }

    public void stop() {
        Log.d(tag, "stop()");
        if (EnumSet.of(State.PREPARED, State.STARTED, State.STOPPED, State.PAUSED, State.PLAYBACK_COMPLETE).contains(
                currentState)) {
            mPlayer.stop();
            currentState = State.STOPPED;
        } else throw new RuntimeException(currentState.name());
    }

    public void reset() {
        Log.d(tag, "reset()");
        mPlayer.reset();
        currentState = State.IDLE;
    }

    /**
     * @return The current state of the mediaplayer state machine.
     */
    public State getState() {
        Log.d(tag, "getState()");
        return currentState;
    }

    public void release() {
        Log.d(tag, "release()");
        mPlayer.release();
    }

    /* INTERNAL LISTENERS */
    private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.d(tag, "on prepared");
            currentState = State.PREPARED;
            mWrapper.onPrepared(mp);
            mPlayer.start();
            currentState = State.STARTED;
        }
    };
    private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.d(tag, "on completion");
            currentState = State.PLAYBACK_COMPLETE;
            mWrapper.onCompletion(mp);
        }
    };
    private OnBufferingUpdateListener mOnBufferingUpdateListener = new OnBufferingUpdateListener() {

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            Log.d(tag, "on buffering update");
            mWrapper.onBufferingUpdate(mp, percent);
        }
    };
    private OnErrorListener mOnErrorListener = new OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.d(tag, "on error");
            currentState = State.ERROR;
            mWrapper.onError(mp, what, extra);
            return false;
        }
    };
    private OnInfoListener mOnInfoListener = new OnInfoListener() {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.d(tag, "on info");
            mWrapper.onInfo(mp, what, extra);
            return false;
        }
    };

    /* EXTERNAL STUBS TO OVERRIDE */
    public void onPrepared(MediaPlayer mp) {
        mService.onPrepared(mp);
    }

    public void onCompletion(MediaPlayer mp) {
        mService.onComplete(mp);
    }

    public void onBufferingUpdate(MediaPlayer mp, int percent) {}

    boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    /* OTHER STUFF */
    public int getCurrentPosition() {
        if (currentState != State.ERROR) {
            return mPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public int getDuration() {
        // Prepared, Started, Paused, Stopped, PlaybackCompleted
        if (EnumSet.of(State.PREPARED, State.STARTED, State.PAUSED, State.STOPPED, State.PLAYBACK_COMPLETE).contains(
                currentState)) {
            return mPlayer.getDuration();
        } else {
            return 100;
        }
    }
}