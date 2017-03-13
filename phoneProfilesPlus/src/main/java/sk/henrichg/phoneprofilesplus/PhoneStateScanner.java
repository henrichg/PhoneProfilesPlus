package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class PhoneStateScanner extends PhoneStateListener {

    private Context context;
    private TelephonyManager telephonyManager = null;
    //private TelephonyManager telephonyManager2 = null;

    int registeredCell = Integer.MAX_VALUE;
    long lastConnectedTime = 0;

    boolean enabledAutoRegistration = false;
    int durationForAutoRegistration = 0;
    String cellsNameForAutoRegistration = "";

    static MobileCellsRegistrationService autoRegistrationService = null;

    static String ACTION_PHONE_STATE_CHANGED = "sk.henrichg.phoneprofilesplus.ACTION_PHONE_STATE_CHANGED";

    PhoneStateScanner(Context context) {
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

    void connect() {
        if (PPApplication.isPowerSaveMode && ApplicationPreferences.applicationEventMobileCellsScanInPowerSaveMode(context).equals("2"))
            // start scanning in power save mode is not allowed
            return;

        if ((telephonyManager != null) &&
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY) &&
                Permissions.checkLocation(context.getApplicationContext()))
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
        startAutoRegistration();
    }

    void disconnect() {
        if ((telephonyManager != null) && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
        /*if ((telephonyManager2 != null) && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            telephonyManager2.listen(this, PhoneStateListener.LISTEN_NONE);*/
        stopAutoRegistration();
    }

    void resetListening(boolean oldPowerSaveMode, boolean forceReset) {
        if ((forceReset) || (PPApplication.isPowerSaveMode != oldPowerSaveMode)) {
            disconnect();
            connect();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getAllCellInfo(List<CellInfo> cellInfo) {
        // only for registered cells is returned identify
        // SlimKat in Galaxy Nexus - returns null :-/
        // Honor 7 - returns emty list (not null), Dual SIM?

        if (cellInfo!=null) {

            if (Permissions.checkLocation(context.getApplicationContext())) {

                //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "---- start ----------------------------");

                for (CellInfo _cellInfo : cellInfo) {
                    //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "registered="+_cellInfo.isRegistered());

                    if (_cellInfo instanceof CellInfoGsm) {
                        //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "gsm info="+_cellInfo);
                        CellIdentityGsm identityGsm = ((CellInfoGsm) _cellInfo).getCellIdentity();
                        if (identityGsm.getCid() != Integer.MAX_VALUE) {
                            //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "gsm mCid="+identityGsm.getCid());
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityGsm.getCid();
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                            }
                        }
                    } else if (_cellInfo instanceof CellInfoLte) {
                        //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "lte info="+_cellInfo);
                        CellIdentityLte identityLte = ((CellInfoLte) _cellInfo).getCellIdentity();
                        if (identityLte.getCi() != Integer.MAX_VALUE) {
                            //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "lte mCi="+identityLte.getCi());
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityLte.getCi();
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                            }
                        }
                    } else if (_cellInfo instanceof CellInfoWcdma) {
                        //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "wcdma info="+_cellInfo);
                        if (android.os.Build.VERSION.SDK_INT >= 18) {
                            CellIdentityWcdma identityWcdma = ((CellInfoWcdma) _cellInfo).getCellIdentity();
                            if (identityWcdma.getCid() != Integer.MAX_VALUE) {
                                //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "wcdma mCid=" + identityWcdma.getCid());
                                if (_cellInfo.isRegistered()) {
                                    registeredCell = identityWcdma.getCid();
                                    lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                                }
                            }
                        }
                        //else {
                        //    PPApplication.logE("PhoneStateScanner.getAllCellInfo", "wcdma mCid=not supported for API level < 18");
                        //}
                    } else if (_cellInfo instanceof CellInfoCdma) {
                        //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "cdma info="+_cellInfo);
                        CellIdentityCdma identityCdma = ((CellInfoCdma) _cellInfo).getCellIdentity();
                        if (identityCdma.getBasestationId() != Integer.MAX_VALUE) {
                            //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "wcdma mCid="+identityCdma.getBasestationId());
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityCdma.getBasestationId();
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                            }
                        }
                    }
                    //else {
                    //    PPApplication.logE("PhoneStateScanner.getAllCellInfo", "unknown info="+_cellInfo);
                    //}
                }

                //PPApplication.logE("PhoneStateScanner.getAllCellInfo", "---- end ----------------------------");

                PPApplication.logE("PhoneStateScanner.getAllCellInfo", "registeredCell=" + registeredCell);
            }

        }
        else
            PPApplication.logE("PhoneStateScanner.getAllCellInfo", "cell info is null");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getAllCellInfo() {
        if (telephonyManager != null) {
            List<CellInfo> cellInfo = telephonyManager.getAllCellInfo();
            getAllCellInfo(cellInfo);
        }
    }

    @Override
    public void onCellInfoChanged(List<CellInfo> cellInfo)
    {
        super.onCellInfoChanged(cellInfo);

        PPApplication.logE("PhoneStateScanner.onCellInfoChanged", "telephonyManager="+telephonyManager);

        if (cellInfo == null)
            getAllCellInfo();
        else
            getAllCellInfo(cellInfo);

        DatabaseHandler db = DatabaseHandler.getInstance(context);
        db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);

        doAutoRegistration();
        sendBroadcast();
    }

    @Override
    public void onServiceStateChanged (ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);

        PPApplication.logE("PhoneStateScanner.onServiceStateChanged", "telephonyManager="+telephonyManager);

        getRegisteredCell();

        DatabaseHandler db = DatabaseHandler.getInstance(context);
        db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);

        doAutoRegistration();
        sendBroadcast();
    }

    private void getCellLocation(CellLocation location) {

        if (location!=null) {

            if (Permissions.checkLocation(context.getApplicationContext())) {

                if (location instanceof GsmCellLocation) {
                    GsmCellLocation gcLoc = (GsmCellLocation) location;
                    //PPApplication.logE("PhoneStateScanner.getCellLocation", "gsm location="+gcLoc);
                    if (gcLoc.getCid() != Integer.MAX_VALUE) {
                        //PPApplication.logE("PhoneStateScanner.getCellLocation", "gsm mCid="+gcLoc.getCid());
                        registeredCell = gcLoc.getCid();
                        lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                    }
                } else if (location instanceof CdmaCellLocation) {
                    CdmaCellLocation ccLoc = (CdmaCellLocation) location;
                    //PPApplication.logE("PhoneStateScanner.getCellLocation", "cdma location="+ccLoc);
                    if (ccLoc.getBaseStationId() != Integer.MAX_VALUE) {
                        //PPApplication.logE("PhoneStateScanner.getCellLocation", "cdma mCid="+ccLoc.getBaseStationId());
                        registeredCell = ccLoc.getBaseStationId();
                        lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                    }
                }
                //else {
                //    PPApplication.logE("PhoneStateScanner.getCellLocation", "unknown location="+location);
                //}

                PPApplication.logE("PhoneStateScanner.getCellLocation", "registeredCell=" + registeredCell);

            }

        }
        else
            PPApplication.logE("PhoneStateScanner.getCellLocation", "location is null");
    }

    private void getCellLocation() {
        if (telephonyManager != null) {
            CellLocation location = telephonyManager.getCellLocation();
            getCellLocation(location);
        }
    }

    @Override
    public void onCellLocationChanged (CellLocation location) {
        super.onCellLocationChanged(location);

        PPApplication.logE("PhoneStateScanner.onCellLocationChanged", "telephonyManager="+telephonyManager);

        if (location == null)
            getCellLocation();
        else
            getCellLocation(location);

        DatabaseHandler db = DatabaseHandler.getInstance(context);
        db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);

        doAutoRegistration();
        sendBroadcast();
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
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            getAllCellInfo();
        getCellLocation();
    }

    void rescanMobileCells() {
        getRegisteredCell();
        doAutoRegistration();
        sendBroadcast();
    }

    private void sendBroadcast() {
        // broadcast for start EventsService
        Intent broadcastIntent = new Intent(context, PhoneStateChangeBroadcastReceiver.class);
        context.sendBroadcast(broadcastIntent);

        // broadcast for cells editor
        Intent intent = new Intent(ACTION_PHONE_STATE_CHANGED);
        //intent.putExtra("state", mode);
        context.sendBroadcast(intent);
    }

    private void doAutoRegistration() {
        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        if (enabledAutoRegistration) {
            //Log.d("PhoneStateScanner.doAutoRegistration", "xxx");
            List<MobileCellsData> localCellsList = new ArrayList<>();
            localCellsList.add(new MobileCellsData(registeredCell, cellsNameForAutoRegistration, true, false, lastConnectedTime));
            DatabaseHandler db = DatabaseHandler.getInstance(context);
            db.saveMobileCellsList(localCellsList, true, true);
        }
    }

    void startAutoRegistration() {
        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        MobileCellsRegistrationService.getMobileCellsAutoRegistration(context);
        if (enabledAutoRegistration) {
            //Log.d("PhoneStateScanner.startAutoRegistration","xxx");
            stopAutoRegistration();
            context.startService(new Intent(context.getApplicationContext(), MobileCellsRegistrationService.class));
        }
    }

    void stopAutoRegistration() {
        if (autoRegistrationService != null) {
            context.stopService(new Intent(context.getApplicationContext(), MobileCellsRegistrationService.class));
            autoRegistrationService = null;
        }
    }
}
