package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class EventPreferencesAccessories extends EventPreferences {

    String _accessoryType;

    static final String PREF_EVENT_ACCESSORIES_ENABLED = "eventPeripheralEnabled";
    private static final String PREF_EVENT_ACCESSORIES_TYPE = "eventAccessoryType";

    private static final String PREF_EVENT_ACCESSORIES_CATEGORY = "eventAccessoriesCategoryRoot";

    private static final int ACCESSORY_TYPE_DESK_DOCK = 0;
    private static final int ACCESSORY_TYPE_CAR_DOCK = 1;
    private static final int ACCESSORY_TYPE_WIRED_HEADSET = 2;
    private static final int ACCESSORY_TYPE_BLUETOOTH_HEADSET = 3;
    private static final int ACCESSORY_TYPE_HEADPHONES = 4;

    EventPreferencesAccessories(Event event,
                                boolean enabled,
                                String accessoryType)
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

        String[] splits;
        if (this._accessoryType != null)
            splits = this._accessoryType.split("\\|");
        else
            splits = new String[]{};
        Set<String> set = new HashSet<>(Arrays.asList(splits));
        editor.putStringSet(PREF_EVENT_ACCESSORIES_TYPE, set);

        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_ACCESSORIES_ENABLED, false);

        Set<String> set = preferences.getStringSet(PREF_EVENT_ACCESSORIES_TYPE, null);
        StringBuilder plugged = new StringBuilder();
        if (set != null) {
            for (String s : set) {
                if (plugged.length() > 0)
                    plugged.append("|");
                plugged.append(s);
            }
        }
        this._accessoryType = plugged.toString();
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
                String selectedAccessory = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                if ((this._accessoryType != null) && !this._accessoryType.isEmpty() && !this._accessoryType.equals("-")) {
                    String[] splits = this._accessoryType.split("\\|");
                    List<String> accessoryTypeValues = Arrays.asList(context.getResources().getStringArray(R.array.eventAccessoryTypeValues));
                    String[] accessoryTypeNames = context.getResources().getStringArray(R.array.eventAccessoryTypeArray);
                    selectedAccessory = "";
                    for (String s : splits) {
                        int idx = accessoryTypeValues.indexOf(s);
                        if (idx != -1) {
                            if (!selectedAccessory.isEmpty())
                                //noinspection StringConcatenationInLoop
                                selectedAccessory = selectedAccessory + ", ";
                            //noinspection StringConcatenationInLoop
                            selectedAccessory = selectedAccessory + accessoryTypeNames[idx];
                        }
                    }
                }
                descr = descr + "<b>" + selectedAccessory + "</b>";
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
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);

                Set<String> set = prefMng.getSharedPreferences().getStringSet(PREF_EVENT_ACCESSORIES_TYPE, null);
                StringBuilder accessoryType = new StringBuilder();
                if (set != null) {
                    for (String s : set) {
                        if (accessoryType.length() > 0)
                            accessoryType.append("|");
                        accessoryType.append(s);
                    }
                }
                boolean bold = accessoryType.length() > 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, true, false, false);
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
            Set<String> set = preferences.getStringSet(key, null);
            String accessoryType = "";
            if (set != null) {
                String[] accessoryTypeValues = context.getResources().getStringArray(R.array.eventAccessoryTypeValues);
                String[] accessoryTypeNames = context.getResources().getStringArray(R.array.eventAccessoryTypeArray);
                for (String s : set) {
                    if (!s.isEmpty()) {
                        if (!accessoryType.isEmpty())
                            //noinspection StringConcatenationInLoop
                            accessoryType = accessoryType + ", ";
                        //noinspection StringConcatenationInLoop
                        accessoryType = accessoryType + accessoryTypeNames[Arrays.asList(accessoryTypeValues).indexOf(s)];
                    }
                }
                if (accessoryType.isEmpty())
                    accessoryType = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            }
            else
                accessoryType = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            setSummary(prefMng, key, accessoryType/*, context*/);
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

    @Override
    boolean isRunnable(Context context)
    {
        boolean runnable = super.isRunnable(context);

        runnable = runnable && (!_accessoryType.isEmpty());

        return runnable;
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
                if (!this._accessoryType.isEmpty()) {
                    String[] splits = this._accessoryType.split("\\|");
                    for (String split : splits) {
                        int accessoryType = Integer.parseInt(split);

                        if ((accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_DESK_DOCK) ||
                                (accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_CAR_DOCK)) {
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
                                    if ((accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_DESK_DOCK)
                                            && isDesk)
                                        eventsHandler.accessoryPassed = true;
                                    else
                                        eventsHandler.accessoryPassed = (accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_CAR_DOCK)
                                                && isCar;
                                } else
                                    eventsHandler.accessoryPassed = false;
                                //eventStart = eventStart && accessoryPassed;
                            } else
                                eventsHandler.notAllowedAccessory = true;
                        } else if ((accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_WIRED_HEADSET) ||
                                (accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_HEADPHONES)) {
                            boolean wiredHeadsetConnected = ApplicationPreferences.prefWiredHeadsetConnected;
                            boolean wiredHeadsetMicrophone = ApplicationPreferences.prefWiredHeadsetMicrophone;

                            eventsHandler.accessoryPassed = false;
                            if (wiredHeadsetConnected) {
                                if ((accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_WIRED_HEADSET)
                                        && wiredHeadsetMicrophone)
                                    eventsHandler.accessoryPassed = true;
                                else
                                if ((accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_HEADPHONES)
                                        && (!wiredHeadsetMicrophone))
                                    eventsHandler.accessoryPassed = true;
                            }
                            //eventStart = eventStart && accessoryPassed;
                        } else if (accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_BLUETOOTH_HEADSET) {
                            BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
                            if (bluetooth != null) {
                                boolean isBluetoothEnabled = bluetooth.isEnabled();
                                boolean isHeadsetConnected = false;
                                if (isBluetoothEnabled) {
                                    boolean isBluetoothConnected = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(null, "");
                                    if (isBluetoothConnected)
                                        isHeadsetConnected = BluetoothProfile.STATE_CONNECTED == bluetooth.getProfileConnectionState(BluetoothProfile.HEADSET);
                                }

                                boolean bluetoothHeadsetConnected = ApplicationPreferences.prefBluetoothHeadsetConnected && isHeadsetConnected;
                                boolean bluetoothHeadsetMicrophone = ApplicationPreferences.prefBluetoothHeadsetMicrophone && isHeadsetConnected;

                                eventsHandler.accessoryPassed = false;
                                if (bluetoothHeadsetConnected) {
                                    if ((accessoryType == EventPreferencesAccessories.ACCESSORY_TYPE_BLUETOOTH_HEADSET)
                                            && bluetoothHeadsetMicrophone)
                                        eventsHandler.accessoryPassed = true;
                                }
                            }
                            else
                                eventsHandler.accessoryPassed = false;
                            //eventStart = eventStart && accessoryPassed;
                        }

                        // this type is passed
                        if (eventsHandler.accessoryPassed)
                            break;
                    }
                }
                else
                    eventsHandler.accessoryPassed = false;

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
