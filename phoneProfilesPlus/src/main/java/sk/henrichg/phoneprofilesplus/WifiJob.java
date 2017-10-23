package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.List;

class WifiJob extends Job {

    static final String JOB_TAG  = "WifiJob";

    private static final String EXTRA_ACTION = "action";
    
    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        final Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "WifiJob.onRunJob", "WifiJob_onRunJob");

        Bundle bundle = params.getTransientExtras();
        String action = bundle.getString(EXTRA_ACTION, "");
        CallsCounter.logCounterNoInc(appContext, "WifiJob.onRunJob->action="+action, "WifiJob_onRunJob");

        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            // WifiConnectionBroadcastReceiver

            NetworkInfo info = bundle.getParcelable(WifiManager.EXTRA_NETWORK_INFO);

            if (info != null) {
                PPApplication.logE("$$$ WifiJob.onRunJob", "WifiConnectionBroadcastReceiver: state=" + info.getState());

                if (PhoneProfilesService.connectToSSIDStarted) {
                    // connect to SSID is started

                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        //WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                        //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        //PPApplication.logE("$$$ WifiJob.onRunJob", "WifiConnectionBroadcastReceiver: wifiInfo.getSSID()=" + wifiInfo.getSSID());
                        //PPApplication.logE("$$$ WifiJob.onRunJob", "WifiConnectionBroadcastReceiver: PhoneProfilesService.connectToSSID=" + PhoneProfilesService.connectToSSID);
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

                            PPApplication.logE("$$$ WifiJob.onRunJob", "WifiConnectionBroadcastReceiver: wifi is not scanned");

                            if (!PhoneProfilesService.connectToSSIDStarted) {
                                // connect to SSID is not started

                                // start events handler
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_CONNECTION, false);

                            }
                        } else
                            PPApplication.logE("$$$ WifiJob.onRunJob", "WifiConnectionBroadcastReceiver: wifi is scanned");
                    }
                }
            }
        }
        else
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            // WifiStateChangedBroadcastReceiver

            if (WifiScanJob.wifi == null)
                WifiScanJob.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

            int wifiState = bundle.getInt(WifiManager.EXTRA_WIFI_STATE, 0);

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

            int forceOneScan = Scanner.getForceOneWifiScan(appContext);
            PPApplication.logE("$$$ WifiJob.onRunJob", "WifiStateChangedBroadcastReceiver: forceOneScan="+forceOneScan);

            if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == Scanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
            {
                PPApplication.logE("$$$ WifiJob.onRunJob","WifiStateChangedBroadcastReceiver: state="+wifiState);

                if ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_DISABLED))
                {
                    if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                        // start scan
                        if (WifiScanJob.getScanRequest(appContext)) {
                            //final Context _context = appContext;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    PPApplication.logE("$$$ WifiJob.onRunJob", "WifiStateChangedBroadcastReceiver: startScan");
                                    WifiScanJob.startScan(getContext());
                                }
                            }, 5000);

                            /*
                            PPApplication.logE("$$$ WifiJob.onRunJob", "WifiStateChangedBroadcastReceiver: before startScan");
                            PPApplication.sleep(5000);
                            WifiScanJobBroadcastReceiver.startScan(appContext);
                            PPApplication.logE("$$$ WifiJob.onRunJob", "WifiStateChangedBroadcastReceiver: after startScan");
                            */

                        } else if (!WifiScanJob.getWaitForResults(appContext)) {
                            // refresh configured networks list
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    PPApplication.logE("$$$ WifiJob.onRunJob", "WifiStateChangedBroadcastReceiver: startScan");
                                    WifiScanJob.fillWifiConfigurationList(appContext);
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
        }
        else
        if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            // WifiScanBroadcastReceiver

            if (WifiScanJob.wifi == null)
                WifiScanJob.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

            int forceOneScan = Scanner.getForceOneWifiScan(appContext);
            PPApplication.logE("%%%% WifiJob.onRunJob", "WifiScanBroadcastReceiver: forceOneScan="+forceOneScan);

            if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == Scanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
            {
                boolean scanStarted = (WifiScanJob.getWaitForResults(appContext));
                PPApplication.logE("%%%% WifiJob.onRunJob", "WifiScanBroadcastReceiver: scanStarted="+scanStarted);

                //boolean isWifiAPEnabled = WifiApManager.isWifiAPEnabled(context);
                //PPApplication.logE("$$$ WifiJob.onRunJob", "WifiScanBroadcastReceiver: isWifiAPEnabled="+isWifiAPEnabled);

                //PPApplication.logE("%%%% WifiJob.onRunJob", "WifiScanBroadcastReceiver: resultsUpdated="+intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false));

                //if ((android.os.Build.VERSION.SDK_INT < 23) || (intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)))
                WifiScanJob.fillScanResults(appContext);
                //WifiScanJobBroadcastReceiver.unlock();

                List<WifiSSIDData> scanResults = WifiScanJob.getScanResults(appContext);
                if (scanResults != null) {
                    //PPApplication.logE("$$$ WifiJob.onRunJob", "WifiScanBroadcastReceiver: scanResults.size="+scanResults.size());
                    for (WifiSSIDData result : scanResults) {
                        PPApplication.logE("$$$ WifiJob.onRunJob", "WifiScanBroadcastReceiver: result.SSID=" + result.ssid);
                    }
                }
                else
                    PPApplication.logE("$$$ WifiJob.onRunJob", "WifiScanBroadcastReceiver: scanResults=null");

                if (scanStarted)
                {
                    WifiScanJob.setWaitForResults(appContext, false);
                    Scanner.setForceOneWifiScan(appContext, Scanner.FORCE_ONE_SCAN_DISABLED);

                    if (forceOneScan != Scanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                    {
                        PPApplication.logE("$$$ WifiJob.onRunJob", "WifiScanBroadcastReceiver: start EventsHandlerJob (1)");
                        // start job
                        new Handler(appContext.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                PPApplication.logE("$$$ WifiJob.onRunJob", "WifiScanBroadcastReceiver: start EventsHandlerJob (2)");
                                EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_WIFI_SCANNER);
                            }
                        }, 5000);
                    }
                }

            }
        }

        return Result.SUCCESS;
    }
    
    static void startForConnectionBroadcast(Context context, Parcelable networkInfo) {
        final JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION);
        bundle.putParcelable(WifiManager.EXTRA_NETWORK_INFO, networkInfo);

        final Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    jobBuilder
                            .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                            .setTransientExtras(bundle)
                            .startNow()
                            .build()
                            .schedule();
                } catch (Exception ignored) { }
            }
        });
    }

    static void startForStateChangedBroadcast(Context context, int wifiState) {
        final JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION);
        bundle.putInt(WifiManager.EXTRA_WIFI_STATE, wifiState);

        final Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    jobBuilder
                            .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                            .setTransientExtras(bundle)
                            .startNow()
                            .build()
                            .schedule();
                } catch (Exception ignored) { }
            }
        });
    }

    static void startForScanBroadcast(Context context) {
        final JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ACTION, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        final Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    jobBuilder
                            .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                            .setTransientExtras(bundle)
                            .startNow()
                            .build()
                            .schedule();
                } catch (Exception ignored) { }
            }
        });
    }
    
    @TargetApi(Build.VERSION_CODES.M)
    static void startForScanBroadcast(Context context, boolean resultsUpdated) {
        final JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ACTION, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        bundle.putBoolean(WifiManager.EXTRA_RESULTS_UPDATED, resultsUpdated);

        final Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    jobBuilder
                            .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                            .setTransientExtras(bundle)
                            .startNow()
                            .build()
                            .schedule();
                } catch (Exception ignored) { }
            }
        });
    }

}
