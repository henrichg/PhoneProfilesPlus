package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.ResultReceiver;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class WifiApManager {
    //private static final int WIFI_AP_STATE_FAILED = 4;
    private final WifiManager mWifiManager;
    //private final String TAG = "Wifi Access Manager";
    private Method wifiControlMethod = null;
    private Method wifiApConfigurationMethod = null;
    //private Method wifiApState;
    private Method wifiApEnabled = null;

    private ConnectivityManager mConnectivityManager;
    private String packageName;

    @SuppressLint("PrivateApi")
    WifiApManager(Context context) throws SecurityException, NoSuchMethodException {
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null)
            wifiApEnabled = mWifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("$$$ WifiAP", "WifiApManager.WifiApManager-mWifiManager=" + mWifiManager);
            PPApplication.logE("$$$ WifiAP", "WifiApManager.WifiApManager-wifiApEnabled=" + wifiApEnabled);
        }*/
        if (Build.VERSION.SDK_INT >= 26) {
            mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            packageName = context.getPackageName();
        }
        else {
            if (mWifiManager != null) {
                wifiControlMethod = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                wifiApConfigurationMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration"/*,null*/);
                //wifiApState = mWifiManager.getClass().getMethod("getWifiApState");
            }
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("$$$ WifiAP", "WifiApManager.WifiApManager-wifiControlMethod=" + wifiControlMethod);
                PPApplication.logE("$$$ WifiAP", "WifiApManager.WifiApManager-wifiApConfigurationMethod=" + wifiApConfigurationMethod);
            }*/
        }
    }

    private void setWifiApState(WifiConfiguration config, boolean enabled) {
        try {
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("$$$ WifiAP", "WifiApManager.setWifiApState-config=" + config);
                PPApplication.logE("$$$ WifiAP", "WifiApManager.setWifiApState-enabled=" + enabled);
                PPApplication.logE("$$$ WifiAP", "WifiApManager.setWifiApState-mWifiManager=" + mWifiManager);
                PPApplication.logE("$$$ WifiAP", "WifiApManager.setWifiApState-wifiControlMethod=" + wifiControlMethod);
            }*/
            if (enabled) {
                if (mWifiManager != null) {
                    int wifiState = mWifiManager.getWifiState();
                    boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                    if (isWifiEnabled) {
                        //PPApplication.logE("#### setWifiEnabled", "from WifAPManager.setWifiApState");
                        //if (Build.VERSION.SDK_INT >= 26)
                        //    CmdWifi.setWifi(false);
                        //else
                            mWifiManager.setWifiEnabled(false);
                    }
                }
            }
            wifiControlMethod.setAccessible(true);
            wifiControlMethod.invoke(mWifiManager, config, enabled);
        } catch (Exception e) {
            //Log.e(TAG, "", e);
            Log.e("$$$ WifiAP", "WifiApManager.setWifiApState-exception="+e);
            Crashlytics.logException(e);
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
            Log.e("$$$ WifiAP", "WifiApManager.getWifiApConfiguration-exception="+e);
            Crashlytics.logException(e);
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
            Log.e("$$$ WifiAP", "WifiApManager.isWifiAPEnabled-exception="+e);
            Crashlytics.logException(e);
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
                    canScan = wifiApState == 11;*/
            return wifiApManager.isWifiAPEnabled();
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static boolean canExploitWifiAP(Context context) {
        try {
            /*WifiApManager wifiApManager = */new WifiApManager(context);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    void startTethering() {
        //PPApplication.logE("WifiApManager.startTethering", "mWifiManager="+mWifiManager);
        if (mWifiManager != null) {
            int wifiState = mWifiManager.getWifiState();
            boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
            //PPApplication.logE("WifiApManager.startTethering", "isWifiEnabled="+isWifiEnabled);
            if (isWifiEnabled) {
                //PPApplication.logE("#### setWifiEnabled", "from WifiAPManager.startTethering");
                //if (Build.VERSION.SDK_INT >= 26)
                //    CmdWifi.setWifi(false);
                //else
                    mWifiManager.setWifiEnabled(false);
            }
        }
        //PPApplication.logE("WifiApManager.startTethering", "mConnectivityManager="+mConnectivityManager);
        if (mConnectivityManager != null) {
            try {
                //noinspection JavaReflectionMemberAccess
                Field internalConnectivityManagerField = ConnectivityManager.class.getDeclaredField("mService");
                internalConnectivityManagerField.setAccessible(true);

                callStartTethering(internalConnectivityManagerField.get(mConnectivityManager));
            } catch (Exception e) {
                Log.e("WifiApManager.startTethering", Log.getStackTraceString(e));
                Crashlytics.logException(e);
                //PPApplication.logE("WifiApManager.startTethering", Log.getStackTraceString(e));
            }
        }
    }

    void stopTethering() {
        //PPApplication.logE("WifiApManager.stopTethering", "mConnectivityManager="+mConnectivityManager);
        if (mConnectivityManager != null) {
            try {
                Method stopTetheringMethod = ConnectivityManager.class.getDeclaredMethod("stopTethering", int.class);
                stopTetheringMethod.invoke(mConnectivityManager, 0);
            } catch (Exception e) {
                Log.e("WifiApManager.stopTethering", Log.getStackTraceString(e));
                Crashlytics.logException(e);
                //PPApplication.logE("WifiApManager.stopTethering", Log.getStackTraceString(e));
            }
        }
    }

    @SuppressWarnings({"unchecked", "JavaReflectionMemberAccess"})
    private void callStartTethering(Object internalConnectivityManager) throws ReflectiveOperationException {
        //PPApplication.logE("WifiApManager.callStartTethering", "START");

        Class internalConnectivityManagerClass = Class.forName("android.net.IConnectivityManager");
        ResultReceiver dummyResultReceiver = new ResultReceiver(null);
        try {
            Method startTetheringMethod = internalConnectivityManagerClass.getDeclaredMethod("startTethering",
                    int.class,
                    ResultReceiver.class,
                    boolean.class);
            //PPApplication.logE("WifiApManager.callStartTethering", "startTetheringMethod.1="+startTetheringMethod);

            startTetheringMethod.invoke(internalConnectivityManager,
                    0,
                    dummyResultReceiver,
                    false);
        } catch (NoSuchMethodException e) {
            //PPApplication.logE("WifiApManager.callStartTethering", Log.getStackTraceString(e));

            // Newer devices have "callingPkg" String argument at the end of this method.
            Method startTetheringMethod = internalConnectivityManagerClass.getDeclaredMethod("startTethering",
                    int.class,
                    ResultReceiver.class,
                    boolean.class,
                    String.class);
            //PPApplication.logE("WifiApManager.callStartTethering", "startTetheringMethod.2="+startTetheringMethod);

            startTetheringMethod.invoke(internalConnectivityManager,
                    0,
                    dummyResultReceiver,
                    false,
                    packageName);
        }
        //PPApplication.logE("WifiApManager.callStartTethering", "END");
    }

    @SuppressWarnings({"unchecked", "JavaReflectionMemberAccess"})
    static boolean canExploitWifiTethering(Context context) {
        try {
            canExploitWifiAP(context);
            ConnectivityManager.class.getDeclaredField("mService");
            Class internalConnectivityManagerClass = Class.forName("android.net.IConnectivityManager");
            try {
                internalConnectivityManagerClass.getDeclaredMethod("startTethering",
                        int.class,
                        ResultReceiver.class,
                        boolean.class);
            } catch (NoSuchMethodException e) {
                internalConnectivityManagerClass.getDeclaredMethod("startTethering",
                        int.class,
                        ResultReceiver.class,
                        boolean.class,
                        String.class);
            }
            ConnectivityManager.class.getDeclaredMethod("stopTethering", int.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}