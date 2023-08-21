package sk.henrichg.phoneprofilesplus;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.List;

public class EventsPrefsActivity extends AppCompatActivity
                                implements RefreshGUIActivatorEditorListener
{

    long event_id = 0;
    Event event = null;
    private int old_event_status;
    int newEventMode = PPApplication.EDIT_MODE_UNDEFINED;
    int predefinedEventIndex = 0;

    private int resultCode = RESULT_CANCELED;

    boolean showSaveMenu = false;

    private Toolbar toolbar;

    private static final String BUNDLE_OLD_EVENT_STATUS = "old_event_status";
    private static final String BUNDLE_NEW_EVENT_MODE = "newEventMode";
    private static final String BUNDLE_PREDEFINED_EVENT_INDEX = "predefinedEventIndex";

    static private class RefreshGUIBroadcastReceiver extends BroadcastReceiver {

        private final RefreshGUIActivatorEditorListener listener;

        public RefreshGUIBroadcastReceiver(RefreshGUIActivatorEditorListener listener){
            this.listener = listener;
        }

        @Override
        public void onReceive( Context context, Intent intent ) {
            listener.refreshGUIFromListener(intent);
        }
    }
    private RefreshGUIBroadcastReceiver refreshGUIBroadcastReceiver = new RefreshGUIBroadcastReceiver(this);

    static final String PREF_START_TARGET_HELPS = "event_preferences_activity_start_target_helps";
    //static final String PREF_START_TARGET_HELPS_FINISHED = "event_preferences_activity_start_target_helps_finiahed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false, false, false, false, true);
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        toolbar = findViewById(R.id.activity_preferences_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        event_id = getIntent().getLongExtra(PPApplication.EXTRA_EVENT_ID, 0L);
        old_event_status = getIntent().getIntExtra(PPApplication.EXTRA_EVENT_STATUS, -1);
        newEventMode = getIntent().getIntExtra(PPApplication.EXTRA_NEW_EVENT_MODE, PPApplication.EDIT_MODE_UNDEFINED);
        predefinedEventIndex = getIntent().getIntExtra(PPApplication.EXTRA_PREDEFINED_EVENT_INDEX, 0);

        if (getIntent().getBooleanExtra(DataWrapperStatic.EXTRA_FROM_RED_TEXT_PREFERENCES_NOTIFICATION, false)) {
            // check if profile exists in db
            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
            Event event = dataWrapper.getEventById(event_id);
            dataWrapper.invalidateDataWrapper();
            if (event == null) {
                PPApplication.showToast(getApplicationContext(),
                        getString(R.string.event_preferences_event_not_found),
                        Toast.LENGTH_SHORT);
                super.finish();
                return;
            }
        }

        EventsPrefsFragment preferenceFragment = new EventsPrefsRoot();

        if (savedInstanceState == null) {
            loadPreferences(newEventMode, predefinedEventIndex);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_preferences_settings, preferenceFragment)
                    .commit();
        }
        else {
            event_id = savedInstanceState.getLong(PPApplication.EXTRA_EVENT_ID, 0);
            old_event_status = savedInstanceState.getInt(BUNDLE_OLD_EVENT_STATUS, -1);
            newEventMode = savedInstanceState.getInt(BUNDLE_NEW_EVENT_MODE, PPApplication.EDIT_MODE_UNDEFINED);
            predefinedEventIndex = savedInstanceState.getInt(BUNDLE_PREDEFINED_EVENT_INDEX, 0);

            showSaveMenu = savedInstanceState.getBoolean(PPApplication.BUNDLE_SHOW_SAVE_MENU, false);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver(refreshGUIBroadcastReceiver,
                new IntentFilter(PPApplication.ACTION_REFRESH_EVENTS_PREFS_GUI_BROADCAST_RECEIVER));
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshGUIBroadcastReceiver);
            refreshGUIBroadcastReceiver = null;
        } catch (IllegalArgumentException e) {
            //PPApplicationStatic.recordException(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
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

        onNextLayout(toolbar, this::showTargetHelps);

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
            PPAlertDialog dialog = new PPAlertDialog(
                    getString(R.string.not_saved_changes_alert_title),
                    getString(R.string.not_saved_changes_alert_message),
                    getString(R.string.alert_button_yes),
                    getString(R.string.alert_button_no),
                    null, null,
                    (dialog1, which) -> {
                        if (checkPreferences(newEventMode, predefinedEventIndex)) {
                            savePreferences(newEventMode, predefinedEventIndex);
                            resultCode = RESULT_OK;
                            finish();
                        }
                    },
                    (dialog2, which) -> finish(),
                    null,
                    null,
                    null,
                    true, true,
                    false, false,
                    true,
                    this
            );

            if (!isFinishing())
                dialog.show();
        }
        else
            finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0)
                finishActivity();
            else
                getSupportFragmentManager().popBackStack();
            return true;
        }
        else
        if (itemId == R.id.event_preferences_save) {
            if (checkPreferences(newEventMode, predefinedEventIndex)) {
                savePreferences(newEventMode, predefinedEventIndex);
                resultCode = RESULT_OK;
                finish();
            }
            return true;
        }
        else
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

        savedInstanceState.putLong(PPApplication.EXTRA_EVENT_ID, event_id);
        savedInstanceState.putInt(BUNDLE_OLD_EVENT_STATUS, old_event_status);
        savedInstanceState.putInt(BUNDLE_NEW_EVENT_MODE, newEventMode);
        savedInstanceState.putInt(BUNDLE_PREDEFINED_EVENT_INDEX, predefinedEventIndex);

        savedInstanceState.putBoolean(PPApplication.BUNDLE_SHOW_SAVE_MENU, showSaveMenu);
    }

    @Override
    public void finish() {
        // for startActivityForResult
        Intent returnIntent = new Intent();
        returnIntent.putExtra(PPApplication.EXTRA_EVENT_ID, event_id);
        returnIntent.putExtra(PPApplication.EXTRA_NEW_EVENT_MODE, newEventMode);
        returnIntent.putExtra(PPApplication.EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
        setResult(resultCode,returnIntent);

        super.finish();
    }

    private Event createEvent(Context context, long event_id, int new_event_mode, int predefinedEventIndex,
                              boolean leaveSaveMenu) {
        Event event;
        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

        if (!leaveSaveMenu)
            showSaveMenu = false;

        if (new_event_mode == PPApplication.EDIT_MODE_INSERT)
        {
            // create new event - default is TIME
            if (predefinedEventIndex == 0)
                event = DataWrapperStatic.getNonInitializedEvent(context.getString(R.string.event_name_default), 0);
            else
                event = dataWrapper.getPredefinedEvent(predefinedEventIndex-1, false, getBaseContext());
            showSaveMenu = true;
        }
        else
        if (new_event_mode == PPApplication.EDIT_MODE_DUPLICATE)
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
                        origEvent._ignoreManualActivation,
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
                        origEvent._notificationVibrateEnd,
                        //origEvent._atEndHowUndo
                        origEvent._manualProfileActivationAtEnd,
                        origEvent._notificationSoundStartPlayAlsoInSilentMode,
                        origEvent._notificationSoundEndPlayAlsoInSilentMode
                );
                event.copyEventPreferences(origEvent);
                showSaveMenu = true;
            }
            else
                event = null;
        }
        else
            event = dataWrapper.getEventById(event_id);

        dataWrapper.invalidateDataWrapper();

        return event;
    }

    private void loadPreferences(int new_event_mode, int predefinedEventIndex) {
        event = createEvent(getApplicationContext(), event_id, new_event_mode, predefinedEventIndex, false);
        if (event == null)
            event = createEvent(getApplicationContext(), event_id, PPApplication.EDIT_MODE_INSERT, predefinedEventIndex, false);

        if (event != null)
        {
            // must be used handler for rewrite toolbar title/subtitle
            final String eventName = event._name;
            Handler handler = new Handler(getMainLooper());
            handler.postDelayed(() -> {
//                    PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EventsPrefsActivity.loadPreferences");
                //Toolbar toolbar = findViewById(R.id.activity_preferences_toolbar);
                //toolbar.setSubtitle(getString(R.string.event_string_0) + ": " + eventName);
                toolbar.setTitle(getString(R.string.event_string_0) + StringConstants.STR_COLON_WITH_SPACE + eventName);
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
            if (!ApplicationPreferences.applicationEventNeverAskForEnableRun) {
                //if (new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT) {

                /*
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
                doNotShowAgain.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    SharedPreferences settings = ApplicationPreferences.getSharedPreferences(EventsPrefsActivity.this);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, isChecked);
                    editor.apply();
                    ApplicationPreferences.applicationEventNeverAskForEnableRun(getApplicationContext());
                });

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(R.string.phone_preferences_actionMode_save);
                dialogBuilder.setMessage(R.string.alert_message_enable_event);
                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                //dialogBuilder.setView(doNotShowAgain);
                dialogBuilder.setView(superContainer);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                    SharedPreferences preferences1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences1.edit();
                    editor.putBoolean(Event.PREF_EVENT_ENABLED, true);
                    editor.apply();

                    savePreferences(new_event_mode, predefinedEventIndex);
                    resultCode = RESULT_OK;
                    finish();
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, (dialog, which) -> {
                    savePreferences(new_event_mode, predefinedEventIndex);
                    resultCode = RESULT_OK;
                    finish();
                });
                AlertDialog dialog = dialogBuilder.create();

//                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(DialogInterface dialog) {
//                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                        if (positive != null) positive.setAllCaps(false);
//                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                        if (negative != null) negative.setAllCaps(false);
//                    }
//                });
                */

                PPAlertDialog dialog = new PPAlertDialog(getString(R.string.phone_preferences_actionMode_save),
                        getString(R.string.alert_message_enable_event),
                        getString(R.string.alert_button_yes), getString(R.string.alert_button_no),
                        null,
                        getString(R.string.alert_message_enable_event_check_box),
                        (dialog1, which) -> {
                            SharedPreferences preferences1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = preferences1.edit();
                            editor.putBoolean(Event.PREF_EVENT_ENABLED, true);
                            editor.apply();

                            savePreferences(new_event_mode, predefinedEventIndex);
                            resultCode = RESULT_OK;
                            finish();
                        },
                        (dialog2, which) -> {
                            savePreferences(new_event_mode, predefinedEventIndex);
                            resultCode = RESULT_OK;
                            finish();
                        },
                        null,
                        null,
                        (buttonView, isChecked) -> {
                            SharedPreferences settings = ApplicationPreferences.getSharedPreferences(EventsPrefsActivity.this);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, isChecked);
                            editor.apply();
                            ApplicationPreferences.applicationEventNeverAskForEnableRun(getApplicationContext());
                        },
                        true, true,
                        false, true,
                        false,
                        this
                );

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
        final Event event = getEventFromPreferences(event_id, new_event_mode, predefinedEventIndex);
        if (event == null)
            return;

        event.setSensorsWaiting();

        final DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

        if ((new_event_mode == PPApplication.EDIT_MODE_INSERT) ||
                (new_event_mode == PPApplication.EDIT_MODE_DUPLICATE))
        {
            PPApplicationStatic.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_EVENT_ADDED, event._name, null, "");

            // add event into DB
            DatabaseHandler.getInstance(dataWrapper.context).addEvent(event);
            event_id = event._id;

            // restart Events
            PPApplicationStatic.setBlockProfileEventActions(true);
            //dataWrapper.restartEvents(false, true, true, true, true);
            dataWrapper.restartEventsWithRescan(true, false, true, false, true, false);
        }
        else
        if (event_id > 0)
        {
            PPApplicationStatic.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_EVENT_PREFERENCES_CHANGED, event._name, null, "");

            // update event in DB
            DatabaseHandler.getInstance(dataWrapper.context).updateEvent(event);

            saveUpdateOfPreferences(event, dataWrapper, old_event_status);
        }
    }

    static void saveUpdateOfPreferences(final Event event, final DataWrapper dataWrapper, final int old_event_status) {
        // save preferences into profile
        dataWrapper.getEventTimelineList(true);

        //noinspection IfStatementWithIdenticalBranches
        if (event.getStatus() == Event.ESTATUS_STOP)
        {
            //PPApplication.startHandlerThread(/*"EventsPrefsActivity.savePreferences.1"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
            //__handler.post(new SaveUpdateOfPreferencesRunnable(dataWrapper, event) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=EventsPrefsActivity.saveUpdateOfPreferences.1");

                //DataWrapper dataWrapper = dataWrapperWeakRef.get();
                //Event event = eventWeakRef.get();

                //if ((dataWrapper != null) && (event != null)) {
                    PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_EventsPrefsActivity_saveUpdateOfPreferences_1);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (old_event_status != Event.ESTATUS_STOP) {
                            synchronized (PPApplication.eventsHandlerMutex) {

                                synchronized (PPApplication.eventsHandlerMutex) {
                                    // pause event - must be called, because status is ESTATUS_STOP
                                    event.pauseEvent(dataWrapper, true, false,
                                            false, false, null, false, false, true);
                                    // stop event
                                    event.stopEvent(dataWrapper, true, false,
                                            true, true, true);
                                }

                                PPApplicationStatic.setBlockProfileEventActions(true);
                            }
                            // restart Events
                            //dataWrapper.restartEvents(false, true, true, true, false);
                            dataWrapper.restartEventsWithRescan(true, false, false, false, true, false);
                        }

                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            }; //);
            PPApplicationStatic.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);
        }
        else {
            //PPApplication.startHandlerThread(/*"EventsPrefsActivity.savePreferences.2"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
            //__handler.post(new SaveUpdateOfPreferencesRunnable(dataWrapper, event) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=EventsPrefsActivity.saveUpdateOfPreferences.2");

                //DataWrapper dataWrapper = dataWrapperWeakRef.get();
                //Event event = eventWeakRef.get();

                //if ((dataWrapper != null) && (event != null)) {
                    PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_EventsPrefsActivity_saveUpdateOfPreferences_2);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        // pause event
                        event.pauseEvent(dataWrapper, true, false,
                                false, false, null, false, false, true);
                        // must be called, because status is ESTATUS_PAUSE and in pauseEvent is not called
                        // ESTATUS_PAUSE is set in Event.saveSharedPreferences()
                        event.doLogForPauseEvent(dataWrapper.context, false);

                        // restart Events
                        PPApplicationStatic.setBlockProfileEventActions(true);
                        //dataWrapper.restartEvents(false, true, true, true, false);
                        dataWrapper.restartEventsWithRescan(true, false, false, false, true, false);

                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            }; //);
            PPApplicationStatic.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);
        }
    }

    private void showTargetHelps() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        if (!showSaveMenu)
            return;

        if (ApplicationPreferences.prefEventPrefsActivityStartTargetHelps) {
            //Log.d("EventPrefsActivity.showTargetHelps", "PREF_START_TARGET_HELPS=true");

            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
            editor.putBoolean(PREF_START_TARGET_HELPS, false);
            editor.apply();
            ApplicationPreferences.prefEventPrefsActivityStartTargetHelps = false;

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
            int titleTextColor = R.color.tabTargetHelpTitleTextColor;
            int descriptionTextColor = R.color.tabTargetHelpDescriptionTextColor;
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
                                .titleTextColor(titleTextColor)
                                .descriptionTextColor(descriptionTextColor)
                                .textTypeface(Typeface.DEFAULT_BOLD)
                                .tintTarget(true)
                                .drawShadow(true)
                                .id(id)
                );
                ++id;
            } catch (Exception e) {
                //PPApplicationStatic.recordException(e);
            }

            sequence.targets(targets);
            sequence.listener(new TapTargetSequence.Listener() {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                @Override
                public void onSequenceFinish() {
                    //targetHelpsSequenceStarted = false;

                    //SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
                    //editor.putBoolean(PREF_START_TARGET_HELPS_FINISHED, true);
                    //editor.apply();
                    //ApplicationPreferences.prefEventPrefsActivityStartTargetHelpsFinished = true;

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

            //editor = ApplicationPreferences.getEditor(getApplicationContext());
            //editor.putBoolean(PREF_START_TARGET_HELPS_FINISHED, false);
            //editor.apply();
            //ApplicationPreferences.prefEventPrefsActivityStartTargetHelpsFinished = false;

            sequence.start();
        }
    }

//--------------------------------------------------------------------------------------------------

    void changeCurentLightSensorValue() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_preferences_settings);
        if (fragment != null)
            ((EventsPrefsFragment)fragment).changeCurentLightSensorValue();
    }

/*    private static abstract class SaveUpdateOfPreferencesRunnable implements Runnable {

        final WeakReference<DataWrapper> dataWrapperWeakRef;
        final WeakReference<Event> eventWeakRef;

        SaveUpdateOfPreferencesRunnable(DataWrapper dataWrapper,
                                                Event event) {
            this.dataWrapperWeakRef = new WeakReference<>(dataWrapper);
            this.eventWeakRef = new WeakReference<>(event);
        }

    }*/

    @Override
    public void refreshGUIFromListener(Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] EventsPrefsActivity.refreshGUIBroadcastReceiver", "xxx");
        changeCurentLightSensorValue();
    }

}
