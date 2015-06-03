package com.steelgirderdev.spotifystreamer.activity;

import android.app.Activity;
import android.os.Bundle;

import com.steelgirderdev.spotifystreamer.fragment.SettingsFragment;

/**
 * Created by teisentraeger on 6/2/2015.
 * http://developer.android.com/guide/topics/ui/settings.html#Fragment
 */
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
