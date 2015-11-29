package wiklosoft.mediaprovider;

import android.content.Context;
import android.graphics.Typeface;
import android.media.session.MediaSession;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Pawel Wiklowski on 12.10.15.
 */
public class PlaylistAdapter extends ArrayAdapter<MediaSession.QueueItem> {

    private List<MediaSession.QueueItem> mQueueItems;

    public PlaylistAdapter(Context context, List<MediaSession.QueueItem> items){
        super(context, R.layout.playlist_item, items);
        mQueueItems = items;

    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.playlist_item, parent, false);

        MediaSession.QueueItem item = getItem(position);

        TextView title = (TextView) rowView.findViewById(R.id.title);
        title.setText(item.getDescription().getTitle());

        MediaSession.QueueItem currentItem = MusicService.getService().getQueue().getCurrentItem();
        if (currentItem != null && currentItem.equals(item)){
            title.setTypeface(null, Typeface.BOLD);
        }

        return rowView;
    }
}
