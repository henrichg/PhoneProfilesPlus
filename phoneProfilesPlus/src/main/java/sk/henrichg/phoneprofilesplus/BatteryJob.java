package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class BatteryJob extends Job {

    static final String JOB_TAG  = "BatteryJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "BatteryJob.onRunJob", "BatteryJob_onRunJob");

        Bundle bundle = params.getTransientExtras();

        boolean statusReceived = bundle.getBoolean(BatteryBroadcastReceiver.EXTRA_STATUS_RECEIVED, false);
        boolean levelReceived = bundle.getBoolean(BatteryBroadcastReceiver.EXTRA_LEVEL_RECEIVED, false);
        boolean isCharging;
        int batteryPct;
        isCharging = bundle.getBoolean(BatteryBroadcastReceiver.EXTRA_IS_CHARGING, false);
        batteryPct = bundle.getInt(BatteryBroadcastReceiver.EXTRA_BATTERY_PCT, -100);
        if (!(statusReceived && levelReceived)) {
            // get battery status
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = appContext.registerReceiver(null, filter);

            if (batteryStatus != null) {
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;

                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                batteryPct = Math.round(level / (float) scale * 100);
            }
        }

        PPApplication.logE("BatteryJob.onRunJob", "batteryPct=" + batteryPct);
        PPApplication.logE("BatteryJob.onRunJob", "isCharging=" + isCharging);

        /*
        boolean oldPowerSaveMode = PPApplication.isPowerSaveMode;
        PPApplication.isPowerSaveMode = false;
        if ((!isCharging) &&
                ((ApplicationPreferences.applicationPowerSaveModeInternal(appContext).equals("1") && (batteryPct <= 5)) ||
                        (ApplicationPreferences.applicationPowerSaveModeInternal(appContext).equals("2") && (batteryPct <= 15))))
            PPApplication.isPowerSaveMode = true;
        else {
            if (isCharging)
                PPApplication.isPowerSaveMode = false;
            else
                PPApplication.isPowerSaveMode = oldPowerSaveMode;
        }
        */

        Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_JOBS, true);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        //TODO Android O
        //if (Build.VERSION.SDK_INT < 26)
        appContext.startService(serviceIntent);
        //else
        //    context.startForegroundService(serviceIntent);

        if (Event.getGlobalEventsRunning(appContext)) {
                /*
                if (PhoneProfilesService.instance != null) {
                    if (PhoneProfilesService.isGeofenceScannerStarted())
                        PhoneProfilesService.getGeofencesScanner().resetLocationUpdates(oldPowerSaveMode, false);
                    PhoneProfilesService.instance.resetListeningOrientationSensors(oldPowerSaveMode, false);
                    if (PhoneProfilesService.isPhoneStateScannerStarted())
                        PhoneProfilesService.phoneStateScanner.resetListening(oldPowerSaveMode, false);
                }
                */

                /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                batteryEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BATTERY) > 0;
                dataWrapper.invalidateDataWrapper();

                if (batteryEventsExists)
                {*/
            // start events handler
            EventsHandler eventsHandler = new EventsHandler(appContext);
            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BATTERY, false);
            //}
        }
        
        return Result.SUCCESS;
    }

    static void start(Context context, boolean isCharging, int batteryPct, boolean statusReceived, boolean levelReceived) {
        final JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        final Bundle bundle = new Bundle();
        bundle.putBoolean(BatteryBroadcastReceiver.EXTRA_IS_CHARGING, isCharging);
        bundle.putInt(BatteryBroadcastReceiver.EXTRA_BATTERY_PCT, batteryPct);
        bundle.putBoolean(BatteryBroadcastReceiver.EXTRA_STATUS_RECEIVED, statusReceived);
        bundle.putBoolean(BatteryBroadcastReceiver.EXTRA_LEVEL_RECEIVED, levelReceived);

        final Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    jobBuilder
                            .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                            .setTransientExtras(bundle)
                            .startNow()
                            .build()
                            .schedule();
                } catch (Exception ignored) { }
            }
        });
    }
    
}
