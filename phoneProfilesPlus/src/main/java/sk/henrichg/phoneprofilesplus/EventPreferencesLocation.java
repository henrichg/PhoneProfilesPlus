package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.PreferenceManager;

class EventPreferencesLocation extends EventPreferences {

    String _geofences;
    boolean _whenOutside;

    static final String PREF_EVENT_LOCATION_ENABLED = "eventLocationEnabled";
    private static final String PREF_EVENT_LOCATION_GEOFENCES = "eventLocationGeofences";
    private static final String PREF_EVENT_LOCATION_WHEN_OUTSIDE = "eventLocationStartWhenOutside";
    private static final String PREF_EVENT_LOCATION_APP_SETTINGS = "eventLocationScanningSystemSettings";

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
    public String getPreferencesDescription(boolean addBullet, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_location_summary);
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_locations) + ": " + "</b>";
            }

            String selectedLocations = "";
            if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                selectedLocations = context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
            }
            else {
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
            descr = descr + "(S) "+context.getString(R.string.event_preferences_locations_location) + ": " + selectedLocations;
            if (this._whenOutside)
                descr = descr + "; " + context.getString(R.string.event_preferences_location_when_outside_description);
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_LOCATION_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                if (!ApplicationPreferences.applicationEventLocationEnableScannig(context))
                    preference.setSummary(context.getResources().getString(R.string.phone_profiles_pref_applicationEventScanningDisabled) + "\n" +
                            context.getResources().getString(R.string.phone_profiles_pref_eventLocationAppSettings_summary));
                else
                    preference.setSummary(context.getResources().getString(R.string.phone_profiles_pref_eventLocationAppSettings_summary));
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
                if (!ApplicationPreferences.applicationEventLocationEnableScannig(context.getApplicationContext())) {
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
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, true);
            }
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_LOCATION_GEOFENCES) ||
            key.equals(PREF_EVENT_LOCATION_APP_SETTINGS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_LOCATION_GEOFENCES, preferences, context);
        setSummary(prefMng, PREF_EVENT_LOCATION_APP_SETTINGS, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        if (Event.isEventPreferenceAllowed(PREF_EVENT_LOCATION_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED) {
            EventPreferencesLocation tmp = new EventPreferencesLocation(this._event, this._enabled, this._geofences, this._whenOutside);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_CATEGORY);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_CATEGORY);
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

        runnable = runnable && (!_geofences.isEmpty());

        return runnable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        final boolean enabled = PhoneProfilesService.isLocationEnabled(context.getApplicationContext())/* &&
                                ApplicationPreferences.applicationEventLocationEnableScannig(context.getApplicationContext())*/;
        Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_GEOFENCES);
        if (preference != null) preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_LOCATION_WHEN_OUTSIDE);
        if (preference != null) preference.setEnabled(enabled);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        setSummary(prefMng, PREF_EVENT_LOCATION_GEOFENCES, preferences, context);
        setSummary(prefMng, PREF_EVENT_LOCATION_APP_SETTINGS, preferences, context);
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
            dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0);
        String name = DatabaseHandler.getInstance(context.getApplicationContext()).getGeofenceName(geofenceId);
        if (name.isEmpty())
            name = context.getString(R.string.event_preferences_locations_location_not_selected);
        return name;
    }
}
