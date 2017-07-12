package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.List;

public class WifiStateChangedBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "wifiState";

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
                        new Handler().postDelayed(new Runnable() {
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
                        new Handler().post(new Runnable() {
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

                    /*Intent broadcastIntent = new Intent(appContext, RadioSwitchBroadcastReceiver.class);
                    broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_WIFI);
                    broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, wifiState == WifiManager.WIFI_STATE_ENABLED);
                    appContext.sendBroadcast(broadcastIntent);*/
                    LocalBroadcastManager.getInstance(appContext).registerReceiver(PPApplication.radioSwitchBroadcastReceiver, new IntentFilter("RadioSwitchBroadcastReceiver"));
                    Intent broadcastIntent = new Intent("RadioSwitchBroadcastReceiver");
                    broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_WIFI);
                    broadcastIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, wifiState == WifiManager.WIFI_STATE_ENABLED);
                    LocalBroadcastManager.getInstance(appContext).sendBroadcast(broadcastIntent);

                    // start service
                    Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                    eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                    startWakefulService(appContext, eventsServiceIntent);
                }
            }
        }

    }

}
