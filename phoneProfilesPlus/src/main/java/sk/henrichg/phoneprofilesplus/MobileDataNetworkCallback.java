package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MobileDataNetworkCallback extends ConnectivityManager.NetworkCallback {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Context context;

    @SuppressWarnings("unused")
    static volatile boolean connected = false;

    MobileDataNetworkCallback(Context context) { this.context = context.getApplicationContext(); }

    @Override
    public void onLost(Network network) {
//        PPApplication.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onLost", "xxx");
        connected = false;
        doConnection();
    }

    @Override
    public void onUnavailable() {
//        PPApplication.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onUnavailable", "xxx");
        connected = false;
        doConnection();
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
//        PPApplication.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onLosing", "xxx");
        doConnection();
    }

    @Override
    public void onAvailable(Network network) {
//        PPApplication.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onAvailable", "xxx");
        connected = true;
        doConnection();
    }

    @Override
    public void onCapabilitiesChanged (Network network, NetworkCapabilities networkCapabilities) {
//        PPApplication.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onCapabilitiesChanged", "xxx");
        doConnection();
    }

    @SuppressWarnings("SuspiciousIndentAfterControlStatement")
    private void doConnection() {
        //final Context appContext = getApplicationContext();

        if (!PPApplication.getApplicationStarted(true, true))
            // application is not started
            return;

/*
        if (Build.VERSION.SDK_INT >= 26) {
            // configured is PPApplication.handlerThreadBroadcast handler (see PhoneProfilesService.registerCallbacks()
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileDataNetworkCallback_doConnection_1");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                _doConnection(context);

            } catch (Exception e) {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplication.recordException(e);
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
                            .addTag(MainWorker.HANDLE_EVENTS_MOBILE_DATA_NETWORK_CALLBACK_WORK_TAG)
                            //.setInputData(workData)
                            .setInitialDelay(5, TimeUnit.SECONDS)
                            //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                            .build();
            try {
                if (PPApplication.getApplicationStarted(true, true)) {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null) {

    //                            //if (PPApplication.logEnabled()) {
    //                            ListenableFuture<List<WorkInfo>> statuses;
    //                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG);
    //                            try {
    //                                List<WorkInfo> workInfoList = statuses.get();
    //                            } catch (Exception ignored) {
    //                            }
    //                            //}
    //
    //                            PPApplication.logE("[WORKER_CALL] PhoneProfilesService.doCommand", "xxx");
                        //workManager.enqueue(worker);
                        workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_MOBILE_DATA_NETWORK_CALLBACK_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                    }
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
            }

            /*
            final Context appContext = this.context;
            //PPApplication.startHandlerThreadBroadcast();
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        appContext) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=MobileDataNetworkCallback.doConnection");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileDataNetworkCallback_doConnection_2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        _doConnection(appContext);

                    } catch (Exception e) {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
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
        if (Event.getGlobalEventsRunning()) {
//            PPApplication.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback._doConnection", "xxx");

            //if ((info.getState() == NetworkInfo.State.CONNECTED) ||
            //        (info.getState() == NetworkInfo.State.DISCONNECTED)) {

            if (PhoneProfilesService.getInstance() != null) {
                // start events handler

//                PPApplication.logE("[EVENTS_HANDLER_CALL] MobileDataNetworkCallback._doConnection", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);
            }
        }
    }

}
