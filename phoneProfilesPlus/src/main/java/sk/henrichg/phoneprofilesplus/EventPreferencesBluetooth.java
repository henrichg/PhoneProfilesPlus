package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import java.util.Arrays;

class EventPreferencesBluetooth extends EventPreferences {

    String _adapterName;
    int _connectionType;
    int _devicesType;

    static final int CTYPE_CONNECTED = 0;
    static final int CTYPE_INFRONT = 1;
    static final int CTYPE_NOTCONNECTED = 2;
    static final int CTYPE_NOTINFRONT = 3;

    static final int DTYPE_CLASSIC = 0;
    static final int DTYPE_LE = 1;

    static final String PREF_EVENT_BLUETOOTH_ENABLED = "eventBluetoothEnabled";
    static final String PREF_EVENT_BLUETOOTH_ADAPTER_NAME = "eventBluetoothAdapterNAME";
    private static final String PREF_EVENT_BLUETOOTH_CONNECTION_TYPE = "eventBluetoothConnectionType";
    private static final String PREF_EVENT_BLUETOOTH_DEVICES_TYPE = "eventBluetoothDevicesType";
    static final String PREF_EVENT_BLUETOOTH_APP_SETTINGS = "eventEnableBluetoothScanningAppSettings";
    static final String PREF_EVENT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS = "eventBluetoothLocationSystemSettings";

    private static final String PREF_EVENT_BLUETOOTH_CATEGORY = "eventBluetoothCategory";


    static final String CONFIGURED_BLUETOOTH_NAMES_VALUE = "^configured_bluetooth_names^";
    static final String ALL_BLUETOOTH_NAMES_VALUE = "%";

    EventPreferencesBluetooth(Event event,
                                    boolean enabled,
                                    String adapterName,
                                    int connectionType,
                                    int devicesType)
    {
        super(event, enabled);

        this._adapterName = adapterName;
        this._connectionType = connectionType;
        this._devicesType = devicesType;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesBluetooth._enabled;
        this._adapterName = fromEvent._eventPreferencesBluetooth._adapterName;
        this._connectionType = fromEvent._eventPreferencesBluetooth._connectionType;
        this._devicesType = fromEvent._eventPreferencesBluetooth._devicesType;
        this.setSensorPassed(fromEvent._eventPreferencesBluetooth.getSensorPassed());
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_BLUETOOTH_ENABLED, _enabled);
        editor.putString(PREF_EVENT_BLUETOOTH_ADAPTER_NAME, this._adapterName);
        editor.putString(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, String.valueOf(this._connectionType));
        editor.putString(PREF_EVENT_BLUETOOTH_DEVICES_TYPE, String.valueOf(this._devicesType));
        editor.apply();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_BLUETOOTH_ENABLED, false);
        this._adapterName = preferences.getString(PREF_EVENT_BLUETOOTH_ADAPTER_NAME, "");
        this._connectionType = Integer.parseInt(preferences.getString(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, "1"));
        this._devicesType = Integer.parseInt(preferences.getString(PREF_EVENT_BLUETOOTH_DEVICES_TYPE, "0"));
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_bluetooth_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_BLUETOOTH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>\u2022 ";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_bluetooth), addPassStatus, DatabaseHandler.ETYPE_BLUETOOTH, context);
                    descr = descr + ": </b>";
                }

                if (!ApplicationPreferences.applicationEventBluetoothEnableScanning(context)) {
                    if (!ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(context))
                        descr = descr + "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *<br>";
                    else
                        descr = descr + context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "<br>";
                }
                else
                if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                    descr = descr + "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *<br>";
                }

                descr = descr + context.getString(R.string.pref_event_bluetooth_connectionType);
                String[] connectionListTypeNames = context.getResources().getStringArray(R.array.eventBluetoothConnectionTypeArray);
                String[] connectionListTypes = context.getResources().getStringArray(R.array.eventBluetoothConnectionTypeValues);
                int index = Arrays.asList(connectionListTypes).indexOf(Integer.toString(this._connectionType));
                descr = descr + ": " + connectionListTypeNames[index] + "; ";

                String selectedBluetoothNames = context.getString(R.string.pref_event_bluetooth_adapterName) + ": ";
                String[] splits = this._adapterName.split("\\|");
                for (String _bluetoothName : splits) {
                    if (_bluetoothName.isEmpty()) {
                        //noinspection StringConcatenationInLoop
                        selectedBluetoothNames = selectedBluetoothNames + context.getString(R.string.applications_multiselect_summary_text_not_selected);
                    } else if (splits.length == 1) {
                        switch (_bluetoothName) {
                            case ALL_BLUETOOTH_NAMES_VALUE:
                                selectedBluetoothNames = selectedBluetoothNames + context.getString(R.string.bluetooth_name_pref_dlg_all_bt_names_chb);
                                break;
                            case CONFIGURED_BLUETOOTH_NAMES_VALUE:
                                selectedBluetoothNames = selectedBluetoothNames + context.getString(R.string.bluetooth_name_pref_dlg_configured_bt_names_chb);
                                break;
                            default:
                                if ((this._connectionType == CTYPE_INFRONT) || (this._connectionType == CTYPE_NOTINFRONT)) {
                                    if (WifiBluetoothScanner.bluetoothLESupported(context)) {
                                        if (this._devicesType == DTYPE_CLASSIC)
                                            selectedBluetoothNames = selectedBluetoothNames + "[CL] ";
                                        else if (this._devicesType == DTYPE_LE)
                                            selectedBluetoothNames = selectedBluetoothNames + "[LE] ";
                                    }
                                }
                                selectedBluetoothNames = selectedBluetoothNames + _bluetoothName;
                                break;
                        }
                    } else {
                        selectedBluetoothNames = context.getString(R.string.applications_multiselect_summary_text_selected);
                        selectedBluetoothNames = selectedBluetoothNames + " " + splits.length;
                        break;
                    }
                }
                descr = descr + selectedBluetoothNames;
            }
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_BLUETOOTH_ENABLED)) {
            CheckBoxPreference preference = (CheckBoxPreference) prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, true, preference.isChecked(), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_BLUETOOTH_ENABLED) ||
            key.equals(PREF_EVENT_BLUETOOTH_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_APP_SETTINGS);
            if (preference != null) {
                String summary;
                int titleColor;
                if (!ApplicationPreferences.applicationEventBluetoothEnableScanning(context)) {
                    if (!ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(context)) {
                        summary = "* " + context.getResources().getString(R.string.phone_profiles_pref_applicationEventScanningDisabled) + " *\n\n" +
                                context.getResources().getString(R.string.phone_profiles_pref_eventBluetoothAppSettings_summary);
                        titleColor = Color.RED; //0xFFffb000;
                    }
                    else {
                        summary = context.getResources().getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "\n\n" +
                                context.getResources().getString(R.string.phone_profiles_pref_eventBluetoothAppSettings_summary);
                        titleColor = 0;
                    }
                }
                else {
                    summary = context.getResources().getString(R.string.array_pref_applicationDisableScanning_enabled) + ".\n\n" +
                            context.getResources().getString(R.string.phone_profiles_pref_eventBluetoothAppSettings_summary);
                    titleColor = 0;
                }
                CharSequence sTitle = preference.getTitle();
                Spannable sbt = new SpannableString(sTitle);
                Object spansToRemove[] = sbt.getSpans(0, sTitle.length(), Object.class);
                for(Object span: spansToRemove){
                    if(span instanceof CharacterStyle)
                        sbt.removeSpan(span);
                }
                CheckBoxPreference enabledPreference = (CheckBoxPreference)prefMng.findPreference(PREF_EVENT_BLUETOOTH_ENABLED);
                if ((enabledPreference != null) && enabledPreference.isChecked()) {
                    if (titleColor != 0)
                        sbt.setSpan(new ForegroundColorSpan(titleColor), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    preference.setTitle(sbt);
                }
                else {
                    preference.setTitle(sbt);
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary = context.getString(R.string.phone_profiles_pref_eventBluetoothLocationSystemSettings_summary);
                if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                    summary = "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *\n\n" +
                            summary;
                }
                else {
                    summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n"+
                            summary;
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_BLUETOOTH_ADAPTER_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                /*if (!ApplicationPreferences.applicationEventBluetoothEnableScanning(context.getApplicationContext())) {
                    preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                            ": "+context.getResources().getString(R.string.preference_not_allowed_reason_not_enabled_scanning));
                }
                else {*/
                    String[] splits = value.split("\\|");
                    for (String _bluetoothName : splits) {
                        if (_bluetoothName.isEmpty()) {
                            preference.setSummary(R.string.applications_multiselect_summary_text_not_selected);
                        } else if (splits.length == 1) {
                            switch (value) {
                                case ALL_BLUETOOTH_NAMES_VALUE:
                                    preference.setSummary(R.string.bluetooth_name_pref_dlg_all_bt_names_chb);
                                    break;
                                case CONFIGURED_BLUETOOTH_NAMES_VALUE:
                                    preference.setSummary(R.string.bluetooth_name_pref_dlg_configured_bt_names_chb);
                                    break;
                                default:
                                    preference.setSummary(_bluetoothName);
                                    break;
                            }
                        } else {
                            String selectedBluetoothNames = context.getString(R.string.applications_multiselect_summary_text_selected);
                            selectedBluetoothNames = selectedBluetoothNames + " " + splits.length;
                            preference.setSummary(selectedBluetoothNames);
                            break;
                        }
                    }
                //}
                //GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
            }
        }
        if (key.equals(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }

            boolean btLESupported = WifiBluetoothScanner.bluetoothLESupported(context);
            listPreference = (ListPreference)prefMng.findPreference(PREF_EVENT_BLUETOOTH_DEVICES_TYPE);
            if (listPreference != null) {
                if ((!btLESupported) || value.equals("0") || value.equals("2"))
                    listPreference.setEnabled(false);
                else
                    listPreference.setEnabled(true);
            }
        }
        if (key.equals(PREF_EVENT_BLUETOOTH_DEVICES_TYPE))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                boolean btLESupported = WifiBluetoothScanner.bluetoothLESupported(context);

                if (!btLESupported) {
                    listPreference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                            ": "+context.getString(R.string.preference_not_allowed_reason_no_hardware));
                } else {
                    int index = listPreference.findIndexOfValue(value);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                }
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesBluetooth.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesBluetooth.isRunnable(context);
        CheckBoxPreference enabledPreference = (CheckBoxPreference)prefMng.findPreference(PREF_EVENT_BLUETOOTH_ENABLED);
        boolean enabled = (enabledPreference != null) && enabledPreference.isChecked();
        Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_ADAPTER_NAME);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_BLUETOOTH_ADAPTER_NAME, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyle(preference, enabled, bold, true, !isRunnable, false);
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_BLUETOOTH_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true" : "false", context);
        }
        if (key.equals(PREF_EVENT_BLUETOOTH_ADAPTER_NAME) ||
            key.equals(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE)||
            key.equals(PREF_EVENT_BLUETOOTH_DEVICES_TYPE) ||
            key.equals(PREF_EVENT_BLUETOOTH_APP_SETTINGS) ||
            key.equals(PREF_EVENT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_ENABLED, preferences, context);
        //setSummary(prefMng, PREF_EVENT_BLUETOOTH_ENABLE_SCANNING_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_ADAPTER_NAME, preferences, context);
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_DEVICES_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS, preferences, context);

        if (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed
                != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            Preference preference;
            preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_ENABLED);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_ADAPTER_NAME);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_DEVICES_TYPE);
            if (preference != null) preference.setEnabled(false);
        }

    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_BLUETOOTH_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesBluetooth tmp = new EventPreferencesBluetooth(this._event, this._enabled, this._adapterName, this._connectionType, this._devicesType);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_CATEGORY);
            if (preference != null) {
                CheckBoxPreference enabledPreference = (CheckBoxPreference)prefMng.findPreference(PREF_EVENT_BLUETOOTH_ENABLED);
                boolean enabled = (enabledPreference != null) && enabledPreference.isChecked();
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, enabled, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_CATEGORY);
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
        return super.isRunnable(context) && (!this._adapterName.isEmpty());
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        //final boolean enabled = ApplicationPreferences.applicationEventBluetoothEnableScanning(context.getApplicationContext());
        //Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_ADAPTER_NAME);
        //if (preference != null) preference.setEnabled(enabled);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        //setSummary(prefMng, PREF_EVENT_BLUETOOTH_ADAPTER_NAME, preferences, context);
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS, preferences, context);
        setCategorySummary(prefMng, preferences, context);
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
