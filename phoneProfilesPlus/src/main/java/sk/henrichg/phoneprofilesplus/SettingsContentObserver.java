package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
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
        previousScreenTimeout = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, -1);
        PPApplication.savedBrightnessMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
        PPApplication.savedBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
        //savedAdaptiveBrightness = Settings.System.getFloat(context.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);
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
                if (!PPApplication.ringerModeInternalChange) {
                    if (volumeStream == AudioManager.STREAM_RING) {
//                        PPApplicationStatic.logE("[SYNCHRONIZED] SettingsContentObserver.volumeChangeDetect", "(1) PPApplication.notUnlinkVolumesMutex");
                        synchronized (PPApplication.notUnlinkVolumesMutex) {
                            PPApplication.ringerModeNotUnlinkVolumes = true;
                        }
                        ActivateProfileHelper.setRingerVolume(context, currentVolume);
                        PlayRingingNotification.simulatingRingingCallActualRingingVolume = currentVolume;
                    }
                    if (volumeStream == AudioManager.STREAM_NOTIFICATION) {
//                        PPApplicationStatic.logE("[SYNCHRONIZED] SettingsContentObserver.volumeChangeDetect", "(2) PPApplication.notUnlinkVolumesMutex");
                        synchronized (PPApplication.notUnlinkVolumesMutex) {
                            PPApplication.ringerModeNotUnlinkVolumes = true;
                        }
                        ActivateProfileHelper.setNotificationVolume(context, currentVolume);
                        //PhoneProfilesService.notificationVolume = currentVolume;
                    }
                }
            } else if (delta < 0) {
                if (!PPApplication.ringerModeInternalChange) {
                    if (volumeStream == AudioManager.STREAM_RING) {
//                        PPApplicationStatic.logE("[SYNCHRONIZED] SettingsContentObserver.volumeChangeDetect", "(3) PPApplication.notUnlinkVolumesMutex");
                        synchronized (PPApplication.notUnlinkVolumesMutex) {
                            PPApplication.ringerModeNotUnlinkVolumes = true;
                        }
                        ActivateProfileHelper.setRingerVolume(context, currentVolume);
                        PlayRingingNotification.simulatingRingingCallActualRingingVolume = currentVolume;
                    }
                    if (volumeStream == AudioManager.STREAM_NOTIFICATION) {
//                        PPApplicationStatic.logE("[SYNCHRONIZED] SettingsContentObserver.volumeChangeDetect", "(4) PPApplication.notUnlinkVolumesMutex");
                        synchronized (PPApplication.notUnlinkVolumesMutex) {
                            PPApplication.ringerModeNotUnlinkVolumes = true;
                        }
                        ActivateProfileHelper.setNotificationVolume(context, currentVolume);
                        //PhoneProfilesService.notificationVolume = currentVolume;
                    }
                }
            }
            return currentVolume;
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
            return -1;
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
//        PPApplicationStatic.logE("SettingsContentObserver.onChange", "(2)");

        //super.onChange(selfChange);

//        PPApplicationStatic.logE("[IN_OBSERVER] SettingsContentObserver.onChange", "uri="+uri);
//        PPApplicationStatic.logE("[IN_OBSERVER] SettingsContentObserver.onChange", "current thread="+Thread.currentThread());

        boolean okSetting = false;
        boolean volumeChange = false;
        boolean brightnessChange = false;
        boolean screenTimeoutChange = false;

        if (uri != null) {
            String sUri = uri.toString();
            if (sUri.contains(Settings.System.VOLUME_RING) ||
                sUri.contains(Settings.System.VOLUME_NOTIFICATION) ||
                sUri.contains(Settings.System.VOLUME_MUSIC) ||
                sUri.contains(Settings.System.VOLUME_ALARM) ||
                sUri.contains(Settings.System.VOLUME_SYSTEM) ||
                sUri.contains(Settings.System.VOLUME_VOICE) ||
                sUri.contains(Settings.System.VOLUME_BLUETOOTH_SCO) //||
                //(sUri.contains(Settings.System.VOLUME_DTMF)) || -- not received
                //(sUri.contains(Settings.System.VOLUME_ACCESSIBILITY))) -- not received
            ) {
                //PPApplicationStatic.logE("[IN_OBSERVER] SettingsContentObserver.onChange", "uri="+uri);
                okSetting = true;
                volumeChange = true;
            }
            else
            if (sUri.endsWith(Settings.System.SCREEN_BRIGHTNESS_MODE) ||
                sUri.endsWith(Settings.System.SCREEN_BRIGHTNESS) ||
                sUri.contains(Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ)) {
                okSetting = true;
                brightnessChange = true;
            }
            else
            if (sUri.contains(Settings.System.SCREEN_OFF_TIMEOUT)) {
                okSetting = true;
                screenTimeoutChange = true;
            }
        }
        else {
            okSetting = true;
            volumeChange = true;
            brightnessChange = true;
            screenTimeoutChange = true;
        }

        if (!okSetting)
            return;

//        PPApplicationStatic.logE("[IN_OBSERVER] SettingsContentObserver.onChange", "uri="+uri);
//        PPApplicationStatic.logE("[IN_OBSERVER] SettingsContentObserver.onChange", "------ do onChange ------");

        //final boolean _okSetting = okSetting;
        final boolean _volumeChange = volumeChange;
        final boolean _brightnessChange = brightnessChange;
        final boolean _screenTimeoutChange = screenTimeoutChange;

        final Context appContext = context.getApplicationContext();
        Runnable runnable = () -> {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_SettingsContentObserver_onChange);
                    wakeLock.acquire(10 * 60 * 1000);
                }

                if (_volumeChange) {
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager != null) {

                        int audioMode = audioManager.getMode();

                        if ((audioMode == AudioManager.MODE_NORMAL) || (audioMode == AudioManager.MODE_RINGTONE)) {
                            boolean ringMuted = audioManager.isStreamMute(AudioManager.STREAM_RING);
                            boolean notificationMuted = audioManager.isStreamMute(AudioManager.STREAM_NOTIFICATION);

                            int newVolumeRing = volumeChangeDetect(AudioManager.STREAM_RING, previousVolumeRing, ringMuted, audioManager);
                            int newVolumeNotification = volumeChangeDetect(AudioManager.STREAM_NOTIFICATION, previousVolumeNotification, notificationMuted, audioManager);

                            if ((newVolumeRing != -1) && (newVolumeNotification != -1)) {
                            /* commented because this is bad, bad detection of link-unlink.
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
                            */

                                if (!ringMuted)
                                    previousVolumeRing = newVolumeRing;
                                if (!notificationMuted)
                                    previousVolumeNotification = newVolumeNotification;
                            }

                        }
                    }
                    if (!PPApplication.volumesInternalChange) {

                        if (PPApplicationStatic.getApplicationStarted(true, true)) {
                            // application is started

                            if (EventStatic.getGlobalEventsRunning(context)) {

                                // !!! must be used MainWorker with delay and REPLACE, because is often called this onChange
                                // for change volumes
                                Data workData = new Data.Builder()
                                        .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_VOLUMES)
                                        .build();

    //                        PPApplicationStatic.logE("[MAIN_WORKER_CALL] SettingsContentObserver.onChange", " (1) xxxxxxxxxxxxxxxxxxxx");

                                OneTimeWorkRequest worker =
                                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                                .addTag(MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG)
                                                .setInputData(workData)
                                                .setInitialDelay(5, TimeUnit.SECONDS)
                                                //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                                .build();
                                try {
    //                            if (PPApplicationStatic.getApplicationStarted(true, true)) {
                                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                                    if (workManager != null) {

    //                            //if (PPApplicationStatic.logEnabled()) {
    //                            ListenableFuture<List<WorkInfo>> statuses;
    //                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG);
    //                            try {
    //                                List<WorkInfo> workInfoList = statuses.get();
    //                            } catch (Exception ignored) {
    //                            }
    //                            //}
    //
    //                                    PPApplicationStatic.logE("[WORKER_CALL] SettingsContentObserver.onChange", "(1)");
                                        //workManager.enqueue(worker);
                                        workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                                    }
    //                            }
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
                                }
                            }
                        }
                    }
                }

                if (_screenTimeoutChange) {
                    int screenTimeout = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, -1);
                    if (screenTimeout != -1) {
                        if (!PPApplication.disableScreenTimeoutInternalChange) {
                            if (previousScreenTimeout != screenTimeout) {
                                ActivateProfileHelper.setActivatedProfileScreenTimeoutWhenScreenOff(context, 0);
                            }
                        }
                        previousScreenTimeout = screenTimeout;
                    }
                }

                if (_brightnessChange) {
                    if (!PPApplication.brightnessInternalChange) {
                        PPApplication.savedBrightnessMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
                        PPApplication.savedBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
                        //PPApplication.savedAdaptiveBrightness = Settings.System.getFloat(context.getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);

                        //TODO this is for log brightness values to log file
                        //  use only for check brightness values 0%, 50%, 100% by user,
                        //  when in his device brightness not working good
                        //PPApplicationStatic.logE("SettingsContentObserver.onChange", "savedBrightnessMode=" + PPApplication.savedBrightnessMode);
                        //PPApplicationStatic.logE("SettingsContentObserver.onChange", "savedBrightness=" + PPApplication.savedBrightness);
                        //PPApplicationStatic.logE("SettingsContentObserver.onChange", "savedAdaptiveBrightness="+savedAdaptiveBrightness);
                        //PowerManager pm = context.getSystemService(PowerManager.class);
                        //PPApplicationStatic.logE("SettingsContentObserver.onChange", "minimun brightnress="+pm.getMinimumScreenBrightnessSetting());
                        //PPApplicationStatic.logE("SettingsContentObserver.onChange", "maximum brightnress="+pm.getMaximumScreenBrightnessSetting());
                        //PPApplicationStatic.logE("SettingsContentObserver.onChange", "default brightnress="+pm.getDefaultScreenBrightnessSetting());

                        if (PPApplicationStatic.getApplicationStarted(true, true)) {
                            // application is started

                            if (EventStatic.getGlobalEventsRunning(context)) {

    //                        PPApplicationStatic.logE("[BLUETOOTH] SettingsContentObserver.onChange", "%%%%%%%%%%%%%%%%");

                                // !!! must be used MainWorker with delay and REPLACE, because is often called this onChange
                                // for change volumes
                                Data workData = new Data.Builder()
                                        .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_BRIGHTNESS)
                                        .build();

    //                        PPApplicationStatic.logE("[MAIN_WORKER_CALL] SettingsContentObserver.onChange", " (2) xxxxxxxxxxxxxxxxxxxx");

                                OneTimeWorkRequest worker =
                                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                                .addTag(MainWorker.HANDLE_EVENTS_BRIGHTNESS_WORK_TAG)
                                                .setInputData(workData)
                                                .setInitialDelay(5, TimeUnit.SECONDS)
                                                //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                                .build();
                                try {
    //                            if (PPApplicationStatic.getApplicationStarted(true, true)) {
                                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                                    if (workManager != null) {

    //                            //if (PPApplicationStatic.logEnabled()) {
    //                            ListenableFuture<List<WorkInfo>> statuses;
    //                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG);
    //                            try {
    //                                List<WorkInfo> workInfoList = statuses.get();
    //                            } catch (Exception ignored) {
    //                            }
    //                            //}
    //
    //                                PPApplicationStatic.logE("[WORKER_CALL] SettingsContentObserver.onChange", "(2)");
                                        //workManager.enqueue(worker);
                                        workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_BRIGHTNESS_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                                    }
    //                            }
                                } catch (Exception e) {
                                    PPApplicationStatic.recordException(e);
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
//                PPApplicationStatic.logE("[WAKELOCK_EXCEPTION] SettingsContentObserver.onChange", Log.getStackTraceString(e));
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
        PPApplication.basicExecutorPool.submit(runnable);
    }

    @Override
    public void onChange(boolean selfChange) {
//        PPApplicationStatic.logE("SettingsContentObserver.onChange", "(1)");
        onChange(selfChange, null);
    }

}
