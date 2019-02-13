package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
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
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static android.content.Context.POWER_SERVICE;

class PhoneStateScanner extends PhoneStateListener {

    private final Context context;
    private final TelephonyManager telephonyManager;
    //private TelephonyManager telephonyManager2 = null;

    static int registeredCell = Integer.MAX_VALUE;
    static long lastConnectedTime = 0;

    static boolean forceStart = false;

    static boolean enabledAutoRegistration = false;
    static int durationForAutoRegistration = 0;
    static String cellsNameForAutoRegistration = "";
    static private final List<Long> eventList = Collections.synchronizedList(new ArrayList<Long>());

    //private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_PHONE_STATE = "show_enable_location_notification_phone_state";

    //static MobileCellsRegistrationService autoRegistrationService = null;

    //static String ACTION_PHONE_STATE_CHANGED = "sk.henrichg.phoneprofilesplus.ACTION_PHONE_STATE_CHANGED";

    PhoneStateScanner(Context context) {
        PPApplication.logE("PhoneStateScanner.constructor", "xxx");
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
        PPApplication.logE("PhoneStateScanner.connect", "xxx");
        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if (/*PPApplication.*/isPowerSaveMode && ApplicationPreferences.applicationEventMobileCellsScanInPowerSaveMode(context).equals("2"))
            // start scanning in power save mode is not allowed
            return;

        PPApplication.logE("PhoneStateScanner.connect", "telephonyManager="+telephonyManager);
        PPApplication.logE("PhoneStateScanner.connect", "FEATURE_TELEPHONY="+PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY));
        PPApplication.logE("PhoneStateScanner.connect", "checkLocation="+Permissions.checkLocation(context.getApplicationContext()));

        if ((telephonyManager != null) &&
                PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY) &&
                (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) &&
                Permissions.checkLocation(context.getApplicationContext())) {
            PPApplication.logE("PhoneStateScanner.connect", "telephonyManager.listen");
            telephonyManager.listen(this,
                    //  PhoneStateListener.LISTEN_CALL_STATE
                    PhoneStateListener.LISTEN_CELL_INFO // Requires API 17
                            | PhoneStateListener.LISTEN_CELL_LOCATION
                            //| PhoneStateListener.LISTEN_DATA_ACTIVITY
                            //| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                            | PhoneStateListener.LISTEN_SERVICE_STATE
                    //| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                    //| PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                    //| PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
            );
            /*if ((telephonyManager2 != null) &&
                    context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY) &&
                    Permissions.checkLocation(context.getApplicationContext()))
                telephonyManager2.listen(this,
                    //  PhoneStateListener.LISTEN_CALL_STATE
                        PhoneStateListener.LISTEN_CELL_INFO // Requires API 17
                      | PhoneStateListener.LISTEN_CELL_LOCATION
                    //| PhoneStateListener.LISTEN_DATA_ACTIVITY
                    //| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                      | PhoneStateListener.LISTEN_SERVICE_STATE
                    //| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                    //| PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                    //| PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                );*/
            //checkLocationEnabled();
        }
        startAutoRegistration(context, true);
    }

    void disconnect() {
        PPApplication.logE("PhoneStateScanner.disconnect", "xxx");
        if ((telephonyManager != null) && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY))
            telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
        /*if ((telephonyManager2 != null) && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            telephonyManager2.listen(this, PhoneStateListener.LISTEN_NONE);*/
        stopAutoRegistration(context);
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
                        //noinspection deprecation
                        mBuilder.setPriority(Notification.PRIORITY_MAX);
                        mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
                        mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            mNotificationManager.notify(PPApplication.LOCATION_SETTINGS_FOR_MOBILE_CELLS_SCANNING_NOTIFICATION_ID, mBuilder.build());
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

                PPApplication.logE("PhoneStateScanner.getAllCellInfo", "---- start ----------------------------");

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
                    else {
                        PPApplication.logE("PhoneStateScanner.getAllCellInfo", "unknown info="+_cellInfo);
                    }

                    if (isRegistered) {
                        PPApplication.logE("PhoneStateScanner.getAllCellInfo", "registeredCell=" + registeredCell);

                        PPApplication.logE("PhoneStateScanner.getAllCellInfo", "is registered, save it");
                        DatabaseHandler db = DatabaseHandler.getInstance(context);
                        db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
                        doAutoRegistration(registeredCell);
                    }
                }

                PPApplication.logE("PhoneStateScanner.getAllCellInfo", "---- end ----------------------------");
            }

        }
        else
            PPApplication.logE("PhoneStateScanner.getAllCellInfo", "cell info is null");
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

        PPApplication.logE("PhoneStateScanner.onCellInfoChanged", "telephonyManager="+telephonyManager);
        CallsCounter.logCounter(context, "PhoneStateScanner.onCellInfoChanged", "PhoneStateScanner_onCellInfoChanged");

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThread("PhoneStateScanner.onCellInfoChanged");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneStateScanner_onCellInfoChanged");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if (cellInfo == null)
                        getAllCellInfo();
                    else
                        getAllCellInfo(cellInfo);

                    handleEvents();
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

        PPApplication.logE("PhoneStateScanner.onServiceStateChanged", "telephonyManager=" + telephonyManager);
        CallsCounter.logCounter(context, "PhoneStateScanner.onServiceStateChanged", "PhoneStateScanner_onServiceStateChanged");

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThread("PhoneStateScanner.onServiceStateChanged");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneStateScanner_onServiceStateChanged");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    getRegisteredCell();
                    PPApplication.logE("PhoneStateScanner.onServiceStateChanged", "registeredCell=" + registeredCell);

                    handleEvents();
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

        if (location!=null) {

            if (Permissions.checkLocation(context.getApplicationContext())) {

                PPApplication.logE("PhoneStateScanner.getCellLocation", "---- start ----------------------------");

                if (location instanceof GsmCellLocation) {
                    GsmCellLocation gcLoc = (GsmCellLocation) location;
                    //PPApplication.logE("PhoneStateScanner.getCellLocation", "gsm location="+gcLoc);
                    if (isValidCellId(gcLoc.getCid())) {
                        //PPApplication.logE("PhoneStateScanner.getCellLocation", "gsm mCid="+gcLoc.getCid());
                        registeredCell = gcLoc.getCid();
                        PPApplication.logE("PhoneStateScanner.getCellLocation", "gsm registeredCell="+registeredCell);
                        lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                        DatabaseHandler db = DatabaseHandler.getInstance(context);
                        db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
                        doAutoRegistration(gcLoc.getCid());
                    }
                } else if (location instanceof CdmaCellLocation) {
                    CdmaCellLocation ccLoc = (CdmaCellLocation) location;
                    //PPApplication.logE("PhoneStateScanner.getCellLocation", "cdma location="+ccLoc);
                    if (isValidCellId(ccLoc.getBaseStationId())) {
                        //PPApplication.logE("PhoneStateScanner.getCellLocation", "cdma mCid="+ccLoc.getBaseStationId());
                        registeredCell = ccLoc.getBaseStationId();
                        PPApplication.logE("PhoneStateScanner.getCellLocation", "cdma registeredCell="+registeredCell);
                        lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                        DatabaseHandler db = DatabaseHandler.getInstance(context);
                        db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
                        doAutoRegistration(ccLoc.getBaseStationId());
                    }
                }
                //else {
                //    PPApplication.logE("PhoneStateScanner.getCellLocation", "unknown location="+location);
                //}

                PPApplication.logE("PhoneStateScanner.getCellLocation", "registeredCell=" + registeredCell);

                PPApplication.logE("PhoneStateScanner.getCellLocation", "---- end ----------------------------");

            }

        }
        else
            PPApplication.logE("PhoneStateScanner.getCellLocation", "location is null");
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

        PPApplication.logE("PhoneStateScanner.onCellLocationChanged", "telephonyManager="+telephonyManager);
        CallsCounter.logCounter(context, "PhoneStateScanner.onCellLocationChanged", "PhoneStateScanner_onCellLocationChanged");

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThread("PhoneStateScanner.onCellLocationChanged");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneStateScanner_onCellLocationChanged");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    /*if (location == null)
                        getCellLocation();
                    else
                        getCellLocation(location);*/
                    getRegisteredCell();

                    //PPApplication.logE("PhoneStateScanner.onCellLocationChanged", "location="+location);
                    //PPApplication.logE("PhoneStateScanner.onCellLocationChanged", "registeredCell="+registeredCell);

                    handleEvents();
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
        PPApplication.logE("PhoneStateScanner.getRegisteredCell", "xxx");
        getAllCellInfo();
        //getCellLocation();
    }

    void rescanMobileCells() {
        PPApplication.logE("PhoneStateScanner.rescanMobileCells", "xxx");
        if (ApplicationPreferences.applicationEventMobileCellEnableScanning(context.getApplicationContext()) || PhoneStateScanner.forceStart) {
            PPApplication.logE("PhoneStateScanner.rescanMobileCells", "-----");

            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThread("PhoneStateScanner.rescanMobileCells");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneStateScanner_rescanMobileCells");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        getRegisteredCell();
                        handleEvents();
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

    private void handleEvents() {
        PPApplication.logE("PhoneStateScanner.handleEvents", "xxx");
        //PhoneStateJob.start(context);
        if (Event.getGlobalEventsRunning(context))
        {
            if (DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false) > 0) {
                PPApplication.logE("PhoneStateScanner.handleEvents", "start events handler");
                // start events handler
                EventsHandler eventsHandler = new EventsHandler(context);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PHONE_STATE);
            }
        }

        /*
        // broadcast for cells editor
        Intent intent = new Intent("PhoneStateChangedBroadcastReceiver_preference");
        //intent.putExtra("state", mode);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        */
    }

    private void doAutoRegistration(final int cellIdToRegister) {
        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.logE("PhoneStateScanner.doAutoRegistration", "enabledAutoRegistration="+enabledAutoRegistration);
        PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellsNameForAutoRegistration="+cellsNameForAutoRegistration);
        PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellIdToRegister="+cellIdToRegister);
        if (enabledAutoRegistration) {
            // use handlerThread, because is used in handleEvents(). handleEvents() must be called after doAutoRegistration().
            PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneStateScanner_doAutoRegistration");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                //Log.d("PhoneStateScanner.doAutoRegistration", "xxx");
                if (isValidCellId(cellIdToRegister)) {
                    PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellId is valid");

                    DatabaseHandler db = DatabaseHandler.getInstance(context);
                    if (!db.isMobileCellSaved(cellIdToRegister)) {
                        PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellId is NOT saved, save it");

                        List<MobileCellsData> localCellsList = new ArrayList<>();
                        localCellsList.add(new MobileCellsData(cellIdToRegister, cellsNameForAutoRegistration, true, false, Calendar.getInstance().getTimeInMillis()));
                        db.saveMobileCellsList(localCellsList, true, true);

                        DataWrapper dataWrapper = new DataWrapper(context, false, 0, false);

                        synchronized (eventList) {
                            for (Long event_id : eventList) {
                                Event event = dataWrapper.getEventById(event_id);
                                if (event != null) {
                                    PPApplication.logE("PhoneStateScanner.doAutoRegistration", "save cellId to event="+event._name);
                                    String cells = event._eventPreferencesMobileCells._cells;
                                    cells = addCellId(cells, cellIdToRegister);
                                    event._eventPreferencesMobileCells._cells = cells;
                                    dataWrapper.updateEvent(event);
                                    db.updateMobileCellsCells(event);

                                    // broadcast for event preferences
                                    Intent intent = new Intent(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_NEW_CELLS);
                                    intent.putExtra(PPApplication.EXTRA_EVENT_ID, event_id);
                                    intent.putExtra(MobileCellsRegistrationService.EXTRA_NEW_CELLS_VALUE, cellIdToRegister);
                                    intent.setPackage(context.getPackageName());
                                    context.sendBroadcast(intent);

                                    Intent refreshIntent = new Intent("RefreshActivitiesBroadcastReceiver");
                                    refreshIntent.putExtra(PPApplication.EXTRA_EVENT_ID, event_id);
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
                                }
                            }
                        }

                        dataWrapper.invalidateDataWrapper();
                    }
                    else
                        PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellId is saved");
                }
                else
                    PPApplication.logE("PhoneStateScanner.doAutoRegistration", "cellId is NOT valid");
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    static void startAutoRegistration(Context context, boolean forConnect) {
        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        if (!forConnect) {
            enabledAutoRegistration = true;
            MobileCellsRegistrationService.setMobileCellsAutoRegistration(context, false);
        }
        else
            MobileCellsRegistrationService.getMobileCellsAutoRegistration(context);

        PPApplication.logE("PhoneStateScanner.startAutoRegistration", "enabledAutoRegistration="+enabledAutoRegistration);
        PPApplication.logE("PhoneStateScanner.startAutoRegistration", "cellsNameForAutoRegistration="+cellsNameForAutoRegistration);

        if (enabledAutoRegistration) {
            try {
                Intent serviceIntent = new Intent(context.getApplicationContext(), MobileCellsRegistrationService.class);
                PPApplication.startPPService(context, serviceIntent);
            } catch (Exception ignored) {
            }
        }
    }

    static void stopAutoRegistration(Context context) {
        PPApplication.logE("PhoneStateScanner.stopAutoRegistration", "xxx");

        context.stopService(new Intent(context.getApplicationContext(), MobileCellsRegistrationService.class));

        clearEventList();

        MobileCellsRegistrationService.setMobileCellsAutoRegistration(context, true);
    }

    static boolean isEventAdded(long event_id) {
        synchronized (eventList) {
            return eventList.indexOf(event_id) != -1;
        }
    }

    static void addEvent(long event_id) {
        synchronized (eventList) {
            eventList.add(event_id);
        }
    }

    static void removeEvent(long event_id) {
        synchronized (eventList) {
            eventList.remove(event_id);
        }
    }

    private static void clearEventList() {
        synchronized (eventList) {
            eventList.clear();
        }
    }

    static int getEventCount() {
        synchronized (eventList) {
            return eventList.size();
        }
    }

    private String addCellId(String cells, int cellId) {
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
