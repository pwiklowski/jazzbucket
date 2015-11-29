package wiklosoft.mediaprovider;


import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

    public void setItem(MediaBrowser.MediaItem item){
        mMediaItem = item;
    }

    private String ADD_TO_QUEUE_TOP = "Top of queue";
    private String ADD_TO_QUEUE_BOTTOM = "Bottom of queue";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Add to playlist:");
        View v = inflater.inflate(R.layout.item_menu_dialog, container, false);
        ListView menu_items = (ListView) v.findViewById(R.id.item_menu_list);


        PlaylistDatabaseHandler db = new PlaylistDatabaseHandler(getActivity());

        items.add(ADD_TO_QUEUE_TOP);
        items.add(ADD_TO_QUEUE_BOTTOM);
        items.addAll(db.getPlaylistNames());

        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(getContext(), R.layout.media_imem_context_menu_item, items);
        menu_items.setAdapter(itemsAdapter);
        menu_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = items.get(i);

                PlaylistDatabaseHandler db = new PlaylistDatabaseHandler(getContext());
                if (selectedItem.equals(ADD_TO_QUEUE_BOTTOM) || selectedItem.equals(ADD_TO_QUEUE_TOP)){
                    Queue q = MusicService.getService().getQueue();
                    if (selectedItem.equals(ADD_TO_QUEUE_BOTTOM))
                        q.add(new MediaSession.QueueItem(mMediaItem.getDescription(), 0));
                    else if (selectedItem.equals(ADD_TO_QUEUE_TOP)) {
                        int position = q.getCurrentItemId() + 1;
                        if (position <= 0)
                            position = 0;

                        q.add(position, new MediaSession.QueueItem(mMediaItem.getDescription(), 0));
                    }
                }else {
                    db.addItemToPlaylist(selectedItem, mMediaItem);
                }
                dismiss();
            }

        });
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return v;
    }




}
