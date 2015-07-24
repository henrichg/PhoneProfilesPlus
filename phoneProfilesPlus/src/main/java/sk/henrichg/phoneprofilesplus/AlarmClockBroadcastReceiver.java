package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
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

            int zenMode = Settings.Global.getInt(context.getContentResolver(), "zen_mode", ActivateProfileHelper.ZENMODE_ALL);
            Log.e("AlarmClockBroadcastReceiver", "zen_mode="+zenMode);

            if (zenMode != ActivateProfileHelper.ZENMODE_ALL) {

                Log.e("AlarmClockBroadcastReceiver", "zen_mode != ALL");

                DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);

                Profile profile = dataWrapper.getActivatedProfile();
                profile = GlobalData.getMappedProfile(profile, context);

                if (profile != null) {

                    Log.e("AlarmClockBroadcastReceiver", "profile is activated");

                    /*
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //System.out.println(e);
                    }

                    Log.e("AlarmClockBroadcastReceiver", "set interruption filter to ALL");

                    PPNotificationListenerService.requestInterruptionFilter(context.getApplicationContext(),
                            NotificationListenerService.INTERRUPTION_FILTER_ALL);
                    */

                    /*
                    final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                    SettingsContentObserver.internalChange = true;
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM,  1, 0);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    */

                }
            }
        }
    }
}
