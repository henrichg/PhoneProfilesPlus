package sk.henrichg.phoneprofilesplus;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.melnykov.fab.FloatingActionButton;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;

public class EditorEventListFragment extends Fragment {

	public DataWrapper dataWrapper;
	private List<Event> eventList;
	private EditorEventListAdapter eventListAdapter;
	private ListView listView;
	public FloatingActionButton fabButton;
	private DatabaseHandler databaseHandler;
	
	private WeakReference<LoadEventListAsyncTask> asyncTaskContext;
	
	public static final int EDIT_MODE_UNDEFINED = 0;
	public static final int EDIT_MODE_INSERT = 1;
	public static final int EDIT_MODE_DUPLICATE = 2;
	public static final int EDIT_MODE_EDIT = 3;
	public static final int EDIT_MODE_DELETE = 4;
	
	public static final String FILTER_TYPE_ARGUMENT = "filter_type";
	public static final String ORDER_TYPE_ARGUMENT = "order_type";

	public static final int FILTER_TYPE_ALL = 0;
	public static final int FILTER_TYPE_RUNNING = 1;
	public static final int FILTER_TYPE_PAUSED = 2;
	public static final int FILTER_TYPE_STOPPED = 3;
	
	public static final int ORDER_TYPE_EVENT_NAME = 0;
	public static final int ORDER_TYPE_PROFILE_NAME = 1;
	public static final int ORDER_TYPE_PRIORITY = 2;
	
	private int filterType = FILTER_TYPE_ALL; 
	private int orderType = ORDER_TYPE_EVENT_NAME;
	
	/**
	 * The fragment's current callback objects
	 */
	private OnStartEventPreferences onStartEventPreferencesCallback = sDummyOnStartEventPreferencesCallback;
	private OnFinishEventPreferencesActionMode onFinishEventPreferencesActionModeCallback = sDummyOnFinishEventPreferencesActionModeCallback;
	
	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified.
	 */
	// invoked when start profile preference fragment/activity needed 
	public interface OnStartEventPreferences {
		public void onStartEventPreferences(Event event, int editMode, int filterType, int orderType);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static OnStartEventPreferences sDummyOnStartEventPreferencesCallback = new OnStartEventPreferences() {
		public void onStartEventPreferences(Event event, int editMode, int filterType, int orderType) {
		}
	};
	
	// invoked when action mode finish needed (from event list adapter) 
	public interface OnFinishEventPreferencesActionMode {
		public void onFinishEventPreferencesActionMode();
	}

	private static OnFinishEventPreferencesActionMode sDummyOnFinishEventPreferencesActionModeCallback = new OnFinishEventPreferencesActionMode() {
		public void onFinishEventPreferencesActionMode() {
		}
	};

	public EditorEventListFragment() {
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof OnStartEventPreferences)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		onStartEventPreferencesCallback = (OnStartEventPreferences) activity;

		if (activity instanceof OnFinishEventPreferencesActionMode)
			onFinishEventPreferencesActionModeCallback = (OnFinishEventPreferencesActionMode) activity;

	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		onStartEventPreferencesCallback = sDummyOnStartEventPreferencesCallback;
		onFinishEventPreferencesActionModeCallback = sDummyOnFinishEventPreferencesActionModeCallback;
	}
	

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// this is really important in order to save the state across screen
		// configuration changes for example
		setRetainInstance(true);
		
        filterType = getArguments() != null ? 
        		getArguments().getInt(FILTER_TYPE_ARGUMENT, EditorEventListFragment.FILTER_TYPE_ALL) : 
        			EditorEventListFragment.FILTER_TYPE_ALL;
        orderType = getArguments() != null ? 
             	getArguments().getInt(ORDER_TYPE_ARGUMENT, EditorEventListFragment.ORDER_TYPE_EVENT_NAME) : 
                	EditorEventListFragment.ORDER_TYPE_EVENT_NAME;
        //Log.e("EditorEventListFragment.onCreate","filterType="+filterType);
        //Log.e("EditorEventListFragment.onCreate","orderType="+orderType);
		
   		dataWrapper = new DataWrapper(getActivity().getBaseContext(), true, false, 0);
    	dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getActivity(), getActivity().getBaseContext());
             	
       	databaseHandler = dataWrapper.getDatabaseHandler();
		
		getActivity().getIntent();
		
		setHasOptionsMenu(true);

		//Log.e("EditorEventListFragment.onCreate", "xxxx");
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView;
		
		rootView = inflater.inflate(R.layout.editor_event_list, container, false); 

		//Log.e("EditorEventListFragment.onCreateView", "xxxx");
		
		return rootView;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		doOnViewCreated(view, savedInstanceState);
		//Log.e("EditorEventListFragment.onViewCreated", "xxxx");
	}

	//@Override
	//public void onActivityCreated(Bundle savedInstanceState)
	public void doOnViewCreated(View view, Bundle savedInstanceState)
	{
		//super.onActivityCreated(savedInstanceState);
		
		// az tu mame layout, tak mozeme ziskat view-y
		listView = (ListView)view.findViewById(R.id.editor_events_list);
		listView.setEmptyView(view.findViewById(R.id.editor_events_list_empty));
		
		View footerView =  ((LayoutInflater)getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.editor_list_footer, null, false);
        listView.addFooterView(footerView);		

        fabButton = (FloatingActionButton)view.findViewById(R.id.editor_events_list_fab);
        fabButton.attachToListView(listView);
        
		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				//Log.e("EditorEventListFragment.onItemClick", "xxxx");

				startEventPreferencesActivity((Event)eventListAdapter.getItem(position));
				
			}
			
		}); 

		fabButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startEventPreferencesActivity(null);
			}
		});
		
		if (eventList == null)
		{
			LoadEventListAsyncTask asyncTask = new LoadEventListAsyncTask(this, orderType);
		    this.asyncTaskContext = new WeakReference<LoadEventListAsyncTask >(asyncTask );
		    asyncTask.execute();			
		}
		else
		{
			listView.setAdapter(eventListAdapter);
		}
		
		//Log.d("EditorEventListFragment.onActivityCreated", "xxx");
        
	}
	
	private static class LoadEventListAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<EditorEventListFragment> fragmentWeakRef;
		private DataWrapper dataWrapper;
		private int orderType;

        private LoadEventListAsyncTask (EditorEventListFragment fragment, int orderType) {
            this.fragmentWeakRef = new WeakReference<EditorEventListFragment>(fragment);
            this.orderType = orderType;
	        this.dataWrapper = new DataWrapper(fragment.getActivity().getBaseContext(), true, false, 0);
        }

        @Override
        protected Void doInBackground(Void... params) {
        	List<Event> eventList = dataWrapper.getEventList();
        	
        	//Log.e("EditorEventListFragment.LoadEventListAsyncTask.doInBackground","orderType="+orderType);
        	EditorEventListFragment.sortList(eventList, orderType, dataWrapper);
			dataWrapper.getProfileList();

			return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            EditorEventListFragment fragment = this.fragmentWeakRef.get(); 
            
            if ((fragment != null) && (fragment.isAdded())) {
    	        // get local eventList
    	    	List<Event> eventList = dataWrapper.getEventList();
    	    	// set local event list into activity dataWrapper
    	        fragment.dataWrapper.setEventList(eventList);
    	        // set reference of profile list from dataWrapper
    	        fragment.eventList = fragment.dataWrapper.getEventList();

    			fragment.eventListAdapter = new EditorEventListAdapter(fragment, fragment.dataWrapper, fragment.filterType);
    			fragment.listView.setAdapter(fragment.eventListAdapter);
    			
            }
        }
    }

	private boolean isAsyncTaskPendingOrRunning() {
	    return this.asyncTaskContext != null &&
	          this.asyncTaskContext.get() != null && 
	          !this.asyncTaskContext.get().getStatus().equals(AsyncTask.Status.FINISHED);
	}	
	
	@Override
	public void onStart()
	{
		super.onStart();

		//Log.d("EditorEventListFragment.onStart", "xxxx");
		
	}
	
	@Override
	public void onDestroy()
	{
		if (!isAsyncTaskPendingOrRunning())
		{
			if (listView != null)
				listView.setAdapter(null);
			if (eventListAdapter != null)
				eventListAdapter.release();
	
			eventList = null;
			databaseHandler = null;
			
			if (dataWrapper != null)
				dataWrapper.invalidateDataWrapper();
			dataWrapper = null;
		}
		
		super.onDestroy();

		//Log.e("EditorEventListFragment.onDestroy","xxx");
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.fragment_editor_event_list, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		/*case R.id.menu_new_event:
			//Log.e("EditorEventListFragment.onOptionsItemSelected", "menu_new_event");

			startEventPreferencesActivity(null);
			
			return true;*/
		case R.id.menu_delete_all_events:
			//Log.d("EditorEventListFragment.onOptionsItemSelected", "menu_delete_all_events");
			
			deleteAllEvents();
			
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void startEventPreferencesActivity(Event event)
	{

		Event _event = event;
		int editMode;
		
		if (_event != null)
		{
			// editacia udalosti
			int profilePos = eventListAdapter.getItemPosition(_event);
			listView.setSelection(profilePos);
			listView.setItemChecked(profilePos, true);
			listView.smoothScrollToPosition(profilePos);
			editMode = EDIT_MODE_EDIT;
		}
		else
		{
			// pridanie novej udalost
			editMode = EDIT_MODE_INSERT;

		}

		//Log.d("EditorEventListFragment.startProfilePreferencesActivity", profile.getID()+"");
		
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) one must start profile preferences
		onStartEventPreferencesCallback.onStartEventPreferences(_event, editMode, filterType, orderType);
	}
	
	public void runStopEvent(Event event)
	{
		if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
		{
			// pause event
			List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();
			event.pauseEvent(dataWrapper, eventTimelineList, true, false, false, false); //no activate return profile
			// redraw event list
			updateListView(event, false);
			// restart events
			dataWrapper.restartEvents(false, false);
		}
		else
		{
			// stop event
			List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();
			event.stopEvent(dataWrapper, eventTimelineList, true, false, true); // activate return profile
			// redraw event list
			updateListView(event, false);
			// restart events
			dataWrapper.restartEvents(false, false);
		}
	}

	public void duplicateEvent(Event origEvent)
	{
		/*
		Event newEvent = new Event(
				   origEvent._name+"_d", 
				   origEvent._type, 
				   origEvent._fkProfile, 
				   origEvent._status
					);
		newEvent.copyEventPreferences(origEvent);

		// add event into db and set id and order
		databaseHandler.addEvent(newEvent); 
		// add event into listview
		eventListAdapter.addItem(newEvent, false);
		
		updateListView(newEvent, false);

		startEventPreferencesActivity(newEvent);
		*/
		
		int editMode;

		// zduplikovanie profilu
		editMode = EDIT_MODE_DUPLICATE;

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) one must start profile preferences
		onStartEventPreferencesCallback.onStartEventPreferences(origEvent, editMode, filterType, orderType);
		
		
	}

	public void deleteEvent(Event event)
	{
		if (dataWrapper.getEventById(event._id) == null)
			// event not exists
			return;

		List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();
		event.stopEvent(dataWrapper, eventTimelineList, false, true, true);
		// restart events
		dataWrapper.restartEvents(false, false);
		
		eventListAdapter.deleteItemNoNotify(event);
		databaseHandler.deleteEvent(event);
		
		if (!eventListAdapter.released)
		{
			eventListAdapter.notifyDataSetChanged();
		
			onStartEventPreferencesCallback.onStartEventPreferences(null, EDIT_MODE_DELETE, filterType, orderType);
		}
	}
	
	public void showEditMenu(View view)
	{
		//Context context = ((ActionBarActivity)getActivity()).getSupportActionBar().getThemedContext();
		Context context = view.getContext();
		PopupMenu popup = new PopupMenu(context, view);
		Menu menu = popup.getMenu();
		getActivity().getMenuInflater().inflate(R.menu.event_list_item_edit, menu);

		final Event event = (Event)view.getTag();
		
		MenuItem menuItem = menu.findItem(R.id.event_list_item_menu_run_stop);
        if (GlobalData.getGlobalEventsRuning(dataWrapper.context))
        {
        	menuItem.setVisible(true);
        	
	        if (event.getStatusFromDB(dataWrapper) == Event.ESTATUS_STOP)
	        {
	        	menuItem.setTitle(R.string.event_list_item_menu_run);
	        }
	        else
	        {
	        	menuItem.setTitle(R.string.event_list_item_menu_stop);
	        }
        }
        else
        	menuItem.setVisible(false);
		
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

			public boolean onMenuItemClick(android.view.MenuItem item) {
				switch (item.getItemId()) {
				case R.id.event_list_item_menu_run_stop:
					runStopEvent(event);
					return true;
				case R.id.event_list_item_menu_duplicate:
					duplicateEvent(event);
					return true;
				case R.id.event_list_item_menu_delete:
					deleteEventWithAlert(event);
					return true;
				default:
					return false;
				}
			}
			});		
		
		
		popup.show();		
	}
	
	public void deleteEventWithAlert(Event event)
	{
		final Event _event = event;

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
		dialogBuilder.setTitle(getResources().getString(R.string.event_string_0) + ": " + event._name);
		dialogBuilder.setMessage(getResources().getString(R.string.delete_event_alert_message));
		//dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				deleteEvent(_event);
			}
		});
		dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
		dialogBuilder.show();
	}

	private void deleteAllEvents()
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
		dialogBuilder.setTitle(getResources().getString(R.string.alert_title_delete_all_events));
		dialogBuilder.setMessage(getResources().getString(R.string.alert_message_delete_all_events));
		//dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				dataWrapper.stopAllEvents(true, false);
				
				databaseHandler.deleteAllEvents();
				eventListAdapter.clear();
				
				onStartEventPreferencesCallback.onStartEventPreferences(null, EDIT_MODE_DELETE, filterType, orderType);
				
			}
		});
		dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
		dialogBuilder.show();
	}
	
	// called from event list adapter
	public void finishEventPreferencesActionMode()
	{
		onFinishEventPreferencesActionModeCallback.onFinishEventPreferencesActionMode();
	}
	
	public void updateListView(Event event, boolean newEvent)
	{
		if (eventListAdapter != null)
		{
			if ((newEvent) && (event != null))
				// add event into listview
				eventListAdapter.addItem(event, false);
		}

		if (eventList != null)
		{
			// sort list
			sortList(eventList, orderType, dataWrapper);
		}

		if (eventListAdapter != null)
		{
			int eventPos;

			if (event != null)
				eventPos = eventListAdapter.getItemPosition(event);
			else
				eventPos = listView.getCheckedItemPosition();

			eventListAdapter.notifyDataSetChanged();
			
			if (eventPos != ListView.INVALID_POSITION)
			{
				// set event visible in list
				listView.setSelection(eventPos);
				listView.setItemChecked(eventPos, true);
				listView.smoothScrollToPosition(eventPos);
			}
		}
	}

	public int getFilterType()
	{
		return filterType;
	}
	
	public void changeListOrder(int orderType)
	{
		//Log.e("EditorEventListFragment.changeListOrder","orderType="+orderType);
		this.orderType = orderType;
		if (eventListAdapter != null)
		{
			sortList(eventList, orderType, dataWrapper);
			eventListAdapter.notifyDataSetChanged();
		}
	}
	
	private static void sortList(List<Event> eventList, int orderType, DataWrapper _dataWrapper)
	{
		final DataWrapper dataWrapper = _dataWrapper;
		
		class EventNameComparator implements Comparator<Event> {
			public int compare(Event lhs, Event rhs) {
			    int res = GUIData.collator.compare(lhs._name, rhs._name); 
		        return res;
		    }
		}
		
		class ProfileNameComparator implements Comparator<Event> {
			public int compare(Event lhs, Event rhs) {
				Profile profileLhs = dataWrapper.getProfileById(lhs._fkProfileStart);
				Profile profileRhs = dataWrapper.getProfileById(rhs._fkProfileStart);
				String nameLhs = "";
				if (profileLhs != null) nameLhs = profileLhs._name;
				String nameRhs = "";
				if (profileRhs != null) nameRhs = profileRhs._name;
			    int res = GUIData.collator.compare(nameLhs, nameRhs);
		        return res;
		    }
		}

		class PriorityComparator implements Comparator<Event> {
			public int compare(Event lhs, Event rhs) {

			    //int res =  lhs._priority - rhs._priority;
			    int res =  rhs._priority - lhs._priority;
		        return res;
		    }
		}
		
		switch (orderType)
		{
			case ORDER_TYPE_EVENT_NAME: 
				Collections.sort(eventList, new EventNameComparator());
				break;
			case ORDER_TYPE_PROFILE_NAME:
			    Collections.sort(eventList, new ProfileNameComparator());
			    break;
			case ORDER_TYPE_PRIORITY:
			    Collections.sort(eventList, new PriorityComparator());
			    break;
		}
	}
	
	public void refreshGUI()
	{
		if ((dataWrapper == null) || (eventList == null))
			return;

		for (Event event : eventList)
		{
			int status = dataWrapper.getDatabaseHandler().getEventStatus(event);
			event.setStatus(status);
			boolean isInDelay = dataWrapper.getDatabaseHandler().getEventInDelay(event);
			event._isInDelay = isInDelay;
			dataWrapper.getDatabaseHandler().setEventCalendarTimes(event);
			dataWrapper.getDatabaseHandler().setSMSStartTimes(event);
		}
		updateListView(null, false);
	}
    
}
