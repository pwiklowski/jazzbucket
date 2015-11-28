package wiklosoft.mediaprovider;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.net.Uri;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.net.URL;
import java.util.List;

import wiklosoft.mediaprovider.playlists.PlaylistDatabaseHandler;

/**
 * Created by Pawel Wiklowski on 12.10.15.
 */
public class PlaylistAdapter extends ArrayAdapter<MediaSession.QueueItem> {

    public PlaylistAdapter(Context context, List<MediaSession.QueueItem> contacts){
        super(context, R.layout.playlist_item,contacts);


    }



    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.playlist_item, parent, false);



        final MediaSession.QueueItem item = getItem(position);



        TextView title = (TextView) rowView.findViewById(R.id.title);


        title.setText(item.getDescription().getTitle());

        if (position == 5){
            //title.setTypeface(null, Typeface.BOLD);
        }

        return rowView;
    }
}
