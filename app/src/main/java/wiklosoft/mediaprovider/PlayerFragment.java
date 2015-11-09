package wiklosoft.mediaprovider;

import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pawel Wiklowski on 12.10.15.
 */
public class PlayerFragment extends Fragment {
    private final String TAG = "PlayerFragment";
    private TextView mTitle = null;
    private TextView mArtist = null;
    private Button mPlay = null;
    private TextView status = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.player_view, container, false);

        mTitle = (TextView) rootView.findViewById(R.id.title);
        mArtist = (TextView) rootView.findViewById(R.id.artist);
        status = (TextView) rootView.findViewById(R.id.status);
        mPlay = (Button) rootView.findViewById(R.id.play);

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


        return rootView;
    }

    public void onPlaybackStateChanged(@NonNull PlaybackState state) {
        Log.d(TAG, "mediaControllerCallback.onPlaybackStateChanged: " + state.getState());
        if(state.getState() == PlaybackState.STATE_PAUSED)
            status.setText("PAUSED");

        if(state.getState() == PlaybackState.STATE_PLAYING)
            status.setText("PLAYING");

        if(state.getState() == PlaybackState.STATE_BUFFERING)
            status.setText("BUFFERING");
        if(state.getState() == PlaybackState.STATE_CONNECTING){

            status.setText("CONNECTING");
            mArtist.setText("");
            mTitle.setText("Connecting...");

        }
    }

    public void onMetadataChanged(MediaMetadata metadata) {
        if (mArtist != null) mArtist.setText(metadata.getText(MediaMetadata.METADATA_KEY_ARTIST));
        if (mTitle != null) mTitle.setText(metadata.getText(MediaMetadata.METADATA_KEY_TITLE));
    }

}
