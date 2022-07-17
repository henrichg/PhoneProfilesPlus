package sk.henrichg.phoneprofilesplus;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

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

    static boolean useGPS = true; // must be static
    static boolean mUpdatesStarted = false; // must be static

    static boolean mTransitionsUpdated = false;

    static final int INTERVAL_DIVIDE_VALUE = 6;
    static final int INTERVAL_DIVIDE_VALUE_FOR_GPS = 3;

    LocationScanner(Context context) {
        this.context = context;
        //dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

        mLocationListener = new LocationScannerListener();
    }

    void connect(boolean resetUseGPS) {
//        PPApplication.logE("##### LocationScanner.connect", "xxx");
        /*if (PPApplication.logEnabled()) {
            if (PhoneProfilesService.getInstance() != null)
                PPApplication.logE("##### LocationScanner.connect", "PhoneProfilesService.isLocationScannerStarted()=" + PhoneProfilesService.getInstance().isLocationScannerStarted());
        }*/
        try {
            synchronized (PPApplication.locationScannerMutex) {
                //if (dataWrapper.getDatabaseHandler().getGeofenceCount() > 0)

                /*if (PPApplication.logEnabled()) {
                    if (PhoneProfilesService.getInstance() != null)
                        PPApplication.logE("##### LocationScanner.onConnected", "PhoneProfilesService.isLocationScannerStarted()=" + PhoneProfilesService.getInstance().isLocationScannerStarted());
                }*/
                try {
                    //PPApplication.logE("##### LocationScanner.onConnected", "xxx2");
                    if (mLocationManager == null)
                        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    if (resetUseGPS)
                        useGPS = true;

                    final Context appContext = context.getApplicationContext();
                    //PPApplication.startHandlerThreadPPScanners(/*"LocationScanner.onConnected"*/);
                    //final Handler __handler6 = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
                    //__handler6.post(new PPApplication.PPHandlerThreadRunnable(
                    //        context.getApplicationContext()) {
                    //__handler6.post(() -> {
                    Runnable runnable = () -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=LocationScanner.connect");

                        //Context appContext= appContextWeakRef.get();
                        //if (appContext != null) {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":LocationScanner_connect");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isLocationScannerStarted()) {
                                    LocationScanner scanner = PhoneProfilesService.getInstance().getLocationScanner();
                                    if (scanner != null) {
                                        scanner.clearAllEventGeofences();
                                        //PPApplication.logE("##### LocationScanner.onConnected", "updateTransitionsByLastKnownLocation");
                                        String provider = scanner.startLocationUpdates();
                                        scanner.updateTransitionsByLastKnownLocation(provider);
                                    }
                                }

                                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=LocationScanner.onConnected");
                            } catch (SecurityException e) {
                                //
                            } catch (Exception e) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                PPApplication.recordException(e);
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
                    PPApplication.createScannersExecutor();
                    PPApplication.scannersExecutor.submit(runnable);
                } catch (Exception ee) {
                    //Log.e("##### LocationScanner.onConnected", Log.getStackTraceString(e));
                    PPApplication.recordException(ee);
                }

            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    void disconnect() {
        //PPApplication.logE("##### LocationScanner.disconnect", "xxx");
        /*if (PPApplication.logEnabled()) {
            if (PhoneProfilesService.getInstance() != null)
                PPApplication.logE("##### LocationScanner.disconnect", "PhoneProfilesService.isLocationScannerStarted()=" + PhoneProfilesService.getInstance().isLocationScannerStarted());
        }*/
        try {
            stopLocationUpdates();
            LocationScannerSwitchGPSBroadcastReceiver.removeAlarm(context);
            //useGPS = true; disconnect is called from screen on/off broadcast therefore not change this
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    void updateGeofencesInDB() {
        synchronized (PPApplication.locationScannerMutex) {
//            PPApplication.logE("#####   LocationScanner.updateGeofencesInDB", "xxx");
            /*if (PPApplication.logEnabled()) {
                if (PhoneProfilesService.getInstance() != null)
                    PPApplication.logE("[***] LocationScanner.updateGeofencesInDB", "PhoneProfilesService.isLocationScannerStarted()=" + PhoneProfilesService.getInstance().isLocationScannerStarted());
            }*/

            if (PPApplication.lastLocation == null)
                return;

            List<Geofence> geofences = DatabaseHandler.getInstance(context).getAllGeofences();
//            PPApplication.logE("#####   LocationScanner.updateGeofencesInDB", "geofences.size="+geofences.size());

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

//                    if (PPApplication.logEnabled()) {
//                        PPApplication.logE("#####  LocationScanner.updateGeofencesInDB", "geofence._name=" + geofence._name);
//                        PPApplication.logE("#####  LocationScanner.updateGeofencesInDB", "distance=" + distance);
//                        PPApplication.logE("#####  LocationScanner.updateGeofencesInDB", "radius=" + radius);
//                    }
                }

                int transitionType;
                if (distance <= radius) {
                    transitionType = Geofence.GEOFENCE_TRANSITION_ENTER;
//                    PPApplication.logE("#####  LocationScanner.updateGeofencesInDB", "transition=ENTER");
                }
                else {
                    transitionType = Geofence.GEOFENCE_TRANSITION_EXIT;
//                    PPApplication.logE("#####  LocationScanner.updateGeofencesInDB", "transition=EXIT");
                }

//                if (geofence._transition == Geofence.GEOFENCE_TRANSITION_ENTER)
//                    PPApplication.logE("#####  LocationScanner.updateGeofencesInDB", "geofence._transition=GEOFENCE_TRANSITION_ENTER");
//                else if (geofence._transition == Geofence.GEOFENCE_TRANSITION_EXIT)
//                    PPApplication.logE("#####  LocationScanner.updateGeofencesInDB", "geofence._transition=GEOFENCE_TRANSITION_EXIT");
//                else
//                    PPApplication.logE("#####  LocationScanner.updateGeofencesInDB", "geofence._transition=0");

                //int savedTransition = DatabaseHandler.getInstance(context).getGeofenceTransition(geofence._id);

                //PPApplication.logE("##### LocationScanner.updateGeofencesInDB", "geofence._name=" + geofence._name);

                if (geofence._transition != transitionType) {
//                    PPApplication.logE("#####  LocationScanner.updateGeofencesInDB", "transition changed");
                    DatabaseHandler.getInstance(context).updateGeofenceTransition(geofence._id, transitionType);
                    //change = true;
                }
                //else
                //    PPApplication.logE("#####  LocationScanner.updateGeofencesInDB", "geofence._transition == transitionType");
            }

            mTransitionsUpdated = true;
        }
    }

    void clearAllEventGeofences() {
        synchronized (PPApplication.locationScannerMutex) {
            // clear all geofence transitions
            DatabaseHandler.getInstance(context).clearAllGeofenceTransitions();
            mTransitionsUpdated = false;
        }
    }

    //-------------------------------------------

    String getProvider() {
        String provider;

        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if ((!ApplicationPreferences.applicationEventLocationUseGPS) || isPowerSaveMode || (!useGPS)) {
//            PPApplication.logE("##### LocationScanner.getProvider","NETWORK_PROVIDER");
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
//            PPApplication.logE("##### LocationScanner.getProvider","GPS_PROVIDER");
            provider = LocationManager.GPS_PROVIDER;
        }

        /*
        // do not force GPS when device is in power save mode
        // removed - consume battery
        boolean gpsForced = false;
        if (provider.equals(LocationManager.NETWORK_PROVIDER) &&
                (!CheckOnlineStatusBroadcastReceiver.isOnline(context)) &&
                (!isPowerSaveMode)) {
//            PPApplication.logE("##### LocationScanner.getProvider", "NOT ONLINE");
            // device is not connected to network, force GPS_PROVIDER
            provider = LocationManager.GPS_PROVIDER;
            gpsForced = true;
        }
        */

        boolean locationEnabled = false;
        // check if GPS provider is enabled in system settings
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            try {
                locationEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//                PPApplication.logE("##### LocationScanner.getProvider","GPS_PROVIDER="+locationEnabled);
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
//                    PPApplication.logE("##### LocationScanner.getProvider","NETWORK_PROVIDER="+locationEnabled);
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
            }
            else {
                // get best provider
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                //criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
                provider = mLocationManager.getBestProvider(criteria, false);
                locationEnabled = (provider != null) && (!provider.isEmpty());
            }
        }

        if (!locationEnabled) {
            provider = "";
            showNotification();
        }

//        PPApplication.logE("##### LocationScanner.getProvider","provider="+provider);

        return provider;
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    String startLocationUpdates() {
        if (!ApplicationPreferences.applicationEventLocationEnableScanning)
            return "";

//        if (PPApplication.logEnabled()) {
//            if (PPApplication.getInstance() != null)
//                PPApplication.logE("##### LocationScanner.startLocationUpdates", "PhoneProfilesService.isLocationScannerStarted()=" + PhoneProfilesService.getInstance().isLocationScannerStarted());
//        }

        String provider = "";

        if ((!mUpdatesStarted) /*|| mUpdateTransitionsByLastKnownLocationIsRunning*/) {
            synchronized (PPApplication.locationScannerMutex) {
                try {
                    if (Permissions.checkLocation(context)) {
                        if (!mListenerEnabled) {

                            provider = getProvider();

                            if (!provider.isEmpty()) {
                                try {
                                    // check power save mode
                                    String applicationEventLocationUpdateInPowerSaveMode = ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode;
                                    boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                                    boolean canScan = true;
                                    if (isPowerSaveMode && applicationEventLocationUpdateInPowerSaveMode.equals("2")) {
                                        canScan = false;
                                    }
                                    else {
                                        if (ApplicationPreferences.applicationEventLocationScanInTimeMultiply.equals("2")) {
                                            if (PhoneProfilesService.isNowTimeBetweenTimes(
                                                    ApplicationPreferences.applicationEventLocationScanInTimeMultiplyFrom,
                                                    ApplicationPreferences.applicationEventLocationScanInTimeMultiplyTo)) {
                                                // not scan wi-fi in configured time
//                                                PPApplication.logE("LocationScanner.startLocationUpdates", "-- END - scan in time = 2 -------");
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
//                                        PPApplication.logE("##### LocationScanner.startLocationUpdates", "ApplicationPreferences.applicationEventLocationUpdateInterval="+ApplicationPreferences.applicationEventLocationUpdateInterval);
//                                        PPApplication.logE("##### LocationScanner.startLocationUpdates", "interval="+interval);
                                        if (isPowerSaveMode && applicationEventLocationUpdateInPowerSaveMode.equals("1"))
                                            interval = 2 * interval;
                                        final long UPDATE_INTERVAL_IN_MILLISECONDS = (interval * 1000L) / 2;

//                                        PPApplication.logE("##### LocationScanner.startLocationUpdates", "request location updates - provider=" + provider);
//                                        PPApplication.logE("##### LocationScanner.startLocationUpdates", "request location updates - interval=" + UPDATE_INTERVAL_IN_MILLISECONDS / 1000);
                                        PPApplication.startHandlerThreadLocation();
                                        mLocationManager.requestLocationUpdates(provider, UPDATE_INTERVAL_IN_MILLISECONDS, 10, mLocationListener, PPApplication.handlerThreadLocation.getLooper());

                                        mListenerEnabled = true;

                                        mUpdatesStarted = true;
                                    }
                                    else
                                        PPApplication.cancelWork(LocationSensorWorker.LOCATION_SENSOR_WORK_TAG, false);

                                } catch (SecurityException securityException) {
                                    // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                                    //PPApplication.logE("##### LocationScanner.startLocationUpdates", "mUpdatesStarted=false");
                                    mUpdatesStarted = false;
                                    return "";
                                }
                            }
                            //else {
//                                PPApplication.logE("##### LocationScanner.startLocationUpdates", "location not allowed");
                            //    mListenerEnabled = true;
                            //}
                        }
                    }
                } catch (Exception e) {
                    //PPApplication.logE("##### LocationScanner.startLocationUpdates", "mUpdatesStarted=false");
                    mUpdatesStarted = false;
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

//        if (PPApplication.logEnabled()) {
//            if (PPApplication.getInstance() != null)
//                PPApplication.logE("##### LocationScanner.stopLocationUpdates", "PhoneProfilesService.isLocationScannerStarted()=" + PhoneProfilesService.getInstance().isLocationScannerStarted());
//        }

        if (mUpdatesStarted) {
            synchronized (PPApplication.locationScannerMutex) {
                if (mListenerEnabled) {
                    try {
                        PPApplication.cancelWork(LocationSensorWorker.LOCATION_SENSOR_WORK_TAG, false);

                        if (mLocationManager != null)
                            mLocationManager.removeUpdates(mLocationListener);
                        mListenerEnabled = false;
                        mUpdatesStarted = false;
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
            }
        }
    }

    void updateTransitionsByLastKnownLocation(String provider) {
//        PPApplication.logE("##### LocationScanner.updateTransitionsByLastKnownLocation","xxxx");

        try {
            Location location;
            if (provider.isEmpty()) {
                if (useGPS)
                    location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                else
                    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else {
                location = mLocationManager.getLastKnownLocation(provider);
            }

            LocationSensorWorker.enqueueWork(true, context);
            doLocationChanged(location, true);
        } catch (SecurityException e) {
            //
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void doLocationChanged(Location location, boolean callEventsHandler) {
//        PPApplication.logE("##### LocationScanner.doLocationChanged", "callEventsHandler="+callEventsHandler);
        if (location == null)
            return;


        if ((!location.hasAccuracy()))
            return;

        if (location.getAccuracy() > 500)
            return;

        //PPApplication.logE("LocationScanner.doLocationChanged", "location="+location);

        synchronized (PPApplication.locationScannerLastLocationMutex) {
            //PPApplication.logE("##### LocationScanner.doLocationChanged", "lastLocation update");
            if (PPApplication.lastLocation == null) {
                PPApplication.lastLocation = new Location("GL");
            }
            PPApplication.lastLocation.set(location);
            //PPApplication.logE("[IN_LISTENER] LocationScanner.doLocationChanged", "lastLocation=" + lastLocation);
            //PPApplication.logE("##### LocationScanner.doLocationChanged", "lastLocation=" + lastLocation);
//            if (PPApplication.logEnabled()) {
//                PPApplication.logE("##### LocationScanner.doLocationChanged", "lastLocation.getProvider()=" + location.getProvider());
//                PPApplication.logE("##### LocationScanner.doLocationChanged", "lastLocation.getLatitude()=" + location.getLatitude());
//                PPApplication.logE("##### LocationScanner.doLocationChanged", "lastLocation.getLongitude()=" + location.getLongitude());
//                PPApplication.logE("##### LocationScanner.doLocationChanged", "lastLocation.getAccuracy()=" + location.getAccuracy());
//            }
        }

        if (Event.getGlobalEventsRunning()) {
            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isLocationScannerStarted()) {
//                PPApplication.logE("##### LocationScanner.doLocationChanged", "updateGeofencesInDB");
                LocationScanner scanner = PhoneProfilesService.getInstance().getLocationScanner();
                scanner.updateGeofencesInDB();
                /*if (useGPS) {
                    // location is from enabled GPS, disable it
                    LocationScannerSwitchGPSBroadcastReceiver.setAlarm(scanner.context);
                }*/
                //}
                //if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isLocationScannerStarted()) {
                //LocationScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
//                PPApplication.logE("##### LocationScanner.doLocationChanged", "handleEvents");

                if (callEventsHandler) {
//                    PPApplication.logE("[EVENTS_HANDLER] LocationScanner.doLocationChanged", "sensorType=SENSOR_TYPE_LOCATION_SCANNER");
                    EventsHandler eventsHandler = new EventsHandler(scanner.context);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_LOCATION_SCANNER);
                }
            }
        }
    }

    static class LocationScannerListener implements LocationListener {

        public void onLocationChanged(Location location) {
//            PPApplication.logE("[IN_LISTENER] LocationScanner.LocationScannerListener.onLocationChanged", "xxx");
            doLocationChanged(location, false);
        }

        public void onProviderDisabled(String provider) {
//            PPApplication.logE("[IN_LISTENER] LocationScanner.LocationScannerListener.onProviderDisabled", "xxx");
        }

        public void onProviderEnabled(String provider) {
//            PPApplication.logE("[IN_LISTENER] LocationScanner.LocationScannerListener.onProviderEnabled", "xxx");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
//            PPApplication.logE("[IN_LISTENER] LocationScanner.LocationScannerListener.onStatusChanged", "xxx");
        }
    }

    static void onlineStatusChanged(Context context) {
        if (PhoneProfilesService.getInstance() != null) {
            PhoneProfilesService serviceInstance = PhoneProfilesService.getInstance();
            LocationScanner scanner = serviceInstance.getLocationScanner();
            if (scanner != null) {
                if (serviceInstance.isLocationScannerStarted()) {

//                    PPApplication.logE("LocationScanner.onlineStatusChanged", "xxx");

                    if (ApplicationPreferences.applicationEventLocationUseGPS &&
                            (!CheckOnlineStatusBroadcastReceiver.isOnline(context))) {
                        // device is not online
//                        PPApplication.logE("LocationScanner.onlineStatusChanged", "NOT ONLINE");

//                        if (PPApplication.lastLocation == null) {
//                            PPApplication.lastLocation = new Location("GL");
//                        }
//
//                        doLocationChanged(PPApplication.lastLocation, true);

                        scanner.stopLocationUpdates();

                        PPApplication.sleep(1000);

                        // force useGPS
                        LocationScanner.useGPS = true;

                        // this also calls LocationScannerSwitchGPSBroadcastReceiver.setAlarm()
                        String provider = scanner.startLocationUpdates();
                        scanner.updateTransitionsByLastKnownLocation(provider);

                    } else {
                        // device is online
//                        PPApplication.logE("LocationScanner.onlineStatusChanged", "ONLINE");

                        scanner.stopLocationUpdates();

                        PPApplication.sleep(1000);

                        // this also calls LocationScannerSwitchGPSBroadcastReceiver.setAlarm()
                        String provider = scanner.startLocationUpdates();
                        scanner.updateTransitionsByLastKnownLocation(provider);
                    }
                }
            }
        }
    }

    private void showNotification() {
        String nText = context.getString(R.string.location_scanner_location_not_working_notification_text);

        PPApplication.createExclamationNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(context.getString(R.string.location_scanner_location_not_working_notification_title)) // title for notification
                .setContentText(nText) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));

        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);

        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}

        mBuilder.setGroup(PPApplication.SYTEM_CONFIGURATION_ERRORS_NOTIFICATION_GROUP);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            mNotificationManager.notify(
                    PPApplication.LOCATION_NOT_WORKING_NOTIFICATION_TAG,
                    PPApplication.LOCATION_NOT_WORKING_NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            //Log.e("ActionForExternalApplicationActivity.showNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

}
