package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Handler;
import android.os.PowerManager;

@SuppressWarnings("WeakerAccess")
public class PPWifiNetworkCallback extends ConnectivityManager.NetworkCallback {

    private final Context appContext;

    static boolean connected = false;

    PPWifiNetworkCallback(Context context) {
        appContext = context.getApplicationContext();
    }

    @Override
    public void onLost(Network network) {
        //record wi-fi disconnect event
        PPApplication.logE("PPWifiNetworkCallback.onLost", "xxx");
        connected = false;
        doConnection();
    }

    @Override
    public void onUnavailable() {
        PPApplication.logE("PPWifiNetworkCallback.onUnavailable", "xxx");
        connected = false;
        doConnection();
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
        PPApplication.logE("PPWifiNetworkCallback.onLosing", "xxx");
        doConnection();
    }

    @Override
    public void onAvailable(Network network) {
        //record wi-fi connect event
        PPApplication.logE("PPWifiNetworkCallback.onAvailable", "xxx");
        connected = true;
        doConnection();
    }

    private void doConnection() {
        //PPApplication.logE("[TEST BATTERY] WifiConnectionBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "WifiConnectionBroadcastReceiver.onReceive", "WifiConnectionBroadcastReceiver_onReceive");

        //final Context appContext = getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        //PPApplication.logE("[TEST BATTERY] WifiConnectionBroadcastReceiver.onReceive", "connected or disconnected");

        PPApplication.startHandlerThread(/*"WifiConnectionBroadcastReceiver.onReceive"*/);
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
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

                    //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=WifiConnectionBroadcastReceiver.onReceive");

                    //PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "isConnected=" + isConnected);

                    if (PhoneProfilesService.getInstance() != null) {
                        if (PhoneProfilesService.getInstance().connectToSSIDStarted) {
                            // connect to SSID is started

                            if (connected) {
                                //WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                                //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                //PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "wifiInfo.getSSID()=" + wifiInfo.getSSID());
                                //PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "PhoneProfilesService.connectToSSID=" + PhoneProfilesService.connectToSSID);
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

                            //PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "wifi is not scanned");

                            if ((PhoneProfilesService.getInstance() != null) && (!PhoneProfilesService.getInstance().connectToSSIDStarted)) {
                                // connect to SSID is not started

                                //PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "start HandleEvents - SENSOR_TYPE_WIFI_CONNECTION");

                                // start events handler
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_CONNECTION);

                            }
                        } //else
                        //PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "wifi is scanned");
                        //}
                    }

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiConnectionBroadcastReceiver.onReceive");
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
