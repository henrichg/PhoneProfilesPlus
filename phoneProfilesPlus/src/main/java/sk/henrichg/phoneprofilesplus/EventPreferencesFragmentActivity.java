package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.fnp.materialpreferences.PreferenceActivity;

import java.util.List;

public class EventPreferencesFragmentActivity extends PreferenceActivity
{

    private long event_id = 0;
    int newEventMode = EditorEventListFragment.EDIT_MODE_UNDEFINED;

    EventPreferencesFragment fragment;

    private int resultCode = RESULT_CANCELED;

    public static boolean showSaveMenu = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // must by called before super.onCreate() for PreferenceActivity
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            GUIData.setTheme(this, false, true);
        else
            GUIData.setTheme(this, false, false);
        GUIData.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_event_preferences);

        /*
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            if (GlobalData.applicationTheme.equals("material"))
                tintManager.setStatusBarTintColor(Color.parseColor("#ff237e9f"));
            else
                tintManager.setStatusBarTintColor(Color.parseColor("#ff202020"));
        }
        */

        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle(R.string.title_activity_event_preferences);

        event_id = getIntent().getLongExtra(GlobalData.EXTRA_EVENT_ID, 0L);
        newEventMode = getIntent().getIntExtra(GlobalData.EXTRA_NEW_EVENT_MODE, EditorEventListFragment.EDIT_MODE_UNDEFINED);

        fragment = new EventPreferencesFragment();

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putLong(GlobalData.EXTRA_EVENT_ID, event_id);
            arguments.putInt(GlobalData.EXTRA_NEW_EVENT_MODE, newEventMode);
            fragment.setArguments(arguments);

            loadPreferences(newEventMode);

            /*getFragmentManager().beginTransaction()
                    .replace(R.id.activity_event_preferences_container, fragment, "EventPreferencesFragment").commit();*/
        }

        setPreferenceFragment(fragment);

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void finish() {

        /*
        EventPreferencesFragment fragment = (EventPreferencesFragment)getFragmentManager().
                findFragmentByTag(GUIData.MAIN_PREFERENCE_FRAGMENT_TAG);
        if (fragment != null)
            event_id = fragment.event_id;
        */

        // for startActivityForResult
        Intent returnIntent = new Intent();
        returnIntent.putExtra(GlobalData.EXTRA_EVENT_ID, event_id);
        returnIntent.putExtra(GlobalData.EXTRA_NEW_EVENT_MODE, newEventMode);
        setResult(resultCode,returnIntent);

        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (showSaveMenu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.profile_preferences_action_mode, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile_preferences_action_mode_save:
                savePreferences(newEventMode);
                resultCode = RESULT_OK;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        GUIData.reloadActivity(this, false);
    }
    */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (fragment != null)
            fragment.doOnActivityResult(requestCode, resultCode, data);
    }

    public static Event createEvent(Context context, long event_id, int new_event_mode, boolean leaveSaveMenu) {
        Event event;
        DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);

        if (!leaveSaveMenu)
            showSaveMenu = false;

        if (new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT)
        {
            // create new event - default is TIME
            event = dataWrapper.getNoinitializedEvent(context.getString(R.string.event_name_default));
            showSaveMenu = true;
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
                    //origEvent._undoneProfile,
                    origEvent._priority,
                    origEvent._delayStart,
                    origEvent._isInDelay,
                    origEvent._atEndDo,
                    origEvent._manualProfileActivation,
                    origEvent._fkProfileStartWhenActivated
            );
            event.copyEventPreferences(origEvent);
            showSaveMenu = true;
        }
        else
            event = dataWrapper.getEventById(event_id);

        return event;
    }

    private void loadPreferences(int new_event_mode) {
        Event event = createEvent(getApplicationContext(), event_id, new_event_mode, false);

        if (event != null)
        {
            String PREFS_NAME;
            if (EventPreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
                PREFS_NAME = EventPreferencesFragment.PREFS_NAME_ACTIVITY;
            else
            if (EventPreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
                PREFS_NAME = EventPreferencesFragment.PREFS_NAME_FRAGMENT;
            else
                PREFS_NAME = EventPreferencesFragment.PREFS_NAME_FRAGMENT;

            SharedPreferences preferences=getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

            event.loadSharedPreferences(preferences);
        }
    }

    private void savePreferences(int new_event_mode)
    {
        DataWrapper dataWrapper = new DataWrapper(getApplicationContext().getApplicationContext(), false, false, 0);
        Event event = createEvent(getApplicationContext(), event_id, new_event_mode, true);

        String PREFS_NAME;
        if (EventPreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
            PREFS_NAME = EventPreferencesFragment.PREFS_NAME_ACTIVITY;
        else
        if (EventPreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
            PREFS_NAME = EventPreferencesFragment.PREFS_NAME_FRAGMENT;
        else
            PREFS_NAME = EventPreferencesFragment.PREFS_NAME_FRAGMENT;

        SharedPreferences preferences=getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

        // save preferences into profile
        List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();

        if ((new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT) ||
                (new_event_mode == EditorEventListFragment.EDIT_MODE_DUPLICATE))
        {
            event.saveSharedPreferences(preferences);

            // add event into DB
            dataWrapper.getDatabaseHandler().addEvent(event);
            event_id = event._id;

            // restart Events
            GlobalData.logE("$$$ restartEvents","from EventPreferencesFragment.savePreferences");
            dataWrapper.restartEvents(false, true);
        }
        else
        if (event_id > 0)
        {
            event.saveSharedPreferences(preferences);

            // udate event in DB
            dataWrapper.getDatabaseHandler().updateEvent(event);

            if (event.getStatus() == Event.ESTATUS_STOP)
            {
                // pause event
                event.pauseEvent(dataWrapper, eventTimelineList, true, false, false, false, null, false);
                // stop event
                event.stopEvent(dataWrapper, eventTimelineList, true, false, true, false, false);
            }
            else
                // pause event
                event.pauseEvent(dataWrapper, eventTimelineList, true, false, false, false, null, false);
            // restart Events
            GlobalData.logE("$$$ restartEvents","from EventPreferencesFragmentActivity.savePreferences");
            dataWrapper.restartEvents(false, true);

        }
    }

}
