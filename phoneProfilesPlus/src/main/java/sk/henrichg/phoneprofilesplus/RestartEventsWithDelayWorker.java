package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// DO NOT REMOVE. MUST EXISTS !!!
public class RestartEventsWithDelayWorker extends Worker {

    //private final Context context;

    static final String WORK_TAG_1 = "restartEventsWithDelay1Work";
    static final String WORK_TAG_2 = "restartEventsWithDelay2Work";

    @SuppressWarnings("unused")
    public RestartEventsWithDelayWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        //this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
/*        try {
            long start = System.currentTimeMillis();
            PPApplication.logE("[IN_WORKER]  RestartEventsWithDelayWorker.doWork", "--------------- START");

//            PPApplication.logE("[FIFO_TEST] RestartEventsWithDelayWorker.doWork","xxx"); //"clearOld="+clearOld);

            if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return Result.success();

            //Data outputData;

            // Get the input
            boolean alsoRescan = getInputData().getBoolean(PhoneProfilesService.EXTRA_ALSO_RESCAN, false);
            boolean unblockEventsRun = getInputData().getBoolean(PhoneProfilesService.EXTRA_UNBLOCK_EVENTS_RUN, false);
            int logType = getInputData().getInt(PhoneProfilesService.EXTRA_LOG_TYPE, 0);

            //outputData = generateResult(LocationGeofenceEditorActivity.FAILURE_RESULT,
            //                                    getApplicationContext().getString(R.string.event_preferences_location_no_address_found),
            //                                    updateName);

            //return Result.success(outputData);

            doWork(false, alsoRescan, unblockEventsRun, logType, context);

            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            PPApplication.logE("[IN_WORKER]  RestartEventsWithDelayWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success();
        } catch (Exception e) {
            //Log.e("RestartEventsWithDelayWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            //Handler _handler = new Handler(getApplicationContext().getMainLooper());
            //Runnable r = new Runnable() {
            //    public void run() {
            //        android.os.Process.killProcess(PPApplication.pid);
            //    }
            //};
            _handler.postDelayed(r, 1000);
            return Result.failure();
        }
        */
        return Result.success();
    }

/*
    static void doWork(boolean useHandler, final boolean alsoRescan, final boolean unblockEventsRun, final int logType, Context context) {
        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        final Context appContext = context.getApplicationContext();
        if (useHandler) {
            PPApplication.startHandlerThreadBroadcast();
            final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        context.getApplicationContext()) {
            __handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=RestartEventsWithDelayWorker.doWork (1)");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":RestartEventsWithDelayWorker_doWork");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, 0, 0, 0f);
                    if (logType != PPApplication.ALTYPE_UNDEFINED)
                        PPApplication.addActivityLog(context, logType, null, null, "");
                    //dataWrapper.restartEvents(unblockEventsRun, true, true, false);
//                    PPApplication.logE("[APP_START] RestartEventsWithDelayWorker.doWork", "xxx");
                    dataWrapper.restartEventsWithRescan(alsoRescan, unblockEventsRun, false, false, true, false);
                    //dataWrapper.invalidateDataWrapper();

                } catch (Exception e) {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
            });
        } else {
            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
            if (logType != PPApplication.ALTYPE_UNDEFINED)
                PPApplication.addActivityLog(appContext, logType, null, null, "");
            //dataWrapper.restartEvents(unblockEventsRun, true, true, false);
//            PPApplication.logE("[APP_START] RestartEventsWithDelayWorker.doWork", "xxx");
            dataWrapper.restartEventsWithRescan(alsoRescan, unblockEventsRun, false, false, true, false);
            //dataWrapper.invalidateDataWrapper();
        }

    }
*/
}
