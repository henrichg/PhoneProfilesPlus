package sk.henrichg.phoneprofilesplus;

import android.app.backup.BackupAgentHelper;

public class PhoneProfilesBackupAgent extends BackupAgentHelper {

    @Override
    public void onCreate() {
        PPApplication.logE("PhoneProfilesBackupAgent","onCreate");
    }

    @Override
    public void onRestoreFinished() {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("PhoneProfilesBackupAgent","onRestoreFinished");

        // NEZAVRIE APLIKACIU PO RESTORE.

        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);

        PPApplication.loadPreferences(getApplicationContext());

        EditorProfilesActivity.exitApp(getApplicationContext(), dataWrapper, null);

        ActivateProfileActivity activateProfileActivity = ActivateProfileActivity.getInstance();
        if (activateProfileActivity != null)
        {
            PPApplication.logE("PhoneProfilesBackupAgent","close ActivateProfileActivity");
            activateProfileActivity.finish();
        }

        EditorProfilesActivity editorProfilesActivity = EditorProfilesActivity.getInstance();
        if (editorProfilesActivity != null)
        {
            PPApplication.logE("PhoneProfilesBackupAgent","close EditorProfilesActivity");
            editorProfilesActivity.finish();
        }

        PPApplication.setShowRequestAccessNotificationPolicyPermission(getApplicationContext(), true);
        PPApplication.setShowRequestWriteSettingsPermission(getApplicationContext(), true);
        PPApplication.setShowEnableLocationNotification(getApplicationContext(), true);
        PPApplication.setScreenUnlocked(getApplicationContext(), true);
        PPApplication.setMergedRingNotificationVolumes(getApplicationContext(), true);
    }


}
