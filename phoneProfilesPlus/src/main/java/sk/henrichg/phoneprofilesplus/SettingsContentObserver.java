package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
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

    static boolean previousIsScreenOn = false;
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
                        synchronized (PPApplication.notUnlinkVolumesMutex) {
                            RingerModeChangeReceiver.notUnlinkVolumes = true;
                        }
                        ActivateProfileHelper.setRingerVolume(context, currentVolume);
                        if (PhoneProfilesService.getInstance() != null)
                            PhoneProfilesService.getInstance().ringingVolume = currentVolume;
                    }
                    if (volumeStream == AudioManager.STREAM_NOTIFICATION) {
                        synchronized (PPApplication.notUnlinkVolumesMutex) {
                            RingerModeChangeReceiver.notUnlinkVolumes = true;
                        }
                        ActivateProfileHelper.setNotificationVolume(context, currentVolume);
                        //PhoneProfilesService.notificationVolume = currentVolume;
                    }
                }
            } else if (delta < 0) {
                if (!RingerModeChangeReceiver.internalChange) {
                    if (volumeStream == AudioManager.STREAM_RING) {
                        synchronized (PPApplication.notUnlinkVolumesMutex) {
                            RingerModeChangeReceiver.notUnlinkVolumes = true;
                        }
                        ActivateProfileHelper.setRingerVolume(context, currentVolume);
                        if (PhoneProfilesService.getInstance() != null)
                            PhoneProfilesService.getInstance().ringingVolume = currentVolume;
                    }
                    if (volumeStream == AudioManager.STREAM_NOTIFICATION) {
                        synchronized (PPApplication.notUnlinkVolumesMutex) {
                            RingerModeChangeReceiver.notUnlinkVolumes = true;
                        }
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

//        PPApplication.logE("[IN_OBSERVER] SettingsContentObserver.onChange", "uri="+uri);
//        PPApplication.logE("[IN_OBSERVER] SettingsContentObserver.onChange", "current thread="+Thread.currentThread());

//        if (uri != null)
//            PPApplication.logE("[TEST BATTERY] SettingsContentObserver.onChange", "uri="+uri.toString());
//        else
//            PPApplication.logE("[TEST BATTERY] SettingsContentObserver.onChange", "without Uri");

        /*if (PPApplication.logEnabled()) {
            //int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            //PPApplication.logE("********** SettingsContentObserver.onChange", "channel=" + AudioManager.STREAM_RING + " currentVolume=" + currentVolume);
            //currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            //PPApplication.logE("********** SettingsContentObserver.onChange", "channel=" + AudioManager.STREAM_NOTIFICATION + " currentVolume=" + currentVolume);
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                PPApplication.logE("[TEST MEDIA VOLUME] SettingsContentObserver.onChange", "STREAM_MUSIC=" + currentVolume);
            }
        }*/

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

        if (!okSetting)
            return;

//        PPApplication.logE("[IN_OBSERVER] SettingsContentObserver.onChange", "------ do onChange ------");

        ////// volume change
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {

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
                //if (Permissions.checkScreenTimeout(context)) {
                    ActivateProfileHelper.setActivatedProfileScreenTimeout(context, 0);
                //}
            }
        }
        previousScreenTimeout = screenTimeout;

        if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
            boolean isScreenOn = false;
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null)
                isScreenOn = pm.isInteractive();
            PPApplication.logE("[BRSD] SettingsContentObserver.onChange", "isScreenOn=" + isScreenOn);
            PPApplication.logE("[BRSD] SettingsContentObserver.onChange", "previousIsScreenOn=" + previousIsScreenOn);
            if (!ActivateProfileHelper.brightnessDialogInternalChange) {
                if (isScreenOn && previousIsScreenOn) {
                    savedBrightnessMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
                    savedBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
                    savedAdaptiveBrightness = Settings.System.getFloat(context.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);
                    if (PPApplication.logEnabled()) {
                        PPApplication.logE("[BRSD] SettingsContentObserver.onChange (1)", "brightness mode=" + savedBrightnessMode);
                        PPApplication.logE("[BRSD] SettingsContentObserver.onChange (1)", "manual brightness value=" + savedBrightness);
                        PPApplication.logE("[BRSD] SettingsContentObserver.onChange (1)", "adaptive brightness value=" + savedAdaptiveBrightness);
                    }
                    previousIsScreenOn = true;
                }
                if (isScreenOn && (!previousIsScreenOn)) {
                    final Context appContext = context.getApplicationContext();
                    PPApplication.startHandlerThreadBroadcast(/*"ScreenOnOffBroadcastReceiver.onReceive"*/);
                    final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    //__handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
                    __handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":SettingsContentObserver_onReceive");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                // reset brightness
                                try {
                                    if (SettingsContentObserver.savedBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                                        Settings.System.putInt(context.getContentResolver(),
                                                Settings.System.SCREEN_BRIGHTNESS_MODE,
                                                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                                        if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS, null, null, false, context).allowed
                                                == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                            Settings.System.putInt(context.getContentResolver(),
                                                    Settings.System.SCREEN_BRIGHTNESS,
                                                    SettingsContentObserver.savedBrightness);
                                            try {
                                                Settings.System.putFloat(context.getContentResolver(),
                                                        Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ,
                                                        SettingsContentObserver.savedAdaptiveBrightness);
                                            } catch (Exception ee) {
                                                ActivateProfileHelper.executeRootForAdaptiveBrightness(
                                                        SettingsContentObserver.savedAdaptiveBrightness,
                                                        context);
                                            }
                                        }
                                    } else {
                                        Settings.System.putInt(context.getContentResolver(),
                                                Settings.System.SCREEN_BRIGHTNESS_MODE,
                                                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                                        Settings.System.putInt(context.getContentResolver(),
                                                Settings.System.SCREEN_BRIGHTNESS,
                                                SettingsContentObserver.savedBrightness);
                                    }
                                } catch (Exception ignored) {
                                }

                                savedBrightnessMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
                                savedBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
                                savedAdaptiveBrightness = Settings.System.getFloat(context.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);
                                if (PPApplication.logEnabled()) {
                                    PPApplication.logE("[BRSD] SettingsContentObserver.onChange (2)", "brightness mode=" + savedBrightnessMode);
                                    PPApplication.logE("[BRSD] SettingsContentObserver.onChange (2)", "manual brightness value=" + savedBrightness);
                                    PPApplication.logE("[BRSD] SettingsContentObserver.onChange (2)", "adaptive brightness value=" + savedAdaptiveBrightness);
                                }
                                previousIsScreenOn = true;

                            } catch (Exception e) {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                                PPApplication.recordException(e);
                            } finally {
                                previousIsScreenOn = true;

                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {
                                    }
                                }
                            }

                        }
                    }, 200);
                }
                if (!isScreenOn)
                    previousIsScreenOn = false;
            }
        } else {
            savedBrightnessMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
            savedBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
            savedAdaptiveBrightness = Settings.System.getFloat(context.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);
            if (PPApplication.logEnabled()) {
                PPApplication.logE("[BRSD] SettingsContentObserver.onChange (1)", "brightness mode=" + savedBrightnessMode);
                PPApplication.logE("[BRSD] SettingsContentObserver.onChange (1)", "manual brightness value=" + savedBrightness);
                PPApplication.logE("[BRSD] SettingsContentObserver.onChange (1)", "adaptive brightness value=" + savedAdaptiveBrightness);
            }
        }

//        if (PPApplication.logEnabled()) {
//            int value = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
//            PPApplication.logE("[BRS] SettingsContentObserver.onChange", "brightness mode=" + value);
//            value = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
//            PPApplication.logE("[BRS] SettingsContentObserver.onChange", "manual brightness value=" + value);
//            float fValue = Settings.System.getFloat(context.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);
//            PPApplication.logE("[BRS] SettingsContentObserver.onChange", "adaptive brightness value=" + fValue);
//        }

        /////////////
    }

    @Override
    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

}
