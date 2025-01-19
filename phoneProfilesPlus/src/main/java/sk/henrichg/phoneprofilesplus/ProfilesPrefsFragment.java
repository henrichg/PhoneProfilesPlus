package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProfilesPrefsFragment extends PreferenceFragmentCompat
                                    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceManager prefMng;
    private SharedPreferences preferences;

    private boolean nestedFragment = false;

    private SetRingtonePreferenceSummaryAsyncTask setRingtonePreferenceSummaryAsyncTask = null;
    private SetProfileSoundsPreferenceSummaryAsyncTask setProfileSoundsPreferenceSummaryAsyncTask = null;
    private SetProfileSoundsDualSIMPreferenceSummaryAsyncTask setProfileSoundsDualSIMPreferenceSummaryAsyncTask = null;
    private SetRedTextToPreferencesAsyncTask setRedTextToPreferencesAsyncTask = null;

    //private static final String PREF_NOTIFICATION_ACCESS = "prf_pref_volumeNotificationsAccessSettings";
    private static final int RESULT_NOTIFICATION_ACCESS_SETTINGS = 2980;
    private static final int RESULT_UNLINK_VOLUMES_APP_PREFERENCES = 2981;
    private static final int RESULT_ACCESSIBILITY_SETTINGS = 2983;
    private static final int RESULT_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON_SETTINGS = 2984;
    private static final int RESULT_ASSISTANT_SETTINGS = 2985;
    private static final int RESULT_NOTIFICATION_ACCESS_SYSTEM_SETTINGS = 2986;

    private static final String PREF_VOLUME_NOTIFICATION_VOLUME0 = "prf_pref_volumeNotificationVolume0";

    private static final String PREF_GRANT_PERMISSIONS = "prf_pref_grantPermissions";
    private static final String PREF_GRANT_ROOT = "prf_pref_grantRoot";
    private static final String PREF_GRANT_G1_PREFERENCES = "prf_pref_grantG1Permissions";
    private static final String PREF_NOT_ENABLED_ACCESSIBILITY_SERVICE = "prf_pref_notEnabledAccessibilityService";
    private static final String PREF_NOT_INSTALLED_PPPPS = "prf_pref_notInstammedPPPPS";
    private static final String PREF_GRANT_SHIZUKU_PREFERENCES = "prf_pref_grantShizukuPermissions";

    private static final String PREF_FORCE_STOP_APPLICATIONS_CATEGORY_ROOT = "prf_pref_forceStopApplicationsCategoryRoot";
    private static final String PREF_FORCE_STOP_APPLICATIONS_EXTENDER = "prf_pref_deviceForceStopApplicationExtender";
    //private static final String PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER = "prf_pref_deviceForceStopApplicationInstallExtender";
    //private static final String PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS = "prf_pref_deviceForceStopApplicationAccessibilitySettings";
    //private static final String PREF_INSTALL_SILENT_TONE = "prf_pref_soundInstallSilentTone";
    private static final String PREF_LOCK_DEVICE_CATEGORY_ROOT = "prf_pref_lockDeviceCategoryRoot";
    private static final String PREF_LOCK_DEVICE_EXTENDER = "prf_pref_lockDeviceExtender";
    //private static final String PREF_LOCK_DEVICE_INSTALL_EXTENDER = "prf_pref_lockDeviceInstallExtender";
    //private static final String PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS = "prf_pref_lockDeviceAccessibilitySettings";
    //private static final String PREF_FORCE_STOP_APPLICATIONS_LAUNCH_EXTENDER = "prf_pref_deviceForceStopApplicationLaunchExtender";
    //private static final String PREF_LOCK_DEVICE_LAUNCH_EXTENDER = "prf_pref_lockDeviceLaunchExtender";
    private static final String PREF_NOTIFICATION_ACCESS_ENABLED = "prf_pref_notificationAccessEnable";
    private static final String PREF_NOTIFICATION_LED_INFO = "prf_pref_notificationLedInfo";
    private static final String PREF_ALWAYS_ON_DISPLAY_INFO = "prf_pref_alwaysOnDisplayInfo";
    private static final String PREF_PROFILE_DEVICE_RADIOS_DUAL_SIM_SUPPORT_CATEGORY_ROOT = "prf_pref_deviceRadiosDualSIMSupportCategoryRoot";
    private static final String PREF_PROFILE_SOUNDS_DUAL_SIM_SUPPORT_CATEGORY_ROOT = "prf_pref_soundsDualSIMSupportCategoryRoot";
    private static final String PREF_DEVICE_WALLPAPER_CATEGORY_ROOT = "prf_pref_deviceWallpaperCategoryRoot";
    private static final String PREF_PROFILE_DEVICE_RUN_APPLICATION_MIUI_PERMISSIONS = "prf_pref_deviceRunApplicationMIUIPermissions";
    private static final String PREF_PROFILE_DEVICE_BRIGHTNESS_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON = "prf_pref_deviceBrightness_forceSetBrightnessAtScreenOn";
    private static final String PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS = "prf_pref_deviceAirplaneMode_assistantSettings";
    private static final String PREF_SCREEN_DARK_MODE_INFO = "prf_pref_screenDarkModeInfo";
    private static final String PREF_PROFILE_AIRPLANE_MODE_RADIOS_INFO = "prf_pref_deviceAirplaneModeRadiosInfo";
    private static final String PREF_PROFILE_APPLICATION_WIFI_SCAN_INTERVAL_INFO = "prf_pref_applicationWifiScanIntervalInfo";
    private static final String PREF_PROFILE_APPLICATION_BLUETOOTH_SCAN_INTERVAL_INFO = "prf_pref_applicationBluetoothScanIntervalInfo";
    private static final String PREF_PROFILE_APPLICATION_LOCATION_UPDATE_INTERVAL = "prf_pref_applicationLocationUpdateIntervalInfo";
    private static final String PREF_PROFILE_APPLICATION_MOBILE_CELLS_SCAN_INTERVAL_INFO = "prf_pref_applicationMobileCellScanIntervalInfo";
    private static final String PREF_PROFILE_APPLICATION_ORIENTATION_SCAN_INTERVAL_INFO = "prf_pref_applicationOrientationScanIntervalInfo";
    private static final String PREF_PROFILE_APPLICATION_NOTIFICATION_SCAN_INTERVAL_INFO = "prf_pref_applicationNotificationScanIntervalInfo";
    private static final String PREF_PROFILE_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL_INFO = "prf_pref_applicationPeriodicScanningScanIntervalInfo";
    private static final String PREF_PROFILE_DEVICE_AIRPLANE_MODE_CATEGORY_ROOT = "prf_pref_deviceRadiosAirplaneModeCategoryRoot";
    private static final String PREF_NOTIFICATION_ACCESS_SYSTEM_SETTINGS = "prf_pref_clearNotificationNotificationsAccessSettings";
    private static final String PREF_NOTIFICATION_ACCESS_RESTRICTED_SETTINGS = "prf_pref_clearNotificationNotificationsAccessSettingsRestrictedSettings";

    private static final String PREF_PROFILE_VOLUME_ZEN_MODE_INFO = "prf_pref_volumeZenModeInfo";
    private static final String PREF_PROFILE_VOLUME_SOUND_MODE_INFO = "prf_pref_volumeSoundMode_info";
    private static final String PREF_PROFILE_VOLUME_RINGTONE0_INFO = "prf_pref_volumeRingtone0Info";
    private static final String PREF_PROFILE_VOLUME_IGNORE_SOUND_MODE_INFO2 = "prf_pref_volumeIgnoreSoundModeInfo2";
    private static final String PREF_PROFILE_DEVICE_WALLPAPER_FOLDER_INFO = "prf_pref_deviceWallpaperFolderInfo";
    private static final String PREF_PROFILE_PREFERENCE_TYPES_INFO = "prf_pref_preferenceTypesInfo";
    private static final String PREF_PROFILE_DEVICE_WIFI_AP_INFO = "prf_pref_deviceWiFiAPInfo";
    private static final String PREF_PROFILE_DEVICE_COSE_ALL_APPLICATIONS_INFO = "prf_pref_deviceCloseAllApplicationsInfo";
    private static final String PREF_PROFILE_VOLUME_SOUND_MODE_VIBRATION_INFO = "prf_pref_volumeSoundModeVibrationInfo";
    private static final String PREF_PROFILE_DEVICE_SCREEN_TIMEOUT_AND_KEEP_SCREEN_ON_INFO = "prf_pref_deviceScreenTimeoutAndKeeepScreenOnInfo";
    private static final String PREF_PROFILE_VOLUME_MEDIA_ONEPLUS_INFO = "prf_pref_volumeMediaOnePlusInfo";
    private static final String PREF_PROFILE_SOUND_PROFILE_PPPPS = "prf_pref_soundProfilePPPPS";
    private static final String PREF_PROFILE_SOUND_PROFILE_CATTEGORY = "prf_pref_soundProfileCategory";
    private static final String PREF_PROFILE_OTHERS_CATTEGORY = "prf_pref_othersCategory";
    private static final String PREF_PROFILE_SCREEN_CATTEGORY = "prf_pref_screenCategory";
    private static final String PREF_PROFILE_VOLUME_TYPE_CATTEGORY = "prf_pref_volumeTypeCategory";
    private static final String PREF_PROFILE_DEVICE_WALLPAPER_CATTEGORY = "prf_pref_deviceWallpaperCategory";
    private static final String PREF_PROFILE_DEVICE_WALLPAPER_HUAWEI_INFO = "prf_pref_deviceWallpaperHuaweiInfo";
    private static final String PREF_PROFILE_DEVICE_KEYGUARD_INFO = "prf_pref_deviceKeyguardInfo";
    private static final String PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_INFO = "prf_pref_forceStopApplicationsInfo";

    private static final String PREF_PROFILE_ACTIVATION_DURATION_CATTEGORY_ROOT = "prf_pref_activationDurationCategoryRoot";
    private static final String PREF_PROFILE_SOUND_PROFILE_CATTEGORY_ROOT = "prf_pref_soundProfileCategoryRoot";
    private static final String PREF_PROFILE_VOLUME_CATTEGORY_ROOT = "prf_pref_volumeCategoryRoot";
    private static final String PREF_PROFILE_SOUNDS_CATTEGORY_ROOT = "prf_pref_soundsCategoryRoot";
    private static final String PREF_PROFILE_TOUCH_EFFECTS_CATTEGORY_ROOT = "prf_pref_touchEffectsCategoryRoot";
    private static final String PREF_PROFILE_VIBRATION_INTENSITY_CATTEGORY_ROOT = "prf_pref_vibrationIntensityCategoryRoot";
    private static final String PREF_PROFILE_RADIOS_CATTEGORY_ROOT = "prf_pref_radiosCategoryRoot";
    private static final String PREF_PROFILE_SCREEN_CATTEGORY_ROOT = "prf_pref_screenCategoryRoot";
    private static final String PREF_PROFILE_LED_ACCESSORIES_CATTEGORY_ROOT = "prf_pref_ledAccessoriesCategoryRoot";
    private static final String PREF_PROFILE_OTHERS_CATTEGORY_ROOT = "prf_pref_othersCategoryRoot";
    private static final String PREF_PROFILE_APPLICATION_CATTEGORY_ROOT = "prf_pref_applicationCategoryRoot";
    private static final String PREF_PROFILE_SEND_SMS_CATTEGORY_ROOT = "prf_pref_sendSMSCategoryRoot";
    private static final String PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT = "prf_pref_NotificationsCategoryRoot";
    private static final String PREF_PROFILE_CLEAR_NOTIFICATIONS_CATTEGORY_ROOT = "prf_pref_clearNotificationsCategoryRoot";

    private static final String TAG_RINGTONE_NAME = "<ringtone_name>";
    private static final String TAG_NOTIFICATION_NAME = "<notification_name>";
    private static final String TAG_ALARM_NAME = "<alarm_name>";
    private static final String TAG_RINGTONE_NAME_SIM1 = "<ringtone_name_sim1>";
    private static final String TAG_RINGTONE_NAME_SIM2 = "<ringtone_name_sim2>";
    private static final String TAG_NOTIFICATION_NAME_SIM1 = "<notification_name_sim1>";
    private static final String TAG_NOTIFICATION_NAME_SIM2 = "<notification_name_sim2>";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // is required for to not call onCreate and onDestroy on orientation change
        //noinspection deprecation
        setRetainInstance(true);

        nestedFragment = !(this instanceof ProfilesPrefsRoot);

        initPreferenceFragment(/*savedInstanceState*/);
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

        // must be set only when state == null, because without this, generated is exception on orientation change:
        // java.lang.NullPointerException: Attempt to invoke virtual method 'android.widget.ScrollBarDrawable
        // android.widget.ScrollBarDrawable.mutate()' on a null object reference
        if (state == null)
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
        if (preference instanceof TimeDialogPreference)
        {
            ((TimeDialogPreference)preference).fragment = new TimeDialogPreferenceFragment();
            dialogFragment = ((TimeDialogPreference)preference).fragment;
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
        if (preference instanceof InfoDialogPreference)
        {
            ((InfoDialogPreference)preference).fragment = new InfoDialogPreferenceFragment();
            dialogFragment = ((InfoDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ProfileIconPreference)
        {
            ((ProfileIconPreference)preference).fragment = new ProfileIconPreferenceFragment();
            dialogFragment = ((ProfileIconPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof VolumeDialogPreference)
        {
            ((VolumeDialogPreference)preference).fragment = new VolumeDialogPreferenceFragment();
            dialogFragment = ((VolumeDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof NotificationVolume0DialogPreference)
        {
            ((NotificationVolume0DialogPreference)preference).fragment = new NotificationVolume0DialogPreferenceFragment();
            dialogFragment = ((NotificationVolume0DialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ConnectToSSIDDialogPreference)
        {
            ((ConnectToSSIDDialogPreference)preference).fragment = new ConnectToSSIDDialogPreferenceFragment();
            dialogFragment = ((ConnectToSSIDDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof BrightnessDialogPreference)
        {
            ((BrightnessDialogPreference)preference).fragment = new BrightnessDialogPreferenceFragment();
            dialogFragment = ((BrightnessDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof RunApplicationsDialogPreference)
        {
            ((RunApplicationsDialogPreference)preference).fragment = new RunApplicationsDialogPreferenceFragment();
            dialogFragment = ((RunApplicationsDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ApplicationsMultiSelectDialogPreference)
        {
            ((ApplicationsMultiSelectDialogPreference)preference).fragment = new ApplicationsMultiSelectDialogPreferenceFragment();
            dialogFragment = ((ApplicationsMultiSelectDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ProfilePreference) {
            ((ProfilePreference) preference).fragment = new ProfilePreferenceFragment();
            dialogFragment = ((ProfilePreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof GenerateNotificationDialogPreference)
        {
            ((GenerateNotificationDialogPreference)preference).fragment = new GenerateNotificationDialogPreferenceFragment();
            dialogFragment = ((GenerateNotificationDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ConfiguredProfilePreferencesDialogPreference)
        {
            ((ConfiguredProfilePreferencesDialogPreference)preference).fragment = new ConfiguredProfilePreferencesDialogPreferenceFragment();
            if (getActivity() != null)
                ((ConfiguredProfilePreferencesDialogPreference)preference).profile_id = ((ProfilesPrefsActivity)getActivity()).profile_id;
            else
                ((ConfiguredProfilePreferencesDialogPreference)preference).profile_id = 0;
            dialogFragment = ((ConfiguredProfilePreferencesDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof DefaultSIMDialogPreference)
        {
            ((DefaultSIMDialogPreference)preference).fragment = new DefaultSIMDialogPreferenceFragment();
            dialogFragment = ((DefaultSIMDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof LiveWallpapersDialogPreference)
        {
            ((LiveWallpapersDialogPreference)preference).fragment = new LiveWallpapersDialogPreferenceFragment();
            dialogFragment = ((LiveWallpapersDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof VPNDialogPreference)
        {
            ((VPNDialogPreference)preference).fragment = new VPNDialogPreferenceFragment();
            dialogFragment = ((VPNDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof VibrationIntensityPreference)
        {
            ((VibrationIntensityPreference)preference).fragment = new VibrationIntensityPreferenceFragment();
            dialogFragment = ((VibrationIntensityPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof PPPPSDialogPreference)
        {
            ((PPPPSDialogPreference)preference).fragment = new PPPPSDialogPreferenceFragment();
            dialogFragment = ((PPPPSDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ExtenderDialogPreference)
        {
            ((ExtenderDialogPreference)preference).fragment = new ExtenderDialogPreferenceFragment();
            dialogFragment = ((ExtenderDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof BetterNumberPickerPreference) {
            ((BetterNumberPickerPreference) preference).fragment = new BetterNumberPickerPreferenceFragment();
            dialogFragment = ((BetterNumberPickerPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ContactGroupsMultiSelectDialogPreference) {
            ((ContactGroupsMultiSelectDialogPreference) preference).fragment = new ContactGroupsMultiSelectDialogPreferenceFragment();
            dialogFragment = ((ContactGroupsMultiSelectDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ContactsMultiSelectDialogPreference) {
            ((ContactsMultiSelectDialogPreference) preference).fragment = new ContactsMultiSelectDialogPreferenceFragment();
            dialogFragment = ((ContactsMultiSelectDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if ((Build.VERSION.SDK_INT >= 29) && preference instanceof SendSMSDialogPreference)
        {
            ((SendSMSDialogPreference) preference).fragment = new SendSMSDialogPreferenceFragment();
            dialogFragment = ((SendSMSDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof SearchStringPreference) {
            ((SearchStringPreference) preference).fragment = new SearchStringPreferenceFragment();
            dialogFragment = ((SearchStringPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString(PPApplication.BUNDLE_KEY, preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof PPEditTextDialogPreference)
        {
            ((PPEditTextDialogPreference)preference).fragment = new PPEditTextDialogPreferenceFragment();
            dialogFragment = ((PPEditTextDialogPreference)preference).fragment;
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
                dialogFragment.show(fragmentManager, PPApplication.PACKAGE_NAME + ".ProfilesPrefsActivity.DIALOG");
                //}
            }
        }
        else
        {
            super.onDisplayPreferenceDialog(preference);
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() == null)
            return;

        ProfilesPrefsActivity activity = (ProfilesPrefsActivity) getActivity();

        // must be used handler for rewrite toolbar title/subtitle
        final Handler handler = new Handler(activity.getMainLooper());
        final WeakReference<ProfilesPrefsActivity> activityWeakRef
                = new WeakReference<>(activity);
        handler.postDelayed(() -> {
//                PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ProfilesPrefsFragment.onActivityCreated");
            ProfilesPrefsActivity __activity = activityWeakRef.get();
            if ((__activity == null) || __activity.isFinishing() || __activity.isDestroyed())
                return;

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(__activity.getApplicationContext());
            final String profileName = preferences.getString(Profile.PREF_PROFILE_NAME, "");
            Toolbar toolbar = __activity.findViewById(R.id.activity_preferences_toolbar);
            //noinspection DataFlowIssue
            toolbar.setSubtitle(__activity.getString(R.string.title_activity_profile_preferences));
            toolbar.setTitle(__activity.getString(R.string.profile_string_0) + StringConstants.STR_COLON_WITH_SPACE + profileName);
        }, 200);

        final Context context = activity.getBaseContext();
        final ProfilesPrefsFragment fragment = this;
        final TextView preferenceSubTitle = activity.findViewById(R.id.activity_preferences_subtitle);

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
        } else {
            //noinspection DataFlowIssue
            preferenceSubTitle.setVisibility(View.GONE);
        }

        //ProfilesPrefsActivity activity = (ProfilesPrefsActivity) getActivity();
        //activity.progressLinearLayout.setVisibility(View.GONE);
        activity.settingsLinearLayout.setVisibility(View.VISIBLE);

        setDivider(null); // this remove dividers for categories

        /*
        if (savedInstanceState != null) {
            //startupSource = savedInstanceState.getInt("startupSource", PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
        }
        */

        //updateAllSummary();

        //setRedTextToPreferences();

        PPListPreference ringerModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);
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
        //final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext());

        /*ListPreference zenModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
        if (zenModePreference != null) {
            String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "");
            zenModePreference.setEnabled((value.equals("5")) && canEnableZenMode);
        }*/

        if (ringerModePreference != null) {
            CharSequence[] entries;
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if ((vibrator != null) && vibrator.hasVibrator()) {
                entries = ringerModePreference.getEntries();
                if (!entries[1].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_Off)))
                    entries[1] = entries[1] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                if (!entries[2].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_Off)))
                    entries[2] = entries[2] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                        (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                        (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                        PPApplication.deviceIsRealme) {
                    if (!entries[3].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_Off)))
                        entries[3] = entries[3] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                }
                else {
                    if (!entries[3].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_On)))
                        entries[3] = entries[3] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_On) + ")";
                }
            }
            else {
                ringerModePreference.setEntries(R.array.soundModeNotVibratorArray);
                ringerModePreference.setEntryValues(R.array.soundModeNotVibratorValues);
                entries = ringerModePreference.getEntries();
                if (!entries[1].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_Off)))
                    entries[1] = entries[1] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                        (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                        (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                        PPApplication.deviceIsRealme) {
                    if (!entries[2].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_Off)))
                        entries[2] = entries[2] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                }
                else {
                    if (!entries[2].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_On)))
                        entries[2] = entries[2] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_On) + ")";
                }
            }
            ringerModePreference.setEntries(entries);
            setSummary(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);

            PPListPreference zenModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
            if (zenModePreference != null) {
                if (!((vibrator != null) && vibrator.hasVibrator())) {
                    zenModePreference.setEntries(R.array.zenModeNotVibratorArray);
                    zenModePreference.setEntryValues(R.array.zenModeNotVibratorValues);
                }
            }

            ringerModePreference.setOnPreferenceChangeListener((preference, newValue) -> {
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
                final boolean canEnableZenMode1 = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext());

                PPListPreference _zenModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
                if (_zenModePreference != null) {
                    _zenModePreference.setEnabled((iNewValue == 5) && canEnableZenMode1);

                    GlobalGUIRoutines.setPreferenceTitleStyleX(_zenModePreference, true, false, false, false, false, false);

                    Preference zenModePreferenceInfo = prefMng.findPreference(PREF_PROFILE_VOLUME_ZEN_MODE_INFO);
                    if (zenModePreferenceInfo != null) {
                        zenModePreferenceInfo.setEnabled(_zenModePreference.isEnabled());
                    }
                }

                return true;
            });
        }
        /*if (Build.VERSION.SDK_INT != 23) {
            Preference preference = prefMng.findPreference("prf_pref_volumeVibrateWhenRingingRootInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_PROFILE_SOUND_PROFILE_CATTEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }*/
        if ((PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) || PPApplication.deviceIsOnePlus) {
            PPListPreference listPreference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (listPreference != null)
            {
                listPreference.setTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT+getString(R.string.profile_preferences_vibrateWhenRinging));
                listPreference.setDialogTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT+getString(R.string.profile_preferences_vibrateWhenRinging));
                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, "");
                setSummary(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, value);
            }
        } else {
            PPPPSDialogPreference ppppsPreference = prefMng.findPreference(PREF_PROFILE_SOUND_PROFILE_PPPPS);
            if (ppppsPreference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_PROFILE_SOUND_PROFILE_CATTEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(ppppsPreference);
            }
        }

        if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, null, preferences, true, context);
            PPListPreference vibrateNotificationsPreference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS);
            if ((preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                    ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED)||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))) {
                if (vibrateNotificationsPreference != null) {
                    vibrateNotificationsPreference.setTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrateNotifications));
                    vibrateNotificationsPreference.setDialogTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrateNotifications));
                    String value = preferences.getString(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, "");
                    setSummary(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, value);
                }
            }
            else
            if (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM) {
                if (vibrateNotificationsPreference != null) {
                    PreferenceScreen preferenceCategory = findPreference(PREF_PROFILE_SOUND_PROFILE_CATTEGORY);
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(vibrateNotificationsPreference);
                }
            }
        }

        PreferenceAllowed _preferenceAllowed = new PreferenceAllowed();
        _preferenceAllowed.isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY( context);
        if (_preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            VibrationIntensityPreference vibrationIntensityPreference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING);
            if (vibrationIntensityPreference != null) {
                vibrationIntensityPreference.setTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrationIntensityRinging));
                vibrationIntensityPreference.setDialogTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrationIntensityRinging));
                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING, "-1|1");
                setSummary(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING, value);
            }
            vibrationIntensityPreference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS);
            if (vibrationIntensityPreference != null) {
                vibrationIntensityPreference.setTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrationIntensityNotificatiions));
                vibrationIntensityPreference.setDialogTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrationIntensityNotificatiions));
                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, "-1|1");
                setSummary(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, value);
            }
            vibrationIntensityPreference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION);
            if (vibrationIntensityPreference != null) {
                vibrationIntensityPreference.setTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrationIntensityTouchInteraction));
                vibrationIntensityPreference.setDialogTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrationIntensityTouchInteraction));
                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, "-1|1");
                setSummary(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, value);
            }
        }

        /*
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
            if (preference != null)
            {
                preference.setTitle("(R) "+getString(R.string.profile_preferences_deviceWiFiAP));
                String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP, "");
                setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP, value);
            }
        */
        if (PPApplication.HAS_FEATURE_TELEPHONY)
        {
            fillDeviceNetworkTypePreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, context);

                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        fillDeviceNetworkTypePreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, context);
                        fillDeviceNetworkTypePreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, context);
                    }
                }
        }

        DurationDialogPreference durationPreference = prefMng.findPreference(Profile.PREF_PROFILE_DURATION);
        if (durationPreference != null)
        {
            durationPreference.setTitle(context.getString(R.string.profile_preferences_duration));
            durationPreference.setDialogTitle(context.getString(R.string.profile_preferences_duration));
            String value = preferences.getString(Profile.PREF_PROFILE_DURATION, "");
            setSummary(Profile.PREF_PROFILE_DURATION, value);
        }

        //Preference preference;

        Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_ASK_FOR_DURATION);
        if (preference != null) {
            preference.setTitle(StringConstants.STR_MANUAL_SPACE + getString(R.string.profile_preferences_askForDuration));
        }

        preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);
        if (preference != null) {
            preference.setOnPreferenceClickListener(preference1 -> {
                // start preferences activity for default profile
                //if (activity != null) {
                    Intent intent = new Intent(activity.getBaseContext(), PhoneProfilesPrefsActivity.class);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_SYSTEM_CATEGORY_ROOT);
                    //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    activity.startActivityForResult(intent, RESULT_UNLINK_VOLUMES_APP_PREFERENCES);
                //}
                return false;
            });
        }

        InfoDialogPreference infoDialogPreference = prefMng.findPreference(PREF_PROFILE_PREFERENCE_TYPES_INFO);
        if (infoDialogPreference != null) {
            String grantRootURL = "";
            if (ApplicationPreferences.applicationNeverAskForGrantRoot && RootUtils.isRooted()) {
                grantRootURL = StringConstants.TAG_BREAK_HTML;
                // <a href>
                grantRootURL = grantRootURL +
                        StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.GRANT_ROOT +
                        StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                        getString(R.string.profile_preferences_types_R_grant_info) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;
            }
            infoDialogPreference.setInfoText(
                // <ul><li>
                StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +
                getString(R.string.important_info_profile_install_pppps) +
                StringConstants.TAG_BREAK_HTML +
                // <a href>
                StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.ACTIVITY_IMPORTANT_INFO_PROFILES + "__" +
                R.id.activity_info_notification_profile_pppps_howTo_1 + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                getString(R.string.profile_preferences_types_G1_show_info) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML +
                //</li>
                StringConstants.TAG_LIST_ITEM_END_HTML +
                //<li>
                StringConstants.TAG_LIST_ITEM_START_HTML +
                getString(R.string.important_info_profile_grant) +
                StringConstants.TAG_BREAK_HTML +
                // <a href>
                StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.ACTIVITY_IMPORTANT_INFO_PROFILES + "__" +
                R.id.activity_info_notification_profile_grant_1_howTo_1 + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                getString(R.string.profile_preferences_types_G1_show_info) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML +
                //</li>
                StringConstants.TAG_LIST_ITEM_END_HTML +
                //<li>
                StringConstants.TAG_LIST_ITEM_START_HTML +
                getString(R.string.important_info_profile_root) +
                grantRootURL + StringConstants.TAG_DOUBLE_BREAK_HTML +
                //</li>
                StringConstants.TAG_LIST_ITEM_END_HTML +

                //<li>
                StringConstants.TAG_LIST_ITEM_START_HTML +
                getString(R.string.phone_profiles_pref_grantShizukuPermission_summary1) +
                StringConstants.TAG_BREAK_HTML +
                // <a href>
                StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.ACTIVITY_IMPORTANT_INFO_PROFILES + "__" +
                R.id.activity_info_notification_profile_shizuku_howTo_1 + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
                getString(R.string.profile_preferences_types_shizuku_show_info1) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML +
                //</li>
                StringConstants.TAG_LIST_ITEM_END_HTML +

                //<li>
                StringConstants.TAG_LIST_ITEM_START_HTML +
                getString(R.string.important_info_profile_interactive) +
                //</li></ul>
                StringConstants.TAG_LIST_END_LAST_ITEM_HTML
            );
            infoDialogPreference.setIsHtml(true);
        }

        Preference showInActivatorPreference = prefMng.findPreference(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR);
        if (showInActivatorPreference != null) {
            showInActivatorPreference.setTitle(/*"[A] " + */getString(R.string.profile_preferences_showInActivator));
            boolean value = preferences.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, false);
            setSummary(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, value);
        }

        /*
        Preference extenderPreference = prefMng.findPreference(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(preference12 -> {
                ExtenderDialogPreferenceFragment.installPPPExtender(activity, null);
                return false;
            });
        }
        */
        /*
        Preference accessibilityPreference = prefMng.findPreference(PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(preference13 -> {
                enableExtender();
                return false;
            });
        }
        */
        /*
        boolean toneInstalled = TonesHandler.isToneInstalled(TonesHandler.TONE_ID, activity.getApplicationContext());
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
                                    context.getString(R.string.profile_preferences_installSilentTone_installed_summary),
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

        /*
        extenderPreference = prefMng.findPreference(PREF_LOCK_DEVICE_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(preference14 -> {
                ExtenderDialogPreferenceFragment.installPPPExtender(activity, null);
                return false;
            });
        }
        */
        /*
        accessibilityPreference = prefMng.findPreference(PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(preference15 -> {
                enableExtender();
                return false;
            });
        }
        */
        /*
        accessibilityPreference = prefMng.findPreference(PREF_FORCE_STOP_APPLICATIONS_LAUNCH_EXTENDER);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(preference16 -> {
                if (PPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
                    PackageManager packageManager = context.getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_EXTENDER);
                    if (intent != null) {
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }
                else {
                    //if (getActivity() != null) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                getString(R.string.event_preferences_applications_LaunchExtender_title),
                                getString(R.string.event_preferences_extender_not_installed),
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
                    //}
                }
                return false;
            });
        }
        accessibilityPreference = prefMng.findPreference(PREF_LOCK_DEVICE_LAUNCH_EXTENDER);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(preference17 -> {
                if (PPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
                    PackageManager packageManager = context.getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_EXTENDER);
                    if (intent != null) {
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }
                else {
                    //if (getActivity() != null) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                getString(R.string.event_preferences_applications_LaunchExtender_title),
                                getString(R.string.event_preferences_extender_not_installed),
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
                    //}
                }
                return false;
            });
        }
        */

        if (Build.VERSION.SDK_INT >= 29) {
            //if (Build.VERSION.SDK_INT < 30) {
                preference = findPreference(PREF_PROFILE_DEVICE_WIFI_AP_INFO);
                if (preference != null) {
                    preference.setSummary(getString(R.string.profile_preferences_deviceWiFiAPInfo_summary) +
                            StringConstants.CHAR_NEW_LINE + getString(R.string.profile_preferences_deviceWiFiAPInfo2_summary) +
                            StringConstants.CHAR_NEW_LINE + getString(R.string.profile_preferences_deviceWiFiAPInfo_2_summary));
                }
            //}
            preference = findPreference(PREF_PROFILE_DEVICE_COSE_ALL_APPLICATIONS_INFO);
            if (preference != null) {
                preference.setSummary(getString(R.string.profile_preferences_deviceCloseAllApplicationsInfo_summary) + StringConstants.CHAR_NEW_LINE +
                        getString(R.string.profile_preferences_deviceWiFiAPInfo2_summary));
            }
        }

        preference = findPreference(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND);
        if (preference != null) {
            preference.setSummary(getString(R.string.profile_preferences_volumeMuteSound_summary)+". "+
                    getString(R.string.profile_preferences_volumeMuteSound_summary_2));
        }

        preference = findPreference(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
        if (preference != null) {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP, null, preferences, true, context);
            preference.setEnabled((preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                    ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                            (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                            (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                            (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                            (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
        }

        PPListPreference notificationLEDPreference = findPreference(Profile.PREF_PROFILE_NOTIFICATION_LED);
        if (notificationLEDPreference != null) {
            notificationLEDPreference.setTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_notificationLed_23));
            notificationLEDPreference.setDialogTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_notificationLed_23));
            String value = preferences.getString(Profile.PREF_PROFILE_NOTIFICATION_LED, "");
            setSummary(Profile.PREF_PROFILE_NOTIFICATION_LED, value);
        }
        preference = findPreference(PREF_NOTIFICATION_LED_INFO);
        if (preference != null) {
            preference.setTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_notificationLed_23));
            //preference.setDialogTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrationIntensityTouchInteraction));
            //String value = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, "-1|1");
            //setSummary(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, value);
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_NOTIFICATION_LED, null, preferences, true, context);
            preference.setEnabled((preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                    ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
        }
        preference = findPreference(PREF_ALWAYS_ON_DISPLAY_INFO);
        if (preference != null) {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, null, preferences, true, context);
            preference.setEnabled((preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                    ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
        }
        preference = findPreference(PREF_SCREEN_DARK_MODE_INFO);
        if (preference != null) {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SCREEN_DARK_MODE, null, preferences, true, context);
            preference.setEnabled((preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                    ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
        }

        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            if (telephonyManager.getPhoneCount() > 1) {

                preference = findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, null, preferences, true, context);
                    preference.setEnabled((preferenceAllowedSIM1.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                        ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                         (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                         (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                         (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                         (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                }
                preference = findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, null, preferences, true, context);
                    preference.setEnabled((preferenceAllowedSIM2.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                             (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                             (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                             (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                             (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                }
                /*
                preference = findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, null, preferences, true, context);
                    preference.setEnabled((preferenceAllowedSIM1.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                             (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                             (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                             (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                }
                preference = findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, null, preferences, true, context);
                    preference.setEnabled((preferenceAllowedSIM2.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                             (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                             (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                             (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                }
                */

                preference = findPreference(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, null, preferences, true, context);
                    preference.setEnabled((preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                             (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                             (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                             (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                             (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                }

                PPListPreference listPreference = findPreference(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1);
                if (listPreference != null) {
                    PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, null, preferences, true, context);

                    listPreference.setTitle(StringConstants.STR_SHIZUKU_ROOT+ getString(R.string.profile_preferences_deviceOnOff_SIM1));
                    listPreference.setDialogTitle(StringConstants.STR_SHIZUKU_ROOT+getString(R.string.profile_preferences_deviceOnOff_SIM1));
                    String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, "");
                    setSummary(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, value);

                    listPreference.setEnabled((preferenceAllowedSIM1.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                             (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                             (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                             (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                             (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                }
                listPreference = findPreference(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2);
                if (listPreference != null) {
                    PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, null, preferences, true, context);

                    listPreference.setTitle(StringConstants.STR_SHIZUKU_ROOT+ getString(R.string.profile_preferences_deviceOnOff_SIM2));
                    listPreference.setDialogTitle(StringConstants.STR_SHIZUKU_ROOT+getString(R.string.profile_preferences_deviceOnOff_SIM2));
                    String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, "");
                    setSummary(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, value);

                    listPreference.setEnabled((preferenceAllowedSIM2.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                             (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                             (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                             (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                             (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                }

                if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                        (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                        (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                        (PPApplication.deviceIsOnePlus)) {
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, null, preferences, true, context);
                        preference.setEnabled((preferenceAllowedSIM1.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                    }

                    preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1, null, preferences, true, context);
                        preference.setEnabled((preferenceAllowedSIM1.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                        disableDependedPref(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
                    }

                    preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, null, preferences, true, context);
                        preference.setEnabled((preferenceAllowedSIM2.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                    }

                    preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2, null, preferences, true, context);
                        preference.setEnabled((preferenceAllowedSIM2.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                        disableDependedPref(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
                    }

                    listPreference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
                    if (listPreference != null) {
                        PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, null, preferences, true, context);

                        listPreference.setTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT+getString(R.string.profile_preferences_soundNotificationChangeSIM1));
                        listPreference.setDialogTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT+getString(R.string.profile_preferences_soundNotificationChangeSIM1));
                        String value = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, "");
                        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, value);

                        listPreference.setEnabled((preferenceAllowedSIM1.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                    }

                    RingtonePreference ringtonePreference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
                    if (ringtonePreference != null) {
                        PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1, null, preferences, true, context);

                        ringtonePreference.setTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT+getString(R.string.profile_preferences_soundNotificationSIM1));
                        ringtonePreference.setDialogTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT+getString(R.string.profile_preferences_soundNotificationSIM1));
                        String value = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1, "");
                        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1, value);

                        ringtonePreference.setEnabled((preferenceAllowedSIM1.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                        disableDependedPref(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
                    }

                    listPreference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
                    if (listPreference != null) {
                        PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, null, preferences, true, context);

                        if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI)) {
                            listPreference.setTitle(StringConstants.STR_SHIZUKU_ROOT + getString(R.string.profile_preferences_soundNotificationChangeSIM2));
                            listPreference.setDialogTitle(StringConstants.STR_SHIZUKU_ROOT + getString(R.string.profile_preferences_soundNotificationChangeSIM2));
                        } else {
                            listPreference.setTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_soundNotificationChangeSIM2));
                            listPreference.setDialogTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_soundNotificationChangeSIM2));
                        }
                        String value = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, "");
                        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, value);

                        listPreference.setEnabled((preferenceAllowedSIM2.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                    }

                    ringtonePreference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
                    if (ringtonePreference != null) {
                        PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2, null, preferences, true, context);

                        if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI)) {
                            ringtonePreference.setTitle(StringConstants.STR_SHIZUKU_ROOT + getString(R.string.profile_preferences_soundNotificationSIM2));
                            ringtonePreference.setDialogTitle(StringConstants.STR_SHIZUKU_ROOT + getString(R.string.profile_preferences_soundNotificationSIM2));
                        } else {
                            ringtonePreference.setTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_soundNotificationSIM2));
                            ringtonePreference.setDialogTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_soundNotificationSIM2));
                        }
                        String value = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2, "");
                        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2, value);

                        ringtonePreference.setEnabled((preferenceAllowedSIM2.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                        disableDependedPref(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
                    }
                } else {
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
                    if (preference != null)
                        preference.setVisible(false);
                }

            } else {

                preference = findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2);
                if (preference != null)
                    preference.setVisible(false);

                //preference = findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1);
                //if (preference != null)
                //    preference.setVisible(false);
                //preference = findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2);
                //if (preference != null)
                //    preference.setVisible(false);

                preference = findPreference(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS);
                if (preference != null)
                    preference.setVisible(false);

                preference = findPreference(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2);
                if (preference != null)
                    preference.setVisible(false);

                preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
                if (preference != null)
                    preference.setVisible(false);

            }
        } else {

            preference = findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1);
            if (preference != null)
                preference.setVisible(false);
            preference = findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2);
            if (preference != null)
                preference.setVisible(false);

            /*
            preference = findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1);
            if (preference != null)
                preference.setVisible(false);
            preference = findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2);
            if (preference != null)
                preference.setVisible(false);
             */

            preference = findPreference(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS);
            if (preference != null)
                preference.setVisible(false);

            preference = findPreference(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1);
            if (preference != null)
                preference.setVisible(false);
            preference = findPreference(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2);
            if (preference != null)
                preference.setVisible(false);

            preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
            if (preference != null)
                preference.setVisible(false);
            preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
            if (preference != null)
                preference.setVisible(false);
            preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
            if (preference != null)
                preference.setVisible(false);
            preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
            if (preference != null)
                preference.setVisible(false);
            preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
            if (preference != null)
                preference.setVisible(false);
            preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
            if (preference != null)
                preference.setVisible(false);
            preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
            if (preference != null)
                preference.setVisible(false);
            preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
            if (preference != null)
                preference.setVisible(false);

        }

        if (!((PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                PPApplication.deviceIsOnePlus)) {
            preference = findPreference(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS);
            if (preference != null) {
                preference.setVisible(false);
            }
        }
        else {
            PPListPreference listPreference = findPreference(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS);
            if (listPreference != null) {
                listPreference.setTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT+getString(R.string.profile_preferences_soundSameRingtoneForBothSIMCards));
                listPreference.setDialogTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT+getString(R.string.profile_preferences_soundSameRingtoneForBothSIMCards));
                String value = preferences.getString(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, "");
                setSummary(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, value);
            }
        }

        if (PPApplication.deviceIsXiaomi || PPApplication.romIsMIUI) {
            preference = findPreference(PREF_PROFILE_DEVICE_RUN_APPLICATION_MIUI_PERMISSIONS);
            if (preference != null) {
                preference.setOnPreferenceClickListener(preference118 -> {
                    PPAlertDialog dialog = new PPAlertDialog(
                            preference118.getTitle(),
                            getString(R.string.profile_preferences_deviceRunApplicationsShortcutsForMIU_dialod_message),
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
                                            true, true,
                                            false, false,
                                            true,
                                            false,
                                            activity
                                    );

                                    if (!activity.isFinishing())
                                        dialog2.show();
                                }
                            },
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
                        dialog.show();
                    return false;
                });
            }
        }
        else {
            preference = findPreference(PREF_PROFILE_DEVICE_RUN_APPLICATION_MIUI_PERMISSIONS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_PROFILE_OTHERS_CATTEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }

        if (!PPApplication.deviceIsPixel) {
            preference = findPreference(PREF_PROFILE_VOLUME_SOUND_MODE_VIBRATION_INFO);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_PROFILE_SOUND_PROFILE_CATTEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }

        /*
        infoDialogPreference = prefMng.findPreference("prf_pref_deviceVPNInfo");
        if (infoDialogPreference != null) {
            String url1 = "https://openvpn.net/vpn-server-resources/faq-regarding-openvpn-connect-android/#how-do-i-use-tasker-with-openvpn-connect-for-android";
            String url2 = "https://github.com/schwabe/ics-openvpn#controlling-from-external-apps";

            String infoText =
                    getString(R.string.profile_preferences_deviceVPNInfo_infoText)+"<br><br>"+
                            "<a href=" + url1 + ">OpenVPN Connect&nbsp;»»</a>"+"<br><br>"+
                            "<a href=" + url2 + ">OpenVPN for Android&nbsp;»»</a>";

            infoDialogPreference.setInfoText(infoText);
            infoDialogPreference.setIsHtml(true);
        }
        */

        preference = findPreference(PREF_PROFILE_DEVICE_SCREEN_TIMEOUT_AND_KEEP_SCREEN_ON_INFO);
        if (preference != null) {
            String title = "\"" + getString(R.string.profile_preferences_deviceScreenTimeout) + "\" " +
                    getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOn_title) +
                    " \"" + getString(R.string.profile_preferences_deviceScreenOnPermanent) + "\"";
            preference.setTitle(title);
            String summary = getString(R.string.profile_preferences_deviceScreenOnPermanent) + StringConstants.STR_COLON_WITH_SPACE;
            if (ApplicationPreferences.keepScreenOnPermanent)
                summary = summary + getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOnInfo_summary_0_On);
            else
                summary = summary + getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOnInfo_summary_0_Off);
            summary = summary + StringConstants.STR_DOUBLE_NEWLINE;
            summary = summary + getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOnInfo_summary);
            preference.setSummary(summary);
        }

        if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
            preference = prefMng.findPreference(PREF_PROFILE_DEVICE_BRIGHTNESS_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference116 -> {
                    Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, PhoneProfilesPrefsFragment.PREF_SPECIAL_PROFILE_PARAMETERS_CATEGORY_ROOT);
                    //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    startActivityForResult(intent, RESULT_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON_SETTINGS);
                    return false;
                });
            }
        }
        else {
            preference = findPreference(PREF_PROFILE_DEVICE_BRIGHTNESS_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_PROFILE_SCREEN_CATTEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }

        Preference assistantPreference = prefMng.findPreference(PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS);
        if (assistantPreference != null) {
            //assistantPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            assistantPreference.setOnPreferenceClickListener(preference13 -> {
                configureAssistant();
                return false;
            });
        }

        infoDialogPreference = prefMng.findPreference(PREF_PROFILE_AIRPLANE_MODE_RADIOS_INFO);
        if (infoDialogPreference != null) {

            String url;
            if (DebugVersion.enabled)
                url = PPApplication.HELP_AIRPLANE_MODE_RADIOS_CONFIG_DEVEL;
            else
                url = PPApplication.HELP_AIRPLANE_MODE_RADIOS_CONFIG;

            String infoText =
                    StringConstants.TAG_BOLD_START_HTML+getString(R.string.profile_preferences_deviceAirplaneModeRadios_info1) + StringConstants.TAG_BOLD_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML +
                            getString(R.string.profile_preferences_deviceAirplaneModeRadios_info2) + " " +
                            getString(R.string.profile_preferences_deviceAirplaneModeRadios_info3) + ":"+StringConstants.TAG_BREAK_HTML +
                            StringConstants.TAG_URL_LINK_START_HTML + url + StringConstants.TAG_URL_LINK_START_URL_END_HTML + url+ StringConstants.STR_HARD_SPACE_DOUBLE_ARROW_HTML+StringConstants.TAG_URL_LINK_END_HTML+StringConstants.TAG_DOUBLE_BREAK_HTML;

            String configuredRadios = Settings.Global.getString(context.getContentResolver(), "airplane_mode_radios");

            infoText = infoText + getString(R.string.profile_preferences_deviceAirplaneModeRadios_info4) + " " + configuredRadios;

            infoDialogPreference.setInfoText(infoText);
            infoDialogPreference.setIsHtml(true);
        }

        preference = findPreference(PREF_PROFILE_VOLUME_MEDIA_ONEPLUS_INFO);
        if (!PPApplication.deviceIsOnePlus) {
            if (preference != null) {
                PreferenceCategory preferenceCategory = findPreference(PREF_PROFILE_VOLUME_TYPE_CATTEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        } else {
            if (Build.VERSION.SDK_INT >= 33) {
                if (preference != null)
                    preference.setSummary(R.string.profile_preferences_volumeMediaOnePlusInfo_33_summary);
            }
        }

        long workMinInterval = TimeUnit.MILLISECONDS.toMinutes(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS);
        String summary = getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary1) + " " +
                workMinInterval + " " +
                getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary2);
        preference = findPreference(PREF_PROFILE_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL_INFO);
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference(PREF_PROFILE_APPLICATION_LOCATION_UPDATE_INTERVAL);
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference(PREF_PROFILE_APPLICATION_WIFI_SCAN_INTERVAL_INFO);
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference(PREF_PROFILE_APPLICATION_BLUETOOTH_SCAN_INTERVAL_INFO);
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference(PREF_PROFILE_APPLICATION_ORIENTATION_SCAN_INTERVAL_INFO);
        if (preference != null) {
            summary = getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary1) + " 10 " +
                    getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary3);
            preference.setSummary(summary);
        }

        if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme ||
                (PPApplication.deviceIsOnePlus && (Build.VERSION.SDK_INT < 34))) {
            PPListPreference listPreference = findPreference(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, null, preferences, true, context);

                listPreference.setTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_deviceScreenTimeout));
                listPreference.setDialogTitle(StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_deviceScreenTimeout));
                String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, "");
                setSummary(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, value);

                listPreference.setEnabled((preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                        ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED)||
                                (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                disableDependedPref(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT);
            }
        }

        if (!(PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)) {
            preference = findPreference(PREF_PROFILE_DEVICE_WALLPAPER_HUAWEI_INFO);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(PREF_PROFILE_DEVICE_WALLPAPER_CATTEGORY);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }

        preference = findPreference(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS);
        if (preference != null) {
            disableDependedPref(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS);
        }

        preference = findPreference(PREF_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference114 -> {
                boolean ok = false;
                String action;
                action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
                if (GlobalGUIRoutines.activityActionExists(action, activity.getApplicationContext())) {
                    try {
                        Intent intent = new Intent(action);
                        startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
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
                            true, true,
                            false, false,
                            true,
                            false,
                            activity
                    );

                    if (!activity.isFinishing())
                        dialog.show();
                }
                return false;
            });
        }
        if (Build.VERSION.SDK_INT >= 33) {
            InfoDialogPreference infoDialogPreference2 = prefMng.findPreference(PREF_NOTIFICATION_ACCESS_RESTRICTED_SETTINGS);
            if (infoDialogPreference2 != null) {
                infoDialogPreference2.setOnPreferenceClickListener(preference120 -> {
//                    Log.e("PhoneProfilesPrefsFragment.onActivityCreated", "preference clicked");

                    infoDialogPreference2.setInfoText(
                            StringConstants.TAG_URL_LINK_START_HTML + InfoDialogPreference.PPP_APP_INFO_SCREEN + StringConstants.TAG_URL_LINK_START_URL_END_HTML +
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
                                    "\"" + getString(R.string.menu_import_export) + "\"/\"" + getString(R.string.menu_import) + "\"."
                    );
                    infoDialogPreference2.setIsHtml(true);

                    return false;
                });
            }
        }

        preference = findPreference(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED);
        if (preference != null) {
            disableDependedPref(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED);
        }

        /*if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
            preference = findPreference(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT);
            if (preference != null) {
                preference.setVisible(false);
            }
        } else {*/
            PPListPreference listPreference = findPreference(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT);
            if (listPreference != null) {
                listPreference.setTitle(ProfileStatic.getNightLightStringId());
                listPreference.setDialogTitle(ProfileStatic.getNightLightStringId());
            }
        //}
        if ((PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                PPApplication.deviceIsOnePlus) {
            preference = findPreference(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT_PREFS);
            if (preference != null) {
                preference.setVisible(false);
            }
        } else {
            listPreference = findPreference(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT_PREFS);
            if (listPreference != null) {
                listPreference.setTitle(ProfileStatic.getNightLightStringPrefsId());
                listPreference.setDialogTitle(ProfileStatic.getNightLightStringPrefsId());
            }
        }

        preference = findPreference(Profile.PREF_PROFILE_DEVICE_KEYGUARD);
        if (preference != null) {
            disableDependedPref(Profile.PREF_PROFILE_DEVICE_KEYGUARD);
        }
        preference = findPreference(Profile.PREF_PROFILE_SCREEN_ON_OFF);
        if (preference != null) {
            disableDependedPref(Profile.PREF_PROFILE_SCREEN_ON_OFF);
        }

        preference = findPreference((PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_INFO));
        if (preference != null) {
            preference.setSummary(getString(R.string.profile_preferences_deviceForceStopApplicationsInfo_summary) + "\n"
                    + getString(R.string.profile_preferences_deviceForceStopApplicationsInfo_summary_2));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() == null)
            return;

        //Log.e("ProfilesPrefsFragment.onResume", "xxxxxx");

        // this is important for update preferences after PPPPS and Extender installation
        //Log.e("ProfilesPrefsFragment.onResume", "called updateAllSummary");
        updateAllSummary();

        if (!nestedFragment) {
            //final Context context = getActivity().getBaseContext();

            //disableDependedPref(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            //disableDependedPref(Profile.PREF_PROFILE_LOCK_DEVICE);
            setRedTextToPreferences();
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] ProfilesPrefsFragment.onResume", "call of updateGUI");
            PPApplication.updateGUI(true, false, getActivity());
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if ((setRingtonePreferenceSummaryAsyncTask != null) &&
                setRingtonePreferenceSummaryAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            setRingtonePreferenceSummaryAsyncTask.cancel(true);
        setRingtonePreferenceSummaryAsyncTask = null;
        if ((setProfileSoundsPreferenceSummaryAsyncTask != null) &&
                setProfileSoundsPreferenceSummaryAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            setProfileSoundsPreferenceSummaryAsyncTask.cancel(true);
        setProfileSoundsPreferenceSummaryAsyncTask = null;
        if ((setProfileSoundsDualSIMPreferenceSummaryAsyncTask != null) &&
                setProfileSoundsDualSIMPreferenceSummaryAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            setProfileSoundsDualSIMPreferenceSummaryAsyncTask.cancel(true);
        setProfileSoundsDualSIMPreferenceSummaryAsyncTask = null;
        if ((setRedTextToPreferencesAsyncTask != null) &&
                setRedTextToPreferencesAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            setRedTextToPreferencesAsyncTask.cancel(true);
        setRedTextToPreferencesAsyncTask = null;

        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);

            /*
            SharedPreferences.Editor editor = profilesPreferences.edit();
            updateSharedPreferences(editor, preferences);
            editor.apply();
            */

        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getActivity() == null)
            return;

        ProfilesPrefsActivity activity = (ProfilesPrefsActivity) getActivity();

        if ((key != null) && key.equals(Profile.PREF_PROFILE_NAME)) {
            //noinspection UnnecessaryLocalVariable
            String value = sharedPreferences.getString(key, "");
            //if (getActivity() != null) {

                // must be used handler for rewrite toolbar title/subtitle
                final String _value = value;
                final Handler handler = new Handler(activity.getMainLooper());
                final WeakReference<ProfilesPrefsActivity> activityWeakRef
                        = new WeakReference<>(activity);
                handler.postDelayed(() -> {
//                        PPApplicationStatic.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ProfilesPrefsFragment.onSharedPreferenceChanged");
                    ProfilesPrefsActivity __activity = activityWeakRef.get();
                    if ((__activity == null) || __activity.isFinishing() || __activity.isDestroyed())
                        return;

                    Toolbar toolbar = __activity.findViewById(R.id.activity_preferences_toolbar);
                    //toolbar.setSubtitle(getString(R.string.profile_string_0) + ": " + _value);
                    //noinspection DataFlowIssue
                    toolbar.setTitle(__activity.getString(R.string.profile_string_0) + StringConstants.STR_COLON_WITH_SPACE + _value);
                }, 200);
            //}
        }

        String value;

        if ((key != null) &&
                (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
                key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
                key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE) ||
                key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_MEDIA_CHANGE_DURING_PLAY) ||
                key.equals(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS) ||
                key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED) ||
                key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS) ||
                key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT))) {
            boolean bValue = sharedPreferences.getBoolean(key, false);
            value = Boolean.toString(bValue);
        }
        else
        if ((key != null) && key.equals(Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME)) {
            value = String.valueOf(sharedPreferences.getInt(key, 0));
        }
        else {
            if ((key != null) && prefMng.findPreference(key) != null)
                value = sharedPreferences.getString(key, "");
            else
                value = "";
        }
        setSummary(key, value);

        // disable depended preferences
        disableDependedPref(key, value);

        setRedTextToPreferences();

        //ProfilesPrefsActivity activity = (ProfilesPrefsActivity)getActivity();
        //if (activity != null) {
            activity.showSaveMenu = true;
            activity.invalidateOptionsMenu();
        //}

    }

    void doOnActivityResult(int requestCode, int resultCode, Intent data) {
        if (getActivity() == null)
            return;

        final Context context = getActivity().getBaseContext();

        ProfilesPrefsActivity activity = (ProfilesPrefsActivity) getActivity();

        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE)) {
            setRedTextToPreferences();
        }
        /*if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_GRANT_ROOT) {
            PPApplication.isRootGranted();
            setRedTextToPreferences();
        }*/

        if (((requestCode == WallpaperViewPreference.RESULT_LOAD_IMAGE) || (requestCode == WallpaperViewPreference.RESULT_LOAD_IMAGE_LOCKSCREEN)) &&
                (resultCode == Activity.RESULT_OK) && (data != null))
        {
            //Uri selectedImage = data.getData();
            String  d = data.getDataString();
            if (d != null) {
                Uri selectedImage = Uri.parse(d);
                /*//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        ContentResolver resolver = activity.getContentResolver();
                        resolver.takePersistableUriPermission(selectedImage, takeFlags);
                    } catch (Exception e) {
                        Log.e("ProfilesPrefsFragment.doOnActivityResult", Log.getStackTraceString(e));
                    }
                //}*/
                WallpaperViewPreference preference;
                if (requestCode == WallpaperViewPreference.RESULT_LOAD_IMAGE)
                    preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
                else
                    preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_LOCKSCREEN);
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

        if ((requestCode == WallpaperFolderPreference.RESULT_GET_FOLDER) && (resultCode == Activity.RESULT_OK) && (data != null))
        {
            //Uri selectedImage = data.getData();
            String  d = data.getDataString();
            if (d != null) {
                Uri selectedFolder = Uri.parse(d);
                WallpaperFolderPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER);
                if (preference != null)
                    preference.setWallpaperFolder(selectedFolder.toString());
            }
        }

        if ((requestCode == ProfileIconPreference.RESULT_LOAD_IMAGE) && (resultCode == Activity.RESULT_OK) && (data != null))
        {
            //Uri selectedImage = data.getData();
            String  d = data.getDataString();
            if (d != null) {
                Uri selectedImage = Uri.parse(d);
                /*//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        ContentResolver resolver = activity.getContentResolver();
                        resolver.takePersistableUriPermission(selectedImage, takeFlags);
                    } catch (Exception e) {
                        Log.e("ProfilesPrefsFragment.doOnActivityResult", Log.getStackTraceString(e));
                    }
                //}*/

                int height = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
                int width = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
                if (BitmapManipulator.checkBitmapSize(selectedImage.toString(), width, height, getContext())) {
                    ProfileIconPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_ICON);
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
                    //if (getActivity() != null) {
                        String text = getString(R.string.profileicon_pref_dialog_custom_icon_image_too_large);
                        text = text + " " + (width * BitmapManipulator.ICON_BITMAP_SIZE_MULTIPLIER);
                        text = text + "x" + (height * BitmapManipulator.ICON_BITMAP_SIZE_MULTIPLIER);
                        PPApplication.showToast(activity.getApplicationContext(), text, Toast.LENGTH_LONG);
                    //}
                }
            }
        }
        if (requestCode == RESULT_NOTIFICATION_ACCESS_SETTINGS) {
            //final boolean canEnableZenMode =
            //        (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
            //                (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
            //        );

            final String sZenModeType = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "");
            setSummary(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, sZenModeType);
        }
        if ((requestCode == RunApplicationsDialogPreference.RESULT_APPLICATIONS_EDITOR) && (resultCode == Activity.RESULT_OK) && (data != null))
        {
            RunApplicationsDialogPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            if (preference != null) {
                //noinspection deprecation
                preference.updateShortcut(
                        data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT),
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
        if (requestCode == RunApplicationEditorDialog.RESULT_INTENT_EDITOR) {
            if (resultCode == Activity.RESULT_OK) {
                RunApplicationsDialogPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
                if ((preference != null) && (data != null)) {
                    preference.updateIntent(data.getParcelableExtra(RunApplicationEditorDialog.EXTRA_PP_INTENT),
                            data.getParcelableExtra(RunApplicationEditorDialog.EXTRA_APPLICATION),
                            data.getIntExtra(RunApplicationEditorIntentActivity.EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0));
                }
            }
        }
        if (requestCode == RESULT_UNLINK_VOLUMES_APP_PREFERENCES) {
            setSummary(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);
            disableDependedPref(Profile.PREF_PROFILE_VOLUME_RINGTONE);
            disableDependedPref(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
        }
        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS) {
            // this is important for update all preferences
            updateAllSummary();
//            setSummary(PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS);
//            setSummary(PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS);
//            disableDependedPref(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
//            disableDependedPref(Profile.PREF_PROFILE_LOCK_DEVICE);

            setRedTextToPreferences();
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] EventsPrefsFragment.doOnActivityResult (1)", "call of updateGUI");
            PPApplication.updateGUI(true, false, context);

            // show save menu
            //ProfilesPrefsActivity activity = (ProfilesPrefsActivity)getActivity();
            //if (activity != null) {
                activity.showSaveMenu = true;
                activity.invalidateOptionsMenu();
            //}
        }
        if ((requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_IMAGE_WALLPAPER)) ||
            (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_IMAGE_WALLPAPER_LOCKSCREEN))) {
            WallpaperViewPreference preference;
            if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_IMAGE_WALLPAPER))
                preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
            else
                preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_LOCKSCREEN);
            if (preference != null)
                preference.startGallery(); // image file
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_WALLPAPER_FOLDER)) {
            WallpaperFolderPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER);
            if (preference != null)
                preference.startGallery(); // folder of images
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON)) {
            ProfileIconPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_ICON);
            if (preference != null)
                preference.startGallery();
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG)) {
            BrightnessDialogPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS);
            if (preference != null)
                preference.enableViews();
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_RINGTONE_PREFERENCE)) {
            RingtonePreference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_ALARM);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
            if (preference != null)
                preference.refreshListView();
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CONNECT_TO_SSID_DIALOG)) {
            ConnectToSSIDDialogPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID);
            if (preference != null)
                preference.refreshListView();
        }
        if (requestCode == RESULT_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON_SETTINGS) {
            setSummary(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS);
        }
        if (requestCode == RESULT_ASSISTANT_SETTINGS) {
            //disableDependedPref(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
            setSummary(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
            setSummary(PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS);
            // show save menu
            //ProfilesPrefsActivity activity = (ProfilesPrefsActivity)getActivity();
            //if (activity != null) {
                activity.showSaveMenu = true;
                activity.invalidateOptionsMenu();
            //}
        }
        if (requestCode == RESULT_NOTIFICATION_ACCESS_SYSTEM_SETTINGS) {
            setSummary(PREF_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
            setSummary(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS);
            //disableDependedPref(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED);
        }
    }

    @SuppressWarnings("deprecation")
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

    private void initPreferenceFragment(/*Bundle savedInstanceState*/) {
        prefMng = getPreferenceManager();

        preferences = prefMng.getSharedPreferences();

        /*

        if (savedInstanceState == null) {
            if (getContext() != null) {
                profilesPreferences = getContext().getSharedPreferences(PREFS_NAME_ACTIVITY, Activity.MODE_PRIVATE);

                SharedPreferences.Editor editor = preferences.edit();
                updateSharedPreferences(editor, profilesPreferences);
                editor.apply();
            }
        }
        */

        if (preferences != null)
            preferences.registerOnSharedPreferenceChangeListener(this);
    }

    /*
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
    }
    */

    private boolean notGrantedG1Permission;
    private boolean notRootedOrGrantetRoot;
    private boolean notInstalledPPPPS;
    private boolean notGrantedShizuku;

    private String getCategoryTitleWhenPreferenceChanged(String key, int preferenceTitleId,
                                                         Context context) {
        //Preference preference = prefMng.findPreference(key);
        String title = "";
        PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
        boolean _notGrantedG1Permission =
                (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION);
        boolean _notRootedOrGrantedRoot =
                (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                        ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                         (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED));
        boolean _notDefaultAssistant =
                (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SET_AS_ASSISTANT);
        boolean _notInstalledPPPS =
                (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                        (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS);
        boolean _notGrantedShizuku =
                (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                        (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED);
        if ((preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                _notGrantedG1Permission ||
                _notRootedOrGrantedRoot ||
                _notGrantedShizuku ||
                _notDefaultAssistant ||
                _notInstalledPPPS) {
            String defaultValueS;
            switch (key) {
                case Profile.PREF_PROFILE_ASK_FOR_DURATION:
                case Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE:
                case Profile.PREF_PROFILE_VOLUME_MUTE_SOUND:
                //case Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR:
                case Profile.PREF_PROFILE_SEND_SMS_SEND_SMS:
                case Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED:
                case Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS:
                case Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT:
                    /*boolean defaultValue =
                            getResources().getBoolean(
                                    GlobalGUIRoutines.getResourceId(key, "bool", context));*/

                    boolean hasVibrator = true;
                    if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE)) {
                        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        hasVibrator = (vibrator != null) && vibrator.hasVibrator();
                    }

                    //noinspection ConstantConditions
                    boolean defaultValue = Profile.defaultValuesBoolean.get(key);
                    if (hasVibrator && preferences.getBoolean(key, defaultValue) != defaultValue) {
                        title = getString(preferenceTitleId);
                        notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                        notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                        notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                        notGrantedShizuku = notGrantedShizuku || _notGrantedShizuku;
                    }
                    break;
                case Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME:
                    title = context.getString(R.string.profile_preferences_exactTime);
                    break;
                case Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE:
                    defaultValueS = Profile.defaultValuesString.get(key);
                    String airplanemode = preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, defaultValueS);
                    if ((airplanemode != null) && (!airplanemode.equals(defaultValueS))) {
                        title = getString(preferenceTitleId);
                        notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                        notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                        notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                        notGrantedShizuku = notGrantedShizuku || _notGrantedShizuku;
                    }
                    break;
                case Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE:
                    defaultValueS = Profile.defaultValuesString.get(key);
                    String forceStop = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, defaultValueS);
                    if ((forceStop != null) && (!forceStop.equals(defaultValueS))) {
                        title = getString(preferenceTitleId);
                        notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                        notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                        notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                        notGrantedShizuku = notGrantedShizuku || _notGrantedShizuku;
                    }
                    break;
                default:
                    /*String defaultValue =
                            getResources().getString(
                                    GlobalGUIRoutines.getResourceId(key, "string", context));*/
                    defaultValueS = Profile.defaultValuesString.get(key);
                    String value = preferences.getString(key, defaultValueS);
                    if (value != null) {
                        switch (key) {
                            case Profile.PREF_PROFILE_VOLUME_RINGTONE:
                            case Profile.PREF_PROFILE_VOLUME_NOTIFICATION:
                            case Profile.PREF_PROFILE_VOLUME_MEDIA:
                            case Profile.PREF_PROFILE_VOLUME_ALARM:
                            case Profile.PREF_PROFILE_VOLUME_SYSTEM:
                            case Profile.PREF_PROFILE_VOLUME_VOICE:
                            case Profile.PREF_PROFILE_VOLUME_DTMF:
                            case Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY:
                            case Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO:
                                if (VolumeDialogPreference.changeEnabled(value)) {
                                    title = getString(preferenceTitleId);
                                    notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                    notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                    notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                    notGrantedShizuku = notGrantedShizuku || _notGrantedShizuku;
                                }
                                break;
                            case Profile.PREF_PROFILE_DEVICE_BRIGHTNESS:
                                if (BrightnessDialogPreference.changeEnabled(value)) {
                                    title = getString(preferenceTitleId);
                                    notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                    notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                    notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                    notGrantedShizuku = notGrantedShizuku || _notGrantedShizuku;
                                }
                                break;
                            case Profile.PREF_PROFILE_VOLUME_ZEN_MODE:
                                title = getString(preferenceTitleId);
                                notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                notGrantedShizuku = notGrantedShizuku || _notGrantedShizuku;
                                break;
                            case Profile.PREF_PROFILE_GENERATE_NOTIFICATION:
                                if (GenerateNotificationDialogPreference.changeEnabled(value)) {
                                    title = getString(preferenceTitleId);
                                    notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                    notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                    notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                    notGrantedShizuku = notGrantedShizuku || _notGrantedShizuku;
                                }
                                break;
                            case Profile.PREF_PROFILE_DEVICE_VPN:
                                if (VPNDialogPreference.changeEnabled(value)) {
                                    title = getString(preferenceTitleId);
                                    notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                    notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                    notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                    notGrantedShizuku = notGrantedShizuku || _notGrantedShizuku;
                                }
                                break;
                            case Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING:
                            case Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS:
                            case Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION:
                                if (VibrationIntensityPreference.changeEnabled(value)) {
                                    //title = getString(preferenceTitleId);
                                    notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                    notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                    notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                    notGrantedShizuku = notGrantedShizuku || _notGrantedShizuku;
                                    if (!((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                                            PPApplication.deviceIsOnePlus)) {
                                        if (key.equals(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING))
                                            title = StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrationIntensityRinging);
                                        else if (key.equals(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS))
                                            title = StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrationIntensityNotificatiions);
                                        else/* if (key.equals(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION))*/
                                            title = StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrationIntensityTouchInteraction);
                                    } else
                                        title = getString(preferenceTitleId);
                                }
                                break;
                            default:
                                if (!value.equals(defaultValueS)) {
                                    if (key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING) &&
                                            ((PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                                                    PPApplication.deviceIsOnePlus))
                                        title = StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrateWhenRinging);
                                    else if (key.equals(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS))
                                        title = StringConstants.STR_PPPPS_SHIZUKU_ROOT + getString(R.string.profile_preferences_vibrateNotifications);
                                    else if (key.equals(Profile.PREF_PROFILE_DURATION))
                                        title = context.getString(R.string.profile_preferences_duration);
                                    else
                                        title = getString(preferenceTitleId);
                                    notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                    notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                    notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                    notGrantedShizuku = notGrantedShizuku || _notGrantedShizuku;
                                }
                                break;
                        }
                    }
                    break;
            }
        }
        return title;
    }

    private static class CattegorySummaryData {
        String summary;
        boolean permissionGranted;
        boolean forceSet = false;
        boolean bold = false;
        boolean accessibilityEnabled = true;
        boolean defaultAssistantSet = true;
    }

    private boolean setCategorySummaryActivationDuration(Context context,
                                                         Preference preferenceScreen,
                                                         CattegorySummaryData cattegorySummaryData) {

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title;
        String askForDurationTitle = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_ASK_FOR_DURATION, R.string.profile_preferences_askForDuration, context);
        if (askForDurationTitle.isEmpty()) {
            String value = preferences.getString(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE));
            if ((value != null) && value.equals(String.valueOf(Profile.AFTER_DURATION_DURATION_TYPE_DURATION))) {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION, R.string.profile_preferences_duration, context);
                String afterDurationDoTitle = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_AFTER_DURATION_DO, R.string.profile_preferences_afterDurationDo, context);
                if (!title.isEmpty()) {
                    cattegorySummaryData.bold = true;
                    value = preferences.getString(Profile.PREF_PROFILE_DURATION, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION));
                    if (value != null) {
                        value = StringFormatUtils.getDurationString(Integer.parseInt(value));
                        _value.append(title)
                                .append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML)
                                .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_ACTIVATION_DURATION_CATTEGORY_ROOT, context))
                                .append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                        String afterDurationDoValue = preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_DO,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_AFTER_DURATION_DO));
                        value = StringFormatUtils.getListPreferenceString(afterDurationDoValue,
                                R.array.afterProfileDurationDoValues, R.array.afterProfileDurationDoArray, context);
                        _value.append(afterDurationDoTitle).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML)
                                .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_ACTIVATION_DURATION_CATTEGORY_ROOT, context))
                                .append(StringConstants.TAG_BOLD_END_HTML);

                        if ((afterDurationDoValue != null) && afterDurationDoValue.equals(String.valueOf(Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE))) {
                            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
                            long profileId = Long.parseLong(preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE)));
                            String profileName = dataWrapper.getProfileName(profileId);
                            dataWrapper.invalidateDataWrapper();
                            if (profileName != null)
                                value = profileName;
                            else {
                                if (profileId == Profile.PROFILE_NO_ACTIVATE)
                                    value = context.getString(R.string.profile_preference_profile_end_no_activate);
                            }
                            String _title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE, R.string.profile_preferences_afterDurationProfile, context);
                            _value.append(StringConstants.STR_BULLET).append(_title).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_ACTIVATION_DURATION_CATTEGORY_ROOT, context))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        }
                    } else
                        _value.append(afterDurationDoTitle);
                }
            } else {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME, R.string.profile_preferences_exactTime, context);
                String afterDurationDoTitle = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_AFTER_DURATION_DO, R.string.profile_preferences_afterExactTimeDo, context);
                if (!title.isEmpty()) {
                    cattegorySummaryData.bold = true;
                    //noinspection ConstantConditions
                    int iValue = preferences.getInt(Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME, Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME)));
                    value = String.valueOf(iValue);
                    //if (value != null) {
                    value = StringFormatUtils.getTimeString(Integer.parseInt(value));
                    _value.append(title).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML)
                            .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_ACTIVATION_DURATION_CATTEGORY_ROOT, context))
                            .append(StringConstants.TAG_BOLD_END_HTML+StringConstants.STR_BULLET);

                    String afterDurationDoValue = preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_DO,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_AFTER_DURATION_DO));
                    value = StringFormatUtils.getListPreferenceString(afterDurationDoValue,
                            R.array.afterProfileDurationDoValues, R.array.afterProfileDurationDoArray, context);
                    _value.append(afterDurationDoTitle).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                            .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_ACTIVATION_DURATION_CATTEGORY_ROOT, context))
                            .append(StringConstants.TAG_BOLD_END_HTML);

                    if ((afterDurationDoValue != null) && afterDurationDoValue.equals(String.valueOf(Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE))) {
                        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
                        long profileId = Long.parseLong(preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE)));
                        String profileName = dataWrapper.getProfileName(profileId);
                        dataWrapper.invalidateDataWrapper();
                        if (profileName != null)
                            value = profileName;
                        else {
                            if (profileId == Profile.PROFILE_NO_ACTIVATE)
                                value = context.getString(R.string.profile_preference_profile_end_no_activate);
                        }
                        String _title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE, R.string.profile_preferences_afterDurationProfile, context);
                        _value.append(StringConstants.STR_BULLET).append(_title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_ACTIVATION_DURATION_CATTEGORY_ROOT, context))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }
                }
            }
        }
        else {
            cattegorySummaryData.bold = true;
            askForDurationTitle = StringConstants.STR_MANUAL_SPACE + askForDurationTitle;
            _value.append(askForDurationTitle).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(getString(R.string.profile_preferences_enabled), prefMng, PREF_PROFILE_ACTIVATION_DURATION_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        if (cattegorySummaryData.bold) {
            // any of duration preferences are set
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, R.string.profile_preferences_durationNotificationSound, context);
            if (!title.isEmpty()) {
                if (_value.length() > 0)
                    _value.append(StringConstants.STR_BULLET);
                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(TAG_RINGTONE_NAME, prefMng, PREF_PROFILE_ACTIVATION_DURATION_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }

            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if ((vibrator != null) && vibrator.hasVibrator()) {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, R.string.profile_preferences_durationNotificationVibrate, context);
                if (!title.isEmpty()) {
                    if (_value.length() > 0)
                        _value.append(StringConstants.STR_BULLET);
                    _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                            .append(ProfileStatic.getColorForChangedPreferenceValue(getString(R.string.profile_preferences_enabled), prefMng, PREF_PROFILE_ACTIVATION_DURATION_CATTEGORY_ROOT, context))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                }
            }

            cattegorySummaryData.summary = _value.toString();

            setRingtonePreferenceSummary(cattegorySummaryData.summary,
                    preferences.getString(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND)),
                    preferenceScreen, context);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, cattegorySummaryData.bold, false, false, false, false);
            return true;
        }
        cattegorySummaryData.summary = _value.toString();
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummarySoundProfile(Context context,
                                                   CattegorySummaryData cattegorySummaryData) {
        String ringerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE,
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_RINGER_MODE));
        String zenMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE,
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_ZEN_MODE));

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, R.string.profile_preferences_volumeSoundMode, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;

            String value = StringFormatUtils.getListPreferenceString(ringerMode,
                    R.array.soundModeValues, R.array.soundModeArray, context);

            if (ringerMode != null) {
                boolean zenModeOffValue = ringerMode.equals("1") || ringerMode.equals("2") || ringerMode.equals("3");
                {
                    if (zenModeOffValue)
                        value = value + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                    else if (ringerMode.equals("4")) {
                        if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                                (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                                (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                                PPApplication.deviceIsRealme)
                            value = value + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                        else
                            value = value + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_On) + ")";
                    }
                }
            }

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SOUND_PROFILE_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        if (cattegorySummaryData.bold) {
            int titleRes;
            titleRes = R.string.profile_preferences_volumeZenModeM;
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, titleRes, context);
            if (!title.isEmpty()) {
                final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext());
                if ((ringerMode != null) && (ringerMode.equals("5")) && canEnableZenMode) {
                    if (_value.length() > 0)
                        _value.append(StringConstants.STR_BULLET);

                    String value = StringFormatUtils.getZenModePreferenceString(zenMode, context);

                    _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                            .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SOUND_PROFILE_CATTEGORY_ROOT, context))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                }
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, R.string.profile_preferences_vibrateWhenRinging, context);
            if (!title.isEmpty()) {
                if (ringerMode != null) {
                    if (ringerMode.equals("1") || ringerMode.equals("4")) {
                        if (_value.length() > 0)
                            _value.append(StringConstants.STR_BULLET);

                        String value = StringFormatUtils.getListPreferenceString(
                                preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING)),
                                R.array.vibrateWhenRingingValues, R.array.vibrateWhenRingingArray, context);

                        _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SOUND_PROFILE_CATTEGORY_ROOT, context))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    } else
                        //noinspection DuplicateExpressions
                        if ((ringerMode.equals("5")) && (zenMode != null) && (zenMode.equals("1") || zenMode.equals("2"))) {
                            if (_value.length() > 0)
                                _value.append(StringConstants.STR_BULLET);

                            String value = StringFormatUtils.getListPreferenceString(
                                    preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING)),
                                    R.array.vibrateWhenRingingValues, R.array.vibrateWhenRingingArray, context);

                            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SOUND_PROFILE_CATTEGORY_ROOT, context))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        }
                }
            }
            if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, R.string.profile_preferences_vibrateNotifications, context);
                if (!title.isEmpty()) {
                    if (ringerMode != null) {
                        if (ringerMode.equals("1") || ringerMode.equals("4")) {
                            if (_value.length() > 0)
                                _value.append(StringConstants.STR_BULLET);

                            String value = StringFormatUtils.getListPreferenceString(
                                    preferences.getString(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS)),
                                    R.array.vibrateNotificationsValues, R.array.vibrateNotificationsArray, context);

                            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SOUND_PROFILE_CATTEGORY_ROOT, context))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        } else
                            //noinspection DuplicateExpressions
                            if ((ringerMode.equals("5")) && (zenMode != null) && (zenMode.equals("1") || zenMode.equals("2"))) {
                                if (_value.length() > 0)
                                    _value.append(StringConstants.STR_BULLET);

                                String value = StringFormatUtils.getListPreferenceString(
                                        preferences.getString(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS,
                                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS)),
                                        R.array.vibrateNotificationsValues, R.array.vibrateNotificationsArray, context);

                                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                        .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SOUND_PROFILE_CATTEGORY_ROOT, context))
                                        .append(StringConstants.TAG_BOLD_END_HTML);
                            }
                    }
                }
            }
        }

        cattegorySummaryData.summary = _value.toString();

        Profile profile = new Profile();
        profile._vibrateWhenRinging = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, "0"));
        if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
            profile._vibrateNotifications = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, "0"));
        }
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileVibrateWhenRinging(context, profile, permissions);
        //if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
        //    Permissions.checkProfileVibrateNotifications(context, profile, permissions);
        //}
        cattegorySummaryData.permissionGranted = permissions.isEmpty();

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryVolume(Context context,
                                             CattegorySummaryData cattegorySummaryData) {

        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND, R.string.profile_preferences_volumeMuteSound, context);
        boolean isMuteEnabled = false;
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            isMuteEnabled = true;
            _value.append(title);
        }
        if (!isMuteEnabled) {
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_RINGTONE, R.string.profile_preferences_volumeRingtone, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                //if (!summary.isEmpty()) summary = summary + " • ";

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_RINGTONE));

                    value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

                    _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                            .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_VOLUME_CATTEGORY_ROOT, context))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                } else
                    _value.append(title);
            }
            String ringtoneValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
            if ((!ActivateProfileHelper.getMergedRingNotificationVolumes() || ApplicationPreferences.applicationUnlinkRingerNotificationVolumes) &&
                    getEnableVolumeNotificationByRingtone(ringtoneValue)) {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, R.string.profile_preferences_volumeNotification, context);
                if (!title.isEmpty()) {
                    cattegorySummaryData.bold = true;
                    if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                    if (audioManager != null) {
                        String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_NOTIFICATION,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_NOTIFICATION));

                        value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

                        _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_VOLUME_CATTEGORY_ROOT, context))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    } else
                        _value.append(title);
                }
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_MEDIA, R.string.profile_preferences_volumeMedia, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_MEDIA,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_MEDIA));

                    value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                    _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                            .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_VOLUME_CATTEGORY_ROOT, context))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                } else
                    _value.append(title);
            }
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ALARM, R.string.profile_preferences_volumeAlarm, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            if (audioManager != null) {
                String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_ALARM,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_ALARM));

                value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_VOLUME_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
            else
                _value.append(title);
        }
        if (!isMuteEnabled) {
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_SYSTEM, R.string.profile_preferences_volumeSystem, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_SYSTEM,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_SYSTEM));

                    value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);

                    _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                            .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_VOLUME_CATTEGORY_ROOT, context))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                } else
                    _value.append(title);
            }
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_VOICE, R.string.profile_preferences_volumeVoiceCall, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            if (audioManager != null) {
                String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_VOICE,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_VOICE));

                value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);

                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_VOLUME_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
            else
                _value.append(title);
        }
        if (!isMuteEnabled) {
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_DTMF, R.string.profile_preferences_volumeDTMF, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_DTMF,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_DTMF));

                    value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF);

                    _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                            .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_VOLUME_CATTEGORY_ROOT, context))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                } else
                    _value.append(title);
            }
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY, R.string.profile_preferences_volumeAccessibility, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            if ((audioManager != null)) {
                String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY));

                value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY);

                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_VOLUME_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
            else
                _value.append(title);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO, R.string.profile_preferences_volumeBluetoothSCO, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            if (audioManager != null) {
                String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO));

                value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO);

                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_VOLUME_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
            else
                _value.append(title);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, R.string.profile_preferences_volumeSpeakerPhone, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0)
                _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE)),
                    R.array.volumeSpeakerPhoneValues, R.array.volumeSpeakerPhoneArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_VOLUME_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }

        cattegorySummaryData.summary = _value.toString();

        Profile profile = new Profile();
        profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, "0"));
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.isEmpty();

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummarySounds(Context context,
                                             Preference preferenceScreen,
                                             CattegorySummaryData cattegorySummaryData,
                                             int phoneCount) {

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, R.string.profile_preferences_soundRingtoneChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(TAG_RINGTONE_NAME, prefMng, PREF_PROFILE_SOUNDS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_RINGTONE);
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, R.string.profile_preferences_soundNotificationChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0)
                _value.append(StringConstants.STR_BULLET);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(TAG_NOTIFICATION_NAME, prefMng, PREF_PROFILE_SOUNDS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, R.string.profile_preferences_soundAlarmChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0)
                _value.append(StringConstants.STR_BULLET);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(TAG_ALARM_NAME, prefMng, PREF_PROFILE_SOUNDS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_ALARM);

        //_permissionGranted = true;

        boolean isDualSIM = (phoneCount > 1);

        if (isDualSIM &&
                ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                        (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                        (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                        (PPApplication.deviceIsOnePlus))) {
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, R.string.profile_preferences_soundRingtoneChangeSIM1, context);
            if (!title.isEmpty()) {
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                cattegorySummaryData.bold = true;
                _value.append(title);
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, R.string.profile_preferences_soundRingtoneChangeSIM2, context);
            if (!title.isEmpty()) {
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                cattegorySummaryData.bold = true;
                _value.append(title);
            }

            if ((PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                    PPApplication.deviceIsOnePlus) {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, R.string.profile_preferences_soundSameRingtoneForBothSIMCards, context);
                if (!title.isEmpty()) {
                    if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                    cattegorySummaryData.bold = true;
                    _value.append(title);
                }
            }

            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, R.string.profile_preferences_soundNotificationChangeSIM1, context);
            if (!title.isEmpty()) {
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                cattegorySummaryData.bold = true;
                _value.append(title);
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, R.string.profile_preferences_soundNotificationChangeSIM2, context);
            if (!title.isEmpty()) {
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                cattegorySummaryData.bold = true;
                _value.append(title);
            }

            Profile profile = new Profile();
            profile._soundRingtoneChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, "0"));
            profile._soundRingtoneChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, "0"));
            profile._soundNotificationChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, "0"));
            profile._soundNotificationChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, "0"));
            ArrayList<PermissionType> permissions = new ArrayList<>();
            Permissions.checkProfileRingtones(context, profile, permissions);
            cattegorySummaryData.permissionGranted = permissions.isEmpty();

            if (cattegorySummaryData.bold) {
                //noinspection ConstantConditions
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, cattegorySummaryData.bold, false, false,
                        (!cattegorySummaryData.permissionGranted) ||
                        notGrantedG1Permission ||
                        notRootedOrGrantetRoot ||
                        notInstalledPPPPS ||
                        notGrantedShizuku, false);
            }
        }

        cattegorySummaryData.summary = _value.toString();

        Profile profile = new Profile();
        profile._soundRingtoneChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, "0"));
        profile._soundNotificationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0"));
        profile._soundAlarmChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, "0"));
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileRingtones(context, profile, permissions);
        cattegorySummaryData.permissionGranted = cattegorySummaryData.permissionGranted && (permissions.isEmpty());

        if (cattegorySummaryData.bold) {
            setProfileSoundsPreferenceSummary(cattegorySummaryData.summary,
                    preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE)),
                    preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION)),
                    preferences.getString(Profile.PREF_PROFILE_SOUND_ALARM,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ALARM)),
                    preferenceScreen, context);

            GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, cattegorySummaryData.bold, false, false, !cattegorySummaryData.permissionGranted, false);
        }

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryTouchEffects(Context context,
                                                   CattegorySummaryData cattegorySummaryData) {

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_ON_TOUCH, R.string.profile_preferences_soundOnTouch, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_SOUND_ON_TOUCH,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ON_TOUCH)),
                    R.array.soundOnTouchValues, R.array.soundOnTouchArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_TOUCH_EFFECTS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, R.string.profile_preferences_vibrationOnTouch, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH)),
                    R.array.vibrationOnTouchValues, R.array.vibrationOnTouchArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_TOUCH_EFFECTS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, R.string.profile_preferences_dtmfToneWhenDialing, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING)),
                    R.array.dtmfToneWhenDialingValues, R.array.dtmfToneWhenDialingArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_TOUCH_EFFECTS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }

        cattegorySummaryData.summary = _value.toString();

        Profile profile = new Profile();
        profile._soundOnTouch = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ON_TOUCH, "0"));
        profile._vibrationOnTouch = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, "0"));
        profile._dtmfToneWhenDialing = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, "0"));
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileSoundOnTouch(context, profile, permissions);
        Permissions.checkProfileVibrationOnTouch(context, profile, permissions);
        Permissions.checkProfileDtmfToneWhenDialing(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.isEmpty();

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryVibrationIntensity(Context context,
                                                         CattegorySummaryData cattegorySummaryData) {
        PreferenceAllowed _preferenceAllowed = new PreferenceAllowed();
        _preferenceAllowed.isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY( context);

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        if (_preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING, R.string.profile_preferences_vibrationIntensityRinging, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING));

                value = ProfileStatic.getVolumeValue(value) + "/" + VibrationIntensityPreference.getMaxValue(VibrationIntensityPreference.RINGING_VYBRATION_INTENSITY_TYPE);

                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_VIBRATION_INTENSITY_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, R.string.profile_preferences_vibrationIntensityNotificatiions, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS));

                value = ProfileStatic.getVolumeValue(value) + "/" + VibrationIntensityPreference.getMaxValue(VibrationIntensityPreference.NOTIFICATIONS_VYBRATION_INTENSITY_TYPE);

                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_VIBRATION_INTENSITY_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, R.string.profile_preferences_vibrationIntensityTouchInteraction, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION));

                value = ProfileStatic.getVolumeValue(value) + "/" + VibrationIntensityPreference.getMaxValue(VibrationIntensityPreference.TOUCHINTERACTION_VYBRATION_INTENSITY_TYPE);

                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_VIBRATION_INTENSITY_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }

            cattegorySummaryData.summary = _value.toString();

            /*
            Profile profile = new Profile();
            profile._vibrationIntensityRinging = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING, "-1|1");
            profile._vibrationIntensityNotifications = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, "-1|1");
            profile._vibrationIntensityTouchInteraction = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, "-1|1");
            ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
            Permissions.checkProfileVibrationIntensityForSamsung(context, profile, permissions);
            cattegorySummaryData.permissionGranted = permissions.size() == 0;
            */

        } else {
            // remove "Vibration intensity", because is not allowed for non-Samsung, non-OnePLus devices and API < 33
            Preference preference = prefMng.findPreference(PREF_PROFILE_VIBRATION_INTENSITY_CATTEGORY_ROOT);
            if (preference != null) {
                //preference.setVisible(false);
                preference.setEnabled(false);
            }
            cattegorySummaryData.summary = _preferenceAllowed.getNotAllowedPreferenceReasonString(context);
            cattegorySummaryData.bold = false;
            cattegorySummaryData.forceSet = true;
        }

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryRadios(Context context,
                                             CattegorySummaryData cattegorySummaryData,
                                             TelephonyManager telephonyManager, int phoneCount) {

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, R.string.profile_preferences_deviceAirplaneMode, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE)),
                    R.array.airplaneModeValues, R.array.airplaneModeArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);

            Profile profile = new Profile();
            profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, "0"));
            ArrayList<PermissionType> permissions = new ArrayList<>();
            Permissions.checkProfileMicrophone(context, profile, permissions);
            cattegorySummaryData.permissionGranted = permissions.isEmpty();

            if ((profile._deviceAirplaneMode >= 4)) {
                // change only when default assistant is false, becuse may be checked also for another
                // profile parameters
                boolean defaultAssistantSet = ActivateProfileHelper.isPPPSetAsDefaultAssistant(context);
                if (!defaultAssistantSet)
                    cattegorySummaryData.defaultAssistantSet = false;
            }
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AUTOSYNC, R.string.profile_preferences_deviceAutosync, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOSYNC,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AUTOSYNC)),
                    R.array.hardwareModeValues, R.array.hardwareModeArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, R.string.profile_preferences_deviceMobileData_21, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)),
                    R.array.hardwareModeValues, R.array.hardwareModeArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, R.string.profile_preferences_deviceMobileDataPrefs, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS)),
                    R.array.mobileDataPrefsValues, R.array.mobileDataPrefsArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }

        //_permissionGranted = true;
        boolean isDualSIM = (phoneCount > 1);

        if (isDualSIM) {
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, R.string.profile_preferences_deviceOnOff_SIM1, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                _value.append(title);
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, R.string.profile_preferences_deviceOnOff_SIM2, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                _value.append(title);
            }

            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, R.string.profile_preferences_deviceDefaultSIM, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                _value.append(title);
            }

            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, R.string.profile_preferences_deviceNetworkTypeSIM1, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                _value.append(title);
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, R.string.profile_preferences_deviceNetworkTypeSIM2, context);
            //PPApplicationStatic.logE("[DUAL_SIM] ProfilesPrefsFragment.setCategorySummary", "PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2 - notGrantedG1Permission="+notGrantedG1Permission);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                _value.append(title);
            }

            cattegorySummaryData.summary = _value.toString();

            Profile profile = new Profile();
            profile._deviceDefaultSIMCards = preferences.getString(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, "0|0|0");
            //profile._deviceMobileDataSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, "0"));
            //profile._deviceMobileDataSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, "0"));
            profile._deviceNetworkTypeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, "0"));
            profile._deviceNetworkTypeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, "0"));
            profile._deviceOnOffSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, "0"));
            profile._deviceOnOffSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, "0"));
            ArrayList<PermissionType> permissions = new ArrayList<>();
            Permissions.checkProfileRadioPreferences(context, profile, permissions);
            //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
            cattegorySummaryData.permissionGranted = permissions.isEmpty();

        }

        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI, R.string.profile_preferences_deviceWiFi, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WIFI)),
                    R.array.wifiModeValues, R.array.wifiModeArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, R.string.profile_preferences_deviceConnectToSSID, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID));
            if (value != null) {
                if (value.equals(StringConstants.CONNECTTOSSID_JUSTANY))
                    value = "[" + StringConstants.CHAR_HARD_SPACE + getString(R.string.connect_to_ssid_pref_dlg_summary_text_just_any) + StringConstants.CHAR_HARD_SPACE + "]";
                else
                    value = value.replace("\"", "");

                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
            else
                _value.append(title);
        }
        //if (Build.VERSION.SDK_INT < 30) {
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI_AP, R.string.profile_preferences_deviceWiFiAP, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                String value = StringFormatUtils.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WIFI_AP)),
                        R.array.wifiAPValues, R.array.wifiAPArray, context);

                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
        //}
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, R.string.profile_preferences_deviceWiFiAPPrefs, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS)),
                    R.array.wiFiAPPrefsValues, R.array.wiFiAPPrefsArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, R.string.profile_preferences_deviceBluetooth, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_BLUETOOTH,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_BLUETOOTH)),
                    R.array.hardwareModeValues, R.array.hardwareModeArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE, R.string.profile_preferences_deviceLocationMode, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE)),
                    R.array.locationModeValues, R.array.locationModeArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_GPS, R.string.profile_preferences_deviceGPS, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_GPS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_GPS)),
                    R.array.hardwareModeValues, R.array.hardwareModeArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, R.string.profile_preferences_deviceLocationServicePrefs, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS)),
                    R.array.locationServicePrefsValues, R.array.locationServicePrefsArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NFC, R.string.profile_preferences_deviceNFC, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_NFC,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NFC)),
                    R.array.hardwareModeValues, R.array.hardwareModeArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, R.string.profile_preferences_deviceNetworkType, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            //final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)),
                    arrayValues, arrayStrings, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, R.string.profile_preferences_deviceNetworkTypePrefs, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS)),
                    R.array.networkTypePrefsValues, R.array.networkTypePrefsArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS, R.string.profile_preferences_deviceVPNSettingsPrefs, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS)),
                    R.array.vpnSettingsPrefsValues, R.array.vpnSettingsPrefsArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_VPN, R.string.profile_preferences_deviceVPN, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_VPN,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_VPN));

            int vpnApplication;

            String[] splits = null;
            if (value != null)
                splits = value.split(StringConstants.STR_SPLIT_REGEX);

            try {
                if (splits != null)
                    vpnApplication = Integer.parseInt(splits[0]);
                else
                    vpnApplication = 0;
            } catch (Exception e) {
                //Log.e("ProfilesPrefsFragment.setCategorySummaryRadios", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
                vpnApplication = 0;
            }

            String[] entries = getResources().getStringArray(R.array.vpnApplicationArray);
            String[] entryValues = getResources().getStringArray(R.array.vpnApplicationValues);

            int applicaitonIdx = 0;
            for (String entryValue : entryValues) {
                if (entryValue.equals(String.valueOf(vpnApplication))) {
                    break;
                }
                ++applicaitonIdx;
            }
            value = entries[applicaitonIdx];

            boolean enableVPN = true;
            try {
                enableVPN = (splits != null) && Integer.parseInt(splits[1]) == 0;
            } catch (Exception e) {
                //Log.e("ProfilesPrefsFragment.setCategorySummaryRadios", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            }
            if (enableVPN)
                value = value + "; " + getString(R.string.vpn_profile_pref_dlg_enable_vpn);
            else
                value = value + "; " + getString(R.string.vpn_profile_pref_dlg_disable_vpn);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }

        cattegorySummaryData.summary = _value.toString();

        Profile profile = new Profile();
        profile._deviceWiFiAP = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP, "0"));
        profile._deviceBluetooth = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, "0"));
        profile._deviceMobileData = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, "0"));
        profile._deviceNetworkType = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, "0"));
        profile._deviceConnectToSSID = preferences.getString(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, StringConstants.CONNECTTOSSID_JUSTANY);
        profile._deviceNetworkTypePrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, "0"));
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileRadioPreferences(context, profile, permissions);
        //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
        cattegorySummaryData.permissionGranted = cattegorySummaryData.permissionGranted && (permissions.isEmpty());

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryScreen(Context context,
                                             CattegorySummaryData cattegorySummaryData) {

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, R.string.profile_preferences_deviceScreenTimeout, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT)),
                    R.array.screenTimeoutValues, R.array.screenTimeoutArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, R.string.profile_preferences_deviceBrightness, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS));
            boolean automatic = ProfileStatic.getDeviceBrightnessAutomatic(value);
            boolean changeLevel = ProfileStatic.getDeviceBrightnessChangeLevel(value);
            int iValue = ProfileStatic.getDeviceBrightnessValue(value);

            //boolean adaptiveAllowed =
            //        (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS, null, preferences, true, context).allowed
            //                == PreferenceAllowed.PREFERENCE_ALLOWED);

            String summaryString;
            if (automatic)
            {
                summaryString = context.getString(R.string.preference_profile_adaptiveBrightness);
            } else
                summaryString = context.getString(R.string.preference_profile_manual_brightness);

            if (changeLevel /*&& (adaptiveAllowed || !automatic)*/) {
                String __value = iValue + "/100";
                summaryString = summaryString + "; " + __value;
            }

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(summaryString, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, R.string.profile_preferences_deviceAutoRotation,context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOROTATE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AUTOROTATE)),
                    R.array.displayRotationValues, R.array.displayRotationArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT, R.string.profile_preferences_deviceScreenOnPermanent, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT)),
                    R.array.screenOnPermanentValues, R.array.screenOnPermanentArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SCREEN_ON_OFF, R.string.profile_preferences_deviceScreenOnOff, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_SCREEN_ON_OFF,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SCREEN_ON_OFF)),
                    R.array.screenOnOffValues, R.array.screenOnOffArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_KEYGUARD, R.string.profile_preferences_deviceKeyguard, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_KEYGUARD,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_KEYGUARD)),
                    R.array.keyguardValues, R.array.keyguardArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, R.string.profile_preferences_deviceWallpaperChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String wallpaperChangeValue = preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE));

            String sValue = StringFormatUtils.getListPreferenceString(wallpaperChangeValue,
                    R.array.changeWallpaperValues, R.array.changeWallpaperArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(sValue, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);

            if ((wallpaperChangeValue != null) &&
                    (wallpaperChangeValue.equals("1") ||
                     wallpaperChangeValue.equals("3"))) {
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                String value = StringFormatUtils.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR)),
                        R.array.wallpaperForValues, R.array.wallpaperForArray, context);

                _value.append(context.getString(R.string.profile_preferences_deviceWallpaperFor)).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_LOCK_DEVICE, R.string.profile_preferences_lockDevice, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_LOCK_DEVICE)),
                    R.array.lockDeviceValues, R.array.lockDeviceArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, R.string.profile_preferences_headsUpNotifications, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS)),
                    R.array.headsUpNotificationsValues, R.array.headsUpNotificationsArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, R.string.profile_preferences_alwaysOnDisplay, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY)),
                    R.array.alwaysOnDisplayValues, R.array.alwaysOnDisplayArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }

        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SCREEN_DARK_MODE, R.string.profile_preferences_screenDarkMode, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_SCREEN_DARK_MODE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SCREEN_DARK_MODE)),
                    R.array.screenDarkModeValues, R.array.screenDarkModeArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }

        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT, ProfileStatic.getNightLightStringId(), context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT)),
                    R.array.screenNightLightValues, R.array.screenNightLightArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT_PREFS, ProfileStatic.getNightLightStringPrefsId(), context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT_PREFS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT_PREFS)),
                    R.array.screenNightLightPrefsValues, R.array.screenNightLightPrefsArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }

        cattegorySummaryData.summary = _value.toString();

        Profile profile = new Profile();
        profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, "0"));
        profile._screenOnPermanent = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT, "0"));
        profile._deviceBrightness = preferences.getString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, "");
        profile._deviceAutoRotate = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, "0"));
        profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, "0"));
        profile._alwaysOnDisplay = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, "0"));
        profile._screenNightLight = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT, "0"));
        profile._screenOnOff = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_ON_OFF, "0"));
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileScreenTimeout(context, profile, permissions);
        Permissions.checkProfileScreenOnPermanent(context, profile, permissions);
        Permissions.checkProfileScreenBrightness(context, profile, permissions);
        Permissions.checkProfileAutoRotation(context, profile, permissions);
        Permissions.checkProfileImageWallpaper(context, profile, permissions);
        Permissions.checkProfileWallpaperFolder(context, profile, permissions);
        Permissions.checkProfileAlwaysOnDisplay(context, profile, permissions);
        Permissions.checkProfileScreenNightLight(context, profile, permissions);
        Permissions.checkProfileScreenOnOff(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.isEmpty();

        profile._lockDevice = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, "0"));
        cattegorySummaryData.accessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false) == 1;

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryLedAccessories(Context context,
                                                     CattegorySummaryData cattegorySummaryData) {

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_NOTIFICATION_LED, R.string.profile_preferences_notificationLed, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            //if (!summary.isEmpty()) summary = summary +" • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_NOTIFICATION_LED,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_NOTIFICATION_LED)),
                    R.array.notificationLedValues, R.array.notificationLedArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_LED_ACCESSORIES_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_CAMERA_FLASH, R.string.profile_preferences_cameraFlash, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_CAMERA_FLASH,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_CAMERA_FLASH)),
                    R.array.cameraFlashValues, R.array.cameraFlashArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_LED_ACCESSORIES_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }

        cattegorySummaryData.summary = _value.toString();

        Profile profile = new Profile();
        profile._notificationLed = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_NOTIFICATION_LED, "0"));
        profile._cameraFlash = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_CAMERA_FLASH, "0"));
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileNotificationLed(context, profile, permissions);
        Permissions.checkProfileCameraFlash(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.isEmpty();

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummarySendSMS(Context context,
                                             CattegorySummaryData cattegorySummaryData) {

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        //int contactListType = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_PHONE_CALLS_CONTACT_LIST_TYPE,
        //        Profile.defaultValuesString.get(Profile.PREF_PROFILE_PHONE_CALLS_CONTACT_LIST_TYPE)));
        String contactGroupsValue = preferences.getString(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS,
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS));
        String contactsValue = preferences.getString(Profile.PREF_PROFILE_SEND_SMS_CONTACTS,
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_SEND_SMS_CONTACTS));
        boolean sendSMS = false;

        if (
            /*(contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) ||*/
                ((contactGroupsValue != null) && (!contactGroupsValue.isEmpty())) ||
                        ((contactsValue != null) && (!contactsValue.isEmpty()))
        ) {
            String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS, R.string.profile_preference_sendSMSSendSMS, context);
            if (!title.isEmpty()) {
                sendSMS = true;
                cattegorySummaryData.bold = true;
                _value.append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(title, prefMng, PREF_PROFILE_SEND_SMS_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
        }

        if (sendSMS) {
            String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS, R.string.profile_preference_sendSMSContactGroups, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                contactGroupsValue = ContactGroupsMultiSelectDialogPreference.getSummary(contactGroupsValue, context);

                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(contactGroupsValue, prefMng, PREF_PROFILE_SEND_SMS_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);

            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SEND_SMS_CONTACTS, R.string.profile_preference_sendSMSContacts, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                contactsValue = ContactsMultiSelectDialogPreference.getSummary(contactsValue, false, context);

                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(contactsValue, prefMng, PREF_PROFILE_SEND_SMS_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);

            }
        }

        cattegorySummaryData.summary = _value.toString();

        Profile profile = new Profile();
        profile._sendSMSContacts = preferences.getString(Profile.PREF_PROFILE_SEND_SMS_CONTACTS, "");
        profile._sendSMSContactGroups = preferences.getString(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS, "");
        profile._sendSMSSendSMS = preferences.getBoolean(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS, false);
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileSendSMS(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.isEmpty();

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryOthers(Context context,
                                             CattegorySummaryData cattegorySummaryData) {

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, R.string.profile_preferences_devicePowerSaveMode, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE)),
                    R.array.hardwareModeValues, R.array.hardwareModeArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_OTHERS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, R.string.profile_preferences_deviceRunApplicationsShortcutsChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME));
            if ((value != null) &&
                    (!value.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME)))) {
                String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);

                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(context.getString(R.string.applications_multiselect_summary_text_selected) + " " + splits.length, prefMng, PREF_PROFILE_OTHERS_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
            else
                _value.append(title);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, R.string.profile_preferences_deviceCloseAllApplications, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS)),
                    R.array.closeAllApplicationsValues, R.array.closeAllApplicationsArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_OTHERS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, R.string.profile_preferences_deviceForceStopApplicationsChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME));
            if ((value != null) &&
                    (!value.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME)))) {
                String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);

                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(context.getString(R.string.applications_multiselect_summary_text_selected) + " " + splits.length, prefMng, PREF_PROFILE_OTHERS_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
            else
                _value.append(title);

            Profile profile = new Profile();
            profile._deviceForceStopApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, "0"));
            cattegorySummaryData.accessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false) == 1;
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS, R.string.profile_preference_sendSMSSendSMS, context);
        if (!title.isEmpty()) {
            String contactGroupsValue = preferences.getString(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS));
            String contactsValue = preferences.getString(Profile.PREF_PROFILE_SEND_SMS_CONTACTS,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_SEND_SMS_CONTACTS));

            if (
                /*(contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) ||*/
                    ((contactGroupsValue != null) && (!contactGroupsValue.isEmpty())) ||
                            ((contactsValue != null) && (!contactsValue.isEmpty()))
            ) {
                cattegorySummaryData.bold = true;
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                _value.append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(title, prefMng, PREF_PROFILE_SEND_SMS_CATTEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
        }

        cattegorySummaryData.summary = _value.toString();

        Profile profile = new Profile();
        profile._deviceCloseAllApplications = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, "0"));
        profile._deviceRunApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, "0"));
        profile._sendSMSContacts = preferences.getString(Profile.PREF_PROFILE_SEND_SMS_CONTACTS, "");
        profile._sendSMSContactGroups = preferences.getString(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS, "");
        profile._sendSMSSendSMS = preferences.getBoolean(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS, false);
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileCloseAllApplications(context, profile, permissions);
        Permissions.checkProfileRunApplications(context, profile, permissions);
        Permissions.checkProfileSendSMS(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.isEmpty();

        profile._deviceForceStopApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, "0"));
        if (profile._deviceForceStopApplicationChange == 1)
            cattegorySummaryData.accessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false) == 1;

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryForceStopApplications(Context context,
                                                            CattegorySummaryData cattegorySummaryData) {

        Profile profile = new Profile();

        StringBuilder _value = new StringBuilder(); // must be empty for this cattegory

        //String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, R.string.profile_preferences_deviceForceStopApplicationsChange, false, context);
        String title = context.getString(R.string.profile_preferences_deviceForceStopApplicationsChange);
        int index = 0;
        String defaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
        String sValue = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, defaultValue);
        String[] entryValues = getResources().getStringArray(R.array.forceStopApplicationValues);
        for (String v : entryValues) {
            if (v.equals(sValue))
                break;
            index++;
        }
        String[] entries = getResources().getStringArray(R.array.forceStopApplicationArray);
        _value.append(title).append(": ").append(
                ((index >= 0) ? StringConstants.TAG_BOLD_START_HTML +
                        ProfileStatic.getColorForChangedPreferenceValue(entries[index], prefMng, PREF_FORCE_STOP_APPLICATIONS_CATEGORY_ROOT, context)
                        + StringConstants.TAG_BOLD_END_HTML : null)
        );

        boolean ok = true;
        if ("1".equals(sValue)) {
            int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
            if (extenderVersion == 0) {
                _value.append(getString(R.string.profile_preferences_device_not_allowed))
                        .append(": ").append(getString(R.string.preference_not_allowed_reason_not_extender_installed));
                ok = false;
            } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED) {
                _value.append(getString(R.string.profile_preferences_device_not_allowed))
                        .append(": ").append(getString(R.string.preference_not_allowed_reason_extender_not_upgraded));
                ok = false;
            } else if (!PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, false, true
                    /*, "ProfilesPrefsFragment.setCategorySummaryForceStopApplications"*/)) {
                _value.append(getString(R.string.profile_preferences_device_not_allowed))
                        .append(": ").append(getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender));
                ok = false;
            } else if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                _value.append(getString(R.string.profile_preferences_device_not_allowed))
                        .append(": ").append(getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined));
                ok = false;
            }
            cattegorySummaryData.accessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false) == 1;
        } else
        if ("2".equals(sValue)) {
            PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
            preferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION(
                    Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE,
                    null, preferences);
            ok = preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED;
            notRootedOrGrantetRoot = preferenceAllowed.notAllowedRoot;
            notGrantedShizuku = preferenceAllowed.notAllowedShizuku;
        }

        if (ok) {
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, R.string.profile_preferences_deviceForceStopApplicationsPackageName, context);
            defaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
            sValue = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, defaultValue);
            _value.append(StringConstants.STR_BULLET).append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                            .append(
                                ProfileStatic.getColorForChangedPreferenceValue(
                                        ApplicationsMultiSelectDialogPreference.getSummaryForPreferenceCategory(sValue, "accessibility_2.0", context, false),
                                        prefMng, PREF_FORCE_STOP_APPLICATIONS_CATEGORY_ROOT, context)
                            ).append(StringConstants.TAG_BOLD_END_HTML);
        }

        cattegorySummaryData.summary = _value.toString();

        cattegorySummaryData.bold = (index > 0);
        cattegorySummaryData.forceSet = true;

        profile._deviceForceStopApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, "0"));

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryLockDevice(Context context,
                                                 CattegorySummaryData cattegorySummaryData) {

        StringBuilder _value = new StringBuilder(); // must be empty for this cattegory

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_LOCK_DEVICE, R.string.profile_preferences_lockDevice, context);
        if (!title.isEmpty()) {
            _value.append(title).append(": ");
        }

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
        if (index == 0)
            _value.append(ProfileStatic.getColorForChangedPreferenceValue(entries[index], prefMng, PREF_LOCK_DEVICE_CATEGORY_ROOT, context));
        else
            _value.append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(entries[index], prefMng, PREF_LOCK_DEVICE_CATEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);

        if ((sValue != null) && sValue.equals("3")) {
            int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
            if (extenderVersion == 0) {
                //ok = false;
                _value.append(getString(R.string.profile_preferences_device_not_allowed))
                        .append(": ").append(getString(R.string.preference_not_allowed_reason_not_extender_installed));
            } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED) {
                //ok = false;
                _value.append(getString(R.string.profile_preferences_device_not_allowed))
                        .append(": ").append(getString(R.string.preference_not_allowed_reason_extender_not_upgraded));
            } else if (!PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, false, true
                    /*, "ProfilesPrefsFragment.setCategorySummaryLockDevice"*/)) {
                //ok = false;
                _value.append(getString(R.string.profile_preferences_device_not_allowed))
                        .append(": ").append(getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender));
            } else if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                //ok = false;
                _value.append(getString(R.string.profile_preferences_device_not_allowed))
                        .append(": ").append(getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined));
            }
        }

        cattegorySummaryData.summary = _value.toString();

        cattegorySummaryData.bold = (index > 0);
        cattegorySummaryData.forceSet = true;

        Profile profile = new Profile();
        profile._lockDevice = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, "0"));
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileLockDevice(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.isEmpty();

        cattegorySummaryData.accessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false) == 1;

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryApplication(Context context,
                                                  CattegorySummaryData cattegorySummaryData) {

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_ENABLE_WIFI_SCANNING, R.string.profile_preferences_applicationEnableWifiScanning, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;

            String enabledValue = preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_WIFI_SCANNING,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_ENABLE_WIFI_SCANNING));
            String value = StringFormatUtils.getListPreferenceString(enabledValue,
                    R.array.applicationEnableScanningValues, R.array.applicationEnableScanningArray, context);

            if ((enabledValue != null) && enabledValue.equals("3")) {
                String interval = preferences.getString(Profile.PREF_PROFILE_APPLICATION_WIFI_SCAN_INTERVAL, "");
                value = value + ": "+interval;
            }
            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_APPLICATION_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_ENABLE_BLUETOOTH_SCANNING, R.string.profile_preferences_applicationEnableBluetoothScanning, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String enabledValue = preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_BLUETOOTH_SCANNING,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_ENABLE_BLUETOOTH_SCANNING));
            String value = StringFormatUtils.getListPreferenceString(enabledValue,
                    R.array.applicationEnableScanningValues, R.array.applicationEnableScanningArray, context);

            if ((enabledValue != null) && enabledValue.equals("3")) {
                String interval = preferences.getString(Profile.PREF_PROFILE_APPLICATION_BLUETOOTH_SCAN_INTERVAL, "");
                String duration = preferences.getString(Profile.PREF_PROFILE_APPLICATION_BLUETOOTH_LE_SCAN_DURATION, "");
                value = value + ": "+interval+", "+duration;
            }
            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_APPLICATION_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_ENABLE_LOCATION_SCANNING, R.string.profile_preferences_applicationEnableLocationScanning,context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String enabledValue = preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_LOCATION_SCANNING,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_ENABLE_LOCATION_SCANNING));
            String value = StringFormatUtils.getListPreferenceString(enabledValue,
                    R.array.applicationEnableScanningValues, R.array.applicationEnableScanningArray, context);

            if ((enabledValue != null) && enabledValue.equals("3")) {
                String interval = preferences.getString(Profile.PREF_PROFILE_APPLICATION_LOCATION_UPDATE_INTERVAL, "");
                value = value + ": "+interval;
            }

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_APPLICATION_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_ENABLE_MOBILE_CELL_SCANNING, R.string.profile_preferences_applicationEnableMobileCellScanning, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String enabledValue = preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_MOBILE_CELL_SCANNING,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_ENABLE_MOBILE_CELL_SCANNING));
            String value = StringFormatUtils.getListPreferenceString(enabledValue,
                    R.array.applicationEnableScanningValues, R.array.applicationEnableScanningArray, context);

            // interval not exists for monile cells scanning

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_APPLICATION_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_ENABLE_ORIENTATION_SCANNING, R.string.profile_preferences_applicationEnableOrientationScanning, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String enabledValue = preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_ORIENTATION_SCANNING,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_ENABLE_ORIENTATION_SCANNING));
            String value = StringFormatUtils.getListPreferenceString(enabledValue,
                    R.array.applicationEnableScanningValues, R.array.applicationEnableScanningArray, context);

            if ((enabledValue != null) && enabledValue.equals("3")) {
                String interval = preferences.getString(Profile.PREF_PROFILE_APPLICATION_ORIENTATION_SCAN_INTERVAL, "");
                value = value + ": "+interval;
            }

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_APPLICATION_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_ENABLE_NOTIFICATION_SCANNING, R.string.profile_preferences_applicationEnableNotificationScanning, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String enabledValue = preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_NOTIFICATION_SCANNING,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_ENABLE_NOTIFICATION_SCANNING));
            String value = StringFormatUtils.getListPreferenceString(enabledValue,
                    R.array.applicationEnableScanningValues, R.array.applicationEnableScanningArray, context);

            // interval not exists for notification scanning

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_APPLICATION_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_ENABLE_PERIODIC_SCANNING, R.string.profile_preferences_applicationEnablePeriodicScanning, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String enabledValue = preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_PERIODIC_SCANNING,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_ENABLE_PERIODIC_SCANNING));
            String value = StringFormatUtils.getListPreferenceString(enabledValue,
                    R.array.applicationEnableScanningValues, R.array.applicationEnableScanningArray, context);

            if ((enabledValue != null) && enabledValue.equals("3")) {
                String interval = preferences.getString(Profile.PREF_PROFILE_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL, "");
                value = value + ": "+interval;
            }

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_APPLICATION_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }

        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN, R.string.profile_preferences_applicationEnableGlobalEventsRun, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN)),
                    R.array.applicationDisableGlobalEventsRunValues, R.array.applicationDisableGlobalEventsRunArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_APPLICATION_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }

        cattegorySummaryData.summary = _value.toString();

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryRadiosDualSIMSupport(Context context,
                                                           Preference preferenceScreen,
                                                           CattegorySummaryData cattegorySummaryData,
                                                           TelephonyManager telephonyManager, int phoneCount) {
        boolean isDualSIM = true;
        if (telephonyManager != null) {
            if (phoneCount < 2) {
                preferenceScreen.setVisible(false);
                isDualSIM = false;
            }
            if (isDualSIM) {

                    StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

                    String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, R.string.profile_preferences_deviceOnOff_SIM1, context);
                    if (!title.isEmpty()) {
                        cattegorySummaryData.bold = true;
                        //if (!summary.isEmpty()) summary = summary + " • ";

                        String value = StringFormatUtils.getListPreferenceString(
                                preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1)),
                                R.array.onOffSIMValues, R.array.onOffSIMArray, context);

                        _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_DEVICE_RADIOS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }
                    title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, R.string.profile_preferences_deviceOnOff_SIM2, context);
                    if (!title.isEmpty()) {
                        cattegorySummaryData.bold = true;
                        if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                        String value = StringFormatUtils.getListPreferenceString(
                                preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2)),
                                R.array.onOffSIMValues, R.array.onOffSIMArray, context);

                        _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_DEVICE_RADIOS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }

                    title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, R.string.profile_preferences_deviceDefaultSIM, context);
                    if (!title.isEmpty()) {
                        cattegorySummaryData.bold = true;
                        if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                        String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS));

                        String voiceStr = "";
                        String smsStr = "";
                        String dataStr = "";
                        if (value != null) {
                            String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
                            try {
                                String[] arrayStrings = context.getResources().getStringArray(R.array.defaultSIMVoiceArray);
                                int index = Integer.parseInt(splits[0]);
                                voiceStr = arrayStrings[index];
                            } catch (Exception ignored) {
                            }
                            try {
                                String[] arrayStrings = context.getResources().getStringArray(R.array.defaultSIMSMSArray);
                                int index = Integer.parseInt(splits[1]);
                                smsStr = arrayStrings[index];
                            } catch (Exception ignored) {
                            }
                            try {
                                String[] arrayStrings = context.getResources().getStringArray(R.array.defaultSIMDataArray);
                                int index = Integer.parseInt(splits[2]);
                                dataStr = arrayStrings[index];
                            } catch (Exception ignored) {
                            }
                        }

                        _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                .append(ProfileStatic.getColorForChangedPreferenceValue(
                                        voiceStr + "; " + smsStr + "; " + dataStr,
                                        prefMng, PREF_PROFILE_DEVICE_RADIOS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context)
                                )
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }

                    title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, R.string.profile_preferences_deviceNetworkTypeSIM1, context);
                    //PPApplicationStatic.logE("[DUAL_SIM] ProfilesPrefsFragment.setCategorySummary", "PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1 - notGrantedG1Permission="+notGrantedG1Permission);
                    if (!title.isEmpty()) {
                        cattegorySummaryData.bold = true;
                        if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                        int phoneType;// = TelephonyManager.PHONE_TYPE_GSM;
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

                        String value = StringFormatUtils.getListPreferenceString(
                                preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1)),
                                arrayValues, arrayStrings, context);

                        _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_DEVICE_RADIOS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }
                    title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, R.string.profile_preferences_deviceNetworkTypeSIM2, context);
                    //PPApplicationStatic.logE("[DUAL_SIM] ProfilesPrefsFragment.setCategorySummary", "PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2 - notGrantedG1Permission="+notGrantedG1Permission);
                    if (!title.isEmpty()) {
                        cattegorySummaryData.bold = true;
                        if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                        int phoneType; // = TelephonyManager.PHONE_TYPE_GSM;
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

                        String value = StringFormatUtils.getListPreferenceString(
                                preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2)),
                                arrayValues, arrayStrings, context);

                        _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_DEVICE_RADIOS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }

                    cattegorySummaryData.summary = _value.toString();

                    Profile profile = new Profile();
                    profile._deviceDefaultSIMCards = preferences.getString(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, "0|0|0");
                    //profile._deviceMobileDataSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, "0"));
                    //profile._deviceMobileDataSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, "0"));
                    profile._deviceNetworkTypeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, "0"));
                    profile._deviceNetworkTypeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, "0"));
                    profile._deviceOnOffSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, "0"));
                    profile._deviceOnOffSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, "0"));
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    Permissions.checkProfileRadioPreferences(context, profile, permissions);
                    //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
                    cattegorySummaryData.permissionGranted = permissions.isEmpty();

            }
        }
        else
            preferenceScreen.setVisible(false);

        return false;
    }

    private boolean setCategorySummarySoundsDualSIMSupport(Context context,
                                                           Preference preferenceScreen,
                                                           CattegorySummaryData cattegorySummaryData,
                                                           TelephonyManager telephonyManager, int phoneCount) {
        if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                (PPApplication.deviceIsOnePlus)) {
            boolean isDualSIM = true;
            if (telephonyManager != null) {
                if (phoneCount < 2) {
                    preferenceScreen.setVisible(false);
                    isDualSIM = false;
                }
                if (isDualSIM) {

                        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

                        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, R.string.profile_preferences_soundRingtoneChangeSIM1, context);
                        if (!title.isEmpty()) {
                            cattegorySummaryData.bold = true;
                            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(ProfileStatic.getColorForChangedPreferenceValue(TAG_RINGTONE_NAME_SIM1, prefMng, PREF_PROFILE_SOUNDS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        }
                        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, R.string.profile_preferences_soundRingtoneChangeSIM2, context);
                        if (!title.isEmpty()) {
                            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                            cattegorySummaryData.bold = true;
                            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(ProfileStatic.getColorForChangedPreferenceValue(TAG_RINGTONE_NAME_SIM2, prefMng, PREF_PROFILE_SOUNDS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        }
                        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, R.string.profile_preferences_soundNotificationChangeSIM1, context);
                        if (!title.isEmpty()) {
                            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                            cattegorySummaryData.bold = true;
                            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(ProfileStatic.getColorForChangedPreferenceValue(TAG_NOTIFICATION_NAME_SIM1, prefMng, PREF_PROFILE_SOUNDS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        }
                        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, R.string.profile_preferences_soundNotificationChangeSIM2, context);
                        if (!title.isEmpty()) {
                            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                            cattegorySummaryData.bold = true;
                            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(ProfileStatic.getColorForChangedPreferenceValue(TAG_NOTIFICATION_NAME_SIM2, prefMng, PREF_PROFILE_SOUNDS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        }

                        if ((PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                                PPApplication.deviceIsOnePlus) {
                            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, R.string.profile_preferences_soundSameRingtoneForBothSIMCards, context);
                            if (!title.isEmpty()) {

                                cattegorySummaryData.bold = true;

                                String value = StringFormatUtils.getListPreferenceString(
                                        preferences.getString(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS,
                                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS)),
                                        R.array.soundSameRingtoneForBothSIMCardsValues, R.array.soundSameRingtoneForBothSIMCardsArray, context);

                                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                                        .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_SOUNDS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context))
                                        .append(StringConstants.TAG_BOLD_END_HTML);
                            }
                        }

                    cattegorySummaryData.summary = _value.toString();

                    if (cattegorySummaryData.bold) {
                            setProfileSoundsDualSIMPreferenceSummary(cattegorySummaryData.summary,
                                    preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1)),
                                    preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2)),
                                    preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1)),
                                    preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2)),
                                    preferenceScreen, context);

                            Profile profile = new Profile();
                            profile._soundRingtoneChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, "0"));
                            profile._soundRingtoneChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, "0"));
                            profile._soundNotificationChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, "0"));
                            profile._soundNotificationChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, "0"));
                            ArrayList<PermissionType> permissions = new ArrayList<>();
                            Permissions.checkProfileRingtones(context, profile, permissions);
                            cattegorySummaryData.permissionGranted = permissions.isEmpty();

                            GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, cattegorySummaryData.bold, false, false,
                                    (!cattegorySummaryData.permissionGranted) ||
                                    notGrantedG1Permission ||
                                    notRootedOrGrantetRoot ||
                                    notInstalledPPPPS ||
                                    notGrantedShizuku, false);
                            return true;
                        }

                        Profile profile = new Profile();
                        profile._soundRingtoneChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, "0"));
                        profile._soundRingtoneChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, "0"));
                        profile._soundNotificationChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, "0"));
                        profile._soundNotificationChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, "0"));
                        ArrayList<PermissionType> permissions = new ArrayList<>();
                        Permissions.checkProfileRingtones(context, profile, permissions);
                        cattegorySummaryData.permissionGranted = permissions.isEmpty();

                        GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, cattegorySummaryData.bold, false, false,
                                (!cattegorySummaryData.permissionGranted) ||
                                        notGrantedG1Permission ||
                                        notRootedOrGrantetRoot ||
                                        notInstalledPPPPS ||
                                        notGrantedShizuku, false);
                }
            } else
                preferenceScreen.setVisible(false);
        } else
            preferenceScreen.setVisible(false);

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryDeviceWallpaper(Context context,
                                                      CattegorySummaryData cattegorySummaryData) {

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, R.string.profile_preferences_deviceWallpaperChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String wallpaperChangeValue = preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE));

            String sValue = StringFormatUtils.getListPreferenceString(wallpaperChangeValue,
                    R.array.changeWallpaperValues, R.array.changeWallpaperArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(sValue, prefMng, PREF_DEVICE_WALLPAPER_CATEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);

            if ((wallpaperChangeValue != null) &&
                    (wallpaperChangeValue.equals("1") ||
                     wallpaperChangeValue.equals("3"))) {
                if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

                String value = StringFormatUtils.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR)),
                        R.array.wallpaperForValues, R.array.wallpaperForArray, context);

                _value.append(context.getString(R.string.profile_preferences_deviceWallpaperFor)).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_DEVICE_WALLPAPER_CATEGORY_ROOT, context))
                        .append(StringConstants.TAG_BOLD_END_HTML);
            }
        } else {
            cattegorySummaryData.bold = false;

            int index = 0;
            String defaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
            //Log.e("ProfilePrefsFragment.setCategorySummaryDeviceWallpaper", "defaultValue="+defaultValue);
            String[] entryValues = getResources().getStringArray(R.array.changeWallpaperValues);
            for (String v : entryValues) {
                if (v.equals(defaultValue))
                    break;
                index++;
            }
            //Log.e("ProfilePrefsFragment.setCategorySummaryDeviceWallpaper", "index="+index);
            String[] entries = getResources().getStringArray(R.array.changeWallpaperArray);
            if (index == 0) {
                _value.append(entries[index]);
            }
        }

        cattegorySummaryData.summary = _value.toString();
        cattegorySummaryData.forceSet = true;

        Profile profile = new Profile();
        profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, "0"));
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileImageWallpaper(context, profile, permissions);
        Permissions.checkProfileWallpaperFolder(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.isEmpty();

        cattegorySummaryData.forceSet = true;

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryAirplaneMode(Context context,
                                                   CattegorySummaryData cattegorySummaryData) {

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, R.string.profile_preferences_deviceAirplaneMode, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            //if (!summary.isEmpty()) summary = summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE)),
                    R.array.onOffSIMValues, R.array.onOffSIMArray, context);

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, PREF_PROFILE_DEVICE_RADIOS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        } else {
            cattegorySummaryData.bold = false;

            int index = 0;
            String defaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
            String[] entryValues = getResources().getStringArray(R.array.airplaneModeValues);
            for (String v : entryValues) {
                if (v.equals(defaultValue))
                    break;
                index++;
            }
            String[] entries = getResources().getStringArray(R.array.airplaneModeArray);
            if (index == 0) {
                _value.append(entries[index]);
            }
        }

        cattegorySummaryData.summary = _value.toString();
        cattegorySummaryData.forceSet = true;

        Profile profile = new Profile();
        profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, "0"));
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileRadioPreferences(context, profile, permissions);
        //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.isEmpty();

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryNotifications(Context context,
                                                    CattegorySummaryData cattegorySummaryData) {

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_GENERATE_NOTIFICATION, R.string.profile_preferences_generateNotification, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            String value = preferences.getString(Profile.PREF_PROFILE_GENERATE_NOTIFICATION,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_GENERATE_NOTIFICATION));

            //boolean generate = Profile.getGenerateNotificationChange(value);
            int iconType = ProfileStatic.getGenerateNotificationIconType(value);
            boolean replaceWithPPPIcon = ProfileStatic.getGenerateNotificationReplaceWithPPPIcon(value);
            boolean showLargeIcon = ProfileStatic.getGenerateNotificationShowLargeIcon(value);
            String notificationTitle = ProfileStatic.getGenerateNotificationTitle(value);
            String notificationBody = ProfileStatic.getGenerateNotificationBody(value);

            String summaryString = "";

            if (iconType == 0)
                summaryString = summaryString + getString(R.string.preference_profile_generate_notification_information_icon) + "; ";
            else if (iconType == 1)
                summaryString = summaryString + getString(R.string.preference_profile_generate_notification_exclamation_icon) + "; ";
            else
                summaryString = summaryString + getString(R.string.preference_profile_generate_notification_profile_icon) + "; ";

            if (replaceWithPPPIcon)
                summaryString = summaryString + getString(R.string.preference_profile_generate_notification_replace_with_ppp_icon) + "; ";
            if (showLargeIcon)
                summaryString = summaryString + getString(R.string.preference_profile_generate_notification_show_large_icon) + "; ";

            if (notificationBody.isEmpty())
                summaryString = summaryString + StringConstants.CHAR_QUOTE_HTML + notificationTitle + StringConstants.CHAR_QUOTE_HTML;
            else
                summaryString = summaryString + StringConstants.CHAR_QUOTE_HTML + notificationTitle + StringConstants.CHAR_QUOTE_HTML
                        + "; " + StringConstants.CHAR_QUOTE_HTML + notificationBody + StringConstants.CHAR_QUOTE_HTML;

            _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                    .append(ProfileStatic.getColorForChangedPreferenceValue(summaryString, prefMng, PREF_PROFILE_OTHERS_CATTEGORY_ROOT, context))
                    .append(StringConstants.TAG_BOLD_END_HTML);
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED, R.string.profile_preferences_category_clear_notifications, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            if (PPNotificationListenerService.isNotificationListenerServiceEnabled(context, false)) {
                //boolean applications = false;
                //boolean contactGroups = false;
                //boolean contacts = false;
                String value = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS));
                if ((value != null) &&
                        (!value.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS)))) {
                    //applications = true;
                    _value.append(title).append(": ");

                    String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
                    _value.append(StringConstants.TAG_BOLD_START_HTML)
                            .append(ProfileStatic.getColorForChangedPreferenceValue(context.getString(R.string.profile_preferences_clearNotification_applications_summary_text) + " " + splits.length, prefMng, PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT, context))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                    //noinspection DataFlowIssue
                    if (preferences.getBoolean(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS,
                            Profile.defaultValuesBoolean.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS))) {
                        value = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS));
                        if ((value != null) &&
                                (!value.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS)))) {
                            //contactGroups = true;
                            //if (applications)
                                _value.append("; ");
                            //else
                            //    _value.append(title).append(": ");

                            splits = value.split(StringConstants.STR_SPLIT_REGEX);
                            _value.append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(ProfileStatic.getColorForChangedPreferenceValue(context.getString(R.string.profile_preferences_clearNotification_contact_groups_summary_text) + " " + splits.length, prefMng, PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT, context))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        }
                        value = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS));
                        if ((value != null) &&
                                (!value.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS)))) {
                            //contacts = true;
                            //if (applications || contactGroups)
                                _value.append("; ");
                            //else
                            //    _value.append(title).append(": ");

                            splits = value.split(StringConstants.STR_SPLIT_REGEX);
                            _value.append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(ProfileStatic.getColorForChangedPreferenceValue(context.getString(R.string.profile_preferences_clearNotification_contacts_summary_text) + " " + splits.length, prefMng, PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT, context))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        }
                    }
                    //noinspection DataFlowIssue
                    if (preferences.getBoolean(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT,
                            Profile.defaultValuesBoolean.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT))) {
                        //if (applications || contactGroups || contacts)
                        _value.append("; ");
                        //else
                        //    _value.append(title).append(": ");

                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(ProfileStatic.getColorForChangedPreferenceValue(context.getString(R.string.profile_preferences_clearNotification_text_summary_text), prefMng, PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT, context))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }
                } else {
                    value = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS));
                    _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                            .append(
                                    ProfileStatic.getColorForChangedPreferenceValue(
                                            context.getString(R.string.profile_preferences_clearNotification_applications_summary_text) + " " +
                                            ApplicationsMultiSelectDialogPreference.getSummaryForPreferenceCategory(value, "notifications", context, true),
                                            prefMng, PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT, context)
                            ).append(StringConstants.TAG_BOLD_END_HTML);

                }
            } else {
                String value = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS));
                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(
                                ProfileStatic.getColorForChangedPreferenceValue(
                                        ApplicationsMultiSelectDialogPreference.getSummaryForPreferenceCategory(value, "notifications", context, true),
                                        prefMng, PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT, context)
                        ).append(StringConstants.TAG_BOLD_END_HTML);
            }
        }

        cattegorySummaryData.summary = _value.toString();

        Profile profile = new Profile();
        profile._clearNotificationEnabled = preferences.getBoolean(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED, false);
        profile._clearNotificationApplications = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS, "");
        profile._clearNotificationCheckContacts = preferences.getBoolean(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS, false);
        profile._clearNotificationContacts = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS, "");
        profile._clearNotificationContactGroups = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS, "");
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileClearNotifications(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.isEmpty();

        return false;
    }

    /** @noinspection SameReturnValue*/
    private boolean setCategorySummaryClearNotifications(Context context,
                                                         CattegorySummaryData cattegorySummaryData) {

        StringBuilder _value = new StringBuilder(cattegorySummaryData.summary);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED, R.string.profile_preferences_category_clear_notifications, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);

            if (PPNotificationListenerService.isNotificationListenerServiceEnabled(context, false)) {
                //boolean applications = false;
                //boolean contactGroups = false;
                //boolean contacts = false;
                String value = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS));
                if ((value != null) &&
                        (!value.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS)))) {
                    //applications = true;
                    _value.append(title).append(": ");

                    String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
                    _value.append(StringConstants.TAG_BOLD_START_HTML)
                            .append(ProfileStatic.getColorForChangedPreferenceValue(context.getString(R.string.profile_preferences_clearNotification_applications_summary_text) + " " + splits.length, prefMng, PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT, context))
                            .append(StringConstants.TAG_BOLD_END_HTML);
                    //noinspection DataFlowIssue
                    if (preferences.getBoolean(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS,
                            Profile.defaultValuesBoolean.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS))) {
                        value = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS));
                        if ((value != null) &&
                                (!value.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS)))) {
                            //contactGroups = true;
                            //if (applications)
                                _value.append("; ");
                            //else
                            //    _value.append(title).append(": ");

                            splits = value.split(StringConstants.STR_SPLIT_REGEX);
                            _value.append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(ProfileStatic.getColorForChangedPreferenceValue(context.getString(R.string.profile_preferences_clearNotification_contact_groups_summary_text) + " " + splits.length, prefMng, PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT, context))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        }
                        value = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS));
                        if ((value != null) &&
                                (!value.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS)))) {
                            //contacts = true;
                            //if (applications || contactGroups)
                                _value.append("; ");
                            //else
                            //    _value.append(title).append(": ");

                            splits = value.split(StringConstants.STR_SPLIT_REGEX);
                            _value.append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(ProfileStatic.getColorForChangedPreferenceValue(context.getString(R.string.profile_preferences_clearNotification_contacts_summary_text) + " " + splits.length, prefMng, PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT, context))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        }
                    }
                    //noinspection DataFlowIssue
                    if (preferences.getBoolean(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT,
                            Profile.defaultValuesBoolean.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT))) {
                        //if (applications || contactGroups || contacts)
                        _value.append("; ");
                        //else
                        //    _value.append(title).append(": ");

                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(ProfileStatic.getColorForChangedPreferenceValue(context.getString(R.string.profile_preferences_clearNotification_text_summary_text), prefMng, PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT, context))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }
                } else {
                    value = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS));
                    _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                            .append(
                                    ProfileStatic.getColorForChangedPreferenceValue(
                                            context.getString(R.string.profile_preferences_clearNotification_applications_summary_text) + " " +
                                            ApplicationsMultiSelectDialogPreference.getSummaryForPreferenceCategory(value, "notifications", context, true),
                                            prefMng, PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT, context)
                            ).append(StringConstants.TAG_BOLD_END_HTML);

                }
            } else {
                String value = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS));
                _value.append(title).append(": ").append(StringConstants.TAG_BOLD_START_HTML)
                        .append(
                                ProfileStatic.getColorForChangedPreferenceValue(
                                        ApplicationsMultiSelectDialogPreference.getSummaryForPreferenceCategory(value, "notifications", context, true),
                                        prefMng, PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT, context)
                        ).append(StringConstants.TAG_BOLD_END_HTML);
            }
        }

        cattegorySummaryData.summary = _value.toString();

        Profile profile = new Profile();
        profile._clearNotificationEnabled = preferences.getBoolean(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED, false);
        profile._clearNotificationApplications = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS, "");
        profile._clearNotificationCheckContacts = preferences.getBoolean(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS, false);
        profile._clearNotificationContacts = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS, "");
        profile._clearNotificationContactGroups = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS, "");
        ArrayList<PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileClearNotifications(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.isEmpty();

        return false;
    }


    private void setCategorySummary(String key, Context context) {
        Preference preferenceScreen = prefMng.findPreference(key);
        if (preferenceScreen == null)
            return;

        //SharedPreferences preferences = prefMng.getSharedPreferences();

        CattegorySummaryData cattegorySummaryData = new CattegorySummaryData();
        cattegorySummaryData.summary = "";
        cattegorySummaryData.permissionGranted = true;
        cattegorySummaryData.forceSet = false;
        cattegorySummaryData.bold = false;
        cattegorySummaryData.accessibilityEnabled = true;
        cattegorySummaryData.defaultAssistantSet = true;

        int phoneCount = 1;
        TelephonyManager telephonyManager;// = null;
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                phoneCount = telephonyManager.getPhoneCount();
            }

        notGrantedG1Permission = false;
        notRootedOrGrantetRoot = false;
        notInstalledPPPPS = false;
        notGrantedShizuku = false;

        if (key.equals(PREF_PROFILE_ACTIVATION_DURATION_CATTEGORY_ROOT)) {
            if (setCategorySummaryActivationDuration(context,
                    preferenceScreen, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_SOUND_PROFILE_CATTEGORY_ROOT)) {
            if (setCategorySummarySoundProfile(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_VOLUME_CATTEGORY_ROOT)) {
            if (setCategorySummaryVolume(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_SOUNDS_CATTEGORY_ROOT)) {
            if (setCategorySummarySounds(context,
                    preferenceScreen, cattegorySummaryData, phoneCount))
                return;
        }

        if (key.equals(PREF_PROFILE_TOUCH_EFFECTS_CATTEGORY_ROOT)) {
            if (setCategorySummaryTouchEffects(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_VIBRATION_INTENSITY_CATTEGORY_ROOT)) {
            if (setCategorySummaryVibrationIntensity(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_RADIOS_CATTEGORY_ROOT)) {
            if (setCategorySummaryRadios(context, cattegorySummaryData,
                    telephonyManager, phoneCount))
                return;
        }

        if (key.equals(PREF_PROFILE_SCREEN_CATTEGORY_ROOT)) {
            if (setCategorySummaryScreen(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_LED_ACCESSORIES_CATTEGORY_ROOT)) {
            if (setCategorySummaryLedAccessories(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT)) {
            if (setCategorySummaryNotifications(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_OTHERS_CATTEGORY_ROOT)) {
            if (setCategorySummaryOthers(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_FORCE_STOP_APPLICATIONS_CATEGORY_ROOT)) {
            if (setCategorySummaryForceStopApplications(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_LOCK_DEVICE_CATEGORY_ROOT)) {
            if (setCategorySummaryLockDevice(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_APPLICATION_CATTEGORY_ROOT)) {
            if (setCategorySummaryApplication(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_DEVICE_RADIOS_DUAL_SIM_SUPPORT_CATEGORY_ROOT)) {
            if (setCategorySummaryRadiosDualSIMSupport(context, preferenceScreen,
                    cattegorySummaryData, telephonyManager, phoneCount))
                return;
        }

        if (key.equals(PREF_PROFILE_SOUNDS_DUAL_SIM_SUPPORT_CATEGORY_ROOT)) {
            if (setCategorySummarySoundsDualSIMSupport(context, preferenceScreen,
                    cattegorySummaryData, telephonyManager, phoneCount))
                return;
        }

        if (key.equals(PREF_DEVICE_WALLPAPER_CATEGORY_ROOT)) {
            if (setCategorySummaryDeviceWallpaper(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_DEVICE_AIRPLANE_MODE_CATEGORY_ROOT)) {
            if (setCategorySummaryAirplaneMode(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_SEND_SMS_CATTEGORY_ROOT)) {
            if (setCategorySummarySendSMS(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_CLEAR_NOTIFICATIONS_CATTEGORY_ROOT)) {
            if (setCategorySummaryClearNotifications(context, cattegorySummaryData))
                return;
        }

        GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, cattegorySummaryData.bold, false, false,
                (!cattegorySummaryData.permissionGranted) ||
                (!cattegorySummaryData.accessibilityEnabled) ||
                (!cattegorySummaryData.defaultAssistantSet) ||
                notGrantedG1Permission ||
                notRootedOrGrantetRoot ||
                notInstalledPPPPS ||
                notGrantedShizuku, false);
        if (cattegorySummaryData.bold || cattegorySummaryData.forceSet)
            preferenceScreen.setSummary(StringFormatUtils.fromHtml(cattegorySummaryData.summary, false,  false, 0, 0, true));
        else
            preferenceScreen.setSummary("");
    }

    private void setSummaryForNotificationVolume0(/*Context context*/) {
        Preference preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
        if (preference != null) {
            String notificationToneChange = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0");
            String notificationTone = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");
            //String uriId = TonesHandler.getPhoneProfilesSilentUri(context, RingtoneManager.TYPE_NOTIFICATION);
            /*if (notificationToneChange.equals("1") && notificationTone.equals(uriId))
                preference.setSummary(R.string.profile_preferences_volumeNotificationVolume0_summaryPhoneProfilesSilentConfigured);
            else*/
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

        int phoneCount = 1;
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                phoneCount = telephonyManager.getPhoneCount();
            }

        if (key.equals(Profile.PREF_PROFILE_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value.toString());
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !value.toString().isEmpty(), false, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_ICON))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                //preference.setSummary(value.toString());
                boolean valueChanged = !value.toString().equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_ICON));

                Profile profile = new Profile();
                profile._icon = value.toString();
                ArrayList<PermissionType> permissions = new ArrayList<>();
                Permissions.checkProfileCustomProfileIcon(context, profile, false, permissions);
                boolean permissionGranted = permissions.isEmpty();

                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, valueChanged, false, false, !permissionGranted, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR)) {
            String sValue = value.toString();
            SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals(StringConstants.TRUE_STRING);
                GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, false, false, false, false);
            }
        }

        boolean alsoSetZenMode = false;
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE))
        {
            String sValue = value.toString();
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false, false);
                if (sValue.equals("5")) {
                    // do not disturb
                    value = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "");
                    alsoSetZenMode = true;
                    //setSummary(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, zenModeValue);
                } else {
                    PPListPreference zenModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
                    if (zenModePreference != null) {
                        zenModePreference.setEnabled(false);
                        Preference zenModePreferenceInfo = prefMng.findPreference(PREF_PROFILE_VOLUME_ZEN_MODE_INFO);
                        if (zenModePreferenceInfo != null) {
                            zenModePreferenceInfo.setEnabled(zenModePreference.isEnabled());
                        }
                    }
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE) || alsoSetZenMode)
        {
            final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context);

            if (!canEnableZenMode)
            {
                PPListPreference listPreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
                if (listPreference != null) {
                    listPreference.setEnabled(false);
                    listPreference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                            ": "+getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, false, false);

                    Preference zenModePreferenceInfo = prefMng.findPreference(PREF_PROFILE_VOLUME_ZEN_MODE_INFO);
                    if (zenModePreferenceInfo != null) {
                        zenModePreferenceInfo.setEnabled(listPreference.isEnabled());
                    }
                }
            }
            else
            {
                String sValue = value.toString();
                PPListPreference listPreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
                if (listPreference != null) {
                    int iValue = Integer.parseInt(sValue);
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    if ((iValue != Profile.NO_CHANGE_VALUE) /*&& (iValue != Profile.SHARED_PROFILE_VALUE)*/) {
                        if (!((iValue == 6))) {
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
                        GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, true, false, false, false, false);
                    }
                    listPreference.setEnabled(iRingerMode == 5);

                    Preference zenModePreferenceInfo = prefMng.findPreference(PREF_PROFILE_VOLUME_ZEN_MODE_INFO);
                    if (zenModePreferenceInfo != null) {
                        zenModePreferenceInfo.setEnabled(listPreference.isEnabled());
                    }
                }

                /*Preference notificationAccessPreference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
                if (notificationAccessPreference != null) {
                    PreferenceScreen preferenceCategory = findPreference(PREF_PROFILE_SOUND_PROFILE_CATTEGORY);
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(notificationAccessPreference);
                }*/
            }
        }

        if (key.equals(Profile.PREF_PROFILE_DURATION))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                String defaultValue = Profile.defaultValuesString.get(key);
                //preference.setSummary(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, (!sValue.equals(defaultValue)), false, false, false, false);
                preference = prefMng.findPreference(Profile.PREF_PROFILE_AFTER_DURATION_DO);
                if (preference != null) {
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, (!sValue.equals(defaultValue)), false, false, false, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_AFTER_DURATION_DO))
        {
            String sValue = value.toString();
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                String durationDefaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION);
                String durationValue = preferences.getString(Profile.PREF_PROFILE_DURATION, durationDefaultValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true,
                        (durationValue != null) && (!durationValue.equals(durationDefaultValue)), false,
                        false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION))
        {
            String sValue = value.toString();
            SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals(StringConstants.TRUE_STRING);
                GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, false, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND))
        {
            String sValue = value.toString();
            RingtonePreference ringtonePreference = prefMng.findPreference(key);
            if (ringtonePreference != null) {
                boolean show = !sValue.isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(ringtonePreference, true, show, false, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE))
        {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if ((vibrator != null) && vibrator.hasVibrator()) {
                String sValue = value.toString();
                SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
                if (checkBoxPreference != null) {
                    checkBoxPreference.setVisible(true);
                    boolean show = sValue.equals(StringConstants.TRUE_STRING);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, false, false, false, false);
                }
            }
            else {
                SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
                if (checkBoxPreference != null)
                    checkBoxPreference.setVisible(false);
            }
        }

        if (key.equals(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary;

                int forceMergeValue = ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes;
                String[] valuesArray = getResources().getStringArray(R.array.forceSetMergeRingNotificationVolumesValues);
                String[] labelsArray = getResources().getStringArray(R.array.forceSetMergeRingNotificationVolumesArray);
                int setVolumeLinkIndex = 0;
                for (String _value : valuesArray) {
                    if (_value.equals(String.valueOf(forceMergeValue))) {
                        break;
                    }
                    ++setVolumeLinkIndex;
                }

                boolean bold = false;
                if ((ApplicationPreferences.prefMergedRingNotificationVolumes || (setVolumeLinkIndex == 1)) &&
                        ApplicationPreferences.applicationUnlinkRingerNotificationVolumes) {
                    summary = getString(R.string.profile_preferences_applicationUnlinkRingerNotificationVolumes_enabled);
                    bold = true;
                }
                else
                    summary = getString(R.string.profile_preferences_applicationUnlinkRingerNotificationVolumes_disabled);

                summary = summary + StringConstants.CHAR_NEW_LINE + getString(R.string.phone_profiles_pref_applicationForceSetMergeRingNotificationVolumes) + ": ";
                summary = summary + labelsArray[setVolumeLinkIndex];

                if (!ApplicationPreferences.prefMergedRingNotificationVolumes)
                    // detection of volumes merge = volumes are not merged
                    summary = summary + StringConstants.STR_DOUBLE_NEWLINE + getString(R.string.profile_preferences_applicationUnlinkRingerNotificationVolumes_not_merged);
                else
                    // detection of volumes merge = volumes are merged
                    summary = summary + StringConstants.STR_DOUBLE_NEWLINE + getString(R.string.profile_preferences_applicationUnlinkRingerNotificationVolumes_merged);

                preference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, false, false, false, false);
            }
        }

        setSummaryTones(key, value, context, phoneCount);

        setSummaryRadios(key, value, context, phoneCount);

        if (key.equals(Profile.PREF_PROFILE_DEVICE_KEYGUARD))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean errorColor = false;
                    listPreference.setEnabled(false);
                    Preference preference = findPreference(PREF_PROFILE_DEVICE_KEYGUARD_INFO);
                    //noinspection DataFlowIssue
                    preference.setEnabled(false);
                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, errorColor, true);
                }
                else {
                    //listPreference.setEnabled(true);
                    String sValue = value.toString();
                    disableDependentPrefsScreenOnOffDeviceKeyguard(Profile.PREF_PROFILE_DEVICE_KEYGUARD, sValue);
                    Preference preference = findPreference(PREF_PROFILE_DEVICE_KEYGUARD_INFO);
                    //noinspection DataFlowIssue
                    preference.setEnabled(true);
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT))
        {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED)
            {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        preference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) {
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                }

            } else {
                String sValue = value.toString();
                PPListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);

                    Profile profile = new Profile();
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, "0"));
                    Permissions.checkProfileScreenTimeout(context, profile, permissions);
                    boolean _permissionGranted = permissions.isEmpty();

                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AUTOROTATE))
        {
            String sValue = value.toString();
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);

                Profile profile = new Profile();
                ArrayList<PermissionType> permissions = new ArrayList<>();
                profile._deviceAutoRotate = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, "0"));
                Permissions.checkProfileAutoRotation(context, profile, permissions);
                boolean _permissionGranted = permissions.isEmpty();

                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE) ||
                key.equals(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
                key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING) ||
                key.equals(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR) ||
                //key.equals(Profile.PREF_PROFILE_LOCK_DEVICE) ||
                key.equals(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ON_TOUCH))
        {
            PreferenceAllowed preferenceAllowed;
            if (key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING)) {
                preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            }
            else
            if (key.equals(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS)) {
                if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
                    preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                }
                else {
                    preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                }
            }
            else {
                preferenceAllowed = new PreferenceAllowed();
                preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED)
            {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        preference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                }
            }
            else {
                String sValue = value.toString();
                PPListPreference preference = prefMng.findPreference(key);
                if (preference !=  null) {
                    int index = preference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? preference.getEntries()[index] : null;
                    preference.setSummary(summary);

                    boolean _permissionGranted = true;

                    if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
                            key.equals(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE) ||
                            key.equals(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
                            key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING) ||
                            key.equals(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS) ||
                            //key.equals(Profile.PREF_PROFILE_LOCK_DEVICE) ||
                            key.equals(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING) ||
                            key.equals(Profile.PREF_PROFILE_SOUND_ON_TOUCH)) {
                        Profile profile = new Profile();
                        ArrayList<PermissionType> permissions = new ArrayList<>();
                        profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, "0"));
                        profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, "0"));
                        profile._vibrationOnTouch = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, "0"));
                        profile._vibrateWhenRinging = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, "0"));
                        if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
                            profile._vibrateNotifications = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, "0"));
                        }
                        //profile._lockDevice = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, "0"));
                        profile._dtmfToneWhenDialing = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, "0"));
                        profile._soundOnTouch = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ON_TOUCH, "0"));
                        Permissions.checkProfileImageWallpaper(context, profile, permissions);
                        Permissions.checkProfileWallpaperFolder(context, profile, permissions);
                        Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
                        Permissions.checkProfileVibrationOnTouch(context, profile, permissions);
                        Permissions.checkProfileVibrateWhenRinging(context, profile, permissions);
                        //if (Build.VERSION.SDK_INT >= 28)
                        //    Permissions.checkProfileVibrateNotifications(context, profile, permissions);
                        Permissions.checkProfileLockDevice(context, profile, permissions);
                        Permissions.checkProfileDtmfToneWhenDialing(context, profile, permissions);
                        Permissions.checkProfileSoundOnTouch(context, profile, permissions);
                        _permissionGranted = permissions.isEmpty();
                    }

                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, index > 0, false, false, !_permissionGranted, false);
                }
            }
        }

        if (key.equals(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING) ||
            key.equals(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS) ||
            key.equals(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION))
        {
            PreferenceAllowed _preferenceAllowed = new PreferenceAllowed();
            _preferenceAllowed.isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY( context);
            if (_preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                String sValue = value.toString();
                PreferenceAllowed preferenceAllowed =
                        ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED)
                {
                    Preference preference = prefMng.findPreference(key);
                    if (preference != null) {
                        boolean errorColor = false;
                        if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                            preference.setEnabled(false);
                        else
                            errorColor = ProfileStatic.getVibrationIntensityChange(sValue);
                        if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                            preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                    ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                        GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                    }
                }
                /*else {
                    VibrationIntensityPreference preference = prefMng.findPreference(key);
                    if (preference !=  null) {
                        boolean change = VibrationIntensityPreference.changeEnabled(sValue);

                        boolean _permissionGranted;

                        Profile profile = new Profile();
                        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                        profile._vibrationIntensityRinging = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING, "-1|1");
                        profile._vibrationIntensityNotifications = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, "-1|1");
                        profile._vibrationIntensityTouchInteraction = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, "-1|1");
                        //Permissions.checkProfileVibrationIntensityForSamsung(context, profile, permissions);
                        _permissionGranted = permissions.size() == 0;

                        GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, false, false, !_permissionGranted);
                    }
                }*/
            } else {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + _preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
            }
        }

        if (key.equals(Profile.PREF_PROFILE_NOTIFICATION_LED))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        listPreference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, errorColor, true);
                } else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);

                    Profile profile = new Profile();
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    profile._notificationLed = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_NOTIFICATION_LED, "0"));
                    Permissions.checkProfileNotificationLed(context, profile, permissions);
                    boolean _permissionGranted = permissions.isEmpty();

                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS) ||
                key.equals(Profile.PREF_PROFILE_SCREEN_DARK_MODE) ||
                key.equals(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY) ||
                key.equals(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT) ||
                key.equals(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT) ||
                key.equals(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT_PREFS) ||
                key.equals(Profile.PREF_PROFILE_SCREEN_ON_OFF))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        listPreference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, errorColor, true);
                } else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);

                    boolean _permissionGranted = true;

                    if (key.equals(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT) ||
                            key.equals(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY) ||
                            key.equals(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT) ||
                            key.equals(Profile.PREF_PROFILE_SCREEN_ON_OFF)) {
                        Profile profile = new Profile();
                        ArrayList<PermissionType> permissions = new ArrayList<>();
                        profile._screenOnPermanent = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT, "0"));
                        profile._alwaysOnDisplay = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, "0"));
                        profile._screenNightLight = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT, "0"));
                        profile._screenOnOff = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_ON_OFF, "0"));
                        Permissions.checkProfileScreenOnPermanent(context, profile, permissions);
                        Permissions.checkProfileAlwaysOnDisplay(context, profile, permissions);
                        Permissions.checkProfileScreenNightLight(context, profile, permissions);
                        Permissions.checkProfileScreenOnOff(context, profile, permissions);
                        _permissionGranted = permissions.isEmpty();
                    }

                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
                }
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
                boolean change = VolumeDialogPreference.changeEnabled(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, false, false, false, false);
            }
        }
        if (key.equals(PREF_VOLUME_NOTIFICATION_VOLUME0)) {
            setSummaryForNotificationVolume0(/*context*/);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = BrightnessDialogPreference.changeEnabled(sValue);

                Profile profile = new Profile();
                ArrayList<PermissionType> permissions = new ArrayList<>();
                profile._deviceBrightness = preferences.getString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, "");
                Permissions.checkProfileScreenBrightness(context, profile, permissions);
                boolean _permissionGranted = permissions.isEmpty();

                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, false, false, !_permissionGranted, false);

                if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
                    preference = prefMng.findPreference(PREF_PROFILE_DEVICE_BRIGHTNESS_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON);
                    if (preference != null) {
                        boolean forceSetBrightnessAtScreenOn = ApplicationPreferences.applicationForceSetBrightnessAtScreenOn;
                        String summary = context.getString(R.string.profile_preferences_forceSetBrightnessAtScreenOn_summary);
                        if (forceSetBrightnessAtScreenOn)
                            summary = context.getString(R.string.profile_preferences_enabled) + StringConstants.STR_DOUBLE_NEWLINE + summary;
                        else {
                            summary = context.getString(R.string.profile_preferences_disabled) + StringConstants.STR_DOUBLE_NEWLINE + summary;
                        }
                        preference.setSummary(summary);
                        GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, forceSetBrightnessAtScreenOn, false, false, false, false);
                    }
                }

            }
        }
        if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_WIFI_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_BLUETOOTH_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_LOCATION_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_MOBILE_CELL_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_ORIENTATION_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_NOTIFICATION_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_PERIODIC_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean enabled = true;
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)) {
                        enabled = false;
                        listPreference.setEnabled(false);
                    }
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, errorColor, true);

                    String sValue = value.toString();
                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_WIFI_SCANNING)) {
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_WIFI_SCAN_INTERVAL_INFO);
                        enabled = enabled && sValue.equals("3");
                        if (preference != null)
                            preference.setEnabled(enabled);
                        preference = prefMng.findPreference(Profile.PREF_PROFILE_APPLICATION_WIFI_SCAN_INTERVAL);
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_BLUETOOTH_SCANNING)) {
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_BLUETOOTH_SCAN_INTERVAL_INFO);
                        enabled = enabled && sValue.equals("3");
                        if (preference != null)
                            preference.setEnabled(enabled);
                        preference = prefMng.findPreference(Profile.PREF_PROFILE_APPLICATION_BLUETOOTH_SCAN_INTERVAL);
                        if (preference != null)
                            preference.setEnabled(enabled);
                        preference = prefMng.findPreference(Profile.PREF_PROFILE_APPLICATION_BLUETOOTH_LE_SCAN_DURATION);
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_LOCATION_SCANNING)) {
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_LOCATION_UPDATE_INTERVAL);
                        enabled = enabled && sValue.equals("3");
                        if (preference != null)
                            preference.setEnabled(enabled);
                        preference = prefMng.findPreference(Profile.PREF_PROFILE_APPLICATION_LOCATION_UPDATE_INTERVAL);
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_MOBILE_CELL_SCANNING)) {
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_MOBILE_CELLS_SCAN_INTERVAL_INFO);
                        enabled = enabled && sValue.equals("3");
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_ORIENTATION_SCANNING)) {
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_ORIENTATION_SCAN_INTERVAL_INFO);
                        enabled = enabled && sValue.equals("3");
                        if (preference != null)
                            preference.setEnabled(enabled);
                        preference = prefMng.findPreference(Profile.PREF_PROFILE_APPLICATION_ORIENTATION_SCAN_INTERVAL);
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_NOTIFICATION_SCANNING)) {
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_NOTIFICATION_SCAN_INTERVAL_INFO);
                        enabled = enabled && sValue.equals("3");
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_PERIODIC_SCANNING)) {
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL_INFO);
                        enabled = enabled && sValue.equals("3");
                        if (preference != null)
                            preference.setEnabled(enabled);
                        preference = prefMng.findPreference(Profile.PREF_PROFILE_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL);
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                }
                else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false, false);

                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_WIFI_SCANNING)) {
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_WIFI_SCAN_INTERVAL_INFO);
                        boolean enabled = sValue.equals("3");
                        if (preference != null)
                            preference.setEnabled(enabled);
                        preference = prefMng.findPreference(Profile.PREF_PROFILE_APPLICATION_WIFI_SCAN_INTERVAL);
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_BLUETOOTH_SCANNING)) {
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_BLUETOOTH_SCAN_INTERVAL_INFO);
                        boolean enabled = sValue.equals("3");
                        if (preference != null)
                            preference.setEnabled(enabled);
                        preference = prefMng.findPreference(Profile.PREF_PROFILE_APPLICATION_BLUETOOTH_SCAN_INTERVAL);
                        if (preference != null)
                            preference.setEnabled(enabled);
                        preference = prefMng.findPreference(Profile.PREF_PROFILE_APPLICATION_BLUETOOTH_LE_SCAN_DURATION);
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_LOCATION_SCANNING)) {
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_LOCATION_UPDATE_INTERVAL);
                        boolean enabled = sValue.equals("3");
                        if (preference != null)
                            preference.setEnabled(enabled);
                        preference = prefMng.findPreference(Profile.PREF_PROFILE_APPLICATION_LOCATION_UPDATE_INTERVAL);
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_MOBILE_CELL_SCANNING)) {
                        boolean enabled = sValue.equals("3");
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_MOBILE_CELLS_SCAN_INTERVAL_INFO);
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_ORIENTATION_SCANNING)) {
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_ORIENTATION_SCAN_INTERVAL_INFO);
                        boolean enabled = sValue.equals("3");
                        if (preference != null)
                            preference.setEnabled(enabled);
                        preference = prefMng.findPreference(Profile.PREF_PROFILE_APPLICATION_ORIENTATION_SCAN_INTERVAL);
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_NOTIFICATION_SCANNING)) {
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_NOTIFICATION_SCAN_INTERVAL_INFO);
                        boolean enabled = sValue.equals("3");
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                    if (key.equals(Profile.PREF_PROFILE_APPLICATION_ENABLE_PERIODIC_SCANNING)) {
                        Preference preference = prefMng.findPreference(PREF_PROFILE_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL_INFO);
                        boolean enabled = sValue.equals("3");
                        if (preference != null)
                            preference.setEnabled(enabled);
                        preference = prefMng.findPreference(Profile.PREF_PROFILE_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL);
                        if (preference != null)
                            preference.setEnabled(enabled);
                    }
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_APPLICATION_WIFI_SCAN_INTERVAL) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_BLUETOOTH_SCAN_INTERVAL) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_BLUETOOTH_LE_SCAN_DURATION) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_LOCATION_UPDATE_INTERVAL) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_ORIENTATION_SCAN_INTERVAL) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL)) {
            BetterNumberPickerPreference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(preference.value);
            }
        }

        /*
        if (key.equals(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0) {
                    String summary = getString(R.string.profile_preferences_PPPExtender_not_installed_summary) +
                            "\n\n" + getString(R.string.profile_preferences_deviceForceStopApplications_PPPExtender_install_summary);
                    preference.setSummary(summary);
                }
                else {
                    String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(context);
                    String summary =  getString(R.string.profile_preferences_PPPExtender_installed_summary) +
                            " " + extenderVersionName + " (" + extenderVersion + ")\n\n";
                    if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)
                        summary = summary + getString(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                    else
                        summary = summary + getString(R.string.pppextender_pref_dialog_PPPExtender_upgrade_summary);
                    preference.setSummary(summary);
                }
            }
        }
        */
        if (key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE)) {
            int index;
            String sValue;

            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                sValue = listPreference.getValue();
                index = listPreference.findIndexOfValue(sValue);

                String defaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
                String _sValue = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, defaultValue);
                boolean ok = true;
                CharSequence changeSummary = "";
                if ("1".equals(_sValue)) {
                    int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
                    if (extenderVersion == 0) {
                        ok = false;
                        changeSummary = getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_not_extender_installed);
                    } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED) {
                        ok = false;
                        changeSummary = getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                    } else if (!PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, false, true
                            /*, "ProfilesPrefsFragment.setSummary (PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE)"*/)) {
                        ok = false;
                        changeSummary = getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                    } else if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                        ok = false;
                        changeSummary = getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined);
                    }
                }
                else
                if ("2".equals(_sValue)) {
                    PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.isProfilePreferenceAllowed_PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION(
                            Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE,
                            null, preferences);
                    ok = preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED;
                    changeSummary = (index >= 0) ? listPreference.getEntries()[index] : null;
                }

                if (!ok) {
                    listPreference.setSummary(changeSummary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, true, false);
                }
                else {
                    changeSummary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(changeSummary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false, false);
                }
            }
        }

        /*
        if (key.equals(PREF_LOCK_DEVICE_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0) {
                    String summary = getString(R.string.profile_preferences_PPPExtender_not_installed_summary) +
                            "\n\n" + getString(R.string.profile_preferences_lockDevice_PPPExtender_install_summary);
                    preference.setSummary(summary);
                }
                else {
                    String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(context);
                    String summary =  getString(R.string.profile_preferences_PPPExtender_installed_summary) +
                            " " + extenderVersionName + " (" + extenderVersion + ")\n\n";
                    if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)
                        summary = summary + getString(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                    else
                        summary = summary + getString(R.string.pppextender_pref_dialog_PPPExtender_upgrade_summary);
                    preference.setSummary(summary);
                }
            }
        }
        */
        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
            int index;
            String sValue;

            PPListPreference listPreference =
                    prefMng.findPreference(Profile.PREF_PROFILE_LOCK_DEVICE);
            if (listPreference != null) {
                sValue = listPreference.getValue();
                //boolean ok = true;
                CharSequence changeSummary;// = "";

                index = listPreference.findIndexOfValue(sValue);
                changeSummary = (index >= 0) ? listPreference.getEntries()[index] : null;

                if (sValue.equals("3")) {
                    int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
                    if (extenderVersion == 0) {
                        //ok = false;
                        changeSummary = changeSummary + StringConstants.STR_DOUBLE_NEWLINE +
                                getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_not_extender_installed);
                    } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED) {
                        //ok = false;
                        changeSummary = changeSummary + StringConstants.STR_DOUBLE_NEWLINE +
                                getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                    } else if (!PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, false, true
                            /*, "ProfilesPrefsFragment.setSummary (PREF_PROFILE_LOCK_DEVICE)"*/)) {
                        //ok = false;
                        changeSummary = changeSummary + StringConstants.STR_DOUBLE_NEWLINE +
                                getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                    } else if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                        //ok = false;
                        changeSummary = changeSummary + getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined);
                    }
                }

                listPreference.setSummary(changeSummary);

                Profile profile = new Profile();
                ArrayList<PermissionType> permissions = new ArrayList<>();
                profile._lockDevice = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, "0"));
                Permissions.checkProfileLockDevice(context, profile, permissions);
                boolean _permissionGranted = permissions.isEmpty();

                boolean _accessibilityEnabled = true;
                if (sValue.equals("3")) {
                    int _isAccessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false);
                    _accessibilityEnabled = _isAccessibilityEnabled == 1;
                }

                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, (!_permissionGranted) || (!_accessibilityEnabled), false);
            }
        }
        /*
        if (key.equals(PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                Profile profile = new Profile();
                profile._lockDevice = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, "0"));

                if (profile._lockDevice == 3) {
                    int _isAccessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false);
                    boolean _accessibilityEnabled = _isAccessibilityEnabled == 1;

                    String summary;
                    if (_accessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1))
                        summary = getString(R.string.accessibility_service_enabled);
                    else {
                        if (_isAccessibilityEnabled == -1) {
                            summary = getString(R.string.accessibility_service_not_used);
                            summary = summary + "\n\n" + getString(R.string.preference_not_used_extender_reason) + " " +
                                    getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                        } else {
                            summary = getString(R.string.accessibility_service_disabled);
                            summary = summary + "\n\n" + getString(R.string.profile_preferences_lockDevice_AccessibilitySettingsForExtender_summary);
                        }
                    }
                    preference.setSummary(summary);
                    //GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, true, !_accessibilityEnabled);
                } else {
                    preference.setSummary(R.string.accessibility_service_not_used);
                }
            }
        }
        */
        /*
        if (key.equals(PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                Profile profile = new Profile();
                profile._deviceForceStopApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, "0"));

                int _isAccessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false);
                boolean _accessibilityEnabled = _isAccessibilityEnabled == 1;

                String summary;
                if (_accessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1))
                    summary = getString(R.string.accessibility_service_enabled);
                else {
                    if (_isAccessibilityEnabled == -1) {
                        summary = getString(R.string.accessibility_service_not_used);
                        summary = summary + "\n\n" + getString(R.string.preference_not_used_extender_reason) + " " +
                                getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                    } else {
                        summary = getString(R.string.accessibility_service_disabled);
                        summary = summary + "\n\n" + getString(R.string.profile_preferences_deviceForceStopApplications_AccessibilitySettingsForExtender_summary);
                    }
                }
                preference.setSummary(summary);

                //GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, true, !_accessibilityEnabled);
            }
        }
        */
        if (key.equals(Profile.PREF_PROFILE_GENERATE_NOTIFICATION))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = GenerateNotificationDialogPreference.changeEnabled(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, false, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_CAMERA_FLASH))
        {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED)
            {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        preference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                }
            } else {
                String sValue = value.toString();
                PPListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);

                    Profile profile = new Profile();
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    profile._cameraFlash = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_CAMERA_FLASH, "0"));
                    Permissions.checkProfileCameraFlash(context, profile, permissions);
                    boolean _permissionGranted = permissions.isEmpty();

                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE))
        {
            String sValue = value.toString();
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
//                String durationDefaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION);
//                String durationValue = preferences.getString(Profile.PREF_PROFILE_DURATION, durationDefaultValue);
//                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true,
//                        (durationValue != null) && (!durationValue.equals(durationDefaultValue)),
//                        false, false, false);
            }

            listPreference = prefMng.findPreference(Profile.PREF_PROFILE_AFTER_DURATION_DO);
            if (listPreference != null) {
                int iValue = Integer.parseInt(value.toString());
                if (iValue == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME) {
                    listPreference.setTitle(R.string.profile_preferences_afterExactTimeDo);
                    listPreference.setDialogTitle(R.string.profile_preferences_afterExactTimeDo);
                }
                else {
                    listPreference.setTitle(R.string.profile_preferences_afterDurationDo);
                    listPreference.setDialogTitle(R.string.profile_preferences_afterDurationDo);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_VPN))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = VPNDialogPreference.changeEnabled(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, false, false, false, false);
            }
        }
        /*if (key.equals(Profile.PREF_PROFILE_PHONE_CALLS_CONTACT_LIST_TYPE)) {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                String sValue = value.toString();
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }*/
        if (key.equals(Profile.PREF_PROFILE_SEND_SMS_SMS_TEXT)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                preference.setSummary(sValue);
            }
        }

        if (key.equals(PREF_NOTIFICATION_ACCESS_SYSTEM_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary = getString(R.string.profile_preferences_clearNotificationsAccessSettings_summary);
                if (!PPNotificationListenerService.isNotificationListenerServiceEnabled(context, true)) {
                    summary = "* " + getString(R.string.phone_profiles_pref_applicationEventScanningNotificationAccessSettingsDisabled_summary) + "! *" + StringConstants.STR_DOUBLE_NEWLINE +
                            summary;
                } else {
                    summary = getString(R.string.phone_profiles_pref_applicationEventScanningNotificationAccessSettingsEnabled_summary) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                            summary;
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS)) {
            //boolean isEnabled = preferences.getBoolean(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED, false);
            //boolean listenerEnabled = PPNotificationListenerService.isNotificationListenerServiceEnabled(context, false);
            ApplicationsMultiSelectDialogPreference appPreference =
                    prefMng.findPreference(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS);
            if (appPreference != null) {
                appPreference.setSummaryAMSDP();
            }
        }
        if (key.equals(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT))
        {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED)
            {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        preference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                }
            } else {
                String sValue = value.toString();
                PPListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);

                    Profile profile = new Profile();
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    profile._screenNightLight = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT, "0"));
                    Permissions.checkProfileCameraFlash(context, profile, permissions);
                    boolean _permissionGranted = permissions.isEmpty();

                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE))
        {
            String sValue = value.toString();
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);

                Profile profile = new Profile();
                ArrayList<PermissionType> permissions = new ArrayList<>();
                profile._deviceCloseAllApplications = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, "0"));
                profile._deviceRunApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, "0"));
                Permissions.checkProfileCloseAllApplications(context, profile, permissions);
                Permissions.checkProfileRunApplications(context, profile, permissions);
                boolean _permissionGranted = permissions.isEmpty();

                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
            }
        }

    }

    private void setSummaryTones(String key, Object value, Context context, int phoneCount) {
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            String sValue = value.toString();
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);

                Profile profile = new Profile();
                ArrayList<PermissionType> permissions = new ArrayList<>();
                profile._soundRingtoneChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, "0"));
                profile._soundNotificationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0"));
                profile._soundAlarmChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, "0"));
                Permissions.checkProfileRingtones(context, profile, permissions);
                boolean _permissionGranted = permissions.isEmpty();

                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
            }
            setSummaryForNotificationVolume0(/*context*/);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ALARM))
        {
            setSummaryForNotificationVolume0(/*context*/);
        }

        if (((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                        (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                        (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                        (PPApplication.deviceIsOnePlus))) {

            if (phoneCount > 1) {

                if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1) ||
                        key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0");
                            if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);

                            if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1)) {
                                preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
                                if (preference != null) {
                                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                        preference.setEnabled(false);
                                    else
                                        errorColor = !value.toString().equals("0");
                                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                                }
                            }
                            if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2)) {
                                preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
                                if (preference != null) {
                                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                        preference.setEnabled(false);
                                    else
                                        errorColor = !value.toString().equals("0");
                                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                                }
                            }
                        }
                    } else {
                        String sValue = value.toString();
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            int index = listPreference.findIndexOfValue(sValue);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);

                            boolean _permissionGranted;
                            Profile profile = new Profile();
                            ArrayList<PermissionType> permissions = new ArrayList<>();
                            profile._soundRingtoneChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, "0"));
                            profile._soundRingtoneChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, "0"));
                            Permissions.checkProfileRingtones(context, profile, permissions);
                            //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
                            _permissionGranted = permissions.isEmpty();

                            GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
                        }
                    }
                }

                if (key.equals(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0");
                            if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                        }
                    } else {
                        String sValue = value.toString();
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            int index = listPreference.findIndexOfValue(sValue);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);

                            GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false, false);
                        }
                    }
                }

                if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1) ||
                        key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0");
                            if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);

                            if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1)) {
                                preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
                                if (preference != null) {
                                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                        preference.setEnabled(false);
                                    else
                                        errorColor = !value.toString().equals("0");
                                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                                }
                            }
                            if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2)) {
                                preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
                                if (preference != null) {
                                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                        preference.setEnabled(false);
                                    else
                                        errorColor = !value.toString().equals("0");
                                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                                }
                            }

                        }
                    } else {
                        String sValue = value.toString();
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            int index = listPreference.findIndexOfValue(sValue);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);

                            boolean _permissionGranted;
                            Profile profile = new Profile();
                            ArrayList<PermissionType> permissions = new ArrayList<>();
                            profile._soundNotificationChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, "0"));
                            profile._soundNotificationChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, "0"));
                            Permissions.checkProfileRingtones(context, profile, permissions);
                            _permissionGranted = permissions.isEmpty();

                            GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
                        }
                    }
                }
            }
        }

    }

    private void setSummaryRadios(String key, Object value, Context context, int phoneCount)
    {
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AUTOSYNC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_GPS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NFC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS))
        {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED)
            {
//                if (key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE))
//                    Log.e("ProfilesPrefsFragment.setSummaryRadios", "network type  not allowed");
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        preference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) {
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                }
            }
            else
            {
                String sValue = value.toString();
                PPListPreference preference = prefMng.findPreference(key);
                if (preference !=  null) {
                    int index = preference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? preference.getEntries()[index] : null;
                    preference.setEnabled(true);
                    preference.setSummary(summary);

                    boolean _permissionGranted = true;
                    if (key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP) ||
                            key.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH) ||
                            key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
                            key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                        Profile profile = new Profile();
                        ArrayList<PermissionType> permissions = new ArrayList<>();
                        profile._deviceWiFiAP = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP, "0"));
                        profile._deviceBluetooth = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, "0"));
                        profile._deviceMobileData = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, "0"));
                        profile._deviceNetworkType = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, "0"));
                        profile._deviceMobileDataPrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, "0"));
                        profile._deviceNetworkTypePrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, "0"));
                        Permissions.checkProfileRadioPreferences(context, profile, permissions);
                        //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
                        _permissionGranted = permissions.isEmpty();
                    }

                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, index > 0, false, false, !_permissionGranted, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE)) {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean errorColor; // = false;
                    /*if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SET_AS_ASSISTANT))
                        preference.setEnabled(false);
                    else*/
                        errorColor = !value.toString().equals("0");
                    //if (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SET_AS_ASSISTANT) {
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    //}
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                } else {
                    preference.setEnabled(true);

                    String sValue = value.toString();
                    PPListPreference listPreference = prefMng.findPreference(key);
                    if (listPreference != null) {
                        int index = listPreference.findIndexOfValue(sValue);
                        CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                        listPreference.setSummary(summary);

                        Profile profile = new Profile();
                        ArrayList<PermissionType> permissions = new ArrayList<>();
                        profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, "0"));
                        Permissions.checkProfileMicrophone(context, profile, permissions);
                        boolean _permissionGranted = permissions.isEmpty();

                        GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
                    }
                }
            }
        }
        if (key.equals(PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS)) {
            String summary = getString(R.string.profile_preferences_deviceAirplaneMode_assistantSettings_summary);
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                if (ActivateProfileHelper.isPPPSetAsDefaultAssistant(context)) {
                    summary = getString(R.string.profile_preferences_deviceAirplaneMode_assistantSettings_summary_ststus_1) +
                            StringConstants.STR_DOUBLE_NEWLINE + summary;
                }
                else {
                    summary = getString(R.string.profile_preferences_deviceAirplaneMode_assistantSettings_summary_ststus_0) +
                            StringConstants.STR_DOUBLE_NEWLINE + summary;
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID)) {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        preference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                } else {
                    preference.setEnabled(true);

                    String sValue = value.toString();
                    boolean bold = !sValue.equals(StringConstants.CONNECTTOSSID_JUSTANY);

                    Profile profile = new Profile();
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    profile._deviceConnectToSSID = preferences.getString(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, StringConstants.CONNECTTOSSID_JUSTANY);
                    Permissions.checkProfileRadioPreferences(context, profile, permissions);
                    boolean _permissionGranted = permissions.isEmpty();

                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, false, false, !_permissionGranted, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_VPN)) {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        preference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                } else {
                    preference.setEnabled(true);

                    String sValue = value.toString();
                    boolean bold = !sValue.startsWith("0");

                    Profile profile = new Profile();
                    ArrayList<PermissionType> permissions = new ArrayList<>();
                    profile._deviceVPN = preferences.getString(Profile.PREF_PROFILE_DEVICE_VPN, "0|0|||0");
                    Permissions.checkProfileVPN(context, profile, permissions);
                    Permissions.checkProfileWireGuard(context, profile, permissions);
                    boolean _permissionGranted = permissions.isEmpty();

                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, false, false, !_permissionGranted, false);
                }
            }
        }
            if (phoneCount > 1) {

                /*
                if (key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1) ||
                        key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0");
                            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                        }
                    } else {
                        String sValue = value.toString();
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            listPreference.setEnabled(true);

                            int index = listPreference.findIndexOfValue(sValue);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);

                            boolean _permissionGranted;
                            Profile profile = new Profile();
                            ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                            profile._deviceMobileDataSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, "0"));
                            profile._deviceMobileDataSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, "0"));
                            Permissions.checkProfileRadioPreferences(context, profile, permissions);
                            //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
                            _permissionGranted = permissions.size() == 0;

                            GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
                        }
                    }
                }
                */
                if (key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1) ||
                        key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0");
                            if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                        }
                    } else {
                        String sValue = value.toString();
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            listPreference.setEnabled(true);

                            int index = listPreference.findIndexOfValue(sValue);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);

                            boolean _permissionGranted;
                            Profile profile = new Profile();
                            ArrayList<PermissionType> permissions = new ArrayList<>();
                            profile._deviceNetworkTypeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, "0"));
                            profile._deviceNetworkTypeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, "0"));
                            Permissions.checkProfileRadioPreferences(context, profile, permissions);
                            _permissionGranted = permissions.isEmpty();

                            GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
                        }
                    }
                }
                if (key.equals(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0|0|0");
                            if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                        }
                    } else {
                        String sValue = value.toString();
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            preference.setEnabled(true);

                            //int index = listPreference.findIndexOfValue(sValue);
                            //CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            //listPreference.setSummary(summary);

                            boolean _permissionGranted;
                            Profile profile = new Profile();
                            ArrayList<PermissionType> permissions = new ArrayList<>();
                            profile._deviceDefaultSIMCards = preferences.getString(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, "0|0|0");
                            Permissions.checkProfileRadioPreferences(context, profile, permissions);
                            _permissionGranted = permissions.isEmpty();

                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !sValue.equals("0|0|0"), false, false, !_permissionGranted, false);
                        }
                    }
                }
                if (key.equals(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1) ||
                        key.equals(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0");
                            if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, false, errorColor, true);
                        }
                    } else {
                        String sValue = value.toString();
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            listPreference.setEnabled(true);

                            int index = listPreference.findIndexOfValue(sValue);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);

                            boolean _permissionGranted;
                            Profile profile = new Profile();
                            ArrayList<PermissionType> permissions = new ArrayList<>();
                            profile._deviceOnOffSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, "0"));
                            profile._deviceOnOffSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, "0"));
                            Permissions.checkProfileRadioPreferences(context, profile, permissions);
                            //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
                            _permissionGranted = permissions.isEmpty();

                            GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted, false);
                        }
                    }
                }

            }
    }

    private void setSummary(String key) {
        String value;
        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
            key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
            key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE) ||
            key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND) ||
            key.equals(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS) ||
            key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED) ||
            key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS) ||
            key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
            setSummary(key, value);
        }
        else
        if (key.equals(PREF_FORCE_STOP_APPLICATIONS_EXTENDER) ||
                key.equals(PREF_LOCK_DEVICE_EXTENDER)) {
            ExtenderDialogPreference preference = prefMng.findPreference(key);
            if (preference != null)
                preference.setSummaryEDP();
        }
        else {
            value = preferences.getString(key, "");
            setSummary(key, value);
        }
    }

    private void updateAllSummary() {
        if (getActivity() == null)
            return;

        final Context context = getActivity().getBaseContext();

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
        disableDependedPref(Profile.PREF_PROFILE_AFTER_DURATION_DO);
        disableDependedPref(Profile.PREF_PROFILE_ASK_FOR_DURATION);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
        disableDependedPref(Profile.PREF_PROFILE_LOCK_DEVICE);
        disableDependedPref(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS);
        disableDependedPref(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED);

        //if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE)
        //{
        setSummary(Profile.PREF_PROFILE_NAME);
        setSummary(Profile.PREF_PROFILE_ICON);
        setSummary(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR);
        setSummary(Profile.PREF_PROFILE_DURATION);
        setSummary(Profile.PREF_PROFILE_AFTER_DURATION_DO);
        setSummary(Profile.PREF_PROFILE_ASK_FOR_DURATION);
        setSummary(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE);
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
        setSummary(PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI);
        setSummary(Profile.PREF_PROFILE_DEVICE_BLUETOOTH);
        setSummary(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT);
        setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA);
        setSummary(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE);
        setSummary(Profile.PREF_PROFILE_DEVICE_GPS);
        setSummary(Profile.PREF_PROFILE_DEVICE_AUTOSYNC);
        setSummary(Profile.PREF_PROFILE_DEVICE_AUTOROTATE);
        setSummary(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS);
        setSummary(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
        setSummary(PREF_FORCE_STOP_APPLICATIONS_EXTENDER);
        //setSummary(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
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
        setSummary(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS);
        setSummary(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
        setSummary(Profile.PREF_PROFILE_LOCK_DEVICE);
        setSummary(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID);
        setSummary(Profile.PREF_PROFILE_APPLICATION_ENABLE_WIFI_SCANNING);
        setSummary(Profile.PREF_PROFILE_APPLICATION_ENABLE_BLUETOOTH_SCANNING);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS);
        setSummary(Profile.PREF_PROFILE_APPLICATION_ENABLE_LOCATION_SCANNING);
        setSummary(Profile.PREF_PROFILE_APPLICATION_ENABLE_MOBILE_CELL_SCANNING);
        setSummary(Profile.PREF_PROFILE_APPLICATION_ENABLE_ORIENTATION_SCANNING);
        setSummary(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS);
        setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS);
        setSummary(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS);
        setSummary(Profile.PREF_PROFILE_SCREEN_DARK_MODE);
        setSummary(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING);
        setSummary(Profile.PREF_PROFILE_SOUND_ON_TOUCH);
        setSummary(PREF_LOCK_DEVICE_EXTENDER);
        //setSummary(PREF_LOCK_DEVICE_INSTALL_EXTENDER);
        setSummary(Profile.PREF_PROFILE_VOLUME_DTMF);
        setSummary(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY);
        setSummary(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO);
        setSummary(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY);
        setSummary(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT);
        setSummary(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND);
        setSummary(Profile.PREF_PROFILE_APPLICATION_ENABLE_NOTIFICATION_SCANNING);
        //setSummary(PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS);
        //setSummary(PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS);
        setSummary(Profile.PREF_PROFILE_GENERATE_NOTIFICATION);
        setSummary(Profile.PREF_PROFILE_CAMERA_FLASH);
        setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1);
        setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2);
        //setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1);
        //setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2);
        setSummary(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS);
        setSummary(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1);
        setSummary(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
        setSummary(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS);
        setSummary(Profile.PREF_PROFILE_DEVICE_LIVE_WALLPAPER);
        setSummary(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN);
        setSummary(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS);
        setSummary(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE);
        setSummary(Profile.PREF_PROFILE_APPLICATION_ENABLE_PERIODIC_SCANNING);
        setSummary(Profile.PREF_PROFILE_DEVICE_VPN);
        setSummary(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING);
        setSummary(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS);
        setSummary(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION);
        setSummary(Profile.PREF_PROFILE_APPLICATION_WIFI_SCAN_INTERVAL);
        setSummary(Profile.PREF_PROFILE_APPLICATION_BLUETOOTH_SCAN_INTERVAL);
        setSummary(Profile.PREF_PROFILE_APPLICATION_BLUETOOTH_LE_SCAN_DURATION);
        setSummary(Profile.PREF_PROFILE_APPLICATION_LOCATION_UPDATE_INTERVAL);
        setSummary(Profile.PREF_PROFILE_APPLICATION_ORIENTATION_SCAN_INTERVAL);
        setSummary(Profile.PREF_PROFILE_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL);
        setSummary(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS);
        setSummary(Profile.PREF_PROFILE_SEND_SMS_CONTACTS);
        //setSummary(Profile.PREF_PROFILE_SEND_SMS_CONTACT_LIST_TYPE);
        setSummary(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS);
        setSummary(Profile.PREF_PROFILE_SEND_SMS_SMS_TEXT);
        setSummary(PREF_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
        setSummary(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED);
        setSummary(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS);
        setSummary(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS);
        setSummary(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS);
        setSummary(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS);
        setSummary(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT);
        setSummary(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_TEXT);
        setSummary(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT);
        setSummary(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT_PREFS);
        setSummary(Profile.PREF_PROFILE_SCREEN_ON_OFF);

        setCategorySummary(PREF_PROFILE_ACTIVATION_DURATION_CATTEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_SOUND_PROFILE_CATTEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_VOLUME_CATTEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_SOUNDS_CATTEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_TOUCH_EFFECTS_CATTEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_VIBRATION_INTENSITY_CATTEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_RADIOS_CATTEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_SCREEN_CATTEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_LED_ACCESSORIES_CATTEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_NOTIFICATIONS_CATTEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_OTHERS_CATTEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_APPLICATION_CATTEGORY_ROOT, context);
        setCategorySummary(PREF_FORCE_STOP_APPLICATIONS_CATEGORY_ROOT, context);
        setCategorySummary(PREF_LOCK_DEVICE_CATEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_DEVICE_RADIOS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_SOUNDS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context);
        setCategorySummary(PREF_DEVICE_WALLPAPER_CATEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_DEVICE_AIRPLANE_MODE_CATEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_SEND_SMS_CATTEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_CLEAR_NOTIFICATIONS_CATTEGORY_ROOT, context);
    }

    private boolean getEnableVolumeNotificationByRingtone(String ringtoneValue) {
        boolean enabled = ProfileStatic.getVolumeChange(ringtoneValue);
        if (enabled) {
            int volume = ProfileStatic.getVolumeValue(ringtoneValue);
            return volume > 0;
        }
        else
            return true;
    }

    private boolean getEnableVolumeNotificationVolume0(boolean notificationEnabled, String notificationValue/*, Context context*/) {
        return  notificationEnabled && ActivateProfileHelper.getMergedRingNotificationVolumes() &&
                ApplicationPreferences.applicationUnlinkRingerNotificationVolumes &&
                ProfileStatic.getVolumeChange(notificationValue) && (ProfileStatic.getVolumeValue(notificationValue) == 0);
    }

    private void disableDependentPrefsScreenOnOffDeviceKeyguard(String key, String value) {
        if (getActivity() == null)
            return;
        Context context = getActivity().getApplicationContext();
        if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_KEYGUARD, null, preferences, true, context).preferenceAllowed
                != PreferenceAllowed.PREFERENCE_ALLOWED) {
            Preference preference = findPreference(Profile.PREF_PROFILE_DEVICE_KEYGUARD);
            if (preference != null)
                preference.setEnabled(false);
        }
        if (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SCREEN_ON_OFF, null, preferences, true, context).preferenceAllowed
                != PreferenceAllowed.PREFERENCE_ALLOWED) {
            Preference preference = findPreference(Profile.PREF_PROFILE_SCREEN_ON_OFF);
            if (preference != null)
                preference.setEnabled(false);
        }

        String screenOnOffValue = preferences.getString(Profile.PREF_PROFILE_SCREEN_ON_OFF, "");
        String deviceKeyguardValue = preferences.getString(Profile.PREF_PROFILE_DEVICE_KEYGUARD, "");
        if ((!screenOnOffValue.equals("0")) && (!deviceKeyguardValue.equals("0"))) {
            // if both are configured, force disable devcieKeyguard
            Preference preference = findPreference(Profile.PREF_PROFILE_DEVICE_KEYGUARD);
            if (preference != null)
                preference.setEnabled(false);
        } else {
            if (key.equals(Profile.PREF_PROFILE_SCREEN_ON_OFF)) {
                //value = sharedPreferences.getString(Profile.PREF_PROFILE_SCREEN_ON_OFF, "0");
                Preference preference = findPreference(Profile.PREF_PROFILE_DEVICE_KEYGUARD);
                if (preference != null)
                    preference.setEnabled(value.equals("0"));
            }
            if (key.equals(Profile.PREF_PROFILE_DEVICE_KEYGUARD)) {
                //value = sharedPreferences.getString(Profile.PREF_PROFILE_DEVICE_KEYGUARD, "0");
                Preference preference = findPreference(Profile.PREF_PROFILE_SCREEN_ON_OFF);
                if (preference != null)
                    preference.setEnabled(value.equals("0"));
            }
        }
    }

    private void disableDependedPref(String key, Object value) {
        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        String sValue = value.toString();

        //final String ON = "1";

        boolean enabledMuteSound = preferences.getBoolean(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND, false);
        if (key.equals(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE)) {
            if (!enabledMuteSound) {

                String ringtoneValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
                boolean enabled = (!ActivateProfileHelper.getMergedRingNotificationVolumes() || ApplicationPreferences.applicationUnlinkRingerNotificationVolumes) &&
                        getEnableVolumeNotificationByRingtone(ringtoneValue);
                Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
                if (preference != null)
                    preference.setEnabled(enabled);

                String notificationValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, "");
                enabled = getEnableVolumeNotificationVolume0(enabled, notificationValue);
                preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
                if (preference != null)
                    preference.setEnabled(enabled);

                String ringerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "0");
                enabled = (ProfileStatic.getVolumeChange(ringtoneValue) ||
                        ProfileStatic.getVolumeChange(notificationValue)) &&
                        ringerMode.equals("0");
                preference = prefMng.findPreference(PREF_PROFILE_VOLUME_SOUND_MODE_INFO);
                if (preference != null)
                    preference.setEnabled(enabled);
            } else {
                Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_RINGTONE);
                if (preference != null)
                    preference.setEnabled(false);
                preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
                if (preference != null)
                    preference.setEnabled(false);
            }
        }
        Preference _preference;
        if (enabledMuteSound) {
            _preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
            if (_preference != null)
                _preference.setEnabled(false);
            _preference = prefMng.findPreference(PREF_PROFILE_VOLUME_SOUND_MODE_INFO);
            if (_preference != null)
                _preference.setEnabled(false);
        }
        _preference = prefMng.findPreference(PREF_PROFILE_VOLUME_RINGTONE0_INFO);
        if (_preference != null)
            _preference.setEnabled(!enabledMuteSound);
        _preference = prefMng.findPreference(PREF_PROFILE_VOLUME_IGNORE_SOUND_MODE_INFO2);
        if (_preference != null)
            _preference.setEnabled(!enabledMuteSound);
        _preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_SYSTEM);
        if (_preference != null)
            _preference.setEnabled(!enabledMuteSound);
        _preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_MEDIA);
        if (_preference != null)
            _preference.setEnabled(!enabledMuteSound);
        _preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_DTMF);
        if (_preference != null)
            _preference.setEnabled(!enabledMuteSound);

        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE)) {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE)) {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE)) {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_ALARM);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE)) {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
            if (preference != null)
                preference.setEnabled(enabled && (sValue.equals("1") || sValue.equals("4")));
            preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_LOCKSCREEN);
            if (preference != null)
                preference.setEnabled(enabled && (sValue.equals("1")));
            preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
            if (preference != null)
                preference.setEnabled(enabled && (sValue.equals("1") || sValue.equals("3")));
            preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_LIVE_WALLPAPER);
            if (preference != null)
                preference.setEnabled(enabled && sValue.equals("2"));
            preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER);
            if (preference != null)
                preference.setEnabled(enabled && sValue.equals("3"));
            preference = prefMng.findPreference(PREF_PROFILE_DEVICE_WALLPAPER_FOLDER_INFO);
            if (preference != null)
                preference.setEnabled(enabled && sValue.equals("3"));
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE)) {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            if (preference != null)
                preference.setEnabled(enabled);
            preference = prefMng.findPreference(PREF_PROFILE_DEVICE_RUN_APPLICATION_MIUI_PERMISSIONS);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        //if (Build.VERSION.SDK_INT < 30) {
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP)) {
            PPListPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WIFI);
            if (preference != null) {
                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.preferenceAllowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    int iValue = Integer.parseInt(sValue);
                    if (iValue > 0)
                        preference.setValue(Profile.NO_CHANGE_VALUE_STR);
                    preference.setEnabled(false);
                } else
                    preference.setEnabled(true);
            }
        }
        //}
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE)) {
            String ringerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "0");
            String zenMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "0");
            boolean enabled = false;

            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, null, preferences, true, context);
            if ((preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                    ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                            (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                            (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                            (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) ||
                            (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))) {
                if (ringerMode.equals("1") || ringerMode.equals("4"))
                    enabled = true;
                if (ringerMode.equals("5")) {
                    if (zenMode.equals("1") || zenMode.equals("2"))
                        enabled = true;
                }
            }
            PPListPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null) {
                if (!enabled)
                    preference.setValue(Profile.NO_CHANGE_VALUE_STR);
                preference.setEnabled(enabled);
            }

            if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
                enabled = false;
                preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, null, preferences, true, context);
                if ((preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                        ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_SHIZUKU_NOT_GRANTED) ||
                                (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))) {
                    if (ringerMode.equals("1") || ringerMode.equals("4"))
                        enabled = true;
                    if (ringerMode.equals("5")) {
                        if (zenMode.equals("1") || zenMode.equals("2"))
                            enabled = true;
                    }
                }
                preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS);
                if (preference != null) {
                    if (!enabled)
                        preference.setValue(Profile.NO_CHANGE_VALUE_STR);
                    preference.setEnabled(enabled);
                }
//                PPPPSDialogPreference ppppsPreference = prefMng.findPreference(PREF_PROFILE_SOUND_PROFILE_PPPPS);
//                if (ppppsPreference != null) {
//                    ppppsPreference.setEnabled(enabled);
//                }
            }
        }

        if (key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE)) {
            String defaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            String _sValue = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, defaultValue);
            boolean enabled = true;
            if ("1".equals(_sValue)) {
                setSummary(PREF_FORCE_STOP_APPLICATIONS_EXTENDER);
                //setSummary(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
                enabled = PPExtenderBroadcastReceiver.isEnabled(context, PPApplication.VERSION_CODE_EXTENDER_REQUIRED, true, false
                        /*, "ProfilesPrefsFragment.disableDependedPref (Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE)"*/);
                //enabled = PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, true);
            }
            else
            if ("2".equals(_sValue)) {
                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, null, preferences, true, context);
                enabled = preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED;
            }

            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            if (preference != null) {
                //preference.setEnabled(enabled);
                setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            }
            ApplicationsMultiSelectDialogPreference appPreference =
                    prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
            if (appPreference != null) {
                appPreference.setEnabled(enabled && (!(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR))));
                appPreference.setSummaryAMSDP();
            }
        }

        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
            setSummary(PREF_LOCK_DEVICE_EXTENDER);
            //setSummary(PREF_LOCK_DEVICE_INSTALL_EXTENDER);
            //setSummary(PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_LOCK_DEVICE);
            if (preference != null) {
                setSummary(Profile.PREF_PROFILE_LOCK_DEVICE);
            }
        }

        if (key.equals(Profile.PREF_PROFILE_DURATION) ||
                key.equals(Profile.PREF_PROFILE_AFTER_DURATION_DO) ||
                key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
                key.equals(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE)) {
            String sEndOfActivationType = preferences.getString(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE));
            int endOfActivationType = 0;
            if (sEndOfActivationType != null)
                endOfActivationType = Integer.parseInt(sEndOfActivationType);
            Preference durationPreference = prefMng.findPreference(Profile.PREF_PROFILE_DURATION);
            Preference endOfActivationTimePreference = prefMng.findPreference(Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME);
            if (durationPreference != null)
                durationPreference.setEnabled(endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION);
            if (endOfActivationTimePreference != null)
                endOfActivationTimePreference.setEnabled(endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME);

            String duration = preferences.getString(Profile.PREF_PROFILE_DURATION, "0");
            boolean askForDuration = preferences.getBoolean(Profile.PREF_PROFILE_ASK_FOR_DURATION, false);

            boolean enable;
            if (endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION)
                enable = (!askForDuration) && (!duration.equals("0"));
            else
                enable = (!askForDuration);

            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_AFTER_DURATION_DO);
            if (preference != null)
                preference.setEnabled(enable);

            preference = prefMng.findPreference(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE);
            if (preference != null) {
                String afterDurationDo = preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_DO, "0");
                int afterDurationDoValue = Integer.parseInt(afterDurationDo);
                preference.setEnabled(enable &&
                        ((afterDurationDoValue == Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE) ||
                                (afterDurationDoValue == Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE_THEN_RESTART_EVENTS)));
            }
        }

        if (((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) ||
                (PPApplication.deviceIsOnePlus))) {
            if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1)) {
                boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
                Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
                if (preference != null)
                    preference.setEnabled(enabled);
            }
            if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1)) {
                boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
                Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
                if (preference != null)
                    preference.setEnabled(enabled);
            }
            if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2)) {
                boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
                Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
                if (preference != null)
                    preference.setEnabled(enabled);
            }
            if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2)) {
                boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
                Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
                if (preference != null)
                    preference.setEnabled(enabled);
            }
        }
        /*
            if (key.equals(PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS)) {
                // RECORD_AUDIO must be granted for set PPP as default assistant
                // must be enabled when PPP is defult assistant
                // because must be possible remove it
                boolean enabled = ActivateProfileHelper.isPPPSetAsDefaultAssistant(context) ||
                                    Permissions.checkMicrophone(context);
                Preference preference = prefMng.findPreference(PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS);
                if (preference != null)
                    preference.setEnabled(enabled);
            }
        */

        if (key.equals(Profile.PREF_PROFILE_SEND_SMS_CONTACTS) ||
                key.equals(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS) ||
                //key.equals(Profile.PREF_PROFILE_SEND_SMS_CONTACT_LIST_TYPE) ||
                key.equals(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS)) {
            //int phoneCallsContactListType = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SEND_SMS_CONTACT_LIST_TYPE,
            //        Profile.defaultValuesString.get(Profile.PREF_PROFILE_SEND_SMS_CONTACT_LIST_TYPE)));
            String contactGroupsValue = preferences.getString(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS));
            String contactsValue = preferences.getString(Profile.PREF_PROFILE_SEND_SMS_CONTACTS,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_SEND_SMS_CONTACTS));

            boolean contactsConfigured =
                    /*(phoneCallsContactListType == EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) ||*/
                    ((contactGroupsValue != null) && (!contactGroupsValue.isEmpty())) ||
                            ((contactsValue != null) && (!contactsValue.isEmpty()));

            //Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS);
            //if (preference != null)
            //    preference.setEnabled(isHeld);
            //preference = prefMng.findPreference(Profile.PREF_PROFILE_SEND_SMS_CONTACTS);
            //if (preference != null)
            //    preference.setEnabled(isHeld);
            //preference = prefMng.findPreference(Profile.PREF_PROFILE_PHONE_CALLS_CONTACT_LIST_TYPE);
            //if (preference != null)
            //    preference.setEnabled(isHeld);

            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS);
            if (preference != null)
                preference.setEnabled(contactsConfigured);
            preference = prefMng.findPreference(Profile.PREF_PROFILE_SEND_SMS_SMS_TEXT);
            if (preference != null)
                preference.setEnabled(contactsConfigured);
        }

        if (key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED) ||
                key.equals(PREF_NOTIFICATION_ACCESS_SYSTEM_SETTINGS) ||
                key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS) ||
                key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS) ||
                key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS) ||
                key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS) ||
                key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT) ||
                key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_TEXT)) {

            boolean listenerEnabled = PPNotificationListenerService.isNotificationListenerServiceEnabled(context, false);
            //noinspection DataFlowIssue
            boolean clearEnabled = preferences.getBoolean(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED,
                    Profile.defaultValuesBoolean.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED));
            String applicationsSetValue = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS));
            boolean applicationsSet = (applicationsSetValue != null) &&
                    (!applicationsSetValue.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS)));

            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS);
            if (preference != null)
                preference.setEnabled(listenerEnabled);

            preference = prefMng.findPreference(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED);
            if (preference != null)
                preference.setEnabled(listenerEnabled && applicationsSet);

            preference = prefMng.findPreference(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS);
            if (preference != null)
                preference.setEnabled(listenerEnabled && clearEnabled && applicationsSet);
            preference = prefMng.findPreference(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT);
            if (preference != null)
                preference.setEnabled(listenerEnabled && clearEnabled && applicationsSet);
        }

        disableDependentPrefsScreenOnOffDeviceKeyguard(key, sValue);
    }

    private void disableDependedPref(String key) {
        String value;
        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
            key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND) ||
            key.equals(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS) ||
            key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED) ||
            key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS) ||
            key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
        }
        else
            value = preferences.getString(key, "");

        /*
        if (key.equals(Profile.PREF_PROFILE_SEND_SMS_CONTACTS) ||
            key.equals(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS) ||
            key.equals(PREF_NOTIFICATION_ACCESS_SYSTEM_SETTINGS) ||
            key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS) ||
            key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS) ||
            key.equals(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS)) {
            value = preferences.getString(key, "");
        }
        */

        disableDependedPref(key, value);
    }

    void setRedTextToPreferences() {
        if (nestedFragment)
            return;

        if (getActivity() == null)
            return;

        final ProfilesPrefsActivity activity = (ProfilesPrefsActivity)getActivity();

        Context context = activity.getApplicationContext();

        setRedTextToPreferencesAsyncTask =
                new SetRedTextToPreferencesAsyncTask
                        ((ProfilesPrefsActivity) getActivity(), this, prefMng, context);
        setRedTextToPreferencesAsyncTask.execute();
    }

    // this is required for "Do not disturb"
    private void enableNotificationPolicyAccess(
            @SuppressWarnings("SameParameterValue") boolean showDoNotDisturbPermission) {
        boolean ok = false;
        if (showDoNotDisturbPermission) {
            // Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS exists
            try {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                //noinspection deprecation
                startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                ok = true;
            } catch (Exception e) {
                if (getActivity() != null) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            getString(R.string.phone_profiles_pref_notificationSystemSettings),
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
                            false,
                            getActivity()
                    );

                    if (!getActivity().isFinishing())
                        dialog.show();
                }
            }
        }
        /*else
        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS, getActivity())) {
            try {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                ok = true;
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        }*/
        if (!ok) {
            if (getActivity() != null) {
                PPAlertDialog dialog = new PPAlertDialog(
                        getString(R.string.phone_profiles_pref_notificationSystemSettings),
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
                        false,
                        getActivity()
                );

                if (!getActivity().isFinishing())
                    dialog.show();
            }
        }
    }

    /*
    private void installExtenderFromGitHub() {
        if (getActivity() == null) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.install_extender_dialog_title);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_install_ppp_pppe_from_github, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_info_text);

        String dialogText = "";

        int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(getActivity().getApplicationContext());
        if (extenderVersion != 0) {
            String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(getActivity().getApplicationContext());
            dialogText = dialogText + getString(R.string.install_extender_installed_version) + " " + extenderVersionName + " (" + extenderVersion + ")\n";
        }
        dialogText = dialogText + getString(R.string.install_extender_required_version) +
                " " + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")\n\n";
        dialogText = dialogText + getString(R.string.install_extender_text1) + " \"" + getString(R.string.alert_button_install) + "\".\n\n";
        dialogText = dialogText + getString(R.string.install_extender_text2) + "\n\n";
        dialogText = dialogText + getString(R.string.install_extender_text3);

        text.setText(dialogText);

        text = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_github_releases);
        CharSequence str1 = getString(R.string.install_extender_github_releases);
        CharSequence str2 = str1 + " " + PPApplication.GITHUB_PPPE_RELEASES_URL + "\u00A0»»";
        Spannable sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = PPApplication.GITHUB_PPPE_RELEASES_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    if (getActivity() != null)
                        getActivity().startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
            //String url = PPApplication.GITHUB_PPPE_DOWNLOAD_URL_1 + PPApplication.VERSION_NAME_EXTENDER_LATEST + PPApplication.GITHUB_PPPE_DOWNLOAD_URL_2;
            String url = PPApplication.GITHUB_PPPE_DOWNLOAD_URL;

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if ((getActivity() != null) && (!getActivity().isFinishing()))
            dialog.show();
    }

    private void installExtender() {
        if (getActivity() == null) {
            return;
        }

        PackageManager packageManager = getActivity().getPackageManager();
        Intent _intent = packageManager.getLaunchIntentForPackage(PPApplication.GALAXY_STORE_PACKAGE_NAME);
        boolean galaxyStoreInstalled = (_intent != null);

        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy && galaxyStoreInstalled) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder.setTitle(R.string.install_extender_dialog_title);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_install_pppe_from_store, null);
            dialogBuilder.setView(layout);

            TextView text = layout.findViewById(R.id.install_pppe_from_store_dialog_info_text);

            String dialogText = "";

            int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(getActivity().getApplicationContext());
            if (extenderVersion != 0) {
                String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(getActivity().getApplicationContext());
                dialogText = dialogText + getString(R.string.install_extender_installed_version) + " " + extenderVersionName + " (" + extenderVersion + ")\n";
            }
            dialogText = dialogText + getString(R.string.install_extender_required_version) +
                    " " + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")\n\n";
            dialogText = dialogText + getString(R.string.install_extender_text1) + " \"" + getString(R.string.alert_button_install) + "\".";

            text.setText(dialogText);

            dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("samsungapps://ProductDetail/sk.henrichg.phoneprofilesplusextender"));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            Button button = layout.findViewById(R.id.install_pppe_from_store_dialog_installFromGitHub);

            final AlertDialog dialog = dialogBuilder.create();

            button.setText(getActivity().getString(R.string.alert_button_install_extender_from_github));
            button.setOnClickListener(v -> {
                dialog.cancel();
                installExtenderFromGitHub();
            });

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

            if ((getActivity() != null) && (!getActivity().isFinishing()))
                dialog.show();
        }
        else
            installExtenderFromGitHub();
    }
    */
    /*
    private void enableExtender() {
        if (getActivity() == null)
            return;

        boolean ok = false;
        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, getActivity())) {
            try {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                ok = true;
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        }
        if (!ok) {
            if (getActivity() != null) {
                PPAlertDialog dialog = new PPAlertDialog(
                        getString(R.string.event_preferences_applications_AccessibilitySettings_title),
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
                        getActivity()
                );

                if (!getActivity().isFinishing())
                    dialog.show();
            }
        }
    }
    */

    private void configureAssistant() {
        if (getActivity() == null)
            return;

        boolean ok = false;
        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_VOICE_INPUT_SETTINGS, getActivity())) {
            try {
                //activity.startActivity(new Intent("android.settings.VOICE_INPUT_SETTINGS"));

                Intent intent = new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS);
                //noinspection deprecation
                startActivityForResult(intent, RESULT_ASSISTANT_SETTINGS);
                ok = true;
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        }
        if (!ok) {
            if (getActivity() != null) {
                PPAlertDialog dialog = new PPAlertDialog(
                        getString(R.string.profile_preferences_deviceAirplaneMode_assistantSettings),
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
                        false,
                        getActivity()
                );

                if (!getActivity().isFinishing())
                    dialog.show();
            }
        }
    }

    /*
    private void installPPPPutSettings() {
        if (getActivity() == null) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.install_pppps_dialog_title);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_install_pppps, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_pppps_from_github_dialog_info_text);

        String dialogText = "";

        dialogText = dialogText + getString(R.string.install_pppps_text1) + " \"" + getString(R.string.alert_button_install) + "\"\n";
        dialogText = dialogText + getString(R.string.install_pppps_text2) + "\n";
        dialogText = dialogText + getString(R.string.install_pppps_text3) + "\n\n";
        dialogText = dialogText + getString(R.string.install_pppps_text4);
        text.setText(dialogText);

        dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
            String url = PPApplication.GITHUB_PPPPS_DOWNLOAD_URL;

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        dialogBuilder.setCancelable(false);
        //dialogBuilder.setOnCancelListener(dialog -> {
        //    if (finishActivity)
        //        activity.finish();
        //});

        final AlertDialog dialog = dialogBuilder.create();

        text = layout.findViewById(R.id.install_pppps_from_github_dialog_github_releases);
        CharSequence str1 = getString(R.string.install_extender_github_releases);
        CharSequence str2 = str1 + " " + PPApplication.GITHUB_PPPPS_RELEASES_URL + "\u00A0»»";
        Spannable sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = PPApplication.GITHUB_PPPPS_RELEASES_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    dialog.cancel();
                    //if (activity != null)
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());


//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if ((getActivity() != null) && (!getActivity().isFinishing()))
            dialog.show();

    }
    */

    private void fillDeviceNetworkTypePreference(String key, Context context) {
        PPListPreference networkTypePreference = prefMng.findPreference(key);
        if (networkTypePreference != null) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (!key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                int subscriptionId = -1;

                SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                //SubscriptionManager.from(context);
                if (mSubscriptionManager != null) {
                    List<SubscriptionInfo> subscriptionList = null;
                    try {
                        if (Permissions.hasPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                        }
                    } catch (SecurityException e) {
                        PPApplicationStatic.recordException(e);
                    }
                    if (subscriptionList != null) {
                        int size = subscriptionList.size(); /*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/
                        for (int i = 0; i < size; i++) {
                            // Get the active subscription ID for a given SIM card.
                            SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                            if (subscriptionInfo != null) {
                                int slotIndex = subscriptionInfo.getSimSlotIndex();
                                if ((slotIndex == 0) && key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1))
                                    subscriptionId = subscriptionInfo.getSubscriptionId();
                                if ((slotIndex == 1) && key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2))
                                    subscriptionId = subscriptionInfo.getSubscriptionId();
                            }
                        }
                    }
                }

                if (subscriptionId != -1) {
                    if (telephonyManager != null)
                        telephonyManager = telephonyManager.createForSubscriptionId(subscriptionId);
                }
            }

            int phoneType = TelephonyManager.PHONE_TYPE_GSM;
            if (telephonyManager != null)
                phoneType = telephonyManager.getPhoneType();

            if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                    /*if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMDPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMDPValues));
                    } else {*/

                // https://github.com/aosp-mirror/platform_frameworks_base/blob/master/telephony/java/com/android/internal/telephony/RILConstants.java
                networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMArray));
                networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMValues));

                //}
                String value = preferences.getString(key, "");
                networkTypePreference.setValue(value);
                setSummary(key, value);
            }

            if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    /*if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMADPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMADPValues));
                    } else {*/

                // https://github.com/aosp-mirror/platform_frameworks_base/blob/master/telephony/java/com/android/internal/telephony/RILConstants.java
                networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMAArray));
                networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMAValues));

                //}
                String value = preferences.getString(key, "");
                networkTypePreference.setValue(value);
                setSummary(key, value);
            }
        }
    }

    private void setRingtonePreferenceSummary(final String initSummary, final String ringtoneUri,
                                             final androidx.preference.Preference preference, final Context context) {
        setRingtonePreferenceSummaryAsyncTask =
                new SetRingtonePreferenceSummaryAsyncTask(initSummary, ringtoneUri, preference, context);
        setRingtonePreferenceSummaryAsyncTask.execute();
    }

    private static class SetRingtonePreferenceSummaryAsyncTask extends AsyncTask<Void, Integer, Void> {
        private String ringtoneName;

        final String initSummary;
        final String ringtoneUri;
        private final WeakReference<Preference> preferenceWeakRef;
        private final WeakReference<Context> contextWeakReference;

        public SetRingtonePreferenceSummaryAsyncTask(final String initSummary, final String ringtoneUri,
                                                     final androidx.preference.Preference preference, final Context context) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.contextWeakReference = new WeakReference<>(context);
            this.initSummary = initSummary;
            this.ringtoneUri = ringtoneUri;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Context context = contextWeakReference.get();
            androidx.preference.Preference preference = preferenceWeakRef.get();
            if ((context != null) && (preference != null)) {
                if ((ringtoneUri == null) || ringtoneUri.isEmpty())
                    ringtoneName = context.getString(R.string.ringtone_preference_none);
                else {
                    Uri uri = Uri.parse(ringtoneUri);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        ringtoneName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        ringtoneName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Context context = contextWeakReference.get();
            androidx.preference.Preference preference = preferenceWeakRef.get();
            if ((context != null) && (preference != null)) {
                String summary = TextUtils.replace(initSummary, new String[]{TAG_RINGTONE_NAME}, new String[]{ringtoneName}).toString();
                preference.setSummary(StringFormatUtils.fromHtml(summary, false,  false, 0, 0, true));
            }
        }

    }

    private void setProfileSoundsPreferenceSummary(final String initSummary,
                                                  final String ringtoneUri, final String notificationUri, final String alarmUri,
                                                  final androidx.preference.Preference preference, final Context context) {
        setProfileSoundsPreferenceSummaryAsyncTask =
                new SetProfileSoundsPreferenceSummaryAsyncTask(initSummary,
                        ringtoneUri, notificationUri, alarmUri,
                        preference, context);
        setProfileSoundsPreferenceSummaryAsyncTask.execute();
    }

    private static class SetProfileSoundsPreferenceSummaryAsyncTask extends AsyncTask<Void, Integer, Void> {
        private String ringtoneName;
        private String notificationName;
        private String alarmName;

        final String initSummary;
        final String ringtoneUri;
        final String notificationUri;
        final String alarmUri;
        private final WeakReference<androidx.preference.Preference> preferenceWeakRef;
        private final WeakReference<Context> contextWeakReference;

        public SetProfileSoundsPreferenceSummaryAsyncTask(final String initSummary,
                                                          final String ringtoneUri, final String notificationUri, final String alarmUri,
                                                          final androidx.preference.Preference preference, final Context context) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.contextWeakReference = new WeakReference<>(context);
            this.initSummary = initSummary;
            this.ringtoneUri = ringtoneUri;
            this.notificationUri = notificationUri;
            this.alarmUri = alarmUri;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Context context = contextWeakReference.get();
            androidx.preference.Preference preference = preferenceWeakRef.get();
            if ((context != null) && (preference != null)) {
                if ((ringtoneUri == null) || ringtoneUri.isEmpty())
                    ringtoneName = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = ringtoneUri.split(StringConstants.STR_SPLIT_REGEX);
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        ringtoneName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        ringtoneName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((notificationUri == null) || notificationUri.isEmpty())
                    notificationName = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = notificationUri.split(StringConstants.STR_SPLIT_REGEX);
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        notificationName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        notificationName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((alarmUri == null) || alarmUri.isEmpty())
                    alarmName = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = alarmUri.split(StringConstants.STR_SPLIT_REGEX);
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        alarmName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        alarmName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Context context = contextWeakReference.get();
            androidx.preference.Preference preference = preferenceWeakRef.get();
            if ((context != null) && (preference != null)) {
                String summary = TextUtils.replace(initSummary,
                        new String[]{TAG_RINGTONE_NAME, TAG_NOTIFICATION_NAME, TAG_ALARM_NAME},
                        new String[]{ringtoneName, notificationName, alarmName}).toString();
                preference.setSummary(StringFormatUtils.fromHtml(summary, false,  false, 0, 0, true));
            }
        }

    }

    private void setProfileSoundsDualSIMPreferenceSummary(final String initSummary,
                                                         final String ringtoneSIM1Uri, final String ringtoneSIM2Uri,
                                                         final String notificationSIM1Uri, final String notificationSIM2Uri,
                                                         final androidx.preference.Preference preference, final Context context) {
        setProfileSoundsDualSIMPreferenceSummaryAsyncTask =
                new SetProfileSoundsDualSIMPreferenceSummaryAsyncTask(initSummary,
                        ringtoneSIM1Uri, ringtoneSIM2Uri, notificationSIM1Uri, notificationSIM2Uri,
                        preference, context);
        setProfileSoundsDualSIMPreferenceSummaryAsyncTask.execute();
    }

    private static class SetProfileSoundsDualSIMPreferenceSummaryAsyncTask extends AsyncTask<Void, Integer, Void> {

        private String ringtoneNameSIM1;
        private String ringtoneNameSIM2;
        private String notificationNameSIM1;
        private String notificationNameSIM2;

        final String initSummary;
        final String ringtoneSIM1Uri;
        final String ringtoneSIM2Uri;
        final String notificationSIM1Uri;
        final String notificationSIM2Uri;
        private final WeakReference<androidx.preference.Preference> preferenceWeakRef;
        private final WeakReference<Context> contextWeakReference;

        public SetProfileSoundsDualSIMPreferenceSummaryAsyncTask(final String initSummary,
                                                                 final String ringtoneSIM1Uri, final String ringtoneSIM2Uri,
                                                                 final String notificationSIM1Uri, final String notificationSIM2Uri,
                                                                 final androidx.preference.Preference preference, final Context context) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.contextWeakReference = new WeakReference<>(context);
            this.initSummary = initSummary;
            this.ringtoneSIM1Uri = ringtoneSIM1Uri;
            this.ringtoneSIM2Uri = ringtoneSIM2Uri;
            this.notificationSIM1Uri = notificationSIM1Uri;
            this.notificationSIM2Uri = notificationSIM2Uri;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Context context = contextWeakReference.get();
            androidx.preference.Preference preference = preferenceWeakRef.get();
            if ((context != null) && (preference != null)) {
                if ((ringtoneSIM1Uri == null) || ringtoneSIM1Uri.isEmpty())
                    ringtoneNameSIM1 = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = ringtoneSIM1Uri.split(StringConstants.STR_SPLIT_REGEX);
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        ringtoneNameSIM1 = ringtone.getTitle(context);
                    } catch (Exception e) {
                        ringtoneNameSIM1 = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((ringtoneSIM2Uri == null) || ringtoneSIM2Uri.isEmpty())
                    ringtoneNameSIM2 = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = ringtoneSIM2Uri.split(StringConstants.STR_SPLIT_REGEX);
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        ringtoneNameSIM2 = ringtone.getTitle(context);
                    } catch (Exception e) {
                        ringtoneNameSIM2 = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((notificationSIM1Uri == null) || notificationSIM1Uri.isEmpty())
                    notificationNameSIM1 = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = notificationSIM1Uri.split(StringConstants.STR_SPLIT_REGEX);
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        notificationNameSIM1 = ringtone.getTitle(context);
                    } catch (Exception e) {
                        notificationNameSIM1 = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((notificationSIM2Uri == null) || notificationSIM2Uri.isEmpty())
                    notificationNameSIM2 = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = notificationSIM2Uri.split(StringConstants.STR_SPLIT_REGEX);
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        notificationNameSIM2 = ringtone.getTitle(context);
                    } catch (Exception e) {
                        notificationNameSIM2 = context.getString(R.string.ringtone_preference_not_set);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Context context = contextWeakReference.get();
            androidx.preference.Preference preference = preferenceWeakRef.get();
            if ((context != null) && (preference != null)) {
                String summary = TextUtils.replace(initSummary,
                        new String[]{TAG_RINGTONE_NAME_SIM1, TAG_RINGTONE_NAME_SIM2, TAG_NOTIFICATION_NAME_SIM1, TAG_NOTIFICATION_NAME_SIM2},
                        new String[]{ringtoneNameSIM1, ringtoneNameSIM2, notificationNameSIM1, notificationNameSIM2}).toString();
                preference.setSummary(StringFormatUtils.fromHtml(summary, false,  false, 0, 0, true));
            }
        }

    }

    private static class SetRedTextToPreferencesAsyncTask extends AsyncTask<Void, Integer, Void> {

        Profile profile;
        PreferenceAllowed preferenceAllowed;
        ArrayList<PermissionType> profilePermissions;
        boolean canChangeZenMode;
        int accessibilityEnabled;

        private final WeakReference<PreferenceManager> prefMngWeakRef;
        private final WeakReference<Context> contextWeakReference;
        private final WeakReference<ProfilesPrefsActivity> activityWeakReference;
        private final WeakReference<ProfilesPrefsFragment> fragmentWeakReference;

        public SetRedTextToPreferencesAsyncTask(final ProfilesPrefsActivity activity,
                                                final ProfilesPrefsFragment fragment,
                                                final PreferenceManager prefMng,
                                                final Context context) {
            this.prefMngWeakRef = new WeakReference<>(prefMng);
            this.contextWeakReference = new WeakReference<>(context);
            this.activityWeakReference = new WeakReference<>(activity);
            this.fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Context context = contextWeakReference.get();
            ProfilesPrefsActivity activity = activityWeakReference.get();

            if ((context != null) && (activity != null)) {

                long profile_id = activity.profile_id;
                if (profile_id != 0) {
                    int newProfileMode = activity.newProfileMode;
                    int predefinedProfileIndex = activity.predefinedProfileIndex;

                    profile = activity
                            .getProfileFromPreferences(profile_id, newProfileMode, predefinedProfileIndex);
                    if (profile != null) {
                        // test only root or G1 parameters, because key is not set but profile is
                        preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed("-", profile, null, true, context);
                        profilePermissions = Permissions.checkProfilePermissions(context, profile);
                        canChangeZenMode = ActivateProfileHelper.canChangeZenMode(context);
                        accessibilityEnabled = profile.isAccessibilityServiceEnabled(context.getApplicationContext(), false);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Context context = contextWeakReference.get();
            PreferenceManager prefMng = prefMngWeakRef.get();
            ProfilesPrefsActivity activity = activityWeakReference.get();
            ProfilesPrefsFragment fragment = fragmentWeakReference.get();

            if ((context != null) && (activity != null) && (fragment != null) && (prefMng != null)) {

                String rootScreen = PPApplication.PREF_ROOT_SCREEN;

                int errorColor = ContextCompat.getColor(context, R.color.errorColor);

                boolean hidePreferences = false;
                int order = 1;

                if (profile != null) {
                    Preference preference = prefMng.findPreference(PREF_GRANT_G1_PREFERENCES);
                    if (!preferenceAllowed.notAllowedG1) {
                        if (preference != null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null)
                                preferenceCategory.removePreference(preference);
                        }
                    } else {
                        //if (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) {
                        if (preference == null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null) {
                                preference = new StartActivityPreference(context);
                                preference.setKey(PREF_GRANT_G1_PREFERENCES);
                                preference.setIconSpaceReserved(false);
                                preference.setLayoutResource(R.layout.mp_preference_material_widget);
                                preference.setOrder(-100);
                                preferenceCategory.addPreference(preference);
                            }
                        }
                        if (preference != null) {
                            String _title = order + ". " + context.getString(R.string.preferences_grantG1Preferences_title);
                            ++order;
                            Spannable title = new SpannableString(_title);
                            title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                            preference.setTitle(title);
                            Spannable summary = new SpannableString(context.getString(R.string.preferences_grantG1Preferences_summary));
                            summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                            preference.setSummary(summary);

                            preference.setOnPreferenceClickListener(preference12 -> {
                                Permissions.grantG1Permission(fragment, activity);
                                return false;
                            });
                        }
                        //}
                    }

                    //preferenceAllowed = Profile.isProfilePreferenceAllowed("-", profile, null, true, false, true, context);
                    // not enabled grant root
                    //if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if ((!preferenceAllowed.notAllowedRoot) || (!RootUtils.isRooted())) {
                        preference = prefMng.findPreference(PREF_GRANT_ROOT);
                        if (preference != null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null)
                                preferenceCategory.removePreference(preference);
                        }
                    } else {
                        //if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) && (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED)) {
                        if (!ApplicationPreferences.applicationNeverAskForGrantRoot) {
                            preference = prefMng.findPreference(PREF_GRANT_ROOT);
                            if (preference == null) {
                                PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                                if (preferenceCategory != null) {
                                    preference = new StartActivityPreference(context);
                                    preference.setKey(PREF_GRANT_ROOT);
                                    preference.setIconSpaceReserved(false);
                                    preference.setLayoutResource(R.layout.mp_preference_material_widget);
                                    preference.setOrder(-100);
                                    preferenceCategory.addPreference(preference);
                                }
                            }
                            if (preference != null) {
                                String _title = order + ". " + context.getString(R.string.preferences_grantRoot_title);
                                ++order;
                                Spannable title = new SpannableString(_title);
                                title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                                preference.setTitle(title);
                                Spannable summary = new SpannableString(context.getString(R.string.preferences_grantRoot_summary));
                                summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                                preference.setSummary(summary);

                                preference.setOnPreferenceClickListener(preference13 -> {
                                    Permissions.grantRootX(fragment, activity);
                                    return false;
                                });
                            }
                        }
                    }
                    //}

                    preference = prefMng.findPreference(PREF_GRANT_SHIZUKU_PREFERENCES);
                    if (!preferenceAllowed.notAllowedShizuku) {
                        if (preference != null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null)
                                preferenceCategory.removePreference(preference);
                        }
                    } else {
                        if (preference == null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null) {
                                preference = new StartActivityPreference(context);
                                preference.setKey(PREF_GRANT_SHIZUKU_PREFERENCES);
                                preference.setIconSpaceReserved(false);
                                preference.setLayoutResource(R.layout.mp_preference_material_widget);
                                preference.setOrder(-100);
                                preferenceCategory.addPreference(preference);
                            }
                        }
                        if (preference != null) {
                            String _title;
                            if (ShizukuUtils.shizukuAvailable())
                                _title = order + ". " + context.getString(R.string.preferences_grantShizukuPreferences_title);
                            else
                                _title = order + ". " + context.getString(R.string.phone_profiles_pref_shizuku_is_not_running);
                            ++order;
                            Spannable title = new SpannableString(_title);
                            title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                            preference.setTitle(title);
                            Spannable summary;
                            if (ShizukuUtils.shizukuAvailable())
                                summary = new SpannableString(context.getString(R.string.preferences_grantShizukuPreferences_summary));
                            else
                                summary = new SpannableString(context.getString(R.string.profile_preferences_types_shizuku_show_info1));
                            summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                            preference.setSummary(summary);

                            preference.setOnPreferenceClickListener(preference12 -> {
                                Permissions.grantShizukuPermission(fragment, activity);
                                return false;
                            });
                        }
                    }

                    // not some permissions
                    if (profilePermissions.isEmpty()) {
                        preference = prefMng.findPreference(PREF_GRANT_PERMISSIONS);
                        if (preference != null) {
                            PreferenceScreen preferenceCategory = prefMng.findPreference(rootScreen);
                            if (preferenceCategory != null)
                                preferenceCategory.removePreference(preference);
                        }
                    } else {
                        preference = prefMng.findPreference(PREF_GRANT_PERMISSIONS);
                        if (preference == null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null) {
                                if (profile._id > 0)
                                    preference = new StartActivityPreference(context);
                                else
                                    preference = new ExclamationPreference(context);
                                preference.setKey(PREF_GRANT_PERMISSIONS);
                                preference.setIconSpaceReserved(false);
                                //if (profile._id > 0)
                                //    preference.setWidgetLayoutResource(R.layout.preference_widget_start_activity);
                                //else
                                //    preference.setWidgetLayoutResource(R.layout.preference_widget_exclamation_preference);
                                preference.setLayoutResource(R.layout.mp_preference_material_widget);
                                preference.setOrder(-100);
                                preferenceCategory.addPreference(preference);
                            }
                        }
                        if (preference != null) {
                            String _title = order + ". " + context.getString(R.string.preferences_grantPermissions_title);
                            ++order;
                            Spannable title = new SpannableString(_title);
                            title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                            preference.setTitle(title);
                            Spannable summary = new SpannableString(context.getString(R.string.preferences_grantPermissions_summary));
                            summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                            preference.setSummary(summary);

                            if (profile._id > 0) {
                                preference.setOnPreferenceClickListener(preference1 -> {
                                    //Profile mappedProfile = Profile.getMappedProfile(profile, appContext);
                                    Permissions.grantProfilePermissions(activity, profile/*, false, false,*/
                                            /*true, false, 0,*/ /*PPApplication.STARTUP_SOURCE_EDITOR, false, false, true*/);
                                    return false;
                                });
                            }
                        }
                    }

                    // not enabled notification access
                    if (/*(profile._volumeRingerMode == 0) ||*/ canChangeZenMode) {
                        preference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS_ENABLED);
                        if (preference != null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null)
                                preferenceCategory.removePreference(preference);
                        }
                    } else {
                        preference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS_ENABLED);
                        if (preference == null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null) {
                                preference = new StartActivityPreference(context);
                                preference.setKey(PREF_NOTIFICATION_ACCESS_ENABLED);
                                preference.setIconSpaceReserved(false);
                                preference.setLayoutResource(R.layout.mp_preference_material_widget);
                                preference.setOrder(-100);
                                preferenceCategory.addPreference(preference);
                            }
                        }
                        if (preference != null) {
                            String _title = order + ". ";
                            String _summary;
                            //final boolean showDoNotDisturbPermission =
                            //                GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getActivity().getApplicationContext());
                            //if (showDoNotDisturbPermission) {
                            _title = _title + context.getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions);
                            _summary = context.getString(R.string.profile_preferences_red_volumeNotificationsAccessSettings_summary_2);
                            //} else {
                            //    _title = _title + getString(R.string.profile_preferences_volumeNotificationsAccessSettings_title);
                            //    _summary = getString(R.string.profile_preferences_red_volumeNotificationsAccessSettings_summary_notification_access);
                            //}
                            ++order;
                            Spannable title = new SpannableString(_title);
                            title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                            preference.setTitle(title);
                            Spannable summary = new SpannableString(_summary);
                            summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                            preference.setSummary(summary);

                            preference.setOnPreferenceClickListener(preference14 -> {
                                fragment.enableNotificationPolicyAccess(true/*showDoNotDisturbPermission*/);
                                return false;
                            });
                        }
                    }

                    // not enabled accessibility service
                    preference = prefMng.findPreference(PREF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
                    if (accessibilityEnabled == 1) {
                        if (preference != null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null)
                                preferenceCategory.removePreference(preference);
                        }
                    } else {
                        if (preference == null) {
                            PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                            if (preferenceCategory != null) {
                                preference = new StartActivityPreference(context);
                                preference.setKey(PREF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
                                preference.setIconSpaceReserved(false);
                                preference.setLayoutResource(R.layout.mp_preference_material_widget);
                                preference.setOrder(-97);
                                preferenceCategory.addPreference(preference);
                            }
                        }
                        if (preference != null) {
                            int stringRes = R.string.preferences_not_enabled_accessibility_service_title;
                            if (accessibilityEnabled == -2)
                                stringRes = R.string.preferences_not_installed_PPPExtender_title;
                            else if (accessibilityEnabled == -1)
                                stringRes = R.string.preferences_old_version_PPPExtender_title;
                            String _title = order + ". " + context.getString(stringRes);
                            ++order;
                            Spannable title = new SpannableString(_title);
                            title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                            preference.setTitle(title);
                            if ((accessibilityEnabled == -1) || (accessibilityEnabled == -2)) {
                                _title = context.getString(R.string.event_preferences_red_install_PPPExtender);
                                Spannable summary = new SpannableString(_title);
                                summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                                preference.setSummary(summary);

                                preference.setOnPreferenceClickListener(preference15 -> {
                                    ExtenderDialogPreferenceFragment.installPPPExtender(activity, null, false);
                                    return false;
                                });
                            } else {
                                _title = context.getString(R.string.event_preferences_red_enable_PPPExtender);
                                Spannable summary = new SpannableString(_title);
                                summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                                preference.setSummary(summary);

                                preference.setOnPreferenceClickListener(preference16 -> {
                                    ExtenderDialogPreferenceFragment.enableExtender(activity, null);
                                    return false;
                                });
                            }
                        }
                    }

                    // not installed PPPPs
                    if (preferenceAllowed.notAllowedPPPPS) {
                        boolean installedPPPPS = ActivateProfileHelper.isPPPPutSettingsInstalled(context) > 0;
                        preference = prefMng.findPreference(PREF_NOT_INSTALLED_PPPPS);
                        if (installedPPPPS) {
                            if (preference != null) {
                                PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                                if (preferenceCategory != null)
                                    preferenceCategory.removePreference(preference);
                            }
                        } else {
                            if (preference == null) {
                                PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                                if (preferenceCategory != null) {
                                    preference = new StartActivityPreference(context);
                                    preference.setKey(PREF_NOT_INSTALLED_PPPPS);
                                    preference.setIconSpaceReserved(false);
                                    preference.setLayoutResource(R.layout.mp_preference_material_widget);
                                    preference.setOrder(-97);
                                    preferenceCategory.addPreference(preference);
                                }
                            }
                            if (preference != null) {
                                /*int shizukuInstalled = ActivateProfileHelper.isShizukuInstalled(context);
                                Log.e("ProfilePreferenceFragment.SetRedTextToPreferencesAsyncTask", "shizukuInstalled="+shizukuInstalled);
                                if (shizukuInstalled != 0) {
                                    if (!ShizukuUtils.hasShizukuPermission()) {
                                        // Shizuku is installed but not started
                                        String _title;
                                        _title = order + ". " + context.getString(R.string.preferences_grantShizukuPreferences_title);
                                        ++order;
                                        Spannable title = new SpannableString(_title);
                                        title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                                        preference.setTitle(title);
                                        Spannable summary;
                                        summary = new SpannableString(context.getString(R.string.preferences_grantShizukuPreferences_summary));
                                        summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                                        preference.setSummary(summary);

                                        preference.setOnPreferenceClickListener(preference12 -> {
                                            Permissions.grantShizukuPermission(fragment, activity);
                                            return false;
                                        });
                                    }
                                } else {*/
                                    int stringRes = R.string.preferences_not_installed_PPPPutSettings_title;
                                    String _title = order + ". " + context.getString(stringRes);
                                    ++order;
                                    Spannable title = new SpannableString(_title);
                                    title.setSpan(new ForegroundColorSpan(errorColor), 0, title.length(), 0);
                                    preference.setTitle(title);
                                    _title = context.getString(R.string.event_preferences_red_install_PPPExtender);
                                    Spannable summary = new SpannableString(_title);
                                    summary.setSpan(new ForegroundColorSpan(errorColor), 0, summary.length(), 0);
                                    preference.setSummary(summary);

                                    preference.setOnPreferenceClickListener(preference15 -> {
                                        PPPPSDialogPreferenceFragment.installPPPPutSettings(activity, null, false);
                                        return false;
                                    });
                                //}
                            }
                        }
                    }
                } else
                    hidePreferences = true;

                if (hidePreferences) {
                    Preference preference = prefMng.findPreference(PREF_GRANT_PERMISSIONS);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                    preference = prefMng.findPreference(PREF_GRANT_G1_PREFERENCES);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                    preference = prefMng.findPreference(PREF_GRANT_ROOT);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                    preference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS_ENABLED);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                    preference = prefMng.findPreference(PREF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                    preference = prefMng.findPreference(PREF_GRANT_SHIZUKU_PREFERENCES);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = fragment.findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                }

            }
        }

    }

}
