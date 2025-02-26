package sk.henrichg.phoneprofilesplus;

// is from reginer android.jar
import android.nfc.INfcAdapter;
import android.os.Build;
// is from reginer android.jar
import android.os.ServiceManager;
import android.util.Log;

/**
 * A shell executable for NTC toggle.
 */

public class CmdNfc {

    public static void main(String[] args) {
        if (!(run(Boolean.parseBoolean(args[0])))) {
            System.exit(1);
        }
    }

    private static boolean run(boolean enable) {
        return setNFC(enable);
    }

    // requires android.permission.WRITE_SECURE_SETTINGS
    static boolean setNFC(boolean enable) {
        try {
            INfcAdapter adapter = INfcAdapter.Stub.asInterface(ServiceManager.getService("nfc"));
            if (Build.VERSION.SDK_INT >= 35) {
                return enable ? adapter.enable(PPApplication.PACKAGE_NAME) : adapter.disable(true, PPApplication.PACKAGE_NAME);
            } else {
                return enable ? ReflectUtils.reflect(adapter).method("enable").get() : ReflectUtils.reflect(adapter).method("disable", true).get();
            }
            //INfcAdapter adapter = INfcAdapter.Stub.asInterface(ServiceManager.getService("nfc")); // service list | grep INfcAdapter
            //return enable ? adapter.enable(/*PPApplication.PACKAGE_NAME*/) : adapter.disable(true/*, PPApplication.PACKAGE_NAME*/);
        } catch (Throwable e) {
            PPApplicationStatic.logException("CmdNfc.setNFC", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);
            return false;
        }
    }
    /*
    static void setNFC35(boolean enable) {
        if (ShizukuUtils.hasShizukuPermission()) {
            synchronized (PPApplication.rootMutex) {
                String command1 = "svc nfc " + (enable ? "enable" : "disable");
                try {
                    ShizukuUtils.executeCommand(command1);
                } catch (Exception e) {
                    //Log.e("ActivateProfileHelper.setMobileData", Log.getStackTraceString(e));
                }
            }
        } else {
            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) && RootUtils.isRooted()) {
//                            PPApplicationStatic.logE("[SYNCHRONIZED] ActivateProfileHelper.setMobileData", "PPApplication.rootMutex");
                synchronized (PPApplication.rootMutex) {
                    String command1 = "svc nfc " + (enable ? "enable" : "disable");
                    Command command = new Command(0, command1);
                    try {
                        RootTools.getShell(true, Shell.ShellContext.SHELL).add(command);
                        RootUtils.commandWait(command, RootCommandWaitCalledFromConstants.ROOT_COMMAND_WAIT_CALLED_FROM_SET_MOBILE_DATA);
                    } catch (Exception e) {
                        //Log.e("ActivateProfileHelper.setMobileData", Log.getStackTraceString(e));
                    }
                }
            }
        }
    }
    */

}
