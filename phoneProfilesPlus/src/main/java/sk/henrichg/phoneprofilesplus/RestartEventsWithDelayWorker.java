package sk.henrichg.phoneprofilesplus;

import android.content.Context;

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

        PPApplication.logE("RestartEventsWithDelayWorker.doWork", "xxx");
        PPApplication.logE("[TEST HANDLER] DataWrapper.restartEventsWithDelay", "restart from handler");

        //Data outputData;

        // Get the input
        boolean unblockEventsRun = getInputData().getBoolean(PhoneProfilesService.EXTRA_UNBLOCK_EVENTS_RUN, false);
        int logType = getInputData().getInt(PhoneProfilesService.EXTRA_LOG_TYPE, 0);
        PPApplication.logE("[TEST HANDLER] DataWrapper.restartEventsWithDelay", "unblockEventsRun="+unblockEventsRun);
        PPApplication.logE("[TEST HANDLER] DataWrapper.restartEventsWithDelay", "logType="+logType);

        //outputData = generateResult(LocationGeofenceEditorActivity.FAILURE_RESULT,
        //                                    getApplicationContext().getString(R.string.event_preferences_location_no_address_found),
        //                                    updateName);

        //return Result.success(outputData);


        DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);
        if (logType != DataWrapper.ALTYPE_UNDEFINED)
            dataWrapper.addActivityLog(logType, null, null, null, 0);
        //dataWrapper.restartEvents(unblockEventsRun, true, true, false);
        dataWrapper.restartEventsWithRescan(/*true, */unblockEventsRun, false, true, false);

        return Result.success();
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
