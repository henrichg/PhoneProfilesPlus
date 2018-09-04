package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.Context.POWER_SERVICE;

public class NextAlarmClockBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### NextAlarmClockBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "NextAlarmClockBroadcastReceiver.onReceive", "NextAlarmClockBroadcastReceiver_onReceive");

        if (intent == null)
            return;

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(context.getApplicationContext(), true))
            return;

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            String action = intent.getAction();
            if ((action != null) && action.equals(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    AlarmManager.AlarmClockInfo alarmClockInfo = alarmManager.getNextAlarmClock();
                    if (alarmClockInfo != null) {
                        long _time = alarmClockInfo.getTriggerTime();
                        if (PPApplication.logEnabled()) {
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                            String result = sdf.format(_time);
                            PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "_time=" + result);
                        }

                        PendingIntent pendingIntent = alarmClockInfo.getShowIntent();
                        if (pendingIntent != null) {
                            String packageName = pendingIntent.getCreatorPackage();
                            if (packageName != null) {
                                PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "packageName=" + packageName);

                                // com.google.android.deskclock - Google Clock
                                // com.sec.android.app.clockpackage - Samsung Clock

                                if (packageName.equals("com.google.android.deskclock") ||
                                    packageName.equals("com.sec.android.app.clockpackage")) {
                                    //Intent _intent = new Intent(context, AlarmClockBroadcastReceiver.class);
                                    Intent _intent = new Intent();
                                    _intent.setAction(PhoneProfilesService.ACTION_ALARM_CLOCK_BROADCAST_RECEIVER);
                                    //_intent.setClass(context, AlarmClockBroadcastReceiver.class);

                                    PendingIntent _pendingIntent = PendingIntent.getBroadcast(context, 9998, _intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(_time, infoPendingIntent);
                                    alarmManager.setAlarmClock(clockInfo, _pendingIntent);
                                }
                            } else
                                PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "packageName == null");
                        } else
                            PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "pendingIntent == null");
                    }
                    else
                        PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "alarmClockInfo == null");
                }
            }
        }
    }
}
