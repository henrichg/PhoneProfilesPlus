package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Build;
import android.telephony.TelephonyManager;


class ProfilePreferencesIndicator {

    final int[] drawables = new int[60];
    final String[] strings = new String[60];
    int countDrawables = 0;

    final String[] preferences = new String[60];
    final int[] countItems = new int[60];
    int countPreferences = 0;

    private Bitmap createIndicatorBitmap(/*Context context,*/ int countDrawables)
    {
        // bitmap to get size
        //Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile_pref_volume_on);

        //Bitmap bmp = BitmapManipulator.getBitmapFromResource(R.drawable.ic_profile_pref_volume_on, false, context);

        //int width  = bmp.getWidth() * countDrawables;
        //int height  = bmp.getHeight();

        //final BitmapFactory.Options opt = new BitmapFactory.Options();
        //opt.inJustDecodeBounds = true;
        //BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile_pref_volume_on, opt);

        //int width = opt.outWidth * countDrawables;
        //int height = opt.outHeight;

        int iconSize = GlobalGUIRoutines.dpToPx(24);
        int width = iconSize * countDrawables;
        //noinspection UnnecessaryLocalVariable
        int height = iconSize;

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    private void addIndicator(int preferenceBitmapResourceID, int index, Context context, Canvas canvas)
    {
        Bitmap preferenceBitmap = BitmapFactory.decodeResource(context.getResources(), preferenceBitmapResourceID);
        //Bitmap preferenceBitmap = BitmapManipulator.getBitmapFromResource(preferenceBitmapResourceID, false, context);

        if (preferenceBitmap != null)
            canvas.drawBitmap(preferenceBitmap, preferenceBitmap.getWidth() * index, 0, null);
        //canvas.save();

    }

    void fillArrays(Profile profile, boolean fillStrings, boolean monochrome, boolean fillPreferences, Context appContext) {
        countDrawables = 0;
        countPreferences = 0;
        if (profile != null)
        {
            if (profile._volumeRingerMode != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean vibrateWhenRingingAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (profile._volumeRingerMode == 5) {
                        // zen mode
                        if (profile._volumeZenMode == 1) {
                            if (fillPreferences) {
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + ": " +
                                        appContext.getString(R.string.array_pref_soundModeArray_ZenModeM) + " = " +
                                        appContext.getString(R.string.array_pref_zenModeArray_off);
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    preferences[countPreferences] = preferences[countPreferences] + ", " +
                                            appContext.getString(R.string.profile_preferences_vibrateWhenRinging);
                            }
                            if (fillStrings) {
                                strings[countDrawables++] = "dnd:off";
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    strings[countDrawables++] = "vibr";
                            }
                            else {
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zen_mode;
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                            }
                            if (fillPreferences) {
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    countItems[countPreferences++] = 2;
                                else
                                    countItems[countPreferences++] = 1;
                            }
                        }
                        if (profile._volumeZenMode == 2) {
                            if (fillPreferences) {
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + ": " +
                                        appContext.getString(R.string.array_pref_soundModeArray_ZenModeM) + " = " +
                                        appContext.getString(R.string.array_pref_zenModeArray_priority);
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    preferences[countPreferences] = preferences[countPreferences] + ", " +
                                            appContext.getString(R.string.profile_preferences_vibrateWhenRinging);
                            }
                            if (fillStrings) {
                                strings[countDrawables++] = "dnd:pri";
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    strings[countDrawables++] = "vibr";
                            }
                            else {
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_priority;
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                            }
                            if (fillPreferences) {
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    countItems[countPreferences++] = 2;
                                else
                                    countItems[countPreferences++] = 1;
                            }
                        }
                        if (profile._volumeZenMode == 3) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + ": " +
                                        appContext.getString(R.string.array_pref_soundModeArray_ZenModeM) + " = " +
                                        appContext.getString(R.string.array_pref_zenModeArray_totalSilence);
                            if (fillStrings)
                                strings[countDrawables++] = "dnd:sln";
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_none;
                            if (fillPreferences)
                                countItems[countPreferences++] = 1;
                        }
                        if (profile._volumeZenMode == 4) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + ": " +
                                        appContext.getString(R.string.array_pref_soundModeArray_ZenModeM) + " = " +
                                        appContext.getString(R.string.array_pref_zenModeArray_offButVibration);
                            if (fillStrings) {
                                strings[countDrawables++] = "dnd:off";
                                strings[countDrawables++] = "vib";
                            }
                            else {
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zen_mode;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                            }
                            if (fillPreferences)
                                countItems[countPreferences++] = 2;
                        }
                        if (profile._volumeZenMode == 5) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + ": " +
                                        appContext.getString(R.string.array_pref_soundModeArray_ZenModeM) + " = " +
                                        appContext.getString(R.string.array_pref_zenModeArray_priorityWithVibration);
                            if (fillStrings) {
                                strings[countDrawables++] = "dnd:pri";
                                strings[countDrawables++] = "vib";
                            }
                            else {
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_priority;
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                            }
                            if (fillPreferences)
                                countItems[countPreferences++] = 2;
                        }
                        if (profile._volumeZenMode == 6) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + ": " +
                                        appContext.getString(R.string.array_pref_soundModeArray_ZenModeM) + " = " +
                                        appContext.getString(R.string.array_pref_zenModeArray_alarms);
                            if (fillStrings)
                                strings[countDrawables++] = "dnd:ala";
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_alarms;
                            if (fillPreferences)
                                countItems[countPreferences++] = 1;
                        }
                    } else {
                        // sound mode sound
                        if ((profile._volumeRingerMode == 1) || (profile._volumeRingerMode == 2)) {
                            if (fillPreferences) {
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + ": " +
                                        appContext.getString(R.string.array_pref_soundModeArray_sound);
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    preferences[countPreferences] = preferences[countPreferences] + ", " +
                                            appContext.getString(R.string.profile_preferences_vibrateWhenRinging);
                            }
                            if (fillStrings) {
                                strings[countDrawables++] = "sond";
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    strings[countDrawables++] = "vibr";
                            }
                            else {
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_volume_on;
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                            }
                            if (fillPreferences) {
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    countItems[countPreferences++] = 2;
                                else
                                    countItems[countPreferences++] = 1;
                            }
                        }
                        // sound mode vibrate
                        if ((profile._volumeRingerMode == 2) || (profile._volumeRingerMode == 3)) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + ": " +
                                        appContext.getString(R.string.array_pref_soundModeArray_vibration);
                            if (fillStrings)
                                strings[countDrawables++] = "vibr";
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                            if (fillPreferences)
                                countItems[countPreferences++] = 1;
                        }
                        // sound mode alarms only
                        if (profile._volumeRingerMode == 4) {
                            if (fillPreferences) {
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSoundMode) + ": " +
                                        appContext.getString(R.string.array_pref_soundModeArray_silentM);
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    preferences[countPreferences] = preferences[countPreferences] + ", " +
                                            appContext.getString(R.string.profile_preferences_vibrateWhenRinging);
                            }
                            if (fillStrings) {
                                strings[countDrawables++] = "alrm";
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    strings[countDrawables++] = "vibr";
                            }
                            else {
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_zenmode_alarms;
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration;
                            }
                            if (fillPreferences) {
                                if (vibrateWhenRingingAllowed && ((profile._vibrateWhenRinging == 1) || (profile._vibrateWhenRinging == 3)))
                                    countItems[countPreferences++] = 2;
                                else
                                    countItems[countPreferences++] = 1;
                            }
                        }
                    }
                }
            }
            // volume level
            if (profile._volumeMuteSound &&
                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                if (fillPreferences)
                    preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeMuteSound) + ": " +
                            appContext.getString(R.string.array_pref_hardwareModeArray_off);
                if (fillStrings)
                    strings[countDrawables++] = "volm";
                else
                    drawables[countDrawables++] = R.drawable.ic_profile_pref_volume_mute;
                if (fillPreferences)
                    countItems[countPreferences++] = 1;

                if (profile.getVolumeAlarmChange() ||
                        profile.getVolumeVoiceChange() ||
                        profile.getVolumeAccessibilityChange() ||
                        profile.getVolumeBluetoothSCOChange()) {
                    if ((Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_ALARM, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_VOICE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumePartial);
                        if (fillStrings)
                            strings[countDrawables++] = "volp";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_volume_level_partial;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            else {
                if (profile.getVolumeAlarmChange() ||
                        profile.getVolumeMediaChange() ||
                        profile.getVolumeNotificationChange() ||
                        profile.getVolumeRingtoneChange() ||
                        profile.getVolumeSystemChange() ||
                        profile.getVolumeVoiceChange() ||
                        profile.getVolumeDTMFChange() ||
                        profile.getVolumeAccessibilityChange() ||
                        profile.getVolumeBluetoothSCOChange()) {
                    if ((Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_ALARM, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_MEDIA, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_RINGTONE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_SYSTEM, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_VOICE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_DTMF, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeAll);
                        if (fillStrings)
                            strings[countDrawables++] = "vola";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_volume_level;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // speaker phone
            if (profile._volumeSpeakerPhone != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._volumeSpeakerPhone == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSpeakerPhone) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "spe:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_speakerphone;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._volumeSpeakerPhone == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_volumeSpeakerPhone) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "spe:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_speakerphone_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_speakerphone_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }

            // sound
            if ((profile._soundRingtoneChange == 1) ||
                    (profile._soundNotificationChange == 1) ||
                    (profile._soundAlarmChange == 1)) {
                if ((Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                        (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                        (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_soundsChange);
                    if (fillStrings)
                        strings[countDrawables++] = "sond";
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_sound;
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }

            if ((Build.VERSION.SDK_INT >= 26) &&
                    (PPApplication.deviceIsSamsung ||
                            (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                            (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI))) {
                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        // sound for sim 1
                        if ((profile._soundRingtoneChangeSIM1 == 1) ||
                                (profile._soundNotificationChangeSIM1 == 1)) {
                            if ((Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                                if (fillPreferences)
                                    preferences[countPreferences] = appContext.getString(R.string.profile_preferences_soundsChangeSIM1);
                                if (fillStrings)
                                    strings[countDrawables++] = "snd1";
                                else
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_sound_sim1;
                                if (fillPreferences)
                                    countItems[countPreferences++] = 1;
                            }
                        }
                        // sound for sim 2
                        if ((profile._soundRingtoneChangeSIM2 == 1) ||
                                (profile._soundNotificationChangeSIM2 == 1)) {
                            if ((Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                                if (fillPreferences)
                                    preferences[countPreferences] = appContext.getString(R.string.profile_preferences_soundsChangeSIM2);
                                if (fillStrings)
                                    strings[countDrawables++] = "snd2";
                                else
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_sound_sim2;
                                if (fillPreferences)
                                    countItems[countPreferences++] = 1;
                            }
                        }
                    }
                }
                if (profile._soundSameRingtoneForBothSIMCards == 1) {
                    if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_soundSameRingtoneForBothSIMCards);
                        if (fillStrings)
                            strings[countDrawables++] = "srbs";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_sound;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }

            // sound on touch
            if (profile._soundOnTouch != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_ON_TOUCH, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._soundOnTouch == 1) || (profile._soundOnTouch == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_soundOnTouch) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "sto:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_sound_on_touch;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._soundOnTouch == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_soundOnTouch) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "sto:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_sound_on_touch_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_sound_on_touch_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // vibration on touch
            if (profile._vibrationOnTouch != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._vibrationOnTouch == 1) || (profile._vibrationOnTouch == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_vibrationOnTouch) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "vto:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration_on_touch;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._vibrationOnTouch == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_vibrationOnTouch) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "vto:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration_on_touch_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_vibration_on_touch_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // dtmf tone when dialing
            if (profile._dtmfToneWhenDialing != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._dtmfToneWhenDialing == 1) || (profile._dtmfToneWhenDialing == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_dtmfToneWhenDialing) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "dtd:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_dtmf_tone_when_dialing;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._dtmfToneWhenDialing == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_dtmfToneWhenDialing) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "dtd:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_dtmf_tone_when_dialing_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_dtmf_tone_when_dialing_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // airplane mode
            if (profile._deviceAirplaneMode != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceAirplaneMode == 1) || (profile._deviceAirplaneMode == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceAirplaneMode) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "arm:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_airplane_mode;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceAirplaneMode == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceAirplaneMode) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "arm:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_airplane_mode_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_airplane_mode_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // auto-sync
            if (profile._deviceAutoSync != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AUTOSYNC, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceAutoSync == 1) || (profile._deviceAutoSync == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceAutosync) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "asy:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_autosync;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceAutoSync == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceAutosync) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "asy:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_autosync_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_autosync_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // on/off sim
            if (Build.VERSION.SDK_INT >= 26) {
                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        if (profile._deviceOnOffSIM1 != 0) {
                            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if ((profile._deviceOnOffSIM1 == 1) || (profile._deviceOnOffSIM1 == 3)) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceOnOff_SIM1) + ": " +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_on);
                                    if (fillStrings)
                                        strings[countDrawables++] = "so1:1";
                                    else
                                        drawables[countDrawables++] = R.drawable.ic_profile_pref_onoff_sim1;
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                                if (profile._deviceOnOffSIM1 == 2) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceOnOff_SIM1) + ": " +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_off);
                                    if (fillStrings)
                                        strings[countDrawables++] = "so1:0";
                                    else {
                                        if (monochrome)
                                            drawables[countDrawables++] = R.drawable.ic_profile_pref_onoff_sim1_off_mono;
                                        else
                                            drawables[countDrawables++] = R.drawable.ic_profile_pref_onoff_sim1_off;
                                    }
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                            }
                        }
                        if (profile._deviceOnOffSIM2 != 0) {
                            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if ((profile._deviceOnOffSIM2 == 1) || (profile._deviceOnOffSIM2 == 3)) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceOnOff_SIM2) + ": " +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_on);
                                    if (fillStrings)
                                        strings[countDrawables++] = "so2:1";
                                    else
                                        drawables[countDrawables++] = R.drawable.ic_profile_pref_onoff_sim2;
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                                if (profile._deviceOnOffSIM2 == 2) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceOnOff_SIM2) + ": " +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_off);
                                    if (fillStrings)
                                        strings[countDrawables++] = "so2:0";
                                    else {
                                        if (monochrome)
                                            drawables[countDrawables++] = R.drawable.ic_profile_pref_onoff_sim2_off_mono;
                                        else
                                            drawables[countDrawables++] = R.drawable.ic_profile_pref_onoff_sim2_off;
                                    }
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                            }
                        }
                    }
                }
            }

            // default sim card
            if (Build.VERSION.SDK_INT >= 26) {
                if (!profile._deviceDefaultSIMCards.equals("0|0|0")) {
                    if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                        if (telephonyManager != null) {
                            int phoneCount = telephonyManager.getPhoneCount();
                            if (phoneCount > 1) {
                                if (fillPreferences)
                                    preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceDefaultSIM);
                                if (fillStrings)
                                    strings[countDrawables++] = "dsim";
                                else
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_defaultsimcards;
                                if (fillPreferences)
                                    countItems[countPreferences++] = 1;
                            }
                        }
                    }
                }
            }

            // mobile data
            if (profile._deviceMobileData != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceMobileData == 1) || (profile._deviceMobileData == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileData_21) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "mda:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceMobileData == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileData_21) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "mda:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= 26) {
                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        if (profile._deviceMobileDataSIM1 != 0) {
                            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if ((profile._deviceMobileDataSIM1 == 1) || (profile._deviceMobileDataSIM1 == 3)) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileData_21_SIM1) + ": " +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_on);
                                    if (fillStrings)
                                        strings[countDrawables++] = "md1:1";
                                    else
                                        drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_sim1;
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                                if (profile._deviceMobileDataSIM1 == 2) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileData_21_SIM1) + ": " +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_off);
                                    if (fillStrings)
                                        strings[countDrawables++] = "md1:0";
                                    else {
                                        if (monochrome)
                                            drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_sim1_off_mono;
                                        else
                                            drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_sim1_off;
                                    }
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                            }
                        }
                        if (profile._deviceMobileDataSIM2 != 0) {
                            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if ((profile._deviceMobileDataSIM2 == 1) || (profile._deviceMobileDataSIM2 == 3)) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileData_21_SIM2) + ": " +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_on);
                                    if (fillStrings)
                                        strings[countDrawables++] = "md2:1";
                                    else
                                        drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_sim2;
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                                if (profile._deviceMobileDataSIM2 == 2) {
                                    if (fillPreferences)
                                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileData_21_SIM2) + ": " +
                                                appContext.getString(R.string.array_pref_hardwareModeArray_off);
                                    if (fillStrings)
                                        strings[countDrawables++] = "md2:0";
                                    else {
                                        if (monochrome)
                                            drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_sim2_off_mono;
                                        else
                                            drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_sim2_off;
                                    }
                                    if (fillPreferences)
                                        countItems[countPreferences++] = 1;
                                }
                            }
                        }
                    }
                }
            }

            // mobile data preferences
            if (profile._deviceMobileDataPrefs == 1) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceMobileDataPrefs);
                    if (fillStrings)
                        strings[countDrawables++] = "mdpr";
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_mobiledata_pref;
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // wifi
            if (profile._deviceWiFi != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceWiFi == 1) || (profile._deviceWiFi == 3) || (profile._deviceWiFi == 4) || (profile._deviceWiFi == 5)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceWiFi) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "wif:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceWiFi == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceWiFi) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "wif:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            if (Build.VERSION.SDK_INT < 30) {
                // wifi AP
                if (profile._deviceWiFiAP != 0) {
                    if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        if ((profile._deviceWiFiAP == 1) || (profile._deviceWiFiAP == 3) || (profile._deviceWiFiAP == 4) || (profile._deviceWiFiAP == 5)) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceWiFiAP) + ": " +
                                        appContext.getString(R.string.array_pref_hardwareModeArray_on);
                            if (fillStrings)
                                strings[countDrawables++] = "wap:1";
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_ap;
                            if (fillPreferences)
                                countItems[countPreferences++] = 1;
                        }
                        if (profile._deviceWiFiAP == 2) {
                            if (fillPreferences)
                                preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceWiFiAP) + ": " +
                                        appContext.getString(R.string.array_pref_hardwareModeArray_off);
                            if (fillStrings)
                                strings[countDrawables++] = "wap:0";
                            else {
                                if (monochrome)
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_ap_off_mono;
                                else
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_ap_off;
                            }
                            if (fillPreferences)
                                countItems[countPreferences++] = 1;
                        }
                    }
                }
            }
            // wifi AP preferences
            if (profile._deviceWiFiAPPrefs == 1) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceWiFiAPPrefs);
                    if (fillStrings)
                        strings[countDrawables++] = "wapr";
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_wifi_ap_pref;
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // connect to ssid
            if (!profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceConnectToSSID);
                    if (fillStrings)
                        strings[countDrawables++] = "ssid";
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_connect_to_ssid;
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // bluetooth
            if (profile._deviceBluetooth != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceBluetooth == 1) || (profile._deviceBluetooth == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceBluetooth) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "blt:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_bluetooth;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceBluetooth == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceBluetooth) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "blt:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_bluetooth_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_bluetooth_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // location mode
            if (profile._deviceLocationMode != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._deviceLocationMode > 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceLocationMode) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "lom:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_location_mode_on;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceLocationMode == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceLocationMode) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "lom:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_location_mode_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_location_mode_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // gps
            if (profile._deviceGPS != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_GPS, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceGPS == 1) || (profile._deviceGPS == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceGPS) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "gps:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_gps_on;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceGPS == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceGPS) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "gps:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_gps_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_gps_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // location settings preferences
            if (profile._deviceLocationServicePrefs == 1) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceLocationServicePrefs);
                    if (fillStrings)
                        strings[countDrawables++] = "lopr";
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_locationsettings_pref;
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // nfc
            if (profile._deviceNFC != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NFC, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceNFC == 1) || (profile._deviceNFC == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceNFC) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "nfc:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_nfc;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceNFC == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceNFC) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "nfc:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_nfc_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_nfc_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // network type
            if (profile._deviceNetworkType != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceNetworkType);
                    if (fillStrings)
                        strings[countDrawables++] = "ntyp";
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_network_type;
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            if (Build.VERSION.SDK_INT >= 26) {
                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        if (profile._deviceNetworkTypeSIM1 != 0) {
                            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if (fillPreferences)
                                    preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceNetworkTypeSIM1);
                                if (fillStrings)
                                    strings[countDrawables++] = "ntp1";
                                else
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_network_type_sim1;
                                if (fillPreferences)
                                    countItems[countPreferences++] = 1;
                            }
                        }
                        if (profile._deviceNetworkTypeSIM2 != 0) {
                            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if (fillPreferences)
                                    preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceNetworkTypeSIM2);
                                if (fillStrings)
                                    strings[countDrawables++] = "ntp2";
                                else
                                    drawables[countDrawables++] = R.drawable.ic_profile_pref_network_type_sim2;
                                if (fillPreferences)
                                    countItems[countPreferences++] = 1;
                            }
                        }
                    }
                }
            }
            // network type prefs
            if (profile._deviceNetworkTypePrefs != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceNetworkTypePrefs);
                    if (fillStrings)
                        strings[countDrawables++] = "ntpr";
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_network_type_pref;
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }

            // screen timeout
            if (profile._deviceScreenTimeout != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceScreenTimeout);
                    if (fillStrings)
                        strings[countDrawables++] = "sctm";
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_timeout;
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // brightness/auto-brightness
            if (profile.getDeviceBrightnessChange()) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    //noinspection IfStatementWithIdenticalBranches
                    if (profile.getDeviceBrightnessAutomatic()) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceBrightness) + ": " +
                                    appContext.getString(R.string.profile_preferences_deviceBrightness_automatic);
                        if (fillStrings)
                            strings[countDrawables++] = "bri:a";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_autobrightness;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    else {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceBrightness) + ": " +
                                    appContext.getString(R.string.profile_preferences_deviceBrightness_manual);
                        if (fillStrings)
                            strings[countDrawables++] = "bri:m";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_brightness;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // auto-rotate
            if (profile._deviceAutoRotate != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                    if (profile._deviceAutoRotate == 6) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceAutoRotation) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "art:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_autorotate_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_autorotate_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    else {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceAutoRotation) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "art:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_autorotate;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
            }
            // screen on permanent
            if (profile._screenOnPermanent != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._screenOnPermanent == 1) || (profile._screenOnPermanent == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceScreenOnPermanent) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "son:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_on_permanent;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._screenOnPermanent == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceScreenOnPermanent) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "son:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_on_permanent_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_on_permanent_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // wallpaper
            if (profile._deviceWallpaperChange == 1) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, null,null,  true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceWallpaperChange);
                    if (fillStrings)
                        strings[countDrawables++] = "walp";
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_wallpaper;
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // lock screen
            if (profile._deviceKeyguard != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_KEYGUARD, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._deviceKeyguard == 1) || (profile._deviceKeyguard == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceKeyguard) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "kgu:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_lockscreen;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._deviceKeyguard == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceKeyguard) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "kgu:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_lockscreen_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_lockscreen_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // lock device
            if (profile._lockDevice != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_LOCK_DEVICE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_lockDevice);
                    if (fillStrings)
                        strings[countDrawables++] = "lock";
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_lock;
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // notification led
            if (profile._notificationLed != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_NOTIFICATION_LED, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._notificationLed == 1) || (profile._notificationLed == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_notificationLed) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "nld:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_notification_led;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._notificationLed == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_notificationLed) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "nld:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_notification_led_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_notification_led_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // heads-up notifications
            if (profile._headsUpNotifications != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._headsUpNotifications == 1) || (profile._headsUpNotifications == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_headsUpNotifications) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "hup:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_heads_up_notifications;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._headsUpNotifications == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_headsUpNotifications) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "hup:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_heads_up_notifications_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_heads_up_notifications_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // always on display
            if (profile._alwaysOnDisplay != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._alwaysOnDisplay == 1) || (profile._alwaysOnDisplay == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_alwaysOnDisplay) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "aod:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_always_on_display;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._alwaysOnDisplay == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_alwaysOnDisplay) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "aod:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_always_on_display_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_always_on_display_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // screen dark mode
            if (profile._screenDarkMode != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SCREEN_DARK_MODE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (profile._screenDarkMode == 1) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_screenDarkMode) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "dkm:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_dark_mode;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._screenDarkMode == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_screenDarkMode) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "dkm:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_dark_mode_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_screen_dark_mode_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }

            // power save mode
            if (profile._devicePowerSaveMode != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._devicePowerSaveMode == 1) || (profile._devicePowerSaveMode == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_devicePowerSaveMode) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "psm:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_power_save_mode;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._devicePowerSaveMode == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_devicePowerSaveMode) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "psm:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_power_save_mode_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_power_save_mode_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // run application
            if (profile._deviceRunApplicationChange == 1) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceRunApplicationsShortcutsChange);
                    if (fillStrings)
                        strings[countDrawables++] = "ruap";
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_run_application;
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // close app applications
            if (profile._deviceCloseAllApplications != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceCloseAllApplications);
                    if (fillStrings)
                        strings[countDrawables++] = "caap";
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_close_all_applications;
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // force stop application
            if (profile._deviceForceStopApplicationChange == 1) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean enabled;
                    if (Build.VERSION.SDK_INT >= 28)
                        enabled = PPPExtenderBroadcastReceiver.isEnabled(appContext, PPApplication.VERSION_CODE_EXTENDER_5_1_3_1);
                    else
                        enabled = PPPExtenderBroadcastReceiver.isEnabled(appContext, PPApplication.VERSION_CODE_EXTENDER_3_0);
                    if (enabled) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_deviceForceStopApplicationsChange);
                        if (fillStrings)
                            strings[countDrawables++] = "fcst";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_force_stop_application;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // generate notification
            if (profile.getGenerateNotificationGenerate()) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_GENERATE_NOTIFICATION, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (fillPreferences)
                        preferences[countPreferences] = appContext.getString(R.string.profile_preferences_generateNotification);
                    if (fillStrings)
                        strings[countDrawables++] = "gent";
                    else
                        drawables[countDrawables++] = R.drawable.ic_profile_pref_generate_notification;
                    if (fillPreferences)
                        countItems[countPreferences++] = 1;
                }
            }
            // camera flash
            if (profile._cameraFlash != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_CAMERA_FLASH, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._cameraFlash == 1) || (profile._cameraFlash == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_cameraFlash) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_on);
                        if (fillStrings)
                            strings[countDrawables++] = "fla:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_camera_flash;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._cameraFlash == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_cameraFlash) + ": " +
                                    appContext.getString(R.string.array_pref_hardwareModeArray_off);
                        if (fillStrings)
                            strings[countDrawables++] = "fla:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_camera_flash_off_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_camera_flash_off;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }

            // disable wifi scanning
            if (profile._applicationDisableWifiScanning != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._applicationDisableWifiScanning == 1) || (profile._applicationDisableWifiScanning == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationDisableWifiScanning) + ": " +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "wfs:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_wifi_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_wifi;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._applicationDisableWifiScanning == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationDisableWifiScanning) + ": " +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "wfs:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_wifi_off;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // disable bluetooth scanning
            if (profile._applicationDisableBluetoothScanning != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._applicationDisableBluetoothScanning == 1) || (profile._applicationDisableBluetoothScanning == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationDisableBluetoothScanning) + ": " +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "bls:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_bluetooth_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_bluetooth;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._applicationDisableBluetoothScanning == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationDisableBluetoothScanning) + ": " +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "bls:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_bluetooth_off;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // disable location scanning
            if (profile._applicationDisableLocationScanning != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._applicationDisableLocationScanning == 1) || (profile._applicationDisableLocationScanning == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationDisableLocationScanning) + ": " +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "los:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_location_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_location;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._applicationDisableLocationScanning == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationDisableLocationScanning) + ": " +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "los:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_location_off;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // disable mobile cell scanning
            if (profile._applicationDisableMobileCellScanning != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._applicationDisableMobileCellScanning == 1) || (profile._applicationDisableMobileCellScanning == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationDisableMobileCellScanning) + ": " +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "mcs:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_mobile_cell_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_mobile_cell;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._applicationDisableMobileCellScanning == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationDisableMobileCellScanning) + ": " +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "mcs:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_mobile_cell_off;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // disable orientation scanning
            if (profile._applicationDisableOrientationScanning != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._applicationDisableOrientationScanning == 1) || (profile._applicationDisableOrientationScanning == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationDisableOrientationScanning) + ": " +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "ors:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_orientation_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_orientation;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._applicationDisableOrientationScanning == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationDisableOrientationScanning) + ": " +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "ors:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_orientation_off;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
            // disable notification scanning
            if (profile._applicationDisableNotificationScanning != 0) {
                if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_NOTIFICATION_SCANNING, null, null, true, appContext).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((profile._applicationDisableNotificationScanning == 1) || (profile._applicationDisableNotificationScanning == 3)) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationDisableNotificationScanning) + ": " +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_disabled);
                        if (fillStrings)
                            strings[countDrawables++] = "nos:0";
                        else {
                            if (monochrome)
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_notification_mono;
                            else
                                drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_notification;
                        }
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                    if (profile._applicationDisableNotificationScanning == 2) {
                        if (fillPreferences)
                            preferences[countPreferences] = appContext.getString(R.string.profile_preferences_applicationDisableNotificationScanning) + ": " +
                                    appContext.getString(R.string.array_pref_applicationDisableScanning_enabled);
                        if (fillStrings)
                            strings[countDrawables++] = "nos:1";
                        else
                            drawables[countDrawables++] = R.drawable.ic_profile_pref_disable_notification_off;
                        if (fillPreferences)
                            countItems[countPreferences++] = 1;
                    }
                }
            }
        }
        else
            countDrawables = -1;
    }

    Bitmap paint(Profile profile, boolean monochrome, Context context)
    {
        Context appContext = context.getApplicationContext();

        //Profile profile = _profile; //Profile.getMappedProfile(_profile, context);

        fillArrays(profile, false, monochrome, false, context);

        Bitmap indicatorBitmap;
        if (countDrawables >= 0)
        {
            if (countDrawables > 0)
            {
                try {
                    indicatorBitmap = createIndicatorBitmap(/*appContext,*/ countDrawables);
                    Canvas canvas = new Canvas(indicatorBitmap);
                    for (int i = 0; i < countDrawables; i++)
                        addIndicator(drawables[i], i, appContext, canvas);
                } catch (Exception e) {
                    indicatorBitmap = null;
                }
            }
            else
                indicatorBitmap = createIndicatorBitmap(/*appContext,*/ 1);
        }
        else
            indicatorBitmap = null;

        return indicatorBitmap;

    }

    private int maxLength;
    private String addIntoIndicator(String indicator, String preference, int maxLineLength)
    {
        String ind = indicator;
        if (maxLineLength > 0) {
            if (ind.length() > maxLength) {
                ind = ind + '\n';
                maxLength += maxLineLength;
            }
            else
                if (!ind.isEmpty()) ind = ind + "-";
        }
        else
            if (!ind.isEmpty()) ind = ind + "-";

        ind = ind + preference;
        return ind;
    }

    String getString(Profile profile, int maxLineLength, Context context) {
        // profile preferences indicator

        Context appContext = context.getApplicationContext();

        fillArrays(profile, true, false, false, appContext);

        String indicator1 = "";
        if (countDrawables > 0) {
            maxLength = maxLineLength;
            for (int i = 0; i < countDrawables; i++)
                indicator1 = addIntoIndicator(indicator1, strings[i], maxLineLength);
        }

        return indicator1;
    }

}
