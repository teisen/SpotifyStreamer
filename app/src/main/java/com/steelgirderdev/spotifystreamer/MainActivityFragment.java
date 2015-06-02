package com.steelgirderdev.spotifystreamer;

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

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * The Main Activity Fragment that shows the search for the artist and the results
 */
public class MainActivityFragment extends Fragment {

    private ArtistAdapter mArtistsAdapter;
    private Toast searchToast;
    private String searchString;
    // ProgressDialog usage http://stackoverflow.com/questions/9814821/show-progressdialog-android
    ProgressDialog progress = null;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //inflate fragment layout and find UI Elements
        final View rootView = inflater.inflate(R.layout.fragment_main, container);
        final EditText searchEditText = (EditText) rootView.findViewById(R.id.editText_artist);
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_artists);

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

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            searchString = savedInstanceState.getString(Constants.STATE_ARTIST_NAME);
            searchEditText.setText(searchString);
            searchForArtists(searchEditText);
        }

        return rootView;
    }

    private void searchForArtists(EditText searchEditText) {
        if(searchString.isEmpty()) {
            toastIt(getString(R.string.error_search_cannot_be_empty));
            return;
        }
        showProgressDialog(searchString);
        FetchArtistsTask task = new FetchArtistsTask();
        task.execute(searchString);
        // Hide the keyboard after search
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(Constants.STATE_ARTIST_NAME, searchString);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void showProgressDialog(String searchString) {
        progress = ProgressDialog.show(getActivity(), getString(R.string.progress_dialog_searching), getString(R.string.progress_dialog_searching_for, searchString), true);
    }
    private void hideProgressDialog() {
        if(progress != null){
            progress.dismiss();
            progress = null;
        }
    }

    private void toastIt(CharSequence msg) {
        //Stop any previous toasts
        if(searchToast !=null){
            searchToast.cancel();
        }

        //Make and display new toast
        searchToast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
        searchToast.show();
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, List<Artist>> {
        @Override
        protected List<Artist> doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            ArtistsPager pager = spotify.searchArtists(params[0]);
            Log.d(Constants.LOG_TAG, "Returned " + pager.artists.total + " artists for searchstring " + params[0]);
            return pager.artists.items;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            super.onPostExecute(artists);
            try {
                if (artists != null) {
                    mArtistsAdapter.clear();
                    for (Artist art : artists) {
                        mArtistsAdapter.add(art);
                    }
                    if (artists.isEmpty()) {
                        toastIt(getString(R.string.toast_search_no_results_found));
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            } finally {
                hideProgressDialog();
            }

        }
    }
}
