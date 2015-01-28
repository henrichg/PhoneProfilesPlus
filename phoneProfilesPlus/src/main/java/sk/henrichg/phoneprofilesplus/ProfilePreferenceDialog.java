package sk.henrichg.phoneprofilesplus;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
//import android.preference.Preference;
//import android.preference.Preference.OnPreferenceChangeListener;
import android.view.View;


public class ProfilePreferenceDialog extends Dialog
{

	public ProfilePreference profilePreference;
	public int addNoActivateItem;
	private ProfilePreferenceAdapter profilePreferenceAdapter;
	
	public List<Profile> profileList;
	String profileId;
	
	private Context _context;
	
	private ListView listView;

	public ProfilePreferenceDialog(Context context) {
		super(context);
	}
	
	public ProfilePreferenceDialog(Context context, ProfilePreference preference, String profileId)
	{
		super(context);
		
		profilePreference = preference;

		this.profileId = profileId;
		profileList = ProfilePreference.dataWrapper.getProfileList();
	    Collections.sort(profileList, new AlphabeticallyComparator());
		
		addNoActivateItem = profilePreference.addNoActivateItem;


		_context = context;
		
		setContentView(R.layout.activity_profile_pref_dialog);
		
		listView = (ListView)findViewById(R.id.profile_pref_dlg_listview);
		
		profileList = ProfilePreference.dataWrapper.getProfileList();
	    Collections.sort(profileList, new AlphabeticallyComparator());
		
		profilePreferenceAdapter = new ProfilePreferenceAdapter(this, _context, profileId, profileList); 
		listView.setAdapter(profilePreferenceAdapter);

		int position;
		long iProfileId;
		if (profileId.isEmpty())
			iProfileId = 0;
		else
			iProfileId = Long.valueOf(profileId);
	    if ((addNoActivateItem == 1) && (iProfileId == GlobalData.PROFILE_NO_ACTIVATE))
	    	position = 0;
	    else
	    {
	    	boolean found = false;
	    	position = 0;
	    	for (Profile profile : profileList)
	    	{
	    		if (profile._id == iProfileId)
	    		{
	    			found = true;
	    			break;
	    		}
	    		position++;
	    	}
	    	if (found)
	    	{
	    		if (addNoActivateItem == 1)
	    			position++;
	    	}
	    	else
	    		position = 0;
	    }
		listView.setSelection(position);
		listView.setItemChecked(position, true);
		listView.smoothScrollToPosition(position);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				doOnItemSelected(position);
			}

		});
		
	}

	public void doOnItemSelected(int position)
	{
		if (addNoActivateItem == 1)
		{
			long profileId;
			if (position == 0)
				profileId = GlobalData.PROFILE_NO_ACTIVATE;
			else
				profileId = profilePreferenceAdapter.profileList.get(position-1)._id;
			profilePreference.setProfileId(profileId);	
		}
		else
			profilePreference.setProfileId(profilePreferenceAdapter.profileList.get(position)._id);
		ProfilePreferenceDialog.this.dismiss();
	}
	
	private class AlphabeticallyComparator implements Comparator<Profile> {

		public int compare(Profile lhs, Profile rhs) {

		    int res = GUIData.collator.compare(lhs._name, rhs._name);
	        return res;
	    }
	}
	
}
