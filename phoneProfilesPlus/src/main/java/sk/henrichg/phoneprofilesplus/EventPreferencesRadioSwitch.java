package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesRadioSwitch extends EventPreferences {

    int _wifi;
    int _bluetooth;
    int _simOnOff;
    int _defaultSIMForCalls;
    int _defaultSIMForSMS;
    int _mobileData;
    int _gps;
    int _nfc;
    int _airplaneMode;

    static final String PREF_EVENT_RADIO_SWITCH_ENABLED = "eventRadioSwitchEnabled";
    private static final String PREF_EVENT_RADIO_SWITCH_WIFI = "eventRadioSwitchWifi";
    private static final String PREF_EVENT_RADIO_SWITCH_BLUETOOTH = "eventRadioSwitchBluetooth";
    private static final String PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF = "eventRadioSwitchSIMOnOff";
    private static final String PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS = "eventRadioSwitchDefaultSIMForCalls";
    private static final String PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS = "eventRadioSwitchDefaultSIMForSMS";
    private static final String PREF_EVENT_RADIO_SWITCH_MOBILE_DATA = "eventRadioSwitchMobileData";
    private static final String PREF_EVENT_RADIO_SWITCH_GPS = "eventRadioSwitchGPS";
    private static final String PREF_EVENT_RADIO_SWITCH_NFC = "eventRadioSwitchNFC";
    private static final String PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE = "eventRadioSwitchAirplaneMode";

    private static final String PREF_EVENT_RADIO_SWITCH_CATEGORY = "eventRadioSwitchCategoryRoot";

    EventPreferencesRadioSwitch(Event event,
                                boolean enabled,
                                int wifi,
                                int bluetooth,
                                int simOnOff,
                                int defaultSIMForCalls,
                                int defaultSIMForSMS,
                                int mobileData,
                                int gps,
                                int nfc,
                                int airplaneMode)
    {
        super(event, enabled);

        this._wifi = wifi;
        this._bluetooth = bluetooth;
        this._simOnOff = simOnOff;
        this._defaultSIMForCalls = defaultSIMForCalls;
        this._defaultSIMForSMS = defaultSIMForSMS;
        this._mobileData = mobileData;
        this._gps = gps;
        this._nfc = nfc;
        this._airplaneMode = airplaneMode;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesRadioSwitch._enabled;
        this._wifi = fromEvent._eventPreferencesRadioSwitch._wifi;
        this._bluetooth = fromEvent._eventPreferencesRadioSwitch._bluetooth;
        this._simOnOff = fromEvent._eventPreferencesRadioSwitch._simOnOff;
        this._defaultSIMForCalls = fromEvent._eventPreferencesRadioSwitch._defaultSIMForCalls;
        this._defaultSIMForSMS = fromEvent._eventPreferencesRadioSwitch._defaultSIMForSMS;
        this._mobileData = fromEvent._eventPreferencesRadioSwitch._mobileData;
        this._gps = fromEvent._eventPreferencesRadioSwitch._gps;
        this._nfc = fromEvent._eventPreferencesRadioSwitch._nfc;
        this._airplaneMode = fromEvent._eventPreferencesRadioSwitch._airplaneMode;
        this.setSensorPassed(fromEvent._eventPreferencesRadioSwitch.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, _enabled);
        editor.putString(PREF_EVENT_RADIO_SWITCH_WIFI, String.valueOf(this._wifi));
        editor.putString(PREF_EVENT_RADIO_SWITCH_BLUETOOTH, String.valueOf(this._bluetooth));
        editor.putString(PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF, String.valueOf(this._simOnOff));
        editor.putString(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS, String.valueOf(this._defaultSIMForCalls));
        editor.putString(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS, String.valueOf(this._defaultSIMForSMS));
        editor.putString(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, String.valueOf(this._mobileData));
        editor.putString(PREF_EVENT_RADIO_SWITCH_GPS, String.valueOf(this._gps));
        editor.putString(PREF_EVENT_RADIO_SWITCH_NFC, String.valueOf(this._nfc));
        editor.putString(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE, String.valueOf(this._airplaneMode));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, false);
        this._wifi = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_WIFI, "0"));
        this._bluetooth = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_BLUETOOTH, "0"));
        this._simOnOff = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF, "0"));
        this._defaultSIMForCalls = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS, "0"));
        this._defaultSIMForSMS = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS, "0"));
        this._mobileData = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, "0"));
        this._gps = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_GPS, "0"));
        this._nfc = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_NFC, "0"));
        this._airplaneMode = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE, "0"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
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
                    descr = descr + "</b> ";
                }

                boolean _addBullet = false;
                if (this._wifi != 0) {
                    descr = descr + context.getString(R.string.event_preferences_radioSwitch_wifi) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchWithConnectionArray);
                    descr = descr + "<b>" + fields[this._wifi] + "</b>";
                    _addBullet = true;
                }

                if (this._bluetooth != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.event_preferences_radioSwitch_bluetooth) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchWithConnectionArray);
                    descr = descr + "<b>" + fields[this._bluetooth] + "</b>";
                    _addBullet = true;
                }

                if (Build.VERSION.SDK_INT >= 26) {
                    int phoneCount = 1;
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        phoneCount = telephonyManager.getPhoneCount();
                    }
                    boolean twoSimCards = false;
                    synchronized (PPApplication.simCardsMutext) {
                        if (phoneCount == 2) {
                            twoSimCards = PPApplication.simCardsMutext.sim1Exists &&
                                    PPApplication.simCardsMutext.sim2Exists;
                        }
                    }

                    if (phoneCount > 1) {
                        if (this._simOnOff != 0) {
                            if (_addBullet)
                                descr = descr +  " • ";
                            String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchSIMOnOffArray);
                            descr = descr + context.getString(R.string.event_preferences_radioSwitch_simOnOff) + ": ";
                            descr = descr + "<b>" + fields[this._simOnOff] + "</b>";
                            _addBullet = true;
                        }
                    }

                    if (twoSimCards) {
                        if (this._defaultSIMForCalls != 0) {
                            if (_addBullet)
                                descr = descr +  " • ";
                            descr = descr + context.getString(R.string.event_preferences_radioSwitch_defaultSIMForCalls) + ": ";
                            String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchDefaultSIMArray);
                            descr = descr + "<b>" + fields[this._defaultSIMForCalls] + "</b>";
                            _addBullet = true;
                        }
                        if (this._defaultSIMForSMS != 0) {
                            if (_addBullet)
                                descr = descr +  " • ";
                            descr = descr + context.getString(R.string.event_preferences_radioSwitch_defaultSIMForSMS) + ": ";
                            String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchDefaultSIMArray);
                            descr = descr + "<b>" + fields[this._defaultSIMForSMS] + "</b>";
                            _addBullet = true;
                        }
                    }
                }

                if (this._mobileData != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.event_preferences_radioSwitch_mobileData) + ": ";
                    int phoneCount = 1;
                    if (Build.VERSION.SDK_INT >= 26) {
                        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (telephonyManager != null) {
                            phoneCount = telephonyManager.getPhoneCount();
                        }
                    }
                    String[] fieldArray;
                    String[] fieldValues;
                    if (phoneCount > 1) {
                        fieldArray = context.getResources().getStringArray(R.array.eventRadioSwitchMobileDataDualSIMArray);
                        fieldValues = context.getResources().getStringArray(R.array.eventRadioSwitchhMobileDataDualSIMValues);
                    }
                    else {
                        fieldArray = context.getResources().getStringArray(R.array.eventRadioSwitchWithConnectionArray);
                        fieldValues = context.getResources().getStringArray(R.array.eventRadioSwitchhWithConnectionValues);
                    }
                    int index = -1;
                    for (int i = 0; i < fieldValues.length; i++) {
                        if (fieldValues[i].equals(String.valueOf(this._mobileData))) {
                            index = i;
                            break;
                        }
                    }
                    if (index != -1)
                        descr = descr + "<b>" + fieldArray[index] + "</b>";
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

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_RADIO_SWITCH_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false);
            }
        }

        if (key.equals(PREF_EVENT_RADIO_SWITCH_WIFI) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_BLUETOOTH) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_GPS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_NFC) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE))
        {
            boolean hasHardware = true;
            if (key.equals(PREF_EVENT_RADIO_SWITCH_WIFI) && (!PPApplication.HAS_FEATURE_WIFI)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
                hasHardware = false;
            }
            if (key.equals(PREF_EVENT_RADIO_SWITCH_BLUETOOTH) && (!PPApplication.HAS_FEATURE_BLUETOOTH)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
                hasHardware = false;
            }
            if (key.equals(PREF_EVENT_RADIO_SWITCH_GPS) && (!PPApplication.HAS_FEATURE_LOCATION_GPS)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
                hasHardware = false;
            }
            if (key.equals(PREF_EVENT_RADIO_SWITCH_NFC) && (!PPApplication.HAS_FEATURE_NFC)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
                hasHardware = false;
            }
            if (key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA)) {
                ListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    int phoneCount = 1;
                    if (Build.VERSION.SDK_INT >= 26) {
                        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (telephonyManager != null) {
                            phoneCount = telephonyManager.getPhoneCount();
                        }
                    }
                    String[] fieldArray;
                    String[] fieldValues;
                    if (phoneCount > 1) {
                        fieldArray = context.getResources().getStringArray(R.array.eventRadioSwitchMobileDataDualSIMArray);
                        fieldValues = context.getResources().getStringArray(R.array.eventRadioSwitchhMobileDataDualSIMValues);
                    }
                    else {
                        fieldArray = context.getResources().getStringArray(R.array.eventRadioSwitchWithConnectionArray);
                        fieldValues = context.getResources().getStringArray(R.array.eventRadioSwitchhWithConnectionValues);
                    }
                    int index = -1;
                    for (int i = 0; i < fieldValues.length; i++) {
                        if (fieldValues[i].equals(value)) {
                            index = i;
                            break;
                        }
                    }
                    CharSequence summary = (index >= 0) ? fieldArray[index] : null;
                    listPreference.setSummary(summary);
                }
                hasHardware = false; // do not use default change of summary
            }
            if (hasHardware) {
                ListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(value);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                }
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesRadioSwitch.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesRadioSwitch.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, false);
        ListPreference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_WIFI);
        if (preference != null) {
            if (!PPApplication.HAS_FEATURE_WIFI)
                enabled = false;
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_BLUETOOTH);
        if (preference != null) {
            if (!PPApplication.HAS_FEATURE_BLUETOOTH)
                enabled = false;
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_GPS);
        if (preference != null) {
            if (!PPApplication.HAS_FEATURE_LOCATION_GPS)
                enabled = false;
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_NFC);
        if (preference != null) {
            if (!PPApplication.HAS_FEATURE_NFC)
                enabled = false;
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_RADIO_SWITCH_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
        if (key.equals(PREF_EVENT_RADIO_SWITCH_WIFI) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_BLUETOOTH) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_GPS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_NFC) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_WIFI, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_BLUETOOTH, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS, preferences, context);
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

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesRadioSwitch tmp = new EventPreferencesRadioSwitch(this._event, this._enabled,
                    this._wifi, this._bluetooth, this._simOnOff, this._defaultSIMForCalls, this._defaultSIMForSMS,
                    this._mobileData, this._gps, this._nfc, this._airplaneMode);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_RADIO_SWITCH).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !(tmp.isRunnable(context) && permissionGranted));
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_CATEGORY);
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

        if (Build.VERSION.SDK_INT < 26)
            runnable = runnable &&
                    ((_wifi != 0) || (_bluetooth != 0) || (_mobileData != 0) || (_gps != 0) ||
                     (_nfc != 0) || (_airplaneMode != 0));
        else {
            boolean ok = false;
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                int phoneCount = telephonyManager.getPhoneCount();
                if (phoneCount > 1) {
                    boolean twoSimCards = false;
                    synchronized (PPApplication.simCardsMutext) {
                        if (phoneCount == 2) {
                            twoSimCards = PPApplication.simCardsMutext.sim1Exists &&
                                    PPApplication.simCardsMutext.sim2Exists;
                        }
                    }
                    if (twoSimCards)
                        runnable = runnable &&
                                ((_wifi != 0) || (_bluetooth != 0) || (_mobileData != 0) || (_gps != 0) ||
                                 (_nfc != 0) || (_airplaneMode != 0) || (_defaultSIMForCalls != 0) || (_defaultSIMForSMS != 0) ||
                                 (_simOnOff != 0));
                    else
                        runnable = runnable &&
                                ((_wifi != 0) || (_bluetooth != 0) || (_mobileData != 0) || (_gps != 0) ||
                                        (_nfc != 0) || (_airplaneMode != 0) || (_simOnOff != 0));
                    ok = true;
                }
            }
            if (!ok)
                runnable = runnable &&
                        ((_wifi != 0) || (_bluetooth != 0) || (_mobileData != 0) || (_gps != 0) ||
                         (_nfc != 0) || (_airplaneMode != 0));
        }

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, Context context)
    {
        boolean enabled = Event.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED;

        Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_WIFI);
        if (preference != null)
            preference.setEnabled(enabled && PPApplication.HAS_FEATURE_WIFI);
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_BLUETOOTH);
        if (preference != null)
            preference.setEnabled(enabled && PPApplication.HAS_FEATURE_BLUETOOTH);

        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS);
        if (preference != null) {
            if (Build.VERSION.SDK_INT >= 26) {
                int phoneCount = 1;
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    phoneCount = telephonyManager.getPhoneCount();
                }
                if (phoneCount > 1) {
                    synchronized (PPApplication.simCardsMutext) {
                        if (phoneCount == 2) {
                            boolean twoSimCards = PPApplication.simCardsMutext.sim1Exists &&
                                    PPApplication.simCardsMutext.sim2Exists;
                            preference.setVisible(twoSimCards);
                            if (twoSimCards)
                                preference.setEnabled(enabled);
                        }
                    }
                } else
                    preference.setVisible(false);
            } else
                preference.setVisible(false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS);
        if (preference != null) {
            if (Build.VERSION.SDK_INT >= 26) {
                int phoneCount = 1;
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    phoneCount = telephonyManager.getPhoneCount();
                }
                if (phoneCount > 1) {
                    synchronized (PPApplication.simCardsMutext) {
                        if (phoneCount == 2) {
                            boolean twoSimCards = PPApplication.simCardsMutext.sim1Exists &&
                                    PPApplication.simCardsMutext.sim2Exists;
                            preference.setVisible(twoSimCards);
                            if (twoSimCards)
                                preference.setEnabled(enabled);
                        }
                    }
                } else
                    preference.setVisible(false);
            } else
                preference.setVisible(false);
        }

        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA);
        if (preference != null)
            preference.setEnabled(enabled);
        if (Build.VERSION.SDK_INT >= 26) {
            int phoneCount = 1;
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                phoneCount = telephonyManager.getPhoneCount();
            }
            SharedPreferences preferences = prefMng.getSharedPreferences();
            ListPreference listPreference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA);
            if (phoneCount > 1) {
                if (listPreference != null) {
                    String value = listPreference.getValue();
                    listPreference.setEntries(R.array.eventRadioSwitchMobileDataDualSIMArray);
                    listPreference.setEntryValues(R.array.eventRadioSwitchhMobileDataDualSIMValues);
                    listPreference.setValue(value);
                    setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, preferences, context);
                }
            } else {
                if (listPreference != null) {
                    String value = listPreference.getValue();
                    listPreference.setEntries(R.array.eventRadioSwitchWithConnectionArray);
                    listPreference.setEntryValues(R.array.eventRadioSwitchhWithConnectionValues);
                    if (value.equals("5") || value.equals("6"))
                        value = "3";
                    listPreference.setValue(value);
                    setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, preferences, context);
                }
            }
        }
        ListPreference listPreference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF);
        if (listPreference != null) {
            if (Build.VERSION.SDK_INT >= 26) {
                listPreference.setEnabled(enabled);
                int phoneCount = 1;
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    phoneCount = telephonyManager.getPhoneCount();
                }
                SharedPreferences preferences = prefMng.getSharedPreferences();
                if (phoneCount <= 1) {
                    String value = listPreference.getValue();
                    if (value.equals("3") || value.equals("5"))
                        value = "1";
                    if (value.equals("4") || value.equals("6"))
                        value = "2";
                    listPreference.setValue(value);
                    setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF, preferences, context);
                }
            } else
                listPreference.setVisible(false);
        }

        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_GPS);
        if (preference != null)
            preference.setEnabled(enabled && PPApplication.HAS_FEATURE_LOCATION_GPS);
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_NFC);
        if (preference != null)
            preference.setEnabled(enabled && PPApplication.HAS_FEATURE_NFC);
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE);
        if (preference != null)
            preference.setEnabled(enabled);
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
            if ((Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                eventsHandler.radioSwitchPassed = true;
                boolean tested = false;

                if ((_wifi != 0) && PPApplication.HAS_FEATURE_WIFI) {

                    if (!(ApplicationPreferences.prefEventWifiScanRequest ||
                            ApplicationPreferences.prefEventWifiWaitForResult ||
                            ApplicationPreferences.prefEventWifiEnabledForScan)) {
                        // ignore for wifi scanning

                        WifiManager wifiManager = (WifiManager) eventsHandler.context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (wifiManager != null) {
                            int wifiState = wifiManager.getWifiState();
                            boolean enabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
//                            PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "wifi enabled=" + enabled);

                            boolean connected = false;
                            ConnectivityManager connManager = null;
                            try {
                                connManager = (ConnectivityManager) eventsHandler.context.getSystemService(Context.CONNECTIVITY_SERVICE);
                            } catch (Exception e) {
                                // java.lang.NullPointerException: missing IConnectivityManager
                                // Dual SIM?? Bug in Android ???
                                PPApplication.recordException(e);
                            }
                            if (connManager != null) {
                                Network[] networks = connManager.getAllNetworks();
                                if ((networks != null) && (networks.length > 0)) {
                                    for (Network network : networks) {
                                        try {
                                            NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                                            if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                                connected = WifiNetworkCallback.connected;
//                                                PPApplication.logE("[CONNECTIVITY_TEST] EventPreferencesRadioSwitch.doHandleEvent", "connected="+connected);
                                                break;
                                            }
                                        } catch (Exception e) {
//                                            Log.e("EventPreferencesWifi.doHandleEvent", Log.getStackTraceString(e));
                                            PPApplication.recordException(e);
                                        }
                                    }
                                }
                            }
//                            PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "wifi connected=" + connected);

                            tested = true;
                            if (_wifi == 1)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && enabled;
                            else
                            if (_wifi == 2)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (!enabled);
                            else
                            if (_wifi == 3)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && connected;
                            else
                            if (_wifi == 4)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (!connected);
                        }
                        else
                            eventsHandler.notAllowedRadioSwitch = true;
                    } else
                        eventsHandler.notAllowedRadioSwitch = true;
                }

                if ((_bluetooth != 0) && PPApplication.HAS_FEATURE_BLUETOOTH) {

                    if (!(ApplicationPreferences.prefEventBluetoothScanRequest ||
                            ApplicationPreferences.prefEventBluetoothLEScanRequest ||
                            ApplicationPreferences.prefEventBluetoothWaitForResult ||
                            ApplicationPreferences.prefEventBluetoothLEWaitForResult ||
                            ApplicationPreferences.prefEventBluetoothEnabledForScan)) {
                        // ignore for bluetooth scanning


                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
                        if (bluetoothAdapter != null) {
                            boolean enabled = bluetoothAdapter.isEnabled();
//                            PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "bluetooth enabled=" + enabled);

                            BluetoothConnectionBroadcastReceiver.getConnectedDevices(eventsHandler.context);
                            boolean connected = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(null, "");
//                            PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "bluetooth connected=" + connected);

                            tested = true;
                            if (_bluetooth == 1)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && enabled;
                            else
                            if (_bluetooth == 2)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (!enabled);
                            else
                            if (_bluetooth == 3)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && connected;
                            else
                            if (_bluetooth == 4)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (!connected);
                        }
                    } else
                        eventsHandler.notAllowedRadioSwitch = true;
                }

                if ((_mobileData != 0) && PPApplication.HAS_FEATURE_TELEPHONY) {
                    boolean enabled = ActivateProfileHelper.isMobileData(eventsHandler.context, 0);
//                    PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "mobileData enabled=" + enabled);

                    boolean connected = false;
                    ConnectivityManager connManager = null;
                    try {
                        connManager = (ConnectivityManager) eventsHandler.context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    } catch (Exception e) {
                        // java.lang.NullPointerException: missing IConnectivityManager
                        // Dual SIM?? Bug in Android ???
                        PPApplication.recordException(e);
                    }
                    if (connManager != null) {
//                        PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "connManager != null");
                        Network[] networks = connManager.getAllNetworks();
                        if ((networks != null) && (networks.length > 0)) {
//                            PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "networks.length > 0");
                            for (Network network : networks) {
                                try {
                                    NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                                    if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                                        PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "(NetworkCapabilities.TRANSPORT_CELLULAR");
                                        connected = MobileDataNetworkCallback.connected;
//                                        PPApplication.logE("[CONNECTIVITY_TEST] EventPreferencesRadioSwitch.doHandleEvent", "connected="+connected);
                                        break;
                                    }
                                } catch (Exception e) {
//                                    Log.e("EventPreferencesWifi.doHandleEvent", Log.getStackTraceString(e));
                                    PPApplication.recordException(e);
                                }
                            }
                        }
                    }
//                    PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "mobileData connected=" + connected);

                    tested = true;
                    if (_mobileData == 1)
                        eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && enabled;
                    else
                    if (_mobileData == 2)
                        eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (!enabled);
                    else
                    if (_mobileData == 3)
                        eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && connected;
                    else
                    if (_mobileData == 4)
                        eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (!connected);
                    else
                    if ((_mobileData == 5) || (_mobileData == 6)) {
                        if (Build.VERSION.SDK_INT >= 26) {
                            int phoneCount = 1;
                            TelephonyManager telephonyManager = (TelephonyManager) eventsHandler.context.getSystemService(Context.TELEPHONY_SERVICE);
                            if (telephonyManager != null) {
                                phoneCount = telephonyManager.getPhoneCount();
                            }
                            boolean twoSimCards = false;
                            synchronized (PPApplication.simCardsMutext) {
                                if (phoneCount == 2) {
                                    twoSimCards = PPApplication.simCardsMutext.sim1Exists &&
                                                    PPApplication.simCardsMutext.sim2Exists;
                                }
                            }

                            if (phoneCount > 1) {
                                if (twoSimCards) {
                                    if (connected) {
                                        if (Permissions.checkPhone(eventsHandler.context.getApplicationContext())) {
                                            int defaultSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
                                            int defaultSIM = PPApplication.getSIMCardFromSubscriptionId(eventsHandler.context, defaultSubscriptionId);
//                                        PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "defaultSubscriptionId=" + defaultSubscriptionId);
                                            if (_mobileData == 5)
                                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (defaultSIM == 1);
                                            else
                                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (defaultSIM == 2);
                                        } else
                                            eventsHandler.radioSwitchPassed = false;
                                    } else
                                        eventsHandler.radioSwitchPassed = false;
                                } else
                                    // only one sim card is inserted
                                    eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && connected;
                            }
                            else
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && connected;
                        }
                    }
                }

                if ((_gps != 0) && PPApplication.HAS_FEATURE_LOCATION_GPS) {

                    boolean enabled;
                    /*if (android.os.Build.VERSION.SDK_INT < 19)
                        enabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
                    else {*/
                    LocationManager locationManager = (LocationManager) eventsHandler.context.getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager != null) {
                        enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        //}
                        //PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "gpsState=" + enabled);
                        tested = true;
                        if (_gps == 1)
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && enabled;
                        else
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && !enabled;
                    }
                    else
                        eventsHandler.notAllowedRadioSwitch = true;
                }

                if ((_nfc != 0) && PPApplication.HAS_FEATURE_NFC) {

                    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(eventsHandler.context);
                    if (nfcAdapter != null) {
                        boolean enabled = nfcAdapter.isEnabled();
                        //PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "nfcState=" + enabled);
                        tested = true;
                        if (_nfc == 1)
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && enabled;
                        else
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && !enabled;
                    }
                }

                if (_airplaneMode != 0) {

                    boolean enabled = ActivateProfileHelper.isAirplaneMode(eventsHandler.context);
                    //PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "airplaneModeState=" + enabled);
                    tested = true;
                    if (_airplaneMode == 1)
                        eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && enabled;
                    else
                        eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && !enabled;
                }

                if ((_defaultSIMForCalls != 0) || (_defaultSIMForSMS != 0)) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        int phoneCount = 1;
                        TelephonyManager telephonyManager = (TelephonyManager) eventsHandler.context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (telephonyManager != null) {
                            phoneCount = telephonyManager.getPhoneCount();
                        }
                        boolean twoSimCards = false;
                        synchronized (PPApplication.simCardsMutext) {
                            if (phoneCount == 2) {
                                twoSimCards = PPApplication.simCardsMutext.sim1Exists &&
                                        PPApplication.simCardsMutext.sim2Exists;
                            }
                        }

                        if ((phoneCount > 1) && twoSimCards) {
                            tested = true;
                            if (Permissions.checkPhone(eventsHandler.context.getApplicationContext())) {
                                if (_defaultSIMForCalls != 0) {
                                    int defaultSubscriptionId = SubscriptionManager.getDefaultSubscriptionId();
                                    int simCard = PPApplication.getSIMCardFromSubscriptionId(eventsHandler.context, defaultSubscriptionId);
                                    int configuredSIMCard = 1;
                                    if (_defaultSIMForCalls == 2)
                                        configuredSIMCard = 2;
                                    eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                            (simCard == configuredSIMCard);
                                }
                                if (_defaultSIMForSMS != 0) {
                                    int defaultSubscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId();
                                    int simCard = PPApplication.getSIMCardFromSubscriptionId(eventsHandler.context, defaultSubscriptionId);
                                    int configuredSIMCard = 1;
                                    if (_defaultSIMForSMS == 2)
                                        configuredSIMCard = 2;
                                    eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                            (simCard == configuredSIMCard);
                                }
                            }
                            else
                                eventsHandler.radioSwitchPassed = false;
                        }
                    }
                }

                if (_simOnOff != 0) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        tested = true;
                        synchronized (PPApplication.simCardsMutext) {
                            switch (_simOnOff) {
                                case 1:
                                    eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                            (PPApplication.simCardsMutext.sim0Exists ||
                                             PPApplication.simCardsMutext.sim1Exists ||
                                             PPApplication.simCardsMutext.sim2Exists);
                                    break;
                                case 2:
                                    eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                            ((!PPApplication.simCardsMutext.sim0Exists) ||
                                             (!PPApplication.simCardsMutext.sim1Exists) ||
                                             (!PPApplication.simCardsMutext.sim2Exists));
                                    break;
                                case 3:
                                    eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                                    (PPApplication.simCardsMutext.sim1Exists);
                                    break;
                                case 4:
                                    eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                            (!PPApplication.simCardsMutext.sim1Exists);
                                    break;
                                case 5:
                                    eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                            (PPApplication.simCardsMutext.sim2Exists);
                                    break;
                                case 6:
                                    eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                            (!PPApplication.simCardsMutext.sim2Exists);
                                    break;
                            }
                        }
                    }
                }

                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && tested;

                if (!eventsHandler.notAllowedRadioSwitch) {
                    if (eventsHandler.radioSwitchPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedRadioSwitch = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                //PPApplication.logE("[TEST BATTERY] EventPreferencesRadioSwitch.doHandleEvent", "radio switch - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_RADIO_SWITCH);
            }
        }
    }

}
