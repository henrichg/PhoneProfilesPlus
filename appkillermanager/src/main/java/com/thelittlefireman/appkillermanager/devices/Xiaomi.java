package com.thelittlefireman.appkillermanager.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.thelittlefireman.appkillermanager.utils.ActionsUtils;
import com.thelittlefireman.appkillermanager.utils.Manufacturer;
import com.thelittlefireman.appkillermanager.utils.SystemUtils;

public class Xiaomi extends DeviceAbstract {


    // TODO TEST new Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings"))
    @SuppressWarnings("unused")
    private static final String MIUI_ACTION_PERMS = "miui.intent.action.APP_PERM_EDITOR";
    @SuppressWarnings("unused")
    private static final String MIUI_ACTION_PERMS_EXTRA = "extra_pkgname";

    // ONE SPECIFIQUE APP
    private static final String[] MIUI_ACTION_POWERSAVE = {"com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"};

    // OPEN DEFAULT LIST BATTERYSAVER
    @SuppressWarnings("unused")
    private static final String MIUI_ACTION_POWER_SAVE_LIST = "miui.intent.action.POWER_HIDE_MODE_APP_LIST";
    private static final String MIUI_ACTION_POWER_SAVE_EXTRA_NAME = "package_name";
    private static final String MIUI_ACTION_POWER_SAVE_EXTRA_LABEL = "package_label";
    private static final String MIUI_ACTION_AUTOSTART_V1 = "miui.intent.action.OP_AUTO_START";
    private static final String[] MIUI_ACTION_AUTOSTART_V2 = {"com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"};

    @Override
    public boolean isThatRom() {
        return Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.XIAOMI;
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(MIUI_ACTION_POWERSAVE[0], MIUI_ACTION_POWERSAVE[1]));
        intent.putExtra(MIUI_ACTION_POWER_SAVE_EXTRA_NAME, context.getPackageName());
        intent.putExtra(MIUI_ACTION_POWER_SAVE_EXTRA_LABEL, SystemUtils.getApplicationName(context));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        return null;
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setAction(MIUI_ACTION_AUTOSTART_V1);
        intent.putExtra(MIUI_ACTION_POWER_SAVE_EXTRA_NAME, context.getPackageName());
        intent.putExtra(MIUI_ACTION_POWER_SAVE_EXTRA_LABEL, SystemUtils.getApplicationName(context));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(MIUI_ACTION_AUTOSTART_V2[0], MIUI_ACTION_AUTOSTART_V2[1]));
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
    // TODO CHECK IF GETPACKAGENAME IS NAME OF LIB OR APP
    @Override
    public List<Intent> getAutoStartSettings(Context context) {
        List<Intent> intents = new ArrayList<>();
        intents.add(new Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").addCategory(Intent.CATEGORY_DEFAULT));
        //com.miui.powerkeeper/com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity
        intents.add(new Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT));
        //com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity
        return intents;
    }
*/
    /*
    * new Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").addCategory(Intent.CATEGORY_DEFAULT)
new Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT)
    * */

    /*
    private static int getMiuiVersion() {
        String version = SystemUtils.getSystemProperty("ro.miui.ui.version.name");
        if (version != null) {
            try {
                return Integer.parseInt(version.substring(1));
            } catch (Exception e) {
                Log.e(Xiaomi.class.getName(), "get miui version code error, version : " + version);
                Log.e(Xiaomi.class.getName(), Log.getStackTraceString(e));
            }
        }
        return -1;
    }

    public Intent miui_V5(Context context) {
        String packageName = context.getPackageName();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package" , packageName, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }


    public Intent miui_V6(Context context) {
        Intent intent = new Intent(MIUI_ACTION);
        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        intent.putExtra(MIUI_EXTRA, context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    // MIUI V7

    private Intent miui_V7(Context context) {
        Intent intent = new Intent(MIUI_ACTION);
        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        intent.putExtra(MIUI_EXTRA, context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    //MIUI V8

    private Intent miui_V8(Context context) {
        Intent intent = new Intent(MIUI_ACTION);
        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
        intent.putExtra(MIUI_EXTRA, context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
*/
}
