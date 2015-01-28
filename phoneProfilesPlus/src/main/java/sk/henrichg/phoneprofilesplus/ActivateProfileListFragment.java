package sk.henrichg.phoneprofilesplus;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class ActivateProfileListFragment extends Fragment {

	private DataWrapper dataWrapper;
	private List<Profile> profileList = null;
	private ActivateProfileListAdapter profileListAdapter = null;
	private ListView listView = null;
	private GridView gridView = null;
	private TextView activeProfileName;
	private ImageView activeProfileIcon;
	
	private WeakReference<LoadProfileListAsyncTask> asyncTaskContext;
	
	public ActivateProfileListFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// this is really important in order to save the state across screen
		// configuration changes for example
		setRetainInstance(true);
	
		dataWrapper = new DataWrapper(getActivity().getBaseContext(), true, false, 0);
		dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getActivity(), getActivity().getBaseContext());		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView;
		
		if (!GlobalData.applicationActivatorGridLayout)
		{
			if (GlobalData.applicationActivatorPrefIndicator && GlobalData.applicationActivatorHeader)
				rootView = inflater.inflate(R.layout.activate_profile_list, container, false); 
			else
			if (GlobalData.applicationActivatorHeader)
				rootView = inflater.inflate(R.layout.activate_profile_list_no_indicator, container, false); 
			else
				rootView = inflater.inflate(R.layout.activate_profile_list_no_header, container, false);
		}
		else
		{
			if (GlobalData.applicationActivatorPrefIndicator && GlobalData.applicationActivatorHeader)
				rootView = inflater.inflate(R.layout.activate_profile_grid, container, false); 
			else
			if (GlobalData.applicationActivatorHeader)
				rootView = inflater.inflate(R.layout.activate_profile_grid_no_indicator, container, false); 
			else
				rootView = inflater.inflate(R.layout.activate_profile_grid_no_header, container, false);
		}
		
		return rootView;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		doOnViewCreated(view, savedInstanceState);
	}
	
	//@Override
	public void doOnViewCreated(View view, Bundle savedInstanceState)
	{
		activeProfileName = (TextView)view.findViewById(R.id.act_prof_activated_profile_name);
		activeProfileIcon = (ImageView)view.findViewById(R.id.act_prof_activated_profile_icon);
		if (!GlobalData.applicationActivatorGridLayout)
			listView = (ListView)view.findViewById(R.id.act_prof_profiles_list);
		else
			gridView = (GridView)view.findViewById(R.id.act_prof_profiles_grid);
		
		AbsListView absListView;
		if (!GlobalData.applicationActivatorGridLayout)
			absListView = listView;
		else
			absListView = gridView;

		//absListView.setLongClickable(false);
		
		absListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				//Log.d("ActivateProfilesActivity.onItemClick", "xxxx");

				if (!GlobalData.applicationLongClickActivation)
					//activateProfileWithAlert(position);
					activateProfile((Profile)profileListAdapter.getItem(position), GlobalData.STARTUP_SOURCE_ACTIVATOR);

			}
			
		}); 
		
		absListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				//Log.d("ActivateProfilesActivity.onItemLongClick", "xxxx");
				
				if (GlobalData.applicationLongClickActivation)
					//activateProfileWithAlert(position);
					activateProfile((Profile)profileListAdapter.getItem(position), GlobalData.STARTUP_SOURCE_ACTIVATOR);

				return false;
			}
			
		});

        //absListView.setRemoveListener(onRemove);

		if (profileList == null)
		{
			LoadProfileListAsyncTask asyncTask = new LoadProfileListAsyncTask(this);
		    this.asyncTaskContext = new WeakReference<LoadProfileListAsyncTask >(asyncTask );
		    asyncTask.execute();			
		}
		else
		{
			absListView.setAdapter(profileListAdapter);
			doOnStart();
		}
		
		//Log.d("EditorProfileListFragment.onActivityCreated", "xxx");
        
	}
	
	private static class LoadProfileListAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<ActivateProfileListFragment> fragmentWeakRef;
		private DataWrapper dataWrapper; 

		private class ProfileComparator implements Comparator<Profile> {
			public int compare(Profile lhs, Profile rhs) {
			    int res = lhs._porder - rhs._porder;
		        return res;
		    }
		}
		
        private LoadProfileListAsyncTask (ActivateProfileListFragment fragment) {
            this.fragmentWeakRef = new WeakReference<ActivateProfileListFragment>(fragment);
	        this.dataWrapper = new DataWrapper(fragment.getActivity().getBaseContext(), true, false, 0);
        }

        @Override
        protected Void doInBackground(Void... params) {
	    	List<Profile> profileList = dataWrapper.getProfileList();

	    	if (!GlobalData.applicationActivatorHeader)
	    	{
	    		Profile profile = dataWrapper.getActivatedProfile();
	    		if ((profile != null) && (!profile._showInActivator))
	    		{
	    			profile._showInActivator = true;
	    			profile._porder = -1;
	    		}
	    	}
	    	
		    Collections.sort(profileList, new ProfileComparator());
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            ActivateProfileListFragment fragment = this.fragmentWeakRef.get(); 
            
            if ((fragment != null) && (fragment.isAdded())) {
            	
    	        // get local profileList
    	    	List<Profile> profileList = dataWrapper.getProfileList();
    	    	// set copy local profile list into activity profilesDataWrapper
    	        fragment.dataWrapper.setProfileList(profileList, false);
    	        // set reference of profile list from profilesDataWrapper
    	        fragment.profileList = fragment.dataWrapper.getProfileList();

				if (fragment.profileList.size() == 0)
				{
					// nie je ziaden profile, startneme Editor
					
					Intent intent = new Intent(fragment.getActivity().getBaseContext(), EditorProfilesActivity.class);
					intent.putExtra(GlobalData.EXTRA_START_APP_SOURCE, GlobalData.STARTUP_SOURCE_ACTIVATOR_START);
					fragment.getActivity().startActivity(intent);
					
					fragment.getActivity().finish();
	
					return;
				}
    	        
    	        fragment.profileListAdapter = new ActivateProfileListAdapter(fragment, fragment.profileList, dataWrapper);

    	        AbsListView absListView;
    			if (!GlobalData.applicationActivatorGridLayout)
    				absListView = fragment.listView;
    			else
    				absListView = fragment.gridView;
    			absListView.setAdapter(fragment.profileListAdapter);
    			
				fragment.doOnStart();
            }
        }
    }
	
	private boolean isAsyncTaskPendingOrRunning() {
	    return this.asyncTaskContext != null &&
	          this.asyncTaskContext.get() != null && 
	          !this.asyncTaskContext.get().getStatus().equals(AsyncTask.Status.FINISHED);
	}	
	
	private void doOnStart()
	{
		//long nanoTimeStart = GlobalData.startMeasuringRunTime();

		//Log.d("ActivateProfilesActivity.onStart", "startupSource="+startupSource);

		Profile profile = dataWrapper.getActivatedProfile();
		
		updateHeader(profile);
		endOnStart();

		//GlobalData.getMeasuredRunTime(nanoTimeStart, "ActivateProfileActivity.onStart");
		
		//Log.d("PhoneProfileActivity.onStart", "xxxx");
		
	}
	
	private void endOnStart()
	{
		//Log.e("ActivateProfileActivity.endOnStart","xxx");

		//  aplikacia uz je 1. krat spustena
		GlobalData.setApplicationStarted(getActivity().getBaseContext(), true);
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();

		//Log.d("EditorProfileListFragment.onStart", "xxxx");
		
	}
	
	@Override
	public void onDestroy()
	{
		if (!isAsyncTaskPendingOrRunning())
		{
	        AbsListView absListView;
			if (!GlobalData.applicationActivatorGridLayout)
				absListView = listView;
			else
				absListView = gridView;
			if (absListView != null)
				absListView.setAdapter(null);
			if (profileListAdapter != null)
				profileListAdapter.release();
			
			profileList = null;
	
			if (dataWrapper != null)
				dataWrapper.invalidateDataWrapper();
			dataWrapper = null;
		}
		
		super.onDestroy();
	}

	private void updateHeader(Profile profile)
	{
		if (!GlobalData.applicationActivatorHeader)
			return;
		
		if (profile == null)
		{
			activeProfileName.setText(getResources().getString(R.string.profiles_header_profile_name_no_activated));
	    	activeProfileIcon.setImageResource(R.drawable.ic_profile_default);
		}
		else
		{
			activeProfileName.setText(dataWrapper.getProfileNameWithManualIndicator(profile, true));
	        if (profile.getIsIconResourceID())
	        {
				int res = getResources().getIdentifier(profile.getIconIdentifier(), "drawable", getActivity().getPackageName());
				activeProfileIcon.setImageResource(res); // resource na ikonu
	        }
	        else
	        {
        		//Resources resources = getResources();
        		//int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
        		//int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
        		//Bitmap bitmap = BitmapResampler.resample(profile.getIconIdentifier(), width, height);
	        	//activeProfileIcon.setImageBitmap(bitmap);
	        	activeProfileIcon.setImageBitmap(profile._iconBitmap);
	        }
		}
		
		if (GlobalData.applicationActivatorPrefIndicator)
		{
			ImageView profilePrefIndicatorImageView = (ImageView)getActivity().findViewById(R.id.act_prof_activated_profile_pref_indicator);
			if (profilePrefIndicatorImageView != null)
			{
				if (profile == null)
					profilePrefIndicatorImageView.setImageResource(R.drawable.ic_empty);
				else
					profilePrefIndicatorImageView.setImageBitmap(profile._preferencesIndicator);
			}
		}
	}

	private void activateProfile(Profile profile, int startupSource)
	{
		dataWrapper.activateProfile(profile._id, startupSource, getActivity(), "");
	}

	public void refreshGUI()
	{
		if ((dataWrapper == null) || (profileListAdapter == null))
			return;
		
		((ActivateProfileActivity)getActivity()).setEventsRunStopIndicator();
		
		Profile profileFromAdapter = profileListAdapter.getActivatedProfile();
		if (profileFromAdapter != null)
			profileFromAdapter._checked = false;

		Profile profileFromDB = dataWrapper.getDatabaseHandler().getActivatedProfile();
		if (profileFromDB != null)
		{
			Profile profileFromDataWrapper = dataWrapper.getProfileById(profileFromDB._id);
			if (profileFromDataWrapper != null)
				profileFromDataWrapper._checked = true;
			updateHeader(profileFromDataWrapper);
		}
		else
			updateHeader(null);

		profileListAdapter.notifyDataSetChanged();
		
	}
	
}
