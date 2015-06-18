package com.steelgirderdev.spotifystreamer;

/**
 * Created by teisentraeger on 5/31/2015.
 * Holds Application Constants
 */
public class Constants {
    public static final String LOG_TAG = "SpotifyStreamer";
    public static final String EXTRA_TOP_TRACKS = "TopTracks";
    public static final String EXTRA_ARTIST = "Artist";
    public static final String EXTRA_SEEK_TO_MILLIS = "SeekToMs";
    public static final String EXTRA_PLAYER_COMMAND = "PlayerCommand";
    public static final String STATE_ARTIST_NAME = "artistName";
    public static final String PARCEL_KEY_TOPTRACKS_LIST = "toptracksList";
    public static final String PARCEL_KEY_TOPTRACKS = "toptracks";
    public static final String PARCEL_KEY_ARTISTS = "artists";
    public static final String PARCEL_KEY_TRACK_UI_UPDATE = "trackuiupdate";
    public static final String SPOTIFY_API_ARTIST_SEARCH_LIMIT_PARAMNAME = "limit";
    public static final String SPOTIFY_API_ARTIST_SEARCH_LIMIT = "50";
    public static final String SPOTIFY_API_TOPTRACKS_SEARCH_COUNTRY_PARAMNAME = "country";
    public static final String PREFERENCE_KEY_COUNTRY = "country";
    public static final String PREFERENCE_KEY_SHOW_NOTIFICATIONS = "show_notification_controls";
    public static final String WIFI_LOCK_KEY = "wifiLock";
    public static final int NOTIFICATION_ID_PLAYER = 1;

    public static final String BROADCAST_INTENT_TRACKUIUPDATE = "BroadCastTRACKUIUPDATE";
    public static final String BROADCAST_INTENT_TRACKSTARTED = "BroadCastTRACKSTARTED";
    public static final String BROADCAST_INTENT_STOPPED = "BroadCastSTOPPED";

    //actions
    public static final String ACTION_PLAY_IF_NOT_PLAYING = "com.steelgirderdev.spotifystreamer.service.action.PLAYIFNOTPLAYING";
    public static final String ACTION_PLAYPAUSETOGGLE = "com.steelgirderdev.spotifystreamer.service.action.PLAYPAUSETOGGLE";
    public static final String ACTION_PLAY = "com.steelgirderdev.spotifystreamer.service.action.PLAY";
    public static final String ACTION_PREVIOUS = "com.steelgirderdev.spotifystreamer.service.action.PREVIOUS";
    public static final String ACTION_NEXT = "com.steelgirderdev.spotifystreamer.service.action.NEXT";
    public static final String ACTION_PREVIOUS_FROM_NOTIFICATION = "com.steelgirderdev.spotifystreamer.service.action.PREVIOUS_FROM_NOTIFICATION";
    public static final String ACTION_NEXT_FROM_NOTIFICATION = "com.steelgirderdev.spotifystreamer.service.action.NEXT_FROM_NOTIFICATION";
    public static final String ACTION_NONE = "com.steelgirderdev.spotifystreamer.service.action.NONE";
    public static final String ACTION_STOP = "com.steelgirderdev.spotifystreamer.service.action.STOP";
    public static final String ACTION_STOP_PROGRESS_UPDATE = "com.steelgirderdev.spotifystreamer.service.action.STOP_PROGRESS_UPDATE";
    public static final String ACTION_SEEK_TO = "com.steelgirderdev.spotifystreamer.service.action.SEEK_TO";
}
