package sk.henrichg.phoneprofilesplus;

import android.net.IConnectivityManager;
import android.os.ServiceManager;

@SuppressWarnings("WeakerAccess")
public class CmdMobileData {

    public static void main(String[] args) {
        if (!(run())) {
            System.exit(1);
        }
    }

    private static boolean run() {
        try {
            IConnectivityManager adapter = IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity"));
            //adapter.;
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

}
