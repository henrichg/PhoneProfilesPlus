package sk.henrichg.phoneprofilesplus;

import android.bluetooth.IBluetoothManager;
import android.os.ServiceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

@SuppressWarnings({"WeakerAccess", "unused"})
public class CmdBluetooth {

    public static void main(String[] args) {
        if (!(run(Boolean.parseBoolean(args[0])))) {
            System.exit(1);
        }
    }

    private static boolean run(boolean enable) {
        return setBluetooth(enable);
    }

    static boolean setBluetooth(boolean enable) {
        final String packageName = PPApplication.PACKAGE_NAME;
        try {
            //PPApplication.logE("CmdBluetooth.setBluetooth", "enable="+enable);
            IBluetoothManager bluetoothAdapter = IBluetoothManager.Stub.asInterface(ServiceManager.getService("bluetooth_manager"));  // service list | grep IWifiManager
            if (enable)
                bluetoothAdapter.enable(packageName);
            else
                bluetoothAdapter.disable(packageName, true);
            return true;
        } catch (Throwable e) {
            Log.e("CmdBluetooth.setBluetooth", Log.getStackTraceString(e));
            //FirebaseCrashlytics.getInstance().recordException(e);
            Crashlytics.logException(e);
            return false;
        }
    }

    static boolean isEnabled() {
        try {
            boolean enabled;
            IBluetoothManager bluetoothAdapter = IBluetoothManager.Stub.asInterface(ServiceManager.getService("bluetooth_manager"));  // service list | grep IWifiManager
            enabled = bluetoothAdapter.isEnabled();
            //PPApplication.logE("CmdBluetooth.isEnabled", "enabled="+enabled);
            return enabled;
        } catch (Throwable e) {
            Log.e("CmdBluetooth.isEnabled", Log.getStackTraceString(e));
            //FirebaseCrashlytics.getInstance().recordException(e);
            Crashlytics.logException(e);
            return false;
        }
    }

}
