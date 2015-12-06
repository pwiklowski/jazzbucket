package wiklosoft.mediaprovider.providers;

import android.app.Activity;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.Rating;
import android.media.browse.MediaBrowser;
import android.media.browse.MediaBrowser.MediaItem;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.service.media.MediaBrowserService;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wiklosoft.mediaprovider.MainActivity;
import wiklosoft.mediaprovider.MetadataReady;
import wiklosoft.mediaprovider.MusicReady;
import wiklosoft.mediaprovider.MusicService;
import wiklosoft.mediaprovider.QueueReady;
import wiklosoft.mediaprovider.R;
import wiklosoft.mediaprovider.playlists.Playlist;
import wiklosoft.mediaprovider.playlists.PlaylistDatabaseHandler;

/**
 * Created by Pawel Wiklowski on 07.10.15.
 */
public class TestMusicProvider implements MusicProvider {
    private String mId;

    HashMap<String, String> files = new HashMap<>();



    public TestMusicProvider(String id){
        mId = id;

        files.put("02 - O.S.T.R. - Nowy Dzień.mp3", "http://192.168.1.8/ostr/02%20-%20O.S.T.R.%20-%20%20Nowy%20Dzie%c5%84.mp3");
        files.put("03 - O.S.T.R. - Hybryd.mp3", "http://192.168.1.8/ostr/03%20-%20O.S.T.R.%20-%20%20Hybryd.mp3");

        files.put("04 - O.S.T.R. -  Wampiry Budzą Się Po 12.00.mp3", "http://192.168.1.8/ostr/04%20-%20O.S.T.R.%20-%20%20Wampiry%20Budz%c4%85%20Si%c4%99%20Po%2012.00.mp3");
        files.put("05 - O.S.T.R. -  Pistolet Do Skroni.mp3", "http://192.168.1.8/ostr/05%20-%20O.S.T.R.%20-%20%20Pistolet%20Do%20Skroni.mp3");
        files.put("06 - O.S.T.R. -  Podróż Zwana Życiem.mp3", "http://192.168.1.8/ostr/06%20-%20O.S.T.R.%20-%20%20Podr%c3%b3%c5%bc%20Zwana%20%c5%bbyciem.mp3");
        files.put("07 - O.S.T.R. -  Kraina Karłów.mp3", "http://192.168.1.8/ostr/07%20-%20O.S.T.R.%20-%20%20Kraina%20Kar%c5%82%c3%b3w.mp3");
        files.put("08 - O.S.T.R. -  Rise Of The Sun.mp3", "http://192.168.1.8/ostr/08%20-%20O.S.T.R.%20-%20%20Rise%20Of%20The%20Sun.mp3");
        files.put("09 - O.S.T.R. -  Post Scriptum.mp3", "http://192.168.1.8/ostr/09%20-%20O.S.T.R.%20-%20%20Post%20Scriptum.mp3");
        files.put("10 - O.S.T.R. -  Grawitacja.mp3", "http://192.168.1.8/ostr/10%20-%20O.S.T.R.%20-%20%20Grawitacja.mp3");
        files.put("11 - O.S.T.R. -  Nie Do Rozwiązania.mp3", "http://192.168.1.8/ostr/11%20-%20O.S.T.R.%20-%20%20Nie%20Do%20Rozwi%c4%85zania.mp3");
        files.put("12 - O.S.T.R. -  Ja, Ty, My, Wy, Oni.mp3", "http://192.168.1.8/ostr/12%20-%20O.S.T.R.%20-%20%20Ja,%20Ty,%20My,%20Wy,%20Oni.mp3");
        files.put("13 - O.S.T.R. -  Keep Stabbing.mp3", "http://192.168.1.8/ostr/13%20-%20O.S.T.R.%20-%20%20Keep%20Stabbing.mp3");
        files.put("14 - O.S.T.R. -  Gdybym Tylko Chciał.mp3", "http://192.168.1.8/ostr/14%20-%20O.S.T.R.%20-%20%20Gdybym%20Tylko%20Chcia%c5%82.mp3");
        files.put("15 - O.S.T.R. -  Fizyka Umysłu.mp3", "http://192.168.1.8/ostr/15%20-%20O.S.T.R.%20-%20%20Fizyka%20Umys%c5%82u.mp3");
        files.put("16 - O.S.T.R. -  Kilka Zdań O.mp3", "http://192.168.1.8/ostr/16%20-%20O.S.T.R.%20-%20%20Kilka%20Zda%c5%84%20O.mp3");
        files.put("17 - O.S.T.R. -  Lubię Być Sam.mp3", "http://192.168.1.8/ostr/17%20-%20O.S.T.R.%20-%20%20Lubi%c4%99%20By%c4%87%20Sam.mp3");

    }



    @Override
    public String getName() {
        return "Test Music Provider";
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public int getIcon() {
        return 0;
    }

    @Override
    public void getChildren(String s, MediaBrowserService.Result<List<MediaItem>> result){
        List<MediaBrowser.MediaItem> list = new ArrayList<>();

        for(String f: files.keySet()){
            list.add(new MediaItem(new MediaDescription.Builder()
                    .setMediaId(mId + "/" + f)
                    .setTitle(f)
                    .build(), MediaBrowser.MediaItem.FLAG_PLAYABLE));
        }

        result.sendResult(list);
    }





    @Override
    public void getMediaUrl(String id, MusicReady callback) {
        callback.ready(id.replace(mId +"/", ""), null);
    }

    @Override
    public boolean getMetaData(String mediaId, MetadataReady callback) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(mediaId.replace(mId +"/", ""), new HashMap<String, String>());

        MediaMetadata.Builder b = new MediaMetadata.Builder();
        b.putString(MediaMetadata.METADATA_KEY_ARTIST, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        b.putString(MediaMetadata.METADATA_KEY_TITLE, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        b.putLong(MediaMetadata.METADATA_KEY_DURATION, Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));

        mmr.release();
        callback.ready(b.build());
        return true;
    }

    @Override
    public void getQueue(String mediaId, QueueReady callback) {

    }



    @Override
    public Fragment getSettingsFragment() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    public static class SettingsFragment extends Fragment {

        public SettingsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.test_settings, container, false);
            return rootView;
        }
    }
}
