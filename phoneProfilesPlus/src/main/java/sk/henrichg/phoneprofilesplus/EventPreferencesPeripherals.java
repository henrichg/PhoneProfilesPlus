package sk.henrichg.phoneprofilesplus;

import android.content.Context;
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

    static final int PERIPHERAL_TYPE_DESK_DOCK = 0;
    static final int PERIPHERAL_TYPE_CAR_DOCK = 1;
    static final int PERIPHERAL_TYPE_WIRED_HEADSET = 2;
    static final int PERIPHERAL_TYPE_BLUETOOTH_HEADSET = 3;
    static final int PERIPHERAL_TYPE_HEADPHONES = 4;

    EventPreferencesPeripherals(Event event,
                                    boolean enabled,
                                    int peripheralType)
    {
        super(event, enabled);

        this._peripheralType = peripheralType;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesPeripherals._enabled;
        this._peripheralType = fromEvent._eventPreferencesPeripherals._peripheralType;
        this.setSensorPassed(fromEvent._eventPreferencesPeripherals.getSensorPassed());
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_PERIPHERAL_ENABLED, _enabled);
        editor.putString(PREF_EVENT_PERIPHERAL_TYPE, String.valueOf(this._peripheralType));
        editor.apply();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_PERIPHERAL_ENABLED, false);
        this._peripheralType = Integer.parseInt(preferences.getString(PREF_EVENT_PERIPHERAL_TYPE, "0"));
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
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
                    descr = descr + ": </b>";
                }

                String[] peripheralTypes = context.getResources().getStringArray(R.array.eventPeripheralTypeArray);
                descr = descr + peripheralTypes[this._peripheralType];
            }
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_PERIPHERAL_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preference.isChecked(), true, false, false, false);
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

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_PERIPHERAL_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
        if (key.equals(PREF_EVENT_PERIPHERAL_TYPE))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_PERIPHERAL_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_PERIPHERAL_TYPE, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_PERIPHERAL_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesPeripherals tmp = new EventPreferencesPeripherals(this._event, this._enabled, this._peripheralType);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_PERIPHERAL_CATEGORY);
            if (preference != null) {
                SwitchPreferenceCompat enabledPreference = prefMng.findPreference(PREF_EVENT_PERIPHERAL_ENABLED);
                boolean enabled = (enabledPreference != null) && enabledPreference.isChecked();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, true, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false));
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
