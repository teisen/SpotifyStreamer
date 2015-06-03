package com.steelgirderdev.spotifystreamer.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by teisentraeger on 6/2/2015.
 * Utility with common static method for UI Use
 */
public class UIUtil {

    public static void toastIt(Context context, Toast toast, CharSequence msg) {
        //Stop any previous toasts
        if(toast !=null){
            toast.cancel();
        }

        //Make and display new toast
        toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

}
