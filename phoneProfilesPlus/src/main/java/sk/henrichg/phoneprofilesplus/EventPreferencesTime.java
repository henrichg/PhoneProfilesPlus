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
                                alarmTime = computeAlarm(true, true);
                                // date and time format by user system settings configuration
                                alarmTimeS = "(st) " + DateFormat.getDateFormat(context).format(alarmTime) +
                                        " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                descr = descr + "<br>"; //'\n';
                                descr = descr + "&nbsp;&nbsp;&nbsp;-> " + alarmTimeS;
                            } else if ((_event.getStatus() == Event.ESTATUS_RUNNING)/* && _useEndTime*/) {
                                alarmTime = computeAlarm(false, true);
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
                                long startTime = computeAlarm(true, false);
                                long endTime = computeAlarm(false, false);
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
                                                alarmTime = computeAlarm(true, true);
                                                // date and time format by user system settings configuration
                                                alarmTimeS = "(st) " + DateFormat.getDateFormat(context).format(alarmTime) +
                                                        " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                                descr = descr + "<br>"; //'\n';
                                                descr = descr + "&nbsp;&nbsp;&nbsp;-> " + alarmTimeS;
                                            } else if ((_event.getStatus() == Event.ESTATUS_RUNNING)/* && _useEndTime*/) {
                                                alarmTime = computeAlarm(false, true);
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
                key.equals(PREF_EVENT_TIME_TYPE))
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

    long computeAlarm(boolean startEvent, boolean addWeekDay)
    {
        PPApplication.logE("EventPreferencesTime.computeAlarm","startEvent="+startEvent);

        Calendar now = Calendar.getInstance();

        Calendar calStartTime = Calendar.getInstance();
        Calendar calEndTime = Calendar.getInstance();

        boolean setAlarm = false;

        if (_timeType == TIME_TYPE_EXACT) {
            setAlarm = true;

            ///// set calendar for startTime and endTime
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

            if (calStartTime.getTimeInMillis() >= calEndTime.getTimeInMillis())
            {
                // endTime is over midnight
                PPApplication.logE("EventPreferencesTime.computeAlarm","startTime >= endTime");

                /*if (now.getTimeInMillis() < calEndTime.getTimeInMillis())
                {
                    // now is before endTime
                    // decrease start/end time
                    calStartTime.add(Calendar.DAY_OF_YEAR, -1);
                    calEndTime.add(Calendar.DAY_OF_YEAR, -1);
                }*/

                // add next day to end time
                calEndTime.add(Calendar.DAY_OF_YEAR, 1);
            }
            else
            if (calEndTime.getTimeInMillis() < now.getTimeInMillis())
            {
                // endTime is before actual time, compute for tomorrow
                calStartTime.add(Calendar.DAY_OF_YEAR, 1);
                calEndTime.add(Calendar.DAY_OF_YEAR, 1);
            }
            ////////////////////////////

            if (addWeekDay) {
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
                    PPApplication.logE("EventPreferencesTime.computeAlarm", "startTime of week is selected");
                } else {
                    // startTime of week is not selected,
                    PPApplication.logE("EventPreferencesTime.computeAlarm", "startTime of week is NOT selected");
                    PPApplication.logE("EventPreferencesTime.computeAlarm", "startDayOfWeek=" + startDayOfWeek);

                    // search for selected day of week
                    boolean found = false;
                    int daysToAdd = 0;
                    for (int i = startDayOfWeek + 1; i < 8; i++) {
                        ++daysToAdd;
                        if (daysOfWeek[i]) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        for (int i = 1; i < startDayOfWeek; i++) {
                            ++daysToAdd;
                            if (daysOfWeek[i]) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (found) {
                        PPApplication.logE("EventPreferencesTime.computeAlarm", "daysToAdd=" + daysToAdd);
                        calStartTime.add(Calendar.DAY_OF_YEAR, daysToAdd);
                        calEndTime.add(Calendar.DAY_OF_YEAR, daysToAdd);
                    }
                }
                //////////////////////
            }
        }
        else {
            if (PhoneProfilesService.getInstance() != null) {
                PPApplication.logE("EventPreferencesTime.computeAlarm", "PPService started");
                TwilightScanner twilightScanner = PhoneProfilesService.getInstance().getTwilightScanner();
                if (twilightScanner != null) {
                    PPApplication.logE("EventPreferencesTime.computeAlarm", "TwilightScanner started");
                    TwilightState twilightState = twilightScanner.getTwilightState();
                    if (twilightState != null) {
                        PPApplication.logE("EventPreferencesTime.computeAlarm", "TwilightState set");
                        setAlarm = true;

                        //// get day by selected day of week
                        int firstDayOfWeek = now.get(Calendar.DAY_OF_WEEK);
                        int secondDayOfWeek = firstDayOfWeek;
                        int firstDaysToAdd = 0;
                        int secondDaysToAdd = 0;

                        if (addWeekDay) {
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
                                        foundFirst = true;
                                        break;
                                    }
                                    ++firstDaysToAdd;
                                }
                            }
                            if (foundFirst) {
                                // next day
                                secondDayOfWeek = firstDayOfWeek + 1;
                                if (secondDayOfWeek == 8)
                                    secondDayOfWeek = 1;
                            }

                            // search for second selected day of week
                            boolean foundSecond = false;
                            for (int i = secondDayOfWeek + 1; i < 8; i++) {
                                if (daysOfWeek[i]) {
                                    foundSecond = true;
                                    break;
                                }
                                ++secondDaysToAdd;
                            }
                            if (!foundSecond) {
                                for (int i = 1; i < secondDayOfWeek; i++) {
                                    if (daysOfWeek[i]) {
                                        //foundSecond = true;
                                        break;
                                    }
                                    ++secondDaysToAdd;
                                }
                            }

                            for (int i = 0; i < firstDaysToAdd; i++) {
                                firstDayOfWeek++;
                                if (firstDayOfWeek == 0)
                                    firstDayOfWeek = 1;
                            }
                            for (int i = 0; i < secondDaysToAdd; i++) {
                                secondDayOfWeek++;
                                if (secondDayOfWeek == 0)
                                    secondDayOfWeek = 1;
                            }
                        }
                        /////////////////

                        ///// set calendar for startTime and endTime
                        if (_timeType == TIME_TYPE_SUNRISE_SUNSET) {
                            if (addWeekDay) {
                                long[] twilightDaysOfWeekSunrise = twilightState.getDaysOfWeekSunrise();
                                long[] twilightDaysOfWeekSunset = twilightState.getDaysOfWeekSunset();
                                if ((twilightDaysOfWeekSunrise[firstDayOfWeek] != 1) && (twilightDaysOfWeekSunset[firstDayOfWeek] != 1)) {
                                    // today
                                    calStartTime.setTimeInMillis(twilightDaysOfWeekSunrise[firstDayOfWeek]);
                                    calEndTime.setTimeInMillis(twilightDaysOfWeekSunset[firstDayOfWeek]);

                                    // endTime is before actual time, compute for tomorrow
                                    if (calEndTime.getTimeInMillis() < now.getTimeInMillis()) {
                                        if ((twilightDaysOfWeekSunrise[secondDayOfWeek] != 1) && (twilightDaysOfWeekSunset[secondDayOfWeek] != 1)) {
                                            calStartTime.setTimeInMillis(twilightDaysOfWeekSunrise[secondDayOfWeek]);
                                            calEndTime.setTimeInMillis(twilightDaysOfWeekSunset[secondDayOfWeek]);
                                        } else
                                            setAlarm = false;
                                    }
                                } else
                                    setAlarm = false;

                            }
                            else {
                                if ((twilightState.getTodaySunrise() != -1) && (twilightState.getTodaySunset() != -1)) {
                                    calStartTime.setTimeInMillis(twilightState.getTodaySunrise());
                                    calEndTime.setTimeInMillis(twilightState.getTodaySunset());

                                    // endTime is before actual time, compute for future
                                    if (calEndTime.getTimeInMillis() < now.getTimeInMillis()) {
                                        if ((twilightState.getTomorrowSunrise() != -1) && (twilightState.getTomorrowSunset() != -1)) {
                                            calStartTime.setTimeInMillis(twilightState.getTomorrowSunrise());
                                            calEndTime.setTimeInMillis(twilightState.getTomorrowSunset());
                                        } else
                                            setAlarm = false;
                                    }
                                } else
                                    setAlarm = false;
                            }
                        } else {
                            if (addWeekDay) {
                                long[] twilightDaysOfWeekSunrise = twilightState.getDaysOfWeekSunrise();
                                long[] twilightDaysOfWeekSunset = twilightState.getDaysOfWeekSunset();
                                if ((twilightDaysOfWeekSunset[firstDayOfWeek] != 1) && (twilightDaysOfWeekSunrise[secondDayOfWeek] != 1)) {
                                    calStartTime.setTimeInMillis(twilightDaysOfWeekSunset[firstDayOfWeek]);
                                    calEndTime.setTimeInMillis(twilightDaysOfWeekSunrise[secondDayOfWeek]);
                                } else
                                    setAlarm = false;
                            }
                            else {
                                if ((twilightState.getTodaySunset() != -1) && (twilightState.getTomorrowSunrise() != -1)) {
                                    calStartTime.setTimeInMillis(twilightState.getTodaySunset());
                                    calEndTime.setTimeInMillis(twilightState.getTomorrowSunrise());
                                } else
                                    setAlarm = false;
                            }
                        }
                        ////////////////////////////
                    }
                    else
                        PPApplication.logE("EventPreferencesTime.computeAlarm", "TwilightState NOT set");
                }
                else
                    PPApplication.logE("EventPreferencesTime.computeAlarm", "TwilightScanner NOT started");
            }
            else
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

        long alarmTime = computeAlarm(true, false/*_timeType == EventPreferencesTime.TIME_TYPE_EXACT*/);
        if (alarmTime > 0)
            setAlarm(true, alarmTime, context);

        alarmTime = computeAlarm(false, false/*_timeType == EventPreferencesTime.TIME_TYPE_EXACT*/);
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

        long alarmTime = computeAlarm(false, false/*_timeType == EventPreferencesTime.TIME_TYPE_EXACT*/);
        if (alarmTime > 0)
            setAlarm(false, alarmTime, context);

        alarmTime = computeAlarm(true, false/*_timeType == EventPreferencesTime.TIME_TYPE_EXACT*/);
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
