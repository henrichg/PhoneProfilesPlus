package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

//import me.drakeet.support.toast.ToastCompat;

public class DataWrapper {

    final Context context;
    //private boolean forGUI = false;
    private boolean monochrome = false;
    private int monochromeValue = 0xFF;
    private boolean useMonochromeValueForCustomIcon = false;
    private int indicatorsType = 0;
    private int indicatorsMonoValue = 0xFF;
    private float indicatorsLightnessValue = 0f;

    boolean profileListFilled = false;
    boolean eventListFilled = false;
    boolean eventTimelineListFilled = false;
    @SuppressWarnings("Convert2Diamond")
    final List<Profile> profileList = Collections.synchronizedList(new ArrayList<Profile>());
    @SuppressWarnings("Convert2Diamond")
    final List<Event> eventList = Collections.synchronizedList(new ArrayList<Event>());
    @SuppressWarnings("Convert2Diamond")
    final List<EventTimeline> eventTimelines = Collections.synchronizedList(new ArrayList<EventTimeline>());

    //static final String EXTRA_INTERACTIVE = "interactive";

    private static final String ACTIVATED_PROFILES_FIFO_COUNT_PREF = "activated_profiles_fifo_count";
    private static final String ACTIVATED_PROFILES_FIFO_ID_PREF = "activated_profiles_fifo_id";

    static final int IT_FOR_EDITOR = 1;
    static final int IT_FOR_NOTIFICATION = 2;
    static final int IT_FOR_WIDGET = 3;
    static final int IT_FOR_WIDGET_DYNAMIC_COLORS = 4;
    static final int IT_FOR_NOTIFICATION_DYNAMIC_COLORS = 5;
    static final int IT_FOR_NOTIFICATION_NATIVE_BACKGROUND = 6;
    static final int IT_FOR_NOTIFICATION_DARK_BACKGROUND = 7;
    static final int IT_FOR_NOTIFICATION_LIGHT_BACKGROUND = 8;
    static final int IT_FOR_WIDGET_DARK_BACKGROUND = 9;
    static final int IT_FOR_WIDGET_LIGHT_BACKGROUND = 10;
    static final int IT_FOR_WIDGET_NATIVE_BACKGROUND = 11;

    DataWrapper(Context _context,
                        //boolean fgui,
                        boolean mono,
                        int monoVal,
                        boolean useMonoValForCustomIcon,
                        int indicatorsType,
                        int indicatorsMonoVal,
                        float indicatorsLightnessVal)
    {
        context = _context.getApplicationContext();

        setParameters(/*fgui, */mono, monoVal, useMonoValForCustomIcon, indicatorsType, indicatorsMonoVal, indicatorsLightnessVal);
    }

    void setParameters(
            //boolean fgui,
            boolean mono,
            int monoVal,
            boolean useMonoValForCustomIcon,
            int indicatorsType,
            int indicatorsMonoVal,
            float indicatorsLightnessVal)
    {
        //forGUI = fgui;
        monochrome = mono;
        monochromeValue = monoVal;
        useMonochromeValueForCustomIcon = useMonoValForCustomIcon;
        this.indicatorsType = indicatorsType;
        indicatorsMonoValue = indicatorsMonoVal;
        indicatorsLightnessValue = indicatorsLightnessVal;
    }

    DataWrapper copyDataWrapper() {
        DataWrapper dataWrapper = new DataWrapper(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon, indicatorsType, indicatorsMonoValue, indicatorsLightnessValue);
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
                    profile.generatePreferencesIndicator(context, monochrome, indicatorsMonoValue, indicatorsType, indicatorsLightnessValue);
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
                profile = DataWrapperStatic.getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_home), "ic_profile_home_2", index+1);
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
                profile = DataWrapperStatic.getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_outdoor), "ic_profile_outdoors_1", index+1);
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
                profile = DataWrapperStatic.getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_work), "ic_profile_work_5", index+1);
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
                profile = DataWrapperStatic.getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_meeting), "ic_profile_meeting_2", index+1);
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
                profile = DataWrapperStatic.getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_sleep), "ic_profile_sleep", index+1);
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
                profile = DataWrapperStatic.getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_battery_low), "ic_profile_battery_1", index+1);
                profile._showInActivator = false;
                profile._deviceAutoSync = 2;
                if (RootUtils.isRooted(true))
                    profile._deviceMobileData = 2;
                profile._deviceWiFi = 2;
                profile._deviceBluetooth = 2;
                if (RootUtils.isRooted(true) ||
                        Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS))
                    profile._deviceGPS = 2;
                break;
            case 6:
                profile = DataWrapperStatic.getNonInitializedProfile(baseContext.getString(R.string.default_profile_name_battery_ok), "ic_profile_battery_3", index+1);
                profile._showInActivator = false;
                profile._deviceAutoSync = 1;
                if (RootUtils.isRooted(true))
                    profile._deviceMobileData = 1;
                profile._deviceWiFi = 1;
                profile._deviceBluetooth = 1;
                if (RootUtils.isRooted(true) ||
                        Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS))
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

    void clearProfileList() {
        synchronized (profileList) {
            for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                //noinspection unused
                Profile profile = it.next(); // this must be called
                it.remove();
            }
            profileListFilled = false;
        }
    }
    void invalidateProfileList()
    {
        synchronized (profileList) {
            //if (profileListFilled)
            //{
                for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    profile.releaseIconBitmap();
                    profile.releasePreferencesIndicator();
                    it.remove();
                }
            //}
            profileListFilled = false;
        }
    }

    Profile getActivatedProfileFromDB(boolean generateIcon, boolean generateIndicators)
    {
        Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
        if (/*forGUI &&*/ (profile != null))
        {
            if (generateIcon)
                profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
            if (generateIndicators)
                profile.generatePreferencesIndicator(context, monochrome, indicatorsMonoValue, indicatorsType, indicatorsLightnessValue);
        }
        return profile;
    }

    Profile getActivatedProfile(boolean generateIcon, boolean generateIndicators)
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

    Profile getActivatedProfile(List<Profile> profileList) {
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

            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = profileList.iterator(); it.hasNext();) {
                Profile _profile = it.next();
                _profile._checked = false;
            }


            if (profile != null) {
                profile._checked = true;
                PPApplication.setLastActivatedProfile(context, profile._id);
            }
            else {
                PPApplication.setLastActivatedProfile(context, 0);
            }

        }
    }

    void activateProfileFromEvent(long event_id, long profile_id, boolean manualActivation, boolean merged, boolean forRestartEvents)
    {
        if (event_id != 0) {
            // save before activated profile into FIFO
            Profile activatedProfile = getActivatedProfileFromDB(false, false);
            if (activatedProfile != null) {
                long profileId = activatedProfile._id;
                fifoAddProfile(profileId, event_id);
            }
        }

        int startupSource = PPApplication.STARTUP_SOURCE_EVENT;
        if (manualActivation)
            startupSource = PPApplication.STARTUP_SOURCE_EVENT_MANUAL;
        Profile profile = getProfileById(profile_id, false, false, merged);
        if (profile == null)
            return;
        //if (Permissions.grantProfilePermissions(context, profile, merged, true,
        //        /*false, monochrome, monochromeValue,*/
        //        startupSource, false,true, false)) {
        if (!DataWrapperStatic.displayPreferencesErrorNotification(profile, null, true, context)) {
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
                profile.generatePreferencesIndicator(context, monochrome, indicatorsMonoValue, indicatorsType, indicatorsLightnessValue);
        }
        return profile;
    }

    Profile getProfileById(long id, boolean generateIcon, boolean generateIndicators, boolean merged)
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
            notificationManager.cancel(
                    PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_TAG,
                    PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_ID + (int) profile._id);

            profileList.remove(profile);
        }
        ActivateProfileHelper.cancelNotificationsForInteractiveParameters(context);
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
                    notificationManager.cancel(
                            PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_TAG,
                            PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_ID + (int) profile._id);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
            profileList.clear();
            ActivateProfileHelper.cancelNotificationsForInteractiveParameters(context);
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
                    profile.generatePreferencesIndicator(context, monochrome, indicatorsMonoValue, indicatorsType, indicatorsLightnessValue);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    void generateProfileIcon(Profile profile,
                             boolean generateIcon,
                             boolean generateIndicator) {
        if (generateIcon)
            profile.generateIconBitmap(context, monochrome, monochromeValue, useMonochromeValueForCustomIcon);
        if (generateIndicator)
            profile.generatePreferencesIndicator(context, monochrome, indicatorsMonoValue, indicatorsType, indicatorsLightnessValue);
    }

    void setDynamicLauncherShortcutsFromMainThread()
    {
        //final DataWrapper dataWrapper = copyDataWrapper();

        final Context appContext = context;
        //PPApplication.startHandlerThread(/*"DataWrapper.setDynamicLauncherShortcutsFromMainThread"*/);
        //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(
        //        context, dataWrapper, null, null) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=DataWrapper.setDynamicLauncherShortcutsFromMainThread");

            //Context appContext= appContextWeakRef.get();
            //DataWrapper dataWrapper = dataWrapperWeakRef.get();
            //Profile profile = profileWeakRef.get();
            //Activity activity = activityWeakRef.get();

            //if ((appContext != null) && (dataWrapper != null) /*&& (profile != null) && (activity != null)*/) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_setDynamicLauncherShortcutsFromMainThread");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    DataWrapperStatic.setDynamicLauncherShortcuts(context);

                } catch (Exception e) {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        }; //);
        PPApplication.createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);
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

    void clearEventList()
    {
        synchronized (eventList) {
            //if (eventListFilled)
                eventList.clear();
            eventListFilled = false;
        }
    }

    void invalidateEventList()
    {
        //clearEventList();
        synchronized (eventList) {
            //if (eventListFilled)
                eventList.clear();
            eventListFilled = false;
        }
    }

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
        synchronized (PPApplication.eventsHandlerMutex) {
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
                if (alsoUnlink) {
                    unlinkEventsFromProfile(profile);
                    DatabaseHandler.getInstance(context).unlinkEventsFromProfile(profile);
                }
            }
        }
        //restartEvents(false, true, true, true, true);
        restartEventsWithRescan(true, false, true, false, true, false);
    }

    void stopEventsForProfileFromMainThread(Profile profile,
                                            @SuppressWarnings("SameParameterValue") final boolean alsoUnlink) {
        final DataWrapper dataWrapper = copyDataWrapper();

        final Context appContext = context;
        //PPApplication.startHandlerThread(/*"DataWrapper.stopEventsForProfileFromMainThread"*/);
        //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(
        //        context, dataWrapper, profile, null) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=DataWrapper.stopEventsForProfileFromMainThread");

            //Context appContext= appContextWeakRef.get();
            //DataWrapper dataWrapper = dataWrapperWeakRef.get();
            //Profile profile = profileWeakRef.get();
            //Activity activity = activityWeakRef.get();

            //if ((appContext != null) && (dataWrapper != null) && (profile != null) /*&& (activity != null)*/) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_stopEventsForProfileFromMainThread");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    dataWrapper.stopEventsForProfile(profile, alsoUnlink);

                } catch (Exception e) {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        }; //);
        PPApplication.createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);
    }

    // pauses all events
    void pauseAllEvents(boolean noSetSystemEvent, boolean blockEvents/*, boolean activateReturnProfile*/)
    {
        // blockEvents == true -> manual profile activation is set
        Event.setEventsBlocked(context, blockEvents);

        getEventTimelineList(true);

        synchronized (eventList) {
            fillEventList();
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                if (event != null) {
                    int status = event.getStatusFromDB(context);

                    if (status == Event.ESTATUS_RUNNING) {
                        if (!(event._ignoreManualActivation && event._noPauseByManualActivation)) {
                            event.pauseEvent(this, false, true, noSetSystemEvent, true, null, false, false, true);
                        }
                    }

                    setEventBlocked(event, false);
                    if (blockEvents && (status == Event.ESTATUS_RUNNING) && event._ignoreManualActivation) {
                        // block only running forceRun events
                        if (!event._noPauseByManualActivation) // do not pause event, even when is running
                            setEventBlocked(event, true);
                    }

                    if (!(event._ignoreManualActivation && event._noPauseByManualActivation)) {
                        // for "push" events, set startTime to 0
                        clearSensorsStartTime(event, true);
                    }
                }
            }
        }
    }

    private void pauseAllEventsForGlobalStopEvents() {
        final DataWrapper dataWrapper = copyDataWrapper();

        final Context appContext = context;
        //PPApplication.startHandlerThread(/*"DataWrapper.pauseAllEventsForGlobalStopEvents"*/);
        //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(
        //        context, dataWrapper, null, null) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=DataWrapper.pauseAllEventsForGlobalStopEvents");

            //Context appContext= appContextWeakRef.get();
            //DataWrapper dataWrapper = dataWrapperWeakRef.get();
            //Profile profile = profileWeakRef.get();
            //Activity activity = activityWeakRef.get();

            //if ((appContext != null) && (dataWrapper != null) /*&& (profile != null) && (activity != null)*/) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_pauseAllEventsForGlobalStopEvents");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    synchronized (PPApplication.eventsHandlerMutex) {
                        dataWrapper.pauseAllEvents(true, false);
                    }

                } catch (Exception e) {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        }; //);
        PPApplication.createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);
    }

    // stops all events
    void stopAllEvents(boolean saveEventStatus, boolean alsoDelete, boolean log, boolean updateGUI)
    {
        synchronized (PPApplication.eventsHandlerMutex) {
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
    }

    void stopAllEventsFromMainThread(@SuppressWarnings("SameParameterValue") final boolean saveEventStatus,
                                     final boolean alsoDelete) {
        final DataWrapper dataWrapper = copyDataWrapper();

        final Context appContext = context;
        //PPApplication.startHandlerThread(/*"DataWrapper.stopAllEventsFromMainThread"*/);
        //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(
        //        context, dataWrapper, null, null) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=DataWrapper.stopAllEventsFromMainThread");

            //Context appContext= appContextWeakRef.get();
            //DataWrapper dataWrapper = dataWrapperWeakRef.get();
            //Profile profile = profileWeakRef.get();
            //Activity activity = activityWeakRef.get();

            //if ((appContext != null) && (dataWrapper != null) /*&& (profile != null) && (activity != null)*/) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_stopAllEventsFromMainThread");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    dataWrapper.stopAllEvents(saveEventStatus, alsoDelete, true, true);

                } catch (Exception e) {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        }; //);
        PPApplication.createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);
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

    void activateProfileAtFirstStart()
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
            if (profileId == 0) {

                profileId = ApplicationPreferences.getApplicationDefaultProfileOnBoot();

                if (profileId == Profile.PROFILE_NO_ACTIVATE)
                    profileId = 0;
            }

            activateProfile(profileId, PPApplication.STARTUP_SOURCE_FOR_FIRST_START, null, true);
        }
        // do not remove last activated profile at first start
        //else
        //    activateProfile(0, startupSource, null, true);
    }

    private void startEventsAtFirstStart(boolean startedFromService, boolean useHandler)
    {
        if (startedFromService) {
            if (ApplicationPreferences.applicationActivate &&
                    ApplicationPreferences.applicationStartEvents) {
                //restartEvents(false, false, true, false, useHandler);
                restartEventsWithRescan(true, false, useHandler, false, false, false);
//                restartEventsWithDelay(5, true, false, true, PPApplication.ALTYPE_UNDEFINED);
            }
            else {
                //Event.setGlobalEventsRunning(context, false);
                PPApplication.setApplicationFullyStarted(context);
//                PPApplication.logE("[APPLICATION_FULLY_STARTED] DataWrapper.startEventsAtFirstStart", "xxx");
                activateProfileAtFirstStart();
            }
        }
        else {
            //restartEvents(false, false, true, false, useHandler);
            restartEventsWithRescan(true, false, useHandler, false, false, false);
//            restartEventsWithDelay(5, true, false, true, PPApplication.ALTYPE_UNDEFINED);
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

        if (!DataWrapperStatic.getIsManualProfileActivation(false, context)) {
            PPApplication.logE("DataWrapper.firstStartEvents", "no manual profile activation, restart events");
        }
        else
        {
            PPApplication.logE("DataWrapper.firstStartEvents", "manual profile activation, activate profile");

            activateProfileAtFirstStart();
        }
        startEventsAtFirstStart(startedFromService, useHandler);
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
                event = DataWrapperStatic.getNonInitializedEvent(baseContext.getString(R.string.default_event_name_during_the_week), index+1);
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
                event = DataWrapperStatic.getNonInitializedEvent(baseContext.getString(R.string.default_event_name_weekend), index+1);
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
                event = DataWrapperStatic.getNonInitializedEvent(baseContext.getString(R.string.default_event_name_during_the_work), index+1);
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
                event = DataWrapperStatic.getNonInitializedEvent(baseContext.getString(R.string.default_event_name_overnight), index+1);
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
                event = DataWrapperStatic.getNonInitializedEvent(baseContext.getString(R.string.default_event_name_night_call), index+1);
                event._fkProfileStart = getProfileIdByName(baseContext.getString(R.string.default_profile_name_home), false);
                //if (event._fkProfileStart == 0)
                //    event._fkProfileStart = getPredefinedProfile(0, true, baseContext)._id;
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_UNDONE_PROFILE;
                event._priority = Event.EPRIORITY_HIGHEST;
                event._ignoreManualActivation = true;
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
                event = DataWrapperStatic.getNonInitializedEvent(baseContext.getString(R.string.default_event_name_low_battery), index+1);
                event._fkProfileStart = getProfileIdByName(baseContext.getString(R.string.default_profile_name_battery_low), false);
                //if (event._fkProfileStart == 0)
                //    event._fkProfileStart = getPredefinedProfile(5, true, baseContext)._id;
                event._fkProfileEnd = getProfileIdByName(baseContext.getString(R.string.default_profile_name_battery_ok), false);
                //if (event._fkProfileEnd == 0)
                //    event._fkProfileEnd = getPredefinedProfile(6, true, baseContext)._id;
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_RESTART_EVENTS;
                event._priority = Event.EPRIORITY_HIGHEST;
                event._ignoreManualActivation = true;
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

    void clearEventTimelineList() {
        synchronized (eventTimelines) {
            //if (eventTimelineListFilled)
            eventTimelines.clear();
            eventTimelineListFilled = false;
        }
    }
    private void invalidateEventTimelineList()
    {
        synchronized (eventTimelines) {
            //if (eventTimelineListFilled)
                eventTimelines.clear();
            eventTimelineListFilled = false;
        }
    }

    List<EventTimeline> getEventTimelineList(boolean fromDB)
    {
        synchronized (eventTimelines) {
            if (!eventTimelineListFilled || fromDB) {
                if (fromDB)
                    invalidateEventTimelineList();
                fillEventTimelineList();
            }
        }
        return eventTimelines;
    }

//-------------------------------------------------------------------------------------------------------------------

    void invalidateDataWrapper()
    {
        invalidateProfileList();
        invalidateEventList();
        invalidateEventTimelineList();
    }

//----- Activate profile ---------------------------------------------------------------------------------------------

    private void _activateProfile(Profile _profile, boolean merged, int startupSource, final boolean forRestartEvents)
    {
            // show notification when battery optimization is not enabled
            DrawOverAppsPermissionNotification.showNotification(context, false);
            IgnoreBatteryOptimizationNotification.showNotification(context, false);

            // remove last configured profile duration alarm
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(_profile, context);
            //Profile.setActivatedProfileForDuration(context, 0);

            // get currently activated profile
            //Profile oldActivatedProfile = getActivatedProfile(false, false);

            if ((startupSource != PPApplication.STARTUP_SOURCE_EVENT) //&&
                //(startupSource != PPApplication.STARTUP_SOURCE_BOOT) &&  // on boot must set as manual activation
                //(startupSource != PPApplication.STARTUP_SOURCE_LAUNCHER_START)
            ) {
                // manual profile activation

                PPApplication.lockRefresh = true;

                // pause all events
                // for forceRun events set system events and block all events
                pauseAllEvents(false, true/*, true*/);

                PPApplication.lockRefresh = false;
            }

            DatabaseHandler.getInstance(context).activateProfile(_profile);
            setProfileActive(_profile);

            boolean profileDuration = false;
            if (_profile != null) {
                if ((_profile._endOfActivationType == 0) &&
                        (_profile._afterDurationDo != Profile.AFTER_DURATION_DO_NOTHING) &&
                        (_profile._duration > 0)) {
                    profileDuration = true;
                }
                else
                if (_profile._endOfActivationType == 1) {
                    Calendar now = Calendar.getInstance();

                    Calendar configuredTime = Calendar.getInstance();
                    configuredTime.set(Calendar.HOUR_OF_DAY, _profile._endOfActivationTime / 60);
                    configuredTime.set(Calendar.MINUTE, _profile._endOfActivationTime % 60);
                    configuredTime.set(Calendar.SECOND, 0);
                    configuredTime.set(Calendar.MILLISECOND, 0);

                    if (now.getTimeInMillis() < configuredTime.getTimeInMillis()) {
                        // configured time is not expired
                        profileDuration = true;
                    }
                }

                if (((startupSource != PPApplication.STARTUP_SOURCE_EVENT) &&
                     (startupSource != PPApplication.STARTUP_SOURCE_FOR_FIRST_START) //&&
                   //(startupSource != PPApplication.STARTUP_SOURCE_LAUNCHER_START)
                    ) ||
                    ((!_profile._askForDuration) &&
                     (_profile._afterDurationDo == Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE))
                ) {
                    // activation with duration


                    if (startupSource != PPApplication.STARTUP_SOURCE_EVENT_MANUAL) {
                        long profileId = _profile._id;
                        fifoAddProfile(profileId, 0);
                    }

                    ProfileDurationAlarmBroadcastReceiver.setAlarm(_profile, forRestartEvents, startupSource, context);
                    ///////////
                } else {
                    profileDuration = false;

                }

            }

//            PPApplication.logE("[PPP_NOTIFICATION] DataWrapper._activateProfile", "call of updateGUI");
            PPApplication.updateGUI(false, false, context);

            if (_profile != null) {
                ActivateProfileHelper.execute(context, _profile);
            }

            if (/*(mappedProfile != null) &&*/ (!merged)) {
                PPApplication.addActivityLog(context, PPApplication.ALTYPE_PROFILE_ACTIVATION,
                        null,
                        DataWrapperStatic.getProfileNameWithManualIndicatorAsString(_profile, true, "", profileDuration, false, false, this),
                        "");
            }

            //if (mappedProfile != null)
            //{
            if (ApplicationPreferences.notificationsToast &&
                    (!PPApplication.lockRefresh) &&
                    (PPApplication.applicationFullyStarted &&
                            PPApplication.normalServiceStart &&
                            PPApplication.showToastForProfileActivation)) {
                // toast notification
                if (PPApplication.toastHandler != null) {
                    final Profile __profile = _profile;
                    PPApplication.toastHandler.post(() -> showToastAfterActivation(__profile));
                }// else
                //    showToastAfterActivation(profile);
            }
            //}
//        }
    }

    void activateProfileFromMainThread(final Profile profile, final boolean merged, final int startupSource,
                                    final boolean interactive, final Activity activity, final boolean testGrant)
    {
        final DataWrapper dataWrapper = copyDataWrapper();

        final Context appContext = context;
        //PPApplication.startHandlerThread(/*"DataWrapper.activateProfileFromMainThread"*/);
        //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(
        //        context, dataWrapper, profile, _activity) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=DataWrapper.activateProfileFromMainThread");

            //Context appContext= appContextWeakRef.get();
            //DataWrapper dataWrapper = dataWrapperWeakRef.get();
            //Profile profile = profileWeakRef.get();
            //Activity activity = activityWeakRef.get();

            //if ((appContext != null) && (dataWrapper != null) && (profile != null) && (activity != null)) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_activateProfileFromMainThread");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    synchronized (PPApplication.eventsHandlerMutex) {

                        boolean granted = true;
                        if (testGrant)
                            granted = !DataWrapperStatic.displayPreferencesErrorNotification(profile, null, true, context);
                        if (granted) {
                            dataWrapper._activateProfile(profile, merged, startupSource, false);
                            if (interactive) {
                                DatabaseHandler.getInstance(dataWrapper.context).increaseActivationByUserCount(profile);
                                DataWrapperStatic.setDynamicLauncherShortcuts(context);
                            }
                        }

                    }

                } catch (Exception e) {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        }; //);
        PPApplication.createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);

        // for startActivityForResult
        if (activity != null)
        {
            //final Profile profile = _profile; //Profile.getMappedProfile(_profile, context);

            Intent returnIntent = new Intent();
            if (profile == null)
                returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            else
                returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            returnIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
            activity.setResult(Activity.RESULT_OK,returnIntent);
        }

        finishActivity(startupSource, true, activity);

    }

    private void showToastAfterActivation(Profile profile)
    {
        //boolean fullyStarted = false;
        //if (PhoneProfilesService.getInstance() != null)
        //    fullyStarted = PhoneProfilesService.getInstance().getApplicationFullyStarted();

//        boolean fullyStarted = PPApplication.applicationFullyStarted;

        //fullyStarted = fullyStarted && (!PPApplication.applicationPackageReplaced);

//        if (!fullyStarted)
//            return;

        try {
            String profileName = DataWrapperStatic.getProfileNameWithManualIndicatorAsString(profile, true, "", false, false, false, this);
            PPApplication.showToast(context.getApplicationContext(),
                    context.getString(R.string.toast_profile_activated_0) + ": " + profileName + " " +
                            context.getString(R.string.toast_profile_activated_1),
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
            GlobalGUIRoutines.setTheme(activity, true, true/*, false*/, false, false, false, false);
            //GlobalGUIRoutines.setLanguage(activity);

            final Profile _profile = profile;
            //final boolean _interactive = interactive;
            final int _startupSource = startupSource;
            final Activity _activity = activity;
            final DataWrapper _dataWrapper = this;

            if (profile._askForDuration) {
                if (!_activity.isFinishing()) {
                    AskForDurationDialog dlg = new AskForDurationDialog(_activity, _profile, _dataWrapper,
                            /*monochrome, monochromeValue,*/ _startupSource);
                    dlg.show();
                }
            }
            else {
                PPAlertDialog dialog = new PPAlertDialog(
                        activity.getString(R.string.profile_string_0) + ": " + profile._name,
                        activity.getString(R.string.activate_profile_alert_message),
                        activity.getString(R.string.alert_button_yes),
                        activity.getString(R.string.alert_button_no),
                        null, null,
                        (dialog1, which) -> {
                            //if (Permissions.grantProfilePermissions(context, _profile, false, true,
                            //        /*false, monochrome, monochromeValue,*/
                            //        _startupSource, true, true, false))
                            if (!DataWrapperStatic.displayPreferencesErrorNotification(_profile, null, true, context)) {

                                if ((startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT) ||
                                        (startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
                                        (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR) ||
                                        (startupSource == PPApplication.STARTUP_SOURCE_EDITOR) ||
                                        (startupSource == PPApplication.STARTUP_SOURCE_QUICK_TILE)) {
                                    if (!ApplicationPreferences.applicationApplicationProfileActivationNotificationSound.isEmpty() || ApplicationPreferences.applicationApplicationProfileActivationNotificationVibrate) {
                                        if (PhoneProfilesService.getInstance() != null) {
                                            PhoneProfilesService.getInstance().playNotificationSound(
                                                    ApplicationPreferences.applicationApplicationProfileActivationNotificationSound,
                                                    ApplicationPreferences.applicationApplicationProfileActivationNotificationVibrate,
                                                false);
                                            //PPApplication.sleep(500);
                                        }
                                    }
                                }

                                _dataWrapper.activateProfileFromMainThread(_profile, false, _startupSource, true, _activity, false);
                            } else {
                                Intent returnIntent = new Intent();
                                _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                                finishActivity(_startupSource, true, _activity);
                            }
                        },
                        (dialog2, which) -> {
                            // for startActivityForResult
                            Intent returnIntent = new Intent();
                            _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                            finishActivity(_startupSource, false, _activity);
                        },
                        null,
                        dialog3 -> {
                            // for startActivityForResult
                            Intent returnIntent = new Intent();
                            _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                            finishActivity(_startupSource, false, _activity);
                        },
                        null,
                        true, true,
                        false, false,
                        true,
                        activity
                );

                if (!activity.isFinishing())
                    dialog.show();
            }
        }
        else
        {
            GlobalGUIRoutines.setTheme(activity, true, true/*, false*/, false, false, false, false);
            //GlobalGUIRoutines.setLanguage(activity);

            if (profile._askForDuration/* && interactive*/) {
                if (!activity.isFinishing()) {
                    AskForDurationDialog dlg = new AskForDurationDialog(activity, profile, this,
                            /*monochrome, monochromeValue,*/ startupSource);
                    dlg.show();
                }
            }
            else {
                if (!DataWrapperStatic.displayPreferencesErrorNotification(profile, null, true, context)) {
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
                if (PPApplication.getApplicationStarted(false, false))
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
            handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=DataWrapper.finishActivity");

                try {
                    //if (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR)
                    //    _activity.finishAndRemoveTask();
                    //else
                        _activity.finish();
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            });
        }
    }

    void activateProfile(final long profile_id, final int startupSource, final Activity activity, boolean testGrant)
    {
        Profile profile;

        // for activated profile is recommended update of activity
        if (startupSource == PPApplication.STARTUP_SOURCE_FOR_FIRST_START) {
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
            (startupSource == PPApplication.STARTUP_SOURCE_EVENT) ||
            (startupSource == PPApplication.STARTUP_SOURCE_EVENT_MANUAL) ||
            (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER) ||
            (startupSource == PPApplication.STARTUP_SOURCE_QUICK_TILE))
        {
            // activation is invoked from shortcut, widget, Activator, Editor, service,
            // do profile activation
            actProfile = true;
            //interactive = ((startupSource != PPApplication.STARTUP_SOURCE_EVENT));
        }
        else
        if (startupSource == PPApplication.STARTUP_SOURCE_FOR_FIRST_START)
        {
            // activation is invoked during device boot

            //ProfileDurationAlarmBroadcastReceiver.removeAlarm(null, context);
            //Profile.setActivatedProfileForDuration(context, 0);

            synchronized (PPApplication.profileActivationMutex) {
                List<String> activateProfilesFIFO = new ArrayList<>();
                fifoSaveProfiles(activateProfilesFIFO);
            }

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
            (startupSource == PPApplication.STARTUP_SOURCE_EVENT) ||
            //(startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER_START) ||
            (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER) ||
            (startupSource == PPApplication.STARTUP_SOURCE_QUICK_TILE))
        {
            if (profile_id == 0)
                profile = null;
            else
                profile = getProfileById(profile_id, false, false, false);
        }


        if (actProfile && (profile != null))
        {
            // profile activation
            if (startupSource == PPApplication.STARTUP_SOURCE_FOR_FIRST_START) {
                activateProfileFromMainThread(profile, false, PPApplication.STARTUP_SOURCE_FOR_FIRST_START,
                        false, null, testGrant);
            }
            else
                activateProfileWithAlert(profile, startupSource, /*interactive,*/ activity);
        }
        else
        {
            if (profile != null) {
                DatabaseHandler.getInstance(context).activateProfile(profile);
                setProfileActive(profile);
            }

//            PPApplication.logE("[PPP_NOTIFICATION] DataWrapper.activateProfile", "call of updateGUI");
            PPApplication.updateGUI(false, false, context);

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
        synchronized (PPApplication.eventsHandlerMutex) {

            Profile profile = getProfileById(profile_id, false, false, false);
            if (profile == null) {
                ProfileDurationAlarmBroadcastReceiver.removeAlarm(null, context);
                //Profile.setActivatedProfileForDuration(context, 0);

//                PPApplication.logE("[PPP_NOTIFICATION] DataWrapper.activateProfileAfterDuration", "call of updateGUI");
                PPApplication.updateGUI(false, false, context);
                return;
            }
            //if (Permissions.grantProfilePermissions(context, profile, false, true,
            //        /*false, monochrome, monochromeValue,*/
            //        startupSource, true,true, false)) {
            if (!DataWrapperStatic.displayPreferencesErrorNotification(profile, null, true, context)) {
                // activateProfileAfterDuration is already called from handlerThread
                _activateProfile(profile, false, startupSource, false);
            }

        }
    }

    private void _restartEvents(final boolean unblockEventsRun, /*final boolean notClearActivatedProfile,*/
                                /*final boolean reactivateProfile,*/ final boolean manualRestart, final boolean logRestart)
    {
            if (logRestart) {
                if (manualRestart)
                    PPApplication.addActivityLog(context, PPApplication.ALTYPE_MANUAL_RESTART_EVENTS, null, null, "");
                else
                    PPApplication.addActivityLog(context, PPApplication.ALTYPE_RESTART_EVENTS, null, null, "");
            }

            //if ((ApplicationPreferences.prefEventsBlocked && (!unblockEventsRun)) /*|| (!reactivateProfile)*/) {
            if ((Event.getEventsBlocked(context) && (!unblockEventsRun)) /*|| (!reactivateProfile)*/) {

//                PPApplication.logE("[EVENTS_HANDLER_CALL] DataWrapper._restartEvents", "sensorType=SENSOR_TYPE_RESTART_EVENTS_NOT_UNBLOCK");
                EventsHandler eventsHandler = new EventsHandler(context);
                // this do not perform restart, only SENSOR_TYPE_RESTART_EVENTS perform restart
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RESTART_EVENTS_NOT_UNBLOCK);

                return;
            }

            //Profile activatedProfile = getActivatedProfile();

            if (unblockEventsRun) {
                synchronized (profileList) {
                    // remove alarm for profile duration
                    if (!profileListFilled)
                        fillProfileList(false, false);
                    for (Profile profile : profileList)
                        ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, context);
                }
                //Profile.setActivatedProfileForDuration(context, 0);

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

                DatabaseHandler.getInstance(context).unblockAllEvents();
                Event.setForceRunEventRunning(context, false);
            }

            /*if (!notClearActivatedProfile) {
                DatabaseHandler.getInstance(context).deactivateProfile();
                setProfileActive(null);
            }*/

            EventsHandler eventsHandler = new EventsHandler(context);
            if (manualRestart) {
//                PPApplication.logE("[EVENTS_HANDLER_CALL] DataWrapper._restartEvents", "sensorType=SENSOR_TYPE_MANUAL_RESTART_EVENTS");
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_MANUAL_RESTART_EVENTS);
            } else {
//                PPApplication.logE("[EVENTS_HANDLER_CALL] DataWrapper._restartEvents", "sensorType=SENSOR_TYPE_RESTART_EVENTS");
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RESTART_EVENTS);
            }
//        }
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

                        _restartEvents(unblockEventsRun, notClearActivatedProfile, reactivateProfile, log);
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
        if (alsoRescan) {
            // remove all event delay alarms
            resetAllEventsInDelayStart(false);
            resetAllEventsInDelayEnd(false);
        }

        restartEvents(unblockEventsRun, /*true, true,*/ manualRestart, logRestart/*, false*/);

        if (alsoRescan) {
            // for screenOn=true -> used only for Location scanner - start scan with GPS On
            boolean restart = false;
            if (ApplicationPreferences.applicationEventLocationEnableScanning)
                restart = true;
            else if (ApplicationPreferences.applicationEventWifiEnableScanning)
                restart = true;
            else if (ApplicationPreferences.applicationEventBluetoothEnableScanning)
                restart = true;
            else if (ApplicationPreferences.applicationEventMobileCellEnableScanning) {
//                PPApplication.logE("[TEST BATTERY] DataWrapper._restartEventsWithRescan", "******** ### *******");
                restart = true;
            }
            else if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
//                PPApplication.logE("[TEST BATTERY] DataWrapper._restartEventsWithRescan", "******** ### *******");
                restart = true;
            }
            else if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning)
                restart = true;
            if (restart) {
                PPApplication.rescanAllScanners(context);
            }
        }

        DrawOverAppsPermissionNotification.showNotification(context, false);
        IgnoreBatteryOptimizationNotification.showNotification(context, false);
    }

    void restartEventsWithRescan(final boolean alsoRescan,
            final boolean unblockEventsRun, boolean useHandler,
            final boolean manualRestart, final boolean logRestart, boolean showToast)
    {
        if (useHandler) {
            final DataWrapper dataWrapper = copyDataWrapper();

            final Context appContext = context;
            //PPApplication.startHandlerThread(/*"DataWrapper.restartEventsWithRescan"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
            //__handler.post(new PPHandlerThreadRunnable(
            //        context, dataWrapper, null, null) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=DataWrapper.restartEventsWithRescan");

                //Context appContext= appContextWeakRef.get();
                //DataWrapper dataWrapper = dataWrapperWeakRef.get();
                //Profile profile = profileWeakRef.get();
                //Activity activity = activityWeakRef.get();

                //if ((appContext != null) && (dataWrapper != null) /*&& (profile != null) && (activity != null)*/) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_restartEventsWithRescan");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        dataWrapper._restartEventsWithRescan(alsoRescan, unblockEventsRun, manualRestart, logRestart);

                    } catch (Exception e) {
//                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
            }; //);
            PPApplication.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);
        }
        else
            _restartEventsWithRescan(alsoRescan, unblockEventsRun, manualRestart, logRestart);

        if (showToast && PPApplication.showToastForProfileActivation) {
            if (ApplicationPreferences.notificationsToast) {
                PPApplication.showToast(context.getApplicationContext(),
                        context.getString(R.string.toast_events_restarted),
                        Toast.LENGTH_SHORT);
            }
        }
    }

    void restartEventsWithAlert(final Activity activity)
    {
        if (!Event.getGlobalEventsRunning()) {
            // events are globally stopped

            PPApplication.showToastForProfileActivation = true;

            // show toast about hot working restart events, because global events run is disabled
            PPApplication.showToast(context.getApplicationContext(),
                    context.getString(R.string.toast_restart_events_global_events_run_is_disabled),
                    Toast.LENGTH_SHORT);

            boolean finish;
            if (activity instanceof ActivatorActivity)
                finish = ApplicationPreferences.applicationClose;
            else
                finish = (activity instanceof RestartEventsFromGUIActivity) ||
                        (activity instanceof BackgroundActivateProfileActivity);
            if (finish) {
                final Handler handler = new Handler(context.getMainLooper());
                handler.post(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=DataWrapper.restartEventsWithAlert");
                    try {
                        activity.finish();
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                });
            }

            return;
        }

        /*
        if (!PPApplication.getEventsBlocked(context))
            return;
        */

        PPApplication.setBlockProfileEventActions(false);

        if (ApplicationPreferences.applicationRestartEventsWithAlert || (activity instanceof EditorActivity))
        {
            PPAlertDialog dialog = new PPAlertDialog(
                    activity.getString(R.string.restart_events_alert_title),
                    activity.getString(R.string.restart_events_alert_message),
                    activity.getString(R.string.alert_button_yes),
                    activity.getString(R.string.alert_button_no),
                    null, null,
                    (dialog1, which) -> {
                        boolean finish;
                        if (activity instanceof ActivatorActivity)
                            finish = ApplicationPreferences.applicationClose;
                        else if ((activity instanceof RestartEventsFromGUIActivity) ||
                                (activity instanceof BackgroundActivateProfileActivity))
                            finish = true;
                        else
                            finish = false;
                        if (finish)
                            activity.finish();

                        boolean serviceStarted = GlobalUtils.isServiceRunning(context, PhoneProfilesService.class, false);
                        if (!serviceStarted) {

                            AutostartPermissionNotification.showNotification(context, true);

                            PPApplication.setApplicationStarted(context, true);
                            Intent serviceIntent = new Intent(context, PhoneProfilesService.class);
                            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, false);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                            serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
//                    PPApplication.logE("[START_PP_SERVICE] DataWrapper.restartEventsWithAlert", "xxx");
                            PPApplication.startPPService(context, serviceIntent);
                        } else {
                            if (!ApplicationPreferences.applicationApplicationProfileActivationNotificationSound.isEmpty() || ApplicationPreferences.applicationApplicationProfileActivationNotificationVibrate) {
                                if (PhoneProfilesService.getInstance() != null) {
                                    PhoneProfilesService.getInstance().playNotificationSound(
                                            ApplicationPreferences.applicationApplicationProfileActivationNotificationSound,
                                            ApplicationPreferences.applicationApplicationProfileActivationNotificationVibrate,
                                    false);
                                    //PPApplication.sleep(500);
                                }
                            }

                            restartEventsWithRescan(true, true, true, true, true, true);
                            //IgnoreBatteryOptimizationNotification.showNotification(context);
                        }
                    },
                    (dialogInterface, i) -> {
                        boolean finish = (!(activity instanceof ActivatorActivity)) &&
                                (!(activity instanceof EditorActivity));

                        if (finish)
                            activity.finish();
                    },
                    null,
                    dialogInterface -> {
                        boolean finish = (!(activity instanceof ActivatorActivity)) &&
                                (!(activity instanceof EditorActivity));

                        if (finish)
                            activity.finish();
                    },
                    null,
                    true, true,
                    false, false,
                    true,
                    activity
            );

            if (!activity.isFinishing())
                dialog.show();
        }
        else
        {
            boolean finish;
            if (activity instanceof ActivatorActivity)
                finish = ApplicationPreferences.applicationClose;
            else
                finish = (activity instanceof RestartEventsFromGUIActivity) ||
                        (activity instanceof BackgroundActivateProfileActivity);
            if (finish) {
                final Handler handler = new Handler(context.getMainLooper());
                handler.post(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=DataWrapper.restartEventsWithAlert");
                    try {
                        activity.finish();
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                });
            }

            if (!ApplicationPreferences.applicationApplicationProfileActivationNotificationSound.isEmpty() || ApplicationPreferences.applicationApplicationProfileActivationNotificationVibrate) {
                if (PhoneProfilesService.getInstance() != null) {
                    PhoneProfilesService.getInstance().playNotificationSound(
                            ApplicationPreferences.applicationApplicationProfileActivationNotificationSound,
                            ApplicationPreferences.applicationApplicationProfileActivationNotificationVibrate,
                            false);
                    //PPApplication.sleep(500);
                }
            }

            restartEventsWithRescan(true, true, true, true, true, true);

            //IgnoreBatteryOptimizationNotification.showNotification(context);
        }
    }

    // delay is in seconds
    void restartEventsWithDelay(final boolean longDelay, final boolean alsoRescan, final boolean unblockEventsRun,
                                final int logType)
    {
        if (longDelay) {

            Data workData = new Data.Builder()
                    .putBoolean(PhoneProfilesService.EXTRA_ALSO_RESCAN, alsoRescan)
                    .putBoolean(PhoneProfilesService.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun)
                    .putInt(PhoneProfilesService.EXTRA_LOG_TYPE, logType)
                    .build();

            OneTimeWorkRequest restartEventsWithDelayWorker;
            restartEventsWithDelayWorker =
                    new OneTimeWorkRequest.Builder(RestartEventsWithDelayWorker.class)
                            .addTag(RestartEventsWithDelayWorker.WORK_TAG_2)
                            .setInputData(workData)
                            .setInitialDelay(15, TimeUnit.SECONDS)
                            .build();
            try {
                if (PPApplication.getApplicationStarted(true, true)) {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null) {

//                              //if (PPApplication.logEnabled()) {
//                              ListenableFuture<List<WorkInfo>> statuses;
//                              statuses = workManager.getWorkInfosForUniqueWork(RestartEventsWithDelayWorker.WORK_TAG);
//                              try {
//                                  List<WorkInfo> workInfoList = statuses.get();
//                              } catch (Exception ignored) {
//                              }
//                              //}

//                         PPApplication.logE("[WORKER_CALL] DataWrapper.restartEventsWithDelay", "xxx");
                        //workManager.enqueue(restartEventsWithDelayWorker);
                        //if (replace)
                        workManager.enqueueUniqueWork(RestartEventsWithDelayWorker.WORK_TAG_2, ExistingWorkPolicy.REPLACE, restartEventsWithDelayWorker);
                        //else
                        //    workManager.enqueueUniqueWork(RestartEventsWithDelayWorker.WORK_TAG_APPEND, ExistingWorkPolicy.APPEND_OR_REPLACE, restartEventsWithDelayWorker);
                    }
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } else {
//            PPApplication.logE("[EXECUTOR_CALL]  ***** DataWrapper.restartEventsWithDelay", "schedule");

            final Context appContext = context.getApplicationContext();
            //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
            Runnable runnable = () -> {
//                long start = System.currentTimeMillis();
//                PPApplication.logE("[IN_EXECUTOR]  ***** DataWrapper.restartEventsWithDelay", "--------------- START");

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":DataWrapper_restartEventsWithDelay");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    //PPExecutors.doRestartEventsWithDelay(alsoRescan, unblockEventsRun, logType, context);

                    DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                    if (logType != PPApplication.ALTYPE_UNDEFINED)
                        PPApplication.addActivityLog(appContext, logType, null, null, "");
                    //dataWrapper.restartEvents(unblockEventsRun, true, true, false);
                    dataWrapper.restartEventsWithRescan(alsoRescan, unblockEventsRun, false, false, true, false);
                    //dataWrapper.invalidateDataWrapper();


//                    long finish = System.currentTimeMillis();
//                    long timeElapsed = finish - start;
//                    PPApplication.logE("[IN_EXECUTOR]  ***** DataWrapper.restartEventsWithDelay", "--------------- END - timeElapsed="+timeElapsed);
                } catch (Exception e) {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                    //worker.shutdown();
                }
            };
            PPApplication.createDelayedEventsHandlerExecutor();
            PPApplication.delayedEventsHandlerExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
        }
    }

    void setEventBlocked(Event event, boolean blocked)
    {
        event._blocked = blocked;
        DatabaseHandler.getInstance(context).updateEventBlocked(event);
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
            PPAlertDialog dialog = new PPAlertDialog(
                    activity.getString(R.string.menu_stop_events),
                    activity.getString(R.string.stop_events_alert_message),
                    activity.getString(R.string.alert_button_yes),
                    activity.getString(R.string.alert_button_no),
                    null, null,
                    (dialog1, which) -> {
                        if (globalRunStopEvents(true)) {
                            //PPPAppNotification.showNotification(/*activity.getApplicationContext()*/true, false);

//                    PPApplication.logE("[PPP_NOTIFICATION] DataWrapper.runStopEventsWithAlert (1)", "call of updateGUI");
                            PPApplication.updateGUI(true, false, activity);
                        }
                    },
                    (dialog2, which) -> {
                        if (checkBox != null)
                            checkBox.setChecked(true);
                    },
                    null,
                    null,
                    null,
                    true, true,
                    false, false,
                    true,
                    activity
            );

            if (!activity.isFinishing())
                dialog.show();
        }
        else {
            if (globalRunStopEvents(false)) {
//                PPApplication.logE("[PPP_NOTIFICATION] DataWrapper.runStopEventsWithAlert (2)", "call of updateGUI");
                PPApplication.updateGUI(true, false, activity);
            }
        }
    }

    boolean globalRunStopEvents(boolean stop) {
        if (stop) {
            if (Event.getGlobalEventsRunning()) {
                PPApplication.addActivityLog(context, PPApplication.ALTYPE_RUN_EVENTS_DISABLE, null, null, "");

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
                PPApplication.addActivityLog(context, PPApplication.ALTYPE_RUN_EVENTS_ENABLE, null, null, "");

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
            _event._eventPreferencesSMS._fromSIMSlot = 0;
            //if ((_event != null) && (_event._name != null) && (_event._name.equals("SMS event")))
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

        if (force) {
            _event._eventPreferencesPeriodic._startTime = 0;
            DatabaseHandler.getInstance(context.getApplicationContext()).updatePeriodicStartTime(_event);
            _event._eventPreferencesPeriodic.removeAlarm(context);
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
                        case DatabaseHandler.ETYPE_BATTERY_WITH_LEVEL:
                            sensorEnabled = _event._eventPreferencesBattery._enabled;
                            if (sensorEnabled) {
                                sensorEnabled =
                                        (_event._eventPreferencesBattery._levelLow > 0) ||
                                                (_event._eventPreferencesBattery._levelHight < 100);
                            }
                            break;
                        case DatabaseHandler.ETYPE_CALL:
                            sensorEnabled = _event._eventPreferencesCall._enabled;
                            break;
                        case DatabaseHandler.ETYPE_ACCESSORY:
                            sensorEnabled = _event._eventPreferencesAccessories._enabled;
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
                        case DatabaseHandler.ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS:
                            sensorEnabled = _event._eventPreferencesRadioSwitch._enabled;
                            sensorEnabled = sensorEnabled &&
                                    (_event._eventPreferencesRadioSwitch._defaultSIMForCalls != 0);
                            break;
                        case DatabaseHandler.ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS:
                            sensorEnabled = _event._eventPreferencesRadioSwitch._enabled;
                            sensorEnabled = sensorEnabled &&
                                    (_event._eventPreferencesRadioSwitch._defaultSIMForSMS != 0);
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
                        case DatabaseHandler.ETYPE_RADIO_SWITCH_SIM_ON_OFF:
                            sensorEnabled = _event._eventPreferencesRadioSwitch._enabled;
                            sensorEnabled = sensorEnabled &&
                                    (_event._eventPreferencesRadioSwitch._simOnOff != 0);
                            break;
                        case DatabaseHandler.ETYPE_ALARM_CLOCK:
                            sensorEnabled = _event._eventPreferencesAlarmClock._enabled;
                            break;
                        case DatabaseHandler.ETYPE_TIME_TWILIGHT:
                            sensorEnabled = _event._eventPreferencesTime._enabled;
                            sensorEnabled = sensorEnabled &&
                                    (_event._eventPreferencesTime._timeType != EventPreferencesTime.TIME_TYPE_EXACT);
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
                        case DatabaseHandler.ETYPE_SOUND_PROFILE:
                            sensorEnabled = _event._eventPreferencesSoundProfile._enabled;
                            break;
                        case DatabaseHandler.ETYPE_PERIODIC:
                            sensorEnabled = _event._eventPreferencesPeriodic._enabled;
                            break;
                        case DatabaseHandler.ETYPE_VOLUMES:
                            sensorEnabled = _event._eventPreferencesVolumes._enabled;
                            break;
                        case DatabaseHandler.ETYPE_ACTIVATED_PROFILE:
                            sensorEnabled = _event._eventPreferencesActivatedProfile._enabled;
                            break;
                        case DatabaseHandler.ETYPE_ROAMING:
                            sensorEnabled = _event._eventPreferencesRoaming._enabled;
                            break;
                        case DatabaseHandler.ETYPE_VPN:
                            sensorEnabled = _event._eventPreferencesVPN._enabled;
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

    List<String> fifoGetActivatedProfiles() {
        //synchronized (PPApplication.profileActivationMutex) {
            SharedPreferences preferences = context.getSharedPreferences(PPApplication.ACTIVATED_PROFILES_FIFO_PREFS_NAME, Context.MODE_PRIVATE);
            int count = preferences.getInt(ACTIVATED_PROFILES_FIFO_COUNT_PREF, -1);

            if (count > -1) {
                List<String> activateProfilesFifo = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    String profileId = preferences.getString(ACTIVATED_PROFILES_FIFO_ID_PREF + i, "0|0");
                    activateProfilesFifo.add(profileId);
                }
                return activateProfilesFifo;
            } else
                return null;
        //}
    }

    void fifoSaveProfiles(List<String> activateProfilesFifo)
    {
        //synchronized (PPApplication.profileActivationMutex) {
            SharedPreferences preferences = context.getSharedPreferences(PPApplication.ACTIVATED_PROFILES_FIFO_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            if (activateProfilesFifo == null)
                editor.putInt(ACTIVATED_PROFILES_FIFO_COUNT_PREF, -1);
            else {
                editor.putInt(ACTIVATED_PROFILES_FIFO_COUNT_PREF, activateProfilesFifo.size());

                for (int i = 0; i < activateProfilesFifo.size(); i++) {
                    editor.putString(ACTIVATED_PROFILES_FIFO_ID_PREF + i, activateProfilesFifo.get(i));
                }
            }

            editor.apply();
        //}
    }

    void fifoAddProfile(long profileId, long eventId) {
        if (profileId == Profile.PROFILE_NO_ACTIVATE)
            return;

        synchronized (PPApplication.profileActivationMutex) {
            List<String> activateProfilesFIFO = fifoGetActivatedProfiles();
            if (activateProfilesFIFO == null)
                activateProfilesFIFO = new ArrayList<>();
            int size = activateProfilesFIFO.size();
            if (size > PPApplication.ACTIVATED_PROFILES_FIFO_SIZE) {
                activateProfilesFIFO.remove(0);
                size--;
            }
            String toFifo = profileId + "|" + eventId;
            if ((size == 0) || (!activateProfilesFIFO.get(size - 1).equals(toFifo)))
                activateProfilesFIFO.add(toFifo);
            fifoSaveProfiles(activateProfilesFIFO);
        }
    }

/*    private static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<DataWrapper> dataWrapperWeakRef;
        final WeakReference<Profile> profileWeakRef;
        final WeakReference<Activity> activityWeakRef;

        PPHandlerThreadRunnable(Context appContext,
                                       DataWrapper dataWrapper,
                                       Profile profile,
                                       Activity activity) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.dataWrapperWeakRef = new WeakReference<>(dataWrapper);
            this.profileWeakRef = new WeakReference<>(profile);
            this.activityWeakRef = new WeakReference<>(activity);
        }

    }*/

}
