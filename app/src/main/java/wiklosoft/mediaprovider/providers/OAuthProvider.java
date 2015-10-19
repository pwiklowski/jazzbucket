package wiklosoft.mediaprovider.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
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

import wiklosoft.mediaprovider.MetadataReady;
import wiklosoft.mediaprovider.MusicReady;
import wiklosoft.mediaprovider.QueueReady;


/**
 * Created by Pawel Wiklowski on 08.10.15.
 */
public class OAuthProvider implements MusicProvider {
    protected String ID = null;
    protected Context mContext;
    protected String mRefreshToken = "";

    protected String AUTH_URL = null;
    protected String TOKEN_URL = null;
    protected String CLIENT_ID = null;
    protected String CLIENT_SECRET = null;

    public String getAuthUrl(){
        return AUTH_URL;
    }
    public String getTokenUrl(){
        return TOKEN_URL;
    }
    public String getClientId(){
        return CLIENT_ID;
    }
    public String getClientSecret(){
        return CLIENT_SECRET;
    }

    void setRefrehToken(String token){
        mRefreshToken = token;
    }

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
    public boolean getMetaData(String id, MetadataReady callback) {
        return false;
    }

    @Override
    public void getQueue(String mediaId, QueueReady callback) {

    }


    @Override
    public Fragment getSettingsFragment() {
        return null;
    }




}
