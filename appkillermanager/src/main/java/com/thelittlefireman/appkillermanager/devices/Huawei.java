package com.thelittlefireman.appkillermanager.devices;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.thelittlefireman.appkillermanager.utils.ActionsUtils;
import com.thelittlefireman.appkillermanager.utils.Manufacturer;

import static com.thelittlefireman.appkillermanager.utils.SystemUtils.getEmuiRomName;

public class Huawei extends DeviceAbstract {

    private static final String HUAWEI_ACTION_POWERSAVING = "huawei.intent.action.HSM_PROTECTED_APPS";
    private static final String HUAWEI_ACTION_AUTOSTART = "huawei.intent.action.HSM_BOOTAPP_MANAGER";
    private static final String HUAWEI_ACTION_NOTIFICATION = "huawei.intent.action.NOTIFICATIONMANAGER";
    private static final String HUAWEI_SYSTEMMANAGER_PACKAGE_NAME = "com.huawei.systemmanager";
    //private static final String HUAWEI_SYSTEMMANAGER_AUTO_START_V1 = "com.huawei.systemmanager.optimize.bootstart.BootStartActivity";
    //private static final String HUAWEI_SYSTEMMANAGER_AUTO_START_V2 = "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity";
    //private static final String HUAWEI_SYSTEMMANAGER_AUTO_START_V3 = "com.huawei.permissionmanager.ui.MainActivity";
    private static final String HUAWEI_SYSTEMMANAGER_POWERSAVING_V1 = "com.huawei.systemmanager.optimize.process.ProtectActivity";
    private static final String HUAWEI_SYSTEMMANAGER_POWERSAVING_V2 = "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity";

    //com.huawei.systemmanager/com.huawei.notificationmanager.ui.NotificationManagmentActivity // huawei.intent.action.NOTIFICATIONMANAGER
    @Override
    public boolean isThatRom() {
        return isEmotionUI_23() ||
                /*isEmotionUI_3() ||
                isEmotionUI_301() ||
                isEmotionUI_31() ||
                isEmotionUI_41() ||*/
                isEmotionUI() ||
                Build.BRAND.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.MANUFACTURER.equalsIgnoreCase(getDeviceManufacturer().toString()) ||
                Build.FINGERPRINT.toLowerCase().contains(getDeviceManufacturer().toString());
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isEmotionUI_23() {
        return "EmotionUI_2.3".equalsIgnoreCase(getEmuiRomName()) || Build.DISPLAY.toLowerCase().contains("emui2.3") || "EMUI 2.3".equalsIgnoreCase(getEmuiRomName());
    }
    /*
    public static boolean isEmotionUI_301() {
        return "EmotionUI_3.0.1".equalsIgnoreCase(getEmuiRomName());
    }

    public static boolean isEmotionUI_31() {
        return "EmotionUI_3.1".equalsIgnoreCase(getEmuiRomName());
    }

    public static boolean isEmotionUI_41() {
        return "EmotionUI_4.1".equalsIgnoreCase(getEmuiRomName());
    }

    public static boolean isEmotionUI_3() {
        return "EmotionUI_3.0".equalsIgnoreCase(getEmuiRomName());
    }
    */

    @SuppressWarnings("WeakerAccess")
    public static boolean isEmotionUI() {
        String romName = getEmuiRomName();
        if (romName != null)
            return romName.toLowerCase().indexOf("emotionui_") == 0;
        else
            return false;
    }

    @Override
    public Manufacturer getDeviceManufacturer() {
        return Manufacturer.HUAWEI;
    }

    @Override
    public Intent getActionPowerSaving(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setAction(HUAWEI_ACTION_POWERSAVING);
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, HUAWEI_SYSTEMMANAGER_POWERSAVING_V1));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, HUAWEI_SYSTEMMANAGER_POWERSAVING_V2));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        return null;
    }

    @Override
    public Intent getActionAutoStart(Context context) {
        // AUTOSTART not used in huawei
        return null;
        /*
        Intent intent = ActionsUtils.createIntent();
        intent.setAction(HUAWEI_ACTION_AUTOSTART);
        if (ActionsUtils.isIntentAvailable(context, intent)) {
            return intent;

        intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, HUAWEI_SYSTEMMANAGER_AUTO_START_V1));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, HUAWEI_SYSTEMMANAGER_AUTO_START_V2));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        intent = ActionsUtils.createIntent();
        intent.setComponent(new ComponentName(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, HUAWEI_SYSTEMMANAGER_AUTO_START_V3));
        if (ActionsUtils.isIntentAvailable(context, intent))
            return intent;

        return null;*/
    }

    @Override
    public Intent getActionNotification(Context context) {
        Intent intent = ActionsUtils.createIntent();
        intent.setAction(HUAWEI_ACTION_NOTIFICATION);
        return intent;
    }

    @Override
    public String getExtraDebugInformations(Context context) {
        //noinspection StringBufferReplaceableByString
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("   ROM_VERSION:").append(getEmuiRomName()).append("\n");

        /*
        stringBuilder.append("   HuaweiSystemManagerVersionMethod:").append(getHuaweiSystemManagerVersion(context)).append("\n");

        PackageManager manager = context.getPackageManager();
        PackageInfo info;
        String versionStr = "";
        try {
            info = manager.getPackageInfo(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, 0);
            versionStr = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        stringBuilder.append("   HuaweiSystemManagerPackageVersion:").append(versionStr).append("\n");
        */

        // ----- PACAKGE INFORMATIONS -----
        stringBuilder.append("   "+HUAWEI_ACTION_AUTOSTART+":").append(ActionsUtils.isIntentAvailable(context, HUAWEI_ACTION_AUTOSTART)).append("\n");
        stringBuilder.append("   "+HUAWEI_ACTION_POWERSAVING+":").append(ActionsUtils.isIntentAvailable(context, HUAWEI_ACTION_POWERSAVING)).append("\n");
        stringBuilder.append("   "+HUAWEI_ACTION_NOTIFICATION+":").append(ActionsUtils.isIntentAvailable(context, HUAWEI_ACTION_NOTIFICATION)).append("\n");
        //stringBuilder.append("   "+HUAWEI_SYSTEMMANAGER_PACKAGE_NAME + "." + HUAWEI_SYSTEMMANAGER_AUTO_START_V1 + ":").append(ActionsUtils.isIntentAvailable(context, new ComponentName(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, HUAWEI_SYSTEMMANAGER_AUTO_START_V1))).append("\n");
        //stringBuilder.append("   "+HUAWEI_SYSTEMMANAGER_PACKAGE_NAME + "." + HUAWEI_SYSTEMMANAGER_AUTO_START_V2 + ":").append(ActionsUtils.isIntentAvailable(context, new ComponentName(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, HUAWEI_SYSTEMMANAGER_AUTO_START_V2))).append("\n");
        //stringBuilder.append("   "+HUAWEI_SYSTEMMANAGER_PACKAGE_NAME + "." + HUAWEI_SYSTEMMANAGER_AUTO_START_V3 + ":").append(ActionsUtils.isIntentAvailable(context, new ComponentName(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, HUAWEI_SYSTEMMANAGER_AUTO_START_V3))).append("\n");
        stringBuilder.append("   "+HUAWEI_SYSTEMMANAGER_PACKAGE_NAME + "." + HUAWEI_SYSTEMMANAGER_POWERSAVING_V1 + ":").append(ActionsUtils.isIntentAvailable(context, new ComponentName(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, HUAWEI_SYSTEMMANAGER_POWERSAVING_V1))).append("\n");
        stringBuilder.append("   "+HUAWEI_SYSTEMMANAGER_PACKAGE_NAME + "." + HUAWEI_SYSTEMMANAGER_POWERSAVING_V2 + ":").append(ActionsUtils.isIntentAvailable(context, new ComponentName(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, HUAWEI_SYSTEMMANAGER_POWERSAVING_V2))).append("\n");

        return stringBuilder.toString();
    }

    @Override
    public int getHelpImagePowerSaving() {
        return 0; //R.drawable.huawei_powersaving;
    }

    /*
    private ComponentName getComponentNameAutoStart(Context context) {
        int mVersion = getHuaweiSystemManagerVersion(context);
        if (mVersion == 4 || mVersion == 5) {
            return new ComponentName(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, HUAWEI_SYSTEMMANAGER_AUTO_START_V2);
        } else if (mVersion == 6) {
            return new ComponentName(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, HUAWEI_SYSTEMMANAGER_AUTO_START_V3);
        } else {
            return new ComponentName(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, HUAWEI_SYSTEMMANAGER_AUTO_START_V1);
        }
    }

    private static int getHuaweiSystemManagerVersion(Context context) {
        int version = 0;
        int versionNum = 0;
        int thirdPartFirstDigit = 0;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(HUAWEI_SYSTEMMANAGER_PACKAGE_NAME, 0);
            Log.i(Huawei.class.getName(), "manager info = " + info.toString());
            String versionStr = info.versionName;
            String versionTmp[] = versionStr.split("\\.");
            if (versionTmp.length >= 2) {
                if (Integer.parseInt(versionTmp[0]) == 5) {
                    versionNum = 500;
                } else if (Integer.parseInt(versionTmp[0]) == 4) {
                    versionNum = Integer.parseInt(versionTmp[0] + versionTmp[1] + versionTmp[2]);
                } else {
                    versionNum = Integer.parseInt(versionTmp[0] + versionTmp[1]);
                }

            }
            if (versionTmp.length >= 3) {
                thirdPartFirstDigit = Integer.valueOf(versionTmp[2].substring(0, 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (versionNum >= 330) {
            if (versionNum >= 500) {
                version = 6;
            } else if (versionNum >= 400) {
                version = 5;
            } else if (versionNum >= 331) {
                version = 4;
            } else {
                version = (thirdPartFirstDigit == 6 || thirdPartFirstDigit == 4 || thirdPartFirstDigit == 2) ? 3 : 2;
            }
        } else if (versionNum != 0) {
            version = 1;
        }
        return version;
    }
    */
}
