package com.example.spotifystreamer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

    private static final String TAG_SEARCH_RESULTS_FRAGMENT = "search_results_fragment";

    private boolean mUseTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.top_ten_tracks_container) != null) {
            // The top10 container will only be present in large-screen layouts (two-pane).
            mUseTwoPane = true;

            if (savedInstanceState == null) { // If we are rotating the frag state gets saved
                                              // automatically.
                getFragmentManager().beginTransaction()
                        .replace(R.id.top_ten_tracks_container, new TopTracksFragment(),
                                TAG_SEARCH_RESULTS_FRAGMENT)
                        .commit();
            }
        } else {
            mUseTwoPane = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
