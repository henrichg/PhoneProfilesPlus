package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Handler;

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
            //PPApplication.logE("PeriodicEventsHandlerWorker.doWork", "xxx");

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

                    EventsHandler eventsHandler = new EventsHandler(getApplicationContext());
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PERIODIC_EVENTS_HANDLER);

                    //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PeriodicEventsHandlerWorker.doWork");
                }

                PPApplication.startHandlerThreadPPScanners();
                final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int interval = ApplicationPreferences.applicationEventBackgroundScanningScanInterval;
                        //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
                        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                        if (isPowerSaveMode && ApplicationPreferences.applicationEventBackgroundScanningScanInPowerSaveMode.equals("1"))
                            interval = 2 * interval;

                        int keepResultsDelay = (interval * 5);
                        if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
                            keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;
                        OneTimeWorkRequest periodicEventsHandlerWorker =
                                new OneTimeWorkRequest.Builder(PeriodicEventsHandlerWorker.class)
                                        .addTag(PeriodicEventsHandlerWorker.WORK_TAG)
                                        .setInitialDelay(interval, TimeUnit.MINUTES)
                                        //.keepResultsForAtLeast(keepResultsDelay, TimeUnit.MINUTES)
                                        .build();
                        try {
                            WorkManager workManager = PPApplication.getWorkManagerInstance();
                            if (workManager != null)
                                workManager.enqueueUniqueWork(PeriodicEventsHandlerWorker.WORK_TAG, ExistingWorkPolicy.REPLACE/*KEEP*/, periodicEventsHandlerWorker);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                }, 500);

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

}
