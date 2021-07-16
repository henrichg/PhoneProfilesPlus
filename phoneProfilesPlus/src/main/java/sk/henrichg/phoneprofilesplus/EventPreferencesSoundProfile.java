package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesSoundProfile extends EventPreferences {

    int _ringerMode;
    int _zenMode;

    static final String PREF_EVENT_SOUND_PROFILE_ENABLED = "eventSoundProfileEnabled";
    private static final String PREF_EVENT_SOUND_PROFILE_RINGER_MODE = "eventSoundProfileRingerMode";
    private static final String PREF_EVENT_SOUND_PROFILE_ZEN_MODE = "eventSoundProfileZenMode";

    private static final String PREF_EVENT_SOUND_PROFILE_CATEGORY = "eventSoundProfileCategoryRoot";

    EventPreferencesSoundProfile(Event event,
                                 boolean enabled,
                                 int ringerMode,
                                 int zenMode)
    {
        super(event, enabled);

        this._ringerMode = ringerMode;
        this._zenMode = zenMode;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesSoundProfile._enabled;
        this._ringerMode = fromEvent._eventPreferencesSoundProfile._ringerMode;
        this._zenMode = fromEvent._eventPreferencesSoundProfile._zenMode;
        this.setSensorPassed(fromEvent._eventPreferencesSoundProfile.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_SOUND_PROFILE_ENABLED, _enabled);
        editor.putString(PREF_EVENT_SOUND_PROFILE_RINGER_MODE, String.valueOf(this._ringerMode));
        editor.putString(PREF_EVENT_SOUND_PROFILE_ZEN_MODE, String.valueOf(this._zenMode));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_SOUND_PROFILE_ENABLED, false);
        this._ringerMode = Integer.parseInt(preferences.getString(PREF_EVENT_SOUND_PROFILE_RINGER_MODE, "0"));
        this._zenMode = Integer.parseInt(preferences.getString(PREF_EVENT_SOUND_PROFILE_ZEN_MODE, "0"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_soundProfile_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_SOUND_PROFILE_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_soundProfile), addPassStatus, DatabaseHandler.ETYPE_SOUND_PROFILE, context);
                    descr = descr + "</b> ";
                }

                boolean _addBullet = false;
                if (this._ringerMode != 0) {
                    descr = descr + context.getString(R.string.event_preferences_soundProfile_ringerMode) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.eventSoundProfileRingerModeArray);
                    descr = descr + "<b>" + fields[this._ringerMode] + "</b>";
                    _addBullet = true;
                }

                if (this._zenMode != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.event_preferences_soundProfile_zenMode) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.eventSoundProfileZenModeArray);
                    descr = descr + "<b>" + fields[this._zenMode] + "</b>";
                }
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_SOUND_PROFILE_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_SOUND_PROFILE_RINGER_MODE) ||
                key.equals(PREF_EVENT_SOUND_PROFILE_ZEN_MODE))
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
        event._eventPreferencesSoundProfile.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesSoundProfile.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_SOUND_PROFILE_ENABLED, false);
        ListPreference preference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_RINGER_MODE);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_ZEN_MODE);
        if (preference != null) {
            int index = preference.findIndexOfValue(preference.getValue());
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, index > 0, true, !isRunnable, false);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_SOUND_PROFILE_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
        if (key.equals(PREF_EVENT_SOUND_PROFILE_RINGER_MODE) ||
            key.equals(PREF_EVENT_SOUND_PROFILE_ZEN_MODE))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_SOUND_PROFILE_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_SOUND_PROFILE_RINGER_MODE, preferences, context);
        setSummary(prefMng, PREF_EVENT_SOUND_PROFILE_ZEN_MODE, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_SOUND_PROFILE_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesSoundProfile tmp = new EventPreferencesSoundProfile(this._event, this._enabled,
                    this._ringerMode, this._zenMode);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_SOUND_PROFILE_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_SOUND_PROFILE).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !(tmp.isRunnable(context) && permissionGranted), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_CATEGORY);
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

        runnable = runnable && ((_ringerMode != 0) || (_zenMode != 0));

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, Context context)
    {
        boolean enabled = Event.isEventPreferenceAllowed(PREF_EVENT_SOUND_PROFILE_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED;

        Preference preference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_RINGER_MODE);
        if (preference != null)
            preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_ZEN_MODE);
        if (preference != null)
            preference.setEnabled(enabled);
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
            if ((Event.isEventPreferenceAllowed(EventPreferencesSoundProfile.PREF_EVENT_SOUND_PROFILE_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                eventsHandler.soundProfilePassed = true;
                boolean tested = false;

                /*
                if (_airplaneMode != 0) {

                    boolean enabled = ActivateProfileHelper.isAirplaneMode(eventsHandler.context);
                    //PPApplication.logE("-###- EventPreferencesRadioSwitch.doHandleEvent", "airplaneModeState=" + enabled);
                    tested = true;
                    if (_airplaneMode == 1)
                        eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && enabled;
                    else
                        eventsHandler.radioSwitchPassed = eventsHandler.radioSwitchPassed && !enabled;
                }
                */

                eventsHandler.soundProfilePassed = eventsHandler.soundProfilePassed && tested;

                if (!eventsHandler.notAllowedSoundProfile) {
                    if (eventsHandler.soundProfilePassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedSoundProfile = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                //PPApplication.logE("[TEST BATTERY] EventPreferencesRadioSwitch.doHandleEvent", "sound profile - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_SOUND_PROFILE);
            }
        }
    }

}
