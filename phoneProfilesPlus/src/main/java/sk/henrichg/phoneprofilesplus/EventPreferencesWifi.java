package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

import java.util.Arrays;

class EventPreferencesWifi extends EventPreferences {

    String _SSID;
    int _connectionType;

    static final int CTYPE_CONNECTED = 0;
    static final int CTYPE_INFRONT = 1;
    static final int CTYPE_NOTCONNECTED = 2;
    static final int CTYPE_NOTINFRONT = 3;

    //static final String PREF_EVENT_WIFI_ENABLE_SCANNING_APP_SETTINGS = "eventEnableWiFiScaningAppSettings";
    static final String PREF_EVENT_WIFI_ENABLED = "eventWiFiEnabled";
    private static final String PREF_EVENT_WIFI_SSID = "eventWiFiSSID";
    private static final String PREF_EVENT_WIFI_CONNECTION_TYPE = "eventWiFiConnectionType";

    private static final String PREF_EVENT_WIFI_CATEGORY = "eventWifiCategory";

    static final String CONFIGURED_SSIDS_VALUE = "^configured_ssids^";
    static final String ALL_SSIDS_VALUE = "%";

    EventPreferencesWifi(Event event,
                                    boolean enabled,
                                    String SSID,
                                    int connectionType)
    {
        super(event, enabled);

        this._SSID = SSID;
        this._connectionType = connectionType;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesWifi._enabled;
        this._SSID = fromEvent._eventPreferencesWifi._SSID;
        this._connectionType = fromEvent._eventPreferencesWifi._connectionType;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_WIFI_ENABLED, _enabled);
        editor.putString(PREF_EVENT_WIFI_SSID, this._SSID);
        editor.putString(PREF_EVENT_WIFI_CONNECTION_TYPE, String.valueOf(this._connectionType));
        editor.apply();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_WIFI_ENABLED, false);
        this._SSID = preferences.getString(PREF_EVENT_WIFI_SSID, "");
        this._connectionType = Integer.parseInt(preferences.getString(PREF_EVENT_WIFI_CONNECTION_TYPE, "1"));
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_wifi_summary);
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_wifi) + ": " + "</b>";
            }

            descr = descr + context.getString(R.string.pref_event_wifi_connectionType);
            String[] connectionListTypeNames = context.getResources().getStringArray(R.array.eventWifiConnectionTypeArray);
            String[] connectionListTypes = context.getResources().getStringArray(R.array.eventWifiConnectionTypeValues);
            int index = Arrays.asList(connectionListTypes).indexOf(Integer.toString(this._connectionType));
            descr = descr + ": " + connectionListTypeNames[index] + "; ";

            String selectedSSIDs = context.getString(R.string.pref_event_wifi_ssid) + ": ";
            String[] splits = this._SSID.split("\\|");
            for (String _ssid : splits) {
                if (_ssid.isEmpty()) {
                    selectedSSIDs = selectedSSIDs + context.getString(R.string.applications_multiselect_summary_text_not_selected);
                }
                else
                if (splits.length == 1) {
                    if (_ssid.equals(ALL_SSIDS_VALUE))
                        selectedSSIDs = selectedSSIDs + context.getString(R.string.wifi_ssid_pref_dlg_all_ssids_chb);
                    else
                    if (_ssid.equals(CONFIGURED_SSIDS_VALUE))
                        selectedSSIDs = selectedSSIDs + context.getString(R.string.wifi_ssid_pref_dlg_configured_ssids_chb);
                    else
                        selectedSSIDs = selectedSSIDs + _ssid;
                }
                else {
                    selectedSSIDs = context.getString(R.string.applications_multiselect_summary_text_selected);
                    selectedSSIDs = selectedSSIDs + " " + splits.length;
                    break;
                }
            }
            descr = descr + selectedSSIDs;
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        /*if (key.equals(PREF_EVENT_WIFI_ENABLE_SCANNING_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.menu_settings) + ": " +
                        context.getResources().getString(R.string.phone_profiles_pref_applicationEventWifiEnableWifi));
            }
        }*/
        if (key.equals(PREF_EVENT_WIFI_SSID))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String[] splits = value.split("\\|");
                for (String _ssid : splits) {
                    if (_ssid.isEmpty()) {
                        preference.setSummary(R.string.applications_multiselect_summary_text_not_selected);
                    }
                    else
                    if (splits.length == 1) {
                        if (_ssid.equals(ALL_SSIDS_VALUE))
                            preference.setSummary(R.string.wifi_ssid_pref_dlg_all_ssids_chb);
                        else
                        if (_ssid.equals(CONFIGURED_SSIDS_VALUE))
                            preference.setSummary(R.string.wifi_ssid_pref_dlg_configured_ssids_chb);
                        else
                            preference.setSummary(_ssid);
                    }
                    else {
                        String selectedSSIDs = context.getString(R.string.applications_multiselect_summary_text_selected);
                        selectedSSIDs = selectedSSIDs + " " + splits.length;
                        preference.setSummary(selectedSSIDs);
                        break;
                    }
                }
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
            }
        }
        if (key.equals(PREF_EVENT_WIFI_CONNECTION_TYPE))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_WIFI_SSID) ||
            key.equals(PREF_EVENT_WIFI_CONNECTION_TYPE))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        //setSummary(prefMng, PREF_EVENT_WIFI_ENABLE_SCANNING_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_WIFI_SSID, preferences, context);
        setSummary(prefMng, PREF_EVENT_WIFI_CONNECTION_TYPE, preferences, context);

        if (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context)
                != PPApplication.PREFERENCE_ALLOWED)
        {
            Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_ENABLED);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_WIFI_SSID);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_WIFI_CONNECTION_TYPE);
            if (preference != null) preference.setEnabled(false);
        }

    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (Event.isEventPreferenceAllowed(PREF_EVENT_WIFI_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED) {
            EventPreferencesWifi tmp = new EventPreferencesWifi(this._event, this._enabled, this._SSID, this._connectionType);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_CATEGORY);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_CATEGORY);
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
        return super.isRunnable(context) && (!this._SSID.isEmpty());
    }

    @Override
    public boolean activateReturnProfile()
    {
        return true;
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
