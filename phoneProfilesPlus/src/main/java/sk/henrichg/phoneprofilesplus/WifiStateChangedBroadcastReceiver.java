package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.List;

public class WifiStateChangedBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "wifiState";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### WifiStateChangedBroadcastReceiver.onReceive", "xxx");

        if (WifiScanAlarmBroadcastReceiver.wifi == null)
            WifiScanAlarmBroadcastReceiver.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.loadPreferences(context);

        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
            if (!((WifiScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                    (WifiScanAlarmBroadcastReceiver.getWaitForResults(context)) ||
                    (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context)))) {
                // ignore for wifi scanning

                if (!PhoneProfilesService.connectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                    for (WifiConfiguration i : list) {
                        if (i.SSID != null && i.SSID.equals(PhoneProfilesService.connectToSSID)) {
                            //wifiManager.disconnect();
                            wifiManager.enableNetwork(i.networkId, true);
                            //wifiManager.reconnect();
                            break;
                        }
                    }
                }
                //else {
                //    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                //    wifiManager.disconnect();
                //    wifiManager.reconnect();
                //}
            }
        }

        int forceOneScan = PPApplication.getForceOneWifiScan(context);
        PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "forceOneScan="+forceOneScan);

        if (PPApplication.getGlobalEventsRuning(context) || (forceOneScan == PPApplication.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {
            PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive","state="+wifiState);

            if ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_DISABLED))
            {
                if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    // start scan
                    if (WifiScanAlarmBroadcastReceiver.getScanRequest(context)) {
                        PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "before startScan");
                        PPApplication.sleep(1000);
                        WifiScanAlarmBroadcastReceiver.startScan(context.getApplicationContext());
                        PPApplication.logE("$$$ WifiStateChangedBroadcastReceiver.onReceive", "after startScan");
                    } else if (!WifiScanAlarmBroadcastReceiver.getWaitForResults(context)) {
                        // refresh configured networks list
                        WifiScanAlarmBroadcastReceiver.fillWifiConfigurationList(context);
                    }
                }

                if (!((WifiScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                        (WifiScanAlarmBroadcastReceiver.getWaitForResults(context)) ||
                        (WifiScanAlarmBroadcastReceiver.getWifiEnabledForScan(context)))) {
                    // required for Wifi ConnectionType="Not connected"

                    Intent broadcastIntent = new Intent(context, RadioSwitchBroadcastReceiver.class);
                    broadcastIntent.putExtra(PPApplication.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_WIFI);
                    broadcastIntent.putExtra(PPApplication.EXTRA_EVENT_RADIO_SWITCH_STATE, wifiState == WifiManager.WIFI_STATE_ENABLED);
                    context.sendBroadcast(broadcastIntent);

                    // start service
                    Intent eventsServiceIntent = new Intent(context, EventsService.class);
                    eventsServiceIntent.putExtra(PPApplication.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                    startWakefulService(context, eventsServiceIntent);
                }
            }
        }

    }
}
