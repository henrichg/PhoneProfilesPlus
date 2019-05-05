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
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.SENSOR_SERVICE;

class EventPreferencesOrientation extends EventPreferences {

    String _display;
    String _sides;
    int _distance;
    String _ignoredApplications;

    static final String PREF_EVENT_ORIENTATION_ENABLED = "eventOrientationEnabled";
    private static final String PREF_EVENT_ORIENTATION_DISPLAY = "eventOrientationDisplay";
    private static final String PREF_EVENT_ORIENTATION_SIDES = "eventOrientationSides";
    private static final String PREF_EVENT_ORIENTATION_DISTANCE = "eventOrientationDistance";
    static final String PREF_EVENT_ORIENTATION_INSTALL_EXTENDER = "eventOrientationInstallExtender";
    static final String PREF_EVENT_ORIENTATION_ACCESSIBILITY_SETTINGS = "eventOrientationAccessibilitySettings";
    private static final String PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS = "eventOrientationIgnoredApplications";
    private static final String PREF_EVENT_ORIENTATION_APP_SETTINGS = "eventEnableOrientationScanningAppSettings";

    private static final String PREF_EVENT_ORIENTATION_CATEGORY = "eventOrientationCategory";


    EventPreferencesOrientation(Event event,
                                       boolean enabled,
                                       String display,
                                       String sides,
                                       int distance,
                                       String ignoredApplications)
    {
        super(event, enabled);

        this._display = display;
        this._sides = sides;
        this._distance = distance;
        this._ignoredApplications = ignoredApplications;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesOrientation._enabled;
        this._display = fromEvent._eventPreferencesOrientation._display;
        this._sides = fromEvent._eventPreferencesOrientation._sides;
        this._distance = fromEvent._eventPreferencesOrientation._distance;
        this._ignoredApplications = fromEvent._eventPreferencesOrientation._ignoredApplications;
        this.setSensorPassed(fromEvent._eventPreferencesOrientation.getSensorPassed());
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
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
        editor.putString(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS, this._ignoredApplications);

        editor.apply();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
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
        this._ignoredApplications = preferences.getString(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS, "");
    }

    @SuppressWarnings("StringConcatenationInLoop")
    @Override
    public String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_orientation_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_ORIENTATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>\u2022 ";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_orientation), addPassStatus, DatabaseHandler.ETYPE_ORIENTATION, context);
                    descr = descr + ": </b>";
                }

                if (!ApplicationPreferences.applicationEventOrientationEnableScanning(context)) {
                    if (!ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(context))
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
                        if (!selectedSides.isEmpty())
                            selectedSides = selectedSides + ", ";
                        selectedSides = selectedSides + sideNames[Arrays.asList(sideValues).indexOf(s)];
                    }
                }
                descr = descr + context.getString(R.string.event_preferences_orientation_display) + ": " + selectedSides;

                SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

                if ((sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null)) {
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
                    descr = descr + " • " + context.getString(R.string.event_preferences_orientation_sides) + ": " + selectedSides;
                }

                String[] distanceValues = context.getResources().getStringArray(R.array.eventOrientationDistanceTypeValues);
                String[] distanceNames = context.getResources().getStringArray(R.array.eventOrientationDistanceTypeArray);
                int i = Arrays.asList(distanceValues).indexOf(String.valueOf(this._distance));
                if (i != -1)
                    descr = descr + " • " + context.getString(R.string.event_preferences_orientation_distance) + ": " + distanceNames[i];

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
                descr = descr + " • " + /*"(S) "+*/context.getString(R.string.event_preferences_orientation_ignoreForApplications) + ": " + selectedApplications;
            }
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_ORIENTATION_ENABLED)) {
            CheckBoxPreference preference = (CheckBoxPreference) prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, true, preference.isChecked(), true, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_ORIENTATION_ENABLED) ||
            key.equals(PREF_EVENT_ORIENTATION_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_APP_SETTINGS);
            String summary;
            int titleColor;
            if (preference != null) {
                if (!ApplicationPreferences.applicationEventOrientationEnableScanning(context)) {
                    if (!ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(context)) {
                        summary = "* " + context.getResources().getString(R.string.phone_profiles_pref_applicationEventScanningDisabled) + " *\n" +
                                context.getResources().getString(R.string.phone_profiles_pref_eventOrientationAppSettings_summary);
                        titleColor = Color.RED; //0xFFffb000;
                    }
                    else {
                        summary = context.getResources().getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "\n" +
                                context.getResources().getString(R.string.phone_profiles_pref_eventOrientationAppSettings_summary);
                        titleColor = 0;
                    }
                }
                else {
                    summary = context.getResources().getString(R.string.array_pref_applicationDisableScanning_enabled) + ".\n" +
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
                CheckBoxPreference enabledPreference = (CheckBoxPreference)prefMng.findPreference(PREF_EVENT_ORIENTATION_ENABLED);
                if ((enabledPreference != null) && enabledPreference.isChecked()) {
                    if (titleColor != 0)
                        sbt.setSpan(new ForegroundColorSpan(titleColor), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    preference.setTitle(sbt);
                }
                else {
                    preference.setTitle(sbt);
                }
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
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_ORIENTATION_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0)
                    preference.setSummary(R.string.event_preferences_orientation_PPPExtender_install_summary);
                else
                if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0)
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                else
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_upgrade_summary);
            }
        }
        if (key.equals(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS)) {
            Preference preference = prefMng.findPreference(key);
            GlobalGUIRoutines.setPreferenceTitleStyle(preference, true, !value.isEmpty(), true, false, false, true);
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesOrientation.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesOrientation.isRunnable(context);
        CheckBoxPreference enabledPreference = (CheckBoxPreference)prefMng.findPreference(PREF_EVENT_ORIENTATION_ENABLED);
        boolean enabled = (enabledPreference != null) && enabledPreference.isChecked();
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
            GlobalGUIRoutines.setPreferenceTitleStyle(preference, enabled, bold, true, true, !isRunnable, false);
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
            GlobalGUIRoutines.setPreferenceTitleStyle(preference, enabled, bold, true, true, !isRunnable, false);
        }
        ListPreference distancePreference = (ListPreference)prefMng.findPreference(PREF_EVENT_ORIENTATION_DISTANCE);
        if (distancePreference != null) {
            int index = distancePreference.findIndexOfValue(distancePreference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyle(distancePreference, enabled, index > 0, true, true, !isRunnable, false);
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_ORIENTATION_ENABLED)) {
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
        if (key.equals(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS) ||
            key.equals(PREF_EVENT_ORIENTATION_INSTALL_EXTENDER) ||
            key.equals(PREF_EVENT_ORIENTATION_APP_SETTINGS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }

    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_ORIENTATION_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_DISPLAY, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_SIDES, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_DISTANCE, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_INSTALL_EXTENDER, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_APP_SETTINGS, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_ORIENTATION_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesOrientation tmp = new EventPreferencesOrientation(this._event, this._enabled, this._display, this._sides, this._distance, this._ignoredApplications);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_CATEGORY);
            if (preference != null) {
                CheckBoxPreference enabledPreference = (CheckBoxPreference)prefMng.findPreference(PREF_EVENT_ORIENTATION_ENABLED);
                boolean enabled = (enabledPreference != null) && enabledPreference.isChecked();
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, enabled, tmp._enabled, true, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context)));
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
    public boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

        SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        if ((sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null))
            runnable = runnable && (!_display.isEmpty() || !_sides.isEmpty() || (_distance != 0));
        else
            runnable = runnable && (!_display.isEmpty() || (_distance != 0));

        return runnable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        boolean hasAccelerometer = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null);
        boolean hasMagneticField = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null);
        boolean hasProximity = (sensorManager != null) && (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null);
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
        enabled = PPPExtenderBroadcastReceiver.isEnabled(context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_3_0);
        ApplicationsMultiSelectDialogPreference applicationsPreference = (ApplicationsMultiSelectDialogPreference) prefMng.findPreference(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS);
        if (applicationsPreference != null) {
            applicationsPreference.setEnabled(enabled);
            applicationsPreference.setSummaryAMSDP();
        }
        SharedPreferences preferences = prefMng.getSharedPreferences();
        setSummary(prefMng, PREF_EVENT_ORIENTATION_APP_SETTINGS, preferences, context);
        setCategorySummary(prefMng, preferences, context);
    }

    /*
    @Override
    public void setSystemEventForStart(Context context)
    {
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
    }

    @Override
    public void removeSystemEvent(Context context)
    {
    }
    */
}
