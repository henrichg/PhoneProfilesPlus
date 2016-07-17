package sk.henrichg.phoneprofilesplus;

import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ActivateProfileListAdapter extends BaseAdapter
{

    private List<Profile> profileList;
    private ActivateProfileListFragment fragment;
    private DataWrapper dataWrapper;

    public ActivateProfileListAdapter(ActivateProfileListFragment f, List<Profile> pl, DataWrapper dataWrapper)
    {
        fragment = f;
        profileList = pl;
        this.dataWrapper = dataWrapper;
    }

    public void release()
    {
        fragment = null;
        profileList = null;
    }

    public int getCount()
    {
        int count = 0;
        for (Profile profile : profileList)
        {
            if (profile._showInActivator)
                ++count;
        }
        return count;
    }

    public Object getItem(int position)
    {
        if (getCount() == 0)
            return null;
        else
        {
            Profile _profile = null;

            int pos = -1;
            for (Profile profile : profileList)
            {
                if (profile._showInActivator)
                    ++pos;

                if (pos == position)
                {
                    _profile = profile;
                    break;
                }
            }

            return _profile;
        }
    }

    public long getItemId(int position)
    {
        return position;
    }


    public int getItemId(Profile profile)
    {
        for (int i = 0; i < profileList.size(); i++)
        {
            if (profileList.get(i)._id == profile._id)
                return i;
        }
        return -1;
    }

    public Profile getActivatedProfile()
    {
        for (Profile p : profileList)
        {
            if (p._checked)
            {
                return p;
            }
        }

        return null;
    }

    public void notifyDataSetChanged(boolean refreshIcons) {
        if (refreshIcons) {
            for (Profile profile : profileList) {
                dataWrapper.refreshProfileIcon(profile, false, 0);
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder {
          ViewGroup listItemRoot;
          ImageView profileIcon;
          TextView profileName;
          ImageView profileIndicator;
          int position;
        }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        View vi = convertView;
        if (convertView == null)
        {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
            if (!GlobalData.applicationActivatorGridLayout)
            {
                if (GlobalData.applicationActivatorPrefIndicator)
                    vi = inflater.inflate(R.layout.activate_profile_list_item, parent, false);
                else
                    vi = inflater.inflate(R.layout.activate_profile_list_item_no_indicator, parent, false);
                holder.listItemRoot = (RelativeLayout)vi.findViewById(R.id.act_prof_list_item_root);
                holder.profileName = (TextView)vi.findViewById(R.id.act_prof_list_item_profile_name);
                holder.profileIcon = (ImageView)vi.findViewById(R.id.act_prof_list_item_profile_icon);
                if (GlobalData.applicationActivatorPrefIndicator)
                    holder.profileIndicator = (ImageView)vi.findViewById(R.id.act_prof_list_profile_pref_indicator);
            }
            else
            {
                vi = inflater.inflate(R.layout.activate_profile_grid_item, parent, false);
                holder.listItemRoot = (LinearLayout)vi.findViewById(R.id.act_prof_list_item_root);
                holder.profileName = (TextView)vi.findViewById(R.id.act_prof_list_item_profile_name);
                holder.profileIcon = (ImageView)vi.findViewById(R.id.act_prof_list_item_profile_icon);
            }
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        final Profile profile = (Profile)getItem(position);

        if (profile._checked && (!GlobalData.applicationActivatorHeader))
        {
            if (GlobalData.applicationTheme.equals("material"))
                holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dlight);
            else
            if (GlobalData.applicationTheme.equals("dark"))
                holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dark);
            else
            if (GlobalData.applicationTheme.equals("dlight"))
                holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dlight);
            holder.profileName.setTypeface(null, Typeface.BOLD);
        }
        else
        {
            if (GlobalData.applicationTheme.equals("material"))
                holder.listItemRoot.setBackgroundResource(R.drawable.card);
            else
            if (GlobalData.applicationTheme.equals("dark"))
                holder.listItemRoot.setBackgroundResource(R.drawable.card_dark);
            else
            if (GlobalData.applicationTheme.equals("dlight"))
                holder.listItemRoot.setBackgroundResource(R.drawable.card);
            holder.profileName.setTypeface(null, Typeface.NORMAL);
        }
      
        String profileName = dataWrapper.getProfileNameWithManualIndicator(profile,
                                    (!GlobalData.applicationActivatorGridLayout) &&
                                    profile._checked &&
                                    (!GlobalData.applicationActivatorHeader), true, GlobalData.applicationActivatorGridLayout);
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

        if ((GlobalData.applicationActivatorPrefIndicator) && (!GlobalData.applicationActivatorGridLayout))
        {
            //profilePrefIndicatorImageView.setImageBitmap(null);
            //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
            //profilePrefIndicatorImageView.setImageBitmap(bitmap);
            holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
        }

        return vi;
    }

}
