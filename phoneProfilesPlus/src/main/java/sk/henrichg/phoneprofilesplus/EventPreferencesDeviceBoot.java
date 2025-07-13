package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.Calendar;

class EventPreferencesDeviceBoot extends EventPreferences {

    long _startTime;
    boolean _permanentRun;
    int _duration;

    static final String PREF_EVENT_DEVICE_BOOT_ENABLED = "eventDeviceBootEnabled";
    private static final String PREF_EVENT_DEVICE_BOOT_PERMANENT_RUN = "eventDeviceBootPermanentRun";
    private static final String PREF_EVENT_DEVICE_BOOT_DURATION = "eventDeviceBootDuration";

    static final String PREF_EVENT_DEVICE_BOOT_CATEGORY = "eventDeviceBootCategoryRoot";

    EventPreferencesDeviceBoot(Event event,
                               boolean enabled,
                               boolean permanentRun,
                               int duration)
    {
        super(event, enabled);

        this._permanentRun = permanentRun;
        this._duration = duration;

        this._startTime = 0;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesDeviceBoot._enabled;
        this._permanentRun = fromEvent._eventPreferencesDeviceBoot._permanentRun;
        this._duration = fromEvent._eventPreferencesDeviceBoot._duration;
        this.setSensorPassed(fromEvent._eventPreferencesDeviceBoot.getSensorPassed());

        this._startTime = 0;
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_DEVICE_BOOT_ENABLED, _enabled);
        editor.putBoolean(PREF_EVENT_DEVICE_BOOT_PERMANENT_RUN, this._permanentRun);
        editor.putString(PREF_EVENT_DEVICE_BOOT_DURATION, String.valueOf(this._duration));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_DEVICE_BOOT_ENABLED, false);
        this._permanentRun = preferences.getBoolean(PREF_EVENT_DEVICE_BOOT_PERMANENT_RUN, false);
        this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_DEVICE_BOOT_DURATION, "5"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_device_boot_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_DEVICE_BOOT_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_device_boot), addPassStatus, DatabaseHandler.ETYPE_DEVICE_BOOT, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                if (this._permanentRun)
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.pref_event_permanentRun), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                else
                    _value.append(context.getString(R.string.pref_event_duration)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(this._duration), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value/*, Context context*/)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_DEVICE_BOOT_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_DEVICE_BOOT_PERMANENT_RUN)) {
            SwitchPreferenceCompat permanentRunPreference = prefMng.findPreference(key);
            if (permanentRunPreference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(permanentRunPreference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
            Preference preference = prefMng.findPreference(PREF_EVENT_DEVICE_BOOT_DURATION);
            if (preference != null) {
                preference.setEnabled(value.equals(StringConstants.FALSE_STRING));
            }
        }
        if (key.equals(PREF_EVENT_DEVICE_BOOT_DURATION)) {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 5;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 5, false, false, false, false);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences/*, Context context*/)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_DEVICE_BOOT_ENABLED) ||
            key.equals(PREF_EVENT_DEVICE_BOOT_PERMANENT_RUN)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING/*, context*/);
        }
        if (key.equals(PREF_EVENT_DEVICE_BOOT_DURATION))
        {
            setSummary(prefMng, key, preferences.getString(key, "")/*, context*/);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences/*, Context context*/)
    {
        setSummary(prefMng, PREF_EVENT_DEVICE_BOOT_ENABLED, preferences);
        setSummary(prefMng, PREF_EVENT_DEVICE_BOOT_PERMANENT_RUN, preferences);
        setSummary(prefMng, PREF_EVENT_DEVICE_BOOT_DURATION, preferences);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_DEVICE_BOOT_ENABLED, false, context);
        if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesDeviceBoot tmp = new EventPreferencesDeviceBoot(this._event, this._enabled, this._permanentRun, this._duration);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_DEVICE_BOOT_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_DEVICE_BOOT_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_DEVICE_BOOT).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && tmp.isAllConfigured(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_DEVICE_BOOT_CATEGORY);
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
        return super.isRunnable(context);
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_DEVICE_BOOT_ENABLED) != null) {
                setSummary(prefMng, PREF_EVENT_DEVICE_BOOT_ENABLED, preferences);
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
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_DEVICE_BOOT_EVENT_END_BROADCAST_RECEIVER);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_DEVICE_BOOT_EVENT_SENSOR_TAG_WORK+"_" + (int) _event._id);
    }

    private void setAlarm(long alarmTime, Context context)
    {
        if (!_permanentRun) {
            if (_startTime > 0) {
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_DEVICE_BOOT_EVENT_END_BROADCAST_RECEIVER);

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

    void saveStartTime(DataWrapper dataWrapper, long startTime) {
        if (this._startTime == 0) {
            // alarm for end is not set

            this._startTime = startTime; // + (10 * 1000);

            DatabaseHandler.getInstance(dataWrapper.context).updateDeviceBootStartTime(_event);

            setSystemEventForPause(dataWrapper.context);
        }
    }

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if (EventStatic.isEventPreferenceAllowed(EventPreferencesDeviceBoot.PREF_EVENT_DEVICE_BOOT_ENABLED, false, eventsHandler.context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                // compute start time

                if (_startTime > 0) {
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    long startTime = _startTime - gmtOffset;

                    // compute end datetime
                    long endAlarmTime = computeAlarm();

                    Calendar now = Calendar.getInstance();
                    long nowAlarmTime = now.getTimeInMillis();

                    if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_DEVICE_BOOT))
                        eventsHandler.deviceBootPassed = true;
                    else if (!_permanentRun) {
                        if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_DEVICE_BOOT_EVENT_END))
                            eventsHandler.deviceBootPassed = false;
                        else
                            eventsHandler.deviceBootPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                    } else {
                        eventsHandler.deviceBootPassed = nowAlarmTime >= startTime;
                    }
                } else
                    eventsHandler.deviceBootPassed = false;

                if (!eventsHandler.deviceBootPassed) {
                    _startTime = 0;
                    DatabaseHandler.getInstance(eventsHandler.context).updateDeviceBootStartTime(_event);
                }

                if (!eventsHandler.notAllowedDeviceBoot) {
                    if (eventsHandler.deviceBootPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedDeviceBoot = true;

            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_DEVICE_BOOT);
            }
        }
    }

}
