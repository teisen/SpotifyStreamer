package com.steelgirderdev.spotifystreamer.filters;

import android.util.Log;

import com.steelgirderdev.spotifystreamer.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by teisentraeger on 6/1/2015.
 */
public class ImageSizeFilter {

    public static List<Image> filterLargerThan(List<Image> unfiltered, Integer height) {
        List<Image> res = new ArrayList<Image>();
        for (Image img : unfiltered) {
            if(img.height > height) {
                res.add(img);
            }
        }
        return res;
    }

    public static List<Image> filterSmallerThan(List<Image> unfiltered, Integer height) {
        List<Image> res = new ArrayList<Image>();
        for (Image img : unfiltered) {
            if(img.height < height) {
                res.add(img);
            }
        }
        return res;
    }

    public static List<Image> filterEquals(List<Image> unfiltered, Integer height) {
        List<Image> res = new ArrayList<Image>();
        for (Image img : unfiltered) {
            if(img.height.equals(height)) {
                res.add(img);
            }
        }
        return res;
    }
}
