package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MusicBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
/*
//        PPApplicationStatic.logE("[IN_BROADCAST] MusicBroadcastReceiver.onReceive","xxx");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        final String action = intent.getAction();

        final Context appContext = context.getApplicationContext();

        if (EventStatic.getGlobalEventsRunning(appContext)) {
            Runnable runnable = () -> {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=MusicBroadcastReceiver.onReceive");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager1 = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager1 != null) {
                            wakeLock = powerManager1.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_DeviceIdleModeBroadcastReceiver_onReceive);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        Log.e("MusicBroadcastReceiver.onReceive", action);
                        AudioManager audioManager = (AudioManager)appContext.getSystemService(Context.AUDIO_SERVICE);
                        Log.e("MusicBroadcastReceiver.onReceive", "music active = " + audioManager.isMusicActive());

                        // start events handler
//                            PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] MusicBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_DEVICE_IDLE_MODE");
                        //EventsHandler eventsHandler = new EventsHandler(appContext);
                        //eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_MUSIC});

                    } catch (Exception e) {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
            PPApplicationStatic.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);
        }

 */
    }

}
