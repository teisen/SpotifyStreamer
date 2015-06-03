package com.steelgirderdev.spotifystreamer.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.R;
import com.steelgirderdev.spotifystreamer.adapter.TrackAdapter;
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

    private TrackAdapter trackAdapter;
    private Toast toast;
    // ProgressDialog usage http://stackoverflow.com/questions/9814821/show-progressdialog-android
    ProgressDialog progress = null;
    private ArrayList<Track> tracks;

    public TopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //inflate fragment layout and find UI Elements
        final View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        final TextView artistNameTextView = (TextView) rootView.findViewById(R.id.textView_toptracks_artist);
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_toptracks);

        // read intent extras
        String name = getActivity().getIntent().getExtras().getString(Constants.EXTRA_ARTIST_NAME);
        String spotifyId = getActivity().getIntent().getExtras().getString(Constants.EXTRA_SPOTIFY_ID);
        Log.v(Constants.LOG_TAG, "name:" + name + " spotifyId:" + spotifyId);

        artistNameTextView.setText(name);

        tracks = new ArrayList<Track>();

        //create the arrayAdapter
        trackAdapter = new TrackAdapter(
                // the current context
                getActivity(),
                // ID of the list item layout
                //TODO add into generic adapter def R.layout.list_item_artist,
                // ID of the textview to populate
                //TODO add into generic adapter def R.id.list_item_artist_textview,
                // data
                tracks
        );

        // load the tracks if resumed
        if(savedInstanceState == null || !savedInstanceState.containsKey(Constants.PARCEL_KEY_TOPTRACKS)) {
            loadTrackList(spotifyId, name);
        } else {
            tracks = savedInstanceState.getParcelableArrayList(Constants.PARCEL_KEY_TOPTRACKS);
            trackAdapter.clear();
            for (Track track : tracks) {
                trackAdapter.add(track);
            }
        }

        //set the adapter on the found listview
        listView.setAdapter(trackAdapter);

        actionBarSetup(name);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Constants.PARCEL_KEY_TOPTRACKS, tracks);
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
            ab.setDisplayHomeAsUpEnabled(true); //http://developer.android.com/training/implementing-navigation/ancestral.html
            ab.setSubtitle(subtitle);
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
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            Map<String, Object> queryMap = new HashMap<String, Object>();
            queryMap.put("country", "US");
            Tracks tracks = spotify.getArtistTopTrack(params[0], queryMap);
            Log.d(Constants.LOG_TAG, "Returned " + tracks.tracks.size() + " tracks for searchstring " + params[0]);
            return tracks.tracks;
        }

        @Override
        protected void onPostExecute(List<kaaes.spotify.webapi.android.models.Track> tracks) {
            super.onPostExecute(tracks);
            try {
                if (tracks != null) {
                    topTracksFragment.trackAdapter.clear();
                    for (kaaes.spotify.webapi.android.models.Track track : tracks) {
                        topTracksFragment.trackAdapter.add(new com.steelgirderdev.spotifystreamer.model.Track(track));
                    }
                    if (tracks.isEmpty()) {
                        UIUtil.toastIt(getActivity(), toast, topTracksFragment.getString(R.string.toast_search_no_results_found));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                topTracksFragment.hideProgressDialog();
            }

        }
    }

}
