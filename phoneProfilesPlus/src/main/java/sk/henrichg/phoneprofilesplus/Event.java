package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

class Event {

    long _id;
    String _name;
    int _startOrder;
    long _fkProfileStart;
    long _fkProfileEnd;
    //public boolean _undoneProfile;
    int _atEndDo;
    private int _status;
    String _notificationSoundStart;
    boolean _notificationVibrateStart;
    boolean _repeatNotificationStart;
    int _repeatNotificationIntervalStart;
    boolean _forceRun;
    boolean _blocked;
    int _priority;
    int _delayStart;
    boolean _isInDelayStart;
    boolean _manualProfileActivation;
    String _startWhenActivatedProfile;
    int _delayEnd;
    boolean _isInDelayEnd;
    long _startStatusTime;
    long _pauseStatusTime;
    boolean _noPauseByManualActivation;
    String _notificationSoundEnd;
    boolean _notificationVibrateEnd;

    EventPreferencesTime _eventPreferencesTime;
    EventPreferencesBattery _eventPreferencesBattery;
    EventPreferencesCall _eventPreferencesCall;
    EventPreferencesPeripherals _eventPreferencesPeripherals;
    EventPreferencesCalendar _eventPreferencesCalendar;
    EventPreferencesWifi _eventPreferencesWifi;
    EventPreferencesScreen _eventPreferencesScreen;
    EventPreferencesBluetooth _eventPreferencesBluetooth;
    EventPreferencesSMS _eventPreferencesSMS;
    EventPreferencesNotification _eventPreferencesNotification;
    EventPreferencesApplication _eventPreferencesApplication;
    EventPreferencesLocation _eventPreferencesLocation;
    EventPreferencesOrientation _eventPreferencesOrientation;
    EventPreferencesMobileCells _eventPreferencesMobileCells;
    EventPreferencesNFC _eventPreferencesNFC;
    EventPreferencesRadioSwitch _eventPreferencesRadioSwitch;
    EventPreferencesAlarmClock _eventPreferencesAlarmClock;

    static final int ESTATUS_STOP = 0;
    static final int ESTATUS_PAUSE = 1;
    static final int ESTATUS_RUNNING = 2;
    //static final int ESTATUS_NONE = 99;

    //static final int EPRIORITY_LOWEST = -5;
    //static final int EPRIORITY_VERY_LOW = -4;
    //static final int EPRIORITY_LOWER = -3;
    //static final int EPRIORITY_LOW = -1;
    //static final int EPRIORITY_LOWER_MEDIUM = -1;
    static final int EPRIORITY_MEDIUM = 0;
    //static final int EPRIORITY_UPPER_MEDIUM = 1;
    //static final int EPRIORITY_HIGH = 2;
    static final int EPRIORITY_HIGHER = 3;
    //static final int EPRIORITY_VERY_HIGH = 4;
    static final int EPRIORITY_HIGHEST = 5;

    static final int EATENDDO_NONE = 0;
    static final int EATENDDO_UNDONE_PROFILE = 1;
    static final int EATENDDO_RESTART_EVENTS = 2;

    private static final String PREF_EVENT_ID = "eventId";
    static final String PREF_EVENT_ENABLED = "eventEnabled";
    static final String PREF_EVENT_NAME = "eventName";
    private static final String PREF_EVENT_PROFILE_START = "eventProfileStart";
    private static final String PREF_EVENT_PROFILE_END = "eventProfileEnd";
    static final String PREF_EVENT_NOTIFICATION_SOUND_START = "eventStartNotificationSound";
    private static final String PREF_EVENT_NOTIFICATION_VIBRATE_START = "eventStartNotificationVibrate";
    private static final String PREF_EVENT_NOTIFICATION_REPEAT_START = "eventStartNotificationRepeat";
    private static final String PREF_EVENT_NOTIFICATION_REPEAT_INTERVAL_START = "eventStartNotificationRepeatInterval";
    static final String PREF_EVENT_NOTIFICATION_SOUND_END = "eventEndNotificationSound";
    private static final String PREF_EVENT_NOTIFICATION_VIBRATE_END = "eventEndNotificationVibrate";
    private static final String PREF_EVENT_FORCE_RUN = "eventForceRun";
    //static final String PREF_EVENT_UNDONE_PROFILE = "eventUndoneProfile";
    static final String PREF_EVENT_PRIORITY_APP_SETTINGS = "eventUsePriorityAppSettings";
    static final String PREF_EVENT_PRIORITY = "eventPriority";
    private static final String PREF_EVENT_DELAY_START = "eventDelayStart";
    private static final String PREF_EVENT_AT_END_DO = "eventAtEndDo";
    private static final String PREF_EVENT_MANUAL_PROFILE_ACTIVATION = "manualProfileActivation";
    private static final String PREF_EVENT_START_WHEN_ACTIVATED_PROFILE = "eventStartWhenActivatedProfile";
    private static final String PREF_EVENT_DELAY_END = "eventDelayEnd";
    private static final String PREF_EVENT_NO_PAUSE_BY_MANUAL_ACTIVATION = "eventNoPauseByManualActivation";
    private static final String PREF_EVENT_END_OTHERS = "eventEndOthersCategoryRoot";

    static final String PREF_GLOBAL_EVENTS_RUN_STOP = "globalEventsRunStop";
    private static final String PREF_EVENTS_BLOCKED = "eventsBlocked";
    private static final String PREF_FORCE_RUN_EVENT_RUNNING = "forceRunEventRunning";

    // alarm time offset (milliseconds) for events with generated alarms
    static final int EVENT_ALARM_TIME_OFFSET = 15000;
    static final int EVENT_ALARM_TIME_SOFT_OFFSET = 5000;

    // Empty constructor
    Event(){
        createEventPreferences();
    }

    // constructor
    Event(long id,
                 String name,
                 int startOrder,
                 long fkProfileStart,
                 long fkProfileEnd,
                 int status,
                 String notificationSoundStart,
                 boolean forceRun,
                 boolean blocked,
                 //boolean undoneProfile,
                 int priority,
                 int delayStart,
                 boolean isInDelayStart,
                 int atEndDo,
                 boolean manualProfileActivation,
                 String startWhenActivatedProfile,
                 int delayEnd,
                 boolean isInDelayEnd,
                 long startStatusTime,
                 long pauseStatusTime,
                 boolean notificationVibrateStart,
                 boolean noPauseByManualActivation,
                 boolean repeatNotificationStart,
                 int repeatNotificationIntervalStart,
                 String notificationSoundEnd,
                 boolean notificationVibrateEnd)
    {
        this._id = id;
        this._name = name;
        this._startOrder = startOrder;
        this._fkProfileStart = fkProfileStart;
        this._fkProfileEnd = fkProfileEnd;
        this._status = status;
        this._notificationSoundStart = notificationSoundStart;
        this._notificationVibrateStart = notificationVibrateStart;
        this._repeatNotificationStart = repeatNotificationStart;
        this._repeatNotificationIntervalStart = repeatNotificationIntervalStart;
        this._notificationSoundEnd = notificationSoundEnd;
        this._notificationVibrateEnd = notificationVibrateEnd;
        this._forceRun = forceRun;
        this._blocked = blocked;
        //this._undoneProfile = undoneProfile;
        this._priority = priority;
        this._delayStart = delayStart;
        this._isInDelayStart = isInDelayStart;
        this._atEndDo = atEndDo;
        this._manualProfileActivation = manualProfileActivation;
        this._startWhenActivatedProfile = startWhenActivatedProfile;
        this._delayEnd = delayEnd;
        this._isInDelayEnd = isInDelayEnd;
        this._startStatusTime = startStatusTime;
        this._pauseStatusTime = pauseStatusTime;
        this._noPauseByManualActivation = noPauseByManualActivation;

        createEventPreferences();
    }

    // constructor
    Event(String name,
                 int startOrder,
                 long fkProfileStart,
                 long fkProfileEnd,
                 int status,
                 String notificationSoundStart,
                 boolean forceRun,
                 boolean blocked,
                 //boolean undoneProfile,
                 int priority,
                 int delayStart,
                 boolean isInDelayStart,
                 int atEndDo,
                 boolean manualProfileActivation,
                 String startWhenActivatedProfile,
                 int delayEnd,
                 boolean isInDelayEnd,
                 long startStatusTime,
                 long pauseStatusTime,
                 boolean notificationVibrateStart,
                 boolean noPauseByManualActivation,
                 boolean repeatNotificationStart,
                 int repeatNotificationIntervalStart,
                 String notificationSoundEnd,
                 boolean notificationVibrateEnd)
    {
        this._name = name;
        this._startOrder = startOrder;
        this._fkProfileStart = fkProfileStart;
        this._fkProfileEnd = fkProfileEnd;
        this._status = status;
        this._notificationSoundStart = notificationSoundStart;
        this._notificationVibrateStart = notificationVibrateStart;
        this._repeatNotificationStart = repeatNotificationStart;
        this._repeatNotificationIntervalStart = repeatNotificationIntervalStart;
        this._notificationSoundEnd = notificationSoundEnd;
        this._notificationVibrateEnd = notificationVibrateEnd;
        this._forceRun = forceRun;
        this._blocked = blocked;
        //this._undoneProfile = undoneProfile;
        this._priority = priority;
        this._delayStart = delayStart;
        this._isInDelayStart = isInDelayStart;
        this._atEndDo = atEndDo;
        this._manualProfileActivation = manualProfileActivation;
        this._startWhenActivatedProfile = startWhenActivatedProfile;
        this._delayEnd = delayEnd;
        this._isInDelayEnd = isInDelayEnd;
        this._startStatusTime = startStatusTime;
        this._pauseStatusTime = pauseStatusTime;
        this._noPauseByManualActivation = noPauseByManualActivation;

        createEventPreferences();
    }

    void copyEvent(Event event)
    {
        this._id = event._id;
        this._name = event._name;
        this._startOrder = event._startOrder;
        this._fkProfileStart = event._fkProfileStart;
        this._fkProfileEnd = event._fkProfileEnd;
        this._status = event._status;
        this._notificationSoundStart = event._notificationSoundStart;
        this._notificationVibrateStart = event._notificationVibrateStart;
        this._repeatNotificationStart = event._repeatNotificationStart;
        this._repeatNotificationIntervalStart = event._repeatNotificationIntervalStart;
        this._notificationSoundEnd = event._notificationSoundEnd;
        this._notificationVibrateEnd = event._notificationVibrateEnd;
        this._forceRun = event._forceRun;
        this._blocked = event._blocked;
        //this._undoneProfile = event._undoneProfile;
        this._priority = event._priority;
        this._delayStart = event._delayStart;
        this._isInDelayStart = event._isInDelayStart;
        this._atEndDo = event._atEndDo;
        this._manualProfileActivation = event._manualProfileActivation;
        this._startWhenActivatedProfile = event._startWhenActivatedProfile;
        this._delayEnd = event._delayEnd;
        this._isInDelayEnd = event._isInDelayEnd;
        this._startStatusTime = event._startStatusTime;
        this._pauseStatusTime = event._pauseStatusTime;
        this._noPauseByManualActivation = event._noPauseByManualActivation;

        copyEventPreferences(event);
    }

    private void createEventPreferencesTime()
    {
        this._eventPreferencesTime = new EventPreferencesTime(this, false, false, false, false, false, false, false, false, 0, 0, 0/*, false*/);
    }

    private void createEventPreferencesBattery()
    {
        this._eventPreferencesBattery = new EventPreferencesBattery(this, false, 0, 100, 0, false, "");
    }

    private void createEventPreferencesCall()
    {
        this._eventPreferencesCall = new EventPreferencesCall(this, false, 0, "", "", 0, false, 5);
    }

    private void createEventPreferencesPeripherals()
    {
        this._eventPreferencesPeripherals = new EventPreferencesPeripherals(this, false, 0);
    }

    private void createEventPreferencesCalendar()
    {
        this._eventPreferencesCalendar = new EventPreferencesCalendar(this, false, "", false,0, "", 0, false, 0);
    }

    private void createEventPreferencesWiFi()
    {
        this._eventPreferencesWifi = new EventPreferencesWifi(this, false, "", 1);
    }

    private void createEventPreferencesScreen()
    {
        this._eventPreferencesScreen = new EventPreferencesScreen(this, false, 1, false);
    }

    private void createEventPreferencesBluetooth()
    {
        this._eventPreferencesBluetooth = new EventPreferencesBluetooth(this, false, "", 0/*, 0*/);
    }

    private void createEventPreferencesSMS()
    {
        this._eventPreferencesSMS = new EventPreferencesSMS(this, false, "", "", 0, false, 5);
    }

    private void createEventPreferencesNotification()
    {
        this._eventPreferencesNotification = new EventPreferencesNotification(this, false, "", false, false, 0, false, "", "", false, "", 0);
    }

    private void createEventPreferencesApplication()
    {
        this._eventPreferencesApplication = new EventPreferencesApplication(this, false, "");
    }

    private void createEventPreferencesLocation()
    {
        this._eventPreferencesLocation = new EventPreferencesLocation(this, false, "", false);
    }

    private void createEventPreferencesOrientation()
    {
        this._eventPreferencesOrientation = new EventPreferencesOrientation(this, false, "", "", 0, false, "0", "0", "");
    }

    private void createEventPreferencesMobileCells()
    {
        this._eventPreferencesMobileCells = new EventPreferencesMobileCells(this, false, "", false);
    }

    private void createEventPreferencesNFC()
    {
        this._eventPreferencesNFC = new EventPreferencesNFC(this, false, "", true, 5);
    }

    private void createEventPreferencesRadioSwitch()
    {
        this._eventPreferencesRadioSwitch = new EventPreferencesRadioSwitch(this, false, 0, 0, 0, 0, 0, 0);
    }

    private void createEventPreferencesAlarmClock()
    {
        this._eventPreferencesAlarmClock = new EventPreferencesAlarmClock(this, false, false, 5);
    }

    void createEventPreferences()
    {
        createEventPreferencesTime();
        createEventPreferencesBattery();
        createEventPreferencesCall();
        createEventPreferencesPeripherals();
        createEventPreferencesCalendar();
        createEventPreferencesWiFi();
        createEventPreferencesScreen();
        createEventPreferencesBluetooth();
        createEventPreferencesSMS();
        createEventPreferencesNotification();
        createEventPreferencesApplication();
        createEventPreferencesLocation();
        createEventPreferencesOrientation();
        createEventPreferencesMobileCells();
        createEventPreferencesNFC();
        createEventPreferencesRadioSwitch();
        createEventPreferencesAlarmClock();
    }

    void copyEventPreferences(Event fromEvent)
    {
        if (this._eventPreferencesTime == null)
            createEventPreferencesTime();
        if (this._eventPreferencesBattery == null)
            createEventPreferencesBattery();
        if (this._eventPreferencesCall == null)
            createEventPreferencesCall();
        if (this._eventPreferencesPeripherals == null)
            createEventPreferencesPeripherals();
        if (this._eventPreferencesCalendar == null)
            createEventPreferencesCalendar();
        if (this._eventPreferencesWifi == null)
            createEventPreferencesWiFi();
        if (this._eventPreferencesScreen == null)
            createEventPreferencesScreen();
        if (this._eventPreferencesBluetooth == null)
            createEventPreferencesBluetooth();
        if (this._eventPreferencesSMS == null)
            createEventPreferencesSMS();
        if (this._eventPreferencesNotification == null)
            createEventPreferencesNotification();
        if (this._eventPreferencesApplication == null)
            createEventPreferencesApplication();
        if (this._eventPreferencesLocation == null)
            createEventPreferencesLocation();
        if (this._eventPreferencesOrientation == null)
            createEventPreferencesOrientation();
        if (this._eventPreferencesMobileCells == null)
            createEventPreferencesMobileCells();
        if (this._eventPreferencesNFC == null)
            createEventPreferencesNFC();
        if (this._eventPreferencesRadioSwitch == null)
            createEventPreferencesRadioSwitch();
        if (this._eventPreferencesAlarmClock == null)
            createEventPreferencesAlarmClock();
        this._eventPreferencesTime.copyPreferences(fromEvent);
        this._eventPreferencesBattery.copyPreferences(fromEvent);
        this._eventPreferencesCall.copyPreferences(fromEvent);
        this._eventPreferencesPeripherals.copyPreferences(fromEvent);
        this._eventPreferencesCalendar.copyPreferences(fromEvent);
        this._eventPreferencesWifi.copyPreferences(fromEvent);
        this._eventPreferencesScreen.copyPreferences(fromEvent);
        this._eventPreferencesBluetooth.copyPreferences(fromEvent);
        this._eventPreferencesSMS.copyPreferences(fromEvent);
        this._eventPreferencesNotification.copyPreferences(fromEvent);
        this._eventPreferencesApplication.copyPreferences(fromEvent);
        this._eventPreferencesLocation.copyPreferences(fromEvent);
        this._eventPreferencesOrientation.copyPreferences(fromEvent);
        this._eventPreferencesMobileCells.copyPreferences(fromEvent);
        this._eventPreferencesNFC.copyPreferences(fromEvent);
        this._eventPreferencesRadioSwitch.copyPreferences(fromEvent);
        this._eventPreferencesAlarmClock.copyPreferences(fromEvent);
    }

    boolean isEnabledSomeSensor(Context context) {
        return  (this._eventPreferencesTime._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesTime.PREF_EVENT_TIME_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesBattery._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesCall._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesPeripherals._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesPeripherals.PREF_EVENT_PERIPHERAL_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesCalendar._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesWifi._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesScreen._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesScreen.PREF_EVENT_SCREEN_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesBluetooth._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesSMS._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesNotification._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesApplication._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesLocation._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesOrientation._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesMobileCells._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesNFC._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesRadioSwitch._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesAlarmClock._enabled &&
                        (isEventPreferenceAllowed(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED));
    }

    public boolean isRunnable(Context context, boolean checkSomeSensorEnabled) {
        boolean runnable = (this._fkProfileStart != 0);
        if (checkSomeSensorEnabled) {
            boolean someEnabled = isEnabledSomeSensor(context);
            if (!someEnabled)
                runnable = false;
        }
        if (this._eventPreferencesTime._enabled)
            runnable = runnable && this._eventPreferencesTime.isRunnable(context);
        if (this._eventPreferencesBattery._enabled)
            runnable = runnable && this._eventPreferencesBattery.isRunnable(context);
        if (this._eventPreferencesCall._enabled)
            runnable = runnable && this._eventPreferencesCall.isRunnable(context);
        if (this._eventPreferencesPeripherals._enabled)
            runnable = runnable && this._eventPreferencesPeripherals.isRunnable(context);
        if (this._eventPreferencesCalendar._enabled)
            runnable = runnable && this._eventPreferencesCalendar.isRunnable(context);
        if (this._eventPreferencesWifi._enabled)
            runnable = runnable && this._eventPreferencesWifi.isRunnable(context);
        if (this._eventPreferencesScreen._enabled)
            runnable = runnable && this._eventPreferencesScreen.isRunnable(context);
        if (this._eventPreferencesBluetooth._enabled)
            runnable = runnable && this._eventPreferencesBluetooth.isRunnable(context);
        if (this._eventPreferencesSMS._enabled)
            runnable = runnable && this._eventPreferencesSMS.isRunnable(context);
        if (this._eventPreferencesNotification._enabled)
            runnable = runnable && this._eventPreferencesNotification.isRunnable(context);
        if (this._eventPreferencesApplication._enabled)
            runnable = runnable && this._eventPreferencesApplication.isRunnable(context);
        if (this._eventPreferencesLocation._enabled)
            runnable = runnable && this._eventPreferencesLocation.isRunnable(context);
        if (this._eventPreferencesOrientation._enabled)
            runnable = runnable && this._eventPreferencesOrientation.isRunnable(context);
        if (this._eventPreferencesMobileCells._enabled)
            runnable = runnable && this._eventPreferencesMobileCells.isRunnable(context);
        if (this._eventPreferencesNFC._enabled)
            runnable = runnable && this._eventPreferencesNFC.isRunnable(context);
        if (this._eventPreferencesRadioSwitch._enabled)
            runnable = runnable && this._eventPreferencesRadioSwitch.isRunnable(context);
        if (this._eventPreferencesAlarmClock._enabled)
            runnable = runnable && this._eventPreferencesAlarmClock.isRunnable(context);

        return runnable;
    }

    public int isAccessibilityServiceEnabled(Context context, boolean checkSomeSensorEnabled) {
        int accessibilityEnabled = 1;
        boolean someEnabled = true;
        if (checkSomeSensorEnabled) {
            someEnabled =
                    this._eventPreferencesTime._enabled ||
                            this._eventPreferencesBattery._enabled ||
                            this._eventPreferencesCall._enabled ||
                            this._eventPreferencesPeripherals._enabled ||
                            this._eventPreferencesCalendar._enabled ||
                            this._eventPreferencesWifi._enabled ||
                            this._eventPreferencesScreen._enabled ||
                            this._eventPreferencesBluetooth._enabled ||
                            this._eventPreferencesSMS._enabled ||
                            this._eventPreferencesNotification._enabled ||
                            this._eventPreferencesApplication._enabled ||
                            this._eventPreferencesLocation._enabled ||
                            this._eventPreferencesOrientation._enabled ||
                            this._eventPreferencesMobileCells._enabled ||
                            this._eventPreferencesNFC._enabled ||
                            this._eventPreferencesRadioSwitch._enabled;
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
                someEnabled = someEnabled ||
                        this._eventPreferencesAlarmClock._enabled;
            //}
        }
        if (someEnabled) {
            if (this._eventPreferencesTime._enabled)
                accessibilityEnabled = this._eventPreferencesTime.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesBattery._enabled)
                accessibilityEnabled = this._eventPreferencesBattery.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesCall._enabled)
                accessibilityEnabled = this._eventPreferencesCall.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesPeripherals._enabled)
                accessibilityEnabled = this._eventPreferencesPeripherals.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesCalendar._enabled)
                accessibilityEnabled = this._eventPreferencesCalendar.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesWifi._enabled)
                accessibilityEnabled = this._eventPreferencesWifi.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesScreen._enabled)
                accessibilityEnabled = this._eventPreferencesScreen.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesBluetooth._enabled)
                accessibilityEnabled = this._eventPreferencesBluetooth.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesSMS._enabled)
                accessibilityEnabled = this._eventPreferencesSMS.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesNotification._enabled)
                accessibilityEnabled = this._eventPreferencesNotification.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesApplication._enabled)
                accessibilityEnabled = this._eventPreferencesApplication.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesLocation._enabled)
                accessibilityEnabled = this._eventPreferencesLocation.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesOrientation._enabled)
                accessibilityEnabled = this._eventPreferencesOrientation.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesMobileCells._enabled)
                accessibilityEnabled = this._eventPreferencesMobileCells.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesNFC._enabled)
                accessibilityEnabled = this._eventPreferencesNFC.isAccessibilityServiceEnabled(context);
            if (this._eventPreferencesRadioSwitch._enabled)
                accessibilityEnabled = this._eventPreferencesRadioSwitch.isAccessibilityServiceEnabled(context);
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
                if (this._eventPreferencesAlarmClock._enabled)
                    accessibilityEnabled = this._eventPreferencesAlarmClock.isAccessibilityServiceEnabled(context);
            //}
        }

        return accessibilityEnabled;
    }

    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putLong(PREF_EVENT_ID, this._id);
        editor.putString(PREF_EVENT_NAME, this._name);
        editor.putString(PREF_EVENT_PROFILE_START, Long.toString(this._fkProfileStart));
        editor.putString(PREF_EVENT_PROFILE_END, Long.toString(this._fkProfileEnd));
        editor.putBoolean(PREF_EVENT_ENABLED, this._status != ESTATUS_STOP);
        editor.putString(PREF_EVENT_NOTIFICATION_SOUND_START, this._notificationSoundStart);
        editor.putBoolean(PREF_EVENT_NOTIFICATION_VIBRATE_START, this._notificationVibrateStart);
        editor.putBoolean(PREF_EVENT_NOTIFICATION_REPEAT_START, this._repeatNotificationStart);
        editor.putString(PREF_EVENT_NOTIFICATION_REPEAT_INTERVAL_START, String.valueOf(this._repeatNotificationIntervalStart));
        editor.putString(PREF_EVENT_NOTIFICATION_SOUND_END, this._notificationSoundEnd);
        editor.putBoolean(PREF_EVENT_NOTIFICATION_VIBRATE_END, this._notificationVibrateEnd);
        editor.putBoolean(PREF_EVENT_FORCE_RUN, this._forceRun);
        //editor.putBoolean(PREF_EVENT_UNDONE_PROFILE, this._undoneProfile);
        editor.putString(PREF_EVENT_PRIORITY_APP_SETTINGS, Integer.toString(this._priority));
        editor.putString(PREF_EVENT_PRIORITY, Integer.toString(this._priority));
        editor.putString(PREF_EVENT_DELAY_START, Integer.toString(this._delayStart));
        editor.putString(PREF_EVENT_AT_END_DO, Integer.toString(this._atEndDo));
        editor.putBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION, this._manualProfileActivation);
        editor.putString(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, this._startWhenActivatedProfile);
        editor.putString(PREF_EVENT_DELAY_END, Integer.toString(this._delayEnd));
        editor.putBoolean(PREF_EVENT_NO_PAUSE_BY_MANUAL_ACTIVATION, this._noPauseByManualActivation);
        this._eventPreferencesTime.loadSharedPreferences(preferences);
        this._eventPreferencesBattery.loadSharedPreferences(preferences);
        this._eventPreferencesCall.loadSharedPreferences(preferences);
        this._eventPreferencesPeripherals.loadSharedPreferences(preferences);
        this._eventPreferencesCalendar.loadSharedPreferences(preferences);
        this._eventPreferencesWifi.loadSharedPreferences(preferences);
        this._eventPreferencesScreen.loadSharedPreferences(preferences);
        this._eventPreferencesBluetooth.loadSharedPreferences(preferences);
        this._eventPreferencesSMS.loadSharedPreferences(preferences);
        this._eventPreferencesNotification.loadSharedPreferences(preferences);
        this._eventPreferencesApplication.loadSharedPreferences(preferences);
        this._eventPreferencesLocation.loadSharedPreferences(preferences);
        this._eventPreferencesOrientation.loadSharedPreferences(preferences);
        this._eventPreferencesMobileCells.loadSharedPreferences(preferences);
        this._eventPreferencesNFC.loadSharedPreferences(preferences);
        this._eventPreferencesRadioSwitch.loadSharedPreferences(preferences);
        this._eventPreferencesAlarmClock.loadSharedPreferences(preferences);
        editor.apply();
    }

    public void saveSharedPreferences(SharedPreferences preferences, Context context)
    {
        this._name = preferences.getString(PREF_EVENT_NAME, "");
        this._fkProfileStart = Long.parseLong(preferences.getString(PREF_EVENT_PROFILE_START, "0"));
        this._fkProfileEnd = Long.parseLong(preferences.getString(PREF_EVENT_PROFILE_END, Long.toString(Profile.PROFILE_NO_ACTIVATE)));
        this._status = (preferences.getBoolean(PREF_EVENT_ENABLED, false)) ? ESTATUS_PAUSE : ESTATUS_STOP;
        this._notificationSoundStart = preferences.getString(PREF_EVENT_NOTIFICATION_SOUND_START, "");
        this._notificationVibrateStart = preferences.getBoolean(PREF_EVENT_NOTIFICATION_VIBRATE_START, false);
        this._repeatNotificationStart = preferences.getBoolean(PREF_EVENT_NOTIFICATION_REPEAT_START, false);
        this._repeatNotificationIntervalStart = Integer.parseInt(preferences.getString(PREF_EVENT_NOTIFICATION_REPEAT_INTERVAL_START, "900"));
        this._notificationSoundEnd = preferences.getString(PREF_EVENT_NOTIFICATION_SOUND_END, "");
        this._notificationVibrateEnd = preferences.getBoolean(PREF_EVENT_NOTIFICATION_VIBRATE_END, false);
        this._forceRun = preferences.getBoolean(PREF_EVENT_FORCE_RUN, false);
        //this._undoneProfile = preferences.getBoolean(PREF_EVENT_UNDONE_PROFILE, true);
        this._priority = Integer.parseInt(preferences.getString(PREF_EVENT_PRIORITY, Integer.toString(EPRIORITY_MEDIUM)));
        this._atEndDo = Integer.parseInt(preferences.getString(PREF_EVENT_AT_END_DO, Integer.toString(EATENDDO_RESTART_EVENTS)));
        this._manualProfileActivation = preferences.getBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION, false);
        this._startWhenActivatedProfile = preferences.getString(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, "");
        this._noPauseByManualActivation = preferences.getBoolean(PREF_EVENT_NO_PAUSE_BY_MANUAL_ACTIVATION, false);

        String sDelayStart = preferences.getString(PREF_EVENT_DELAY_START, "0");
        if (sDelayStart.isEmpty()) sDelayStart = "0";
        int iDelayStart = Integer.parseInt(sDelayStart);
        if (iDelayStart < 0) iDelayStart = 0;
        this._delayStart = iDelayStart;

        String sDelayEnd = preferences.getString(PREF_EVENT_DELAY_END, "0");
        if (sDelayEnd.isEmpty()) sDelayEnd = "0";
        int iDelayEnd = Integer.parseInt(sDelayEnd);
        if (iDelayEnd < 0) iDelayEnd = 0;
        this._delayEnd = iDelayEnd;


        this._eventPreferencesTime.saveSharedPreferences(preferences);
        this._eventPreferencesBattery.saveSharedPreferences(preferences);
        this._eventPreferencesCall.saveSharedPreferences(preferences);
        this._eventPreferencesPeripherals.saveSharedPreferences(preferences);
        this._eventPreferencesCalendar.saveSharedPreferences(preferences);
        this._eventPreferencesWifi.saveSharedPreferences(preferences);
        this._eventPreferencesScreen.saveSharedPreferences(preferences);
        this._eventPreferencesBluetooth.saveSharedPreferences(preferences);
        this._eventPreferencesSMS.saveSharedPreferences(preferences);
        this._eventPreferencesNotification.saveSharedPreferences(preferences);
        this._eventPreferencesApplication.saveSharedPreferences(preferences);
        this._eventPreferencesLocation.saveSharedPreferences(preferences);
        this._eventPreferencesOrientation.saveSharedPreferences(preferences);
        this._eventPreferencesMobileCells.saveSharedPreferences(preferences);
        this._eventPreferencesNFC.saveSharedPreferences(preferences);
        this._eventPreferencesRadioSwitch.saveSharedPreferences(preferences);
        this._eventPreferencesAlarmClock.saveSharedPreferences(preferences);

        if (!this.isRunnable(context, true))
            this._status = ESTATUS_STOP;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        Preference pref = prefMng.findPreference(key);
        if (pref == null)
            return;
        if (key.equals(PREF_EVENT_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !value.isEmpty(), false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_PROFILE_START)||key.equals(PREF_EVENT_PROFILE_END))
        {
            ProfilePreferenceX preference = prefMng.findPreference(key);
            if (preference != null) {
                long lProfileId;
                try {
                    lProfileId = Long.parseLong(value);
                } catch (Exception e) {
                    lProfileId = 0;
                }
                preference.setSummary(lProfileId);
                if (key.equals(PREF_EVENT_PROFILE_START))
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, (lProfileId != 0) && (lProfileId != Profile.PROFILE_NO_ACTIVATE), true, lProfileId == 0, false);
                else
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, (lProfileId != 0) && (lProfileId != Profile.PROFILE_NO_ACTIVATE), false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE))
        {
            ProfileMultiSelectPreferenceX preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !value.isEmpty(), false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_SOUND_START) ||
            key.equals(PREF_EVENT_NOTIFICATION_SOUND_END))
        {
            Preference preference = prefMng.findPreference(key);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !value.isEmpty(), false, false, false);
        }
        if (key.equals(PREF_EVENT_PRIORITY_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                boolean applicationEventUsePriority = ApplicationPreferences.applicationEventUsePriority;
                String summary = context.getString(R.string.event_preferences_event_priorityInfo_summary);
                if (applicationEventUsePriority)
                    summary = context.getString(R.string.event_preferences_priority_appSettings_enabled) + "\n\n" + summary;
                else {
                    summary = context.getString(R.string.event_preferences_priority_appSettings_disabled) + "\n\n" + summary + "\n"+
                            context.getString(R.string.phone_profiles_pref_eventUsePriorityAppSettings_summary);
                }
                preference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, applicationEventUsePriority, false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_PRIORITY))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                boolean applicationEventUsePriority = ApplicationPreferences.applicationEventUsePriority;
                if (applicationEventUsePriority) {
                    int index = listPreference.findIndexOfValue(value);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, !value.equals("0"), false, false, false);
                } else {
                    listPreference.setSummary(R.string.event_preferences_priority_notUse);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, false);
                }
                listPreference.setEnabled(applicationEventUsePriority);
            }
        }
        if (key.equals(PREF_EVENT_AT_END_DO))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_DELAY_START))
        {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 0;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 0, false, false, false);
        }
        if (key.equals(PREF_EVENT_DELAY_END))
        {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 0;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 0, false, false, false);
        }
        /*
        if (key.equals(PREF_EVENT_NOTIFICATION_REPEAT_INTERVAL)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);
                //int iValue;
                //try {
                //    iValue = Integer.parseInt(value);
                //} catch (Exception e) {
                //    iValue = 0;
                //}
                //GlobalGUIRoutines.setPreferenceTitleStyle(preference, iValue != 15, false, false, false);
            }
        }
        */
        if (key.equals(PREF_EVENT_ENABLED) ||
            key.equals(PREF_EVENT_FORCE_RUN) ||
            key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION) ||
            key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_START) ||
            key.equals(PREF_EVENT_NOTIFICATION_REPEAT_START) ||
            key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_END) ||
            key.equals(PREF_EVENT_NO_PAUSE_BY_MANUAL_ACTIVATION)) {

            Preference preference = prefMng.findPreference(key);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, value.equals("true"), false, false, false);
        }

    }

    private void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (key.isEmpty() ||
                //key.equals(PREF_EVENT_FORCE_RUN) ||
                key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION) ||
                key.equals(PREF_EVENT_NOTIFICATION_SOUND_START) ||
                key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_START) ||
                key.equals(PREF_EVENT_NOTIFICATION_REPEAT_START) ||
                key.equals(PREF_EVENT_NOTIFICATION_REPEAT_INTERVAL_START) ||
                key.equals(PREF_EVENT_NOTIFICATION_SOUND_END) ||
                key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_END) ||
                key.equals(PREF_EVENT_DELAY_START) ||
                key.equals(PREF_EVENT_DELAY_END) ||
                key.equals(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE)) {
            //boolean forceRunChanged = false;
            boolean manualProfileActivationChanged;
            boolean profileStartWhenActivatedChanged;
            boolean delayStartChanged;
            boolean delayEndChanged;
            boolean notificationSoundStartChanged;
            boolean notificationVibrateStartChanged;
            boolean notificationRepeatStartChanged;
            boolean notificationSoundEndChanged;
            boolean notificationVibrateEndChanged;

            String startWhenActivatedProfile;
            int delayStart;
            int delayEnd;

            if (preferences == null) {
                //forceRunChanged = this._forceRun;
                manualProfileActivationChanged = this._manualProfileActivation;
                profileStartWhenActivatedChanged = !this._startWhenActivatedProfile.isEmpty();
                startWhenActivatedProfile = this._startWhenActivatedProfile;
                delayStartChanged = this._delayStart != 0;
                delayEndChanged = this._delayEnd != 0;
                delayStart = this._delayStart;
                delayEnd = this._delayEnd;
                notificationSoundStartChanged = !this._notificationSoundStart.isEmpty();
                notificationVibrateStartChanged = this._notificationVibrateStart;
                notificationRepeatStartChanged = this._repeatNotificationStart;
                notificationSoundEndChanged = !this._notificationSoundEnd.isEmpty();
                notificationVibrateEndChanged = this._notificationVibrateEnd;
            }
            else {
                //forceRunChanged = preferences.getBoolean(PREF_EVENT_FORCE_RUN, false);
                manualProfileActivationChanged = preferences.getBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION, false);
                startWhenActivatedProfile = preferences.getString(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, "");
                profileStartWhenActivatedChanged = !startWhenActivatedProfile.isEmpty();
                delayStartChanged = !preferences.getString(PREF_EVENT_DELAY_START, "0").equals("0");
                delayEndChanged = !preferences.getString(PREF_EVENT_DELAY_END, "0").equals("0");
                delayStart = Integer.parseInt(preferences.getString(PREF_EVENT_DELAY_START, "0"));
                delayEnd = Integer.parseInt(preferences.getString(PREF_EVENT_DELAY_END, "0"));
                notificationSoundStartChanged = !preferences.getString(PREF_EVENT_NOTIFICATION_SOUND_START, "").isEmpty();
                notificationVibrateStartChanged = preferences.getBoolean(PREF_EVENT_NOTIFICATION_VIBRATE_START, false);
                notificationRepeatStartChanged = preferences.getBoolean(PREF_EVENT_NOTIFICATION_REPEAT_START, false);
                notificationSoundEndChanged = !preferences.getString(PREF_EVENT_NOTIFICATION_SOUND_END, "").isEmpty();
                notificationVibrateEndChanged = preferences.getBoolean(PREF_EVENT_NOTIFICATION_VIBRATE_END, false);
            }
            Preference preference = prefMng.findPreference("eventStartOthersCategoryRoot");
            if (preference != null) {
                boolean bold = (//forceRunChanged ||
                                manualProfileActivationChanged ||
                                profileStartWhenActivatedChanged ||
                                delayStartChanged ||
                                notificationSoundStartChanged ||
                                notificationVibrateStartChanged ||
                                notificationRepeatStartChanged);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, false, false, false);
                if (bold) {
                    String summary = "";
                    //if (forceRunChanged)
                    //    summary = summary + "[»] " + context.getString(R.string.event_preferences_ForceRun);
                    if (manualProfileActivationChanged) {
                        /*if (!summary.isEmpty())*/ summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_manualProfileActivation);
                    }
                    if (profileStartWhenActivatedChanged) {
                        if (!summary.isEmpty()) summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_eventStartWhenActivatedProfile) + ": ";
                        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
                        String[] splits = startWhenActivatedProfile.split("\\|");
                        Profile profile;
                        if (splits.length == 1) {
                            profile = dataWrapper.getProfileById(Long.valueOf(startWhenActivatedProfile), false, false, false);
                            if (profile != null)
                                summary = summary + profile._name;
                        }
                        else {
                            summary = summary + context.getString(R.string.profile_multiselect_summary_text_selected) + " " + splits.length;
                        }
                    }
                    if (delayStartChanged) {
                        if (!summary.isEmpty()) summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_delayStart) + ": ";
                        summary = summary + GlobalGUIRoutines.getDurationString(delayStart);
                    }
                    if (notificationSoundStartChanged) {
                        if (!summary.isEmpty()) summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_notificationSound);
                    }
                    if (notificationVibrateStartChanged) {
                        if (!summary.isEmpty()) summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_notificationVibrate);
                    }
                    if (notificationRepeatStartChanged) {
                        if (!summary.isEmpty()) summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_notificationRepeat);
                    }
                    preference.setSummary(summary);
                }
                else
                    preference.setSummary("");
            }
            preference = prefMng.findPreference("eventEndOthersCategoryRoot");
            if (preference != null) {
                boolean bold = (delayEndChanged ||
                                notificationSoundEndChanged ||
                                notificationVibrateEndChanged);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, false, false, false);
                if (bold) {
                    String summary = "";
                    if (delayEndChanged) {
                        /*if (!summary.isEmpty())*/ summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_delayStart) + ": ";
                        summary = summary + GlobalGUIRoutines.getDurationString(delayEnd);
                    }
                    if (notificationSoundEndChanged) {
                        if (!summary.isEmpty()) summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_notificationSound);
                    }
                    if (notificationVibrateEndChanged) {
                        if (!summary.isEmpty()) summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_notificationVibrate);
                    }
                    preference.setSummary(summary);
                }
                else
                    preference.setSummary("");
            }
        }
    }

    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_NAME) ||
            key.equals(PREF_EVENT_PROFILE_START) ||
            key.equals(PREF_EVENT_PROFILE_END) ||
            key.equals(PREF_EVENT_NOTIFICATION_SOUND_START) ||
            key.equals(PREF_EVENT_NOTIFICATION_SOUND_END) ||
            key.equals(PREF_EVENT_NOTIFICATION_REPEAT_INTERVAL_START) ||
            key.equals(PREF_EVENT_PRIORITY) ||
            key.equals(PREF_EVENT_DELAY_START) ||
            key.equals(PREF_EVENT_DELAY_END) ||
            key.equals(PREF_EVENT_AT_END_DO) ||
            key.equals(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE))
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        if (key.equals(PREF_EVENT_ENABLED) ||
            key.equals(PREF_EVENT_FORCE_RUN) ||
            key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION) ||
            key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_START) ||
            key.equals(PREF_EVENT_NOTIFICATION_REPEAT_START) ||
            key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_END) ||
            key.equals(PREF_EVENT_NO_PAUSE_BY_MANUAL_ACTIVATION)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, Boolean.toString(value), context);
        }
        if (key.equals(PREF_EVENT_PRIORITY_APP_SETTINGS)) {
            setSummary(prefMng, key, "", context);
        }

        if (key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION) ||
                key.equals(PREF_EVENT_PROFILE_END) ||
                key.equals(PREF_EVENT_AT_END_DO) ||
                key.equals(PREF_EVENT_END_OTHERS)) {
            boolean value = preferences.getBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION, false);
            Preference preference = prefMng.findPreference(PREF_EVENT_PROFILE_END);
            if (preference != null)
                preference.setEnabled(!value);
            preference = prefMng.findPreference(PREF_EVENT_AT_END_DO);
            if (preference != null)
                preference.setEnabled(!value);
            preference = prefMng.findPreference(PREF_EVENT_END_OTHERS);
            if (preference != null)
                preference.setEnabled(!value);
        }

        setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesTime.setSummary(prefMng, key, preferences, context);
        _eventPreferencesTime.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesBattery.setSummary(prefMng, key, preferences, context);
        _eventPreferencesBattery.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesCall.setSummary(prefMng, key, preferences, context);
        _eventPreferencesCall.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesPeripherals.setSummary(prefMng, key, preferences, context);
        _eventPreferencesPeripherals.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesCalendar.setSummary(prefMng, key, preferences, context);
        _eventPreferencesCalendar.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesWifi.setSummary(prefMng, key, preferences, context);
        _eventPreferencesWifi.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesScreen.setSummary(prefMng, key, preferences, context);
        _eventPreferencesScreen.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesBluetooth.setSummary(prefMng, key, preferences, context);
        _eventPreferencesBluetooth.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesSMS.setSummary(prefMng, key, preferences, context);
        _eventPreferencesSMS.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesNotification.setSummary(prefMng, key, preferences, context);
        _eventPreferencesNotification.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesApplication.setSummary(prefMng, key, preferences, context);
        _eventPreferencesApplication.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesLocation.setSummary(prefMng, key, preferences, context);
        _eventPreferencesLocation.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesOrientation.setSummary(prefMng, key, preferences, context);
        _eventPreferencesOrientation.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesMobileCells.setSummary(prefMng, key, preferences, context);
        _eventPreferencesMobileCells.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesNFC.setSummary(prefMng, key, preferences, context);
        _eventPreferencesNFC.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesRadioSwitch.setSummary(prefMng, key, preferences, context);
        _eventPreferencesRadioSwitch.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesAlarmClock.setSummary(prefMng, key, preferences, context);
        _eventPreferencesAlarmClock.setCategorySummary(prefMng, preferences, context);
    }

    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context) {

        Preference preference = prefMng.findPreference(PREF_EVENT_FORCE_RUN);
        if (preference != null)
            preference.setTitle("[»] " + context.getString(R.string.event_preferences_ForceRun));

        setSummary(prefMng, PREF_EVENT_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_NAME, preferences, context);
        setSummary(prefMng, PREF_EVENT_PROFILE_START, preferences, context);
        setSummary(prefMng, PREF_EVENT_PROFILE_END, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_SOUND_START, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_VIBRATE_START, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_REPEAT_START, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_REPEAT_INTERVAL_START, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_SOUND_END, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_VIBRATE_END, preferences, context);
        setSummary(prefMng, PREF_EVENT_PRIORITY_APP_SETTINGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_PRIORITY, preferences, context);
        setSummary(prefMng, PREF_EVENT_DELAY_START, preferences, context);
        setSummary(prefMng, PREF_EVENT_DELAY_END, preferences, context);
        setSummary(prefMng, PREF_EVENT_AT_END_DO, preferences, context);
        setSummary(prefMng, PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, preferences, context);
        setSummary(prefMng, PREF_EVENT_FORCE_RUN, preferences, context);
        setSummary(prefMng, PREF_EVENT_MANUAL_PROFILE_ACTIVATION, preferences, context);
        setSummary(prefMng, PREF_EVENT_NO_PAUSE_BY_MANUAL_ACTIVATION, preferences, context);
        setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesTime.setAllSummary(prefMng, preferences, context);
        _eventPreferencesTime.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesBattery.setAllSummary(prefMng, preferences, context);
        _eventPreferencesBattery.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesCall.setAllSummary(prefMng, preferences, context);
        _eventPreferencesCall.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesPeripherals.setAllSummary(prefMng, preferences, context);
        _eventPreferencesPeripherals.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesCalendar.setAllSummary(prefMng, preferences, context);
        _eventPreferencesCalendar.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesWifi.setAllSummary(prefMng, preferences, context);
        _eventPreferencesWifi.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesScreen.setAllSummary(prefMng, preferences, context);
        _eventPreferencesScreen.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesBluetooth.setAllSummary(prefMng, preferences, context);
        _eventPreferencesBluetooth.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesSMS.setAllSummary(prefMng, preferences, context);
        _eventPreferencesSMS.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesNotification.setAllSummary(prefMng, preferences, context);
        _eventPreferencesNotification.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesApplication.setAllSummary(prefMng, preferences, context);
        _eventPreferencesApplication.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesLocation.setAllSummary(prefMng, preferences, context);
        _eventPreferencesLocation.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesOrientation.setAllSummary(prefMng, preferences, context);
        _eventPreferencesOrientation.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesMobileCells.setAllSummary(prefMng, preferences, context);
        _eventPreferencesMobileCells.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesNFC.setAllSummary(prefMng, preferences, context);
        _eventPreferencesNFC.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesRadioSwitch.setAllSummary(prefMng, preferences, context);
        _eventPreferencesRadioSwitch.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesAlarmClock.setAllSummary(prefMng, preferences, context);
        _eventPreferencesAlarmClock.setCategorySummary(prefMng, preferences, context);
    }

    public String getPreferencesDescription(Context context, boolean addPassStatus)
    {
        String description;

        description = "";

        if (_eventPreferencesTime._enabled) {
            String desc = _eventPreferencesTime.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesCalendar._enabled) {
            String desc = _eventPreferencesCalendar.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesBattery._enabled) {
            String desc = _eventPreferencesBattery.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesCall._enabled) {
            String desc = _eventPreferencesCall.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesSMS._enabled) {
            String desc = _eventPreferencesSMS.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesRadioSwitch._enabled) {
            String desc = _eventPreferencesRadioSwitch.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesLocation._enabled) {
            String desc = _eventPreferencesLocation.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesWifi._enabled) {
            String desc = _eventPreferencesWifi.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesBluetooth._enabled) {
            String desc = _eventPreferencesBluetooth.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesMobileCells._enabled) {
            String desc = _eventPreferencesMobileCells.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesPeripherals._enabled) {
            String desc = _eventPreferencesPeripherals.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesScreen._enabled) {
            String desc = _eventPreferencesScreen.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesNotification._enabled) {
            String desc = _eventPreferencesNotification.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesApplication._enabled) {
            String desc = _eventPreferencesApplication.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesOrientation._enabled) {
            String desc = _eventPreferencesOrientation.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesNFC._enabled) {
            String desc = _eventPreferencesNFC.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (_eventPreferencesAlarmClock._enabled) {
            String desc = _eventPreferencesAlarmClock.getPreferencesDescription(true, addPassStatus, context);
            if (desc != null)
                description = description + "<li>" + desc + "</li>";
        }

        if (!description.isEmpty())
            description = "<ul>" + description + "</ul>";

        return description;
    }

    public void checkPreferences(PreferenceManager prefMng, Context context) {
        _eventPreferencesTime.checkPreferences(prefMng, context);
        _eventPreferencesBattery.checkPreferences(prefMng, context);
        _eventPreferencesCall.checkPreferences(prefMng, context);
        _eventPreferencesPeripherals.checkPreferences(prefMng, context);
        _eventPreferencesCalendar.checkPreferences(prefMng, context);
        _eventPreferencesWifi.checkPreferences(prefMng, context);
        _eventPreferencesScreen.checkPreferences(prefMng, context);
        _eventPreferencesBluetooth.checkPreferences(prefMng, context);
        _eventPreferencesSMS.checkPreferences(prefMng, context);
        _eventPreferencesNotification.checkPreferences(prefMng, context);
        _eventPreferencesApplication.checkPreferences(prefMng, context);
        _eventPreferencesLocation.checkPreferences(prefMng, context);
        _eventPreferencesOrientation.checkPreferences(prefMng, context);
        _eventPreferencesMobileCells.checkPreferences(prefMng, context);
        _eventPreferencesNFC.checkPreferences(prefMng, context);
        _eventPreferencesRadioSwitch.checkPreferences(prefMng, context);
        _eventPreferencesAlarmClock.checkPreferences(prefMng, context);
    }

    /*
    private boolean canActivateReturnProfile()
    {
        return true;
    }
    */

    private int getEventTimelinePosition(List<EventTimeline> eventTimelineList)
    {
        boolean exists = false;
        int eventPosition = -1;
        for (EventTimeline eventTimeline : eventTimelineList)
        {
            eventPosition++;
            if (eventTimeline._fkEvent == this._id)
            {
                exists = true;
                break;
            }
        }
        if (exists)
            return eventPosition;
        else
            return -1;
    }

    private void addEventTimeline(DataWrapper dataWrapper,
                                            List<EventTimeline> eventTimelineList/*,
                                            Profile mergedProfile*/)
    {
        EventTimeline eventTimeline = new EventTimeline();
        eventTimeline._fkEvent = this._id;
        eventTimeline._eorder = 0;

        if (eventTimelineList.size() == 0)
        {
            Profile profile = dataWrapper.getActivatedProfile(false, false);
            if (profile != null)
                eventTimeline._fkProfileEndActivated = profile._id;
            else
                eventTimeline._fkProfileEndActivated = 0;
        }
        else
        {
            eventTimeline._fkProfileEndActivated = 0;
            EventTimeline _eventTimeline = eventTimelineList.get(eventTimelineList.size()-1);
            if (_eventTimeline != null)
            {
                Event event = dataWrapper.getEventById(_eventTimeline._fkEvent);
                if (event != null)
                    eventTimeline._fkProfileEndActivated = event._fkProfileStart;
            }
        }

        DatabaseHandler.getInstance(dataWrapper.context).addEventTimeline(eventTimeline);
        eventTimelineList.add(eventTimeline);
    }

    void startEvent(DataWrapper dataWrapper,
                            List<EventTimeline> eventTimelineList,
                            //boolean ignoreGlobalPref,
                            //boolean interactive,
                            boolean forRestartEvents,
                            //boolean log,
                            Profile mergedProfile)
    {
        // remove delay alarm
        removeDelayStartAlarm(dataWrapper); // for start delay
        removeDelayEndAlarm(dataWrapper); // for end delay
        removeStartEventNotificationAlarm(dataWrapper); // for start repeating notification

        if ((!getGlobalEventsRunning())/* && (!ignoreGlobalPref)*/)
            // events are globally stopped
            return;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("Event.startEvent", "event_id=" + this._id + "-----------------------------------");
            PPApplication.logE("Event.startEvent", "-- event_name=" + this._name);
        }*/

        if (!this.isRunnable(dataWrapper.context, true)) {
            // event is not runnable, no start it
            //PPApplication.logE("Event.startEvent","event is not runnable, no start it");
            return;
        }

        if (ApplicationPreferences.prefEventsBlocked)
        {
            // blocked by manual profile activation
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("Event.startEvent", "event_id=" + this._id + " events blocked");
                PPApplication.logE("Event.startEvent", "event_id=" + this._id + " forceRun=" + _forceRun);
                PPApplication.logE("Event.startEvent", "event_id=" + this._id + " blocked=" + _blocked);
            }*/

            if (!_forceRun)
                // event is not forceRun
                return;
            if (_blocked)
                // forceRun event is temporary blocked
                return;
        }

        // check activated profile
        if (!_startWhenActivatedProfile.isEmpty()) {
            Profile activatedProfile = dataWrapper.getActivatedProfile(false, false);
            if (activatedProfile != null) {
                boolean found = false;
                String[] splits = _startWhenActivatedProfile.split("\\|");
                for (String split : splits) {
                    if (activatedProfile._id == Long.valueOf(split)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // if activated profile is not _startWhenActivatedProfile, not start event
                    //PPApplication.logE("Event.startEvent","is not started _startWhenActivatedProfile");
                    return;
                }
            }
        }

        // search for running event with higher priority
        boolean applicationEventUsePriority = ApplicationPreferences.applicationEventUsePriority;
        for (EventTimeline eventTimeline : eventTimelineList)
        {
            Event event = dataWrapper.getEventById(eventTimeline._fkEvent);
            if ((event != null) && applicationEventUsePriority && (event._priority > this._priority)) {
                // is running event with higher priority
                //PPApplication.logE("Event.startEvent","is running event with higher priority");
                return;
            }
        }

        if (_forceRun)
            setForceRunEventRunning(dataWrapper.context, true);

        EventTimeline eventTimeline;

    /////// delete duplicate from timeline
        boolean exists = true;
        while (exists)
        {
            //exists = false;

            int timeLineSize = eventTimelineList.size();

            // test whenever event exists in timeline
            eventTimeline = null;
            int eventPosition = getEventTimelinePosition(eventTimelineList);
            //PPApplication.logE("Event.startEvent","eventPosition="+eventPosition);
            if (eventPosition != -1)
                eventTimeline = eventTimelineList.get(eventPosition);

            exists = eventPosition != -1;

            if (exists)
            {
                // remove event from timeline
                eventTimelineList.remove(eventTimeline);
                DatabaseHandler.getInstance(dataWrapper.context).deleteEventTimeline(eventTimeline);

                if (eventPosition < (timeLineSize-1))
                {
                    if (eventPosition > 0)
                    {
                        EventTimeline _eventTimeline = eventTimelineList.get(eventPosition-1);
                        Event event = dataWrapper.getEventById(_eventTimeline._fkEvent);
                        if (event != null)
                            eventTimelineList.get(eventPosition)._fkProfileEndActivated = event._fkProfileStart;
                        else
                            eventTimelineList.get(eventPosition)._fkProfileEndActivated = 0;
                    }
                    else
                    {
                        eventTimelineList.get(eventPosition)._fkProfileEndActivated = eventTimeline._fkProfileEndActivated;
                    }
                }

            }
        }
    //////////////////////////////////

        addEventTimeline(dataWrapper, eventTimelineList/*, mergedProfile*/);


        setSystemEvent(dataWrapper.context, ESTATUS_RUNNING);
        int status = this._status;
        this._status = ESTATUS_RUNNING;
        DatabaseHandler.getInstance(dataWrapper.context).updateEventStatus(this);

        if (/*log && */(status != this._status)) {
            PPApplication.addActivityLog(dataWrapper.context, PPApplication.ALTYPE_EVENT_START, _name, null, null, 0, "");
        }

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("Event.startEvent", "event=" + this._id + " activate profile id=" + this._fkProfileStart);
            PPApplication.logE("Event.startEvent", "mergedProfile=" + mergedProfile);
        }*/

        if (mergedProfile == null) {
            long activatedProfileId = 0;
            Profile activatedProfile = dataWrapper.getActivatedProfile(false, false);
            if (activatedProfile != null)
                activatedProfileId = activatedProfile._id;
            if (this._manualProfileActivation || forRestartEvents || (this._fkProfileStart != activatedProfileId))
                dataWrapper.activateProfileFromEvent(this._fkProfileStart, false, false, forRestartEvents);
            else {
                //PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from Event.startEvent");
                dataWrapper.updateNotificationAndWidgets(false, false);
            }
        }
        else {
            mergedProfile.mergeProfiles(this._fkProfileStart, dataWrapper/*, true*/);
            //PPApplication.logE("Event.startEvent","mergedProfile="+mergedProfile._name);
            if (this._manualProfileActivation) {
                DatabaseHandler.getInstance(dataWrapper.context).saveMergedProfile(mergedProfile);
                dataWrapper.activateProfileFromEvent(mergedProfile._id, true, true, forRestartEvents);
                mergedProfile._id = 0;
            }
        }

        //return;
    }

    private void doActivateEndProfile(DataWrapper dataWrapper,
                                        int eventPosition,
                                        int timeLineSize,
                                        List<EventTimeline> eventTimelineList,
                                        EventTimeline eventTimeline,
                                        boolean activateReturnProfile,
                                        Profile mergedProfile,
                                        boolean allowRestart,
                                        boolean forRestartEvents)
    {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("@@@ Event.pauseEvent", "doActivateEndProfile-activateReturnProfile=" + activateReturnProfile);
            PPApplication.logE("@@@ Event.pauseEvent", "doActivateEndProfile-allowRestart=" + allowRestart);
            PPApplication.logE("@@@ Event.pauseEvent", "doActivateEndProfile-forRestartEvents=" + forRestartEvents);
        }*/

        if (!(eventPosition == (timeLineSize-1)))
        {
            // event is not in end of timeline

            // check whether events behind have set _fkProfileEnd or _undoProfile
            // when true, no activate "end profile"
            /*for (int i = eventPosition; i < (timeLineSize-1); i++)
            {
                if (_fkProfileEnd != Event.PROFILE_END_NO_ACTIVATE)
                    return;
                if (_undoneProfile)
                    return;
            }*/
            return;
        }

        boolean profileActivated = false;
        if (activateReturnProfile/* && canActivateReturnProfile()*/)
        {
            if (mergedProfile == null) {
                Profile activatedProfile = dataWrapper.getActivatedProfile(false, false);
                long activatedProfileId = 0;
                if (activatedProfile != null)
                    activatedProfileId = activatedProfile._id;
                // first activate _fkProfileEnd
                if (_fkProfileEnd != Profile.PROFILE_NO_ACTIVATE)
                {
                    if ((_fkProfileEnd != activatedProfileId) || forRestartEvents)
                    {
                        //PPApplication.logE("@@@ Event.pauseEvent","doActivateEndProfile-activate end profile");
                        dataWrapper.activateProfileFromEvent(_fkProfileEnd, false, false, forRestartEvents);
                        activatedProfileId = _fkProfileEnd;
                        profileActivated = true;
                    }
                }
                // second activate when undone profile is set
                if (_atEndDo == EATENDDO_UNDONE_PROFILE)
                {
                    // when in timeline list is event, get start profile from last event in timeline list
                    // because last event in timeline list may be changed
                    if (eventTimelineList.size() > 0) {
                        EventTimeline _eventTimeline = eventTimelineList.get(eventTimelineList.size() - 1);
                        if (_eventTimeline != null) {
                            Event event = dataWrapper.getEventById(_eventTimeline._fkEvent);
                            if (event != null)
                                eventTimeline._fkProfileEndActivated = event._fkProfileStart;
                        }
                    }

                    if ((eventTimeline._fkProfileEndActivated != activatedProfileId) || forRestartEvents)
                    {
                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("@@@ Event.pauseEvent", "doActivateEndProfile-undone profile");
                            PPApplication.logE("@@@ Event.pauseEvent", "doActivateEndProfile-_fkProfileEndActivated=" + eventTimeline._fkProfileEndActivated);
                        }*/
                        if (eventTimeline._fkProfileEndActivated != 0)
                        {
                            dataWrapper.activateProfileFromEvent(eventTimeline._fkProfileEndActivated, false, false, forRestartEvents);
                            profileActivated = true;
                        }
                    }
                }
            }
            else {
                // first activate _fkProfileEnd
                if (_fkProfileEnd != Profile.PROFILE_NO_ACTIVATE)
                {
                    //PPApplication.logE("@@@ Event.pauseEvent","doActivateEndProfile-activate end profile");
                    mergedProfile.mergeProfiles(_fkProfileEnd, dataWrapper/*, false*/);
                }
                // second activate when undone profile is set
                if (_atEndDo == EATENDDO_UNDONE_PROFILE)
                {
                    // when in timeline list is event, get start profile from last event in timeline list
                    // because last event in timeline list may be changed
                    if (eventTimelineList.size() > 0) {
                        EventTimeline _eventTimeline = eventTimelineList.get(eventTimelineList.size() - 1);
                        if (_eventTimeline != null) {
                            Event event = dataWrapper.getEventById(_eventTimeline._fkEvent);
                            if (event != null)
                                eventTimeline._fkProfileEndActivated = event._fkProfileStart;
                        }
                    }
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("@@@ Event.pauseEvent", "doActivateEndProfile-undone profile");
                        PPApplication.logE("@@@ Event.pauseEvent", "doActivateEndProfile-_fkProfileEndActivated=" + eventTimeline._fkProfileEndActivated);
                    }*/
                    if (eventTimeline._fkProfileEndActivated != 0)
                    {
                        mergedProfile.mergeProfiles(eventTimeline._fkProfileEndActivated, dataWrapper/*, false*/);
                    }
                }
            }

            // restart events when is set
            if ((_atEndDo == EATENDDO_RESTART_EVENTS) && allowRestart && !forRestartEvents) {
                // test of forRestartEvents is required!!!
                // Do not restart events when is event paused during restart events !!!
                PPApplication.logE("@@@ Event.pauseEvent","doActivateEndProfile-restart events");
                // do not reactivate profile to avoid infinite loop
                dataWrapper.restartEventsWithDelay(5, false, true, /*true,*/ PPApplication.ALTYPE_UNDEFINED);
                profileActivated = true;
            }

        }

        if (!profileActivated)
        {
            //PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from Event.doActivateEndProfile");
            dataWrapper.updateNotificationAndWidgets(false, false);
        }

    }

    void pauseEvent(DataWrapper dataWrapper,
                            List<EventTimeline> eventTimelineList,
                            boolean activateReturnProfile,
                            boolean ignoreGlobalPref,
                            boolean noSetSystemEvent,
                            boolean log,
                            Profile mergedProfile,
                            boolean allowRestart,
                            boolean forRestartEvents)
    {
        // remove delay alarm
        removeDelayStartAlarm(dataWrapper); // for start delay
        removeDelayEndAlarm(dataWrapper); // for end delay
        removeStartEventNotificationAlarm(dataWrapper); // for start repeating notification

        if ((!getGlobalEventsRunning()) && (!ignoreGlobalPref))
            // events are globally stopped
            return;

        if (!this.isRunnable(dataWrapper.context, true))
            // event is not runnable, no pause it
            return;

/*		if (PPApplication.getEventsBlocked(dataWrapper.context))
        {
            // blocked by manual profile activation
            PPApplication.logE("Event.pauseEvent","event_id="+this._id+" events blocked");


            if (!_forceRun)
                // event is not forceRun
                return;
        }
*/

        // unblock event when paused
        dataWrapper.setEventBlocked(this, false);

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("@@@ Event.pauseEvent", "event_id=" + this._id + "-----------------------------------");
            PPApplication.logE("@@@ Event.pauseEvent", "-- event_name=" + this._name);
        }*/

        int timeLineSize = eventTimelineList.size();

        // test whenever event exists in timeline
        int eventPosition = getEventTimelinePosition(eventTimelineList);
        //PPApplication.logE("Event.pauseEvent","eventPosition="+eventPosition);

        boolean exists = eventPosition != -1;

        EventTimeline eventTimeline = null;

        if (exists)
        {
            eventTimeline = eventTimelineList.get(eventPosition);

            // remove event from timeline
            eventTimelineList.remove(eventTimeline);
            DatabaseHandler.getInstance(dataWrapper.context).deleteEventTimeline(eventTimeline);

            if (eventPosition < (timeLineSize-1)) // event is not in end of timeline and no only one event in timeline
            {
                if (eventPosition > 0)  // event is not in start of timeline
                {
                    // get event prior deleted event
                    EventTimeline _eventTimeline = eventTimelineList.get(eventPosition-1);
                    Event event = dataWrapper.getEventById(_eventTimeline._fkEvent);
                    // set _fkProfileEndActivated for event behind deleted event with _fkProfileStart of deleted event
                    if (event != null)
                        eventTimelineList.get(eventPosition)._fkProfileEndActivated = event._fkProfileStart;
                    else
                        eventTimelineList.get(eventPosition)._fkProfileEndActivated = 0;
                }
                else // event is in start of timeline
                {
                    // set _fkProfileEndActivated of first event with _fkProfileEndActivated of deleted event
                    eventTimelineList.get(eventPosition)._fkProfileEndActivated = eventTimeline._fkProfileEndActivated;
                }
            }
        }

        if (!noSetSystemEvent)
            setSystemEvent(dataWrapper.context, ESTATUS_PAUSE);
        int status = this._status;
        //PPApplication.logE("@@@ Event.pauseEvent","-- old status="+this._status);
        this._status = ESTATUS_PAUSE;
        //PPApplication.logE("@@@ Event.pauseEvent","-- new status="+this._status);
        DatabaseHandler.getInstance(dataWrapper.context).updateEventStatus(this);

        if (log && (status != this._status)) {
            doLogForPauseEvent(dataWrapper.context, allowRestart);
        }


        //if (_forceRun)
        //{ look for forceRun events always, not only when forceRun event is paused
            boolean forceRunRunning = false;
            for (EventTimeline _eventTimeline : eventTimelineList)
            {
                Event event = dataWrapper.getEventById(_eventTimeline._fkEvent);
                if ((event != null) && (event._forceRun))
                {
                    forceRunRunning = true;
                    break;
                }
            }

            if (!forceRunRunning)
                setForceRunEventRunning(dataWrapper.context, false);
        //}

        if (exists)
        {
            doActivateEndProfile(dataWrapper, eventPosition, timeLineSize,
                    eventTimelineList, eventTimeline,
                    activateReturnProfile, mergedProfile, allowRestart, forRestartEvents);

        }

        //return;
    }

    void doLogForPauseEvent(Context context, boolean allowRestart) {
        int alType = PPApplication.ALTYPE_EVENT_END_NONE;
        if ((_atEndDo == EATENDDO_UNDONE_PROFILE) && (_fkProfileEnd != Profile.PROFILE_NO_ACTIVATE))
            alType = PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE_UNDO_PROFILE;
        if ((_atEndDo == EATENDDO_RESTART_EVENTS) && (_fkProfileEnd != Profile.PROFILE_NO_ACTIVATE)) {
            if (allowRestart)
                alType = PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE_RESTART_EVENTS;
            else
                alType = PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE;
        }
        else if (_atEndDo == EATENDDO_UNDONE_PROFILE)
            alType = PPApplication.ALTYPE_EVENT_END_UNDO_PROFILE;
        else if (_atEndDo == EATENDDO_RESTART_EVENTS) {
            if (allowRestart)
                alType = PPApplication.ALTYPE_EVENT_END_RESTART_EVENTS;
        }
        else if (_fkProfileEnd != Profile.PROFILE_NO_ACTIVATE)
            alType = PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE;

        PPApplication.addActivityLog(context, alType, _name, null, null, 0, "");
    }

    void stopEvent(DataWrapper dataWrapper,
                            List<EventTimeline> eventTimelineList,
                            boolean activateReturnProfile,
                            boolean ignoreGlobalPref,
                            boolean saveEventStatus,
                            boolean log)
                            //boolean allowRestart)
    {
        // remove delay alarm
        removeDelayStartAlarm(dataWrapper); // for start delay
        removeDelayEndAlarm(dataWrapper); // for end delay
        removeStartEventNotificationAlarm(dataWrapper); // for start repeating notification

        if ((!getGlobalEventsRunning()) && (!ignoreGlobalPref))
            // events are globally stopped
            return;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("@@@ Event.stopEvent", "event_id=" + this._id + "-----------------------------------");
            PPApplication.logE("@@@ Event.stopEvent", "-- event_name=" + this._name);
        }*/

        if (this._status != ESTATUS_STOP)
        {
            pauseEvent(dataWrapper, eventTimelineList, activateReturnProfile, ignoreGlobalPref, true, false, null, false/*allowRestart*/, false);
        }

        setSystemEvent(dataWrapper.context, ESTATUS_STOP);
        int status = this._status;
        //PPApplication.logE("@@@ Event.stopEvent","-- old status="+this._status);
        this._status = ESTATUS_STOP;

        //PPApplication.logE("@@@ Event.stopEvent","-- new status="+this._status);
        if (saveEventStatus)
            DatabaseHandler.getInstance(dataWrapper.context).updateEventStatus(this);

        setSensorsWaiting();
        if (saveEventStatus)
            DatabaseHandler.getInstance(dataWrapper.context).updateAllEventSensorsPassed(this);

        if (log && (status != this._status)) {
            PPApplication.addActivityLog(dataWrapper.context, PPApplication.ALTYPE_EVENT_STOP, _name, null, null, 0, "");
        }

        //return;
    }

    public int getStatus()
    {
        return _status;
    }

    int getStatusFromDB(Context context)
    {
        return DatabaseHandler.getInstance(context).getEventStatus(this);
    }

    public void setStatus(int status)
    {
        _status = status;
    }

    void setSensorsWaiting() {
        //if (_eventPreferencesApplication._enabled)
            _eventPreferencesApplication.setSensorPassed(_eventPreferencesApplication.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesApplication.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesBattery._enabled)
            _eventPreferencesBattery.setSensorPassed(_eventPreferencesBattery.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesBattery.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesBluetooth._enabled)
            _eventPreferencesBluetooth.setSensorPassed(_eventPreferencesBluetooth.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesBluetooth.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesCalendar._enabled)
            _eventPreferencesCalendar.setSensorPassed(_eventPreferencesCalendar.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesCalendar.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesCall._enabled)
            _eventPreferencesCall.setSensorPassed(_eventPreferencesCall.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesCall.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesLocation._enabled)
            _eventPreferencesLocation.setSensorPassed(_eventPreferencesLocation.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesLocation.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesMobileCells._enabled)
            _eventPreferencesMobileCells.setSensorPassed(_eventPreferencesMobileCells.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesMobileCells.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesNFC._enabled)
            _eventPreferencesNFC.setSensorPassed(_eventPreferencesNFC.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesNFC.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesNotification._enabled)
            _eventPreferencesNotification.setSensorPassed(_eventPreferencesNotification.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesNotification.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesOrientation._enabled)
            _eventPreferencesOrientation.setSensorPassed(_eventPreferencesOrientation.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesOrientation.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesPeripherals._enabled)
            _eventPreferencesPeripherals.setSensorPassed(_eventPreferencesPeripherals.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesPeripherals.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesRadioSwitch._enabled)
            _eventPreferencesRadioSwitch.setSensorPassed(_eventPreferencesRadioSwitch.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesRadioSwitch.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesScreen._enabled)
            _eventPreferencesScreen.setSensorPassed(_eventPreferencesScreen.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesScreen.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesSMS._enabled)
            _eventPreferencesSMS.setSensorPassed(_eventPreferencesSMS.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesSMS.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesTime._enabled)
            _eventPreferencesTime.setSensorPassed(_eventPreferencesTime.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesTime.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesWifi._enabled)
            _eventPreferencesWifi.setSensorPassed(_eventPreferencesWifi.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesWifi.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);

        //if (_eventPreferencesAlarmClock._enabled)
            _eventPreferencesAlarmClock.setSensorPassed(_eventPreferencesAlarmClock.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        //else
        //    _eventPreferencesAlarmClock.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
    }

    private void setSystemEvent(Context context, int forStatus)
    {
        if (forStatus == ESTATUS_PAUSE)
        {
            // event paused
            // setup system event for next running status
            _eventPreferencesTime.setSystemEventForStart(context);
            _eventPreferencesBattery.setSystemEventForStart(context);
            _eventPreferencesCall.setSystemEventForStart(context);
            _eventPreferencesPeripherals.setSystemEventForStart(context);
            _eventPreferencesCalendar.setSystemEventForStart(context);
            _eventPreferencesWifi.setSystemEventForStart(context);
            _eventPreferencesScreen.setSystemEventForStart(context);
            _eventPreferencesBluetooth.setSystemEventForStart(context);
            _eventPreferencesSMS.setSystemEventForStart(context);
            _eventPreferencesNotification.setSystemEventForStart(context);
            _eventPreferencesApplication.setSystemEventForStart(context);
            _eventPreferencesLocation.setSystemEventForStart(context);
            _eventPreferencesOrientation.setSystemEventForStart(context);
            _eventPreferencesMobileCells.setSystemEventForStart(context);
            _eventPreferencesNFC.setSystemEventForStart(context);
            _eventPreferencesRadioSwitch.setSystemEventForStart(context);
            _eventPreferencesAlarmClock.setSystemEventForStart(context);
        }
        else
        if (forStatus == ESTATUS_RUNNING)
        {
            // event started
            // setup system event for pause status
            _eventPreferencesTime.setSystemEventForPause(context);
            _eventPreferencesBattery.setSystemEventForPause(context);
            _eventPreferencesCall.setSystemEventForPause(context);
            _eventPreferencesPeripherals.setSystemEventForPause(context);
            _eventPreferencesCalendar.setSystemEventForPause(context);
            _eventPreferencesWifi.setSystemEventForPause(context);
            _eventPreferencesScreen.setSystemEventForPause(context);
            _eventPreferencesBluetooth.setSystemEventForPause(context);
            _eventPreferencesSMS.setSystemEventForPause(context);
            _eventPreferencesNotification.setSystemEventForPause(context);
            _eventPreferencesApplication.setSystemEventForPause(context);
            _eventPreferencesLocation.setSystemEventForPause(context);
            _eventPreferencesOrientation.setSystemEventForPause(context);
            _eventPreferencesMobileCells.setSystemEventForPause(context);
            _eventPreferencesNFC.setSystemEventForPause(context);
            _eventPreferencesRadioSwitch.setSystemEventForPause(context);
            _eventPreferencesAlarmClock.setSystemEventForPause(context);
        }
        else
        if (forStatus == ESTATUS_STOP)
        {
            // event stopped
            // remove all system events
            _eventPreferencesTime.removeSystemEvent(context);
            _eventPreferencesBattery.removeSystemEvent(context);
            _eventPreferencesCall.removeSystemEvent(context);
            _eventPreferencesPeripherals.removeSystemEvent(context);
            _eventPreferencesCalendar.removeSystemEvent(context);
            _eventPreferencesWifi.removeSystemEvent(context);
            _eventPreferencesScreen.removeSystemEvent(context);
            _eventPreferencesBluetooth.removeSystemEvent(context);
            _eventPreferencesSMS.removeSystemEvent(context);
            _eventPreferencesNotification.removeSystemEvent(context);
            _eventPreferencesApplication.removeSystemEvent(context);
            _eventPreferencesLocation.removeSystemEvent(context);
            _eventPreferencesOrientation.removeSystemEvent(context);
            _eventPreferencesMobileCells.removeSystemEvent(context);
            _eventPreferencesNFC.removeSystemEvent(context);
            _eventPreferencesRadioSwitch.removeSystemEvent(context);
            _eventPreferencesAlarmClock.removeSystemEvent(context);
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    void setDelayStartAlarm(DataWrapper dataWrapper)
    {
        removeDelayStartAlarm(dataWrapper);

        if (!getGlobalEventsRunning())
            // events are globally stopped
            return;

        if (!this.isRunnable(dataWrapper.context, true))
            // event is not runnable, no pause it
            return;

        if (ApplicationPreferences.prefEventsBlocked)
        {
            // blocked by manual profile activation
            //PPApplication.logE("Event.setDelayStartAlarm","event_id="+this._id+" events blocked");


            if (!_forceRun)
                // event is not forceRun
                return;
            if (_blocked)
                // forceRun event is temporary blocked
                return;
        }

        if (getStatus() == ESTATUS_RUNNING)
            // event is already in running status
            return;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("@@@ Event.setDelayStartAlarm", "event_id=" + this._id + "-----------------------------------");
            PPApplication.logE("@@@ Event.setDelayStartAlarm", "-- event_name=" + this._name);
            PPApplication.logE("@@@ Event.setDelayStartAlarm", "-- delay=" + this._delayStart);
        }*/

        if (this._delayStart > 0)
        {
            Context _context = dataWrapper.context;

            // delay for start is > 0
            // set alarm

            if (ApplicationPreferences.applicationUseAlarmClock) {
                //Intent intent = new Intent(_context, EventDelayStartBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_EVENT_DELAY_START_BROADCAST_RECEIVER);
                //intent.setClass(context, EventDelayStartBroadcastReceiver.class);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, this._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, (int) this._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, this._delayStart);
                    long alarmTime = now.getTimeInMillis();

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String result = sdf.format(alarmTime);
                        PPApplication.logE("Event.setDelayStartAlarm", "startTime=" + result);
                    }*/

                    Intent editorIntent = new Intent(_context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(_context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);

                    now = Calendar.getInstance();
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    this._startStatusTime = now.getTimeInMillis() - gmtOffset;
                    this._isInDelayStart = true;
                }
                else {
                    this._startStatusTime = 0;
                    this._isInDelayStart = false;
                }
            }
            else {
                Data workData = new Data.Builder()
                        .putString(PhoneProfilesService.EXTRA_ELAPSED_ALARMS_WORK, ElapsedAlarmsWorker.ELAPSED_ALARMS_EVENT_DELAY_START)
                        .build();

                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(ElapsedAlarmsWorker.class)
                                .addTag("elapsedAlarmsEventDelayStartWork_"+(int) this._id)
                                .setInputData(workData)
                                .setInitialDelay(this._delayStart, TimeUnit.SECONDS)
                                .build();
                try {
                    WorkManager workManager = PPApplication.getWorkManagerInstance(_context);
                    //PPApplication.logE("[HANDLER] Event.setDelayStartAlarm", "enqueueUniqueWork - this._delayStart="+this._delayStart);
                    workManager.enqueue(worker);
                    PPApplication.elapsedAlarmsEventDelayStartWork.add("elapsedAlarmsEventDelayStartWork_"+(int) this._id);

                    Calendar now = Calendar.getInstance();
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    this._startStatusTime = now.getTimeInMillis() - gmtOffset;
                    this._isInDelayStart = true;
                } catch (Exception e) {
                    this._startStatusTime = 0;
                    this._isInDelayStart = false;
                }
            }

            /*//Intent intent = new Intent(_context, EventDelayStartBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_EVENT_DELAY_START_BROADCAST_RECEIVER);
            //intent.setClass(context, EventDelayStartBroadcastReceiver.class);

            //intent.putExtra(PPApplication.EXTRA_EVENT_ID, this._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, (int) this._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (ApplicationPreferences.applicationUseAlarmClock(_context)) {

                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, this._delayStart);
                    long alarmTime = now.getTimeInMillis();

                    if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String result = sdf.format(alarmTime);
                        PPApplication.logE("Event.setDelayStartAlarm", "startTime=" + result);
                    }

                    Intent editorIntent = new Intent(_context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(_context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
                else {
                    long alarmTime = SystemClock.elapsedRealtime() + this._delayStart * 1000;

                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    else
                        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                }

                Calendar now = Calendar.getInstance();
                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                this._startStatusTime = now.getTimeInMillis() - gmtOffset;

                this._isInDelayStart = true;
            }
            else {
                this._startStatusTime = 0;
                this._isInDelayStart = false;
            }*/
        }
        else {
            this._startStatusTime = 0;
            this._isInDelayStart = false;
        }

        DatabaseHandler.getInstance(dataWrapper.context).updateEventInDelayStart(this);

        if (_isInDelayStart) {
            PPApplication.addActivityLog(dataWrapper.context, PPApplication.ALTYPE_EVENT_START_DELAY, _name, null, null, _delayStart, "");
        }

        //return;
    }

    void checkDelayStart(/*DataWrapper dataWrapper*/) {
        if (this._startStatusTime == 0) {
            this._isInDelayStart = false;
            return;
        }

        Calendar now = Calendar.getInstance();
        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
        long nowTime = now.getTimeInMillis() - gmtOffset;
        now.add(Calendar.SECOND, this._delayStart);
        long delayTime = now.getTimeInMillis() - gmtOffset;

        if (nowTime > delayTime)
            this._isInDelayStart = false;
    }

    void removeDelayStartAlarm(DataWrapper dataWrapper)
    {
        Context _context = dataWrapper.context;
        try {
            AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {

                //Intent intent = new Intent(_context, EventDelayStartBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_EVENT_DELAY_START_BROADCAST_RECEIVER);
                //intent.setClass(context, EventDelayStartBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, (int) this._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    //PPApplication.logE("Event.removeDelayStartAlarm", "alarm found");

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception ignored) {}
        PhoneProfilesService.cancelWork("elapsedAlarmsEventDelayStartWork_"+((int) this._id), _context);
        PPApplication.elapsedAlarmsEventDelayStartWork.remove("elapsedAlarmsEventDelayStartWork_"+((int) this._id));

        this._isInDelayStart = false;
        this._startStatusTime = 0;
        DatabaseHandler.getInstance(dataWrapper.context).updateEventInDelayStart(this);
        //PPApplication.logE("[HANDLER] Event.removeDelayStartAlarm", "removed");
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    void setDelayEndAlarm(DataWrapper dataWrapper)
    {
        removeDelayEndAlarm(dataWrapper);

        if (!getGlobalEventsRunning())
            // events are globally stopped
            return;

        if (!this.isRunnable(dataWrapper.context, true))
            // event is not runnable, no pause it
            return;

        if (ApplicationPreferences.prefEventsBlocked)
        {
            // blocked by manual profile activation
            //PPApplication.logE("Event.setDelayEndAlarm","event_id="+this._id+" events blocked");


            if (!_forceRun)
                // event is not forceRun
                return;
            if (_blocked)
                // forceRun event is temporary blocked
                return;
        }

        if (getStatus() == ESTATUS_PAUSE)
            // event is already in pause status
            return;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("@@@ Event.setDelayEndAlarm", "event_id=" + this._id + "-----------------------------------");
            PPApplication.logE("@@@ Event.setDelayEndAlarm", "-- event_name=" + this._name);
            PPApplication.logE("@@@ Event.setDelayEndAlarm", "-- delay=" + this._delayEnd);
        }*/

        if (this._delayEnd > 0)
        {
            Context _context = dataWrapper.context;

            // delay for end is > 0
            // set alarm

            if (ApplicationPreferences.applicationUseAlarmClock) {
                //Intent intent = new Intent(_context, EventDelayEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_EVENT_DELAY_END_BROADCAST_RECEIVER);
                //intent.setClass(context, EventDelayEndBroadcastReceiver.class);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, this._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, (int) this._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, this._delayEnd);
                    long alarmTime = now.getTimeInMillis(); // + 1000 * /* 60 * */ this._delayEnd;

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String result = sdf.format(alarmTime);
                        PPApplication.logE("Event.setDelayEndAlarm", "endTime=" + result);
                    }*/

                    Intent editorIntent = new Intent(_context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(_context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);

                    now = Calendar.getInstance();
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    this._pauseStatusTime = now.getTimeInMillis() - gmtOffset;
                    this._isInDelayEnd = true;
                }
                else {
                    this._pauseStatusTime = 0;
                    this._isInDelayEnd = false;
                }
            }
            else {
                Data workData = new Data.Builder()
                        .putString(PhoneProfilesService.EXTRA_ELAPSED_ALARMS_WORK, ElapsedAlarmsWorker.ELAPSED_ALARMS_EVENT_DELAY_END)
                        .build();

                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(ElapsedAlarmsWorker.class)
                                .addTag("elapsedAlarmsEventDelayEndWork_"+(int) this._id)
                                .setInputData(workData)
                                .setInitialDelay(this._delayEnd, TimeUnit.SECONDS)
                                .build();
                try {
                    WorkManager workManager = PPApplication.getWorkManagerInstance(_context);
                    //PPApplication.logE("[HANDLER] Event.setDelayEndAlarm", "enqueueUniqueWork - this._delayEnd="+this._delayEnd);
                    workManager.enqueue(worker);
                    PPApplication.elapsedAlarmsEventDelayEndWork.add("elapsedAlarmsEventDelayEndWork_"+(int) this._id);

                    Calendar now = Calendar.getInstance();
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    this._pauseStatusTime = now.getTimeInMillis() - gmtOffset;
                    this._isInDelayEnd = true;
                } catch (Exception e) {
                    this._pauseStatusTime = 0;
                    this._isInDelayEnd = false;
                }
            }

            /*//Intent intent = new Intent(_context, EventDelayEndBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_EVENT_DELAY_END_BROADCAST_RECEIVER);
            //intent.setClass(context, EventDelayEndBroadcastReceiver.class);

            //intent.putExtra(PPApplication.EXTRA_EVENT_ID, this._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, (int) this._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                if (ApplicationPreferences.applicationUseAlarmClock(_context)) {

                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, this._delayEnd);
                    long alarmTime = now.getTimeInMillis();

                    if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String result = sdf.format(alarmTime);
                        PPApplication.logE("Event.setDelayEndAlarm", "endTime=" + result);
                    }

                    Intent editorIntent = new Intent(_context, EditorProfilesActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(_context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
                else {
                    long alarmTime = SystemClock.elapsedRealtime() + this._delayEnd * 1000;

                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    else
                        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                }

                Calendar now = Calendar.getInstance();
                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                this._pauseStatusTime = now.getTimeInMillis() - gmtOffset;

                this._isInDelayEnd = true;
            }
            else {
                this._pauseStatusTime = 0;
                this._isInDelayEnd = false;
            }*/
        }
        else {
            this._pauseStatusTime = 0;
            this._isInDelayEnd = false;
        }

        DatabaseHandler.getInstance(dataWrapper.context).updateEventInDelayEnd(this);

        if (_isInDelayEnd) {
            PPApplication.addActivityLog(dataWrapper.context, PPApplication.ALTYPE_EVENT_END_DELAY, _name, null, null, _delayEnd, "");
        }

        //return;
    }

    void checkDelayEnd(/*DataWrapper dataWrapper*/) {
        //PPApplication.logE("Event.checkDelayEnd","this._pauseStatusTime="+this._pauseStatusTime);
        //PPApplication.logE("Event.checkDelayEnd","this._isInDelayEnd="+this._isInDelayEnd);
        //PPApplication.logE("Event.checkDelayEnd","this._delayEnd="+this._delayEnd);

        if (this._pauseStatusTime == 0) {
            this._isInDelayEnd = false;
            return;
        }

        Calendar now = Calendar.getInstance();
        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
        long nowTime = now.getTimeInMillis() - gmtOffset;
        now.add(Calendar.SECOND, this._delayEnd);
        long delayTime = now.getTimeInMillis() - gmtOffset;

        /*
        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");

        String result = sdf.format(nowTime);
        PPApplication.logE("Event.checkDelayEnd","nowTime="+result);

        result = sdf.format(this._pauseStatusTime);
        PPApplication.logE("Event.checkDelayEnd","pauseStatusTime="+result);

        result = sdf.format(delayTime);
        PPApplication.logE("Event.checkDelayEnd","delayTime="+result);
        */

        if (nowTime > delayTime)
            this._isInDelayEnd = false;

        //PPApplication.logE("Event.checkDelayEnd","this._isInDelayEnd="+this._isInDelayEnd);
    }

    void removeDelayEndAlarm(DataWrapper dataWrapper)
    {
        Context _context = dataWrapper.context;
        try {
            AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(_context, EventDelayEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_EVENT_DELAY_END_BROADCAST_RECEIVER);
                //intent.setClass(context, EventDelayEndBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, (int) this._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    //PPApplication.logE("Event.removeDelayEndAlarm", "alarm found");

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception ignored) {}
        PhoneProfilesService.cancelWork("elapsedAlarmsEventDelayEndWork_"+((int) this._id), _context);
        PPApplication.elapsedAlarmsEventDelayEndWork.remove("elapsedAlarmsEventDelayEndWork_"+((int) this._id));

        this._isInDelayEnd = false;
        DatabaseHandler.getInstance(dataWrapper.context).updateEventInDelayEnd(this);
        //PPApplication.logE("[HANDLER] Event.removeDelayEndAlarm", "removed");
    }

    private void removeStartEventNotificationAlarm(DataWrapper dataWrapper) {
        if (_repeatNotificationStart) {
            /*boolean clearNotification = true;
            for (int i = eventTimelineList.size()-1; i > 0; i--)
            {
                EventTimeline _eventTimeline = eventTimelineList.get(i);
                Event event = dataWrapper.getEventById(_eventTimeline._fkEvent);
                if ((event != null) && (event._repeatNotificationStart) && (event._id != this._id)) {
                    // not clear, notification is from another event
                    clearNotification = false;
                    break;
                }
            }
            if (clearNotification) {*/
                NotificationManager notificationManager = (NotificationManager) dataWrapper.context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    int notificationID = -(99999999 + (int) _id);
                    notificationManager.cancel(notificationID);
                }
                StartEventNotificationBroadcastReceiver.removeAlarm(this, dataWrapper.context);
            //}
        }
    }

    static PreferenceAllowed isEventPreferenceAllowed(String preferenceKey, Context context)
    {
        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();

        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;

        boolean checked = false;

        if (preferenceKey.equals(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_WIFI))
                // device has Wifi
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked)
            return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_BLUETOOTH))
                // device has bluetooth
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked)
            return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED))
        {
            //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            /*else {
                PPApplication.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
                PPApplication.notAllowedReasonDetail = context.getString(R.string.preference_not_allowed_reason_detail_old_android);
            }*/
            checked = true;
        }
        if (checked)
            return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED))
        {
            //if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext()))
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            //else
            //    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_EXTENDER_INSTALLED;
            checked = true;
        }
        if (checked)
            return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED))
        {
            boolean hasAccelerometer = PPApplication.accelerometerSensor != null;
            //boolean hasMagneticField = PPApplication.magneticFieldSensor != null;
            boolean hasProximity = PPApplication.proximitySensor != null;
            boolean hasLight = PPApplication.lightSensor != null;

            boolean enabled = hasAccelerometer;
            enabled = enabled || hasProximity || hasLight;

            if (enabled) {
                //if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext()))
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                //else
                //    PPApplication.notAllowedReason = PPApplication.PREFERENCE_NOT_ALLOWED_NO_EXTENDER_INSTALLED;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked)
            return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED) ||
                preferenceKey.equals(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED_NO_CHECK_SIM))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY)) {
                // device has telephony
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    if (preferenceKey.equals(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED)) {
                        if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY)
                            // sim card is ready
                            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else
                            preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                    }
                    else
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked)
            return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_NFC))
                // device has nfc
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked)
            return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED) ||
                preferenceKey.equals(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED_NO_CHECK_SIM))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY)) {
                // device has telephony
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    if (preferenceKey.equals(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED)) {
                        if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY)
                            // sim card is ready
                            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else
                            preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                    }
                    else
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked)
            return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesCall.PREF_EVENT_CALL_ENABLED) ||
                preferenceKey.equals(EventPreferencesCall.PREF_EVENT_CALL_ENABLED_NO_CHECK_SIM))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY)) {
                // device has telephony
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    if (preferenceKey.equals(EventPreferencesCall.PREF_EVENT_CALL_ENABLED)) {
                        if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY)
                            // sim card is ready
                            preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                        else
                            preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD;
                    }
                    else
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
                }
                else
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            }
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked)
            return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED))
        {
            if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_LOCATION))
                // device has location
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            else
                preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
            checked = true;
        }
        if (checked)
            return preferenceAllowed;

        if (preferenceKey.equals(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED))
        {
            //if (android.os.Build.VERSION.SDK_INT >= 21)
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            //else
            //    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SUPPORTED_BY_SYSTEM;
            checked = true;
        }
        if (checked)
            return preferenceAllowed;

        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;

        return preferenceAllowed;
    }

    static boolean getGlobalEventsRunning()
    {
        synchronized (PPApplication.globalEventsRunStopMutex) {
            return PPApplication.globalEventsRunStop;
        }
    }

    static void setGlobalEventsRunning(Context context, boolean globalEventsRunning)
    {
        synchronized (PPApplication.globalEventsRunStopMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_GLOBAL_EVENTS_RUN_STOP, globalEventsRunning);
            editor.apply();
            PPApplication.globalEventsRunStop = globalEventsRunning;
        }
    }

    static void getEventsBlocked(Context context)
    {
        synchronized (PPApplication.eventsRunMutex) {
            ApplicationPreferences.prefEventsBlocked = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_EVENTS_BLOCKED, false);
            //return prefEventsBlocked;
        }
    }
    static void setEventsBlocked(Context context, boolean eventsBlocked)
    {
        synchronized (PPApplication.eventsRunMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_EVENTS_BLOCKED, eventsBlocked);
            editor.apply();
            ApplicationPreferences.prefEventsBlocked = eventsBlocked;
        }
    }

    static void getForceRunEventRunning(Context context)
    {
        synchronized (PPApplication.eventsRunMutex) {
            ApplicationPreferences.prefForceRunEventRunning = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_FORCE_RUN_EVENT_RUNNING, false);
            //return prefForceRunEventRunning;
        }
    }
    static void setForceRunEventRunning(Context context, boolean forceRunEventRunning)
    {
        synchronized (PPApplication.eventsRunMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_FORCE_RUN_EVENT_RUNNING, forceRunEventRunning);
            editor.apply();
            ApplicationPreferences.prefForceRunEventRunning = forceRunEventRunning;
        }
    }

    //----------------------------------

    boolean notifyEventStart(Context context, boolean playSound) {
        String notificationSoundStart = _notificationSoundStart;
        boolean notificationVibrateStart = _notificationVibrateStart;

        if (!notificationSoundStart.isEmpty() || notificationVibrateStart) {

            //PPApplication.logE("Event.notifyEventStart", "event._id="+_id);

            if (_repeatNotificationStart) {
                NotificationCompat.Builder mBuilder;

                String nTitle = context.getString(R.string.start_event_notification_title);
                String nText = context.getString(R.string.start_event_notification_text1);
                nText = nText + ": " + _name;
                nText = nText + ". " + context.getString(R.string.start_event_notification_text2);
                if (android.os.Build.VERSION.SDK_INT < 24) {
                    nTitle = context.getString(R.string.app_name);
                    nText = context.getString(R.string.start_event_notification_title) + ": " + nText;
                }
                PPApplication.createNotifyEventStartNotificationChannel(context);
                mBuilder = new NotificationCompat.Builder(context, PPApplication.NOTIFY_EVENT_START_NOTIFICATION_CHANNEL)
                        .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                        .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                        .setContentTitle(nTitle) // title for notification
                        .setContentText(nText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                        .setAutoCancel(false); // clear notification after click

                PendingIntent pi = PendingIntent.getActivity(context, (int) _id, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pi);
                mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                //if (android.os.Build.VERSION.SDK_INT >= 21) {
                    mBuilder.setCategory(NotificationCompat.CATEGORY_EVENT);
                    mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                //}

                Intent deleteIntent = new Intent(StartEventNotificationDeletedReceiver.START_EVENT_NOTIFICATION_DELETED_ACTION);
                deleteIntent.putExtra(PPApplication.EXTRA_EVENT_ID, _id);
                PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, (int) _id, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setDeleteIntent(deletePendingIntent);

                Notification notification = mBuilder.build();
                notification.sound = null;
                notification.vibrate = null;
                notification.defaults &= ~DEFAULT_SOUND;
                notification.defaults &= ~DEFAULT_VIBRATE;

                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotificationManager != null) {
                    int notificationID = -(99999999 + (int) _id);
                    mNotificationManager.notify(notificationID, notification);
                }

                StartEventNotificationBroadcastReceiver.setAlarm(this, context);
            }

            if (playSound)
                if (PhoneProfilesService.getInstance() != null)
                    PhoneProfilesService.getInstance().playNotificationSound(notificationSoundStart, notificationVibrateStart);

            return true;
        }
        return false;
    }

    boolean notifyEventEnd(/*Context context*/ boolean playSound) {
        String notificationSoundEnd = _notificationSoundEnd;
        boolean notificationVibrateEnd = _notificationVibrateEnd;

        if (!notificationSoundEnd.isEmpty() || notificationVibrateEnd) {

            if (playSound)
                if (PhoneProfilesService.getInstance() != null)
                    PhoneProfilesService.getInstance().playNotificationSound(notificationSoundEnd, notificationVibrateEnd);

            return true;
        }
        return false;
    }

}

