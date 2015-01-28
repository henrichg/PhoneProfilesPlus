package sk.henrichg.phoneprofilesplus;

import java.util.List;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EditorProfileListAdapter extends BaseAdapter
{

	private EditorProfileListFragment fragment;
	private DataWrapper dataWrapper;
	private int filterType;
	public List<Profile> profileList;
	public boolean released = false;
	
	
	public EditorProfileListAdapter(EditorProfileListFragment f, DataWrapper pdw, int filterType)
	{
		fragment = f;
		dataWrapper = pdw;
		profileList = dataWrapper.getProfileList();
		this.filterType = filterType;
	}   
	
	public void release()
	{
		released = true;
		
		fragment = null;
		profileList = null;
		dataWrapper = null;
	}
	
	public int getCount()
	{
		if (profileList == null)
			return 0;
			
		if (filterType == EditorProfileListFragment.FILTER_TYPE_ALL)
			return profileList.size();
		
		int count = 0;
		for (Profile profile : profileList)
		{
	        switch (filterType)
	        {
				case EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR:
					if (profile._showInActivator)
						++count;
					break;
				case EditorProfileListFragment.FILTER_TYPE_NO_SHOW_IN_ACTIVATOR:
					if (!profile._showInActivator)
						++count;
					break;
	        }
		}
		return count;
	}

	public Object getItem(int position)
	{
		if (getCount() == 0)
			return null;
		else
		{
			
			if (filterType == EditorProfileListFragment.FILTER_TYPE_ALL)
				return profileList.get(position);
			
			Profile _profile = null;
			
			int pos = -1;
			for (Profile profile : profileList)
			{
				switch (filterType)
		        {
					case EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR:
						if (profile._showInActivator)
							++pos;
						break;
					case EditorProfileListFragment.FILTER_TYPE_NO_SHOW_IN_ACTIVATOR:
						if (!profile._showInActivator)
							++pos;
						break;
		        }
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
		if (profileList == null)
			return -1;
	
		for (int i = 0; i < profileList.size(); i++)
		{
			if (profileList.get(i)._id == profile._id)
				return i;
		}
		return -1;
	}
	
	public int getItemPosition(Profile profile)
	{
		if (profile == null)
			return -1;
		
		if (profileList == null)
			return -1;
		
		int pos = -1;
		
		for (int i = 0; i < profileList.size(); i++)
		{
			switch (filterType)
	        {
				case EditorProfileListFragment.FILTER_TYPE_ALL:
					++pos;
					break;
	        	case EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR:
					if (profile._showInActivator)
						++pos;
					break;
				case EditorProfileListFragment.FILTER_TYPE_NO_SHOW_IN_ACTIVATOR:
					if (!profile._showInActivator)
						++pos;
					break;
	        }
			
			if (profileList.get(i)._id == profile._id)
				return pos;
		}
		return -1;
	}
	
	public void setList(List<Profile> pl)
	{
		profileList = pl;
		notifyDataSetChanged();
	}
	
	public void addItem(Profile profile, boolean refresh)
	{
		if (profileList == null)
			return;
		
		profileList.add(profile);
		if (refresh)
			notifyDataSetChanged();
	}

	public void deleteItemNoNotify(Profile profile)
	{
		dataWrapper.deleteProfile(profile);
	}

	public void deleteItem(Profile profile)
	{
		deleteItemNoNotify(profile);
		notifyDataSetChanged();
	}

	public void clearNoNotify()
	{
		dataWrapper.deleteAllProfiles();
	}
	
	public void clear()
	{
		clearNoNotify();
		notifyDataSetChanged();
	}
	
	public void changeItemOrder(int from, int to)
	{
		if (profileList == null)
			return;
	
		// convert positions from adapter into profileList
		int plFrom = profileList.indexOf(getItem(from));
		int plTo = profileList.indexOf(getItem(to));
		
		Profile profile = profileList.get(plFrom);
		profileList.remove(plFrom);
		profileList.add(plTo, profile);
		notifyDataSetChanged();
	}
	
	public Profile getActivatedProfile()
	{
		if (profileList == null)
			return null;

		for (Profile p : profileList)
		{
			if (p._checked)
			{
				return p;
			}
		}
		
		return null;
	}
	
	public void activateProfile(Profile profile)
	{
		if (profileList == null)
			return;

		for (Profile p : profileList)
		{
			p._checked = false;
		}
		
		// teraz musime najst profile v profileList 
		int position = getItemId(profile);
		if (position != -1)
		{
			// najdenemu objektu nastavime _checked
			Profile _profile = profileList.get(position);
			if (_profile != null)
				_profile._checked = true;
		}
		notifyDataSetChanged();
	}

	static class ViewHolder {
		  RelativeLayout listItemRoot;
		  ImageView profileIcon;
		  TextView profileName;
		  ImageView profileIndicator;
		  ImageView profileItemEditMenu;
		  ImageView profileShowInActivator;
		  int position;
		}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		
		View vi = convertView;
        if (convertView == null)
        {
    	    LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
    	    if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR)
    	    {
	      	    if (GlobalData.applicationEditorPrefIndicator)
	      		    vi = inflater.inflate(R.layout.editor_profile_list_item, parent, false);
	      	    else
	      		    vi = inflater.inflate(R.layout.editor_profile_list_item_no_indicator, parent, false);
    	    }
    	    else
    	    {
	      	    if (GlobalData.applicationEditorPrefIndicator)
	      		    vi = inflater.inflate(R.layout.editor_profile_list_item_no_order_handler, parent, false);
	      	    else
	      		    vi = inflater.inflate(R.layout.editor_profile_list_item_no_indicator_no_order_handler, parent, false);
    	    }
            holder = new ViewHolder();
            holder.listItemRoot = (RelativeLayout)vi.findViewById(R.id.profile_list_item_root);
            holder.profileName = (TextView)vi.findViewById(R.id.profile_list_item_profile_name);
            holder.profileIcon = (ImageView)vi.findViewById(R.id.profile_list_item_profile_icon);
    		holder.profileItemEditMenu = (ImageView)vi.findViewById(R.id.profile_list_item_edit_menu);
  		    holder.profileShowInActivator = (ImageView)vi.findViewById(R.id.profile_list_item_show_in_activator);
  		    if (GlobalData.applicationEditorPrefIndicator)
  			    holder.profileIndicator = (ImageView)vi.findViewById(R.id.profile_list_profile_pref_indicator);
            vi.setTag(holder);        
        }
        else
        {
      	    holder = (ViewHolder)vi.getTag();
        }
        
        final Profile profile = (Profile)getItem(position);
        if (profile != null)
        {
	
	        /*
	        switch (filterType)
	        {
				case DatabaseHandler.FILTER_TYPE_PROFILES_ALL:
					vi.setVisibility(View.VISIBLE);
					break;
				case DatabaseHandler.FILTER_TYPE_PROFILES_SHOW_IN_ACTIVATOR:
					if (!profile._showInActivator)
						vi.setVisibility(View.GONE);
					else
						vi.setVisibility(View.VISIBLE);
					break;
				case DatabaseHandler.FILTER_TYPE_PROFILES_NO_SHOW_IN_ACTIVATOR:
					if (profile._showInActivator)
						vi.setVisibility(View.GONE);
					else
						vi.setVisibility(View.VISIBLE);
					break;
	        }
	        */
	
	        if (profile._checked && (!GlobalData.applicationEditorHeader))
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
					profile._checked &&
					(!GlobalData.applicationEditorHeader));
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
	        	//Resources resources = vi.getResources();
	        	//int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
	        	//int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
	        	//Bitmap bitmap = BitmapResampler.resample(profile.getIconIdentifier(), width, height);
	        	//profileIcon.setImageBitmap(bitmap);
	        	holder.profileIcon.setImageBitmap(profile._iconBitmap);
	        }
	
	        if (profile._showInActivator)
	        	holder.profileShowInActivator.setImageResource(R.drawable.ic_profile_show_in_activator_on);
	        else
	        	holder.profileShowInActivator.setImageResource(R.drawable.ic_profile_show_in_activator_off);
	        
			if (GlobalData.applicationEditorPrefIndicator)
			{
				//profilePrefIndicatorImageView.setImageBitmap(null);
				//Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
				//profilePrefIndicatorImageView.setImageBitmap(bitmap);
				holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
			}
			
	        holder.profileItemEditMenu.setTag(profile);
	        final ImageView profileItemEditMenu = holder.profileItemEditMenu;
	        holder.profileItemEditMenu.setOnClickListener(new OnClickListener() {
	
					public void onClick(View v) {
						//Log.d("EditorProfileListAdapter.onClick", "delete");
						((EditorProfileListFragment)fragment).finishProfilePreferencesActionMode();
						((EditorProfileListFragment)fragment).showEditMenu(profileItemEditMenu);
					}
				}); 
			
			//Log.d("ProfileListAdapter.getView", profile.getName());
        }
      
		return vi;
	}

}
