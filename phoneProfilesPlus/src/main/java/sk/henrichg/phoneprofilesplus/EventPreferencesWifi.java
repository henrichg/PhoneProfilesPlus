package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
//import android.preference.CheckBoxPreference;
//import android.preference.ListPreference;
//import android.preference.Preference;
//import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import java.util.Arrays;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesWifi extends EventPreferences {

    String _SSID;
    int _connectionType;

    static final int CTYPE_CONNECTED = 0;
    static final int CTYPE_INFRONT = 1;
    static final int CTYPE_NOTCONNECTED = 2;
    static final int CTYPE_NOTINFRONT = 3;

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

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesWifi._enabled;
        this._SSID = fromEvent._eventPreferencesWifi._SSID;
        this._connectionType = fromEvent._eventPreferencesWifi._connectionType;
        this.setSensorPassed(fromEvent._eventPreferencesWifi.getSensorPassed());
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_WIFI_ENABLED, _enabled);
        editor.putString(PREF_EVENT_WIFI_SSID, this._SSID);
        editor.putString(PREF_EVENT_WIFI_CONNECTION_TYPE, String.valueOf(this._connectionType));
        editor.apply();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_WIFI_ENABLED, false);
        this._SSID = preferences.getString(PREF_EVENT_WIFI_SSID, "");
        this._connectionType = Integer.parseInt(preferences.getString(PREF_EVENT_WIFI_CONNECTION_TYPE, "1"));
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
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
                    descr = descr + ": </b>";
                }

                if (!ApplicationPreferences.applicationEventWifiEnableScanning(context)) {
                    if (!ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(context))
                        descr = descr + "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *<br>";
                    else
                        descr = descr + context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "<br>";
                }
                else
                if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                    descr = descr + "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *<br>";
                }

                descr = descr + context.getString(R.string.pref_event_wifi_connectionType);
                String[] connectionListTypeNames = context.getResources().getStringArray(R.array.eventWifiConnectionTypeArray);
                String[] connectionListTypes = context.getResources().getStringArray(R.array.eventWifiConnectionTypeValues);
                int index = Arrays.asList(connectionListTypes).indexOf(Integer.toString(this._connectionType));
                descr = descr + ": " + connectionListTypeNames[index] + " • ";

                String selectedSSIDs = context.getString(R.string.pref_event_wifi_ssid) + ": ";
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
                descr = descr + selectedSSIDs;
            }
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_WIFI_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preference.isChecked(), true, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_WIFI_ENABLED) ||
            key.equals(PREF_EVENT_WIFI_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_APP_SETTINGS);
            if (preference != null) {
                String summary;
                int titleColor;
                if (!ApplicationPreferences.applicationEventWifiEnableScanning(context)) {
                    if (!ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(context)) {
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
                SwitchPreferenceCompat enabledPreference = prefMng.findPreference(PREF_EVENT_WIFI_ENABLED);
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
        SwitchPreferenceCompat enabledPreference = prefMng.findPreference(PREF_EVENT_WIFI_ENABLED);
        boolean enabled = (enabledPreference != null) && enabledPreference.isChecked();
        Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_SSID);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_WIFI_SSID, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, true, !isRunnable, false);
        }

    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
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

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
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

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_WIFI_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesWifi tmp = new EventPreferencesWifi(this._event, this._enabled, this._SSID, this._connectionType);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_WIFI_CATEGORY);
            if (preference != null) {
                SwitchPreferenceCompat enabledPreference = prefMng.findPreference(PREF_EVENT_WIFI_ENABLED);
                boolean enabled = (enabledPreference != null) && enabledPreference.isChecked();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, true, false, !tmp.isRunnable(context), false);
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
    public boolean isRunnable(Context context)
    {
        return super.isRunnable(context) && (!this._SSID.isEmpty());
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
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
