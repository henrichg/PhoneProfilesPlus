package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsMobileCellsScanning  extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)) {
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_mobile_cells_scanning, rootKey);
    }

    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE));
        editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_FROM, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_FROM, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE));
        editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_TO, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_TO, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE));
    }

}
