package com.steelgirderdev.spotifystreamer.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.R;

/**
 * Activity that shows the
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
