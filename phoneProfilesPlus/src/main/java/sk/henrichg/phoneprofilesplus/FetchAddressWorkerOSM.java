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

public class FetchAddressWorkerOSM extends Worker {

    private final Context context;

    public FetchAddressWorkerOSM(
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
//            PPApplicationStatic.logE("[IN_WORKER]  FetchAddressWorkerOSM.doWork", "--------------- START");

            if (!PPApplicationStatic.getApplicationStarted(true, true))
                // application is not started
                return Result.success();

            Data outputData;

            // Get the input
            // Get the location passed to this service through an extra.
            double latitude = getInputData().getDouble(LocationGeofenceEditorActivityOSM.WORKRES_LATITUDE_EXTRA, 0);
            double longitude = getInputData().getDouble(LocationGeofenceEditorActivityOSM.WORKRES_LONGITUDE_EXTRA, 0);
            boolean updateName = getInputData().getBoolean(LocationGeofenceEditorActivityOSM.WORKRES_UPDATE_NAME_EXTRA, false);

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(latitude, longitude,
                        // In this sample, get just a single address.
                        1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                //Log.e("FetchAddressWorkerOSM.doWork", "Service not available", ioException);
                //PPApplicationStatic.recordException(e);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                /*Log.e("FetchAddressWorkerOSM.doWork", "Invalid location. " +
                        "Latitude = " + latitude +
                        ", Longitude = " +
                        longitude, illegalArgumentException);
                PPApplicationStatic.recordException(e);*/
            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size() == 0) {
                //if (errorMessage.isEmpty()) {
                //Log.e("FetchAddressWorkerOSM.doWork", "No address found");
                //}
                outputData = generateResult(LocationGeofenceEditorActivityOSM.WORKRES_FAILURE_RESULT,
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
                    lineSeparator = StringConstants.CHAR_NEW_LINE;
                outputData = generateResult(LocationGeofenceEditorActivityOSM.WORKRES_SUCCESS_RESULT,
                        TextUtils.join(lineSeparator, addressFragments),
                        updateName);
            }

            //if (outputData == null)
            //    return Result.success();
            //else
            // Return the output

//            long finish = System.currentTimeMillis();
//            long timeElapsed = finish - start;
//            PPApplicationStatic.logE("[IN_WORKER]  FetchAddressWorkerOSM.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success(outputData);
        } catch (Exception e) {
            //Log.e("FetchAddressWorkerOSM.doWork", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            return Result.failure();
        }
    }

    private Data generateResult(int resultCode, String message, boolean updateName) {
        // Create the output of the work

        return new Data.Builder()
                .putInt(LocationGeofenceEditorActivityOSM.WORKRES_RESULT_CODE, resultCode)
                .putString(LocationGeofenceEditorActivityOSM.WORKRES_RESULT_DATA_KEY, message)
                .putBoolean(LocationGeofenceEditorActivityOSM.WORKRES_UPDATE_NAME_EXTRA, updateName)
                .build();
    }


}
