package wiklosoft.mediaprovider.providers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.util.Calendar;

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
            public void onAuthorize(String token, String refreshToken, int valid) {
                mProvider.setToken(token);
                mProvider.setRefrehToken(refreshToken);

                if (valid != 1){
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.SECOND, valid);
                    mProvider.setTokenExpiration(c.getTime());
                }

                mIsConnected.setChecked(!mProvider.getToken().isEmpty());
            }
        });
    }



    @Override
    public void onPause(){
        super.onPause();
    }


}
