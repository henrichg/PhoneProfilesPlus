package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

class FastAccessDurationProfileAdapter extends BaseAdapter {

    private final List<Profile> profileList;
    private final long profileId;
    private final FastAccessDurationProfileDialog dialog;

    //private final Context context;

    private final LayoutInflater inflater;

    FastAccessDurationProfileAdapter(FastAccessDurationProfileDialog dialog, Context c, long profileId, List<Profile> profileList)
    {
        //context = c;

        this.dialog = dialog;
        this.profileList = profileList;

        if (profileId == -1)
            this.profileId = Profile.PROFILE_NO_ACTIVATE;
        else
            this.profileId = profileId;

        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        if (profileList == null)
            return 0;

        return profileList.size() + 1;
    }

    public Object getItem(int position) {
        Profile profile;
        if (position == 0)
            profile = null;
        else
            profile = profileList.get(position-1);
        return profile;
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView profileIcon;
        TextView profileLabel;
        ImageView profileIndicator;
        RadioButton radioBtn;
        //int position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        FastAccessDurationProfileAdapter.ViewHolder holder;

        View vi = convertView;

        boolean applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;

        if (convertView == null)
        {
            if (applicationEditorPrefIndicator)
                vi = inflater.inflate(R.layout.profile_preference_list_item, parent, false);
            else
                vi = inflater.inflate(R.layout.profile_preference_list_item_no_indicator, parent, false);

            holder = new FastAccessDurationProfileAdapter.ViewHolder();
            holder.profileIcon = vi.findViewById(R.id.profile_pref_dlg_item_icon);
            holder.profileLabel = vi.findViewById(R.id.profile_pref_dlg_item_label);
            holder.profileIndicator = vi.findViewById(R.id.profile_pref_dlg_item_indicator);
            holder.radioBtn = vi.findViewById(R.id.profile_pref_dlg_item_radiobtn);
            vi.setTag(holder);
        }
        else
        {
            holder = (FastAccessDurationProfileAdapter.ViewHolder)vi.getTag();
        }

        Profile profile;
        if (position == 0)
            profile = null;
        else
            profile = profileList.get(position-1);

        holder.radioBtn.setTag(position);
        holder.radioBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                dialog.doOnItemSelected((Integer)rb.getTag());
            }
        });

        if (profile != null)
        {
            holder.radioBtn.setChecked(profileId == profile._id);

            holder.profileLabel.setText(profile._name);
            holder.profileIcon.setVisibility(View.VISIBLE);
            if (profile.getIsIconResourceID())
            {
                if (profile._iconBitmap != null)
                    holder.profileIcon.setImageBitmap(profile._iconBitmap);
                else {
                    //holder.profileIcon.setImageBitmap(null);
                    //int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                    //        vi.getContext().getPackageName());
                    int res = Profile.getIconResource(profile.getIconIdentifier());
                    holder.profileIcon.setImageResource(res); // icon resource
                }
            }
            else
                holder.profileIcon.setImageBitmap(profile._iconBitmap);
            if (applicationEditorPrefIndicator) {
                if (holder.profileIndicator != null) {
                    if (profile._preferencesIndicator != null) {
                        holder.profileIndicator.setVisibility(View.VISIBLE);
                        holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
                    }
                    else
                        //holder.profileIndicator.setImageResource(R.drawable.ic_empty);
                        holder.profileIndicator.setVisibility(View.GONE);
                }
            }
        }
        else
        {
            if (position == 0)
            {
                holder.radioBtn.setChecked((profileId == Profile.PROFILE_NO_ACTIVATE));
                holder.profileLabel.setText(vi.getResources().getString(R.string.profile_preference_profile_end_no_activate));
                //holder.profileIcon.setImageResource(R.drawable.ic_empty);
                holder.profileIcon.setVisibility(View.GONE);
                //if (applicationEditorPrefIndicator)
                    //holder.profileIndicator.setImageResource(R.drawable.ic_empty);
                if (holder.profileIndicator != null)
                    holder.profileIndicator.setVisibility(View.GONE);
            }
            else
            {
                holder.radioBtn.setChecked(false);
                holder.profileLabel.setText("");
                holder.profileIcon.setVisibility(View.VISIBLE);
                holder.profileIcon.setImageResource(R.drawable.ic_empty);
                if (applicationEditorPrefIndicator) {
                    if (holder.profileIndicator != null) {
                        holder.profileIndicator.setVisibility(View.VISIBLE);
                        holder.profileIndicator.setImageResource(R.drawable.ic_empty);
                    }
                }
                else {
                    if (holder.profileIndicator != null)
                        holder.profileIndicator.setVisibility(View.GONE);
                }
            }
        }

        return vi;
    }

}
