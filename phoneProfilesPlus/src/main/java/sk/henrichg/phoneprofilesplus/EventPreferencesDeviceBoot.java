package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.crashlytics.android.Crashlytics;

import java.util.Calendar;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.WorkManager;

class EventPreferencesDeviceBoot extends EventPreferences {

    long _startTime;
    boolean _permanentRun;
    int _duration;

    static final String PREF_EVENT_DEVICE_BOOT_ENABLED = "eventDeviceBootEnabled";
    private static final String PREF_EVENT_DEVICE_BOOT_PERMANENT_RUN = "eventDeviceBootPermanentRun";
    private static final String PREF_EVENT_DEVICE_BOOT_DURATION = "eventDeviceBootDuration";

    private static final String PREF_EVENT_DEVICE_BOOT_CATEGORY = "eventDeviceBootCategoryRoot";

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

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_device_boot_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_DEVICE_BOOT_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_device_boot), addPassStatus, DatabaseHandler.ETYPE_DEVICE_BOOT, context);
                    descr = descr + "</b> ";
                }

                if (this._permanentRun)
                    descr = descr + "<b>" + context.getString(R.string.pref_event_permanentRun) + "</b>";
                else
                    descr = descr + context.getString(R.string.pref_event_duration) + ": <b>" + GlobalGUIRoutines.getDurationString(this._duration) + "</b>";
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value/*, Context context*/)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_DEVICE_BOOT_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_DEVICE_BOOT_PERMANENT_RUN)) {
            SwitchPreferenceCompat permanentRunPreference = prefMng.findPreference(key);
            if (permanentRunPreference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(permanentRunPreference, true, preferences.getBoolean(key, false), false, false, false);
            }
            Preference preference = prefMng.findPreference(PREF_EVENT_DEVICE_BOOT_DURATION);
            if (preference != null) {
                preference.setEnabled(value.equals("false"));
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
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 5, false, false, false);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences,
                    @SuppressWarnings("unused") Context context)
    {
        if (key.equals(PREF_EVENT_DEVICE_BOOT_ENABLED) ||
            key.equals(PREF_EVENT_DEVICE_BOOT_PERMANENT_RUN)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false"/*, context*/);
        }
        if (key.equals(PREF_EVENT_DEVICE_BOOT_DURATION))
        {
            setSummary(prefMng, key, preferences.getString(key, "")/*, context*/);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_DEVICE_BOOT_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_DEVICE_BOOT_PERMANENT_RUN, preferences, context);
        setSummary(prefMng, PREF_EVENT_DEVICE_BOOT_DURATION, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_DEVICE_BOOT_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesDeviceBoot tmp = new EventPreferencesDeviceBoot(this._event, this._enabled, this._permanentRun, this._duration);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_DEVICE_BOOT_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_DEVICE_BOOT_ENABLED, false);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_DEVICE_BOOT_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context)
    {
        //if (android.os.Build.VERSION.SDK_INT >= 21)
            return super.isRunnable(context);
        //else
        //    return false;
    }

    private long computeAlarm()
    {
        //PPApplication.logE("EventPreferencesDeviceBoot.computeAlarm","xxx");

        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        calEndTime.setTimeInMillis((_startTime - gmtOffset) + (_duration * 1000));
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

        //PPApplication.logE("EventPreferencesDeviceBoot.setSystemRunningEvent","xxx");

        removeAlarm(context);
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        //PPApplication.logE("[BOOT] EventPreferencesDeviceBoot.setSystemPauseEvent","xxx");

        removeAlarm(context);

        if (!(isRunnable(context) && _enabled))
            return;

        setAlarm(computeAlarm(), context);
    }

    @Override
    void removeSystemEvent(Context context)
    {
        removeAlarm(context);

        //PPApplication.logE("EventPreferencesDeviceBoot.removeSystemEvent", "xxx");
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
                    //PPApplication.logE("EventPreferencesDeviceBoot.removeAlarm", "alarm found");

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance(context);
            //workManager.cancelUniqueWork("elapsedAlarmsDeviceBootSensorWork_"+(int)_event._id);
            workManager.cancelAllWorkByTag("elapsedAlarmsDeviceBootSensorWork_"+(int)_event._id);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    private void setAlarm(long alarmTime, Context context)
    {
        //PPApplication.logE("[BOOT] EventPreferencesDeviceBoot.setAlarm","_permanentRun="+_permanentRun);
        //PPApplication.logE("[BOOT] EventPreferencesDeviceBoot.setAlarm","_startTime="+_startTime);

        if (!_permanentRun) {
            if (_startTime > 0) {
                /*if (PPApplication.logEnabled()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String result = sdf.format(alarmTime);
                    PPApplication.logE("[BOOT] EventPreferencesDeviceBoot.setAlarm", "endTime=" + result);
                }*/

                /*if (ApplicationPreferences.applicationUseAlarmClock(context)) {
                    Intent intent = new Intent();
                    intent.setAction(PhoneProfilesService.ACTION_DEVICE_BOOT_EVENT_END_BROADCAST_RECEIVER);

                    //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null) {
                        Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                }
                else {
                    Calendar now = Calendar.getInstance();
                    long elapsedTime = (alarmTime + Event.EVENT_ALARM_TIME_OFFSET) - now.getTimeInMillis();

                    if (PPApplication.logEnabled()) {
                        long allSeconds = elapsedTime / 1000;
                        long hours = allSeconds / 60 / 60;
                        long minutes = (allSeconds - (hours * 60 * 60)) / 60;
                        long seconds = allSeconds % 60;

                        PPApplication.logE("EventPreferencesDeviceBoot.setAlarm", "elapsedTime=" + hours + ":" + minutes + ":" + seconds);
                    }

                    Data workData = new Data.Builder()
                            .putString(PhoneProfilesService.EXTRA_ELAPSED_ALARMS_WORK, ElapsedAlarmsWorker.ELAPSED_ALARMS_DEVICE_BOOT_EVENT_END_SENSOR)
                            .build();

                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(ElapsedAlarmsWorker.class)
                                    .addTag("elapsedAlarmsDeviceBootSensorWork_"+(int)_event._id)
                                    .setInputData(workData)
                                    .setInitialDelay(elapsedTime, TimeUnit.MILLISECONDS)
                                    .build();
                    try {
                        WorkManager workManager = WorkManager.getInstance(context);
                        PPApplication.logE("[HANDLER] EventPreferencesDeviceBoot.setAlarm", "enqueueUniqueWork - elapsedTime="+elapsedTime);
                        //workManager.enqueueUniqueWork("elapsedAlarmsDeviceBootSensorWork_"+(int)_event._id, ExistingWorkPolicy.REPLACE, worker);
                        workManager.enqueue(worker);
                    } catch (Exception ignored) {}
                }*/

                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_DEVICE_BOOT_EVENT_END_BROADCAST_RECEIVER);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                    else {
                        if (android.os.Build.VERSION.SDK_INT >= 23)
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                        else //if (android.os.Build.VERSION.SDK_INT >= 19)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                        //else
                        //    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
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
            //PPApplication.logE("[BOOT] EventPreferencesDeviceBoot.doHandleEvent", "xxx");
            int oldSensorPassed = getSensorPassed();
            if (Event.isEventPreferenceAllowed(EventPreferencesDeviceBoot.PREF_EVENT_DEVICE_BOOT_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                //PPApplication.logE("[BOOT] EventPreferencesDeviceBoot.doHandleEvent", "allowed");

                // compute start time

                if (_startTime > 0) {
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    long startTime = _startTime - gmtOffset;

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(startTime);
                        PPApplication.logE("[BOOT] EventPreferencesDeviceBoot.doHandleEvent", "startTime=" + alarmTimeS);
                    }*/

                    // compute end datetime
                    long endAlarmTime = computeAlarm();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(endAlarmTime);
                        PPApplication.logE("[BOOT] EventPreferencesDeviceBoot.doHandleEvent", "endAlarmTime=" + alarmTimeS);
                    }*/

                    Calendar now = Calendar.getInstance();
                    long nowAlarmTime = now.getTimeInMillis();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(nowAlarmTime);
                        PPApplication.logE("[BOOT] EventPreferencesDeviceBoot.doHandleEvent", "nowAlarmTime=" + alarmTimeS);
                    }*/

                    if (eventsHandler.sensorType.equals(EventsHandler.SENSOR_TYPE_DEVICE_BOOT))
                        eventsHandler.deviceBootPassed = true;
                    else if (!_permanentRun) {
                        if (eventsHandler.sensorType.equals(EventsHandler.SENSOR_TYPE_DEVICE_BOOT_EVENT_END))
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

            //PPApplication.logE("[BOOT] EventPreferencesDeviceBoot.doHandleEvent", "deviceBootPassed=" + deviceBootPassed);
            //PPApplication.logE("[BOOT] EventPreferencesDeviceBoot.doHandleEvent", "notAllowedDeviceBoot=" + notAllowedDeviceBoot);

            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                //PPApplication.logE("[TEST BATTERY] EventPreferencesDeviceBoot.doHandleEvent", "device boot - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_DEVICE_BOOT);
            }
        }
    }

}
