package wiklosoft.mediaprovider;

import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
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
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pawel Wiklowski on 12.10.15.
 */
public class MediaView extends Fragment{
    private final String TAG = "MediaView";
    ListView mListView = null;
    MainActivity mMainActivity = null;
    private String mMediaId = "/";
    private String mParentId;
    MediaViewAdapter mMediaViewAdapter = null;
    MediaBrowser mMediaBrowser = null;
    ProgressBar mProgressBar = null;
    private static final String ARG_MEDIA_ID = "media_id";
    private static final String GET_BACK_ID = "get_bacK_id";
    List<MediaBrowser.MediaItem> mListItems = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.media_view, container, false);
        mListView = (ListView) rootView.findViewById(R.id.mediaList);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mMediaViewAdapter = new MediaViewAdapter(getActivity(), new ArrayList<MediaBrowser.MediaItem>());
        mListView.setAdapter(mMediaViewAdapter);
        mListView.setOnItemClickListener(mOnItemClickListener);
        mListView.setOnItemLongClickListener(mOnItemLongClickListener);
        return rootView;
    }

    AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (mMediaBrowser != null){
                MediaBrowser.MediaItem item = mMediaViewAdapter.getItem(i);

                if (item.isBrowsable()) {

                    if (item.getMediaId() == GET_BACK_ID){
                        getActivity().getSupportFragmentManager().popBackStack();
                    }else {
                        MediaView mv = new MediaView();
                        mv.setMediaId(item.getMediaId());
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        transaction.replace(R.id.container, mv).addToBackStack("").commit();
                    }
                }else if (item.isPlayable()){
                    MediaSession.QueueItem queueItem= new MediaSession.QueueItem(item.getDescription(), 0);
                    Queue q = MusicService.getService().getQueue();

                    q.playItem(queueItem);
                }
            }
        }
    };

    AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (mMediaBrowser != null){
                MediaBrowser.MediaItem item = mMediaViewAdapter.getItem(i);
                if (item.isPlayable()) {
                    ItemMenuDialog id = new ItemMenuDialog();
                    id.setItem(item);
                    id.show(getFragmentManager(), "dialog");
                }
                return true;
            }
            return false;
        }
    };
    public String getMediaId() {
        return mMediaId;
    }

    public void setMediaId(String mediaId) {
        Bundle args = new Bundle(1);
        args.putString(ARG_MEDIA_ID, mediaId);
        setArguments(args);
    }

    private final MediaBrowser.SubscriptionCallback mSubscriptionCallback = new MediaBrowser.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowser.MediaItem> children) {
                try {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mListItems = children;

                    if (parentId != "/")
                        mListItems.add(0, new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                .setMediaId(GET_BACK_ID)
                                .setTitle("..")
                                .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE));

                    mMediaViewAdapter.clear();
                    mMediaViewAdapter.addAll(children);
                    mMediaViewAdapter.notifyDataSetChanged();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            public void onError(@NonNull String id) {
                Log.e(TAG, "browse fragment subscription onError, id=" + id);
            }
        };


    private final MediaController.Callback mMediaControllerCallback = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            if (metadata == null) {
                return;
            }
            Log.d(TAG, "Received metadata change to media " + metadata.getDescription().getMediaId());
        }

        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackState state) {
            super.onPlaybackStateChanged(state);
            Log.d(TAG, "Received state change: " + state);
        }
    };
    @Override
    public void onStart() {
        super.onStart();
        mMainActivity = (MainActivity) getActivity();

        // fetch browsing information to fill the listview:
        MediaBrowser mediaBrowser = mMainActivity.getMediaBrowser();
        Bundle args = getArguments();
        if (args != null) {
            mMediaId =  args.getString(ARG_MEDIA_ID);
        }
        Log.d(TAG, "fragment.onStart, mediaId=");

        if (mediaBrowser.isConnected()) {
            onConnected();
        }

    }
    @Override
    public void onStop() {
        super.onStop();
        MediaBrowser mediaBrowser = mMainActivity.getMediaBrowser();
        if (mediaBrowser != null && mediaBrowser.isConnected() && mMediaId != null) {
            mediaBrowser.unsubscribe(mMediaId);
        }
        if (getActivity().getMediaController() != null) {
            getActivity().getMediaController().unregisterCallback(mMediaControllerCallback);
        }
    }

    public void onConnected() {
        if (isDetached()) {
            return;
        }
        mMediaBrowser = mMainActivity.getMediaBrowser();
        mMediaId = getMediaId();
        if (mMediaId == null) {
            mMediaId = mMainActivity.getMediaBrowser().getRoot();
        }


        // Unsubscribing before subscribing is required if this mediaId already has a subscriber
        // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
        // the callback, but won't trigger the initial callback.onChildrenLoaded.
        //
        // This is temporary: A bug is being fixed that will make subscribe
        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
        // subscriber or not. Currently this only happens if the mediaID has no previous
        // subscriber or if the media content changes on the service side, so we need to
        // unsubscribe first.
        mMediaBrowser.unsubscribe(mMediaId);
        mMediaBrowser.subscribe(mMediaId, mSubscriptionCallback);

        // Add MediaController callback so we can redraw the list when metadata changes:
        if (getActivity().getMediaController() != null) {
            getActivity().getMediaController().registerCallback(mMediaControllerCallback);
        }
    }

}
