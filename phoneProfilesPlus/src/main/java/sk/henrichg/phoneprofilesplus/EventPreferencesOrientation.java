package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class EventPreferencesOrientation extends EventPreferences {

    String _display;
    String _sides;
    int _distance;
    String _ignoredApplications;

    static final String PREF_EVENT_ORIENTATION_ENABLED = "eventOrientationEnabled";
    private static final String PREF_EVENT_ORIENTATION_DISPLAY = "eventOrientationDisplay";
    private static final String PREF_EVENT_ORIENTATION_SIDES = "eventOrientationSides";
    private static final String PREF_EVENT_ORIENTATION_DISTANCE = "eventOrientationDistance";
    private static final String PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS = "eventOrientationIgnoredApplications";

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
        editor.commit();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_ORIENTATION_ENABLED, false);

        Set<String> set = preferences.getStringSet(PREF_EVENT_ORIENTATION_DISPLAY, null);
        String sides = "";
        if (set != null) {
            for (String s : set) {
                if (!sides.isEmpty())
                    sides = sides + "|";
                sides = sides + s;
            }
        }
        this._display = sides;

        set = preferences.getStringSet(PREF_EVENT_ORIENTATION_SIDES, null);
        sides = "";
        if (set != null) {
            for (String s : set) {
                if (!sides.isEmpty())
                    sides = sides + "|";
                sides = sides + s;
            }
        }
        this._sides = sides;

        this._distance = Integer.parseInt(preferences.getString(PREF_EVENT_ORIENTATION_DISTANCE, "0"));
        this._ignoredApplications = preferences.getString(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS, "");
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, Context context)
    {
        String descr = "";

        if (!this._enabled)
        {
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_orientation) + ": " + "</b>";
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

            if (PhoneProfilesService.getMagneticFieldSensor(context) != null) {
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
                descr = descr + "; " + context.getString(R.string.event_preferences_orientation_sides) + ": " + selectedSides;
            }

            String[] distanceValues = context.getResources().getStringArray(R.array.eventOrientationDistanceTypeValues);
            String[] distanceNames = context.getResources().getStringArray(R.array.eventOrientationDistanceTypeArray);
            int i = Arrays.asList(distanceValues).indexOf(String.valueOf(this._distance));
            if (i != -1)
                descr = descr + "; " + context.getString(R.string.event_preferences_orientation_distance) + ": " + distanceNames[i];

            String selectedApplications = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            if (!ForegroundApplicationChangedService.isEnabled(context.getApplicationContext())) {
                selectedApplications = context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
            }
            else
            if (!this._ignoredApplications.isEmpty() && !this._ignoredApplications.equals("-")) {
                String[] splits = this._ignoredApplications.split("\\|");
                if (splits.length == 1) {
                    String packageName = ApplicationsCache.getPackageName(splits[0]);

                    PackageManager packageManager = context.getPackageManager();
                    if (ApplicationsCache.getActivityName(splits[0]).isEmpty()) {
                        ApplicationInfo app;
                        try {
                            app = packageManager.getApplicationInfo(packageName, 0);
                            if (app != null)
                                selectedApplications = packageManager.getApplicationLabel(app).toString();
                        } catch (PackageManager.NameNotFoundException e) {
                            //e.printStackTrace();
                            selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
                        }
                    }
                    else {
                        Intent intent = new Intent();
                        intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                        if (info != null)
                            selectedApplications = info.loadLabel(packageManager).toString();
                    }
                }
                else
                    selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
            }
            descr = descr + "; " + "(S) "+context.getString(R.string.event_preferences_orientation_ignoreForApplications) + ": " + selectedApplications;

        }

        return descr;
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_ORIENTATION_DISPLAY)) {
            Preference preference = prefMng.findPreference(key);
            preference.setSummary(value);
            GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
        }
        if (key.equals(PREF_EVENT_ORIENTATION_SIDES)) {
            Preference preference = prefMng.findPreference(key);
            preference.setSummary(value);
            GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
        }
        if (key.equals(PREF_EVENT_ORIENTATION_DISTANCE))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
            //GlobalGUIRoutines.setPreferenceTitleStyle(listPreference, false, true, false);
        }
        if (key.equals(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS)) {
            Preference preference = prefMng.findPreference(key);
            GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, false, false, true);
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
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
            }
            else
                sides = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            setSummary(prefMng, key, sides, context);
        }

        if (key.equals(PREF_EVENT_ORIENTATION_DISTANCE))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }

        if (key.equals(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }

    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_ORIENTATION_DISPLAY, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_SIDES, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_DISTANCE, preferences, context);
        setSummary(prefMng, PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (PPApplication.isEventPreferenceAllowed(PREF_EVENT_ORIENTATION_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED) {
            EventPreferencesOrientation tmp = new EventPreferencesOrientation(this._event, this._enabled, this._display, this._sides, this._distance, this._ignoredApplications);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_CATEGORY);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ PPApplication.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    public boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

        if (PhoneProfilesService.getMagneticFieldSensor(context) != null)
            runnable = runnable && (!_display.isEmpty() || !_sides.isEmpty() || (_distance != 0));
        else
            runnable = runnable && (!_display.isEmpty() || (_distance != 0));

        return runnable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        boolean enabledAccelerometer = PhoneProfilesService.getAccelerometerSensor(context) != null;
        boolean enabledMagneticField = PhoneProfilesService.getMagneticFieldSensor(context) != null;
        boolean enabledAll = (enabledAccelerometer) && (enabledMagneticField);
        Preference preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_DISPLAY);
        if (preference != null) {
            if (!enabledAccelerometer)
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getString(R.string.preference_not_allowed_reason_no_hardware));
            preference.setEnabled(enabledAccelerometer);
        }
        preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_SIDES);
        if (preference != null) {
            if (!enabledAll)
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getString(R.string.preference_not_allowed_reason_no_hardware));
            preference.setEnabled(enabledAll);
        }
        boolean enabled = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY);
        preference = prefMng.findPreference(PREF_EVENT_ORIENTATION_DISTANCE);
        if (preference != null) {
            if (!enabled)
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getString(R.string.preference_not_allowed_reason_no_hardware));
            preference.setEnabled(enabled);
        }
        enabled = ForegroundApplicationChangedService.isEnabled(context.getApplicationContext());
        ApplicationsMultiSelectDialogPreference applicationsPreference = (ApplicationsMultiSelectDialogPreference) prefMng.findPreference(PREF_EVENT_ORIENTATION_IGNORED_APPLICATIONS);
        if (applicationsPreference != null) {
            applicationsPreference.setEnabled(enabled);
            applicationsPreference.setSummaryAMSDP();
        }
        SharedPreferences preferences = prefMng.getSharedPreferences();
        setCategorySummary(prefMng, "", preferences, context);
    }

    @Override
    public boolean activateReturnProfile()
    {
        return true;
    }

    @Override
    public void setSystemEventForStart(Context context)
    {
        if (PhoneProfilesService.instance != null) {
            if (_enabled && (!PhoneProfilesService.isOrientationScannerStarted())) {
                PPApplication.startOrientationScanner(context);
            }
        }
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
    }

    @Override
    public void removeSystemEvent(Context context)
    {
    }

}
