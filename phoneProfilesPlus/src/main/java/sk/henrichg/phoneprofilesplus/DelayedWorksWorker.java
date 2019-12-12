package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

@SuppressWarnings("WeakerAccess")
public class DelayedWorksWorker extends Worker {

    private final Context context;

    static final String DELAYED_WORK_AFTER_FIRST_START = "after_first_start";
    static final String DELAYED_WORK_HANDLE_EVENTS = "handle_events";
    static final String DELAYED_WORK_START_WIFI_SCAN = "start_wifi_scan";
    static final String DELAYED_WORK_BLOCK_PROFILE_EVENT_ACTIONS = "block_profile_event_actions";
    static final String DELAYED_WORK_PACKAGE_REPLACED = "package_replaced";

    public DelayedWorksWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        PPApplication.logE("RestartEventsWithDelayWorker.doWork", "xxx");

        //Data outputData;

        // Get the input
        String action = getInputData().getString(PhoneProfilesService.EXTRA_DELAYED_WORK);
        if (action == null)
            return Result.success();

        boolean activateProfiles = getInputData().getBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
        String sensorType = getInputData().getString(PhoneProfilesService.EXTRA_SENSOR_TYPE);

        //outputData = generateResult(LocationGeofenceEditorActivity.FAILURE_RESULT,
        //                                    getApplicationContext().getString(R.string.event_preferences_location_no_address_found),
        //                                    updateName);

        //return Result.success(outputData);

        Context appContext = getApplicationContext();

        switch (action) {
            case DELAYED_WORK_AFTER_FIRST_START:
                if (PhoneProfilesService.getInstance() != null) {
                    PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "PhoneProfilesService.doForFirstStart.2 START");

                    PhoneProfilesService instance = PhoneProfilesService.getInstance();
                    instance.setWaitForEndOfStart(false);

                    if (Event.getGlobalEventsRunning(appContext)) {
                        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

                        if (!DataWrapper.getIsManualProfileActivation(false, appContext)) {
                            PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "RESTART EVENTS AFTER WAIT FOR END OF START");
                            dataWrapper.restartEventsWithRescan(/*true, */activateProfiles, false, true, false);
                            dataWrapper.invalidateDataWrapper();
                        }
                    }

                    PPApplication.logE("PhoneProfilesService.doForFirstStart.2 - worker", "PhoneProfilesService.doForFirstStart.2 END");
                }
                break;
            case DELAYED_WORK_PACKAGE_REPLACED:
                PPApplication.logE("DelayedWorksWorker.doWork", "restartService="+PackageReplacedReceiver.restartService);

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

                if (!PPApplication.getApplicationStarted(appContext, true)) {
                    // service is not started, start it
                    PPApplication.logE("PackageReplacedReceiver.onReceive", "PP service is not started, start it");
                    startService(dataWrapper);
                    PackageReplacedReceiver.restartService = true;
                }
                else {
                    PPApplication.logE("PackageReplacedReceiver.onReceive", "PP service is started");
                    if (PackageReplacedReceiver.restartService)
                        startService(dataWrapper);
                }

                if (!PackageReplacedReceiver.restartService) {
                    // restart service is not set, only restart events.

                    //PPApplication.sleep(3000);
                    if (PPApplication.getApplicationStarted(appContext, true)) {
                        // service is started by PPApplication

                        if (PhoneProfilesService.getInstance() != null)
                            PhoneProfilesService.getInstance().removeRestartEventsForFirstStartHandler(true);

                        dataWrapper.addActivityLog(DataWrapper.ALTYPE_APPLICATION_START, null, null, null, 0);

                        // start events
                        if (Event.getGlobalEventsRunning(appContext)) {
                            PPApplication.logE("PackageReplacedReceiver.onReceive", "global event run is enabled, first start events");

                            if (!DataWrapper.getIsManualProfileActivation(false, appContext)) {
                                ////// unblock all events for first start
                                //     that may be blocked in previous application run
                                dataWrapper.pauseAllEvents(true, false);
                            }

                            dataWrapper.firstStartEvents(true, false);
                            PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from PackageReplacedReceiver.onReceive");
                            dataWrapper.updateNotificationAndWidgets(true);
                        } else {
                            PPApplication.logE("PackageReplacedReceiver.onReceive", "global event run is not enabled, manually activate profile");

                            ////// unblock all events for first start
                            //     that may be blocked in previous application run
                            dataWrapper.pauseAllEvents(true, false);

                            dataWrapper.activateProfileOnBoot();
                            PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from PackageReplacedReceiver.onReceive");
                            dataWrapper.updateNotificationAndWidgets(true);
                        }
                    }
                }
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
            default:
                break;
        }

        return Result.success();
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

    private void startService(DataWrapper dataWrapper) {
        boolean isStarted = PPApplication.getApplicationStarted(dataWrapper.context, false);

        PPApplication.logE("PPApplication.exitApp", "from DelayedWorksWorker.doWork shutdown=false");
        PPApplication.exitApp(false, dataWrapper.context, dataWrapper, null, false/*, false, true*/);

        if (isStarted)
        {
            PPApplication.sleep(2000);

            // start PhoneProfilesService
            PPApplication.logE("DelayedWorksWorker.doWork", "xxx");
            PPApplication.setApplicationStarted(dataWrapper.context, true);
            Intent serviceIntent = new Intent(dataWrapper.context, PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            PPApplication.startPPService(dataWrapper.context, serviceIntent);

            //PPApplication.sleep(2000);
        }
    }

}
