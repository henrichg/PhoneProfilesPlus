package sk.henrichg.phoneprofilesplus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("FieldCanBeLocal")
class AutoStartPermissionHelper  {

    static AutoStartPermissionHelper instance = null;

    /***
     * Xiaomi
     */
    private final String BRAND_XIAOMI = "xiaomi";
    private final String BRAND_XIAOMI_POCO = "poco";
    private final String BRAND_XIAOMI_REDMI = "redmi";
    private final String PACKAGE_XIAOMI_MAIN = "com.miui.securitycenter";
    private final String PACKAGE_XIAOMI_COMPONENT = "com.miui.permcenter.autostart.AutoStartManagementActivity";

    /***
     * Letv
     */
    private final String BRAND_LETV = "letv";
    private final String PACKAGE_LETV_MAIN = "com.letv.android.letvsafe";
    private final String PACKAGE_LETV_COMPONENT = "com.letv.android.letvsafe.AutobootManageActivity";

    /***
     * ASUS ROG
     */
    private final String BRAND_ASUS = "asus";
    private final String PACKAGE_ASUS_MAIN = "com.asus.mobilemanager";
    private final String PACKAGE_ASUS_COMPONENT = "com.asus.mobilemanager.powersaver.PowerSaverSettings";
    private final String PACKAGE_ASUS_COMPONENT_FALLBACK = "com.asus.mobilemanager.autostart.AutoStartActivity";

    /***
     * Honor
     */
    private final String BRAND_HONOR = "honor";
    private final String PACKAGE_HONOR_MAIN = "com.huawei.systemmanager";
    private final String PACKAGE_HONOR_COMPONENT = "com.huawei.systemmanager.optimize.process.ProtectActivity";

    /***
     * Huawei
     */
    private final String BRAND_HUAWEI = "huawei";
    private final String PACKAGE_HUAWEI_MAIN = "com.huawei.systemmanager";
    private final String PACKAGE_HUAWEI_COMPONENT = "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity";
    private final String PACKAGE_HUAWEI_COMPONENT_FALLBACK = "com.huawei.systemmanager.optimize.process.ProtectActivity";

    /**
     * Oppo
     */
    private final String BRAND_OPPO = "oppo";
    private final String PACKAGE_OPPO_MAIN = "com.coloros.safecenter";
    private final String PACKAGE_OPPO_FALLBACK = "com.oppo.safe";
    private final String PACKAGE_OPPO_COMPONENT = "com.coloros.safecenter.permission.startup.StartupAppListActivity";
    private final String PACKAGE_OPPO_COMPONENT_FALLBACK = "com.oppo.safe.permission.startup.StartupAppListActivity";
    private final String PACKAGE_OPPO_COMPONENT_FALLBACK_A = "com.coloros.safecenter.startupapp.StartupAppListActivity";

    /**
     * Vivo
     */

    private final String BRAND_VIVO = "vivo";
    private final String PACKAGE_VIVO_MAIN = "com.iqoo.secure";
    private final String PACKAGE_VIVO_FALLBACK = "com.vivo.permissionmanager";
    private final String PACKAGE_VIVO_COMPONENT = "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity";
    private final String PACKAGE_VIVO_COMPONENT_FALLBACK = "com.vivo.permissionmanager.activity.BgStartUpManagerActivity";
    private final String PACKAGE_VIVO_COMPONENT_FALLBACK_A = "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager";

    /**
     * Nokia
     */

    private final String BRAND_NOKIA = "nokia";
    private final String PACKAGE_NOKIA_MAIN = "com.evenwell.powersaving.g3";
    private final String PACKAGE_NOKIA_COMPONENT = "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity";

    /***
     * Samsung
     */
    @SuppressWarnings("unused")
    private final String BRAND_SAMSUNG = "samsung";
    private final String PACKAGE_SAMSUNG_MAIN = "com.samsung.android.lool";
    private final String PACKAGE_SAMSUNG_COMPONENT = "com.samsung.android.sm.ui.battery.BatteryActivity";
    private final String PACKAGE_SAMSUNG_COMPONENT_2 = "com.samsung.android.sm.battery.ui.usage.CheckableAppListActivity";
    private final String PACKAGE_SAMSUNG_COMPONENT_3 = "com.samsung.android.sm.battery.ui.BatteryActivity";

    /***
     * One plus
     */
    private final String BRAND_ONE_PLUS = "oneplus";
    private final String PACKAGE_ONE_PLUS_MAIN = "com.oneplus.security";
    private final String PACKAGE_ONE_PLUS_COMPONENT = "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity";
    private final String PACKAGE_ONE_PLUS_ACTION = "com.android.settings.action.BACKGROUND_OPTIMIZE";

    private final List<String> PACKAGES_TO_CHECK_FOR_PERMISSION = Arrays.asList(
            PACKAGE_ASUS_MAIN,
            PACKAGE_XIAOMI_MAIN,
            PACKAGE_LETV_MAIN,
            PACKAGE_HONOR_MAIN,
            PACKAGE_OPPO_MAIN,
            PACKAGE_OPPO_FALLBACK,
            PACKAGE_VIVO_MAIN,
            PACKAGE_VIVO_FALLBACK,
            PACKAGE_NOKIA_MAIN,
            PACKAGE_HUAWEI_MAIN,
//            PACKAGE_SAMSUNG_MAIN,
            PACKAGE_ONE_PLUS_MAIN);

    boolean getAutoStartPermission(Context context) {

        String brand = Build.BRAND.toLowerCase(Locale.ROOT);
        switch (brand) {
            case BRAND_ASUS:
                return autoStartAsus(context);
            case BRAND_XIAOMI:
            case BRAND_XIAOMI_POCO:
            case BRAND_XIAOMI_REDMI:
                return autoStartXiaomi(context);
            case BRAND_LETV:
                return autoStartLetv(context);
            case BRAND_HONOR:
                return autoStartHonor(context);
            case BRAND_HUAWEI:
                return autoStartHuawei(context);
            case BRAND_OPPO:
                return autoStartOppo(context);
            case BRAND_VIVO:
                return autoStartVivo(context);
            case BRAND_NOKIA:
                return autoStartNokia(context);
//            case BRAND_SAMSUNG:
//                return autoStartSamsung(context);
            case BRAND_ONE_PLUS:
                return autoStartOnePlus(context);
            default:
                return false;
        }
    }

    boolean isAutoStartPermissionAvailable(Context context) {

        List<ApplicationInfo> packages;
        PackageManager pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (PACKAGES_TO_CHECK_FOR_PERMISSION.contains(packageInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean autoStartXiaomi(Context context) {
        if (isPackageExists(context, PACKAGE_XIAOMI_MAIN)) {
            try {
                startIntent(context, PACKAGE_XIAOMI_MAIN, PACKAGE_XIAOMI_COMPONENT);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean autoStartAsus(Context context) {
        if (isPackageExists(context, PACKAGE_ASUS_MAIN)) {
            try {
                startIntent(context, PACKAGE_ASUS_MAIN, PACKAGE_ASUS_COMPONENT);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    startIntent(context, PACKAGE_ASUS_MAIN, PACKAGE_ASUS_COMPONENT_FALLBACK);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean autoStartLetv(Context context) {
        if (isPackageExists(context, PACKAGE_LETV_MAIN)) {
            try {
                startIntent(context, PACKAGE_LETV_MAIN, PACKAGE_LETV_COMPONENT);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean autoStartHonor(Context context) {
        if (isPackageExists(context, PACKAGE_HONOR_MAIN)) {
            try {
                startIntent(context, PACKAGE_HONOR_MAIN, PACKAGE_HONOR_COMPONENT);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean autoStartHuawei(Context context) {
        if (isPackageExists(context, PACKAGE_HUAWEI_MAIN)) {
            try {
                startIntent(context, PACKAGE_HUAWEI_MAIN, PACKAGE_HUAWEI_COMPONENT);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    startIntent(context, PACKAGE_HUAWEI_MAIN, PACKAGE_HUAWEI_COMPONENT_FALLBACK);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean autoStartOppo(Context context) {
        boolean ok;
        if (isPackageExists(context, PACKAGE_OPPO_MAIN) || isPackageExists(context, PACKAGE_OPPO_FALLBACK)) {
            try {
                startIntent(context, PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT);
                ok = true;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    startIntent(context, PACKAGE_OPPO_FALLBACK, PACKAGE_OPPO_COMPONENT_FALLBACK);
                    ok = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    try {
                        startIntent(context, PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT_FALLBACK_A);
                        ok = true;
                    } catch (Exception exx) {
                        exx.printStackTrace();
                        ok = false;
                    }
                }
            }
        } else {
            ok = false;
        }

        if (!ok) {
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:${context.packageName}"));
                try {
                    context.startActivity(intent);
                    ok = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    ok = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                ok = false;
            }
        }

        return ok;
    }

    private boolean autoStartVivo(Context context) {
        if (isPackageExists(context, PACKAGE_VIVO_MAIN) || isPackageExists(context, PACKAGE_VIVO_FALLBACK)) {
            try {
                startIntent(context, PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    startIntent(context, PACKAGE_VIVO_FALLBACK, PACKAGE_VIVO_COMPONENT_FALLBACK);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    try {
                        startIntent(context, PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT_FALLBACK_A);
                    } catch (Exception exx) {
                        exx.printStackTrace();
                        return false;
                    }
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean autoStartNokia(Context context) {
        if (isPackageExists(context, PACKAGE_NOKIA_MAIN)) {
            try {
                startIntent(context, PACKAGE_NOKIA_MAIN, PACKAGE_NOKIA_COMPONENT);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    @SuppressWarnings("unused")
    private boolean autoStartSamsung(Context context) {
        if (isPackageExists(context, PACKAGE_SAMSUNG_MAIN)) {
            try {
                startIntent(context, PACKAGE_SAMSUNG_MAIN, PACKAGE_SAMSUNG_COMPONENT);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    startIntent(context, PACKAGE_SAMSUNG_MAIN, PACKAGE_SAMSUNG_COMPONENT_2);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    try {
                        startIntent(context, PACKAGE_SAMSUNG_MAIN, PACKAGE_SAMSUNG_COMPONENT_3);
                    } catch (Exception exx) {
                        exx.printStackTrace();
                        return false;
                    }
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean autoStartOnePlus(Context context) {
        boolean ok;
        if (isPackageExists(context, PACKAGE_ONE_PLUS_MAIN)) {
            try {
                startIntent(context, PACKAGE_ONE_PLUS_MAIN, PACKAGE_ONE_PLUS_COMPONENT);
                ok = true;
            } catch (Exception e) {
                e.printStackTrace();
                ok = false;
            }
        } else {
            ok = false;
        }

        if (!ok) {
            try {
                startAction(context, PACKAGE_ONE_PLUS_ACTION);
            } catch (Exception e) {
                e.printStackTrace();
                ok = false;
            }
        }

        return ok;
    }

    private void startIntent(Context context, String packageName, String componentName) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, componentName));
            context.startActivity(intent);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw exception;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void startAction(Context context, String action) {
        try {
            Intent intent = new Intent();
            intent.setAction(action);
            context.startActivity(intent);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw exception;
        }
    }

    private boolean isPackageExists(Context context, String targetPackage) {
        List<ApplicationInfo> packages;
        PackageManager pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(targetPackage)) {
                return true;
            }
        }
        return false;
    }

    static AutoStartPermissionHelper getInstance() {
        if (instance == null)
            instance = new AutoStartPermissionHelper();
        return instance;
    }

}
