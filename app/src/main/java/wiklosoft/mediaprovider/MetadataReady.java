package wiklosoft.mediaprovider;

import android.media.MediaMetadata;

import java.util.Map;

/**
 * Created by Pawel Wiklowski on 10.10.15.
 */
public interface MetadataReady {

    void ready(MediaMetadata data);

}
