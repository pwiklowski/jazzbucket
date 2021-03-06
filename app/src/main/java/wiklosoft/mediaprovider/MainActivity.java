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
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
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


public class MainActivity extends FragmentActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, PlayerFragment.OnPlaylistShow {
    private PlayerFragment mPlayerFragment = null;
    private MediaBrowser mMediaBrowser;
    private PlaylistFragment mPlaylistFragment = null;
    private ActionBarFragment mActionBar = null;
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

        mActionBar = new ActionBarFragment();
        mActionBar.setOnDrawerOpen(new ActionBarFragment.OnDrawerOpen() {
            @Override
            public void open(boolean open) {
                if (open){
                    mNavigationDrawerFragment.getDrawerLayout().openDrawer(Gravity.LEFT);
                }else{
                    mNavigationDrawerFragment.getDrawerLayout().closeDrawer(Gravity.LEFT);
                }
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.actionbar, mActionBar).commit();


        mPlayerFragment.setOnPlaylistShowListener(this);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.blueish_dark));
        window.setNavigationBarColor(getResources().getColor(R.color.blueish_dark));
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
            if (mActionBar != null){
                mActionBar.onPlaybackStateChanged(state);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            if (mPlayerFragment != null){
                mPlayerFragment.onMetadataChanged(metadata);
            }
            if (mActionBar != null){
                mActionBar.onMetadataChanged(metadata);
            }
        }
    };


    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "onServiceConnected");
            mMusicService = MusicService.getService();
            mMusicProviderList = mMusicService.getMusicProviders();
            mNavigationDrawerFragment.setItems(mMusicProviderList);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    };


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.d(TAG, "onNavigationDrawerItemSelected " + position);

        if (position != NavigationDrawerFragment.SETTINGS_ID) {
            MusicProvider mp = mMusicProviderList.get(position);

            MediaView mv = new MediaView();
            mv.setMediaId(mp.getId());
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            transaction.replace(R.id.container, mv).addToBackStack("").commit();

        }else {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).addToBackStack("").commit();
        }
    }

    @Override
    public void onShow(boolean show) {
        if (mPlaylistFragment == null) {
            mPlaylistFragment = new PlaylistFragment();
            mPlaylistFragment.setMusicService(mMusicService);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom,
                R.anim.slide_in_bottom, R.anim.slide_out_bottom);

        if (mPlaylistFragment.isAdded()) {
            getSupportFragmentManager().popBackStack();
            mPlaylistFragment = null;
        } else {
            transaction.add(R.id.container, mPlaylistFragment).addToBackStack("").commit();
        }
    }
}
