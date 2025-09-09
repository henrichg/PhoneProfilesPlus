package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import java.util.List;

public class WifiStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] ----------- WifiStateChangedBroadcastReceiver.onReceive", "xxx");
//        Log.e("[IN_BROADCAST] ----------- WifiStateChangedBroadcastReceiver.onReceive", "xxx");

        final Context appContext = context.getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (intent == null)
            return;

        if (intent.getAction() != null) {
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                // WifiStateChangedBroadcastReceiver

                //if (WifiScanWorker.wifi == null)
                //    WifiScanWorker.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

                final int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

                @SuppressLint("MissingPermission")
                Runnable __runnable = () -> {
//                  PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=WifiStateChangedBroadcastReceiver.onReceive.1");

//                    Log.e("[IN_BROADCAST] ----------- WifiStateChangedBroadcastReceiver.onReceive", "(1)");

                    //Context appContext= appContextWeakRef.get();

                    //if (appContext != null) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_WifiStateChangedBroadcastReceiver_onReceive);
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {

                                WifiScanWorker.fillWifiConfigurationList(appContext/*, false*/);

                                if (!(ApplicationPreferences.prefEventWifiScanRequest ||
                                        ApplicationPreferences.prefEventWifiWaitForResult ||
                                        ApplicationPreferences.prefEventWifiEnabledForScan)) {
                                    // ignore for wifi scanning

                                    if (PhoneProfilesService.getInstance() != null) {
                                        if (!PPApplication.connectToSSID.equals(StringConstants.CONNECTTOSSID_JUSTANY)) {
                                            WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                                            if (wifiManager != null) {
                                                List<WifiConfiguration> list = null;
                                                if (Permissions.hasPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION))
                                                    list = wifiManager.getConfiguredNetworks();
                                                if (list != null) {
                                                    for (WifiConfiguration i : list) {
                                                        //noinspection deprecation
                                                        if (i.SSID != null && i.SSID.equals(PPApplication.connectToSSID)) {
                                                            //wifiManager.disconnect();
                                                            //noinspection deprecation
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
                            }

                        } catch (Exception e) {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    //}

                    handleEvents(appContext, wifiState);
                };
                PPApplicationStatic.createEventsHandlerExecutor();
                PPApplication.basicExecutorPool.submit(__runnable);
            }
        }

    }

    private void handleEvents(final Context appContext, final int wifiState) {
        Runnable __runnable = () -> {
//            Log.e("[IN_BROADCAST] ----------- WifiStateChangedBroadcastReceiver.handleEvents", "(2.1)");

            synchronized (PPApplication.handleEventsMutex) {

//                Log.e("[IN_BROADCAST] ----------- WifiStateChangedBroadcastReceiver.handleEvents", "(2.2)");

                int forceOneScan = ApplicationPreferences.prefForceOneWifiScan;

                if (EventStatic.getGlobalEventsRunning(appContext) || (forceOneScan == WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)) {

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_WifiStateChangedBroadcastReceiver_onReceive);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_DISABLED)) {
                            /*if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                                // start scan
                                if (ApplicationPreferences.prefEventWifiScanRequest) {
//                                            PPApplicationStatic.logE("[EXECUTOR_CALL]  ***** WifiStateChangedBroadcastReceiver.onReceive", "schedule");

                                    //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
                                    Runnable runnable = () -> {
//                                                long start = System.currentTimeMillis();
//                                                PPApplicationStatic.logE("[IN_EXECUTOR]  ***** WifiStateChangedBroadcastReceiver.onReceive", "--------------- START");
                                        WifiScanWorker.startScan(appContext);
//                                                long finish = System.currentTimeMillis();
//                                                long timeElapsed = finish - start;
//                                                PPApplicationStatic.logE("[IN_EXECUTOR]  ***** WifiStateChangedBroadcastReceiver.onReceive", "--------------- END - timeElapsed="+timeElapsed);
                                        //worker.shutdown();
                                    };
                                    PPApplicationStatic.createDelayedEventsHandlerExecutor();
                                    PPApplication.delayedEventsHandlerExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
                                }
                            }*/

                            if (ApplicationPreferences.prefEventWifiScanRequest ||
                                    ApplicationPreferences.prefEventWifiWaitForResult ||
                                    ApplicationPreferences.prefEventWifiEnabledForScan)
                                PhoneProfilesServiceStatic.cancelWifiWorker(appContext, true, false);

                            // start events handler

//                                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] WifiStateChangedBroadcastReceiver.onReceive", "SENSOR_TYPE_RADIO_SWITCH,SENSOR_TYPE_WIFI_STATE,SENSOR_TYPE_WIFI_CONNECTION");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(new int[]{
                                    EventsHandler.SENSOR_TYPE_RADIO_SWITCH,
                                    EventsHandler.SENSOR_TYPE_WIFI_STATE,
                                    EventsHandler.SENSOR_TYPE_WIFI_CONNECTION});

//                                    PPApplicationStatic.logE("[RESTART_WIFI_SCANNER] ----------- WifiStateChangedBroadcastReceiver", "xxx");
                            PPApplicationStatic.restartWifiScanner(appContext);
                        }

                    } catch (Exception e) {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }

                }
            }
        };
//        PPApplicationStatic.logE("[EXECUTOR_CALL] WifiStateChangedBroadcastReceiver.handleEvents", "(xxx");
        PPApplicationStatic.createEventsHandlerExecutor();
        PPApplication.eventsHandlerExecutor.submit(__runnable);
    }

}
