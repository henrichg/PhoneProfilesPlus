package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
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

import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class PhoneStateScanner extends PhoneStateListener {

    private Context context;
    TelephonyManager telephonyManager;

    public PhoneStateScanner(Context context) {
        this.context = context;
        telephonyManager = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void connect() {
        if ((telephonyManager != null) && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            telephonyManager.listen(this,
                /*PhoneStateListener.LISTEN_CALL_STATE
                |*/ PhoneStateListener.LISTEN_CELL_INFO // Requires API 17
                | PhoneStateListener.LISTEN_CELL_LOCATION
                //| PhoneStateListener.LISTEN_DATA_ACTIVITY
                //| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                  | PhoneStateListener.LISTEN_SERVICE_STATE
                //| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                //| PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                //| PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                );
    };

    public void disconnect() {
        if ((telephonyManager != null) && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
            telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
    }

    private void getAllCellInfo(List<CellInfo> cellInfo) {
        // only for registered cells is returned identify (Android 6, 7)
        // SlimKat in Galaxy Nexus - returns null :-/

        if (cellInfo!=null) {

            GlobalData.logE("PhoneStateScanner.getAllCellInfo", "---- start ----------------------------");

            for (CellInfo _cellInfo : cellInfo) {
                GlobalData.logE("PhoneStateScanner.getAllCellInfo", "registered="+_cellInfo.isRegistered());

                if (_cellInfo instanceof CellInfoGsm) {
                    GlobalData.logE("PhoneStateScanner.getAllCellInfo", "gsm info="+_cellInfo);
                    CellIdentityGsm identityGsm = ((CellInfoGsm) _cellInfo).getCellIdentity();
                    if (identityGsm.getCid() != Integer.MAX_VALUE) {
                        GlobalData.logE("PhoneStateScanner.getAllCellInfo", "gsm mCid="+identityGsm.getCid());
                    }
                }
                else
                if (_cellInfo instanceof CellInfoLte) {
                    GlobalData.logE("PhoneStateScanner.getAllCellInfo", "lte info="+_cellInfo);
                    CellIdentityLte identityLte = ((CellInfoLte) _cellInfo).getCellIdentity();
                    if (identityLte.getCi() != Integer.MAX_VALUE) {
                        GlobalData.logE("PhoneStateScanner.getAllCellInfo", "lte mCi="+identityLte.getCi());
                    }
                }
                else
                if (_cellInfo instanceof CellInfoWcdma) {
                    GlobalData.logE("PhoneStateScanner.getAllCellInfo", "wcdma info="+_cellInfo);
                    if (android.os.Build.VERSION.SDK_INT >= 18) {
                        CellIdentityWcdma identityWcdma = null;
                        identityWcdma = ((CellInfoWcdma) _cellInfo).getCellIdentity();
                        if (identityWcdma.getCid() != Integer.MAX_VALUE) {
                            GlobalData.logE("PhoneStateScanner.getAllCellInfo", "wcdma mCid=" + identityWcdma.getCid());
                        }
                    }
                    else {
                        GlobalData.logE("PhoneStateScanner.getAllCellInfo", "wcdma mCid=not supported for API level < 18");
                    }
                }
                else
                if (_cellInfo instanceof CellInfoCdma) {
                    GlobalData.logE("PhoneStateScanner.getAllCellInfo", "cdma info="+_cellInfo);
                    CellIdentityCdma identityCdma = ((CellInfoCdma) _cellInfo).getCellIdentity();
                    if (identityCdma.getBasestationId() != Integer.MAX_VALUE) {
                        GlobalData.logE("PhoneStateScanner.getAllCellInfo", "wcdma mCid="+identityCdma.getBasestationId());
                    }
                }
                else {
                    GlobalData.logE("PhoneStateScanner.getAllCellInfo", "unknown info="+_cellInfo);
                }
            }

            GlobalData.logE("PhoneStateScanner.getAllCellInfo", "---- end ----------------------------");

        }
        else
            GlobalData.logE("PhoneStateScanner.getAllCellInfo", "cell info is null");
    }

    public void getAllCellInfo() {
        if (telephonyManager != null) {
            List<CellInfo> cellInfo = telephonyManager.getAllCellInfo();
            getAllCellInfo(cellInfo);
        }
    }

    @Override
    public void onCellInfoChanged(List<CellInfo> cellInfo)
    {
        super.onCellInfoChanged(cellInfo);

        GlobalData.logE("PhoneStateScanner.onCellInfoChanged", "xxx");

        if (cellInfo == null)
            getAllCellInfo();
        else
            getAllCellInfo(cellInfo);
    }

    @Override
    public void onServiceStateChanged (ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);

        GlobalData.logE("PhoneStateScanner.onServiceStateChanged", "serviceState="+serviceState);

        getAllCellInfo();
    }

    @Override
    public void onCellLocationChanged (CellLocation location) {
        super.onCellLocationChanged(location);

        GlobalData.logE("PhoneStateScanner.onCellLocationChanged", "location="+location);

    }

    /*
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength)
    {
        super.onSignalStrengthsChanged(signalStrength);

        signal = signalStrength.getGsmSignalStrength()*2-113;
    }
    */

}
