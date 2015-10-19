package wiklosoft.mediaprovider.providers;

import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.service.media.MediaBrowserService;
import android.support.v4.app.Fragment;

import java.util.List;

import wiklosoft.mediaprovider.MetadataReady;
import wiklosoft.mediaprovider.MusicReady;
import wiklosoft.mediaprovider.QueueReady;

/**
 * Created by Pawel Wiklowski on 07.10.15.
 */
public interface MusicProvider {
    String getToken();
    void setToken(String token);
    String getName();
    String getId();
    void getChildren(String s, MediaBrowserService.Result<List<MediaBrowser.MediaItem>> result);
    void getMediaUrl(String id, MusicReady callback);
    boolean getMetaData(String id, MetadataReady callback);
    void getQueue(String mediaId, QueueReady callback);

    Fragment getSettingsFragment();
}
