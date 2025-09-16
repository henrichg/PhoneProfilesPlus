package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.os.PowerManager;

import java.util.List;

public class PPAudioPlaybackCallback extends  AudioManager.AudioPlaybackCallback {

    private final Context appContext;

    PPAudioPlaybackCallback(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void onPlaybackConfigChanged(List<AudioPlaybackConfiguration> configs) {
        // play media from PPP is ignored
        if ((RingtonePreference.mediaPlayer != null) ||
                (VolumeDialogPreferenceFragment.mediaPlayer != null)/* ||
                not needed to test it, it uses ALARM channel
                (PlayRingingNotification.ringingMediaPlayer != null)*/)
            return;

        if (EventStatic.getGlobalEventsRunning(appContext)) {
            //final Context appContext = context.getApplicationContext();
            Runnable runnable = () -> {

                synchronized (PPApplication.handleEventsMutex) {

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PPAudioPlaybackCallback_onPlaybackConfigChanged);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        // Iterate all playback configurations
                        for (int i = 0; i < configs.size(); i++) {
                            // Check if usage of current configuration is media
                            if (configs.get(i).getAudioAttributes().getUsage() == AudioAttributes.USAGE_MEDIA) {
                                // Set is media playing to true since active playback was found
                                //Log.e("PPAudioPlaybackCallback.onPlaybackConfigChanged", "played");

                                // start events handler
    //                          PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPAudioPlaybackCallback.onPlaybackConfigChanged", "SENSOR_TYPE_MUSIC");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_MUSIC});

                                return;
                            }
                        }
                        // Set is media playing to false since no active playback found
                        //Log.e("PPAudioPlaybackCallback.onPlaybackConfigChanged", "not played");
    //                PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPAudioPlaybackCallback.onPlaybackConfigChanged", "SENSOR_TYPE_MUSIC");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_MUSIC});

                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[WAKELOCK_EXCEPTION] PPAudioPlaybackCallback.onPlaybackConfigChanged", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }

                }

            };
//            PPApplicationStatic.logE("[EXECUTOR_CALL] PPAudioPlaybackCallback.onPlaybackConfigChanged", "xxx");
            PPApplicationStatic.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);
        }
    }

}
