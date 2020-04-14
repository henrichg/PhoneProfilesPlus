package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.util.Log;

//import com.crashlytics.android.Crashlytics;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

@SuppressWarnings("WeakerAccess")
public class RestartEventsWithDelayWorker extends Worker {

    private final Context context;

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
            //PPApplication.logE("[TEST BATTERY] RestartEventsWithDelayWorker.doWork", "xxx");

            //Data outputData;

            // Get the input
            boolean alsoRescan = getInputData().getBoolean(PhoneProfilesService.EXTRA_ALSO_RESCAN, false);
            boolean unblockEventsRun = getInputData().getBoolean(PhoneProfilesService.EXTRA_UNBLOCK_EVENTS_RUN, false);
            int logType = getInputData().getInt(PhoneProfilesService.EXTRA_LOG_TYPE, 0);

            //outputData = generateResult(LocationGeofenceEditorActivity.FAILURE_RESULT,
            //                                    getApplicationContext().getString(R.string.event_preferences_location_no_address_found),
            //                                    updateName);

            //return Result.success(outputData);


            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
            if (logType != PPApplication.ALTYPE_UNDEFINED)
                PPApplication.addActivityLog(context, logType, null, null, null, 0, "");
            //dataWrapper.restartEvents(unblockEventsRun, true, true, false);
            //PPApplication.logE("*********** restartEvents", "from RestartEventsWithDelayWorker.doWork()");
            dataWrapper.restartEventsWithRescan(alsoRescan, unblockEventsRun, false, false, true, false);
            //dataWrapper.invalidateDataWrapper();

            return Result.success();
        } catch (Exception e) {
            Log.e("RestartEventsWithDelayWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
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

    /*
    private Data generateResult(int resultCode, String message, boolean updateName) {
        // Create the output of the work
        PPApplication.logE("FetchAddressWorker.generateResult", "resultCode="+resultCode);
        PPApplication.logE("FetchAddressWorker.generateResult", "message="+message);
        PPApplication.logE("FetchAddressWorker.generateResult", "updateName="+updateName);

        return new Data.Builder()
                .putInt(LocationGeofenceEditorActivity.RESULT_CODE, resultCode)
                .putString(LocationGeofenceEditorActivity.RESULT_DATA_KEY, message)
                .putBoolean(LocationGeofenceEditorActivity.UPDATE_NAME_EXTRA, updateName)
                .build();
    }
    */

}
