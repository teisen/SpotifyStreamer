package com.steelgirderdev.spotifystreamer.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * A list fragment representing a list of Artist2s. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ArtistDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ArtistListFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sArtistCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;


    private ArtistAdapter artistsAdapter;
    private Toast searchToast;
    private String searchString;
    private View rootView;
    private ListView listView;
    private EditText searchEditText;
    private Button btnTestFragment;
    private Button btnTest2;
    // ProgressDialog usage http://stackoverflow.com/questions/9814821/show-progressdialog-android
    ProgressDialog progress = null;
    ArrayList<Artist> artists;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(Artist art);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sArtistCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Artist art) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(Constants.LOG_TAG, this.getClass().getSimpleName() + " onCreateView called");
        //inflate fragment layout and find UI Elements
        rootView = inflater.inflate(R.layout.fragment_main, container);
        searchEditText = (EditText) rootView.findViewById(R.id.editText_artist);
        listView = (ListView) rootView.findViewById(android.R.id.list);

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

        artists = new ArrayList<>();

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

        //set the adapter ron the found listview
        listView.setAdapter(artistsAdapter);
        listView.setChoiceMode(R.attr.singleChoiceItemLayout);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(Constants.LOG_TAG, this.getClass().getSimpleName() + " onCreate called");

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
        private ArtistListFragment artistSearchFragment;

        public FetchArtistsTask(ArtistListFragment artistSearchFragment) {
            this.artistSearchFragment = artistSearchFragment;
        }

        @Override
        protected ArtistsPager doInBackground(String... params) {
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                Map<String, Object> queryMap = new HashMap<>();
                queryMap.put(Constants.SPOTIFY_API_ARTIST_SEARCH_LIMIT_PARAMNAME, Constants.SPOTIFY_API_ARTIST_SEARCH_LIMIT);
                ArtistsPager pager = spotify.searchArtists(params[0], queryMap);
                Log.d(Constants.LOG_TAG, "Returned " + pager.artists.total + " artists for searchstring " + params[0]);
                return pager;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
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
                } else {
                    UIUtil.toastIt(getActivity(), searchToast, artistSearchFragment.getString(R.string.toast_search_no_results_found));
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                artistSearchFragment.hideProgressDialog();
            }

        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sArtistCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(artists.get(position));
		

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.v(Constants.LOG_TAG, this.getClass().getSimpleName() + " onSaveInstanceState called");
        // Serialize and persist the activated item position and the other data.
        outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        outState.putString(Constants.STATE_ARTIST_NAME, searchString);
        outState.putParcelableArrayList(Constants.PARCEL_KEY_ARTISTS, artists);
        super.onSaveInstanceState(outState);
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
