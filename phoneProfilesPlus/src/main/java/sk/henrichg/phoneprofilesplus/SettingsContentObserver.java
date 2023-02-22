package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

class SettingsContentObserver  extends ContentObserver {

    //public static boolean internalChange = false;

    private static volatile int previousVolumeRing = 0;
    private static volatile int previousVolumeNotification = 0;
    //private static volatile int previousVolumeMusic = 0;
    //private static volatile int previousVolumeAlarm = 0;
    //private static volatile int previousVolumeSystem = 0;
    //private static volatile int previousVolumeVoice = 0;
    //private static volatile int previousVolumeBluetoothCall = 0;
    //private static volatile int previousVolumeDTMFTones = 0;
    //private static volatile int previousVolumeAccessibilityPrompt = 0;

    //private int defaultRingerMode = 0;
    private static volatile int previousScreenTimeout = 0;

    static volatile int savedBrightness;
    static volatile float savedAdaptiveBrightness;
    static volatile int savedBrightnessMode;

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
                //PPApplication.logE("[IN_OBSERVER] SettingsContentObserver.onChange", "uri="+uri);

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
            //////////////
        }
        if (volumeChange) {
            if (!EventPreferencesVolumes.internalChange) {

                if (PPApplication.getApplicationStarted(true, true)) {
                    // application is started

                    if (Event.getGlobalEventsRunning(context)) {

                        // !!! must be used MainWorker with delay and REPLACE, because is often called this onChange
                        // for change volumes
                        Data workData = new Data.Builder()
                                .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_VOLUMES)
                                .build();

                        OneTimeWorkRequest worker =
                                new OneTimeWorkRequest.Builder(MainWorker.class)
                                        .addTag(MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG)
                                        .setInputData(workData)
                                        .setInitialDelay(5, TimeUnit.SECONDS)
                                        //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                        .build();
                        try {
                            if (PPApplication.getApplicationStarted(true, true)) {
                                WorkManager workManager = PPApplication.getWorkManagerInstance();
                                if (workManager != null) {

//                            //if (PPApplication.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}
//
//                            PPApplication.logE("[WORKER_CALL] PhoneProfilesService.doCommand", "xxx");
                                    //workManager.enqueue(worker);
                                    workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                                }
                            }
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }

                        /*
                        final Context appContext = context.getApplicationContext();
                        // handler is not needed because is already used:
                        //PPApplication.settingsContentObserver = new SettingsContentObserver(appContext, new Handler(PPApplication.handlerThreadBroadcast.getLooper()));

                        //PPApplication.startHandlerThreadBroadcast();
                        //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                        //__handler.post(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=SettingsContentObserver.onChange");

                            //Context appContext= appContextWeakRef.get();
                            //if (appContext != null) {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":SettingsContentObserver_onReceive");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

//                                PPApplication.logE("[EVENTS_HANDLER_CALL] SettingsContentObserver.onChange", "sensorType=SENSOR_TYPE_VOLUMES");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_VOLUMES);

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
                        //});
                        //}
                        */

                    }
                }
            }
        }

        ////// screen timeout change
        int screenTimeout = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, -1);
        if (screenTimeout != -1) {
            if (!ActivateProfileHelper.disableScreenTimeoutInternalChange) {
                if (previousScreenTimeout != screenTimeout) {
                    ActivateProfileHelper.setActivatedProfileScreenTimeoutWhenScreenOff(context, 0);
                }
            }
            previousScreenTimeout = screenTimeout;
        }

        if (!ActivateProfileHelper.brightnessDialogInternalChange) {
            savedBrightnessMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
            savedBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
            savedAdaptiveBrightness = Settings.System.getFloat(context.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);

            // TODO this is for log brightness values to log file
            //  use only for check brightness values 0%, 50%, 100% by user,
            //  when in his device brightness not working good
//            PPApplication.logE("SettingsContentObserver.onChange", "savedBrightnessMode="+savedBrightnessMode);
//            PPApplication.logE("SettingsContentObserver.onChange", "savedBrightness="+savedBrightness);
//            PPApplication.logE("SettingsContentObserver.onChange", "savedAdaptiveBrightness="+savedAdaptiveBrightness);
        }

        /////////////
    }

    @Override
    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

}
