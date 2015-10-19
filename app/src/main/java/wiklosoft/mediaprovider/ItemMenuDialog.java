package wiklosoft.mediaprovider;


import android.media.browse.MediaBrowser;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import wiklosoft.mediaprovider.playlists.PlaylistDatabaseHandler;

/**
 * Created by Pawel Wiklowski on 18.10.15.
 */
public class ItemMenuDialog extends DialogFragment {
    List<String> items = new ArrayList<>();
    MediaBrowser.MediaItem mMediaItem = null;

    private final String ADD_TO_PLAYLIST = "Add to playlist";
    private final String ADD_TO_FAVORITES_PLAYLIST = "Add to provider favortites";

    public void setItem(MediaBrowser.MediaItem item){
        mMediaItem = item;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.item_menu_dialog, container, false);
        ListView menu_items = (ListView) v.findViewById(R.id.item_menu_list);

        items.add(ADD_TO_PLAYLIST);




        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items);
        menu_items.setAdapter(itemsAdapter);
        menu_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = items.get(i);

                if (selectedItem.equals(ADD_TO_FAVORITES_PLAYLIST)) {
                    PlaylistDatabaseHandler db = new PlaylistDatabaseHandler(getContext());
                    db.addItemToPlaylist("Favorites", mMediaItem);
                }
            }
        });
        return v;
    }




}
