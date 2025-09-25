package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class InterruptionFilterChangedBroadcastReceiver extends BroadcastReceiver {

    //private static final String TAG = InterruptionFilterChangedBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] InterruptionFilterChangedBroadcastReceiver.onReceive","xxx");

        if (!PPApplication.ringerModeInternalChange) {

            NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();

                final AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                int systemRingerMode = AudioManager.RINGER_MODE_NORMAL;
                if (audioManager != null)
                    systemRingerMode = audioManager.getRingerMode();

                // convert to profile zenMode
                int ringerMode = Profile.RINGERMODE_ZENMODE;
                int zenMode = 0;
                switch (interruptionFilter) {
                    case NotificationManager.INTERRUPTION_FILTER_ALL:
                        //if (ActivateProfileHelper.vibrationIsOn(/*context.getApplicationContext(), */audioManager, true))
                        if (systemRingerMode == AudioManager.RINGER_MODE_VIBRATE) {
                            zenMode = Profile.ZENMODE_ALL_AND_VIBRATE;
                            ringerMode = Profile.RINGERMODE_VIBRATE;
                        }
                        else {
                            zenMode = Profile.ZENMODE_ALL;
                            ringerMode = Profile.RINGERMODE_RING;
                        }
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                        //if (ActivateProfileHelper.vibrationIsOn(/*context.getApplicationContext(), */audioManager, true))
                        if (systemRingerMode == AudioManager.RINGER_MODE_VIBRATE)
                            zenMode = Profile.ZENMODE_PRIORITY_AND_VIBRATE;
                        else
                            zenMode = Profile.ZENMODE_PRIORITY;
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_NONE:
                        zenMode = Profile.ZENMODE_NONE;
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                        zenMode = Profile.ZENMODE_ALARMS;
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                        zenMode = Profile.ZENMODE_ALL;
                        ringerMode = Profile.RINGERMODE_RING;
                        break;
                }
                if (zenMode != 0) {
//                            PPApplicationStatic.logE("[SYNCHRONIZED] InterruptionFilterChangedBroadcastReceiver.onReceive", "PPApplication.notUnlinkVolumesMutex");
                    synchronized (PPApplication.notUnlinkVolumesMutex) {
                        PPApplication.ringerModeNotUnlinkVolumes = true;
                    }
                    ActivateProfileHelper.saveRingerMode(context.getApplicationContext(), ringerMode);
                    ActivateProfileHelper.saveZenMode(context.getApplicationContext(), zenMode);
                }
            }
        }
    }

    private static int getZenMode(Context context, AudioManager audioManager) {
        // convert to profile zenMode
        int zenMode = 0;
        NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();
            int ringerMode = audioManager.getRingerMode();
            switch (interruptionFilter) {
                case NotificationManager.INTERRUPTION_FILTER_ALL:
                    //if (ActivateProfileHelper.vibrationIsOn(/*context, */audioManager, true))
                    if (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
                        zenMode = Profile.ZENMODE_ALL_AND_VIBRATE;
                    else
                        zenMode = Profile.ZENMODE_ALL;
                    break;
                case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                    //if (ActivateProfileHelper.vibrationIsOn(/*context, */audioManager, true))
                    if (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
                        zenMode = Profile.ZENMODE_PRIORITY_AND_VIBRATE;
                    else
                        zenMode = Profile.ZENMODE_PRIORITY;
                    break;
                case NotificationManager.INTERRUPTION_FILTER_NONE:
                    zenMode = Profile.ZENMODE_NONE;
                    break;
                case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                    zenMode = Profile.ZENMODE_ALARMS;
                    break;
                case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                    zenMode = Profile.ZENMODE_ALL;
                    break;
            }
        }
        return zenMode;
    }

    static void setZenMode(Context context, AudioManager audioManager/*, String from*/) {
            //boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            //if (/*no60 &&*/ GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                int zenMode = getZenMode(context, audioManager);
                if (zenMode != 0) {
                    ActivateProfileHelper.saveRingerMode(context, Profile.RINGERMODE_ZENMODE);
                    ActivateProfileHelper.saveZenMode(context, zenMode);
                }
            //}
    }

}
