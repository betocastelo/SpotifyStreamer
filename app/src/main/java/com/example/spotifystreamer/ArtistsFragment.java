package com.example.spotifystreamer;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

public class ArtistsFragment extends Fragment {

    private static final String KEY_SELECTED = "selected_position";

    private ArtistAdapter mArtistAdapter;
    private ArrayList<SpotifySearchResult> mSearchResults = new ArrayList<>();

    private ListView mListView;
    private int mListPosition = ListView.INVALID_POSITION;

    /**
     * Calls search task with query given.
     * @param artist Query.
     */
    private void searchArtist(String artist) {
        SpotifySearchTask spotifySearchTask = new SpotifySearchTask();
        spotifySearchTask.execute(artist);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_SELECTED)) {
                mListPosition = savedInstanceState.getInt(KEY_SELECTED);
            }

            if (savedInstanceState.containsKey(Utility.KEY_SEARCH_RESULTS)) {
                mSearchResults =
                        savedInstanceState.getParcelableArrayList(Utility.KEY_SEARCH_RESULTS);

                // If we have search results, I don't think we need the keyboard after a rotation.
                getActivity().getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
        }

        mArtistAdapter = new ArtistAdapter(getActivity(), mSearchResults);

        mListView = (ListView) rootView.findViewById(R.id.listview_artists);

        // This is the only way I could find to know when the listview is done being populated.
        // From http://stackoverflow.com/questions/29173588/
        // how-do-i-check-when-my-listview-has-finished-redrawing
        mListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5,
                                       int i6, int i7) {
                mListView.removeOnLayoutChangeListener(this);

                if (mListPosition != ListView.INVALID_POSITION) {
                    mListView.smoothScrollToPosition(mListPosition);
                }
            }
        });

        mListView.setAdapter(mArtistAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SpotifySearchResult artist = mArtistAdapter.getItem(position);
                ((Callback) getActivity()).onArtistSelected(artist.artistId);
                mListPosition = position;
            }
        });

        EditText artistQuery = (EditText) rootView.findViewById(R.id.editText_search);
        artistQuery.setImeOptions(EditorInfo.IME_ACTION_DONE); // otherwise we get NEXT in some cases...
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(Utility.KEY_SEARCH_RESULTS, mSearchResults);

        if (mListPosition != ListView.INVALID_POSITION) {
            outState.putInt(KEY_SELECTED, mListPosition);
        }

        super.onSaveInstanceState(outState);
    }

    public class ArtistAdapter extends ArrayAdapter<SpotifySearchResult> {

        /**
         * This custom constructor drops the view argument of the superclass's constructor, since
         * it reflects a custom view and that argument would have been ignored.
         *
         * @param context Current context, used to inflate layout.
         * @param searchResults List of artists to be displayed.
         */
        public ArtistAdapter(Context context, List<SpotifySearchResult> searchResults) {
            super(context, 0, searchResults);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView =
                        inflater.inflate(R.layout.list_item_individual_artist, parent, false);
            }

            SpotifySearchResult searchResult = getItem(position);

            if (searchResult != null) {
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageViewArtist);
                TextView textView = (TextView) convertView.findViewById(R.id.textViewArtist);

                if (imageView != null && searchResult.imageUrl != null) {
                    Picasso.with(getContext()).load(searchResult.imageUrl).into(imageView);
                } else if (imageView != null) {
                    imageView.setImageResource(
                            Utility.getDrawableResourceId(Utility.DRAWABLE_MISSING_IMAGE_ICON));
                }

                if (textView != null) {
                    textView.setText(searchResult.artistName);
                }
            }

            return convertView;
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

            ArtistsPager results;
            try {
                results = spotify.searchArtists(strings[0]);
            } catch (RetrofitError e) {
                results = null;
            }

            return results;
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            mArtistAdapter.clear();

            if (artistsPager != null) {
                for (Artist artist : artistsPager.artists.items) {
                    mArtistAdapter.add(new SpotifySearchResult(artist));
                }
            }

            mArtistAdapter.notifyDataSetChanged();

            // If no results are found, tell user.
            if (mArtistAdapter.isEmpty()) {
                Toast.makeText(getActivity(), R.string.warning_no_artists_found,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Allows parent activity to be notified of artist selections.
     */
    public interface Callback {
        void onArtistSelected(String artistId);
    }
}
