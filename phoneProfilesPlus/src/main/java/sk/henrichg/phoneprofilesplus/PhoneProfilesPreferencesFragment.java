package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

public class PhoneProfilesPreferencesFragment extends PhoneProfilesPreferencesNestedFragment
                                              implements SharedPreferences.OnSharedPreferenceChangeListener
{

    //private PreferenceManager prefMng;
    //private SharedPreferences preferences;
    public static Activity preferencesActivity = null;
    public static LocationGeofencePreference changedLocationGeofencePreference;
    private String extraScrollTo;
    //private String extraScrollToType;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(false);

        preferencesActivity = getActivity();

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        extraScrollTo = getArguments().getString(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "");
        //Log.e("------- PhoneProfilesPreferencesFragment", "extraScrollTo=" + extraScrollTo);
        //extraScrollToType = getArguments().getString(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "");

        updateSharedPreference();

    }

    @Override
    public void addPreferencesFromResource(int preferenceResId) {
        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(GlobalData.APPLICATION_PREFS_NAME);
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
        setSummary(GlobalData.PREF_APPLICATION_START_ON_BOOT);
//	    setSummary(GlobalData.PREF_APPLICATION_ACTIVATE);
        setSummary(GlobalData.PREF_APPLICATION_ALERT);
        setSummary(GlobalData.PREF_APPLICATION_CLOSE);
        setSummary(GlobalData.PREF_APPLICATION_LONG_PRESS_ACTIVATION);
        setSummary(GlobalData.PREF_APPLICATION_HOME_LAUNCHER);
        setSummary(GlobalData.PREF_APPLICATION_NOTIFICATION_LAUNCHER);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LAUNCHER);
        setSummary(GlobalData.PREF_APPLICATION_LANGUAGE);
        setSummary(GlobalData.PREF_APPLICATION_THEME);
        setSummary(GlobalData.PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR);
        setSummary(GlobalData.PREF_APPLICATION_EDITOR_PREF_INDICATOR);
        setSummary(GlobalData.PREF_APPLICATION_ACTIVATOR_HEADER);
        setSummary(GlobalData.PREF_APPLICATION_EDITOR_HEADER);
        setSummary(GlobalData.PREF_NOTIFICATION_TOAST);
        setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR);
        setSummary(GlobalData.PREF_NOTIFICATION_TEXT_COLOR);

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            setSummary(GlobalData.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                Preference preference = prefMng.findPreference(GlobalData.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
                if (preference != null) {
                    preference.setTitle(R.string.phone_profiles_pref_notificationShowInStatusBarAndLockscreen);
                }
            }
        }
        else {
            Preference preference = prefMng.findPreference(GlobalData.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
            if (preference != null) {
                preference.setEnabled(false);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(GlobalData.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true);
                editor.commit();
            }
        }

        setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR_PERMANENT);
        //setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR_CANCEL);

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            // for Android 5.0, color notification icon is not supported
            Preference preference = prefMng.findPreference(GlobalData.PREF_NOTIFICATION_STATUS_BAR_STYLE);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryNotifications");
                preferenceCategory.removePreference(preference);
            }
        }
        else
            setSummary(GlobalData.PREF_NOTIFICATION_STATUS_BAR_STYLE);

        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_HEADER);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_BACKGROUND);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_ICON_COLOR);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_WIFI_RESCAN);
        setSummary(GlobalData.PREF_APPLICATION_BACKGROUND_PROFILE);
        setSummary(GlobalData.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME);
        setSummary(GlobalData.PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_LOCATION_RESCAN);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL);
        setSummary(GlobalData.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE);
        setSummary(GlobalData.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_ICON_BACKGROUND);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
        setSummary(GlobalData.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T);


        if (GlobalData.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, preferencesActivity.getApplicationContext())
                    != GlobalData.PREFERENCE_ALLOWED)
        {
            /*prefMng.findPreference(GlobalData.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL).setEnabled(false);
            prefMng.findPreference(GlobalData.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI).setEnabled(false);
            prefMng.findPreference(GlobalData.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE).setEnabled(false);
            prefMng.findPreference(GlobalData.PREF_APPLICATION_EVENT_WIFI_RESCAN).setEnabled(false);*/
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(GlobalData.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, false);
            editor.commit();
            Preference preference = (Preference) prefMng.findPreference("wifiScanningCategory");
            preference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                    ": "+getResources().getString(GlobalData.getNotAllowedPreferenceReasonString()));
            preference.setEnabled(false);
        }

        if (GlobalData.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, preferencesActivity.getApplicationContext())
                != GlobalData.PREFERENCE_ALLOWED)
        {
            /*prefMng.findPreference(GlobalData.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL).setEnabled(false);
            prefMng.findPreference(GlobalData.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH).setEnabled(false);
            prefMng.findPreference(GlobalData.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE).setEnabled(false);
            prefMng.findPreference(GlobalData.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN).setEnabled(false);
            if (ScannerService.bluetoothLESupported(preferencesActivity.getApplicationContext()))
                prefMng.findPreference(GlobalData.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION).setEnabled(false);*/
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(GlobalData.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, false);
            editor.commit();
            Preference preference = (Preference) prefMng.findPreference("bluetoothScanninCategory");
            preference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                    ": "+getResources().getString(GlobalData.getNotAllowedPreferenceReasonString()));
            preference.setEnabled(false);
        }

        if (GlobalData.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, preferencesActivity.getApplicationContext())
                != GlobalData.PREFERENCE_ALLOWED)
        {
            Preference preference = (Preference) prefMng.findPreference("orientationScanningCategory");
            preference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                    ": "+getResources().getString(GlobalData.getNotAllowedPreferenceReasonString()));
            preference.setEnabled(false);
        }
        if (GlobalData.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, preferencesActivity.getApplicationContext())
                != GlobalData.PREFERENCE_ALLOWED)
        {
            Preference preference = (Preference) prefMng.findPreference("mobileCellsScanningCategory");
            preference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                    ": "+getResources().getString(GlobalData.getNotAllowedPreferenceReasonString()));
            preference.setEnabled(false);
        }

        if (!PhoneProfilesService.isLocationEnabled(preferencesActivity.getApplicationContext())) {
            Preference preference = prefMng.findPreference(PREF_LOCATION_EDITOR);
            preference.setEnabled(false);
        }

    }

    @Override
    public void onStart()
    {
        super.onStart();

        /*
        // scroll to preference
        ListView listView = (ListView) getActivity().findViewById(android.R.id.list);
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

    static public Activity getPreferencesActivity()
    {
        return preferencesActivity;
    }

    static public void setChangedLocationGeofencePreference(LocationGeofencePreference changedLocationGeofencePref)
    {
        changedLocationGeofencePreference = changedLocationGeofencePref;
    }

}
