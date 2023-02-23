package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

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
        GlobalGUIRoutines.setTheme(this, false, false/*, false*/, false, false, false, true); // must by called before super.onCreate()
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
        if (intent.hasCategory(Notification.INTENT_CATEGORY_NOTIFICATION_PREFERENCES)) {
            // activity is started from notification, scroll to notifications category
            extraScrollTo = "categoryAppNotificationRoot";
            //extraScrollToType = "category";
        }
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
        else {
            extraScrollTo = intent.getStringExtra(EXTRA_SCROLL_TO);
            //extraScrollToType = intent.getStringExtra(EXTRA_SCROLL_TO_TYPE);
        }

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
        PPApplication.setApplicationFullyStarted(getApplicationContext());
//        PPApplication.logE("[APPLICATION_FULLY_STARTED] PhoneProfilesPrefsActivity.showNotStartedToast", "xxx");
        return false;
/*        boolean applicationStarted = PPApplication.getApplicationStarted(true);
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
        // this is for list widget header
        boolean serviceStarted = GlobalUtils.isServiceRunning(getApplicationContext(), PhoneProfilesService.class, false);
        if (!serviceStarted) {
            AutostartPermissionNotification.showNotification(getApplicationContext(), true);

            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplication.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
//            PPApplication.logE("[START_PP_SERVICE] PhoneProfilesPrefsActivity.startPPServiceWhenNotStarted", "(1)");
            PPApplication.startPPService(this, serviceIntent);
            //return true;
        } else {
            //noinspection StatementWithEmptyBody
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                //return true;
            }
        }

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
//            PPApplication.logE("[PPP_NOTIFICATION] ActivatorActivity.onActivityResult", "call of PPPAppNotification.drawNotification");
                PPPAppNotification.drawNotification(true, getApplicationContext());
                ProfileListNotification.drawNotification(true, getApplicationContext());
                DrawOverAppsPermissionNotification.showNotification(getApplicationContext(), true);
                IgnoreBatteryOptimizationNotification.showNotification(getApplicationContext(), true);
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
//            PPApplication.logE("[TEST BATTERY] PhoneProfilesPrefsActivity.doPreferenceChanges", "******** ### ******* (1)");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, false);
        }
        if (orientationScannerEnabled != ApplicationPreferences.applicationEventOrientationEnableScanning) {
//            PPApplication.logE("[TEST BATTERY] PhoneProfilesPrefsActivity.doPreferenceChanges", "******** ### ******* (1)");
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, false);
        }
        if (notificationScannerEnabled != ApplicationPreferences.applicationEventNotificationEnableScanning)
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_DISABLED_SCANNING_BY_PROFILE, false);
        if (periodicScannerEnabled != ApplicationPreferences.applicationEventPeriodicScanningEnableScanning)
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_DISABLED_SCANNING_BY_PROFILE, false);
        editor.apply();

        PPApplication.loadApplicationPreferences(getApplicationContext());

        PPPAppNotification.forceDrawNotificationFromSettings(appContext);

        // !! must be after PPApplication.loadApplicationPreferences()
        if (ApplicationPreferences.notificationProfileListDisplayNotification)
            ProfileListNotification.enable(true, getApplicationContext());
        else
            ProfileListNotification.disable(getApplicationContext());

       //try {
            if ((Build.VERSION.SDK_INT < 26)) {
                PPApplication.setCustomKey(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar);
            }
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING, ApplicationPreferences.applicationEventPeriodicScanningEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL, ApplicationPreferences.applicationEventPeriodicScanningScanInterval);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, ApplicationPreferences.applicationEventWifiEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, ApplicationPreferences.applicationEventWifiScanInterval);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, ApplicationPreferences.applicationEventBluetoothEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, ApplicationPreferences.applicationEventBluetoothScanInterval);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventLocationEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, ApplicationPreferences.applicationEventLocationUpdateInterval);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, ApplicationPreferences.applicationEventMobileCellEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventOrientationEnableScanning);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, ApplicationPreferences.applicationEventOrientationScanInterval);
            PPApplication.setCustomKey(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, ApplicationPreferences.applicationEventNotificationEnableScanning);
        //} catch (Exception e) {
            // https://github.com/firebase/firebase-android-sdk/issues/1226
            // PPApplication.recordException(e);
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
//                PPApplication.logE("[IN_THREAD_HANDLER] PhoneProfilesPrefsActivity.onStop", "PhoneProfilesService.getInstance()="+PhoneProfilesService.getInstance());
            if (PhoneProfilesService.getInstance() != null) {
                synchronized (PPApplication.applicationPreferencesMutex) {
                    PPApplication.doNotShowProfileNotification = false;
                }
                // forServiceStart must be true because of call of clearProfileNotification()
                PhoneProfilesService.getInstance().showProfileNotification(false, true, true);
            }
        }, 1000);*/
        //PhoneProfilesService.getInstance().showProfileNotification(false, true, true);

//        PPApplication.logE("[PPP_NOTIFICATION] PhoneProfilesPrefsActivity.doPreferenceChanges", "call of updateGUI");
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
            PPApplication.restartPeriodicScanningScanner(appContext);
        }

        if (permissionsChanged ||
                (wifiScannerEnabled != ApplicationPreferences.applicationEventWifiEnableScanning) ||
                (wifiScanInterval != ApplicationPreferences.applicationEventWifiScanInterval)) {
            PPApplication.restartWifiScanner(appContext);
        }

        if (permissionsChanged ||
                (bluetoothScannerEnabled != ApplicationPreferences.applicationEventBluetoothEnableScanning) ||
                (bluetoothScanInterval != ApplicationPreferences.applicationEventBluetoothScanInterval)) {
            PPApplication.restartBluetoothScanner(appContext);
        }

        if (permissionsChanged ||
                (locationScannerEnabled != ApplicationPreferences.applicationEventLocationEnableScanning) ||
                (locationScanInterval != ApplicationPreferences.applicationEventLocationUpdateInterval)) {
            PPApplication.restartLocationScanner(appContext);
        }

        if (permissionsChanged ||
                (orientationScannerEnabled != ApplicationPreferences.applicationEventOrientationEnableScanning) ||
                orientationScanInterval != ApplicationPreferences.applicationEventOrientationScanInterval) {
//            PPApplication.logE("[TEST BATTERY] PhoneProfilesPrefsActivity.doPreferenceChanges", "******** ### ******* (2)");
            PPApplication.restartOrientationScanner(appContext);
        }

        if (permissionsChanged ||
                (notificationScannerEnabled != ApplicationPreferences.applicationEventNotificationEnableScanning)) {
            PPApplication.restartNotificationScanner(appContext);
        }

        if (permissionsChanged ||
                mobileCellScannerEnabled != ApplicationPreferences.applicationEventMobileCellEnableScanning) {
//            PPApplication.logE("[TEST BATTERY] PhoneProfilesPrefsActivity.doPreferenceChanges", "******** ### ******* (2)");
            PPApplication.restartMobileCellsScanner(appContext);
        }

        if (permissionsChanged) {
            PPApplication.restartTwilightScanner(appContext);
        }

        if (useAlarmClockEnabled != ApplicationPreferences.applicationUseAlarmClock) {
            //PPApplication.startHandlerThreadBroadcast();
            //final Handler __handler2 = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler2.post(new PPApplication.PPHandlerThreadRunnable(
            //        appContext) {
            //__handler2.post(() -> {
            Runnable runnable = () -> {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PhoneProfilesPrefsActivity.doPreferenceChanges");

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
//                            PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PhoneProfilesPrefsActivity.doPreferenceChanges (2)");
                        TimeChangedReceiver.doWork(appContext, true);

                    } catch (Exception e) {
//                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
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
            PPApplication.createBasicExecutorPool();
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
        PPApplication.runCommand(this, commandIntent);

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


//--------------------------------------------------------------------------------------------------

    static public class PhoneProfilesPrefsRoot extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_root, rootKey);
        }

        /*
        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        }
        */
    }

    static public class PhoneProfilesPrefsInterface extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)){
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_interface, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_THEME, ApplicationPreferences.applicationThemeDefaultValue()));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_HOME_LAUNCHER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_HOME_LAUNCHER, ApplicationPreferences.PREF_APPLICATION_HOME_LAUNCHER_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER, ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER, ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ICON_COLOR, ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ICON_COLOR_DEFAULT_VALUE));
            //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_INCREASE_BRIGHTNESS_FOR_PROFILE_ICON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_INCREASE_BRIGHTNESS_FOR_PROFILE_ICON, ApplicationPreferences.PREF_APPLICATION_INCREASE_BRIGHTNESS_FOR_PROFILE_ICON_DEFAULT_VALUE));
        }

    }

    static public class PhoneProfilesPrefsApplicationStart extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_application_start, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT, ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATE, ApplicationPreferences.PREF_APPLICATION_ACTIVATE_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_START_EVENTS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_START_EVENTS, ApplicationPreferences.PREF_APPLICATION_START_EVENTS_DEFAULT_VALUE));
        }

    }

    static public class PhoneProfilesPrefsSystem extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_system, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_USE_ALARM_CLOCK, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_USE_ALARM_CLOCK, ApplicationPreferences.PREF_APPLICATION_USE_ALARM_CLOCK_DEFAULT_VALUE));
        }

    }

    static public class PhoneProfilesPrefsPermissions extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_permissions, rootKey);
        }

        /*
        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
        }
        */
    }

    static public class PhoneProfilesPrefsAppNotification extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_app_notification, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE, ApplicationPreferences.notificationStatusBarStyleDefaultValue()));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_NOTIFICATION_STYLE, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_NOTIFICATION_STYLE, ApplicationPreferences.notificationNotificationStyleDefaultValue()));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE, ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, ApplicationPreferences.notificationUseDecorationDefaultValue()));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, fromPreference.getInt(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR, ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON, ApplicationPreferences.notificationShowProfileIconDefaultValue()));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS, ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR, ApplicationPreferences.notificationPrefIndicatorDefaultValue()));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS, ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON, ApplicationPreferences.notificationShowRestartEventsAsButtonDefaultValue()));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT, ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT_DEFAULT_VALUE));
        }

    }

    static public class PhoneProfilesPrefsProfileActivation extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_profile_activation, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE, ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND, ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE, ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ALERT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ALERT, ApplicationPreferences.PREF_APPLICATION_ALERT_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND, ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_VIBRATE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_VIBRATE, ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_VIBRATE_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_TOAST, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_TOAST, ApplicationPreferences.PREF_NOTIFICATION_TOAST_DEFAULT_VALUE));
        }

    }

    static public class PhoneProfilesPrefsSpecialProfileParameters extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_special_profile_parameters, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON, ApplicationPreferences.PREF_APPLICATION_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON_DEFAULT_VALUE));
        }

    }

    static public class PhoneProfilesPrefsEventRun extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_event_run, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY, ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT, ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsPeriodicScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_periodic_scanning, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING, ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL, ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_POWER_SAVE_MODE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_POWER_SAVE_MODE, ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY, ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_FROM, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_FROM, ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_TO, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_TO, ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_ONLY_WHEN_SCREEN_IS_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_ONLY_WHEN_SCREEN_IS_ON, ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsLocationScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_location_scanning, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE, ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY, ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_FROM, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_FROM, ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_TO, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_TO, ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_USE_GPS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_USE_GPS, ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_USE_GPS_DEFAULT_VALUE));
        }

    }

    static public class PhoneProfilesPrefsWifiScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_wifi_scanning, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF, ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCANNING_IGNORE_HOTSPOT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCANNING_IGNORE_HOTSPOT, ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCANNING_IGNORE_HOTSPOT_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE, ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY, ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_FROM, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_FROM, ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_TO, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_TO, ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON, ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsBluetoothScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_bluetooth_scanning, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF, ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION, ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE, ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY, ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_FROM, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_FROM, ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_TO, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_TO, ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON, ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsMobileCellsScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_mobile_cells_scanning, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_FROM, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_FROM, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_TO, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_TO, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON, ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsOrientationScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_orientation_scanning, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE, ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY, ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_FROM, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_FROM, ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_TO, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_TO, ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsNotificationScanning extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_notification_scanning, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_POWER_SAVE_MODE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_POWER_SAVE_MODE, ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY, ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_FROM, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_FROM, ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_TO, fromPreference.getInt(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_TO, ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsActivator extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_activator, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION, ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_CLOSE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_CLOSE, ApplicationPreferences.PREF_APPLICATION_CLOSE_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_NUM_COLUMNS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_NUM_COLUMNS, ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_NUM_COLUMNS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_ADD_RESTART_EVENTS_INTO_PROFILE_LIST, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_ADD_RESTART_EVENTS_INTO_PROFILE_LIST, ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_ADD_RESTART_EVENTS_INTO_PROFILE_LIST_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_INCREASE_BRIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_INCREASE_BRIGHTNESS, ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_INCREASE_BRIGHTNESS_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsEditor extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_editor, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR, ApplicationPreferences.PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsWidgetList extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_widget_list, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_HEADER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_HEADER, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_HEADER_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_COMPACT_GRID, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_COMPACT_GRID, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_COMPACT_GRID_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE, ApplicationPreferences.applicationWidgetListChangeColorsByNightModeDefaultValue(getContext())));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND, ApplicationPreferences.applicationWidgetListBackgroundDefaultValue(getContext())));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER_DEFAULT_VALUE));
            //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS,ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON, ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsWidgetOneRow extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_widget_one_row, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LAYOUT_HEIGHT, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LAYOUT_HEIGHT, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LAYOUT_HEIGHT_DEFAULT_VALUE));
            //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_HIGHER_LAYOUT, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_HIGHER_LAYOUT, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_HIGHER_LAYOUT_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_FILL_BACKGROUND, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_FILL_BACKGROUND, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_FILL_BACKGROUND_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE,ApplicationPreferences.applicationWidgetOneRowChangeColorsByNightModeDefaultValue(getContext())));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, ApplicationPreferences.applicationWidgetOneRowBackgroundDefaultValue(getContext())));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER_DEFAULT_VALUE));
            //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS,ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_OFF, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_OFF, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_OFF_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_ON, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_ON, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsWidgetIcon extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_widget_icon, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LAYOUT_HEIGHT, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LAYOUT_HEIGHT, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LAYOUT_HEIGHT_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_FILL_BACKGROUND, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_FILL_BACKGROUND, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_FILL_BACKGROUND_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE, ApplicationPreferences.applicationWidgetIconChangeColorsByNightModeDefaultValue(getContext())));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND, ApplicationPreferences.applicationWidgetIconBackgroundDefaultValue(getContext())));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER_DEFAULT_VALUE));
            //editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS,ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_OFF, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_OFF, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_OFF_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_ON, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_ON, ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsShortcut extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_shortcut, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR, ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_EMBLEM, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_EMBLEM, ApplicationPreferences.PREF_APPLICATION_SHORTCUT_EMBLEM_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsSamsungEdgePanel extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_samsung_edge_panel, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_HEADER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_HEADER, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_HEADER_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE, ApplicationPreferences.applicationSamsungEdgeChangeColorsByNightModeDefaultValue()));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, ApplicationPreferences.applicationSamsungEdgeBackgroundDefaultValue()));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_NIGHT_MODE_OFF, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_NIGHT_MODE_OFF, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_NIGHT_MODE_OFF_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_NIGHT_MODE_ON, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_NIGHT_MODE_ON, ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsWidgetOneRowProfileList extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_widget_one_row_profile_list, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LAYOUT_HEIGHT, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LAYOUT_HEIGHT, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LAYOUT_HEIGHT_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_FILL_BACKGROUND, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_FILL_BACKGROUND, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_FILL_BACKGROUND_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_CHANGE_COLOR_BY_NIGHT_MODE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_CHANGE_COLOR_BY_NIGHT_MODE,ApplicationPreferences.applicationWidgetOneRowProfileListChangeColorsByNightModeDefaultValue(getContext())));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND, ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundDefaultValue(getContext())));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_TYPE, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_TYPE, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_TYPE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_B, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_B, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_B_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_SHOW_BORDER, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_SHOW_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_SHOW_BORDER_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_BORDER, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_BORDER, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_BORDER_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ROUNDED_CORNERS_RADIUS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ROUNDED_CORNERS_RADIUS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ROUNDED_CORNERS_RADIUS_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ICON_COLOR, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ICON_COLOR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_USE_DYNAMIC_COLORS, fromPreference.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_USE_DYNAMIC_COLORS,ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_USE_DYNAMIC_COLORS_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ARROWS_MARK_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ARROWS_MARK_LIGHTNESS, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ARROWS_MARK_LIGHTNESS_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_NUMBER_OF_PROFILES_PER_PAGE, fromPreference.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_NUMBER_OF_PROFILES_PER_PAGE, ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_NUMBER_OF_PROFILES_PER_PAGE_DEFAULT_VALUE));
        }
    }

    static public class PhoneProfilesPrefsProfileListNotification extends PhoneProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            PreferenceManager prefMng = getPreferenceManager();
            SharedPreferences preferences = prefMng.getSharedPreferences();
            if ((getContext() != null) && (preferences != null)) {
                SharedPreferences applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                loadSharedPreferences(preferences, applicationPreferences);
            }

            setPreferencesFromResource(R.xml.phone_profiles_prefs_profile_list_notification, rootKey);
        }

        @Override
        void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_DISPLAY_NOTIFICATION, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_DISPLAY_NOTIFICATION, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_DISPLAY_NOTIFICATION_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_SHOW_IN_STATUS_BAR, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_SHOW_IN_STATUS_BAR, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_SHOW_IN_STATUS_BAR_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_HIDE_IN_LOCKSCREEN, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_HIDE_IN_LOCKSCREEN, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_HIDE_IN_LOCKSCREEN_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_STATUS_BAR_STYLE, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_STATUS_BAR_STYLE, ApplicationPreferences.notificationProfileListStatusBarStyleDefaultValue()));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_COLOR, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_COLOR, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_COLOR_DEFAULT_VALUE));
            editor.putInt(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_CUSTOM_COLOR, fromPreference.getInt(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_CUSTOM_COLOR, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_CUSTOM_COLOR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ARROWS_MARK_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ARROWS_MARK_LIGHTNESS, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ARROWS_MARK_LIGHTNESS_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_NUMBER_OF_PROFILES_PER_PAGE, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_NUMBER_OF_PROFILES_PER_PAGE, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_NUMBER_OF_PROFILES_PER_PAGE_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_COLOR, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_COLOR, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_COLOR_DEFAULT_VALUE));
            editor.putString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_LIGHTNESS, fromPreference.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_LIGHTNESS, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_LIGHTNESS_DEFAULT_VALUE));
            editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS, fromPreference.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE));
        }

    }

}
