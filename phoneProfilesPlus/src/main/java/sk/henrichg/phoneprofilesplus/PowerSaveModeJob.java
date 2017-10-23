package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class PowerSaveModeJob extends Job {

    static final String JOB_TAG  = "PowerSaveModeJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "PowerSaveModeJob.onRunJob", "PowerSaveModeJob_onRunJob");

        /*
        boolean oldPowerSaveMode = PPApplication.isPowerSaveMode;
        PPApplication.isPowerSaveMode = false;
        if (ApplicationPreferences.applicationPowerSaveModeInternal(appContext).equals("3")) {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PPApplication.isPowerSaveMode = powerManager.isPowerSaveMode();
        }
        else
            PPApplication.isPowerSaveMode = oldPowerSaveMode;
        */

        Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_JOBS, true);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        //TODO Android O
        //if (Build.VERSION.SDK_INT < 26)
        appContext.startService(serviceIntent);
        //else
        //    context.startForegroundService(serviceIntent);

        if (Event.getGlobalEventsRunning(appContext))
        {
                /*
                if (PhoneProfilesService.instance != null) {
                    if (PhoneProfilesService.isGeofenceScannerStarted())
                        PhoneProfilesService.getGeofencesScanner().resetLocationUpdates(oldPowerSaveMode, false);
                    PhoneProfilesService.instance.resetListeningOrientationSensors(oldPowerSaveMode, false);
                    if (PhoneProfilesService.isPhoneStateScannerStarted())
                        PhoneProfilesService.phoneStateScanner.resetListening(oldPowerSaveMode, false);
                }
                */

            //if (!powerSaveMode)
            //{
            // start events handler
            EventsHandler eventsHandler = new EventsHandler(appContext);
            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_POWER_SAVE_MODE, false);
            //}
        }

        return Result.SUCCESS;
    }

    static void start() {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        try {
            jobBuilder
                    .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                    .startNow()
                    .build()
                    .schedule();
        } catch (Exception ignored) { }
    }

}
