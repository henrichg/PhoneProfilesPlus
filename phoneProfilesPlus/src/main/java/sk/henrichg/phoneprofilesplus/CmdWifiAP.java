package sk.henrichg.phoneprofilesplus;

import android.net.IConnectivityManager;
import android.net.wifi.IWifiManager;
import android.net.wifi.WifiManager;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.util.Log;

//import com.crashlytics.android.Crashlytics;

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
        //PPApplication.logE("CmdWifiAP.setWifiAP", "START enable="+enable);
        final String packageName = PPApplication.PACKAGE_NAME;
        try {
            IConnectivityManager connectivityAdapter = IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity"));  // service list | grep IConnectivityManager
            //PPApplication.logE("CmdWifiAP.setWifiAP", "connectivityAdapter="+connectivityAdapter);
            if (enable) {
                IWifiManager wifiAdapter = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));  // service list | grep IWifiManager
                //PPApplication.logE("CmdWifiAP.setWifiAP", "wifiAdapter="+wifiAdapter);
                int wifiState = wifiAdapter.getWifiEnabledState();
                boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                //PPApplication.logE("CmdWifiAP.setWifiAP", "isWifiEnabled="+isWifiEnabled);
                if (isWifiEnabled)
                    wifiAdapter.setWifiEnabled(packageName, false);

                ResultReceiver dummyResultReceiver = new ResultReceiver(null);
                connectivityAdapter.startTethering(0, dummyResultReceiver, false, packageName);
            } else {
                connectivityAdapter.stopTethering(0, packageName);
            }

            //PPApplication.logE("CmdWifiAP.setWifiAP", "END=");
            return true;
        } catch (java.lang.SecurityException ee) {
            Log.e("CmdWifiAP.setWifiAP", Log.getStackTraceString(ee));
            //PPApplication.logToCrashlytics("E/CmdWifiAP.setWifiAP: " + Log.getStackTraceString(ee));
            //Crashlytics.logException(ee);
            //PPApplication.logE("CmdWifiAP.setWifiAP", Log.getStackTraceString(e));
            return false;
        } catch (Throwable e) {
            Log.e("CmdWifiAP.setWifiAP", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            //Crashlytics.logException(e);
            //PPApplication.logE("CmdWifiAP.setWifiAP", Log.getStackTraceString(e));
            return false;
        }
    }

    static boolean isEnabled() {
        try {
            boolean enabled;
            IWifiManager adapter = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));  // service list | grep IWifiManager
            //PPApplication.logE("CmdWifiAP.isEnabled", "adapter="+adapter);
            enabled = adapter.getWifiApEnabledState() == WifiManager.WIFI_AP_STATE_ENABLED;
            //PPApplication.logE("CmdWifiAP.isEnabled", "enabled="+enabled);
            return enabled;
        } catch (Throwable e) {
            Log.e("CmdWifiAP.isEnabled", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            //Crashlytics.logException(e);
            return false;
        }
    }

}
