package wiklosoft.mediaprovider.providers;

import android.content.Context;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.browse.MediaBrowser;
import android.media.browse.MediaBrowser.MediaItem;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.service.media.MediaBrowserService;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import wiklosoft.mediaprovider.MetadataReady;
import wiklosoft.mediaprovider.MusicReady;
import wiklosoft.mediaprovider.QueueReady;
import wiklosoft.mediaprovider.R;
import wiklosoft.mediaprovider.playlists.PlaylistDatabaseHandler;

/**
 * Created by Pawel Wiklowski on 07.10.15.
 */
public class PlaylistsProvider implements MusicProvider {
    private String mId = "playlists";
    private PlaylistDatabaseHandler mDb = null;

    public PlaylistsProvider(Context context){
        mDb = new PlaylistDatabaseHandler(context);
    }

    @Override
    public String getName() {
        return "Playlists";
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public void getChildren(String s, MediaBrowserService.Result<List<MediaItem>> result){
        List<MediaItem> list = new ArrayList<>();


        if (s.equals(mId)) {
            List<String> names = mDb.getPlaylistNames();
            for (String name : names) {
                list.add(new MediaBrowser.MediaItem(new MediaDescription.Builder()
                        .setMediaId(mId + "/" + name)
                        .setTitle(name)
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE));
            }
        }else {
            String playlistname = s.replace(mId + "/", "");

            list.addAll(mDb.getPlaylist(playlistname).getPlaylistItems());

        }
        result.sendResult(list);
    }




    @Override
    public void getMediaUrl(String id, MusicReady callback) {
        callback.ready(id.replace(mId, ""), null);
    }

    @Override
    public boolean getMetaData(String url, MetadataReady callback) {
        return false;
    }

    @Override
    public void getQueue(String mediaId, QueueReady callback) {

    }


    @Override
    public Fragment getSettingsFragment() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    public static class SettingsFragment extends Fragment {

        public SettingsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.test_settings, container, false);
            return rootView;
        }
    }
}
