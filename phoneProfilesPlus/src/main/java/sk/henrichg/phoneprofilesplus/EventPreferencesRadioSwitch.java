package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
//import android.preference.CheckBoxPreference;
//import android.preference.ListPreference;
//import android.preference.Preference;
//import android.preference.PreferenceManager;

class EventPreferencesRadioSwitch extends EventPreferences {

    int _wifi;
    int _bluetooth;
    int _mobileData;
    int _gps;
    int _nfc;
    int _airplaneMode;

    /*
    static final int RADIO_TYPE_WIFI = 1;
    static final int RADIO_TYPE_BLUETOOTH = 2;
    static final int RADIO_TYPE_MOBILE_DATA = 3;
    static final int RADIO_TYPE_GPS = 4;
    static final int RADIO_TYPE_NFC = 5;
    static final int RADIO_TYPE_AIRPLANE_MODE = 6;
    */

    static final String PREF_EVENT_RADIO_SWITCH_ENABLED = "eventRadioSwitchEnabled";
    private static final String PREF_EVENT_RADIO_SWITCH_WIFI = "eventRadioSwitchWifi";
    private static final String PREF_EVENT_RADIO_SWITCH_BLUETOOTH = "eventRadioSwitchBluetooth";
    private static final String PREF_EVENT_RADIO_SWITCH_MOBILE_DATA = "eventRadioSwitchMobileData";
    private static final String PREF_EVENT_RADIO_SWITCH_GPS = "eventRadioSwitchGPS";
    private static final String PREF_EVENT_RADIO_SWITCH_NFC = "eventRadioSwitchNFC";
    private static final String PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE = "eventRadioSwitchAirplaneMode";

    private static final String PREF_EVENT_RADIO_SWITCH_CATEGORY = "eventRadioSwitchCategoryRoot";

    EventPreferencesRadioSwitch(Event event,
                                boolean enabled,
                                int wifi,
                                int bluetooth,
                                int mobileData,
                                int gps,
                                int nfc,
                                int airplaneMode)
    {
        super(event, enabled);

        this._wifi = wifi;
        this._bluetooth = bluetooth;
        this._mobileData = mobileData;
        this._gps = gps;
        this._nfc = nfc;
        this._airplaneMode = airplaneMode;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesRadioSwitch._enabled;
        this._wifi = fromEvent._eventPreferencesRadioSwitch._wifi;
        this._bluetooth = fromEvent._eventPreferencesRadioSwitch._bluetooth;
        this._mobileData = fromEvent._eventPreferencesRadioSwitch._mobileData;
        this._gps = fromEvent._eventPreferencesRadioSwitch._gps;
        this._nfc = fromEvent._eventPreferencesRadioSwitch._nfc;
        this._airplaneMode = fromEvent._eventPreferencesRadioSwitch._airplaneMode;
        this.setSensorPassed(fromEvent._eventPreferencesRadioSwitch.getSensorPassed());
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, _enabled);
        editor.putString(PREF_EVENT_RADIO_SWITCH_WIFI, String.valueOf(this._wifi));
        editor.putString(PREF_EVENT_RADIO_SWITCH_BLUETOOTH, String.valueOf(this._bluetooth));
        editor.putString(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, String.valueOf(this._mobileData));
        editor.putString(PREF_EVENT_RADIO_SWITCH_GPS, String.valueOf(this._gps));
        editor.putString(PREF_EVENT_RADIO_SWITCH_NFC, String.valueOf(this._nfc));
        editor.putString(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE, String.valueOf(this._airplaneMode));
        editor.apply();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, false);
        this._wifi = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_WIFI, "0"));
        this._bluetooth = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_BLUETOOTH, "0"));
        this._mobileData = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, "0"));
        this._gps = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_GPS, "0"));
        this._nfc = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_NFC, "0"));
        this._airplaneMode = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE, "0"));
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_radioSwitch_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_radioSwitch), addPassStatus, DatabaseHandler.ETYPE_RADIO_SWITCH, context);
                    descr = descr + ": </b>";
                }

                boolean _addBullet = false;
                if (this._wifi != 0) {
                    descr = descr + context.getString(R.string.event_preferences_radioSwitch_wifi) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                    descr = descr + "<b>" + fields[this._wifi] + "</b>";
                    _addBullet = true;
                }

                if (this._bluetooth != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.event_preferences_radioSwitch_bluetooth) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                    descr = descr + "<b>" + fields[this._bluetooth] + "</b>";
                    _addBullet = true;
                }

                if (this._mobileData != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.event_preferences_radioSwitch_mobileData) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                    descr = descr + "<b>" + fields[this._mobileData] + "</b>";
                    _addBullet = true;
                }

                if (this._gps != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.event_preferences_radioSwitch_gps) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                    descr = descr + "<b>" + fields[this._gps] + "</b>";
                    _addBullet = true;

                }

                if (this._nfc != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.event_preferences_radioSwitch_nfc) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                    descr = descr + "<b>" + fields[this._nfc] + "</b>";
                    _addBullet = true;
                }

                if (this._airplaneMode != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.event_preferences_radioSwitch_airplaneMode) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                    descr = descr + "<b>" + fields[this._airplaneMode] + "</b>";
                }
            }
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_RADIO_SWITCH_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_RADIO_SWITCH_WIFI) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_BLUETOOTH) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_GPS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_NFC) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesRadioSwitch.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesRadioSwitch.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, false);
        ListPreference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_WIFI);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_BLUETOOTH);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_GPS);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_NFC);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_RADIO_SWITCH_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
        if (key.equals(PREF_EVENT_RADIO_SWITCH_WIFI) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_BLUETOOTH) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_GPS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_NFC) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_WIFI, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_BLUETOOTH, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_GPS, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_NFC, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE, preferences, context);

        /*
        if (Event.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED, context)
                != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            Preference preference = prefMng.findPreference(PREF_EVENT_NFC_ENABLED);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_NFC_NFC_TAGS);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_NFC_DURATION);
            if (preference != null) preference.setEnabled(false);
        }
        */
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesRadioSwitch tmp = new EventPreferencesRadioSwitch(this._event, this._enabled,
                    this._wifi, this._bluetooth, this._mobileData, this._gps, this._nfc, this._airplaneMode);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, false);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_CATEGORY);
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

        runnable = runnable &&
                ((_wifi != 0) || (_bluetooth != 0) || (_mobileData != 0) || (_gps != 0) || (_nfc != 0) || (_airplaneMode != 0));

        return runnable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context)
    {
        boolean enabled = Event.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED;

        Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_WIFI);
        if (preference != null)
            preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_BLUETOOTH);
        if (preference != null)
            preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA);
        if (preference != null)
            preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_GPS);
        if (preference != null)
            preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_NFC);
        if (preference != null)
            preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE);
        if (preference != null)
            preference.setEnabled(enabled);
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
