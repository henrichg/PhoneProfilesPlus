package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

class ConfiguredProfilePreferencesAdapterX extends BaseAdapter
{
    private final ConfiguredProfilePreferencesDialogPreferenceX preference;
    private final Context context;

    private final LayoutInflater inflater;

    ConfiguredProfilePreferencesAdapterX(Context context, ConfiguredProfilePreferencesDialogPreferenceX preference)
    {
        this.preference = preference;
        this.context = context;

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return preference.preferencesList.size();
    }

    public Object getItem(int position) {
        return preference.preferencesList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    static class ViewHolder {
        ImageView preferenceIcon;
        ImageView preferenceIcon2;
        TextView preferenceString;
        TextView preferenceDescription;
        //int position;
    }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ConfiguredProfilePreferencesData configuredPreferences = preference.preferencesList.get(position);

        ViewHolder holder;
        
        View vi = convertView;
        if (convertView == null)
        {
            vi = inflater.inflate(R.layout.configured_profile_preferences_list_item, parent, false);
            holder = new ViewHolder();
            holder.preferenceIcon = vi.findViewById(R.id.configured_profile_preferences_preference_icon);
            holder.preferenceIcon2 = vi.findViewById(R.id.configured_profile_preferences_preference_icon2);
            holder.preferenceString = vi.findViewById(R.id.configured_profile_preferences_preference_string);
            holder.preferenceDescription = vi.findViewById(R.id.configured_profile_preferences_preference_decription);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        String applicationTheme = ApplicationPreferences.applicationTheme(context.getApplicationContext(), true);

        if (configuredPreferences.preferenceIcon == 0) {
            holder.preferenceIcon.setVisibility(View.GONE);
        }
        else {
            holder.preferenceIcon.setImageResource(configuredPreferences.preferenceIcon);

            if (applicationTheme.equals("dark")) {
                if (configuredPreferences.preferenceIconDisabled)
                    holder.preferenceIcon.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_dark), PorterDuff.Mode.SRC_ATOP);
                else
                    holder.preferenceIcon.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP);
            } else {
                if (configuredPreferences.preferenceIconDisabled)
                    holder.preferenceIcon.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_light), PorterDuff.Mode.SRC_ATOP);
                else
                    holder.preferenceIcon.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP);
            }

            holder.preferenceIcon.setVisibility(View.VISIBLE);
        }
        if (configuredPreferences.preferenceIcon2 == 0) {
            holder.preferenceIcon2.setVisibility(View.GONE);
        }
        else {
            holder.preferenceIcon2.setImageResource(configuredPreferences.preferenceIcon2);

            if (applicationTheme.equals("dark")) {
                if (configuredPreferences.preferenceIcon2Disabled)
                    holder.preferenceIcon2.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_dark), PorterDuff.Mode.SRC_ATOP);
                else
                    holder.preferenceIcon2.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_dark), PorterDuff.Mode.SRC_ATOP);
            } else {
                if (configuredPreferences.preferenceIcon2Disabled)
                    holder.preferenceIcon2.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColorDisabled_light), PorterDuff.Mode.SRC_ATOP);
                else
                    holder.preferenceIcon2.setColorFilter(ContextCompat.getColor(context, R.color.profileindicatorColor_light), PorterDuff.Mode.SRC_ATOP);
            }

            holder.preferenceIcon2.setVisibility(View.VISIBLE);
        }

        if (configuredPreferences.preferenceIcon == 0) {
            holder.preferenceString.setVisibility(View.GONE);
        }
        else {
            holder.preferenceString.setText(configuredPreferences.preferenceString);
            holder.preferenceString.setVisibility(View.VISIBLE);
        }
        holder.preferenceDescription.setText(configuredPreferences.preferenceDecription);

        return vi;
    }

}
