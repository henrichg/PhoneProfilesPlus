package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsWidgetPanel extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)) {
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_widget_panel, rootKey);
    }

    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_VERTICAL_POSITION, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_VERTICAL_POSITION, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_VERTICAL_POSITION_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_HEADER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_HEADER, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_HEADER_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_CHANGE_COLOR_BY_NIGHT_MODE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_CHANGE_COLOR_BY_NIGHT_MODE, ApplicationPreferences.applicationWidgetPanelChangeColorsByNightModeDefaultValue()));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND, ApplicationPreferences.applicationWidgetPanelBackgroundDefaultValue()));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_TYPE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_TYPE, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_TYPE_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_B, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_B, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_B_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_COLOR_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_LIGHTNESS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_OFF, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_OFF, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_OFF_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_ON, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_ON, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T_CHANGE_BY_NIGHT_MODE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T_CHANGE_BY_NIGHT_MODE, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T_CHANGE_BY_NIGHT_MODE_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_SHOW_BORDER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_SHOW_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_SHOW_BORDER_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_BORDER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_BORDER_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ROUNDED_CORNERS_RADIUS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ROUNDED_CORNERS_RADIUS, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ROUNDED_CORNERS_RADIUS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_USE_DYNAMIC_COLORS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_USE_DYNAMIC_COLORS,ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_USE_DYNAMIC_COLORS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_BORDER_CHANGE_BY_NIGHT_MODE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_BORDER_CHANGE_BY_NIGHT_MODE, ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_BORDER_CHANGE_BY_NIGHT_MODE_DEFAULT_VALUE));
    }

}
