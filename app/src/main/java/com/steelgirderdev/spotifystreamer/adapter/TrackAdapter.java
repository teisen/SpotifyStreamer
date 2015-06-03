package com.steelgirderdev.spotifystreamer.adapter;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.R;
import com.steelgirderdev.spotifystreamer.model.Track;

import java.util.List;

/**
 * Created by teisentraeger on 5/31/2015.
 * Source: http://stackoverflow.com/questions/2265661/how-to-use-arrayadaptermyclass
 * Adapter which connect the listview with a Track
 */
public class TrackAdapter extends GenericArrayAdapter<Track> {

    public TrackAdapter(Context context, List<Track> objects) {
        super(context, objects);
    }

    @Override public void drawRow(TextView textView, ImageView imageView, final Track track) {
        textView.setText(track.trackname + "\n" + track.albumname);
        Log.v(Constants.LOG_TAG, track.toString());

        // load the thumbnail is available, otherwise show a questionmark symbol
        if(track.urlThumbnail != null) {
            showImage(mContext, imageView, track.urlThumbnail);
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_help);
        }

    }

    private void showImage(Context context, ImageView imageView, String url) {
        Log.v(Constants.LOG_TAG, "Loading image:" + url);
        Picasso.with(context)
                .load(url)
                .placeholder(android.R.drawable.ic_menu_upload)
                .error(android.R.drawable.ic_menu_close_clear_cancel)
                .resizeDimen(R.dimen.artists_albumWH, R.dimen.artists_albumWH)
                .centerInside()
                .into(imageView);
    }


}
