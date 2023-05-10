package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * Handles broadcasts related to SIM card state changes.
 * <p>
 * Possible states that are received here are:
 * <p>
 * Documented:
 * ABSENT
 * NETWORK_LOCKED
 * PIN_REQUIRED
 * PUK_REQUIRED
 * READY
 * UNKNOWN
 * <p>
 * Undocumented:
 * NOT_READY (ICC interface is not ready, e.g. radio is off or powering on)
 * CARD_IO_ERROR (three consecutive times there was a SIM IO error)
 * IMSI (ICC IMSI is ready in property)
 * LOADED (all ICC records, including IMSI, are loaded)
 * <p>
 * Note: some of these are not documented in
 * <a href="https://developer.android.com/reference/android/telephony/TelephonyManager.html">...</a>
 * but they can be found deeper in the source code, namely in com.android.internal.telephony.IccCardConstants.
 */
public class SimStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] SimStateChangedBroadcastReceiver.onReceive", "xxx");

        if (intent == null)
            return;

//        final Intent _intent = intent;

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadBroadcast();
        //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        //__handler.post(new PPApplication.PPHandlerThreadRunnable(
        //        context.getApplicationContext()) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//          PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=SimStateChangedBroadcastReceiver.onReceive");

            //Context appContext= appContextWeakRef.get();
            //if (appContext != null) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":SimStateChangedBroadcastReceiver_onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    // Not needed, GlobalUtils.hasSIMCard() do not set result into global variables
                    //GlobalUtils.hasSIMCard(appContext, 0);
                    //GlobalUtils.hasSIMCard(appContext, 1);
                    //GlobalUtils.hasSIMCard(appContext, 2);

                    // for Call and Roaming sensor
                    PPApplicationStatic.registerPhoneCallsListener(false, appContext);
                    // for Call and SMS sensor
                    PPApplicationStatic.registerPPPExtenderReceiverForSMSCall(false, appContext);
                    PPApplicationStatic.registerReceiversForCallSensor(false, appContext);
                    PPApplicationStatic.registerReceiversForSMSSensor(false, appContext);

                    GlobalUtils.sleep(1000);

                    // for Call and Roaming sensor
                    PPApplicationStatic.registerPhoneCallsListener(true, appContext);
                    // for Call and SMS sensor
                    PPApplicationStatic.registerPPPExtenderReceiverForSMSCall(true, appContext);
                    PPApplicationStatic.registerReceiversForCallSensor(true, appContext);
                    PPApplicationStatic.registerReceiversForSMSSensor(true, appContext);

                    // for Mobile cells sensor
                    PPApplicationStatic.restartMobileCellsScanner(appContext);

                    if (EventStatic.getGlobalEventsRunning(appContext)) {
                        //if (PhoneProfilesService.getInstance() != null) {

                            // start events handler

//                            PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] SimStateChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_SIM_STATE_CHANGED");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SIM_STATE_CHANGED);

//                            PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] SimStateChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                        //}
                    }

                } catch (Exception e) {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        }; //);
        PPApplicationStatic.createEventsHandlerExecutor();
        PPApplication.eventsHandlerExecutor.submit(runnable);
    }

}
