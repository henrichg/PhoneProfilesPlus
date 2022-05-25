package sk.henrichg.phoneprofilesplus;

import android.content.ComponentName;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

class LiveWallpapersDialogPreferenceAdapterX extends BaseAdapter
{
    private final LiveWallpapersDialogPreferenceX preference;

    private final LayoutInflater inflater;

    LiveWallpapersDialogPreferenceAdapterX(Context context, LiveWallpapersDialogPreferenceX preference)
    {
        this.preference = preference;

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return preference.liveWallpapersList.size();
    }

    public Object getItem(int position) {
        return preference.liveWallpapersList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    static class ViewHolder {
        TextView wallpaperName;
        RadioButton radioButton;
        //int position;
    }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        // SSID to display
        LiveWallpapersData liveWallpaper = preference.liveWallpapersList.get(position);
        //System.out.println(String.valueOf(position));

        ViewHolder holder;
        
        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.live_wallpapers_preference_list_item, parent, false);
            holder = new ViewHolder();
            holder.wallpaperName = vi.findViewById(R.id.live_wallpapers_pref_dlg_item_label);
            holder.radioButton = vi.findViewById(R.id.live_wallpapers_pref_dlg_item_radiobutton);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        holder.wallpaperName.setText(liveWallpaper.wallpaperName);

        if (liveWallpaper.componentName != null) {
            holder.radioButton.setTag(position);
            holder.radioButton.setVisibility(View.VISIBLE);
            holder.radioButton.setChecked(preference.value.equals(liveWallpaper.componentName.flattenToString()));
            holder.radioButton.setOnClickListener(v -> {
                RadioButton rb = (RadioButton) v;
                ComponentName componentName = preference.liveWallpapersList.get((Integer) rb.getTag()).componentName;
                if (componentName != null) {
                    preference.value = componentName.flattenToString();
                    notifyDataSetChanged();
                }
            });
        } else {
            holder.radioButton.setTag(position);
            holder.radioButton.setVisibility(View.GONE);
        }

        return vi;
    }

}
