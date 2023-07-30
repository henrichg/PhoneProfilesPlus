package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerSaveModeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] PowerSaveModeBroadcastReceiver.onReceive", "xxx");

        final Context appContext = context.getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        //PPApplication.isPowerSaveMode = DataWrapper.isPowerSaveMode(appContext);

        // restart scanners when any is enabled
        // required for reschedule workers for power save mode
        boolean restart = false;
        if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning)
            restart = true;
        else
        if (ApplicationPreferences.applicationEventLocationEnableScanning)
            restart = true;
        else
        if (ApplicationPreferences.applicationEventWifiEnableScanning)
            restart = true;
        else
        if (ApplicationPreferences.applicationEventBluetoothEnableScanning)
            restart = true;
        else
        if (ApplicationPreferences.applicationEventMobileCellEnableScanning) {
//            PPApplicationStatic.logE("[TEST BATTERY] PowerSaveModeBroadcastReceiver.onReceive", "******** ### *******");
            restart = true;
        }
        else
        if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
//            PPApplicationStatic.logE("[TEST BATTERY] PowerSaveModeBroadcastReceiver.onReceive", "******** ### *******");
            restart = true;
        }
        if (restart) {
            // for screenOn=true -> used only for Location scanner - start scan with GPS On
            PPApplicationStatic.restartAllScanners(appContext, true);
        }

        if (EventStatic.getGlobalEventsRunning(appContext)) {
            PPExecutors.handleEvents(appContext, EventsHandler.SENSOR_TYPE_POWER_SAVE_MODE, PPExecutors.SENSOR_NAME_SENSOR_TYPE_POWER_SAVE_MODE, 0);
        }

    }
}
