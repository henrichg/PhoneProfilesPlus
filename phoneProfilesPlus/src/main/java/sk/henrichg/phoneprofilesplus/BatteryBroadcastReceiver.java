package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class BatteryBroadcastReceiver extends BroadcastReceiver {

    private static boolean isCharging = false;
    private static int batteryPct = -100;

    public static final String EXTRA_IS_CHARGING = "isCharging";
    public static final String EXTRA_BATTERY_PCT = "batteryPct";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BatteryBroadcastReceiver.onReceive","xxx");

        CallsCounter.logCounter(context, "BatteryBroadcastReceiver.onReceive", "BatteryBroadcastReceiver_onReceive");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        PPApplication.logE("BatteryBroadcastReceiver.onReceive", "status=" + status);

        if (status != -1) {
            boolean _isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            PPApplication.logE("BatteryBroadcastReceiver.onReceive", "isCharging=" + isCharging);

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int pct = Math.round(level / (float) scale * 100);


            if ((isCharging != _isCharging) || (batteryPct != pct)) {
                PPApplication.logE("BatteryBroadcastReceiver.onReceive", "state changed");
                PPApplication.logE("BatteryBroadcastReceiver.onReceive", "batteryPct=" + pct);
                PPApplication.logE("BatteryBroadcastReceiver.onReceive", "level=" + level);
                PPApplication.logE("BatteryBroadcastReceiver.onReceive", "isCharging=" + isCharging);
                PPApplication.logE("BatteryBroadcastReceiver.onReceive", "_isCharging=" + _isCharging);

                isCharging = _isCharging;
                batteryPct = pct;

                try {
                    Intent serviceIntent = new Intent(context, BatteryService.class);
                    serviceIntent.setAction(intent.getAction());
                    serviceIntent.putExtra(EXTRA_IS_CHARGING, isCharging);
                    serviceIntent.putExtra(EXTRA_BATTERY_PCT, batteryPct);
                    WakefulIntentService.sendWakefulWork(context, serviceIntent);
                } catch (Exception ignored) {}
            }
        }
    }
}
