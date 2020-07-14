package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class WorkerWithData extends Worker {

    //Context context;

    static final String HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG = "handleEventsBluetoothLEScannerWorkWithData";
    static final String HANDLE_EVENTS_BLUETOOTH_CE_SCANNER_WORK_TAG = "handleEventsBluetoothCLScannerWorkWithData";
    static final String HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG = "handleEventsWifiScannerFromReceiverWorkWithData";
    static final String HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG = "handleEventsWifiScannerFromScannerWorkWithData";
    static final String HANDLE_EVENTS_TWILIGHT_SCANNER_WORK_TAG = "handleEventsTwilightScannerWorkWithData";
    static final String HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG = "handleEventsMobileCellsScannerWorkWithData";
    static final String START_EVENT_NOTIFICATION_TAG_WORK = "startEventNotificationWorkWithData";
    static final String RUN_APPLICATION_WITH_DELAY_TAG_WORK = "runApplicationWithDelayWorkWithData";
    static final String PROFILE_DURATION_TAG_WORK = "profileDurationWorkWithData";

    /*
    static final String DELAYED_WORK_HANDLE_EVENTS = "handle_events"; // must be enqueoue() because od work data
    static final String DELAYED_WORK_START_WIFI_SCAN = "start_wifi_scan";
    static final String DELAYED_WORK_BLOCK_PROFILE_EVENT_ACTIONS = "block_profile_event_actions";
    static final String DELAYED_WORK_AFTER_FIRST_START = "after_first_start";
    static final String DELAYED_WORK_PACKAGE_REPLACED = "package_replaced";
    static final String DELAYED_WORK_CLOSE_ALL_APPLICATIONS = "close_all_applications";
    */
    //static final String DELAYED_WORK_CHANGE_FILTER_AFTER_EDITOR_DATA_CHANGE = "change_filter_after_editor_data_change";

    public WorkerWithData(
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

            if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return Result.success();

            //Data outputData;

            // Get the input
            //String action = getInputData().getString(PhoneProfilesService.EXTRA_DELAYED_WORK);
            //if (action == null)
            //    return Result.success();

            String sensorType = getInputData().getString(PhoneProfilesService.EXTRA_SENSOR_TYPE);
            long eventId = getInputData().getLong(PPApplication.EXTRA_EVENT_ID, 0);
            String profileName = getInputData().getString(RunApplicationWithDelayBroadcastReceiver.EXTRA_PROFILE_NAME);
            String runApplicationData = getInputData().getString(RunApplicationWithDelayBroadcastReceiver.EXTRA_RUN_APPLICATION_DATA);
            long profileId = getInputData().getLong(PPApplication.EXTRA_PROFILE_ID, 0);
            boolean forRestartEvents = getInputData().getBoolean(ProfileDurationAlarmBroadcastReceiver.EXTRA_FOR_RESTART_EVENTS, false);
            int startupSource = getInputData().getInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SERVICE_MANUAL);

            //outputData = generateResult(LocationGeofenceEditorActivity.FAILURE_RESULT,
            //                                    getApplicationContext().getString(R.string.event_preferences_location_no_address_found),
            //                                    updateName);

            //return Result.success(outputData);

            Context appContext = getApplicationContext();

            Set<String> tags = getTags();
            for (String tag : tags) {
                PPApplication.logE("WorkerWithData.doWork", "tag=" + tag);

                switch (tag) {
                    case PPApplication.AFTER_FIRST_START_WORK_TAG:
                        PPApplication.logE("PhoneProfilesService.doForFirstStart.doWork", "START");

                        BootUpReceiver.bootUpCompleted = true;

                        //boolean fromDoFirstStart = getInputData().getBoolean(PhoneProfilesService.EXTRA_FROM_DO_FIRST_START, true);

                        // activate profile immediately after start of PPP
                        // this is required for some users, for example: francescocaldelli@gmail.com
                        //PPApplication.applicationPackageReplaced = false;
                        //if (fromDoFirstStart) {
                            //PhoneProfilesService instance = PhoneProfilesService.getInstance();
                            //if (instance != null)
                            //    instance.PhoneProfilesService.setApplicationFullyStarted(appContext/*true*/);
                            PPApplication.setApplicationFullyStarted(appContext);
                        //}

                        //if (fromDoFirstStart) {
                            PPApplication.createNotificationChannels(appContext);

                            boolean activateProfiles = getInputData().getBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);

                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

                            if (Event.getGlobalEventsRunning()) {
                                PPApplication.logE("PhoneProfilesService.doForFirstStart.doWork", "global event run is enabled, first start events");

                                if (activateProfiles) {
                                    if (!DataWrapper.getIsManualProfileActivation(false/*, appContext*/)) {
                                        ////// unblock all events for first start
                                        //     that may be blocked in previous application run
                                        dataWrapper.pauseAllEvents(false, false);
                                    }
                                }

                                dataWrapper.firstStartEvents(true, false);

                                if (PPApplication.deviceBoot) {
                                    PPApplication.deviceBoot = false;
                                    PPApplication.logE("PhoneProfilesService.doForFirstStart.doWork", "device boot");
                                    boolean deviceBootEvents = dataWrapper.eventTypeExists(DatabaseHandler.ETYPE_DEVICE_BOOT);
                                    if (deviceBootEvents) {
                                        PPApplication.logE("PhoneProfilesService.doForFirstStart.doWork", "device boot event exists");

                                        // start events handler
                                        //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=DelayedWorksWorker.doWork (DELAYED_WORK_AFTER_FIRST_START)");

                                        EventsHandler eventsHandler = new EventsHandler(appContext);

                                        Calendar now = Calendar.getInstance();
                                        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                                        final long _time = now.getTimeInMillis() + gmtOffset;
                                        eventsHandler.setEventDeviceBootParameters(_time);

                                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_BOOT);

                                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=DelayedWorksWorker.doWork (DELAYED_WORK_AFTER_FIRST_START)");
                                    }
                                }

                                //PPApplication.updateNotificationAndWidgets(true, true, appContext);
                                //PPApplication.updateGUI(appContext, true, true);
                            } else {
                                PPApplication.logE("PhoneProfilesService.doForFirstStart.doWork", "global event run is not enabled, manually activate profile");

                                if (activateProfiles) {
                                    ////// unblock all events for first start
                                    //     that may be blocked in previous application run
                                    dataWrapper.pauseAllEvents(true, false);
                                }

                                dataWrapper.activateProfileOnBoot();
                                //PPApplication.updateNotificationAndWidgets(true, true, appContext);
                                //PPApplication.updateGUI(appContext, true, true);
                            }

                            //PPApplication.logE("-------- PPApplication.forceUpdateGUI", "from=DelayedWorksWorker.doWork");
                            PPApplication.forceUpdateGUI(appContext, true, true/*, true*/);
                        //}

                        PPApplication.logE("PhoneProfilesService.doForFirstStart.doWork", "END");
                        break;
                    case HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG:
                    case HANDLE_EVENTS_BLUETOOTH_CE_SCANNER_WORK_TAG:
                    case HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG:
                    case HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG:
                    case HANDLE_EVENTS_TWILIGHT_SCANNER_WORK_TAG:
                    case HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG:
                        if (Event.getGlobalEventsRunning() && (sensorType != null)) {
                            //PPApplication.logE("DelayedWorksWorker.doWork", "DELAYED_WORK_HANDLE_EVENTS");
                            //PPApplication.logE("DelayedWorksWorker.doWork", "sensorType="+sensorType);
                            // start events handler
                            //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=DelayedWorksWorker.doWork (DELAYED_WORK_HANDLE_EVENTS): sensorType="+sensorType);

                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(sensorType);

                            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=DelayedWorksWorker.doWork (DELAYED_WORK_HANDLE_EVENTS)");
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
                        if (tag.startsWith(START_EVENT_NOTIFICATION_TAG_WORK))
                            StartEventNotificationBroadcastReceiver.doWork(false, appContext, eventId);
                        else
                        if (tag.startsWith(RUN_APPLICATION_WITH_DELAY_TAG_WORK))
                            RunApplicationWithDelayBroadcastReceiver.doWork(appContext, profileName, runApplicationData);
                        else
                        if (tag.startsWith(PROFILE_DURATION_TAG_WORK))
                            ProfileDurationAlarmBroadcastReceiver.doWork(false, appContext, profileId, forRestartEvents, startupSource);
                        else

                        break;
                }
            }

            return Result.success();
        } catch (Exception e) {
            //Log.e("DelayedWorksWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
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

}
