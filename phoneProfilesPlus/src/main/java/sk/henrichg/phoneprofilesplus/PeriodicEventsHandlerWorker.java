package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.util.Log;

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
            PPApplication.logE("PeriodicEventsHandlerWorker.doWork", "xxx");

            if (ApplicationPreferences.applicationEventBackgroundScanningEnableScanning) {
                if (PPApplication.getApplicationStarted(true)
                        && Event.getGlobalEventsRunning()) {

                    EventsHandler eventsHandler = new EventsHandler(getApplicationContext());
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PERIODIC_EVENTS_HANDLER);
                }

                int interval = ApplicationPreferences.applicationEventBackgroundScanningScanInterval;
                //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
                boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                if (isPowerSaveMode && ApplicationPreferences.applicationEventBackgroundScanningScanInPowerSaveMode.equals("1"))
                    interval = 2 * interval;

                OneTimeWorkRequest periodicEventsHandlerWorker =
                        new OneTimeWorkRequest.Builder(PeriodicEventsHandlerWorker.class)
                                .addTag("periodicEventsHandlerWorker")
                                .setInitialDelay(interval, TimeUnit.MINUTES)
                                .build();
                try {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null)
                        workManager.enqueueUniqueWork("periodicEventsHandlerWorker", ExistingWorkPolicy.KEEP, periodicEventsHandlerWorker);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }

            return Result.success();
        } catch (Exception e) {
            Log.e("PeriodicEventsHandlerWorker.doWork", Log.getStackTraceString(e));
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
