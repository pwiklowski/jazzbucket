package wiklosoft.mediaprovider;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.JsonObject;
import com.kodart.httpzoid.Http;
import com.kodart.httpzoid.HttpFactory;
import com.kodart.httpzoid.HttpResponse;
import com.kodart.httpzoid.NetworkError;
import com.kodart.httpzoid.ResponseHandler;

import java.net.URLEncoder;

import wiklosoft.mediaprovider.providers.OAuthProvider;


/**
 * Created by Pawel Wiklowski on 10.11.15.
 */


public class OAuthClientWebView extends DialogFragment {
    private String TAG = "OAuthClientWebView";
    WebView mWebView = null;
    OAuthProvider mOAuthProvider = null;
    OAuthClientAuthResult mCallback = null;


    public void setAuthCallback(OAuthClientAuthResult callback){
        mCallback = callback;
    }

    public void setProvider(OAuthProvider provider){
        mOAuthProvider = provider;
    }
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.oauth_dialog, container, false);

        mWebView = (WebView) v.findViewById(R.id.webView);
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                if (url.startsWith(mOAuthProvider.getCallbackUrl()) && url.contains("code")){
                    Log.d(TAG, "Request token" + url);

                    dismiss();
                    String code = url.split("code=")[1];


                    String postData = "";
                    postData += "client_id=" +mOAuthProvider.getClientId();
                    postData += "&client_secret=" +mOAuthProvider.getClientSecret();
                    postData += "&grant_type=" + mOAuthProvider.getGrantType();
                    postData += "&redirect_uri=" +mOAuthProvider.getCallbackUrl();
                    postData += "&code=" + code;

                    //postData = Uri.encode(postData);

                    Http http = HttpFactory.create(getActivity());
                    http.post(mOAuthProvider.getTokenUrl())
                        .data(postData)
                        .contentType("application/x-www-form-urlencoded")
                        .handler(new ResponseHandler<JsonObject>() {
                            @Override
                            public void success(JsonObject result, HttpResponse response) {
                                Log.d(TAG, "succes " + result);
                                String token = result.get("access_token").getAsString();
                                String refreshToken = null;

                                int valid = -1;
                                if (result.has("expires_in"))
                                    valid = result.get("expires_in").getAsInt();

                                if (result.has("refresh_token"))
                                    refreshToken = result.get("refresh_token").getAsString();

                                if (mCallback != null)
                                    mCallback.onAuthorize(token, refreshToken, valid);
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

            }
        });

        String url = mOAuthProvider.getAuthUrl();
        url += "?client_id=" +mOAuthProvider.getClientId();
        url += "&redirect_uri=" +mOAuthProvider.getCallbackUrl();
        url += "&response_type=" + mOAuthProvider.getResponseType();

        if(mOAuthProvider.getScopes() != null)
            url += "&scope=" + mOAuthProvider.getScopes();

        if (mOAuthProvider.getAuthExtras() != null)
            url += mOAuthProvider.getAuthExtras();

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(url);


        resizeWindow(v);
        return v;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
    private void resizeWindow(View v){
        Rect displayRectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        v.setMinimumWidth((int) (displayRectangle.width() * 0.7f));
        v.setMinimumHeight((int) (displayRectangle.height() * 0.7f));
    }
}

