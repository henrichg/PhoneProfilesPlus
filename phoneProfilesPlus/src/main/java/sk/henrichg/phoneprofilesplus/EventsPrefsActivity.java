package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
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
    final List<String> displayedSensors = new ArrayList<>();

    private Toolbar toolbar;

    LinearLayout settingsLinearLayout;
    //LinearLayout progressLinearLayout;

    private StartPreferencesActivityAsyncTask startPreferencesActivityAsyncTask = null;
    private FinishPreferencesActivityAsyncTask finishPreferencesActivityAsyncTask = null;

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
    private RefreshGUIBroadcastReceiver refreshGUIBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EditorActivity.itemDragPerformed = false;

        GlobalGUIRoutines.setTheme(this, false, false, false, false, false, true);
        //GlobalGUIRoutines.setLanguage(this);

        //if (Build.VERSION.SDK_INT >= 34)
        //    EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(this.getWindow(), false);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_events_preferences);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        toolbar = findViewById(R.id.activity_preferences_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }
        toolbar.setSubtitle(getString(R.string.title_activity_event_preferences));
        toolbar.setTitle(getString(R.string.event_string_0));

        settingsLinearLayout = findViewById(R.id.activity_preferences_settings);
        //progressLinearLayout = findViewById(R.id.activity_preferences_settings_linla_progress);

        event_id = getIntent().getLongExtra(PPApplication.EXTRA_EVENT_ID, 0L);
        old_event_status = getIntent().getIntExtra(PPApplication.EXTRA_EVENT_STATUS, -1);
        newEventMode = getIntent().getIntExtra(PPApplication.EXTRA_NEW_EVENT_MODE, PPApplication.EDIT_MODE_UNDEFINED);
        predefinedEventIndex = getIntent().getIntExtra(PPApplication.EXTRA_PREDEFINED_EVENT_INDEX, 0);

        if (getIntent().getBooleanExtra(DataWrapperStatic.EXTRA_FROM_RED_TEXT_PREFERENCES_NOTIFICATION, false)) {
            // check if profile exists in db
            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
            boolean eventExists = dataWrapper.eventExists(event_id);
            dataWrapper.invalidateDataWrapper();
            if (!eventExists) {
                PPApplication.showToast(getApplicationContext(),
                        getString(R.string.event_preferences_event_not_found),
                        Toast.LENGTH_SHORT);
//                PPApplicationStatic.logE("[CONTACTS_OBSERVER] EventsPrefsActivity.onCreate", "(1) PPApplication.blockContactContentObserver=false");
//                PPApplication.blockContactContentObserver = false;
//                ContactsContentObserver.enqueueContactsContentObserverWorker();
                super.finish();
                return;
            }
        }

        if (savedInstanceState == null) {
//            PPApplicationStatic.logE("[CONTACTS_OBSERVER] EventsPrefsActivity.onCreate", "(2) PPApplication.blockContactContentObserver=true");
//            PPApplication.blockContactContentObserver = true;

            startPreferencesActivityAsyncTask =
                    new StartPreferencesActivityAsyncTask(this, newEventMode, predefinedEventIndex);
            startPreferencesActivityAsyncTask.execute();

            //event = loadPreferences(newEventMode, predefinedEventIndex);
            //getSupportFragmentManager()
            //        .beginTransaction()
            //        .replace(R.id.activity_preferences_settings, preferenceFragment)
            //        .commit();
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

        refreshGUIBroadcastReceiver = new RefreshGUIBroadcastReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshGUIBroadcastReceiver,
                new IntentFilter(PPApplication.ACTION_REFRESH_EVENTS_PREFS_GUI_BROADCAST_RECEIVER));
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        PPApplicationStatic.logE("[CONTACTS_CACHE] EventsPrefsActivity.onPause", "PPApplication.blockContactContentObserver=false");
//        PPApplication.blockContactContentObserver = false;
//    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshGUIBroadcastReceiver);
        } catch (Exception ignored) {}
        refreshGUIBroadcastReceiver = null;
    }

    @Override
    public void onResume() {
        super.onResume();
//        PPApplicationStatic.logE("[CONTACTS_OBSERVER] EventsPrefsActivity.onResume", "PPApplication.blockContactContentObserver=true");
//        PPApplication.blockContactContentObserver = true;

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        //if (fragments == null)
        //    return;
        for (Fragment fragment : fragments) {
            if (fragment instanceof ContactsMultiSelectDialogPreferenceFragment) {
                ContactsMultiSelectDialogPreferenceFragment dialogFragment =
                        (ContactsMultiSelectDialogPreferenceFragment) fragment;
                dialogFragment.dismiss();
            }
            if (fragment instanceof ContactGroupsMultiSelectDialogPreferenceFragment) {
                ContactGroupsMultiSelectDialogPreferenceFragment dialogFragment =
                        (ContactGroupsMultiSelectDialogPreferenceFragment) fragment;
                dialogFragment.dismiss();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EditorActivity.itemDragPerformed = false;

        event = null;

        if ((startPreferencesActivityAsyncTask != null) &&
                startPreferencesActivityAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            startPreferencesActivityAsyncTask.cancel(true);
        startPreferencesActivityAsyncTask = null;
        if ((finishPreferencesActivityAsyncTask != null) &&
                finishPreferencesActivityAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            finishPreferencesActivityAsyncTask.cancel(true);
        finishPreferencesActivityAsyncTask = null;

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshGUIBroadcastReceiver);
        } catch (Exception ignored) {}
        refreshGUIBroadcastReceiver = null;
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
                            finishPreferencesActivityAsyncTask =
                                    new EventsPrefsActivity.FinishPreferencesActivityAsyncTask(this, newEventMode, predefinedEventIndex);
                            finishPreferencesActivityAsyncTask.execute();

                            //savePreferences(newEventMode, predefinedEventIndex);
                            //resultCode = RESULT_OK;
                            //finish();
                        }
                    },
                    (dialog2, which) -> finish(),
                    null,
                    null,
                    null,
                    true, true,
                    false, false,
                    true,
                    false,
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
                finishPreferencesActivityAsyncTask =
                        new EventsPrefsActivity.FinishPreferencesActivityAsyncTask(this, newEventMode, predefinedEventIndex);
                finishPreferencesActivityAsyncTask.execute();

                //savePreferences(newEventMode, predefinedEventIndex);
                //resultCode = RESULT_OK;
                //finish();
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

    /** @noinspection deprecation*/
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
//        PPApplicationStatic.logE("[CONTACTS_OBSERVER] EventsPrefsActivity.finish", "PPApplication.blockContactContentObserver=false");
//        PPApplication.blockContactContentObserver = false;
//        ContactsContentObserver.enqueueContactsContentObserverWorker();

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

    private Event loadPreferences(int new_event_mode, int predefinedEventIndex) {
        Event event = createEvent(getApplicationContext(), event_id, new_event_mode, predefinedEventIndex, false);
        if (event == null)
            event = createEvent(getApplicationContext(), event_id, PPApplication.EDIT_MODE_INSERT, predefinedEventIndex, false);

        if (event != null)
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            event.loadSharedPreferences(preferences);
        }

        return event;
    }

    private boolean checkPreferences(final int new_event_mode, final int predefinedEventIndex)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean enabled = preferences.getBoolean(Event.PREF_EVENT_ENABLED, false);
        if (!enabled) {
            if (!ApplicationPreferences.applicationEventNeverAskForEnableRun) {
                //if (new_event_mode == EditorEventListFragment.EDIT_MODE_INSERT) {

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

                            finishPreferencesActivityAsyncTask =
                                    new EventsPrefsActivity.FinishPreferencesActivityAsyncTask(this, new_event_mode, predefinedEventIndex);
                            finishPreferencesActivityAsyncTask.execute();

                            //savePreferences(new_event_mode, predefinedEventIndex);
                            //resultCode = RESULT_OK;
                            //finish();
                        },
                        (dialog2, which) -> {
                            finishPreferencesActivityAsyncTask =
                                    new EventsPrefsActivity.FinishPreferencesActivityAsyncTask(this, new_event_mode, predefinedEventIndex);
                            finishPreferencesActivityAsyncTask.execute();

                            //savePreferences(new_event_mode, predefinedEventIndex);
                            //resultCode = RESULT_OK;
                            //finish();
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

    void savePreferences(int new_event_mode, int predefinedEventIndex)
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
            dataWrapper.restartEventsWithRescan(true, false, true, true, true, false);
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

    static void saveUpdateOfPreferences(Event _event, DataWrapper _dataWrapper, final int old_event_status) {
        // save preferences into profile
        _dataWrapper.getEventTimelineList(true);

        //noinspection IfStatementWithIdenticalBranches
        if (_event.getStatus() == Event.ESTATUS_STOP)
        {
            final WeakReference<DataWrapper> dataWrapperWeakRef = new WeakReference<>(_dataWrapper);
            final WeakReference<Event> eventWeakRef = new WeakReference<>(_event);
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=EventsPrefsActivity.saveUpdateOfPreferences.1");

                DataWrapper dataWrapper = dataWrapperWeakRef.get();
                Event event = eventWeakRef.get();

                if ((dataWrapper != null) && (event != null)) {
                    PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_EventsPrefsActivity_saveUpdateOfPreferences_1);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (old_event_status != Event.ESTATUS_STOP) {
//                            PPApplicationStatic.logE("[SYNCHRONIZED] EventsPrefsActivity.saveUpdateOfPreferences", "PPApplication.eventsHandlerMutex");
                            synchronized (PPApplication.eventsHandlerMutex) {

                                // pause event - must be called, because status is ESTATUS_STOP
                                event.pauseEvent(dataWrapper, true, false,
                                        false, false, null, false, false, false, true);
                                // stop event
                                event.stopEvent(dataWrapper, true, false,
                                        true, true, true);

                                PPApplicationStatic.setBlockProfileEventActions(true);
                            }
                            // restart Events
                            //dataWrapper.restartEvents(false, true, true, true, false);
                            dataWrapper.restartEventsWithRescan(true, false, false, true, true, false);
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
                }
            };
            PPApplicationStatic.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);
        }
        else {
            final WeakReference<DataWrapper> dataWrapperWeakRef = new WeakReference<>(_dataWrapper);
            final WeakReference<Event> eventWeakRef = new WeakReference<>(_event);
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=EventsPrefsActivity.saveUpdateOfPreferences.2");

                DataWrapper dataWrapper = dataWrapperWeakRef.get();
                Event event = eventWeakRef.get();

                if ((dataWrapper != null) && (event != null)) {
                    PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_EventsPrefsActivity_saveUpdateOfPreferences_2);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        // pause event
                        event.pauseEvent(dataWrapper, true, false,
                                false, false, null, false, false, false, true);
                        // must be called, because status is ESTATUS_PAUSE and in pauseEvent is not called
                        // ESTATUS_PAUSE is set in Event.saveSharedPreferences()
                        event.doLogForPauseEvent(dataWrapper.context, false);

                        // restart Events
                        PPApplicationStatic.setBlockProfileEventActions(true);
                        //dataWrapper.restartEvents(false, true, true, true, false);
                        dataWrapper.restartEventsWithRescan(true, false, false, true, true, false);

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
                }
            };
            PPApplicationStatic.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);
        }
    }

    private void showTargetHelps() {
        if (!showSaveMenu)
            return;

        if (ApplicationPreferences.prefEventPrefsActivityStartTargetHelps) {
            //Log.d("EventPrefsActivity.showTargetHelps", "PREF_START_TARGET_HELPS=true");

            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
            editor.putBoolean(PPApplication.PREF_EVENTS_PREFS_ACTIVITY_START_TARGET_HELPS, false);
            editor.apply();
            ApplicationPreferences.prefEventPrefsActivityStartTargetHelps = false;

            Toolbar toolbar = findViewById(R.id.activity_preferences_toolbar);

            int outerCircleColor = R.color.tabTargetHelpOuterCircleColor;
            int targetCircleColor = R.color.tabTargetHelpTargetCircleColor;
            int titleTextColor = R.color.tabTargetHelpTitleTextColor;
            int descriptionTextColor = R.color.tabTargetHelpDescriptionTextColor;

            final TapTargetSequence sequence = new TapTargetSequence(this);
            List<TapTarget> targets = new ArrayList<>();
            int id = 1;
            try {
                //noinspection DataFlowIssue
                targets.add(
                        TapTarget.forToolbarMenuItem(toolbar, R.id.event_preferences_save, getString(R.string.event_preference_activity_targetHelps_save_title), getString(R.string.event_preference_activity_targetHelps_save_description))
                                .outerCircleColor(outerCircleColor)
                                .targetCircleColor(targetCircleColor)
                                .titleTextColor(titleTextColor)
                                .descriptionTextColor(descriptionTextColor)
                                .descriptionTextAlpha(PPApplication.descriptionTapTargetAlpha)
                                .dimColor(R.color.tabTargetHelpDimColor)
                                .titleTextSize(PPApplication.titleTapTargetSize)
                                .textTypeface(Typeface.DEFAULT_BOLD)
                                .tintTarget(true)
                                .drawShadow(true)
                                .id(id)
                );
                ++id;
            } catch (Exception e) {
                //PPApplicationStatic.recordException(e);
            }

            for (TapTarget target : targets) {
                target.setDrawBehindStatusBar(true);
                target.setDrawBehindNavigationBar(true);
            }

            sequence.targets(targets);
            sequence.listener(new TapTargetSequence.Listener() {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                @Override
                public void onSequenceFinish() {
                }

                @Override
                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                    //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {
                }
            });
            sequence.continueOnCancel(true)
                    .considerOuterCircleCanceled(true);

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

    private static class StartPreferencesActivityAsyncTask extends AsyncTask<Void, Integer, Void> {

        final int new_event_mode;
        final int predefinedEventIndex;

        //private final WeakReference<EventsPrefsActivity> activityWeakReference;
        @SuppressLint("StaticFieldLeak")
        private EventsPrefsActivity activity;
        private EventsPrefsFragment fragment;

        public StartPreferencesActivityAsyncTask(final EventsPrefsActivity activity,
                                                 int new_event_mode, int predefinedEventIndex) {
            //this.activityWeakReference = new WeakReference<>(activity);
            this.activity = activity;
            this.new_event_mode = new_event_mode;
            this.predefinedEventIndex = predefinedEventIndex;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fragment = new EventsPrefsRoot();

            //EventsPrefsActivity activity = activityWeakReference.get();

            //if (activity != null) {
            //    activity.settingsLinearLayout.setVisibility(View.GONE);
            //    activity.progressLinearLayout.setVisibility(View.VISIBLE);
            //}
        }

        @Override
        protected Void doInBackground(Void... params) {
            //EventsPrefsActivity activity = activityWeakReference.get();

            if (activity != null) {
//                Log.e("EventsPrefsActivity.StartPreferencesActivityAsyncTask", ".doInBackground");
                activity.event = activity.loadPreferences(new_event_mode, predefinedEventIndex);
                //GlobalUtils.sleep(100);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //EventsPrefsActivity activity = activityWeakReference.get();

            if ((activity != null) && (!activity.isFinishing())) {
//                Log.e("EventsPrefsActivity.StartPreferencesActivityAsyncTask", ".onPostExecute");

                activity.toolbar.setTitle(activity.getString(R.string.event_string_0) + StringConstants.STR_COLON_WITH_SPACE + activity.event._name);

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_preferences_settings, fragment)
                        .commit();

                //activity.progressLinearLayout.setVisibility(View.GONE);
                //activity.settingsLinearLayout.setVisibility(View.VISIBLE);
            }
            activity = null;
        }

    }

    private static class FinishPreferencesActivityAsyncTask extends AsyncTask<Void, Integer, Void> {

        final int new_event_mode;
        final int predefinedEventIndex;

        private final WeakReference<EventsPrefsActivity> activityWeakReference;

        public FinishPreferencesActivityAsyncTask(final EventsPrefsActivity activity,
                                                  int new_event_mode, int predefinedEventIndex) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.new_event_mode = new_event_mode;
            this.predefinedEventIndex = predefinedEventIndex;
        }

        @Override
        protected Void doInBackground(Void... params) {
            EventsPrefsActivity activity = activityWeakReference.get();

            if (activity != null) {
//                Log.e("EventsPrefsActivity.FinishPreferencesActivityAsyncTask", ".doInBackground");
                activity.savePreferences(new_event_mode, predefinedEventIndex);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            EventsPrefsActivity activity = activityWeakReference.get();

            if (activity != null) {
//                Log.e("EventsPrefsActivity.FinishPreferencesActivityAsyncTask", ".onPostExecute");

                activity.resultCode = RESULT_OK;
                activity.finish();
            }
        }

    }

}
