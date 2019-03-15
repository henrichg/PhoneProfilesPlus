package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;

public class EventPreferencesActivity extends PreferenceActivity
                                implements PreferenceFragment.OnCreateNestedPreferenceFragment
{

    private long event_id = 0;
    private int old_event_status;
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
        GlobalGUIRoutines.setTheme(this, false, true, false);
        GlobalGUIRoutines.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_event_preferences);

        if (/*(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) &&*/ (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (ApplicationPreferences.applicationTheme(getApplicationContext(), true)) {
                case "color":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
                    break;
                case "white":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primaryDark19_white));
                    break;
                default:
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
                    break;
            }
        }

        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle(R.string.title_activity_event_preferences);

        event_id = getIntent().getLongExtra(PPApplication.EXTRA_EVENT_ID, 0L);
        old_event_status = getIntent().getIntExtra(PPApplication.EXTRA_EVENT_STATUS, -1);
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
            toolbar.inflateMenu(R.menu.event_preferences_save);
        }
        return true;
    }

    private static void onNextLayout(final View view, final Runnable runnable) {
        final ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ViewTreeObserver trueObserver;

                if (observer.isAlive()) {
                    trueObserver = observer;
                } else {
                    trueObserver = view.getViewTreeObserver();
                }

                trueObserver.removeOnGlobalLayoutListener(this);

                runnable.run();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);

        onNextLayout(toolbar, new Runnable() {
            @Override
            public void run() {
                showTargetHelps();
            }
        });

        /*
        final Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showTargetHelps();
            }
        }, 1000);
        */

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
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
    public void onBackPressed() {
        if (!showSaveMenu)
            super.onBackPressed();
        else {
            boolean nested = false;
            PreferenceFragment fragment = getFragment();
            if (fragment != null) {
                Bundle bundle = fragment.getArguments();
                if (bundle.getBoolean(PreferenceFragment.EXTRA_NESTED, false))
                    nested = true;
            }
            if (!nested) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.not_saved_changes_alert_title);
                dialogBuilder.setMessage(R.string.not_saved_changes_alert_message);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkPreferences(newEventMode, predefinedEventIndex)) {
                            savePreferences(newEventMode, predefinedEventIndex);
                            resultCode = RESULT_OK;
                            finish();
                        }
                    }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EventPreferencesActivity.super.onBackPressed();
                    }
                });
                AlertDialog dialog = dialogBuilder.create();
                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        if (positive != null) positive.setAllCaps(false);
                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        if (negative != null) negative.setAllCaps(false);
                    }
                });*/
                if (!isFinishing())
                    dialog.show();
            }
            else
                super.onBackPressed();
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
        DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);

        if (!leaveSaveMenu)
            showSaveMenu = false;

        if (new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT)
        {
            // create new event - default is TIME
            if (predefinedEventIndex == 0)
                event = DataWrapper.getNonInitializedEvent(context.getString(R.string.event_name_default), 0);
            else
                event = dataWrapper.getPredefinedEvent(predefinedEventIndex-1, false, getBaseContext());
            showSaveMenu = true;
        }
        else
        if (new_event_mode == EditorEventListFragment.EDIT_MODE_DUPLICATE)
        {
            // duplicate event
            Event origEvent = dataWrapper.getEventById(event_id);
            if (origEvent != null) {
                event = new Event(
                        origEvent._name + "_d",
                        origEvent._startOrder,
                        origEvent._fkProfileStart,
                        origEvent._fkProfileEnd,
                        origEvent.getStatus(),
                        origEvent._notificationSoundStart,
                        origEvent._forceRun,
                        origEvent._blocked,
                        //origEvent._undoneProfile,
                        origEvent._priority,
                        origEvent._delayStart,
                        origEvent._isInDelayStart,
                        origEvent._atEndDo,
                        origEvent._manualProfileActivation,
                        origEvent._startWhenActivatedProfile,
                        origEvent._delayEnd,
                        origEvent._isInDelayEnd,
                        origEvent._startStatusTime,
                        origEvent._pauseStatusTime,
                        origEvent._notificationVibrateStart,
                        origEvent._noPauseByManualActivation,
                        origEvent._repeatNotificationStart,
                        origEvent._repeatNotificationIntervalStart,
                        origEvent._notificationSoundEnd,
                        origEvent._notificationVibrateEnd
                );
                event.copyEventPreferences(origEvent);
                showSaveMenu = true;
            }
            else
                event = null;
        }
        else
            event = dataWrapper.getEventById(event_id);

        return event;
    }

    private void loadPreferences(int new_event_mode, int predefinedEventIndex) {
        Event event = createEvent(getApplicationContext(), event_id, new_event_mode, predefinedEventIndex, false);
        if (event == null)
            event = createEvent(getApplicationContext(), event_id, EditorEventListFragment.EDIT_MODE_INSERT, predefinedEventIndex, false);

        if (event != null)
        {
            Toolbar toolbar = findViewById(R.id.mp_toolbar);
            toolbar.setSubtitle(getString(R.string.event_string_0) + ": " + event._name);

            SharedPreferences preferences=getSharedPreferences(EventPreferencesNestedFragment.getPreferenceName(PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY), Activity.MODE_PRIVATE);

            event.loadSharedPreferences(preferences);
        }
    }

    private boolean checkPreferences(final int new_event_mode, final int predefinedEventIndex)
    {
        final SharedPreferences preferences = getSharedPreferences(EventPreferencesNestedFragment.getPreferenceName(PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY), Activity.MODE_PRIVATE);
        boolean enabled = preferences.getBoolean(Event.PREF_EVENT_ENABLED, false);
        if (!enabled) {
            if (!ApplicationPreferences.applicationEventNeverAskForEnableRun(this)) {
                //if (new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT) {
                final AppCompatCheckBox doNotShowAgain = new AppCompatCheckBox(this);

                FrameLayout container = new FrameLayout(this);
                container.addView(doNotShowAgain);
                FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                containerParams.leftMargin = GlobalGUIRoutines.dpToPx(20);
                container.setLayoutParams(containerParams);

                FrameLayout superContainer = new FrameLayout(this);
                superContainer.addView(container);

                doNotShowAgain.setText(R.string.alert_message_enable_event_check_box);
                doNotShowAgain.setChecked(false);
                doNotShowAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SharedPreferences settings = ApplicationPreferences.getSharedPreferences(EventPreferencesActivity.this);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, isChecked);
                        editor.apply();
                    }
                });

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.phone_preferences_actionMode_save);
                dialogBuilder.setMessage(R.string.alert_message_enable_event);
                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                //dialogBuilder.setView(doNotShowAgain);
                dialogBuilder.setView(superContainer);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences settings = ApplicationPreferences.getSharedPreferences(EventPreferencesActivity.this);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, false);
                        editor.apply();

                        String PREFS_NAME = EventPreferencesNestedFragment.getPreferenceName(PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
                        SharedPreferences preferences=getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
                        editor = preferences.edit();
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
                AlertDialog dialog = dialogBuilder.create();
                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        if (positive != null) positive.setAllCaps(false);
                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        if (negative != null) negative.setAllCaps(false);
                    }
                });*/
                if (!isFinishing())
                    dialog.show();
                return false;
            }
            //}
        }
        return true;
    }

    Event getEventFromPreferences(long event_id, int new_event_mode, int predefinedEventIndex) {
        final Event event = createEvent(getApplicationContext(), event_id, new_event_mode, predefinedEventIndex, true);
        if (event != null) {
            String PREFS_NAME = EventPreferencesNestedFragment.getPreferenceName(PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
            SharedPreferences preferences=getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
            event.saveSharedPreferences(preferences, getApplicationContext());
        }
        return event;
    }

    private void savePreferences(int new_event_mode, int predefinedEventIndex)
    {
        PPApplication.logE("EventPreferencesActivity.savePreferences","new_event_mode="+new_event_mode);

        final Event event = getEventFromPreferences(event_id, new_event_mode, predefinedEventIndex);
        if (event == null)
            return;

        PPApplication.logE("EventPreferencesActivity.savePreferences","event._name="+event._name);

        event.setSensorsWaiting();

        final DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);

        dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_EVENTPREFERENCESCHANGED, event._name, null, null, 0);

        // save preferences into profile
        final List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();

        if ((new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT) ||
                (new_event_mode == EditorEventListFragment.EDIT_MODE_DUPLICATE))
        {
            // add event into DB
            DatabaseHandler.getInstance(dataWrapper.context).addEvent(event);
            event_id = event._id;

            // restart Events
            PPApplication.logE("$$$ restartEvents","from EventPreferencesActivity.savePreferences");
            PPApplication.setBlockProfileEventActions(true);
            dataWrapper.restartEvents(false, true, true, true, true);
        }
        else
        if (event_id > 0)
        {
            // update event in DB
            DatabaseHandler.getInstance(dataWrapper.context).updateEvent(event);

            if (event.getStatus() == Event.ESTATUS_STOP)
            {
                final int _old_event_status = old_event_status;
                PPApplication.startHandlerThread("EventPreferencesActivity.savePreferences.1");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":EditorPreferencesActivity_savePreferences_1");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            PPApplication.logE("PPApplication.startHandlerThread", "START run - from=EventPreferencesActivity.savePreferences.1");

                            if (_old_event_status != Event.ESTATUS_STOP) {
                                // pause event - must be called, because status is ESTATUS_STOP
                                event.pauseEvent(dataWrapper, eventTimelineList, true, false,
                                        false, /*false,*/ null, false, false);
                                // stop event
                                event.stopEvent(dataWrapper, eventTimelineList, true, false,
                                        true/*, false*/);

                                // restart Events
                                PPApplication.logE("$$$ restartEvents", "from EventPreferencesActivity.savePreferences");
                                PPApplication.setBlockProfileEventActions(true);
                                dataWrapper.restartEvents(false, true, true, true, false);
                            }

                            PPApplication.logE("PPApplication.startHandlerThread", "END run - from=EventPreferencesActivity.savePreferences.1");
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                });
            }
            else {
                PPApplication.startHandlerThread("EventPreferencesActivity.savePreferences.2");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":EditorPreferencesActivity_savePreferences_2");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            PPApplication.logE("PPApplication.startHandlerThread", "START run - from=EventPreferencesActivity.savePreferences.2");

                            // pause event
                            event.pauseEvent(dataWrapper, eventTimelineList, true, false,
                                    false, /*false,*/ null, false, false);
                            // must be called, because status is ESTATUS_PAUSE and in pauseEvent is not called
                            event.doLogForPauseEvent(dataWrapper, false);

                            // restart Events
                            PPApplication.logE("$$$ restartEvents", "from EventPreferencesActivity.savePreferences");
                            PPApplication.setBlockProfileEventActions(true);
                            dataWrapper.restartEvents(false, true, true, true, false);

                            PPApplication.logE("PPApplication.startHandlerThread", "END run - from=EventPreferencesActivity.savePreferences.2");
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                });
            }
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

            Toolbar toolbar = findViewById(R.id.mp_toolbar);

            //TypedValue tv = new TypedValue();
            //getTheme().resolveAttribute(R.attr.colorAccent, tv, true);

            //final Display display = getWindowManager().getDefaultDisplay();

            String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true);
            int circleColor = R.color.tabTargetHelpCircleColor;
            if (appTheme.equals("dark"))
                circleColor = R.color.tabTargetHelpCircleColor_dark;
            int textColor = R.color.tabTargetHelpTextColor;
            if (appTheme.equals("white"))
                textColor = R.color.tabTargetHelpTextColor_white;
            boolean tintTarget = !appTheme.equals("white");

            final TapTargetSequence sequence = new TapTargetSequence(this);
            List<TapTarget> targets = new ArrayList<>();
            int id = 1;
            try {
                targets.add(
                        TapTarget.forToolbarMenuItem(toolbar, R.id.event_preferences_save, getString(R.string.event_preference_activity_targetHelps_save_title), getString(R.string.event_preference_activity_targetHelps_save_description))
                                .targetCircleColor(circleColor)
                                .textColor(textColor)
                                .tintTarget(tintTarget)
                                .drawShadow(true)
                                .id(id)
                );
                ++id;
            } catch (Exception ignored) {} // not in action bar?

            sequence.targets(targets);
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
