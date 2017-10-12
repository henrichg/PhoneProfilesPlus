package sk.henrichg.phoneprofilesplus;

import android.os.IPowerManager;
import android.os.ServiceManager;
import android.os.SystemClock;

class CmdGoToSleep {

    public static void main(String[] args) {
        if (!(run())) {
            System.exit(1);
        }
    }

    private static boolean run() {
        try {
            IPowerManager adapter = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
            adapter.goToSleep(SystemClock.uptimeMillis(), 0, 0);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

}
