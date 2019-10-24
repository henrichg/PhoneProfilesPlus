package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class FetchAddressWorker extends Worker {

    final Context context;

    public FetchAddressWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        PPApplication.logE("FetchAddressWorker.doWork", "xxx");
        Data outputData;

        boolean error = false;

        // Get the input
        // Get the location passed to this service through an extra.
        String sLocation = getInputData().getString(LocationGeofenceEditorActivity.LOCATION_DATA_EXTRA);
        Location location = null;
        if ((sLocation == null) || sLocation.isEmpty())
            error = true;
        else {
            location = deserializeLocation(sLocation);
            if (location == null)
                error = true;
        }
        PPApplication.logE("FetchAddressWorker.doWork", "error="+error);
        if (error) {
            outputData = generateResult(LocationGeofenceEditorActivity.FAILURE_RESULT, "No location data provided", false);
        }
        else {
            boolean updateName = getInputData().getBoolean(LocationGeofenceEditorActivity.UPDATE_NAME_EXTRA, false);

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(),
                        // In this sample, get just a single address.
                        1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                Log.e("FetchAddressWorker.doWork", "Service not available", ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                Log.e("FetchAddressWorker.doWork", "Invalid location. " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " +
                        location.getLongitude(), illegalArgumentException);
            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size()  == 0) {
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
                for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                String lineSeparator = System.getProperty("line.separator");
                if (lineSeparator == null)
                    lineSeparator = "\n";
                outputData = generateResult(LocationGeofenceEditorActivity.SUCCESS_RESULT,
                        TextUtils.join(lineSeparator, addressFragments),
                        updateName);
            }
        }

        //if (outputData == null)
        //    return Result.success();
        //else
            // Return the output
            return Result.success(outputData);
    }

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


    // Serialize a single object.
    static String serializeLocation(Location myClass) {
        Gson gson = new Gson();
        return gson.toJson(myClass);
    }

    // Deserialize to single object.
    private static Location deserializeLocation(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, Location.class);
    }

}
