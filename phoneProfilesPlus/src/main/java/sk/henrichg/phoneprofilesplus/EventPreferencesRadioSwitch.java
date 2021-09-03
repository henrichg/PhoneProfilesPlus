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
import android.telephony.TelephonyManager;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesRadioSwitch extends EventPreferences {

    int _wifi;
    int _bluetooth;
    int _mobileData;
    //int _mobileDataSIM1;
    //int _mobileDataSIM2;
    int _gps;
    int _nfc;
    int _airplaneMode;

    static final String PREF_EVENT_RADIO_SWITCH_ENABLED = "eventRadioSwitchEnabled";
    private static final String PREF_EVENT_RADIO_SWITCH_WIFI = "eventRadioSwitchWifi";
    private static final String PREF_EVENT_RADIO_SWITCH_BLUETOOTH = "eventRadioSwitchBluetooth";
    private static final String PREF_EVENT_RADIO_SWITCH_MOBILE_DATA = "eventRadioSwitchMobileData";
    //private static final String PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM1 = "eventRadioSwitchMobileDataSIM1";
    //private static final String PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM2 = "eventRadioSwitchMobileDataSIM2";
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
                                int airplaneMode/*,
                                int mobileDataSIM1,
                                int mobileDataSIM2*/)
    {
        super(event, enabled);

        this._wifi = wifi;
        this._bluetooth = bluetooth;
        this._mobileData = mobileData;
        this._gps = gps;
        this._nfc = nfc;
        this._airplaneMode = airplaneMode;
        //this._mobileDataSIM1 = mobileDataSIM1;
        //this._mobileDataSIM2 = mobileDataSIM2;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesRadioSwitch._enabled;
        this._wifi = fromEvent._eventPreferencesRadioSwitch._wifi;
        this._bluetooth = fromEvent._eventPreferencesRadioSwitch._bluetooth;
        this._mobileData = fromEvent._eventPreferencesRadioSwitch._mobileData;
        this._gps = fromEvent._eventPreferencesRadioSwitch._gps;
        this._nfc = fromEvent._eventPreferencesRadioSwitch._nfc;
        this._airplaneMode = fromEvent._eventPreferencesRadioSwitch._airplaneMode;
        //this._mobileDataSIM1 = fromEvent._eventPreferencesRadioSwitch._mobileDataSIM1;
        //this._mobileDataSIM2 = fromEvent._eventPreferencesRadioSwitch._mobileDataSIM2;
        this.setSensorPassed(fromEvent._eventPreferencesRadioSwitch.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, _enabled);
        editor.putString(PREF_EVENT_RADIO_SWITCH_WIFI, String.valueOf(this._wifi));
        editor.putString(PREF_EVENT_RADIO_SWITCH_BLUETOOTH, String.valueOf(this._bluetooth));
        editor.putString(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, String.valueOf(this._mobileData));
        editor.putString(PREF_EVENT_RADIO_SWITCH_GPS, String.valueOf(this._gps));
        editor.putString(PREF_EVENT_RADIO_SWITCH_NFC, String.valueOf(this._nfc));
        editor.putString(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE, String.valueOf(this._airplaneMode));
        //editor.putString(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM1, String.valueOf(this._mobileDataSIM1));
        //editor.putString(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM2, String.valueOf(this._mobileDataSIM2));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, false);
        this._wifi = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_WIFI, "0"));
        this._bluetooth = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_BLUETOOTH, "0"));
        this._mobileData = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, "0"));
        this._gps = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_GPS, "0"));
        this._nfc = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_NFC, "0"));
        this._airplaneMode = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE, "0"));
        //this._mobileDataSIM1 = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM1, "0"));
        //this._mobileDataSIM2 = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM2, "0"));
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

                if (this._mobileData != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.event_preferences_radioSwitch_mobileData) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchWithConnectionArray);
                    descr = descr + "<b>" + fields[this._mobileData] + "</b>";
                    _addBullet = true;
                }
/*                if (Build.VERSION.SDK_INT >= 26) {
                    boolean hasSIMCard = false;
                    //if (Build.VERSION.SDK_INT >= 26) {
                        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (telephonyManager != null) {
                            int phoneCount = telephonyManager.getPhoneCount();
                            if (phoneCount > 1) {
                                boolean simExists;
                                synchronized (PPApplication.simCardsMutext) {
                                    simExists = PPApplication.simCardsMutext.simCardsDetected;
                                    simExists = simExists && PPApplication.simCardsMutext.sim1Exists;
                                    simExists = simExists && PPApplication.simCardsMutext.sim2Exists;
                                }
                                hasSIMCard = simExists;
                            }
                        }
                    //}
                    if (hasSIMCard) {
                        if (this._mobileDataSIM1 != 0) {
                            if (_addBullet)
                                descr = descr + " • ";
                            descr = descr + context.getString(R.string.event_preferences_radioSwitch_mobileData_SIM1) + ": ";
                            String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchWithConnectionArray);
                            descr = descr + "<b>" + fields[this._mobileDataSIM1] + "</b>";
                            _addBullet = true;
                        }
                        if (this._mobileDataSIM2 != 0) {
                            if (_addBullet)
                                descr = descr + " • ";
                            descr = descr + context.getString(R.string.event_preferences_radioSwitch_mobileData_SIM2) + ": ";
                            String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchWithConnectionArray);
                            descr = descr + "<b>" + fields[this._mobileDataSIM2] + "</b>";
                            _addBullet = true;
                        }
                    }
                }
 */

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
            boolean hasHardware = true;
            if (key.equals(PREF_EVENT_RADIO_SWITCH_WIFI) && (!PPApplication.HAS_FEATURE_WIFI)) {
                Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_WIFI);
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
                Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_BLUETOOTH);
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
                Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_GPS);
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
                Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_NFC);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
                hasHardware = false;
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

/*        boolean hasFeature = false;
        boolean hasSIMCard = false;
        if (Build.VERSION.SDK_INT >= 26) {
            if (key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM1) ||
                    key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM2))
            {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        hasFeature = true;
                        boolean simExists;
                        synchronized (PPApplication.simCardsMutext) {
                            simExists = PPApplication.simCardsMutext.simCardsDetected;
                            simExists = simExists && PPApplication.simCardsMutext.sim1Exists;
                            simExists = simExists && PPApplication.simCardsMutext.sim2Exists;
                        }
                        hasSIMCard = simExists;
                        ListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            int index = listPreference.findIndexOfValue(value);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);
                        }
                    }
                }
                if (!hasFeature) {
                    Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM1);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                    preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM2);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                }
                else if (!hasSIMCard) {
                    Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM1);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                    preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM2);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                }
            }
        }
 */

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
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_BLUETOOTH);
        if (preference != null) {
            if (!PPApplication.HAS_FEATURE_BLUETOOTH)
                enabled = false;
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
        }
/*        if (Build.VERSION.SDK_INT >= 26) {
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                int phoneCount = telephonyManager.getPhoneCount();
                if (phoneCount > 1) {
                    preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM1);
                    if (preference != null) {
                        if (!(hasFeature && hasSIMCard))
                            enabled = false;
                        int index = preference.findIndexOfValue(preference.getValue());
                        GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
                    }
                    preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM2);
                    if (preference != null) {
                        if (!(hasFeature && hasSIMCard))
                            enabled = false;
                        int index = preference.findIndexOfValue(preference.getValue());
                        GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
                    }
                }
            }
        }*/
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_GPS);
        if (preference != null) {
            if (!PPApplication.HAS_FEATURE_LOCATION_GPS)
                enabled = false;
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_NFC);
        if (preference != null) {
            if (!PPApplication.HAS_FEATURE_NFC)
                enabled = false;
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
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
            key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA) ||
            //key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM1) ||
            //key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM2) ||
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
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, preferences, context);
/*        if (Build.VERSION.SDK_INT >= 26) {
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                int phoneCount = telephonyManager.getPhoneCount();
                if (phoneCount > 1) {
                    setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM1, preferences, context);
                    setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM2, preferences, context);
                }
            }
        }*/
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
                    this._wifi, this._bluetooth, this._mobileData, this._gps, this._nfc, this._airplaneMode/*,
                    this._mobileDataSIM1, this._mobileDataSIM2*/);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_RADIO_SWITCH, false).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !(tmp.isRunnable(context) && permissionGranted), false);
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
                    runnable = runnable &&
                            ((_wifi != 0) || (_bluetooth != 0) || (_mobileData != 0) || (_gps != 0) ||
                             (_nfc != 0) || (_airplaneMode != 0) /*|| (_mobileDataSIM1 != 0) || (_mobileDataSIM2 != 0)*/);
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

        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA);
        if (preference != null)
            preference.setEnabled(enabled);
/*        if (Build.VERSION.SDK_INT >= 26) {
            boolean showPreferences = false;
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                int phoneCount = telephonyManager.getPhoneCount();
                if (phoneCount > 1) {
                    boolean sim1Exists;
                    boolean sim2Exists;
                    synchronized (PPApplication.simCardsMutext) {
                        sim1Exists = PPApplication.simCardsMutext.simCardsDetected;
                        sim2Exists = sim1Exists;
                        sim1Exists = sim1Exists && PPApplication.simCardsMutext.sim1Exists;
                        sim2Exists = sim2Exists && PPApplication.simCardsMutext.sim2Exists;
                    }

                    showPreferences = true;
                    preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM1);
                    if (preference != null)
                        preference.setEnabled(enabled && sim1Exists && sim2Exists);
                    preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM2);
                    if (preference != null)
                        preference.setEnabled(enabled && sim1Exists && sim2Exists);
                }
                else {
                    preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM1);
                    if (preference != null)
                        preference.setEnabled(false);
                    preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM2);
                    if (preference != null)
                        preference.setEnabled(false);
                }
            }
            if (!showPreferences) {
                preference = prefMng.findPreference("eventRadioSwitchMobileDataDualSIMInfo");
                if (preference != null)
                    preference.setVisible(false);
                preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA_SIM2);
                if (preference != null)
                    preference.setVisible(false);
            }
        }
 */

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
                }

/*                if ((Build.VERSION.SDK_INT >= 26) && PPApplication.HAS_FEATURE_TELEPHONY) {
                    final TelephonyManager telephonyManager = (TelephonyManager) eventsHandler.context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
                            boolean enabled = false;
                            boolean connected = false;

                            if ((_mobileDataSIM1 == 1) || (_mobileDataSIM1 == 2)) {
                                enabled = ActivateProfileHelper.isMobileData(eventsHandler.context, 1);
//                                PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "mobileDataSIM1 enabled=" + enabled);
                            }
                            if ((_mobileDataSIM1 == 3) || (_mobileDataSIM1 == 4)) {
                                boolean defaultIsSIM1 = false;
                                SubscriptionManager mSubscriptionManager = (SubscriptionManager) eventsHandler.context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                                //SubscriptionManager.from(context);
                                if (mSubscriptionManager != null) {
                                    //noinspection ConstantConditions
                                    //if (Build.VERSION.SDK_INT > 23) {
                                        int defaultDataId = SubscriptionManager.getDefaultDataSubscriptionId();

                                        List<SubscriptionInfo> subscriptionList = null;
                                        try {
                                            // Loop through the subscription list i.e. SIM list.
                                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                                        } catch (SecurityException e) {
                                            PPApplication.recordException(e);
                                        }
                                        if (subscriptionList != null) {
                                            for (int i = 0; i < subscriptionList.size(); i++) {
                                                // Get the active subscription ID for a given SIM card.
                                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                                if (subscriptionInfo != null) {
                                                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                                                    if (1 == (slotIndex + 1)) {
                                                        int subscriptionId = subscriptionInfo.getSubscriptionId();
                                                        if (subscriptionId == defaultDataId) {
                                                            defaultIsSIM1 = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    //}
                                }

                                if (defaultIsSIM1) {
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
                                                    if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                                                        connected = MobileDataNetworkCallback.connected;
                                                        break;
                                                    }
                                                } catch (Exception e) {
//                                                Log.e("EventPreferencesWifi.doHandleEvent", Log.getStackTraceString(e));
                                                    PPApplication.recordException(e);
                                                }
                                            }
                                        }
                                    }
                                }
//                                PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "mobileDataSIM1 connected=" + connected);
                            }

                            if (_mobileDataSIM1 != 0)
                                tested = true;
                            if (_mobileDataSIM1 == 1)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && enabled;
                            else
                            if (_mobileDataSIM1 == 2)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (!enabled);
                            else
                            if (_mobileDataSIM1 == 3)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && connected;
                            else
                            if (_mobileDataSIM1 == 4)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (!connected);

                            enabled = false;
                            connected = false;
                            if ((_mobileDataSIM2 == 1) || (_mobileDataSIM2 == 2)) {
                                enabled = ActivateProfileHelper.isMobileData(eventsHandler.context, 2);
//                                PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "mobileDataSIM2 enabled=" + enabled);
                            }
                            if ((_mobileDataSIM2 == 3) || (_mobileDataSIM2 == 4)) {
                                boolean defaultIsSIM2 = false;
                                SubscriptionManager mSubscriptionManager = (SubscriptionManager) eventsHandler.context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                                //SubscriptionManager.from(context);
                                if (mSubscriptionManager != null) {
                                    //noinspection ConstantConditions
                                    //if (Build.VERSION.SDK_INT > 23) {
                                        int defaultDataId = SubscriptionManager.getDefaultDataSubscriptionId();

                                        List<SubscriptionInfo> subscriptionList = null;
                                        try {
                                            // Loop through the subscription list i.e. SIM list.
                                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                                        } catch (SecurityException e) {
                                            PPApplication.recordException(e);
                                        }
                                        if (subscriptionList != null) {
                                            for (int i = 0; i < subscriptionList.size(); i++) {
                                                // Get the active subscription ID for a given SIM card.
                                                SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                                                if (subscriptionInfo != null) {
                                                    int slotIndex = subscriptionInfo.getSimSlotIndex();
                                                    if (2 == (slotIndex + 1)) {
                                                        int subscriptionId = subscriptionInfo.getSubscriptionId();
                                                        if (subscriptionId == defaultDataId) {
                                                            defaultIsSIM2 = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    //}
                                }

                                if (defaultIsSIM2) {
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
                                                    if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                                                        connected = MobileDataNetworkCallback.connected;
                                                        break;
                                                    }
                                                } catch (Exception e) {
//                                                Log.e("EventPreferencesWifi.doHandleEvent", Log.getStackTraceString(e));
                                                    PPApplication.recordException(e);
                                                }
                                            }
                                        }
                                    }
                                }
//                                PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "mobileDataSIM1 connected=" + connected);
                            }

                            if (_mobileDataSIM2 != 0)
                                tested = true;
                            if (_mobileDataSIM2 == 1)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && enabled;
                            else
                            if (_mobileDataSIM2 == 2)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (!enabled);
                            else
                            if (_mobileDataSIM2 == 3)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && connected;
                            else
                            if (_mobileDataSIM2 == 4)
                                eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (!connected);
                        }
                    }
                }
*/
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
