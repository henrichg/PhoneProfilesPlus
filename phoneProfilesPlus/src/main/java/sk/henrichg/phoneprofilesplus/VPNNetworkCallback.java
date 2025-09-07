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

    private void doConnection() {
        //final Context appContext = getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        // configured is PPApplication.handlerThreadBroadcast handler (see PhoneProfilesService.registerCallbacks()

        final Context appContext = context.getApplicationContext();
        Runnable runnable = () -> {

            synchronized (PPApplication.handleEventsMutex) {

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_VPNNetworkCallback_doConnection_1);
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    _doConnection(appContext);

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
            }
        };
        PPApplicationStatic.logE("[EXECUTOR_CALL] VPNNetworkCallback.doConnection", "(xxx");
        PPApplicationStatic.createBasicExecutorPool();
        PPApplication.eventsHandlerExecutor.submit(runnable);
    }

    private void _doConnection(Context appContext) {
        if (EventStatic.getGlobalEventsRunning(appContext)) {

            //if ((info.getState() == NetworkInfo.State.CONNECTED) ||
            //        (info.getState() == NetworkInfo.State.DISCONNECTED)) {

            // start events handler

//            PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] VPNNetworkCallback._doConnection", "SENSOR_TYPE_VPN");
            EventsHandler eventsHandler = new EventsHandler(appContext);
            eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_VPN});
        }
    }

}
