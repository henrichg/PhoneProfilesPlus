package sk.henrichg.phoneprofilesplus;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class PhoneProfilesPrefsActivity extends AppCompatActivity {

    boolean activityStarted = false;

    private boolean showEditorPrefIndicator;
    private boolean hideEditorHeaderOrBottomBar;
    //private String activeLanguage;
    private String activeTheme;
    //private String activeNightModeOffTheme;
    private boolean periodicScannerEnabled;
    private boolean locationScannerEnabled;
    private boolean wifiScannerEnabled;
    private boolean bluetoothScannerEnabled;
    private boolean orientationScannerEnabled;
    private boolean mobileCellScannerEnabled;
    private boolean notificationScannerEnabled;
    private int periodicScanInterval;
    private int wifiScanInterval;
    private int bluetoothScanInterval;
    private int locationScanInterval;
    private int orientationScanInterval;
    //private String activeDefaultProfile;
    private boolean useAlarmClockEnabled;

    private boolean fromFinish = false;
    private boolean invalidateEditor = false;

    //int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public static final String EXTRA_SCROLL_TO = "extra_phone_profile_preferences_scroll_to";
    //public static final String EXTRA_SCROLL_TO_TYPE = "extra_phone_profile_preferences_scroll_to_type";
    public static final String EXTRA_RESET_EDITOR = "reset_editor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false, false, false, false, true); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (doServiceStart) {
            finish();
            return;
        }
        else
        if (showNotStartedToast()) {
            finish();
            return;
        }

        activityStarted = true;

        Toolbar toolbar = findViewById(R.id.activity_preferences_toolbar);
        toolbar.setVisibility(View.GONE);
        toolbar = findViewById(R.id.activity_preferences_toolbar_no_subtitle);
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        invalidateEditor = false;

        SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(getApplicationContext());
        //activeLanguage = preferences.getString(ApplicationPreferences.PREF_APPLICATION_LANGUAGE, "system");
        String defaultValue = "white";
        if (Build.VERSION.SDK_INT >= 28)
            defaultValue = "night_mode";
        activeTheme = preferences.getString(ApplicationPreferences.PREF_APPLICATION_THEME, defaultValue);
        //activeNightModeOffTheme = preferences.getString(ApplicationPreferences.PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "white");
        showEditorPrefIndicator = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, true);
        hideEditorHeaderOrBottomBar = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR, true);
        //showEditorHeader = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_HEADER, true);

        periodicScannerEnabled = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING, false);
        locationScannerEnabled = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, false);
        wifiScannerEnabled = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, false);
        bluetoothScannerEnabled = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, false);
        orientationScannerEnabled = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, false);
        mobileCellScannerEnabled = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, false);
        notificationScannerEnabled = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, false);

        periodicScanInterval = Integer.parseInt(preferences.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL, "15"));
        wifiScanInterval = Integer.parseInt(preferences.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, "15"));
        bluetoothScanInterval = Integer.parseInt(preferences.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, "15"));
        locationScanInterval = Integer.parseInt(preferences.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, "15"));
        orientationScanInterval = Integer.parseInt(preferences.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, "10"));

        useAlarmClockEnabled = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_USE_ALARM_CLOCK, false);

        String extraScrollTo; //= null;
        Intent intent = getIntent();
        //String action = intent.getAction();
        /*if (intent.hasCategory(Notification.INTENT_CATEGORY_NOTIFICATION_PREFERENCES)) {
            // activity is started from notification, scroll to notifications category
            extraScrollTo = "categoryAppNotificationRoot";
            //extraScrollToType = "category";
        }*/
        /*else if ((action != null) && action.equals(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)) {
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

            int widgetType = 0;
            PackageManager packageManager = getApplicationContext().getPackageManager();
            AppWidgetProviderInfo providerInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
            if ((packageManager != null) && (providerInfo != null)) {
                String widgetLabel = providerInfo.loadLabel(packageManager);
                if (widgetLabel != null) {
                    if (widgetLabel.equals(getString(R.string.widget_label_icon)))
                        widgetType = PPApplication.WIDGET_TYPE_ICON;
                    else
                    if (widgetLabel.equals(getString(R.string.widget_label_one_row)))
                        widgetType = PPApplication.WIDGET_TYPE_ONE_ROW;
                    else
                    if (widgetLabel.equals(getString(R.string.widget_label_list)))
                        widgetType = PPApplication.WIDGET_TYPE_LIST;
                }
            }

            if (widgetType == PPApplication.WIDGET_TYPE_ICON)
                extraScrollTo = "categoryWidgetIconRoot";
            else
            if (widgetType == PPApplication.WIDGET_TYPE_ONE_ROW)
                extraScrollTo = "categoryWidgetOneRowRoot";
            else
            if (widgetType == PPApplication.WIDGET_TYPE_LIST)
                extraScrollTo = "categoryWidgetListRoot";
        }*/
        //else {
            extraScrollTo = intent.getStringExtra(EXTRA_SCROLL_TO);
            //extraScrollToType = intent.getStringExtra(EXTRA_SCROLL_TO_TYPE);
        //}

        PhoneProfilesPrefsFragment preferenceFragment = new PhoneProfilesPrefsRoot();
        if (extraScrollTo != null) {
            switch (extraScrollTo) {
                case "categorySystemRoot":
                    preferenceFragment = new PhoneProfilesPrefsSystem();
                    break;
                case "categoryPermissionsRoot":
                    preferenceFragment = new PhoneProfilesPrefsPermissions();
                    break;
                case "profileActivationCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsProfileActivation();
                    break;
                case "eventRunCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsEventRun();
                    break;
                case "categoryAppNotificationRoot":
                    preferenceFragment = new PhoneProfilesPrefsAppNotification();
                    break;
                case "specialProfileParametersCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsSpecialProfileParameters();
                    break;
                case "periodicScanningCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsPeriodicScanning();
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
                case "notificationScanningCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsNotificationScanning();
                    break;
                case "applicationInterfaceCategoryRoot":
                    preferenceFragment = new PhoneProfilesPrefsInterface();
                    break;
                case "categoryApplicationStartRoot":
                    preferenceFragment = new PhoneProfilesPrefsApplicationStart();
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
                case "categoryShortcutRoot":
                    preferenceFragment = new PhoneProfilesPrefsShortcut();
                    break;
                case "categorySamsungEdgePanelRoot":
                    preferenceFragment = new PhoneProfilesPrefsSamsungEdgePanel();
                    break;
                case "categoryWidgetOneRowProfileListRoot":
                    preferenceFragment = new PhoneProfilesPrefsWidgetOneRowProfileList();
                    break;
                case "categoryProfileListNotificationRoot":
                    preferenceFragment = new PhoneProfilesPrefsProfileListNotification();
                    break;
            }
            //preferenceFragment.scrollToSet = true;
        }

        if (savedInstanceState == null) {
            Permissions.saveAllPermissions(getApplicationContext(), false);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_preferences_settings, preferenceFragment)
                    .commit();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (doServiceStart) {
            if (!isFinishing())
                finish();
            return;
        }
        else
        if (showNotStartedToast()) {
            if (!isFinishing())
                finish();
            return;
        }

        if (!activityStarted) {
            if (!isFinishing())
                finish();
        } else {
            Permissions.grantNotificationsPermission(this);
        }
    }

    @SuppressWarnings("SameReturnValue")
    private boolean showNotStartedToast() {
        PPApplicationStatic.setApplicationFullyStarted(getApplicationContext());
//        PPApplicationStatic.logE("[APPLICATION_FULLY_STARTED] PhoneProfilesPrefsActivity.showNotStartedToast", "xxx");
        return false;
/*        boolean applicationStarted = PPApplicationStatic.getApplicationStarted(true);
        boolean fullyStarted = PPApplication.applicationFullyStarted;
        if (!applicationStarted) {
            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_not_started);
            PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            return true;
        }
        if (!fullyStarted) {
            if ((PPApplication.startTimeOfApplicationStart > 0) &&
                    ((Calendar.getInstance().getTimeInMillis() - PPApplication.startTimeOfApplicationStart) > PPApplication.APPLICATION_START_DELAY)) {
                Intent activityIntent = new Intent(this, WorkManagerNotWorkingActivity.class);
                // clear all opened activities
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(activityIntent);
            }
            else {
                String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_starting_toast);
                PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            }
            return true;
        }
        return false;*/
    }

    @SuppressWarnings("SameReturnValue")
    private boolean startPPServiceWhenNotStarted() {
        if (PPApplicationStatic.getApplicationStopping(getApplicationContext())) {
            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_stopping_toast);
            PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            return true;
        }

        boolean serviceStarted = GlobalUtils.isServiceRunning(getApplicationContext(), PhoneProfilesService.class, false);
        if (!serviceStarted) {
            //AutostartPermissionNotification.showNotification(getApplicationContext(), true);

            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplicationStatic.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
//            PPApplicationStatic.logE("[START_PP_SERVICE] PhoneProfilesPrefsActivity.startPPServiceWhenNotStarted", "(1)");
            PPApplicationStatic.startPPService(this, serviceIntent);
            //return true;
        } /*else {
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                //return true;
            }
        }*/

        return false;
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

        if (requestCode == Permissions.NOTIFICATIONS_PERMISSION_REQUEST_CODE)
        {
            ActivityManager.RunningServiceInfo serviceInfo = GlobalUtils.getServiceInfo(getApplicationContext(), PhoneProfilesService.class);
            if (serviceInfo == null)
                startPPServiceWhenNotStarted();
            else {
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] ActivatorActivity.onActivityResult", "call of PPAppNotification.drawNotification");
                ProfileListNotification.drawNotification(true, getApplicationContext());
                DrawOverAppsPermissionNotification.showNotification(getApplicationContext(), true);
                IgnoreBatteryOptimizationNotification.showNotification(getApplicationContext(), true);
                sk.henrichg.phoneprofilesplus.PPAppNotification.drawNotification(true, getApplicationContext());
            }
        }
        else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_preferences_settings);
            if (fragment != null)
                ((PhoneProfilesPrefsFragment) fragment).doOnActivityResult(requestCode, resultCode);
        }
    }

    @Override
    protected void onStop() {
        if (activityStarted) {
            if (!fromFinish)
                doPreferenceChanges();
        }

        super.onStop();
    }

    @Override
    public void finish() {
        // finish is called before of onStop()

        fromFinish = true;

        Intent returnIntent = new Intent();
        if (activityStarted) {
            doPreferenceChanges();

            // for startActivityForResult
            returnIntent.putExtra(PhoneProfilesPrefsActivity.EXTRA_RESET_EDITOR, invalidateEditor);
            Permissions.grantRootChanged = false;

            //if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            //    Intent resultValue = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            //    setResult(RESULT_OK, resultValue);
            //} else
                setResult(RESULT_OK, returnIntent);
        }
        else {
            Permissions.grantRootChanged = false;
            setResult(RESULT_CANCELED);//, returnIntent);
        }

        super.finish();
    }

    private void doPreferenceChanges() {
        final Context appContext = getApplicationContext();

        PhoneProfilesPrefsFragment fragment = (PhoneProfilesPrefsFragment)getSupportFragmentManager().findFragmentById(R.id.activity_preferences_settings);
        if (fragment != null) {
            fragment.updateSharedPreferences();
        }

        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
        if (wifiScannerEnabled != ApplicationPreferences.applicationEventWifiEnableScanning)
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, false);
        if (bluetoothScannerEnabled != ApplicationPreferences.applicationEventBluetoothEnableScanning)
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, false);
        if (locationScannerEnabled != ApplicationPreferences.applicationEventLocationEnableScanning)
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, false);
        if (mobileCellScannerEnabled != ApplicationPreferences.applicationEventMobileCellEnableScanning) {
//            PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesPrefsActivity.doPreferenceChanges", "******** ### ******* (1)");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, false);
        }
        if (orientationScannerEnabled != ApplicationPreferences.applicationEventOrientationEnableScanning) {
//            PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesPrefsActivity.doPreferenceChanges", "******** ### ******* (1)");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, false);
        }
        if (notificationScannerEnabled != ApplicationPreferences.applicationEventNotificationEnableScanning)
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_DISABLED_SCANNING_BY_PROFILE, false);
        if (periodicScannerEnabled != ApplicationPreferences.applicationEventPeriodicScanningEnableScanning)
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_DISABLED_SCANNING_BY_PROFILE, false);
        editor.apply();

        PPApplicationStatic.loadApplicationPreferences(getApplicationContext());

        sk.henrichg.phoneprofilesplus.PPAppNotification.forceDrawNotificationFromSettings(appContext);

        // !! must be after PPApplication.loadApplicationPreferences()
        if (ApplicationPreferences.notificationProfileListDisplayNotification)
            ProfileListNotification.enable(true, getApplicationContext());
        else
            ProfileListNotification.disable(getApplicationContext());

       //try {
            /*if ((Build.VERSION.SDK_INT < 26)) {
                PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar);
            }*/
        PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING, ApplicationPreferences.applicationEventPeriodicScanningEnableScanning);
        PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL, ApplicationPreferences.applicationEventPeriodicScanningScanInterval);
        PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, ApplicationPreferences.applicationEventWifiEnableScanning);
        PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, ApplicationPreferences.applicationEventWifiScanInterval);
        PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, ApplicationPreferences.applicationEventBluetoothEnableScanning);
        PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, ApplicationPreferences.applicationEventBluetoothScanInterval);
        PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventLocationEnableScanning);
        PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, ApplicationPreferences.applicationEventLocationUpdateInterval);
        PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, ApplicationPreferences.applicationEventMobileCellEnableScanning);
        PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventOrientationEnableScanning);
        PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, ApplicationPreferences.applicationEventOrientationScanInterval);
        PPApplicationStatic.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventNotificationEnableScanning);
        //} catch (Exception e) {
            // https://github.com/firebase/firebase-android-sdk/issues/1226
            // PPApplicationStatic.recordException(e);
        //}

        DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
        dataWrapper.setDynamicLauncherShortcutsFromMainThread();

        /*
        if (PhoneProfilesService.getInstance() != null) {
            synchronized (PPApplication.applicationPreferencesMutex) {
                PPApplication.doNotShowProfileNotification = true;
            }
            PhoneProfilesService.getInstance().clearProfileNotification();
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(() -> {
//                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PhoneProfilesPrefsActivity.onStop", "PhoneProfilesService.getInstance()="+PhoneProfilesService.getInstance());
            if (PhoneProfilesService.getInstance() != null) {
                synchronized (PPApplication.applicationPreferencesMutex) {
                    PPApplication.doNotShowProfileNotification = false;
                }
                // forServiceStart must be true because of call of clearProfileNotification()
                PhoneProfilesService.getInstance().showProfileNotification(false, true, true);
            }
        }, 1000);*/
        //PhoneProfilesService.getInstance().showProfileNotification(false, true, true);

//        PPApplicationStatic.logE("[PPP_NOTIFICATION] PhoneProfilesPrefsActivity.doPreferenceChanges", "call of updateGUI");
        PPApplication.updateGUI(true, false, getApplicationContext());

        if (Permissions.grantRootChanged) {
            invalidateEditor = true;
        }

        boolean permissionsChanged = Permissions.getPermissionsChanged(appContext);

        if (permissionsChanged) {
            invalidateEditor = true;
        }

        /*if (!activeLanguage.equals(ApplicationPreferences.applicationLanguage(appContext)))
        {
            GlobalGUIRoutines.setLanguage(this);
            invalidateEditor = true;
        }*/
        if (!activeTheme.equals(ApplicationPreferences.applicationTheme(appContext, false)))
        {
            //EditorActivity.setTheme(this, false);
            GlobalGUIRoutines.switchNightMode(appContext, false);
            invalidateEditor = true;
        }
        /*if (!activeNightModeOffTheme.equals(ApplicationPreferences.applicationNightModeOffTheme(appContext)))
        {
            //EditorActivity.setTheme(this, false);
            invalidateEditor = true;
        }*/
        if (showEditorPrefIndicator != ApplicationPreferences.applicationEditorPrefIndicator)
        {
            invalidateEditor = true;
        }
        if (hideEditorHeaderOrBottomBar != ApplicationPreferences.applicationEditorHideHeaderOrBottomBar)
        {
            invalidateEditor = true;
        }
        /*if (showEditorHeader != ApplicationPreferences.applicationEditorHeader(appContext))
        {
            invalidateEditor = true;
        }*/

        if (permissionsChanged ||
                (periodicScannerEnabled != ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) ||
                periodicScanInterval != ApplicationPreferences.applicationEventPeriodicScanningScanInterval) {
            PPApplicationStatic.restartPeriodicScanningScanner(appContext);
        }

        if (permissionsChanged ||
                (wifiScannerEnabled != ApplicationPreferences.applicationEventWifiEnableScanning) ||
                (wifiScanInterval != ApplicationPreferences.applicationEventWifiScanInterval)) {
            PPApplicationStatic.restartWifiScanner(appContext);
        }

        if (permissionsChanged ||
                (bluetoothScannerEnabled != ApplicationPreferences.applicationEventBluetoothEnableScanning) ||
                (bluetoothScanInterval != ApplicationPreferences.applicationEventBluetoothScanInterval)) {
            PPApplicationStatic.restartBluetoothScanner(appContext);
        }

        if (permissionsChanged ||
                (locationScannerEnabled != ApplicationPreferences.applicationEventLocationEnableScanning) ||
                (locationScanInterval != ApplicationPreferences.applicationEventLocationUpdateInterval)) {
            PPApplicationStatic.restartLocationScanner(appContext);
        }

        if (permissionsChanged ||
                (orientationScannerEnabled != ApplicationPreferences.applicationEventOrientationEnableScanning) ||
                orientationScanInterval != ApplicationPreferences.applicationEventOrientationScanInterval) {
//            PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesPrefsActivity.doPreferenceChanges", "******** ### ******* (2)");
            PPApplicationStatic.restartOrientationScanner(appContext);
        }

        if (permissionsChanged ||
                (notificationScannerEnabled != ApplicationPreferences.applicationEventNotificationEnableScanning)) {
            PPApplicationStatic.restartNotificationScanner(appContext);
        }

        if (permissionsChanged ||
                mobileCellScannerEnabled != ApplicationPreferences.applicationEventMobileCellEnableScanning) {
//            PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesPrefsActivity.doPreferenceChanges", "******** ### ******* (2)");
            PPApplicationStatic.restartMobileCellsScanner(appContext);
        }

        if (permissionsChanged) {
            PPApplicationStatic.restartTwilightScanner(appContext);
        }

        if (useAlarmClockEnabled != ApplicationPreferences.applicationUseAlarmClock) {
            //PPApplication.startHandlerThreadBroadcast();
            //final Handler __handler2 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler2.post(new PPApplication.PPHandlerThreadRunnable(
            //        appContext) {
            //__handler2.post(() -> {
            Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PhoneProfilesPrefsActivity.doPreferenceChanges");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneProfilesPrefsActivity_doPreferenceChanges");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        /*if (DataWrapper.getIsManualProfileActivation(false, appContext))
                            x
                        else*/
                        // unblockEventsRun must be true to reset alarms
                        //PPApplication.restartEvents(appContext, true, true);

                        // change of this parameter is as change of local time
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PhoneProfilesPrefsActivity.doPreferenceChanges (2)");
                        TimeChangedReceiver.doWork(appContext, true);

                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            }; //);
            PPApplicationStatic.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);
        }

        /*
        if (activeDefaultProfile != PPApplication.applicationDefaultProfile)
        {
            long lApplicationDefaultProfile = Long.valueOf(PPApplication.applicationDefaultProfile);
            if (lApplicationDefaultProfile != PPApplication.PROFILE_NO_ACTIVATE)
            {
                DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);
                if (dataWrapper.getActivatedProfile() == null)
                {
                    dataWrapper.getActivateProfileHelper().initialize(dataWrapper, null, getApplicationContext());
                    dataWrapper.activateProfile(lApplicationDefaultProfile, PPApplication.STARTUP_SOURCE_EVENT, null, "");
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
        PPApplicationStatic.runCommand(this, commandIntent);

        //if (PhoneProfilesService.getInstance() != null) {
                    /*
                    boolean powerSaveMode = PPApplication.isPowerSaveMode;
                    if ((PhoneProfilesService.isLocationScannerStarted())) {
                        PhoneProfilesService.getLocationScanner().resetLocationUpdates(powerSaveMode, true);
                    }
                    PhoneProfilesService.getInstance().resetListeningOrientationSensors(powerSaveMode, true);
                    if (PhoneProfilesService.isMobileCellsScannerStarted())
                        PhoneProfilesService.mobileCellsScanner.resetListening(powerSaveMode, true);
                    */
        //}
    }

}
