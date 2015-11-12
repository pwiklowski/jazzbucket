package wiklosoft.mediaprovider;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.net.URL;
import java.util.List;

import wiklosoft.mediaprovider.playlists.Playlist;
import wiklosoft.mediaprovider.playlists.PlaylistDatabaseHandler;

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

        final MediaBrowser.MediaItem item = getItem(position);

        TextView title = (TextView) rowView.findViewById(R.id.title);
        final ImageView typeIcon = (ImageView) rowView.findViewById(R.id.item_type);
        final ToggleButton star = (ToggleButton) rowView.findViewById(R.id.star);

        if(item.isBrowsable()){
            typeIcon.setImageResource(R.mipmap.folder);
            star.setVisibility(View.INVISIBLE);
        }else if (item.isPlayable()){
            typeIcon.setImageResource(R.mipmap.music);
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PlaylistDatabaseHandler db = new PlaylistDatabaseHandler(getContext());
                    if (star.isChecked()) {
                        db.addItemToPlaylist(PlaylistDatabaseHandler.FAVORITES, item);
                        star.setBackgroundResource(R.mipmap.star_on);
                    } else {
                        db.removeItemFromPlaylist(PlaylistDatabaseHandler.FAVORITES, item.getMediaId());
                        star.setBackgroundResource(R.mipmap.star_off);
                    }
                }
            });
            star.setChecked(PlaylistDatabaseHandler.isOnFavorites(getContext(), item.getMediaId()));
            if (star.isChecked()) {
                star.setBackgroundResource(R.mipmap.star_on);
            } else {
                star.setBackgroundResource(R.mipmap.star_off);
            }
        }

        if (item.getDescription().getIconUri() != null){
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Uri u = item.getDescription().getIconUri();
                        final Bitmap b = BitmapFactory.decodeStream(new URL(u.toString()).openConnection().getInputStream());
                        ((Activity)getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                typeIcon.setImageBitmap(b);
                            }
                        });

                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            t.start();

        }


        title.setText(item.getDescription().getTitle());

        return rowView;
    }
}
