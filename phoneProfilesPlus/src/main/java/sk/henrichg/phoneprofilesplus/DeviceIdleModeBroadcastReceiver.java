package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class DeviceIdleModeBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "deviceIdleMode";

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        GlobalData.logE("##### DeviceIdleModeBroadcastReceiver.onReceive","xxx");

        if (!GlobalData.getApplicationStarted(context))
            // application is not started
            return;

        GlobalData.loadPreferences(context);

        if (GlobalData.getGlobalEventsRuning(context))
        {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (!powerManager.isDeviceIdleMode())
            {
                // start service
                Intent eventsServiceIntent = new Intent(context, EventsService.class);
                eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                startWakefulService(context, eventsServiceIntent);
            }
        }
    }
}
