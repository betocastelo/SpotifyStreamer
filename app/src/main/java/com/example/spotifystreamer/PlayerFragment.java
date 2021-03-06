package com.example.spotifystreamer;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifystreamer.service.MediaPlayerService;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

public class PlayerFragment extends DialogFragment {

    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    private static final String KEY_CURRENT_TRACK_POSITION = "current_track_position";

    private ArrayList<SpotifySearchResult> mSearchResults;
    private int mCurrentTrackIndex = 0;
    private int mPlayingTrackIndex = -1;
    private int mNumberOfResults = 0;
    private String mCurrentTrackPosition = "0:00";

    private MediaPlayerService mPlayerService = null;

    private int mPlayerState = -1;
    private boolean mTrackPlayRequested = false;

    // Media player relevant states
    private static final int PLAYER_STARTED = 0;
    private static final int PLAYER_PAUSED = 1;
    private static final int PLAYER_PREPARED = 2;

    // View components
    private ImageButton mImageButtonNext;
    private ImageButton mImageButtonPlayPause;
    private ImageButton mImageButtonPrevious;
    private ImageView mImageViewAlbum;
    private SeekBar mSeekBar;
    private TextView mTextViewAlbum;
    private TextView mTextViewArtist;
    private TextView mTextViewCurrentTime;
    private TextView mTextViewMaxTime;
    private TextView mTextViewTrack;

    // Resources
    private int mMissingImageIcon;
    private int mPauseIcon;
    private int mPlayIcon;

    // ************
    // Communications handling
    private boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaPlayerService.MediaPlayerBinder binder =
                    (MediaPlayerService.MediaPlayerBinder) iBinder;
            mPlayerService = binder.getService();
            mBound = true;

            if ((mPlayerState != PLAYER_PREPARED
                    && mPlayerState != PLAYER_STARTED
                    && mPlayerState != PLAYER_PAUSED)
                    || mPlayingTrackIndex != mCurrentTrackIndex) {
                initializePlayer();
            }

            populateTrackDuration();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    private BroadcastReceiver onEndOfSong = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSeekBar.removeCallbacks(mUpdateSeekBarRunnable);
            updateTrackPosition(0);
            mPlayerState = PLAYER_PAUSED; // Technically the media player is stopped, but we don't care.
            mTrackPlayRequested = false;
            mImageButtonPlayPause.setImageResource(mPlayIcon);
        }
    };

    private BroadcastReceiver onPlayerStarted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPlayerState = PLAYER_STARTED;
            updateTrackPosition();
        }
    };

    private BroadcastReceiver onPlayerPrepared = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPlayerState = PLAYER_PREPARED;
            mSeekBar.setEnabled(true);
            populateTrackDuration();

            if (mTrackPlayRequested) {
                playerStart();
            }
        }
    };

    private BroadcastReceiver onSeekCompleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSeekBar.postDelayed(mUpdateSeekBarRunnable, 1000);
        }
    };

    private void registerReceivers() {
        Context context = getActivity();
        IntentFilter filter = new IntentFilter(Utility.ACTION_PLAYER_STARTED);
        LocalBroadcastManager.getInstance(context).registerReceiver(onPlayerStarted, filter);
        filter = new IntentFilter(Utility.ACTION_PLAYER_PREPARED);
        LocalBroadcastManager.getInstance(context).registerReceiver(onPlayerPrepared, filter);
        filter = new IntentFilter(Utility.ACTION_END_OF_SONG);
        LocalBroadcastManager.getInstance(context).registerReceiver(onEndOfSong, filter);
        filter = new IntentFilter(Utility.ACTION_PLAYER_SEEK_COMPLETED);
        LocalBroadcastManager.getInstance(context).registerReceiver(onSeekCompleted, filter);
    }

    private void unregisterReceivers() {
        Context context = getActivity();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(onPlayerStarted);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(onPlayerPrepared);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(onEndOfSong);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(onSeekCompleted);
    }
    // End of communications handling
    // ***********

    private void initializePlayer() {
        SpotifySearchResult currentTrack = mSearchResults.get(mCurrentTrackIndex);
        initializePlayer(currentTrack.previewUrl);
    }

    private void initializePlayer(String previewUrl) {
        mSeekBar.setEnabled(false);

        if (mPlayerService != null) {
            try {
                mPlayerService.playerInitialize(previewUrl);
            } catch (IOException exception) {
                Toast.makeText(getActivity(), R.string.media_player_error_ioerror,
                        Toast.LENGTH_LONG).show();
            }

            mPlayingTrackIndex = mCurrentTrackIndex;
        }
    }

    private void initializeViews(View rootView) {
        mImageButtonNext = (ImageButton) rootView.findViewById(R.id.playerNextButton);
        mImageButtonPlayPause = (ImageButton) rootView.findViewById(R.id.playerPlayPauseButton);
        mImageButtonPrevious = (ImageButton) rootView.findViewById(R.id.playerPreviousButton);
        mImageViewAlbum = (ImageView) rootView.findViewById(R.id.playerAlbumCover);
        mTextViewAlbum = (TextView) rootView.findViewById(R.id.playerAlbumName);
        mTextViewArtist = (TextView) rootView.findViewById(R.id.playerArtistName);
        mTextViewTrack = (TextView) rootView.findViewById(R.id.playerTrackName);
        mTextViewCurrentTime = (TextView) rootView.findViewById(R.id.playerCurrentTime);
        mTextViewMaxTime = (TextView) rootView.findViewById(R.id.playerDuration);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.playerSeekBar);

        mMissingImageIcon = Utility.getDrawableResourceId(Utility.DRAWABLE_MISSING_IMAGE_ICON);
        mPauseIcon = Utility.getDrawableResourceId(Utility.DRAWABLE_PAUSE_ICON);
        mPlayIcon = Utility.getDrawableResourceId(Utility.DRAWABLE_PLAY_ICON);
    }

    /**
     * Loads current track (mCurrentTrackIndex) from search results into view and media player.
     * Assume views have been initialized (see initializeViews).
     */
    private void loadTrack() {
        // makes sure we don't overrun bounds in either direction (and java modulus is
        // different from c modulus, so we need to check sign).
        mCurrentTrackIndex %= mNumberOfResults;
        if (mCurrentTrackIndex < 0) {
            mCurrentTrackIndex += mNumberOfResults;
        }

        SpotifySearchResult currentTrack = mSearchResults.get(mCurrentTrackIndex);

        Log.i(LOG_TAG, "Index: " + mCurrentTrackIndex);
        Log.i(LOG_TAG, "Artist: " + currentTrack.artistName);
        Log.i(LOG_TAG, "Album: " + currentTrack.albumName);
        Log.i(LOG_TAG, "Name: " + currentTrack.trackName);

        mTextViewCurrentTime.setText(mCurrentTrackPosition);

        if (mCurrentTrackIndex != mPlayingTrackIndex) {
            initializePlayer(currentTrack.previewUrl);
            playerStart();
        } else if (mPlayerState == PLAYER_STARTED) {
            mTrackPlayRequested = true;
            mImageButtonPlayPause.setImageResource(
                    Utility.getDrawableResourceId(Utility.DRAWABLE_PAUSE_ICON));
            updateTrackPosition();
        }

        mTextViewArtist.setText(currentTrack.artistName);
        mTextViewAlbum.setText(currentTrack.albumName);

        if (mImageViewAlbum != null  && currentTrack.imageUrl != null) {
            Picasso.with(getActivity()).load(currentTrack.imageUrl).into(mImageViewAlbum);
        } else if (mImageViewAlbum != null) {
            mImageViewAlbum.setImageResource(mMissingImageIcon);
        }

        mTextViewTrack.setText(currentTrack.trackName);

        populateTrackDuration();
        updateTrackPosition(0);
        setupSeekBar();
    }

    private void nextTrack() {
        mSeekBar.removeCallbacks(mUpdateSeekBarRunnable);
        mCurrentTrackIndex++; // loadTrack takes care of index robustness
        loadTrack();
    }

    private void playerPause() {
        if (mPlayerService != null) {
            mPlayerService.playerPause();
        }

        mSeekBar.removeCallbacks(mUpdateSeekBarRunnable);
        mTrackPlayRequested = false;
        mImageButtonPlayPause.setImageResource(mPlayIcon);
        mPlayerState = PLAYER_PAUSED;
    }

    private void playerStart() {
        mTrackPlayRequested = true;

        if (mPlayerService != null) {
            mPlayerService.playerStart();
            mPlayingTrackIndex = mCurrentTrackIndex;
            mImageButtonPlayPause.setImageResource(mPauseIcon);
        }
    }

    private void playPauseTrack() {
        if (mPlayerService != null) {
            if (mTrackPlayRequested) {
                playerPause();
            } else {
                playerStart();
            }
        }
    }

    private void populateTrackDuration() {
        int milliseconds;
        if (mPlayerService != null) {
            milliseconds = mPlayerService.getSongDuration();
        } else {
            milliseconds = 30000;
        }

        mSeekBar.setMax(milliseconds);
        mTextViewMaxTime.setText(timeStringFromMs(milliseconds));
    }

    private void previousTrack() {
        mCurrentTrackIndex--; // loadTrack takes care of index robustness
        loadTrack();
    }

    /**
     * {@see initializeViews} must have been called first. There shouldn't be a need to check
     * this here, since these are both internal implementation details.
     */
    private void setupButtons() {
        mImageButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextTrack();
            }
        });

        mImageButtonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousTrack();
            }
        });

        mImageButtonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPauseTrack();
            }
        });
    }

    private void setupSeekBar() {
        if (mSeekBar == null)
            return;

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.removeCallbacks(mUpdateSeekBarRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mPlayerState == PLAYER_STARTED) {
                    seekBar.postDelayed(mUpdateSeekBarRunnable, 1000);
                }

                mPlayerService.playerSeek(seekBar.getProgress());
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mTextViewCurrentTime.setText(timeStringFromMs(progress));
                }
            }
        });
    }

    private String timeStringFromMs(int milliseconds) {
        int minutes = milliseconds/60000;
        milliseconds /= 1000;
        milliseconds %= 60;
        return (minutes + ":" + (milliseconds > 9 ? milliseconds : "0" + milliseconds));
    }

    private final Runnable mUpdateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            updateTrackPosition();
        }
    };

    /**
     * Calling this updates the seekbar position, and starts timer for regular updates.
     *
     * Based on http://stackoverflow.com/questions/24725030/using-seekbar-with-music-in-android.
     */
    private void updateTrackPosition() {
        mSeekBar.removeCallbacks(mUpdateSeekBarRunnable);
        if (mPlayerService != null) {
            updateTrackPosition(mPlayerService.getSongPosition());
        }
        mSeekBar.postDelayed(mUpdateSeekBarRunnable, 1000);
    }

    private void updateTrackPosition(int position) {
        mSeekBar.setProgress(position);
        mTextViewCurrentTime.setText(timeStringFromMs(position));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mSearchResults = new ArrayList<>();
            Bundle arguments = getArguments();
            if (arguments != null) {
                mSearchResults = arguments.getParcelableArrayList(Utility.KEY_SEARCH_RESULTS);
                mCurrentTrackIndex = arguments.getInt(Utility.KEY_TRACK_INDEX);
            }
        } else {
            mSearchResults = savedInstanceState.getParcelableArrayList(Utility.KEY_SEARCH_RESULTS);
            mCurrentTrackIndex = savedInstanceState.getInt(Utility.KEY_TRACK_INDEX);
            mPlayerState = savedInstanceState.getInt(Utility.KEY_PLAYER_STATE);
            mPlayingTrackIndex = savedInstanceState.getInt(Utility.KEY_PLAYING_TRACK_INDEX);
            mCurrentTrackPosition = savedInstanceState.getString(KEY_CURRENT_TRACK_POSITION);
        }

        mNumberOfResults = mSearchResults.size();

        Context context = getActivity();
        Intent intent = new Intent(context, MediaPlayerService.class);
        context.startService(intent);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        initializeViews(rootView);
        loadTrack();
        setupButtons();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(Utility.KEY_SEARCH_RESULTS, mSearchResults);
        outState.putInt(Utility.KEY_TRACK_INDEX, mCurrentTrackIndex);
        outState.putInt(Utility.KEY_PLAYING_TRACK_INDEX, mPlayingTrackIndex);
        outState.putInt(Utility.KEY_PLAYER_STATE, mPlayerState);
        outState.putString(KEY_CURRENT_TRACK_POSITION, (String) mTextViewCurrentTime.getText());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceivers();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers();
    }

    @Override
    public void onStop() {
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }

        mSeekBar.removeCallbacks(mUpdateSeekBarRunnable);
        super.onStop();
    }

    public static PlayerFragment newInstance(ArrayList<SpotifySearchResult> searchResults,
                                             int currentTrackIndex) {
        PlayerFragment fragment = new PlayerFragment();

        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(Utility.KEY_SEARCH_RESULTS, searchResults);
        arguments.putInt(Utility.KEY_TRACK_INDEX, currentTrackIndex);
        fragment.setArguments(arguments);
        return fragment;
    }
}
