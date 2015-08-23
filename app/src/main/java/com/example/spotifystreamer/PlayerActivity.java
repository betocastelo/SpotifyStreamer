package com.example.spotifystreamer;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class PlayerActivity extends Activity {

    private static final String LOG_TAG = PlayerActivity.class.getSimpleName();

    private static final String TAG_PLAYER_FRAGMENT = "player_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        FragmentManager fragmentManager = getFragmentManager();
        PlayerFragment playerFragment = (PlayerFragment) fragmentManager
                .findFragmentByTag(TAG_PLAYER_FRAGMENT);

        if (playerFragment == null) {
            playerFragment = new PlayerFragment();
            fragmentManager.beginTransaction().add(R.id.player_container, playerFragment,
                    TAG_PLAYER_FRAGMENT)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
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
