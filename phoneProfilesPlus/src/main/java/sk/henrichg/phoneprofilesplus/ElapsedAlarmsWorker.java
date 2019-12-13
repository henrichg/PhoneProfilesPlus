package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

@SuppressWarnings("WeakerAccess")
public class ElapsedAlarmsWorker extends Worker {

    static final String ELAPSED_ALARMS_GEOFENCE_SCANNER_SWITCH_GPS = "geofence_scanner_swith_gps";
    static final String ELAPSED_ALARMS_LOCK_DEVICE_FINISH_ACTIVITY = "lock_device_finish_activity";
    static final String ELAPSED_ALARMS_LOCK_DEVICE_AFTER_SCREEN_OFF = "lock_device_after_screen_off";

    Context context;

    public ElapsedAlarmsWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        PPApplication.logE("ElapsedAlarmsWorker.doWork", "xxx");

        //Data outputData;

        // Get the input
        String action = getInputData().getString(PhoneProfilesService.EXTRA_ELAPSED_ALARMS_WORK);
        if (action == null) {
            PPApplication.logE("ElapsedAlarmsWorker.doWork", "action ins null");
            return Result.success();
        }

        PPApplication.logE("ElapsedAlarmsWorker.doWork", "action="+action);

        //boolean activateProfiles = getInputData().getBoolean(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
        //String sensorType = getInputData().getString(PhoneProfilesService.EXTRA_SENSOR_TYPE);

        //outputData = generateResult(LocationGeofenceEditorActivity.FAILURE_RESULT,
        //                                    getApplicationContext().getString(R.string.event_preferences_location_no_address_found),
        //                                    updateName);

        //return Result.success(outputData);

        //Context appContext = getApplicationContext();

        switch (action) {
            case ELAPSED_ALARMS_GEOFENCE_SCANNER_SWITCH_GPS:
                GeofencesScannerSwitchGPSBroadcastReceiver.doWork();
                break;
            case ELAPSED_ALARMS_LOCK_DEVICE_FINISH_ACTIVITY:
                LockDeviceActivityFinishBroadcastReceiver.doWork();
                break;
            case ELAPSED_ALARMS_LOCK_DEVICE_AFTER_SCREEN_OFF:
                final Context appContext = context.getApplicationContext();
                LockDeviceAfterScreenOffBroadcastReceiver.doWork(false, appContext);
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

}
