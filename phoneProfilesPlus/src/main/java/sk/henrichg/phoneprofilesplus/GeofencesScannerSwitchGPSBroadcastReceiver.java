package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GeofencesScannerSwitchGPSBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### GeofencesScannerSwitchGPSBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "GeofencesScannerSwitchGPSBroadcastReceiver.onReceive", "GeofencesScannerSwitchGPSBroadcastReceiver_onReceive");

        if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isGeofenceScannerStarted()) {
            GeofencesScanner.useGPS = !GeofencesScanner.useGPS;
            GeofencesScanner geofencesScanner = PhoneProfilesService.getGeofencesScanner();
            if (geofencesScanner != null) {
                geofencesScanner.stopLocationUpdates();
                geofencesScanner.startLocationUpdates();
            }
        }
    }

    static void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(context, GeofencesScannerSwitchGPSBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                PPApplication.logE("GeofencesScannerSwitchGPSBroadcastReceiver.removeAlarm", "alarm found");

                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static void setAlarm(Context context)
    {
        int delay = 1; // one minute with GPS ON
        if (!GeofencesScanner.useGPS)
            delay = 30;  // 30 minutes with GPS OFF

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, delay);
        long alarmTime = now.getTimeInMillis();

        if (PPApplication.logEnabled()) {
            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            String result = sdf.format(alarmTime);
            PPApplication.logE("GeofencesScannerSwitchGPSBroadcastReceiver.setAlarm", "alarmTime=" + result);
        }

        Intent intent = new Intent(context, GeofencesScannerSwitchGPSBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        if (alarmManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= 23)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else if (android.os.Build.VERSION.SDK_INT >= 19)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        }
    }

}
