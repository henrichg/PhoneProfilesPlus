package sk.henrichg.phoneprofilesplus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class GeofencesScanner implements GoogleApiClient.ConnectionCallbacks,
                                         GoogleApiClient.OnConnectionFailedListener,
                                         ResultCallback<Status>
{

    private GoogleApiClient mGoogleApiClient;
    private Context context;
    DataWrapper dataWrapper;

    protected ArrayList<com.google.android.gms.location.Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;

    // Bool to track whether the app is already resolving an error
    public boolean mResolvingError = false;
    // Request code to use when launching the resolution activity
    public static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    public static final String DIALOG_ERROR = "dialog_error";

    //private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    //private static final long GEOFENCE_EXPIRATION_IN_MILISECONDS =
    //        GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    public static final String GEOFENCE_KEY_PREFIX = "PhoneProfilesPlusGeofence";

    public GeofencesScanner(Context context) {
        this.context = context;
        dataWrapper = new DataWrapper(context, false, false, 0);

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void connect() {
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    };

    public void connectForResolve() {
        if (!mGoogleApiClient.isConnecting() &&
                !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        unregisterAllEventGeofences();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //Log.d("GeofencesScanner.onConnected", "xxx");
        registerAllEventGeofences();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
        //Log.d("GeofencesScanner.onConnectionSuspended", "xxx");
        //mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Log.d("GeofencesScanner.onConnectionFailed", "xxx");
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            /*try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }*/
            showErrorNotification(connectionResult.getErrorCode());
            mResolvingError = true;
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorNotification(connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofencesScannerService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void registerAllEventGeofences() {
        if (mGoogleApiClient.isConnected() && Permissions.checkLocation(context)) {
            //Log.d("GeofencesScanner.registerAllEventGeofences","xxx");

            // clear all geofence transitions
            dataWrapper.getDatabaseHandler().clearAllGeofenceTransitions();

            // Empty list for storing geofences.
            mGeofenceList = new ArrayList<com.google.android.gms.location.Geofence>();

            List<Geofence> geofences = dataWrapper.getDatabaseHandler().getAllGeofences();

            for (Geofence geofence : geofences) {
                if (dataWrapper.getDatabaseHandler().isGeofenceUsed(geofence._id, true)) {
                    mGeofenceList.add(new com.google.android.gms.location.Geofence.Builder()
                                    .setRequestId(GEOFENCE_KEY_PREFIX + "_" + String.valueOf(geofence._id))
                                    .setCircularRegion(geofence._latitude, geofence._longitude, geofence._radius)
                                    .setExpirationDuration(com.google.android.gms.location.Geofence.NEVER_EXPIRE)
                                    .setTransitionTypes(com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER |
                                            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT)
                                    .build()
                    );
                }
            }

            if (mGeofenceList.size() == 0)
                return;

            try {
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        // The GeofenceRequest object.
                        getGeofencingRequest(),
                        // A pending intent that that is reused when calling removeGeofences(). This
                        // pending intent is used to generate an intent when a matched geofence
                        // transition is observed.
                        getGeofencePendingIntent()
                ).setResultCallback(this); // Result processed in onResult().
            } catch (SecurityException securityException) {
                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            }
        }
    }

    public void unregisterAllEventGeofences() {
        if (mGoogleApiClient.isConnected()) {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        }
    }

    public void registerGeofenceForEvent(Event event) {
        if (mGoogleApiClient.isConnected() && Permissions.checkLocation(context)) {
            //Log.d("GeofencesScanner.registerGeofenceForEvent", "enabled="+event._eventPreferencesLocation._enabled);
            if ((event._eventPreferencesLocation != null) && (event._eventPreferencesLocation._enabled)) {
                //Log.d("GeofencesScanner.registerGeofenceForEvent", "geofenceId="+event._eventPreferencesLocation._geofenceId);

                Geofence geofence = dataWrapper.getDatabaseHandler().getGeofence(event._eventPreferencesLocation._geofenceId);
                if (geofence != null) {
                    dataWrapper.getDatabaseHandler().updateGeofenceTransition(event._eventPreferencesLocation._geofenceId, 0);

                    // Empty list for storing geofences.
                    mGeofenceList = new ArrayList<com.google.android.gms.location.Geofence>();
                    mGeofenceList.add(new com.google.android.gms.location.Geofence.Builder()
                                    .setRequestId(GEOFENCE_KEY_PREFIX + "_" + String.valueOf(geofence._id))
                                    .setCircularRegion(geofence._latitude, geofence._longitude, geofence._radius)
                                    .setExpirationDuration(com.google.android.gms.location.Geofence.NEVER_EXPIRE)
                                    .setTransitionTypes(com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER |
                                            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT)
                                    .build()
                    );

                    try {
                        LocationServices.GeofencingApi.addGeofences(
                                mGoogleApiClient,
                                // The GeofenceRequest object.
                                getGeofencingRequest(),
                                // A pending intent that that is reused when calling removeGeofences(). This
                                // pending intent is used to generate an intent when a matched geofence
                                // transition is observed.
                                getGeofencePendingIntent()
                        ).setResultCallback(this); // Result processed in onResult().
                    } catch (SecurityException securityException) {
                        // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                    }
                }
            }
        }
    }

    public void unregisterGeofenceForEvent(Event event) {
        if (mGoogleApiClient.isConnected()) {
            if (event._eventPreferencesLocation != null) {
                //Log.d("GeofencesScanner.unregisterGeofenceForEvent", "xxx");

                ArrayList<String> geofenceRequestIdList = new ArrayList<String>();
                geofenceRequestIdList.add(GEOFENCE_KEY_PREFIX + "_" + String.valueOf(event._eventPreferencesLocation._geofenceId));

                LocationServices.GeofencingApi.removeGeofences(
                        mGoogleApiClient,
                        geofenceRequestIdList
                ).setResultCallback(this); // Result processed in onResult().
            }

        }
    }

    //-------------------------------------------

    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     *
     * Since this activity implements the {@link ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {

        } else {
            Log.e("GeofencesScanner", "Error adding geofences: "+status.getStatusCode());
        }
    }

    //-------------------------------------------

    private void showErrorNotification(int errorCode) {
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_pphelper_upgrade_notify) // notification icon
                .setContentTitle(context.getString(R.string.event_preferences_location_google_api_connection_error_title)) // title for notification
                .setContentText(context.getString(R.string.app_name) + ": " +
                        context.getString(R.string.event_preferences_location_google_api_connection_error_text)) // message for notification
                                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(context, GeofenceScannerErrorActivity.class);
        intent.putExtra(DIALOG_ERROR, errorCode);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        if (android.os.Build.VERSION.SDK_INT >= 16)
            mBuilder.setPriority(Notification.PRIORITY_MAX);
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(GlobalData.GEOFENCE_SCANNER_ERROR_NOTIFICATION_ID, mBuilder.build());
    }

}
