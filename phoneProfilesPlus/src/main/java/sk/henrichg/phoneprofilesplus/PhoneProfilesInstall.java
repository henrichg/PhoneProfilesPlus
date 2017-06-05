package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PhoneProfilesInstall extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName;
        if (intent.getData() != null)
            packageName = intent.getData().getEncodedSchemeSpecificPart();
        else
            packageName = "";
        PPApplication.logE("PhoneProfilesInstall.onReceive","packageName="+packageName);

        if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
            try {
                PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                if (packageName.equals(pinfo.packageName)) {
                    Permissions.setShowRequestAccessNotificationPolicyPermission(context.getApplicationContext(), true);
                    Permissions.setShowRequestWriteSettingsPermission(context.getApplicationContext(), true);
                    ScannerService.setShowEnableLocationNotification(context.getApplicationContext(), true);
                    //ActivateProfileHelper.setScreenUnlocked(context.getApplicationContext(), true);
                }
            } catch (PackageManager.NameNotFoundException e) {
                //e.printStackTrace();
            }
        }
    }


}
