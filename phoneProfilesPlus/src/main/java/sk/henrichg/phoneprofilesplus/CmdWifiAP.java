package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.IConnectivityManager;
import android.net.wifi.IWifiManager;
import android.net.wifi.WifiManager;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.util.Log;

@SuppressWarnings("WeakerAccess")
public class CmdWifiAP {

    public static void main(String[] args) {
        if (!(run(Boolean.parseBoolean(args[0])))) {
            System.exit(1);
        }
    }

    private static boolean run(boolean enable) {
        return setWifiAP(enable);
    }

    static boolean setWifiAP(boolean enable) {
        final String packageName = "sk.henrichg.phoneprofilesplus";
        try {
            IConnectivityManager connectivityAdapter = IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity"));
            if (enable) {
                IWifiManager wifiAdapter = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));
                int wifiState = wifiAdapter.getWifiEnabledState();
                boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                if (isWifiEnabled)
                    wifiAdapter.setWifiEnabled(packageName, false);

                ResultReceiver dummyResultReceiver = new ResultReceiver(null);

                connectivityAdapter.startTethering(0, dummyResultReceiver, false, packageName);
            }
            else {
                connectivityAdapter.stopTethering(0, packageName);
            }

            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    static boolean isEnabled() {
        try {
            boolean enabled;
            IWifiManager adapter = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));
            enabled = adapter.getWifiApEnabledState() == WifiManager.WIFI_AP_STATE_ENABLED;
            PPApplication.logE("CmdWifiAP.isEnabled", "enabled="+enabled);
            return enabled;
        } catch (Throwable e) {
            PPApplication.logE("CmdWifiAP.isEnabled", Log.getStackTraceString(e));
            return false;
        }
    }

}
