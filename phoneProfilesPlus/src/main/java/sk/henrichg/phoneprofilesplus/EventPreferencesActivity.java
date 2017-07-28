package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.List;

public class EventPreferencesActivity extends PreferenceActivity
                                implements PreferenceFragment.OnCreateNestedPreferenceFragment
{

    private long event_id = 0;
    private int newEventMode = EditorEventListFragment.EDIT_MODE_UNDEFINED;
    private int predefinedEventIndex = 0;

    private int resultCode = RESULT_CANCELED;

    public static boolean showSaveMenu = false;

    //private boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "event_preferences_activity_start_target_helps";

    @SuppressLint("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        // must by called before super.onCreate() for PreferenceActivity
        GlobalGUIRoutines.setTheme(this, false, true);
        GlobalGUIRoutines.setLanguage(getBaseContext());

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
            if (ApplicationPreferences.applicationTheme(getApplicationContext()).equals("material"))
                tintManager.setStatusBarTintColor(Color.parseColor("#ff237e9f"));
            else
                tintManager.setStatusBarTintColor(Color.parseColor("#ff202020"));
        }
        else
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ApplicationPreferences.applicationTheme(getApplicationContext()).equals("material"))
                getWindow().setStatusBarColor(Color.parseColor("#1d6681"));
            else
                getWindow().setStatusBarColor(Color.parseColor("#141414"));
        }

        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle(R.string.title_activity_event_preferences);

        event_id = getIntent().getLongExtra(PPApplication.EXTRA_EVENT_ID, 0L);
        newEventMode = getIntent().getIntExtra(EditorProfilesActivity.EXTRA_NEW_EVENT_MODE, EditorEventListFragment.EDIT_MODE_UNDEFINED);
        predefinedEventIndex = getIntent().getIntExtra(EditorProfilesActivity.EXTRA_PREDEFINED_EVENT_INDEX, 0);

        EventPreferencesNestedFragment fragment = createFragment(false);

        if (savedInstanceState == null)
            loadPreferences(newEventMode, predefinedEventIndex);

        setPreferenceFragment(fragment);

    }

    private EventPreferencesNestedFragment createFragment(boolean nested) {
        EventPreferencesNestedFragment fragment;
        if (nested)
            fragment = new EventPreferencesNestedFragment();
        else
            fragment = new EventPreferencesFragment();

        Bundle arguments = new Bundle();
        arguments.putLong(PPApplication.EXTRA_EVENT_ID, event_id);
        arguments.putInt(EditorProfilesActivity.EXTRA_NEW_EVENT_MODE, newEventMode);
        arguments.putInt(EditorProfilesActivity.EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
        arguments.putInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
        arguments.putBoolean(PreferenceFragment.EXTRA_NESTED, nested);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void finish() {

        /*
        EventPreferencesFragment fragment = (EventPreferencesFragment)getFragmentManager().
                findFragmentByTag(GlobalGUIRoutines.MAIN_PREFERENCE_FRAGMENT_TAG);
        if (fragment != null)
            event_id = fragment.event_id;
        */

        // for startActivityForResult
        Intent returnIntent = new Intent();
        returnIntent.putExtra(PPApplication.EXTRA_EVENT_ID, event_id);
        returnIntent.putExtra(EditorProfilesActivity.EXTRA_NEW_EVENT_MODE, newEventMode);
        returnIntent.putExtra(EditorProfilesActivity.EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
        setResult(resultCode,returnIntent);

        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (showSaveMenu) {
            //MenuInflater inflater = getMenuInflater();
            //inflater.inflate(R.menu.profile_preferences_save, menu);
            Toolbar toolbar = (Toolbar) findViewById(R.id.mp_toolbar);
            toolbar.inflateMenu(R.menu.event_preferences_save);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);

        final Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showTargetHelps();
            }
        }, 1000);

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.event_preferences_save:
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
        PreferenceFragment fragment = getFragment();
        if (fragment != null)
            ((EventPreferencesNestedFragment)fragment).doOnActivityResult(requestCode, resultCode, data);
    }

    private Event createEvent(Context context, long event_id, int new_event_mode, int predefinedEventIndex,
                             boolean leaveSaveMenu) {
        Event event;
        DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);

        if (!leaveSaveMenu)
            showSaveMenu = false;

        if (new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT)
        {
            // create new event - default is TIME
            if (predefinedEventIndex == 0)
                event = dataWrapper.getNonInitializedEvent(context.getString(R.string.event_name_default), 0);
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
            SharedPreferences preferences=getSharedPreferences(EventPreferencesNestedFragment.getPreferenceName(PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY), Activity.MODE_PRIVATE);

            event.loadSharedPreferences(preferences);
        }
    }

    private boolean checkPreferences(final int new_event_mode, final int predefinedEventIndex)
    {
        if (new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT) {
            final SharedPreferences preferences = getSharedPreferences(EventPreferencesNestedFragment.getPreferenceName(PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY), Activity.MODE_PRIVATE);
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
                        editor.apply();
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

        String PREFS_NAME = EventPreferencesNestedFragment.getPreferenceName(PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY);

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
            PPApplication.logE("$$$ restartEvents","from EventPreferencesFragment.savePreferences");
            dataWrapper.restartEvents(false, true, false);
        }
        else
        if (event_id > 0)
        {
            event.saveSharedPreferences(preferences, dataWrapper.context);

            // update event in DB
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
            PPApplication.logE("$$$ restartEvents","from EventPreferencesActivity.savePreferences");
            dataWrapper.restartEvents(false, true, false);

        }
    }

    @Override
    public PreferenceFragment onCreateNestedPreferenceFragment() {
        return createFragment(true);
    }

    private void showTargetHelps() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        if (!showSaveMenu)
            return;

        ApplicationPreferences.getSharedPreferences(this);

        if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {
            //Log.d("EventPreferencesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=true");

            SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
            editor.putBoolean(PREF_START_TARGET_HELPS, false);
            editor.apply();

            Toolbar toolbar = (Toolbar) findViewById(R.id.mp_toolbar);

            //TypedValue tv = new TypedValue();
            //getTheme().resolveAttribute(R.attr.colorAccent, tv, true);

            //final Display display = getWindowManager().getDefaultDisplay();

            int circleColor = 0xFFFFFF;
            if (ApplicationPreferences.applicationTheme(getApplicationContext()).equals("dark"))
                circleColor = 0x7F7F7F;

            final TapTargetSequence sequence = new TapTargetSequence(this);
            sequence.targets(
                    TapTarget.forToolbarMenuItem(toolbar, R.id.event_preferences_save, getString(R.string.event_preference_activity_targetHelps_save_title), getString(R.string.event_preference_activity_targetHelps_save_description))
                            .targetCircleColorInt(circleColor)
                            .textColorInt(0xFFFFFF)
                            .drawShadow(true)
                            .id(1)
            );
            sequence.listener(new TapTargetSequence.Listener() {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                @Override
                public void onSequenceFinish() {
                    //targetHelpsSequenceStarted = false;
                }

                @Override
                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                    //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {
                    //targetHelpsSequenceStarted = false;
                }
            });
            sequence.continueOnCancel(true)
                    .considerOuterCircleCanceled(true);
            //targetHelpsSequenceStarted = true;
            sequence.start();
        }
    }
}
