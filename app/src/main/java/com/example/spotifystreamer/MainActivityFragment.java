package com.example.spotifystreamer;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    ArtistAdapter mArtistAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Stop the keyboard from appearing on startup.
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize an empty pager so that onCreateView can start up.
        ArtistsPager dummyPager = new ArtistsPager();
        dummyPager.artists = new Pager<>();
        dummyPager.artists.items = new ArrayList<>();

        mArtistAdapter =
                new ArtistAdapter(getActivity(),
                        R.layout.list_item_individual_artist,
                        dummyPager.artists.items);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_artists);
        listView.setAdapter(mArtistAdapter);

        // This bit comes from http://stackoverflow.com/questions/8063439/android-edittext-finished-typing-event
        EditText artistQuery = (EditText) rootView.findViewById(R.id.editText_search);
        artistQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // The user is done typing.
                    searchArtist(textView.getText().toString());
                }

                return false;
            }
        });

        return rootView;
    }

    /**
     * Calls search task with query given.
     * @param artist Query.
     */
    private void searchArtist(String artist) {
        SpotifySearchTask spotifySearchTask = new SpotifySearchTask();
        spotifySearchTask.execute(artist);
    }

    public class ArtistAdapter extends ArrayAdapter<Artist> {

        public ArtistAdapter(Context context, int resource) {
            super(context, resource);
        }

        public ArtistAdapter(Context context, int resource, List<Artist> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(R.layout.list_item_individual_artist, null);
            }

            Artist artist = getItem(position);

            if (artist != null) {
                ImageView imageView = (ImageView) view.findViewById(R.id.imageViewArtist);
                TextView textView = (TextView) view.findViewById(R.id.textViewArtist);

                if (imageView != null && !artist.images.isEmpty()) {
                    Picasso.with(getContext()).load(artist.images.get(0).url).into(imageView);
                }

                if (textView != null) {
                    textView.setText(artist.name);
                }
            }

            return view;
        }
    }

    public class SpotifySearchTask extends AsyncTask<String, Void, ArtistsPager> {

        @Override
        protected ArtistsPager doInBackground(String... strings) {
            if (strings.length == 0) {
                return null;
            }

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(strings[0]);
            return results;
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            int numOfResults = artistsPager.artists.items.size();
            Log.i(LOG_TAG, "onPostExectute; found " + numOfResults + " results.");
            List<Artist> artists = artistsPager.artists.items;
            for (int i=0; i<numOfResults; i++) {
                Artist artist = artists.get(i);
                Log.i(LOG_TAG, i + " " + artist.name);
            }

            mArtistAdapter.clear();
            for (Artist artist : artistsPager.artists.items) {
                mArtistAdapter.add(artist);
            }
        }
    }
}
