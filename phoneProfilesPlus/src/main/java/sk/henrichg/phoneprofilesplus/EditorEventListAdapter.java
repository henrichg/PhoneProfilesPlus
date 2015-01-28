package sk.henrichg.phoneprofilesplus;

import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EditorEventListAdapter extends BaseAdapter
{

	private EditorEventListFragment fragment;
	private DataWrapper dataWrapper;
	private int filterType;
	public List<Event> eventList;
	public boolean released = false;
	
	public EditorEventListAdapter(EditorEventListFragment f, DataWrapper pdw, int filterType)
	{
		fragment = f;
		dataWrapper = pdw;
		eventList = dataWrapper.getEventList();
		this.filterType = filterType;
	}   
	
	public void release()
	{
		released = true;
		
		fragment = null;
		eventList = null;
		dataWrapper = null;
	}
	
	public int getCount()
	{
		if (eventList == null)
			return 0;

		if (filterType == EditorEventListFragment.FILTER_TYPE_ALL)
			return eventList.size();
		
		int count = 0;
		for (Event event : eventList)
		{
	        switch (filterType)
	        {
				case EditorEventListFragment.FILTER_TYPE_RUNNING:
					if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_RUNNING)
						++count;
					break;
				case EditorEventListFragment.FILTER_TYPE_PAUSED:
					if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_PAUSE)
						++count;
					break;
				case EditorEventListFragment.FILTER_TYPE_STOPPED:
					if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
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
			
			if (filterType == EditorEventListFragment.FILTER_TYPE_ALL)
				return eventList.get(position);
			
			Event _event = null;
			
			int pos = -1;
			for (Event event : eventList)
			{
				switch (filterType)
		        {
					case EditorEventListFragment.FILTER_TYPE_RUNNING:
						if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_RUNNING)
							++pos;
						break;
					case EditorEventListFragment.FILTER_TYPE_PAUSED:
						if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_PAUSE)
							++pos;
						break;
					case EditorEventListFragment.FILTER_TYPE_STOPPED:
						if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
							++pos;
						break;
		        }
		        if (pos == position)
		        {
		        	_event = event;
		        	break;
		        }
			}
			
			return _event;
		}
	}

	public long getItemId(int position)
	{
		return position;
	}

	public int getItemId(Event event)
	{
		if (eventList == null)
			return -1;

		for (int i = 0; i < eventList.size(); i++)
		{
			if (eventList.get(i)._id == event._id)
				return i;
		}
		return -1;
	}
	
	public int getItemPosition(Event event)
	{
		if (eventList == null)
			return -1;
		
		if (event == null)
			return -1;
		
		int pos = -1;
		
		for (int i = 0; i < eventList.size(); i++)
		{
			switch (filterType)
	        {
				case EditorEventListFragment.FILTER_TYPE_ALL:
					++pos;
					break;
				case EditorEventListFragment.FILTER_TYPE_RUNNING:
					if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_RUNNING)
						++pos;
					break;
				case EditorEventListFragment.FILTER_TYPE_PAUSED:
					if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_PAUSE)
						++pos;
					break;
				case EditorEventListFragment.FILTER_TYPE_STOPPED:
					if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
						++pos;
					break;
	        }
			
			if (eventList.get(i)._id == event._id)
				return pos;
		}
		return -1;
	}
	
	public void setList(List<Event> el)
	{
		eventList = el;
		notifyDataSetChanged();
	}
	
	public void addItem(Event event, boolean refresh)
	{
		if (eventList == null)
			return;
		
		eventList.add(event);
		if (refresh)
			notifyDataSetChanged();
	}

	public void deleteItemNoNotify(Event event)
	{
		if (eventList == null)
			return;

		eventList.remove(event);
	}

	public void deleteItem(Event event)
	{
		deleteItemNoNotify(event);
		notifyDataSetChanged();
	}
	
	public void clear()
	{
		if (eventList == null)
			return;

		eventList.clear();
		notifyDataSetChanged();
	}
	
	static class ViewHolder {
		  RelativeLayout listItemRoot;
		  TextView eventName;
		  TextView eventPreferencesDescription;
		  ImageView eventStatus;
		  ImageView profileStartIcon;
		  TextView profileStartName;
		  ImageView profileStartIndicator;
		  ImageView profileEndIcon;
		  TextView profileEndName;
		  ImageView profileEndIndicator;
		  ImageView eventItemEditMenu;
		  int position;
		}
	
	@SuppressLint("SimpleDateFormat")
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		
		View vi = convertView;
        if (convertView == null)
        {
    		LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
      	    if (GlobalData.applicationEditorPrefIndicator)
      		    vi = inflater.inflate(R.layout.editor_event_list_item, parent, false);
      	    else
      		    vi = inflater.inflate(R.layout.editor_event_list_item_no_indicator, parent, false);
            holder = new ViewHolder();
            holder.listItemRoot = (RelativeLayout)vi.findViewById(R.id.event_list_item_root);
            holder.eventName = (TextView)vi.findViewById(R.id.event_list_item_event_name);
            holder.eventStatus = (ImageView)vi.findViewById(R.id.event_list_item_status);
    		holder.eventItemEditMenu = (ImageView)vi.findViewById(R.id.event_list_item_edit_menu);
            holder.profileStartName = (TextView)vi.findViewById(R.id.event_list_item_profile_start_name);
            holder.profileStartIcon = (ImageView)vi.findViewById(R.id.event_list_item_profile_start_icon);
            holder.profileEndName = (TextView)vi.findViewById(R.id.event_list_item_profile_end_name);
            holder.profileEndIcon = (ImageView)vi.findViewById(R.id.event_list_item_profile_end_icon);
  		    if (GlobalData.applicationEditorPrefIndicator)
  		    {
  		    	holder.eventPreferencesDescription  = (TextView)vi.findViewById(R.id.event_list_item_preferences_description);
  		    	holder.eventPreferencesDescription.setHorizontallyScrolling(true); // disable auto word wrap :-)
  		    	holder.profileStartIndicator = (ImageView)vi.findViewById(R.id.event_list_item_profile_start_pref_indicator);
  		    	holder.profileEndIndicator = (ImageView)vi.findViewById(R.id.event_list_item_profile_end_pref_indicator);
  		    }
            vi.setTag(holder);        
        }
        else
        {
      	    holder = (ViewHolder)vi.getTag();
        }
        
		
        final Event event = (Event)getItem(position);
        if (event != null)
        {
	       	int eventStatus = event.getStatusFromDB(dataWrapper); 
	
	        if (eventStatus == Event.ESTATUS_RUNNING)
	        {
		       	if (GlobalData.applicationTheme.equals("material"))
		       		holder.listItemRoot.setBackgroundResource(R.drawable.header_card);
		       	else
		       	if (GlobalData.applicationTheme.equals("dark"))
		       		holder.listItemRoot.setBackgroundResource(R.drawable.header_card_dark);
		      	else
	         	if (GlobalData.applicationTheme.equals("dlight"))
	         		holder.listItemRoot.setBackgroundResource(R.drawable.header_card);
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
	        }
	        
	       	int statusRes = Event.ESTATUS_STOP;
	       	switch (eventStatus)
	       	{
	       		case Event.ESTATUS_RUNNING:
	       			statusRes = R.drawable.ic_event_status_running;
	       			break;
	       		case Event.ESTATUS_PAUSE:
	       			if (event._isInDelay)
	       				statusRes = R.drawable.ic_event_status_pause_delay;
	       			else
	       				statusRes = R.drawable.ic_event_status_pause;
	       			break;
	       		case Event.ESTATUS_STOP:
	       			if (event.isRunnable())
	       				statusRes = R.drawable.ic_event_status_stop;
	       			else
	       				statusRes = R.drawable.ic_event_status_stop_not_runnable;
	       			break;
	       	}
	   		holder.eventStatus.setImageResource(statusRes);
	
	   		if (eventStatus == Event.ESTATUS_RUNNING)
	   			holder.eventName.setTypeface(null, Typeface.BOLD);
	   		else
	   			holder.eventName.setTypeface(null, Typeface.NORMAL);
	   			
	   		String eventPriority = "[" + (event._priority + Event.EPRIORITY_HIGHEST)  + "] ";
	   		if (event._forceRun)
	   		{
	   			/*if (android.os.Build.VERSION.SDK_INT >= 16)
	   				holder.eventName.setText("\u23E9 " + eventPriority + event._name);
	   			else */
	   				holder.eventName.setText("[\u00BB]" + eventPriority + event._name);
	   		}
	   		else
	   			holder.eventName.setText(eventPriority + event._name);
	   		
		    if (GlobalData.applicationEditorPrefIndicator)
		    {
		    	String eventPrefDescription = event.getPreferecesDescription(vi.getContext());
		    	holder.eventPreferencesDescription.setText(eventPrefDescription);
		    }
	
		    // profile start
	        Profile profile =  dataWrapper.getProfileById(event._fkProfileStart);
	        if (profile != null)
	        {
	        	String profileName = profile._name;
	        	if (event._delayStart > 0)
	        		profileName = "[" + event._delayStart + "] " + profileName;
	        	holder.profileStartName.setText(profileName);
			    if (profile.getIsIconResourceID())
			    {
			    	holder.profileStartIcon.setImageResource(0);
			      	int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable", 
			      				vi.getContext().getPackageName());
			      	holder.profileStartIcon.setImageResource(res); // resource na ikonu
			    }
			    else
			    {
			      	holder.profileStartIcon.setImageBitmap(profile._iconBitmap);
			    }
			    
				if (GlobalData.applicationEditorPrefIndicator)
				{
					//profilePrefIndicatorImageView.setImageBitmap(null);
					//Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
					//profilePrefIndicatorImageView.setImageBitmap(bitmap);
					holder.profileStartIndicator.setImageBitmap(profile._preferencesIndicator);
				}
	        }
	        else
	        {
	        	holder.profileStartName.setText(R.string.profile_preference_profile_not_set);
	        	holder.profileStartIcon.setImageResource(R.drawable.ic_profile_default);
				if (GlobalData.applicationEditorPrefIndicator)
				{
					//profilePrefIndicatorImageView.setImageBitmap(null);
					//Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
					//profilePrefIndicatorImageView.setImageBitmap(bitmap);
					holder.profileStartIndicator.setImageResource(R.drawable.ic_empty);
				}
	        }
	
		    // profile end
	        profile =  dataWrapper.getProfileById(event._fkProfileEnd);
	        if (profile != null)
	        {
	        	String profileName = profile._name;
	        	if (event._undoneProfile)
	        		profileName = profileName + " + " + vi.getResources().getString(R.string.event_prefernce_profile_undone);
	        	holder.profileEndName.setText(profileName);
			    if (profile.getIsIconResourceID())
			    {
			    	holder.profileEndIcon.setImageResource(0);
			      	int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable", 
			      				vi.getContext().getPackageName());
			      	holder.profileEndIcon.setImageResource(res); // resource na ikonu
			    }
			    else
			    {
			      	holder.profileEndIcon.setImageBitmap(profile._iconBitmap);
			    }
			    
				if (GlobalData.applicationEditorPrefIndicator)
				{
					//profilePrefIndicatorImageView.setImageBitmap(null);
					//Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
					//profilePrefIndicatorImageView.setImageBitmap(bitmap);
					holder.profileEndIndicator.setImageBitmap(profile._preferencesIndicator);
				}
	        }
	        else
	        {
	        	String profileName;
	        	if (event._undoneProfile)
	        		profileName = vi.getResources().getString(R.string.event_prefernce_profile_undone);
	        	else
	        	{
		        	if (event._fkProfileEnd == GlobalData.PROFILE_NO_ACTIVATE)
		        		profileName = vi.getResources().getString(R.string.profile_preference_profile_end_no_activate); 
		        	else
		        		profileName = vi.getResources().getString(R.string.profile_preference_profile_not_set);
	        	}
	    		holder.profileEndName.setText(profileName);
	        	holder.profileEndIcon.setImageResource(R.drawable.ic_profile_default);
				if (GlobalData.applicationEditorPrefIndicator)
				{
					//profilePrefIndicatorImageView.setImageBitmap(null);
					//Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
					//profilePrefIndicatorImageView.setImageBitmap(bitmap);
					holder.profileEndIndicator.setImageResource(R.drawable.ic_empty);
				}
	        }
	        
	        holder.eventItemEditMenu.setTag(event);
	        final ImageView eventItemEditMenu = holder.eventItemEditMenu;
	        holder.eventItemEditMenu.setOnClickListener(new OnClickListener() {
	
					public void onClick(View v) {
						//Log.d("EditorEventListAdapter.onClick", "delete");
						((EditorEventListFragment)fragment).finishEventPreferencesActionMode();
						((EditorEventListFragment)fragment).showEditMenu(eventItemEditMenu);
					}
				}); 
			
	        //Log.d("ProfileListAdapter.getView", profile.getName());
        }
        
		return vi;
	}

}
