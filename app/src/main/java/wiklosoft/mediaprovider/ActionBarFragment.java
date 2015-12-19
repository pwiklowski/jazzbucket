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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import wiklosoft.mediaprovider.providers.TestMusicProvider;

/**
 * Created by Pawel Wiklowski on 12.10.15.
 */
public class ActionBarFragment extends Fragment {
    private final String TAG = "PlayerFragment";
    private ImageView mArt = null;
    private ImageView mOpenDrawer = null;
    private TextView mArtist = null;
    private TextView mTitle = null;
    private TextView mTime = null;
    private String mLength = null;
    private OnDrawerOpen mOnDrawerOpen = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.action_bar, container, false);

        mTitle = (TextView) rootView.findViewById(R.id.title);
        mArtist = (TextView) rootView.findViewById(R.id.artist);
        mTime = (TextView) rootView.findViewById(R.id.time);
        mArt = (ImageView) rootView.findViewById(R.id.art);
        mOpenDrawer = (ImageView) rootView.findViewById(R.id.openDrawer);
        mOpenDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnDrawerOpen !=null){
                    mOnDrawerOpen.open(true);
                }
            }
        });
        return rootView;
    }

    String formatTime(int sec){
        String minutes = Integer.toString(sec / 60);
        String seconds = String.format("%02d", sec % 60);

        return minutes +":"+ seconds;
    }

    public void onPlaybackStateChanged(@NonNull PlaybackState state) {
        int currentPosition = (int)state.getPosition()/1000;
        mTime.setText(formatTime(currentPosition) + "/" + mLength);
    }
    public void onMetadataChanged(MediaMetadata metadata) {
        String artist = metadata.getText(MediaMetadata.METADATA_KEY_ARTIST).toString();
        String title = metadata.getText(MediaMetadata.METADATA_KEY_TITLE).toString();

        long duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);

        mLength = formatTime((int) duration / 1000);
        if (mArt != null) mArt.setImageBitmap(metadata.getBitmap(MediaMetadata.METADATA_KEY_ART));

        mArtist.setText(artist);
        mTitle.setText(title);

    }

    public void setOnDrawerOpen(OnDrawerOpen onDrawerOpen){
        mOnDrawerOpen = onDrawerOpen;
    }

    interface OnDrawerOpen{
        void open(boolean open);
    }
}
