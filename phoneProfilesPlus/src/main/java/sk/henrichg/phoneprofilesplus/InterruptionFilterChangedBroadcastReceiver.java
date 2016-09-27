package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;

public class InterruptionFilterChangedBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = InterruptionFilterChangedBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!RingerModeChangeReceiver.internalChange) {

                final AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

                NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();

                // convert to profile zenMode
                int zenMode = 0;
                switch (interruptionFilter) {
                    case NotificationManager.INTERRUPTION_FILTER_ALL:
                        if (vibrationIsOn(context.getApplicationContext(), audioManager))
                            zenMode = 4;
                        else
                            zenMode = 1;
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                        if (vibrationIsOn(context.getApplicationContext(), audioManager))
                            zenMode = 5;
                        else
                            zenMode = 2;
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_NONE:
                        zenMode = 3;
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                        zenMode = 6;
                        break;
                }
                GlobalData.logE(TAG, "onReceive(zenMode=" + zenMode + ')');
                if (zenMode != 0) {
                    //Log.e(TAG, "onInterruptionFilterChanged  new zenMode=" + zenMode);
                    GlobalData.setRingerMode(context.getApplicationContext(), 5);
                    GlobalData.setZenMode(context.getApplicationContext(), zenMode);
                }
            }

            //RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());
        }
    }

    private static boolean vibrationIsOn(Context context, AudioManager audioManager) {
        int ringerMode = audioManager.getRingerMode();
        int vibrateType = -999;
        //if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1)
        //    vibrateType = audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
        int vibrateWhenRinging;
        vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0);

        GlobalData.logE(TAG, "vibrationIsOn(ringerMode="+ringerMode+")");
        GlobalData.logE(TAG, "vibrationIsOn(vibrateType="+vibrateType+")");
        GlobalData.logE(TAG, "vibrationIsOn(vibrateWhenRinging="+vibrateWhenRinging+")");

        return (ringerMode == AudioManager.RINGER_MODE_VIBRATE) ||
                (vibrateType == AudioManager.VIBRATE_SETTING_ON) ||
                (vibrateType == AudioManager.VIBRATE_SETTING_ONLY_SILENT) ||
                (vibrateWhenRinging == 1);
    }

    private static int getZenMode(Context context, AudioManager audioManager) {
        // convert to profile zenMode
        int zenMode = 0;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();
            GlobalData.logE(TAG, "getZenMode(interruptionFilter=" + interruptionFilter + ')');
            switch (interruptionFilter) {
                case NotificationManager.INTERRUPTION_FILTER_ALL:
                    if (vibrationIsOn(context, audioManager))
                        zenMode = 4;
                    else
                        zenMode = 1;
                    break;
                case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                    if (vibrationIsOn(context, audioManager))
                        zenMode = 5;
                    else
                        zenMode = 2;
                    break;
                case NotificationManager.INTERRUPTION_FILTER_NONE:
                    zenMode = 3;
                    break;
                case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                    zenMode = 6;
                    break;
            }
            GlobalData.logE(TAG, "getZenMode(zenMode=" + zenMode + ')');
        }
        return zenMode;
    }

    public static void setZenMode(Context context, AudioManager audioManager) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int zenMode = getZenMode(context, audioManager);
            if (zenMode != 0) {
                GlobalData.setRingerMode(context, 5);
                GlobalData.setZenMode(context, zenMode);
            }
        }
    }

    public static void requestInterruptionFilter(Context context, final int zenMode) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int interruptionFilter = NotificationManager.INTERRUPTION_FILTER_ALL;
            switch (zenMode) {
                case ActivateProfileHelper.ZENMODE_ALL:
                    interruptionFilter = NotificationManager.INTERRUPTION_FILTER_ALL;
                    break;
                case ActivateProfileHelper.ZENMODE_PRIORITY:
                    interruptionFilter = NotificationManager.INTERRUPTION_FILTER_PRIORITY;
                    break;
                case ActivateProfileHelper.ZENMODE_NONE:
                    interruptionFilter = NotificationManager.INTERRUPTION_FILTER_NONE;
                    break;
                case ActivateProfileHelper.ZENMODE_ALARMS:
                    interruptionFilter = NotificationManager.INTERRUPTION_FILTER_ALARMS;
                    break;
            }
            //Log.e(TAG, "requestInterruptionFilter(" + interruptionFilter + ')');
            NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.setInterruptionFilter(interruptionFilter);
        }
    }

}
