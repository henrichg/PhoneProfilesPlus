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

class AddProfileAdapter extends BaseAdapter {

    public List<Profile> profileList;
    AddProfileDialog dialog;

    private Context context;

    private LayoutInflater inflater = null;

    AddProfileAdapter(AddProfileDialog dialog, Context c, List<Profile> profileList)
    {
        context = c;

        this.dialog = dialog;
        this.profileList = profileList;

        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return profileList.size();
    }

    public Object getItem(int position) {
        Profile profile;
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
        int position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            if (ApplicationPreferences.applicationEditorPrefIndicator(context))
                vi = inflater.inflate(R.layout.profile_preference_list_item, parent, false);
            else
                vi = inflater.inflate(R.layout.profile_preference_list_item_no_indicator, parent, false);

            holder = new ViewHolder();
            holder.profileIcon = (ImageView)vi.findViewById(R.id.profile_pref_dlg_item_icon);
            holder.profileLabel = (TextView)vi.findViewById(R.id.profile_pref_dlg_item_label);
            if (ApplicationPreferences.applicationEditorPrefIndicator(context))
                holder.profileIndicator = (ImageView)vi.findViewById(R.id.profile_pref_dlg_item_indicator);
            holder.radioBtn = (RadioButton)vi.findViewById(R.id.profile_pref_dlg_item_radiobtn);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        Profile profile;
        profile = profileList.get(position);

        holder.radioBtn.setTag(position);
        holder.radioBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                dialog.doOnItemSelected((Integer)rb.getTag());
            }
        });

        if (profile != null)
        {
            holder.radioBtn.setChecked(false);

            if (position == 0)
                holder.profileLabel.setText(context.getString(R.string.new_empty_profile));
            else
                holder.profileLabel.setText(profile._name);
            holder.profileIcon.setVisibility(View.VISIBLE);
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
                holder.profileIcon.setImageBitmap(profile._iconBitmap);
            if (ApplicationPreferences.applicationEditorPrefIndicator(context)) {
                holder.profileIndicator.setVisibility(View.VISIBLE);
                holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
            }
        }
        else
        {
            holder.radioBtn.setChecked(false);
            holder.profileLabel.setText("");
            holder.profileIcon.setVisibility(View.VISIBLE);
            holder.profileIcon.setImageResource(R.drawable.ic_empty);
            if (ApplicationPreferences.applicationEditorPrefIndicator(context)) {
                holder.profileIndicator.setVisibility(View.VISIBLE);
                holder.profileIndicator.setImageResource(R.drawable.ic_empty);
            }
        }

        return vi;
    }

}
