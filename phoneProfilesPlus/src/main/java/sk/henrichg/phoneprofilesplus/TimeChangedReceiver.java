package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class TimeChangedReceiver extends BroadcastReceiver {
    public TimeChangedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("TimeChangedReceiver.onReceive", "xxx");

        if ((intent != null) && (intent.getAction() != null)) {
            final String action = intent.getAction();
            PPApplication.logE("TimeChangedReceiver.onReceive", "action="+action);
            if (action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                    action.equals(Intent.ACTION_TIME_CHANGED)/* ||
                    action.equals(Intent.ACTION_TIME_TICK)*/) {
                CallsCounter.logCounter(context, "TimeChangedReceiver.onReceive", "TimeChangedReceiver_onReceive");

                final Context appContext = context.getApplicationContext();

                /*long oldCurrentTime = PPApplication.currentTime;
                //if (action.equals(Intent.ACTION_TIME_TICK))
                    PPApplication.currentTime = Calendar.getInstance().getTimeInMillis();*/

                if (!PPApplication.getApplicationStarted(appContext, true))
                    return;

                boolean timeChanged = true;

                /*if (action.equals(Intent.ACTION_TIME_TICK)) {
                    Calendar oldCalendar = Calendar.getInstance();
                    oldCalendar.setTimeInMillis(oldCurrentTime);
                    //oldCalendar.set(Calendar.SECOND, 0);
                    oldCalendar.set(Calendar.MILLISECOND, 0);

                    Calendar newCalendar = Calendar.getInstance();
                    newCalendar.setTimeInMillis(PPApplication.currentTime);
                    //newCalendar.set(Calendar.SECOND, 0);
                    newCalendar.set(Calendar.MILLISECOND, 0);

                    if ((newCalendar.getTimeInMillis() - oldCalendar.getTimeInMillis()) < 0) {
                        PPApplication.logE("TimeChangedReceiver.onReceive", "old is higher");
                        timeChanged = true;
                    }
                    else {
                        PPApplication.logE("TimeChangedReceiver.onReceive", "old is lower");
                        timeChanged = (newCalendar.getTimeInMillis() - oldCalendar.getTimeInMillis()) >= (2000 * 60);
                    }
                }*/
                if (action.equals(Intent.ACTION_TIME_CHANGED)) {
                    if (!PPApplication.isScreenOn)
                        timeChanged = false;
                }

                /*if (action.equals(Intent.ACTION_TIME_CHANGED)) {
                    timeChanged = false;
                    String isAutoTime = Settings.Global.getString(appContext.getContentResolver(), Settings.Global.AUTO_TIME);
                    PPApplication.logE("TimeChangedReceiver.onReceive", "isAutoTime="+isAutoTime);
                    if ((isAutoTime != null) && isAutoTime.equals("0")) {
                        timeChanged = true;
                    }
                }*/

                PPApplication.logE("TimeChangedReceiver.onReceive", "timeChanged="+timeChanged);

                if (timeChanged) {
                    PPApplication.startHandlerThread("TimeChangedReceiver.onReceive");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":StartEventNotificationBroadcastReceiver_doWork");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                PPApplication.logE("PPApplication.startHandlerThread", "START run - from=TimeChangedReceiver.onReceive");

                                doWork(appContext);

                                PPApplication.logE("PPApplication.startHandlerThread", "END run - from=TimeChangedReceiver.onReceive");

                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    static void doWork(Context appContext) {
        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);

        dataWrapper.fillProfileList(false, false);
        for (Profile profile : dataWrapper.profileList) {
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, appContext);

            if (profile._deviceRunApplicationChange == 1) {
                String[] splits = profile._deviceRunApplicationPackageName.split("\\|");
                for (String split : splits)
                    RunApplicationWithDelayBroadcastReceiver.removeDelayAlarm(appContext, split);
            }
        }
        Profile.setActivatedProfileForDuration(appContext, 0);

        LockDeviceAfterScreenOffBroadcastReceiver.doWork(false, appContext);
        LockDeviceActivityFinishBroadcastReceiver.doWork();
        GeofencesScanner.useGPS = true;
        GeofencesScannerSwitchGPSBroadcastReceiver.doWork();

        SearchCalendarEventsWorker.scheduleWork(appContext, false, null, true);

        //dataWrapper.clearSensorsStartTime();
        //dataWrapper.restartEvents(false, true, false, false, false);
        dataWrapper.restartEventsWithRescan(false, false, false, false);
    }
}
