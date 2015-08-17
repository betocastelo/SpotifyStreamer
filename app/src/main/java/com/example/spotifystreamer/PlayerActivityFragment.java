package com.example.spotifystreamer;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifystreamer.service.MediaPlayerService;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerActivityFragment extends Fragment {

    private static final String LOG_TAG = PlayerActivityFragment.class.getSimpleName();

    private ArrayList<SpotifySearchResult> mSearchResults;
    private int mCurrentTrackIndex = 0;
    private int mPlayingTrackIndex = -1;
    private int mNumberOfResults = 0;

    private MediaPlayerService mPlayerService = null;

    private int mPlayerState = -1;
    private boolean mTrackPlayRequested = false;

    // Media player relevant states
    private static final int PLAYER_STARTED = 0;
    private static final int PLAYER_PAUSED = 1;

    // Views
    private ImageButton mImageButtonPlayPause;
    private ImageView mImageViewAlbum;
    private TextView mTextViewAlbum;
    private TextView mTextViewArtist;
    private TextView mTextViewTrack;

    // Resources
    private int mMissingImageIcon;
    private int mPauseIcon;
    private int mPlayIcon;

    private void initializeViews(View rootView) {
        mImageButtonPlayPause = (ImageButton) rootView.findViewById(R.id.playerPlayPauseButton);
        mImageViewAlbum = (ImageView) rootView.findViewById(R.id.playerAlbumCover);
        mTextViewAlbum = (TextView) rootView.findViewById(R.id.playerAlbumName);
        mTextViewArtist = (TextView) rootView.findViewById(R.id.playerArtistName);
        mTextViewTrack = (TextView) rootView.findViewById(R.id.playerTrackName);

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

        // Load track into MediaPlayer object, but only if this isn't a configuration change.
        if ((mPlayerState != PLAYER_STARTED && mPlayerState != PLAYER_PAUSED)
                || mPlayingTrackIndex != mCurrentTrackIndex) {
            playerInitialize(currentTrack.previewUrl);
        } else if (mPlayerState == PLAYER_STARTED) {
            mTrackPlayRequested = true;
            mImageButtonPlayPause.setImageResource(
                    Utility.getDrawableResourceId(Utility.DRAWABLE_PAUSE_ICON));
        }

        mTextViewArtist.setText(currentTrack.artistName);
        mTextViewAlbum.setText(currentTrack.albumName);

        if (mImageViewAlbum != null  && currentTrack.imageUrl != null) {
            Picasso.with(getActivity()).load(currentTrack.imageUrl).into(mImageViewAlbum);
        } else if (mImageViewAlbum != null) {
            mImageViewAlbum.setImageResource(mMissingImageIcon);
        }

        mTextViewTrack.setText(currentTrack.trackName);
    }

    private void playerInitialize() {
        SpotifySearchResult currentTrack = mSearchResults.get(mCurrentTrackIndex);
        playerInitialize(currentTrack.previewUrl);
    }
    
    private void playerInitialize(String previewUrl) {
        Log.i(LOG_TAG, "Initializing player...");

        if (mPlayerService != null) {
            try {
                mPlayerService.playerInitialize(previewUrl);
            } catch (IOException exception) {
                Toast.makeText(getActivity(), R.string.media_player_error_ioerror,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void playerPause() {
        Log.i(LOG_TAG, "Pausing player...");

        if (mPlayerService != null) {
            mPlayerService.playerPause();
        }

        mTrackPlayRequested = false;
        mImageButtonPlayPause.setImageResource(mPlayIcon);
        mPlayerState = PLAYER_PAUSED;
    }

    private void playerStart() {
        mTrackPlayRequested = true;

        if (mPlayerService != null) {
            Log.i(LOG_TAG, "Starting player...");
            mPlayerService.playerStart();
            mPlayingTrackIndex = mCurrentTrackIndex;
            mImageButtonPlayPause.setImageResource(mPauseIcon);
        }
    }

    private void startSeekBar(int position) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null
                || !savedInstanceState.containsKey(Utility.KEY_SEARCH_RESULTS)) {
            mSearchResults = new ArrayList<>();

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Utility.KEY_SEARCH_RESULTS)) {
                mSearchResults = intent.getParcelableArrayListExtra(Utility.KEY_SEARCH_RESULTS);

                if (intent.hasExtra(Utility.KEY_TRACK_INDEX))
                    mCurrentTrackIndex = intent.getIntExtra(Utility.KEY_TRACK_INDEX, 0);
            }
        } else {
            mSearchResults = savedInstanceState.getParcelableArrayList(Utility.KEY_SEARCH_RESULTS);
            mCurrentTrackIndex = savedInstanceState.getInt(Utility.KEY_TRACK_INDEX);
            mPlayerState = savedInstanceState.getInt(Utility.KEY_PLAYER_STATE);
            mPlayingTrackIndex = savedInstanceState.getInt(Utility.KEY_PLAYING_TRACK_INDEX);
        }

        mNumberOfResults = mSearchResults.size();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        initializeViews(rootView);
        loadTrack();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Utility.KEY_SEARCH_RESULTS, mSearchResults);
        outState.putInt(Utility.KEY_TRACK_INDEX, mCurrentTrackIndex);
        outState.putInt(Utility.KEY_PLAYING_TRACK_INDEX, mPlayingTrackIndex);
        outState.putInt(Utility.KEY_PLAYER_STATE, mPlayerState);
        super.onSaveInstanceState(outState);
    }

    public void nextTrack() {
        mCurrentTrackIndex++; // loadTrack takes care of index robustness
        loadTrack();
    }

    public void playerStarted() {
        mPlayerState = PLAYER_STARTED;
        mPlayingTrackIndex = mCurrentTrackIndex;
        startSeekBar(0);
    }

    /**
     * todo update this comment
     * This method only has any effect if the MediaPlayer member has been prepared. Otherwise
     * it's a no-op. This method's main purpose is to be used as part of gui call backs (in
     * which case initialization is likely to have occurred).
     */
    public void playPauseTrack() {
        if (mPlayerService != null) {
            if (mTrackPlayRequested) {
                playerPause();
            } else {
                playerStart();
            }
        }
    }

    public void previousTrack() {
        mCurrentTrackIndex--; // loadTrack takes care of index robustness
        loadTrack();
    }

    /**
     * Allows parent activity to send this fragment the parent's bound service instance.
     * @param player Parent's bound service instance.
     */
    public void setPlayerService(MediaPlayerService player) {
        mPlayerService = player;

        if ((mPlayerState != PLAYER_STARTED && mPlayerState != PLAYER_PAUSED)
                || mPlayingTrackIndex != mCurrentTrackIndex) {
            playerInitialize();
        }
    }
}
