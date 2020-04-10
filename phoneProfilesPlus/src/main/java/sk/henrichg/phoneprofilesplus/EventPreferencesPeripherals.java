package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
//import android.preference.CheckBoxPreference;
//import android.preference.ListPreference;
//import android.preference.Preference;
//import android.preference.PreferenceManager;

class EventPreferencesPeripherals extends EventPreferences {

    int _peripheralType;

    static final String PREF_EVENT_PERIPHERAL_ENABLED = "eventPeripheralEnabled";
    private static final String PREF_EVENT_PERIPHERAL_TYPE = "eventPeripheralType";

    private static final String PREF_EVENT_PERIPHERAL_CATEGORY = "eventAccessoriesCategoryRoot";

    private static final int PERIPHERAL_TYPE_DESK_DOCK = 0;
    private static final int PERIPHERAL_TYPE_CAR_DOCK = 1;
    private static final int PERIPHERAL_TYPE_WIRED_HEADSET = 2;
    private static final int PERIPHERAL_TYPE_BLUETOOTH_HEADSET = 3;
    private static final int PERIPHERAL_TYPE_HEADPHONES = 4;

    EventPreferencesPeripherals(Event event,
                                    boolean enabled,
                                    int peripheralType)
    {
        super(event, enabled);

        this._peripheralType = peripheralType;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesPeripherals._enabled;
        this._peripheralType = fromEvent._eventPreferencesPeripherals._peripheralType;
        this.setSensorPassed(fromEvent._eventPreferencesPeripherals.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_PERIPHERAL_ENABLED, _enabled);
        editor.putString(PREF_EVENT_PERIPHERAL_TYPE, String.valueOf(this._peripheralType));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_PERIPHERAL_ENABLED, false);
        this._peripheralType = Integer.parseInt(preferences.getString(PREF_EVENT_PERIPHERAL_TYPE, "0"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_accessories_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_PERIPHERAL_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_peripheral), addPassStatus, DatabaseHandler.ETYPE_PERIPHERAL, context);
                    descr = descr + "</b> ";
                }

                descr = descr + context.getString(R.string.event_preferences_peripheral_type) + ": ";
                String[] peripheralTypes = context.getResources().getStringArray(R.array.eventPeripheralTypeArray);
                descr = descr + "<b>" + peripheralTypes[this._peripheralType] + "</b>";
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value/*, Context context*/)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_PERIPHERAL_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_PERIPHERAL_TYPE))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences,
                    @SuppressWarnings("unused") Context context)
    {
        if (key.equals(PREF_EVENT_PERIPHERAL_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false"/*, context*/);
        }
        if (key.equals(PREF_EVENT_PERIPHERAL_TYPE))
        {
            setSummary(prefMng, key, preferences.getString(key, "")/*, context*/);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_PERIPHERAL_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_PERIPHERAL_TYPE, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_PERIPHERAL_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesPeripherals tmp = new EventPreferencesPeripherals(this._event, this._enabled, this._peripheralType);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_PERIPHERAL_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_PERIPHERAL_ENABLED, false);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_PERIPHERAL_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
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
            if (Event.isEventPreferenceAllowed(EventPreferencesPeripherals.PREF_EVENT_PERIPHERAL_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if ((_peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_DESK_DOCK) ||
                        (_peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_CAR_DOCK)) {
                    // get dock status
                    IntentFilter iFilter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
                    Intent dockStatus = eventsHandler.context.registerReceiver(null, iFilter);

                    if (dockStatus != null) {
                        int dockState = dockStatus.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
                        boolean isDocked = dockState != Intent.EXTRA_DOCK_STATE_UNDOCKED;
                        boolean isCar = dockState == Intent.EXTRA_DOCK_STATE_CAR;
                        boolean isDesk = dockState == Intent.EXTRA_DOCK_STATE_DESK ||
                                dockState == Intent.EXTRA_DOCK_STATE_LE_DESK ||
                                dockState == Intent.EXTRA_DOCK_STATE_HE_DESK;

                        if (isDocked) {
                            if ((_peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_DESK_DOCK)
                                    && isDesk)
                                eventsHandler.peripheralPassed = true;
                            else
                                eventsHandler.peripheralPassed = (_peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_CAR_DOCK)
                                        && isCar;
                        } else
                            eventsHandler.peripheralPassed = false;
                        //eventStart = eventStart && peripheralPassed;
                    } else
                        eventsHandler.notAllowedPeripheral = true;
                } else if ((_peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_WIRED_HEADSET) ||
                        (_peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_BLUETOOTH_HEADSET) ||
                        (_peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_HEADPHONES)) {
                    boolean wiredHeadsetConnected = ApplicationPreferences.prefWiredHeadsetConnected;
                    boolean wiredHeadsetMicrophone = ApplicationPreferences.prefWiredHeadsetMicrophone;
                    boolean bluetoothHeadsetConnected = ApplicationPreferences.prefBluetoothHeadsetConnected;
                    boolean bluetoothHeadsetMicrophone = ApplicationPreferences.prefBluetoothHeadsetMicrophone;

                    eventsHandler.peripheralPassed = false;
                    if (wiredHeadsetConnected) {
                        if ((_peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_WIRED_HEADSET)
                                && wiredHeadsetMicrophone)
                            eventsHandler.peripheralPassed = true;
                        else
                        if ((_peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_HEADPHONES)
                                && (!wiredHeadsetMicrophone))
                            eventsHandler.peripheralPassed = true;
                    }
                    if (bluetoothHeadsetConnected) {
                        if ((_peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_BLUETOOTH_HEADSET)
                                && bluetoothHeadsetMicrophone)
                            eventsHandler.peripheralPassed = true;
                    }
                    //eventStart = eventStart && peripheralPassed;
                }

                if (!eventsHandler.notAllowedPeripheral) {
                    if (eventsHandler.peripheralPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedPeripheral = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                //PPApplication.logE("[TEST BATTERY] EventPreferencesPeripherals.doHandleEvent", "peripherals - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_PERIPHERAL);
            }
        }
    }

}
