package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
 
public class EventPreferencesFragment extends EventPreferencesNestedFragment
                                        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    //private DataWrapper dataWrapper;
    //private Event event;
    //private boolean first_start_activity;
    //private int new_event_mode;
    //private int predefinedEventIndex;
    public static int startupSource;
    //private PreferenceManager prefMng;
    //private SharedPreferences preferences;
    private Context context;

    public static Activity preferencesActivity = null;
    public static LocationGeofencePreference changedLocationGeofencePreference;

    static final String PREFS_NAME_ACTIVITY = "event_preferences_activity";
    static final String PREFS_NAME_FRAGMENT = "event_preferences_fragment";

    /*
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
    */

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);

        preferencesActivity = getActivity();
        context = getActivity().getBaseContext();

        /*
        dataWrapper = new DataWrapper(context.getApplicationContext(), true, false, 0);

        long event_id = 0;

        // getting attached fragment data
        if (getArguments().containsKey(GlobalData.EXTRA_NEW_EVENT_MODE))
            new_event_mode = getArguments().getInt(GlobalData.EXTRA_NEW_EVENT_MODE);
        if (getArguments().containsKey(GlobalData.EXTRA_EVENT_ID))
            event_id = getArguments().getLong(GlobalData.EXTRA_EVENT_ID);
        predefinedEventIndex = getArguments().getInt(GlobalData.EXTRA_PREDEFINED_EVENT_INDEX);

        //event = EventPreferencesFragmentActivity.createEvent(context.getApplicationContext(), event_id, new_event_mode, predefinedEventIndex, true);
        event = new Event();
        */

        //Log.e("------------- EventPreferencesFragment", prefMng.getSharedPreferencesName());

        //if (savedInstanceState == null)
        //    loadPreferences();

        updateSharedPreference();

    }

    public static String getPreferenceName() {
        String PREFS_NAME;
        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        else
        if (startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
            PREFS_NAME = PREFS_NAME_FRAGMENT;
        else
            PREFS_NAME = PREFS_NAME_FRAGMENT;
        return PREFS_NAME;
    }

    @Override
    public void addPreferencesFromResource(int preferenceResId) {
        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(getPreferenceName());
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        super.addPreferencesFromResource(preferenceResId);
    }

    @Override
    public int addPreferencesFromResource() {
        return R.xml.event_preferences;
    }

    public void updateSharedPreference()
    {
        // updating activity with selected event preferences

        Event event = new Event();
        event.setAllSummary(prefMng, preferences, context);
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
