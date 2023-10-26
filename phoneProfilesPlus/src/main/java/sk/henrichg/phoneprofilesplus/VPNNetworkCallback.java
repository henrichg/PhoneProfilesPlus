package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.PowerManager;

import androidx.annotation.NonNull;

public class VPNNetworkCallback extends ConnectivityManager.NetworkCallback {

    private final Context context;

    VPNNetworkCallback(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onLost(@NonNull Network network) {
        //record vpn disconnect event
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- VPNNetworkCallback.onLost", "xxx");
        PPApplication.vpnNetworkConnected = false;
        doConnection();
    }

    @Override
    public void onUnavailable() {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- VPNNetworkCallback.onUnavailable", "xxx");
        PPApplication.vpnNetworkConnected = false;
        doConnection();
    }

    @Override
    public void onLosing(@NonNull Network network, int maxMsToLive) {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- VPNNetworkCallback.onLosing", "xxx");
        doConnection();
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        //record vpn connect event
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- VPNNetworkCallback.onAvailable", "xxx");
        PPApplication.vpnNetworkConnected = true;
        doConnection();
    }

/*
    private void doConnection() {
        //final Context appContext = getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true))
            // application is not started
            return;

        PPApplication.startHandlerThreadBroadcast();
        final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        handler.postDelayed(() -> {
//            PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=VPNNetworkCallback.doConnection");

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":VPNNetworkCallback_doConnection");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                _doConnection();

            } catch (Exception e) {
//                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
            }
        }, 5000);
    }
*/

    private void doConnection() {
        //final Context appContext = getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        // configured is PPApplication.handlerThreadBroadcast handler (see PhoneProfilesService.registerCallbacks()

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        try {
            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_VPNNetworkCallback_doConnection_1);
                wakeLock.acquire(10 * 60 * 1000);
            }

            _doConnection(context);

        } catch (Exception e) {
//                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        } finally {
            if ((wakeLock != null) && wakeLock.isHeld()) {
                try {
                    wakeLock.release();
                } catch (Exception ignored) {
                }
            }
        }
        /*}
        else {
            final Context appContext = context;
            //PPApplication.startHandlerThreadBroadcast();
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        appContext) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=VPNNetworkCallback.doConnection");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":VPNNetworkCallback_doConnection_2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        _doConnection(appContext);

                    } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        }*/
    }

    private void _doConnection(Context appContext) {
        if (EventStatic.getGlobalEventsRunning(appContext)) {
            //if ((info.getState() == NetworkInfo.State.CONNECTED) ||
            //        (info.getState() == NetworkInfo.State.DISCONNECTED)) {

            // start events handler

//            PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] VPNNetworkCallback._doConnection", "sensorType=SENSOR_TYPE_RADIO_VPN");
            EventsHandler eventsHandler = new EventsHandler(appContext);
            eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_VPN});

        }
    }

}
