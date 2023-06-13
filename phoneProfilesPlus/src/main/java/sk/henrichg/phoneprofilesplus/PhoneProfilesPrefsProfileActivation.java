package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsProfileActivation  extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)) {
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_profile_activation, rootKey);
    }

    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putString(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE, ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND, ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE, ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ALERT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ALERT, ApplicationPreferences.PREF_APPLICATION_ALERT_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND, ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_VIBRATE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_VIBRATE, ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_VIBRATE_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_TOAST, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_TOAST, ApplicationPreferences.PREF_NOTIFICATION_TOAST_DEFAULT_VALUE));
    }

}
