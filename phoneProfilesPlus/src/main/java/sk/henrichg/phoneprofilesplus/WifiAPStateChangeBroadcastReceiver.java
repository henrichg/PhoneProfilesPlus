package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

public class WifiAPStateChangeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("[IN_BROADCAST] WifiAPStateChangeBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "WifiAPStateChangeBroadcastReceiver.onReceive", "WifiAPStateChangeBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning())
        {
            PPApplication.startHandlerThreadBroadcast(/*"WifiAPStateChangeBroadcastReceiver.onReceive"*/);
            final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=WifiAPStateChangeBroadcastReceiver.onReceive");

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiAPStateChangeBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        boolean isWifiAPEnabled = false;
                        if (!ApplicationPreferences.applicationEventWifiScanIgnoreHotspot) {
                            if (Build.VERSION.SDK_INT < 28)
                                isWifiAPEnabled = WifiApManager.isWifiAPEnabled(appContext);
                            else
                                isWifiAPEnabled = CmdWifiAP.isEnabled();
                        }
                        if (isWifiAPEnabled) {
                            // Wifi AP is enabled - cancel wifi scan work
                            //PPApplication.logE("WifiAPStateChangeBroadcastReceiver.onReceive","wifi AP enabled");
                            WifiScanWorker.cancelWork(appContext, true);
                        }
                        else {
                            // Wifi AP is disabled - schedule wifi scan work
                            //PPApplication.logE("[RJS] WifiAPStateChangeBroadcastReceiver.onReceive","wifi AP disabled");
                            if (PhoneProfilesService.getInstance() != null) {
                                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
                                dataWrapper.fillEventList();
                                PhoneProfilesService.getInstance().scheduleWifiWorker(/*true,*/ dataWrapper/*, false, true, false, false*/);
                            }
                        }
                    } catch (Exception e) {
                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            });
        }
    }

}
