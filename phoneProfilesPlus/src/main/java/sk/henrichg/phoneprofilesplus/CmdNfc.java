package sk.henrichg.phoneprofilesplus;

import android.nfc.INfcAdapter;
import android.os.ServiceManager;
import android.util.Log;

import com.stericson.rootshell.execution.Command;
import com.stericson.rootshell.execution.Shell;
import com.stericson.roottools.RootTools;

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
            INfcAdapter adapter = INfcAdapter.Stub.asInterface(ServiceManager.getService("nfc")); // service list | grep INfcAdapter
            return enable ? adapter.enable(/*PPApplication.PACKAGE_NAME*/) : adapter.disable(true/*, PPApplication.PACKAGE_NAME*/);
        } catch (Throwable e) {
            PPApplicationStatic.logException("CmdNfc.setNFC", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);
            return false;
        }
    }

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
            if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) && RootUtils.isRooted(/*false*/)) {
//                            PPApplicationStatic.logE("[SYNCHRONIZED] ActivateProfileHelper.setMobileData", "PPApplication.rootMutex");
                synchronized (PPApplication.rootMutex) {
                    String command1 = "svc nfc " + (enable ? "enable" : "disable");
                    Command command = new Command(0, /*false,*/ command1);
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

}
