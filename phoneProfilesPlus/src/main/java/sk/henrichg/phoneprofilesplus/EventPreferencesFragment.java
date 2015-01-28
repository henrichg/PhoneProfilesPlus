package sk.henrichg.phoneprofilesplus;
 
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
 
public class EventPreferencesFragment extends PreferenceFragment 
										implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private DataWrapper dataWrapper;
	private Event event;
	public long event_id;
	//private boolean first_start_activity;
	private int new_event_mode;
	private int startupSource;
	public boolean eventNonEdited = true;
	private PreferenceManager prefMng;
	private SharedPreferences preferences;
	private Context context;
	private ActionMode actionMode;
	private Callback actionModeCallback;
	
	private int actionModeButtonClicked = BUTTON_UNDEFINED;
	
	private static Activity preferencesActivity = null;
		
	static final String PREFS_NAME_ACTIVITY = "event_preferences_activity";
	static final String PREFS_NAME_FRAGMENT = "event_preferences_fragment";
	private String PREFS_NAME;

	static final String SP_ACTION_MODE_SHOWED = "action_mode_showed";
	
	static final int BUTTON_UNDEFINED = 0;
	static final int BUTTON_CANCEL = 1;
	static final int BUTTON_SAVE = 2;
	
	private OnShowActionModeInEventPreferences onShowActionModeInEventPreferencesCallback = sDummyOnShowActionModeInEventPreferencesCallback;
	private OnHideActionModeInEventPreferences onHideActionModeInEventPreferencesCallback = sDummyOnHideActionModeInEventPreferencesCallback;
	private OnRestartEventPreferences onRestartEventPreferencesCallback = sDummyOnRestartEventPreferencesCallback;
	private OnRedrawEventListFragment onRedrawEventListFragmentCallback = sDummyOnRedrawEventListFragmentCallback;

	// invokes when action mode shows
	public interface OnShowActionModeInEventPreferences {
		public void onShowActionModeInEventPreferences();
	}

	private static OnShowActionModeInEventPreferences sDummyOnShowActionModeInEventPreferencesCallback = new OnShowActionModeInEventPreferences() {
		public void onShowActionModeInEventPreferences() {
		}
	};
	
	// invokes when action mode hides
	public interface OnHideActionModeInEventPreferences {
		public void onHideActionModeInEventPreferences();
	}

	private static OnHideActionModeInEventPreferences sDummyOnHideActionModeInEventPreferencesCallback = new OnHideActionModeInEventPreferences() {
		public void onHideActionModeInEventPreferences() {
		}
	};
	
	// invokes when restart of event preferences fragment needed (undo preference changes)
	public interface OnRestartEventPreferences {
		/**
		 * Callback for restart fragment.
		 */
		public void onRestartEventPreferences(Event event, int newEventMode);
	}

	private static OnRestartEventPreferences sDummyOnRestartEventPreferencesCallback = new OnRestartEventPreferences() {
		public void onRestartEventPreferences(Event event, int newEventMode) {
		}
	};
	
	// invokes when event list fragment redraw needed (preference changes accepted)
	public interface OnRedrawEventListFragment {
		/**
		 * Callback for redraw event list fragment.
		 */
		public void onRedrawEventListFragment(Event event, int newEventMode);
	}

	private static OnRedrawEventListFragment sDummyOnRedrawEventListFragmentCallback = new OnRedrawEventListFragment() {
		public void onRedrawEventListFragment(Event event, int newEventMode) {
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof OnShowActionModeInEventPreferences)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		onShowActionModeInEventPreferencesCallback = (OnShowActionModeInEventPreferences) activity;
		
		if (!(activity instanceof OnHideActionModeInEventPreferences)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		onHideActionModeInEventPreferencesCallback = (OnHideActionModeInEventPreferences) activity;
		
		if (!(activity instanceof OnRestartEventPreferences)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		onRestartEventPreferencesCallback = (OnRestartEventPreferences) activity;
		
		if (!(activity instanceof OnRedrawEventListFragment)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		onRedrawEventListFragmentCallback = (OnRedrawEventListFragment) activity;
		
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		onShowActionModeInEventPreferencesCallback = sDummyOnShowActionModeInEventPreferencesCallback;
		onHideActionModeInEventPreferencesCallback = sDummyOnHideActionModeInEventPreferencesCallback;
		onRestartEventPreferencesCallback = sDummyOnRestartEventPreferencesCallback;
		onRedrawEventListFragmentCallback = sDummyOnRedrawEventListFragmentCallback;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		// must by false to avoid FC when rotation changes and preference dialogs are shown
		setRetainInstance(false);
		
		preferencesActivity = getActivity();
        context = getActivity().getBaseContext();

        dataWrapper = new DataWrapper(context, true, false, 0);
		
	    startupSource = getArguments().getInt(GlobalData.EXTRA_PREFERENCES_STARTUP_SOURCE, GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT);
	    if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
	    	PREFS_NAME = PREFS_NAME_ACTIVITY;
	    else
	    if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
	    	PREFS_NAME = PREFS_NAME_FRAGMENT;
	    else
			PREFS_NAME = PREFS_NAME_FRAGMENT;
		
		prefMng = getPreferenceManager();
		prefMng.setSharedPreferencesName(PREFS_NAME);
		prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);
		
        // getting attached fragment data
		if (getArguments().containsKey(GlobalData.EXTRA_NEW_EVENT_MODE))
			new_event_mode = getArguments().getInt(GlobalData.EXTRA_NEW_EVENT_MODE);
		if (getArguments().containsKey(GlobalData.EXTRA_EVENT_ID))
			event_id = getArguments().getLong(GlobalData.EXTRA_EVENT_ID);
    	//Log.e("EventPreferencesFragment.onCreate", "event_position=" + event_position);
		if (new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT)
		{
			// create new event - default is TIME
			event = dataWrapper.getNoinitializedEvent(getResources().getString(R.string.event_name_default));
			event_id = 0;
		}
		else
		if (new_event_mode == EditorEventListFragment.EDIT_MODE_DUPLICATE)
		{
			// duplicate event
			Event origEvent = dataWrapper.getEventById(event_id);
			event = new Event(
						   origEvent._name+"_d", 
						   origEvent._fkProfileStart, 
						   origEvent._fkProfileEnd, 
						   origEvent.getStatus(),
						   origEvent._notificationSound,
						   origEvent._forceRun,
						   origEvent._blocked,
						   origEvent._undoneProfile,
						   origEvent._priority,
						   origEvent._delayStart,
						   origEvent._isInDelay
							);
			event.copyEventPreferences(origEvent);
			event_id = 0;
		}
		else
			event = dataWrapper.getEventById(event_id);

    	//Log.e("EventPreferencesFragment.onCreate", "event_type_new="+event_type_new);
    	
        preferences = prefMng.getSharedPreferences();
        
		if (savedInstanceState == null)
        	loadPreferences();
   	
    	// get preference resource id from EventPreference
		addPreferencesFromResource(R.xml.event_preferences);
		
		RingtonePreference notificationSoundPreference = (RingtonePreference)prefMng.findPreference(Event.PREF_EVENT_NOTIFICATION_SOUND);
		notificationSoundPreference.setEnabled(GlobalData.notificationStatusBar);
		
		event._eventPreferencesTime.checkPreferences(prefMng, context);
		event._eventPreferencesBattery.checkPreferences(prefMng, context);
		event._eventPreferencesCall.checkPreferences(prefMng, context);
		event._eventPreferencesCalendar.checkPreferences(prefMng, context);
		event._eventPreferencesPeripherals.checkPreferences(prefMng, context);
		event._eventPreferencesWifi.checkPreferences(prefMng, context);
		event._eventPreferencesScreen.checkPreferences(prefMng, context);
		event._eventPreferencesBluetooth.checkPreferences(prefMng, context);
		event._eventPreferencesSMS.checkPreferences(prefMng, context);

        preferences.registerOnSharedPreferenceChangeListener(this);  
        
        createActionModeCallback();

		if (savedInstanceState == null)
		{
	       	SharedPreferences preferences = getActivity().getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
	       	Editor editor = preferences.edit();
	       	editor.remove(SP_ACTION_MODE_SHOWED);
	       	editor.commit();
		}

		updateSharedPreference();
       	
    	//Log.d("EventPreferencesFragment.onCreate", "xxxx");
    }
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		// must by in onStart(), in ocCreate() crashed
    	SharedPreferences preferences = getActivity().getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
    	int actionModeShowed = preferences.getInt(SP_ACTION_MODE_SHOWED, 0);
		//Log.e("EventPreferencesFragment.onStart","actionModeShowed="+actionModeShowed);
        if (actionModeShowed == 2)
        	showActionMode();
        else
        if (((new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT) ||
            (new_event_mode == EditorEventListFragment.EDIT_MODE_DUPLICATE))
        	&& (actionModeShowed == 0))
        	showActionMode();

		//Log.e("EventPreferencesFragment.onStart","typeOld="+String.valueOf(event._typeOld));
		
    	//Log.d("EventPreferencesFragment.onStart", preferences.getString(PREF_EVENT_NAME, ""));

	}
	
	@Override
	public void onPause()
	{
		super.onPause();

		/*
		if (actionMode != null)
		{
			restart = false; // nerestartovat fragment
			actionMode.finish();
		}
		*/
		
    	//Log.d("EventPreferencesFragment.onPause", "xxxx");
		
	}
	
	@Override
	public void onDestroy()
	{
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        event = null;

		if (dataWrapper != null)
			dataWrapper.invalidateDataWrapper();
		dataWrapper = null;
        
		super.onDestroy();
	}

	public void doOnActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		doOnActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		SharedPreferences preferences = getActivity().getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
    	Editor editor = preferences.edit();
    	if (actionMode != null) 
    		editor.putInt(SP_ACTION_MODE_SHOWED, 2);
    	else
    		editor.putInt(SP_ACTION_MODE_SHOWED, 1);
		editor.commit();
	}	
	
	private void loadPreferences()
	{
    	if (event != null)
    	{
	    	//SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
	    	event.loadSharedPrefereces(preferences);
    	}
		
	}
	
	private void savePreferences()
	{
		//Log.e("EventPreferencesFragment.savePreferences","xxx");
		
		List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();

		if ((new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT) ||
		    (new_event_mode == EditorEventListFragment.EDIT_MODE_DUPLICATE))
		{
	    	event.saveSharedPrefereces(preferences);
			
			// add event into DB
			dataWrapper.getDatabaseHandler().addEvent(event);
			event_id = event._id;

			// restart Events
			dataWrapper.restartEvents(false, false);
			
        	//Log.d("ProfilePreferencesFragment.savePreferences", "addEvent");
			
		}
		else
    	if (event_id > 0) 
        {
	    	event.saveSharedPrefereces(preferences);
	    	
    		// udate event in DB
			dataWrapper.getDatabaseHandler().updateEvent(event);

			if (event.getStatus() == Event.ESTATUS_STOP)
			{
				// pause event
				event.pauseEvent(dataWrapper, eventTimelineList, true, false, false, false);
				// stop event
				event.stopEvent(dataWrapper, eventTimelineList, true, false, true);
			}
			else
				// pause event
				event.pauseEvent(dataWrapper, eventTimelineList, true, false, false, false);
			// restart Events
			dataWrapper.restartEvents(false, false);
			
        	//Log.d("EventPreferencesFragment.savePreferences", "updateEvent");

        }

        onRedrawEventListFragmentCallback.onRedrawEventListFragment(event, new_event_mode);
	}
	
	private void updateSharedPreference()
	{
        if (event != null) 
        {	

	    	// updating activity with selected event preferences
	    	
        	//Log.d("EventPreferencesActivity.updateSharedPreference", event.getName());
    		event.setAllSummary(prefMng, context);
			
        }
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
	{

		//eventTypeChanged = false;
		
		event.setSummary(prefMng, key, sharedPreferences, context);
		
    	Activity activity = getActivity();
    	boolean canShow = (EditorProfilesActivity.mTwoPane) && (activity instanceof EditorProfilesActivity);
    	canShow = canShow || ((!EditorProfilesActivity.mTwoPane) && (activity instanceof EventPreferencesFragmentActivity));
    	if (canShow)
    		showActionMode();
	}
	
	private void createActionModeCallback()
	{
		actionModeCallback = new ActionMode.Callback() {
			 
            /** Invoked whenever the action mode is shown. This is invoked immediately after onCreateActionMode */
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        		MenuItem menuItem = menu.findItem(R.id.event_preferences_action_mode_save);
        		menuItem.setTitle(menuItem.getTitle() + "    ");
        		return true;
            }
 
            /** Called when user exits action mode */
            public void onDestroyActionMode(ActionMode mode) {
                 actionMode = null;
                 if (actionModeButtonClicked == BUTTON_CANCEL)
            	     // cancel button clicked
                 {
            	     onRestartEventPreferencesCallback.onRestartEventPreferences(event, new_event_mode);
                 } 
            }
 
            /** This is called when the action mode is created. This is called by startActionMode() */
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.event_preferences_action_mode, menu);
            	return true;
            }
 
            /** This is called when an item in the context menu is selected */
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.event_preferences_action_mode_save:
        				savePreferences();
        				finishActionMode(BUTTON_SAVE);
                        return true;
                    default:
        				finishActionMode(BUTTON_CANCEL);
                        return false;                        
                }
            }

        };		
	}
	
	@SuppressLint("InflateParams")
	private void showActionMode()
	{
		eventNonEdited = false;
		
		if (actionMode != null)
			actionMode.finish();
		
    	actionModeButtonClicked = BUTTON_UNDEFINED;
    	
    	LayoutInflater inflater = LayoutInflater.from(getActivity());
    	View actionView = inflater.inflate(R.layout.event_preferences_action_mode, null);
    	TextView title = (TextView)actionView.findViewById(R.id.event_preferences_action_menu_title);
       	title.setText(R.string.title_activity_event_preferences);		

        actionMode = ((ActionBarActivity)getActivity()).startSupportActionMode(actionModeCallback);
        actionMode.setCustomView(actionView);
        
        onShowActionModeInEventPreferencesCallback.onShowActionModeInEventPreferences();        
        
        actionMode.getCustomView().findViewById(R.id.event_preferences_action_menu_cancel).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				//Log.d("actionMode.onClick", "cancel");
				
				finishActionMode(BUTTON_CANCEL);
				
			}
       	});

	}
	
	public boolean isActionModeActive()
	{
		return (actionMode != null);
	}
	
	public void finishActionMode(int button)
	{
		int _button = button;
		
		if (_button == BUTTON_SAVE)
			new_event_mode = EditorEventListFragment.EDIT_MODE_UNDEFINED;
		
		if (getActivity() instanceof EventPreferencesFragmentActivity)
		{
			actionModeButtonClicked = BUTTON_UNDEFINED;
			getActivity().finish(); // finish activity;
		}
		else
		if (actionMode != null)
		{	
			actionModeButtonClicked = _button;
			actionMode.finish();
		}
		
        onHideActionModeInEventPreferencesCallback.onHideActionModeInEventPreferences();        
		
	}

	static public Activity getPreferencesActivity()
	{
		return preferencesActivity;
	}
	
}
