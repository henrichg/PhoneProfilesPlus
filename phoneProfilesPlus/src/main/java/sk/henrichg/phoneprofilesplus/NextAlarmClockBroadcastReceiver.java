package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class NextAlarmClockBroadcastReceiver extends BroadcastReceiver {

    static final String PREF_EVENT_ALARM_CLOCK_TIME = "eventAlarmClockTime";
    static final String PREF_EVENT_ALARM_CLOCK_PACKAGE_NAME = "eventAlarmClockPackageName";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] NextAlarmClockBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "NextAlarmClockBroadcastReceiver.onReceive", "NextAlarmClockBroadcastReceiver_onReceive");

        if (intent == null)
            return;

        //if (!PPApplication.getApplicationStarted(context.getApplicationContext(), true))
        //    return;

        //if (android.os.Build.VERSION.SDK_INT >= 21) {
            String action = intent.getAction();
            if ((action != null) && action.equals(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    AlarmManager.AlarmClockInfo alarmClockInfo = alarmManager.getNextAlarmClock();
                    if (alarmClockInfo != null) {
                        long _time = alarmClockInfo.getTriggerTime();
                        /*if (PPApplication.logEnabled()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                            String result = sdf.format(_time);
                            PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "_time=" + result);
                        }*/

                        PendingIntent infoPendingIntent = alarmClockInfo.getShowIntent();
                        // infoPendingIntent == null - Xiaomi Clock :-/
                        // infoPendingIntent == null - LG Clock :-/
                        // infoPendingIntent == null - Huawei Clock :-/

                        if (infoPendingIntent != null) {
                            String packageName = infoPendingIntent.getCreatorPackage();
                            //PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "packageName=" + packageName);
                            if (packageName != null) {
                                if (!packageName.equals(PPApplication.PACKAGE_NAME)) {
                                    //PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "packageName=" + packageName);

                                    // com.google.android.deskclock - Google Clock
                                    // com.sec.android.app.clockpackage - Samsung Clock
                                    // com.sonyericsson.organizer - Sony Clock
                                    // com.amdroidalarmclock.amdroid - AMdroid
                                    // com.alarmclock.xtreme.free - Alarm Clock XTreme free
                                    // com.alarmclock.xtreme - Alarm Clock XTreme
                                    // droom.sleepIfUCan - Alarmy (Sleep if u can)
                                    // com.funanduseful.earlybirdalarm - Early Bird Alarm Clock
                                    // com.apalon.alarmclock.smart - Good Morning Alarm Clock
                                    // com.kog.alarmclock - I Can't Wake Up! Alarm Clock
                                    // com.urbandroid.sleep - Sleep as Android
                                    // ch.bitspin.timely - Timely
                                    // com.angrydoughnuts.android.alarmclock - Alarm Klock

                                    /*if (packageName.equals("com.google.android.deskclock") ||
                                        packageName.equals("com.sec.android.app.clockpackage") ||
                                        packageName.equals("com.sonyericsson.organizer") ||
                                        packageName.equals("com.amdroidalarmclock.amdroid") ||
                                        packageName.equals("com.alarmclock.xtreme") ||
                                        packageName.equals("com.alarmclock.xtreme.free") ||
                                        packageName.equals("droom.sleepIfUCan") ||
                                        packageName.equals("com.funanduseful.earlybirdalarm") ||
                                        packageName.equals("com.apalon.alarmclock.smart") ||
                                        packageName.equals("com.kog.alarmclock") ||
                                        packageName.equals("com.urbandroid.sleep") ||
                                        packageName.equals("ch.bitspin.timely") ||
                                        packageName.equals("com.angrydoughnuts.android.alarmclock"))*/

                                        setEventAlarmClockTime(context, _time);
                                        setEventAlarmClockPackageName(context, packageName);

                                        setAlarm(_time, packageName, alarmManager, context);
                                }
                            } /*else {
                                //PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "packageName == null");
                                setAlarm(_time, "", alarmManager, context);
                            }*/
                        } /*else {
                            //PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "infoPendingIntent == null");
                            setAlarm(_time, "", alarmManager, context);
                        }*/
                    }
                    //else {
                        //PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "alarmClockInfo == null");
                        //removeAlarm(alarmManager, context);
                    //}
                }
            }
        //}
    }

    private static void removeAlarm(AlarmManager alarmManager, Context context) {
        //Intent intent = new Intent(context, AlarmClockBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PhoneProfilesService.ACTION_ALARM_CLOCK_BROADCAST_RECEIVER);
        //intent.setClass(context, AlarmClockBroadcastReceiver.class);

        // cancel alarm
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 9998, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            //PPApplication.logE("NextAlarmClockBroadcastReceiver.removeAlarm", "alarm found");
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    static void setAlarm(long alarmTime, String alarmPackageName, AlarmManager alarmManager, Context context) {
        removeAlarm(alarmManager, context);

        Calendar now = Calendar.getInstance();
        if ((alarmTime >= now.getTimeInMillis()) && (!alarmPackageName.isEmpty())) {
            //PhoneProfilesService instance = PhoneProfilesService.getInstance();
            //if (instance == null)
            //    return;

            // !!! Keep disabled "if", next alarm my be received before registering
            // AlarmClockBroadcastReceiver for example from Editor
            //if (instance.alarmClockBroadcastReceiver != null) {
            //long alarmTime = time;// - Event.EVENT_ALARM_TIME_SOFT_OFFSET;

            /*if (PPApplication.logEnabled()) {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                String result = sdf.format(alarmTime);
                PPApplication.logE("NextAlarmClockBroadcastReceiver.setAlarm", "alarmTime=" + result);
            }*/

            //Intent intent = new Intent(context, AlarmClockBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_ALARM_CLOCK_BROADCAST_RECEIVER);
            //intent.setClass(context, AlarmClockBroadcastReceiver.class);

            intent.putExtra(AlarmClockBroadcastReceiver.EXTRA_ALARM_PACKAGE_NAME, alarmPackageName);

            // set alarm
            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 9998, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //if (android.os.Build.VERSION.SDK_INT >= 23)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            //else //if (android.os.Build.VERSION.SDK_INT >= 19)
            //    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            //else
            //    alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            //}
        }
    }

    static void getEventAlarmClockTime(Context context) {
        ApplicationPreferences.prefEventAlarmClockTime = ApplicationPreferences.
                getSharedPreferences(context).getLong(PREF_EVENT_ALARM_CLOCK_TIME, 0L);
    }
    static void setEventAlarmClockTime(Context context, long time) {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        editor.putLong(PREF_EVENT_ALARM_CLOCK_TIME, time);
        editor.apply();
        ApplicationPreferences.prefEventAlarmClockTime = time;
    }

    static void getEventAlarmClockPackageName(Context context) {
        ApplicationPreferences.prefEventAlarmClockPackageName = ApplicationPreferences.
                getSharedPreferences(context).getString(PREF_EVENT_ALARM_CLOCK_PACKAGE_NAME, "");
    }
    static void setEventAlarmClockPackageName(Context context, String packageName) {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        editor.putString(PREF_EVENT_ALARM_CLOCK_PACKAGE_NAME, packageName);
        editor.apply();
        ApplicationPreferences.prefEventAlarmClockPackageName = packageName;
    }

}
