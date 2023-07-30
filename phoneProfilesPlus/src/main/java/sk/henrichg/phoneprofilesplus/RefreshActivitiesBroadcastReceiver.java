package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class RefreshActivitiesBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_REFRESH_ICONS = "refresh_icons";
    static final String EXTRA_REFRESH_ALSO_EDITOR = "refresh_also_editor";
    static final String EXTRA_RELOAD_ACTIVITY = "reload_activity";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] RefreshActivitiesBroadcastReceiver.onReceive", "xxx");

        boolean refreshIcons = intent.getBooleanExtra(EXTRA_REFRESH_ICONS, false);
        boolean refreshAlsoEditor = intent.getBooleanExtra(EXTRA_REFRESH_ALSO_EDITOR, true);
        boolean reloadActivity = intent.getBooleanExtra(EXTRA_RELOAD_ACTIVITY, false);

//        PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] RefreshActivatorGUIBroadcastReceiver.onReceive", "(1)");
        Intent refreshIntent = new Intent(ActivatorActivity.ACTION_REFRESH_ACTIVATOR_GUI_BROADCAST_RECEIVER);
        refreshIntent.putExtra(EXTRA_REFRESH_ICONS, refreshIcons);
        refreshIntent.putExtra(EXTRA_RELOAD_ACTIVITY, reloadActivity);
        LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);

        if (refreshAlsoEditor) {
//            PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] RefreshActivatorGUIBroadcastReceiver.onReceive", "(2)");

            long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            long eventId = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);

            refreshIntent = new Intent(EditorActivity.ACTION_REFRESH_EDITOR_GUI_BROADCAST_RECEIVER);
            refreshIntent.putExtra(EXTRA_REFRESH_ICONS, refreshIcons);
            refreshIntent.putExtra(EXTRA_RELOAD_ACTIVITY, reloadActivity);
            refreshIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profileId);
            refreshIntent.putExtra(PPApplication.EXTRA_EVENT_ID, eventId);
            LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
        }

    }

}
