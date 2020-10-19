package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public class FetchAddressWorker extends Worker {

    private final Context context;

    public FetchAddressWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            PPApplication.logE("[IN_WORKER]  FetchAddressWorker.doWork", "xxxx");

            if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return Result.success();

            Data outputData;

            // Get the input
            // Get the location passed to this service through an extra.
            double latitude = getInputData().getDouble(LocationGeofenceEditorActivity.LATITUDE_EXTRA, 0);
            double longitude = getInputData().getDouble(LocationGeofenceEditorActivity.LONGITUDE_EXTRA, 0);
            boolean updateName = getInputData().getBoolean(LocationGeofenceEditorActivity.UPDATE_NAME_EXTRA, false);

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(latitude, longitude,
                        // In this sample, get just a single address.
                        1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                //Log.e("FetchAddressWorker.doWork", "Service not available", ioException);
                //PPApplication.recordException(e);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                /*Log.e("FetchAddressWorker.doWork", "Invalid location. " +
                        "Latitude = " + latitude +
                        ", Longitude = " +
                        longitude, illegalArgumentException);
                PPApplication.recordException(e);*/
            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size() == 0) {
                //if (errorMessage.isEmpty()) {
                //Log.e("FetchAddressIntentService.onHandleIntent", "No address found");
                //}
                outputData = generateResult(LocationGeofenceEditorActivity.FAILURE_RESULT,
                        getApplicationContext().getString(R.string.event_preferences_location_no_address_found),
                        updateName);
            } else {
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread.
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                String lineSeparator = System.getProperty("line.separator");
                if (lineSeparator == null)
                    lineSeparator = "\n";
                outputData = generateResult(LocationGeofenceEditorActivity.SUCCESS_RESULT,
                        TextUtils.join(lineSeparator, addressFragments),
                        updateName);
            }

            //if (outputData == null)
            //    return Result.success();
            //else
            // Return the output
            return Result.success(outputData);
        } catch (Exception e) {
            //Log.e("FetchAddressWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            return Result.failure();
        }
    }

    private Data generateResult(int resultCode, String message, boolean updateName) {
        // Create the output of the work
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("FetchAddressWorker.generateResult", "resultCode=" + resultCode);
            PPApplication.logE("FetchAddressWorker.generateResult", "message=" + message);
            PPApplication.logE("FetchAddressWorker.generateResult", "updateName=" + updateName);
        }*/

        return new Data.Builder()
                .putInt(LocationGeofenceEditorActivity.RESULT_CODE, resultCode)
                .putString(LocationGeofenceEditorActivity.RESULT_DATA_KEY, message)
                .putBoolean(LocationGeofenceEditorActivity.UPDATE_NAME_EXTRA, updateName)
                .build();
    }


}
