package wiklosoft.mediaprovider.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.browse.MediaBrowser;
import android.preference.PreferenceManager;
import android.service.media.MediaBrowserService;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import wiklosoft.mediaprovider.MetadataReady;
import wiklosoft.mediaprovider.MusicReady;
import wiklosoft.mediaprovider.OAuthClient;
import wiklosoft.mediaprovider.OAuthClientAuthResult;
import wiklosoft.mediaprovider.QueueReady;


/**
 * Created by Pawel Wiklowski on 08.10.15.
 */
public class OAuthProvider implements MusicProvider {
    private String REFRESH = "REFRESH_";
    private String ACCESS = "ACCESS_";
    protected String ID = null;
    protected Context mContext;


    private Date mTokenExpiration = new Date();

    protected String AUTH_URL = null;
    protected String TOKEN_URL = null;
    protected String CLIENT_ID = null;
    protected String CLIENT_SECRET = null;
    protected String CALLBACK_URL = "http://localhost/Callback";

    public Date getTokenExpiration() {
        return mTokenExpiration;
    }

    public void setTokenExpiration(Date tokenExpiration) {
        mTokenExpiration = tokenExpiration;
    }
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

    public String getRefreshToken(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPref.getString(REFRESH + ID, "");
    }

    public String getCallbackUrl(){ return CALLBACK_URL; }

    void setRefrehToken(String token){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(REFRESH + ID, token);
        editor.commit();
    }

    public OAuthProvider(String id, Context context){
        ID = id;
        mContext = context;

    }

    public String getToken(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPref.getString(ACCESS + ID, "");
    }

    public void setToken(String token){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ACCESS + ID, token);
        editor.commit();
    }
    public String getScopes(){
        return null;
    }

    public String getAuthExtras(){
        return null;
    }

    public String getGrantType(){
        return "authorization_code";
    }
    public String getResponseType(){
        return "code";
    }
    @Override
    public String getName() {
        return "No name";
    }

    void refreshToken(){
        Log.d("OauthProvider", "refreshToken");

        final OAuthClient client = new OAuthClient();


        final CountDownLatch refreshed = new CountDownLatch(1);
        client.refreshToken(mContext, OAuthProvider.this, new OAuthClientAuthResult() {
            @Override
            public void onAuthorize(String token, String refreshToken, int valid) {
                setToken(token);

                if (valid != 1) {
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.SECOND, valid);
                    setTokenExpiration(c.getTime());
                }
                refreshed.countDown();
            }
        });
        try {
            refreshed.await();
        }catch (Exception e){
            e.printStackTrace();
        }

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
