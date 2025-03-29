package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.telecom.TelecomManager;

public class EndCallCallLengthBroadcastReceiver extends BroadcastReceiver {

    private long eventId;

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] EndCallCallLengthBroadcastReceiver.onReceive", "xxx");
//        PPApplicationStatic.logE("[IN_BROADCAST_ALARM] EndCallCallLengthBroadcastReceiver.onReceive", "xxx");

        String action = intent.getAction();
        if (action != null) {
            eventId = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);
            if (eventId != 0)
                doWork(/*true,*/ context);
        }
    }

    @SuppressLint("MissingPermission")
    private void doWork(/*boolean useHandler,*/ Context context) {
        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        final Context appContext = context.getApplicationContext();
        Runnable runnable = () -> {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_EndCallCallLengthBroadcastReceiver_onReceive);
                    wakeLock.acquire(10 * 60 * 1000);
                }

                Event event = DatabaseHandler.getInstance(appContext).getEvent(eventId);
                if (event != null) {
                    if (event._eventPreferencesCall._endCall) {
                        if (EventStatic.getGlobalEventsRunning(context)) {
                            TelecomManager telecomManager = (TelecomManager) appContext.getSystemService(Context.TELECOM_SERVICE);
                            if (telecomManager != null) {
                                if (Permissions.checkAnswerPhoneCalls(appContext))
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                                        telecomManager.endCall();
                            }
                        }
                    }
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
        };
        PPApplicationStatic.createEventsHandlerExecutor();
        PPApplication.eventsHandlerExecutor.submit(runnable);

    }

}
