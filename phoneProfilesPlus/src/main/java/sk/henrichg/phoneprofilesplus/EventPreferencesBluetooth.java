package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.List;

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
    static final String PREF_EVENT_BLUETOOTH_CONNECTION_TYPE = "eventBluetoothConnectionType";
    private static final String PREF_EVENT_BLUETOOTH_DEVICES_TYPE = "eventBluetoothDevicesType";
    static final String PREF_EVENT_BLUETOOTH_APP_SETTINGS = "eventEnableBluetoothScanningAppSettings";
    static final String PREF_EVENT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS = "eventBluetoothLocationSystemSettings";

    static final String PREF_EVENT_BLUETOOTH_CATEGORY = "eventBluetoothCategoryRoot";


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

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_bluetooth_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_BLUETOOTH_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_bluetooth), addPassStatus, DatabaseHandler.ETYPE_BLUETOOTH, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                boolean locationErrorDisplayed = false;
                if ((this._connectionType == 1) || (this._connectionType == 3)) {
                    if (!ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                        if (!ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile)
                            _value.append("* ").append(context.getString(R.string.array_pref_applicationDisableScanning_disabled)).append("! *").append(StringConstants.TAG_BREAK_HTML);
                        else
                            _value.append(context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile)).append(StringConstants.TAG_BREAK_HTML);
                    } else if (!GlobalUtils.isLocationEnabled(context.getApplicationContext())) {
                        _value.append("* ").append(context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary)).append("! *").append(StringConstants.TAG_BREAK_HTML);
                        locationErrorDisplayed = true;
                    } else {
                        boolean scanningPaused = ApplicationPreferences.applicationEventBluetoothScanInTimeMultiply.equals("2") &&
                                GlobalUtils.isNowTimeBetweenTimes(
                                        ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyFrom,
                                        ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyTo);
                        if (scanningPaused) {
                            _value.append(context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused)).append(StringConstants.TAG_BREAK_HTML);
                        }
                    }
                    if (Build.VERSION.SDK_INT >= 29) {
                        if ((!locationErrorDisplayed) && (!GlobalUtils.isLocationEnabled(context.getApplicationContext()))) {
                            _value.append("* ").append(context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary)).append("! *").append(StringConstants.TAG_BREAK_HTML);
                        }
                    }
                }

                String[] connectionListTypes = context.getResources().getStringArray(R.array.eventBluetoothConnectionTypeValues);
                int index = Arrays.asList(connectionListTypes).indexOf(Integer.toString(this._connectionType));
                if (index != -1) {
                    _value.append(context.getString(R.string.event_preferences_bluetooth_connection_type));
                    String[] connectionListTypeNames = context.getResources().getStringArray(R.array.eventBluetoothConnectionTypeArray);
                    _value.append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(connectionListTypeNames[index], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);
                }

                /*
                if ((this._connectionType == CTYPE_NEARBY) || (this._connectionType == CTYPE_NOT_NEARBY)) {
                    if (WifiBluetoothScanner.bluetoothLESupported(context)) {
                        descr = descr + context.getString(R.string.event_preferences_bluetooth_devices_type);
                        String[] deviceTypeListTypeNames = context.getResources().getStringArray(R.array.eventBluetoothDevicesTypeArray);
                        String[] deviceTypeListTypes = context.getResources().getStringArray(R.array.eventBluetoothDevicesTypeValues);
                        index = Arrays.asList(deviceTypeListTypes).indexOf(Integer.toString(this._devicesType));
                        descr = descr + ": <b>" + getColorForChangedPreferenceValue(deviceTypeListTypeNames[index], disabled, context) + "</b> • ";
                    }
                }
                */

                _value.append(context.getString(R.string.event_preferences_bluetooth_adapter_name)).append(StringConstants.STR_COLON_WITH_SPACE);
                String selectedBluetoothNames;// = "";
                StringBuilder value = new StringBuilder();
                String[] splits = this._adapterName.split(StringConstants.STR_SPLIT_REGEX);
                for (String _bluetoothName : splits) {
                    if (_bluetoothName.isEmpty()) {
                        //selectedBluetoothNames = selectedBluetoothNames + context.getString(R.string.applications_multiselect_summary_text_not_selected);
                        value.append(context.getString(R.string.applications_multiselect_summary_text_not_selected));
                    } else if (splits.length == 1) {
                        switch (_bluetoothName) {
                            case ALL_BLUETOOTH_NAMES_VALUE:
                                //selectedBluetoothNames = selectedBluetoothNames + "[\u00A0" + context.getString(R.string.bluetooth_name_pref_dlg_all_bt_names_chb) + "\u00A0]";
                                value.append("[").append(StringConstants.CHAR_HARD_SPACE).append(context.getString(R.string.bluetooth_name_pref_dlg_all_bt_names_chb)).append(StringConstants.CHAR_HARD_SPACE).append("]");
                                break;
                            case CONFIGURED_BLUETOOTH_NAMES_VALUE:
                                //selectedBluetoothNames = selectedBluetoothNames + "[\u00A0" + context.getString(R.string.bluetooth_name_pref_dlg_configured_bt_names_chb) + "\u00A0]";
                                value.append("[").append(StringConstants.CHAR_HARD_SPACE).append(context.getString(R.string.bluetooth_name_pref_dlg_configured_bt_names_chb)).append(StringConstants.CHAR_HARD_SPACE).append("]");
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
                                //selectedBluetoothNames = selectedBluetoothNames + _bluetoothName;
                                value.append(_bluetoothName);
                                break;
                        }
                    } else {
                        //selectedBluetoothNames = context.getString(R.string.applications_multiselect_summary_text_selected);
                        //selectedBluetoothNames = selectedBluetoothNames + " " + splits.length;
                        value.append(context.getString(R.string.applications_multiselect_summary_text_selected));
                        value.append(" ").append(splits.length);
                        break;
                    }
                }
                selectedBluetoothNames = value.toString();
                _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(selectedBluetoothNames, disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_BLUETOOTH_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
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
                        int connectionType = Integer.parseInt(preferences.getString(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, "1"));
                        if ((connectionType == 1) || (connectionType == 3)) {
                            summary = "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *"+StringConstants.STR_DOUBLE_NEWLINE +
                                    context.getString(R.string.phone_profiles_pref_eventBluetoothAppSettings_summary);
                            titleColor = ContextCompat.getColor(context, R.color.errorColor);
                        } else {
                            summary = context.getString(R.string.array_pref_applicationDisableScanning_disabled) + StringConstants.STR_DOUBLE_NEWLINE +
                                    context.getString(R.string.phone_profiles_pref_eventBluetoothAppSettings_summary);
                            titleColor = 0;
                        }
                    }
                    else {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + StringConstants.STR_DOUBLE_NEWLINE +
                                context.getString(R.string.phone_profiles_pref_eventBluetoothAppSettings_summary);
                        titleColor = 0;
                    }
                }
                else {
                    boolean scanningPaused = ApplicationPreferences.applicationEventBluetoothScanInTimeMultiply.equals("2") &&
                            GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyTo);
                    if (scanningPaused) {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventBluetoothAppSettings_summary);
                    } else {
                        summary = context.getString(R.string.array_pref_applicationDisableScanning_enabled) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventBluetoothAppSettings_summary);
                    }
                    titleColor = 0;
                }
                CharSequence sTitle = preference.getTitle();
                int titleLenght = 0;
                if (sTitle != null)
                    titleLenght = sTitle.length();
                Spannable sbt = new SpannableString(sTitle);
                Object[] spansToRemove = sbt.getSpans(0, titleLenght, Object.class);
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
        if (key.equals(PREF_EVENT_BLUETOOTH_ENABLED) ||
            key.equals(PREF_EVENT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
            if (preference != null) {
                String summary = context.getString(R.string.phone_profiles_pref_eventBluetoothLocationSystemSettings_summary);
                int titleColor;
                if (!GlobalUtils.isLocationEnabled(context.getApplicationContext())) {
                    int connectionType = Integer.parseInt(preferences.getString(PREF_EVENT_BLUETOOTH_CONNECTION_TYPE, "1"));
                    if ((connectionType == 1) || (connectionType == 3)) {
                        summary = "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *" + StringConstants.STR_DOUBLE_NEWLINE +
                                summary;
                        titleColor = ContextCompat.getColor(context, R.color.errorColor);
                    }
                    else {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + StringConstants.STR_DOUBLE_NEWLINE +
                                summary;
                        titleColor = 0;
                    }
                } else {
                    summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                            summary;
                    titleColor = 0;
                }
                CharSequence sTitle = preference.getTitle();
                int titleLenght = 0;
                if (sTitle != null)
                    titleLenght = sTitle.length();
                Spannable sbt = new SpannableString(sTitle);
                Object[] spansToRemove = sbt.getSpans(0, titleLenght, Object.class);
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
        /*if (key.equals(PREF_EVENT_BLUETOOTH_ADAPTER_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                //if (!ApplicationPreferences.applicationEventBluetoothEnableScanning(context.getApplicationContext())) {
                //    preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                //            ": "+context.getString(R.string.preference_not_allowed_reason_not_enabled_scanning));
                //}
                //else {
                    String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
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
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }

            boolean btLESupported = BluetoothScanner.bluetoothLESupported(/*context*/);
            listPreference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_DEVICES_TYPE);
            if (listPreference != null) {
                listPreference.setEnabled((btLESupported) && !value.equals("0") && !value.equals("2"));
            }
        }
        if (key.equals(PREF_EVENT_BLUETOOTH_DEVICES_TYPE))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                boolean btLESupported = BluetoothScanner.bluetoothLESupported(/*context*/);

                if (!btLESupported) {
                    listPreference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                            StringConstants.STR_COLON_WITH_SPACE+context.getString(R.string.preference_not_allowed_reason_no_hardware));
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
        //boolean isAllConfigured = event._eventPreferencesBluetooth.isAllConfigured(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_BLUETOOTH_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_ADAPTER_NAME);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_BLUETOOTH_ADAPTER_NAME, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_BLUETOOTH_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
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

        if (EventStatic.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, false, context).preferenceAllowed
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
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_BLUETOOTH_ENABLED, false, context);
        if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesBluetooth tmp = new EventPreferencesBluetooth(this._event, this._enabled, this._adapterName, this._connectionType/*, this._devicesType*/);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_BLUETOOTH_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && tmp.isAllConfigured(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_BLUETOOTH_CATEGORY);
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
        return super.isRunnable(context) && (!this._adapterName.isEmpty());
    }

    @Override
    boolean isAllConfigured(Context context)
    {
        boolean allConfigured = super.isAllConfigured(context);

        if ((this._connectionType == 1) || (this._connectionType == 3)) {
            allConfigured = allConfigured &&
                    (ApplicationPreferences.applicationEventBluetoothEnableScanning ||
                     ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile);
        }

        if (Build.VERSION.SDK_INT >= 29) {
            if ((this._connectionType == 1) || (this._connectionType == 3))
                allConfigured = allConfigured && GlobalUtils.isLocationEnabled(context.getApplicationContext());
        }

        return allConfigured;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_BLUETOOTH_ENABLED) != null) {
                //setSummary(prefMng, PREF_EVENT_BLUETOOTH_ADAPTER_NAME, preferences, context);
                setSummary(prefMng, PREF_EVENT_BLUETOOTH_APP_SETTINGS, preferences, context);
                setSummary(prefMng, PREF_EVENT_BLUETOOTH_LOCATION_SYSTEM_SETTINGS, preferences, context);
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

    void doHandleEvent(EventsHandler eventsHandler, boolean forRestartEvents) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, false, eventsHandler.context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventLocation(context, event, null)
                && Permissions.checkEventBluetoothForEMUI(context, event, null)*/) {
                eventsHandler.bluetoothPassed = false;

                List<BluetoothDeviceData> boundedDevicesList = BluetoothScanWorker.getBoundedDevicesList(eventsHandler.context);

                boolean done = false;

                BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
                if (bluetooth != null) {
                    boolean isBluetoothEnabled = bluetooth.isEnabled();

                    if (isBluetoothEnabled) {
                        BluetoothConnectionBroadcastReceiver.getConnectedDevices(eventsHandler.context);

                        if (BluetoothConnectionBroadcastReceiver.isBluetoothConnected(null, "")) {
//                            PPApplicationStatic.logE("EventPreferencesBluetooth.doHandleEvent", "bluetooth is connected  event="+_event._name);

                            String[] splits = _adapterName.split(StringConstants.STR_SPLIT_REGEX);
                            boolean[] connected = new boolean[splits.length];

                            int i = 0;
                            for (String _bluetoothName : splits) {
                                connected[i] = false;
                                switch (_bluetoothName) {
                                    case EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE:
                                        connected[i] = true;
                                        break;
                                    case EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE:
                                    default:
                                        for (BluetoothDeviceData data : boundedDevicesList) {
                                            connected[i] = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(data, "");
                                            if (connected[i])
                                                break;
                                        }
                                        connected[i] = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(null, _bluetoothName);
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
                                for (boolean conn : connected) {
                                    if (conn) {
                                        // when is connected to configured bt name, is also nearby
                                        eventsHandler.bluetoothPassed = true;
                                        break;
                                    }
                                }
                                if (eventsHandler.bluetoothPassed)
                                    // not use scanner data
                                    done = true;
                            }
                        } else {
//                            PPApplicationStatic.logE("EventPreferencesBluetooth.doHandleEvent", "bluetooth is NOT connected  event="+_event._name);
                            if ((_connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) ||
                                (_connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED)) {
                                // not use scanner data
                                done = true;
                                eventsHandler.bluetoothPassed = (_connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED);
                            }
                        }
//                        PPApplicationStatic.logE("EventPreferencesBluetooth.doHandleEvent", "eventsHandler.bluetoothPassed="+eventsHandler.bluetoothPassed);
                    } else {
                        if ((_connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) ||
                            (_connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED)) {
                            // not use scanner data
                            done = true;
                            eventsHandler.bluetoothPassed = (_connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED);
                        }
                    }
                }

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
                            if (GlobalUtils.isLocationEnabled(eventsHandler.context)) {

                                //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                                if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn) {
                                    if (forRestartEvents)
                                        eventsHandler.bluetoothPassed = (EventPreferences.SENSOR_PASSED_PASSED & getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                                    else
                                        // not allowed for screen Off
                                        eventsHandler.notAllowedBluetooth = true;
                                } else {
                                    eventsHandler.bluetoothPassed = false;

                                    boolean scanningPaused = ApplicationPreferences.applicationEventBluetoothScanInTimeMultiply.equals("2") &&
                                            GlobalUtils.isNowTimeBetweenTimes(
                                                    ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyFrom,
                                                    ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyTo);

                                    if (!scanningPaused) {
                                        List<BluetoothDeviceData> scanResults = BluetoothScanWorker.getScanResults(eventsHandler.context);

                                        if (scanResults != null) {
                                            for (BluetoothDeviceData device : scanResults) {
                                                String[] splits = _adapterName.split(StringConstants.STR_SPLIT_REGEX);
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
                                                                if ((!_device.isEmpty()) &&
                                                                        (!_adapterName.isEmpty()) &&
                                                                        Wildcard.match(_device, _adapterName, '_', '%', true)) {
                                                                    nearby[i] = true;
                                                                    break;
                                                                }
                                                            }
                                                            break;
                                                        default:
                                                        /* Removed, hidden BT are not supported
                                                        if ((device.getName() == null) || device.getName().isEmpty()) {
                                                            // scanned device has not name (hidden BT?)
                                                            String deviceAddress = device.getAddress();
                                                            if ((deviceAddress != null) && (!deviceAddress.isEmpty())) {
                                                                // device has address
                                                                for (BluetoothDeviceData data : boundedDevicesList) {
                                                                    String dataAddress = data.getAddress();
                                                                    if ((dataAddress != null) &&
                                                                            (!dataAddress.isEmpty()) &&
                                                                            dataAddress.equals(deviceAddress)) {
                                                                        nearby[i] = true;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        } else*/
                                                        {
                                                            String _deviceName = device.getName().toUpperCase();
                                                            String _adapterName = _bluetoothName.toUpperCase();
                                                            if ((!_adapterName.isEmpty()) &&
                                                                    (!_deviceName.isEmpty()) &&
                                                                    Wildcard.match(_deviceName, _adapterName, '_', '%', true)) {
                                                                nearby[i] = true;
                                                            }
                                                        }
                                                        break;
                                                    }
                                                    i++;
                                                }

                                                //noinspection ConstantConditions
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
                                                } else {
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

                                            if (!done) {
                                                if (scanResults.isEmpty()) {
                                                    if (_connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY)
                                                        eventsHandler.bluetoothPassed = true;
                                                }
                                            }

                                        }
                                    }
                                }
                            } else
                                eventsHandler.bluetoothPassed = false;
                        }
                    }
                }

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
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_BLUETOOTH);
            }
        }
    }

}
