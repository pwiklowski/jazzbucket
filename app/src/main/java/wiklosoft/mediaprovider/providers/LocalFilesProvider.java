package wiklosoft.mediaprovider.providers;

import android.content.Context;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.browse.MediaBrowser.MediaItem;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.Environment;
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

/**
 * Created by Pawel Wiklowski on 07.10.15.
 */
public class LocalFilesProvider implements MusicProvider {
    private String mId = "local_files";
    private String MUSIC_DIR = "/sdcard/Music/";


    HashMap<String, String> files = new HashMap<>();


    public LocalFilesProvider(Context id){


    }

    @Override
    public String getName() {
        return "Local Music";
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public int getIcon() {
        return 0;
    }

    @Override
    public void getChildren(String s, MediaBrowserService.Result<List<MediaItem>> result){
        List<MediaItem> list = new ArrayList<>();
        File f;
        if (s.equals(mId))
            f = new File(MUSIC_DIR);
        else
            f = new File(s.replace(mId, ""));

        File[] files = f.listFiles();
        Arrays.sort(files);
        for(int i=0; i<files.length;i++){
            File item = files[i];

            list.add(new MediaItem(new MediaDescription.Builder()
                    .setMediaId(mId + item.getAbsolutePath())
                    .setTitle(item.getName())
                    .build(), item.isDirectory() ? MediaItem.FLAG_BROWSABLE : MediaItem.FLAG_PLAYABLE));
        }

        result.sendResult(list);
    }

    @Override
    public void getMediaUrl(String id, MusicReady callback) {
        callback.ready(id.replace(mId, ""), null);
    }

    @Override
    public boolean getMetaData(String url, MetadataReady callback) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(url.replace(mId, ""));

        MediaMetadata.Builder b = new MediaMetadata.Builder();
        b.putString(MediaMetadata.METADATA_KEY_ARTIST, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        b.putString(MediaMetadata.METADATA_KEY_TITLE, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        b.putLong(MediaMetadata.METADATA_KEY_DURATION, Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));

        mmr.release();
        callback.ready(b.build());
        return true;
    }

    @Override
    public  void getQueue(String mediaId, QueueReady callback) {
        List<MediaSession.QueueItem> list = new ArrayList<>();


        File f = new File(new File(mediaId.replace(mId, "")).getParent());

        File[] files = f.listFiles();
        Arrays.sort(files);
        for(int i=0; i<files.length;i++){
            File item = files[i];

            if (!item.isDirectory()) {
                list.add(new MediaSession.QueueItem(new MediaDescription.Builder()
                        .setMediaId(mId + item.getAbsolutePath())
                        .setTitle(item.getName())
                        .build(), MediaItem.FLAG_PLAYABLE));
            }
        }

        callback.ready(list);
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
