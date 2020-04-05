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

class ProfilePreferenceAdapterX extends BaseAdapter {

    final List<Profile> profileList;
    private final long profileId;
    private final ProfilePreferenceFragmentX preferenceFragment;

    private final Context context;

    //private final LayoutInflater inflater;

    ProfilePreferenceAdapterX(ProfilePreferenceFragmentX preferenceFragment, Context c, String profileId, List<Profile> profileList)
    {
        context = c;

        this.preferenceFragment = preferenceFragment;
        this.profileList = profileList;

        if (profileId.isEmpty())
            if (preferenceFragment.preference.addNoActivateItem == 1)
                this.profileId = Profile.PROFILE_NO_ACTIVATE;
            else
                this.profileId = 0;
        else
            this.profileId = Long.parseLong(profileId);

        //inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        if (profileList == null)
            return 0;

        int count = profileList.size();
        if (preferenceFragment.preference.addNoActivateItem == 1)
            count++;
        return count;
    }

    public Object getItem(int position) {
        Profile profile;
        if (preferenceFragment.preference.addNoActivateItem == 1)
        {
            if (position == 0)
                profile = null;
            else
                profile = profileList.get(position-1);
        }
        else
            profile = profileList.get(position);
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
        ViewHolder holder;

        View vi = convertView;

        boolean applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;

        if (convertView == null)
        {
            if (applicationEditorPrefIndicator)
                vi = LayoutInflater.from(context).inflate(R.layout.profile_preference_list_item, parent, false);
            else
                vi = LayoutInflater.from(context).inflate(R.layout.profile_preference_list_item_no_indicator, parent, false);

            holder = new ViewHolder();
            holder.profileIcon = vi.findViewById(R.id.profile_pref_dlg_item_icon);
            holder.profileLabel = vi.findViewById(R.id.profile_pref_dlg_item_label);
            if (applicationEditorPrefIndicator)
                holder.profileIndicator = vi.findViewById(R.id.profile_pref_dlg_item_indicator);
            holder.radioBtn = vi.findViewById(R.id.profile_pref_dlg_item_radiobtn);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        Profile profile;
        if (preferenceFragment.preference.addNoActivateItem == 1)
        {
            if (position == 0)
                profile = null;
            else
                profile = profileList.get(position-1);
        }
        else
            profile = profileList.get(position);

        holder.radioBtn.setTag(position);
        holder.radioBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                preferenceFragment.doOnItemSelected((Integer)rb.getTag());
            }
        });

        if (profile != null)
        {
            holder.radioBtn.setChecked(profileId == profile._id);

            if (preferenceFragment.preference.showDuration == 1)
                holder.profileLabel.setText(profile.getProfileNameWithDuration("", "", false, false, context.getApplicationContext()));
            else
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
                    holder.profileIndicator.setVisibility(View.VISIBLE);
                    if (profile._preferencesIndicator != null)
                        holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
                    else
                        holder.profileIndicator.setImageResource(R.drawable.ic_empty);
                }
            }
        }
        else
        {
            if ((preferenceFragment.preference.addNoActivateItem == 1) && (position == 0))
            {
                holder.radioBtn.setChecked((profileId == Profile.PROFILE_NO_ACTIVATE));
                if (preferenceFragment.preference.noActivateAsDoNotApply == 1)
                    holder.profileLabel.setText(vi.getResources().getString(R.string.profile_preference_do_not_apply));
                else
                    holder.profileLabel.setText(vi.getResources().getString(R.string.profile_preference_profile_end_no_activate));
                //holder.profileIcon.setImageResource(R.drawable.ic_empty);
                holder.profileIcon.setVisibility(View.GONE);
                if (applicationEditorPrefIndicator)
                    //holder.profileIndicator.setImageResource(R.drawable.ic_empty);
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
            }
        }

        return vi;
    }

}
