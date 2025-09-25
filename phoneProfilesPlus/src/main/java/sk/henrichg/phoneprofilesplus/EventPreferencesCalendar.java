package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.CalendarContract.Instances;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.CharacterStyle;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/** @noinspection ExtractMethodRecommender*/
class EventPreferencesCalendar extends EventPreferences {

    String _calendars;
    boolean _allEvents;
    int _searchField;
    String _searchString;
    int _availability;
    int _status;
    //boolean _ignoreAllDayEvents;
    int _startBeforeEvent;
    int _dayContainsEvent;
    int _allDayEvents;

    long _startTime;
    long _endTime;
    boolean _eventFound;
    boolean _eventTodayExists;

    static final String PREF_EVENT_CALENDAR_ENABLED = "eventCalendarEnabled";
    static final String PREF_EVENT_CALENDAR_CALENDARS = "eventCalendarCalendars";
    private static final String PREF_EVENT_CALENDAR_ALL_EVENTS = "eventCalendarAllEvents";
    private static final String PREF_EVENT_CALENDAR_SEARCH_FIELD = "eventCalendarSearchField";
    private static final String PREF_EVENT_CALENDAR_SEARCH_STRING = "eventCalendarSearchString";
    private static final String PREF_EVENT_CALENDAR_AVAILABILITY = "eventCalendarAvailability";
    //private static final String PREF_EVENT_CALENDAR_IGNORE_ALL_DAY_EVENTS = "eventCalendarIgnoreAllDayEvents";
    private static final String PREF_EVENT_CALENDAR_START_BEFORE_EVENT = "eventCalendarStartBeforeEvent";
    static final String PREF_EVENT_CALENDAR_APP_SETTINGS = "eventCalendarPeriodicScanningAppSettings";
    private static final String PREF_EVENT_CALENDAR_STATUS = "eventCalendarStatus";
    private static final String PREF_EVENT_CALENDAR_DAY_CONTAINS_EVENT = "eventCalendarDayContainsEvent";
    private static final String PREF_EVENT_CALENDAR_ALL_DAY_EVENTS = "eventCalendarAllDayEvents";

    static final String PREF_EVENT_CALENDAR_CATEGORY = "eventCalendarCategoryRoot";

    private static final int SEARCH_FIELD_TITLE = 0;
    private static final int SEARCH_FIELD_DESCRIPTION = 1;
    private static final int SEARCH_FIELD_LOCATION = 2;

    //private static final int AVAILABILITY_NO_CHECK = 0;
    private static final int AVAILABILITY_BUSY = 1;
    private static final int AVAILABILITY_FREE = 2;
    private static final int AVAILABILITY_TENTATIVE = 3;

    //private static final int STATUS_NO_CHECK = 0;
    private static final int STATUS_CONFIRMED = 1;
    private static final int STATUS_TENTATIVE = 2;
    private static final int STATUS_CANCELED = 3;

    EventPreferencesCalendar(Event event,
                                boolean enabled,
                                String calendars,
                                boolean allEvents,
                                int searchField,
                                String searchString,
                                int availability,
                                int status,
                                //boolean ignoreAllDayEvents,
                                int startBeforeEvent,
                                int dayContainsEvent,
                                int allDayEvents)
    {
        super(event, enabled);

        this._calendars = calendars;
        this._allEvents = allEvents;
        this._searchField = searchField;
        this._searchString = searchString;
        this._availability = availability;
        this._status = status;
        //this._ignoreAllDayEvents = ignoreAllDayEvents;
        this._startBeforeEvent = startBeforeEvent;
        this._dayContainsEvent = dayContainsEvent;
        this._allDayEvents = allDayEvents;

        this._startTime = 0;
        this._endTime = 0;
        this._eventFound = false;
        this._eventTodayExists = false;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesCalendar._enabled;
        this._calendars = fromEvent._eventPreferencesCalendar._calendars;
        this._allEvents = fromEvent._eventPreferencesCalendar._allEvents;
        this._searchField = fromEvent._eventPreferencesCalendar._searchField;
        this._searchString = fromEvent._eventPreferencesCalendar._searchString;
        this._availability = fromEvent._eventPreferencesCalendar._availability;
        this._status = fromEvent._eventPreferencesCalendar._status;
        //this._ignoreAllDayEvents = fromEvent._eventPreferencesCalendar._ignoreAllDayEvents;
        this._startBeforeEvent = fromEvent._eventPreferencesCalendar._startBeforeEvent;
        this._dayContainsEvent = fromEvent._eventPreferencesCalendar._dayContainsEvent;
        this._allDayEvents = fromEvent._eventPreferencesCalendar._allDayEvents;

        this.setSensorPassed(fromEvent._eventPreferencesCalendar.getSensorPassed());

        this._startTime = 0;
        this._endTime = 0;
        this._eventFound = false;
        this._eventTodayExists = false;
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_CALENDAR_ENABLED, _enabled);
        editor.putString(PREF_EVENT_CALENDAR_CALENDARS, _calendars);
        editor.putBoolean(PREF_EVENT_CALENDAR_ALL_EVENTS, _allEvents);
        editor.putString(PREF_EVENT_CALENDAR_SEARCH_FIELD, String.valueOf(_searchField));
        editor.putString(PREF_EVENT_CALENDAR_SEARCH_STRING, _searchString);
        editor.putString(PREF_EVENT_CALENDAR_AVAILABILITY, String.valueOf(_availability));
        editor.putString(PREF_EVENT_CALENDAR_STATUS, String.valueOf(_status));
        //editor.putBoolean(PREF_EVENT_CALENDAR_IGNORE_ALL_DAY_EVENTS, _ignoreAllDayEvents);
        editor.putString(PREF_EVENT_CALENDAR_START_BEFORE_EVENT, Integer.toString(_startBeforeEvent));
        editor.putString(PREF_EVENT_CALENDAR_DAY_CONTAINS_EVENT, Integer.toString(_dayContainsEvent));
        editor.putString(PREF_EVENT_CALENDAR_ALL_DAY_EVENTS, Integer.toString(_allDayEvents));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_CALENDAR_ENABLED, false);
        this._calendars = preferences.getString(PREF_EVENT_CALENDAR_CALENDARS, "");
        this._allEvents = preferences.getBoolean(PREF_EVENT_CALENDAR_ALL_EVENTS, false);
        this._searchField = Integer.parseInt(preferences.getString(PREF_EVENT_CALENDAR_SEARCH_FIELD, "0"));
        this._searchString = preferences.getString(PREF_EVENT_CALENDAR_SEARCH_STRING, "");
        this._availability = Integer.parseInt(preferences.getString(PREF_EVENT_CALENDAR_AVAILABILITY, "0"));
        this._status = Integer.parseInt(preferences.getString(PREF_EVENT_CALENDAR_STATUS, "0"));
        //this._ignoreAllDayEvents = preferences.getBoolean(PREF_EVENT_CALENDAR_IGNORE_ALL_DAY_EVENTS, false);
        this._startBeforeEvent = Integer.parseInt(preferences.getString(PREF_EVENT_CALENDAR_START_BEFORE_EVENT, "0"));
        this._dayContainsEvent = Integer.parseInt(preferences.getString(PREF_EVENT_CALENDAR_DAY_CONTAINS_EVENT, "0"));
        this._allDayEvents = Integer.parseInt(preferences.getString(PREF_EVENT_CALENDAR_ALL_DAY_EVENTS, "0"));

        this._startTime = 0;
        this._endTime = 0;
        this._eventFound = false;
        this._eventTodayExists = false;
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_calendar_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_CALENDAR_ENABLED, false, context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_calendar), addPassStatus, DatabaseHandler.ETYPE_CALENDAR, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                _value.append(context.getString(R.string.event_preferences_calendar_calendars)).append(StringConstants.STR_COLON_WITH_SPACE);
                _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(CalendarsMultiSelectDialogPreference.getSummary(_calendars, context), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                if (this._dayContainsEvent > 0) {
                    _value.append(context.getString(R.string.event_preferences_calendar_day_contains_event)).append(StringConstants.STR_COLON_WITH_SPACE);
                    String[] dayContainsEventArray = context.getResources().getStringArray(R.array.eventCalendarDayContainsEventArray);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(dayContainsEventArray[this._dayContainsEvent], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);
                }
                if (this._allEvents) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.event_preferences_calendar_all_events), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);
                } else {
                    _value.append(context.getString(R.string.event_preferences_calendar_search_field)).append(StringConstants.STR_COLON_WITH_SPACE);
                    String[] searchFields = context.getResources().getStringArray(R.array.eventCalendarSearchFieldArray);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(searchFields[this._searchField], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                    _value.append(context.getString(R.string.event_preferences_calendar_search_string)).append(StringConstants.STR_COLON_WITH_SPACE);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue("\"" + this._searchString + "\"", disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);
                }

                //if (this._ignoreAllDayEvents)
                //    descr = descr + "<b>" + getColorForChangedPreferenceValue(context.getString(R.string.event_preferences_calendar_ignore_all_day_events, disabled, addBullet, context)) + "</b> • ";
                _value.append(context.getString(R.string.event_preferences_calendar_all_day_events)).append(StringConstants.STR_COLON_WITH_SPACE);
                String[] dayContainsEventArray = context.getResources().getStringArray(R.array.eventCalendarAllDayEventsArray);
                _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(dayContainsEventArray[this._allDayEvents], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);


                String[] availabilities = context.getResources().getStringArray(R.array.eventCalendarAvailabilityArray);
                _value.append(context.getString(R.string.event_preference_calendar_availability)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(availabilities[this._availability], disabled, addBullet, context)).append(StringConstants.STR_BULLET).append(StringConstants.TAG_BOLD_END_HTML);

                String[] statuses = context.getResources().getStringArray(R.array.eventCalendarStatusArray);
                _value.append(context.getString(R.string.event_preference_calendar_status)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(statuses[this._status], disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);

                if (this._startBeforeEvent > 0)
                    _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_calendar_start_before_event)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(this._startBeforeEvent), disabled, addBullet, context)).append(StringConstants.TAG_BOLD_END_HTML);

                if (addBullet) {
                    if (EventStatic.getGlobalEventsRunning(context)) {
                        if (_eventFound) {
                            long alarmTime;
                            //SimpleDateFormat sdf = new SimpleDateFormat("EEd/MM/yy HH:mm");
                            /*if (_dayContainsEvent == 0) {
                                String alarmTimeS;
                                if (_event.getStatus() == Event.ESTATUS_PAUSE) {
                                    alarmTime = computeAlarm(true);
                                    // date and time format by user system settings configuration
                                    alarmTimeS = "(st) " + DateFormat.getDateFormat(context).format(alarmTime) +
                                            " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                    descr = descr + "<br>"; //'\n';
                                    descr = descr + "&nbsp;&nbsp;&nbsp;-> " + alarmTimeS;
                                } else if (_event.getStatus() == Event.ESTATUS_RUNNING) {
                                    alarmTime = computeAlarm(false);
                                    // date and time format by user system settings configuration
                                    alarmTimeS = "(et) " + DateFormat.getDateFormat(context).format(alarmTime) +
                                            " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                    descr = descr + "<br>"; //'\n';
                                    descr = descr + "&nbsp;&nbsp;&nbsp;-> " + alarmTimeS;
                                }
                            }
                            else {*/
                                String alarmTimeS;
                                alarmTime = computeAlarm(true);
                                // date and time format by user system settings configuration
                                alarmTimeS = StringConstants.EVENT_START_TIME+" " + DateFormat.getDateFormat(context).format(alarmTime) +
                                        " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                _value.append(StringConstants.TAG_BREAK_HTML);
                                _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append("-> ").append(alarmTimeS);

                                alarmTime = computeAlarm(false);
                                // date and time format by user system settings configuration
                                alarmTimeS = StringConstants.EVENT_END_TIME + " " + DateFormat.getDateFormat(context).format(alarmTime) +
                                        " " + DateFormat.getTimeFormat(context).format(alarmTime);
                                _value.append(StringConstants.TAG_BREAK_HTML);
                                _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append("-> ").append(alarmTimeS);
                            //}
                        } else {
                            _value.append(StringConstants.TAG_BREAK_HTML);
                            _value.append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append(StringConstants.CHAR_HARD_SPACE_HTML).append("-> ").append(context.getString(R.string.event_preferences_calendar_no_event));
                        }
                    }
                }
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_CALENDAR_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_CALENDAR_ENABLED) ||
            key.equals(PREF_EVENT_CALENDAR_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_CALENDAR_APP_SETTINGS);
            if (preference != null) {
                String summary;
                //int titleColor;
                if (!ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) {
                    //if (!ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile) {
                    summary = context.getString(R.string.array_pref_applicationDisableScanning_disabled) + StringConstants.STR_SEPARATOR_WITH_DOT +
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
                        summary = context.getString(R.string.phone_profiles_pref_applicationEventScanningPaused) + StringConstants.STR_SEPARATOR_WITH_DOT +
                                context.getString(R.string.phone_profiles_pref_eventBackgroundScanningAppSettings_summary);
                    } else {
                        summary = context.getString(R.string.array_pref_applicationDisableScanning_enabled) + StringConstants.STR_SEPARATOR_WITH_DOT +
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

        if (key.equals(PREF_EVENT_CALENDAR_SEARCH_FIELD) ||
            key.equals(PREF_EVENT_CALENDAR_AVAILABILITY) ||
            key.equals(PREF_EVENT_CALENDAR_STATUS) ||
            key.equals(PREF_EVENT_CALENDAR_DAY_CONTAINS_EVENT) ||
            key.equals(PREF_EVENT_CALENDAR_ALL_DAY_EVENTS))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }
        /*if (key.equals(PREF_EVENT_CALENDAR_IGNORE_ALL_DAY_EVENTS)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }*/
        if (key.equals(PREF_EVENT_CALENDAR_AVAILABILITY)) {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_CALENDAR_STATUS)) {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_CALENDAR_START_BEFORE_EVENT)) {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 0;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 0, false, false, false, false);
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesCalendar.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesCalendar.isRunnable(context);
        //boolean isAllConfigured = event._eventPreferencesCalendar.isAllConfigured(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_CALENDAR_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_CALENDAR_CALENDARS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_CALENDAR_CALENDARS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !bold, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_CALENDAR_ALL_EVENTS);
        if (preference != null) {
            boolean bold = prefMng.getSharedPreferences().getBoolean(PREF_EVENT_CALENDAR_ALL_EVENTS, false);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        boolean allEventsNotChecked = !preferences.getBoolean(PREF_EVENT_CALENDAR_ALL_EVENTS, false);
        enabled = enabled && allEventsNotChecked;
        preference = prefMng.findPreference(PREF_EVENT_CALENDAR_SEARCH_FIELD);
        if (preference != null) {
            preference.setEnabled(allEventsNotChecked);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, true, false, false, false, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_CALENDAR_SEARCH_STRING);
        if (preference != null) {
            preference.setEnabled(allEventsNotChecked);
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_CALENDAR_SEARCH_STRING, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        //preference = prefMng.findPreference(PREF_EVENT_CALENDAR_DAY_CONTAINS_EVENT);
        //if (preference != null) {
        //    boolean bold = Integer.parseInt(prefMng.getSharedPreferences().getString(PREF_EVENT_CALENDAR_DAY_CONTAINS_EVENT, "0")) > 0;
        //    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, !isRunnable, false);
        //}
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_CALENDAR_ENABLED) ||
            key.equals(PREF_EVENT_CALENDAR_ALL_EVENTS)/* ||
            key.equals(PREF_EVENT_CALENDAR_IGNORE_ALL_DAY_EVENTS)*/) {
            boolean value = preferences.getBoolean(key, false);
            String sValue = StringConstants.FALSE_STRING;
            if (value) sValue = StringConstants.TRUE_STRING;
            setSummary(prefMng, key, sValue, context);
        }
        if (key.equals(PREF_EVENT_CALENDAR_CALENDARS) ||
            key.equals(PREF_EVENT_CALENDAR_SEARCH_FIELD) ||
            key.equals(PREF_EVENT_CALENDAR_SEARCH_STRING) ||
            key.equals(PREF_EVENT_CALENDAR_AVAILABILITY) ||
            key.equals(PREF_EVENT_CALENDAR_STATUS) ||
            key.equals(PREF_EVENT_CALENDAR_START_BEFORE_EVENT) ||
            key.equals(PREF_EVENT_CALENDAR_APP_SETTINGS) ||
            key.equals(PREF_EVENT_CALENDAR_DAY_CONTAINS_EVENT) ||
            key.equals(PREF_EVENT_CALENDAR_ALL_DAY_EVENTS))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_CALENDAR_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALENDAR_CALENDARS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALENDAR_SEARCH_FIELD, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALENDAR_SEARCH_STRING, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALENDAR_AVAILABILITY, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALENDAR_STATUS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALENDAR_START_BEFORE_EVENT, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALENDAR_ALL_EVENTS, preferences, context);
        //setSummary(prefMng, PREF_EVENT_CALENDAR_IGNORE_ALL_DAY_EVENTS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALENDAR_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALENDAR_DAY_CONTAINS_EVENT, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALENDAR_ALL_DAY_EVENTS, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_CALENDAR_ENABLED, false, context);
        if (preferenceAllowed.preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesCalendar tmp = new EventPreferencesCalendar(this._event, this._enabled, this._calendars, this._allEvents,
                    this._searchField, this._searchString, this._availability, this._status, /*this._ignoreAllDayEvents,*/ this._startBeforeEvent,
                    this._dayContainsEvent, this._allDayEvents);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_CALENDAR_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_CALENDAR_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_CALENDAR).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && tmp.isAllConfigured(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_CALENDAR_CATEGORY);
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

        runnable = runnable && (!_calendars.isEmpty());
        //if (_dayContainsEvent > 0)
        //    runnable = runnable && (_allEvents || (!_searchString.isEmpty()));
        //else
        //if (_dayContainsEvent == 2)
        //    runnable = runnable && (_allEvents || (!_searchString.isEmpty()));
        //else
            runnable = runnable && (_allEvents || (!_searchString.isEmpty()));

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_CALENDAR_ENABLED) != null) {
                setSummary(prefMng, PREF_EVENT_CALENDAR_APP_SETTINGS, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    private long computeAlarm(boolean startEvent)
    {
        if ((_startTime == 0) || (_endTime == 0))
            return 0;

        ///// set calendar for startTime and endTime
        Calendar calStartTime = Calendar.getInstance();
        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        calStartTime.setTimeInMillis(_startTime - gmtOffset);
        calStartTime.set(Calendar.SECOND, -_startBeforeEvent);
        calStartTime.set(Calendar.MILLISECOND, 0);

        calEndTime.setTimeInMillis(_endTime - gmtOffset);
        calEndTime.set(Calendar.SECOND, 0);
        calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        if (startEvent)
            alarmTime = calStartTime.getTimeInMillis();
        else
            alarmTime = calEndTime.getTimeInMillis();

        return alarmTime;
    }

    @Override
    void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        //removeAlarm(true, _context);
        removeAlarm(/*false, */context);

        //searchEvent(context);

        if (!(isRunnable(context) && isAllConfigured(context) && _enabled))
            return;

        setAlarm(/*true,*/ 0, context, true);

        //if (!_eventFound)
        //    return;

        setAlarm(/*true,*/ computeAlarm(true), context, false);
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        //removeAlarm(true, _context);
        removeAlarm(/*false, */context);

        //searchEvent(context);

        if (!(isRunnable(context) && isAllConfigured(context) && _enabled))
            return;

        setAlarm(/*false,*/ 0, context, true);

        //if (!_eventFound)
        //    return;

        setAlarm(/*false,*/ computeAlarm(false), context, false);
    }

    @Override
    void removeSystemEvent(Context context)
    {
        // remove alarms for state STOP

        //removeAlarm(true, _context);
        removeAlarm(/*false, */context);

        _eventFound = false;
        _eventTodayExists = false;

    }

    private void removeAlarm(/*boolean startEvent, */Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, CalendarEventExistsCheckBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_CALENDAR_EVENT_EXISTS_CHECK_BROADCAST_RECEIVER);
                //intent.setClass(context, CalendarEventExistsCheckBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }

                //Intent intent = new Intent(context, EventCalendarBroadcastReceiver.class);
                intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_EVENT_CALENDAR_BROADCAST_RECEIVER);
                //intent.setClass(context, EventCalendarBroadcastReceiver.class);

                pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }

        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_CALENDAR_SENSOR_TAG_WORK+"_" + (int) _event._id);
    }

    void setAlarm(/*boolean startEvent,*/ long alarmTime, Context context, boolean forExistCheck)
    {
        boolean applicationUseAlarmClock = ApplicationPreferences.applicationUseAlarmClock;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (forExistCheck) {
            //Intent intent = new Intent(context, CalendarEventExistsCheckBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_CALENDAR_EVENT_EXISTS_CHECK_BROADCAST_RECEIVER);
            //intent.setClass(context, CalendarEventExistsCheckBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar _alarmTime = Calendar.getInstance();

            int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

            //if (DebugVersion.enabled) {
            //    _alarmTime.add(Calendar.MINUTE, 1);
            //} else {
                _alarmTime.set(Calendar.HOUR_OF_DAY, 0);
                _alarmTime.set(Calendar.MINUTE, 0);
                _alarmTime.set(Calendar.SECOND, 1);
                _alarmTime.set(Calendar.MILLISECOND, 0);
                _alarmTime.add(Calendar.DAY_OF_YEAR, 1);
            //}

            if (alarmManager != null) {
                if (applicationUseAlarmClock) {
                    Intent editorIntent = new Intent(context, EditorActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(_alarmTime.getTimeInMillis() - gmtOffset + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                } else {
                    //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, _alarmTime.getTimeInMillis() - gmtOffset + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    // must be used SystemClock.elapsedRealtime() because of AlarmManager.ELAPSED_REALTIME_WAKEUP
                    Calendar now = Calendar.getInstance();
                    long duration = _alarmTime.getTimeInMillis() - now.getTimeInMillis();
                    long __alarmTime = SystemClock.elapsedRealtime() + duration;
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, __alarmTime - gmtOffset + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                }
            }

            return;
        }


        if (alarmTime == 0)
            return;

        // not set alarm if alarmTime is over.
        Calendar now = Calendar.getInstance();
        if (applicationUseAlarmClock) {
            if (now.getTimeInMillis() > (alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET))
                return;
        }
        else {
            if (now.getTimeInMillis() > (alarmTime + Event.EVENT_ALARM_TIME_OFFSET))
                return;
        }

        //Intent intent = new Intent(context, EventCalendarBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PhoneProfilesService.ACTION_EVENT_CALENDAR_BROADCAST_RECEIVER);
        //intent.setClass(context, EventCalendarBroadcastReceiver.class);

        //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmManager != null) {
            if (applicationUseAlarmClock) {
                Intent editorIntent = new Intent(context, EditorActivity.class);
                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            }
            else {
                //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                // must be used SystemClock.elapsedRealtime() because of AlarmManager.ELAPSED_REALTIME_WAKEUP
                long duration = alarmTime - now.getTimeInMillis();
                alarmTime = SystemClock.elapsedRealtime() + duration;
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
            }
        }
    }

    //private void searchEvent(Context context)
    @SuppressLint("Recycle")
    void saveStartEndTime(DataWrapper dataWrapper)
    {
        if (!(/*isRunnable(context) && _enabled &&*/ Permissions.checkCalendar(dataWrapper.context)))
        {
            _startTime = 0;
            _endTime = 0;
            _eventFound = false;
            DatabaseHandler.getInstance(dataWrapper.context).updateEventCalendarTimes(_event);
            return;
        }

        final String[] INSTANCE_PROJECTION = new String[] {
                Instances.BEGIN,           // 0
                Instances.END,			   // 1
                Instances.TITLE,           // 2
                Instances.DESCRIPTION,     // 3
                Instances.CALENDAR_ID,     // 4
                Instances.ALL_DAY,         // 5
                Instances.EVENT_LOCATION,  // 6
                Instances.AVAILABILITY,    // 7
                Instances.EVENT_ID,        // 8
                Instances.STATUS           // 9
                /*Instances.EVENT_TIMEZONE    10 */
        };

        // The indices for the projection array above.
        final int PROJECTION_BEGIN_INDEX = 0;
        final int PROJECTION_END_INDEX = 1;
        //final int PROJECTION_TITLE_INDEX = 2;
        //final int PROJECTION_DESCRIPTION_INDEX = 3;
        final int PROJECTION_CALENDAR_ID_INDEX = 4;
        final int PROJECTION_ALL_DAY_INDEX = 5;
        //final int PROJECTION_EVENT_TIMEZONE_INDEX = 9;
        //final int PROJECTION_EVENT_ID = 8;

        Cursor cur = null;
        ContentResolver cr = dataWrapper.context.getContentResolver();

        StringBuilder selection = new StringBuilder("(");

        List<String> selectionArgs = new ArrayList<>();
        if (!_allEvents) {
            String[] searchStringSplits = _searchString.split(StringConstants.STR_SPLIT_REGEX);
            //int argsId = 0;

            // positive strings
            boolean positiveExists = false;
            for (String split : searchStringSplits) {
                if (!split.isEmpty()) {
                    String searchPattern = split;

                    if (searchPattern.startsWith("!")) {
                        // only positive
                        continue;
                    }

                    // trim leading and trailing spaces
                    searchPattern = searchPattern.trim();

                    if (!positiveExists)
                        selection.append("(");

                    // when in searchPattern are not wildcards add %
                    if (!(searchPattern.contains("%") || searchPattern.contains("_")))
                        searchPattern = "%" + searchPattern + "%";

                    if (!searchPattern.equals("%"))
                        selectionArgs.add(searchPattern);

                    if (positiveExists)
                        selection.append(" OR ");

                    switch (_searchField) {
                        case SEARCH_FIELD_TITLE:
                            if (searchPattern.equals("%"))
                                selection.append("((" + Instances.TITLE + " IS NOT NULL) AND (" + Instances.TITLE + " <> \"\"))");
                            else
                                selection.append("(lower(" + Instances.TITLE + ")" + " LIKE lower(?) ESCAPE '\\')");
                            break;
                        case SEARCH_FIELD_DESCRIPTION:
                            if (searchPattern.equals("%"))
                                selection.append("((" + Instances.DESCRIPTION + " IS NOT NULL) AND (" + Instances.DESCRIPTION + " <> \"\"))");
                            else
                                selection.append("(lower(" + Instances.DESCRIPTION + ")" + " LIKE lower(?) ESCAPE '\\')");
                            break;
                        case SEARCH_FIELD_LOCATION:
                            if (searchPattern.equals("%"))
                                selection.append("((" + Instances.EVENT_LOCATION + " IS NOT NULL) AND (" + Instances.EVENT_LOCATION + " <> \"\"))");
                            else
                                selection.append("(lower(" + Instances.EVENT_LOCATION + ")" + " LIKE lower(?) ESCAPE '\\')");
                            break;
                    }

                    positiveExists = true;

                    //++argsId;
                }
            }
            if (positiveExists)
                selection.append(")");

            // negative strings
            boolean negativeExists = false;
            for (String split : searchStringSplits) {
                if (!split.isEmpty()) {
                    String searchPattern = split;

                    if (!searchPattern.startsWith("!")) {
                        // only negative
                        continue;
                    }

                    if (!negativeExists) {
                        if (positiveExists)
                            selection.append(" AND (");
                        else
                            selection.append("(");
                    }

                    // remove !
                    searchPattern = searchPattern.substring(1);

                    // trim leading and trailing spaces
                    searchPattern = searchPattern.trim();

                    // when in searchPattern are not wildcards add %
                    if (!(searchPattern.contains("%") || searchPattern.contains("_")))
                        searchPattern = "%" + searchPattern + "%";

                    if (!searchPattern.equals("%"))
                        selectionArgs.add(searchPattern);

                    if (negativeExists)
                        selection.append(" AND ");

                    switch (_searchField) {
                        case SEARCH_FIELD_TITLE:
                            if (searchPattern.equals("%"))
                                selection.append("((" + Instances.TITLE + " IS NULL) OR (" + Instances.TITLE + " = \"\"))");
                            else
                                selection.append("(lower(" + Instances.TITLE + ")" + " NOT LIKE lower(?) ESCAPE '\\')");
                            break;
                        case SEARCH_FIELD_DESCRIPTION:
                            if (searchPattern.equals("%"))
                                selection.append("((" + Instances.DESCRIPTION + " IS NULL) OR (" + Instances.DESCRIPTION + " = \"\"))");
                            else
                                selection.append("(lower(" + Instances.DESCRIPTION + ")" + " NOT LIKE lower(?) ESCAPE '\\')");
                            break;
                        case SEARCH_FIELD_LOCATION:
                            if (searchPattern.equals("%"))
                                selection.append("((" + Instances.EVENT_LOCATION + " IS NULL) OR (" + Instances.EVENT_LOCATION + " = \"\"))");
                            else
                                selection.append("(lower(" + Instances.EVENT_LOCATION + ")" + " NOT LIKE lower(?) ESCAPE '\\')");
                            break;
                    }

                    negativeExists = true;

                    //if (!searchPattern.equals("%"))
                    //    ++argsId;
                }
            }
            if (negativeExists)
                selection.append(")");
        }
        else {
            selection.append("(1 = 1)");
        }

        switch (_availability) {
            case AVAILABILITY_BUSY:
                selection.append(" AND (" + Instances.AVAILABILITY + "=" + Instances.AVAILABILITY_BUSY + ")");
                break;
            case AVAILABILITY_FREE:
                selection.append(" AND (" + Instances.AVAILABILITY + "=" + Instances.AVAILABILITY_FREE + ")");
                break;
            case AVAILABILITY_TENTATIVE:
                selection.append(" AND (" + Instances.AVAILABILITY + "=" + Instances.AVAILABILITY_TENTATIVE + ")");
                //selection.append(" AND (" + Instances.STATUS + "=" + Instances.STATUS_TENTATIVE + ")");
                break;
        }

        switch (_status) {
            case STATUS_CONFIRMED:
                selection.append(" AND (" + Instances.STATUS + "=" + Instances.STATUS_CONFIRMED + ")");
                break;
            case STATUS_TENTATIVE:
                selection.append(" AND (" + Instances.STATUS + "=" + Instances.STATUS_TENTATIVE + ")");
                break;
            case STATUS_CANCELED:
                selection.append(" AND (" + Instances.STATUS + "=" + Instances.STATUS_CANCELED + ")");
                break;
        }

        selection.append(")");

        // Construct the query with the desired date range.
        long startMillis;
        long endMillis;
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        //if (_dayContainsEvent == 0) {
            // search now - 1 day .. now + 31 days
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            startMillis = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_YEAR, 32);
            endMillis = calendar.getTimeInMillis();
        /*}
        else {
            // search now at 00:00:00 .. now at 00:00:00 + 1 day
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            startMillis = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            endMillis = calendar.getTimeInMillis();
        }*/
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        _eventFound = false;
        _startTime = 0;
        _endTime = 0;

        String[] calendarsSplits = _calendars.split(StringConstants.STR_SPLIT_REGEX);

        // Submit the query
        boolean ok = true;
        try {
            if (selectionArgs.isEmpty())
                cur = cr.query(builder.build(), INSTANCE_PROJECTION, selection.toString(), null, Instances.BEGIN + " ASC");
            else {
                String[] _selectionArgs = new String[selectionArgs.size()];
                int idx = 0;
                for (String arg : selectionArgs) {
                    _selectionArgs[idx++] = arg;
                }
                cur = cr.query(builder.build(), INSTANCE_PROJECTION, selection.toString(), _selectionArgs, Instances.BEGIN + " ASC");
            }
        } catch (SecurityException e) {
            //Log.e("EventPreferencesCalendar.saveStartEndTime", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);
            //if ((cur != null) && (!cur.isClosed()))
            //    cur.close();
            ok  = false;
        } catch (Exception e) {
            //Log.e("EventPreferencesCalendar.saveStartEndTime", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            //if ((cur != null) && (!cur.isClosed()))
            //    cur.close();
            ok = false;
        }

        if (ok) {
            if (cur != null) {
                while (cur.moveToNext()) {

                    boolean calendarFound = false;
                    for (String split : calendarsSplits) {
                        long calendarId = Long.parseLong(split);
                        if (cur.getLong(PROJECTION_CALENDAR_ID_INDEX) == calendarId) {
                            calendarFound = true;
                        }
                    }
                    if (!calendarFound)
                        continue;

                    //if ((cur.getInt(PROJECTION_ALL_DAY_INDEX) == 1) && this._ignoreAllDayEvents)
                    //    continue;
                    int allDays = cur.getInt(PROJECTION_ALL_DAY_INDEX);
                    if ((this._allDayEvents == 1) && (allDays == 1))
                        // required is only not all days event
                        continue;
                    if ((this._allDayEvents == 2) && (allDays != 1))
                        // required is only all days event
                        continue;

                    long beginVal;
                    long endVal;
                    //String title = null;

                    // Get the field values
                    beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
                    endVal = cur.getLong(PROJECTION_END_INDEX);

                    if (allDays == 1) {
                        // get UTC offset
                        Date _now = new Date();
                        int utcOffset = TimeZone.getDefault().getOffset(_now.getTime());

                        beginVal -= utcOffset;
                        endVal -= utcOffset;
                    }

                    //title = cur.getString(PROJECTION_TITLE_INDEX);

                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

                    if ((beginVal <= now) && (endVal > now)) {
                        // event instance is found - actual instance
                        _eventFound = true;
                        _startTime = beginVal + gmtOffset;
                        _endTime = endVal + gmtOffset;
                        break;
                    } else if (beginVal > now) {
                        // event instance is found - future instance
                        _eventFound = true;
                        _startTime = beginVal + gmtOffset;
                        _endTime = endVal + gmtOffset;
                        break;
                    }
                }
            }
        }

        if ((cur != null) && (!cur.isClosed()))
            cur.close();


        DatabaseHandler.getInstance(dataWrapper.context).updateEventCalendarTimes(_event);

        if (_event.getStatus() == Event.ESTATUS_RUNNING)
            _event._eventPreferencesCalendar.setSystemEventForPause(dataWrapper.context);
        if (_event.getStatus() == Event.ESTATUS_PAUSE)
            _event._eventPreferencesCalendar.setSystemEventForStart(dataWrapper.context);
    }

    void saveCalendarEventExists(DataWrapper dataWrapper) {
        if (!(/*isRunnable(context) && _enabled &&*/ Permissions.checkCalendar(dataWrapper.context)))
        {
            _eventTodayExists = false;
            DatabaseHandler.getInstance(dataWrapper.context).updateEventCalendarTodayExists(_event);
            return;
        }

        final String[] INSTANCE_PROJECTION = new String[] {
                Instances.CALENDAR_ID,     // 0
                Instances.ALL_DAY,         // 1
                Instances.BEGIN,           // 2
                Instances.END			   // 3
                //Instances.CALENDAR_DISPLAY_NAME // 4
        };

        // The indices for the projection array above.
        final int PROJECTION_CALENDAR_ID_INDEX = 0;
        final int PROJECTION_ALL_DAY_INDEX = 1;
        final int PROJECTION_BEGIN_INDEX = 2;
        final int PROJECTION_END_INDEX = 3;
        //final int PROJECTION_CALENDAR_DISPLAY_NAME_INDEX = 4;

        Cursor cur;
        ContentResolver cr = dataWrapper.context.getContentResolver();

        // Construct the query with the desired date range.
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startMillis = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        long endMillis = calendar.getTimeInMillis() - 1;

        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        String[] calendarsSplits = _calendars.split(StringConstants.STR_SPLIT_REGEX);

        // Submit the query
        try {
            cur = cr.query(builder.build(), INSTANCE_PROJECTION, null, null, null);
        } catch (SecurityException e) {
            //Log.e("EventPreferencesCalendar.saveCalendarEventExists", Log.getStackTraceString(e));
            //PPApplicationStatic.recordException(e);
            cur = null;
        } catch (Exception e) {
            //Log.e("EventPreferencesCalendar.saveCalendarEventExists", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            cur = null;
        }

        if (cur != null)
        {
            _eventTodayExists = false;
            while (cur.moveToNext()) {
                boolean calendarFound = false;
                for (String split : calendarsSplits) {
                    long calendarId = Long.parseLong(split);
                    if (cur.getLong(PROJECTION_CALENDAR_ID_INDEX) == calendarId) {
                        calendarFound = true;
                    }
                }
                if (!calendarFound)
                    continue;

                long beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
                long endVal = cur.getLong(PROJECTION_END_INDEX);

                if (cur.getInt(PROJECTION_ALL_DAY_INDEX) == 1) {
                    // get UTC offset
                    Date _now = new Date();
                    int utcOffset = TimeZone.getDefault().getOffset(_now.getTime());

                    beginVal -= utcOffset;
                    endVal -= utcOffset;
                }

                //int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

                if ((beginVal <= now) && (endVal > now)) {
                    // event instance is found - actual instance
                    _eventTodayExists = true;
                    break;
                }
            }

            cur.close();
        }

        DatabaseHandler.getInstance(dataWrapper.context).updateEventCalendarTodayExists(_event);

        /*
        if (_event.getStatus() == Event.ESTATUS_RUNNING)
            _event._eventPreferencesCalendar.setSystemEventForPause(dataWrapper.context);
        if (_event.getStatus() == Event.ESTATUS_PAUSE)
            _event._eventPreferencesCalendar.setSystemEventForStart(dataWrapper.context);
        */
    }

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, false, eventsHandler.context).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorActivity.displayRedTextToPreferencesNotification()
                /*&& (Permissions.checkEventCalendar(context, event, null))*/) {

                // compute start datetime
                long startAlarmTime;
                long endAlarmTime;

                if (_eventFound) {
                    startAlarmTime = computeAlarm(true);

                    endAlarmTime = computeAlarm(false);

                    Calendar now = Calendar.getInstance();
                    long nowAlarmTime = now.getTimeInMillis();

                    eventsHandler.calendarPassed = ((nowAlarmTime >= startAlarmTime) && (nowAlarmTime < endAlarmTime));
                } else
                    eventsHandler.calendarPassed = false;

                if (_dayContainsEvent == 1)
                    eventsHandler.calendarPassed = eventsHandler.calendarPassed && _eventTodayExists;
                if (_dayContainsEvent == 2)
                    eventsHandler.calendarPassed = eventsHandler.calendarPassed || (!_eventTodayExists);

                if (!eventsHandler.notAllowedCalendar) {
                    if (eventsHandler.calendarPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedCalendar = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_CALENDAR);
            }
        }
    }

}
