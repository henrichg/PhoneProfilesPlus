package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GlobalData.logE("##### PackageReplacedReceiver.onReceive", "xxx");

        //int intentUid = intent.getExtras().getInt("android.intent.extra.UID");
        //int myUid = android.os.Process.myUid();
        //if (intentUid == myUid)
        //{

            GlobalData.setShowRequestAccessNotificationPolicyPermission(context.getApplicationContext(), true);
            GlobalData.setShowRequestWriteSettingsPermission(context.getApplicationContext(), true);
            GlobalData.setShowEnableLocationNotification(context.getApplicationContext(), true);
            GlobalData.setScreenUnlocked(context.getApplicationContext(), true);

            int oldVersionCode = GlobalData.getSavedVersionCode(context.getApplicationContext());
            GlobalData.logE("@@@ PackageReplacedReceiver.onReceive", "oldVersionCode="+oldVersionCode);
            int actualVersionCode;
            try {
                PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                actualVersionCode = pinfo.versionCode;
                GlobalData.logE("@@@ PackageReplacedReceiver.onReceive", "actualVersionCode=" + actualVersionCode);

                if (oldVersionCode < actualVersionCode) {
                    if (actualVersionCode <= 2302) {
                        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        // for old packages use Priority in events
                        GlobalData.logE("@@@ PackageReplacedReceiver.onReceive", "applicationEventUsePriority=true");
                        editor.putBoolean(GlobalData.PREF_APPLICATION_EVENT_USE_PRIORITY, true);
                        editor.commit();

                        GlobalData.loadPreferences(context);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                //e.printStackTrace();
            }

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
