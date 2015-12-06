package wiklosoft.mediaprovider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import wiklosoft.mediaprovider.providers.MusicProvider;

/**
 * Created by Pawel Wiklowski on 12.10.15.
 */
public class HamburgerAdapter extends ArrayAdapter<MusicProvider> {

    private List<MusicProvider> mItems;

    public HamburgerAdapter(Context context, List<MusicProvider> items){
        super(context, R.layout.hamburger_item, items);
        mItems = items;

    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.hamburger_item, parent, false);

        MusicProvider item = getItem(position);

        TextView title = (TextView) rowView.findViewById(R.id.title);
        title.setText(item.getName());

        ImageView icon = (ImageView) rowView.findViewById(R.id.icon);
        if (item.getIcon() != 0)
            icon.setImageResource(item.getIcon());
        return rowView;
    }
}
