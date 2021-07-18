package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class EventPreferencesSoundProfile extends EventPreferences {

    String _ringerModes;
    String _zenModes;

    static final String PREF_EVENT_SOUND_PROFILE_ENABLED = "eventSoundProfileEnabled";
    private static final String PREF_EVENT_SOUND_PROFILE_RINGER_MODES = "eventSoundProfileRingerModes";
    private static final String PREF_EVENT_SOUND_PROFILE_ZEN_MODES = "eventSoundProfileZenModes";

    private static final String PREF_EVENT_SOUND_PROFILE_CATEGORY = "eventSoundProfileCategoryRoot";

    // it must be same as array/eventSoundProfileRingerModeValues for "array_pref_event_ringerMode_zenMode".
    static String RINGER_MODE_DO_NOT_DISTURB_VALUE = "4";

    EventPreferencesSoundProfile(Event event,
                                 boolean enabled,
                                 String ringerModes,
                                 String zenModes)
    {
        super(event, enabled);

        this._ringerModes = ringerModes;
        this._zenModes = zenModes;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesSoundProfile._enabled;
        this._ringerModes = fromEvent._eventPreferencesSoundProfile._ringerModes;
        this._zenModes = fromEvent._eventPreferencesSoundProfile._zenModes;
        this.setSensorPassed(fromEvent._eventPreferencesSoundProfile.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_SOUND_PROFILE_ENABLED, _enabled);

        String[] splits = this._ringerModes.split("\\|");
        Set<String> set = new HashSet<>(Arrays.asList(splits));
        editor.putStringSet(PREF_EVENT_SOUND_PROFILE_RINGER_MODES, set);

        splits = this._zenModes.split("\\|");
        set = new HashSet<>(Arrays.asList(splits));
        editor.putStringSet(PREF_EVENT_SOUND_PROFILE_ZEN_MODES, set);

        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_SOUND_PROFILE_ENABLED, false);

        Set<String> set = preferences.getStringSet(PREF_EVENT_SOUND_PROFILE_RINGER_MODES, null);
        StringBuilder values = new StringBuilder();
        if (set != null) {
            for (String s : set) {
                if (values.length() > 0)
                    values.append("|");
                values.append(s);
            }
        }
        this._ringerModes = values.toString();

        set = preferences.getStringSet(PREF_EVENT_SOUND_PROFILE_ZEN_MODES, null);
        values = new StringBuilder();
        if (set != null) {
            for (String s : set) {
                if (values.length() > 0)
                    values.append("|");
                values.append(s);
            }
        }
        this._zenModes = values.toString();
    }

    @SuppressWarnings("StringConcatenationInLoop")
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

                boolean dndChecked = false;
                String selectedValues = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                if (!this._ringerModes.isEmpty() && !this._ringerModes.equals("-")) {
                    String[] splits = this._ringerModes.split("\\|");
                    String[] values = context.getResources().getStringArray(R.array.eventSoundProfileRingerModeValues);
                    String[] names = context.getResources().getStringArray(R.array.eventSoundProfileRingerModeArray);
                    selectedValues = "";
                    for (String s : splits) {
                        int idx = Arrays.asList(values).indexOf(s);
                        if (idx != -1) {
                            if (!selectedValues.isEmpty())
                                selectedValues = selectedValues + ", ";
                            if (values[idx].equals(RINGER_MODE_DO_NOT_DISTURB_VALUE))
                                dndChecked = true;
                            selectedValues = selectedValues + names[idx];
                        }
                    }
                }
                descr = descr + context.getString(R.string.event_preferences_soundProfile_ringerModes) + ": <b>" + selectedValues + "</b>";

                if (dndChecked) {
                    selectedValues = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                    if (!this._zenModes.isEmpty() && !this._zenModes.equals("-")) {
                        String[] splits = this._zenModes.split("\\|");
                        String[] values = context.getResources().getStringArray(R.array.eventSoundProfileZenModeValues);
                        String[] names = context.getResources().getStringArray(R.array.eventSoundProfileZenModeArray);
                        selectedValues = "";
                        for (String s : splits) {
                            int idx = Arrays.asList(values).indexOf(s);
                            if (idx != -1) {
                                if (!selectedValues.isEmpty())
                                    selectedValues = selectedValues + ", ";
                                selectedValues = selectedValues + names[idx];
                            }
                        }
                    }
                    descr = descr + " • " + context.getString(R.string.event_preferences_soundProfile_zenModes) + ": <b>" + selectedValues + "</b>";
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

        if (key.equals(PREF_EVENT_SOUND_PROFILE_RINGER_MODES) ||
                key.equals(PREF_EVENT_SOUND_PROFILE_ZEN_MODES))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesSoundProfile.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesSoundProfile.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_SOUND_PROFILE_ENABLED, false);

        Preference preference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_RINGER_MODES);
        if (preference != null) {
            Set<String> set = prefMng.getSharedPreferences().getStringSet(PREF_EVENT_SOUND_PROFILE_RINGER_MODES, null);
            StringBuilder values = new StringBuilder();
            if (set != null) {
                for (String s : set) {
                    if (values.length() > 0)
                        values.append("|");
                    values.append(s);
                }
            }
            boolean bold = values.length() > 0;
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_ZEN_MODES);
        if (preference != null) {
            Set<String> set = prefMng.getSharedPreferences().getStringSet(PREF_EVENT_SOUND_PROFILE_ZEN_MODES, null);
            StringBuilder values = new StringBuilder();
            if (set != null) {
                for (String s : set) {
                    if (values.length() > 0)
                        values.append("|");
                    values.append(s);
                }
            }
            boolean bold = values.length() > 0;
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, false, false);
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_SOUND_PROFILE_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }

        if (key.equals(PREF_EVENT_SOUND_PROFILE_RINGER_MODES)) {
            Set<String> set = preferences.getStringSet(key, null);
            String values = "";
            if (set != null) {
                String[] sValues = context.getResources().getStringArray(R.array.eventSoundProfileRingerModeValues);
                String[] sNames = context.getResources().getStringArray(R.array.eventSoundProfileRingerModeArray);
                for (String s : set) {
                    if (!s.isEmpty()) {
                        if (!values.isEmpty())
                            values = values + ", ";
                        values = values + sNames[Arrays.asList(sValues).indexOf(s)];
                    }
                }
                if (values.isEmpty())
                    values = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            }
            else
                values = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            setSummary(prefMng, key, values, context);
        }
        if (key.equals(PREF_EVENT_SOUND_PROFILE_ZEN_MODES)) {
            Set<String> set = preferences.getStringSet(key, null);
            String values = "";
            if (set != null) {
                String[] sValues = context.getResources().getStringArray(R.array.eventSoundProfileZenModeValues);
                String[] sNames = context.getResources().getStringArray(R.array.eventSoundProfileZenModeArray);
                for (String s : set) {
                    if (!s.isEmpty()) {
                        if (!values.isEmpty())
                            values = values + ", ";
                        values = values + sNames[Arrays.asList(sValues).indexOf(s)];
                    }
                }
                if (values.isEmpty())
                    values = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            }
            else
                values = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            setSummary(prefMng, key, values, context);
        }

    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_SOUND_PROFILE_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_SOUND_PROFILE_RINGER_MODES, preferences, context);
        setSummary(prefMng, PREF_EVENT_SOUND_PROFILE_ZEN_MODES, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_SOUND_PROFILE_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesSoundProfile tmp = new EventPreferencesSoundProfile(this._event, this._enabled,
                    this._ringerModes, this._zenModes);
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

        runnable = runnable && (!_ringerModes.isEmpty());

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, Context context)
    {
        boolean enabled = Event.isEventPreferenceAllowed(PREF_EVENT_SOUND_PROFILE_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED;

        MultiSelectListPreference ringerModesPreference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_RINGER_MODES);
        if (ringerModesPreference != null)
            ringerModesPreference.setEnabled(enabled);

        MultiSelectListPreference zenModesPreference = prefMng.findPreference(PREF_EVENT_SOUND_PROFILE_ZEN_MODES);
        if (zenModesPreference != null)
            zenModesPreference.setEnabled(enabled);

        if (enabled) {
            if (ringerModesPreference != null) {
                boolean checked = false;
                Set<String> set = ringerModesPreference.getValues();
                if (set != null) {
                    String[] sValues = context.getResources().getStringArray(R.array.eventSoundProfileRingerModeValues);
                    for (String s : set) {
                        if (!s.isEmpty()) {
                            String value = sValues[Arrays.asList(sValues).indexOf(s)];
                            if (value.equals(RINGER_MODE_DO_NOT_DISTURB_VALUE)) {
                                // checked is "Do not disturb"
                                checked = true;
                            }
                        }
                    }
                }
                if (zenModesPreference != null)
                    zenModesPreference.setEnabled(checked);
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
            if ((Event.isEventPreferenceAllowed(EventPreferencesSoundProfile.PREF_EVENT_SOUND_PROFILE_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                eventsHandler.soundProfilePassed = true;
                boolean tested = false;

                boolean dndChecked = false;
                if (!this._ringerModes.isEmpty() && !this._ringerModes.equals("-")) {
                    String[] splits = this._ringerModes.split("\\|");
                    String[] values = eventsHandler.context.getResources().getStringArray(R.array.eventSoundProfileRingerModeValues);
                    for (String s : splits) {
                        int idx = Arrays.asList(values).indexOf(s);
                        if (idx != -1) {
                            if (values[idx].equals(RINGER_MODE_DO_NOT_DISTURB_VALUE))
                                dndChecked = true;

                            ActivateProfileHelper.getRingerMode(eventsHandler.context);
                            // check ringer modes
                            switch (values[idx]) {
                                case "1":
                                    // ring
                                    eventsHandler.soundProfilePassed =
                                            ApplicationPreferences.prefRingerMode == Profile.RINGERMODE_RING;
                                    tested = true;
                                    break;
                                case "2":
                                    // vibrate
                                    eventsHandler.soundProfilePassed =
                                            ApplicationPreferences.prefRingerMode == Profile.RINGERMODE_VIBRATE;
                                    tested = true;
                                    break;
                                case "3":
                                    // silent
                                    eventsHandler.soundProfilePassed =
                                            ApplicationPreferences.prefRingerMode == Profile.RINGERMODE_SILENT;
                                    tested = true;
                                    break;
                                case "4":
                                    // dnd
                                    eventsHandler.soundProfilePassed =
                                            ApplicationPreferences.prefRingerMode == Profile.RINGERMODE_ZENMODE;
                                    tested = true;
                                    break;
                            }
                        }
                    }
                }

                if (dndChecked && !this._zenModes.isEmpty() && !this._zenModes.equals("-")) {
                    String[] splits = this._zenModes.split("\\|");
                    String[] values = eventsHandler.context.getResources().getStringArray(R.array.eventSoundProfileZenModeValues);
                    for (String s : splits) {
                        int idx = Arrays.asList(values).indexOf(s);
                        if (idx != -1) {
                            // check zen modes

                            ActivateProfileHelper.getZenMode(eventsHandler.context);
                            switch (values[idx]) {
                                case "1":
                                    // off
                                    eventsHandler.soundProfilePassed =
                                            ApplicationPreferences.prefZenMode == Profile.ZENMODE_ALL;
                                    tested = true;
                                    break;
                                case "2":
                                    // off with vibration
                                    eventsHandler.soundProfilePassed =
                                            ApplicationPreferences.prefZenMode == Profile.ZENMODE_ALL_AND_VIBRATE;
                                    tested = true;
                                    break;
                                case "3":
                                    // priority
                                    eventsHandler.soundProfilePassed =
                                            ApplicationPreferences.prefZenMode == Profile.ZENMODE_PRIORITY;
                                    tested = true;
                                    break;
                                case "4":
                                    // priority with vibration
                                    eventsHandler.soundProfilePassed =
                                            ApplicationPreferences.prefZenMode == Profile.ZENMODE_PRIORITY_AND_VIBRATE;
                                    tested = true;
                                    break;
                                case "5":
                                    // alarms only
                                    eventsHandler.soundProfilePassed =
                                            ApplicationPreferences.prefZenMode == Profile.ZENMODE_ALARMS;
                                    tested = true;
                                    break;
                                case "6":
                                    // total silence
                                    eventsHandler.soundProfilePassed =
                                            ApplicationPreferences.prefZenMode == Profile.ZENMODE_NONE;
                                    tested = true;
                                    break;
                            }
                        }
                    }
                }

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
