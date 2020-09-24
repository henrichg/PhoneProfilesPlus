package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
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
        PPApplication.logE("[LISTENER CALL] ----------- WifiNetworkCallback.onLost", "xxx");
        connected = false;
        doConnection();
    }

    @Override
    public void onUnavailable() {
        PPApplication.logE("[LISTENER CALL] ----------- WifiNetworkCallback.onUnavailable", "xxx");
        connected = false;
        doConnection();
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
        PPApplication.logE("[LISTENER CALL] ----------- WifiNetworkCallback.onLosing", "xxx");
        doConnection();
    }

    @Override
    public void onAvailable(Network network) {
        //record wi-fi connect event
        PPApplication.logE("[LISTENER CALL] ----------- WifiNetworkCallback.onAvailable", "xxx");
        connected = true;
        doConnection();
    }

    private void doConnection() {
        //PPApplication.logE("[TEST BATTERY] PPWifiNetworkCallback.doConnection", "xxx");
        //CallsCounter.logCounter(context, "PPWifiNetworkCallback.doConnection", "PPWifiNetworkCallback_doConnection");

        //final Context appContext = getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        //PPApplication.logE("[TEST BATTERY] PPWifiNetworkCallback.doConnection", "connected or disconnected");

        PPApplication.startHandlerThreadBroadcast(/*"WifiConnectionBroadcastReceiver.onReceive"*/);
        final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiConnectionBroadcastReceiver_onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

//                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=WifiNetworkCallback.doConnection");

                    //PPApplication.logE("$$$ PPWifiNetworkCallback.doConnection", "isConnected=" + isConnected);

                    if (PhoneProfilesService.getInstance() != null) {
                        if (PhoneProfilesService.getInstance().connectToSSIDStarted) {
                            // connect to SSID is started

                            if (connected) {
                                //WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                                //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                //PPApplication.logE("$$$ PPWifiNetworkCallback.doConnection", "wifiInfo.getSSID()=" + wifiInfo.getSSID());
                                //PPApplication.logE("$$$ PPWifiNetworkCallback.doConnection", "PhoneProfilesService.connectToSSID=" + PhoneProfilesService.connectToSSID);
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

                            //PPApplication.logE("$$$ PPWifiNetworkCallback.doConnection", "wifi is not scanned");

                            if ((PhoneProfilesService.getInstance() != null) && (!PhoneProfilesService.getInstance().connectToSSIDStarted)) {
                                // connect to SSID is not started

                                //PPApplication.logE("$$$ PPWifiNetworkCallback.doConnection", "start HandleEvents - SENSOR_TYPE_WIFI_CONNECTION");

                                // start events handler
                                //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=PPWifiNetworkCallback.doConnection");

//                                PPApplication.logE("[EVENTS_HANDLER] WifiNetworkCallback.doConnection", "sensorType=SENSOR_TYPE_WIFI_CONNECTION");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_CONNECTION);

                                //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PPWifiNetworkCallback.doConnection");
                            }
                        } //else
                        //PPApplication.logE("$$$ PPWifiNetworkCallback.doConnection", "wifi is scanned");
                        //}
                    }

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PPWifiNetworkCallback.doConnection");
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
