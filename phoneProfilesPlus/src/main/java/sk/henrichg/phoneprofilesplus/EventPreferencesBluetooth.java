package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.List;

//import android.preference.CheckBoxPreference;
//import android.preference.ListPreference;
//import android.preference.Preference;
//import android.preference.PreferenceManager;

class EventPreferencesBluetooth extends EventPreferences {

    String _adapterName;
    int _connectionType;
    //int _devicesType;

    static final int CTYPE_CONNECTED = 0;
    static final int CTYPE_NEARBY = 1;
    static final int CTYPE_NOT_CONNECTED = 2;
    static final int CTYPE_NOT_NEARBY = 3;

    //static final int DTYPE_CLASSIC = 0;
    //static final int DTYPE_LE = 1;

    static final String PREF_EVENT_BLUETOOTH_ENABLED = "eventBluetoothEnabled";
    static final String PREF_EVENT_BLUETOOTH_ADAPTER_NAME = "eventBluetoothAdapterNAME";
    private static final String PREF_EVENT_BLUETOOTH_CONNECTION_TYPE = "eventBluetoothConnectionType";
    private static final String PREF_EVENT_BLUETOOTH_DEVICES_TYPE = "eventBluetoothDevicesType";
    static final String PREF_EVENT_BLUETOOTH_APP_SETTINGS = "eventEnableBluetoothScanningAppSettings";
    static final String PREF_EVENT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS = "eventBluetoothLocationSystemSettings";

    private static final String PREF_EVENT_BLUETOOTH_CATEGORY = "eventBluetoothCategoryRoot";


    static final String CONFIGURED_BLUETOOTH_NAMES_VALUE = "^configured_bluetooth_names^";
    static final String ALL_BLUETOOTH_NAMES_VALUE = "%";

    EventPreferencesBluetooth(Event event,
                                    boolean enabled,
                                    String adapterName,
                                    int connectionType/*,
                                    int devicesType*/)
    {
        super(event, enabled);

        this._adapterName = adapterName;
        this._connectionType = connectionType;
        //this._devicesType = devicesType;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesBluetooth._enabled;
        this._adapterName = fromEvent._eventPreferencesBluetooth._adapterName;
        this._connectionType = fromEvent._eventPreferencesBluetooth._connectionType;
        //this._devicesType = fromEvent._eventPreferencesBluetooth._devicesType;
        this.setSensorPassed(fromEvent._eventPreferencesBluetooth.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_BLUETOOTH_ENABLED, _enabled);
        editor.putString(PREF_EVENT_BLUETOOTH_ADAPTER_NAME, this._adapterName);
        editor.putString(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, String.valueOf(this._connectionType));
        //editor.putString(PREF_EVENT_BLUETOOTH_DEVICES_TYPE, String.valueOf(this._devicesType));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_BLUETOOTH_ENABLED, false);
        this._adapterName = preferences.getString(PREF_EVENT_BLUETOOTH_ADAPTER_NAME, "");
        this._connectionType = Integer.parseInt(preferences.getString(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, "1"));
        //this._devicesType = Integer.parseInt(preferences.getString(PREF_EVENT_BLUETOOTH_DEVICES_TYPE, "0"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_bluetooth_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_BLUETOOTH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_bluetooth), addPassStatus, DatabaseHandler.ETYPE_BLUETOOTH, context);
                    descr = descr + "</b> ";
                }

                if ((this._connectionType == 1) || (this._connectionType == 3)) {
                    if (!ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                        if (!ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile)
                            descr = descr + "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *<br>";
                        else
                            descr = descr + context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "<br>";
                    } else if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                        descr = descr + "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *<br>";
                    }
                }

                descr = descr + context.getString(R.string.event_preferences_bluetooth_connection_type);
                String[] connectionListTypeNames = context.getResources().getStringArray(R.array.eventBluetoothConnectionTypeArray);
                String[] connectionListTypes = context.getResources().getStringArray(R.array.eventBluetoothConnectionTypeValues);
                int index = Arrays.asList(connectionListTypes).indexOf(Integer.toString(this._connectionType));
                descr = descr + ": <b>" + connectionListTypeNames[index] + "</b> • ";

                /*
                if ((this._connectionType == CTYPE_NEARBY) || (this._connectionType == CTYPE_NOT_NEARBY)) {
                    if (WifiBluetoothScanner.bluetoothLESupported(context)) {
                        descr = descr + context.getString(R.string.event_preferences_bluetooth_devices_type);
                        String[] deviceTypeListTypeNames = context.getResources().getStringArray(R.array.eventBluetoothDevicesTypeArray);
                        String[] deviceTypeListTypes = context.getResources().getStringArray(R.array.eventBluetoothDevicesTypeValues);
                        index = Arrays.asList(deviceTypeListTypes).indexOf(Integer.toString(this._devicesType));
                        descr = descr + ": <b>" + deviceTypeListTypeNames[index] + "</b> • ";
                    }
                }
                */

                descr = descr + context.getString(R.string.event_preferences_bluetooth_adapter_name) + ": ";
                String selectedBluetoothNames = "";
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
                                /*if ((this._connectionType == CTYPE_NEARBY) || (this._connectionType == CTYPE_NOT_NEARBY)) {
                                    if (WifiBluetoothScanner.bluetoothLESupported(context)) {
                                        if (this._devicesType == DTYPE_CLASSIC)
                                            selectedBluetoothNames = selectedBluetoothNames + "[CL] ";
                                        else if (this._devicesType == DTYPE_LE)
                                            selectedBluetoothNames = selectedBluetoothNames + "[LE] ";
                                    }
                                }*/
                                selectedBluetoothNames = selectedBluetoothNames + _bluetoothName;
                                break;
                        }
                    } else {
                        selectedBluetoothNames = context.getString(R.string.applications_multiselect_summary_text_selected);
                        selectedBluetoothNames = selectedBluetoothNames + " " + splits.length;
                        break;
                    }
                }
                descr = descr + "<b>" + selectedBluetoothNames + "</b>";
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_BLUETOOTH_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_BLUETOOTH_ENABLED) ||
            key.equals(PREF_EVENT_BLUETOOTH_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_APP_SETTINGS);
            if (preference != null) {
                String summary;
                int titleColor;
                if (!ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                    if (!ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile) {
                        summary = "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *\n\n" +
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
                Object[] spansToRemove = sbt.getSpans(0, sTitle.length(), Object.class);
                for(Object span: spansToRemove){
                    if(span instanceof CharacterStyle)
                        sbt.removeSpan(span);
                }
                if (preferences.getBoolean(PREF_EVENT_BLUETOOTH_ENABLED, false)) {
                    if (titleColor != 0)
                        sbt.setSpan(new ForegroundColorSpan(titleColor), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                preference.setTitle(sbt);
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
        /*if (key.equals(PREF_EVENT_BLUETOOTH_ADAPTER_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                //if (!ApplicationPreferences.applicationEventBluetoothEnableScanning(context.getApplicationContext())) {
                //    preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                //            ": "+context.getResources().getString(R.string.preference_not_allowed_reason_not_enabled_scanning));
                //}
                //else {
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
        }*/
        if (key.equals(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }

            boolean btLESupported = BluetoothScanner.bluetoothLESupported(context);
            listPreference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_DEVICES_TYPE);
            if (listPreference != null) {
                if ((!btLESupported) || value.equals("0") || value.equals("2"))
                    listPreference.setEnabled(false);
                else
                    listPreference.setEnabled(true);
            }
        }
        if (key.equals(PREF_EVENT_BLUETOOTH_DEVICES_TYPE))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                boolean btLESupported = BluetoothScanner.bluetoothLESupported(context);

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
        boolean enabled = preferences.getBoolean(PREF_EVENT_BLUETOOTH_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_ADAPTER_NAME);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_BLUETOOTH_ADAPTER_NAME, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, !isRunnable, false);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
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

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
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

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_BLUETOOTH_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesBluetooth tmp = new EventPreferencesBluetooth(this._event, this._enabled, this._adapterName, this._connectionType/*, this._devicesType*/);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_BLUETOOTH_ENABLED, false);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
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
    boolean isRunnable(Context context)
    {
        return super.isRunnable(context) && (!this._adapterName.isEmpty());
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, Context context) {
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

    void doHandleEvent(EventsHandler eventsHandler, boolean forRestartEvents) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorProfilesActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventLocation(context, event, null)
                && Permissions.checkEventBluetoothForEMUI(context, event, null)*/) {
                eventsHandler.bluetoothPassed = false;

                List<BluetoothDeviceData> boundedDevicesList = BluetoothScanWorker.getBoundedDevicesList(eventsHandler.context);

                boolean done = false;

                BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
                if (bluetooth != null) {
                    boolean isBluetoothEnabled = bluetooth.isEnabled();

                    if (isBluetoothEnabled) {
                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "bluetoothEnabled=true");
                            PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "-- eventAdapterName=" + event._eventPreferencesBluetooth._adapterName);
                        }*/

                        //List<BluetoothDeviceData> connectedDevices = BluetoothConnectedDevices.getConnectedDevices(context);
                        BluetoothConnectionBroadcastReceiver.getConnectedDevices(eventsHandler.context);

                        if (BluetoothConnectionBroadcastReceiver.isBluetoothConnected(null, "")) {
                            //if (BluetoothConnectedDevices.isBluetoothConnected(connectedDevices,null, "")) {

                            //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "any device connected");

                            String[] splits = _adapterName.split("\\|");
                            boolean[] connected = new boolean[splits.length];

                            int i = 0;
                            for (String _bluetoothName : splits) {
                                connected[i] = false;
                                switch (_bluetoothName) {
                                    case EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE:
                                        //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "any device connected");
                                        connected[i] = true;
                                        break;
                                    case EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE:
                                        for (BluetoothDeviceData data : boundedDevicesList) {
                                            /*if (PPApplication.logEnabled()) {
                                                PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "boundedDevice.name=" + data.getName());
                                                PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "boundedDevice.address=" + data.getAddress());
                                            }*/
                                            connected[i] = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(data, "");
                                            if (connected[i])
                                                break;
                                        }
                                        //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "paired device connected=" + connected[i]);
                                        break;
                                    default:
                                        connected[i] = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(null, _bluetoothName);
                                        //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "event sensor device connected=" + connected[i]);
                                        break;
                                }
                                i++;
                            }

                            if (_connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED) {
                                eventsHandler.bluetoothPassed = true;
                                for (boolean conn : connected) {
                                    if (conn) {
                                        eventsHandler.bluetoothPassed = false;
                                        break;
                                    }
                                }
                                // not use scanner data
                                done = true;
                            }
                            else
                            if ((_connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) ||
                                (_connectionType == EventPreferencesBluetooth.CTYPE_NEARBY)){
                                eventsHandler.bluetoothPassed = false;
                                if ((_connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) ||
                                    ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                                    for (boolean conn : connected) {
                                        if (conn) {
                                            // when is connected to configured bt name, is also nearby
                                            eventsHandler.bluetoothPassed = true;
                                            break;
                                        }
                                    }
                                }
                                // not use scanner data
                                done = true;
                            }
                        } else {
                            //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "not any device connected");

                            if ((_connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) ||
                                    (_connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED)) {
                                // not use scanner data
                                done = true;
                                eventsHandler.bluetoothPassed = (_connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED);
                            }
                        }
                    } else {
                        //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "bluetoothEnabled=true");

                        if ((_connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) ||
                                (_connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED)) {
                            // not use scanner data
                            done = true;
                            eventsHandler.bluetoothPassed = (_connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED);
                        }
                    }
                }

                //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "bluetoothPassed=" + bluetoothPassed);

                if ((_connectionType == EventPreferencesBluetooth.CTYPE_NEARBY) ||
                        (_connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY)) {
                    if (!done) {
                        if (!ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                            //if (forRestartEvents)
                            //    bluetoothPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesBluetooth.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                            //else
                            // not allowed for disabled scanning
                            //    notAllowedBluetooth = true;
                            eventsHandler.bluetoothPassed = false;
                        } else {
                            //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                            if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn) {
                                if (forRestartEvents)
                                    eventsHandler.bluetoothPassed = (EventPreferences.SENSOR_PASSED_PASSED & getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                                else
                                    // not allowed for screen Off
                                    eventsHandler.notAllowedBluetooth = true;
                            } else {
                                eventsHandler.bluetoothPassed = false;

                                List<BluetoothDeviceData> scanResults = BluetoothScanWorker.getScanResults(eventsHandler.context);

                                if (scanResults != null) {
                                    //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "scanResults.size="+scanResults.size());

                                    for (BluetoothDeviceData device : scanResults) {
                                        /*if (PPApplication.logEnabled()) {
                                            PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "device.getName=" + device.getName());
                                            PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "device.getAddress=" + device.getAddress());
                                        }*/
                                        String[] splits = _adapterName.split("\\|");
                                        boolean[] nearby = new boolean[splits.length];
                                        int i = 0;
                                        for (String _bluetoothName : splits) {
                                            nearby[i] = false;
                                            switch (_bluetoothName) {
                                                case EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE:
                                                    nearby[i] = true;
                                                    break;
                                                case EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE:
                                                    for (BluetoothDeviceData data : boundedDevicesList) {
                                                        String _device = device.getName().toUpperCase();
                                                        String _adapterName = data.getName().toUpperCase();
                                                        if (Wildcard.match(_device, _adapterName, '_', '%', true)) {
                                                            //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "bluetooth found");
                                                            //PPApplication.logE("@@@ EventPreferencesBluetooth.doHandleEvent","bluetoothAdapterName="+device.getName());
                                                            //PPApplication.logE("@@@ EventPreferencesBluetooth.doHandleEvent","bluetoothAddress="+device.getAddress());
                                                            nearby[i] = true;
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                default:
                                                    String _device = device.getName().toUpperCase();
                                                    if ((device.getName() == null) || device.getName().isEmpty()) {
                                                        // scanned device has not name (hidden BT?)
                                                        if ((device.getAddress() != null) && (!device.getAddress().isEmpty())) {
                                                            // device has address
                                                            for (BluetoothDeviceData data : boundedDevicesList) {
                                                                if ((data.getAddress() != null) && data.getAddress().equals(device.getAddress())) {
                                                                    //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "bluetooth found");
                                                                    //PPApplication.logE("@@@ EventPreferencesBluetooth.doHandleEvent","bluetoothAdapterName="+device.getName());
                                                                    //PPApplication.logE("@@@ EventPreferencesBluetooth.doHandleEvent","bluetoothAddress="+device.getAddress());
                                                                    nearby[i] = true;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        String _adapterName = _bluetoothName.toUpperCase();
                                                        if (Wildcard.match(_device, _adapterName, '_', '%', true)) {
                                                            //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "bluetooth found");
                                                            //PPApplication.logE("@@@ EventPreferencesBluetooth.doHandleEvent","bluetoothAdapterName="+device.getName());
                                                            //PPApplication.logE("@@@ EventPreferencesBluetooth.doHandleEvent","bluetoothAddress="+device.getAddress());
                                                            nearby[i] = true;
                                                        }
                                                    }
                                                    break;
                                            }
                                            i++;
                                        }

                                        done = false;
                                        if (_connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY) {
                                            eventsHandler.bluetoothPassed = true;
                                            for (boolean inF : nearby) {
                                                if (inF) {
                                                    done = true;
                                                    eventsHandler.bluetoothPassed = false;
                                                    break;
                                                }
                                            }
                                        }
                                        else {
                                            eventsHandler.bluetoothPassed = false;
                                            for (boolean inF : nearby) {
                                                if (inF) {
                                                    done = true;
                                                    eventsHandler.bluetoothPassed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (done)
                                            break;
                                    }
                                    //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "bluetoothPassed=" + bluetoothPassed);

                                    if (!done) {
                                        if (scanResults.size() == 0) {
                                            //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "scanResult is empty");

                                            if (_connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY)
                                                eventsHandler.wifiPassed = true;
                                        }
                                    }

                                } //else
                                //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "scanResults == null");
                            }
                        }
                    }
                }

                //PPApplication.logE("EventPreferencesBluetooth.doHandleEvent", "bluetoothPassed=" + bluetoothPassed);

                if (!eventsHandler.notAllowedBluetooth) {
                    if (eventsHandler.bluetoothPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedBluetooth = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                //PPApplication.logE("[TEST BATTERY] EventPreferencesBluetooth.doHandleEvent", "bluetooth - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_BLUETOOTH);
            }
        }
    }

}
