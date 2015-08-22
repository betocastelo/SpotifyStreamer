package com.example.spotifystreamer;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.spotifystreamer.service.MediaPlayerService;

public class PlayerActivity extends Activity {

    private static final String LOG_TAG = PlayerActivity.class.getSimpleName();

    private static final String TAG_PLAYER_FRAGMENT = "player_fragment";

    private PlayerActivityFragment mPlayerFragment;

    private boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaPlayerService.MediaPlayerBinder binder =
                    (MediaPlayerService.MediaPlayerBinder) iBinder;
            MediaPlayerService mPlayerService = binder.getService();
            mBound = true;
            mPlayerFragment.setPlayerService(mPlayerService);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    private BroadcastReceiver onEndOfSong = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPlayerFragment.songFinished();
        }
    };

    private BroadcastReceiver onPlayerStarted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "Received broadcast.");

            mPlayerFragment.playerStarted();
        }
    };

    private BroadcastReceiver onPlayerPrepared = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPlayerFragment.playerPrepared();
        }
    };

    private BroadcastReceiver onSeekCompleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPlayerFragment.seekCompleted();
        }
    };

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter(Utility.ACTION_PLAYER_STARTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(onPlayerStarted, filter);
        filter = new IntentFilter(Utility.ACTION_PLAYER_PREPARED);
        LocalBroadcastManager.getInstance(this).registerReceiver(onPlayerPrepared, filter);
        filter = new IntentFilter(Utility.ACTION_END_OF_SONG);
        LocalBroadcastManager.getInstance(this).registerReceiver(onEndOfSong, filter);
        filter = new IntentFilter(Utility.ACTION_PLAYER_SEEK_COMPLETED);
        LocalBroadcastManager.getInstance(this).registerReceiver(onSeekCompleted, filter);
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onPlayerStarted);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onPlayerPrepared);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onEndOfSong);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onSeekCompleted);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        FragmentManager fragmentManager = getFragmentManager();
        mPlayerFragment =
                (PlayerActivityFragment) fragmentManager
                        .findFragmentByTag(TAG_PLAYER_FRAGMENT);

        if (mPlayerFragment == null) {
            mPlayerFragment = new PlayerActivityFragment();
            fragmentManager.beginTransaction().add(R.id.player_container, mPlayerFragment,
                    TAG_PLAYER_FRAGMENT)
                    .commit();
        }

        Intent intent = new Intent(this, MediaPlayerService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceivers();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
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
