package wiklosoft.mediaprovider.providers;

import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.service.media.MediaBrowserService;

import java.util.List;

/**
 * Created by Pawel Wiklowski on 07.10.15.
 */
public interface MusicProvider {

    String getName();
    String getId();
    List<MediaBrowser.MediaItem> getChildren(String s);
    String getMediaUrl(String id);
    MediaMetadata getMetaData(String id);
}
