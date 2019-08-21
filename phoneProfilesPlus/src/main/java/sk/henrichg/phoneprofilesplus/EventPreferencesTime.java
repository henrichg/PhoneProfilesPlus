package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
//import android.preference.CheckBoxPreference;
//import android.preference.Preference;
//import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import java.sql.Date;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesTime extends EventPreferences {

    boolean _sunday;
    boolean _monday;
    boolean _tuesday;
    boolean _wednesday;
    boolean _thursday;
    boolean _friday;
    boolean _saturday;
    int _startTime;
    int _endTime;
    int _timeType;
    //boolean _useEndTime;

    static final String PREF_EVENT_TIME_ENABLED = "eventTimeEnabled";
    private static final String PREF_EVENT_TIME_DAYS = "eventTimeDays";
    private static final String PREF_EVENT_TIME_START_TIME = "eventTimeStartTime";
    private static final String PREF_EVENT_TIME_END_TIME = "eventTimeEndTime";
    private static final String PREF_EVENT_TIME_TYPE = "eventTimeType";
    //private static final String PREF_EVENT_TIME_USE_END_TIME = "eventTimeUseEndTime";
    static final String PREF_EVENT_TIME_LOCATION_SYSTEM_SETTINGS = "eventTimeLocationSystemSettings";

    private static final String PREF_EVENT_TIME_CATEGORY = "eventTimeCategoryRoot";

    static final int TIME_TYPE_EXACT = 0;
    private static final int TIME_TYPE_SUNRISE_SUNSET = 1;
    private static final int TIME_TYPE_SUNSET_SUNRISE = 2;

    EventPreferencesTime(Event event,
                                boolean enabled,
                                boolean sunday,
                                boolean monday,
                                boolean tuesday,
                                boolean wednesday,
                                boolean thursday,
                                boolean friday,
                                boolean saturday,
                                int startTime,
                                int endTime,
                                int timeType//,
                                //boolean useEndTime
                                )
    {
        super(event, enabled);

        this._sunday = sunday;
        this._monday = monday;
        this._tuesday = tuesday;
        this._wednesday = wednesday;
        this._thursday = thursday;
        this._friday = friday;
        this._saturday = saturday;
        this._startTime = startTime;
        this._endTime = endTime;
        this._timeType = timeType;
        //this._useEndTime = useEndTime;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesTime._enabled;
        this._sunday = fromEvent._eventPreferencesTime._sunday;
        this._monday = fromEvent._eventPreferencesTime._monday;
        this._tuesday = fromEvent._eventPreferencesTime._tuesday;
        this._wednesday = fromEvent._eventPreferencesTime._wednesday;
        this._thursday = fromEvent._eventPreferencesTime._thursday;
        this._friday = fromEvent._eventPreferencesTime._friday;
        this._saturday = fromEvent._eventPreferencesTime._saturday;
        this._startTime = fromEvent._eventPreferencesTime._startTime;
        this._endTime = fromEvent._eventPreferencesTime._endTime;
        this._timeType = fromEvent._eventPreferencesTime._timeType;
        //this._useEndTime = fromEvent._eventPreferencesTime._useEndTime;
        this.setSensorPassed(fromEvent._eventPreferencesTime.getSensorPassed());
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_TIME_ENABLED, _enabled);
        String sValue = "";
        if (this._sunday) sValue = sValue + "0|";
        if (this._monday) sValue = sValue + "1|";
        if (this._tuesday) sValue = sValue + "2|";
        if (this._wednesday) sValue = sValue + "3|";
        if (this._thursday) sValue = sValue + "4|";
        if (this._friday) sValue = sValue + "5|";
        if (this._saturday) sValue = sValue + "6|";

        editor.putString(PREF_EVENT_TIME_DAYS, sValue);
        editor.putInt(PREF_EVENT_TIME_START_TIME, this._startTime);
        editor.putInt(PREF_EVENT_TIME_END_TIME, this._endTime);
        editor.putString(PREF_EVENT_TIME_TYPE, String.valueOf(this._timeType));
        //editor.putBoolean(PREF_EVENT_TIME_USE_END_TIME, this._useEndTime);
        editor.apply();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_TIME_ENABLED, false);

        String sDays = preferences.getString(PREF_EVENT_TIME_DAYS, DaysOfWeekPreferenceX.allValue);
        String[] splits = sDays.split("\\|");
        if (splits[0].equals(DaysOfWeekPreferenceX.allValue))
        {
            this._sunday = true;
            this._monday = true;
            this._tuesday = true;
            this._wednesday = true;
            this._thursday = true;
            this._friday = true;
            this._saturday = true;
        }
        else
        {
            this._sunday = false;
            this._monday = false;
            this._tuesday = false;
            this._wednesday = false;
            this._thursday = false;
            this._friday = false;
            this._saturday = false;
            for (String value : splits)
            {
                this._sunday = this._sunday || value.equals("0");
                this._monday = this._monday || value.equals("1");
                this._tuesday = this._tuesday || value.equals("2");
                this._wednesday = this._wednesday || value.equals("3");
                this._thursday = this._thursday || value.equals("4");
                this._friday = this._friday || value.equals("5");
                this._saturday = this._saturday || value.equals("6");
            }
        }

        Calendar now = Calendar.getInstance();
        int defaultValue = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        this._startTime = preferences.getInt(PREF_EVENT_TIME_START_TIME, defaultValue);
        this._endTime = preferences.getInt(PREF_EVENT_TIME_END_TIME, defaultValue);
        this._timeType = Integer.parseInt(preferences.getString(PREF_EVENT_TIME_TYPE, "0"));
        //this._useEndTime = preferences.getBoolean(PREF_EVENT_TIME_USE_END_TIME, false);
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_time_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_TIME_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_time), addPassStatus, DatabaseHandler.ETYPE_TIME, context);
                    descr = descr + ": </b>";
                }

                if (_timeType != TIME_TYPE_EXACT) {
                    if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                        descr = descr + "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *<br>";
                    }
                }

                boolean[] daySet = new boolean[7];
                daySet[0] = this._sunday;
                daySet[1] = this._monday;
                daySet[2] = this._tuesday;
                daySet[3] = this._wednesday;
                daySet[4] = this._thursday;
                daySet[5] = this._friday;
                daySet[6] = this._saturday;

                boolean allDays = true;
                for (int i = 0; i < 7; i++)
                    allDays = allDays && daySet[i];

                if (allDays) {
                    descr = descr + context.getString(R.string.array_pref_event_all);
                    descr = descr + " ";
                } else {
                    String[] namesOfDay = DateFormatSymbols.getInstance().getShortWeekdays();

                    int dayOfWeek;
                    for (int i = 0; i < 7; i++) {
                        dayOfWeek = getDayOfWeekByLocale(i);

                        if (daySet[dayOfWeek])
                            //noinspection StringConcatenationInLoop
                            descr = descr + namesOfDay[dayOfWeek + 1] + " ";
                    }
                }

                descr = descr + "• ";
                switch (_timeType) {
                    case TIME_TYPE_EXACT:
                        descr = descr + context.getString(R.string.event_preference_sensor_time_type_exact);
                        break;
                    case TIME_TYPE_SUNRISE_SUNSET:
                        descr = descr + context.getString(R.string.event_preference_sensor_time_type_sunrise_sunset);
                        break;
                    case TIME_TYPE_SUNSET_SUNRISE:
                        descr = descr + context.getString(R.string.event_preference_sensor_time_type_sunset_sunrise);
                        break;
                }

                if (_timeType == TIME_TYPE_EXACT) {
                    descr = descr + " • ";

                    Calendar calendar = Calendar.getInstance();

                    calendar.set(Calendar.HOUR_OF_DAY, _startTime / 60);
                    calendar.set(Calendar.MINUTE, _startTime % 60);
                    descr = descr + DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));
                    //if (tmp._useEndTime)
                    //{
                    calendar.set(Calendar.HOUR_OF_DAY, _endTime / 60);
                    calendar.set(Calendar.MINUTE, _endTime % 60);
                    descr = descr + "-";
                    descr = descr + DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));
                    //}

                    if (addBullet) {
                        if (Event.getGlobalEventsRunning(context)) {
                            long alarmTime;
                            //SimpleDateFormat sdf = new SimpleDateFormat("EEd/MM/yy HH:mm");
                            String alarmTimeS;
                            if (_event.getStatus() == Event.ESTATUS_PAUSE) {
                                alarmTime = computeAlarm(true, context);
                                // date and time format by user system settings configuration
                                alarmTimeS = "(st) " + DateFormat.getDateFormat(context).format(alarmTime) +
                                        " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                descr = descr + "<br>"; //'\n';
                                descr = descr + "&nbsp;&nbsp;&nbsp;-> " + alarmTimeS;
                            } else if ((_event.getStatus() == Event.ESTATUS_RUNNING)/* && _useEndTime*/) {
                                alarmTime = computeAlarm(false, context);
                                // date and time format by user system settings configuration
                                alarmTimeS = "(et) " + DateFormat.getDateFormat(context).format(alarmTime) +
                                        " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                descr = descr + "<br>"; //'\n';
                                descr = descr + "&nbsp;&nbsp;&nbsp;-> " + alarmTimeS;
                            }
                        }
                    }
                }
                else {
                    if (PhoneProfilesService.getInstance() != null) {
                        TwilightScanner twilightScanner = PhoneProfilesService.getInstance().getTwilightScanner();
                        if (twilightScanner != null) {
                            TwilightState twilightState = twilightScanner.getTwilightState();
                            if (twilightState != null) {
                                long startTime = computeAlarm(true, context);
                                long endTime = computeAlarm(false, context);
                                if ((startTime != 0) && (endTime != 0)) {
                                    descr = descr + " • ";

                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeInMillis(startTime);
                                    descr = descr + DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));
                                    calendar.setTimeInMillis(endTime);
                                    descr = descr + "-";
                                    descr = descr + DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));

                                    if (addBullet) {
                                        if (Event.getGlobalEventsRunning(context)) {
                                            long alarmTime;
                                            //SimpleDateFormat sdf = new SimpleDateFormat("EEd/MM/yy HH:mm");
                                            String alarmTimeS;
                                            if (_event.getStatus() == Event.ESTATUS_PAUSE) {
                                                alarmTime = computeAlarm(true, context);
                                                // date and time format by user system settings configuration
                                                alarmTimeS = "(st) " + DateFormat.getDateFormat(context).format(alarmTime) +
                                                        " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                                descr = descr + "<br>"; //'\n';
                                                descr = descr + "&nbsp;&nbsp;&nbsp;-> " + alarmTimeS;
                                            } else if ((_event.getStatus() == Event.ESTATUS_RUNNING)/* && _useEndTime*/) {
                                                alarmTime = computeAlarm(false, context);
                                                // date and time format by user system settings configuration
                                                alarmTimeS = "(et) " + DateFormat.getDateFormat(context).format(alarmTime) +
                                                        " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                                descr = descr + "<br>"; //'\n';
                                                descr = descr + "&nbsp;&nbsp;&nbsp;-> " + alarmTimeS;
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }

        return descr;
    }

    // dayOfWeek: value are (for example) Calendar.SUNDAY-1
    // return: value are (for example) Calendar.MONDAY-1
    static int getDayOfWeekByLocale(int dayOfWeek)
    {

        Calendar cal = Calendar.getInstance();
        int firstDayOfWeek = cal.getFirstDayOfWeek();

        int resDayOfWeek = dayOfWeek + (firstDayOfWeek-1);
        if (resDayOfWeek > 6)
            resDayOfWeek = resDayOfWeek - 7;

        return resDayOfWeek;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_TIME_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), true, false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_TIME_TYPE)) {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }

            boolean enable = Integer.parseInt(value) == TIME_TYPE_EXACT;
            Preference preference = prefMng.findPreference(PREF_EVENT_TIME_START_TIME);
            if (preference != null)
                preference.setEnabled(enable);
            preference = prefMng.findPreference(PREF_EVENT_TIME_END_TIME);
            if (preference != null)
                preference.setEnabled(enable);
            preference = prefMng.findPreference(PREF_EVENT_TIME_LOCATION_SYSTEM_SETTINGS);
            if (preference != null)
                preference.setEnabled(!enable);
        }
        if (key.equals(PREF_EVENT_TIME_LOCATION_SYSTEM_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary = context.getString(R.string.event_preference_sensor_time_locationSystemSettings_summary);
                if (!PhoneProfilesService.isLocationEnabled(context.getApplicationContext())) {
                    summary = "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *\n\n"+
                            summary;
                }
                else {
                    summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n"+
                            summary;
                }
                preference.setSummary(summary);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesTime.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesTime.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_TIME_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_TIME_DAYS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_TIME_DAYS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, true, !isRunnable, false);
        }
        /*preference = prefMng.findPreference(PREF_EVENT_TIME_TYPE);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_TIME_TYPE, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, true, !isRunnable, false);
        }*/
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_TIME_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
        if (key.equals(PREF_EVENT_TIME_DAYS) ||
                key.equals(PREF_EVENT_TIME_TYPE) ||
                key.equals(PREF_EVENT_TIME_LOCATION_SYSTEM_SETTINGS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_TIME_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_TIME_DAYS, preferences, context);
        setSummary(prefMng, PREF_EVENT_TIME_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_TIME_LOCATION_SYSTEM_SETTINGS, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_TIME_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesTime tmp = new EventPreferencesTime(this._event, this._enabled, this._sunday, this._monday, this._tuesday, this._wednesday,
                    this._thursday, this._friday, this._saturday, this._startTime, this._endTime, this._timeType);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_TIME_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_TIME_ENABLED, false);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, true, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_TIME_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    public boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

        boolean dayOfWeek = this._sunday;
        dayOfWeek = dayOfWeek || this._monday;
        dayOfWeek = dayOfWeek || this._tuesday;
        dayOfWeek = dayOfWeek || this._wednesday;
        dayOfWeek = dayOfWeek || this._thursday;
        dayOfWeek = dayOfWeek || this._friday;
        dayOfWeek = dayOfWeek || this._saturday;
        runnable = runnable && dayOfWeek;

        return runnable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        setSummary(prefMng, PREF_EVENT_TIME_LOCATION_SYSTEM_SETTINGS, preferences, context);
        setCategorySummary(prefMng, preferences, context);
    }

    long computeAlarm(boolean startEvent, Context context)
    {
        boolean testEvent = (_event._name != null) && _event._name.equals("TEST TIME SENSOR");
        if (testEvent) {
            PPApplication.logE("EventPreferencesTime.computeAlarm", "eventName=" + _event._name);
            PPApplication.logE("EventPreferencesTime.computeAlarm", "startEvent=" + startEvent);
        }

        Calendar now = Calendar.getInstance();

        Calendar calStartTime = Calendar.getInstance();
        Calendar calEndTime = Calendar.getInstance();

        boolean setAlarm = false;

        if (_timeType == TIME_TYPE_EXACT) {
            setAlarm = true;

            ///// set calendar for startTime and endTime
            Calendar hoursStartTime = Calendar.getInstance();
            hoursStartTime.set(Calendar.HOUR_OF_DAY, _startTime / 60);
            hoursStartTime.set(Calendar.MINUTE, _startTime % 60);
            hoursStartTime.set(Calendar.DAY_OF_MONTH, 0);
            hoursStartTime.set(Calendar.MONTH, 0);
            hoursStartTime.set(Calendar.YEAR, 0);
            hoursStartTime.set(Calendar.SECOND, 0);
            hoursStartTime.set(Calendar.MILLISECOND, 0);

            Calendar hoursEndTime = Calendar.getInstance();
            hoursEndTime.set(Calendar.HOUR_OF_DAY, _endTime / 60);
            hoursEndTime.set(Calendar.MINUTE, _endTime % 60);
            hoursEndTime.set(Calendar.DAY_OF_MONTH, 0);
            hoursEndTime.set(Calendar.MONTH, 0);
            hoursEndTime.set(Calendar.YEAR, 0);
            hoursEndTime.set(Calendar.SECOND, 0);
            hoursEndTime.set(Calendar.MILLISECOND, 0);

            Calendar hoursNowTime = Calendar.getInstance();
            hoursNowTime.set(Calendar.DAY_OF_MONTH, 0);
            hoursNowTime.set(Calendar.MONTH, 0);
            hoursNowTime.set(Calendar.YEAR, 0);
            hoursNowTime.set(Calendar.SECOND, 0);
            hoursNowTime.set(Calendar.MILLISECOND, 0);

            Calendar midnightTime = Calendar.getInstance();
            midnightTime.set(Calendar.HOUR_OF_DAY, 0);
            midnightTime.set(Calendar.MINUTE, 0);
            midnightTime.set(Calendar.DAY_OF_MONTH, 0);
            midnightTime.set(Calendar.MONTH, 0);
            midnightTime.set(Calendar.YEAR, 0);
            midnightTime.set(Calendar.SECOND, 0);
            midnightTime.set(Calendar.MILLISECOND, 0);

            calStartTime.set(Calendar.HOUR_OF_DAY, _startTime / 60);
            calStartTime.set(Calendar.MINUTE, _startTime % 60);
            calStartTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
            calStartTime.set(Calendar.MONTH, now.get(Calendar.MONTH));
            calStartTime.set(Calendar.YEAR, now.get(Calendar.YEAR));
            calStartTime.set(Calendar.SECOND, 0);
            calStartTime.set(Calendar.MILLISECOND, 0);

            calEndTime.set(Calendar.HOUR_OF_DAY, _endTime / 60);
            calEndTime.set(Calendar.MINUTE, _endTime % 60);
            calEndTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
            calEndTime.set(Calendar.MONTH, now.get(Calendar.MONTH));
            calEndTime.set(Calendar.YEAR, now.get(Calendar.YEAR));
            calEndTime.set(Calendar.SECOND, 0);
            calEndTime.set(Calendar.MILLISECOND, 0);

            if (hoursStartTime.getTimeInMillis() >= hoursEndTime.getTimeInMillis())
            {
                // endTime is over midnight
                if (testEvent)
                    PPApplication.logE("EventPreferencesTime.computeAlarm","startTime >= endTime");

                if ((hoursNowTime.getTimeInMillis() >= midnightTime.getTimeInMillis()) &&
                    (hoursNowTime.getTimeInMillis() <= hoursEndTime.getTimeInMillis())) {
                    // now is between midnight and endTime
                    if (testEvent)
                        PPApplication.logE("EventPreferencesTime.computeAlarm","now is between midnight and endTime");

                    calStartTime.add(Calendar.DAY_OF_YEAR, -1);
                }
                else
                if ((hoursNowTime.getTimeInMillis() >= hoursStartTime.getTimeInMillis()) &&
                    (hoursNowTime.getTimeInMillis() < midnightTime.getTimeInMillis())) {
                    // now is between startTime and midnight
                    if (testEvent)
                        PPApplication.logE("EventPreferencesTime.computeAlarm","now is between startTime and midnight");

                    calEndTime.add(Calendar.DAY_OF_YEAR, 1);
                }
                else {
                    // now is before start time
                    if (testEvent)
                        PPApplication.logE("EventPreferencesTime.computeAlarm","now is before start time");

                    calEndTime.add(Calendar.DAY_OF_YEAR, 1);
                }
            }
            else {
                if (testEvent)
                    PPApplication.logE("EventPreferencesTime.computeAlarm","startTime < endTime");

                if (hoursNowTime.getTimeInMillis() > hoursEndTime.getTimeInMillis()) {
                    // endTime is before actual time, compute for tomorrow
                    if (testEvent)
                        PPApplication.logE("EventPreferencesTime.computeAlarm", "nowTime > endTime");

                    calStartTime.add(Calendar.DAY_OF_YEAR, 1);
                    calEndTime.add(Calendar.DAY_OF_YEAR, 1);
                }
            }

            //// update calendar for startTime a endTime by selected day of week
            boolean[] daysOfWeek = new boolean[8];
            daysOfWeek[Calendar.SUNDAY] = this._sunday;
            daysOfWeek[Calendar.MONDAY] = this._monday;
            daysOfWeek[Calendar.TUESDAY] = this._tuesday;
            daysOfWeek[Calendar.WEDNESDAY] = this._wednesday;
            daysOfWeek[Calendar.THURSDAY] = this._thursday;
            daysOfWeek[Calendar.FRIDAY] = this._friday;
            daysOfWeek[Calendar.SATURDAY] = this._saturday;

            int startDayOfWeek = calStartTime.get(Calendar.DAY_OF_WEEK);
            if (daysOfWeek[startDayOfWeek]) {
                // startTime of week is selected
                if (testEvent)
                    PPApplication.logE("EventPreferencesTime.computeAlarm", "startTime of week is selected");
            } else {
                // startTime of week is not selected,
                if (testEvent) {
                    PPApplication.logE("EventPreferencesTime.computeAlarm", "startTime of week is NOT selected");
                    PPApplication.logE("EventPreferencesTime.computeAlarm", "startDayOfWeek=" + startDayOfWeek);
                }

                // search for selected day of week
                boolean found = false;
                int daysToAdd = 0;
                for (int i = startDayOfWeek; i < 8; i++) {
                    if (daysOfWeek[i]) {
                        found = true;
                        break;
                    }
                    ++daysToAdd;
                }
                if (!found) {
                    for (int i = 1; i < startDayOfWeek; i++) {
                        if (daysOfWeek[i]) {
                            found = true;
                            break;
                        }
                        ++daysToAdd;
                    }
                }
                if (found) {
                    if (testEvent)
                        PPApplication.logE("EventPreferencesTime.computeAlarm", "daysToAdd=" + daysToAdd);
                    calStartTime.add(Calendar.DAY_OF_YEAR, daysToAdd);
                    calEndTime.add(Calendar.DAY_OF_YEAR, daysToAdd);
                }
            }
            //////////////////////

            if (testEvent) {
                PPApplication.logE("EventPreferencesTime.computeAlarm", "callStartTime=" + DateFormat.getDateFormat(context).format(calStartTime.getTimeInMillis()) +
                        " " + DateFormat.getTimeFormat(context).format(calStartTime.getTimeInMillis()));
                PPApplication.logE("EventPreferencesTime.computeAlarm", "callEndTime=" + DateFormat.getDateFormat(context).format(calEndTime.getTimeInMillis()) +
                        " " + DateFormat.getTimeFormat(context).format(calEndTime.getTimeInMillis()));
            }
            ////////////////////////////
        }
        else {
            if (PhoneProfilesService.getInstance() != null) {
                if (testEvent)
                    PPApplication.logE("EventPreferencesTime.computeAlarm", "PPService started");
                TwilightScanner twilightScanner = PhoneProfilesService.getInstance().getTwilightScanner();
                if (twilightScanner != null) {
                    if (testEvent)
                        PPApplication.logE("EventPreferencesTime.computeAlarm", "TwilightScanner started");
                    TwilightState twilightState = twilightScanner.getTwilightState();
                    if (twilightState != null) {
                        if (testEvent)
                            PPApplication.logE("EventPreferencesTime.computeAlarm", "TwilightState set");
                        setAlarm = true;

                        if ((twilightState.getTodaySunset() != -1) && (twilightState.getTodaySunrise() != -1)) {
                            Calendar hoursStartTime = Calendar.getInstance();
                            if (_timeType == TIME_TYPE_SUNRISE_SUNSET) {
                                calStartTime.setTimeInMillis(twilightState.getTodaySunrise());
                                hoursStartTime.setTimeInMillis(twilightState.getTodaySunrise());
                            }
                            else {
                                calStartTime.setTimeInMillis(twilightState.getTodaySunset());
                                hoursStartTime.setTimeInMillis(twilightState.getTodaySunset());
                            }
                            hoursStartTime.set(Calendar.DAY_OF_MONTH, 0);
                            hoursStartTime.set(Calendar.MONTH, 0);
                            hoursStartTime.set(Calendar.YEAR, 0);
                            hoursStartTime.set(Calendar.SECOND, 0);
                            hoursStartTime.set(Calendar.MILLISECOND, 0);

                            Calendar hoursEndTime = Calendar.getInstance();
                            if (_timeType == TIME_TYPE_SUNRISE_SUNSET) {
                                calEndTime.setTimeInMillis(twilightState.getTodaySunset());
                                hoursEndTime.setTimeInMillis(twilightState.getTodaySunset());
                            }
                            else {
                                calEndTime.setTimeInMillis(twilightState.getTodaySunrise());
                                hoursEndTime.setTimeInMillis(twilightState.getTodaySunrise());
                            }
                            hoursEndTime.set(Calendar.DAY_OF_MONTH, 0);
                            hoursEndTime.set(Calendar.MONTH, 0);
                            hoursEndTime.set(Calendar.YEAR, 0);
                            hoursEndTime.set(Calendar.SECOND, 0);
                            hoursEndTime.set(Calendar.MILLISECOND, 0);

                            Calendar hoursNowTime = Calendar.getInstance();
                            hoursNowTime.set(Calendar.DAY_OF_MONTH, 0);
                            hoursNowTime.set(Calendar.MONTH, 0);
                            hoursNowTime.set(Calendar.YEAR, 0);
                            hoursNowTime.set(Calendar.SECOND, 0);
                            hoursNowTime.set(Calendar.MILLISECOND, 0);

                            Calendar midnightTime = Calendar.getInstance();
                            midnightTime.set(Calendar.HOUR_OF_DAY, 0);
                            midnightTime.set(Calendar.MINUTE, 0);
                            midnightTime.set(Calendar.DAY_OF_MONTH, 0);
                            midnightTime.set(Calendar.MONTH, 0);
                            midnightTime.set(Calendar.YEAR, 0);
                            midnightTime.set(Calendar.SECOND, 0);
                            midnightTime.set(Calendar.MILLISECOND, 0);

                            int moveStartTime = 0;
                            int moveEndTime = 0;

                            ///// set calendar for startTime and endTime
                            if (hoursStartTime.getTimeInMillis() >= hoursEndTime.getTimeInMillis()) {
                                // endTime is over midnight
                                if (testEvent)
                                    PPApplication.logE("EventPreferencesTime.computeAlarm", "startTime >= endTime");

                                if ((hoursNowTime.getTimeInMillis() >= midnightTime.getTimeInMillis()) &&
                                        (hoursNowTime.getTimeInMillis() <= hoursEndTime.getTimeInMillis())) {
                                    // now is between midnight and endTime
                                    if (testEvent)
                                        PPApplication.logE("EventPreferencesTime.computeAlarm", "now is between midnight and endTime");

                                    calStartTime.add(Calendar.DAY_OF_YEAR, -1);
                                    moveStartTime = -1;
                                } else if ((hoursNowTime.getTimeInMillis() >= hoursStartTime.getTimeInMillis()) &&
                                        (hoursNowTime.getTimeInMillis() < midnightTime.getTimeInMillis())) {
                                    // now is between startTime and midnight
                                    if (testEvent)
                                        PPApplication.logE("EventPreferencesTime.computeAlarm", "now is between startTime and midnight");

                                    calEndTime.add(Calendar.DAY_OF_YEAR, 1);
                                    moveEndTime = 1;
                                } else {
                                    // now is before start time
                                    if (testEvent)
                                        PPApplication.logE("EventPreferencesTime.computeAlarm", "now is before start time");

                                    calEndTime.add(Calendar.DAY_OF_YEAR, 1);
                                    moveEndTime = 1;
                                }
                            } else {
                                if (testEvent)
                                    PPApplication.logE("EventPreferencesTime.computeAlarm", "startTime < endTime");

                                if (hoursNowTime.getTimeInMillis() > hoursEndTime.getTimeInMillis()) {
                                    // endTime is before actual time, compute for tomorrow
                                    if (testEvent)
                                        PPApplication.logE("EventPreferencesTime.computeAlarm", "nowTime > endTime");

                                    calStartTime.add(Calendar.DAY_OF_YEAR, 1);
                                    calEndTime.add(Calendar.DAY_OF_YEAR, 1);
                                    moveStartTime = 1;
                                    moveEndTime = 1;
                                }
                            }

                            //// get day by selected day of week
                            int startDayOfWeek = calStartTime.get(Calendar.DAY_OF_WEEK);
                            if (testEvent)
                                PPApplication.logE("EventPreferencesTime.computeAlarm", "startDayOfWeek="+startDayOfWeek);
                            boolean[] daysOfWeek = new boolean[8];
                            daysOfWeek[Calendar.SUNDAY] = this._sunday;
                            daysOfWeek[Calendar.MONDAY] = this._monday;
                            daysOfWeek[Calendar.TUESDAY] = this._tuesday;
                            daysOfWeek[Calendar.WEDNESDAY] = this._wednesday;
                            daysOfWeek[Calendar.THURSDAY] = this._thursday;
                            daysOfWeek[Calendar.FRIDAY] = this._friday;
                            daysOfWeek[Calendar.SATURDAY] = this._saturday;

                            if (daysOfWeek[startDayOfWeek]) {
                                // startTime of week is selected
                                if (testEvent)
                                    PPApplication.logE("EventPreferencesTime.computeAlarm", "startTime of week is selected");
                            } else {
                                // startTime of week is not selected,
                                if (testEvent) {
                                    PPApplication.logE("EventPreferencesTime.computeAlarm", "startTime of week is NOT selected");
                                    PPApplication.logE("EventPreferencesTime.computeAlarm", "startDayOfWeek=" + startDayOfWeek);
                                }

                                // search for selected day of week
                                boolean found = false;
                                int daysToAdd = 0;
                                for (int i = startDayOfWeek; i < 8; i++) {
                                    if (daysOfWeek[i]) {
                                        found = true;
                                        break;
                                    }
                                    ++daysToAdd;
                                }
                                if (!found) {
                                    for (int i = 1; i < startDayOfWeek; i++) {
                                        if (daysOfWeek[i]) {
                                            found = true;
                                            break;
                                        }
                                        ++daysToAdd;
                                    }
                                }
                                if (found) {
                                    if (testEvent)
                                        PPApplication.logE("EventPreferencesTime.computeAlarm", "daysToAdd=" + daysToAdd);

                                    for (int i = 0; i < 7; i++) {
                                        startDayOfWeek++;
                                        if (startDayOfWeek == 8)
                                            startDayOfWeek = 1;
                                    }
                                    //calStartTime.add(Calendar.DAY_OF_YEAR, daysToAdd);
                                    //calEndTime.add(Calendar.DAY_OF_YEAR, daysToAdd);
                                }
                            }
                            //////////////////////


                            /*int firstDayOfWeek = calStartTime.get(Calendar.DAY_OF_WEEK);
                            int nextDayOfWeek;
                            int previousDayOfWeek;
                            int firstDaysToAdd = 0;
                            int nextDaysToAdd = 0;
                            int previousDaysToRemove = 0;

                            boolean[] daysOfWeek = new boolean[8];
                            daysOfWeek[Calendar.SUNDAY] = this._sunday;
                            daysOfWeek[Calendar.MONDAY] = this._monday;
                            daysOfWeek[Calendar.TUESDAY] = this._tuesday;
                            daysOfWeek[Calendar.WEDNESDAY] = this._wednesday;
                            daysOfWeek[Calendar.THURSDAY] = this._thursday;
                            daysOfWeek[Calendar.FRIDAY] = this._friday;
                            daysOfWeek[Calendar.SATURDAY] = this._saturday;

                            // search for first selected day of week
                            boolean foundFirst = false;
                            for (int i = firstDayOfWeek; i < 8; i++) {
                                if (daysOfWeek[i]) {
                                    foundFirst = true;
                                    break;
                                }
                                ++firstDaysToAdd;
                            }
                            if (!foundFirst) {
                                for (int i = 1; i < firstDayOfWeek; i++) {
                                    if (daysOfWeek[i]) {
                                        //foundFirst = true;
                                        break;
                                    }
                                    ++firstDaysToAdd;
                                }
                            }

                            // next day
                            nextDayOfWeek = firstDayOfWeek + 1;
                            if (nextDayOfWeek == 8)
                                nextDayOfWeek = 1;

                            // search for next selected day of week
                            boolean foundNext = false;
                            for (int i = nextDayOfWeek; i < 8; i++) {
                                if (daysOfWeek[i]) {
                                    foundNext = true;
                                    break;
                                }
                                ++nextDaysToAdd;
                            }
                            if (!foundNext) {
                                for (int i = 1; i < nextDayOfWeek; i++) {
                                    if (daysOfWeek[i]) {
                                        //foundSecond = true;
                                        break;
                                    }
                                    ++nextDaysToAdd;
                                }
                            }

                            // previous day
                            previousDayOfWeek = firstDayOfWeek - 1;
                            if (previousDayOfWeek == 0)
                                previousDayOfWeek = 7;

                            // search for previous selected day of week
                            boolean foundPrevious = false;
                            for (int i = previousDayOfWeek; i > 0; i--) {
                                if (daysOfWeek[i]) {
                                    foundPrevious = true;
                                    break;
                                }
                                --previousDaysToRemove;
                            }
                            if (!foundPrevious) {
                                for (int i = 7; i > previousDayOfWeek; i--) {
                                    if (daysOfWeek[i]) {
                                        //foundSecond = true;
                                        break;
                                    }
                                    --previousDaysToRemove;
                                }
                            }

                            for (int i = 0; i < firstDaysToAdd; i++) {
                                firstDayOfWeek++;
                                if (firstDayOfWeek == 8)
                                    firstDayOfWeek = 1;
                            }
                            for (int i = 0; i < nextDaysToAdd; i++) {
                                nextDayOfWeek++;
                                if (nextDayOfWeek == 8)
                                    nextDayOfWeek = 1;
                            }
                            for (int i = 0; i < previousDaysToRemove; i++) {
                                previousDayOfWeek--;
                                if (previousDayOfWeek == 0)
                                    previousDayOfWeek = 7;
                            }*/
                            /////////////////

                            long[] twilightDaysOfWeekSunrise = twilightState.getDaysOfWeekSunrise();
                            long[] twilightDaysOfWeekSunset = twilightState.getDaysOfWeekSunset();

                            if (_timeType == TIME_TYPE_SUNRISE_SUNSET) {
                                if (testEvent)
                                    PPApplication.logE("EventPreferencesTime.computeAlarm", "from sunrise to sunset");

                                if (twilightDaysOfWeekSunrise[startDayOfWeek] != -1)
                                    calStartTime.setTimeInMillis(twilightDaysOfWeekSunrise[startDayOfWeek]);
                                else
                                    setAlarm = false;
                                startDayOfWeek++;
                                if (startDayOfWeek == 8)
                                    startDayOfWeek = 1;
                                if (twilightDaysOfWeekSunset[startDayOfWeek] != -1)
                                    calEndTime.setTimeInMillis(twilightDaysOfWeekSunset[startDayOfWeek]);
                                else
                                    setAlarm = false;

                                /*switch (moveStartTime) {
                                    case -1:
                                        if (twilightDaysOfWeekSunrise[previousDayOfWeek] != -1)
                                            calStartTime.setTimeInMillis(twilightDaysOfWeekSunrise[previousDayOfWeek]);
                                        else
                                            setAlarm = false;
                                        break;
                                    case 0:
                                        if (twilightDaysOfWeekSunrise[firstDayOfWeek] != -1)
                                            calStartTime.setTimeInMillis(twilightDaysOfWeekSunrise[firstDayOfWeek]);
                                        else
                                            setAlarm = false;
                                        break;
                                    case 1:
                                        if (twilightDaysOfWeekSunrise[nextDayOfWeek] != -1)
                                            calStartTime.setTimeInMillis(twilightDaysOfWeekSunrise[nextDayOfWeek]);
                                        else
                                            setAlarm = false;
                                        break;
                                }
                                switch (moveEndTime) {
                                    //noinspection ConstantConditions
                                    case -1:
                                        if (twilightDaysOfWeekSunset[previousDayOfWeek] != -1)
                                            calEndTime.setTimeInMillis(twilightDaysOfWeekSunset[previousDayOfWeek]);
                                        else
                                            setAlarm = false;
                                        break;
                                    case 0:
                                        if (twilightDaysOfWeekSunset[firstDayOfWeek] != -1)
                                            calEndTime.setTimeInMillis(twilightDaysOfWeekSunset[firstDayOfWeek]);
                                        else
                                            setAlarm = false;
                                        break;
                                    case 1:
                                        if (twilightDaysOfWeekSunset[nextDayOfWeek] != -1)
                                            calEndTime.setTimeInMillis(twilightDaysOfWeekSunset[nextDayOfWeek]);
                                        else
                                            setAlarm = false;
                                        break;
                                }*/
                            }
                            else {
                                if (testEvent)
                                    PPApplication.logE("EventPreferencesTime.computeAlarm", "from sunset to sunrise");

                                if (twilightDaysOfWeekSunset[startDayOfWeek] != -1)
                                    calStartTime.setTimeInMillis(twilightDaysOfWeekSunset[startDayOfWeek]);
                                else
                                    setAlarm = false;
                                startDayOfWeek++;
                                if (startDayOfWeek == 8)
                                    startDayOfWeek = 1;
                                if (twilightDaysOfWeekSunrise[startDayOfWeek] != -1)
                                    calEndTime.setTimeInMillis(twilightDaysOfWeekSunrise[startDayOfWeek]);
                                else
                                    setAlarm = false;

                                /*switch (moveStartTime) {
                                    case -1:
                                        if (twilightDaysOfWeekSunset[previousDayOfWeek] != -1)
                                            calStartTime.setTimeInMillis(twilightDaysOfWeekSunset[previousDayOfWeek]);
                                        else
                                            setAlarm = false;
                                        break;
                                    case 0:
                                        if (twilightDaysOfWeekSunset[firstDayOfWeek] != -1)
                                            calStartTime.setTimeInMillis(twilightDaysOfWeekSunset[firstDayOfWeek]);
                                        else
                                            setAlarm = false;
                                        break;
                                    case 1:
                                        if (twilightDaysOfWeekSunset[nextDayOfWeek] != -1)
                                            calStartTime.setTimeInMillis(twilightDaysOfWeekSunset[nextDayOfWeek]);
                                        else
                                            setAlarm = false;
                                        break;
                                }
                                switch (moveEndTime) {
                                    //noinspection ConstantConditions
                                    case -1:
                                        if (twilightDaysOfWeekSunrise[previousDayOfWeek] != -1)
                                            calEndTime.setTimeInMillis(twilightDaysOfWeekSunrise[previousDayOfWeek]);
                                        else
                                            setAlarm = false;
                                        break;
                                    case 0:
                                        if (twilightDaysOfWeekSunrise[firstDayOfWeek] != -1)
                                            calEndTime.setTimeInMillis(twilightDaysOfWeekSunrise[firstDayOfWeek]);
                                        else
                                            setAlarm = false;
                                        break;
                                    case 1:
                                        if (twilightDaysOfWeekSunrise[nextDayOfWeek] != -1)
                                            calEndTime.setTimeInMillis(twilightDaysOfWeekSunrise[nextDayOfWeek]);
                                        else
                                            setAlarm = false;
                                        break;
                                }*/
                            }
                            if (testEvent) {
                                PPApplication.logE("EventPreferencesTime.computeAlarm", "callStartTime=" + DateFormat.getDateFormat(context).format(calStartTime.getTimeInMillis()) +
                                        " " + DateFormat.getTimeFormat(context).format(calStartTime.getTimeInMillis()));
                                PPApplication.logE("EventPreferencesTime.computeAlarm", "callEndTime=" + DateFormat.getDateFormat(context).format(calEndTime.getTimeInMillis()) +
                                        " " + DateFormat.getTimeFormat(context).format(calEndTime.getTimeInMillis()));
                            }
                            ////////////////////////////

                        }
                        else
                            setAlarm = false;
                    }
                    else
                    if (testEvent)
                        PPApplication.logE("EventPreferencesTime.computeAlarm", "TwilightState NOT set");
                }
                else
                if (testEvent)
                    PPApplication.logE("EventPreferencesTime.computeAlarm", "TwilightScanner NOT started");
            }
            else
            if (testEvent)
                PPApplication.logE("EventPreferencesTime.computeAlarm", "PPService NOT started");
        }

        if (setAlarm) {
            long alarmTime;
            if (startEvent)
                alarmTime = calStartTime.getTimeInMillis();
            else
                alarmTime = calEndTime.getTimeInMillis();

            return alarmTime;
        }
        else
            return 0;
    }

    @Override
    public void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        PPApplication.logE("EventPreferencesTime.setSystemEventForStart","xxx");

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        //removeAlarm(true, _context);
        removeAlarm(/*false, */context);

        if (!(isRunnable(context) && _enabled))
            return;

        long alarmTime = computeAlarm(true, context/*_timeType == EventPreferencesTime.TIME_TYPE_EXACT*/);
        if (alarmTime > 0)
            setAlarm(true, alarmTime, context);

        alarmTime = computeAlarm(false, context/*_timeType == EventPreferencesTime.TIME_TYPE_EXACT*/);
        if (alarmTime > 0)
            setAlarm(false, alarmTime, context);
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        PPApplication.logE("EventPreferencesTime.setSystemEventForPause","xxx");

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        //removeAlarm(true, _context);
        removeAlarm(/*false, */context);

        if (!(isRunnable(context) && _enabled))
            return;

        long alarmTime = computeAlarm(false, context/*_timeType == EventPreferencesTime.TIME_TYPE_EXACT*/);
        if (alarmTime > 0)
            setAlarm(false, alarmTime, context);

        alarmTime = computeAlarm(true, context/*_timeType == EventPreferencesTime.TIME_TYPE_EXACT*/);
        if (alarmTime > 0)
            setAlarm(true, alarmTime, context);
    }

    @Override
    public void removeSystemEvent(Context context)
    {
        // remove alarms for state STOP

        //removeAlarm(true, _context);
        removeAlarm(/*false, */context);

        //PPApplication.logE("EventPreferencesTime.removeSystemEvent","forceNotUseAlarmClock="+ApplicationPreferences.forceNotUseAlarmClock);
        PPApplication.logE("EventPreferencesTime.removeSystemEvent","xxx");
    }

    private void removeAlarm(/*boolean startEvent, */Context context)
    {
        PPApplication.logE("EventPreferencesTime.removeAlarm", "event="+_event._name);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            //Intent intent = new Intent(context, EventTimeBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_EVENT_TIME_BROADCAST_RECEIVER);
            //intent.setClass(context, EventPreferencesTime.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
            PPApplication.logE("EventPreferencesTime.removeAlarm", "pendingIntent="+pendingIntent);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                PPApplication.logE("EventPreferencesTime.removeAlarm", "event="+_event._name + " alarm removed");
            }
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    private void setAlarm(boolean startEvent, long alarmTime, Context context)
    {
        if (PPApplication.logEnabled()) {
            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            String result = sdf.format(alarmTime);
            if (startEvent)
                PPApplication.logE("EventPreferencesTime.setAlarm", "startTime=" + result);
            else
                PPApplication.logE("EventPreferencesTime.setAlarm", "endTime=" + result);
        }

        boolean applicationUseAlarmClock = ApplicationPreferences.applicationUseAlarmClock(context);

        // not set alarm if alarmTime is over.
        Calendar now = Calendar.getInstance();
        if (/*(android.os.Build.VERSION.SDK_INT >= 21) &&*/
                applicationUseAlarmClock) {
            if (now.getTimeInMillis() > (alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET)) {
                PPApplication.logE("EventPreferencesTime.setAlarm", "event="+_event._name + " alarm clock is over");
                return;
            }
        }
        else {
            if (now.getTimeInMillis() > (alarmTime + Event.EVENT_ALARM_TIME_OFFSET)) {
                PPApplication.logE("EventPreferencesTime.setAlarm", "event="+_event._name + " alarm is over");
                return;
            }
        }

        //Intent intent = new Intent(context, EventTimeBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PhoneProfilesService.ACTION_EVENT_TIME_BROADCAST_RECEIVER);
        //intent.setClass(context, EventPreferencesTime.class);

        //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (/*(android.os.Build.VERSION.SDK_INT >= 21) &&*/
                    applicationUseAlarmClock) {
                Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                int requestCode = 1000;
                if (!startEvent)
                    requestCode = -1000;
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, requestCode, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
                PPApplication.logE("EventPreferencesTime.setAlarm", "event="+_event._name + " alarm clock set");
            }
            else {
                if (android.os.Build.VERSION.SDK_INT >= 23)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                else //if (android.os.Build.VERSION.SDK_INT >= 19)
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                //else
                //    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                PPApplication.logE("EventPreferencesTime.setAlarm", "event="+_event._name + " alarm set");
            }
        }
    }

}
