package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RefreshGUIBroadcastReceiver extends BroadcastReceiver {

    public static final String INTENT_REFRESH_GUI = "sk.henrichg.phoneprofilesplus.REFRESH_GUI";
    public static final String EXTRA_REFRESH_ICONS = "refresh_icons";

    @Override
    public void onReceive(Context context, Intent intent) {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        GlobalData.logE("##### RefreshGUIBroadcastReceiver.onReceive", "xxx");

        GlobalData.logE("$$$ RefreshGUIBroadcastReceiver","xxx");

        boolean refreshIcons = intent.getBooleanExtra(EXTRA_REFRESH_ICONS, false);

        ActivateProfileActivity activateProfileActivity = ActivateProfileActivity.getInstance();
        if (activateProfileActivity != null)
        {
            GlobalData.logE("$$$ RefreshGUIBroadcastReceiver","ActivateProfileActivity");
            activateProfileActivity.refreshGUI(refreshIcons);
        }

        EditorProfilesActivity editorProfilesActivity = EditorProfilesActivity.getInstance();
        if (editorProfilesActivity != null)
        {
            GlobalData.logE("$$$ RefreshGUIBroadcastReceiver","EditorProfilesActivity");
            editorProfilesActivity.refreshGUI(refreshIcons, true);
        }

    }

}
