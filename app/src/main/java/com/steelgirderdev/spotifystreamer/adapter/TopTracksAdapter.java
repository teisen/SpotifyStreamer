package com.steelgirderdev.spotifystreamer.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.R;
import com.steelgirderdev.spotifystreamer.model.TopTracks;
import com.steelgirderdev.spotifystreamer.model.Track;
import com.steelgirderdev.spotifystreamer.ui.ArtistListActivity;
import com.steelgirderdev.spotifystreamer.ui.PlayerActivity;
import com.steelgirderdev.spotifystreamer.ui.PlayerFragment;

import java.util.List;

/**
 * Created by teisentraeger on 5/31/2015.
 * Source: http://stackoverflow.com/questions/2265661/how-to-use-arrayadaptermyclass
 * Adapter which connect the listview with a Track
 */
public class TopTracksAdapter extends GenericArrayAdapter<TopTracks, FragmentActivity> {

    public TopTracksAdapter(FragmentActivity context, List<TopTracks> objects) {
        super(context, objects);
    }

    @Override public void drawRow(TextView textView, ImageView imageView, final TopTracks topTracks) {
        final Track track = topTracks.getCurrentTrack();
        textView.setText(track.trackname + "\n" + track.albumname);
        Log.v(Constants.LOG_TAG, track.toString());
        LinearLayout linearLayout = (LinearLayout) textView.getParent();
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean twoPane = false;
                // if the activity that has this fragment is the ArtistListActivity , then its twoPane
                if(mContext instanceof ArtistListActivity) {
                    twoPane = true;
                }
                if(twoPane) { //TODO - reactivate after test
                    //TODO load fragment in popup
                    Log.v(Constants.LOG_TAG,"TODO load fragment in popup with data ");
                    if(topTracks == null) {
                        Log.e(Constants.LOG_TAG,"topTracks is null");
                    } else {
                        Log.v(Constants.LOG_TAG,topTracks.artist.artistname);
                    }
                    // The device is using a large layout, so show the fragment as a dialog
                    PlayerFragment newFragment = new PlayerFragment();
                    newFragment.setContext(mContext);
                    FragmentManager fragmentManager = mContext.getSupportFragmentManager();
                    newFragment.show(fragmentManager, "dialog");
                    topTracks.command = Constants.ACTION_PLAY;
                    newFragment.setTopTracks(topTracks);
                    newFragment.executeCommand();

                } else {
                    Intent myIntent = new Intent(v.getContext(), PlayerActivity.class);
                    myIntent.putExtra(Constants.EXTRA_TOP_TRACKS, topTracks);
                    mContext.startActivity(myIntent);
                }
            }
        });

        // load the thumbnail is available, otherwise show a questionmark symbol
        if(track.urlThumbnail != null) {
            showImage(mContext, imageView, track.urlThumbnail);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setImageResource(R.drawable.ic_audiotrack_black_24dp);
        }

    }

    private void showImage(Context context, ImageView imageView, String url) {
        Log.v(Constants.LOG_TAG, "Loading image:" + url);
        Picasso.with(context)
                .load(url)
                //TODO: find a error and placeholder image to use .error(android.R.drawable.ic_menu_close_clear_cancel)
                .resizeDimen(R.dimen.artists_albumWH, R.dimen.artists_albumWH)
                .centerInside()
                .placeholder(R.drawable.ic_audiotrack_white_24dp)
                .into(imageView);
    }


}
