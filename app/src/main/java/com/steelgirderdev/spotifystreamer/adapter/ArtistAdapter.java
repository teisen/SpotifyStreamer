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
import com.steelgirderdev.spotifystreamer.filter.ImageSizeFilter;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

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
        textView.setText(object.name);
        Log.v(Constants.LOG_TAG, "name:" + object.name + " pop:" + object.popularity + " #urls=" + object.images.size());
        LinearLayout linearLayout = (LinearLayout) textView.getParent();
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), TopTracksActivity.class);
                myIntent.putExtra(Constants.EXTRA_SPOTIFY_ID, object.id);
                myIntent.putExtra(Constants.EXTRA_ARTIST_NAME, object.name);
                mContext.startActivity(myIntent);

            }
        });

        if(object.images.size()>0) {

            //find url with size 300 for the list item
            List<Image> imagesThumbnails = ImageSizeFilter.filterSmallerThan(object.images, 301);
            Log.v(Constants.LOG_TAG, "found " + imagesThumbnails.size() + " artist thumbnail image/s");

            // if we have a 300 or smaller image use it for the list, otherwise simple use the first image
            if(imagesThumbnails.isEmpty()) {
                showImage(mContext, imageView, object.images.get(0));
            } else {
                showImage(mContext, imageView, imagesThumbnails.get(0));
            }

        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_help);
        }
    }

    private void showImage(Context context, ImageView imageView, Image img) {
        Log.v(Constants.LOG_TAG, "url:" + img.url + " h:" + img.height + " w:" + img.width);
        Picasso.with(context)
                .load(img.url)
                .placeholder(android.R.drawable.ic_menu_upload)
                .error(android.R.drawable.ic_menu_close_clear_cancel)
                        //.resize(R.dimen.artists_albumWH, R.dimen.artists_albumWH)
                .resizeDimen(R.dimen.artists_albumWH, R.dimen.artists_albumWH)
                .centerInside()
                .into(imageView);
    }

}
