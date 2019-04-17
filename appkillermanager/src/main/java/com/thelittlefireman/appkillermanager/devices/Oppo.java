package com.thelittlefireman.appkillermanager.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.thelittlefireman.appkillermanager.utils.ActionsUtils;
import com.thelittlefireman.appkillermanager.utils.Manufacturer;

@SuppressWarnings("unused")
public class Oppo extends DeviceAbstract {
    // TODO multiple intent in a same actions need to be refractor!
    /*
    * java.lang.SecurityException: Permission Denial: starting Intent { cmp=com.coloros.safecenter/.startupapp.StartupAppListActivity } from ProcessRecord{7eba0ba 27527:crb.call.follow.mycrm/u0a229} (pid=27527, uid=10229) requires oppo.permission.OPPO_COMPONENT_SAFE*/
    //coloros3.0

    private static final String[] OPPO_ACTION_POWERSAVE_V1 = {"com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity"};
    private static final String[] OPPO_ACTION_POWERSAVE_V2 = {"com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"};

    private static final String[] OPPO_ACTION_AUTOSTART_V1 = {"com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"};
    private static final String[] OPPO_ACTION_AUTOSTART_V2 = {"com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"};

    //OLD == ColorOS V2.1
    private static final String[] OPPO_ACTION_AUTOSTART_V3 = {"com.color.oppoguardelf", "com.color.safecenter.permission.startup.StartupAppListActivity"};
    private static final String[] OPPO_ACTION_AUTOSTART_V4 = {"com.color.oppoguardelf", "com.color.safecenter.startupapp.StartupAppListActivity"};

    private static final String[] OPPO_ACTION_AUTOSTART_V5 = {"com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"};

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.OPPO;
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(OPPO_ACTION_POWERSAVE_V1[0], OPPO_ACTION_POWERSAVE_V1[1]));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(OPPO_ACTION_POWERSAVE_V2[0], OPPO_ACTION_POWERSAVE_V2[1]));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        return null;
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(OPPO_ACTION_AUTOSTART_V1[0], OPPO_ACTION_AUTOSTART_V1[1]));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(OPPO_ACTION_AUTOSTART_V2[0], OPPO_ACTION_AUTOSTART_V2[1]));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(OPPO_ACTION_AUTOSTART_V3[0], OPPO_ACTION_AUTOSTART_V3[1]));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(OPPO_ACTION_AUTOSTART_V4[0], OPPO_ACTION_AUTOSTART_V4[1]));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(OPPO_ACTION_AUTOSTART_V5[0], OPPO_ACTION_AUTOSTART_V5[1]));
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
    private ComponentName getComponentName(Context context){
        if(ActionsUtils.isPackageExist(context,p1)){

        }
        else if(ActionsUtils.isPackageExist(context,p12)){

        }
        else if(ActionsUtils.isPackageExist(context,p13)){

        }
        else if(ActionsUtils.isPackageExist(context,p2)){

        }
        return null;
    }*/
}
