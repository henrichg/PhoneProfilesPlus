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
    //private static int previousVolumeBluetoothCall = 0;
    //private static int previousVolumeDTMFTones = 0;
    //private static int previousVolumeAccessibilityPrompt = 0;

    //private int defaultRingerMode = 0;
    private static int previousScreenTimeout = 0;

    static int savedBrightness;
    static float savedAdaptiveBrightness;
    static int savedBrightnessMode;

    private final Context context;

    SettingsContentObserver(Context c, Handler handler) {
        super(handler);

        context=c.getApplicationContext();

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            previousVolumeRing = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            previousVolumeNotification = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            //previousVolumeMusic = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            //previousVolumeAlarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            //previousVolumeSystem = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
            //previousVolumeVoice = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            //previousVolumeBluetoothCall = audioManager.getStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO);
            //previousVolumeDTMFTones = audioManager.getStreamVolume(AudioManager.STREAM_DTMF);
            //previousVolumeAccessibilityPrompt = audioManager.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY);
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

//            PPApplication.logE("[VOLUMES] SettingsContentObserver.volumeChangeDetect", "volumeStream="+volumeStream);
//            PPApplication.logE("[VOLUMES] SettingsContentObserver.volumeChangeDetect", "currentVolume="+currentVolume);
//            PPApplication.logE("[VOLUMES] SettingsContentObserver.volumeChangeDetect", "delta="+delta);

            if (delta > 0) {
//                PPApplication.logE("[VOLUMES] SettingsContentObserver.volumeChangeDetect (1)", "internaChange="+RingerModeChangeReceiver.internalChange);
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
//                PPApplication.logE("[VOLUMES] SettingsContentObserver.volumeChangeDetect (2)", "internaChange="+RingerModeChangeReceiver.internalChange);
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
        boolean volumeChange = false;

        if (uri != null) {
            String sUri = uri.toString();
            if ((sUri.contains(Settings.System.VOLUME_RING)) ||
                (sUri.contains(Settings.System.VOLUME_NOTIFICATION)) ||
                (sUri.contains(Settings.System.VOLUME_MUSIC)) ||
                (sUri.contains(Settings.System.VOLUME_ALARM)) ||
                (sUri.contains(Settings.System.VOLUME_SYSTEM)) ||
                (sUri.contains(Settings.System.VOLUME_VOICE)) ||
                (sUri.contains(Settings.System.VOLUME_BLUETOOTH_SCO)) //||
                //(sUri.contains(Settings.System.VOLUME_DTMF)) || -- not received
                //(sUri.contains(Settings.System.VOLUME_ACCESSIBILITY))) -- not received
            ) {
                okSetting = true;
                volumeChange = true;
            }
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

//        PPApplication.logE("[IN_OBSERVER] SettingsContentObserver.onChange", "uri="+uri);
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
        if (volumeChange) {
            // TODO volume change event sensor
            if (!EventPreferencesVolumes.internalChange) {

                if (PPApplication.getApplicationStarted(true)) {
                    // application is started

                    if (Event.getGlobalEventsRunning()) {
                        //PPApplication.logE("SettingsContentObserver.onChange","xxx");

                        final Context appContext = context.getApplicationContext();
                        PPApplication.startHandlerThreadBroadcast();
                        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                        //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                        //        context.getApplicationContext()) {
                        __handler.post(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=SettingsContentObserver.onChange");

                            //Context appContext= appContextWeakRef.get();
                            //if (appContext != null) {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":CalendarProviderChangedBroadcastReceiver_onReceive");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

//                                PPApplication.logE("[EVENTS_HANDLER_CALL] SettingsContentObserver.onChange", "sensorType=SENSOR_TYPE_VOLUMES");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_VOLUMES);

                                //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=SettingsContentObserver.onChange");
                            } catch (Exception e) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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

        if (!ActivateProfileHelper.brightnessDialogInternalChange) {
            savedBrightnessMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
            savedBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
            savedAdaptiveBrightness = Settings.System.getFloat(context.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);
//            if (PPApplication.logEnabled()) {
//                PPApplication.logE("[BRSD] SettingsContentObserver.onChange (1)", "brightness mode=" + savedBrightnessMode);
//                PPApplication.logE("[BRSD] SettingsContentObserver.onChange (1)", "manual brightness value=" + savedBrightness);
//                PPApplication.logE("[BRSD] SettingsContentObserver.onChange (1)", "adaptive brightness value=" + savedAdaptiveBrightness);
//            }
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
