package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsSamsungEdgePanel  extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)) {
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_samsung_edge_panel, rootKey);
    }

    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_HEADER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_HEADER, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_HEADER_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE, ApplicationPreferences.applicationSamsungEdgeChangeColorsByNightModeDefaultValue()));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, ApplicationPreferences.applicationSamsungEdgeBackgroundDefaultValue()));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_NIGHT_MODE_OFF, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_NIGHT_MODE_OFF, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_NIGHT_MODE_OFF_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_NIGHT_MODE_ON, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_NIGHT_MODE_ON, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE));
    }

}
