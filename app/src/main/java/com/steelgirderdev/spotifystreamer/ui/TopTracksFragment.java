package com.steelgirderdev.spotifystreamer.ui;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.R;
import com.steelgirderdev.spotifystreamer.adapter.TopTracksAdapter;
import com.steelgirderdev.spotifystreamer.model.Artist;
import com.steelgirderdev.spotifystreamer.model.TopTracks;
import com.steelgirderdev.spotifystreamer.model.Track;
import com.steelgirderdev.spotifystreamer.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * Fragment that shows the Top Tracks of a given Artist
 */
public class TopTracksFragment extends Fragment {

    private TopTracksAdapter topTracksAdapter;
    private Toast toast;
    // ProgressDialog usage http://stackoverflow.com/questions/9814821/show-progressdialog-android
    ProgressDialog progress = null;
    private ArrayList<TopTracks> topTracks;
    private View rootView;
    private ListView listView;
    private Artist artist;

    public TopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //inflate fragment layout and find UI Elements
        rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        listView = (ListView) rootView.findViewById(R.id.listview_toptracks);

        // read intent extras
        artist = (Artist) getActivity().getIntent().getExtras().get(Constants.EXTRA_ARTIST);
        Log.v(Constants.LOG_TAG, "name:" + artist.artistname + " spotifyId:" + artist.spotifyId);

        topTracks = new ArrayList<>();

        //create the arrayAdapter
        topTracksAdapter = new TopTracksAdapter(
                // the current context
                getActivity(),
                // ID of the list item layout
                //TODO add into generic adapter def R.layout.list_item_artist,
                // ID of the textview to populate
                //TODO add into generic adapter def R.id.list_item_artist_textview,
                // data
                topTracks
        );

        // load the tracks if resumed
        if(savedInstanceState == null || !savedInstanceState.containsKey(Constants.PARCEL_KEY_TOPTRACKS_LIST)) {
            loadTrackList(artist.spotifyId, artist.artistname);
        } else {
            topTracks = savedInstanceState.getParcelableArrayList(Constants.PARCEL_KEY_TOPTRACKS_LIST);
            topTracksAdapter.clear();
            for (TopTracks track : topTracks) {
                topTracksAdapter.add(track);
            }
        }

        //set the adapter on the found listview
        listView.setAdapter(topTracksAdapter);

        actionBarSetup(artist.artistname);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Constants.PARCEL_KEY_TOPTRACKS_LIST, topTracks);
        super.onSaveInstanceState(outState);
    }

    private void loadTrackList(String spotifyId, String artistName) {
        showProgressDialog(artistName);
        FetchTopTracksTask task = new FetchTopTracksTask(this);
        task.execute(spotifyId);
    }

    public void showProgressDialog(String searchString) {
        progress = ProgressDialog.show(getActivity(), getString(R.string.progress_dialog_searching), getString(R.string.progress_dialog_loading_top_tracks_of, searchString), true);
    }
    public void hideProgressDialog() {
        if(progress != null){
            progress.dismiss();
            progress = null;
        }
    }

    /**
     * Sets the Action Bar title
     */
    private void actionBarSetup(String subtitle) {
        try {
            ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if(ab != null) {
                ab.setDisplayHomeAsUpEnabled(true); //http://developer.android.com/training/implementing-navigation/ancestral.html
                ab.setSubtitle(subtitle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class FetchTopTracksTask extends AsyncTask<String, Void, List<kaaes.spotify.webapi.android.models.Track>> {
        private TopTracksFragment topTracksFragment;

        public FetchTopTracksTask(TopTracksFragment topTracksFragment) {
            this.topTracksFragment = topTracksFragment;
        }

        @Override
        protected List<kaaes.spotify.webapi.android.models.Track> doInBackground(String... params) {
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                Map<String, Object> queryMap = new HashMap<>();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String countryname = sharedPref.getString(Constants.PREFERENCE_KEY_COUNTRY, "");
                queryMap.put(Constants.SPOTIFY_API_TOPTRACKS_SEARCH_COUNTRY_PARAMNAME, countryname);
                Tracks tracks = spotify.getArtistTopTrack(params[0], queryMap);
                Log.d(Constants.LOG_TAG, "Returned " + tracks.tracks.size() + " tracks for searchstring " + params[0]);
                return tracks.tracks;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<kaaes.spotify.webapi.android.models.Track> tracks) {
            super.onPostExecute(tracks);
            try {
                if (tracks != null) {
                    topTracksFragment.topTracksAdapter.clear();

                    // create alltracks object
                    ArrayList<Track> allTracks = new ArrayList<>(tracks.size());
                    for(int i = 0; i < tracks.size(); i++) {
                        allTracks.add(new Track(tracks.get(i)));
                    }
                    // set all alltracks objects to the list
                    for(int i = 0; i < allTracks.size(); i++) {
                        TopTracks topTrack = new TopTracks(artist, allTracks, i);
                        topTracksFragment.topTracksAdapter.add(topTrack);
                    }

                    if (tracks.isEmpty()) {
                        UIUtil.toastIt(getActivity(), toast, topTracksFragment.getString(R.string.toast_search_no_results_found));
                    }
                    topTracksAdapter.notifyDataSetChanged();
                    listView.setSelection(0);
                } else {
                    UIUtil.toastIt(getActivity(), toast, "No tracks found or error. Did you specify the country code correctly?");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                topTracksFragment.hideProgressDialog();
            }

        }
    }

}
