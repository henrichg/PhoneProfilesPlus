package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;

public class EventPreferencesNestedFragment extends PreferenceFragment
                                        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    protected int startupSource;

    private Event event;
    //private boolean first_start_activity;
    protected PreferenceManager prefMng;
    protected SharedPreferences preferences;
    private Context context;

    public static MobileCellsPreference.PhoneStateChangedBroadcastReceiver phoneStateChangedBroadcastReceiver;

    private static final String PREFS_NAME_ACTIVITY = "event_preferences_activity";
    //static final String PREFS_NAME_FRAGMENT = "event_preferences_fragment";

    private static final String PREF_NOTIFICATION_ACCESS = "eventNotificationNotificationsAccessSettings";
    private static final int RESULT_NOTIFICATION_ACCESS_SETTINGS = 1981;
    private static final String PREF_APPLICATIONS_ACCESSIBILITY_SETTINGS = "eventApplicationAccessibilitySettings";
    private static final int RESULT_ACCESSIBILITY_SETTINGS = 1982;
    private static final String PREF_LOCATION_SETTINGS = "eventLocationScanningSystemSettings";
    private static final int RESULT_LOCATION_SETTINGS = 1983;
    private static final String PREF_WIFI_SCANNING_APP_SETTINGS = "eventEnableWiFiScaningAppSettings";
    private static final int RESULT_WIFI_SCANNING_SETTINGS = 1984;
    private static final String PREF_BLUETOOTH_SCANNING_APP_SETTINGS = "eventEnableBluetoothScaningAppSettings";
    private static final int RESULT_BLUETOOTH_SCANNING_SETTINGS = 1985;
    private static final String PREF_ORIENTATION_ACCESSIBILITY_SETTINGS = "eventOrientationAccessibilitySettings";
    private static final String PREF_ORIENTATION_SCANNING_APP_SETTINGS = "eventEnableOrientationScanningAppSettings";
    private static final int RESULT_ORIENTATION_SCANNING_SETTINGS = 1986;
    private static final String PREF_MOBILE_CELLS_SCANNING_APP_SETTINGS = "eventMobileCellsScaningAppSettings";
    private static final int RESULT_MOBILE_CELLS_SCANNING_SETTINGS = 1987;
    private static final String PREF_USE_PRIORITY_APP_SETTINGS = "eventUsePriorityAppSettings";
    private static final int RESULT_USE_PRIORITY_SETTINGS = 1988;

    @Override
    public int addPreferencesFromResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);

        Bundle bundle = this.getArguments();
        if (bundle != null)
            startupSource = bundle.getInt(PPApplication.EXTRA_STARTUP_SOURCE, 0);

        context = getActivity().getBaseContext();

        prefMng = getPreferenceManager();
        preferences = prefMng.getSharedPreferences();

        event = new Event();
    }

    public static String getPreferenceName(int startupSource) {
        String PREFS_NAME;
        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        /*else
        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
            PREFS_NAME = PREFS_NAME_FRAGMENT;*/
        else
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        return PREFS_NAME;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(EventPreferencesNestedFragment.getPreferenceName(startupSource));
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        //RingtonePreference notificationSoundPreference = (RingtonePreference)prefMng.findPreference(Event.PREF_EVENT_NOTIFICATION_SOUND);
        //notificationSoundPreference.setEnabled(PPApplication.notificationStatusBar);

        event.checkPreferences(prefMng, context);

        Preference notificationAccessPreference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
        if (notificationAccessPreference != null) {
            //notificationAccessPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            notificationAccessPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (GlobalGUIRoutines.activityActionExists("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS", context)) {
                        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                        startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        dialogBuilder.show();
                    }
                    return false;
                }
            });
        }
        Preference accessibilityPreference = prefMng.findPreference(PREF_APPLICATIONS_ACCESSIBILITY_SETTINGS);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, context)) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        dialogBuilder.show();
                    }
                    return false;
                }
            });
        }
        Preference preference = prefMng.findPreference(PREF_LOCATION_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "locationScanningCategory");
                    //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_LOCATION_SETTINGS);
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(PREF_WIFI_SCANNING_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "wifiScanningCategory");
                    //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_WIFI_SCANNING_SETTINGS);
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(PREF_BLUETOOTH_SCANNING_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "bluetoothScanninCategory");
                    //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_BLUETOOTH_SCANNING_SETTINGS);
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(PREF_ORIENTATION_SCANNING_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "orientationScanningCategory");
                    //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_ORIENTATION_SCANNING_SETTINGS);
                    return false;
                }
            });
        }
        Preference orientationPreference = prefMng.findPreference(PREF_ORIENTATION_ACCESSIBILITY_SETTINGS);
        if (orientationPreference != null) {
            //orientationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            orientationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, context)) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        dialogBuilder.show();
                    }
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(PREF_MOBILE_CELLS_SCANNING_APP_SETTINGS);
        if (preference != null) {
            //locationPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "mobileCellsScanningCategory");
                    //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_MOBILE_CELLS_SCANNING_SETTINGS);
                    return false;
                }
            });
        }
        preference = prefMng.findPreference(PREF_USE_PRIORITY_APP_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setSummary(getString(R.string.event_preferences_event_priorityInfo_summary)+"\n"+
                                  getString(R.string.phone_profiles_pref_eventUsePriorityAppSettings_summary));
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "eventRunCategory");
                    //intent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_USE_PRIORITY_SETTINGS);
                    return false;
                }
            });
        }
    }

    @Override
    public void onDestroy()
    {
        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception ignored) {}
        if (phoneStateChangedBroadcastReceiver != null) {
            //getActivity().unregisterReceiver(phoneStateChangedBroadcastReceiver);
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(phoneStateChangedBroadcastReceiver);
            phoneStateChangedBroadcastReceiver = null;
        }
        super.onDestroy();
    }

    public void doOnActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Log.d("EventPreferencesFragment.doOnActivityResult", "requestCode="+requestCode);

        if (requestCode == RESULT_NOTIFICATION_ACCESS_SETTINGS) {
            event._eventPreferencesNotification.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS) {
            event._eventPreferencesApplication.checkPreferences(prefMng, context);
            event._eventPreferencesOrientation.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_WIFI_SCANNING_SETTINGS) {
            event._eventPreferencesWifi.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_BLUETOOTH_SCANNING_SETTINGS) {
            event._eventPreferencesBluetooth.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_LOCATION_SETTINGS) {
            event._eventPreferencesLocation.checkPreferences(prefMng, context);
        }
        if (requestCode == LocationGeofencePreference.RESULT_GEOFENCE_EDITOR) {
            //Log.d("EventPreferencesFragment.doOnActivityResult", "xxx");
            if (EventPreferencesFragment.changedLocationGeofencePreference != null) {
                if(resultCode == Activity.RESULT_OK){
                    //long geofenceId = data.getLongExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, 0);
                    // this persistGeofence, for multislelect this mus only refresh listView in preference
                    EventPreferencesFragment.changedLocationGeofencePreference.setGeofenceFromEditor(/*geofenceId*/);
                }
            }
        }
        if (requestCode == RESULT_ORIENTATION_SCANNING_SETTINGS) {
            event._eventPreferencesOrientation.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_MOBILE_CELLS_SCANNING_SETTINGS) {
            event._eventPreferencesMobileCells.checkPreferences(prefMng, context);
        }
        if (requestCode == RESULT_USE_PRIORITY_SETTINGS) {
            event.setSummary(prefMng, Event.PREF_EVENT_PRIORITY, preferences, context);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode, data);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {

        //eventTypeChanged = false;

        event.setSummary(prefMng, key, sharedPreferences, context);

        //Activity activity = getActivity();
        //boolean canShow = (EditorProfilesActivity.mTwoPane) && (activity instanceof EditorProfilesActivity);
        //canShow = canShow || ((!EditorProfilesActivity.mTwoPane) && (activity instanceof EventPreferencesActivity));
        //if (canShow)
        //    showActionMode();
        EventPreferencesActivity activity = (EventPreferencesActivity)getActivity();
        EventPreferencesActivity.showSaveMenu = true;
        activity.invalidateOptionsMenu();
    }

    @Override
    protected String getSavedInstanceStateKeyName() {
        return "EventPreferencesFragment_PreferenceScreenKey";
    }

}
