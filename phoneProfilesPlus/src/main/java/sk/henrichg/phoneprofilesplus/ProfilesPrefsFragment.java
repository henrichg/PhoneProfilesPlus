package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import me.drakeet.support.toast.ToastCompat;

public class ProfilesPrefsFragment extends PreferenceFragmentCompat
                                    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceManager prefMng;
    private SharedPreferences preferences;

    private boolean nestedFragment = false;

    private static final String PREF_NOTIFICATION_ACCESS = "prf_pref_volumeNotificationsAccessSettings";
    private static final int RESULT_NOTIFICATION_ACCESS_SETTINGS = 1980;

    private static final int RESULT_UNLINK_VOLUMES_APP_PREFERENCES = 1981;

    private static final String PREF_VOLUME_NOTIFICATION_VOLUME0 = "prf_pref_volumeNotificationVolume0";

    private static final String PRF_GRANT_PERMISSIONS = "prf_pref_grantPermissions";
    private static final String PREF_FORCE_STOP_APPLICATIONS_CATEGORY = "prf_pref_forceStopApplicationsCategoryRoot";
    private static final String PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER = "prf_pref_deviceForceStopApplicationInstallExtender";
    private static final String PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS = "prf_pref_deviceForceStopApplicationAccessibilitySettings";
    private static final int RESULT_ACCESSIBILITY_SETTINGS = 1983;
    private static final String PRF_GRANT_ROOT = "prf_pref_grantRoot";
    //private static final String PREF_INSTALL_SILENT_TONE = "prf_pref_soundInstallSilentTone";
    private static final String PREF_LOCK_DEVICE_CATEGORY = "prf_pref_lockDeviceCategoryRoot";
    private static final String PREF_LOCK_DEVICE_INSTALL_EXTENDER = "prf_pref_lockDeviceInstallExtender";
    private static final String PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS = "prf_pref_lockDeviceAccessibilitySettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        PPApplication.logE("ProfilesPrefsFragment.onCreate", "xxx");

        // is required for to not call onCreate and onDestroy on orientation change
        setRetainInstance(true);

        nestedFragment = !(this instanceof ProfilesPrefsActivity.ProfilesPrefsRoot);
        PPApplication.logE("ProfilesPrefsFragment.onCreate", "nestedFragment="+nestedFragment);

        initPreferenceFragment(savedInstanceState);

        updateAllSummary();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //initPreferenceFragment();
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference)
    {
        PPApplication.logE("ProfilesPrefsFragment.onDisplayPreferenceDialog", "xxx");

        PreferenceDialogFragmentCompat dialogFragment = null;

        if (preference instanceof DurationDialogPreferenceX)
        {
            ((DurationDialogPreferenceX)preference).fragment = new DurationDialogPreferenceFragmentX();
            dialogFragment = ((DurationDialogPreferenceX)preference).fragment;
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

        if (preference instanceof InfoDialogPreferenceX)
        {
            ((InfoDialogPreferenceX)preference).fragment = new InfoDialogPreferenceFragmentX();
            dialogFragment = ((InfoDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ProfileIconPreferenceX)
        {
            ((ProfileIconPreferenceX)preference).fragment = new ProfileIconPreferenceFragmentX();
            dialogFragment = ((ProfileIconPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof VolumeDialogPreferenceX)
        {
            ((VolumeDialogPreferenceX)preference).fragment = new VolumeDialogPreferenceFragmentX();
            dialogFragment = ((VolumeDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof NotificationVolume0DialogPreferenceX)
        {
            ((NotificationVolume0DialogPreferenceX)preference).fragment = new NotificationVolume0DialogPreferenceFragmentX();
            dialogFragment = ((NotificationVolume0DialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ConnectToSSIDDialogPreferenceX)
        {
            ((ConnectToSSIDDialogPreferenceX)preference).fragment = new ConnectToSSIDDialogPreferenceFragmentX();
            dialogFragment = ((ConnectToSSIDDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof BrightnessDialogPreferenceX)
        {
            ((BrightnessDialogPreferenceX)preference).fragment = new BrightnessDialogPreferenceFragmentX();
            dialogFragment = ((BrightnessDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ApplicationsDialogPreferenceX)
        {
            ((ApplicationsDialogPreferenceX)preference).fragment = new ApplicationsDialogPreferenceFragmentX();
            dialogFragment = ((ApplicationsDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        if (preference instanceof ApplicationsMultiSelectDialogPreferenceX)
        {
            ((ApplicationsMultiSelectDialogPreferenceX)preference).fragment = new ApplicationsMultiSelectDialogPreferenceFragmentX();
            dialogFragment = ((ApplicationsMultiSelectDialogPreferenceX)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null)
        {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(fragmentManager, PPApplication.PACKAGE_NAME + ".ProfilesPrefsActivity.DIALOG");
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

        PPApplication.logE("ProfilesPrefsFragment.onActivityCreated", "xxx");

        if (getActivity() == null)
            return;

        final Context context = getActivity().getBaseContext();

        // must be used handler for rewrite toolbar title/subtitle
        final ProfilesPrefsFragment fragment = this;
        Handler handler = new Handler(getActivity().getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null)
                    return;

                Toolbar toolbar = getActivity().findViewById(R.id.activity_preferences_toolbar);
                if (nestedFragment) {
                    toolbar.setTitle(fragment.getPreferenceScreen().getTitle());
                }
                else {
                    toolbar.setTitle(getString(R.string.title_activity_profile_preferences));
                }

            }
        }, 200);

        /*
        if (savedInstanceState != null) {
            //startupSource = savedInstanceState.getInt("startupSource", PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
        }
        */

        setCategorySummary("prf_pref_activationDurationCategoryRoot", context);
        setCategorySummary("prf_pref_soundProfileCategoryRoot", context);
        setCategorySummary("prf_pref_volumeCategoryRoot", context);
        setCategorySummary("prf_pref_soundsCategoryRoot", context);
        setCategorySummary("prf_pref_touchEffectsCategoryRoot", context);
        setCategorySummary("prf_pref_radiosCategoryRoot", context);
        setCategorySummary("prf_pref_screenCategoryRoot", context);
        setCategorySummary("prf_pref_othersCategoryRoot", context);
        setCategorySummary("prf_pref_applicationCategoryRoot", context);
        setCategorySummary(PREF_FORCE_STOP_APPLICATIONS_CATEGORY, context);
        setCategorySummary(PREF_LOCK_DEVICE_CATEGORY, context);

        setPermissionsPreference();

        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
        ListPreference ringerModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);
            /*if (ringerModePreference.findIndexOfValue("5") < 0) {
                // add zen mode option to preference Ringer mode
                CharSequence[] entries = ringerModePreference.getEntries();
                CharSequence[] entryValues = ringerModePreference.getEntryValues();

                CharSequence[] newEntries = new CharSequence[entries.length + 1];
                CharSequence[] newEntryValues = new CharSequence[entries.length + 1];

                for (int i = 0; i < entries.length; i++) {
                    newEntries[i] = entries[i];
                    newEntryValues[i] = entryValues[i];
                }

                newEntries[entries.length] = context.getString(R.string.array_pref_ringerModeArray_ZenMode);
                newEntryValues[entries.length] = "5";

                ringerModePreference.setEntries(newEntries);
                ringerModePreference.setEntryValues(newEntryValues);
                ringerModePreference.setValue(Integer.toString(profile._volumeRingerMode));
                setSummary(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, profile._volumeRingerMode);
            }
            */

            /*final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                     (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                    );*/
        final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext(), false);
        PPApplication.logE("ProfilesPrefsFragment.onActivityCreated","canEnableZenMode="+canEnableZenMode);

        ListPreference zenModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
        if (zenModePreference != null) {
            String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "");
            zenModePreference.setEnabled((value.equals("5")) && canEnableZenMode);
        }

        Preference notificationAccessPreference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
        if (notificationAccessPreference != null) {
            if (canEnableZenMode) {
                PreferenceScreen preferenceCategory = findPreference("prf_pref_soundProfileCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(notificationAccessPreference);
            } else {
                if (ringerModePreference != null) {
                    CharSequence[] entries = ringerModePreference.getEntries();
                    entries[5] = "(S) " + getString(R.string.array_pref_soundModeArray_ZenMode);
                    ringerModePreference.setEntries(entries);
                }

                boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                @SuppressLint("InlinedApi")
                final boolean showDoNotDisturbPermission =
                        (android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                                GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getActivity().getApplicationContext());
                if (showDoNotDisturbPermission) {
                    notificationAccessPreference.setTitle(getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions));
                    notificationAccessPreference.setSummary(getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions_summary));
                }

                //notificationAccessPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
                notificationAccessPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                        if (showDoNotDisturbPermission) {
                            @SuppressLint("InlinedApi")
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                        }
                        else
                        if (GlobalGUIRoutines.activityActionExists("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS", context)) {
                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                        }
                        else {
                            if (getActivity() != null) {
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
                        }
                        return false;
                    }
                });
            }
        }

        if (ringerModePreference != null) {
            ringerModePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String sNewValue = (String) newValue;
                    int iNewValue;
                    if (sNewValue.isEmpty())
                        iNewValue = 0;
                    else
                        iNewValue = Integer.parseInt(sNewValue);

                    /*final boolean canEnableZenMode =
                            (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                                    (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                            );*/
                    final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext(), true);

                    Preference zenModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
                    if (zenModePreference != null) {
                        zenModePreference.setEnabled((iNewValue == 5) && canEnableZenMode);

                        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                        @SuppressLint("InlinedApi")
                        boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                                GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
                        GlobalGUIRoutines.setPreferenceTitleStyleX(zenModePreference, true, false, true, false, false, addS);
                    }

                    return true;
                }
            });
        }
        /*}
        else
        {
            // remove zen mode preferences from preferences screen
            // for Android version < 5.0 this is not supported
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
        }*/
        if ((android.os.Build.VERSION.SDK_INT < 23) || (android.os.Build.VERSION.SDK_INT > 23)) {
            Preference preference = prefMng.findPreference("prf_pref_volumeVibrateWhenRingingRootInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("prf_pref_soundProfileCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
        if (android.os.Build.VERSION.SDK_INT == 23) {
            ListPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null)
            {
                preference.setTitle("(R) "+getString(R.string.profile_preferences_vibrateWhenRinging));
                preference.setDialogTitle("(R) "+getString(R.string.profile_preferences_vibrateWhenRinging));
                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, "");
                setSummary(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, value);
            }
        }
        if (android.os.Build.VERSION.SDK_INT < 24) {
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("prf_pref_othersCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
        /*if (android.os.Build.VERSION.SDK_INT >= 26) {
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
            if (preference != null)
            {
                preference.setTitle("(R) "+getString(R.string.profile_preferences_deviceWiFiAP));
                String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP, "");
                setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP, value);
            }
        }*/
        if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY))
        {
            ListPreference networkTypePreference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE);
            if (networkTypePreference != null) {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                int phoneType = TelephonyManager.PHONE_TYPE_GSM;
                if (telephonyManager != null)
                    phoneType = telephonyManager.getPhoneType();

                if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                    /*if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMDPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMDPValues));
                    } else {*/
                    networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMArray));
                    networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMValues));
                    //}
                    String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, "");
                    networkTypePreference.setValue(value);
                    setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, value);
                }

                if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    /*if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMADPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMADPValues));
                    } else {*/
                    networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMAArray));
                    networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMAValues));
                    //}
                    String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, "");
                    networkTypePreference.setValue(value);
                    setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, value);
                }
            }
        }
        DurationDialogPreferenceX durationPreference = prefMng.findPreference(Profile.PREF_PROFILE_DURATION);
        if (durationPreference != null)
        {
            durationPreference.setTitle("[M] " + context.getString(R.string.profile_preferences_duration));
            durationPreference.setDialogTitle("[M] " + context.getString(R.string.profile_preferences_duration));
            String value = preferences.getString(Profile.PREF_PROFILE_DURATION, "");
            setSummary(Profile.PREF_PROFILE_DURATION, value);
        }

        Preference preference;
        /*if (!ApplicationPreferences.preferences.getBoolean(ActivateProfileHelper.PREF_MERGED_RING_NOTIFICATION_VOLUMES, true)) {
            // detection of volumes merge = volumes are not merged
            preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("prf_pref_volumeCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
        else {*/
            preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);
            if (preference != null) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // start preferences activity for default profile
                        if (getActivity() != null) {
                            Intent intent = new Intent(getActivity().getBaseContext(), PhoneProfilesPrefsActivity.class);
                            intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "categorySystemRoot");
                            //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                            getActivity().startActivityForResult(intent, RESULT_UNLINK_VOLUMES_APP_PREFERENCES);
                        }
                        return false;
                    }
                });
            }
        //}

        InfoDialogPreferenceX infoDialogPreference = prefMng.findPreference("prf_pref_preferenceTypesInfo");
        if (infoDialogPreference != null) {
            infoDialogPreference.setInfoText(
                    "• " + getString(R.string.important_info_profile_grant)+"\n\n"+
                    "<II0 [0,"+R.id.activity_info_notification_profile_grant_1_howTo_1+"]>"+
                        getString(R.string.profile_preferences_types_G1_show_info)+
                    "<II0/>"+
                    "\n\n"+
                    "• " + getString(R.string.important_info_profile_root)+"\n\n"+
                    "• " + getString(R.string.important_info_profile_settings)+"\n\n"+
                    "• " + getString(R.string.important_info_profile_interactive));
        }

        Preference showInActivatorPreference = prefMng.findPreference(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR);
        if (showInActivatorPreference != null) {
            showInActivatorPreference.setTitle("[A] " + getResources().getString(R.string.profile_preferences_showInActivator));
            boolean value = preferences.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, false);
            setSummary(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, value);
        }

        Preference extenderPreference = prefMng.findPreference(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String url = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
                    return false;
                }
            });
        }
        Preference accessibilityPreference = prefMng.findPreference(PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, context)) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                    } else {
                        if (getActivity() != null) {
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
                    }
                    return false;
                }
            });
        }

        /*
        boolean toneInstalled = TonesHandler.isToneInstalled(TonesHandler.TONE_ID, getActivity().getApplicationContext());
        if (!toneInstalled) {
            Preference installTonePreference = prefMng.findPreference(PREF_INSTALL_SILENT_TONE);
            if (installTonePreference != null) {
                installTonePreference.setSummary(R.string.profile_preferences_installSilentTone_summary);
                installTonePreference.setEnabled(true);
                installTonePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (!TonesHandler.isToneInstalled(TonesHandler.TONE_ID, context.getApplicationContext()))
                            TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, context.getApplicationContext());
                        else {
                            Toast msg = ToastCompat.makeText(context.getApplicationContext(),
                                    context.getResources().getString(R.string.profile_preferences_installSilentTone_installed_summary),
                                    Toast.LENGTH_SHORT);
                            msg.show();
                        }
                        return false;
                    }
                });
            }
        }
        else {
            Preference installTonePreference = prefMng.findPreference(PREF_INSTALL_SILENT_TONE);
            if (installTonePreference != null) {
                installTonePreference.setSummary(R.string.profile_preferences_installSilentTone_installed_summary);
                installTonePreference.setEnabled(false);
            }
        }
        */

        extenderPreference = prefMng.findPreference(PREF_LOCK_DEVICE_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String url = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
                    return false;
                }
            });
        }
        accessibilityPreference = prefMng.findPreference(PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, context)) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                    } else {
                        if (getActivity() != null) {
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
                    }
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

            /*
            SharedPreferences.Editor editor = profilesPreferences.edit();
            updateSharedPreferences(editor, preferences);
            editor.apply();
            */

            PPApplication.logE("ProfilesPrefsFragment.onDestroy", "xxx");

        } catch (Exception ignored) {}

        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PPApplication.logE("ProfilesPrefsFragment.onSharedPreferenceChanged", "key="+key);

        String value;
        if (key.equals(Profile.PREF_PROFILE_NAME)) {
            value = sharedPreferences.getString(key, "");
            if (getActivity() != null) {
                // must be used handler for rewrite toolbar title/subtitle
                final String _value = value;
                Handler handler = new Handler(getActivity().getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() == null)
                            return;

                        Toolbar toolbar = getActivity().findViewById(R.id.activity_preferences_toolbar);
                        toolbar.setSubtitle(getString(R.string.profile_string_0) + ": " + _value);
                    }
                }, 200);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
                key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
                key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE) ||
                key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON)) {
            boolean bValue = sharedPreferences.getBoolean(key, false);
            value = Boolean.toString(bValue);
        }
        else {
            if (prefMng.findPreference(key) != null)
                value = sharedPreferences.getString(key, "");
            else
                value = "";
        }
        setSummary(key, value);

        // disable depended preferences
        disableDependedPref(key, value);

        setPermissionsPreference();

        ProfilesPrefsActivity activity = (ProfilesPrefsActivity)getActivity();
        PPApplication.logE("ProfilesPrefsFragment.onSharedPreferenceChanged", "activity="+activity);
        if (activity != null) {
            activity.showSaveMenu = true;
            activity.invalidateOptionsMenu();
        }
    }

    void doOnActivityResult(int requestCode, int resultCode, Intent data) {
        PPApplication.logE("ProfilesPrefsFragment.doOnActivityResult", "xxx");
        PPApplication.logE("ProfilesPrefsFragment.doOnActivityResult", "requestCode=" + requestCode);

        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE) {
            setPermissionsPreference();
        }
        /*if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_GRANT_ROOT) {
            Log.e("------ ProfilesPrefsFragment.doOnActivityResult", "requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_GRANT_ROOT");
            PPApplication.isRootGranted();
            setPermissionsPreference();
        }*/
        if (requestCode == WallpaperViewPreferenceX.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null)
        {
            //Uri selectedImage = data.getData();
            String  d = data.getDataString();
            if (d != null) {
                Uri selectedImage = Uri.parse(d);
                /*//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        ContentResolver resolver = getActivity().getContentResolver();
                        resolver.takePersistableUriPermission(selectedImage, takeFlags);
                    } catch (Exception e) {
                        Log.e("ProfilesPrefsFragment.doOnActivityResult", Log.getStackTraceString(e));
                    }
                //}*/
                WallpaperViewPreferenceX preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
                if (preference != null)
                    preference.setImageIdentifier(selectedImage.toString());
                /*
                if (ProfilesPrefsFragment.changedWallpaperViewPreference != null) {
                    // set image identifier for get bitmap path
                    ProfilesPrefsFragment.changedWallpaperViewPreference.setImageIdentifier(selectedImage.toString());
                    ProfilesPrefsFragment.changedWallpaperViewPreference = null;
                }
                */
            }
        }
        if (requestCode == ProfileIconPreferenceX.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null)
        {
            //Uri selectedImage = data.getData();
            String  d = data.getDataString();
            if (d != null) {
                Uri selectedImage = Uri.parse(d);
                /*//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        ContentResolver resolver = getActivity().getContentResolver();
                        resolver.takePersistableUriPermission(selectedImage, takeFlags);
                    } catch (Exception e) {
                        Log.e("ProfilesPrefsFragment.doOnActivityResult", Log.getStackTraceString(e));
                    }
                //}*/

                Resources resources = getResources();
                int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
                int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
                if (BitmapManipulator.checkBitmapSize(selectedImage.toString(), width, height, getContext())) {
                    ProfileIconPreferenceX preference = prefMng.findPreference(Profile.PREF_PROFILE_ICON);
                    if (preference != null) {
                        preference.setImageIdentifierAndType(selectedImage.toString(), false);
                        preference.setValue(true);
                        preference.dismissDialog();
                    }
                    /*if (ProfilesPrefsFragment.changedProfileIconPreference != null) {
                        // set image identifier ant type for get bitmap path
                        ProfilesPrefsFragment.changedProfileIconPreference.dismissDialog();
                        ProfilesPrefsFragment.changedProfileIconPreference.setImageIdentifierAndType(selectedImage.toString(), false, true);
                        ProfilesPrefsFragment.changedProfileIconPreference = null;
                    }*/
                }
                else {
                    if (getActivity() != null) {
                        String text = getResources().getString(R.string.profileicon_pref_dialog_custom_icon_image_too_large);
                        text = text + " " + (width * BitmapManipulator.ICON_BITMAP_SIZE_MULTIPLIER);
                        text = text + "x" + (height * BitmapManipulator.ICON_BITMAP_SIZE_MULTIPLIER);
                        Toast msg = ToastCompat.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_LONG);
                        msg.show();
                    }
                }
            }
        }
        if (requestCode == RESULT_NOTIFICATION_ACCESS_SETTINGS) {
            /*final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                            (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                    );*/

            final String sZenModeType = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "");
            setSummary(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, sZenModeType);
        }
        if (requestCode == ApplicationsDialogPreferenceX.RESULT_APPLICATIONS_EDITOR && resultCode == Activity.RESULT_OK && data != null)
        {
            ApplicationsDialogPreferenceX preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            if (preference != null) {
                preference.updateShortcut(
                        (Intent)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT),
                        data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                        /*(Bitmap)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON),*/
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_POSITION, -1),
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0));
            }
            /*
            if (ProfilesPrefsFragment.applicationsDialogPreference != null) {
                ProfilesPrefsFragment.applicationsDialogPreference.updateShortcut(
                        (Intent)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT),
                        data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_POSITION, -1),
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0));

                ProfilesPrefsFragment.applicationsDialogPreference = null;
            }*/
        }
        if (requestCode == ApplicationEditorDialogX.RESULT_INTENT_EDITOR) {
            if (resultCode == Activity.RESULT_OK) {
                ApplicationsDialogPreferenceX preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
                if ((preference != null) && (data != null)) {
                    preference.updateIntent((PPIntent) data.getParcelableExtra(ApplicationEditorDialogX.EXTRA_PP_INTENT),
                            (Application) data.getParcelableExtra(ApplicationEditorDialogX.EXTRA_APPLICATION),
                            data.getIntExtra(ApplicationEditorIntentActivityX.EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0));
                }
            }
        }
        if (requestCode == RESULT_UNLINK_VOLUMES_APP_PREFERENCES) {
            disableDependedPref(Profile.PREF_PROFILE_VOLUME_RINGTONE);
            disableDependedPref(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
            setSummary(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);
        }
        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS) {
            disableDependedPref(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            disableDependedPref(Profile.PREF_PROFILE_LOCK_DEVICE);
            // show save menu
            ProfilesPrefsActivity activity = (ProfilesPrefsActivity)getActivity();
            if (activity != null) {
                activity.showSaveMenu = true;
                activity.invalidateOptionsMenu();
            }
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_WALLPAPER) {
            WallpaperViewPreferenceX preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
            if (preference != null)
                preference.startGallery();
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON) {
            ProfileIconPreferenceX preference = prefMng.findPreference(Profile.PREF_PROFILE_ICON);
            if (preference != null)
                preference.startGallery();
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG) {
            BrightnessDialogPreferenceX preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS);
            if (preference != null)
                preference.enableViews();
        }
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_RINGTONE_PREFERENCE) {
            RingtonePreferenceX preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_ALARM);
            if (preference != null)
                preference.refreshListView();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //outState.putBoolean("nestedFragment", nestedFragment);
    }

    private void initPreferenceFragment(@SuppressWarnings("unused") Bundle savedInstanceState) {
        prefMng = getPreferenceManager();

        preferences = prefMng.getSharedPreferences();

        /*
        PPApplication.logE("ProfilesPrefsFragment.initPreferenceFragment", "getContext()="+getContext());

        if (savedInstanceState == null) {
            if (getContext() != null) {
                profilesPreferences = getContext().getSharedPreferences(PREFS_NAME_ACTIVITY, Activity.MODE_PRIVATE);

                SharedPreferences.Editor editor = preferences.edit();
                updateSharedPreferences(editor, profilesPreferences);
                editor.apply();
            }
        }
        */

        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    /*
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
    }
    */

    private String getCategoryTitleWhenPreferenceChanged(String key, int preferenceTitleId, boolean systemSettings, Context context) {
        //Preference preference = prefMng.findPreference(key);
        String title = "";
        //if ((preference != null) && (preference.isEnabled())) {
        if (Profile.isProfilePreferenceAllowed(key, null, preferences, true, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            if (//key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
                    key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
                            key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE)) {
                /*boolean defaultValue =
                        getResources().getBoolean(
                                GlobalGUIRoutines.getResourceId(key, "bool", context));*/
                //noinspection ConstantConditions
                boolean defaultValue = Profile.defaultValuesBoolean.get(key);
                if (preferences.getBoolean(key, defaultValue) != defaultValue)
                    title = getString(preferenceTitleId);
            }
            else {
                /*String defaultValue =
                        getResources().getString(
                                GlobalGUIRoutines.getResourceId(key, "string", context));*/
                String defaultValue = Profile.defaultValuesString.get(key);
                String value = preferences.getString(key, defaultValue);
                if (value != null) {
                    if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE) ||
                            key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION) ||
                            key.equals(Profile.PREF_PROFILE_VOLUME_MEDIA) ||
                            key.equals(Profile.PREF_PROFILE_VOLUME_ALARM) ||
                            key.equals(Profile.PREF_PROFILE_VOLUME_SYSTEM) ||
                            key.equals(Profile.PREF_PROFILE_VOLUME_VOICE) ||
                            key.equals(Profile.PREF_PROFILE_VOLUME_DTMF) ||
                            key.equals(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY) ||
                            key.equals(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO)) {
                        if (VolumeDialogPreferenceX.changeEnabled(value))
                            title = getString(preferenceTitleId);
                    } else
                    if (key.equals(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS)) {
                        if (BrightnessDialogPreferenceX.changeEnabled(value))
                            title = getString(preferenceTitleId);
                    } else {
                        if (!value.equals(defaultValue)) {
                            if (key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING) &&
                                    (android.os.Build.VERSION.SDK_INT == 23))
                                title = "(R) "+getString(R.string.profile_preferences_vibrateWhenRinging);
                            else
                            if (key.equals(Profile.PREF_PROFILE_DURATION))
                                title = "[M] " + context.getString(R.string.profile_preferences_duration);
                            else
                                title = getString(preferenceTitleId);
                        }
                    }
                }
            }
            if (systemSettings) {
                if (!title.isEmpty() && !title.contains("(S)"))
                    title = "(S) " + title;
            }
            return title;
        }
        else
            return title;
    }

    private void setCategorySummary(String key, Context context) {
        Preference preferenceScreen = prefMng.findPreference(key);
        if (preferenceScreen == null)
            return;

        boolean forceSet = false;
        boolean _bold = false;
        String summary = "";

        /*if (key.equals(Profile.PREF_PROFILE_DURATION) ||
                key.equals(Profile.PREF_PROFILE_AFTER_DURATION_DO) ||
                key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
                key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND) ||
                key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE)) {*/
        if (key.equals("prf_pref_activationDurationCategoryRoot")) {
            String title;
            String askForDurationTitle = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_ASK_FOR_DURATION, R.string.profile_preferences_askForDuration, false, context);
            if (askForDurationTitle.isEmpty()) {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION, R.string.profile_preferences_duration, false, context);
                String afterDurationDoTitle = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_AFTER_DURATION_DO, R.string.profile_preferences_afterDurationDo, false, context);
                if (!title.isEmpty()) {
                    _bold = true;
                    String value = preferences.getString(Profile.PREF_PROFILE_DURATION,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION));
                    if (value != null) {
                        value = GlobalGUIRoutines.getDurationString(Integer.parseInt(value));
                        summary = summary + title + ": " + value + " • ";

                        value = GlobalGUIRoutines.getListPreferenceString(
                                preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_DO,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_AFTER_DURATION_DO)),
                                R.array.afterProfileDurationDoValues, R.array.afterProfileDurationDoArray, context);
                        summary = summary + afterDurationDoTitle + ": " + value;
                    }
                    else
                        summary = summary + afterDurationDoTitle;
                }
            }
            else {
                _bold = true;
                summary = summary + askForDurationTitle + ": " + getString(R.string.profile_preferences_enabled);
            }
            if (_bold) {
                // any of duration preferences are set
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, R.string.profile_preferences_durationNotificationSound, false, context);
                if (!title.isEmpty()) {
                    if (!summary.isEmpty()) summary = summary + " • ";
                    summary = summary + title + ": <ringtone_name>";
                }
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, R.string.profile_preferences_durationNotificationVibrate, false, context);
                if (!title.isEmpty()) {
                    if (!summary.isEmpty()) summary = summary + " • ";
                    summary = summary + title +  ": " + getString(R.string.profile_preferences_enabled);
                }
                GlobalGUIRoutines.setRingtonePreferenceSummary(summary,
                        preferences.getString(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND)),
                        preferenceScreen, context);
                //noinspection ConstantConditions
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, _bold, true, false, false, false);
                return;
            }
        }

        /*if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE) ||
                key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING)) {*/
        if (key.equals("prf_pref_soundProfileCategoryRoot")) {
            String ringerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_RINGER_MODE));
            String zenMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_ZEN_MODE));
            String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, R.string.profile_preferences_volumeSoundMode, false, context);
            if (!title.isEmpty()) {
                _bold = true;

                String value = GlobalGUIRoutines.getListPreferenceString(ringerMode,
                                        R.array.soundModeValues, R.array.soundModeArray, context);

                summary = summary + title + ": " + value;
            }
            if (_bold) {
                boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                @SuppressLint("InlinedApi")
                boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
                int titleRes = R.string.profile_preferences_volumeZenMode;
                if (Build.VERSION.SDK_INT >= 23)
                    titleRes = R.string.profile_preferences_volumeZenModeM;
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, titleRes, addS, context);
                if (!title.isEmpty()) {
                    final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext(), false);
                    if ((ringerMode != null) && (ringerMode.equals("5")) && canEnableZenMode) {
                        if (!summary.isEmpty()) summary = summary + " • ";

                        String value = GlobalGUIRoutines.getZenModePreferenceString(zenMode, context);

                        summary = summary + title + ": " + value;
                    }
                }
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, R.string.profile_preferences_vibrateWhenRinging, false, context);
                if (!title.isEmpty()) {
                    if (ringerMode != null) {
                        if (ringerMode.equals("1") || ringerMode.equals("4")) {
                            if (!summary.isEmpty()) summary = summary + " • ";

                            String value = GlobalGUIRoutines.getListPreferenceString(
                                    preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING)),
                                    R.array.vibrateWhenRingingValues, R.array.vibrateWhenRingingArray, context);

                            summary = summary + title + ": " + value;
                        } else if ((ringerMode.equals("5")) && (zenMode != null) && (zenMode.equals("1") || zenMode.equals("2"))) {
                            if (!summary.isEmpty()) summary = summary + " • ";

                            String value = GlobalGUIRoutines.getListPreferenceString(
                                    preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING)),
                                    R.array.vibrateWhenRingingValues, R.array.vibrateWhenRingingArray, context);

                            summary = summary + title + ": " + value;
                        }
                    }
                }
            }
        }

        /*if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_MEDIA) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ALARM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SYSTEM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_VOICE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE)) {*/
        if (key.equals("prf_pref_volumeCategoryRoot")) {

            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

            String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_RINGTONE, R.string.profile_preferences_volumeRingtone, false, context);
            if (!title.isEmpty()) {
                _bold = true;

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_RINGTONE));

                    value = Profile.getVolumeRingtoneValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

                    summary = summary + title + ": " + value;
                }
                else
                    summary = summary + title;
            }
            String ringtoneValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
            if ((!ActivateProfileHelper.getMergedRingNotificationVolumes(context) || ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) &&
                    getEnableVolumeNotificationByRingtone(ringtoneValue)) {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, R.string.profile_preferences_volumeNotification, false, context);
                if (!title.isEmpty()) {
                    _bold = true;
                    if (!summary.isEmpty()) summary = summary + " • ";

                    if (audioManager != null) {
                        String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_NOTIFICATION,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_NOTIFICATION));

                        value = Profile.getVolumeRingtoneValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

                        summary = summary + title + ": " + value;
                    }
                    else
                        summary = summary + title;
                }
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_MEDIA, R.string.profile_preferences_volumeMedia, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_MEDIA,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_MEDIA));

                    value = Profile.getVolumeRingtoneValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                    summary = summary + title + ": " + value;
                }
                else
                    summary = summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ALARM, R.string.profile_preferences_volumeAlarm, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_ALARM,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_ALARM));

                    value = Profile.getVolumeRingtoneValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

                    summary = summary + title + ": " + value;
                }
                else
                    summary = summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_SYSTEM, R.string.profile_preferences_volumeSystem, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_SYSTEM,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_SYSTEM));

                    value = Profile.getVolumeRingtoneValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);

                    summary = summary + title + ": " + value;
                }
                else
                    summary = summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_VOICE, R.string.profile_preferences_volumeVoiceCall, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_VOICE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_VOICE));

                    value = Profile.getVolumeRingtoneValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);

                    summary = summary + title + ": " + value;
                }
                else
                    summary = summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_DTMF, R.string.profile_preferences_volumeDTMF, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_DTMF,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_DTMF));

                    value = Profile.getVolumeRingtoneValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF);

                    summary = summary + title + ": " + value;
                }
                else
                    summary = summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY, R.string.profile_preferences_volumeAccessibility, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                if ((Build.VERSION.SDK_INT >= 26) && (audioManager != null)) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY));

                    value = Profile.getVolumeRingtoneValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY);

                    summary = summary + title + ": " + value;
                }
                else
                    summary = summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO, R.string.profile_preferences_volumeBluetoothSCO, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO));

                    value = Profile.getVolumeRingtoneValue(value) + "/" + audioManager.getStreamMaxVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO);

                    summary = summary + title + ": " + value;
                }
                else
                    summary = summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, R.string.profile_preferences_volumeSpeakerPhone, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE)),
                        R.array.volumeSpeakerPhoneValues, R.array.volumeSpeakerPhoneArray, context);

                summary = summary + title + ": " + value;
            }
        }

        /*if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
                //key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
                //key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE)) {
            //key.equals(Profile.PREF_PROFILE_SOUND_ALARM)) {*/
        if (key.equals("prf_pref_soundsCategoryRoot")) {
            String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, R.string.profile_preferences_soundRingtoneChange, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title + ": <ringtone_name>";
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_RINGTONE);
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, R.string.profile_preferences_soundNotificationChange, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title + ": <notification_name>";
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, R.string.profile_preferences_soundAlarmChange, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title + ": <alarm_name>";
            }
            //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_ALARM);
            if (_bold) {
                GlobalGUIRoutines.setProfileSoundsPreferenceSummary(summary,
                        preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE)),
                        preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION)),
                        preferences.getString(Profile.PREF_PROFILE_SOUND_ALARM,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ALARM)),
                        preferenceScreen, context);
                //noinspection ConstantConditions
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, _bold, true, false, false, false);
                return;
            }

        }

        /*if (key.equals(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
                key.equals(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ON_TOUCH)) {*/
        if (key.equals("prf_pref_touchEffectsCategoryRoot")) {
            String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_ON_TOUCH, R.string.profile_preferences_soundOnTouch, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_SOUND_ON_TOUCH,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ON_TOUCH)),
                        R.array.soundOnTouchValues, R.array.soundOnTouchArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, R.string.profile_preferences_vibrationOnTouch, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH)),
                        R.array.vibrationOnTouchValues, R.array.vibrationOnTouchArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, R.string.profile_preferences_dtmfToneWhenDialing, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING)),
                        R.array.dtmfToneWhenDialingValues, R.array.dtmfToneWhenDialingArray, context);

                summary = summary + title + ": " + value;
            }
        }

        /*if (key.equals(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_AUTOSYNC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_GPS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NFC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID)) {*/
        if (key.equals("prf_pref_radiosCategoryRoot")) {
            String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, R.string.profile_preferences_deviceAirplaneMode, false, context);
            if (!title.isEmpty()) {
                _bold = true;

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE)),
                        R.array.hardwareModeValues, R.array.hardwareModeArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AUTOSYNC, R.string.profile_preferences_deviceAutosync, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOSYNC,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AUTOSYNC)),
                        R.array.hardwareModeValues, R.array.hardwareModeArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, R.string.profile_preferences_deviceNetworkType, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                int phoneType = TelephonyManager.PHONE_TYPE_GSM;
                if (telephonyManager != null)
                    phoneType = telephonyManager.getPhoneType();

                int arrayValues = 0;
                int arrayStrings = 0;
                if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                    arrayStrings = R.array.networkTypeGSMArray;
                    arrayValues = R.array.networkTypeGSMValues;
                }

                if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    arrayStrings = R.array.networkTypeCDMAArray;
                    arrayValues = R.array.networkTypeCDMAValues;
                }

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)),
                        arrayValues, arrayStrings, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, R.string.profile_preferences_deviceNetworkTypePrefs, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS)),
                        R.array.networkTypePrefsValues, R.array.networkTypePrefsArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, R.string.profile_preferences_deviceMobileData, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)),
                        R.array.hardwareModeValues, R.array.hardwareModeArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, R.string.profile_preferences_deviceMobileDataPrefs, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS)),
                        R.array.mobileDataPrefsValues, R.array.mobileDataPrefsArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI, R.string.profile_preferences_deviceWiFi, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WIFI)),
                        R.array.wifiModeValues, R.array.wifiModeArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, R.string.profile_preferences_deviceConnectToSSID, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID));
                if (value != null) {
                    if (value.equals(Profile.CONNECTTOSSID_JUSTANY))
                        value = getString(R.string.connect_to_ssid_pref_dlg_summary_text_just_any);

                    summary = summary + title + ": " + value;
                }
                else
                    summary = summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI_AP, R.string.profile_preferences_deviceWiFiAP, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WIFI_AP)),
                        R.array.wifiAPValues, R.array.wifiAPArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, R.string.profile_preferences_deviceWiFiAPPrefs, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS)),
                        R.array.wiFiAPPrefsValues, R.array.wiFiAPPrefsArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, R.string.profile_preferences_deviceBluetooth, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_BLUETOOTH,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_BLUETOOTH)),
                        R.array.hardwareModeValues, R.array.hardwareModeArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_GPS, R.string.profile_preferences_deviceGPS, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_GPS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_GPS)),
                        R.array.hardwareModeValues, R.array.hardwareModeArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, R.string.profile_preferences_deviceLocationServicePrefs, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS)),
                        R.array.locationServicePrefsValues, R.array.locationServicePrefsArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NFC, R.string.profile_preferences_deviceNFC, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_NFC,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NFC)),
                        R.array.hardwareModeValues, R.array.hardwareModeArray, context);

                summary = summary + title + ": " + value;
            }
        }

        /*if (key.equals(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_KEYGUARD) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_AUTOROTATE) ||
                key.equals(Profile.PREF_PROFILE_NOTIFICATION_LED) ||
                key.equals(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS) ||
                key.equals(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE)) {*/
        if (key.equals("prf_pref_screenCategoryRoot")) {
            String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, R.string.profile_preferences_deviceScreenTimeout, false, context);
            if (!title.isEmpty()) {
                _bold = true;

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT)),
                        R.array.screenTimeoutValues, R.array.screenTimeoutArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_KEYGUARD, R.string.profile_preferences_deviceKeyguard, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_KEYGUARD,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_KEYGUARD)),
                        R.array.keyguardValues, R.array.keyguardArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, R.string.profile_preferences_deviceBrightness, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS,
                                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS));
                boolean automatic = Profile.getDeviceBrightnessAutomatic(value);
                boolean changeLevel = Profile.getDeviceBrightnessChangeLevel(value);
                int iValue = Profile.getDeviceBrightnessValue(value);

                boolean adaptiveAllowed = (android.os.Build.VERSION.SDK_INT <= 21) ||
                                    (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS, null, null, true, context).allowed
                                            == PreferenceAllowed.PREFERENCE_ALLOWED);

                String summaryString;
                if (automatic)
                {
                    //if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
                    summaryString = context.getResources().getString(R.string.preference_profile_adaptiveBrightness);
                    //else
                    //    summaryString = _context.getResources().getString(R.string.preference_profile_autoBrightness);
                }
                else
                    summaryString = context.getResources().getString(R.string.preference_profile_manual_brightness);

                if (changeLevel && (adaptiveAllowed || !automatic)) {
                    String _value = iValue + "/100";
                    summaryString = summaryString + "; " + _value;
                }

                summary = summary + title + ": " + summaryString;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, R.string.profile_preferences_deviceAutoRotation,false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOROTATE,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AUTOROTATE)),
                        R.array.displayRotationValues, R.array.displayRotationArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_NOTIFICATION_LED, R.string.profile_preferences_notificationLed, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_NOTIFICATION_LED,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_NOTIFICATION_LED)),
                        R.array.notificationLedValues, R.array.notificationLedArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, R.string.profile_preferences_headsUpNotifications, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS)),
                        R.array.headsUpNotificationsValues, R.array.headsUpNotificationsArray, context);

                summary = summary + title + ": " + value;
            }
            /*title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE, R.string.profile_preferences_screenNightMode, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE)),
                        R.array.screenNightModeValues, R.array.screenNightModeArray, context);

                summary = summary + title + ": " + value;
            }*/
        }

        /*if (key.equals(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {*/
        if (key.equals("prf_pref_othersCategoryRoot")) {
            String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, R.string.profile_preferences_devicePowerSaveMode, false, context);
            if (!title.isEmpty()) {
                _bold = true;

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE)),
                        R.array.hardwareModeValues, R.array.hardwareModeArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, R.string.profile_preferences_deviceRunApplicationsShortcutsChange, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME));
                if (value != null) {
                    String[] splits = value.split("\\|");

                    summary = summary + title + ": " + context.getString(R.string.applications_multiselect_summary_text_selected) + " " + splits.length;
                }
                else
                    summary = summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, R.string.profile_preferences_deviceCloseAllApplications, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS)),
                        R.array.closeAllApplicationsValues, R.array.closeAllApplicationsArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, R.string.profile_preferences_deviceWallpaperChange, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE)),
                        R.array.changeWallpaperValues, R.array.changeWallpaperArray, context);

                summary = summary + title + ": " + value;

                if (!summary.isEmpty()) summary = summary +" • ";

                value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR)),
                        R.array.wallpaperForValues, R.array.wallpaperForArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_LOCK_DEVICE, R.string.profile_preferences_lockDevice, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_LOCK_DEVICE)),
                        R.array.lockDeviceValues, R.array.lockDeviceArray, context);

                summary = summary + title + ": " + value;
            }
        }

        if (key.equals(PREF_FORCE_STOP_APPLICATIONS_CATEGORY)) {

            boolean ok = true;
            int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
            if (extenderVersion == 0) {
                ok = false;
                summary = getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + getString(R.string.preference_not_allowed_reason_not_extender_installed);
            }
            else
            if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0) {
                ok = false;
                summary = getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
            }
            else
            if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context)) {
                ok = false;
                summary = getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
            }

            int index = 0;
            String sValue = "0";
            if (ok) {
                String defaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
                sValue = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, defaultValue);
                String[] entryValues = getResources().getStringArray(R.array.forceStopApplicationValues);
                for (String v : entryValues) {
                    if (v.equals(sValue))
                        break;
                    index++;
                }
                String[] entries = getResources().getStringArray(R.array.forceStopApplicationArray);
                summary = (index >= 0) ? entries[index] : null;
            }

            if ((sValue != null) && sValue.equals("1")) {
                String defaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
                sValue = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, defaultValue);
                summary = summary + " • " +
                        ApplicationsMultiSelectDialogPreferenceX.getSummaryForPreferenceCategory(sValue, "accessibility_2.0", context);
            }

            _bold = (index > 0);
            forceSet = true;
        }

        if (key.equals(PREF_LOCK_DEVICE_CATEGORY)) {
            int index = 0;
            String sValue;

            String defaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_LOCK_DEVICE);
            sValue = preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, defaultValue);
            String[] entryValues = getResources().getStringArray(R.array.lockDeviceValues);
            for (String v : entryValues) {
                if (v.equals(sValue))
                    break;
                index++;
            }
            String[] entries = getResources().getStringArray(R.array.lockDeviceArray);
            summary = (index >= 0) ? entries[index] : null;

            if ((sValue != null) && sValue.equals("3")) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0) {
                    //ok = false;
                    summary = summary + "\n\n" +
                            getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + getString(R.string.preference_not_allowed_reason_not_extender_installed);
                } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_4_0) {
                    //ok = false;
                    summary = summary + "\n\n" +
                            getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context)) {
                    //ok = false;
                    summary = summary + "\n\n" +
                            getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                }
            }

            _bold = (index > 0);
            forceSet = true;
        }

        /*if (key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING)) {*/
        if (key.equals("prf_pref_applicationCategoryRoot")) {
            String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING, R.string.profile_preferences_applicationDisableWifiScanning, false, context);
            if (!title.isEmpty()) {
                _bold = true;

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING)),
                        R.array.applicationDisableScanningValues, R.array.applicationDisableScanningArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING, R.string.profile_preferences_applicationDisableBluetoothScanning, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING)),
                        R.array.applicationDisableScanningValues, R.array.applicationDisableScanningArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING, R.string.profile_preferences_applicationDisableLocationScanning,false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING)),
                        R.array.applicationDisableScanningValues, R.array.applicationDisableScanningArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING, R.string.profile_preferences_applicationDisableMobileCellScanning, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING)),
                        R.array.applicationDisableScanningValues, R.array.applicationDisableScanningArray, context);

                summary = summary + title + ": " + value;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING, R.string.profile_preferences_applicationDisableOrientationScanning, false, context);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";

                String value = GlobalGUIRoutines.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING)),
                        R.array.applicationDisableScanningValues, R.array.applicationDisableScanningArray, context);

                summary = summary + title + ": " + value;
            }
        }

        PPApplication.logE("ProfilesPrefsFragment.setCategorySummary", "key="+key);
        PPApplication.logE("ProfilesPrefsFragment.setCategorySummary", "preferenceScreen="+preferenceScreen);
        PPApplication.logE("ProfilesPrefsFragment.setCategorySummary", "_bold="+_bold);
        GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, _bold, true, false, false, false);
        if (_bold || forceSet)
            preferenceScreen.setSummary(summary);
        else
            preferenceScreen.setSummary("");
    }

    private void setSummaryForNotificationVolume0(Context context) {
        Preference preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
        if (preference != null) {
            String notificationToneChange = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0");
            String notificationTone = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");
            String uriId = TonesHandler.getPhoneProfilesSilentUri(context, RingtoneManager.TYPE_NOTIFICATION);
            if (notificationToneChange.equals("1") && notificationTone.equals(uriId))
                preference.setSummary(R.string.profile_preferences_volumeNotificationVolume0_summaryPhoneProfilesSilentConfigured);
            else
            if (notificationToneChange.equals("1") && (notificationTone.isEmpty() ||
                                    notificationTone.equals(TonesHandler.NOTIFICATION_TONE_URI_NONE)))
                preference.setSummary(R.string.profile_preferences_volumeNotificationVolume0_summaryNoneConfigured);
            else
                preference.setSummary(R.string.profile_preferences_volumeNotificationVolume0_summaryConfigureForVolume0);
        }
    }

    private void setSummary(String key, Object value)
    {
        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        if (key.equals(Profile.PREF_PROFILE_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value.toString());
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !value.toString().isEmpty(), true, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_ICON))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                //preference.setSummary(value.toString());
                boolean valueChanged = !value.toString().equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_ICON));
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, valueChanged, true, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR)) {
            String sValue = value.toString();
            //Log.e("ProfilesPrefsFragment.setSummary","PREF_PROFILE_SHOW_IN_ACTIVATOR sValue="+sValue);
            SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                //Log.e("ProfilesPrefsFragment.setSummary","PREF_PROFILE_SHOW_IN_ACTIVATOR show="+show);
                GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, true, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE))
        {
            String sValue = value.toString();
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, true, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE))
        {
            //if (android.os.Build.VERSION.SDK_INT >= 21)
            //{
                /*final boolean canEnableZenMode =
                        (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                         (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                        );*/
            final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context, false);

            if (!canEnableZenMode)
            {
                ListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    listPreference.setEnabled(false);
                    listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                            ": "+getResources().getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings));
                    boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                    @SuppressLint("InlinedApi")
                    boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                            GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, true, false, false, addS);
                }
            }
            else
            {
                String sValue = value.toString();
                ListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    int iValue = Integer.parseInt(sValue);
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    if ((iValue != Profile.NO_CHANGE_VALUE) /*&& (iValue != Profile.SHARED_PROFILE_VALUE)*/) {
                        if (!((iValue == 6) && (android.os.Build.VERSION.SDK_INT < 23))) {
                            String[] summaryArray = getResources().getStringArray(R.array.zenModeSummaryArray);
                            summary = summary + " - " + summaryArray[iValue - 1];
                        }
                    }
                    listPreference.setSummary(summary);

                    final String sRingerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "");
                    int iRingerMode;
                    if (sRingerMode.isEmpty())
                        iRingerMode = 0;
                    else
                        iRingerMode = Integer.parseInt(sRingerMode);

                    if (iRingerMode == 5) {
                        boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                        @SuppressLint("InlinedApi")
                        boolean addS = !((android.os.Build.VERSION.SDK_INT >= 23) && (!a60) &&
                                GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, context));
                        GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, true, false, false, addS);
                    }
                    listPreference.setEnabled(iRingerMode == 5);
                }
            }
            //}
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary;
                boolean bold = false;
                if (ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) {
                    summary = getString(R.string.profile_preferences_applicationUnlinkRingerNotificationVolumes_enabled);
                    bold = true;
                }
                else
                    summary = getString(R.string.profile_preferences_applicationUnlinkRingerNotificationVolumes_disabled);

                summary = summary + "\n" + getString(R.string.phone_profiles_pref_applicationForceSetMergeRingNotificationVolumes) + ": ";
                int forceMergeValue = ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes(context);
                String[] valuesArray = getResources().getStringArray(R.array.forceSetMergeRingNotificationVolumesValues);
                String[] labelsArray = getResources().getStringArray(R.array.forceSetMergeRingNotificationVolumesArray);
                int index = 0;
                for (String _value : valuesArray) {
                    if (_value.equals(String.valueOf(forceMergeValue))) {
                        summary = summary + labelsArray[index];
                        break;
                    }
                    ++index;
                }

                if (!ApplicationPreferences.preferences.getBoolean(ActivateProfileHelper.PREF_MERGED_RING_NOTIFICATION_VOLUMES, true))
                    // detection of volumes merge = volumes are not merged
                    summary = summary + "\n\n" + getString(R.string.profile_preferences_applicationUnlinkRingerNotificationVolumes_not_merged);
                else
                    // detection of volumes merge = volumes are merged
                    summary = summary + "\n\n" + getString(R.string.profile_preferences_applicationUnlinkRingerNotificationVolumes_merged);

                preference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, true, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            String sValue = value.toString();
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, true, false, false, false);
            }
            setSummaryForNotificationVolume0(context);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ALARM))
        {
            setSummaryForNotificationVolume0(context);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_AUTOSYNC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_GPS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NFC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID))
        {
            PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(key, null, preferences, true, context);
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
            {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    preference.setEnabled(false);
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, true, false, false, false);
                }
            }
            else
            if (key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    String sValue = value.toString();
                    boolean bold = !sValue.equals(Profile.CONNECTTOSSID_JUSTANY);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, true, false, false, false);
                }
            }
            else
            {
                String sValue = value.toString();
                ListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, true, false, false, false);
                }
            }

        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_KEYGUARD))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, true, false, false, false);
                }
                else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, true, false, false, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT))
        {
            String sValue = value.toString();
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, true, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AUTOROTATE))
        {
            String sValue = value.toString();
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, true, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE) ||
                key.equals(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
                key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR) ||
                key.equals(Profile.PREF_PROFILE_LOCK_DEVICE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ON_TOUCH))
        {
            PreferenceAllowed preferenceAllowed;
            if (key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING))
                preferenceAllowed = Profile.isProfilePreferenceAllowed(key, null, preferences, true, context);
            else {
                preferenceAllowed = new PreferenceAllowed();
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
            {
                ListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    listPreference.setEnabled(false);
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, true, false, false, false);
                }
            }
            else {
                String sValue = value.toString();
                ListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, true, false, false, false);
                }
            }
        }

        if (key.equals(Profile.PREF_PROFILE_NOTIFICATION_LED))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, true, false, false, false);
                } else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, true, false, false, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS) ||
                key.equals(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, true, false, false, false);
                } else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, true, false, false, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                String defaultValue = Profile.defaultValuesString.get(key);
                //preference.setSummary(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true,
                        (!sValue.equals(defaultValue)), true,
                        false, false, false);
                preference = prefMng.findPreference(Profile.PREF_PROFILE_AFTER_DURATION_DO);
                if (preference != null) {
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true,
                            (!sValue.equals(defaultValue)), true,
                            false, false, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_AFTER_DURATION_DO))
        {
            String sValue = value.toString();
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                String durationDefaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION);
                String durationValue = preferences.getString(Profile.PREF_PROFILE_DURATION, durationDefaultValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true,
                        (durationValue != null) && (!durationValue.equals(durationDefaultValue)), true,
                        false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION))
        {
            String sValue = value.toString();
            SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, true, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND))
        {
            String sValue = value.toString();
            RingtonePreferenceX ringtonePreference = prefMng.findPreference(key);
            if (ringtonePreference != null) {
                boolean show = !sValue.isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(ringtonePreference, true, show, true, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE))
        {
            String sValue = value.toString();
            SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, true, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON) && (Build.VERSION.SDK_INT < 26))
        {
            String sValue = value.toString();
            SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, true, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_MEDIA) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ALARM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SYSTEM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_VOICE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_DTMF) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = VolumeDialogPreferenceX.changeEnabled(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, true, false, false, false);
            }
        }
        if (key.equals(PREF_VOLUME_NOTIFICATION_VOLUME0)) {
            setSummaryForNotificationVolume0(context);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = BrightnessDialogPreferenceX.changeEnabled(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, true, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, true, false, false, false);
                }
                else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, true, false, false, false);
                }
            }
        }

        if (key.equals(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0)
                    preference.setSummary(R.string.profile_preferences_deviceForceStopApplications_PPPExtender_install_summary);
                else
                if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0)
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                else
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_upgrade_summary);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE)) {
            int index;
            String sValue;

            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                boolean ok = true;
                CharSequence changeSummary = "";
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0) {
                    ok = false;
                    changeSummary = getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + getString(R.string.preference_not_allowed_reason_not_extender_installed);
                }
                else
                if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0) {
                    ok = false;
                    changeSummary = getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                }
                else
                if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context)) {
                    ok = false;
                    changeSummary = getResources().getString(R.string.profile_preferences_device_not_allowed)+
                            ": "+getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                }
                if (!ok) {
                    listPreference.setSummary(changeSummary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, true, false, false, false);
                }
                else {
                    sValue = listPreference.getValue();
                    index = listPreference.findIndexOfValue(sValue);
                    changeSummary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(changeSummary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, true, false, false, false);
                }
            }
        }

        if (key.equals(PREF_LOCK_DEVICE_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0)
                    preference.setSummary(R.string.profile_preferences_lockDevice_PPPExtender_install_summary);
                else
                if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_4_0)
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                else
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_upgrade_summary);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
            int index;
            String sValue;

            ListPreference listPreference =
                    prefMng.findPreference(Profile.PREF_PROFILE_LOCK_DEVICE);
            if (listPreference != null) {
                sValue = listPreference.getValue();
                //boolean ok = true;
                CharSequence changeSummary;// = "";

                index = listPreference.findIndexOfValue(sValue);
                changeSummary = (index >= 0) ? listPreference.getEntries()[index] : null;

                if (sValue.equals("3")) {
                    int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                    if (extenderVersion == 0) {
                        //ok = false;
                        changeSummary = changeSummary + "\n\n" +
                                getResources().getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_not_extender_installed);
                    } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_4_0) {
                        //ok = false;
                        changeSummary = changeSummary + "\n\n" +
                                getResources().getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                    } else if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context)) {
                        //ok = false;
                        changeSummary = changeSummary + "\n\n" +
                                getResources().getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                    }
                }

                listPreference.setSummary(changeSummary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, true, false, false, false);
            }
        }

    }

    private void setSummary(String key) {
        String value;
        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
                key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
                key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE) ||
                key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
        }
        else
            value = preferences.getString(key, "");
        setSummary(key, value);
    }

    private void updateAllSummary() {
        if (getActivity() == null)
            return;

        // disable depended preferences
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_RINGTONE);
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);

        //if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE)
        //{
        setSummary(Profile.PREF_PROFILE_NAME);
        setSummary(Profile.PREF_PROFILE_ICON);
        setSummary(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR);
        setSummary(Profile.PREF_PROFILE_DURATION);
        setSummary(Profile.PREF_PROFILE_AFTER_DURATION_DO);
        setSummary(Profile.PREF_PROFILE_ASK_FOR_DURATION);
        setSummary(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND);
        setSummary(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE);
        setSummary(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON);
        //}
        setSummary(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);
        setSummary(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
        setSummary(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
        setSummary(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE);
        setSummary(Profile.PREF_PROFILE_SOUND_ALARM);
        setSummary(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI);
        setSummary(Profile.PREF_PROFILE_DEVICE_BLUETOOTH);
        setSummary(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT);
        setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA);
        setSummary(Profile.PREF_PROFILE_DEVICE_GPS);
        setSummary(Profile.PREF_PROFILE_DEVICE_AUTOSYNC);
        setSummary(Profile.PREF_PROFILE_DEVICE_AUTOROTATE);
        setSummary(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS);
        setSummary(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
        setSummary(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
        setSummary(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS);
        setSummary(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE);
        setSummary(Profile.PREF_PROFILE_DEVICE_NFC);
        setSummary(Profile.PREF_PROFILE_DEVICE_KEYGUARD);
        setSummary(Profile.PREF_PROFILE_VOLUME_RINGTONE);
        setSummary(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
        setSummary(Profile.PREF_PROFILE_VOLUME_MEDIA);
        setSummary(Profile.PREF_PROFILE_VOLUME_ALARM);
        setSummary(Profile.PREF_PROFILE_VOLUME_SYSTEM);
        setSummary(Profile.PREF_PROFILE_VOLUME_VOICE);
        setSummary(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS);
        setSummary(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
        setSummary(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE);
        setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE);
        setSummary(Profile.PREF_PROFILE_NOTIFICATION_LED);
        setSummary(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
        setSummary(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
        setSummary(Profile.PREF_PROFILE_LOCK_DEVICE);
        setSummary(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING);
        setSummary(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS);
        setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS);
        setSummary(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS);
        setSummary(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE);
        setSummary(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING);
        setSummary(Profile.PREF_PROFILE_SOUND_ON_TOUCH);
        setSummary(PREF_LOCK_DEVICE_INSTALL_EXTENDER);
        setSummary(Profile.PREF_PROFILE_VOLUME_DTMF);
        setSummary(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY);
        setSummary(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO);
    }

    private boolean getEnableVolumeNotificationByRingtone(String ringtoneValue) {
        boolean enabled = Profile.getVolumeRingtoneChange(ringtoneValue);
        if (enabled) {
            int volume = Profile.getVolumeRingtoneValue(ringtoneValue);
            return volume > 0;
        }
        else
            return true;
    }

    private boolean getEnableVolumeNotificationVolume0(boolean notificationEnabled, String notificationValue, Context context) {
        return  notificationEnabled && ActivateProfileHelper.getMergedRingNotificationVolumes(context) &&
                ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context) &&
                Profile.getVolumeRingtoneChange(notificationValue) && (Profile.getVolumeRingtoneValue(notificationValue) == 0);
    }

    private void disableDependedPref(String key, Object value)
    {
        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        String sValue = value.toString();

        final String ON = "1";

        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE)) {
            boolean enabled = getEnableVolumeNotificationByRingtone(sValue);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
            String notificationValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, "");
            enabled = getEnableVolumeNotificationVolume0(enabled, notificationValue, context);
            preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION)) {
            String ringtoneValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
            boolean enabled = (!ActivateProfileHelper.getMergedRingNotificationVolumes(context) || ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context)) &&
                    getEnableVolumeNotificationByRingtone(ringtoneValue);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
            enabled = getEnableVolumeNotificationVolume0(enabled, sValue, context);
            preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_ALARM);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
            if (preference != null)
                preference.setEnabled(enabled);
            preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP))
        {
            if (Profile.isProfilePreferenceAllowed(key, null, preferences, true, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                boolean enabled = !sValue.equals(ON);
                ListPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WIFI);
                if (preference != null) {
                    if (!enabled)
                        preference.setValue(Profile.NO_CHANGE_VALUE_STR);
                    preference.setEnabled(enabled);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE)) {
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
            String ringerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "0");
            String zenMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "0");
            boolean enabled = false;
            // also look at Profile.mergeProfiles()
            if (Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, null, preferences, true, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (ringerMode.equals("1") || ringerMode.equals("4"))
                    enabled = true;
                if (ringerMode.equals("5")) {
                    if (zenMode.equals("1") || zenMode.equals("2"))
                        enabled = true;
                }
            }
            ListPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null) {
                if (!enabled)
                    preference.setValue(Profile.NO_CHANGE_VALUE_STR);
                preference.setEnabled(enabled);
            }
            //}
        }

        if (key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE)) {
            setSummary(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
            boolean ok = PPPExtenderBroadcastReceiver.isEnabled(context, PPApplication.VERSION_CODE_EXTENDER_3_0);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            if (preference != null) {
                preference.setEnabled(ok);
                setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            }
            ApplicationsMultiSelectDialogPreferenceX appPreference =
                    prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
            if (appPreference != null) {
                appPreference.setEnabled(ok && (!(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR))));
                appPreference.setSummaryAMSDP();
            }
        }

        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
            setSummary(PREF_LOCK_DEVICE_INSTALL_EXTENDER);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_LOCK_DEVICE);
            if (preference != null) {
                setSummary(Profile.PREF_PROFILE_LOCK_DEVICE);
            }
        }
    }

    private void disableDependedPref(String key) {
        String value;
        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
        }
        else
            value = preferences.getString(key, "");
        disableDependedPref(key, value);
    }

    void setPermissionsPreference() {
        if (nestedFragment)
            return;

        if (getActivity() == null)
            return;

        final ProfilesPrefsActivity activity = (ProfilesPrefsActivity)getActivity();

        Context context = activity.getApplicationContext();

        long profile_id = activity.profile_id;
        if (profile_id != 0) {
            int newProfileMode = activity.newProfileMode;
            int predefinedProfileIndex = activity.predefinedProfileIndex;
            final Profile profile = ((ProfilesPrefsActivity) getActivity())
                    .getProfileFromPreferences(profile_id, newProfileMode, predefinedProfileIndex);

            // not some permissions
            if (Permissions.checkProfilePermissions(context, profile).size() == 0) {
                Preference preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = prefMng.findPreference("rootScreen");
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                Preference preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null) {
                        preference = new Preference(context);
                        preference.setKey(PRF_GRANT_PERMISSIONS);
                        preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                        preference.setIconSpaceReserved(false);
                        preference.setOrder(-100);
                        preferenceCategory.addPreference(preference);
                    }
                }
                if (preference != null) {
                    Spannable title = new SpannableString(getString(R.string.preferences_grantPermissions_title));
                    title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                    preference.setTitle(title);
                    Spannable summary = new SpannableString(getString(R.string.preferences_grantPermissions_summary));
                    summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                    preference.setSummary(summary);

                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            //Profile mappedProfile = Profile.getMappedProfile(profile, appContext);
                            Permissions.grantProfilePermissions(activity, profile, false, false,
                                    /*true, false, 0,*/ PPApplication.STARTUP_SOURCE_EDITOR, false, false, true);
                            return false;
                        }
                    });
                }
            }

            // not enabled grant root
            if (Profile.isProfilePreferenceAllowed("-", profile, null, true, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                Preference preference = prefMng.findPreference(PRF_GRANT_ROOT);
                if (preference != null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }
            else {
                Preference preference = prefMng.findPreference(PRF_GRANT_ROOT);
                if (preference == null) {
                    PreferenceScreen preferenceCategory = findPreference("rootScreen");
                    if (preferenceCategory != null) {
                        preference = new Preference(context);
                        preference.setKey(PRF_GRANT_ROOT);
                        preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                        preference.setIconSpaceReserved(false);
                        preference.setOrder(-100);
                        preferenceCategory.addPreference(preference);
                    }
                }
                if (preference != null) {
                    Spannable title = new SpannableString(getString(R.string.preferences_grantRoot_title));
                    title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                    preference.setTitle(title);
                    Spannable summary = new SpannableString(getString(R.string.preferences_grantRoot_summary));
                    summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                    preference.setSummary(summary);

                    final ProfilesPrefsFragment fragment = this;
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Permissions.grantRootX(fragment, activity);
                            return false;
                        }
                    });
                }
            }
        }
        else {
            Preference preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("rootScreen");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_GRANT_ROOT);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("rootScreen");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
    }

}
