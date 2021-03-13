package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Build;
import android.os.ServiceManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

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
            adapter.setUserDataEnabled(subId, enable); // hm... support for dual sim
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    static boolean isEnabled(Context context, int simCard) {
        try {
            boolean enabled = false;
            if (Permissions.checkPhone(context.getApplicationContext())) {
                boolean ok = false;
                ITelephony adapter = ITelephony.Stub.asInterface(ServiceManager.getService("phone")); // service list | grep ITelephony
                if (adapter != null) {
                    SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    //SubscriptionManager.from(context);
                    if (mSubscriptionManager != null) {
                        int defaultDataId = 0;
                        if (Build.VERSION.SDK_INT > 23)
                            defaultDataId = SubscriptionManager.getDefaultDataSubscriptionId();
                        PPApplication.logE("CmdMobileData.isEnabled", "defaultDataId=" + defaultDataId);

                        List<SubscriptionInfo> subscriptionList = null;
                        try {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                        } catch (SecurityException e) {
                            PPApplication.recordException(e);
                        }
                        if (subscriptionList != null) {
                            for (int i = 0; i < subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/ i++) {
                                // Get the active subscription ID for a given SIM card.
                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                if (subscriptionInfo != null) {
                                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                                    if ((simCard == 0) || (simCard == (slotIndex + 1))) {
                                        int subscriptionId = subscriptionInfo.getSubscriptionId();
                                        if (Build.VERSION.SDK_INT > 23)
                                            enabled = adapter.getDataEnabled(subscriptionId) && (subscriptionId == defaultDataId);
                                        else
                                            enabled = adapter.getDataEnabled(subscriptionId);
                                        PPApplication.logE("CmdMobileData.isEnabled", "subscriptionId=" + subscriptionId);
                                        PPApplication.logE("CmdMobileData.isEnabled", "simCard=" + simCard);
                                        PPApplication.logE("CmdMobileData.isEnabled", "slotIndex=" + (slotIndex + 1));
                                        PPApplication.logE("CmdMobileData.isEnabled", "enabled=" + enabled);
                                        ok = true;
                                    }
                                    if (ok)
                                        break;
                                }
                            }
                        }
                    }
                    if (!ok) {
                        enabled = adapter.getDataEnabled(1);
                    }
                }
            }
            return enabled;
        } catch (Throwable e) {
            //Log.e("CmdMobileData.isEnabled", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            return false;
        }
    }

}
