package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NextAlarmClockBroadcastReceiver extends BroadcastReceiver {

    static final String PREF_EVENT_ALARM_CLOCK_TIME_COUNT = "eventAlarmClockTimeCount";
    static final String PREF_EVENT_ALARM_CLOCK_TIME = "eventAlarmClockTime";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("[IN_BROADCAST] NextAlarmClockBroadcastReceiver.onReceive", "xxx");
//        PPApplication.logE("[IN_BROADCAST_ALARM] NextAlarmClockBroadcastReceiver.onReceive", "xxx");

        if (intent == null)
            return;

        //if (!PPApplication.getApplicationStarted(context.getApplicationContext(), true))
        //    return;

        //if (android.os.Build.VERSION.SDK_INT >= 21) {
            String action = intent.getAction();
            PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "action="+action);

            if ((action != null) && action.equals(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)) {

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    AlarmManager.AlarmClockInfo alarmClockInfo = alarmManager.getNextAlarmClock();
                    PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "alarmClockInfo="+alarmClockInfo);

                    if (alarmClockInfo != null) {

                        long _time = alarmClockInfo.getTriggerTime();

                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
                        String time = sdf.format(_time);
                        PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "_time="+time);

                        PendingIntent infoPendingIntent = alarmClockInfo.getShowIntent();
                        // infoPendingIntent == null - Xiaomi Clock :-/
                        // infoPendingIntent == null - LG Clock :-/
                        // infoPendingIntent == null - Huawei Clock :-/

                        PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "infoPendingIntent="+infoPendingIntent);

                        if (infoPendingIntent != null) {
                            String packageName = infoPendingIntent.getCreatorPackage();
                            PPApplication.logE("NextAlarmClockBroadcastReceiver.onReceive", "packageName="+packageName);
                            if (packageName != null) {
                                if (!packageName.equals(PPApplication.PACKAGE_NAME)) {

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

                                        setAlarm(_time, packageName, alarmManager, context);
                                }
                            } /*else {
                                setAlarm(_time, "", alarmManager, context);
                            }*/
                        } /*else {
                            setAlarm(_time, "", alarmManager, context);
                        }*/
                    }
                    /*else {
                        //getEventAlarmClockTime(context);
                        //getEventAlarmClockPackageName(context);
                        //removeAlarm(ApplicationPreferences.prefEventAlarmClockPackageName, alarmManager, context);
                        setEventAlarmClockTime("", 0, context);
                    }*/
                }
            }
        //}
    }

    private static int hashData(String alarmPackageName) {
        int sLength = alarmPackageName.length();
        int sum = 0;
        for(int i = 0 ; i < sLength-1 ; i++){
            sum += alarmPackageName.charAt(i)<<(5*i);
        }
        return sum;
    }

    private static void removeAlarm(String alarmPackageName,AlarmManager alarmManager, Context context) {
        PPApplication.logE("NextAlarmClockBroadcastReceiver.removeAlarm", "xxx");

        //Intent intent = new Intent(context, AlarmClockBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PhoneProfilesService.ACTION_ALARM_CLOCK_BROADCAST_RECEIVER);
        //intent.setClass(context, AlarmClockBroadcastReceiver.class);

        // cancel alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, hashData(alarmPackageName), intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    static void setAlarm(long alarmTime, String alarmPackageName, AlarmManager alarmManager, Context context) {
        removeAlarm(alarmPackageName, alarmManager, context);

        if (alarmTime == 0)
            return;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarmTime);

        Calendar alarmCalendar = Calendar.getInstance();
        alarmCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        alarmCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        alarmCalendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        alarmCalendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        alarmCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        alarmCalendar.set(Calendar.MILLISECOND, 0);
        alarmCalendar.set(Calendar.SECOND, 0);
        // removed 5 seconds, because must be received before set it again
        alarmCalendar.add(Calendar.SECOND, -5);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yyyy HH:mm:ss:S");
        String time = sdf.format(alarmCalendar.getTimeInMillis());
        PPApplication.logE("NextAlarmClockBroadcastReceiver.setAlarm", "alarmTime="+time);
        PPApplication.logE("NextAlarmClockBroadcastReceiver.setAlarm", "alarmPackageName="+alarmPackageName);

        Calendar now = Calendar.getInstance();
        if ((alarmCalendar.getTimeInMillis() >= now.getTimeInMillis()) && (!alarmPackageName.isEmpty())) {

            setEventAlarmClockTime(alarmPackageName, alarmTime, context);

            PPApplication.logE("NextAlarmClockBroadcastReceiver.setAlarm", "SET ALARM");

            //PhoneProfilesService instance = PhoneProfilesService.getInstance();
            //if (instance == null)
            //    return;

            // !!! Keep disabled "if", next alarm my be received before registering
            // AlarmClockBroadcastReceiver for example from Editor
            //if (instance.alarmClockBroadcastReceiver != null) {
            //long alarmTime = time;// - Event.EVENT_ALARM_TIME_SOFT_OFFSET;

            //Intent intent = new Intent(context, AlarmClockBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_ALARM_CLOCK_BROADCAST_RECEIVER);
            //intent.setClass(context, AlarmClockBroadcastReceiver.class);

            intent.putExtra(AlarmClockBroadcastReceiver.EXTRA_ALARM_PACKAGE_NAME, alarmPackageName);

            // set alarm

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, hashData(alarmPackageName), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent editorIntent = new Intent(context, EditorActivity.class);
            editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmCalendar.getTimeInMillis(), infoPendingIntent);
            alarmManager.setAlarmClock(clockInfo, pendingIntent);

            //if (android.os.Build.VERSION.SDK_INT >= 23)
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pendingIntent);
            //else //if (android.os.Build.VERSION.SDK_INT >= 19)
            //    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            //else
            //    alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            //}
        }
    }

    static List<NextAlarmClockData> getEventAlarmClockTimes(Context context) {
        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
        int count = preferences.getInt(PREF_EVENT_ALARM_CLOCK_TIME_COUNT, -1);

        PPApplication.logE("NextAlarmClockBroadcastReceiver.getEventAlarmClockTimes", "count="+count);

        if (count > -1) {
            List<NextAlarmClockData> times = new ArrayList<>();

            Gson gson = new Gson();

            for (int i = 0; i < count; i++) {
                String json = preferences.getString(PREF_EVENT_ALARM_CLOCK_TIME + i, "");
                if (!json.isEmpty()) {
                    NextAlarmClockData time = gson.fromJson(json, NextAlarmClockData.class);
                    times.add(time);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yyyy HH:mm:ss:S");
                    String _time = sdf.format(time.time);
                    PPApplication.logE("NextAlarmClockBroadcastReceiver.getEventAlarmClockTimes", "alarmTime="+_time);
                    PPApplication.logE("NextAlarmClockBroadcastReceiver.getEventAlarmClockTimes", "alarmPackageName="+time.packageName);
                }
            }
            return times;
        } else
            return null;
    }
    @SuppressLint("SimpleDateFormat")
    static void setEventAlarmClockTime(String packageName, long time, Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yyyy HH:mm:ss:S");
        String ___time = sdf.format(time);
        PPApplication.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "alarmTime="+___time);
        PPApplication.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "alarmPackageName="+packageName);

        if ((packageName != null) && !packageName.isEmpty()) {
            List<NextAlarmClockData> times = getEventAlarmClockTimes(context);
            PPApplication.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "times="+times);

            if (times == null) {
                NextAlarmClockData _time = new NextAlarmClockData(packageName, time);
                times = new ArrayList<>();
                times.add(_time);
                PPApplication.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "added (1)");
            } else {
                boolean found = false;
                int idx = 0;
                for (NextAlarmClockData __time : times) {
                    sdf = new SimpleDateFormat("d.MM.yyyy HH:mm:ss:S");
                    ___time = sdf.format(time);
                    PPApplication.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "alarmTime from shared prefs="+___time);
                    PPApplication.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "alarmPackageName from shared prefs="+packageName);

                    if (__time.packageName.equals(packageName)) {
                        NextAlarmClockData _time = new NextAlarmClockData(__time.packageName, time);
                        times.set(idx, _time);
                        PPApplication.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "set");
                        found = true;
                        break;
                    }
                    ++idx;
                }
                if (!found) {
                    NextAlarmClockData _time = new NextAlarmClockData(packageName, time);
                    times.add(_time);
                    PPApplication.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "added (2)");
                }
            }
            PPApplication.logE("NextAlarmClockBroadcastReceiver.getEventAlarmClockTimes", "count="+times.size());

            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);

            editor.putInt(PREF_EVENT_ALARM_CLOCK_TIME_COUNT, times.size());

            Gson gson = new Gson();

            for (int i = 0; i < times.size(); i++) {
                String json = gson.toJson(times.get(i));
                editor.putString(PREF_EVENT_ALARM_CLOCK_TIME + i, json);
            }

            editor.apply();
        }
    }

}
