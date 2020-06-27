package sk.henrichg.phoneprofilesplus;

import android.nfc.INfcAdapter;
import android.os.ServiceManager;

/**
 * A shell executable for NTC toggle.
 */

@SuppressWarnings("WeakerAccess")
public class CmdNfc {

    public static void main(String[] args) {
        //PPApplication.logE("CmdNfc.main", "args="+args);
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
            return enable ? adapter.enable() : adapter.disable(true);
        } catch (Throwable e) {
            //Log.e("CmdNfc.setNFC", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            return false;
        }
    }

}
