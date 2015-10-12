package wiklosoft.mediaprovider;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.media.MediaBrowserService;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wiklosoft.mediaprovider.providers.MusicProvider;
import wiklosoft.mediaprovider.providers.TestMusicProvider;

/**
 * Created by Pawel Wiklowski on 07.10.15.
 */
public class MusicService extends MediaBrowserService{

    private static MusicService mMusicService; //I don't like it, it should be safe TODO: find better way to do it

    private String TAG = "MusicService";
    private List<MusicProvider> mMusicProviderList = new ArrayList<>();
    private String ROOT_ID = "/";

    private MediaSession mSession;
    private List<MediaSession.QueueItem> mPlayingQueue;

    private MediaPlayer mMediaPlayer;


    @Override
    public void onCreate(){
        Log.d(TAG, "onCreate");
        super.onCreate();

        mMusicProviderList.add(new TestMusicProvider("test1"));


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

        mMediaPlayer = new MediaPlayer();
        mMusicService = this;
    }



    static MusicService getService() {
            return mMusicService;
    }

    public List<MusicProvider> getMusicProviders(){
        return mMusicProviderList;
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
        public void onPlayFromMediaId(final String mediaId, Bundle extras) {
            Log.d(TAG, "playFromMediaId mediaId:"+ mediaId+ "  extras="+ extras);


            getMediaUrl(mediaId, new MusicReady() {
                @Override
                public void ready(String url, Map<String, String> headers) {
                    try {
                        String providerId = mediaId.split("/")[0];
                        MusicProvider provider = getMusicProvider(providerId);

                        try {
                            if (!provider.getMetaData(mediaId, mMetadataReady)) {
                                //try generic way
                                mSession.setMetadata(getMetaData(url, headers));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        mMediaPlayer.reset();

                        mMediaPlayer.setDataSource(mMusicService, Uri.parse(url), headers);
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();

                        updatePlaybackState(PlaybackState.STATE_PLAYING);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });





        }
        MetadataReady mMetadataReady = new MetadataReady() {
            @Override
            public void ready(MediaMetadata data) {
                mSession.setMetadata(data);
            }
        };


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
    private void getMediaUrl(String mediaId, MusicReady ready){
        String providerId = mediaId.split("/")[0];

        MusicProvider provider = getMusicProvider(providerId);

        provider.getMediaUrl(mediaId, ready);
    }


    public MediaMetadata getMetaData(String url, Map<String, String> headers) {

        if (headers == null){
            headers = new HashMap<String,String>();
        }

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(url, headers);

        MediaMetadata.Builder b = new MediaMetadata.Builder();
        b.putString(MediaMetadata.METADATA_KEY_ARTIST, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        b.putString(MediaMetadata.METADATA_KEY_TITLE, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        b.putLong(MediaMetadata.METADATA_KEY_DURATION, Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));

        mmr.release();
        return b.build();
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
            result.sendResult(mediaItems);
        }else{
            MusicProvider p = getMusicProvider(s.split("/")[0]);
            result.detach();
            p.getChildren(s, result);
        }
    }
}
