package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GlobalData.logE("##### PackageReplacedReceiver.onReceive", "xxx");

        //int intentUid = intent.getExtras().getInt("android.intent.extra.UID");
        //int myUid = android.os.Process.myUid();
        //if (intentUid == myUid)
        //{
            //GlobalData.logE("@@@ PackageReplacedReceiver.onReceive", "####");

            GlobalData.setShowRequestAccessNotificationPolicyPermission(context.getApplicationContext(), true);
            GlobalData.setShowRequestWriteSettingsPermission(context.getApplicationContext(), true);
            GlobalData.setShowEnableLocationNotification(context.getApplicationContext(), true);

            GlobalData.logE("PackageReplacedReceiver.onReceive","PhoneProfilesService.instance="+PhoneProfilesService.instance);

            if (GlobalData.getApplicationStarted(context))
            {
                GlobalData.logE("@@@ PackageReplacedReceiver.onReceive", "start PhoneProfilesService");

                if (PhoneProfilesService.instance != null) {
                    // stop PhoneProfilesService
                    context.stopService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));
                    GlobalData.sleep(2000);
                }

                // must by false for avoiding starts/pause events before restart events
                GlobalData.setApplicationStarted(context, false);

                // start PhoneProfilesService
                context.startService(new Intent(context.getApplicationContext(), PhoneProfilesService.class));

            }
        //}
    }

}
