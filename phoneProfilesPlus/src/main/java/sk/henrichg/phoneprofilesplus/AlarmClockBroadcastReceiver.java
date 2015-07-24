package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.util.Log;

public class AlarmClockBroadcastReceiver extends BroadcastReceiver {
    public AlarmClockBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("AlarmClockBroadcastReceiver", "ALARM");

        if (!GlobalData.getApplicationStarted(context))
            return;

        if (android.os.Build.VERSION.SDK_INT >= 21) {

            if (Settings.Global.getInt(context.getContentResolver(), "zen_mode", ActivateProfileHelper.ZENMODE_ALL)
                    != ActivateProfileHelper.ZENMODE_ALL) {

                DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

                Profile profile = dataWrapper.getActivatedProfile();
                profile = GlobalData.getMappedProfile(profile, context);

                if (profile != null) {
                    PPNotificationListenerService.requestInterruptionFilter(context.getApplicationContext(),
                            NotificationListenerService.INTERRUPTION_FILTER_ALL);
                }
            }
        }
    }
}
