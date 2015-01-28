package sk.henrichg.phoneprofilesplus;

import java.util.List;

import android.app.Fragment;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActivateProfileListAdapter extends BaseAdapter
{

	private List<Profile> profileList;
	private Fragment fragment;
	private DataWrapper dataWrapper;
	
	public ActivateProfileListAdapter(Fragment f, List<Profile> pl, DataWrapper dataWrapper)
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
	
	static class ViewHolder {
		  RelativeLayout listItemRoot;
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
		        holder.listItemRoot = (RelativeLayout)vi.findViewById(R.id.act_prof_list_item_root);
		        holder.profileName = (TextView)vi.findViewById(R.id.act_prof_list_item_profile_name);
		        holder.profileIcon = (ImageView)vi.findViewById(R.id.act_prof_list_item_profile_icon);
			}
	        vi.setTag(holder);        
		}
		else
		{
			holder = (ViewHolder)vi.getTag();
		}

		Profile profile = (Profile)getItem(position);

		if (profile._checked && (!GlobalData.applicationActivatorHeader))
		{
			if (GlobalData.applicationTheme.equals("material"))
				holder.listItemRoot.setBackgroundResource(R.drawable.header_card);
			else
         	if (GlobalData.applicationTheme.equals("dark"))
         		holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dark);
         	else
         	if (GlobalData.applicationTheme.equals("dlight"))
         		holder.listItemRoot.setBackgroundResource(R.drawable.header_card);
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
									(!GlobalData.applicationActivatorHeader));
		holder.profileName.setText(profileName);
		
		
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
		/*	Resources resources = vi.getResources();
  			int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
  			int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
  			Bitmap bitmap = BitmapResampler.resample(profile.getIconIdentifier(), width, height);
      		profileIcon.setImageBitmap(bitmap); */
			holder.profileIcon.setImageBitmap(profile._iconBitmap);
		}

		if ((GlobalData.applicationActivatorPrefIndicator) && (!GlobalData.applicationActivatorGridLayout))
		{
			//profilePrefIndicatorImageView.setImageBitmap(null);
			//Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
			//profilePrefIndicatorImageView.setImageBitmap(bitmap);
			holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
		}

      /*ImageView profileItemEditMenu = (ImageView)vi.findViewById(R.id.act_prof_list_item_edit_menu);
		profileItemEditMenu.setTag(position);
		profileItemEditMenu.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					//Log.d("ActivateProfileListAdapter.onClick", "x");
					activity.openContextMenu(v);
				}
			});
*/		
			
		//Log.d("ActivateProfileListAdapter.onGetView", "memory usage (after complete View)=" + Debug.getNativeHeapAllocatedSize());
	
      //Log.d("ProfileListAdapter.getView", profile.getName());

		return vi;
	}

}
