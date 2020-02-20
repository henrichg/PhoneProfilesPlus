package sk.henrichg.phoneprofilesplus;

import android.os.IPowerManager;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

@SuppressWarnings("WeakerAccess")
public class CmdGoToSleep {

    public static void main(String[] args) {
        if (!(run())) {
            System.exit(1);
        }
    }

    private static boolean run() {
        return doSleep();
    }

    // requires android.permission.DEVICE_POWER but 'pm grant package permission' not working :-(
    static boolean doSleep() {
        try {
            IPowerManager adapter = IPowerManager.Stub.asInterface(ServiceManager.getService("power")); // service list | grep IPowerManager
            adapter.goToSleep(SystemClock.uptimeMillis(), 0, 0);
            return true;
        } catch (Throwable e) {
            Log.e("CmdGoToSleep.doSleep", Log.getStackTraceString(e));
            //FirebaseCrashlytics.getInstance().recordException(e);
            Crashlytics.logException(e);
            return false;
        }
    }

}
