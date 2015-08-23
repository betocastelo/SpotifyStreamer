package com.example.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.spotifystreamer.Utility;

import java.io.IOException;

/**
 *
 */
public class MediaPlayerService extends Service {

    private static final String LOG_TAG = MediaPlayerService.class.getSimpleName();

    private final IBinder mBinder = new MediaPlayerBinder();

    private static MediaPlayer mMediaPlayer = null;
    private int mPlayerState = -1;
    private final Object mSynchronizationLock = new Object();
    private boolean mTrackPlayRequested = false;

    // Media player relevant states
    private static final int PLAYER_IDLE = 0;
    private static final int PLAYER_INITIALIZED = 1;
    private static final int PLAYER_PREPARED = 2;
    private static final int PLAYER_STARTED = 3;
    private static final int PLAYER_PAUSED = 4;
    private static final int PLAYER_COMPLETED = 5;

    private void playerCompleted() {
        Log.i(LOG_TAG, "MP completed, stopping self.");
        Intent intent = new Intent(Utility.ACTION_END_OF_SONG);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        stopSelf();
    }

    private void setupMediaPlayer() {
        Log.i(LOG_TAG, "Setting up player...");

        if (mMediaPlayer == null) {
            Log.i(LOG_TAG, "Creating new player...");
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayerState = PLAYER_IDLE;
        }

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.i(LOG_TAG, "Player prepared...");

                synchronized (mSynchronizationLock) {
                    mPlayerState = PLAYER_PREPARED;
                }

                Intent intent = new Intent(Utility.ACTION_PLAYER_PREPARED);
                LocalBroadcastManager.getInstance(MediaPlayerService.this).sendBroadcast(intent);

                if (mTrackPlayRequested) {
                    playerStart();
                }
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mTrackPlayRequested = false;
                mediaPlayer.stop();
                mediaPlayer.prepareAsync();
                playerCompleted();
            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                Log.i(LOG_TAG, "In onError callback...");

                if (extra == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    playerReset();
                }

                // todo Handle different types of errors.

                return false;
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        setupMediaPlayer();
        return mBinder;
    }

    public int getSongPosition() {
        Log.i(LOG_TAG, "Sending song position...");
        return mMediaPlayer.getCurrentPosition();
    }

    public int getSongDuration() {
        int duration = 30000;
        synchronized (mSynchronizationLock) {
            if (mPlayerState != PLAYER_IDLE && mPlayerState != PLAYER_INITIALIZED) {
                duration = mMediaPlayer.getDuration();
            }
        }

        return duration;
    }

    public void playerInitialize(String previewUrl) throws IOException {
        Log.i(LOG_TAG, "Initializing player...");

        synchronized (mSynchronizationLock) {
            if (mPlayerState != PLAYER_IDLE) {
                playerReset();
            }
        }

        mMediaPlayer.setDataSource(previewUrl);
        mPlayerState = PLAYER_INITIALIZED;
        mMediaPlayer.prepareAsync();
    }

    public void playerPause() {
        Log.i(LOG_TAG, "Pausing player...");

        synchronized (mSynchronizationLock) {
            if (mPlayerState == PLAYER_STARTED) {
                mMediaPlayer.pause();
                mPlayerState = PLAYER_PAUSED;
                mTrackPlayRequested = false;
            }
        }
    }

    public void playerReset() {
        Log.i(LOG_TAG, "Resetting player...");

        synchronized (mSynchronizationLock) {
            mMediaPlayer.reset();
            mPlayerState = PLAYER_IDLE;
        }
    }

    public void playerSeek(int milliseconds) {
        if (mPlayerState == PLAYER_PREPARED
                || mPlayerState == PLAYER_STARTED
                || mPlayerState == PLAYER_PAUSED
                || mPlayerState == PLAYER_COMPLETED) {
            mMediaPlayer.seekTo(milliseconds);
        }
    }

    public void playerStart() {
        mTrackPlayRequested = true;

        synchronized (mSynchronizationLock) {
            if (mPlayerState == PLAYER_PREPARED || mPlayerState == PLAYER_PAUSED) {
                Log.i(LOG_TAG, "Starting player...");
                mMediaPlayer.start();
                mPlayerState = PLAYER_STARTED;
                Intent intent = new Intent(Utility.ACTION_PLAYER_STARTED);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }
    }

    public class MediaPlayerBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}
