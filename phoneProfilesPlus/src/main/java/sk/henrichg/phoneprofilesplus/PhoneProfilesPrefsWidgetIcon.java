package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsWidgetIcon  extends PhoneProfilesPrefsFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        PreferenceManager prefMng = getPreferenceManager();
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if ((getContext() != null) && (preferences != null)) {
            SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
            loadSharedPreferences(preferences, applicationPreferences);
        }

        setPreferencesFromResource(R.xml.phone_profiles_prefs_widget_icon, rootKey);
    }

    @Override
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LAUNCHER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LAUNCHER, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LAUNCHER_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LAYOUT_HEIGHT, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LAYOUT_HEIGHT, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LAYOUT_HEIGHT_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_FILL_BACKGROUND, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_FILL_BACKGROUND, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_FILL_BACKGROUND_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE, ApplicationPreferences.applicationWidgetIconChangeColorsByNightModeDefaultValue(/*getContext()*/)));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND, ApplicationPreferences.applicationWidgetIconBackgroundDefaultValue(/*getContext()*/)));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER_DEFAULT_VALUE));
        //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE));
        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS,ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_OFF, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_OFF, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_OFF_DEFAULT_VALUE));
        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_ON, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_ON, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE));
    }

}
