package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
//import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.ResultReceiver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class WifiApManager {
    //private static final int WIFI_AP_STATE_FAILED = 4;
    private final WifiManager mWifiManager;
    //private final String TAG = "Wifi Access Manager";
    //private final Method wifiControlMethod = null;
    //private final Method wifiApConfigurationMethod = null;
    //private Method wifiApState;
    private Method wifiApEnabled = null;

    private final ConnectivityManager mConnectivityManager;
    private final String packageName;

    WifiApManager(Context context) throws SecurityException, NoSuchMethodException {
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null)
            wifiApEnabled = mWifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        packageName = PPApplication.PACKAGE_NAME;
    }

    /*
    private void setWifiApState(WifiConfiguration config, boolean enabled, boolean doNotChangeWifi) {
        try {
            if (enabled) {
                if (!doNotChangeWifi) {
                    if (mWifiManager != null) {
                        int wifiState = mWifiManager.getWifiState();
                        boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                        if (isWifiEnabled) {
                            //if (Build.VERSION.SDK_INT >= 29)
                            //    CmdWifi.setWifi(false);
                            ActivateProfileHelper.setWifi(context.getApplicationContext(), false);
                            mWifiManager.setWifiEnabled(false);
                        }
                    }
                }
            }
            wifiControlMethod.setAccessible(true);
            wifiControlMethod.invoke(mWifiManager, config, enabled);
        } catch (Exception e) {
            //Log.e("WifiApManager.setWifiApState", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
    }
    */

    /*
    void setWifiApState(boolean enabled, boolean doNotChangeWifi) {
        WifiConfiguration wifiConfiguration = getWifiApConfiguration();
        setWifiApState(wifiConfiguration, enabled, doNotChangeWifi);
    }
    */

    /*
    // not working in Android 8+ :-/
    // https://stackoverflow.com/questions/46392277/changing-android-hotspot-settings
    private WifiConfiguration getWifiApConfiguration()
    {
        try{
            wifiApConfigurationMethod.setAccessible(true);
            return (WifiConfiguration)wifiApConfigurationMethod.invoke(mWifiManager);
        }
        catch (Exception e)
        {
            //Log.e("WifiApManager.getWifiApConfiguration", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            return null;
        }
    }
    */

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
            //noinspection ConstantConditions
            return (Boolean) wifiApEnabled.invoke(mWifiManager);
        } catch (Exception e) {
            //Log.e("WifiApManager.isWifiAPEnabled", e);
            PPApplicationStatic.recordException(e);
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

    static boolean isWifiAPEnabledA30(Context context) {
        try {
            //boolean enabled;
            /*IWifiManager adapter = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));  // service list | grep IWifiManager
            enabled = adapter.getWifiApEnabledState() == WifiManager.WIFI_AP_STATE_ENABLED;
            return enabled;*/
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return wifiManager.isWifiApEnabled();
        } catch (Throwable e) {
            //Log.e("WifiApManager.isWifiAPEnabledA30", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
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

    @SuppressWarnings("JavaReflectionMemberAccess")
    void startTethering(Context context, boolean doNotChangeWifi) {
        if (!doNotChangeWifi) {
            if (mWifiManager != null) {
                int wifiState = mWifiManager.getWifiState();
                boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                if (isWifiEnabled) {
                    //if (Build.VERSION.SDK_INT >= 29)
                    //    CmdWifi.setWifi(false);
                        ActivateProfileHelper.setWifi(context.getApplicationContext(), false);
                        //mWifiManager.setWifiEnabled(false);
                }
            }
        }
        if (mConnectivityManager != null) {
            try {
                @SuppressLint("DiscouragedPrivateApi")
                Field internalConnectivityManagerField = ConnectivityManager.class.getDeclaredField("mService");
                internalConnectivityManagerField.setAccessible(true);

                callStartTethering(internalConnectivityManagerField.get(mConnectivityManager));
            } catch (Exception e) {
                //Log.e("WifiApManager.startTethering", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            }
        }
    }

    void stopTethering() {
        if (mConnectivityManager != null) {
            try {
                Method stopTetheringMethod = ConnectivityManager.class.getDeclaredMethod("stopTethering", int.class);
                stopTetheringMethod.invoke(mConnectivityManager, 0);
            } catch (Exception e) {
                //Log.e("WifiApManager.stopTethering", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            }
        }
    }

    @SuppressWarnings({"unchecked", "JavaReflectionMemberAccess", "rawtypes"})
    private void callStartTethering(Object internalConnectivityManager) throws ReflectiveOperationException {
        @SuppressLint("PrivateApi")
        Class internalConnectivityManagerClass = Class.forName("android.net.IConnectivityManager");
        ResultReceiver dummyResultReceiver = new ResultReceiver(null);
        try {
            Method startTetheringMethod = internalConnectivityManagerClass.getDeclaredMethod("startTethering",
                    int.class,
                    ResultReceiver.class,
                    boolean.class);

            startTetheringMethod.invoke(internalConnectivityManager,
                    0,
                    dummyResultReceiver,
                    false);
        } catch (NoSuchMethodException e) {

            // Newer devices have "callingPkg" String argument at the end of this method.
            Method startTetheringMethod = internalConnectivityManagerClass.getDeclaredMethod("startTethering",
                    int.class,
                    ResultReceiver.class,
                    boolean.class,
                    String.class);

            startTetheringMethod.invoke(internalConnectivityManager,
                    0,
                    dummyResultReceiver,
                    false,
                    packageName);
        }
    }

    @SuppressWarnings({"unchecked", "JavaReflectionMemberAccess", "rawtypes"})
    @SuppressLint("DiscouragedPrivateApi")
    static boolean canExploitWifiTethering(Context context) {
        try {
            if (canExploitWifiAP(context)) {
                ConnectivityManager.class.getDeclaredField("mService");
                @SuppressLint("PrivateApi")
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
            } else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Tnank to author of MacroDroid application.
    // It is used as source of this implenetation.
    static class MyOnStartTetheringCallback extends MyOnStartTetheringCallbackAbstract {
        MyOnStartTetheringCallback() {
        }
    }

    @SuppressWarnings("RedundantArrayCreation")
    @SuppressLint("PrivateApi")
    static boolean canExploitWifiTethering30(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.isWifiApEnabled();
        } catch (Throwable e) {
            return false;
        }

        MyOnStartTetheringCallback callback = new MyOnStartTetheringCallback();
        Object myOnStartTetheringCallbackAbstractObj;
        Class<?> myOnStartTetheringCallbackAbstractObjCls;// = null;
        try {
            myOnStartTetheringCallbackAbstractObj =
                    new WifiTetheringCallbackMaker(context, callback)
                            .getTtetheringCallback().getDeclaredConstructor(new Class[]{Integer.TYPE}).newInstance(new Object[]{0});
        } catch (Exception e) {
            myOnStartTetheringCallbackAbstractObj = null;
        }
        if (myOnStartTetheringCallbackAbstractObj == null)
            return false;

        ConnectivityManager connectivityManager = context.getApplicationContext().getSystemService(ConnectivityManager.class);
        try {
            myOnStartTetheringCallbackAbstractObjCls = Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
        } catch (Exception e2) {
            return false;
        }

        try {
            Method declaredMethod = connectivityManager.getClass().getDeclaredMethod("startTethering",
                    new Class[]{Integer.TYPE, Boolean.TYPE, myOnStartTetheringCallbackAbstractObjCls, Handler.class});
            //noinspection ConstantConditions
            if (declaredMethod == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        try {
            Method declaredMethod = connectivityManager.getClass().getDeclaredMethod("stopTethering", new Class[]{Integer.TYPE});
            //noinspection ConstantConditions
            if (declaredMethod == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    // Tnank to author of MacroDroid application.
    // It is used as source of this implenetation.
    static void startTethering30(final Context context, boolean doNotChangeWifi) {
        if (!doNotChangeWifi) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                int wifiState = wifiManager.getWifiState();
                boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                if (isWifiEnabled) {
                    //if (Build.VERSION.SDK_INT >= 29)
                    //    CmdWifi.setWifi(false);
                    ActivateProfileHelper.setWifi(context.getApplicationContext(), false);
                    //wifiManager.setWifiEnabled(false);
                }
            }
        }

        // keep this: it is required to use handlerThreadBroadcast for cal listener
        PPApplicationStatic.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        __handler.post(() -> {
            MyOnStartTetheringCallback callback = new MyOnStartTetheringCallback();
            _startTethering30(context, callback, __handler);
        });
    }

    // Thanks to author of MacroDroid application.
    // It is used as source of this implenetation.
    @SuppressWarnings("RedundantArrayCreation")
    @SuppressLint("PrivateApi")
    static private void _startTethering30(Context context,
                                          MyOnStartTetheringCallbackAbstract myOnStartTetheringCallbackAbstract,
                                          Handler handler) {
        Object myOnStartTetheringCallbackAbstractObj;
        Class<?> myOnStartTetheringCallbackAbstractObjCls;// = null;
        try {
            myOnStartTetheringCallbackAbstractObj =
                    new WifiTetheringCallbackMaker(context, myOnStartTetheringCallbackAbstract)
                    .getTtetheringCallback().getDeclaredConstructor(new Class[]{Integer.TYPE}).newInstance(new Object[]{0});
        } catch (Exception e) {
            //Log.e("WifiApManager._startTethering30 (1)", Log.getStackTraceString(e));
            myOnStartTetheringCallbackAbstractObj = null;
        }
        //if (myOnStartTetheringCallbackAbstractObj != null) {
            ConnectivityManager connectivityManager = context.getApplicationContext().getSystemService(ConnectivityManager.class);
            try {
                myOnStartTetheringCallbackAbstractObjCls = Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
            } catch (Exception e2) {
                //Log.e("WifiApManager._startTethering30 (2)", Log.getStackTraceString(e2));
                PPApplicationStatic.recordException(e2);
                return;
            }
            try {
                Method declaredMethod = connectivityManager.getClass().getDeclaredMethod("startTethering",
                        new Class[]{Integer.TYPE, Boolean.TYPE, myOnStartTetheringCallbackAbstractObjCls, Handler.class});
                //noinspection ConstantConditions
                if (declaredMethod == null) {
                    //Log.e("WifiApManager._startTethering30", "startTetheringMethod is null");
                    return;
                }
                declaredMethod.invoke(connectivityManager, new Object[]{0, Boolean.FALSE, myOnStartTetheringCallbackAbstractObj, handler});
            } catch (Exception e) {
                //Log.e("WifiApManager._startTethering30 (3)", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            }
        //}
    }

    // Thanks to author of MacroDroid application.
    // It is used as source of this implenetation.
    @SuppressWarnings("RedundantArrayCreation")
    static void stopTethering30(Context context) {
        ConnectivityManager connectivityManager = context.getApplicationContext().getSystemService(ConnectivityManager.class);
        try {
            Method declaredMethod = connectivityManager.getClass().getDeclaredMethod("stopTethering", new Class[]{Integer.TYPE});
            //noinspection ConstantConditions
            if (declaredMethod == null) {
                return;
            }
            declaredMethod.invoke(connectivityManager, new Object[]{0});
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

}