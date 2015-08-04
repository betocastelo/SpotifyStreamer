package com.example.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Parcelable containing query results from spotify. Maintains the relevant fields from either
 * artist or track searches. An instance will contain _either_ an artist _or_ a track.
 */
public class SpotifySearchResult implements Parcelable {
    static final int TYPE_EMPTY = 0;
    static final int TYPE_ARTIST = 1;
    static final int TYPE_TRACK = 2;

    // Common
    int contentType = TYPE_EMPTY;
    String imageUrl = null;

    // From Artist
    String artistId = null;
    String artistName = null;

    // From Track
    String trackId = null;
    String trackName = null;
    String albumName = null;
    boolean isPlayable = false;
    String previewUrl = null;

    // Get the largest image back from album images (artist.images is ordered
    // from largest to smallest). Tried with the smallest image, but was not
    // happy with quality with the results. Since even the largest image is not
    // very large (typically 640x640), and since I might want to use tha larger
    // images later in the project (and Picasso will cache them), I don't think
    // this is a bad deal.
    //! todo get all image urls instead.
    public SpotifySearchResult(Artist artist) {
        if (artist != null) {
            contentType = TYPE_ARTIST;

            if (!artist.images.isEmpty())
                imageUrl = artist.images.get(0).url;

            artistId = artist.id;
            artistName = artist.name;
        }
    }

    public SpotifySearchResult(Track track) {
        if (track != null) {
            contentType = TYPE_TRACK;

            if (!track.album.images.isEmpty())
                imageUrl = track.album.images.get(0).url;

            trackId = track.id;
            trackName = track.name;
            albumName = track.album.name;

            artistName = track.artists.get(0).name;

            // apparently Track.is_playable may be null?!
            isPlayable = track.is_playable == null ? false : track.is_playable;
            previewUrl = track.preview_url;
        }
    }

    private SpotifySearchResult(Parcel parcel) {
        // Keeping fields in alphabetical order.
        albumName = parcel.readString();
        artistId = parcel.readString();
        artistName = parcel.readString();
        contentType = parcel.readInt();
        imageUrl = parcel.readString();
        isPlayable = parcel.readInt() != 0;
        previewUrl = parcel.readString();
        trackId = parcel.readString();
        trackName = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        // Fields in alphabetical order.
        parcel.writeString(albumName);
        parcel.writeString(artistId);
        parcel.writeString(artistName);
        parcel.writeInt(contentType);
        parcel.writeString(imageUrl);
        parcel.writeInt(isPlayable ? 1 : 0);
        parcel.writeString(previewUrl);
        parcel.writeString(trackId);
        parcel.writeString(trackName);
    }

    public static final Parcelable.Creator<SpotifySearchResult> CREATOR =
            new Parcelable.Creator<SpotifySearchResult>() {
                @Override
                public SpotifySearchResult createFromParcel(Parcel parcel) {
                    return new SpotifySearchResult(parcel);
                }

                @Override
                public SpotifySearchResult[] newArray(int i) {
                    return new SpotifySearchResult[i];
                }
            };
}
