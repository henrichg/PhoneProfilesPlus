package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsProfileListNotification  extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)) {
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_profile_list_notification, rootKey);
    }

    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_DISPLAY_NOTIFICATION, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_DISPLAY_NOTIFICATION, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_DISPLAY_NOTIFICATION_DEFAULT_VALUE));
        //editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_SHOW_IN_STATUS_BAR, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_SHOW_IN_STATUS_BAR, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_SHOW_IN_STATUS_BAR_DEFAULT_VALUE));
        //editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_HIDE_IN_LOCKSCREEN, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_HIDE_IN_LOCKSCREEN, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_HIDE_IN_LOCKSCREEN_DEFAULT_VALUE));
        //editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_STATUS_BAR_STYLE, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_STATUS_BAR_STYLE, ApplicationPreferences.notificationProfileListStatusBarStyleDefaultValue()));
        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_COLOR, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_COLOR_DEFAULT_VALUE));
        editor.putInt(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_CUSTOM_COLOR, fromPreference.getInt(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_CUSTOM_COLOR, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_CUSTOM_COLOR_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ARROWS_MARK_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ARROWS_MARK_LIGHTNESS, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ARROWS_MARK_LIGHTNESS_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_NUMBER_OF_PROFILES_PER_PAGE, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_NUMBER_OF_PROFILES_PER_PAGE, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_NUMBER_OF_PROFILES_PER_PAGE_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_COLOR, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_COLOR_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_LIGHTNESS, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_LIGHTNESS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE));
    }

}
