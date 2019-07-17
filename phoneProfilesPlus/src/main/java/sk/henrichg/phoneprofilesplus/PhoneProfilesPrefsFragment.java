package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import com.evernote.android.job.JobRequest;
//import com.thelittlefireman.appkillermanager.managers.KillerManager;

import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

class PhoneProfilesPrefsFragment extends PreferenceFragmentCompat
                        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceManager prefMng;
    private SharedPreferences preferences;
    private SharedPreferences applicationPreferences;

    //boolean scrollToSet = false;
    private boolean nestedFragment = false;

    private static final String PREF_APPLICATION_PERMISSIONS = "permissionsApplicationPermissions";
    private static final int RESULT_APPLICATION_PERMISSIONS = 1990;
    private static final String PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS = "permissionsWriteSystemSettingsPermissions";
    private static final int RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS = 1991;
    private static final String PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = "permissionsAccessNotificationPolicyPermissions";
    private static final int RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = 1997;
    private static final String PREF_DRAW_OVERLAYS_PERMISSIONS = "permissionsDrawOverlaysPermissions";
    private static final int RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS = 1998;
    private static final String PREF_GRANT_ROOT_PERMISSION = "permissionsGrantRootPermission";

    private static final String PREF_WIFI_LOCATION_SYSTEM_SETTINGS = "applicationEventWiFiLocationSystemSettings";
    private static final String PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS = "applicationEventBluetoothLocationSystemSettings";
    private static final String PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS = "applicationEventMobileCellsLocationSystemSettings";
    private static final int RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS = 1992;
    private static final String PREF_POWER_SAVE_MODE_SETTINGS = "applicationPowerSaveMode";
    private static final int RESULT_POWER_SAVE_MODE_SETTINGS = 1993;
    //static final String PREF_POWER_SAVE_MODE_INTERNAL = "applicationPowerSaveModeInternal";
    private static final String PREF_LOCATION_SYSTEM_SETTINGS = "applicationEventLocationSystemSettings";
    private static final int RESULT_LOCATION_SYSTEM_SETTINGS = 1994;
    private static final String PREF_LOCATION_EDITOR = "applicationEventLocationsEditor";
    private static final String PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS = "applicationBatteryOptimization";
    //private static final String PREF_APPLICATION_LANGUAGE_24 = "applicationLanguage24";
    //static final int RESULT_LOCALE_SETTINGS = 1996;
    //private static final String PREF_AUTOSTART_MANAGER = "applicationAutoStartManager";
    private static final String PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS = "applicationEventWiFiKeepOnSystemSettings";
    private static final int RESULT_WIFI_KEEP_ON_SETTINGS = 1999;
    private static final String PREF_NOTIFICATION_SYSTEM_SETTINGS = "notificationSystemSettings";
    //private static final String PREF_APPLICATION_POWER_MANAGER = "applicationPowerManager";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        PPApplication.logE("PhoneProfilesPrefsFragment.onCreate", "xxx");

        // is required for to not call onCreate and onDestroy on orientation change
        setRetainInstance(true);

        nestedFragment = !(this instanceof PhoneProfilesPrefsActivity.PhoneProfilesPrefsRoot);
        PPApplication.logE("PhoneProfilesPrefsFragment.onCreate", "nestedFragment="+nestedFragment);

        initPreferenceFragment(savedInstanceState);
        //prefMng = getPreferenceManager();
        //preferences = prefMng.getSharedPreferences();

        updateAllSummary();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //initPreferenceFragment();
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference)
    {
        PPApplication.logE("PhoneProfilesPrefsFragment.onDisplayPreferenceDialog", "xxx");

        PreferenceDialogFragmentCompat dialogFragment = null;

        if (preference instanceof DurationDialogPreferenceX)
        {
            ((DurationDialogPreferenceX)preference).fragment = new DurationDialogPreferenceFragmentX();
            dialogFragment = ((DurationDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ProfilePreferenceX)
        {
            ((ProfilePreferenceX)preference).fragment = new ProfilePreferenceFragmentX();
            dialogFragment = ((ProfilePreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof RingtonePreferenceX)
        {
            ((RingtonePreferenceX)preference).fragment = new RingtonePreferenceFragmentX();
            dialogFragment = ((RingtonePreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof LocationGeofencePreferenceX)
        {
            ((LocationGeofencePreferenceX)preference).fragment = new LocationGeofencePreferenceFragmentX();
            dialogFragment = ((LocationGeofencePreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof BetterNumberPickerPreferenceX)
        {
            ((BetterNumberPickerPreferenceX)preference).fragment = new BetterNumberPickerPreferenceFragmentX();
            dialogFragment = ((BetterNumberPickerPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ColorChooserPreferenceX)
        {
            ((ColorChooserPreferenceX)preference).fragment = new ColorChooserPreferenceFragmentX();
            dialogFragment = ((ColorChooserPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null)
        {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(fragmentManager, PPApplication.PACKAGE_NAME + ".PhoneProfilesPrefsActivity.DIALOG");
            }
        }
        else
        {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PPApplication.logE("PhoneProfilesPrefsFragment.onActivityCreated", "xxx");

        if (getActivity() == null)
            return;

        // must be used handler for rewrite toolbar title/subtitle
        final PhoneProfilesPrefsFragment fragment = this;
        Handler handler = new Handler(getActivity().getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null)
                    return;

                Toolbar toolbar = getActivity().findViewById(R.id.activity_preferences_toolbar);
                if (nestedFragment) {
                    toolbar.setTitle(fragment.getPreferenceScreen().getTitle());
                    toolbar.setSubtitle(getString(R.string.title_activity_phone_profiles_preferences));
                }
                else {
                    toolbar.setTitle(getString(R.string.title_activity_phone_profiles_preferences));
                    toolbar.setSubtitle(null);
                }

            }
        }, 200);

        /*
        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);
        */

        if (!nestedFragment) {
            Preference preferenceCategoryScreen;
            preferenceCategoryScreen = findPreference("applicationInterfaceCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("categoryApplicationStartRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("categorySystemRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("categoryPermissionsRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("categoryNotificationsRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("profileActivationCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("eventRunCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("locationScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("wifiScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("bluetoothScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("mobileCellsScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("orientationScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("categoryActivatorRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("categoryEditorRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("categoryWidgetListRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("categoryWidgetOneRowRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = findPreference("categoryWidgetIconRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
                preferenceCategoryScreen = findPreference("categorySamsungEdgePanelRoot");
                if (preferenceCategoryScreen != null)
                    setCategorySummary(preferenceCategoryScreen, "");
            }
        }

        //if (!ActivateProfileHelper.getMergedRingNotificationVolumes(getActivity().getApplicationContext())) {
        if (!ApplicationPreferences.preferences.getBoolean(ActivateProfileHelper.PREF_MERGED_RING_NOTIFICATION_VOLUMES, true)) {
            // detection of volumes merge = volumes are not merged
            //Log.e("PhoneProfilesPrefsFragment.onActivityCreated","volumes are merged=false");
            Preference preference = findPreference("applicationUnlinkRingerNotificationVolumesInfo");
            if (preference != null) {
                //preference.setEnabled(false);
                preference.setTitle(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumesUnlinked_summary);
                //systemCategory.removePreference(preference);
            }
        }
        else {
            Preference preference = findPreference("applicationUnlinkRingerNotificationVolumesInfo");
            if (preference != null) {
                //preference.setEnabled(true);
                preference.setSummary(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes_summary);
            }
            //Log.e("PhoneProfilesPrefsFragment.onActivityCreated","volumes are merged=true");
            /*Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_RINGER_NOTIFICATION_VOLUMES_UNLINKED_INFO);
            if (preference != null)
                systemCategory.removePreference(preference);*/
        }

        Preference preference = findPreference(PREF_POWER_SAVE_MODE_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @SuppressWarnings("ConstantConditions")
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean activityExists;
                    Intent intent;
                    if (Build.VERSION.SDK_INT == 21) {
                        intent = new Intent();
                        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$BatterySaverSettingsActivity"));
                        activityExists = GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext());
                    } else {
                        activityExists = GlobalGUIRoutines.activityActionExists(Settings.ACTION_BATTERY_SAVER_SETTINGS, getActivity().getApplicationContext());
                        intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                    }
                    if (activityExists) {
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        try {
                            startActivityForResult(intent, RESULT_POWER_SAVE_MODE_SETTINGS);
                        } catch (Exception e) {
                            if (Build.VERSION.SDK_INT > 21) {
                                intent = new Intent();
                                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$BatterySaverSettingsActivity"));
                                activityExists = GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext());
                                if (activityExists) {
                                    try {
                                        startActivityForResult(intent, RESULT_POWER_SAVE_MODE_SETTINGS);
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    }
                    if (!activityExists) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });*/
                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                    return false;
                }
            });
        }
        if (Build.VERSION.SDK_INT >= 23) {
            preference = findPreference(PREF_APPLICATION_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Permissions.saveAllPermissions(getActivity().getApplicationContext(), false);
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:sk.henrichg.phoneprofilesplus"));
                        if (GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext())) {
                            startActivityForResult(intent, RESULT_APPLICATION_PERMISSIONS);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });*/
                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                        return false;
                    }
                });
            }
            preference = findPreference(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                /*if (PPApplication.romIsMIUI) {
                    preference.setSummary(R.string.phone_profiles_pref_writeSystemSettingPermissions_summary_miui);
                }*/
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //if (!PPApplication.romIsMIUI) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_WRITE_SETTINGS, getActivity().getApplicationContext())) {
                            @SuppressLint("InlinedApi")
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse("package:sk.henrichg.phoneprofilesplus"));
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                        if (positive != null) positive.setAllCaps(false);
                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                        if (negative != null) negative.setAllCaps(false);
                                    }
                                });*/
                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                        /*}
                        else {
                            try {
                                // MIUI 8
                                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                                localIntent.putExtra("extra_pkgname", getActivity().getPackageName());
                                startActivityForResult(localIntent, RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                            } catch (Exception e) {
                                try {
                                    // MIUI 5/6/7
                                    Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                    localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                    localIntent.putExtra("extra_pkgname", getActivity().getPackageName());
                                    startActivityForResult(localIntent, RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                                } catch (Exception e1) {
                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                    dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = dialogBuilder.create();
                                    //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    //    @Override
                                    //    public void onShow(DialogInterface dialog) {
                                    //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    //        if (positive != null) positive.setAllCaps(false);
                                    //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    //        if (negative != null) negative.setAllCaps(false);
                                    //    }
                                    //});
                                    dialog.show();
                                }
                            }
                        }*/
                        return false;
                    }
                });
            }
            preference = findPreference(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
            if (preference != null) {
                boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                if ((!a60) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getActivity().getApplicationContext())) {
                    //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            @SuppressLint("InlinedApi")
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
                            return false;
                        }
                    });
                } else {
                    PreferenceScreen preferenceCategory = findPreference("categoryPermissions");
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            preference = findPreference(PREF_DRAW_OVERLAYS_PERMISSIONS);
            if (preference != null) {
                //if (android.os.Build.VERSION.SDK_INT >= 25) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    /*if (PPApplication.romIsMIUI) {
                        preference.setTitle(R.string.phone_profiles_pref_drawOverlaysPermissions_miui);
                        preference.setSummary(R.string.phone_profiles_pref_drawOverlaysPermissions_summary_miui);
                    }*/
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //if (!PPApplication.romIsMIUI) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, getActivity().getApplicationContext())) {
                            @SuppressLint("InlinedApi")
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse("package:sk.henrichg.phoneprofilesplus"));
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS);
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                                    /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialog) {
                                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                            if (positive != null) positive.setAllCaps(false);
                                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                            if (negative != null) negative.setAllCaps(false);
                                        }
                                    });*/
                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                            /*}
                            else {
                                try {
                                    // MIUI 8
                                    Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                    localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                                    localIntent.putExtra("extra_pkgname", getActivity().getPackageName());
                                    startActivityForResult(localIntent, RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS);
                                } catch (Exception e) {
                                    try {
                                        // MIUI 5/6/7
                                        Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                        localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                        localIntent.putExtra("extra_pkgname", getActivity().getPackageName());
                                        startActivityForResult(localIntent, RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS);
                                    } catch (Exception e1) {
                                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                        AlertDialog dialog = dialogBuilder.create();
                                        //*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                        //    @Override
                                        //    public void onShow(DialogInterface dialog) {
                                        //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                        //        if (positive != null) positive.setAllCaps(false);
                                        //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                        //        if (negative != null) negative.setAllCaps(false);
                                        //    }
                                        //});
                                        dialog.show();
                                    }
                                }
                            }*/
                        return false;
                    }
                });
            }

            //int locationMode = Settings.Secure.getInt(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);

            /*
            if (WifiScanJob.wifi == null)
                WifiScanJob.wifi = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            boolean isScanAlwaysAvailable = WifiScanJob.wifi.isScanAlwaysAvailable();

            PPApplication.logE("PhoneProfilesPrefsFragment.onActivityCreated", "locationMode="+locationMode);
            PPApplication.logE("PhoneProfilesPrefsFragment.onActivityCreated", "isScanAlwaysAvailable="+isScanAlwaysAvailable);

            if ((locationMode == Settings.Secure.LOCATION_MODE_OFF) || (!isScanAlwaysAvailable)) {*/
            preference = findPreference(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                        if (positive != null) positive.setAllCaps(false);
                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                        if (negative != null) negative.setAllCaps(false);
                                    }
                                });*/
                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                        return false;
                    }
                });
            }

            preference = findPreference(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_WIFI_IP_SETTINGS, getActivity().getApplicationContext())) {
                            Intent intent = new Intent(Settings.ACTION_WIFI_IP_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WIFI_KEEP_ON_SETTINGS);
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });*/
                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                        return false;
                    }
                });
            }

            //if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
            preference = findPreference(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                        if (positive != null) positive.setAllCaps(false);
                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                        if (negative != null) negative.setAllCaps(false);
                                    }
                                });*/
                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                        return false;
                    }
                });
            }

            //if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
            preference = findPreference(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                                /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                        if (positive != null) positive.setAllCaps(false);
                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                        if (negative != null) negative.setAllCaps(false);
                                    }
                                });*/
                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                        return false;
                    }
                });
            }

            preference = findPreference(PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, getActivity().getApplicationContext())) {
                            @SuppressLint("InlinedApi")
                            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivity(intent);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });*/
                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                        return false;
                    }
                });
            }

            if (!PPApplication.isRooted(true)) {
                PreferenceScreen preferenceCategory = findPreference("categoryPermissions");
                preference = findPreference(PREF_GRANT_ROOT_PERMISSION);
                if ((preferenceCategory != null) && (preference != null))
                    preferenceCategory.removePreference(preference);
            }
        }
        else {
            if (PPApplication.isRooted(true)) {
                PreferenceScreen preferenceCategory = findPreference("categoryPermissions");
                if (preferenceCategory != null) {
                    preference = findPreference(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                    if (preference != null)
                        preferenceCategory.removePreference(preference);
                    preference = findPreference(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
                    if (preference != null)
                        preferenceCategory.removePreference(preference);
                    preference = findPreference(PREF_DRAW_OVERLAYS_PERMISSIONS);
                    if (preference != null)
                        preferenceCategory.removePreference(preference);
                    preference = findPreference(PREF_APPLICATION_PERMISSIONS);
                    if (preference != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                PreferenceScreen preferenceScreen = findPreference("rootScreen");
                Preference preferenceCategory = findPreference("categoryPermissionsRoot");
                if ((preferenceScreen != null) && (preferenceCategory != null))
                        preferenceScreen.removePreference(preferenceCategory);
            }

            PreferenceScreen preferenceCategory = findPreference("wifiScanningCategory");
            preference = findPreference(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
            if ((preferenceCategory != null) && (preference != null))
                preferenceCategory.removePreference(preference);

            preferenceCategory = findPreference("bluetoothScanningCategory");
            preference = findPreference(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
            if ((preferenceCategory != null) && (preference != null))
                preferenceCategory.removePreference(preference);

            PreferenceCategory preferenceCategory2 = findPreference("applicationPowerParametersCategory");
            preference = findPreference(PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
            if ((preferenceCategory2 != null) && (preference != null))
                preferenceCategory2.removePreference(preference);
        }

        if (PPApplication.isRooted(true)) {
            preference = findPreference(PREF_GRANT_ROOT_PERMISSION);
            if (preference != null) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Permissions.grantRootX(null, getActivity());
                        return false;
                    }
                });
            }
        }

        if (!WifiBluetoothScanner.bluetoothLESupported(getActivity().getApplicationContext())) {
            PreferenceScreen preferenceCategory = findPreference("bluetoothScanningCategory");
            preference = findPreference(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION);
            if ((preferenceCategory != null) && (preference != null))
                preferenceCategory.removePreference(preference);
        }
        preference = findPreference(PREF_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @SuppressWarnings("ConstantConditions")
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivityForResult(intent, RESULT_LOCATION_SYSTEM_SETTINGS);
                    }
                    else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
                        /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                if (positive != null) positive.setAllCaps(false);
                                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                if (negative != null) negative.setAllCaps(false);
                            }
                        });*/
                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                    return false;
                }
            });
        }
        /*
        if (android.os.Build.VERSION.SDK_INT < 21) {
            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("categoryNotificationsStatusBar");
            preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN);
            if (preference != null)
                preferenceCategory.removePreference(preference);
        }
        */
        if ((PPApplication.sLook == null) || (!PPApplication.sLookCocktailPanelEnabled)) {
            PreferenceScreen preferenceScreen = findPreference("rootScreen");
            Preference preferenceCategory = findPreference("categorySamsungEdgePanelRoot");
            if ((preferenceScreen != null) && (preferenceCategory != null))
                preferenceScreen.removePreference(preferenceCategory);
        }
        /*preference = findPreference(PREF_AUTOSTART_MANAGER);
        if (preference != null) {
            if (KillerManager.isActionAvailable(getActivity(), KillerManager.Actions.ACTION_AUTOSTART)) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            KillerManager.doActionAutoStart(getActivity());
                        }catch (Exception e) {
                            PPApplication.logE("PhoneProfilesPrefsFragment.onActivityCreated", Log.getStackTraceString(e));
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            //    @Override
                            //    public void onShow(DialogInterface dialog) {
                            //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                            //        if (positive != null) positive.setAllCaps(false);
                            //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                            //        if (negative != null) negative.setAllCaps(false);
                            //    }
                            //});
                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                        return false;
                    }
                });
            } else {
                PreferenceScreen preferenceCategory = findPreference("categoryApplicationStart");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }*/
        long jobMinInterval = TimeUnit.MILLISECONDS.toMinutes(JobRequest.MIN_INTERVAL);
        String summary = getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary1) + " " +
                jobMinInterval + " " +
                getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary2);
        preference = findPreference("applicationEventLocationUpdateIntervalInfo");
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference("applicationEventWifiScanIntervalInfo");
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference("applicationEventBluetoothScanIntervalInfo");
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference("applicationEventOrientationScanIntervalInfo");
        if (preference != null) {
            summary = getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary1) + " 10 " +
                    getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary3);
            preference.setSummary(summary);
        }
        Preference _preference;
        preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false)) {
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false)) {
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false)) {
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        preference = findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false)) {
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (Build.VERSION.SDK_INT >= 27) {
            preference = findPreference(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("wifiScanningCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
        if (Build.VERSION.SDK_INT >= 26) {
            preference = findPreference(PREF_NOTIFICATION_SYSTEM_SETTINGS);
            if (preference != null) {
                preference.setSummary(getString(R.string.phone_profiles_pref_notificationSystemSettings_summary) +
                        " " + getString(R.string.notification_channel_activated_profile));
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @TargetApi(Build.VERSION_CODES.O)
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_CHANNEL_ID, PPApplication.PROFILE_NOTIFICATION_CHANNEL);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().getPackageName());
                        if (GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext())) {
                            startActivity(intent);
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });*/
                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                        return false;
                    }
                });
            }
        }
        //preference = findPreference(PREF_APPLICATION_POWER_MANAGER);
        //if (preference != null) {
            /*boolean intentFound = false;
            KillerManager.init(getActivity());
            DeviceBase device = KillerManager.getDevice();
            if (device != null) {
                if (PPApplication.logEnabled()) {
                    PPApplication.logE("PhoneProfilesPrefsFragment.onActivityCreated", "device="+device.toString());
                    PPApplication.logE("PhoneProfilesPrefsFragment.onActivityCreated", "device="+device.getDeviceManufacturer());
                    String debugInfo = device.getExtraDebugInformations(getActivity());
                    if (debugInfo != null)
                        PPApplication.logE("PhoneProfilesPrefsFragment.onActivityCreated", debugInfo);
                    else
                        PPApplication.logE("PhoneProfilesPrefsFragment.onActivityCreated", "no extra debug info");
                }
                Intent intent = device.getActionPowerSaving(getActivity());
                PPApplication.logE("PhoneProfilesPrefsFragment.onActivityCreated", "intent="+intent);
                if (intent != null && ActionsUtils.isIntentAvailable(getActivity(), intent))
                    intentFound = true;
                //if (intent != null && GlobalGUIRoutines.activityIntentExists(intent, getActivity()))
                //    intentFound = true;
            }
            PPApplication.logE("PhoneProfilesPrefsFragment.onActivityCreated", "intentFound="+intentFound);*/
            /*
            if (KillerManager.isActionAvailable(getActivity(), KillerManager.Actions.ACTION_POWERSAVING)) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            KillerManager.doActionPowerSaving(getActivity());
                        }catch (Exception e) {
                            if (getActivity() != null) {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = dialogBuilder.create();
                                //dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                //    @Override
                                //    public void onShow(DialogInterface dialog) {
                                //        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                //        if (positive != null) positive.setAllCaps(false);
                                //        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                //        if (negative != null) negative.setAllCaps(false);
                                //    }
                                //});
                                if (!getActivity().isFinishing())
                                    dialog.show();
                            }
                        }
                        return false;
                    }
                });
            } else {
                PreferenceCategory preferenceCategory = findPreference("applicationPowerParametersCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            */
        //}
        if (Build.VERSION.SDK_INT < 24) {
            PreferenceCategory preferenceCategory = findPreference("categoryNotificationsStatusBar");
            preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE);
            if ((preferenceCategory != null) && (preference != null))
                preferenceCategory.removePreference(preference);
            preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION);
            if ((preferenceCategory != null) && (preference != null))
                preferenceCategory.removePreference(preference);
            preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
            if ((preferenceCategory != null) && (preference != null))
                preferenceCategory.removePreference(preference);
        }
        preference = findPreference("applicationUnlinkRingerNotificationVolumesImportantInfo");
        if (preference != null) {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intentLaunch = new Intent(getActivity(), ImportantInfoActivity.class);
                    intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, 0);
                    intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SCROLL_TO, R.id.activity_info_notification_how_does_volume_separation_work_title);
                    startActivity(intentLaunch);
                    return false;
                }
            });
        }

        preference = findPreference("applicationDoNotKillMyApp");
        if (preference != null) {
            preference.setSummary(getString(R.string.phone_profiles_pref_applicationDoNotKillMyApp_summary1) + " " +
                    getString(R.string.phone_profiles_pref_applicationDoNotKillMyApp_webSiteName) + " " +
                    getString(R.string.phone_profiles_pref_applicationDoNotKillMyApp_summary2));
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    PPApplication.showDoNotKillMyAppDialog(PhoneProfilesPrefsFragment.this);
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

            SharedPreferences.Editor editor = applicationPreferences.edit();
            updateSharedPreferences(editor, preferences);
            editor.apply();

            PPApplication.logE("PhoneProfilesPrefsFragment.onDestroy", "xxx");

        } catch (Exception ignored) {}

        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PPApplication.logE("PhoneProfilesPrefsFragment.onSharedPreferenceChanged", "xxx");
        setSummary(key);
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_LANGUAGE)) {
            if (getActivity() != null) {
                GlobalGUIRoutines.setLanguage(getActivity());
                GlobalGUIRoutines.reloadActivity(getActivity(), true);
            }
        }
    }

    void doOnActivityResult(int requestCode, int resultCode/*, Intent data*/)
    {
        PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "xxx");
        PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "requestCode="+requestCode);

        if ((requestCode == RESULT_APPLICATION_PERMISSIONS) ||
                (requestCode == RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) ||
                (requestCode == RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) ||
                (requestCode == RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Activity activity = getActivity();
                if (activity != null) {
                    Context context = activity.getApplicationContext();

                    boolean finishActivity = false;
                    boolean permissionsChanged = Permissions.getPermissionsChanged(context);

                    if (requestCode == RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) {
                        boolean canWrite = Settings.System.canWrite(context);
                        permissionsChanged = Permissions.getWriteSystemSettingsPermission(context) != canWrite;
                        PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "canWrite=" + permissionsChanged);
                        if (canWrite)
                            Permissions.setShowRequestWriteSettingsPermission(context, true);
                    }
                    if (requestCode == RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        boolean notificationPolicyGranted = (mNotificationManager != null) && (mNotificationManager.isNotificationPolicyAccessGranted());
                        permissionsChanged = Permissions.getNotificationPolicyPermission(context) != notificationPolicyGranted;
                        PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "notificationPolicyGranted=" + permissionsChanged);
                        if (notificationPolicyGranted)
                            Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                    }
                    if (requestCode == RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS) {
                        boolean canDrawOverlays = Settings.canDrawOverlays(context);
                        permissionsChanged = Permissions.getDrawOverlayPermission(context) != canDrawOverlays;
                        PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "canDrawOverlays=" + permissionsChanged);
                        if (canDrawOverlays)
                            Permissions.setShowRequestDrawOverlaysPermission(context, true);
                    }
                    if (requestCode == RESULT_APPLICATION_PERMISSIONS) {
                        boolean calendarPermission = Permissions.checkCalendar(context);
                        permissionsChanged = Permissions.getCalendarPermission(context) != calendarPermission;
                        PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "calendarPermission=" + permissionsChanged);
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!calendarPermission);
                        if (!permissionsChanged) {
                            boolean contactsPermission = Permissions.checkContacts(context);
                            permissionsChanged = Permissions.getContactsPermission(context) != contactsPermission;
                            PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "contactsPermission=" + permissionsChanged);
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!contactsPermission);
                        }
                        if (!permissionsChanged) {
                            boolean locationPermission = Permissions.checkLocation(context);
                            permissionsChanged = Permissions.getLocationPermission(context) != locationPermission;
                            PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "locationPermission=" + permissionsChanged);
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!locationPermission);
                        }
                        if (!permissionsChanged) {
                            boolean smsPermission = Permissions.checkSMS(context);
                            permissionsChanged = Permissions.getSMSPermission(context) != smsPermission;
                            PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "smsPermission=" + permissionsChanged);
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!smsPermission);
                        }
                        if (!permissionsChanged) {
                            boolean phonePermission = Permissions.checkPhone(context);
                            PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "phonePermission=" + phonePermission);
                            permissionsChanged = Permissions.getPhonePermission(context) != phonePermission;
                            PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "permissionsChanged=" + permissionsChanged);
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!phonePermission);
                        }
                        if (!permissionsChanged) {
                            boolean storagePermission = Permissions.checkStorage(context);
                            permissionsChanged = Permissions.getStoragePermission(context) != storagePermission;
                            PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "storagePermission=" + permissionsChanged);
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!storagePermission);
                        }
                        if (!permissionsChanged) {
                            boolean cameraPermission = Permissions.checkCamera(context);
                            permissionsChanged = Permissions.getCameraPermission(context) != cameraPermission;
                            PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "cameraPermission=" + permissionsChanged);
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!cameraPermission);
                        }
                        if (!permissionsChanged) {
                            boolean microphonePermission = Permissions.checkMicrophone(context);
                            permissionsChanged = Permissions.getMicrophonePermission(context) != microphonePermission;
                            PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "microphonePermission=" + permissionsChanged);
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!microphonePermission);
                        }
                        if (!permissionsChanged) {
                            boolean sensorsPermission = Permissions.checkSensors(context);
                            permissionsChanged = Permissions.getSensorsPermission(context) != sensorsPermission;
                            PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "sensorsPermission=" + permissionsChanged);
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!sensorsPermission);
                        }
                    }

                    Permissions.saveAllPermissions(context, permissionsChanged);
                    PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "permissionsChanged=" + permissionsChanged);

                    if (permissionsChanged) {
                        //DataWrapper dataWrapper = new DataWrapper(context, false, 0);

                        //Profile activatedProfile = dataWrapper.getActivatedProfile(true, true);
                        //dataWrapper.refreshProfileIcon(activatedProfile);
                        PPApplication.showProfileNotification(/*context*/true);
                        PPApplication.logE("ActivateProfileHelper.updateGUI", "from PhoneProfilesPrefsFragment.doOnActivityResult");
                        ActivateProfileHelper.updateGUI(context, !finishActivity, true);

                        if (finishActivity) {
                            activity.setResult(Activity.RESULT_CANCELED);
                            activity.finishAffinity();
                        } else {
                            setSummary(PREF_APPLICATION_PERMISSIONS);
                            setSummary(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                            setSummary(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
                            setSummary(PREF_DRAW_OVERLAYS_PERMISSIONS);

                            activity.setResult(Activity.RESULT_OK);
                        }
                    } else
                        activity.setResult(Activity.RESULT_CANCELED);
                }
            }
        }

        if (requestCode == RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS) {
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING);
            setSummary(PREF_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
        }
        if (requestCode == RESULT_LOCATION_SYSTEM_SETTINGS) {
            final boolean enabled = PhoneProfilesService.isLocationEnabled(getContext());
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

        if (requestCode == LocationGeofencePreferenceX.RESULT_GEOFENCE_EDITOR) {
            PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "RESULT_GEOFENCE_EDITOR");
            if (resultCode == Activity.RESULT_OK) {
                PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "RESULT_OK");
                LocationGeofencePreferenceX preference = prefMng.findPreference(PREF_LOCATION_EDITOR);
                PPApplication.logE("PhoneProfilesPrefsFragment.doOnActivityResult", "preference="+preference);
                if (preference != null) {
                    preference.setGeofenceFromEditor(/*geofenceId*/);
                }
            }
            /*if (PhoneProfilesPreferencesFragment.changedLocationGeofencePreference != null) {
                if(resultCode == Activity.RESULT_OK){
                    //long geofenceId = data.getLongExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, 0);
                    // this persistGeofence, for multiselect this mus only refresh listView in preference
                    PhoneProfilesPreferencesFragment.changedLocationGeofencePreference.setGeofenceFromEditor();
                    PhoneProfilesPreferencesFragment.changedLocationGeofencePreference = null;
                }
            }*/
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_RINGTONE_PREFERENCE) {
            RingtonePreferenceX preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_SOUND);
            if (preference != null)
                preference.refreshListView();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode);
    }


    private void initPreferenceFragment(Bundle savedInstanceState) {
        prefMng = getPreferenceManager();

        //prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        //prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        if (getContext() != null) {
            applicationPreferences = getContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
        }

        PPApplication.logE("PhoneProfilesPrefsFragment.initPreferenceFragment", "getContext()="+getContext());

        if (savedInstanceState == null) {
            SharedPreferences.Editor editor = preferences.edit();
            updateSharedPreferences(editor, applicationPreferences);
            editor.apply();
        }

        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
    }

    void updateSharedPreferences() {
        SharedPreferences.Editor editor = applicationPreferences.edit();
        updateSharedPreferences(editor, preferences);
        editor.apply();
    }

    private void updateAllSummary()
    {
        if (getActivity() == null)
            return;

        setSummary(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ALERT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_CLOSE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION);
        setSummary(ApplicationPreferences.PREF_APPLICATION_HOME_LAUNCHER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_LANGUAGE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_THEME);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_NIGHT_MODE_OFF_THEME);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_HEADER);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_HEADER);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_TOAST);
        if (Build.VERSION.SDK_INT < 26)
            setSummary(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE);

        if (Build.VERSION.SDK_INT < 26) {
            setSummary(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
            Preference preference = prefMng.findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
            if (preference != null) {
                preference.setTitle(R.string.phone_profiles_pref_notificationShowInStatusBarAndLockscreen);
            }
            //}
        }

        if (Build.VERSION.SDK_INT < 26)
            setSummary(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR);
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
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME);
        setSummary(ApplicationPreferences.PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T);
        setSummary(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES);
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            //setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_PREF_INDICATOR);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_HEADER);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS);
            //setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_GRID_LAYOUT);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS);
        }
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR);
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
        setSummary(PREF_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_GRANT_ROOT_PERMISSION);
        setSummary(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
        setSummary(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
        setSummary(PREF_DRAW_OVERLAYS_PERMISSIONS);
        setSummary(PREF_APPLICATION_PERMISSIONS);
        //setSummary(PREF_AUTOSTART_MANAGER);
        setSummary(PREF_NOTIFICATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_USAGE);
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
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS);

        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, getActivity().getApplicationContext());
        if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            /*prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_RESCAN).setEnabled(false);*/
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, false);
            editor.apply();
            Preference preference = prefMng.findPreference("wifiScanningCategory");
            if (preference != null)
                preference.setEnabled(false);
        }

        preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, getActivity().getApplicationContext());
        if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
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
            Preference preference = prefMng.findPreference("bluetoothScanningCategory");
            if (preference != null)
                preference.setEnabled(false);
        }

        preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, getActivity().getApplicationContext());
        if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            Preference preference = prefMng.findPreference("orientationScanningCategory");
            if (preference != null)
                preference.setEnabled(false);
        }

        preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, getActivity().getApplicationContext());
        if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {

            Preference preference = prefMng.findPreference("mobileCellsScanningCategory");
            if (preference != null)
                preference.setEnabled(false);
        }

        if (!PhoneProfilesService.isLocationEnabled(getActivity().getApplicationContext())) {
            Preference preference = prefMng.findPreference(PREF_LOCATION_EDITOR);
            if (preference != null)
                preference.setEnabled(false);
        }

    }

    private void setSummary(String key) {

        Preference preference = prefMng.findPreference(key);

        if (preference == null)
            return;

        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        if (Build.VERSION.SDK_INT < 26) {
            boolean notificationStatusBar = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true);
            boolean notificationStatusBarPermanent = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
            PreferenceScreen preferenceCategoryNotifications = findPreference("categoryNotificationsRoot");
            if (!(notificationStatusBar && notificationStatusBarPermanent)) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceCategoryNotifications, true, true, false, false, true, false);
                if (preferenceCategoryNotifications != null) {
                    String summary = getString(R.string.phone_profiles_pref_notificationStatusBarNotEnabled_summary) + " " +
                            getString(R.string.phone_profiles_pref_notificationStatusBarRequired) + "\n\n";
                    setCategorySummary(preferenceCategoryNotifications, summary);
                }
            } else {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceCategoryNotifications, true, false, false, false, false, false);
                if (preferenceCategoryNotifications != null) {
                    String summary = "";
                    setCategorySummary(preferenceCategoryNotifications, summary);
                }
            }
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR)) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !notificationStatusBar, false, false, !notificationStatusBar, false);
            }
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT)) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !notificationStatusBarPermanent, false, false, !notificationStatusBarPermanent, false);
            }
        }

        if (/*(android.os.Build.VERSION.SDK_INT >= 21) &&*/ (android.os.Build.VERSION.SDK_INT < 26)) {
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR)) {
                boolean show = preferences.getBoolean(key, true);
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN);
                if (_preference != null)
                    _preference.setEnabled(show);
            }
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE)) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false)) {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER);
            if (_preference != null) {
                _preference.setEnabled(preferences.getBoolean(key, false));
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER);
            if (_preference != null) {
                _preference.setEnabled(preferences.getBoolean(key, false));
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER);
            if (_preference != null) {
                _preference.setEnabled(preferences.getBoolean(key, false));
            }
        }

        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR)) {
            String backgroundColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
            Preference _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR);
            if (_preference != null)
                _preference.setEnabled(backgroundColor.equals("0"));
            _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION);
            if (_preference != null)
                _preference.setEnabled(backgroundColor.equals("0"));
            boolean useDecoration = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, true);
            _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
            if (_preference != null)
                _preference.setEnabled(useDecoration && backgroundColor.equals("0"));
        }
        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION)) {
            boolean useDecoration = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, true);
            String backgroundColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
            Preference _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
            if (_preference != null)
                _preference.setEnabled(useDecoration && backgroundColor.equals("0"));
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(context))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("locationScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(context))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("wifiScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(context))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("bluetoothScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(context))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("mobileCellsScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(context))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("orientationScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }

        // Do not bind toggles.
        if (preference instanceof CheckBoxPreference || preference instanceof SwitchPreferenceCompat) {
            return;
        }

        String stringValue = preferences.getString(key, "");

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE)) {
            long lProfileId;
            try {
                lProfileId = Long.parseLong(stringValue);
            } catch (Exception e) {
                lProfileId = 0;
            }
            ProfilePreferenceX profilePreference = (ProfilePreferenceX) preference;
            profilePreference.setSummary(lProfileId);

            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_SOUND);
            if (_preference != null)
                _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_VIBRATE);
            if (_preference != null)
                _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_USAGE);
            if (_preference != null)
                _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);
        } else if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
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
            if (preference instanceof RingtonePreferenceX) {
                // keep summary from preference
            } else {
                if (!stringValue.isEmpty()) {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    //preference.setSummary(preference.toString());
                    preference.setSummary(stringValue);
                }
            }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
            if (_preference != null) {
                boolean enabled;
                String value = preferences.getString(key, "0");
                if (!value.equals("0"))
                    enabled = value.equals("1");
                else
                    enabled = ApplicationPreferences.preferences.getBoolean(ActivateProfileHelper.PREF_MERGED_RING_NOTIFICATION_VOLUMES, true);
                //Log.d("PhoneProfilesPrefsFragment.setSummary","enabled="+enabled);
                _preference.setEnabled(enabled);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS);
            if (_preference != null) {
                boolean colorful = preferences.getString(key, "0").equals("1");
                _preference.setEnabled(colorful);
            }
        }
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
        if (Build.VERSION.SDK_INT >= 23) {
            /*if (key.equals(PREF_APPLICATION_PERMISSIONS)) {
                // not possible to get granted runtime permission groups :-(
            }*/
            if (key.equals(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS)) {
                String summary;
                if (Settings.System.canWrite(context))
                    summary = getString(R.string.permission_granted);
                else {
                    summary = getString(R.string.permission_not_granted);
                    //summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_writeSystemSettingPermissions_summary);
                }
                preference.setSummary(summary);
            }
            if (key.equals(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS)) {
                String summary;
                if (Permissions.checkAccessNotificationPolicy(context))
                    summary = getString(R.string.permission_granted);
                else {
                    summary = getString(R.string.permission_not_granted);
                    summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions_summary);
                }
                preference.setSummary(summary);
            }
            if (key.equals(PREF_DRAW_OVERLAYS_PERMISSIONS)) {
                String summary;
                if (Settings.canDrawOverlays(context))
                    summary = getString(R.string.permission_granted);
                else {
                    summary = getString(R.string.permission_not_granted);
                    //summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_drawOverlaysPermissions_summary);
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_LOCATION_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventLocationSystemSettings_summary);
            if (!PhoneProfilesService.isLocationEnabled(context)) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_WIFI_LOCATION_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventWiFiLocationSystemSettings_summary);
            if (!PhoneProfilesService.isLocationEnabled(context)) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventBluetoothLocationSystemSettings_summary);
            if (!PhoneProfilesService.isLocationEnabled(context)) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n" +
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
            if (!PhoneProfilesService.isLocationEnabled(context)) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_summary);
            if (PhoneProfilesService.isWifiSleepPolicySetToNever(context)) {
                summary = getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_setToAlways_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_notSetToAlways_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
    }

    private void setCategorySummary(Preference preferenceCategory, String summary) {
        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();


        String key = preferenceCategory.getKey();

        //boolean addEnd = true;

        if (key.equals("applicationInterfaceCategoryRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationLanguage);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationTheme);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHomeLauncher);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLauncher);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationLauncher);
        }
        if (key.equals("categoryApplicationStartRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationStartOnBoot);
            /*if (KillerManager.isActionAvailable(context, KillerManager.Actions.ACTION_AUTOSTART)) {
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + getString(R.string.phone_profiles_pref_systemAutoStartManager);
            }*/
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationActivate);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationStartEvents);
        }
        if (key.equals("categorySystemRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationForceSetMergeRingNotificationVolumes);
            if (Build.VERSION.SDK_INT >= 23) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationBatteryOptimization);
            }
            /*if (KillerManager.isActionAvailable(context, KillerManager.Actions.ACTION_POWERSAVING)) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationPowerManager);
            }*/
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationPowerSaveMode);
            //if (Build.VERSION.SDK_INT >= 21) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationBatterySaver);
            //}
        }
        if (key.equals("categoryPermissionsRoot")) {
            if (PPApplication.isRooted(true)) {
                summary = summary + getString(R.string.phone_profiles_pref_grantRootPermission);
            }
            if (Build.VERSION.SDK_INT >= 23) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_writeSystemSettingPermissions);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_drawOverlaysPermissions);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationPermissions);
            }
        }
        if (key.equals("categoryNotificationsRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_notificationsToast);
            if (Build.VERSION.SDK_INT >= 26) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationSystemSettings);
            }
            else {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationStatusBar);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationStatusBarPermanent);
            }
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationLayoutType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationStatusBarStyle);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationBackgroundColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationTextColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationUseDecoration);
        }
        if (key.equals("profileActivationCategoryRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventBackgroundProfile);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_backgroundProfileUsage);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationAlert);
        }
        if (key.equals("eventRunCategoryRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_eventRunUsePriority);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationRestartEventsAlert);
        }
        if (key.equals("locationScanningCategoryRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventLocationEnableScanning) + ": ";
            if (ApplicationPreferences.applicationEventLocationEnableScanning(context)) {
                summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                if (!PhoneProfilesService.isLocationEnabled(context)) {
                    summary = summary + "\n";
                    summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                            getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                }
                else {
                    summary = summary + "\n";
                    summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                            getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
                }
            }
            else {
                if (!ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(context))
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_disabled);
                else
                    summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
            }
            summary = summary + "\n\n";
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventLocationsEditor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventLocationScanInterval);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventLocationsUseGPS);
        }
        if (key.equals("wifiScanningCategoryRoot")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context);
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context);
            }
            else {
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventWifiEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventWifiEnableScanning(context)) {
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                    if (!PhoneProfilesService.isLocationEnabled(context)) {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                    } else {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
                    }
                    if (Build.VERSION.SDK_INT < 27) {
                        if (PhoneProfilesService.isWifiSleepPolicySetToNever(context)) {
                            summary = summary + "\n";
                            summary = summary + getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings) + ": " +
                                    getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_setToAlways_summary);
                        } else {
                            summary = summary + "\n";
                            summary = summary + getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings) + ": " +
                                    getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_notSetToAlways_summary);
                        }
                    }
                } else {
                    if (!ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(context))
                        summary = summary + getString(R.string.array_pref_applicationDisableScanning_disabled);
                    else
                        summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                }
                summary = summary + "\n\n";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventWifiScanIfWifiOff);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventWifiScanInterval);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
            }
        }
        if (key.equals("bluetoothScanningCategoryRoot")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context);
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context);
            }
            else {
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventBluetoothEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventBluetoothEnableScanning(context)) {
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                    if (!PhoneProfilesService.isLocationEnabled(context)) {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                    } else {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
                    }
                } else {
                    if (!ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(context))
                        summary = summary + getString(R.string.array_pref_applicationDisableScanning_disabled);
                    else
                        summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                }
                summary = summary + "\n\n";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventBluetoothScanIfBluetoothOff);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventBluetoothScanInterval);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventBluetoothLEScanDuration);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
            }
        }
        if (key.equals("mobileCellsScanningCategoryRoot")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, context);
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context);
                //addEnd = false;
            }
            else {
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventMobileCellEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventMobileCellEnableScanning(context)) {
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                    if (!PhoneProfilesService.isLocationEnabled(context)) {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                    } else {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
                    }
                } else {
                    if (!ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(context))
                        summary = summary + getString(R.string.array_pref_applicationDisableScanning_disabled);
                    else
                        summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                }
                summary = summary + "\n\n";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
            }
        }
        if (key.equals("orientationScanningCategoryRoot")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, context);
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context);
            }
            else {
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventOrientationEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventOrientationEnableScanning(context))
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                else {
                    if (!ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(context))
                        summary = summary + getString(R.string.array_pref_applicationDisableScanning_disabled);
                    else
                        summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                }
                summary = summary + "\n\n";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventOrientationScanInterval);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
            }
        }
        if (key.equals("categoryActivatorRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationClose);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationGridLayout);
        }
        if (key.equals("categoryEditorRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
            //if (!summary.isEmpty()) summary = summary + " • ";
            //summary = summary + getString(R.string.phone_profiles_pref_applicationEditorSaveEditorState);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_deleteOldActivityLogs);
        }
        if (key.equals("categoryWidgetListRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationGridLayout);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackground);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetColorB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetShowBorder);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetRoundedCorners);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessT);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
        }
        if (key.equals("categoryWidgetOneRowRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackground);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetColorB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetShowBorder);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetRoundedCorners);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessT);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
        }
        if (key.equals("categoryWidgetIconRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconBackgroundType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconBackground);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconLightnessB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColorB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconShowBorder);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconRoundedCorners);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconHideProfileName);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconLightnessT);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
        }
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            if (key.equals("categorySamsungEdgePanelRoot")) {
                summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackground);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessB);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetColorB);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessT);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
            }
        }

        /*if (addEnd) {
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + "…";
        }*/

        preferenceCategory.setSummary(summary);
    }

}
