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
//        PPApplication.logE("[IN_BROADCAST] InterruptionFilterChangedBroadcastReceiver.onReceive","xxx");

        //CallsCounter.logCounter(context, "InterruptionFilterChangedBroadcastReceiver.onReceive", "InterruptionFilterChangedBroadcastReceiver_onReceive");

        /*if (PPApplication.logEnabled()) {
            NotificationManager _mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (_mNotificationManager != null) {
                int interruptionFilter = _mNotificationManager.getCurrentInterruptionFilter();
                PPApplication.logE("********** InterruptionFilterChangedBroadcastReceiver.onReceive", "interruptionFilter=" + interruptionFilter);
            }
        }*/

        //if (android.os.Build.VERSION.SDK_INT >= 23) {
            //boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            //if (/*no60 &&*/ GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                if (!RingerModeChangeReceiver.internalChange) {

                    NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (mNotificationManager != null) {
                        int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();

                        final AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                        int ringerMode = AudioManager.RINGER_MODE_NORMAL;
                        if (audioManager != null)
                            ringerMode = audioManager.getRingerMode();

                        // convert to profile zenMode
                        int zenMode = 0;
                        switch (interruptionFilter) {
                            case NotificationManager.INTERRUPTION_FILTER_ALL:
                                //if (ActivateProfileHelper.vibrationIsOn(/*context.getApplicationContext(), */audioManager, true))
                                if (ringerMode == AudioManager.RINGER_MODE_VIBRATE)
                                    zenMode = Profile.ZENMODE_ALL_AND_VIBRATE;
                                else
                                    zenMode = Profile.ZENMODE_ALL;
                                break;
                            case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                                //if (ActivateProfileHelper.vibrationIsOn(/*context.getApplicationContext(), */audioManager, true))
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
                        //PPApplication.logE("********* InterruptionFilterChangedBroadcastReceiver.setZenMode", "from=InterruptionFilterChangedBroadcastReceiver.onReceive zenMode="+zenMode);
                        if (zenMode != 0) {
                            synchronized (PPApplication.notUnlinkVolumesMutex) {
                                RingerModeChangeReceiver.notUnlinkVolumes = true;
                            }
                            ActivateProfileHelper.saveRingerMode(context.getApplicationContext(), Profile.RINGERMODE_ZENMODE);
                            ActivateProfileHelper.saveZenMode(context.getApplicationContext(), zenMode);
                        }
                    }
                }

                //RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());
            //}
        //}
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static int getZenMode(Context context, AudioManager audioManager) {
        // convert to profile zenMode
        int zenMode = 0;
        NotificationManager mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            int interruptionFilter = mNotificationManager.getCurrentInterruptionFilter();
            //PPApplication.logE("********** InterruptionFilterChangedBroadcastReceiver.getZenMode", "interruptionFilter=" + interruptionFilter);
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
            //PPApplication.logE("InterruptionFilterChangedBroadcastReceiver.getZenMode", "zenMode=" + zenMode);
        }
        return zenMode;
    }

    public static void setZenMode(Context context, AudioManager audioManager/*, String from*/) {
        //if (android.os.Build.VERSION.SDK_INT >= 23) {
            //boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            //if (/*no60 &&*/ GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
                int zenMode = getZenMode(context, audioManager);
                //PPApplication.logE("********* InterruptionFilterChangedBroadcastReceiver.setZenMode", "from="+from+" zenMode="+zenMode);
                if (zenMode != 0) {
                    ActivateProfileHelper.saveRingerMode(context, Profile.RINGERMODE_ZENMODE);
                    ActivateProfileHelper.saveZenMode(context, zenMode);
                }
            //}
        //}
    }

    public static void requestInterruptionFilter(Context context, final int zenMode) {
        try {
            //if (android.os.Build.VERSION.SDK_INT >= 23) {
            //boolean no60 = !Build.VERSION.RELEASE.equals("6.0");
            //if (/*no60 &&*/ GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context)) {
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
            //}
            //}
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

}
