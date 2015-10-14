package wiklosoft.mediaprovider;

import android.content.Context;
import android.media.browse.MediaBrowser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Pawel Wiklowski on 12.10.15.
 */
public class MediaViewAdapter extends ArrayAdapter<MediaBrowser.MediaItem> {


    public MediaViewAdapter(Context context, List<MediaBrowser.MediaItem> contacts){
        super(context, R.layout.media_item,contacts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.media_item, parent, false);

        MediaBrowser.MediaItem item = getItem(position);

        TextView title = (TextView) rowView.findViewById(R.id.title);

        title.setText(item.getDescription().getTitle());

        return rowView;
    }
}
