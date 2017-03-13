package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

class ImageViewPreferenceAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater = null;
    private String imageIdentifier;
    private boolean isImageResourceID;

    ImageViewPreferenceAdapter(Context c, String imageIdentifier, boolean isImageResourceID)
    {
        context = c;

        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.imageIdentifier = imageIdentifier;
        this.isImageResourceID = isImageResourceID;
    }

    public int getCount() {
        return Profile.profileIconId.length;
    }

    public Object getItem(int position) {
        return Profile.profileIconId[position];
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
          ImageView icon;
          int position;
        }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.imageview_preference_gridview_item, parent, false);
            holder = new ViewHolder();
            holder.icon = (ImageView)vi.findViewById(R.id.imageview_preference_gridview_item_icon);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        if (Profile.profileIconId[position].equals(imageIdentifier) && isImageResourceID)
            if (ApplicationPreferences.applicationTheme(context).equals("dark"))
                holder.icon.setBackgroundResource(R.drawable.abc_list_pressed_holo_dark);
            else
                holder.icon.setBackgroundResource(R.drawable.abc_list_pressed_holo_light);
        else
            holder.icon.setBackgroundResource(0);

        holder.icon.setImageResource(context.getResources().getIdentifier(Profile.profileIconId[position], "drawable", context.getPackageName()));

        return vi;
    }

}
