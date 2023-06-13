package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsApplicationStart  extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)) {
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_application_start, rootKey);
    }

    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT, ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATE, ApplicationPreferences.PREF_APPLICATION_ACTIVATE_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_START_EVENTS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_START_EVENTS, ApplicationPreferences.PREF_APPLICATION_START_EVENTS_DEFAULT_VALUE));
    }

}
