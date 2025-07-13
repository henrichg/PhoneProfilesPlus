package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

class ActivityLogActivatedProfileFilterAdapter extends BaseAdapter {

    private final List<Profile> profileList;
    private final long profileId;
    private final ActivityLogActivatedProfileFilterDialog dialog;

    private final Context context;

    //private final LayoutInflater inflater;

    ActivityLogActivatedProfileFilterAdapter(ActivityLogActivatedProfileFilterDialog dialog, Context c, long profileId, List<Profile> profileList)
    {
        context = c;

        this.dialog = dialog;
        this.profileList = profileList;

        if (profileId == -1)
            this.profileId = Profile.PROFILE_NO_ACTIVATE;
        else
            this.profileId = profileId;

        //inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    private static class ViewHolder {
        ImageView profileIcon;
        TextView profileLabel;
        ImageView profileIndicator;
        RadioButton radioBtn;
        //int position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ActivityLogActivatedProfileFilterAdapter.ViewHolder holder;

        View vi = convertView;

        boolean applicationEditorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;

        if (convertView == null)
        {
            if (applicationEditorPrefIndicator)
                vi = LayoutInflater.from(context).inflate(R.layout.listitem_profile_preference, parent, false);
            else
                vi = LayoutInflater.from(context).inflate(R.layout.listitem_profile_preference_no_indicator, parent, false);

            holder = new ActivityLogActivatedProfileFilterAdapter.ViewHolder();
            holder.profileIcon = vi.findViewById(R.id.profile_pref_dlg_item_icon);
            holder.profileLabel = vi.findViewById(R.id.profile_pref_dlg_item_label);
            holder.profileIndicator = vi.findViewById(R.id.profile_pref_dlg_item_indicator);
            holder.radioBtn = vi.findViewById(R.id.profile_pref_dlg_item_radiobtn);
            vi.setTag(holder);
        }
        else
        {
            holder = (ActivityLogActivatedProfileFilterAdapter.ViewHolder)vi.getTag();
        }

        Profile profile;
        if (position == 0)
            profile = null;
        else
            profile = profileList.get(position-1);

        holder.radioBtn.setTag(position);
        holder.radioBtn.setOnClickListener(v -> {
            RadioButton rb = (RadioButton) v;
            dialog.doOnItemSelected((Integer)rb.getTag());
        });

        if (profile != null)
        {
            holder.radioBtn.setChecked(profileId == profile._id);

            holder.profileLabel.setText(profile._name);
            holder.profileIcon.setVisibility(View.VISIBLE);
            if (profile.getIsIconResourceID())
            {
                Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(dialog.getActivity(), profile._iconBitmap);
                if (bitmap != null)
                    holder.profileIcon.setImageBitmap(bitmap);
                else {
                    if (profile._iconBitmap != null)
                        holder.profileIcon.setImageBitmap(profile._iconBitmap);
                    else {
                        //holder.profileIcon.setImageBitmap(null);
                        //int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                        //        vi.getContext().PPApplication.PACKAGE_NAME);
                        int res = ProfileStatic.getIconResource(profile.getIconIdentifier());
                        holder.profileIcon.setImageResource(res); // icon resource
                    }
                }
            }
            else {
                //Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(dialog.activity, profile._iconBitmap);
                //Bitmap bitmap = profile._iconBitmap;
                //if (bitmap != null)
                //    holder.profileIcon.setImageBitmap(bitmap);
                //else
                holder.profileIcon.setImageBitmap(profile._iconBitmap);
            }
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
                holder.profileLabel.setText(vi.getResources().getString(R.string.activity_log_filter_profile_activation_all_profiles));
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
