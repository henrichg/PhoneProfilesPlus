package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.backup.BackupAgentHelper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class PhoneProfilesBackupAgent extends BackupAgentHelper {

    @Override
    public void onCreate() {
        GlobalData.logE("PhoneProfilesBackupAgent","onCreate");
    }

    @Override
    public void onRestoreFinished() {
        GlobalData.logE("PhoneProfilesBackupAgent","onRestoreFinished");

        // NEZAVRIE APLIKACIU PO RESTORE.

        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);

        GlobalData.loadPreferences(getApplicationContext());

        EditorProfilesActivity.exitApp(getApplicationContext(), dataWrapper);

        ActivateProfileActivity activateProfileActivity = ActivateProfileActivity.getInstance();
        if (activateProfileActivity != null)
        {
            GlobalData.logE("PhoneProfilesBackupAgent","close ActivateProfileActivity");
            activateProfileActivity.finish();
        }

        EditorProfilesActivity editorProfilesActivity = EditorProfilesActivity.getInstance();
        if (editorProfilesActivity != null)
        {
            GlobalData.logE("PhoneProfilesBackupAgent","close EditorProfilesActivity");
            editorProfilesActivity.finish();
        }

    }


}
