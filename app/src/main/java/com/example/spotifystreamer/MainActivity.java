package com.example.spotifystreamer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements ArtistsFragment.Callback {

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
    public void onArtistSelected(String artistId) {
        if (mUseTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(Utility.KEY_ARTIST_ID, artistId);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(arguments);

            getFragmentManager().beginTransaction()
                    .replace(R.id.top_ten_tracks_container, fragment, TAG_SEARCH_RESULTS_FRAGMENT)
                    .commit();
        } else {
            Intent intent = new Intent(this, TopTracksActivity.class)
                    .putExtra(Utility.KEY_ARTIST_ID, artistId);
            startActivity(intent);
        }
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
