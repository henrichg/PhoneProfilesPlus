package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.Context.POWER_SERVICE;

public class PostDelayedBroadcastReceiver extends BroadcastReceiver {

    static final String ACTION_REMOVE_BRIGHTNESS_VIEW = "sk.henrichg.phoneprofilesplus.ACTION_REMOVE_BRIGHTNESS_VIEW";
    static final String ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE = "sk.henrichg.phoneprofilesplus.ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE";
    static final String ACTION_DISABLE_SCREEN_TIMEOUT_INTERNAL_CHANGE_TO_FALSE = "sk.henrichg.phoneprofilesplus.ACTION_DISABLE_SCREEN_TIMEOUT_INTERNAL_CHANGE_TO_FALSE";
    static final String ACTION_HANDLE_EVENTS = "sk.henrichg.phoneprofilesplus.ACTION_HANDLE_EVENTS";
    static final String ACTION_RESTART_EVENTS = "sk.henrichg.phoneprofilesplus.ACTION_RESTART_EVENTS";
    static final String ACTION_START_WIFI_SCAN = "sk.henrichg.phoneprofilesplus.ACTION_START_WIFI_SCAN";

    private static final String EXTRA_SENSOR_TYPE = "sensor_type";
    private static final String EXTRA_UNBLOCK_EVENTS_RUN = "unblock_events_run";
    private static final String EXTRA_LOG_TYPE = "log_type";

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### PostDelayedBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "PostDelayedBroadcastReceiver.onReceive", "PostDelayedBroadcastReceiver_onReceive");
        CallsCounter.logCounterNoInc(context, "PostDelayedBroadcastReceiver.onReceive->action="+intent.getAction(), "BatteryBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        String action = intent.getAction();
        if (action != null) {

            PPApplication.logE("PostDelayedBroadcastReceiver.onReceive", "action="+action);

            if (action.equals(ACTION_REMOVE_BRIGHTNESS_VIEW)) {
                PPApplication.startHandlerThread("PostDelayedBroadcastReceiver.onReceive");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PowerSaveModeBroadcastReceiver.onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        WindowManager windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
                        if (windowManager != null) {
                            if ((PhoneProfilesService.getInstance() != null) && (PhoneProfilesService.getInstance().brightnessView != null)) {
                                try {
                                    windowManager.removeView(PhoneProfilesService.getInstance().brightnessView);
                                } catch (Exception ignored) {
                                }
                                PhoneProfilesService.getInstance().brightnessView = null;
                            }
                        }

                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }

                    }
                });
            }

            if (action.equals(ACTION_RINGER_MODE_INTERNAL_CHANGE_TO_FALSE)) {
                RingerModeChangeReceiver.internalChange = false;
            }

            if (action.equals(ACTION_DISABLE_SCREEN_TIMEOUT_INTERNAL_CHANGE_TO_FALSE)) {
                ActivateProfileHelper.disableScreenTimeoutInternalChange = false;
            }

            if (action.equals(ACTION_HANDLE_EVENTS)) {
                final String sensorType = intent.getStringExtra(EXTRA_SENSOR_TYPE);

                PPApplication.startHandlerThread("PostDelayedBroadcastReceiver.onReceive");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PowerSaveModeBroadcastReceiver.onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(sensorType);

                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }

                    }
                });
            }

            if (action.equals(ACTION_RESTART_EVENTS)) {
                final boolean unblockEventsRun = intent.getBooleanExtra(EXTRA_UNBLOCK_EVENTS_RUN, false);
                final int logType = intent.getIntExtra(EXTRA_LOG_TYPE, DatabaseHandler.ALTYPE_UNDEFINED);

                PPApplication.startHandlerThreadRestartEventsWithDelay();
                PPApplication.restartEventsWithDelayHandler.removeCallbacksAndMessages(null);
                PPApplication.restartEventsWithDelayHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PowerSaveModeBroadcastReceiver.onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0);
                        if (logType != DatabaseHandler.ALTYPE_UNDEFINED)
                            dataWrapper.addActivityLog(logType, null, null, null, 0);
                        dataWrapper.restartEvents(unblockEventsRun, true/*, _interactive*/, true, false);

                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }

                    }
                });
            }

            if (action.equals(ACTION_START_WIFI_SCAN)) {
                PPApplication.startHandlerThread("PostDelayedBroadcastReceiver.onReceive");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PowerSaveModeBroadcastReceiver.onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        WifiScanJob.startScan(appContext);

                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }

                    }
                });
            }
        }
    }

    @SuppressLint("NewApi")
    static void setAlarm(String action, int delaySeconds)
    {
        if (PhoneProfilesService.getInstance() != null) {
            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, delaySeconds);
            int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
            long delayTime = now.getTimeInMillis() - gmtOffset;

            if (PPApplication.logEnabled()) {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                String result = sdf.format(delayTime);
                PPApplication.logE("PostDelayedBroadcastReceiver.setAlarm", action + " -> delayTime=" + result);
            }

            //Intent intent = new Intent(context, PostDelayedBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(action);
            //intent.setClass(context, PostDelayedBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(PhoneProfilesService.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) PhoneProfilesService.getInstance().getSystemService(Activity.ALARM_SERVICE);
            if (alarmManager != null) {
                if ((android.os.Build.VERSION.SDK_INT >= 21) &&
                        ApplicationPreferences.applicationUseAlarmClock(PhoneProfilesService.getInstance())) {
                    Intent editorIntent = new Intent(PhoneProfilesService.getInstance(), EditorProfilesActivity.class);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(PhoneProfilesService.getInstance(), 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(delayTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
                else {
                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, delayTime, pendingIntent);
                    else //if (android.os.Build.VERSION.SDK_INT >= 19)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, delayTime, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.RTC_WAKEUP, delayTime, pendingIntent);
                }
            }
        }
    }

    @SuppressLint("NewApi")
    static void setAlarmForHandleEvents(String sensorType,
                                        @SuppressWarnings("SameParameterValue") int delaySeconds)
    {
        if (PhoneProfilesService.getInstance() != null) {
            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, delaySeconds);
            int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
            long delayTime = now.getTimeInMillis() - gmtOffset;

            if (PPApplication.logEnabled()) {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                String result = sdf.format(delayTime);
                PPApplication.logE("PostDelayedBroadcastReceiver.setAlarm", ACTION_HANDLE_EVENTS + " -> delayTime=" + result);
            }

            //Intent intent = new Intent(context, PostDelayedBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(ACTION_HANDLE_EVENTS);
            //intent.setClass(context, PostDelayedBroadcastReceiver.class);

            intent.putExtra(EXTRA_SENSOR_TYPE, sensorType);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(PhoneProfilesService.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) PhoneProfilesService.getInstance().getSystemService(Activity.ALARM_SERVICE);
            if (alarmManager != null) {
                if ((android.os.Build.VERSION.SDK_INT >= 21) &&
                        ApplicationPreferences.applicationUseAlarmClock(PhoneProfilesService.getInstance())) {
                    Intent editorIntent = new Intent(PhoneProfilesService.getInstance(), EditorProfilesActivity.class);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(PhoneProfilesService.getInstance(), 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(delayTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
                else {
                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, delayTime, pendingIntent);
                    else //if (android.os.Build.VERSION.SDK_INT >= 19)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, delayTime, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.RTC_WAKEUP, delayTime, pendingIntent);
                }
            }
        }
    }

    @SuppressLint("NewApi")
    static void setAlarmForRestartEvents(int delaySeconds, boolean clearOld, final boolean unblockEventsRun, final int logType)
    {
        if (PhoneProfilesService.getInstance() != null) {
            AlarmManager alarmManager = (AlarmManager) PhoneProfilesService.getInstance().getSystemService(Activity.ALARM_SERVICE);
            if (alarmManager != null) {
                if (clearOld) {
                    //Intent intent = new Intent(context, PostDelayedBroadcastReceiver.class);
                    Intent intent = new Intent();
                    intent.setAction(ACTION_RESTART_EVENTS);
                    //intent.setClass(context, PostDelayedBroadcastReceiver.class);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(PhoneProfilesService.getInstance(), 0, intent, PendingIntent.FLAG_NO_CREATE);
                    if (pendingIntent != null) {
                        PPApplication.logE("PostDelayedBroadcastReceiver.removeAlarm", "alarm found");

                        alarmManager.cancel(pendingIntent);
                        pendingIntent.cancel();
                    }
                }

                Calendar now = Calendar.getInstance();
                now.add(Calendar.SECOND, delaySeconds);
                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                long delayTime = now.getTimeInMillis() - gmtOffset;

                if (PPApplication.logEnabled()) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String result = sdf.format(delayTime);
                    PPApplication.logE("PostDelayedBroadcastReceiver.setAlarm", ACTION_RESTART_EVENTS + " -> delayTime=" + result);
                }

                //Intent intent = new Intent(context, PostDelayedBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(ACTION_RESTART_EVENTS);
                //intent.setClass(context, PostDelayedBroadcastReceiver.class);

                intent.putExtra(EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun);
                intent.putExtra(EXTRA_LOG_TYPE, logType);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(PhoneProfilesService.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                if ((android.os.Build.VERSION.SDK_INT >= 21) &&
                        ApplicationPreferences.applicationUseAlarmClock(PhoneProfilesService.getInstance())) {
                    Intent editorIntent = new Intent(PhoneProfilesService.getInstance(), EditorProfilesActivity.class);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(PhoneProfilesService.getInstance(), 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(delayTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
                else {
                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, delayTime, pendingIntent);
                    else //if (android.os.Build.VERSION.SDK_INT >= 19)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, delayTime, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.RTC_WAKEUP, delayTime, pendingIntent);
                }
            }
        }
    }

}
