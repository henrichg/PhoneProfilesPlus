package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

// Delete button (X) or "clear all" in notification
public class CheckOnlineStatusBroadcastReceiver extends BroadcastReceiver {

    //static boolean deviceIsOnline = false;

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] CheckOnlineStatusBroadcastReceiver.onReceive", "xxx");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        //deviceIsOnline = isOnline(context.getApplicationContext());

        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadBroadcast();
        //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        //__handler.post(new PPApplication.PPHandlerThreadRunnable(
        //        context.getApplicationContext()) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//          PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=CheckOnlineStatusBroadcastReceiver.onReceive");

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

//                    boolean isNetworkRoaming = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).isNetworkRoaming();

                } catch (Exception e) {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        PPApplicationStatic.createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);


//        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] CheckOnlineStatusBroadcastReceiver.onReceive", "xxx");
        Intent _intent = new Intent(PPApplication.PACKAGE_NAME + ".LocationGeofenceEditorOnlineStatusBroadcastReceiver");
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(_intent);
    }

    static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            //noinspection deprecation
            return (networkInfo != null && networkInfo.isConnected());
        }
        else
            return false;
    }


}
