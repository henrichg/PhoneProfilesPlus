package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.Calendar;

class EventPreferencesPeriodic extends EventPreferences {

    int _multipleInterval;
    int _duration;

    int _counter;
    long _startTime;


    static final String PREF_EVENT_PERIODIC_ENABLED = "eventPeriodicEnabled";
    private static final String PREF_EVENT_PERIODIC_MULTIPLE_INTERVAL = "eventPeriodicMultipleInterval";
    private static final String PREF_EVENT_PERIODIC_DURATION = "eventPeriodicDuration";
    static final String PREF_EVENT_PERIODIC_APP_SETTINGS = "eventEnablePeriodicScanningAppSettings";
    private static final String PREF_EVENT_PERIODIC_RESULTING_INTERVAL = "eventPeriodicResultingInterval";

    static final String PREF_EVENT_PERIODIC_CATEGORY = "eventPeriodicCategoryRoot";

    EventPreferencesPeriodic(Event event,
                             boolean enabled,
                             int multipleInterval,
                             int duration)
    {
        super(event, enabled);

        this._multipleInterval = multipleInterval;
        this._duration = duration;

        this._counter = 0;
        this._startTime = 0;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesPeriodic._enabled;
        this._multipleInterval = fromEvent._eventPreferencesPeriodic._multipleInterval;
        this._duration = fromEvent._eventPreferencesPeriodic._duration;
        this.setSensorPassed(fromEvent._eventPreferencesPeriodic.getSensorPassed());

        this._counter = 0;
        this._startTime = 0;
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_PERIODIC_ENABLED, _enabled);
        editor.putString(PREF_EVENT_PERIODIC_MULTIPLE_INTERVAL, String.valueOf(this._multipleInterval));
        editor.putString(PREF_EVENT_PERIODIC_DURATION, String.valueOf(this._duration));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_PERIODIC_ENABLED, false);
        this._multipleInterval = Integer.parseInt(preferences.getString(PREF_EVENT_PERIODIC_MULTIPLE_INTERVAL, "1"));
        this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_PERIODIC_DURATION, "5"));

        this._counter = 0;
        this._startTime = 0;
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_periodic_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_PERIODIC_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_periodic), addPassStatus, DatabaseHandler.ETYPE_PERIODIC, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                if (!ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) {
                    if (!ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile)
                        _value.append("* ").append(context.getString(R.string.array_pref_applicationDisableScanning_disabled)).append("! *").append(StringConstants.TAG_BREAK_HTML);
                    else
                        _value.append(context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile)).append(StringConstants.TAG_BREAK_HTML);
                } else {
                    boolean scanningPaused = ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiply.equals("2") &&
                            GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyTo);

                    if (scanningPaused) {
                        _value.append(context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused)).append(StringConstants.TAG_BREAK_HTML);
                    } else {
                        _value.append(context.getString(R.string.phone_profiles_pref_applicationEventBackgroundScanningScanInterval)).append(StringConstants.STR_COLON_WITH_SPACE)
                                .append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(String.valueOf(ApplicationPreferences.applicationEventPeriodicScanningScanInterval), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                        _value.append(StringConstants.STR_BULLET);
                    }
                }

                _value.append(context.getString(R.string.pref_event_periodic_multiple_interval)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(String.valueOf(this._multipleInterval), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                _value.append(StringConstants.STR_BULLET);
                int resultingInterval = this._multipleInterval * ApplicationPreferences.applicationEventPeriodicScanningScanInterval;
                _value.append(context.getString(R.string.pref_event_periodic_resulting_interval)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(String.valueOf(resultingInterval), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
                _value.append(StringConstants.STR_BULLET);
                _value.append(context.getString(R.string.pref_event_duration)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(this._duration), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_PERIODIC_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_PERIODIC_ENABLED) ||
                key.equals(PREF_EVENT_PERIODIC_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_PERIODIC_APP_SETTINGS);
            if (preference != null) {
                String summary;
                int titleColor;
                if (!ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) {
                    if (!ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile) {
                        summary = "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *"+StringConstants.STR_SEPARATOR_LINE +
                                context.getString(R.string.phone_profiles_pref_eventBackgroundScanningAppSettings_summary);
                        titleColor = ContextCompat.getColor(context, R.color.errorColor);
                    }
                    else {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + StringConstants.STR_SEPARATOR_LINE +
                                context.getString(R.string.phone_profiles_pref_eventBackgroundScanningAppSettings_summary);
                        titleColor = 0;
                    }
                }
                else {
                    boolean scanningPaused = ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiply.equals("2") &&
                            GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyTo);
                    if (scanningPaused) {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused) + StringConstants.STR_SEPARATOR_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventBackgroundScanningAppSettings_summary);
                    } else {
                        summary = context.getString(R.string.array_pref_applicationDisableScanning_enabled) + "."+StringConstants.CHAR_NEW_LINE;
                        summary = summary + context.getString(R.string.phone_profiles_pref_applicationEventBackgroundScanningScanInterval) + StringConstants.STR_COLON_WITH_SPACE +
                                ApplicationPreferences.applicationEventPeriodicScanningScanInterval;
                        summary = summary + StringConstants.STR_SEPARATOR_LINE +
                                context.getString(R.string.phone_profiles_pref_eventBackgroundScanningAppSettings_summary);
                    }
                    titleColor = 0;
                }
                CharSequence sTitle = preference.getTitle();
                int titleLenght = 0;
                if (sTitle != null)
                    titleLenght = sTitle.length();
                Spannable sbt = new SpannableString(sTitle);
                Object[] spansToRemove = sbt.getSpans(0, titleLenght, Object.class);
                for(Object span: spansToRemove){
                    if(span instanceof CharacterStyle)
                        sbt.removeSpan(span);
                }
                if (preferences.getBoolean(PREF_EVENT_PERIODIC_ENABLED, false)) {
                    if (titleColor != 0)
                        sbt.setSpan(new ForegroundColorSpan(titleColor), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                preference.setTitle(sbt);
                preference.setSummary(summary);
            }
        }

        if (key.equals(PREF_EVENT_PERIODIC_MULTIPLE_INTERVAL)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null)
                preference.setSummary(value);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 1;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 1, false, false, false, false);
        }

        if (key.equals(PREF_EVENT_PERIODIC_DURATION)) {
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
        event._eventPreferencesPeriodic.saveSharedPreferences(prefMng.getSharedPreferences());
        Preference preference = prefMng.findPreference(PREF_EVENT_PERIODIC_RESULTING_INTERVAL);
        if (preference != null) {
            int resultingInterval = prefMng.getSharedPreferences().getInt(PREF_EVENT_PERIODIC_RESULTING_INTERVAL, 1)
                                                    * ApplicationPreferences.applicationEventPeriodicScanningScanInterval;
            preference.setSummary(String.valueOf(resultingInterval));
        }

    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_PERIODIC_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
        if (key.equals(PREF_EVENT_PERIODIC_MULTIPLE_INTERVAL) ||
                key.equals(PREF_EVENT_PERIODIC_RESULTING_INTERVAL) ||
                key.equals(PREF_EVENT_PERIODIC_DURATION) ||
                key.equals(PREF_EVENT_PERIODIC_APP_SETTINGS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_PERIODIC_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_PERIODIC_MULTIPLE_INTERVAL, preferences, context);
        setSummary(prefMng, PREF_EVENT_PERIODIC_RESULTING_INTERVAL, preferences, context);
        setSummary(prefMng, PREF_EVENT_PERIODIC_DURATION, preferences, context);
        setSummary(prefMng, PREF_EVENT_PERIODIC_APP_SETTINGS, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_PERIODIC_ENABLED, false, context);
        if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesPeriodic tmp = new EventPreferencesPeriodic(this._event, this._enabled, this._multipleInterval, this._duration);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_PERIODIC_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_PERIODIC_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_PERIODIC).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && tmp.isAllConfigured(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_PERIODIC_CATEGORY);
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
    boolean isAllConfigured(Context context)
    {
        boolean allConfigured = super.isAllConfigured(context);

        allConfigured = allConfigured &&
                (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning ||
                        ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile);

        return allConfigured;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_PERIODIC_ENABLED) != null) {
                setSummary(prefMng, PREF_EVENT_PERIODIC_APP_SETTINGS, preferences, context);
                setSummary(prefMng, PREF_EVENT_PERIODIC_RESULTING_INTERVAL, preferences, context);
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
                intent.setAction(PhoneProfilesService.ACTION_PERIODIC_EVENT_END_BROADCAST_RECEIVER);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_PERIODIC_EVENT_SENSOR_TAG_WORK+"_" + (int) _event._id);
    }

    private void setAlarm(long alarmTime, Context context)
    {
        if (_startTime > 0) {
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_PERIODIC_EVENT_END_BROADCAST_RECEIVER);

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

    void increaseCounter(DataWrapper dataWrapper) {
        if (EventStatic.getGlobalEventsRunning(dataWrapper.context)) {
            int multipleInterval = _multipleInterval;
            if (multipleInterval == 0)
                multipleInterval = 1;
            if (_event.getStatus() == Event.ESTATUS_PAUSE) {
                // len ak nebezi pocitaj counter

                if (_counter < multipleInterval) {
                    _counter += 1;
                    DatabaseHandler.getInstance(dataWrapper.context).updatePeriodicCounter(_event);
                }
                if (_counter >= multipleInterval) {
                    _counter = 0;
                    DatabaseHandler.getInstance(dataWrapper.context).updatePeriodicCounter(_event);

                    // must be used, because of delay 5 seconds
                    PPApplicationStatic.logE("[DELAYED_EXECUTOR_CALL] EventPreferencesPeriodic.increaseCounter", "PPExecutors.handleEvents");
                    PPExecutors.handleEvents(dataWrapper.context,
                            new int[]{EventsHandler.SENSOR_TYPE_PERIODIC},
                            PPExecutors.SENSOR_NAME_SENSOR_TYPE_PERIODIC, 5);
                }
            } else {
                _counter = 0;
            }
        }
        else {
            _counter = 0;
            _startTime = 0;
            DatabaseHandler.getInstance(dataWrapper.context).updatePeriodicCounter(_event);
            DatabaseHandler.getInstance(dataWrapper.context).updatePeriodicStartTime(_event);
        }
    }

    void saveStartTime(DataWrapper dataWrapper) {
        if (this._startTime == 0) {
            // alarm for end is not set

            Calendar calendar = Calendar.getInstance();
            this._startTime = calendar.getTimeInMillis();

            DatabaseHandler.getInstance(dataWrapper.context).updatePeriodicStartTime(_event);

            setSystemEventForPause(dataWrapper.context);
        }
    }

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if (EventStatic.isEventPreferenceAllowed(EventPreferencesPeriodic.PREF_EVENT_PERIODIC_ENABLED, false, eventsHandler.context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                // compute start time

                if (_startTime > 0) {
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    long startTime = _startTime - gmtOffset;

                    // compute end datetime
                    long endAlarmTime = computeAlarm();

                    Calendar now = Calendar.getInstance();
                    long nowAlarmTime = now.getTimeInMillis();

                    if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_PERIODIC))
                        eventsHandler.periodicPassed = true;
                    else {
                        if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_PERIODIC_EVENT_END))
                            eventsHandler.periodicPassed = false;
                        else
                            eventsHandler.periodicPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                    }
                } else
                    eventsHandler.periodicPassed = false;

                if (!eventsHandler.periodicPassed) {
                    _startTime = 0;
                    DatabaseHandler.getInstance(eventsHandler.context).updatePeriodicStartTime(_event);
                }

                if (!eventsHandler.notAllowedPeriodic) {
                    if (eventsHandler.periodicPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedPeriodic = true;

            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_PERIODIC);
            }
        }
    }

}
