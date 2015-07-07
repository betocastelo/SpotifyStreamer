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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Displays the top 10 tracks for the given artist id (passed by Intent.EXTRA_TEXT).
 */
public class TopTracksActivityFragment extends Fragment {

    private static final String LOG_TAG = TopTracksActivityFragment.class.getSimpleName();

    private TracksAdapter mTracksAdapter;

    /**
     * Calls top 10 tracks retrive task from spotify web api.
     * @param artistId Spotify unique artist id.
     */
    private void retrieveTopTracks(String artistId) {
        SpotifyTracksTask spotifyTracksTask = new SpotifyTracksTask();
        spotifyTracksTask.execute(artistId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "In onCreate");

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        // Initialize empty tracks list so we can start up.
        Tracks dummyTracks = new Tracks();
        dummyTracks.tracks = new ArrayList<>();

        mTracksAdapter =
                new TracksAdapter(getActivity(),
                        R.layout.list_item_individual_track,
                        dummyTracks.tracks);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String artistId = intent.getStringExtra(Intent.EXTRA_TEXT);
            retrieveTopTracks(artistId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "In onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_top_tracks);
        listView.setAdapter(mTracksAdapter);

        return rootView;
    }

    public class TracksAdapter extends ArrayAdapter<Track> {

        private int RES_MISSING_IMAGE_ICON;

        private void assignResourceIds() {
            RES_MISSING_IMAGE_ICON = Resources.getSystem().getIdentifier("ic_dialog_alert",
                    "drawable", "android");
        }

        public TracksAdapter(Context context, int resource, List<Track> objects) {
            super(context, resource, objects);
            assignResourceIds();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.list_item_individual_track, null);
            }

            Track track = getItem(position);

            if (track != null) {
                ImageView imageViewAlbum =
                        (ImageView) convertView.findViewById(R.id.imageViewAlbum);
                TextView textViewTrack = (TextView) convertView.findViewById(R.id.textViewTrack);
                TextView textViewAlbum = (TextView) convertView.findViewById(R.id.textViewAlbum);

                if (imageViewAlbum != null && !track.album.images.isEmpty()) {
                    Picasso.with(getContext()).load(track.album.images.get(0).url)
                            .into(imageViewAlbum);
                } else if (imageViewAlbum != null) {
                    imageViewAlbum.setImageResource(RES_MISSING_IMAGE_ICON);
                }

                if (textViewTrack != null) {
                    textViewTrack.setText(track.name);
                }

                if (textViewAlbum != null) {
                    textViewAlbum.setText(track.album.name);
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
            return spotify.getArtistTopTrack(strings[0], spotifyOptions);
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            mTracksAdapter.clear();
            for (Track track : tracks.tracks) {
                mTracksAdapter.add(track);
            }
        }
    }
}
