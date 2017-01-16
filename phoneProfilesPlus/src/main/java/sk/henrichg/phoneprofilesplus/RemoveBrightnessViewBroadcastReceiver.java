package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RemoveBrightnessViewBroadcastReceiver extends BroadcastReceiver
{

    public void onReceive(Context context, Intent intent)
    {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### RemoveBrightnessViewBroadcastReceiver.onReceive", "xxx");

        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        if (GUIData.brightneesView != null)
        {
            try {
                PPApplication.logE("##### RemoveBrightnessViewBroadcastReceiver.onReceive", "before removeView");
                windowManager.removeView(GUIData.brightneesView);
                PPApplication.logE("##### RemoveBrightnessViewBroadcastReceiver.onReceive", "after removeView");
            } catch (Exception ignored) {
            }
            GUIData.brightneesView = null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static void setAlarm(Context context)
    {
        if (context != null)
        {
            PPApplication.logE("@@@ RemoveBrightnessViewBroadcastReceiver.setAlarm","xxx");

            removeAlarm(context);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, 5);
            long alarmTime = calendar.getTimeInMillis();

            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            PPApplication.logE("@@@ RemoveBrightnessViewBroadcastReceiver.setAlarm","alarmTime="+sdf.format(alarmTime));

            AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, RemoveBrightnessViewBroadcastReceiver.class);

            PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            // not needed exact for removing notification
            /*if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            else
            if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            else*/
                alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
        }
    }

    public static void removeAlarm(Context context)
    {
        PPApplication.logE("@@@ RemoveBrightnessViewBroadcastReceiver.removeAlarm","xxx");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent intent = new Intent(context, RemoveBrightnessViewBroadcastReceiver.class);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            PPApplication.logE("@@@ RemoveBrightnessViewBroadcastReceiver.removeAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        else
            PPApplication.logE("@@@ RemoveBrightnessViewBroadcastReceiver.removeAlarm","alarm not found");
    }

}
