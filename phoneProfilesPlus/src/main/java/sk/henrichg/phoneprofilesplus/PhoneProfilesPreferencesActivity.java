package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class PhoneProfilesPreferencesActivity extends PreferenceActivity
                        implements PreferenceFragment.OnCreateNestedPreferenceFragment
{
    private String extraScrollTo;
    //String extraScrollToType = "";

    private boolean showEditorPrefIndicator;
    private boolean showEditorHeader;
    private String activeLanguage;
    private String activeTheme;
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
    public static final String EXTRA_SCROLL_TO_TYPE = "extra_phone_profile_preferences_scroll_to_type";
    public static final String EXTRA_RESET_EDITOR = "reset_editor";

    @SuppressLint("InlinedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // must by called before super.onCreate() for PreferenceActivity
        GlobalGUIRoutines.setTheme(this, false, true, false);
        GlobalGUIRoutines.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_phone_profiles_preferences);

        if (/*(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) &&*/ (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (ApplicationPreferences.applicationTheme(getApplicationContext())) {
                case "color":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
                    break;
                case "white":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primaryDark19_white));
                    break;
                default:
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
                    break;
            }
        }

        invalidateEditor = false;

        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle(R.string.title_activity_phone_profiles_preferences);


        ApplicationPreferences.getSharedPreferences(this);
        activeLanguage = ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_LANGUAGE, "system");
        activeTheme = ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_THEME, "color");
        showEditorPrefIndicator = ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
        showEditorHeader = ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_HEADER, true);

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

        //Permissions.disablePermissionsChanged(this);

        PhoneProfilesPreferencesNestedFragment fragment = createFragment(false);

        setPreferenceFragment(fragment);
    }

    private PhoneProfilesPreferencesNestedFragment createFragment(boolean nested) {
        PhoneProfilesPreferencesNestedFragment fragment;
        if (nested)
            fragment = new PhoneProfilesPreferencesNestedFragment();
        else
            fragment = new PhoneProfilesPreferencesFragment();

        Intent intent = getIntent();
        if (intent.hasCategory(Notification.INTENT_CATEGORY_NOTIFICATION_PREFERENCES)) {
            // activity is started from notification, scroll to notifications category
            extraScrollTo = "categoryNotifications";
            //extraScrollToType = "category";
        }
        else {
            extraScrollTo = intent.getStringExtra(EXTRA_SCROLL_TO);
            //extraScrollToType = intent.getStringExtra(EXTRA_SCROLL_TO_TYPE);
        }

        Bundle args = new Bundle();
        args.putString(EXTRA_SCROLL_TO, extraScrollTo);
        //args.putString(EXTRA_SCROLL_TO_TYPE, extraScrollToType);
        args.putBoolean(PreferenceFragment.EXTRA_NESTED, nested);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        PreferenceFragment fragment = getFragment();
        if (fragment != null)
            ((PhoneProfilesPreferencesNestedFragment)fragment).doOnActivityResult(requestCode, resultCode);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_CLEAR_SERVICE_FOREGROUND, true);
        PPApplication.startPPService(this, serviceIntent);

        final Context context = this;
        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_SET_SERVICE_FOREGROUND, true);
                PPApplication.startPPService(context, serviceIntent);
            }
        }, 500);
        ActivateProfileHelper.updateGUI(getApplicationContext(), true);
    }

    @Override
    public void finish() {
        Context appContext = getApplicationContext();

        try {
            if ((Build.VERSION.SDK_INT < 26)) {
                Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, ApplicationPreferences.notificationStatusBar(this));
                Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, ApplicationPreferences.notificationStatusBarPermanent(this));
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
            invalidateEditor = true;
        }

        if (!activeLanguage.equals(ApplicationPreferences.applicationLanguage(appContext)))
        {
            GlobalGUIRoutines.setLanguage(getBaseContext());
            invalidateEditor = true;
        }
        else
        if (!activeTheme.equals(ApplicationPreferences.applicationTheme(appContext)))
        {
            //EditorProfilesActivity.setTheme(this, false);
            invalidateEditor = true;
        }
        else
        if (showEditorPrefIndicator != ApplicationPreferences.applicationEditorPrefIndicator(appContext))
        {
            invalidateEditor = true;
        }
        else
        if (showEditorHeader != ApplicationPreferences.applicationEditorHeader(appContext))
        {
            invalidateEditor = true;
        }

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

        if (useAlarmClockEnabled != ApplicationPreferences.applicationUseAlarmClock(appContext)) {
            // unblockEventsRun must be true to reset alarms
            PPApplication.restartEvents(appContext, true);
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

        // for startActivityForResult
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_RESET_EDITOR, invalidateEditor);
        setResult(RESULT_OK,returnIntent);

        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (extraScrollTo == null)
                    return super.onOptionsItemSelected(item);
                else {
                    finish();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        if (extraScrollTo != null)
            finish();
        else
            super.onBackPressed();
    }

    /*
    public void onPreferenceAttached(PreferenceScreen root, int xmlId) {

    }
    */

    @Override
    public PreferenceFragment onCreateNestedPreferenceFragment() {
        return createFragment(true);
    }

    // required for fix security vulnerability Fragment Injection
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PhoneProfilesPreferencesFragment.class.getName().equals(fragmentName) ||
                PhoneProfilesPreferencesNestedFragment.class.getName().equals(fragmentName);
    }
}
