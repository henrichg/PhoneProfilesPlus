package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
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
        if (telephonyManager != null)
            telephonyManager.listen(this,
                /*PhoneStateListener.LISTEN_CALL_STATE
                |*/ PhoneStateListener.LISTEN_CELL_INFO // Requires API 17
                //| PhoneStateListener.LISTEN_CELL_LOCATION
                //| PhoneStateListener.LISTEN_DATA_ACTIVITY
                //| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                //| PhoneStateListener.LISTEN_SERVICE_STATE
                //| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                //| PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                //| PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                );
    };

    public void disconnect() {
        if (telephonyManager != null)
            telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public void onCellInfoChanged(List<CellInfo> cellInfo)
    {
        super.onCellInfoChanged(cellInfo);

        if (telephonyManager != null)
            cellInfo = telephonyManager.getAllCellInfo();

        if (cellInfo!=null) { }

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
