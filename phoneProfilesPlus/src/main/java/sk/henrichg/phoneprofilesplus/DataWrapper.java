package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
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
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_volumeRingerMode")),
                Profile.defaultValuesString.get("prf_pref_volumeRingtone"),
                Profile.defaultValuesString.get("prf_pref_volumeNotification"),
                Profile.defaultValuesString.get("prf_pref_volumeMedia"),
                Profile.defaultValuesString.get("prf_pref_volumeAlarm"),
                Profile.defaultValuesString.get("prf_pref_volumeSystem"),
                Profile.defaultValuesString.get("prf_pref_volumeVoice"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_soundRingtoneChange")),
                Settings.System.DEFAULT_RINGTONE_URI.toString(),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_soundNotificationChange")),
                Settings.System.DEFAULT_NOTIFICATION_URI.toString(),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_soundAlarmChange")),
                Settings.System.DEFAULT_ALARM_ALERT_URI.toString(),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceAirplaneMode")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWiFi")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceBluetooth")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceScreenTimeout")),
                Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + Profile.defaultValuesString.get("prf_pref_deviceBrightness_withoutLevel"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWallpaperChange")),
                Profile.defaultValuesString.get("prf_pref_deviceWallpaper"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceMobileData")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceMobileDataPrefs")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceGPS")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceRunApplicationChange")),
                Profile.defaultValuesString.get("prf_pref_deviceRunApplicationPackageName"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceAutosync")),
                Profile.defaultValuesBoolean.get("prf_pref_showInActivator_notShow"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceAutoRotation")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceLocationServicePrefs")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_volumeSpeakerPhone")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceNFC")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_duration")),
                  Profile.AFTER_DURATION_DO_RESTART_EVENTS,
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_volumeZenMode")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceKeyguard")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_vibrationOnTouch")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWiFiAP")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_devicePowerSaveMode")),
                Profile.defaultValuesBoolean.get("prf_pref_askForDuration"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceNetworkType")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_notificationLed")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_vibrateWhenRinging")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWallpaperFor")),
                Profile.defaultValuesBoolean.get("prf_pref_hideStatusBarIcon"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_lockDevice")),
                Profile.defaultValuesString.get("prf_pref_deviceConnectToSSID"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_applicationDisableWifiScanning")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_applicationDisableBluetoothScanning")),
                Profile.defaultValuesString.get("prf_pref_durationNotificationSound"),
                Profile.defaultValuesBoolean.get("prf_pref_durationNotificationVibrate"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWiFiAPPrefs")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_applicationDisableLocationScanning")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_applicationDisableMobileCellScanning")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_applicationDisableOrientationScanning")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_headsUpNotifications")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceForceStopApplicationChange")),
                Profile.defaultValuesString.get("prf_pref_deviceForceStopApplicationPackageName"),
                0,
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceNetworkTypePrefs")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceCloseAllApplications")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_screenCarMode")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_dtmfToneWhenDialing")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_soundOnTouch")),
                Profile.defaultValuesString.get("prf_pref_volumeDTMF"),
                Profile.defaultValuesString.get("prf_pref_volumeAccessibility"),
                Profile.defaultValuesString.get("prf_pref_volumeBluetoothSCO"),
                Long.valueOf(Profile.defaultValuesString.get("prf_pref_afterDurationProfile")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_alwaysOnDisplay")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_screenOnPermanent"))
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
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 1; // ALL
                        /*if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else
                            profile._volumeRingerMode = 1;*/
                    } else
                        profile._volumeRingerMode = 1;
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
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 1; // ALL
                        /*if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else
                            profile._volumeRingerMode = 2;*/
                    } else
                        profile._volumeRingerMode = 2;
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
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 4; // ALL with vibration
                        /*if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 4; // ALL with vibration
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 4; // ALL with vibration
                        } else
                            profile._volumeRingerMode = 1;*/
                    } else
                        profile._volumeRingerMode = 1;
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
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 3; // NONE
                        /*if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 3; // NONE
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 3; // NONE
                        } else
                            profile._volumeRingerMode = 4;*/
                    } else
                        profile._volumeRingerMode = 4;
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
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        profile._volumeRingerMode = 5;
                        profile._volumeZenMode = 6; // ALARMS
                        /*if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 6; // ALARMS
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 3; // NONE
                        } else
                            profile._volumeRingerMode = 4;*/
                    } else
                        profile._volumeRingerMode = 4;
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
            if (!profileListFilled) {
                return getActivatedProfileFromDB(generateIcon, generateIndicators);
            } else {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    if (profile._checked) {
                        return profile;
                    }
                }
                // when profile not found, get profile from db
                return getActivatedProfileFromDB(generateIcon, generateIndicators);
            }
        }
    }

    public Profile getActivatedProfile(List<Profile> profileList) {
        if (profileList == null) {
            return null;
        } else {
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = profileList.iterator(); it.hasNext();) {
                Profile profile = it.next();
                if (profile._checked)
                    return profile;
            }
            return null;
        }
    }

    void setProfileActive(Profile profile)
    {
        synchronized (profileList) {
            if (!profileListFilled)
                return;

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
        if (EditorProfilesActivity.displayRedTextToPreferencesNotification(profile, null, context)) {
            _activateProfile(profile, merged, startupSource, forRestartEvents);
        }
    }

    void updateNotificationAndWidgets(boolean refresh, boolean forService)
    {
        PPApplication.showProfileNotification(/*context*/refresh, forService);
        //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper.updateNotificationAndWidgets");
        ActivateProfileHelper.updateGUI(context, true, refresh);
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
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(9999 + (int) profile._id);

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
                        long fkProfile = Long.valueOf(split);
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
        if (Long.valueOf(ApplicationPreferences.applicationBackgroundProfile) == profile._id)
        {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE));
            editor.apply();
            ApplicationPreferences.applicationBackgroundProfile(context);
        }
    }

    void deleteAllProfiles()
    {
        synchronized (profileList) {
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            // remove notifications about profile parameters errors
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                Profile profile = it.next();
                notificationManager.cancel(9999 + (int) profile._id);
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
        editor.putString(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE));
        editor.apply();
        ApplicationPreferences.applicationBackgroundProfile(context);
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
                //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
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
            shortcutIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
            shortcutIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
        }
        else {
            shortcutIntent = new Intent(context.getApplicationContext(), BackgroundActivateProfileActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            shortcutIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
            shortcutIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        }

        String id;
        String profileName;
        String longLabel;

        if (restartEvents) {
            id = "restart_events";
            profileName = context.getString(R.string.menu_restart_events);
            longLabel = profileName;
        }
        else {
            id = "profile_" + profile._id;
            profileName = profile._name;
            longLabel = /*context.getString(R.string.shortcut_activate_profile) + */profileName;
        }
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

                    shortcutManager.setDynamicShortcuts(shortcuts);
                }
            } catch (Exception e) {
                Log.e("DataWrapper.setDynamicLauncherShortcuts", Log.getStackTraceString(e));
                FirebaseCrashlytics.getInstance().recordException(e);
                //Crashlytics.logException(e);
            }
        }
    }

    void setDynamicLauncherShortcutsFromMainThread()
    {
        //PPApplication.logE("DataWrapper.setDynamicLauncherShortcutsFromMainThread", "start");
        final DataWrapper dataWrapper = copyDataWrapper();

        PPApplication.startHandlerThread("DataWrapper.setDynamicLauncherShortcutsFromMainThread");
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

                    //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=DataWrapper.setDynamicLauncherShortcutsFromMainThread");

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

    void sortEventsByStartOrderAsc()
    {
        class PriorityComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res =  lhs._startOrder - rhs._startOrder;
                return res;
            }
        }

        synchronized (eventList) {
            fillEventList();
            Collections.sort(eventList, new PriorityComparator());
        }
    }

    void sortEventsByStartOrderDesc()
    {
        class PriorityComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res =  rhs._startOrder - lhs._startOrder;
                return res;
            }
        }

        synchronized (eventList) {
            fillEventList();
            Collections.sort(eventList, new PriorityComparator());
        }
    }

    Event getEventById(long id)
    {
        synchronized (eventList) {
            if (!eventListFilled) {
                return DatabaseHandler.getInstance(context).getEvent(id);
            } else {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    if (event._id == id)
                        return event;
                }

                // when filter is set and profile not found, get profile from db
                return DatabaseHandler.getInstance(context).getEvent(id);
            }
        }
    }

    void updateEvent(Event event)
    {
        if (event != null)
        {
            Event origEvent = getEventById(event._id);
            origEvent.copyEvent(event);
        }
    }

    // stops all events associated with profile
    private void stopEventsForProfile(Profile profile, boolean alsoUnlink/*, boolean saveEventStatus*/)
    {
        List<EventTimeline> eventTimelineList = getEventTimelineList(true);

        synchronized (eventList) {
            fillEventList();
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                //if ((event.getStatusFromDB(this) == Event.ESTATUS_RUNNING) &&
                //	(event._fkProfileStart == profile._id))
                if (event._fkProfileStart == profile._id)
                    event.stopEvent(this, eventTimelineList, false, true, true/*saveEventStatus*/, false);
            }
        }
        if (alsoUnlink) {
            unlinkEventsFromProfile(profile);
            DatabaseHandler.getInstance(context).unlinkEventsFromProfile(profile);
        }
        //PPApplication.logE("$$$ restartEvents", "from DataWrapper.stopEventsForProfile");
        //restartEvents(false, true, true, true, true);
        //PPApplication.logE("*********** restartEvents", "from DataWrapper.stopEventsForProfile()");
        restartEventsWithRescan(false, true, false, true, false);
    }

    void stopEventsForProfileFromMainThread(final Profile profile,
                                            @SuppressWarnings("SameParameterValue") final boolean alsoUnlink) {
        final DataWrapper dataWrapper = copyDataWrapper();

        PPApplication.startHandlerThread("DataWrapper.stopEventsForProfileFromMainThread");
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

                    //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=DataWrapper.stopEventsForProfileFromMainThread");

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
        List<EventTimeline> eventTimelineList = getEventTimelineList(true);

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
                            event.pauseEvent(this, eventTimelineList, false, true, noSetSystemEvent, true, null, false, false);
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

        PPApplication.startHandlerThread("DataWrapper.pauseAllEventsForGlobalStopEvents");
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

                    //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=DataWrapper.pauseAllEventsForGlobalStopEvents");

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
    void stopAllEvents(boolean saveEventStatus, boolean alsoDelete/*, boolean activateReturnProfile*/, boolean log)
    {
        List<EventTimeline> eventTimelineList = getEventTimelineList(true);
        synchronized (eventList) {
            fillEventList();
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                //if (event.getStatusFromDB(this) != Event.ESTATUS_STOP)
                event.stopEvent(this, eventTimelineList, false/*activateReturnProfile*/,
                        true, saveEventStatus, log);
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

        PPApplication.startHandlerThread("DataWrapper.stopAllEventsFromMainThread");
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

                    //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=DataWrapper.stopAllEventsFromMainThread");

                    dataWrapper.stopAllEvents(saveEventStatus, alsoDelete, true);

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
                profileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(context));
                if (profileId == Profile.PROFILE_NO_ACTIVATE)
                    profileId = 0;
            }
            */
            long profileId = PPApplication.prefLastActivatedProfile;
            //PPApplication.logE("DataWrapper.activateProfileOnBoot", "lastActivatedProfile="+profileId);
            if (profileId == 0) {
                profileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile);
                if (profileId == Profile.PROFILE_NO_ACTIVATE)
                    profileId = 0;
            }

            activateProfile(profileId, PPApplication.STARTUP_SOURCE_BOOT, null/*, ""*/);
        }
        else
            activateProfile(0, PPApplication.STARTUP_SOURCE_BOOT, null/*, ""*/);
    }

    private void startEventsOnBoot(boolean startedFromService, boolean useHandler)
    {
        if (startedFromService) {
            if (ApplicationPreferences.applicationStartEvents) {
                //restartEvents(false, false, true, false, useHandler);
                //PPApplication.logE("*********** restartEvents", "from DataWrapper.startEventsOnBoot() - 1");
                restartEventsWithRescan(/*true, */false, useHandler, false, false, false);
            }
            else {
                Event.setGlobalEventsRunning(context, false);
                activateProfileOnBoot();
            }
        }
        else {
            //restartEvents(false, false, true, false, useHandler);
            //PPApplication.logE("*********** restartEvents", "from DataWrapper.startEventsOnBoot() - 2");
            restartEventsWithRescan(false, useHandler, false, false, false);
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
            startEventsOnBoot(startedFromService, useHandler);
        }
        else
        {
            PPApplication.logE("DataWrapper.firstStartEvents", "manual profile activation, activate profile");
            activateProfileOnBoot();
            startEventsOnBoot(startedFromService, useHandler);
        }
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

        for (int index = 0; index < 6; index++) {
            Event event = getPredefinedEvent(index, true, baseContext);
            if (event != null)
                eventList.add(event);
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
        // show notification when battery optimization is not enabled
        IgnoreBatteryOptimizationNotification.showNotification(context);

        // remove last configured profile duration alarm
        ProfileDurationAlarmBroadcastReceiver.removeAlarm(_profile, context);
        Profile.setActivatedProfileForDuration(context, 0);

        //final Profile mappedProfile = _profile; //Profile.getMappedProfile(_profile, context);
        //profile = filterProfileWithBatteryEvents(profile);

        /*if (_profile != null)
            PPApplication.logE("$$$ DataWrapper._activateProfile","profileName="+_profile._name);
        else
            PPApplication.logE("$$$ DataWrapper._activateProfile","profile=null");*/

        boolean fullyStarted = false;
        if (PhoneProfilesService.getInstance() != null)
            fullyStarted = PhoneProfilesService.getInstance().getApplicationFullyStarted();
        boolean applicationPackageReplaced = PPApplication.applicationPackageReplaced;
        if ((!fullyStarted) || applicationPackageReplaced) {
            // do not activate profile during application start
            PPApplication.showProfileNotification(/*context*/startupSource == PPApplication.STARTUP_SOURCE_BOOT, false);
            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper._activateProfile");
            ActivateProfileHelper.updateGUI(context, true, startupSource == PPApplication.STARTUP_SOURCE_BOOT);
            return;
        }

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("$$$ DataWrapper._activateProfile", "startupSource=" + startupSource);
            PPApplication.logE("$$$ DataWrapper._activateProfile", "merged=" + merged);
        }*/

//        if (PPApplication.logEnabled()) {
//            if (PhoneProfilesService.getInstance() != null) {
//                PPApplication.logE("### DataWrapper._activateProfile", "serviceHasFirstStart=" + PhoneProfilesService.getInstance().getServiceHasFirstStart());
//                PPApplication.logE("### DataWrapper._activateProfile", "serviceRunning=" + PhoneProfilesService.getInstance().getServiceRunning());
//            }
//        }

        //boolean interactive = _interactive;
        //final Activity activity = _activity;

        // get currently activated profile
        Profile activatedProfile = getActivatedProfile(false, false);

        if ((startupSource != PPApplication.STARTUP_SOURCE_SERVICE) &&
            //(startupSource != PPApplication.STARTUP_SOURCE_BOOT) &&  // on boot must set as manual activation
            (startupSource != PPApplication.STARTUP_SOURCE_LAUNCHER_START))
        {
            // manual profile activation

            ActivateProfileHelper.lockRefresh = true;

            // pause all events
            // for forceRun events set system events and block all events
            pauseAllEvents(false, true/*, true*/);

            ActivateProfileHelper.lockRefresh = false;
        }

        //PPApplication.logE("$$$ DataWrapper._activateProfile","before activation");

        DatabaseHandler.getInstance(context).activateProfile(_profile);
        setProfileActive(_profile);
        if (_profile != null)
            Profile.saveProfileToSharedPreferences(_profile, context, PPApplication.ACTIVATED_PROFILE_PREFS_NAME);

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

            // activation with duration
            if (((startupSource != PPApplication.STARTUP_SOURCE_SERVICE) &&
                 (startupSource != PPApplication.STARTUP_SOURCE_BOOT) &&
                 (startupSource != PPApplication.STARTUP_SOURCE_LAUNCHER_START)) ||
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
        }

        //PPApplication.logE("$$$ DataWrapper._activateProfile","before update GUI");

        PPApplication.showProfileNotification(/*context*/startupSource == PPApplication.STARTUP_SOURCE_BOOT, false);
        //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper._activateProfile");
        ActivateProfileHelper.updateGUI(context, true, startupSource == PPApplication.STARTUP_SOURCE_BOOT);

        //PPApplication.logE("$$$ DataWrapper._activateProfile","after update GUI");

        //if (mappedProfile != null) {
            //PPApplication.logE("$$$ DataWrapper._activateProfile","execute activation");
            ActivateProfileHelper.execute(context, _profile);
        //}

        if (/*(mappedProfile != null) &&*/ (!merged)) {
            PPApplication.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ACTIVATION, null,
                    getProfileNameWithManualIndicatorAsString(_profile, true, "", profileDuration > 0, false, false, this, false, context),
                    profileIcon, profileDuration, "");
        }

        //if (mappedProfile != null)
        //{
            if (ApplicationPreferences.notificationsToast && (!ActivateProfileHelper.lockRefresh))
            {
                // toast notification
                if (PPApplication.toastHandler != null) {
                    final Profile __profile = _profile;
                    PPApplication.toastHandler.post(new Runnable() {
                        public void run() {
                            showToastAfterActivation(__profile);
                        }
                    });
                }// else
                //    showToastAfterActivation(profile);
            }
        //}
    }

    void activateProfileFromMainThread(final Profile profile, final boolean merged, final int startupSource,
                                    final boolean interactive, final Activity _activity)
    {
        //PPApplication.logE("$$$$$ DataWrapper.activateProfileFromMainThread", "start");
        final DataWrapper dataWrapper = copyDataWrapper();

        PPApplication.startHandlerThread("DataWrapper.activateProfileFromMainThread");
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

                    //PPApplication.logE("$$$$$ PPApplication.startHandlerThread", "START run - from=DataWrapper.activateProfileFromMainThread");

                    dataWrapper._activateProfile(profile, merged, startupSource, false);
                    if (interactive) {
                        DatabaseHandler.getInstance(dataWrapper.context).increaseActivationByUserCount(profile);
                        dataWrapper.setDynamicLauncherShortcuts();
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
        boolean fullyStarted = false;
        if (PhoneProfilesService.getInstance() != null)
            fullyStarted = PhoneProfilesService.getInstance().getApplicationFullyStarted();
        fullyStarted = fullyStarted && (!PPApplication.applicationPackageReplaced);

        if (!fullyStarted)
            return;

        try {
            String profileName = getProfileNameWithManualIndicatorAsString(profile, true, "", false, false, false, this, false, context);
            GlobalGUIRoutines.showToast(context.getApplicationContext(),
                    context.getResources().getString(R.string.toast_profile_activated_0) + ": " + profileName + " " +
                            context.getResources().getString(R.string.toast_profile_activated_1),
                    Toast.LENGTH_SHORT);
        }
        catch (Exception ignored) {
        }
        //Log.d("DataWrapper.showToastAfterActivation", "-- end");
    }

    private void activateProfileWithAlert(Profile profile, int startupSource, /*final boolean interactive,*/
                                            Activity activity)
    {
        PPApplication.setBlockProfileEventActions(false, context);

        if (/*interactive &&*/ (ApplicationPreferences.applicationActivateWithAlert ||
                            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR)))
        {
            // set theme and language for dialog alert ;-)
            // not working on Android 2.3.x
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
                            if (EditorProfilesActivity.displayRedTextToPreferencesNotification(_profile, null, context))
                                _dataWrapper.activateProfileFromMainThread(_profile, false, _startupSource, true, _activity);
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
                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        if (positive != null) positive.setAllCaps(false);
                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        if (negative != null) negative.setAllCaps(false);
                    }
                });*/
                if (!activity.isFinishing())
                    dialog.show();
            }
        }
        else
        {
            if (profile._askForDuration/* && interactive*/) {
                if (!activity.isFinishing()) {
                    FastAccessDurationDialog dlg = new FastAccessDurationDialog(activity, profile, this,
                            /*monochrome, monochromeValue,*/ startupSource);
                    dlg.show();
                }
            }
            else {
                boolean granted;
                GlobalGUIRoutines.setTheme(activity, true, true/*, false*/, false);
                //GlobalGUIRoutines.setLanguage(activity);

                //granted = Permissions.grantProfilePermissions(context, profile, false, true,
                //        /*false, monochrome, monochromeValue,*/
                //        startupSource, true, true, false);
                granted = EditorProfilesActivity.displayRedTextToPreferencesNotification(profile, null, context);
                if (granted)
                    activateProfileFromMainThread(profile, false, startupSource, true, activity);
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
                    try {
                        //if (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR)
                        //    _activity.finishAndRemoveTask();
                        //else
                            _activity.finish();
                    } catch (Exception ignored) {}
                }
            });
        }
    }

    public void activateProfile(final long profile_id, final int startupSource, final Activity activity)
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
        else
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
        }

        if ((startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT) ||
            (startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SERVICE) ||
            (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER_START) ||
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
            if (startupSource == PPApplication.STARTUP_SOURCE_BOOT)
                activateProfileFromMainThread(profile, false, PPApplication.STARTUP_SOURCE_BOOT,
                                        false, null);
            else
                activateProfileWithAlert(profile, startupSource, /*interactive,*/ activity);
        }
        else
        {
            DatabaseHandler.getInstance(context).activateProfile(profile);
            setProfileActive(profile);

            PPApplication.showProfileNotification(/*context*/false, false);
            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper.activateProfile");
            ActivateProfileHelper.updateGUI(context, true, startupSource == PPApplication.STARTUP_SOURCE_BOOT);

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
            PPApplication.showProfileNotification(/*context*/false, false);
            //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper.activateProfileAfterDuration");
            ActivateProfileHelper.updateGUI(context, true, false);
            return;
        }
        //if (Permissions.grantProfilePermissions(context, profile, false, true,
        //        /*false, monochrome, monochromeValue,*/
        //        startupSource, true,true, false)) {
        if (EditorProfilesActivity.displayRedTextToPreferencesNotification(profile, null, context)) {
            // activateProfileAfterDuration is already called from handlerThread
            //PPApplication.logE("DataWrapper.activateProfileAfterDuration", "activate");
            _activateProfile(profile, false, startupSource, false);
        }
    }

    boolean startProfileMerged;
    boolean endProfileMerged;

    @SuppressLint({ "NewApi", "SimpleDateFormat" })
    void doHandleEvents(Event event, boolean statePause,
                                    boolean forRestartEvents, /*boolean interactive,*/
                                    boolean forDelayStartAlarm, boolean forDelayEndAlarm,
                                    /*boolean reactivate,*/ Profile mergedProfile,
                                    String sensorType)
    {
        //if (!Permissions.grantEventPermissions(context, event, true, false))
        if (!EditorProfilesActivity.displayRedTextToPreferencesNotification(null, event, context))
            return;

        startProfileMerged = false;
        endProfileMerged = false;

        int newEventStatus;// = Event.ESTATUS_NONE;

        boolean notAllowedTime = false;
        boolean notAllowedBattery = false;
        boolean notAllowedCall = false;
        boolean notAllowedPeripheral = false;
        boolean notAllowedCalendar = false;
        boolean notAllowedWifi = false;
        boolean notAllowedScreen = false;
        boolean notAllowedBluetooth = false;
        boolean notAllowedSms = false;
        boolean notAllowedNotification = false;
        boolean notAllowedApplication = false;
        boolean notAllowedLocation = false;
        boolean notAllowedOrientation = false;
        boolean notAllowedMobileCell = false;
        boolean notAllowedNfc = false;
        boolean notAllowedRadioSwitch = false;
        boolean notAllowedAlarmClock = false;

        boolean timePassed = true;
        boolean batteryPassed = true;
        boolean callPassed = true;
        boolean peripheralPassed = true;
        boolean calendarPassed = true;
        boolean wifiPassed = true;
        boolean screenPassed = true;
        boolean bluetoothPassed = true;
        boolean smsPassed = true;
        boolean notificationPassed = true;
        boolean applicationPassed = true;
        boolean locationPassed = true;
        boolean orientationPassed = true;
        boolean mobileCellPassed = true;
        boolean nfcPassed = true;
        boolean radioSwitchPassed = true;
        boolean alarmClockPassed = true;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("%%%%%%% DataWrapper.doHandleEvents", "--- start --------------------------");
            PPApplication.logE("%%%%%%% DataWrapper.doHandleEvents", "------- event._id=" + event._id);
            PPApplication.logE("%%%%%%% DataWrapper.doHandleEvents", "------- event._name=" + event._name);
            PPApplication.logE("%%%%%%% DataWrapper.doHandleEvents", "------- sensorType=" + sensorType);
        }*/

        if (event._eventPreferencesTime._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesTime.PREF_EVENT_TIME_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                    && Permissions.checkEventLocation(context, event, null)) {
                /*boolean testEvent = (event._name != null) && event._name.equals("Plugged In Nighttime");
                if (testEvent) {
                    if (PPApplication.logEnabled()) {
                        PPApplication.logE("[TIME] DataWrapper.doHandleEvents", "------- event._id=" + event._id);
                        PPApplication.logE("[TIME] DataWrapper.doHandleEvents", "------- event._name=" + event._name);
                    }
                }*/

                // compute start datetime
                long startAlarmTime;
                long endAlarmTime;

                startAlarmTime = event._eventPreferencesTime.computeAlarm(true, context);
                endAlarmTime = event._eventPreferencesTime.computeAlarm(false, context);

                //if (startAlarmTime > 0) {
                    //String alarmTimeS = DateFormat.getDateFormat(context).format(startAlarmTime) +
                    //        " " + DateFormat.getTimeFormat(context).format(startAlarmTime);
                    //if (testEvent)
                    //    PPApplication.logE("[TIME] DataWrapper.doHandleEvents", "startAlarmTime=" + alarmTimeS);
                //}
                //else
                //if (testEvent)
                //    PPApplication.logE("[TIME] DataWrapper.doHandleEvents", "startAlarmTime=not alarm computed");
                //if (endAlarmTime > 0) {
                    //String alarmTimeS = DateFormat.getDateFormat(context).format(endAlarmTime) +
                    //        " " + DateFormat.getTimeFormat(context).format(endAlarmTime);
                    //if (testEvent)
                    //    PPApplication.logE("[TIME] DataWrapper.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                //}
                //else
                //if (testEvent)
                //    PPApplication.logE("[TIME] DataWrapper.doHandleEvents", "endAlarmTime=not alarm computed");

                Calendar now = Calendar.getInstance();
                long nowAlarmTime = now.getTimeInMillis();
                //String alarmTimeS = DateFormat.getDateFormat(context).format(nowAlarmTime) +
                //        " " + DateFormat.getTimeFormat(context).format(nowAlarmTime);
                //if (testEvent)
                //    PPApplication.logE("[TIME] DataWrapper.doHandleEvents", "nowAlarmTime=" + alarmTimeS);

                /*boolean[] daysOfWeek =  new boolean[8];
                daysOfWeek[Calendar.SUNDAY] = event._eventPreferencesTime._sunday;
                daysOfWeek[Calendar.MONDAY] = event._eventPreferencesTime._monday;
                daysOfWeek[Calendar.TUESDAY] = event._eventPreferencesTime._tuesday;
                daysOfWeek[Calendar.WEDNESDAY] = event._eventPreferencesTime._wednesday;
                daysOfWeek[Calendar.THURSDAY] = event._eventPreferencesTime._thursday;
                daysOfWeek[Calendar.FRIDAY] = event._eventPreferencesTime._friday;
                daysOfWeek[Calendar.SATURDAY] = event._eventPreferencesTime._saturday;*/

                Calendar calStartTime = Calendar.getInstance();
                calStartTime.setTimeInMillis(startAlarmTime);
                //int startDayOfWeek = calStartTime.get(Calendar.DAY_OF_WEEK);
                //if (daysOfWeek[startDayOfWeek])
                //{
                    // startTime of week is selected
                    //if (testEvent)
                    //    PPApplication.logE("[TIME] DataWrapper.doHandleEvents","startTime of week is selected");
                    if ((startAlarmTime > 0) && (endAlarmTime > 0))
                        timePassed = ((nowAlarmTime >= startAlarmTime) && (nowAlarmTime < endAlarmTime));
                    else
                        timePassed = false;
                /*}
                else {
                    PPApplication.logE("[TIME] DataWrapper.doHandleEvents","startTime of week is NOT selected");
                    timePassed = false;
                }*/

                //if (testEvent)
                //    PPApplication.logE("[TIME] DataWrapper.doHandleEvents", "timePassed=" + timePassed);

                if (!notAllowedTime) {
                    if (timePassed)
                        event._eventPreferencesTime.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesTime.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedTime = true;
            event._eventPreferencesTime.setSensorPassed(event._eventPreferencesTime.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_TIME);
        }

        if (event._eventPreferencesBattery._enabled) {
            if (Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                boolean isPowerSaveMode = isPowerSaveMode(context);
                //PPApplication.logE("[BAT] DataWrapper.doHandleEvents", "isPowerSaveMode=" + isPowerSaveMode);

                boolean isCharging;
                int batteryPct;
                int plugged;

                // get battery status
                Intent batteryStatus = null;
                try { // Huawei devices: java.lang.IllegalArgumentException: registered too many Broadcast Receivers
                    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    batteryStatus = context.registerReceiver(null, filter);
                } catch (Exception ignored) {
                }

                if (batteryStatus != null) {
                    batteryPassed = false;

                    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    //PPApplication.logE("[BAT] DataWrapper.doHandleEvents", "status=" + status);
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;
                    //PPApplication.logE("[BAT] DataWrapper.doHandleEvents", "isCharging=" + isCharging);
                    plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    //PPApplication.logE("[BAT] DataWrapper.doHandleEvents", "plugged=" + plugged);

                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("[BAT] DataWrapper.doHandleEvents", "level=" + level);
                        PPApplication.logE("[BAT] DataWrapper.doHandleEvents", "scale=" + scale);
                    }*/

                    batteryPct = Math.round(level / (float) scale * 100);
                    //PPApplication.logE("[BAT] DataWrapper.doHandleEvents", "batteryPct=" + batteryPct);

                    if ((batteryPct >= event._eventPreferencesBattery._levelLow) &&
                            (batteryPct <= event._eventPreferencesBattery._levelHight))
                        batteryPassed = true;

                    if ((event._eventPreferencesBattery._charging > 0) ||
                            ((event._eventPreferencesBattery._plugged != null) &&
                             (!event._eventPreferencesBattery._plugged.isEmpty()))){
                        if (event._eventPreferencesBattery._charging == 1)
                            batteryPassed = batteryPassed && isCharging;
                        else
                        if (event._eventPreferencesBattery._charging == 2)
                            batteryPassed = batteryPassed && (!isCharging);
                        //PPApplication.logE("[BAT] DataWrapper.doHandleEvents", "event._eventPreferencesBattery._plugged=" + event._eventPreferencesBattery._plugged);
                        if ((event._eventPreferencesBattery._plugged != null) &&
                            (!event._eventPreferencesBattery._plugged.isEmpty())) {
                            String[] splits = event._eventPreferencesBattery._plugged.split("\\|");
                            //PPApplication.logE("[BAT] DataWrapper.doHandleEvents", "splits.length=" + splits.length);
                            if (splits.length > 0) {
                                boolean passed = false;
                                for (String split : splits) {
                                    try {
                                        int plug = Integer.valueOf(split);
                                        //PPApplication.logE("[BAT] DataWrapper.doHandleEvents", "plug=" + plug);
                                        if ((plug == 1) && (plugged == BatteryManager.BATTERY_PLUGGED_AC)) {
                                            passed = true;
                                            break;
                                        }
                                        if ((plug == 2) && (plugged == BatteryManager.BATTERY_PLUGGED_USB)) {
                                            passed = true;
                                            break;
                                        }
                                        if ((plug == 3) && (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS)) {
                                            passed = true;
                                            break;
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }
                                batteryPassed = batteryPassed && passed;
                            }
                        }
                    } else if (event._eventPreferencesBattery._powerSaveMode)
                        batteryPassed = batteryPassed && isPowerSaveMode;
                } else
                    notAllowedBattery = true;

                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("[BAT] DataWrapper.doHandleEvents", "notAllowedBattery=" + notAllowedBattery);
                    PPApplication.logE("[BAT] DataWrapper.doHandleEvents", "batteryPassed=" + batteryPassed);
                }*/

                if (!notAllowedBattery) {
                    if (batteryPassed)
                        event._eventPreferencesBattery.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesBattery.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedBattery = true;
            event._eventPreferencesBattery.setSensorPassed(event._eventPreferencesBattery.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_BATTERY);
        }

        if (event._eventPreferencesCall._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) &&
                    Permissions.checkEventCallContacts(context, event, null)/* &&
                    this is not required, is only for simulating ringing -> Permissions.checkEventPhoneBroadcast(context, event, null)*/) {
                int callEventType = ApplicationPreferences.prefEventCallEventType;
                String phoneNumber = ApplicationPreferences.prefEventCallPhoneNumber;

                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "callEventType=" + callEventType);
                    PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "phoneNumber=" + phoneNumber);
                }*/

                boolean phoneNumberFound = false;

                if (callEventType != EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED) {
                    if (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_SERVICE_UNBIND)
                        callPassed = false;
                    else
                        phoneNumberFound = event._eventPreferencesCall.isPhoneNumberConfigured(phoneNumber/*, this*/);

                    //PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "phoneNumberFound=" + phoneNumberFound);

                    if (phoneNumberFound) {
                        if (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_RINGING) {
                            //noinspection StatementWithEmptyBody
                            if ((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_RINGING) ||
                                    ((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ANSWERED)))
                                ;//eventStart = eventStart && true;
                            else
                                callPassed = false;
                        } else if (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ANSWERED) {
                            //noinspection StatementWithEmptyBody
                            if (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ANSWERED)
                                ;//eventStart = eventStart && true;
                            else
                                callPassed = false;
                        } else if (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_STARTED) {
                            //noinspection StatementWithEmptyBody
                            if (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_OUTGOING_CALL_ANSWERED)
                                ;//eventStart = eventStart && true;
                            else
                                callPassed = false;
                        } else
                        if ((event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL) ||
                            (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ENDED) ||
                            (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_ENDED)) {
                            if (event._eventPreferencesCall._startTime > 0) {
                                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                                long startTime = event._eventPreferencesCall._startTime - gmtOffset;

                                /*if (PPApplication.logEnabled()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                                    String alarmTimeS = sdf.format(startTime);
                                    PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "startTime=" + alarmTimeS);
                                }*/

                                // compute end datetime
                                long endAlarmTime = event._eventPreferencesCall.computeAlarm();
                                /*if (PPApplication.logEnabled()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                                    String alarmTimeS = sdf.format(endAlarmTime);
                                    PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                                }*/

                                Calendar now = Calendar.getInstance();
                                long nowAlarmTime = now.getTimeInMillis();
                                /*if (PPApplication.logEnabled()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                                    String alarmTimeS = sdf.format(nowAlarmTime);
                                    PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "nowAlarmTime=" + alarmTimeS);
                                }*/

                                if (sensorType.equals(EventsHandler.SENSOR_TYPE_PHONE_CALL)) {
                                    //noinspection StatementWithEmptyBody
                                    if (((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_MISSED_CALL) && (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL)) ||
                                        ((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ENDED) && (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ENDED)) ||
                                        ((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_OUTGOING_CALL_ENDED) && (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_ENDED)))
                                        ;//eventStart = eventStart && true;
                                    else
                                        callPassed = false;
                                } else if (!event._eventPreferencesCall._permanentRun) {
                                    if (sensorType.equals(EventsHandler.SENSOR_TYPE_PHONE_CALL_EVENT_END))
                                        callPassed = false;
                                    else
                                        callPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                                } else {
                                    callPassed = nowAlarmTime >= startTime;
                                }
                            } else
                                callPassed = false;
                        }

                        //if ((callEventType == PhoneCallBroadcastReceiver.CALL_EVENT_INCOMING_CALL_ENDED) ||
                        //        (callEventType == PhoneCallBroadcastReceiver.CALL_EVENT_OUTGOING_CALL_ENDED)) {
                        //    //callPassed = true;
                        //    //eventStart = eventStart && false;
                        //    callPassed = false;
                        //}
                    } else
                        callPassed = false;

                    //PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "callPassed=" + callPassed);

                    if (!callPassed) {
                        //PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "startTime=0");
                        event._eventPreferencesCall._startTime = 0;
                        DatabaseHandler.getInstance(context).updateCallStartTime(event);
                    }
                } else {
                    if ((event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL) ||
                        (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ENDED) ||
                        (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_ENDED)) {
                        if (event._eventPreferencesCall._startTime > 0) {
                            int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                            long startTime = event._eventPreferencesCall._startTime - gmtOffset;

                            /*if (PPApplication.logEnabled()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                                String alarmTimeS = sdf.format(startTime);
                                PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "startTime=" + alarmTimeS);
                            }*/

                            // compute end datetime
                            long endAlarmTime = event._eventPreferencesCall.computeAlarm();
                            /*if (PPApplication.logEnabled()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                                String alarmTimeS = sdf.format(endAlarmTime);
                                PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                            }*/

                            Calendar now = Calendar.getInstance();
                            long nowAlarmTime = now.getTimeInMillis();
                            /*if (PPApplication.logEnabled()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                                String alarmTimeS = sdf.format(nowAlarmTime);
                                PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "nowAlarmTime=" + alarmTimeS);
                            }*/

                            if (!event._eventPreferencesCall._permanentRun) {
                                if (sensorType.equals(EventsHandler.SENSOR_TYPE_PHONE_CALL_EVENT_END))
                                    callPassed = false;
                                else
                                    callPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                            } else {
                                callPassed = nowAlarmTime >= startTime;
                            }
                        }
                        else
                            callPassed = false;

                        if (!callPassed) {
                            //PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "startTime=0");
                            event._eventPreferencesCall._startTime = 0;
                            DatabaseHandler.getInstance(context).updateCallStartTime(event);
                        }
                    }
                    else
                        notAllowedCall = true;
                }

                if (!notAllowedCall) {
                    if (callPassed)
                        event._eventPreferencesCall.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesCall.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            }
            else
                notAllowedCall = true;
            event._eventPreferencesCall.setSensorPassed(event._eventPreferencesCall.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_CALL);
        }

        if (event._eventPreferencesPeripherals._enabled) {
            if (Event.isEventPreferenceAllowed(EventPreferencesPeripherals.PREF_EVENT_PERIPHERAL_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_DESK_DOCK) ||
                        (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_CAR_DOCK)) {
                    // get dock status
                    IntentFilter iFilter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
                    Intent dockStatus = context.registerReceiver(null, iFilter);

                    if (dockStatus != null) {
                        int dockState = dockStatus.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
                        boolean isDocked = dockState != Intent.EXTRA_DOCK_STATE_UNDOCKED;
                        boolean isCar = dockState == Intent.EXTRA_DOCK_STATE_CAR;
                        boolean isDesk = dockState == Intent.EXTRA_DOCK_STATE_DESK ||
                                dockState == Intent.EXTRA_DOCK_STATE_LE_DESK ||
                                dockState == Intent.EXTRA_DOCK_STATE_HE_DESK;

                        if (isDocked) {
                            if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_DESK_DOCK)
                                    && isDesk)
                                peripheralPassed = true;
                            else
                                peripheralPassed = (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_CAR_DOCK)
                                        && isCar;
                        } else
                            peripheralPassed = false;
                        //eventStart = eventStart && peripheralPassed;
                    } else
                        notAllowedPeripheral = true;
                } else if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_WIRED_HEADSET) ||
                        (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_BLUETOOTH_HEADSET) ||
                        (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_HEADPHONES)) {
                    boolean wiredHeadsetConnected = ApplicationPreferences.prefWiredHeadsetConnected;
                    boolean wiredHeadsetMicrophone = ApplicationPreferences.prefWiredHeadsetMicrophone;
                    boolean bluetoothHeadsetConnected = ApplicationPreferences.prefBluetoothHeadsetConnected;
                    boolean bluetoothHeadsetMicrophone = ApplicationPreferences.prefBluetoothHeadsetMicrophone;

                    peripheralPassed = false;
                    if (wiredHeadsetConnected) {
                        if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_WIRED_HEADSET)
                                && wiredHeadsetMicrophone)
                            peripheralPassed = true;
                        else
                        if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_HEADPHONES)
                                    && (!wiredHeadsetMicrophone))
                            peripheralPassed = true;
                    }
                    if (bluetoothHeadsetConnected) {
                        if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_BLUETOOTH_HEADSET)
                                && bluetoothHeadsetMicrophone)
                            peripheralPassed = true;
                    }
                    //eventStart = eventStart && peripheralPassed;
                }

                if (!notAllowedPeripheral) {
                    if (peripheralPassed)
                        event._eventPreferencesPeripherals.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesPeripherals.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedPeripheral = true;
            event._eventPreferencesPeripherals.setSensorPassed(event._eventPreferencesPeripherals.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_PERIPHERAL);
        }

        if (event._eventPreferencesCalendar._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) &&
                    (Permissions.checkEventCalendar(context, event, null))) {
                // compute start datetime
                long startAlarmTime;
                long endAlarmTime;

                if (event._eventPreferencesCalendar._eventFound) {
                    startAlarmTime = event._eventPreferencesCalendar.computeAlarm(true);

                    //String alarmTimeS = DateFormat.getDateFormat(context).format(startAlarmTime) +
                    //        " " + DateFormat.getTimeFormat(context).format(startAlarmTime);
                    //PPApplication.logE("DataWrapper.doHandleEvents", "startAlarmTime=" + alarmTimeS);

                    endAlarmTime = event._eventPreferencesCalendar.computeAlarm(false);

                    //alarmTimeS = DateFormat.getDateFormat(context).format(endAlarmTime) +
                    //        " " + DateFormat.getTimeFormat(context).format(endAlarmTime);
                    //PPApplication.logE("DataWrapper.doHandleEvents", "endAlarmTime=" + alarmTimeS);

                    Calendar now = Calendar.getInstance();
                    long nowAlarmTime = now.getTimeInMillis();
                    //alarmTimeS = DateFormat.getDateFormat(context).format(nowAlarmTime) +
                    //        " " + DateFormat.getTimeFormat(context).format(nowAlarmTime);
                    //PPApplication.logE("DataWrapper.doHandleEvents", "nowAlarmTime=" + alarmTimeS);

                    calendarPassed = ((nowAlarmTime >= startAlarmTime) && (nowAlarmTime < endAlarmTime));
                } else
                    calendarPassed = false;

                if (!notAllowedCalendar) {
                    if (calendarPassed)
                        event._eventPreferencesCalendar.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesCalendar.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedCalendar = true;
            event._eventPreferencesCalendar.setSensorPassed(event._eventPreferencesCalendar.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_CALENDAR);
        }
        

        if (event._eventPreferencesWifi._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                    && Permissions.checkEventLocation(context, event, null)) {
                //if (event._name.equals("Doma"))
                //    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "-------- eventSSID=" + event._eventPreferencesWifi._SSID);

                wifiPassed = false;

                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                boolean isWifiEnabled = wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;

                List<WifiSSIDData> wifiConfigurationList = WifiScanWorker.getWifiConfigurationList(context);

                boolean done = false;

                if (isWifiEnabled) {
                    //if (event._name.equals("Doma"))
                    //    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "wifiStateEnabled=true");

                    //PPApplication.logE("----- DataWrapper.doHandleEvents","-- eventSSID="+event._eventPreferencesWifi._SSID);

                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                    boolean wifiConnected = false;

                    ConnectivityManager connManager = null;
                    try {
                        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    } catch (Exception ignored) {
                        // java.lang.NullPointerException: missing IConnectivityManager
                        // Dual SIM?? Bug in Android ???
                    }
                    if (connManager != null) {
                        //if (android.os.Build.VERSION.SDK_INT >= 21) {
                            Network[] networks = connManager.getAllNetworks();
                            if ((networks != null) && (networks.length > 0)) {
                                for (Network network : networks) {
                                    try {
                                        if (Build.VERSION.SDK_INT < 28) {
                                            NetworkInfo ntkInfo = connManager.getNetworkInfo(network);
                                            if (ntkInfo != null) {
                                                if (ntkInfo.getType() == ConnectivityManager.TYPE_WIFI && ntkInfo.isConnected()) {
                                                    if (wifiInfo != null) {
                                                        wifiConnected = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        else {
                                            NetworkInfo networkInfo = connManager.getNetworkInfo(network);
                                            if((networkInfo != null) && networkInfo.isConnected()) {
                                                NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                                                if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                                    wifiConnected = true;
                                                    break;
                                                }
                                            }
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        /*} else {
                            //noinspection deprecation
                            NetworkInfo ntkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                            wifiConnected = (ntkInfo != null) && ntkInfo.isConnected();
                        }*/
                    }

                    if (wifiConnected) {
                        /*if (PPApplication.logEnabled()) {
                            if (event._name.equals("Doma")) {
                                PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "wifi connected");
                                PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "wifiSSID=" + WifiScanWorker.getSSID(wifiManager, wifiInfo, wifiConfigurationList));
                                PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "wifiBSSID=" + wifiInfo.getBSSID());
                            }
                        }*/

                        //PPApplication.logE("----- DataWrapper.doHandleEvents","SSID="+event._eventPreferencesWifi._SSID);

                        String[] splits = event._eventPreferencesWifi._SSID.split("\\|");
                        boolean[] connected = new boolean[splits.length];

                        int i = 0;
                        for (String _ssid : splits) {
                            connected[i] = false;
                            switch (_ssid) {
                                case EventPreferencesWifi.ALL_SSIDS_VALUE:
                                    connected[i] = true;
                                    break;
                                case EventPreferencesWifi.CONFIGURED_SSIDS_VALUE:
                                    for (WifiSSIDData data : wifiConfigurationList) {
                                        connected[i] = WifiScanWorker.compareSSID(wifiManager, wifiInfo, data.ssid.replace("\"", ""), wifiConfigurationList);
                                        if (connected[i])
                                            break;
                                    }
                                    break;
                                default:
                                    connected[i] = WifiScanWorker.compareSSID(wifiManager, wifiInfo, _ssid, wifiConfigurationList);
                                    break;
                            }
                            i++;
                        }

                        if (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED) {
                            wifiPassed = true;
                            for (boolean conn : connected) {
                                if (conn) {
                                    wifiPassed = false;
                                    break;
                                }
                            }
                            // not use scanner data
                            done = true;
                        }
                        else
                        if (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_CONNECTED) {
                            wifiPassed = false;
                            for (boolean conn : connected) {
                                if (conn) {
                                    wifiPassed = true;
                                    break;
                                }
                            }
                            // not use scanner data
                            done = true;
                        }
                    } else {
                        //if (event._name.equals("Doma"))
                        //    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "wifi not connected");

                        if ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_CONNECTED) ||
                                (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED)) {
                            // not use scanner data
                            done = true;
                            wifiPassed = (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED);
                        }
                    }
                } else {
                    //if (event._name.equals("Doma"))
                    //    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "wifiStateEnabled=false");
                    if ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_CONNECTED) ||
                            (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED)) {
                        // not use scanner data
                        done = true;
                        wifiPassed = (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED);
                    }
                }

                //if (event._name.equals("Doma"))
                //    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "wifiPassed - connected =" + wifiPassed);

                if ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NEARBY) ||
                        (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY)) {
                    if (!done) {
                        if (!ApplicationPreferences.applicationEventWifiEnableScanning) {
                            //if (forRestartEvents)
                            //    wifiPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesWifi.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                            //else
                                // not allowed for disabled scanning
                            //    notAllowedWifi = true;
                            wifiPassed = false;
                        } else {
                            //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                            if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn) {
                                if (forRestartEvents)
                                    wifiPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesWifi.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                                else
                                    // not allowed for screen Off
                                    notAllowedWifi = true;
                            } else {

                                wifiPassed = false;

                                List<WifiSSIDData> scanResults = WifiScanWorker.getScanResults(context);

                                //PPApplication.logE("----- DataWrapper.doHandleEvents","scanResults="+scanResults);

                                if (scanResults != null) {
                                    /*if (PPApplication.logEnabled()) {
                                        if (event._name.equals("Doma")) {
                                            PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "scanResults != null");
                                            PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "scanResults.size=" + scanResults.size());
                                            //PPApplication.logE("----- DataWrapper.doHandleEvents","-- eventSSID="+event._eventPreferencesWifi._SSID);
                                        }
                                    }*/

                                    for (WifiSSIDData result : scanResults) {
                                        /*if (PPApplication.logEnabled()) {
                                            if (event._name.equals("Doma")) {
                                                PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "scanSSID=" + result.ssid);
                                                PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "scanBSSID=" + result.bssid);
                                            }
                                        }*/
                                        String[] splits = event._eventPreferencesWifi._SSID.split("\\|");
                                        boolean[] nearby = new boolean[splits.length];
                                        int i = 0;
                                        for (String _ssid : splits) {
                                            nearby[i] = false;
                                            switch (_ssid) {
                                                case EventPreferencesWifi.ALL_SSIDS_VALUE:
                                                    //if (event._name.equals("Doma"))
                                                    //    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "all ssids");
                                                    nearby[i] = true;
                                                    break;
                                                case EventPreferencesWifi.CONFIGURED_SSIDS_VALUE:
                                                    //if (event._name.equals("Doma"))
                                                    //    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "configured ssids");
                                                    for (WifiSSIDData data : wifiConfigurationList) {
                                                        if (WifiScanWorker.compareSSID(result, data.ssid.replace("\"", ""), wifiConfigurationList)) {
                                                            /*if (PPApplication.logEnabled()) {
                                                                if (event._name.equals("Doma")) {
                                                                    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "configured SSID=" + data.ssid.replace("\"", ""));
                                                                    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "wifi found");
                                                                }
                                                            }*/
                                                            nearby[i] = true;
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                default:
                                                    if (WifiScanWorker.compareSSID(result, _ssid, wifiConfigurationList)) {
                                                        /*if (PPApplication.logEnabled()) {
                                                            if (event._name.equals("Doma")) {
                                                                PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "event SSID=" + event._eventPreferencesWifi._SSID);
                                                                PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "wifi found");
                                                            }
                                                        }*/
                                                        nearby[i] = true;
                                                    }
                                                    break;
                                            }
                                            i++;
                                        }

                                        done = false;
                                        if (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY) {
                                            wifiPassed = true;
                                            for (boolean inF : nearby) {
                                                if (inF) {
                                                    done = true;
                                                    wifiPassed = false;
                                                    break;
                                                }
                                            }
                                        }
                                        else {
                                            wifiPassed = false;
                                            for (boolean inF : nearby) {
                                                if (inF) {
                                                    done = true;
                                                    wifiPassed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (done)
                                            break;
                                    }
                                    //if (event._name.equals("Doma"))
                                    //    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "wifiPassed - in front =" + wifiPassed);

                                    if (!done) {
                                        if (scanResults.size() == 0) {
                                            //if (event._name.equals("Doma"))
                                            //    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "scanResult is empty");

                                            if (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY)
                                                wifiPassed = true;

                                            //if (event._name.equals("Doma"))
                                            //    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "wifiPassed - in front - for empty scanResult =" + wifiPassed);
                                        }
                                    }

                                } //else
                                    //if (event._name.equals("Doma"))
                                    //    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "scanResults == null");
                            }
                        }
                    }
                }

                /*if (event._name.equals("Doma")) {
                    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "------- wifiPassed=" + wifiPassed);
                    PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "------- notAllowedWifi=" + notAllowedWifi);
                }*/

                if (!notAllowedWifi) {
                    if (wifiPassed)
                        event._eventPreferencesWifi.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesWifi.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedWifi = true;
            event._eventPreferencesWifi.setSensorPassed(event._eventPreferencesWifi.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_WIFI);
        }

        if (event._eventPreferencesScreen._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesScreen.PREF_EVENT_SCREEN_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                //PPApplication.logE("[Screen] DataWrapper.doHandleEvents", "xxx");

                //boolean isScreenOn;
                //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                boolean keyguardShowing = false;

                if (event._eventPreferencesScreen._whenUnlocked) {
                    KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                    keyguardShowing = kgMgr.isKeyguardLocked();
                }
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("[Screen] DataWrapper.doHandleEvents", "PPApplication.isScreenOn=" + PPApplication.isScreenOn);
                    PPApplication.logE("[Screen] DataWrapper.doHandleEvents", "keyguardShowing=" + keyguardShowing);
                }*/

                if (event._eventPreferencesScreen._eventType == EventPreferencesScreen.ETYPE_SCREENON) {
                    // event type = screen is on
                    if (event._eventPreferencesScreen._whenUnlocked)
                        // passed if screen is on and unlocked
                        if (PPApplication.isScreenOn) {
                            screenPassed = !keyguardShowing;
                        }
                        else
                        if (!PPApplication.isScreenOn) {
                            screenPassed = !keyguardShowing;
                        }
                        //screenPassed = PPApplication.isScreenOn && (!keyguardShowing);
                    else
                        screenPassed = PPApplication.isScreenOn;
                } else {
                    // event type = screen is off
                    if (event._eventPreferencesScreen._whenUnlocked) {
                        // passed if screen is off and locked
                        if (!PPApplication.isScreenOn) {
                            screenPassed = keyguardShowing;
                        }
                        else
                        if (PPApplication.isScreenOn) {
                            screenPassed = keyguardShowing;
                        }
                        //screenPassed = (!PPApplication.isScreenOn) && keyguardShowing;
                    }
                    else
                        screenPassed = !PPApplication.isScreenOn;
                }

                //PPApplication.logE("[Screen] DataWrapper.doHandleEvents", "screenPassed="+screenPassed);

                if (!notAllowedScreen) {
                    if (screenPassed)
                        event._eventPreferencesScreen.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesScreen.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedScreen = true;
            event._eventPreferencesScreen.setSensorPassed(event._eventPreferencesScreen.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_SCREEN);
        }


        if (event._eventPreferencesBluetooth._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                    && Permissions.checkEventLocation(context, event, null)
                    && Permissions.checkEventBluetoothForEMUI(context, event, null)) {
                bluetoothPassed = false;

                List<BluetoothDeviceData> boundedDevicesList = BluetoothScanWorker.getBoundedDevicesList(context);

                boolean done = false;

                BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
                if (bluetooth != null) {
                    boolean isBluetoothEnabled = bluetooth.isEnabled();

                    if (isBluetoothEnabled) {
                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetoothEnabled=true");
                            PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "-- eventAdapterName=" + event._eventPreferencesBluetooth._adapterName);
                        }*/

                        //List<BluetoothDeviceData> connectedDevices = BluetoothConnectedDevices.getConnectedDevices(context);
                        BluetoothConnectionBroadcastReceiver.getConnectedDevices(context);

                        if (BluetoothConnectionBroadcastReceiver.isBluetoothConnected(null, "")) {
                            //if (BluetoothConnectedDevices.isBluetoothConnected(connectedDevices,null, "")) {

                            //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "any device connected");

                            String[] splits = event._eventPreferencesBluetooth._adapterName.split("\\|");
                            boolean[] connected = new boolean[splits.length];

                            int i = 0;
                            for (String _bluetoothName : splits) {
                                connected[i] = false;
                                switch (_bluetoothName) {
                                    case EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE:
                                        //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "any device connected");
                                        connected[i] = true;
                                        break;
                                    case EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE:
                                        for (BluetoothDeviceData data : boundedDevicesList) {
                                            /*if (PPApplication.logEnabled()) {
                                                PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "boundedDevice.name=" + data.getName());
                                                PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "boundedDevice.address=" + data.getAddress());
                                            }*/
                                            connected[i] = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(data, "");
                                            if (connected[i])
                                                break;
                                        }
                                        //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "paired device connected=" + connected[i]);
                                        break;
                                    default:
                                        connected[i] = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(null, _bluetoothName);
                                        //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "event sensor device connected=" + connected[i]);
                                        break;
                                }
                                i++;
                            }

                            if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED) {
                                bluetoothPassed = true;
                                for (boolean conn : connected) {
                                    if (conn) {
                                        bluetoothPassed = false;
                                        break;
                                    }
                                }
                                // not use scanner data
                                done = true;
                            }
                            else
                            if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) {
                                bluetoothPassed = false;
                                for (boolean conn : connected) {
                                    if (conn) {
                                        // when is connected to configured bt name, is also nearby
                                        bluetoothPassed = true;
                                        break;
                                    }
                                }
                                // not use scanner data
                                done = true;
                            }
                        } else {
                            //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "not any device connected");

                            if ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) ||
                                    (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED)) {
                                // not use scanner data
                                done = true;
                                bluetoothPassed = (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED);
                            }
                        }
                    } else {
                        //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetoothEnabled=true");

                        if ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) ||
                                (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED)) {
                            // not use scanner data
                            done = true;
                            bluetoothPassed = (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED);
                        }
                    }
                }

                //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetoothPassed=" + bluetoothPassed);

                if ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NEARBY) ||
                        (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY)) {
                    if (!done) {
                        if (!ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                            //if (forRestartEvents)
                            //    bluetoothPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesBluetooth.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                            //else
                                // not allowed for disabled scanning
                            //    notAllowedBluetooth = true;
                            bluetoothPassed = false;
                        } else {
                            //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                            if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn) {
                                if (forRestartEvents)
                                    bluetoothPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesBluetooth.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                                else
                                    // not allowed for screen Off
                                    notAllowedBluetooth = true;
                            } else {
                                bluetoothPassed = false;

                                List<BluetoothDeviceData> scanResults = BluetoothScanWorker.getScanResults(context);

                                if (scanResults != null) {
                                    //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "scanResults.size="+scanResults.size());

                                    for (BluetoothDeviceData device : scanResults) {
                                        /*if (PPApplication.logEnabled()) {
                                            PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "device.getName=" + device.getName());
                                            PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "device.getAddress=" + device.getAddress());
                                        }*/
                                        String[] splits = event._eventPreferencesBluetooth._adapterName.split("\\|");
                                        boolean[] nearby = new boolean[splits.length];
                                        int i = 0;
                                        for (String _bluetoothName : splits) {
                                            nearby[i] = false;
                                            switch (_bluetoothName) {
                                                case EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE:
                                                    nearby[i] = true;
                                                    break;
                                                case EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE:
                                                    for (BluetoothDeviceData data : boundedDevicesList) {
                                                        String _device = device.getName().toUpperCase();
                                                        String _adapterName = data.getName().toUpperCase();
                                                        if (Wildcard.match(_device, _adapterName, '_', '%', true)) {
                                                            //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetooth found");
                                                            //PPApplication.logE("@@@ DataWrapper.doHandleEvents","bluetoothAdapterName="+device.getName());
                                                            //PPApplication.logE("@@@ DataWrapper.doHandleEvents","bluetoothAddress="+device.getAddress());
                                                            nearby[i] = true;
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                default:
                                                    String _device = device.getName().toUpperCase();
                                                    if ((device.getName() == null) || device.getName().isEmpty()) {
                                                        // scanned device has not name (hidden BT?)
                                                        if ((device.getAddress() != null) && (!device.getAddress().isEmpty())) {
                                                            // device has address
                                                            for (BluetoothDeviceData data : boundedDevicesList) {
                                                                if ((data.getAddress() != null) && data.getAddress().equals(device.getAddress())) {
                                                                    //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetooth found");
                                                                    //PPApplication.logE("@@@ DataWrapper.doHandleEvents","bluetoothAdapterName="+device.getName());
                                                                    //PPApplication.logE("@@@ DataWrapper.doHandleEvents","bluetoothAddress="+device.getAddress());
                                                                    nearby[i] = true;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        String _adapterName = _bluetoothName.toUpperCase();
                                                        if (Wildcard.match(_device, _adapterName, '_', '%', true)) {
                                                            //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetooth found");
                                                            //PPApplication.logE("@@@ DataWrapper.doHandleEvents","bluetoothAdapterName="+device.getName());
                                                            //PPApplication.logE("@@@ DataWrapper.doHandleEvents","bluetoothAddress="+device.getAddress());
                                                            nearby[i] = true;
                                                        }
                                                    }
                                                    break;
                                            }
                                            i++;
                                        }

                                        done = false;
                                        if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY) {
                                            bluetoothPassed = true;
                                            for (boolean inF : nearby) {
                                                if (inF) {
                                                    done = true;
                                                    bluetoothPassed = false;
                                                    break;
                                                }
                                            }
                                        }
                                        else {
                                            bluetoothPassed = false;
                                            for (boolean inF : nearby) {
                                                if (inF) {
                                                    done = true;
                                                    bluetoothPassed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (done)
                                            break;
                                    }
                                    //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetoothPassed=" + bluetoothPassed);

                                    if (!done) {
                                        if (scanResults.size() == 0) {
                                            //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "scanResult is empty");

                                            if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY)
                                                wifiPassed = true;
                                        }
                                    }

                                } //else
                                    //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "scanResults == null");
                            }
                        }
                    }
                }

                //PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetoothPassed=" + bluetoothPassed);

                if (!notAllowedBluetooth) {
                    if (bluetoothPassed)
                        event._eventPreferencesBluetooth.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesBluetooth.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedBluetooth = true;
            event._eventPreferencesBluetooth.setSensorPassed(event._eventPreferencesBluetooth.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_BLUETOOTH);
        }

        if (event._eventPreferencesSMS._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                    && Permissions.checkEventSMSContacts(context, event, null)
                /* moved to Extender && Permissions.checkEventSMSBroadcast(context, event, null)*/) {
                // compute start time

                if (event._eventPreferencesSMS._startTime > 0) {
                    //PPApplication.logE("[SMS sensor] DataWrapper.doHandleEvents", "startTime > 0");

                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    long startTime = event._eventPreferencesSMS._startTime - gmtOffset;

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(startTime);
                        PPApplication.logE("[SMS sensor] DataWrapper.doHandleEvents", "startTime=" + alarmTimeS);
                    }*/

                    // compute end datetime
                    long endAlarmTime = event._eventPreferencesSMS.computeAlarm();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(endAlarmTime);
                        PPApplication.logE("[SMS sensor] DataWrapper.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                    }*/

                    Calendar now = Calendar.getInstance();
                    long nowAlarmTime = now.getTimeInMillis();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(nowAlarmTime);
                        PPApplication.logE("[SMS sensor] DataWrapper.doHandleEvents", "nowAlarmTime=" + alarmTimeS);
                    }*/

                    if (sensorType.equals(EventsHandler.SENSOR_TYPE_SMS))
                        smsPassed = true;
                    else if (!event._eventPreferencesSMS._permanentRun) {
                        //PPApplication.logE("[SMS sensor] DataWrapper.doHandleEvents", "sensorType=" + sensorType);
                        if (sensorType.equals(EventsHandler.SENSOR_TYPE_SMS_EVENT_END))
                            smsPassed = false;
                        else
                            smsPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                        //PPApplication.logE("[SMS sensor] DataWrapper.doHandleEvents", "smsPassed=" + smsPassed);
                    } else {
                        smsPassed = nowAlarmTime >= startTime;
                    }
                } else {
                    //PPApplication.logE("[SMS sensor] DataWrapper.doHandleEvents", "startTime == 0");
                    smsPassed = false;
                }

                if (!smsPassed) {
                    event._eventPreferencesSMS._startTime = 0;
                    //if ((event != null) && (event._name != null) && (event._name.equals("SMS event")))
                    //    PPApplication.logE("[SMS sensor] DataWrapper.doHandleEvents", "startTime="+event._eventPreferencesSMS._startTime);
                    DatabaseHandler.getInstance(context).updateSMSStartTime(event);
                }

                if (!notAllowedSms) {
                    if (smsPassed)
                        event._eventPreferencesSMS.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesSMS.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedSms = true;
            event._eventPreferencesSMS.setSensorPassed(event._eventPreferencesSMS.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_SMS);
        }

        if (event._eventPreferencesNotification._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                notificationPassed = event._eventPreferencesNotification.isNotificationVisible(context);

                //PPApplication.logE("[NOTIF] DataWrapper.doHandleEvents", "notificationPassed=" + notificationPassed);

                if (!notAllowedNotification) {
                    if (notificationPassed)
                        event._eventPreferencesNotification.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesNotification.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedNotification = true;
            event._eventPreferencesNotification.setSensorPassed(event._eventPreferencesNotification.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_NOTIFICATION);
        }


        if (event._eventPreferencesApplication._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                applicationPassed = false;

                if (PPPExtenderBroadcastReceiver.isEnabled(context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_3_0)) {
                    String foregroundApplication = ApplicationPreferences.prefApplicationInForeground;

                    if (!foregroundApplication.isEmpty()) {
                        String[] splits = event._eventPreferencesApplication._applications.split("\\|");
                        for (String split : splits) {
                            String packageName = Application.getPackageName(split);

                            if (foregroundApplication.equals(packageName)) {
                                applicationPassed = true;
                                break;
                            }
                        }
                    } else
                        notAllowedApplication = true;
                } else
                    notAllowedApplication = true;

                if (!notAllowedApplication) {
                    if (applicationPassed)
                        event._eventPreferencesApplication.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesApplication.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedApplication = true;
            event._eventPreferencesApplication.setSensorPassed(event._eventPreferencesApplication.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_APPLICATION);
        }

        if (event._eventPreferencesLocation._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                    && Permissions.checkEventLocation(context, event, null)) {
                if (!ApplicationPreferences.applicationEventLocationEnableScanning) {
                    //if (forRestartEvents)
                    //    locationPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesLocation.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    //else {
                        // not allowed for disabled location scanner
                    //    PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "ignore for disabled scanner");
                    //    notAllowedLocation = true;
                    //}
                    locationPassed = false;
                } else {
                    //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn) {
                        if (forRestartEvents)
                            locationPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesLocation.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                        else {
                            // not allowed for screen Off
                            //PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "ignore for screen off");
                            notAllowedLocation = true;
                        }
                    } else {
                        synchronized (PPApplication.geofenceScannerMutex) {
                            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted() &&
                                    PhoneProfilesService.getInstance().getGeofencesScanner().mTransitionsUpdated) {

                                /*if (PPApplication.logEnabled()) {
                                    PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "--------");
                                    PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "_eventPreferencesLocation._geofences=" + event._eventPreferencesLocation._geofences);
                                }*/

                                String[] splits = event._eventPreferencesLocation._geofences.split("\\|");
                                boolean[] passed = new boolean[splits.length];

                                int i = 0;
                                for (String _geofence : splits) {
                                    passed[i] = false;
                                    if (!_geofence.isEmpty()) {
                                        //PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "geofence=" + DatabaseHandler.getInstance(context).getGeofenceName(Long.valueOf(_geofence)));

                                        int geofenceTransition = DatabaseHandler.getInstance(context).getGeofenceTransition(Long.valueOf(_geofence));
                                        /*if (geofenceTransition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER)
                                            PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "transitionType=GEOFENCE_TRANSITION_ENTER");
                                        else
                                            PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "transitionType=GEOFENCE_TRANSITION_EXIT");*/

                                        if (geofenceTransition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER) {
                                            passed[i] = true;
                                        }
                                    }
                                    ++i;
                                }

                                if (event._eventPreferencesLocation._whenOutside) {
                                    // all locations must not be passed
                                    locationPassed = true;
                                    for (boolean pass : passed) {
                                        if (pass) {
                                            locationPassed = false;
                                            break;
                                        }
                                    }
                                }
                                else {
                                    // one location must be passed
                                    locationPassed = false;
                                    for (boolean pass : passed) {
                                        if (pass) {
                                            locationPassed = true;
                                            break;
                                        }
                                    }
                                }
                                //PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "locationPassed=" + locationPassed);

                            } else {
                                notAllowedLocation = true;
                            }
                        }
                    }
                }

                if (!notAllowedLocation) {
                    if (locationPassed)
                        event._eventPreferencesLocation.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesLocation.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedLocation = true;
            event._eventPreferencesLocation.setSensorPassed(event._eventPreferencesLocation.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_LOCATION);
        }

        if (event._eventPreferencesOrientation._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                boolean inCall = false;
                TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephony != null) {
                    int callState = telephony.getCallState();
                    inCall = (callState == TelephonyManager.CALL_STATE_RINGING) || (callState == TelephonyManager.CALL_STATE_OFFHOOK);
                }
                if (inCall) {
                    // not allowed changes during call
                    notAllowedOrientation = true;
                } else if (!ApplicationPreferences.applicationEventOrientationEnableScanning) {
                    //if (forRestartEvents)
                    //    orientationPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesOrientation.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    //else
                        // not allowed for disabled orientation scanner
                    //    notAllowedOrientation = true;
                    orientationPassed = false;
                } else if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn) {
                    if (forRestartEvents)
                        orientationPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesOrientation.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    else
                        // not allowed for screen Off
                        notAllowedOrientation = true;
                } else {
                    synchronized (PPApplication.orientationScannerMutex) {
                        if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isOrientationScannerStarted()) {
                            PPApplication.startHandlerThreadOrientationScanner();
                            boolean lApplicationPassed = false;
                            if (!event._eventPreferencesOrientation._ignoredApplications.isEmpty()) {
                                if (PPPExtenderBroadcastReceiver.isEnabled(context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_3_0)) {
                                    String foregroundApplication = ApplicationPreferences.prefApplicationInForeground;
                                    if (!foregroundApplication.isEmpty()) {
                                        String[] splits = event._eventPreferencesOrientation._ignoredApplications.split("\\|");
                                        for (String split : splits) {
                                            String packageName = Application.getPackageName(split);

                                            if (foregroundApplication.equals(packageName)) {
                                                lApplicationPassed = true;
                                                break;
                                            }
                                        }
                                    }
                                } else
                                    notAllowedOrientation = true;
                            }
                            if (!lApplicationPassed) {
                                boolean lDisplayPassed = false;
                                boolean lSidePassed = false;

                                boolean hasAccelerometer = PPApplication.accelerometerSensor != null;
                                boolean hasMagneticField = PPApplication.magneticFieldSensor != null;
                                boolean hasProximity = PPApplication.proximitySensor != null;
                                boolean hasLight = PPApplication.lightSensor != null;

                                boolean enabledAll = (hasAccelerometer) && (hasMagneticField);

                                boolean configuredDisplay = false;
                                if (hasAccelerometer) {
                                    if (!event._eventPreferencesOrientation._display.isEmpty()) {
                                        String[] splits = event._eventPreferencesOrientation._display.split("\\|");
                                        if (splits.length > 0) {
                                            configuredDisplay = true;
                                            //lDisplayPassed = false;
                                            for (String split : splits) {
                                                try {
                                                    int side = Integer.valueOf(split);
                                                    if (side == PPApplication.handlerThreadOrientationScanner.mDisplayUp) {
                                                        lDisplayPassed = true;
                                                        break;
                                                    }
                                                } catch (Exception ignored) {
                                                }
                                            }
                                        }
                                    }
                                }

                                boolean configuredSide = false;
                                if (enabledAll) {
                                    if (!event._eventPreferencesOrientation._sides.isEmpty()) {
                                        String[] splits = event._eventPreferencesOrientation._sides.split("\\|");
                                        if (splits.length > 0) {
                                            configuredSide = true;
                                            //lSidePassed = false;
                                            for (String split : splits) {
                                                try {
                                                    int side = Integer.valueOf(split);
                                                    if (side == OrientationScannerHandlerThread.DEVICE_ORIENTATION_HORIZONTAL) {
                                                        if (PPApplication.handlerThreadOrientationScanner.mSideUp == PPApplication.handlerThreadOrientationScanner.mDisplayUp) {
                                                            lSidePassed = true;
                                                            break;
                                                        }
                                                    } else {
                                                        if (side == PPApplication.handlerThreadOrientationScanner.mSideUp) {
                                                            lSidePassed = true;
                                                            break;
                                                        }
                                                    }
                                                } catch (Exception ignored) {
                                                }
                                            }
                                        }
                                    }
                                }

                                boolean lDistancePassed = false;
                                boolean configuredDistance = false;
                                if (hasProximity) {
                                    if (event._eventPreferencesOrientation._distance != 0) {
                                        configuredDistance = true;
                                        lDistancePassed = event._eventPreferencesOrientation._distance == PPApplication.handlerThreadOrientationScanner.mDeviceDistance;
                                    }
                                }

                                boolean lLightPassed = false;
                                boolean configuredLight = false;
                                if (hasLight) {
                                    if (event._eventPreferencesOrientation._checkLight) {
                                        configuredLight = true;
                                        int light = PPApplication.handlerThreadOrientationScanner.mLight;
                                        int min = Integer.parseInt(event._eventPreferencesOrientation._lightMin);
                                        int max = Integer.parseInt(event._eventPreferencesOrientation._lightMax);
                                        lLightPassed = (light >= min) && (light <= max);
                                        /*if (PPApplication.logEnabled()) {
                                            PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "light=" + light);
                                            PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "min=" + min);
                                            PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "max=" + max);
                                        }*/
                                    }
                                }

                                /*if (PPApplication.logEnabled()) {
                                    PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "configuredDisplay=" + configuredDisplay);
                                    PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "configuredSide=" + configuredSide);
                                    PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "configuredDistance=" + configuredDistance);
                                    PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "configuredLight=" + configuredLight);

                                    PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "hasAccelerometer=" + hasAccelerometer);
                                    PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "hasMagneticField=" + hasMagneticField);
                                    PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "hasProximity=" + hasProximity);
                                    PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "hasLight=" + hasLight);

                                    PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "lDisplayPassed=" + lDisplayPassed);
                                    PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "lSidePassed=" + lSidePassed);
                                    PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "lDistancePassed=" + lDistancePassed);
                                    PPApplication.logE("[OriSensor] DataWrapper.doHandleEvents", "lLightPassed=" + lLightPassed);
                                }*/

                                if (configuredDisplay || configuredSide || configuredDistance || configuredLight) {
                                    orientationPassed = true;
                                    if (configuredDisplay)
                                        orientationPassed = orientationPassed && lDisplayPassed;
                                    if (configuredSide)
                                        orientationPassed = orientationPassed && lSidePassed;
                                    if (configuredDistance)
                                        orientationPassed = orientationPassed && lDistancePassed;
                                    if (configuredLight)
                                        orientationPassed = orientationPassed && lLightPassed;
                                }
                                else
                                    notAllowedOrientation = true;
                                //orientationPassed = lDisplayPassed || lSidePassed || lDistancePassed || lLightPassed;
                            }
                        } else {
                            notAllowedOrientation = true;
                        }
                    }
                }

                if (!notAllowedOrientation) {
                    if (orientationPassed)
                        event._eventPreferencesOrientation.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesOrientation.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedOrientation = true;
            event._eventPreferencesOrientation.setSensorPassed(event._eventPreferencesOrientation.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_ORIENTATION);
        }

        if (event._eventPreferencesMobileCells._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                    && Permissions.checkEventLocation(context, event, null)) {
                if (!ApplicationPreferences.applicationEventMobileCellEnableScanning) {
                    //if (forRestartEvents)
                    //    mobileCellPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesMobileCells.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    //else
                        // not allowed for disabled mobile cells scanner
                    //    notAllowedMobileCell = true;
                    mobileCellPassed = false;
                } else {
                    //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn) {
                        if (forRestartEvents)
                            mobileCellPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesMobileCells.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                        else
                            // not allowed for screen Off
                            notAllowedMobileCell = true;
                    } else {
                        synchronized (PPApplication.phoneStateScannerMutex) {
                            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isPhoneStateScannerStarted()) {
                                if (PhoneStateScanner.isValidCellId(PhoneStateScanner.registeredCell)) {
                                    String registeredCell = Integer.toString(PhoneStateScanner.registeredCell);

                                    String[] splits = event._eventPreferencesMobileCells._cells.split("\\|");
                                    boolean[] registered = new boolean[splits.length];
                                    int i = 0;
                                    for (String cell : splits) {
                                        registered[i] = cell.equals(registeredCell);
                                        i++;
                                    }

                                    if (event._eventPreferencesMobileCells._whenOutside) {
                                        // all mobile cells must not be registered
                                        mobileCellPassed = true;
                                        for (boolean reg : registered) {
                                            if (reg) {
                                                mobileCellPassed = false;
                                                break;
                                            }
                                        }
                                    }
                                    else {
                                        // one mobile cell must be registered
                                        mobileCellPassed = false;
                                        for (boolean reg : registered) {
                                            if (reg) {
                                                mobileCellPassed = true;
                                                break;
                                            }
                                        }
                                    }
                                } else
                                    notAllowedMobileCell = true;

                            } else
                                notAllowedMobileCell = true;
                        }
                    }
                }

                if (!notAllowedMobileCell) {
                    if (mobileCellPassed)
                        event._eventPreferencesMobileCells.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesMobileCells.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedMobileCell = true;
            event._eventPreferencesMobileCells.setSensorPassed(event._eventPreferencesMobileCells.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_MOBILE_CELLS);
        }

        if (event._eventPreferencesNFC._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                // compute start time

                if (event._eventPreferencesNFC._startTime > 0) {
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    long startTime = event._eventPreferencesNFC._startTime - gmtOffset;

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(startTime);
                        PPApplication.logE("DataWrapper.doHandleEvents", "startTime=" + alarmTimeS);
                    }*/

                    // compute end datetime
                    long endAlarmTime = event._eventPreferencesNFC.computeAlarm();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(endAlarmTime);
                        PPApplication.logE("DataWrapper.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                    }*/

                    Calendar now = Calendar.getInstance();
                    long nowAlarmTime = now.getTimeInMillis();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(nowAlarmTime);
                        PPApplication.logE("DataWrapper.doHandleEvents", "nowAlarmTime=" + alarmTimeS);
                    }*/

                    if (sensorType.equals(EventsHandler.SENSOR_TYPE_NFC_TAG))
                        nfcPassed = true;
                    else if (!event._eventPreferencesNFC._permanentRun) {
                        if (sensorType.equals(EventsHandler.SENSOR_TYPE_NFC_EVENT_END))
                            nfcPassed = false;
                        else
                            nfcPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                    } else
                        nfcPassed = nowAlarmTime >= startTime;
                } else
                    nfcPassed = false;

                if (!nfcPassed) {
                    event._eventPreferencesNFC._startTime = 0;
                    DatabaseHandler.getInstance(context).updateNFCStartTime(event);
                }

                if (!notAllowedNfc) {
                    if (nfcPassed)
                        event._eventPreferencesNFC.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesNFC.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }

            } else
                notAllowedNfc = true;
            event._eventPreferencesNFC.setSensorPassed(event._eventPreferencesNFC.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_NFC);
        }

        if (event._eventPreferencesRadioSwitch._enabled) {
            if ((Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                radioSwitchPassed = true;
                boolean tested = false;

                if ((event._eventPreferencesRadioSwitch._wifi == 1 || event._eventPreferencesRadioSwitch._wifi == 2)
                        && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_WIFI)) {

                    if (!(ApplicationPreferences.prefEventWifiScanRequest ||
                            ApplicationPreferences.prefEventWifiWaitForResult ||
                            ApplicationPreferences.prefEventWifiEnabledForScan)) {
                        // ignore for wifi scanning

                        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        int wifiState = wifiManager.getWifiState();
                        boolean enabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                        //PPApplication.logE("-###- DataWrapper.doHandleEvents", "wifiState=" + enabled);
                        tested = true;
                        if (event._eventPreferencesRadioSwitch._wifi == 1)
                            radioSwitchPassed = radioSwitchPassed && enabled;
                        else
                            radioSwitchPassed = radioSwitchPassed && !enabled;
                    } else
                        notAllowedRadioSwitch = true;
                }

                if ((event._eventPreferencesRadioSwitch._bluetooth == 1 || event._eventPreferencesRadioSwitch._bluetooth == 2)
                        && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_BLUETOOTH)) {

                    if (!(ApplicationPreferences.prefEventBluetoothScanRequest ||
                            ApplicationPreferences.prefEventBluetoothLEScanRequest ||
                            ApplicationPreferences.prefEventBluetoothWaitForResult ||
                            ApplicationPreferences.prefEventBluetoothLEWaitForResult ||
                            ApplicationPreferences.prefEventBluetoothEnabledForScan)) {
                        // ignore for bluetooth scanning


                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
                        if (bluetoothAdapter != null) {
                            boolean enabled = bluetoothAdapter.isEnabled();
                            //PPApplication.logE("-###- DataWrapper.doHandleEvents", "bluetoothState=" + enabled);
                            tested = true;
                            if (event._eventPreferencesRadioSwitch._bluetooth == 1)
                                radioSwitchPassed = radioSwitchPassed && enabled;
                            else
                                radioSwitchPassed = radioSwitchPassed && !enabled;
                        }
                    } else
                        notAllowedRadioSwitch = true;
                }

                if ((event._eventPreferencesRadioSwitch._mobileData == 1 || event._eventPreferencesRadioSwitch._mobileData == 2)
                        && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY)) {

                    boolean enabled = ActivateProfileHelper.isMobileData(context);
                    //PPApplication.logE("-###- DataWrapper.doHandleEvents", "mobileDataState=" + enabled);
                    tested = true;
                    if (event._eventPreferencesRadioSwitch._mobileData == 1)
                        radioSwitchPassed = radioSwitchPassed && enabled;
                    else
                        radioSwitchPassed = radioSwitchPassed && !enabled;
                }

                if ((event._eventPreferencesRadioSwitch._gps == 1 || event._eventPreferencesRadioSwitch._gps == 2)
                        && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_LOCATION_GPS)) {

                    boolean enabled;
                    /*if (android.os.Build.VERSION.SDK_INT < 19)
                        enabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
                    else {*/
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    //}
                    //PPApplication.logE("-###- DataWrapper.doHandleEvents", "gpsState=" + enabled);
                    tested = true;
                    if (event._eventPreferencesRadioSwitch._gps == 1)
                        radioSwitchPassed = radioSwitchPassed && enabled;
                    else
                        radioSwitchPassed = radioSwitchPassed && !enabled;
                }

                if ((event._eventPreferencesRadioSwitch._nfc == 1 || event._eventPreferencesRadioSwitch._nfc == 2)
                        && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_NFC)) {

                    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
                    if (nfcAdapter != null) {
                        boolean enabled = nfcAdapter.isEnabled();
                        //PPApplication.logE("-###- DataWrapper.doHandleEvents", "nfcState=" + enabled);
                        tested = true;
                        if (event._eventPreferencesRadioSwitch._nfc == 1)
                            radioSwitchPassed = radioSwitchPassed && enabled;
                        else
                            radioSwitchPassed = radioSwitchPassed && !enabled;
                    }
                }

                if (event._eventPreferencesRadioSwitch._airplaneMode == 1 || event._eventPreferencesRadioSwitch._airplaneMode == 2) {

                    boolean enabled = ActivateProfileHelper.isAirplaneMode(context);
                    //PPApplication.logE("-###- DataWrapper.doHandleEvents", "airplaneModeState=" + enabled);
                    tested = true;
                    if (event._eventPreferencesRadioSwitch._airplaneMode == 1)
                        radioSwitchPassed = radioSwitchPassed && enabled;
                    else
                        radioSwitchPassed = radioSwitchPassed && !enabled;
                }

                radioSwitchPassed = radioSwitchPassed && tested;

                if (!notAllowedRadioSwitch) {
                    if (radioSwitchPassed)
                        event._eventPreferencesRadioSwitch.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesRadioSwitch.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedRadioSwitch = true;
            event._eventPreferencesRadioSwitch.setSensorPassed(event._eventPreferencesRadioSwitch.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_RADIO_SWITCH);
        }

        if (event._eventPreferencesAlarmClock._enabled) {
            if (Event.isEventPreferenceAllowed(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                // compute start time

                if (event._eventPreferencesAlarmClock._startTime > 0) {
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    long startTime = event._eventPreferencesAlarmClock._startTime - gmtOffset;

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(startTime);
                        PPApplication.logE("DataWrapper.doHandleEvents", "startTime=" + alarmTimeS);
                    }*/

                    // compute end datetime
                    long endAlarmTime = event._eventPreferencesAlarmClock.computeAlarm();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(endAlarmTime);
                        PPApplication.logE("DataWrapper.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                    }*/

                    Calendar now = Calendar.getInstance();
                    long nowAlarmTime = now.getTimeInMillis();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(nowAlarmTime);
                        PPApplication.logE("DataWrapper.doHandleEvents", "nowAlarmTime=" + alarmTimeS);
                    }*/

                    if (sensorType.equals(EventsHandler.SENSOR_TYPE_ALARM_CLOCK))
                        alarmClockPassed = true;
                    else if (!event._eventPreferencesAlarmClock._permanentRun) {
                        if (sensorType.equals(EventsHandler.SENSOR_TYPE_ALARM_CLOCK_EVENT_END))
                            alarmClockPassed = false;
                        else
                            alarmClockPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                    } else {
                        alarmClockPassed = nowAlarmTime >= startTime;
                    }
                } else
                    alarmClockPassed = false;

                if (!alarmClockPassed) {
                    event._eventPreferencesAlarmClock._startTime = 0;
                    DatabaseHandler.getInstance(context).updateAlarmClockStartTime(event);
                }

                if (!notAllowedAlarmClock) {
                    if (alarmClockPassed)
                        event._eventPreferencesAlarmClock.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesAlarmClock.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedAlarmClock = true;
            event._eventPreferencesAlarmClock.setSensorPassed(event._eventPreferencesAlarmClock.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING));
            DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_ALARM_CLOCK);
        }

        List<EventTimeline> eventTimelineList = getEventTimelineList(true);

        boolean allPassed = true;
        boolean someNotAllowed = false;
        if (!notAllowedTime)
            allPassed &= timePassed;
        else
            someNotAllowed = true;
        if (!notAllowedBattery)
            allPassed &= batteryPassed;
        else
            someNotAllowed = true;
        if (!notAllowedCall)
            allPassed &= callPassed;
        else
            someNotAllowed = true;
        if (!notAllowedPeripheral)
            allPassed &= peripheralPassed;
        else
            someNotAllowed = true;
        if (!notAllowedCalendar)
            allPassed &= calendarPassed;
        else
            someNotAllowed = true;
        if (!notAllowedWifi)
            allPassed &= wifiPassed;
        else
            someNotAllowed = true;
        if (!notAllowedScreen)
            allPassed &= screenPassed;
        else
            someNotAllowed = true;
        if (!notAllowedBluetooth)
            allPassed &= bluetoothPassed;
        else
            someNotAllowed = true;
        if (!notAllowedSms)
            allPassed &= smsPassed;
        else
            someNotAllowed = true;
        if (!notAllowedNotification)
            allPassed &= notificationPassed;
        else
            someNotAllowed = true;
        if (!notAllowedApplication)
            allPassed &= applicationPassed;
        else
            someNotAllowed = true;
        if (!notAllowedLocation)
            allPassed &= locationPassed;
        else
            someNotAllowed = true;
        if (!notAllowedOrientation)
            allPassed &= orientationPassed;
        else
            someNotAllowed = true;
        if (!notAllowedMobileCell)
            allPassed &= mobileCellPassed;
        else
            someNotAllowed = true;
        if (!notAllowedNfc)
            allPassed &= nfcPassed;
        else
            someNotAllowed = true;
        if (!notAllowedRadioSwitch)
            allPassed &= radioSwitchPassed;
        else
            someNotAllowed = true;
        if (!notAllowedAlarmClock)
            allPassed &= alarmClockPassed;
        else
            someNotAllowed = true;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("DataWrapper.doHandleEvents", "timePassed=" + timePassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "batteryPassed=" + batteryPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "callPassed=" + callPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "peripheralPassed=" + peripheralPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "calendarPassed=" + calendarPassed);
            if (event._name.equals("Doma"))
                PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "wifiPassed=" + wifiPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "screenPassed=" + screenPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "bluetoothPassed=" + bluetoothPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "smsPassed=" + smsPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "notificationPassed=" + notificationPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "applicationPassed=" + applicationPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "locationPassed=" + locationPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "orientationPassed=" + orientationPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "mobileCellPassed=" + mobileCellPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "nfcPassed=" + nfcPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "radioSwitchPassed=" + radioSwitchPassed);
            PPApplication.logE("DataWrapper.doHandleEvents", "alarmClockPassed=" + alarmClockPassed);

            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedTime=" + notAllowedTime);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedBattery=" + notAllowedBattery);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedCall=" + notAllowedCall);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedPeripheral=" + notAllowedPeripheral);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedCalendar=" + notAllowedCalendar);
            if (event._name.equals("Doma"))
                PPApplication.logE("[WiFi] DataWrapper.doHandleEvents", "notAllowedWifi=" + notAllowedWifi);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedScreen=" + notAllowedScreen);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedBluetooth=" + notAllowedBluetooth);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedSms=" + notAllowedSms);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedNotification=" + notAllowedNotification);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedApplication=" + notAllowedApplication);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedLocation=" + notAllowedLocation);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedOrientation=" + notAllowedOrientation);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedMobileCell=" + notAllowedMobileCell);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedNfc=" + notAllowedNfc);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedRadioSwitch=" + notAllowedRadioSwitch);
            PPApplication.logE("DataWrapper.doHandleEvents", "notAllowedAlarmClock=" + notAllowedAlarmClock);

            if (event._name.equals("Doma")) {
                PPApplication.logE("[***] DataWrapper.doHandleEvents", "allPassed=" + allPassed);
                PPApplication.logE("[***] DataWrapper.doHandleEvents", "someNotAllowed=" + someNotAllowed);
            }

            if (event._name.equals("Doma")) {
                //PPApplication.logE("DataWrapper.doHandleEvents","eventStart="+eventStart);
                PPApplication.logE("[***] DataWrapper.doHandleEvents", "forRestartEvents=" + forRestartEvents);
                PPApplication.logE("[***] DataWrapper.doHandleEvents", "statePause=" + statePause);
            }
        }*/

        if (!someNotAllowed) {
            // some sensor is not allowed, do not change event status

            if (allPassed) {
                // all sensors are passed

                //if (eventStart)
                newEventStatus = Event.ESTATUS_RUNNING;
                //else
                //    newEventStatus = Event.ESTATUS_PAUSE;

            } else
                newEventStatus = Event.ESTATUS_PAUSE;

            /*if (PPApplication.logEnabled()) {
                if (event._name.equals("Doma")) {
                    PPApplication.logE("[***] DataWrapper.doHandleEvents", "event.getStatus()=" + event.getStatus());
                    PPApplication.logE("[***] DataWrapper.doHandleEvents", "newEventStatus=" + newEventStatus);
                }
            }*/

            //PPApplication.logE("@@@ DataWrapper.doHandleEvents","restartEvent="+restartEvent);

            if ((event.getStatus() != newEventStatus) || forRestartEvents || event._isInDelayStart || event._isInDelayEnd) {
                //if (event._name.equals("Doma"))
                    //PPApplication.logE("[***] DataWrapper.doHandleEvents", " do new event status");

                if ((newEventStatus == Event.ESTATUS_RUNNING) && (!statePause)) {
                    // do start of events, all sensors are passed

                    /*if (PPApplication.logEnabled()) {
                        if (event._name.equals("Doma")) {
                            PPApplication.logE("[***] DataWrapper.doHandleEvents", "start event");
                            PPApplication.logE("[***] DataWrapper.doHandleEvents", "event._name=" + event._name);
                        }
                    }*/

                    if (event._isInDelayEnd)
                        event.removeDelayEndAlarm(this);
                    else {
                        if (!forDelayStartAlarm) {
                            // called not for delay alarm
                            /*if (forRestartEvents) {
                                event._isInDelayStart = false;
                            } else*/ {
                                if (!event._isInDelayStart) {
                                    // if not delay alarm is set, set it
                                    // this also set event._isInDelayStart
                                    event.setDelayStartAlarm(this); // for start delay
                                }
                                if (event._isInDelayStart) {
                                    // if delay expires, start event
                                    // this also set event._isInDelayStart
                                    event.checkDelayStart(/*this*/);
                                }
                            }
                            //if (event._name.equals("Doma"))
                            //    PPApplication.logE("[***] DataWrapper.doHandleEvents", "event._isInDelayStart=" + event._isInDelayStart);
                            if (!event._isInDelayStart) {
                                // no delay alarm is set
                                // start event
                                long oldMergedProfile = mergedProfile._id;
                                event.startEvent(this, eventTimelineList, /*interactive,*/ forRestartEvents, mergedProfile);
                                startProfileMerged = oldMergedProfile != mergedProfile._id;
                                //if (event._name.equals("Doma"))
                                //    PPApplication.logE("[***] DataWrapper.doHandleEvents", "mergedProfile._id=" + mergedProfile._id);
                            }
                        }
                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("[DSTART] DataWrapper.doHandleEvents", "forDelayStartAlarm=" + forDelayStartAlarm);
                            PPApplication.logE("[DSTART] DataWrapper.doHandleEvents", "event._isInDelayStart=" + event._isInDelayStart);
                        }*/
                        if (forDelayStartAlarm && event._isInDelayStart) {
                            // called for delay alarm
                            // start event
                            long oldMergedProfile = mergedProfile._id;
                            event.startEvent(this, eventTimelineList, /*interactive,*/ forRestartEvents, mergedProfile);
                            startProfileMerged = oldMergedProfile != mergedProfile._id;
                            //PPApplication.logE("[DSTART] DataWrapper.doHandleEvents", "mergedProfile=" + mergedProfile._name);
                        }
                    }
                } else if (((newEventStatus == Event.ESTATUS_PAUSE) || forRestartEvents) && statePause) {
                    // do end of events, some sensors are not passed
                    // when pausing and it is for restart events (forRestartEvent=true), force pause

                    if (newEventStatus == Event.ESTATUS_RUNNING) {
                        //event must be running, all sensors are passed
                        if (!forRestartEvents)
                            // it is not restart event, do not pause this event
                            return;
                    }

                    /*if (PPApplication.logEnabled()) {
                        if (event._name.equals("Doma")) {
                            PPApplication.logE("[***] DataWrapper.doHandleEvents", "pause event");
                            PPApplication.logE("[***] DataWrapper.doHandleEvents", "event._name=" + event._name);
                        }
                    }*/

                    if (event._isInDelayStart) {
                        //if (event._name.equals("Doma"))
                        //    PPApplication.logE("[***] DataWrapper.doHandleEvents", "isInDelayStart");
                        event.removeDelayStartAlarm(this);
                    }
                    else {
                        if (!forDelayEndAlarm) {
                            //if (event._name.equals("Doma"))
                            //    PPApplication.logE("[***] DataWrapper.doHandleEvents", "!forDelayEndAlarm");
                            // called not for delay alarm
                            /*if (forRestartEvents) {
                                event._isInDelayEnd = false;
                            } else*/ {
                                if (!event._isInDelayEnd) {
                                    // if not delay alarm is set, set it
                                    // this also set event._isInDelayEnd
                                    event.setDelayEndAlarm(this); // for end delay
                                }
                                if (event._isInDelayEnd) {
                                    // if delay expires, pause event
                                    // this also set event._isInDelayEnd
                                    event.checkDelayEnd(/*this*/);
                                }
                            }
                            if (!event._isInDelayEnd) {
                                // no delay alarm is set
                                // pause event
                                long oldMergedProfile = mergedProfile._id;
                                event.pauseEvent(this, eventTimelineList, true, false,
                                        false, true, mergedProfile, !forRestartEvents, forRestartEvents);
                                endProfileMerged = oldMergedProfile != mergedProfile._id;
                            }
                        }

                        if (forDelayEndAlarm && event._isInDelayEnd) {
                            // called for delay alarm
                            // pause event
                            long oldMergedProfile = mergedProfile._id;
                            event.pauseEvent(this, eventTimelineList, true, false,
                                    false, true, mergedProfile, !forRestartEvents, forRestartEvents);
                            endProfileMerged = oldMergedProfile != mergedProfile._id;
                        }
                    }
                }
            }
        }

        //PPApplication.logE("%%% DataWrapper.doHandleEvents","--- end --------------------------");
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
            EventsHandler eventsHandler = new EventsHandler(context);
            // this do not perform restart, only SENSOR_TYPE_RESTART_EVENTS perform restart
            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RESTART_EVENTS_NOT_UNBLOCK);
            return;
        }

        //PPApplication.logE("DataWrapper._restartEvents", "events are not blocked");

        //Profile activatedProfile = getActivatedProfile();

        if (unblockEventsRun)
        {
            // remove alarm for profile duration
            if (!profileListFilled)
                fillProfileList(false, false);
            for (Profile profile : profileList)
                ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, context);
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

        EventsHandler eventsHandler = new EventsHandler(context);
        if (manualRestart)
            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_MANUAL_RESTART_EVENTS);
        else
            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RESTART_EVENTS);
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

    private void _restartEventsWithRescan(/*boolean forceRestart, */boolean unblockEventsRun, boolean manualRestart, boolean logRestart) {
        //PPApplication.logE("$$$ DataWrapper._restartEventsWithRescan","xxx");

        // remove all event delay alarms
        resetAllEventsInDelayStart(false);
        resetAllEventsInDelayEnd(false);
        // ignore manual profile activation
        // and unblock forceRun events
        restartEvents(unblockEventsRun, /*true, true,*/ manualRestart, logRestart/*, false*/);

        //if (forceRestart || ApplicationPreferences.applicationEventWifiRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
            PPApplication.restartWifiScanner(context, false);
        //}
        //if (forceRestart || ApplicationPreferences.applicationEventBluetoothRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
            PPApplication.restartBluetoothScanner(context, false);
        //}
        //if (forceRestart || ApplicationPreferences.applicationEventLocationRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
            PPApplication.restartGeofenceScanner(context, false);
        //}
        //if (forceRestart || ApplicationPreferences.applicationEventMobileCellsRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS)) {
            PPApplication.restartPhoneStateScanner(context, false);
        //}
        PPApplication.restartTwilightScanner(context);
    }

    void restartEventsWithRescan(/*final boolean forceRestart, */
            final boolean unblockEventsRun, boolean useHandler,
            final boolean manualRestart, final boolean logRestart, boolean showToast)
    {
        //PPApplication.logE("$$$ DataWrapper.restartEventsWithRescan","xxx");

        if (useHandler) {
            final DataWrapper dataWrapper = copyDataWrapper();

            PPApplication.startHandlerThread("DataWrapper.restartEventsWithRescan");
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

                        //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=DataWrapper.restartEventsWithRescan");

                        dataWrapper._restartEventsWithRescan(/*forceRestart, */unblockEventsRun, manualRestart, logRestart);

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
            _restartEventsWithRescan(/*forceRestart, */unblockEventsRun, manualRestart, logRestart);

        if (showToast) {
            if (ApplicationPreferences.notificationsToast) {
                GlobalGUIRoutines.showToast(context.getApplicationContext(),
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

        PPApplication.setBlockProfileEventActions(false, context);

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
                        PPApplication.startPPService(context, serviceIntent);
                    }
                    else {
                        //PPApplication.logE("*********** restartEvents", "from DataWrapper.restartEventsWithAlert() - 1");
                        restartEventsWithRescan(/*true, */true, true, true, true, true);
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
            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    if (positive != null) positive.setAllCaps(false);
                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    if (negative != null) negative.setAllCaps(false);
                }
            });*/
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
                        try {
                            activity.finish();
                        } catch (Exception ignored) {}
                    }
                });
            }

            //PPApplication.logE("*********** restartEvents", "from DataWrapper.restartEventsWithAlert() - 2");
            restartEventsWithRescan(/*true, */true, true, true, true, true);

            //IgnoreBatteryOptimizationNotification.showNotification(context);
        }
    }

    @SuppressLint("NewApi")
    // delay is in seconds, max 5
    void restartEventsWithDelay(int delay, final boolean unblockEventsRun, /*final boolean reactivateProfile,*/
                                /*boolean clearOld,*/ final int logType)
    {
        //PPApplication.logE("[TEST HANDLER] DataWrapper.restartEventsWithDelay","xxx"); //"clearOld="+clearOld);

        /*if (PhoneProfilesService.getInstance() != null)
            PhoneProfilesService.getInstance().willBeDoRestartEvents = true;*/

        //final DataWrapper dataWrapper = copyDataWrapper();

        /*if (PhoneProfilesService.getInstance() != null) {
            ++PhoneProfilesService.getInstance().willBeDoRestartEvents;
        }*/

/*        if (clearOld) {
            Data workData = new Data.Builder()
                    .putBoolean(PhoneProfilesService.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun)
                    .putInt(PhoneProfilesService.EXTRA_LOG_TYPE, logType)
                    .build();

            OneTimeWorkRequest restartEventsWithDelayWorker =
                    new OneTimeWorkRequest.Builder(RestartEventsWithDelayWorker.class)
                            .setInputData(workData)
                            .setInitialDelay(delay, TimeUnit.SECONDS)
                            .build();
            try {
                WorkManager workManager = WorkManager.getInstance(context);
//                workManager.cancelUniqueWork("restartEventsWithDelayClearOldWork");
//                workManager.cancelAllWorkByTag("restartEventsWithDelayClearOldWork");
//                workManager.cancelUniqueWork("restartEventsWithDelayNotClearOldWork");
//                workManager.cancelAllWorkByTag("restartEventsWithDelayNotClearOldWork");
                workManager.enqueueUniqueWork("restartEventsWithDelayClearOldWork", ExistingWorkPolicy.REPLACE, restartEventsWithDelayWorker);
            } catch (Exception ignored) {}

//            PPApplication.startHandlerThreadRestartEventsWithDelay();
//            PPApplication.restartEventsWithDelayHandler.removeCallbacksAndMessages(null);
//            PPApplication.restartEventsWithDelayHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    PPApplication.logE("[TEST HANDLER] DataWrapper.restartEventsWithDelay", "restart from handler");
//                    if (logType != ALTYPE_UNDEFINED)
//                        dataWrapper.addActivityLog(logType, null, null, null, 0);
//                    dataWrapper.restartEventsWithRescan(unblockEventsRun, false, true, false);
//                }
//            }, delay * 1000);
            //PostDelayedBroadcastReceiver.setAlarmForRestartEvents(delay, true, unblockEventsRun, logType, context);
        }
        else {*/
            Data workData = new Data.Builder()
                        .putBoolean(PhoneProfilesService.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun)
                        .putInt(PhoneProfilesService.EXTRA_LOG_TYPE, logType)
                        .build();

            OneTimeWorkRequest restartEventsWithDelayWorker =
                    new OneTimeWorkRequest.Builder(RestartEventsWithDelayWorker.class)
                            .addTag("restartEventsWithDelayWork")
                            .setInputData(workData)
                            .setInitialDelay(delay, TimeUnit.SECONDS)
                            .build();
            try {
                WorkManager workManager = WorkManager.getInstance(context);
                //workManager.enqueueUniqueWork("restartEventsWithDelayNotClearOldWork", ExistingWorkPolicy.REPLACE, restartEventsWithDelayWorker);
                workManager.enqueueUniqueWork("restartEventsWithDelayWork", ExistingWorkPolicy.REPLACE, restartEventsWithDelayWorker);
            } catch (Exception ignored) {}

            /*PPApplication.startHandlerThread("DataWrapper.restartEventsWithDelay");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("[TEST HANDLER] DataWrapper.restartEventsWithDelay", "restart from handler");
                    if (logType != ALTYPE_UNDEFINED)
                        dataWrapper.addActivityLog(logType, null, null, null, 0);
                    dataWrapper.restartEventsWithRescan(unblockEventsRun, false, true, false);
                }
            }, delay * 1000);*/
            //PostDelayedBroadcastReceiver.setAlarmForRestartEvents(delay, false, unblockEventsRun, /*reactivateProfile,*/ logType, context);
        //}
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
            PPApplication.logE("DataWrapper.getIsManualProfileActivation", "getEventsBlocked()=" + Event.getEventsBlocked(context));
            PPApplication.logE("DataWrapper.getIsManualProfileActivation", "getForceRunEventRunning()=" + Event.getForceRunEventRunning(context));
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

    static private Spannable getProfileNameWithManualIndicator(Profile profile, List<EventTimeline> eventTimelineList,
                                                               boolean addEventName, String indicators, boolean addDuration, boolean multiLine,
                                                               boolean durationInNextLine, DataWrapper dataWrapper, boolean fromDB, Context context)
    {
        if (profile == null)
            return new SpannableString("");

        String eventName = "";
        String manualIndicators = "";
        if (addEventName && (dataWrapper != null))
        {
            if (ApplicationPreferences.prefEventsBlocked) {
                if (ApplicationPreferences.prefForceRunEventRunning)
                    manualIndicators = "[»]";
                else
                    manualIndicators = "[M]";
            }

            String _eventName = getLastStartedEventName(eventTimelineList, dataWrapper, fromDB);
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
                sName = profile.getProfileNameWithDuration(eventName, indicators, multiLine, durationInNextLine, context);
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

    static Spannable getProfileNameWithManualIndicator(Profile profile, boolean addEventName, String indicators, boolean addDuration, boolean multiLine,
                                                       boolean durationInNextLine, DataWrapper dataWrapper, boolean fromDB, Context context) {
        if (dataWrapper != null) {
            List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(fromDB);
            return getProfileNameWithManualIndicator(profile, eventTimelineList, addEventName, indicators, addDuration, multiLine, durationInNextLine, dataWrapper, fromDB, context);
        }
        else {
            return getProfileNameWithManualIndicator(profile, null, addEventName, indicators, addDuration, multiLine, durationInNextLine, null, fromDB, context);
        }
    }

    @SuppressWarnings("SameParameterValue")
    static String getProfileNameWithManualIndicatorAsString(Profile profile, boolean addEventName, String indicators, boolean addDuration, boolean multiLine,
                                                            boolean durationInNextLine, DataWrapper dataWrapper, boolean fromDB, Context context) {
        Spannable sProfileName = getProfileNameWithManualIndicator(profile, addEventName, indicators, addDuration, multiLine, durationInNextLine, dataWrapper, fromDB, context);
        Spannable sbt = new SpannableString(sProfileName);
        Object[] spansToRemove = sbt.getSpans(0, sProfileName.length(), Object.class);
        for (Object span : spansToRemove) {
            if (span instanceof CharacterStyle)
                sbt.removeSpan(span);
        }
        return sbt.toString();
    }

    static private String getLastStartedEventName(List<EventTimeline> eventTimelineList, DataWrapper dataWrapper, boolean fromDB)
    {

        if (Event.getGlobalEventsRunning() && PPApplication.getApplicationStarted(false))
        {
            if (eventTimelineList.size() > 0)
            {
                EventTimeline eventTimeLine = eventTimelineList.get(eventTimelineList.size()-1);
                long event_id = eventTimeLine._fkEvent;
                Event event = dataWrapper.getEventById(event_id);
                if (event != null)
                {
                    if ((!ApplicationPreferences.prefEventsBlocked) || (event._forceRun))
                    {
                        Profile profile;
                        if (fromDB)
                            profile = dataWrapper.getActivatedProfileFromDB(false, false);
                        else
                            profile = dataWrapper.getActivatedProfile(false, false);
                        if ((profile != null) && (event._fkProfileStart == profile._id))
                            // last started event activates activated profile
                            return event._name;
                        else
                            return "?";
                    }
                    else
                        return "?";
                }
                else
                    return "?";
            }
            else
            {
                long profileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile);
                if ((!ApplicationPreferences.prefEventsBlocked) && (profileId != Profile.PROFILE_NO_ACTIVATE))
                {
                    Profile profile;
                    if (fromDB)
                        profile = dataWrapper.getActivatedProfileFromDB(false, false);
                    else
                        profile = dataWrapper.getActivatedProfile(false, false);
                    if ((profile != null) && (profile._id == profileId))
                        return dataWrapper.context.getString(R.string.event_name_background_profile);
                    else
                        return "?";
                }
                else
                    return "?";
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
                        PPApplication.showProfileNotification(/*activity.getApplicationContext()*/true, false);

                        /*if (activity instanceof EditorProfilesActivity)
                            ((EditorProfilesActivity) activity).refreshGUI(true, false, true, 0, 0);
                        else if (activity instanceof ActivateProfileActivity)
                            ((ActivateProfileActivity) activity).refreshGUI(true, false);*/
                        //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper.runStopEventsWithAlert");
                        ActivateProfileHelper.updateGUI(activity, true, true);
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
            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    if (positive != null) positive.setAllCaps(false);
                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    if (negative != null) negative.setAllCaps(false);
                }
            });*/
            if (!activity.isFinishing())
                dialog.show();
        }
        else {
            if (globalRunStopEvents(false)) {
                PPApplication.showProfileNotification(/*activity.getApplicationContext()*/true, false);
                /*if (activity instanceof EditorProfilesActivity)
                    ((EditorProfilesActivity) activity).refreshGUI(true, false, true, 0, 0);
                else if (activity instanceof ActivateProfileActivity)
                    ((ActivateProfileActivity) activity).refreshGUI(true, false);*/
                //PPApplication.logE("ActivateProfileHelper.updateGUI", "from DataWrapper.runStopEventsWithAlert");
                ActivateProfileHelper.updateGUI(activity, true, true);
            }
        }
    }

    private boolean globalRunStopEvents(boolean stop) {
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
        boolean isCharging = false;
        int batteryPct = -100;
        boolean isPowerSaveMode = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null)
                isPowerSaveMode = powerManager.isPowerSaveMode();
        }

        Intent batteryStatus = null;
        try { // Huawei devices: java.lang.IllegalArgumentException: registered too many Broadcast Receivers
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            batteryStatus = context.registerReceiver(null, filter);
        } catch (Exception ignored) {}
        if (batteryStatus != null) {
            //int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            //PPApplication.logE("DataWrapper.isPowerSaveMode", "status=" + status);
            int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            isCharging = plugged == BatteryManager.BATTERY_PLUGGED_AC
                    || plugged == BatteryManager.BATTERY_PLUGGED_USB
                    || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
            //isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            //             status == BatteryManager.BATTERY_STATUS_FULL;
            //PPApplication.logE("DataWrapper.isPowerSaveMode", "isCharging=" + isCharging);

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("DataWrapper.isPowerSaveMode", "level=" + level);
                PPApplication.logE("DataWrapper.isPowerSaveMode", "scale=" + scale);
            }*/

            batteryPct = Math.round(level / (float) scale * 100);
            //PPApplication.logE("DataWrapper.isPowerSaveMode", "batteryPct=" + batteryPct);
        }

        String applicationPowerSaveModeInternal = ApplicationPreferences.applicationPowerSaveModeInternal;

        if (applicationPowerSaveModeInternal.equals("1") && (batteryPct <= 5) && (!isCharging))
            return true;
        if (applicationPowerSaveModeInternal.equals("2") && (batteryPct <= 15) && (!isCharging))
            return true;
        if (applicationPowerSaveModeInternal.equals("3"))
            return isPowerSaveMode;

        return false;
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
            DatabaseHandler.getInstance(context.getApplicationContext()).updateAlarmClockStartTime(_event);
            _event._eventPreferencesAlarmClock.removeAlarm(context);
        }
    }

    void clearSensorsStartTime(/*boolean force*/) {
        for (Event _event : eventList) {
            clearSensorsStartTime(_event, false/*force*/);
        }
    }

}
