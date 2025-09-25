package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class WifiNetworkCallback extends ConnectivityManager.NetworkCallback {

    //private final Context context;

    static final String EXTRA_FOR_CAPABILITIES = "forCapabilities";

    WifiNetworkCallback(/*Context context*/) {
        //this.context = context.getApplicationContext();
    }

    @Override
    public void onLost(@NonNull Network network) {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onLost", "xxx");
//        Log.e("[IN_LISTENER] ----------- WifiNetworkCallback.onLost", "xxx");
        PPApplication.wifiNetworkconnected = false;
        doConnection(false);
    }

    @Override
    public void onUnavailable() {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onUnavailable", "xxx");
//        Log.e("[IN_LISTENER] ----------- WifiNetworkCallback.onUnavailable", "xxx");
        PPApplication.wifiNetworkconnected = false;
        doConnection(false);
    }

    @Override
    public void onLosing(@NonNull Network network, int maxMsToLive) {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onLosing", "xxx");
//        Log.e("[IN_LISTENER] ----------- WifiNetworkCallback.onLosing", "xxx");
        doConnection(true);
    }

    @Override
    public void onAvailable(@NonNull Network network) {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onAvailable", "xxx");
//        Log.e("[IN_LISTENER] ----------- WifiNetworkCallback.onAvailable", "xxx");
        PPApplication.wifiNetworkconnected = true;
        doConnection(false);
    }

    @Override
    public void onCapabilitiesChanged (@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- WifiNetworkCallback.onCapabilitiesChanged", "xxx");
//        Log.e("[IN_LISTENER] ----------- WifiNetworkCallback.onCapabilitiesChanged", "xxx");
        doConnection(true);
    }

    private void doConnection(boolean forCapabilities) {
        //final Context appContext = getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;
/*
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
        */
            // !!! must be used MainWorker with delay ans REPLACE, because is often called this onChange
            // for change volumes
                /*Data workData = new Data.Builder()
                        .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_VOLUMES)
                        .build();*/

//        PPApplicationStatic.logE("[MAIN_WORKER_CALL] WifiNetworkCallback.doConnection", "xxxxxxxxxxxxxxxxxxxx");

        Data workData = new Data.Builder()
                .putBoolean(EXTRA_FOR_CAPABILITIES, forCapabilities)
                .build();
        OneTimeWorkRequest worker =
                new OneTimeWorkRequest.Builder(MainWorker.class)
                        .addTag(MainWorker.HANDLE_EVENTS_WIFI_NETWORK_CALLBACK_WORK_TAG)
                        .setInputData(workData)
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
//                    PPApplicationStatic.logE("[WORKER_CALL] WifiNetworkCallback.doConnection", "xxx");
                    //workManager.enqueue(worker);
                    workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_WIFI_NETWORK_CALLBACK_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
//                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

            /*
            final Context appContext = context.getApplicationContext();
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
            };
            //PPApplication.createEventsHandlerExecutor();
            //PPApplication.eventsHandlerExecutor.submit(runnable);
            PPApplication.createDelayedEventsHandlerExecutor();
            PPApplication.delayedEventsHandlerExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
            */
//        }
    }

    static void _doConnection(Context appContext, boolean forCapabilities) {
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
//            PPApplicationStatic.logE("[IN_LISTENER] ----------- WifiNetworkCallback._doConnection", "forCapabilities="+forCapabilities);
            //if ((info.getState() == NetworkInfo.State.CONNECTED) ||
            //        (info.getState() == NetworkInfo.State.DISCONNECTED)) {

            if (!forCapabilities) {
                if (ApplicationPreferences.prefEventWifiScanRequest ||
                        ApplicationPreferences.prefEventWifiWaitForResult ||
                        ApplicationPreferences.prefEventWifiEnabledForScan)
                    PhoneProfilesServiceStatic.cancelWifiWorker(appContext, true, false);
            }

            if ((PhoneProfilesService.getInstance() != null) && (!PPApplication.connectToSSIDStarted)) {
                // connect to SSID is not started

                // start events handler

                synchronized (PPApplication.handleEventsMutex) {

                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    if (forCapabilities) {
//                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] WifiNetworkCallback._doConnection", "SENSOR_TYPE_RADIO_SWITCH");
                        eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_RADIO_SWITCH});
                    } else {
//                    PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] WifiNetworkCallback._doConnection", "SENSOR_TYPE_RADIO_SWITCH,SENSOR_TYPE_WIFI_CONNECTION");
                        eventsHandler.handleEvents(new int[]{
                                EventsHandler.SENSOR_TYPE_RADIO_SWITCH,
                                EventsHandler.SENSOR_TYPE_WIFI_CONNECTION});

//                    PPApplicationStatic.logE("[RESTART_WIFI_SCANNER] ----------- WifiNetworkCallback._doConnection", "xxx");
                        PPApplicationStatic.restartWifiScanner(appContext);
                    }

                }
            }
            //}
        }
    }

}
