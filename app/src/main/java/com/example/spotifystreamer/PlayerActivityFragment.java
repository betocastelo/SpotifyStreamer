package com.example.spotifystreamer;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerActivityFragment extends Fragment {

    private static final String LOG_TAG = PlayerActivity.class.getSimpleName();

    private ArrayList<SpotifySearchResult> mSearchResults;
    private int mCurrentTrackIndex = 0;
    private int mNumberOfResults = 0;

    // Views
    private ImageView mImageViewAlbum;
    private TextView mTextViewAlbum;
    private TextView mTextViewArtist;
    private TextView mTextViewTrack;

    private void initializeView(View rootView) {
        mImageViewAlbum = (ImageView) rootView.findViewById(R.id.playerAlbumCover);
        mTextViewAlbum = (TextView) rootView.findViewById(R.id.playerAlbumName);
        mTextViewArtist = (TextView) rootView.findViewById(R.id.playerArtistName);
        mTextViewTrack = (TextView) rootView.findViewById(R.id.playerTrackName);
        loadTrackIntoView();
    }

    private void loadTrackIntoView() {
        // makes sure we don't overrun bounds in either direction
        mCurrentTrackIndex %= mNumberOfResults;
        if (mCurrentTrackIndex < 0) {
            mCurrentTrackIndex += mNumberOfResults;
        }

        SpotifySearchResult currentTrack = mSearchResults.get(mCurrentTrackIndex);

        Log.i(LOG_TAG, "Index: " + mCurrentTrackIndex);
        Log.i(LOG_TAG, "Artist: " + currentTrack.artistName);
        Log.i(LOG_TAG, "Album: " + currentTrack.albumName);
        Log.i(LOG_TAG, "Name: " + currentTrack.trackName);

        mTextViewArtist.setText(currentTrack.artistName);
        mTextViewAlbum.setText(currentTrack.albumName);

        if (mImageViewAlbum != null  && currentTrack.imageUrl != null) {
            Picasso.with(getActivity()).load(currentTrack.imageUrl).into(mImageViewAlbum);
        } else if (mImageViewAlbum != null) {
            mImageViewAlbum.setImageResource(
                    Utility.getDrawableResourceId(Utility.DRAWABLE_MISSING_IMAGE_ICON));
        }

        mTextViewTrack.setText(currentTrack.trackName);
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
        }

        mNumberOfResults = mSearchResults.size();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        initializeView(rootView);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Utility.KEY_SEARCH_RESULTS, mSearchResults);
        outState.putInt(Utility.KEY_TRACK_INDEX, mCurrentTrackIndex);
        super.onSaveInstanceState(outState);
    }

    public void nextTrack() {
        mCurrentTrackIndex++; // loadTrackIntoView takes care of index robustness
        loadTrackIntoView();
    }

    public void playPauseTrack() {

    }

    public void previousTrack() {
        mCurrentTrackIndex--; // loadTrackIntoView takes care of index robustness
        loadTrackIntoView();
    }
}
