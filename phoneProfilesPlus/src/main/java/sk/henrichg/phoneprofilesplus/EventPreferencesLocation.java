package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

class EventPreferencesLocation extends EventPreferences {

    String _geofences;
    boolean _whenOutside;

    static final String PREF_EVENT_LOCATION_ENABLED = "eventLocationEnabled";
    static final String PREF_EVENT_LOCATION_GEOFENCES = "eventLocationGeofences";
    private static final String PREF_EVENT_LOCATION_WHEN_OUTSIDE = "eventLocationStartWhenOutside";
    static final String PREF_EVENT_LOCATION_APP_SETTINGS = "eventLocationScanningAppSettings";
    static final String PREF_EVENT_LOCATION_LOCATION_SYSTEM_SETTINGS = "eventLocationLocationSystemSettings";

    private static final String PREF_EVENT_LOCATION_CATEGORY = "eventLocationCategory";

    private DataWrapper dataWrapper = null;

    EventPreferencesLocation(Event event,
                                    boolean enabled,
                                    String geofences,
                                    boolean _whenOutside)
    {
        super(event, enabled);

        this._geofences = geofences;
        this._whenOutside = _whenOutside;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesLocation._enabled;
        this._geofences = fromEvent._eventPreferencesLocation._geofences;
        this._whenOutside = fromEvent._eventPreferencesLocation._whenOutside;
        this.setSensorPassed(fromEvent._eventPreferencesLocation.getSensorPassed());
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Editor editor = preferences.edit();
            editor.putBoolean(PREF_EVENT_LOCATION_ENABLED, _enabled);
            editor.putString(PREF_EVENT_LOCATION_GEOFENCES, this._geofences);
            editor.putBoolean(PREF_EVENT_LOCATION_WHEN_OUTSIDE, this._whenOutside);
            editor.apply();
        //}
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this._enabled = preferences.getBoolean(PREF_EVENT_LOCATION_ENABLED, false);
            this._geofences = preferences.getString(PREF_EVENT_LOCATION_GEOFENCES, "");
            this._whenOutside = preferences.getBoolean(PREF_EVENT_LOCATION_WHEN_OUTSIDE, false);
        //}
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_location_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_LOCATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>\u2022 ";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_locations), addPassStatus, DatabaseHandler.ETYPE_LOCATION, context);
                    descr = descr + ": </b>";
                }

                if (!ApplicationPreferences.applicationEventLocationEnableScanning(context)) {
                    if (!ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(context))
                        descr = descr + "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *<br>";
                    else
                        descr = descr + context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "<br>";
                }
                if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                    descr = descr + "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *<br>";
                }

                String selectedLocations = "";
                if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                    selectedLocations = context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
                } else {
                    String[] splits = this._geofences.split("\\|");
                    for (String _geofence : splits) {
                        if (_geofence.isEmpty()) {
                            //noinspection StringConcatenationInLoop
                            selectedLocations = selectedLocations + context.getString(R.string.applications_multiselect_summary_text_not_selected);
                        } else if (splits.length == 1) {
                            selectedLocations = selectedLocations + getGeofenceName(Long.valueOf(_geofence), context);
                        } else {
                            selectedLocations = context.getString(R.string.applications_multiselect_summary_text_selected);
                            selectedLocations = selectedLocations + " " + splits.length;
                            break;
                        }
                    }
                }
                descr = descr + /*"(S) "+*/context.getString(R.string.event_preferences_locations_location) + ": " + selectedLocations;
                if (this._whenOutside)
                    descr = descr + "; " + context.getString(R.string.event_preferences_location_when_outside_description);
            }
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_LOCATION_ENABLED)) {
            CheckBoxPreference preference = (CheckBoxPreference) prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, true, preference.isChecked(), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_LOCATION_ENABLED) ||
            key.equals(PREF_EVENT_LOCATION_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_APP_SETTINGS);
            if (preference != null) {
                String summary;
                int titleColor;
                if (!ApplicationPreferences.applicationEventLocationEnableScanning(context)) {
                    if (!ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(context)) {
                        summary = "* " + context.getResources().getString(R.string.phone_profiles_pref_applicationEventScanningDisabled) + " *\n" +
                                context.getResources().getString(R.string.phone_profiles_pref_eventLocationAppSettings_summary);
                        titleColor = Color.RED; //0xFFffb000;
                    }
                    else {
                        summary = context.getResources().getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "\n" +
                                context.getResources().getString(R.string.phone_profiles_pref_eventLocationAppSettings_summary);
                        titleColor = 0;
                    }
                }
                else {
                    summary =  context.getResources().getString(R.string.array_pref_applicationDisableScanning_enabled) + ".\n" +
                            context.getResources().getString(R.string.phone_profiles_pref_eventLocationAppSettings_summary);
                    titleColor = 0;
                }
                CharSequence sTitle = preference.getTitle();
                Spannable sbt = new SpannableString(sTitle);
                Object spansToRemove[] = sbt.getSpans(0, sTitle.length(), Object.class);
                for(Object span: spansToRemove){
                    if(span instanceof CharacterStyle)
                        sbt.removeSpan(span);
                }
                CheckBoxPreference enabledPreference = (CheckBoxPreference)prefMng.findPreference(PREF_EVENT_LOCATION_ENABLED);
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
        if (key.equals(PREF_EVENT_LOCATION_LOCATION_SYSTEM_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary = context.getString(R.string.phone_profiles_pref_eventLocationSystemSettings_summary);
                if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                    summary = "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *\n" +
                            summary;
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_LOCATION_GEOFENCES)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                    preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                            ": "+context.getResources().getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings));
                }
                /*else
                if (!ApplicationPreferences.applicationEventLocationEnableScanning(context.getApplicationContext())) {
                    preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                            ": "+context.getResources().getString(R.string.preference_not_allowed_reason_not_enabled_scanning));
                }*/
                else {
                    String[] splits = value.split("\\|");
                    for (String _geofence : splits) {
                        if (_geofence.isEmpty()) {
                            preference.setSummary(R.string.applications_multiselect_summary_text_not_selected);
                        } else if (splits.length == 1) {
                            preference.setSummary(getGeofenceName(Long.valueOf(_geofence), context));
                        } else {
                            String selectedLocations = context.getString(R.string.applications_multiselect_summary_text_selected);
                            selectedLocations = selectedLocations + " " + splits.length;
                            preference.setSummary(selectedLocations);
                            break;
                        }
                    }
                }
                //GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, true);
            }
        }
        if (key.equals(PREF_EVENT_LOCATION_WHEN_OUTSIDE)) {
            CheckBoxPreference preference = (CheckBoxPreference) prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, true, preference.isChecked(), false, false, false);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesLocation.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesLocation.isRunnable(context);
        CheckBoxPreference enabledPreference = (CheckBoxPreference)prefMng.findPreference(PREF_EVENT_LOCATION_ENABLED);
        boolean enabled = (enabledPreference != null) && enabledPreference.isChecked();
        Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_GEOFENCES);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_LOCATION_GEOFENCES, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyle(preference, enabled, bold, true, !isRunnable, true);
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_LOCATION_ENABLED) ||
            key.equals(PREF_EVENT_LOCATION_WHEN_OUTSIDE)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true" : "false", context);
        }
        if (key.equals(PREF_EVENT_LOCATION_GEOFENCES) ||
            key.equals(PREF_EVENT_LOCATION_APP_SETTINGS) ||
            key.equals(PREF_EVENT_LOCATION_LOCATION_SYSTEM_SETTINGS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_LOCATION_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_LOCATION_GEOFENCES, preferences, context);
        setSummary(prefMng, PREF_EVENT_LOCATION_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_LOCATION_LOCATION_SYSTEM_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_LOCATION_WHEN_OUTSIDE, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_LOCATION_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesLocation tmp = new EventPreferencesLocation(this._event, this._enabled, this._geofences, this._whenOutside);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_CATEGORY);
            if (preference != null) {
                CheckBoxPreference enabledPreference = (CheckBoxPreference)prefMng.findPreference(PREF_EVENT_LOCATION_ENABLED);
                boolean enabled = (enabledPreference != null) && enabledPreference.isChecked();
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, enabled, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_CATEGORY);
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

        runnable = runnable && (!_geofences.isEmpty());

        return runnable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        final boolean enabled = PhoneProfilesService.isLocationEnabled(context.getApplicationContext())/* &&
                                ApplicationPreferences.applicationEventLocationEnableScanning(context.getApplicationContext())*/;
        Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_GEOFENCES);
        if (preference != null) preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_LOCATION_WHEN_OUTSIDE);
        if (preference != null) preference.setEnabled(enabled);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        setSummary(prefMng, PREF_EVENT_LOCATION_GEOFENCES, preferences, context);
        setSummary(prefMng, PREF_EVENT_LOCATION_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_LOCATION_LOCATION_SYSTEM_SETTINGS, preferences, context);
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

    private String getGeofenceName(long geofenceId, Context context) {
        if (dataWrapper == null)
            dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
        String name = DatabaseHandler.getInstance(context.getApplicationContext()).getGeofenceName(geofenceId);
        if (name.isEmpty())
            name = context.getString(R.string.event_preferences_locations_location_not_selected);
        return name;
    }
}
