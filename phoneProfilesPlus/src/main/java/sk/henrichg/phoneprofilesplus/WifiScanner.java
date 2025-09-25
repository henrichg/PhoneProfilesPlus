package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;

import java.lang.ref.WeakReference;

/** @noinspection ExtractMethodRecommender*/
class WifiScanner {

    private final Context context;

    static final int WIFI_SCAN_DURATION = 25;      // 25 seconds for wifi scan

    //static boolean wifiEnabledForScan;

    private static final String PREF_FORCE_ONE_WIFI_SCAN = "forceOneWifiScanInt";

    static final int FORCE_ONE_SCAN_DISABLED = 0;
    static final int FORCE_ONE_SCAN_FROM_PREF_DIALOG = 3;

    //private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_WIFI = "show_enable_location_notification_wifi";

    WifiScanner(Context context) {
        this.context = context.getApplicationContext();
    }

    void doScan(boolean fromDialog) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] WifiScanner.doScan", "PPApplication.wifiScannerMutex");
        synchronized (PPApplication.wifiScannerMutex) {
            if (!PPApplicationStatic.getApplicationStarted(true, true))
                // application is not started
                return;

            //DataWrapper dataWrapper;

            // check power save mode
            //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
            boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(context);
            int forceScan = ApplicationPreferences.prefForceOneWifiScan;
            if (isPowerSaveMode) {
                if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                    if (ApplicationPreferences.applicationEventWifiScanInPowerSaveMode.equals("2")) {
                        // not scan wi-fi in power save mode
                        return;
                    }
                }
            }
            else {
                if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                    if (ApplicationPreferences.applicationEventWifiScanInTimeMultiply.equals("2")) {
                        if (GlobalUtils.isNowTimeBetweenTimes(
                                ApplicationPreferences.applicationEventWifiScanInTimeMultiplyFrom,
                                ApplicationPreferences.applicationEventWifiScanInTimeMultiplyTo)) {
                            // not scan wi-fi in configured time
                            return;
                        }
                    }
                }
            }

            //PPApplication.startHandlerThreadPPScanners(/*"WifiScanner.doScan.1"*/);
            //final Handler wifiChangeHandler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());

            //synchronized (PPApplication.radioChangeStateMutex) {

                WifiScanWorker.fillWifiConfigurationList(context/*, false*/);

                boolean canScan = EventStatic.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED;
//                PPApplicationStatic.logE("[BLUETOOTH] WifiScanner.doScan", "^^^^^^^^^ (1) canScan="+canScan);

                if (canScan) {
                    if (!ApplicationPreferences.applicationEventWifiScanIgnoreHotspot) {
                        if (Build.VERSION.SDK_INT < 30)
                            canScan = !WifiApManager.isWifiAPEnabled(context);
                        else
                            //canScan = !CmdWifiAP.isEnabled(context);
                            canScan = !WifiApManager.isWifiAPEnabledA30(context);
                    }
                }
//                PPApplicationStatic.logE("[BLUETOOTH] WifiScanner.doScan", "^^^^^^^^^ (2) canScan="+canScan);

                if (canScan) {

                    //dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

                    // check if wifi scan events exists
                    //lock();
                    //boolean wifiEventsExists = DatabaseHandler.getInstance(context.getApplicationContext()).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false) > 0;
                    //unlock();
                    //int forceScan = ApplicationPreferences.prefForceOneWifiScan;
                    boolean scan; //(wifiEventsExists || (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                    //if (scan) {
                    //    if (wifiEventsExists)
                            scan = isLocationEnabled(context/*, scannerType*/);
                    //}
//                    PPApplicationStatic.logE("[BLUETOOTH] WifiScanner.doScan", "^^^^^^^^^ scan="+scan);
                    if (!scan) {
                        // wifi scan events not exists
                        WifiScanWorker.cancelWork(context, fromDialog/*, null*/);
                    } else {
//                        PPApplicationStatic.logE("[BLUETOOTH] WifiScanner.doScan", "^^^^^^^^^ prefEventWifiEnabledForScan="+ApplicationPreferences.prefEventWifiEnabledForScan);
                        if (ApplicationPreferences.prefEventWifiEnabledForScan) {
                            final Context appContext = context.getApplicationContext();
                            // service restarted during scanning (prefEventWifiEnabledForScan is set to false at end of scan),
                            // disable wifi
                            Runnable runnable = () -> {
                                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                PowerManager.WakeLock wakeLock = null;
                                try {
                                    if (powerManager != null) {
                                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_WifScaner_doScan_1);
                                        wakeLock.acquire(10 * 60 * 1000);
                                    }

                                    try {
    //                                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=WifiScanner.doScan.1");
                                        if (WifiScanWorker.wifi == null)
                                            WifiScanWorker.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                                        //lock();
                                        //if (Build.VERSION.SDK_INT >= 29)
                                        //    CmdWifi.setWifi(false);
                                        //else
                                        ActivateProfileHelper.setWifi(appContext, false);
                                        //if (WifiScanWorker.wifi != null)
                                        //    WifiScanWorker.wifi.setWifiEnabled(false);

    //                                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "END run - from=WifiScanner.doScan.1");
                                    } catch (Exception e) {
                                        PPApplicationStatic.recordException(e);
                                    }

                                } catch (Exception e) {
//                                    PPApplicationStatic.logE("[WAKELOCK_EXCEPTION] WifiScanner.doScan", Log.getStackTraceString(e));
                                    PPApplicationStatic.recordException(e);
                                } finally {
                                    if ((wakeLock != null) && wakeLock.isHeld()) {
                                        try {
                                            wakeLock.release();
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }

                            };
                            PPApplicationStatic.createScannersExecutor();
                            PPApplication.scannersExecutor.submit(runnable);
                            if (WifiScanWorker.wifi == null)
                                WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            waitForWifiDisabled(WifiScanWorker.wifi);
                            //PPApplication.sleep(1000);
                            //unlock();
                        }

//                        PPApplicationStatic.logE("[BLUETOOTH] WifiScanner.doScan", "^^^^^^^^^ continue");

                        //noinspection ConstantConditions
                        if (true /*canScanWifi(dataWrapper)*/) { // scan even if wifi is connected

                            WifiScanWorker.setScanRequest(context, false);
                            WifiScanWorker.setWaitForResults(context, false);
                            WifiScanWorker.setWifiEnabledForScan(context, false);

                            WifiScanWorker.unlock();

                            // start scan

                            //lock();

                            // enable wifi
                            int wifiState;
                            wifiState = enableWifi(WifiScanWorker.wifi/*, wifiChangeHandler*/);

                            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
//                                PPApplicationStatic.logE("[BLUETOOTH] WifiScanner.doScan", "^^^^^^^^^ WIFI_STATE_ENABLED");
                                /*WifiScanWorker.*/startScan(context);
                            } else if (wifiState != WifiManager.WIFI_STATE_ENABLING) {
//                                PPApplicationStatic.logE("[BLUETOOTH] WifiScanner.doScan", "^^^^^^^^^ WIFI_STATE_ENABLING");
                                WifiScanWorker.setScanRequest(context, false);
                                WifiScanWorker.setWaitForResults(context, false);
                                setForceOneWifiScan(context, FORCE_ONE_SCAN_DISABLED);
                            }

                            if (ApplicationPreferences.prefEventWifiScanRequest ||
                                    ApplicationPreferences.prefEventWifiWaitForResult) {

                                // wait for scan end
                                waitForWifiScanEnd(/*context*/);


                                if (ApplicationPreferences.prefEventWifiWaitForResult) {
                                    if (ApplicationPreferences.prefForceOneWifiScan != WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                                    {
                                        if (fromDialog) {
//                                            PPApplicationStatic.logE("[DELAYED_EXECUTOR_CALL] WifiScanner.doScan", "PPExecutors.handleEvents");
                                            PPExecutors.handleEvents(context,
                                                    new int[]{EventsHandler.SENSOR_TYPE_WIFI_SCANNER},
                                                    PPExecutors.SENSOR_NAME_SENSOR_TYPE_WIFI_SCANNER, 5);
                                        }
                                        else {
                                            EventsHandler eventsHandler = new EventsHandler(context);
                                            eventsHandler.handleEvents(new int[]{
                                                    EventsHandler.SENSOR_TYPE_WIFI_SCANNER});
                                        }
                                    }
                                }
                            }

                            WifiScanWorker.unlock();
                            //unlock();
                        }
                    }

                    if (ApplicationPreferences.prefEventWifiEnabledForScan) {
                        final Context appContext = context.getApplicationContext();
                        Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=WifiScanner.doScan.2");

                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_WifScaner_doScan_2);
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                try {
                                    if (WifiScanWorker.wifi == null)
                                        WifiScanWorker.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                                    //lock();
                                    //if (Build.VERSION.SDK_INT >= 29)
                                    //    CmdWifi.setWifi(false);
                                    //else
                                    ActivateProfileHelper.setWifi(appContext, false);
                                    //if (WifiScanWorker.wifi != null)
                                    //    WifiScanWorker.wifi.setWifiEnabled(false);
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
                                }

//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "END run - from=WifiScanner.doScan.1");
                            } catch (Exception e) {
//                                PPApplicationStatic.logE("[WAKELOCK_EXCEPTION] WifiScanner.doScan", Log.getStackTraceString(e));
                                PPApplicationStatic.recordException(e);
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }

                        };
                        PPApplicationStatic.createScannersExecutor();
                        PPApplication.scannersExecutor.submit(runnable);
                        //PPApplication.sleep(1000);
                        if (WifiScanWorker.wifi == null)
                            WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        waitForWifiDisabled(WifiScanWorker.wifi);
                        //unlock();
                    }
                }

                setForceOneWifiScan(context, FORCE_ONE_SCAN_DISABLED);
                WifiScanWorker.setWifiEnabledForScan(context, false);
                WifiScanWorker.setWaitForResults(context, false);
                WifiScanWorker.setScanRequest(context, false);

                WifiScanWorker.unlock();
                //unlock();

            //}

        }
    }

    static void getForceOneWifiScan(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] WifiScanner.getForceOneWifiScan", "PPApplication.eventWifiSensorMutex");
        synchronized (PPApplication.eventWifiSensorMutex) {
            ApplicationPreferences.prefForceOneWifiScan = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_FORCE_ONE_WIFI_SCAN, FORCE_ONE_SCAN_DISABLED);
        }
    }
    static void setForceOneWifiScan(Context context, int forceScan)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] WifiScanner.setForceOneWifiScan", "PPApplication.eventWifiSensorMutex");
        synchronized (PPApplication.eventWifiSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_FORCE_ONE_WIFI_SCAN, forceScan);
            editor.apply();
            ApplicationPreferences.prefForceOneWifiScan = forceScan;
        }
    }

    /*
    private void lock() {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null)
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ScanWakeLock");
        try {
            if (!wakeLock.isHeld())
                wakeLock.acquire(10 * 60 * 1000);
        } catch(Exception e) {
            Log.e("WifiScanner.lock", "Error getting Lock: ", e);
        }
    }

    private void unlock() {
        if ((wakeLock != null) && (wakeLock.isHeld())) {
            wakeLock.release();
        }
    }
    */

    private void startScan(Context context)
    {
        WifiScanWorker.lock(context); // lock wakeLock and wifiLock, then scan.
        // unlock() is then called at the end of the onReceive function of WifiScanBroadcastReceiver
        try {
            if (WifiScanWorker.wifi == null)
                WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            boolean startScan = false;
            if (WifiScanWorker.wifi != null) {
//                PPApplicationStatic.logE("[BLUETOOTH] WifiScanner.startScan", "^^^^^^^^^ wifi.startScan()");
                startScan = WifiScanWorker.wifi.startScan();
            }
//            PPApplicationStatic.logE("[BLUETOOTH] WifiScanner.startScan", "^^^^^^^^^ startScan="+startScan);
            if (!startScan) {
                if (ApplicationPreferences.prefEventWifiEnabledForScan) {
                    //if (Build.VERSION.SDK_INT >= 29)
                    //    CmdWifi.setWifi(false);
                    //else
                    ActivateProfileHelper.setWifi(context.getApplicationContext(), false);
                    //if (WifiScanWorker.wifi != null)
                    //    WifiScanWorker.wifi.setWifiEnabled(false);
                }
                WifiScanWorker.unlock();
            }
            WifiScanWorker.setWaitForResults(context, startScan);
            WifiScanWorker.setScanRequest(context, false);
        } catch (Exception e) {
            if (ApplicationPreferences.prefEventWifiEnabledForScan) {
                //if (Build.VERSION.SDK_INT >= 29)
                //    CmdWifi.setWifi(false);
                //else {
                if (WifiScanWorker.wifi == null)
                    WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                ActivateProfileHelper.setWifi(context.getApplicationContext(), false);
                //if (WifiScanWorker.wifi != null)
                //    WifiScanWorker.wifi.setWifiEnabled(false);
                //}
            }
            WifiScanWorker.unlock();
            WifiScanWorker.setWaitForResults(context, false);
            WifiScanWorker.setScanRequest(context, false);
        }
    }

    private int enableWifi(WifiManager wifi/*, Handler wifiChangeHandler*/)
    {
        int wifiState = wifi.getWifiState();
        int forceScan = ApplicationPreferences.prefForceOneWifiScan;

        //if ((!dataWrapper.getIsManualProfileActivation()) || forceScan)
        //{
        if (wifiState != WifiManager.WIFI_STATE_ENABLING)
        {
            boolean isWifiEnabled = (wifiState == WifiManager.WIFI_STATE_ENABLED);
            boolean isScanAlwaysAvailable = false;
            if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                // this must be disabled because scanning not working, when wifi is disabled after disabled WiFi AP
                // Tested and scanning working ;-)
                isScanAlwaysAvailable = wifi.isScanAlwaysAvailable();
            }
//            PPApplicationStatic.logE("[BLUETOOTH] WifiScanner.enableWifi", "isWifiEnabled="+isWifiEnabled);
//            PPApplicationStatic.logE("[BLUETOOTH] WifiScanner.enableWifi", "isScanAlwaysAvailable="+isScanAlwaysAvailable);
            isWifiEnabled = isWifiEnabled || isScanAlwaysAvailable;
            if (!isWifiEnabled)
            {
                boolean applicationEventWifiScanIfWifiOff = ApplicationPreferences.applicationEventWifiScanIfWifiOff;
                if (applicationEventWifiScanIfWifiOff || (forceScan != FORCE_ONE_SCAN_DISABLED))
                {
                    //boolean wifiEventsExists = DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false) > 0;
                    boolean scan = ((/*wifiEventsExists &&*/ applicationEventWifiScanIfWifiOff) ||
                            (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                    if (scan)
                    {
                        WifiScanWorker.setWifiEnabledForScan(context, true);
                        WifiScanWorker.setScanRequest(context, true);
                        WifiScanWorker.lock(context);
                        final Context appContext = context.getApplicationContext();
                        final WeakReference<WifiManager> wifiWeakRef = new WeakReference<>(wifi);
                        final WeakReference<WifiScanner> scannerWeakRef = new WeakReference<>(this);
                        Runnable runnable = () -> {
                            //if (PPApplicationStatic.logEnabled()) {
//                                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=WifiScanner.enableWifi");
                            //}

                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_WifScaner_enableWifi);
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                WifiManager _wifi = wifiWeakRef.get();
                                WifiScanner scanner = scannerWeakRef.get();

                                if ((_wifi != null) && (scanner != null)) {
                                    //if (Build.VERSION.SDK_INT >= 29)
                                    //    CmdWifi.setWifi(true);
                                    //else
                                    ActivateProfileHelper.setWifi(appContext, true);
                                    //_wifi.setWifiEnabled(true);
    //                                PPApplicationStatic.logE("[BLUETOOTH] WifiScanner.enableWifi", "(1) @@@@@@@");

                                    long start = SystemClock.uptimeMillis();
                                    do {
                                        if (!ApplicationPreferences.prefEventWifiScanRequest)
                                            break;
                                        if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                                            GlobalUtils.sleep(5000);
    //                                        PPApplicationStatic.logE("[BLUETOOTH] WifiScanner.enableWifi", "(2) @@@@@@@");
    //                                        Log.e("WifiScanner.enableWifi", "start wifi scan");
                                            scanner.startScan(appContext);
                                            break;
                                        }
                                        GlobalUtils.sleep(200);
                                    } while (SystemClock.uptimeMillis() - start < 30 * 1000);
                                }

                            } catch (Exception e) {
//                                PPApplicationStatic.logE("[WAKELOCK_EXCEPTION] WifiScanner.enableWifi", Log.getStackTraceString(e));
                                PPApplicationStatic.recordException(e);
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }

                        };
                        PPApplicationStatic.createScannersExecutor();
                        PPApplication.scannersExecutor.submit(runnable);
                        return WifiManager.WIFI_STATE_ENABLING;
                    }
                }
            }
            else
            {
                // this is not needed, enableWifi() is called only from doScan and after when hotspot is disabled
                /*boolean isWifiAPEnabled = false;
                if (Build.VERSION.SDK_INT < 28) {
                    WifiApManager wifiApManager = null;
                    try {
                        wifiApManager = new WifiApManager(context);
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                    if (wifiApManager != null)
                        isWifiAPEnabled = wifiApManager.isWifiAPEnabled();
                }
                else
                    isWifiAPEnabled = CmdWifiAP.isEnabled();*/

                if (isScanAlwaysAvailable/*  && !isWifiAPEnabled*/) {
                    wifiState =  WifiManager.WIFI_STATE_ENABLED;
                }
                return wifiState;
            }
        }
        //}

        return wifiState;
    }

    private void waitForWifiDisabled(WifiManager wifi) {
        long start = SystemClock.uptimeMillis();
        do {
            int wifiState = wifi.getWifiState();
            if (wifiState == WifiManager.WIFI_STATE_DISABLED)
                break;
            /*if (asyncTask != null)
            {
                if (asyncTask.isCancelled())
                    break;
            }*/

            GlobalUtils.sleep(200);
        } while (SystemClock.uptimeMillis() - start < 5 * 1000);
    }

    private void waitForWifiScanEnd(/*Context context*//*, AsyncTask<Void, Integer, Void> asyncTask*/)
    {
        long start = SystemClock.uptimeMillis();
        do {
            if (!(ApplicationPreferences.prefEventWifiScanRequest ||
                    ApplicationPreferences.prefEventWifiWaitForResult)) {
                break;
            }
            /*if (asyncTask != null)
            {
                if (asyncTask.isCancelled())
                    break;
            }*/

            GlobalUtils.sleep(200);
        } while (SystemClock.uptimeMillis() - start < WIFI_SCAN_DURATION * 1000);
    }

    private static boolean isLocationEnabled(Context context/*, String scanType*/) {
            // check for Location Settings

            /* isScanAlwaysAvailable() may be disabled for unknown reason :-(
            //boolean isScanAlwaysAvailable = true;
            if (WifiScanWorker.wifi == null)
                WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int wifiState = WifiScanWorker.wifi.getWifiState();
            boolean isWifiEnabled = (wifiState == WifiManager.WIFI_STATE_ENABLED);
            isScanAlwaysAvailable = isWifiEnabled || WifiScanWorker.wifi.isScanAlwaysAvailable();
            */

            if (!GlobalUtils.isLocationEnabled(context)/* || (!isScanAlwaysAvailable)*/) {
                // Location settings are not properly set, show notification about it

                /*
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {

                    if (getShowEnableLocationNotification(context, scanType)) {
                        //Intent notificationIntent = new Intent(context, PhoneProfilesPrefsActivity.class);
                        Intent notificationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        String notificationText;
                        String notificationBigText;

                        notificationText = context.getString(R.string.phone_profiles_pref_category_wifi_scanning);
                        notificationBigText = context.getString(R.string.phone_profiles_pref_eventWiFiLocationSystemSettings_summary);

                        String nTitle = notificationText;
                        String nText = notificationBigText;
                        PPApplication.createExclamationNotificationChannel(context);
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                                .setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.notification_color))
                                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                                .setContentTitle(nTitle) // title for notification
                                .setContentText(nText) // message for notification
                                .setAutoCancel(true); // clear notification after click
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));

                        int requestCode;
                        //notificationIntent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_WIFI_SCANNING_CATEGORY);
                        requestCode = 1;
                        //notificationIntent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");

                        PendingIntent pi = PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(pi);
                        mBuilder.setPriority(Notification.PRIORITY_MAX);
                        mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
                        mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            mNotificationManager.notify(LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_TAG,
                                                        PPApplication.LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID, mBuilder.build());
                        }

                        setShowEnableLocationNotification(context, false, scanType);
                    }
                }
                */

                return false;
            }
            else {
                /*NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(PPApplication.LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID);
                }
                setShowEnableLocationNotification(context, true, scanType);*/
                return true;
            }
    }

}
