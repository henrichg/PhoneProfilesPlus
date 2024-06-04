package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesMusic extends EventPreferences {

    int _musicState;

    static final String PREF_EVENT_MUSIC_ENABLED = "eventMusicEnabled";
    private static final String PREF_EVENT_MUSIC_MUSIC_STATE = "eventMusicMusicState";

    static final String PREF_EVENT_MUSIC_CATEGORY = "eventMusicCategoryRoot";

    EventPreferencesMusic(Event event,
                          boolean enabled,
                          int musicState)
    {
        super(event, enabled);

        this._musicState = musicState;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesMusic._enabled;
        this._musicState = fromEvent._eventPreferencesMusic._musicState;
        this.setSensorPassed(fromEvent._eventPreferencesMusic.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_MUSIC_ENABLED, _enabled);
        editor.putString(PREF_EVENT_MUSIC_MUSIC_STATE, String.valueOf(this._musicState));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_MUSIC_ENABLED, false);
        this._musicState = Integer.parseInt(preferences.getString(PREF_EVENT_MUSIC_MUSIC_STATE, "0"));
    }

    /** @noinspection unused*/
    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_music_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_MUSIC_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_music), addPassStatus, DatabaseHandler.ETYPE_MUSIC, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                _value.append(context.getString(R.string.event_preferences_music_state));
                String[] musicSate = context.getResources().getStringArray(R.array.eventMusicStatesArray);
                _value.append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(musicSate[this._musicState], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
            }
        }

        return _value.toString();
    }

    /** @noinspection unused*/
    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_MUSIC_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_MUSIC_MUSIC_STATE)) {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }

        /*
        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesMusic.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesMusic.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_MUSIC_ENABLED, false);
        Preference applicationsPreference = prefMng.findPreference(PREF_EVENT_ALARM_CLOCK_APPLICATIONS);
        if (applicationsPreference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_ALARM_CLOCK_APPLICATIONS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(applicationsPreference, enabled, bold, false, false, !isRunnable, false);
        }
        */
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_MUSIC_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }

        if (key.equals(PREF_EVENT_MUSIC_MUSIC_STATE)) {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }

    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_MUSIC_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_MUSIC_MUSIC_STATE, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_MUSIC_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesMusic tmp = new EventPreferencesMusic(this._event, this._enabled, this._musicState);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_MUSIC_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_ALARM_CLOCK_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_MUSIC).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_MUSIC_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        StringConstants.STR_COLON_WITH_SPACE+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    boolean isRunnable(Context context)
    {
        return super.isRunnable(context);
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_MUSIC_ENABLED) != null) {
                setSummary(prefMng, PREF_EVENT_MUSIC_ENABLED, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    /*
    @Override
    void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING
    }

    @Override
    void removeSystemEvent(Context context)
    {
    }
    */

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if (EventStatic.isEventPreferenceAllowed(EventPreferencesMusic.PREF_EVENT_MUSIC_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                AudioManager audioManager = (AudioManager)eventsHandler.context.getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    if (_musicState == 1)
                        eventsHandler.musicPassed = !audioManager.isMusicActive();
                    else
                        eventsHandler.musicPassed = audioManager.isMusicActive();
                }
                else
                    eventsHandler.notAllowedMusic = true;

                if (!eventsHandler.notAllowedMusic) {
                    //noinspection ConstantValue
                    if (eventsHandler.musicPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedMusic = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_MUSIC);
            }
        }
    }

}
