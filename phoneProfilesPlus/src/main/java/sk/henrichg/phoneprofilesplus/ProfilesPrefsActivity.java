package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ProfilesPrefsActivity extends AppCompatActivity {

    long profile_id = 0;
    int newProfileMode = PPApplication.EDIT_MODE_UNDEFINED;
    int predefinedProfileIndex = 0;

    private int resultCode = RESULT_CANCELED;

    boolean showSaveMenu = false;

    private Toolbar toolbar;

    LinearLayout settingsLinearLayout;
    LinearLayout progressLinearLayout;

    private StartPreferencesActivityAsyncTask startPreferencesActivityAsyncTask = null;
    private FinishPreferencesActivityAsyncTask finishPreferencesActivityAsyncTask = null;

    private static final String BUNDLE_NEW_PROFILE_MODE = "newProfileMode";
    private static final String BUNDLE_PREDEFINED_PROFILE_INDEX = "predefinedProfileIndex";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false, false, false, false, true);
        //GlobalGUIRoutines.setLanguage(this);

        //if (Build.VERSION.SDK_INT >= 34)
        //    EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(this.getWindow(), false);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_events_preferences);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        toolbar = findViewById(R.id.activity_preferences_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        settingsLinearLayout = findViewById(R.id.activity_preferences_settings);
        progressLinearLayout = findViewById(R.id.activity_preferences_settings_linla_progress);

        profile_id = getIntent().getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        newProfileMode = getIntent().getIntExtra(PPApplication.EXTRA_NEW_PROFILE_MODE, PPApplication.EDIT_MODE_UNDEFINED);
        predefinedProfileIndex = getIntent().getIntExtra(PPApplication.EXTRA_PREDEFINED_PROFILE_INDEX, 0);

        if (getIntent().getBooleanExtra(DataWrapperStatic.EXTRA_FROM_RED_TEXT_PREFERENCES_NOTIFICATION, false)) {
            // check if profile exists in db
            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
            boolean profileExists = dataWrapper.profileExists(profile_id);
            dataWrapper.invalidateDataWrapper();
            if (!profileExists) {
                PPApplication.showToast(getApplicationContext(),
                        getString(R.string.profile_preferences_profile_not_found),
                        Toast.LENGTH_SHORT);
                PPApplication.blockContactContentObserver = false;
                ContactsContentObserver.enqueueContactsContentObserverWorker();
                super.finish();
                return;
            }
        }

        if (savedInstanceState == null) {
            PPApplication.blockContactContentObserver = true;

            startPreferencesActivityAsyncTask =
                    new StartPreferencesActivityAsyncTask(this, newProfileMode, predefinedProfileIndex);
            startPreferencesActivityAsyncTask.execute();

            //loadPreferences(newProfileMode, predefinedProfileIndex);
            //getSupportFragmentManager()
            //        .beginTransaction()
            //        .replace(R.id.activity_preferences_settings, preferenceFragment)
            //        .commit();
        }
        else {
            profile_id = savedInstanceState.getLong(PPApplication.EXTRA_PROFILE_ID, 0);
            newProfileMode = savedInstanceState.getInt(BUNDLE_NEW_PROFILE_MODE, PPApplication.EDIT_MODE_UNDEFINED);
            predefinedProfileIndex = savedInstanceState.getInt(BUNDLE_PREDEFINED_PROFILE_INDEX, 0);

            showSaveMenu = savedInstanceState.getBoolean(PPApplication.BUNDLE_SHOW_SAVE_MENU, false);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onPause() {
        super.onPause();
        PPApplication.blockContactContentObserver = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        PPApplication.blockContactContentObserver = true;

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        //if (fragments == null)
        //    return;
        for (Fragment fragment : fragments) {
            if (fragment instanceof ContactsMultiSelectDialogPreferenceFragment) {
                ContactsMultiSelectDialogPreferenceFragment dialogFragment =
                        (ContactsMultiSelectDialogPreferenceFragment) fragment;
                dialogFragment.dismiss();
            }
            if (fragment instanceof ContactGroupsMultiSelectDialogPreferenceFragment) {
                ContactGroupsMultiSelectDialogPreferenceFragment dialogFragment =
                        (ContactGroupsMultiSelectDialogPreferenceFragment) fragment;
                dialogFragment.dismiss();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (showSaveMenu) {
            // for shared profile is not needed, for shared profile is used PPApplication.SHARED_PROFILE_PREFS_NAME
            // and this is used in Profile.getSharedProfile()
            //if (profile_id != Profile.SHARED_PROFILE_ID) {
            toolbar.inflateMenu(R.menu.profile_preferences_save);
            //}
        }
        else {
            // no menu for shared profile
            //if (profile_id != Profile.SHARED_PROFILE_ID) {
            //toolbar.inflateMenu(R.menu.profile_preferences);
            toolbar.getMenu().clear();
            //}
        }
        return true;
    }

    private static void onNextLayout(final View view, final Runnable runnable) {
        final ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ViewTreeObserver trueObserver;

                if (observer.isAlive()) {
                    trueObserver = observer;
                } else {
                    trueObserver = view.getViewTreeObserver();
                }

                trueObserver.removeOnGlobalLayoutListener(this);

                runnable.run();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);

        //if (profile_id != Profile.SHARED_PROFILE_ID) {
        // no menu for shared profile

        onNextLayout(toolbar, this::showTargetHelps);

        return ret;
    }

    private void finishActivity() {
        if (showSaveMenu) {
            PPAlertDialog dialog = new PPAlertDialog(
                    getString(R.string.not_saved_changes_alert_title),
                    getString(R.string.not_saved_changes_alert_message),
                    getString(R.string.alert_button_yes),
                    getString(R.string.alert_button_no),
                    null, null,
                    (dialog1, which) -> {
                        finishPreferencesActivityAsyncTask =
                                new FinishPreferencesActivityAsyncTask(this, newProfileMode, predefinedProfileIndex);
                        finishPreferencesActivityAsyncTask.execute();

                        //savePreferences(newProfileMode, predefinedProfileIndex);
                        //resultCode = RESULT_OK;
                        //finish();
                    },
                    (dialog2, which) -> finish(),
                    null,
                    null,
                    null,
                    true, true,
                    false, false,
                    true,
                    false,
                    this
            );

            if (!isFinishing())
                dialog.show();
        }
        else
            finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0)
                finishActivity();
            else
                getSupportFragmentManager().popBackStack();
            return true;
        }
        else
        if (itemId == R.id.profile_preferences_save) {
            finishPreferencesActivityAsyncTask =
                    new FinishPreferencesActivityAsyncTask(this, newProfileMode, predefinedProfileIndex);
            finishPreferencesActivityAsyncTask.execute();

            //savePreferences(newProfileMode, predefinedProfileIndex);
            //resultCode = RESULT_OK;
            //finish();
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_preferences_settings);
        if (fragment != null)
            ((ProfilesPrefsFragment)fragment).doOnActivityResult(requestCode, resultCode, data);
    }

    /** @noinspection deprecation*/
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            finishActivity();
        else
            super.onBackPressed();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putLong(PPApplication.EXTRA_PROFILE_ID, profile_id);
        savedInstanceState.putInt(BUNDLE_NEW_PROFILE_MODE, newProfileMode);
        savedInstanceState.putInt(BUNDLE_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);

        savedInstanceState.putBoolean(PPApplication.BUNDLE_SHOW_SAVE_MENU, showSaveMenu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ((startPreferencesActivityAsyncTask != null) &&
                startPreferencesActivityAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            startPreferencesActivityAsyncTask.cancel(true);
        startPreferencesActivityAsyncTask = null;
        if ((finishPreferencesActivityAsyncTask != null) &&
                finishPreferencesActivityAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            finishPreferencesActivityAsyncTask.cancel(true);
        finishPreferencesActivityAsyncTask = null;
    }

    @Override
    public void finish() {
        PPApplication.blockContactContentObserver = false;
        ContactsContentObserver.enqueueContactsContentObserverWorker();

        // for startActivityForResult
        Intent returnIntent = new Intent();
        returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile_id);
        returnIntent.putExtra(PPApplication.EXTRA_NEW_PROFILE_MODE, newProfileMode);
        returnIntent.putExtra(PPApplication.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
        returnIntent.putExtra(PhoneProfilesPrefsActivity.EXTRA_RESET_EDITOR, PPApplication.grantRootChanged || PPApplication.grantShizukuChanged);
        PPApplication.grantRootChanged = false;
        PPApplication.grantShizukuChanged = false;
        setResult(resultCode,returnIntent);

        super.finish();
    }

    private Profile createProfile(long profile_id, int new_profile_mode, int predefinedProfileIndex, boolean leaveSaveMenu) {
        Profile profile;
        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

        // no change this in shared profile
        if (!leaveSaveMenu)
            showSaveMenu = false;

        if (new_profile_mode == PPApplication.EDIT_MODE_INSERT)
        {
            // create new profile
            if (predefinedProfileIndex == 0) {
                profile = DataWrapperStatic.getNonInitializedProfile(
                        getBaseContext().getString(R.string.profile_name_default),
                        StringConstants.PROFILE_ICON_DEFAULT, 0);
            }
            else {
                profile = dataWrapper.getPredefinedProfile(predefinedProfileIndex-1, false, getBaseContext());
            }
            //profile._showInActivator = true;
            showSaveMenu = true;
        }
        else
        if (new_profile_mode == PPApplication.EDIT_MODE_DUPLICATE)
        {
            // duplicate profile
            Profile origProfile = dataWrapper.getProfileById(profile_id, false, false, false);
            if (origProfile != null) {
                profile = new Profile(
                        origProfile._name + "_d",
                        origProfile._icon,
                        false,
                        origProfile._porder,
                        origProfile._volumeRingerMode,
                        origProfile._volumeRingtone,
                        origProfile._volumeNotification,
                        origProfile._volumeMedia,
                        origProfile._volumeAlarm,
                        origProfile._volumeSystem,
                        origProfile._volumeVoice,
                        origProfile._soundRingtoneChange,
                        origProfile._soundRingtone,
                        origProfile._soundNotificationChange,
                        origProfile._soundNotification,
                        origProfile._soundAlarmChange,
                        origProfile._soundAlarm,
                        origProfile._deviceAirplaneMode,
                        origProfile._deviceWiFi,
                        origProfile._deviceBluetooth,
                        origProfile._deviceScreenTimeout,
                        origProfile._deviceBrightness,
                        origProfile._deviceWallpaperChange,
                        origProfile._deviceWallpaper,
                        origProfile._deviceMobileData,
                        origProfile._deviceMobileDataPrefs,
                        origProfile._deviceGPS,
                        origProfile._deviceRunApplicationChange,
                        origProfile._deviceRunApplicationPackageName,
                        origProfile._deviceAutoSync,
                        origProfile._showInActivator,
                        origProfile._deviceAutoRotate,
                        origProfile._deviceLocationServicePrefs,
                        origProfile._volumeSpeakerPhone,
                        origProfile._deviceNFC,
                        origProfile._duration,
                        origProfile._afterDurationDo,
                        origProfile._volumeZenMode,
                        origProfile._deviceKeyguard,
                        origProfile._vibrationOnTouch,
                        origProfile._deviceWiFiAP,
                        origProfile._devicePowerSaveMode,
                        origProfile._askForDuration,
                        origProfile._deviceNetworkType,
                        origProfile._notificationLed,
                        origProfile._vibrateWhenRinging,
                        origProfile._deviceWallpaperFor,
                        origProfile._hideStatusBarIcon,
                        origProfile._lockDevice,
                        origProfile._deviceConnectToSSID,
                        origProfile._applicationEnableWifiScanning,
                        origProfile._applicationEnableBluetoothScanning,
                        origProfile._durationNotificationSound,
                        origProfile._durationNotificationVibrate,
                        origProfile._deviceWiFiAPPrefs,
                        origProfile._applicationEnableLocationScanning,
                        origProfile._applicationEnableMobileCellScanning,
                        origProfile._applicationEnableOrientationScanning,
                        origProfile._headsUpNotifications,
                        origProfile._deviceForceStopApplicationChange,
                        origProfile._deviceForceStopApplicationPackageName,
                        origProfile._activationByUserCount,
                        origProfile._deviceNetworkTypePrefs,
                        origProfile._deviceCloseAllApplications,
                        origProfile._screenDarkMode,
                        origProfile._dtmfToneWhenDialing,
                        origProfile._soundOnTouch,
                        origProfile._volumeDTMF,
                        origProfile._volumeAccessibility,
                        origProfile._volumeBluetoothSCO,
                        origProfile._afterDurationProfile,
                        origProfile._alwaysOnDisplay,
                        origProfile._screenOnPermanent,
                        origProfile._volumeMuteSound,
                        origProfile._deviceLocationMode,
                        origProfile._applicationEnableNotificationScanning,
                        origProfile._generateNotification,
                        origProfile._cameraFlash,
                        origProfile._deviceNetworkTypeSIM1,
                        origProfile._deviceNetworkTypeSIM2,
                        //origProfile._deviceMobileDataSIM1,
                        //origProfile._deviceMobileDataSIM2,
                        origProfile._deviceDefaultSIMCards,
                        origProfile._deviceOnOffSIM1,
                        origProfile._deviceOnOffSIM2,
                        origProfile._soundRingtoneChangeSIM1,
                        origProfile._soundRingtoneSIM1,
                        origProfile._soundRingtoneChangeSIM2,
                        origProfile._soundRingtoneSIM2,
                        origProfile._soundNotificationChangeSIM1,
                        origProfile._soundNotificationSIM1,
                        origProfile._soundNotificationChangeSIM2,
                        origProfile._soundNotificationSIM2,
                        origProfile._soundSameRingtoneForBothSIMCards,
                        origProfile._deviceLiveWallpaper,
                        origProfile._vibrateNotifications,
                        origProfile._deviceWallpaperFolder,
                        origProfile._applicationDisableGloabalEventsRun,
                        origProfile._deviceVPNSettingsPrefs,
                        origProfile._endOfActivationType,
                        origProfile._endOfActivationTime,
                        origProfile._applicationEnablePeriodicScanning,
                        origProfile._deviceVPN,
                        origProfile._vibrationIntensityRinging,
                        origProfile._vibrationIntensityNotifications,
                        origProfile._vibrationIntensityTouchInteraction,
                        origProfile._volumeMediaChangeDuringPlay,
                        origProfile._applicationWifiScanInterval,
                        origProfile._applicationBluetoothScanInterval,
                        origProfile._applicationBluetoothLEScanDuration,
                        origProfile._applicationLocationScanInterval,
                        origProfile._applicationOrientationScanInterval,
                        origProfile._applicationPeriodicScanInterval,
                        origProfile._sendSMSContacts,
                        origProfile._sendSMSContactGroups,
                        //origProfile._sendSMSContactListType,
                        origProfile._sendSMSSendSMS,
                        origProfile._sendSMSSMSText,
                        origProfile._deviceWallpaperLockScreen,
                        origProfile._clearNotificationEnabled,
                        origProfile._clearNotificationApplications,
                        origProfile._clearNotificationCheckContacts,
                        origProfile._clearNotificationContacts,
                        origProfile._clearNotificationContactGroups,
                        origProfile._clearNotificationCheckText,
                        origProfile._clearNotificationText,
                        origProfile._screenNightLight,
                        origProfile._screenNightLightPrefs
                );
                showSaveMenu = true;
            }
            else
                profile = null;
        }
        else
            profile = dataWrapper.getProfileById(profile_id, false, false, false);

        dataWrapper.invalidateDataWrapper();
        return profile;
    }

    private Profile loadPreferences(int new_profile_mode, int predefinedProfileIndex) {
        Profile profile = createProfile(profile_id, new_profile_mode, predefinedProfileIndex, false);
        if (profile == null)
            profile = createProfile(profile_id, PPApplication.EDIT_MODE_INSERT, predefinedProfileIndex, false);

        if (profile != null)
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            profile.saveProfileToSharedPreferences(preferences);
        }

        return profile;
    }

    Profile getProfileFromPreferences(long profile_id, int new_profile_mode, int predefinedProfileIndex) {
        Profile profile = createProfile(profile_id, new_profile_mode, predefinedProfileIndex, true);

        if (profile != null) {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            // save preferences into profile
            profile._name = preferences.getString(Profile.PREF_PROFILE_NAME, "");
            profile._icon = preferences.getString(Profile.PREF_PROFILE_ICON, "");
            profile._showInActivator = preferences.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, false);

            profile._duration = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DURATION, ""));
            profile._afterDurationDo = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_DO, ""));
            profile._afterDurationProfile = Long.parseLong(preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE, ""));
            profile._askForDuration = preferences.getBoolean(Profile.PREF_PROFILE_ASK_FOR_DURATION, false);
            profile._endOfActivationType = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE, ""));
            profile._endOfActivationTime = preferences.getInt(Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME, 0);
            profile._durationNotificationSound = preferences.getString(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, "");
            profile._durationNotificationVibrate = preferences.getBoolean(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, false);

            profile._hideStatusBarIcon = preferences.getBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false);
            profile._volumeRingerMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, ""));
            profile._volumeZenMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, ""));
            profile._volumeRingtone = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
            profile._volumeNotification = preferences.getString(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, "");
            profile._volumeMedia = preferences.getString(Profile.PREF_PROFILE_VOLUME_MEDIA, "");
            profile._volumeAlarm = preferences.getString(Profile.PREF_PROFILE_VOLUME_ALARM, "");
            profile._volumeSystem = preferences.getString(Profile.PREF_PROFILE_VOLUME_SYSTEM, "");
            profile._volumeVoice = preferences.getString(Profile.PREF_PROFILE_VOLUME_VOICE, "");

            profile._soundRingtoneChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, ""));
            String toneString = preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE, "");
            String[] splits = toneString.split(StringConstants.STR_SPLIT_REGEX);
            //Uri soundUri = Uri.parse(splits[0]);
            /*if (TonesHandler.isPhoneProfilesSilent(soundUri, getApplicationContext()))
                profile._soundRingtone = splits[0]+"|1";
            else*/
                profile._soundRingtone = splits[0];//+"|0";

            profile._soundNotificationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, ""));
            toneString = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");
            splits = toneString.split(StringConstants.STR_SPLIT_REGEX);
            //soundUri = Uri.parse(splits[0]);
            /*if (TonesHandler.isPhoneProfilesSilent(soundUri, getApplicationContext()))
                profile._soundNotification = splits[0]+"|1";
            else*/
                profile._soundNotification = splits[0];//+"|0";

            profile._soundAlarmChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, ""));
            toneString = preferences.getString(Profile.PREF_PROFILE_SOUND_ALARM, "");
            splits = toneString.split(StringConstants.STR_SPLIT_REGEX);
            //soundUri = Uri.parse(splits[0]);
            /*if (TonesHandler.isPhoneProfilesSilent(soundUri, getApplicationContext()))
                profile._soundAlarm = splits[0]+"|1";
            else*/
                profile._soundAlarm = splits[0];//+"|0";

            profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, ""));
            profile._deviceWiFi = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI, ""));
            profile._deviceBluetooth = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, ""));
            profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, ""));
            profile._deviceBrightness = preferences.getString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, "");
            profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, ""));
            if (profile._deviceWallpaperChange != 0) {
                profile._deviceWallpaper = preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER, "");
                profile._deviceWallpaperLockScreen = preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_LOCKSCREEN, "");
                profile._deviceLiveWallpaper = preferences.getString(Profile.PREF_PROFILE_DEVICE_LIVE_WALLPAPER, "");
                profile._deviceWallpaperFor = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR, ""));
                profile._deviceWallpaperFolder = preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER, "");
            } else {
                profile._deviceWallpaper = "-";
                profile._deviceWallpaperLockScreen = "-";
                profile._deviceLiveWallpaper = "";
                profile._deviceWallpaperFor = 0;
                profile._deviceWallpaperFolder = "-";
            }
            profile._deviceMobileData = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, ""));
            profile._deviceMobileDataPrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, ""));
            profile._deviceGPS = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_GPS, ""));
            profile._deviceRunApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, ""));
            if (profile._deviceRunApplicationChange == 1)
                profile._deviceRunApplicationPackageName = preferences.getString(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
            else
                profile._deviceRunApplicationPackageName = "-";
            profile._deviceAutoSync = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOSYNC, ""));
            profile._deviceAutoRotate = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, ""));
            profile._deviceLocationServicePrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, ""));
            profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, ""));
            profile._deviceNFC = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NFC, ""));
            profile._deviceKeyguard = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_KEYGUARD, ""));
            profile._vibrationOnTouch = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, ""));
            profile._deviceWiFiAP = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP, ""));
            profile._devicePowerSaveMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, ""));
            profile._deviceNetworkType = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, ""));
            profile._notificationLed = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_NOTIFICATION_LED, ""));
            profile._vibrateWhenRinging = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, ""));
            profile._vibrateNotifications = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, ""));
            profile._lockDevice = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, ""));
            profile._deviceConnectToSSID = preferences.getString(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, "");
            profile._applicationEnableWifiScanning = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_WIFI_SCANNING, ""));
            profile._applicationEnableBluetoothScanning = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_BLUETOOTH_SCANNING, ""));
            profile._deviceWiFiAPPrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, ""));
            profile._applicationEnableLocationScanning = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_LOCATION_SCANNING, ""));
            profile._applicationEnableMobileCellScanning = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_MOBILE_CELL_SCANNING, ""));
            profile._applicationEnableOrientationScanning = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_ORIENTATION_SCANNING, ""));
            profile._headsUpNotifications = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, ""));
            profile._deviceForceStopApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, ""));
            if (profile._deviceForceStopApplicationChange == 1)
                profile._deviceForceStopApplicationPackageName = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, "-");
            else
                profile._deviceForceStopApplicationPackageName = "-";
            profile._deviceNetworkTypePrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, ""));
            profile._deviceCloseAllApplications = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, ""));
            profile._screenDarkMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_DARK_MODE, ""));
            profile._dtmfToneWhenDialing = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, ""));
            profile._soundOnTouch = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ON_TOUCH, ""));
            profile._volumeDTMF = preferences.getString(Profile.PREF_PROFILE_VOLUME_DTMF, "");
            profile._volumeAccessibility = preferences.getString(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY, "");
            profile._volumeBluetoothSCO = preferences.getString(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO, "");
            profile._alwaysOnDisplay = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, ""));
            profile._screenOnPermanent = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT, ""));
            profile._volumeMuteSound = preferences.getBoolean(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND, false);
            profile._deviceLocationMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE, ""));
            profile._applicationEnableNotificationScanning = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_NOTIFICATION_SCANNING, ""));
            profile._generateNotification = preferences.getString(Profile.PREF_PROFILE_GENERATE_NOTIFICATION, "");
            profile._cameraFlash = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_CAMERA_FLASH, ""));
            profile._deviceNetworkTypeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, ""));
            profile._deviceNetworkTypeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, ""));
            //profile._deviceMobileDataSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, ""));
            //profile._deviceMobileDataSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, ""));
            profile._deviceDefaultSIMCards = preferences.getString(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, "");
            profile._deviceOnOffSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, ""));
            profile._deviceOnOffSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, ""));
            profile._soundRingtoneChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, ""));
            toneString = preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1, "");
            splits = toneString.split(StringConstants.STR_SPLIT_REGEX);
            //Uri soundUri = Uri.parse(splits[0]);
            /*if (TonesHandler.isPhoneProfilesSilent(soundUri, getApplicationContext()))
                profile._soundRingtoneSIM1 = splits[0]+"|1";
            else*/
            profile._soundRingtoneSIM1 = splits[0];//+"|0";
            profile._soundNotificationChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, ""));
            toneString = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1, "");
            splits = toneString.split(StringConstants.STR_SPLIT_REGEX);
            //soundUri = Uri.parse(splits[0]);
            /*if (TonesHandler.isPhoneProfilesSilent(soundUri, getApplicationContext()))
                profile._soundNotificationSIM1 = splits[0]+"|1";
            else*/
            profile._soundNotificationSIM1 = splits[0];//+"|0";
            profile._soundRingtoneChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, ""));
            toneString = preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2, "");
            splits = toneString.split(StringConstants.STR_SPLIT_REGEX);
            //Uri soundUri = Uri.parse(splits[0]);
            /*if (TonesHandler.isPhoneProfilesSilent(soundUri, getApplicationContext()))
                profile._soundRingtoneSIM2 = splits[0]+"|1";
            else*/
            profile._soundRingtoneSIM2 = splits[0];//+"|0";
            profile._soundNotificationChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, ""));
            toneString = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2, "");
            splits = toneString.split(StringConstants.STR_SPLIT_REGEX);
            //soundUri = Uri.parse(splits[0]);
            /*if (TonesHandler.isPhoneProfilesSilent(soundUri, getApplicationContext()))
                profile._soundNotificationSIM2 = splits[0]+"|1";
            else*/
            profile._soundNotificationSIM2 = splits[0];//+"|0";
            profile._soundSameRingtoneForBothSIMCards = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, ""));
            profile._applicationDisableGloabalEventsRun = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN, ""));
            profile._deviceVPNSettingsPrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS, ""));
            profile._applicationEnablePeriodicScanning = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_ENABLE_PERIODIC_SCANNING, ""));
            profile._deviceVPN = preferences.getString(Profile.PREF_PROFILE_DEVICE_VPN, "");
            profile._vibrationIntensityRinging = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING, "");
            profile._vibrationIntensityNotifications = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, "");
            profile._vibrationIntensityTouchInteraction = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, "");
            profile._volumeMediaChangeDuringPlay = preferences.getBoolean(Profile.PREF_PROFILE_VOLUME_MEDIA_CHANGE_DURING_PLAY, false);
            profile._applicationWifiScanInterval = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_WIFI_SCAN_INTERVAL, ""));
            profile._applicationBluetoothScanInterval = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_BLUETOOTH_SCAN_INTERVAL, ""));
            profile._applicationBluetoothLEScanDuration = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_BLUETOOTH_LE_SCAN_DURATION, ""));
            profile._applicationLocationScanInterval = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_LOCATION_UPDATE_INTERVAL, ""));
            profile._applicationOrientationScanInterval = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_ORIENTATION_SCAN_INTERVAL, ""));
            profile._applicationPeriodicScanInterval = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_PERIODIC_SCANNING_SCAN_INTERVAL, ""));
            profile._sendSMSContacts = preferences.getString(Profile.PREF_PROFILE_SEND_SMS_CONTACTS, "");
            profile._sendSMSContactGroups = preferences.getString(Profile.PREF_PROFILE_SEND_SMS_CONTACT_GROUPS, "");
            //profile._phoneCallsContactListType = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_PHONE_CALLS_CONTACT_LIST_TYPE, ""));
            profile._sendSMSSendSMS = preferences.getBoolean(Profile.PREF_PROFILE_SEND_SMS_SEND_SMS, false);
            profile._sendSMSSMSText = preferences.getString(Profile.PREF_PROFILE_SEND_SMS_SMS_TEXT, "");
            profile._clearNotificationEnabled = preferences.getBoolean(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_ENABLED, false);
            profile._clearNotificationApplications = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_APPLICATIONS, "");
            profile._clearNotificationCheckContacts = preferences.getBoolean(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_CONTACTS, false);
            profile._clearNotificationContacts = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACTS, "");
            profile._clearNotificationContactGroups = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CONTACT_GROUPS, "");
            profile._clearNotificationCheckText = preferences.getBoolean(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_CHECK_TEXT, false);
            profile._clearNotificationText = preferences.getString(Profile.PREF_PROFILE_CLEAR_NOTIFICATION_TEXT, "");
            profile._screenNightLight = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT, ""));
            profile._screenNightLightPrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_NIGHT_LIGHT_PREFS, ""));
        }

        return profile;
    }

    private void savePreferences(int new_profile_mode, int predefinedProfileIndex)
    {
        Profile profile = getProfileFromPreferences(profile_id, new_profile_mode, predefinedProfileIndex);
        if (profile != null) {
            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

            long activatedProfileId = dataWrapper.getActivatedProfileId();
            if (activatedProfileId == profile._id) {
                // set alarm for profile duration
                ProfileDurationAlarmBroadcastReceiver.setAlarm(profile, false, false, PPApplication.STARTUP_SOURCE_EDITOR, getApplicationContext());
                //Profile.setActivatedProfileForDuration(getApplicationContext(), profile._id);
            }

            if ((new_profile_mode == PPApplication.EDIT_MODE_INSERT) ||
                    (new_profile_mode == PPApplication.EDIT_MODE_DUPLICATE)) {
                PPApplicationStatic.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_PROFILE_ADDED, null, profile._name, "");

                // add profile into DB
                DatabaseHandler.getInstance(getApplicationContext()).addProfile(profile, false);
                profile_id = profile._id;

            } else if (profile_id > 0) {
                PPApplicationStatic.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_PROFILE_PREFERENCES_CHANGED, null, profile._name, "");

                DatabaseHandler.getInstance(getApplicationContext()).updateProfile(profile);

                // restart Events
                PPApplicationStatic.setBlockProfileEventActions(true);
                if (EventStatic.getGlobalEventsRunning(this)) {
                    if (!DataWrapperStatic.getIsManualProfileActivation(false, getApplicationContext())) {
                        //dataWrapper.restartEvents(false, true, true, true, true);
                        dataWrapper.restartEventsWithRescan(true, false, true, true, true, false);
                    }
                    else {
                        if (activatedProfileId == profile._id) {
                            dataWrapper.activateProfileFromMainThread(profile, false, PPApplication.STARTUP_SOURCE_EDITOR, false, null, true);
                        }
                    }
                }
                else {
                    if (activatedProfileId == profile._id) {
                        dataWrapper.activateProfileFromMainThread(profile, false, PPApplication.STARTUP_SOURCE_EDITOR, false, null, true);
                    }
                }
            }
        }
    }


    private void showTargetHelps() {
        if (!showSaveMenu)
            return;

        if (ApplicationPreferences.prefProfilePrefsActivityStartTargetHelps) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
            editor.putBoolean(PPApplication.PREF_PROFILES_PREFS_ACTIVITY_START_TARGET_HELPS, false);
            editor.apply();
            ApplicationPreferences.prefProfilePrefsActivityStartTargetHelps = false;

            Toolbar toolbar = findViewById(R.id.activity_preferences_toolbar);

            int outerCircleColor = R.color.tabTargetHelpOuterCircleColor;
            int targetCircleColor = R.color.tabTargetHelpTargetCircleColor;
            int titleTextColor = R.color.tabTargetHelpTitleTextColor;
            int descriptionTextColor = R.color.tabTargetHelpDescriptionTextColor;

            final TapTargetSequence sequence = new TapTargetSequence(this);
            List<TapTarget> targets = new ArrayList<>();
            int id = 1;
            try {
                targets.add(
                        TapTarget.forToolbarMenuItem(toolbar, R.id.profile_preferences_save, getString(R.string.profile_preference_activity_targetHelps_save_title), getString(R.string.profile_preference_activity_targetHelps_save_description))
                                .outerCircleColor(outerCircleColor)
                                .targetCircleColor(targetCircleColor)
                                .titleTextColor(titleTextColor)
                                .descriptionTextColor(descriptionTextColor)
                                .descriptionTextAlpha(PPApplication.descriptionTapTargetAlpha)
                                .dimColor(R.color.tabTargetHelpDimColor)
                                .titleTextSize(PPApplication.titleTapTargetSize)
                                .textTypeface(Typeface.DEFAULT_BOLD)
                                .tintTarget(true)
                                .drawShadow(true)
                                .id(id)
                );
                ++id;
            } catch (Exception e) {
                //PPApplicationStatic.recordException(e);
            }

            for (TapTarget target : targets) {
                target.setDrawBehindStatusBar(true);
                target.setDrawBehindNavigationBar(true);
            }

            sequence.targets(targets);

            sequence.listener(new TapTargetSequence.Listener() {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                @Override
                public void onSequenceFinish() {
                }

                @Override
                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                    //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {
                }
            });
            sequence.continueOnCancel(true)
                    .considerOuterCircleCanceled(true);

            sequence.start();
        }
    }

    private static class StartPreferencesActivityAsyncTask extends AsyncTask<Void, Integer, Void> {

        final int new_profile_mode;
        final int predefinedProfileIndex;

        Profile profile;

        //private final WeakReference<ProfilesPrefsActivity> activityWeakReference;
        @SuppressLint("StaticFieldLeak")
        private ProfilesPrefsActivity activity;
        private ProfilesPrefsFragment fragment;

        public StartPreferencesActivityAsyncTask(final ProfilesPrefsActivity activity,
                                                  int new_profile_mode, int predefinedProfileIndex) {
            //this.activityWeakReference = new WeakReference<>(activity);
            this.activity = activity;
            this.new_profile_mode = new_profile_mode;
            this.predefinedProfileIndex = predefinedProfileIndex;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fragment = new ProfilesPrefsRoot();

            //ProfilesPrefsActivity activity = activityWeakReference.get();

            //if (activity != null) {
            //    activity.settingsLinearLayout.setVisibility(View.GONE);
            //    activity.progressLinearLayout.setVisibility(View.VISIBLE);
            //}
        }

        @Override
        protected Void doInBackground(Void... params) {
            //ProfilesPrefsActivity activity = activityWeakReference.get();

            if (activity != null) {
//                Log.e("ProfilesPrefsActivity.StartPreferencesActivityAsyncTask", ".doInBackground");
                profile = activity.loadPreferences(new_profile_mode, predefinedProfileIndex);
                //GlobalUtils.sleep(100);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //ProfilesPrefsActivity activity = activityWeakReference.get();

            if ((activity != null) && (!activity.isFinishing())) {
//                Log.e("ProfilesPrefsActivity.StartPreferencesActivityAsyncTask", ".onPostExecute");

                activity.toolbar.setTitle(activity.getString(R.string.profile_string_0) + StringConstants.STR_COLON_WITH_SPACE + profile._name);

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_preferences_settings, fragment)
                        .commit();

                //activity.progressLinearLayout.setVisibility(View.GONE);
                //activity.settingsLinearLayout.setVisibility(View.VISIBLE);
            }
            activity = null;
        }

    }

    private static class FinishPreferencesActivityAsyncTask extends AsyncTask<Void, Integer, Void> {

        final int new_profile_mode;
        final int predefinedProfileIndex;

        private final WeakReference<ProfilesPrefsActivity> activityWeakReference;

        public FinishPreferencesActivityAsyncTask(final ProfilesPrefsActivity activity,
                                                  int new_profile_mode, int predefinedProfileIndex) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.new_profile_mode = new_profile_mode;
            this.predefinedProfileIndex = predefinedProfileIndex;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ProfilesPrefsActivity activity = activityWeakReference.get();

            if (activity != null) {
//                Log.e("ProfilesPrefsActivity.FinishPreferencesActivityAsyncTask", ".doInBackground");
                activity.savePreferences(new_profile_mode, predefinedProfileIndex);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ProfilesPrefsActivity activity = activityWeakReference.get();

            if (activity != null) {
//                Log.e("ProfilesPrefsActivity.FinishPreferencesActivityAsyncTask", ".onPostExecute");

                activity.resultCode = RESULT_OK;
                activity.finish();
            }
        }

    }

}
