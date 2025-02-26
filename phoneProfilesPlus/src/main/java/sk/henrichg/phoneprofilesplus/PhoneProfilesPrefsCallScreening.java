package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsCallScreening extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)) {
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_call_screening, rootKey);
    }

    /*
    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT, ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT_DEFAULT_VALUE));
    }
    */

}
