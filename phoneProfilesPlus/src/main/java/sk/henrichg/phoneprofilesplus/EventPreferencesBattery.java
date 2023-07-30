package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.BatteryManager;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class EventPreferencesBattery extends EventPreferences {

    int _levelLow;
    int _levelHight;
    int _charging;
    String _plugged;
    boolean _powerSaveMode;

    static final String PREF_EVENT_BATTERY_ENABLED = "eventBatteryEnabled";
    static final String PREF_EVENT_BATTERY_LEVEL_LOW = "eventBatteryLevelLow";
    static final String PREF_EVENT_BATTERY_LEVEL_HIGHT = "eventBatteryLevelHight";
    private static final String PREF_EVENT_BATTERY_CHARGING = "eventBatteryCharging";
    private static final String PREF_EVENT_BATTERY_PLUGGED = "eventBatteryPlugged";
    private static final String PREF_EVENT_BATTERY_POWER_SAVE_MODE = "eventBatteryPowerSaveMode";
    static final String PREF_EVENT_BATTERY_BATTERY_SAVER_SYSTEM_SETTINGS = "eventBatteryBatterySaver";

    private static final String PREF_EVENT_BATTERY_CATEGORY = "eventBatteryCategoryRoot";

    EventPreferencesBattery(Event event,
                                    boolean enabled,
                                    int levelLow,
                                    int levelHight,
                                    int charging,
                                    boolean powerSaveMode,
                                    String plugged)
    {
        super(event, enabled);

        this._levelLow = levelLow;
        this._levelHight = levelHight;
        this._charging = charging;
        this._plugged = plugged;
        this._powerSaveMode = powerSaveMode;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesBattery._enabled;
        this._levelLow = fromEvent._eventPreferencesBattery._levelLow;
        this._levelHight = fromEvent._eventPreferencesBattery._levelHight;
        this._charging = fromEvent._eventPreferencesBattery._charging;
        this._plugged = fromEvent._eventPreferencesBattery._plugged;
        this._powerSaveMode = fromEvent._eventPreferencesBattery._powerSaveMode;
        this.setSensorPassed(fromEvent._eventPreferencesBattery.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_BATTERY_ENABLED, _enabled);
        editor.putString(PREF_EVENT_BATTERY_LEVEL_LOW, String.valueOf(this._levelLow));
        editor.putString(PREF_EVENT_BATTERY_LEVEL_HIGHT, String.valueOf(this._levelHight));
        //editor.putBoolean(PREF_EVENT_BATTERY_CHARGING, this._charging);
        editor.putString(PREF_EVENT_BATTERY_CHARGING, String.valueOf(this._charging));

        String[] splits;
        if (this._plugged != null)
            splits = this._plugged.split(StringConstants.STR_SPLIT_REGEX);
        else
            splits = new String[]{};
        Set<String> set = new HashSet<>(Arrays.asList(splits));
        editor.putStringSet(PREF_EVENT_BATTERY_PLUGGED, set);

        editor.putBoolean(PREF_EVENT_BATTERY_POWER_SAVE_MODE, this._powerSaveMode);
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_BATTERY_ENABLED, false);

        String sLevel;
        int iLevel;

        sLevel = preferences.getString(PREF_EVENT_BATTERY_LEVEL_LOW, "0");
        if (sLevel.isEmpty()) sLevel = "0";
        iLevel = Integer.parseInt(sLevel);
        if ((iLevel < 0) || (iLevel > 100)) iLevel = 0;
        this._levelLow= iLevel;

        sLevel = preferences.getString(PREF_EVENT_BATTERY_LEVEL_HIGHT, "100");
        if (sLevel.isEmpty()) sLevel = "100";
        iLevel = Integer.parseInt(sLevel);
        if ((iLevel < 0) || (iLevel > 100)) iLevel = 100;
        this._levelHight= iLevel;

        //this._charging = preferences.getBoolean(PREF_EVENT_BATTERY_CHARGING, false);
        this._charging = Integer.parseInt(preferences.getString(PREF_EVENT_BATTERY_CHARGING, "0"));

        Set<String> set = preferences.getStringSet(PREF_EVENT_BATTERY_PLUGGED, null);
        StringBuilder plugged = new StringBuilder();
        if (set != null) {
            for (String s : set) {
                if (plugged.length() > 0)
                    plugged.append("|");
                plugged.append(s);
            }
        }
        this._plugged = plugged.toString();

        this._powerSaveMode = preferences.getBoolean(PREF_EVENT_BATTERY_POWER_SAVE_MODE, false);
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_battery_summary);
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_BATTERY_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + StringConstants.TAG_BOLD_START_HTML;
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_battery), addPassStatus, DatabaseHandler.ETYPE_BATTERY, context);
                    descr = descr + StringConstants.TAG_BOLD_END_HTML+" ";
                }

                descr = descr + context.getString(R.string.pref_event_battery_level);
                descr = descr + ": "+StringConstants.TAG_BOLD_START_HTML + getColorForChangedPreferenceValue(this._levelLow + "% - " + this._levelHight + "%", disabled, context) + StringConstants.TAG_BOLD_END_HTML;

                if (this._powerSaveMode)
                    descr = descr + StringConstants.STR_DOT+StringConstants.TAG_BOLD_START_HTML + getColorForChangedPreferenceValue(context.getString(R.string.pref_event_battery_power_save_mode), disabled, context) + StringConstants.TAG_BOLD_END_HTML;
                else {
                    descr = descr + StringConstants.STR_DOT + context.getString(R.string.pref_event_battery_charging);
                    String[] charging = context.getResources().getStringArray(R.array.eventBatteryChargingArray);
                    descr = descr + ": "+StringConstants.TAG_BOLD_START_HTML + getColorForChangedPreferenceValue(charging[this._charging], disabled, context) + StringConstants.TAG_BOLD_END_HTML;

                    String selectedPlugged = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                    if ((this._plugged != null) && !this._plugged.isEmpty() && !this._plugged.equals("-")) {
                        String[] splits = this._plugged.split(StringConstants.STR_SPLIT_REGEX);
                        List<String> pluggedValues = Arrays.asList(context.getResources().getStringArray(R.array.eventBatteryPluggedValues));
                        String[] pluggedNames = context.getResources().getStringArray(R.array.eventBatteryPluggedArray);
                        //selectedPlugged = "";
                        StringBuilder value = new StringBuilder();
                        for (String s : splits) {
                            int idx = pluggedValues.indexOf(s);
                            if (idx != -1) {
                                //if (!selectedPlugged.isEmpty())
                                //    selectedPlugged = selectedPlugged + ", ";
                                //selectedPlugged = selectedPlugged + pluggedNames[idx];
                                if (value.length() > 0)
                                    value.append(", ");
                                value.append(pluggedNames[idx]);
                            }
                        }
                        selectedPlugged = value.toString();
                    }
                    descr = descr + StringConstants.STR_DOT + context.getString(R.string.event_preferences_battery_plugged) + ": "+StringConstants.TAG_BOLD_START_HTML + getColorForChangedPreferenceValue(selectedPlugged, disabled, context) + StringConstants.TAG_BOLD_END_HTML;
                }
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value/*, Context context*/)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_BATTERY_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        /*if (key.equals(PREF_EVENT_BATTERY_LEVEL_LOW) || key.equals(PREF_EVENT_BATTERY_LEVEL_HIGHT))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null)
                preference.setSummary(value + "%");
        }*/
        if (key.equals(PREF_EVENT_BATTERY_CHARGING)) {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_BATTERY_PLUGGED)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);

                Set<String> set = prefMng.getSharedPreferences().getStringSet(PREF_EVENT_BATTERY_PLUGGED, null);
                StringBuilder plugged = new StringBuilder();
                if (set != null) {
                    for (String s : set) {
                        if (plugged.length() > 0)
                            plugged.append("|");
                        plugged.append(s);
                    }
                }
                boolean bold = plugged.length() > 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, false, false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_BATTERY_POWER_SAVE_MODE)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_BATTERY_ENABLED) ||
            key.equals(PREF_EVENT_BATTERY_POWER_SAVE_MODE)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING/*, context*/);
        }
        if (key.equals(PREF_EVENT_BATTERY_LEVEL_LOW) ||
            key.equals(PREF_EVENT_BATTERY_LEVEL_HIGHT) ||
            key.equals(PREF_EVENT_BATTERY_CHARGING))
        {
            setSummary(prefMng, key, preferences.getString(key, "")/*, context*/);
        }

        if (key.equals(PREF_EVENT_BATTERY_PLUGGED)) {
            Set<String> set = preferences.getStringSet(key, null);
            String plugged = "";
            if (set != null) {
                String[] pluggedValues = context.getResources().getStringArray(R.array.eventBatteryPluggedValues);
                String[] pluggedNames = context.getResources().getStringArray(R.array.eventBatteryPluggedArray);
                StringBuilder _plugged = new StringBuilder();
                for (String s : set) {
                    if (!s.isEmpty()) {
                        int pos = Arrays.asList(pluggedValues).indexOf(s);
                        if (pos != -1) {
                            //if (!plugged.isEmpty())
                            //    plugged = plugged + ", ";
                            //plugged = plugged + pluggedNames[pos];
                            if (_plugged.length() > 0)
                                _plugged.append(", ");
                            _plugged.append(pluggedNames[pos]);
                        }
                    }
                    plugged = _plugged.toString();
                }
                if (plugged.isEmpty())
                    plugged = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            }
            else
                plugged = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            setSummary(prefMng, key, plugged/*, context*/);
        }

    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_BATTERY_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_BATTERY_LEVEL_LOW, preferences, context);
        setSummary(prefMng, PREF_EVENT_BATTERY_LEVEL_HIGHT, preferences, context);
        setSummary(prefMng, PREF_EVENT_BATTERY_CHARGING, preferences, context);
        setSummary(prefMng, PREF_EVENT_BATTERY_PLUGGED, preferences, context);
        setSummary(prefMng, PREF_EVENT_BATTERY_POWER_SAVE_MODE, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_BATTERY_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesBattery tmp = new EventPreferencesBattery(this._event, this._enabled, this._levelLow, this._levelHight, this._charging, this._powerSaveMode, this._plugged);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_BATTERY_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_BATTERY_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_BATTERY).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false, false, false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_BATTERY_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_BATTERY_ENABLED) != null) {
                final Preference lowLevelPreference = prefMng.findPreference(PREF_EVENT_BATTERY_LEVEL_LOW);
                final Preference hightLevelPreference = prefMng.findPreference(PREF_EVENT_BATTERY_LEVEL_HIGHT);
                final PPListPreference chargingPreference = prefMng.findPreference(PREF_EVENT_BATTERY_CHARGING);
                final SwitchPreferenceCompat powerSaveModePreference = prefMng.findPreference(PREF_EVENT_BATTERY_POWER_SAVE_MODE);
                final PPMultiSelectListPreference pluggedPreference = prefMng.findPreference(PREF_EVENT_BATTERY_PLUGGED);
                final PreferenceManager _prefMng = prefMng;
                final Context _context = context.getApplicationContext();

                if (lowLevelPreference != null) {
                    lowLevelPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                        String sNewValue = (String) newValue;
                        int iNewValue;
                        if (sNewValue.isEmpty())
                            iNewValue = 0;
                        else
                            iNewValue = Integer.parseInt(sNewValue);

                        String sHightLevelValue = "100";
                        if (_prefMng.getSharedPreferences() != null)
                            sHightLevelValue = _prefMng.getSharedPreferences().getString(PREF_EVENT_BATTERY_LEVEL_HIGHT, "100");
                        int iHightLevelValue;
                        if (sHightLevelValue.isEmpty())
                            iHightLevelValue = 100;
                        else
                            iHightLevelValue = Integer.parseInt(sHightLevelValue);

                        boolean OK = ((iNewValue >= 0) && (iNewValue <= iHightLevelValue));

                        if (!OK) {
                            PPApplication.showToast(_context.getApplicationContext(),
                                    _context.getString(R.string.event_preferences_battery_level_low) + ": " +
                                            _context.getString(R.string.event_preferences_battery_level_bad_value),
                                    Toast.LENGTH_SHORT);
                        }

                        return OK;
                    });
                }

                if (hightLevelPreference != null) {
                    hightLevelPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                        String sNewValue = (String) newValue;
                        int iNewValue;
                        if (sNewValue.isEmpty())
                            iNewValue = 100;
                        else
                            iNewValue = Integer.parseInt(sNewValue);

                        String sLowLevelValue = "0";
                        if (_prefMng.getSharedPreferences() != null)
                            sLowLevelValue = _prefMng.getSharedPreferences().getString(PREF_EVENT_BATTERY_LEVEL_LOW, "0");
                        int iLowLevelValue;
                        if (sLowLevelValue.isEmpty())
                            iLowLevelValue = 0;
                        else
                            iLowLevelValue = Integer.parseInt(sLowLevelValue);

                        boolean OK = ((iNewValue >= iLowLevelValue) && (iNewValue <= 100));

                        if (!OK) {
                            PPApplication.showToast(_context.getApplicationContext(),
                                    _context.getString(R.string.event_preferences_battery_level_hight) + ": " +
                                            _context.getString(R.string.event_preferences_battery_level_bad_value),
                                    Toast.LENGTH_SHORT);
                        }

                        return OK;
                    });
                }

                if ((chargingPreference != null) && (powerSaveModePreference != null) && (pluggedPreference != null)) {
                    chargingPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                        String sNewValue = (String) newValue;
                        if (!sNewValue.equals("0"))
                            powerSaveModePreference.setChecked(false);
                        return true;
                    });
                    pluggedPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                        if (newValue != null)
                            powerSaveModePreference.setChecked(false);
                        return true;
                    });
                    powerSaveModePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                        boolean bNewValue = (boolean) newValue;
                        if (bNewValue) {
                            chargingPreference.setValue("0");
                            Set<String> uncheckValues = new HashSet<>();
                            pluggedPreference.setValues(uncheckValues);
                        }
                        return true;
                    });
                }

                setSummary(prefMng, PREF_EVENT_BATTERY_ENABLED, preferences, context);
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

    void doHandleEvent(EventsHandler eventsHandler/*, String sensorType, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if (EventStatic.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(eventsHandler.context);

                boolean isCharging = false;
                int batteryPct;
                int plugged = 0;

                // get battery status
                try { // Huawei devices: java.lang.IllegalArgumentException: registered too many Broadcast Receivers
                    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = eventsHandler.context.registerReceiver(null, filter);

                    if (batteryStatus != null) {
                        eventsHandler.batteryPassed = false;

                        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                status == BatteryManager.BATTERY_STATUS_FULL;
                        plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

                        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                        batteryPct = Math.round(level / (float) scale * 100);

                        if ((batteryPct >= _levelLow) &&
                                (batteryPct <= _levelHight))
                            eventsHandler.batteryPassed = true;
                    } else
                        eventsHandler.notAllowedBattery = true;

                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                    eventsHandler.notAllowedBattery = true;
                }

                if ((_charging > 0) ||
                        (/*(!sensorType.equals(EventsHandler.SENSOR_TYPE_BATTERY)) &&*/
                                (_plugged != null) && (!_plugged.isEmpty()))) {
                    if (_charging == 1)
                        eventsHandler.batteryPassed = eventsHandler.batteryPassed && isCharging;
                    else
                    if (_charging == 2)
                        eventsHandler.batteryPassed = eventsHandler.batteryPassed && (!isCharging);
                    if (/*(!sensorType.equals(EventsHandler.SENSOR_TYPE_BATTERY)) &&*/
                            (_plugged != null) && (!_plugged.isEmpty())) {
                        String[] splits = _plugged.split(StringConstants.STR_SPLIT_REGEX);
                        if (splits.length > 0) {
                            boolean passed = false;
                            for (String split : splits) {
                                try {
                                    int plug = Integer.parseInt(split);
                                    if ((plug == 1) && (plugged == BatteryManager.BATTERY_PLUGGED_AC)) {
                                        passed = true;
                                        break;
                                    }
                                    if ((plug == 2) && (plugged == BatteryManager.BATTERY_PLUGGED_USB)) {
                                        passed = true;
                                        break;
                                    }
                                    if ((plug == 3) && (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS)) {
                                        passed = true;
                                        break;
                                    }
                                } catch (Exception e) {
                                    //PPApplicationStatic.recordException(e);
                                }
                            }
                            eventsHandler.batteryPassed = eventsHandler.batteryPassed && passed;
                        }
                    }
                } else if (_powerSaveMode)
                    eventsHandler.batteryPassed = eventsHandler.batteryPassed && isPowerSaveMode;

                if (!eventsHandler.notAllowedBattery) {
                    if (eventsHandler.batteryPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedBattery = true;

//            PPApplicationStatic.logE("[IN_EVENTS_HANDLER] EventPreferencesBattery.doHandleEvent", "event="+_event._name);
//            PPApplicationStatic.logE("[IN_EVENTS_HANDLER] EventPreferencesBattery.doHandleEvent", "eventsHandler.batteryPassed="+eventsHandler.batteryPassed);
//            PPApplicationStatic.logE("[IN_EVENTS_HANDLER] EventPreferencesBattery.doHandleEvent", "eventsHandler.notAllowedBattery="+eventsHandler.notAllowedBattery);

            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_BATTERY);
            }
        }
    }

}
