package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;

class SettingsContentObserver  extends ContentObserver {

    //public static boolean internalChange = false;

    private static int previousVolumeRing = 0;
    private static int previousVolumeNotification = 0;
    //private static int previousVolumeMusic = 0;
    //private static int previousVolumeAlarm = 0;
    //private static int previousVolumeSystem = 0;
    //private static int previousVolumeVoice = 0;
    //private int defaultRingerMode = 0;
    private static int previousScreenTimeout = 0;

    static int savedBrightness;
    static float savedAdaptiveBrightness;
    static int savedBrightnessMode;

    private final Context context;

    SettingsContentObserver(Context c, Handler handler) {
        super(handler);

        context=c;

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            previousVolumeRing = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            previousVolumeNotification = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            //previousVolumeMusic = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            //previousVolumeAlarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            //previousVolumeSystem = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
            //previousVolumeVoice = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        }

        savedBrightnessMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
        savedBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
        savedAdaptiveBrightness = Settings.System.getFloat(context.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("[BRSD] SettingsContentObserver.constructor", "brightness mode=" + savedBrightnessMode);
            PPApplication.logE("[BRSD] SettingsContentObserver.constructor", "manual brightness value=" + savedBrightness);
            PPApplication.logE("[BRSD] SettingsContentObserver.constructor", "adaptive brightness value=" + savedAdaptiveBrightness);
        }*/
    }

    /*
    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }
    */

    private int volumeChangeDetect(int volumeStream, int previousVolume, boolean muted, AudioManager audioManager) {
        if (muted)
            return previousVolume;

        try {
            int currentVolume = audioManager.getStreamVolume(volumeStream);
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("SettingsContentObserver.volumeChangeDetect", "channel=" + volumeStream + " currentVolume=" + currentVolume);
                PPApplication.logE("SettingsContentObserver.volumeChangeDetect", "channel=" + volumeStream + " previousVolume=" + previousVolume);
                PPApplication.logE("SettingsContentObserver.volumeChangeDetect", "internalChange=" + RingerModeChangeReceiver.internalChange);
                if (volumeStream == AudioManager.STREAM_RING) {
                    PPApplication.logE("[VOL] SettingsContentObserver.volumeChangeDetect", "currentVolume=" + currentVolume);
                    PPApplication.logE("[VOL] SettingsContentObserver.volumeChangeDetect", "maxVolume=" + audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
                }
            }*/

            int delta = previousVolume - currentVolume;

            if (delta > 0) {
                if (!RingerModeChangeReceiver.internalChange) {
                    if (volumeStream == AudioManager.STREAM_RING) {
                        RingerModeChangeReceiver.notUnlinkVolumes = true;
                        ActivateProfileHelper.setRingerVolume(context, currentVolume);
                        if (PhoneProfilesService.getInstance() != null)
                            PhoneProfilesService.getInstance().ringingVolume = currentVolume;
                    }
                    if (volumeStream == AudioManager.STREAM_NOTIFICATION) {
                        RingerModeChangeReceiver.notUnlinkVolumes = true;
                        ActivateProfileHelper.setNotificationVolume(context, currentVolume);
                        //PhoneProfilesService.notificationVolume = currentVolume;
                    }
                }
            } else if (delta < 0) {
                if (!RingerModeChangeReceiver.internalChange) {
                    if (volumeStream == AudioManager.STREAM_RING) {
                        RingerModeChangeReceiver.notUnlinkVolumes = true;
                        ActivateProfileHelper.setRingerVolume(context, currentVolume);
                        if (PhoneProfilesService.getInstance() != null)
                            PhoneProfilesService.getInstance().ringingVolume = currentVolume;
                    }
                    if (volumeStream == AudioManager.STREAM_NOTIFICATION) {
                        RingerModeChangeReceiver.notUnlinkVolumes = true;
                        ActivateProfileHelper.setNotificationVolume(context, currentVolume);
                        //PhoneProfilesService.notificationVolume = currentVolume;
                    }
                }
            }
            return currentVolume;
        } catch (Exception e) {
            PPApplication.recordException(e);
            return -1;
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        //super.onChange(selfChange);

//        PPApplication.logE("[OBSERVER CALL] SettingsContentObserver.onChange", "uri="+uri);
        /*if (uri != null)
            PPApplication.logE("[OBSERVER CALL] SettingsContentObserver.onChange", "uri="+uri.toString());
        else
            PPApplication.logE("[OBSERVER CALL] SettingsContentObserver.onChange", "without Uri");*/

        boolean okSetting = false;
        if (uri != null) {
            String sUri = uri.toString();
            if (sUri.contains(Settings.System.VOLUME_RING))
                okSetting = true;
            else
            if (sUri.contains(Settings.System.VOLUME_NOTIFICATION))
                okSetting = true;
            //else
            //if (sUri.contains(Settings.System.VOLUME_MUSIC))
            //    okSetting = true;
            else
            if (sUri.contains(Settings.System.SCREEN_BRIGHTNESS_MODE))
                okSetting = true;
            else
            if (sUri.contains(Settings.System.SCREEN_BRIGHTNESS))
                okSetting = true;
            else
            if (sUri.contains(Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ))
                okSetting = true;
            else
            if (sUri.contains(Settings.System.SCREEN_OFF_TIMEOUT))
                okSetting = true;
        }
        else
            okSetting = true;

        //CallsCounter.logCounter(context, "SettingsContentObserver.onChange", "SettingsContentObserver_onChange");

        //PPApplication.logE("[OBSERVER CALL] SettingsContentObserver.onChange", "okSetting="+okSetting);

        if (!okSetting)
            return;

        ////// volume change
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {

            /*if (PPApplication.logEnabled()) {
                //int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                //PPApplication.logE("********** SettingsContentObserver.onChange", "channel=" + AudioManager.STREAM_RING + " currentVolume=" + currentVolume);
                //currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                //PPApplication.logE("********** SettingsContentObserver.onChange", "channel=" + AudioManager.STREAM_NOTIFICATION + " currentVolume=" + currentVolume);
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                PPApplication.logE("[TEST MEDIA VOLUME] SettingsContentObserver.onChange", "STREAM_MUSIC=" + currentVolume);
            }*/

            int audioMode = audioManager.getMode();

            if ((audioMode == AudioManager.MODE_NORMAL) || (audioMode == AudioManager.MODE_RINGTONE)) {
                boolean ringMuted = audioManager.isStreamMute(AudioManager.STREAM_RING);
                boolean notificationMuted = audioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION);

                int newVolumeRing = volumeChangeDetect(AudioManager.STREAM_RING, previousVolumeRing, ringMuted, audioManager);
                int newVolumeNotification = volumeChangeDetect(AudioManager.STREAM_NOTIFICATION, previousVolumeNotification, notificationMuted, audioManager);

                if ((newVolumeRing != -1) && (newVolumeNotification != -1)) {
                    if (((!ringMuted) && (previousVolumeRing != newVolumeRing)) ||
                            ((!notificationMuted) && (previousVolumeNotification != newVolumeNotification))) {
                        // volumes changed

                        if (!(ringMuted || notificationMuted)) {
                            boolean merged = (newVolumeRing == newVolumeNotification) && (previousVolumeRing == previousVolumeNotification);

                            if (!ApplicationPreferences.getSharedPreferences(context).contains(ActivateProfileHelper.PREF_MERGED_RING_NOTIFICATION_VOLUMES)) {
                                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);

                                editor.putBoolean(ActivateProfileHelper.PREF_MERGED_RING_NOTIFICATION_VOLUMES, merged);
                                ApplicationPreferences.prefMergedRingNotificationVolumes = merged;

                                editor.apply();
                            }
                        }
                    }

                    if (!ringMuted)
                        previousVolumeRing = newVolumeRing;
                    if (!notificationMuted)
                        previousVolumeNotification = newVolumeNotification;
                }

            }
            //previousVolumeVoice = volumeChangeDetect(AudioManager.STREAM_VOICE_CALL, previousVolumeVoice, audioManager);
            //int value = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            //PPApplication.logE("[VOL] SettingsContentObserver.onChange", "STREAM_VOICE_CALL="+value);
            //int value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            //PPApplication.logE("[VOL] SettingsContentObserver.onChange", "STREAM_MUSIC="+value);
            //////////////
        }

        ////// screen timeout change
        int screenTimeout = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0);
        if (!ActivateProfileHelper.disableScreenTimeoutInternalChange) {
            if (previousScreenTimeout != screenTimeout) {
                if (Permissions.checkScreenTimeout(context)) {
                    ActivateProfileHelper.setActivatedProfileScreenTimeout(context, 0);
                    /*if (PPApplication.screenTimeoutHandler != null) {
                        PPApplication.screenTimeoutHandler.post(new Runnable() {
                            public void run() {
                                ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(context);
                            }
                        });
                    }*/// else
                    //    ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(context);
                }
            }
        }
        previousScreenTimeout = screenTimeout;

        if (!ActivateProfileHelper.brightnessDialogInternalChange) {
            savedBrightnessMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
            savedBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
            savedAdaptiveBrightness = Settings.System.getFloat(context.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("[BRSD] SettingsContentObserver.onChange", "brightness mode=" + savedBrightnessMode);
                PPApplication.logE("[BRSD] SettingsContentObserver.onChange", "manual brightness value=" + savedBrightness);
                PPApplication.logE("[BRSD] SettingsContentObserver.onChange", "adaptive brightness value=" + savedAdaptiveBrightness);
            }*/
        }

        /*
        if (PPApplication.logEnabled()) {
            int value = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
            PPApplication.logE("[BRS] SettingsContentObserver.onChange", "brightness mode=" + value);
            value = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
            PPApplication.logE("[BRS] SettingsContentObserver.onChange", "manual brightness value=" + value);
            float fValue = Settings.System.getFloat(context.getContentResolver(), ActivateProfileHelper.ADAPTIVE_BRIGHTNESS_SETTING_NAME, -1);
            PPApplication.logE("[BRS] SettingsContentObserver.onChange", "adaptive brightness value=" + fValue);
        }
        */

        /////////////
    }

    @Override
    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

}
