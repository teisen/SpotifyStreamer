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
import com.steelgirderdev.spotifystreamer.activity.TopTracksActivity;
import com.steelgirderdev.spotifystreamer.model.Artist;

import java.util.List;

/**
 * Created by teisentraeger on 5/31/2015.
 * Source: http://stackoverflow.com/questions/2265661/how-to-use-arrayadaptermyclass
 * Adapter which connect the listview with an Artist Record
 */
public class ArtistAdapter extends GenericArrayAdapter<Artist> {

    public ArtistAdapter(Context context, List<Artist> objects) {
        super(context, objects);
    }

    @Override public void drawRow(TextView textView, ImageView imageView, final Artist object) {
        textView.setText(object.artistname);
        Log.v(Constants.LOG_TAG, object.toString());
        LinearLayout linearLayout = (LinearLayout) textView.getParent();
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), TopTracksActivity.class);
                myIntent.putExtra(Constants.EXTRA_SPOTIFY_ID, object.spotifyId);
                myIntent.putExtra(Constants.EXTRA_ARTIST_NAME, object.artistname);
                mContext.startActivity(myIntent);

            }
        });

        // load the thumbnail if available, otherwise show a questionmark symbol
        if(object.urlThumbnail != null) {
            showImage(mContext, imageView, object.urlThumbnail);
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
