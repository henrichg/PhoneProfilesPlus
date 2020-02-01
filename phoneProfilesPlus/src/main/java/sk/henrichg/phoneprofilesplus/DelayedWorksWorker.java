package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

@SuppressWarnings("WeakerAccess")
public class DelayedWorksWorker extends Worker {

    //Context context;

    static final String DELAYED_WORK_AFTER_FIRST_START = "after_first_start";
    static final String DELAYED_WORK_HANDLE_EVENTS = "handle_events";
    static final String DELAYED_WORK_START_WIFI_SCAN = "start_wifi_scan";
    static final String DELAYED_WORK_BLOCK_PROFILE_EVENT_ACTIONS = "block_profile_event_actions";
    static final String DELAYED_WORK_PACKAGE_REPLACED = "package_replaced";
    static final String DELAYED_WORK_CLOSE_ALL_APPLICATIONS = "close_all_applications";
    //static final String DELAYED_WORK_CHANGE_FILTER_AFTER_EDITOR_DATA_CHANGE = "change_filter_after_editor_data_change";

    public DelayedWorksWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        //this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            //PPApplication.logE("DelayedWorksWorker.doWork", "xxx");

            //Data outputData;

            // Get the input
            String action = getInputData().getString(PhoneProfilesService.EXTRA_DELAYED_WORK);
            if (action == null)
                return Result.success();

            boolean activateProfiles = getInputData().getBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
            boolean restartService = getInputData().getBoolean(PackageReplacedReceiver.EXTRA_RESTART_SERVICE, false);
            String sensorType = getInputData().getString(PhoneProfilesService.EXTRA_SENSOR_TYPE);
            //int filterSelectedItem = getInputData().getInt(EditorProfilesActivity.EXTRA_SELECTED_FILTER, 0);
            //long profileId = getInputData().getLong(PPApplication.EXTRA_PROFILE_ID, 0);
            //long eventId = getInputData().getLong(PPApplication.EXTRA_EVENT_ID, 0);

            //outputData = generateResult(LocationGeofenceEditorActivity.FAILURE_RESULT,
            //                                    getApplicationContext().getString(R.string.event_preferences_location_no_address_found),
            //                                    updateName);

            //return Result.success(outputData);

            Context appContext = getApplicationContext();

            switch (action) {
                case DELAYED_WORK_AFTER_FIRST_START:
                    if (PhoneProfilesService.getInstance() != null) {
                        PhoneProfilesService instance = PhoneProfilesService.getInstance();

                        PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "START");
                        PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "instance.getWaitForEndOfStart()="+instance.getWaitForEndOfStart());

                        //if (instance.getWaitForEndOfStart()) {

                            //PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "wait for end of start is TRUE");

                            /*if (Event.getGlobalEventsRunning(appContext)) {
                                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

                                if (!DataWrapper.getIsManualProfileActivation(false, appContext)) {
                                    PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "RESTART EVENTS AFTER WAIT FOR END OF START");
                                    dataWrapper.restartEventsWithRescan(activateProfiles, false, true, false);
                                    //dataWrapper.invalidateDataWrapper();
                                }
                            }*/
                            // start events
                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

                            if (activateProfiles) {
                                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(appContext);
                                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, false);
                                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, false);
                                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, false);
                                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, false);
                                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, false);
                                editor.apply();
                                ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(appContext);
                                ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(appContext);
                                ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(appContext);
                                ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(appContext);
                                ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(appContext);
                            }

                            if (Event.getGlobalEventsRunning()) {
                                PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "global event run is enabled, first start events");

                                if (activateProfiles) {
                                    if (!DataWrapper.getIsManualProfileActivation(false/*, appContext*/)) {
                                        ////// unblock all events for first start
                                        //     that may be blocked in previous application run
                                        dataWrapper.pauseAllEvents(false, false);
                                    }
                                }

                                dataWrapper.firstStartEvents(true, false);
                                dataWrapper.updateNotificationAndWidgets(true, true);
                            } else {
                                PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "global event run is not enabled, manually activate profile");

                                if (activateProfiles) {
                                    ////// unblock all events for first start
                                    //     that may be blocked in previous application run
                                    dataWrapper.pauseAllEvents(true, false);
                                }

                                dataWrapper.activateProfileOnBoot();
                                dataWrapper.updateNotificationAndWidgets(true, true);
                            }

                            // set waitForEndOfStart to false only when is not enqueued packageReplacedWork
                            try {
                                WorkManager workInstance = WorkManager.getInstance(appContext);
                                ListenableFuture<List<WorkInfo>> statuses = workInstance.getWorkInfosByTag("packageReplacedWork");
                                //noinspection TryWithIdenticalCatches
                                try {
                                    List<WorkInfo> workInfoList = statuses.get();
                                    boolean running = false;
                                    for (WorkInfo workInfo : workInfoList) {
                                        WorkInfo.State state = workInfo.getState();
                                        running = (state == WorkInfo.State.ENQUEUED) || (state == WorkInfo.State.RUNNING);
                                    }

                                    PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "packageReplacedWork - running="+running);

                                    if (!running) {
                                        instance.setWaitForEndOfStart(false);
                                        PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "instance.getWaitForEndOfStart()="+instance.getWaitForEndOfStart());
                                    }
                                } catch (ExecutionException e) {
                                    Log.e("PhoneProfilesService.doForFirstStart.2 - worker", Log.getStackTraceString(e));
                                    instance.setWaitForEndOfStart(false);
                                } catch (InterruptedException e) {
                                    Log.e("PhoneProfilesService.doForFirstStart.2 - worker", Log.getStackTraceString(e));
                                    instance.setWaitForEndOfStart(false);
                                }
                            } catch (Exception e) {
                                Log.e("PhoneProfilesService.doForFirstStart.2 - worker", Log.getStackTraceString(e));
                                instance.setWaitForEndOfStart(false);
                            }
                        //}

                        PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "END");
                    }
                    break;
                case DELAYED_WORK_PACKAGE_REPLACED:
                    PPApplication.logE("PackageReplacedReceiver.doWork", "START  restartService=" + restartService);

                    DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

                    /*
                    ApplicationPreferences.getSharedPreferences(appContext);
                    SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "white");
                    editor.apply();
                    */

                    if (PhoneStateScanner.enabledAutoRegistration) {
                        PhoneStateScanner.stopAutoRegistration(appContext);
                        PPApplication.sleep(2000);
                    }

                    if (!PPApplication.getApplicationStarted(true)) {
                        // service is not started, start it
                        PPApplication.logE("PackageReplacedReceiver.doWork", "PP service is not started, start it");
                        if (startService(dataWrapper))
                            restartService = true;
                    } else {
                        PPApplication.logE("PackageReplacedReceiver.doWork", "PP service is started");
                        if (restartService)
                            startService(dataWrapper);
                    }

                    if (!restartService) {
                        // restart service is not set, only restart events.

                        if (PhoneProfilesService.getInstance() != null) {
                            PhoneProfilesService instance = PhoneProfilesService.getInstance();

                            PPApplication.logE("PackageReplacedReceiver.doWork", "instance.getWaitForEndOfStart()="+instance.getWaitForEndOfStart());

                            //PPApplication.sleep(3000);
                            if (PPApplication.getApplicationStarted(true)) {
                                // service is started by PPApplication

                                //if (PhoneProfilesService.getInstance() != null)
                                //    PhoneProfilesService.getInstance().removeRestartEventsForFirstStartHandler(true);

                                dataWrapper.addActivityLog(DataWrapper.ALTYPE_APPLICATION_START, null, null, null, 0);

                                // start events
                                if (Event.getGlobalEventsRunning()) {
                                    PPApplication.logE("PackageReplacedReceiver.doWork", "global event run is enabled, first start events");

                                    if (!DataWrapper.getIsManualProfileActivation(false/*, appContext*/)) {
                                        ////// unblock all events for first start
                                        //     that may be blocked in previous application run
                                        dataWrapper.pauseAllEvents(false, false);
                                    }

                                    dataWrapper.firstStartEvents(true, false);
                                    //PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from PackageReplacedReceiver.onReceive - worker");
                                    dataWrapper.updateNotificationAndWidgets(true, true);
                                } else {
                                    PPApplication.logE("PackageReplacedReceiver.doWork", "global event run is not enabled, manually activate profile");

                                    ////// unblock all events for first start
                                    //     that may be blocked in previous application run
                                    dataWrapper.pauseAllEvents(true, false);

                                    dataWrapper.activateProfileOnBoot();
                                    //PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from PackageReplacedReceiver.onReceive - worker");
                                    dataWrapper.updateNotificationAndWidgets(true, true);
                                }
                            }

                            instance.setWaitForEndOfStart(false);
                            PPApplication.logE("PackageReplacedReceiver.doWork", "instance.getWaitForEndOfStart()="+instance.getWaitForEndOfStart());
                        }
                    }

                    PPApplication.logE("PackageReplacedReceiver.doWork", "END");
                    break;
                case DELAYED_WORK_HANDLE_EVENTS:
                    if (sensorType != null) {
                        // start events handler
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(sensorType);
                    }
                    break;
                case DELAYED_WORK_START_WIFI_SCAN:
                    WifiScanWorker.startScan(appContext);
                    break;
                case DELAYED_WORK_BLOCK_PROFILE_EVENT_ACTIONS:
                    PPApplication.blockProfileEventActions = false;
                    break;
                case DELAYED_WORK_CLOSE_ALL_APPLICATIONS:
                    if (!PPApplication.blockProfileEventActions) {
                        try {
                            /*boolean appFound = false;
                            // intentionally using string value as Context.USAGE_STATS_SERVICE was
                            // strangely only added in API 22 (LOLLIPOP_MR1)
                            @SuppressLint("WrongConstant")
                            UsageStatsManager usm = (UsageStatsManager)appContext.getSystemService("usagestats");
                            long time = System.currentTimeMillis();
                            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,time - 1000 * 1000, time);
                            if (appList != null && appList.size() > 0) {
                                appFound = true;
//                                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
//                                for (UsageStats usageStats : appList) {
//                                    mySortedMap.put(usageStats.getLastTimeUsed(),
//                                            usageStats);
//                                }
//                                if (mySortedMap != null && !mySortedMap.isEmpty()) {
//                                    currentApp = mySortedMap.get(
//                                            mySortedMap.lastKey()).getPackageName();
//                                }
                            }*/

                            /*boolean appFound = false;
                            ActivityManager manager = (ActivityManager)appContext.getSystemService(Context.ACTIVITY_SERVICE);
                            List<ActivityManager.RunningAppProcessInfo> tasks = manager.getRunningAppProcesses();
                            Log.e("DelayedWorksWorker.doWork", "tasks="+tasks);
                            if ((tasks != null) && (!tasks.isEmpty())) {
                                Log.e("DelayedWorksWorker.doWork", "tasks.size()="+tasks.size());
                                for (ActivityManager.RunningAppProcessInfo task : tasks) {
                                    Log.e("DelayedWorksWorker.doWork", "task.processName="+task.processName);
                                    if (task.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                                        Log.e("DelayedWorksWorker.doWork", "IMPORTANCE_FOREGROUND");
                                        appFound = true;
                                        break;
                                    }
                                }
                            }*/
                            //if (appFound) {
                                Intent startMain = new Intent(Intent.ACTION_MAIN);
                                startMain.addCategory(Intent.CATEGORY_HOME);
                                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                appContext.startActivity(startMain);
                            //}
                        } catch (Exception e) {
                            Log.e("DelayedWorksWorker.doWork", Log.getStackTraceString(e));
                        }
                    }
                    break;
                /*case DELAYED_WORK_CHANGE_FILTER_AFTER_EDITOR_DATA_CHANGE:
                    if (filterSelectedItem != 0) {
                        Activity activity = PPApplication.getEditorActivity();
                        if (activity instanceof EditorProfilesActivity) {
                            final EditorProfilesActivity editorActivity = (EditorProfilesActivity)activity;
                            Fragment fragment = editorActivity.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                            if (fragment instanceof EditorProfileListFragment) {
                                EditorProfileListFragment profileFragment = (EditorProfileListFragment) fragment;
                                boolean changeFilter = false;
                                Profile scrollToProfile = DatabaseHandler.getInstance(context).getProfile(profileId, false);
                                if (scrollToProfile != null) {
                                    switch (filterSelectedItem) {
                                        case EditorProfilesActivity.DSI_PROFILES_NO_SHOW_IN_ACTIVATOR:
                                            changeFilter = scrollToProfile._showInActivator;
                                            break;
                                        case EditorProfilesActivity.DSI_PROFILES_SHOW_IN_ACTIVATOR:
                                            changeFilter = !scrollToProfile._showInActivator;
                                            break;
                                    }
                                }
                                if (changeFilter) {
                                    profileFragment.scrollToProfile = scrollToProfile;
                                    Handler handler = new Handler(context.getMainLooper());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((GlobalGUIRoutines.HighlightedSpinnerAdapter) editorActivity.filterSpinner.getAdapter()).setSelection(0);
                                            editorActivity.selectFilterItem(0, 0, false, true);
                                        }
                                    });
                                } else
                                    profileFragment.scrollToProfile = null;
                            }
                            if (fragment instanceof EditorEventListFragment) {
                                EditorEventListFragment eventFragment = (EditorEventListFragment) fragment;
                                boolean changeFilter = false;
                                Event scrollToEvent = DatabaseHandler.getInstance(context).getEvent(eventId);
                                if (scrollToEvent != null) {
                                    switch (filterSelectedItem) {
                                        case EditorProfilesActivity.DSI_EVENTS_NOT_STOPPED:
                                            changeFilter = scrollToEvent.getStatus() == Event.ESTATUS_STOP;
                                            break;
                                        case EditorProfilesActivity.DSI_EVENTS_RUNNING:
                                            changeFilter = scrollToEvent.getStatus() != Event.ESTATUS_RUNNING;
                                            break;
                                        case EditorProfilesActivity.DSI_EVENTS_PAUSED:
                                            changeFilter = scrollToEvent.getStatus() != Event.ESTATUS_PAUSE;
                                            break;
                                        case EditorProfilesActivity.DSI_EVENTS_STOPPED:
                                            changeFilter = scrollToEvent.getStatus() != Event.ESTATUS_STOP;
                                            break;
                                    }
                                }
                                if (changeFilter) {
                                    eventFragment.scrollToEvent = scrollToEvent;
                                    Handler handler = new Handler(context.getMainLooper());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((GlobalGUIRoutines.HighlightedSpinnerAdapter) editorActivity.filterSpinner.getAdapter()).setSelection(0);
                                            editorActivity.selectFilterItem(1, 0, false, true);
                                        }
                                    });
                                } else
                                    eventFragment.scrollToEvent = null;
                            }
                        }
                    }
                    break;*/
                default:
                    break;
            }

            return Result.success();
        } catch (Exception e) {
            Log.e("DelayedWorksWorker.doWork", Log.getStackTraceString(e));
            Crashlytics.logException(e);
            /*Handler _handler = new Handler(getApplicationContext().getMainLooper());
            Runnable r = new Runnable() {
                public void run() {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            };
            _handler.postDelayed(r, 1000);*/
            return Result.failure();
        }
    }

    /*
    private Data generateResult(int resultCode, String message, boolean updateName) {
        // Create the output of the work
        PPApplication.logE("FetchAddressWorker.generateResult", "resultCode="+resultCode);
        PPApplication.logE("FetchAddressWorker.generateResult", "message="+message);
        PPApplication.logE("FetchAddressWorker.generateResult", "updateName="+updateName);

        return new Data.Builder()
                .putInt(LocationGeofenceEditorActivity.RESULT_CODE, resultCode)
                .putString(LocationGeofenceEditorActivity.RESULT_DATA_KEY, message)
                .putBoolean(LocationGeofenceEditorActivity.UPDATE_NAME_EXTRA, updateName)
                .build();
    }
    */

    private boolean startService(DataWrapper dataWrapper) {
        boolean isApplicationStarted = PPApplication.getApplicationStarted(false);

        //PPApplication.logE("PPApplication.exitApp", "from DelayedWorksWorker.doWork shutdown=false");
        PPApplication.exitApp(false, dataWrapper.context, dataWrapper, null, false/*, false, true*/);

        if (isApplicationStarted)
        {
            PPApplication.sleep(2000);

            // start PhoneProfilesService
            //PPApplication.logE("DelayedWorksWorker.doWork", "xxx");
            PPApplication.setApplicationStarted(dataWrapper.context, true);
            Intent serviceIntent = new Intent(dataWrapper.context, PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            PPApplication.startPPService(dataWrapper.context, serviceIntent);

            //PPApplication.sleep(2000);

            return true;
        }
        else
            return false;
    }

}
