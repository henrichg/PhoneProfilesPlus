package sk.henrichg.phoneprofilesplus;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;

class EventPreferencesScreen extends EventPreferences {

    int _eventType;
    boolean _whenUnlocked;

    private static final int ETYPE_SCREENON = 0;
    //static final int ETYPE_SCREENOFF = 1;

    static final String PREF_EVENT_SCREEN_ENABLED = "eventScreenEnabled";
    private static final String PREF_EVENT_SCREEN_EVENT_TYPE = "eventScreenEventType";
    private static final String PREF_EVENT_SCREEN_WHEN_UNLOCKED = "eventScreenWhenUnlocked";

    static final String PREF_EVENT_SCREEN_CATEGORY = "eventScreenCategoryRoot";

    EventPreferencesScreen(Event event,
                                    boolean enabled,
                                    int eventType,
                                    boolean whenUnlocked)
    {
        super(event, enabled);

        this._eventType = eventType;
        this._whenUnlocked = whenUnlocked;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesScreen._enabled;
        this._eventType = fromEvent._eventPreferencesScreen._eventType;
        this._whenUnlocked = fromEvent._eventPreferencesScreen._whenUnlocked;
        this.setSensorPassed(fromEvent._eventPreferencesScreen.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_SCREEN_ENABLED, _enabled);
        editor.putString(PREF_EVENT_SCREEN_EVENT_TYPE, String.valueOf(this._eventType));
        editor.putBoolean(PREF_EVENT_SCREEN_WHEN_UNLOCKED, _whenUnlocked);
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_SCREEN_ENABLED, false);
        this._eventType = Integer.parseInt(preferences.getString(PREF_EVENT_SCREEN_EVENT_TYPE, "1"));
        this._whenUnlocked = preferences.getBoolean(PREF_EVENT_SCREEN_WHEN_UNLOCKED, false);
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_screen_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_SCREEN_ENABLED, false, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_screen), addPassStatus, DatabaseHandler.ETYPE_SCREEN, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                String[] eventListTypes = context.getResources().getStringArray(R.array.eventScreenEventTypeValues);
                int index = Arrays.asList(eventListTypes).indexOf(Integer.toString(this._eventType));
                if (index != -1) {
                    _value.append(context.getString(R.string.event_preferences_screen_event_type)).append(StringConstants.STR_COLON_WITH_SPACE);
                    String[] eventListTypeNames = context.getResources().getStringArray(R.array.eventScreenEventTypeArray);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(eventListTypeNames[index], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                }

                if (this._whenUnlocked) {
                    if (this._eventType == EventPreferencesScreen.ETYPE_SCREENON)
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

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences/*, Context context*/)
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

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences/*, Context context*/)
    {
        setSummary(prefMng, PREF_EVENT_SCREEN_ENABLED, preferences);
        setSummary(prefMng, PREF_EVENT_SCREEN_EVENT_TYPE, preferences);
        setSummary(prefMng, PREF_EVENT_SCREEN_WHEN_UNLOCKED, preferences);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_SCREEN_ENABLED, false, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesScreen tmp = new EventPreferencesScreen(this._event, this._enabled, this._eventType, this._whenUnlocked);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_SCREEN_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_SCREEN_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_SCREEN).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && tmp.isAllConfigured(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
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
        super.checkPreferences(prefMng, onlyCategory, context);
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

                setSummary(prefMng, PREF_EVENT_SCREEN_ENABLED, preferences);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    private void setWhenUnlockedTitle(PreferenceManager prefMng, int eventTypeValue)
    {
        final SwitchPreferenceCompat whenUnlockedPreference = prefMng.findPreference(PREF_EVENT_SCREEN_WHEN_UNLOCKED);

        if (whenUnlockedPreference != null) {
            if (eventTypeValue == EventPreferencesScreen.ETYPE_SCREENON)
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
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesScreen.PREF_EVENT_SCREEN_ENABLED, false, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                //boolean isScreenOn;
                //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                boolean keyguardShowing = false;

                if (_whenUnlocked) {
                    KeyguardManager kgMgr = (KeyguardManager) eventsHandler.context.getSystemService(Context.KEYGUARD_SERVICE);
                    if (kgMgr == null)
                        eventsHandler.notAllowedScreen = true;
                    else
                        keyguardShowing = kgMgr.isKeyguardLocked();
                }

                if (!eventsHandler.notAllowedScreen) {
                    if (_eventType == EventPreferencesScreen.ETYPE_SCREENON) {
                        // event type = screen is on
                        if (_whenUnlocked)
                            // passed if screen is on and unlocked
                            eventsHandler.screenPassed = PPApplication.isScreenOn && (!keyguardShowing);
                        else
                            eventsHandler.screenPassed = PPApplication.isScreenOn;
                    } else {
                        // event type = screen is off
                        if (_whenUnlocked)
                            // passed if screen is off and locked
                            eventsHandler.screenPassed = (!PPApplication.isScreenOn) && keyguardShowing;
                        else
                            eventsHandler.screenPassed = !PPApplication.isScreenOn;
                    }

                }

                if (!eventsHandler.notAllowedScreen) {
                    if (eventsHandler.screenPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedScreen = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_SCREEN);
            }
        }
    }

}
