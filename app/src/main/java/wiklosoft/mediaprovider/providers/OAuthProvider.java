package wiklosoft.mediaprovider.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.preference.PreferenceManager;
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
public class OAuthProvider implements MusicProvider {
    protected String ID = null;
    protected Context mContext;

    public OAuthProvider(String id, Context context){
        ID = id;
        mContext = context;

    }

    public String getToken(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPref.getString(ID, "");
    }

    public void setToken(String token){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ID, token);
        editor.commit();
    }

    @Override
    public String getName() {
        return "No name";
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void getChildren(String s, final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> childrens) {

    }


    @Override
    public void getMediaUrl(String id, final MusicReady callback) {

    }

    @Override
    public MediaMetadata getMetaData(String id) {
        return null;
    }
    @Override
    public Fragment getSettingsFragment() {
        return null;
    }




}
