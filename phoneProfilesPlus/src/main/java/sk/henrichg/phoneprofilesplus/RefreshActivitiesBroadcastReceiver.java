package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class RefreshActivitiesBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_REFRESH_ICONS = "refresh_icons";
    static final String EXTRA_REFRESH_ALSO_EDITOR = "refresh_also_editor";
    static final String EXTRA_REFRESH = "refresh";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### RefreshActivitiesBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "RefreshActivitiesBroadcastReceiver.onReceive", "RefreshGUIBroadcastReceiver_onReceive");

        boolean refresh = intent.getBooleanExtra(EXTRA_REFRESH, true);
        boolean refreshIcons = intent.getBooleanExtra(EXTRA_REFRESH_ICONS, false);
        boolean refreshAlsoEditor = intent.getBooleanExtra(EXTRA_REFRESH_ALSO_EDITOR, true);

        Intent refreshIntent = new Intent("RefreshActivatorGUIBroadcastReceiver");
        refreshIntent.putExtra(EXTRA_REFRESH, refresh);
        refreshIntent.putExtra(EXTRA_REFRESH_ICONS, refreshIcons);
        LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
        /*
        ActivateProfileActivity activateProfileActivity = ActivateProfileActivity.getInstance();
        if (activateProfileActivity != null)
        {
            PPApplication.logE("$$$ RefreshActivitiesBroadcastReceiver","ActivateProfileActivity");
            activateProfileActivity.refreshGUI(refreshIcons);
        }
        */

        PPApplication.logE("$$$ RefreshActivitiesBroadcastReceiver", "refreshAlsoEditor="+refreshAlsoEditor);

        if (refreshAlsoEditor) {
            long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            long eventId = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);

            refreshIntent = new Intent("RefreshEditorGUIBroadcastReceiver");
            refreshIntent.putExtra(EXTRA_REFRESH, refresh);
            refreshIntent.putExtra(EXTRA_REFRESH_ICONS, refreshIcons);
            refreshIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profileId);
            refreshIntent.putExtra(PPApplication.EXTRA_EVENT_ID, eventId);
            LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
            /*EditorProfilesActivity editorProfilesActivity = EditorProfilesActivity.getInstance();
            PPApplication.logE("$$$ RefreshActivitiesBroadcastReceiver", "editorProfilesActivity="+editorProfilesActivity);
            if (editorProfilesActivity != null) {
                PPApplication.logE("$$$ RefreshActivitiesBroadcastReceiver", "EditorProfilesActivity");
                // not change selection in editor if refresh is outside editor
                editorProfilesActivity.refreshGUI(refreshIcons, false);
            }
            */
        }

    }

}
