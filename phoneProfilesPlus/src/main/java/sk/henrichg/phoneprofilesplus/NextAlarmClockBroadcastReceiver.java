package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.SystemClock;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** @noinspection ExtractMethodRecommender*/
public class NextAlarmClockBroadcastReceiver extends BroadcastReceiver {

    static final String PREF_EVENT_ALARM_CLOCK_TIME_COUNT = "eventAlarmClockTimeCount";
    static final String PREF_EVENT_ALARM_CLOCK_TIME = "eventAlarmClockTime";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] NextAlarmClockBroadcastReceiver.onReceive", "xxx");
//        PPApplicationStatic.logE("[IN_BROADCAST_ALARM] NextAlarmClockBroadcastReceiver.onReceive", "xxx");

        if (intent == null)
            return;

        //if (!PPApplicationStatic.getApplicationStarted(context.getApplicationContext(), true))
        //    return;

        String action = intent.getAction();
//            PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.onReceive", "action="+action);

        if ((action != null) && action.equals(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)) {

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                AlarmManager.AlarmClockInfo alarmClockInfo = alarmManager.getNextAlarmClock();
//                    PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.onReceive", "alarmClockInfo="+alarmClockInfo);

                if (alarmClockInfo != null) {

                    long _time = alarmClockInfo.getTriggerTime();

//                        SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
//                        String time = sdf.format(_time);
//                        PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.onReceive", "_time="+time);

                    PendingIntent infoPendingIntent = alarmClockInfo.getShowIntent();
                    // infoPendingIntent == null - Xiaomi Clock :-/
                    // infoPendingIntent == null - LG Clock :-/
                    // infoPendingIntent == null - Huawei Clock :-/

//                        PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.onReceive", "infoPendingIntent="+infoPendingIntent);

                    if (infoPendingIntent != null) {
                        String packageName = infoPendingIntent.getCreatorPackage();
//                            PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.onReceive", "packageName="+packageName);
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

                                    setAlarm(_time, packageName, context);
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
    }

    private static int hashData(String alarmPackageName) {
        int sLength = alarmPackageName.length();
        int sum = 0;
        int length = sLength-1;
        for(int i = 0 ; i < length; i++){
            sum += alarmPackageName.charAt(i)<<(5*i);
        }
        return sum;
    }

    private static void removeAlarm(String alarmPackageName,AlarmManager alarmManager, Context context) {
//        PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.removeAlarm", "xxx");

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

    static void setAlarm(final long alarmTime, final String alarmPackageName, Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;

        if (alarmTime == 0) {
            removeAlarm(alarmPackageName, alarmManager, context);
            return;
        }

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

        // removed 5 seconds, because of:
        // - normally is alarmTime for future
        // - when is reached (boradcasted is ACTION_ALARM_CLOCK_BROADCAST_RECEIVER),
        //   received from system is again NextAlarmClockBroadcastReceiver with alarmTime = now time
        // - and with this situation must be be alarmCalendar < now time
        //   this prevents to configure alarm again
        alarmCalendar.add(Calendar.SECOND, -5);

        Calendar now = Calendar.getInstance();

        // alarm is not stored in shared prefs PREF_EVENT_ALARM_CLOCK_TIME_*

//            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yyyy HH:mm:ss:S");
//            String time = sdf.format(alarmCalendar.getTimeInMillis());
//            PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.setAlarm", "alarmTime="+time);
//            PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.setAlarm", "alarmPackageName="+alarmPackageName);

        final Context appContext = context.getApplicationContext();

        if ((alarmCalendar.getTimeInMillis() >= now.getTimeInMillis()) && (!alarmPackageName.isEmpty())) {

            removeAlarm(alarmPackageName, alarmManager, context);

            setEventAlarmClockTime(alarmPackageName, alarmTime, context);

            final long alamCalendarTime = alarmCalendar.getTimeInMillis();

            Runnable runnable = () -> {

//            PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.setAlarm", "SET ALARM");

                //PhoneProfilesService instance = PhoneProfilesService.getInstance();
                //if (instance == null)
                //    return;

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_NextAlarmClockBroadcastReceiver_setAlarm);
                        wakeLock.acquire(10 * 60 * 1000);
                    }

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

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, hashData(alarmPackageName), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    // Do not use this!!! User sends me e-mail with problems about usage of setAlarmClock
    //                Intent editorIntent = new Intent(context, EditorActivity.class);
    //                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    //                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    //                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmCalendar.getTimeInMillis(), infoPendingIntent);
    //                alarmManager.setAlarmClock(clockInfo, pendingIntent);

                    AlarmManager _alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
                    if (_alarmManager != null) {
                        //_alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alamCalendarTime, pendingIntent);
                        // must be used SystemClock.elapsedRealtime() because of AlarmManager.ELAPSED_REALTIME_WAKEUP
                        long duration = alamCalendarTime - now.getTimeInMillis();
                        long _alamCalendarTime = SystemClock.elapsedRealtime() + duration;
                        _alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, _alamCalendarTime, pendingIntent);
                    }

                } catch (Exception e) {
//                    PPApplicationStatic.logE("[WAKELOCK_EXCEPTION] NextAlarmClockBroadcastReceiver.setAlarm", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                }

            };
            PPApplicationStatic.createDelayedEventsHandlerExecutor();
            PPApplication.delayedEventsHandlerExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
        }
    }

    static List<NextAlarmClockData> getEventAlarmClockTimes(Context context) {
        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
        int count = preferences.getInt(PREF_EVENT_ALARM_CLOCK_TIME_COUNT, -1);

//        PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.getEventAlarmClockTimes", "count="+count);

        if (count > -1) {
            List<NextAlarmClockData> times = new ArrayList<>();

            Gson gson = new Gson();

            for (int i = 0; i < count; i++) {
                String json = preferences.getString(PREF_EVENT_ALARM_CLOCK_TIME + i, "");
                if (!json.isEmpty()) {
                    NextAlarmClockData time = gson.fromJson(json, NextAlarmClockData.class);
                    times.add(time);
//                    SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yyyy HH:mm:ss:S");
//                    String _time = sdf.format(time.time);
//                    PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.getEventAlarmClockTimes", "alarmTime="+_time);
//                    PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.getEventAlarmClockTimes", "alarmPackageName="+time.packageName);
                }
            }
            return times;
        } else
            return null;
    }

    static void setEventAlarmClockTime(String packageName, long time, Context context) {
//        SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yyyy HH:mm:ss:S");
//        String ___time = sdf.format(time);
//        PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "alarmTime="+___time);
//        PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "alarmPackageName="+packageName);

        if ((packageName != null) && !packageName.isEmpty()) {
            List<NextAlarmClockData> times = getEventAlarmClockTimes(context);
//            PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "times="+times);

            if (times == null) {
                NextAlarmClockData _time = new NextAlarmClockData(packageName, time);
                times = new ArrayList<>();
                times.add(_time);
//                PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "added (1)");
            } else {
                boolean found = false;
                int idx = 0;
                for (NextAlarmClockData __time : times) {
//                    sdf = new SimpleDateFormat("d.MM.yyyy HH:mm:ss:S");
//                    ___time = sdf.format(time);
//                    PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "alarmTime from shared prefs="+___time);
//                    PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "alarmPackageName from shared prefs="+packageName);

                    if (__time.packageName.equals(packageName)) {
                        NextAlarmClockData _time = new NextAlarmClockData(__time.packageName, time);
                        times.set(idx, _time);
//                        PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "set");
                        found = true;
                        break;
                    }
                    ++idx;
                }
                if (!found) {
                    NextAlarmClockData _time = new NextAlarmClockData(packageName, time);
                    times.add(_time);
//                    PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.setEventAlarmClockTime", "added (2)");
                }
            }
//            PPApplicationStatic.logE("NextAlarmClockBroadcastReceiver.getEventAlarmClockTimes", "count="+times.size());

            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);

            int size = times.size();
            editor.putInt(PREF_EVENT_ALARM_CLOCK_TIME_COUNT, size);

            Gson gson = new Gson();

            for (int i = 0; i < size; i++) {
                String json = gson.toJson(times.get(i));
                editor.putString(PREF_EVENT_ALARM_CLOCK_TIME + i, json);
            }

            editor.apply();
        }
    }

}
