package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Calendar;

class EventPreferencesPeriodic extends EventPreferences {

    int _multipleInterval;
    int _duration;

    int _counter;
    long _startTime;


    static final String PREF_EVENT_PERIODIC_ENABLED = "eventPeriodicEnabled";
    private static final String PREF_EVENT_PERIODIC_MULTIPLE_INTERVAL = "eventPeriodicMultipleInterval";
    private static final String PREF_EVENT_PERIODIC_DURATION = "eventPeriodicDuration";
    static final String PREF_EVENT_PERIODIC_APP_SETTINGS = "eventEnableBackgroundScanningAppSettings";
    private static final String PREF_EVENT_PERIODIC_RESULTING_INTERVAL = "eventPeriodicResultingInterval";

    private static final String PREF_EVENT_PERIODIC_CATEGORY = "eventPeriodicCategoryRoot";

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
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_periodic_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_PERIODIC_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_periodic), addPassStatus, DatabaseHandler.ETYPE_PERIODIC, context);
                    descr = descr + "</b> ";
                }

                if (!ApplicationPreferences.applicationEventBackgroundScanningEnableScanning) {
                    //if (!ApplicationPreferences.applicationEventWifiDisabledScannigByProfile)
                        descr = descr + "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *<br>";
                    //else
                    //    descr = descr + context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "<br>";
                } else {
                    descr = descr + context.getString(R.string.phone_profiles_pref_applicationEventBackgroundScanningScanInterval) + ": " +
                            "<b>" + ApplicationPreferences.applicationEventBackgroundScanningScanInterval + "</b>";
                    descr = descr + " • ";
                }

                descr = descr + context.getString(R.string.pref_event_periodic_multiple_interval) + ": <b>" + this._multipleInterval + "</b>";
                descr = descr + " • ";
                int resultingInterval = this._multipleInterval * ApplicationPreferences.applicationEventBackgroundScanningScanInterval;
                descr = descr + context.getString(R.string.pref_event_periodic_resulting_interval) + ": <b>" + resultingInterval + "</b>";
                descr = descr + " • ";
                descr = descr + context.getString(R.string.pref_event_duration) + ": <b>" + GlobalGUIRoutines.getDurationString(this._duration) + "</b>";
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_PERIODIC_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_PERIODIC_ENABLED) ||
                key.equals(PREF_EVENT_PERIODIC_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_PERIODIC_APP_SETTINGS);
            if (preference != null) {
                String summary;
                int titleColor;
                if (!ApplicationPreferences.applicationEventBackgroundScanningEnableScanning) {
                    //if (!ApplicationPreferences.applicationEventWifiDisabledScannigByProfile) {
                        summary = "* " + context.getString(R.string.array_pref_applicationDisableScanning_disabled) + "! *\n\n" +
                                context.getString(R.string.phone_profiles_pref_eventBackgroundScanningAppSettings_summary);
                        titleColor = Color.RED; //0xFFffb000;
                    //}
                    //else {
                    //    summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "\n\n" +
                    //            context.getString(R.string.phone_profiles_pref_eventWifiAppSettings_summary);
                    //    titleColor = 0;
                    //}
                }
                else {
                    summary = context.getString(R.string.array_pref_applicationDisableScanning_enabled) + ".\n";
                    summary = summary  + context.getString(R.string.phone_profiles_pref_applicationEventBackgroundScanningScanInterval) + ": " +
                            ApplicationPreferences.applicationEventBackgroundScanningScanInterval;
                    summary = summary + "\n\n" +
                            context.getString(R.string.phone_profiles_pref_eventBackgroundScanningAppSettings_summary);
                    titleColor = 0;
                }
                CharSequence sTitle = preference.getTitle();
                Spannable sbt = new SpannableString(sTitle);
                Object[] spansToRemove = sbt.getSpans(0, sTitle.length(), Object.class);
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
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 1, false, false, false);
        }

        if (key.equals(PREF_EVENT_PERIODIC_DURATION)) {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 5;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 5, false, false, false);
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesPeriodic.saveSharedPreferences(prefMng.getSharedPreferences());
        Preference preference = prefMng.findPreference(PREF_EVENT_PERIODIC_RESULTING_INTERVAL);
        if (preference != null) {
            int resultingInterval = event._eventPreferencesPeriodic._multipleInterval * ApplicationPreferences.applicationEventBackgroundScanningScanInterval;
            preference.setSummary(String.valueOf(resultingInterval));
        }

    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences,
                    @SuppressWarnings("unused") Context context)
    {
        if (key.equals(PREF_EVENT_PERIODIC_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
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
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_PERIODIC_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesPeriodic tmp = new EventPreferencesPeriodic(this._event, this._enabled, this._multipleInterval, this._duration);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_PERIODIC_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_PERIODIC_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_PERIODIC).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !(tmp.isRunnable(context) && permissionGranted), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_PERIODIC_CATEGORY);
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
        setSummary(prefMng, PREF_EVENT_PERIODIC_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_PERIODIC_RESULTING_INTERVAL, preferences, context);
        setCategorySummary(prefMng, preferences, context);
    }

    private long computeAlarm()
    {
        //PPApplication.logE("EventPreferencesPeriodic.computeAlarm","xxx");

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

        //PPApplication.logE("EventPreferencesPeriodic.setSystemRunningEvent","xxx");

        removeAlarm(context);
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        //PPApplication.logE("[BOOT] EventPreferencesPeriodic.setSystemPauseEvent","xxx");

        removeAlarm(context);

        if (!(isRunnable(context) && _enabled))
            return;

        setAlarm(computeAlarm(), context);
    }

    @Override
    void removeSystemEvent(Context context)
    {
        removeAlarm(context);

        //PPApplication.logE("EventPreferencesPeriodic.removeSystemEvent", "xxx");
    }

    void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_PERIODIC_EVENT_END_BROADCAST_RECEIVER);

                @SuppressLint("UnspecifiedImmutableFlag")
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    //PPApplication.logE("EventPreferencesPeriodic.removeAlarm", "alarm found");

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_PERIODIC_EVENT_SENSOR_TAG_WORK+"_" + (int) _event._id);
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    private void setAlarm(long alarmTime, Context context)
    {
        //PPApplication.logE("[BOOT] EventPreferencesPeriodic.setAlarm","_permanentRun="+_permanentRun);
        //PPApplication.logE("[BOOT] EventPreferencesPeriodic.setAlarm","_startTime="+_startTime);

        if (_startTime > 0) {
            /*if (PPApplication.logEnabled()) {
                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                String result = sdf.format(alarmTime);
                PPApplication.logE("[BOOT] EventPreferencesPeriodic.setAlarm", "endTime=" + result);
            }*/

            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_PERIODIC_EVENT_END_BROADCAST_RECEIVER);

            //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (ApplicationPreferences.applicationUseAlarmClock) {
                    Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    @SuppressLint("UnspecifiedImmutableFlag")
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
                else {
                    //if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    //else //if (android.os.Build.VERSION.SDK_INT >= 19)
                    //    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                }
            }
        }
    }

    void increaseCounter(DataWrapper dataWrapper) {
        if (Event.getGlobalEventsRunning()) {
            if (_counter >= _multipleInterval) {
//                    PPApplication.logE("[EVENTS_HANDLER_CALL] EventPreferencesPeriodic.increaseCounter", "sensorType=SENSOR_TYPE_PERIODIC");
                EventsHandler eventsHandler = new EventsHandler(dataWrapper.context.getApplicationContext());
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PERIODIC);

                //TODO tu zapis do databazy _counter=0, aby sa zacalo dozaciatku pocitanie
                _counter = 0;
            } else {
                //TODO tu len rob inc _countera a zapisuj ho do databazy
                _counter += 1;
            }
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
        /*if (_enabled) {
            //PPApplication.logE("[BOOT] EventPreferencesPeriodic.doHandleEvent", "xxx");
            int oldSensorPassed = getSensorPassed();
            if (Event.isEventPreferenceAllowed(EventPreferencesPeriodic.PREF_EVENT_PERIODIC_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                //PPApplication.logE("[BOOT] EventPreferencesPeriodic.doHandleEvent", "allowed");

                // compute start time

                if (_startTime > 0) {
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    long startTime = _startTime - gmtOffset;

                    //if (PPApplication.logEnabled()) {
                    //    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    //    String alarmTimeS = sdf.format(startTime);
                    //    PPApplication.logE("[BOOT] EventPreferencesPeriodic.doHandleEvent", "startTime=" + alarmTimeS);
                    //}

                    // compute end datetime
                    long endAlarmTime = computeAlarm();
                    //if (PPApplication.logEnabled()) {
                    //    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    //    String alarmTimeS = sdf.format(endAlarmTime);
                    //    PPApplication.logE("[BOOT] EventPreferencesPeriodic.doHandleEvent", "endAlarmTime=" + alarmTimeS);
                    //}

                    Calendar now = Calendar.getInstance();
                    long nowAlarmTime = now.getTimeInMillis();
                    //if (PPApplication.logEnabled()) {
                    //    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    //    String alarmTimeS = sdf.format(nowAlarmTime);
                    //    PPApplication.logE("[BOOT] EventPreferencesPeriodic.doHandleEvent", "nowAlarmTime=" + alarmTimeS);
                    //}

                    if (eventsHandler.sensorType.equals(EventsHandler.SENSOR_TYPE_PERIODIC))
                        eventsHandler.periodicPassed = true;
                    else {
                        if (eventsHandler.sensorType.equals(EventsHandler.SENSOR_TYPE_PERIODIC_EVENT_END))
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

            //PPApplication.logE("[BOOT] EventPreferencesPeriodic.doHandleEvent", "periodicPassed=" + periodicPassed);
            //PPApplication.logE("[BOOT] EventPreferencesPeriodic.doHandleEvent", "notAllowedPeriodic=" + notAllowedPeriodic);

            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                //PPApplication.logE("[TEST BATTERY] EventPreferencesPeriodic.doHandleEvent", "device boot - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_PERIODIC);
            }
        }*/
    }

}
