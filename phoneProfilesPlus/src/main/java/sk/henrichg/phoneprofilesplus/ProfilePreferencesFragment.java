package sk.henrichg.phoneprofilesplus;

import android.app.Fragment;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

public class ProfilePreferencesFragment extends ProfilePreferencesNestedFragment
{
    public static ImageViewPreference changedImageViewPreference;
    public static ProfileIconPreference changedProfileIconPreference;
    public static ApplicationsDialogPreference applicationsDialogPreference;

    static final String PREFS_NAME_ACTIVITY = "profile_preferences_activity";
    static final String PREFS_NAME_FRAGMENT = "profile_preferences_fragment";
    static final String PREFS_NAME_DEFAULT_PROFILE = PPApplication.DEFAULT_PROFILE_PREFS_NAME;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);

        //context = getActivity().getApplicationContext();

        /*
        dataWrapper = new DataWrapper(context, true, false, 0);

        long profile_id = 0;

        // getting attached fragment data
        if (getArguments().containsKey(PPApplication.EXTRA_NEW_PROFILE_MODE))
            new_profile_mode = getArguments().getInt(PPApplication.EXTRA_NEW_PROFILE_MODE);
        if (getArguments().containsKey(PPApplication.EXTRA_PROFILE_ID))
            profile_id = getArguments().getLong(PPApplication.EXTRA_PROFILE_ID);
        predefineProfileIndex = getArguments().getInt(PPApplication.EXTRA_PREDEFINED_PROFILE_INDEX);

        profile = ProfilePreferencesFragmentActivity.createProfile(context.getApplicationContext(), profile_id, new_profile_mode, predefineProfileIndex, true);
        */

        /*if (first_start_activity)*/
        //if (savedInstanceState == null)
        //    loadPreferences();

        updateSharedPreference();

    }

    @Override
    public void addPreferencesFromResource(int preferenceResId) {
        setPreferencesManager();
        super.addPreferencesFromResource(preferenceResId);
    }

    @Override
    public int addPreferencesFromResource() {
        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            return R.xml.default_profile_preferences;
        else
            return R.xml.profile_preferences;
    }

    private void updateSharedPreference()
    {
        //if (profile != null)
        //{

            // updating activity with selected profile preferences

            setSummary(PPApplication.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);

            if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            {
                setSummary(PPApplication.PREF_PROFILE_NAME);
                setSummary(PPApplication.PREF_PROFILE_DURATION);
                setSummary(PPApplication.PREF_PROFILE_AFTER_DURATION_DO);
                setSummary(PPApplication.PREF_PROFILE_ASK_FOR_DURATION);
            }
            setSummary(PPApplication.PREF_PROFILE_VOLUME_RINGER_MODE);
            setSummary(PPApplication.PREF_PROFILE_VOLUME_ZEN_MODE);
            setSummary(PPApplication.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
            setSummary(PPApplication.PREF_PROFILE_SOUND_RINGTONE);
            setSummary(PPApplication.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
            setSummary(PPApplication.PREF_PROFILE_SOUND_NOTIFICATION);
            setSummary(PPApplication.PREF_PROFILE_SOUND_ALARM_CHANGE);
            setSummary(PPApplication.PREF_PROFILE_SOUND_ALARM);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_WIFI);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_BLUETOOTH);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_MOBILE_DATA);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_GPS);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_AUTOSYNC);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_AUTOROTATE);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS);
            setSummary(PPApplication.PREF_PROFILE_VOLUME_SPEAKER_PHONE);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_NFC);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_KEYGUARD);
            setSummary(PPApplication.PREF_PROFILE_VOLUME_RINGTONE);
            setSummary(PPApplication.PREF_PROFILE_VOLUME_NOTIFICATION);
            setSummary(PPApplication.PREF_PROFILE_VOLUME_MEDIA);
            setSummary(PPApplication.PREF_PROFILE_VOLUME_ALARM);
            setSummary(PPApplication.PREF_PROFILE_VOLUME_SYSTEM);
            setSummary(PPApplication.PREF_PROFILE_VOLUME_VOICE);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_BRIGHTNESS);
            setSummary(PPApplication.PREF_PROFILE_VIBRATION_ON_TOUCH);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_WIFI_AP);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_POWER_SAVE_MODE);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_NETWORK_TYPE);
            setSummary(PPApplication.PREF_PROFILE_NOTIFICATION_LED);
            setSummary(PPApplication.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            setSummary(PPApplication.PREF_PROFILE_DEVICE_WALLPAPER_FOR);

            // disable depended preferences
            disableDependedPref(PPApplication.PREF_PROFILE_VOLUME_RINGTONE);
            disableDependedPref(PPApplication.PREF_PROFILE_VOLUME_NOTIFICATION);
            disableDependedPref(PPApplication.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
            disableDependedPref(PPApplication.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
            disableDependedPref(PPApplication.PREF_PROFILE_SOUND_ALARM_CHANGE);
            disableDependedPref(PPApplication.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
            disableDependedPref(PPApplication.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
            disableDependedPref(PPApplication.PREF_PROFILE_DEVICE_WIFI_AP);
            disableDependedPref(PPApplication.PREF_PROFILE_VOLUME_RINGER_MODE);
            disableDependedPref(PPApplication.PREF_PROFILE_VOLUME_ZEN_MODE);

        //}
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
