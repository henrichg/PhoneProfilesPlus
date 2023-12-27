package sk.henrichg.phoneprofilesplus;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import java.util.Timer;
import java.util.TimerTask;

class PlayRingingNotification
{
    static volatile boolean ringingCallIsSimulating = false;
    static volatile int oldVolumeForRingingSimulation = -1;
    static volatile int simulatingRingingCallActualRingingVolume = 0;
    //static volatile int simulatingRingingCallRingingMuted = 0;

    static volatile  boolean notificationIsPlayed = false;
    static volatile  int oldVolumeForPlayNotificationSound = -1;

    static volatile MediaPlayer ringingMediaPlayer = null;
    static volatile MediaPlayer notificationMediaPlayer = null;
    static volatile  Timer notificationPlayTimer = null;

    static void doSimulatingRingingCall(Intent intent, Context context) {
        if (intent.getBooleanExtra(PhoneProfilesService.EXTRA_SIMULATE_RINGING_CALL, false))
        {
            Context appContext = context.getApplicationContext();

            PlayRingingNotification.ringingCallIsSimulating = false;

            // wait for change of ringer mode+volume by profile activation
            GlobalUtils.sleep(3000);

            int oldRingerMode = intent.getIntExtra(PhoneProfilesService.EXTRA_OLD_RINGER_MODE, 0);
            //int oldSystemRingerMode = intent.getIntExtra(EXTRA_OLD_SYSTEM_RINGER_MODE, 0);
            int oldZenMode = intent.getIntExtra(PhoneProfilesService.EXTRA_OLD_ZEN_MODE, 0);

            int fromSIMSlot = intent.getIntExtra(PhoneProfilesService.EXTRA_CALL_FROM_SIM_SLOT, 0);

            final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);

            // get ringtone configured in system at start call of EventsHanlder.handleEvents()
            String oldRingtone = intent.getStringExtra(PhoneProfilesService.EXTRA_OLD_RINGTONE);
            if (telephonyManager != null) {
                int phoneCount = telephonyManager.getPhoneCount();
                if (phoneCount > 1) {
                    switch (fromSIMSlot) {
                        case 0:
                            oldRingtone = intent.getStringExtra(PhoneProfilesService.EXTRA_OLD_RINGTONE);
                            break;
                        case 1:
                            oldRingtone = intent.getStringExtra(PhoneProfilesService.EXTRA_OLD_RINGTONE_SIM1);
                            break;
                        case 2:
                            oldRingtone = intent.getStringExtra(PhoneProfilesService.EXTRA_OLD_RINGTONE_SIM2);
                            break;
                    }
                }
            }
            if (oldRingtone == null)
                oldRingtone = "";

            // all EXTRA_NEW_% are from mergedProfile in EventsHandler.doEndHandler
            // and are activated by event (look at GlobalUtils.sleep(1500); at start of this method)
            int newRingerMode;
            int newZenMode;
            /*int ringerModeFromProfile = intent.getIntExtra(PhoneProfilesService.EXTRA_NEW_RINGER_MODE, 0);
            if (ringerModeFromProfile != 0) {
                newRingerMode = ringerModeFromProfile;
                newZenMode = Profile.ZENMODE_ALL;
                if (ringerModeFromProfile == Profile.RINGERMODE_ZENMODE) {
                    newZenMode = intent.getIntExtra(PhoneProfilesService.EXTRA_NEW_RINGER_MODE, Profile.ZENMODE_ALL);
                }
            }
            else*/ {
                //newRingerMode = ApplicationPreferences.prefRingerMode;
                //newZenMode = ApplicationPreferences.prefZenMode;
                AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                switch (audioManager.getRingerMode()) {
                    case AudioManager.RINGER_MODE_SILENT:
                        newRingerMode = Profile.RINGERMODE_SILENT;
//                        PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.doSimulatingRingingCall", "newRingerMode=SILENT");
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        newRingerMode = Profile.RINGERMODE_VIBRATE;
//                        PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.doSimulatingRingingCall", "newRingerMode=VIBRATE");
                        break;
                    //case AudioManager.RINGER_MODE_NORMAL:
                    default:
                        newRingerMode = Profile.RINGERMODE_RING;
//                        PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.doSimulatingRingingCall", "newRingerMode=RING");
                        break;
                }
                switch (ActivateProfileHelper.getSystemZenMode(context)) {
                    case ActivateProfileHelper.ZENMODE_ALARMS:
                        newZenMode = Profile.ZENMODE_ALARMS;
//                        PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.doSimulatingRingingCall", "newZenMode=ALARMS");
                        break;
                    case ActivateProfileHelper.ZENMODE_NONE:
                        newZenMode = Profile.ZENMODE_NONE;
//                        PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.doSimulatingRingingCall", "newZenMode=NONE");
                        break;
                    case ActivateProfileHelper.ZENMODE_PRIORITY:
                        newZenMode = Profile.ZENMODE_PRIORITY;
//                        PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.doSimulatingRingingCall", "newZenMode=PRIORITY");
                        break;
                    //case ActivateProfileHelper.ZENMODE_ALL:
                    default:
                        newZenMode = Profile.ZENMODE_ALL;
//                        PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.doSimulatingRingingCall", "newZenMode=ALL");
                        break;
                }
            }

            String phoneNumber = "";
            if (sk.henrichg.phoneprofilesplus.PPExtenderBroadcastReceiver.isEnabled(appContext/*, PPApplication.VERSION_CODE_EXTENDER_7_0*/, true, true
                    /*, "PhoneProfilesService.doSimulatingRingingCall"*/))
                phoneNumber = ApplicationPreferences.prefEventCallPhoneNumber;

            // get ringtone from contact
            String _ringtoneFromContact = "";
            if (!phoneNumber.isEmpty()) {
                try {
                    Uri contactLookup = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                    Cursor contactLookupCursor = appContext.getContentResolver().query(contactLookup, new String[]{ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.CUSTOM_RINGTONE}, null, null, null);
                    if (contactLookupCursor != null) {
                        if (contactLookupCursor.moveToNext()) {
                            _ringtoneFromContact = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.CUSTOM_RINGTONE));
                            if (_ringtoneFromContact == null)
                                _ringtoneFromContact = "";
                        }
                        contactLookupCursor.close();
                    }
                } catch (SecurityException e) {
                    Permissions.grantPlayRingtoneNotificationPermissions(appContext, true);
                    _ringtoneFromContact = "";
                } catch (Exception e) {
                    _ringtoneFromContact = "";
                }
            }

            String _ringtoneFromProfile = "";
            String _ringtoneFromSystem = "";
            if (_ringtoneFromContact.isEmpty()) {
                // ringtone is not from ringing contact

                int ringtoneChangeFromProfile = intent.getIntExtra(PhoneProfilesService.EXTRA_NEW_RINTONE_CHANGE, 0);
                if (ringtoneChangeFromProfile != 0) {
                    String __ringtoneFromProfile = intent.getStringExtra(PhoneProfilesService.EXTRA_NEW_RINGTONE);
                    String[] splits = __ringtoneFromProfile.split(StringConstants.STR_SPLIT_REGEX);
                    if (!splits[0].isEmpty()) {
                        _ringtoneFromProfile = splits[0];
                    }
                }
                if (fromSIMSlot > 0) {
                    if (telephonyManager != null) {
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
                            if (fromSIMSlot == 1) {
                                ringtoneChangeFromProfile = intent.getIntExtra(PhoneProfilesService.EXTRA_NEW_RINTONE_CHANGE_SIM1, 0);
                                if (ringtoneChangeFromProfile != 0) {
                                    String __ringtoneFromProfile = intent.getStringExtra(PhoneProfilesService.EXTRA_NEW_RINGTONE_SIM1);
                                    String[] splits = __ringtoneFromProfile.split(StringConstants.STR_SPLIT_REGEX);
                                    if (!splits[0].isEmpty()) {
                                        _ringtoneFromProfile = splits[0];
                                    }
                                }
                            }
                            if (fromSIMSlot == 2) {
                                ringtoneChangeFromProfile = intent.getIntExtra(PhoneProfilesService.EXTRA_NEW_RINTONE_CHANGE_SIM2, 0);
                                if (ringtoneChangeFromProfile != 0) {
                                    String __ringtoneFromProfile = intent.getStringExtra(PhoneProfilesService.EXTRA_NEW_RINGTONE_SIM2);
                                    String[] splits = __ringtoneFromProfile.split(StringConstants.STR_SPLIT_REGEX);
                                    if (!splits[0].isEmpty()) {
                                        _ringtoneFromProfile = splits[0];
                                    }
                                }
                            }
                        }
                    }
                }

                if (_ringtoneFromProfile.isEmpty()) {
                    // in profile is not change of ringtone
                    // get it from system
                    try {
                        Uri uri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_RINGTONE);
                        if (uri != null)
                            _ringtoneFromSystem = uri.toString();
                        else
                            _ringtoneFromSystem = "";

                        if (fromSIMSlot > 0) {
                            if (telephonyManager != null) {
                                int phoneCount = telephonyManager.getPhoneCount();
                                if (phoneCount > 1) {
                                    if (fromSIMSlot == 1) {
                                        String _uri = ActivateProfileHelper.getRingtoneFromSystem(appContext, 1);
                                        if (_uri != null)
                                            _ringtoneFromSystem = _uri;
                                    }
                                    if (fromSIMSlot == 2) {
                                        String _uri = ActivateProfileHelper.getRingtoneFromSystem(appContext, 2);
                                        if (_uri != null)
                                            _ringtoneFromSystem = _uri;
                                    }
                                }
                            }
                        }

                    } catch (SecurityException e) {
                        Permissions.grantPlayRingtoneNotificationPermissions(appContext, false);
                        _ringtoneFromSystem = "";
                    } catch (Exception e) {
                        _ringtoneFromSystem = "";
                    }
                }
            }

            String newRingtone;
            // PPP do not support chnage of tone in contacts
            // for this contact ringtone has highest priority
            if (!_ringtoneFromContact.isEmpty())
                newRingtone = _ringtoneFromContact;
            else
            if (!_ringtoneFromProfile.isEmpty())
                newRingtone = _ringtoneFromProfile;
            else
                newRingtone = _ringtoneFromSystem;

            if (ActivateProfileHelper.isAudibleRinging(newRingerMode, newZenMode)) {
//                PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.doSimulatingRingingCall", "mewRingerMode, newZenMode audible");

                boolean simulateRinging = false;

                if (ActivateProfileHelper.mustSimulateRinging(oldRingerMode, oldZenMode)) {
//                    PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.doSimulatingRingingCall", "must simulate ringing");
                    simulateRinging = true;
                } //else
//                    PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.doSimulatingRingingCall","not needed to simulate ringing");

                // simulate rnging when in profile is change of tone
                //   removed, because system plays changed ringtone, when simulating is not enabled
                //   by ringerMode, zenMode
                //if (oldRingtone.isEmpty() || (!newRingtone.isEmpty() && !newRingtone.equals(oldRingtone)))
                //    simulateRinging = true;

                if (simulateRinging) {
                    int _ringingVolume;
                    String ringtoneVolumeFromProfile = intent.getStringExtra(PhoneProfilesService.EXTRA_NEW_RINGER_VOLUME);
                    if (ProfileStatic.getVolumeChange(ringtoneVolumeFromProfile)) {
                        _ringingVolume = ProfileStatic.getVolumeValue(ringtoneVolumeFromProfile);
                    }
                    else {
                        _ringingVolume = PlayRingingNotification.simulatingRingingCallActualRingingVolume;
                    }
                    startSimulatingRingingCall(/*stream,*/ newRingtone, _ringingVolume, appContext);
                }
            }

        }
    }

    static void startSimulatingRingingCall(/*int stream,*/ String ringtone, int ringingVolume, Context context) {
        Context appContext = context.getApplicationContext();
        stopSimulatingRingingCall(/*true*/true, appContext);
        if (!PlayRingingNotification.ringingCallIsSimulating) {
            AudioManager audioManager = (AudioManager)appContext.getSystemService(Context.AUDIO_SERVICE);

            //stopSimulatingNotificationTone(true);

            // stop playing notification sound, becaiuse must be played ringtone
            if (PlayRingingNotification.notificationPlayTimer != null) {
                PlayRingingNotification.notificationPlayTimer.cancel();
                PlayRingingNotification.notificationPlayTimer = null;
            }
            if ((PlayRingingNotification.notificationMediaPlayer != null) && PlayRingingNotification.notificationIsPlayed) {
                try {
                    if (PlayRingingNotification.notificationMediaPlayer.isPlaying())
                        PlayRingingNotification.notificationMediaPlayer.stop();
                } catch (Exception e) {
                    //PPApplicationStatic.recordException(e);
                }
                try {
                    PlayRingingNotification.notificationMediaPlayer.release();
                } catch (Exception e) {
                    //PPApplicationStatic.recordException(e);
                }

                PlayRingingNotification.notificationIsPlayed = false;
                PlayRingingNotification.notificationMediaPlayer = null;
            }
            // ----------

            // do not simulate ringing when ring or stream is muted
            if (audioManager != null) {
                if (audioManager.isStreamMute(AudioManager.STREAM_RING)) {
//                    PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.startSimulatingRingingCall", "stream_rin mutted");
                    return;
                }
            }

//            PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.startSimulatingRingingCall", "stream_ring NOT muted");

            if ((ringtone != null) && !ringtone.isEmpty()) {
                PPApplication.volumesInternalChange = true;
                PPApplication.ringerModeInternalChange = true;

                // play repeating: default ringtone with ringing volume level
                try {
                    if (audioManager != null) {
                        audioManager.setMode(AudioManager.MODE_NORMAL);
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

                        PlayRingingNotification.ringingMediaPlayer = new MediaPlayer();

                        /*
                        // mute STREAM_RING, ringtone will be played via STREAM_ALARM
                        PlayRingingNotification.simulatingRingingCallRingingMuted = (audioManager.isStreamMute(AudioManager.STREAM_RING)) ? 1 : -1;
                        if (PlayRingingNotification.simulatingRingingCallRingingMuted == -1) {
                            Log.e("EventsHandler.startSimulatingRingingCall", "mute stream_ring");
                            PPApplication.volumesInternalChange = true;
                            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        }
                        */

                        AudioAttributes attrs = new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build();
                        PlayRingingNotification.ringingMediaPlayer.setAudioAttributes(attrs);

                        PlayRingingNotification.ringingMediaPlayer.setDataSource(appContext, Uri.parse(ringtone));
                        PlayRingingNotification.ringingMediaPlayer.prepare();
                        PlayRingingNotification.ringingMediaPlayer.setLooping(true);

                        PlayRingingNotification.oldVolumeForRingingSimulation = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

                        int maximumRingValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                        int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

                        float percentage = (float) ringingVolume / maximumRingValue * 100.0f;
                        int mediaRingingVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                        PPApplication.volumesInternalChange = true;
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mediaRingingVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

//                        PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.startSimulatingRingingCall", "start simulating");
                        PlayRingingNotification.ringingMediaPlayer.start();

                        PlayRingingNotification.ringingCallIsSimulating = true;
                    }
                } catch (Exception e) {
//                    Log.e("PhoneProfilesService.startSimulatingRingingCall", Log.getStackTraceString(e));
                    PlayRingingNotification.ringingMediaPlayer = null;

                    PPExecutors.scheduleDisableRingerModeInternalChangeExecutor();
                    PPExecutors.scheduleDisableVolumesInternalChangeExecutor();

                    Permissions.grantPlayRingtoneNotificationPermissions(appContext, false);
                }
            }
        }
    }

    // must be static because must be called immediatelly from PhoneCallListener
    static void stopSimulatingRingingCall(/*boolean abandonFocus*/boolean disableInternalChange, Context context) {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        if (PlayRingingNotification.ringingMediaPlayer != null) {
            try {
                if (PlayRingingNotification.ringingMediaPlayer.isPlaying())
                    PlayRingingNotification.ringingMediaPlayer.stop();
            } catch (Exception e) {
                //PPApplicationStatic.recordException(e);
            }
            try {
                PlayRingingNotification.ringingMediaPlayer.release();
            } catch (Exception e) {
                //PPApplicationStatic.recordException(e);
            }
            PlayRingingNotification.ringingMediaPlayer = null;

            try {
                if (PlayRingingNotification.ringingCallIsSimulating) {
//                    PPApplicationStatic.logE("[RINGING_SIMULATION] PlayRingingNotification.stopSimulatingRingingCall", "stop simulating");

                    PPApplication.volumesInternalChange = true;
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, PlayRingingNotification.oldVolumeForRingingSimulation, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    /*if (PlayRingingNotification.simulatingRingingCallRingingMuted == -1) {
                        // ringing was not mutted at start of simulation and was mutted by simuation
                        // result: must be unmutted
                        if (audioManager.isStreamMute(AudioManager.STREAM_RING)) {
                            Log.e("EventsHandler.stopSimulatingRingingCall", "unmute stream_ring");
                            PPApplication.volumesInternalChange = true;
                            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        } else
                            Log.e("EventsHandler.stopSimulatingRingingCall", "NOT muted stream_ring");
                        // 0 = not detected by simulation
                        PlayRingingNotification.simulatingRingingCallRingingMuted = 0;
                    }*/
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }

        }
        PlayRingingNotification.ringingCallIsSimulating = false;

        if (disableInternalChange) {
            PPExecutors.scheduleDisableRingerModeInternalChangeExecutor();
            PPExecutors.scheduleDisableVolumesInternalChangeExecutor();
        }
    }

    /*private void doSimulatingNotificationTone(Intent intent) {
        if (intent.getBooleanExtra(EventsHandler.EXTRA_SIMULATE_NOTIFICATION_TONE, false) &&
                !ringingCallIsSimulating)
        {
            Context context = getApplicationContext();

            notificationToneIsSimulating = false;

            int oldRingerMode = intent.getIntExtra(EventsHandler.EXTRA_OLD_RINGER_MODE, 0);
            int oldSystemRingerMode = intent.getIntExtra(EventsHandler.EXTRA_OLD_SYSTEM_RINGER_MODE, 0);
            int oldZenMode = intent.getIntExtra(EventsHandler.EXTRA_OLD_ZEN_MODE, 0);
            String oldNotificationTone = intent.getStringExtra(EventsHandler.EXTRA_OLD_NOTIFICATION_TONE);
            int newRingerMode = ActivateProfileHelper.getRingerMode(context);
            int newZenMode = ActivateProfileHelper.getZenMode(context);
            String newNotificationTone;

            try {
                Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                if (uri != null)
                    newNotificationTone = uri.toString();
                else
                    newNotificationTone = "";
            } catch (SecurityException e) {
                Permissions.grantPlayRingtoneNotificationPermissions(context, true, false);
                newNotificationTone = "";
            } catch (Exception e) {
                newNotificationTone = "";
            }

            if (ActivateProfileHelper.isAudibleRinging(newRingerMode, newZenMode)) {

                boolean simulateNotificationTone = false;
                int stream = AudioManager.STREAM_NOTIFICATION;

                    if (!(((newRingerMode == 4)) ||
                            ((newRingerMode == 5) && ((newZenMode == 3) || (newZenMode == 6))))) {
                        // actual ringer/zen mode is changed to another then NONE and ONLY_ALARMS
                        // Android 6 - ringerMode=4 = ONLY_ALARMS

                        // test old ringer and zen mode
                        if (((oldRingerMode == 4)) ||
                                ((oldRingerMode == 5) && ((oldZenMode == 3) || (oldZenMode == 6)))) {
                            // old ringer/zen mode is NONE and ONLY_ALARMS
                            simulateNotificationTone = true;
                            stream = AudioManager.STREAM_ALARM;
                        }
                    }

                    if (!simulateNotificationTone) {
                        if (!(((newRingerMode == 4)) ||
                                ((newRingerMode == 5) && (newZenMode == 2)))) {
                            // actual ringer/zen mode is changed to another then PRIORITY
                            // Android 5 - ringerMode=4 = PRIORITY
                            if (((oldRingerMode == 4)) ||
                                    ((oldRingerMode == 5) && (oldZenMode == 2))) {
                                // old ringer/zen mode is PRIORITY
                                simulateNotificationTone = true;
                                if (oldSystemRingerMode == AudioManager.RINGER_MODE_SILENT) {
                                    stream = AudioManager.STREAM_ALARM;
                                }
                                else {
                                    stream = AudioManager.STREAM_NOTIFICATION;
                                    //stream = AudioManager.STREAM_ALARM;
                                }
                            }
                        }
                    }

                if (oldNotificationTone.isEmpty() || (!newNotificationTone.isEmpty() && !newNotificationTone.equals(oldNotificationTone)))
                    simulateNotificationTone = true;

                if (simulateNotificationTone)
                    startSimulatingNotificationTone(stream, newNotificationTone);
            }

        }
    }

    private void startSimulatingNotificationTone(int stream, String notificationTone) {
        stopSimulatingNotificationTone(true);
        if ((!ringingCallIsSimulating) && (!notificationToneIsSimulating)) {
            if (audioManager == null )
                audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            if ((eventNotificationPlayTimer != null) && eventNotificationIsPlayed) {
                eventNotificationPlayTimer.cancel();
                eventNotificationPlayTimer = null;
            }
            if ((eventNotificationMediaPlayer != null) && eventNotificationIsPlayed) {
                if (eventNotificationMediaPlayer.isPlaying())
                    eventNotificationMediaPlayer.stop();
                eventNotificationIsPlayed = false;
                eventNotificationMediaPlayer = null;
            }

            if ((notificationTone != null) && !notificationTone.isEmpty()) {
                RingerModeChangeReceiver.removeAlarm(getApplicationContext());
                RingerModeChangeReceiver.internalChange = true;

                usedNotificationStream = stream;
                // play repeating: default ringtone with ringing volume level
                try {
                    AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    am.setMode(AudioManager.MODE_NORMAL);

                    //int requestType = AudioManager.AUDIOFOCUS_GAIN;
                    int requestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
                    requestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
                    //int result = audioManager.requestAudioFocus(getApplicationContext(), usedNotificationStream, requestType);
                    //if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        notificationMediaPlayer = new MediaPlayer();

                        AudioAttributes attrs = new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build();
                        notificationMediaPlayer.setAudioAttributes(attrs);
                        //notificationMediaPlayer.setAudioStreamType(usedNotificationStream);

                        notificationMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(notificationTone));
                        notificationMediaPlayer.prepare();
                        notificationMediaPlayer.setLooping(false);


                        oldMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

                        int maximumNotificationValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                        int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

                        float percentage = (float) notificationVolume / maximumNotificationValue * 100.0f;
                        mediaNotificationVolume = Math.round(maximumMediaValue / 100.0f * percentage);

                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mediaNotificationVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                        notificationMediaPlayer.start();

                        notificationToneIsSimulating = true;

                        final Context context = getApplicationContext();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                stopSimulatingNotificationTone(true);
                            }
                        }, notificationMediaPlayer.getDuration());

                } catch (SecurityException e) {
                    Log.e("PhoneProfilesService.startSimulatingNotificationTone", " security exception");
                    Permissions.grantPlayRingtoneNotificationPermissions(getApplicationContext(), true, false);
                    notificationMediaPlayer = null;
                    RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());
                } catch (Exception e) {
                    Log.e("PhoneProfilesService.startSimulatingNotificationTone", "exception");
                    notificationMediaPlayer = null;
                    RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());
                }
            }
        }
    }

    public void stopSimulatingNotificationTone(boolean abandonFocus) {
        //if (notificationToneIsSimulating) {
        if (audioManager == null )
            audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        if (notificationMediaPlayer != null) {
            try {
                if (notificationMediaPlayer.isPlaying())
                    notificationMediaPlayer.stop();
            } catch (Exception ignored) {};
            notificationMediaPlayer.release();
            notificationMediaPlayer = null;

            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, oldMediaVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
        //if (abandonFocus)
        //    audioManager.abandonAudioFocus(getApplicationContext());
        //}
        notificationToneIsSimulating = false;
        RingerModeChangeReceiver.setAlarmForDisableInternalChange(getApplicationContext());
    }*/

    static void playNotificationSound(final String notificationSound,
                                      final boolean notificationVibrate,
                                      final boolean playAlsoInSilentMode,
                                      Context context) {

        Context appContext = context.getApplicationContext();

        //final Context appContext = getApplicationContext();
        Runnable runnable = () -> {

            AudioManager audioManager = (AudioManager)appContext.getSystemService(Context.AUDIO_SERVICE);

            //int ringerMode = ApplicationPreferences.prefRingerMode;
            //int zenMode = ApplicationPreferences.prefZenMode;
            //boolean isAudible = ActivateProfileHelper.isAudibleRinging(ringerMode, zenMode/*, false*/);
            int systemZenMode = ActivateProfileHelper.getSystemZenMode(appContext);
            boolean isAudible =
                    ActivateProfileHelper.isAudibleSystemRingerMode(audioManager, systemZenMode/*, getApplicationContext()*/);

            if (notificationVibrate || ((!isAudible) && (!notificationSound.isEmpty()))) {
                // vibrate when is configured or when is not audible and sound is configured

                // why vibrate?
                // 1. vibration is configured by user
                // 2. notification sound is configured by user, but sound mode is not audible
                Vibrator vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
                if ((vibrator != null) && vibrator.hasVibrator()) {
                    try {
                        if (!isAudible) {
                            // sound mode is not audible, force vibrate = Vibration intensity is ignored
                            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            // Vibration intensity is also used
                            if (Build.VERSION.SDK_INT >= 33)
                                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE),
                                        VibrationAttributes.createForUsage(VibrationAttributes.USAGE_NOTIFICATION));
                            else
                                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE),
                                        new AudioAttributes.Builder()
                                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                                .build());
                        }
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
            }

            if ((!PlayRingingNotification.ringingCallIsSimulating)/* && (!notificationToneIsSimulating)*/) {

                stopPlayNotificationSound(false, appContext);

                if (!notificationSound.isEmpty())
                {
                    if (isAudible || playAlsoInSilentMode) {

                        Uri notificationUri = Uri.parse(notificationSound);
                        try {
                            ContentResolver contentResolver = appContext.getContentResolver();
                            appContext.grantUriPermission(PPApplication.PACKAGE_NAME, notificationUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            contentResolver.takePersistableUriPermission(notificationUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            // java.lang.SecurityException: UID 10157 does not have permission to
                            // content://com.android.externalstorage.documents/document/93ED-1CEC%3AMirek%2Fmobil%2F.obr%C3%A1zek%2Fblack.jpg
                            // [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs
                            //Log.e("PhoneProfilesService.playNotificationSound", Log.getStackTraceString(e));
                            //PPApplicationStatic.recordException(e);
                        }

                        try {
                            PlayRingingNotification.notificationMediaPlayer = new MediaPlayer();

                            int usage = AudioAttributes.USAGE_NOTIFICATION;
                            if (!isAudible)
                                usage = AudioAttributes.USAGE_ALARM;

                            AudioAttributes attrs = new AudioAttributes.Builder()
                                    .setUsage(usage)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build();
                            PlayRingingNotification.notificationMediaPlayer.setAudioAttributes(attrs);

                            PlayRingingNotification.notificationMediaPlayer.setDataSource(appContext, notificationUri);
                            PlayRingingNotification.notificationMediaPlayer.prepare();
                            PlayRingingNotification.notificationMediaPlayer.setLooping(false);

                            PPApplication.volumesMediaVolumeChangeed = false;

                            if (!isAudible) {
                                PlayRingingNotification.oldVolumeForPlayNotificationSound = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                                int maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                                int mediaRingingVolume = Math.round(maximumMediaValue / 100.0f * 75.0f);
                                PPApplication.volumesInternalChange = true;
                                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mediaRingingVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                            } else
                                PlayRingingNotification.oldVolumeForPlayNotificationSound = -1;

                            PlayRingingNotification.notificationMediaPlayer.start();

                            PlayRingingNotification.notificationIsPlayed = true;

                            PlayRingingNotification.notificationPlayTimer = new Timer();
                            PlayRingingNotification.notificationPlayTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {

                                    if (PlayRingingNotification.notificationMediaPlayer != null) {
                                        try {
                                            if (PlayRingingNotification.notificationMediaPlayer.isPlaying())
                                                PlayRingingNotification.notificationMediaPlayer.stop();
                                        } catch (Exception e) {
                                            //PPApplicationStatic.recordException(e);
                                        }
                                        try {
                                            PlayRingingNotification.notificationMediaPlayer.release();
                                        } catch (Exception e) {
                                            //PPApplicationStatic.recordException(e);
                                        }

                                        if ((PlayRingingNotification.notificationIsPlayed) && (PlayRingingNotification.oldVolumeForPlayNotificationSound != -1) &&
                                                (!PPApplication.volumesMediaVolumeChangeed)) {
                                            try {
                                                PPApplication.volumesInternalChange = true;
                                                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, PlayRingingNotification.oldVolumeForPlayNotificationSound, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                            } catch (Exception e) {
                                                //PPApplicationStatic.recordException(e);
                                            }
                                        }

                                    }

                                    PlayRingingNotification.notificationIsPlayed = false;
                                    PlayRingingNotification.notificationMediaPlayer = null;
                                    PPApplication.volumesMediaVolumeChangeed = false;

                                    PlayRingingNotification.notificationPlayTimer = null;
                                }
                            }, PlayRingingNotification.notificationMediaPlayer.getDuration());

                        }
                        catch (Exception e) {
                            //Log.e("PhoneProfilesService.playNotificationSound", "exception");
                            stopPlayNotificationSound(true, appContext);

                            Permissions.grantPlayRingtoneNotificationPermissions(appContext, false);
                        }
                    }
                }
            }

        };
        PPApplicationStatic.createPlayToneExecutor();
        PPApplication.playToneExecutor.submit(runnable);
    }

    static void stopPlayNotificationSound(boolean setBackMediaVolume, Context context) {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        if (PlayRingingNotification.notificationPlayTimer != null) {
            PlayRingingNotification.notificationPlayTimer.cancel();
            PlayRingingNotification.notificationPlayTimer = null;
        }
        if ((PlayRingingNotification.notificationMediaPlayer != null) && PlayRingingNotification.notificationIsPlayed) {
            try {
                if (PlayRingingNotification.notificationMediaPlayer.isPlaying())
                    PlayRingingNotification.notificationMediaPlayer.stop();
            } catch (Exception e) {
                //PPApplicationStatic.recordException(e);
            }
            try {
                PlayRingingNotification.notificationMediaPlayer.release();
            } catch (Exception e) {
                //PPApplicationStatic.recordException(e);
            }

            if (setBackMediaVolume) {
                try {
                    if (PlayRingingNotification.notificationIsPlayed) {
                        if ((PlayRingingNotification.oldVolumeForPlayNotificationSound != -1) &&
                                (!PPApplication.volumesMediaVolumeChangeed)) {
                            try {
                                PPApplication.volumesInternalChange = true;
                                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, PlayRingingNotification.oldVolumeForPlayNotificationSound, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                            } catch (Exception e) {
                                //PPApplicationStatic.recordException(e);
                            }
                        }
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }

            PlayRingingNotification.notificationIsPlayed = false;
            PlayRingingNotification.notificationMediaPlayer = null;
        }
        PPApplication.volumesMediaVolumeChangeed = false;
    }

}
