package com.thelittlefireman.appkillermanager.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.thelittlefireman.appkillermanager.utils.ActionsUtils;
import com.thelittlefireman.appkillermanager.utils.Manufacturer;

public class Letv extends DeviceAbstract {

    private final String[] LETV_ACTION_POWERSAVE_V1 = {"com.letv.android.letvsafe", "com.letv.android.letvsafe.BackgroundAppManageActivity"};

    private final String[] LETV_ACTION_AUTOSTART_V1 = {"com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"};


    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.LETV;
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(LETV_ACTION_POWERSAVE_V1[0], LETV_ACTION_POWERSAVE_V1[1]));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        return null;
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(LETV_ACTION_AUTOSTART_V1[0], LETV_ACTION_AUTOSTART_V1[1]));
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
}
