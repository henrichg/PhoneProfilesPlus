package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import java.util.Calendar;

public class RingerModeChangeReceiver extends BroadcastReceiver {

    public static boolean internalChange = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        GlobalData.logE("##### RingerModeChangeReceiver.onReceive", "xxx");

        if (!internalChange) {
            GlobalData.logE("RingerModeChangeReceiver.onReceive", "!internalChange");
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            setRingerMode(context, audioManager);
        }

        //setAlarmForDisableInternalChange(context);
    }

    private static int getRingerMode(Context context, AudioManager audioManager) {
        int ringerMode = audioManager.getRingerMode();

        GlobalData.logE("RingerModeChangeReceiver.getRingerMode", "ringerMode="+ringerMode);

        // convert to profile ringerMode
        int pRingerMode = 0;
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            int systemZenMode = GlobalData.getSystemZenMode(context, -1);
            GlobalData.logE("RingerModeChangeReceiver.getRingerMode", "systemZenMode=" + systemZenMode);
            if (systemZenMode == ActivateProfileHelper.ZENMODE_ALL) {
                switch (ringerMode) {
                    case AudioManager.RINGER_MODE_NORMAL:
                        if (GlobalData.vibrationIsOn(context, audioManager, false))
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
            else
                pRingerMode = 5;
        }
        else {
            switch (ringerMode) {
                case AudioManager.RINGER_MODE_NORMAL:
                    if (GlobalData.vibrationIsOn(context, audioManager, false))
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

    public static void removeAlarm(Context context/*, boolean oneshot*/)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DisableInernalChangeBroadcastReceiver.class);
        PendingIntent pendingIntent =  PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static void setAlarmForDisableInternalChange(Context context) {
        removeAlarm(context);

        //Context context = getApplicationContext();
        Intent _intent = new Intent(context, DisableInernalChangeBroadcastReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 1, _intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 3);
        long alarmTime = calendar.getTimeInMillis();

        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
    }

}
