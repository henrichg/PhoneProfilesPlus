package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class WorkerWithoutData extends Worker {

    static final String GEOFENCE_SCANNER_SWITCH_GPS_TAG_WORK = "geofenceScannerSwitchGPSWorkWithoutData";
    static final String LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK = "lockDeviceFinishActivityWorkWithoutData";
    static final String LOCK_DEVICE_AFTER_SCREEN_OFF_TAG_WORK = "lockDeviceAfterScreenOffWorkWithoutData";
    static final String EVENT_DELAY_START_TAG_WORK = "eventDelayStartWorkWithoutData";
    static final String EVENT_DELAY_END_TAG_WORK = "eventDelayEndWorkWithoutData";
    static final String UPDATE_GUI_TAG_WORK = "updateGUIWorkWithoutData";
    static final String SHOW_PROFILE_NOTIFICATION_TAG_WORK = "showProfileNotificationWorkWithoutData";
    //static final String ELAPSED_ALARMS_DONATION_TAG_WORK = "elapsedAlarmsDonationWork";
    //static final String ELAPSED_ALARMS_ALARM_CLOCK_SENSOR_TAG_WORK = "elapsedAlarmsAlarmClockSensorWork";
    //static final String ELAPSED_ALARMS_TWILIGHT_SCANNER_TAG_WORK = "elapsedAlarmsTwilightScannerWork";
    //static final String ELAPSED_ALARMS_TIME_SENSOR_TAG_WORK = "elapsedAlarmsTimeSensorWork";
    //static final String ELAPSED_ALARMS_CALENDAR_SENSOR_TAG_WORK = "elapsedAlarmsCalendarSensorWork";
    //static final String ELAPSED_ALARMS_CALL_SENSOR_TAG_WORK = "elapsedAlarmsCallSensorWork";
    //static final String ELAPSED_ALARMS_SMS_EVENT_SENSOR_TAG_WORK = "elapsedAlarmsSMSSensorWork";
    //static final String ELAPSED_ALARMS_NFC_EVENT_SENSOR_TAG_WORK = "elapsedAlarmsNFCSensorWork";
    //static final String ELAPSED_ALARMS_DEVICE_BOOT_EVENT_SENSOR_TAG_WORK = "elapsedAlarmsDeviceBootSensorWork";
    //static final String ELAPSED_ALARMS_NOTIFICATION_EVENT_SENSOR_TAG_WORK = "elapsedAlarmsNotificationSensorWork";
    //static final String ELAPSED_ALARMS_ORIENTATION_EVENT_SENSOR_TAG_WORK = "elapsedAlarmsOrientationSensorWork";

    /*
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
    */
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

    public WorkerWithoutData(
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
            //String action = getInputData().getString(PhoneProfilesService.EXTRA_ELAPSED_ALARMS_WORK);
            //if (action == null) {
            //    //PPApplication.logE("ElapsedAlarmsWorker.doWork", "action ins null");
            //    return Result.success();
            //}

            //PPApplication.logE("ElapsedAlarmsWorker.doWork", "action=" + action);

            //outputData = generateResult(LocationGeofenceEditorActivity.FAILURE_RESULT,
            //                                    getApplicationContext().getString(R.string.event_preferences_location_no_address_found),
            //                                    updateName);

            //return Result.success(outputData);

            Context appContext = context.getApplicationContext();

            Set<String> tags = getTags();
            for (String tag : tags) {
                PPApplication.logE("WorkerWithoutData.doWork", "tag=" + tag);

                switch (tag) {
                    case GEOFENCE_SCANNER_SWITCH_GPS_TAG_WORK:
                        GeofencesScannerSwitchGPSBroadcastReceiver.doWork();
                        break;
                    case LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK:
                        LockDeviceActivityFinishBroadcastReceiver.doWork();
                        break;
                    case LOCK_DEVICE_AFTER_SCREEN_OFF_TAG_WORK:
                        LockDeviceAfterScreenOffBroadcastReceiver.doWork(false, appContext);
                        break;
                    case UPDATE_GUI_TAG_WORK:
                        //UpdateGUIBroadcastReceiver.doWork(false, appContext, refresh, refreshAlsoEditor/*, true*/);
                        //PPApplication.forceUpdateGUI(context.getApplicationContext(), refreshAlsoEditor, true, refresh);
                        //PPApplication.logE("-------- PPApplication.forceUpdateGUI", "from=ElapsedAlarmsWorker.doWork");
                        PPApplication.forceUpdateGUI(appContext, true, true/*, true*/);
                        break;
                    case SHOW_PROFILE_NOTIFICATION_TAG_WORK:
                        //ShowProfileNotificationBroadcastReceiver.doWork(appContext/*, true*/);
                        if ((!PPApplication.doNotShowProfileNotification) &&
                                PhoneProfilesService.getInstance() != null) {
                            try {
                                //PPApplication.logE("ShowProfileNotificationBroadcastReceiver._doWork", "xxx");
                                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
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
                        if (tag.startsWith(EVENT_DELAY_START_TAG_WORK))
                            EventDelayStartBroadcastReceiver.doWork(false, appContext);
                        else
                        if (tag.startsWith(EVENT_DELAY_END_TAG_WORK))
                            EventDelayEndBroadcastReceiver.doWork(false, appContext);

                        break;
                }
            }

            return Result.success();
        } catch (Exception e) {
            //Log.e("ElapsedAlarmsWorker.doWork", Log.getStackTraceString(e));
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
