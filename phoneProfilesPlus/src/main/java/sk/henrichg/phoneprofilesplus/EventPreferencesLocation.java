package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesLocation extends EventPreferences {

    String _geofences;
    boolean _whenOutside;

    static final String PREF_EVENT_LOCATION_ENABLED = "eventLocationEnabled";
    static final String PREF_EVENT_LOCATION_GEOFENCES = "eventLocationGeofences";
    private static final String PREF_EVENT_LOCATION_WHEN_OUTSIDE = "eventLocationStartWhenOutside";
    static final String PREF_EVENT_LOCATION_APP_SETTINGS = "eventLocationScanningAppSettings";
    static final String PREF_EVENT_LOCATION_LOCATION_SYSTEM_SETTINGS = "eventLocationLocationSystemSettings";

    private static final String PREF_EVENT_LOCATION_CATEGORY = "eventLocationCategoryRoot";

    EventPreferencesLocation(Event event,
                                    boolean enabled,
                                    String geofences,
                                    boolean _whenOutside)
    {
        super(event, enabled);

        this._geofences = geofences;
        this._whenOutside = _whenOutside;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesLocation._enabled;
        this._geofences = fromEvent._eventPreferencesLocation._geofences;
        this._whenOutside = fromEvent._eventPreferencesLocation._whenOutside;
        this.setSensorPassed(fromEvent._eventPreferencesLocation.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Editor editor = preferences.edit();
            editor.putBoolean(PREF_EVENT_LOCATION_ENABLED, _enabled);
            editor.putString(PREF_EVENT_LOCATION_GEOFENCES, this._geofences);
            editor.putBoolean(PREF_EVENT_LOCATION_WHEN_OUTSIDE, this._whenOutside);
            editor.apply();
        //}
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this._enabled = preferences.getBoolean(PREF_EVENT_LOCATION_ENABLED, false);
            this._geofences = preferences.getString(PREF_EVENT_LOCATION_GEOFENCES, "");
            this._whenOutside = preferences.getBoolean(PREF_EVENT_LOCATION_WHEN_OUTSIDE, false);
        //}
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_location_summary);
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_LOCATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_locations), addPassStatus, DatabaseHandler.ETYPE_LOCATION, context);
                    descr = descr + "</b> ";
                }

                if (!ApplicationPreferences.applicationEventLocationEnableScanning) {
                    if (!ApplicationPreferences.applicationEventLocationDisabledScannigByProfile)
                        descr = descr + "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *<br>";
                    else
                        descr = descr + context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "<br>";
                }
                else
                if (!GlobalUtils.isLocationEnabled(context.getApplicationContext())) {
                    descr = descr + "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *<br>";
                }

                String selectedLocations;// = "";
                StringBuilder value = new StringBuilder();
                if (!GlobalUtils.isLocationEnabled(context.getApplicationContext())) {
                    //selectedLocations = context.getString(R.string.profile_preferences_device_not_allowed) +
                    //        ": " + context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
                    value.append(context.getString(R.string.profile_preferences_device_not_allowed)).
                            append(": ").
                            append(context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings));
                } else {
                    String[] splits = this._geofences.split("\\|");
                    for (String _geofence : splits) {
                        if (_geofence.isEmpty()) {
                            //selectedLocations = selectedLocations + context.getString(R.string.applications_multiselect_summary_text_not_selected);
                            value.append(context.getString(R.string.applications_multiselect_summary_text_not_selected));
                        } else if (splits.length == 1) {
                            //selectedLocations = selectedLocations + getGeofenceName(Long.parseLong(_geofence), context);
                            value.append(getGeofenceName(Long.parseLong(_geofence), context));
                        } else {
                            //selectedLocations = context.getString(R.string.applications_multiselect_summary_text_selected);
                            //selectedLocations = selectedLocations + " " + splits.length;
                            value.append(context.getString(R.string.applications_multiselect_summary_text_selected));
                            value.append(" ").append(splits.length);
                            break;
                        }
                    }
                }
                selectedLocations = value.toString();
                descr = descr + context.getString(R.string.event_preferences_locations_location) + ": <b>" + getColorForChangedPreferenceValue(selectedLocations, disabled, context) + "</b>";
                if (this._whenOutside)
                    descr = descr + " • <b>" + getColorForChangedPreferenceValue(context.getString(R.string.event_preferences_location_when_outside_description), disabled, context) + "</b>";
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key/*, String value*/, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_LOCATION_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_LOCATION_ENABLED) ||
            key.equals(PREF_EVENT_LOCATION_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_APP_SETTINGS);
            if (preference != null) {
                String summary;
                int titleColor;
                if (!ApplicationPreferences.applicationEventLocationEnableScanning) {
                    if (!ApplicationPreferences.applicationEventLocationDisabledScannigByProfile) {
                        summary = "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *\n\n" +
                                context.getString(R.string.phone_profiles_pref_eventLocationAppSettings_summary);
                        titleColor = ContextCompat.getColor(context, R.color.error_color);
                    }
                    else {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "\n\n" +
                                context.getString(R.string.phone_profiles_pref_eventLocationAppSettings_summary);
                        titleColor = 0;
                    }
                }
                else {
                    summary =  context.getString(R.string.array_pref_applicationDisableScanning_enabled) + ".\n\n" +
                            context.getString(R.string.phone_profiles_pref_eventLocationAppSettings_summary);
                    titleColor = 0;
                }
                CharSequence sTitle = preference.getTitle();
                int titleLenght = 0;
                if (sTitle != null)
                    titleLenght = sTitle.length();
                Spannable sbt = new SpannableString(sTitle);
                Object[] spansToRemove = sbt.getSpans(0, titleLenght, Object.class);
                for(Object span: spansToRemove){
                    if(span instanceof CharacterStyle)
                        sbt.removeSpan(span);
                }
                if (preferences.getBoolean(PREF_EVENT_LOCATION_ENABLED, false)) {
                    if (titleColor != 0)
                        sbt.setSpan(new ForegroundColorSpan(titleColor), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                preference.setTitle(sbt);
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_LOCATION_LOCATION_SYSTEM_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary = context.getString(R.string.phone_profiles_pref_eventLocationSystemSettings_summary);
                if (!GlobalUtils.isLocationEnabled(context.getApplicationContext())) {
                    summary = "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *\n\n" +
                            summary;
                }
                else {
                    summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n"+
                            summary;
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_LOCATION_WHEN_OUTSIDE)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesLocation.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesLocation.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_LOCATION_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_GEOFENCES);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_LOCATION_GEOFENCES, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_LOCATION_ENABLED) ||
            key.equals(PREF_EVENT_LOCATION_WHEN_OUTSIDE)) {
            //boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, /*value ? "true" : "false",*/ context);
        }
        if (key.equals(PREF_EVENT_LOCATION_GEOFENCES) ||
            key.equals(PREF_EVENT_LOCATION_APP_SETTINGS) ||
            key.equals(PREF_EVENT_LOCATION_LOCATION_SYSTEM_SETTINGS))
        {
            setSummary(prefMng, key, /*preferences.getString(key, ""),*/ context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_LOCATION_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_LOCATION_GEOFENCES, preferences, context);
        setSummary(prefMng, PREF_EVENT_LOCATION_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_LOCATION_LOCATION_SYSTEM_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_LOCATION_WHEN_OUTSIDE, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_LOCATION_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesLocation tmp = new EventPreferencesLocation(this._event, this._enabled, this._geofences, this._whenOutside);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_LOCATION_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_LOCATION_SCANNER).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false, false, false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

        runnable = runnable && (!_geofences.isEmpty());

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_LOCATION_ENABLED) != null) {
                final boolean enabled = GlobalUtils.isLocationEnabled(context.getApplicationContext())/* &&
                                ApplicationPreferences.applicationEventLocationEnableScanning(context.getApplicationContext())*/;
                Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_GEOFENCES);
                if (preference != null) preference.setEnabled(enabled);
                preference = prefMng.findPreference(PREF_EVENT_LOCATION_WHEN_OUTSIDE);
                if (preference != null) preference.setEnabled(enabled);
                setSummary(prefMng, PREF_EVENT_LOCATION_GEOFENCES, preferences, context);
                setSummary(prefMng, PREF_EVENT_LOCATION_APP_SETTINGS, preferences, context);
                setSummary(prefMng, PREF_EVENT_LOCATION_LOCATION_SYSTEM_SETTINGS, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    /*
    @Override
    void setSystemEventForStart(Context context)
    {
    }

    @Override
    void setSystemEventForPause(Context context)
    {
    }

    @Override
    void removeSystemEvent(Context context)
    {
    }
    */

    static String getGeofenceName(long geofenceId, Context context) {
        String name = DatabaseHandler.getInstance(context.getApplicationContext()).getGeofenceName(geofenceId);
        if (name.isEmpty())
            name = context.getString(R.string.event_preferences_locations_location_not_selected);
        return name;
    }

    void doHandleEvent(EventsHandler eventsHandler, boolean forRestartEvents) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventLocation(context, event, null)*/) {
                if (!ApplicationPreferences.applicationEventLocationEnableScanning) {
                    //if (forRestartEvents)
                    //    locationPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesLocation.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    //else {
                    // not allowed for disabled location scanner
                    //    notAllowedLocation = true;
                    //}
                    eventsHandler.locationPassed = false;
                } else {
                    //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn) {
                        if (forRestartEvents)
                            eventsHandler.locationPassed = (EventPreferences.SENSOR_PASSED_PASSED & getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                        else {
                            // not allowed for screen Off
                            eventsHandler.notAllowedLocation = true;
                        }
                    } else {
                        if ((PhoneProfilesService.getInstance() != null) && (PPApplication.locationScanner != null)) {
                            boolean transitionsUpdated;
                            synchronized (PPApplication.locationScannerMutex) {
                                transitionsUpdated = LocationScanner.mTransitionsUpdated;
                            }
                            if (transitionsUpdated) {
                                String[] splits = _geofences.split("\\|");
                                boolean[] passed = new boolean[splits.length];

                                int i = 0;
                                for (String _geofence : splits) {
                                    passed[i] = false;
                                    if (!_geofence.isEmpty()) {

                                        int geofenceTransition = DatabaseHandler.getInstance(eventsHandler.context).getGeofenceTransition(Long.parseLong(_geofence));
                                        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                                            passed[i] = true;
                                        }
                                    }
                                    ++i;
                                }

                                if (_whenOutside) {
                                    // all locations must not be passed
                                    eventsHandler.locationPassed = true;
                                    for (boolean pass : passed) {
                                        if (pass) {
                                            eventsHandler.locationPassed = false;
                                            break;
                                        }
                                    }
                                } else {
                                    // one location must be passed
                                    eventsHandler.locationPassed = false;
                                    for (boolean pass : passed) {
                                        if (pass) {
                                            eventsHandler.locationPassed = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            else
                                eventsHandler.notAllowedLocation = true;

                        } else {
                            eventsHandler.notAllowedLocation = true;
                        }
                    }
                }

                if (!eventsHandler.notAllowedLocation) {
                    if (eventsHandler.locationPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedLocation = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_LOCATION);
            }
        }
    }

}
