package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;

class EventPreferencesBrightness extends EventPreferences {

    int _operatorFrom;
    String _brightnessLevelFrom;
    int _operatorTo;
    String _brightnessLevelTo;

    static final String PREF_EVENT_BRIGHTNESS_ENABLED = "eventBrightnessEnabled";
    private static final String PREF_EVENT_BRIGHTNESS_OPERATOR_FROM = "eventBrightnessOperatorFrom";
    static final String PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_FROM = "eventBrightnessBrightnessLevelFrom";
    private static final String PREF_EVENT_BRIGHTNESS_OPERATOR_TO = "eventBrightnessOperatorTo";
    static final String PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_TO = "eventBrightnessBrightnessLevelTo";

    static final String PREF_EVENT_BRIGHTNESS_CATEGORY = "eventBrightnessCategoryRoot";

    EventPreferencesBrightness(Event event,
                               boolean enabled,
                               int operatorFrom,
                               String brightnessLevelFrom,
                               int operatorTo,
                               String brightnessLevelTo)
    {
        super(event, enabled);

        this._operatorFrom = operatorFrom;
        this._brightnessLevelFrom = brightnessLevelFrom;
        this._operatorTo = operatorTo;
        this._brightnessLevelTo = brightnessLevelTo;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesBrightness._enabled;
        this._operatorFrom = fromEvent._eventPreferencesBrightness._operatorFrom;
        this._brightnessLevelFrom = fromEvent._eventPreferencesBrightness._brightnessLevelFrom;
        this._operatorTo = fromEvent._eventPreferencesBrightness._operatorTo;
        this._brightnessLevelTo = fromEvent._eventPreferencesBrightness._brightnessLevelTo;
        this.setSensorPassed(fromEvent._eventPreferencesBrightness.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_BRIGHTNESS_ENABLED, _enabled);
        editor.putString(PREF_EVENT_BRIGHTNESS_OPERATOR_FROM, String.valueOf(this._operatorFrom));
        editor.putString(PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_FROM, _brightnessLevelFrom);
        editor.putString(PREF_EVENT_BRIGHTNESS_OPERATOR_TO, String.valueOf(this._operatorTo));
        editor.putString(PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_TO, _brightnessLevelTo);
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_BRIGHTNESS_ENABLED, false);
        this._operatorFrom = Integer.parseInt(preferences.getString(PREF_EVENT_BRIGHTNESS_OPERATOR_FROM, "0"));
        this._brightnessLevelFrom = preferences.getString(PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_FROM, "50|0|1|0");
        this._operatorTo = Integer.parseInt(preferences.getString(PREF_EVENT_BRIGHTNESS_OPERATOR_TO, "0"));
        this._brightnessLevelTo = preferences.getString(PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_TO, "50|0|1|0");
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_brightness_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_BRIGHTNESS_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_brightness), addPassStatus, DatabaseHandler.ETYPE_BRIGHTNESS, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                String[] operators = context.getResources().getStringArray(R.array.brightnessSensorOperatorValues);

                _value.append(context.getString(R.string.event_preferences_brightness_level_from)).append(StringConstants.STR_COLON_WITH_SPACE);
                int index = Arrays.asList(operators).indexOf(Integer.toString(this._operatorFrom));
                if (index != -1) {
                    //_value.append(context.getString(R.string.event_preferences_brightness_operator_from)).append(StringConstants.STR_COLON_WITH_SPACE);
                    String[] operatorNames = context.getResources().getStringArray(R.array.brightnessSensorOperatorArray);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(operatorNames[index], disabled, context));
                    String value = this._brightnessLevelFrom;
                    int iValue = ProfileStatic.getDeviceBrightnessValue(value);
                    _value.append(" ").append(getColorForChangedPreferenceValue(iValue + "/100", disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                }

                _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_brightness_level_to)).append(StringConstants.STR_COLON_WITH_SPACE);
                index = Arrays.asList(operators).indexOf(Integer.toString(this._operatorTo));
                if (index != -1) {
                    //_value.append(context.getString(R.string.event_preferences_brightness_operator_from)).append(StringConstants.STR_COLON_WITH_SPACE);
                    String[] operatorNames = context.getResources().getStringArray(R.array.brightnessSensorOperatorArray);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(operatorNames[index], disabled, context));
                    String value = this._brightnessLevelTo;
                    int iValue = ProfileStatic.getDeviceBrightnessValue(value);
                    _value.append(" ").append(getColorForChangedPreferenceValue(iValue + "/100", disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
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

        if (key.equals(PREF_EVENT_BRIGHTNESS_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_BRIGHTNESS_OPERATOR_FROM) ||
            key.equals(PREF_EVENT_BRIGHTNESS_OPERATOR_TO))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }

        if (key.equals(PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_FROM) ||
            key.equals(PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_TO))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int iValue = ProfileStatic.getDeviceBrightnessValue(value);
                preference.setSummary(iValue + "/100");
            }
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences/*, Context context*/)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_BRIGHTNESS_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING/*, context*/);
        }
        if (key.equals(PREF_EVENT_BRIGHTNESS_OPERATOR_FROM) ||
                key.equals(PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_FROM) ||
                key.equals(PREF_EVENT_BRIGHTNESS_OPERATOR_TO) ||
                    key.equals(PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_TO))
        {
            setSummary(prefMng, key, preferences.getString(key, "")/*, context*/);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences/*,
                       Context context*/)
    {
        setSummary(prefMng, PREF_EVENT_BRIGHTNESS_ENABLED, preferences/*, context*/);
        setSummary(prefMng, PREF_EVENT_BRIGHTNESS_OPERATOR_FROM, preferences/*, context*/);
        setSummary(prefMng, PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_FROM, preferences/*, context*/);
        setSummary(prefMng, PREF_EVENT_BRIGHTNESS_OPERATOR_TO, preferences/*, context*/);
        setSummary(prefMng, PREF_EVENT_BRIGHTNESS_BRIGHTNESS_LEVEL_TO, preferences/*, context*/);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_BRIGHTNESS_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesBrightness tmp = new EventPreferencesBrightness(this._event, this._enabled, this._operatorFrom, this._brightnessLevelFrom, this._operatorTo, this._brightnessLevelTo);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_BRIGHTNESS_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_SCREEN_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_BRIGHTNESS).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_BRIGHTNESS_CATEGORY);
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
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_BRIGHTNESS_ENABLED) != null) {
                /*
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
                */

                setSummary(prefMng, PREF_EVENT_BRIGHTNESS_ENABLED, preferences/*, context*/);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    /*
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
    */

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
                if (PPApplication.isScreenOn && (!PPApplication.brightnessInternalChange)) {
                    // allowed only when screen is on, because of Huawei devices
                    // check ScreenOnOffBroadcastReceiver for this

                    eventsHandler.brightnessPassed = false;

                    int actualBrightness = Settings.System.getInt(eventsHandler.context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
                    if (actualBrightness > -1) {

                        boolean fromPassed = false;

                        int configuredFromValue = ProfileStatic.getDeviceBrightnessValue(_brightnessLevelFrom);
                        configuredFromValue = ProfileStatic.convertPercentsToBrightnessManualValue(configuredFromValue, eventsHandler.context);

                        switch (_operatorFrom) {
                            case 0: // equal to
                                if (actualBrightness == configuredFromValue)
                                    fromPassed = true;
                                break;
                            case 1: // do not equal to
                                if (actualBrightness != configuredFromValue)
                                    fromPassed = true;
                                break;
                            case 2: // is less then
                                if (actualBrightness < configuredFromValue)
                                    fromPassed = true;
                                break;
                            case 3: // is greather then
                                if (actualBrightness > configuredFromValue)
                                    fromPassed = true;
                                break;
                            case 4: // is less or equal to
                                if (actualBrightness <= configuredFromValue)
                                    fromPassed = true;
                                break;
                            case 5: // is greather or equal to
                                if (actualBrightness >= configuredFromValue)
                                    fromPassed = true;
                                break;
                        }

                        boolean toPassed = false;

                        int configuredToValue = ProfileStatic.getDeviceBrightnessValue(_brightnessLevelTo);
                        configuredToValue = ProfileStatic.convertPercentsToBrightnessManualValue(configuredToValue, eventsHandler.context);

                        switch (_operatorTo) {
                            case 0: // equal to
                                if (actualBrightness == configuredToValue)
                                    toPassed = true;
                                break;
                            case 1: // do not equal to
                                if (actualBrightness != configuredToValue)
                                    toPassed = true;
                                break;
                            case 2: // is less then
                                if (actualBrightness < configuredToValue)
                                    toPassed = true;
                                break;
                            case 3: // is greather then
                                if (actualBrightness > configuredToValue)
                                    toPassed = true;
                                break;
                            case 4: // is less or equal to
                                if (actualBrightness <= configuredToValue)
                                    toPassed = true;
                                break;
                            case 5: // is greather or equal to
                                if (actualBrightness >= configuredToValue)
                                    toPassed = true;
                                break;
                        }

                        eventsHandler.brightnessPassed = fromPassed && toPassed;
                    } else
                        eventsHandler.notAllowedBrightness = true;
                } else
                    eventsHandler.notAllowedBrightness = true;

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
