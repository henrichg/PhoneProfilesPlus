package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public class EventsPrefsActivity extends AppCompatActivity {

    long event_id = 0;
    private int old_event_status;
    int newEventMode = EditorEventListFragment.EDIT_MODE_UNDEFINED;
    int predefinedEventIndex = 0;

    private int resultCode = RESULT_CANCELED;

    boolean showSaveMenu = false;

    private Toolbar toolbar;

    private MobileCellsRegistrationCountDownBroadcastReceiver mobileCellsRegistrationCountDownBroadcastReceiver = null;
    private MobileCellsRegistrationStoppedBroadcastReceiver mobileCellsRegistrationStoppedBroadcastReceiver = null;

    public static final String PREF_START_TARGET_HELPS = "event_preferences_activity_start_target_helps";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, true/*, false*/, false);
        GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        PPApplication.logE("EventsPrefsActivity.onCreate", "xxx");

        setContentView(R.layout.activity_preferences);

        toolbar = findViewById(R.id.activity_preferences_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        event_id = getIntent().getLongExtra(PPApplication.EXTRA_EVENT_ID, 0L);
        old_event_status = getIntent().getIntExtra(PPApplication.EXTRA_EVENT_STATUS, -1);
        newEventMode = getIntent().getIntExtra(EditorProfilesActivity.EXTRA_NEW_EVENT_MODE, EditorEventListFragment.EDIT_MODE_UNDEFINED);
        predefinedEventIndex = getIntent().getIntExtra(EditorProfilesActivity.EXTRA_PREDEFINED_EVENT_INDEX, 0);

        EventsPrefsFragment preferenceFragment = new EventsPrefsActivity.EventsPrefsRoot();

        if (savedInstanceState == null) {
            loadPreferences(newEventMode, predefinedEventIndex);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_preferences_settings, preferenceFragment)
                    .commit();
        }
        else {
            event_id = savedInstanceState.getLong("event_id", 0);
            old_event_status = savedInstanceState.getInt("old_event_status", -1);
            newEventMode = savedInstanceState.getInt("newEventMode", EditorProfileListFragment.EDIT_MODE_UNDEFINED);
            predefinedEventIndex = savedInstanceState.getInt("predefinedEventIndex", 0);

            showSaveMenu = savedInstanceState.getBoolean("showSaveMenu", false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mobileCellsRegistrationCountDownBroadcastReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_COUNTDOWN);
            mobileCellsRegistrationCountDownBroadcastReceiver = new MobileCellsRegistrationCountDownBroadcastReceiver();
            registerReceiver(mobileCellsRegistrationCountDownBroadcastReceiver, intentFilter);
        }

        if (mobileCellsRegistrationStoppedBroadcastReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_NEW_CELLS);
            mobileCellsRegistrationStoppedBroadcastReceiver = new MobileCellsRegistrationStoppedBroadcastReceiver();
            registerReceiver(mobileCellsRegistrationStoppedBroadcastReceiver, intentFilter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mobileCellsRegistrationCountDownBroadcastReceiver != null) {
            try {
                unregisterReceiver(mobileCellsRegistrationCountDownBroadcastReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            mobileCellsRegistrationCountDownBroadcastReceiver = null;
        }

        if (mobileCellsRegistrationStoppedBroadcastReceiver != null) {
            try {
                unregisterReceiver(mobileCellsRegistrationStoppedBroadcastReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            mobileCellsRegistrationStoppedBroadcastReceiver = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (showSaveMenu) {
            // for shared profile is not needed, for shared profile is used PPApplication.SHARED_PROFILE_PREFS_NAME
            // and this is used in Profile.getSharedProfile()
            //if (profile_id != Profile.SHARED_PROFILE_ID) {
            toolbar.inflateMenu(R.menu.event_preferences_save);
            //}
        }
        else {
            // no menu for shared profile
            //if (profile_id != Profile.SHARED_PROFILE_ID) {
            //toolbar.inflateMenu(R.menu.event_preferences);
            toolbar.getMenu().clear();
            //}
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

        //if (profile_id != Profile.SHARED_PROFILE_ID) {
        // no menu for shared profile

        onNextLayout(toolbar, new Runnable() {
            @Override
            public void run() {
                showTargetHelps();
            }
        });
        //}

        /*final Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showTargetHelps();
            }
        }, 1000);*/

        return ret;
    }

    private void finishActivity() {
        if (showSaveMenu) {
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
        }
        else
            finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() == 0)
                    finishActivity();
                else
                    getSupportFragmentManager().popBackStack();
                return true;
            case R.id.event_preferences_save:
                if (checkPreferences(newEventMode, predefinedEventIndex)) {
                    savePreferences(newEventMode, predefinedEventIndex);
                    resultCode = RESULT_OK;
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_preferences_settings);
        if (fragment != null)
            ((EventsPrefsFragment)fragment).doOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            finishActivity();
        else
            super.onBackPressed();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putLong("event_id", event_id);
        savedInstanceState.putInt("old_event_status", old_event_status);
        savedInstanceState.putInt("newEventMode", newEventMode);
        savedInstanceState.putInt("predefinedEventIndex", predefinedEventIndex);

        savedInstanceState.putBoolean("showSaveMenu", showSaveMenu);
    }

    @Override
    public void finish() {
        // for startActivityForResult
        Intent returnIntent = new Intent();
        returnIntent.putExtra(PPApplication.EXTRA_EVENT_ID, event_id);
        returnIntent.putExtra(EditorProfilesActivity.EXTRA_NEW_EVENT_MODE, newEventMode);
        returnIntent.putExtra(EditorProfilesActivity.EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
        setResult(resultCode,returnIntent);

        super.finish();
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
            // must be used handler for rewrite toolbar title/subtitle
            final String eventName = event._name;
            Handler handler = new Handler(getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Toolbar toolbar = findViewById(R.id.activity_preferences_toolbar);
                    toolbar.setSubtitle(getString(R.string.event_string_0) + ": " + eventName);
                }
            }, 200);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            event.loadSharedPreferences(preferences);
        }
    }

    private boolean checkPreferences(final int new_event_mode, final int predefinedEventIndex)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
                        SharedPreferences settings = ApplicationPreferences.getSharedPreferences(EventsPrefsActivity.this);
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
                        /*SharedPreferences settings = ApplicationPreferences.getSharedPreferences(EventsPrefsActivity.this);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, false);
                        editor.apply();*/

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            event.saveSharedPreferences(preferences, getApplicationContext());
        }
        return event;
    }

    private void savePreferences(int new_event_mode, int predefinedEventIndex)
    {
        PPApplication.logE("EventsPrefsActivity.savePreferences","new_event_mode="+new_event_mode);

        final Event event = getEventFromPreferences(event_id, new_event_mode, predefinedEventIndex);
        if (event == null)
            return;

        PPApplication.logE("EventsPrefsActivity.savePreferences","event._name="+event._name);

        event.setSensorsWaiting();

        final DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);

        dataWrapper.addActivityLog(DataWrapper.ALTYPE_EVENT_PREFERENCES_CHANGED, event._name, null, null, 0);

        if ((new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT) ||
                (new_event_mode == EditorEventListFragment.EDIT_MODE_DUPLICATE))
        {
            // add event into DB
            DatabaseHandler.getInstance(dataWrapper.context).addEvent(event);
            event_id = event._id;

            // restart Events
            PPApplication.logE("$$$ restartEvents","from EventsPrefsActivity.savePreferences");
            PPApplication.setBlockProfileEventActions(true);
            //dataWrapper.restartEvents(false, true, true, true, true);
            dataWrapper.restartEventsWithRescan(/*true, */false, true, true, false);
        }
        else
        if (event_id > 0)
        {
            // update event in DB
            DatabaseHandler.getInstance(dataWrapper.context).updateEvent(event);

            saveUpdateOfPreferences(event, dataWrapper, old_event_status);
        }
    }

    static void saveUpdateOfPreferences(final Event event, final DataWrapper dataWrapper, final int old_event_status) {
        // save preferences into profile
        final List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(true);

        if (event.getStatus() == Event.ESTATUS_STOP)
        {
            PPApplication.startHandlerThread("EventsPrefsActivity.savePreferences.1");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":EventsPrefsActivity_savePreferences_1");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=EventsPrefsActivity.savePreferences.1");

                        if (old_event_status != Event.ESTATUS_STOP) {
                            // pause event - must be called, because status is ESTATUS_STOP
                            event.pauseEvent(dataWrapper, eventTimelineList, true, false,
                                    false, /*false,*/ null, false, false);
                            // stop event
                            event.stopEvent(dataWrapper, eventTimelineList, true, false,
                                    true/*, false*/);

                            // restart Events
                            PPApplication.logE("$$$ restartEvents", "from EventsPrefsActivity.savePreferences");
                            PPApplication.setBlockProfileEventActions(true);
                            //dataWrapper.restartEvents(false, true, true, true, false);
                            dataWrapper.restartEventsWithRescan(/*true, */false, false, true, false);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=EventsPrefsActivity.savePreferences.1");
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
            PPApplication.startHandlerThread("EventsPrefsActivity.savePreferences.2");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":EventsPrefsActivity_savePreferences_2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=EventsPrefsActivity.savePreferences.2");

                        // pause event
                        event.pauseEvent(dataWrapper, eventTimelineList, true, false,
                                false, /*false,*/ null, false, false);
                        // must be called, because status is ESTATUS_PAUSE and in pauseEvent is not called
                        event.doLogForPauseEvent(dataWrapper, false);

                        // restart Events
                        PPApplication.logE("$$$ restartEvents", "from EventsPrefsActivity.savePreferences");
                        PPApplication.setBlockProfileEventActions(true);
                        //dataWrapper.restartEvents(false, true, true, true, false);
                        dataWrapper.restartEventsWithRescan(/*true, */false, false, true, false);

                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=EventsPrefsActivity.savePreferences.2");
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

            Toolbar toolbar = findViewById(R.id.activity_preferences_toolbar);

            //TypedValue tv = new TypedValue();
            //getTheme().resolveAttribute(R.attr.colorAccent, tv, true);

            //final Display display = getWindowManager().getDefaultDisplay();

            //String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true);
            int outerCircleColor = R.color.tabTargetHelpOuterCircleColor;
//                if (appTheme.equals("dark"))
//                    outerCircleColor = R.color.tabTargetHelpOuterCircleColor_dark;
            int targetCircleColor = R.color.tabTargetHelpTargetCircleColor;
//                if (appTheme.equals("dark"))
//                    targetCircleColor = R.color.tabTargetHelpTargetCircleColor_dark;
            int textColor = R.color.tabTargetHelpTextColor;
//                if (appTheme.equals("dark"))
//                    textColor = R.color.tabTargetHelpTextColor_dark;
            //boolean tintTarget = !appTheme.equals("white");

            final TapTargetSequence sequence = new TapTargetSequence(this);
            List<TapTarget> targets = new ArrayList<>();
            int id = 1;
            try {
                targets.add(
                        TapTarget.forToolbarMenuItem(toolbar, R.id.event_preferences_save, getString(R.string.event_preference_activity_targetHelps_save_title), getString(R.string.event_preference_activity_targetHelps_save_description))
                                .outerCircleColor(outerCircleColor)
                                .targetCircleColor(targetCircleColor)
                                .textColor(textColor)
                                .tintTarget(true)
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

    public class MobileCellsRegistrationCountDownBroadcastReceiver extends BroadcastReceiver {

        MobileCellsRegistrationCountDownBroadcastReceiver(/*MobileCellsRegistrationDialogPreferenceX preference*/) {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_preferences_settings);
            if (fragment != null) {
                long millisUntilFinished = intent.getLongExtra(MobileCellsRegistrationService.EXTRA_COUNTDOWN, 0L);
                ((EventsPrefsFragment) fragment).doMobileCellsRegistrationCountDownBroadcastReceiver(millisUntilFinished);
            }
        }
    }

    public class MobileCellsRegistrationStoppedBroadcastReceiver extends BroadcastReceiver {

        MobileCellsRegistrationStoppedBroadcastReceiver(/*MobileCellsPreferenceX preference*/) {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_preferences_settings);
            if (fragment != null)
                ((EventsPrefsFragment)fragment).doMobileCellsRegistrationStoppedBroadcastReceiver();
        }
    }

//--------------------------------------------------------------------------------------------------

    static public class EventsPrefsRoot extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_root, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsStartOfEventsOthers extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_start_of_event_others, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsEndOfEventsOthers extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_end_of_event_others, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsTimeParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_time_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsCalendarParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_calendar_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsBatteryParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_battery_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsCallParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_call_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsSMSParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_sms_mms_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsRadioSwitchParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_radio_switch_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsLocationParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_location_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsWifiParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_wifi_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsBluetoothParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_bluetooth_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsMobileCellsParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_mobile_cells_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsAccessoriesParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_accessories_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsScreenParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_screen_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsNotificationsParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_notification_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsApplicationsParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_application_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsOrientationParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_orientation_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsNFCParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_nfc_sensor, rootKey);
        }
    }

    @SuppressWarnings("unused")
    static public class EventsPrefsAlarmClockParameters extends EventsPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.event_prefs_alarm_clock_sensor, rootKey);
        }
    }

}
