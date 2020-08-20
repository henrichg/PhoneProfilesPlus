package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

class PhoneStateScanner extends PhoneStateListener {

    private final Context context;
    private final TelephonyManager telephonyManager;
    //private TelephonyManager telephonyManager2 = null;

    static int registeredCell = Integer.MAX_VALUE;
    static long lastConnectedTime = 0;
    static String lastRunningEventsNotOutside = "";
    static String lastPausedEventsOutside = "";

    //static boolean forceStart = false;

    static boolean enabledAutoRegistration = false;
    static int durationForAutoRegistration = 0;
    static String cellsNameForAutoRegistration = "";
    static private final List<Long> autoRegistrationEventList = Collections.synchronizedList(new ArrayList<Long>());

    static final String NEW_MOBILE_CELLS_NOTIFICATION_DELETED_ACTION = PPApplication.PACKAGE_NAME + ".PhoneStateScanner.NEW_MOBILE_CELLS_NOTIFICATION_DELETED";
    static final String NEW_MOBILE_CELLS_NOTIFICATION_DISABLE_ACTION = PPApplication.PACKAGE_NAME + ".PhoneStateScanner.NEW_MOBILE_CELLS_NOTIFICATION_DISABLE_ACTION";

    //private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_PHONE_STATE = "show_enable_location_notification_phone_state";

    //static MobileCellsRegistrationService autoRegistrationService = null;

    //static String ACTION_PHONE_STATE_CHANGED = PPApplication.PACKAGE_NAME + ".ACTION_PHONE_STATE_CHANGED";

    PhoneStateScanner(Context context) {
        //PPApplication.logE("PhoneStateScanner.constructor", "xxx");
        this.context = context;
        /*if (Build.VERSION.SDK_INT >= 24) {
            TelephonyManager telephonyManager = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                SubscriptionManager mSubscriptionManager = SubscriptionManager.from(context);
                // Loop through the subscription list i.e. SIM list.
                List<SubscriptionInfo> subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                if (subscriptionList != null) {
                    for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                        SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                        int subscriptionId = subscriptionInfo.getSubscriptionId();
                        if (telephonyManager1 == null)
                            telephonyManager1 = telephonyManager.createForSubscriptionId(subscriptionId);
                        if (telephonyManager2 == null)
                            telephonyManager2 = telephonyManager.createForSubscriptionId(subscriptionId);
                    }
                } else
                    telephonyManager1 = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
            }
        }
        else {*/
            telephonyManager = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
        //}
        MobileCellsRegistrationService.getMobileCellsAutoRegistration(context);
    }

    @SuppressLint("InlinedApi")
    void connect() {
        //PPApplication.logE("PhoneStateScanner.connect", "xxx");
        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if (/*PPApplication.*/isPowerSaveMode && ApplicationPreferences.applicationEventMobileCellsScanInPowerSaveMode.equals("2"))
            // start scanning in power save mode is not allowed
            return;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("PhoneStateScanner.connect", "telephonyManager=" + telephonyManager);
            PPApplication.logE("PhoneStateScanner.connect", "FEATURE_TELEPHONY=" + PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY));
            PPApplication.logE("PhoneStateScanner.connect", "checkLocation=" + Permissions.checkLocation(context.getApplicationContext()));
        }*/

        if ((telephonyManager != null) &&
                PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY) &&
                Permissions.checkLocation(context.getApplicationContext())) {
            boolean simIsReady = false;
            if (Build.VERSION.SDK_INT < 26) {
                if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY)
                    // sim card is ready
                    simIsReady = true;
            } else {
                if (Permissions.checkPhone(context.getApplicationContext())) {
                    SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    //SubscriptionManager.from(context);
                    if (mSubscriptionManager != null) {
                        List<SubscriptionInfo> subscriptionList = null;
                        try {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                        } catch (SecurityException e) {
                            PPApplication.recordException(e);
                        }
                        if (subscriptionList != null) {
                            for (int i = 0; i < subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/ i++) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                if (subscriptionInfo != null) {
                                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                                    if (telephonyManager.getSimState(slotIndex) == TelephonyManager.SIM_STATE_READY) {
                                        // sim card is ready
                                        simIsReady = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (simIsReady) {
                //PPApplication.logE("PhoneStateScanner.connect", "telephonyManager.listen");
                telephonyManager.listen(this,
                        //  PhoneStateListener.LISTEN_CALL_STATE
                        PhoneStateListener.LISTEN_CELL_INFO // Requires API 17
                                //| PhoneStateListener.LISTEN_CELL_LOCATION
                                //| PhoneStateListener.LISTEN_DATA_ACTIVITY
                                //| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                                | PhoneStateListener.LISTEN_SERVICE_STATE
                        //| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        //| PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                        //| PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                );
                //checkLocationEnabled();
            }
        }
        startAutoRegistration(context, true);
    }

    void disconnect() {
        //PPApplication.logE("PhoneStateScanner.disconnect", "xxx");
        if ((telephonyManager != null) && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY))
            telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
        stopAutoRegistration(context, false);
    }

    /*
    void resetListening(boolean oldPowerSaveMode, boolean forceReset) {
        if ((forceReset) || (PPApplication.isPowerSaveMode != oldPowerSaveMode)) {
            disconnect();
            connect();
        }
    }
    */

    /*
    private void checkLocationEnabled() {
        if (Build.VERSION.SDK_INT >= 28) {
            // check for Location Settings

            if (!PhoneProfilesService.isLocationEnabled(context)) {
                // Location settings are not properly set, show notification about it

                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {

                    if (getShowEnableLocationNotification(context)) {
                        //Intent notificationIntent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                        Intent notificationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        String notificationText;
                        String notificationBigText;

                        notificationText = context.getString(R.string.phone_profiles_pref_category_mobile_cells_scanning);
                        notificationBigText = context.getString(R.string.phone_profiles_pref_eventMobileCellsLocationSystemSettings_summary);

                        String nTitle = notificationText;
                        String nText = notificationBigText;
                        PPApplication.createExclamationNotificationChannel(context);
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                                .setColor(ContextCompat.getColor(context, R.color.primary))
                                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                                .setContentTitle(nTitle) // title for notification
                                .setContentText(nText) // message for notification
                                .setAutoCancel(true); // clear notification after click
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));

                        int requestCode = 3;
                        //notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "mobileCellsScanningCategory");
                        //notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");

                        PendingIntent pi = PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(pi);
                        mBuilder.setPriority(Notification.PRIORITY_MAX);
                        mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
                        mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            mNotificationManager.notify(PPApplication.LOCATION_SETTINGS_FOR_MOBILE_CELLS_SCANNING_NOTIFICATION_TAG,
                                                PPApplication.LOCATION_SETTINGS_FOR_MOBILE_CELLS_SCANNING_NOTIFICATION_ID, mBuilder.build());
                        }

                        setShowEnableLocationNotification(context, false);
                    }
                }
            }
            else {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(PPApplication.LOCATION_SETTINGS_FOR_MOBILE_CELLS_SCANNING_NOTIFICATION_ID);
                }
                setShowEnableLocationNotification(context, true);
            }

        }
        else {
            setShowEnableLocationNotification(context, true);
        }
    }
    */

    static boolean isValidCellId(int cid) {
        return (cid != -1) /*&& (cid != 0) && (cid != 1)*/ && (cid != Integer.MAX_VALUE);
    }

    private void getAllCellInfo(List<CellInfo> cellInfo) {
        // only for registered cells is returned identify
        // SlimKat in Galaxy Nexus - returns null :-/
        // Honor 7 - returns empty list (not null), Dual SIM?

        //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "cellInfo="+cellInfo);

        if (cellInfo!=null) {

            if (Permissions.checkLocation(context.getApplicationContext())) {

                //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "---- start ----------------------------");

                boolean anyRegistered = false;

                for (CellInfo _cellInfo : cellInfo) {
                    //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "registered="+_cellInfo.isRegistered());

                    boolean isRegistered = false;

                    if (_cellInfo instanceof CellInfoGsm) {
                        //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "gsm info="+_cellInfo);
                        CellIdentityGsm identityGsm = ((CellInfoGsm) _cellInfo).getCellIdentity();
                        //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "gsm cid="+identityGsm.getCid());
                        if (isValidCellId(identityGsm.getCid())) {
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityGsm.getCid();
                                //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "gsm registeredCell="+registeredCell);
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                                isRegistered = true;
                            }
                        }
                    } else if (_cellInfo instanceof CellInfoLte) {
                        //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "lte info="+_cellInfo);
                        CellIdentityLte identityLte = ((CellInfoLte) _cellInfo).getCellIdentity();
                        //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "lte cid="+identityLte.getCi());
                        if (isValidCellId(identityLte.getCi())) {
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityLte.getCi();
                                //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "lte registeredCell="+registeredCell);
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                                isRegistered = true;
                            }
                        }
                    } else if (_cellInfo instanceof CellInfoWcdma) {
                        //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "wcdma info="+_cellInfo);
                        CellIdentityWcdma identityWcdma = ((CellInfoWcdma) _cellInfo).getCellIdentity();
                        //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "wcdma cid=" + identityWcdma.getCid());
                        if (isValidCellId(identityWcdma.getCid())) {
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityWcdma.getCid();
                                //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "wcdma registeredCell="+registeredCell);
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                                isRegistered = true;
                            }
                        }
                    } else if (_cellInfo instanceof CellInfoCdma) {
                        //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "cdma info="+_cellInfo);
                        CellIdentityCdma identityCdma = ((CellInfoCdma) _cellInfo).getCellIdentity();
                        //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "wcdma cid="+identityCdma.getBasestationId());
                        if (isValidCellId(identityCdma.getBasestationId())) {
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityCdma.getBasestationId();
                                //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "cdma registeredCell="+registeredCell);
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                                isRegistered = true;
                            }
                        }
                    }
                    /*else {
                        PPApplication.logE("PhoneStateScanner.getAllCellInfo", "unknown info="+_cellInfo);
                    }*/

                    if (isRegistered) {
                        anyRegistered = true;
                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("PhoneStateScanner.getAllCellInfo", "registeredCell=" + registeredCell);
                            PPApplication.logE("PhoneStateScanner.getAllCellInfo", "is registered, save it");
                        }*/
                        DatabaseHandler db = DatabaseHandler.getInstance(context);
                        db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
                        doAutoRegistration(registeredCell);
                    }
                }

                if (!anyRegistered) {
                    //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "empty cellInfo");
                    registeredCell = Integer.MAX_VALUE;
                    doAutoRegistration(registeredCell);
                }

                //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "---- end ----------------------------");
            }

        }
        //else
        //    PPApplication.logE("PhoneStateScanner.getAllCellInfo", "cell info is null");
    }

    @SuppressLint("MissingPermission")
    private void getAllCellInfo() {
        if (telephonyManager != null) {
            List<CellInfo> cellInfo = null;
            if (Permissions.checkLocation(context.getApplicationContext()))
                cellInfo = telephonyManager.getAllCellInfo();
            //PPApplication.logE("PhoneStateScanner.getAllCellInfo.2", "cellInfo="+cellInfo);
            getAllCellInfo(cellInfo);
        }
    }

    @Override
    public void onCellInfoChanged(final List<CellInfo> cellInfo)
    {
        super.onCellInfoChanged(cellInfo);

        PPApplication.logE("[LISTENER CALL] PhoneStateScanner.onCellInfoChanged", "xxx");

        //PPApplication.logE("PhoneStateScanner.onCellInfoChanged", "telephonyManager="+telephonyManager);
        //CallsCounter.logCounter(context, "PhoneStateScanner.onCellInfoChanged", "PhoneStateScanner_onCellInfoChanged");

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThread(/*"PhoneStateScanner.onCellInfoChanged"*/);
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneStateScanner_onCellInfoChanged");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=PhoneStateScanner.onCellInfoChanged");

                    if (cellInfo == null)
                        getAllCellInfo();
                    else
                        getAllCellInfo(cellInfo);

                    //PPApplication.logE("[TEST BATTERY] PhoneStateScanner.onCellInfoChanged()", "xxx");
                    handleEvents(/*appContext*/);

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PhoneStateScanner.onCellInfoChanged");
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

    @Override
    public void onServiceStateChanged (ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);

        PPApplication.logE("[LISTENER CALL] PhoneStateScanner.onServiceStateChanged", "xxx");

        //PPApplication.logE("PhoneStateScanner.onServiceStateChanged", "telephonyManager=" + telephonyManager);
        //CallsCounter.logCounter(context, "PhoneStateScanner.onServiceStateChanged", "PhoneStateScanner_onServiceStateChanged");

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThread(/*"PhoneStateScanner.onServiceStateChanged"*/);
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneStateScanner_onServiceStateChanged");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=PhoneStateScanner.onServiceStateChanged");

                    getRegisteredCell();
                    /*if (PPApplication.logEnabled()) {
                        if (isValidCellId(registeredCell))
                            PPApplication.logE("PhoneStateScanner.onServiceStateChanged", "registeredCell=" + registeredCell);
                        else
                            PPApplication.logE("PhoneStateScanner.onServiceStateChanged", "registeredCell=NOT valid");
                    }*/

                    //PPApplication.logE("[TEST BATTERY] PhoneStateScanner.onServiceStateChanged()", "xxx");
                    handleEvents(/*appContext*/);

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PhoneStateScanner.onServiceStateChanged");
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

    /*
    private void getCellLocation(CellLocation location) {
        //PPApplication.logE("PhoneStateScanner.getCellLocation", "location="+location);

        if (!isValidCellId(registeredCell)) {
            if (location != null) {

                if (Permissions.checkLocation(context.getApplicationContext())) {

                    PPApplication.logE("PhoneStateScanner.getCellLocation", "---- start ----------------------------");

                    boolean isRegistered = false;

                    if (location instanceof GsmCellLocation) {
                        GsmCellLocation gcLoc = (GsmCellLocation) location;
                        //PPApplication.logE("PhoneStateScanner.getCellLocation", "gsm location="+gcLoc);
                        if (isValidCellId(gcLoc.getCid())) {
                            //PPApplication.logE("PhoneStateScanner.getCellLocation", "gsm mCid="+gcLoc.getCid());
                            registeredCell = gcLoc.getCid();
                            PPApplication.logE("PhoneStateScanner.getCellLocation", "gsm registeredCell=" + registeredCell);
                            lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                            DatabaseHandler db = DatabaseHandler.getInstance(context);
                            db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
                            doAutoRegistration(gcLoc.getCid());
                            isRegistered = true;
                        }
                    } else if (location instanceof CdmaCellLocation) {
                        CdmaCellLocation ccLoc = (CdmaCellLocation) location;
                        //PPApplication.logE("PhoneStateScanner.getCellLocation", "cdma location="+ccLoc);
                        if (isValidCellId(ccLoc.getBaseStationId())) {
                            //PPApplication.logE("PhoneStateScanner.getCellLocation", "cdma mCid="+ccLoc.getBaseStationId());
                            registeredCell = ccLoc.getBaseStationId();
                            PPApplication.logE("PhoneStateScanner.getCellLocation", "cdma registeredCell=" + registeredCell);
                            lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                            DatabaseHandler db = DatabaseHandler.getInstance(context);
                            db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
                            doAutoRegistration(ccLoc.getBaseStationId());
                            isRegistered = true;
                        }
                    }
                    //else {
                    //    PPApplication.logE("PhoneStateScanner.getCellLocation", "unknown location="+location);
                    //}

                    if (!isRegistered) {
                        PPApplication.logE("PhoneStateScanner.getCellLocation", "no cell location");
                        registeredCell = Integer.MAX_VALUE;
                        doAutoRegistration(registeredCell);
                    }

                    PPApplication.logE("PhoneStateScanner.getCellLocation", "---- end ----------------------------");

                }

            } else
                PPApplication.logE("PhoneStateScanner.getCellLocation", "location is null");
        }

        if (isValidCellId(registeredCell))
            PPApplication.logE("PhoneStateScanner.getCellLocation", "registeredCell=" + registeredCell);
        else
            PPApplication.logE("PhoneStateScanner.getCellLocation", "registeredCell=NOT valid");
    }


    @SuppressLint("MissingPermission")
    private void getCellLocation() {
        if (telephonyManager != null) {
            CellLocation location = null;
            if (Permissions.checkLocation(context.getApplicationContext()))
                location = telephonyManager.getCellLocation();
            //PPApplication.logE("PhoneStateScanner.getCellLocation.2", "location="+location);
            getCellLocation(location);
        }
    }
    */

    @Override
    public void onCellLocationChanged (final CellLocation location) {
        super.onCellLocationChanged(location);

        PPApplication.logE("[LISTENER CALL] PhoneStateScanner.onCellLocationChanged", "xxx");

        //PPApplication.logE("PhoneStateScanner.onCellLocationChanged", "telephonyManager="+telephonyManager);
        //CallsCounter.logCounter(context, "PhoneStateScanner.onCellLocationChanged", "PhoneStateScanner_onCellLocationChanged");

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThread(/*"PhoneStateScanner.onCellLocationChanged"*/);
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneStateScanner_onCellLocationChanged");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=PhoneStateScanner.onCellLocationChanged");

                    /*if (location == null)
                        getCellLocation();
                    else
                        getCellLocation(location);*/
                    getRegisteredCell();

                    /*if (PPApplication.logEnabled()) {
                        //PPApplication.logE("PhoneStateScanner.onCellLocationChanged", "location="+location);
                        if (isValidCellId(registeredCell))
                            PPApplication.logE("PhoneStateScanner.onCellLocationChanged", "registeredCell=" + registeredCell);
                        else
                            PPApplication.logE("PhoneStateScanner.onCellLocationChanged", "registeredCell=NOT valid");
                    }*/

                    //PPApplication.logE("[TEST BATTERY] PhoneStateScanner.onCellLocationChanged()", "xxx");
                    handleEvents(/*appContext*/);

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PhoneStateScanner.onCellLocationChanged");
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

    /*
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength)
    {
        super.onSignalStrengthsChanged(signalStrength);

        signal = signalStrength.getGsmSignalStrength()*2-113;
    }
    */

    void getRegisteredCell() {
        //PPApplication.logE("PhoneStateScanner.getRegisteredCell", "xxx");
        getAllCellInfo();
        //getCellLocation();
    }

    void rescanMobileCells() {
        //PPApplication.logE("PhoneStateScanner.rescanMobileCells", "xxx");
        //if (ApplicationPreferences.applicationEventMobileCellEnableScanning || PhoneStateScanner.forceStart) {
        if (ApplicationPreferences.applicationEventMobileCellEnableScanning ||
                MobileCellsPreferenceX.forceStart || MobileCellsRegistrationService.forceStart) {
            //PPApplication.logE("PhoneStateScanner.rescanMobileCells", "-----");

            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThread(/*"PhoneStateScanner.rescanMobileCells"*/);
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneStateScanner_rescanMobileCells");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=PhoneStateScanner.rescanMobileCells");

                        getRegisteredCell();
                        /*if (PPApplication.logEnabled()) {
                            if (isValidCellId(registeredCell))
                                PPApplication.logE("PhoneStateScanner.rescanMobileCells", "registeredCell=" + registeredCell);
                            else
                                PPApplication.logE("PhoneStateScanner.rescanMobileCells", "registeredCell=NOT valid");
                        }*/

                        //PPApplication.logE("[TEST BATTERY] PhoneStateScanner.rescanMobileCells()", "xxx");
                        handleEvents(/*appContext*/);

                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PhoneStateScanner.rescanMobileCells");
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

    static void handleEvents(/*final Context appContext*/) {
        //PPApplication.logE("PhoneStateScanner.handleEvents", "xxx");
        if (Event.getGlobalEventsRunning())
        {
            /*
            //if (DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false) > 0) {
                //PPApplication.logE("PhoneStateScanner.handleEvents", "start events handler");
                // start events handler
                PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=PhoneStateScanner.handleEvents");

                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PHONE_STATE);

                PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PhoneStateScanner.handleEvents");
            //}*/

            Data workData = new Data.Builder()
                    .putString(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_PHONE_STATE)
                    .build();

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(MainWorker.HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG)
                            .setInputData(workData)
                            .setInitialDelay(5, TimeUnit.SECONDS)
                            .keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                            .build();
            try {
                if (PPApplication.getApplicationStarted(true)) {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null) {

//                        //if (PPApplication.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                            PPApplication.logE("[TEST BATTERY] PhoneStateScanner.handleEvents", "for=" + MainWorker.HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                        } catch (Exception ignored) {
//                        }
//                        //}

                        //workManager.enqueue(worker);
                        workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG, ExistingWorkPolicy.APPEND_OR_REPLACE, worker);
                    }
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }

        /*
        // broadcast for cells editor
        Intent intent = new Intent(PPApplication.PACKAGE_NAME + ".PhoneStateChangedBroadcastReceiver_preference");
        //intent.putExtra("state", mode);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        */
    }

    private boolean isNotUsedCellsNotificationEnabled() {
        /*if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = manager.getNotificationChannel(PPApplication.NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL);
            return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
        }
        else {*/
            return ApplicationPreferences.applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled;
        //}
    }

    @SuppressWarnings("StringConcatenationInLoop")
    private void doAutoRegistration(final int _registeredCell) {
        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("PhoneStateScanner.doAutoRegistration", "enabledAutoRegistration=" + enabledAutoRegistration);
            PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellsNameForAutoRegistration=" + cellsNameForAutoRegistration);
            if (isValidCellId(_registeredCell))
                PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellIdToRegister=" + _registeredCell);
            else
                PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellIdToRegister=NOT valid");
        }*/

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        try {
            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneStateScanner_doAutoRegistration");
                wakeLock.acquire(10 * 60 * 1000);
            }

            DatabaseHandler db = DatabaseHandler.getInstance(context);

            boolean notUsedMobileCellsNotificationEnabled = isNotUsedCellsNotificationEnabled();
            //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "notUsedMobileCellsNotificationEnabled="+notUsedMobileCellsNotificationEnabled);

            lastRunningEventsNotOutside = "";
            lastPausedEventsOutside = "";
            List<Long> runningEventList = new ArrayList<>();
            List<Long> pausedEventList = new ArrayList<>();
            if (notUsedMobileCellsNotificationEnabled) {
                // get running events with enabled Mobile cells sensor
                db.loadMobileCellsSensorRunningPausedEvents(runningEventList, false);
                for (long runningEvent : runningEventList) {
                    if (!lastRunningEventsNotOutside.isEmpty())
                        lastRunningEventsNotOutside = lastRunningEventsNotOutside + "|";
                    lastRunningEventsNotOutside = lastRunningEventsNotOutside + runningEvent;
                }
                //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "lastRunningEventsNotOutside=" + lastRunningEventsNotOutside);

                db.loadMobileCellsSensorRunningPausedEvents(pausedEventList, true);
                for (long runningEvent : pausedEventList) {
                    if (!lastPausedEventsOutside.isEmpty())
                        lastPausedEventsOutside = lastPausedEventsOutside + "|";
                    lastPausedEventsOutside = lastPausedEventsOutside + runningEvent;
                }
                //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "lastPausedEventsOutside=" + lastPausedEventsOutside);
            }

            if (enabledAutoRegistration) {
                //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "by user enabled autoregistration");

                if (isValidCellId(_registeredCell)) {
                    //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellId is valid");

                    if (!db.isMobileCellSaved(_registeredCell)) {
                        //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellId is NOT saved, save it");

                        List<MobileCellsData> localCellsList = new ArrayList<>();
                        localCellsList.add(new MobileCellsData(_registeredCell, cellsNameForAutoRegistration, true, false,
                                Calendar.getInstance().getTimeInMillis(), lastRunningEventsNotOutside, lastPausedEventsOutside, false));
                        db.saveMobileCellsList(localCellsList, true, true);

                        synchronized (autoRegistrationEventList) {
                            for (Long event_id : autoRegistrationEventList) {
                                String currentCells = db.getEventMobileCellsCells(event_id);
                                if (!currentCells.isEmpty()) {
                                    //PPApplication.logE("NotUsedMobileCellsDetectedActivity.onClick", "save cellId to event="+event._name);
                                    String newCells = addCellId(currentCells, _registeredCell);
                                    db.updateMobileCellsCells(event_id, newCells);

                                    // broadcast new cell to
                                    Intent intent = new Intent(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_NEW_CELL);
                                    intent.putExtra(PPApplication.EXTRA_EVENT_ID, event_id);
                                    intent.putExtra(MobileCellsRegistrationService.EXTRA_NEW_CELL_VALUE, _registeredCell);
                                    intent.setPackage(PPApplication.PACKAGE_NAME);
                                    context.sendBroadcast(intent);

                                    Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
                                    refreshIntent.putExtra(PPApplication.EXTRA_EVENT_ID, event_id);
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
                                }
                            }
                        }
                    }
                }
                //else
                //    PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellId is NOT valid");
            }
            else
            if (notUsedMobileCellsNotificationEnabled) {
                //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "not used mobile cell detection");

                boolean showRunningNotification = false;
                boolean showPausedNotification = false;

                if (!lastRunningEventsNotOutside.isEmpty()) {
                    //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "any event with mobile cells sensor is running");

                    if (isValidCellId(_registeredCell)) {
                        //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellId is valid");

                        if (!db.isMobileCellSaved(_registeredCell)) {
                            //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellId is NOT saved, save it");

                            List<MobileCellsData> localCellsList = new ArrayList<>();
                            localCellsList.add(new MobileCellsData(_registeredCell, "", true, false,
                                    Calendar.getInstance().getTimeInMillis(), lastRunningEventsNotOutside, lastPausedEventsOutside, false));
                            db.saveMobileCellsList(localCellsList, true, false);
                            showRunningNotification = true;
                        }

                        if (!showRunningNotification) {
                            /*if (PPApplication.logEnabled()) {
                                PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellId is saved");
                                PPApplication.logE("PhoneStateScanner.doAutoRegistration", "runningEventList=" + runningEventList);
                            }*/

                            // it is not new cell
                            // test if registered cell is configured in running events

                            List<MobileCellsData> _cellsList = new ArrayList<>();
                            db.addMobileCellsToList(_cellsList, _registeredCell);
                            //if (!_cellsList.isEmpty())
                            //    PPApplication.logE("PhoneStateScanner.doAutoRegistration", "_cellsList.get(0).doNotDetect="+_cellsList.get(0).doNotDetect);

                            if ((!_cellsList.isEmpty()) && (!_cellsList.get(0).doNotDetect)) {
                                boolean found = false;
                                for (long eventId : runningEventList) {
                                    String configuredCells = db.getEventMobileCellsCells(eventId);
                                    if (!configuredCells.isEmpty()) {
                                        /*if (PPApplication.logEnabled()) {
                                            PPApplication.logE("PhoneStateScanner.doAutoRegistration", "configuredCells=" + configuredCells);
                                            PPApplication.logE("PhoneStateScanner.doAutoRegistration", "_registeredCell=" + _registeredCell);
                                        }*/
                                        if (configuredCells.contains("|" + _registeredCell + "|")) {
                                            // cell is between others
                                            found = true;
                                            break;
                                        }
                                        if (configuredCells.startsWith(_registeredCell + "|")) {
                                            // cell is at start of others
                                            found = true;
                                            break;
                                        }
                                        if (configuredCells.endsWith("|" + _registeredCell)) {
                                            // cell is at end of others
                                            found = true;
                                            break;
                                        }
                                        if (configuredCells.equals(String.valueOf(_registeredCell))) {
                                            // only this cell is configured
                                            found = true;
                                            break;
                                        }
                                    }
                                    else
                                        found = true;
                                }
                                showRunningNotification = !found;
                            }
                        }
                    }
                }

                if (!lastPausedEventsOutside.isEmpty()) {
                    //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "any event with mobile cells sensor is paused");

                    if (isValidCellId(_registeredCell)) {
                        //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellId is valid");

                        if (!db.isMobileCellSaved(_registeredCell)) {
                            //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellId is NOT saved, save it");

                            List<MobileCellsData> localCellsList = new ArrayList<>();
                            localCellsList.add(new MobileCellsData(_registeredCell, "", true, false,
                                    Calendar.getInstance().getTimeInMillis(), lastRunningEventsNotOutside, lastPausedEventsOutside, false));
                            db.saveMobileCellsList(localCellsList, true, false);
                            showPausedNotification = true;
                        }

                        if (!showPausedNotification) {
                            /*if (PPApplication.logEnabled()) {
                                PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellId is saved");
                                PPApplication.logE("PhoneStateScanner.doAutoRegistration", "pausedEventList=" + pausedEventList);
                            }*/

                            // it is not new cell
                            // test if registered cell is configured in running events

                            List<MobileCellsData> _cellsList = new ArrayList<>();
                            db.addMobileCellsToList(_cellsList, _registeredCell);
                            //if (!_cellsList.isEmpty())
                            //    PPApplication.logE("PhoneStateScanner.doAutoRegistration", "_cellsList.get(0).doNotDetect="+_cellsList.get(0).doNotDetect);

                            if ((!_cellsList.isEmpty()) && (!_cellsList.get(0).doNotDetect)) {
                                boolean found = false;
                                for (long eventId : pausedEventList) {
                                    String configuredCells = db.getEventMobileCellsCells(eventId);
                                    if (!configuredCells.isEmpty()) {
                                        /*if (PPApplication.logEnabled()) {
                                            PPApplication.logE("PhoneStateScanner.doAutoRegistration", "configuredCells=" + configuredCells);
                                            PPApplication.logE("PhoneStateScanner.doAutoRegistration", "_registeredCell=" + _registeredCell);
                                        }*/
                                        if (configuredCells.contains("|" + _registeredCell + "|")) {
                                            // cell is between others
                                            found = true;
                                            break;
                                        }
                                        if (configuredCells.startsWith(_registeredCell + "|")) {
                                            // cell is at start of others
                                            found = true;
                                            break;
                                        }
                                        if (configuredCells.endsWith("|" + _registeredCell)) {
                                            // cell is at end of others
                                            found = true;
                                            break;
                                        }
                                        if (configuredCells.equals(String.valueOf(_registeredCell))) {
                                            // only this cell is configured
                                            found = true;
                                            break;
                                        }
                                    }
                                    else
                                        found = true;
                                }
                                showPausedNotification = !found;
                            }
                        }
                    }
                }

                if (showRunningNotification || showPausedNotification) {

                    PPApplication.createMobileCellsNewCellNotificationChannel(context);

                    boolean isShown = false;
                    //if (Build.VERSION.SDK_INT >= 23) {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
                            for (StatusBarNotification notification : notifications) {
                                String tag = notification.getTag();
                                if ((tag != null) && tag.contains(PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_TAG+"_")) {
                                    if (notification.getId() == _registeredCell + PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_ID) {
                                        isShown = true;
                                        break;
                                    }
                                }
                            }
                        }
                    /*}
                    else {
                        Intent notificationIntent = new Intent(context, NotUsedMobileCellsDetectedActivity.class);
                        PendingIntent test = PendingIntent.getActivity(context, _registeredCell, notificationIntent, PendingIntent.FLAG_NO_CREATE);
                        isShown = test != null;
                    }*/
                    if (!isShown) {
                        NotificationCompat.Builder mBuilder;

                        Intent intent = new Intent(context, NotUsedMobileCellsDetectedActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        String nText = context.getString(R.string.notification_not_used_mobile_cell_text1);
                        nText = nText + " " + _registeredCell + ". ";
                        nText = nText + context.getString(R.string.notification_not_used_mobile_cell_text2);

                        mBuilder = new NotificationCompat.Builder(context, PPApplication.NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL)
                                .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                                .setSmallIcon(R.drawable.ic_exclamation_notify)
                                .setContentTitle(context.getString(R.string.notification_not_used_mobile_cell_title))
                                .setContentText(nText)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                                .setAutoCancel(true); // clear notification after click

                        Intent deleteIntent = new Intent(NEW_MOBILE_CELLS_NOTIFICATION_DELETED_ACTION);
                        deleteIntent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_CELL_ID, _registeredCell);
                        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, _registeredCell, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setDeleteIntent(deletePendingIntent);

                        // add action button to disable not used cells detection
                        Intent disableDetectionIntent = new Intent(NEW_MOBILE_CELLS_NOTIFICATION_DISABLE_ACTION);
                        disableDetectionIntent.putExtra("notificationId", _registeredCell + PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_ID);
                        PendingIntent pDisableDetectionIntent = PendingIntent.getBroadcast(context, 0, disableDetectionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(
                                R.drawable.ic_action_exit_app_white,
                                context.getString(R.string.notification_not_used_mobile_cell_disable),
                                pDisableDetectionIntent);
                        mBuilder.addAction(actionBuilder.build());

                        intent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_CELL_ID, _registeredCell);
                        intent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_LAST_CONNECTED_TIME, lastConnectedTime);
                        intent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_LAST_RUNNING_EVENTS, lastRunningEventsNotOutside);
                        intent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_LAST_PAUSED_EVENTS, lastPausedEventsOutside);

                        PendingIntent pi = PendingIntent.getActivity(context, _registeredCell, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(pi);
                        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
                        mBuilder.setWhen(0);
                        //mBuilder.setOnlyAlertOnce(true);
                        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
                        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                        NotificationManagerCompat _mNotificationManager = NotificationManagerCompat.from(context);
                        try {
                            //_mNotificationManager.cancel(_registeredCell + NEW_MOBILE_CELLS_NOTIFICATION_ID);
                            _mNotificationManager.notify(
                                    PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_TAG+"_" + registeredCell,
                                    PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_ID + _registeredCell, mBuilder.build());
                        } catch (Exception e) {
                            //Log.e("PhoneProfilesService.doAutoRegistration", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        }
                    }
                }
            }

        } finally {
            if ((wakeLock != null) && wakeLock.isHeld()) {
                try {
                    wakeLock.release();
                } catch (Exception ignored) {}
            }
        }

        //if (forceStart) {
        if (MobileCellsPreferenceX.forceStart || MobileCellsRegistrationService.forceStart) {
            if (isValidCellId(_registeredCell)) {
                //PPApplication.logE("PhoneStateScanner.doAutoRegistration", "send broadcast for force start");
                // broadcast for event preferences
                Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".MobileCellsPreference_refreshListView");
                LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
            }
        }

    }

    static void startAutoRegistration(Context context, boolean forConnect) {
        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (!forConnect) {
            enabledAutoRegistration = true;
            // save to shared preferences
            //PPApplication.logE("[REG] PhoneStateScanner.startAutoRegistration", "setMobileCellsAutoRegistration(true)");
            MobileCellsRegistrationService.setMobileCellsAutoRegistration(context, false);
        }
        else
            // read from shared preferences
            MobileCellsRegistrationService.getMobileCellsAutoRegistration(context);

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("PhoneStateScanner.startAutoRegistration", "enabledAutoRegistration=" + enabledAutoRegistration);
            PPApplication.logE("PhoneStateScanner.startAutoRegistration", "cellsNameForAutoRegistration=" + cellsNameForAutoRegistration);
        }*/

        if (enabledAutoRegistration) {
            try {
                // start registration service
                Intent serviceIntent = new Intent(context.getApplicationContext(), MobileCellsRegistrationService.class);
                PPApplication.startPPService(context, serviceIntent/*, false*/);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void stopAutoRegistration(Context context, boolean clearRegistration) {
        //PPApplication.logE("PhoneStateScanner.stopAutoRegistration", "xxx");

        // stop registration service
        context.stopService(new Intent(context.getApplicationContext(), MobileCellsRegistrationService.class));
        //MobileCellsRegistrationService.stop(context);

        if (clearRegistration) {
            //clearEventList();
            // set enabledAutoRegistration=false
            //PPApplication.logE("[REG] PhoneStateScanner.stopAutoRegistration", "setMobileCellsAutoRegistration(true)");
            MobileCellsRegistrationService.setMobileCellsAutoRegistration(context, true);
        }
    }

    static boolean isEventAdded(long event_id) {
        synchronized (autoRegistrationEventList) {
            return autoRegistrationEventList.indexOf(event_id) != -1;
        }
    }

    static void addEvent(long event_id) {
        synchronized (autoRegistrationEventList) {
            autoRegistrationEventList.add(event_id);
        }
    }

    static void removeEvent(long event_id) {
        synchronized (autoRegistrationEventList) {
            autoRegistrationEventList.remove(event_id);
        }
    }

    static void clearEventList() {
        synchronized (autoRegistrationEventList) {
            autoRegistrationEventList.clear();
        }
    }

    static int getEventCount() {
        synchronized (autoRegistrationEventList) {
            return autoRegistrationEventList.size();
        }
    }

    static void getAllEvents(SharedPreferences sharedPreferences,
                             @SuppressWarnings("SameParameterValue") String key) {
        synchronized (autoRegistrationEventList) {
            Gson gson = new Gson();
            String json =sharedPreferences.getString(key, null);
            Type type = new TypeToken<ArrayList<Long>>() {}.getType();
            autoRegistrationEventList.clear();
            ArrayList<Long> list = gson.fromJson(json, type);
            if (list != null)
                autoRegistrationEventList.addAll(list);
        }
    }

    static void saveAllEvents(SharedPreferences.Editor editor,
                              @SuppressWarnings("SameParameterValue") String key) {
        synchronized (autoRegistrationEventList) {
            Gson gson = new Gson();
            String json = gson.toJson(autoRegistrationEventList);
            editor.putString(key, json);
        }
    }

    static String addCellId(String cells, int cellId) {
        String[] splits = cells.split("\\|");
        String sCellId = Integer.toString(cellId);
        boolean found = false;
        for (String cell : splits) {
            if (!cell.isEmpty()) {
                if (cell.equals(sCellId)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            if (!cells.isEmpty())
                cells = cells + "|";
            cells = cells + sCellId;
        }
        return cells;
    }

    /*
    private static boolean getShowEnableLocationNotification(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getBoolean(PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_PHONE_STATE, true);
    }

    static void setShowEnableLocationNotification(Context context, boolean show)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putBoolean(PhoneStateScanner.PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_PHONE_STATE, show);
        editor.apply();
    }
    */

}
