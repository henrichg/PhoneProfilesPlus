package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Pair;

import com.stericson.rootshell.RootShell;
import com.stericson.rootshell.execution.Command;
import com.stericson.rootshell.execution.Shell;
import com.stericson.roottools.RootTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RootUtils {

    private RootUtils() {
        // private constructor to prevent instantiation
    }

    static synchronized void initRoot() {
//        PPApplicationStatic.logE("[SYNCHRONIZED] RootUtils.initRoot", "PPApplication.rootMutex");
        synchronized (PPApplication.rootMutex) {
            PPApplication.rootMutex.rootChecked = false;
            PPApplication.rootMutex.rooted = false;
            //PPApplication.rootMutex.grantRootChecked = false;
            //PPApplication.rootMutex.rootGranted = false;
            PPApplication.rootMutex.settingsBinaryChecked = false;
            PPApplication.rootMutex.settingsBinaryExists = false;
            //PPApplication.rootMutex.isSELinuxEnforcingChecked = false;
            //PPApplication.rootMutex.isSELinuxEnforcing = false;
            //PPApplication.rootMutex.suVersion = null;
            //PPApplication.rootMutex.suVersionChecked = false;
            PPApplication.rootMutex.serviceBinaryChecked = false;
            PPApplication.rootMutex.serviceBinaryExists = false;
        }
    }

    static boolean _isRooted()
    {
        RootShell.debugMode = PPApplication.rootToolsDebug;

        if (PPApplication.rootMutex.rootChecked) {
            try {
                PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, String.valueOf(PPApplication.rootMutex.rooted));
                if (PPApplication.rootMutex.rooted) {
                    PackageManager packageManager = PPApplication.getInstance().getPackageManager();
                    // SuperSU
                    Intent intent = packageManager.getLaunchIntentForPackage("eu.chainfire.supersu");
                    if (intent != null)
                        PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "SuperSU");
                    else {
                        intent = packageManager.getLaunchIntentForPackage("com.topjohnwu.magisk");
                        if (intent != null)
                            PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "Magisk");
                        else
                            PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "another manager");
                    }
                }
            } catch (Exception e) {
                // https://github.com/firebase/firebase-android-sdk/issues/1226
                //PPApplicationStatic.recordException(e);
            }
            return PPApplication.rootMutex.rooted;
        }

        try {
            //if (roottools.isRootAvailable()) {
            if (RootToolsSmall.isRooted()) {
                // device is rooted
                PPApplication.rootMutex.rooted = true;
            } else {
                PPApplication.rootMutex.rooted = false;
                //rootMutex.settingsBinaryExists = false;
                //rootMutex.settingsBinaryChecked = false;
                //rootMutex.isSELinuxEnforcingChecked = false;
                //rootMutex.isSELinuxEnforcing = false;
                //rootMutex.suVersionChecked = false;
                //rootMutex.suVersion = null;
                //rootMutex.serviceBinaryExists = false;
                //rootMutex.serviceBinaryChecked = false;
            }
            PPApplication.rootMutex.rootChecked = true;
            try {
                PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED, String.valueOf(PPApplication.rootMutex.rooted));
                if (PPApplication.rootMutex.rooted) {
                    PackageManager packageManager = PPApplication.getInstance().getPackageManager();
                    // SuperSU
                    Intent intent = packageManager.getLaunchIntentForPackage("eu.chainfire.supersu");
                    if (intent != null)
                        PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "SuperSU");
                    else {
                        intent = packageManager.getLaunchIntentForPackage("com.topjohnwu.magisk");
                        if (intent != null)
                            PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "Magisk");
                        else
                            PPApplicationStatic.setCustomKey(PPApplication.CRASHLYTICS_LOG_DEVICE_ROOTED_WITH, "another manager");
                    }
                }
            } catch (Exception e) {
                // https://github.com/firebase/firebase-android-sdk/issues/1226
                //PPApplicationStatic.recordException(e);
            }
        } catch (Exception e) {
            //Log.e("RootUtils._isRooted", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
        }
        //if (rooted)
        //	getSUVersion();
        return PPApplication.rootMutex.rooted;
    }

    static boolean isRooted(/*boolean fromUIThread*/) {
        if (PPApplication.rootMutex.rootChecked)
            return PPApplication.rootMutex.rooted;

        //if (fromUIThread)
        //    return false;

//        PPApplicationStatic.logE("[SYNCHRONIZED] RootUtils.isRooted", "PPApplication.rootMutex");
        synchronized (PPApplication.rootMutex) {
            return _isRooted();
        }
    }

    static void isRootGranted(/*boolean onlyCheck*/)
    {
        RootShell.debugMode = PPApplication.rootToolsDebug;

        /*if (onlyCheck && rootMutex.grantRootChecked)
            return rootMutex.rootGranted;*/

        if (isRooted(/*false*/)) {
//            PPApplicationStatic.logE("[SYNCHRONIZED] RootUtils.isRootGranted", "PPApplication.rootMutex");
            synchronized (PPApplication.rootMutex) {
                try {
                    //noinspection StatementWithEmptyBody
                    if (RootTools.isAccessGiven()) {
                        // root is granted
                        //rootMutex.rootGranted = true;
                        //rootMutex.grantRootChecked = true;
                    }/* else {
                        // grant denied
                        //rootMutex.rootGranted = false;
                        //rootMutex.grantRootChecked = true;
                    }*/
                } catch (Exception e) {
                    //Log.e("RootUtils.isRootGranted", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                    //rootMutex.rootGranted = false;
                }
                //return rootMutex.rootGranted;
            }
        }
        //return false;
    }

    static boolean settingsBinaryExists(boolean fromUIThread)
    {
        if (ShizukuUtils.hasShizukuPermission()) {
            if (PPApplication.rootMutex.settingsBinaryChecked)
                return PPApplication.rootMutex.settingsBinaryExists;

            if (fromUIThread)
                return false;

            synchronized (PPApplication.rootMutex) {
                if (!PPApplication.rootMutex.settingsBinaryChecked) {
                    PPApplication.rootMutex.settingsBinaryExists = ShizukuUtils.hasSettingBin();
                    PPApplication.rootMutex.settingsBinaryChecked = true;
                }
                return PPApplication.rootMutex.settingsBinaryExists;
            }
        } else {
            RootShell.debugMode = PPApplication.rootToolsDebug;

            if (PPApplication.rootMutex.settingsBinaryChecked)
                return PPApplication.rootMutex.settingsBinaryExists;

            if (fromUIThread)
                return false;

//        PPApplicationStatic.logE("[SYNCHRONIZED] RootUtils.settingsBinaryExists", "PPApplication.rootMutex");
            synchronized (PPApplication.rootMutex) {
                if (RootUtils.isRooted()) {
                    if (!PPApplication.rootMutex.settingsBinaryChecked) {
                        PPApplication.rootMutex.settingsBinaryExists = RootToolsSmall.hasSettingBin();
                        PPApplication.rootMutex.settingsBinaryChecked = true;
                    }
                    return PPApplication.rootMutex.settingsBinaryExists;
                } else
                    return false;
            }
        }
    }

    static boolean serviceBinaryExists(boolean fromUIThread)
    {
        if (ShizukuUtils.hasShizukuPermission()) {
            if (PPApplication.rootMutex.serviceBinaryChecked)
                return PPApplication.rootMutex.serviceBinaryExists;

            if (fromUIThread)
                return false;

            synchronized (PPApplication.rootMutex) {
                if (!PPApplication.rootMutex.serviceBinaryChecked) {
                    PPApplication.rootMutex.serviceBinaryExists = ShizukuUtils.hasServiceBin();
                    PPApplication.rootMutex.serviceBinaryChecked = true;
                }
                return PPApplication.rootMutex.serviceBinaryExists;
            }
        } else {
            RootShell.debugMode = PPApplication.rootToolsDebug;

            if (PPApplication.rootMutex.serviceBinaryChecked)
                return PPApplication.rootMutex.serviceBinaryExists;

            if (fromUIThread)
                return false;

//        PPApplicationStatic.logE("[SYNCHRONIZED] RootUtils.serviceBinaryExists", "PPApplication.rootMutex");
            synchronized (PPApplication.rootMutex) {
                if (RootUtils.isRooted()) {
                    if (!PPApplication.rootMutex.serviceBinaryChecked) {
                        PPApplication.rootMutex.serviceBinaryExists = RootToolsSmall.hasServiceBin();
                        PPApplication.rootMutex.serviceBinaryChecked = true;
                    }
                    return PPApplication.rootMutex.serviceBinaryExists;
                } else
                    return false;
            }
        }
    }

    /**
     * Detect if SELinux is set to enforcing, caches result
     *
     * @return true if SELinux set to enforcing, or false in the case of
     *         permissive or not present
     * @noinspection MismatchedJavadocCode, BlockingMethodInNonBlockingContext
     */
    /*public static boolean isSELinuxEnforcing()
    {
        rootshell.debugMode = rootToolsDebug;

        synchronized (PPApplication.rootMutex) {
            if (!isSELinuxEnforcingChecked)
            {
                boolean enforcing = false;

                // First known firmware with SELinux built-in was a 4.2 (17)
                // leak
                // Detect enforcing through sysfs, not always present
                File f = new File("/sys/fs/selinux/enforce");
                if (f.exists()) {
                    try {
                        InputStream is = new FileInputStream("/sys/fs/selinux/enforce");
                        try {
                            enforcing = (is.read() == '1');
                        } finally {
                            is.close();
                        }
                    } catch (Exception ignored) {
                    }
                }

                isSELinuxEnforcing = enforcing;
                isSELinuxEnforcingChecked = true;
            }

            return isSELinuxEnforcing;
        }
    }*/

    /*
    public static String getSELinuxEnforceCommand(String command, Shell.ShellContext context)
    {
        if ((suVersion != null) && suVersion.contains("SUPERSU"))
            return "su --context " + context.getValue() + " -c \"" + command + "\"  < /dev/null";
        else
            return command;
    }

    public static String getSUVersion()
    {
        if (!suVersionChecked)
        {
            Command command = new Command(0, false, "su -v")
            {
                @Override
                public void commandOutput(int id, String line) {
                    suVersion = line;

                    super.commandOutput(id, line);
                }
            }
            ;
            try {
                roottools.getShell(false).add(command);
                commandWait(command);
                suVersionChecked = true;
            } catch (Exception e) {
                Log.e("RootUtils.getSUVersion", Log.getStackTraceString(e));
            }
        }
        return suVersion;
    }
    */

    public static String getJavaCommandFile(Class<?> mainClass, String fileName, Context context, Object cmdParam) {
        try {
            String cmd =
                    "#!/system/bin/sh\n" +
                            "base=/system\n" +
                            "export CLASSPATH=" + context.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0).applicationInfo.sourceDir + "\n" +
                            "exec app_process $base/bin " + mainClass.getName() + " " + cmdParam + " \"$@\"\n";

            /*String dir = context.getPackageManager().getApplicationInfo(context.PPApplication.PACKAGE_NAME, 0).dataDir;
            File fDir = new File(dir);
            File file = new File(fDir, name);
            OutputStream out = new FileOutputStream(file);
            out.write(cmd.getBytes());
            out.close();*/

            FileOutputStream fos = context.getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(cmd.getBytes());
            fos.close();

            File file = context.getFileStreamPath(fileName);
            if (!file.setExecutable(true))
                return null;

            return file.getAbsolutePath();

        } catch (Exception e) {
            return null;
        }
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    static void getServicesList() {
//        PPApplicationStatic.logE("[SYNCHRONIZED] RootUtils.getServicesList", "(1) PPApplication.serviceListMutex");
        synchronized (PPApplication.serviceListMutex) {
            if (PPApplication.serviceListMutex.serviceList == null)
                PPApplication.serviceListMutex.serviceList = new ArrayList<>();
            else
                PPApplication.serviceListMutex.serviceList.clear();
        }

        if (ShizukuUtils.hasShizukuPermission()) {
            synchronized (PPApplication.rootMutex) {
                try {
                    //noinspection RegExpRedundantEscape,RegExpSimplifiable
                    final Pattern compile = Pattern.compile("^[0-9]+\\s+([a-zA-Z0-9_\\-\\.]+): \\[(.*)\\]$");

                    String command1 = "service list";
                    Process process = ShizukuUtils.executeCommandNoWait(command1);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String readline;
                    while ((readline = reader.readLine()) != null) {
                        //Log.e("RootUtils.getServicesList", "line="+readline);

                        Matcher matcher = compile.matcher(readline);
                        if (matcher.find()) {
//                            PPApplicationStatic.logE("[SYNCHRONIZED] RootUtils.getServicesList", "(3) PPApplication.serviceListMutex");
                            synchronized (PPApplication.serviceListMutex) {
                                //serviceListMutex.serviceList.add(new Pair(matcher.group(1), matcher.group(2)));
                                Pair<String, String> pair = Pair.create(matcher.group(1), matcher.group(2));
                                if (pair.first.equals(RootMutex.SERVICE_PHONE) ||
                                        pair.first.equals(RootMutex.SERVICE_WIFI) ||
                                        pair.first.equals(RootMutex.SERVICE_ISUB)) {
//                                Log.e("RootUtils.getServicesList", "line="+readline);
//                                Log.e("RootUtils.getServicesList", "pair.first="+pair.first);
//                                Log.e("RootUtils.getServicesList", "pair.second="+pair.second);
                                    PPApplication.serviceListMutex.serviceList.add(pair);
                                }
                            }
                        }
                    }

                    fillTransactionCodes();
                } catch (Exception e) {
                    //Log.e("RootUtils.getServicesList", Log.getStackTraceString(e));
                }
                //Log.e("RootUtils.getServicesList", "**** end of service list");
            }
        } else
        if (isRooted(/*false*/)) {
//            PPApplicationStatic.logE("[SYNCHRONIZED] RootUtils.getServicesList", "(2) PPApplication.rootMutex");
            synchronized (PPApplication.rootMutex) {
                //noinspection RegExpRedundantEscape,RegExpSimplifiable
                final Pattern compile = Pattern.compile("^[0-9]+\\s+([a-zA-Z0-9_\\-\\.]+): \\[(.*)\\]$");

                Command command = new Command(0, /*false,*/ "service list") {
                    @Override
                    public void commandOutput(int id, String line) {
                        Matcher matcher = compile.matcher(line);
                        if (matcher.find()) {
//                            PPApplicationStatic.logE("[SYNCHRONIZED] RootUtils.getServicesList", "(3) PPApplication.serviceListMutex");
                            synchronized (PPApplication.serviceListMutex) {
                                //serviceListMutex.serviceList.add(new Pair(matcher.group(1), matcher.group(2)));
                                Pair<String, String> pair = Pair.create(matcher.group(1), matcher.group(2));
                                if (pair.first.equals(RootMutex.SERVICE_PHONE) ||
                                        pair.first.equals(RootMutex.SERVICE_WIFI) ||
                                        pair.first.equals(RootMutex.SERVICE_ISUB)) {
                                    PPApplication.serviceListMutex.serviceList.add(pair);
                                }
                            }
                        }
                        super.commandOutput(id, line);
                    }
                };

                try {
                    //roottools.getShell(false).add(command);
                    RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(command);
                    commandWait(command, "PPApplication.getServicesList");

                    fillTransactionCodes();
                } catch (Exception e) {
                    //Log.e("RootUtils.getServicesList", Log.getStackTraceString(e));
                }
            }
        }
    }

    private static void fillTransactionCodes() {
        PPApplication.rootMutex.serviceManagerPhone = getServiceManager(RootMutex.SERVICE_PHONE);
        PPApplication.rootMutex.serviceManagerWifi = getServiceManager(RootMutex.SERVICE_WIFI);
        PPApplication.rootMutex.serviceManagerIsub = getServiceManager(RootMutex.SERVICE_ISUB);

        /* this is not used, used is "svc data"
        PPApplication.rootMutex.transactionCode_setUserDataEnabled = -1;
        PPApplication.rootMutex.transactionCode_setDataEnabled = -1;
        if (PPApplication.rootMutex.serviceManagerPhone != null) {
            if (Build.VERSION.SDK_INT >= 28)
                PPApplication.rootMutex.transactionCode_setUserDataEnabled = getTransactionCode(String.valueOf(PPApplication.rootMutex.serviceManagerPhone), "setUserDataEnabled");
            else
                PPApplication.rootMutex.transactionCode_setDataEnabled = getTransactionCode(String.valueOf(PPApplication.rootMutex.serviceManagerPhone), "setDataEnabled");
            Log.e("RootUtils.fillTransactionCodes", "PPApplication.rootMutex.transactionCode_setUserDataEnabled="+PPApplication.rootMutex.transactionCode_setUserDataEnabled);
            Log.e("RootUtils.fillTransactionCodes", "PPApplication.rootMutex.transactionCode_setDataEnabled="+PPApplication.rootMutex.transactionCode_setDataEnabled);
        }
        */

        PPApplication.rootMutex.transactionCode_setPreferredNetworkType = -1;
        if (PPApplication.rootMutex.serviceManagerPhone != null) {
            PPApplication.rootMutex.transactionCode_setPreferredNetworkType = getTransactionCode(String.valueOf(PPApplication.rootMutex.serviceManagerPhone), "setPreferredNetworkType");
//            Log.e("RootUtils.fillTransactionCodes", "PPApplication.rootMutex.transactionCode_setPreferredNetworkType="+PPApplication.rootMutex.transactionCode_setPreferredNetworkType);
        }

        PPApplication.rootMutex.transactionCode_setDefaultVoiceSubId = -1;
        PPApplication.rootMutex.transactionCode_setDefaultSmsSubId = -1;
        PPApplication.rootMutex.transactionCode_setDefaultDataSubId = -1;
        if (PPApplication.rootMutex.serviceManagerIsub != null) {
            PPApplication.rootMutex.transactionCode_setDefaultVoiceSubId = getTransactionCode(String.valueOf(PPApplication.rootMutex.serviceManagerIsub), "setDefaultVoiceSubId");
            PPApplication.rootMutex.transactionCode_setDefaultSmsSubId = getTransactionCode(String.valueOf(PPApplication.rootMutex.serviceManagerIsub), "setDefaultSmsSubId");
            PPApplication.rootMutex.transactionCode_setDefaultDataSubId = getTransactionCode(String.valueOf(PPApplication.rootMutex.serviceManagerIsub), "setDefaultDataSubId");
//            Log.e("RootUtils.fillTransactionCodes", "PPApplication.rootMutex.transactionCode_setDefaultVoiceSubId="+PPApplication.rootMutex.transactionCode_setDefaultVoiceSubId);
//            Log.e("RootUtils.fillTransactionCodes", "PPApplication.rootMutex.transactionCode_setDefaultSmsSubId="+PPApplication.rootMutex.transactionCode_setDefaultSmsSubId);
//            Log.e("RootUtils.fillTransactionCodes", "PPApplication.rootMutex.transactionCode_setDefaultDataSubId="+PPApplication.rootMutex.transactionCode_setDefaultDataSubId);
        }

        PPApplication.rootMutex.transactionCode_setSubscriptionEnabled = -1;
        if (PPApplication.rootMutex.serviceManagerIsub != null) {
            PPApplication.rootMutex.transactionCode_setSubscriptionEnabled = getTransactionCode(String.valueOf(PPApplication.rootMutex.serviceManagerIsub), "setSubscriptionEnabled");
//            Log.e("RootUtils.fillTransactionCodes", "PPApplication.rootMutex.transactionCode_setSubscriptionEnabled="+PPApplication.rootMutex.transactionCode_setSubscriptionEnabled);
        }

        PPApplication.rootMutex.transactionCode_setSimPowerStateForSlot = -1;
        if (PPApplication.rootMutex.serviceManagerPhone != null)
            PPApplication.rootMutex.transactionCode_setSimPowerStateForSlot = getTransactionCode(String.valueOf(PPApplication.rootMutex.serviceManagerPhone), "setSimPowerStateForSlot");

        // used only in API < 28
        PPApplication.rootMutex.transactionCode_setWifiApEnabled = -1;
        if (PPApplication.rootMutex.serviceManagerWifi != null) {
            PPApplication.rootMutex.transactionCode_setWifiApEnabled = getTransactionCode(String.valueOf(PPApplication.rootMutex.serviceManagerWifi), "setWifiApEnabled");
//            Log.e("RootUtils.fillTransactionCodes", "PPApplication.rootMutex.transactionCode_setWifiApEnabled="+PPApplication.rootMutex.transactionCode_setWifiApEnabled);
        }
    }

    static Object getServiceManager(String serviceType) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] RootUtils.getServiceManager", "PPApplication.serviceListMutex");
        synchronized (PPApplication.serviceListMutex) {
            if (PPApplication.serviceListMutex.serviceList != null) {
                //noinspection rawtypes
                for (Pair pair : PPApplication.serviceListMutex.serviceList) {
                    if (serviceType.equals(pair.first)) {
                        return pair.second;
                    }
                }
            }
            return null;
        }
    }

    static int getTransactionCode(String serviceManager, String method) {
        if (method.isEmpty()) {
            Log.e("RootUtils.getTransactionCode", "serviceManager=" + serviceManager);
            Log.e("RootUtils.getTransactionCode", "method=" + method);
        }
        int code = -1;
        try {
            //noinspection rawtypes
            for (Class declaredFields : Class.forName(serviceManager).getDeclaredClasses()) {
                Field[] declaredFields2 = declaredFields.getDeclaredFields();
                int length = declaredFields2.length;
                int iField = 0;
                while (iField < length) {
                    Field field = declaredFields2[iField];
                    String name = field.getName();
//                    Log.e("RootUtils.getTransactionCode", "filed.name="+name);
                    if (method.isEmpty()) {
                        if (name.contains("TRANSACTION_"))
                            Log.e("RootUtils.getTransactionCode", "field.getName()="+name);
                        iField++;
                    }
                    else {
                        if (/*name == null ||*/ !name.equals("TRANSACTION_" + method)) {
                            iField++;
                        } else {
                            try {
                                field.setAccessible(true);
                                code = field.getInt(field);
//                                PPApplicationStatic.logE("[DUAL_SIM] RootUtils.getTransactionCode", "name="+name+",  code="+code);
                                break;
                            } catch (Exception e) {
                                //Log.e("RootUtils.getTransactionCode", Log.getStackTraceString(e));
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            //Log.e("RootUtils.getTransactionCode", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);
        }
        return code;
    }

    static String getServiceCommand(String serviceType, int transactionCode, Object... params) {
        if (params.length > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("service").append(" ").append("call").append(" ").append(serviceType).append(" ").append(transactionCode);
            for (Object param : params) {
                if (param != null) {
                    stringBuilder.append(" ");
                    if (param instanceof Integer) {
                        stringBuilder.append("i32").append(" ").append(param);
                    } else if (param instanceof String) {
                        stringBuilder.append("s16").append(" ").append("'").append(((String) param).replace("'", "'\\''")).append("'");
                    }
                }
            }
            return stringBuilder.toString();
        }
        else
            return null;
    }

    /** @noinspection BlockingMethodInNonBlockingContext*/
    static void commandWait(Command cmd, String calledFrom) /*throws Exception*/ {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 6400; // 12850 milliseconds (6400 * 2 - 50)
        // 1.              50
        // 2. 2 * 50 =    100
        // 3. 2 * 100 =   200
        // 4. 2 * 200 =   400
        // 5. 2 * 400 =   800
        // 6. 2 * 800 =  1600
        // 7. 2 * 1600 = 3200
        // 8. 2 * 3200 = 6400
        // ------------------
        //              12850

//        PPApplicationStatic.logE("[SYNCHRONIZED] RootUtils.commandWait", "cmd");
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (cmd) {
            while (!cmd.isFinished() && waitTill<=waitTillLimit) {
                try {
                    //if (!cmd.isFinished()) {
                    cmd.wait(waitTill);
                    waitTill *= waitTillMultiplier;
                    //}
                } catch (InterruptedException e) {
                    //Log.e("RootUtils.commandWait", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e("RootUtils.commandWait", "Called from: " + calledFrom + "; Could not finish root command in " + (waitTill/waitTillMultiplier));
            //PPApplicationStatic.logToACRA("E/GlobalUtils.commandWait: Called from: " + calledFrom + "; Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }

}
