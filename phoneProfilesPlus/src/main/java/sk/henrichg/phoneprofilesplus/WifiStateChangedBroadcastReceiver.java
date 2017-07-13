package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.util.List;

public class WifiStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### WifiStateChangedBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        if (WifiScanJob.wifi == null)
            WifiScanJob.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(appContext);

        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
            if (!((WifiScanJob.getScanRequest(appContext)) ||
                    (WifiScanJob.getWaitForResults(appContext)) ||
                    (WifiScanJob.getWifiEnabledForScan(appContext)))) {
                // ignore for wifi scanning

                if (!PhoneProfilesService.connectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) {
                    WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
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
                //else {
                //    WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                //    wifiManager.disconnect();
                //    wifiManager.reconnect();
                //}
            }
        }

        int forceOneScan = ScannerService.getForceOneWifiScan(appContext);
        PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "forceOneScan="+forceOneScan);

        if (Event.getGlobalEventsRuning(appContext) || (forceOneScan == ScannerService.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {
            PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive","state="+wifiState);

            if ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_DISABLED))
            {
                if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    // start scan
                    if (WifiScanJob.getScanRequest(appContext)) {
                        final Context _context = appContext;
                        new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "startScan");
                                WifiScanJob.startScan(_context);
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
                        final Context _context = appContext;
                        new Handler(context.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "startScan");
                                WifiScanJob.fillWifiConfigurationList(_context);
                            }
                        });
                    }
                }

                if (!((WifiScanJob.getScanRequest(appContext)) ||
                        (WifiScanJob.getWaitForResults(appContext)) ||
                        (WifiScanJob.getWifiEnabledForScan(appContext)))) {
                    // required for Wifi ConnectionType="Not connected"

                    Intent eventsServiceIntent = new Intent(context, EventsService.class);
                    eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_RADIO_SWITCH);
                    eventsServiceIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_WIFI);
                    eventsServiceIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, wifiState == WifiManager.WIFI_STATE_ENABLED);
                    WakefulIntentService.sendWakefulWork(context, eventsServiceIntent);

                    // start service
                    Intent eventsServiceIntent2 = new Intent(appContext, EventsService.class);
                    eventsServiceIntent2.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_WIFI_STATE);
                    WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent2);
                }
            }
        }

    }

}
