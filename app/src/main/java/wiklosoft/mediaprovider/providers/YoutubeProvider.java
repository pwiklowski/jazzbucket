package wiklosoft.mediaprovider.providers;

import android.content.Context;
import android.media.MediaDescription;
import android.media.MediaMetadata;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wiklosoft.mediaprovider.MetadataReady;
import wiklosoft.mediaprovider.MusicReady;
import wiklosoft.mediaprovider.R;


/**
 * Created by Pawel Wiklowski on 08.10.15.
 */
public class YoutubeProvider extends OAuthProvider {
    private String TAG = "YoutubeProvider";


    public YoutubeProvider(Context context){
        super("youtube", context);

        AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
        TOKEN_URL = "https://www.googleapis.com/oauth2/v3/token";
        CLIENT_ID = "94210570259-ltnois2uoourqqcgic7ptqr7cv0gpumc.apps.googleusercontent.com";
        CLIENT_SECRET = "OCOC95FB_Sha140EhSj2fMYJ";

        mContext = context;
    }

    @Override
    public String getAuthExtras(){
        return "&approval_prompt=force&access_type=offline";
    }

    @Override
    public String getScopes(){
        return "https://www.googleapis.com/auth/youtube";
    }


    @Override
    public String getName(){
        return "Youtube";
    }

    @Override
    public void getChildren(String s, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens) {

        Http http = HttpFactory.create(mContext);
        http.get("https://www.googleapis.com/youtube/v3/videos?part=snippet&chart=mostPopular&maxResults=50")
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {
                        Log.d(TAG, "success");
                        List<MediaBrowser.MediaItem> list = new ArrayList<>();
                        JsonArray files = result.get("items").getAsJsonArray();
                        for (JsonElement file : files) {
                            JsonObject snippet = file.getAsJsonObject().get("snippet").getAsJsonObject();
                            String title = snippet.get("title").getAsString();
                            String video_id =file.getAsJsonObject().get("id").getAsString();

                            Log.d(TAG, title);


                            MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                    .setMediaId(getId() + "/" + video_id)
                                    .setTitle(title)
                                    .build(), MediaBrowser.MediaItem.FLAG_PLAYABLE);
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
        Http http = HttpFactory.create(mContext);
        http.get("http://192.168.1.8:8000/"+id+"/")
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {
                        Log.d(TAG, "success");
                        Map<String,String> headers = new HashMap<String,String>();

                        callback.ready(result.get("url").getAsString(), headers);


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
    public boolean getMetaData(String id, final MetadataReady callback) {
        Http http = HttpFactory.create(mContext);
        http.get("https://www.googleapis.com/youtube/v3/videos?part=snippet%2C+contentDetails&id="+id.replace(getId()+"/",""))
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {
                        Log.d(TAG, "success");
                        JsonObject item= result.get("items").getAsJsonArray().get(0).getAsJsonObject();
                        JsonObject snippet = item.get("snippet").getAsJsonObject();
                        String title = snippet.get("title").getAsString();
                        String channelTitle = snippet.get("channelTitle").getAsString();

                        JsonObject contentDetails = item.get("snippet").getAsJsonObject();


                        Log.d(TAG, title);


                        MediaMetadata.Builder b = new MediaMetadata.Builder();
                        b.putString(MediaMetadata.METADATA_KEY_ARTIST, channelTitle);
                        b.putString(MediaMetadata.METADATA_KEY_TITLE, title);
                        //b.putLong(MediaMetadata.METADATA_KEY_DURATION, result.get("duration").getAsInt());

                        callback.ready(b.build());


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


        return true;
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
            mLogo = R.mipmap.youtube;
            super.init();
        }
    }
}
