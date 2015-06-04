package com.steelgirderdev.spotifystreamer.fragment;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.R;
import com.steelgirderdev.spotifystreamer.activity.PlayerActivity;
import com.steelgirderdev.spotifystreamer.model.TopTracks;
import com.steelgirderdev.spotifystreamer.util.UIUtil;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerFragment extends Fragment {

    private TopTracks topTracks;
    private Toast toast;
    private PlayerActivity act;

    public PlayerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.hello_player);

        // load the tracks if resumed
        if(savedInstanceState == null || !savedInstanceState.containsKey(Constants.PARCEL_KEY_TOPTRACKS)) {
            // read intent extras
            topTracks = (TopTracks) getActivity().getIntent().getExtras().get(Constants.EXTRA_TOP_TRACKS);
            Log.v(Constants.LOG_TAG, "Loaded " + topTracks.toString());
        } else {
            // restore topTracks object
            topTracks = savedInstanceState.getParcelable(Constants.PARCEL_KEY_TOPTRACKS);
        }

        act = (PlayerActivity) getActivity();
        stop();
        act.mediaPlayer = new MediaPlayer();

        textView.setText(topTracks.getCurrentTrack().trackname);

        play();

        return rootView;
    }

    private void play() {
        stop();

        try {
            Log.v(Constants.LOG_TAG, "mp:" + act.mediaPlayer.hashCode());
            String url = topTracks.getCurrentTrack().urlPreview; // your URL here
            act.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            act.mediaPlayer.setDataSource(url);
            act.mediaPlayer.prepareAsync(); // will call OnPreparedListener
        } catch(IllegalArgumentException iae) {
            iae.printStackTrace();
            UIUtil.toastIt(getActivity(), toast, getString(R.string.toast_could_not_play_file));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            UIUtil.toastIt(getActivity(), toast, getString(R.string.toast_could_not_play_file));
        }

        // called from prepareAsync
        act.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer player) {
                player.start();
            }

        });
    }

    private void stop() {
        if(act.mediaPlayer!=null && act.mediaPlayer.isPlaying()) {
            act.mediaPlayer.stop();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Constants.PARCEL_KEY_TOPTRACKS, topTracks);
        super.onSaveInstanceState(outState);
    }


}
