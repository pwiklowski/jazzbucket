package wiklosoft.mediaprovider;

import android.media.session.MediaSession;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Pawel Wiklowski on 12.10.15.
 */
public class PlaylistFragment extends Fragment {
    MusicService mService = null;
    TouchInterceptor mListView = null;
    PlaylistAdapter mPlaylistAdapter = null;
    TextView mEmptyQueueText = null;

    public void setMusicService(MusicService service){
        mService = service;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.playlist_fragment, container, false);
        mListView = (TouchInterceptor) rootView.findViewById(R.id.playlist_list);
        mListView.setDropListener(mDropListener);
        mListView.setRemoveListener(mRemoveListener);

        mPlaylistAdapter = new PlaylistAdapter(getActivity(), mService.getQueue().getItems());

        mListView.setAdapter(mPlaylistAdapter);
        mListView.setDivider(null);
        mListView.setTrashcan(rootView.findViewById(R.id.trash_view));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mService.getQueue().playItem(i);
                mPlaylistAdapter.notifyDataSetInvalidated();
            }
        });
        mEmptyQueueText = (TextView) rootView.findViewById(R.id.empty_queue_notifier);

        if (mService.getQueue().getItems().size() > 0){
            mEmptyQueueText.setVisibility(View.GONE);

        }

        return rootView;
    }
    private TouchInterceptor.DropListener mDropListener =
            new TouchInterceptor.DropListener() {
                public void drop(int from, int to) {
                    if (from != to && to < mService.getQueue().getItems().size()){
                        mService.getQueue().move(from, to);
                        mPlaylistAdapter.notifyDataSetInvalidated();
                    }
                }
            };

    private TouchInterceptor.RemoveListener mRemoveListener =
            new TouchInterceptor.RemoveListener() {
                public void remove(int which) {
                    mService.getQueue().remove(which);
                    if (mService.getQueue().getItems().size() > 0){
                        mEmptyQueueText.setVisibility(View.GONE);
                    }else{
                        mEmptyQueueText.setVisibility(View.VISIBLE);
                    }

                }
            };

}
