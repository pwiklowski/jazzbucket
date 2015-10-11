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

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.jackson.JacksonFactory;
import com.wuman.android.auth.AuthorizationFlow;
import com.wuman.android.auth.AuthorizationUIController;
import com.wuman.android.auth.DialogFragmentController;
import com.wuman.android.auth.OAuthManager;
import com.wuman.android.auth.oauth2.store.SharedPreferencesCredentialStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wiklosoft.mediaprovider.R;

/**
 * Created by Pawel Wiklowski on 10.10.15.
 */
public class BaseSettingsFragment extends Fragment {
    private MusicProvider mProvider = null;
    private CheckBox mIsConnected = null;
    private Button mAuth = null;
    private OAuthManager oauth = null;
    private Credential credential = null;

    protected String AUTH_URL = null;
    protected String TOKEN_URL = null;
    protected String CLIENT_ID = null;
    protected String CLIENT_SECRET = null;

    public BaseSettingsFragment() {
        init();
    }

    public void init(){

    }

    public void setProvider(MusicProvider provider){
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

        return rootView;
    }

    List<String> getScopes(){
        return new ArrayList<String>();
    }

    void authorize(){
        final SharedPreferencesCredentialStore credentialStore =
                new SharedPreferencesCredentialStore(getActivity(), "preferenceFileName", new JacksonFactory());
        AuthorizationFlow.Builder builder = new AuthorizationFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                AndroidHttp.newCompatibleTransport(),
                new JacksonFactory(),
                new GenericUrl(TOKEN_URL),
                new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET),
                CLIENT_ID,
                AUTH_URL);
        builder.setCredentialStore(credentialStore);
        builder.setScopes(getScopes());

        AuthorizationFlow flow = builder.build();

        AuthorizationUIController controller = new DialogFragmentController(getFragmentManager()) {
            @Override
            public String getRedirectUri() throws IOException {
                return "http://localhost/Callback";
            }

            @Override
            public boolean isJavascriptEnabledForWebView() {
                return true;
            }

        };


        oauth = new OAuthManager(flow, controller);

        OAuthManager.OAuthCallback<Credential> callback = new OAuthManager.OAuthCallback<Credential>() {
            @Override public void run(OAuthManager.OAuthFuture<Credential> future) {
                try {
                    credential = future.getResult();
                    mProvider.setToken(credential.getAccessToken());
                    mIsConnected.setChecked(!mProvider.getToken().isEmpty());
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        oauth.authorizeImplicitly(mProvider.getId(), callback, null);
    }



    @Override
    public void onPause(){
        super.onPause();
    }


}
