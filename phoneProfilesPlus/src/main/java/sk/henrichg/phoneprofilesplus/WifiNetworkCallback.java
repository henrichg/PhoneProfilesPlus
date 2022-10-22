package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.PowerManager;

import java.util.concurrent.TimeUnit;

public class WifiNetworkCallback extends ConnectivityManager.NetworkCallback {

    private final Context context;

    static volatile boolean connected = false;

    WifiNetworkCallback(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onLost(Network network) {
//        PPApplication.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onLost", "xxx");
        connected = false;
        doConnection();
    }

    @Override
    public void onUnavailable() {
//        PPApplication.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onUnavailable", "xxx");
        connected = false;
        doConnection();
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
//        PPApplication.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onLosing", "xxx");
        doConnection();
    }

    @Override
    public void onAvailable(Network network) {
//        PPApplication.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onAvailable", "xxx");
        connected = true;
        doConnection();
    }

    @Override
    public void onCapabilitiesChanged (Network network, NetworkCapabilities networkCapabilities) {
//        PPApplication.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onCapabilitiesChanged", "xxx");
        doConnection();
    }

/*
    private void doConnection() {

        //final Context appContext = getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        PPApplication.startHandlerThreadBroadcast();
        final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        handler.postDelayed(() -> {
//            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=WifiNetworkCallback.doConnection");

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiNetworkCallback_doConnection");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                _doConnection();

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
        }, 5000);
    }
*/

    private void doConnection() {
        //final Context appContext = getApplicationContext();

        if (!PPApplication.getApplicationStarted(true, true))
            // application is not started
            return;
/*
        if (Build.VERSION.SDK_INT >= 26) {
            // configured is PPApplication.handlerThreadBroadcast handler (see PhoneProfilesService.registerCallbacks()

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiNetworkCallback_doConnection_1");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                _doConnection(context);

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
        }
        else {*/
            final Context appContext = context;
            //PPApplication.startHandlerThreadBroadcast();
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        appContext) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=WifiNetworkCallback.doConnection");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiNetworkCallback_doConnection_2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        _doConnection(appContext);

                    } catch (Exception e) {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
            }; //);
            //PPApplication.createEventsHandlerExecutor();
            //PPApplication.eventsHandlerExecutor.submit(runnable);
            PPApplication.createDelayedEventsHandlerExecutor();
            PPApplication.delayedEventsHandlerExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
//        }
    }

    private void _doConnection(Context appContext) {
        if (PhoneProfilesService.getInstance() != null) {
            if (PhoneProfilesService.getInstance().connectToSSIDStarted) {
                // connect to SSID is started

                if (connected) {
                    //WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                    //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //if ((PhoneProfilesService.connectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) ||
                    //    (wifiInfo.getSSID().equals(PhoneProfilesService.connectToSSID)))
                    PhoneProfilesService.getInstance().connectToSSIDStarted = false;
                }
            }
        }

        if (Event.getGlobalEventsRunning()) {
            //if ((info.getState() == NetworkInfo.State.CONNECTED) ||
            //        (info.getState() == NetworkInfo.State.DISCONNECTED)) {
            if (!(ApplicationPreferences.prefEventWifiScanRequest ||
                    ApplicationPreferences.prefEventWifiWaitForResult ||
                    ApplicationPreferences.prefEventWifiEnabledForScan)) {
                // wifi is not scanned

                if ((PhoneProfilesService.getInstance() != null) && (!PhoneProfilesService.getInstance().connectToSSIDStarted)) {
                    // connect to SSID is not started

                    // start events handler

//                                    PPApplication.logE("[EVENTS_HANDLER_CALL] WifiNetworkCallback._doConnection", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

//                                PPApplication.logE("[EVENTS_HANDLER_CALL] WifiNetworkCallback._doConnection", "sensorType=SENSOR_TYPE_WIFI_CONNECTION");
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_CONNECTION);

                }
            } //else
            //}
        }
    }

}
