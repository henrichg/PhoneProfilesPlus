package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class RestartEventsWithDelayWorker extends Worker {

    private final Context context;

    static final String WORK_TAG_1 = "restartEventsWithDelay1Work";
    static final String WORK_TAG_2 = "restartEventsWithDelay2Work";

    public RestartEventsWithDelayWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
//            long start = System.currentTimeMillis();
//            PPApplicationStatic.logE("[IN_WORKER]  RestartEventsWithDelayWorker.doWork", "--------------- START");

            if (!PPApplicationStatic.getApplicationStarted(true, true))
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

            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, 0, 0, 0f);
            if (logType != PPApplication.ALTYPE_UNDEFINED)
                PPApplicationStatic.addActivityLog(context, logType, null, null, "");
            //dataWrapper.restartEvents(unblockEventsRun, true, true, false);
            dataWrapper.restartEventsWithRescan(alsoRescan, unblockEventsRun, false, false, true, false);
            //dataWrapper.invalidateDataWrapper();

//            long finish = System.currentTimeMillis();
//            long timeElapsed = finish - start;
//            PPApplicationStatic.logE("[IN_WORKER]  RestartEventsWithDelayWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success();
        } catch (Exception e) {
            //Log.e("RestartEventsWithDelayWorker.doWork", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            //Handler _handler = new Handler(getApplicationContext().getMainLooper());
            //Runnable r = new Runnable() {
            //    public void run() {
            //        android.os.Process.killProcess(PPApplication.pid);
            //    }
            //};
            //_handler.postDelayed(r, 1000);
            return Result.failure();
        }
        //return Result.success();
    }

}
