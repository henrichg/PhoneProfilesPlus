package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import java.util.List;

class ProfileMultiSelectPreferenceAdapterX extends BaseAdapter {

    private final List<Profile> profileList;

    private final Context context;

    //private final LayoutInflater inflater;

    ProfileMultiSelectPreferenceAdapterX(Context c, List<Profile> profileList)
    {
        context = c;

        this.profileList = profileList;

        //inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        if (profileList == null)
            return 0;

        return profileList.size();
    }

    public Object getItem(int position) {
        return profileList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ProfilesViewHolder holder;

        View vi = convertView;

        boolean applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;

        if (convertView == null)
        {
            if (applicationEditorPrefIndicator)
                vi = LayoutInflater.from(context).inflate(R.layout.profile_multiselect_pref_list_item, parent, false);
            else
                vi = LayoutInflater.from(context).inflate(R.layout.profile_multiselect_pref_list_item_no_indicator, parent, false);

            holder = new ProfilesViewHolder();
            holder.profileIcon = vi.findViewById(R.id.profile_multiselect_pref_dlg_item_icon);
            holder.profileName = vi.findViewById(R.id.profile_multiselect_pref_dlg_item_label);
            if (applicationEditorPrefIndicator)
                holder.preferencesIndicator = vi.findViewById(R.id.profile_multiselect_pref_dlg_item_indicator);
            holder.checkBox = vi.findViewById(R.id.profile_multiselect_pref_dlg_item_checkbox);
            vi.setTag(holder);

            holder.checkBox.setOnClickListener(v -> {
                CheckBox cb = (CheckBox) v;
                Profile profile = profileList.get((int)cb.getTag());
                profile._checked = cb.isChecked();
            });

        }
        else
        {
            holder = (ProfilesViewHolder)vi.getTag();
        }

        Profile profile;
        profile = profileList.get(position);

        holder.checkBox.setTag(position);

        if (profile != null)
        {
            holder.checkBox.setChecked(profile._checked);

            holder.profileName.setText(profile._name);
            holder.profileIcon.setVisibility(View.VISIBLE);
            if (profile.getIsIconResourceID())
            {
                if (profile._iconBitmap != null)
                    holder.profileIcon.setImageBitmap(profile._iconBitmap);
                else {
                    //holder.profileIcon.setImageBitmap(null);
                    //int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                    //        vi.getContext().PPApplication.PACKAGE_NAME);
                    int res = Profile.getIconResource(profile.getIconIdentifier());
                    holder.profileIcon.setImageResource(res); // icon resource
                }
            }
            else
                holder.profileIcon.setImageBitmap(profile._iconBitmap);
            if (applicationEditorPrefIndicator) {
                if (holder.preferencesIndicator != null) {
                    holder.preferencesIndicator.setVisibility(View.VISIBLE);
                    if (profile._preferencesIndicator != null)
                        holder.preferencesIndicator.setImageBitmap(profile._preferencesIndicator);
                    else
                        holder.preferencesIndicator.setImageResource(R.drawable.ic_empty);
                }
            }
        }
        else
        {
            holder.checkBox.setChecked(false);
            holder.profileName.setText("");
            holder.profileIcon.setVisibility(View.VISIBLE);
            holder.profileIcon.setImageResource(R.drawable.ic_empty);
            if (applicationEditorPrefIndicator) {
                if (holder.preferencesIndicator != null) {
                    holder.preferencesIndicator.setVisibility(View.VISIBLE);
                    holder.preferencesIndicator.setImageResource(R.drawable.ic_empty);
                }
            }
        }

        return vi;
    }

}
