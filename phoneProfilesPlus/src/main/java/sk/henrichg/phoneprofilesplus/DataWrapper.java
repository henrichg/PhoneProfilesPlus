package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

//import me.drakeet.support.toast.ToastCompat;

public class DataWrapper {

    public final Context context;
    //private boolean forGUI = false;
    private boolean monochrome = false;
    private int monochromeValue = 0xFF;
    private boolean useMonochromeValueForCustomIcon = false;

    boolean profileListFilled = false;
    boolean eventListFilled = false;
    private boolean eventTimelineListFilled = false;
    final List<Profile> profileList = Collections.synchronizedList(new ArrayList<Profile>());
    final List<Event> eventList = Collections.synchronizedList(new ArrayList<Event>());
    private final List<EventTimeline> eventTimelines = Collections.synchronizedList(new ArrayList<EventTimeline>());

    //static final String EXTRA_INTERACTIVE = "interactive";

    DataWrapper(Context _context,
                        //boolean fgui,
                        boolean mono,
                        int monoVal,
                        boolean useMonoValForCustomIcon)
    {
        context = _context.getApplicationContext();

        setParameters(/*fgui, */mono, monoVal, useMonoValForCustomIcon);
    }

    void setParameters(
            //boolean fgui,
            boolean mono,
            int monoVal,
            boolean useMonoValForCustomIcon)
    {
        //forGUI = fgui;
        monochrome = mono;
        monochromeValue = monoVal;
        useMonochromeValueForCustomIcon = useMonoValForCustomIcon;
    }

    private DataWrapper copyDataWrapper() {
        DataWrapper dataWrapper = new DataWrapper(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
        synchronized (profileList) {
            dataWrapper.copyProfileList(this);
        }
        synchronized (eventList) {
            dataWrapper.copyEventList(this);
        }
        synchronized (eventTimelines) {
            dataWrapper.copyEventTimelineList(this);
        }
        return dataWrapper;
    }

    void fillProfileList(boolean generateIcons, boolean generateIndicators)
    {
        synchronized (profileList) {
            if (!profileListFilled)
            {
                profileList.addAll(getNewProfileList(generateIcons, generateIndicators));
                profileListFilled = true;
            }
        }
    }

    List<Profile> getNewProfileList(boolean generateIcons, boolean generateIndicators) {
        List<Profile> newProfileList = DatabaseHandler.getInstance(context).getAllProfiles();

        //if (forGUI)
        //{
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = newProfileList.iterator(); it.hasNext();) {
                Profile profile = it.next();
                if (generateIcons)
                    profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
                if (generateIndicators)
                    profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            }
        //}
        return newProfileList;
    }

    void setProfileList(List<Profile> sourceProfileList)
    {
        synchronized (profileList) {
            if (profileListFilled)
                profileList.clear();
            profileList.addAll(sourceProfileList);
            profileListFilled = true;
        }
    }

    void copyProfileList(DataWrapper fromDataWrapper)
    {
        synchronized (profileList) {
            if (profileListFilled) {
                profileList.clear();
                profileListFilled = false;
            }
            if (fromDataWrapper.profileListFilled) {
                profileList.addAll(fromDataWrapper.profileList);
                profileListFilled = true;
            }
        }
    }

    static Profile getNonInitializedProfile(String name, String icon, int order)
    {
        //noinspection ConstantConditions
        return new Profile(
                name,
                icon + Profile.defaultValuesString.get("prf_pref_profileIcon_withoutIcon"),
                false,
                order,
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_volumeRingerMode")),
                Profile.defaultValuesString.get("prf_pref_volumeRingtone"),
                Profile.defaultValuesString.get("prf_pref_volumeNotification"),
                Profile.defaultValuesString.get("prf_pref_volumeMedia"),
                Profile.defaultValuesString.get("prf_pref_volumeAlarm"),
                Profile.defaultValuesString.get("prf_pref_volumeSystem"),
                Profile.defaultValuesString.get("prf_pref_volumeVoice"),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_soundRingtoneChange")),
                Settings.System.DEFAULT_RINGTONE_URI.toString(),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_soundNotificationChange")),
                Settings.System.DEFAULT_NOTIFICATION_URI.toString(),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_soundAlarmChange")),
                Settings.System.DEFAULT_ALARM_ALERT_URI.toString(),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceAirplaneMode")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceWiFi")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceBluetooth")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceScreenTimeout")),
                Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + Profile.defaultValuesString.get("prf_pref_deviceBrightness_withoutLevel"),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceWallpaperChange")),
                Profile.defaultValuesString.get("prf_pref_deviceWallpaper"),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceMobileData")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceMobileDataPrefs")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceGPS")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceRunApplicationChange")),
                Profile.defaultValuesString.get("prf_pref_deviceRunApplicationPackageName"),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceAutosync")),
                Profile.defaultValuesBoolean.get("prf_pref_showInActivator_notShow"),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceAutoRotation")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceLocationServicePrefs")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_volumeSpeakerPhone")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceNFC")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_duration")),
                  Profile.AFTER_DURATION_DO_RESTART_EVENTS,
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_volumeZenMode")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceKeyguard")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_vibrationOnTouch")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceWiFiAP")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_devicePowerSaveMode")),
                Profile.defaultValuesBoolean.get("prf_pref_askForDuration"),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceNetworkType")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_notificationLed")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_vibrateWhenRinging")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceWallpaperFor")),
                Profile.defaultValuesBoolean.get("prf_pref_hideStatusBarIcon"),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_lockDevice")),
                Profile.defaultValuesString.get("prf_pref_deviceConnectToSSID"),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_applicationDisableWifiScanning")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_applicationDisableBluetoothScanning")),
                Profile.defaultValuesString.get("prf_pref_durationNotificationSound"),
                Profile.defaultValuesBoolean.get("prf_pref_durationNotificationVibrate"),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceWiFiAPPrefs")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_applicationDisableLocationScanning")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_applicationDisableMobileCellScanning")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_applicationDisableOrientationScanning")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_headsUpNotifications")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceForceStopApplicationChange")),
                Profile.defaultValuesString.get("prf_pref_deviceForceStopApplicationPackageName"),
                0,
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceNetworkTypePrefs")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceCloseAllApplications")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_screenDarkMode")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_dtmfToneWhenDialing")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_soundOnTouch")),
                Profile.defaultValuesString.get("prf_pref_volumeDTMF"),
                Profile.defaultValuesString.get("prf_pref_volumeAccessibility"),
                Profile.defaultValuesString.get("prf_pref_volumeBluetoothSCO"),
                Long.parseLong(Profile.defaultValuesString.get("prf_pref_afterDurationProfile")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_alwaysOnDisplay")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_screenOnPermanent")),
                Profile.defaultValuesBoolean.get("prf_pref_volumeMuteSound"),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_deviceLocationMode")),
                Integer.parseInt(Profile.defaultValuesString.get("prf_pref_applicationDisableNotificationScanning"))
            );
    }

    private String getVolumeLevelString(int percentage, int maxValue)
    {
        double dValue = maxValue / 100.0 * percentage;
        return String.valueOf((int)Math.ceil(dValue));
    }

    Profile getPredefinedProfile(int index, boolean saveToDB, Context baseContext) {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int maximumValueRing = 7;
        int maximumValueNotification = 7;
        int maximumValueMusic = 15;
        int maximumValueAlarm = 7;
        //int	maximumValueSystem = 7;
        //int	maximumValueVoiceCall = 7;
        if (audioManager != null) {
            maximumValueRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            maximumValueNotification = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
            maximumValueMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            maximumValueAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            //maximumValueSystem = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
            //maximumValueVoiceCall = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        }

        Profile profile;

        switch (index) {
            case 0:
                profile = getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_home), "ic_profile_home_2", index+1);
                profile._showInActivator = true;
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context)) {
                        profile._volumeRingerMode = Profile.RINGERMODE_ZENMODE;
                        profile._volumeZenMode = Profile.ZENMODE_ALL; // ALL
                        /*if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else
                            profile._volumeRingerMode = 1;*/
                    } else
                        profile._volumeRingerMode = Profile.RINGERMODE_RING;
                //} else
                //    profile._volumeRingerMode = 1;
                profile._volumeRingtone = getVolumeLevelString(71, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(86, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 1;
                //profile._deviceBrightness = "60|0|0|0";
                break;
            case 1:
                profile = getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_outdoor), "ic_profile_outdoors_1", index+1);
                profile._showInActivator = true;
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context)) {
                        profile._volumeRingerMode = Profile.RINGERMODE_ZENMODE;
                        profile._volumeZenMode = Profile.ZENMODE_ALL; // ALL
                        /*if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else
                            profile._volumeRingerMode = 2;*/
                    } else
                        profile._volumeRingerMode = Profile.RINGERMODE_RING;
                //} else
                //    profile._volumeRingerMode = 2;
                profile._volumeRingtone = getVolumeLevelString(100, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(100, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(93, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 2;
                //profile._deviceBrightness = "255|0|0|0";
                break;
            case 2:
                profile = getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_work), "ic_profile_work_5", index+1);
                profile._showInActivator = true;
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context)) {
                        profile._volumeRingerMode = Profile.RINGERMODE_ZENMODE;
                        profile._volumeZenMode = Profile.ZENMODE_ALL_AND_VIBRATE; // ALL with vibration
                        /*if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 4; // ALL with vibration
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 4; // ALL with vibration
                        } else
                            profile._volumeRingerMode = 1;*/
                    } else
                        profile._volumeRingerMode = Profile.RINGERMODE_RING;
                //} else
                //    profile._volumeRingerMode = 1;
                profile._volumeRingtone = getVolumeLevelString(57, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(71, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(57, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 2;
                //profile._deviceBrightness = "60|0|0|0";
                break;
            case 3:
                profile = getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_meeting), "ic_profile_meeting_2", index+1);
                profile._showInActivator = true;
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context)) {
                        profile._volumeRingerMode = Profile.RINGERMODE_ZENMODE;
                        profile._volumeZenMode = Profile.ZENMODE_NONE; // NONE
                        /*if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 3; // NONE
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 3; // NONE
                        } else
                            profile._volumeRingerMode = 4;*/
                    } else
                        profile._volumeRingerMode = Profile.RINGERMODE_SILENT;
                //} else
                //    profile._volumeRingerMode = 4;
                profile._volumeRingtone = getVolumeLevelString(57, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(71, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(57, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 0;
                //profile._deviceBrightness = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|1|1|0";
                break;
            case 4:
                profile = getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_sleep), "ic_profile_sleep", index+1);
                profile._showInActivator = true;
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context)) {
                        profile._volumeRingerMode = Profile.RINGERMODE_ZENMODE;
                        profile._volumeZenMode = Profile.ZENMODE_ALARMS; // ALARMS
                        /*if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 6; // ALARMS
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 3; // NONE
                        } else
                            profile._volumeRingerMode = 4;*/
                    } else
                        profile._volumeRingerMode = Profile.RINGERMODE_SILENT;
                //} else
                //    profile._volumeRingerMode = 4;
                profile._volumeRingtone = getVolumeLevelString(71, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(86, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 0;
                //profile._deviceBrightness = "10|0|0|0";
                break;
            case 5:
                profile = getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_battery_low), "ic_profile_battery_1", index+1);
                profile._showInActivator = false;
                profile._deviceAutoSync = 2;
                profile._deviceMobileData = 2;
                profile._deviceWiFi = 2;
                profile._deviceBluetooth = 2;
                profile._deviceGPS = 2;
                break;
            case 6:
                profile = getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_battery_ok), "ic_profile_battery_3", index+1);
                profile._showInActivator = false;
                profile._deviceAutoSync = 1;
                profile._deviceMobileData = 1;
                profile._deviceWiFi = 1;
                profile._deviceBluetooth = 1;
                profile._deviceGPS = 1;
                break;
            default:
                profile = null;
        }

        if (profile != null) {
            if (saveToDB)
                DatabaseHandler.getInstance(context).addProfile(profile, false);
        }

        return profile;
    }

    void fillPredefinedProfileList(@SuppressWarnings("SameParameterValue") boolean generateIcons,
                                   boolean generateIndicators,
                                   Context baseContext)
    {
        synchronized (profileList) {
            //invalidateProfileList();
            DatabaseHandler.getInstance(context).deleteAllProfiles();

            for (int index = 0; index < 7; index++) {
                Profile profile = getPredefinedProfile(index, true, baseContext);
                if (profile != null)
                    profileList.add(profile);
            }

            fillProfileList(generateIcons, generateIndicators);
        }
    }

    /*void invalidateProfileList()
    {
        synchronized (profileList) {
            if (profileListFilled)
            {
                for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    profile.releaseIconBitmap();
                    profile.releasePreferencesIndicator();
                    it.remove();
                }
            }
            profileListFilled = false;
        }
    }*/

    Profile getActivatedProfileFromDB(boolean generateIcon, boolean generateIndicators)
    {
        Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
        if (/*forGUI &&*/ (profile != null))
        {
            if (generateIcon)
                profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
            if (generateIndicators)
                profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
        }
        return profile;
    }

    public Profile getActivatedProfile(boolean generateIcon, boolean generateIndicators)
    {
        synchronized (profileList) {
            if (profileListFilled) {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    if (profile._checked) {
                        return profile;
                    }
                }
            }
            return getActivatedProfileFromDB(generateIcon, generateIndicators);
        }
    }

    public Profile getActivatedProfile(List<Profile> profileList) {
        if (profileList != null) {
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = profileList.iterator(); it.hasNext();) {
                Profile profile = it.next();
                if (profile._checked)
                    return profile;
            }
        }
        return null;
    }

    void setProfileActive(Profile profile)
    {
        synchronized (profileList) {
            if (!profileListFilled)
                return;

            //PPApplication.logE("$$$ DataWrapper.setProfileActive", "xxx");

            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = profileList.iterator(); it.hasNext();) {
                Profile _profile = it.next();
                _profile._checked = false;
            }

            if (profile != null) {
                profile._checked = true;
                //PPApplication.logE("DataWrapper.setProfileActive", "profile._name="+profile._name);
                PPApplication.setLastActivatedProfile(context, profile._id);
            }
            else {
                //PPApplication.logE("DataWrapper.setProfileActive", "profile=null");
                PPApplication.setLastActivatedProfile(context, 0);
            }

            //PPApplication.logE("$$$ DataWrapper.setProfileActive", "PPApplication.prefLastActivatedProfile="+PPApplication.prefLastActivatedProfile);
        }
    }

    void activateProfileFromEvent(long profile_id, boolean manualActivation, boolean merged, boolean forRestartEvents)
    {
        int startupSource = PPApplication.STARTUP_SOURCE_SERVICE;
        if (manualActivation)
            startupSource = PPApplication.STARTUP_SOURCE_SERVICE_MANUAL;
        Profile profile = getProfileById(profile_id, false, false, merged);
        if (profile == null)
            return;
        //if (Permissions.grantProfilePermissions(context, profile, merged, true,
        //        /*false, monochrome, monochromeValue,*/
        //        startupSource, false,true, false)) {
        if (!EditorProfilesActivity.displayPreferencesErrorNotification(profile, null, context)) {
            //PPApplication.logE("&&&&&&& DataWrapper.activateProfileFromEvent", "called is DataWrapper._activateProfile()");
            _activateProfile(profile, merged, startupSource, forRestartEvents);
        }
    }

    private Profile getProfileByIdFromDB(long id, boolean generateIcon, boolean generateIndicators, boolean merged)
    {
        Profile profile = DatabaseHandler.getInstance(context).getProfile(id, merged);
        if (/*forGUI &&*/ (profile != null))
        {
            if (generateIcon)
                profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
            if (generateIndicators)
                profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
        }
        return profile;
    }

    public Profile getProfileById(long id, boolean generateIcon, boolean generateIndicators, boolean merged)
    {
        synchronized (profileList) {
            if ((!profileListFilled) || merged) {
                return getProfileByIdFromDB(id, generateIcon, generateIndicators, merged);
            } else {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    if (profile._id == id)
                        return profile;
                }
                // when filter is set and profile not found, get profile from db
                return getProfileByIdFromDB(id, generateIcon, generateIndicators, false);
            }
        }
    }

    void updateProfile(Profile profile)
    {
        if (profile != null)
        {
            Profile origProfile = getProfileById(profile._id, false, false, false);
            if (origProfile != null)
                origProfile.copyProfile(profile);
        }
    }

    void deleteProfile(Profile profile)
    {
        if (profile == null)
            return;

        synchronized (profileList) {
            // remove notifications about profile parameters errors
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(
                    PPApplication.DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG+"_"+profile._id,
                    PPApplication.PROFILE_ID_NOTIFICATION_ID + (int) profile._id);

            profileList.remove(profile);
        }
        synchronized (eventList) {
            fillEventList();
            // unlink profile from events
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                if (event._fkProfileStart == profile._id)
                    event._fkProfileStart = 0;
                if (event._fkProfileEnd == profile._id)
                    event._fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;

                String oldFkProfiles = event._startWhenActivatedProfile;
                if (!oldFkProfiles.isEmpty()) {
                    String[] splits = oldFkProfiles.split("\\|");
                    StringBuilder newFkProfiles = new StringBuilder();
                    for (String split : splits) {
                        long fkProfile = Long.parseLong(split);
                        if (fkProfile != profile._id) {
                            if (newFkProfiles.length() > 0)
                                newFkProfiles.append("|");
                            newFkProfiles.append(split);
                        }
                    }
                    event._startWhenActivatedProfile = newFkProfiles.toString();
                }
            }
        }
        // unlink profile from Background profile
        if (ApplicationPreferences.applicationDefaultProfile == profile._id)
        {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE));
            editor.apply();
            ApplicationPreferences.applicationDefaultProfile(context);
        }
    }

    void deleteAllProfiles()
    {
        synchronized (profileList) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            // remove notifications about profile parameters errors
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                Profile profile = it.next();
                try {
                    notificationManager.cancel(
                            PPApplication.DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG+"_"+profile._id,
                            PPApplication.PROFILE_ID_NOTIFICATION_ID + (int) profile._id);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
            profileList.clear();
        }
        synchronized (eventList) {
            fillEventList();
            // unlink profiles from events
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                event._fkProfileStart = 0;
                event._fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;
                event._startWhenActivatedProfile = "";
            }
        }
        // unlink profiles from Background profile
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putString(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE));
        editor.apply();
        ApplicationPreferences.applicationDefaultProfile(context);
    }

    void refreshProfileIcon(Profile profile,
                            @SuppressWarnings("SameParameterValue") boolean generateIcon,
                            boolean generateIndicators) {
        if (profile != null) {
            boolean isIconResourceID = profile.getIsIconResourceID();
            String iconIdentifier = profile.getIconIdentifier();
            DatabaseHandler.getInstance(context).getProfileIcon(profile);
            if (isIconResourceID && iconIdentifier.equals("ic_profile_default") && (!profile.getIsIconResourceID())) {
                if (generateIcon)
                    profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
                if (generateIndicators)
                    profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private ShortcutInfo createShortcutInfo(Profile profile, boolean restartEvents) {
        boolean isIconResourceID;
        String iconIdentifier;
        Bitmap profileBitmap;
        boolean useCustomColor;

        Intent shortcutIntent;

        isIconResourceID = profile.getIsIconResourceID();
        iconIdentifier = profile.getIconIdentifier();
        useCustomColor = profile.getUseCustomColorForIcon();

        if (isIconResourceID) {
            if (profile._iconBitmap != null)
                profileBitmap = profile._iconBitmap;
            else {
                //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.PPApplication.PACKAGE_NAME);
                int iconResource = Profile.getIconResource(iconIdentifier);
                //profileBitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
                profileBitmap = BitmapManipulator.getBitmapFromResource(iconResource, true, context);
            }
        } else {
            int height = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
            int width = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
            //Log.d("---- ShortcutCreatorListFragment.generateIconBitmap","resampleBitmapUri");
            profileBitmap = BitmapManipulator.resampleBitmapUri(iconIdentifier, width, height, true, false, context.getApplicationContext());
            if (profileBitmap == null) {
                int iconResource = R.drawable.ic_profile_default;
                //profileBitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
                profileBitmap = BitmapManipulator.getBitmapFromResource(iconResource, true, context);
            }
        }

        if (ApplicationPreferences.applicationWidgetIconColor.equals("1")) {
            if (isIconResourceID || useCustomColor) {
                // icon is from resource or colored by custom color
                int monochromeValue = 0xFF;
                String applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness;
                if (applicationWidgetIconLightness.equals("0")) monochromeValue = 0x00;
                if (applicationWidgetIconLightness.equals("25")) monochromeValue = 0x40;
                if (applicationWidgetIconLightness.equals("50")) monochromeValue = 0x80;
                if (applicationWidgetIconLightness.equals("75")) monochromeValue = 0xC0;
                //if (applicationWidgetIconLightness.equals("100")) monochromeValue = 0xFF;
                profileBitmap = BitmapManipulator.monochromeBitmap(profileBitmap, monochromeValue/*, getActivity().getBaseContext()*/);
            } else {
                float monochromeValue = 255f;
                String applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness;
                if (applicationWidgetIconLightness.equals("0")) monochromeValue = -255f;
                if (applicationWidgetIconLightness.equals("25")) monochromeValue = -128f;
                if (applicationWidgetIconLightness.equals("50")) monochromeValue = 0f;
                if (applicationWidgetIconLightness.equals("75")) monochromeValue = 128f;
                //if (applicationWidgetIconLightness.equals("100")) monochromeValue = 255f;
                profileBitmap = BitmapManipulator.grayScaleBitmap(profileBitmap);
                profileBitmap = BitmapManipulator.setBitmapBrightness(profileBitmap, monochromeValue);
            }
        }

        if (restartEvents) {
            /*shortcutIntent = new Intent(context.getApplicationContext(), ActionForExternalApplicationActivity.class);
            shortcutIntent.setAction(ActionForExternalApplicationActivity.ACTION_RESTART_EVENTS);*/
            shortcutIntent = new Intent(context.getApplicationContext(), BackgroundActivateProfileActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            shortcutIntent.addCategory(Intent.ACTION_DEFAULT);
            shortcutIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
            shortcutIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
        }
        else {
            shortcutIntent = new Intent(context.getApplicationContext(), BackgroundActivateProfileActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            shortcutIntent.addCategory(Intent.ACTION_DEFAULT);
            shortcutIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
            shortcutIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        }

        String id;
        String profileName;
        String longLabel;

        if (restartEvents) {
            id = "restart_events";
            profileName = context.getString(R.string.menu_restart_events);
        }
        else {
            id = "profile_" + profile._id;
            profileName = profile._name;
        }
        longLabel = profileName;
        if (profileName.isEmpty())
            profileName = " ";
        if (longLabel.isEmpty())
            longLabel = " ";

        return new ShortcutInfo.Builder(context, id)
                .setShortLabel(profileName)
                .setLongLabel(longLabel)
                .setIcon(Icon.createWithBitmap(profileBitmap))
                .setIntent(shortcutIntent)
                .build();
    }

    void setDynamicLauncherShortcuts() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            try {
                ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

                if (shortcutManager != null) {
                    final int limit = 4;

                    List<Profile> countedProfiles = DatabaseHandler.getInstance(context).getProfilesForDynamicShortcuts(true/*, limit*/);
                    List<Profile> notCountedProfiles = DatabaseHandler.getInstance(context).getProfilesForDynamicShortcuts(false/*, limit*/);

                    ArrayList<ShortcutInfo> shortcuts = new ArrayList<>();

                    Profile _profile = DataWrapper.getNonInitializedProfile(context.getString(R.string.menu_restart_events), "ic_list_item_events_restart_color|1|0|0", 0);
                    _profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
                    // first profile is restart events
                    shortcuts.add(createShortcutInfo(_profile, true));

                    for (Profile profile : countedProfiles) {
                        //PPApplication.logE("DataWrapper.setDynamicLauncherShortcuts", "countedProfile=" + profile._name);
                        profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
                        shortcuts.add(createShortcutInfo(profile, false));
                    }

                    int shortcutsCount = countedProfiles.size();
                    if (shortcutsCount < limit) {
                        for (Profile profile : notCountedProfiles) {
                            //PPApplication.logE("DataWrapper.setDynamicLauncherShortcuts", "notCountedProfile=" + profile._name);
                            profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
                            shortcuts.add(createShortcutInfo(profile, false));

                            ++shortcutsCount;
                            if (shortcutsCount == limit)
                                break;
                        }
                    }

                    if (shortcuts.size() > 0)
                        shortcutManager.setDynamicShortcuts(shortcuts);
                }
            } catch (Exception e) {
                //Log.e("DataWrapper.setDynamicLauncherShortcuts", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    void setDynamicLauncherShortcutsFromMainThread()
    {
        //PPApplication.logE("DataWrapper.setDynamicLauncherShortcutsFromMainThread", "start");
        final DataWrapper dataWrapper = copyDataWrapper();

        PPApplication.startHandlerThread(/*"DataWrapper.setDynamicLauncherShortcutsFromMainThread"*/);
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_setDynamicLauncherShortcutsFromMainThread");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=DataWrapper.setDynamicLauncherShortcutsFromMainThread");

                    dataWrapper.setDynamicLauncherShortcuts();

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=DataWrapper.setDynamicLauncherShortcutsFromMainThread");
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

//---------------------------------------------------

    void fillEventList()
    {
        synchronized (eventList) {
            if (!eventListFilled) {
                eventList.addAll(DatabaseHandler.getInstance(context).getAllEvents());
                eventListFilled = true;
            }
        }
    }

    /*
    void setEventList(List<Event> sourceEventList) {
        synchronized (eventList) {
            if (eventListFilled)
                eventList.clear();
            eventList.addAll(sourceEventList);
            eventListFilled = true;
        }
    }
    */

    void copyEventList(DataWrapper fromDataWrapper) {
        synchronized (eventList) {
            if (eventListFilled) {
                eventList.clear();
                eventListFilled = false;
            }
            if (fromDataWrapper.eventListFilled) {
                eventList.addAll(fromDataWrapper.eventList);
                eventListFilled = true;
            }
        }
    }

    /*void invalidateEventList()
    {
        synchronized (eventList) {
            if (eventListFilled)
                eventList.clear();
            eventListFilled = false;
        }
    }*/

    Event getEventById(long id)
    {
        synchronized (eventList) {
            if (eventListFilled) {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    if (event._id == id)
                        return event;
                }

                // when filter is set and profile not found, get profile from db
            }
            return DatabaseHandler.getInstance(context).getEvent(id);
        }
    }

    long getEventIdByName(String name, @SuppressWarnings("SameParameterValue") boolean fromDB)
    {
        String _name = name.trim();
        if ((!eventListFilled) || fromDB)
        {
            return DatabaseHandler.getInstance(context).getEventIdByName(_name);
        }
        else
        {
            synchronized (eventList) {
                Event event;
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                    event = it.next();
                    if (event._name.trim().equals(_name))
                        return event._id;
                }
            }
            return 0;
        }
    }

    void updateEvent(Event event)
    {
        if (event != null)
        {
            Event origEvent = getEventById(event._id);
            if (origEvent != null)
                origEvent.copyEvent(event);
        }
    }

    // stops all events associated with profile
    private void stopEventsForProfile(Profile profile, boolean alsoUnlink/*, boolean saveEventStatus*/)
    {
        getEventTimelineList(true);

        synchronized (eventList) {
            fillEventList();
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                //if ((event.getStatusFromDB(this) == Event.ESTATUS_RUNNING) &&
                //	(event._fkProfileStart == profile._id))
                if (event._fkProfileStart == profile._id)
                    event.stopEvent(this, false, true, true/*saveEventStatus*/, false, true);
            }
        }
        if (alsoUnlink) {
            unlinkEventsFromProfile(profile);
            DatabaseHandler.getInstance(context).unlinkEventsFromProfile(profile);
        }
        //PPApplication.logE("$$$ restartEvents", "from DataWrapper.stopEventsForProfile");
        //restartEvents(false, true, true, true, true);
        //PPApplication.logE("*********** restartEvents", "from DataWrapper.stopEventsForProfile()");
        restartEventsWithRescan(true, false, true, false, true, false);
    }

    void stopEventsForProfileFromMainThread(final Profile profile,
                                            @SuppressWarnings("SameParameterValue") final boolean alsoUnlink) {
        final DataWrapper dataWrapper = copyDataWrapper();

        PPApplication.startHandlerThread(/*"DataWrapper.stopEventsForProfileFromMainThread"*/);
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_stopEventsForProfileFromMainThread");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=DataWrapper.stopEventsForProfileFromMainThread");

                    dataWrapper.stopEventsForProfile(profile, alsoUnlink);

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=DataWrapper.stopEventsForProfileFromMainThread");
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

    // pauses all events
    void pauseAllEvents(boolean noSetSystemEvent, boolean blockEvents/*, boolean activateReturnProfile*/)
    {
        getEventTimelineList(true);

        synchronized (eventList) {
            //PPApplication.logE("DataWrapper.pauseAllEvents", "eventListFilled="+eventListFilled);
            fillEventList();
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                if (event != null) {
                    int status = event.getStatusFromDB(context);
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("DataWrapper.pauseAllEvents", "event._name=" + event._name);
                        PPApplication.logE("DataWrapper.pauseAllEvents", "status=" + status);
                        PPApplication.logE("DataWrapper.pauseAllEvents", "event._forceRun=" + event._forceRun);
                        PPApplication.logE("DataWrapper.pauseAllEvents", "event._noPauseByManualActivation=" + event._noPauseByManualActivation);
                    }*/

                    if (status == Event.ESTATUS_RUNNING) {
                        if (!(event._forceRun && event._noPauseByManualActivation)) {
                            event.pauseEvent(this, false, true, noSetSystemEvent, true, null, false, false, true);
                        }
                    }

                    setEventBlocked(event, false);
                    if (blockEvents && (status == Event.ESTATUS_RUNNING) && event._forceRun) {
                        // block only running forceRun events
                        if (!event._noPauseByManualActivation)
                            setEventBlocked(event, true);
                    }

                    if (!(event._forceRun && event._noPauseByManualActivation)) {
                        // for "push" events, set startTime to 0
                        clearSensorsStartTime(event, true);
                    }
                }
            }
        }

        // blockEvents == true -> manual profile activation is set
        Event.setEventsBlocked(context, blockEvents);
    }

    private void pauseAllEventsForGlobalStopEvents() {
        final DataWrapper dataWrapper = copyDataWrapper();

        PPApplication.startHandlerThread(/*"DataWrapper.pauseAllEventsForGlobalStopEvents"*/);
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_pauseAllEventsFromMainThread");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=DataWrapper.pauseAllEventsForGlobalStopEvents");

                    dataWrapper.pauseAllEvents(true, false);

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=DataWrapper.pauseAllEventsForGlobalStopEvents");
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

    // stops all events
    void stopAllEvents(boolean saveEventStatus, boolean alsoDelete, boolean log, boolean updateGUI)
    {
        getEventTimelineList(true);
        synchronized (eventList) {
            fillEventList();
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                //if (event.getStatusFromDB(this) != Event.ESTATUS_STOP)
                event.stopEvent(this, false/*activateReturnProfile*/,
                        true, saveEventStatus, log, updateGUI);
            }
        }
        if (alsoDelete) {
            unlinkAllEvents();
            DatabaseHandler.getInstance(context).deleteAllEvents();
        }
    }

    void stopAllEventsFromMainThread(@SuppressWarnings("SameParameterValue") final boolean saveEventStatus,
                                     final boolean alsoDelete) {
        final DataWrapper dataWrapper = copyDataWrapper();

        PPApplication.startHandlerThread(/*"DataWrapper.stopAllEventsFromMainThread"*/);
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_stopAllEventsFromMainThread");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=DataWrapper.stopAllEventsFromMainThread");

                    dataWrapper.stopAllEvents(saveEventStatus, alsoDelete, true, true);

                    //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=DataWrapper.stopAllEventsFromMainThread");
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });
    }

    private void unlinkEventsFromProfile(Profile profile)
    {
        synchronized (eventList) {
            fillEventList();
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                if (event._fkProfileStart == profile._id)
                    event._fkProfileStart = 0;
                if (event._fkProfileEnd == profile._id)
                    event._fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;
            }
        }
    }

    private void unlinkAllEvents()
    {
        synchronized (eventList) {
            fillEventList();
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                event._fkProfileStart = 0;
                event._fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;
            }
        }
    }

    void activateProfileOnBoot()
    {
        if (ApplicationPreferences.applicationActivate)
        {
            /*Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
            long profileId;
            if (profile != null)
                profileId = profile._id;
            else
            {
                profileId = Long.valueOf(ApplicationPreferences.applicationDefaultProfile(context));
                if (profileId == Profile.PROFILE_NO_ACTIVATE)
                    profileId = 0;
            }
            */
            long profileId = PPApplication.prefLastActivatedProfile;
            //PPApplication.logE("DataWrapper.activateProfileOnBoot", "lastActivatedProfile="+profileId);
            if (profileId == 0) {
                profileId = ApplicationPreferences.applicationDefaultProfile;
                if (profileId == Profile.PROFILE_NO_ACTIVATE)
                    profileId = 0;
            }

            activateProfile(profileId, PPApplication.STARTUP_SOURCE_BOOT, null, true);
        }
        else
            activateProfile(0, PPApplication.STARTUP_SOURCE_BOOT, null, true);
    }

    private void startEventsOnBoot(boolean startedFromService, boolean useHandler)
    {
        if (startedFromService) {
            if (ApplicationPreferences.applicationStartEvents) {
                //restartEvents(false, false, true, false, useHandler);
                //PPApplication.logE("*********** restartEvents", "from DataWrapper.startEventsOnBoot() - 1");
                restartEventsWithRescan(true, false, useHandler, false, false, false);
            }
            else {
                Event.setGlobalEventsRunning(context, false);
                activateProfileOnBoot();
            }
        }
        else {
            //restartEvents(false, false, true, false, useHandler);
            //PPApplication.logE("*********** restartEvents", "from DataWrapper.startEventsOnBoot() - 2");
            restartEventsWithRescan(true, false, useHandler, false, false, false);
        }
    }

    // this is called in boot or first start application
    void firstStartEvents(boolean startedFromService, boolean useHandler)
    {
        PPApplication.logE("DataWrapper.firstStartEvents", "startedFromService="+startedFromService);

        //if (startedFromService)
            //invalidateEventList();  // force load form db

        if (!startedFromService) {
            Event.setEventsBlocked(context, false);
            synchronized (eventList) {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    if (event != null)
                        event._blocked = false;
                }
            }
            DatabaseHandler.getInstance(context).unblockAllEvents();
            Event.setForceRunEventRunning(context, false);
        }

        synchronized (eventList) {
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                if (event != null) {
                    event.setSensorsWaiting();
                    DatabaseHandler.getInstance(context).updateAllEventSensorsPassed(event);
                }
            }
        }

        resetAllEventsInDelayStart(true);
        resetAllEventsInDelayEnd(true);

        if (!getIsManualProfileActivation(false/*, context*/)) {
            PPApplication.logE("DataWrapper.firstStartEvents", "no manual profile activation, restart events");
        }
        else
        {
            PPApplication.logE("DataWrapper.firstStartEvents", "manual profile activation, activate profile");

            activateProfileOnBoot();
        }
        startEventsOnBoot(startedFromService, useHandler);
    }

    static Event getNonInitializedEvent(String name, int startOrder)
    {
        return new Event(name,
                startOrder,
                0,
                Profile.PROFILE_NO_ACTIVATE,
                Event.ESTATUS_STOP,
                "",
                false,
                false,
                Event.EPRIORITY_MEDIUM,
                0,
                false,
                Event.EATENDDO_RESTART_EVENTS,
                false,
                "",
                0,
                false,
                0,
                0,
                false,
                false,
                false,
                15,
                "",
                false
         );
    }

    long getProfileIdByName(String name, boolean fromDB)
    {
        String _name = name.trim();
        if ((!profileListFilled) || fromDB)
        {
            return DatabaseHandler.getInstance(context).getProfileIdByName(_name);
        }
        else
        {
            synchronized (profileList) {
                Profile profile;
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                    profile = it.next();
                    if (profile._name.trim().equals(_name))
                        return profile._id;
                }
            }
            return 0;
        }
    }

    Event getPredefinedEvent(int index, boolean saveToDB, Context baseContext) {
        Event event;

        switch (index) {
            case 0:
                event = getNonInitializedEvent(baseContext.getString(R.string.default_event_name_during_the_week), index+1);
                event._fkProfileStart = getProfileIdByName(baseContext.getString(R.string.default_profile_name_home), false);
                //if (event._fkProfileStart == 0)
                //    event._fkProfileStart = getPredefinedProfile(0, true, baseContext)._id;
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_NONE;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._monday = true;
                event._eventPreferencesTime._tuesday = true;
                event._eventPreferencesTime._wednesday = true;
                event._eventPreferencesTime._thursday = true;
                event._eventPreferencesTime._friday = true;
                event._eventPreferencesTime._timeType = EventPreferencesTime.TIME_TYPE_EXACT;
                event._eventPreferencesTime._startTime = 8 * 60;
                event._eventPreferencesTime._endTime = 23 * 60;
                //event._eventPreferencesTime._useEndTime = true;
                break;
            case 1:
                event = getNonInitializedEvent(baseContext.getString(R.string.default_event_name_weekend), index+1);
                event._fkProfileStart = getProfileIdByName(baseContext.getString(R.string.default_profile_name_home), false);
                //if (event._fkProfileStart == 0)
                //    event._fkProfileStart = getPredefinedProfile(0, true, baseContext)._id;
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_NONE;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._saturday = true;
                event._eventPreferencesTime._sunday = true;
                event._eventPreferencesTime._timeType = EventPreferencesTime.TIME_TYPE_EXACT;
                event._eventPreferencesTime._startTime = 8 * 60;
                event._eventPreferencesTime._endTime = 23 * 60;
                //event._eventPreferencesTime._useEndTime = true;
                break;
            case 2:
                event = getNonInitializedEvent(baseContext.getString(R.string.default_event_name_during_the_work), index+1);
                event._fkProfileStart = getProfileIdByName(baseContext.getString(R.string.default_profile_name_work), false);
                //if (event._fkProfileStart == 0)
                //    event._fkProfileStart = getPredefinedProfile(2, true, baseContext)._id;
                //event._undoneProfile = true;
                event._atEndDo = Event.EATENDDO_NONE;
                event._priority = Event.EPRIORITY_HIGHER;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._monday = true;
                event._eventPreferencesTime._tuesday = true;
                event._eventPreferencesTime._wednesday = true;
                event._eventPreferencesTime._thursday = true;
                event._eventPreferencesTime._friday = true;
                event._eventPreferencesTime._timeType = EventPreferencesTime.TIME_TYPE_EXACT;
                event._eventPreferencesTime._startTime = 9 * 60 + 30;
                event._eventPreferencesTime._endTime = 17 * 60 + 30;
                //event._eventPreferencesTime._useEndTime = true;
                break;
            case 3:
                event = getNonInitializedEvent(baseContext.getString(R.string.default_event_name_overnight), index+1);
                event._fkProfileStart = getProfileIdByName(baseContext.getString(R.string.default_profile_name_sleep), false);
                //if (event._fkProfileStart == 0)
                //    event._fkProfileStart = getPredefinedProfile(4, true, baseContext)._id;
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_UNDONE_PROFILE;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._monday = true;
                event._eventPreferencesTime._tuesday = true;
                event._eventPreferencesTime._wednesday = true;
                event._eventPreferencesTime._thursday = true;
                event._eventPreferencesTime._friday = true;
                event._eventPreferencesTime._saturday = true;
                event._eventPreferencesTime._sunday = true;
                event._eventPreferencesTime._timeType = EventPreferencesTime.TIME_TYPE_EXACT;
                event._eventPreferencesTime._startTime = 23 * 60;
                event._eventPreferencesTime._endTime = 8 * 60;
                //event._eventPreferencesTime._useEndTime = true;
                break;
            case 4:
                event = getNonInitializedEvent(baseContext.getString(R.string.default_event_name_night_call), index+1);
                event._fkProfileStart = getProfileIdByName(baseContext.getString(R.string.default_profile_name_home), false);
                //if (event._fkProfileStart == 0)
                //    event._fkProfileStart = getPredefinedProfile(0, true, baseContext)._id;
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_UNDONE_PROFILE;
                event._priority = Event.EPRIORITY_HIGHEST;
                event._forceRun = true;
                event._noPauseByManualActivation = false;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._monday = true;
                event._eventPreferencesTime._tuesday = true;
                event._eventPreferencesTime._wednesday = true;
                event._eventPreferencesTime._thursday = true;
                event._eventPreferencesTime._friday = true;
                event._eventPreferencesTime._saturday = true;
                event._eventPreferencesTime._sunday = true;
                event._eventPreferencesTime._timeType = EventPreferencesTime.TIME_TYPE_EXACT;
                event._eventPreferencesTime._startTime = 23 * 60;
                event._eventPreferencesTime._endTime = 8 * 60;
                //event._eventPreferencesTime._useEndTime = true;
                event._eventPreferencesCall._enabled = true;
                event._eventPreferencesCall._callEvent = EventPreferencesCall.CALL_EVENT_RINGING;
                event._eventPreferencesCall._contactListType = EventPreferencesCall.CONTACT_LIST_TYPE_WHITE_LIST;
                break;
            case 5:
                event = getNonInitializedEvent(baseContext.getString(R.string.default_event_name_low_battery), index+1);
                event._fkProfileStart = getProfileIdByName(baseContext.getString(R.string.default_profile_name_battery_low), false);
                //if (event._fkProfileStart == 0)
                //    event._fkProfileStart = getPredefinedProfile(5, true, baseContext)._id;
                event._fkProfileEnd = getProfileIdByName(baseContext.getString(R.string.default_profile_name_battery_ok), false);
                //if (event._fkProfileEnd == 0)
                //    event._fkProfileEnd = getPredefinedProfile(6, true, baseContext)._id;
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_RESTART_EVENTS;
                event._priority = Event.EPRIORITY_HIGHEST;
                event._forceRun = true;
                event._noPauseByManualActivation = false;
                event._eventPreferencesBattery._enabled = true;
                //if (Build.VERSION.SDK_INT >= 21) {
                    event._eventPreferencesBattery._levelLow = 0;
                    event._eventPreferencesBattery._levelHight = 100;
                    event._eventPreferencesBattery._powerSaveMode = true;
                /*}
                else {
                    event._eventPreferencesBattery._levelLow = 0;
                    event._eventPreferencesBattery._levelHight = 10;
                    event._eventPreferencesBattery._powerSaveMode = false;
                }*/
                event._eventPreferencesBattery._charging = 0;
                event._eventPreferencesBattery._plugged = "";
                break;
            default:
                event = null;
        }

        if (event != null) {
            if (saveToDB)
                DatabaseHandler.getInstance(context).addEvent(event);
        }

        return event;
    }

    void generatePredefinedEventList(Context baseContext)
    {
        //invalidateEventList();
        DatabaseHandler.getInstance(context).deleteAllEvents();

        synchronized (eventList) {
            for (int index = 0; index < 6; index++) {
                Event event = getPredefinedEvent(index, true, baseContext);
                if (event != null)
                    eventList.add(event);
            }
        }

        fillEventList();
    }


//---------------------------------------------------

//---------------------------------------------------

    void fillEventTimelineList()
    {
        synchronized (eventTimelines) {
            if (!eventTimelineListFilled) {
                eventTimelines.addAll(DatabaseHandler.getInstance(context).getAllEventTimelines());
                eventTimelineListFilled = true;
            }
        }
    }

    void copyEventTimelineList(DataWrapper fromDataWrapper) {
        synchronized (eventTimelines) {
            if (eventTimelineListFilled) {
                eventTimelines.clear();
                eventTimelineListFilled = false;
            }
            if (fromDataWrapper.eventTimelineListFilled) {
                eventTimelines.addAll(fromDataWrapper.eventTimelines);
                eventTimelineListFilled = true;
            }
        }
    }

    private void invalidateEventTimelineList()
    {
        synchronized (eventTimelines) {
            if (eventTimelineListFilled)
                eventTimelines.clear();
            eventTimelineListFilled = false;
        }
    }

    List<EventTimeline> getEventTimelineList(boolean fromDB)
    {
        synchronized (eventTimelines) {
            if (!eventTimelineListFilled || fromDB)
                if (fromDB)
                    invalidateEventTimelineList();
                fillEventTimelineList();
        }
        return eventTimelines;
    }

//-------------------------------------------------------------------------------------------------------------------

    /*public void invalidateDataWrapper()
    {
        invalidateProfileList();
        invalidateEventList();
        invalidateEventTimelineList();
    }*/

//----- Activate profile ---------------------------------------------------------------------------------------------

    private void _activateProfile(Profile _profile, boolean merged, int startupSource, final boolean forRestartEvents)
    {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "_profile=" + _profile);
            if (_profile != null)
                PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "_profile._name=" + _profile._name);
            PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "merged=" + merged);
            PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "startupSource=" + startupSource);
            PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "forRestartEvents=" + forRestartEvents);
        }*/

        // show notification when battery optimization is not enabled
        DrawOverAppsPermissionNotification.showNotification(context, false);
        IgnoreBatteryOptimizationNotification.showNotification(context, false);

        // remove last configured profile duration alarm
        ProfileDurationAlarmBroadcastReceiver.removeAlarm(_profile, context);
        Profile.setActivatedProfileForDuration(context, 0);

        //final Profile mappedProfile = _profile; //Profile.getMappedProfile(_profile, context);
        //profile = filterProfileWithBatteryEvents(profile);

        /*if (_profile != null)
            PPApplication.logE("$$$ DataWrapper._activateProfile","profileName="+_profile._name);
        else
            PPApplication.logE("$$$ DataWrapper._activateProfile","profile=null");*/

        if (_profile != null)
            Profile.saveProfileToSharedPreferences(_profile, context);

        //boolean fullyStarted = false;
        //if (PhoneProfilesService.getInstance() != null)
        //    fullyStarted = PhoneProfilesService.getInstance().getApplicationFullyStarted();
        boolean fullyStarted = PPApplication.applicationFullyStarted;
        //boolean applicationPackageReplaced = PPApplication.applicationPackageReplaced;
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "fullyStarted=" + fullyStarted);
            PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "applicationPackageReplaced=" + applicationPackageReplaced);
        }*/
        if ((!fullyStarted) /*|| applicationPackageReplaced*/) {
            // do not activate profile during application start
            //PPApplication.showProfileNotification(/*context*/forRestartEvents || (startupSource == PPApplication.STARTUP_SOURCE_BOOT), false);
            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper._activateProfile");
            //PPApplication.logE("###### PPApplication.updateGUI", "from=DataWrapper._activateProfile (1)");
            PPApplication.updateGUI(1/*context, true, forRestartEvents || (startupSource == PPApplication.STARTUP_SOURCE_BOOT)*/);
            return;
        }
        //PPApplication.logE("DataWrapper._activateProfile", "activate");

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("$$$ DataWrapper._activateProfile", "startupSource=" + startupSource);
            PPApplication.logE("$$$ DataWrapper._activateProfile", "merged=" + merged);
        }*/

//        if (PPApplication.logEnabled()) {
//            if (PhoneProfilesService.getInstance() != null) {
//                PPApplication.logE("### DataWrapper._activateProfile", "serviceHasFirstStart=" + PPApplication.serviceHasFirstStart);
//                PPApplication.logE("### DataWrapper._activateProfile", "serviceRunning=" + PhoneProfilesService.getInstance().getServiceRunning());
//            }
//        }

        //boolean interactive = _interactive;
        //final Activity activity = _activity;

        // get currently activated profile
        Profile activatedProfile = getActivatedProfile(false, false);
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "activatedProfile=" + activatedProfile);
            if (activatedProfile != null)
                PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "activatedProfile._name=" + activatedProfile._name);
        }*/

        if ((startupSource != PPApplication.STARTUP_SOURCE_SERVICE) //&&
            //(startupSource != PPApplication.STARTUP_SOURCE_BOOT) &&  // on boot must set as manual activation
            //(startupSource != PPApplication.STARTUP_SOURCE_LAUNCHER_START)
        )
        {
            // manual profile activation

            PPApplication.lockRefresh = true;

            // pause all events
            // for forceRun events set system events and block all events
            pauseAllEvents(false, true/*, true*/);

            PPApplication.lockRefresh = false;
        }

        //PPApplication.logE("$$$ DataWrapper._activateProfile","before activation");

        //PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "set activate profile start");
        DatabaseHandler.getInstance(context).activateProfile(_profile);
        setProfileActive(_profile);
        //PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "set activate profile end");

        //PPApplication.logE("$$$ DataWrapper._activateProfile","after activation");

        String profileIcon = "";
        int profileDuration = 0;
        if (_profile != null)
        {
            profileIcon = _profile._icon;

            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("$$$ DataWrapper._activateProfile", "duration=" + mappedProfile._duration);
                PPApplication.logE("$$$ DataWrapper._activateProfile", "afterDurationDo=" + mappedProfile._afterDurationDo);
            }*/
            if ((_profile._afterDurationDo != Profile.AFTER_DURATION_DO_NOTHING) &&
                (_profile._duration > 0)) {
                profileDuration = _profile._duration;
            }

            //PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "profileDuration="+profileDuration);

            // activation with duration
            if (((startupSource != PPApplication.STARTUP_SOURCE_SERVICE) &&
                 (startupSource != PPApplication.STARTUP_SOURCE_BOOT) //&&
                 //(startupSource != PPApplication.STARTUP_SOURCE_LAUNCHER_START)
                ) ||
                (_profile._afterDurationDo == Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE))
            {
                /*if (mappedProfile._afterDurationDo == Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE)
                    // manual profile activation
                    PPApplication.logE("$$$ DataWrapper._activateProfile","activation of specific profile");
                else
                    // manual profile activation
                    PPApplication.logE("$$$ DataWrapper._activateProfile","manual profile activation");*/

                //// set profile duration alarm

                // save before activated profile
                if (activatedProfile != null) {
                    long profileId = activatedProfile._id;
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("$$$ DataWrapper._activateProfile", "setActivatedProfileForDuration profileId=" + profileId);
                        PPApplication.logE("$$$ DataWrapper._activateProfile", "setActivatedProfileForDuration duration=" + profileDuration);
                        PPApplication.logE("$$$ DataWrapper._activateProfile", "setActivatedProfileForDuration forRestartEvents=" + forRestartEvents);
                    }*/
                    Profile.setActivatedProfileForDuration(context, profileId);
                }
                else
                    Profile.setActivatedProfileForDuration(context, 0);

                ProfileDurationAlarmBroadcastReceiver.setAlarm(_profile, forRestartEvents, startupSource, context);
                ///////////
            }
            else {
                //PPApplication.logE("$$$ DataWrapper._activateProfile","NO manual profile activation");
                profileDuration = 0;
            }

            //PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "profileDuration="+profileDuration);
        }

        //PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "update gui");
        //PPApplication.showProfileNotification(/*context*/forRestartEvents || (startupSource == PPApplication.STARTUP_SOURCE_BOOT), false);
        //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper._activateProfile");
        //PPApplication.logE("###### PPApplication.updateGUI", "from=DataWrapper._activateProfile (2)");
        PPApplication.updateGUI(1/*context, true, forRestartEvents || (startupSource == PPApplication.STARTUP_SOURCE_BOOT)*/);

        //if (mappedProfile != null) {
            //PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "call execute");
            //PPApplication.logE("$$$ DataWrapper._activateProfile","execute activation");
            ActivateProfileHelper.execute(context, _profile);
        //}

        if (/*(mappedProfile != null) &&*/ (!merged)) {
            //PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "add log");
            PPApplication.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ACTIVATION, null,
                    getProfileNameWithManualIndicatorAsString(_profile, true, "", profileDuration > 0, false, false, this),
                    profileIcon, profileDuration, "");
        }

        //if (mappedProfile != null)
        //{
            if (ApplicationPreferences.notificationsToast && (!PPApplication.lockRefresh))
            {
                // toast notification
                if (PPApplication.toastHandler != null) {
                    final Profile __profile = _profile;
                    PPApplication.toastHandler.post(new Runnable() {
                        public void run() {
                            //PPApplication.logE("[ACTIVATOR] DataWrapper._activateProfile", "show toast");
                            showToastAfterActivation(__profile);
                        }
                    });
                }// else
                //    showToastAfterActivation(profile);
            }
        //}
    }

    void activateProfileFromMainThread(final Profile profile, final boolean merged, final int startupSource,
                                    final boolean interactive, final Activity _activity, final boolean testGrant)
    {
        //PPApplication.logE("$$$$$ DataWrapper.activateProfileFromMainThread", "start");
        final DataWrapper dataWrapper = copyDataWrapper();

        PPApplication.startHandlerThread(/*"DataWrapper.activateProfileFromMainThread"*/);
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_activateProfileFromMainThread");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=DataWrapper.activateProfileFromMainThread");

                    boolean granted = true;
                    if (testGrant)
                        granted = !EditorProfilesActivity.displayPreferencesErrorNotification(profile, null, context);
                    if (granted) {
                        //PPApplication.logE("&&&&&&& DataWrapper.activateProfileFromMainThread", "called is DataWrapper._activateProfile()");
                        dataWrapper._activateProfile(profile, merged, startupSource, false);
                        if (interactive) {
                            DatabaseHandler.getInstance(dataWrapper.context).increaseActivationByUserCount(profile);
                            dataWrapper.setDynamicLauncherShortcuts();
                        }
                    }

                    //PPApplication.logE("$$$$$ PPApplication.startHandlerThread", "END run - from=DataWrapper.activateProfileFromMainThread");
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {}
                    }
                }
            }
        });

        // for startActivityForResult
        if (_activity != null)
        {
            //final Profile profile = _profile; //Profile.getMappedProfile(_profile, context);

            Intent returnIntent = new Intent();
            if (profile == null)
                returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            else
                returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            returnIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
            _activity.setResult(Activity.RESULT_OK,returnIntent);
        }

        finishActivity(startupSource, true, _activity);

    }

    private void showToastAfterActivation(Profile profile)
    {
        //boolean fullyStarted = false;
        //if (PhoneProfilesService.getInstance() != null)
        //    fullyStarted = PhoneProfilesService.getInstance().getApplicationFullyStarted();
        boolean fullyStarted = PPApplication.applicationFullyStarted;
        //fullyStarted = fullyStarted && (!PPApplication.applicationPackageReplaced);

        if (!fullyStarted)
            return;

        try {
            String profileName = getProfileNameWithManualIndicatorAsString(profile, true, "", false, false, false, this);
            PPApplication.showToast(context.getApplicationContext(),
                    context.getResources().getString(R.string.toast_profile_activated_0) + ": " + profileName + " " +
                            context.getResources().getString(R.string.toast_profile_activated_1),
                    Toast.LENGTH_SHORT);
        }
        catch (Exception e) {
            PPApplication.recordException(e);
        }
        //Log.d("DataWrapper.showToastAfterActivation", "-- end");
    }

    private void activateProfileWithAlert(Profile profile, int startupSource, /*final boolean interactive,*/
                                            Activity activity)
    {
        PPApplication.setBlockProfileEventActions(false);

        if (/*interactive &&*/ (ApplicationPreferences.applicationActivateWithAlert ||
                            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR)))
        {
            // set theme and language for dialog alert ;-)
            GlobalGUIRoutines.setTheme(activity, true, true/*, false*/, false);
            //GlobalGUIRoutines.setLanguage(activity);

            final Profile _profile = profile;
            //final boolean _interactive = interactive;
            final int _startupSource = startupSource;
            final Activity _activity = activity;
            final DataWrapper _dataWrapper = this;

            if (profile._askForDuration) {
                if (!_activity.isFinishing()) {
                    FastAccessDurationDialog dlg = new FastAccessDurationDialog(_activity, _profile, _dataWrapper,
                            /*monochrome, monochromeValue,*/ _startupSource);
                    dlg.show();
                }
            }
            else {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                dialogBuilder.setTitle(activity.getResources().getString(R.string.profile_string_0) + ": " + profile._name);
                dialogBuilder.setMessage(activity.getResources().getString(R.string.activate_profile_alert_message));
                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                            //if (Permissions.grantProfilePermissions(context, _profile, false, true,
                            //        /*false, monochrome, monochromeValue,*/
                            //        _startupSource, true, true, false))
                            if (!EditorProfilesActivity.displayPreferencesErrorNotification(_profile, null, context)) {
                                //PPApplication.logE("&&&&&&& DataWrapper.activateProfileWithAlert", "(1) called is DataWrapper.activateProfileFromMainThread");
                                _dataWrapper.activateProfileFromMainThread(_profile, false, _startupSource, true, _activity, false);
                            }
                            else {
                                Intent returnIntent = new Intent();
                                _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                                finishActivity(_startupSource, true, _activity);
                            }
                        }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // for startActivityForResult
                        Intent returnIntent = new Intent();
                        _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                        finishActivity(_startupSource, false, _activity);
                    }
                });
                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        // for startActivityForResult
                        Intent returnIntent = new Intent();
                        _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                        finishActivity(_startupSource, false, _activity);
                    }
                });
                AlertDialog dialog = dialogBuilder.create();

//                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(DialogInterface dialog) {
//                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                        if (positive != null) positive.setAllCaps(false);
//                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                        if (negative != null) negative.setAllCaps(false);
//                    }
//                });

                if (!activity.isFinishing())
                    dialog.show();
            }
        }
        else
        {
            GlobalGUIRoutines.setTheme(activity, true, true/*, false*/, false);
            //GlobalGUIRoutines.setLanguage(activity);

            if (profile._askForDuration/* && interactive*/) {
                if (!activity.isFinishing()) {
                    FastAccessDurationDialog dlg = new FastAccessDurationDialog(activity, profile, this,
                            /*monochrome, monochromeValue,*/ startupSource);
                    dlg.show();
                }
            }
            else {
                if (!EditorProfilesActivity.displayPreferencesErrorNotification(profile, null, context)) {
                    //PPApplication.logE("&&&&&&& DataWrapper.activateProfileWithAlert", "(2) called is DataWrapper.activateProfileFromMainThread");
                    activateProfileFromMainThread(profile, false, startupSource, true, activity, false);
                }
                else {
                    Intent returnIntent = new Intent();
                    activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                    finishActivity(startupSource, true, activity);
                }
            }
        }
    }

    void finishActivity(final int startupSource, boolean finishActivator, final Activity _activity)
    {
        if (_activity == null)
            return;

        //final Activity activity = _activity;

        boolean finish = true;

        if (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR)
        {
            finish = false;
            if (ApplicationPreferences.applicationClose)
            {
                // close of activity after profile activation is enabled
                if (PPApplication.getApplicationStarted(false))
                    // application is already started and is possible to close activity
                    finish = finishActivator;
            }
        }
        else
        if (startupSource == PPApplication.STARTUP_SOURCE_EDITOR)
        {
            finish = false;
        }

        if (finish) {
            final Handler handler = new Handler(context.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=DataWrapper.finishActivity");

                    try {
                        //if (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR)
                        //    _activity.finishAndRemoveTask();
                        //else
                            _activity.finish();
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
            });
        }
    }

    public void activateProfile(final long profile_id, final int startupSource, final Activity activity, boolean testGrant)
    {
        Profile profile;

        // for activated profile is recommended update of activity
        if (startupSource == PPApplication.STARTUP_SOURCE_BOOT) {
            long profileId = PPApplication.prefLastActivatedProfile;
            profile = getProfileById(profileId, false, false, false);
        }
        else
            profile = getActivatedProfile(false, false);

        boolean actProfile = false;
        //boolean interactive = false;
        if ((startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT) ||
            (startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SERVICE) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SERVICE_MANUAL) ||
            (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER))
        {
            // activation is invoked from shortcut, widget, Activator, Editor, service,
            // do profile activation
            actProfile = true;
            //interactive = ((startupSource != PPApplication.STARTUP_SOURCE_SERVICE));
        }
        else
        if (startupSource == PPApplication.STARTUP_SOURCE_BOOT)
        {
            // activation is invoked during device boot

            //ProfileDurationAlarmBroadcastReceiver.removeAlarm(null, context);
            Profile.setActivatedProfileForDuration(context, 0);

            if (ApplicationPreferences.applicationActivate)
            {
                actProfile = true;
            }

            if (profile_id == 0)
                profile = null;
        }
        /*else
        if (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER_START)
        {
            // activation is invoked from launcher

            Profile _profile;
            if (profile_id == 0)
                _profile = null;
            else
                _profile = getProfileById(profile_id, false, false, false);
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(_profile, context);
            Profile.setActivatedProfileForDuration(context, 0);

            if (ApplicationPreferences.applicationActivate)
            {
                actProfile = true;
            }

            if (profile_id == 0)
                profile = null;
        }*/

        if ((startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT) ||
            (startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SERVICE) ||
            //(startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER_START) ||
            (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER))
        {
            if (profile_id == 0)
                profile = null;
            else
                profile = getProfileById(profile_id, false, false, false);
        }


        if (actProfile && (profile != null))
        {
            // profile activation
            if (startupSource == PPApplication.STARTUP_SOURCE_BOOT) {
                //PPApplication.logE("&&&&&&& DataWrapper.activateProfile", "called is DataWrapper.activateProfileFromMainThread");
                activateProfileFromMainThread(profile, false, PPApplication.STARTUP_SOURCE_BOOT,
                        false, null, testGrant);
            }
            else
                activateProfileWithAlert(profile, startupSource, /*interactive,*/ activity);
        }
        else
        {
            DatabaseHandler.getInstance(context).activateProfile(profile);
            setProfileActive(profile);

            //PPApplication.showProfileNotification(/*context*/false, false);
            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper.activateProfile");
            //PPApplication.logE("###### PPApplication.updateGUI", "from=DataWrapper.activateProfile");
            PPApplication.updateGUI(1/*context, true, startupSource == PPApplication.STARTUP_SOURCE_BOOT*/);

            // for startActivityForResult
            if (activity != null)
            {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile_id);
                returnIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
                activity.setResult(Activity.RESULT_OK,returnIntent);
            }

            finishActivity(startupSource, true, activity);
        }
    }

    void activateProfileAfterDuration(long profile_id, int startupSource)
    {
        Profile profile = getProfileById(profile_id, false, false, false);
        //PPApplication.logE("DataWrapper.activateProfileAfterDuration", "profile="+profile);
        if (profile == null) {
            //PPApplication.logE("DataWrapper.activateProfileAfterDuration", "no activate");
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(null, context);
            Profile.setActivatedProfileForDuration(context, 0);
            //PPApplication.showProfileNotification(/*context*/false, false);
            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper.activateProfileAfterDuration");
            //PPApplication.logE("###### PPApplication.updateGUI", "from=DataWrapper.activateProfileAfterDuration");
            PPApplication.updateGUI(1/*context, true, false*/);
            return;
        }
        //if (Permissions.grantProfilePermissions(context, profile, false, true,
        //        /*false, monochrome, monochromeValue,*/
        //        startupSource, true,true, false)) {
        if (!EditorProfilesActivity.displayPreferencesErrorNotification(profile, null, context)) {
            // activateProfileAfterDuration is already called from handlerThread
            //PPApplication.logE("&&&&&&& DataWrapper.activateProfileAfterDuration", "called is DataWrapper._activateProfile()");
            _activateProfile(profile, false, startupSource, false);
        }
    }

    private void _restartEvents(final boolean unblockEventsRun, /*final boolean notClearActivatedProfile,*/
                                /*final boolean reactivateProfile,*/ final boolean manualRestart, final boolean logRestart)
    {
        //PPApplication.logE("DataWrapper._restartEvents", "xxx");

        if (logRestart) {
            if (manualRestart)
                PPApplication.addActivityLog(context, PPApplication.ALTYPE_MANUAL_RESTART_EVENTS, null, null, null, 0, "");
            else
                PPApplication.addActivityLog(context, PPApplication.ALTYPE_RESTART_EVENTS, null, null, null, 0, "");
        }

        if ((ApplicationPreferences.prefEventsBlocked && (!unblockEventsRun)) /*|| (!reactivateProfile)*/) {
            //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=DataWrapper._restartEvents (1)");

            PPApplication.logE("[EVENTS_HANDLER] DataWrapper._restartEvents", "sensorType=SENSOR_TYPE_RESTART_EVENTS_NOT_UNBLOCK");
            EventsHandler eventsHandler = new EventsHandler(context);
            // this do not perform restart, only SENSOR_TYPE_RESTART_EVENTS perform restart
            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RESTART_EVENTS_NOT_UNBLOCK);

            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=DataWrapper._restartEvents (1)");
            return;
        }

        //PPApplication.logE("DataWrapper._restartEvents", "events are not blocked");

        //Profile activatedProfile = getActivatedProfile();

        if (unblockEventsRun)
        {
            synchronized (profileList) {
                // remove alarm for profile duration
                if (!profileListFilled)
                    fillProfileList(false, false);
                for (Profile profile : profileList)
                    ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, context);
            }
            Profile.setActivatedProfileForDuration(context, 0);

            Event.setEventsBlocked(context, false);
            synchronized (eventList) {
                fillEventList();
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    if (event != null)
                        event._blocked = false;
                }
            }
            //PPApplication.logE("DataWrapper._restartEvents", "after synchronized (eventList)");

            DatabaseHandler.getInstance(context).unblockAllEvents();
            Event.setForceRunEventRunning(context, false);
        }

        /*if (!notClearActivatedProfile) {
            DatabaseHandler.getInstance(context).deactivateProfile();
            setProfileActive(null);
        }*/

        //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=DataWrapper._restartEvents (2)");

        EventsHandler eventsHandler = new EventsHandler(context);
        if (manualRestart) {
            PPApplication.logE("[EVENTS_HANDLER] DataWrapper._restartEvents", "sensorType=SENSOR_TYPE_MANUAL_RESTART_EVENTS");
           eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_MANUAL_RESTART_EVENTS);
        }
        else {
            PPApplication.logE("[EVENTS_HANDLER] DataWrapper._restartEvents", "sensorType=SENSOR_TYPE_RESTART_EVENTS");
            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RESTART_EVENTS);
        }

        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=DataWrapper._restartEvents (2)");
    }

    private void restartEvents(final boolean unblockEventsRun, /*final boolean notClearActivatedProfile,*/
                       /*final boolean reactivateProfile,*/ final boolean manualRestart, final boolean logRestart
                       /*, final boolean useHandler*/)
    {
        /*if (!Event.getGlobalEventsRunning()) {
            // events are globally stopped

            if (PhoneProfilesService.getInstance() != null)
                PhoneProfilesService.getInstance().willBeDoRestartEvents = false;

            return;
        }*/

        /*
        PPApplication.logE("DataWrapper.restartEvents", "useHandler="+useHandler);

        if (useHandler) {
            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThread("DataWrapper.restartEvents");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_restartEvents");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=DataWrapper.restartEvents");

                        _restartEvents(unblockEventsRun, notClearActivatedProfile, reactivateProfile, log);

                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=DataWrapper.restartEvents");
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            });
        }
        else
        */
            _restartEvents(unblockEventsRun, /*notClearActivatedProfile, reactivateProfile,*/ manualRestart, logRestart);
    }

    private void _restartEventsWithRescan(boolean alsoRescan, boolean unblockEventsRun, boolean manualRestart, boolean logRestart) {
        //PPApplication.logE("$$$ DataWrapper._restartEventsWithRescan","xxx");

        if (alsoRescan) {
            // remove all event delay alarms
            resetAllEventsInDelayStart(false);
            resetAllEventsInDelayEnd(false);
        }

        restartEvents(unblockEventsRun, /*true, true,*/ manualRestart, logRestart/*, false*/);

        if (alsoRescan) {
            //PPApplication.logE("[RJS] DataWrapper._restartEventsWithRescan", "restart all scanners");
            // for screenOn=true -> used only for geofence scanner - start scan with GPS On
            boolean restart = false;
            if (ApplicationPreferences.applicationEventLocationEnableScanning)
                restart = true;
            else if (ApplicationPreferences.applicationEventWifiEnableScanning)
                restart = true;
            else if (ApplicationPreferences.applicationEventBluetoothEnableScanning)
                restart = true;
            else if (ApplicationPreferences.applicationEventMobileCellEnableScanning)
                restart = true;
            else if (ApplicationPreferences.applicationEventOrientationEnableScanning)
                restart = true;
            else if (ApplicationPreferences.applicationEventBackgroundScanningEnableScanning)
                restart = true;
            if (restart) {
                PPApplication.restartAllScanners(context, false);
            }
        }

        DrawOverAppsPermissionNotification.showNotification(context, false);
        IgnoreBatteryOptimizationNotification.showNotification(context, false);
    }

    void restartEventsWithRescan(final boolean alsoRescan,
            final boolean unblockEventsRun, boolean useHandler,
            final boolean manualRestart, final boolean logRestart, boolean showToast)
    {
        //PPApplication.logE("[TEST BATTERY] DataWrapper.restartEventsWithRescan","xxx");

        if (useHandler) {
            final DataWrapper dataWrapper = copyDataWrapper();

            PPApplication.startHandlerThread(/*"DataWrapper.restartEventsWithRescan"*/);
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) dataWrapper.context.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_restartEventsWithRescan");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=DataWrapper.restartEventsWithRescan");

                        dataWrapper._restartEventsWithRescan(alsoRescan, unblockEventsRun, manualRestart, logRestart);

                        //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=DataWrapper.restartEventsWithRescan");
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            });
        }
        else
            _restartEventsWithRescan(alsoRescan, unblockEventsRun, manualRestart, logRestart);

        if (showToast) {
            if (ApplicationPreferences.notificationsToast) {
                PPApplication.showToast(context.getApplicationContext(),
                        context.getResources().getString(R.string.toast_events_restarted),
                        Toast.LENGTH_SHORT);
            }
        }
    }

    void restartEventsWithAlert(final Activity activity)
    {
        if (!Event.getGlobalEventsRunning())
            // events are globally stopped
            return;

        /*
        if (!PPApplication.getEventsBlocked(context))
            return;
        */

        //PPApplication.logE("DataWrapper.restartEventsWithAlert", "xxx");

        PPApplication.setBlockProfileEventActions(false);

        if (ApplicationPreferences.applicationRestartEventsWithAlert || (activity instanceof EditorProfilesActivity))
        {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.restart_events_alert_title);
            dialogBuilder.setMessage(R.string.restart_events_alert_message);
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //PPApplication.logE("DataWrapper.restartEventsWithAlert", "restart");

                    boolean finish;
                    if (activity instanceof ActivateProfileActivity)
                        finish = ApplicationPreferences.applicationClose;
                    else
                    //noinspection RedundantIfStatement
                    if ((activity instanceof RestartEventsFromGUIActivity) ||
                            (activity instanceof BackgroundActivateProfileActivity))
                        finish = true;
                    else
                        finish = false;
                    if (finish)
                        activity.finish();

                    if (!PPApplication.getApplicationStarted(true)) {
                        PPApplication.setApplicationStarted(context, true);
                        Intent serviceIntent = new Intent(context, PhoneProfilesService.class);
                        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, false);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                        //serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                        serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                        PPApplication.startPPService(context, serviceIntent/*, true*/);
                    }
                    else {
                        //PPApplication.logE("*********** restartEvents", "from DataWrapper.restartEventsWithAlert() - 1");
                        restartEventsWithRescan(true, true, true, true, true, true);
                        //IgnoreBatteryOptimizationNotification.showNotification(context);
                    }
                }
            });
            dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    boolean finish = (!(activity instanceof ActivateProfileActivity)) &&
                                     (!(activity instanceof EditorProfilesActivity));

                    if (finish)
                        activity.finish();
                }
            });
            dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    boolean finish = (!(activity instanceof ActivateProfileActivity)) &&
                                     (!(activity instanceof EditorProfilesActivity));

                    if (finish)
                        activity.finish();
                }
            });
            AlertDialog dialog = dialogBuilder.create();

//            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                @Override
//                public void onShow(DialogInterface dialog) {
//                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                    if (positive != null) positive.setAllCaps(false);
//                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                    if (negative != null) negative.setAllCaps(false);
//                }
//            });

            if (!activity.isFinishing())
                dialog.show();
        }
        else
        {
            //PPApplication.logE("DataWrapper.restartEventsWithAlert", "restart");

            boolean finish;
            if (activity instanceof ActivateProfileActivity)
                finish = ApplicationPreferences.applicationClose;
            else
                finish = (activity instanceof RestartEventsFromGUIActivity) ||
                        (activity instanceof BackgroundActivateProfileActivity);
            //PPApplication.logE("DataWrapper.restartEventsWithAlert", "finish="+finish);
            if (finish) {
                final Handler handler = new Handler(context.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=DataWrapper.restartEventsWithAlert");
                        try {
                            activity.finish();
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                });
            }

            //PPApplication.logE("*********** restartEvents", "from DataWrapper.restartEventsWithAlert() - 2");
            restartEventsWithRescan(true, true, true, true, true, true);

            //IgnoreBatteryOptimizationNotification.showNotification(context);
        }
    }

    @SuppressLint("NewApi")
    // delay is in seconds, max 5
    void restartEventsWithDelay(int delay, boolean alsoRescan, final boolean unblockEventsRun, /*final boolean reactivateProfile,*/
                                /*boolean clearOld,*/ final int logType)
    {
        //PPApplication.logE("[TEST BATTERY] DataWrapper.restartEventsWithDelay","xxx"); //"clearOld="+clearOld);
        Data workData = new Data.Builder()
                    .putBoolean(PhoneProfilesService.EXTRA_ALSO_RESCAN, alsoRescan)
                    .putBoolean(PhoneProfilesService.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun)
                    .putInt(PhoneProfilesService.EXTRA_LOG_TYPE, logType)
                    .build();

        /*int keepResultsDelay = (delay * 5) / 60; // conversion to minutes
        if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
            keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/
        OneTimeWorkRequest restartEventsWithDelayWorker =
                new OneTimeWorkRequest.Builder(RestartEventsWithDelayWorker.class)
                        .addTag(RestartEventsWithDelayWorker.WORK_TAG)
                        .setInputData(workData)
                        .setInitialDelay(delay, TimeUnit.SECONDS)
                        .keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                        .build();
        try {
            if (PPApplication.getApplicationStarted(true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                    //if (PPApplication.logEnabled()) {
//                    ListenableFuture<List<WorkInfo>> statuses;
//                    statuses = workManager.getWorkInfosForUniqueWork(RestartEventsWithDelayWorker.WORK_TAG);
//                    try {
//                        List<WorkInfo> workInfoList = statuses.get();
//                        PPApplication.logE("[TEST BATTERY] DataWrapper.restartEventsWithDelay", "for=" + RestartEventsWithDelayWorker.WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                    } catch (Exception ignored) {
//                    }
//                    //}

                    //workManager.enqueue(restartEventsWithDelayWorker);
                    workManager.enqueueUniqueWork(RestartEventsWithDelayWorker.WORK_TAG, ExistingWorkPolicy.APPEND_OR_REPLACE, restartEventsWithDelayWorker);
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    void setEventBlocked(Event event, boolean blocked)
    {
        event._blocked = blocked;
        DatabaseHandler.getInstance(context).updateEventBlocked(event);
    }

    // returns true if:
    // 1. events are blocked = any profile is activated manually
    // 2. no any forceRun event is running
    static boolean getIsManualProfileActivation(boolean afterDuration/*, Context context*/)
    {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("DataWrapper.getIsManualProfileActivation", "ApplicationPreferences.prefEventsBlocked=" + ApplicationPreferences.prefEventsBlocked);
            PPApplication.logE("DataWrapper.getIsManualProfileActivation", "ApplicationPreferences.prefForceRunEventRunning=" + ApplicationPreferences.prefForceRunEventRunning);
        }*/
        if (afterDuration)
            return ApplicationPreferences.prefEventsBlocked;
        else {
            if (!ApplicationPreferences.prefEventsBlocked)
                return false;
            else
                return !ApplicationPreferences.prefForceRunEventRunning;
        }
    }

    static private Spannable _getProfileNameWithManualIndicator(
            Profile profile, boolean addEventName, String indicators, boolean addDuration, boolean multiLine,
            boolean durationInNextLine, DataWrapper dataWrapper)
    {
        if (profile == null)
            return new SpannableString("");

        String eventName = "";
        String manualIndicators = "";
        if (addEventName)
        {
            if (ApplicationPreferences.prefEventsBlocked) {
                if (ApplicationPreferences.prefForceRunEventRunning)
                    manualIndicators = "[»]";
                else
                    manualIndicators = "[M]";
            }

            String _eventName = getLastStartedEventName(dataWrapper, profile);
            if (!_eventName.equals("?"))
                eventName = "[" + _eventName + "]";

            if (!manualIndicators.isEmpty())
                eventName = manualIndicators + " " + eventName;
        }

        if (!PPApplication.getApplicationStarted(true))
            eventName = eventName + " ";

        Spannable sName;
        if (addDuration) {
            if (!addEventName || manualIndicators.equals("[M]"))
                sName = profile.getProfileNameWithDuration(eventName, indicators, multiLine, durationInNextLine, dataWrapper.context);
            else {
                String name = profile._name;
                if (!eventName.isEmpty())
                    name = name + " " + eventName;
                if (!indicators.isEmpty()) {
                    if (multiLine)
                        name = name + "\n" + indicators;
                    else
                        name = name + " " + indicators;
                }
                sName = new SpannableString(name);
            }
        }
        else {
            String name = profile._name;
            if (!eventName.isEmpty())
                name = name + " " + eventName;
            if (!indicators.isEmpty()) {
                if (multiLine)
                    name = name + "\n" + indicators;
                else
                    name = name + " " + indicators;
            }
            sName = new SpannableString(name);
        }

        return sName;
    }

    static Spannable getProfileNameWithManualIndicator(
            Profile profile, boolean addEventName, String indicators, boolean addDuration, boolean multiLine,
            boolean durationInNextLine, DataWrapper dataWrapper) {
        if (dataWrapper != null) {
            return _getProfileNameWithManualIndicator(profile, addEventName, indicators, addDuration, multiLine, durationInNextLine, dataWrapper);
        }
        else {
            return _getProfileNameWithManualIndicator(profile, false, indicators, addDuration, multiLine, durationInNextLine, null);
        }
    }

    @SuppressWarnings("SameParameterValue")
    static String getProfileNameWithManualIndicatorAsString(
            Profile profile, boolean addEventName, String indicators, boolean addDuration, boolean multiLine,
            boolean durationInNextLine, DataWrapper dataWrapper) {
        Spannable sProfileName = getProfileNameWithManualIndicator(profile, addEventName, indicators, addDuration, multiLine, durationInNextLine, dataWrapper);
        Spannable sbt = new SpannableString(sProfileName);
        Object[] spansToRemove = sbt.getSpans(0, sProfileName.length(), Object.class);
        for (Object span : spansToRemove) {
            if (span instanceof CharacterStyle)
                sbt.removeSpan(span);
        }
        return sbt.toString();
    }

    static private String getLastStartedEventName(DataWrapper dataWrapper, Profile forProfile)
    {

        if (Event.getGlobalEventsRunning() && PPApplication.getApplicationStarted(false))
        {
            if (dataWrapper.eventListFilled && dataWrapper.eventTimelineListFilled) {
                List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(false);
                if (eventTimelineList.size() > 0)
                {
                    EventTimeline eventTimeLine = eventTimelineList.get(eventTimelineList.size()-1);
                    long event_id = eventTimeLine._fkEvent;
                    Event event = dataWrapper.getEventById(event_id);
                    if (event != null)
                    {
                        if ((!ApplicationPreferences.prefEventsBlocked) || (event._forceRun))
                        {
                            //Profile profile;
                            //profile = dataWrapper.getActivatedProfile(false, false);
                            //if ((profile != null) && (event._fkProfileStart == profile._id))
                                // last started event activates activated profile
                                return event._name;
                            //else
                            //    return "?";
                        }
                        else
                            return "?";
                    }
                    else
                        return "?";
                }
                else
                {
                    long profileId = ApplicationPreferences.applicationDefaultProfile;
                    if ((!ApplicationPreferences.prefEventsBlocked) &&
                            (profileId != Profile.PROFILE_NO_ACTIVATE) &&
                            (profileId == forProfile._id))
                    {
                        //Profile profile;
                        //profile = dataWrapper.getActivatedProfile(false, false);
                        //if ((profile != null) && (profile._id == profileId))
                            return dataWrapper.context.getString(R.string.event_name_background_profile);
                        //else
                        //    return "?";
                    }
                    else
                        return "?";
                }
            }
            else {
                String eventName = DatabaseHandler.getInstance(dataWrapper.context).getLastStartedEventName();
                if (!eventName.equals("?")) {
                    return eventName;
                }
                /*
                List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(true);
                if (eventTimelineList.size() > 0)
                {
                    EventTimeline eventTimeLine = eventTimelineList.get(eventTimelineList.size()-1);
                    long event_id = eventTimeLine._fkEvent;
                    Event event = dataWrapper.getEventById(event_id);
                    if (event != null)
                    {
                        if ((!ApplicationPreferences.prefEventsBlocked) || (event._forceRun))
                        {
                            //Profile profile;
                            //profile = dataWrapper.getActivatedProfileFromDB(false, false);
                            //if ((profile != null) && (event._fkProfileStart == profile._id))
                                // last started event activates activated profile
                                return event._name;
                            //else
                            //    return "?";
                        }
                        else
                            return "?";
                    }
                    else
                        return "?";
                }*/
                else
                {
                    long profileId = ApplicationPreferences.applicationDefaultProfile;
                    if ((!ApplicationPreferences.prefEventsBlocked) &&
                        (profileId != Profile.PROFILE_NO_ACTIVATE) &&
                        (profileId == forProfile._id))
                    {
                        //Profile profile;
                        //profile = dataWrapper.getActivatedProfileFromDB(false, false);
                        //if ((profile != null) && (profile._id == profileId))
                            return dataWrapper.context.getString(R.string.event_name_background_profile);
                        //else
                        //    return "?";
                    }
                    else
                        return "?";
                }
            }

        }
        else
            return "?";
    }

    private void resetAllEventsInDelayStart(boolean onlyFromDb)
    {
        if (!onlyFromDb) {
            synchronized (eventList) {
                fillEventList();
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    event.removeDelayStartAlarm(this);
                }
            }
        }
        DatabaseHandler.getInstance(context).resetAllEventsInDelayStart();
    }

    private void resetAllEventsInDelayEnd(boolean onlyFromDb)
    {
        if (!onlyFromDb) {
            synchronized (eventList) {
                fillEventList();
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    event.removeDelayEndAlarm(this);
                }
            }
        }
        DatabaseHandler.getInstance(context).resetAllEventsInDelayStart();
    }

    void runStopEventsWithAlert(final Activity activity, final SwitchCompat checkBox, boolean isChecked) {
        boolean eventRunningEnabled = Event.getGlobalEventsRunning();
        if (checkBox != null) {
            if (isChecked && eventRunningEnabled)
                // already enabled
                return;
            if (!isChecked && !eventRunningEnabled)
                // already disabled
                return;
        }
        if (eventRunningEnabled) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.stop_events_alert_title);
            dialogBuilder.setMessage(R.string.stop_events_alert_message);
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //PPApplication.logE("DataWrapper.runStopEventsWithAlert", "stop");
                    if (globalRunStopEvents(true)) {
                        //PPApplication.showProfileNotification(/*activity.getApplicationContext()*/true, false);

                        /*if (activity instanceof EditorProfilesActivity)
                            ((EditorProfilesActivity) activity).refreshGUI(true, false, true, 0, 0);
                        else if (activity instanceof ActivateProfileActivity)
                            ((ActivateProfileActivity) activity).refreshGUI(true, false);*/
                        //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper.runStopEventsWithAlert");
                        //PPApplication.logE("###### PPApplication.updateGUI", "from=DataWrapper.runStopEventsWithAlert (1)");
                        PPApplication.updateGUI(0/*activity, true, true*/);
                    }
                }
            });
            dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //PPApplication.logE("DataWrapper.runStopEventsWithAlert", "no stop");
                    if (checkBox != null)
                        checkBox.setChecked(true);
                }
            });
            AlertDialog dialog = dialogBuilder.create();

//            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                @Override
//                public void onShow(DialogInterface dialog) {
//                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                    if (positive != null) positive.setAllCaps(false);
//                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                    if (negative != null) negative.setAllCaps(false);
//                }
//            });

            if (!activity.isFinishing())
                dialog.show();
        }
        else {
            if (globalRunStopEvents(false)) {
                //PPApplication.showProfileNotification(/*activity.getApplicationContext()*/true, false);
                /*if (activity instanceof EditorProfilesActivity)
                    ((EditorProfilesActivity) activity).refreshGUI(true, false, true, 0, 0);
                else if (activity instanceof ActivateProfileActivity)
                    ((ActivateProfileActivity) activity).refreshGUI(true, false);*/
                //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper.runStopEventsWithAlert");
                //PPApplication.logE("###### PPApplication.updateGUI", "from=DataWrapper.runStopEventsWithAlert (2)");
                PPApplication.updateGUI(0/*activity, true, true*/);
            }
        }
    }

    boolean globalRunStopEvents(boolean stop) {
        if (stop) {
            if (Event.getGlobalEventsRunning()) {
                PPApplication.addActivityLog(context, PPApplication.ALTYPE_RUN_EVENTS_DISABLE, null, null, null, 0, "");

                // no setup for next start
                resetAllEventsInDelayStart(false);
                resetAllEventsInDelayEnd(false);

                Event.setGlobalEventsRunning(context, false);

                // no set system events, unblock all events, no activate return profile
                pauseAllEventsForGlobalStopEvents();

                /*Intent serviceIntent = new Intent(context, PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.startPPService(context, serviceIntent);*/
                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                commandIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.runCommand(context, commandIntent);
                return true;
            }
        }
        else {
            if (!Event.getGlobalEventsRunning()) {
                PPApplication.addActivityLog(context, PPApplication.ALTYPE_RUN_EVENTS_ENABLE, null, null, null, 0, "");

                Event.setGlobalEventsRunning(context, true);

                /*Intent serviceIntent = new Intent(context, PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.startPPService(context, serviceIntent);*/
                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.runCommand(context, commandIntent);

                // setup for next start
                firstStartEvents(false, true);

                //IgnoreBatteryOptimizationNotification.showNotification(context.getApplicationContext());
                return true;
            }
        }
        return false;
    }

    static boolean isPowerSaveMode(Context context) {

        /*String applicationPowerSaveModeInternal = ApplicationPreferences.applicationPowerSaveModeInternal;

        if (applicationPowerSaveModeInternal.equals("1") || applicationPowerSaveModeInternal.equals("2")) {
            Intent batteryStatus = null;
            try { // Huawei devices: java.lang.IllegalArgumentException: registered too many Broadcast Receivers
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                batteryStatus = context.registerReceiver(null, filter);
            } catch (Exception ignored) {
            }
            if (batteryStatus != null) {
                boolean isCharging;
                int batteryPct;

                //int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                //PPApplication.logE("DataWrapper.isPowerSaveMode", "status=" + status);
                int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                isCharging = plugged == BatteryManager.BATTERY_PLUGGED_AC
                        || plugged == BatteryManager.BATTERY_PLUGGED_USB
                        || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
                //isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                //             status == BatteryManager.BATTERY_STATUS_FULL;
                //PPApplication.logE("DataWrapper.isPowerSaveMode", "isCharging=" + isCharging);
                if (!isCharging) {
                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    //if (PPApplication.logEnabled()) {
                    //    PPApplication.logE("DataWrapper.isPowerSaveMode", "level=" + level);
                    //    PPApplication.logE("DataWrapper.isPowerSaveMode", "scale=" + scale);
                    //}

                    batteryPct = Math.round(level / (float) scale * 100);
                    //PPApplication.logE("DataWrapper.isPowerSaveMode", "batteryPct=" + batteryPct);

                    if (applicationPowerSaveModeInternal.equals("1") && (batteryPct <= 5))
                        return true;
                    if (applicationPowerSaveModeInternal.equals("2") && (batteryPct <= 15))
                        return true;
                }
            }
        }
        else
        if (applicationPowerSaveModeInternal.equals("3")) {*/
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (powerManager != null)
                    return powerManager.isPowerSaveMode();
            //}
            //return isPowerSaveMode;
        //}

        return false;
    }

    void clearSensorsStartTime() {
        synchronized (eventList) {
            for (Event _event : eventList) {
                clearSensorsStartTime(_event, false/*force*/);
            }
        }
    }

    private void clearSensorsStartTime(Event _event, boolean force) {
        if (force || _event._eventPreferencesSMS._permanentRun) {
            _event._eventPreferencesSMS._startTime = 0;
            //if ((_event != null) && (_event._name != null) && (_event._name.equals("SMS event")))
            //    PPApplication.logE("[SMS sensor] DataWrapper.clearSensorsStartTime", "startTime="+_event._eventPreferencesSMS._startTime);
            DatabaseHandler.getInstance(context.getApplicationContext()).updateSMSStartTime(_event);
            _event._eventPreferencesSMS.removeAlarm(context);
        }

        //if (force || _event._eventPreferencesNotification._permanentRun) {
        //_event._eventPreferencesNotification._startTime = 0;
        //dataWrapper.getDatabaseHandler().updateNotificationStartTime(_event);
        //_event._eventPreferencesNotification.removeAlarm(context);
        //}

        if (force || _event._eventPreferencesNFC._permanentRun) {
            _event._eventPreferencesNFC._startTime = 0;
            DatabaseHandler.getInstance(context.getApplicationContext()).updateNFCStartTime(_event);
            _event._eventPreferencesNFC.removeAlarm(context);
        }

        if (force || _event._eventPreferencesCall._permanentRun) {
            _event._eventPreferencesCall._startTime = 0;
            DatabaseHandler.getInstance(context.getApplicationContext()).updateCallStartTime(_event);
            _event._eventPreferencesCall.removeAlarm(context);
        }

        if (force || _event._eventPreferencesAlarmClock._permanentRun) {
            _event._eventPreferencesAlarmClock._startTime = 0;
            _event._eventPreferencesAlarmClock._alarmPackageName = "";
            DatabaseHandler.getInstance(context.getApplicationContext()).updateAlarmClockStartTime(_event);
            _event._eventPreferencesAlarmClock.removeAlarm(context);
        }

        if (force || _event._eventPreferencesDeviceBoot._permanentRun) {
            _event._eventPreferencesDeviceBoot._startTime = 0;
            DatabaseHandler.getInstance(context.getApplicationContext()).updateDeviceBootStartTime(_event);
            _event._eventPreferencesDeviceBoot.removeAlarm(context);
        }
    }

    boolean eventTypeExists(int eventType/*, boolean onlyRunning*/) {
        synchronized (eventList) {
            for (Event _event : eventList) {
                //boolean eventEnabled;
                //if (onlyRunning)
                //    eventEnabled = _event.getStatus() == Event.ESTATUS_RUNNING;
                //else
                //    eventEnabled = _event.getStatus() != Event.ESTATUS_STOP;
                if (_event.getStatus() != Event.ESTATUS_STOP) {
                    boolean sensorEnabled;
                    switch (eventType) {
                        case DatabaseHandler.ETYPE_TIME:
                            sensorEnabled = _event._eventPreferencesTime._enabled;
                            break;
                        case DatabaseHandler.ETYPE_BATTERY:
                            sensorEnabled = _event._eventPreferencesBattery._enabled;
                            break;
                        case DatabaseHandler.ETYPE_CALL:
                            sensorEnabled = _event._eventPreferencesCall._enabled;
                            break;
                        case DatabaseHandler.ETYPE_PERIPHERAL:
                            sensorEnabled = _event._eventPreferencesPeripherals._enabled;
                            break;
                        case DatabaseHandler.ETYPE_CALENDAR:
                            sensorEnabled = _event._eventPreferencesCalendar._enabled;
                            break;
                        case DatabaseHandler.ETYPE_WIFI_CONNECTED:
                            sensorEnabled = _event._eventPreferencesWifi._enabled;
                            sensorEnabled = sensorEnabled &&
                                    ((_event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_CONNECTED) ||
                                     (_event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED));
                            break;
                        case DatabaseHandler.ETYPE_WIFI_NEARBY:
                            sensorEnabled = _event._eventPreferencesWifi._enabled;
                            sensorEnabled = sensorEnabled &&
                                    ((_event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NEARBY) ||
                                     (_event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY));
                            break;
                        case DatabaseHandler.ETYPE_SCREEN:
                            sensorEnabled = _event._eventPreferencesScreen._enabled;
                            break;
                        case DatabaseHandler.ETYPE_BLUETOOTH_CONNECTED:
                            sensorEnabled = _event._eventPreferencesBluetooth._enabled;
                            sensorEnabled = sensorEnabled &&
                                    ((_event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) ||
                                     (_event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED));
                            break;
                        case DatabaseHandler.ETYPE_BLUETOOTH_NEARBY:
                            sensorEnabled = _event._eventPreferencesBluetooth._enabled;
                            sensorEnabled = sensorEnabled &&
                                    ((_event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NEARBY) ||
                                     (_event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY));
                            break;
                        case DatabaseHandler.ETYPE_SMS:
                            sensorEnabled = _event._eventPreferencesSMS._enabled;
                            break;
                        case DatabaseHandler.ETYPE_NOTIFICATION:
                            sensorEnabled = _event._eventPreferencesNotification._enabled;
                            break;
                        case DatabaseHandler.ETYPE_APPLICATION:
                            sensorEnabled = _event._eventPreferencesApplication._enabled;
                            break;
                        case DatabaseHandler.ETYPE_LOCATION:
                            sensorEnabled = _event._eventPreferencesLocation._enabled;
                            break;
                        case DatabaseHandler.ETYPE_ORIENTATION:
                            sensorEnabled = _event._eventPreferencesOrientation._enabled;
                            break;
                        case DatabaseHandler.ETYPE_MOBILE_CELLS:
                            sensorEnabled = _event._eventPreferencesMobileCells._enabled;
                            break;
                        case DatabaseHandler.ETYPE_NFC:
                            sensorEnabled = _event._eventPreferencesNFC._enabled;
                            break;
                        case DatabaseHandler.ETYPE_RADIO_SWITCH:
                            sensorEnabled = _event._eventPreferencesRadioSwitch._enabled;
                            break;
                        case DatabaseHandler.ETYPE_RADIO_SWITCH_WIFI:
                            sensorEnabled = _event._eventPreferencesRadioSwitch._enabled;
                            sensorEnabled = sensorEnabled &&
                                    (_event._eventPreferencesRadioSwitch._wifi != 0);
                            break;
                        case DatabaseHandler.ETYPE_RADIO_SWITCH_BLUETOOTH:
                            sensorEnabled = _event._eventPreferencesRadioSwitch._enabled;
                            sensorEnabled = sensorEnabled &&
                                    (_event._eventPreferencesRadioSwitch._bluetooth != 0);
                            break;
                        case DatabaseHandler.ETYPE_RADIO_SWITCH_MOBILE_DATA:
                            sensorEnabled = _event._eventPreferencesRadioSwitch._enabled;
                            sensorEnabled = sensorEnabled &&
                                    (_event._eventPreferencesRadioSwitch._mobileData != 0);
                            break;
                        case DatabaseHandler.ETYPE_RADIO_SWITCH_GPS:
                            sensorEnabled = _event._eventPreferencesRadioSwitch._enabled;
                            sensorEnabled = sensorEnabled &&
                                    (_event._eventPreferencesRadioSwitch._gps != 0);
                            break;
                        case DatabaseHandler.ETYPE_RADIO_SWITCH_NFC:
                            sensorEnabled = _event._eventPreferencesRadioSwitch._enabled;
                            sensorEnabled = sensorEnabled &&
                                    (_event._eventPreferencesRadioSwitch._nfc != 0);
                            break;
                        case DatabaseHandler.ETYPE_RADIO_SWITCH_AIRPLANE_MODE:
                            sensorEnabled = _event._eventPreferencesRadioSwitch._enabled;
                            sensorEnabled = sensorEnabled &&
                                    (_event._eventPreferencesRadioSwitch._airplaneMode != 0);
                            break;
                        case DatabaseHandler.ETYPE_ALARM_CLOCK:
                            sensorEnabled = _event._eventPreferencesAlarmClock._enabled;
                            break;
                        case DatabaseHandler.ETYPE_TIME_TWILIGHT:
                            sensorEnabled = _event._eventPreferencesTime._enabled;
                            sensorEnabled = sensorEnabled &&
                                    (_event._eventPreferencesTime._timeType != EventPreferencesTime.TIME_TYPE_EXACT);
                            break;
                        case DatabaseHandler.ETYPE_BATTERY_WITH_LEVEL:
                            sensorEnabled = _event._eventPreferencesBattery._enabled;
                            if (sensorEnabled) {
                                sensorEnabled =
                                        (_event._eventPreferencesBattery._levelLow > 0) ||
                                        (_event._eventPreferencesBattery._levelHight < 100);
                            }
                            break;
                        case DatabaseHandler.ETYPE_ALL_SCANNER_SENSORS:
                            sensorEnabled = _event._eventPreferencesWifi._enabled &&
                                    ((_event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NEARBY) ||
                                     (_event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY));
                            if (!sensorEnabled)
                                sensorEnabled = _event._eventPreferencesBluetooth._enabled &&
                                    ((_event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NEARBY) ||
                                     (_event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY));
                            if (!sensorEnabled) {
                                sensorEnabled = _event._eventPreferencesLocation._enabled;
                                sensorEnabled = sensorEnabled || _event._eventPreferencesMobileCells._enabled;
                                sensorEnabled = sensorEnabled || _event._eventPreferencesOrientation._enabled;
                            }
                            break;
                        case DatabaseHandler.ETYPE_DEVICE_BOOT:
                            sensorEnabled = _event._eventPreferencesDeviceBoot._enabled;
                            break;
                        case DatabaseHandler.ETYPE_ALL:
                        default:
                            sensorEnabled = true;
                            break;
                    }

                    if (sensorEnabled)
                        return true;
                }
            }
            return false;
        }
    }

    boolean profileTypeExists(int profileType/*, boolean sharedProfile*/) {
        synchronized (profileList) {
            for (Profile _profile : profileList) {
                boolean profileEnabled;
                switch (profileType) {
                    case DatabaseHandler.PTYPE_CONNECT_TO_SSID:
                        profileEnabled = !_profile._deviceConnectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY);
                        break;
                    case DatabaseHandler.PTYPE_FORCE_STOP:
                        profileEnabled = _profile._deviceForceStopApplicationChange != 0;
                        break;
                    case DatabaseHandler.PTYPE_LOCK_DEVICE:
                        profileEnabled = _profile._lockDevice != 0;
                        break;
                    default:
                        profileEnabled = true;
                        break;
                }
                if (profileEnabled)
                    return true;
            }
            return false;
        }
    }
}
