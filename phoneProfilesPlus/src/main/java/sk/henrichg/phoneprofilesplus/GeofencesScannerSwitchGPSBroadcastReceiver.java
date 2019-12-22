package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class GeofencesScannerSwitchGPSBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### GeofencesScannerSwitchGPSBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "GeofencesScannerSwitchGPSBroadcastReceiver.onReceive", "GeofencesScannerSwitchGPSBroadcastReceiver_onReceive");
        doWork();
    }

    static void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(_context, GeofencesScannerSwitchGPSBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_GEOFENCES_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
                //intent.setClass(context, GeofencesScannerSwitchGPSBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    PPApplication.logE("GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm", "alarm found");

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception ignored) {}
        try {
            WorkManager workManager = WorkManager.getInstance(context);
            workManager.cancelUniqueWork("elapsedAlarmsGeofenceScannerSwitchGPSWork");
            workManager.cancelAllWorkByTag("elapsedAlarmsGeofenceScannerSwitchGPSWork");
        } catch (Exception ignored) {}
        PPApplication.logE("[HANDLER] GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm", "removed");
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static void setAlarm(Context context)
    {
        int delay = 1; // one minute with GPS ON
        if (!GeofencesScanner.useGPS)
            delay = 30;  // 30 minutes with GPS OFF

        if (ApplicationPreferences.applicationUseAlarmClock(context)) {
            //Intent intent = new Intent(_context, GeofencesScannerSwitchGPSBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_GEOFENCES_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
            //intent.setClass(context, GeofencesScannerSwitchGPSBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Calendar now = Calendar.getInstance();
                now.add(Calendar.MINUTE, delay);
                long alarmTime = now.getTimeInMillis();

                if (PPApplication.logEnabled()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String result = sdf.format(alarmTime);
                    PPApplication.logE("GeofencesScannerSwitchGPSBroadcastReceiver.setAlarm", "alarmTime=" + result);
                }

                Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            }
        }
        else {
            Data workData = new Data.Builder()
                    .putString(PhoneProfilesService.EXTRA_ELAPSED_ALARMS_WORK, ElapsedAlarmsWorker.ELAPSED_ALARMS_GEOFENCE_SCANNER_SWITCH_GPS)
                    .build();

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(ElapsedAlarmsWorker.class)
                            .setInputData(workData)
                            .setInitialDelay(delay, TimeUnit.MINUTES)
                            .build();
            try {
                WorkManager workManager = WorkManager.getInstance(context);
                PPApplication.logE("[HANDLER] GeofencesScannerSwitchGPSBroadcastReceiver.setAlarm", "enqueueUniqueWork - delay="+delay);
                workManager.enqueueUniqueWork("elapsedAlarmsGeofenceScannerSwitchGPSWork", ExistingWorkPolicy.REPLACE, worker);
            } catch (Exception ignored) {}
        }

        /*
        //Intent intent = new Intent(_context, GeofencesScannerSwitchGPSBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PhoneProfilesService.ACTION_GEOFENCES_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
        //intent.setClass(context, GeofencesScannerSwitchGPSBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (ApplicationPreferences.applicationUseAlarmClock(context)) {

                Calendar now = Calendar.getInstance();
                now.add(Calendar.MINUTE, delay);
                long alarmTime = now.getTimeInMillis();

                if (PPApplication.logEnabled()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String result = sdf.format(alarmTime);
                    PPApplication.logE("GeofencesScannerSwitchGPSBroadcastReceiver.setAlarm", "alarmTime=" + result);
                }

                Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            }
            else {
                long alarmTime = SystemClock.elapsedRealtime() + delay * 60 * 1000;

                if (android.os.Build.VERSION.SDK_INT >= 23)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                else //if (android.os.Build.VERSION.SDK_INT >= 19)
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                //else
                //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
            }
        }
        */
    }

    static void doWork() {
        PPApplication.logE("[HANDLER] GeofencesScannerSwitchGPSBroadcastReceiver.doWork", "xxx");
        if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted()) {
            GeofencesScanner.useGPS = !GeofencesScanner.useGPS;
            GeofencesScanner geofencesScanner = PhoneProfilesService.getInstance().getGeofencesScanner();
            if (geofencesScanner != null) {
                geofencesScanner.stopLocationUpdates();
                geofencesScanner.startLocationUpdates();
            }
        }
    }

}
