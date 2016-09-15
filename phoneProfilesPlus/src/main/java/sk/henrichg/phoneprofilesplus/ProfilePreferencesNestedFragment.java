package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.telephony.TelephonyManager;

import com.fnp.materialpreferences.PreferenceFragment;

public class ProfilePreferencesNestedFragment extends PreferenceFragment
                                        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    protected PreferenceManager prefMng;
    protected SharedPreferences preferences;
    private Context context;

    static final String PREFS_NAME_ACTIVITY = "profile_preferences_activity";
    static final String PREFS_NAME_FRAGMENT = "profile_preferences_fragment";
    static final String PREFS_NAME_DEFAULT_PROFILE = GlobalData.DEFAULT_PROFILE_PREFS_NAME;

    static final String PREF_NOTIFICATION_ACCESS = "prf_pref_volumeNotificationsAccessSettings";
    static final int RESULT_NOTIFICATION_ACCESS_SETTINGS = 1980;
    static final String PREF_UNLINK_VOLUMES_APP_PREFERENCES = "prf_pref_volumeUnlinkVolumesAppSettings";

    @Override
    public int addPreferencesFromResource() {
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // must by false to avoid FC when rotation changes and preference dialogs are shown
        setRetainInstance(false);

        context = getActivity().getApplicationContext();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Log.d("------ ProfilePreferencesFragment.onActivityCreated", "xxxx");

        String PREFS_NAME;
        if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_ACTIVITY)
            PREFS_NAME = PREFS_NAME_ACTIVITY;
        else
        if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_FRAGMENT)
            PREFS_NAME = PREFS_NAME_FRAGMENT;
        else
        if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE)
            PREFS_NAME = PREFS_NAME_DEFAULT_PROFILE;
        else
            PREFS_NAME = PREFS_NAME_FRAGMENT;

        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            ListPreference ringerModePreference = (ListPreference) prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE);

            /*
            if (ringerModePreference.findIndexOfValue("5") < 0) {
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
                setSummary(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, profile._volumeRingerMode);
            }
            */

            /*final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                     (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                    );*/
            final boolean canEnableZenMode = GlobalData.canChangeZenMode(context.getApplicationContext(), true);

            Preference zenModePreference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
            if (zenModePreference != null) {
                String value = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, "");
                zenModePreference.setEnabled((value.equals("5")) && canEnableZenMode);
            }

            Preference notificationAccessPreference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
            if (notificationAccessPreference != null) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                    preferenceCategory.removePreference(notificationAccessPreference);
                } else {
                    //notificationAccessPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    notificationAccessPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                            return false;
                        }
                    });
                }
            }

            Preference volumeUnlinkPreference = prefMng.findPreference(PREF_UNLINK_VOLUMES_APP_PREFERENCES);
            if (volumeUnlinkPreference != null) {
                //volumeUnlinkPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            }

            if (ringerModePreference != null) {
                ringerModePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
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
                                    (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                            );*/
                        final boolean canEnableZenMode = GlobalData.canChangeZenMode(context.getApplicationContext(), true);

                        Preference zenModePreference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);

                        zenModePreference.setEnabled((iNewValue == 5) && canEnableZenMode);
                        GUIData.setPreferenceTitleStyle(zenModePreference, false, false, false);

                        return true;
                    }
                });
            }
        }
        else
        {
            // remove zen mode preferences from preferences screen
            // for Android version < 5.0 this is not supported
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
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
            preference = prefMng.findPreference(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        if (android.os.Build.VERSION.SDK_INT < 23) {
            Preference preference = (Preference) prefMng.findPreference("prf_pref_volumeVibrateWhenRingingRootInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        else {
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null)
            {
                preference.setTitle("(R) "+getString(R.string.profile_preferences_vibrateWhenRinging));
                String value = preferences.getString(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING, "");
                setSummary(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING, value);
            }
        }
        if (android.os.Build.VERSION.SDK_INT < 24) {
            Preference preference = (Preference) prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
            if (preference != null) {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_othersCategory");
                preferenceCategory.removePreference(preference);
            }
        }
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
        {
            ListPreference networkTypePreference = (ListPreference) prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE);
            if (networkTypePreference != null) {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                final int phoneType = telephonyManager.getPhoneType();

                if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                    if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMDPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMDPValues));
                    } else {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMValues));
                    }
                    String value = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE, "");
                    networkTypePreference.setValue(value);
                    setSummary(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE, value);
                }

                if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    if (ProfilePreferencesFragment.startupSource == GlobalData.PREFERENCES_STARTUP_SOURCE_DEFAUT_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMADPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMADPValues));
                    } else {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMAArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMAValues));
                    }
                    String value = preferences.getString(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE, "");
                    networkTypePreference.setValue(value);
                    setSummary(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE, value);
                }
            }
        }
        Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_DURATION);
        if (preference != null)
        {
            preference.setTitle("[M] " + context.getString(R.string.profile_preferences_duration));
            String value = preferences.getString(GlobalData.PREF_PROFILE_DURATION, "");
            setSummary(GlobalData.PREF_PROFILE_DURATION, value);
        }
    }

    @Override
    public void onDestroy()
    {
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public void doOnActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == ImageViewPreference.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null)
        {
            Uri selectedImage = data.getData();
            String picturePath = ImageViewPreference.getPath(context, selectedImage);

            if (ProfilePreferencesFragment.changedImageViewPreference != null)
                // nastavime image identifikatoru na ziskanu cestu ku obrazku
                ProfilePreferencesFragment.changedImageViewPreference.setImageIdentifierAndType(picturePath, false);
        }
        if (requestCode == ProfileIconPreference.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null)
        {
            Uri selectedImage = data.getData();
            String picturePath = ImageViewPreference.getPath(context, selectedImage);

            if (ProfilePreferencesFragment.changedProfileIconPreference != null)
                // nastavime image identifikatoru na ziskanu cestu ku obrazku
                ProfilePreferencesFragment.changedProfileIconPreference.setImageIdentifierAndType(picturePath, false, true);
        }
        if (requestCode == RESULT_NOTIFICATION_ACCESS_SETTINGS) {
            /*final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                            (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                    );*/

            final String sZenModeType = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, "");
            setSummary(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, sZenModeType);
        }
        if (requestCode == ApplicationsDialogPreference.RESULT_APPLICATIONS_EDITOR && resultCode == Activity.RESULT_OK && data != null)
        {
            if (ProfilePreferencesFragment.applicationsDialogPreference != null) {
                ProfilePreferencesFragment.applicationsDialogPreference.updateShortcut(
                        (Intent)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT),
                        data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                        (Bitmap)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON),
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_POSITION, -1));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode, data);
    }

    private String getTitleWhenPreferenceChanged(String key) {
        Preference preference = prefMng.findPreference(key);
        String title = "";
        if ((preference != null) && (preference.isEnabled())) {
            if (key.equals(GlobalData.PREF_PROFILE_ASK_FOR_DURATION)) {
                boolean defaultValue =
                        getResources().getBoolean(
                                GlobalData.getResourceId(preference.getKey(), "bool", context));
                if (preferences.getBoolean(key, defaultValue) != defaultValue)
                    title = preference.getTitle().toString();
            }
            else {
                String defaultValue =
                        getResources().getString(
                                GlobalData.getResourceId(preference.getKey(), "string", context));
                if (preference instanceof VolumeDialogPreference) {
                    if (VolumeDialogPreference.changeEnabled(preferences.getString(preference.getKey(), defaultValue)))
                        title = preference.getTitle().toString();
                } else if (preference instanceof BrightnessDialogPreference) {
                    if (BrightnessDialogPreference.changeEnabled(preferences.getString(preference.getKey(), defaultValue)))
                        title = preference.getTitle().toString();
                } else {
                    if (!preferences.getString(preference.getKey(), defaultValue).equals(defaultValue))
                        title = preference.getTitle().toString();
                }
            }
            return title;
        }
        else
            return title;
    }

    private void setCategorySummary(Preference preference, boolean bold) {
        String key = preference.getKey();
        boolean _bold = bold;
        Preference preferenceScreen = null;
        String summary = "";

        if (key.equals(GlobalData.PREF_PROFILE_DURATION) ||
            key.equals(GlobalData.PREF_PROFILE_AFTER_DURATION_DO) ||
            key.equals(GlobalData.PREF_PROFILE_ASK_FOR_DURATION)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DURATION);
            String afterDurationDoTitle = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_AFTER_DURATION_DO);
            if ((!afterDurationDoTitle.isEmpty()) && (!title.isEmpty())) {
                _bold = true;
                summary = summary + title + " • ";
                summary = summary + afterDurationDoTitle;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_ASK_FOR_DURATION);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_activationDurationCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE) ||
                key.equals(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
                key.equals(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_soundProfileCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGTONE) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_MEDIA) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_ALARM) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_SYSTEM) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_VOICE) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_RINGTONE);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_MEDIA);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_ALARM);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_SYSTEM);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_VOICE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_volumeCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
                //key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE) ||
                key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
                //key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION) ||
                key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE)) {
            //key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_SOUND_RINGTONE);
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION);
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_SOUND_ALARM);
            preferenceScreen = prefMng.findPreference("prf_pref_soundsCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_WIFI) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_GPS) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_NFC) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_WIFI);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_GPS);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_NFC);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_radiosCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS) ||
                key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE) ||
                key.equals(GlobalData.PREF_PROFILE_NOTIFICATION_LED)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_NOTIFICATION_LED);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            preferenceScreen = prefMng.findPreference("prf_pref_screenCategory");
        }

        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
            //key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE)) {
            //key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER)) {
            String title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE);
            if (!title.isEmpty()) {
                _bold = true;
                summary = summary + title;
            }
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            title = getTitleWhenPreferenceChanged(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
            if (!title.isEmpty()) {
                _bold = true;
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + title;
            }
            //_bold = _bold || isBold(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER);
            preferenceScreen = prefMng.findPreference("prf_pref_othersCategory");
        }

        if (preferenceScreen != null) {
            GUIData.setPreferenceTitleStyle(preferenceScreen, _bold, false, false);
            if (_bold)
                preferenceScreen.setSummary(summary);
            else
                preferenceScreen.setSummary("");
        }
    }

    private void setSummary(String key, Object value)
    {
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.menu_settings) + ": " +
                        context.getResources().getString(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes));
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value.toString());
                GUIData.setPreferenceTitleStyle(preference, false, true, false);
                setCategorySummary(preference, false);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GUIData.setPreferenceTitleStyle(listPreference, index > 0, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE))
        {
            if (android.os.Build.VERSION.SDK_INT >= 21)
            {
                /*final boolean canEnableZenMode =
                        (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                         (GlobalData.isRooted(false) && GlobalData.settingsBinaryExists())
                        );*/
                final boolean canEnableZenMode = GlobalData.canChangeZenMode(context.getApplicationContext(), true);

                if (!canEnableZenMode)
                {
                    ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                    if (listPreference != null) {
                        listPreference.setEnabled(false);
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+getResources().getString(R.string.preference_not_allowed_reason_not_supported));
                        GUIData.setPreferenceTitleStyle(listPreference, false, false, false);
                        setCategorySummary(listPreference, false);
                    }
                }
                else
                {
                    String sValue = value.toString();
                    ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                    if (listPreference != null) {
                        int iValue = Integer.parseInt(sValue);
                        int index = listPreference.findIndexOfValue(sValue);
                        CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                        if ((iValue != 0) && (iValue != 99)) {
                            if (!((iValue == 6) && (android.os.Build.VERSION.SDK_INT < 23))) {
                                String[] summaryArray = getResources().getStringArray(R.array.zenModeSummaryArray);
                                summary = summary + " - " + summaryArray[iValue - 1];
                            }
                        }
                        listPreference.setSummary(summary);

                        final String sRingerMode = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, "");
                        int iRingerMode;
                        if (sRingerMode.isEmpty())
                            iRingerMode = 0;
                        else
                            iRingerMode = Integer.parseInt(sRingerMode);

                        if (iRingerMode == 5) {
                            GUIData.setPreferenceTitleStyle(listPreference, index > 0, false, false);
                            setCategorySummary(listPreference, index > 0);
                        }
                        listPreference.setEnabled(iRingerMode == 5);
                    }
                }
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
                key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
                key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GUIData.setPreferenceTitleStyle(listPreference, index > 0, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE) ||
            key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION) ||
            key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM))
        {
            String ringtoneUri = value.toString();

            if (ringtoneUri.isEmpty()) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    preference.setSummary(R.string.preferences_notificationSound_None);
                }
            }
            else
            {
                Uri uri = Uri.parse(ringtoneUri);
                Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                String ringtoneName;
                if (ringtone == null)
                    ringtoneName = "";
                else
                    ringtoneName = ringtone.getTitle(context);
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    preference.setSummary(ringtoneName);
                }
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_AIRPLANE_MODE) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOSYNC) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_WIFI) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_BLUETOOTH) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_GPS) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_NFC) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_NETWORK_TYPE))
        {
            if (key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA)) {
                // set mobile data preference title
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    Preference mobileDataPreference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
                    if (mobileDataPreference != null) {
                        mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData_21);
                    }
                }
                else {
                    Preference mobileDataPreference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA);
                    if (mobileDataPreference != null) {
                        mobileDataPreference.setTitle(R.string.profile_preferences_deviceMobileData);
                    }
                }
            }
            int canChange = GlobalData.isProfilePreferenceAllowed(key, context);
            if (canChange != GlobalData.PREFERENCE_ALLOWED)
            {
                ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                if (listPreference != null) {
                    listPreference.setEnabled(false);
                    if (canChange == GlobalData.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+getResources().getString(GlobalData.getNotAllowedPreferenceReasonString()));
                    GUIData.setPreferenceTitleStyle(listPreference, false, false, false);
                    setCategorySummary(listPreference, false);
                }
            }
            else
            {
                String sValue = value.toString();
                ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    //if (key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA))
                    //    Log.e("ProfilePreferencesFragment", "index="+index);
                    GUIData.setPreferenceTitleStyle(listPreference, index > 0, false, false);
                    setCategorySummary(listPreference, index > 0);
                }
            }

        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_KEYGUARD))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                boolean secureKeyguard;
                KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= 16)
                    secureKeyguard = keyguardManager.isKeyguardSecure();
                else
                    secureKeyguard = keyguardManager.inKeyguardRestrictedInputMode();
                listPreference.setEnabled(!secureKeyguard);
                if (secureKeyguard) {
                    GUIData.setPreferenceTitleStyle(listPreference, false, false, false);
                    setCategorySummary(listPreference, false);
                    listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                            ": "+getResources().getString(R.string.preference_not_allowed_reason_not_supported));
                } else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GUIData.setPreferenceTitleStyle(listPreference, index > 0, false, false);
                    setCategorySummary(listPreference, index > 0);
                }
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GUIData.setPreferenceTitleStyle(listPreference, index > 0, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_AUTOROTATE))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GUIData.setPreferenceTitleStyle(listPreference, index > 0, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_SPEAKER_PHONE) ||
            key.equals(GlobalData.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
            key.equals(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING) ||
            key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_FOR))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GUIData.setPreferenceTitleStyle(listPreference, index > 0, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_NOTIFICATION_LED))
        {
            ListPreference listPreference = (ListPreference) prefMng.findPreference(key);
            if (listPreference != null) {
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    listPreference.setTitle(R.string.profile_preferences_notificationLed_23);
                } else {
                    listPreference.setTitle(R.string.profile_preferences_notificationLed);
                }
                int canChange = GlobalData.isProfilePreferenceAllowed(key, context);
                if (canChange != GlobalData.PREFERENCE_ALLOWED) {
                    listPreference.setEnabled(false);
                    if (canChange == GlobalData.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getResources().getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+getResources().getString(GlobalData.getNotAllowedPreferenceReasonString()));
                    GUIData.setPreferenceTitleStyle(listPreference, false, false, false);
                    setCategorySummary(listPreference, false);
                } else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GUIData.setPreferenceTitleStyle(listPreference, index > 0, false, false);
                    setCategorySummary(listPreference, index > 0);
                }
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_DURATION))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                int iValue = 0;
                if (!sValue.isEmpty())
                    iValue = Integer.valueOf(sValue);
                //preference.setSummary(sValue);
                GUIData.setPreferenceTitleStyle(preference, iValue > 0, false, false);
                setCategorySummary(preference, iValue > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_AFTER_DURATION_DO))
        {
            String sValue = value.toString();
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GUIData.setPreferenceTitleStyle(listPreference, index > 0, false, false);
                setCategorySummary(listPreference, index > 0);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_ASK_FOR_DURATION))
        {
            String sValue = value.toString();
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference)prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                GUIData.setPreferenceTitleStyle(checkBoxPreference, show, false, false);
                setCategorySummary(checkBoxPreference, show);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGTONE) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_NOTIFICATION) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_MEDIA) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_ALARM) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_SYSTEM) ||
            key.equals(GlobalData.PREF_PROFILE_VOLUME_VOICE))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = VolumeDialogPreference.changeEnabled(sValue);
                GUIData.setPreferenceTitleStyle(preference, change, false, false);
                setCategorySummary(preference, change);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_BRIGHTNESS))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = BrightnessDialogPreference.changeEnabled(sValue);
                GUIData.setPreferenceTitleStyle(preference, change, false, false);
                setCategorySummary(preference, change);
            }
        }

    }

    public void setSummary(String key) {
        String value;
        if (key.equals(GlobalData.PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
            key.equals(GlobalData.PREF_PROFILE_ASK_FOR_DURATION)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
        }
        else
            value = preferences.getString(key, "");
        setSummary(key, value);
    }

    private void disableDependedPref(String key, Object value)
    {
        String sValue = value.toString();

        final String NO_CHANGE = "0";
        final String DEFAULT_PROFILE = "99";
        final String ON = "1";

        if (key.equals(GlobalData.PREF_PROFILE_SOUND_RINGTONE_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_SOUND_RINGTONE);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_SOUND_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(GlobalData.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_SOUND_ALARM);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER);
            if (preference != null)
                preference.setEnabled(enabled);
            preference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE))
        {
            boolean enabled = !(sValue.equals(DEFAULT_PROFILE) || sValue.equals(NO_CHANGE));
            Preference preference = prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(GlobalData.PREF_PROFILE_DEVICE_WIFI_AP))
        {
            boolean enabled = !sValue.equals(ON);
            ListPreference preference = (ListPreference) prefMng.findPreference(GlobalData.PREF_PROFILE_DEVICE_WIFI);
            if (preference != null) {
                if (!enabled)
                    preference.setValue(NO_CHANGE);
                preference.setEnabled(enabled);
            }
        }
        if (key.equals(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE)) {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                String ringerMode = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_RINGER_MODE, "0");
                String zenMode = preferences.getString(GlobalData.PREF_PROFILE_VOLUME_ZEN_MODE, "0");
                boolean enabled = false;
                if (ringerMode.equals("5")) {
                    if (zenMode.equals("1") || zenMode.equals("2"))
                        enabled = true;
                }
                ListPreference preference = (ListPreference) prefMng.findPreference(GlobalData.PREF_PROFILE_VIBRATE_WHEN_RINGING);
                if (preference != null) {
                    if (!enabled)
                        preference.setValue(NO_CHANGE);
                    preference.setEnabled(enabled);
                }
            }
        }
    }

    public void disableDependedPref(String key) {
        String value;
        if (key.equals(GlobalData.PREF_PROFILE_SHOW_IN_ACTIVATOR)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
        }
        else
            value = preferences.getString(key, "");
        disableDependedPref(key, value);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {

        String value;
        if (key.equals(GlobalData.PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
            key.equals(GlobalData.PREF_PROFILE_ASK_FOR_DURATION)) {
            boolean bValue = sharedPreferences.getBoolean(key, false);
            value = Boolean.toString(bValue);
        }
        else
            value = sharedPreferences.getString(key, "");
        setSummary(key, value);
        // disable depended preferences
        disableDependedPref(key, value);

        //Activity activity = getActivity();
        //boolean canShow = (EditorProfilesActivity.mTwoPane) && (activity instanceof EditorProfilesActivity);
        //canShow = canShow || ((!EditorProfilesActivity.mTwoPane) && (activity instanceof ProfilePreferencesFragmentActivity));
        //if (canShow)
        //    showActionMode();
        ProfilePreferencesFragmentActivity activity = (ProfilePreferencesFragmentActivity)getActivity();
        ProfilePreferencesFragmentActivity.showSaveMenu = true;
        activity.invalidateOptionsMenu();

    }

}
