package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PowerSaveModeBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "powerSaveMode";

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### PowerSaveModeBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(appContext);

        boolean oldPowerSaveMode = PPApplication.isPowerSaveMode;
        PPApplication.isPowerSaveMode = false;
        if (ApplicationPreferences.applicationPowerSaveModeInternal(appContext).equals("3")) {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PPApplication.isPowerSaveMode = powerManager.isPowerSaveMode();
        }
        else
            PPApplication.isPowerSaveMode = oldPowerSaveMode;

        if (Event.getGlobalEventsRuning(appContext))
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
                Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
            //}
        }
    }
}
