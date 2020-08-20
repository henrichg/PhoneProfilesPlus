package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class PeriodicEventsHandlerWorker extends Worker {

    final Context context;

    static final String WORK_TAG  = "periodicEventsHandlerWorker";
    static final String WORK_TAG_SHORT  = "periodicEventsHandlerWorkerShort";

    public PeriodicEventsHandlerWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            PPApplication.logE("[WORKER CALL]  PeriodicEventsHandlerWorker.doWork", "xxxx");

            if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return Result.success();

            if (ApplicationPreferences.applicationEventBackgroundScanningEnableScanning) {

                //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
                boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                if (isPowerSaveMode && ApplicationPreferences.applicationEventBackgroundScanningScanInPowerSaveMode.equals("2")) {
                    PPApplication.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG);
                    PPApplication.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT);
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("PeriodicEventsHandlerWorker.doWork", "return - update in power save mode is not allowed");
                        PPApplication.logE("PeriodicEventsHandlerWorker.doWork", "---------------------------------------- END");
                    }*/
                    return Result.success();
                }

                if (Event.getGlobalEventsRunning()) {

                    //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=PeriodicEventsHandlerWorker.doWork");

                    PPApplication.logE("[EVENTS_HANDLER] PeriodicEventsHandlerWorker.doWork", "sensorType=SENSOR_TYPE_PERIODIC_EVENTS_HANDLER");
                    EventsHandler eventsHandler = new EventsHandler(getApplicationContext());
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PERIODIC_EVENTS_HANDLER);

                    //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PeriodicEventsHandlerWorker.doWork");
                }

                //enqueueWork();
                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG)
                                //.setInitialDelay(200, TimeUnit.MILLISECONDS)
                                .build();
                try {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null) {

//                            //if (PPApplication.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                                PPApplication.logE("[TEST BATTERY] PeriodicEventsHandlerWorker.doWork", "for=" + MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                            } catch (Exception ignored) {
//                            }
//                            //}

                        workManager.enqueueUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }

            }

            return Result.success();
        } catch (Exception e) {
            //Log.e("PeriodicEventsHandlerWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            /*Handler _handler = new Handler(getApplicationContext().getMainLooper());
            Runnable r = new Runnable() {
                public void run() {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            };
            _handler.postDelayed(r, 1000);*/
            return Result.failure();
        }
    }

    static void enqueueWork(Context appContext) {
        int interval = ApplicationPreferences.applicationEventBackgroundScanningScanInterval;
        //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(appContext);
        if (isPowerSaveMode && ApplicationPreferences.applicationEventBackgroundScanningScanInPowerSaveMode.equals("1"))
            interval = 2 * interval;

        /*int keepResultsDelay = (interval * 5);
        if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
            keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/
        OneTimeWorkRequest periodicEventsHandlerWorker =
                new OneTimeWorkRequest.Builder(PeriodicEventsHandlerWorker.class)
                        .addTag(PeriodicEventsHandlerWorker.WORK_TAG)
                        .setInitialDelay(interval, TimeUnit.MINUTES)
                        .build();
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {

//                                //if (PPApplication.logEnabled()) {
//                                ListenableFuture<List<WorkInfo>> statuses;
//                                statuses = workManager.getWorkInfosForUniqueWork(PeriodicEventsHandlerWorker.WORK_TAG);
//                                try {
//                                    List<WorkInfo> workInfoList = statuses.get();
//                                    PPApplication.logE("[TEST BATTERY] PeriodicEventsHandlerWorker.enqueueWork", "for=" + PeriodicEventsHandlerWorker.WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                                } catch (Exception ignored) {
//                                }
//                                //}

                workManager.enqueueUniqueWork(PeriodicEventsHandlerWorker.WORK_TAG, ExistingWorkPolicy.REPLACE/*KEEP*/, periodicEventsHandlerWorker);
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

}
