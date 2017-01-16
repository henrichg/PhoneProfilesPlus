package sk.henrichg.phoneprofilesplus;

import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

class ShortcutProfileListAdapter extends BaseAdapter {

    private Fragment fragment;
    private List<Profile> profileList;

    ShortcutProfileListAdapter(Fragment f, List<Profile> pl)
    {
        fragment = f;
        profileList = pl;
    }

    public void release()
    {
        fragment = null;
        profileList = null;
    }

    public int getCount() {
        return profileList.size();
    }

    public Object getItem(int position) {
        return profileList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
          ImageView profileIcon;
          TextView profileName;
          ImageView profileIndicator;
          int position;
        }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        View vi = convertView;
        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
            if (PPApplication.applicationActivatorPrefIndicator)
                vi = inflater.inflate(R.layout.shortcut_list_item, parent, false);
            else
                vi = inflater.inflate(R.layout.shortcut_list_item_no_indicator, parent, false);
            holder = new ViewHolder();
            holder.profileName = (TextView)vi.findViewById(R.id.shortcut_list_item_profile_name);
            holder.profileIcon = (ImageView)vi.findViewById(R.id.shortcut_list_item_profile_icon);
            if (PPApplication.applicationActivatorPrefIndicator)
                holder.profileIndicator = (ImageView)vi.findViewById(R.id.shortcut_list_profile_pref_indicator);
            vi.setTag(holder);        
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        Profile profile = profileList.get(position);

        String profileName = profile.getProfileNameWithDuration(false, fragment.getActivity());
        holder.profileName.setText(profileName);

        if (profile.getIsIconResourceID())
        {
            if (profile._iconBitmap != null)
                holder.profileIcon.setImageBitmap(profile._iconBitmap);
            else {
                //holder.profileIcon.setImageBitmap(null);
                int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                        vi.getContext().getPackageName());
                holder.profileIcon.setImageResource(res); // resource na ikonu
            }
        }
        else
        {
            holder.profileIcon.setImageBitmap(profile._iconBitmap);
        }
        
        if (PPApplication.applicationActivatorPrefIndicator)
        {
            //profilePrefIndicatorImageView.setImageBitmap(null);
            //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
            //profilePrefIndicatorImageView.setImageBitmap(bitmap);
            holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
        }
        
        return vi;
    }

    public void setList(List<Profile> pl) {
        profileList = pl;
        notifyDataSetChanged();
    }

}
