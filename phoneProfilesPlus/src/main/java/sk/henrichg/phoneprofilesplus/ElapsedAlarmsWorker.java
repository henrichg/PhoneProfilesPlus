package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

@SuppressWarnings("WeakerAccess")
public class ElapsedAlarmsWorker extends Worker {

    static final String ELAPSED_ALARMS_GEOFENCE_SCANNER_SWITCH_GPS_TAG_WORK = "elapsedAlarmsGeofenceScannerSwitchGPSWork";
    static final String ELAPSED_ALARMS_LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK = "elapsedAlarmsLockDeviceFinishActivity";
    static final String ELAPSED_ALARMS_LOCK_DEVICE_AFTER_SCREEN_OFF_TAG_WORK = "elapsedAlarmsLockDeviceAfterScreenOff";
    static final String ELAPSED_ALARMS_START_EVENT_NOTIFICATION_TAG_WORK = "elapsedAlarmsStartEventNotificationWork";
    static final String ELAPSED_ALARMS_RUN_APPLICATION_WITH_DELAY_TAG_WORK = "elapsedAlarmsRunApplicationWithDelayWork";
    static final String ELAPSED_ALARMS_PROFILE_DURATION_TAG_WORK = "elapsedAlarmsProfileDurationWork";
    static final String ELAPSED_ALARMS_EVENT_DELAY_START_TAG_WORK = "elapsedAlarmsEventDelayStartWork";
    static final String ELAPSED_ALARMS_EVENT_DELAY_END_TAG_WORK = "elapsedAlarmsEventDelayEndWork";
    static final String ELAPSED_ALARMS_UPDATE_GUI_TAG_WORK = "elapsedAlarmsUpdateGUIWork";
    static final String ELAPSED_ALARMS_SHOW_PROFILE_NOTIFICATION_TAG_WORK = "elapsedAlarmsShowProfileNotificationWork";
    static final String ELAPSED_ALARMS_DONATION_TAG_WORK = "elapsedAlarmsDonationWork";
    static final String ELAPSED_ALARMS_ALARM_CLOCK_SENSOR_TAG_WORK = "elapsedAlarmsAlarmClockSensorWork";
    static final String ELAPSED_ALARMS_TWILIGHT_SCANNER_TAG_WORK = "elapsedAlarmsTwilightScannerWork";
    static final String ELAPSED_ALARMS_TIME_SENSOR_TAG_WORK = "elapsedAlarmsTimeSensorWork";
    static final String ELAPSED_ALARMS_CALENDAR_SENSOR_TAG_WORK = "elapsedAlarmsCalendarSensorWork";
    static final String ELAPSED_ALARMS_CALL_SENSOR_TAG_WORK = "elapsedAlarmsCallSensorWork";
    static final String ELAPSED_ALARMS_SMS_EVENT_SENSOR_TAG_WORK = "elapsedAlarmsSMSSensorWork";
    static final String ELAPSED_ALARMS_NFC_EVENT_SENSOR_TAG_WORK = "elapsedAlarmsNFCSensorWork";
    static final String ELAPSED_ALARMS_DEVICE_BOOT_EVENT_SENSOR_TAG_WORK = "elapsedAlarmsDeviceBootSensorWork";
    static final String ELAPSED_ALARMS_NOTIFICATION_EVENT_SENSOR_TAG_WORK = "elapsedAlarmsNotificationSensorWork";
    static final String ELAPSED_ALARMS_ORIENTATION_EVENT_SENSOR_TAG_WORK = "elapsedAlarmsOrientationSensorWork";



    static final String ELAPSED_ALARMS_GEOFENCE_SCANNER_SWITCH_GPS = "geofence_scanner_switch_gps";
    static final String ELAPSED_ALARMS_LOCK_DEVICE_FINISH_ACTIVITY = "lock_device_finish_activity";
    static final String ELAPSED_ALARMS_LOCK_DEVICE_AFTER_SCREEN_OFF = "lock_device_after_screen_off";
    static final String ELAPSED_ALARMS_START_EVENT_NOTIFICATION = "start_event_notification";
    static final String ELAPSED_ALARMS_RUN_APPLICATION_WITH_DELAY = "run_application_with_delay";
    static final String ELAPSED_ALARMS_PROFILE_DURATION = "profile_duration";
    static final String ELAPSED_ALARMS_EVENT_DELAY_START = "event_delay_start";
    static final String ELAPSED_ALARMS_EVENT_DELAY_END = "event_delay_end";
    static final String ELAPSED_ALARMS_UPDATE_GUI = "update_gui";
    static final String ELAPSED_ALARMS_SHOW_PROFILE_NOTIFICATION = "show_profile_notification";
    //static final String ELAPSED_ALARMS_DONATION = "donation";
    //static final String ELAPSED_ALARMS_TWILIGHT_SCANNER = "twilight_scanner";
    //static final String ELAPSED_ALARMS_TIME_SENSOR = "time_sensor";
    //static final String ELAPSED_ALARMS_ALARM_CLOCK_EVENT_END_SENSOR = "alarm_clock_event_end_sensor";
    //static final String ELAPSED_ALARMS_CALENDAR_SENSOR = "calendar_sensor";
    //static final String ELAPSED_ALARMS_CALL_SENSOR = "call_sensor";
    //static final String ELAPSED_ALARMS_SMS_EVENT_END_SENSOR = "sms_event_end_sensor";
    //static final String ELAPSED_ALARMS_NFC_EVENT_END_SENSOR = "nfc_event_end_sensor";
    //static final String ELAPSED_ALARMS_NOTIFICATION_EVENT_END_SENSOR = "notification_event_end_sensor";
    //static final String ELAPSED_ALARMS_DEVICE_BOOT_EVENT_END_SENSOR = "device_boot_event_end_sensor";

    final Context context;

    public ElapsedAlarmsWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            //PPApplication.logE("ElapsedAlarmsWorker.doWork", "xxx");

            if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return Result.success();

            //Data outputData;

            // Get the input
            String action = getInputData().getString(PhoneProfilesService.EXTRA_ELAPSED_ALARMS_WORK);
            if (action == null) {
                //PPApplication.logE("ElapsedAlarmsWorker.doWork", "action ins null");
                return Result.success();
            }

            //PPApplication.logE("ElapsedAlarmsWorker.doWork", "action=" + action);

            long eventId = getInputData().getLong(PPApplication.EXTRA_EVENT_ID, 0);
            String profileName = getInputData().getString(RunApplicationWithDelayBroadcastReceiver.EXTRA_PROFILE_NAME);
            String runApplicationData = getInputData().getString(RunApplicationWithDelayBroadcastReceiver.EXTRA_RUN_APPLICATION_DATA);
            long profileId = getInputData().getLong(PPApplication.EXTRA_PROFILE_ID, 0);
            boolean forRestartEvents = getInputData().getBoolean(ProfileDurationAlarmBroadcastReceiver.EXTRA_FOR_RESTART_EVENTS, false);
            int startupSource = getInputData().getInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SERVICE_MANUAL);
            //boolean refresh = getInputData().getBoolean(PPApplication.EXTRA_REFRESH, true);
            //boolean refreshAlsoEditor = getInputData().getBoolean(PPApplication.EXTRA_REFRESH_ALSO_EDITOR, true);

            //outputData = generateResult(LocationGeofenceEditorActivity.FAILURE_RESULT,
            //                                    getApplicationContext().getString(R.string.event_preferences_location_no_address_found),
            //                                    updateName);

            //return Result.success(outputData);

            Context appContext = context.getApplicationContext();

            switch (action) {
                case ELAPSED_ALARMS_GEOFENCE_SCANNER_SWITCH_GPS:
                    GeofencesScannerSwitchGPSBroadcastReceiver.doWork();
                    break;
                case ELAPSED_ALARMS_LOCK_DEVICE_FINISH_ACTIVITY:
                    LockDeviceActivityFinishBroadcastReceiver.doWork();
                    break;
                case ELAPSED_ALARMS_LOCK_DEVICE_AFTER_SCREEN_OFF:
                    LockDeviceAfterScreenOffBroadcastReceiver.doWork(false, appContext);
                    break;
                case ELAPSED_ALARMS_START_EVENT_NOTIFICATION:
                    StartEventNotificationBroadcastReceiver.doWork(false, appContext, eventId);
                    break;
                case ELAPSED_ALARMS_RUN_APPLICATION_WITH_DELAY:
                    RunApplicationWithDelayBroadcastReceiver.doWork(appContext, profileName, runApplicationData);
                    break;
                case ELAPSED_ALARMS_PROFILE_DURATION:
                    ProfileDurationAlarmBroadcastReceiver.doWork(false, appContext, profileId, forRestartEvents, startupSource);
                    break;
                case ELAPSED_ALARMS_EVENT_DELAY_START:
                    EventDelayStartBroadcastReceiver.doWork(false, appContext);
                    break;
                case ELAPSED_ALARMS_EVENT_DELAY_END:
                    EventDelayEndBroadcastReceiver.doWork(false, appContext);
                    break;
                case ELAPSED_ALARMS_UPDATE_GUI:
                    //UpdateGUIBroadcastReceiver.doWork(false, appContext, refresh, refreshAlsoEditor/*, true*/);
                    //PPApplication.forceUpdateGUI(context.getApplicationContext(), refreshAlsoEditor, true, refresh);
                    //PPApplication.logE("-------- PPApplication.forceUpdateGUI", "from=ElapsedAlarmsWorker.doWork");
                    PPApplication.forceUpdateGUI(context.getApplicationContext(), true, true/*, true*/);
                    break;
                case ELAPSED_ALARMS_SHOW_PROFILE_NOTIFICATION:
                    //ShowProfileNotificationBroadcastReceiver.doWork(appContext/*, true*/);
                    if ((!PPApplication.doNotShowProfileNotification) &&
                            PhoneProfilesService.getInstance() != null) {
                        try {
                            //PPApplication.logE("ShowProfileNotificationBroadcastReceiver._doWork", "xxx");
                            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
                            Profile profile = dataWrapper.getActivatedProfileFromDB(false, false);
                            //PPApplication.logE("ShowProfileNotificationBroadcastReceiver._doWork", "_showProfileNotification()");
                            if (PhoneProfilesService.getInstance() != null) {
                                //PPApplication.logE("ShowProfileNotificationBroadcastReceiver._doWork", "handler");
                                PhoneProfilesService.getInstance()._showProfileNotification(profile, dataWrapper, /*false,*/ false/*, cleared*/);
                            }
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    break;
                //case ELAPSED_ALARMS_DONATION:
                //    DonationBroadcastReceiver.doWork(false, appContext);
                //    break;
                //case ELAPSED_ALARMS_TWILIGHT_SCANNER:
                //    TwilightScanner.doWork();
                //    break;
                //case ELAPSED_ALARMS_TIME_SENSOR:
                //    EventTimeBroadcastReceiver.doWork(false, appContext);
                //    break;
                //case ELAPSED_ALARMS_ALARM_CLOCK_EVENT_END_SENSOR:
                //    AlarmClockEventEndBroadcastReceiver.doWork(false, appContext);
                //    break;
                //case ELAPSED_ALARMS_CALENDAR_SENSOR:
                //    EventCalendarBroadcastReceiver.doWork(false, appContext);
                //    break;
                //case ELAPSED_ALARMS_CALL_SENSOR:
                //    MissedCallEventEndBroadcastReceiver.doWork(false, appContext);
                //    break;
                //case ELAPSED_ALARMS_SMS_EVENT_END_SENSOR:
                //    SMSEventEndBroadcastReceiver.doWork(false, appContext);
                //    break;
                //case ELAPSED_ALARMS_NFC_EVENT_END_SENSOR:
                //    NFCEventEndBroadcastReceiver.doWork(false, appContext);
                //    break;
                //case ELAPSED_ALARMS_NOTIFICATION_EVENT_END_SENSOR:
                //    NotificationEventEndBroadcastReceiver.doWork(false, appContext);
                //    break;
                //case ELAPSED_ALARMS_DEVICE_BOOT_EVENT_END_SENSOR:
                //    DeviceBootEventEndBroadcastReceiver.doWork(false, appContext);
                //    break;
                default:
                    break;
            }

            return Result.success();
        } catch (Exception e) {
            Log.e("ElapsedAlarmsWorker.doWork", Log.getStackTraceString(e));
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
