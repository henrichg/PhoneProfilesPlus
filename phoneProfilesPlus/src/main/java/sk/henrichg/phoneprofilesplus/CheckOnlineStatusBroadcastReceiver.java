package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.PowerManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

// Delete button (X) or "clear all" in notification
public class CheckOnlineStatusBroadcastReceiver extends BroadcastReceiver {

    //static boolean deviceIsOnline = false;

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] CheckOnlineStatusBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        //deviceIsOnline = isOnline(context.getApplicationContext());

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        //__handler.post(new PPApplication.PPHandlerThreadRunnable(
        //        context.getApplicationContext()) {
        __handler.post(() -> {
//          PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=CheckOnlineStatusBroadcastReceiver.onReceive");

            //Context appContext= appContextWeakRef.get();
            //if (appContext != null) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":CheckOnlineStatusBroadcastReceiver_onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    LocationScanner.onlineStatusChanged(appContext);

                } catch (Exception e) {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                }
            //}
        });


//        PPApplication.logE("[LOCAL_BROADCAST_CALL] CheckOnlineStatusBroadcastReceiver.onReceive", "xxx");
        Intent _intent = new Intent(PPApplication.PACKAGE_NAME + ".LocationGeofenceEditorOnlineStatusBroadcastReceiver");
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(_intent);
    }

    static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//            PPApplication.logE("[CONNECTIVITY_TEST] CheckOnlineStatusBroadcastReceiver.isOnline", "isOnline="+(networkInfo != null && networkInfo.isConnected()));
            return (networkInfo != null && networkInfo.isConnected());
        }
        else
            return false;
    }


}
