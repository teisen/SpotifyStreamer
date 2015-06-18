package com.steelgirderdev.spotifystreamer.ui;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

/**
 * Activity that shows the Player - only used by Handsets
 */
public class PlayerActivity extends ActionBarActivity {

    private MediaPlayer mediaPlayer;
    private PlayerFragment mMyFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_player);
        if (savedInstanceState == null) {
            mMyFragment = new PlayerFragment();
            if (mMyFragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .add(android.R.id.content, mMyFragment)
                        .commit();
            }
        }

    }

}
