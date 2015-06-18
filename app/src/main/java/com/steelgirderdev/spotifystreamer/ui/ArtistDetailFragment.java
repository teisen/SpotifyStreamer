package com.steelgirderdev.spotifystreamer.ui;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import com.steelgirderdev.spotifystreamer.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A fragment representing a single Artist detail screen.
 * This fragment is either contained in a {@link ArtistListActivity}
 * in two-pane mode (on tablets) or a {@link ArtistDetailActivity}
 * on handsets.
 */
public class ArtistDetailFragment extends Fragment {

    /**
     * The Artist this Fragment Presents
     */
    private Artist mArtist;
    private TopTracksAdapter topTracksAdapter;
    private Toast toast;
    // ProgressDialog usage http://stackoverflow.com/questions/9814821/show-progressdialog-android
    ProgressDialog progress = null;
    ArrayList<TopTracks> topTracks;
    private View rootView;
    private ListView listView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Constants.LOG_TAG, this.getClass().getSimpleName() + " onCreate called");

        if (getArguments() != null && getArguments().containsKey(Constants.EXTRA_ARTIST)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mArtist = getArguments().getParcelable(Constants.EXTRA_ARTIST);
        } else if(getActivity().getIntent().hasExtra(Constants.EXTRA_ARTIST)) {
            mArtist = (Artist) getActivity().getIntent().getExtras().get(Constants.EXTRA_ARTIST);
        } else {
            Log.e(Constants.LOG_TAG, "No extras or arguments of type Constants.EXTRA_ARTIST passed.");
        }

        topTracks = new ArrayList<>();

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
        if (savedInstanceState == null || !savedInstanceState.containsKey(Constants.PARCEL_KEY_TOPTRACKS_LIST)) {
            if(mArtist != null) {
                loadTrackList(mArtist);
            }
        } else {
            topTracks = savedInstanceState.getParcelableArrayList(Constants.PARCEL_KEY_TOPTRACKS_LIST);
            topTracksAdapter.clear();
            for (TopTracks track : topTracks) {
                topTracksAdapter.add(track);
            }
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(Constants.LOG_TAG, this.getClass().getSimpleName() + " onCreateView called");
        rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        listView = (ListView) rootView.findViewById(R.id.listview_toptracks);

        //set the adapter on the found listview
        listView.setAdapter(topTracksAdapter);

        // Show the dummy content as text in a TextView.
        if (mArtist != null) {
            //loadTrackList(mArtist);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(Constants.LOG_TAG, this.getClass().getSimpleName() + " onSaveInstanceState called");
        outState.putParcelableArrayList(Constants.PARCEL_KEY_TOPTRACKS_LIST, topTracks);
        super.onSaveInstanceState(outState);
    }

    /**
     * gets called internally as well as from the artist search on change of artist
     * @param pArtist
     */
    public void loadTrackList(Artist pArtist) {
        Log.d(Constants.LOG_TAG, this.getClass().getSimpleName() + " loadTrackList called");
        mArtist = pArtist;
        showProgressDialog(mArtist.artistname);
        FetchTopTracksTask task = new FetchTopTracksTask(this);
        task.execute(mArtist.spotifyId);
        actionBarSetup(mArtist);
    }

    public void showProgressDialog(String searchString) {
        Log.d(Constants.LOG_TAG, this.getClass().getSimpleName() + " showProgressDialog called");
        progress = ProgressDialog.show(getActivity(), getString(R.string.progress_dialog_searching), getString(R.string.progress_dialog_loading_top_tracks_of, searchString), true);
    }
    public void hideProgressDialog() {
        Log.d(Constants.LOG_TAG, this.getClass().getSimpleName() + " hideProgressDialog called");
        if(progress != null){
            progress.dismiss();
            progress = null;
        }
    }

    /**
     * Sets the Action Bar title
     */
    private void actionBarSetup(Artist art) {
        try {
            ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if(ab != null) {
                ab.setDisplayHomeAsUpEnabled(true); //http://developer.android.com/training/implementing-navigation/ancestral.html
                if(art != null) {
                    ab.setTitle(art.artistname);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class FetchTopTracksTask extends AsyncTask<String, Void, List<Track>> {
        private ArtistDetailFragment fragment;

        public FetchTopTracksTask(ArtistDetailFragment topTracksFragment) {
            Log.d(Constants.LOG_TAG, this.getClass().getSimpleName() + " FetchTopTracksTask called");
            this.fragment = topTracksFragment;
        }

        @Override
        protected List<kaaes.spotify.webapi.android.models.Track> doInBackground(String... params) {
            Log.d(Constants.LOG_TAG, this.getClass().getSimpleName() + " doInBackground called");
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
            Log.d(Constants.LOG_TAG, this.getClass().getSimpleName() + " onPostExecute called. adapter= "+fragment.topTracksAdapter.hashCode());
            super.onPostExecute(tracks);
            try {
                if (tracks != null) {
                    fragment.topTracksAdapter.clear();

                    // create alltracks object
                    ArrayList<com.steelgirderdev.spotifystreamer.model.Track> allTracks = new ArrayList<>(tracks.size());
                    for(int i = 0; i < tracks.size(); i++) {
                        allTracks.add(new com.steelgirderdev.spotifystreamer.model.Track(tracks.get(i)));
                    }
                    // set all alltracks objects to the list
                    for(int i = 0; i < allTracks.size(); i++) {
                        TopTracks topTrack = new TopTracks(mArtist, allTracks, i, Constants.ACTION_PLAY);
                        fragment.topTracksAdapter.add(topTrack);
                    }

                    if (tracks.isEmpty()) {
                        UIUtil.toastIt(getActivity(), toast, fragment.getString(R.string.toast_search_no_results_found));
                    }
                    topTracksAdapter.notifyDataSetChanged();
                    listView.setSelection(0);

                } else {
                    UIUtil.toastIt(getActivity(), toast, "No tracks found or error. Did you specify the country code correctly?");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                fragment.hideProgressDialog();
            }

        }
    }
}
