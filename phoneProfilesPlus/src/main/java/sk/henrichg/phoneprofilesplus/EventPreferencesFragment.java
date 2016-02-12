package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import com.fnp.materialpreferences.PreferenceFragment;
 
public class EventPreferencesFragment extends PreferenceFragment
                                        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private DataWrapper dataWrapper;
    private Event event;
    //private boolean first_start_activity;
    private int new_event_mode;
    private int predefinedEventIndex;
    public static int startupSource;
    private PreferenceManager prefMng;
    private SharedPreferences preferences;
    private Context context;

    private static Activity preferencesActivity = null;
    private static LocationGeofencePreference changedLocationGeofencePreference;

    static final String PREFS_NAME_ACTIVITY = "event_preferences_activity";
    static final String PREFS_NAME_FRAGMENT = "event_preferences_fragment";

    static final String PREF_NOTIFICATION_ACCESS = "eventNotificationNotificationsAccessSettings";
    static final int RESULT_NOTIFICATION_ACCESS_SETTINGS = 1981;
    static final String PREF_ACCESSIBILITY_SETTINGS = "eventApplicationAccessibilitySettings";
    static final int RESULT_ACCESSIBILITY_SETTINGS = 1982;
    static final String PREF_LOCATION_SETTINGS = "eventLocationScanningSystemSettings";
    static final int RESULT_LOCATION_SETTINGS = 1983;
    static final String PREF_WIFI_SCANNING_APP_SETTINGS = "eventEnableWiFiScaningAppSettings";
    static final int RESULT_WIFI_SCANNING_SETTINGS = 1984;
    static final String PREF_BLUETOOTH_SCANNING_APP_SETTINGS = "eventEnableBluetoothScaningAppSettings";
    static final int RESULT_BLUETOOTH_SCANNING_SETTINGS = 1985;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);

        preferencesActivity = getActivity();
        context = getActivity().getBaseContext();

        dataWrapper = new DataWrapper(context.getApplicationContext(), true, false, 0);

        long event_id = 0;

        // getting attached fragment data
        if (getArguments().containsKey(GlobalData.EXTRA_NEW_EVENT_MODE))
            new_event_mode = getArguments().getInt(GlobalData.EXTRA_NEW_EVENT_MODE);
        if (getArguments().containsKey(GlobalData.EXTRA_EVENT_ID))
            event_id = getArguments().getLong(GlobalData.EXTRA_EVENT_ID);
        predefinedEventIndex = getArguments().getInt(GlobalData.EXTRA_PREDEFINED_EVENT_INDEX);

        event = EventPreferencesFragmentActivity.createEvent(context.getApplicationContext(), event_id, new_event_mode, predefinedEventIndex, true);

        //prefMng = getPreferenceManager();
        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);
        //Log.e("------------- EventPreferencesFragment", prefMng.getSharedPreferencesName());

        //if (savedInstanceState == null)
        //    loadPreferences();

        updateSharedPreference();

    }

    @Override
    public void addPreferencesFromResource(int preferenceResId) {
        String PREFS_NAME;
        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        else
        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
            PREFS_NAME = PREFS_NAME_FRAGMENT;
        else
            PREFS_NAME = PREFS_NAME_FRAGMENT;

        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        super.addPreferencesFromResource(preferenceResId);
    }

    @Override
    public int addPreferencesFromResource() {
        return R.xml.event_preferences;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //RingtonePreference notificationSoundPreference = (RingtonePreference)prefMng.findPreference(Event.PREF_EVENT_NOTIFICATION_SOUND);
        //notificationSoundPreference.setEnabled(GlobalData.notificationStatusBar);

        event._eventPreferencesTime.checkPreferences(prefMng, context);
        event._eventPreferencesBattery.checkPreferences(prefMng, context);
        event._eventPreferencesCall.checkPreferences(prefMng, context);
        event._eventPreferencesCalendar.checkPreferences(prefMng, context);
        event._eventPreferencesPeripherals.checkPreferences(prefMng, context);
        event._eventPreferencesWifi.checkPreferences(prefMng, context);
        event._eventPreferencesScreen.checkPreferences(prefMng, context);
        event._eventPreferencesBluetooth.checkPreferences(prefMng, context);
        event._eventPreferencesSMS.checkPreferences(prefMng, context);
        event._eventPreferencesNotification.checkPreferences(prefMng, context);
        event._eventPreferencesApplication.checkPreferences(prefMng, context);
        event._eventPreferencesLocation.checkPreferences(prefMng, context);

        Preference notificationAccessPreference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
        if (notificationAccessPreference != null) {
            //notificationAccessPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            notificationAccessPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                    return false;
                }
            });
        }
        Preference accessibilityPreference = prefMng.findPreference(PREF_ACCESSIBILITY_SETTINGS);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
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

    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        /*
        if (actionMode != null)
        {
            restart = false; // nerestartovat fragment
            actionMode.finish();
        }
        */

    }

    @Override
    public void onDestroy()
    {
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        event = null;

        if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
        
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
            if (changedLocationGeofencePreference != null) {
                if(resultCode == Activity.RESULT_OK){
                    long geofenceId = data.getLongExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, 0);
                    changedLocationGeofencePreference.setGeofenceFromEditor(geofenceId);
                }
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void updateSharedPreference()
    {
        if (event != null) 
        {	

            // updating activity with selected event preferences

            event.setAllSummary(prefMng, preferences, context);

        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {

        //eventTypeChanged = false;

        event.setSummary(prefMng, key, sharedPreferences, context);

        //Activity activity = getActivity();
        //boolean canShow = (EditorProfilesActivity.mTwoPane) && (activity instanceof EditorProfilesActivity);
        //canShow = canShow || ((!EditorProfilesActivity.mTwoPane) && (activity instanceof EventPreferencesFragmentActivity));
        //if (canShow)
        //    showActionMode();
        EventPreferencesFragmentActivity activity = (EventPreferencesFragmentActivity)getActivity();
        EventPreferencesFragmentActivity.showSaveMenu = true;
        activity.invalidateOptionsMenu();
    }

    /*
    public boolean onPreferenceScreenClick(PreferenceScreen preference) {
        boolean click = super.onPreferenceScreenClick(preference);
        updateSharedPreference();
        return click;
    }
    */

    static public Activity getPreferencesActivity()
    {
        return preferencesActivity;
    }

    static public void setChangedLocationGeofencePreference(LocationGeofencePreference changedLocationGeofencePref)
    {
        changedLocationGeofencePreference = changedLocationGeofencePref;
    }

}
