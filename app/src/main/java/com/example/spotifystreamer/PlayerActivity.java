package com.example.spotifystreamer;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class PlayerActivity extends Activity {

    private PlayerActivityFragment mPlayerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        FragmentManager fragmentManager = getFragmentManager();
        mPlayerFragment =
                (PlayerActivityFragment) fragmentManager
                        .findFragmentByTag(Utility.TAG_PLAYER_FRAGMENT);

        if (mPlayerFragment == null) {
            mPlayerFragment = new PlayerActivityFragment();
            fragmentManager.beginTransaction().add(R.id.player_container, mPlayerFragment,
                    Utility.TAG_PLAYER_FRAGMENT)
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

    public void nextTrack(View view) {
        mPlayerFragment.nextTrack();
    }

    public void playPauseTrack(View view) {
        mPlayerFragment.playPauseTrack();
    }

    public void previousTrack(View view) {
        mPlayerFragment.previousTrack();
    }
}
