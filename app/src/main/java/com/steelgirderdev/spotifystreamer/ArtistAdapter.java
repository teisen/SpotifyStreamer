package com.steelgirderdev.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by teisentraeger on 5/31/2015.
 * Found solution at http://stackoverflow.com/questions/2265661/how-to-use-arrayadaptermyclass
 */
public class ArtistAdapter extends GenericArrayAdapter<Artist> {

    public ArtistAdapter(Context context, List<Artist> objects) {
        super(context, objects);
    }

    @Override public void drawRow(TextView textView, ImageView imageView, Artist object) {
        textView.setText(object.name);
        if(object.images.size()>0) {
            //textViewImage.setText(object.images.get(0).url);
            Picasso.with(mContext).load(object.images.get(0).url).into(imageView);
        } else {
            //textViewImage.setText("No image");
        }
    }


}
