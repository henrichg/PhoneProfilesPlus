package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.thelittlefireman.appkillermanager.managers.KillerManager;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

public class PhoneProfilesPrefsFragment extends PreferenceFragmentCompat
                        implements SharedPreferences.OnSharedPreferenceChangeListener {

    PreferenceManager prefMng;
    SharedPreferences preferences;

    static final String PREF_APPLICATION_PERMISSIONS = "permissionsApplicationPermissions";
    private static final int RESULT_APPLICATION_PERMISSIONS = 1990;
    static final String PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS = "permissionsWriteSystemSettingsPermissions";
    private static final int RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS = 1991;
    static final String PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = "permissionsAccessNotificationPolicyPermissions";
    private static final int RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = 1997;
    static final String PREF_DRAW_OVERLAYS_PERMISSIONS = "permissionsDrawOverlaysPermissions";
    private static final int RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS = 1998;
    static final String PREF_GRANT_ROOT_PERMISSION = "permissionsGrantRootPermission";

    static final String PREF_WIFI_LOCATION_SYSTEM_SETTINGS = "applicationEventWiFiLocationSystemSettings";
    static final String PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS = "applicationEventBluetoothLocationSystemSettings";
    static final String PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS = "applicationEventMobileCellsLocationSystemSettings";
    private static final int RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS = 1992;
    static final String PREF_POWER_SAVE_MODE_SETTINGS = "applicationPowerSaveMode";
    private static final int RESULT_POWER_SAVE_MODE_SETTINGS = 1993;
    //static final String PREF_POWER_SAVE_MODE_INTERNAL = "applicationPowerSaveModeInternal";
    static final String PREF_LOCATION_SYSTEM_SETTINGS = "applicationEventLocationSystemSettings";
    private static final int RESULT_LOCATION_SYSTEM_SETTINGS = 1994;
    static final String PREF_LOCATION_EDITOR = "applicationEventLocationsEditor";
    static final String PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS = "applicationBatteryOptimization";
    private static final String PREF_APPLICATION_LANGUAGE_24 = "applicationLanguage24";
    //static final int RESULT_LOCALE_SETTINGS = 1996;
    static final String PREF_AUTOSTART_MANAGER = "applicationAutoStartManager";
    static final String PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS = "applicationEventWiFiKeepOnSystemSettings";
    private static final int RESULT_WIFI_KEEP_ON_SETTINGS = 1999;
    static final String PREF_NOTIFICATION_SYSTEM_SETTINGS = "notificationSystemSettings";
    static final String PREF_APPLICATION_POWER_MANAGER = "applicationPowerManager";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        PPApplication.logE("PhoneProfilesPrefsFragment.onCreate", "xxx");

        prefMng = getPreferenceManager();
        preferences = prefMng.getSharedPreferences();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        initPreferenceFragment();
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference)
    {
        DialogFragment dialogFragment = null;

        if (preference instanceof DurationDialogPreferenceX)
        {
            dialogFragment = new DurationDialogPreferenceFragmentX();
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null)
        {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(fragmentManager, "sk.henrichg.phoneprofilesplus.PhoneProfilesPrefsActivity.DIALOG");
            }
        }
        else
        {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onDestroy()
    {
        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception ignored) {}

        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setSummary(key);
    }

    void initPreferenceFragment() {
        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("ConstantConditions")
    void setSummary(String key) {

        Preference preference = prefMng.findPreference(key);

        if (preference == null)
            return;

        if (Build.VERSION.SDK_INT < 26) {
            boolean notificationStatusBar = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true);
            boolean notificationStatusBarPermanent = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
            PreferenceScreen preferenceCategoryNotifications = findPreference("categoryNotifications");
            if (!(notificationStatusBar && notificationStatusBarPermanent)) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceCategoryNotifications, true, true, false, true, false);
                if (preferenceCategoryNotifications != null) {
                    String summary = getString(R.string.phone_profiles_pref_notificationStatusBarNotEnabled_summary) + " " +
                            getString(R.string.phone_profiles_pref_notificationStatusBarRequired) + "\n\n";
                    setCategorySummary(preferenceCategoryNotifications, summary);
                }
            } else {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceCategoryNotifications, true, false, false, false, false);
                if (preferenceCategoryNotifications != null) {
                    String summary = "";
                    setCategorySummary(preferenceCategoryNotifications, summary);
                }
            }
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR)) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !notificationStatusBar, false, !notificationStatusBar, false);
            }
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT)) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !notificationStatusBarPermanent, false, !notificationStatusBarPermanent, false);
            }
        }

        if (/*(android.os.Build.VERSION.SDK_INT >= 21) &&*/ (android.os.Build.VERSION.SDK_INT < 26)) {
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR)) {
                boolean show = preferences.getBoolean(key, true);
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN);
                if (_preference != null)
                    _preference.setEnabled(show);
            }
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER);
            if (_preference != null) {
                _preference.setEnabled(preferences.getBoolean(key, false));
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER);
            if (_preference != null) {
                _preference.setEnabled(preferences.getBoolean(key, false));
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER);
            if (_preference != null) {
                _preference.setEnabled(preferences.getBoolean(key, false));
            }
        }

        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR)) {
            String backgroundColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
            Preference _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR);
            if (_preference != null)
                _preference.setEnabled(backgroundColor.equals("0"));
            _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION);
            if (_preference != null)
                _preference.setEnabled(backgroundColor.equals("0"));
            boolean useDecoration = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, true);
            _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
            if (_preference != null)
                _preference.setEnabled(useDecoration && backgroundColor.equals("0"));
        }
        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION)) {
            boolean useDecoration = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, true);
            String backgroundColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
            Preference _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
            if (_preference != null)
                _preference.setEnabled(useDecoration && backgroundColor.equals("0"));
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(getActivity().getApplicationContext()))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("locationScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(getActivity().getApplicationContext()))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("wifiScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(getActivity().getApplicationContext()))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("bluetoothScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(getActivity().getApplicationContext()))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("mobileCellsScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(getActivity().getApplicationContext()))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("orientationScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }

        // Do not bind toggles.
        if (preference instanceof CheckBoxPreference || preference instanceof TwoStatePreference) {
            return;
        }

        String stringValue = preferences.getString(key, "");

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE)) {
            long lProfileId;
            try {
                lProfileId = Long.parseLong(stringValue);
            } catch (Exception e) {
                lProfileId = 0;
            }
            ProfilePreference profilePreference = (ProfilePreference) preference;
            profilePreference.setSummary(lProfileId);

            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_SOUND);
            if (_preference != null)
                _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_VIBRATE);
            if (_preference != null)
                _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_USAGE);
            if (_preference != null)
                _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);
        } else if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            // added support for "%" in list items
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            if (summary != null) {
                String sSummary = summary.toString();
                sSummary = sSummary.replace("%", "%%");
                preference.setSummary(sSummary);
            } else
                preference.setSummary(null);

            //if (key.equals(PPApplication.PREF_APPLICATION_LANGUAGE))
            //    setTitleStyle(preference, true, false);
        } else
            //noinspection StatementWithEmptyBody
            if (preference instanceof RingtonePreference) {
                // keep summary from preference
            } else {
                if (!stringValue.isEmpty()) {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    //preference.setSummary(preference.toString());
                    preference.setSummary(stringValue);
                }
            }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
            if (_preference != null) {
                boolean enabled;
                String value = preferences.getString(key, "0");
                if (!value.equals("0"))
                    enabled = value.equals("1");
                else
                    enabled = ActivateProfileHelper.getMergedRingNotificationVolumes(getActivity().getApplicationContext());
                //Log.d("PhoneProfilesPreferencesNestedFragment.setSummary","enabled="+enabled);
                _preference.setEnabled(enabled);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
        /*if (key.equals(PREF_GRANT_ROOT_PERMISSION)) {
            if (PPApplication.isRooted()) {
                String summary;
                if (PPApplication.isRootGranted(true))
                    summary = getString(R.string.permission_granted);
                else
                    summary = getString(R.string.permission_not_granted);
                preference.setSummary(summary);
            }
        }*/
        if (Build.VERSION.SDK_INT >= 23) {
            /*if (key.equals(PREF_APPLICATION_PERMISSIONS)) {
                // not possible to get granted runtime permission groups :-(
            }*/
            if (key.equals(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS)) {
                String summary;
                if (Settings.System.canWrite(getActivity().getApplicationContext()))
                    summary = getString(R.string.permission_granted);
                else {
                    summary = getString(R.string.permission_not_granted);
                    summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_writeSystemSettingPermissions_summary);
                }
                preference.setSummary(summary);
            }
            if (key.equals(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS)) {
                String summary;
                if (Permissions.checkAccessNotificationPolicy(getActivity().getApplicationContext()))
                    summary = getString(R.string.permission_granted);
                else {
                    summary = getString(R.string.permission_not_granted);
                    summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions_summary);
                }
                preference.setSummary(summary);
            }
            if (key.equals(PREF_DRAW_OVERLAYS_PERMISSIONS)) {
                String summary;
                if (Settings.canDrawOverlays(getActivity().getApplicationContext()))
                    summary = getString(R.string.permission_granted);
                else {
                    summary = getString(R.string.permission_not_granted);
                    summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_drawOverlaysPermissions_summary);
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_LOCATION_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventLocationSystemSettings_summary);
            if (!PhoneProfilesService.isLocationEnabled(getActivity().getApplicationContext())) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_WIFI_LOCATION_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventWiFiLocationSystemSettings_summary);
            if (!PhoneProfilesService.isLocationEnabled(getActivity().getApplicationContext())) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventBluetoothLocationSystemSettings_summary);
            if (!PhoneProfilesService.isLocationEnabled(getActivity().getApplicationContext())) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS)) {
            String summary;
            if (Build.VERSION.SDK_INT < 28)
                summary = getString(R.string.phone_profiles_pref_eventMobileCellsLocationSystemSettingsNotA9_summary);
            else
                summary = getString(R.string.phone_profiles_pref_eventMobileCellsLocationSystemSettings_summary);
            if (!PhoneProfilesService.isLocationEnabled(getActivity().getApplicationContext())) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_summary);
            if (PhoneProfilesService.isWifiSleepPolicySetToNever(getActivity().getApplicationContext())) {
                summary = getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_setToAlways_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_notSetToAlways_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setCategorySummary(PreferenceScreen preferenceCategory, String summary) {
        String key = preferenceCategory.getKey();

        if (key.equals("applicationInterfaceCategory")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationLanguage);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationTheme);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHomeLauncher);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLauncher);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationLauncher);
        }
        if (key.equals("categoryApplicationStart")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationStartOnBoot);
            if (KillerManager.isActionAvailable(getActivity(), KillerManager.Actions.ACTION_AUTOSTART)) {
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + getString(R.string.phone_profiles_pref_systemAutoStartManager);
            }
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationActivate);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationStartEvents);
        }
        if (key.equals("categorySystem")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationForceSetMergeRingNotificationVolumes);
            if (Build.VERSION.SDK_INT >= 23) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationBatteryOptimization);
            }
            if (KillerManager.isActionAvailable(getActivity(), KillerManager.Actions.ACTION_POWERSAVING)) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationPowerManager);
            }
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationPowerSaveMode);
            if (Build.VERSION.SDK_INT >= 21) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationBatterySaver);
            }
        }
        if (key.equals("categoryPermissions")) {
            if (PPApplication.isRooted(true)) {
                summary = summary + getString(R.string.phone_profiles_pref_grantRootPermission);
            }
            if (Build.VERSION.SDK_INT >= 23) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_writeSystemSettingPermissions);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_drawOverlaysPermissions);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationPermissions);
            }
        }
        if (key.equals("categoryNotifications")) {
            summary = summary + getString(R.string.phone_profiles_pref_notificationsToast);
            if (Build.VERSION.SDK_INT >= 26) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationSystemSettings);
            }
            else {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationStatusBar);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationStatusBarPermanent);
            }
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationLayoutType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationStatusBarStyle);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationBackgroundColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationTextColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationUseDecoration);
        }
        if (key.equals("profileActivationCategory")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventBackgroundProfile);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_backgroundProfileUsage);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationAlert);
        }
        if (key.equals("eventRunCategory")) {
            summary = summary + getString(R.string.phone_profiles_pref_eventRunUsePriority);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationRestartEventsAlert);
        }
        if (key.equals("locationScanningCategory")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventLocationEnableScanning) + ": ";
            if (ApplicationPreferences.applicationEventLocationEnableScanning(getActivity())) {
                summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                if (!PhoneProfilesService.isLocationEnabled(getActivity())) {
                    summary = summary + "\n";
                    summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                            getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                }
                else {
                    summary = summary + "\n";
                    summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                            getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
                }
            }
            else {
                if (!ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(getActivity()))
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_disabled);
                else
                    summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
            }
            summary = summary + "\n\n";
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventLocationsEditor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventLocationScanInterval);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventLocationsUseGPS);
        }
        if (key.equals("wifiScanningCategory")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, getActivity().getApplicationContext());
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(getActivity());
            }
            else {
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventWifiEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventWifiEnableScanning(getActivity())) {
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                    if (!PhoneProfilesService.isLocationEnabled(getActivity())) {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                    } else {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
                    }
                    if (Build.VERSION.SDK_INT < 27) {
                        if (PhoneProfilesService.isWifiSleepPolicySetToNever(getActivity())) {
                            summary = summary + "\n";
                            summary = summary + getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings) + ": " +
                                    getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_setToAlways_summary);
                        } else {
                            summary = summary + "\n";
                            summary = summary + getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings) + ": " +
                                    getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_notSetToAlways_summary);
                        }
                    }
                } else {
                    if (!ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(getActivity()))
                        summary = summary + getString(R.string.array_pref_applicationDisableScanning_disabled);
                    else
                        summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                }
                summary = summary + "\n\n";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventWifiScanIfWifiOff);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventWifiScanInterval);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
            }
        }
        if (key.equals("bluetoothScanningCategory")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, getActivity().getApplicationContext());
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(getActivity());
            }
            else {
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventBluetoothEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventBluetoothEnableScanning(getActivity())) {
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                    if (!PhoneProfilesService.isLocationEnabled(getActivity())) {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                    } else {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
                    }
                } else {
                    if (!ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(getActivity()))
                        summary = summary + getString(R.string.array_pref_applicationDisableScanning_disabled);
                    else
                        summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                }
                summary = summary + "\n\n";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventBluetoothScanIfBluetoothOff);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventBluetoothScanInterval);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventBluetoothLEScanDuration);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
            }
        }
        if (key.equals("mobileCellsScanningCategory")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, getActivity().getApplicationContext());
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(getActivity());
            }
            else {
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventMobileCellEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventMobileCellEnableScanning(getActivity())) {
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                    if (!PhoneProfilesService.isLocationEnabled(getActivity())) {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                    } else {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
                    }
                } else {
                    if (!ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(getActivity()))
                        summary = summary + getString(R.string.array_pref_applicationDisableScanning_disabled);
                    else
                        summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                }
                summary = summary + "\n\n";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
            }
        }
        if (key.equals("orientationScanningCategory")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, getActivity().getApplicationContext());
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(getActivity());
            }
            else {
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventOrientationEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventOrientationEnableScanning(getActivity()))
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                else {
                    if (!ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(getActivity()))
                        summary = summary + getString(R.string.array_pref_applicationDisableScanning_disabled);
                    else
                        summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                }
                summary = summary + "\n\n";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventOrientationScanInterval);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
            }
        }
        if (key.equals("categoryActivator")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationClose);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationGridLayout);
        }
        if (key.equals("categoryEditor")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationEditorSaveEditorState);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_deleteOldActivityLogs);
        }
        if (key.equals("categoryWidgetList")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationGridLayout);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackground);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetColorB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetShowBorder);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetRoundedCorners);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessT);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
        }
        if (key.equals("categoryWidgetOneRow")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackground);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetColorB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetShowBorder);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetRoundedCorners);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessT);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
        }
        if (key.equals("categoryWidgetIcon")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconBackgroundType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconBackground);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconLightnessB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColorB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconShowBorder);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconRoundedCorners);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconHideProfileName);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconLightnessT);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
        }
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            if (key.equals("categorySamsungEdgePanel")) {
                summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackground);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessB);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetColorB);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessT);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
            }
        }

        if (!summary.isEmpty()) summary = summary +" • ";
        summary = summary + "…";

        preferenceCategory.setSummary(summary);
    }

}
