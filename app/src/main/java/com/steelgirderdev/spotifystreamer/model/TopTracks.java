package com.steelgirderdev.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by teisentraeger on 6/3/2015.
 */
public class TopTracks implements Parcelable {
    public Artist artist;
    public ArrayList<Track> tracks;
    public int playerpos;
    public String command;

    public TopTracks(final Artist artist, final ArrayList<Track> tracks, final int playerpos, final String command) {
        this.artist = artist;
        this.tracks = tracks;
        this.playerpos = playerpos;
        this.command = command;
    }

    public TopTracks() {
        
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
        command = in.readString();
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
        out.writeString(command);
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
        String ret = artist.toString() + ", " + playerpos + ", \n" +command + "\n";
        for(Track t : tracks) {
            ret += "\n" + t.toString();
        }
        return ret;
    }

    public String getShareString() {
        return "I am listening to " + getCurrentTrack().trackname +" from " + artist.artistname + " #SpotifyStreamer " + " Listen Now: " + getCurrentTrack().urlPreview ;
    }

    public TopTracks createClone() {
        TopTracks clone = new TopTracks(artist, tracks, playerpos, command);
        return clone;
    }
}
