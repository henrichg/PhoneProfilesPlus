package sk.henrichg.phoneprofilesplus;

import android.app.backup.BackupAgentHelper;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.PowerManager;

public class PhoneProfilesBackupAgent extends BackupAgentHelper {

    @Override
    public void onCreate() {
        PPApplication.logE("PhoneProfilesBackupAgent","onCreate");
    }

    @Override
    public void onRestoreFinished() {
        PPApplication.logE("PhoneProfilesBackupAgent","onRestoreFinished");

        // Do NOT CLOSE APPLICATION AFTER RESTORE.

        final Context appContext = getApplicationContext();

        final DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

        Intent intent = new Intent(PPApplication.ACTION_FINISH_ACTIVITY);
        intent.putExtra(PPApplication.EXTRA_WHAT_FINISH, "activator");
        appContext.sendBroadcast(intent);
        intent = new Intent(PPApplication.ACTION_FINISH_ACTIVITY);
        intent.putExtra(PPApplication.EXTRA_WHAT_FINISH, "editor");
        appContext.sendBroadcast(intent);
        /*
        ActivateProfileActivity activateProfileActivity = ActivateProfileActivity.getInstance();
        if (activateProfileActivity != null)
        {
            PPApplication.logE("PhoneProfilesBackupAgent","close ActivateProfileActivity");
            try {
                activateProfileActivity.finish();
            } catch (Exception ignored) {};
        }

        EditorProfilesActivity editorProfilesActivity = EditorProfilesActivity.getInstance();
        if (editorProfilesActivity != null)
        {
            PPApplication.logE("PhoneProfilesBackupAgent","close EditorProfilesActivity");
            try {
                editorProfilesActivity.finish();
            } catch (Exception ignored) {};
        }
        */

        PPApplication.startHandlerThread("PhoneProfilesBackupAgent.onRestoreFinished");
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("PhoneProfilesBackupAgent.onRestoreFinished", "in handler");

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesBackupAgent_onRestoreFinished");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PhoneProfilesBackupAgent.onRestoreFinished");

                    PPApplication.logE("PPApplication.exitApp", "from PhoneProfilesBackupAgent.onRestoreFinished shutdown=false");
                    PPApplication.exitApp(false, appContext, dataWrapper, null, false/*, false, false*/);

                    // save version code
                    try {
                        PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
                        int actualVersionCode = PPApplication.getVersionCode(pInfo);
                        PPApplication.setSavedVersionCode(appContext, actualVersionCode);
                    } catch (Exception ignored) {
                    }

                    Permissions.setAllShowRequestPermissions(appContext, true);
                    IgnoreBatteryOptimizationNotification.setShowIgnoreBatteryOptimizationNotificationOnStart(appContext, true);
                    IgnoreBatteryOptimizationNotification.showNotification(appContext);

                    //WifiBluetoothScanner.setShowEnableLocationNotification(appContext, true, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
                    //WifiBluetoothScanner.setShowEnableLocationNotification(appContext, true, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
                    //PhoneStateScanner.setShowEnableLocationNotification(appContext, true);
                    //ActivateProfileHelper.setScreenUnlocked(appContext, true);
                    ActivateProfileHelper.setMergedRingNotificationVolumes(appContext, true);

                    PPApplication.logE("PPApplication.startHandlerThread", "END run - from=PhoneProfilesBackupAgent.onRestoreFinished");
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

}
