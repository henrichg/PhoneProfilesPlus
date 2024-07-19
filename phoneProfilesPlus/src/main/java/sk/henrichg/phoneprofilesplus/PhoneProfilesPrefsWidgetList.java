package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsWidgetList  extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)) {
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_widget_list, rootKey);
    }

    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LAUNCHER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LAUNCHER, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LAUNCHER_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_HEADER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_HEADER, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_HEADER_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_COMPACT_GRID, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_COMPACT_GRID, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_COMPACT_GRID_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE, ApplicationPreferences.applicationWidgetListChangeColorsByNightModeDefaultValue(getContext())));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND, ApplicationPreferences.applicationWidgetListBackgroundDefaultValue(getContext())));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER_DEFAULT_VALUE));
        //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS,ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE));
    }

}
