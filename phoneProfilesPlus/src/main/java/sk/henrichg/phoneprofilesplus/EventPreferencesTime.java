package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.CharacterStyle;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.sql.Date;
import java.text.DateFormatSymbols;
import java.util.Calendar;

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
    static final String PREF_EVENT_TIME_TYPE = "eventTimeType";
    //private static final String PREF_EVENT_TIME_USE_END_TIME = "eventTimeUseEndTime";
    static final String PREF_EVENT_TIME_LOCATION_SYSTEM_SETTINGS = "eventTimeLocationSystemSettings";
    static final String PREF_EVENT_TIME_APP_SETTINGS = "eventTimePeriodicScanningAppSettings";

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

    void copyPreferences(Event fromEvent)
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

    void loadSharedPreferences(SharedPreferences preferences)
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

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_TIME_ENABLED, false);

        String sDays = preferences.getString(PREF_EVENT_TIME_DAYS, DaysOfWeekPreference.allValue);
        String[] splits = sDays.split(StringConstants.STR_SPLIT_REGEX);
        if (splits[0].equals(DaysOfWeekPreference.allValue))
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

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_time_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_TIME_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_time), addPassStatus, DatabaseHandler.ETYPE_TIME, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                if (_timeType != TIME_TYPE_EXACT) {
                    if (!GlobalUtils.isLocationEnabled(context.getApplicationContext())) {
                        _value.append("* ").append(context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary)).append("! *").append(StringConstants.TAG_BREAK_HTML);
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

                _value.append(context.getString(R.string.event_preferences_time_timeDays)).append(StringConstants.STR_COLON_WITH_SPACE);
                //if (allDays) {
                //    descr = descr + "<b>" + context.getString(R.string.array_pref_event_all) + "</b>";
                //    descr = descr + " ";
                //} else
                {
                    String[] namesOfDay = DateFormatSymbols.getInstance().getShortWeekdays();

                    String _descr;// = "";
                    StringBuilder value = new StringBuilder();
                    int dayOfWeek;
                    for (int i = 0; i < 7; i++) {
                        dayOfWeek = getDayOfWeekByLocale(i);

                        if (daySet[dayOfWeek])
                            //_descr = _descr + namesOfDay[dayOfWeek + 1] + " ";
                            value.append(namesOfDay[dayOfWeek + 1]).append(" ");
                    }
                    _descr = value.toString();
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(_descr, disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                }

                _value.append(StringConstants.CHAR_BULLET).append(" ").append(context.getString(R.string.event_preferences_time_timeType)).append(StringConstants.STR_COLON_WITH_SPACE);
                String _descr = "";
                switch (_timeType) {
                    case TIME_TYPE_EXACT:
                        _descr = _descr + context.getString(R.string.event_preference_sensor_time_type_exact);
                        break;
                    case TIME_TYPE_SUNRISE_SUNSET:
                        _descr = _descr + context.getString(R.string.event_preference_sensor_time_type_sunrise_sunset);
                        break;
                    case TIME_TYPE_SUNSET_SUNRISE:
                        _descr = _descr + context.getString(R.string.event_preference_sensor_time_type_sunset_sunrise);
                        break;
                }
                _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(_descr, disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);

                if (_timeType == TIME_TYPE_EXACT) {
                    _value.append(StringConstants.STR_BULLET);

                    _value.append(context.getString(R.string.event_preferences_time_startTime)).append("-").append(context.getString(R.string.event_preferences_time_endTime)).append(StringConstants.STR_COLON_WITH_SPACE);

                    _descr = "";
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, _startTime / 60);
                    calendar.set(Calendar.MINUTE, _startTime % 60);
                    _descr = _descr + DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));
                    //if (tmp._useEndTime)
                    //{
                    calendar.set(Calendar.HOUR_OF_DAY, _endTime / 60);
                    calendar.set(Calendar.MINUTE, _endTime % 60);
                    _descr = _descr + "-";
                    _descr = _descr + DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));
                    //}
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(_descr, disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);

                    if (addBullet) {
                        if (EventStatic.getGlobalEventsRunning(context)) {
                            long alarmTime;
                            //SimpleDateFormat sdf = new SimpleDateFormat("EEd/MM/yy HH:mm");
                            String alarmTimeS;
                            if (_event.getStatus() == Event.ESTATUS_PAUSE) {
                                alarmTime = computeAlarm(true/*, context*/);
                                // date and time format by user system settings configuration
                                alarmTimeS = "(st) " + DateFormat.getDateFormat(context).format(alarmTime) +
                                        " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                _value.append(StringConstants.TAG_BREAK_HTML);
                                _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append("-> ").append(alarmTimeS);
                            } else if ((_event.getStatus() == Event.ESTATUS_RUNNING)/* && _useEndTime*/) {
                                alarmTime = computeAlarm(false/*, context*/);
                                // date and time format by user system settings configuration
                                alarmTimeS = "(et) " + DateFormat.getDateFormat(context).format(alarmTime) +
                                        " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                _value.append(StringConstants.TAG_BREAK_HTML);
                                _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append("-> ").append(alarmTimeS);
                            }
                        }
                    }
                }
                else {
                    if (PhoneProfilesService.getInstance() != null) {
                        if (PPApplication.twilightScanner != null) {
                            TwilightState twilightState = PPApplication.twilightScanner.getTwilightState(/*true*/);
                            if (twilightState != null) {
                                long startTime = computeAlarm(true/*, context*/);
                                long endTime = computeAlarm(false/*, context*/);
                                if ((startTime != 0) && (endTime != 0)) {
                                    _value.append(StringConstants.STR_BULLET);

                                    if (_timeType == TIME_TYPE_SUNRISE_SUNSET)
                                        _value.append(context.getString(R.string.event_preference_sensor_time_sunrise)).append("-").append(context.getString(R.string.event_preference_sensor_time_sunset)).append(StringConstants.STR_COLON_WITH_SPACE);
                                    else
                                        _value.append(context.getString(R.string.event_preference_sensor_time_sunset)).append("-").append(context.getString(R.string.event_preference_sensor_time_sunrise)).append(StringConstants.STR_COLON_WITH_SPACE);

                                    _descr = "";
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeInMillis(startTime);
                                    _descr = _descr + DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));
                                    calendar.setTimeInMillis(endTime);
                                    _descr = _descr + "-";
                                    _descr = _descr + DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));
                                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(_descr, disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);

                                    if (addBullet) {
                                        if (EventStatic.getGlobalEventsRunning(context)) {
                                            long alarmTime;
                                            //SimpleDateFormat sdf = new SimpleDateFormat("EEd/MM/yy HH:mm");
                                            String alarmTimeS;
                                            if (_event.getStatus() == Event.ESTATUS_PAUSE) {
                                                alarmTime = computeAlarm(true/*, context*/);
                                                // date and time format by user system settings configuration
                                                alarmTimeS = "(st) " + DateFormat.getDateFormat(context).format(alarmTime) +
                                                        " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                                _value.append(StringConstants.TAG_BREAK_HTML);
                                                _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append("-> ").append(alarmTimeS);
                                            } else if ((_event.getStatus() == Event.ESTATUS_RUNNING)/* && _useEndTime*/) {
                                                alarmTime = computeAlarm(false/*, context*/);
                                                // date and time format by user system settings configuration
                                                alarmTimeS = "(et) " + DateFormat.getDateFormat(context).format(alarmTime) +
                                                        " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                                _value.append(StringConstants.TAG_BREAK_HTML);
                                                _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append("-> ").append(alarmTimeS);
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

        return _value.toString();
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

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_TIME_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_TIME_ENABLED) ||
            key.equals(PREF_EVENT_TIME_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_TIME_APP_SETTINGS);
            if (preference != null) {
                String summary;
                //int titleColor;
                if (!ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) {
                    //if (!ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile) {
                        summary = context.getString(R.string.array_pref_applicationDisableScanning_disabled) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                                            context.getString(R.string.phone_profiles_pref_eventBackgroundScanningAppSettings_summary);
                        //titleColor = ContextCompat.getColor(context, R.color.altype_error);
                    //}
                    //else {
                    //    summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile) + "\n\n" +
                    //            context.getString(R.string.phone_profiles_pref_eventBackgroundScanningAppSettings_summary);
                    //    titleColor = 0;
                    //}
                }
                else {
                    boolean scanningPaused = ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiply.equals("2") &&
                            GlobalUtils.isNowTimeBetweenTimes(
                                    ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyFrom,
                                    ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyTo);
                    if (scanningPaused) {
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventBackgroundScanningAppSettings_summary);
                    } else {
                        summary = context.getString(R.string.array_pref_applicationDisableScanning_enabled) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventBackgroundScanningAppSettings_summary);
                    }
                    //titleColor = 0;
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
                //if (preferences.getBoolean(PREF_EVENT_TIME_ENABLED, false)) {
                //    if (titleColor != 0)
                //        sbt.setSpan(new ForegroundColorSpan(titleColor), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //}
                preference.setTitle(sbt);
                preference.setSummary(summary);
            }
        }

        if (key.equals(PREF_EVENT_TIME_TYPE)) {
            PPListPreference listPreference = prefMng.findPreference(key);
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
                if (!GlobalUtils.isLocationEnabled(context.getApplicationContext())) {
                    summary = "* " + context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + "! *"+StringConstants.STR_DOUBLE_NEWLINE+
                            summary;
                }
                else {
                    summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT+
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
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        /*preference = prefMng.findPreference(PREF_EVENT_TIME_TYPE);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_TIME_TYPE, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, true, !isRunnable, false);
        }*/
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_TIME_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
        if (key.equals(PREF_EVENT_TIME_DAYS) ||
                key.equals(PREF_EVENT_TIME_TYPE) ||
                key.equals(PREF_EVENT_TIME_LOCATION_SYSTEM_SETTINGS) ||
                key.equals(PREF_EVENT_TIME_APP_SETTINGS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_TIME_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_TIME_DAYS, preferences, context);
        setSummary(prefMng, PREF_EVENT_TIME_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_TIME_LOCATION_SYSTEM_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_TIME_APP_SETTINGS, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_TIME_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesTime tmp = new EventPreferencesTime(this._event, this._enabled, this._sunday, this._monday, this._tuesday, this._wednesday,
                    this._thursday, this._friday, this._saturday, this._startTime, this._endTime, this._timeType);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_TIME_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_TIME_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_TIME).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false, false, false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_TIME_CATEGORY);
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
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_TIME_ENABLED) != null) {
                setSummary(prefMng, PREF_EVENT_TIME_LOCATION_SYSTEM_SETTINGS, preferences, context);
                setSummary(prefMng, PREF_EVENT_TIME_APP_SETTINGS, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    private long computeAlarm(boolean startEvent/*, Context context*/)
    {
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

            Calendar nowTime = Calendar.getInstance();
            nowTime.set(Calendar.DAY_OF_MONTH, 0);
            nowTime.set(Calendar.MONTH, 0);
            nowTime.set(Calendar.YEAR, 0);

            /*Calendar hoursNowTime = Calendar.getInstance();
            hoursNowTime.set(Calendar.DAY_OF_MONTH, 0);
            hoursNowTime.set(Calendar.MONTH, 0);
            hoursNowTime.set(Calendar.YEAR, 0);
            hoursNowTime.set(Calendar.SECOND, 0);
            hoursNowTime.set(Calendar.MILLISECOND, 0);*/

            Calendar midnightTime = Calendar.getInstance();
            midnightTime.set(Calendar.HOUR_OF_DAY, 0);
            midnightTime.set(Calendar.MINUTE, 0);
            midnightTime.set(Calendar.SECOND, 0);
            midnightTime.set(Calendar.MILLISECOND, 0);
            midnightTime.set(Calendar.DAY_OF_MONTH, 0);
            midnightTime.set(Calendar.MONTH, 0);
            midnightTime.set(Calendar.YEAR, 0);

            Calendar midnightMinusOneTime = Calendar.getInstance();
            midnightMinusOneTime.set(Calendar.HOUR_OF_DAY, 23);
            midnightMinusOneTime.set(Calendar.MINUTE, 59);
            midnightMinusOneTime.set(Calendar.SECOND, 59);
            midnightMinusOneTime.set(Calendar.MILLISECOND, 999);
            midnightMinusOneTime.set(Calendar.DAY_OF_MONTH, 0);
            midnightMinusOneTime.set(Calendar.MONTH, 0);
            midnightMinusOneTime.set(Calendar.YEAR, 0);

            calStartTime.set(Calendar.HOUR_OF_DAY, _startTime / 60);
            calStartTime.set(Calendar.MINUTE, _startTime % 60);
            calStartTime.set(Calendar.SECOND, 0);
            calStartTime.set(Calendar.MILLISECOND, 0);
            calStartTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
            calStartTime.set(Calendar.MONTH, now.get(Calendar.MONTH));
            calStartTime.set(Calendar.YEAR, now.get(Calendar.YEAR));

            calEndTime.set(Calendar.HOUR_OF_DAY, _endTime / 60);
            calEndTime.set(Calendar.MINUTE, _endTime % 60);
            calEndTime.set(Calendar.SECOND, 0);
            calEndTime.set(Calendar.MILLISECOND, 0);
            calEndTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
            calEndTime.set(Calendar.MONTH, now.get(Calendar.MONTH));
            calEndTime.set(Calendar.YEAR, now.get(Calendar.YEAR));

            if (hoursStartTime.getTimeInMillis() >= hoursEndTime.getTimeInMillis())
            {
                // endTime is over midnight

                if ((nowTime.getTimeInMillis() >= midnightTime.getTimeInMillis()) &&
                    (nowTime.getTimeInMillis() <= hoursEndTime.getTimeInMillis())) {
                    // now is between midnight and endTime

                    calStartTime.add(Calendar.DAY_OF_YEAR, -1);
                }
                else
                if ((nowTime.getTimeInMillis() >= hoursStartTime.getTimeInMillis()) &&
                    (nowTime.getTimeInMillis() <= midnightMinusOneTime.getTimeInMillis())) {
                    // now is between startTime and midnight

                    calEndTime.add(Calendar.DAY_OF_YEAR, 1);
                }
                else {
                    // now is before start time

                    calEndTime.add(Calendar.DAY_OF_YEAR, 1);
                }
            }
            else {

                if (nowTime.getTimeInMillis() > hoursEndTime.getTimeInMillis()) {
                    // now is after end time, compute for tomorrow

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
            //noinspection StatementWithEmptyBody
            if (daysOfWeek[startDayOfWeek]) {
                // week for startTime is selected
            } else {
                // week for startTime is not selected,

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
                    calStartTime.add(Calendar.DAY_OF_YEAR, daysToAdd);
                    calEndTime.add(Calendar.DAY_OF_YEAR, daysToAdd);
                }
            }
            //////////////////////

        }
        else {
            if (PhoneProfilesService.getInstance() != null) {
                if (PPApplication.twilightScanner != null) {
                    TwilightState twilightState = PPApplication.twilightScanner.getTwilightState(/*false*//*testEvent*/);
                    if (twilightState != null) {
                        setAlarm = true;

                        if ((twilightState.getTodaySunset() != -1) && (twilightState.getTodaySunrise() != -1)) {
                            Calendar hoursStartTime = Calendar.getInstance();

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

                            Calendar middayTime = Calendar.getInstance();
                            middayTime.set(Calendar.HOUR_OF_DAY, 12);
                            middayTime.set(Calendar.MINUTE, 0);
                            middayTime.set(Calendar.DAY_OF_MONTH, 0);
                            middayTime.set(Calendar.MONTH, 0);
                            middayTime.set(Calendar.YEAR, 0);
                            middayTime.set(Calendar.SECOND, 0);
                            middayTime.set(Calendar.MILLISECOND, 0);

                            boolean inMorning =
                                (hoursNowTime.getTimeInMillis() >= midnightTime.getTimeInMillis()) &&
                                (hoursNowTime.getTimeInMillis() < middayTime.getTimeInMillis());

                            if (_timeType == TIME_TYPE_SUNRISE_SUNSET) {
                                Calendar todaySunset = Calendar.getInstance();
                                todaySunset.setTimeInMillis(twilightState.getTodaySunset());
                                if (now.compareTo(todaySunset) > 0)
                                    calStartTime.setTimeInMillis(twilightState.getTomorrowSunrise());
                                else
                                    calStartTime.setTimeInMillis(twilightState.getTodaySunrise());
                            } else {
                                if (inMorning) {
                                    calStartTime.setTimeInMillis(twilightState.getYesterdaySunset());
                                    calEndTime.setTimeInMillis(twilightState.getTodaySunrise());
                                }
                                else {
                                    calStartTime.setTimeInMillis(twilightState.getTodaySunset());
                                    calEndTime.setTimeInMillis(twilightState.getTomorrowSunrise());
                                }
                                if (now.compareTo(calEndTime) > 0)
                                    calStartTime.setTimeInMillis(twilightState.getTodaySunset());
                            }
                            hoursStartTime.setTimeInMillis(calStartTime.getTimeInMillis());
                            hoursStartTime.set(Calendar.DAY_OF_MONTH, 0);
                            hoursStartTime.set(Calendar.MONTH, 0);
                            hoursStartTime.set(Calendar.YEAR, 0);
                            hoursStartTime.set(Calendar.SECOND, 0);
                            hoursStartTime.set(Calendar.MILLISECOND, 0);

                            Calendar hoursEndTime = Calendar.getInstance();
                            if (_timeType == TIME_TYPE_SUNRISE_SUNSET) {
                                Calendar todaySunset = Calendar.getInstance();
                                todaySunset.setTimeInMillis(twilightState.getTodaySunset());
                                if (now.compareTo(todaySunset) > 0)
                                    calStartTime.setTimeInMillis(twilightState.getTomorrowSunset());
                                else
                                    calEndTime.setTimeInMillis(twilightState.getTodaySunset());
                            }
                            else {
                                if (inMorning)
                                    calEndTime.setTimeInMillis(twilightState.getTodaySunrise());
                                else
                                    calEndTime.setTimeInMillis(twilightState.getTomorrowSunrise());
                                if (now.compareTo(calEndTime) > 0)
                                    calEndTime.setTimeInMillis(twilightState.getTomorrowSunrise());
                            }
                            hoursEndTime.setTimeInMillis(calEndTime.getTimeInMillis());
                            hoursEndTime.set(Calendar.DAY_OF_MONTH, 0);
                            hoursEndTime.set(Calendar.MONTH, 0);
                            hoursEndTime.set(Calendar.YEAR, 0);
                            hoursEndTime.set(Calendar.SECOND, 0);
                            hoursEndTime.set(Calendar.MILLISECOND, 0);


                            /*
                            ///// set calendar for startTime and endTime
                            boolean previousDayUsed = false;
                            if (hoursStartTime.getTimeInMillis() >= hoursEndTime.getTimeInMillis()) {

                                if ((hoursNowTime.getTimeInMillis() >= midnightTime.getTimeInMillis()) &&
                                    (hoursNowTime.getTimeInMillis() <= hoursEndTime.getTimeInMillis())) {
                                    // now is between midnight and endTime

                                    startIndex = 0;

                                    // SunriseSunset get previous day when time is between 00:00 and 01:00
                                    //if (hoursNowTime.getTimeInMillis() >= midnightPlusOneTime.getTimeInMillis()) {
                                    //    calStartTime.add(Calendar.DAY_OF_YEAR, -1);
                                    //    previousDayUsed = true;
                                    //}
                                } else if ((hoursNowTime.getTimeInMillis() >= hoursStartTime.getTimeInMillis()) &&
                                        (hoursNowTime.getTimeInMillis() <= midnightMinusOneTime.getTimeInMillis())) {
                                    // now is between startTime and midnight

                                    //calEndTime.add(Calendar.DAY_OF_YEAR, 1);
                                } else {
                                    // now is before start time

                                    //calEndTime.add(Calendar.DAY_OF_YEAR, 1);
                                }
                            } else {

                                if (hoursNowTime.getTimeInMillis() > hoursEndTime.getTimeInMillis()) {
                                    // endTime is before actual time, compute for tomorrow

                                    calStartTime.add(Calendar.DAY_OF_YEAR, 1);
                                    //calEndTime.add(Calendar.DAY_OF_YEAR, 1);
                                }
                            }
                            */

                            //// get day by selected day of week
                            int startDayOfWeek = calStartTime.get(Calendar.DAY_OF_WEEK);
                            boolean[] daysOfWeek = new boolean[8];
                            daysOfWeek[Calendar.SUNDAY] = this._sunday;
                            daysOfWeek[Calendar.MONDAY] = this._monday;
                            daysOfWeek[Calendar.TUESDAY] = this._tuesday;
                            daysOfWeek[Calendar.WEDNESDAY] = this._wednesday;
                            daysOfWeek[Calendar.THURSDAY] = this._thursday;
                            daysOfWeek[Calendar.FRIDAY] = this._friday;
                            daysOfWeek[Calendar.SATURDAY] = this._saturday;

                            //noinspection StatementWithEmptyBody
                            if (daysOfWeek[startDayOfWeek]) {
                                // week for startTime is selected
                            } else {
                                // week for startTime is not selected,

                                // search for selected day of week
                                boolean found = false;
                                for (int i = startDayOfWeek; i < 8; i++) {
                                    if (daysOfWeek[i]) {
                                        startDayOfWeek = i;
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    for (int i = 1; i < startDayOfWeek; i++) {
                                        if (daysOfWeek[i]) {
                                            startDayOfWeek = i;
                                            //found = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            //////////////////////

                            long[] twilightDaysSunrise = twilightState.getDaysSunrise();
                            long[] twilightDaysSunset = twilightState.getDaysSunset();

                            if (_timeType == TIME_TYPE_SUNRISE_SUNSET) {

                                for (int daysIndex = 1; daysIndex < 9; daysIndex++) {

                                    int idx;

                                    //if (inOneHour)
                                    //    idx = daysIndex+1;
                                    //else
                                        idx = daysIndex;

                                    if (twilightDaysSunrise[idx] != -1) {

                                        calStartTime.setTimeInMillis(twilightDaysSunrise[idx]);

                                        //noinspection MagicConstant
                                        if (calStartTime.get(Calendar.DAY_OF_WEEK) == startDayOfWeek) {

                                            //if (inOneHour)
                                            //    idx = daysIndex+1;
                                            //else
                                            //noinspection ConstantConditions
                                            idx = daysIndex;

                                            if (twilightDaysSunset[idx] != -1)
                                                calEndTime.setTimeInMillis(twilightDaysSunset[idx]);
                                            else
                                                setAlarm = false;

                                            break;
                                        }
                                    }
                                    else
                                        setAlarm = false;
                                }
                            }
                            else {

                                for (int daysIndex = 1; daysIndex < 9; daysIndex++) {

                                    int idx;

                                    if (inMorning)
                                        idx = daysIndex - 1;
                                    else
                                        idx = daysIndex;

                                    if (twilightDaysSunset[idx] != -1) {

                                        calStartTime.setTimeInMillis(twilightDaysSunset[idx]);

                                        //noinspection MagicConstant
                                        if (calStartTime.get(Calendar.DAY_OF_WEEK) == startDayOfWeek) {

                                            if (inMorning)
                                                idx = daysIndex;
                                            else
                                                idx = daysIndex+1;

                                            if (twilightDaysSunrise[idx] != -1)
                                                calEndTime.setTimeInMillis(twilightDaysSunrise[idx]);
                                            else
                                                setAlarm = false;

                                            break;
                                        }
                                    }
                                    else
                                        setAlarm = false;
                                }
                            }
                            ////////////////////////////

                        }
                        else
                            setAlarm = false;
                    }
                }
            }
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
    void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        //removeAlarm(true, _context);
        removeAlarm(/*false, */context);

        if (!(isRunnable(context) && _enabled))
            return;

        long alarmTime = computeAlarm(true/*, context*/);
        if (alarmTime > 0)
            setAlarm(true, alarmTime, context);

        alarmTime = computeAlarm(false/*, context*/);
        if (alarmTime > 0)
            setAlarm(false, alarmTime, context);
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        //removeAlarm(true, _context);
        removeAlarm(/*false, */context);

        if (!(isRunnable(context) && _enabled))
            return;

        long alarmTime = computeAlarm(false/*, context*/);
        if (alarmTime > 0)
            setAlarm(false, alarmTime, context);

        alarmTime = computeAlarm(true/*, context*/);
        if (alarmTime > 0)
            setAlarm(true, alarmTime, context);
    }

    @Override
    void removeSystemEvent(Context context)
    {
        // remove alarms for state STOP

        //removeAlarm(true, _context);
        removeAlarm(/*false, */context);
    }

    private void removeAlarm(/*boolean startEvent, */Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, EventTimeBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_EVENT_TIME_BROADCAST_RECEIVER);
                //intent.setClass(context, EventPreferencesTime.class);


                try {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent);
                        pendingIntent.cancel();
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }

                try {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, -(int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent);
                        pendingIntent.cancel();
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        } catch (Exception ee) {
            PPApplicationStatic.recordException(ee);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_TIME_SENSOR_TAG_WORK+"_" + (int) _event._id);
    }

    private void setAlarm(boolean startEvent, long alarmTime, Context context)
    {
        boolean applicationUseAlarmClock = ApplicationPreferences.applicationUseAlarmClock;

        // not set alarm if alarmTime is over.
        Calendar now = Calendar.getInstance();
        if (applicationUseAlarmClock) {
            if (now.getTimeInMillis() > (alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET)) {
                return;
            }
        }
        else {
            if (now.getTimeInMillis() > (alarmTime + Event.EVENT_ALARM_TIME_OFFSET)) {
                return;
            }
        }

        int requestCode = (int)_event._id;
        if (!startEvent)
            requestCode = -(int)_event._id;

        //Intent intent = new Intent(context, EventTimeBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PhoneProfilesService.ACTION_EVENT_TIME_BROADCAST_RECEIVER);
        //intent.setClass(context, EventPreferencesTime.class);

        //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

        //int requestCode = (int)_event._id;
        //if (!startEvent)
        //    requestCode = -(int)_event._id;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (applicationUseAlarmClock) {
                Intent editorIntent = new Intent(context, EditorActivity.class);
                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            }
            else {
                //if (android.os.Build.VERSION.SDK_INT >= 23)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
            }
        }
    }

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesTime.PREF_EVENT_TIME_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventLocation(context, event, null)*/) {

                // compute start datetime
                long startAlarmTime;
                long endAlarmTime;

                startAlarmTime = computeAlarm(true/*, eventsHandler.context*/);
                endAlarmTime = computeAlarm(false/*, eventsHandler.context*/);

                Calendar now = Calendar.getInstance();
                long nowAlarmTime = now.getTimeInMillis();

                /*boolean[] daysOfWeek =  new boolean[8];
                daysOfWeek[Calendar.SUNDAY] = event._eventPreferencesTime._sunday;
                daysOfWeek[Calendar.MONDAY] = event._eventPreferencesTime._monday;
                daysOfWeek[Calendar.TUESDAY] = event._eventPreferencesTime._tuesday;
                daysOfWeek[Calendar.WEDNESDAY] = event._eventPreferencesTime._wednesday;
                daysOfWeek[Calendar.THURSDAY] = event._eventPreferencesTime._thursday;
                daysOfWeek[Calendar.FRIDAY] = event._eventPreferencesTime._friday;
                daysOfWeek[Calendar.SATURDAY] = event._eventPreferencesTime._saturday;*/

                //Calendar calStartTime = Calendar.getInstance();
                //calStartTime.setTimeInMillis(startAlarmTime);
                //int startDayOfWeek = calStartTime.get(Calendar.DAY_OF_WEEK);
                //if (daysOfWeek[startDayOfWeek])
                //{
                // startTime of week is selected
                if ((startAlarmTime > 0) && (endAlarmTime > 0))
                    eventsHandler.timePassed = ((nowAlarmTime >= startAlarmTime) && (nowAlarmTime < endAlarmTime));
                else
                    eventsHandler.timePassed = false;
                /*}
                else {
                    timePassed = false;
                }*/

                if (!eventsHandler.notAllowedTime) {
                    if (eventsHandler.timePassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedTime = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_TIME);
            }
        }

    }

}
