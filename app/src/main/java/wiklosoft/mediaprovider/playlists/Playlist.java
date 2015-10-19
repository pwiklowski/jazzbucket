package wiklosoft.mediaprovider.playlists;

import android.media.MediaDescription;
import android.media.browse.MediaBrowser;
import android.media.browse.MediaBrowser.MediaItem;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pawel Wiklowski on 15.10.15.
 */
public class Playlist {
    private List<MediaItem> mPlaylist = new ArrayList<>();
    private String mName;

    public Playlist(String name){
        mName = name;
    }

    public List<MediaItem> getPlaylistItems() {
        return mPlaylist;
    }

    public void setPlaylistItems(List<MediaItem> playlist) {
        mPlaylist = playlist;
    }


    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public void addItem(String mediaId, String title){
        int id = 0;
        Bundle extra = new Bundle();
        extra.putInt(PlaylistDatabaseHandler.ITEM_ID, id);

        MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                .setMediaId(mediaId)
                .setTitle(title)
                .setExtras(new Bundle())
                .build(), MediaBrowser.MediaItem.FLAG_PLAYABLE);

        mPlaylist.add(item);

    }

    public void addItem(MediaItem item){
        mPlaylist.add(item);
    }
}