package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
//import android.preference.CheckBoxPreference;
//import android.preference.ListPreference;
//import android.preference.Preference;
//import android.preference.PreferenceManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import com.crashlytics.android.Crashlytics;

import java.util.Arrays;
import java.util.List;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesWifi extends EventPreferences {

    String _SSID;
    int _connectionType;

    static final int CTYPE_CONNECTED = 0;
    static final int CTYPE_NEARBY = 1;
    static final int CTYPE_NOT_CONNECTED = 2;
    static final int CTYPE_NOT_NEARBY = 3;

    static final String PREF_EVENT_WIFI_ENABLED = "eventWiFiEnabled";
    static final String PREF_EVENT_WIFI_SSID = "eventWiFiSSID";
    private static final String PREF_EVENT_WIFI_CONNECTION_TYPE = "eventWiFiConnectionType";
    static final String PREF_EVENT_WIFI_APP_SETTINGS = "eventEnableWiFiScanningAppSettings";
    static final String PREF_EVENT_WIFI_LOCATION_SYSTEM_SETTINGS = "eventWiFiLocationSystemSettings";
    static final String PREF_EVENT_WIFI_KEEP_ON_SYSTEM_SETTINGS = "eventWiFiKeepOnSystemSettings";

    private static final String PREF_EVENT_WIFI_CATEGORY = "eventWifiCategoryRoot";

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

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_wifi_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_WIFI_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_wifi), addPassStatus, DatabaseHandler.ETYPE_WIFI, context);
                    descr = descr + "</b> ";
                }

                if ((this._connectionType == 1) || (this._connectionType == 3)) {
                    if (!ApplicationPreferences.applicationEventWifiEnableScanning) {
                        if (!ApplicationPreferences.applicationEventWifiDisabledScannigByProfile)
                            descr = descr + "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *<br>";
                        else
                            descr = descr + context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "<br>";
                    } else if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                        descr = descr + "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *<br>";
                    }
                }

                descr = descr + context.getString(R.string.event_preferences_wifi_connection_type);
                String[] connectionListTypeNames = context.getResources().getStringArray(R.array.eventWifiConnectionTypeArray);
                String[] connectionListTypes = context.getResources().getStringArray(R.array.eventWifiConnectionTypeValues);
                int index = Arrays.asList(connectionListTypes).indexOf(Integer.toString(this._connectionType));
                descr = descr + ": <b>" + connectionListTypeNames[index] + "</b> • ";

                descr = descr + context.getString(R.string.pref_event_wifi_ssid) + ": ";
                String selectedSSIDs = "";
                String[] splits = this._SSID.split("\\|");
                for (String _ssid : splits) {
                    if (_ssid.isEmpty()) {
                        //noinspection StringConcatenationInLoop
                        selectedSSIDs = selectedSSIDs + context.getString(R.string.applications_multiselect_summary_text_not_selected);
                    } else if (splits.length == 1) {
                        switch (_ssid) {
                            case ALL_SSIDS_VALUE:
                                selectedSSIDs = selectedSSIDs + context.getString(R.string.wifi_ssid_pref_dlg_all_ssids_chb);
                                break;
                            case CONFIGURED_SSIDS_VALUE:
                                selectedSSIDs = selectedSSIDs + context.getString(R.string.wifi_ssid_pref_dlg_configured_ssids_chb);
                                break;
                            default:
                                selectedSSIDs = selectedSSIDs + _ssid;
                                break;
                        }
                    } else {
                        selectedSSIDs = context.getString(R.string.applications_multiselect_summary_text_selected);
                        selectedSSIDs = selectedSSIDs + " " + splits.length;
                        break;
                    }
                }
                descr = descr + "<b>" + selectedSSIDs + "</b>";
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_WIFI_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
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
                        summary = "* " + context.getResources().getString(R.string.phone_profiles_pref_applicationEventScanningDisabled) + " *\n\n" +
                                context.getResources().getString(R.string.phone_profiles_pref_eventWifiAppSettings_summary);
                        titleColor = Color.RED; //0xFFffb000;
                    }
                    else {
                        summary = context.getResources().getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "\n\n" +
                                context.getResources().getString(R.string.phone_profiles_pref_eventWifiAppSettings_summary);
                        titleColor = 0;
                    }
                }
                else {
                    summary = context.getResources().getString(R.string.array_pref_applicationDisableScanning_enabled) + ".\n\n" +
                            context.getResources().getString(R.string.phone_profiles_pref_eventWifiAppSettings_summary);
                    titleColor = 0;
                }
                CharSequence sTitle = preference.getTitle();
                Spannable sbt = new SpannableString(sTitle);
                Object[] spansToRemove = sbt.getSpans(0, sTitle.length(), Object.class);
                for(Object span: spansToRemove){
                    if(span instanceof CharacterStyle)
                        sbt.removeSpan(span);
                }
                if (preferences.getBoolean(PREF_EVENT_WIFI_ENABLED, false)) {
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
        if (key.equals(PREF_EVENT_WIFI_LOCATION_SYSTEM_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary = context.getString(R.string.phone_profiles_pref_eventWiFiLocationSystemSettings_summary);
                if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                    summary = "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *\n\n"+
                            summary;
                }
                else {
                    summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n"+
                            summary;
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_WIFI_KEEP_ON_SYSTEM_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary = context.getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_summary);
                if (PhoneProfilesService.isWifiSleepPolicySetToNever(context.getApplicationContext())) {
                    summary = context.getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_setToAlways_summary) + ".\n\n"+
                            summary;
                }
                else {
                    summary = context.getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_notSetToAlways_summary) + ".\n\n"+
                            summary;
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_WIFI_CONNECTION_TYPE))
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
        event._eventPreferencesWifi.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesWifi.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_WIFI_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_SSID);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_WIFI_SSID, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, !isRunnable, false);
        }

    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_WIFI_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
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

        if (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context).allowed
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
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_WIFI_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesWifi tmp = new EventPreferencesWifi(this._event, this._enabled, this._SSID, this._connectionType);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_WIFI_ENABLED, false);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_CATEGORY);
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
        return super.isRunnable(context) && (!this._SSID.isEmpty());
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, Context context) {
        /*final boolean enabled = ApplicationPreferences.applicationEventWifiEnableScanning(context.getApplicationContext());
        Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_SSID);
        if (preference != null) preference.setEnabled(enabled);*/
        SharedPreferences preferences = prefMng.getSharedPreferences();
        //setSummary(prefMng, PREF_EVENT_WIFI_SSID, preferences, context);
        setSummary(prefMng, PREF_EVENT_WIFI_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_WIFI_LOCATION_SYSTEM_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_WIFI_KEEP_ON_SYSTEM_SETTINGS, preferences, context);
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
            if ((Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorProfilesActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventLocation(context, event, null)*/) {
                //if (event._name.equals("Doma"))
                //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "-------- eventSSID=" + event._eventPreferencesWifi._SSID);

                eventsHandler.wifiPassed = false;

                WifiManager wifiManager = (WifiManager) eventsHandler.context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager == null) {
                    eventsHandler.notAllowedWifi = true;
                }
                else {

                    boolean isWifiEnabled = wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;

                    List<WifiSSIDData> wifiConfigurationList = WifiScanWorker.getWifiConfigurationList(eventsHandler.context);

                    boolean done = false;

                    if (isWifiEnabled) {
                        //if (event._name.equals("Doma"))
                        //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiStateEnabled=true");

                        //PPApplication.logE("----- EventsHandler.doHandleEvents","-- eventSSID="+event._eventPreferencesWifi._SSID);

                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                        boolean wifiConnected = false;

                        ConnectivityManager connManager = null;
                        try {
                            connManager = (ConnectivityManager) eventsHandler.context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        } catch (Exception e) {
                            // java.lang.NullPointerException: missing IConnectivityManager
                            // Dual SIM?? Bug in Android ???
                            Crashlytics.logException(e);
                        }
                        if (connManager != null) {
                            //if (android.os.Build.VERSION.SDK_INT >= 21) {
                            Network[] networks = connManager.getAllNetworks();
                            if ((networks != null) && (networks.length > 0)) {
                                for (Network network : networks) {
                                    try {
                                        if (Build.VERSION.SDK_INT < 28) {
                                            NetworkInfo ntkInfo = connManager.getNetworkInfo(network);
                                            if (ntkInfo != null) {
                                                if (ntkInfo.getType() == ConnectivityManager.TYPE_WIFI && ntkInfo.isConnected()) {
                                                    if (wifiInfo != null) {
                                                        wifiConnected = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        } else {
                                            NetworkInfo networkInfo = connManager.getNetworkInfo(network);
                                            if ((networkInfo != null) && networkInfo.isConnected()) {
                                                NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                                                if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                                    wifiConnected = true;
                                                    break;
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        Crashlytics.logException(e);
                                    }
                                }
                            }
                            /*} else {
                                //noinspection deprecation
                                NetworkInfo ntkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                                wifiConnected = (ntkInfo != null) && ntkInfo.isConnected();
                            }*/
                        }

                        if (wifiConnected) {
                            /*if (PPApplication.logEnabled()) {
                                if (event._name.equals("Doma")) {
                                    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifi connected");
                                    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiSSID="+wifiInfo.getSSID());
                                    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiBSSID=" + wifiInfo.getBSSID());
                                }
                            }*/

                            //PPApplication.logE("----- EventsHandler.doHandleEvents","SSID="+event._eventPreferencesWifi._SSID);

                            String[] splits = _SSID.split("\\|");
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
                                            connected[i] = WifiScanWorker.compareSSID(wifiManager, wifiInfo, data.ssid.replace("\"", ""), wifiConfigurationList);
                                            if (connected[i])
                                                break;
                                        }
                                        break;
                                    default:
                                        connected[i] = WifiScanWorker.compareSSID(wifiManager, wifiInfo, _ssid, wifiConfigurationList);
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
                            } else if (_connectionType == EventPreferencesWifi.CTYPE_CONNECTED) {
                                eventsHandler.wifiPassed = false;
                                for (boolean conn : connected) {
                                    if (conn) {
                                        eventsHandler.wifiPassed = true;
                                        break;
                                    }
                                }
                                // not use scanner data
                                done = true;
                            }
                        } else {
                            //if (event._name.equals("Doma"))
                            //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifi not connected");

                            if ((_connectionType == EventPreferencesWifi.CTYPE_CONNECTED) ||
                                    (_connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED)) {
                                // not use scanner data
                                done = true;
                                eventsHandler.wifiPassed = (_connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED);
                            }
                        }
                    } else {
                        //if (event._name.equals("Doma"))
                        //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiStateEnabled=false");
                        if ((_connectionType == EventPreferencesWifi.CTYPE_CONNECTED) ||
                                (_connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED)) {
                            // not use scanner data
                            done = true;
                            eventsHandler.wifiPassed = (_connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED);
                        }
                    }

                    //if (event._name.equals("Doma"))
                    //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiPassed - connected =" + wifiPassed);

                    if ((_connectionType == EventPreferencesWifi.CTYPE_NEARBY) ||
                            (_connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY)) {
                        //noinspection ConstantConditions
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
                                    else
                                        // not allowed for screen Off
                                        eventsHandler.notAllowedWifi = true;
                                } else {

                                    eventsHandler.wifiPassed = false;

                                    List<WifiSSIDData> scanResults = WifiScanWorker.getScanResults(eventsHandler.context);

                                    //PPApplication.logE("----- EventsHandler.doHandleEvents","scanResults="+scanResults);

                                    if (scanResults != null) {
                                        /*if (PPApplication.logEnabled()) {
                                            if (event._name.equals("Doma")) {
                                                PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "scanResults != null");
                                                PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "scanResults.size=" + scanResults.size());
                                                //PPApplication.logE("----- EventsHandler.doHandleEvents","-- eventSSID="+event._eventPreferencesWifi._SSID);
                                            }
                                        }*/

                                        for (WifiSSIDData result : scanResults) {
                                            /*if (PPApplication.logEnabled()) {
                                                if (event._name.equals("Doma")) {
                                                    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "scanSSID=" + result.ssid);
                                                    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "scanBSSID=" + result.bssid);
                                                }
                                            }*/
                                            String[] splits = _SSID.split("\\|");
                                            boolean[] nearby = new boolean[splits.length];
                                            int i = 0;
                                            for (String _ssid : splits) {
                                                nearby[i] = false;
                                                switch (_ssid) {
                                                    case EventPreferencesWifi.ALL_SSIDS_VALUE:
                                                        //if (event._name.equals("Doma"))
                                                        //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "all ssids");
                                                        nearby[i] = true;
                                                        break;
                                                    case EventPreferencesWifi.CONFIGURED_SSIDS_VALUE:
                                                        //if (event._name.equals("Doma"))
                                                        //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "configured ssids");
                                                        for (WifiSSIDData data : wifiConfigurationList) {
                                                            if (WifiScanWorker.compareSSID(result, data.ssid.replace("\"", ""), wifiConfigurationList)) {
                                                                /*if (PPApplication.logEnabled()) {
                                                                    if (event._name.equals("Doma")) {
                                                                        PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "configured SSID=" + data.ssid.replace("\"", ""));
                                                                        PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifi found");
                                                                    }
                                                                }*/
                                                                nearby[i] = true;
                                                                break;
                                                            }
                                                        }
                                                        break;
                                                    default:
                                                        if (WifiScanWorker.compareSSID(result, _ssid, wifiConfigurationList)) {
                                                            /*if (PPApplication.logEnabled()) {
                                                                if (event._name.equals("Doma")) {
                                                                    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "event SSID=" + event._eventPreferencesWifi._SSID);
                                                                    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifi found");
                                                                }
                                                            }*/
                                                            nearby[i] = true;
                                                        }
                                                        break;
                                                }
                                                i++;
                                            }

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
                                        //if (event._name.equals("Doma"))
                                        //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiPassed - in front =" + wifiPassed);

                                        if (!done) {
                                            if (scanResults.size() == 0) {
                                                //if (event._name.equals("Doma"))
                                                //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "scanResult is empty");

                                                if (_connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY)
                                                    eventsHandler.wifiPassed = true;

                                                //if (event._name.equals("Doma"))
                                                //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiPassed - in front - for empty scanResult =" + wifiPassed);
                                            }
                                        }

                                    } /*else
                                    if (event._name.equals("Doma"))
                                        PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "scanResults == null");*/
                                }
                            }
                        }
                    }
                }

                /*if (PPApplication.logEnabled()) {
                    if (event._name.equals("Doma")) {
                        PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "------- wifiPassed=" + wifiPassed);
                        PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "------- notAllowedWifi=" + notAllowedWifi);
                    }
                }*/

                if (!eventsHandler.notAllowedWifi) {
                    if (eventsHandler.wifiPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedWifi = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            //PPApplication.logE("[TEST BATTERY] EventsHandler.doHandleEvents", "wifi - event._name="+event._name);
            //PPApplication.logE("[TEST BATTERY] EventsHandler.doHandleEvents", "wifi - old pass="+oldSensorPassed);
            //PPApplication.logE("[TEST BATTERY] EventsHandler.doHandleEvents", "wifi - new pass="+newSensorPassed);
            if (oldSensorPassed != newSensorPassed) {
                //PPApplication.logE("[TEST BATTERY] EventsHandler.doHandleEvents", "wifi - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_WIFI);
            }
        }
    }

}
