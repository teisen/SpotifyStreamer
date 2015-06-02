package com.steelgirderdev.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.steelgirderdev.spotifystreamer.filters.ImageSizeFilter;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by teisentraeger on 5/31/2015.
 * Source: http://stackoverflow.com/questions/2265661/how-to-use-arrayadaptermyclass
 */
public class TrackAdapter extends GenericArrayAdapter<Track> {

    public TrackAdapter(Context context, List<Track> objects) {
        super(context, objects);
    }

    @Override public void drawRow(TextView textView, ImageView imageView, final Track object) {
        textView.setText(object.name + "\n" + object.album.name);
        Log.v(Constants.LOG_TAG, "name:" + object.name + " album:" + object.album.name + " preview_url:" + object.preview_url + " #urls=" + object.album.images.size());
        LinearLayout linearLayout = (LinearLayout) textView.getParent();

        if(object.album.images.size()>0) {
            /*
            Album art thumbnail (large (640px for Now Playing screen) and small (200px for list items)).
            If the image size does not exist in the API response, you are free to choose whatever size is available.)
             */
            //find url with size 640 for the Now Playing screen
            List<Image> imagesHighres = ImageSizeFilter.filterLargerThan(object.album.images, 639);
            Log.v(Constants.LOG_TAG, "found "+imagesHighres.size()+" album highres image/s");

            //find url with size 300 for the list item
            List<Image> imagesThumbnails = ImageSizeFilter.filterSmallerThan(object.album.images, 301);
            Log.v(Constants.LOG_TAG, "found " + imagesThumbnails.size() + " album thumbnail image/s");

            // if we have a 300 or smaller image use it for the list, otherwise simple use the first image
            if(imagesThumbnails.isEmpty()) {
                showImage(mContext, imageView, object.album.images.get(0));
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
