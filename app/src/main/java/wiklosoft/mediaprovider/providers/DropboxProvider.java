package wiklosoft.mediaprovider.providers;

import android.content.Context;
import android.media.MediaDescription;
import android.media.browse.MediaBrowser;
import android.service.media.MediaBrowserService;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kodart.httpzoid.Http;
import com.kodart.httpzoid.HttpFactory;
import com.kodart.httpzoid.HttpResponse;
import com.kodart.httpzoid.NetworkError;
import com.kodart.httpzoid.ResponseHandler;

import java.util.ArrayList;
import java.util.List;

import wiklosoft.mediaprovider.MusicReady;


/**
 * Created by Pawel Wiklowski on 08.10.15.
 */
public class DropboxProvider extends OAuthProvider {
    private String TAG = "DropboxProvider";

    private final String DBOX_GET_LINK = "https://api.dropboxapi.com/2/sharing/create_shared_link";


    public DropboxProvider(Context context){
        super("dropbox", context);
        mContext = context;
    }

    @Override
    public String getName() {
        return "Dropbox";
    }

    @Override
    public void getChildren(String s, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens) {
        String path = s.replace(ID, "");

        Http http = HttpFactory.create(mContext);
        http.post("https://api.dropboxapi.com/2/files/list_folder")
            .header("Authorization", "Bearer " + getToken())
            .contentType("application/json")
            .data("{\"path\": \"" + path + "\",\"recursive\": false}")
            .handler(new ResponseHandler<JsonObject>() {
                @Override
                public void success(JsonObject result, HttpResponse response) {
                    Log.d(TAG, "success");
                    List<MediaBrowser.MediaItem> list = new ArrayList<>();
                    JsonArray files = result.getAsJsonArray("entries");
                    for (JsonElement file : files) {
                        JsonObject fileObject = file.getAsJsonObject();
                        String filename = fileObject.get("name").getAsString();
                        Log.d(TAG, filename);

                        boolean isFile = fileObject.get(".tag").getAsString().equals("file");
                        if (!isFile || filename.endsWith(".mp3")) {

                            MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                    .setMediaId(ID + fileObject.get("path_lower").getAsString())
                                    .setTitle(file.getAsJsonObject().get("name").getAsString())
                                    .build(), isFile ? MediaBrowser.MediaItem.FLAG_PLAYABLE : MediaBrowser.MediaItem.FLAG_BROWSABLE);
                            list.add(item);
                        }
                    }
                    childrens.sendResult(list);
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

    @Override
    public void getMediaUrl(String id, final MusicReady callback) {
        final String path = id.replace(ID, "");

        Http http = HttpFactory.create(mContext);
        http.post(DBOX_GET_LINK)
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .data("{\"path\": \"" + path + "\",\"short_url\": false}")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {
                        Log.d(TAG, "succes " + result.get("url").getAsString());
                        String url = result.get("url").getAsString();
                        url = url.replace("dl=0", "dl=1");
                        callback.ready(url, null);
                    }

                    @Override
                    public void error(String message, HttpResponse response) { Log.e(TAG, "error" + message); }

                    @Override
                    public void failure(NetworkError error) { Log.e(TAG, "failure" + error); }

                    @Override
                    public void complete() { Log.d(TAG, "complete"); }
                }).send();

    }

    @Override
    public Fragment getSettingsFragment() {
        SettingsFragment fragment = new SettingsFragment();
        fragment.setProvider(this);
        return fragment;
    }

    public static class SettingsFragment extends BaseSettingsFragment {

        @Override
        public void init(){
            AUTH_URL = "https://www.dropbox.com/1/oauth2/authorize";
            TOKEN_URL = "https://api.dropbox.com/1/oauth2/token";
            CLIENT_ID = "bvyu7eknqot0rkl";
            CLIENT_SECRET = "u4an3qd7gkkmjo9";
            super.init();
        }
    }
}
