package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

public class PhoneProfilesPreferencesFragment extends PhoneProfilesPreferencesNestedFragment
                                              implements SharedPreferences.OnSharedPreferenceChangeListener
{

    //private PreferenceManager prefMng;
    //private SharedPreferences preferences;
    //public static LocationGeofencePreference changedLocationGeofencePreference;
    private String extraScrollTo;
    //private String extraScrollToType;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);
        // this is really important in order to save the state across screen
        // configuration changes for example
        //setRetainInstance(true);

        extraScrollTo = getArguments().getString(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "");
        //extraScrollToType = getArguments().getString(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "");

        updateSharedPreference();

    }

    @Override
    public void addPreferencesFromResource(int preferenceResId) {
        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);
        super.addPreferencesFromResource(preferenceResId);
    }

    @Override
    public int addPreferencesFromResource() {
        return R.xml.phone_profiles_preferences;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        setSummary(key);
    }

    private void updateSharedPreference()
    {
        setSummary(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ALERT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_CLOSE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION);
        setSummary(ApplicationPreferences.PREF_APPLICATION_HOME_LAUNCHER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_LANGUAGE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_THEME);
        setSummary(ApplicationPreferences.PREF_APPLICATION_NIGHT_MODE_OFF_THEME);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_HEADER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_HEADER);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_TOAST);
        if (Build.VERSION.SDK_INT < 26)
            setSummary(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE);

        if (Build.VERSION.SDK_INT < 26) {
            setSummary(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
                Preference preference = prefMng.findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
                if (preference != null) {
                    preference.setTitle(R.string.phone_profiles_pref_notificationShowInStatusBarAndLockscreen);
                }
            //}
        }

        if (Build.VERSION.SDK_INT < 26)
            setSummary(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_HEADER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME);
        setSummary(ApplicationPreferences.PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T);
        setSummary(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES);
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            //setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_PREF_INDICATOR);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_HEADER);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS);
            //setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_GRID_LAYOUT);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS);
        }
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_START_EVENTS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_APPLICATION_POWER_MANAGER);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_POWER_SAVE_MODE_SETTINGS);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_GRANT_ROOT_PERMISSION);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_DRAW_OVERLAYS_PERMISSIONS);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_APPLICATION_PERMISSIONS);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_AUTOSTART_MANAGER);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_NOTIFICATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_USAGE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_LOCATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_LOCATION_EDITOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_USE_GPS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING);
        setSummary(PhoneProfilesPreferencesNestedFragment.PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS);

        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, getActivity().getApplicationContext());
        if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            /*prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_RESCAN).setEnabled(false);*/
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, false);
            editor.apply();
            Preference preference = prefMng.findPreference("wifiScanningCategory");
            if (preference != null)
                preference.setEnabled(false);
        }

        preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, getActivity().getApplicationContext());
        if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            /*prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN).setEnabled(false);
            if (WifiBluetoothScanner.bluetoothLESupported(preferencesActivity.getApplicationContext()))
                prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION).setEnabled(false);*/
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, false);
            editor.apply();
            Preference preference = prefMng.findPreference("bluetoothScanningCategory");
            if (preference != null)
                preference.setEnabled(false);
        }

        preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, getActivity().getApplicationContext());
        if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            Preference preference = prefMng.findPreference("orientationScanningCategory");
            if (preference != null)
                preference.setEnabled(false);
        }

        preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, getActivity().getApplicationContext());
        if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {

            Preference preference = prefMng.findPreference("mobileCellsScanningCategory");
            if (preference != null)
                preference.setEnabled(false);
        }

        if (!PhoneProfilesService.isLocationEnabled(getActivity().getApplicationContext())) {
            Preference preference = prefMng.findPreference(PREF_LOCATION_EDITOR);
            if (preference != null)
                preference.setEnabled(false);
        }

    }

    @Override
    public void onStart()
    {
        super.onStart();

        /*
        // scroll to preference
        ListView listView = getActivity().findViewById(android.R.id.list);
        if (listView != null) {
            PreferenceCategory scrollCategory = null;
            CheckBoxPreference scrollCheckBox = null;
            PreferenceScreen scrollScreen = null;
            if (extraScrollToType.equals("category"))
                scrollCategory = (PreferenceCategory) findPreference(extraScrollTo);
            else
            if (extraScrollToType.equals("checkbox"))
                scrollCheckBox = (CheckBoxPreference) findPreference(extraScrollTo);
            else
            if (extraScrollToType.equals("screen"))
                scrollScreen = (PreferenceScreen) findPreference(extraScrollTo);
            for (int i = 0; i < getPreferenceScreen().getRootAdapter().getCount(); i++) {
                Object o = getPreferenceScreen().getRootAdapter().getItem(i);
                if ((scrollCategory != null) &&
                        (o instanceof PreferenceCategory) && (o.equals(scrollCategory)))
                    listView.setSelection(i);
                else
                if ((scrollCheckBox != null) &&
                        (o instanceof CheckBoxPreference) && (o.equals(scrollCheckBox)))
                    listView.setSelection(i);
                else
                if ((scrollScreen != null) &&
                        (o instanceof PreferenceScreen) && (o.equals(scrollScreen)))
                    listView.setSelection(i);
            }
        }
        */

        if (extraScrollTo != null) {
            PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("rootScreen");
            Preference preference = findPreference(extraScrollTo);
            if (preference != null) {
                int pos = preference.getOrder();
                preferenceScreen.onItemClick(null, null, pos, 0);
            }
            extraScrollTo = null;
        }

    }

    /*
    static public void setChangedLocationGeofencePreference(LocationGeofencePreference changedLocationGeofencePref)
    {
        changedLocationGeofencePreference = changedLocationGeofencePref;
    }
    */

}
