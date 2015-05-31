package com.steelgirderdev.spotifystreamer;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayAdapter<String> mArtistsAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate fragment layout
        View rootView = inflater.inflate(R.layout.fragment_main, container);

        // create mock data
        //TODO remove mock data
        String[] artistsArray = {"Coldplay", "Coldplay & Lele", "Coldplay & Rihanna", "Various Artists - Coldplay Tribute",
                                    "Coldplay Test 1", "Coldplay Test 2", "Coldplay Test 3", "Coldplay Test 4", "Coldplay Test 5"};
        List<String> artists = new ArrayList<String>(Arrays.asList(artistsArray));

        //create the arrayAdapter
        mArtistsAdapter = new ArrayAdapter<String>(
                // the current context
                getActivity(),
                // ID of the list item layout
                R.layout.list_item_artist,
                // ID of the textview to populate
                R.id.list_item_artist_textview,
                // data
                artists
        );

        //find the listview by id
        ListView listView = (ListView) rootView.findViewById(R.id.listview_artists);

        //set the adapte ron the found listview
        listView.setAdapter(mArtistsAdapter);


        return rootView;
    }
}
