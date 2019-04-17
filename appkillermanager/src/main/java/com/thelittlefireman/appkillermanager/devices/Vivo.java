package com.thelittlefireman.appkillermanager.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.thelittlefireman.appkillermanager.utils.ActionsUtils;
import com.thelittlefireman.appkillermanager.utils.Manufacturer;

@SuppressWarnings("unused")
public class Vivo extends DeviceAbstract {
// TODO multiple intent in a same actions !
    // Starting: Intent { cmp=com.vivo.permissionmanager/.activity.BgStartUpManagerActivity }
    //java.lang.SecurityException: Permission Denial: starting Intent { flg=0x10000000 cmp=com.vivo.permissionmanager/.activity.BgStartUpManagerActivity } from null (pid=28141, uid=2000) not exported from uid 1000

    private final String[] VIVO_ACTION_POWERSAVE_V1 = {"com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"};
    private final String[] VIVO_ACTION_POWERSAVE_V2 = {"com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"};

    private final String[] VIVO_ACTION_AUTOSTART_V1 = {"com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"};

   // "com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"
    //com.iqoo.secure.MainGuideActivity ??
    @Override

    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.VIVO;
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(VIVO_ACTION_POWERSAVE_V1[0], VIVO_ACTION_POWERSAVE_V1[1]));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(VIVO_ACTION_POWERSAVE_V2[0], VIVO_ACTION_POWERSAVE_V2[1]));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        return null;
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(VIVO_ACTION_AUTOSTART_V1[0], VIVO_ACTION_AUTOSTART_V1[1]));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        return null;
    }

    @Override
    public Intent getActionNotification(Context context) {
        return null;
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        return null;
    }

    @Override
    public int getHelpImagePowerSaving() {
        return 0;
    }
/*
    @Override
    public List<ComponentName> getAutoStartSettings(Context context) {
        List<ComponentName> componentNames = new ArrayList<>();
        if(ActionsUtils.isPackageExist(context, p1)){
            componentNames.add(new ComponentName(p1,p1c1));
            componentNames.add(new ComponentName(p1,p1c2));
        }
        if(ActionsUtils.isPackageExist(context,p2)){
            componentNames.add(new ComponentName(p2,p2c1));
        }
        return componentNames;
    }*/
}
