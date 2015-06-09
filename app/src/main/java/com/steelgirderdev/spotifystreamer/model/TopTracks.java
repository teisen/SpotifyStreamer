package com.steelgirderdev.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by teisentraeger on 6/3/2015.
 */
public class TopTracks implements Parcelable {
    public Artist artist;
    public ArrayList<Track> tracks;
    public int playerpos;

    public TopTracks(final Artist artist, final ArrayList<Track> tracks, final int playerpos) {
        this.artist = artist;
        this.tracks = tracks;
        this.playerpos = playerpos;
    }

    public Track getCurrentTrack() {
        return tracks.get(playerpos);
    }

    // selects the next track and returns it. loop support
    public Track getNextTrack() {
        if(tracks.size() == playerpos+1) {
            // if we are at the end of the list, loop to first
            playerpos = 0;
        } else {
            playerpos++;
        }
        return getCurrentTrack();
    }

    // selects the next track and returns it. loop support
    public Track getPreviousTrack() {
        if(playerpos==0) {
            // if we are at the beginning of the list, loop to last
            playerpos = tracks.size()-1;
        } else {
            playerpos--;
        }
        return getCurrentTrack();
    }

    private TopTracks(Parcel in) {
        artist = in.readParcelable(TopTracks.class.getClassLoader());
        tracks = in.readArrayList(TopTracks.class.getClassLoader());
        playerpos = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(artist, flags);
        out.writeList(tracks);
        out.writeInt(playerpos);
    }

    public static final Parcelable.Creator<TopTracks> CREATOR = new Parcelable.Creator<TopTracks>() {
        public TopTracks createFromParcel(Parcel in) {
            return new TopTracks(in);
        }

        public TopTracks[] newArray(int size) {
            return new TopTracks[size];
        }
    };

    @Override
    public String toString() {
        String ret = artist.toString() + ", " + playerpos;
        for(Track t : tracks) {
            ret += "\n" + t.toString();
        }
        return ret;
    }
}
