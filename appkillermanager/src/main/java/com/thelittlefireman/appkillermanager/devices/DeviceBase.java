package com.thelittlefireman.appkillermanager.devices;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.DrawableRes;

import com.thelittlefireman.appkillermanager.utils.Manufacturer;

public interface DeviceBase {
    boolean isThatRom();
    Manufacturer getDeviceManufacturer();
    boolean isActionPowerSavingAvailable(Context context);
    boolean isActionAutoStartAvailable(Context context);
    boolean isActionNotificationAvailable(Context context);
    @SuppressWarnings("unused")
    boolean needToUseAlongwithActionDoseMode();
    Intent getActionPowerSaving(Context context);
    Intent getActionAutoStart(Context context);
    // FIXME IS IT REALY NEEDED ? ==> REPLACE BY OTHER FUNCTION ?
    Intent getActionNotification(Context context);
    // TODO ADD FOR MEMORY OPTIMIZATION : https://github.com/00aj99/CRomAppWhitelist
    String getExtraDebugInformations(Context context);

    /**
     * Function common in all devices
     * @param context the current context
     * @return the Intent to open the doze mode settings
     */
    @SuppressWarnings("unused")
    Intent getActionDozeMode(Context context);
    @SuppressWarnings("unused")
    boolean isActionDozeModeNotNecessary(Context context);
    @SuppressWarnings({"SameReturnValue", "unused"})
    @DrawableRes int getHelpImagePowerSaving();
    @SuppressWarnings({"SameReturnValue", "unused"})
    @DrawableRes int getHelpImageAutoStart();
    @SuppressWarnings({"SameReturnValue", "unused"})
    @DrawableRes int getHelpImageNotification();
}
