package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;

import java.util.List;

import static android.content.Context.POWER_SERVICE;

public class WifiStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### WifiStateChangedBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "WifiStateChangedBroadcastReceiver.onReceive", "WifiStateChangedBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (intent == null)
            return;

        //WifiJob.startForStateChangedBroadcast(context.getApplicationContext(), intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0));

        if (intent.getAction() != null) {
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                // WifiStateChangedBroadcastReceiver

                if (WifiScanJob.wifi == null)
                    WifiScanJob.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

                final int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

                final Handler handler = new Handler(appContext.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WifiStateChangedBroadcastReceiver.onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                            if (!((WifiScanJob.getScanRequest(appContext)) ||
                                    (WifiScanJob.getWaitForResults(appContext)) ||
                                    (WifiScanJob.getWifiEnabledForScan(appContext)))) {
                                // ignore for wifi scanning

                                if (!PhoneProfilesService.connectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) {
                                    WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                                    if (wifiManager != null) {
                                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                                        if (list != null) {
                                            for (WifiConfiguration i : list) {
                                                if (i.SSID != null && i.SSID.equals(PhoneProfilesService.connectToSSID)) {
                                                    //wifiManager.disconnect();
                                                    wifiManager.enableNetwork(i.networkId, true);
                                                    //wifiManager.reconnect();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                //else {
                                //    WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                                //    wifiManager.disconnect();
                                //    wifiManager.reconnect();
                                //}
                            }
                        }

                        int forceOneScan = Scanner.getForceOneWifiScan(appContext);
                        PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "forceOneScan=" + forceOneScan);

                        if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == Scanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)) {
                            PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "state=" + wifiState);

                            if ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_DISABLED)) {
                                if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                                    // start scan
                                    if (WifiScanJob.getScanRequest(appContext)) {
                                        //final Context _context = appContext;
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                                PowerManager.WakeLock wakeLock = null;
                                                if (powerManager != null) {
                                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WifiStateChangedBroadcastReceiver.onReceive.Handler.postDelayed.1");
                                                    wakeLock.acquire(10 * 60 * 1000);
                                                }

                                                PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "startScan");
                                                WifiScanJob.startScan(appContext);

                                                if (wakeLock != null)
                                                    wakeLock.release();
                                            }
                                        }, 5000);

                            /*
                            PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "before startScan");
                            PPApplication.sleep(5000);
                            WifiScanJobBroadcastReceiver.startScan(appContext);
                            PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "after startScan");
                            */

                                    } else if (!WifiScanJob.getWaitForResults(appContext)) {
                                        // refresh configured networks list
                                        new Handler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                                PowerManager.WakeLock wakeLock = null;
                                                if (powerManager != null) {
                                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WifiStateChangedBroadcastReceiver.onReceive.Handler.post.2");
                                                    wakeLock.acquire(10 * 60 * 1000);
                                                }

                                                PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "startScan");
                                                WifiScanJob.fillWifiConfigurationList(appContext);

                                                if (wakeLock != null)
                                                    wakeLock.release();
                                            }
                                        });
                                    }
                                }

                                if (!((WifiScanJob.getScanRequest(appContext)) ||
                                        (WifiScanJob.getWaitForResults(appContext)) ||
                                        (WifiScanJob.getWifiEnabledForScan(appContext)))) {
                                    // required for Wifi ConnectionType="Not connected"

                                    // start events handler
                                    EventsHandler eventsHandler = new EventsHandler(appContext);
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH, false);

                                    // start events handler
                                    eventsHandler = new EventsHandler(appContext);
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_STATE, false);
                                }
                            }
                        }

                        if (wakeLock != null)
                            wakeLock.release();
                    }
                });
            }
        }

    }

}
