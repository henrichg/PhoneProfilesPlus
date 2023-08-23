package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesVPN extends EventPreferences {

    int _connectionStatus;

    static final String PREF_EVENT_VPN_ENABLED = "eventVPNEnabled";
    private static final String PREF_EVENT_VPN_CONNECTION_STATUS = "eventVPNConnectionStatus";

    private static final String PREF_EVENT_VPN_CATEGORY = "eventVPNCategoryRoot";

    EventPreferencesVPN(Event event,
                        boolean enabled,
                        int connectionStatus) {
        super(event, enabled);

        this._connectionStatus = connectionStatus;
    }

    void copyPreferences(Event fromEvent) {
        this._enabled = fromEvent._eventPreferencesVPN._enabled;
        this._connectionStatus = fromEvent._eventPreferencesVPN._connectionStatus;
        this.setSensorPassed(fromEvent._eventPreferencesVPN.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences) {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_VPN_ENABLED, _enabled);
        editor.putString(PREF_EVENT_VPN_CONNECTION_STATUS, String.valueOf(this._connectionStatus));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences) {
        this._enabled = preferences.getBoolean(PREF_EVENT_VPN_ENABLED, false);
        this._connectionStatus = Integer.parseInt(preferences.getString(PREF_EVENT_VPN_CONNECTION_STATUS, "0"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_vpn_summary));
        } else {
            if (addBullet) {
                _value.append(StringConstants.TAG_BOLD_START_HTML);
                _value.append(getPassStatusString(context.getString(R.string.event_type_vpn), addPassStatus, DatabaseHandler.ETYPE_VPN, context));
                _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
            }

            PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_VPN_ENABLED, context);
            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                _value.append(context.getString(R.string.pref_event_vpn_connection_status)).append(StringConstants.STR_COLON_WITH_SPACE);
                String[] fields = context.getResources().getStringArray(R.array.eventVPNArray);
                _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[this._connectionStatus], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);

            }
            else {
                _value.append(context.getString(R.string.profile_preferences_device_not_allowed)).append(StringConstants.STR_COLON_WITH_SPACE).append(preferenceAllowed.getNotAllowedPreferenceReasonString(context));
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_VPN_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_VPN_CONNECTION_STATUS))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesVPN.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesVPN.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_VPN_ENABLED, false);
        PPListPreference preference = prefMng.findPreference(PREF_EVENT_VPN_CONNECTION_STATUS);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, false, true, !isRunnable, false);
        }

    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_VPN_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }

        if (key.equals(PREF_EVENT_VPN_CONNECTION_STATUS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context) {
        setSummary(prefMng, PREF_EVENT_VPN_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_VPN_CONNECTION_STATUS, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_VPN_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesVPN tmp = new EventPreferencesVPN(this._event, this._enabled, this._connectionStatus);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_VPN_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_ROAMING_ENABLED, false);
                boolean runnable = tmp.isRunnable(context) && (tmp.isAccessibilityServiceEnabled(context, false) == 1);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_VPN).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(runnable && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false, false, false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        } else {
            Preference preference = prefMng.findPreference(PREF_EVENT_VPN_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                        StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context) {

        //boolean runnable = super.isRunnable(context);
        //runnable = runnable && (_checkNetwork || _checkData);
        //return runnable;
        return super.isRunnable(context);
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_VPN_ENABLED) != null) {
                setSummary(prefMng, PREF_EVENT_VPN_ENABLED, preferences, context);
                setSummary(prefMng, PREF_EVENT_VPN_CONNECTION_STATUS, preferences, context);
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

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if (EventStatic.isEventPreferenceAllowed(EventPreferencesVPN.PREF_EVENT_VPN_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                if (!eventsHandler.notAllowedVPN) {
                    if (_connectionStatus == 0)
                        eventsHandler.vpnPassed = PPApplication.vpnNetworkConnected;
                    else
                    if (_connectionStatus == 1)
                        eventsHandler.vpnPassed = !PPApplication.vpnNetworkConnected;
                    else
                        eventsHandler.vpnPassed = false;

                    if (eventsHandler.vpnPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            }
            else
                eventsHandler.notAllowedVPN = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_VPN);
            }
        }
    }

}
