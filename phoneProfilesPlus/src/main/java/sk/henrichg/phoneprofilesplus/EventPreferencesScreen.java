package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
//import android.preference.CheckBoxPreference;
//import android.preference.ListPreference;
//import android.preference.Preference;
//import android.preference.Preference.OnPreferenceChangeListener;
//import android.preference.PreferenceManager;

import java.util.Arrays;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesScreen extends EventPreferences {

    int _eventType;
    boolean _whenUnlocked;

    static final int ETYPE_SCREENON = 0;
    //static final int ETYPE_SCREENOFF = 1;

    static final String PREF_EVENT_SCREEN_ENABLED = "eventScreenEnabled";
    private static final String PREF_EVENT_SCREEN_EVENT_TYPE = "eventScreenEventType";
    private static final String PREF_EVENT_SCREEN_WHEN_UNLOCKED = "eventScreenWhenUnlocked";

    private static final String PREF_EVENT_SCREEN_CATEGORY = "eventScreenCategoryRoot";

    EventPreferencesScreen(Event event,
                                    boolean enabled,
                                    int eventType,
                                    boolean whenUnlocked)
    {
        super(event, enabled);

        this._eventType = eventType;
        this._whenUnlocked = whenUnlocked;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesScreen._enabled;
        this._eventType = fromEvent._eventPreferencesScreen._eventType;
        this._whenUnlocked = fromEvent._eventPreferencesScreen._whenUnlocked;
        this.setSensorPassed(fromEvent._eventPreferencesScreen.getSensorPassed());
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_SCREEN_ENABLED, _enabled);
        editor.putString(PREF_EVENT_SCREEN_EVENT_TYPE, String.valueOf(this._eventType));
        editor.putBoolean(PREF_EVENT_SCREEN_WHEN_UNLOCKED, _whenUnlocked);
        editor.apply();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_SCREEN_ENABLED, false);
        this._eventType = Integer.parseInt(preferences.getString(PREF_EVENT_SCREEN_EVENT_TYPE, "1"));
        this._whenUnlocked = preferences.getBoolean(PREF_EVENT_SCREEN_WHEN_UNLOCKED, false);
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_screen_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_SCREEN_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_screen), addPassStatus, DatabaseHandler.ETYPE_SCREEN, context);
                    descr = descr + ": </b>";
                }

                descr = descr + context.getString(R.string.event_preferences_screen_event_type) + ": ";
                String[] eventListTypeNames = context.getResources().getStringArray(R.array.eventScreenEventTypeArray);
                String[] eventListTypes = context.getResources().getStringArray(R.array.eventScreenEventTypeValues);
                int index = Arrays.asList(eventListTypes).indexOf(Integer.toString(this._eventType));
                descr = descr + "<b>" + eventListTypeNames[index] + "</b>";

                if (this._whenUnlocked) {
                    if (this._eventType == 0)
                        descr = descr + " • <b>" + context.getString(R.string.pref_event_screen_startWhenUnlocked) + "</b>";
                    else
                        descr = descr + " • <b>" + context.getString(R.string.pref_event_screen_startWhenLocked) + "</b>";
                }
            }
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_SCREEN_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_SCREEN_EVENT_TYPE))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_SCREEN_WHEN_UNLOCKED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_SCREEN_ENABLED) ||
            key.equals(PREF_EVENT_SCREEN_WHEN_UNLOCKED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
        if (key.equals(PREF_EVENT_SCREEN_EVENT_TYPE))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_SCREEN_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_SCREEN_EVENT_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_SCREEN_WHEN_UNLOCKED, preferences, context);

        setWhenUnlockedTitle(prefMng, _eventType);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_SCREEN_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesScreen tmp = new EventPreferencesScreen(this._event, this._enabled, this._eventType, this._whenUnlocked);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_SCREEN_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_SCREEN_ENABLED, false);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_SCREEN_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context)
    {
        final Preference eventTypePreference = prefMng.findPreference(PREF_EVENT_SCREEN_EVENT_TYPE);
        final PreferenceManager _prefMng = prefMng;

        if (eventTypePreference != null) {
            eventTypePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String sNewValue = (String) newValue;
                    int iNewValue;
                    if (sNewValue.isEmpty())
                        iNewValue = 100;
                    else
                        iNewValue = Integer.parseInt(sNewValue);

                    setWhenUnlockedTitle(_prefMng, iNewValue);

                    return true;
                }
            });
        }
    }

    private void setWhenUnlockedTitle(PreferenceManager prefMng, int value)
    {
        final SwitchPreferenceCompat whenUnlockedPreference = prefMng.findPreference(PREF_EVENT_SCREEN_WHEN_UNLOCKED);

        if (whenUnlockedPreference != null) {
            if (value == 0)
                whenUnlockedPreference.setTitle(R.string.event_preferences_screen_start_when_unlocked);
            else
                whenUnlockedPreference.setTitle(R.string.event_preferences_screen_start_when_locked);
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
