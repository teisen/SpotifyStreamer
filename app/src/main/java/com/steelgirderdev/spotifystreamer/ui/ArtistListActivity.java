package com.steelgirderdev.spotifystreamer.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.Visibility;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.R;
import com.steelgirderdev.spotifystreamer.model.Artist;
import com.steelgirderdev.spotifystreamer.model.TopTracks;
import com.steelgirderdev.spotifystreamer.model.TrackUiUpdate;

/**
 * An activity representing a list of Artist2s. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ArtistDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ArtistListFragment} and the item details
 * (if present) is a {@link ArtistDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ArtistListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ArtistListActivity extends AppCompatActivity
implements ArtistListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private MenuItem shareItem;
	private ShareActionProvider mShareActionProvider;
    private BroadcastReceiver receiver;

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(Constants.BROADCAST_INTENT_TRACKSTARTED)
        );
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // load default values fro preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (findViewById(R.id.fragment_detail_toptracks) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ArtistListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_artistsearch))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        // receiver that updates the current track progress on the UI from the service.
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TopTracks topTracks = (TopTracks) intent.getExtras().get(Constants.PARCEL_KEY_TOPTRACKS);
                Log.v(Constants.LOG_TAG, "onReceive update " + topTracks);
                // update the share intent with the new artist info
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, topTracks.getShareString());
                sendIntent.setType("text/plain");
                setShareIntent(sendIntent);
                // set the button to return to player
            }
        };

        return super.onCreateView(name, context, attrs);
    }

    /**
     * Callback method from {@link ArtistListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Artist art) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            //arguments.putString(ArtistDetailFragment.ARG_ITEM_ID, id);
            arguments.putParcelable(Constants.EXTRA_ARTIST, art);
            ArtistDetailFragment fragment = new ArtistDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_detail_toptracks, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ArtistDetailActivity.class);
            //detailIntent.putExtra(ArtistDetailFragment.ARG_ITEM_ID, id);
            detailIntent.putExtra(Constants.EXTRA_ARTIST, art);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
		// Inflate menu resource file.
		//TODO - this is probs not neccessary
		//getMenuInflater().inflate(R.menu.menu_item_share, menu);

		// Locate MenuItem with ShareActionProvider
		MenuItem item = menu.findItem(R.id.action_share);

		// Fetch and store ShareActionProvider
		mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
	
        return true;*/

        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Set up ShareActionProvider's default share intent
        shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(shareItem);
        //mShareActionProvider.setShareIntent(getDefaultIntent());

        shareItem.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }
	
	/** Call to update the share intent
	You may only need to set the share intent once during the creation of your menus, or you may want to set it and then update it as the UI changes. For example, when you view photos full screen in the Gallery app, the sharing intent changes as you flip between photos.
	*/
	public void setShareIntent(Intent shareIntent) {
		if (mShareActionProvider != null && shareItem != null) {
			mShareActionProvider.setShareIntent(shareIntent);
            shareItem.setVisible(true);
		}
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(this, SettingsActivity.class);
            this.startActivity(myIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
