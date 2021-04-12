package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import android.telephony.TelephonyManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

class MobileCellsListener extends PhoneStateListener {

    private final Context context;
    private final TelephonyManager telephonyManager;
    //private final SubscriptionInfo subscriptionInfo;

    int registeredCell = Integer.MAX_VALUE;
    long lastConnectedTime = 0;

    final MobileCellsScanner scanner;

    MobileCellsListener(/*SubscriptionInfo subscriptionInfo, */Context context,
                        MobileCellsScanner scanner, TelephonyManager telephonyManager) {
        //PPApplication.logE("MobileCellsScanner.constructor", "xxx");
        //this.subscriptionInfo = subscriptionInfo;
        this.context = context;
        this.scanner = scanner;
        this.telephonyManager = telephonyManager;
    }

    private void getAllCellInfo(List<CellInfo> cellInfo) {
        // only for registered cells is returned identify
        // SlimKat in Galaxy Nexus - returns null :-/
        // Honor 7 - returns empty list (not null), Dual SIM?

        //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "cellInfo="+cellInfo);

        if (cellInfo!=null) {

            if (Permissions.checkLocation(context.getApplicationContext())) {

                //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "---- start ----------------------------");

                boolean anyRegistered = false;

                for (CellInfo _cellInfo : cellInfo) {
                    //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "registered="+_cellInfo.isRegistered());

                    boolean isRegistered = false;

                    if (_cellInfo instanceof CellInfoGsm) {
                        //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "gsm info="+_cellInfo);
                        CellIdentityGsm identityGsm = ((CellInfoGsm) _cellInfo).getCellIdentity();
                        //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "gsm cid="+identityGsm.getCid());
                        if (MobileCellsScanner.isValidCellId(identityGsm.getCid())) {
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityGsm.getCid();
                                //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "gsm registeredCell="+registeredCell);
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                                isRegistered = true;
                            }
                        }
                    } else if (_cellInfo instanceof CellInfoLte) {
                        //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "lte info="+_cellInfo);
                        CellIdentityLte identityLte = ((CellInfoLte) _cellInfo).getCellIdentity();
                        //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "lte cid="+identityLte.getCi());
                        if (MobileCellsScanner.isValidCellId(identityLte.getCi())) {
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityLte.getCi();
                                //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "lte registeredCell="+registeredCell);
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                                isRegistered = true;
                            }
                        }
                    } else if (_cellInfo instanceof CellInfoWcdma) {
                        //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "wcdma info="+_cellInfo);
                        CellIdentityWcdma identityWcdma = ((CellInfoWcdma) _cellInfo).getCellIdentity();
                        //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "wcdma cid=" + identityWcdma.getCid());
                        if (MobileCellsScanner.isValidCellId(identityWcdma.getCid())) {
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityWcdma.getCid();
                                //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "wcdma registeredCell="+registeredCell);
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                                isRegistered = true;
                            }
                        }
                    } else if (_cellInfo instanceof CellInfoCdma) {
                        //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "cdma info="+_cellInfo);
                        CellIdentityCdma identityCdma = ((CellInfoCdma) _cellInfo).getCellIdentity();
                        //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "wcdma cid="+identityCdma.getBasestationId());
                        if (MobileCellsScanner.isValidCellId(identityCdma.getBasestationId())) {
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityCdma.getBasestationId();
                                //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "cdma registeredCell="+registeredCell);
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                                isRegistered = true;
                            }
                        }
                    }
                    /*else {
                        PPApplication.logE("MobileCellsScanner.getAllCellInfo", "unknown info="+_cellInfo);
                    }*/

                    if (isRegistered) {
                        anyRegistered = true;
                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("MobileCellsScanner.getAllCellInfo", "registeredCell=" + registeredCell);
                            PPApplication.logE("MobileCellsScanner.getAllCellInfo", "is registered, save it");
                        }*/
                        DatabaseHandler db = DatabaseHandler.getInstance(context);
                        db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
                        doAutoRegistration(registeredCell);
                    }
                }

                if (!anyRegistered) {
                    //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "empty cellInfo");
                    registeredCell = Integer.MAX_VALUE;
                    doAutoRegistration(registeredCell);
                }

                //PPApplication.logE("MobileCellsScanner.getAllCellInfo", "---- end ----------------------------");
            }

        }
        //else
        //    PPApplication.logE("MobileCellsScanner.getAllCellInfo", "cell info is null");
    }

    @SuppressLint("MissingPermission")
    private void getAllCellInfo() {
        if (telephonyManager != null) {
            List<CellInfo> cellInfo = null;
            if (Permissions.checkLocation(context.getApplicationContext()))
                cellInfo = telephonyManager.getAllCellInfo();
            //PPApplication.logE("MobileCellsScanner.getAllCellInfo.2", "cellInfo="+cellInfo);
            getAllCellInfo(cellInfo);
        }
    }

    @Override
    public void onCellInfoChanged(final List<CellInfo> cellInfo)
    {
        super.onCellInfoChanged(cellInfo);

//        PPApplication.logE("[IN_LISTENER] MobileCellsScanner.onCellInfoChanged", "xxx");

        //PPApplication.logE("MobileCellsScanner.onCellInfoChanged", "telephonyManager="+telephonyManager);
        //CallsCounter.logCounter(context, "MobileCellsScanner.onCellInfoChanged", "MobileCellsScanner_onCellInfoChanged");

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadBroadcast(/*"MobileCellsScanner.onCellInfoChanged"*/);
        final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=MobileCellsScanner.onCellInfoChanged");

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileCellsScanner_onCellInfoChanged");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                if (cellInfo == null)
                    getAllCellInfo();
                else
                    getAllCellInfo(cellInfo);

                //PPApplication.logE("[TEST BATTERY] MobileCellsScanner.onCellInfoChanged()", "xxx");
                handleEvents(/*appContext*/);

                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=MobileCellsScanner.onCellInfoChanged");
            } catch (Exception e) {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    @Override
    public void onServiceStateChanged (ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);

//        PPApplication.logE("[IN_LISTENER] MobileCellsScanner.onServiceStateChanged", "xxx");

        //PPApplication.logE("MobileCellsScanner.onServiceStateChanged", "telephonyManager=" + telephonyManager);
        //CallsCounter.logCounter(context, "MobileCellsScanner.onServiceStateChanged", "MobileCellsScanner_onServiceStateChanged");

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadBroadcast(/*"MobileCellsScanner.onServiceStateChanged"*/);
        final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=MobileCellsScanner.onServiceStateChanged");

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileCellsScanner_onServiceStateChanged");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                registerCell();
                /*if (PPApplication.logEnabled()) {
                    if (isValidCellId(registeredCell))
                        PPApplication.logE("MobileCellsScanner.onServiceStateChanged", "registeredCell=" + registeredCell);
                    else
                        PPApplication.logE("MobileCellsScanner.onServiceStateChanged", "registeredCell=NOT valid");
                }*/

                //PPApplication.logE("[TEST BATTERY] MobileCellsScanner.onServiceStateChanged()", "xxx");
                handleEvents(/*appContext*/);

                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=MobileCellsScanner.onServiceStateChanged");
            } catch (Exception e) {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    /*
    private void getCellLocation(CellLocation location) {
        //PPApplication.logE("MobileCellsScanner.getCellLocation", "location="+location);

        if (!isValidCellId(registeredCell)) {
            if (location != null) {

                if (Permissions.checkLocation(context.getApplicationContext())) {

                    PPApplication.logE("MobileCellsScanner.getCellLocation", "---- start ----------------------------");

                    boolean isRegistered = false;

                    if (location instanceof GsmCellLocation) {
                        GsmCellLocation gcLoc = (GsmCellLocation) location;
                        //PPApplication.logE("MobileCellsScanner.getCellLocation", "gsm location="+gcLoc);
                        if (isValidCellId(gcLoc.getCid())) {
                            //PPApplication.logE("MobileCellsScanner.getCellLocation", "gsm mCid="+gcLoc.getCid());
                            registeredCell = gcLoc.getCid();
                            PPApplication.logE("MobileCellsScanner.getCellLocation", "gsm registeredCell=" + registeredCell);
                            lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                            DatabaseHandler db = DatabaseHandler.getInstance(context);
                            db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
                            doAutoRegistration(gcLoc.getCid());
                            isRegistered = true;
                        }
                    } else if (location instanceof CdmaCellLocation) {
                        CdmaCellLocation ccLoc = (CdmaCellLocation) location;
                        //PPApplication.logE("MobileCellsScanner.getCellLocation", "cdma location="+ccLoc);
                        if (isValidCellId(ccLoc.getBaseStationId())) {
                            //PPApplication.logE("MobileCellsScanner.getCellLocation", "cdma mCid="+ccLoc.getBaseStationId());
                            registeredCell = ccLoc.getBaseStationId();
                            PPApplication.logE("MobileCellsScanner.getCellLocation", "cdma registeredCell=" + registeredCell);
                            lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                            DatabaseHandler db = DatabaseHandler.getInstance(context);
                            db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
                            doAutoRegistration(ccLoc.getBaseStationId());
                            isRegistered = true;
                        }
                    }
                    //else {
                    //    PPApplication.logE("MobileCellsScanner.getCellLocation", "unknown location="+location);
                    //}

                    if (!isRegistered) {
                        PPApplication.logE("MobileCellsScanner.getCellLocation", "no cell location");
                        registeredCell = Integer.MAX_VALUE;
                        doAutoRegistration(registeredCell);
                    }

                    PPApplication.logE("MobileCellsScanner.getCellLocation", "---- end ----------------------------");

                }

            } else
                PPApplication.logE("MobileCellsScanner.getCellLocation", "location is null");
        }

        if (isValidCellId(registeredCell))
            PPApplication.logE("MobileCellsScanner.getCellLocation", "registeredCell=" + registeredCell);
        else
            PPApplication.logE("MobileCellsScanner.getCellLocation", "registeredCell=NOT valid");
    }


    @SuppressLint("MissingPermission")
    private void getCellLocation() {
        if (telephonyManager != null) {
            CellLocation location = null;
            if (Permissions.checkLocation(context.getApplicationContext()))
                location = telephonyManager.getCellLocation();
            //PPApplication.logE("MobileCellsScanner.getCellLocation.2", "location="+location);
            getCellLocation(location);
        }
    }
    */

    @Override
    public void onCellLocationChanged (final CellLocation location) {
        super.onCellLocationChanged(location);

//        PPApplication.logE("[IN_LISTENER] MobileCellsScanner.onCellLocationChanged", "xxx");

        //PPApplication.logE("MobileCellsScanner.onCellLocationChanged", "telephonyManager="+telephonyManager);
        //CallsCounter.logCounter(context, "MobileCellsScanner.onCellLocationChanged", "MobileCellsScanner_onCellLocationChanged");

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadBroadcast(/*"MobileCellsScanner.onCellLocationChanged"*/);
        final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=MobileCellsScanner.onCellLocationChanged");

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileCellsScanner_onCellLocationChanged");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                /*if (location == null)
                    getCellLocation();
                else
                    getCellLocation(location);*/
                registerCell();

                /*if (PPApplication.logEnabled()) {
                    //PPApplication.logE("MobileCellsScanner.onCellLocationChanged", "location="+location);
                    if (isValidCellId(registeredCell))
                        PPApplication.logE("MobileCellsScanner.onCellLocationChanged", "registeredCell=" + registeredCell);
                    else
                        PPApplication.logE("MobileCellsScanner.onCellLocationChanged", "registeredCell=NOT valid");
                }*/

                //PPApplication.logE("[TEST BATTERY] MobileCellsScanner.onCellLocationChanged()", "xxx");
                handleEvents(/*appContext*/);

                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=MobileCellsScanner.onCellLocationChanged");
            } catch (Exception e) {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    void registerCell() {
        //PPApplication.logE("MobileCellsScanner.getRegisteredCell", "xxx");
        getAllCellInfo();
        //getCellLocation();
    }

    void rescanMobileCells() {
        //PPApplication.logE("MobileCellsScanner.rescanMobileCells", "xxx");
        //if (ApplicationPreferences.applicationEventMobileCellEnableScanning || MobileCellsScanner.forceStart) {
        if (ApplicationPreferences.applicationEventMobileCellEnableScanning ||
                MobileCellsPreferenceX.forceStart || MobileCellsRegistrationService.forceStart) {
            //PPApplication.logE("MobileCellsScanner.rescanMobileCells", "-----");

            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThreadBroadcast(/*"MobileCellsScanner.rescanMobileCells"*/);
            final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=MobileCellsScanner.rescanMobileCells");

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileCellsScanner_rescanMobileCells");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    registerCell();
                    /*if (PPApplication.logEnabled()) {
                        if (isValidCellId(registeredCell))
                            PPApplication.logE("MobileCellsScanner.rescanMobileCells", "registeredCell=" + registeredCell);
                        else
                            PPApplication.logE("MobileCellsScanner.rescanMobileCells", "registeredCell=NOT valid");
                    }*/

                    //PPApplication.logE("[TEST BATTERY] MobileCellsScanner.rescanMobileCells()", "xxx");
                    handleEvents(/*appContext*/);

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=MobileCellsScanner.rescanMobileCells");
                } catch (Exception e) {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                }
            });
        }
    }

    void handleEvents(/*final Context appContext*/) {
        //PPApplication.logE("MobileCellsScanner.handleEvents", "xxx");
        if (Event.getGlobalEventsRunning())
        {
            /*
            //if (DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false) > 0) {
                //PPApplication.logE("MobileCellsScanner.handleEvents", "start events handler");
                // start events handler
                PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=MobileCellsScanner.handleEvents");

                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_MOBILE_CELLS);

                PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=MobileCellsScanner.handleEvents");
            //}*/

            Data workData = new Data.Builder()
                    .putString(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_MOBILE_CELLS)
                    .build();

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(MainWorker.HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG)
                            .setInputData(workData)
                            .setInitialDelay(5, TimeUnit.SECONDS)
                            //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
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
//                            PPApplication.logE("[TEST BATTERY] MobileCellsScanner.handleEvents", "for=" + MainWorker.HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                        PPApplication.logE("[WORKER_CALL] MobileCellsScanner.handleEvents", "xxx");
                        //workManager.enqueue(worker);
                        workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG, ExistingWorkPolicy./*APPEND_OR_*/REPLACE, worker);
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

    @SuppressWarnings("StringConcatenationInLoop")
    void doAutoRegistration(final int _registeredCell) {
        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        synchronized (PPApplication.mobileCellsScannerMutex) {
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("MobileCellsScanner.doAutoRegistration", "enabledAutoRegistration=" + enabledAutoRegistration);
                PPApplication.logE("MobileCellsScanner.doAutoRegistration", "cellsNameForAutoRegistration=" + cellsNameForAutoRegistration);
                if (isValidCellId(_registeredCell))
                    PPApplication.logE("MobileCellsScanner.doAutoRegistration", "cellIdToRegister=" + _registeredCell);
                else
                    PPApplication.logE("MobileCellsScanner.doAutoRegistration", "cellIdToRegister=NOT valid");
            }*/

            /*PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileCellsScanner_doAutoRegistration");
                    wakeLock.acquire(10 * 60 * 1000);
            }*/

            DatabaseHandler db = DatabaseHandler.getInstance(context);

            boolean notUsedMobileCellsNotificationEnabled = scanner.isNotUsedCellsNotificationEnabled();
            //PPApplication.logE("%%%%% MobileCellsScanner.doAutoRegistration", "notUsedMobileCellsNotificationEnabled="+notUsedMobileCellsNotificationEnabled);

            MobileCellsScanner.lastRunningEventsNotOutside = "";
            MobileCellsScanner.lastPausedEventsOutside = "";
            //List<NotUsedMobileCells> runningEventList = new ArrayList<>();
            //List<NotUsedMobileCells> pausedEventList = new ArrayList<>();
            List<NotUsedMobileCells> mobileCellsEventList = new ArrayList<>();
            if (notUsedMobileCellsNotificationEnabled) {
                // get running events with enabled Mobile cells sensor

                db.loadMobileCellsSensorRunningPausedEvents(mobileCellsEventList);
                for (NotUsedMobileCells _event : mobileCellsEventList) {
                    if (_event.whenOutside) {
                        if (!MobileCellsScanner.lastPausedEventsOutside.isEmpty())
                            MobileCellsScanner.lastPausedEventsOutside = MobileCellsScanner.lastPausedEventsOutside + "|";
                        MobileCellsScanner.lastPausedEventsOutside = MobileCellsScanner.lastPausedEventsOutside + _event.eventId;
                    } else {
                        if (!MobileCellsScanner.lastRunningEventsNotOutside.isEmpty())
                            MobileCellsScanner.lastRunningEventsNotOutside = MobileCellsScanner.lastRunningEventsNotOutside + "|";
                        MobileCellsScanner.lastRunningEventsNotOutside = MobileCellsScanner.lastRunningEventsNotOutside + _event.eventId;
                    }
                }
                //PPApplication.logE("%%%%% MobileCellsScanner.doAutoRegistration", "lastRunningEventsNotOutside=" + lastRunningEventsNotOutside);
                //PPApplication.logE("%%%%% MobileCellsScanner.doAutoRegistration", "lastPausedEventsOutside=" + lastPausedEventsOutside);

                /*
                db.loadMobileCellsSensorRunningPausedEvents(runningEventList, false);
                for (NotUsedMobileCells runningEvent : runningEventList) {
                    if (!lastRunningEventsNotOutside.isEmpty())
                        lastRunningEventsNotOutside = lastRunningEventsNotOutside + "|";
                    lastRunningEventsNotOutside = lastRunningEventsNotOutside + runningEvent.eventId;
                }
                //PPApplication.logE("MobileCellsScanner.doAutoRegistration", "lastRunningEventsNotOutside=" + lastRunningEventsNotOutside);

                db.loadMobileCellsSensorRunningPausedEvents(pausedEventList, true);
                for (NotUsedMobileCells pausedEvent : pausedEventList) {
                    if (!lastPausedEventsOutside.isEmpty())
                        lastPausedEventsOutside = lastPausedEventsOutside + "|";
                    lastPausedEventsOutside = lastPausedEventsOutside + pausedEvent.eventId;
                }
                */
                //PPApplication.logE("MobileCellsScanner.doAutoRegistration", "lastPausedEventsOutside=" + lastPausedEventsOutside);
            }

            if (MobileCellsScanner.enabledAutoRegistration) {
                //PPApplication.logE("MobileCellsScanner.doAutoRegistration", "by user enabled autoregistration");

                if (MobileCellsScanner.isValidCellId(_registeredCell)) {
                    //PPApplication.logE("MobileCellsScanner.doAutoRegistration", "cellId is valid");

                    if (!db.isMobileCellSaved(_registeredCell)) {
                        //PPApplication.logE("MobileCellsScanner.doAutoRegistration", "cellId is NOT saved, save it");

                        List<MobileCellsData> localCellsList = new ArrayList<>();
                        localCellsList.add(new MobileCellsData(_registeredCell, MobileCellsScanner.cellsNameForAutoRegistration, true, false,
                                Calendar.getInstance().getTimeInMillis(), MobileCellsScanner.lastRunningEventsNotOutside, MobileCellsScanner.lastPausedEventsOutside, false));
                        db.saveMobileCellsList(localCellsList, true, true);

                        synchronized (MobileCellsScanner.autoRegistrationEventList) {
                            for (Long event_id : MobileCellsScanner.autoRegistrationEventList) {
                                String currentCells = db.getEventMobileCellsCells(event_id);
                                if (!currentCells.isEmpty()) {
                                    //PPApplication.logE("NotUsedMobileCellsDetectedActivity.onClick", "save cellId to event="+event._name);
                                    String newCells = MobileCellsScanner.addCellId(currentCells, _registeredCell);
                                    db.updateMobileCellsCells(event_id, newCells);

                                    // broadcast new cell to
                                    Intent intent = new Intent(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_NEW_CELL);
                                    intent.putExtra(PPApplication.EXTRA_EVENT_ID, event_id);
                                    intent.putExtra(MobileCellsRegistrationService.EXTRA_NEW_CELL_VALUE, _registeredCell);
                                    intent.setPackage(PPApplication.PACKAGE_NAME);
                                    context.sendBroadcast(intent);

//                                    PPApplication.logE("[LOCAL_BROADCAST_CALL] PhoneProfilesService.doAutoRegistration", "(1)");
                                    Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
                                    refreshIntent.putExtra(PPApplication.EXTRA_EVENT_ID, event_id);
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
                                }
                            }
                        }
                    }
                }
                //else
                //    PPApplication.logE("MobileCellsScanner.doAutoRegistration", "cellId is NOT valid");
            } else if (notUsedMobileCellsNotificationEnabled) {
                //PPApplication.logE("MobileCellsScanner.doAutoRegistration", "not used mobile cell detection");

                //boolean showRunningNotification = false;
                //boolean showPausedNotification = false;
                boolean showNotification = false;

                if ((!MobileCellsScanner.lastRunningEventsNotOutside.isEmpty()) || (!MobileCellsScanner.lastPausedEventsOutside.isEmpty())) {
                    if (MobileCellsScanner.isValidCellId(_registeredCell)) {

                        if (!db.isMobileCellSaved(_registeredCell)) {
                            //PPApplication.logE("%%%%% MobileCellsScanner.doAutoRegistration", "cellId is NOT saved, save it");

                            // add new cell
                            List<MobileCellsData> localCellsList = new ArrayList<>();
                            localCellsList.add(new MobileCellsData(_registeredCell, "", true, false,
                                    Calendar.getInstance().getTimeInMillis(), MobileCellsScanner.lastRunningEventsNotOutside, MobileCellsScanner.lastPausedEventsOutside, false));
                            db.saveMobileCellsList(localCellsList, true, false);
                            showNotification = true;
                        }

                        if (!showNotification) {
                            /*if (PPApplication.logEnabled()) {
                                PPApplication.logE("%%%%% MobileCellsScanner.doAutoRegistration", "cellId is saved");
                                PPApplication.logE("%%%%% MobileCellsScanner.doAutoRegistration", "mobileCellsEventList=" + mobileCellsEventList);
                            }*/

                            // it is not new cell
                            // test if registered cell is configured in running events

                            List<MobileCellsData> _cellsList = new ArrayList<>();
                            db.addMobileCellsToList(_cellsList, _registeredCell);
                            //if (!_cellsList.isEmpty())
                            //    PPApplication.logE("%%%%% MobileCellsScanner.doAutoRegistration", "_cellsList.get(0).doNotDetect="+_cellsList.get(0).doNotDetect);

                            if ((!_cellsList.isEmpty()) && (!_cellsList.get(0).doNotDetect)) {
                                boolean found = false;
                                for (NotUsedMobileCells notUsedMobileCells : mobileCellsEventList) {
                                    //String configuredCells = db.getEventMobileCellsCells(eventId);
                                    String configuredCells = notUsedMobileCells.cells;
                                    if (!configuredCells.isEmpty()) {
                                        /*if (PPApplication.logEnabled()) {
                                            PPApplication.logE("%%%%% MobileCellsScanner.doAutoRegistration", "configuredCells=" + configuredCells);
                                            PPApplication.logE("%%%%% MobileCellsScanner.doAutoRegistration", "_registeredCell=" + _registeredCell);
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
                                    } else
                                        found = true;
                                }
                                // found == false = cell is not in running events
                                showNotification = !found;
                            }
                        }

                    }
                }

                /*
                if (!lastRunningEventsNotOutside.isEmpty()) {
                    //PPApplication.logE("MobileCellsScanner.doAutoRegistration", "any event with mobile cells sensor is running");

                    if (isValidCellId(_registeredCell)) {
                        //PPApplication.logE("MobileCellsScanner.doAutoRegistration", "cellId is valid");

                        if (!db.isMobileCellSaved(_registeredCell)) {
                            //PPApplication.logE("MobileCellsScanner.doAutoRegistration", "cellId is NOT saved, save it");

                            // add new cell
                            List<MobileCellsData> localCellsList = new ArrayList<>();
                            localCellsList.add(new MobileCellsData(_registeredCell, "", true, false,
                                    Calendar.getInstance().getTimeInMillis(), lastRunningEventsNotOutside, lastPausedEventsOutside, false));
                            db.saveMobileCellsList(localCellsList, true, false);
                            showRunningNotification = true;
                        }

                        if (!showRunningNotification) {
                            //if (PPApplication.logEnabled()) {
                            //    PPApplication.logE("MobileCellsScanner.doAutoRegistration", "cellId is saved");
                            //    PPApplication.logE("MobileCellsScanner.doAutoRegistration", "runningEventList=" + runningEventList);
                            //}

                            // it is not new cell
                            // test if registered cell is configured in running events

                            List<MobileCellsData> _cellsList = new ArrayList<>();
                            db.addMobileCellsToList(_cellsList, _registeredCell);
                            //if (!_cellsList.isEmpty())
                            //    PPApplication.logE("MobileCellsScanner.doAutoRegistration", "_cellsList.get(0).doNotDetect="+_cellsList.get(0).doNotDetect);

                            if ((!_cellsList.isEmpty()) && (!_cellsList.get(0).doNotDetect)) {
                                boolean found = false;
                                for (NotUsedMobileCells notUsedMobileCells : runningEventList) {
                                    //String configuredCells = db.getEventMobileCellsCells(eventId);
                                    String configuredCells = notUsedMobileCells.cells;
                                    if (!configuredCells.isEmpty()) {
                                        //if (PPApplication.logEnabled()) {
                                        //    PPApplication.logE("MobileCellsScanner.doAutoRegistration", "configuredCells=" + configuredCells);
                                        //    PPApplication.logE("MobileCellsScanner.doAutoRegistration", "_registeredCell=" + _registeredCell);
                                        //}
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
                                // found == false = cell is not in running events
                                showRunningNotification = !found;
                            }
                        }
                    }
                }

                if (!lastPausedEventsOutside.isEmpty()) {
                    //PPApplication.logE("MobileCellsScanner.doAutoRegistration", "any event with mobile cells sensor is paused");

                    if (isValidCellId(_registeredCell)) {
                        //PPApplication.logE("MobileCellsScanner.doAutoRegistration", "cellId is valid");

                        if (!db.isMobileCellSaved(_registeredCell)) {
                            //PPApplication.logE("MobileCellsScanner.doAutoRegistration", "cellId is NOT saved, save it");

                            // add new cell
                            List<MobileCellsData> localCellsList = new ArrayList<>();
                            localCellsList.add(new MobileCellsData(_registeredCell, "", true, false,
                                    Calendar.getInstance().getTimeInMillis(), lastRunningEventsNotOutside, lastPausedEventsOutside, false));
                            db.saveMobileCellsList(localCellsList, true, false);
                            showPausedNotification = true;
                        }

                        if (!showPausedNotification) {
                            //if (PPApplication.logEnabled()) {
                            //    PPApplication.logE("MobileCellsScanner.doAutoRegistration", "cellId is saved");
                            //    PPApplication.logE("MobileCellsScanner.doAutoRegistration", "pausedEventList=" + pausedEventList);
                            //}

                            // it is not new cell
                            // test if registered cell is configured in running events

                            List<MobileCellsData> _cellsList = new ArrayList<>();
                            db.addMobileCellsToList(_cellsList, _registeredCell);
                            //if (!_cellsList.isEmpty())
                            //    PPApplication.logE("MobileCellsScanner.doAutoRegistration", "_cellsList.get(0).doNotDetect="+_cellsList.get(0).doNotDetect);

                            if ((!_cellsList.isEmpty()) && (!_cellsList.get(0).doNotDetect)) {
                                boolean found = false;
                                for (NotUsedMobileCells notUsedMobileCells : pausedEventList) {
                                    //String configuredCells = db.getEventMobileCellsCells(eventId);
                                    String configuredCells = notUsedMobileCells.cells;
                                    if (!configuredCells.isEmpty()) {
                                        //if (PPApplication.logEnabled()) {
                                        //    PPApplication.logE("MobileCellsScanner.doAutoRegistration", "configuredCells=" + configuredCells);
                                        //    PPApplication.logE("MobileCellsScanner.doAutoRegistration", "_registeredCell=" + _registeredCell);
                                        //}
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
                                // found == false = cell is not in paused events
                                showPausedNotification = !found;
                            }
                        }
                    }
                }
                */

                //PPApplication.logE("%%%%% MobileCellsScanner.doAutoRegistration", "showNotification="+showNotification);

                //if (showRunningNotification || showPausedNotification) {
                if (showNotification) {

                    // show notification about new cell non-configured in events

                    PPApplication.createMobileCellsNewCellNotificationChannel(context);

                    boolean isShown = false;
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (mNotificationManager != null) {
                        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
                        for (StatusBarNotification notification : notifications) {
                            String tag = notification.getTag();
                            if ((tag != null) && tag.contains(PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_TAG + "_")) {
                                if (notification.getId() == _registeredCell + PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_ID) {
                                    isShown = true;
                                    break;
                                }
                            }
                        }
                    }

                    //PPApplication.logE("%%%%% MobileCellsScanner.doAutoRegistration", "isShown="+isShown);

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

                        Intent deleteIntent = new Intent(MobileCellsScanner.NEW_MOBILE_CELLS_NOTIFICATION_DELETED_ACTION);
                        deleteIntent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_CELL_ID, _registeredCell);
                        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, _registeredCell, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setDeleteIntent(deletePendingIntent);

                        // add action button to disable not used cells detection
                        Intent disableDetectionIntent = new Intent(MobileCellsScanner.NEW_MOBILE_CELLS_NOTIFICATION_DISABLE_ACTION);
                        disableDetectionIntent.putExtra("notificationId", _registeredCell + PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_ID);
                        PendingIntent pDisableDetectionIntent = PendingIntent.getBroadcast(context, 0, disableDetectionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(
                                R.drawable.ic_action_exit_app_white,
                                context.getString(R.string.notification_not_used_mobile_cell_disable),
                                pDisableDetectionIntent);
                        mBuilder.addAction(actionBuilder.build());

                        intent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_CELL_ID, _registeredCell);
                        intent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_LAST_CONNECTED_TIME, lastConnectedTime);
                        intent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_LAST_RUNNING_EVENTS, MobileCellsScanner.lastRunningEventsNotOutside);
                        intent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_LAST_PAUSED_EVENTS, MobileCellsScanner.lastPausedEventsOutside);

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
                                    PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_TAG + "_" + registeredCell,
                                    PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_ID + _registeredCell, mBuilder.build());
                        } catch (Exception e) {
                            //Log.e("PhoneProfilesService.doAutoRegistration", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        }
                    }
                }
            }

            /*} finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {}
                }
            }*/

            //if (forceStart) {
            if (MobileCellsPreferenceX.forceStart || MobileCellsRegistrationService.forceStart) {
                if (MobileCellsScanner.isValidCellId(_registeredCell)) {
//                PPApplication.logE("[LOCAL_BROADCAST_CALL] PhoneProfilesService.doAutoRegistration", "(2)");
                    // broadcast for event preferences
                    Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".MobileCellsPreference_refreshListView");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
                }
            }

        }

    }

}
