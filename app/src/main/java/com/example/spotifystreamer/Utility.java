package com.example.spotifystreamer;

import android.content.res.Resources;

/**
 * Resources management, common definitions, helper methods, etc.
 */
public class Utility {
    public static final String KEY_MEDIA_ID = "media_id";
    public static final String KEY_MEDIA_URL = "media_url";
    public static final String KEY_PLAYER_STATE = "player_state";
    public static final String KEY_PLAYING_TRACK_INDEX = "playing_track_index";
    public static final String KEY_TRACK_INDEX = "clicked_index";
    public static final String KEY_SEARCH_RESULTS = "search_results";

    public static final String TAG_TOP_TRACKS_FRAGMENT = "top_tracks_fragment";
    public static final String TAG_PLAYER_FRAGMENT = "player_fragment";

    // Used in LocalBroadcast (so I don't have to worry about collision with unknown
    // pending intents.
    public static final String ACTION_PLAY_STARTED = "action_play_started";

    // List of system drawables we use in this app.
    public static int DRAWABLE_MISSING_IMAGE_ICON = 0;
    public static int DRAWABLE_PAUSE_ICON = 1;
    public static int DRAWABLE_PLAY_ICON = 2;

    public static int getDrawableResourceId(int drawable) {
        if (drawable == DRAWABLE_MISSING_IMAGE_ICON) {
            return Resources.getSystem().getIdentifier("ic_dialog_alert",
                    "drawable", "android");
        }

        if (drawable == DRAWABLE_PAUSE_ICON) {
            return Resources.getSystem().getIdentifier("ic_media_pause",
                    "drawable", "android");
        }

        if (drawable == DRAWABLE_PLAY_ICON) {
            return Resources.getSystem().getIdentifier("ic_media_play",
                    "drawable", "android");
        }

        return -1;
    }
}
