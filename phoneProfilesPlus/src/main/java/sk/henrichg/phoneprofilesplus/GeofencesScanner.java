package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    UpdateTransitionsByLastKnownLocationCallback updateTransitionsByLastKnownLocationCallback;
    UpdateTransitionsByLastKnownLocationListener updateTransitionsByLastKnownLocationListener;
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

        if (PPApplication.googlePlayServiceAvailable)
            updateTransitionsByLastKnownLocationCallback = new UpdateTransitionsByLastKnownLocationCallback();
        else
            updateTransitionsByLastKnownLocationListener = new UpdateTransitionsByLastKnownLocationListener();

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

        if (PPApplication.logEnabled()) {
            if (PPApplication.getInstance() != null)
                PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "PhoneProfilesService.isGeofenceScannerStarted()=" + PhoneProfilesService.getInstance().isGeofenceScannerStarted());
        }

        if ((!mUpdatesStarted) || mUpdateTransitionsByLastKnownLocationIsRunning) {
            synchronized (PPApplication.geofenceScannerMutex) {
                try {
                    if (Permissions.checkLocation(context)) {
                        if (PPApplication.googlePlayServiceAvailable) {
                            try {
                                //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "xxx");
                                createLocationRequest();
                                //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "mFusedLocationClient="+mFusedLocationClient);
                                if ((mFusedLocationClient != null) && (mLocationRequest != null)) {
                                    PPApplication.startHandlerThreadLocation();
                                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, PPApplication.handlerThreadLocation.getLooper());
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
                            if (!mListenerEnabled) {

                                String provider;
                                boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                                if ((!ApplicationPreferences.applicationEventLocationUseGPS) || isPowerSaveMode || (!useGPS)) {
                                    //PPApplication.logE("##### GeofenceScanner.startLocationUpdates","NETWORK_PROVIDER");
                                    provider = LocationManager.NETWORK_PROVIDER;
                                } else {
                                    //PPApplication.logE("##### GeofenceScanner.startLocationUpdates","PRIORITY_HIGH_ACCURACY");
                                    provider = LocationManager.GPS_PROVIDER;
                                }

                                boolean locationEnabled = false;
                                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                                    try {
                                        //noinspection ConstantConditions
                                        locationEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                    } catch (Exception e) {
                                        // we may get IllegalArgumentException if network location provider
                                        // does not exist or is not yet installed.
                                        locationEnabled = false;
                                    }
                                }
                                if (!locationEnabled) {
                                    try {
                                        //noinspection ConstantConditions
                                        locationEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                                    } catch (Exception e) {
                                        // we may get IllegalArgumentException if network location provider
                                        // does not exist or is not yet installed.
                                        locationEnabled = false;
                                    }
                                }

                                if (locationEnabled) {
                                    try {
                                        // check power save mode
                                        String applicationEventLocationUpdateInPowerSaveMode = ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode;
                                        //boolean powerSaveMode = PPApplication.isPowerSaveMode;
                                        if (!(isPowerSaveMode && applicationEventLocationUpdateInPowerSaveMode.equals("2"))) {
                                            int interval = 25; // seconds
                                            if (ApplicationPreferences.applicationEventLocationUpdateInterval > 1)
                                                interval = (ApplicationPreferences.applicationEventLocationUpdateInterval * 60) / INTERVAL_DIVIDE_VALUE; // interval is in minutes
                                            //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "ApplicationPreferences.applicationEventLocationUpdateInterval="+ApplicationPreferences.applicationEventLocationUpdateInterval);
                                            //PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "interval="+interval);
                                            if (isPowerSaveMode && applicationEventLocationUpdateInPowerSaveMode.equals("1"))
                                                interval = 2 * interval;
                                            final long UPDATE_INTERVAL_IN_MILLISECONDS = (interval * 1000) / 2;

                                            PPApplication.logE("##### GeofenceScanner.startLocationUpdates", "request location updates - provider=" + provider);
                                            PPApplication.startHandlerThreadLocation();
                                            mLocationManager.requestLocationUpdates(provider, UPDATE_INTERVAL_IN_MILLISECONDS, 0, mLocationListener, PPApplication.handlerThreadLocation.getLooper());
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

        if (PPApplication.logEnabled()) {
            if (PPApplication.getInstance() != null)
                PPApplication.logE("##### GeofenceScanner.stopLocationUpdates", "PhoneProfilesService.isGeofenceScannerStarted()=" + PhoneProfilesService.getInstance().isGeofenceScannerStarted());
        }

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

                if (PPApplication.googlePlayServiceAvailable) {
                    if (mFusedLocationClient == null)
                        return;
                } else {
                    if (mLocationManager == null)
                        return;
                }

                if (PPApplication.googlePlayServiceAvailable) {
                    //FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

                    if (mFusedLocationClient != null) {
                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        PPApplication.sleep(500);
                    }

                    final LocationRequest locationRequest = LocationRequest.create();

                    final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
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

                    PPApplication.startHandlerThreadLocation();
                    mFusedLocationClient.requestLocationUpdates(locationRequest, updateTransitionsByLastKnownLocationCallback, PPApplication.handlerThreadLocation.getLooper());
                } else {
                    if (mLocationManager != null) {
                        mLocationManager.removeUpdates(mLocationListener);
                        mListenerEnabled = false;
                        PPApplication.sleep(500);
                    }

                    String provider;
                    boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                    if ((!ApplicationPreferences.applicationEventLocationUseGPS) || isPowerSaveMode || (!useGPS)) {
                        //PPApplication.logE("##### GeofenceScanner.startLocationUpdates","NETWORK_PROVIDER");
                        provider = LocationManager.NETWORK_PROVIDER;
                    } else {
                        //PPApplication.logE("##### GeofenceScanner.startLocationUpdates","PRIORITY_HIGH_ACCURACY");
                        provider = LocationManager.GPS_PROVIDER;
                    }

                    boolean locationEnabled = false;
                    if (provider.equals(LocationManager.GPS_PROVIDER)) {
                        try {
                            //noinspection ConstantConditions
                            locationEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        } catch (Exception e) {
                            // we may get IllegalArgumentException if network location provider
                            // does not exist or is not yet installed.
                            locationEnabled = false;
                        }
                    }
                    if (!locationEnabled) {
                        try {
                            //noinspection ConstantConditions
                            locationEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                        } catch (Exception e) {
                            // we may get IllegalArgumentException if network location provider
                            // does not exist or is not yet installed.
                            locationEnabled = false;
                        }
                    }

                    if (locationEnabled) {
                        final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000 / 2;

                        PPApplication.logE("[IN_THREAD_HANDLER] GeofenceScanner.updateTransitionsByLastKnownLocation", "request location updates");
                        PPApplication.startHandlerThreadLocation();
                        mLocationManager.requestLocationUpdates(provider, UPDATE_INTERVAL_IN_MILLISECONDS, 0, updateTransitionsByLastKnownLocationListener, PPApplication.handlerThreadLocation.getLooper());
                    }
                }

                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.GEOFENCE_REMOVE_LAST_KNOWN_LOCATION_UPDATES_WORK_TAG)
                                .setInitialDelay(15, TimeUnit.SECONDS)
                                .build();
                try {
                    if (PPApplication.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        //PPApplication.logE("PhoneProfilesService.doForFirstStart - handler", "workManager="+workManager);
                        if (workManager != null) {
                            //workManager.enqueue(worker);
                            workManager.enqueueUniqueWork(MainWorker.GEOFENCE_REMOVE_LAST_KNOWN_LOCATION_UPDATES_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                        }
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }

/*
                PPApplication.startHandlerThreadPPScanners();
                final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
                handler.postDelayed(() -> {
                    if (PPApplication.googlePlayServiceAvailable) {
                        if (mFusedLocationClient != null) {
                            mFusedLocationClient.flushLocations();
                            mFusedLocationClient.removeLocationUpdates(updateTransitionsByLastKnownLocationCallback);
                        }
                    } else {
                        PPApplication.logE("[IN_THREAD_HANDLER] GeofenceScanner.updateTransitionsByLastKnownLocation", "remove location updates");
                        if (mLocationManager != null)
                            mLocationManager.removeUpdates(updateTransitionsByLastKnownLocationListener);
                    }

                    mUpdateTransitionsByLastKnownLocationIsRunning = false;
                }, 15000);
 */
            }
        } catch (Exception ee) {
            PPApplication.recordException(ee);
        }
    }

    void removeLastKnownLocationUpdates() {
        if (PPApplication.googlePlayServiceAvailable) {
            if (mFusedLocationClient != null) {
                mFusedLocationClient.flushLocations();
                mFusedLocationClient.removeLocationUpdates(updateTransitionsByLastKnownLocationCallback);
            }
        } else {
            PPApplication.logE("GeofenceScanner.removeLastKnownLocationUpdates", "remove last known location updates");
            if (mLocationManager != null)
                mLocationManager.removeUpdates(updateTransitionsByLastKnownLocationListener);
        }

        PPApplication.sleep(500);

        PPApplication.logE("GeofenceScanner.removeLastKnownLocationUpdates", "start location updates");
        startLocationUpdates();

        // THIS MUST BE CALLED AFTER startLocationUpdates()
        mUpdateTransitionsByLastKnownLocationIsRunning = false;
    }

    void flushLocations() {
        synchronized (PPApplication.geofenceScannerMutex) {
            if (mFusedLocationClient != null) {
                mFusedLocationClient.flushLocations();
            }
        }
    }

    static void  doLocationResult(LocationResult locationResult) {
        //PPApplication.logE("##### GeofenceScanner.doLocationResult", "xxx");
        //PPApplication.logE("##### GeofenceScanner.doLocationResult", "locationResult="+locationResult);
        if (locationResult == null)
            return;

        Location location = locationResult.getLastLocation();
        if (location == null)
            return;

        if ((!location.hasAccuracy()))
            return;

        //CallsCounter.logCounter(GeofencesScanner.this.context, "GeofenceScanner.doLocationResult", "GeofenceScanner_doLocationResult");
        //PPApplication.logE("[IN_LISTENER] GeofenceScanner.doLocationResult", "locationResult="+locationResult);
        //PPApplication.logE("[IN_LISTENER] GeofenceScanner.doLocationResult", "locationResult="+locationResult.getLocations().size());
        //PPApplication.logE("##### GeofenceScanner.doLocationResult", "locationResult="+locationResult.getLocations().size());

        synchronized (PPApplication.geofenceScannerLastLocationMutex) {
            //PPApplication.logE("##### GeofenceScanner.doLocationResult", "lastLocation update");
            if (PPApplication.lastLocation == null) {
                PPApplication.lastLocation = new Location("GL");
            }
            PPApplication.lastLocation.set(location);
            //PPApplication.logE("[IN_LISTENER] GeofenceScanner.doLocationResult", "lastLocation=" + lastLocation);
            //PPApplication.logE("##### GeofenceScanner.doLocationResult", "lastLocation=" + lastLocation);
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("##### GeofenceScanner.doLocationResult", "lastLocation.getLatitude()=" + lastLocation.getLatitude());
                PPApplication.logE("##### GeofenceScanner.doLocationResult", "lastLocation.getLongitude()=" + lastLocation.getLongitude());
                PPApplication.logE("##### GeofenceScanner.doLocationResult", "lastLocation.getAccuracy()=" + lastLocation.getAccuracy());
            }*/
        }

        if (Event.getGlobalEventsRunning()) {
            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                //PPApplication.logE("##### GeofenceScanner.doLocationResult", "updateGeofencesInDB");
                GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                scanner.updateGeofencesInDB();
                /*if (useGPS) {
                    // location is from enabled GPS, disable it
                    GeofencesScannerSwitchGPSBroadcastReceiver.setAlarm(scanner.context);
                }*/
                //}
                //if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                //GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                //PPApplication.logE("##### GeofenceScanner.doLocationResult", "handleEvents");
//                 PPApplication.logE("[EVENTS_HANDLER] GeofenceScanner.doLocationResult", "sensorType=SENSOR_TYPE_GEOFENCES_SCANNER");
                EventsHandler eventsHandler = new EventsHandler(scanner.context);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_GEOFENCES_SCANNER);
            }
        }
    }

    static class GeofenceScannerCallback extends LocationCallback {

        @Override
        public void onLocationResult(LocationResult locationResult) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScanner.GeofenceScannerCallback.onLocationResult", "xxx");
            doLocationResult(locationResult);
        }

    }

    static class UpdateTransitionsByLastKnownLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScanner.UpdateTransitionsByLastKnownLocationCallback.onLocationResult", "xxx");
            doLocationResult(locationResult);
        }
    }

    static void doLocationChanged(Location location) {
        //PPApplication.logE("##### GeofenceScanner.doLocationChanged", "xxx");
        //PPApplication.logE("##### GeofenceScanner.doLocationChanged", "locationResult="+locationResult);
        if (location == null)
            return;


        if ((!location.hasAccuracy()))
            return;

        //CallsCounter.logCounter(GeofencesScanner.this.context, "GeofenceScanner.doLocationChanged", "GeofenceScannerGMS_doLocationChanged");
        //PPApplication.logE("GeofenceScanner.doLocationChanged", "location="+location);

        synchronized (PPApplication.geofenceScannerLastLocationMutex) {
            //PPApplication.logE("##### GeofenceScanner.doLocationChanged", "lastLocation update");
            if (PPApplication.lastLocation == null) {
                PPApplication.lastLocation = new Location("GL");
            }
            PPApplication.lastLocation.set(location);
            //PPApplication.logE("[IN_LISTENER] GeofenceScanner.doLocationChanged", "lastLocation=" + lastLocation);
            //PPApplication.logE("##### GeofenceScanner.doLocationChanged", "lastLocation=" + lastLocation);
            if (PPApplication.logEnabled()) {
                PPApplication.logE("##### GeofenceScanner.doLocationChanged", "lastLocation.getProvider()=" + location.getProvider());
                PPApplication.logE("##### GeofenceScanner.doLocationChanged", "lastLocation.getLatitude()=" + location.getLatitude());
                PPApplication.logE("##### GeofenceScanner.doLocationChanged", "lastLocation.getLongitude()=" + location.getLongitude());
                PPApplication.logE("##### GeofenceScanner.doLocationChanged", "lastLocation.getAccuracy()=" + location.getAccuracy());
            }
        }

        if (Event.getGlobalEventsRunning()) {
            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                //PPApplication.logE("##### GeofenceScanner.doLocationChanged", "updateGeofencesInDB");
                GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                scanner.updateGeofencesInDB();
                /*if (useGPS) {
                    // location is from enabled GPS, disable it
                    GeofencesScannerSwitchGPSBroadcastReceiver.setAlarm(scanner.context);
                }*/
                //}
                //if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
                //GeofencesScanner scanner = PhoneProfilesService.getInstance().getGeofencesScanner();
                //PPApplication.logE("##### GeofenceScanner.doLocationChanged", "handleEvents");
//                PPApplication.logE("[EVENTS_HANDLER] GeofenceScanner.doLocationChanged", "sensorType=SENSOR_TYPE_GEOFENCES_SCANNER");
                EventsHandler eventsHandler = new EventsHandler(scanner.context);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_GEOFENCES_SCANNER);
            }
        }
    }

    static class GeofenceScannerListener implements LocationListener {

        public void onLocationChanged(Location location) {
            PPApplication.logE("[IN_LISTENER] GeofenceScanner.GeofenceScannerListener.onLocationChanged", "xxx");
            doLocationChanged(location);
        }

        public void onProviderDisabled(String provider) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScanner.GeofenceScannerListener.onProviderDisabled", "xxx");
        }

        public void onProviderEnabled(String provider) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScanner.GeofenceScannerListener.onProviderEnabled", "xxx");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScanner.GeofenceScannerListener.onStatusChanged", "xxx");
        }
    }

    static class UpdateTransitionsByLastKnownLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            PPApplication.logE("[IN_LISTENER] GeofenceScanner.UpdateTransitionsByLastKnownLocationListener.onLocationChanged", "xxx");
            doLocationChanged(location);
        }

        public void onProviderDisabled(String provider) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScanner.UpdateTransitionsByLastKnownLocationListener.onProviderDisabled", "xxx");
        }

        public void onProviderEnabled(String provider) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScanner.UpdateTransitionsByLastKnownLocationListener.onProviderEnabled", "xxx");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
//            PPApplication.logE("[IN_LISTENER] GeofenceScanner.UpdateTransitionsByLastKnownLocationListener.onStatusChanged", "xxx");
        }
    }

}
