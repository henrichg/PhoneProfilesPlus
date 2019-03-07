package sk.henrichg.phoneprofilesplus;

import android.os.ServiceManager;

import com.android.internal.telephony.ITelephony;

@SuppressWarnings("WeakerAccess")
public class CmdMobileData {

    public static void main(String[] args) {
        int subId= Integer.parseInt(args[0]);
        boolean enable = Boolean.parseBoolean(args[1]);
        if (!(run(subId, enable))) {
            System.exit(1);
        }
    }

    private static boolean run(int subId, boolean enable) {
        try {
            ITelephony adapter = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
            //adapter.getDataEnabled(subId); // hm podpora pre dual sim
            adapter.setUserDataEnabled(subId, enable); // hm podpora pre dual sim
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

}
