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
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.evernote.android.job.JobRequest;
import com.thelittlefireman.appkillermanager.managers.KillerManager;

import java.util.concurrent.TimeUnit;

public class PhoneProfilesPreferencesNestedFragment extends PreferenceFragment
                                              implements SharedPreferences.OnSharedPreferenceChangeListener
{

    PreferenceManager prefMng;
    SharedPreferences preferences;

    static final String PREF_APPLICATION_PERMISSIONS = "permissionsApplicationPermissions";
    private static final int RESULT_APPLICATION_PERMISSIONS = 1990;
    static final String PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS = "permissionsWriteSystemSettingsPermissions";
    private static final int RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS = 1991;
    static final String PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = "permissionsAccessNotificationPolicyPermissions";
    private static final int RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = 1997;
    static final String PREF_DRAW_OVERLAYS_PERMISSIONS = "permissionsDrawOverlaysPermissions";
    private static final int RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS = 1998;
    static final String PREF_GRANT_ROOT_PERMISSION = "permissionsGrantRootPermission";

    static final String PREF_WIFI_LOCATION_SYSTEM_SETTINGS = "applicationEventWiFiLocationSystemSettings";
    static final String PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS = "applicationEventBluetoothLocationSystemSettings";
    static final String PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS = "applicationEventMobileCellsLocationSystemSettings";
    private static final int RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS = 1992;
    static final String PREF_POWER_SAVE_MODE_SETTINGS = "applicationPowerSaveMode";
    private static final int RESULT_POWER_SAVE_MODE_SETTINGS = 1993;
    //static final String PREF_POWER_SAVE_MODE_INTERNAL = "applicationPowerSaveModeInternal";
    static final String PREF_LOCATION_SYSTEM_SETTINGS = "applicationEventLocationSystemSettings";
    private static final int RESULT_LOCATION_SYSTEM_SETTINGS = 1994;
    static final String PREF_LOCATION_EDITOR = "applicationEventLocationsEditor";
    static final String PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS = "applicationBatteryOptimization";
    private static final String PREF_APPLICATION_LANGUAGE_24 = "applicationLanguage24";
    //static final int RESULT_LOCALE_SETTINGS = 1996;
    static final String PREF_AUTOSTART_MANAGER = "applicationAutoStartManager";
    static final String PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS = "applicationEventWiFiKeepOnSystemSettings";
    private static final int RESULT_WIFI_KEEP_ON_SETTINGS = 1999;
    static final String PREF_NOTIFICATION_SYSTEM_SETTINGS = "notificationSystemSettings";
    static final String PREF_APPLICATION_POWER_MANAGER = "applicationPowerManager";

    @Override
    public int addPreferencesFromResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);
        // this is really important in order to save the state across screen
        // configuration changes for example
        //setRetainInstance(true);

        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.onCreate", "xxx");

        prefMng = getPreferenceManager();
        preferences = prefMng.getSharedPreferences();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.onActivityCreated", "xxx");

        Toolbar toolbar = getActivity().findViewById(R.id.mp_toolbar);
        Bundle bundle = getArguments();
        boolean nestedFragment = bundle.getBoolean(PreferenceFragment.EXTRA_NESTED, false);
        if (nestedFragment)
            toolbar.setSubtitle(getString(R.string.title_activity_phone_profiles_preferences));
        else
            toolbar.setSubtitle(null);

        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        if (!nestedFragment) {
            PreferenceScreen preferenceCategoryScreen;
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("applicationInterfaceCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("categoryApplicationStart");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("categorySystem");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("categoryPermissions");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("categoryNotifications");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("profileActivationCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("eventRunCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("locationScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("wifiScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("bluetoothScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("mobileCellsScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("orientationScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("categoryActivator");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("categoryEditor");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("categoryWidgetList");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("categoryWidgetOneRow");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("categoryWidgetIcon");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
            if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
                preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("categorySamsungEdgePanel");
                if (preferenceCategoryScreen != null)
                    setCategorySummary(preferenceCategoryScreen, "");
            }
        }

        //PreferenceScreen systemCategory = (PreferenceScreen) findPreference("categorySystem");
        if (!ActivateProfileHelper.getMergedRingNotificationVolumes(getActivity().getApplicationContext())) {
            //Log.e("PhoneProfilesPreferencesNestedFragment.onActivityCreated","volumes are merged=false");
            Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
            if (preference != null) {
                preference.setEnabled(false);
                preference.setSummary(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumesUnlinked_summary);
                //systemCategory.removePreference(preference);
            }
        }
        else {
            Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
            if (preference != null) {
                preference.setEnabled(true);
                preference.setSummary(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes_summary);
            }
            //Log.e("PhoneProfilesPreferencesNestedFragment.onActivityCreated","volumes are merged=true");
            /*Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_RINGER_NOTIFICATION_VOLUMES_UNLINKED_INFO);
            if (preference != null)
                systemCategory.removePreference(preference);*/
        }

        /*if (Build.VERSION.SDK_INT >= 24) {
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("applicationInterfaceCategory");
            Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_LANGUAGE);
            if (preference != null)
                preferenceCategory.removePreference(preference);
            preference = findPreference(PREF_APPLICATION_LANGUAGE_24);
            if (preference != null) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCALE_SETTINGS, getActivity().getApplicationContext())) {
                            Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                            startActivityForResult(intent, RESULT_LOCALE_SETTINGS);
                        }
                        else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    if (positive != null) positive.setAllCaps(false);
                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    if (negative != null) negative.setAllCaps(false);
                                }
                            });
                            dialog.show();
                        }
                        return false;
                    }
                });
            }
        }
        else {*/
            PreferenceScreen _preferenceCategory = (PreferenceScreen) findPreference("applicationInterfaceCategory");
            Preference _preference = findPreference(PREF_APPLICATION_LANGUAGE_24);
            if (_preference != null)
                _preferenceCategory.removePreference(_preference);
        //}
        if (Build.VERSION.SDK_INT >= 21) {
            //PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categorySystem");
            //Preference preference = findPreference(PREF_POWER_SAVE_MODE_INTERNAL);
            //if (preference != null)
            //    preferenceCategory.removePreference(preference);

            Preference preference = prefMng.findPreference(PREF_POWER_SAVE_MODE_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @SuppressLint("InlinedApi")
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
        } else {
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categorySystem");
            Preference preference = findPreference(PREF_POWER_SAVE_MODE_SETTINGS);
            if (preference != null)
                preferenceCategory.removePreference(preference);
            /*preference = findPreference(PREF_POWER_SAVE_MODE_SETTINGS);
            if (preference != null)
                preferenceCategory.removePreference(preference);*/
        }
        if (Build.VERSION.SDK_INT >= 23) {
            Preference preference = prefMng.findPreference(PREF_APPLICATION_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
            preference = prefMng.findPreference(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                /*if (PPApplication.romIsMIUI) {
                    preference.setSummary(R.string.phone_profiles_pref_writeSystemSettingPermissions_summary_miui);
                }*/
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //if (!PPApplication.romIsMIUI) {
                            if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_WRITE_SETTINGS, getActivity().getApplicationContext())) {
                                @SuppressLint("InlinedApi")
                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
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
            preference = prefMng.findPreference(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
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
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
                    preferenceCategory.removePreference(preference);
                }
            }
            preference = prefMng.findPreference(PREF_DRAW_OVERLAYS_PERMISSIONS);
            if (preference != null) {
                //if (android.os.Build.VERSION.SDK_INT >= 25) {
                    //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    /*if (PPApplication.romIsMIUI) {
                        preference.setTitle(R.string.phone_profiles_pref_drawOverlaysPermissions_miui);
                        preference.setSummary(R.string.phone_profiles_pref_drawOverlaysPermissions_summary_miui);
                    }*/
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            //if (!PPApplication.romIsMIUI) {
                                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, getActivity().getApplicationContext())) {
                                    @SuppressLint("InlinedApi")
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
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
                /*} else {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
                    preferenceCategory.removePreference(preference);
                }*/
            }

            //int locationMode = Settings.Secure.getInt(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);

            /*
            if (WifiScanJob.wifi == null)
                WifiScanJob.wifi = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            boolean isScanAlwaysAvailable = WifiScanJob.wifi.isScanAlwaysAvailable();

            PPApplication.logE("PhoneProfilesPreferencesNestedFragment.onActivityCreated", "locationMode="+locationMode);
            PPApplication.logE("PhoneProfilesPreferencesNestedFragment.onActivityCreated", "isScanAlwaysAvailable="+isScanAlwaysAvailable);

            if ((locationMode == Settings.Secure.LOCATION_MODE_OFF) || (!isScanAlwaysAvailable)) {*/
                preference = prefMng.findPreference(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
                if (preference != null) {
                    //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
            /*}
            else {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("wifiScanningCategory");
                preference = findPreference(PREF_WIFI_SCANNING_SYSTEM_SETTINGS);
                if (preference != null)
                    preferenceCategory.removePreference(preference);
            }*/

            preference = prefMng.findPreference(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
                preference = prefMng.findPreference(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
                if (preference != null) {
                    //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
            /*}
            else {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("bluetoothScanningCategory");
                preference = findPreference(PREF_BLUETOOTH_SCANNING_SYSTEM_SETTINGS);
                if (preference != null)
                    preferenceCategory.removePreference(preference);
            }*/

            //if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
            preference = prefMng.findPreference(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
            /*}
            else {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("bluetoothScanningCategory");
                preference = findPreference(PREF_BLUETOOTH_SCANNING_SYSTEM_SETTINGS);
                if (preference != null)
                    preferenceCategory.removePreference(preference);
            }*/

            preference = prefMng.findPreference(PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
                preference = findPreference(PREF_GRANT_ROOT_PERMISSION);
                if ((preferenceCategory != null) && (preference != null))
                    preferenceCategory.removePreference(preference);
            }
        }
        else {
            if (PPApplication.isRooted(true)) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
                if (preferenceCategory != null) {
                    Preference preference = findPreference(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
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
                PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("rootScreen");
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryPermissions");
                if (preferenceCategory != null)
                    preferenceScreen.removePreference(preferenceCategory);
            }

            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("wifiScanningCategory");
            Preference preference = findPreference(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
            if (preference != null)
                preferenceCategory.removePreference(preference);

            preferenceCategory = (PreferenceScreen) findPreference("bluetoothScanningCategory");
            preference = findPreference(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
            if (preference != null)
                preferenceCategory.removePreference(preference);

            preferenceCategory = (PreferenceScreen) findPreference("categorySystem");
            preference = findPreference(PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
            if (preference != null)
                preferenceCategory.removePreference(preference);
        }

        if (PPApplication.isRooted(true)) {
            Preference preference = findPreference(PREF_GRANT_ROOT_PERMISSION);
            if (preference != null) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Permissions.grantRoot(null, getActivity());
                        return false;
                    }
                });
            }
        }

        if (!WifiBluetoothScanner.bluetoothLESupported(getActivity().getApplicationContext())) {
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("bluetoothScanningCategory");
            Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION);
            if (preference != null)
                preferenceCategory.removePreference(preference);
        }
        Preference preference = prefMng.findPreference(PREF_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
        if (android.os.Build.VERSION.SDK_INT < 21) {
            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("categoryNotificationsStatusBar");
            preference = prefMng.findPreference(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN);
            if (preference != null)
                preferenceCategory.removePreference(preference);
        }
        if ((PPApplication.sLook == null) || (!PPApplication.sLookCocktailPanelEnabled)) {
            PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("rootScreen");
            PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categorySamsungEdgePanel");
            if (preferenceCategory != null)
                preferenceScreen.removePreference(preferenceCategory);
        }
        preference = prefMng.findPreference(PREF_AUTOSTART_MANAGER);
        if (preference != null) {
            if (KillerManager.isActionAvailable(getActivity(), KillerManager.Actions.ACTION_AUTOSTART)) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            KillerManager.doActionAutoStart(getActivity());
                        }catch (Exception e) {
                            PPApplication.logE("PhoneProfilesPreferencesNestedFragment.onActivityCreated", Log.getStackTraceString(e));
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
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categoryApplicationStart");
                preferenceCategory.removePreference(preference);
            }
        }
        long jobMinInterval = TimeUnit.MILLISECONDS.toMinutes(JobRequest.MIN_INTERVAL);
        String summary = getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary1) + " " +
                Long.toString(jobMinInterval) + " " +
                getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary2);
        preference = prefMng.findPreference("applicationEventLocationUpdateIntervalInfo");
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = prefMng.findPreference("applicationEventWifiScanIntervalInfo");
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = prefMng.findPreference("applicationEventBluetoothScanIntervalInfo");
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = prefMng.findPreference("applicationEventOrientationScanIntervalInfo");
        if (preference != null) {
            summary = getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary1) + " 10 " +
                    getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary3);
            preference.setSummary(summary);
        }
        preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false)) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false)) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false)) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE);
        if (preference != null) {
            if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false)) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
            else {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(true);
            }
        }
        if (Build.VERSION.SDK_INT >= 27) {
            preference = prefMng.findPreference(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("wifiScanningCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        if (Build.VERSION.SDK_INT >= 26) {
            preference = prefMng.findPreference(PREF_NOTIFICATION_SYSTEM_SETTINGS);
            if (preference != null) {
                preference.setSummary(getString(R.string.phone_profiles_pref_notificationSystemSettings_summary) +
                                      " " + getString(R.string.notification_channel_activated_profile));
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
        preference = prefMng.findPreference(PREF_APPLICATION_POWER_MANAGER);
        if (preference != null) {
            /*boolean intentFound = false;
            KillerManager.init(getActivity());
            DeviceBase device = KillerManager.getDevice();
            if (device != null) {
                if (PPApplication.logEnabled()) {
                    PPApplication.logE("PhoneProfilesPreferencesNestedFragment.onActivityCreated", "device="+device.toString());
                    PPApplication.logE("PhoneProfilesPreferencesNestedFragment.onActivityCreated", "device="+device.getDeviceManufacturer());
                    String debugInfo = device.getExtraDebugInformations(getActivity());
                    if (debugInfo != null)
                        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.onActivityCreated", debugInfo);
                    else
                        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.onActivityCreated", "no extra debug info");
                }
                Intent intent = device.getActionPowerSaving(getActivity());
                PPApplication.logE("PhoneProfilesPreferencesNestedFragment.onActivityCreated", "intent="+intent);
                if (intent != null && ActionsUtils.isIntentAvailable(getActivity(), intent))
                    intentFound = true;
                //if (intent != null && GlobalGUIRoutines.activityIntentExists(intent, getActivity()))
                //    intentFound = true;
            }
            PPApplication.logE("PhoneProfilesPreferencesNestedFragment.onActivityCreated", "intentFound="+intentFound);*/

            if (KillerManager.isActionAvailable(getActivity(), KillerManager.Actions.ACTION_POWERSAVING)) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            KillerManager.doActionPowerSaving(getActivity());
                        }catch (Exception e) {
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
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("categorySystem");
                preferenceCategory.removePreference(preference);
            }
        }
        if (Build.VERSION.SDK_INT < 24) {
            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("categoryNotificationsStatusBar");
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
    }

    /*
    private void setTitleStyle(Preference preference, boolean bold, boolean underline)
    {
        CharSequence title = preference.getTitle();
        Spannable sbt = new SpannableString(title);
        Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
        for(Object span: spansToRemove){
            if(span instanceof CharacterStyle)
                sbt.removeSpan(span);
        }
        if (bold || underline)
        {
            if (bold)
                sbt.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (underline)
                sbt.setSpan(new UnderlineSpan(), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            preference.setTitle(sbt);
        }
        else
        {
            preference.setTitle(sbt);
        }
    }
    */

    void setSummary(String key) {

        Preference preference = prefMng.findPreference(key);

        if (preference == null)
            return;

        if (Build.VERSION.SDK_INT < 26) {
            boolean notificationStatusBar = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR, true);
            boolean notificationStatusBarPermanent = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
            PreferenceScreen preferenceCategoryNotifications = (PreferenceScreen) findPreference("categoryNotifications");
            if (!(notificationStatusBar && notificationStatusBarPermanent)) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preferenceCategoryNotifications, true, true, false, true, false);
                if (preferenceCategoryNotifications != null) {
                    String summary = getString(R.string.phone_profiles_pref_notificationStatusBarNotEnabled_summary) + " " +
                                        getString(R.string.phone_profiles_pref_notificationStatusBarRequired) + "\n\n";
                    setCategorySummary(preferenceCategoryNotifications, summary);
                }
            } else {
                GlobalGUIRoutines.setPreferenceTitleStyle(preferenceCategoryNotifications, true, false, false, false, false);
                if (preferenceCategoryNotifications != null) {
                    String summary = "";
                    setCategorySummary(preferenceCategoryNotifications, summary);
                }
            }
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR)) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, true, !notificationStatusBar, false, !notificationStatusBar, false);
            }
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT)) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, true, !notificationStatusBarPermanent, false, !notificationStatusBarPermanent, false);
            }
        }

        if ((android.os.Build.VERSION.SDK_INT >= 21) && (android.os.Build.VERSION.SDK_INT < 26)) {
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
                if (ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(getActivity().getApplicationContext()))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("locationScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(getActivity().getApplicationContext()))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("wifiScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(getActivity().getApplicationContext()))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("bluetoothScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(getActivity().getApplicationContext()))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("mobileCellsScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(getActivity().getApplicationContext()))
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = (PreferenceScreen) prefMng.findPreference("orientationScanningCategory");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen, "");
        }

        // Do not bind toggles.
        if (preference instanceof CheckBoxPreference || preference instanceof TwoStatePreference) {
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
            ProfilePreference profilePreference = (ProfilePreference) preference;
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
            if (preference instanceof RingtonePreference) {
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
                    enabled = ActivateProfileHelper.getMergedRingNotificationVolumes(getActivity().getApplicationContext());
                //Log.d("PhoneProfilesPreferencesNestedFragment.setSummary","enabled="+enabled);
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
                if (Settings.System.canWrite(getActivity().getApplicationContext()))
                    summary = getString(R.string.permission_granted);
                else {
                    summary = getString(R.string.permission_not_granted);
                    summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_writeSystemSettingPermissions_summary);
                }
                preference.setSummary(summary);
            }
            if (key.equals(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS)) {
                String summary;
                if (Permissions.checkAccessNotificationPolicy(getActivity().getApplicationContext()))
                    summary = getString(R.string.permission_granted);
                else {
                    summary = getString(R.string.permission_not_granted);
                    summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions_summary);
                }
                preference.setSummary(summary);
            }
            if (key.equals(PREF_DRAW_OVERLAYS_PERMISSIONS)) {
                String summary;
                if (Settings.canDrawOverlays(getActivity().getApplicationContext()))
                    summary = getString(R.string.permission_granted);
                else {
                    summary = getString(R.string.permission_not_granted);
                    summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_drawOverlaysPermissions_summary);
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_LOCATION_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventLocationSystemSettings_summary);
            if (!PhoneProfilesService.isLocationEnabled(getActivity().getApplicationContext())) {
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
            if (!PhoneProfilesService.isLocationEnabled(getActivity().getApplicationContext())) {
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
            if (!PhoneProfilesService.isLocationEnabled(getActivity().getApplicationContext())) {
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
            String summary = getString(R.string.phone_profiles_pref_eventMobileCellsLocationSystemSettings_summary);
            if (!PhoneProfilesService.isLocationEnabled(getActivity().getApplicationContext())) {
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
            if (PhoneProfilesService.isWifiSleepPolicySetToNever(getActivity().getApplicationContext())) {
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

    private void setCategorySummary(PreferenceScreen preferenceCategory, String summary) {
        String key = preferenceCategory.getKey();

        if (key.equals("applicationInterfaceCategory")) {
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
        if (key.equals("categoryApplicationStart")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationStartOnBoot);
            if (KillerManager.isActionAvailable(getActivity(), KillerManager.Actions.ACTION_AUTOSTART)) {
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + getString(R.string.phone_profiles_pref_systemAutoStartManager);
            }
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationActivate);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationStartEvents);
        }
        if (key.equals("categorySystem")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationForceSetMergeRingNotificationVolumes);
            if (Build.VERSION.SDK_INT >= 23) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationBatteryOptimization);
            }
            if (KillerManager.isActionAvailable(getActivity(), KillerManager.Actions.ACTION_POWERSAVING)) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationPowerManager);
            }
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationPowerSaveMode);
            if (Build.VERSION.SDK_INT >= 21) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationBatterySaver);
            }
        }
        if (key.equals("categoryPermissions")) {
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
        if (key.equals("categoryNotifications")) {
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
        if (key.equals("profileActivationCategory")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventBackgroundProfile);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_backgroundProfileUsage);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationAlert);
        }
        if (key.equals("eventRunCategory")) {
            summary = summary + getString(R.string.phone_profiles_pref_eventRunUsePriority);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationRestartEventsAlert);
        }
        if (key.equals("locationScanningCategory")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventLocationEnableScanning) + ": ";
            if (ApplicationPreferences.applicationEventLocationEnableScanning(getActivity())) {
                summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                if (!PhoneProfilesService.isLocationEnabled(getActivity())) {
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
                if (!ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(getActivity()))
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
        if (key.equals("wifiScanningCategory")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, getActivity().getApplicationContext());
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(getActivity());
            }
            else {
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventWifiEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventWifiEnableScanning(getActivity())) {
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                    if (!PhoneProfilesService.isLocationEnabled(getActivity())) {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                    } else {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
                    }
                    if (Build.VERSION.SDK_INT < 27) {
                        if (PhoneProfilesService.isWifiSleepPolicySetToNever(getActivity())) {
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
                    if (!ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(getActivity()))
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
        if (key.equals("bluetoothScanningCategory")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, getActivity().getApplicationContext());
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(getActivity());
            }
            else {
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventBluetoothEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventBluetoothEnableScanning(getActivity())) {
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                    if (!PhoneProfilesService.isLocationEnabled(getActivity())) {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                    } else {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
                    }
                } else {
                    if (!ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(getActivity()))
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
        if (key.equals("mobileCellsScanningCategory")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, getActivity().getApplicationContext());
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(getActivity());
            }
            else {
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventMobileCellEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventMobileCellEnableScanning(getActivity())) {
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                    if (!PhoneProfilesService.isLocationEnabled(getActivity())) {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary);
                    } else {
                        summary = summary + "\n";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary);
                    }
                } else {
                    if (!ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(getActivity()))
                        summary = summary + getString(R.string.array_pref_applicationDisableScanning_disabled);
                    else
                        summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                }
                summary = summary + "\n\n";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
            }
        }
        if (key.equals("orientationScanningCategory")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, getActivity().getApplicationContext());
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(getActivity());
            }
            else {
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventOrientationEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventOrientationEnableScanning(getActivity()))
                    summary = summary + getString(R.string.array_pref_applicationDisableScanning_enabled);
                else {
                    if (!ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(getActivity()))
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
        if (key.equals("categoryActivator")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationClose);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationGridLayout);
        }
        if (key.equals("categoryEditor")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationEditorSaveEditorState);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_deleteOldActivityLogs);
        }
        if (key.equals("categoryWidgetList")) {
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
        if (key.equals("categoryWidgetOneRow")) {
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
        if (key.equals("categoryWidgetIcon")) {
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
            if (key.equals("categorySamsungEdgePanel")) {
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

        if (!summary.isEmpty()) summary = summary +" • ";
        summary = summary + "…";

        preferenceCategory.setSummary(summary);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        setSummary(key);
    }


    @Override
    public void onDestroy()
    {
        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception ignored) {}

        super.onDestroy();
    }

    public void doOnActivityResult(int requestCode, int resultCode/*, Intent data*/)
    {
        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.doOnActivityResult", "xxx");

        if ((requestCode == PhoneProfilesPreferencesNestedFragment.RESULT_APPLICATION_PERMISSIONS) ||
                (requestCode == PhoneProfilesPreferencesNestedFragment.RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) ||
                (requestCode == PhoneProfilesPreferencesNestedFragment.RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) ||
                (requestCode == PhoneProfilesPreferencesNestedFragment.RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Context context = getActivity().getApplicationContext();

                boolean finishActivity = false;
                boolean permissionsChanged = Permissions.getPermissionsChanged(context);

                if (requestCode == PhoneProfilesPreferencesNestedFragment.RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) {
                    boolean canWrite = Settings.System.canWrite(context);
                    permissionsChanged = Permissions.getWriteSystemSettingsPermission(context) != canWrite;
                    if (canWrite)
                        Permissions.setShowRequestWriteSettingsPermission(context, true);
                }
                if (requestCode == PhoneProfilesPreferencesNestedFragment.RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) {
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    boolean notificationPolicyGranted = (mNotificationManager != null) && (mNotificationManager.isNotificationPolicyAccessGranted());
                    permissionsChanged = Permissions.getNotificationPolicyPermission(context) != notificationPolicyGranted;
                    if (notificationPolicyGranted)
                        Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                }
                if (requestCode == PhoneProfilesPreferencesNestedFragment.RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS) {
                    boolean canDrawOverlays = Settings.canDrawOverlays(context);
                    permissionsChanged = Permissions.getDrawOverlayPermission(context) != canDrawOverlays;
                    if (canDrawOverlays)
                        Permissions.setShowRequestDrawOverlaysPermission(context, true);
                }
                if (requestCode == PhoneProfilesPreferencesNestedFragment.RESULT_APPLICATION_PERMISSIONS) {
                    boolean calendarPermission = Permissions.checkCalendar(context);
                    permissionsChanged = Permissions.getCalendarPermission(context) != calendarPermission;
                    PPApplication.logE("PhoneProfilesPreferencesNestedFragment.doOnActivityResult", "calendarPermission="+permissionsChanged);
                    // finish Editor when permission is disabled
                    finishActivity = permissionsChanged && (!calendarPermission);
                    if (!permissionsChanged) {
                        boolean contactsPermission = Permissions.checkContacts(context);
                        permissionsChanged = Permissions.getContactsPermission(context) != contactsPermission;
                        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.doOnActivityResult", "contactsPermission="+permissionsChanged);
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!contactsPermission);
                    }
                    if (!permissionsChanged) {
                        boolean locationPermission = Permissions.checkLocation(context);
                        permissionsChanged = Permissions.getLocationPermission(context) != locationPermission;
                        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.doOnActivityResult", "locationPermission="+permissionsChanged);
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!locationPermission);
                    }
                    if (!permissionsChanged) {
                        boolean smsPermission = Permissions.checkSMS(context);
                        permissionsChanged = Permissions.getSMSPermission(context) != smsPermission;
                        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.doOnActivityResult", "smsPermission="+permissionsChanged);
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!smsPermission);
                    }
                    if (!permissionsChanged) {
                        boolean phonePermission = Permissions.checkPhone(context);
                        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.doOnActivityResult", "phonePermission="+phonePermission);
                        permissionsChanged = Permissions.getPhonePermission(context) != phonePermission;
                        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.doOnActivityResult", "permissionsChanged="+permissionsChanged);
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!phonePermission);
                    }
                    if (!permissionsChanged) {
                        boolean storagePermission = Permissions.checkStorage(context);
                        permissionsChanged = Permissions.getStoragePermission(context) != storagePermission;
                        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.doOnActivityResult", "storagePermission="+permissionsChanged);
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!storagePermission);
                    }
                    if (!permissionsChanged) {
                        boolean cameraPermission = Permissions.checkCamera(context);
                        permissionsChanged = Permissions.getCameraPermission(context) != cameraPermission;
                        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.doOnActivityResult", "cameraPermission="+permissionsChanged);
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!cameraPermission);
                    }
                    if (!permissionsChanged) {
                        boolean microphonePermission = Permissions.checkMicrophone(context);
                        permissionsChanged = Permissions.getMicrophonePermission(context) != microphonePermission;
                        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.doOnActivityResult", "microphonePermission="+permissionsChanged);
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!microphonePermission);
                    }
                    if (!permissionsChanged) {
                        boolean sensorsPermission = Permissions.checkSensors(context);
                        permissionsChanged = Permissions.getSensorsPermission(context) != sensorsPermission;
                        PPApplication.logE("PhoneProfilesPreferencesNestedFragment.doOnActivityResult", "sensorsPermission="+permissionsChanged);
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!sensorsPermission);
                    }
                }

                Permissions.saveAllPermissions(context, permissionsChanged);
                PPApplication.logE("PhoneProfilesPreferencesNestedFragment.doOnActivityResult", "permissionsChanged="+permissionsChanged);

                if (permissionsChanged) {
                    //DataWrapper dataWrapper = new DataWrapper(context, false, 0);

                    //Profile activatedProfile = dataWrapper.getActivatedProfile(true, true);
                    //dataWrapper.refreshProfileIcon(activatedProfile);
                    PPApplication.showProfileNotification(/*context*/true);
                    PPApplication.logE("ActivateProfileHelper.updateGUI", "from PhoneProfilesPreferencesNestedFragment.doOnActivityResult");
                    ActivateProfileHelper.updateGUI(context, !finishActivity, true);

                    if (finishActivity) {
                        getActivity().setResult(Activity.RESULT_CANCELED);
                        getActivity().finishAffinity();
                    } else {
                        getActivity().setResult(Activity.RESULT_OK);
                    }
                }
                else
                    getActivity().setResult(Activity.RESULT_CANCELED);
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
            final boolean enabled = PhoneProfilesService.isLocationEnabled(getActivity().getApplicationContext());
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
                LocationGeofencePreference preference = (LocationGeofencePreference)prefMng.findPreference(PREF_LOCATION_EDITOR);
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
            RingtonePreference preference = (RingtonePreference) prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE_NOTIFICATION_SOUND);
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

    @Override
    protected String getSavedInstanceStateKeyName() {
        return "PhoneProfilesPreferencesFragment_PreferenceScreenKey";
    }

}
