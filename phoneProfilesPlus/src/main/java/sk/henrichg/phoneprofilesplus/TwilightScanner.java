package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import java.util.Calendar;
import java.util.Iterator;

import static android.content.Context.POWER_SERVICE;

class TwilightScanner {

    private static final String TAG = "TwilightScanner";
    private static final boolean DEBUG = false;
    private static final String ACTION_UPDATE_TWILIGHT_STATE = "sk.henrichg.phoneprofilesplus.ACTION_UPDATE_TWILIGHT_STATE";

    private final Object mLock = new Object();

    Context context;
    private AlarmManager mAlarmManager;
    private LocationManager mLocationManager;
    private LocationHandler mLocationHandler;

    private TwilightState mTwilightState;

    TwilightScanner(Context context) {
        this.context = context;
    }

    void start() {

        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mLocationManager = (LocationManager) context.getSystemService(
                Context.LOCATION_SERVICE);
        mLocationHandler = new LocationHandler();

        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(ACTION_UPDATE_TWILIGHT_STATE);
        context.registerReceiver(mUpdateLocationReceiver, filter);

        mLocationHandler.enableLocationUpdates();
    }

    void stop() {
        mLocationHandler.requestLocationUpdate();

        try {
            context.unregisterReceiver(mUpdateLocationReceiver);
        } catch (Exception ignored) {
        }
    }

    private void setTwilightState(TwilightState state) {
        synchronized (mLock) {
            if ((mTwilightState == null) || (state == null) || !mTwilightState.equals(state)) {
                if (DEBUG) {
                    Log.d(TAG, "Twilight state changed: " + state);
                }

                mTwilightState = state;

                final Context appContext = context.getApplicationContext();

                if (!PPApplication.getApplicationStarted(appContext, true))
                    // application is not started
                    return;

                if (Event.getGlobalEventsRunning(appContext)) {
                    PPApplication.logE("TwilightScanner.setTwilightState", "xxx");

                    PPApplication.startHandlerThread("TwilightScanner.setTwilightState");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":TwilightScanner_setTwilightState");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                PPApplication.logE("PPApplication.startHandlerThread", "START run - from=TwilightScanner.setTwilightState");

                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_TIME);

                                PPApplication.logE("PPApplication.startHandlerThread", "END run - from=TwilightScanner.setTwilightState");
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    TwilightState getTwilightState() {
        synchronized (mLock) {
            return mTwilightState;
        }
    }

    // The user has moved if the accuracy circles of the two locations don't overlap.
    private static boolean hasMoved(Location from, Location to) {
        if (to == null) {
            return false;
        }

        if (from == null) {
            return true;
        }

        // if new location is older than the current one, the device hasn't moved.
        if (to.getElapsedRealtimeNanos() < from.getElapsedRealtimeNanos()) {
            return false;
        }

        // Get the distance between the two points.
        float distance = from.distanceTo(to);

        // Get the total accuracy radius for both locations.
        float totalAccuracy = from.getAccuracy() + to.getAccuracy();

        // If the distance is greater than the combined accuracy of the two
        // points then they can't overlap and hence the user has moved.
        return distance >= totalAccuracy;
    }

    @SuppressLint("HandlerLeak")
    private final class LocationHandler extends Handler {
        private static final int MSG_ENABLE_LOCATION_UPDATES = 1;
        private static final int MSG_GET_NEW_LOCATION_UPDATE = 2;
        private static final int MSG_PROCESS_NEW_LOCATION = 3;
        private static final int MSG_DO_TWILIGHT_UPDATE = 4;

        private static final long LOCATION_UPDATE_MS = 24 * DateUtils.HOUR_IN_MILLIS;
        private static final long MIN_LOCATION_UPDATE_MS = 30 * DateUtils.MINUTE_IN_MILLIS;
        private static final float LOCATION_UPDATE_DISTANCE_METER = 1000 * 20;
        private static final long LOCATION_UPDATE_ENABLE_INTERVAL_MIN = 5000;
        private static final long LOCATION_UPDATE_ENABLE_INTERVAL_MAX =
                15 * DateUtils.MINUTE_IN_MILLIS;
        private static final double FACTOR_GMT_OFFSET_LONGITUDE =
                1000.0 * 360.0 / DateUtils.DAY_IN_MILLIS;

        private boolean mPassiveListenerEnabled;
        private boolean mNetworkListenerEnabled;
        private boolean mDidFirstInit;
        private long mLastNetworkRegisterTime = -MIN_LOCATION_UPDATE_MS;
        private long mLastUpdateInterval;
        private Location mLocation;
        private final TwilightCalculator mTwilightCalculator = new TwilightCalculator();

        void processNewLocation(Location location) {
            Message msg = obtainMessage(MSG_PROCESS_NEW_LOCATION, location);
            sendMessage(msg);
        }

        void enableLocationUpdates() {
            sendEmptyMessage(MSG_ENABLE_LOCATION_UPDATES);
        }

        void requestLocationUpdate() {
            sendEmptyMessage(MSG_GET_NEW_LOCATION_UPDATE);
        }

        void requestTwilightUpdate() {
            sendEmptyMessage(MSG_DO_TWILIGHT_UPDATE);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PROCESS_NEW_LOCATION: {
                    final Location location = (Location) msg.obj;
                    final boolean hasMoved = hasMoved(mLocation, location);
                    final boolean hasBetterAccuracy = mLocation == null
                            || location.getAccuracy() < mLocation.getAccuracy();
                    if (DEBUG) {
                        Log.d(TAG, "Processing new location: " + location
                                + ", hasMoved=" + hasMoved
                                + ", hasBetterAccuracy=" + hasBetterAccuracy);
                    }
                    if (hasMoved || hasBetterAccuracy) {
                        setLocation(location);
                    }
                    break;
                }

                case MSG_GET_NEW_LOCATION_UPDATE:
                    if (!mNetworkListenerEnabled) {
                        // Don't do anything -- we are still trying to get a
                        // location.
                        return;
                    }
                    if ((mLastNetworkRegisterTime + MIN_LOCATION_UPDATE_MS) >=
                            SystemClock.elapsedRealtime()) {
                        // Don't do anything -- it hasn't been long enough
                        // since we last requested an update.
                        return;
                    }

                    // Unregister the current location monitor, so we can
                    // register a new one for it to get an immediate update.
                    mNetworkListenerEnabled = false;
                    mLocationManager.removeUpdates(mEmptyLocationListener);

                    // Fall through to re-register listener.
                case MSG_ENABLE_LOCATION_UPDATES:
                    // enable network provider to receive at least location updates for a given
                    // distance.
                    boolean networkLocationEnabled;
                    try {
                        networkLocationEnabled =
                                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    } catch (Exception e) {
                        // we may get IllegalArgumentException if network location provider
                        // does not exist or is not yet installed.
                        networkLocationEnabled = false;
                    }
                    if (!mNetworkListenerEnabled && networkLocationEnabled) {
                        if (Permissions.checkLocation(context)) {
                            mNetworkListenerEnabled = true;
                            mLastNetworkRegisterTime = SystemClock.elapsedRealtime();
                            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                    LOCATION_UPDATE_MS, 0, mEmptyLocationListener);

                            if (!mDidFirstInit) {
                                mDidFirstInit = true;
                                if (mLocation == null) {
                                    retrieveLocation();
                                }
                            }
                        }
                    }

                    // enable passive provider to receive updates from location fixes (gps
                    // and network).
                    boolean passiveLocationEnabled;
                    try {
                        passiveLocationEnabled =
                                mLocationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
                    } catch (Exception e) {
                        // we may get IllegalArgumentException if passive location provider
                        // does not exist or is not yet installed.
                        passiveLocationEnabled = false;
                    }

                    if (!mPassiveListenerEnabled && passiveLocationEnabled) {
                        if (Permissions.checkLocation(context)) {
                            mPassiveListenerEnabled = true;
                            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                                    0, LOCATION_UPDATE_DISTANCE_METER, mLocationListener);
                        }
                    }

                    if (!(mNetworkListenerEnabled && mPassiveListenerEnabled)) {
                        mLastUpdateInterval *= 1.5;
                        if (mLastUpdateInterval == 0) {
                            mLastUpdateInterval = LOCATION_UPDATE_ENABLE_INTERVAL_MIN;
                        } else if (mLastUpdateInterval > LOCATION_UPDATE_ENABLE_INTERVAL_MAX) {
                            mLastUpdateInterval = LOCATION_UPDATE_ENABLE_INTERVAL_MAX;
                        }
                        sendEmptyMessageDelayed(MSG_ENABLE_LOCATION_UPDATES, mLastUpdateInterval);
                    }
                    break;

                case MSG_DO_TWILIGHT_UPDATE:
                    updateTwilightState();
                    break;
            }
        }

        private void retrieveLocation() {
            Location location = null;
            if (Permissions.checkLocation(context)) {
                final Iterator<String> providers =
                        mLocationManager.getProviders(new Criteria(), true).iterator();
                //noinspection WhileLoopReplaceableByForEach
                while (providers.hasNext()) {
                    @SuppressLint("MissingPermission")
                    final Location lastKnownLocation =
                            mLocationManager.getLastKnownLocation(providers.next());
                    // pick the most recent location
                    if (location == null || (lastKnownLocation != null &&
                            location.getElapsedRealtimeNanos() <
                                    lastKnownLocation.getElapsedRealtimeNanos())) {
                        location = lastKnownLocation;
                    }
                }
            }

            // In the case there is no location available (e.g. GPS fix or network location
            // is not available yet), the longitude of the location is estimated using the timezone,
            // latitude and accuracy are set to get a good average.
            if (location == null) {
                Time currentTime = new Time();
                currentTime.set(System.currentTimeMillis());
                double lngOffset = FACTOR_GMT_OFFSET_LONGITUDE *
                        (currentTime.gmtoff - (currentTime.isDst > 0 ? 3600 : 0));
                location = new Location("fake");
                location.setLongitude(lngOffset);
                location.setLatitude(0);
                location.setAccuracy(417000.0f);
                location.setTime(System.currentTimeMillis());
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

                if (DEBUG) {
                    Log.d(TAG, "Estimated location from timezone: " + location);
                }
            }

            setLocation(location);
        }

        private void setLocation(Location location) {
            mLocation = location;
            updateTwilightState();
        }

        private void updateTwilightState() {
            if (mLocation == null) {
                setTwilightState(null);
                return;
            }

            final long now = System.currentTimeMillis();

            // calculate yesterday's twilight
            mTwilightCalculator.calculateTwilight(now - DateUtils.DAY_IN_MILLIS,
                    mLocation.getLatitude(), mLocation.getLongitude());
            final long yesterdaySunrise = mTwilightCalculator.mSunrise;
            final long yesterdaySunset = mTwilightCalculator.mSunset;

            // calculate today's twilight
            mTwilightCalculator.calculateTwilight(now,
                    mLocation.getLatitude(), mLocation.getLongitude());
            final boolean isNight = (mTwilightCalculator.mState == TwilightCalculator.NIGHT);
            final long todaySunrise = mTwilightCalculator.mSunrise;
            final long todaySunset = mTwilightCalculator.mSunset;

            // calculate tomorrow's twilight
            mTwilightCalculator.calculateTwilight(now + DateUtils.DAY_IN_MILLIS,
                    mLocation.getLatitude(), mLocation.getLongitude());
            final long tomorrowSunrise = mTwilightCalculator.mSunrise;
            final long tomorrowSunset = mTwilightCalculator.mSunset;

            long[] daysSunrise = new long[9];
            long[] daysSunset = new long[9];
            Calendar calNow = Calendar.getInstance();
            calNow.add(Calendar.DAY_OF_YEAR, -1); // index 0 = previous day
            long calNowMillis = calNow.getTimeInMillis();
            for (int i = 0; i < 9; i++) {
                mTwilightCalculator.calculateTwilight(calNowMillis + i*DateUtils.DAY_IN_MILLIS,
                        mLocation.getLatitude(), mLocation.getLongitude());
                daysSunrise[i] = mTwilightCalculator.mSunrise;
                daysSunset[i] = mTwilightCalculator.mSunset;
            }

            // set twilight state
            TwilightState state = new TwilightState(isNight, yesterdaySunrise, yesterdaySunset,
                    todaySunrise, todaySunset, tomorrowSunrise, tomorrowSunset, daysSunrise, daysSunset);
            if (DEBUG) {
                Log.d(TAG, "Updating twilight state: " + state);
            }
            setTwilightState(state);

            // schedule next update
            long nextUpdate = 0;
            if (todaySunrise == -1 || todaySunset == -1) {
                // In the case the day or night never ends the update is scheduled 12 hours later.
                nextUpdate = now + 12 * DateUtils.HOUR_IN_MILLIS;
            } else {
                // add some extra time to be on the safe side.
                nextUpdate += DateUtils.MINUTE_IN_MILLIS;

                if (now > todaySunset) {
                    nextUpdate += tomorrowSunrise;
                } else if (now > todaySunrise) {
                    nextUpdate += todaySunset;
                } else {
                    nextUpdate += todaySunrise;
                }
            }

            if (DEBUG) {
                Log.d(TAG, "Next update in " + (nextUpdate - now) + " ms");
            }

            Intent updateIntent = new Intent(ACTION_UPDATE_TWILIGHT_STATE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, updateIntent, 0);
            mAlarmManager.cancel(pendingIntent);
            mAlarmManager.setExact(AlarmManager.RTC, nextUpdate, pendingIntent);
        }
    }

    private final BroadcastReceiver mUpdateLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())
                    && !intent.getBooleanExtra("state", false)) {
                // Airplane mode is now off!
                mLocationHandler.requestLocationUpdate();
                return;
            }

            // Time zone has changed or alarm expired.
            mLocationHandler.requestTwilightUpdate();
        }
    };

    // A LocationListener to initialize the network location provider. The location updates
    // are handled through the passive location provider.
    private final LocationListener mEmptyLocationListener =  new LocationListener() {
        public void onLocationChanged(Location location) {
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            mLocationHandler.processNewLocation(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

}
