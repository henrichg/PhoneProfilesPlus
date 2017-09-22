package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.util.List;

public class WifiService extends WakefulIntentService {

    public WifiService() {
        super("WifiService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        CallsCounter.logCounter(getApplicationContext(), "WifiService.doWakefulWork", "WifiService_doWakefulWork");

        if (intent != null) {

            Context appContext = getApplicationContext();

            String action = intent.getAction();
            CallsCounter.logCounterNoInc(getApplicationContext(), "WifiService.doWakefulWork->action="+action, "WifiService_doWakefulWork");

            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                // WifiConnectionBroadcastReceiver

                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                if (info != null) {
                    PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiConnectionBroadcastReceiver: state=" + info.getState());

                    if (PhoneProfilesService.connectToSSIDStarted) {
                        // connect to SSID is started

                        if (info.getState() == NetworkInfo.State.CONNECTED) {
                            WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                            PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiConnectionBroadcastReceiver: wifiInfo.getSSID()=" + wifiInfo.getSSID());
                            PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiConnectionBroadcastReceiver: PhoneProfilesService.connectToSSID=" + PhoneProfilesService.connectToSSID);
                            //if ((PhoneProfilesService.connectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) ||
                            //    (wifiInfo.getSSID().equals(PhoneProfilesService.connectToSSID)))
                            PhoneProfilesService.connectToSSIDStarted = false;
                        }
                    }

                    if (Event.getGlobalEventsRunning(appContext)) {
                        if ((info.getState() == NetworkInfo.State.CONNECTED) ||
                                (info.getState() == NetworkInfo.State.DISCONNECTED)) {
                            if (!((WifiScanJob.getScanRequest(appContext)) ||
                                    (WifiScanJob.getWaitForResults(appContext)) ||
                                    (WifiScanJob.getWifiEnabledForScan(appContext)))) {
                                // wifi is not scanned

                                PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiConnectionBroadcastReceiver: wifi is not scanned");

                                if (!PhoneProfilesService.connectToSSIDStarted) {
                                    // connect to SSID is not started

                                    // start service
                                    try {
                                        Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                                        eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_WIFI_CONNECTION);
                                        WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
                                    } catch (Exception ignored) {}

                                }
                            } else
                                PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiConnectionBroadcastReceiver: wifi is scanned");
                        }
                    }
                }
            }
            else
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                // WifiStateChangedBroadcastReceiver

                if (WifiScanJob.wifi == null)
                    WifiScanJob.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

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
                PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiStateChangedBroadcastReceiver: forceOneScan="+forceOneScan);

                if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == ScannerService.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
                {
                    PPApplication.logE("$$$ WifiService.doWakefulWork","WifiStateChangedBroadcastReceiver: state="+wifiState);

                    if ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_DISABLED))
                    {
                        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                            // start scan
                            if (WifiScanJob.getScanRequest(appContext)) {
                                final Context _context = appContext;
                                new Handler(appContext.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiStateChangedBroadcastReceiver: startScan");
                                        WifiScanJob.startScan(_context);
                                    }
                                }, 5000);

                            /*
                            PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiStateChangedBroadcastReceiver: before startScan");
                            PPApplication.sleep(5000);
                            WifiScanJobBroadcastReceiver.startScan(appContext);
                            PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiStateChangedBroadcastReceiver: after startScan");
                            */

                            } else if (!WifiScanJob.getWaitForResults(appContext)) {
                                // refresh configured networks list
                                final Context _context = appContext;
                                new Handler(appContext.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiStateChangedBroadcastReceiver: startScan");
                                        WifiScanJob.fillWifiConfigurationList(_context);
                                    }
                                });
                            }
                        }

                        if (!((WifiScanJob.getScanRequest(appContext)) ||
                                (WifiScanJob.getWaitForResults(appContext)) ||
                                (WifiScanJob.getWifiEnabledForScan(appContext)))) {
                            // required for Wifi ConnectionType="Not connected"

                            try {
                                Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
                                eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_RADIO_SWITCH);
                                eventsServiceIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_TYPE, EventPreferencesRadioSwitch.RADIO_TYPE_WIFI);
                                eventsServiceIntent.putExtra(EventsService.EXTRA_EVENT_RADIO_SWITCH_STATE, wifiState == WifiManager.WIFI_STATE_ENABLED);
                                WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent);
                            } catch (Exception ignored) {}

                            // start service
                            try {
                                Intent eventsServiceIntent2 = new Intent(appContext, EventsService.class);
                                eventsServiceIntent2.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_WIFI_STATE);
                                WakefulIntentService.sendWakefulWork(appContext, eventsServiceIntent2);
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
            else
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                // WifiScanBroadcastReceiver

                if (WifiScanJob.wifi == null)
                    WifiScanJob.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

                int forceOneScan = ScannerService.getForceOneWifiScan(appContext);
                PPApplication.logE("%%%% WifiService.doWakefulWork", "WifiScanBroadcastReceiver: forceOneScan="+forceOneScan);

                if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == ScannerService.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
                {
                    boolean scanStarted = (WifiScanJob.getWaitForResults(appContext));
                    PPApplication.logE("%%%% WifiService.doWakefulWork", "WifiScanBroadcastReceiver: scanStarted="+scanStarted);

                    //boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
                    //PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiScanBroadcastReceiver: isWifiAPEnabled="+isWifiAPEnabled);

                    //PPApplication.logE("%%%% WifiService.doWakefulWork", "WifiScanBroadcastReceiver: resultsUpdated="+intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false));

                    //if ((android.os.Build.VERSION.SDK_INT < 23) || (intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)))
                    WifiScanJob.fillScanResults(appContext);
                    //WifiScanJobBroadcastReceiver.unlock();

                    /*
                    List<WifiSSIDData> scanResults = WifiScanJobBroadcastReceiver.getScanResults(context);
                    if (scanResults != null) {
                        PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiScanBroadcastReceiver: scanResults.size="+scanResults.size());
                        //for (WifiSSIDData result : scanResults) {
                        //    PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiScanBroadcastReceiver: result.SSID=" + result.ssid);
                        //}
                    }
                    else
                        PPApplication.logE("$$$ WifiService.doWakefulWork", "WifiScanBroadcastReceiver: scanResults=null");
                    */

                    if (scanStarted)
                    {
                        WifiScanJob.setWaitForResults(appContext, false);
                        ScannerService.setForceOneWifiScan(appContext, ScannerService.FORCE_ONE_SCAN_DISABLED);

                        if (forceOneScan != ScannerService.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                        {
                            // start service
                            final Context _context = appContext;
                            new Handler(appContext.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Intent eventsServiceIntent = new Intent(_context, EventsService.class);
                                        eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, EventsService.SENSOR_TYPE_WIFI_SCANNER);
                                        WakefulIntentService.sendWakefulWork(_context, eventsServiceIntent);
                                    } catch (Exception ignored) {}
                                }
                            }, 5000);
                        }
                    }

                }
            }
        }
        else
            CallsCounter.logCounterNoInc(getApplicationContext(), "WifiService.doWakefulWork->intent=null", "WifiService_doWakefulWork");
    }

}
