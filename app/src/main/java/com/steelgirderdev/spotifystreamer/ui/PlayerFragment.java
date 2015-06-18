package com.steelgirderdev.spotifystreamer.ui;


import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.steelgirderdev.spotifystreamer.Constants;
import com.steelgirderdev.spotifystreamer.R;
import com.steelgirderdev.spotifystreamer.model.TopTracks;
import com.steelgirderdev.spotifystreamer.model.TrackUiUpdate;
import com.steelgirderdev.spotifystreamer.service.MediaPlayerService;

/**
 * A fragment which displays the Player
 */
public class PlayerFragment extends DialogFragment {

    private Activity mContext;
    private TopTracks topTracks;
    private Toast toast;

    private Handler mHandler = new Handler();
    SeekBar seekBarPlayer;
    ImageView imageViewPlayPause;
    private BroadcastReceiver receiver;
    private ImageView imageViewPrev;
    private ImageView imageViewNext;
    private View rootView;
    private LinearLayout pageView;
    private TextView textViewTrackName;
    private TextView textViewArtist;
    private TextView textViewAlbum;
    private ImageView ImageViewHighresImage;
    private TextView textViewCurrentpos;
    private TextView textViewTracklength;

    public PlayerFragment() {
        // Required empty public constructor
    }

    public void setContext(Activity mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(mContext).registerReceiver((receiver),
                new IntentFilter(Constants.BROADCAST_INTENT_TRACKUIUPDATE)
        );
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        // set the dimensions of the dialog if we are runing in dialog
        Dialog d = getDialog();
        if (d!=null){
            int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
            int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
            d.getWindow().setLayout(width, height);
        }
    }

    /** The system calls this only when creating the layout in a dialog.
     * http://developer.android.com/guide/topics/ui/dialogs.html */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.MY_DIALOG);
        return dialog;
    }

    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment http://developer.android.com/guide/topics/ui/dialogs.html
        rootView = inflater.inflate(R.layout.fragment_player, container, false);
        pageView = (LinearLayout) rootView.findViewById(R.id.pageView);
        textViewTrackName = (TextView) rootView.findViewById(R.id.player_textview_trackname);
        textViewArtist = (TextView) rootView.findViewById(R.id.player_textview_artist);
        textViewAlbum = (TextView) rootView.findViewById(R.id.player_textview_album);
        ImageViewHighresImage = (ImageView) rootView.findViewById(R.id.player_highresimage);
        textViewCurrentpos = (TextView) rootView.findViewById(R.id.player_textview_currentpos);
        textViewTracklength = (TextView) rootView.findViewById(R.id.player_textview_tracklength);
        seekBarPlayer = (SeekBar) rootView.findViewById(R.id.player_seekBar1);
        imageViewPrev = (ImageView) rootView.findViewById(R.id.player_prev);
        imageViewPlayPause = (ImageView) rootView.findViewById(R.id.player_playPause);
        imageViewNext = (ImageView) rootView.findViewById(R.id.player_next);


        // if we got called from an activity, mContext is not set yet, initialize it
        if(mContext == null) {
            mContext = getActivity();
        }

        // load the tracks if resumed
        if(savedInstanceState == null || !savedInstanceState.containsKey(Constants.PARCEL_KEY_TOPTRACKS)) {
            // read intent extras
            if(mContext.getIntent().hasExtra(Constants.EXTRA_TOP_TRACKS)) {
                topTracks = (TopTracks) mContext.getIntent().getExtras().get(Constants.EXTRA_TOP_TRACKS);
                Log.v(Constants.LOG_TAG, "Intent Extras: " + topTracks.toString());
                executeCommand();
            } else {
                Log.v(Constants.LOG_TAG,"NO EXTRA DEFINED WITH KEY "+Constants.EXTRA_TOP_TRACKS);
            }
        } else {
            // restore topTracks object
            topTracks = savedInstanceState.getParcelable(Constants.PARCEL_KEY_TOPTRACKS);
            Log.v(Constants.LOG_TAG, "Restore Intent Extras: " + topTracks.toString());
            executeCommand();
        }

        // receiver that updates the current track progress on the UI from the service.
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TrackUiUpdate uiUpdate = (TrackUiUpdate) intent.getExtras().get(Constants.PARCEL_KEY_TRACK_UI_UPDATE);
                //Log.v(Constants.LOG_TAG, "onReceive update " + uiUpdate.toString());
                seekBarPlayer.setProgress(uiUpdate.trackpos);
                if(topTracks.playerpos != uiUpdate.playerpos) {
                    // if the service sent that the current pos has changes, update the view
                    topTracks.playerpos = uiUpdate.playerpos;
                    setDataToUI(textViewTrackName, textViewArtist, textViewAlbum, ImageViewHighresImage);
                }
                if(uiUpdate.playing) {
                    imageViewPlayPause.setImageResource(R.drawable.ic_pause_white_24dp);
                } else {
                    imageViewPlayPause.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                }
            }
        };


        imageViewPlayPause.setImageResource(R.drawable.ic_pause_white_24dp);

        if(topTracks != null) {
            // set data to UI
            setDataToUI(textViewTrackName, textViewArtist, textViewAlbum, ImageViewHighresImage);
        }

        // The call to getDuration returns wrong milliseconds, 100's of hours for the 30 sec
        // mp3s. Must be file issue on Spotify side. Since all previews are 30s we can hardcode it
        // until something changes.
        //seekBarPlayer.setMax(act.getOrCreateMediaplayer().getDuration());
        seekBarPlayer.setMax(30000);
        textViewTracklength.setText(formatDuration(30000));

        seekBarPlayer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewCurrentpos.setText(formatDuration(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                playerServiceAction(Constants.ACTION_STOP_PROGRESS_UPDATE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playerServiceAction(Constants.ACTION_SEEK_TO, seekBar.getProgress());
            }
        });

        imageViewPrev.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                playerServiceAction(Constants.ACTION_PREVIOUS);
            }
        });

        imageViewPlayPause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                playerServiceAction(Constants.ACTION_PLAYPAUSETOGGLE);
            }
        });

        imageViewNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                playerServiceAction(Constants.ACTION_NEXT);
            }
        });


        return rootView;
    }

    public void executeCommand() {
        if(topTracks.command!=null) {

            switch (topTracks.command) {
                case (Constants.ACTION_NONE): {
                    // do nothing
                    break;
                }
                case (Constants.ACTION_PREVIOUS): {
                    playerServiceAction(Constants.ACTION_PREVIOUS);
                    break;
                }
                case (Constants.ACTION_PLAY): {
                    playerServiceAction(Constants.ACTION_PLAY);
                    break;
                }
                case (Constants.ACTION_PLAYPAUSETOGGLE): {
                    playerServiceAction(Constants.ACTION_PLAYPAUSETOGGLE);
                    break;
                }
                case (Constants.ACTION_NEXT): {
                    playerServiceAction(Constants.ACTION_NEXT);
                    break;
                }
                default: {
                    // do nothing
                    break;
                }
            }
        }
    }

    public void setTopTracks(TopTracks pTopTracks) {
        topTracks = pTopTracks;
    }

    private void setDataToUI(TextView textViewTrackName, TextView textViewArtist, TextView textViewAlbum, ImageView imageViewHighresImage) {
        textViewTrackName.setText(topTracks.getCurrentTrack().trackname);
        textViewArtist.setText(topTracks.artist.artistname);
        textViewAlbum.setText(topTracks.getCurrentTrack().albumname);

        showImage(mContext, imageViewHighresImage, topTracks.getCurrentTrack().urlHighres);
    }

    /**
     * Convenience Method without millis
     * @param action the action to send
     */
    private void playerServiceAction(String action) {
        playerServiceAction(action, null);
    }

    /**
     * Sends a command to the service and starts it if neccessary
     * @param action the action to send
     * @param seekToMillis the milliseconds to seek to if the command is seekTo
     */
    private void playerServiceAction(String action, Integer seekToMillis) {
        Intent serviceIntent = new Intent(mContext, MediaPlayerService.class);
        // in the case of seek, only send millis, and not the whole topTracks object
        if(action.equals(Constants.ACTION_SEEK_TO)) {
            serviceIntent.putExtra(Constants.EXTRA_SEEK_TO_MILLIS, seekToMillis);
        } else {
            serviceIntent.putExtra(Constants.EXTRA_TOP_TRACKS, topTracks);
        }
        serviceIntent.setAction(action);
        mContext.startService(serviceIntent);
    }




    @Override
    public void onSaveInstanceState(Bundle outState) {
        topTracks.command = Constants.ACTION_NONE;
        outState.putParcelable(Constants.PARCEL_KEY_TOPTRACKS, topTracks);
        super.onSaveInstanceState(outState);
    }

    private void showImage(Context context, ImageView imageView, String url) {
        Log.v(Constants.LOG_TAG, "Loading image:" + url);

        if(url != null) {
            Picasso.with(context)
                    .load(url)
                            //TODO: find a error and placeholder image to use .error(android.R.drawable.ic_menu_close_clear_cancel)
                    .resizeDimen(R.dimen.player_highresWH, R.dimen.player_highresWH)
                    .centerInside()
                    .into(imageView);
            imageView.setVisibility(View.VISIBLE);
        } else {
            //TODO: find a placeholder image to use imageView.setImageResource(android.R.drawable.ic_menu_help);
            imageView.setVisibility(View.INVISIBLE);
        }


    }

    /**
     * Source: http://stackoverflow.com/questions/9027317/how-to-convert-milliseconds-to-hhmmss-format
     * @param millis
     * @return
     */
    public static String formatDuration(final long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = millis / (1000 * 60 * 60);
        //Log.v(Constants.LOG_TAG, "Converting " + millis + "ms to " + hours + ":" + minutes + ":" + seconds);

        StringBuilder b = new StringBuilder();
        if(hours>0) {
            b.append(hours == 0 ? "00" : hours < 10 ? String.valueOf("0" + hours) :
                    String.valueOf(hours));
            b.append(":");
        }
        b.append(minutes == 0 ? "00" : minutes < 10 ? String.valueOf("0" + minutes) :
                String.valueOf(minutes));
        b.append(":");
        b.append(seconds == 0 ? "00" : seconds < 10 ? String.valueOf("0" + seconds) :
                String.valueOf(seconds));
        return b.toString();
    }


}
