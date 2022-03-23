package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.PowerManager;

public class VolumeChangedBroadcastReceiver extends BroadcastReceiver {

    static boolean previousRingtoneMuted = false;
    static boolean previousNotificationMuted = false;
    static boolean previousMediaMuted = false;
    static boolean previousAlarmMuted = false;
    static boolean previousSystemMuted = false;
    static boolean previousVoiceMuted = false;
    static boolean previousBluetoothSCOMuted = false;

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] VolumeChangedBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "VolumeChangedBroadcastReceiver.onReceive", "VolumeChangedBroadcastReceiver_onReceive");

//        Bundle extras = intent.getExtras();
//        if (extras != null) {
//            for (String key : extras.keySet()) {
//                Log.e("VolumeChangedBroadcastReceiver.onReceive", key + " : " + (extras.get(key) != null ? extras.get(key) : "NULL"));
//            }
//        }

        //android.media.EXTRA_VOLUME_STREAM_TYPE : 2
        //android.media.EXTRA_VOLUME_STREAM_VALUE : 0
        //android.media.EXTRA_VOLUME_SHOW_UI : false
        //android.media.EXTRA_PREV_VOLUME_STREAM_VALUE : 1

        final int streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
        //int streamValue = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1);
        //int prevStreamValue = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1);

        final Context appContext = context.getApplicationContext();

        if (!EventPreferencesVolumes.internalChange) {

            if (PPApplication.getApplicationStarted(true)) {
                // application is started

                if (Event.getGlobalEventsRunning()) {
                    //PPApplication.logE("VolumeChangedBroadcastReceiver.onReceive","xxx");

                    PPApplication.startHandlerThreadBroadcast();
                    final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                    //        context.getApplicationContext()) {
                    __handler.post(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=VolumeChangedBroadcastReceiver.onReceive");

                        //Context appContext= appContextWeakRef.get();
                        //if (appContext != null) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":CalendarProviderChangedBroadcastReceiver_onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            final AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);

                            boolean callEventHandler = false;
                            if (streamType == AudioManager.STREAM_RING) {
                                boolean actualMute = audioManager.isStreamMute(streamType);
                                if (actualMute != previousRingtoneMuted) {
                                    previousRingtoneMuted = actualMute;
                                    callEventHandler = true;
                                }
                            }
                            if (streamType == AudioManager.STREAM_NOTIFICATION) {
                                boolean actualMute = audioManager.isStreamMute(streamType);
                                if (actualMute != previousNotificationMuted) {
                                    previousNotificationMuted = actualMute;
                                    callEventHandler = true;
                                }
                            }
                            if (streamType == AudioManager.STREAM_MUSIC) {
                                boolean actualMute = audioManager.isStreamMute(streamType);
                                if (actualMute != previousMediaMuted) {
                                    previousMediaMuted = actualMute;
                                    callEventHandler = true;
                                }
                            }
                            if (streamType == AudioManager.STREAM_ALARM) {
                                boolean actualMute = audioManager.isStreamMute(streamType);
                                if (actualMute != previousAlarmMuted) {
                                    previousAlarmMuted = actualMute;
                                    callEventHandler = true;
                                }
                            }
                            if (streamType == AudioManager.STREAM_SYSTEM) {
                                boolean actualMute = audioManager.isStreamMute(streamType);
                                if (actualMute != previousSystemMuted) {
                                    previousSystemMuted = actualMute;
                                    callEventHandler = true;
                                }
                            }
                            if (streamType == AudioManager.STREAM_VOICE_CALL) {
                                boolean actualMute = audioManager.isStreamMute(streamType);
                                if (actualMute != previousVoiceMuted) {
                                    previousVoiceMuted = actualMute;
                                    callEventHandler = true;
                                }
                            }
                            if (streamType == AudioManager.STREAM_BLUETOOTH_SCO) {
                                boolean actualMute = audioManager.isStreamMute(streamType);
                                if (actualMute != previousBluetoothSCOMuted) {
                                    previousBluetoothSCOMuted = actualMute;
                                    callEventHandler = true;
                                }
                            }
                            if (callEventHandler) {
//                                PPApplication.logE("[EVENTS_HANDLER_CALL] SettingsContentObserver.onChange", "sensorType=SENSOR_TYPE_VOLUMES");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_VOLUMES);
                            }

                            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=VolumeChangedBroadcastReceiver.onReceive");
                        } catch (Exception e) {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
                    //}

                }
            }
        }


    }

    static void init(Context appContext) {
        final AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);

        previousRingtoneMuted = audioManager.isStreamMute(AudioManager.STREAM_RING);
        previousNotificationMuted = audioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION);
        previousMediaMuted = audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
        previousAlarmMuted = audioManager.isStreamMute(AudioManager.STREAM_ALARM);
        previousSystemMuted = audioManager.isStreamMute(AudioManager.STREAM_SYSTEM);
        previousVoiceMuted = audioManager.isStreamMute(AudioManager.STREAM_VOICE_CALL);
        previousBluetoothSCOMuted = audioManager.isStreamMute(AudioManager.STREAM_BLUETOOTH_SCO);
    }

}
