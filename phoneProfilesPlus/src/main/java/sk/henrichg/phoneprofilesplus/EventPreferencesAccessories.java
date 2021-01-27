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

class EventPreferencesAccessories extends EventPreferences {

    int _accessoryType;

    static final String PREF_EVENT_ACCESSORIES_ENABLED = "eventPeripheralEnabled";
    private static final String PREF_EVENT_ACCESSORIES_TYPE = "eventPeripheralType";

    private static final String PREF_EVENT_ACCESSORIES_CATEGORY = "eventAccessoriesCategoryRoot";

    private static final int ACCESSORY_TYPE_DESK_DOCK = 0;
    private static final int ACCESSORY_TYPE_CAR_DOCK = 1;
    private static final int ACCESSORY_TYPE_WIRED_HEADSET = 2;
    private static final int ACCESSORY_TYPE_BLUETOOTH_HEADSET = 3;
    private static final int ACCESSORY_TYPE_HEADPHONES = 4;

    EventPreferencesAccessories(Event event,
                                boolean enabled,
                                int accessoryType)
    {
        super(event, enabled);

        this._accessoryType = accessoryType;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesAccessories._enabled;
        this._accessoryType = fromEvent._eventPreferencesAccessories._accessoryType;
        this.setSensorPassed(fromEvent._eventPreferencesAccessories.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_ACCESSORIES_ENABLED, _enabled);
        editor.putString(PREF_EVENT_ACCESSORIES_TYPE, String.valueOf(this._accessoryType));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_ACCESSORIES_ENABLED, false);
        this._accessoryType = Integer.parseInt(preferences.getString(PREF_EVENT_ACCESSORIES_TYPE, "0"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_accessories_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_ACCESSORIES_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_peripheral), addPassStatus, DatabaseHandler.ETYPE_ACCESSORY, context);
                    descr = descr + "</b> ";
                }

                descr = descr + context.getString(R.string.event_preferences_peripheral_type) + ": ";
                String[] accessoryTypes = context.getResources().getStringArray(R.array.eventAccessoryTypeArray);
                descr = descr + "<b>" + accessoryTypes[this._accessoryType] + "</b>";
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value/*, Context context*/)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_ACCESSORIES_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_ACCESSORIES_TYPE))
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
        if (key.equals(PREF_EVENT_ACCESSORIES_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false"/*, context*/);
        }
        if (key.equals(PREF_EVENT_ACCESSORIES_TYPE))
        {
            setSummary(prefMng, key, preferences.getString(key, "")/*, context*/);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_ACCESSORIES_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_ACCESSORIES_TYPE, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_ACCESSORIES_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesAccessories tmp = new EventPreferencesAccessories(this._event, this._enabled, this._accessoryType);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_ACCESSORIES_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_ACCESSORIES_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_ACCESSORIES).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !(tmp.isRunnable(context) && permissionGranted), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_ACCESSORIES_CATEGORY);
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
            if (Event.isEventPreferenceAllowed(EventPreferencesAccessories.PREF_EVENT_ACCESSORIES_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if ((_accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_DESK_DOCK) ||
                        (_accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_CAR_DOCK)) {
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
                            if ((_accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_DESK_DOCK)
                                    && isDesk)
                                eventsHandler.accessoryPassed = true;
                            else
                                eventsHandler.accessoryPassed = (_accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_CAR_DOCK)
                                        && isCar;
                        } else
                            eventsHandler.accessoryPassed = false;
                        //eventStart = eventStart && accessoryPassed;
                    } else
                        eventsHandler.notAllowedAccessory = true;
                } else if ((_accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_WIRED_HEADSET) ||
                        (_accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_BLUETOOTH_HEADSET) ||
                        (_accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_HEADPHONES)) {
                    boolean wiredHeadsetConnected = ApplicationPreferences.prefWiredHeadsetConnected;
                    boolean wiredHeadsetMicrophone = ApplicationPreferences.prefWiredHeadsetMicrophone;
                    boolean bluetoothHeadsetConnected = ApplicationPreferences.prefBluetoothHeadsetConnected;
                    boolean bluetoothHeadsetMicrophone = ApplicationPreferences.prefBluetoothHeadsetMicrophone;

                    eventsHandler.accessoryPassed = false;
                    if (wiredHeadsetConnected) {
                        if ((_accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_WIRED_HEADSET)
                                && wiredHeadsetMicrophone)
                            eventsHandler.accessoryPassed = true;
                        else
                        if ((_accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_HEADPHONES)
                                && (!wiredHeadsetMicrophone))
                            eventsHandler.accessoryPassed = true;
                    }
                    if (bluetoothHeadsetConnected) {
                        if ((_accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_BLUETOOTH_HEADSET)
                                && bluetoothHeadsetMicrophone)
                            eventsHandler.accessoryPassed = true;
                    }
                    //eventStart = eventStart && accessoryPassed;
                }

                if (!eventsHandler.notAllowedAccessory) {
                    if (eventsHandler.accessoryPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedAccessory = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                //PPApplication.logE("[TEST BATTERY] EventPreferencesAccessories.doHandleEvent", "accessories - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_ACCESSORY);
            }
        }
    }

}
