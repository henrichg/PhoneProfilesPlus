package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public class PhoneProfilesPrefsActivity extends AppCompatActivity {

    private boolean showEditorPrefIndicator;
    //private boolean showEditorHeader;
    private String activeLanguage;
    private String activeTheme;
    //private String activeNightModeOffTheme;
    private boolean locationScannerEnabled;
    private boolean wifiScannerEnabled;
    private boolean bluetoothScannerEnabled;
    private boolean orientationScannerEnabled;
    private boolean mobileCellScannerEnabled;
    private int wifiScanInterval;
    private int bluetoothScanInterval;
    private int locationScanInterval;
    private int orientationScanInterval;
    //private String activeBackgroundProfile;
    private boolean useAlarmClockEnabled;

    private boolean invalidateEditor = false;

    public static final String EXTRA_SCROLL_TO = "extra_phone_profile_preferences_scroll_to";
    //public static final String EXTRA_SCROLL_TO_TYPE = "extra_phone_profile_preferences_scroll_to_type";
    public static final String EXTRA_RESET_EDITOR = "reset_editor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, true/*, false*/, false); // must by called before super.onCreate()
        GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        PPApplication.logE("PhoneProfilesPrefsActivity.onCreate", "savedInstanceState="+savedInstanceState);

        setContentView(R.layout.activity_preferences);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name)));

        Toolbar toolbar = findViewById(R.id.activity_preferences_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        invalidateEditor = false;

        ApplicationPreferences.getSharedPreferences(this);
        activeLanguage = ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_LANGUAGE, "system");
        String defaultValue = "white";
        if (Build.VERSION.SDK_INT >= 28)
            defaultValue = "night_mode";
        activeTheme = ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_THEME, defaultValue);
        //activeNightModeOffTheme = ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "white");
        showEditorPrefIndicator = ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
        //showEditorHeader = ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_HEADER, true);

        locationScannerEnabled = ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, true);
        wifiScannerEnabled = ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, true);
        bluetoothScannerEnabled = ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, true);
        orientationScannerEnabled = ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, true);
        mobileCellScannerEnabled = ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, true);

        wifiScanInterval = Integer.valueOf(ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, "15"));
        bluetoothScanInterval = Integer.valueOf(ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, "15"));
        locationScanInterval = Integer.valueOf(ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, "15"));
        orientationScanInterval = Integer.valueOf(ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, "10"));

        useAlarmClockEnabled = ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_USE_ALARM_CLOCK, false);

        String extraScrollTo;
        Intent intent = getIntent();
        if (intent.hasCategory(Notification.INTENT_CATEGORY_NOTIFICATION_PREFERENCES)) {
            // activity is started from notification, scroll to notifications category
            extraScrollTo = "categoryNotificationsRoot";
            //extraScrollToType = "category";
        }
        else {
            extraScrollTo = intent.getStringExtra(EXTRA_SCROLL_TO);
            //extraScrollToType = intent.getStringExtra(EXTRA_SCROLL_TO_TYPE);
        }

        PhoneProfilesPrefsFragment preferenceFragment = new PhoneProfilesPrefsRoot();
        if (extraScrollTo != null) {
            switch (extraScrollTo) {
                case "applicationInterfaceCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsInterface();
                    break;
                case "categoryApplicationStartRoot":
                    preferenceFragment = new PhoneProfilesPrefsApplicationStart();
                    break;
                case "categorySystemRoot":
                    preferenceFragment = new PhoneProfilesPrefsSystem();
                    break;
                case "categoryPermissionsRoot":
                    preferenceFragment = new PhoneProfilesPrefsPermissions();
                    break;
                case "categoryNotificationsRoot":
                    preferenceFragment = new PhoneProfilesPrefsNotifications();
                    break;
                case "profileActivationCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsProfileActivation();
                    break;
                case "eventRunCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsEventRun();
                    break;
                case "locationScanningCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsLocationScanning();
                    break;
                case "wifiScanningCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsWifiScanning();
                    break;
                case "bluetoothScanningCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsBluetoothScanning();
                    break;
                case "mobileCellsScanningCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsMobileCellsScanning();
                    break;
                case "orientationScanningCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsOrientationScanning();
                    break;
                case "categoryActivatorRoot":
                    preferenceFragment = new PhoneProfilesPrefsActivator();
                    break;
                case "categoryEditorRoot":
                    preferenceFragment = new PhoneProfilesPrefsEditor();
                    break;
                case "categoryWidgetListRoot":
                    preferenceFragment = new PhoneProfilesPrefsWidgetList();
                    break;
                case "categoryWidgetOneRowRoot":
                    preferenceFragment = new PhoneProfilesPrefsWidgetOneRow();
                    break;
                case "categoryWidgetIconRoot":
                    preferenceFragment = new PhoneProfilesPrefsWidgetIcon();
                    break;
                case "categorySamsungEdgePanelRoot":
                    preferenceFragment = new PhoneProfilesPrefsSamsungEdgePanel();
                    break;
            }
            //preferenceFragment.scrollToSet = true;
        }

        if (savedInstanceState == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                Permissions.saveAllPermissions(getApplicationContext(), false);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_preferences_settings, preferenceFragment)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PPApplication.logE("PhoneProfilesPrefsActivity.onStart", "xxx");
        //GlobalGUIRoutines.lockScreenOrientation(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PPApplication.logE("PhoneProfilesPrefsActivity.onStop", "xxx");

        if (PhoneProfilesService.getInstance() != null)
            PhoneProfilesService.getInstance().clearProfileNotification();

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (PhoneProfilesService.getInstance() != null)
                    PhoneProfilesService.getInstance().showProfileNotification(true, true);
            }
        }, 500);
        PPApplication.logE("ActivateProfileHelper.updateGUI", "from PhoneProfilesPrefsActivity.onStop");
        ActivateProfileHelper.updateGUI(getApplicationContext(), true, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_preferences_settings);
        if (fragment != null)
            ((PhoneProfilesPrefsFragment)fragment).doOnActivityResult(requestCode, resultCode);
    }

    @Override
    public void finish() {
        PPApplication.logE("PhoneProfilesPrefsActivity.finish", "xxx");

        doPreferenceChanges();

        // for startActivityForResult
        Intent returnIntent = new Intent();
        returnIntent.putExtra(PhoneProfilesPrefsActivity.EXTRA_RESET_EDITOR, invalidateEditor);
        setResult(RESULT_OK,returnIntent);

        super.finish();
    }

    private void doPreferenceChanges() {
        Context appContext = getApplicationContext();

        PhoneProfilesPrefsFragment fragment = (PhoneProfilesPrefsFragment)getSupportFragmentManager().findFragmentById(R.id.activity_preferences_settings);
        if (fragment != null) {
            fragment.updateSharedPreferences();
        }

        try {
            if ((Build.VERSION.SDK_INT < 26)) {
                //Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, ApplicationPreferences.notificationStatusBar(this));
                //Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, ApplicationPreferences.notificationStatusBarPermanent(this));
                Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar(this));
            }
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, ApplicationPreferences.applicationEventWifiEnableScanning(appContext));
            Crashlytics.setInt(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, ApplicationPreferences.applicationEventWifiScanInterval(appContext));
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, ApplicationPreferences.applicationEventBluetoothEnableScanning(appContext));
            Crashlytics.setInt(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, ApplicationPreferences.applicationEventBluetoothScanInterval(appContext));
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventLocationEnableScanning(appContext));
            Crashlytics.setInt(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, ApplicationPreferences.applicationEventLocationUpdateInterval(appContext));
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, ApplicationPreferences.applicationEventMobileCellEnableScanning(appContext));
            Crashlytics.setBool(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventOrientationEnableScanning(appContext));
            Crashlytics.setInt(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, ApplicationPreferences.applicationEventOrientationScanInterval(appContext));
        } catch (Exception ignored) {}

        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        if (wifiScannerEnabled != ApplicationPreferences.applicationEventWifiEnableScanning(appContext))
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, false);
        if (bluetoothScannerEnabled != ApplicationPreferences.applicationEventBluetoothEnableScanning(appContext))
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, false);
        if (locationScannerEnabled != ApplicationPreferences.applicationEventLocationEnableScanning(appContext))
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, false);
        if (mobileCellScannerEnabled != ApplicationPreferences.applicationEventMobileCellEnableScanning(appContext))
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, false);
        if (orientationScannerEnabled != ApplicationPreferences.applicationEventOrientationEnableScanning(appContext))
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, false);
        editor.apply();

        boolean permissionsChanged = Permissions.getPermissionsChanged(appContext);
        if (permissionsChanged) {
            PPApplication.logE("PhoneProfilesPrefsActivity.doPreferenceChanges", "permissions changed");
            invalidateEditor = true;
        }

        if (!activeLanguage.equals(ApplicationPreferences.applicationLanguage(appContext)))
        {
            PPApplication.logE("PhoneProfilesPrefsActivity.doPreferenceChanges", "language changed");
            GlobalGUIRoutines.setLanguage(this);
            invalidateEditor = true;
        }
        else
        if (!activeTheme.equals(ApplicationPreferences.applicationTheme(appContext, false)))
        {
            PPApplication.logE("PhoneProfilesPrefsActivity.doPreferenceChanges", "theme changed");
            //EditorProfilesActivity.setTheme(this, false);
            GlobalGUIRoutines.switchNightMode(appContext, false);
            invalidateEditor = true;
        }
        /*else
        if (!activeNightModeOffTheme.equals(ApplicationPreferences.applicationNightModeOffTheme(appContext)))
        {
            //EditorProfilesActivity.setTheme(this, false);
            invalidateEditor = true;
        }*/
        else
        if (showEditorPrefIndicator != ApplicationPreferences.applicationEditorPrefIndicator(appContext))
        {
            PPApplication.logE("PhoneProfilesPrefsActivity.doPreferenceChanges", "show editor pref. indicator changed");
            invalidateEditor = true;
        }
        /*else
        if (showEditorHeader != ApplicationPreferences.applicationEditorHeader(appContext))
        {
            invalidateEditor = true;
        }*/

        if (permissionsChanged ||
                (wifiScannerEnabled != ApplicationPreferences.applicationEventWifiEnableScanning(appContext)) ||
                (wifiScanInterval != ApplicationPreferences.applicationEventWifiScanInterval(appContext))) {
            PPApplication.restartWifiScanner(appContext, false);
        }

        if (permissionsChanged ||
                (bluetoothScannerEnabled != ApplicationPreferences.applicationEventBluetoothEnableScanning(appContext)) ||
                (bluetoothScanInterval != ApplicationPreferences.applicationEventBluetoothScanInterval(appContext))) {
            PPApplication.restartBluetoothScanner(appContext, false);
        }

        if (permissionsChanged ||
                (locationScannerEnabled != ApplicationPreferences.applicationEventLocationEnableScanning(appContext)) ||
                (locationScanInterval != ApplicationPreferences.applicationEventLocationUpdateInterval(appContext))) {
            PPApplication.restartGeofenceScanner(appContext, false);
        }

        if (permissionsChanged ||
                (orientationScannerEnabled != ApplicationPreferences.applicationEventOrientationEnableScanning(appContext)) ||
                orientationScanInterval != ApplicationPreferences.applicationEventOrientationScanInterval(appContext)) {
            PPApplication.restartOrientationScanner(appContext);
        }

        if (permissionsChanged ||
                mobileCellScannerEnabled != ApplicationPreferences.applicationEventMobileCellEnableScanning(appContext)) {
            PPApplication.restartPhoneStateScanner(appContext, false);
        }

        if (permissionsChanged) {
            PPApplication.restartTwilightScanner(appContext);
        }

        if (useAlarmClockEnabled != ApplicationPreferences.applicationUseAlarmClock(appContext)) {
            PPApplication.logE("PhoneProfilesPrefsActivity.doPreferenceChanges", "use alarm clock enabled changed");
            /*if (DataWrapper.getIsManualProfileActivation(false, appContext))
                x
            else*/
                // unblockEventsRun must be true to reset alarms
                PPApplication.restartEvents(appContext, true, true);
        }

        /*
        if (activeBackgroundProfile != PPApplication.applicationBackgroundProfile)
        {
            long lApplicationBackgroundProfile = Long.valueOf(PPApplication.applicationBackgroundProfile);
            if (lApplicationBackgroundProfile != PPApplication.PROFILE_NO_ACTIVATE)
            {
                DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);
                if (dataWrapper.getActivatedProfile() == null)
                {
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, null, getApplicationContext());
                    dataWrapper.activateProfile(lApplicationBackgroundProfile, PPApplication.STARTUP_SOURCE_SERVICE, null, "");
                }
                //invalidateEditor = true;
            }
        }
        */

        //GlobalGUIRoutines.unlockScreenOrientation(this);

        /*Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
        PPApplication.startPPService(this, serviceIntent);*/
        Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
        //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
        PPApplication.runCommand(this, commandIntent);

        //if (PhoneProfilesService.getInstance() != null) {
                    /*
                    boolean powerSaveMode = PPApplication.isPowerSaveMode;
                    if ((PhoneProfilesService.isGeofenceScannerStarted())) {
                        PhoneProfilesService.getGeofencesScanner().resetLocationUpdates(powerSaveMode, true);
                    }
                    PhoneProfilesService.getInstance().resetListeningOrientationSensors(powerSaveMode, true);
                    if (PhoneProfilesService.isPhoneStateScannerStarted())
                        PhoneProfilesService.phoneStateScanner.resetListening(powerSaveMode, true);
                    */
        //}
    }


//--------------------------------------------------------------------------------------------------

    static public class PhoneProfilesPrefsRoot extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsRoot");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_root, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsRoot");
        }

    }

    static public class PhoneProfilesPrefsInterface extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsInterface");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_interface, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsInterface");
            editor.putString(ApplicationPreferences.PREF_APPLICATION_LANGUAGE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_LANGUAGE, "system"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_HOME_LAUNCHER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_HOME_LAUNCHER, "activator"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER, "activator"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER, "activator"));
            String defaultValue = "white";
            if (Build.VERSION.SDK_INT >= 28)
                defaultValue = "night_mode";
            editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_THEME, defaultValue));
            //editor.putString(ApplicationPreferences.PREF_APPLICATION_NIGHT_MODE_OFF_THEME, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "white"));
        }

    }

    static public class PhoneProfilesPrefsApplicationStart extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsApplicationStart");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_application_start, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsApplicationStart");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT, true));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATE, true));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_START_EVENTS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_START_EVENTS, true));
        }

    }

    static public class PhoneProfilesPrefsSystem extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsSystem");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_system, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsSystem");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, "0"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_USE_ALARM_CLOCK, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_USE_ALARM_CLOCK, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL, "3"));
        }

    }

    static public class PhoneProfilesPrefsPermissions extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsPermissions");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_permissions, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsPermissions");
        }

    }

    static public class PhoneProfilesPrefsNotifications extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsNotifications");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_notifications, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsNotifications");
            //editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_TOAST, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_TOAST, true));
            //editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true));
            //editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true));
            //editor.putString(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_CANCEL, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_CANCEL, "10"));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, true));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, false));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE, "0"));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE, "1"));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR, true));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "0"));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR, "0"));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, true));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT, false));
            //PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsNotifications custom color="+fromPreference.getInt(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, 0xFFFFFFFF));
            editor.putInt(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, fromPreference.getInt(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, 0xFFFFFFFF));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE, false));
        }

    }

    static public class PhoneProfilesPrefsProfileActivation extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsProfileActivation");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_profile_activation, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsProfileActivation");
            editor.putString(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE, "-999"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_USAGE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_USAGE, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_SOUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_SOUND, ""));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_VIBRATE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_VIBRATE, false));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ALERT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ALERT, true));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_TOAST, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_TOAST, true));
        }

    }

    static public class PhoneProfilesPrefsEventRun extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsEventRun");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_event_run, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsEventRun");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, false));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT, true));
        }
    }

    static public class PhoneProfilesPrefsLocationScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsLocationScanning");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_location_scanning, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsLocationScanning");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, "15"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE, "1"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN, "1"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_USE_GPS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_USE_GPS, false));
        }

    }

    static public class PhoneProfilesPrefsWifiScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsWifiScanning");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_wifi_scanning, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsWifiScanning");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, false));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF, true));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, "15"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE, "1"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN, "1"));
        }
    }

    static public class PhoneProfilesPrefsBluetoothScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsBluetoothScanning");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_bluetooth_scanning, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsBluetoothScanning");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, false));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF, true));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, "15"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION, "10"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE, "1"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "1"));
        }
    }

    static public class PhoneProfilesPrefsMobileCellsScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsMobileCellsScanning");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_mobile_cells_scanning, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsMobileCellsScanning");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE, "1"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "1"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED, true));
        }
    }

    static public class PhoneProfilesPrefsOrientationScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsOrientationScanning");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_orientation_scanning, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsOrientationScanning");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, "10"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE, "1"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, false));
        }
    }

    static public class PhoneProfilesPrefsActivator extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsActivator");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_activator, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsActivator");
            //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR, true));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, true));
            //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_HEADER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_HEADER, true));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION, false));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_CLOSE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_CLOSE, true));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, true));
        }
    }

    static public class PhoneProfilesPrefsEditor extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsEditor");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_editor, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsEditor");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, true));
            //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_HEADER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_HEADER, true));
            //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true));
            //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER, true));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, "7"));
        }
    }

    static public class PhoneProfilesPrefsWidgetList extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsWidgetList");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_widget_list, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsWidgetList");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_HEADER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_HEADER, true));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, true));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND, "25"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, "0"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, "-1"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, "100"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, true));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, "100"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, "0"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, "100"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, false));
        }
    }

    static public class PhoneProfilesPrefsWidgetOneRow extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsWidgetOneRow");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_widget_one_row, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsWidgetOneRow");
            //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, true));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, "25"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, "0"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, "-1"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, "100"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, true));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, "100"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, "0"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, "100"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS, false));
        }
    }

    static public class PhoneProfilesPrefsWidgetIcon extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsWidgetIcon");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_widget_icon, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsWidgetIcon");
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND, "25"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, "0"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, "-1"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, "100"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, true));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, "100"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR, "0"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, "100"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, false));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_EMBLEM, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_EMBLEM, true));
        }
    }

    static public class PhoneProfilesPrefsSamsungEdgePanel extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PPApplication.logE("PhoneProfilesPrefsFragment.onCreatePreferences", "from PhoneProfilesPrefsSamsungEdgePanel");

            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (getContext() != null) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_samsung_edge_panel, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            PPApplication.logE("PhoneProfilesPrefsFragment.updateSharedPreferences", "from PhoneProfilesPrefsSamsungEdgePanel");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_HEADER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_HEADER, true));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, "25"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, "0"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, "-1"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, "100"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, "0"));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, "100"));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, false));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION, "0"));
        }
    }

}
