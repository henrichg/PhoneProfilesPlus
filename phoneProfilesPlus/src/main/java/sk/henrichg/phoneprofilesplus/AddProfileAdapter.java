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

    private final List<Profile> profileList;

    private final AddProfileDialog dialog;

    private final Context context;

    //private final LayoutInflater inflater;

    AddProfileAdapter(AddProfileDialog dialog, Context c, List<Profile> profileList)
    {
        this.dialog = dialog;
        context = c;

        this.profileList = profileList;

        //inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        RadioButton radioButton;
        ImageView profileIcon;
        TextView profileLabel;
        ImageView profileIndicator;
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
                vi = LayoutInflater.from(context).inflate(R.layout.add_profile_list_item, parent, false);
            else
                vi = LayoutInflater.from(context).inflate(R.layout.add_profile_list_item_no_indicator, parent, false);

            holder = new ViewHolder();
            holder.radioButton = vi.findViewById(R.id.profile_pref_dlg_item_radio_button);
            holder.profileIcon = vi.findViewById(R.id.profile_pref_dlg_item_icon);
            holder.profileLabel = vi.findViewById(R.id.profile_pref_dlg_item_label);
            if (applicationEditorPrefIndicator)
                holder.profileIndicator = vi.findViewById(R.id.profile_pref_dlg_item_indicator);
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        Profile profile;
        profile = profileList.get(position);

        if (profile != null)
        {
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
                    //int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                    //        vi.getContext().PPApplication.PACKAGE_NAME);
                    int res = Profile.getIconResource(profile.getIconIdentifier());
                    holder.profileIcon.setImageResource(res); // icon resource
                }
            }
            else
                holder.profileIcon.setImageBitmap(profile._iconBitmap);
            if (applicationEditorPrefIndicator) {
                if (holder.profileIndicator != null) {
                    if (position == 0)
                        holder.profileIndicator.setVisibility(View.GONE);
                    else
                    if (profile._preferencesIndicator != null) {
                        holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
                        holder.profileIndicator.setVisibility(View.VISIBLE);
                    }
                    else
                        //holder.profileIndicator.setImageResource(R.drawable.ic_empty);
                        holder.profileIndicator.setVisibility(View.GONE);
                }
            }
        }
        else
        {
            holder.profileLabel.setText("");
            holder.profileIcon.setVisibility(View.VISIBLE);
            holder.profileIcon.setImageResource(R.drawable.ic_empty);
            if (applicationEditorPrefIndicator) {
                if (holder.profileIndicator != null) {
                    holder.profileIndicator.setVisibility(View.GONE);
                    //holder.profileIndicator.setImageResource(R.drawable.ic_empty);
                }
            }
        }

        holder.radioButton.setTag(position);
        holder.radioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                dialog.doOnItemSelected((Integer)rb.getTag());
            }
        });

        return vi;
    }

}
