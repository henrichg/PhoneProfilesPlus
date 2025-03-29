package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telecom.TelecomManager;
import android.util.Log;

public class EndCallCallLengthBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] EndCallCallLengthBroadcastReceiver.onReceive", "xxx");
//        PPApplicationStatic.logE("[IN_BROADCAST_ALARM] EndCallCallLengthBroadcastReceiver.onReceive", "xxx");

        String action = intent.getAction();
        if (action != null) {
            doWork(/*true,*/ context);
        }
    }

    @SuppressLint("MissingPermission")
    private void doWork(/*boolean useHandler,*/ Context context) {
        Log.e("EndCallCallLengthBroadcastReceiver.doWork", "xxxxx");
        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (EventStatic.getGlobalEventsRunning(context)) {
            final Context appContext = context.getApplicationContext();
            TelecomManager telecomManager = (TelecomManager) appContext.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                if (Permissions.checkAnswerPhoneCalls(appContext))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        telecomManager.endCall();
            }
        }
    }

}
