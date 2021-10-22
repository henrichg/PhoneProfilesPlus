package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

public class DefaultSIMChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] DefaultSIMChangedBroadcastReceiver.onReceive", "xxx");

        if (intent == null)
            return;

        final Intent _intent = intent;

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThreadBroadcast();
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        //__handler.post(new PPApplication.PPHandlerThreadRunnable(
        //        context.getApplicationContext()) {
        __handler.post(() -> {
//          PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=DefaultSIMChangedBroadcastReceiver.onReceive");

            //Context appContext= appContextWeakRef.get();
            //if (appContext != null) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DefaultSIMChangedBroadcastReceiver_onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

//                    Bundle extras = _intent.getExtras();
//                    if (extras != null) {
//                        for (String key : extras.keySet()) {
//                            PPApplication.logE("DefaultSIMChangedBroadcastReceiver.onReceive", key + " : " + (extras.get(key) != null ? extras.get(key) : "NULL"));
//                        }
//                    }

                    PPApplication.logE("DefaultSIMChangedBroadcastReceiver.onReceive", "action="+_intent.getAction());
                    int subscriptionIdx = _intent.getIntExtra(SubscriptionManager.EXTRA_SUBSCRIPTION_INDEX, -1);
                    PPApplication.logE("DefaultSIMChangedBroadcastReceiver.onReceive", "subscriptionIdx="+subscriptionIdx);
                    /*String action = _intent.getAction();
                    if (action.equals(SubscriptionManager.ACTION_DEFAULT_SUBSCRIPTION_CHANGED)) {
                        // default sim for calls changed
                    }
                    if (action.equals(SubscriptionManager.ACTION_DEFAULT_SMS_SUBSCRIPTION_CHANGED)) {
                        // default sim for sms changed
                    }*/

                    if (Event.getGlobalEventsRunning()) {
                        if (PhoneProfilesService.getInstance() != null) {

                            // start events handler
                            //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=DefaultSIMChangedBroadcastReceiver.onReceive");

//                            PPApplication.logE("[EVENTS_HANDLER_CALL] MDefaultSIMChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=DefaultSIMChangedBroadcastReceiver.onReceive");
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
