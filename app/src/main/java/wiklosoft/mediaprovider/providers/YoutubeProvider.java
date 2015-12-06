package wiklosoft.mediaprovider.providers;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.Date;
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


    private final String PATH_PLAYLISTS = "playlist";
    private final String PATH_CHANNELS = "channels";
    private final String PATH_LIKED = "liked";
    private final String PATH_SUBSCRIBED = "subscribed";
    private final String PATH_HISTORY = "history";
    private final String PATH_FAVORITES = "favorites";
    private final String PATH_WATCH_LATER = "watch_later";

    private final String PLAYLIST_LIKES = "likes";
    private final String PLAYLIST_FAVORITES = "favorites";
    private final String PLAYLIST_UPLOADS = "uploads";
    private final String PLAYLIST_WATCH_HISTORY = "watchHistory";
    private final String PLAYLIST_WATCH_LATER = "watchLater";

    private String mChannelId = "";


    private HashMap<String,String> mChannelsMap = new HashMap<>();


    public YoutubeProvider(Context context){
        super("youtube", context);

        AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
        TOKEN_URL = "https://www.googleapis.com/oauth2/v3/token";
        CLIENT_ID = "94210570259-ltnois2uoourqqcgic7ptqr7cv0gpumc.apps.googleusercontent.com";
        CLIENT_SECRET = "OCOC95FB_Sha140EhSj2fMYJ";

        mContext = context;
        mIcon = R.mipmap.youtube;

        getChannels();

    }

    @Override
    public String getAuthExtras(){
        return "&approval_prompt=force&access_type=offline";
    }

    @Override
    public String getScopes(){
        return "https://www.googleapis.com/auth/youtube https://www.googleapis.com/auth/youtube.force-ssl https://www.googleapis.com/auth/youtubepartner https://www.googleapis.com/auth/youtube.readonly";
    }


    @Override
    public String getName(){
        return "Youtube";
    }

    MediaBrowser.MediaItem buildMenuEntry(String name, String id){
        return new MediaBrowser.MediaItem(new MediaDescription.Builder()
                .setMediaId(id)
                .setTitle(name)
                .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE);
    }


    @Override
    public void getChildren(String s, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens) {
        if (getTokenExpiration().before(new Date()))
        {
            refreshToken();
        }
        String path = s.replace(getId(), "");
        if(path.startsWith("/")) path = path.substring(1);

        List<MediaBrowser.MediaItem> list = new ArrayList<>();
        if (path.isEmpty()) {
            list.add(buildMenuEntry("Subscribed", getId() + "/" + PATH_SUBSCRIBED));
            list.add(buildMenuEntry("Liked videos", getId() + "/" + PATH_LIKED));
            list.add(buildMenuEntry("Watch later videos", getId() + "/" +PATH_WATCH_LATER));
            list.add(buildMenuEntry("History", getId() + "/" + PATH_HISTORY));
            list.add(buildMenuEntry("Favorites", getId() + "/" + PATH_FAVORITES));
            list.add(buildMenuEntry("Playlists", getId() + "/" + PATH_PLAYLISTS));


            childrens.sendResult(list);
        }else if (path.equals(PATH_HISTORY)){
            getPlaylist(mChannelsMap.get(PLAYLIST_WATCH_HISTORY), childrens);
        }else if (path.equals(PATH_FAVORITES)){
            getPlaylist(mChannelsMap.get(PLAYLIST_FAVORITES), childrens);
        }else if (path.equals(PATH_LIKED)){
            getPlaylist(mChannelsMap.get(PLAYLIST_LIKES), childrens);
        }else if (path.equals(PATH_WATCH_LATER)){
            getPlaylist(mChannelsMap.get(PLAYLIST_WATCH_LATER), childrens);
        }else if (path.equals(PATH_SUBSCRIBED)){
            getSubscriptions(childrens);
        }else if (path.startsWith(PATH_SUBSCRIBED)){
            String channelId = path.split("/")[1];
            getVideosOfUser(channelId, childrens);
        }else if (path.equals(PATH_PLAYLISTS)){
            getPlaylists(childrens);
        }else if (path.startsWith(PATH_PLAYLISTS)){
            String playlistId = path.split("/")[1];
            getPlaylist(playlistId, childrens);
        }

    }

    public void getPlaylists(final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens){


        if (getTokenExpiration().before(new Date()))
        {
            refreshToken();
        }
        Http http = HttpFactory.create(mContext);
        http.get("https://www.googleapis.com/youtube/v3/playlists?part=snippet&mine=true")
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {
                        List<MediaBrowser.MediaItem> list = new ArrayList<>();
                        JsonArray items = result.get("items").getAsJsonArray();
                        for (JsonElement i: items) {
                            String id = i.getAsJsonObject().get("id").getAsString();
                            JsonObject snippet = i.getAsJsonObject().get("snippet").getAsJsonObject();
                            String title = snippet.get("title").getAsString();

                            MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                    .setMediaId(getId() + "/" + PATH_PLAYLISTS + "/" + id)
                                    .setTitle(title)
                                    .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE);
                            list.add(item);

                        }
                        childrens.sendResult(list);


                    }
                }).send();
    }

    public void getChannels(){
        if (getTokenExpiration().before(new Date()))
        {
            refreshToken();
        }
        Http http = HttpFactory.create(mContext);
        http.get("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&mine=true")
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {
                        Log.d(TAG, "success");
                        JsonObject items = result.get("items").getAsJsonArray().get(0).getAsJsonObject();
                        JsonObject files = items.get("contentDetails").getAsJsonObject().get("relatedPlaylists").getAsJsonObject();

                        mChannelId = items.get("id").getAsString();

                        mChannelsMap.put(PLAYLIST_LIKES, files.get(PLAYLIST_LIKES).getAsString());
                        mChannelsMap.put(PLAYLIST_FAVORITES, files.get(PLAYLIST_FAVORITES).getAsString());
                        mChannelsMap.put(PLAYLIST_UPLOADS, files.get(PLAYLIST_UPLOADS).getAsString());
                        mChannelsMap.put(PLAYLIST_WATCH_HISTORY, files.get(PLAYLIST_WATCH_HISTORY).getAsString());
                        mChannelsMap.put(PLAYLIST_WATCH_LATER, files.get(PLAYLIST_WATCH_LATER).getAsString());

                    }
                }).send();

    }

    public void getVideosOfUser(String channelId, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens){
        if (getTokenExpiration().before(new Date()))
        {
            refreshToken();
        }
        Http http = HttpFactory.create(mContext);
        http.get("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&id="+ channelId)
                .header("Authorization", "Bearer " + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {
                        Log.d(TAG, "success");
                        JsonObject items = result.get("items").getAsJsonArray().get(0).getAsJsonObject();
                        JsonObject files = items.get("contentDetails").getAsJsonObject().get("relatedPlaylists").getAsJsonObject();
                        String playlistId = files.get("uploads").getAsString();
                        getPlaylist(playlistId, childrens);
                    }
                }).send();

    }



    public void getPlaylist(String playlistId, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens){
        if (getTokenExpiration().before(new Date()))
        {
            refreshToken();
        }
        Http http = HttpFactory.create(mContext);
        http.get("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&fields=items%2Fsnippet%2FresourceId%2FvideoId%2Citems%2Fsnippet%2Ftitle&maxResults=50&playlistId="+playlistId)
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
                            String video_id = snippet.get("resourceId").getAsJsonObject().get("videoId").getAsString();

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
    public void getSubscriptions(final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens){
        if (getTokenExpiration().before(new Date()))
        {
            refreshToken();
        }
        Http http = HttpFactory.create(mContext);
        http.get("https://www.googleapis.com/youtube/v3/subscriptions?part=snippet&maxResults=50&fields=items(id%2Csnippet)&order=alphabetical&channelId="+mChannelId)
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
                            String channel_id = snippet.get("resourceId").getAsJsonObject().get("channelId").getAsString();
                            String thumbnail_id = snippet.get("thumbnails").getAsJsonObject().get("default").getAsJsonObject().get("url").getAsString();

                            Log.d(TAG, title);


                            MediaBrowser.MediaItem item = new MediaBrowser.MediaItem(new MediaDescription.Builder()
                                    .setMediaId(getId() + "/" + PATH_SUBSCRIBED + "/"+ channel_id)
                                    .setTitle(title)
                                    .setIconUri(Uri.parse(thumbnail_id))
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


    @Override
    public void getMediaUrl(String id, final MusicReady callback) {
        if (getTokenExpiration().before(new Date()))
        {
            refreshToken();
        }

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
        if (getTokenExpiration().before(new Date()))
        {
            refreshToken();
        }
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
    }
}
