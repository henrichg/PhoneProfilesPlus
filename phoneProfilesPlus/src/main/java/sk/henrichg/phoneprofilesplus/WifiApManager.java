package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

final class WifiApManager {
    //private static final int WIFI_AP_STATE_FAILED = 4;
    private final WifiManager mWifiManager;
    private final String TAG = "Wifi Access Manager";
    private Method wifiControlMethod = null;
    private Method wifiApConfigurationMethod = null;
    //private Method wifiApState;
    private Method wifiApEnabled = null;

    @SuppressLint("PrivateApi")
    WifiApManager(Context context) throws SecurityException, NoSuchMethodException {
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            wifiControlMethod = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            wifiApConfigurationMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration"/*,null*/);
            //wifiApState = mWifiManager.getClass().getMethod("getWifiApState");
            wifiApEnabled = mWifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
        }
        PPApplication.logE("$$$ WifiAP", "WifiApManager.WifiApManager-mWifiManager="+mWifiManager);
        PPApplication.logE("$$$ WifiAP", "WifiApManager.WifiApManager-wifiControlMethod="+wifiControlMethod);
        PPApplication.logE("$$$ WifiAP", "WifiApManager.WifiApManager-wifiApConfigurationMethod="+wifiApConfigurationMethod);
        PPApplication.logE("$$$ WifiAP", "WifiApManager.WifiApManager-wifiApEnabled="+wifiApEnabled);
    }

    private void setWifiApState(WifiConfiguration config, boolean enabled) {
        try {
            PPApplication.logE("$$$ WifiAP", "WifiApManager.setWifiApState-config="+config);
            PPApplication.logE("$$$ WifiAP", "WifiApManager.setWifiApState-enabled="+enabled);
            PPApplication.logE("$$$ WifiAP", "WifiApManager.setWifiApState-mWifiManager="+mWifiManager);
            PPApplication.logE("$$$ WifiAP", "WifiApManager.setWifiApState-wifiControlMethod="+wifiControlMethod);
            if (enabled) {
                if (mWifiManager != null) {
                    int wifiState = mWifiManager.getWifiState();
                    boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                    if (isWifiEnabled)
                        mWifiManager.setWifiEnabled(false);
                }
            }
            wifiControlMethod.setAccessible(true);
            wifiControlMethod.invoke(mWifiManager, config, enabled);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            PPApplication.logE("$$$ WifiAP", "WifiApManager.setWifiApState-exception="+e);
        }
    }

    void setWifiApState(boolean enabled) {
        WifiConfiguration wifiConfiguration = getWifiApConfiguration();
        /*return*/ setWifiApState(wifiConfiguration, enabled);
    }

    // not working in Android 8+ :-/
    // https://stackoverflow.com/questions/46392277/changing-android-hotspot-settings
    private WifiConfiguration getWifiApConfiguration()
    {
        try{
            wifiApConfigurationMethod.setAccessible(true);
            return (WifiConfiguration)wifiApConfigurationMethod.invoke(mWifiManager/*, null*/);
        }
        catch(Exception e)
        {
            PPApplication.logE("$$$ WifiAP", "WifiApManager.getWifiApConfiguration-exception="+e);
            return null;
        }
    }

    /*
    public int getWifiApState() {
        try {
            wifiApState.setAccessible(true);
            return (Integer)wifiApState.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return WIFI_AP_STATE_FAILED;
        }
    }
    */

    boolean isWifiAPEnabled() {
        try {
            wifiApEnabled.setAccessible(true);
            return (Boolean) wifiApEnabled.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            PPApplication.logE("$$$ WifiAP", "WifiApManager.isWifiAPEnabled-exception="+e);
            return false;
        }

    }

    static boolean isWifiAPEnabled(Context context) {
        try {
            WifiApManager wifiApManager = new WifiApManager(context);
                    /*
                    int wifiApState = wifiApManager.getWifiApState();
                    // 11 => AP OFF
                    // 13 => AP ON
                    Log.e("&&&& WifiBluetoothScanner", "wifiApState=" + wifiApState);
                    canScan = wifiApState == 11;*/
            return wifiApManager.isWifiAPEnabled();
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
            return false;
        }
    }

    static boolean canExploitWifiAP(Context context) {
        try {
            /*WifiApManager wifiApManager = */new WifiApManager(context);
            return true;
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
            return false;
        }
    }

}