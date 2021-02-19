package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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
    private GeofenceScannerCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    private LocationManager mLocationManager;
    private GeofenceScannerListener mLocationListener;
    private boolean mListenerEnabled = false;

    final Context context;
    //private final DataWrapper dataWrapper;

    static boolean useGPS = true; // must be static
    static boolean mUpdatesStarted = false; // must be static

    static boolean mTransitionsUpdated = false;

    static final int INTERVAL_DIVIDE_VALUE = 6;

    private boolean mUpdateTransitionsByLastKnownLocationIsRunning;

    GeofencesScanner(Context context) {
        this.context = context;
        //dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

        if (PPApplication.googlePlayServiceAvailable)
            mLocationCallback = new GeofenceScannerCallback();
        else
            mLocationListener = new GeofenceScannerListener();

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
                    if (PPApplication.googlePlayServiceAvailable) {
                        if (mFusedLocationClient == null)
                            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
                    }
                    else {
                        if (mLocationManager == null)
                            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    }
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

            if (PPApplication.lastLocation == null)
                return;

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
                    transitionType = Geofence.GEOFENCE_TRANSITION_ENTER;
                    //PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "transition=ENTER");
                }
                else {
                    transitionType = Geofence.GEOFENCE_TRANSITION_EXIT;
                    //PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "transition=exit");
                }

                //int savedTransition = DatabaseHandler.getInstance(context).getGeofenceTransition(geofence._id);

                //PPApplication.logE("##### GeofenceScanner.updateGeofencesInDB", "geofence._name=" + geofence._name);

                if (geofence._transition != transitionType) {

                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "transition changed");

                        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
                            PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "transitionType=GEOFENCE_TRANSITION_ENTER");
                        else
                            PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "transitionType=GEOFENCE_TRANSITION_EXIT");

                        if (geofence._transition == Geofence.GEOFENCE_TRANSITION_ENTER)
                            PPApplication.logE("#####  GeofenceScanner.updateGeofencesInDB", "geofence._transition=GEOFENCE_TRANSITION_ENTER");
                        else if (geofence._transition == Geofence.GEOFENCE_TRANSITION_EXIT)
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
            interval = (ApplicationPreferences.applicationEventLocationUpdateInterval * 60) / INTERVAL_DIVIDE_VALUE; // interval is in minutes
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
                        if (PPApplication.googlePlayServiceAvailable) {
                            try {
                                //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "xxx");
                                createLocationRequest();
                                //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "mFusedLocationClient="+mFusedLocationClient);
                                if ((mFusedLocationClient != null) && (mLocationRequest != null)) {
                                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, PPApplication.handlerThreadPPScanners.getLooper());
                                    //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "mUpdatesStarted=true");
                                    mUpdatesStarted = true;
                                }
                            } catch (SecurityException securityException) {
                                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                                //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "mUpdatesStarted=false");
                                mUpdatesStarted = false;
                                return;
                            }
                        } else {
                            boolean locationEnabled;
                            try {
                                //noinspection ConstantConditions
                                locationEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                            } catch (Exception e) {
                                // we may get IllegalArgumentException if network location provider
                                // does not exist or is not yet installed.
                                locationEnabled = false;
                            }

                            if (!mListenerEnabled && locationEnabled) {
                                try {
                                    // check power save mode
                                    String applicationEventLocationUpdateInPowerSaveMode = ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode;
                                    //boolean powerSaveMode = PPApplication.isPowerSaveMode;
                                    boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                                    if (!(isPowerSaveMode && applicationEventLocationUpdateInPowerSaveMode.equals("2"))) {

                                        int interval = 25; // seconds
                                        if (ApplicationPreferences.applicationEventLocationUpdateInterval > 1)
                                            interval = (ApplicationPreferences.applicationEventLocationUpdateInterval * 60) / INTERVAL_DIVIDE_VALUE; // interval is in minutes
                                        //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "ApplicationPreferences.applicationEventLocationUpdateInterval="+ApplicationPreferences.applicationEventLocationUpdateInterval);
                                        //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "interval="+interval);
                                        if (isPowerSaveMode && applicationEventLocationUpdateInPowerSaveMode.equals("1"))
                                            interval = 2 * interval;
                                        final long UPDATE_INTERVAL_IN_MILLISECONDS = (interval * 1000) / 2;

                                        String provider;
                                        if ((!ApplicationPreferences.applicationEventLocationUseGPS) || isPowerSaveMode || (!useGPS)) {
                                            //PPApplication.logE("##### GeofenceScanner.startLocationUpdates","NETWORK_PROVIDER");
                                            provider = LocationManager.NETWORK_PROVIDER;
                                        } else {
                                            //PPApplication.logE("##### GeofenceScanner.startLocationUpdates","PRIORITY_HIGH_ACCURACY");
                                            provider = LocationManager.GPS_PROVIDER;
                                        }

                                        mLocationManager.requestLocationUpdates(provider, UPDATE_INTERVAL_IN_MILLISECONDS, 0, mLocationListener, PPApplication.handlerThreadPPScanners.getLooper());
                                        mListenerEnabled = true;

                                        mUpdatesStarted = true;
                                    }
                                } catch (SecurityException securityException) {
                                    // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                                    //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "mUpdatesStarted=false");
                                    mUpdatesStarted = false;
                                    return;
                                }
                            }
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
                if (PPApplication.googlePlayServiceAvailable) {
                    try {
                        //PPApplication.logE("##### GeofenceScanner.stopLocationUpdates", "xxx");
                        if (mFusedLocationClient != null)
                            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        mUpdatesStarted = false;
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                } else {
                    if (mListenerEnabled) {
                        try {
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

    final UpdateTransitionsByLastKnownLocationCallback updateTransitionsByLastKnownLocationCallback = new UpdateTransitionsByLastKnownLocationCallback();
    final UpdateTransitionsByLastKnownLocationListener updateTransitionsByLastKnownLocationListener = new UpdateTransitionsByLastKnownLocationListener();

    @SuppressLint("MissingPermission")
    void updateTransitionsByLastKnownLocation() {
        if (mUpdateTransitionsByLastKnownLocationIsRunning)
            return;

        try {
            if (Permissions.checkLocation(context)) {

                //if (PPApplication.logEnabled()) {
                //    if (PhoneProfilesService.getInstance() != null)
                //        PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation", "PhoneProfilesService.isGeofenceScannerStarted()=" + PhoneProfilesService.getInstance().isGeofenceScannerStarted());
                //}

                mUpdateTransitionsByLastKnownLocationIsRunning = true;

                final Context appContext = context.getApplicationContext();

                PPApplication.startHandlerThreadPPScanners();
                final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
                handler.post(() -> {
//                         PPApplication.logE("[IN_THREAD_HANDLER] GeofenceScanner.updateTransitionsByLastKnownLocation", "START update");

                    if (PPApplication.googlePlayServiceAvailable) {
                        if (mFusedLocationClient == null)
                            return;
                    } else {
                        if (mLocationManager == null)
                            return;
                    }

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":GeofenceScanner_updateTransitionsByLastKnownLocation");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (PPApplication.googlePlayServiceAvailable) {
                            final LocationRequest locationRequest = LocationRequest.create();

                            final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
                            final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
                            locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
                            locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

                            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(appContext);
                            if ((!ApplicationPreferences.applicationEventLocationUseGPS) || isPowerSaveMode || (!useGPS)) {
                                //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation","PRIORITY_BALANCED_POWER_ACCURACY");
                                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                            } else {
                                //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation","PRIORITY_HIGH_ACCURACY");
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            }

                            //FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
                            mFusedLocationClient.requestLocationUpdates(locationRequest, updateTransitionsByLastKnownLocationCallback, null);
                        } else {
                            final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000 / 2;

                            String provider;
                            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(appContext);
                            if (isPowerSaveMode) {
                                //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation","PRIORITY_BALANCED_POWER_ACCURACY");
                                provider = LocationManager.NETWORK_PROVIDER;
                            } else {
                                //PPApplication.logE("##### GeofenceScanner.updateTransitionsByLastKnownLocation","PRIORITY_HIGH_ACCURACY");
                                provider = LocationManager.GPS_PROVIDER;
                            }

                            mLocationManager.requestLocationUpdates(provider, UPDATE_INTERVAL_IN_MILLISECONDS, 0, updateTransitionsByLastKnownLocationListener);
                        }

                        PPApplication.sleep(6000);

                        if (PPApplication.googlePlayServiceAvailable) {
                            mFusedLocationClient.flushLocations();
                            mFusedLocationClient.removeLocationUpdates(updateTransitionsByLastKnownLocationCallback);
                        } else {
                            if (mLocationManager != null)
                                mLocationManager.removeUpdates(updateTransitionsByLastKnownLocationListener);
                        }

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
            if (PPApplication.googlePlayServiceAvailable) {
                if (mFusedLocationClient != null) {
                    mFusedLocationClient.flushLocations();
                }
            }
        }
    }

    static class GeofenceScannerCallback extends LocationCallback {

        @Override
        public void onLocationResult(LocationResult locationResult) {
//                PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "xxx");
            //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "xxx");
            //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "locationResult="+locationResult);
            if (locationResult == null)
                return;

            Location location = locationResult.getLastLocation();
            if (location == null)
                return;

            if ((!location.hasAccuracy()))
                return;

            //CallsCounter.logCounter(GeofencesScanner.this.context, "GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "GeofenceScannerGMS_GeofenceScannerCallback_onLocationResult");
            //PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "locationResult="+locationResult);
            //PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "locationResult="+locationResult.getLocations().size());
            //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "locationResult="+locationResult.getLocations().size());

            synchronized (PPApplication.geofenceScannerLastLocationMutex) {
                //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "lastLocation update");
                if (PPApplication.lastLocation == null) {
                    PPApplication.lastLocation = new Location("GL");
                }
                PPApplication.lastLocation.set(location);
                //PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "lastLocation=" + lastLocation);
                //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "lastLocation=" + lastLocation);
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "lastLocation.getLatitude()=" + lastLocation.getLatitude());
                        PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "lastLocation.getLongitude()=" + lastLocation.getLongitude());
                        PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "lastLocation.getAccuracy()=" + lastLocation.getAccuracy());
                    }*/
            }

            if (Event.getGlobalEventsRunning()) {
                if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                    //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "updateGeofencesInDB");
                    GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                    scanner.updateGeofencesInDB();
                        /*if (useGPS) {
                            // location is from enabled GPS, disable it
                            GeofencesScannerSwitchGPSBroadcastReceiver.setAlarm(scanner.context);
                        }*/
                    //}
                    //if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                    //GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                    //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "handleEvents");
//                        PPApplication.logE("[EVENTS_HANDLER] GeofenceScannerGMS.GeofenceScannerCallback.onLocationResult", "sensorType=SENSOR_TYPE_GEOFENCES_SCANNER");
                    EventsHandler eventsHandler = new EventsHandler(scanner.context);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_GEOFENCES_SCANNER);
                }
            }
        }

    }

    static class UpdateTransitionsByLastKnownLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "xxx");
            //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "xxx");
            //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "locationResult="+locationResult);

            if (locationResult == null)
                return;

            Location location = locationResult.getLastLocation();
            if (location == null)
                return;

            if ((!location.hasAccuracy()))
                return;

            //CallsCounter.logCounter(GeofencesScanner.this.context, "GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "GeofenceScannerGMS_UpdateTransitionsByLastKnownLocationCallback_onLocationResult");
            //PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "locationResult="+locationResult);
            //PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "locationResult="+locationResult.getLocations().size());
            //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "locationResult="+locationResult.getLocations().size());

            synchronized (PPApplication.geofenceScannerLastLocationMutex) {
                //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "lastLocation update");
                if (PPApplication.lastLocation == null) {
                    PPApplication.lastLocation = new Location("GL");
                }
                PPApplication.lastLocation.set(location);
                //PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "lastLocation=" + lastLocation);
                //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "lastLocation=" + lastLocation);
                //if (PPApplication.logEnabled()) {
                //    PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "lastLocation.getLatitude()=" + lastLocation.getLatitude());
                //    PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "lastLocation.getLongitude()=" + lastLocation.getLongitude());
                //    PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "lastLocation.getAccuracy()=" + lastLocation.getAccuracy());
                //}
            }

            if (Event.getGlobalEventsRunning()) {
                if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                    //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "updateGeofencesInDB");
                    GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                    scanner.updateGeofencesInDB();
                    //}
                    //if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                    //GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                    //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "handleEvents");
//                    PPApplication.logE("[EVENTS_HANDLER_CALL] GeofenceScanner.updateTransitionsByLastKnownLocationCallback", "sensorType=SENSOR_TYPE_GEOFENCES_SCANNER");
                    EventsHandler eventsHandler = new EventsHandler(scanner.context);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_GEOFENCES_SCANNER);
                }
            }

        }
    }

    static class GeofenceScannerListener implements LocationListener {

        public void onLocationChanged(Location location) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "xxx");
            //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "xxx");
            //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "locationResult="+locationResult);
            if (location == null)
                return;


            if ((!location.hasAccuracy()))
                return;

            //CallsCounter.logCounter(GeofencesScanner.this.context, "GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "GeofenceScannerGMS_GeofenceScannerListener_onLocationChanged");
            //PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "locationResult="+locationResult);
            //PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "locationResult="+locationResult.getLocations().size());
            //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "locationResult="+locationResult.getLocations().size());

            synchronized (PPApplication.geofenceScannerLastLocationMutex) {
                //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "lastLocation update");
                if (PPApplication.lastLocation == null) {
                    PPApplication.lastLocation = new Location("GL");
                }
                PPApplication.lastLocation.set(location);
                //PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "lastLocation=" + lastLocation);
                //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "lastLocation=" + lastLocation);
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "lastLocation.getLatitude()=" + lastLocation.getLatitude());
                        PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "lastLocation.getLongitude()=" + lastLocation.getLongitude());
                        PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "lastLocation.getAccuracy()=" + lastLocation.getAccuracy());
                    }*/
            }

            if (Event.getGlobalEventsRunning()) {
                if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                    //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "updateGeofencesInDB");
                    GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                    scanner.updateGeofencesInDB();
                        /*if (useGPS) {
                            // location is from enabled GPS, disable it
                            GeofencesScannerSwitchGPSBroadcastReceiver.setAlarm(scanner.context);
                        }*/
                    //}
                    //if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                    //GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                    //PPApplication.logE("##### GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "handleEvents");
//                        PPApplication.logE("[EVENTS_HANDLER] GeofenceScannerGMS.GeofenceScannerListener.onLocationChanged", "sensorType=SENSOR_TYPE_GEOFENCES_SCANNER");
                    EventsHandler eventsHandler = new EventsHandler(scanner.context);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_GEOFENCES_SCANNER);
                }
            }
        }

        public void onProviderDisabled(String provider) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.GeofenceScannerListener.onProviderDisabled", "xxx");
        }

        public void onProviderEnabled(String provider) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.GeofenceScannerListener.onProviderEnabled", "xxx");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.GeofenceScannerListener.onStatusChanged", "xxx");
        }
    }

    static class UpdateTransitionsByLastKnownLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "xxx");
            //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "xxx");
            //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "locationResult="+locationResult);

            if (location == null)
                return;

            if ((!location.hasAccuracy()))
                return;

            //CallsCounter.logCounter(GeofencesScanner.this.context, "GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "GeofenceScannerGMS_UpdateTransitionsByLastKnownLocationListener_onLocationChanged");
            //PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "locationResult="+locationResult);
            //PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "locationResult="+locationResult.getLocations().size());
            //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "locationResult="+locationResult.getLocations().size());

            synchronized (PPApplication.geofenceScannerLastLocationMutex) {
                //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "lastLocation update");
                if (PPApplication.lastLocation == null) {
                    PPApplication.lastLocation = new Location("GL");
                }
                PPApplication.lastLocation.set(location);
                //PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "lastLocation=" + lastLocation);
                //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "lastLocation=" + lastLocation);
                //if (PPApplication.logEnabled()) {
                //    PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "lastLocation.getLatitude()=" + lastLocation.getLatitude());
                //    PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "lastLocation.getLongitude()=" + lastLocation.getLongitude());
                //    PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "lastLocation.getAccuracy()=" + lastLocation.getAccuracy());
                //}
            }

            if (Event.getGlobalEventsRunning()) {
                if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                    //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "updateGeofencesInDB");
                    GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                    scanner.updateGeofencesInDB();
                    //}
                    //if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                    //GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                    //PPApplication.logE("##### GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "handleEvents");
//                    PPApplication.logE("[EVENTS_HANDLER_CALL] GeofenceScanner.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "sensorType=SENSOR_TYPE_GEOFENCES_SCANNER");
                    EventsHandler eventsHandler = new EventsHandler(scanner.context);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_GEOFENCES_SCANNER);
                }
            }
        }

        public void onProviderDisabled(String provider) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onProviderDisabled", "xxx");
        }

        public void onProviderEnabled(String provider) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onProviderEnabled", "xxx");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScannerGMS.UpdateTransitionsByLastKnownLocationListener.onStatusChanged", "xxx");
        }
    }

}
