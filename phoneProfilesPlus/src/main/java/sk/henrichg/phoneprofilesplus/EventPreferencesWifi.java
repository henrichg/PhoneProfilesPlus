package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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

class EventPreferencesWifi extends EventPreferences {

    String _SSID;
    int _connectionType;

    static final int CTYPE_CONNECTED = 0;
    static final int CTYPE_NEARBY = 1;
    static final int CTYPE_NOT_CONNECTED = 2;
    static final int CTYPE_NOT_NEARBY = 3;

    static final String PREF_EVENT_WIFI_ENABLED = "eventWiFiEnabled";
    static final String PREF_EVENT_WIFI_SSID = "eventWiFiSSID";
    static final String PREF_EVENT_WIFI_CONNECTION_TYPE = "eventWiFiConnectionType";
    static final String PREF_EVENT_WIFI_APP_SETTINGS = "eventEnableWiFiScanningAppSettings";
    static final String PREF_EVENT_WIFI_LOCATION_SYSTEM_SETTINGS = "eventWiFiLocationSystemSettings";
    static final String PREF_EVENT_WIFI_KEEP_ON_SYSTEM_SETTINGS = "eventWiFiKeepOnSystemSettings";

    static final String PREF_EVENT_WIFI_CATEGORY_ROOT = "eventWifiCategoryRoot";
    static final String PREF_EVENT_WIFI_CATEGORY = "eventWifiCategory";

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

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesWifi._enabled;
        this._SSID = fromEvent._eventPreferencesWifi._SSID;
        this._connectionType = fromEvent._eventPreferencesWifi._connectionType;
        this.setSensorPassed(fromEvent._eventPreferencesWifi.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_WIFI_ENABLED, _enabled);
        editor.putString(PREF_EVENT_WIFI_SSID, this._SSID);
        editor.putString(PREF_EVENT_WIFI_CONNECTION_TYPE, String.valueOf(this._connectionType));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_WIFI_ENABLED, false);
        this._SSID = preferences.getString(PREF_EVENT_WIFI_SSID, "");
        this._connectionType = Integer.parseInt(preferences.getString(PREF_EVENT_WIFI_CONNECTION_TYPE, "1"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_wifi_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_WIFI_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_wifi), addPassStatus, DatabaseHandler.ETYPE_WIFI, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                boolean locationErrorDisplayed = false;
                if ((_connectionType == EventPreferencesWifi.CTYPE_NEARBY) ||
                        (_connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY)) {
                    if (!ApplicationPreferences.applicationEventWifiEnableScanning) {
                        if (!ApplicationPreferences.applicationEventWifiDisabledScannigByProfile)
                            _value.append("* ").append(context.getString(R.string.array_pref_applicationDisableScanning_disabled)).append("! *").append(StringConstants.TAG_BREAK_HTML);
                        else
                            _value.append(context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile)).append(StringConstants.TAG_BREAK_HTML);
                    } else if (!GlobalUtils.isLocationEnabled(context.getApplicationContext())) {
                        _value.append("* ").append(context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary)).append("! *").append(StringConstants.TAG_BREAK_HTML);
                        locationErrorDisplayed = true;
                    } else {
                        boolean scanningPaused = ApplicationPreferences.applicationEventWifiScanInTimeMultiply.equals("2") &&
                                GlobalUtils.isNowTimeBetweenTimes(
                                        ApplicationPreferences.applicationEventWifiScanInTimeMultiplyFrom,
                                        ApplicationPreferences.applicationEventWifiScanInTimeMultiplyTo);
                        if (scanningPaused) {
                            _value.append(context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused)).append(StringConstants.TAG_BREAK_HTML);
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= 29) {
                    if ((!locationErrorDisplayed) && (!GlobalUtils.isLocationEnabled(context.getApplicationContext()))) {
                        _value.append("* ").append(context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary)).append("! *").append(StringConstants.TAG_BREAK_HTML);
                    }
                }

                String[] connectionListTypes = context.getResources().getStringArray(R.array.eventWifiConnectionTypeValues);
                int index = Arrays.asList(connectionListTypes).indexOf(Integer.toString(this._connectionType));
                if (index != -1) {
                    _value.append(context.getString(R.string.event_preferences_wifi_connection_type));
                    String[] connectionListTypeNames = context.getResources().getStringArray(R.array.eventWifiConnectionTypeArray);
                    _value.append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(connectionListTypeNames[index], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);
                }

                _value.append(context.getString(R.string.pref_event_wifi_ssid)).append(StringConstants.STR_COLON_WITH_SPACE);
                String selectedSSIDs;// = "";
                StringBuilder value = new StringBuilder();
                String[] splits = this._SSID.split(StringConstants.STR_SPLIT_REGEX);
                for (String _ssid : splits) {
                    if (_ssid.isEmpty()) {
                        //selectedSSIDs = selectedSSIDs + context.getString(R.string.applications_multiselect_summary_text_not_selected);
                        value.append(context.getString(R.string.applications_multiselect_summary_text_not_selected));
                    } else if (splits.length == 1) {
                        switch (_ssid) {
                            case ALL_SSIDS_VALUE:
                                //selectedSSIDs = selectedSSIDs + "[\u00A0" + context.getString(R.string.wifi_ssid_pref_dlg_all_ssids_chb) + "\u00A0]";
                                value.append("[").append(StringConstants.CHAR_HARD_SPACE).append(context.getString(R.string.wifi_ssid_pref_dlg_all_ssids_chb)).append(StringConstants.CHAR_HARD_SPACE).append("]");
                                break;
                            case CONFIGURED_SSIDS_VALUE:
                                //selectedSSIDs = selectedSSIDs + "[\u00A0" + context.getString(R.string.wifi_ssid_pref_dlg_configured_ssids_chb) + "\u00A0]";
                                value.append("[").append(StringConstants.CHAR_HARD_SPACE).append(context.getString(R.string.wifi_ssid_pref_dlg_configured_ssids_chb)).append(StringConstants.CHAR_HARD_SPACE).append("]");
                                break;
                            default:
                                //selectedSSIDs = selectedSSIDs + _ssid;
                                value.append(_ssid);
                                break;
                        }
                    } else {
                        //selectedSSIDs = context.getString(R.string.applications_multiselect_summary_text_selected);
                        //selectedSSIDs = selectedSSIDs + " " + splits.length;
                        value.append(context.getString(R.string.applications_multiselect_summary_text_selected));
                        value.append(" ").append(splits.length);
                        break;
                    }
                }
                selectedSSIDs = value.toString();
                _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(selectedSSIDs, disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_WIFI_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_WIFI_ENABLED) ||
            key.equals(PREF_EVENT_WIFI_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_APP_SETTINGS);
            if (preference != null) {
                String summary;
                int titleColor;
                if (!ApplicationPreferences.applicationEventWifiEnableScanning) {
                    if (!ApplicationPreferences.applicationEventWifiDisabledScannigByProfile) {
                        int connectionType = Integer.parseInt(preferences.getString(PREF_EVENT_WIFI_CONNECTION_TYPE, "1"));
                        if ((connectionType == 1) || (connectionType == 3)) {
                            summary = "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *"+StringConstants.STR_DOUBLE_NEWLINE +
                                    context.getString(R.string.phone_profiles_pref_eventWifiAppSettings_summary);
                            titleColor = ContextCompat.getColor(context, R.color.errorColor);
                        } else {
                            summary = context.getString(R.string.array_pref_applicationDisableScanning_disabled) + StringConstants.STR_DOUBLE_NEWLINE +
                                    context.getString(R.string.phone_profiles_pref_eventWifiAppSettings_summary);
                            titleColor = 0;
                        }
                    }
                    else {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + StringConstants.STR_DOUBLE_NEWLINE +
                                context.getString(R.string.phone_profiles_pref_eventWifiAppSettings_summary);
                        titleColor = 0;
                    }
                }
                else {
                    boolean scanningPaused = ApplicationPreferences.applicationEventWifiScanInTimeMultiply.equals("2") &&
                            GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventWifiScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventWifiScanInTimeMultiplyTo);
                    if (scanningPaused) {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventWifiAppSettings_summary);
                    } else {
                        summary = context.getString(R.string.array_pref_applicationDisableScanning_enabled) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventWifiAppSettings_summary);
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
                if (preferences.getBoolean(PREF_EVENT_WIFI_ENABLED, false)) {
                    if (titleColor != 0)
                        sbt.setSpan(new ForegroundColorSpan(titleColor), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                preference.setTitle(sbt);
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_WIFI_ENABLED) ||
            key.equals(PREF_EVENT_WIFI_LOCATION_SYSTEM_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_LOCATION_SYSTEM_SETTINGS);
            if (preference != null) {
                String summary;
                int titleColor;
                if (Build.VERSION.SDK_INT < 29)
                    summary = context.getString(R.string.phone_profiles_pref_eventWiFiLocationSystemSettings_summary);
                else
                    summary = context.getString(R.string.phone_profiles_pref_eventWiFiLocationSystemSettings_summary_api29);
                if (!GlobalUtils.isLocationEnabled(context.getApplicationContext())) {
                    summary = "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *"+StringConstants.STR_DOUBLE_NEWLINE+
                            summary;
                    titleColor = ContextCompat.getColor(context, R.color.errorColor);
                }
                else {
                    summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT+
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
                if (preferences.getBoolean(PREF_EVENT_WIFI_ENABLED, false)) {
                    if (titleColor != 0)
                        sbt.setSpan(new ForegroundColorSpan(titleColor), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                preference.setTitle(sbt);
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_WIFI_KEEP_ON_SYSTEM_SETTINGS)) {
            if (Build.VERSION.SDK_INT < 27) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    String summary = context.getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_summary);
                    if (GlobalUtils.isWifiSleepPolicySetToNever(context.getApplicationContext())) {
                        summary = context.getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_setToAlways_summary) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                                summary;
                    } else {
                        summary = context.getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_notSetToAlways_summary) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                                summary;
                    }
                    preference.setSummary(summary);
                }
            }
        }
        if (key.equals(PREF_EVENT_WIFI_CONNECTION_TYPE))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesWifi.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesWifi.isRunnable(context);
        //boolean isAllConfigured = event._eventPreferencesWifi.isAllConfigured(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_WIFI_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_SSID);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_WIFI_SSID, "").isEmpty();
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

        if (key.equals(PREF_EVENT_WIFI_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
        if (key.equals(PREF_EVENT_WIFI_SSID) ||
            key.equals(PREF_EVENT_WIFI_CONNECTION_TYPE) ||
            key.equals(PREF_EVENT_WIFI_APP_SETTINGS) ||
            key.equals(PREF_EVENT_WIFI_LOCATION_SYSTEM_SETTINGS) ||
            key.equals(PREF_EVENT_WIFI_KEEP_ON_SYSTEM_SETTINGS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_WIFI_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_WIFI_SSID, preferences, context);
        setSummary(prefMng, PREF_EVENT_WIFI_CONNECTION_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_WIFI_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_WIFI_LOCATION_SYSTEM_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_WIFI_KEEP_ON_SYSTEM_SETTINGS, preferences, context);

        if (EventStatic.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, false, context).preferenceAllowed
                != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_ENABLED);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_WIFI_SSID);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_WIFI_CONNECTION_TYPE);
            if (preference != null) preference.setEnabled(false);
        }

    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_WIFI_ENABLED, false, context);
        if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesWifi tmp = new EventPreferencesWifi(this._event, this._enabled, this._SSID, this._connectionType);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_CATEGORY_ROOT);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_WIFI_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_WIFI_SCANNER).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && tmp.isAllConfigured(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_CATEGORY_ROOT);
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
        return super.isRunnable(context) && (!this._SSID.isEmpty());
    }

    @Override
    boolean isAllConfigured(Context context)
    {
        boolean allConfigured = super.isAllConfigured(context);

        if ((_connectionType == EventPreferencesWifi.CTYPE_NEARBY) ||
                (_connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY)) {
            allConfigured = allConfigured &&
                    (ApplicationPreferences.applicationEventWifiEnableScanning ||
                     ApplicationPreferences.applicationEventWifiDisabledScannigByProfile);
        }

        // locaiton is required also for connected/not connected
        //if ((_connectionType == EventPreferencesWifi.CTYPE_NEARBY) ||
        //        (_connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY)) {
            if (Build.VERSION.SDK_INT >= 29)
                allConfigured = allConfigured && GlobalUtils.isLocationEnabled(context.getApplicationContext());
        //}

        return allConfigured;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_WIFI_ENABLED) != null) {
                //setSummary(prefMng, PREF_EVENT_WIFI_SSID, preferences, context);
                setSummary(prefMng, PREF_EVENT_WIFI_APP_SETTINGS, preferences, context);
                setSummary(prefMng, PREF_EVENT_WIFI_LOCATION_SYSTEM_SETTINGS, preferences, context);
                setSummary(prefMng, PREF_EVENT_WIFI_KEEP_ON_SYSTEM_SETTINGS, preferences, context);
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
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, false, eventsHandler.context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventLocation(context, event, null)*/) {

                // location must be enabled, for get proper connected SSID
                // locaiton is required also for connected/not connected
                //boolean locationNeeded = (_connectionType == EventPreferencesWifi.CTYPE_NEARBY) ||
                //                        (_connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY);
                if ((Build.VERSION.SDK_INT < 29) || /*(!locationNeeded) ||*/ GlobalUtils.isLocationEnabled(eventsHandler.context)) {

                    eventsHandler.wifiPassed = false;

                    WifiManager wifiManager = (WifiManager) eventsHandler.context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (wifiManager == null) {
                        eventsHandler.notAllowedWifi = true;
                    } else {

                        boolean isWifiEnabled = wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;

                        List<WifiSSIDData> wifiConfigurationList = WifiScanWorker.getWifiConfigurationList(eventsHandler.context);

                        boolean done = false;

                        if (isWifiEnabled) {
                            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                            boolean wifiConnected = false;

                            ConnectivityManager connManager = null;
                            try {
                                connManager = (ConnectivityManager) eventsHandler.context.getSystemService(Context.CONNECTIVITY_SERVICE);
                            } catch (Exception e) {
                                // java.lang.NullPointerException: missing IConnectivityManager
                                // Dual SIM?? Bug in Android ???
                                PPApplicationStatic.recordException(e);
                            }
                            if (connManager != null) {
                                Network[] networks = connManager.getAllNetworks();
                                //noinspection ConstantValue,RedundantLengthCheck
                                if ((networks != null) && (networks.length > 0)) {
                                    for (Network network : networks) {
                                        try {
                                            //if (Build.VERSION.SDK_INT < 28) {
                                            NetworkInfo ntkInfo = connManager.getNetworkInfo(network);
                                            if (ntkInfo != null) {
                                                //noinspection deprecation
                                                if (ntkInfo.getType() == ConnectivityManager.TYPE_WIFI && ntkInfo.isConnected()) {
                                                    if (wifiInfo != null) {
                                                        wifiConnected = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        /*} else {
                                            //NetworkInfo networkInfo = connManager.getNetworkInfo(network);
                                            //if ((networkInfo != null) && networkInfo.isConnected()) {
                                                NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                                                if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                                    wifiConnected = WifiNetworkCallback.connected;
                                                    break;
                                                }
                                            //}
                                        }*/
                                        } catch (Exception e) {
//                                        Log.e("EventPreferencesWifi.doHandleEvent", Log.getStackTraceString(e));
                                            PPApplicationStatic.recordException(e);
                                        }
                                    }
                                }
                            }

                            if (wifiConnected) {
                                //Log.e("EventPreferencesWifi.doHandleEvent", "wifiConnected");

                                String[] splits = _SSID.split(StringConstants.STR_SPLIT_REGEX);
                                boolean[] connected = new boolean[splits.length];

                                int i = 0;
                                for (String _ssid : splits) {
                                    connected[i] = false;
                                    switch (_ssid) {
                                        case EventPreferencesWifi.ALL_SSIDS_VALUE:
                                            connected[i] = true;
                                            break;
                                        case EventPreferencesWifi.CONFIGURED_SSIDS_VALUE:
                                            for (WifiSSIDData data : wifiConfigurationList) {
                                                connected[i] = WifiScanWorker.compareSSID(wifiManager, wifiInfo, data.ssid.replace("\"", ""), wifiConfigurationList, eventsHandler.context);
                                                if (connected[i])
                                                    break;
                                            }
                                            break;
                                        default:
                                            connected[i] = WifiScanWorker.compareSSID(wifiManager, wifiInfo, _ssid, wifiConfigurationList, eventsHandler.context);
                                            //Log.e("EventPreferencesWifi.doHandleEvent", "wifiInfo="+wifiInfo.getSSID());
                                            break;
                                    }
                                    i++;
                                }

                                if (_connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED) {
                                    eventsHandler.wifiPassed = true;
                                    for (boolean conn : connected) {
                                        if (conn) {
                                            eventsHandler.wifiPassed = false;
                                            break;
                                        }
                                    }
                                    // not use scanner data
                                    done = true;
                                } else if ((_connectionType == EventPreferencesWifi.CTYPE_CONNECTED) ||
                                        (_connectionType == EventPreferencesWifi.CTYPE_NEARBY)) {
                                    eventsHandler.wifiPassed = false;
                                    for (boolean conn : connected) {
                                        if (conn) {
                                            // when is connected to configured ssid, is also nearby
                                            eventsHandler.wifiPassed = true;
                                            break;
                                        }
                                    }
                                    if (eventsHandler.wifiPassed)
                                        // not use scanner data
                                        done = true;
                                }
                            } else {
                                if ((_connectionType == EventPreferencesWifi.CTYPE_CONNECTED) ||
                                        (_connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED)) {
                                    // not use scanner data
                                    done = true;
                                    eventsHandler.wifiPassed = (_connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED);
                                }
                            }
                        } else {
                            if ((_connectionType == EventPreferencesWifi.CTYPE_CONNECTED) ||
                                    (_connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED)) {
                                // not use scanner data
                                done = true;
                                eventsHandler.wifiPassed = (_connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED);
                            }
                        }

                        if ((_connectionType == EventPreferencesWifi.CTYPE_NEARBY) ||
                                (_connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY)) {
                            if (!done) {
                                if (!ApplicationPreferences.applicationEventWifiEnableScanning) {
                                    //if (forRestartEvents)
                                    //    wifiPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesWifi.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                                    //else
                                    // not allowed for disabled scanning
                                    //    notAllowedWifi = true;
                                    eventsHandler.wifiPassed = false;
                                } else {
                                    //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                                    if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn) {
                                        if (forRestartEvents)
                                            eventsHandler.wifiPassed = (EventPreferences.SENSOR_PASSED_PASSED & getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                                        else {
                                            // not allowed for screen Off
                                            eventsHandler.notAllowedWifi = true;
                                        }
                                    } else {

                                        eventsHandler.wifiPassed = false;

                                        boolean scanningPaused = ApplicationPreferences.applicationEventWifiScanInTimeMultiply.equals("2") &&
                                                GlobalUtils.isNowTimeBetweenTimes(
                                                        ApplicationPreferences.applicationEventWifiScanInTimeMultiplyFrom,
                                                        ApplicationPreferences.applicationEventWifiScanInTimeMultiplyTo);

                                        if (!scanningPaused) {

                                            List<WifiSSIDData> scanResults = WifiScanWorker.getScanResults(eventsHandler.context);

                                            if (scanResults != null) {

                                                for (WifiSSIDData result : scanResults) {
                                                    String[] splits = _SSID.split(StringConstants.STR_SPLIT_REGEX);
                                                    boolean[] nearby = new boolean[splits.length];
                                                    int i = 0;
                                                    for (String _ssid : splits) {
                                                        nearby[i] = false;
                                                        switch (_ssid) {
                                                            case EventPreferencesWifi.ALL_SSIDS_VALUE:
                                                                nearby[i] = true;
                                                                break;
                                                            case EventPreferencesWifi.CONFIGURED_SSIDS_VALUE:
                                                                for (WifiSSIDData data : wifiConfigurationList) {
                                                                    if (WifiScanWorker.compareSSID(result, data.ssid.replace("\"", ""), wifiConfigurationList)) {
                                                                        nearby[i] = true;
                                                                        break;
                                                                    }
                                                                }
                                                                break;
                                                            default:
                                                                if (WifiScanWorker.compareSSID(result, _ssid, wifiConfigurationList)) {
                                                                    nearby[i] = true;
                                                                }
                                                                break;
                                                        }
                                                        i++;
                                                    }

                                                    //noinspection ConstantConditions
                                                    done = false;
                                                    if (_connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY) {
                                                        eventsHandler.wifiPassed = true;
                                                        for (boolean inF : nearby) {
                                                            if (inF) {
                                                                done = true;
                                                                eventsHandler.wifiPassed = false;
                                                                break;
                                                            }
                                                        }
                                                    } else {
                                                        eventsHandler.wifiPassed = false;
                                                        for (boolean inF : nearby) {
                                                            if (inF) {
                                                                done = true;
                                                                eventsHandler.wifiPassed = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if (done)
                                                        break;
                                                }

                                                if (!done) {
                                                    if (scanResults.isEmpty()) {

                                                        if (_connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY)
                                                            eventsHandler.wifiPassed = true;

                                                    }
                                                }

                                            } else {
                                                // not allowed, no scan results
                                                eventsHandler.notAllowedWifi = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else
                    eventsHandler.wifiPassed = false;

                if (!eventsHandler.notAllowedWifi) {
                    if (eventsHandler.wifiPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else {
                eventsHandler.notAllowedWifi = true;
            }
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_WIFI);
            }

        }
    }

}
