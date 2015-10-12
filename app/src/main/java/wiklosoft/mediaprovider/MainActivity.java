package wiklosoft.mediaprovider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import wiklosoft.mediaprovider.providers.DibbleProvider;
import wiklosoft.mediaprovider.providers.DropboxProvider;
import wiklosoft.mediaprovider.providers.GoogleDriveProvider;
import wiklosoft.mediaprovider.providers.MusicProvider;
import wiklosoft.mediaprovider.providers.SoundCloudProvider;


public class MainActivity extends FragmentActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

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

    }
    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "onServiceConnected");

            mMusicService = MusicService.getService();
            mMusicProviderList = mMusicService.getMusicProviders();



            mMusicProviderList.add(new DropboxProvider(mMusicService));
            mMusicProviderList.add(new GoogleDriveProvider(mMusicService));
            mMusicProviderList.add(new SoundCloudProvider(mMusicService));
            mMusicProviderList.add(new DibbleProvider(mMusicService));


            String[] names = new String[mMusicProviderList.size()];
            for(int i=0; i<mMusicProviderList.size();i++)
                names[i] =mMusicProviderList.get(i).getName();

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
        MusicProvider mp = mMusicProviderList.get(position);
        mTitle = mp.getName();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mp.getSettingsFragment()).commit();
    }

    public void onSectionAttached(int number) {
        if (mMusicProviderList.size() >0)
            mTitle = mMusicProviderList.get(number).getName();
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
