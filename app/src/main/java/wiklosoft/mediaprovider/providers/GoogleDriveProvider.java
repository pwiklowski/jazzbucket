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
import com.kodart.httpzoid.HttpRequest;
import com.kodart.httpzoid.HttpResponse;
import com.kodart.httpzoid.NetworkError;
import com.kodart.httpzoid.ResponseHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wiklosoft.mediaprovider.MusicReady;


/**
 * Created by Pawel Wiklowski on 08.10.15.
 */
public class GoogleDriveProvider extends OAuthProvider {
    private String TAG = "GoogleDriveProvider";


    public GoogleDriveProvider(Context context){
        super("googledrive", context);
        mContext = context;
        AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
        TOKEN_URL = "https://www.googleapis.com/oauth2/v3/token";
        CLIENT_ID = "94210570259-ltnois2uoourqqcgic7ptqr7cv0gpumc.apps.googleusercontent.com";
        CLIENT_SECRET = "OCOC95FB_Sha140EhSj2fMYJ";
    }

    @Override
    public String getAuthExtras(){
        return "&approval_prompt=force&access_type=offline";
    }

    @Override
    public String getScopes(){
        return "https://www.googleapis.com/auth/drive";
    }

    @Override
    public String getName(){
        return "Google Drive";
    }

    @Override
    public void getChildren(String s, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens) {

        if (getTokenExpiration().before(new Date())){
            refreshToken();
        }

        String path = s.replace(getId(), "");
        if(path.startsWith("/")) path = path.substring(1);

        //path = "0B-1hrAieu74ZWEFzbms3TFBETE0";

        if (path.isEmpty()) path = "root";


        String q= "'"+path+"'+in+parents+and+(mimeType+%3D+'application%2Fvnd.google-apps.folder'+or+mimeType+contains+'audio')";
        String fields = "items(id%2CmimeType%2Ctitle)";

        Http http = HttpFactory.create(mContext);
        http.get("https://www.googleapis.com/drive/v2/files?orderBy=title&q=" + q + "&fields=" + fields)
            .header("Authorization", "Bearer " + getToken())
            .contentType("application/json")
            .handler(new ResponseHandler<JsonObject>() {
                @Override
                public void success(JsonObject result, HttpResponse response) {
                    Log.d(TAG, "success");
                    List<MediaBrowser.MediaItem> list = new ArrayList<>();
                    JsonArray files = result.get("items").getAsJsonArray();
                    for (JsonElement file : files) {
                        JsonObject fileObject = file.getAsJsonObject();
                        String filename = fileObject.get("title").getAsString();
                        String id = fileObject.get("id").getAsString();

                        Log.d(TAG, filename);

                        boolean isFile = !fileObject.get("mimeType").getAsString().equals("application/vnd.google-apps.folder");

                            MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                .setMediaId(getId() + "/" + id)
                                .setTitle(filename)
                                .build(), isFile ? MediaBrowser.MediaItem.FLAG_PLAYABLE : MediaBrowser.MediaItem.FLAG_BROWSABLE);
                        list.add(item);
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
        if (getTokenExpiration().before(new Date())){
            refreshToken();
        }
        String link = "https://www.googleapis.com/drive/v2/files/"+id.replace(getId()+"/","")+"?fields=downloadUrl";

        Http http = HttpFactory.create(mContext);
        http.get(link)
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {
                        Log.d(TAG, "success");
                        Map<String,String> headers = new HashMap<String,String>();
                        headers.put("Authorization", "Bearer "+ getToken());

                        callback.ready(result.get("downloadUrl").getAsString(), headers);

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
    public Fragment getSettingsFragment() {
        SettingsFragment fragment = new SettingsFragment();
        fragment.setProvider(this);
        return fragment;
    }

    public static class SettingsFragment extends BaseSettingsFragment {

        @Override
        public void init(){

            super.init();
        }
    }
}
