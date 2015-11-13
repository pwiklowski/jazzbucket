package wiklosoft.mediaprovider.providers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.net.Uri;
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

import org.json.JSONArray;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import wiklosoft.mediaprovider.MetadataReady;
import wiklosoft.mediaprovider.MusicReady;
import wiklosoft.mediaprovider.R;


/**
 * Created by Pawel Wiklowski on 08.10.15.
 */
public class SoundCloudProvider extends OAuthProvider {
    private String TAG = "SoundCloudProvider";
    private JsonObject userInfo = null;

    private final String PATH_PLAYLIST = "playlist";
    private final String PATH_PLAYLISTS = "playlists";
    private final String PATH_FOLLOWINGS = "followings";
    private final String PATH_GROUPS = "groups";
    private final String PATH_FAVORITES = "favorites";


    private final String PATH_ROOT = "/";
    private final String PATH_TRACKS = "tracks";


    public SoundCloudProvider(Context context){
        super("soundcloud", context);

        AUTH_URL = "https://soundcloud.com/connect";
        TOKEN_URL = "https://api.soundcloud.com/oauth2/token";
        CLIENT_ID = "5cdd2995d85289e5fc99586b5bcee550";
        CLIENT_SECRET = "b84e8b162585031dbdd7f927397ffaae";

        mContext = context;
        getAccountInfo();
    }

    @Override
    public String getScopes(){
        return "non-expiring";
    }

    public String getResponseType(){
        return "token";
    }

    @Override
    public String getGrantType(){
        return "authorization_code";
    }

    @Override
    public void setToken(String token){
        super.setToken(token);
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
            }).send();
    }
    void getPlaylists(final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens, String userId){
        Http http = HttpFactory.create(mContext);

        String url = "http://api.soundcloud.com/users/"+ userId + "/playlists?client_id=" + getClientId();

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
                                    .setMediaId(getId() + "/" + PATH_PLAYLIST + "/" + id)
                                    .setTitle(title)
                                    .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE);

                            list.add(item);
                        }

                        childrens.sendResult(list);

                    }
            }).send();
    }

    void getFollowings(final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens){
        Http http = HttpFactory.create(mContext);

        String url = "http://api.soundcloud.com/users/"+getUserId()+ "/followings?client_id=" + getClientId();

        http.get(url)
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonArray>() {
                    @Override
                    public void success(JsonArray result, HttpResponse response) {
                        Log.d(TAG, "success");
                        List<MediaBrowser.MediaItem> list = new ArrayList<>();

                        for (JsonElement user : result) {
                            JsonObject u = user.getAsJsonObject();
                            String title = u.get("username").getAsString();
                            String id = Integer.toString(u.get("id").getAsInt());
                            String avatar = "";

                            if (u.has("avatar_url")) {
                                avatar = u.get("avatar_url").getAsString();
                            }

                            MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                    .setMediaId(getId() + "/" + PATH_FOLLOWINGS + "/" + id)
                                    .setTitle(title)
                                    .setIconUri(Uri.parse(avatar))
                                    .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE);

                            list.add(item);
                        }

                        childrens.sendResult(list);

                    }
                }).send();
    }


    void getPlaylistTracks(String playlistId, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens){
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
                            String artwork = track.getAsJsonObject().get("artwork_url").getAsString();

                            boolean streamable = false;
                            try {
                                if (track.getAsJsonObject().has("streamable"))
                                    streamable = track.getAsJsonObject().get("streamable").getAsBoolean();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            String id = Integer.toString(track.getAsJsonObject().get("id").getAsInt());

                            if (streamable) {
                                MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                        .setMediaId(getId() + "/" + PATH_TRACKS + "/" + id)
                                        .setTitle(title)
                                        .setIconUri(Uri.parse(artwork))
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
    void getUserTracks(String userId, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens){
        Http http = HttpFactory.create(mContext);

        String url = "http://api.soundcloud.com/users/"+userId+ "/tracks?client_id=" + getClientId();

        http.get(url)
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonArray>() {
                    @Override
                    public void success(JsonArray tracks, HttpResponse response) {
                        Log.d(TAG, "success");
                        List<MediaBrowser.MediaItem> list = new ArrayList<>();
                        for(JsonElement track: tracks){
                            String title = track.getAsJsonObject().get("title").getAsString();
                            String artwork = track.getAsJsonObject().get("artwork_url").getAsString();
                            boolean streamable = false;
                            try {
                                if (track.getAsJsonObject().has("streamable"))
                                    streamable = track.getAsJsonObject().get("streamable").getAsBoolean();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            String id = Integer.toString(track.getAsJsonObject().get("id").getAsInt());

                            if (streamable) {
                                MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                        .setMediaId(getId() + "/" + PATH_TRACKS + "/" + id)
                                        .setTitle(title)
                                        .setIconUri(Uri.parse(artwork))
                                        .build(), MediaBrowser.MediaItem.FLAG_PLAYABLE);

                                list.add(item);
                            }
                        }
                        childrens.sendResult(list);

                    }
                }).send();
    }

    void getGroupTracks(String groupId, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens){
        Http http = HttpFactory.create(mContext);

        String url = "http://api.soundcloud.com/groups/"+ groupId + "/tracks?client_id=" + getClientId();

        http.get(url)
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonArray>() {
                    @Override
                    public void success(JsonArray tracks, HttpResponse response) {
                        Log.d(TAG, "success");
                        List<MediaBrowser.MediaItem> list = new ArrayList<>();
                        for(JsonElement track: tracks){
                            String title = track.getAsJsonObject().get("title").getAsString();
                            String artwork = track.getAsJsonObject().get("artwork_url").getAsString();
                            boolean streamable = false;
                            try {
                                if (track.getAsJsonObject().has("streamable"))
                                    streamable = track.getAsJsonObject().get("streamable").getAsBoolean();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            String id = Integer.toString(track.getAsJsonObject().get("id").getAsInt());

                            if (streamable) {
                                MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                        .setMediaId(getId() + "/" + PATH_TRACKS + "/" + id)
                                        .setTitle(title)
                                        .setIconUri(Uri.parse(artwork))
                                        .build(), MediaBrowser.MediaItem.FLAG_PLAYABLE);

                                list.add(item);
                            }
                        }
                        childrens.sendResult(list);

                    }
                }).send();
    }

    void getGroups(String userId, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens){
        Http http = HttpFactory.create(mContext);

        String url = "http://api.soundcloud.com/users/"+userId+ "/groups?client_id=" + getClientId();

        http.get(url)
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonArray>() {
                    @Override
                    public void success(JsonArray groups, HttpResponse response) {
                        Log.d(TAG, "success");
                        List<MediaBrowser.MediaItem> list = new ArrayList<>();
                        for(JsonElement group: groups){
                            String name = group.getAsJsonObject().get("name").getAsString();
                            String artwork = group.getAsJsonObject().get("artwork_url").getAsString();
                            String id = Integer.toString(group.getAsJsonObject().get("id").getAsInt());

                            MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                    .setMediaId(getId() + "/" + PATH_GROUPS + "/" + id)
                                    .setIconUri(Uri.parse(artwork))
                                    .setTitle(name)
                                    .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE);

                            list.add(item);
                        }
                        childrens.sendResult(list);

                    }
                }).send();
    }

    void getFavoritesTracks(String userId, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens){
        Http http = HttpFactory.create(mContext);

        String url = "http://api.soundcloud.com/users/"+userId+ "/favorites?client_id=" + getClientId();

        http.get(url)
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonArray>() {
                    @Override
                    public void success(JsonArray tracks, HttpResponse response) {
                        Log.d(TAG, "success");
                        List<MediaBrowser.MediaItem> list = new ArrayList<>();
                        for(JsonElement track: tracks){
                            String title = track.getAsJsonObject().get("title").getAsString();
                            String artwork = track.getAsJsonObject().get("artwork_url").getAsString();
                            boolean streamable = false;
                            try {
                                if (track.getAsJsonObject().has("streamable"))
                                    streamable = track.getAsJsonObject().get("streamable").getAsBoolean();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            String id = Integer.toString(track.getAsJsonObject().get("id").getAsInt());

                            if (streamable) {
                                MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                        .setMediaId(getId() + "/" + PATH_TRACKS + "/" + id)
                                        .setTitle(title)
                                        .setIconUri(Uri.parse(artwork))
                                        .build(), MediaBrowser.MediaItem.FLAG_PLAYABLE);

                                list.add(item);
                            }
                        }
                        childrens.sendResult(list);

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
            list.add(new MediaBrowser.MediaItem(new MediaDescription.Builder()
                    .setMediaId(getId() + "/" + PATH_PLAYLISTS)
                    .setTitle("Playlists")
                    .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE));


            list.add(new MediaBrowser.MediaItem(new MediaDescription.Builder()
                    .setMediaId(getId() + "/" + PATH_FAVORITES)
                    .setTitle("Favorites")
                    .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE));


            list.add(new MediaBrowser.MediaItem(new MediaDescription.Builder()
                    .setMediaId(getId() + "/" + PATH_FOLLOWINGS)
                    .setTitle("Followings")
                    .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE));

            list.add(new MediaBrowser.MediaItem(new MediaDescription.Builder()
                    .setMediaId(getId() + "/" +PATH_GROUPS)
                    .setTitle("Groups")
                    .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE));

            childrens.sendResult(list);
        }else if (path.equals(PATH_PLAYLISTS)) {
            getPlaylists(childrens, getUserId());
        }else if (path.equals(PATH_FAVORITES)) {
            getFavoritesTracks(getUserId(), childrens);
        }else if (path.equals(PATH_GROUPS)) {
            getGroups(getUserId(), childrens);

        }else if (path.startsWith(PATH_GROUPS)) {
            String groupId = path.split("/")[1];
            getGroupTracks(groupId, childrens);

        }else if (path.startsWith(PATH_TRACKS)) {
            String userId = path.split("/")[1];
            getUserTracks(userId, childrens);
        }else if (path.startsWith(PATH_PLAYLISTS)) {
            String userId = path.split("/")[1];
            getPlaylists(childrens, userId);
        }else if (path.startsWith(PATH_PLAYLIST)) {
            String playlistId = path.split("/")[1];
            getPlaylistTracks(playlistId, childrens);
        }else if (path.equals(PATH_FOLLOWINGS)){
            getFollowings(childrens);
        }else if (path.startsWith(PATH_FOLLOWINGS)) {
            String userID = path.split("/")[1];


            list.add(new MediaBrowser.MediaItem(new MediaDescription.Builder()
                    .setMediaId(getId() + "/"+ PATH_PLAYLISTS +"/" +userID)
                    .setTitle("Playlists")
                    .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE));


            list.add(new MediaBrowser.MediaItem(new MediaDescription.Builder()
                    .setMediaId(getId() + "/"+ PATH_TRACKS + "/" + userID)
                    .setTitle("Tracks")
                    .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE));

            childrens.sendResult(list);
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

                        callback.ready(result.get("stream_url").getAsString() + "?client_id=" + getClientId(), null);


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
                    public void success(final JsonObject result, HttpResponse response) {
                        Log.d(TAG, "success");
                        final MediaMetadata.Builder b = new MediaMetadata.Builder();
                        b.putString(MediaMetadata.METADATA_KEY_TITLE, result.get("title").getAsString());
                        b.putLong(MediaMetadata.METADATA_KEY_DURATION, result.get("duration").getAsInt());

                        b.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, result.get("artwork_url").getAsString());

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
            mLogo = R.mipmap.soundcloud;
            super.init();
        }
    }
}
