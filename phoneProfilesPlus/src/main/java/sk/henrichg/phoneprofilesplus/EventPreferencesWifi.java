package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Html;

import java.util.Arrays;

public class EventPreferencesWifi extends EventPreferences {

    public String _SSID;
    public int _connectionType;

    static final int CTYPE_CONNECTED = 0;
    static final int CTYPE_INFRONT = 1;
    static final int CTYPE_NOTCONNECTED = 2;
    static final int CTYPE_NOTINFRONT = 3;

    static final String PREF_EVENT_WIFI_ENABLE_SCANNING_APP_SETTINGS = "eventEnableWiFiScaningAppSettings";
    static final String PREF_EVENT_WIFI_ENABLED = "eventWiFiEnabled";
    static final String PREF_EVENT_WIFI_SSID = "eventWiFiSSID";
    static final String PREF_EVENT_WIFI_CONNECTION_TYPE = "eventWiFiConnectionType";

    static final String PREF_EVENT_WIFI_CATEGORY = "eventWifiCategory";

    public EventPreferencesWifi(Event event,
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
        this._enabled = ((EventPreferencesWifi)fromEvent._eventPreferencesWifi)._enabled;
        this._SSID = ((EventPreferencesWifi)fromEvent._eventPreferencesWifi)._SSID;
        this._connectionType = ((EventPreferencesWifi)fromEvent._eventPreferencesWifi)._connectionType;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_WIFI_ENABLED, _enabled);
        editor.putString(PREF_EVENT_WIFI_SSID, this._SSID);
        editor.putString(PREF_EVENT_WIFI_CONNECTION_TYPE, String.valueOf(this._connectionType));
        editor.commit();
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

        if (!this._enabled)
        {
            //descr = descr + context.getString(R.string.event_type_wifi) + ": ";
            //descr = descr + context.getString(R.string.event_preferences_not_enabled);
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
            descr = descr + context.getString(R.string.pref_event_wifi_ssid);
            descr = descr + ": " + this._SSID;
        }

        return descr;
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_WIFI_ENABLE_SCANNING_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.menu_settings) + ": " +
                        context.getResources().getString(R.string.phone_profiles_pref_applicationEventWifiEnableWifi));
            }
        }
        if (key.equals(PREF_EVENT_WIFI_SSID))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);
                GUIData.setPreferenceTitleStyle(preference, false, true, false);
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
        setSummary(prefMng, PREF_EVENT_WIFI_ENABLE_SCANNING_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_WIFI_SSID, preferences, context);
        setSummary(prefMng, PREF_EVENT_WIFI_CONNECTION_TYPE, preferences, context);

        if (GlobalData.isPreferenceAllowed(GlobalData.PREF_PROFILE_DEVICE_WIFI, context)
                != GlobalData.PREFERENCE_ALLOWED)
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
        EventPreferencesWifi tmp = new EventPreferencesWifi(this._event, this._enabled, this._SSID, this._connectionType);
        if (preferences != null)
            tmp.saveSharedPreferences(preferences);

        Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_CATEGORY);
        if (preference != null) {
            GUIData.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunable());
            preference.setSummary(Html.fromHtml(tmp.getPreferencesDescription(false, context)));
        }
    }

    @Override
    public boolean isRunable()
    {
        return super.isRunable() && (!this._SSID.isEmpty());
    }

    @Override
    public boolean activateReturnProfile()
    {
        return true;
    }

    @Override
    public void setSystemEventForStart(Context context)
    {
        if (((_connectionType == CTYPE_INFRONT) || (_connectionType == CTYPE_NOTINFRONT)) &&
            (!WifiScanAlarmBroadcastReceiver.isAlarmSet(context/*, false*/)))
            WifiScanAlarmBroadcastReceiver.setAlarm(context, true, false);
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
        /*
        if (((_connectionType == CTYPE_INFRONT) || (_connectionType == CTYPE_NOTINFRONT)) &&
            (!WifiScanAlarmBroadcastReceiver.isAlarmSet(context, false)))
            WifiScanAlarmBroadcastReceiver.setAlarm(context, false, true);
        */
    }

    @Override
    public void removeSystemEvent(Context context)
    {
    }

}
