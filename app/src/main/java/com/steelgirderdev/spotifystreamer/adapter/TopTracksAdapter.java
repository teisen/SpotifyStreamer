package com.steelgirderdev.spotifystreamer.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.R;
import com.steelgirderdev.spotifystreamer.ui.PlayerActivity;
import com.steelgirderdev.spotifystreamer.model.TopTracks;
import com.steelgirderdev.spotifystreamer.model.Track;

import java.util.List;

/**
 * Created by teisentraeger on 5/31/2015.
 * Source: http://stackoverflow.com/questions/2265661/how-to-use-arrayadaptermyclass
 * Adapter which connect the listview with a Track
 */
public class TopTracksAdapter extends GenericArrayAdapter<TopTracks> {

    public TopTracksAdapter(Context context, List<TopTracks> objects) {
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
                Intent myIntent = new Intent(v.getContext(), PlayerActivity.class);
                myIntent.putExtra(Constants.EXTRA_TOP_TRACKS, topTracks);
                mContext.startActivity(myIntent);
            }
        });

        // load the thumbnail is available, otherwise show a questionmark symbol
        if(track.urlThumbnail != null) {
            showImage(mContext, imageView, track.urlThumbnail);
            imageView.setVisibility(View.VISIBLE);
        } else {
            //TODO: find a placeholder image to use imageView.setImageResource(android.R.drawable.ic_menu_help);
            imageView.setVisibility(View.INVISIBLE);
        }

    }

    private void showImage(Context context, ImageView imageView, String url) {
        Log.v(Constants.LOG_TAG, "Loading image:" + url);
        Picasso.with(context)
                .load(url)
                //TODO: find a error and placeholder image to use .error(android.R.drawable.ic_menu_close_clear_cancel)
                .resizeDimen(R.dimen.artists_albumWH, R.dimen.artists_albumWH)
                .centerInside()
                .into(imageView);
    }


}
