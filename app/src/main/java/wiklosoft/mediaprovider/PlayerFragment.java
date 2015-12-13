package wiklosoft.mediaprovider;

import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Pawel Wiklowski on 12.10.15.
 */
public class PlayerFragment extends Fragment {
    private final String TAG = "PlayerFragment";
    private ImageButton mPlay = null;
    private ImageView mArt = null;
    private OnPlaylistShow mOnPlaylistShow = null;
    private ImageButton mShowPlaylist = null;
    private SeekBar mProgress = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.player_view, container, false);

        mPlay = (ImageButton) rootView.findViewById(R.id.play);
        mArt = (ImageView) rootView.findViewById(R.id.art);
        mProgress = (SeekBar) rootView.findViewById(R.id.progress);

        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                long progress = mProgress.getProgress()*1000;
                MediaController controller = getActivity().getMediaController();
                controller.getTransportControls().seekTo(progress);
            }
        });
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaController controller = getActivity().getMediaController();

                PlaybackState state = controller.getPlaybackState();
                if (state != null) {
                    if (state.getState() == PlaybackState.STATE_PAUSED)
                        getActivity().getMediaController().getTransportControls().play();
                    else if (state.getState() == PlaybackState.STATE_PLAYING)
                        getActivity().getMediaController().getTransportControls().pause();
                }
            }
        });

        mShowPlaylist = (ImageButton) rootView.findViewById(R.id.show_playlist);
        mShowPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnPlaylistShow != null)
                    mOnPlaylistShow.onShow(true);
            }
        });


        return rootView;
    }

    public void onPlaybackStateChanged(@NonNull PlaybackState state) {
        Log.d(TAG, "mediaControllerCallback.onPlaybackStateChanged: " + state.getState());
        if(state.getState() == PlaybackState.STATE_PAUSED)
            mPlay.setImageResource(R.drawable.play);

        if(state.getState() == PlaybackState.STATE_PLAYING)
            mPlay.setImageResource(R.drawable.pause);


        mProgress.setProgress((int)(state.getPosition()/1000));


        //if(state.getState() == PlaybackState.STATE_BUFFERING)
        //    setTitle("Buffering...");
        //if(state.getState() == PlaybackState.STATE_CONNECTING)
        //    setTitle("Connecting...");

    }



    private void setTitle(String title){
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView yourTextView = (TextView) getActivity().findViewById(titleId);
        yourTextView.setText(title);
    }

    public void onMetadataChanged(MediaMetadata metadata) {
        String artist = metadata.getText(MediaMetadata.METADATA_KEY_ARTIST).toString();
        String title = metadata.getText(MediaMetadata.METADATA_KEY_TITLE).toString();

        long duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);

        mProgress.setMax((int)duration/1000);


        if (mArt != null) mArt.setImageBitmap(metadata.getBitmap(MediaMetadata.METADATA_KEY_ART));

        setTitle(artist + " " + title);
    }

    public void setOnPlaylistShowListener(OnPlaylistShow listener){
        mOnPlaylistShow = listener;
    }

    public interface OnPlaylistShow {
        void onShow(boolean show);
    }

}
