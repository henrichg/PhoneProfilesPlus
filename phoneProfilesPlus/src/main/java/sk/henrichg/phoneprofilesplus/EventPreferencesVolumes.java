package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesVolumes extends EventPreferences {

    static boolean internalChange = false;

    static final String PREF_EVENT_VOLUMES_ENABLED = "eventVolumesEnabled";

    private static final String PREF_EVENT_VOLUMES_CATEGORY = "eventVolumesCategoryRoot";

    EventPreferencesVolumes(Event event,
                            boolean enabled)
    {
        super(event, enabled);
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesVolumes._enabled;
        this.setSensorPassed(fromEvent._eventPreferencesVolumes.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_VOLUMES_ENABLED, _enabled);
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_VOLUMES_ENABLED, false);
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_volumes_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_VOLUMES_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_volumes), addPassStatus, DatabaseHandler.ETYPE_VOLUMES, context);
                    descr = descr + "</b> ";
                }

            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_VOLUMES_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesVolumes.saveSharedPreferences(prefMng.getSharedPreferences());

    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences,
                    @SuppressWarnings("unused") Context context)
    {
        if (key.equals(PREF_EVENT_VOLUMES_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_VOLUMES_ENABLED, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_VOLUMES_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesVolumes tmp = new EventPreferencesVolumes(this._event, this._enabled);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_VOLUMES_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_VOLUMES_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_VOLUMES).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !(tmp.isRunnable(context) && permissionGranted));
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_VOLUMES_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context)
    {
        return super.isRunnable(context);
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        setCategorySummary(prefMng, preferences, context);
    }
/*
    @Override
    void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        //PPApplication.logE("EventPreferencesVolumes.setSystemRunningEvent","xxx");
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        //PPApplication.logE("[BOOT] EventPreferencesVolumes.setSystemPauseEvent","xxx");
    }

    @Override
    void removeSystemEvent(Context context)
    {
        //PPApplication.logE("EventPreferencesVolumes.removeSystemEvent", "xxx");
    }
*/
    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
//            PPApplication.logE("######### EventPreferencesVolumes.doHandleEvent", "xxx");
            int oldSensorPassed = getSensorPassed();
            if (Event.isEventPreferenceAllowed(EventPreferencesVolumes.PREF_EVENT_VOLUMES_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                //PPApplication.logE("[BOOT] EventPreferencesVolumes.doHandleEvent", "allowed");

            } else
                eventsHandler.notAllowedVolumes = true;

//            PPApplication.logE("######### EventPreferencesVolumes.doHandleEvent", "volumesPassed=" + eventsHandler.volumesPassed);
//            PPApplication.logE("######### EventPreferencesVolumes.doHandleEvent", "notAllowedVolumes=" + eventsHandler.notAllowedVolumes);

            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
//                PPApplication.logE("######### EventPreferencesVolumes.doHandleEvent", "volumes - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_VOLUMES);
            }
        }
    }

}
