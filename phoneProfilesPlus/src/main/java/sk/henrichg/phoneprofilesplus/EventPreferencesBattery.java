package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
//import android.preference.CheckBoxPreference;
//import android.preference.ListPreference;
//import android.preference.Preference;
//import android.preference.Preference.OnPreferenceChangeListener;
//import android.preference.PreferenceManager;
//import android.preference.MultiSelectListPreference;
import android.os.BatteryManager;
import android.widget.Toast;

//import com.crashlytics.android.Crashlytics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
//import me.drakeet.support.toast.ToastCompat;

class EventPreferencesBattery extends EventPreferences {

    int _levelLow;
    int _levelHight;
    int _charging;
    String _plugged;
    boolean _powerSaveMode;

    static final String PREF_EVENT_BATTERY_ENABLED = "eventBatteryEnabled";
    private static final String PREF_EVENT_BATTERY_LEVEL_LOW = "eventBatteryLevelLow";
    private static final String PREF_EVENT_BATTERY_LEVEL_HIGHT = "eventBatteryLevelHight";
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
            splits = this._plugged.split("\\|");
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

    @SuppressWarnings("StringConcatenationInLoop")
    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_battery_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_BATTERY_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_battery), addPassStatus, DatabaseHandler.ETYPE_BATTERY, context);
                    descr = descr + "</b> ";
                }

                descr = descr + context.getString(R.string.pref_event_battery_level);
                descr = descr + ": <b>" + this._levelLow + "% - " + this._levelHight + "%</b>";

                if (this._powerSaveMode)
                    descr = descr + " • <b>" + context.getString(R.string.pref_event_battery_power_save_mode) + "</b>";
                else {
                    descr = descr + " • " + context.getString(R.string.pref_event_battery_charging);
                    String[] charging = context.getResources().getStringArray(R.array.eventBatteryChargingArray);
                    descr = descr + ": <b>" + charging[this._charging] + "</b>";

                    String selectedPlugged = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                    if ((this._plugged != null) && !this._plugged.isEmpty() && !this._plugged.equals("-")) {
                        String[] splits = this._plugged.split("\\|");
                        String[] pluggedValues = context.getResources().getStringArray(R.array.eventBatteryPluggedValues);
                        String[] pluggedNames = context.getResources().getStringArray(R.array.eventBatteryPluggedArray);
                        selectedPlugged = "";
                        for (String s : splits) {
                            if (!selectedPlugged.isEmpty())
                                selectedPlugged = selectedPlugged + ", ";
                            try {
                                selectedPlugged = selectedPlugged + pluggedNames[Arrays.asList(pluggedValues).indexOf(s)];
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                                //Crashlytics.logException(e);
                            }
                        }
                    }
                    descr = descr + " • " + context.getString(R.string.event_preferences_battery_plugged) + ": <b>" + selectedPlugged + "</b>";
                }
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value/*, Context context*/)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_BATTERY_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_BATTERY_LEVEL_LOW) || key.equals(PREF_EVENT_BATTERY_LEVEL_HIGHT))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null)
                preference.setSummary(value + "%");
        }
        if (key.equals(PREF_EVENT_BATTERY_CHARGING)) {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
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
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, true, false, false);
            }
        }
        if (key.equals(PREF_EVENT_BATTERY_POWER_SAVE_MODE)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_BATTERY_ENABLED) ||
            key.equals(PREF_EVENT_BATTERY_POWER_SAVE_MODE)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true" : "false"/*, context*/);
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
                for (String s : set) {
                    if (!s.isEmpty()) {
                        if (!plugged.isEmpty())
                            plugged = plugged + ", ";
                        plugged = plugged + pluggedNames[Arrays.asList(pluggedValues).indexOf(s)];
                    }
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
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_BATTERY_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesBattery tmp = new EventPreferencesBattery(this._event, this._enabled, this._levelLow, this._levelHight, this._charging, this._powerSaveMode, this._plugged);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_BATTERY_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_BATTERY_ENABLED, false);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_BATTERY_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, Context context)
    {
        final Preference lowLevelPreference = prefMng.findPreference(PREF_EVENT_BATTERY_LEVEL_LOW);
        final Preference hightLevelPreference = prefMng.findPreference(PREF_EVENT_BATTERY_LEVEL_HIGHT);
        final ListPreference chargingPreference = prefMng.findPreference(PREF_EVENT_BATTERY_CHARGING);
        final SwitchPreferenceCompat powerSaveModePreference = prefMng.findPreference(PREF_EVENT_BATTERY_POWER_SAVE_MODE);
        final MultiSelectListPreference pluggedPreference = prefMng.findPreference(PREF_EVENT_BATTERY_PLUGGED);
        final PreferenceManager _prefMng = prefMng;
        final Context _context = context.getApplicationContext();

        if (lowLevelPreference != null) {
            lowLevelPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String sNewValue = (String) newValue;
                    int iNewValue;
                    if (sNewValue.isEmpty())
                        iNewValue = 0;
                    else
                        iNewValue = Integer.parseInt(sNewValue);

                    String sHightLevelValue = _prefMng.getSharedPreferences().getString(PREF_EVENT_BATTERY_LEVEL_HIGHT, "100");
                    int iHightLevelValue;
                    if (sHightLevelValue.isEmpty())
                        iHightLevelValue = 100;
                    else
                        iHightLevelValue = Integer.parseInt(sHightLevelValue);

                    boolean OK = ((iNewValue >= 0) && (iNewValue <= iHightLevelValue));

                    if (!OK) {
                        PPApplication.showToast(_context.getApplicationContext(),
                                _context.getResources().getString(R.string.event_preferences_battery_level_low) + ": " +
                                        _context.getResources().getString(R.string.event_preferences_battery_level_bad_value),
                                Toast.LENGTH_SHORT);
                    }

                    return OK;
                }
            });
        }

        if (hightLevelPreference != null) {
            hightLevelPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String sNewValue = (String) newValue;
                    int iNewValue;
                    if (sNewValue.isEmpty())
                        iNewValue = 100;
                    else
                        iNewValue = Integer.parseInt(sNewValue);

                    String sLowLevelValue = _prefMng.getSharedPreferences().getString(PREF_EVENT_BATTERY_LEVEL_LOW, "0");
                    int iLowLevelValue;
                    if (sLowLevelValue.isEmpty())
                        iLowLevelValue = 0;
                    else
                        iLowLevelValue = Integer.parseInt(sLowLevelValue);

                    boolean OK = ((iNewValue >= iLowLevelValue) && (iNewValue <= 100));

                    if (!OK) {
                        PPApplication.showToast(_context.getApplicationContext(),
                                _context.getResources().getString(R.string.event_preferences_battery_level_hight) + ": " +
                                        _context.getResources().getString(R.string.event_preferences_battery_level_bad_value),
                                Toast.LENGTH_SHORT);
                    }

                    return OK;
                }
            });
        }

        if ((chargingPreference != null) && (powerSaveModePreference != null)  && (pluggedPreference != null)) {
            chargingPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String sNewValue = (String) newValue;
                    if (!sNewValue.equals("0"))
                        powerSaveModePreference.setChecked(false);
                    return true;
                }
            });
            pluggedPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue != null)
                        powerSaveModePreference.setChecked(false);
                    return true;
                }
            });
            powerSaveModePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean bNewValue = (boolean) newValue;
                    if (bNewValue) {
                        chargingPreference.setValue("0");
                        Set<String> uncheckValues = new HashSet<>();
                        pluggedPreference.setValues(uncheckValues);
                    }
                    return true;
                }
            });
        }
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
            if (Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(eventsHandler.context);
                //PPApplication.logE("EventPreferencesBattery.doHandleEvent", "isPowerSaveMode=" + isPowerSaveMode);

                boolean isCharging;
                int batteryPct;
                int plugged;

                // get battery status
                Intent batteryStatus = null;
                try { // Huawei devices: java.lang.IllegalArgumentException: registered too many Broadcast Receivers
                    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    batteryStatus = eventsHandler.context.registerReceiver(null, filter);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                    //Crashlytics.logException(e);
                }

                if (batteryStatus != null) {
                    eventsHandler.batteryPassed = false;

                    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    //PPApplication.logE("EventPreferencesBattery.doHandleEvent", "status=" + status);
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;
                    //PPApplication.logE("EventPreferencesBattery.doHandleEvent", "isCharging=" + isCharging);
                    plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    //PPApplication.logE("EventPreferencesBattery.doHandleEvent", "plugged=" + plugged);

                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("EventPreferencesBattery.doHandleEvent", "level=" + level);
                        PPApplication.logE("EventPreferencesBattery.doHandleEvent", "scale=" + scale);
                    }*/

                    batteryPct = Math.round(level / (float) scale * 100);
                    //PPApplication.logE("EventPreferencesBattery.doHandleEvent", "batteryPct=" + batteryPct);

                    if ((batteryPct >= _levelLow) &&
                            (batteryPct <= _levelHight))
                        eventsHandler.batteryPassed = true;

                    if ((_charging > 0) ||
                            ((_plugged != null) &&
                                    (!_plugged.isEmpty()))){
                        if (_charging == 1)
                            eventsHandler.batteryPassed = eventsHandler.batteryPassed && isCharging;
                        else
                        if (_charging == 2)
                            eventsHandler.batteryPassed = eventsHandler.batteryPassed && (!isCharging);
                        //PPApplication.logE("EventPreferencesBattery.doHandleEvent", "event._eventPreferencesBattery._plugged=" + event._eventPreferencesBattery._plugged);
                        if ((_plugged != null) &&
                                (!_plugged.isEmpty())) {
                            String[] splits = _plugged.split("\\|");
                            //PPApplication.logE("EventPreferencesBattery.doHandleEvent", "splits.length=" + splits.length);
                            if (splits.length > 0) {
                                boolean passed = false;
                                for (String split : splits) {
                                    try {
                                        int plug = Integer.parseInt(split);
                                        //PPApplication.logE("EventPreferencesBattery.doHandleEvent", "plug=" + plug);
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
                                        //Crashlytics.logException(e);
                                    }
                                }
                                eventsHandler.batteryPassed = eventsHandler.batteryPassed && passed;
                            }
                        }
                    } else if (_powerSaveMode)
                        eventsHandler.batteryPassed = eventsHandler.batteryPassed && isPowerSaveMode;
                } else
                    eventsHandler.notAllowedBattery = true;

                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("EventPreferencesBattery.doHandleEvent", "notAllowedBattery=" + notAllowedBattery);
                    PPApplication.logE("EventPreferencesBattery.doHandleEvent", "batteryPassed=" + batteryPassed);
                }*/

                if (!eventsHandler.notAllowedBattery) {
                    if (eventsHandler.batteryPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedBattery = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                //PPApplication.logE("[TEST BATTERY] EventPreferencesBattery.doHandleEvent", "battery - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_BATTERY);
            }
        }
    }

}
