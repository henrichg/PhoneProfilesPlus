package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;

class EventPreferencesBrightness extends EventPreferences {

    int _operator;
    String _brightnessLevel;

    static final String PREF_EVENT_BRIGHTNESS_ENABLED = "eventBrightnessEnabled";
    private static final String PREF_EVENT_BRIGHTNESS_OPERATOR = "eventBrightnessOperator";
    private static final String PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL = "eventBrightnessBrightnessLevel";

    private static final String PREF_EVENT_SCREEN_CATEGORY = "eventBrightnessCategoryRoot";

    EventPreferencesBrightness(Event event,
                               boolean enabled,
                               int operator,
                               String brightnessLevel)
    {
        super(event, enabled);

        this._operator = operator;
        this._brightnessLevel = brightnessLevel;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesBrightness._enabled;
        this._operator = fromEvent._eventPreferencesBrightness._operator;
        this._brightnessLevel = fromEvent._eventPreferencesBrightness._brightnessLevel;
        this.setSensorPassed(fromEvent._eventPreferencesBrightness.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_BRIGHTNESS_ENABLED, _enabled);
        editor.putString(PREF_EVENT_BRIGHTNESS_OPERATOR, String.valueOf(this._operator));
        editor.putString(PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL, _brightnessLevel);
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_BRIGHTNESS_ENABLED, false);
        this._operator = Integer.parseInt(preferences.getString(PREF_EVENT_BRIGHTNESS_OPERATOR, "0"));
        this._brightnessLevel = preferences.getString(PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL, "50|0|1|0");
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_brightness_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_BRIGHTNESS_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_brightness), addPassStatus, DatabaseHandler.ETYPE_BRIGHTNESS, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                String[] operators = context.getResources().getStringArray(R.array.brightnessSensorOperatorValues);
                int index = Arrays.asList(operators).indexOf(Integer.toString(this._operator);
                if (index != -1) {
                    _value.append(context.getString(R.string.event_preferences_brightness_operator)).append(StringConstants.STR_COLON_WITH_SPACE);
                    String[] operatorNames = context.getResources().getStringArray(R.array.brightnessSensorOperatorArray);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(operatorNames[index], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                }


                if (this._whenUnlocked) {
                    if (this._eventType == EventPreferencesBrightness.ETYPE_SCREENON)
                        _value.append(StringConstants.STR_BULLET).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.pref_event_screen_startWhenUnlocked), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    else
                        _value.append(StringConstants.STR_BULLET).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.pref_event_screen_startWhenLocked), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                }
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value/*, Context context*/)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_SCREEN_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_SCREEN_EVENT_TYPE))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);

                int typeValue = Integer.parseInt(listPreference.getValue());
                setWhenUnlockedTitle(prefMng, typeValue);
            }
        }
        if (key.equals(PREF_EVENT_SCREEN_WHEN_UNLOCKED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences,
                    @SuppressWarnings("unused") Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_SCREEN_ENABLED) ||
            key.equals(PREF_EVENT_SCREEN_WHEN_UNLOCKED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING/*, context*/);
        }
        if (key.equals(PREF_EVENT_SCREEN_EVENT_TYPE))
        {
            setSummary(prefMng, key, preferences.getString(key, "")/*, context*/);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_SCREEN_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_SCREEN_EVENT_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_SCREEN_WHEN_UNLOCKED, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_SCREEN_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesBrightness tmp = new EventPreferencesBrightness(this._event, this._enabled, this._eventType, this._whenUnlocked);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_SCREEN_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_SCREEN_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_BRIGHTNESS).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false, false, false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_SCREEN_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        StringConstants.STR_COLON_WITH_SPACE+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_SCREEN_ENABLED) != null) {
                final Preference eventTypePreference = prefMng.findPreference(PREF_EVENT_SCREEN_EVENT_TYPE);
                final PreferenceManager _prefMng = prefMng;

                if (eventTypePreference != null) {
                    eventTypePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                        String sNewValue = (String) newValue;
                        int iNewValue;
                        if (sNewValue.isEmpty())
                            iNewValue = 100;
                        else
                            iNewValue = Integer.parseInt(sNewValue);

                        setWhenUnlockedTitle(_prefMng, iNewValue);

                        return true;
                    });
                }

                setSummary(prefMng, PREF_EVENT_SCREEN_ENABLED, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    private void setWhenUnlockedTitle(PreferenceManager prefMng, int eventTypeValue)
    {
        final SwitchPreferenceCompat whenUnlockedPreference = prefMng.findPreference(PREF_EVENT_SCREEN_WHEN_UNLOCKED);

        if (whenUnlockedPreference != null) {
            if (eventTypeValue == EventPreferencesBrightness.ETYPE_SCREENON)
                whenUnlockedPreference.setTitle(R.string.event_preferences_screen_start_when_unlocked);
            else
                whenUnlockedPreference.setTitle(R.string.event_preferences_screen_start_when_locked);
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
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesBrightness.PREF_EVENT_BRIGHTNESS_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {

                //TODO

                if (!eventsHandler.notAllowedBrightness) {
                    if (eventsHandler.brightnessPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedBrightness = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_BRIGHTNESS);
            }
        }
    }

}
