package wiklosoft.mediaprovider;

import android.media.session.MediaController;
import android.media.session.MediaSession;

import java.util.ArrayList;

/**
 * Created by Pawel Wiklowski on 29.11.15.
 */
public class Queue extends ArrayList<MediaSession.QueueItem> {
    private MediaController mMediaController = null;
    private MediaSession.QueueItem mCurrentItem = null;
    private MusicService mMusicService;

    Queue(MusicService service, MediaController mediaController){
        mMediaController = mediaController;
        mMusicService = service;
    }

    void playItem(MediaSession.QueueItem item){
        if (indexOf(item) != -1)
            mCurrentItem = item;
        mMediaController.getTransportControls().playFromMediaId(item.getDescription().getMediaId(), null);
    }

    void playItem(int id){

        mCurrentItem = get(id);
        mMediaController.getTransportControls().playFromMediaId(mCurrentItem.getDescription().getMediaId(), null);
    }

    MediaSession.QueueItem getCurrentItem(){
        return mCurrentItem;
    }

    int getCurrentItemId(){
        return indexOf(mCurrentItem);
    }


    void playNext(){

    }
    void playPrevious(){

    }

}
