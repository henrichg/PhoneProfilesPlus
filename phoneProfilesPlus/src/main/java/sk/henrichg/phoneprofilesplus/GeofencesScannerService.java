package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofencesScannerService extends IntentService {

    public GeofencesScannerService() {
        super("GeofencesScannerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                Log.e("GeofencesScannerService", "Error getting geofence event: " + geofencingEvent.getErrorCode());
                return;
            }

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT) {

                // Get the geofences that were triggered. A single event can trigger multiple geofences.
                List<com.google.android.gms.location.Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                // save transitions to geofence table
                if (updateGeofences(geofenceTransition, triggeringGeofences)) {
                    // send broadcast for calling EventsService
                    Intent broadcastIntent = new Intent(getApplicationContext(), GeofenceScannerBroadcastReceiver.class);
                    sendBroadcast(broadcastIntent);
                }

                // Get the transition details as a String.
                String geofenceTransitionDetails = getGeofenceTransitionDetails(
                        this,
                        geofenceTransition,
                        triggeringGeofences
                );
                Log.d("GeofencesScannerService", geofenceTransitionDetails);
            } else {
                // Log the error.
                Log.e("GeofencesScannerService", "Invalid geofence transition type: "+geofenceTransition);
            }
        }
    }

    private boolean updateGeofences(int geofenceTransition,
                                 List<com.google.android.gms.location.Geofence> triggeringGeofences) {

        for (com.google.android.gms.location.Geofence geofence : triggeringGeofences) {

            String geofenceRequestId = geofence.getRequestId();
            Log.d("GeofencesScannerService.updateGeofences", "geofenceRequestId="+geofenceRequestId);

            String[] splits = geofenceRequestId.split("_");
            Log.d("GeofencesScannerService.updateGeofences", "splits[0]="+splits[0]);
            Log.d("GeofencesScannerService.updateGeofences", "splits[1]="+splits[1]);

            if (splits[0].equals(GeofencesScanner.GEOFENCE_KEY_PREFIX)) {
                try {
                    long geofenceId = Long.parseLong(splits[1]);
                    Log.d("GeofencesScannerService.updateGeofences", "geofenceId="+geofenceId);

                    DataWrapper dataWrapper  = new DataWrapper(getApplicationContext(), false, false, 0);
                    dataWrapper.getDatabaseHandler().updateGeofenceTransition(geofenceId, geofenceTransition);

                    return true;
                } catch (Exception e) {
                }
            }
        }
        return false;
    }


    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param context               The app context.
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<com.google.android.gms.location.Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (com.google.android.gms.location.Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Entered";
            case com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Exited";
            default:
                return "Unknown";
        }
    }
}
