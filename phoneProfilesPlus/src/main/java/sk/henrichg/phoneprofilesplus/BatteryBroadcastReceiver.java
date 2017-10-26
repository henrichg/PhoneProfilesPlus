package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class BatteryBroadcastReceiver extends BroadcastReceiver {

    static boolean isCharging = false;
    static int batteryPct = -100;

    public static final String EXTRA_IS_CHARGING = "isCharging";
    public static final String EXTRA_BATTERY_PCT = "batteryPct";
    public static final String EXTRA_STATUS_RECEIVED = "statusReceived";
    public static final String EXTRA_LEVEL_RECEIVED = "levelReceived";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BatteryBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "BatteryBroadcastReceiver.onReceive", "BatteryBroadcastReceiver_onReceive");
        CallsCounter.logCounterNoInc(context, "BatteryBroadcastReceiver.onReceive->action="+intent.getAction(), "BatteryBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        boolean statusReceived = false;
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        PPApplication.logE("BatteryBroadcastReceiver.onReceive", "status=" + status);
        boolean _isCharging = false;
        if (status != -1) {
            statusReceived = true;
            _isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
        }

        boolean levelReceived = false;
        int pct = -100;
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        PPApplication.logE("BatteryBroadcastReceiver.onReceive", "level=" + level);
        if (level != -1) {
            levelReceived = true;
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            pct = Math.round(level / (float) scale * 100);
        }

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
            statusReceived = true;
            _isCharging = true;
        }
        else
        if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            statusReceived = true;
            _isCharging = false;
        }

        PPApplication.logE("BatteryBroadcastReceiver.onReceive", "isCharging=" + isCharging);
        PPApplication.logE("BatteryBroadcastReceiver.onReceive", "_isCharging=" + _isCharging);
        PPApplication.logE("BatteryBroadcastReceiver.onReceive", "batteryPct=" + batteryPct);
        PPApplication.logE("BatteryBroadcastReceiver.onReceive", "pct=" + pct);

        if ((statusReceived && (isCharging != _isCharging)) || (levelReceived && (batteryPct != pct))) {
            PPApplication.logE("BatteryBroadcastReceiver.onReceive", "state changed");

            if (statusReceived)
                isCharging = _isCharging;
            if (levelReceived)
                batteryPct = pct;

            //BatteryJob.start(appContext, isCharging, batteryPct, statusReceived, levelReceived);

            Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_JOBS, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            //TODO Android O
            //if (Build.VERSION.SDK_INT < 26)
            appContext.startService(serviceIntent);
            //else
            //    context.startForegroundService(serviceIntent);

            if (Event.getGlobalEventsRunning(appContext)) {
                final Handler handler = new Handler(appContext.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BatteryBroadcastReceiver.onReceive");
                        wakeLock.acquire();

                        // start events handler
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BATTERY, false);

                        wakeLock.release();
                    }
                });
            }
        }
    }
}
