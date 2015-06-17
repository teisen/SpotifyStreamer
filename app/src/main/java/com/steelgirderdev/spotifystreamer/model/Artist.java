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
 * Artist object that is parcable
 */
public class Artist implements Parcelable {

    public String artistname;
    public String spotifyId;
    public int popularity;
    public String urlThumbnail;
    public String urlHighres;

    public Artist() {

    }

    public Artist(final kaaes.spotify.webapi.android.models.Artist object) {
        this.artistname = object.name;
        this.spotifyId = object.id;
        this.popularity = object.popularity;

        if(object.images.size()>0) {
            /*
            Artist thumbnail (large (640px for Now Playing screen) and small (200px for list items)).
            If the image size does not exist in the API response, you are free to choose whatever size is available.)
             */
            //find url with size 640 for the Now Playing screen
            List<Image> imagesHighres = ImageSizeFilter.filterLargerThan(object.images, 639);
            Log.v(Constants.LOG_TAG, "found " + imagesHighres.size() + " artist highres image/s");

            //find url with size 300 for the list item
            List<Image> imagesThumbnails = ImageSizeFilter.filterSmallerThan(object.images, 301);
            Log.v(Constants.LOG_TAG, "found " + imagesThumbnails.size() + " artist thumbnail image/s");

            // if we have a 300 or smaller image use it for the list, otherwise simply use the first image
            if(imagesThumbnails.isEmpty()) {
                this.urlThumbnail = object.images.get(0).url;
            } else {
                this.urlThumbnail = imagesThumbnails.get(0).url;
            }
            // use the highres img if available, otherwise use the first available one
            if(imagesHighres.isEmpty()) {
                this.urlHighres = object.images.get(0).url;
            } else {
                this.urlHighres = imagesHighres.get(0).url;
            }
        }
    }

    private Artist(Parcel in) {
        artistname = in.readString();
        spotifyId = in.readString();
        popularity = in.readInt();
        urlThumbnail = in.readString();
        urlHighres = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(artistname);
        out.writeString(spotifyId);
        out.writeInt(popularity);
        out.writeString(urlThumbnail);
        out.writeString(urlHighres);
    }

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    @Override
    public String toString() {
        return artistname + " , " + spotifyId + " , " + popularity;
    }
}
