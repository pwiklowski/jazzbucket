package wiklosoft.mediaprovider.providers;

import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.service.media.MediaBrowserService;
import android.support.v4.app.Fragment;

import java.util.List;

import wiklosoft.mediaprovider.MusicReady;

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
    MediaMetadata getMetaData(String id);

    Fragment getSettingsFragment();
}
