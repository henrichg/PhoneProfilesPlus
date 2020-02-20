package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

@SuppressWarnings("WeakerAccess")
public class GeofenceScanWorker extends Worker {

    private final Context context;

    static final String WORK_TAG  = "GeofenceScannerJob";

    public GeofenceScanWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            //PPApplication.logE("GeofenceScanWorker.doWork", "---------------------------------------- START");

            //CallsCounter.logCounter(context, "GeofenceScanWorker.doWork", "GeofenceScanWorker_doWork");

            if (Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, context).allowed !=
                    PreferenceAllowed.PREFERENCE_ALLOWED) {
                cancelWork(context, false, null);
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("GeofenceScanWorker.doWork", "return - not allowed geofence scanning");
                    PPApplication.logE("GeofenceScanWorker.doWork", "---------------------------------------- END");
                }*/
                return Result.success();
            }

            //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
            if (isPowerSaveMode && ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode.equals("2")) {
                //PPApplication.logE("GeofenceScanWorker.doWork", "update in power save mode is not allowed");
                cancelWork(context, false, null);
                //PPApplication.logE("GeofenceScanWorker.doWork", "---------------------------------------- END");
                return Result.success();
            }

            if (Event.getGlobalEventsRunning()) {
                boolean geofenceScannerUpdatesStarted = false;
                synchronized (PPApplication.geofenceScannerMutex) {
                    if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().getGeofencesScanner() != null)) {
                        GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                        if (scanner.mUpdatesStarted) {
                            //PPApplication.logE("GeofenceScanWorker.doWork", "location updates started - save to DB");

                            //if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted())
                            scanner.updateGeofencesInDB();

                            geofenceScannerUpdatesStarted = true;
                        }
                    }
                }

                if (geofenceScannerUpdatesStarted) {
                    //PPApplication.logE("GeofenceScanWorker.doWork", "location updates started - start EventsHandler");

                    // start events handler
                    EventsHandler eventsHandler = new EventsHandler(context);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_GEOFENCES_SCANNER);

                }
            }

            //PPApplication.logE("GeofenceScanWorker.doWork - handler", "schedule work");
            scheduleWork(context.getApplicationContext(), false, null, false/*, false*/);

            /*PPApplication.startHandlerThreadPPScanners();
            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("GeofenceScanWorker.doWork - handler", "schedule work");
                    scheduleWork(context, false, null, false);
                }
            }, 500);*/

            //PPApplication.logE("GeofenceScanWorker.doWork", "---------------------------------------- END");
            return Result.success();
        } catch (Exception e) {
            Log.e("GeofenceScanWorker.doWork", Log.getStackTraceString(e));
            FirebaseCrashlytics.getInstance().recordException(e);
            //Crashlytics.logException(e);
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

    public void onStopped () {
        //PPApplication.logE("GeofenceScanWorker.onStopped", "xxx");

        //CallsCounter.logCounter(context, "GeofenceScanWorker.onStopped", "GeofenceScanWorker_onStopped");
    }

    private static void _scheduleWork(final Context context, boolean shortInterval/*, final boolean forScreenOn*/) {
        try {
            WorkManager workManager = WorkManager.getInstance(context);

            //PPApplication.logE("GeofenceScanWorker._scheduleWork", "---------------------------------------- START");

            int interval;
            synchronized (PPApplication.geofenceScannerMutex) {
                /*if (PPApplication.logEnabled()) {
                    if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted())
                        PPApplication.logE("GeofenceScanWorker._scheduleWork", "mUpdatesStarted=" + PhoneProfilesService.getInstance().getGeofencesScanner().mUpdatesStarted);
                    else {
                        PPApplication.logE("GeofenceScanWorker._scheduleWork", "scanner is not started");
                        PPApplication.logE("GeofenceScanWorker._scheduleWork", "PhoneProfilesService.getInstance()=" + PhoneProfilesService.getInstance());
                    }
                }*/

                // look at GeofenceScanner:UPDATE_INTERVAL_IN_MILLISECONDS
                //int updateDuration = 30;

                if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted() &&
                        PhoneProfilesService.getInstance().getGeofencesScanner().mUpdatesStarted) {
                    interval = ApplicationPreferences.applicationEventLocationUpdateInterval * 60;
                    //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
                    boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                    if (isPowerSaveMode && ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode.equals("1"))
                        interval = 2 * interval;
                    //interval = interval - updateDuration;
                } else {
                    interval = 5;
                    shortInterval = true;
                }

                //PPApplication.logE("GeofenceScanWorker._scheduleWork", "interval=" + interval);
            }

            if (!shortInterval) {
                //PPApplication.logE("GeofenceScanWorker._scheduleWork", "exact work");
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(GeofenceScanWorker.class)
                        .setInitialDelay(interval, TimeUnit.SECONDS)
                        .addTag(WORK_TAG)
                        .build();
                workManager.enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, workRequest);
            } else {
                //PPApplication.logE("GeofenceScanWorker._scheduleWork", "start now work");
                waitForFinish(context);
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(GeofenceScanWorker.class)
                        .addTag(WORK_TAG)
                        .build();
                workManager.enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, workRequest);
            }

            //PPApplication.logE("GeofenceScanWorker._scheduleWork", "---------------------------------------- END");
        } catch (Exception e) {
            Log.e("GeofenceScanWorker._scheduleWork", Log.getStackTraceString(e));
            FirebaseCrashlytics.getInstance().recordException(e);
            //Crashlytics.logException(e);
        }
    }

    static void scheduleWork(final Context context,
                            @SuppressWarnings("SameParameterValue") final boolean useHandler,
                            final Handler _handler, final boolean startScanning/*, final boolean forScreenOn*/) {
        //PPApplication.logE("GeofenceScanWorker.scheduleWork", "startScanning="+startScanning);

        //if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.isGeofenceScannerStarted()) {
        if (useHandler && (_handler == null)) {
            PPApplication.startHandlerThreadPPScanners();
            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    _scheduleWork(context, startScanning/*, forScreenOn*/);
                }
            });
        }
        else {
            _scheduleWork(context, startScanning/*, forScreenOn*/);
        }
        //}
        //else
        //    PPApplication.logE("GeofenceScanWorker.scheduleWork", "scanner is not started");
    }

    private static void _cancelWork(final Context context) {
        if (isWorkScheduled(context)) {
            try {
                waitForFinish(context);

                PhoneProfilesService.cancelWork(WORK_TAG, context);

                //PPApplication.logE("GeofenceScanWorker._cancelWork", "CANCELED");

            } catch (Exception e) {
                Log.e("GeofenceScanWorker._cancelWork", Log.getStackTraceString(e));
                FirebaseCrashlytics.getInstance().recordException(e);
                //Crashlytics.logException(e);
            }
        }
    }

    private static void waitForFinish(Context context) {
        if (!isWorkRunning(context)) {
            //PPApplication.logE("GeofenceScanWorker.waitForFinish", "NOT RUNNING");
            return;
        }

        try {
            WorkManager workManager = WorkManager.getInstance(context);

            //PPApplication.logE("GeofenceScanWorker.waitForFinish", "START WAIT FOR FINISH");
            long start = SystemClock.uptimeMillis();
            do {

                ListenableFuture<List<WorkInfo>> statuses = workManager.getWorkInfosByTag(WORK_TAG);
                boolean allFinished = true;
                //noinspection TryWithIdenticalCatches
                try {
                    List<WorkInfo> workInfoList = statuses.get();
                    for (WorkInfo workInfo : workInfoList) {
                        WorkInfo.State state = workInfo.getState();
                        if (!state.isFinished()) {
                            allFinished = false;
                            break;
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (allFinished) {
                    //PPApplication.logE("GeofenceScanWorker.waitForFinish", "FINISHED");
                    break;
                }

                //try { Thread.sleep(100); } catch (InterruptedException e) { }
                SystemClock.sleep(100);
            } while (SystemClock.uptimeMillis() - start < 10 * 1000);

            //PPApplication.logE("GeofenceScanWorker.waitForFinish", "END WAIT FOR FINISH");
        } catch (Exception e) {
            Log.e("GeofenceScanWorker.waitForFinish", Log.getStackTraceString(e));
            FirebaseCrashlytics.getInstance().recordException(e);
            //Crashlytics.logException(e);
        }
    }

    static void cancelWork(final Context context, final boolean useHandler, final Handler _handler) {
        //PPApplication.logE("GeofenceScanWorker.cancelWork", "xxx");

        if (useHandler && (_handler == null)) {
            PPApplication.startHandlerThreadPPScanners();
            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    _cancelWork(context);
                }
            });
        }
        else {
            _cancelWork(context);
        }
    }

    private static boolean isWorkRunning(Context context) {
        try {
            WorkManager instance = WorkManager.getInstance(context);
            ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag(WORK_TAG);
            //noinspection TryWithIdenticalCatches
            try {
                List<WorkInfo> workInfoList = statuses.get();
                //PPApplication.logE("GeofenceScanWorker.isWorkScheduled", "workInfoList.size()="+workInfoList.size());
                //return workInfoList.size() != 0;
                boolean running = false;
                for (WorkInfo workInfo : workInfoList) {
                    WorkInfo.State state = workInfo.getState();
                    running = state == WorkInfo.State.RUNNING;
                }
                return running;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            Log.e("GeofenceScanWorker.isWorkRunning", Log.getStackTraceString(e));
            FirebaseCrashlytics.getInstance().recordException(e);
            //Crashlytics.logException(e);
            return false;
        }
    }

    static boolean isWorkScheduled(Context context) {
        //PPApplication.logE("GeofenceScanWorker.isWorkScheduled", "xxx");
        try {
            WorkManager instance = WorkManager.getInstance(context);
            ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag(WORK_TAG);
            //noinspection TryWithIdenticalCatches
            try {
                List<WorkInfo> workInfoList = statuses.get();
                //PPApplication.logE("GeofenceScanWorker.isWorkScheduled", "workInfoList.size()="+workInfoList.size());
                //return workInfoList.size() != 0;
                boolean running = false;
                for (WorkInfo workInfo : workInfoList) {
                    WorkInfo.State state = workInfo.getState();
                    running = state == WorkInfo.State.RUNNING | state == WorkInfo.State.ENQUEUED;
                }
                return running;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            Log.e("GeofenceScanWorker.isWorkScheduled", Log.getStackTraceString(e));
            FirebaseCrashlytics.getInstance().recordException(e);
            //Crashlytics.logException(e);
            return false;
        }
    }

}
