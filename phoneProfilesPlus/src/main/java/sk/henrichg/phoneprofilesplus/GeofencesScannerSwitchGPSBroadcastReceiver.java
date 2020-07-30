package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class GeofencesScannerSwitchGPSBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[BROADCAST CALL] GeofencesScannerSwitchGPSBroadcastReceiver.onReceive", "xxx");
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
                    //PPApplication.logE("GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm", "alarm found");

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        PPApplication.cancelWork(MainWorker.GEOFENCE_SCANNER_SWITCH_GPS_TAG_WORK);
        //PPApplication.logE("[HANDLER] GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm", "removed");
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static void setAlarm(Context context)
    {
        int delay = 1; // one minute with GPS ON
        if (!GeofencesScanner.useGPS)
            delay = 30;  // 30 minutes with GPS OFF

        if (ApplicationPreferences.applicationUseAlarmClock) {
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

                /*if (PPApplication.logEnabled()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String result = sdf.format(alarmTime);
                    PPApplication.logE("GeofencesScannerSwitchGPSBroadcastReceiver.setAlarm", "alarmTime=" + result);
                }*/

                Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            }
        }
        else {
            /*int keepResultsDelay = delay * 5;
            if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
                keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/
            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(MainWorker.GEOFENCE_SCANNER_SWITCH_GPS_TAG_WORK)
                            .setInitialDelay(delay, TimeUnit.MINUTES)
                            .build();
            try {
                if (PPApplication.getApplicationStarted(true)) {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null) {
                        //PPApplication.logE("[HANDLER] GeofencesScannerSwitchGPSBroadcastReceiver.setAlarm", "enqueueUniqueWork - delay="+delay);

//                        //if (PPApplication.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(MainWorker.GEOFENCE_SCANNER_SWITCH_GPS_TAG_WORK);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                            PPApplication.logE("[TEST BATTERY] GeofencesScannerSwitchGPSBroadcastReceiver.setAlarm", "for=" + MainWorker.GEOFENCE_SCANNER_SWITCH_GPS_TAG_WORK + " workInfoList.size()=" + workInfoList.size());
//                        } catch (Exception ignored) {
//                        }
//                        //}

                        workManager.enqueueUniqueWork(MainWorker.GEOFENCE_SCANNER_SWITCH_GPS_TAG_WORK, ExistingWorkPolicy.REPLACE/*KEEP*/, worker);
                    }
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
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
        //PPApplication.logE("[HANDLER] GeofencesScannerSwitchGPSBroadcastReceiver.doWork", "xxx");
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
