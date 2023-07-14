package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class WifiNetworkCallback extends ConnectivityManager.NetworkCallback {

    //private final Context context;

    WifiNetworkCallback(/*Context context*/) {
        //this.context = context.getApplicationContext();
    }

    @Override
    public void onLost(Network network) {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onLost", "xxx");
        PPApplication.wifiNetworkconnected = false;
        doConnection();
    }

    @Override
    public void onUnavailable() {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onUnavailable", "xxx");
        PPApplication.wifiNetworkconnected = false;
        doConnection();
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onLosing", "xxx");
        doConnection();
    }

    @Override
    public void onAvailable(Network network) {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onAvailable", "xxx");
        PPApplication.wifiNetworkconnected = true;
        doConnection();
    }

    @Override
    public void onCapabilitiesChanged (Network network, NetworkCapabilities networkCapabilities) {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onCapabilitiesChanged", "xxx");
        doConnection();
    }

/*
    private void doConnection() {

        //final Context appContext = getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true))
            // application is not started
            return;

        PPApplication.startHandlerThreadBroadcast();
        final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        handler.postDelayed(() -> {
//            PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=WifiNetworkCallback.doConnection");

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiNetworkCallback_doConnection");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                _doConnection();

            } catch (Exception e) {
//                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
            }
        }, 5000);
    }
*/

    private void doConnection() {
        //final Context appContext = getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;
/*
        if (Build.VERSION.SDK_INT >= 26) {
            // configured is PPApplication.handlerThreadBroadcast handler (see PhoneProfilesService.registerCallbacks()

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiNetworkCallback_doConnection_1");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                _doConnection(context);

            } catch (Exception e) {
//                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        else {*/
            // !!! must be used MainWorker with delay ans REPLACE, because is often called this onChange
            // for change volumes
                /*Data workData = new Data.Builder()
                        .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_VOLUMES)
                        .build();*/

        OneTimeWorkRequest worker =
                new OneTimeWorkRequest.Builder(MainWorker.class)
                        .addTag(MainWorker.HANDLE_EVENTS_WIFI_NETWORK_CALLBACK_WORK_TAG)
                        //.setInputData(workData)
                        .setInitialDelay(5, TimeUnit.SECONDS)
                        //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                        .build();
        try {
//            if (PPApplicationStatic.getApplicationStarted(true, true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

                    //                            //if (PPApplicationStatic.logEnabled()) {
                    //                            ListenableFuture<List<WorkInfo>> statuses;
                    //                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG);
                    //                            try {
                    //                                List<WorkInfo> workInfoList = statuses.get();
                    //                            } catch (Exception ignored) {
                    //                            }
                    //                            //}
                    //
                    //                            PPApplicationStatic.logE("[WORKER_CALL] PhoneProfilesService.doCommand", "xxx");
                    //workManager.enqueue(worker);
                    workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_WIFI_NETWORK_CALLBACK_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
//                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

            /*
            final Context appContext = context;
            //PPApplication.startHandlerThreadBroadcast();
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        appContext) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=WifiNetworkCallback.doConnection");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiNetworkCallback_doConnection_2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        _doConnection(appContext);

                    } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
            }; //);
            //PPApplication.createEventsHandlerExecutor();
            //PPApplication.eventsHandlerExecutor.submit(runnable);
            PPApplication.createDelayedEventsHandlerExecutor();
            PPApplication.delayedEventsHandlerExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
            */
//        }
    }

    static void _doConnection(Context appContext) {
        if (PPApplication.connectToSSIDStarted) {
            // connect to SSID is started
//                PPApplicationStatic.logE("[IN_LISTENER] ----------- WifiNetworkCallback._doConnection", "connectToSSIDStarted");

            if (PPApplication.wifiNetworkconnected) {
                //WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                //if ((PhoneProfilesService.connectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) ||
                //    (wifiInfo.getSSID().equals(PhoneProfilesService.connectToSSID)))
                PPApplication.connectToSSIDStarted = false;
            }
        }

        if (EventStatic.getGlobalEventsRunning(appContext)) {
//            PPApplicationStatic.logE("[IN_LISTENER] ----------- WifiNetworkCallback._doConnection", "xxx");
            //if ((info.getState() == NetworkInfo.State.CONNECTED) ||
            //        (info.getState() == NetworkInfo.State.DISCONNECTED)) {

            if (ApplicationPreferences.prefEventWifiScanRequest ||
                    ApplicationPreferences.prefEventWifiWaitForResult ||
                    ApplicationPreferences.prefEventWifiEnabledForScan)
                PhoneProfilesServiceStatic.cancelWifiWorker(appContext, true, false);

            if ((PhoneProfilesService.getInstance() != null) && (!PPApplication.connectToSSIDStarted)) {
                // connect to SSID is not started

                // start events handler

//                PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] WifiNetworkCallback._doConnection", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

//                PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] WifiNetworkCallback._doConnection", "sensorType=SENSOR_TYPE_WIFI_CONNECTION");
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_CONNECTION);

                PPApplicationStatic.restartWifiScanner(appContext);
            }
            //}
        }
    }

}
