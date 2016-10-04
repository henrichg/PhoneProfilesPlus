package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Html;

public class EventPreferencesLocation extends EventPreferences {

    public String _geofences;
    public boolean _whenOutside;

    static final String PREF_EVENT_LOCATION_ENABLED = "eventLocationEnabled";
    static final String PREF_EVENT_LOCATION_GEOFENCES = "eventLocationGeofences";
    static final String PREF_EVENT_LOCATION_WHEN_OUTSIDE = "eventLocationStartWhenOutside";

    static final String PREF_EVENT_LOCATION_CATEGORY = "eventLocationCategory";

    private DataWrapper dataWrapper = null;

    public EventPreferencesLocation(Event event,
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
        this._enabled = ((EventPreferencesLocation)fromEvent._eventPreferencesLocation)._enabled;
        this._geofences = ((EventPreferencesLocation)fromEvent._eventPreferencesLocation)._geofences;
        this._whenOutside = ((EventPreferencesLocation)fromEvent._eventPreferencesLocation)._whenOutside;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Editor editor = preferences.edit();
            editor.putBoolean(PREF_EVENT_LOCATION_ENABLED, _enabled);
            editor.putString(PREF_EVENT_LOCATION_GEOFENCES, this._geofences);
            editor.putBoolean(PREF_EVENT_LOCATION_WHEN_OUTSIDE, this._whenOutside);
            editor.commit();
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

        if (!this._enabled)
        {
            ;
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_locations) + ": " + "</b>";
            }

            String selectedLocations = "";
            String[] splits = this._geofences.split("\\|");
            for (String _geofence : splits) {
                if (_geofence.isEmpty()) {
                    selectedLocations = selectedLocations + context.getString(R.string.applications_multiselect_summary_text_not_selected);
                }
                else
                if (splits.length == 1) {
                    selectedLocations = selectedLocations + getGeofenceName(Long.valueOf(_geofence), context);
                }
                else {
                    selectedLocations = context.getString(R.string.applications_multiselect_summary_text_selected);
                    selectedLocations = selectedLocations + " " + splits.length;
                    break;
                }
            }
            descr = descr + selectedLocations;
            if (this._whenOutside)
                descr = descr + "; " + context.getString(R.string.event_preferences_location_when_outside_description);
        }

        return descr;
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_LOCATION_GEOFENCES)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String[] splits = value.split("\\|");
                for (String _geofence : splits) {
                    if (_geofence.isEmpty()) {
                        preference.setSummary(R.string.applications_multiselect_summary_text_not_selected);
                    }
                    else
                    if (splits.length == 1) {
                        preference.setSummary(getGeofenceName(Long.valueOf(_geofence), context));
                    }
                    else {
                        String selectedLocations = context.getString(R.string.applications_multiselect_summary_text_selected);
                        selectedLocations = selectedLocations + " " + splits.length;
                        preference.setSummary(selectedLocations);
                        break;
                    }
                }
                GUIData.setPreferenceTitleStyle(preference, false, true, false);
            }
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_LOCATION_GEOFENCES))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_LOCATION_GEOFENCES, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (GlobalData.isEventPreferenceAllowed(PREF_EVENT_LOCATION_ENABLED, context) == GlobalData.PREFERENCE_ALLOWED) {
            EventPreferencesLocation tmp = new EventPreferencesLocation(this._event, this._enabled, this._geofences, this._whenOutside);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_CATEGORY);
            if (preference != null) {
                GUIData.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable(context));
                preference.setSummary(Html.fromHtml(tmp.getPreferencesDescription(false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getResources().getString(GlobalData.getNotAllowedPreferenceReasonString()));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    public boolean isRunnable(Context context)
    {

        boolean runable = super.isRunnable(context);

        runable = runable && (!_geofences.isEmpty());

        return runable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        final boolean enabled = PhoneProfilesService.isLocationEnabled(context.getApplicationContext());
        Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_GEOFENCES);
        if (preference != null) preference.setEnabled(enabled);
            preference = prefMng.findPreference(PREF_EVENT_LOCATION_WHEN_OUTSIDE);
        if (preference != null) preference.setEnabled(enabled);
    }

    @Override
    public boolean activateReturnProfile()
    {
        return true;
    }

    @Override
    public void setSystemEventForStart(Context context)
    {
        if (_enabled && (!GeofenceScannerAlarmBroadcastReceiver.isAlarmSet(context/*, false*/)))
            GeofenceScannerAlarmBroadcastReceiver.setAlarm(context, true, false);
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
    }

    @Override
    public void removeSystemEvent(Context context)
    {
    }

    private String getGeofenceName(long geofenceId, Context context) {
        if (dataWrapper == null)
            dataWrapper = new DataWrapper(context.getApplicationContext(), false, false, 0);
        String name = dataWrapper.getDatabaseHandler().getGeofenceName(geofenceId);
        if (name.isEmpty())
            name = context.getString(R.string.event_preferences_locations_location_not_selected);
        return name;
    }
}
