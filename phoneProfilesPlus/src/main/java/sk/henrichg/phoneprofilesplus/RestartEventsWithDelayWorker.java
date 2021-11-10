package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

@SuppressWarnings("WeakerAccess")
public class RestartEventsWithDelayWorker extends Worker {

    private final Context context;

    static final String WORK_TAG = "restartEventsWithDelayWork";
    static final String WORK_TAG_APPEND = "restartEventsWithDelayAppendWork";

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
//            PPApplication.logE("[IN_WORKER]  RestartEventsWithDelayWorker.doWork", "xxxx");
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


            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, 0, 0f);
            if (logType != PPApplication.ALTYPE_UNDEFINED)
                PPApplication.addActivityLog(context, logType, null, null, "");
            //dataWrapper.restartEvents(unblockEventsRun, true, true, false);
//            PPApplication.logE("[APP_START] RestartEventsWithDelayWorker.doWork", "xxx");
            dataWrapper.restartEventsWithRescan(alsoRescan, unblockEventsRun, false, false, true, false);
            //dataWrapper.invalidateDataWrapper();

            return Result.success();
        } catch (Exception e) {
            //Log.e("RestartEventsWithDelayWorker.doWork", Log.getStackTraceString(e));
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

}
