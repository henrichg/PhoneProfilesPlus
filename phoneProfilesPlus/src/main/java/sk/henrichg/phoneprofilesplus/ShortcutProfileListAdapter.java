package sk.henrichg.phoneprofilesplus;

import java.util.List;

import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ShortcutProfileListAdapter extends BaseAdapter {

	private Fragment fragment;
	private List<Profile> profileList;
	
	public ShortcutProfileListAdapter(Fragment f, List<Profile> pl)
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
        	if (GlobalData.applicationActivatorPrefIndicator)
        		vi = inflater.inflate(R.layout.shortcut_list_item, parent, false);
        	else
        		vi = inflater.inflate(R.layout.shortcut_list_item_no_indicator, parent, false);
            holder = new ViewHolder();
            holder.profileName = (TextView)vi.findViewById(R.id.shortcut_list_item_profile_name);
            holder.profileIcon = (ImageView)vi.findViewById(R.id.shortcut_list_item_profile_icon);
    		if (GlobalData.applicationActivatorPrefIndicator)
    			holder.profileIndicator = (ImageView)vi.findViewById(R.id.shortcut_list_profile_pref_indicator);
            vi.setTag(holder);        
        }
        else
        {
        	holder = (ViewHolder)vi.getTag();
        }

        Profile profile = profileList.get(position);
        
        holder.profileName.setText(profile._name);
        if (profile.getIsIconResourceID())
        {
        	holder.profileIcon.setImageResource(0);
        	int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable", 
        				vi.getContext().getPackageName());
        	holder.profileIcon.setImageResource(res); // resource na ikonu
        }
        else
        {
        	//profileIcon.setImageBitmap(null);
    		//Resources resources = vi.getResources();
    		//int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
    		//int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
    		//Bitmap bitmap = BitmapResampler.resample(profile.getIconIdentifier(), width, height);
        	//profileIcon.setImageBitmap(bitmap);
        	holder.profileIcon.setImageBitmap(profile._iconBitmap);
        }
        
		if (GlobalData.applicationActivatorPrefIndicator)
		{
			//profilePrefIndicatorImageView.setImageBitmap(null);
			//Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
			//profilePrefIndicatorImageView.setImageBitmap(bitmap);
			holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
		}
        
        //Log.d("ShortcutProfileListAdapter.getView", profile.getName());
        
		return vi;
	}

	public void setList(List<Profile> pl) {
		profileList = pl;
		notifyDataSetChanged();
	}

}
