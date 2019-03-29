package sk.henrichg.phoneprofilesplus;

import android.app.backup.BackupAgentHelper;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

        Intent intent = new Intent("FinishActivatorBroadcastReceiver");
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
        intent = new Intent("FinishEditorBroadcastReceiver");
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
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

                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesBackupAgent_onRestoreFinished");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("PPApplication.startHandlerThread", "START run - from=PhoneProfilesBackupAgent.onRestoreFinished");

                    PPApplication.exitApp(false, appContext, dataWrapper, null, false/*, false, false*/);

                    PPApplication.setSavedVersionCode(appContext, 0);

                    Permissions.setAllShowRequestPermissions(appContext, true);

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
