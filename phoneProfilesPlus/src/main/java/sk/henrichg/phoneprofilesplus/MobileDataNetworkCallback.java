package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MobileDataNetworkCallback extends ConnectivityManager.NetworkCallback {

    //private final Context context;

    //static volatile boolean connected = false;

    MobileDataNetworkCallback(/*Context context*/) { /*this.context = context.getApplicationContext();*/ }

    @Override
    public void onLost(@NonNull Network network) {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onLost", "xxx");
        //connected = false;
        doConnection();
    }

    @Override
    public void onUnavailable() {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onUnavailable", "xxx");
        //connected = false;
        doConnection();
    }

    @Override
    public void onLosing(@NonNull Network network, int maxMsToLive) {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onLosing", "xxx");
        doConnection();
    }

    @Override
    public void onAvailable(@NonNull Network network) {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onAvailable", "xxx");
        //connected = true;
        doConnection();
    }

    @Override
    public void onCapabilitiesChanged (@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
//        PPApplicationStatic.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onCapabilitiesChanged", "xxx");
        doConnection();
    }

    private void doConnection() {
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
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileDataNetworkCallback_doConnection_1");
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

//        PPApplicationStatic.logE("[MAIN_WORKER_CALL] MobileDataNetworkCallback.doConnection", "xxxxxxxxxxxxxxxxxxxx");

        OneTimeWorkRequest worker =
                new OneTimeWorkRequest.Builder(MainWorker.class)
                        .addTag(MainWorker.HANDLE_EVENTS_MOBILE_DATA_NETWORK_CALLBACK_WORK_TAG)
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
//                    PPApplicationStatic.logE("[WORKER_CALL] MobileDataNetworkCallback.doConnection", "xxx");
                    //workManager.enqueue(worker);
                    workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_MOBILE_DATA_NETWORK_CALLBACK_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
//                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

            /*
            final Context appContext = this.context;
            Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=MobileDataNetworkCallback.doConnection");

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
    }

    static void _doConnection(Context appContext) {
        if (EventStatic.getGlobalEventsRunning(appContext)) {
//            PPApplicationStatic.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback._doConnection", "xxx");

            synchronized (PPApplication.handleEventsMutex) {

                //if ((info.getState() == NetworkInfo.State.CONNECTED) ||
                //        (info.getState() == NetworkInfo.State.DISCONNECTED)) {

                if (PhoneProfilesService.getInstance() != null) {
                    // start events handler

//                PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] MobileDataNetworkCallback._doConnection", "SENSOR_TYPE_RADIO_SWITCH");
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_RADIO_SWITCH});
                }
            }
        }
    }

}
