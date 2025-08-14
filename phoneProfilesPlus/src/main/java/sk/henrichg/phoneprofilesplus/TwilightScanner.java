package sk.henrichg.phoneprofilesplus;

import static android.content.Context.RECEIVER_NOT_EXPORTED;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.text.format.Time;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Iterator;

class TwilightScanner {

    //private static final String TAG = "TwilightScanner";
    //private static final boolean DEBUG = false;
    private static final String ACTION_UPDATE_TWILIGHT_STATE = PPApplication.PACKAGE_NAME + ".TwilightScanner.ACTION_UPDATE_TWILIGHT_STATE";

    private final Object mLock = new Object();

    private final Context context;
    private final AlarmManager mAlarmManager;
    private final LocationManager mLocationManager;
    private final LocationHandler mLocationHandler;

    private TwilightState mTwilightState;

    TwilightScanner(Context context) {
        this.context = context.getApplicationContext();

        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationHandler = new LocationHandler(null);
    }

    void start() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(TwilightScanner.ACTION_UPDATE_TWILIGHT_STATE);
        int receiverFlags = 0;
        if (Build.VERSION.SDK_INT >= 34)
            receiverFlags = RECEIVER_NOT_EXPORTED;
        context.registerReceiver(mUpdateLocationReceiver, filter, receiverFlags);

        mLocationHandler.enableLocationUpdates();
    }

    void stop() {
        mLocationHandler.requestLocationUpdate();

        try {
            context.unregisterReceiver(mUpdateLocationReceiver);
        } catch (Exception e) {
            //PPApplicationStatic.recordException(e);
        }
    }

    private void setTwilightState(TwilightState state) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] TwilightScanner.setTwilightState", "mLock");
        synchronized (mLock) {
            if ((mTwilightState == null) || (state == null) || !mTwilightState.equals(state)) {

                mTwilightState = state;

                //final Context appContext = context.getApplicationContext();

                if (!PPApplicationStatic.getApplicationStarted(true, true))
                    // application is not started
                    return;

                if (EventStatic.getGlobalEventsRunning(context)) {
                    PPExecutors.handleEvents(context,
                            new int[]{EventsHandler.SENSOR_TYPE_TIME},
                            PPExecutors.SENSOR_NAME_SENSOR_TYPE_TIME, 10, false);
                }
            }
        }
    }

    TwilightState getTwilightState(/*boolean log*/) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] TwilightScanner.getTwilightState", "mLock");
        synchronized (mLock) {
            mLocationHandler.updateTwilightState(false/*, log*/);
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

        @SuppressWarnings("deprecation")
        LocationHandler(@SuppressWarnings({"SameParameterValue", "unused"}) Looper looper) {
        }

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
//            PPApplicationStatic.logE("[IN_THREAD_HANDLER] TwilightScanner.LocationHandler", "xxx");

            switch (msg.what) {
                case MSG_PROCESS_NEW_LOCATION: {
                    final Location location = (Location) msg.obj;
                    final boolean hasMoved = hasMoved(mLocation, location);
                    final boolean hasBetterAccuracy = mLocation == null
                            || location.getAccuracy() < mLocation.getAccuracy();

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
                    if (mLocationManager != null)
                        mLocationManager.removeUpdates(mEmptyLocationListener);

                    // Fall through to re-register listener.
                case MSG_ENABLE_LOCATION_UPDATES:
                    // enable network provider to receive at least location updates for a given
                    // distance.
                    boolean networkLocationEnabled;
                    try {
                        //noinspection ConstantConditions
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
                        mLastUpdateInterval = Math.round(mLastUpdateInterval * 1.5f);
                        if (mLastUpdateInterval == 0) {
                            mLastUpdateInterval = LOCATION_UPDATE_ENABLE_INTERVAL_MIN;
                        } else if (mLastUpdateInterval > LOCATION_UPDATE_ENABLE_INTERVAL_MAX) {
                            mLastUpdateInterval = LOCATION_UPDATE_ENABLE_INTERVAL_MAX;
                        }
                        sendEmptyMessageDelayed(MSG_ENABLE_LOCATION_UPDATES, mLastUpdateInterval);
                    }
                    break;

                case MSG_DO_TWILIGHT_UPDATE:
                    updateTwilightState(true/*, true*/);
                    break;
            }
        }

        private void retrieveLocation() {
            Location location = null;
            if (Permissions.checkLocation(context)) {
                if (mLocationManager != null) {
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
            }

            // In the case there is no location available (e.g. GPS fix or network location
            // is not available yet), the longitude of the location is estimated using the timezone,
            // latitude and accuracy are set to get a good average.
            if (location == null) {

                //noinspection deprecation
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

            }

            setLocation(location);
        }

        private void setLocation(Location location) {
            mLocation = location;
            updateTwilightState(true/*, true*/);
        }

        void updateTwilightState(boolean setAlarm/*, boolean log*/) {
            if (mLocation == null) {
                setTwilightState(null);
                return;
            }

            Calendar now = Calendar.getInstance();

            // calculate correction
            Calendar[] twilight = SunriseSunset.getSunriseSunset(now, mLocation.getLatitude(), mLocation.getLongitude());

            // check if must be applied correction
            Calendar _now = Calendar.getInstance();
            _now.set(Calendar.HOUR_OF_DAY, 0);
            _now.set(Calendar.MINUTE, 0);
            _now.set(Calendar.SECOND, 0);
            _now.set(Calendar.MILLISECOND, 0);
            Calendar _twilight = twilight[0];
            _twilight.set(Calendar.HOUR_OF_DAY, 0);
            _twilight.set(Calendar.MINUTE, 0);
            _twilight.set(Calendar.SECOND, 0);
            _twilight.set(Calendar.MILLISECOND, 0);

            int correction = -_twilight.compareTo(_now);

            // calculate today twilight
            _now = Calendar.getInstance();
            _now.add(Calendar.DAY_OF_YEAR, correction);
            twilight = SunriseSunset.getSunriseSunset(_now, mLocation.getLatitude(), mLocation.getLongitude());
            boolean isNight = SunriseSunset.isNight(_now, mLocation.getLatitude(), mLocation.getLongitude());
            long todaySunrise = twilight[0].getTimeInMillis();
            long todaySunset = twilight[1].getTimeInMillis();

            // calculate yesterday's twilight
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1 + correction);
            twilight = SunriseSunset.getSunriseSunset(yesterday, mLocation.getLatitude(), mLocation.getLongitude());
            final long yesterdaySunrise = twilight[0].getTimeInMillis();
            final long yesterdaySunset = twilight[1].getTimeInMillis();

            // calculate tomorrow's twilight
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DAY_OF_YEAR, 1 + correction);
            twilight = SunriseSunset.getSunriseSunset(tomorrow, mLocation.getLatitude(), mLocation.getLongitude());
            final long tomorrowSunrise = twilight[0].getTimeInMillis();
            final long tomorrowSunset = twilight[1].getTimeInMillis();

            long[] daysSunrise = new long[9];
            long[] daysSunset = new long[9];
            Calendar day = Calendar.getInstance();

            day.add(Calendar.DAY_OF_YEAR, -1 + correction); // index = 0 -> yesterday,

            for (int i = 0; i < 9; i++) {
                twilight = SunriseSunset.getSunriseSunset(day, mLocation.getLatitude(), mLocation.getLongitude());
                daysSunrise[i] = twilight[0].getTimeInMillis();
                daysSunset[i] = twilight[1].getTimeInMillis();
                day.add(Calendar.DAY_OF_YEAR, 1);
            }

            // set twilight state
            TwilightState state = new TwilightState(isNight, yesterdaySunrise, yesterdaySunset,
                    todaySunrise, todaySunset, tomorrowSunrise, tomorrowSunset, daysSunrise, daysSunset);

            setTwilightState(state);

            if (setAlarm) {
                // schedule next update
                long nextUpdate = 0;
                if (todaySunrise == -1 || todaySunset == -1) {
                    // In the case the day or night never ends the update is scheduled 12 hours later.
                    nextUpdate = now.getTimeInMillis() + 12 * DateUtils.HOUR_IN_MILLIS;
                } else {
                    // add some extra time to be on the safe side.
                    nextUpdate += DateUtils.MINUTE_IN_MILLIS;

                    if (now.getTimeInMillis() > todaySunset) {
                        nextUpdate += tomorrowSunrise;
                    } else if (now.getTimeInMillis() > todaySunrise) {
                        nextUpdate += todaySunset;
                    } else {
                        nextUpdate += todaySunrise;
                    }
                }

                // remove alarm
                try {
                    Intent updateIntent = new Intent(ACTION_UPDATE_TWILIGHT_STATE);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_NO_CREATE);
                    if (pendingIntent != null) {
                        if (mAlarmManager != null)
                            mAlarmManager.cancel(pendingIntent);
                        pendingIntent.cancel();
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
                //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_TWILIGHT_SCANNER_TAG_WORK);

                // set alarm
                Intent updateIntent = new Intent(ACTION_UPDATE_TWILIGHT_STATE);

                if (mAlarmManager != null) {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_NO_CREATE);
                    if (pendingIntent != null) {

                        mAlarmManager.cancel(pendingIntent);
                        pendingIntent.cancel();
                    }

                    pendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(nextUpdate, infoPendingIntent);
                        mAlarmManager.setAlarmClock(clockInfo, pendingIntent);
                    } else {
                        mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent);
                    }
                }
            }
        }
    }

    private final BroadcastReceiver mUpdateLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            PPApplicationStatic.logE("[IN_BROADCAST] TwilightScanner.mUpdateLocationReceiver", "xxx");
//            PPApplicationStatic.logE("[IN_BROADCAST_ALARM] TwilightScanner.mUpdateLocationReceiver", "xxx");

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

    static void doWork() {
        if (PhoneProfilesService.getInstance() != null) {
            if (PPApplication.twilightScanner != null) {
                // Time zone has changed or alarm expired.
                PPApplication.twilightScanner.mLocationHandler.requestTwilightUpdate();
            }
        }
    }

    // A LocationListener to initialize the network location provider. The location updates
    // are handled through the passive location provider.
    private final LocationListener mEmptyLocationListener =  new LocationListener() {
        public void onLocationChanged(@NonNull Location location) {
//            PPApplicationStatic.logE("[IN_LISTENER] TwilightScanner.mEmptyLocationListener.onLocationChanged", "xxx");
        }

        public void onProviderDisabled(@NonNull String provider) {
//            PPApplicationStatic.logE("[IN_LISTENER] TwilightScanner.mEmptyLocationListener.onProviderDisabled", "xxx");
        }

        public void onProviderEnabled(@NonNull String provider) {
//            PPApplicationStatic.logE("[IN_LISTENER] TwilightScanner.mEmptyLocationListener.onProviderEnabled", "xxx");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
//            PPApplicationStatic.logE("[IN_LISTENER] TwilightScanner.mEmptyLocationListener.onStatusChanged", "xxx");
        }
    };

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(@NonNull Location location) {
//            PPApplicationStatic.logE("[IN_LISTENER] TwilightScanner.mLocationListener.onLocationChanged", "xxx");
            mLocationHandler.processNewLocation(location);
        }

        public void onProviderDisabled(@NonNull String provider) {
//            PPApplicationStatic.logE("[IN_LISTENER] TwilightScanner.mLocationListener.onProviderDisabled", "xxx");
        }

        public void onProviderEnabled(@NonNull String provider) {
//            PPApplicationStatic.logE("[IN_LISTENER] TwilightScanner.mLocationListener.onProviderEnabled", "xxx");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
//            PPApplicationStatic.logE("[IN_LISTENER] TwilightScanner.mLocationListener.onStatusChanged", "xxx");
        }
    };

}
