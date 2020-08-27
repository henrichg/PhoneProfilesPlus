package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class DashClockBroadcastReceiver extends BroadcastReceiver {

    //public static final String INTENT_REFRESH_DASHCLOCK = PPApplication.PACKAGE_NAME + ".REFRESH_DASHCLOCK";

    //static final String EXTRA_REFRESH = "refresh";

    @Override
    public void onReceive(final Context context, Intent intent) {
        PPApplication.logE("[BROADCAST CALL] DashClockBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "DashClockBroadcastReceiver.onReceive", "DashClockBroadcastReceiver_onReceive");

        //final boolean refresh = (intent == null) || intent.getBooleanExtra(EXTRA_REFRESH, true);

        //final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadBroadcast(/*"DashClockBroadcastReceiver.onReceive"*/);
        final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=DashClockBroadcastReceiver.onReceive");

                PhoneProfilesDashClockExtension dashClockExtension = PhoneProfilesDashClockExtension.getInstance();
                if (dashClockExtension != null)
                {
                    /*
                    DataWrapper dataWrapper = new DataWrapper(appContext, false, 0,false);
                    Profile profile = dataWrapper.getActivatedProfile(false, false);

                    String pName;
                    if (profile != null)
                        pName = DataWrapper.getProfileNameWithManualIndicatorAsString(profile, true, "", true, false, false, dataWrapper);
                    else
                        pName = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);

                    if (!refresh) {
                        String pNameWidget = PPApplication.prefWidgetProfileName5;

                        if (!pNameWidget.isEmpty()) {
                            if (pName.equals(pNameWidget)) {
                                //PPApplication.logE("DashClockBroadcastReceiver.onReceive", "activated profile NOT changed");
                                return;
                            }
                        }
                    }

                    PPApplication.setWidgetProfileName(context, 5, pName);
                    */

                    dashClockExtension.updateExtension();
                }

                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=DashClockBroadcastReceiver.onReceive");
            }
        });

    }

}
