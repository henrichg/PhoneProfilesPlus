package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class GeofenceScannerAlarmBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "geofenceScannerAlarm";
    public static final String EXTRA_ONESHOT = "oneshot";

    @Override
    public void onReceive(Context context, Intent intent) {

        GlobalData.logE("##### GeofenceScannerAlarmBroadcastReceiver.onReceive", "xxx");

        if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19)) {
            int oneshot = intent.getIntExtra(EXTRA_ONESHOT, -1);
            if (oneshot == 0)
                setAlarm(context, false, false);
        }

        GlobalData.loadPreferences(context);

        if (!GlobalData.isGeofenceScannerStarted()) {
            removeAlarm(context, false);
            removeAlarm(context, true);
            return;
        }

        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if (isPowerSaveMode && GlobalData.applicationEventLocationUpdateInPowerSaveMode.equals("2")) {
            removeAlarm(context, false);
            removeAlarm(context, true);
            return;
        }

        if (GlobalData.getGlobalEventsRuning(context)) {
            if (GlobalData.geofencesScanner.mUpdatesStarted) {
                GlobalData.geofencesScanner.stopLocationUpdates();

                // send broadcast for calling EventsService
                Intent broadcastIntent = new Intent(context, GeofenceScannerBroadcastReceiver.class);
                context.sendBroadcast(broadcastIntent);
            }
            else
                GlobalData.geofencesScanner.startLocationUpdates();
        }

    }


    @SuppressLint("NewApi")
    public static void setAlarm(Context context, boolean oneshot, boolean shortInterval)
    {
        GlobalData.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.setAlarm", "oneshot=" + oneshot);

        if (GlobalData.isGeofenceScannerStarted()) {

            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, GeofenceScannerAlarmBroadcastReceiver.class);

            if (oneshot) {
                removeAlarm(context, true);

                Calendar calendar = Calendar.getInstance();
                //calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.SECOND, 2);

                long alarmTime = calendar.getTimeInMillis();

                //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                //GlobalData.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot+"; alarmTime="+sdf.format(alarmTime));

                intent.putExtra(EXTRA_ONESHOT, 1);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
                    alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
                else if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
                    alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
                else
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);

                GlobalData.setForceOneGeofenceScan(context, GlobalData.FORCE_ONE_SCAN_AND_DO_EVENTS);

            } else {
                removeAlarm(context, false);

                Calendar calendar = Calendar.getInstance();

                //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                //GlobalData.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot+"; alarmTime="+sdf.format(alarmTime));

                int updateDuration = 30;
                int interval = 0;
                if (GlobalData.geofencesScanner.mUpdatesStarted) {
                    interval = GlobalData.applicationEventLocationUpdateInterval * 60;
                    boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                    if (isPowerSaveMode && GlobalData.applicationEventLocationUpdateInPowerSaveMode.equals("0"))
                        interval = 2 * interval;
                    interval = interval - updateDuration;
                }
                else
                    interval = updateDuration;

                if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19)) {
                    if (shortInterval)
                        calendar.add(Calendar.SECOND, 2);
                        //calendar.add(Calendar.SECOND, 10);
                    else
                        //calendar.add(Calendar.MINUTE, interval);
                        calendar.add(Calendar.SECOND, interval);
                    long alarmTime = calendar.getTimeInMillis();

                    intent.putExtra(EXTRA_ONESHOT, 0);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, 0);
                    if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
                        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
                    else
                        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
                } else {
                    calendar.add(Calendar.SECOND, 10);
                    long alarmTime = calendar.getTimeInMillis();

                    intent.putExtra(EXTRA_ONESHOT, 0);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, 0);
                    //alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, interval * 60 * 1000, alarmIntent);
                    alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, interval * 1000, alarmIntent);
                }

            }
        }

        GlobalData.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.setAlarm", "alarm is set");
    }

    public static void removeAlarm(Context context, boolean oneshot)
    {
        GlobalData.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.removeAlarm", "oneshot=" + oneshot);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent intent = new Intent(context, GeofenceScannerAlarmBroadcastReceiver.class);
        PendingIntent pendingIntent;
        if (oneshot)
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
        else
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            GlobalData.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        else
            GlobalData.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm not found");
    }

    public static boolean isAlarmSet(Context context, boolean oneshot)
    {
        Intent intent = new Intent(context, GeofenceScannerAlarmBroadcastReceiver.class);
        PendingIntent pendingIntent;
        if (oneshot)
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
        else
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);

        if (pendingIntent != null)
            GlobalData.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.isAlarmSet","oneshot="+oneshot+"; alarm found");
        else
            GlobalData.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.isAlarmSet", "oneshot=" + oneshot + "; alarm not found");

        return (pendingIntent != null);
    }

}
