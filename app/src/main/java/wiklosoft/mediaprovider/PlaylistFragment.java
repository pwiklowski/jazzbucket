package wiklosoft.mediaprovider;

import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Pawel Wiklowski on 12.10.15.
 */
public class PlaylistFragment extends Fragment {
    MusicService mService = null;
    TouchInterceptor mListView = null;
    PlaylistAdapter mPlaylistAdapter = null;

    public void setMusicService(MusicService service){
        mService = service;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.playlist_fragment, container, false);
        mListView = (TouchInterceptor) rootView.findViewById(R.id.playlist_list);

        ((TouchInterceptor) mListView).setDropListener(mDropListener);
        ((TouchInterceptor) mListView).setRemoveListener(mRemoveListener);

        mPlaylistAdapter = new PlaylistAdapter(getActivity(), mService.getPlaylist());

        mListView.setAdapter(mPlaylistAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        return rootView;
    }
    private TouchInterceptor.DropListener mDropListener =
            new TouchInterceptor.DropListener() {
                public void drop(int from, int to) {
                    int a= 5;

                    List<MediaSession.QueueItem> playlist = mService.getPlaylist();
                    Collections.swap(playlist, from, to);
                    mPlaylistAdapter.notifyDataSetInvalidated();
                }
            };

    private TouchInterceptor.RemoveListener mRemoveListener =
            new TouchInterceptor.RemoveListener() {
                public void remove(int which) {
                    int a= 5;
                }
            };

}
