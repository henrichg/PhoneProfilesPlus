package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesVPN extends EventPreferences {

    boolean _checkVPNConnection;

    static final String PREF_EVENT_VPN_ENABLED = "eventVPNEnabled";
    private static final String PREF_EVENT_VPN_CHECK_CONNECTION = "eventVPNCheckConnection";

    private static final String PREF_EVENT_VPN_CATEGORY = "eventVPNCategoryRoot";

    EventPreferencesVPN(Event event,
                        boolean enabled,
                        boolean checkVPNConnection) {
        super(event, enabled);

        this._checkVPNConnection = checkVPNConnection;
    }

    void copyPreferences(Event fromEvent) {
        this._enabled = fromEvent._eventPreferencesVPN._enabled;
        this._checkVPNConnection = fromEvent._eventPreferencesVPN._checkVPNConnection;
        this.setSensorPassed(fromEvent._eventPreferencesVPN.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences) {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_VPN_ENABLED, _enabled);
        editor.putBoolean(PREF_EVENT_VPN_CHECK_CONNECTION, this._checkVPNConnection);
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences) {
        this._enabled = preferences.getBoolean(PREF_EVENT_VPN_ENABLED, false);
        this._checkVPNConnection = preferences.getBoolean(PREF_EVENT_VPN_CHECK_CONNECTION, false);
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context) {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_vpn_summary);
        } else {
            if (addBullet) {
                descr = descr + "<b>";
                descr = descr + getPassStatusString(context.getString(R.string.event_type_roaming), addPassStatus, DatabaseHandler.ETYPE_VPN, context);
                descr = descr + "</b> ";
            }

            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_VPN_ENABLED, context);
            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (this._checkVPNConnection) {
                    descr = descr + "<b>" + context.getString(R.string.pref_event_vpn_check_connected) + "</b>";
                } else {
                    descr = descr + "<b>" + context.getString(R.string.pref_event_vpn_check_disconnected) + "</b>";
                }
            }
            else {
                descr = descr + context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context);
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_VPN_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        /*Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesVPN.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesVPN.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_VPN_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_VPN_CHECK_ENABLED);
        if (preference != null) {
            boolean bold = prefMng.getSharedPreferences().getBoolean(PREF_EVENT_VPN_CHECK_ENABLED, false);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable);
        }*/
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_VPN_ENABLED) ||
                key.equals(PREF_EVENT_VPN_CHECK_CONNECTION)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true" : "false", context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context) {
        setSummary(prefMng, PREF_EVENT_VPN_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_VPN_CHECK_CONNECTION, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_VPN_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesVPN tmp = new EventPreferencesVPN(this._event, this._enabled, this._checkVPNConnection);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_VPN_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_ROAMING_ENABLED, false);
                boolean runnable = tmp.isRunnable(context) && (tmp.isAccessibilityServiceEnabled(context, false) == 1);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_VPN).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(runnable && permissionGranted));
                if (enabled)
                    preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, context));
            }
        } else {
            Preference preference = prefMng.findPreference(PREF_EVENT_VPN_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context) {

        boolean runnable = super.isRunnable(context);

        //runnable = runnable && (_checkNetwork || _checkData);

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_VPN_ENABLED) != null) {
                setSummary(prefMng, PREF_EVENT_VPN_ENABLED, preferences, context);
                setSummary(prefMng, PREF_EVENT_VPN_CHECK_CONNECTION, preferences, context);
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
            if (Event.isEventPreferenceAllowed(EventPreferencesVPN.PREF_EVENT_VPN_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                if (!eventsHandler.notAllowedVPN) {
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
                //PPApplication.logE("[TEST BATTERY] EventPreferencesVPN.doHandleEvent", "vpn - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_VPN);
            }
        }
    }

}
