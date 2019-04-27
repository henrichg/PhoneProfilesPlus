package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;

public class ProfilePreferencesFragment extends ProfilePreferencesNestedFragment
{
    //public static WallpaperViewPreference changedWallpaperViewPreference;
    //public static ProfileIconPreference changedProfileIconPreference;
    //public static ApplicationsDialogPreference applicationsDialogPreference;

    //static final String PREFS_NAME_ACTIVITY = "profile_preferences_activity";
    //static final String PREFS_NAME_FRAGMENT = "profile_preferences_fragment";
    //static final String PREFS_NAME_SHARED_PROFILE = PPApplication.SHARED_PROFILE_PREFS_NAME;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Log.d("------ ProfilePreferencesFragment.onCreate", "this="+this);
        //Log.d("------ ProfilePreferencesFragment.onCreate", "startupSource="+startupSource);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);
        // this is really important in order to save the state across screen
        // configuration changes for example
        //setRetainInstance(true);

        updateSharedPreference();

    }

    @Override
    public void addPreferencesFromResource(int preferenceResId) {
        setPreferencesManager();
        super.addPreferencesFromResource(preferenceResId);
    }

    @Override
    public int addPreferencesFromResource() {
        /*Bundle bundle = this.getArguments();
        if (bundle != null)
            startupSource = bundle.getInt(PPApplication.EXTRA_STARTUP_SOURCE, 0);*/

        //Log.d("------ ProfilePreferencesFragment.addPreferencesFromResource", "startupSource="+startupSource);
        /*if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE)
            return R.xml.default_profile_preferences;
        else*/
            return R.xml.profile_preferences;
    }

    private void updateSharedPreference()
    {
        //if (profile != null)
        //{

        // updating activity with selected profile preferences

        // disable depended preferences
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_RINGTONE);
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);

        setSummary(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);

        //if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE)
        //{
            setSummary(Profile.PREF_PROFILE_NAME);
            setSummary(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR);
            setSummary(Profile.PREF_PROFILE_DURATION);
            setSummary(Profile.PREF_PROFILE_AFTER_DURATION_DO);
            setSummary(Profile.PREF_PROFILE_ASK_FOR_DURATION);
            setSummary(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND);
            setSummary(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE);
            setSummary(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON);
        //}
        setSummary(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);
        setSummary(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
        setSummary(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE);
        setSummary(Profile.PREF_PROFILE_SOUND_ALARM);
        setSummary(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI);
        setSummary(Profile.PREF_PROFILE_DEVICE_BLUETOOTH);
        setSummary(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT);
        setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA);
        setSummary(Profile.PREF_PROFILE_DEVICE_GPS);
        setSummary(Profile.PREF_PROFILE_DEVICE_AUTOSYNC);
        setSummary(Profile.PREF_PROFILE_DEVICE_AUTOROTATE);
        setSummary(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS);
        setSummary(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
        setSummary(ProfilePreferencesNestedFragment.PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
        setSummary(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS);
        setSummary(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE);
        setSummary(Profile.PREF_PROFILE_DEVICE_NFC);
        setSummary(Profile.PREF_PROFILE_DEVICE_KEYGUARD);
        setSummary(Profile.PREF_PROFILE_VOLUME_RINGTONE);
        setSummary(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
        setSummary(Profile.PREF_PROFILE_VOLUME_MEDIA);
        setSummary(Profile.PREF_PROFILE_VOLUME_ALARM);
        setSummary(Profile.PREF_PROFILE_VOLUME_SYSTEM);
        setSummary(Profile.PREF_PROFILE_VOLUME_VOICE);
        setSummary(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS);
        setSummary(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
        setSummary(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE);
        setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE);
        setSummary(Profile.PREF_PROFILE_NOTIFICATION_LED);
        setSummary(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
        setSummary(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
        setSummary(Profile.PREF_PROFILE_LOCK_DEVICE);
        setSummary(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING);
        setSummary(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS);
        setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS);
        setSummary(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS);
        setSummary(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE);
        setSummary(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING);
        setSummary(Profile.PREF_PROFILE_SOUND_ON_TOUCH);
        setSummary(ProfilePreferencesNestedFragment.PREF_LOCK_DEVICE_INSTALL_EXTENDER);

        //}
    }

    /*
    static public void setChangedWallpaperViewPreference(WallpaperViewPreference changedImageViewPref)
    {
        changedWallpaperViewPreference = changedImageViewPref;
    }
    */

    /*
    static public void setChangedProfileIconPreference(ProfileIconPreference changedProfileIconPref)
    {
        changedProfileIconPreference = changedProfileIconPref;
    }
    */

    /*
    static public void setApplicationsDialogPreference(ApplicationsDialogPreference applicationsDialogPref)
    {
        applicationsDialogPreference = applicationsDialogPref;
    }
    */

}
