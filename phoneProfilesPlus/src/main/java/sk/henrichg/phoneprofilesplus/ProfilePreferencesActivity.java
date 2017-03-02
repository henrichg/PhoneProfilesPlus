package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class ProfilePreferencesActivity extends PreferenceActivity
                        implements PreferenceFragment.OnCreateNestedPreferenceFragment
{
    private long profile_id = 0;
    int newProfileMode = EditorProfileListFragment.EDIT_MODE_UNDEFINED;
    int predefinedProfileIndex = 0;

    private ProfilePreferencesNestedFragment fragment;

    private int resultCode = RESULT_CANCELED;

    public static boolean showSaveMenu = false;

    public boolean targetHelpsSequenceStarted;
    public static final String PREF_START_TARGET_HELPS = "profile_preferences_activity_start_target_helps";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        // must by called before super.onCreate() for PreferenceActivity
        GlobalGUIRoutines.setTheme(this, false, true);
        GlobalGUIRoutines.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        //Log.d("------ ProfilePreferencesActivity.onCreate", "this="+this);

        //setContentView(R.layout.activity_profile_preferences);

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            if (PPApplication.applicationTheme.equals("material"))
                tintManager.setStatusBarTintColor(Color.parseColor("#ff237e9f"));
            else
                tintManager.setStatusBarTintColor(Color.parseColor("#ff202020"));
        }
        else
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (PPApplication.applicationTheme.equals("material"))
                getWindow().setStatusBarColor(Color.parseColor("#1d6681"));
            else
                getWindow().setStatusBarColor(Color.parseColor("#141414"));
        }

        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profile_id = getIntent().getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
        newProfileMode = getIntent().getIntExtra(PPApplication.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
        predefinedProfileIndex = getIntent().getIntExtra(PPApplication.EXTRA_PREDEFINED_PROFILE_INDEX, 0);

        //Log.d("------ ProfilePreferencesActivity.onCreate", "profile_id="+profile_id);

        /*
        if (profile_id == PPApplication.DEFAULT_PROFILE_ID)
            getSupportActionBar().setTitle(R.string.title_activity_default_profile_preferences);
        else
            getSupportActionBar().setTitle(R.string.title_activity_profile_preferences);
        */

        fragment = createFragment(false);

        if (savedInstanceState == null)
            loadPreferences(newProfileMode, predefinedProfileIndex);

        setPreferenceFragment(fragment);
    }

    private ProfilePreferencesNestedFragment createFragment(boolean nested) {
        ProfilePreferencesNestedFragment fragment;
        if (nested)
            fragment = new ProfilePreferencesNestedFragment();
        else
            fragment = new ProfilePreferencesFragment();

        Bundle arguments = new Bundle();
        arguments.putLong(PPApplication.EXTRA_PROFILE_ID, profile_id);
        arguments.putInt(PPApplication.EXTRA_NEW_PROFILE_MODE, newProfileMode);
        if (profile_id == PPApplication.DEFAULT_PROFILE_ID)
            arguments.putInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE);
        else
            arguments.putInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
        arguments.putInt(PPApplication.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void finish() {
        // for startActivityForResult
        Intent returnIntent = new Intent();
        returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile_id);
        returnIntent.putExtra(PPApplication.EXTRA_NEW_PROFILE_MODE, newProfileMode);
        returnIntent.putExtra(PPApplication.EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
        setResult(resultCode,returnIntent);

        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (showSaveMenu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.profile_preferences_action_mode, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showTargetHelps();
            }
        }, 1000);

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile_preferences_action_mode_save:
                savePreferences(newProfileMode, predefinedProfileIndex);
                resultCode = RESULT_OK;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        PreferenceFragment fragment = getFragment();
        if (fragment != null)
            ((ProfilePreferencesNestedFragment)fragment).doOnActivityResult(requestCode, resultCode, data);
    }

    private Profile createProfile(int startupSource, Context context, long profile_id,
                                  int new_profile_mode, int predefinedProfileIndex, boolean leaveSaveMenu) {
        Profile profile;
        DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);

        if (!leaveSaveMenu)
            showSaveMenu = false;

        //Log.d("------ ProfilePreferencesActivity.createProfile", "startupSource="+startupSource);

        if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
        {
            profile = PPApplication.getDefaultProfile(context);
        }
        else
        if (new_profile_mode == EditorProfileListFragment.EDIT_MODE_INSERT)
        {
            // create new profile
            if (predefinedProfileIndex == 0) {
                profile = DataWrapper.getNoinitializedProfile(
                        context.getResources().getString(R.string.profile_name_default),
                        PPApplication.PROFILE_ICON_DEFAULT, 0);
            }
            else {
                profile = dataWrapper.getPredefinedProfile(predefinedProfileIndex-1, false);
            }
            profile._showInActivator = true;
            showSaveMenu = true;
        }
        else
        if (new_profile_mode == EditorProfileListFragment.EDIT_MODE_DUPLICATE)
        {
            // duplicate profile
            Profile origProfile = dataWrapper.getProfileById(profile_id, false);
            profile = new Profile(
                    origProfile._name+"_d",
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
                    origProfile._deviceAutosync,
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
                    origProfile._deviceConnectToSSID);
            showSaveMenu = true;
        }
        else
            profile = dataWrapper.getProfileById(profile_id, false);

        return profile;
    }

    private void loadPreferences(int new_profile_mode, int predefinedProfileIndex) {

        //Log.e("------------ ProfilePreferencesActivity", "loadPreferences");

        int startupSource;
        if (profile_id == PPApplication.DEFAULT_PROFILE_ID)
            startupSource = PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE;
        else
            startupSource = PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY;

        Profile profile = createProfile(startupSource, getApplicationContext(), profile_id, new_profile_mode, predefinedProfileIndex, false);

        if (profile != null)
        {
            String PREFS_NAME = ProfilePreferencesNestedFragment.getPreferenceName(startupSource);

            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

            SharedPreferences.Editor editor = preferences.edit();
            if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            {
                /*
                editor.remove(PPApplication.PREF_PROFILE_NAME).putString(PPApplication.PREF_PROFILE_NAME, profile._name);
                editor.remove(PPApplication.PREF_PROFILE_ICON).putString(PPApplication.PREF_PROFILE_ICON, profile._icon);
                editor.remove(PPApplication.PREF_PROFILE_SHOW_IN_ACTIVATOR).putBoolean(PPApplication.PREF_PROFILE_SHOW_IN_ACTIVATOR, profile._showInActivator);
                editor.remove(PPApplication.PREF_PROFILE_DURATION).editor.putString(PPApplication.PREF_PROFILE_DURATION, Integer.toString(profile._duration));
                editor.remove(PPApplication.PREF_PROFILE_AFTER_DURATION_DO).editor.putString(PPApplication.PREF_PROFILE_AFTER_DURATION_DO, Integer.toString(profile._afterDurationDo));
                */
                editor.putString(PPApplication.PREF_PROFILE_NAME, profile._name);
                editor.putString(PPApplication.PREF_PROFILE_ICON, profile._icon);
                editor.putBoolean(PPApplication.PREF_PROFILE_SHOW_IN_ACTIVATOR, profile._showInActivator);
                editor.putString(PPApplication.PREF_PROFILE_DURATION, Integer.toString(profile._duration));
                editor.putString(PPApplication.PREF_PROFILE_AFTER_DURATION_DO, Integer.toString(profile._afterDurationDo));
                editor.putBoolean(PPApplication.PREF_PROFILE_ASK_FOR_DURATION, profile._askForDuration);
                editor.putBoolean(PPApplication.PREF_PROFILE_HIDE_STATUS_BAR_ICON, profile._hideStatusBarIcon);
            }
            editor.putString(PPApplication.PREF_PROFILE_VOLUME_RINGER_MODE, Integer.toString(profile._volumeRingerMode));
            editor.putString(PPApplication.PREF_PROFILE_VOLUME_ZEN_MODE, Integer.toString(profile._volumeZenMode));
            editor.putString(PPApplication.PREF_PROFILE_VOLUME_RINGTONE, profile._volumeRingtone);
            editor.putString(PPApplication.PREF_PROFILE_VOLUME_NOTIFICATION, profile._volumeNotification);
            editor.putString(PPApplication.PREF_PROFILE_VOLUME_MEDIA, profile._volumeMedia);
            editor.putString(PPApplication.PREF_PROFILE_VOLUME_ALARM, profile._volumeAlarm);
            editor.putString(PPApplication.PREF_PROFILE_VOLUME_SYSTEM, profile._volumeSystem);
            editor.putString(PPApplication.PREF_PROFILE_VOLUME_VOICE, profile._volumeVoice);
            editor.putString(PPApplication.PREF_PROFILE_SOUND_RINGTONE_CHANGE, Integer.toString(profile._soundRingtoneChange));
            editor.putString(PPApplication.PREF_PROFILE_SOUND_RINGTONE, profile._soundRingtone);
            editor.putString(PPApplication.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, Integer.toString(profile._soundNotificationChange));
            editor.putString(PPApplication.PREF_PROFILE_SOUND_NOTIFICATION, profile._soundNotification);
            editor.putString(PPApplication.PREF_PROFILE_SOUND_ALARM_CHANGE, Integer.toString(profile._soundAlarmChange));
            editor.putString(PPApplication.PREF_PROFILE_SOUND_ALARM, profile._soundAlarm);
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_AIRPLANE_MODE, Integer.toString(profile._deviceAirplaneMode));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_WIFI, Integer.toString(profile._deviceWiFi));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_BLUETOOTH, Integer.toString(profile._deviceBluetooth));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, Integer.toString(profile._deviceScreenTimeout));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_BRIGHTNESS, profile._deviceBrightness);
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, Integer.toString(profile._deviceWallpaperChange));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_WALLPAPER, profile._deviceWallpaper);
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_MOBILE_DATA, Integer.toString(profile._deviceMobileData));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, Integer.toString(profile._deviceMobileDataPrefs));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_GPS, Integer.toString(profile._deviceGPS));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, Integer.toString(profile._deviceRunApplicationChange));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_AUTOSYNC, Integer.toString(profile._deviceAutosync));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_AUTOROTATE, Integer.toString(profile._deviceAutoRotate));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, Integer.toString(profile._deviceLocationServicePrefs));
            editor.putString(PPApplication.PREF_PROFILE_VOLUME_SPEAKER_PHONE, Integer.toString(profile._volumeSpeakerPhone));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_NFC, Integer.toString(profile._deviceNFC));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_KEYGUARD, Integer.toString(profile._deviceKeyguard));
            editor.putString(PPApplication.PREF_PROFILE_VIBRATION_ON_TOUCH, Integer.toString(profile._vibrationOnTouch));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_WIFI_AP, Integer.toString(profile._deviceWiFiAP));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, Integer.toString(profile._devicePowerSaveMode));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_NETWORK_TYPE, Integer.toString(profile._deviceNetworkType));
            editor.putString(PPApplication.PREF_PROFILE_NOTIFICATION_LED, Integer.toString(profile._notificationLed));
            editor.putString(PPApplication.PREF_PROFILE_VIBRATE_WHEN_RINGING, Integer.toString(profile._vibrateWhenRinging));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_WALLPAPER_FOR, Integer.toString(profile._deviceWallpaperFor));
            editor.putString(PPApplication.PREF_PROFILE_LOCK_DEVICE, Integer.toString(profile._lockDevice));
            editor.putString(PPApplication.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, profile._deviceConnectToSSID);
            editor.commit();
        }
    }

    private void savePreferences(int new_profile_mode, int predefinedProfileIndex)
    {
        int startupSource;
        if (profile_id == PPApplication.DEFAULT_PROFILE_ID)
            startupSource = PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE;
        else
            startupSource = PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY;

        DataWrapper dataWrapper = new DataWrapper(getApplicationContext().getApplicationContext(), false, false, 0);
        Profile profile = createProfile(startupSource, getApplicationContext(), profile_id, new_profile_mode, predefinedProfileIndex, true);

        //Log.d("------ ProfilePreferencesActivity.savePreferences", "startupSource="+startupSource);

        String PREFS_NAME = ProfilePreferencesNestedFragment.getPreferenceName(startupSource);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

        // save preferences into profile
        if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
        {
            profile._name = preferences.getString(PPApplication.PREF_PROFILE_NAME, "");
            profile._icon = preferences.getString(PPApplication.PREF_PROFILE_ICON, "");
            profile._showInActivator = preferences.getBoolean(PPApplication.PREF_PROFILE_SHOW_IN_ACTIVATOR, true);

            profile._duration = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DURATION, ""));
            profile._afterDurationDo = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_AFTER_DURATION_DO, ""));
            profile._askForDuration = preferences.getBoolean(PPApplication.PREF_PROFILE_ASK_FOR_DURATION, false);

            profile._hideStatusBarIcon = preferences.getBoolean(PPApplication.PREF_PROFILE_HIDE_STATUS_BAR_ICON, false);

            Profile activatedProfile = dataWrapper.getActivatedProfile();
            if ((activatedProfile != null) && (activatedProfile._id == profile._id)) {
                // set alarm for profile duration
                ProfileDurationAlarmBroadcastReceiver.setAlarm(profile, getApplicationContext());
                PPApplication.setActivatedProfileForDuration(getApplicationContext(), profile._id);
            }
        }
        profile._volumeRingerMode = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_VOLUME_RINGER_MODE, ""));
        profile._volumeZenMode = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_VOLUME_ZEN_MODE, ""));
        profile._volumeRingtone = preferences.getString(PPApplication.PREF_PROFILE_VOLUME_RINGTONE, "");
        profile._volumeNotification = preferences.getString(PPApplication.PREF_PROFILE_VOLUME_NOTIFICATION, "");
        profile._volumeMedia = preferences.getString(PPApplication.PREF_PROFILE_VOLUME_MEDIA, "");
        profile._volumeAlarm = preferences.getString(PPApplication.PREF_PROFILE_VOLUME_ALARM, "");
        profile._volumeSystem = preferences.getString(PPApplication.PREF_PROFILE_VOLUME_SYSTEM, "");
        profile._volumeVoice = preferences.getString(PPApplication.PREF_PROFILE_VOLUME_VOICE, "");
        profile._soundRingtoneChange = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_SOUND_RINGTONE_CHANGE, ""));
        profile._soundRingtone = preferences.getString(PPApplication.PREF_PROFILE_SOUND_RINGTONE, "");
        profile._soundNotificationChange = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, ""));
        profile._soundNotification = preferences.getString(PPApplication.PREF_PROFILE_SOUND_NOTIFICATION, "");
        profile._soundAlarmChange = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_SOUND_ALARM_CHANGE, ""));
        profile._soundAlarm = preferences.getString(PPApplication.PREF_PROFILE_SOUND_ALARM, "");
        profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_AIRPLANE_MODE, ""));
        profile._deviceWiFi = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_WIFI, ""));
        profile._deviceBluetooth = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_BLUETOOTH, ""));
        profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, ""));
        profile._deviceBrightness = preferences.getString(PPApplication.PREF_PROFILE_DEVICE_BRIGHTNESS, "");
        profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, ""));
        if (profile._deviceWallpaperChange == 1) {
            profile._deviceWallpaper = preferences.getString(PPApplication.PREF_PROFILE_DEVICE_WALLPAPER, "");
            profile._deviceWallpaperFor = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_WALLPAPER_FOR, ""));
        }
        else {
            profile._deviceWallpaper = "-|0";
            profile._deviceWallpaperFor = 0;
        }
        profile._deviceMobileData = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_MOBILE_DATA, ""));
        profile._deviceMobileDataPrefs = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, ""));
        profile._deviceGPS = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_GPS, ""));
        profile._deviceRunApplicationChange = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, ""));
        if (profile._deviceRunApplicationChange == 1)
            profile._deviceRunApplicationPackageName = preferences.getString(PPApplication.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
        else
            profile._deviceRunApplicationPackageName = "-";
        profile._deviceAutosync = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_AUTOSYNC, ""));
        profile._deviceAutoRotate = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_AUTOROTATE, ""));
        profile._deviceLocationServicePrefs = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, ""));
        profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_VOLUME_SPEAKER_PHONE, ""));
        profile._deviceNFC = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_NFC, ""));
        profile._deviceKeyguard = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_KEYGUARD, ""));
        profile._vibrationOnTouch = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_VIBRATION_ON_TOUCH, ""));
        profile._deviceWiFiAP = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_WIFI_AP, ""));
        profile._devicePowerSaveMode = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, ""));
        profile._deviceNetworkType = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_DEVICE_NETWORK_TYPE, ""));
        profile._notificationLed = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_NOTIFICATION_LED, ""));
        profile._vibrateWhenRinging = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_VIBRATE_WHEN_RINGING, ""));
        profile._lockDevice = Integer.parseInt(preferences.getString(PPApplication.PREF_PROFILE_LOCK_DEVICE, ""));
        profile._deviceConnectToSSID = preferences.getString(PPApplication.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, "");

        if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
        {
            if ((new_profile_mode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                    (new_profile_mode == EditorProfileListFragment.EDIT_MODE_DUPLICATE))
            {
                // add profile into DB
                dataWrapper.getDatabaseHandler().addProfile(profile, false);
                profile_id = profile._id;

            }
            else
            if (profile_id > 0)
            {
                dataWrapper.getDatabaseHandler().updateProfile(profile);
            }
        }
    }

    @Override
    public PreferenceFragment onCreateNestedPreferenceFragment() {
        return createFragment(true);
    }

    private void showTargetHelps() {
        if (!showSaveMenu)
            return;

        final SharedPreferences preferences = getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);

        if (preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {
            //Log.d("ProfilePreferencesActivity.showTargetHelps", "PREF_START_TARGET_HELPS=true");

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREF_START_TARGET_HELPS, false);
            editor.commit();

            Toolbar toolbar = (Toolbar) findViewById(R.id.mp_toolbar);

            TypedValue tv = new TypedValue();
            //getTheme().resolveAttribute(R.attr.colorAccent, tv, true);

            final Display display = getWindowManager().getDefaultDisplay();

            int circleColor = 0xFFFFFF;
            if (PPApplication.applicationTheme.equals("dark"))
                circleColor = 0x7F7F7F;

            final TapTargetSequence sequence = new TapTargetSequence(this);
            sequence.targets(
                    TapTarget.forToolbarMenuItem(toolbar, R.id.profile_preferences_action_mode_save, getString(R.string.profile_preference_activity_targetHelps_save_title), getString(R.string.profile_preference_activity_targetHelps_save_description))
                            .targetCircleColorInt(circleColor)
                            .textColorInt(0xFFFFFF)
                            .drawShadow(true)
                            .id(1)
            );
            sequence.listener(new TapTargetSequence.Listener() {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                @Override
                public void onSequenceFinish() {
                    targetHelpsSequenceStarted = false;
                }

                @Override
                public void onSequenceStep(TapTarget lastTarget) {
                    //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {
                    targetHelpsSequenceStarted = false;
                }
            });
            targetHelpsSequenceStarted = true;
            sequence.start();
        }
    }

}
