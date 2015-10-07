package wiklosoft.mediaprovider;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.drm.DrmStore;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.Rating;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.media.MediaBrowserService;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import wiklosoft.mediaprovider.providers.MusicProvider;
import wiklosoft.mediaprovider.providers.TestMusicProvider;

/**
 * Created by Pawel Wiklowski on 07.10.15.
 */
public class MusicService extends MediaBrowserService{
    private String TAG = "MusicService";
    private List<MusicProvider> mMusicProviderList = new ArrayList<>();
    private String ROOT_ID = "/";

    private MediaSession mSession;
    private List<MediaSession.QueueItem> mPlayingQueue;

    private MediaPlayer mMediaPlayer;


    @Override
    public void onCreate(){
        super.onCreate();

        mMusicProviderList.add(new TestMusicProvider("test1"));
        mMusicProviderList.add(new TestMusicProvider("test2"));
        mMusicProviderList.add(new TestMusicProvider("test3"));


        mPlayingQueue = new ArrayList<>();

        // Start a new MediaSession
        mSession = new MediaSession(this, "MusicService");
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Context context = getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);

        Bundle extras = new Bundle();
        mSession.setExtras(extras);


        mMediaPlayer = new MediaPlayer();
    }
    private final class MediaSessionCallback extends MediaSession.Callback {
        @Override
        public void onPlay() {
            Log.d(TAG, "play");
            mMediaPlayer.start();
            updatePlaybackState(PlaybackState.STATE_PLAYING);
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            Log.d(TAG, "OnSkipToQueueItem:" + queueId);
        }

        @Override
        public void onSeekTo(long position) {
            Log.d(TAG, "onSeekTo:"+ position);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d(TAG, "playFromMediaId mediaId:"+ mediaId+ "  extras="+ extras);


            String url = getMediaUrl(mediaId);


            //String url = "http://192.168.1.8/ostr/01%20-%20O.S.T.R.%20-%20%20Prolog.mp3";

            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.prepare();
                mMediaPlayer.start();

                updatePlaybackState(PlaybackState.STATE_PLAYING);
                updateMetadata(mediaId);

            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        public void onPause() {
            Log.d(TAG, "pause. ");
            mMediaPlayer.pause();
            updatePlaybackState(PlaybackState.STATE_PAUSED);
        }

        @Override
        public void onStop() {
            Log.d(TAG, "stop.");
        }

        @Override
        public void onSkipToNext() {
            Log.d(TAG, "skipToNext");

        }

        @Override
        public void onSkipToPrevious() {
            Log.d(TAG, "skipToPrevious");
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            Log.d(TAG, "playFromSearch  query=" + query);

        }
    }
    private String getMediaUrl(String mediaId){
        String providerId = mediaId.split("/")[0];

        MusicProvider provider = getMusicProvider(providerId);

        return provider.getMediaUrl(mediaId);
    }

    private void updateMetadata(String id) {
        String providerId = id.split("/")[0];

        MusicProvider provider = getMusicProvider(providerId);


        mSession.setMetadata(provider.getMetaData(id));
    }

    private void updatePlaybackState(int state) {

        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(getAvailableActions());

        stateBuilder.setState(state,mMediaPlayer.getCurrentPosition(), 1.0f, SystemClock.elapsedRealtime());


        mSession.setPlaybackState(stateBuilder.build());

    }
    private long getAvailableActions() {
        long actions = PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackState.ACTION_PLAY_FROM_SEARCH;

        if (mPlayingQueue == null || mPlayingQueue.isEmpty()) {
            return actions;
        }
        if (mMediaPlayer.isPlaying()) {
            actions |= PlaybackState.ACTION_PAUSE;
        }
            actions |= PlaybackState.ACTION_SKIP_TO_PREVIOUS;
            actions |= PlaybackState.ACTION_SKIP_TO_NEXT;
        return actions;
    }
    @Override
    public BrowserRoot onGetRoot(String s, int i, Bundle bundle) {
        return new BrowserRoot(ROOT_ID, null);
    }

    private MusicProvider getMusicProvider(String id){
        for (MusicProvider provider: mMusicProviderList) {
            if (provider.getId().equals(id)) return provider;
        }
        return null;
    }

    @Override
    public void onLoadChildren(String s, Result<List<MediaBrowser.MediaItem>> result) {
        Log.d(TAG, "onLoadChildren" + s);
        List<MediaBrowser.MediaItem> mediaItems = new ArrayList<>();

        if (s.equals(ROOT_ID)){
            for (MusicProvider provider: mMusicProviderList) {
                mediaItems.add(new MediaBrowser.MediaItem(new MediaDescription.Builder()
                        .setMediaId(provider.getId())
                        .setTitle(provider.getName())
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE));
            }
        }else{
            MusicProvider p = getMusicProvider(s);
            mediaItems = p.getChildren(s);
        }
        result.sendResult(mediaItems);
    }
}
