package com.steelgirderdev.spotifystreamer.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.steelgirderdev.spotifystreamer.R;

/**
 * Created by teisentraeger on 6/2/2015.
 * http://developer.android.com/guide/topics/ui/settings.html#Fragment
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
