package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ShortcutToMobileCellScanningAddedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] ShortcutToEditorAddedBroadcastReceiver.onReceive", "xxx");

        PPApplication.showToast(context.getApplicationContext(),
                context.getString(R.string.shortcut_to_mobile_cell_scanning_created_toast), Toast.LENGTH_SHORT);
    }

}
