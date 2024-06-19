package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsAppNotification  extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)) {
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_app_notification, rootKey);
    }

    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putString(ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER, ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE, ApplicationPreferences.notificationStatusBarStyleDefaultValue()));
        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_NOTIFICATION_STYLE, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_NOTIFICATION_STYLE, ApplicationPreferences.notificationNotificationStyleDefaultValue()));
        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE, ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, ApplicationPreferences.notificationUseDecorationDefaultValue()));
        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR_DEFAULT_VALUE));
        editor.putInt(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, fromPreference.getInt(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR, ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON, ApplicationPreferences.notificationShowProfileIconDefaultValue()));
        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS, ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR, ApplicationPreferences.notificationPrefIndicatorDefaultValue()));
        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS, ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON, ApplicationPreferences.notificationShowRestartEventsAsButtonDefaultValue()));
        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT, ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT_DEFAULT_VALUE));
    }

}
