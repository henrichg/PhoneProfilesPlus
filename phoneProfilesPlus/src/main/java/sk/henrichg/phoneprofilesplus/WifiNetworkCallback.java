package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

@SuppressWarnings("WeakerAccess")
public class WifiNetworkCallback extends ConnectivityManager.NetworkCallback {

    private final Context appContext;

    static boolean connected = false;

    WifiNetworkCallback(Context context) {
        appContext = context.getApplicationContext();
    }

    @Override
    public void onLost(Network network) {
        //record wi-fi disconnect event
//        PPApplication.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onLost", "xxx");
        connected = false;
        doConnection();
    }

    @Override
    public void onUnavailable() {
//        PPApplication.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onUnavailable", "xxx");
        //connected = false;
        doConnection();
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
//        PPApplication.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onLosing", "xxx");
        doConnection();
    }

    @Override
    public void onAvailable(Network network) {
        //record wi-fi connect event
//        PPApplication.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onAvailable", "xxx");
        connected = true;
        doConnection();
    }

    private void doConnection() {
//        PPApplication.logE("[TEST BATTERY] PPWifiNetworkCallback.doConnection", "xxx");
//        PPApplication.logE("[TEST BATTERY] PPWifiNetworkCallback.doConnection", "current thread="+Thread.currentThread());

        //CallsCounter.logCounter(context, "PPWifiNetworkCallback.doConnection", "PPWifiNetworkCallback_doConnection");

        //final Context appContext = getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        //PPApplication.logE("[TEST BATTERY] PPWifiNetworkCallback.doConnection", "connected or disconnected");

        if (Build.VERSION.SDK_INT >= 26) {
            // configured is PPApplication.handlerThreadBroadcast handler (see PhoneProfilesService.registerCallbacks()
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiNetworkCallback_doConnection");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                _doConnection();

//               PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPWifiNetworkCallback.doConnection");

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
        else {
            PPApplication.startHandlerThreadBroadcast(/*"WifiConnectionBroadcastReceiver.onReceive"*/);
            final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=WifiNetworkCallback.doConnection");

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiNetworkCallback_doConnection");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    _doConnection();

//                    PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPWifiNetworkCallback.doConnection");

                } catch (Exception e) {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                }
            });
        }
    }

    private void _doConnection() {
//        PPApplication.logE("$$$ PPWifiNetworkCallback._doConnection", "isConnected=" + connected);

        if (PhoneProfilesService.getInstance() != null) {
            if (PhoneProfilesService.getInstance().connectToSSIDStarted) {
                // connect to SSID is started

                if (connected) {
                    //WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                    //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //PPApplication.logE("$$$ PPWifiNetworkCallback._doConnection", "wifiInfo.getSSID()=" + wifiInfo.getSSID());
                    //PPApplication.logE("$$$ PPWifiNetworkCallback._doConnection", "PhoneProfilesService.connectToSSID=" + PhoneProfilesService.connectToSSID);
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

                //PPApplication.logE("$$$ PPWifiNetworkCallback._doConnection", "wifi is not scanned");

                if ((PhoneProfilesService.getInstance() != null) && (!PhoneProfilesService.getInstance().connectToSSIDStarted)) {
                    // connect to SSID is not started

                    //PPApplication.logE("$$$ PPWifiNetworkCallback._doConnection", "start HandleEvents - SENSOR_TYPE_WIFI_CONNECTION");

                    // start events handler
                    //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=PPWifiNetworkCallback._doConnection");

//                                PPApplication.logE("[EVENTS_HANDLER_CALL] WifiNetworkCallback._doConnection", "sensorType=SENSOR_TYPE_WIFI_CONNECTION");
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_CONNECTION);

                    //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PPWifiNetworkCallback._doConnection");
                }
            } //else
            //PPApplication.logE("$$$ PPWifiNetworkCallback._doConnection", "wifi is scanned");
            //}
        }
    }

}
