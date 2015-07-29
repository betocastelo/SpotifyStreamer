package com.example.spotifystreamer;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Top 10 tracks activity. Relies on dialect shown at
 * http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html to
 * maintain state between configuration changes.
 */
public class TopTracksActivity extends Activity {

    private static final String LOG_TAG = TopTracksActivity.class.getSimpleName();

    private TopTracksActivityFragment mTopTracksFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        // todo We're no longer using this dialect. Fix me.
        FragmentManager fragmentManager = getFragmentManager();
        mTopTracksFragment = (TopTracksActivityFragment) fragmentManager
                .findFragmentByTag(Utility.TAG_TOP_TRACKS_FRAGMENT);

        // If the fragment is non-null, then it is currently being retained across a configuration
        // change.
        if (mTopTracksFragment == null) {
            Log.i(LOG_TAG, "New instance of top 10 fragment -- none retained.");
            mTopTracksFragment = new TopTracksActivityFragment();
            fragmentManager.beginTransaction().add(mTopTracksFragment,
                    Utility.TAG_TOP_TRACKS_FRAGMENT).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
