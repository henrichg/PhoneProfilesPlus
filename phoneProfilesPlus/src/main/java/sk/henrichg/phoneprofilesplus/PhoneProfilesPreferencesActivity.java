package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
    private int wifiScanInterval;
    private int bluetoothScanInterval;
    private int locationScanInterval;
    //private String activeBackgroundProfile;

    private boolean invalidateEditor = false;

    private PhoneProfilesPreferencesNestedFragment fragment;

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

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            if (ApplicationPreferences.applicationTheme(getApplicationContext()).equals("material"))
                tintManager.setStatusBarTintColor(Color.parseColor("#ff237e9f"));
            else
                tintManager.setStatusBarTintColor(Color.parseColor("#ff202020"));
        }
        else
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ApplicationPreferences.applicationTheme(getApplicationContext()).equals("material"))
                getWindow().setStatusBarColor(Color.parseColor("#1d6681"));
            else
                getWindow().setStatusBarColor(Color.parseColor("#141414"));
        }

        invalidateEditor = false;

        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle(R.string.title_activity_phone_profiles_preferences);


        ApplicationPreferences.getSharedPreferences(this);
        activeLanguage = ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_LANGUAGE, "system");
        activeTheme = ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_THEME, "material");
        showEditorPrefIndicator = ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
        showEditorHeader = ApplicationPreferences.preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_HEADER, true);
        wifiScanInterval = Integer.valueOf(ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, "10"));
        bluetoothScanInterval = Integer.valueOf(ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, "10"));
        locationScanInterval = Integer.valueOf(ApplicationPreferences.preferences.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, "5"));

        fragment = createFragment(false);

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
            // activity is started from lock screen, scroll to notifications category
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
        if (fragment != null)
            fragment.doOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        final DataWrapper dataWrapper =  new DataWrapper(getApplicationContext(), true, false, 0);
        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, getApplicationContext());

        Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_CLEAR_SERVICE_FOREGROUND, true);
        //TODO Android O
        // if (Build.VERSION.SDK_INT < 26)
        startService(serviceIntent);
        //else
        //    startForegroundService(serviceIntent);

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_SET_SERVICE_FOREGROUND, true);
                //TODO Android O
                // if (Build.VERSION.SDK_INT < 26)
                startService(serviceIntent);
                //else
                //    startForegroundService(serviceIntent);
            }
        }, 500);
        dataWrapper.getActivateProfileHelper().updateWidget(true);
    }

    @Override
    public void finish() {
        Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, ApplicationPreferences.notificationStatusBar(this));
        Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, ApplicationPreferences.notificationStatusBarPermanent(this));
        Crashlytics.setBool(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar(this));

        if (!activeLanguage.equals(ApplicationPreferences.applicationLanguage(getApplicationContext())))
        {
            GlobalGUIRoutines.setLanguage(getBaseContext());
            invalidateEditor = true;
        }
        else
        if (!activeTheme.equals(ApplicationPreferences.applicationTheme(getApplicationContext())))
        {
            //EditorProfilesActivity.setTheme(this, false);
            invalidateEditor = true;
        }
        else
        if (showEditorPrefIndicator != ApplicationPreferences.applicationEditorPrefIndicator(getApplicationContext()))
        {
            invalidateEditor = true;
        }
        else
        if (showEditorHeader != ApplicationPreferences.applicationEditorHeader(getApplicationContext()))
        {
            invalidateEditor = true;
        }

        if (wifiScanInterval != ApplicationPreferences.applicationEventWifiScanInterval(getApplicationContext()))
        {
            if (PhoneProfilesService.instance != null)
                PhoneProfilesService.instance.scheduleWifiJob(true, true, true, false, false, false, false);
        }
        if (bluetoothScanInterval != ApplicationPreferences.applicationEventBluetoothScanInterval(getApplicationContext()))
        {
            if (PhoneProfilesService.instance != null)
                PhoneProfilesService.instance.scheduleBluetoothJob(true, true, true, false, false, false);
        }
        DataWrapper dataWrapper =  new DataWrapper(getApplicationContext(), false, false, 0);
        if (locationScanInterval != ApplicationPreferences.applicationEventLocationUpdateInterval(getApplicationContext()))
        {
            if (PhoneProfilesService.instance != null)
                PhoneProfilesService.instance.scheduleGeofenceScannerJob(true, true, true, false, false);
        }
        dataWrapper.invalidateDataWrapper();

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
