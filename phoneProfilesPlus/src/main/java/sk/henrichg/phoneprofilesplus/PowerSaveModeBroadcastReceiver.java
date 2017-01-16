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
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### PowerSaveModeBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        boolean oldPowerSaveMode = PPApplication.isPowerSaveMode;
        PPApplication.isPowerSaveMode = false;
        if (PPApplication.applicationPowerSaveModeInternal.equals("3")) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PPApplication.isPowerSaveMode = powerManager.isPowerSaveMode();
        }
        else
            PPApplication.isPowerSaveMode = oldPowerSaveMode;

        if (PPApplication.getGlobalEventsRuning(context))
        {
            if (PhoneProfilesService.instance != null) {
                if (PhoneProfilesService.isGeofenceScannerStarted())
                    PhoneProfilesService.geofencesScanner.resetLocationUpdates(oldPowerSaveMode, false);
                PhoneProfilesService.instance.resetListeningOrientationSensors(oldPowerSaveMode, false);
                if (PhoneProfilesService.isPhoneStateStarted())
                    PhoneProfilesService.phoneStateScanner.resetListening(oldPowerSaveMode, false);
            }

            //if (!powerSaveMode)
            //{
                // start service
                Intent eventsServiceIntent = new Intent(context, EventsService.class);
                eventsServiceIntent.putExtra(PPApplication.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                startWakefulService(context, eventsServiceIntent);
            //}
        }
    }
}
