package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

@TargetApi(Build.VERSION_CODES.M)
class DeviceIdleModeJob extends Job {

    static final String JOB_TAG  = "DeviceIdleModeJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "DeviceIdleModeJob.onRunJob", "DeviceIdleModeJob_onRunJob");

        if (Event.getGlobalEventsRunning(appContext))
        {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            // isLightDeviceIdleMode() is @hide :-(
            if (!powerManager.isDeviceIdleMode() /*&& !powerManager.isLightDeviceIdleMode()*/)
            {
                // start events handler
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_DEVICE_IDLE_MODE, false);

                // rescan
                if (PhoneProfilesService.instance != null) {
                    // schedule job for one wifi scan
                    PhoneProfilesService.instance.scheduleWifiJob(true, true, true, true, false, false);
                    // schedule job for one bluetooth scan
                    PhoneProfilesService.instance.scheduleBluetoothJob(true, true, true, true, false);
                    // schedule job for location scan
                    PhoneProfilesService.instance.scheduleGeofenceScannerJob(true, true, true, true);
                }
                DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS) > 0) {
                    // rescan mobile cells
                    if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isPhoneStateScannerStarted()) {
                        PhoneProfilesService.phoneStateScanner.rescanMobileCells();
                    }
                }
                dataWrapper.invalidateDataWrapper();
            }
        }

        return Result.SUCCESS;
    }

    static void start() {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .startNow()
                .build()
                .schedule();
    }

}
