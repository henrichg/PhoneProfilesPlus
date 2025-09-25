package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class TimeChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        if (intent != null)
//            PPApplicationStatic.logE("[IN_BROADCAST] TimeChangedReceiver.onReceive", "intent.getAction()="+intent.getAction());
//        else
//            PPApplicationStatic.logE("[IN_BROADCAST] TimeChangedReceiver.onReceive", "xxx");

        if ((intent != null) && (intent.getAction() != null)) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)/* ||
                    action.equals(Intent.ACTION_TIME_CHANGED) ||
                    action.equals(Intent.ACTION_TIME_TICK)*/)
            {

                //final Context appContext = context.getApplicationContext();

                if (!PPApplicationStatic.getApplicationStarted(true, true))
                    return;

                /*boolean timeChanged = true;

                if (action.equals(Intent.ACTION_TIME_TICK)) {
                    long uptimeDifference = SystemClock.elapsedRealtime() - PPApplication.lastUptimeTime;
                    long epochDifference = System.currentTimeMillis() - PPApplication.lastEpochTime;
                    long timeChange = Math.abs(uptimeDifference - epochDifference);

                    // Time has changed more than 1 minute
                    timeChanged = timeChange > 1000 * 60;

                    PPApplication.lastUptimeTime = SystemClock.elapsedRealtime();
                    PPApplication.lastEpochTime = System.currentTimeMillis();
                }*/

                /*if (action.equals(Intent.ACTION_TIME_CHANGED)) {
                    timeChanged = false;
                    String isAutoTime = Settings.Global.getString(appContext.getContentResolver(), Settings.Global.AUTO_TIME);
                    if ((isAutoTime != null) && isAutoTime.equals("0")) {
                        timeChanged = true;
                    }
                }

                */

                //if (timeChanged) {

                final Context appContext = context.getApplicationContext();
                Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=TimeChangedReceiver.onReceive");

                    //Context appContext= appContextWeakRef.get();
                    //if (appContext != null) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_TimeChangedReceiver_onReceive);
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            doWork(appContext, false);

                        } catch (Exception e) {
//                                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    //}
                };
//                PPApplicationStatic.logE("[EXECUTOR_CALL] TimeChangedReceiver.onReceive", "(xxx");
                PPApplicationStatic.createEventsHandlerExecutor();
                PPApplication.eventsHandlerExecutor.submit(runnable);
                //}
            }
        }
    }

    static void doWork(Context appContext, boolean logRestart/*, boolean forceRestart*/) {
        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);

        dataWrapper.fillProfileList(false, false);
        for (Profile profile : dataWrapper.profileList) {
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, appContext);

            if (profile._deviceRunApplicationChange == 1) {
                String[] splits = profile._deviceRunApplicationPackageName.split(StringConstants.STR_SPLIT_REGEX);
                for (String split : splits) {
                    RunApplicationWithDelayBroadcastReceiver.removeDelayAlarm(appContext, split);
                    //RunApplicationWithDelayBroadcastReceiver.setDelayAlarm(appContext, split);
                    //int startApplicationDelay = Application.getStartApplicationDelay(split);
                    //if (Application.getStartApplicationDelay(split) > 0)
                    //    RunApplicationWithDelayBroadcastReceiver.setDelayAlarm(appContext, startApplicationDelay, split);
                    //else
                    //    RunApplicationWithDelayBroadcastReceiver.removeDelayAlarm(appContext, split);
                }
            }
        }

        //Profile.setActivatedProfileForDuration(appContext, 0);

//        PPApplicationStatic.logE("[WORKER_CALL] TimeChangedReceiver.doWork", "xxx");
        LockDeviceAfterScreenOffBroadcastReceiver.doWork(false, appContext);
        LockDeviceActivityFinishBroadcastReceiver.doWork(appContext);
        PPApplication.locationScannerUseGPS = true;
        LocationScannerSwitchGPSBroadcastReceiver.doWork(appContext);

        DonationBroadcastReceiver.setAlarm(appContext);
        CheckPPPReleasesBroadcastReceiver.setAlarm(appContext);
        CheckCriticalPPPReleasesBroadcastReceiver.setAlarm(appContext);
        CheckRequiredExtenderReleasesBroadcastReceiver.setAlarm(appContext);
        CheckLatestPPPPSReleasesBroadcastReceiver.setAlarm(appContext);

//        Log.e("TimeChangedReceiver.doWork", "TwilightScanner.doWork()");
        TwilightScanner.doWork();

        SearchCalendarEventsWorker.scheduleWork(true);

        //dataWrapper.clearSensorsStartTime();
        //dataWrapper.restartEvents(false, true, false, false, false);
        //dataWrapper.restartEventsWithRescan(false, false, false, false);
        /*if (forceRestart) {
            if (!DataWrapper.getIsManualProfileActivation(false)) {
                dataWrapper.restartEventsWithRescan(true, false, true, false);
            }
            else {
                dataWrapper.restartEventsWithRescan(false, false, true, false);
            }
        }
        else {
            dataWrapper.restartEventsWithRescan(false, false, false, false);
        }*/

        PPApplicationStatic.addActivityLog(dataWrapper.context, PPApplication.ALTYPE_TIMEZONE_CHANGED,
                null, null, "");
//        Log.e("TimeChangedReceiver.doWork", "call if restart events");
        dataWrapper.restartEventsWithRescan(true, true, false, true, logRestart, false);
    }

}
