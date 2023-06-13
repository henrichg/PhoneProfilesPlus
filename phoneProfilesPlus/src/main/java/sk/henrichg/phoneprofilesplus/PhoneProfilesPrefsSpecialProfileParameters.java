package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsSpecialProfileParameters  extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)) {
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_special_profile_parameters, rootKey);
    }

    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON, ApplicationPreferences.PREF_APPLICATION_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON_DEFAULT_VALUE));
    }

}
