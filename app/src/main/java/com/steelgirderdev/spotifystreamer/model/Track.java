package com.steelgirderdev.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.filter.ImageSizeFilter;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by teisentraeger on 6/2/2015.
 * Source: http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate
 * Track object that is parcable
 */
public class Track implements Parcelable {

    public String trackname;
    public String albumname;
    public String urlPreview;
    public String urlThumbnail;
    public String urlHighres;

    public Track(final kaaes.spotify.webapi.android.models.Track object) {
        this.trackname = object.name;
        this.albumname = object.album.name;
        this.urlPreview = object.preview_url;

        if(object.album.images.size()>0) {
            /*
            Album art thumbnail (large (640px for Now Playing screen) and small (200px for list items)).
            If the image size does not exist in the API response, you are free to choose whatever size is available.)
             */
            //find url with size 640 for the Now Playing screen
            List<Image> imagesHighres = ImageSizeFilter.filterLargerThan(object.album.images, 639);
            Log.v(Constants.LOG_TAG, "found " + imagesHighres.size() + " album highres image/s");

            //find url with size 300 for the list item
            List<Image> imagesThumbnails = ImageSizeFilter.filterSmallerThan(object.album.images, 301);
            Log.v(Constants.LOG_TAG, "found " + imagesThumbnails.size() + " album thumbnail image/s");

            // if we have a 300 or smaller image use it for the list, otherwise simple use the first image
            if(imagesThumbnails.isEmpty()) {
                this.urlThumbnail = object.album.images.get(0).url;
            } else {
                this.urlThumbnail = imagesThumbnails.get(0).url;
            }
            // use the highres img if available, otherwise use the first available one
            if(imagesHighres.isEmpty()) {
                this.urlHighres = object.album.images.get(0).url;
            } else {
                this.urlHighres = imagesHighres.get(0).url;
            }
        }
    }

    private Track(Parcel in) {
        trackname = in.readString();
        albumname = in.readString();
        urlPreview = in.readString();
        urlThumbnail = in.readString();
        urlHighres = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(trackname);
        out.writeString(albumname);
        out.writeString(urlPreview);
        out.writeString(urlThumbnail);
        out.writeString(urlHighres);
    }

    public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    @Override
    public String toString() {
        return trackname + ", " + albumname;
    }
}
