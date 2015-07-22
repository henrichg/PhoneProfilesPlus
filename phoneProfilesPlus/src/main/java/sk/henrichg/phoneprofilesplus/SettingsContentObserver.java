package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

public class SettingsContentObserver  extends ContentObserver {

    public static boolean internalChange = false;

    private int previousVolumeRing = 0;
    private int previousVolumeNotification = 0;
    private int previousVolumeMusic = 0;
    private int previousVolumeAlarm = 0;
    private int previousVolumeSystem = 0;
    private int previousVolumeVoice = 0;
    //private int defaultRingerMode = 0;

    Context context;

    public SettingsContentObserver(Context c, Handler handler) {
        super(handler);
        context=c;

        Log.e("### SettingsContentObserver", "xxx");

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolumeRing = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        previousVolumeNotification = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        previousVolumeMusic = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        previousVolumeAlarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        previousVolumeSystem = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        previousVolumeVoice = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    private int volumeChangeDetect(int volumeStream, int previousVolume, AudioManager audioManager) {

        int currentVolume = audioManager.getStreamVolume(volumeStream);
        Log.e("### SettingsContentObserver", "channel="+volumeStream+" currentVolume="+currentVolume);
        Log.e("### SettingsContentObserver", "channel="+volumeStream+" previousVolume="+previousVolume);

        int delta=previousVolume-currentVolume;

        if(delta>0)
        {
            Log.e("### SettingsContentObserver", "channel="+volumeStream+" Decreased");
            if (!internalChange) {
                if (volumeStream == AudioManager.STREAM_RING)
                    GlobalData.setRingerVolume(context, currentVolume);
                if (volumeStream == AudioManager.STREAM_NOTIFICATION)
                    GlobalData.setRingerVolume(context, currentVolume);
            }
        }
        else if(delta<0)
        {
            Log.e("### SettingsContentObserver", "channel="+volumeStream+" Increased");
            if (!internalChange) {
                if (volumeStream == AudioManager.STREAM_RING)
                    GlobalData.setRingerVolume(context, currentVolume);
                if (volumeStream == AudioManager.STREAM_NOTIFICATION)
                    GlobalData.setRingerVolume(context, currentVolume);
            }
        }
        return currentVolume;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        Log.e("### SettingsContentObserver", "internalChange=" + internalChange);

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        previousVolumeRing = volumeChangeDetect(AudioManager.STREAM_RING, previousVolumeRing, audioManager);
        previousVolumeNotification = volumeChangeDetect(AudioManager.STREAM_NOTIFICATION, previousVolumeNotification, audioManager);
        previousVolumeMusic = volumeChangeDetect(AudioManager.STREAM_MUSIC, previousVolumeMusic, audioManager);
        previousVolumeAlarm = volumeChangeDetect(AudioManager.STREAM_ALARM, previousVolumeAlarm, audioManager);
        previousVolumeSystem = volumeChangeDetect(AudioManager.STREAM_SYSTEM, previousVolumeSystem, audioManager);
        previousVolumeVoice = volumeChangeDetect(AudioManager.STREAM_VOICE_CALL, previousVolumeVoice, audioManager);

        SettingsContentObserver.internalChange = false;

    }

}
