package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Build;
import android.os.ServiceManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.util.List;

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
            ITelephony adapter = ITelephony.Stub.asInterface(ServiceManager.getService("phone")); // service list | grep ITelephony
            adapter.setUserDataEnabled(subId, enable); // hm podpora pre dual sim
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    static boolean isEnabled(Context context) {
        try {
            boolean enabled = false;
            boolean ok = false;
            ITelephony adapter = ITelephony.Stub.asInterface(ServiceManager.getService("phone")); // service list | grep ITelephony
            if (Build.VERSION.SDK_INT >= 22) {
                SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                //SubscriptionManager.from(context);
                if (mSubscriptionManager != null) {
                    List<SubscriptionInfo> subscriptionList = null;
                    try {
                        // Loop through the subscription list i.e. SIM list.
                        subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                    } catch (SecurityException ignored) {
                    }
                    if (subscriptionList != null) {
                        for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                            // Get the active subscription ID for a given SIM card.
                            SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                            if (subscriptionInfo != null) {
                                int subscriptionId = subscriptionInfo.getSubscriptionId();
                                enabled = adapter.getDataEnabled(subscriptionId);
                                ok = true;
                                PPApplication.logE("CmdMobileData.isEnabled", "subscriptionId="+subscriptionId);
                                PPApplication.logE("CmdMobileData.isEnabled", "enabled="+enabled);
                                if (enabled)
                                    break;
                            }
                        }
                    }
                }
            }
            if (!ok) {
                enabled = adapter.getDataEnabled(1);
                PPApplication.logE("CmdMobileData.isEnabled", "subscriptionId=0");
                PPApplication.logE("CmdMobileData.isEnabled", "enabled="+enabled);
            }
            return enabled;
        } catch (Throwable e) {
            PPApplication.logE("CmdMobileData.isEnabled", Log.getStackTraceString(e));
            return false;
        }
    }

}
