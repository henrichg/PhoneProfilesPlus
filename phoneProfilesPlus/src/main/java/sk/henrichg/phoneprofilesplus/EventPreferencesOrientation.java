package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class EventPreferencesOrientation extends EventPreferences {

    String _display;
    String _sides;
    int _distance;
    boolean _checkLight;
    String _lightMin;
    String _lightMax;
    String _ignoredApplications;

    static final String PREF_EVENT_ORIENTATION_ENABLED = "eventOrientationEnabled";
    private static final String PREF_EVENT_ORIENTATION_DISPLAY = "eventOrientationDisplay";
    private static final String PREF_EVENT_ORIENTATION_SIDES = "eventOrientationSides";
    private static final String PREF_EVENT_ORIENTATION_DISTANCE = "eventOrientationDistance";
    private static final String PREF_EVENT_ORIENTATION_CHECK_LIGHT = "eventOrientationCheckLight";
    static final String PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE = "eventOrientationCurrentLightValue";
    private static final String PREF_EVENT_ORIENTATION_LIGHT_MIN = "eventOrientationLightMin";
    private static final String PREF_EVENT_ORIENTATION_LIGHT_MAX = "eventOrientationLightMax";
    static final String PREF_EVENT_ORIENTATION_INSTALL_EXTENDER = "eventOrientationInstallExtender";
    static final String PREF_EVENT_ORIENTATION_ACCESSIBILITY_SETTINGS = "eventOrientationAccessibilitySettings";
    private static final String PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS = "eventOrientationIgnoredApplications";
    private static final String PREF_EVENT_ORIENTATION_APP_SETTINGS = "eventEnableOrientationScanningAppSettings";
    static final String PREF_EVENT_ORIENTATION_LAUNCH_EXTENDER = "eventOrientationLaunchExtender";

    private static final String PREF_EVENT_ORIENTATION_CATEGORY = "eventOrientationCategoryRoot";


    EventPreferencesOrientation(Event event,
                                       boolean enabled,
                                       String display,
                                       String sides,
                                       int distance,
                                       boolean _checkLight,
                                       String lightMin,
                                       String lightMax,
                                       String ignoredApplications)
    {
        super(event, enabled);

        this._display = display;
        this._sides = sides;
        this._distance = distance;
        this._checkLight = _checkLight;
        this._lightMin = lightMin;
        this._lightMax = lightMax;
        this._ignoredApplications = ignoredApplications;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesOrientation._enabled;
        this._display = fromEvent._eventPreferencesOrientation._display;
        this._sides = fromEvent._eventPreferencesOrientation._sides;
        this._distance = fromEvent._eventPreferencesOrientation._distance;
        this._checkLight = fromEvent._eventPreferencesOrientation._checkLight;
        this._lightMin = fromEvent._eventPreferencesOrientation._lightMin;
        this._lightMax = fromEvent._eventPreferencesOrientation._lightMax;
        this._ignoredApplications = fromEvent._eventPreferencesOrientation._ignoredApplications;
        this.setSensorPassed(fromEvent._eventPreferencesOrientation.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_ORIENTATION_ENABLED, _enabled);

        String[] splits = this._display.split("\\|");
        Set<String> set = new HashSet<>(Arrays.asList(splits));
        editor.putStringSet(PREF_EVENT_ORIENTATION_DISPLAY, set);

        splits = this._sides.split("\\|");
        set = new HashSet<>(Arrays.asList(splits));
        editor.putStringSet(PREF_EVENT_ORIENTATION_SIDES, set);

        editor.putString(PREF_EVENT_ORIENTATION_DISTANCE, String.valueOf(this._distance));

        editor.putBoolean(PREF_EVENT_ORIENTATION_CHECK_LIGHT, this._checkLight);
        editor.putString(PREF_EVENT_ORIENTATION_LIGHT_MIN, this._lightMin);
        editor.putString(PREF_EVENT_ORIENTATION_LIGHT_MAX, this._lightMax);

        editor.putString(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS, this._ignoredApplications);

        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_ORIENTATION_ENABLED, false);

        Set<String> set = preferences.getStringSet(PREF_EVENT_ORIENTATION_DISPLAY, null);
        StringBuilder sides = new StringBuilder();
        if (set != null) {
            for (String s : set) {
                if (sides.length() > 0)
                    sides.append("|");
                sides.append(s);
            }
        }
        this._display = sides.toString();

        set = preferences.getStringSet(PREF_EVENT_ORIENTATION_SIDES, null);
        sides = new StringBuilder();
        if (set != null) {
            for (String s : set) {
                if (sides.length() > 0)
                    sides.append("|");
                sides.append(s);
            }
        }
        this._sides = sides.toString();

        this._distance = Integer.parseInt(preferences.getString(PREF_EVENT_ORIENTATION_DISTANCE, "0"));

        this._checkLight = preferences.getBoolean(PREF_EVENT_ORIENTATION_CHECK_LIGHT, false);
        this._lightMin = preferences.getString(PREF_EVENT_ORIENTATION_LIGHT_MIN, "0");
        this._lightMax = preferences.getString(PREF_EVENT_ORIENTATION_LIGHT_MAX, "0");

        this._ignoredApplications = preferences.getString(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS, "");
    }

    @SuppressWarnings("StringConcatenationInLoop")
    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_orientation_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_ORIENTATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_orientation), addPassStatus, DatabaseHandler.ETYPE_ORIENTATION, context);
                    descr = descr + "</b> ";
                }

                if (!ApplicationPreferences.applicationEventOrientationEnableScanning) {
                    if (!ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile)
                        descr = descr + "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *<br>";
                    else
                        descr = descr + context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "<br>";
                }

                String selectedSides = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                if (!this._display.isEmpty() && !this._display.equals("-")) {
                    String[] splits = this._display.split("\\|");
                    String[] sideValues = context.getResources().getStringArray(R.array.eventOrientationDisplayValues);
                    String[] sideNames = context.getResources().getStringArray(R.array.eventOrientationDisplayArray);
                    selectedSides = "";
                    for (String s : splits) {
                        int sideIdx = Arrays.asList(sideValues).indexOf(s);
                        if (sideIdx != -1) {
                            if (!selectedSides.isEmpty())
                                selectedSides = selectedSides + ", ";
                            selectedSides = selectedSides + sideNames[sideIdx];
                        }
                    }
                }
                descr = descr + context.getString(R.string.event_preferences_orientation_display) + ": <b>" + selectedSides + "</b>";

                //SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

                if (PPApplication.magneticFieldSensor != null) {
                    selectedSides = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                    if (!this._sides.isEmpty() && !this._sides.equals("-")) {
                        String[] splits = this._sides.split("\\|");
                        String[] sideValues = context.getResources().getStringArray(R.array.eventOrientationSidesValues);
                        String[] sideNames = context.getResources().getStringArray(R.array.eventOrientationSidesArray);
                        selectedSides = "";
                        for (String s : splits) {
                            if (!selectedSides.isEmpty())
                                selectedSides = selectedSides + ", ";
                            selectedSides = selectedSides + sideNames[Arrays.asList(sideValues).indexOf(s)];
                        }
                    }
                    descr = descr + " • " + context.getString(R.string.event_preferences_orientation_sides) + ": <b>" + selectedSides + "</b>";
                }

                String[] distanceValues = context.getResources().getStringArray(R.array.eventOrientationDistanceTypeValues);
                String[] distanceNames = context.getResources().getStringArray(R.array.eventOrientationDistanceTypeArray);
                int i = Arrays.asList(distanceValues).indexOf(String.valueOf(this._distance));
                if (i != -1)
                    descr = descr + " • " + context.getString(R.string.event_preferences_orientation_distance) + ": <b>" + distanceNames[i] + "</b>";

                if (this._checkLight) {
                    descr = descr + " • " + context.getString(R.string.event_preferences_orientation_light) + ": <b>" +
                                    this._lightMin  + "-" + this._lightMax + "</b>";
                }
                else {
                    descr = descr + " • " + context.getString(R.string.event_preferences_orientation_light) + ": <b>" +
                            context.getString(R.string.event_preferences_orientation_light_not_enabled) + "</b>";
                }

                String selectedApplications = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext());
                if (extenderVersion == 0) {
                    selectedApplications = context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_not_extender_installed);
                } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0) {
                    selectedApplications = context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context.getApplicationContext())) {
                    selectedApplications = context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                } else if (!this._ignoredApplications.isEmpty() && !this._ignoredApplications.equals("-")) {
                    String[] splits = this._ignoredApplications.split("\\|");
                    if (splits.length == 1) {
                        String packageName = Application.getPackageName(splits[0]);
                        String activityName = Application.getActivityName(splits[0]);
                        PackageManager packageManager = context.getPackageManager();
                        if (activityName.isEmpty()) {
                            ApplicationInfo app;
                            try {
                                app = packageManager.getApplicationInfo(packageName, 0);
                                if (app != null)
                                    selectedApplications = packageManager.getApplicationLabel(app).toString();
                            } catch (Exception e) {
                                selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.setClassName(packageName, activityName);
                            ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                            if (info != null)
                                selectedApplications = info.loadLabel(packageManager).toString();
                        }
                    } else
                        selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
                }
                descr = descr + " • " + context.getString(R.string.event_preferences_orientation_ignoreForApplications) + ": <b>" + selectedApplications + "</b>";
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_ORIENTATION_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_ORIENTATION_ENABLED) ||
            key.equals(PREF_EVENT_ORIENTATION_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_APP_SETTINGS);
            String summary;
            int titleColor;
            if (preference != null) {
                if (!ApplicationPreferences.applicationEventOrientationEnableScanning) {
                    if (!ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile) {
                        summary = "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *\n\n" +
                                context.getResources().getString(R.string.phone_profiles_pref_eventOrientationAppSettings_summary);
                        titleColor = Color.RED; //0xFFffb000;
                    }
                    else {
                        summary = context.getResources().getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "\n\n" +
                                context.getResources().getString(R.string.phone_profiles_pref_eventOrientationAppSettings_summary);
                        titleColor = 0;
                    }
                }
                else {
                    summary = context.getResources().getString(R.string.array_pref_applicationDisableScanning_enabled) + ".\n\n" +
                            context.getResources().getString(R.string.phone_profiles_pref_eventOrientationAppSettings_summary);
                    titleColor = 0;
                }
                CharSequence sTitle = preference.getTitle();
                Spannable sbt = new SpannableString(sTitle);
                Object[] spansToRemove = sbt.getSpans(0, sTitle.length(), Object.class);
                for(Object span: spansToRemove){
                    if(span instanceof CharacterStyle)
                        sbt.removeSpan(span);
                }
                if (preferences.getBoolean(PREF_EVENT_ORIENTATION_ENABLED, false)) {
                    if (titleColor != 0)
                        sbt.setSpan(new ForegroundColorSpan(titleColor), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                preference.setTitle(sbt);
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_ORIENTATION_DISPLAY)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);
            }
        }
        if (key.equals(PREF_EVENT_ORIENTATION_SIDES)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);
            }
        }
        if (key.equals(PREF_EVENT_ORIENTATION_DISTANCE))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }

        if (key.equals(PREF_EVENT_ORIENTATION_ENABLED) || // added due to call setEnabled also when this preference is changed
                key.equals(PREF_EVENT_ORIENTATION_CHECK_LIGHT) ||
                key.equals(PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE) ||
                key.equals(PREF_EVENT_ORIENTATION_LIGHT_MIN) ||
                key.equals(PREF_EVENT_ORIENTATION_LIGHT_MAX))
        {
//            PPApplication.logE("[LOCAL_BROADCAST_CALL] EventPreferencesOrientation.setSummary", "xxx");
            Log.e("EventPreferencesOrientation.setSummary", "XXX");
            Intent intent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshEventsPrefsGUIBroadcastReceiver");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            boolean hasLight = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null);
            if (preferences.getBoolean(PREF_EVENT_ORIENTATION_CHECK_LIGHT, false)) {
                Log.e("EventPreferencesOrientation.setSummary", "checkLight = true");
                Preference currentLightValuePreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE);
                if (currentLightValuePreference != null) {
                    currentLightValuePreference.setEnabled(hasLight);
                    //GlobalGUIRoutines.setPreferenceTitleStyleX(currentLightValuePreference, true, false, false, false, false);
                }
                BetterNumberPickerPreferenceX minMaxPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_MIN);
                if (minMaxPreference != null) {
                    minMaxPreference.setEnabled(hasLight);
                    minMaxPreference.setSummary(minMaxPreference.value);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(minMaxPreference, true, true, false, false, false);
                }
                minMaxPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_MAX);
                if (minMaxPreference != null) {
                    minMaxPreference.setEnabled(hasLight);
                    minMaxPreference.setSummary(minMaxPreference.value);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(minMaxPreference, true, true, false, false, false);
                }
            }
            else {
                Log.e("EventPreferencesOrientation.setSummary", "checkLight = false");
                Preference currentLightValuePreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE);
                if (currentLightValuePreference != null) {
                    currentLightValuePreference.setEnabled(false);
                    //GlobalGUIRoutines.setPreferenceTitleStyleX(currentLightValuePreference, true, false, false, false, false);
                }
                BetterNumberPickerPreferenceX minMaxPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_MIN);
                if (minMaxPreference != null) {
                    minMaxPreference.setEnabled(false);
                    minMaxPreference.setSummary(minMaxPreference.value);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(minMaxPreference, true, false, false, false, false);
                }
                minMaxPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_MAX);
                if (minMaxPreference != null) {
                    minMaxPreference.setEnabled(false);
                    minMaxPreference.setSummary(minMaxPreference.value);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(minMaxPreference, true, false, false, false, false);
                }
            }
        }

        if (key.equals(PREF_EVENT_ORIENTATION_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext());
                if (extenderVersion == 0) {
                    String summary = context.getString(R.string.profile_preferences_PPPExtender_not_installed_summary) +
                            "\n\n" + context.getString(R.string.event_preferences_orientation_PPPExtender_install_summary);
                    preference.setSummary(summary);
                }
                else {
                    String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(context);
                    String summary = context.getString(R.string.profile_preferences_PPPExtender_installed_summary) +
                            " " + extenderVersionName + "\n\n";
                    if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0)
                        summary = summary + context.getString(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                    else
                        summary = summary + context.getString(R.string.event_preferences_applications_PPPExtender_upgrade_summary);
                    preference.setSummary(summary);
                }
            }
        }
        if (key.equals(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS)) {
            Preference preference = prefMng.findPreference(key);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !value.isEmpty(), false, false, false);
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesOrientation.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesOrientation.isRunnable(context.getApplicationContext());
        boolean enabled = preferences.getBoolean(PREF_EVENT_ORIENTATION_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_DISPLAY);
        if (preference != null) {
            Set<String> set = prefMng.getSharedPreferences().getStringSet(PREF_EVENT_ORIENTATION_DISPLAY, null);
            StringBuilder sides = new StringBuilder();
            if (set != null) {
                for (String s : set) {
                    if (sides.length() > 0)
                        sides.append("|");
                    sides.append(s);
                }
            }
            boolean bold = sides.length() > 0;
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_SIDES);
        if (preference != null) {
            Set<String> set = prefMng.getSharedPreferences().getStringSet(PREF_EVENT_ORIENTATION_SIDES, null);
            StringBuilder sides = new StringBuilder();
            if (set != null) {
                for (String s : set) {
                    if (sides.length() > 0)
                        sides.append("|");
                    sides.append(s);
                }
            }
            boolean bold = sides.length() > 0;
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, !isRunnable, false);
        }
        ListPreference distancePreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_DISTANCE);
        if (distancePreference != null) {
            int index = distancePreference.findIndexOfValue(distancePreference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(distancePreference, enabled, index > 0, true, !isRunnable, false);
        }

        SwitchPreferenceCompat checkLightPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_CHECK_LIGHT);
        if (checkLightPreference != null) {
            boolean bold = checkLightPreference.isChecked();
            GlobalGUIRoutines.setPreferenceTitleStyleX(checkLightPreference, enabled, bold, true, !isRunnable, false);
        }

        boolean isAccessibilityEnabled = event._eventPreferencesOrientation.isAccessibilityServiceEnabled(context) == 1;
        preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_ACCESSIBILITY_SETTINGS);
        if (preference != null) {

            String summary;
            if (isAccessibilityEnabled)
                summary = context.getString(R.string.accessibility_service_enabled);
            else {
                summary = context.getString(R.string.accessibility_service_disabled);
                summary = summary + "\n\n" + context.getString(R.string.event_preferences_orientation_AccessibilitySettingsForExtender_summary);
            }
            preference.setSummary(summary);

            //GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, true, !isAccessibilityEnabled, false);
        }

    }

    @SuppressWarnings("StringConcatenationInLoop")
    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_ORIENTATION_ENABLED) ||
            key.equals(PREF_EVENT_ORIENTATION_CHECK_LIGHT)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }

        if (key.equals(PREF_EVENT_ORIENTATION_DISPLAY)) {
            Set<String> set = preferences.getStringSet(key, null);
            String sides = "";
            if (set != null) {
                String[] sideValues = context.getResources().getStringArray(R.array.eventOrientationDisplayValues);
                String[] sideNames = context.getResources().getStringArray(R.array.eventOrientationDisplayArray);
                for (String s : set) {
                    if (!s.isEmpty()) {
                        if (!sides.isEmpty())
                            sides = sides + ", ";
                        sides = sides + sideNames[Arrays.asList(sideValues).indexOf(s)];
                    }
                }
                if (sides.isEmpty())
                    sides = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            }
            else
                sides = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            setSummary(prefMng, key, sides, context);
        }

        if (key.equals(PREF_EVENT_ORIENTATION_SIDES)) {
            Set<String> set = preferences.getStringSet(key, null);
            String sides = "";
            if (set != null) {
                String[] sideValues = context.getResources().getStringArray(R.array.eventOrientationSidesValues);
                String[] sideNames = context.getResources().getStringArray(R.array.eventOrientationSidesArray);
                for (String s : set) {
                    if (!s.isEmpty()) {
                        if (!sides.isEmpty())
                            sides = sides + ", ";
                        sides = sides + sideNames[Arrays.asList(sideValues).indexOf(s)];
                    }
                }
                if (sides.isEmpty())
                    sides = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            }
            else
                sides = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            setSummary(prefMng, key, sides, context);
        }

        if (key.equals(PREF_EVENT_ORIENTATION_DISTANCE))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
        if (key.equals(PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE) ||
                key.equals(PREF_EVENT_ORIENTATION_LIGHT_MIN) ||
                key.equals(PREF_EVENT_ORIENTATION_LIGHT_MAX) )
        {
            //int value = preferences.getInt(key, 0);
            //setSummary(prefMng, key, String.valueOf(value), context);
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
        if (key.equals(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS) ||
            key.equals(PREF_EVENT_ORIENTATION_INSTALL_EXTENDER) ||
            key.equals(PREF_EVENT_ORIENTATION_APP_SETTINGS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_ORIENTATION_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_DISPLAY, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_SIDES, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_DISTANCE, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_CHECK_LIGHT, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_LIGHT_MIN, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_LIGHT_MAX, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_INSTALL_EXTENDER, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_APP_SETTINGS, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_ORIENTATION_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesOrientation tmp = new EventPreferencesOrientation(this._event, this._enabled, this._display, this._sides, this._distance, this._checkLight, this._lightMin, this._lightMax, this._ignoredApplications);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_ORIENTATION_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_DEVICE_ORIENTATION).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !(tmp.isRunnable(context) && permissionGranted), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

        boolean magneticSensor = false;
        boolean lightSensor = false;
        if (PPApplication.magneticFieldSensor != null)
            magneticSensor = true;
        if (PPApplication.lightSensor != null)
            lightSensor = true;
        boolean lightEnabled = _checkLight && lightSensor;

        if (magneticSensor)
            runnable = runnable && (!_display.isEmpty() || !_sides.isEmpty() || (_distance != 0) || lightEnabled);
        else
            runnable = runnable && (!_display.isEmpty() || (_distance != 0) || lightEnabled);
        /*
        if (_checkLight) {
            if ((sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) == null))
                runnable = false;
        }
       */

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        boolean hasAccelerometer = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null);
        boolean hasMagneticField = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null);
        boolean hasProximity = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null);
        boolean hasLight = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null);
        boolean enabledAll = (hasAccelerometer) && (hasMagneticField);
        Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_DISPLAY);

        if (preference != null) {
            if (!hasAccelerometer)
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getString(R.string.preference_not_allowed_reason_no_hardware));
            preference.setEnabled(hasAccelerometer);
        }
        preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_SIDES);
        if (preference != null) {
            if (!enabledAll)
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getString(R.string.preference_not_allowed_reason_no_hardware));
            preference.setEnabled(enabledAll);
        }
        boolean enabled = hasProximity;
        preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_DISTANCE);
        if (preference != null) {
            if (!enabled)
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getString(R.string.preference_not_allowed_reason_no_hardware));
            preference.setEnabled(enabled);
        }

        enabled = hasLight;
        Preference currentValuePreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_CURRENT_VALUE);
        if (currentValuePreference != null) {
            if (!enabled)
                currentValuePreference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + context.getString(R.string.preference_not_allowed_reason_no_hardware));
            currentValuePreference.setEnabled(enabled);
        }
        Preference minLightPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_MIN);
        if (minLightPreference != null) {
            if (!enabled)
                minLightPreference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + context.getString(R.string.preference_not_allowed_reason_no_hardware));
            minLightPreference.setEnabled(enabled);
        }
        Preference maxLightPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_LIGHT_MAX);
        if (maxLightPreference != null) {
            if (!enabled)
                maxLightPreference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + context.getString(R.string.preference_not_allowed_reason_no_hardware));
            maxLightPreference.setEnabled(enabled);
        }
        if (enabled) {
            final PreferenceManager _prefMng = prefMng;
            final Context _context = context.getApplicationContext();

            if (minLightPreference != null) {
                minLightPreference.setOnPreferenceChangeListener((preference1, newValue) -> {
                    String sNewValue = (String) newValue;
                    int iNewValue;
                    if (sNewValue.isEmpty())
                        iNewValue = 0;
                    else
                        iNewValue = Integer.parseInt(sNewValue);

                    String sHightLevelValue = _prefMng.getSharedPreferences().getString(PREF_EVENT_ORIENTATION_LIGHT_MAX, "2147483647");
                    int iHightLevelValue;
                    if (sHightLevelValue.isEmpty())
                        iHightLevelValue = 2147483647;
                    else
                        iHightLevelValue = Integer.parseInt(sHightLevelValue);

                    boolean OK = ((iNewValue >= 0) && (iNewValue <= iHightLevelValue));

                    if (!OK) {
                        PPApplication.showToast(_context.getApplicationContext(),
                                _context.getResources().getString(R.string.event_preferences_orientation_light_level_min) + ": " +
                                        _context.getResources().getString(R.string.event_preferences_orientation_light_level_bad_value),
                                Toast.LENGTH_SHORT);
                    }

                    return OK;
                });
            }
            if (maxLightPreference != null) {
                maxLightPreference.setOnPreferenceChangeListener((preference12, newValue) -> {
                    String sNewValue = (String) newValue;
                    int iNewValue;
                    if (sNewValue.isEmpty())
                        iNewValue = 2147483647;
                    else
                        iNewValue = Integer.parseInt(sNewValue);

                    String sLowLevelValue = _prefMng.getSharedPreferences().getString(PREF_EVENT_ORIENTATION_LIGHT_MIN, "0");
                    int iLowLevelValue;
                    if (sLowLevelValue.isEmpty())
                        iLowLevelValue = 0;
                    else
                        iLowLevelValue = Integer.parseInt(sLowLevelValue);

                    //noinspection ConstantConditions
                    boolean OK = ((iNewValue >= iLowLevelValue) && (iNewValue <= 2147483647));

                    if (!OK) {
                        PPApplication.showToast(_context.getApplicationContext(),
                                _context.getResources().getString(R.string.event_preferences_orientation_light_level_max) + ": " +
                                        _context.getResources().getString(R.string.event_preferences_orientation_light_level_bad_value),
                                Toast.LENGTH_SHORT);
                    }

                    return OK;
                });
            }
        }

        enabled = PPPExtenderBroadcastReceiver.isEnabled(context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_3_0);
        ApplicationsMultiSelectDialogPreferenceX applicationsPreference = prefMng.findPreference(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS);
        if (applicationsPreference != null) {
            applicationsPreference.setEnabled(enabled);
            applicationsPreference.setSummaryAMSDP();
        }
        SharedPreferences preferences = prefMng.getSharedPreferences();
        setSummary(prefMng, PREF_EVENT_ORIENTATION_APP_SETTINGS, preferences, context);
        setCategorySummary(prefMng, preferences, context);
    }

    /*long computeAlarm(Context context)
    {
        //PPApplication.logE("EventPreferencesSMS.computeAlarm","xxx");

        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        String applicationEventOrientationScanInPowerSaveMode = ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode;

        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
        if (isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("2"))
            // start scanning in power save mode is not allowed
            return 0;

        int interval = ApplicationPreferences.applicationEventOrientationScanInterval;
        if (isPowerSaveMode && applicationEventOrientationScanInPowerSaveMode.equals("1"))
            interval *= 2;

        calEndTime.setTimeInMillis((calEndTime.getTimeInMillis() - gmtOffset) + (interval * 1000));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = calEndTime.getTimeInMillis();

        return alarmTime;
    }

    @Override
    void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        //PPApplication.logE("EventPreferencesOrientation.setSystemRunningEvent","xxx");

        removeAlarm(context);
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        PPApplication.logE("EventPreferencesOrientation.setSystemEventForPause","_event._name="+_event._name);

        removeAlarm(context);

        PPApplication.logE("EventPreferencesOrientation.setSystemEventForPause","isRunnable()="+isRunnable(context));
        PPApplication.logE("EventPreferencesOrientation.setSystemEventForPause","_enabled="+_enabled);

        if (!(isRunnable(context) && _enabled))
            return;

        PPApplication.logE("EventPreferencesOrientation.setSystemEventForPause","runnable and enabled");

        long alarmTime = computeAlarm(context);
        PPApplication.logE("EventPreferencesOrientation.setSystemEventForPause","alarmTime="+alarmTime);

        if (alarmTime > 0)
            setAlarm(alarmTime, context);
    }

    @Override
    void removeSystemEvent(Context context)
    {
        removeAlarm(context);

        //PPApplication.logE("EventPreferencesOrientation.removeSystemEvent", "xxx");
    }
    */

    /*static int convertLightToSensor(float light, float maxLight) {
        return (int)Math.round(light / maxLight * 10000.0);
    }

    static float convertPercentsToLight(long percentage, float maxLight) {
        return Math.round(maxLight / 100 * percentage);
    }*/

    void doHandleEvent(EventsHandler eventsHandler, boolean forRestartEvents) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                boolean inCall = false;
                TelephonyManager telephony = (TelephonyManager) eventsHandler.context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephony != null) {
                    int callState = telephony.getCallState();
                    inCall = (callState == TelephonyManager.CALL_STATE_RINGING) || (callState == TelephonyManager.CALL_STATE_OFFHOOK);
                }
                if (inCall) {
                    // not allowed changes during call
                    eventsHandler.notAllowedOrientation = true;
                } else if (!ApplicationPreferences.applicationEventOrientationEnableScanning) {
                    //if (forRestartEvents)
                    //    orientationPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesOrientation.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    //else
                    // not allowed for disabled orientation scanner
                    //    notAllowedOrientation = true;
                    eventsHandler.orientationPassed = false;
                } else if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn) {
                    if (forRestartEvents)
                        eventsHandler.orientationPassed = (EventPreferences.SENSOR_PASSED_PASSED & getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    else
                        // not allowed for screen Off
                        eventsHandler.notAllowedOrientation = true;
                } else {
                    synchronized (PPApplication.orientationScannerMutex) {
                        if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isOrientationScannerStarted()) {
                            PPApplication.startHandlerThreadOrientationScanner();
                            boolean lApplicationPassed = false;
                            if (!_ignoredApplications.isEmpty()) {
                                if (PPPExtenderBroadcastReceiver.isEnabled(eventsHandler.context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_3_0)) {
                                    String foregroundApplication = ApplicationPreferences.prefApplicationInForeground;
                                    if (!foregroundApplication.isEmpty()) {
                                        String[] splits = _ignoredApplications.split("\\|");
                                        for (String split : splits) {
                                            String packageName = Application.getPackageName(split);

                                            if (foregroundApplication.equals(packageName)) {
                                                lApplicationPassed = true;
                                                break;
                                            }
                                        }
                                    }
                                } else
                                    eventsHandler.notAllowedOrientation = true;
                            }
                            if (!lApplicationPassed) {
                                boolean lDisplayPassed = false;
                                boolean lSidePassed = false;

                                boolean hasAccelerometer = PPApplication.accelerometerSensor != null;
                                boolean hasMagneticField = PPApplication.magneticFieldSensor != null;
                                boolean hasProximity = PPApplication.proximitySensor != null;
                                boolean hasLight = PPApplication.lightSensor != null;

                                boolean enabledAll = (hasAccelerometer) && (hasMagneticField);

                                boolean configuredDisplay = false;
                                if (hasAccelerometer) {
                                    if (!_display.isEmpty()) {
                                        String[] splits = _display.split("\\|");
                                        if (splits.length > 0) {
                                            configuredDisplay = true;
                                            //lDisplayPassed = false;
                                            for (String split : splits) {
                                                try {
                                                    int side = Integer.parseInt(split);
                                                    if (side == PPApplication.handlerThreadOrientationScanner.resultDisplayUp) {
                                                        lDisplayPassed = true;
                                                        break;
                                                    }
                                                } catch (Exception e) {
                                                    PPApplication.recordException(e);
                                                }
                                            }
                                        }
                                    }
                                }

                                boolean configuredSide = false;
                                if (enabledAll) {
                                    if (!_sides.isEmpty()) {
                                        String[] splits = _sides.split("\\|");
                                        if (splits.length > 0) {
                                            configuredSide = true;
                                            //lSidePassed = false;
                                            for (String split : splits) {
                                                try {
                                                    int side = Integer.parseInt(split);
                                                    if (side == OrientationScannerHandlerThread.DEVICE_ORIENTATION_HORIZONTAL) {
                                                        if (PPApplication.handlerThreadOrientationScanner.resultSideUp == PPApplication.handlerThreadOrientationScanner.resultDisplayUp) {
                                                            lSidePassed = true;
                                                            break;
                                                        }
                                                    } else {
                                                        if (side == PPApplication.handlerThreadOrientationScanner.resultSideUp) {
                                                            lSidePassed = true;
                                                            break;
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    PPApplication.recordException(e);
                                                }
                                            }
                                        }
                                    }
                                }

                                boolean lDistancePassed = false;
                                boolean configuredDistance = false;
                                if (hasProximity) {
                                    if (_distance != 0) {
                                        configuredDistance = true;
                                        lDistancePassed = _distance == PPApplication.handlerThreadOrientationScanner.resultDeviceDistance;
                                    }
                                }

                                boolean lLightPassed = false;
                                boolean configuredLight = false;
                                if (hasLight) {
                                    if (_checkLight) {
                                        configuredLight = true;
                                        int light = PPApplication.handlerThreadOrientationScanner.resultLight;
                                        int min = Integer.parseInt(_lightMin);
                                        int max = Integer.parseInt(_lightMax);
                                        lLightPassed = (light >= min) && (light <= max);
                                        /*if (PPApplication.logEnabled()) {
                                            PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "light=" + light);
                                            PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "min=" + min);
                                            PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "max=" + max);
                                        }*/
                                    }
                                }

                                /*if (PPApplication.logEnabled()) {
                                    PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "configuredDisplay=" + configuredDisplay);
                                    PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "configuredSide=" + configuredSide);
                                    PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "configuredDistance=" + configuredDistance);
                                    PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "configuredLight=" + configuredLight);

                                    PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "hasAccelerometer=" + hasAccelerometer);
                                    PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "hasMagneticField=" + hasMagneticField);
                                    PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "hasProximity=" + hasProximity);
                                    PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "hasLight=" + hasLight);

                                    PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "lDisplayPassed=" + lDisplayPassed);
                                    PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "lSidePassed=" + lSidePassed);
                                    PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "lDistancePassed=" + lDistancePassed);
                                    PPApplication.logE("EventPreferencesOrientation.doHandleEvent", "lLightPassed=" + lLightPassed);
                                }*/

                                if (configuredDisplay || configuredSide || configuredDistance || configuredLight) {
                                    eventsHandler.orientationPassed = true;
                                    if (configuredDisplay)
                                        //noinspection ConstantConditions
                                        eventsHandler.orientationPassed = eventsHandler.orientationPassed && lDisplayPassed;
                                    if (configuredSide)
                                        eventsHandler.orientationPassed = eventsHandler.orientationPassed && lSidePassed;
                                    if (configuredDistance)
                                        eventsHandler.orientationPassed = eventsHandler.orientationPassed && lDistancePassed;
                                    if (configuredLight)
                                        eventsHandler.orientationPassed = eventsHandler.orientationPassed && lLightPassed;
                                }
                                else
                                    eventsHandler.notAllowedOrientation = true;
                                //orientationPassed = lDisplayPassed || lSidePassed || lDistancePassed || lLightPassed;
                            }
                        } else {
                            eventsHandler.notAllowedOrientation = true;
                        }
                    }
                }

                if (!eventsHandler.notAllowedOrientation) {
                    if (eventsHandler.orientationPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedOrientation = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                //PPApplication.logE("[TEST BATTERY] EventPreferencesOrientation.doHandleEvent", "orientation - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_ORIENTATION);
            }
        }
    }

}
