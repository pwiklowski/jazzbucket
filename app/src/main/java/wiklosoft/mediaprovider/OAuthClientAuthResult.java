package wiklosoft.mediaprovider;

import com.google.gson.JsonObject;

/**
 * Created by Pawel Wiklowski on 11.11.15.
 */
public interface OAuthClientAuthResult {

    void onAuthorize(String token, String refreshToken, int valid);
}
