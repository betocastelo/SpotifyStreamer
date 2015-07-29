package com.example.spotifystreamer;

import android.content.res.Resources;

/**
 * Resources management, common definitions, helper methods, etc.
 */
public class Utility {
    public static final String KEY_CLICKED_INDEX = "clicked_index";
    public static final String KEY_MEDIA_ID = "media_id";
    public static final String KEY_SEARCH_RESULTS = "search_results";

    public static final String TAG_TOP_TRACKS_FRAGMENT = "top_tracks_fragment";
    public static final String TAG_PLAYER_FRAGMENT = "player_fragment";

    // List of system drawables we use in this app.
    public static int DRAWABLE_MISSING_IMAGE_ICON = 0;

    public static int getDrawableResourceId(int drawable) {
        if (drawable == DRAWABLE_MISSING_IMAGE_ICON) {
            return Resources.getSystem().getIdentifier("ic_dialog_alert",
                    "drawable", "android");
        }

        return -1;
    }
}
