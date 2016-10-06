package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.fnp.materialpreferences.PreferenceActivity;
import com.fnp.materialpreferences.PreferenceFragment;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.List;

public class EventPreferencesFragmentActivity extends PreferenceActivity
                                implements PreferenceFragment.OnCreateNestedPreferenceFragment
{

    private long event_id = 0;
    private int newEventMode = EditorEventListFragment.EDIT_MODE_UNDEFINED;
    private int predefinedEventIndex = 0;

    EventPreferencesNestedFragment fragment;

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

        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle(R.string.title_activity_event_preferences);

        event_id = getIntent().getLongExtra(GlobalData.EXTRA_EVENT_ID, 0L);
        newEventMode = getIntent().getIntExtra(GlobalData.EXTRA_NEW_EVENT_MODE, EditorEventListFragment.EDIT_MODE_UNDEFINED);
        predefinedEventIndex = getIntent().getIntExtra(GlobalData.EXTRA_PREDEFINED_EVENT_INDEX, 0);

        fragment = createFragment(false);

        if (savedInstanceState == null) {
            loadPreferences(newEventMode, predefinedEventIndex);
        }

        setPreferenceFragment(fragment);
    }

    private EventPreferencesNestedFragment createFragment(boolean nested) {
        EventPreferencesNestedFragment fragment;
        if (nested)
            fragment = new EventPreferencesNestedFragment();
        else
            fragment = new EventPreferencesFragment();

        Bundle arguments = new Bundle();
        arguments.putLong(GlobalData.EXTRA_EVENT_ID, event_id);
        arguments.putInt(GlobalData.EXTRA_NEW_EVENT_MODE, newEventMode);
        arguments.putInt(GlobalData.EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
        fragment.setArguments(arguments);

        return fragment;
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
        returnIntent.putExtra(GlobalData.EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
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
                if (checkPreferences(newEventMode, predefinedEventIndex)) {
                    savePreferences(newEventMode, predefinedEventIndex);
                    resultCode = RESULT_OK;
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (fragment != null)
            fragment.doOnActivityResult(requestCode, resultCode, data);
    }

    public Event createEvent(Context context, long event_id, int new_event_mode, int predefinedEventIndex, boolean leaveSaveMenu) {
        Event event;
        DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);

        if (!leaveSaveMenu)
            showSaveMenu = false;

        if (new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT)
        {
            // create new event - default is TIME
            if (predefinedEventIndex == 0)
                event = dataWrapper.getNoinitializedEvent(context.getString(R.string.event_name_default), 0);
            else
                event = dataWrapper.getPredefinedEvent(predefinedEventIndex-1, false);
            showSaveMenu = true;
        }
        else
        if (new_event_mode == EditorEventListFragment.EDIT_MODE_DUPLICATE)
        {
            // duplicate event
            Event origEvent = dataWrapper.getEventById(event_id);
            event = new Event(
                    origEvent._name+"_d",
                    origEvent._startOrder,
                    origEvent._fkProfileStart,
                    origEvent._fkProfileEnd,
                    origEvent.getStatus(),
                    origEvent._notificationSound,
                    origEvent._forceRun,
                    origEvent._blocked,
                    //origEvent._undoneProfile,
                    origEvent._priority,
                    origEvent._delayStart,
                    origEvent._isInDelayStart,
                    origEvent._atEndDo,
                    origEvent._manualProfileActivation,
                    origEvent._fkProfileStartWhenActivated,
                    origEvent._delayEnd,
                    origEvent._isInDelayEnd,
                    origEvent._startStatusTime,
                    origEvent._pauseStatusTime
            );
            event.copyEventPreferences(origEvent);
            showSaveMenu = true;
        }
        else
            event = dataWrapper.getEventById(event_id);

        return event;
    }

    private void loadPreferences(int new_event_mode, int predefinedEventIndex) {
        Event event = createEvent(getApplicationContext(), event_id, new_event_mode, predefinedEventIndex, false);

        if (event != null)
        {
            SharedPreferences preferences=getSharedPreferences(EventPreferencesFragment.getPreferenceName(), Activity.MODE_PRIVATE);

            event.loadSharedPreferences(preferences);
        }
    }

    private boolean checkPreferences(final int new_event_mode, final int predefinedEventIndex)
    {
        if (new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT) {
            final SharedPreferences preferences = getSharedPreferences(EventPreferencesFragment.getPreferenceName(), Activity.MODE_PRIVATE);
            boolean enabled = preferences.getBoolean(Event.PREF_EVENT_ENABLED, false);
            if (!enabled) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getResources().getString(R.string.menu_new_event));
                dialogBuilder.setMessage(getResources().getString(R.string.alert_message_enable_event));
                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(Event.PREF_EVENT_ENABLED, true);
                        editor.commit();
                        savePreferences(new_event_mode, predefinedEventIndex);
                        resultCode = RESULT_OK;
                        finish();
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        savePreferences(new_event_mode, predefinedEventIndex);
                        resultCode = RESULT_OK;
                        finish();
                    }
                });
                dialogBuilder.show();
                return false;
            }
        }
        return true;
    }

    private void savePreferences(int new_event_mode, int predefinedEventIndex)
    {
        DataWrapper dataWrapper = new DataWrapper(getApplicationContext().getApplicationContext(), false, false, 0);
        Event event = createEvent(getApplicationContext(), event_id, new_event_mode, predefinedEventIndex, true);

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
            event.saveSharedPreferences(preferences, dataWrapper.context);

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
            event.saveSharedPreferences(preferences, dataWrapper.context);

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

    @Override
    public PreferenceFragment onCreateNestedPreferenceFragment() {
        return createFragment(true);
    }
}
