package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Html;

public class EventPreferencesLocation extends EventPreferences {

    public long _geofenceId;

    static final String PREF_EVENT_LOCATION_ENABLED = "eventLocationEnabled";
    static final String PREF_EVENT_LOCATION_GEOFENCE_ID = "eventLocationGeofenceId";

    static final String PREF_EVENT_LOCATION_CATEGORY = "eventLocationCategory";

    private DataWrapper dataWrapper = null;

    public EventPreferencesLocation(Event event,
                                    boolean enabled,
                                    long geofenceId)
    {
        super(event, enabled);

        this._geofenceId = geofenceId;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = ((EventPreferencesLocation)fromEvent._eventPreferencesLocation)._enabled;
        this._geofenceId = ((EventPreferencesLocation)fromEvent._eventPreferencesLocation)._geofenceId;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Editor editor = preferences.edit();
            editor.putBoolean(PREF_EVENT_LOCATION_ENABLED, _enabled);
            editor.putLong(PREF_EVENT_LOCATION_GEOFENCE_ID, this._geofenceId);
            editor.commit();
        //}
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this._enabled = preferences.getBoolean(PREF_EVENT_LOCATION_ENABLED, false);
            this._geofenceId = preferences.getLong(PREF_EVENT_LOCATION_GEOFENCE_ID, 0);
        //}
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, SharedPreferences preferences, Context context)
    {
        String descr = "";

        EventPreferencesLocation tmp = new EventPreferencesLocation(this._event, this._enabled, this._geofenceId);
        if (preferences != null)
            tmp.saveSharedPreferences(preferences);

        if (!tmp._enabled)
        {
            ;
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_locations) + ": " + "</b>";
            }

            String selectedLocation = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            if (tmp._geofenceId != 0) {
                selectedLocation = getGeofenceName(tmp._geofenceId, context);
            }
            descr = descr + selectedLocation;
        }

        return descr;
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (key.equals(PREF_EVENT_LOCATION_GEOFENCE_ID)) {
                Preference preference = prefMng.findPreference(key);
                long lValue;
                if (!value.isEmpty())
                    lValue = Long.valueOf(value);
                else
                    lValue = 0;
                preference.setSummary(getGeofenceName(lValue, context));
                GUIData.setPreferenceTitleStyle(preference, false, true, false);
            }
        //}
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_LOCATION_GEOFENCE_ID))
        {
            setSummary(prefMng, key, String.valueOf(preferences.getLong(key, 0)), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_LOCATION_GEOFENCE_ID, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (key.isEmpty() ||
                key.equals(PREF_EVENT_LOCATION_ENABLED)) {
            boolean preferenceChanged = false;
            if (preferences == null) {
                preferenceChanged = this._enabled;
            } else {
                preferenceChanged = preferences.getBoolean(PREF_EVENT_LOCATION_ENABLED, false);
            }
            boolean bold = preferenceChanged;
            Preference preference = prefMng.findPreference(PREF_EVENT_LOCATION_CATEGORY);
            if (preference != null) {
                GUIData.setPreferenceTitleStyle(preference, bold, false, !isRunable());
                preference.setSummary(Html.fromHtml(getPreferencesDescription(false, preferences, context)));
            }
        }
    }

    @Override
    public boolean isRunable()
    {

        boolean runable = super.isRunable();

        runable = runable && (_geofenceId != 0);

        return runable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final boolean enabled = GlobalData.isLocationEnabled(context.getApplicationContext());
            Preference geofencePreference = prefMng.findPreference(PREF_EVENT_LOCATION_GEOFENCE_ID);
        geofencePreference.setEnabled(enabled);
        //}
    }

    @Override
    public boolean activateReturnProfile()
    {
        return true;
    }

    @Override
    public void setSystemRunningEvent(Context context)
    {
    }

    @Override
    public void setSystemPauseEvent(Context context)
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
