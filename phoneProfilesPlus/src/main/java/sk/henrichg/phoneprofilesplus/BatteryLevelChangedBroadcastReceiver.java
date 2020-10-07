package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.PowerManager;

public class BatteryLevelChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[BROADCAST CALL] BatteryLevelChangedBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "BatteryLevelChangedBroadcastReceiver.onReceive", "BatteryLevelChangedBroadcastReceiver_onReceive");
        //CallsCounter.logCounterNoInc(context, "BatteryLevelChangedBroadcastReceiver.onReceive->action="+intent.getAction(), "BatteryLevelChangedBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        //String action = intent.getAction();
        //PPApplication.logE("[TEST BATTERY] BatteryBroadcastReceiver.onReceive", "action=" + action);

        boolean _isCharging = false;
        int _plugged = -1;
        int _level = -1;
        int _batteryPct = -100;

        int _status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        //PPApplication.logE("[TEST BATTERY] BatteryLevelChangedBroadcastReceiver.onReceive", "_status=" + _status);

        if (_status != -1) {
            _isCharging = _status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            _status == BatteryManager.BATTERY_STATUS_FULL;
            _plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            _level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (_level != -1) {
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                _batteryPct = Math.round(_level / (float) scale * 100);
            }
        } else {
            Intent batteryStatus = null;
            try { // Huawei devices: java.lang.IllegalArgumentException: registered too many Broadcast Receivers
                IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                batteryStatus = context.registerReceiver(null, iFilter);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            if (batteryStatus != null) {
                _status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                if (_status != -1) {
                    _isCharging = _status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            _status == BatteryManager.BATTERY_STATUS_FULL;
                    _plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    _level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    if (_level != -1) {
                        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                        _batteryPct = Math.round(_level / (float) scale * 100);
                    }
                }
            }
        }

        /*
        //int _batteryLow = -1;
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                statusReceived = true;
                _isCharging = true;
                //_plugged = -1;
            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                statusReceived = true;
                _isCharging = false;
                _plugged = -1;
            //} else if (action.equals(Intent.ACTION_BATTERY_LOW)) {
            //    statusReceived = true;
            //    _batteryLow = 1;
            //} else if (action.equals(Intent.ACTION_BATTERY_OKAY)) {
            //    statusReceived = true;
            //    _batteryLow = 0;
            }
        }
        */

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("[TEST BATTERY] BatteryLevelChangedBroadcastReceiver.onReceive", "isCharging=" + isCharging);
            PPApplication.logE("[TEST BATTERY] BatteryLevelChangedBroadcastReceiver.onReceive", "_isCharging=" + _isCharging);
            PPApplication.logE("[TEST BATTERY] BatteryLevelChangedBroadcastReceiver.onReceive", "_level=" + _level);
            PPApplication.logE("[TEST BATTERY] BatteryLevelChangedBroadcastReceiver.onReceive", "batteryPct=" + batteryPct);
            PPApplication.logE("[TEST BATTERY] BatteryLevelChangedBroadcastReceiver.onReceive", "_batteryPct=" + _batteryPct);
            PPApplication.logE("[TEST BATTERY] BatteryLevelChangedBroadcastReceiver.onReceive", "plugged=" + plugged);
            PPApplication.logE("[TEST BATTERY] BatteryLevelChangedBroadcastReceiver.onReceive", "_plugged=" + _plugged);
            //PPApplication.logE("BatteryLevelChangedBroadcastReceiver.onReceive", "batteryLow=" + batteryLow);
            //PPApplication.logE("BatteryLevelChangedBroadcastReceiver.onReceive", "_batteryLow=" + _batteryLow);
        }*/

        /* In Samsung S8 lowLevel is configured to 105 :-(
        int _level = appContext.getResources().getInteger(com.android.internal.R.integer.config_lowBatteryWarningLevel);
        PPApplication.logE("BatteryLevelChangedBroadcastReceiver.onReceive", "lowLevel=" + Math.round(_level / (float) scale * 100));
        */

        if ((PPApplication.isCharging != _isCharging) ||
            ((_plugged != -1) && (PPApplication.plugged != _plugged)) ||
            ((_level != -1) && (PPApplication.batteryPct != _batteryPct))) {
//            PPApplication.logE("[BROADCAST CALL] BatteryLevelChangedBroadcastReceiver.onReceive", "---- state changed");

            PPApplication.isCharging = _isCharging;

            if (_plugged != -1)
                PPApplication.plugged = _plugged;

            if (_level != -1)
                PPApplication.batteryPct = _batteryPct;

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

                //if (PPApplication.logEnabled()) {
                //    PPApplication.logE("[TEST BATTERY] BatteryLevelChangedBroadcastReceiver.onReceive", "oldIsPowerSaveMode=" + oldIsPowerSaveMode);
                //    PPApplication.logE("[TEST BATTERY] BatteryLevelChangedBroadcastReceiver.onReceive", "isPowerSaveMode=" + isPowerSaveMode);
                //}

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
                        //PPApplication.logE("[****] BatteryLevelChangedBroadcastReceiver.onReceive", "restartAllScanners");
                        //PPApplication.logE("[RJS] BatteryLevelChangedBroadcastReceiver.onReceive", "restart all scanners");
                        // for screenOn=true -> used only for geofence scanner - start scan with GPS On
                        PPApplication.restartAllScanners(appContext, true);
                    }
                }
            }*/

            if (Event.getGlobalEventsRunning()) {
                PPApplication.startHandlerThreadBroadcast(/*"BatteryLevelChangedBroadcastReceiver.onReceive"*/);
                final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BatteryLevelChangedBroadcastReceiver_onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

//                            PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=BatteryLevelChangedBroadcastReceiver.onReceive");

                            // start events handler
//                            PPApplication.logE("[EVENTS_HANDLER] BatteryLevelChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_BATTERY");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BATTERY_WITH_LEVEL);

                            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=BatteryLevelChangedBroadcastReceiver.onReceive");
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                });
            }
        }
    }

    static void initialize(Context appContext) {
        // get actual battery status
        Intent batteryStatus = null;
        try { // Huawei devices: java.lang.IllegalArgumentException: registered too many Broadcast Receivers
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            batteryStatus = appContext.registerReceiver(null, filter);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
        if (batteryStatus != null) {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            PPApplication.isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            PPApplication.batteryPct = Math.round(level / (float) scale * 100);
            PPApplication.plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        }
    }

}
