package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;

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

        //final Context appContext = context.getApplicationContext();
        Runnable runnable = () -> {

            synchronized (PPApplication.handleEventsMutex) {

                // Iterate all playback configurations
                for (int i = 0; i < configs.size(); i++) {
                    // Check if usage of current configuration is media
                    if (configs.get(i).getAudioAttributes().getUsage() == AudioAttributes.USAGE_MEDIA) {
                        // Set is media playing to true since active playback was found
                        //Log.e("PPAudioPlaybackCallback.onPlaybackConfigChanged", "played");

                        // start events handler
//                PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] PPAudioPlaybackCallback.onPlaybackConfigChanged", "SENSOR_TYPE_MUSIC");
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

            }

        };
        PPApplicationStatic.createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);
    }

}
