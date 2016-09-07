package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.os.Bundle;

public class ProfilePreferencesFragment extends ProfilePreferencesNestedFragment
{
    //private DataWrapper dataWrapper;
    //private Profile profile;
    //private boolean first_start_activity;
    //private int new_profile_mode;
    //private int predefineProfileIndex;
    public static int startupSource;
    //private PreferenceManager prefMng;
    //private SharedPreferences preferences;
    //private Context context;

    public static ImageViewPreference changedImageViewPreference;
    public static ProfileIconPreference changedProfileIconPreference;
    public static Activity preferencesActivity = null;
    public static ApplicationsDialogPreference applicationsDialogPreference;

    static final String PREFS_NAME_ACTIVITY = "profile_preferences_activity";
    static final String PREFS_NAME_FRAGMENT = "profile_preferences_fragment";
    static final String PREFS_NAME_DEFAULT_PROFILE = GlobalData.DEFAULT_PROFILE_PREFS_NAME;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);

        preferencesActivity = getActivity();
        //context = getActivity().getApplicationContext();

        /*
        dataWrapper = new DataWrapper(context, true, false, 0);

        long profile_id = 0;

        // getting attached fragment data
        if (getArguments().containsKey(GlobalData.EXTRA_NEW_PROFILE_MODE))
            new_profile_mode = getArguments().getInt(GlobalData.EXTRA_NEW_PROFILE_MODE);
        if (getArguments().containsKey(GlobalData.EXTRA_PROFILE_ID))
            profile_id = getArguments().getLong(GlobalData.EXTRA_PROFILE_ID);
        predefineProfileIndex = getArguments().getInt(GlobalData.EXTRA_PREDEFINED_PROFILE_INDEX);

        profile = ProfilePreferencesFragmentActivity.createProfile(context.getApplicationContext(), profile_id, new_profile_mode, predefineProfileIndex, true);
        */

        prefMng = getPreferenceManager();
        preferences = prefMng.getSharedPreferences();


        /*if (first_start_activity)*/
        //if (savedInstanceState == null)
        //    loadPreferences();

        updateSharedPreference();

    }

    @Override
    public void addPreferencesFromResource(int preferenceResId) {
        String PREFS_NAME;
        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        else
        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
            PREFS_NAME = PREFS_NAME_FRAGMENT;
        else
        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            PREFS_NAME = PREFS_NAME_DEFAULT_PROFILE;
        else
            PREFS_NAME = PREFS_NAME_FRAGMENT;

        prefMng = getPreferenceManager();

        prefMng.setSharedPreferencesName(PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        super.addPreferencesFromResource(preferenceResId);
    }

    @Override
    public int addPreferencesFromResource() {
        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            return R.xml.default_profile_preferences;
        else
            return R.xml.profile_preferences;
    }

    private void updateSharedPreference()
    {
        //if (profile != null)
        //{

            // updating activity with selected profile preferences

            setSummary(GlobalData.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);

            if (startupSource != GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            {
                setSummary(GlobalData.PREF_PROFILE_NAME);
                setSummary(GlobalData.PREF_PROFILE_DURATION);
                setSummary(GlobalData.PREF_PROFILE_AFTER_DURATION_DO);
                setSummary(GlobalData.PREF_PROFILE_ASK_FOR_DURATION);
            }
            setSummary(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
            setSummary(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
            setSummary(GlobalData.PREF_PROFILE_SOUND_RINGTONE);
            setSummary(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
            setSummary(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION);
            setSummary(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE);
            setSummary(GlobalData.PREF_PROFILE_SOUND_ALARM);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_WIFI);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_GPS);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_NFC);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_RINGTONE);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_MEDIA);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_ALARM);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_SYSTEM);
            setSummary(GlobalData.PREF_PROFILE_VOLUME_VOICE);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS);
            setSummary(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE);
            setSummary(GlobalData.PREF_PROFILE_NOTIFICATION_LED);
            setSummary(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            setSummary(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_FOR);

            // disable depended preferences
            disableDependedPref(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
            disableDependedPref(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
            disableDependedPref(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE);
            disableDependedPref(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
            disableDependedPref(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
            disableDependedPref(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP);
            disableDependedPref(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE);
            disableDependedPref(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);

        //}
    }

    static public Activity getPreferencesActivity()
    {
        return preferencesActivity;
    }

    static public void setChangedImageViewPreference(ImageViewPreference changedImageViewPref)
    {
        changedImageViewPreference = changedImageViewPref;
    }

    static public void setChangedProfileIconPreference(ProfileIconPreference changedProfileIconPref)
    {
        changedProfileIconPreference = changedProfileIconPref;
    }

    static public void setApplicationsDialogPreference(ApplicationsDialogPreference applicationsDialogPref)
    {
        applicationsDialogPreference = applicationsDialogPref;
    }

}
