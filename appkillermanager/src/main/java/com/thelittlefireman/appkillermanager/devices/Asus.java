package com.thelittlefireman.appkillermanager.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.DrawableRes;

import com.thelittlefireman.appkillermanager.utils.ActionsUtils;
import com.thelittlefireman.appkillermanager.utils.Manufacturer;

// TODO TESTS
public class Asus extends DeviceAbstract {

    //new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(
    //        Uri.parse("mobilemanager://function/entry/AutoStart"))

    private static final String ASUS_PACAKGE_MOBILEMANAGER = "com.asus.mobilemanager";
    private static final String ASUS_ACTIVITY_MOBILEMANAGER_FUNCTION_ACTIVITY = "com.asus.mobilemanager.entry.FunctionActivity";
    private static final String ASUS_ACTIVITY_MOBILEMANAGER_FUNCTION_AUTOSTART_ACTIVITY = "com.asus.mobilemanager.autostart.AutoStartActivity";

    @Override
    public boolean isThatRom() {
        return  Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.ASUS;
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        // Juste need to use the regular battery non optimization
        // permission =)
        return super.getActionDozeMode(context);
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.putExtra("showNotice",true);
        intent.setComponent(new ComponentName(ASUS_PACAKGE_MOBILEMANAGER, ASUS_ACTIVITY_MOBILEMANAGER_FUNCTION_AUTOSTART_ACTIVITY));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        return null;
    }

    @Override
    public Intent getActionNotification(Context context) {
        // Need to clic on notifications items
        Intent intent = ActionsUtils.createIntent();
        intent.putExtra("showNotice",true);
        intent.setComponent(new ComponentName(ASUS_PACAKGE_MOBILEMANAGER, ASUS_ACTIVITY_MOBILEMANAGER_FUNCTION_ACTIVITY));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        return null;
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return null;
    }

    @Override
    @DrawableRes
    public int getHelpImageAutoStart(){
        return 0; //R.drawable.asus_autostart;
    }

    @Override
    @DrawableRes
    public int getHelpImageNotification(){
        return 0; //R.drawable.asus_notification;
    }
}
