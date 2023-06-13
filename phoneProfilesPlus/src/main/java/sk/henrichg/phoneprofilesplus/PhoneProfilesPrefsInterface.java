package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsInterface  extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)){
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_interface, rootKey);
    }

    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_THEME, ApplicationPreferences.applicationThemeDefaultValue()));
        //editor.putString(ApplicationPreferences.PREF_APPLICATION_HOME_LAUNCHER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_HOME_LAUNCHER, ApplicationPreferences.PREF_APPLICATION_HOME_LAUNCHER_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER, ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER, ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ICON_COLOR, ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ICON_COLOR_DEFAULT_VALUE));
        //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_INCREASE_BRIGHTNESS_FOR_PROFILE_ICON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_INCREASE_BRIGHTNESS_FOR_PROFILE_ICON, ApplicationPreferences.PREF_APPLICATION_INCREASE_BRIGHTNESS_FOR_PROFILE_ICON_DEFAULT_VALUE));
    }

}
