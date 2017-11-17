package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class RefreshGUIBroadcastReceiver extends BroadcastReceiver {

    private static final String EXTRA_REFRESH_ICONS = "refresh_icons";
    public static final String EXTRA_REFRESH_ALSO_EDITOR = "refresh_also_editor";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### RefreshGUIBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "RefreshGUIBroadcastReceiver.onReceive", "RefreshGUIBroadcastReceiver_onReceive");

        LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(PPApplication.refreshGUIBroadcastReceiver);

        boolean refreshIcons = intent.getBooleanExtra(EXTRA_REFRESH_ICONS, false);
        boolean refreshAlsoEditor = intent.getBooleanExtra(EXTRA_REFRESH_ALSO_EDITOR, true);

        ActivateProfileActivity activateProfileActivity = ActivateProfileActivity.getInstance();
        if (activateProfileActivity != null)
        {
            PPApplication.logE("$$$ RefreshGUIBroadcastReceiver","ActivateProfileActivity");
            activateProfileActivity.refreshGUI(refreshIcons);
        }

        if (refreshAlsoEditor) {
            EditorProfilesActivity editorProfilesActivity = EditorProfilesActivity.getInstance();
            if (editorProfilesActivity != null) {
                PPApplication.logE("$$$ RefreshGUIBroadcastReceiver", "EditorProfilesActivity");
                // not change selection in editor if refresh is outside editor
                editorProfilesActivity.refreshGUI(refreshIcons, false);
            }
        }

    }

}
