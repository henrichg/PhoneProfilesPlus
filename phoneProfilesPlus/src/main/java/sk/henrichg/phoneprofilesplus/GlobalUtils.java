package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import java.text.Collator;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GlobalUtils {

    static void switchKeyguard(Context context) {
//        PPApplication.logE("[IN_THREAD_HANDLER] GlobalUtils.switchKeyguard", "EXTRA_SWITCH_KEYGUARD");

        //boolean isScreenOn;
        //PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
        //isScreenOn = ((pm != null) && PPApplication.isScreenOn(pm));

        boolean secureKeyguard;
        //if (PPApplication.keyguardManager == null)
        //    KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        if (PPApplication.keyguardManager != null) {
            secureKeyguard = PPApplication.keyguardManager.isKeyguardSecure();
            if (!secureKeyguard) {

                if (PPApplication.isScreenOn) {

                    if (ApplicationPreferences.prefLockScreenDisabled) {
                        reenableKeyguard(context);
                        disableKeyguard(context);
                    } else {
                        reenableKeyguard(context);
                    }
                }
            }
        }
    }

    private static void disableKeyguard(Context context)
    {
        if ((PPApplication.keyguardLock != null) && Permissions.hasPermission(context.getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD)) {
            try {
                PPApplication.keyguardLock.disableKeyguard();
            } catch (Exception e) {
                //Log.e("GlobalUtils.disableKeyguard", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    static void reenableKeyguard(Context context)
    {
        if ((PPApplication.keyguardLock != null) && Permissions.hasPermission(context.getApplicationContext(), Manifest.permission.DISABLE_KEYGUARD)) {
            try {
                PPApplication.keyguardLock.reenableKeyguard();
            } catch (Exception e) {
                //Log.e("GlobalUtils.reenableKeyguard", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    //-------------

    static boolean isNowTimeBetweenTimes(int startTime, int endTime) {
        if (startTime == endTime)
            return false;

        Calendar now = Calendar.getInstance();

        Calendar calStartTime = Calendar.getInstance();
        Calendar calEndTime = Calendar.getInstance();

        ///// set calendar for startTime and endTime
        Calendar hoursStartTime = Calendar.getInstance();
        hoursStartTime.set(Calendar.HOUR_OF_DAY, startTime / 60);
        hoursStartTime.set(Calendar.MINUTE, startTime % 60);
        hoursStartTime.set(Calendar.DAY_OF_MONTH, 0);
        hoursStartTime.set(Calendar.MONTH, 0);
        hoursStartTime.set(Calendar.YEAR, 0);
        hoursStartTime.set(Calendar.SECOND, 0);
        hoursStartTime.set(Calendar.MILLISECOND, 0);

        Calendar hoursEndTime = Calendar.getInstance();
        hoursEndTime.set(Calendar.HOUR_OF_DAY, endTime / 60);
        hoursEndTime.set(Calendar.MINUTE, endTime % 60);
        hoursEndTime.set(Calendar.DAY_OF_MONTH, 0);
        hoursEndTime.set(Calendar.MONTH, 0);
        hoursEndTime.set(Calendar.YEAR, 0);
        hoursEndTime.set(Calendar.SECOND, 0);
        hoursEndTime.set(Calendar.MILLISECOND, 0);

        Calendar nowTime = Calendar.getInstance();
        nowTime.set(Calendar.DAY_OF_MONTH, 0);
        nowTime.set(Calendar.MONTH, 0);
        nowTime.set(Calendar.YEAR, 0);

        Calendar midnightTime = Calendar.getInstance();
        midnightTime.set(Calendar.HOUR_OF_DAY, 0);
        midnightTime.set(Calendar.MINUTE, 0);
        midnightTime.set(Calendar.SECOND, 0);
        midnightTime.set(Calendar.MILLISECOND, 0);
        midnightTime.set(Calendar.DAY_OF_MONTH, 0);
        midnightTime.set(Calendar.MONTH, 0);
        midnightTime.set(Calendar.YEAR, 0);

        Calendar midnightMinusOneTime = Calendar.getInstance();
        midnightMinusOneTime.set(Calendar.HOUR_OF_DAY, 23);
        midnightMinusOneTime.set(Calendar.MINUTE, 59);
        midnightMinusOneTime.set(Calendar.SECOND, 59);
        midnightMinusOneTime.set(Calendar.MILLISECOND, 999);
        midnightMinusOneTime.set(Calendar.DAY_OF_MONTH, 0);
        midnightMinusOneTime.set(Calendar.MONTH, 0);
        midnightMinusOneTime.set(Calendar.YEAR, 0);

        calStartTime.set(Calendar.HOUR_OF_DAY, startTime / 60);
        calStartTime.set(Calendar.MINUTE, startTime % 60);
        calStartTime.set(Calendar.SECOND, 0);
        calStartTime.set(Calendar.MILLISECOND, 0);
        calStartTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
        calStartTime.set(Calendar.MONTH, now.get(Calendar.MONTH));
        calStartTime.set(Calendar.YEAR, now.get(Calendar.YEAR));

        calEndTime.set(Calendar.HOUR_OF_DAY, endTime / 60);
        calEndTime.set(Calendar.MINUTE, endTime % 60);
        calEndTime.set(Calendar.SECOND, 0);
        calEndTime.set(Calendar.MILLISECOND, 0);
        calEndTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
        calEndTime.set(Calendar.MONTH, now.get(Calendar.MONTH));
        calEndTime.set(Calendar.YEAR, now.get(Calendar.YEAR));

        if (hoursStartTime.getTimeInMillis() >= hoursEndTime.getTimeInMillis())
        {
            // endTime is over midnight

            if ((nowTime.getTimeInMillis() >= midnightTime.getTimeInMillis()) &&
                    (nowTime.getTimeInMillis() <= hoursEndTime.getTimeInMillis())) {
                // now is between midnight and endTime

                calStartTime.add(Calendar.DAY_OF_YEAR, -1);
            }
            else
            if ((nowTime.getTimeInMillis() >= hoursStartTime.getTimeInMillis()) &&
                    (nowTime.getTimeInMillis() <= midnightMinusOneTime.getTimeInMillis())) {
                // now is between startTime and midnight

                calEndTime.add(Calendar.DAY_OF_YEAR, 1);
            }
            else {
                // now is before start time

                calEndTime.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
        else {
            if (nowTime.getTimeInMillis() > hoursEndTime.getTimeInMillis()) {
                // now is after end time, compute for tomorrow

                calStartTime.add(Calendar.DAY_OF_YEAR, 1);
                calEndTime.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        long startAlarmTime = calStartTime.getTimeInMillis();
        long endAlarmTime = calEndTime.getTimeInMillis();

        now = Calendar.getInstance();
        long nowAlarmTime = now.getTimeInMillis();

        if ((startAlarmTime > 0) && (endAlarmTime > 0))
            return ((nowAlarmTime >= startAlarmTime) && (nowAlarmTime < endAlarmTime));
        else
            return false;
    }

    static boolean isPowerSaveMode(Context context) {

        /*String applicationPowerSaveModeInternal = ApplicationPreferences.applicationPowerSaveModeInternal;

        if (applicationPowerSaveModeInternal.equals("1") || applicationPowerSaveModeInternal.equals("2")) {
            Intent batteryStatus = null;
            try { // Huawei devices: java.lang.IllegalArgumentException: registered too many Broadcast Receivers
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                batteryStatus = context.registerReceiver(null, filter);
            } catch (Exception ignored) {
            }
            if (batteryStatus != null) {
                boolean isCharging;
                int batteryPct;

                //int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                isCharging = plugged == BatteryManager.BATTERY_PLUGGED_AC
                        || plugged == BatteryManager.BATTERY_PLUGGED_USB
                        || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
                //isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                //             status == BatteryManager.BATTERY_STATUS_FULL;
                if (!isCharging) {
                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                    batteryPct = Math.round(level / (float) scale * 100);

                    if (applicationPowerSaveModeInternal.equals("1") && (batteryPct <= 5))
                        return true;
                    if (applicationPowerSaveModeInternal.equals("2") && (batteryPct <= 15))
                        return true;
                }
            }
        }
        else
        if (applicationPowerSaveModeInternal.equals("3")) {*/
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null)
            return powerManager.isPowerSaveMode();
        //}
        //return isPowerSaveMode;
        //}

        return false;
    }

    public static boolean isLocationEnabled(Context context) {
        boolean enabled;
        if (Build.VERSION.SDK_INT >= 28) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null)
                enabled = lm.isLocationEnabled();
            else
                enabled = true;
        }
        else {
            int locationMode = 0;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                PPApplication.recordException(e);
            }
            enabled = locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        return enabled;
    }

    public static boolean isWifiSleepPolicySetToNever(Context context) {
        int wifiSleepPolicy = -1;
        try {
            wifiSleepPolicy = Settings.Global.getInt(context.getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY);
        } catch (Settings.SettingNotFoundException e) {
            //PPApplication.recordException(e);
        }
        return wifiSleepPolicy == Settings.Global.WIFI_SLEEP_POLICY_NEVER;
    }

    public static ActivityManager.RunningServiceInfo getServiceInfo(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            List<ActivityManager.RunningServiceInfo> services;
            try {
                services = manager.getRunningServices(Integer.MAX_VALUE);
            } catch (Exception e) {
                return null;
            }
            if (services != null) {
                try {
                    //ActivityManager.RunningServiceInfo serviceInfo = null;
                    for (ActivityManager.RunningServiceInfo service : services) {
                        if (serviceClass.getName().equals(service.service.getClassName())) {
                            //serviceInfo = service;
                            return service;
                        }
                    }
                    //if (serviceInfo != null)
                    //    return serviceInfo;
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass, boolean inForeground) {
        /*boolean isRunning = (instance != null);
        if (inForeground)
            isRunning = isRunning && isInForeground;

        return isRunning;*/

        ActivityManager.RunningServiceInfo service = getServiceInfo(context, serviceClass);
        if (service != null) {
            if (inForeground) {
                return service.foreground;
            } else
                return true;
        }
        else
            return false;
    }

    public static void sleep(long ms) {
        /*long start = SystemClock.uptimeMillis();
        do {
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < ms);*/
        //SystemClock.sleep(ms);
        try{ Thread.sleep(ms); }catch(InterruptedException ignored){ }
    }

    static Collator getCollator(/*Context context*/)
    {
        //if (android.os.Build.VERSION.SDK_INT < 24) {
        // get application Locale
//            String lang = ApplicationPreferences.applicationLanguage(context);

        Locale appLocale;

//            if (!lang.equals("system")) {
//                String[] langSplit = lang.split("-");
//                if (langSplit.length == 1)
//                    appLocale = new Locale(lang);
//                else
//                    appLocale = new Locale(langSplit[0], langSplit[1]);
//            } else {
        //if (Build.VERSION.SDK_INT >= 24) {
        //appLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
        //} else {
        //    appLocale = Resources.getSystem().getConfiguration().locale;
        //}
//            }

        // application locale
        appLocale = Locale.getDefault();

        // get collator for application locale
        return Collator.getInstance(appLocale);
//        }
//        else {
//            //Log.d("GlobalGUIRoutines.getCollator", java.util.Locale.getDefault().toString());
//            return Collator.getInstance();
//        }
    }

    // ----------------------

    static String getRealPath(Uri treeUri)
    {
        if (treeUri == null)
            return "";
        String path1 = treeUri.getPath();
        if (path1.startsWith("/tree/"))
        {
            String path2 = path1.substring("/tree/".length());
            if (path2.startsWith("primary:"))
            {
                String primary = path2.substring(0, "primary:".length());
                if (primary.contains(":"))
                {
                    String storeName = "/storage/emulated/0/";
                    String[] splits = path2.split(":");
                    String last = splits[splits.length-1];
                    return storeName + last;
                }
            }
            else
            {
                if (path2.contains(":"))
                {
                    String[] splits = path2.split(":");
                    String storeName = splits[0];
                    String last = splits[splits.length-1];
                    return  "/" + storeName + "/" + last;
                }
            }
        }
        return path1;
    }

    static int getCallState(Context context) {
        TelephonyManager telephonyManagerDefault = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManagerDefault != null) {
            int simCount = telephonyManagerDefault.getPhoneCount();
            if (simCount > 1) {
                SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                //SubscriptionManager.from(appContext);
                if (mSubscriptionManager != null) {
                    List<SubscriptionInfo> subscriptionList = null;
                    try {
                        // Loop through the subscription list i.e. SIM list.
                        subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                    } catch (SecurityException e) {
                        //PPApplication.recordException(e);
                    }
                    if (subscriptionList != null) {
                        int callStateSIM1 = TelephonyManager.CALL_STATE_IDLE;
                        int callStateSIM2 = TelephonyManager.CALL_STATE_IDLE;
                        for (int i = 0; i < subscriptionList.size(); i++) {
                            // Get the active subscription ID for a given SIM card.
                            SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                            if (subscriptionInfo != null) {
                                int subscriptionId = subscriptionInfo.getSubscriptionId();
                                if (subscriptionInfo.getSimSlotIndex() == 0) {
                                    TelephonyManager telephonyManagerSIM1 = telephonyManagerDefault.createForSubscriptionId(subscriptionId);
                                    callStateSIM1 = telephonyManagerSIM1.getCallState(subscriptionId);
                                }
                                if ((subscriptionInfo.getSimSlotIndex() == 1)) {
                                    TelephonyManager telephonyManagerSIM2 = telephonyManagerDefault.createForSubscriptionId(subscriptionId);
                                    callStateSIM2 = telephonyManagerSIM2.getCallState(subscriptionId);
                                }
                            }
                        }
                        if ((callStateSIM1 == TelephonyManager.CALL_STATE_RINGING) ||
                                (callStateSIM2 == TelephonyManager.CALL_STATE_RINGING))
                            return TelephonyManager.CALL_STATE_RINGING;

                        if ((callStateSIM1 == TelephonyManager.CALL_STATE_OFFHOOK) ||
                                (callStateSIM2 == TelephonyManager.CALL_STATE_OFFHOOK))
                            return TelephonyManager.CALL_STATE_OFFHOOK;

                        /*if (callStateSIM1 != TelephonyManager.CALL_STATE_IDLE)
                            return callStateSIM1;
                        return callStateSIM2;*/
                    }
                }
            }
            else {
                return telephonyManagerDefault.getCallState();
            }
        }
        return TelephonyManager.CALL_STATE_IDLE;
    }

    static boolean hasSIMCard(Context appContext, int simCard) {
        TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            if ((Build.VERSION.SDK_INT < 26) || (simCard == 0)) {
                return telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
            } else {
                boolean hasSIM = false;
                if (Permissions.checkPhone(appContext)) {
                    SubscriptionManager mSubscriptionManager = (SubscriptionManager) appContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    //SubscriptionManager.from(context);
                    if (mSubscriptionManager != null) {
                        List<SubscriptionInfo> subscriptionList = null;
                        try {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                        } catch (SecurityException e) {
                            PPApplication.recordException(e);
                        }
                        if (subscriptionList != null) {
                            for (int i = 0; i < subscriptionList.size(); i++) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                if (subscriptionInfo != null) {
                                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                                    //if (simCard == 0) {
                                    //    if (telephonyManager.getSimState(slotIndex) == TelephonyManager.SIM_STATE_READY) {
                                    //        // sim card is ready
                                    //        hasSIM = true;
                                    //        break;
                                    //    }
                                    //}
                                    //else {
                                    if (simCard == (slotIndex + 1)) {
                                        if (telephonyManager.getSimState(slotIndex) == TelephonyManager.SIM_STATE_READY) {
                                            // sim card is ready
                                            hasSIM = true;
                                            break;
                                        }
                                    }
                                    //}
                                }
                            }
                        }
                    }
                }
                return hasSIM;
            }
        }
        return false;
    }

    static int getSIMCardFromSubscriptionId(Context appContext, int subscriptionId) {
        TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            if (Build.VERSION.SDK_INT < 26) {
                return 0;
            } else {
                if (Permissions.checkPhone(appContext)) {
                    SubscriptionManager mSubscriptionManager = (SubscriptionManager) appContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    //SubscriptionManager.from(context);
                    if (mSubscriptionManager != null) {
                        List<SubscriptionInfo> subscriptionList = null;
                        try {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                        } catch (SecurityException e) {
                            PPApplication.recordException(e);
                        }
                        if (subscriptionList != null) {
                            int simCard = 0;
                            for (int i = 0; i < subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/ i++) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                if ((subscriptionInfo != null) &&
                                        (subscriptionInfo.getSubscriptionId() == subscriptionId)) {
                                    simCard = subscriptionInfo.getSimSlotIndex() + 1;
                                    break;
                                }
                            }
                            return simCard;
                        } else
                            return 0;
                    } else
                        return 0;
                } else
                    return -1;
            }
        }
        return -1;
    }

}
