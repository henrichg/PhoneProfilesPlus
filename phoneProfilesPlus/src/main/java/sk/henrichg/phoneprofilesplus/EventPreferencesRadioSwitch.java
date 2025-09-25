package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.List;

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
    int _ethernet;

    static final String PREF_EVENT_RADIO_SWITCH_ENABLED = "eventRadioSwitchEnabled";
    private static final String PREF_EVENT_RADIO_SWITCH_WIFI = "eventRadioSwitchWifi";
    private static final String PREF_EVENT_RADIO_SWITCH_BLUETOOTH = "eventRadioSwitchBluetooth";
    private static final String PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF = "eventRadioSwitchSIMOnOff";
    static final String PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS = "eventRadioSwitchDefaultSIMForCalls";
    static final String PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS = "eventRadioSwitchDefaultSIMForSMS";
    static final String PREF_EVENT_RADIO_SWITCH_MOBILE_DATA = "eventRadioSwitchMobileData";
    private static final String PREF_EVENT_RADIO_SWITCH_GPS = "eventRadioSwitchGPS";
    private static final String PREF_EVENT_RADIO_SWITCH_NFC = "eventRadioSwitchNFC";
    private static final String PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE = "eventRadioSwitchAirplaneMode";
    private static final String PREF_EVENT_RADIO_SWITCH_ETHERNET = "eventRadioSwitchEthernet";

    //static final String PREF_EVENT_RADIO_SWITCH_ENABLED_NO_CHECK_SIM = "eventRadioSwitchEnabledEnabledNoCheckSim";
    static final String PREF_EVENT_RADIO_SWITCH_ENABLED_WIFI = "eventRadioSwitchEnabledEnabledWifi";
    static final String PREF_EVENT_RADIO_SWITCH_ENABLED_BLUETOOTH = "eventRadioSwitchEnabledEnabledBluetooth";
    static final String PREF_EVENT_RADIO_SWITCH_ENABLED_SIM_ON_OFF = "eventRadioSwitchEnabledEnabledSIMOnOff";
    static final String PREF_EVENT_RADIO_SWITCH_ENABLED_DEFAULT_SIM = "eventRadioSwitchEnabledEnabledDefaultSIM";
    static final String PREF_EVENT_RADIO_SWITCH_ENABLED_MOBILE_DATA = "eventRadioSwitchEnabledEnabledMobileData";
    static final String PREF_EVENT_RADIO_SWITCH_ENABLED_GPS = "eventRadioSwitchEnabledEnabledGPS";
    static final String PREF_EVENT_RADIO_SWITCH_ENABLED_NFC = "eventRadioSwitchEnabledEnabledNFC";
    static final String PREF_EVENT_RADIO_SWITCH_ENABLED_AIRPLANE_MODE = "eventRadioSwitchEnabledEnabledAirplaneMode";
    static final String PREF_EVENT_RADIO_SWITCH_ENABLED_ETHERNET = "eventRadioSwitchEnabledEnabledEthernet";

    static final String PREF_EVENT_RADIO_SWITCH_CATEGORY = "eventRadioSwitchCategoryRoot";

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
                                int airplaneMode,
                                int ethernet)
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
        this._ethernet = ethernet;
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
        this._ethernet = fromEvent._eventPreferencesRadioSwitch._ethernet;
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
        editor.putString(PREF_EVENT_RADIO_SWITCH_ETHERNET, String.valueOf(this._ethernet));
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
        this._ethernet = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_ETHERNET, "0"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_radioSwitch_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_radioSwitch), addPassStatus, DatabaseHandler.ETYPE_RADIO_SWITCH, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                boolean _addBullet = false;

                if (this._wifi != 0) {
                    if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_WIFI, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        _value.append(context.getString(R.string.event_preferences_radioSwitch_wifi)).append(StringConstants.STR_COLON_WITH_SPACE);
                        String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchWithConnectionArray);
                        _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[this._wifi], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                        _addBullet = true;
                    }
                }

                if (this._bluetooth != 0) {
                    if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_BLUETOOTH, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        if (_addBullet)
                            _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_radioSwitch_bluetooth)).append(StringConstants.STR_COLON_WITH_SPACE);
                        String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchWithConnectionArray);
                        _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[this._bluetooth], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                        _addBullet = true;
                    }
                }

                    int phoneCount = 1;
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        phoneCount = telephonyManager.getPhoneCount();
                    }
                    boolean twoSimCards = false;
                    if (phoneCount == 2) {
//                        Log.e("EventPreferencesRadioSwitch.getPreferencesDescription", "called hasSIMCard");
                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                        boolean sim1Exists = hasSIMCardData.hasSIM1;
                        boolean sim2Exists = hasSIMCardData.hasSIM2;

                        twoSimCards =
                                sim1Exists &&
                                sim2Exists;
                    }

                    if (phoneCount > 1) {
                        if (this._simOnOff != 0) {
                            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_SIM_ON_OFF, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if (_addBullet)
                                    _value.append(StringConstants.STR_BULLET);
                                String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchSIMOnOffArray);
                                _value.append(context.getString(R.string.event_preferences_radioSwitch_simOnOff)).append(StringConstants.STR_COLON_WITH_SPACE);
                                _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[this._simOnOff], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                                _addBullet = true;
                            }
                        }
                    }

                    if (twoSimCards) {
                        if (this._defaultSIMForCalls != 0) {
                            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_DEFAULT_SIM, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if (_addBullet)
                                    _value.append(StringConstants.STR_BULLET);
                                _value.append(context.getString(R.string.event_preferences_radioSwitch_defaultSIMForCalls)).append(StringConstants.STR_COLON_WITH_SPACE);
                                String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchDefaultSIMArray);
                                _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[this._defaultSIMForCalls], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                                _addBullet = true;
                            }
                        }
                        if (this._defaultSIMForSMS != 0) {
                            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_DEFAULT_SIM, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                                if (_addBullet)
                                    _value.append(StringConstants.STR_BULLET);
                                _value.append(context.getString(R.string.event_preferences_radioSwitch_defaultSIMForSMS)).append(StringConstants.STR_COLON_WITH_SPACE);
                                String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchDefaultSIMArray);
                                _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[this._defaultSIMForSMS], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                                _addBullet = true;
                            }
                        }
                    }

                if (this._mobileData != 0) {
                    if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_MOBILE_DATA, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        if (_addBullet)
                            _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_radioSwitch_mobileData)).append(StringConstants.STR_COLON_WITH_SPACE);
                        /*int*/
                        phoneCount = 1;
                        //TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (telephonyManager != null) {
                            phoneCount = telephonyManager.getPhoneCount();
                        }
                        String[] fieldArray;
                        String[] fieldValues;
                        if (phoneCount > 1) {
                            fieldArray = context.getResources().getStringArray(R.array.eventRadioSwitchMobileDataDualSIMArray);
                            fieldValues = context.getResources().getStringArray(R.array.eventRadioSwitchhMobileDataDualSIMValues);
                        } else {
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
                            _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fieldArray[index], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                        _addBullet = true;
                    }
                }
                if (this._gps != 0) {
                    if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_BLUETOOTH, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        if (_addBullet)
                            _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_radioSwitch_gps)).append(StringConstants.STR_COLON_WITH_SPACE);
                        String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                        _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[this._gps], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                        _addBullet = true;
                    }
                }

                if (this._nfc != 0) {
                    if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_NFC, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        if (_addBullet)
                            _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_radioSwitch_nfc)).append(StringConstants.STR_COLON_WITH_SPACE);
                        String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                        _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[this._nfc], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                        _addBullet = true;
                    }
                }

                if (this._airplaneMode != 0) {
                    if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_AIRPLANE_MODE, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        if (_addBullet)
                            _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_radioSwitch_airplaneMode)).append(StringConstants.STR_COLON_WITH_SPACE);
                        String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                        _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[this._airplaneMode], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    }
                    _addBullet = true;
                }

                if (this._ethernet != 0) {
                    if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_ETHERNET, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        if (_addBullet)
                            _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_radioSwitch_ethernet)).append(StringConstants.STR_COLON_WITH_SPACE);
                        String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchWithConnectionArray);
                        _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[this._ethernet], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    }
                }

            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_RADIO_SWITCH_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        PreferenceAllowed defaultSIMPreferenceAllowed = null;
        PreferenceAllowed mobileDataPreferenceAllowed = null;
        PreferenceAllowed swithSIMOnOffPreferenceAllowed = null;

        if (key.equals(PREF_EVENT_RADIO_SWITCH_WIFI) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_BLUETOOTH) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_GPS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_NFC) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE)||
            key.equals(PREF_EVENT_RADIO_SWITCH_ETHERNET))
        {

            boolean hasHardware = true;
            if (key.equals(PREF_EVENT_RADIO_SWITCH_WIFI) && (!PPApplication.HAS_FEATURE_WIFI)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
                hasHardware = false;
            }
            if (key.equals(PREF_EVENT_RADIO_SWITCH_BLUETOOTH) && (!PPApplication.HAS_FEATURE_BLUETOOTH)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
                hasHardware = false;
            }
            if (key.equals(PREF_EVENT_RADIO_SWITCH_GPS) && (!PPApplication.HAS_FEATURE_LOCATION_GPS)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
                hasHardware = false;
            }
            if (key.equals(PREF_EVENT_RADIO_SWITCH_NFC) && (!PPApplication.HAS_FEATURE_NFC)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
                hasHardware = false;
            }
            if (key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA)) {
                if (PPApplication.HAS_FEATURE_TELEPHONY) {
                    mobileDataPreferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_MOBILE_DATA, false, context);
                    if (mobileDataPreferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            int phoneCount = 1;
                            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            if (telephonyManager != null) {
                                phoneCount = telephonyManager.getPhoneCount();
                            }
                            String[] fieldArray;
                            String[] fieldValues;
                            if (phoneCount > 1) {
                                fieldArray = context.getResources().getStringArray(R.array.eventRadioSwitchMobileDataDualSIMArray);
                                fieldValues = context.getResources().getStringArray(R.array.eventRadioSwitchhMobileDataDualSIMValues);
                            } else {
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
                    } else {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            if (mobileDataPreferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) {
                                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                        StringConstants.STR_COLON_WITH_SPACE + mobileDataPreferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            }
                        }
                    }
                }
                else {
                    Preference preference = prefMng.findPreference(key);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                }
                hasHardware = false; // do not use default change of summary
            }
            if (key.equals(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS) ||
                    key.equals(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    defaultSIMPreferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_DEFAULT_SIM, false, context);
                    if (defaultSIMPreferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) {
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                StringConstants.STR_COLON_WITH_SPACE + defaultSIMPreferenceAllowed.getNotAllowedPreferenceReasonString(context));
                        hasHardware = false;
                    }
                }
            }
            if (key.equals(PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF)) {
                Preference preference = prefMng.findPreference(key);
                if (PPApplication.HAS_FEATURE_TELEPHONY) {
                    if (preference != null) {
                        swithSIMOnOffPreferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_SIM_ON_OFF, false, context);
                        if (swithSIMOnOffPreferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) {
                            preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                    StringConstants.STR_COLON_WITH_SPACE + swithSIMOnOffPreferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            hasHardware = false;
                        }
                    }
                } else {
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                    hasHardware = false;
                }
            }
            /*
            if (key.equals(PREF_EVENT_RADIO_SWITCH_ETHERNET) && (!PPApplication.HAS_FEATURE_ETHERNET)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.preferenceAllowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
                hasHardware = false;
            }
            */

            if (hasHardware) {
                PPListPreference listPreference = prefMng.findPreference(key);
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
        //boolean isAllConfigured = event._eventPreferencesRadioSwitch.isAllConfigured(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, false);
        PPListPreference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_WIFI);
        if (preference != null) {
            boolean __enabled = enabled;
            if (!PPApplication.HAS_FEATURE_WIFI)
                __enabled = false;
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, __enabled, index > 0, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_BLUETOOTH);
        if (preference != null) {
            boolean __enabled = enabled;
            if (!PPApplication.HAS_FEATURE_BLUETOOTH)
                __enabled = false;
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, __enabled, index > 0, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF);
        if (preference != null) {
            boolean __enabled = enabled;
            if (!PPApplication.HAS_FEATURE_TELEPHONY)
                __enabled = false;
            else {
                if (swithSIMOnOffPreferenceAllowed == null)
                    swithSIMOnOffPreferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_SIM_ON_OFF, false, context);
                if (swithSIMOnOffPreferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                    __enabled = false;
            }
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, __enabled, index > 0, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS);
        if (preference != null) {
            boolean __enabled = enabled;
            if (!PPApplication.HAS_FEATURE_TELEPHONY)
                __enabled = false;
            else {
                if (defaultSIMPreferenceAllowed == null)
                    defaultSIMPreferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_DEFAULT_SIM, false, context);
                if (defaultSIMPreferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                    __enabled = false;
            }
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, __enabled, index > 0, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS);
        if (preference != null) {
            boolean __enabled = enabled;
            if (!PPApplication.HAS_FEATURE_TELEPHONY)
                __enabled = false;
            else {
                if (defaultSIMPreferenceAllowed == null)
                    defaultSIMPreferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_DEFAULT_SIM, false, context);
                if (defaultSIMPreferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                    __enabled = false;
            }
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, __enabled, index > 0, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA);
        if (preference != null) {
            boolean __enabled = enabled;
            if (!PPApplication.HAS_FEATURE_TELEPHONY)
                __enabled = false;
            else {
                if (mobileDataPreferenceAllowed == null)
                    mobileDataPreferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED_MOBILE_DATA, false, context);
                if (mobileDataPreferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                    __enabled = false;
            }
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, __enabled, index > 0, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_GPS);
        if (preference != null) {
            boolean __enabled = enabled;
            if (!PPApplication.HAS_FEATURE_LOCATION_GPS)
                __enabled = false;
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, __enabled, index > 0, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_NFC);
        if (preference != null) {
            boolean __enabled = enabled;
            if (!PPApplication.HAS_FEATURE_NFC)
                __enabled = false;
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, __enabled, index > 0, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE);
        if (preference != null) {
            //boolean __enabled = enabled;
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_ETHERNET);
        if (preference != null) {
            //noinspection UnnecessaryLocalVariable
            boolean __enabled = enabled;
            //if (!PPApplication.HAS_FEATURE_ETHERNET)
            //    __enabled = false;
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, __enabled, index > 0, false, true, !isRunnable, false);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_RADIO_SWITCH_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
        if (key.equals(PREF_EVENT_RADIO_SWITCH_WIFI) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_BLUETOOTH) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_GPS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_NFC) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_ETHERNET))
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
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_ETHERNET, preferences, context);

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
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED, false, context);
        if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesRadioSwitch tmp = new EventPreferencesRadioSwitch(this._event, this._enabled,
                    this._wifi, this._bluetooth, this._simOnOff, this._defaultSIMForCalls, this._defaultSIMForSMS,
                    this._mobileData, this._gps, this._nfc, this._airplaneMode, this._ethernet);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_RADIO_SWITCH).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && tmp.isAllConfigured(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        StringConstants.STR_COLON_WITH_SPACE+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

            boolean ok = false;
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                int phoneCount = telephonyManager.getPhoneCount();
                if (phoneCount > 1) {
                    boolean twoSimCards = false;
                    if (phoneCount == 2) {
//                        Log.e("EventPreferencesRadioSwitch.isRunnable", "called hasSIMCard");
                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                        boolean sim1Exists = hasSIMCardData.hasSIM1;
                        boolean sim2Exists = hasSIMCardData.hasSIM2;

                        twoSimCards =
                                sim1Exists &&
                                sim2Exists;
                    }
                    if (twoSimCards)
                        runnable = runnable &&
                                ((_wifi != 0) || (_bluetooth != 0) || (_mobileData != 0) || (_gps != 0) ||
                                 (_nfc != 0) || (_airplaneMode != 0) || (_defaultSIMForCalls != 0) || (_defaultSIMForSMS != 0) ||
                                 (_simOnOff != 0) || (_ethernet != 0));
                    else
                        runnable = runnable &&
                                ((_wifi != 0) || (_bluetooth != 0) || (_mobileData != 0) || (_gps != 0) ||
                                 (_nfc != 0) || (_airplaneMode != 0) || (_simOnOff != 0) || (_ethernet != 0));
                    ok = true;
                }
            }
            if (!ok)
                runnable = runnable &&
                        ((_wifi != 0) || (_bluetooth != 0) || (_mobileData != 0) || (_gps != 0) ||
                         (_nfc != 0) || (_airplaneMode != 0) || (_ethernet != 0));

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context)
    {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_ENABLED) != null)
            {
                boolean enabled = EventStatic.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED;

                Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_WIFI);
                if (preference != null)
                    preference.setEnabled(enabled && PPApplication.HAS_FEATURE_WIFI);
                preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_BLUETOOTH);
                if (preference != null)
                    preference.setEnabled(enabled && PPApplication.HAS_FEATURE_BLUETOOTH);

                int phoneCount = 1;
                boolean hasSIM1 = false;
                boolean hasSIM2 = false;
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    phoneCount = telephonyManager.getPhoneCount();
//                    Log.e("EventPreferencesRadioSwitch.checkPreferences", "called hasSIMCard");
                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                    hasSIM1 = hasSIMCardData.hasSIM1;
                    hasSIM2 = hasSIMCardData.hasSIM2;
                }

                preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS);
                if (preference != null) {
                        if (phoneCount > 1) {
                            if (phoneCount == 2) {
                                boolean twoSimCards = hasSIM1 && hasSIM2;
                                preference.setEnabled(twoSimCards);
                                if (twoSimCards)
                                    preference.setEnabled(enabled && PPApplication.HAS_FEATURE_TELEPHONY);
                            }
                        } else
                            preference.setEnabled(false);
                }
                preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS);
                if (preference != null) {
                        if (phoneCount > 1) {
                            if (phoneCount == 2) {
                                boolean twoSimCards = hasSIM1 && hasSIM2;
                                preference.setEnabled(twoSimCards);
                                if (twoSimCards)
                                    preference.setEnabled(enabled && PPApplication.HAS_FEATURE_TELEPHONY);
                            }
                        } else
                            preference.setEnabled(false);
                }

                PPListPreference listPreference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA);
                if (listPreference != null) {
                    if (hasSIM1 || hasSIM2) {
                        String value = listPreference.getValue();
                        if (phoneCount > 1) {
                            listPreference.setEntries(R.array.eventRadioSwitchMobileDataDualSIMArray);
                            listPreference.setEntryValues(R.array.eventRadioSwitchhMobileDataDualSIMValues);
                        } else {
                            listPreference.setEntries(R.array.eventRadioSwitchWithConnectionArray);
                            listPreference.setEntryValues(R.array.eventRadioSwitchhWithConnectionValues);
                            if (value.equals("5") || value.equals("6"))
                                value = "3";
                        }
                        listPreference.setValue(value);
                        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, preferences, context);
                    }
                    listPreference.setEnabled(enabled && PPApplication.HAS_FEATURE_TELEPHONY && (hasSIM1 || hasSIM2));
                }
                listPreference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF);
                if (listPreference != null) {
                    if (hasSIM1 || hasSIM2) {
                        if (phoneCount <= 1) {
                            String value = listPreference.getValue();
                            if (value.equals("3") || value.equals("5"))
                                value = "1";
                            if (value.equals("4") || value.equals("6"))
                                value = "2";
                            listPreference.setValue(value);
                            setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_SIM_ON_OFF, preferences, context);
                        }
                    }
                    listPreference.setEnabled(enabled && PPApplication.HAS_FEATURE_TELEPHONY && (hasSIM1 || hasSIM2));
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
                //preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_ETHERNET);
                //if (preference != null)
                //    preference.setEnabled(enabled && PPApplication.HAS_FEATURE_ETHERNET);

                setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_ENABLED, preferences, context);
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
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, false, eventsHandler.context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
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

                            boolean connected = false;
                            if ((_wifi == 3) || (_wifi == 4)) {
                                ConnectivityManager connManager = null;
                                try {
                                    connManager = (ConnectivityManager) eventsHandler.context.getSystemService(Context.CONNECTIVITY_SERVICE);
                                } catch (Exception e) {
                                    // java.lang.NullPointerException: missing IConnectivityManager
                                    // Dual SIM?? Bug in Android ???
                                    PPApplicationStatic.recordException(e);
                                }
                                if (connManager != null) {
                                    //noinspection deprecation
                                    NetworkInfo activeNetwork = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                                    //noinspection deprecation
                                    connected = activeNetwork != null && activeNetwork.isConnected();
//                                    PPApplicationStatic.logE("EventPreferencesRadioSwitch.doHandleEvent", "wi-fi connected="+connected);

                                    tested = true;

                                    /*
                                    Network[] networks = connManager.getAllNetworks();
                                    if ((networks != null) && (networks.length > 0)) {
                                        for (Network network : networks) {
                                            try {
                                                NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                                                if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                                    connected = WifiNetworkCallback.connected;

                                                    tested = true;
                                                    break;
                                                }
                                            } catch (Exception e) {
                                                // Log.e("EventPreferencesRadioSwitch.doHandleEvent", Log.getStackTraceString(e));
                                                PPApplicationStatic.recordException(e);
                                            }
                                        }
                                    }
                                    */
                                }
                            }
                            else
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

                            boolean connected = false;
                            if ((_bluetooth == 3) || (_bluetooth == 4)) {
                                List<BluetoothDeviceData> connectedDevices = BluetoothConnectionBroadcastReceiver.getConnectedDevices(eventsHandler.context);
                                connected = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(connectedDevices, null, "");
                            }
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
                        else
                            eventsHandler.notAllowedRadioSwitch = true;
                    } else
                        eventsHandler.notAllowedRadioSwitch = true;
                }

                if ((_mobileData != 0) && PPApplication.HAS_FEATURE_TELEPHONY) {
                    boolean _isMobileDataSIM1 = ActivateProfileHelper.isMobileData(eventsHandler.context, 1);
                    boolean _isMobileDataSIM2 = ActivateProfileHelper.isMobileData(eventsHandler.context, 2);
                    boolean _isMobileDataSIM0 = ActivateProfileHelper.isMobileData(eventsHandler.context, 0);
                    boolean enabled = (_isMobileDataSIM0 || _isMobileDataSIM1 || _isMobileDataSIM2);

                    boolean connected = false;
                    if ((_mobileData == 3) || (_mobileData == 4) || (_mobileData == 5) || (_mobileData == 6)) {
                        ConnectivityManager connManager = null;
                        try {
                            connManager = (ConnectivityManager) eventsHandler.context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        } catch (Exception e) {
                            // java.lang.NullPointerException: missing IConnectivityManager
                            // Dual SIM?? Bug in Android ???
                            PPApplicationStatic.recordException(e);
                        }
                        if (connManager != null) {
                            //noinspection deprecation
                            NetworkInfo activeNetwork = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                            //noinspection deprecation
                            connected = activeNetwork != null && activeNetwork.isConnected();
//                            PPApplicationStatic.logE("EventPreferencesRadioSwitch.doHandleEvent", "mobile data connected="+connected);

                            if ((_mobileData == 3) || (_mobileData == 4))
                                tested = true;

                            /*
                            Network[] networks = connManager.getAllNetworks();
                            if ((networks != null) && (networks.length > 0)) {
                                for (Network network : networks) {
                                    try {
                                        NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                                        if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                                            connected = MobileDataNetworkCallback.connected;

                                            if ((_mobileData == 3) || (_mobileData == 4))
                                                tested = true;
                                            break;
                                        }
                                    } catch (Exception e) {
//                                    Log.e("EventPreferencesRadioSwitch.doHandleEvent", Log.getStackTraceString(e));
                                        PPApplicationStatic.recordException(e);
                                    }
                                }
                            }
                            */
                        }
                    } else
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
                            tested = true;
                            int phoneCount = 1;
                            TelephonyManager telephonyManager = (TelephonyManager) eventsHandler.context.getSystemService(Context.TELEPHONY_SERVICE);
                            if (telephonyManager != null) {
                                phoneCount = telephonyManager.getPhoneCount();
                            }
                            boolean twoSimCards = false;
                            if (phoneCount == 2) {
//                                Log.e("EventPreferencesRadioSwitch.doHandleEvent", "(1) called hasSIMCard");
                                HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(eventsHandler.context);
                                boolean sim1Exists = hasSIMCardData.hasSIM1;
                                boolean sim2Exists = hasSIMCardData.hasSIM2;

                                twoSimCards = sim1Exists &&
                                                sim2Exists;
                            }

                            if (phoneCount > 1) {
                                if (twoSimCards) {
                                    if (connected) {
                                        if (Permissions.checkModifyPhone(eventsHandler.context.getApplicationContext())) {
                                            int defaultSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
                                            int defaultSIM = GlobalUtils.getSIMCardFromSubscriptionId(eventsHandler.context, defaultSubscriptionId);
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

                if ((_gps != 0) && PPApplication.HAS_FEATURE_LOCATION_GPS) {

                    boolean enabled;
                    LocationManager locationManager = (LocationManager) eventsHandler.context.getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager != null) {
                        enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        //}
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
                        tested = true;
                        if (_nfc == 1)
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && enabled;
                        else
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && !enabled;
                    }
                }

                if (_airplaneMode != 0) {

                    boolean enabled = ActivateProfileHelper.isAirplaneMode(eventsHandler.context);
                    tested = true;
                    if (_airplaneMode == 1)
                        eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && enabled;
                    else
                        eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && !enabled;
                }

                if (((_defaultSIMForCalls != 0) || (_defaultSIMForSMS != 0)) && PPApplication.HAS_FEATURE_TELEPHONY) {
                        int phoneCount = 1;
                        TelephonyManager telephonyManager = (TelephonyManager) eventsHandler.context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (telephonyManager != null) {
                            phoneCount = telephonyManager.getPhoneCount();
                        }
                        boolean twoSimCards = false;
                        if (phoneCount == 2) {
//                            Log.e("EventPreferencesRadioSwitch.doHandleEvent", "(2) called hasSIMCard");
                            HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(eventsHandler.context);
                            boolean sim1Exists = hasSIMCardData.hasSIM1;
                            boolean sim2Exists = hasSIMCardData.hasSIM2;

                            twoSimCards = sim1Exists &&
                                    sim2Exists;
                        }

                        if ((phoneCount > 1) && twoSimCards) {
                            tested = true;
                            if (Permissions.checkReadPhoneState(eventsHandler.context.getApplicationContext())) {
                                if (_defaultSIMForCalls != 0) {
                                    int defaultSubscriptionId = SubscriptionManager.getDefaultSubscriptionId();
                                    int simCard = GlobalUtils.getSIMCardFromSubscriptionId(eventsHandler.context, defaultSubscriptionId);
                                    int configuredSIMCard = 1;
                                    if (_defaultSIMForCalls == 2)
                                        configuredSIMCard = 2;
                                    eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                            (simCard == configuredSIMCard);
                                }
                                if (_defaultSIMForSMS != 0) {
                                    int defaultSubscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId();
                                    int simCard = GlobalUtils.getSIMCardFromSubscriptionId(eventsHandler.context, defaultSubscriptionId);
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

                if ((_simOnOff != 0) && PPApplication.HAS_FEATURE_TELEPHONY) {
                    tested = true;
//                    Log.e("EventPreferencesRadioSwitch.doHandleEvent", "(3) called hasSIMCard");
                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(eventsHandler.context);
                    //boolean sim0Exists = hasSIMCardData.hasSIM1 || hasSIMCardData.hasSIM2;
                    boolean sim1Exists = hasSIMCardData.hasSIM1;
                    boolean sim2Exists = hasSIMCardData.hasSIM2;

                    switch (_simOnOff) {
                        case 1:
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                    (/*sim0Exists ||*/
                                     sim1Exists ||
                                     sim2Exists);
                            break;
                        case 2:
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                    (/*(!sim0Exists) ||*/
                                     (!sim1Exists) ||
                                     (!sim2Exists));
                            break;
                        case 3:
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                            (sim1Exists);
                            break;
                        case 4:
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                    (!sim1Exists);
                            break;
                        case 5:
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                    (sim2Exists);
                            break;
                        case 6:
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed &&
                                    (!sim2Exists);
                            break;
                    }
                }

                if ((_ethernet != 0) /*&& PPApplication.HAS_FEATURE_ETHERNET*/) {

                    @SuppressLint("WrongConstant")
                    EthernetManager etherentManager = (EthernetManager) eventsHandler.context.getApplicationContext().getSystemService(Context.ETHERNET_SERVICE);
                    if (etherentManager != null) {
                        boolean enabled = etherentManager.getAvailableInterfaces().length > 0;

                        boolean connected = false;
                        if ((_ethernet == 3) || (_ethernet == 4)) {
                            ConnectivityManager connManager = null;
                            try {
                                connManager = (ConnectivityManager) eventsHandler.context.getSystemService(Context.CONNECTIVITY_SERVICE);
                            } catch (Exception e) {
                                // java.lang.NullPointerException: missing IConnectivityManager
                                // Dual SIM?? Bug in Android ???
                                PPApplicationStatic.recordException(e);
                            }
                            if (connManager != null) {
                                //noinspection deprecation
                                NetworkInfo activeNetwork = connManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
                                //noinspection deprecation
                                connected = activeNetwork != null && activeNetwork.isConnected();
//                                    PPApplicationStatic.logE("EventPreferencesRadioSwitch.doHandleEvent", "ethernet connected="+connected);

                                tested = true;
                            }
                        }
                        else
                            tested = true;
                        if (_ethernet == 1)
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && enabled;
                        else
                        if (_ethernet == 2)
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (!enabled);
                        else
                        if (_ethernet == 3)
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && connected;
                        else
                        if (_ethernet == 4)
                            eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && (!connected);
                    }
                    else
                        eventsHandler.notAllowedRadioSwitch = true;
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
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_RADIO_SWITCH);
            }
        }
    }

}
