package com.thelittlefireman.appkillermanager.devices;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.DrawableRes;

import com.thelittlefireman.appkillermanager.utils.ActionsUtils;
import com.thelittlefireman.appkillermanager.utils.LogUtils;
import com.thelittlefireman.appkillermanager.utils.Manufacturer;
import com.thelittlefireman.appkillermanager.utils.SystemUtils;

public abstract class DeviceAbstract implements DeviceBase {

    @Override
    public boolean needToUseAlongwithActionDoseMode(){
        return false;
    }

    @Override
    @DrawableRes
    public int getHelpImageAutoStart(){
        return 0;
    }

    @Override
    @DrawableRes
    public int getHelpImageNotification(){
        return 0;
    }

    @DrawableRes
    @Override
    public int getHelpImagePowerSaving() {
        return 0;
    }

    @Override
    public boolean isActionDozeModeNotNecessary(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null)
                return pm.isIgnoringBatteryOptimizations(context.getPackageName());
            else
                return false;
        }
        return false;
    }

    @Override
    public Intent getActionDozeMode(Context context) {
        //Android 7.0+ Doze
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            boolean ignoringBatteryOptimizations = (pm != null) && pm.isIgnoringBatteryOptimizations(context.getPackageName());
            if (!ignoringBatteryOptimizations) {
                Intent dozeIntent = ActionsUtils.createIntent();
                // Cannot fire Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                // due to Google play device policy restriction !
                dozeIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                dozeIntent.setData(Uri.parse("package:" + context.getPackageName()));
                return dozeIntent;
            } else {
                LogUtils.i(this.getClass().getName(), "getActionDozeMode" + "App is already enable to ignore doze " +
                        "battery optimization");
            }
        }
        return null;
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.ZTE;
    }

    @Override
    public boolean isActionPowerSavingAvailable(Context context) {
        Intent intent = getActionPowerSaving(context);
        boolean intentAvailable = ActionsUtils.isIntentAvailable(context, intent);
        if (LogUtils.logEnabled()) {
            if (!intentAvailable) {
                LogUtils.logE("KillerManager.isActionPowerSavingAvailable", "INTENT NOT AVAILABLE\n" +
                        "INTENT:\n   " + ActionsUtils.getExtrasDebugInformations(intent) + "\n" +
                        "SYSTEM UTILS:\n   " + SystemUtils.getDefaultDebugInformation() + "\n" +
                        "DEVICE:\n" + getExtraDebugInformations(context));
            }
            else {
                LogUtils.logE("KillerManager.isActionPowerSavingAvailable", "INTENT AVAILABLE\n" +
                        "INTENT:\n   " + ActionsUtils.getExtrasDebugInformations(intent) + "\n" +
                        "SYSTEM UTILS:\n   " + SystemUtils.getDefaultDebugInformation() + "\n" +
                        "DEVICE:\n" + getExtraDebugInformations(context));
            }
        }
        return intentAvailable;
    }

    @Override
    public boolean isActionAutoStartAvailable(Context context) {
        Intent intent = getActionAutoStart(context);
        boolean intentAvailable = ActionsUtils.isIntentAvailable(context, intent);
        if (LogUtils.logEnabled()) {
            if (!intentAvailable) {
                LogUtils.logE("KillerManager.isActionAutoStartAvailable", "INTENT NOT AVAILABLE\n" +
                        "INTENT:\n   " + ActionsUtils.getExtrasDebugInformations(intent) + "\n" +
                        "SYSTEM UTILS:\n   " + SystemUtils.getDefaultDebugInformation() + "\n" +
                        "DEVICE:\n" + getExtraDebugInformations(context));
            }
            else {
                LogUtils.logE("KillerManager.isActionAutoStartAvailable", "INTENT AVAILABLE\n" +
                        "INTENT:\n   " + ActionsUtils.getExtrasDebugInformations(intent) + "\n" +
                        "SYSTEM UTILS:\n   " + SystemUtils.getDefaultDebugInformation() + "\n" +
                        "DEVICE:\n" + getExtraDebugInformations(context));
            }
        }
        return intentAvailable;
    }

    @Override
    public boolean isActionNotificationAvailable(Context context) {
        Intent intent = getActionNotification(context);
        boolean intentAvailable = ActionsUtils.isIntentAvailable(context, intent);
        if (LogUtils.logEnabled()) {
            if (!intentAvailable) {
                LogUtils.logE("KillerManager.isActionNotificationAvailable", "INTENT NOT AVAILABLE\n" +
                        "INTENT:\n   " + ActionsUtils.getExtrasDebugInformations(intent) + "\n" +
                        "SYSTEM UTILS:\n   " + SystemUtils.getDefaultDebugInformation() + "\n" +
                        "DEVICE:\n" + getExtraDebugInformations(context));
            }
            else {
                LogUtils.logE("KillerManager.isActionNotificationAvailable", "INTENT AVAILABLE\n" +
                        "INTENT:\n   " + ActionsUtils.getExtrasDebugInformations(intent) + "\n" +
                        "SYSTEM UTILS:\n   " + SystemUtils.getDefaultDebugInformation() + "\n" +
                        "DEVICE:\n" + getExtraDebugInformations(context));
            }
        }
        return intentAvailable;
    }

}
