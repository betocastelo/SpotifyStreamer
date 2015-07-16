package com.example.spotifystreamer;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * Displays the top 10 tracks for the given artist id (passed by Intent.EXTRA_TEXT).
 */
public class TopTracksActivityFragment extends Fragment {

    private static final String LOG_TAG = TopTracksActivityFragment.class.getSimpleName();
    private static final String KEY_RESULTS = "search_results";

    private TracksAdapter mTracksAdapter;
    private ArrayList<SpotifySearchResult> mSearchResults;

    /**
     * Calls top 10 tracks retrieve task from spotify web api.
     * @param artistId Spotify unique artist id.
     */
    private void retrieveTopTracks(String artistId) {
        SpotifyTracksTask spotifyTracksTask = new SpotifyTracksTask();
        spotifyTracksTask.execute(artistId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null || !savedInstanceState.containsKey(KEY_RESULTS)) {
            mSearchResults = new ArrayList<>();

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                String artistId = intent.getStringExtra(Intent.EXTRA_TEXT);
                retrieveTopTracks(artistId);
            }
        } else {
            mSearchResults = savedInstanceState.getParcelableArrayList(KEY_RESULTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        mTracksAdapter = new TracksAdapter(getActivity(), mSearchResults);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_top_tracks);
        listView.setAdapter(mTracksAdapter);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_RESULTS, mSearchResults);
        super.onSaveInstanceState(outState);
    }

    public class TracksAdapter extends ArrayAdapter<SpotifySearchResult> {

        private int RES_MISSING_IMAGE_ICON;

        private void assignResourceIds() {
            RES_MISSING_IMAGE_ICON = Resources.getSystem().getIdentifier("ic_dialog_alert",
                    "drawable", "android");
        }

        /**
         * This custom constructor drops the view argument of the superclass's constructor, since
         * it reflects a custom view and that argument would have been ignored.
         *
         * @param context Current context, used to inflate layout.
         * @param searchResults List of artists to be displayed.
         */
        public TracksAdapter(Context context, List<SpotifySearchResult> searchResults) {
            super(context, 0, searchResults);
            assignResourceIds();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.list_item_individual_track, parent, false);
            }

            SpotifySearchResult searchResult = getItem(position);

            if (searchResult != null) {
                ImageView imageViewAlbum =
                        (ImageView) convertView.findViewById(R.id.imageViewAlbum);
                TextView textViewTrack = (TextView) convertView.findViewById(R.id.textViewTrack);
                TextView textViewAlbum = (TextView) convertView.findViewById(R.id.textViewAlbum);

                if (imageViewAlbum != null && searchResult.imageUrl != null) {
                    Picasso.with(getContext()).load(searchResult.imageUrl).into(imageViewAlbum);
                } else if (imageViewAlbum != null) {
                    imageViewAlbum.setImageResource(RES_MISSING_IMAGE_ICON);
                }

                if (textViewTrack != null) {
                    textViewTrack.setText(searchResult.trackName);
                }

                if (textViewAlbum != null) {
                    textViewAlbum.setText(searchResult.albumName);
                }
            }

            return convertView;
        }
    }

    public class SpotifyTracksTask extends AsyncTask<String, Void, Tracks> {
        @Override
        protected Tracks doInBackground(String... strings) {
            if (strings.length == 0) {
                return null;
            }

            Log.i(LOG_TAG, "Getting tracks for " + strings[0]);

            // Spotify API requires the country (market) to be specified.
            Map<String, Object> spotifyOptions = new HashMap<>();
            spotifyOptions.put(SpotifyService.COUNTRY, "US");

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Tracks result;
            try {
                result = spotify.getArtistTopTrack(strings[0], spotifyOptions);
            } catch (RetrofitError e) {
                result = null;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            mTracksAdapter.clear();

            if (tracks != null) {
                for (Track track : tracks.tracks) {
                    mTracksAdapter.add(new SpotifySearchResult(track));
                }
            }

            // If no results are found, tell user.
            if (mTracksAdapter.isEmpty()) {
                Toast.makeText(getActivity(), R.string.warning_no_tracks_found,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
