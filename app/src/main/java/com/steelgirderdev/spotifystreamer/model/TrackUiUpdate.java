package com.steelgirderdev.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by teisentraeger on 6/8/2015.
 * Holds a small parcel with just enough info to update the player UI
 */
public class TrackUiUpdate implements Parcelable {

    public int trackpos;
    public int playerpos;
    public boolean playing;

    public TrackUiUpdate(int trackpos, int playerpos, boolean playing) {
        this.trackpos = trackpos;
        this.playerpos = playerpos;
        this.playing = playing;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private TrackUiUpdate(Parcel in) {
        trackpos = in.readInt();
        playerpos = in.readInt();
        playing = (Boolean) in.readValue( null );
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(trackpos);
        out.writeInt(playerpos);
        out.writeValue(playing);
    }

    public static final Parcelable.Creator<TrackUiUpdate> CREATOR = new Parcelable.Creator<TrackUiUpdate>() {
        public TrackUiUpdate createFromParcel(Parcel in) {
            return new TrackUiUpdate(in);
        }

        public TrackUiUpdate[] newArray(int size) {
            return new TrackUiUpdate[size];
        }
    };

    @Override
    public String toString() {
        return "trackpos: "+trackpos/100 + " playerpos:"+playerpos + " playing:"+playing;
    }
}
