package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.Calendar;

class EventPreferencesActivatedProfile extends EventPreferences {

    long _startProfile;
    long _endProfile;
    boolean _useDuration;
    boolean _permanentRun;
    int _duration;

    int _running;
    long _startTime;

    static final String PREF_EVENT_ACTIVATED_PROFILE_ENABLED = "eventActivatedProfileEnabled";
    private static final String PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE = "eventActivatedProfileStartProfile";
    private static final String PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE = "eventActivatedProfileEndProfile";
    private static final String PREF_EVENT_ACTIVATED_PROFILE_USE_DURATION = "eventActivatedProfileUseDuration";
    private static final String PREF_EVENT_ACTIVATED_PROFILE_PERMANENT_RUN = "eventActivatedProfilePermanentRun";
    private static final String PREF_EVENT_ACTIVATED_PROFILE_DURATION = "eventActivatedProfileDuration";

    static final String PREF_EVENT_ACTIVATED_PROFILE_CATEGORY = "eventActivatedProfileCategoryRoot";

    static final int RUNNING_NOTSET = 0;
    static final int RUNNING_RUNNING = 1;
    static final int RUNNING_NOTRUNNING = 2;

    EventPreferencesActivatedProfile(Event event,
                                     boolean enabled,
                                     long startProfile,
                                     long endProfile,
                                     boolean useDuration,
                                     boolean permanentRun,
                                     int duration)
    {
        super(event, enabled);

        this._startProfile = startProfile;
        this._endProfile = endProfile;
        this._useDuration = useDuration;
        this._permanentRun = permanentRun;
        this._duration = duration;

        this._startTime = 0;
        this._running = RUNNING_NOTSET;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesActivatedProfile._enabled;
        this._startProfile = fromEvent._eventPreferencesActivatedProfile._startProfile;
        this._endProfile = fromEvent._eventPreferencesActivatedProfile._endProfile;
        this._useDuration = fromEvent._eventPreferencesActivatedProfile._useDuration;
        this._permanentRun = fromEvent._eventPreferencesActivatedProfile._permanentRun;
        this._duration = fromEvent._eventPreferencesActivatedProfile._duration;
        this.setSensorPassed(fromEvent._eventPreferencesActivatedProfile.getSensorPassed());

        this._startTime = 0;
        this._running = RUNNING_NOTSET;
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_ACTIVATED_PROFILE_ENABLED, _enabled);
        editor.putString(PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE, String.valueOf(this._startProfile));
        editor.putString(PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE, String.valueOf(this._endProfile));
        editor.putBoolean(PREF_EVENT_ACTIVATED_PROFILE_USE_DURATION, this._useDuration);
        editor.putBoolean(PREF_EVENT_ACTIVATED_PROFILE_PERMANENT_RUN, this._permanentRun);
        editor.putString(PREF_EVENT_ACTIVATED_PROFILE_DURATION, String.valueOf(this._duration));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_ACTIVATED_PROFILE_ENABLED, false);
        this._startProfile = Long.parseLong(preferences.getString(PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE, "0"));
        this._endProfile = Long.parseLong(preferences.getString(PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE, "0"));
        this._useDuration = preferences.getBoolean(PREF_EVENT_ACTIVATED_PROFILE_USE_DURATION, false);
        this._permanentRun = preferences.getBoolean(PREF_EVENT_ACTIVATED_PROFILE_PERMANENT_RUN, true);
        this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_ACTIVATED_PROFILE_DURATION, "5"));

        // set it to NOSET when parameters are changed
        this._running = RUNNING_NOTSET;
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_activated_profile_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_ACTIVATED_PROFILE_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_activated_profile), addPassStatus, DatabaseHandler.ETYPE_ACTIVATED_PROFILE, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                _value.append(context.getString(R.string.event_preferences_activated_profile_startProfile)).append(StringConstants.STR_COLON_WITH_SPACE);
                DataWrapper dataWrapper = new DataWrapper(context, false, 0, false, 0, 0, 0f);
                String profileName = dataWrapper.getProfileName(this._startProfile);
                //noinspection ReplaceNullCheck
                if (profileName != null) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(profileName, disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                } else {
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.profile_preference_profile_not_set), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                }

                if (!this._useDuration) {
                    _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_activated_profile_endProfile)).append(StringConstants.STR_COLON_WITH_SPACE);
                    profileName = dataWrapper.getProfileName(this._endProfile);
                    //noinspection ReplaceNullCheck
                    if (profileName != null) {
                        _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(profileName, disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    } else {
                        _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.profile_preference_profile_not_set), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    }
                } else {
                    if (this._permanentRun)
                        _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.pref_event_permanentRun), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    else
                        _value.append(context.getString(R.string.pref_event_duration)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(this._duration), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                }
                dataWrapper.invalidateDataWrapper();
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_ACTIVATED_PROFILE_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE) ||
            key.equals(PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE))
        {
            ProfilePreference preference = prefMng.findPreference(key);
            if (preference != null) {
                long lProfileId;
                try {
                    lProfileId = Long.parseLong(value);
                } catch (Exception e) {
                    lProfileId = 0;
                }
                preference.setSummary(lProfileId);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, (lProfileId != 0) && (lProfileId != Profile.PROFILE_NO_ACTIVATE), false, true, lProfileId == 0, false);
            }
        }

        if (key.equals(PREF_EVENT_ACTIVATED_PROFILE_PERMANENT_RUN)) {
            SwitchPreferenceCompat permanentRunPreference = prefMng.findPreference(key);
            if (permanentRunPreference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(permanentRunPreference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
            Preference preference = prefMng.findPreference(PREF_EVENT_ACTIVATED_PROFILE_DURATION);
            if (preference != null) {
                preference.setEnabled(value.equals(StringConstants.FALSE_STRING));
            }
        }
        if (key.equals(PREF_EVENT_ACTIVATED_PROFILE_DURATION)) {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 5;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 5, false, false, false, false);
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesActivatedProfile.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesActivatedProfile.isRunnable(context);
        //boolean isAllConfigured = event._eventPreferencesActivatedProfile.isAllConfigured(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_ACTIVATED_PROFILE_ENABLED, false);
        ProfilePreference preference = prefMng.findPreference(PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE, "0").equals("0");
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE, "0").equals("0");
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_ACTIVATED_PROFILE_ENABLED) ||
                key.equals(PREF_EVENT_ACTIVATED_PROFILE_USE_DURATION) ||
                key.equals(PREF_EVENT_ACTIVATED_PROFILE_PERMANENT_RUN)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
        if (key.equals(PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE) ||
                key.equals(PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE) ||
                key.equals(PREF_EVENT_ACTIVATED_PROFILE_DURATION)) {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE, preferences, context);
        setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE, preferences, context);
        setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_USE_DURATION, preferences, context);
        setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_PERMANENT_RUN, preferences, context);
        setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_DURATION, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_ACTIVATED_PROFILE_ENABLED, false, context);
        if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesActivatedProfile tmp = new EventPreferencesActivatedProfile(this._event, this._enabled, this._startProfile, this._endProfile, this._useDuration, this._permanentRun, this._duration);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_ACTIVATED_PROFILE_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_ACTIVATED_PROFILE_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_ACTIVATED_PROFILE).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && tmp.isAllConfigured(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_ACTIVATED_PROFILE_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        StringConstants.STR_COLON_WITH_SPACE+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context)
    {
        boolean runnable = super.isRunnable(context);

        if (this._useDuration)
            runnable = runnable && (this._startProfile != 0);
        else
            runnable = runnable && (this._startProfile != 0) && (this._endProfile != 0)
                          && (this._startProfile != this._endProfile);

        return runnable;
    }

    @Override
    boolean isAllConfigured(Context context)
    {
        boolean allConfigured = super.isAllConfigured(context);

        if (this._useDuration)
            allConfigured = allConfigured && (this._startProfile != 0);
        else {
            allConfigured = allConfigured && (this._startProfile != 0) && (this._endProfile != 0);
        }

        return allConfigured;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_ACTIVATED_PROFILE_ENABLED) != null) {
                setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_START_PROFILE, preferences, context);
                setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_END_PROFILE, preferences, context);

                boolean enabled = EventStatic.isEventPreferenceAllowed(PREF_EVENT_ACTIVATED_PROFILE_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED;
                Preference permanentRunPreference = prefMng.findPreference(PREF_EVENT_ACTIVATED_PROFILE_PERMANENT_RUN);
                if (permanentRunPreference != null)
                    permanentRunPreference.setEnabled(enabled);

                if (preferences != null) {
                    Preference durationPreference = prefMng.findPreference(PREF_EVENT_ACTIVATED_PROFILE_DURATION);
                    boolean permanentRun = preferences.getBoolean(PREF_EVENT_ACTIVATED_PROFILE_PERMANENT_RUN, false);
                    enabled = enabled && (!permanentRun);
                    if (durationPreference != null)
                        durationPreference.setEnabled(enabled);
                }

                setSummary(prefMng, PREF_EVENT_ACTIVATED_PROFILE_ENABLED, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    private long computeAlarm()
    {
        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        calEndTime.setTimeInMillis((_startTime - gmtOffset) + (_duration * 1000L));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = calEndTime.getTimeInMillis();

        return alarmTime;
    }

    @Override
    void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        removeAlarm(context);
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        removeAlarm(context);

        if (!(isRunnable(context) && isAllConfigured(context) && _enabled))
            return;

        setAlarm(computeAlarm(), context);
    }

    @Override
    void removeSystemEvent(Context context)
    {
        removeAlarm(context);
    }

    void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, NFCEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_ACTIVATED_PROFILE_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, NFCEventEndBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_NFC_EVENT_SENSOR_TAG_WORK+"_" + (int) _event._id);
    }

    private void setAlarm(long alarmTime, Context context)
    {
        if (!_permanentRun) {
            if (_startTime > 0) {
                //Intent intent = new Intent(context, NFCEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_ACTIVATED_PROFILE_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, NFCEventEndBroadcastReceiver.class);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo;
                        if (_duration * 1000L >= Event.EVENT_ALARM_TIME_SOFT_OFFSET)
                            clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        else
                            clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                    else {
                        if (_duration * 1000L >= Event.EVENT_ALARM_TIME_OFFSET)
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                        else
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    }
                }
            }
        }
    }

    void saveStartTime(DataWrapper dataWrapper) {
        if (this._startTime == 0) {
            // alarm for end is not set

            long activatedProfile = dataWrapper.getActivatedProfileId();
            boolean startProfileActivated = activatedProfile == this._startProfile;

            if (startProfileActivated) {
                Calendar calendar = Calendar.getInstance();
                this._startTime = calendar.getTimeInMillis();
            }
            else
                this._startTime = 0;

            DatabaseHandler.getInstance(dataWrapper.context).updateActivatedProfileStartTime(_event);

            if (startProfileActivated) {
                //if (_event.getStatus() == Event.ESTATUS_RUNNING)
                setSystemEventForPause(dataWrapper.context);
            }
        }
    }


    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if (EventStatic.isEventPreferenceAllowed(EventPreferencesActivatedProfile.PREF_EVENT_ACTIVATED_PROFILE_ENABLED, false, eventsHandler.context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (this._useDuration) {
                    if (_startTime > 0) {
                        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                        long startTime = _startTime - gmtOffset;

                        // compute end datetime
                        long endAlarmTime = computeAlarm();

                        Calendar now = Calendar.getInstance();
                        long nowAlarmTime = now.getTimeInMillis();

                        if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_ACTIVATED_PROFILE))
                            eventsHandler.activatedProfilePassed = true;
                        else if (!_permanentRun) {
                            if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_ACTIVATED_PROFILE_EVENT_END))
                                eventsHandler.activatedProfilePassed = false;
                            else
                                eventsHandler.activatedProfilePassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                        } else
                            eventsHandler.activatedProfilePassed = nowAlarmTime >= startTime;
                    } else
                        eventsHandler.activatedProfilePassed = false;

                    if (!eventsHandler.activatedProfilePassed) {
                        _startTime = 0;
                        DatabaseHandler.getInstance(eventsHandler.context).updateActivatedProfileStartTime(_event);
                    }

                } else {
                    if ((this._startProfile != 0) && (this._endProfile != 0)) {
                        eventsHandler.activatedProfilePassed =
                                this._running == RUNNING_RUNNING;
                    } else
                        eventsHandler.notAllowedActivatedProfile = true;
                }

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
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_ACTIVATED_PROFILE);
            }
        }
    }

}
