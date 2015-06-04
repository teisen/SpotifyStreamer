package com.steelgirderdev.spotifystreamer.activity;

import android.content.Intent;
import android.media.MediaPlayer;
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

    public static MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            if(mediaPlayer!=null && mediaPlayer.isPlaying()) {
                Log.v(Constants.LOG_TAG, "mp:" + mediaPlayer.hashCode());
                //mediaPlayer.stop();
            }
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(this, SettingsActivity.class);
            this.startActivity(myIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
