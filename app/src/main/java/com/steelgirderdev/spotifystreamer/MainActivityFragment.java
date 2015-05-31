package com.steelgirderdev.spotifystreamer;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArtistAdapter mArtistsAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate fragment layout and find UI Elements
        final View rootView = inflater.inflate(R.layout.fragment_main, container);
        final EditText searchEditText = (EditText) rootView.findViewById(R.id.editText_artist);
        final Button button = (Button) rootView.findViewById(R.id.button_search_artists);
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_artists);

        // set listeners

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String searchString = searchEditText.getText().toString();
                Toast.makeText(getActivity(), "Searching for "+searchString, Toast.LENGTH_SHORT).show();
                FetchArtistsTask task = new FetchArtistsTask();
                task.execute(searchString);
            }
        });

        List<Artist> artists = new ArrayList<Artist>();

        //create the arrayAdapter
        mArtistsAdapter = new ArtistAdapter(
                // the current context
                getActivity(),
                // ID of the list item layout
                //TODO add into generic adapter def R.layout.list_item_artist,
                // ID of the textview to populate
                //TODO add into generic adapter def R.id.list_item_artist_textview,
                // data
                artists
        );


        //set the adapte ron the found listview
        listView.setAdapter(mArtistsAdapter);


        return rootView;
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, List<Artist>> {
        @Override
        protected List<Artist> doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            ArtistsPager pager = spotify.searchArtists(params[0]);
            Log.d(Constants.LOG_TAG, "Returned " + pager.artists.total + " artists.");
            for(Artist art : pager.artists.items) {
                Log.d(Constants.LOG_TAG, art.name);
            }
            return pager.artists.items;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            super.onPostExecute(artists);
            if(artists!=null) {
                mArtistsAdapter.clear();
                for(Artist art : artists) {
                    mArtistsAdapter.add(art);
                }
            }
        }
    }
}
