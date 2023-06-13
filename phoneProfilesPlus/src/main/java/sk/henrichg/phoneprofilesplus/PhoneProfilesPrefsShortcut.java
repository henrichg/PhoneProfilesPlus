package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsShortcut  extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)) {
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_shortcut, rootKey);
    }

    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR, ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE));
    }

}
