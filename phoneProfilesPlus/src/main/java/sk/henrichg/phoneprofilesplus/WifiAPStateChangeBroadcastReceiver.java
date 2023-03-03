package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

public class WifiAPStateChangeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] WifiAPStateChangeBroadcastReceiver.onReceive", "xxx");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (EventStatic.getGlobalEventsRunning(context))
        {
            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadBroadcast(/*"WifiAPStateChangeBroadcastReceiver.onReceive"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        context.getApplicationContext()) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=WifiAPStateChangeBroadcastReceiver.onReceive");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiAPStateChangeBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        boolean isWifiAPEnabled = false;
                        if (!ApplicationPreferences.applicationEventWifiScanIgnoreHotspot) {
                            if (Build.VERSION.SDK_INT < 30)
                                isWifiAPEnabled = WifiApManager.isWifiAPEnabled(appContext);
                            else
                                //isWifiAPEnabled = CmdWifiAP.isEnabled(appContext);
                                isWifiAPEnabled = WifiApManager.isWifiAPEnabledA30(appContext);
                        }
                        if (isWifiAPEnabled) {
                            // Wifi AP is enabled - cancel wifi scan work
                            WifiScanWorker.cancelWork(appContext, false);
                        } else {
                            // Wifi AP is disabled - schedule wifi scan work
                            if (PhoneProfilesService.getInstance() != null) {
                                DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                                dataWrapper.fillEventList();
                                PhoneProfilesServiceStatic.scheduleWifiWorker(/*true,*/ dataWrapper/*, false, true, false, false*/);
                            }
                        }
                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            }; //);
            PPApplicationStatic.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);
        }
    }

}
