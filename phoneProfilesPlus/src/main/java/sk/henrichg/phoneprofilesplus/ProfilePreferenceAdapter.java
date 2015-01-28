package sk.henrichg.phoneprofilesplus;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

public class ProfilePreferenceAdapter extends BaseAdapter {

	public List<Profile> profileList;
	long profileId;
	ProfilePreferenceDialog dialog;
	
	//private Context context;
	
	private LayoutInflater inflater = null;
	
	public ProfilePreferenceAdapter(ProfilePreferenceDialog dialog, Context c, String profileId, List<Profile> profileList)
	{
		//context = c;

		this.dialog = dialog;
		this.profileList = profileList;
		
		if (profileId.isEmpty())
			this.profileId = 0;
		else
			this.profileId = Long.valueOf(profileId);
			
		inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public int getCount() {
		int count = profileList.size();
		if (dialog.addNoActivateItem == 1)
			count++;
		return count;
	}

	public Object getItem(int position) {
		Profile profile;
		if (dialog.addNoActivateItem == 1)
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
		  int position;
		}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		
		View vi = convertView;
	    if (convertView == null)
	    {
	      	if (GlobalData.applicationEditorPrefIndicator)
	      		vi = inflater.inflate(R.layout.profile_preference_list_item, parent, false);
	      	else
	      		vi = inflater.inflate(R.layout.profile_preference_list_item_no_indicator, parent, false);
	      	
	        holder = new ViewHolder();
	  		holder.profileIcon = (ImageView)vi.findViewById(R.id.profile_pref_dlg_item_icon);
	  		holder.profileLabel = (TextView)vi.findViewById(R.id.profile_pref_dlg_item_label);
	  		if (GlobalData.applicationEditorPrefIndicator)
	  			holder.profileIndicator = (ImageView)vi.findViewById(R.id.profile_pref_dlg_item_indicator);
	        holder.radioBtn = (RadioButton)vi.findViewById(R.id.profile_pref_dlg_item_radiobtn);
	        vi.setTag(holder);        
	    }
	    else
	    {
	      	holder = (ViewHolder)vi.getTag();
	    }
	    
	    Profile profile;
	    if (dialog.addNoActivateItem == 1)
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
            	dialog.doOnItemSelected((Integer)rb.getTag());
            }
        });
	    
	    if (profile != null)
	    {
	    	holder.radioBtn.setChecked(profileId == profile._id);

	    	holder.profileLabel.setText(profile._name);
		    if (profile.getIsIconResourceID())
		    {
		      	holder.profileIcon.setImageResource(0);
		      	int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable", 
		      				vi.getContext().getPackageName());
		      	holder.profileIcon.setImageResource(res); // resource na ikonu
		    }
		    else
		      	holder.profileIcon.setImageBitmap(profile._iconBitmap);
			if (GlobalData.applicationEditorPrefIndicator)
				holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
	    }
	    else
	    {
	    	if ((dialog.addNoActivateItem == 1) && (position == 0))
	    	{
		    	holder.radioBtn.setChecked((profileId == GlobalData.PROFILE_NO_ACTIVATE));
		    	holder.profileLabel.setText(vi.getResources().getString(R.string.profile_preference_profile_end_no_activate));
		    	holder.profileIcon.setImageResource(R.drawable.ic_profile_default);
				if (GlobalData.applicationEditorPrefIndicator)
					holder.profileIndicator.setImageResource(R.drawable.ic_empty);
	    	}
	    	else
	    	{
		    	holder.radioBtn.setChecked(false);
		    	holder.profileLabel.setText("");
		    	holder.profileIcon.setImageResource(R.drawable.ic_empty);
				if (GlobalData.applicationEditorPrefIndicator)
					holder.profileIndicator.setImageResource(R.drawable.ic_empty);
	    	}
	    }
	    
		return vi;
	}

}
