package sk.henrichg.phoneprofilesplus;

import static android.app.role.RoleManager.ROLE_CALL_SCREENING;
import static android.content.Context.RECEIVER_NOT_EXPORTED;
import static android.content.Context.ROLE_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.PeriodicWorkRequest;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/** @noinspection ExtractMethodRecommender*/
class PhoneProfilesPrefsFragment extends PreferenceFragmentCompat
                        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceManager prefMng;
    private SharedPreferences preferences;
    private SharedPreferences applicationPreferences;

    private ShortcutToEditorAddedBroadcastReceiver shortcutToEditorAddedReceiver;
    private ShortcutToMobileCellScanningAddedBroadcastReceiver shortcutToMobileCellScanningAddedReceiver;
    private ShortcutForRestartEventsAddedBroadcastReceiver shortcutForRestartEventsAddedReceiver;

    //boolean scrollToSet = false;
    private boolean nestedFragment = false;

    private static final String PREF_APPLICATION_PERMISSIONS = "permissionsApplicationPermissions";
    private static final int RESULT_APPLICATION_PERMISSIONS = 1990;
    private static final String PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS = "permissionsWriteSystemSettingsPermissions";
    private static final int RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS = 1991;
    //private static final String PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = "permissionsAccessNotificationPolicyPermissions";
    //private static final int RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = 1997;
    private static final String PREF_DRAW_OVERLAYS_PERMISSIONS = "permissionsDrawOverlaysPermissions";
    private static final int RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS = 1998;
    static final String PREF_GRANT_ROOT_PERMISSION = "permissionsGrantRootPermission";
    private static final String PREF_GRANT_G1_PERMISSION = "permissionsGrantG1Permission";
    private static final String PREF_GRANT_SHIZUKU_PERMISSION = "permissionsGrantShizukuPermission";
    private static final String PREF_NOTIFICATION_POLICY_ACCESS_PERMISSIONS = "permissionsNotificationPolicyAccessPermissions";
    private static final int RESULT_NOTIFICATION_POLICY_ACCESS_PERMISSIONS = 2000;

    private static final String PREF_WIFI_LOCATION_SYSTEM_SETTINGS = "applicationEventWiFiLocationSystemSettings";
    private static final String PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS = "applicationEventBluetoothLocationSystemSettings";
    private static final String PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS = "applicationEventMobileCellsLocationSystemSettings";
    static final int RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS = 1992;
    //static final String PREF_POWER_SAVE_MODE_INTERNAL = "applicationPowerSaveModeInternal";
    private static final String PREF_LOCATION_SYSTEM_SETTINGS = "applicationEventLocationSystemSettings";
    private static final int RESULT_LOCATION_SYSTEM_SETTINGS = 1994;
    private static final String PREF_LOCATION_EDITOR = "applicationEventLocationsEditor";
    private static final String PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS = "applicationBatteryOptimization";
    //private static final String PREF_APPLICATION_LANGUAGE_24 = "applicationLanguage24";
    //static final int RESULT_LOCALE_SETTINGS = 1996;
    private static final String PREF_AUTOSTART_MANAGER = "applicationAutoStartManager";
    private static final String PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS = "applicationEventWiFiKeepOnSystemSettings";
    private static final int RESULT_WIFI_KEEP_ON_SETTINGS = 1999;
    private static final String PREF_ACTIVATED_PROFILE_NOTIFICATION_SYSTEM_SETTINGS = "notificationSystemSettingsActivatedProfile";
    private static final String PREF_ALL_NOTIFICATIONS_SYSTEM_SETTINGS = "notificationSystemSettingsAll";
    //private static final String PREF_APPLICATION_POWER_MANAGER = "applicationPowerManager";
    //private static final String PREF_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_SYSTEM_SETTINGS = "applicationEventMobileCellNotUsedCellsDetectionNotificationSystemSettings";
    private static final String PREF_SYSTEM_POWER_SAVE_MODE_SETTINGS = "applicationSystemPowerSaveMode";
    private static final String PREF_LOCATION_POWER_SAVE_MODE_SETTINGS = "applicationLocationPowerSaveMode";
    private static final String PREF_WIFI_POWER_SAVE_MODE_SETTINGS = "applicationWifiPowerSaveMode";
    private static final String PREF_BLUETOOTH_POWER_SAVE_MODE_SETTINGS = "applicationBluetoothPowerSaveMode";
    private static final String PREF_MOBILE_CELL_POWER_SAVE_MODE_SETTINGS = "applicationMobileCellPowerSaveMode";
    private static final String PREF_ORIENTATION_POWER_SAVE_MODE_SETTINGS = "applicationOrientationPowerSaveMode";
    private static final String PREF_PERIODIC_SCANNING_POWER_SAVE_MODE_SETTINGS = "applicationPeriodicScanningPowerSaveMode";
    private static final String PREF_NOTIFICATION_POWER_SAVE_MODE_SETTINGS = "applicationNotificationPowerSaveMode";
    private static final int RESULT_POWER_SAVE_MODE_SETTINGS = 1993;
    private static final String PREF_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS = "applicationEventNotificationNotificationsAccessSettings";
    private static final int RESULT_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS = 1994;
    //private static final String PREF_NOTIFICATION_PROFILE_ICON_COLOR_INFO1 = "notificationProfileIconColorInfo1";
    private static final String PREF_NOTIFICATION_PROFILE_ICON_COLOR_INFO2 = "notificationProfileIconColorInfo2";
    private static final String PREF_ALL_NOTIFICATIONS_PROFILE_LIST_SYSTEM_SETTINGS = "notificationProfileListSystemSettingsAll";
    private static final String PREF_NOTIFICATION_PROFILE_LIST_SYSTEM_SETTINGS = "notificationProfileListSystemSettingsProfileList";
    private static final String PREF_NOTIFICATION_SCANNING_NOTIFICATION_ACCESS_RESTRICTED_SETTINGS = "applicationEventNotificationNotificationsAccessSettingsRestrictedSettings";
    static final String PREF_EVENT_MOBILE_CELLS_REGISTRATION = "applicationEventMobileCellsRegistration";
    private final String PREF_SET_CALL_SCREENING_ROLE_SETTINGS = "setCallScreeningRoleSettings";
    private final int RESULT_SET_CALL_SCREENING_ROLE_SETTINGS = 1995;

    static final String PREF_APPLICATION_INTERFACE_CATEGORY_ROOT = "applicationInterfaceCategoryRoot";
    static final String PREF_APPLICATION_START_CATEGORY_ROOT = "categoryApplicationStartRoot";
    static final String PREF_SYSTEM_CATEGORY_ROOT = "categorySystemRoot";
    static final String PREF_PERMISSIONS_CATEGORY_ROOT = "categoryPermissionsRoot";
    static final String PREF_PROFILE_ACTIVATION_CATEGORY_ROOT = "profileActivationCategoryRoot";
    static final String PREF_EVENT_RUN_CATEGORY_ROOT = "eventRunCategoryRoot";
    static final String PREF_APP_NOTIFICATION_CATEGORY_ROOT = "categoryAppNotificationRoot";
    static final String PREF_SPECIAL_PROFILE_PARAMETERS_CATEGORY_ROOT = "specialProfileParametersCategoryRoot";
    static final String PREF_PERIODIC_SCANNING_CATEGORY_ROOT = "periodicScanningCategoryRoot";
    static final String PREF_LOCATION_SCANNING_CATEGORY_ROOT = "locationScanningCategoryRoot";
    static final String PREF_WIFI_SCANNING_CATEGORY_ROOT = "wifiScanningCategoryRoot";
    static final String PREF_BLUETOOTH_SCANNING_CATEGORY_ROOT = "bluetoothScanningCategoryRoot";
    static final String PREF_MOBILE_CELLS_SCANNING_CATEGORY_ROOT = "mobileCellsScanningCategoryRoot";
    static final String PREF_ORIENTATION_SCANNING_CATEGORY_ROOT = "orientationScanningCategoryRoot";
    static final String PREF_NOTIFICATION_SCANNING_CATEGORY_ROOT = "notificationScanningCategoryRoot";
    static final String PREF_ACTIVATOR_CATEGORY_ROOT = "categoryActivatorRoot";
    static final String PREF_EDITOR_CATEGORY_ROOT = "categoryEditorRoot";
    static final String PREF_WIDGET_LIST_CATEGORY_ROOT = "categoryWidgetListRoot";
    static final String PREF_WIDGET_ONE_ROW_CATEGORY_ROOT = "categoryWidgetOneRowRoot";
    static final String PREF_WIDGET_ICON_CATEGORY_ROOT = "categoryWidgetIconRoot";
    static final String PREF_WIDGET_ONE_ROW_PROFILE_LIST_CATEGORY_ROOT = "categoryWidgetOneRowProfileListRoot";
    static final String PREF_PROFILE_LIST_NOTIFICATIONLIST_CATEGORY_ROOT = "categoryProfileListNotificationRoot";
    static final String PREF_SHORTCUT_CATEGORY_ROOT = "categoryShortcutRoot";
    static final String PREF_WIDGET_PANEL_CATEGORY_ROOT = "categoryWidgetPanelRoot";
    static final String PREF_WIDGET_DASH_CLOCK_CATEGORY_ROOT = "categoryWidgetDashClockRoot";

    static final String PREF_UNLINK_RINGER_NOTIFICATION_VOLUMES_INFO = "applicationUnlinkRingerNotificationVolumesInfo";
    static final String PREF_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL_INFO = "applicationEventPeriodicScanningScanIntervalInfo";
    static final String PREF_EVENT_LOCATION_UPDATE_INTEFVAL_INFO = "applicationEventLocationUpdateIntervalInfo";
    static final String PREF_EVENT_WIFI_SCAN_INTERVAL_INFO = "applicationEventWifiScanIntervalInfo";
    static final String PREF_EVENT_BLUETOOTH_SCAN_INTERVAL_INFO = "applicationEventBluetoothScanIntervalInfo";
    static final String PREF_EVENT_ORIENTATION_SCAN_INTERVAL_INFO = "applicationEventOrientationScanIntervalInfo";
    static final String PREF_UNLINK_RINGER_NOTIFICATION_VLUMES_IMPORTANT_INFO = "applicationUnlinkRingerNotificationVolumesImportantInfo";
    static final String PREF_COLOR_OS_WIFI_BLUETOOTH_DIALOG_INFO = "applicationColorOsWifiBluetoothDialogsInfo";
    static final String PREF_MIUI_WIFI_BLUETOOTH_DIALOG_INFO = "applicationMIUIWifiBluetoothDialogsInfo";
    static final String PREF_WIDGET_ICON_NOT_WORKING_NIUI_INFO = "applicationWidgetIconNotWorkingMIUIInfo";
    static final String PREF_WIDGET_ONE_ROW_NOT_WORKING_MIUI_INFO = "applicationWidgetOneRowNotWorkingMIUIInfo";
    static final String PREF_WIDGET_LIST_NOT_WORKING_MIUI_INFO = "applicationWidgetListNotWorkingMIUIInfo";
    static final String PREF_WIDGET_ONE_ROW_PROFILE_LIST_NOT_WORKING_MIUI_INFO = "applicationWidgetOneRowProfileListNotWorkingMIUIInfo";
    static final String PREF_NOTIFICATION_APP_INSTEAD_PROFILE_ICON_IN_STATUS_BAR_INFO = "notificationAppInstedProfileIconInStatusBarInfo";
    static final String PREF_WIFI_CONTROL_INFO = "applicationWifiControlInfo";
    static final String PREF_EVENT_WIFI_SCAN_THROTTLING_INFO = "applicationEventWifiScanThrottlingInfo";
    static final String PREF_NOTIFICATION_BACKGROUND_COLOR_INFO = "notificationBackgroundColorInfo";
    static final String PREF_NOTIFICATION_USE_DECORATOR_INFO = "notificationUseDecoratorInfo";
    static final String PREF_NOTIFICATION_APP_INSTEAD_PROFILE_ICON_IN_NOTIFICATION_PANEL_INFO = "notificationAppInstedProfileIconInNotificationPanelInfo";

    static final String PREF_DO_NOT_KILL_MY_APP = "applicationDoNotKillMyApp";
    static final String PREF_CREATE_EDITOR_SHORTCUT = "applicationCreateEditorShortcut";
    static final String PREF_CREATE_MOBILE_CELL_SCANNING_SHORTCUT = "applicationCreateMobileCellScanningShortcut";
    static final String PREF_CREATE_RESTART_EVENTS_SHORTCUT = "applicationCreateReastartEventsShortcut";

    static final String PREF_WIFI_SCANNING_CATEGORY = "wifiScanningCategory";
    static final String PREF_BLUETOOTH_SCANNING_CATEGORY = "bluetoothScanningCategory";
    static final String PREF_MOBILE_CELLS_SCANNING_CATEGORY = "mobileCellsScanningCategory";
    static final String PREF_ORIENTATION_SCANNING_CATEGORY = "orientationScanningCategory";
    static final String PREF_APLICATION_START_CATEGORY = "categoryApplicationStart";
    static final String PREF_NOTIFICATION_STATUS_BAR_CATEGORY = "notificationStatusBarCategory";
    static final String PREF_PERMISSIONS_CATEGORY = "categoryPermissions";
    static final String PREF_SPECIAL_PROFILE_PARAMETERS_CATEGORY = "specialProfileParametersCategory";
    static final String PREF_WIDGET_ICON_CATEGORY = "categoryWidgetIcon";
    static final String PREF_WIDGET_ONE_ROW_CATEGORY = "categoryWidgetOneRow";
    static final String PREF_WIDGET_LISY_CATEGORY = "categoryWidgetList";
    static final String PREF_WIDGET_ONE_ROW_PROFILE_LIST_CATEGORY = "categoryWidgetOneRowProfileList";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_CONFIGURE_CELLS = "applicationEventMobileCellsConfigureCells";
    static final String PREF_NOTIFICATION_NOTIFICATION_PANEL_CATEGORY = "notificationNotificationPanelCategory";
    private static final String PREF_APPLICATION_WIDGET_DASH_CLOCK_INFO = "applicationWidgetDashClockInfo";
    private static final String PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T_INFO = "applicationWidgetOneRowLightnessTInfo";
    private static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T_INFO = "applicationWidgetListLightnessTInfo";
    private static final String PREF_APPLICATION_WIDGET_PANEL_INFO = "applicationWidgetPanelInfo";
    private static final String PREF_DEFAULT_ROLES_APPLICATIONS_RESTRICTED_SETTINGS = "defaultRolesApplicationsRestrictedSettings";

    static final String PREF_DEFAULT_ROLES_APPLICATIONS_ROOT = "categoryDefaultRolesApplicationsRoot";

    //static final String PREF_POWER_SAVE_MODE_INTERNAL = "applicationPowerSaveModeInternal";

    private static final String ACTION_SHORTCUT_TO_EDITOR_ADDED = PPApplication.PACKAGE_NAME + ".ACTION_SHORTCUT_TO_EDITOR_ADDED";
    private static final String ACTION_SHORTCUT_TO_MOBILE_CELL_SCANNING_ADDED = PPApplication.PACKAGE_NAME + ".ACTION_SHORTCUT_TO_MOBILE_CELL_SCANNING_ADDED";
    private static final String ACTION_SHORTCUT_FOR_RESTART_EVENTS_ADDED = PPApplication.PACKAGE_NAME + ".ACTION_SHORTCUT_FOR_RESTART_EVENTS_ADDED";

    //private static final String EXTRA_APP_PACKAGE = "app_package";
    //private static final String EXTRA_APP_UID = "app_uid";

    private static final String SHORTCUT_ID_EDITOR = "ppp_editor";
    private static final String SHORTCUT_ID_MOBILE_CELL_SCANNING = "ppp_mobile_cell_scanning";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // is required for to not call onCreate and onDestroy on orientation change
        //noinspection deprecation
        setRetainInstance(true);

        nestedFragment = !(this instanceof PhoneProfilesPrefsRoot);

        initPreferenceFragment(/*savedInstanceState*/);
        //prefMng = getPreferenceManager();
        //preferences = prefMng.getSharedPreferences();

        updateAllSummary();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //initPreferenceFragment();
    }

    @NonNull
    @Override
    public RecyclerView onCreateRecyclerView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, Bundle state) {
        final RecyclerView view = super.onCreateRecyclerView(inflater, parent, state);
        view.setItemAnimator(null);
        view.setLayoutAnimation(null);

        // WARNING: must be in base_styles_phoneprofilestheme_preferences_daynight:
        //             <item name="android:scrollbars">vertical</item>
        view.setScrollbarFadingEnabled(false);

        return view;
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference)
    {
        PreferenceDialogFragmentCompat dialogFragment = null;

        if (preference instanceof PPListPreference)
        {
            ((PPListPreference)preference).fragment = new PPListPreferenceFragment();
            dialogFragment = ((PPListPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof PPMultiSelectListPreference)
        {
            ((PPMultiSelectListPreference)preference).fragment = new PPMultiSelectListPreferenceFragment();
            dialogFragment = ((PPMultiSelectListPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof DurationDialogPreference)
        {
            ((DurationDialogPreference)preference).fragment = new DurationDialogPreferenceFragment();
            dialogFragment = ((DurationDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ProfilePreference)
        {
            ((ProfilePreference)preference).fragment = new ProfilePreferenceFragment();
            dialogFragment = ((ProfilePreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof RingtonePreference)
        {
            ((RingtonePreference)preference).fragment = new RingtonePreferenceFragment();
            dialogFragment = ((RingtonePreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof LocationGeofencePreference)
        {
            ((LocationGeofencePreference)preference).fragment = new LocationGeofencePreferenceFragment();
            dialogFragment = ((LocationGeofencePreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof BetterNumberPickerPreference)
        {
            ((BetterNumberPickerPreference)preference).fragment = new BetterNumberPickerPreferenceFragment();
            dialogFragment = ((BetterNumberPickerPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ColorChooserPreference)
        {
            ((ColorChooserPreference)preference).fragment = new ColorChooserPreferenceFragment();
            dialogFragment = ((ColorChooserPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof CustomColorDialogPreference)
        {
            ((CustomColorDialogPreference)preference).fragment = new CustomColorDialogPreferenceFragment();
            dialogFragment = ((CustomColorDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof TimeDialogPreference) {
            ((TimeDialogPreference) preference).fragment = new TimeDialogPreferenceFragment();
            dialogFragment = ((TimeDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof OpaquenessLightingPreference) {
            ((OpaquenessLightingPreference) preference).fragment = new OpaquenessLightingPreferenceFragment();
            dialogFragment = ((OpaquenessLightingPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof InfoDialogPreference)
        {
            ((InfoDialogPreference)preference).fragment = new InfoDialogPreferenceFragment();
            dialogFragment = ((InfoDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof RestartEventsIconColorChooserPreference)
        {
            ((RestartEventsIconColorChooserPreference)preference).fragment = new RestartEventsIconColorChooserPreferenceFragment();
            dialogFragment = ((RestartEventsIconColorChooserPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof MobileCellsEditorPreference) {
            ((MobileCellsEditorPreference) preference).fragment = new MobileCellsEditorPreferenceFragment();
            dialogFragment = ((MobileCellsEditorPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof MobileCellsRegistrationDialogPreference) {
            ((MobileCellsRegistrationDialogPreference) preference).fragment = new MobileCellsRegistrationDialogPreferenceFragment();
            dialogFragment = ((MobileCellsRegistrationDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null)
        {
            if ((getActivity() != null) && (!getActivity().isFinishing())) {
                FragmentManager fragmentManager = getParentFragmentManager();//getFragmentManager();
                //if (fragmentManager != null) {
                //noinspection deprecation
                dialogFragment.setTargetFragment(this, 0);
                if (!fragmentManager.isDestroyed())
                    dialogFragment.show(fragmentManager, PPApplication.PACKAGE_NAME + ".PhoneProfilesPrefsActivity.DIALOG");
                //}
            }
        }
        else
        {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @SuppressLint("BatteryLife")
    @SuppressWarnings("deprecation")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() == null)
            return;

        Context appContext = getActivity().getApplicationContext();

        PhoneProfilesPrefsActivity activity = (PhoneProfilesPrefsActivity) getActivity();
        if (!(activity.activityStarted))
            return;

        PhoneProfilesPrefsFragment fragment = this;
        final TextView preferenceSubTitle = activity.findViewById(R.id.activity_preferences_subtitle);


        // must be used handler for rewrite toolbar title/subtitle
        final Handler handler = new Handler(activity.getMainLooper());
        final WeakReference<PhoneProfilesPrefsActivity> activityWeakRef = new WeakReference<>(activity);
        handler.postDelayed(() -> {
//                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PhoneProfilesPrefsFragment.onActivityCreated");
            PhoneProfilesPrefsActivity _activity = activityWeakRef.get();
            if ((_activity == null) || _activity.isFinishing() || _activity.isDestroyed())
                return;

            Toolbar toolbar = _activity.findViewById(R.id.activity_preferences_toolbar_no_subtitle);
            //noinspection DataFlowIssue
            toolbar.setTitle(activity.getString(R.string.title_activity_phone_profiles_preferences));
        }, 200);

        // subtitle
        if (nestedFragment) {
            //noinspection DataFlowIssue
            preferenceSubTitle.setVisibility(View.VISIBLE);

            Drawable triangle = ContextCompat.getDrawable(activity, R.drawable.ic_submenu_triangle);
            if (triangle != null) {
                triangle.setTint(ContextCompat.getColor(activity, R.color.activityNormalTextColor));
                SpannableString headerTitle = new SpannableString("    " +
                        fragment.getPreferenceScreen().getTitle());
                triangle.setBounds(
                        GlobalGUIRoutines.sip(2),
                        GlobalGUIRoutines.sip(1),
                        GlobalGUIRoutines.sip(13),
                        GlobalGUIRoutines.sip(10));
                headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                preferenceSubTitle.setText(headerTitle);
            } else
                preferenceSubTitle.setText(fragment.getPreferenceScreen().getTitle());
        }
        else {
            //noinspection DataFlowIssue
            preferenceSubTitle.setVisibility(View.GONE);
            //toolbar.setSubtitle(null);
        }

        setDivider(null); // this remove dividers for categories

        /*
        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);
        */
        if (!nestedFragment) {
            Preference preferenceCategoryScreen;
            preferenceCategoryScreen = findPreference(PREF_APPLICATION_INTERFACE_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) {
                setCategorySummary(preferenceCategoryScreen);
            }
            preferenceCategoryScreen = findPreference(PREF_APPLICATION_START_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);

            preferenceCategoryScreen = findPreference(PREF_SYSTEM_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceCategoryScreen, true, false, true, false, false, false);
                setCategorySummary(preferenceCategoryScreen);
            }
            preferenceCategoryScreen = findPreference(PREF_PERMISSIONS_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceCategoryScreen, true, false, true, false, false, false);
                setCategorySummary(preferenceCategoryScreen);
            }
            preferenceCategoryScreen = findPreference(PREF_PROFILE_ACTIVATION_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceCategoryScreen, true, false, true, false, false, false);
                setCategorySummary(preferenceCategoryScreen);
            }
            preferenceCategoryScreen = findPreference(PREF_EVENT_RUN_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceCategoryScreen, true, false, true, false, false, false);
                setCategorySummary(preferenceCategoryScreen);
            }
            preferenceCategoryScreen = findPreference(PREF_APP_NOTIFICATION_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_SPECIAL_PROFILE_PARAMETERS_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_PERIODIC_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_LOCATION_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_WIFI_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_BLUETOOTH_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_MOBILE_CELLS_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_ORIENTATION_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_NOTIFICATION_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_ACTIVATOR_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_EDITOR_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_WIDGET_LIST_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_WIDGET_ONE_ROW_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_WIDGET_ICON_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_WIDGET_ONE_ROW_PROFILE_LIST_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_PROFILE_LIST_NOTIFICATIONLIST_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference(PREF_WIDGET_DASH_CLOCK_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);

            preferenceCategoryScreen = findPreference(PREF_SHORTCUT_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);

            preferenceCategoryScreen = findPreference(PREF_WIDGET_PANEL_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);

            preferenceCategoryScreen = findPreference(PREF_DEFAULT_ROLES_APPLICATIONS_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }

        Preference preference;/* = findPreference(PREF_UNLINK_RINGER_NOTIFICATION_VOLUMES_INFO);
        if (preference != null) {
            preference.setShouldDisableView(false);
            preference.setEnabled(false);
        }*/

        //if (!ActivateProfileHelper.getMergedRingNotificationVolumes(activity.getApplicationContext())) {
        if (!ApplicationPreferences.prefMergedRingNotificationVolumes) {
            // detection of volumes merge = volumes are not merged
            preference = findPreference(PREF_UNLINK_RINGER_NOTIFICATION_VOLUMES_INFO);
            if (preference != null) {
                //preference.setEnabled(false);
                preference.setTitle(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumesUnlinked_summary);
                //systemCategory.removePreference(preference);
            }
        }
        else {
            preference = findPreference(PREF_UNLINK_RINGER_NOTIFICATION_VOLUMES_INFO);
            if (preference != null) {
                //preference.setEnabled(true);
                preference.setTitle(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes_summary);
            }
            /*Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_RINGER_NOTIFICATION_VOLUMES_UNLINKED_INFO);
            if (preference != null)
                systemCategory.removePreference(preference);*/
        }

        doOnActivityCreatedBatterySaver(PREF_PERIODIC_SCANNING_POWER_SAVE_MODE_SETTINGS, activity);
        doOnActivityCreatedBatterySaver(PREF_SYSTEM_POWER_SAVE_MODE_SETTINGS, activity);
        doOnActivityCreatedBatterySaver(PREF_LOCATION_POWER_SAVE_MODE_SETTINGS, activity);
        doOnActivityCreatedBatterySaver(PREF_WIFI_POWER_SAVE_MODE_SETTINGS, activity);
        doOnActivityCreatedBatterySaver(PREF_BLUETOOTH_POWER_SAVE_MODE_SETTINGS, activity);
        doOnActivityCreatedBatterySaver(PREF_MOBILE_CELL_POWER_SAVE_MODE_SETTINGS, activity);
        doOnActivityCreatedBatterySaver(PREF_ORIENTATION_POWER_SAVE_MODE_SETTINGS, activity);
        doOnActivityCreatedBatterySaver(PREF_NOTIFICATION_POWER_SAVE_MODE_SETTINGS, activity);

            preference = findPreference(PREF_APPLICATION_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference1 -> {
                    //Permissions.saveAllPermissions(activity.getApplicationContext(), false);
                    boolean ok = false;
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse(PPApplication.INTENT_DATA_PACKAGE +PPApplication.PACKAGE_NAME));
                    if (GlobalGUIRoutines.activityIntentExists(intent, activity.getApplicationContext())) {
                        try {
                            startActivityForResult(intent, RESULT_APPLICATION_PERMISSIONS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    if (!ok){
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference1.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    }
                    return false;
                });
            }
            preference = findPreference(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                /*if (PPApplication.romIsMIUI) {
                    preference.setSummary(R.string.phone_profiles_pref_writeSystemSettingPermissions_summary_miui);
                }*/
                preference.setOnPreferenceClickListener(preference12 -> {
                    boolean ok = false;
                    //if (!PPApplication.romIsMIUI) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_WRITE_SETTINGS, activity.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse(PPApplication.INTENT_DATA_PACKAGE + PPApplication.PACKAGE_NAME));
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference12.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    }
                    return false;
                });
            }
            /*preference = findPreference(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
            if (preference != null) {
                if (GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, activity.getApplicationContext())) {
                    //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
                            return false;
                        }
                    });
                } else {
                    PreferenceScreen preferenceCategory = findPreference(PREF_PERMISSIONS_CATEGORY);
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }*/
            preference = findPreference(PREF_DRAW_OVERLAYS_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    /*if (PPApplication.romIsMIUI) {
                        preference.setTitle(R.string.phone_profiles_pref_drawOverlaysPermissions_miui);
                        preference.setSummary(R.string.phone_profiles_pref_drawOverlaysPermissions_summary_miui);
                    }*/
                preference.setOnPreferenceClickListener(preference13 -> {
                    Intent intent = new Intent(activity, GrantDrawOverAppsActivity.class);
                    startActivityForResult(intent, RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS);
                    /*
                    boolean ok = false;
                    //if (!PPApplication.romIsMIUI) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, activity.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse(PPApplication.INTENT_DATA_PACKAGE +PPApplication.PACKAGE_NAME));
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference13.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.show();
                    }
                    */
                    return false;
                });
            }

            //int locationMode = Settings.Secure.getInt(activity.getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);

            /*
            if (WifiScanWorker.wifi == null)
                WifiScanWorker.wifi = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            boolean isScanAlwaysAvailable = WifiScanWorker.wifi.isScanAlwaysAvailable();

            if ((locationMode == Settings.Secure.LOCATION_MODE_OFF) || (!isScanAlwaysAvailable)) {*/
            preference = findPreference(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference14 -> {
                    boolean ok = false;
                    //Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, activity.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference14.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    }
                    return false;
                });
            }

            preference = findPreference(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference15 -> {
                    boolean ok = false;
                    //Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_WIFI_SETTINGS, activity.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WIFI_KEEP_ON_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference15.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    }
                    return false;
                });
            }

            //if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
            preference = findPreference(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference16 -> {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, activity.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference16.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    }
                    return false;
                });
            }

            //if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
            preference = findPreference(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference17 -> {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, activity.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference17.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    }
                    return false;
                });
            }

            preference = findPreference(PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference18 -> {
                    boolean isIgnoreBartteryOptimisationsSet = false;
                    PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    try {
                        if (pm != null)
                            isIgnoreBartteryOptimisationsSet = pm.isIgnoringBatteryOptimizations(PPApplication.PACKAGE_NAME);
                    } catch (Exception ignore) {
                    }
                    boolean ok = false;
                    if (isIgnoreBartteryOptimisationsSet) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, activity.getApplicationContext())) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivity(intent);
                                ok = true;
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                            }
                        }
                        if (!ok) {
                            PPAlertDialog dialog = new PPAlertDialog(
                                    preference18.getTitle(),
                                    getString(R.string.setting_screen_not_found_alert),
                                    getString(android.R.string.ok),
                                    null,
                                    null, null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    true, true,
                                    false, false,
                                    true,
                                    false,
                                    activity
                            );

                            if (!activity.isFinishing())
                                dialog.showDialog();
                        }
                    } else {
                        try {
                            Intent intent;
                            String packageName = PPApplication.PACKAGE_NAME;
                            intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse(PPApplication.INTENT_DATA_PACKAGE + packageName));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                        if (!ok) {
                            PPAlertDialog dialog = new PPAlertDialog(
                                    preference18.getTitle(),
                                    getString(R.string.setting_screen_not_found_alert),
                                    getString(android.R.string.ok),
                                    null,
                                    null, null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    true, true,
                                    false, false,
                                    true,
                                    false,
                                    activity
                            );

                            if (!activity.isFinishing())
                                dialog.showDialog();
                        }
                    }
                    return false;
                });
            }

            preference = findPreference(PREF_NOTIFICATION_POLICY_ACCESS_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference14 -> {
                    boolean ok = false;
                    //Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, activity.getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_NOTIFICATION_POLICY_ACCESS_PERMISSIONS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference14.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    }
                    return false;
                });
            }

        // force check root
        boolean rooted;
//        PPApplicationStatic.logE("[SYNCHRONIZED] PhoneProfilesPrefsFragment.onActivityCreated", "PPApplication.rootMutex");
        synchronized (PPApplication.rootMutex) {
            PPApplication.rootMutex.rootChecked = false;
            rooted = RootUtils._isRooted();
        }
        if (!rooted) {
            preference = findPreference(PREF_GRANT_ROOT_PERMISSION);
            if (preference != null)
                preference.setEnabled(false);
        }
        if (rooted) {
            preference = findPreference(PREF_GRANT_ROOT_PERMISSION);
            if (preference != null) {
                preference.setOnPreferenceClickListener(preference19 -> {
                    Permissions.grantRootX(null, activity);
                    setSummary(PREF_GRANT_ROOT_PERMISSION);
                    return false;
                });
            }
        }

        preference = findPreference(PREF_GRANT_G1_PERMISSION);
        if (preference != null) {
            preference.setOnPreferenceClickListener(preference110 -> {
                Permissions.grantG1Permission(null, activity);
                return false;
            });
        }

        preference = findPreference(PREF_GRANT_SHIZUKU_PERMISSION);
        if (preference != null) {
            preference.setOnPreferenceClickListener(preference110 -> {
                Permissions.grantShizukuPermission(null, activity);
                setSummary(PREF_GRANT_SHIZUKU_PERMISSION);
                return false;
            });
        }

        if (!BluetoothScanner.bluetoothLESupported(/*activity.getApplicationContext()*/)) {
            PreferenceScreen preferenceCategory = findPreference(PREF_BLUETOOTH_SCANNING_CATEGORY);
            preference = findPreference(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION);
            if ((preferenceCategory != null) && (preference != null))
                preferenceCategory.removePreference(preference);
        }
        preference = findPreference(PREF_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference111 -> {
                boolean ok = false;
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, activity.getApplicationContext())) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivityForResult(intent, RESULT_LOCATION_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            preference111.getTitle(),
                            getString(R.string.setting_screen_not_found_alert),
                            getString(android.R.string.ok),
                            null,
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            false,
                            activity
                    );

                    if (!activity.isFinishing())
                        dialog.showDialog();
                }
                return false;
            });
        }

        preference = findPreference(PREF_AUTOSTART_MANAGER);
        if (preference != null) {
            final AutoStartPermissionHelper autoStartPermissionHelper = AutoStartPermissionHelper.getInstance();
            if (autoStartPermissionHelper.isAutoStartPermissionAvailable(activity.getApplicationContext())) {
                preference.setOnPreferenceClickListener(preference119 -> {
                    boolean success;
                    try {
                        success = autoStartPermissionHelper.getAutoStartPermission(activity);
                    }catch (Exception e) {
                        PPApplicationStatic.logException("****** PhoneProfilesPrefsFragment.onActivityCreated", Log.getStackTraceString(e));
                        success = false;
                    }
                    if (!success) {
                        CharSequence message;
                        if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)
                            message = getString(R.string.phone_profiles_pref_systemAutoStartManager_settingScreenNotFound_huawei_alert);
                        else
                            message = getString(R.string.phone_profiles_pref_systemAutoStartManager_settingScreenNotFound_alert);

                        PPAlertDialog dialog = new PPAlertDialog(
                                getString(R.string.phone_profiles_pref_systemAutoStartManager),
                                message,
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                false,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    }
                    return false;
                });
            } else {
                /*PreferenceScreen preferenceScreen = findPreference("categorySystem");
                if (preferenceScreen != null) {
                    PreferenceCategory preferenceCategory = findPreference("applicationAutostartCategory");
                    if (preferenceCategory != null)
                        preferenceScreen.removePreference(preferenceCategory);
                }*/
                PreferenceScreen preferenceScreen = findPreference(PREF_APLICATION_START_CATEGORY);
                if (preferenceScreen != null) {
                    preference = findPreference(PREF_AUTOSTART_MANAGER);
                    if (preference != null)
                        preferenceScreen.removePreference(preference);
                }
            }
        }

        long workMinInterval = TimeUnit.MILLISECONDS.toMinutes(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS);
        String summary = getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary1) + " " +
                workMinInterval + " " +
                getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary2);
        preference = findPreference(PREF_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL_INFO);
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference(PREF_EVENT_LOCATION_UPDATE_INTEFVAL_INFO);
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference(PREF_EVENT_WIFI_SCAN_INTERVAL_INFO);
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference(PREF_EVENT_BLUETOOTH_SCAN_INTERVAL_INFO);
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference(PREF_EVENT_ORIENTATION_SCAN_INTERVAL_INFO);
        if (preference != null) {
            summary = getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary1) + " 10 " +
                    getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary3);
            preference.setSummary(summary);
        }

        if (Build.VERSION.SDK_INT >= 27) {
            preference = findPreference(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_WIFI_SCANNING_CATEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
            preference = findPreference(PREF_ACTIVATED_PROFILE_NOTIFICATION_SYSTEM_SETTINGS);
            if (preference != null) {
                preference.setSummary(getString(R.string.phone_profiles_pref_notificationSystemSettings_summary) +
                        " " + getString(R.string.notification_channel_activated_profile));
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference112 -> {
                    boolean ok = false;
//                    PPApplicationStatic.logE("[PPP_NOTIFICATION] PhoneProfilesPrefsFragment.onActivityCreated - activated porofile notification preference", "call of PPApplication.createPPPAppNotificationChannel()");
                    PPApplicationStatic.createPPPAppNotificationChannel(activity.getApplicationContext(), false);
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, PPApplication.PROFILE_NOTIFICATION_CHANNEL);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, PPApplication.PACKAGE_NAME);
                    if (GlobalGUIRoutines.activityIntentExists(intent, activity.getApplicationContext())) {
                        try {
                            startActivity(intent);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference112.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    }
                    return false;
                });
            }
            preference = findPreference(PREF_NOTIFICATION_PROFILE_LIST_SYSTEM_SETTINGS);
            if (preference != null) {
                preference.setSummary(getString(R.string.phone_profiles_pref_notificationSystemSettings_summary) +
                        " " + getString(R.string.notification_channel_profile_list));
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference112 -> {
                    boolean ok = false;
//                    PPApplicationStatic.logE("[PPP_NOTIFICATION] PhoneProfilesPrefsFragment.onActivityCreated - activated porofile notification preference", "call of PPApplication.createPPPAppNotificationChannel()");
                    PPApplicationStatic.createPPPAppNotificationChannel(activity.getApplicationContext(), false);
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, PPApplication.PROFILE_LIST_NOTIFICATION_CHANNEL);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, PPApplication.PACKAGE_NAME);
                    if (GlobalGUIRoutines.activityIntentExists(intent, activity.getApplicationContext())) {
                        try {
                            startActivity(intent);
                            ok = true;
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference112.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                    }
                    return false;
                });
            }

        preference = findPreference(PREF_ALL_NOTIFICATIONS_SYSTEM_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference113 -> {
                boolean ok = false;
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] PhoneProfilesPrefsFragment.onActivityCreated - all notifications preference", "call of PPApplication.createPPPAppNotificationChannel()");
                PPApplicationStatic.createPPPAppNotificationChannel(activity.getApplicationContext(), false);

                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT > 26) {
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, PPApplication.PACKAGE_NAME);
                } else /*if (Build.VERSION.SDK_INT == 26)*/ {
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra("android.provider.extra.APP_PACKAGE", PPApplication.PACKAGE_NAME);
                }/* else {
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra(EXTRA_APP_PACKAGE, PPApplication.PACKAGE_NAME);
                    intent.putExtra(EXTRA_APP_UID, activity.getApplicationInfo().uid);
                }*/

                if (GlobalGUIRoutines.activityIntentExists(intent, activity.getApplicationContext())) {
                    try {
                        startActivity(intent);
                        ok = true;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            preference113.getTitle(),
                            getString(R.string.setting_screen_not_found_alert),
                            getString(android.R.string.ok),
                            null,
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            false,
                            activity
                    );

                    if (!activity.isFinishing())
                        dialog.showDialog();
                }
                return false;
            });
        }
        preference = findPreference(PREF_ALL_NOTIFICATIONS_PROFILE_LIST_SYSTEM_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference113 -> {
                boolean ok = false;
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] PhoneProfilesPrefsFragment.onActivityCreated - all notifications preference", "call of PPApplication.createPPPAppNotificationChannel()");
                PPApplicationStatic.createPPPAppNotificationChannel(activity.getApplicationContext(), false);

                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT > 26) {
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, PPApplication.PACKAGE_NAME);
                } else /*if (Build.VERSION.SDK_INT == 26)*/ {
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra("android.provider.extra.APP_PACKAGE", PPApplication.PACKAGE_NAME);
                } /*else {
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra(EXTRA_APP_PACKAGE, PPApplication.PACKAGE_NAME);
                    intent.putExtra(EXTRA_APP_UID, activity.getApplicationInfo().uid);
                }*/

                if (GlobalGUIRoutines.activityIntentExists(intent, activity.getApplicationContext())) {
                    try {
                        startActivity(intent);
                        ok = true;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            preference113.getTitle(),
                            getString(R.string.setting_screen_not_found_alert),
                            getString(android.R.string.ok),
                            null,
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            false,
                            activity
                    );

                    if (!activity.isFinishing())
                        dialog.showDialog();
                }
                return false;
            });
        }

        preference = findPreference(PREF_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference114 -> {
                boolean ok = false;
                String action;
                action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
                if (GlobalGUIRoutines.activityActionExists(action, activity.getApplicationContext())) {
                    try {
                        Intent intent = new Intent(action);
                        startActivityForResult(intent, RESULT_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplicationStatic.recordException(e);
                    }
                }
                if (!ok) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            preference114.getTitle(),
                            getString(R.string.setting_screen_not_found_alert),
                            getString(android.R.string.ok),
                            null,
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            false,
                            activity
                    );

                    if (!activity.isFinishing())
                        dialog.showDialog();
                }
                return false;
            });
        }

        //preference = findPreference(PREF_APPLICATION_POWER_MANAGER);
        preference = findPreference(PREF_UNLINK_RINGER_NOTIFICATION_VLUMES_IMPORTANT_INFO);
        if (preference != null) {
            preference.setOnPreferenceClickListener(preference115 -> {
                Intent intentLaunch = new Intent(activity, ImportantInfoActivityForceScroll.class);
                intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, false);
                intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SHOW_FRAGMENT, 1);
                intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SCROLL_TO, R.id.activity_info_notification_how_does_volume_separation_work_title);
                startActivity(intentLaunch);
                return false;
            });
        }

        preference = findPreference(PREF_DO_NOT_KILL_MY_APP);
        if (preference != null) {
            preference.setSummary(getString(R.string.phone_profiles_pref_applicationDoNotKillMyApp_summary1) + " " +
                    getString(R.string.phone_profiles_pref_applicationDoNotKillMyApp_webSiteName) + " " +
                    getString(R.string.phone_profiles_pref_applicationDoNotKillMyApp_summary2));
            preference.setOnPreferenceClickListener(preference116 -> {
                PPApplicationStatic.showDoNotKillMyAppDialog(activity);
                return false;
            });
        }

        if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
            preference = findPreference(PREF_COLOR_OS_WIFI_BLUETOOTH_DIALOG_INFO);
            if (preference != null) {
                preference.setOnPreferenceClickListener(preference117 -> {
                    PPAlertDialog dialog = new PPAlertDialog(
                            preference117.getTitle(),
                            getString(R.string.phone_profiles_pref_applicationColorOsWifiBluetoothDialogsInfo_message_fix),
                            getString(android.R.string.ok),
                            null,
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            false,
                            false,
                            activity
                    );

                    if (!activity.isFinishing())
                        dialog.showDialog();
                    return false;
                });
            }
        }
        else {
            preference = findPreference(PREF_COLOR_OS_WIFI_BLUETOOTH_DIALOG_INFO);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_PERMISSIONS_CATEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
        if (PPApplication.deviceIsXiaomi || PPApplication.romIsMIUI) {
            if (Build.VERSION.SDK_INT < 34) {
                preference = findPreference(PREF_MIUI_WIFI_BLUETOOTH_DIALOG_INFO);
                if (preference != null) {
                    preference.setOnPreferenceClickListener(preference118 -> {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference118.getTitle(),
                                getString(R.string.phone_profiles_pref_applicationMIUIWifiBluetoothDialogsInfo_message),
                                getString(R.string.miui_permissions_alert_dialog_show),
                                getString(android.R.string.cancel),
                                null, null,
                                (dialog1, which) -> {
                                    boolean ok = false;
                                    Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                    intent.setClassName("com.miui.securitycenter",
                                            "com.miui.permcenter.permissions.PermissionsEditorActivity");
                                    intent.putExtra(PPApplication.EXTRA_PKG_NAME, PPApplication.PACKAGE_NAME);
                                    if (GlobalGUIRoutines.activityIntentExists(intent, activity.getApplicationContext())) {
                                        try {
                                            startActivity(intent);
                                            ok = true;
                                        } catch (Exception e) {
                                            PPApplicationStatic.recordException(e);
                                        }
                                    }
                                    if (!ok) {
                                        PPAlertDialog dialog2 = new PPAlertDialog(
                                                preference118.getTitle(),
                                                getString(R.string.setting_screen_not_found_alert),
                                                getString(android.R.string.ok),
                                                null,
                                                null, null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                true, true,
                                                false, false,
                                                true,
                                                false,
                                                activity
                                        );

                                        if (!activity.isFinishing())
                                            dialog2.showDialog();
                                    }
                                },
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                false,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog.showDialog();
                        return false;
                    });
                }
            } else {
                preference = findPreference(ApplicationPreferences.PREF_APPLICATION_HYPER_OS_WIFI_BLUETOOTH_DIALOGS);
                if (preference != null) {
                    boolean hyperOsWifiBluetoothDialogs = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_HYPER_OS_WIFI_BLUETOOTH_DIALOGS,
                            ApplicationPreferences.PREF_APPLICATION_HYPER_OS_WIFI_BLUETOOTH_DIALOGS_DEFAULT_VALUE);
                    SharedPreferences appSharedPreferences = ApplicationPreferences.getSharedPreferences(activity.getApplicationContext());
                    if (appSharedPreferences != null) {
                        SharedPreferences.Editor editor = appSharedPreferences.edit();
                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_HYPER_OS_WIFI_BLUETOOTH_DIALOGS, hyperOsWifiBluetoothDialogs);
                        editor.apply();
                        ApplicationPreferences.applicationHyperOsWifiBluetoothDialogs(activity.getApplicationContext());
                    }
                    Permissions.setHyperOSWifiBluetoothDialogAppOp();
                }
            }
        }
        else {
            preference = findPreference(PREF_MIUI_WIFI_BLUETOOTH_DIALOG_INFO);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_PERMISSIONS_CATEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = findPreference(ApplicationPreferences.PREF_APPLICATION_HYPER_OS_WIFI_BLUETOOTH_DIALOGS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_PERMISSIONS_CATEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }

        boolean showInfoAboutApplicaitonIcons = true;
        if (!(PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI)) {
            preference = findPreference(PREF_WIDGET_ICON_NOT_WORKING_NIUI_INFO);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_WIDGET_ICON_CATEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = findPreference(PREF_WIDGET_ONE_ROW_NOT_WORKING_MIUI_INFO);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_WIDGET_ONE_ROW_CATEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = findPreference(PREF_WIDGET_LIST_NOT_WORKING_MIUI_INFO);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_WIDGET_LISY_CATEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = findPreference(PREF_WIDGET_ONE_ROW_PROFILE_LIST_NOT_WORKING_MIUI_INFO);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_WIDGET_ONE_ROW_PROFILE_LIST_CATEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = findPreference(PREF_NOTIFICATION_APP_INSTEAD_PROFILE_ICON_IN_STATUS_BAR_INFO);
            if (preference != null) {
                PreferenceCategory preferenceCategory = findPreference(PREF_NOTIFICATION_STATUS_BAR_CATEGORY);
                if (preferenceCategory != null) {
                    preferenceCategory.removePreference(preference);
                    showInfoAboutApplicaitonIcons = false;
                }
            }
        }
        if (showInfoAboutApplicaitonIcons) {
            InfoDialogPreference infoDialogPreference = prefMng.findPreference(PREF_NOTIFICATION_APP_INSTEAD_PROFILE_ICON_IN_STATUS_BAR_INFO);
            if (infoDialogPreference != null) {
                String infoText;
                infoDialogPreference.setSummary(R.string.phone_profiles_pref_notificationAppInstedProfileIconInStatusBarInfoClick_summary);
                infoText = getString(R.string.phone_profiles_pref_notificationAppInstedProfileIconInStatusBarInfo_summary) +
                        StringConstants.TAG_DOUBLE_BREAK_HTML+
                        getString(R.string.phone_profiles_pref_notificationAppInstedProfileIconInStatusBarInfo2_summary);
                infoDialogPreference.setInfoText(infoText);
                infoDialogPreference.setIsHtml(true);
            }
        }

        showInfoAboutApplicaitonIcons = true;
        if (!(PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy && (Build.VERSION.SDK_INT >= 35))) {
            preference = findPreference(PREF_NOTIFICATION_APP_INSTEAD_PROFILE_ICON_IN_NOTIFICATION_PANEL_INFO);
            if (preference != null) {
                PreferenceCategory preferenceCategory = findPreference(PREF_NOTIFICATION_NOTIFICATION_PANEL_CATEGORY);
                if (preferenceCategory != null) {
                    preferenceCategory.removePreference(preference);
                    showInfoAboutApplicaitonIcons = false;
                }
            }
        }
        if (showInfoAboutApplicaitonIcons) {
            InfoDialogPreference infoDialogPreference = prefMng.findPreference(PREF_NOTIFICATION_APP_INSTEAD_PROFILE_ICON_IN_NOTIFICATION_PANEL_INFO);
            if (infoDialogPreference != null) {
                String infoText;
                infoDialogPreference.setSummary(R.string.phone_profiles_pref_notificationAppInstedProfileIconInStatusBarInfoOneUI7Click_summary);
                infoText = getString(R.string.phone_profiles_pref_notificationAppInstedProfileIconInStatusBarInfoOneUI7_summary) +
                        StringConstants.TAG_DOUBLE_BREAK_HTML+
                        getString(R.string.phone_profiles_pref_notificationAppInstedProfileIconInStatusBarInfo2_summary);
                infoText = infoText+StringConstants.TAG_DOUBLE_BREAK_HTML+
                        getString(R.string.phone_profiles_pref_notificationAppInstedProfileIconInStatusBarInfo3OneUI7_summary);
                infoDialogPreference.setInfoText(infoText);
                infoDialogPreference.setIsHtml(true);
            }
        }

        if ((Build.VERSION.SDK_INT >= 28) &&
                (!(PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI))) {
            preference = findPreference(PREF_WIFI_CONTROL_INFO);
            if (preference != null) {
                //if (PPApplication.deviceIsSony)
                //    preference.setSummary(R.string.phone_profiles_pref_applicationWifiControlInfo_sony_summary);
                preference.setOnPreferenceClickListener(preference118 -> {
                    String message = getString(R.string.phone_profiles_pref_applicationWifiControlInfo_message);
                    //if (PPApplication.deviceIsSony)
                    //    message = getString(R.string.phone_profiles_pref_applicationWifiControlInfo_sony_message);
                    PPAlertDialog dialog = new PPAlertDialog(
                            preference118.getTitle(),
                            message,
                            getString(R.string.phone_profiles_pref_applicationWifiControlInfo_showButton),
                            getString(android.R.string.cancel),
                            null, null,
                            (dialog1, which) -> {
                                boolean ok = false;
                                final Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                if (GlobalGUIRoutines.activityIntentExists(intent, activity.getApplicationContext())) {
                                    try {
                                        startActivity(intent);
                                        ok = true;
                                    } catch (Exception e) {
                                        PPApplicationStatic.recordException(e);
                                    }
                                }
                                if (!ok) {
                                    PPAlertDialog dialog2 = new PPAlertDialog(
                                            preference118.getTitle(),
                                            getString(R.string.setting_screen_not_found_alert),
                                            getString(android.R.string.ok),
                                            null,
                                            null, null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            true, true,
                                            false, false,
                                            true,
                                            false,
                                            activity
                                    );

                                    if (!activity.isFinishing())
                                        dialog2.showDialog();
                                }
                            },
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            false,
                            false,
                            activity
                    );

                    if (!activity.isFinishing())
                        dialog.showDialog();
                    return false;
                });
            }
        }
        else {
            preference = findPreference(PREF_WIFI_CONTROL_INFO);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_PERMISSIONS_CATEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }

        /////////////////

        if (Build.VERSION.SDK_INT >= 31) {
            if (PPApplicationStatic.isPixelLauncherDefault(activity) ||
                    PPApplicationStatic.isOneUILauncherDefault(activity) ||
                    PPApplicationStatic.isMIUILauncherDefault(activity)/* ||
                    PPApplicationStatic.isSmartLauncherDefault(activity)*/) {
                //preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS);
                //if (preference != null)
                //    preference.setVisible(false);
                preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS);
                if (preference != null)
                    preference.setVisible(false);

                //preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS);
                //if (preference != null)
                //    preference.setVisible(false);
                preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS);
                if (preference != null)
                    preference.setVisible(false);

                //preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS);
                //if (preference != null)
                //    preference.setVisible(false);
                preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS);
                if (preference != null)
                    preference.setVisible(false);

                //preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_PROFILE_LIST_ROW_ROUNDED_CORNERS);
                //if (preference != null)
                //    preference.setVisible(false);
                preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ROUNDED_CORNERS_RADIUS);
                if (preference != null)
                    preference.setVisible(false);

                //preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ROUNDED_CORNERS);
                //if (preference != null)
                //    preference.setVisible(false);
                preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ROUNDED_CORNERS_RADIUS);
                if (preference != null)
                    preference.setVisible(false);
            }
            if (PPApplication.deviceIsPixel) {
                PPListPreference listPreference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE);
                if (listPreference != null) {
                    String value = listPreference.getValue();
                    if (value.equals("0"))
                        value = "1";
                    listPreference.setEntries(R.array.notificationIconStylePixelA12Array);
                    listPreference.setEntryValues(R.array.notificationIconStylePixelA12Values);
                    listPreference.setValue(value);
                }
            }
        }

        if (!(PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)) {
            preference = findPreference(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_SPECIAL_PROFILE_PARAMETERS_CATEGORY);
                if (preferenceCategory != null) {
                    preferenceCategory.removePreference(preference);
                    if (preferenceCategory.getPreferenceCount() == 0) {
                        //if (getActivity() != null) {
                            preference = new Preference(activity.getApplicationContext());
                            preference.setKey("specialProfileParameters_noParameters");
                            preference.setIconSpaceReserved(false);
                            preference.setLayoutResource(R.layout.mp_preference_material_widget);
                            preference.setOrder(-100);
                            preference.setTitle(R.string.phone_profiles_pref_applicationSpecialPreferencesNotAny);
                            preference.setEnabled(false);
                            preferenceCategory.addPreference(preference);
                        //}
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 29) {
            InfoDialogPreference infoDialogPreference = prefMng.findPreference(PREF_EVENT_WIFI_SCAN_THROTTLING_INFO);
            if (infoDialogPreference != null) {

                String url;
                if (DebugVersion.enabled)
                    url = PPApplication.HELP_WIFI_SCAN_THROTTLING_DEVEL;
                else
                    url = PPApplication.HELP_WIFI_SCAN_THROTTLING;

                String infoText =
                        StringConstants.TAG_BOLD_START_HTML+getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info1) + StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML +
                        getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info2) + StringConstants.TAG_BREAK_HTML +
                        getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info3) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                        StringConstants.TAG_BOLD_START_HTML+getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info4) + StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML +
                        StringConstants.TAG_BOLD_START_HTML+getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info5) + StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_BREAK_HTML +
                        getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info6) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                        getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info7) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                        getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info8) + " " +
                        getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info9) + ":"+StringConstants.TAG_BREAK_HTML +
                        StringConstants.TAG_URL_LINK_START_HTML + url + StringConstants.TAG_URL_LINK_START_URL_END_HTML + url+ StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML;

                infoDialogPreference.setInfoText(infoText);
                infoDialogPreference.setIsHtml(true);
            }
        }

        preference = prefMng.findPreference(PREF_NOTIFICATION_PROFILE_ICON_COLOR_INFO2);
        if (preference != null) {
            preference.setOnPreferenceClickListener(preference120 -> {
//                    Log.e("PhoneProfilesPrefsFragment.onActivityCreated", "preference clicked");
                scrollToPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR);
                return false;
            });
        }

        preference = prefMng.findPreference(PREF_CREATE_EDITOR_SHORTCUT);
        if (preference != null) {
            //Context appContext = activity.getApplicationContext();
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(appContext)) {

                /*List<ShortcutInfoCompat> shortcuts = ShortcutManagerCompat.getShortcuts(appContext, ShortcutManagerCompat.FLAG_MATCH_PINNED);
                boolean exists = false;
                for (ShortcutInfoCompat shortcut : shortcuts) {
                    if (shortcut.getId().equals(SHORTCUT_ID_EDITOR)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {*/
                    if (shortcutToEditorAddedReceiver == null) {
                        shortcutToEditorAddedReceiver = new ShortcutToEditorAddedBroadcastReceiver();
                        IntentFilter shortcutAddedFilter = new IntentFilter(ACTION_SHORTCUT_TO_EDITOR_ADDED);
                        int receiverFlags = 0;
                        if (Build.VERSION.SDK_INT >= 34)
                            receiverFlags = RECEIVER_NOT_EXPORTED;
                        appContext.registerReceiver(shortcutToEditorAddedReceiver, shortcutAddedFilter, receiverFlags);
                    }

                    preference.setVisible(true);
                    preference.setOnPreferenceClickListener(preference120 -> {
                        PPEditTextAlertDialog editTextDialog = new PPEditTextAlertDialog(
                                getString(R.string.shortcut_to_editor_dialog_title),
                                getString(R.string.shortcut_to_dialog_lablel),
                                getString(R.string.editor_launcher_label),
                                getString(R.string.shortcut_to_dialog_create_button),
                                getString(android.R.string.cancel),
                                (dialog1, which) -> {
                                    String iconName = "";
                                    AlertDialog dialog = (AlertDialog) dialog1;
                                    EditText editText = dialog.findViewById(R.id.dialog_with_edittext_edit);
                                    if (editText != null)
                                        iconName = editText.getText().toString();
                                    if (iconName.isEmpty())
                                        iconName = getString(R.string.editor_launcher_label);
                                    //Log.e("PhoneProfilesPrefsFragment createEditorShortcut", "iconName="+iconName);

                                    Intent shortcutIntent = new Intent(appContext, EditorActivity.class);
                                    shortcutIntent.setAction(Intent.ACTION_MAIN);
                                    shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                    ShortcutInfoCompat.Builder shortcutBuilderCompat = new ShortcutInfoCompat.Builder(appContext, SHORTCUT_ID_EDITOR);
                                    shortcutBuilderCompat.setIntent(shortcutIntent);
                                    shortcutBuilderCompat.setShortLabel(iconName);
                                    shortcutBuilderCompat.setLongLabel(getString(R.string.editor_launcher_label));
                                    shortcutBuilderCompat.setIcon(IconCompat.createWithResource(appContext, R.mipmap.ic_editor));

                                    try {
                                        Intent pinnedShortcutCallbackIntent = new Intent(ACTION_SHORTCUT_TO_EDITOR_ADDED);
                                        PendingIntent successCallback = PendingIntent.getBroadcast(appContext, 10, pinnedShortcutCallbackIntent, 0);

                                        ShortcutInfoCompat shortcutInfo = shortcutBuilderCompat.build();
                                        ShortcutManagerCompat.requestPinShortcut(appContext, shortcutInfo, successCallback.getIntentSender());
                                        //activity.setResult(Activity.RESULT_OK, intent);
                                    } catch (Exception e) {
                                        // show dialog about this crash
                                        // for Microsft laucher it is:
                                        // java.lang.IllegalArgumentException ... already exists but disabled
                                    }
                                },
                                null, null,
                                true, true, //false,
                                activity
                        );
                        if (!activity.isFinishing())
                            editTextDialog.showDialog();

                        return false;
                    });
                /*}
                else
                    preference.setVisible(false);*/
            } else
                preference.setVisible(false);
        }

        preference = prefMng.findPreference(PREF_CREATE_RESTART_EVENTS_SHORTCUT);
        if (preference != null) {
            //Context appContext = activity.getApplicationContext();
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(appContext)) {

                /*List<ShortcutInfoCompat> shortcuts = ShortcutManagerCompat.getShortcuts(appContext, ShortcutManagerCompat.FLAG_MATCH_PINNED);
                boolean exists = false;
                for (ShortcutInfoCompat shortcut : shortcuts) {
                    if (shortcut.getId().equals(SHORTCUT_ID_EDITOR)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {*/
                if (shortcutForRestartEventsAddedReceiver == null) {
                    shortcutForRestartEventsAddedReceiver = new ShortcutForRestartEventsAddedBroadcastReceiver();
                    IntentFilter shortcutAddedFilter = new IntentFilter(ACTION_SHORTCUT_FOR_RESTART_EVENTS_ADDED);
                    int receiverFlags = 0;
                    if (Build.VERSION.SDK_INT >= 34)
                        receiverFlags = RECEIVER_NOT_EXPORTED;
                    appContext.registerReceiver(shortcutForRestartEventsAddedReceiver, shortcutAddedFilter, receiverFlags);
                }

                preference.setVisible(true);
                preference.setOnPreferenceClickListener(preference120 -> {
                    PPEditTextAlertDialog editTextDialog = new PPEditTextAlertDialog(
                            getString(R.string.shortcut_for_restart_events_dialog_title),
                            getString(R.string.shortcut_to_dialog_lablel),
                            getString(R.string.menu_restart_events),
                            getString(R.string.shortcut_to_dialog_create_button),
                            getString(android.R.string.cancel),
                            (dialog1, which) -> {
                                String iconName = "";
                                AlertDialog dialog = (AlertDialog) dialog1;
                                EditText editText = dialog.findViewById(R.id.dialog_with_edittext_edit);
                                if (editText != null)
                                    iconName = editText.getText().toString();
                                if (iconName.isEmpty())
                                    iconName = getString(R.string.menu_restart_events);
                                //Log.e("PhoneProfilesPrefsFragment createEditorShortcut", "iconName="+iconName);

                                Intent shortcutIntent = new Intent(appContext, BackgroundActivateProfileActivity.class);
                                shortcutIntent.setAction(Intent.ACTION_MAIN);
                                shortcutIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
                                shortcutIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
                                shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                Profile profile = DataWrapperStatic.getNonInitializedProfile(appContext.getString(R.string.menu_restart_events),
                                        StringConstants.PROFILE_ICON_RESTART_EVENTS+"|1|1|"+ApplicationPreferences.applicationRestartEventsIconColor, 0);
                                profile.generateIconBitmap(appContext, false, 0, false);

                                ShortcutInfoCompat.Builder shortcutBuilderCompat = new ShortcutInfoCompat.Builder(appContext, "restart_events");
                                shortcutBuilderCompat.setIntent(shortcutIntent);
                                shortcutBuilderCompat.setShortLabel(iconName);
                                shortcutBuilderCompat.setLongLabel(iconName);

                                shortcutBuilderCompat.setIcon(IconCompat.createWithBitmap(profile._iconBitmap));

                                try {
                                    Intent pinnedShortcutCallbackIntent = new Intent(ACTION_SHORTCUT_FOR_RESTART_EVENTS_ADDED);
                                    PendingIntent successCallback = PendingIntent.getBroadcast(appContext, 10, pinnedShortcutCallbackIntent, 0);

                                    ShortcutInfoCompat shortcutInfo = shortcutBuilderCompat.build();
                                    ShortcutManagerCompat.requestPinShortcut(appContext, shortcutInfo, successCallback.getIntentSender());
                                    //activity.setResult(Activity.RESULT_OK, intent);
                                } catch (Exception e) {
                                    // show dialog about this crash
                                    // for Microsft laucher it is:
                                    // java.lang.IllegalArgumentException ... already exists but disabled
                                }
                            },
                            null, null,
                            true, true, //false,
                            activity
                    );
                    if (!activity.isFinishing())
                        editTextDialog.showDialog();

                    return false;
                });
                /*}
                else
                    preference.setVisible(false);*/
            } else
                preference.setVisible(false);
        }

        if (Build.VERSION.SDK_INT >= 33) {
            InfoDialogPreference infoDialogPreference = prefMng.findPreference(PREF_NOTIFICATION_SCANNING_NOTIFICATION_ACCESS_RESTRICTED_SETTINGS);
            if (infoDialogPreference != null) {
                infoDialogPreference.setOnPreferenceClickListener(preference120 -> {
//                    Log.e("PhoneProfilesPrefsFragment.onActivityCreated", "preference clicked");

                    PackageManager packageManager = activity.getPackageManager();
                    Intent _intent = packageManager.getLaunchIntentForPackage(PPApplication.FDROID_PACKAGE_NAME);
                    boolean fdroidInstalled = (_intent != null);
                    _intent = packageManager.getLaunchIntentForPackage(PPApplication.DROIDIFY_PACKAGE_NAME);
                    boolean droidifyInstalled = (_intent != null);
                    _intent = packageManager.getLaunchIntentForPackage(PPApplication.NEOSTORE_PACKAGE_NAME);
                    boolean neostoreInstalled = (_intent != null);

                    String info =
                            StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.PPP_APP_INFO_SCREEN + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                                    getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_2) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML +
                                    getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_3) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                    getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_4) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                    getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_5) + StringConstants.TAG_BREAK_HTML +
                                    getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_6) + StringConstants.TAG_DOUBLE_BREAK_HTML;

                    if (!(droidifyInstalled || neostoreInstalled || fdroidInstalled /*|| galaxyStoreInstalled*/)) {
                        info = info +
                                StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.DROIDIFY_INSTALLATION_SITE + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                                getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_10) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
                    }

                    info = info +
                            getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_7) + " " +
                            "\"" + getString(R.string.menu_import_export) + "\"/\"" + getString(R.string.menu_export) + "\"."+StringConstants.TAG_DOUBLE_BREAK_HTML +
                            getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_8) + " " +
                            "\"" + getString(R.string.menu_import_export) + "\"/\"" + getString(R.string.menu_import) + "\".";

                    infoDialogPreference.setInfoText(
                        info
                        /*StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.PPP_APP_INFO_SCREEN + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                        getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_2) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML +
                        getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_3) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                        getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_4) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                        getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_5) + StringConstants.TAG_BREAK_HTML +
                        getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_6) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                        StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.DROIDIFY_INSTALLATION_SITE + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                        getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_10) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML +
                        getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_7) + " " +
                        "\"" + getString(R.string.menu_import_export) + "\"/\"" + getString(R.string.menu_export) + "\"."+StringConstants.TAG_DOUBLE_BREAK_HTML +
                        getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_8) + " " +
                        "\"" + getString(R.string.menu_import_export) + "\"/\"" + getString(R.string.menu_import) + "\"."*/
                    );
                    infoDialogPreference.setIsHtml(true);

                    return false;
                });
            }
        }

        preference = prefMng.findPreference(PREF_CREATE_MOBILE_CELL_SCANNING_SHORTCUT);
        if (preference != null) {
            //Context appContext = activity.getApplicationContext();
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(appContext)) {

                /*List<ShortcutInfoCompat> shortcuts = ShortcutManagerCompat.getShortcuts(appContext, ShortcutManagerCompat.FLAG_MATCH_PINNED);
                boolean exists = false;
                for (ShortcutInfoCompat shortcut : shortcuts) {
                    if (shortcut.getId().equals(SHORTCUT_ID_MOBILE_CELL_SCANNING)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {*/
                    if (shortcutToMobileCellScanningAddedReceiver == null) {
                        shortcutToMobileCellScanningAddedReceiver = new ShortcutToMobileCellScanningAddedBroadcastReceiver();
                        IntentFilter shortcutAddedFilter = new IntentFilter(ACTION_SHORTCUT_TO_MOBILE_CELL_SCANNING_ADDED);
                        int receiverFlags = 0;
                        if (Build.VERSION.SDK_INT >= 34)
                            receiverFlags = RECEIVER_NOT_EXPORTED;
                        appContext.registerReceiver(shortcutToMobileCellScanningAddedReceiver, shortcutAddedFilter, receiverFlags);
                    }

                    preference.setVisible(true);
                    preference.setOnPreferenceClickListener(preference120 -> {
                        PPEditTextAlertDialog editTextDialog = new PPEditTextAlertDialog(
                                getString(R.string.shortcut_to_mobile_cells_scanning_dialog_title),
                                getString(R.string.shortcut_to_dialog_lablel),
                                getString(R.string.mobile_cells_scanning_short_shortcut_name),
                                getString(R.string.shortcut_to_dialog_create_button),
                                getString(android.R.string.cancel),
                                (dialog1, which) -> {
                                    String iconName = "";
                                    AlertDialog dialog = (AlertDialog) dialog1;
                                    EditText editText = dialog.findViewById(R.id.dialog_with_edittext_edit);
                                    if (editText != null)
                                        iconName = editText.getText().toString();
                                    if (iconName.isEmpty())
                                        iconName = getString(R.string.mobile_cells_scanning_short_shortcut_name);
                                    //Log.e("PhoneProfilesPrefsFragment createEditorShortcut", "iconName="+iconName);

                                    Intent shortcutIntent = new Intent(appContext, LaunchMobileCellsScanningActivity.class);
                                    shortcutIntent.setAction(Intent.ACTION_MAIN);
                                    shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                    ShortcutInfoCompat.Builder shortcutBuilderCompat = new ShortcutInfoCompat.Builder(appContext, SHORTCUT_ID_MOBILE_CELL_SCANNING);
                                    shortcutBuilderCompat.setIntent(shortcutIntent);
                                    shortcutBuilderCompat.setShortLabel(iconName);
                                    shortcutBuilderCompat.setLongLabel(getString(R.string.phone_profiles_pref_category_mobile_cells_scanning));
                                    shortcutBuilderCompat.setIcon(IconCompat.createWithResource(appContext, R.mipmap.ic_mobile_cell_scanning));

                                    try {
                                        Intent pinnedShortcutCallbackIntent = new Intent(ACTION_SHORTCUT_TO_MOBILE_CELL_SCANNING_ADDED);
                                        PendingIntent successCallback = PendingIntent.getBroadcast(appContext, 10, pinnedShortcutCallbackIntent,  0);

                                        ShortcutInfoCompat shortcutInfo = shortcutBuilderCompat.build();
                                        ShortcutManagerCompat.requestPinShortcut(appContext, shortcutInfo, successCallback.getIntentSender());
                                        //activity.setResult(Activity.RESULT_OK, intent);
                                    } catch (Exception e) {
                                        // show dialog about this crash
                                        // for Microsft laucher it is:
                                        // java.lang.IllegalArgumentException ... already exists but disabled
                                    }
                                },
                                null, null,
                                true, true, //false,
                                activity
                        );
                        if (!activity.isFinishing())
                            editTextDialog.showDialog();

                        return false;
                    });
                /*}
                else
                    preference.setVisible(false);*/
            } else
                preference.setVisible(false);
        }

        InfoDialogPreference infoDialogPreference = prefMng.findPreference(PREF_APPLICATION_WIDGET_DASH_CLOCK_INFO);
        if (infoDialogPreference != null) {

            final String dashClockGitHub = "https://github.com/romannurik/dashclock";
            final String dashClockAPKMirror = "https://www.apkmirror.com/apk/roman-nurik-and-ian-lake/dashclock-widget/";
            infoDialogPreference.setInfoText(
                    getString(R.string.dash_clock_widget_info_1)  + StringConstants.TAG_DOUBLE_BREAK_HTML +
                            // <ul><li>
                            StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +
                            getString(R.string.dash_clock_widget_info_2) + StringConstants.TAG_BREAK_HTML +
                            StringConstants.TAG_URL_LINK_START_HTML + dashClockGitHub + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                            dashClockGitHub + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+
                            StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML +
                            //</li>
                            StringConstants.TAG_LIST_ITEM_END_HTML +
                            //<li>
                            StringConstants.TAG_LIST_ITEM_START_HTML +
                            getString(R.string.dash_clock_widget_info_3) + StringConstants.TAG_BREAK_HTML +
                            StringConstants.TAG_URL_LINK_START_HTML + dashClockAPKMirror + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                            dashClockAPKMirror + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+
                            StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML +
                            //</li></ul>
                            StringConstants.TAG_LIST_END_LAST_ITEM_HTML
            );
            infoDialogPreference.setIsHtml(true);
        }

        if (Build.VERSION.SDK_INT >= 29) {
            Preference callScreeningPreference = prefMng.findPreference(PREF_SET_CALL_SCREENING_ROLE_SETTINGS);
            if (callScreeningPreference != null) {
                //callScreeningPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
                callScreeningPreference.setOnPreferenceClickListener(preference13 -> {
                    RoleManager roleManager = (RoleManager) appContext.getSystemService(ROLE_SERVICE);
                    if (roleManager != null) {
                        if (roleManager.isRoleHeld(ROLE_CALL_SCREENING)) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivityForResult(intent, RESULT_SET_CALL_SCREENING_ROLE_SETTINGS);
                        } else {
                            Intent intent = roleManager.createRequestRoleIntent(ROLE_CALL_SCREENING);
                            startActivityForResult(intent, RESULT_SET_CALL_SCREENING_ROLE_SETTINGS);
                        }
                    } else {
                        PPAlertDialog dialog2 = new PPAlertDialog(
                                callScreeningPreference.getTitle(),
                                getString(R.string.setting_screen_not_found_alert),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                false,
                                activity
                        );

                        if (!activity.isFinishing())
                            dialog2.showDialog();
                    }
                    return false;
                });
            }
        }

        preference = prefMng.findPreference(PREF_APPLICATION_WIDGET_PANEL_INFO);
        if (preference != null) {
            preference.setOnPreferenceClickListener(preference120 -> {
                //Log.e("PhoneProfilesPrefsFragment.onActivityCreated", "PREF_APPLICATION_WIDGET_PANEL_INFO preference clicked");

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(
                        "https://play.google.com/store/apps/details?id=com.fossor.panels"));
                intent.setPackage("com.android.vending");
                startActivity(intent);

                return false;
            });
        }

        if (Build.VERSION.SDK_INT >= 33) {
            InfoDialogPreference _infoDialogPreference = prefMng.findPreference(PREF_DEFAULT_ROLES_APPLICATIONS_RESTRICTED_SETTINGS);
            if (_infoDialogPreference != null) {
                _infoDialogPreference.setOnPreferenceClickListener(preference120 -> {
//                    Log.e("PhoneProfilesPrefsFragment.onActivityCreated", "preference clicked");

                    PackageManager packageManager = activity.getPackageManager();
                    Intent _intent = packageManager.getLaunchIntentForPackage(PPApplication.FDROID_PACKAGE_NAME);
                    boolean fdroidInstalled = (_intent != null);
                    _intent = packageManager.getLaunchIntentForPackage(PPApplication.DROIDIFY_PACKAGE_NAME);
                    boolean droidifyInstalled = (_intent != null);
                    _intent = packageManager.getLaunchIntentForPackage(PPApplication.NEOSTORE_PACKAGE_NAME);
                    boolean neostoreInstalled = (_intent != null);

                    String info =
                            StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.PPP_APP_INFO_SCREEN + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                                    getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_2) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML +
                                    getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_3) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                    getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_4) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                    getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_5) + StringConstants.TAG_BREAK_HTML +
                                    getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_6) + StringConstants.TAG_DOUBLE_BREAK_HTML;

                    if (!(droidifyInstalled || neostoreInstalled || fdroidInstalled /*|| galaxyStoreInstalled*/)) {
                        info = info +
                                StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.DROIDIFY_INSTALLATION_SITE + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                                getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_10) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
                    }

                    info = info +
                            getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_7) + " " +
                            "\"" + getString(R.string.menu_import_export) + "\"/\"" + getString(R.string.menu_export) + "\"."+StringConstants.TAG_DOUBLE_BREAK_HTML +
                            getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_8) + " " +
                            "\"" + getString(R.string.menu_import_export) + "\"/\"" + getString(R.string.menu_import) + "\".";

                    _infoDialogPreference.setInfoText(
                        info
                        /*StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.PPP_APP_INFO_SCREEN + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                                getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_2) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML +
                                getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_3) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_4) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_5) + StringConstants.TAG_BREAK_HTML +
                                getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_6) + StringConstants.TAG_DOUBLE_BREAK_HTML +
                                StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.DROIDIFY_INSTALLATION_SITE + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                                getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_10) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML +
                                getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_7) + " " +
                                "\"" + getString(R.string.menu_import_export) + "\"/\"" + getString(R.string.menu_export) + "\"."+StringConstants.TAG_DOUBLE_BREAK_HTML +
                                getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary_restrictedSettings_8) + " " +
                                "\"" + getString(R.string.menu_import_export) + "\"/\"" + getString(R.string.menu_import) + "\"."*/
                    );
                    _infoDialogPreference.setIsHtml(true);

                    return false;
                });
            }
        }

    }

    private void doOnActivityCreatedBatterySaver(String key, PhoneProfilesPrefsActivity activity) {
        Preference preference = findPreference(key);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference1 -> {
                boolean activityExists;
                Intent intent;
                activityExists = GlobalGUIRoutines.activityActionExists(Settings.ACTION_BATTERY_SAVER_SETTINGS, activity.getApplicationContext());
                intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                if (activityExists) {
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    try {
                        //noinspection deprecation
                        startActivityForResult(intent, RESULT_POWER_SAVE_MODE_SETTINGS);
                    } catch (Exception e) {
                        intent = new Intent();
                        intent.setComponent(new ComponentName(StringConstants.SETTINGS_PACKAGE_NAME, StringConstants.SETTINGS_BATTERY_SAVER_CLASS_NAME));
                        activityExists = GlobalGUIRoutines.activityIntentExists(intent, activity.getApplicationContext());
                        if (activityExists) {
                            try {
                                //noinspection deprecation
                                startActivityForResult(intent, RESULT_POWER_SAVE_MODE_SETTINGS);
                            } catch (Exception ee) {
                                PPApplicationStatic.recordException(ee);
                            }
                        }
                    }
                }
                if (!activityExists) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            preference1.getTitle(),
                            getString(R.string.setting_screen_not_found_alert),
                            getString(android.R.string.ok),
                            null,
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            false,
                            activity
                    );

                    if (!activity.isFinishing())
                        dialog.showDialog();
                }
                return false;
            });
        }
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();

        try {
            if (getActivity() != null)
                getActivity().unregisterReceiver(shortcutToEditorAddedReceiver);
        } catch (Exception ignored) {}
        shortcutToEditorAddedReceiver = null;
        try {
            if (getActivity() != null)
                getActivity().unregisterReceiver(shortcutToMobileCellScanningAddedReceiver);
        } catch (Exception ignored) {}
        shortcutToMobileCellScanningAddedReceiver = null;

        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);

            //SharedPreferences.Editor editor = applicationPreferences.edit();
            //updateSharedPreferences(editor, preferences);
            //editor.apply();
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setSummary(key);
        try {
            SharedPreferences.Editor editor = applicationPreferences.edit();
            updateSharedPreferences(editor, preferences);
            editor.apply();
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_THEME)) {
            if (getActivity() != null)
                GlobalGUIRoutines.switchNightMode(getActivity().getApplicationContext(), false);
        }
        /*if (key.equals(ApplicationPreferences.PREF_APPLICATION_LANGUAGE)) {
            if (getActivity() != null) {
                //PhoneProfilesPrefsActivity activity = (PhoneProfilesPrefsActivity)getActivity();
                GlobalGUIRoutines.setLanguage(getActivity());
                GlobalGUIRoutines.reloadActivity(getActivity(), true);
                //activity.setResult(Activity.RESULT_OK);
                //activity.finish();
            }
        }*/

        /*
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING)) {
            SharedPreferences.Editor editor = applicationPreferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, false);
            editor.apply();
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING)) {
            SharedPreferences.Editor editor = applicationPreferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, false);
            editor.apply();
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING)) {
            SharedPreferences.Editor editor = applicationPreferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, false);
            editor.apply();
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING)) {
            SharedPreferences.Editor editor = applicationPreferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, false);
            editor.apply();
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING)) {
            SharedPreferences.Editor editor = applicationPreferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, false);
            editor.apply();
        }
        */
    }

    void doOnActivityResult(int requestCode, int resultCode/*, Intent data*/)
    {
        if ((requestCode == RESULT_APPLICATION_PERMISSIONS) ||
                (requestCode == RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) ||
                //(requestCode == RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) ||
                (requestCode == RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS)) {

                Activity activity = getActivity();
                if (activity != null) {
                    Context context = activity.getApplicationContext();

                    boolean finishActivity = false;
                    boolean permissionsChanged = Permissions.getPermissionsChanged(context);

                    if (requestCode == RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) {
                        boolean canWrite = Settings.System.canWrite(context);
                        permissionsChanged = Permissions.getWriteSystemSettingsPermission(context) != canWrite;
                        if (canWrite)
                            Permissions.setShowRequestWriteSettingsPermission(context, true);
                    }
                    /*if (requestCode == RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        boolean notificationPolicyGranted = (mNotificationManager != null) && (mNotificationManager.isNotificationPolicyAccessGranted());
                        permissionsChanged = Permissions.getNotificationPolicyPermission(context) != notificationPolicyGranted;
                        if (notificationPolicyGranted)
                            Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                    }*/
                    if (requestCode == RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS) {
                        boolean canDrawOverlays = Settings.canDrawOverlays(context);
                        permissionsChanged = Permissions.getDrawOverlayPermission(context) != canDrawOverlays;
                        if (canDrawOverlays)
                            Permissions.setShowRequestDrawOverlaysPermission(context, true);
                    }
                    if (requestCode == RESULT_APPLICATION_PERMISSIONS) {
                        boolean calendarPermission = Permissions.checkCalendar(context);
                        permissionsChanged = Permissions.getCalendarPermission(context) != calendarPermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!calendarPermission);
                        if (!permissionsChanged) {
                            boolean contactsPermission = Permissions.checkContacts(context);
                            permissionsChanged = Permissions.getContactsPermission(context) != contactsPermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!contactsPermission);
                        }
                        if (!permissionsChanged) {
                            boolean locationPermission = Permissions.checkLocation(context);
                            permissionsChanged = Permissions.getLocationPermission(context) != locationPermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!locationPermission);
                        }
                        if (!permissionsChanged) {
                            boolean smsPermission = Permissions.checkSMS(/*context*/);
                            permissionsChanged = Permissions.getSMSPermission(context) != smsPermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!smsPermission);
                        }
//                        if (!permissionsChanged) {
//                            // !!! must before of Permissions.checkPhone()
//                            boolean modifyPhonePermission = Permissions.checkModifyPhone(context);
//                            permissionsChanged = Permissions.getModifyPhonePermission(context) != modifyPhonePermission;
//                            // finish Editor when permission is disabled
//                            finishActivity = permissionsChanged && (!modifyPhonePermission);
//                        }
                        if (!permissionsChanged) {
                            boolean phonePermission = Permissions.checkReadPhoneState(context);
                            permissionsChanged = Permissions.getPhonePermission(context) != phonePermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!phonePermission);
                        }
                        if (!permissionsChanged) {
                            boolean storagePermission = Permissions.checkReadStorage(context);
                            permissionsChanged = Permissions.getReadStoragePermission(context) != storagePermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!storagePermission);
                        }
                        if (!permissionsChanged) {
                            boolean storagePermission = Permissions.checkWriteStorage(context);
                            permissionsChanged = Permissions.getWriteStoragePermission(context) != storagePermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!storagePermission);
                        }
                        if (!permissionsChanged) {
                            boolean cameraPermission = Permissions.checkCamera(context);
                            permissionsChanged = Permissions.getCameraPermission(context) != cameraPermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!cameraPermission);
                        }
                        if (!permissionsChanged) {
                            boolean microphonePermission = Permissions.checkMicrophone(context);
                            permissionsChanged = Permissions.getMicrophonePermission(context) != microphonePermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!microphonePermission);
                        }
                        if (!permissionsChanged) {
                            boolean sensorsPermission = Permissions.checkSensors(/*context*/);
                            permissionsChanged = Permissions.getSensorsPermission(context) != sensorsPermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!sensorsPermission);
                        }
                    }

                    Permissions.saveAllPermissions(context, permissionsChanged);

                    if (permissionsChanged) {
                        //DataWrapper dataWrapper = new DataWrapper(context, false, 0);

//                        PPApplicationStatic.logE("[PPP_NOTIFICATION] PhoneProfilesPrefsFragment.doOnActivityResult", "call of updateGUI");
                        PPApplication.updateGUI(true, false, context);

                        if (finishActivity) {
                            activity.setResult(Activity.RESULT_CANCELED);
                            activity.finishAffinity();
                        } else {
                            setSummary(PREF_APPLICATION_PERMISSIONS);
                            setSummary(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                            //setSummary(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
                            setSummary(PREF_DRAW_OVERLAYS_PERMISSIONS);
                            setSummary(PREF_NOTIFICATION_POLICY_ACCESS_PERMISSIONS);

                            activity.setResult(Activity.RESULT_OK);
                        }
                    } else
                        activity.setResult(Activity.RESULT_CANCELED);
                }
        }

        if (requestCode == RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS) {
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING);
            setSummary(PREF_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
            MobileCellsEditorPreference preference = prefMng.findPreference(PREF_APPLICATION_EVENT_MOBILE_CELL_CONFIGURE_CELLS);
            if (preference != null) {
                preference.setLocationEnableStatus();
            }
        }
        if (requestCode == RESULT_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS) {
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING);
            setSummary(PREF_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
        }
        if (requestCode == RESULT_LOCATION_SYSTEM_SETTINGS) {
            final boolean enabled = GlobalUtils.isLocationEnabled(getContext());
            Preference preference = prefMng.findPreference(PREF_LOCATION_EDITOR);
            if (preference != null)
                preference.setEnabled(enabled);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING);
            setSummary(PREF_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
        }

        if (resultCode == RESULT_WIFI_KEEP_ON_SETTINGS) {
            setSummary(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
        }

        if (requestCode == LocationGeofencePreference.RESULT_GEOFENCE_EDITOR) {
            if (resultCode == Activity.RESULT_OK) {
                LocationGeofencePreference preference = prefMng.findPreference(PREF_LOCATION_EDITOR);
                if (preference != null) {
                    preference.setGeofenceFromEditor(/*geofenceId*/);
                }
            }
            /*if (PhoneProfilesPrefsFragment.changedLocationGeofencePreference != null) {
                if(resultCode == Activity.RESULT_OK){
                    //long geofenceId = data.getLongExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, 0);
                    // this persistGeofence, for multiselect this mus only refresh listView in preference
                    PhoneProfilesPrefsFragment.changedLocationGeofencePreference.setGeofenceFromEditor();
                    PhoneProfilesPrefsFragment.changedLocationGeofencePreference = null;
                }
            }*/
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_RINGTONE_PREFERENCE)) {
            RingtonePreference preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND);
            if (preference != null)
                preference.refreshListView();
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_MOBILE_CELLS_SCAN_DIALOG)) {
            MobileCellsEditorPreference preference = prefMng.findPreference(PREF_APPLICATION_EVENT_MOBILE_CELL_CONFIGURE_CELLS);
            if (preference != null)
                preference.refreshListView(true, true/*, Integer.MAX_VALUE*/);
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_MOBILE_CELL_NAMES_SCAN_DIALOG)) {
            MobileCellNamesPreference preference = prefMng.findPreference(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_CELL_NAMES);
            if (preference != null)
                preference.refreshListView();
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_MOBILE_CELLS_REGISTRATION_DIALOG)) {
            MobileCellsRegistrationDialogPreference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_REGISTRATION);
            if (preference != null)
                preference.startRegistration();
        }

        if (requestCode == RESULT_SET_CALL_SCREENING_ROLE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= 29) {
                setSummary(PREF_SET_CALL_SCREENING_ROLE_SETTINGS);
            }
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode);
    }


    private void initPreferenceFragment(/*Bundle savedInstanceState*/) {
        prefMng = getPreferenceManager();

        //prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        //prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        if (getContext() != null) {
            applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
        }

        preferences.registerOnSharedPreferenceChangeListener(this);

        /*if (savedInstanceState == null) {
            SharedPreferences.Editor editor = preferences.edit();
            updateSharedPreferences(editor, applicationPreferences);
            editor.apply();
        }*/
    }

    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
    }

    void updateSharedPreferences() {
        SharedPreferences.Editor editor = applicationPreferences.edit();
        updateSharedPreferences(editor, preferences);
        editor.apply();
    }

    void loadSharedPreferences(SharedPreferences preferences, SharedPreferences applicationPreferences) {
        SharedPreferences.Editor editor = preferences.edit();
        updateSharedPreferences(editor, applicationPreferences);
        editor.apply();
    }

    private void updateAllSummary()
    {
        if (getActivity() == null)
            return;

        PhoneProfilesPrefsActivity activity = (PhoneProfilesPrefsActivity) getActivity();

        setSummary(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ALERT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_CLOSE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_HOME_LAUNCHER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_LANGUAGE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_THEME);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_NIGHT_MODE_OFF_THEME);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_HIDE_EVENT_DETAILS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_HIDE_EVENT_DETAILS_FOR_START_ORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_HEADER);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_HEADER);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_TOAST);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
        //setSummary(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE);

        setSummary(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_HEADER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_COMPACT_GRID);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T);
        setSummary(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_HEADER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_B);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_TYPE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_CUSTOM_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_VERTICAL_POSITION);
        if (Build.VERSION.SDK_INT >= 30)
            setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_CHANGE_COLOR_BY_NIGHT_MODE);

        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_START_EVENTS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
        setSummary(PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
        //setSummary(PREF_APPLICATION_POWER_MANAGER);
        setSummary(PREF_SYSTEM_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_LOCATION_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_WIFI_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_BLUETOOTH_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_MOBILE_CELL_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_ORIENTATION_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_GRANT_ROOT_PERMISSION);
        setSummary(PREF_GRANT_G1_PERMISSION);
        setSummary(PREF_GRANT_SHIZUKU_PERMISSION);
        setSummary(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
        setSummary(PREF_NOTIFICATION_POLICY_ACCESS_PERMISSIONS);
        setSummary(PREF_DRAW_OVERLAYS_PERMISSIONS);
        setSummary(PREF_APPLICATION_PERMISSIONS);
        setSummary(PREF_AUTOSTART_MANAGER);
        setSummary(PREF_ACTIVATED_PROFILE_NOTIFICATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_USAGE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING);
        setSummary(PREF_LOCATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(PREF_LOCATION_EDITOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_USE_GPS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING);
        setSummary(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
        setSummary(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING);
        setSummary(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING);
        setSummary(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS);
        setSummary(PREF_ACTIVATED_PROFILE_NOTIFICATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED);
        setSummary(PREF_APPLICATION_EVENT_MOBILE_CELL_CONFIGURE_CELLS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_NOTIFICATION_STYLE);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(PREF_PERIODIC_SCANNING_POWER_SAVE_MODE_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(PREF_NOTIFICATION_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_NUM_COLUMNS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_VIBRATE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_ADD_RESTART_EVENTS_INTO_PROFILE_LIST);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_INCREASE_BRIGHTNESS);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LAYOUT_HEIGHT);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_HIGHER_LAYOUT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LAYOUT_HEIGHT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_FILL_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_FILL_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_FILL_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_USE_DYNAMIC_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_USE_DYNAMIC_COLOR);

        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_B);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_SHOW_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_TYPE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ROUNDED_CORNERS_RADIUS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LAYOUT_HEIGHT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_CHANGE_COLOR_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_USE_DYNAMIC_COLORS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ARROWS_MARK_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_NUMBER_OF_PROFILES_PER_PAGE);

        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_DISPLAY_NOTIFICATION);
        //setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_SHOW_IN_STATUS_BAR);
        //setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_HIDE_IN_LOCKSCREEN);
        //setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_STATUS_BAR_STYLE);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_CUSTOM_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ARROWS_MARK_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_NUMBER_OF_PROFILES_PER_PAGE);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS);

        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_HEADER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_B);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_TYPE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_CUSTOM_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_VERTICAL_POSITION);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_CHANGE_COLOR_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_OFF);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_ON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_SHOW_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ROUNDED_CORNERS_RADIUS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_USE_DYNAMIC_COLORS);

        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_BORDER_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_BORDER_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ICON_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS_CHANGE_BY_NIGHT_MODE);

        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY);

        //setSummary(PREF_NOTIFICATION_PROFILE_ICON_COLOR_INFO1);
        setSummary(PREF_NOTIFICATION_PROFILE_ICON_COLOR_INFO2);
        setSummary(PREF_SET_CALL_SCREENING_ROLE_SETTINGS);
        setSummary(PREF_APPLICATION_WIDGET_PANEL_INFO);

        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, true, activity.getApplicationContext());
        if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            /*prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_RESCAN).setEnabled(false);*/
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, false);
            editor.apply();
            // this disables the entire preferences screen
            Preference preference = prefMng.findPreference(PREF_WIFI_SCANNING_CATEGORY);
            if (preference != null)
                preference.setEnabled(false);
        }

        preferenceAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, true, activity.getApplicationContext());
        if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            /*prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN).setEnabled(false);
            if (WifiBluetoothScanner.bluetoothLESupported(preferencesActivity.getApplicationContext()))
                prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION).setEnabled(false);*/
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, false);
            editor.apply();
            // this disables the entire preferences screen
            Preference preference = prefMng.findPreference(PREF_BLUETOOTH_SCANNING_CATEGORY);
            if (preference != null)
                preference.setEnabled(false);
        }

        preferenceAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, true, activity.getApplicationContext());
        if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            // this disables the entire preferences screen
            Preference preference = prefMng.findPreference(PREF_ORIENTATION_SCANNING_CATEGORY);
            if (preference != null)
                preference.setEnabled(false);
        }

        preferenceAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, true, activity.getApplicationContext());
        if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            // this disables the entire preferences screen
            Preference preference = prefMng.findPreference(PREF_MOBILE_CELLS_SCANNING_CATEGORY);
            if (preference != null)
                preference.setEnabled(false);
        }

        if (!GlobalUtils.isLocationEnabled(activity.getApplicationContext())) {
            Preference preference = prefMng.findPreference(PREF_LOCATION_EDITOR);
            if (preference != null)
                preference.setEnabled(false);
        }

    }

    /** @noinspection DataFlowIssue*/
    private void setEnabledWidgets(String key) {
        boolean keyIsWidgetIconChangeColorByNightMode = false;
        boolean keyIsWidgetOneRowChangeColorByNightMode = false;
        boolean keyIsWidgetListChangeColorByNightMode = false;
        boolean keyIsWidgetPanelChangeColorByNightMode = false;
        boolean keyIsWidgetOneRowProfileListChangeColorByNightMode = false;
        boolean keyIsWidgetIconUseDynamicColors = false;
        boolean keyIsWidgetOneRowUseDynamicColors = false;
        boolean keyIsWidgetListUseDynamicColors = false;
        boolean keyIsWidgetOneRowProfileListUseDynamicColors = false;
        boolean keyIsWidgetPanelUseDynamicColors = false;
        boolean keyIsWidgetOneRowPrefIndicatorUseDynamicColors = false;
        boolean keyIsWidgetListPrefIndicatorUseDynamicColors = false;
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE)) {
            keyIsWidgetIconChangeColorByNightMode = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE)) {
            keyIsWidgetOneRowChangeColorByNightMode = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE)) {
            keyIsWidgetListChangeColorByNightMode = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_CHANGE_COLOR_BY_NIGHT_MODE)) {
            keyIsWidgetPanelChangeColorByNightMode = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_CHANGE_COLOR_BY_NIGHT_MODE)) {
            keyIsWidgetOneRowProfileListChangeColorByNightMode = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS)) {
            keyIsWidgetIconUseDynamicColors = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS)) {
            keyIsWidgetOneRowUseDynamicColors = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS)) {
            keyIsWidgetListUseDynamicColors = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_USE_DYNAMIC_COLORS)) {
            keyIsWidgetOneRowProfileListUseDynamicColors = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_USE_DYNAMIC_COLOR)) {
            keyIsWidgetOneRowPrefIndicatorUseDynamicColors = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_USE_DYNAMIC_COLOR)) {
            keyIsWidgetListPrefIndicatorUseDynamicColors = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_USE_DYNAMIC_COLORS)) {
            keyIsWidgetPanelUseDynamicColors = true;
        }

        boolean changeWidgetIconColorsByNightMode = false;
        boolean changeWidgetOneRowColorsByNightMode = false;
        boolean changeWidgetListColorsByNightMode = false;
        boolean changeWidgetOneRowProfileListColorsByNightMode = false;
        boolean changeWidgetPanelColorsByNightMode = false;
        if (Build.VERSION.SDK_INT >= 30) {
            changeWidgetIconColorsByNightMode = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE, false);
            changeWidgetOneRowColorsByNightMode = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE, false);
            changeWidgetListColorsByNightMode = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE, false);
            changeWidgetOneRowProfileListColorsByNightMode = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_CHANGE_COLOR_BY_NIGHT_MODE, false);
            changeWidgetPanelColorsByNightMode = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_CHANGE_COLOR_BY_NIGHT_MODE, false);
        }

        boolean useDynamicColorsWidgetIcon = false;
        boolean useDynamicColorsWidgetOneRow = false;
        boolean useDynamicColorsWidgetList = false;
        boolean useDynamicColorsWidgetOneRowProfileList = false;
        boolean useDynamicColorsWidgetOneRowPrefIndicator = false;
        boolean useDynamicColorsWidgetListPrefIndicator = false;
        boolean useDynamicColorsWidgetPanel = false;
        if (Build.VERSION.SDK_INT >= 31) {
            useDynamicColorsWidgetIcon = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS, false);
            useDynamicColorsWidgetOneRow = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS, false);
            useDynamicColorsWidgetList = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS, false);
            useDynamicColorsWidgetOneRowProfileList = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_USE_DYNAMIC_COLORS, false);
            useDynamicColorsWidgetOneRowPrefIndicator = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_USE_DYNAMIC_COLOR, false);
            useDynamicColorsWidgetListPrefIndicator = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_USE_DYNAMIC_COLOR, false);
            useDynamicColorsWidgetPanel = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_USE_DYNAMIC_COLORS, false);
        }

        //boolean roundedCornersListEnabled =
        //        preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, true);
        boolean preferenceIndicatorsListEnabled =
                preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true);
        boolean monochromeIconList =
                preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0).equals("1");
        //boolean roundedCornersOneRowEnabled =
        //        preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, true);
        boolean preferenceIndicatorsOneRowEnabled =
                preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, true);
        boolean monochromeIconOneRow =
                preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, "0").equals("1");
        //boolean roundedCornersIconEnabled =
        //        preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, true);
        boolean monochromeIconIcon =
                preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR, "0").equals("1");
        boolean hideProfileNameIcon =
                preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false);
        boolean monochromeIconWidgetPanel =
                preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_COLOR, "0").equals("1");
        boolean monochromeIconShortcut =
                preferences.getString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR, "0").equals("1");
        boolean monochromeIconOneRowProfileList =
                preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ICON_COLOR, "0").equals("1");

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T)
                || keyIsWidgetListUseDynamicColors || keyIsWidgetListChangeColorByNightMode) {
            boolean enableLightnessT = true;
            if (changeWidgetListColorsByNightMode)
                enableLightnessT = !useDynamicColorsWidgetList;

            Preference _preference = prefMng.findPreference(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T_INFO);
            if (_preference != null)
                _preference.setEnabled(enableLightnessT);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T);
            if (_preference != null)
                _preference.setEnabled(enableLightnessT);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T_CHANGE_BY_NIGHT_MODE);
            if (_preference != null)
                _preference.setEnabled(enableLightnessT);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T)
                || keyIsWidgetOneRowUseDynamicColors || keyIsWidgetOneRowChangeColorByNightMode) {
            boolean enableLightnessT = true;
            if (changeWidgetOneRowColorsByNightMode)
                enableLightnessT = !useDynamicColorsWidgetOneRow;

            Preference _preference = prefMng.findPreference(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T_INFO);
            if (_preference != null)
                _preference.setEnabled(enableLightnessT);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T);
            if (_preference != null)
                _preference.setEnabled(enableLightnessT);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T_CHANGE_BY_NIGHT_MODE);
            if (_preference != null)
                _preference.setEnabled(enableLightnessT);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T)
                || keyIsWidgetIconUseDynamicColors || keyIsWidgetIconChangeColorByNightMode) {
            boolean enableLightnessT = true;
            if (changeWidgetIconColorsByNightMode)
                enableLightnessT = !useDynamicColorsWidgetIcon;

            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T);
            if (_preference != null)
                _preference.setEnabled(enableLightnessT);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T_CHANGE_BY_NIGHT_MODE);
            if (_preference != null)
                _preference.setEnabled(enableLightnessT);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ARROWS_MARK_LIGHTNESS) || keyIsWidgetOneRowProfileListUseDynamicColors) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ARROWS_MARK_LIGHTNESS);
            if (_preference != null)
                _preference.setEnabled(!useDynamicColorsWidgetOneRowProfileList);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE) || keyIsWidgetListChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE);
            if (_preference != null)
                _preference.setEnabled(!changeWidgetListColorsByNightMode);
            if (changeWidgetListColorsByNightMode) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false)) {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(true);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(false);
                } else {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(false);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(true);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE) || keyIsWidgetOneRowChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE);
            if (_preference != null)
                _preference.setEnabled(!changeWidgetOneRowColorsByNightMode);
            if (changeWidgetOneRowColorsByNightMode) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false)) {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(true);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(false);
                } else {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(false);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(true);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE) || keyIsWidgetIconChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE);
            if (_preference != null)
                _preference.setEnabled(!changeWidgetIconColorsByNightMode);
            if (changeWidgetIconColorsByNightMode) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false)) {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(true);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(false);
                } else {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(false);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(true);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_TYPE) || keyIsWidgetOneRowProfileListChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_TYPE);
            if (_preference != null)
                _preference.setEnabled(!changeWidgetOneRowProfileListColorsByNightMode);
            if (changeWidgetOneRowProfileListColorsByNightMode) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_TYPE, false)) {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(true);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(false);
                } else {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(false);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(true);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER) ||
                keyIsWidgetIconUseDynamicColors || keyIsWidgetIconChangeColorByNightMode) {
            boolean enableLightnessBorder = true;
            if (changeWidgetIconColorsByNightMode)
                enableLightnessBorder = !useDynamicColorsWidgetIcon;

            Preference _preference;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER);
            /*if (!enableLightnessBorder) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {*/
                if (_preference != null) {
                    _preference.setEnabled(
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, false));
                }
            //}
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER_CHANGE_BY_NIGHT_MODE);
            if (!enableLightnessBorder) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, false));
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER) ||
                keyIsWidgetOneRowUseDynamicColors || keyIsWidgetOneRowChangeColorByNightMode) {
            boolean enableLightnessBorder = true;
            if (changeWidgetOneRowColorsByNightMode)
                enableLightnessBorder = !useDynamicColorsWidgetOneRow;

            Preference _preference;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER);
            /*if (!enableLightnessBorder) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {*/
                if (_preference != null) {
                    _preference.setEnabled(
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, false));
                }
            //}
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER_CHANGE_BY_NIGHT_MODE);
            if (!enableLightnessBorder) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, false));
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER) ||
                keyIsWidgetListUseDynamicColors || keyIsWidgetListChangeColorByNightMode) {
            boolean enableLightnessBorder = true;
            if (changeWidgetListColorsByNightMode)
                enableLightnessBorder = !useDynamicColorsWidgetList;

            Preference _preference;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER);
            /*if (!enableLightnessBorder) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }

            } else {*/
                if (_preference != null) {
                    _preference.setEnabled(
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, false));
                }
            //}
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER_CHANGE_BY_NIGHT_MODE);
            if (!enableLightnessBorder) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, false));
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_SHOW_BORDER) ||
                keyIsWidgetOneRowProfileListUseDynamicColors || keyIsWidgetOneRowProfileListChangeColorByNightMode) {
            boolean enableLightnessBorder = true;
            if (changeWidgetOneRowProfileListColorsByNightMode)
                enableLightnessBorder = !useDynamicColorsWidgetOneRowProfileList;

            Preference _preference;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_BORDER);
            /*if (!enableLightnessBorder) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {*/
                if (_preference != null) {
                    _preference.setEnabled(
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_SHOW_BORDER, false));
                }
            //}
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_LIGHTNESS_BORDER_CHANGE_BY_NIGHT_MODE);
            if (!enableLightnessBorder) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_SHOW_BORDER, false));
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR)
                || keyIsWidgetIconChangeColorByNightMode) {
            //boolean enableIconLightness = true;
            //if (changeWidgetIconColorsByNightMode)
            //    enableIconLightness = !useDynamicColorsWidgetIcon;
            //noinspection UnnecessaryLocalVariable
            boolean enableIconLightness = changeWidgetIconColorsByNightMode;

            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR);
            //if (_preference != null)
            //    _preference.setEnabled(!changeWidgetIconColorsByNightMode);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS);
            /*if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {*/
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconIcon);
                }
            //}
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS);
            if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconIcon);
                }
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
            if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconIcon);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR)
                || keyIsWidgetOneRowChangeColorByNightMode) {
            /*boolean enableIconLightness = true;
            if (changeWidgetOneRowColorsByNightMode)
                enableIconLightness = !useDynamicColorsWidgetOneRow;*/
            //noinspection UnnecessaryLocalVariable
            boolean enableIconLightness = changeWidgetOneRowColorsByNightMode;

            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS);
            /*if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {*/
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconOneRow);
                }
            //}
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS);
            if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconOneRow);
                }
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
            if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconOneRow);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR)
                || keyIsWidgetListChangeColorByNightMode) {
            /*boolean enableIconLightness = true;
            if (changeWidgetListColorsByNightMode)
                enableIconLightness = !useDynamicColorsWidgetList;*/
            //noinspection UnnecessaryLocalVariable
            boolean enableIconLightness = keyIsWidgetListChangeColorByNightMode;

            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
            /*if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {*/
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconList);
                }
            //}
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS);
            if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconList);
                }
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
            if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconList);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR)) {
            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS);
            if (_preference != null) {
                _preference.setEnabled(monochromeIconShortcut);
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS);
            if (_preference != null) {
                _preference.setEnabled(monochromeIconShortcut);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ICON_COLOR)
                || keyIsWidgetOneRowProfileListUseDynamicColors || keyIsWidgetOneRowProfileListChangeColorByNightMode) {
            /*boolean enableIconLightness = true;
            if (changeWidgetOneRowProfileListColorsByNightMode)
                enableIconLightness = !useDynamicColorsWidgetOneRowProfileList;*/
            //noinspection UnnecessaryLocalVariable
            boolean enableIconLightness = changeWidgetOneRowProfileListColorsByNightMode;

            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ICON_LIGHTNESS);
            /*if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {*/
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconOneRowProfileList);
                }
            //}
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS);
            /*if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {*/
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconOneRowProfileList);
                }
            //}
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ICON_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
            if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconOneRowProfileList);
                }
            }
            boolean enableLightnessArrows = true;
            if (changeWidgetOneRowProfileListColorsByNightMode)
                enableLightnessArrows = !useDynamicColorsWidgetOneRowProfileList;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_ARROWS_MARK_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
            if (_preference != null)
                //noinspection ConstantValue
                _preference.setEnabled((!useDynamicColorsWidgetOneRowProfileList) && enableLightnessArrows);
        }

        //if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS)) {
        //    Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS);
        //    if (_preference != null)
        //        _preference.setEnabled(roundedCornersListEnabled);
        //}
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR)
                || keyIsWidgetListChangeColorByNightMode
                || keyIsWidgetListUseDynamicColors
                || keyIsWidgetListPrefIndicatorUseDynamicColors) {
            boolean enableLightnessPrefIndicator = true;
            if (changeWidgetListColorsByNightMode)
                enableLightnessPrefIndicator = (!useDynamicColorsWidgetList) &&
                        (!useDynamicColorsWidgetListPrefIndicator);

            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS);
            if (_preference != null) {
                _preference.setEnabled(preferenceIndicatorsListEnabled);
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
            if (_preference != null) {
                if (!enableLightnessPrefIndicator) {
                    _preference.setEnabled(false);
                } else {
                    _preference.setEnabled(preferenceIndicatorsListEnabled);
                }
            }
        }
        //if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS)) {
        //    Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS);
        //    if (_preference != null)
        //        _preference.setEnabled(roundedCornersOneRowEnabled);
        //}
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR)
                || keyIsWidgetOneRowChangeColorByNightMode
                || keyIsWidgetOneRowUseDynamicColors
                || keyIsWidgetOneRowPrefIndicatorUseDynamicColors) {
            boolean enableLightnessPrefIndicator = true;
            if (changeWidgetOneRowColorsByNightMode)
                enableLightnessPrefIndicator = (!useDynamicColorsWidgetOneRow) &&
                        (!useDynamicColorsWidgetOneRowPrefIndicator);

            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS);
            if (_preference != null) {
                _preference.setEnabled(preferenceIndicatorsOneRowEnabled);
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
            if (_preference != null) {
                if (!enableLightnessPrefIndicator) {
                    _preference.setEnabled(false);
                } else {
                    _preference.setEnabled(preferenceIndicatorsOneRowEnabled);
                }
            }
        }
        //if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS)) {
        //    Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS);
        //    if (_preference != null)
        //        _preference.setEnabled(roundedCornersIconEnabled);
        //}
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME) || keyIsWidgetIconUseDynamicColors) {
            boolean enableLightnessT = true;
            if (changeWidgetIconColorsByNightMode)
                enableLightnessT = !useDynamicColorsWidgetIcon;

            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T);
            if (_preference != null) {
                if (!enableLightnessT) {
                    _preference.setEnabled(false);
                } else {
                    _preference.setEnabled(!hideProfileNameIcon);
                }
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T_CHANGE_BY_NIGHT_MODE);
            if (_preference != null) {
                if (!enableLightnessT) {
                    _preference.setEnabled(false);
                } else {
                    _preference.setEnabled(!hideProfileNameIcon);
                }
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION);
            if (_preference != null)
                _preference.setEnabled(!hideProfileNameIcon);
        }

        ////
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_TYPE) || keyIsWidgetPanelChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_TYPE);
            if (_preference != null)
                _preference.setEnabled(!changeWidgetPanelColorsByNightMode);
            if (changeWidgetPanelColorsByNightMode) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_TYPE, false)) {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(true);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(false);
                } else {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(false);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(true);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_COLOR)
                || keyIsWidgetPanelChangeColorByNightMode) {
            //boolean enableIconLightness = true;
            //if (changeWidgetPanelColorsByNightMode)
            //    enableIconLightness = !useDynamicColorsWidgetPanel;
            //noinspection UnnecessaryLocalVariable
            boolean enableIconLightness = changeWidgetIconColorsByNightMode;

            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_LIGHTNESS);
            /*if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {*/
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconWidgetPanel);
                }
            //}
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_CUSTOM_ICON_LIGHTNESS);
            if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconWidgetPanel);
                }
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_ICON_LIGHTNESS_CHANGE_BY_NIGHT_MODE);
            if (!enableIconLightness) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconWidgetPanel);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_SHOW_BORDER) ||
                keyIsWidgetPanelUseDynamicColors || keyIsWidgetPanelChangeColorByNightMode) {
            boolean enableLightnessBorder = true;
            if (changeWidgetPanelColorsByNightMode)
                enableLightnessBorder = !useDynamicColorsWidgetPanel;

            Preference _preference;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_BORDER);
            /*if (!enableLightnessBorder) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }

            } else {*/
                if (_preference != null) {
                    _preference.setEnabled(
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_SHOW_BORDER, false));
                }
            //}
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_BORDER_CHANGE_BY_NIGHT_MODE);
            if (!enableLightnessBorder) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_SHOW_BORDER, false));
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T)
                || keyIsWidgetPanelUseDynamicColors || keyIsWidgetPanelChangeColorByNightMode) {
            boolean enableLightnessT = true;
            if (changeWidgetPanelColorsByNightMode)
                enableLightnessT = !useDynamicColorsWidgetPanel;

            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T);
            if (_preference != null)
                _preference.setEnabled(enableLightnessT);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_LIGHTNESS_T_CHANGE_BY_NIGHT_MODE);
            if (_preference != null)
                _preference.setEnabled(enableLightnessT);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_ON) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_OFF) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_USE_DYNAMIC_COLORS) ||
                keyIsWidgetPanelChangeColorByNightMode) {
            Preference _preference;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_OFF);
            if (_preference != null)
                _preference.setEnabled(changeWidgetPanelColorsByNightMode && (!useDynamicColorsWidgetPanel));
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_PANEL_BACKGROUND_COLOR_NIGHT_MODE_ON);
            if (_preference != null)
                _preference.setEnabled(changeWidgetPanelColorsByNightMode && (!useDynamicColorsWidgetPanel));
        }

        ////

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_ON) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_OFF) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS) ||
                keyIsWidgetIconChangeColorByNightMode) {
            Preference _preference;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_OFF);
            if (_preference != null)
                _preference.setEnabled(changeWidgetIconColorsByNightMode && (!useDynamicColorsWidgetIcon));
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_ON);
            if (_preference != null)
                _preference.setEnabled(changeWidgetIconColorsByNightMode && (!useDynamicColorsWidgetIcon));
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_ON) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_OFF) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS) ||
                keyIsWidgetOneRowChangeColorByNightMode) {
            Preference _preference;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_OFF);
            if (_preference != null)
                _preference.setEnabled(changeWidgetOneRowColorsByNightMode && (!useDynamicColorsWidgetOneRow));
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_ON);
            if (_preference != null)
                _preference.setEnabled(changeWidgetOneRowColorsByNightMode && (!useDynamicColorsWidgetOneRow));
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS);
            if (_preference != null) {
                _preference.setEnabled(preferenceIndicatorsOneRowEnabled);
                /*if (changeWidgetOneRowColorsByNightMode) {
                    //if (useDynamicColorsWidgetOneRow)
                    //    _preference.setEnabled(false);
                    //else
                        _preference.setEnabled(preferenceIndicatorsOneRowEnabled);
                } else {
                    _preference.setEnabled(preferenceIndicatorsOneRowEnabled);
                }*/
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS) ||
                keyIsWidgetListChangeColorByNightMode) {
            Preference _preference;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF);
            if (_preference != null)
                _preference.setEnabled(changeWidgetListColorsByNightMode && (!useDynamicColorsWidgetList));
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON);
            if (_preference != null)
                _preference.setEnabled(changeWidgetListColorsByNightMode && (!useDynamicColorsWidgetList));
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS);
            if (_preference != null) {
                _preference.setEnabled(preferenceIndicatorsListEnabled);
                /*if (changeWidgetListColorsByNightMode) {
                    //if (useDynamicColorsWidgetList)
                    //    _preference.setEnabled(false);
                    //else
                        _preference.setEnabled(preferenceIndicatorsListEnabled);
                } else {
                    _preference.setEnabled(preferenceIndicatorsListEnabled);
                }*/
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_USE_DYNAMIC_COLORS) ||
                keyIsWidgetOneRowProfileListChangeColorByNightMode) {
            Preference _preference;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF);
            if (_preference != null)
                _preference.setEnabled(changeWidgetOneRowProfileListColorsByNightMode && (!useDynamicColorsWidgetOneRowProfileList));
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PROFILE_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON);
            if (_preference != null)
                _preference.setEnabled(changeWidgetOneRowProfileListColorsByNightMode && (!useDynamicColorsWidgetOneRowProfileList));
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_USE_DYNAMIC_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_USE_DYNAMIC_COLOR);
            if (_preference != null) {
                _preference.setEnabled(
                        preferenceIndicatorsOneRowEnabled &&
                        changeWidgetOneRowColorsByNightMode &&
                        !monochromeIconOneRow &&
                        !useDynamicColorsWidgetOneRow);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_USE_DYNAMIC_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_USE_DYNAMIC_COLOR);
            if (_preference != null) {
                _preference.setEnabled(
                        preferenceIndicatorsListEnabled &&
                        changeWidgetListColorsByNightMode &&
                        !monochromeIconList &&
                        !useDynamicColorsWidgetList);
            }
        }
    }

    private void setEnabled(String key) {

        Preference preference = prefMng.findPreference(key);

        if (preference == null)
            return;

        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT) ||
                //key.equals(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_NOTIFICATION_STYLE) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS)) {
            String notificationStyle = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_NOTIFICATION_STYLE, "0");
            if (notificationStyle.equals("0")) {
                String backgroundColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
                //boolean nightMode = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE, false);
                boolean useDecoration = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, true);

                Preference _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR);
                if (_preference != null)
                    _preference.setEnabled(backgroundColor.equals("0") || backgroundColor.equals("5"));
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION);
                if (_preference != null) {
                    //if (Build.VERSION.SDK_INT < 29)
                    //    _preference.setEnabled(backgroundColor.equals("0") && (!nightMode));
                    //else
                    _preference.setEnabled(backgroundColor.equals("0"));
                }
                _preference = findPreference(PREF_NOTIFICATION_USE_DECORATOR_INFO);
                if (_preference != null) {
                    //if (Build.VERSION.SDK_INT < 29)
                    //    _preference.setEnabled(backgroundColor.equals("0") && (!nightMode));
                    //else
                    _preference.setEnabled(backgroundColor.equals("0"));
                }

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
                if (_preference != null) {
                    //if (Build.VERSION.SDK_INT < 29)
                    //    _preference.setEnabled(useDecoration && backgroundColor.equals("0") && (!nightMode));
                    //else
                    _preference.setEnabled(useDecoration && backgroundColor.equals("0"));
                }
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON);
                if (_preference != null) {
                    //if (Build.VERSION.SDK_INT < 29)
                    //    _preference.setEnabled(useDecoration && backgroundColor.equals("0") && (!nightMode));
                    //else
                    _preference.setEnabled(useDecoration && backgroundColor.equals("0"));
                }

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR);
                if (_preference != null)
                    _preference.setEnabled(backgroundColor.equals("5"));

                _preference = findPreference(PREF_NOTIFICATION_BACKGROUND_COLOR_INFO);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                //_preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE);
                //if (_preference != null)
                //    _preference.setEnabled(true);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE);
                if (_preference != null)
                    _preference.setEnabled(true);

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON);
                if (_preference != null)
                    _preference.setEnabled(useDecoration);
                // show profile icon for Android 12+ is better false
                boolean showProfileIcon = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON,
                        Build.VERSION.SDK_INT < 31);
                String profileIconColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR, "0");
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS);
                if (_preference != null)
                    _preference.setEnabled(showProfileIcon && (profileIconColor.equals("1")));
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS);
                if (_preference != null)
                    _preference.setEnabled(showProfileIcon && (profileIconColor.equals("1")));

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS);
                SwitchPreferenceCompat __preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR);
                if ((_preference != null) && (__preference != null)) {
                    _preference.setEnabled(__preference.isChecked());
                }
            } else {
                Preference _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
                if (_preference != null)
                    _preference.setEnabled(true);
                // dislabe, restart events action button is forced for native style
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON);
                if (_preference != null)
                    _preference.setEnabled(false);

                _preference = findPreference(PREF_NOTIFICATION_BACKGROUND_COLOR_INFO);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                //_preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE);
                //if (_preference != null)
                //    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference(PREF_NOTIFICATION_USE_DECORATOR_INFO);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE);
                if (_preference != null)
                    _preference.setEnabled(false);

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON);
                if (_preference != null)
                    _preference.setEnabled(true);
                // show profile icon for Android 12+ is better false
                boolean showProfileIcon = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON,
                        Build.VERSION.SDK_INT < 31);
                String profileIconColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR, "0");
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS);
                if (_preference != null)
                    _preference.setEnabled(showProfileIcon && (profileIconColor.equals("1")));
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS);
                if (_preference != null)
                    _preference.setEnabled(showProfileIcon && (profileIconColor.equals("1")));

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
        }
        /*if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION)) {
            boolean useDecoration = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, true);
            String backgroundColor;// = "0";
            //if (Build.VERSION.SDK_INT < 29)
                backgroundColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
            Preference _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
            if (_preference != null)
                _preference.setEnabled(useDecoration && backgroundColor.equals("0"));
        }*/

        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_DISPLAY_NOTIFICATION) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_COLOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_CUSTOM_COLOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ARROWS_MARK_LIGHTNESS) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_COLOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_LIGHTNESS) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS)) {

            boolean displayNotification = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_DISPLAY_NOTIFICATION, false);

            if (displayNotification) {
                String backgroundColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_COLOR, "0");
                String iconColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_COLOR, "0");

                Preference _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ARROWS_MARK_LIGHTNESS);
                if (_preference != null)
                    _preference.setEnabled((Build.VERSION.SDK_INT < 31) ||
                            (!backgroundColor.equals("0")) || iconColor.equals("1"));

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_CUSTOM_COLOR);
                if (_preference != null)
                    _preference.setEnabled(backgroundColor.equals("5"));

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);

                String profileIconColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_COLOR, "0");
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_ICON_LIGHTNESS);
                if (_preference != null)
                    _preference.setEnabled(profileIconColor.equals("1"));
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_CUSTOM_ICON_LIGHTNESS);
                if (_preference != null)
                    _preference.setEnabled(profileIconColor.equals("1"));
            }
        }

        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_CUSTOM_COLOR)) {
            return;
        }

        /*
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCANNING_IGNORE_HOTSPOT) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF)) {
            preference = findPreference(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCANNING_IGNORE_HOTSPOT);
            if (preference != null) {
                SwitchPreferenceCompat scanningEnabledPreference = findPreference(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING);
                SwitchPreferenceCompat scanIfWifiOffPreference = findPreference(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF);
                if ((scanningEnabledPreference != null) && (scanIfWifiOffPreference != null))
                    preference.setEnabled(scanningEnabledPreference.isChecked() && scanIfWifiOffPreference.isChecked());
            }
        }
        */

        setEnabledWidgets(key);

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
            if (_preference != null) {
                boolean enabled;
                String value = preferences.getString(key, "0");
                if (!value.equals("0"))
                    enabled = value.equals("1");
                else
                    enabled = ApplicationPreferences.prefMergedRingNotificationVolumes;
                //Log.d("PhoneProfilesPrefsFragment.setSummary","enabled="+enabled);
                _preference.setEnabled(enabled);
            }
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE)) {
            String stringValue = preferences.getString(key, "");
            long lProfileId;
            try {
                lProfileId = Long.parseLong(stringValue);
            } catch (Exception e) {
                lProfileId = 0;
            }

            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND);
            if (_preference != null)
                _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);

            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE);
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            boolean hasVibrator = (vibrator != null) && vibrator.hasVibrator();
            if (hasVibrator) {
                if (_preference != null) {
                    _preference.setVisible(true);
                    _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);
                }
            } else {
                if (_preference != null)
                    _preference.setVisible(false);
            }

            //_preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_USAGE);
            //if (_preference != null)
            //    _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);
        }
    }

    private void setSummary(String key) {

        setEnabled(key);

        Preference preference = prefMng.findPreference(key);

        if (preference == null)
            return;

        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary("");
            }
            else
                preference.setSummary("");
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference(PREF_PERIODIC_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventLocationDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary("");
            }
            else
                preference.setSummary("");
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference(PREF_LOCATION_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventWifiDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary("");
            }
            else
                preference.setSummary("");
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference(PREF_WIFI_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary("");
            }
            else
                preference.setSummary("");
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference(PREF_BLUETOOTH_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary("");
            }
            else
                preference.setSummary("");
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference(PREF_MOBILE_CELLS_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary("");
            }
            else
                preference.setSummary("");
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference(PREF_ORIENTATION_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary("");
            }
            else
                preference.setSummary("");
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference(PREF_NOTIFICATION_SCANNING_CATEGORY_ROOT);
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }

        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_LIST_BACKGROUND_CUSTOM_COLOR)) {
            return;
        }

        ////////////////////

        /*if (key.equals(PREF_GRANT_ROOT_PERMISSION)) {
            if (PPApplication.isRooted()) {
                String summary;
                if (PPApplication.isRootGranted(true))
                    summary = getString(R.string.permission_granted);
                else
                    summary = getString(R.string.permission_not_granted);
                preference.setSummary(summary);
            }
        }*/
            /*if (key.equals(PREF_APPLICATION_PERMISSIONS)) {
                // not possible to get granted runtime permission groups :-(
            }*/
        if (key.equals(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS)) {
            String summary;
            if (Settings.System.canWrite(context))
                summary = getString(R.string.permission_granted);
            else {
                summary = getString(R.string.permission_not_granted);
                //summary = summary + StringConstant.STR_SEPARATOR_LINE + getString(R.string.phone_profiles_pref_writeSystemSettingPermissions_summary);
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_NOTIFICATION_POLICY_ACCESS_PERMISSIONS)) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            boolean granted = false;
            if (mNotificationManager != null)
                granted = mNotificationManager.isNotificationPolicyAccessGranted();
            String summary;
            if (granted)
                summary = getString(R.string.permission_granted);
            else {
                summary = getString(R.string.permission_not_granted);
                //summary = summary + StringConstant.STR_SEPARATOR_LINE + getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions_summary);
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_DRAW_OVERLAYS_PERMISSIONS)) {
            String summary;
            if (Settings.canDrawOverlays(context))
                summary = getString(R.string.permission_granted);
            else {
                summary = getString(R.string.permission_not_granted);
                //summary = summary + StringConstant.STR_SEPARATOR_LINE + getString(R.string.phone_profiles_pref_drawOverlaysPermissions_summary);
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_GRANT_G1_PERMISSION)) {
            String summary;
            if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS))
                summary = getString(R.string.permission_granted);
            else
                summary = getString(R.string.permission_not_granted);
            summary = summary + StringConstants.STR_SEPARATOR_LINE + getString(R.string.important_info_profile_grant) + " " +
                    getString(R.string.profile_preferences_types_G1_show_info);
            preference.setSummary(summary);
        }
            /*if (key.equals(PREF_APPLICATION_PERMISSIONS)) {
                String summary = getString(R.string.permission_granted);


                summary = summary + StringConstant.STR_SEPARATOR_LINE + getString(R.string.phone_profiles_pref_applicationPermissions_summary);
                preference.setSummary(summary);
            }*/
        //}
        if (key.equals(PREF_LOCATION_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventLocationSystemSettings_summary);
            if (!GlobalUtils.isLocationEnabled(context)) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + StringConstants.STR_SEPARATOR_WITH_DOT +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + StringConstants.STR_SEPARATOR_WITH_DOT +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_WIFI_LOCATION_SYSTEM_SETTINGS)) {
            String summary;
            if (Build.VERSION.SDK_INT < 29)
                summary = context.getString(R.string.phone_profiles_pref_eventWiFiLocationSystemSettings_summary);
            else
                summary = context.getString(R.string.phone_profiles_pref_eventWiFiLocationSystemSettings_summary_api29);
            if (!GlobalUtils.isLocationEnabled(context)) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + StringConstants.STR_SEPARATOR_WITH_DOT +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + StringConstants.STR_SEPARATOR_WITH_DOT +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventBluetoothLocationSystemSettings_summary);
            if (!GlobalUtils.isLocationEnabled(context)) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + StringConstants.STR_SEPARATOR_WITH_DOT +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + StringConstants.STR_SEPARATOR_WITH_DOT +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS)) {
            String summary;
            if (Build.VERSION.SDK_INT < 28)
                summary = getString(R.string.phone_profiles_pref_eventMobileCellsLocationSystemSettingsNotA9_summary);
            else
                summary = getString(R.string.phone_profiles_pref_eventMobileCellsLocationSystemSettings_summary);
            if (!GlobalUtils.isLocationEnabled(context)) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + StringConstants.STR_SEPARATOR_WITH_DOT +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + StringConstants.STR_SEPARATOR_WITH_DOT +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS)) {
            if (Build.VERSION.SDK_INT < 27) {
                String summary = getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_summary);
                if (GlobalUtils.isWifiSleepPolicySetToNever(context)) {
                    summary = getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_setToAlways_summary) + StringConstants.STR_SEPARATOR_WITH_DOT +
                            summary;
                } else {
                    summary = getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_notSetToAlways_summary) + StringConstants.STR_SEPARATOR_WITH_DOT +
                            summary;
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary);
            if (!PPNotificationListenerService.isNotificationListenerServiceEnabled(context, true)) {
                summary = "* " + getString(R.string.phone_profiles_pref_applicationEventScanningNotificationAccessSettingsDisabled_summary) + "! *"+StringConstants.STR_SEPARATOR_LINE +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningNotificationAccessSettingsEnabled_summary) + StringConstants.STR_SEPARATOR_WITH_DOT +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_GRANT_ROOT_PERMISSION)) {
            String summary;
            boolean rooted = RootUtils.isRooted(/*true*/);
            if (rooted) {
                summary = getString(R.string.phone_profiles_pref_grantRootPermission_summary);
                if (ApplicationPreferences.applicationNeverAskForGrantRoot)
                    summary = getString(R.string.phone_profiles_pref_grantRootPermission_neverAsk_set_summary) + StringConstants.STR_SEPARATOR_LINE +
                            summary;
                else
                    summary = getString(R.string.phone_profiles_pref_grantRootPermission_neverAsk_notSet_summary_2) + StringConstants.STR_SEPARATOR_LINE +
                            summary;
                summary = getString(R.string.phone_profiles_pref_device_is_rooted) + StringConstants.STR_SEPARATOR_LINE + summary;
            }
            else
                summary = getString(R.string.phone_profiles_pref_device_is_not_rooted);
            preference.setSummary(summary);
        }
        if (key.equals(PREF_GRANT_SHIZUKU_PERMISSION)) {
            String summary;
            if (ShizukuUtils.shizukuAvailable()) {
                if (ShizukuUtils.hasShizukuPermission())
                    summary = getString(R.string.permission_granted);
                else
                    summary = getString(R.string.permission_not_granted);
                summary = summary + StringConstants.STR_SEPARATOR_LINE + getString(R.string.phone_profiles_pref_grantShizukuPermission_summary2) + " " +
                        getString(R.string.profile_preferences_types_shizuku_show_info2);
            }
            else {
                summary = getString(R.string.phone_profiles_pref_shizuku_is_not_running);
                summary = summary + StringConstants.STR_SEPARATOR_LINE + getString(R.string.phone_profiles_pref_grantShizukuPermission_summary1) + " " +
                        getString(R.string.profile_preferences_types_shizuku_show_info1);
            }
            preference.setSummary(summary);
        }

        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR) ||
                //key.equals(PREF_NOTIFICATION_PROFILE_ICON_COLOR_INFO1) ||
                key.equals(PREF_NOTIFICATION_PROFILE_ICON_COLOR_INFO2)) {
            PPListPreference listPreference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR);
            if (listPreference != null) {
                String stringValue = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR,
                        ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR_DEFAULT_VALUE);

                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                // added support for "%" in list items
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                if (summary != null) {
                    String sSummary = summary.toString();
                    sSummary = sSummary.replace("%", "%%");

                    /*
                    Preference infoPref = findPreference(PREF_NOTIFICATION_PROFILE_ICON_COLOR_INFO1);
                    if (infoPref != null)
                        infoPref.setSummary(sSummary);
                    */
                    Preference infoPref = findPreference(PREF_NOTIFICATION_PROFILE_ICON_COLOR_INFO2);
                    if (infoPref != null)
                        infoPref.setSummary(sSummary + StringConstants.STR_SEPARATOR_LINE + getString(R.string.phone_profiles_pref_notificationProfileIconColor_info_summary));
                } else {
                    /*
                    Preference infoPref = findPreference(PREF_NOTIFICATION_PROFILE_ICON_COLOR_INFO1);
                    if (infoPref != null)
                        infoPref.setSummary(null);
                    */
                    Preference infoPref = findPreference(PREF_NOTIFICATION_PROFILE_ICON_COLOR_INFO2);
                    if (infoPref != null)
                        infoPref.setSummary(null);
                }
            }
        }
        if (key.equals(PREF_SET_CALL_SCREENING_ROLE_SETTINGS)) {
            if (Build.VERSION.SDK_INT >= 29) {
                String summary = getString(R.string.phone_profiles_pref_call_screening_setCallScreeningRole_summary);
                Preference _preference = prefMng.findPreference(key);
                if (_preference != null) {
                    RoleManager roleManager = (RoleManager) context.getSystemService(ROLE_SERVICE);
                    boolean isHeld = (roleManager != null) && roleManager.isRoleHeld(ROLE_CALL_SCREENING);
                    if (isHeld) {
                        summary = getString(R.string.phone_profiles_pref_call_screening_setCallScreeningRole_summary_ststus_1) +
                                StringConstants.STR_SEPARATOR_LINE + summary;
                    } else {
                        summary = getString(R.string.phone_profiles_pref_call_screening_setCallScreeningRole_summary_ststus_0) +
                                StringConstants.STR_SEPARATOR_LINE + summary;
                    }
                    _preference.setSummary(summary);
                }
            }
        }

        // Do not bind toggles.
        if (preference instanceof CheckBoxPreference || preference instanceof SwitchPreferenceCompat) {
            return;
        }

        // Do not bind toggles.
        if (preference instanceof TimeDialogPreference) {
            return;
        }

        String stringValue = preferences.getString(key, "");

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE)) {
            long lProfileId;
            try {
                lProfileId = Long.parseLong(stringValue);
            } catch (Exception e) {
                lProfileId = 0;
            }
            ProfilePreference profilePreference = (ProfilePreference) preference;
            profilePreference.setSummary(lProfileId);

        } else if (preference instanceof PPListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            PPListPreference listPreference = (PPListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            // added support for "%" in list items
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            if (summary != null) {
                String sSummary = summary.toString();
                sSummary = sSummary.replace("%", "%%");
                preference.setSummary(sSummary);
            } else
                preference.setSummary(null);

            //if (key.equals(PPApplication.PREF_APPLICATION_LANGUAGE))
            //    setTitleStyle(preference, true, false);
        } else
        //noinspection StatementWithEmptyBody
        if (preference instanceof RingtonePreference) {
            // keep summary from preference
        }
        else
        //noinspection StatementWithEmptyBody
        if (preference instanceof ColorChooserPreference) {
            // keep summary from preference
        }
        else
        //noinspection StatementWithEmptyBody
        if (preference instanceof RestartEventsIconColorChooserPreference) {
            // keep summary from preference
        }
        else {
            if (!stringValue.isEmpty()) {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                //preference.setSummary(preference.toString());
                preference.setSummary(stringValue);
            }
        }
    }

    private void setCategorySummary(Preference preferenceCategory) {
        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        PhoneProfilesPrefsActivity activity = (PhoneProfilesPrefsActivity) getActivity();

        String key = preferenceCategory.getKey();

        //boolean addEnd = true;

        StringBuilder _value = new StringBuilder();
        if (key.equals(PREF_APPLICATION_INTERFACE_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_applicationTheme));
            //_value.append(StringConstants.STR_BULLET);
            //_value.append(getString(R.string.phone_profiles_pref_applicationWidgetLauncher));
            //_value.append(StringConstants.STR_BULLET);
            //_value.append(getString(R.string.phone_profiles_pref_notificationLauncher));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationRestartEventsIconColor));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_createEditorShortcut));
        }
        if (key.equals(PREF_APPLICATION_START_CATEGORY_ROOT)) {
            //_value.append(getString(R.string.phone_profiles_pref_applicationStartOnBoot));
            //_value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationActivate));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationStartEvents));
        }
        if (key.equals(PREF_SYSTEM_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationForceSetMergeRingNotificationVolumes));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationBatteryOptimization));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationPowerSaveMode));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationBatterySaver));
        }
        if (key.equals(PREF_PERMISSIONS_CATEGORY_ROOT)) {
            if (RootUtils.isRooted(/*true*/)) {
                _value.append(getString(R.string.phone_profiles_pref_grantRootPermission));
            }
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_writeSystemSettingPermissions));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_drawOverlaysPermissions));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationPermissions));
        }
        if (key.equals(PREF_APP_NOTIFICATION_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_notificationSystemSettings));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_notificationLauncher));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_notificationStatusBarStyle));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_notificationNotificationStyle));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_notificationShowProfileIcon));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_notificationLayoutType));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_notificationPrefIndicator));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_notificationBackgroundColor));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_notificationTextColor));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_notificationUseDecoration));
        }
        if (key.equals(PREF_PROFILE_ACTIVATION_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_applicationEventBackgroundProfile));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationAlert));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_notificationsToast));
        }
        if (key.equals(PREF_SPECIAL_PROFILE_PARAMETERS_CATEGORY_ROOT)) {
            if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
                _value.append(getString(R.string.phone_profiles_pref_applicationForceSetBrightnessAtScreenOn));
            }
        }
        if (key.equals(PREF_EVENT_RUN_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_eventRunUsePriority));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationRestartEventsAlert));
        }
        if (key.equals(PREF_PERIODIC_SCANNING_CATEGORY_ROOT)) {
            ApplicationPreferences.applicationEventPeriodicScanningEnableScanning(context);
            ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventPeriodicScanningScanInterval(context);
            _value.append(getString(R.string.phone_profiles_pref_applicationEventBackgroundScanningEnableScanning)).append(StringConstants.STR_COLON_WITH_SPACE);
            if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) {
                _value.append(StringConstants.TAG_BOLD_START_HTML)
                        .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, activity))
                        .append(StringConstants.TAG_BOLD_END_HTML);

                _value.append(StringConstants.TAG_SEPARATOR_BREAK_HTML);
                _value.append(getString(R.string.phone_profiles_pref_applicationEventBackgroundScanningScanInterval)).append(StringConstants.STR_COLON_WITH_SPACE)
                        .append(StringConstants.TAG_BOLD_START_HTML)
                        .append(getColorForChangedPreferenceValue(String.valueOf(ApplicationPreferences.applicationEventPeriodicScanningScanInterval), prefMng, key, activity))
                        .append(StringConstants.TAG_BOLD_END_HTML);
                _value.append(StringConstants.TAG_SEPARATOR_BREAK_HTML);
                _value.append(getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn));
            } else {
                if (!ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile)
                    _value.append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                else
                    _value.append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
            }
        }
        if (key.equals(PREF_LOCATION_SCANNING_CATEGORY_ROOT)) {
            ApplicationPreferences.applicationEventLocationEnableScanning(context);
            ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventLocationUpdateInterval(context);
            _value.append(getString(R.string.phone_profiles_pref_applicationEventLocationEnableScanning)).append(StringConstants.STR_COLON_WITH_SPACE);
            if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                _value.append(StringConstants.TAG_BOLD_START_HTML)
                        .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, activity))
                        .append(StringConstants.TAG_BOLD_END_HTML);
                if (!GlobalUtils.isLocationEnabled(context)) {
                    _value.append(StringConstants.TAG_BREAK_HTML);
                    _value.append(getString(R.string.phone_profiles_pref_eventLocationSystemSettings)).append(StringConstants.STR_COLON_WITH_SPACE)
                            .append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                } else {
                    _value.append(StringConstants.TAG_BREAK_HTML);
                    _value.append(getString(R.string.phone_profiles_pref_eventLocationSystemSettings)).append(StringConstants.STR_COLON_WITH_SPACE)
                            .append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                }

                _value.append(StringConstants.TAG_SEPARATOR_BREAK_HTML);
                _value.append(getString(R.string.phone_profiles_pref_applicationEventLocationScanInterval)).append(StringConstants.STR_COLON_WITH_SPACE)
                        .append(StringConstants.TAG_BOLD_START_HTML)
                        .append(getColorForChangedPreferenceValue(String.valueOf(ApplicationPreferences.applicationEventLocationUpdateInterval), prefMng, key, activity))
                        .append(StringConstants.TAG_BOLD_END_HTML);
                _value.append(StringConstants.TAG_SEPARATOR_BREAK_HTML);
                _value.append(getString(R.string.phone_profiles_pref_applicationEventLocationsEditor));
                _value.append(StringConstants.STR_BULLET);
                _value.append(getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn));
                _value.append(StringConstants.STR_BULLET);
                _value.append(getString(R.string.phone_profiles_pref_applicationEventLocationsUseGPS));
            }
            else {
                if (!ApplicationPreferences.applicationEventLocationDisabledScannigByProfile)
                    _value.append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                else
                    _value.append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
            }
        }
        if (key.equals(PREF_WIFI_SCANNING_CATEGORY_ROOT)) {
            PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, true, context);
            if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                _value.append(getString(R.string.profile_preferences_device_not_allowed))
                        .append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML)
                        .append(getColorForChangedPreferenceValue(preferenceAllowed.getNotAllowedPreferenceReasonString(context), prefMng, key, activity))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
            else {
                ApplicationPreferences.applicationEventWifiEnableScanning(context);
                ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(context);
                ApplicationPreferences.applicationEventWifiScanInterval(context);
                _value.append(getString(R.string.phone_profiles_pref_applicationEventWifiEnableScanning)).append(StringConstants.STR_COLON_WITH_SPACE);
                if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                    if (!GlobalUtils.isLocationEnabled(context)) {
                        _value.append(StringConstants.TAG_BREAK_HTML);
                        _value.append(getString(R.string.phone_profiles_pref_eventLocationSystemSettings)).append(StringConstants.STR_COLON_WITH_SPACE)
                                .append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    } else {
                        _value.append(StringConstants.TAG_BREAK_HTML);
                        _value.append(getString(R.string.phone_profiles_pref_eventLocationSystemSettings)).append(StringConstants.STR_COLON_WITH_SPACE)
                                .append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }
                    if (Build.VERSION.SDK_INT < 27) {
                        if (GlobalUtils.isWifiSleepPolicySetToNever(context)) {
                            _value.append(StringConstants.TAG_BREAK_HTML);
                            _value.append(getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings)).append(StringConstants.STR_COLON_WITH_SPACE)
                                    .append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_setToAlways_summary), prefMng, key, activity))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        } else {
                            _value.append(StringConstants.TAG_BREAK_HTML);
                            _value.append(getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings)).append(StringConstants.STR_COLON_WITH_SPACE)
                                    .append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_notSetToAlways_summary), prefMng, key, activity))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        }
                    }

                    _value.append(StringConstants.TAG_SEPARATOR_BREAK_HTML);
                    _value.append(getString(R.string.phone_profiles_pref_applicationEventWifiScanInterval)).append(StringConstants.STR_COLON_WITH_SPACE)
                            .append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(String.valueOf(ApplicationPreferences.applicationEventWifiScanInterval), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                    _value.append(StringConstants.TAG_SEPARATOR_BREAK_HTML);
                    _value.append(getString(R.string.phone_profiles_pref_applicationEventWifiScanIfWifiOff));
                    _value.append(StringConstants.STR_BULLET);
                    _value.append(getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn));
                } else {
                    if (!ApplicationPreferences.applicationEventWifiDisabledScannigByProfile)
                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    else
                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                }
            }
        }
        if (key.equals(PREF_BLUETOOTH_SCANNING_CATEGORY_ROOT)) {
            PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, true, context);
            if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                _value.append(getString(R.string.profile_preferences_device_not_allowed))
                        .append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML)
                        .append(getColorForChangedPreferenceValue(preferenceAllowed.getNotAllowedPreferenceReasonString(context), prefMng, key, activity))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
            else {
                ApplicationPreferences.applicationEventBluetoothEnableScanning(context);
                ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(context);
                ApplicationPreferences.applicationEventBluetoothScanInterval(context);
                _value.append(getString(R.string.phone_profiles_pref_applicationEventBluetoothEnableScanning)).append(StringConstants.STR_COLON_WITH_SPACE);
                if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                    if (!GlobalUtils.isLocationEnabled(context)) {
                        _value.append(StringConstants.TAG_BREAK_HTML);
                        _value.append(getString(R.string.phone_profiles_pref_eventLocationSystemSettings)).append(StringConstants.STR_COLON_WITH_SPACE)
                                .append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    } else {
                        _value.append(StringConstants.TAG_BREAK_HTML);
                        _value.append(getString(R.string.phone_profiles_pref_eventLocationSystemSettings)).append(StringConstants.STR_COLON_WITH_SPACE)
                                .append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }

                    _value.append(StringConstants.TAG_SEPARATOR_BREAK_HTML);
                    _value.append(getString(R.string.phone_profiles_pref_applicationEventBluetoothScanInterval)).append(StringConstants.STR_COLON_WITH_SPACE)
                            .append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(String.valueOf(ApplicationPreferences.applicationEventBluetoothScanInterval), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                    _value.append(StringConstants.TAG_SEPARATOR_BREAK_HTML);
                    _value.append(getString(R.string.phone_profiles_pref_applicationEventBluetoothScanIfBluetoothOff));
                    _value.append(StringConstants.STR_BULLET);
                    _value.append(getString(R.string.phone_profiles_pref_applicationEventBluetoothLEScanDuration));
                    _value.append(StringConstants.STR_BULLET);
                    _value.append(getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn));
                } else {
                    if (!ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile)
                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    else
                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                }
            }
        }
        if (key.equals(PREF_MOBILE_CELLS_SCANNING_CATEGORY_ROOT)) {
            PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, true, context);
            if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                _value.append(getString(R.string.profile_preferences_device_not_allowed))
                        .append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML)
                        .append(getColorForChangedPreferenceValue(preferenceAllowed.getNotAllowedPreferenceReasonString(context), prefMng, key, activity))
                        .append(StringConstants.TAG_BOLD_END_HTML);
                //addEnd = false;
            }
            else {
                ApplicationPreferences.applicationEventMobileCellEnableScanning(context);
                ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(context);
                _value.append(getString(R.string.phone_profiles_pref_applicationEventMobileCellEnableScanning)).append(StringConstants.STR_COLON_WITH_SPACE);
                if (ApplicationPreferences.applicationEventMobileCellEnableScanning) {
//                    PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesPrefsFragment.setCategorySummary", "******** ### *******");
                    _value.append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                    if (!GlobalUtils.isLocationEnabled(context)) {
                        _value.append(StringConstants.TAG_BREAK_HTML);
                        _value.append(getString(R.string.phone_profiles_pref_eventLocationSystemSettings)).append(StringConstants.STR_COLON_WITH_SPACE)
                                .append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    } else {
                        _value.append(StringConstants.TAG_BREAK_HTML);
                        _value.append(getString(R.string.phone_profiles_pref_eventLocationSystemSettings)).append(StringConstants.STR_COLON_WITH_SPACE)
                                .append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }
                    _value.append(StringConstants.TAG_SEPARATOR_BREAK_HTML);
                    _value.append(getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn));
                } else {
                    if (!ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile)
                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    else
                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                }
            }
        }
        if (key.equals(PREF_ORIENTATION_SCANNING_CATEGORY_ROOT)) {
            PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, true, context);
            if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                _value.append(getString(R.string.profile_preferences_device_not_allowed))
                        .append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML)
                        .append(getColorForChangedPreferenceValue(preferenceAllowed.getNotAllowedPreferenceReasonString(context), prefMng, key, activity))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
            else {
                ApplicationPreferences.applicationEventOrientationEnableScanning(context);
                ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(context);
                ApplicationPreferences.applicationEventOrientationScanInterval(context);
                _value.append(getString(R.string.phone_profiles_pref_applicationEventOrientationEnableScanning)).append(StringConstants.STR_COLON_WITH_SPACE);
                if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
//                    PPApplicationStatic.logE("[TEST BATTERY] PhoneProfilesPrefsFragment.setCategorySummary", "******** ### *******");
                    _value.append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                    _value.append(StringConstants.TAG_SEPARATOR_BREAK_HTML);
                    _value.append(getString(R.string.phone_profiles_pref_applicationEventOrientationScanInterval)).append(StringConstants.STR_COLON_WITH_SPACE)
                            .append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(String.valueOf(ApplicationPreferences.applicationEventOrientationScanInterval), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                    _value.append(StringConstants.TAG_SEPARATOR_BREAK_HTML);
                    _value.append(getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn));
                }
                else {
                    if (!ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile)
                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    else
                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                }
            }
        }
        if (key.equals(PREF_NOTIFICATION_SCANNING_CATEGORY_ROOT)) {
            PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, true, context);
            if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                _value.append(getString(R.string.profile_preferences_device_not_allowed))
                        .append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML)
                        .append(getColorForChangedPreferenceValue(preferenceAllowed.getNotAllowedPreferenceReasonString(context), prefMng, key, activity))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
            else {
                ApplicationPreferences.applicationEventNotificationEnableScanning(context);
                ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile(context);
                _value.append(getString(R.string.phone_profiles_pref_applicationEventNotificationEnableScanning)).append(StringConstants.STR_COLON_WITH_SPACE);
                if (ApplicationPreferences.applicationEventNotificationEnableScanning) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML)
                            .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, activity))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                    if (!PPNotificationListenerService.isNotificationListenerServiceEnabled(context, true)) {
                        _value.append(StringConstants.TAG_BREAK_HTML);
                        _value.append(getString(R.string.phone_profiles_pref_eventNotificationAccessSystemSettings)).append(StringConstants.STR_COLON_WITH_SPACE)
                                .append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue("* " + getString(R.string.phone_profiles_pref_applicationEventScanningNotificationAccessSettingsDisabled_summary) + "! *", prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    } else {
                        _value.append(StringConstants.TAG_BREAK_HTML);
                        _value.append(getString(R.string.phone_profiles_pref_eventNotificationAccessSystemSettings)).append(StringConstants.STR_COLON_WITH_SPACE)
                                .append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningNotificationAccessSettingsEnabled_summary), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }
                    _value.append(StringConstants.TAG_SEPARATOR_BREAK_HTML);
                    _value.append(getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn));
                } else {
                    if (!ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile)
                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    else
                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, activity))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                }
            }
        }
        if (key.equals(PREF_ACTIVATOR_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_applicationPrefIndicator));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationClose));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationGridLayout));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationActivatorNumColumns));
        }
        if (key.equals(PREF_EDITOR_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_applicationPrefIndicator));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationHideEventDetails));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationHideHeaderOrBottomBar));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_deleteOldActivityLogs));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_createEditorShortcut));
        }
        if (key.equals(PREF_WIDGET_LIST_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLauncher));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationPrefIndicator));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationHeader));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationGridLayout));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetBackground));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLightnessB));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetColorB));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetShowBorder));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetCornerRadius));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLightnessT));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconColor));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLightnessI));
        }
        if (key.equals(PREF_WIDGET_ONE_ROW_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLauncher));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationPrefIndicator));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetBackground));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLightnessB));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetColorB));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetShowBorder));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetCornerRadius));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLightnessT));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconColor));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLightnessI));
        }
        if (key.equals(PREF_WIDGET_ICON_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLauncher));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconBackgroundType));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconBackground));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconLightnessB));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconColorB));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconShowBorder));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconCornerRadius));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconHideProfileName));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconLightnessT));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconShowProfileEndOfActivation));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconColor));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLightnessI));
        }
        if (key.equals(PREF_WIDGET_DASH_CLOCK_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLauncher));
        }
        if (key.equals(PREF_WIDGET_ONE_ROW_PROFILE_LIST_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetOneRowProfileListNumberOfProfilesPerPage));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetBackground));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLightnessB));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetColorB));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetShowBorder));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetCornerRadius));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconColor));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLightnessI));
        }
        if (key.equals(PREF_SHORTCUT_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconColor));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLightnessI));
        }
        if (key.equals(PREF_WIDGET_PANEL_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetPanelVerticalPosition));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationHeader));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetBackground));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLightnessB));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetColorB));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLightnessT));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetIconColor));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetLightnessI));
        }
        if (key.equals(PREF_PROFILE_LIST_NOTIFICATIONLIST_CATEGORY_ROOT)) {
            _value.append(getString(R.string.phone_profiles_pref_notificationProfileListDisplayNotification));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetOneRowProfileListNumberOfProfilesPerPage));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_notificationStatusBarStyle));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_notificationBackgroundColor));
            _value.append(StringConstants.STR_BULLET);
            _value.append(getString(R.string.phone_profiles_pref_applicationWidgetOneRowProfileLisArrowsMarkLightness));
        }
        /*
        if (key.equals(PREF_CALL_CONTROL_CATEGORY_ROOT)) {
            if (Build.VERSION.SDK_INT >= 29) {
                String summary; //= getString(R.string.phone_profiles_pref_call_screening_setCallScreeningRole_summary);
                RoleManager roleManager = (RoleManager) context.getSystemService(ROLE_SERVICE);
                boolean isHeld = (roleManager != null) && roleManager.isRoleHeld(ROLE_CALL_SCREENING);
                if (isHeld) {
                    summary = getString(R.string.phone_profiles_pref_call_screening_setCallScreeningRole_summary_ststus_1);// +
                            //StringConstants.STR_DOUBLE_NEWLINE + summary;
                } else {
                    summary = getString(R.string.phone_profiles_pref_call_screening_setCallScreeningRole_summary_ststus_0);//' +
                            //StringConstants.STR_DOUBLE_NEWLINE + summary;
                }
                _value.append(summary);
            }
        }
        */

        /*if (addEnd) {
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + "…";
        }*/

        preferenceCategory.setSummary(StringFormatUtils.fromHtml(_value.toString(), false,  false, 0, 0, true));
    }

    static String getColorForChangedPreferenceValue(String preferenceValue,
                                                    PreferenceManager prefMng,
                                                    String preferenceKey,
                                                    Context context) {
        Preference preference = prefMng.findPreference(preferenceKey);
        if ((preference != null) && preference.isEnabled()) {
            int labelColor = ContextCompat.getColor(context, R.color.preferenceSummaryValueColor);
            String colorString = String.format(StringConstants.STR_FORMAT_INT, labelColor).substring(2); // !!strip alpha value!!
            return String.format(StringConstants.TAG_FONT_COLOR_HTML/*+":"*/, colorString, preferenceValue);
        } else
            return preferenceValue;
    }

    void doMobileCellsRegistrationCountDownBroadcastReceiver(long millisUntilFinished) {
        MobileCellsRegistrationDialogPreference preference = prefMng.findPreference(PREF_EVENT_MOBILE_CELLS_REGISTRATION);
        if (preference != null) {
            //Log.d("mobileCellsRegistrationCountDownBroadcastReceiver", "xxx");
            preference.updateInterface(millisUntilFinished, false);
            preference.setSummaryDDP(millisUntilFinished);
        }
    }

    void doMobileCellsRegistrationStoppedBroadcastReceiver() {
        MobileCellsEditorPreference preference = prefMng.findPreference(PREF_APPLICATION_EVENT_MOBILE_CELL_CONFIGURE_CELLS);
        if (preference != null)
            preference.refreshListView(true, true/*, Integer.MAX_VALUE*/);
    }

}
