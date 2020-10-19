package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

// Delete button (X) or "clear all" in notification
public class CheckOnlineStatusBroadcastReceiver extends BroadcastReceiver {

    //static boolean deviceIsOnline = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("[IN_BROADCAST] CheckOnlineStatusBroadcastReceiver.onReceive", "xxx");

        //deviceIsOnline = isOnline(context.getApplicationContext());

        PPApplication.logE("[LOCAL_BROADCAST_CALL] CheckOnlineStatusBroadcastReceiver.onReceive", "xxx");
        Intent _intent = new Intent(PPApplication.PACKAGE_NAME + ".LocationGeofenceEditorOnlineStatusBroadcastReceiver");
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(_intent);
    }

    static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        }
        else
            return false;
    }


}
