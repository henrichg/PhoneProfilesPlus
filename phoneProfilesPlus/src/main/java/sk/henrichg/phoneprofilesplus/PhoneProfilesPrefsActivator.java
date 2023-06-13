package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsActivator  extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)) {
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_activator, rootKey);
    }

    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION, ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_CLOSE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_CLOSE, ApplicationPreferences.PREF_APPLICATION_CLOSE_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_NUM_COLUMNS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_NUM_COLUMNS, ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_NUM_COLUMNS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_ADD_RESTART_EVENTS_INTO_PROFILE_LIST, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_ADD_RESTART_EVENTS_INTO_PROFILE_LIST, ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_ADD_RESTART_EVENTS_INTO_PROFILE_LIST_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_INCREASE_BRIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_INCREASE_BRIGHTNESS, ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_INCREASE_BRIGHTNESS_DEFAULT_VALUE));
    }

}
