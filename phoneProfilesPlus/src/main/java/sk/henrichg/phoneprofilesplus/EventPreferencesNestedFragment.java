package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;

public class EventPreferencesNestedFragment extends PreferenceFragment
                                        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private long event_id;
    int startupSource;

    private Event event;
    //private boolean first_start_activity;
    PreferenceManager prefMng;
    SharedPreferences preferences;
    private Context context;

    private MobileCellsRegistrationCountDownBroadcastReceiver mobileCellsRegistrationCountDownBroadcastReceiver = null;
    private MobileCellsRegistrationStoppedBroadcastReceiver mobileCellsRegistrationStoppedBroadcastReceiver = null;

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
    private static final String PREF_MOBILE_CELLS_REGISTRATION = "eventMobileCellsRegistration";

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
        if (bundle != null) {
            event_id = bundle.getLong(PPApplication.EXTRA_EVENT_ID, 0);
            startupSource = bundle.getInt(PPApplication.EXTRA_STARTUP_SOURCE, 0);
        }

        context = getActivity().getBaseContext();

        prefMng = getPreferenceManager();
        preferences = prefMng.getSharedPreferences();

        event = new Event();

        if (mobileCellsRegistrationCountDownBroadcastReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_COUNTDOWN);
            mobileCellsRegistrationCountDownBroadcastReceiver =
                    new MobileCellsRegistrationCountDownBroadcastReceiver(
                            (MobileCellsRegistrationDialogPreference)prefMng.findPreference(PREF_MOBILE_CELLS_REGISTRATION));
            context.registerReceiver(mobileCellsRegistrationCountDownBroadcastReceiver, intentFilter);
        }

        if (mobileCellsRegistrationStoppedBroadcastReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_NEWCELLS);
            mobileCellsRegistrationStoppedBroadcastReceiver =
                    new MobileCellsRegistrationStoppedBroadcastReceiver(
                            (MobileCellsPreference)prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELLS));
            context.registerReceiver(mobileCellsRegistrationStoppedBroadcastReceiver, intentFilter);
        }
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
        Preference extenderPreference = prefMng.findPreference(EventPreferencesApplication.PREF_EVENT_APPLICATION_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String url = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
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
                    } else {
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
        extenderPreference = prefMng.findPreference(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String url = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
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
                    } else {
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
        MobileCellsRegistrationDialogPreference mobileCellsRegistrationDialogPreference =
                (MobileCellsRegistrationDialogPreference)prefMng.findPreference(PREF_MOBILE_CELLS_REGISTRATION);
        if (mobileCellsRegistrationDialogPreference != null) {
            mobileCellsRegistrationDialogPreference.event_id = event_id;
        }
        /*
        MobileCellsPreference mobileCellsPreference =
                (MobileCellsPreference)prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELLS);
        if (mobileCellsPreference != null) {
            mobileCellsPreference.event_id = event_id;
        }
        */
    }

    @Override
    public void onDestroy()
    {
        //Log.e("****** EventPreferencesNestedFragment.onDestroy","xxxx");
        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception ignored) {}

        if (mobileCellsRegistrationCountDownBroadcastReceiver != null) {
            try {
                context.unregisterReceiver(mobileCellsRegistrationCountDownBroadcastReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            mobileCellsRegistrationCountDownBroadcastReceiver = null;
        }

        if (mobileCellsRegistrationStoppedBroadcastReceiver != null) {
            try {
                context.unregisterReceiver(mobileCellsRegistrationStoppedBroadcastReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            mobileCellsRegistrationStoppedBroadcastReceiver = null;
        }

        super.onDestroy();
    }

    public void doOnActivityResult(int requestCode, int resultCode/*, Intent data*/)
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
            if (resultCode == Activity.RESULT_OK) {
                LocationGeofencePreference preference = (LocationGeofencePreference)prefMng.findPreference(EventPreferencesLocation.PREF_EVENT_LOCATION_GEOFENCES);
                if (preference != null) {
                    preference.setGeofenceFromEditor(/*geofenceId*/);
                }
            }
            /*if (EventPreferencesFragment.changedLocationGeofencePreference != null) {
                if(resultCode == Activity.RESULT_OK){
                    //long geofenceId = data.getLongExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, 0);
                    // this persistGeofence, for multiselect this mus only refresh listView in preference
                    EventPreferencesFragment.changedLocationGeofencePreference.setGeofenceFromEditor();
                    EventPreferencesFragment.changedLocationGeofencePreference = null;
                }
            }*/
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
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_RINGTONE_PREFERENCE) {
            RingtonePreference preference = (RingtonePreference) prefMng.findPreference(Event.PREF_EVENT_NOTIFICATION_SOUND);
            if (preference != null)
                preference.refreshListView();
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_WIFI_BT_SCAN_DIALOG) {
            WifiSSIDPreference wifiPreference = (WifiSSIDPreference) prefMng.findPreference(EventPreferencesWifi.PREF_EVENT_WIFI_SSID);
            if (wifiPreference != null)
                wifiPreference.refreshListView(true, "");
            BluetoothNamePreference bluetoothPreference = (BluetoothNamePreference) prefMng.findPreference(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ADAPTER_NAME);
            if (bluetoothPreference != null)
                bluetoothPreference.refreshListView(true, "");
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_MOBILE_CELLS_SCAN_DIALOG) {
            MobileCellsPreference preference = (MobileCellsPreference) prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELLS);
            if (preference != null)
                preference.refreshListView(true);
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_MOBILE_CELLS_REGISTRATION_DIALOG) {
            MobileCellsRegistrationDialogPreference preference = (MobileCellsRegistrationDialogPreference) prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_REGISTRATION);
            if (preference != null)
                preference.startRegistration();
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CALENDAR_DIALOG) {
            CalendarsMultiSelectDialogPreference preference = (CalendarsMultiSelectDialogPreference) prefMng.findPreference(EventPreferencesCalendar.PREF_EVENT_CALENDAR_CALENDARS);
            if (preference != null)
                preference.refreshListView(true);
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CONTACT_DIALOG) {
            ContactsMultiSelectDialogPreference preference1 = (ContactsMultiSelectDialogPreference) prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_CONTACTS);
            if (preference1 != null)
                preference1.refreshListView(true);
            preference1 = (ContactsMultiSelectDialogPreference) prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_CONTACTS);
            if (preference1 != null)
                preference1.refreshListView(true);
            ContactGroupsMultiSelectDialogPreference preference2 = (ContactGroupsMultiSelectDialogPreference) prefMng.findPreference(EventPreferencesCall.PREF_EVENT_CALL_CONTACT_GROUPS);
            if (preference2 != null)
                preference2.refreshListView(true);
            preference2 = (ContactGroupsMultiSelectDialogPreference) prefMng.findPreference(EventPreferencesSMS.PREF_EVENT_SMS_CONTACT_GROUPS);
            if (preference2 != null)
                preference2.refreshListView(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(Event.PREF_EVENT_NAME)) {
            String value = sharedPreferences.getString(key, "");
            Toolbar toolbar = getActivity().findViewById(R.id.mp_toolbar);
            toolbar.setSubtitle(getString(R.string.event_string_0) + ": " + value);
        }

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

    public class MobileCellsRegistrationCountDownBroadcastReceiver extends BroadcastReceiver {

        final MobileCellsRegistrationDialogPreference preference;

        MobileCellsRegistrationCountDownBroadcastReceiver(MobileCellsRegistrationDialogPreference preference) {
            this.preference = preference;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (preference != null) {
                //Log.d("mobileCellsRegistrationCountDownBroadcastReceiver", "xxx");
                long millisUntilFinished = intent.getLongExtra(MobileCellsRegistrationService.EXTRA_COUNTDOWN, 0L);
                preference.updateInterface(millisUntilFinished, false);
                preference.setSummaryDDP(millisUntilFinished);
            }
        }
    }

    public class MobileCellsRegistrationStoppedBroadcastReceiver extends BroadcastReceiver {

        final MobileCellsPreference preference;

        MobileCellsRegistrationStoppedBroadcastReceiver(MobileCellsPreference preference) {
            this.preference = preference;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (preference != null) {
                //Log.d("MobileCellsRegistrationStoppedBroadcastReceiver", "xxx");
                preference.refreshListView(true);
            }
        }
    }
}
