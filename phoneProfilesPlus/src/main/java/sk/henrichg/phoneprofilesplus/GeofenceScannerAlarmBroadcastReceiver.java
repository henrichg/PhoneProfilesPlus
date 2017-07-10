package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Calendar;

public class GeofenceScannerAlarmBroadcastReceiver extends BroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "geofenceScannerAlarm";
    //public static final String EXTRA_ONESHOT = "oneshot";

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### GeofenceScannerAlarmBroadcastReceiver.onReceive", "xxx");

        GeofenceScannerJob.unregisterReceiver(context.getApplicationContext());

        //PPApplication.loadPreferences(context);

        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
        if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_LOCATION) > 0) {
            //int oneshot = intent.getIntExtra(EXTRA_ONESHOT, -1);
            //if (oneshot == 0)
            GeofenceScannerJob.scheduleJob(context, false, false);
            //setAlarm(context, false, false);
            dataWrapper.invalidateDataWrapper();
        }
        else {
            //removeAlarm(context);
            GeofenceScannerJob.cancelJob();
            dataWrapper.invalidateDataWrapper();
            return;
        }

        if (!PhoneProfilesService.isGeofenceScannerStarted()) {
            GeofenceScannerJob.cancelJob();
            //removeAlarm(context/*, false*/);
            //removeAlarm(context/*, true*/);
            return;
        }

        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode();
        if (isPowerSaveMode && ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode(context).equals("2")) {
            GeofenceScannerJob.cancelJob();
            //removeAlarm(context/*, false*/);
            //removeAlarm(context/*, true*/);
            return;
        }

        if (Event.getGlobalEventsRuning(context)) {
            if ((PhoneProfilesService.instance != null) && (PhoneProfilesService.geofencesScanner != null)) {
                if (PhoneProfilesService.geofencesScanner.mUpdatesStarted) {
                    PhoneProfilesService.geofencesScanner.stopLocationUpdates();

                    // send broadcast for calling EventsService
                    /*Intent broadcastIntent = new Intent(context, GeofenceScannerBroadcastReceiver.class);
                    context.sendBroadcast(broadcastIntent);*/
                    LocalBroadcastManager.getInstance(context).registerReceiver(PPApplication.geofenceScannerBroadcastReceiver, new IntentFilter("GeofenceScannerBroadcastReceiver"));
                    Intent broadcastIntent = new Intent("GeofenceScannerBroadcastReceiver");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
                } else
                    PhoneProfilesService.geofencesScanner.startLocationUpdates();
            }
        }

    }

    /*
    @SuppressLint("NewApi")
    public static void setAlarm(Context context, boolean startScanning, boolean forScreenOn)
    {
        PPApplication.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.setAlarm", "xxx");

        if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isGeofenceScannerStarted()) {

            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, GeofenceScannerAlarmBroadcastReceiver.class);

            removeAlarm(context);

            if (startScanning)
                PhoneProfilesService.geofencesScanner.mUpdatesStarted = false;

            Calendar calendar = Calendar.getInstance();

            //SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            //PPApplication.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.setAlarm","oneshot="+oneshot+"; alarmTime="+sdf.format(alarmTime));

            int updateDuration = 30;
            int interval;
            if (PhoneProfilesService.geofencesScanner.mUpdatesStarted) {
                interval = ApplicationPreferences.applicationEventLocationUpdateInterval(context) * 60;
                boolean isPowerSaveMode = DataWrapper.isPowerSaveMode();
                if (isPowerSaveMode && ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode(context).equals("1"))
                    interval = 2 * interval;
                interval = interval - updateDuration;
            }
            else {
                interval = updateDuration;
            }

            if (startScanning) {
                if (forScreenOn)
                    calendar.add(Calendar.SECOND, 5);
                else
                    calendar.add(Calendar.SECOND, 5);
            }
            else {
                //calendar.add(Calendar.MINUTE, interval);
                calendar.add(Calendar.SECOND, interval);
            }
            long alarmTime = calendar.getTimeInMillis();

            //intent.putExtra(EXTRA_ONESHOT, 0);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            if (android.os.Build.VERSION.SDK_INT >= 23)
                //alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
                alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            else
            if (android.os.Build.VERSION.SDK_INT >= 19)
                //alarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
                alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
            else
                alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
        }

        PPApplication.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.setAlarm", "alarm is set");
    }

    public static void removeAlarm(Context context)
    {
        //PPApplication.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.removeAlarm", "oneshot=" + oneshot);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Intent intent = new Intent(context, GeofenceScannerAlarmBroadcastReceiver.class);
        PendingIntent pendingIntent;
        //if (oneshot)
        //    pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
        //else
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            //PPApplication.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm found");
            PPApplication.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.removeAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        else
            //PPApplication.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.removeAlarm","oneshot="+oneshot+"; alarm not found");
            PPApplication.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.removeAlarm","alarm not found");
    }

    public static boolean isAlarmSet(Context context)
    {
        Intent intent = new Intent(context, GeofenceScannerAlarmBroadcastReceiver.class);
        PendingIntent pendingIntent;
        //if (oneshot)
        //    pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_NO_CREATE);
        //else
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);

        if (pendingIntent != null)
            //PPApplication.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.isAlarmSet","oneshot="+oneshot+"; alarm found");
            PPApplication.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.isAlarmSet","alarm found");
        else
            //PPApplication.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.isAlarmSet", "oneshot=" + oneshot + "; alarm not found");
            PPApplication.logE("@@@ GeofenceScannerAlarmBroadcastReceiver.isAlarmSet", "alarm not found");

        return (pendingIntent != null);
    }
    */
}
