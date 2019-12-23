package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.PowerManager;

public class BatteryBroadcastReceiver extends BroadcastReceiver {

    static boolean isCharging = false;
    static int batteryPct = -100;
    static int plugged = -1;
    //static boolean batteryLow = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BatteryBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "BatteryBroadcastReceiver.onReceive", "BatteryBroadcastReceiver_onReceive");
        //CallsCounter.logCounterNoInc(context, "BatteryBroadcastReceiver.onReceive->action="+intent.getAction(), "BatteryBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        boolean statusReceived = false;
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        PPApplication.logE("BatteryBroadcastReceiver.onReceive", "status=" + status);
        boolean _isCharging = false;
        int _plugged = -1;
        if (status != -1) {
            statusReceived = true;
            _isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            _plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        }
        else {
            Intent batteryStatus = null;
            try { // Huawei devices: java.lang.IllegalArgumentException: registered too many Broadcast Receivers
                IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                batteryStatus = context.registerReceiver(null, iFilter);
            } catch (Exception ignored) {}
            if (batteryStatus != null) {
                status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                if (status != -1) {
                    statusReceived = true;
                    _isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;
                    _plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                }
            }
        }

        boolean levelReceived = false;
        int pct = -100;
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale;
        PPApplication.logE("BatteryBroadcastReceiver.onReceive", "level=" + level);
        if (level != -1) {
            levelReceived = true;
            scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            pct = Math.round(level / (float) scale * 100);
        }
        else {
            Intent batteryStatus = null;
            try { // Huawei devices: java.lang.IllegalArgumentException: registered too many Broadcast Receivers
                IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                batteryStatus = context.registerReceiver(null, iFilter);
            } catch (Exception ignored) {}
            if (batteryStatus != null) {
                level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                if (level != -1) {
                    levelReceived = true;
                    scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    pct = Math.round(level / (float) scale * 100);
                }
            }
        }

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
            /*} else if (action.equals(Intent.ACTION_BATTERY_LOW)) {
                statusReceived = true;
                _batteryLow = 1;
            } else if (action.equals(Intent.ACTION_BATTERY_OKAY)) {
                statusReceived = true;
                _batteryLow = 0;*/
            }
        }

        if (PPApplication.logEnabled()) {
            PPApplication.logE("BatteryBroadcastReceiver.onReceive", "action=" + action);
            PPApplication.logE("BatteryBroadcastReceiver.onReceive", "isCharging=" + isCharging);
            PPApplication.logE("BatteryBroadcastReceiver.onReceive", "_isCharging=" + _isCharging);
            //PPApplication.logE("BatteryBroadcastReceiver.onReceive", "batteryLow=" + batteryLow);
            //PPApplication.logE("BatteryBroadcastReceiver.onReceive", "_batteryLow=" + _batteryLow);
            PPApplication.logE("BatteryBroadcastReceiver.onReceive", "batteryPct=" + batteryPct);
            PPApplication.logE("BatteryBroadcastReceiver.onReceive", "pct=" + pct);
            PPApplication.logE("BatteryBroadcastReceiver.onReceive", "plugged=" + plugged);
            PPApplication.logE("BatteryBroadcastReceiver.onReceive", "_plugged=" + _plugged);
        }

        /* In Samsung S8 lowLevel is configured to 105 :-(
        int _level = appContext.getResources().getInteger(com.android.internal.R.integer.config_lowBatteryWarningLevel);
        PPApplication.logE("BatteryBroadcastReceiver.onReceive", "lowLevel=" + Math.round(_level / (float) scale * 100));
        */

        if ((statusReceived && (isCharging != _isCharging) && (plugged != _plugged)) ||
                //(statusReceived && (_batteryLow != -1) && (batteryLow != (_batteryLow == 1))) ||
                (levelReceived && (batteryPct != pct))) {
            PPApplication.logE("BatteryBroadcastReceiver.onReceive", "state changed");

            if (statusReceived) {
                isCharging = _isCharging;
                /*if (_batteryLow != -1)
                    batteryLow = (_batteryLow == 1);*/
                plugged = _plugged;
            }
            if (levelReceived)
                batteryPct = pct;

            // required for reschedule workers for power save mode
            PPApplication.logE("[****] BatteryBroadcastReceiver.onReceive", "restartAllScanners");
            PPApplication.restartAllScanners(appContext, true);
            /*PPApplication.restartWifiScanner(appContext, true);
            PPApplication.restartBluetoothScanner(appContext, true);
            PPApplication.restartGeofenceScanner(appContext, true);
            PPApplication.restartPhoneStateScanner(appContext, true);
            PPApplication.restartOrientationScanner(appContext);*/

            if (Event.getGlobalEventsRunning(appContext)) {
                PPApplication.startHandlerThread("BatteryBroadcastReceiver.onReceive");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BatteryBroadcastReceiver_onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            PPApplication.logE("PPApplication.startHandlerThread", "START run - from=BatteryBroadcastReceiver.onReceive");

                            // start events handler
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BATTERY);

                            PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BatteryBroadcastReceiver.onReceive");
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
}
