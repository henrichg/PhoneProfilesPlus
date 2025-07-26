package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ShortcutForRestartEventsAddedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] ShortcutToEditorAddedBroadcastReceiver.onReceive", "xxx");

        PPApplication.showToast(context.getApplicationContext(),
                context.getString(R.string.shortcut_for_restart_events_created_toast), Toast.LENGTH_SHORT);
    }

}
