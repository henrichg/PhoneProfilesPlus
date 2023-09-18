package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BatteryChargingChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] BatteryChargingChangedBroadcastReceiver.onReceive", "xxx");

        //final Context appContext = context.getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        String action = intent.getAction();
//        PPApplicationStatic.logE("[IN_BROADCAST] BatteryChargingChangedBroadcastReceiver.onReceive", "action=" + action);

        if (action == null)
            return;

//        PPApplicationStatic.logE("[IN_BROADCAST] BatteryChargingChangedBroadcastReceiver.onReceive", "isCharging="+PPApplication.isCharging);
//        PPApplicationStatic.logE("[IN_BROADCAST] BatteryChargingChangedBroadcastReceiver.onReceive", "plugged="+PPApplication.plugged);

        boolean _isCharging = false;
        //int _plugged = -1;

        if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
            //_plugged = -BatteryManager.BATTERY_STATUS_CHARGING;
            _isCharging = true;
        }
        /*else
        if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            //_plugged = -BatteryManager.BATTERY_STATUS_NOT_CHARGING;
            _isCharging = false;
        }*/

//        PPApplicationStatic.logE("[IN_BROADCAST] BatteryChargingChangedBroadcastReceiver.onReceive", "PPApplication.isCharging="+PPApplication.isCharging);
//        PPApplicationStatic.logE("[IN_BROADCAST] BatteryChargingChangedBroadcastReceiver.onReceive", "_isCharging="+_isCharging);

        if ((PPApplication.isCharging != _isCharging) /*||
            ((_plugged != -1) && (PPApplication.plugged != _plugged))*/) {
//            PPApplicationStatic.logE("[IN_BROADCAST] BatteryChargingChangedBroadcastReceiver.onReceive", "---- state changed");

            PPApplication.isCharging = _isCharging;

            //if (_plugged != -1)
            //    PPApplication.plugged = _plugged;

            /*
            boolean oldIsPowerSaveMode = PPApplication.isPowerSaveMode;
            // restart scanners when any is enabled
            // required for reschedule workers for power save mode
            String applicationPowerSaveModeInternal = ApplicationPreferences.applicationPowerSaveModeInternal;
            if (applicationPowerSaveModeInternal.equals("1") || applicationPowerSaveModeInternal.equals("2")) {
                // power save mode is configured for control battery percentage

                boolean isPowerSaveMode = false;
                if (!PPApplication.isCharging) {
                    if (applicationPowerSaveModeInternal.equals("1") && (PPApplication.batteryPct <= 5))
                        isPowerSaveMode = true;
                    if (applicationPowerSaveModeInternal.equals("2") && (PPApplication.batteryPct <= 15))
                        isPowerSaveMode = true;
                }
                PPApplication.isPowerSaveMode = isPowerSaveMode;

                if (PPApplication.isPowerSaveMode != oldIsPowerSaveMode) {
                    boolean restart = false;
                    if (!PPApplication.isScreenOn) {
                        // screen is off
                        // test also if scanner is enabled only during screen on
                        if (ApplicationPreferences.applicationEventLocationEnableScanning &&
                                ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn)
                            restart = true;
                        else if (ApplicationPreferences.applicationEventWifiEnableScanning &&
                                ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn)
                            restart = true;
                        else if (ApplicationPreferences.applicationEventBluetoothEnableScanning &&
                                ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn)
                            restart = true;
                        else if (ApplicationPreferences.applicationEventMobileCellEnableScanning &&
                                ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn)
                            restart = true;
                        else if (ApplicationPreferences.applicationEventOrientationEnableScanning &&
                                ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn)
                            restart = true;
                    } else {
                        if (ApplicationPreferences.applicationEventLocationEnableScanning)
                            restart = true;
                        else if (ApplicationPreferences.applicationEventWifiEnableScanning)
                            restart = true;
                        else if (ApplicationPreferences.applicationEventBluetoothEnableScanning)
                            restart = true;
                        else if (ApplicationPreferences.applicationEventMobileCellEnableScanning)
                            restart = true;
                        else if (ApplicationPreferences.applicationEventOrientationEnableScanning)
                            restart = true;
                    }
                    if (restart) {
                        // for screenOn=true -> used only for Location scanner - start scan with GPS On
                        PPApplication.restartAllScanners(appContext, true);
                    }
                }
            }
            */

            if (EventStatic.getGlobalEventsRunning(context)) {
                final Context appContext = context.getApplicationContext();
                PPExecutors.handleEvents(appContext,
                        new int[]{EventsHandler.SENSOR_TYPE_BATTERY},
                        PPExecutors.SENSOR_NAME_SENSOR_TYPE_BATTERY, 0);
            }
        }
    }

}
