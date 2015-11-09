package wiklosoft.mediaprovider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.graphics.Typeface;
import android.media.MediaDescription;
import android.support.v4.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wiklosoft.mediaprovider.playlists.Playlist;
import wiklosoft.mediaprovider.playlists.PlaylistDatabaseHandler;
import wiklosoft.mediaprovider.providers.DibbleProvider;
import wiklosoft.mediaprovider.providers.DropboxProvider;
import wiklosoft.mediaprovider.providers.GoogleDriveProvider;
import wiklosoft.mediaprovider.providers.LocalFilesProvider;
import wiklosoft.mediaprovider.providers.MusicProvider;
import wiklosoft.mediaprovider.providers.SoundCloudProvider;
import wiklosoft.mediaprovider.providers.YoutubeProvider;


public class MainActivity extends FragmentActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private PlayerFragment mPlayerFragment = null;
    private MediaBrowser mMediaBrowser;
    private MusicProvider mProvider = null;
    private String TAG = "MainActivity";
    private MusicService mMusicService = null;
    private List<MusicProvider> mMusicProviderList = new ArrayList<>();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarFont();
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        Intent i = new Intent(this, MusicService.class);
        i.setAction("android.media.browse.MediaBrowserService");

        bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(i);

        mMediaBrowser = new MediaBrowser(this, new ComponentName(this, MusicService.class), mConnectionCallback, null);
        mMediaBrowser.connect();



        mPlayerFragment = new PlayerFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.player, mPlayerFragment).commit();

    }
    void setActionBarFont(){
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView yourTextView = (TextView) findViewById(titleId);
        Typeface font2 =  Typeface.create("sans-serif-light", Typeface.NORMAL);
        yourTextView.setTypeface(font2);
        yourTextView.setTextSize(20);
    }

    private final MediaBrowser.ConnectionCallback mConnectionCallback = new MediaBrowser.ConnectionCallback() {
        @Override
        public void onConnected() {
            connectToSession(mMediaBrowser.getSessionToken());
        }
    };
    private void connectToSession(MediaSession.Token token) {
        MediaController mediaController = new MediaController(this, token);
        setMediaController(mediaController);
        mediaController.registerCallback(mMediaControllerCallback);


        MediaView mv = new MediaView();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.replace(R.id.container, mv).commit();
    }
    public MediaBrowser getMediaBrowser(){
        return mMediaBrowser;
    }

    private final MediaController.Callback mMediaControllerCallback =
    new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackState state) {
            if (mPlayerFragment != null){
                mPlayerFragment.onPlaybackStateChanged(state);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            if (mPlayerFragment != null){
                mPlayerFragment.onMetadataChanged(metadata);
            }
        }
    };


    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "onServiceConnected");

            mMusicService = MusicService.getService();
            mMusicProviderList = mMusicService.getMusicProviders();




            String[] names = new String[mMusicProviderList.size()+1];
            for(int i=0; i<mMusicProviderList.size();i++)
                names[i] =mMusicProviderList.get(i).getName();

            names[mMusicProviderList.size()] = "Settings";

            mNavigationDrawerFragment.setItems(names);


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    };


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.d(TAG,"onNavigationDrawerItemSelected "+ position);

        if (position < mMusicProviderList.size()) {
            MusicProvider mp = mMusicProviderList.get(position);
            mTitle = mp.getName();

            MediaView mv = new MediaView();
            mv.setMediaId(mp.getId());
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            transaction.replace(R.id.container, mv).addToBackStack("").commit();

        }else {
            mTitle = "Settings";
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).addToBackStack("").commit();
        }
    }


    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
