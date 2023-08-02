package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Calendar;

class EventPreferencesAlarmClock extends EventPreferences {

    long _startTime;
    boolean _permanentRun;
    int _duration;
    String _applications;

    String _alarmPackageName;

    static final String PREF_EVENT_ALARM_CLOCK_ENABLED = "eventAlarmClockEnabled";
    private static final String PREF_EVENT_ALARM_CLOCK_PERMANENT_RUN = "eventAlarmClockPermanentRun";
    private static final String PREF_EVENT_ALARM_CLOCK_DURATION = "eventAlarmClockDuration";
    private static final String PREF_EVENT_ALARM_CLOCK_APPLICATIONS = "eventAlarmClockApplications";
    private static final String PREF_EVENT_ALARM_CLOCK_SUPPORTED_APPS = "eventAlarmClockSupportedAppsInfo";

    private static final String PREF_EVENT_ALARM_CLOCK_CATEGORY = "eventAlarmClockCategoryRoot";

    EventPreferencesAlarmClock(Event event,
                                    boolean enabled,
                                    boolean permanentRun,
                                    int duration,
                                    String applications)
    {
        super(event, enabled);

        this._permanentRun = permanentRun;
        this._duration = duration;
        this._applications = applications;

        this._startTime = 0;
        this._alarmPackageName = "";
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesAlarmClock._enabled;
        this._permanentRun = fromEvent._eventPreferencesAlarmClock._permanentRun;
        this._duration = fromEvent._eventPreferencesAlarmClock._duration;
        this._applications = fromEvent._eventPreferencesAlarmClock._applications;
        this.setSensorPassed(fromEvent._eventPreferencesAlarmClock.getSensorPassed());

        this._startTime = 0;
        this._alarmPackageName = "";
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_ALARM_CLOCK_ENABLED, _enabled);
        editor.putBoolean(PREF_EVENT_ALARM_CLOCK_PERMANENT_RUN, this._permanentRun);
        editor.putString(PREF_EVENT_ALARM_CLOCK_DURATION, String.valueOf(this._duration));
        editor.putString(PREF_EVENT_ALARM_CLOCK_APPLICATIONS, this._applications);
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_ALARM_CLOCK_ENABLED, false);
        this._permanentRun = preferences.getBoolean(PREF_EVENT_ALARM_CLOCK_PERMANENT_RUN, false);
        this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_ALARM_CLOCK_DURATION, "5"));
        this._applications = preferences.getString(PREF_EVENT_ALARM_CLOCK_APPLICATIONS, "");
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_alarm_clock_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_ALARM_CLOCK_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_alarm_clock), addPassStatus, DatabaseHandler.ETYPE_ALARM_CLOCK, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                if (this._permanentRun)
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.pref_event_permanentRun), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                else
                    _value.append(context.getString(R.string.pref_event_duration)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(this._duration), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);

                String selectedApplications = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                if (!this._applications.isEmpty() && !this._applications.equals("-")) {
                    String[] splits = this._applications.split(StringConstants.STR_SPLIT_REGEX);
                    if (splits.length == 1) {
                        String packageName = Application.getPackageName(splits[0]);
                        String activityName = Application.getActivityName(splits[0]);
                        PackageManager packageManager = context.getPackageManager();
                        if (activityName.isEmpty()) {
                            ApplicationInfo app;
                            try {
                                app = packageManager.getApplicationInfo(packageName, 0);
                                if (app != null)
                                    selectedApplications = packageManager.getApplicationLabel(app).toString();
                            } catch (Exception e) {
                                selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + StringConstants.STR_COLON_WITH_SPACE + splits.length;
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.setClassName(packageName, activityName);
                            ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                            if (info != null)
                                selectedApplications = info.loadLabel(packageManager).toString();
                        }
                    } else
                        selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + StringConstants.STR_COLON_WITH_SPACE + splits.length;
                }

                _value.append(StringConstants.STR_DOT);
                _value.append(context.getString(R.string.event_preferences_alarm_clock_applications)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(selectedApplications, disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_ALARM_CLOCK_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_ALARM_CLOCK_PERMANENT_RUN)) {
            SwitchPreferenceCompat permanentRunPreference = prefMng.findPreference(key);
            if (permanentRunPreference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(permanentRunPreference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
            Preference preference = prefMng.findPreference(PREF_EVENT_ALARM_CLOCK_DURATION);
            if (preference != null) {
                preference.setEnabled(value.equals(StringConstants.FALSE_STRING));
            }
        }
        if (key.equals(PREF_EVENT_ALARM_CLOCK_DURATION)) {
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
        event._eventPreferencesAlarmClock.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesAlarmClock.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_ALARM_CLOCK_ENABLED, false);
        Preference applicationsPreference = prefMng.findPreference(PREF_EVENT_ALARM_CLOCK_APPLICATIONS);
        if (applicationsPreference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_ALARM_CLOCK_APPLICATIONS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(applicationsPreference, enabled, bold, false, false, !isRunnable, false);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_ALARM_CLOCK_ENABLED) ||
            key.equals(PREF_EVENT_ALARM_CLOCK_PERMANENT_RUN)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
        if (key.equals(PREF_EVENT_ALARM_CLOCK_DURATION)||
            key.equals(PREF_EVENT_ALARM_CLOCK_APPLICATIONS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_ALARM_CLOCK_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_ALARM_CLOCK_PERMANENT_RUN, preferences, context);
        setSummary(prefMng, PREF_EVENT_ALARM_CLOCK_DURATION, preferences, context);
        setSummary(prefMng, PREF_EVENT_ALARM_CLOCK_APPLICATIONS, preferences, context);

        InfoDialogPreference preference = prefMng.findPreference(PREF_EVENT_ALARM_CLOCK_SUPPORTED_APPS);
        if (preference != null) {
            String supportedApps = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML +
                                                             "Google Clock"+StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML+"Samsung Clock"+StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML+"Sony Clock"+StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML+"AMdroid"+StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML+"Alarm Clock XTreme free"+StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML+"Alarm Clock XTreme"+StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML+"Alarmy (Sleep if u can)"+StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML+"Early Bird Alarm Clock"+StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML+"Good Morning Alarm Clock"+StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML+"I Can't Wake Up! Alarm Clock"+StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML+"Sleep as Android"+StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML+"Timely"+StringConstants.TAG_LIST_ITEM_END_HTML +
                    StringConstants.TAG_LIST_ITEM_START_HTML+"Alarm Klock" +
                    StringConstants.TAG_LIST_END_LAST_ITEM_HTML
                    ;
            preference.setInfoText(supportedApps);
            preference.setIsHtml(true);
        }
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_ALARM_CLOCK_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesAlarmClock tmp = new EventPreferencesAlarmClock(this._event, this._enabled, this._permanentRun, this._duration, this._applications);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_ALARM_CLOCK_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_ALARM_CLOCK_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_ALARM_CLOCK).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false, false, false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_ALARM_CLOCK_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        StringConstants.STR_COLON_WITH_SPACE+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    boolean isRunnable(Context context)
    {
        //if (android.os.Build.VERSION.SDK_INT >= 21)
            return super.isRunnable(context);
        //else
        //    return false;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_ALARM_CLOCK_ENABLED) != null) {
                setSummary(prefMng, PREF_EVENT_ALARM_CLOCK_ENABLED, preferences, context);
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

        if (!(isRunnable(context) && _enabled))
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
                intent.setAction(PhoneProfilesService.ACTION_ALARM_CLOCK_EVENT_END_BROADCAST_RECEIVER);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_ALARM_CLOCK_SENSOR_TAG_WORK+"_" + (int) _event._id);
    }

    private void setAlarm(long alarmTime, Context context)
    {
        if (!_permanentRun) {
            if (_startTime > 0) {
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_ALARM_CLOCK_EVENT_END_BROADCAST_RECEIVER);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
    }

    void saveStartTime(DataWrapper dataWrapper, long startTime, String alarmPackageName) {
        if (this._startTime == 0) {
            // alarm for end is not set

            this._startTime = startTime; // + (10 * 1000);
            this._alarmPackageName = alarmPackageName;

            DatabaseHandler.getInstance(dataWrapper.context).updateAlarmClockStartTime(_event);

            setSystemEventForPause(dataWrapper.context);
        }
    }

    private boolean isPackageSupported(Context context) {
        if ((this._alarmPackageName == null) || this._alarmPackageName.equals(PPApplication.PACKAGE_NAME))
            return false;

        if (this._alarmPackageName.equals("com.google.android.deskclock") ||
                this._alarmPackageName.equals("com.sec.android.app.clockpackage") ||
                this._alarmPackageName.equals("com.sonyericsson.organizer") ||
                this._alarmPackageName.equals("com.amdroidalarmclock.amdroid") ||
                this._alarmPackageName.equals("com.alarmclock.xtreme") ||
                this._alarmPackageName.equals("com.alarmclock.xtreme.free") ||
                this._alarmPackageName.equals("droom.sleepIfUCan") ||
                this._alarmPackageName.equals("com.funanduseful.earlybirdalarm") ||
                this._alarmPackageName.equals("com.apalon.alarmclock.smart") ||
                this._alarmPackageName.equals("com.kog.alarmclock") ||
                this._alarmPackageName.equals("com.urbandroid.sleep") ||
                this._alarmPackageName.equals("ch.bitspin.timely") ||
                this._alarmPackageName.equals("com.angrydoughnuts.android.alarmclock"))
            return true;

        if ((_applications == null) || _applications.isEmpty() || _applications.equals(context.getString(R.string.dash_string)))
            // applications are not configured
            return false;

        String[] splits = this._applications.split(StringConstants.STR_SPLIT_REGEX);
        for (String split : splits) {
            // get only package name = remove activity
            String packageName = Application.getPackageName(split);
            return this._alarmPackageName.equals(packageName);
        }

        return false;
    }

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if (EventStatic.isEventPreferenceAllowed(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                // compute start time

                if (_startTime > 0) {
                    if (isPackageSupported(eventsHandler.context)) {

                        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                        long startTime = _startTime - gmtOffset;

                        // compute end datetime
                        long endAlarmTime = computeAlarm();
                        Calendar now = Calendar.getInstance();
                        long nowAlarmTime = now.getTimeInMillis();

                        if (eventsHandler.sensorType == EventsHandler.SENSOR_TYPE_ALARM_CLOCK)
                            eventsHandler.alarmClockPassed = true;
                        else if (!_permanentRun) {
                            if (eventsHandler.sensorType == EventsHandler.SENSOR_TYPE_ALARM_CLOCK_EVENT_END)
                                eventsHandler.alarmClockPassed = false;
                            else
                                eventsHandler.alarmClockPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                        } else {
                            eventsHandler.alarmClockPassed = nowAlarmTime >= startTime;
                        }
                    }
                    else
                        eventsHandler.alarmClockPassed = false;
                } else
                    eventsHandler.alarmClockPassed = false;

                if (!eventsHandler.alarmClockPassed) {
                    _startTime = 0;
                    _alarmPackageName = "";
                    DatabaseHandler.getInstance(eventsHandler.context).updateAlarmClockStartTime(_event);
                }

                if (!eventsHandler.notAllowedAlarmClock) {
                    if (eventsHandler.alarmClockPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedAlarmClock = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_ALARM_CLOCK);
            }
        }
    }

}
