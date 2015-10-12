package wiklosoft.mediaprovider.providers;

import android.content.Context;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.browse.MediaBrowser.MediaItem;
import android.os.Bundle;
import android.service.media.MediaBrowserService;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import wiklosoft.mediaprovider.MetadataReady;
import wiklosoft.mediaprovider.MusicReady;
import wiklosoft.mediaprovider.R;

/**
 * Created by Pawel Wiklowski on 07.10.15.
 */
public class DibbleProvider implements MusicProvider {
    private String TAG = "DirbleProvider";
    private String mId = "dirble";
    private String mToken = "2fe4f20578027d7af016791dac";
    private Context mContext = null;
    private final String PATH_CATEGORIES = "Categories";

    private final String PATH_CONTINENTS = "Continents";
    private final String PATH_COUNTRY = "Countries";
    private final String PATH_STATION = "Station";


    HashMap<String, String> files = new HashMap<>();



    public DibbleProvider(Context context){
        mContext = context;
    }


    @Override
    public String getToken() {
        return mToken;
    }

    @Override
    public void setToken(String token) {
        mToken = token;
    }

    @Override
    public String getName() {
        return "Dirble";
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public void getChildren(String s, MediaBrowserService.Result<List<MediaItem>> result){
        s = s.replace(mId, "");
        if(s.startsWith("/")) s = s.substring(1);
        List<MediaItem> list = new ArrayList<>();

        if (s.isEmpty()){
            list.add(new MediaItem(new MediaDescription.Builder()
                    .setMediaId(mId + "/" + PATH_CATEGORIES)
                    .setTitle(PATH_CATEGORIES)
                    .build(), MediaItem.FLAG_BROWSABLE));

            list.add(new MediaItem(new MediaDescription.Builder()
                    .setMediaId(mId + "/" + PATH_COUNTRY)
                    .setTitle(PATH_COUNTRY)
                    .build(), MediaItem.FLAG_BROWSABLE));
            result.sendResult(list);
        } else if(s.equals(PATH_CATEGORIES)) {
            getCategories(result);
        } else if (s.startsWith(PATH_CATEGORIES)){
            String[] params = s.replace(PATH_CATEGORIES+"/", "").split("/");
            String id = params[0];
            int page = Integer.parseInt(params[1]);

            getCategoryStations(id, result, page);
        } else if (s.equals(PATH_COUNTRY)){
            getContinents(result);
        } else if (s.startsWith(PATH_COUNTRY)){
            String[] params = s.replace(PATH_COUNTRY +"/", "").split("/");
            String id = params[0];
            int page = Integer.parseInt(params[1]);
            getCountryStations(id, result, page);
        } else if (s.startsWith(PATH_CONTINENTS)){
            String id = s.split("/")[1];
            getCountries(id, result);
        }
    }

    public void getCategories(final MediaBrowserService.Result<List<MediaItem>> stations) {
        Http http = HttpFactory.create(mContext);
        http.get("http://api.dirble.com/v2/categories/primary?token=" + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonArray>() {
                    @Override
                    public void success(JsonArray result, HttpResponse response) {
                        Log.d(TAG, "success");

                        List<MediaItem> list = new ArrayList<>();
                        for (JsonElement station: result){
                            String name = station.getAsJsonObject().get("title").getAsString();
                            String id = Integer.toString(station.getAsJsonObject().get("id").getAsInt());

                            list.add(new MediaItem(new MediaDescription.Builder()
                                    .setMediaId(mId + "/" + PATH_CATEGORIES +"/"+ id + "/1")
                                    .setTitle(name)
                                    .build(), MediaItem.FLAG_BROWSABLE));
                        }
                        stations.sendResult(list);
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
    public void getContinents(final MediaBrowserService.Result<List<MediaItem>> stations) {
        Http http = HttpFactory.create(mContext);
        http.get("http://api.dirble.com/v2/continents?token=" + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonArray>() {
                    @Override
                    public void success(JsonArray result, HttpResponse response) {
                        Log.d(TAG, "success");

                        List<MediaItem> list = new ArrayList<>();
                        for (JsonElement station: result){
                            String name = station.getAsJsonObject().get("name").getAsString();
                            String id = Integer.toString(station.getAsJsonObject().get("id").getAsInt());

                            list.add(new MediaItem(new MediaDescription.Builder()
                                    .setMediaId(mId + "/" + PATH_CONTINENTS +"/"+ id)
                                    .setTitle(name)
                                    .build(), MediaItem.FLAG_BROWSABLE));
                        }
                        stations.sendResult(list);
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
    public void getCountries(String continent, final MediaBrowserService.Result<List<MediaItem>> stations) {
        Http http = HttpFactory.create(mContext);
        http.get("http://api.dirble.com/v2/continents/"+continent+"/countries?token=" + getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonArray>() {
                    @Override
                    public void success(JsonArray result, HttpResponse response) {
                        Log.d(TAG, "success");

                        List<MediaItem> list = new ArrayList<>();
                        for (JsonElement station: result){
                            String name = station.getAsJsonObject().get("country_code").getAsString();


                            list.add(new MediaItem(new MediaDescription.Builder()
                                    .setMediaId(mId + "/" + PATH_COUNTRY+"/"+ name + "/1")
                                    .setTitle(name)
                                    .build(), MediaItem.FLAG_BROWSABLE));
                        }
                        stations.sendResult(list);
                    }
                }).send();
    }

    private void getCategoryStations(String id, final MediaBrowserService.Result<List<MediaItem>> stations, int page){
        getStations(id, "category", stations, page);
    }

    private void getCountryStations(String id, final MediaBrowserService.Result<List<MediaItem>> stations, int page){
        getStations(id, "countries", stations, page);
    }
    public void getStations(final String id, final String type, final MediaBrowserService.Result<List<MediaItem>> stations, final int page) {
        String url = "http://api.dirble.com/v2/"+type+"/"+id+"/stations?token=" + getToken();
        Log.d(TAG, "getStations " + url);
        Http http = HttpFactory.create(mContext);
        http.get(url + "&page="+Integer.toString(page))
            .contentType("application/json")
            .handler(new ResponseHandler<JsonArray>() {
                @Override
                public void success(JsonArray result, HttpResponse response) {
                    Log.d(TAG, "success");

                    List<MediaItem> list = new ArrayList<>();
                    for (JsonElement station : result) {
                        String name = station.getAsJsonObject().get("name").getAsString();
                        String id = Integer.toString(station.getAsJsonObject().get("id").getAsInt());

                        JsonArray streams = station.getAsJsonObject().get("streams").getAsJsonArray();

                        String stream = "";
                        if (streams.size() >0) stream = streams.get(0).getAsJsonObject().get("stream").getAsString();

                        if (!stream.isEmpty()) {
                            list.add(new MediaItem(new MediaDescription.Builder()
                                    .setMediaId(mId + "/" + PATH_STATION + "/" + id)
                                    .setTitle(name)
                                    .build(), MediaItem.FLAG_PLAYABLE));
                        }
                    }
                    if (result.size() == 20) {
                        int nextPage = page + 1;
                        String t = type.equals("category") ? PATH_CATEGORIES : PATH_COUNTRY;

                        list.add(new MediaItem(new MediaDescription.Builder()
                                .setMediaId(mId + "/" + t + "/" + id + "/" + Integer.toString(nextPage))
                                .setTitle("More...")
                                .build(), MediaItem.FLAG_BROWSABLE));
                    }
                    stations.sendResult(list);
                }
            }).send();
    }
    @Override
    public void getMediaUrl(String id, final MusicReady callback) {

        id = id.replace(mId +"/"+ PATH_STATION+"/", "");

        Http http = HttpFactory.create(mContext);
        http.get("http://api.dirble.com/v2/station/"+id+"?token="+getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {

                        JsonArray streams = result.get("streams").getAsJsonArray();
                        String stream = streams.get(0).getAsJsonObject().get("stream").getAsString();

                        callback.ready(stream,  null);
                    }
                }).send();



    }

    @Override
    public boolean getMetaData(String url, final MetadataReady callback) {

        String id = url.replace(mId +"/"+ PATH_STATION+"/", "");

        Http http = HttpFactory.create(mContext);
        http.get("http://api.dirble.com/v2/station/"+id+"?token="+getToken())
                .contentType("application/json")
                .handler(new ResponseHandler<JsonObject>() {
                    @Override
                    public void success(JsonObject result, HttpResponse response) {
                        String name = result.get("name").getAsString();
                        MediaMetadata.Builder b = new MediaMetadata.Builder();
                        b.putString(MediaMetadata.METADATA_KEY_TITLE, name);
                        callback.ready(b.build());
                    }
                }).send();
        return true;
    }

    @Override
    public Fragment getSettingsFragment() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    public static class SettingsFragment extends Fragment {

        public SettingsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.test_settings, container, false);
            return rootView;
        }
    }
}
