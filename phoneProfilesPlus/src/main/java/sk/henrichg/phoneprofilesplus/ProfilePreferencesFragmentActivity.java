package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.fnp.materialpreferences.PreferenceActivity;

public class ProfilePreferencesFragmentActivity extends PreferenceActivity
{
    private long profile_id = 0;
    int newProfileMode = EditorProfileListFragment.EDIT_MODE_UNDEFINED;

    private int resultCode = RESULT_CANCELED;

    public static boolean showSaveMenu = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // must by called before super.onCreate() for PreferenceActivity
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            GUIData.setTheme(this, false, true);
        else
            GUIData.setTheme(this, false, false);
        GUIData.setLanguage(getBaseContext());

        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_profile_preferences);

        /*
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            if (GlobalData.applicationTheme.equals("material"))
                tintManager.setStatusBarTintColor(Color.parseColor("#ff237e9f"));
            else
                tintManager.setStatusBarTintColor(Color.parseColor("#ff202020"));
        }
        */

        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profile_id = getIntent().getLongExtra(GlobalData.EXTRA_PROFILE_ID, 0);
        newProfileMode = getIntent().getIntExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);

        /*
        if (profile_id == GlobalData.DEFAULT_PROFILE_ID)
            getSupportActionBar().setTitle(R.string.title_activity_default_profile_preferences);
        else
            getSupportActionBar().setTitle(R.string.title_activity_profile_preferences);
        */

        ProfilePreferencesFragment fragment = new ProfilePreferencesFragment();

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putLong(GlobalData.EXTRA_PROFILE_ID, profile_id);
            arguments.putInt(GlobalData.EXTRA_NEW_PROFILE_MODE, newProfileMode);
            if (profile_id == GlobalData.DEFAULT_PROFILE_ID)
                ProfilePreferencesFragment.startupSource = GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE;
            else
                ProfilePreferencesFragment.startupSource = GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY;
            fragment.setArguments(arguments);

            loadPreferences(newProfileMode);

            //getFragmentManager().beginTransaction()
            //        .replace(R.id.activity_profile_preferences_container, fragment, "ProfilePreferencesFragment").commit();
        }

        setPreferenceFragment(fragment);

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void finish() {

        /*
        ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().
                findFragmentByTag(GUIData.MAIN_PREFERENCE_FRAGMENT_TAG);
        if (fragment != null)
            profile_id = fragment.profile_id;
        */

        // for startActivityForResult
        Intent returnIntent = new Intent();
        returnIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile_id);
        returnIntent.putExtra(GlobalData.EXTRA_NEW_PROFILE_MODE, newProfileMode);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile_preferences_action_mode_save:
                savePreferences(newProfileMode);
                resultCode = RESULT_OK;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        GUIData.reloadActivity(this);
    }
    */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        ProfilePreferencesFragment fragment = (ProfilePreferencesFragment)getFragmentManager().
                findFragmentByTag(GUIData.MAIN_PREFERENCE_FRAGMENT_TAG);
        if (fragment != null)
            fragment.doOnActivityResult(requestCode, resultCode, data);
    }

    public static Profile createProfile(Context context, long profile_id, int new_profile_mode, boolean leaveSaveMenu) {
        Profile profile;
        DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);

        if (!leaveSaveMenu)
            showSaveMenu = false;

        if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
        {
            profile = GlobalData.getDefaultProfile(context);
        }
        else
        if (new_profile_mode == EditorProfileListFragment.EDIT_MODE_INSERT)
        {
            // create new profile
            profile = dataWrapper.getNoinitializedProfile(
                    context.getResources().getString(R.string.profile_name_default),
                    GlobalData.PROFILE_ICON_DEFAULT, 0);
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
                    origProfile._devicePowerSaveMode);
            showSaveMenu = true;
        }
        else
            profile = dataWrapper.getProfileById(profile_id, false);

        return profile;
    }

    private void loadPreferences(int new_profile_mode) {

        //Log.e("------------ ProfilePreferencesFragmentActivity", "loadPreferences");

        Profile profile = createProfile(getApplicationContext(), profile_id, new_profile_mode, false);

        if (profile != null)
        {
            String PREFS_NAME;
            if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
                PREFS_NAME = ProfilePreferencesFragment.PREFS_NAME_ACTIVITY;
            else
            if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
                PREFS_NAME = ProfilePreferencesFragment.PREFS_NAME_FRAGMENT;
            else
            if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
                PREFS_NAME = ProfilePreferencesFragment.PREFS_NAME_DEFAULT_PROFILE;
            else
                PREFS_NAME = ProfilePreferencesFragment.PREFS_NAME_FRAGMENT;

            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

            SharedPreferences.Editor editor = preferences.edit();
            if (ProfilePreferencesFragment.startupSource != GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            {
                /*
                editor.remove(GlobalData.PREF_PROFILE_NAME).putString(GlobalData.PREF_PROFILE_NAME, profile._name);
                editor.remove(GlobalData.PREF_PROFILE_ICON).putString(GlobalData.PREF_PROFILE_ICON, profile._icon);
                editor.remove(GlobalData.PREF_PROFILE_SHOW_IN_ACTIVATOR).putBoolean(GlobalData.PREF_PROFILE_SHOW_IN_ACTIVATOR, profile._showInActivator);
                editor.remove(GlobalData.PREF_PROFILE_DURATION).editor.putString(GlobalData.PREF_PROFILE_DURATION, Integer.toString(profile._duration));
                editor.remove(GlobalData.PREF_PROFILE_AFTER_DURATION_DO).editor.putString(GlobalData.PREF_PROFILE_AFTER_DURATION_DO, Integer.toString(profile._afterDurationDo));
                */
                editor.putString(GlobalData.PREF_PROFILE_NAME, profile._name);
                editor.putString(GlobalData.PREF_PROFILE_ICON, profile._icon);
                editor.putBoolean(GlobalData.PREF_PROFILE_SHOW_IN_ACTIVATOR, profile._showInActivator);
                editor.putString(GlobalData.PREF_PROFILE_DURATION, Integer.toString(profile._duration));
                editor.putString(GlobalData.PREF_PROFILE_AFTER_DURATION_DO, Integer.toString(profile._afterDurationDo));
            }
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, Integer.toString(profile._volumeRingerMode));
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, Integer.toString(profile._volumeZenMode));
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_RINGTONE, profile._volumeRingtone);
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION, profile._volumeNotification);
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_MEDIA, profile._volumeMedia);
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_ALARM, profile._volumeAlarm);
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_SYSTEM, profile._volumeSystem);
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_VOICE, profile._volumeVoice);
            editor.putString(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, Integer.toString(profile._soundRingtoneChange));
            editor.putString(GlobalData.PREF_PROFILE_SOUND_RINGTONE, profile._soundRingtone);
            editor.putString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, Integer.toString(profile._soundNotificationChange));
            editor.putString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION, profile._soundNotification);
            editor.putString(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, Integer.toString(profile._soundAlarmChange));
            editor.putString(GlobalData.PREF_PROFILE_SOUND_ALARM, profile._soundAlarm);
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, Integer.toString(profile._deviceAirplaneMode));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_WIFI, Integer.toString(profile._deviceWiFi));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, Integer.toString(profile._deviceBluetooth));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, Integer.toString(profile._deviceScreenTimeout));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS, profile._deviceBrightness);
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, Integer.toString(profile._deviceWallpaperChange));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER, profile._deviceWallpaper);
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, Integer.toString(profile._deviceMobileData));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, Integer.toString(profile._deviceMobileDataPrefs));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_GPS, Integer.toString(profile._deviceGPS));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, Integer.toString(profile._deviceRunApplicationChange));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC, Integer.toString(profile._deviceAutosync));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE, Integer.toString(profile._deviceAutoRotate));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, Integer.toString(profile._deviceLocationServicePrefs));
            editor.putString(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE, Integer.toString(profile._volumeSpeakerPhone));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_NFC, Integer.toString(profile._deviceNFC));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD, Integer.toString(profile._deviceKeyguard));
            editor.putString(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH, Integer.toString(profile._vibrationOnTouch));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP, Integer.toString(profile._deviceWiFiAP));
            editor.putString(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, Integer.toString(profile._devicePowerSaveMode));
            editor.commit();
        }
    }

    private void savePreferences(int new_profile_mode)
    {
        DataWrapper dataWrapper = new DataWrapper(getApplicationContext().getApplicationContext(), false, false, 0);
        Profile profile = createProfile(getApplicationContext(), profile_id, new_profile_mode, true);

        String PREFS_NAME;
        if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
            PREFS_NAME = ProfilePreferencesFragment.PREFS_NAME_ACTIVITY;
        else
        if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
            PREFS_NAME = ProfilePreferencesFragment.PREFS_NAME_FRAGMENT;
        else
        if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            PREFS_NAME = ProfilePreferencesFragment.PREFS_NAME_DEFAULT_PROFILE;
        else
            PREFS_NAME = ProfilePreferencesFragment.PREFS_NAME_FRAGMENT;

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

        // save preferences into profile
        if (ProfilePreferencesFragment.startupSource != GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
        {
            profile._name = preferences.getString(GlobalData.PREF_PROFILE_NAME, "");
            profile._icon = preferences.getString(GlobalData.PREF_PROFILE_ICON, "");
            profile._showInActivator = preferences.getBoolean(GlobalData.PREF_PROFILE_SHOW_IN_ACTIVATOR, true);

            profile._duration = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DURATION, ""));
            profile._afterDurationDo = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_AFTER_DURATION_DO, ""));

            Profile activatedProfile = dataWrapper.getActivatedProfile();
            if ((activatedProfile != null) && (activatedProfile._id == profile._id)) {
                // remove alarm for profile duration
                ProfileDurationAlarmBroadcastReceiver.setAlarm(profile, getApplicationContext());
                GlobalData.setActivatedProfileForDuration(getApplicationContext(), profile._id);
            }
        }
        profile._volumeRingerMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, ""));
        profile._volumeZenMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, ""));
        profile._volumeRingtone = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGTONE, "");
        profile._volumeNotification = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION, "");
        profile._volumeMedia = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_MEDIA, "");
        profile._volumeAlarm = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ALARM, "");
        profile._volumeSystem = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_SYSTEM, "");
        profile._volumeVoice = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_VOICE, "");
        profile._soundRingtoneChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE, ""));
        profile._soundRingtone = preferences.getString(GlobalData.PREF_PROFILE_SOUND_RINGTONE, "");
        profile._soundNotificationChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, ""));
        profile._soundNotification = preferences.getString(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION, "");
        profile._soundAlarmChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE, ""));
        profile._soundAlarm = preferences.getString(GlobalData.PREF_PROFILE_SOUND_ALARM, "");
        profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE, ""));
        profile._deviceWiFi = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WIFI, ""));
        profile._deviceBluetooth = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH, ""));
        profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, ""));
        profile._deviceBrightness = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS, "");
        profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, ""));
        if (profile._deviceWallpaperChange == 1)
            profile._deviceWallpaper = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER, "");
        else
            profile._deviceWallpaper = "-|0";
        profile._deviceMobileData = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA, ""));
        profile._deviceMobileDataPrefs = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, ""));
        profile._deviceGPS = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_GPS, ""));
        profile._deviceRunApplicationChange = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, ""));
        if (profile._deviceRunApplicationChange == 1)
            profile._deviceRunApplicationPackageName = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
        else
            profile._deviceRunApplicationPackageName = "-";
        profile._deviceAutosync = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC, ""));
        profile._deviceAutoRotate = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE, ""));
        profile._deviceLocationServicePrefs = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, ""));
        profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE, ""));
        profile._deviceNFC = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_NFC, ""));
        profile._deviceKeyguard = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD, ""));
        profile._vibrationOnTouch = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH, ""));
        profile._deviceWiFiAP = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP, ""));
        profile._devicePowerSaveMode = Integer.parseInt(preferences.getString(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, ""));

        if (ProfilePreferencesFragment.startupSource != GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
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

}
