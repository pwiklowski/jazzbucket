package wiklosoft.mediaprovider;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.google.gson.JsonObject;
import com.kodart.httpzoid.Http;
import com.kodart.httpzoid.HttpFactory;
import com.kodart.httpzoid.HttpResponse;
import com.kodart.httpzoid.NetworkError;
import com.kodart.httpzoid.ResponseHandler;

import wiklosoft.mediaprovider.providers.OAuthProvider;

/**
 * Created by Pawel Wiklowski on 10.11.15.
 */
public class OAuthClient {
    public void authorize(FragmentManager manager,
                          OAuthProvider provider,
                          OAuthClientAuthResult callback)
    {
        OAuthClientWebView wv = new OAuthClientWebView();
        wv.setAuthCallback(callback);
        wv.setProvider(provider);
        wv.show(manager, "dialog");
    }

    public void refreshToken(Context context, OAuthProvider provider, final OAuthClientAuthResult callback){
        String postData = "";
        postData += "client_id=" + provider.getClientId();
        postData += "&client_secret=" +provider.getClientSecret();
        postData += "&grant_type=refresh_token";
        postData += "&refresh_token=" + provider.getRefreshToken();


        Http http = HttpFactory.create(context);
        http.post(provider.getTokenUrl())
            .data(postData)
            .contentType("application/x-www-form-urlencoded")
            .handler(new ResponseHandler<JsonObject>() {
                String TAG = "refreshToken";

                @Override
                public void success(JsonObject result, HttpResponse response) {
                    Log.d(TAG, "succes " + result);
                    String token = result.get("access_token").getAsString();

                    int valid = -1;
                    if (result.has("expires_in"))
                        valid = result.get("expires_in").getAsInt();


                    if (callback != null)
                        callback.onAuthorize(token, null, valid);
                }

                @Override
                public void error(String message, HttpResponse response) {
                    Log.e(TAG, "error" + message);
                }

                @Override
                public void failure(NetworkError error) {
                    Log.e(TAG, "failure" + error);
                }

                @Override
                public void complete() {
                    Log.d(TAG, "complete");
                }
            }).send();




    }
}
