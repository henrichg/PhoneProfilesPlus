package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.backup.BackupAgentHelper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by henrisko on 9.6.2016.
 */
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

        dataWrapper.invalidateProfileList();
        dataWrapper.invalidateEventList();

        dataWrapper.updateNotificationAndWidgets(null/*, ""*/);
        //dataWrapper.getActivateProfileHelper().showNotification(null, "");
        //dataWrapper.getActivateProfileHelper().updateWidget();

        GlobalData.setEventsBlocked(getApplicationContext(), false);
        dataWrapper.getDatabaseHandler().unblockAllEvents();
        GlobalData.setForceRunEventRunning(getApplicationContext(), false);

        SharedPreferences preferences = getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(EditorProfilesActivity.SP_EDITOR_DRAWER_SELECTED_ITEM, 1);
        editor.putInt(EditorProfilesActivity.SP_EDITOR_ORDER_SELECTED_ITEM, 0);
        editor.commit();

        // restart events
        // startneme eventy
        if (GlobalData.getGlobalEventsRuning(getApplicationContext())) {
                        /*
                        Intent intent = new Intent();
                        intent.setAction(RestartEventsBroadcastReceiver.INTENT_RESTART_EVENTS);
                        getBaseContext().sendBroadcast(intent);
                        */
            dataWrapper.restartEventsWithDelay(1, false);
        }

        //dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_DATAIMPORT, null, null, null, 0);

        // refresh activity
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
