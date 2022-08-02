package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesActivatedProfile extends EventPreferences {

    long _startProfile;
    long _endProfile;

    int _running;

    static final String PREF_EVENT_ACTIVATED_PROFILE_ENABLED = "eventActivatedProfileEnabled";
    private static final String PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE = "eventActivatedProfileStartProfile";
    private static final String PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE = "eventActivatedProfileEndProfile";

    private static final String PREF_EVENT_ACTIVATED_PROFILE_CATEGORY = "eventActivatedProfileCategoryRoot";

    static final int RUNNING_NOTSET = 0;
    static final int RUNNING_RUNNING = 1;
    static final int RUNNING_NOTRUNNING = 2;

    EventPreferencesActivatedProfile(Event event,
                                     boolean enabled,
                                     long startProfile,
                                     long endProfile)
    {
        super(event, enabled);

        this._startProfile = startProfile;
        this._endProfile = endProfile;

        this._running = RUNNING_NOTSET;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesActivatedProfile._enabled;
        this._startProfile = fromEvent._eventPreferencesActivatedProfile._startProfile;
        this._endProfile = fromEvent._eventPreferencesActivatedProfile._endProfile;
        this.setSensorPassed(fromEvent._eventPreferencesActivatedProfile.getSensorPassed());

        this._running = RUNNING_NOTSET;
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_ACTIVATED_PROFILE_ENABLED, _enabled);
        editor.putString(PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE, String.valueOf(this._startProfile));
        editor.putString(PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE, String.valueOf(this._endProfile));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_ACTIVATED_PROFILE_ENABLED, false);
        this._startProfile = Long.parseLong(preferences.getString(PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE, "0"));
        this._endProfile = Long.parseLong(preferences.getString(PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE, "0"));

        // set it to NOSET when parameters are changed
        this._running = RUNNING_NOTSET;
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_activated_profile_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_ACTIVATED_PROFILE_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_activated_profile), addPassStatus, DatabaseHandler.ETYPE_ACTIVATED_PROFILE, context);
                    descr = descr + "</b> ";
                }

                descr = descr + context.getString(R.string.event_preferences_activated_profile_startProfile) + ": ";
                DataWrapper dataWrapper = new DataWrapper(context, false, 0, false, 0, 0, 0f);
                Profile profile = dataWrapper.getProfileById(this._startProfile, true, true, false);
                if (profile != null) {
                    descr = descr + "<b>" + profile._name + "</b>";
                } else {
                    descr = descr + "<b>" + context.getString(R.string.profile_preference_profile_not_set) + "</b>";
                }

                descr = descr + " • " + context.getString(R.string.event_preferences_activated_profile_endProfile) + ": ";
                profile = dataWrapper.getProfileById(this._endProfile, true, true, false);
                if (profile != null) {
                    descr = descr + "<b>" + profile._name + "</b>";
                } else {
                    descr = descr + "<b>" + context.getString(R.string.profile_preference_profile_not_set) + "</b>";
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

        if (key.equals(PREF_EVENT_ACTIVATED_PROFILE_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE) ||
            key.equals(PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE))
        {
            ProfilePreferenceX preference = prefMng.findPreference(key);
            if (preference != null) {
                long lProfileId;
                try {
                    lProfileId = Long.parseLong(value);
                } catch (Exception e) {
                    lProfileId = 0;
                }
                preference.setSummary(lProfileId);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, (lProfileId != 0) && (lProfileId != Profile.PROFILE_NO_ACTIVATE), false, true, lProfileId == 0);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesActivatedProfile.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesActivatedProfile.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_ACTIVATED_PROFILE_ENABLED, false);
        ProfilePreferenceX preference = prefMng.findPreference(PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE, "0").equals("0");
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable);
        }
        preference = prefMng.findPreference(PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE, "0").equals("0");
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_ACTIVATED_PROFILE_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true" : "false", context);
        }
        if (key.equals(PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE) ||
                key.equals(PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE)) {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE, preferences, context);
        setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_ACTIVATED_PROFILE_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesActivatedProfile tmp = new EventPreferencesActivatedProfile(this._event, this._enabled, this._startProfile, this._endProfile);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_ACTIVATED_PROFILE_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_ACTIVATED_PROFILE_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_ACTIVATED_PROFILE).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && permissionGranted));
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_ACTIVATED_PROFILE_CATEGORY);
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
        boolean runnable = super.isRunnable(context);

        runnable = runnable && (this._startProfile != 0) && (this._endProfile != 0)
                      && (this._startProfile != this._endProfile);

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_ACTIVATED_PROFILE_ENABLED) != null) {
                setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE, preferences, context);
                setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
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
            if (Event.isEventPreferenceAllowed(EventPreferencesActivatedProfile.PREF_EVENT_ACTIVATED_PROFILE_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if ((this._startProfile != 0) && (this._endProfile != 0)) {
                    eventsHandler.activatedProfilePassed =
                            this._running == RUNNING_RUNNING;
                }
                else
                    eventsHandler.notAllowedActivatedProfile = true;

                if (!eventsHandler.notAllowedActivatedProfile) {
                    if (eventsHandler.activatedProfilePassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedActivatedProfile = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                //PPApplication.logE("[TEST BATTERY] EventPreferencesActivatedProfile.doHandleEvent", "activated profile - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_ACTIVATED_PROFILE);
            }
        }
    }

}
