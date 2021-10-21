package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
 * https://developer.android.com/reference/android/telephony/TelephonyManager.html
 * but they can be found deeper in the source code, namely in com.android.internal.telephony.IccCardConstants.
 */
public class SimStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] SimStateChangedBroadcastReceiver.onReceive", "xxx");

        if (intent == null)
            return;

//        final Intent _intent = intent;

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        //__handler.post(new PPApplication.PPHandlerThreadRunnable(
        //        context.getApplicationContext()) {
        __handler.post(() -> {
//          PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=SimStateChangedBroadcastReceiver.onReceive");

            //Context appContext= appContextWeakRef.get();
            //if (appContext != null) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":SimStateChangedBroadcastReceiver_onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

//                    Bundle extras = _intent.getExtras();
//                    if (extras != null) {
//                        for (String key : extras.keySet()) {
//                            PPApplication.logE("SimStateChangedBroadcastReceiver.onReceive", key + " : " + (extras.get(key) != null ? extras.get(key) : "NULL"));
//                        }
//                    }

                    PPApplication.initSIMCards();
                    synchronized (PPApplication.simCardsMutext) {
                        PPApplication.simCardsMutext.sim0Exists = PPApplication.hasSIMCard(appContext, 0);
                        PPApplication.simCardsMutext.sim1Exists = PPApplication.hasSIMCard(appContext, 1);
                        PPApplication.simCardsMutext.sim2Exists = PPApplication.hasSIMCard(appContext, 2);
                        PPApplication.simCardsMutext.simCardsDetected = true;
//                        PPApplication.logE("SimStateChangedBroadcastReceiver.onReceive", "PPApplication.simCardsMutext.sim0Exists="+PPApplication.simCardsMutext.sim0Exists);
//                        PPApplication.logE("SimStateChangedBroadcastReceiver.onReceive", "PPApplication.simCardsMutext.sim1Exists="+PPApplication.simCardsMutext.sim1Exists);
//                        PPApplication.logE("SimStateChangedBroadcastReceiver.onReceive", "PPApplication.simCardsMutext.sim2Exists="+PPApplication.simCardsMutext.sim2Exists);
//                        PPApplication.logE("SimStateChangedBroadcastReceiver.onReceive", "PPApplication.simCardsMutext.simCardsDetected="+PPApplication.simCardsMutext.simCardsDetected);
                    }

                    PhoneProfilesService.registerPhoneCallsListener(false, appContext);
                    PPApplication.sleep(1000);
                    PhoneProfilesService.registerPhoneCallsListener(true, appContext);

                    PPApplication.restartMobileCellsScanner(appContext);


                } catch (Exception e) {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
