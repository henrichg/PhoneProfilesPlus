package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ShortcutToReadNFCTagAddedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] ShortcutToReadNFCTagAddedBroadcastReceiver.onReceive", "xxx");

        PPApplication.showToast(context.getApplicationContext(),
                context.getString(R.string.shortcut_to_read_nfc_tag_created_toast), Toast.LENGTH_SHORT);
    }

}
