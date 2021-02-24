package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

// https://issuetracker.google.com/issues/115575872#comment16

@SuppressWarnings("WeakerAccess")
public class LocationSensorWorker extends Worker {

    final Context context;

    static final String LOCATION_SENSOR_WORK_TAG = "locationSensorWork";

    public LocationSensorWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);

        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            PPApplication.logE("[IN_WORKER] LocationSensorWorker.doWork", "xxxx");

            if (Event.getGlobalEventsRunning()) {
                EventsHandler eventsHandler = new EventsHandler(context);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_GEOFENCES_SCANNER);
            }

            enqueueWork(false, context);

            return Result.success();
        } catch (Exception e) {
            //Log.e("LocationSensorWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            /*Handler _handler = new Handler(getApplicationContext().getMainLooper());
            Runnable r = new Runnable() {
                public void run() {
                    android.os.Process.killProcess(PPApplication.pid);
                }
            };
            _handler.postDelayed(r, 1000);*/
            return Result.failure();
        }
    }

    static void enqueueWork(boolean immediate, Context context) {
        PPApplication.logE("##### LocationSensorWorker.enqueueWork", "immediate=" + immediate);

        OneTimeWorkRequest worker = null;

        if (immediate) {
            worker =
                    new OneTimeWorkRequest.Builder(LocationSensorWorker.class)
                            .addTag(LOCATION_SENSOR_WORK_TAG)
                            .build();
        }
        else {
            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);

            // check power save mode
            String applicationEventLocationUpdateInPowerSaveMode = ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode;
            //boolean powerSaveMode = PPApplication.isPowerSaveMode;
            if (!(isPowerSaveMode && applicationEventLocationUpdateInPowerSaveMode.equals("2"))) {
                int interval = 25; // seconds
                if (ApplicationPreferences.applicationEventLocationUpdateInterval > 1) {
                    // interval is in minutes
                    interval = (ApplicationPreferences.applicationEventLocationUpdateInterval * 60);
                }
                PPApplication.logE("##### LocationSensorWorker.enqueueWork", "ApplicationPreferences.applicationEventLocationUpdateInterval=" + ApplicationPreferences.applicationEventLocationUpdateInterval);
                if (isPowerSaveMode && applicationEventLocationUpdateInPowerSaveMode.equals("1"))
                    interval = 2 * interval;
                PPApplication.logE("##### LocationSensorWorker.enqueueWork", "interval=" + interval);

                worker =
                        new OneTimeWorkRequest.Builder(LocationSensorWorker.class)
                                .addTag(LOCATION_SENSOR_WORK_TAG)
                                .setInitialDelay(interval, TimeUnit.SECONDS)
                                .build();
            }
        }
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance();
            //PPApplication.logE("##### LocationSensorWorker.enqueueWork", "workManager="+workManager);
            if (workManager != null) {

//                //if (PPApplication.logEnabled()) {
//                ListenableFuture<List<WorkInfo>> statuses;
//                statuses = workManager.getWorkInfosForUniqueWork(LOCATION_SENSOR_WORK_TAG);
//                try {
//                    List<WorkInfo> workInfoList = statuses.get();
//                    PPApplication.logE("[TEST BATTERY] LocationSensorWorker.enqueueWork", "for=" + LOCATION_SENSOR_WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                } catch (Exception ignored) {
//                }
//                //}

                if (worker != null) {
                    PPApplication.logE("[WORKER_CALL] LocationSensorWorker.enqueueWork", "enqueue with REPLACE");
                    workManager.enqueueUniqueWork(LOCATION_SENSOR_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

}
