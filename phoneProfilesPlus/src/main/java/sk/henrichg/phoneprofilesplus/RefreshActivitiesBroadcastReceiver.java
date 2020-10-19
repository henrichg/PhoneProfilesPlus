package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class RefreshActivitiesBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_REFRESH_ICONS = "refresh_icons";
    static final String EXTRA_REFRESH_ALSO_EDITOR = "refresh_also_editor";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("[IN_BROADCAST] RefreshActivitiesBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "RefreshActivitiesBroadcastReceiver.onReceive", "RefreshGUIBroadcastReceiver_onReceive");

        boolean refreshIcons = intent.getBooleanExtra(EXTRA_REFRESH_ICONS, false);
        boolean refreshAlsoEditor = intent.getBooleanExtra(EXTRA_REFRESH_ALSO_EDITOR, true);
        //PPApplication.logE("##### RefreshActivitiesBroadcastReceiver.onReceive", "refreshIcons="+refreshIcons);
        //PPApplication.logE("##### RefreshActivitiesBroadcastReceiver.onReceive", "refreshAlsoEditor="+refreshAlsoEditor);

        PPApplication.logE("[LOCAL_BROADCAST_CALL] RefreshActivatorGUIBroadcastReceiver.onReceive", "(1)");
        Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivatorGUIBroadcastReceiver");
        refreshIntent.putExtra(EXTRA_REFRESH_ICONS, refreshIcons);
        LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);

        if (refreshAlsoEditor) {
            PPApplication.logE("[LOCAL_BROADCAST_CALL] RefreshActivatorGUIBroadcastReceiver.onReceive", "(2)");

            long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            long eventId = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);

            refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshEditorGUIBroadcastReceiver");
            refreshIntent.putExtra(EXTRA_REFRESH_ICONS, refreshIcons);
            refreshIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profileId);
            refreshIntent.putExtra(PPApplication.EXTRA_EVENT_ID, eventId);
            LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
        }

    }

}
