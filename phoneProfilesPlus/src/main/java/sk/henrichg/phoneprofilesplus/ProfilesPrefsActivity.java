package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public class ProfilesPrefsActivity extends AppCompatActivity {

    long profile_id = 0;
    int newProfileMode = EditorProfileListFragment.EDIT_MODE_UNDEFINED;
    int predefinedProfileIndex = 0;

    private int resultCode = RESULT_CANCELED;

    boolean showSaveMenu = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // must by called before super.onCreate() for PreferenceActivity
        GlobalGUIRoutines.setTheme(this, false, true, false);
        GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences);

        Toolbar toolbar = findViewById(R.id.activity_preferences_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(GlobalGUIRoutines.dpToPx(1));
        }

        profile_id = getIntent().getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        newProfileMode = getIntent().getIntExtra(EditorProfilesActivity.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
        predefinedProfileIndex = getIntent().getIntExtra(EditorProfilesActivity.EXTRA_PREDEFINED_PROFILE_INDEX, 0);


        ProfilesPrefsFragment preferenceFragment = new ProfilesPrefsActivity.ProfilesPrefsRoot();

        if (savedInstanceState == null) {
            loadPreferences(newProfileMode, predefinedProfileIndex);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_preferences_settings, preferenceFragment)
                    .commit();
        }
        else {
            profile_id = savedInstanceState.getLong("profile_id", 0);
            newProfileMode = savedInstanceState.getInt("newProfileMode", EditorProfileListFragment.EDIT_MODE_UNDEFINED);
            predefinedProfileIndex = savedInstanceState.getInt("predefinedProfileIndex", 0);

            showSaveMenu = savedInstanceState.getBoolean("showSaveMenu", false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
                return true;
            case R.id.profile_preferences_save:
                savePreferences(newProfileMode, predefinedProfileIndex);
                resultCode = RESULT_OK;
                finish();
                return true;
        }
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


    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putLong("profile_id", profile_id);
        savedInstanceState.putInt("newProfileMode", newProfileMode);
        savedInstanceState.putInt("predefinedProfileIndex", predefinedProfileIndex);

        savedInstanceState.putBoolean("showSaveMenu", showSaveMenu);
    }

    private Profile createProfile(long profile_id, int new_profile_mode, int predefinedProfileIndex, boolean leaveSaveMenu) {
        Profile profile;
        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);

        // no change this in shared profile
        if (!leaveSaveMenu)
            showSaveMenu = false;

        if (new_profile_mode == EditorProfileListFragment.EDIT_MODE_INSERT)
        {
            // create new profile
            if (predefinedProfileIndex == 0) {
                profile = DataWrapper.getNonInitializedProfile(
                        getBaseContext().getResources().getString(R.string.profile_name_default),
                        Profile.PROFILE_ICON_DEFAULT, 0);
            }
            else {
                profile = dataWrapper.getPredefinedProfile(predefinedProfileIndex-1, false, getBaseContext());
            }
            profile._showInActivator = true;
            showSaveMenu = true;
        }
        else
        if (new_profile_mode == EditorProfileListFragment.EDIT_MODE_DUPLICATE)
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
                        origProfile._applicationDisableWifiScanning,
                        origProfile._applicationDisableBluetoothScanning,
                        origProfile._durationNotificationSound,
                        origProfile._durationNotificationVibrate,
                        origProfile._deviceWiFiAPPrefs,
                        origProfile._applicationDisableLocationScanning,
                        origProfile._applicationDisableMobileCellScanning,
                        origProfile._applicationDisableOrientationScanning,
                        origProfile._headsUpNotifications,
                        origProfile._deviceForceStopApplicationChange,
                        origProfile._deviceForceStopApplicationPackageName,
                        origProfile._activationByUserCount,
                        origProfile._deviceNetworkTypePrefs,
                        origProfile._deviceCloseAllApplications,
                        origProfile._screenNightMode,
                        origProfile._dtmfToneWhenDialing,
                        origProfile._soundOnTouch);
                showSaveMenu = true;
            }
            else
                profile = null;
        }
        else
            profile = dataWrapper.getProfileById(profile_id, false, false, false);

        return profile;
    }

    private void loadPreferences(int new_profile_mode, int predefinedProfileIndex) {
        Profile profile = createProfile(profile_id, new_profile_mode, predefinedProfileIndex, false);
        if (profile == null)
            profile = createProfile(profile_id, EditorProfileListFragment.EDIT_MODE_INSERT, predefinedProfileIndex, false);

        if (profile != null)
        {
            // must be used handler for rewrite toolbar title/subtitle
            final String profileName = profile._name;
            Handler handler = new Handler(getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toolbar toolbar = findViewById(R.id.activity_preferences_toolbar);
                    toolbar.setSubtitle(getString(R.string.profile_string_0) + ": " + profileName);
                }
            }, 200);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(Profile.PREF_PROFILE_NAME, profile._name);
            editor.putString(Profile.PREF_PROFILE_ICON, profile._icon);
            editor.putBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, profile._showInActivator);
            editor.putString(Profile.PREF_PROFILE_DURATION, Integer.toString(profile._duration));
            editor.putString(Profile.PREF_PROFILE_AFTER_DURATION_DO, Integer.toString(profile._afterDurationDo));
            editor.putBoolean(Profile.PREF_PROFILE_ASK_FOR_DURATION, profile._askForDuration);
            editor.putString(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, profile._durationNotificationSound);
            editor.putBoolean(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, profile._durationNotificationVibrate);
            editor.putBoolean(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON, profile._hideStatusBarIcon);
            editor.putString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, Integer.toString(profile._volumeRingerMode));
            editor.putString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, Integer.toString(profile._volumeZenMode));
            editor.putString(Profile.PREF_PROFILE_VOLUME_RINGTONE, profile._volumeRingtone);
            editor.putString(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, profile._volumeNotification);
            editor.putString(Profile.PREF_PROFILE_VOLUME_MEDIA, profile._volumeMedia);
            editor.putString(Profile.PREF_PROFILE_VOLUME_ALARM, profile._volumeAlarm);
            editor.putString(Profile.PREF_PROFILE_VOLUME_SYSTEM, profile._volumeSystem);
            editor.putString(Profile.PREF_PROFILE_VOLUME_VOICE, profile._volumeVoice);
            editor.putString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, Integer.toString(profile._soundRingtoneChange));
            editor.putString(Profile.PREF_PROFILE_SOUND_RINGTONE, profile._soundRingtone);
            editor.putString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, Integer.toString(profile._soundNotificationChange));
            editor.putString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, profile._soundNotification);
            editor.putString(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, Integer.toString(profile._soundAlarmChange));
            editor.putString(Profile.PREF_PROFILE_SOUND_ALARM, profile._soundAlarm);
            editor.putString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, Integer.toString(profile._deviceAirplaneMode));
            editor.putString(Profile.PREF_PROFILE_DEVICE_WIFI, Integer.toString(profile._deviceWiFi));
            editor.putString(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, Integer.toString(profile._deviceBluetooth));
            editor.putString(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, Integer.toString(profile._deviceScreenTimeout));
            editor.putString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, profile._deviceBrightness);
            editor.putString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, Integer.toString(profile._deviceWallpaperChange));
            editor.putString(Profile.PREF_PROFILE_DEVICE_WALLPAPER, profile._deviceWallpaper);
            editor.putString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, Integer.toString(profile._deviceMobileData));
            editor.putString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, Integer.toString(profile._deviceMobileDataPrefs));
            editor.putString(Profile.PREF_PROFILE_DEVICE_GPS, Integer.toString(profile._deviceGPS));
            editor.putString(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, Integer.toString(profile._deviceRunApplicationChange));
            editor.putString(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
            editor.putString(Profile.PREF_PROFILE_DEVICE_AUTOSYNC, Integer.toString(profile._deviceAutoSync));
            editor.putString(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, Integer.toString(profile._deviceAutoRotate));
            editor.putString(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, Integer.toString(profile._deviceLocationServicePrefs));
            editor.putString(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, Integer.toString(profile._volumeSpeakerPhone));
            editor.putString(Profile.PREF_PROFILE_DEVICE_NFC, Integer.toString(profile._deviceNFC));
            editor.putString(Profile.PREF_PROFILE_DEVICE_KEYGUARD, Integer.toString(profile._deviceKeyguard));
            editor.putString(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, Integer.toString(profile._vibrationOnTouch));
            editor.putString(Profile.PREF_PROFILE_DEVICE_WIFI_AP, Integer.toString(profile._deviceWiFiAP));
            editor.putString(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, Integer.toString(profile._devicePowerSaveMode));
            editor.putString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, Integer.toString(profile._deviceNetworkType));
            editor.putString(Profile.PREF_PROFILE_NOTIFICATION_LED, Integer.toString(profile._notificationLed));
            editor.putString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, Integer.toString(profile._vibrateWhenRinging));
            editor.putString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR, Integer.toString(profile._deviceWallpaperFor));
            editor.putString(Profile.PREF_PROFILE_LOCK_DEVICE, Integer.toString(profile._lockDevice));
            editor.putString(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, profile._deviceConnectToSSID);
            editor.putString(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING, Integer.toString(profile._applicationDisableWifiScanning));
            editor.putString(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING, Integer.toString(profile._applicationDisableBluetoothScanning));
            editor.putString(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, Integer.toString(profile._deviceWiFiAPPrefs));
            editor.putString(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING, Integer.toString(profile._applicationDisableLocationScanning));
            editor.putString(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING, Integer.toString(profile._applicationDisableMobileCellScanning));
            editor.putString(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING, Integer.toString(profile._applicationDisableOrientationScanning));
            editor.putString(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, Integer.toString(profile._headsUpNotifications));
            editor.putString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, Integer.toString(profile._deviceForceStopApplicationChange));
            editor.putString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, profile._deviceForceStopApplicationPackageName);
            editor.putString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, Integer.toString(profile._deviceNetworkTypePrefs));
            editor.putString(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, Integer.toString(profile._deviceCloseAllApplications));
            editor.putString(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE, Integer.toString(profile._screenNightMode));
            editor.putString(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, Integer.toString(profile._dtmfToneWhenDialing));
            editor.putString(Profile.PREF_PROFILE_SOUND_ON_TOUCH, Integer.toString(profile._soundOnTouch));
            editor.apply();
        }
    }

    Profile getProfileFromPreferences(long profile_id, int new_profile_mode, int predefinedProfileIndex) {
        Profile profile = createProfile(profile_id, new_profile_mode, predefinedProfileIndex, true);

        if (profile != null) {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            // save preferences into profile
            profile._name = preferences.getString(Profile.PREF_PROFILE_NAME, "");
            profile._icon = preferences.getString(Profile.PREF_PROFILE_ICON, "");
            profile._showInActivator = preferences.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, true);

            profile._duration = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DURATION, ""));
            profile._afterDurationDo = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_DO, ""));
            profile._askForDuration = preferences.getBoolean(Profile.PREF_PROFILE_ASK_FOR_DURATION, false);
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
            profile._soundRingtone = preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE, "");
            PPApplication.logE("ProfilePreferencesActivity.savePreferences", "profile._soundRingtone=" + profile._soundRingtone);
            profile._soundNotificationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, ""));
            profile._soundNotification = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");
            PPApplication.logE("ProfilePreferencesActivity.savePreferences", "profile._soundNotification=" + profile._soundNotification);
            profile._soundAlarmChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, ""));
            profile._soundAlarm = preferences.getString(Profile.PREF_PROFILE_SOUND_ALARM, "");
            PPApplication.logE("ProfilePreferencesActivity.savePreferences", "profile._soundAlarm=" + profile._soundAlarm);
            profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, ""));
            profile._deviceWiFi = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI, ""));
            profile._deviceBluetooth = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, ""));
            profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, ""));
            profile._deviceBrightness = preferences.getString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, "");
            profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, ""));
            if (profile._deviceWallpaperChange == 1) {
                profile._deviceWallpaper = preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER, "");
                profile._deviceWallpaperFor = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR, ""));
            } else {
                profile._deviceWallpaper = "-|0";
                profile._deviceWallpaperFor = 0;
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
            profile._lockDevice = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, ""));
            profile._deviceConnectToSSID = preferences.getString(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, "");
            profile._applicationDisableWifiScanning = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING, ""));
            profile._applicationDisableBluetoothScanning = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING, ""));
            profile._deviceWiFiAPPrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, ""));
            profile._applicationDisableLocationScanning = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING, ""));
            profile._applicationDisableMobileCellScanning = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING, ""));
            profile._applicationDisableOrientationScanning = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING, ""));
            profile._headsUpNotifications = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, ""));
            profile._deviceForceStopApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, ""));
            if (profile._deviceForceStopApplicationChange == 1)
                profile._deviceForceStopApplicationPackageName = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, "-");
            else
                profile._deviceForceStopApplicationPackageName = "-";
            profile._deviceNetworkTypePrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, ""));
            profile._deviceCloseAllApplications = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, ""));
            profile._screenNightMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_NIGHT_MODE, ""));
            profile._dtmfToneWhenDialing = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, ""));
            profile._soundOnTouch = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ON_TOUCH, ""));
        }

        return profile;
    }

    private void savePreferences(int new_profile_mode, int predefinedProfileIndex)
    {
        Profile profile = getProfileFromPreferences(profile_id, new_profile_mode, predefinedProfileIndex);
        if (profile != null) {
            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);

            Profile activatedProfile = dataWrapper.getActivatedProfile(false, false);
            if ((activatedProfile != null) && (activatedProfile._id == profile._id)) {
                // set alarm for profile duration
                ProfileDurationAlarmBroadcastReceiver.setAlarm(profile, getApplicationContext());
                Profile.setActivatedProfileForDuration(getApplicationContext(), profile._id);
            }

            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_PROFILEPREFERENCESCHANGED, null, profile._name, profile._icon, 0);

            if ((new_profile_mode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                    (new_profile_mode == EditorProfileListFragment.EDIT_MODE_DUPLICATE)) {
                // add profile into DB
                DatabaseHandler.getInstance(getApplicationContext()).addProfile(profile, false);
                profile_id = profile._id;

            } else if (profile_id > 0) {
                DatabaseHandler.getInstance(getApplicationContext()).updateProfile(profile);

                // restart Events
                PPApplication.logE("$$$ restartEvents","from ProfilePreferencesActivity.savePreferences");
                PPApplication.setBlockProfileEventActions(true);
                if (Event.getGlobalEventsRunning(getApplicationContext())) {
                    if (!dataWrapper.getIsManualProfileActivation(false))
                        dataWrapper.restartEvents(false, true, true, true, true);
                    else {
                        if ((activatedProfile != null) && (activatedProfile._id == profile._id)) {
                            dataWrapper.activateProfileFromMainThread(profile, false, PPApplication.STARTUP_SOURCE_EDITOR, false, null);
                        }
                    }
                }
                else {
                    if ((activatedProfile != null) && (activatedProfile._id == profile._id)) {
                        dataWrapper.activateProfileFromMainThread(profile, false, PPApplication.STARTUP_SOURCE_EDITOR, false, null);
                    }
                }
            }
        }
    }

//--------------------------------------------------------------------------------------------------

    static public class ProfilesPrefsRoot extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.profile_prefs_root, rootKey);
        }
    }

    static public class ProfilesPrefsActivationDuration extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.profile_prefs_activation_duration, rootKey);
        }

    }

    static public class ProfilesPrefsSoundProfiles extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.profile_prefs_sound_profile, rootKey);
        }

    }

    static public class ProfilesPrefsVolumes extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.profile_prefs_volumes, rootKey);
        }

    }

    static public class ProfilesPrefsSounds extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.profile_prefs_sounds, rootKey);
        }

    }

    static public class ProfilesPrefsTouchEffects extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.profile_prefs_touch_effects, rootKey);
        }

    }

    static public class ProfilesPrefsRadios extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.profile_prefs_radios, rootKey);
        }

    }

    static public class ProfilesPrefsScreen extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.profile_prefs_screen, rootKey);
        }

    }

    static public class ProfilesPrefsApplication extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.profile_prefs_application, rootKey);
        }

    }

    static public class ProfilesPrefsOthers extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.profile_prefs_others, rootKey);
        }

    }

    static public class ProfilesPrefsForceStopApplications extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.profile_prefs_force_stop, rootKey);
        }

    }

    static public class ProfilesPrefsLockDevice extends ProfilesPrefsFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String rootKey) {
            setPreferencesFromResource(R.xml.profile_prefs_lock_device, rootKey);
        }

    }

}
