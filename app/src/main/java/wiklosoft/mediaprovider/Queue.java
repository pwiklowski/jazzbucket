package wiklosoft.mediaprovider;

import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;

import java.util.ArrayList;
import java.util.List;

import wiklosoft.mediaprovider.playlists.Playlist;
import wiklosoft.mediaprovider.playlists.PlaylistDatabaseHandler;

/**
 * Created by Pawel Wiklowski on 29.11.15.
 */
public class Queue {
    private ArrayList<MediaSession.QueueItem> mQueueItems = new ArrayList<>();
    private MediaController mMediaController = null;
    private MediaSession.QueueItem mCurrentItem = null;
    private MusicService mMusicService;
    private PlaylistDatabaseHandler mDb = null;

    Queue(MusicService service, MediaController mediaController){
        mMediaController = mediaController;
        mMusicService = service;

        mDb = new PlaylistDatabaseHandler(service);
        List<MediaBrowser.MediaItem> items = mDb.getPlaylist(PlaylistDatabaseHandler.QUEUE_PLAYLIST_ID).getPlaylistItems();

        for(MediaBrowser.MediaItem item: items){
           mQueueItems.add(new MediaSession.QueueItem(item.getDescription(),0));
        }
    }

    void playItem(MediaSession.QueueItem item){
        if (mQueueItems.indexOf(item) != -1)
            mCurrentItem = item;
        mMediaController.getTransportControls().playFromMediaId(item.getDescription().getMediaId().replace("''", "'"), null);
    }

    void playItem(int id){
        mCurrentItem = mQueueItems.get(id);
        mMediaController.getTransportControls().playFromMediaId(mCurrentItem.getDescription().getMediaId().replace("''","'"), null);
    }

    void remove(MediaSession.QueueItem item){
        mQueueItems.remove(item);
    }

    void move(int from, int to){
        MediaSession.QueueItem item = get(from);
        remove(from);
        add(to, item);

    }

    void remove(int item){
        mDb.removeItemFromPlaylist(PlaylistDatabaseHandler.QUEUE_PLAYLIST_ID, mQueueItems.get(item).getDescription().getMediaId());
        mQueueItems.remove(item);
    }

    MediaSession.QueueItem getCurrentItem(){
        return mCurrentItem;
    }

    int getCurrentItemId(){
        return mQueueItems.indexOf(mCurrentItem);
    }


    void playNext(){
        int currentIndex = mQueueItems.indexOf(mCurrentItem);
        if ((mQueueItems.size() -1) > currentIndex){
            playItem(mQueueItems.get(currentIndex+1));
        }else{
            playItem(mQueueItems.get(0));
        }

    }
    void playPrevious(){

    }

    MediaSession.QueueItem get(int i){
        return mQueueItems.get(i);
    }
    void add(int i , MediaSession.QueueItem item){
        mQueueItems.add(i, item);
        mDb.addItemToPlaylist(i, PlaylistDatabaseHandler.QUEUE_PLAYLIST_ID, item.getDescription().getTitle().toString(), item.getDescription().getMediaId());
    }

    void add(MediaSession.QueueItem item){
        mQueueItems.add(item);
        mDb.addItemToPlaylist(PlaylistDatabaseHandler.QUEUE_PLAYLIST_ID, item.getDescription().getTitle().toString(), item.getDescription().getMediaId());
    }
    final List<MediaSession.QueueItem> getItems(){
        return mQueueItems;
    }

    boolean isEmpty(){
        return mQueueItems.isEmpty();
    }



}
