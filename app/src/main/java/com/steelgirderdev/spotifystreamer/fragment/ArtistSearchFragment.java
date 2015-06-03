package com.steelgirderdev.spotifystreamer.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.R;
import com.steelgirderdev.spotifystreamer.adapter.ArtistAdapter;
import com.steelgirderdev.spotifystreamer.model.Artist;
import com.steelgirderdev.spotifystreamer.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * The Main Activity Fragment that shows the search for the artist and the results
 */
public class ArtistSearchFragment extends Fragment {

    private ArtistAdapter artistsAdapter;
    private Toast searchToast;
    private String searchString;
    private View rootView;
    private ListView listView;
    private EditText searchEditText;
    // ProgressDialog usage http://stackoverflow.com/questions/9814821/show-progressdialog-android
    ProgressDialog progress = null;
    ArrayList<Artist> artists;

    public ArtistSearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //inflate fragment layout and find UI Elements
        rootView = inflater.inflate(R.layout.fragment_main, container);
        searchEditText = (EditText) rootView.findViewById(R.id.editText_artist);
        listView = (ListView) rootView.findViewById(R.id.listview_artists);

        // set listeners
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchString = searchEditText.getText().toString();
                    searchForArtists(searchEditText);
                    return true;
                }
                return false;
            }
        });

        artists = new ArrayList<Artist>();

        //create the arrayAdapter
        artistsAdapter = new ArtistAdapter(
                // the current context
                getActivity(),
                // ID of the list item layout
                //TODO add into generic adapter def R.layout.list_item_artist,
                // ID of the textview to populate
                //TODO add into generic adapter def R.id.list_item_artist_textview,
                // data
                artists
        );

        // load the artists if resumed
        if(savedInstanceState == null || !savedInstanceState.containsKey(Constants.PARCEL_KEY_ARTISTS)) {
            // do nothing
        } else {
            // Restore value of members from saved state
            searchString = savedInstanceState.getString(Constants.STATE_ARTIST_NAME);
            searchEditText.setText(searchString);

            artists = savedInstanceState.getParcelableArrayList(Constants.PARCEL_KEY_ARTISTS);
            artistsAdapter.clear();
            for (Artist art : artists) {
                artistsAdapter.add(art);
            }
        }

        //set the adapte ron the found listview
        listView.setAdapter(artistsAdapter);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {

        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(Constants.STATE_ARTIST_NAME, searchString);
        outState.putParcelableArrayList(Constants.PARCEL_KEY_ARTISTS, artists);
        super.onSaveInstanceState(outState);
    }

    private void searchForArtists(EditText searchEditText) {
        if(searchString.isEmpty()) {
            UIUtil.toastIt(getActivity(), searchToast, getString(R.string.error_search_cannot_be_empty));
            return;
        }
        showProgressDialog(searchString);
        FetchArtistsTask task = new FetchArtistsTask(this);
        task.execute(searchString);
        // Hide the keyboard after search
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    public void showProgressDialog(String searchString) {
        progress = ProgressDialog.show(getActivity(), getString(R.string.progress_dialog_searching), getString(R.string.progress_dialog_searching_for, searchString), true);
    }
    public void hideProgressDialog() {
        if(progress != null){
            progress.dismiss();
            progress = null;
        }
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, ArtistsPager> {
        private ArtistSearchFragment artistSearchFragment;

        public FetchArtistsTask(ArtistSearchFragment artistSearchFragment) {
            this.artistSearchFragment = artistSearchFragment;
        }

        @Override
        protected ArtistsPager doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            Map<String, Object> queryMap = new HashMap<String, Object>();
            queryMap.put(Constants.SPOTIFY_API_ARTIST_SEARCH_LIMIT_PARAMNAME, Constants.SPOTIFY_API_ARTIST_SEARCH_LIMIT);
            ArtistsPager pager = spotify.searchArtists(params[0], queryMap);
            Log.d(Constants.LOG_TAG, "Returned " + pager.artists.total + " artists for searchstring " + params[0]);

            return pager;
        }

        @Override
        protected void onPostExecute(ArtistsPager artistpager) {
            super.onPostExecute(artistpager);
            try {
                if (artists != null) {
                    //The API only returns 50 artists at a time, advise user
                    if(artistpager.artists.total > artistpager.artists.limit) {
                        UIUtil.toastIt(getActivity(), searchToast, getString(R.string.toast_search_limit_reached));
                    }
                    artistSearchFragment.artistsAdapter.clear();
                    for (kaaes.spotify.webapi.android.models.Artist art : artistpager.artists.items) {
                        artistSearchFragment.artistsAdapter.add(new Artist(art));
                    }
                    if (artists.isEmpty()) {
                        UIUtil.toastIt(getActivity(), searchToast, artistSearchFragment.getString(R.string.toast_search_no_results_found));
                    }
                    artistsAdapter.notifyDataSetChanged();
                    listView.setSelection(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                artistSearchFragment.hideProgressDialog();
            }

        }
    }



}
