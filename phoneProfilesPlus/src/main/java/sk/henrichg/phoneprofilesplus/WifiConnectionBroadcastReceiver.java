package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class WifiConnectionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### WifiConnectionBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "WifiConnectionBroadcastReceiver.onReceive", "WifiConnectionBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (intent == null)
            return;

        if (intent.getAction() != null) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                final NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                if ((info != null)/* && (ConnectivityManager.TYPE_WIFI == info.getType ())*/) {

                    PPApplication.startHandlerThread("WifiConnectionBroadcastReceiver.onReceive");
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

                                PPApplication.logE("PPApplication.startHandlerThread", "START run - from=WifiConnectionBroadcastReceiver.onReceive");

                                boolean isConnected;
                                if (Build.VERSION.SDK_INT < 28)
                                    isConnected = info.getState() == NetworkInfo.State.CONNECTED;
                                else
                                    isConnected = info.isConnected();

                                PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "isConnected=" + isConnected);

                                if (PhoneProfilesService.getInstance() != null) {
                                    if (PhoneProfilesService.getInstance().connectToSSIDStarted) {
                                        // connect to SSID is started

                                        if (isConnected) {
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

                                if (Event.getGlobalEventsRunning(appContext)) {
                                    //if ((info.getState() == NetworkInfo.State.CONNECTED) ||
                                    //        (info.getState() == NetworkInfo.State.DISCONNECTED)) {
                                        if (!(WifiScanWorker.getScanRequest(appContext) ||
                                                WifiScanWorker.getWaitForResults(appContext) ||
                                                WifiScanWorker.getWifiEnabledForScan(appContext))) {
                                            // wifi is not scanned

                                            PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "wifi is not scanned");

                                            if ((PhoneProfilesService.getInstance() != null) && (!PhoneProfilesService.getInstance().connectToSSIDStarted)) {
                                                // connect to SSID is not started

                                                PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "start HandleEvents - SENSOR_TYPE_WIFI_CONNECTION");

                                                // start events handler
                                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_CONNECTION);

                                            }
                                        } else
                                            PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "wifi is scanned");
                                    //}
                                }

                                PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiConnectionBroadcastReceiver.onReceive");
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    });
                }
            }
        }
    }

}
