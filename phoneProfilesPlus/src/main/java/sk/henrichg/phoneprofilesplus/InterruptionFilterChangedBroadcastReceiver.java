package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;

public class InterruptionFilterChangedBroadcastReceiver extends BroadcastReceiver {

    //private static final String TAG = InterruptionFilterChangedBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        //CallsCounter.logCounter(context, "InterruptionFilterChangedBroadcastReceiver.onReceive", "InterruptionFilterChangedBroadcastReceiver_onReceive");

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                if (!RingerModeChangeReceiver.internalChange) {

                    final AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

                    NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (mNotificationManager != null) {
                        int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();

                        // convert to profile zenMode
                        int zenMode = 0;
                        switch (interruptionFilter) {
                            case NotificationManager.INTERRUPTION_FILTER_ALL:
                                if (ActivateProfileHelper.vibrationIsOn(/*context.getApplicationContext(), */audioManager, true))
                                    zenMode = 4;
                                else
                                    zenMode = 1;
                                break;
                            case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                                if (ActivateProfileHelper.vibrationIsOn(/*context.getApplicationContext(), */audioManager, true))
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
                            case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                                zenMode = 1;
                                break;
                        }
                        //PPApplication.logE(TAG, "onReceive(zenMode=" + zenMode + ')');
                        if (zenMode != 0) {
                            RingerModeChangeReceiver.notUnlinkVolumes = true;
                            ActivateProfileHelper.saveRingerMode(context.getApplicationContext(), 5);
                            ActivateProfileHelper.saveZenMode(context.getApplicationContext(), zenMode);
                        }
                    }
                }

                //RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static int getZenMode(Context context, AudioManager audioManager) {
        // convert to profile zenMode
        int zenMode = 0;
        NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();
            PPApplication.logE("InterruptionFilterChangedBroadcastReceiver.getZenMode", "interruptionFilter=" + interruptionFilter);
            switch (interruptionFilter) {
                case NotificationManager.INTERRUPTION_FILTER_ALL:
                    if (ActivateProfileHelper.vibrationIsOn(/*context, */audioManager, true))
                        zenMode = 4;
                    else
                        zenMode = 1;
                    break;
                case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                    if (ActivateProfileHelper.vibrationIsOn(/*context, */audioManager, true))
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
                case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                    zenMode = 1;
                    break;
            }
            PPApplication.logE("InterruptionFilterChangedBroadcastReceiver.getZenMode", "zenMode=" + zenMode);
        }
        return zenMode;
    }

    public static void setZenMode(Context context, AudioManager audioManager) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                int zenMode = getZenMode(context, audioManager);
                if (zenMode != 0) {
                    ActivateProfileHelper.saveRingerMode(context, 5);
                    ActivateProfileHelper.saveZenMode(context, zenMode);
                }
            }
        }
    }

    public static void requestInterruptionFilter(Context context, final int zenMode) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            if (no60 && GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
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
                NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotificationManager != null)
                    mNotificationManager.setInterruptionFilter(interruptionFilter);
            }
        }
    }

}
