package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class PowerSaveModeBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "powerSaveMode";

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        GlobalData.logE("##### PowerSaveModeBroadcastReceiver.onReceive","xxx");

        if (!GlobalData.getApplicationStarted(context))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        boolean oldPowerSaveMode = GlobalData.isPowerSaveMode;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        GlobalData.isPowerSaveMode = powerManager.isPowerSaveMode();

        if (GlobalData.getGlobalEventsRuning(context))
        {
            if (GlobalData.geofencesScanner != null)
                GlobalData.geofencesScanner.resetLocationUpdates(oldPowerSaveMode, false);

            //if (!powerSaveMode)
            //{
                // start service
                Intent eventsServiceIntent = new Intent(context, EventsService.class);
                eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                startWakefulService(context, eventsServiceIntent);
            //}
        }
    }
}
