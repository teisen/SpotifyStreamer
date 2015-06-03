package com.steelgirderdev.spotifystreamer.filter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by teisentraeger on 6/1/2015.
 * Filters The Images by size
 */
public class ImageSizeFilter {

    public static List<Image> filterLargerThan(List<Image> unfiltered, Integer height) {
        List<Image> res = new ArrayList<>();
        for (Image img : unfiltered) {
            if(img.height > height) {
                res.add(img);
            }
        }
        return res;
    }

    public static List<Image> filterSmallerThan(List<Image> unfiltered, Integer height) {
        List<Image> res = new ArrayList<>();
        for (Image img : unfiltered) {
            if(img.height < height) {
                res.add(img);
            }
        }
        return res;
    }

    public static List<Image> filterEquals(List<Image> unfiltered, Integer height) {
        List<Image> res = new ArrayList<>();
        for (Image img : unfiltered) {
            if(img.height.equals(height)) {
                res.add(img);
            }
        }
        return res;
    }
}
