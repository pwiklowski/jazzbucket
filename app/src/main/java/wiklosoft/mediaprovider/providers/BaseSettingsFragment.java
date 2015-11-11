package wiklosoft.mediaprovider.providers;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.gson.JsonObject;
import com.wuman.android.auth.AuthorizationFlow;
import com.wuman.android.auth.AuthorizationUIController;
import com.wuman.android.auth.DialogFragmentController;
import com.wuman.android.auth.OAuthManager;
import com.wuman.android.auth.oauth2.store.SharedPreferencesCredentialStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wiklosoft.mediaprovider.OAuthClient;
import wiklosoft.mediaprovider.OAuthClientAuthResult;
import wiklosoft.mediaprovider.R;

/**
 * Created by Pawel Wiklowski on 10.10.15.
 */
public class BaseSettingsFragment extends Fragment {
    private OAuthProvider mProvider = null;
    private CheckBox mIsConnected = null;
    private Button mAuth = null;
    protected int mLogo = R.mipmap.no_logo;

    public BaseSettingsFragment() {
        init();
    }

    public void init(){

    }

    public void setProvider(OAuthProvider provider){
        mProvider = provider;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dropbox_settings, container, false);
        mIsConnected = (CheckBox) rootView.findViewById(R.id.is_connected);

        mIsConnected.setChecked(!mProvider.getToken().isEmpty());

        mAuth = (Button) rootView.findViewById(R.id.auth);
        mAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authorize();
            }
        });

        ImageView logo = (ImageView) rootView.findViewById(R.id.imageView);
        logo.setImageResource(mLogo);

        return rootView;
    }


    void authorize(){

        OAuthClient c = new OAuthClient();
        c.authorize(getFragmentManager(), mProvider, new OAuthClientAuthResult() {
            @Override
            public void onAuthorize(String token, String refreshToken) {
                mProvider.setToken(token);
                mProvider.setRefrehToken(refreshToken);
                mIsConnected.setChecked(!mProvider.getToken().isEmpty());
            }
        });
    }



    @Override
    public void onPause(){
        super.onPause();
    }


}
