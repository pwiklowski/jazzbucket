package wiklosoft.mediaprovider;

import java.util.Map;

/**
 * Created by Pawel Wiklowski on 10.10.15.
 */
public interface MusicReady {

    void ready(String url, Map<String,String> headers);
}
