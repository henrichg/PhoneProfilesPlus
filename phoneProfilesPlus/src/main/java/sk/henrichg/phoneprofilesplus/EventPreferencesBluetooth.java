package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

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

    //static final String PREF_EVENT_BLUETOOTH_ENABLE_SCANNING_APP_SETTINGS = "eventEnableBluetoothScaningAppSettings";
    static final String PREF_EVENT_BLUETOOTH_ENABLED = "eventBluetoothEnabled";
    private static final String PREF_EVENT_BLUETOOTH_ADAPTER_NAME = "eventBluetoothAdapterNAME";
    private static final String PREF_EVENT_BLUETOOTH_CONNECTION_TYPE = "eventBluetoothConnectionType";
    private static final String PREF_EVENT_BLUETOOTH_DEVICES_TYPE = "eventBluetoothDevicesType";

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
    public String getPreferencesDescription(boolean addBullet, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_bluetooth_summary);
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_bluetooth) + ": " + "</b>";
            }

            descr = descr + context.getString(R.string.pref_event_bluetooth_connectionType);
            String[] connectionListTypeNames = context.getResources().getStringArray(R.array.eventBluetoothConnectionTypeArray);
            String[] connectionListTypes = context.getResources().getStringArray(R.array.eventBluetoothConnectionTypeValues);
            int index = Arrays.asList(connectionListTypes).indexOf(Integer.toString(this._connectionType));
            descr = descr + ": " + connectionListTypeNames[index] + "; ";

            String  selectedBluetoothNames = context.getString(R.string.pref_event_bluetooth_adapterName) + ": ";
            String[] splits = this._adapterName.split("\\|");
            for (String _bluetoothName : splits) {
                if (_bluetoothName.isEmpty()) {
                    //noinspection StringConcatenationInLoop
                    selectedBluetoothNames = selectedBluetoothNames + context.getString(R.string.applications_multiselect_summary_text_not_selected);
                }
                else
                if (splits.length == 1) {
                    if (_bluetoothName.equals(ALL_BLUETOOTH_NAMES_VALUE))
                        selectedBluetoothNames = selectedBluetoothNames + context.getString(R.string.bluetooth_name_pref_dlg_all_bt_names_chb);
                    else
                    if (_bluetoothName.equals(CONFIGURED_BLUETOOTH_NAMES_VALUE))
                        selectedBluetoothNames = selectedBluetoothNames + context.getString(R.string.bluetooth_name_pref_dlg_configured_bt_names_chb);
                    else {
                        if ((this._connectionType == CTYPE_INFRONT) || (this._connectionType == CTYPE_NOTINFRONT)) {
                            if (Scanner.bluetoothLESupported(context)) {
                                if (this._devicesType == DTYPE_CLASSIC)
                                    selectedBluetoothNames = selectedBluetoothNames + "[CL] ";
                                else if (this._devicesType == DTYPE_LE)
                                    selectedBluetoothNames = selectedBluetoothNames + "[LE] ";
                            }
                        }
                        selectedBluetoothNames = selectedBluetoothNames + _bluetoothName;
                    }
                }
                else {
                    selectedBluetoothNames = context.getString(R.string.applications_multiselect_summary_text_selected);
                    selectedBluetoothNames = selectedBluetoothNames + " " + splits.length;
                    break;
                }
            }
            descr = descr + selectedBluetoothNames;
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        /*if (key.equals(PREF_EVENT_BLUETOOTH_ENABLE_SCANNING_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.menu_settings) + ": " +
                        context.getResources().getString(R.string.phone_profiles_pref_applicationEventBluetoothEnableBluetooth));
            }
        }*/
        if (key.equals(PREF_EVENT_BLUETOOTH_ADAPTER_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String[] splits = value.split("\\|");
                for (String _bluetoothName : splits) {
                    if (_bluetoothName.isEmpty()) {
                        preference.setSummary(R.string.applications_multiselect_summary_text_not_selected);
                    }
                    else
                    if (splits.length == 1) {
                        if (value.equals(ALL_BLUETOOTH_NAMES_VALUE))
                            preference.setSummary(R.string.bluetooth_name_pref_dlg_all_bt_names_chb);
                        else
                        if (value.equals(CONFIGURED_BLUETOOTH_NAMES_VALUE))
                            preference.setSummary(R.string.bluetooth_name_pref_dlg_configured_bt_names_chb);
                        else
                            preference.setSummary(_bluetoothName);
                    }
                    else {
                        String selectedBluetoothNames = context.getString(R.string.applications_multiselect_summary_text_selected);
                        selectedBluetoothNames = selectedBluetoothNames + " " + splits.length;
                        preference.setSummary(selectedBluetoothNames);
                        break;
                    }
                }
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
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

            boolean btLESupported = Scanner.bluetoothLESupported(context);
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
                boolean btLESupported = Scanner.bluetoothLESupported(context);

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

    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_BLUETOOTH_ADAPTER_NAME) ||
            key.equals(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE)||
            key.equals(PREF_EVENT_BLUETOOTH_DEVICES_TYPE))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        //setSummary(prefMng, PREF_EVENT_BLUETOOTH_ENABLE_SCANNING_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_ADAPTER_NAME, preferences, context);
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_BLUETOOTH_DEVICES_TYPE, preferences, context);

        if (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context)
                != PPApplication.PREFERENCE_ALLOWED)
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
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (Event.isEventPreferenceAllowed(PREF_EVENT_BLUETOOTH_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED) {
            EventPreferencesBluetooth tmp = new EventPreferencesBluetooth(this._event, this._enabled, this._adapterName, this._connectionType, this._devicesType);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_CATEGORY);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_CATEGORY);
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
        return super.isRunnable(context) && (!this._adapterName.isEmpty());
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
