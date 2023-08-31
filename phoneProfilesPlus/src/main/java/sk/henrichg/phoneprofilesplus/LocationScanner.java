package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

class LocationScanner
{
    private LocationManager mLocationManager;
    private final LocationScannerListener mLocationListener;
    private boolean mListenerEnabled = false;

    final Context context;
    //private final DataWrapper dataWrapper;

    static final int INTERVAL_DIVIDE_VALUE = 6;
    static final int INTERVAL_DIVIDE_VALUE_FOR_GPS = 3;

    LocationScanner(Context context) {
        this.context = context.getApplicationContext();
        //dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

        mLocationListener = new LocationScannerListener();
    }

    void connect(boolean resetUseGPS) {
        try {
            synchronized (PPApplication.locationScannerMutex) {
                //if (dataWrapper.getDatabaseHandler().getGeofenceCount() > 0)

                try {
                    if (mLocationManager == null)
                        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    if (resetUseGPS)
                        PPApplication.locationScannerUseGPS = true;

                    final Context appContext = context.getApplicationContext();
                    //PPApplication.startHandlerThreadPPScanners(/*"LocationScanner.onConnected"*/);
                    //final Handler __handler6 = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
                    //__handler6.post(new PPApplication.PPHandlerThreadRunnable(
                    //        context.getApplicationContext()) {
                    //__handler6.post(() -> {
                    Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=LocationScanner.connect");

                        //Context appContext= appContextWeakRef.get();
                        //if (appContext != null) {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_LocationScanner_connect);
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                synchronized (PPApplication.locationScannerMutex) {
                                    if ((PhoneProfilesService.getInstance() != null) && (PPApplication.locationScanner != null)) {
                                        PPApplication.locationScanner.clearAllEventGeofences();
                                        String provider = PPApplication.locationScanner.startLocationUpdates();
                                        PPApplication.locationScanner.updateTransitionsByLastKnownLocation(provider);
                                    }
                                }

                            } catch (SecurityException e) {
                                //
                            } catch (Exception e) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                PPApplicationStatic.recordException(e);
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        //}
                    }; //);
                    PPApplicationStatic.createScannersExecutor();
                    PPApplication.scannersExecutor.submit(runnable);
                } catch (Exception ee) {
                    //Log.e("##### LocationScanner.connect", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(ee);
                }

            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    void disconnect() {
        try {
            stopLocationUpdates();
            LocationScannerSwitchGPSBroadcastReceiver.removeAlarm(context);
            //useGPS = true; disconnect is called from screen on/off broadcast therefore not change this
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    void updateGeofencesInDB() {
        synchronized (PPApplication.locationScannerMutex) {
            if (PPApplication.lastLocation == null)
                return;

            List<Geofence> geofences = DatabaseHandler.getInstance(context).getAllGeofences();

            //boolean change = false;

            for (Geofence geofence : geofences) {

                Location geofenceLocation = new Location("GL");
                geofenceLocation.setLatitude(geofence._latitude);
                geofenceLocation.setLongitude(geofence._longitude);

                float distance;
                float radius;
                synchronized (PPApplication.locationScannerLastLocationMutex) {
                    Location _lastLocation = new Location("GL");
                    _lastLocation.setLatitude(PPApplication.lastLocation.getLatitude());
                    _lastLocation.setLongitude(PPApplication.lastLocation.getLongitude());

                    distance = Math.abs(_lastLocation.distanceTo(geofenceLocation));
                    radius = PPApplication.lastLocation.getAccuracy() + geofence._radius;
                }

                int transitionType;
                if (distance <= radius) {
                    transitionType = Geofence.GEOFENCE_TRANSITION_ENTER;
                }
                else {
                    transitionType = Geofence.GEOFENCE_TRANSITION_EXIT;
                }

                //int savedTransition = DatabaseHandler.getInstance(context).getGeofenceTransition(geofence._id);

                if (geofence._transition != transitionType) {
                    DatabaseHandler.getInstance(context).updateGeofenceTransition(geofence._id, transitionType);
                    //change = true;
                }
            }

            PPApplication.locationScannerTransitionsUpdated = true;
        }
    }

    void clearAllEventGeofences() {
        synchronized (PPApplication.locationScannerMutex) {
            // clear all geofence transitions
            DatabaseHandler.getInstance(context).clearAllGeofenceTransitions();
            PPApplication.locationScannerTransitionsUpdated = false;
        }
    }

    //-------------------------------------------

    String getProvider(boolean showNotification) {
        String provider;

        boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(context);
        if ((!ApplicationPreferences.applicationEventLocationUseGPS) || isPowerSaveMode || (!PPApplication.locationScannerUseGPS)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            provider = LocationManager.GPS_PROVIDER;
        }

        /*
        // do not force GPS when device is in power save mode
        // removed - consume battery
        boolean gpsForced = false;
        if (provider.equals(LocationManager.NETWORK_PROVIDER) &&
                (!CheckOnlineStatusBroadcastReceiver.isOnline(context)) &&
                (!isPowerSaveMode)) {
            // device is not connected to network, force GPS_PROVIDER
            provider = LocationManager.GPS_PROVIDER;
            gpsForced = true;
        }
        */

        boolean locationEnabled = false;

        if (mLocationManager != null) {

            // check if GPS provider is enabled in system settings
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                try {
                    locationEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if ((!locationEnabled)/* && (!gpsForced)*/)
                        provider = LocationManager.NETWORK_PROVIDER;
                } catch (Exception e) {
                    // we may get IllegalArgumentException if gps location provider
                    // does not exist or is not yet installed.
                    //locationEnabled = false;
                }
            }
            if (!locationEnabled) {
                // check if network provider is enabled in system settings
                if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                    try {
                        // if device is in power save mode, force NETWORK_PROVIDER
                        locationEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                                (isPowerSaveMode);
                    } catch (Exception e) {
                        // we may get IllegalArgumentException if network location provider
                        // does not exist or is not yet installed.
                        //locationEnabled = false;
                    }
                }
            }
            if (!locationEnabled) {
                if (isPowerSaveMode) {
                    // in power save mode force NETWORK_PROVIDER
                    provider = LocationManager.NETWORK_PROVIDER;
                    locationEnabled = true;
                } else {
                    // get best provider
                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    //criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
                    provider = mLocationManager.getBestProvider(criteria, false);
                    locationEnabled = (provider != null) && (!provider.isEmpty());
                }
            }

        }

        if (!locationEnabled) {
            provider = "";
            if (showNotification)
                showNotification();
        }

        return provider;
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    String startLocationUpdates() {
        boolean scanningPaused = ApplicationPreferences.applicationEventLocationScanInTimeMultiply.equals("2") &&
                GlobalUtils.isNowTimeBetweenTimes(
                        ApplicationPreferences.applicationEventLocationScanInTimeMultiplyFrom,
                        ApplicationPreferences.applicationEventLocationScanInTimeMultiplyTo);

        if ((!ApplicationPreferences.applicationEventLocationEnableScanning) || scanningPaused)
            return "";

        String provider = "";

        if ((!PPApplication.locationScannerUpdatesStarted) /*|| mUpdateTransitionsByLastKnownLocationIsRunning*/) {
            synchronized (PPApplication.locationScannerMutex) {
                try {
                    if (Permissions.checkLocation(context)) {
                        if (!mListenerEnabled) {

                            provider = getProvider(true);

                            if (!provider.isEmpty()) {
                                try {
                                    // check power save mode
                                    String applicationEventLocationUpdateInPowerSaveMode = ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode;
                                    boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(context);
                                    boolean canScan = true;
                                    if (isPowerSaveMode && applicationEventLocationUpdateInPowerSaveMode.equals("2")) {
                                        canScan = false;
                                    }
                                    else {
                                        if (ApplicationPreferences.applicationEventLocationScanInTimeMultiply.equals("2")) {
                                            if (GlobalUtils.isNowTimeBetweenTimes(
                                                    ApplicationPreferences.applicationEventLocationScanInTimeMultiplyFrom,
                                                    ApplicationPreferences.applicationEventLocationScanInTimeMultiplyTo)) {
                                                // not scan wi-fi in configured time
                                                canScan = false;
                                            }
                                        }
                                    }
                                    if (canScan) {
                                        int interval = 25; // seconds
                                        if (ApplicationPreferences.applicationEventLocationUpdateInterval > 1) {
                                            // interval is in minutes
                                            if (!CheckOnlineStatusBroadcastReceiver.isOnline(context))
                                                interval = (ApplicationPreferences.applicationEventLocationUpdateInterval * 60) / INTERVAL_DIVIDE_VALUE_FOR_GPS;
                                            else
                                                interval = (ApplicationPreferences.applicationEventLocationUpdateInterval * 60) / INTERVAL_DIVIDE_VALUE;
                                        }
                                        if (isPowerSaveMode && applicationEventLocationUpdateInPowerSaveMode.equals("1"))
                                            interval = 2 * interval;
                                        final long UPDATE_INTERVAL_IN_MILLISECONDS = (interval * 1000L) / 2;

                                        PPApplicationStatic.startHandlerThreadLocation();
                                        mLocationManager.requestLocationUpdates(provider, UPDATE_INTERVAL_IN_MILLISECONDS, 10, mLocationListener, PPApplication.handlerThreadLocation.getLooper());

                                        mListenerEnabled = true;

                                        PPApplication.locationScannerUpdatesStarted = true;
                                    }
                                    else
                                        PPApplicationStatic.cancelWork(LocationSensorWorker.LOCATION_SENSOR_WORK_TAG, false);

                                } catch (SecurityException securityException) {
                                    // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                                    PPApplication.locationScannerUpdatesStarted = false;
                                    return "";
                                }
                            }
                            //else {
                            //    mListenerEnabled = true;
                            //}
                        }
                    }
                } catch (Exception e) {
                    PPApplication.locationScannerUpdatesStarted = false;
                }
            }
        }

        if (!mListenerEnabled)
            provider = "";

        //if (ApplicationPreferences.applicationEventLocationUseGPS &&
        //        CheckOnlineStatusBroadcastReceiver.isOnline(context)) {
            // recursive call this for switch usage of GPS
        LocationScannerSwitchGPSBroadcastReceiver.setAlarm(context);
        //}
        //else
        //    LocationScannerSwitchGPSBroadcastReceiver.removeAlarm(context);

        return provider;
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        if (PPApplication.locationScannerUpdatesStarted) {
            synchronized (PPApplication.locationScannerMutex) {
                if (mListenerEnabled) {
                    try {
                        PPApplicationStatic.cancelWork(LocationSensorWorker.LOCATION_SENSOR_WORK_TAG, false);

                        if (mLocationManager != null)
                            mLocationManager.removeUpdates(mLocationListener);
                        mListenerEnabled = false;
                        PPApplication.locationScannerUpdatesStarted = false;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            }
        }
    }

    void updateTransitionsByLastKnownLocation(String provider) {
        if (mLocationManager != null) {
            try {
                Location location;
                if (provider.isEmpty()) {
                    if (PPApplication.locationScannerUseGPS)
                        location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    else
                        location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                } else {
                    location = mLocationManager.getLastKnownLocation(provider);
                }

//            PPApplicationStatic.logE("LocationScanner.updateTransitionsByLastKnownLocation", "LocationSensorWorker.enqueueWork + doLocationChanged");
                LocationSensorWorker.enqueueWork(true, context);
                doLocationChanged(location, true);
            } catch (SecurityException e) {
                //
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        }
    }

    static void doLocationChanged(Location location, boolean callEventsHandler) {
        if (location == null)
            return;


        if ((!location.hasAccuracy()))
            return;

        if (location.getAccuracy() > 500)
            return;

        boolean scanningPaused = ApplicationPreferences.applicationEventLocationScanInTimeMultiply.equals("2") &&
                GlobalUtils.isNowTimeBetweenTimes(
                        ApplicationPreferences.applicationEventLocationScanInTimeMultiplyFrom,
                        ApplicationPreferences.applicationEventLocationScanInTimeMultiplyTo);
        if (scanningPaused)
            return;

        synchronized (PPApplication.locationScannerLastLocationMutex) {
            if (PPApplication.lastLocation == null) {
                PPApplication.lastLocation = new Location("GL");
            }
            PPApplication.lastLocation.set(location);
            //PPApplicationStatic.logE("[IN_LISTENER] LocationScanner.doLocationChanged", "lastLocation=" + lastLocation);
        }

        synchronized (PPApplication.locationScannerMutex) {
            if (EventStatic.getGlobalEventsRunning(PPApplication.locationScanner.context)) {
                if ((PhoneProfilesService.getInstance() != null) && (PPApplication.locationScanner != null)) {
                    PPApplication.locationScanner.updateGeofencesInDB();

                    if (callEventsHandler) {
//                        PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] LocationScanner.doLocationChanged", "sensorType=SENSOR_TYPE_LOCATION_SCANNER");
                        PPExecutors.handleEvents(PPApplication.locationScanner.context, EventsHandler.SENSOR_TYPE_LOCATION_SCANNER, PPExecutors.SENSOR_NAME_SENSOR_TYPE_LOCATION_SCANNER, 0);
                    }
                }
            }
        }
    }

    static void onlineStatusChanged(Context context) {
        if (PhoneProfilesService.getInstance() != null) {
            synchronized (PPApplication.locationScannerMutex) {
                if (PPApplication.locationScanner != null) {
                    if (ApplicationPreferences.applicationEventLocationUseGPS &&
                            (!CheckOnlineStatusBroadcastReceiver.isOnline(context))) {
                        // device is not online

//                        if (PPApplication.lastLocation == null) {
//                            PPApplication.lastLocation = new Location("GL");
//                        }
//
//                        doLocationChanged(PPApplication.lastLocation, true);

                        PPApplication.locationScanner.stopLocationUpdates();

                        GlobalUtils.sleep(1000);

                        // force useGPS
                        PPApplication.locationScannerUseGPS = true;

                        // this also calls LocationScannerSwitchGPSBroadcastReceiver.setAlarm()
                        String provider = PPApplication.locationScanner.startLocationUpdates();
                        PPApplication.locationScanner.updateTransitionsByLastKnownLocation(provider);

                    } else {
                        // device is online

                        PPApplication.locationScanner.stopLocationUpdates();

                        GlobalUtils.sleep(1000);

                        // this also calls LocationScannerSwitchGPSBroadcastReceiver.setAlarm()
                        String provider = PPApplication.locationScanner.startLocationUpdates();
                        PPApplication.locationScanner.updateTransitionsByLastKnownLocation(provider);
                    }
                }
            }
        }
    }

    private void showNotification() {
        String nText = context.getString(R.string.location_scanner_location_not_working_notification_text);

        PPApplicationStatic.createExclamationNotificationChannel(context.getApplicationContext(), false);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context.getApplicationContext(), PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.error_color))
                .setSmallIcon(R.drawable.ic_ppp_notification/*ic_exclamation_notify*/) // notification icon
                .setLargeIcon(BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.ic_exclamation_notification))
                .setContentTitle(context.getString(R.string.location_scanner_location_not_working_notification_title)) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));

        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);

        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        mBuilder.setGroup(PPApplication.SYTEM_CONFIGURATION_ERRORS_NOTIFICATION_GROUP);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            mNotificationManager.notify(
                    PPApplication.LOCATION_NOT_WORKING_NOTIFICATION_TAG,
                    PPApplication.LOCATION_NOT_WORKING_NOTIFICATION_ID, mBuilder.build());
        } catch (SecurityException en) {
            Log.e("LocationScanner.showNotification", Log.getStackTraceString(en));
        } catch (Exception e) {
            //Log.e("LocationScanner.showNotification", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }

}
