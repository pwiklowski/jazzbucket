package wiklosoft.mediaprovider.providers;

import android.content.Context;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
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


/**
 * Created by Pawel Wiklowski on 08.10.15.
 */
public class SoundCloudProvider extends OAuthProvider {
    private String TAG = "SoundCloudProvider";
    private JsonObject userInfo = null;

    private final String PATH_PLAYLISTS = "playlists";
    private final String PATH_ROOT = "/";
    private final String PATH_TRACKS = "tracks";


    public SoundCloudProvider(Context context){
        super("soundcloud", context);

        AUTH_URL = "https://soundcloud.com/connect";
        TOKEN_URL = "https://api.soundcloud.com/oauth2/token";
        CLIENT_ID = "0068d0f6733d7079d8a9dc1183f716b4";
        CLIENT_SECRET = "92791bb8818d4d39f7d1d4d63714b99c";

        mContext = context;
        getAccountInfo();
    }

    String getUserId(){
        if (userInfo != null){
            return Integer.toString(userInfo.get("id").getAsInt());
        }
        return null;
    }

    void getAccountInfo(){
        Http http = HttpFactory.create(mContext);
        http.get("https://api.soundcloud.com/me?oauth_token="+getToken())
        .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {
                        Log.d(TAG, "success");
                        userInfo = result;
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
    void getPlaylists(final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens){
        Http http = HttpFactory.create(mContext);

        String url = "http://api.soundcloud.com/users/"+getUserId()+ "/playlists?client_id=" + getClientId();

        http.get(url)
            .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonArray>() {
                    @Override
                    public void success(JsonArray result, HttpResponse response) {
                        Log.d(TAG, "success");
                        List<MediaBrowser.MediaItem> list = new ArrayList<>();

                        for(JsonElement playlist: result){
                            String title = playlist.getAsJsonObject().get("title").getAsString();
                            String id = Integer.toString(playlist.getAsJsonObject().get("id").getAsInt());

                            MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                    .setMediaId(getId() + "/" + PATH_PLAYLISTS + "/" + id)
                                    .setTitle(title)
                                    .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE);

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
    void getTracks(String playlistId, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens){
        Http http = HttpFactory.create(mContext);

        String url = "http://api.soundcloud.com/playlists/"+playlistId+ "?client_id=" + getClientId();

        http.get(url)
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {
                        Log.d(TAG, "success");
                        List<MediaBrowser.MediaItem> list = new ArrayList<>();
                        JsonArray tracks = result.get("tracks").getAsJsonArray();
                        for(JsonElement track: tracks){
                            String title = track.getAsJsonObject().get("title").getAsString();
                            boolean streamable = track.getAsJsonObject().get("streamable").getAsBoolean();
                            String id = Integer.toString(track.getAsJsonObject().get("id").getAsInt());

                            if (streamable) {
                                MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                        .setMediaId(getId() + "/" + PATH_TRACKS + "/" + id)
                                        .setTitle(title)
                                        .build(), MediaBrowser.MediaItem.FLAG_PLAYABLE);

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
    public String getName(){
        return "SoundCloud";
    }

    @Override
    public void getChildren(String s, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens) {
        String path = s.replace(getId(), "");
        if(path.startsWith("/")) path = path.substring(1);

        List<MediaBrowser.MediaItem> list = new ArrayList<>();
        if (path.isEmpty()) {
            MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                    .setMediaId(getId() + "/" + PATH_PLAYLISTS)
                    .setTitle("Playlists")
                    .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE);
            list.add(item);
            childrens.sendResult(list);
        }else if (path.equals(PATH_PLAYLISTS)) {
            getPlaylists(childrens);
        }else if (path.startsWith(PATH_PLAYLISTS)) {
            String playlistId = path.split("/")[1];
            getTracks(playlistId, childrens);
        }

    }

    @Override
    public void getMediaUrl(String id, final MusicReady callback) {
        Http http = HttpFactory.create(mContext);

        String url = "http://api.soundcloud.com/tracks/"+id.replace(getId()+"/tracks/","")+ "?client_id=" + getClientId();

        http.get(url)
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {
                        Log.d(TAG, "success");

                        callback.ready(result.get("stream_url").getAsString()+"?client_id=" + getClientId(), null);


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

        String url = "http://api.soundcloud.com/tracks/"+id.replace(getId()+"/tracks/","")+ "?client_id=" + getClientId();

        http.get(url)
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {
                        Log.d(TAG, "success");
                        MediaMetadata.Builder b = new MediaMetadata.Builder();
                        b.putString(MediaMetadata.METADATA_KEY_ARTIST, result.get("tag_list").getAsString());
                        b.putString(MediaMetadata.METADATA_KEY_TITLE, result.get("title").getAsString());
                        b.putLong(MediaMetadata.METADATA_KEY_DURATION, result.get("duration").getAsInt());

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
            super.init();
        }
    }
}
