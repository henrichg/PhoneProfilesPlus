package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

public final class WifiApManager {
    private static final int WIFI_AP_STATE_FAILED = 4;
    private final WifiManager mWifiManager;
    private final String TAG = "Wifi Access Manager";
    private Method wifiControlMethod;
    private Method wifiApConfigurationMethod;
    private Method wifiApState;
    private Method wifiApEnabled;

    public WifiApManager(Context context) throws SecurityException, NoSuchMethodException {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiControlMethod = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class,boolean.class);
        wifiApConfigurationMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration",null);
        wifiApState = mWifiManager.getClass().getMethod("getWifiApState");
        wifiApEnabled = mWifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
    }

    public boolean setWifiApState(WifiConfiguration config, boolean enabled) {
        try {
            if (enabled) {
                mWifiManager.setWifiEnabled(!enabled);
            }
            wifiControlMethod.setAccessible(true);
            return (Boolean) wifiControlMethod.invoke(mWifiManager, config, enabled);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public WifiConfiguration getWifiApConfiguration()
    {
        try{
            wifiApConfigurationMethod.setAccessible(true);
            return (WifiConfiguration)wifiApConfigurationMethod.invoke(mWifiManager, null);
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public int getWifiApState() {
        try {
            wifiApState.setAccessible(true);
            return (Integer)wifiApState.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return WIFI_AP_STATE_FAILED;
        }
    }

    public boolean isWifiAPEnabled() {
        try {
            wifiApEnabled.setAccessible(true);
            return (Boolean) wifiApEnabled.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }

    }
}