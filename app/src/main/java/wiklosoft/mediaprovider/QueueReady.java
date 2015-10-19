package wiklosoft.mediaprovider;

import android.media.MediaMetadata;
import android.media.session.MediaSession;

import java.util.List;

/**
 * Created by Pawel Wiklowski on 10.10.15.
 */
public interface QueueReady {

    void ready(List<MediaSession.QueueItem> data);

}
