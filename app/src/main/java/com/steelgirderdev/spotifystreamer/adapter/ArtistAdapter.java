package com.steelgirderdev.spotifystreamer.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.R;
import com.steelgirderdev.spotifystreamer.ui.ArtistListActivity;
import com.steelgirderdev.spotifystreamer.model.Artist;
import com.steelgirderdev.spotifystreamer.ui.ArtistDetailActivity;
import com.steelgirderdev.spotifystreamer.ui.ArtistDetailFragment;

import java.util.List;

/**
 * Created by teisentraeger on 5/31/2015.
 * Source: http://stackoverflow.com/questions/2265661/how-to-use-arrayadaptermyclass
 * Adapter which connect the listview with an Artist Record
 */
public class ArtistAdapter extends GenericArrayAdapter<Artist, FragmentActivity> {

    public ArtistAdapter(FragmentActivity context, List<Artist> objects) {
        super(context, objects);
    }

    @Override public void drawRow(final TextView textView, ImageView imageView, final Artist artist) {
        textView.setText(artist.artistname);
        Log.v(Constants.LOG_TAG, artist.toString());
        LinearLayout linearLayout = (LinearLayout) textView.getParent();
        /*
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean twoPane = false;
                if(mContext instanceof ArtistListActivity) {
                    if(((ArtistListActivity) mContext).findViewById(R.id.fragment_detail_toptracks) != null) {
                        twoPane = true;
                    }
                    ListView lv = ((ListView)textView.getParent().getParent());
                    lv.dispatchSetActivated(false);

                    ((LinearLayout)textView.getParent()).setActivated(true);
                }
                if(twoPane) {
                    ArtistDetailFragment ttf = (ArtistDetailFragment) mContext.getSupportFragmentManager().findFragmentById(R.id.fragment_detail_toptracks);
                    if (ttf != null) {
                        //TODO ttf.loadTrackList(artist);
                    }
                } else {
                    Intent myIntent = new Intent(v.getContext(), ArtistDetailActivity.class);
                    myIntent.putExtra(Constants.EXTRA_ARTIST, artist);
                    mContext.startActivity(myIntent);
                }

            }
        });
        */

        // load the thumbnail if available, otherwise show a questionmark symbol
        if(artist.urlThumbnail != null) {
            showImage(mContext, imageView, artist.urlThumbnail);
            imageView.setVisibility(View.VISIBLE);
        } else {
            //TODO: find a placeholder image to use imageView.setImageResource();
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    private void showImage(Context context, ImageView imageView, String url) {
        Log.v(Constants.LOG_TAG, "Loading image:" + url);
        Picasso.with(context)
                .load(url)
                //TODO: find a error and placeholder image to use .error(android.R.drawable.ic_menu_close_clear_cancel) .placeholder()
                .resizeDimen(R.dimen.artists_albumWH, R.dimen.artists_albumWH)
                .centerInside()
                .into(imageView);
    }

}
