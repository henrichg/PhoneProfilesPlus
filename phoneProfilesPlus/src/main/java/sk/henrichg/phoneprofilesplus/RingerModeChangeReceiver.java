package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;

import java.util.Calendar;

public class RingerModeChangeReceiver extends BroadcastReceiver {

    public static boolean internalChange = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        GlobalData.logE("##### RingerModeChangeReceiver.onReceive", "xxx");

        if (!internalChange) {
            GlobalData.logE("RingerModeChangeReceiver.onReceive", "!internalChange");
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            setRingerMode(context, audioManager);
        }

        //setAlarmForDisableInternalChange(context);
    }

    @SuppressWarnings("deprecation")
    public static int getRingerMode(Context context, AudioManager audioManager) {
        int ringerMode = audioManager.getRingerMode();

        int vibrateType = audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
        int vibrateWhenRinging;
        if (android.os.Build.VERSION.SDK_INT < 23)    // Not working in Android M (exception)
            vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), "vibrate_when_ringing", 0);
        else
            vibrateWhenRinging = Settings.System.getInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0);
        int interruptionFilter = -1;
        if (android.os.Build.VERSION.SDK_INT >= 21)
            interruptionFilter = Settings.Global.getInt(context.getContentResolver(), "zen_mode", -1);

        GlobalData.logE("RingerModeChangeReceiver.getRingerMode", "ringerMode="+ringerMode);
        GlobalData.logE("RingerModeChangeReceiver.getRingerMode", "vibrateType="+vibrateType);
        GlobalData.logE("RingerModeChangeReceiver.getRingerMode", "vibrateWhenRinging="+vibrateWhenRinging);
        GlobalData.logE("RingerModeChangeReceiver.getRingerMode", "interruptionFilter=" + interruptionFilter);

        // convert to profile ringerMode
        int pRingerMode = 0;
        if ((android.os.Build.VERSION.SDK_INT >= 21) && PPNotificationListenerService.isNotificationListenerServiceEnabled(context)) {
            if (interruptionFilter == ActivateProfileHelper.ZENMODE_ALL) {
                switch (ringerMode) {
                    case AudioManager.RINGER_MODE_NORMAL:
                        if ((vibrateType == AudioManager.VIBRATE_SETTING_ON) || (vibrateWhenRinging == 1))
                            pRingerMode = 2;
                        else
                            pRingerMode = 1;
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        pRingerMode = 3;
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        pRingerMode = 4;
                        break;
                }
            }
            else {
                pRingerMode = 5;
            }
        }
        else {
            if (interruptionFilter == ActivateProfileHelper.ZENMODE_PRIORITY) {
                pRingerMode = 4;
            }
            else {
                switch (ringerMode) {
                    case AudioManager.RINGER_MODE_NORMAL:
                        if ((vibrateType == AudioManager.VIBRATE_SETTING_ON) || (vibrateWhenRinging == 1))
                            pRingerMode = 2;
                        else
                            pRingerMode = 1;
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        pRingerMode = 3;
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        pRingerMode = 4;
                        break;
                }
            }
        }

        GlobalData.logE("RingerModeChangeReceiver.getRingerMode", "pRingerMode=" + pRingerMode);

        return pRingerMode;
    }

    public static void setRingerMode(Context context, AudioManager audioManager) {
        int pRingerMode = getRingerMode(context, audioManager);
        if (pRingerMode != 0) {
            //Log.e("RingerModeChangeReceiver",".setRingerMode  new ringerMode="+pRingerMode);
            GlobalData.setRingerMode(context, pRingerMode);
        }
    }

    public static void setAlarmForDisableInternalChange(Context context) {
        //Context context = getApplicationContext();
        Intent _intent = new Intent(context, DisableInernalChangeBroadcastReceiver.class);
        //intent.putExtra(EXTRA_ONESHOT, 1);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 1, _intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 3);
        long alarmTime = calendar.getTimeInMillis();

        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
    }

}
