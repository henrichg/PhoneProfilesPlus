package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.PowerManager;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

class GeofencesScanner
{
    private FusedLocationProviderClient mFusedLocationClient;
    private final LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    final Context context;
    //private final DataWrapper dataWrapper;

    static boolean useGPS = true; // must be static
    static boolean mUpdatesStarted = false; // must be static

    static boolean mTransitionsUpdated = false;

    static final int INTRVAL_DIVIDE_VALUE = 6;

    private boolean mUpdateTransitionsByLastKnownLocationIsRunning;

    GeofencesScanner(Context context) {
        this.context = context;
        //dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
//                PPApplication.logE("[IN_LISTENER] GeofenceScanner.LocationCallback", "xxx");
                //PPApplication.logE("##### GeofenceScanner.LocationCallback", "xxx");
                //PPApplication.logE("##### GeofenceScanner.LocationCallback", "locationResult="+locationResult);
                if (locationResult == null)
                    return;

                Location location = locationResult.getLastLocation();
                if (location == null)
                    return;

                if ((!location.hasAccuracy()))
                    return;

                //CallsCounter.logCounter(GeofencesScanner.this.context, "GeofencesScanner.LocationCallback", "GeofencesScanner_onLocationResult");
                //PPApplication.logE("[IN_LISTENER] GeofenceScanner.LocationCallback", "locationResult="+locationResult);
                //PPApplication.logE("[IN_LISTENER] GeofenceScanner.LocationCallback", "locationResult="+locationResult.getLocations().size());
                //PPApplication.logE("##### GeofenceScanner.LocationCallback", "locationResult="+locationResult.getLocations().size());

                synchronized (PPApplication.geofenceScannerLastLocationMutex) {
                    //PPApplication.logE("##### GeofenceScanner.LocationCallback", "lastLocation update");
                    PPApplication.lastLocation.set(location);
                    //PPApplication.logE("[IN_LISTENER] GeofenceScanner.LocationCallback", "lastLocation=" + lastLocation);
                    //PPApplication.logE("##### GeofenceScanner.LocationCallback", "lastLocation=" + lastLocation);
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("##### GeofenceScanner.LocationCallback", "lastLocation.getLatitude()=" + lastLocation.getLatitude());
                        PPApplication.logE("##### GeofenceScanner.LocationCallback", "lastLocation.getLongitude()=" + lastLocation.getLongitude());
                        PPApplication.logE("##### GeofenceScanner.LocationCallback", "lastLocation.getAccuracy()=" + lastLocation.getAccuracy());
                    }*/
                }

                if (Event.getGlobalEventsRunning()) {
                    if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                        //PPApplication.logE("##### GeofenceScanner.LocationCallback", "updateGeofencesInDB");
                        GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                        scanner.updateGeofencesInDB();
                        /*if (useGPS) {
                            // location is from enabled GPS, disable it
                            GeofencesScannerSwitchGPSBroadcastReceiver.setAlarm(scanner.context);
                        }*/
                    //}
                    //if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                        //GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                        //PPApplication.logE("##### GeofenceScanner.LocationCallback", "handleEvents");
//                        PPApplication.logE("[EVENTS_HANDLER] GeofenceScanner.LocationCallback", "sensorType=SENSOR_TYPE_GEOFENCES_SCANNER");
                        EventsHandler eventsHandler = new EventsHandler(scanner.context);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_GEOFENCES_SCANNER);
                    }
                }
            }
        };

//        if (lastLocation == null) {
//            //PPApplication.logE("##### GeofenceScanner", "lastLocation update");
//            lastLocation = new Location("GL");
//        }

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("##### GeofenceScanner", "lastLocation=" + lastLocation);
            PPApplication.logE("##### GeofenceScanner", "lastLocation.getLatitude()=" + lastLocation.getLatitude());
            PPApplication.logE("##### GeofenceScanner", "lastLocation.getLongitude()=" + lastLocation.getLongitude());
            PPApplication.logE("##### GeofenceScanner", "lastLocation.getAccuracy()=" + lastLocation.getAccuracy());
        }*/

        mUpdateTransitionsByLastKnownLocationIsRunning= false;
    }

    void connect(boolean resetUseGPS) {
//        PPApplication.logE("##### GeofenceScanner.connect", "xxx");
        /*if (PPApplication.logEnabled()) {
            if (PhoneProfilesService.getInstance() != null)
                PPApplication.logE("##### GeofenceScanner.connect", "PhoneProfilesService.isGeofenceScannerStarted()=" + PhoneProfilesService.getInstance().isGeofenceScannerStarted());
        }*/
        try {
            synchronized (PPApplication.geofenceScannerMutex) {
                //if (dataWrapper.getDatabaseHandler().getGeofenceCount() > 0)

                try {
                    int version = GoogleApiAvailability.getInstance().getApkVersion(this.context);
                    PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_GOOGLE_PLAY_SERVICES_VERSION, version);
                } catch (Exception e) {
                    // https://github.com/firebase/firebase-android-sdk/issues/1226
                    //PPApplication.recordException(e);
                }
                /*if (PPApplication.logEnabled()) {
                    if (PhoneProfilesService.getInstance() != null)
                        PPApplication.logE("##### GeofenceScanner.onConnected", "PhoneProfilesService.isGeofenceScannerStarted()=" + PhoneProfilesService.getInstance().isGeofenceScannerStarted());
                }*/
                try {
                    //PPApplication.logE("##### GeofenceScanner.onConnected", "xxx2");
                    if (mFusedLocationClient == null)
                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
                    if (resetUseGPS)
                        useGPS = true;

                    PPApplication.startHandlerThreadPPScanners(/*"GeofenceScanner.onConnected"*/);
                    final Handler handler6 = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
                    handler6.post(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=GeofenceScanner.connect");

                        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":GeofenceScanner_connect");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                                GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                                if (scanner != null) {
                                    scanner.clearAllEventGeofences();
                                    //PPApplication.logE("##### GeofenceScanner.onConnected", "updateTransitionsByLastKnownLocation");
                                    scanner.startLocationUpdates();
                                    scanner.updateTransitionsByLastKnownLocation();
                                }
                            }

                            //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=GeofenceScanner.onConnected");
                        } catch (Exception e) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                        }
                    });
                } catch (Exception ee) {
                    //Log.e("##### GeofenceScanner.onConnected", Log.getStackTraceString(e));
                    PPApplication.recordException(ee);
                }

            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    void disconnect() {
        //PPApplication.logE("##### GeofenceScanner.disconnect", "xxx");
        /*if (PPApplication.logEnabled()) {
            if (PhoneProfilesService.getInstance() != null)
                PPApplication.logE("##### GeofenceScanner.disconnect", "PhoneProfilesService.isGeofenceScannerStarted()=" + PhoneProfilesService.getInstance().isGeofenceScannerStarted());
        }*/
        try {
            stopLocationUpdates();
            GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm(context);
            mUpdateTransitionsByLastKnownLocationIsRunning= false;
            //useGPS = true; disconnect is called from screen on/off broadcast therefore not change this
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    void updateGeofencesInDB() {
        synchronized (PPApplication.geofenceScannerMutex) {
//            PPApplication.logE("#####   GeofenceScanner.updateGeofencesInDB", "xxx");
            /*if (PPApplication.logEnabled()) {
                if (PhoneProfilesService.getInstance() != null)
                    PPApplication.logE("[***] GeofenceScanner.updateGeofencesInDB", "PhoneProfilesService.isGeofenceScannerStarted()=" + PhoneProfilesService.getInstance().isGeofenceScannerStarted());
            }*/

            List<Geofence> geofences = DatabaseHandler.getInstance(context).getAllGeofences();
            //PPApplication.logE("#####   GeofenceScanner.updateGeofencesInDB", "geofences.size="+geofences.size());

            //boolean change = false;

            for (Geofence geofence : geofences) {

                Location geofenceLocation = new Location("GL");
                geofenceLocation.setLatitude(geofence._latitude);
                geofenceLocation.setLongitude(geofence._longitude);

                float distance;
                float radius;
                synchronized (PPApplication.geofenceScannerLastLocationMutex) {
                    Location _lastLocation = new Location("GL");
                    _lastLocation.setLatitude(PPApplication.lastLocation.getLatitude());
                    _lastLocation.setLongitude(PPApplication.lastLocation.getLongitude());

                    distance = Math.abs(_lastLocation.distanceTo(geofenceLocation));
                    radius = PPApplication.lastLocation.getAccuracy() + geofence._radius;

                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "geofence._name=" + geofence._name);
                        PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "distance=" + distance);
                        PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "radius=" + radius);
                    }*/
                }

                int transitionType;
                if (distance <= radius) {
                    transitionType = com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER;
                    //PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "transition=ENTER");
                }
                else {
                    transitionType = com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT;
                    //PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "transition=exit");
                }

                //int savedTransition = DatabaseHandler.getInstance(context).getGeofenceTransition(geofence._id);

                //PPApplication.logE("##### GeofenceScanner.updateGeofencesInDB", "geofence._name=" + geofence._name);

                if (geofence._transition != transitionType) {

                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "transition changed");

                        if (transitionType == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER)
                            PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "transitionType=GEOFENCE_TRANSITION_ENTER");
                        else
                            PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "transitionType=GEOFENCE_TRANSITION_EXIT");

                        if (geofence._transition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER)
                            PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "geofence._transition=GEOFENCE_TRANSITION_ENTER");
                        else if (geofence._transition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT)
                            PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "geofence._transition=GEOFENCE_TRANSITION_EXIT");
                        else
                            PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "geofence._transition=0");
                    }*/

                    DatabaseHandler.getInstance(context).updateGeofenceTransition(geofence._id, transitionType);
                    //change = true;
                }
                //else
                //    PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "geofence._transition == transitionType");
            }

            mTransitionsUpdated = true;
        }
    }

    void clearAllEventGeofences() {
        synchronized (PPApplication.geofenceScannerMutex) {
            // clear all geofence transitions
            DatabaseHandler.getInstance(context).clearAllGeofenceTransitions();
            mTransitionsUpdated = false;
        }
    }

    //-------------------------------------------

    private void createLocationRequest() {
        //PPApplication.logE("##### GeofenceScanner.createLocationRequest", "xxx");

        // check power save mode
        String applicationEventLocationUpdateInPowerSaveMode = ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode;
        //boolean powerSaveMode = PPApplication.isPowerSaveMode;
        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if (isPowerSaveMode && applicationEventLocationUpdateInPowerSaveMode.equals("2")) {
            mLocationRequest = null;
            return;
        }

        mLocationRequest = LocationRequest.create();

        /*
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        int interval = 25; // seconds
        if (ApplicationPreferences.applicationEventLocationUpdateInterval > 1)
            interval = (ApplicationPreferences.applicationEventLocationUpdateInterval * 60) / INTRVAL_DIVIDE_VALUE; // interval is in minutes
        //PPApplication.logE("##### GeofenceScanner.createLocationRequest", "ApplicationPreferences.applicationEventLocationUpdateInterval="+ApplicationPreferences.applicationEventLocationUpdateInterval);
        //PPApplication.logE("##### GeofenceScanner.createLocationRequest", "interval="+interval);
        if (isPowerSaveMode && applicationEventLocationUpdateInPowerSaveMode.equals("1"))
            interval = 2 * interval;
        final long UPDATE_INTERVAL_IN_MILLISECONDS = interval * 1000;

        /*
         * The fastest rate for active location updates. Exact. Updates will never be more frequent
         * than this value.
         */
        final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;


        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        // batched location (better for Android 8.0)
        mLocationRequest.setMaxWaitTime(UPDATE_INTERVAL_IN_MILLISECONDS * 4);

        if ((!ApplicationPreferences.applicationEventLocationUseGPS) || isPowerSaveMode || (!useGPS)) {
            //PPApplication.logE("##### GeofenceScanner.createLocationRequest","PRIORITY_BALANCED_POWER_ACCURACY");
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
        else {
            //PPApplication.logE("##### GeofenceScanner.createLocationRequest","PRIORITY_HIGH_ACCURACY");
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }


    /**
     * Requests location updates from the FusedLocationApi.
     */
    void startLocationUpdates() {
        if (!ApplicationPreferences.applicationEventLocationEnableScanning)
            return;

        /*if (PPApplication.logEnabled()) {
            if (PPApplication.getInstance() != null)
                PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "PhoneProfilesService.isGeofenceScannerStarted()=" + PhoneProfilesService.getInstance().isGeofenceScannerStarted());
        }*/

        if (!mUpdatesStarted) {
            synchronized (PPApplication.geofenceScannerMutex) {
                try {
                    if (Permissions.checkLocation(context)) {
                        try {
                            //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "xxx");
                            createLocationRequest();
                            //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "mFusedLocationClient="+mFusedLocationClient);
                            if (mFusedLocationClient != null)
                                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                            //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "mUpdatesStarted=true");
                            mUpdatesStarted = true;
                        } catch (SecurityException securityException) {
                            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                            //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "mUpdatesStarted=false");
                            mUpdatesStarted = false;
                            return;
                        }
                    }
                } catch (Exception e) {
                    //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "mUpdatesStarted=false");
                    mUpdatesStarted = false;
                }
            }
        }

        if (ApplicationPreferences.applicationEventLocationUseGPS) {
            // recursive call this for switch usage of GPS
            GeofencesScannerSwitchGPSBroadcastReceiver.setAlarm(context);
        }
        else
            GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm(context);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        /*if (PPApplication.logEnabled()) {
            if (PPApplication.getInstance() != null)
                PPApplication.logE("##### GeofenceScanner.stopLocationUpdates", "PhoneProfilesService.isGeofenceScannerStarted()=" + PhoneProfilesService.getInstance().isGeofenceScannerStarted());
        }*/

        if (mUpdatesStarted) {
            synchronized (PPApplication.geofenceScannerMutex) {
                try {
                    //PPApplication.logE("##### GeofenceScanner.stopLocationUpdates", "xxx");
                    if (mFusedLocationClient != null)
                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                    //PPApplication.logE("##### GeofenceScanWorker.mUpdatesStarted=false", "from GeofenceScanner.stopLocationUpdates");
                    mUpdatesStarted = false;
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        }
    }

    /*
    void resetLocationUpdates(boolean forScreenOn) {
        stopLocationUpdates();
        createLocationRequest();
        PPApplication.logE("GeofenceScanner.scheduleWorker", "from GeofenceScanner.resetLocationUpdates");
        // startLocationUpdates is called from GeofenceScanWorker
        if (PhoneProfilesService.getInstance() != null)
            PhoneProfilesService.getInstance().scheduleGeofenceWorker(true, false, true, forScreenOn, true);
    }
    */

    final LocationCallback updateTransitionsByLastKnownLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScanner.updateTransitionsByLastKnownLocation.LocationCallback", "xxx");
            //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation.LocationCallback", "xxx");
            //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation.LocationCallback", "locationResult="+locationResult);

            if (locationResult == null)
                return;

            Location location = locationResult.getLastLocation();
            if (location == null)
                return;

            if ((!location.hasAccuracy()))
                return;

            //CallsCounter.logCounter(GeofencesScanner.this.context, "GeofencesScanner.updateTransitionsByLastKnownLocation.LocationCallback", "GeofencesScanner_updateTransitionsByLastKnownLocation_onLocationResult");
            //PPApplication.logE("[IN_LISTENER] GeofenceScanner.updateTransitionsByLastKnownLocation.LocationCallback", "locationResult="+locationResult);
            //PPApplication.logE("[IN_LISTENER] GeofenceScanner.updateTransitionsByLastKnownLocation.LocationCallback", "locationResult="+locationResult.getLocations().size());
            //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation.LocationCallback", "locationResult="+locationResult.getLocations().size());

            synchronized (PPApplication.geofenceScannerLastLocationMutex) {
                //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocationCallback", "lastLocation update");
                PPApplication.lastLocation.set(location);
                //PPApplication.logE("[IN_LISTENER] GeofenceScanner.updateTransitionsByLastKnownLocation.LocationCallback", "lastLocation=" + lastLocation);
                //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation.LocationCallback", "lastLocation=" + lastLocation);
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation.LocationCallback", "lastLocation.getLatitude()=" + lastLocation.getLatitude());
                    PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation.LocationCallback", "lastLocation.getLongitude()=" + lastLocation.getLongitude());
                    PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation.LocationCallback", "lastLocation.getAccuracy()=" + lastLocation.getAccuracy());
                }*/
            }

            if (Event.getGlobalEventsRunning()) {
                if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                    //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation.LocationCallback", "updateGeofencesInDB");
                    GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                    scanner.updateGeofencesInDB();
                //}
                //if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                    //GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                    //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation.LocationCallback", "handleEvents");
//                    PPApplication.logE("[EVENTS_HANDLER_CALL] GeofenceScanner.updateTransitionsByLastKnownLocationCallback", "sensorType=SENSOR_TYPE_GEOFENCES_SCANNER");
                    EventsHandler eventsHandler = new EventsHandler(scanner.context);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_GEOFENCES_SCANNER);
                }
            }

        }
    };

    @SuppressLint("MissingPermission")
    void updateTransitionsByLastKnownLocation() {
        if (mUpdateTransitionsByLastKnownLocationIsRunning)
            return;

        try {
            if (Permissions.checkLocation(context)) {

                /*if (PPApplication.logEnabled()) {
                    if (PhoneProfilesService.getInstance() != null)
                        PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation", "PhoneProfilesService.isGeofenceScannerStarted()=" + PhoneProfilesService.getInstance().isGeofenceScannerStarted());
                }*/

                mUpdateTransitionsByLastKnownLocationIsRunning = true;

                final Context appContext = context.getApplicationContext();

                //final FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
                /*fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation", "onSuccess");
                        if (location != null) {
                            //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation", "location=" + location);
                            synchronized (PPApplication.geofenceScannerLastLocationMutex) {
                                lastLocation.set(location);
                                //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation", "lastLocation="+lastLocation);
                            }
                            PPApplication.startHandlerThreadPPScanners();
                            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = null;
                                    try {
                                        if (powerManager != null) {
                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":GeofenceScanner_updateTransitionsByLastKnownLocation");
                                            wakeLock.acquire(10 * 60 * 1000);
                                        }

//                                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=GeofenceScanner.updateTransitionsByLastKnownLocation");

                                        if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                                            GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                                            scanner.updateGeofencesInDB();
                                        }

                                    } finally {
                                        if ((wakeLock != null) && wakeLock.isHeld()) {
                                            try {
                                                wakeLock.release();
                                            } catch (Exception ignored) {}
                                        }
                                    }
                                }
                            });
                        }
                    }
                });*/

                PPApplication.startHandlerThreadPPScanners();
                final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
                handler.post(() -> {
//                         PPApplication.logE("[IN_THREAD_HANDLER] GeofenceScanner.updateTransitionsByLastKnownLocation", "START update");

                    if (mFusedLocationClient == null)
                        return;

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":GeofenceScanner_updateTransitionsByLastKnownLocation");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        final LocationRequest locationRequest = LocationRequest.create();

                        final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
                        final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
                        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
                        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

                        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                        if ((!ApplicationPreferences.applicationEventLocationUseGPS) || isPowerSaveMode || (!useGPS)) {
                            //PPApplication.logE("##### GeofenceScanner.createLocationRequest","PRIORITY_BALANCED_POWER_ACCURACY");
                            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                        } else {
                            //PPApplication.logE("##### GeofenceScanner.createLocationRequest","PRIORITY_HIGH_ACCURACY");
                            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        }

                        //FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
                        mFusedLocationClient.requestLocationUpdates(locationRequest, updateTransitionsByLastKnownLocationCallback, null);

                        PPApplication.sleep(6000);

                        mFusedLocationClient.flushLocations();
                        mFusedLocationClient.removeLocationUpdates(updateTransitionsByLastKnownLocationCallback);

                        /*if (Event.getGlobalEventsRunning()) {
                            boolean geofenceScannerUpdatesStarted = false;
                            synchronized (PPApplication.geofenceScannerMutex) {
                                if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                                    PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation", "updateGeofencesInDB");
                                    GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                                    scanner.updateGeofencesInDB();
                                    geofenceScannerUpdatesStarted = true;
                                }
                            }
                            if (geofenceScannerUpdatesStarted) {
                                EventsHandler eventsHandler = new EventsHandler(context);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_GEOFENCES_SCANNER);
                            }
                        }*/

                        mUpdateTransitionsByLastKnownLocationIsRunning = false;

                        //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation", "END update");
                    } catch (Exception e) {
//                             PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {}
                        }
                    }
                });

            }
        } catch (Exception ee) {
            PPApplication.recordException(ee);
        }
    }

    void flushLocations() {
        synchronized (PPApplication.geofenceScannerMutex) {
            if (mFusedLocationClient != null) {
                mFusedLocationClient.flushLocations();
            }
        }
    }

}
