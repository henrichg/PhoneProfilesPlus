package com.thelittlefireman.appkillermanager.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.os.UserManager;
import androidx.annotation.RequiresApi;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemUtils {

    public static String getDefaultDebugInformation(){
        return "Display_id:" + Build.DISPLAY +
                " MODEL:" + Build.MODEL +
                " MANUFACTURER:" + Build.MANUFACTURER +
                " PRODUCT:" + Build.PRODUCT;
    }

    public static String getEmuiRomName() {
        try {
            return SystemUtils.getSystemProperty("ro.build.version.emui");
        } catch (Exception e) {
            return "";
        }
    }
    public static String getApplicationName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException ignored) {}
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    @SuppressWarnings("unused")
    public static String getMiuiRomName() {
        try {
            return SystemUtils.getSystemProperty("ro.miui.ui.version.name");
        } catch (Exception e) {
            return "";
        }
    }

    private static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            //noinspection ClassGetClass
            Log.e(SystemUtils.class.getClass().getName(), "Unable to read system property " + propName, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    //noinspection ClassGetClass
                    Log.e(SystemUtils.class.getClass().getName(), "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

    // INFO http://imsardine.simplbug.com/note/android/adb/commands/am-start.html
    /**
     * Open an Activity by using Application Manager System (prevent from crash permission exception)
     *
     * @param context current application Context
     * @param packageName  pacakge name of the target application (exemple: com.huawei.systemmanager)
     * @param activityPackage activity name of the target application (exemple: .optimize.process.ProtectActivity)
     */
    @SuppressWarnings("unused")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void startActivityByAMSystem(Context context, String packageName, String activityPackage)
            throws IOException {
        String cmd = "am start -n "+packageName+"/"+activityPackage;
        UserManager um = (UserManager)context.getSystemService(Context.USER_SERVICE);
        if (um != null) {
            cmd += " --user " + um.getSerialNumberForUser(Process.myUserHandle());
            Runtime.getRuntime().exec(cmd);
        }
    }
    /**
     * Open an Action by using Application Manager System (prevent from crash permission exception)
     *
     * @param context current application Context
     * @param intentAction  action of the target application (exemple: com.huawei.systemmanager)
     */
    @SuppressWarnings("unused")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void startActionByAMSystem(Context context, String intentAction)
            throws IOException {
        String cmd = "am start -a "+intentAction;
        UserManager um = (UserManager)context.getSystemService(Context.USER_SERVICE);
        if (um != null) {
            cmd += " --user " + um.getSerialNumberForUser(Process.myUserHandle());
            Runtime.getRuntime().exec(cmd);
        }
    }
}
