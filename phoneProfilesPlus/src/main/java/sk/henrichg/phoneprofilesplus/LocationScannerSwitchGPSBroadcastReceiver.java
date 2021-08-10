package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class LocationScannerSwitchGPSBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] LocationScannerSwitchGPSBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "LocationScannerSwitchGPSBroadcastReceiver.onReceive", "LocationScannerSwitchGPSBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        doWork(appContext);
    }

    static void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(_context, LocationScannerSwitchGPSBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_LOCATION_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
                //intent.setClass(context, LocationScannerSwitchGPSBroadcastReceiver.class);

                @SuppressLint("UnspecifiedImmutableFlag")
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    //PPApplication.logE("LocationScannerSwitchGPSBroadcastReceiver.removeAlarm", "alarm found");

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        PPApplication.cancelWork(MainWorker.LOCATION_SCANNER_SWITCH_GPS_TAG_WORK, false);
        //PPApplication.logE("[HANDLER] LocationScannerSwitchGPSBroadcastReceiver.removeAlarm", "removed");
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    static void setAlarm(Context context)
    {
        removeAlarm(context);

        int interval = 25; // seconds
        if (ApplicationPreferences.applicationEventLocationUpdateInterval > 1)
            interval = (ApplicationPreferences.applicationEventLocationUpdateInterval * 60) / LocationScanner.INTERVAL_DIVIDE_VALUE; // interval is in minutes
        int delay = interval + 10; // interval from settings + 10 seconds;

        if (!LocationScanner.useGPS)
            delay = 30 * 60;  // 30 minutes with GPS OFF

        if (!PPApplication.isIgnoreBatteryOptimizationEnabled(context)) {
            if (ApplicationPreferences.applicationUseAlarmClock) {
                //Intent intent = new Intent(_context, LocationScannerSwitchGPSBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_LOCATION_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
                //intent.setClass(context, LocationScannerSwitchGPSBroadcastReceiver.class);

                @SuppressLint("UnspecifiedImmutableFlag")
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, delay);
                    long alarmTime = now.getTimeInMillis();

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String result = sdf.format(alarmTime);
                        PPApplication.logE("LocationScannerSwitchGPSBroadcastReceiver.setAlarm", "alarmTime=" + result);
                    }*/

                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    @SuppressLint("UnspecifiedImmutableFlag")
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
            } else {
            /*int keepResultsDelay = delay * 5;
            if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
                keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/
                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.LOCATION_SCANNER_SWITCH_GPS_TAG_WORK)
                                .setInitialDelay(delay, TimeUnit.SECONDS)
                                .build();
                try {
                    if (PPApplication.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {
                            //PPApplication.logE("[HANDLER] LocationScannerSwitchGPSBroadcastReceiver.setAlarm", "enqueueUniqueWork - delay="+delay);

//                        //if (PPApplication.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(MainWorker.LOCATION_SCANNER_SWITCH_GPS_TAG_WORK);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                            PPApplication.logE("[TEST BATTERY] LocationScannerSwitchGPSBroadcastReceiver.setAlarm", "for=" + MainWorker.LOCATION_SCANNER_SWITCH_GPS_TAG_WORK + " workInfoList.size()=" + workInfoList.size());
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                            PPApplication.logE("[WORKER_CALL] LocationScannerSwitchGPSBroadcastReceiver.setAlarm", "xxx");
                            workManager.enqueueUniqueWork(MainWorker.LOCATION_SCANNER_SWITCH_GPS_TAG_WORK, ExistingWorkPolicy.REPLACE/*KEEP*/, worker);
                        }
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        }
        else {

            //Intent intent = new Intent(_context, LocationScannerSwitchGPSBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_LOCATION_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
            //intent.setClass(context, LocationScannerSwitchGPSBroadcastReceiver.class);

            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (ApplicationPreferences.applicationUseAlarmClock) {
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, delay);
                    long alarmTime = now.getTimeInMillis();

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String result = sdf.format(alarmTime);
                        PPApplication.logE("LocationScannerSwitchGPSBroadcastReceiver.setAlarm", "alarmTime=" + result);
                    }*/

                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    @SuppressLint("UnspecifiedImmutableFlag")
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                } else {
                    long alarmTime = SystemClock.elapsedRealtime() + delay * 1000L;

                    //if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else //if (android.os.Build.VERSION.SDK_INT >= 19)
                    //    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                }
            }
        }
    }

    static void doWork(final Context appContext) {
//        PPApplication.logE("##### LocationScannerSwitchGPSBroadcastReceiver.doWork", "xxx");

        PPApplication.startHandlerThreadPPScanners(/*"BootUpReceiver.onReceive2"*/);
        final Handler __handler2 = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
        //__handler2.post(new PPApplication.PPHandlerThreadRunnable(
        //        appContext) {
        __handler2.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=LocationScannerSwitchGPSBroadcastReceiver.doWork");

            //Context appContext= appContextWeakRef.get();
            //if (appContext != null) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":LocationScannerSwitchGPSBroadcastReceiver_doWork");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isLocationScannerStarted()) {
                        LocationScanner locationScanner = PhoneProfilesService.getInstance().getLocationScanner();
                        if (locationScanner != null) {
                            if (LocationScanner.mUpdatesStarted) {
//                            if (LocationScanner.useGPS) {
//                                if (PPApplication.googlePlayServiceAvailable) {
//                                    locationScanner.flushLocations();
//                                    PPApplication.sleep(5000);
//                                }
//                            }

//                            PPApplication.logE("##### LocationScannerSwitchGPSBroadcastReceiver.doWork", "LocationScanner.useGPS="+LocationScanner.useGPS);
                                locationScanner.stopLocationUpdates();

                                PPApplication.sleep(1000);

                                if (ApplicationPreferences.applicationEventLocationUseGPS && (!CheckOnlineStatusBroadcastReceiver.isOnline(appContext)))
                                    // force useGPS
                                    LocationScanner.useGPS = true;
                                else
                                    LocationScanner.useGPS = !LocationScanner.useGPS;

                                // this also calls LocationScannerSwitchGPSBroadcastReceiver.setAlarm()
                                locationScanner.startLocationUpdates();
                                locationScanner.updateTransitionsByLastKnownLocation();
                            }
                        }
                    }

                } catch (Exception e) {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                }
            //}
        });
    }

}
