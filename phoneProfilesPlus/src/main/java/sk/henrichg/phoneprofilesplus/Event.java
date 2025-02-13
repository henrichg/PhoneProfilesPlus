package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.Spanned;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** @noinspection ExtractMethodRecommender*/
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
    boolean _ignoreManualActivation;
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
    //int _atEndHowUndo;
    boolean _manualProfileActivationAtEnd;
    boolean _notificationSoundStartPlayAlsoInSilentMode;
    boolean _notificationSoundEndPlayAlsoInSilentMode;

    //boolean _undoCalled;

    EventPreferencesTime _eventPreferencesTime;
    EventPreferencesBattery _eventPreferencesBattery;
    EventPreferencesCall _eventPreferencesCall;
    EventPreferencesAccessories _eventPreferencesAccessories;
    EventPreferencesCalendar _eventPreferencesCalendar;
    EventPreferencesWifi _eventPreferencesWifi;
    EventPreferencesScreen _eventPreferencesScreen;
    EventPreferencesBrightness _eventPreferencesBrightness;
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
    EventPreferencesDeviceBoot _eventPreferencesDeviceBoot;
    EventPreferencesSoundProfile _eventPreferencesSoundProfile;
    EventPreferencesPeriodic _eventPreferencesPeriodic;
    EventPreferencesVolumes _eventPreferencesVolumes;
    EventPreferencesActivatedProfile _eventPreferencesActivatedProfile;
    EventPreferencesRoaming _eventPreferencesRoaming;
    EventPreferencesVPN _eventPreferencesVPN;
    EventPreferencesMusic _eventPreferencesMusic;
    EventPreferencesCallScreening _eventPreferencesCallScreening;

    Spanned _peferencesDecription;

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
    static final int EPRIORITY_DO_NOT_USE = 99;

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
    private static final String PREF_EVENT_IGNORE_MANUAL_ACTIVATION = "eventForceRun";
    //static final String PREF_EVENT_UNDONE_PROFILE = "eventUndoneProfile";
    static final String PREF_EVENT_PRIORITY_APP_SETTINGS = "eventUsePriorityAppSettings";
    static final String PREF_EVENT_PRIORITY = "eventPriority";
    private static final String PREF_EVENT_DELAY_START = "eventDelayStart";
    private static final String PREF_EVENT_AT_END_DO = "eventAtEndDo";
    private static final String PREF_EVENT_MANUAL_PROFILE_ACTIVATION = "manualProfileActivation";
    private static final String PREF_EVENT_START_WHEN_ACTIVATED_PROFILE = "eventStartWhenActivatedProfile";
    private static final String PREF_EVENT_DELAY_END = "eventDelayEnd";
    private static final String PREF_EVENT_NO_PAUSE_BY_MANUAL_ACTIVATION = "eventNoPauseByManualActivation";
    //private static final String PREF_EVENT_AT_END_HOW_UNDO = "eventAtEndHowUndo";
    private static final String PREF_EVENT_MANUAL_PROFILE_ACTIVATION_AT_END = "manualProfileActivationAtEnd";
    private static final String PREF_EVENT_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE = "eventStartNotificationSoundPlayAlsoInSilentMode";
    private static final String PREF_EVENT_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE = "eventEndNotificationSoundPlayAlsoInSilentMode";
    private static final String PREF_EVENT_START_OTHERS_CATEGORY_ROOT = "eventStartOthersCategoryRoot";
    private static final String PREF_EVENT_END_OTHERS_CATEGORY_ROOT = "eventEndOthersCategoryRoot";

    static final String PREF_GLOBAL_EVENTS_RUN_STOP = "globalEventsRunStop";
    static final String PREF_EVENTS_BLOCKED = "eventsBlocked";
    static final String PREF_FORCE_RUN_EVENT_RUNNING = "forceRunEventRunning";

    // alarm time offset (milliseconds) for events with generated alarms
    static final int EVENT_ALARM_TIME_OFFSET = 15000;
    static final int EVENT_ALARM_TIME_SOFT_OFFSET = 5000;

    // Empty constructor
    Event(){}

    // constructor
    Event(long id,
                 String name,
                 int startOrder,
                 long fkProfileStart,
                 long fkProfileEnd,
                 int status,
                 String notificationSoundStart,
                 boolean ignoreManualActivation,
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
                 boolean notificationVibrateEnd,
                 //int atEndHowUndo,
                 boolean manualProfileActivationAtEnd,
                 boolean notificationSoundStartPlayAlsoInSilentMode,
                 boolean notificationSoundEndPlayAlsoInSilentMode
          )
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
        this._ignoreManualActivation = ignoreManualActivation;
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
        //this._atEndHowUndo = atEndHowUndo;
        this._manualProfileActivationAtEnd = manualProfileActivationAtEnd;
        this._notificationSoundStartPlayAlsoInSilentMode = notificationSoundStartPlayAlsoInSilentMode;
        this._notificationSoundEndPlayAlsoInSilentMode = notificationSoundEndPlayAlsoInSilentMode;

        //this._undoCalled = false;

        createEventPreferences();
    }

    // constructor
    Event(String name,
                 int startOrder,
                 long fkProfileStart,
                 long fkProfileEnd,
                 int status,
                 String notificationSoundStart,
                 boolean ignoreManualActivation,
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
                 boolean notificationVibrateEnd,
                 //int atEndHowUndo
                 boolean manualProfileActivationAtEnd,
                 boolean notificationSoundStartPlayAlsoInSilentMode,
                 boolean notificationSoundEndPlayAlsoInSilentMode
        )
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
        this._ignoreManualActivation = ignoreManualActivation;
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
        //this._atEndHowUndo = atEndHowUndo;
        this._manualProfileActivationAtEnd = manualProfileActivationAtEnd;
        this._notificationSoundStartPlayAlsoInSilentMode = notificationSoundStartPlayAlsoInSilentMode;
        this._notificationSoundEndPlayAlsoInSilentMode = notificationSoundEndPlayAlsoInSilentMode;

        //this._undoCalled = false;

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
        this._ignoreManualActivation = event._ignoreManualActivation;
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
        //this._atEndHowUndo = event._atEndHowUndo;
        this._manualProfileActivationAtEnd = event._manualProfileActivationAtEnd;
        this._notificationSoundStartPlayAlsoInSilentMode = event._notificationSoundStartPlayAlsoInSilentMode;
        this._notificationSoundEndPlayAlsoInSilentMode = event._notificationSoundEndPlayAlsoInSilentMode;

        this._peferencesDecription = event._peferencesDecription;

        //this._undoCalled = event._undoCalled;

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
        this._eventPreferencesCall = new EventPreferencesCall(this, false, 0, "", "", 0, false, 5, 0, false, "");
    }

    private void createEventPreferencesAccessories()
    {
        this._eventPreferencesAccessories = new EventPreferencesAccessories(this, false, "");
    }

    private void createEventPreferencesCalendar()
    {
        this._eventPreferencesCalendar = new EventPreferencesCalendar(this, false, "", false,0, "", 0, 0, /*false,*/ 0, 0, 0);
    }

    private void createEventPreferencesWiFi()
    {
        this._eventPreferencesWifi = new EventPreferencesWifi(this, false, "", 1);
    }

    private void createEventPreferencesScreen()
    {
        this._eventPreferencesScreen = new EventPreferencesScreen(this, false, 1, false);
    }

    private void createEventPreferencesBrightness()
    {
        this._eventPreferencesBrightness = new EventPreferencesBrightness(this, false, 0, "50|0|1|0", 0, "50|0|1|0");
    }


    private void createEventPreferencesBluetooth()
    {
        this._eventPreferencesBluetooth = new EventPreferencesBluetooth(this, false, "", 1/*, 0*/);
    }

    private void createEventPreferencesSMS()
    {
        this._eventPreferencesSMS = new EventPreferencesSMS(this, false, "", "", 0, false, 5, 0);
    }

    private void createEventPreferencesNotification()
    {
        this._eventPreferencesNotification = new EventPreferencesNotification(this, false, "", false, false, 0, false, "", "", false, "", 0);
    }

    private void createEventPreferencesApplication()
    {
        this._eventPreferencesApplication = new EventPreferencesApplication(this, false, "", 0);
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
        this._eventPreferencesMobileCells = new EventPreferencesMobileCells(this, false, "", false, 0);
    }

    private void createEventPreferencesNFC()
    {
        this._eventPreferencesNFC = new EventPreferencesNFC(this, false, "", true, 5);
    }

    private void createEventPreferencesRadioSwitch()
    {
        this._eventPreferencesRadioSwitch = new EventPreferencesRadioSwitch(this, false, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    private void createEventPreferencesAlarmClock()
    {
        this._eventPreferencesAlarmClock = new EventPreferencesAlarmClock(this, false, false, 5, "");
    }

    private void createEventPreferencesDeviceBoot()
    {
        this._eventPreferencesDeviceBoot = new EventPreferencesDeviceBoot(this, false, false, 5);
    }

    private void createEventPreferencesSoundProfile()
    {
        this._eventPreferencesSoundProfile = new EventPreferencesSoundProfile(this, false, "", "");
    }

    private void createEventPreferencesPeriodic()
    {
        this._eventPreferencesPeriodic = new EventPreferencesPeriodic(this, false, 1, 5);
    }

    private void createEventPreferencesVolumes()
    {
        this._eventPreferencesVolumes = new EventPreferencesVolumes(this, false,
                "0|0|0",
                "0|0|0",
                "0|0|0",
                "0|0|0",
                "0|0|0",
                "0|0|0",
                "0|0|0",
                "0|0|0",
                "0|0|0",
                "0|0|0",
                "0|0|0",
                "0|0|0",
                "0|0|0",
                "0|0|0"
        );
    }

    private void createEventPreferencesActivatedProfile()
    {
        this._eventPreferencesActivatedProfile = new EventPreferencesActivatedProfile(this, false, 0, 0);
    }

    private void createEventPreferencesRoaming()
    {
        this._eventPreferencesRoaming = new EventPreferencesRoaming(this, false, false, false, 0);
    }

    private void createEventPreferencesVPN()
    {
        this._eventPreferencesVPN = new EventPreferencesVPN(this, false, 0);
    }

    private void createEventPreferencesMusic()
    {
        this._eventPreferencesMusic = new EventPreferencesMusic(this, false, 0, "");
    }

    void createEventPreferencesCallScreening()
    {
        this._eventPreferencesCallScreening = new EventPreferencesCallScreening(this, false, 0, "", "", /*0,*/false, false, false, "", false, 5);
    }

    void createEventPreferences()
    {
        createEventPreferencesTime();
        createEventPreferencesBattery();
        createEventPreferencesCall();
        createEventPreferencesAccessories();
        createEventPreferencesCalendar();
        createEventPreferencesWiFi();
        createEventPreferencesScreen();
        createEventPreferencesBrightness();
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
        createEventPreferencesDeviceBoot();
        createEventPreferencesSoundProfile();
        createEventPreferencesPeriodic();
        createEventPreferencesVolumes();
        createEventPreferencesActivatedProfile();
        createEventPreferencesRoaming();
        createEventPreferencesVPN();
        createEventPreferencesMusic();
        createEventPreferencesCallScreening();
    }

    void copyEventPreferences(Event fromEvent)
    {
        if (this._eventPreferencesTime == null)
            createEventPreferencesTime();
        if (this._eventPreferencesBattery == null)
            createEventPreferencesBattery();
        if (this._eventPreferencesCall == null)
            createEventPreferencesCall();
        if (this._eventPreferencesAccessories == null)
            createEventPreferencesAccessories();
        if (this._eventPreferencesCalendar == null)
            createEventPreferencesCalendar();
        if (this._eventPreferencesWifi == null)
            createEventPreferencesWiFi();
        if (this._eventPreferencesScreen == null)
            createEventPreferencesScreen();
        if (this._eventPreferencesBrightness == null)
            createEventPreferencesBrightness();
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
        if (this._eventPreferencesDeviceBoot == null)
            createEventPreferencesDeviceBoot();
        if (this._eventPreferencesSoundProfile == null)
            createEventPreferencesSoundProfile();
        if (this._eventPreferencesPeriodic == null)
            createEventPreferencesPeriodic();
        if (this._eventPreferencesVolumes == null)
            createEventPreferencesVolumes();
        if (this._eventPreferencesActivatedProfile == null)
            createEventPreferencesActivatedProfile();
        if (this._eventPreferencesRoaming == null)
            createEventPreferencesRoaming();
        if (this._eventPreferencesVPN == null)
            createEventPreferencesVPN();
        if (this._eventPreferencesMusic == null)
            createEventPreferencesMusic();
        if (this._eventPreferencesCallScreening == null)
            createEventPreferencesCallScreening();
        this._eventPreferencesTime.copyPreferences(fromEvent);
        this._eventPreferencesBattery.copyPreferences(fromEvent);
        this._eventPreferencesCall.copyPreferences(fromEvent);
        this._eventPreferencesAccessories.copyPreferences(fromEvent);
        this._eventPreferencesCalendar.copyPreferences(fromEvent);
        this._eventPreferencesWifi.copyPreferences(fromEvent);
        this._eventPreferencesScreen.copyPreferences(fromEvent);
        this._eventPreferencesBrightness.copyPreferences(fromEvent);
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
        this._eventPreferencesDeviceBoot.copyPreferences(fromEvent);
        this._eventPreferencesSoundProfile.copyPreferences(fromEvent);
        this._eventPreferencesPeriodic.copyPreferences(fromEvent);
        this._eventPreferencesVolumes.copyPreferences(fromEvent);
        this._eventPreferencesActivatedProfile.copyPreferences(fromEvent);
        this._eventPreferencesRoaming.copyPreferences(fromEvent);
        this._eventPreferencesVPN.copyPreferences(fromEvent);
        this._eventPreferencesMusic.copyPreferences(fromEvent);
        this._eventPreferencesCallScreening.copyPreferences(fromEvent);
    }

    boolean isEnabledSomeSensor(Context context) {
        Context appContext = context.getApplicationContext();
        return  (this._eventPreferencesTime._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesTime.PREF_EVENT_TIME_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesBattery._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesCall._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesAccessories._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesAccessories.PREF_EVENT_ACCESSORIES_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesCalendar._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesWifi._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesScreen._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesScreen.PREF_EVENT_SCREEN_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesBrightness._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesBrightness.PREF_EVENT_BRIGHTNESS_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesBluetooth._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesSMS._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesNotification._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesApplication._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesLocation._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesOrientation._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesMobileCells._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesNFC._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesRadioSwitch._enabled && (this._eventPreferencesRadioSwitch._wifi != 0) &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_WIFI, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesRadioSwitch._enabled && (this._eventPreferencesRadioSwitch._bluetooth != 0) &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_BLUETOOTH, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesRadioSwitch._enabled && (this._eventPreferencesRadioSwitch._simOnOff != 0) &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_SIM_ON_OFF, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesRadioSwitch._enabled && (this._eventPreferencesRadioSwitch._defaultSIMForCalls != 0) &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_DEFAULT_SIM, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesRadioSwitch._enabled && (this._eventPreferencesRadioSwitch._defaultSIMForSMS != 0) &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_DEFAULT_SIM, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesRadioSwitch._enabled && (this._eventPreferencesRadioSwitch._mobileData != 0) &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_MOBILE_DATA, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesRadioSwitch._enabled && (this._eventPreferencesRadioSwitch._gps != 0) &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_GPS, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesRadioSwitch._enabled && (this._eventPreferencesRadioSwitch._nfc != 0) &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_NFC, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesRadioSwitch._enabled && (this._eventPreferencesRadioSwitch._airplaneMode != 0) &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED_AIRPLANE_MODE, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesAlarmClock._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesDeviceBoot._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesDeviceBoot.PREF_EVENT_DEVICE_BOOT_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesSoundProfile._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesSoundProfile.PREF_EVENT_SOUND_PROFILE_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesPeriodic._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesPeriodic.PREF_EVENT_PERIODIC_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesVolumes._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesVolumes.PREF_EVENT_VOLUMES_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesActivatedProfile._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesActivatedProfile.PREF_EVENT_ACTIVATED_PROFILE_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesRoaming._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesRoaming.PREF_EVENT_ROAMING_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesVPN._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesVPN.PREF_EVENT_VPN_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesMusic._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesMusic.PREF_EVENT_MUSIC_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED)) ||
                (this._eventPreferencesCallScreening._enabled &&
                        (EventStatic.isEventPreferenceAllowed(EventPreferencesCallScreening.PREF_EVENT_CALL_SCREENING_ENABLED, false, appContext).preferenceAllowed == PreferenceAllowed.PREFERENCE_ALLOWED))
                ;
    }

    boolean isRunnable(Context context, boolean checkSomeSensorEnabled) {
        Context appContext = context.getApplicationContext();

        boolean runnable = true;
        if (checkSomeSensorEnabled) {
            boolean someEnabled = isEnabledSomeSensor(appContext);
            if (!someEnabled)
                runnable = false;
        }
        if (this._eventPreferencesTime._enabled)
            runnable = runnable && this._eventPreferencesTime.isRunnable(appContext);
        if (this._eventPreferencesBattery._enabled)
            runnable = runnable && this._eventPreferencesBattery.isRunnable(appContext);
        if (this._eventPreferencesCall._enabled)
            runnable = runnable && this._eventPreferencesCall.isRunnable(appContext);
        if (this._eventPreferencesAccessories._enabled)
            runnable = runnable && this._eventPreferencesAccessories.isRunnable(appContext);
        if (this._eventPreferencesCalendar._enabled)
            runnable = runnable && this._eventPreferencesCalendar.isRunnable(appContext);
        if (this._eventPreferencesWifi._enabled)
            runnable = runnable && this._eventPreferencesWifi.isRunnable(appContext);
        if (this._eventPreferencesScreen._enabled)
            runnable = runnable && this._eventPreferencesScreen.isRunnable(appContext);
        if (this._eventPreferencesBrightness._enabled)
            runnable = runnable && this._eventPreferencesBrightness.isRunnable(appContext);
        if (this._eventPreferencesBluetooth._enabled)
            runnable = runnable && this._eventPreferencesBluetooth.isRunnable(appContext);
        if (this._eventPreferencesSMS._enabled)
            runnable = runnable && this._eventPreferencesSMS.isRunnable(appContext);
        if (this._eventPreferencesNotification._enabled)
            runnable = runnable && this._eventPreferencesNotification.isRunnable(appContext);
        if (this._eventPreferencesApplication._enabled)
            runnable = runnable && this._eventPreferencesApplication.isRunnable(appContext);
        if (this._eventPreferencesLocation._enabled)
            runnable = runnable && this._eventPreferencesLocation.isRunnable(appContext);
        if (this._eventPreferencesOrientation._enabled)
            runnable = runnable && this._eventPreferencesOrientation.isRunnable(appContext);
        if (this._eventPreferencesMobileCells._enabled)
            runnable = runnable && this._eventPreferencesMobileCells.isRunnable(appContext);
        if (this._eventPreferencesNFC._enabled)
            runnable = runnable && this._eventPreferencesNFC.isRunnable(appContext);
        if (this._eventPreferencesRadioSwitch._enabled)
            runnable = runnable && this._eventPreferencesRadioSwitch.isRunnable(appContext);
        if (this._eventPreferencesAlarmClock._enabled)
            runnable = runnable && this._eventPreferencesAlarmClock.isRunnable(appContext);
        if (this._eventPreferencesDeviceBoot._enabled)
            runnable = runnable && this._eventPreferencesDeviceBoot.isRunnable(appContext);
        if (this._eventPreferencesSoundProfile._enabled)
            runnable = runnable && this._eventPreferencesSoundProfile.isRunnable(appContext);
        if (this._eventPreferencesPeriodic._enabled)
            runnable = runnable && this._eventPreferencesPeriodic.isRunnable(appContext);
        if (this._eventPreferencesVolumes._enabled)
            runnable = runnable && this._eventPreferencesVolumes.isRunnable(appContext);
        if (this._eventPreferencesActivatedProfile._enabled)
            runnable = runnable && this._eventPreferencesActivatedProfile.isRunnable(appContext);
        if (this._eventPreferencesRoaming._enabled)
            runnable = runnable && this._eventPreferencesRoaming.isRunnable(appContext);
        if (this._eventPreferencesVPN._enabled)
            runnable = runnable && this._eventPreferencesVPN.isRunnable(appContext);
        if (this._eventPreferencesMusic._enabled)
            runnable = runnable && this._eventPreferencesMusic.isRunnable(appContext);
        if (this._eventPreferencesCallScreening._enabled)
            runnable = runnable && this._eventPreferencesCallScreening.isRunnable(appContext);

        return runnable;
    }

    boolean isAllConfigured(Context context, boolean checkSomeSensorEnabled) {
        Context appContext = context.getApplicationContext();

        boolean isAllConfigured = true;
        if (checkSomeSensorEnabled) {
            boolean someEnabled = isEnabledSomeSensor(appContext);
            if (!someEnabled)
                isAllConfigured = false;
        }
        if (this._eventPreferencesTime._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesTime.isAllConfigured(appContext);
        if (this._eventPreferencesBattery._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesBattery.isAllConfigured(appContext);
        if (this._eventPreferencesCall._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesCall.isAllConfigured(appContext);
        if (this._eventPreferencesAccessories._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesAccessories.isAllConfigured(appContext);
        if (this._eventPreferencesCalendar._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesCalendar.isAllConfigured(appContext);
        if (this._eventPreferencesWifi._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesWifi.isAllConfigured(appContext);
        if (this._eventPreferencesScreen._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesScreen.isAllConfigured(appContext);
        if (this._eventPreferencesBrightness._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesBrightness.isAllConfigured(appContext);
        if (this._eventPreferencesBluetooth._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesBluetooth.isAllConfigured(appContext);
        if (this._eventPreferencesSMS._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesSMS.isAllConfigured(appContext);
        if (this._eventPreferencesNotification._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesNotification.isAllConfigured(appContext);
        if (this._eventPreferencesApplication._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesApplication.isAllConfigured(appContext);
        if (this._eventPreferencesLocation._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesLocation.isAllConfigured(appContext);
        if (this._eventPreferencesOrientation._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesOrientation.isAllConfigured(appContext);
        if (this._eventPreferencesMobileCells._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesMobileCells.isAllConfigured(appContext);
        if (this._eventPreferencesNFC._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesNFC.isAllConfigured(appContext);
        if (this._eventPreferencesRadioSwitch._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesRadioSwitch.isAllConfigured(appContext);
        if (this._eventPreferencesAlarmClock._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesAlarmClock.isAllConfigured(appContext);
        if (this._eventPreferencesDeviceBoot._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesDeviceBoot.isAllConfigured(appContext);
        if (this._eventPreferencesSoundProfile._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesSoundProfile.isAllConfigured(appContext);
        if (this._eventPreferencesPeriodic._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesPeriodic.isAllConfigured(appContext);
        if (this._eventPreferencesVolumes._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesVolumes.isAllConfigured(appContext);
        if (this._eventPreferencesActivatedProfile._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesActivatedProfile.isAllConfigured(appContext);
        if (this._eventPreferencesRoaming._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesRoaming.isAllConfigured(appContext);
        if (this._eventPreferencesVPN._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesVPN.isAllConfigured(appContext);
        if (this._eventPreferencesMusic._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesMusic.isAllConfigured(appContext);
        if (this._eventPreferencesCallScreening._enabled)
            isAllConfigured = isAllConfigured && this._eventPreferencesCallScreening.isAllConfigured(appContext);

        return isAllConfigured;
    }

    int isAccessibilityServiceEnabled(Context context, boolean checkSomeSensorEnabled, boolean againCheckInDelay) {
        int accessibilityEnabled = 1;
        boolean someEnabled = true;
        if (checkSomeSensorEnabled) {
            someEnabled =
                    this._eventPreferencesTime._enabled ||
                            this._eventPreferencesBattery._enabled ||
                            this._eventPreferencesCall._enabled ||
                            this._eventPreferencesAccessories._enabled ||
                            this._eventPreferencesCalendar._enabled ||
                            this._eventPreferencesWifi._enabled ||
                            this._eventPreferencesScreen._enabled ||
                            this._eventPreferencesBrightness._enabled ||
                            this._eventPreferencesBluetooth._enabled ||
                            this._eventPreferencesSMS._enabled ||
                            this._eventPreferencesNotification._enabled ||
                            this._eventPreferencesApplication._enabled ||
                            this._eventPreferencesLocation._enabled ||
                            this._eventPreferencesOrientation._enabled ||
                            this._eventPreferencesMobileCells._enabled ||
                            this._eventPreferencesNFC._enabled ||
                            this._eventPreferencesRadioSwitch._enabled ||
                            this._eventPreferencesAlarmClock._enabled ||
                            this._eventPreferencesDeviceBoot._enabled ||
                            this._eventPreferencesSoundProfile._enabled ||
                            this._eventPreferencesPeriodic._enabled ||
                            this._eventPreferencesVolumes._enabled ||
                            this._eventPreferencesActivatedProfile._enabled ||
                            this._eventPreferencesRoaming._enabled ||
                            this._eventPreferencesVPN._enabled ||
                            this._eventPreferencesMusic._enabled ||
                            this._eventPreferencesCallScreening._enabled;
        }
        if (someEnabled) {
            if (this._eventPreferencesTime._enabled)
                accessibilityEnabled = this._eventPreferencesTime.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesBattery._enabled)
                accessibilityEnabled = this._eventPreferencesBattery.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesCall._enabled)
                accessibilityEnabled = this._eventPreferencesCall.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesAccessories._enabled)
                accessibilityEnabled = this._eventPreferencesAccessories.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesCalendar._enabled)
                accessibilityEnabled = this._eventPreferencesCalendar.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesWifi._enabled)
                accessibilityEnabled = this._eventPreferencesWifi.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesScreen._enabled)
                accessibilityEnabled = this._eventPreferencesScreen.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesBrightness._enabled)
                accessibilityEnabled = this._eventPreferencesBrightness.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesBluetooth._enabled)
                accessibilityEnabled = this._eventPreferencesBluetooth.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesSMS._enabled)
                accessibilityEnabled = this._eventPreferencesSMS.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesNotification._enabled)
                accessibilityEnabled = this._eventPreferencesNotification.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesApplication._enabled)
                accessibilityEnabled = this._eventPreferencesApplication.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesLocation._enabled)
                accessibilityEnabled = this._eventPreferencesLocation.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesOrientation._enabled)
                accessibilityEnabled = this._eventPreferencesOrientation.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesMobileCells._enabled)
                accessibilityEnabled = this._eventPreferencesMobileCells.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesNFC._enabled)
                accessibilityEnabled = this._eventPreferencesNFC.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesRadioSwitch._enabled)
                accessibilityEnabled = this._eventPreferencesRadioSwitch.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesAlarmClock._enabled)
                accessibilityEnabled = this._eventPreferencesAlarmClock.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesDeviceBoot._enabled)
                accessibilityEnabled = this._eventPreferencesDeviceBoot.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesSoundProfile._enabled)
                accessibilityEnabled = this._eventPreferencesSoundProfile.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesPeriodic._enabled)
                accessibilityEnabled = this._eventPreferencesPeriodic.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesVolumes._enabled)
                accessibilityEnabled = this._eventPreferencesVolumes.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesActivatedProfile._enabled)
                accessibilityEnabled = this._eventPreferencesActivatedProfile.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesRoaming._enabled)
                accessibilityEnabled = this._eventPreferencesRoaming.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesVPN._enabled)
                accessibilityEnabled = this._eventPreferencesVPN.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesMusic._enabled)
                accessibilityEnabled = this._eventPreferencesMusic.isAccessibilityServiceEnabled(context, againCheckInDelay);
            if (this._eventPreferencesCallScreening._enabled)
                accessibilityEnabled = this._eventPreferencesCallScreening.isAccessibilityServiceEnabled(context, againCheckInDelay);
        }

        return accessibilityEnabled;
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putLong(PREF_EVENT_ID, this._id);
        editor.putString(PREF_EVENT_NAME, this._name);
        if (this._fkProfileStart == 0)
            this._fkProfileStart = Profile.PROFILE_NO_ACTIVATE;
        editor.putString(PREF_EVENT_PROFILE_START, Long.toString(this._fkProfileStart));
        if (this._fkProfileEnd == 0)
            this._fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;
        editor.putString(PREF_EVENT_PROFILE_END, Long.toString(this._fkProfileEnd));
        editor.putBoolean(PREF_EVENT_ENABLED, this._status != ESTATUS_STOP);
        editor.putString(PREF_EVENT_NOTIFICATION_SOUND_START, this._notificationSoundStart);
        editor.putBoolean(PREF_EVENT_NOTIFICATION_VIBRATE_START, this._notificationVibrateStart);
        editor.putBoolean(PREF_EVENT_NOTIFICATION_REPEAT_START, this._repeatNotificationStart);
        editor.putString(PREF_EVENT_NOTIFICATION_REPEAT_INTERVAL_START, String.valueOf(this._repeatNotificationIntervalStart));
        editor.putString(PREF_EVENT_NOTIFICATION_SOUND_END, this._notificationSoundEnd);
        editor.putBoolean(PREF_EVENT_NOTIFICATION_VIBRATE_END, this._notificationVibrateEnd);
        editor.putBoolean(PREF_EVENT_IGNORE_MANUAL_ACTIVATION, this._ignoreManualActivation);
        //editor.putBoolean(PREF_EVENT_UNDONE_PROFILE, this._undoneProfile);
        editor.putString(PREF_EVENT_PRIORITY_APP_SETTINGS, Integer.toString(this._priority));
        editor.putString(PREF_EVENT_PRIORITY, Integer.toString(this._priority));
        editor.putString(PREF_EVENT_DELAY_START, Integer.toString(this._delayStart));
        editor.putString(PREF_EVENT_AT_END_DO, Integer.toString(this._atEndDo));
        editor.putBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION, this._manualProfileActivation);
        editor.putString(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, this._startWhenActivatedProfile);
        editor.putString(PREF_EVENT_DELAY_END, Integer.toString(this._delayEnd));
        editor.putBoolean(PREF_EVENT_NO_PAUSE_BY_MANUAL_ACTIVATION, this._noPauseByManualActivation);
        //editor.putString(PREF_EVENT_AT_END_HOW_UNDO, Integer.toString(this._atEndHowUndo));
        editor.putBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION_AT_END, this._manualProfileActivationAtEnd);
        editor.putBoolean(PREF_EVENT_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE, this._notificationSoundStartPlayAlsoInSilentMode);
        editor.putBoolean(PREF_EVENT_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE, this._notificationSoundEndPlayAlsoInSilentMode);

        this._eventPreferencesTime.loadSharedPreferences(preferences);
        this._eventPreferencesBattery.loadSharedPreferences(preferences);
        this._eventPreferencesCall.loadSharedPreferences(preferences);
        this._eventPreferencesAccessories.loadSharedPreferences(preferences);
        this._eventPreferencesCalendar.loadSharedPreferences(preferences);
        this._eventPreferencesWifi.loadSharedPreferences(preferences);
        this._eventPreferencesScreen.loadSharedPreferences(preferences);
        this._eventPreferencesBrightness.loadSharedPreferences(preferences);
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
        this._eventPreferencesDeviceBoot.loadSharedPreferences(preferences);
        this._eventPreferencesSoundProfile.loadSharedPreferences(preferences);
        this._eventPreferencesPeriodic.loadSharedPreferences(preferences);
        this._eventPreferencesVolumes.loadSharedPreferences(preferences);
        this._eventPreferencesActivatedProfile.loadSharedPreferences(preferences);
        this._eventPreferencesRoaming.loadSharedPreferences(preferences);
        this._eventPreferencesVPN.loadSharedPreferences(preferences);
        this._eventPreferencesMusic.loadSharedPreferences(preferences);
        this._eventPreferencesCallScreening.loadSharedPreferences(preferences);
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences, Context context)
    {
        this._name = preferences.getString(PREF_EVENT_NAME, "");
        this._fkProfileStart = Long.parseLong(preferences.getString(PREF_EVENT_PROFILE_START, Long.toString(Profile.PROFILE_NO_ACTIVATE)));
        this._fkProfileEnd = Long.parseLong(preferences.getString(PREF_EVENT_PROFILE_END, Long.toString(Profile.PROFILE_NO_ACTIVATE)));
        this._status = (preferences.getBoolean(PREF_EVENT_ENABLED, false)) ? ESTATUS_PAUSE : ESTATUS_STOP;
        this._notificationSoundStart = preferences.getString(PREF_EVENT_NOTIFICATION_SOUND_START, "");
        this._notificationVibrateStart = preferences.getBoolean(PREF_EVENT_NOTIFICATION_VIBRATE_START, false);
        this._repeatNotificationStart = preferences.getBoolean(PREF_EVENT_NOTIFICATION_REPEAT_START, false);
        this._repeatNotificationIntervalStart = Integer.parseInt(preferences.getString(PREF_EVENT_NOTIFICATION_REPEAT_INTERVAL_START, "900"));
        this._notificationSoundEnd = preferences.getString(PREF_EVENT_NOTIFICATION_SOUND_END, "");
        this._notificationVibrateEnd = preferences.getBoolean(PREF_EVENT_NOTIFICATION_VIBRATE_END, false);
        this._ignoreManualActivation = preferences.getBoolean(PREF_EVENT_IGNORE_MANUAL_ACTIVATION, false);
        //this._undoneProfile = preferences.getBoolean(PREF_EVENT_UNDONE_PROFILE, true);
        this._priority = Integer.parseInt(preferences.getString(PREF_EVENT_PRIORITY, Integer.toString(EPRIORITY_MEDIUM)));
        this._atEndDo = Integer.parseInt(preferences.getString(PREF_EVENT_AT_END_DO, Integer.toString(EATENDDO_RESTART_EVENTS)));
        this._manualProfileActivation = preferences.getBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION, false);
        this._startWhenActivatedProfile = preferences.getString(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, "");
        this._noPauseByManualActivation = preferences.getBoolean(PREF_EVENT_NO_PAUSE_BY_MANUAL_ACTIVATION, false);
        //this._atEndHowUndo = Integer.parseInt(preferences.getString(PREF_EVENT_AT_END_HOW_UNDO, "0"));
        this._manualProfileActivationAtEnd = preferences.getBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION_AT_END, false);
        this._notificationSoundStartPlayAlsoInSilentMode = preferences.getBoolean(PREF_EVENT_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE, false);
        this._notificationSoundEndPlayAlsoInSilentMode = preferences.getBoolean(PREF_EVENT_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE, false);

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
        this._eventPreferencesAccessories.saveSharedPreferences(preferences);
        this._eventPreferencesCalendar.saveSharedPreferences(preferences);
        this._eventPreferencesWifi.saveSharedPreferences(preferences);
        this._eventPreferencesScreen.saveSharedPreferences(preferences);
        this._eventPreferencesBrightness.saveSharedPreferences(preferences);
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
        this._eventPreferencesDeviceBoot.saveSharedPreferences(preferences);
        this._eventPreferencesSoundProfile.saveSharedPreferences(preferences);
        this._eventPreferencesPeriodic.saveSharedPreferences(preferences);
        this._eventPreferencesVolumes.saveSharedPreferences(preferences);
        this._eventPreferencesActivatedProfile.saveSharedPreferences(preferences);
        this._eventPreferencesRoaming.saveSharedPreferences(preferences);
        this._eventPreferencesVPN.saveSharedPreferences(preferences);
        this._eventPreferencesMusic.saveSharedPreferences(preferences);
        this._eventPreferencesCallScreening.saveSharedPreferences(preferences);

        if (!(this.isRunnable(context, true) && this.isAllConfigured(context, true)))
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
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !value.isEmpty(), false, false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_PROFILE_START) || key.equals(PREF_EVENT_PROFILE_END))
        {
            ProfilePreference preference = prefMng.findPreference(key);
            if (preference != null) {
                long lProfileId;
                try {
                    lProfileId = Long.parseLong(value);
                } catch (Exception e) {
                    lProfileId = 0;
                }
                preference.setSummary(lProfileId);
                //if (key.equals(PREF_EVENT_PROFILE_START))
                //    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, (lProfileId != 0) && (lProfileId != Profile.PROFILE_NO_ACTIVATE), false, lProfileId == 0, false);
                //else
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, (lProfileId != 0) && (lProfileId != Profile.PROFILE_NO_ACTIVATE), false, false, lProfileId == 0, false);
            }
        }
        if (key.equals(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE))
        {
            ProfileMultiSelectPreference preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !value.isEmpty(), false, false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_SOUND_START) ||
            key.equals(PREF_EVENT_NOTIFICATION_SOUND_END))
        {
            Preference preference = prefMng.findPreference(key);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !value.isEmpty(), false, false, false, false);
        }
        if (key.equals(PREF_EVENT_PRIORITY_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                boolean applicationEventUsePriority = ApplicationPreferences.applicationEventUsePriority;
                String summary = context.getString(R.string.event_preferences_event_priorityInfo_summary);
                if (applicationEventUsePriority)
                    summary = context.getString(R.string.event_preferences_priority_appSettings_enabled) + StringConstants.STR_SEPARATOR_LINE + summary;
                else {
                    summary = context.getString(R.string.event_preferences_priority_appSettings_disabled) + StringConstants.STR_SEPARATOR_LINE + summary + StringConstants.CHAR_NEW_LINE+
                            context.getString(R.string.phone_profiles_pref_eventUsePriorityAppSettings_summary);
                }
                preference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, applicationEventUsePriority, false, true, false, false);
            }
        }
        if (key.equals(PREF_EVENT_PRIORITY))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                boolean applicationEventUsePriority = ApplicationPreferences.applicationEventUsePriority;
                if (applicationEventUsePriority) {
                    int index = listPreference.findIndexOfValue(value);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, !value.equals("0"), false, false, false, false);
                } else {
                    listPreference.setSummary(R.string.event_preferences_priority_notUse);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, false, false);
                }
                listPreference.setEnabled(applicationEventUsePriority);
            }
        }
        if (key.equals(PREF_EVENT_AT_END_DO))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false, false);
            }
        }
        /*if (key.equals(PREF_EVENT_AT_END_HOW_UNDO))
        {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
            }
        }*/
        if (key.equals(PREF_EVENT_DELAY_START))
        {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 0;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 0, false, false, false, false);
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
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 0, false, false, false, false);
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
            key.equals(PREF_EVENT_IGNORE_MANUAL_ACTIVATION) ||
            key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION) ||
            key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_START) ||
            key.equals(PREF_EVENT_NOTIFICATION_REPEAT_START) ||
            key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_END) ||
            key.equals(PREF_EVENT_NO_PAUSE_BY_MANUAL_ACTIVATION) ||
            key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION_AT_END)) {

            boolean hasVibrator = true;
            if (key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_START) ||
                    key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_END)) {
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                hasVibrator = (vibrator != null) && vibrator.hasVibrator();
            }

            Preference preference = prefMng.findPreference(key);
            if (hasVibrator) {
                if (preference != null)
                    preference.setVisible(true);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, value.equals(StringConstants.TRUE_STRING), false, false, false, false);
            } else {
                if (preference != null)
                    preference.setVisible(false);
            }
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
                key.equals(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE) ||
                key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION_AT_END) ||
                key.equals(PREF_EVENT_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE) ||
                key.equals(PREF_EVENT_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE)) {
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
            boolean manualProfileActivationAtEndChanged;
            boolean notificationSoundStartPlayAlsoInSilentMode;
            boolean notificationSoundEndPlayAlsoInSilentMode;

            String startWhenActivatedProfile;
            int delayStart;
            int delayEnd;
            int repeatInterval;

            if (preferences == null) {
                //forceRunChanged = this._ignoreManualActivation;
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
                manualProfileActivationAtEndChanged = this._manualProfileActivationAtEnd;
                notificationSoundStartPlayAlsoInSilentMode = this._notificationSoundStartPlayAlsoInSilentMode;
                notificationSoundEndPlayAlsoInSilentMode = this._notificationSoundEndPlayAlsoInSilentMode;
                repeatInterval = this._repeatNotificationIntervalStart;
            }
            else {
                //forceRunChanged = preferences.getBoolean(PREF_EVENT_IGNORE_MANUAL_ACTIVATION, false);
                manualProfileActivationChanged = preferences.getBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION, false);
                startWhenActivatedProfile = preferences.getString(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, "");
                profileStartWhenActivatedChanged = !startWhenActivatedProfile.isEmpty();
                delayStartChanged = !preferences.getString(PREF_EVENT_DELAY_START, "0").equals("0");
                delayEndChanged = !preferences.getString(PREF_EVENT_DELAY_END, "0").equals("0");
                delayStart = Integer.parseInt(preferences.getString(PREF_EVENT_DELAY_START, "0"));
                delayEnd = Integer.parseInt(preferences.getString(PREF_EVENT_DELAY_END, "0"));
                notificationSoundStartChanged = !preferences.getString(PREF_EVENT_NOTIFICATION_SOUND_START, "").isEmpty();
                notificationVibrateStartChanged = preferences.getBoolean(PREF_EVENT_NOTIFICATION_VIBRATE_START, false);
                notificationVibrateEndChanged = preferences.getBoolean(PREF_EVENT_NOTIFICATION_VIBRATE_END, false);
                notificationSoundStartPlayAlsoInSilentMode = preferences.getBoolean(PREF_EVENT_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE, false);
                notificationSoundEndPlayAlsoInSilentMode = preferences.getBoolean(PREF_EVENT_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE, false);
                if (key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_START) ||
                        key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_END)) {
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    boolean hasVibrator = (vibrator != null) && vibrator.hasVibrator();
                    if (!hasVibrator) {
                        notificationVibrateStartChanged = false;
                        notificationVibrateEndChanged = false;
                    }
                }
                notificationRepeatStartChanged = preferences.getBoolean(PREF_EVENT_NOTIFICATION_REPEAT_START, false);
                notificationSoundEndChanged = !preferences.getString(PREF_EVENT_NOTIFICATION_SOUND_END, "").isEmpty();
                manualProfileActivationAtEndChanged = preferences.getBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION_AT_END, false);
                repeatInterval = Integer.parseInt(preferences.getString(PREF_EVENT_NOTIFICATION_REPEAT_INTERVAL_START, "0"));
            }
            Preference preference = prefMng.findPreference(PREF_EVENT_START_OTHERS_CATEGORY_ROOT);
            if (preference != null) {
                boolean bold = (//forceRunChanged ||
                        manualProfileActivationChanged ||
                                profileStartWhenActivatedChanged ||
                                delayStartChanged ||
                                notificationSoundStartChanged ||
                                notificationVibrateStartChanged ||
                                notificationRepeatStartChanged ||
                                notificationSoundStartPlayAlsoInSilentMode);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, false, false, false, false);
                if (bold) {
                    StringBuilder _value = new StringBuilder();
                    if (manualProfileActivationChanged) {
                        /*if (_value.length() > 0)*/ _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_manualProfileActivation));
                    }
                    if (profileStartWhenActivatedChanged) {
                        if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_eventStartWhenActivatedProfile)).append(StringConstants.STR_COLON_WITH_SPACE);
                        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, 0, 0, 0f);
                        String[] splits = startWhenActivatedProfile.split(StringConstants.STR_SPLIT_REGEX);
                        if (splits.length == 1) {
                            String profileName = dataWrapper.getProfileName(Long.parseLong(startWhenActivatedProfile));
                            if (profileName != null) {
                                _value.append(StringConstants.TAG_BOLD_START_HTML)
                                        .append(EventPreferences.getColorForChangedPreferenceValue(profileName, !preference.isEnabled(), false, context))
                                        .append(StringConstants.TAG_BOLD_END_HTML);
                            }
                        }
                        else {
                            _value.append(StringConstants.TAG_BOLD_START_HTML)
                                    .append(EventPreferences.getColorForChangedPreferenceValue(context.getString(R.string.profile_multiselect_summary_text_selected) + " " + splits.length, !preference.isEnabled(), false, context))
                                    .append(StringConstants.TAG_BOLD_END_HTML);
                        }
                        dataWrapper.invalidateDataWrapper();
                    }
                    if (delayStartChanged) {
                        if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_delayStart)).append(StringConstants.STR_COLON_WITH_SPACE);
                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(EventPreferences.getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(delayStart), !preference.isEnabled(), false, context))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }
                    if (notificationSoundStartChanged) {
                        if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_notificationSound));
                    }
                    if (notificationVibrateStartChanged) {
                        if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_notificationVibrate));
                    }
                    if (notificationRepeatStartChanged) {
                        if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_notificationRepeat)).append(StringConstants.STR_COLON_WITH_SPACE);
                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(EventPreferences.getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(repeatInterval), !preference.isEnabled(), false, context))
                                .append(StringConstants.TAG_BOLD_END_HTML);

                    }
                    preference.setSummary(StringFormatUtils.fromHtml(_value.toString(), false,  false, 0, 0, true));
                }
                else
                    preference.setSummary("");
            }
            preference = prefMng.findPreference(PREF_EVENT_END_OTHERS_CATEGORY_ROOT);
            if (preference != null) {
                boolean bold = (delayEndChanged ||
                        notificationSoundEndChanged ||
                        notificationVibrateEndChanged ||
                        manualProfileActivationAtEndChanged ||
                        notificationSoundEndPlayAlsoInSilentMode);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, false, false, false, false);
                if (bold) {
                    StringBuilder _value = new StringBuilder();
                    if (manualProfileActivationAtEndChanged) {
                        /*if (_value.length() > 0)*/ _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_manualProfileActivationAtEnd));
                    }
                    if (delayEndChanged) {
                        /*if (_value.length() > 0)*/ _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_delayStart)).append(StringConstants.STR_COLON_WITH_SPACE);
                        _value.append(StringConstants.TAG_BOLD_START_HTML)
                                .append(EventPreferences.getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(delayEnd), !preference.isEnabled(), false, context))
                                .append(StringConstants.TAG_BOLD_END_HTML);
                    }
                    if (notificationSoundEndChanged) {
                        if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_notificationSound));
                    }
                    if (notificationVibrateEndChanged) {
                        if (_value.length() > 0) _value.append(StringConstants.STR_BULLET);
                        _value.append(context.getString(R.string.event_preferences_notificationVibrate));
                    }
                    preference.setSummary(StringFormatUtils.fromHtml(_value.toString(), false,  false, 0, 0, true));
                }
                else
                    preference.setSummary("");
            }
        }
    }

    void setSummary(PreferenceManager prefMng,
                           String key,
                           SharedPreferences preferences,
                           Context context,
                           boolean alsoSensors)
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
            //key.equals(PREF_EVENT_AT_END_HOW_UNDO) ||
            key.equals(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE))
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        if (key.equals(PREF_EVENT_ENABLED) ||
            key.equals(PREF_EVENT_IGNORE_MANUAL_ACTIVATION) ||
            key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION) ||
            key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_START) ||
            key.equals(PREF_EVENT_NOTIFICATION_REPEAT_START) ||
            key.equals(PREF_EVENT_NOTIFICATION_VIBRATE_END) ||
            key.equals(PREF_EVENT_NO_PAUSE_BY_MANUAL_ACTIVATION) ||
            key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION_AT_END) ||
            key.equals(PREF_EVENT_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE) ||
            key.equals(PREF_EVENT_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, Boolean.toString(value), context);
        }
        if (key.equals(PREF_EVENT_PRIORITY_APP_SETTINGS)) {
            setSummary(prefMng, key, "", context);
        }

        if (//key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION) ||
                key.equals(PREF_EVENT_PROFILE_END) ||
                key.equals(PREF_EVENT_AT_END_DO) ||
                //key.equals(PREF_EVENT_AT_END_HOW_UNDO) ||
                key.equals(PREF_EVENT_END_OTHERS_CATEGORY_ROOT)) {
            //boolean value = preferences.getBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION, false);
            /*if (value) {
                Preference preference = prefMng.findPreference(PREF_EVENT_PROFILE_END);
                if (preference != null)
                    preference.setEnabled(false);
                preference = prefMng.findPreference(PREF_EVENT_AT_END_DO);
                if (preference != null)
                    preference.setEnabled(false);
                //preference = prefMng.findPreference(PREF_EVENT_AT_END_HOW_UNDO);
                //if (preference != null)
                //    preference.setEnabled(false);
                preference = prefMng.findPreference(PREF_EVENT_END_OTHERS);
                if (preference != null)
                    preference.setEnabled(false);
            }
            else */{
                Preference preference = prefMng.findPreference(PREF_EVENT_AT_END_DO);
                if (preference != null)
                    preference.setEnabled(true);

                /*preference = prefMng.findPreference(PREF_EVENT_AT_END_HOW_UNDO);
                if (preference != null)
                    preference.setEnabled(true);*/
                //String value2 = preferences.getString(PREF_EVENT_AT_END_HOW_UNDO, "0");
                preference = prefMng.findPreference(PREF_EVENT_PROFILE_END);
                if (preference != null)
                    //preference.setEnabled(value2.equals("0"));
                    preference.setEnabled(true);

                preference = prefMng.findPreference(PREF_EVENT_END_OTHERS_CATEGORY_ROOT);
                if (preference != null)
                    preference.setEnabled(true);
            }
        }
        setCategorySummary(prefMng, key, preferences, context);

        if (alsoSensors) {
            _eventPreferencesTime.setSummary(prefMng, key, preferences, context);
            _eventPreferencesTime.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesBattery.setSummary(prefMng, key, preferences, context);
            _eventPreferencesBattery.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesCall.setSummary(prefMng, key, preferences, context);
            _eventPreferencesCall.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesAccessories.setSummary(prefMng, key, preferences, context);
            _eventPreferencesAccessories.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesCalendar.setSummary(prefMng, key, preferences, context);
            _eventPreferencesCalendar.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesWifi.setSummary(prefMng, key, preferences, context);
            _eventPreferencesWifi.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesScreen.setSummary(prefMng, key, preferences/*, context*/);
            _eventPreferencesScreen.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesBrightness.setSummary(prefMng, key, preferences/*, context*/);
            _eventPreferencesBrightness.setCategorySummary(prefMng, preferences, context);
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
            _eventPreferencesDeviceBoot.setSummary(prefMng, key, preferences/*, context*/);
            _eventPreferencesDeviceBoot.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesSoundProfile.setSummary(prefMng, key, preferences, context);
            _eventPreferencesSoundProfile.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesPeriodic.setSummary(prefMng, key, preferences, context);
            _eventPreferencesPeriodic.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesVolumes.setSummary(prefMng, key, preferences, context);
            _eventPreferencesVolumes.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesActivatedProfile.setSummary(prefMng, key, preferences, context);
            _eventPreferencesActivatedProfile.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesRoaming.setSummary(prefMng, key, preferences, context);
            _eventPreferencesRoaming.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesVPN.setSummary(prefMng, key, preferences, context);
            _eventPreferencesVPN.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesMusic.setSummary(prefMng, key, preferences, context);
            _eventPreferencesMusic.setCategorySummary(prefMng, preferences, context);
            _eventPreferencesCallScreening.setSummary(prefMng, key, preferences, context);
            _eventPreferencesCallScreening.setCategorySummary(prefMng, preferences, context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context) {

        Preference preference = prefMng.findPreference(PREF_EVENT_IGNORE_MANUAL_ACTIVATION);
        if (preference != null)
            preference.setTitle(StringConstants.STR_ARROW_INDICATOR +" " + context.getString(R.string.event_preferences_ForceRun));
        preference = prefMng.findPreference(PREF_EVENT_NO_PAUSE_BY_MANUAL_ACTIVATION);
        if (preference != null)
            preference.setTitle(StringConstants.STR_DOUBLE_ARROW_INDICATOR+" " + context.getString(R.string.event_preferences_noPauseByManualActivation));

        setSummary(prefMng, PREF_EVENT_ENABLED, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_NAME, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_PROFILE_START, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_PROFILE_END, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_SOUND_START, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_VIBRATE_START, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_REPEAT_START, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_REPEAT_INTERVAL_START, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_SOUND_END, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_VIBRATE_END, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_PRIORITY_APP_SETTINGS, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_PRIORITY, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_DELAY_START, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_DELAY_END, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_AT_END_DO, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_IGNORE_MANUAL_ACTIVATION, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_MANUAL_PROFILE_ACTIVATION, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_NO_PAUSE_BY_MANUAL_ACTIVATION, preferences, context, false);
        //setSummary(prefMng, PREF_EVENT_AT_END_HOW_UNDO, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_MANUAL_PROFILE_ACTIVATION_AT_END, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE, preferences, context, false);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE, preferences, context, false);
        setCategorySummary(prefMng, "", preferences, context);

        _eventPreferencesTime.setAllSummary(prefMng, preferences, context);
        _eventPreferencesTime.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesBattery.setAllSummary(prefMng, preferences, context);
        _eventPreferencesBattery.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesCall.setAllSummary(prefMng, preferences, context);
        _eventPreferencesCall.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesAccessories.setAllSummary(prefMng, preferences, context);
        _eventPreferencesAccessories.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesCalendar.setAllSummary(prefMng, preferences, context);
        _eventPreferencesCalendar.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesWifi.setAllSummary(prefMng, preferences, context);
        _eventPreferencesWifi.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesScreen.setAllSummary(prefMng, preferences/*, context*/);
        _eventPreferencesScreen.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesBrightness.setAllSummary(prefMng, preferences/*, context*/);
        _eventPreferencesBrightness.setCategorySummary(prefMng, preferences, context);
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
        _eventPreferencesDeviceBoot.setAllSummary(prefMng, preferences/*, context*/);
        _eventPreferencesDeviceBoot.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesSoundProfile.setAllSummary(prefMng, preferences, context);
        _eventPreferencesSoundProfile.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesPeriodic.setAllSummary(prefMng, preferences, context);
        _eventPreferencesPeriodic.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesVolumes.setAllSummary(prefMng, preferences, context);
        _eventPreferencesVolumes.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesActivatedProfile.setAllSummary(prefMng, preferences, context);
        _eventPreferencesActivatedProfile.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesRoaming.setAllSummary(prefMng, preferences, context);
        _eventPreferencesRoaming.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesVPN.setAllSummary(prefMng, preferences, context);
        _eventPreferencesVPN.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesMusic.setAllSummary(prefMng, preferences, context);
        _eventPreferencesMusic.setCategorySummary(prefMng, preferences, context);
        _eventPreferencesCallScreening.setAllSummary(prefMng, preferences, context);
        _eventPreferencesCallScreening.setCategorySummary(prefMng, preferences, context);
    }

    String getPreferencesDescription(Context context, DataWrapper _dataWrapper, boolean addPassStatus)
    {
        StringBuilder _value = new StringBuilder();

        if (ApplicationPreferences.applicationEventUsePriority) {
            // This is pseudo sensor [Priority], this sensor do have notAllowed status
            String passStatusString;
            if (EventStatic.getGlobalEventsRunning(context) && addPassStatus && (getStatusFromDB(context) != Event.ESTATUS_STOP)) {
                int sensorPassed = EventPreferences.SENSOR_PASSED_PASSED;
                DataWrapper dataWrapper;
                if (_dataWrapper == null) {
                    dataWrapper = new DataWrapper(context, false, 0, false, 0, 0, 0f);
                    /*List<EventTimeline> eventTimelineList = */dataWrapper.getEventTimelineList(true);
                } else
                    dataWrapper = _dataWrapper;
                // search for running event with higher priority
                if (ApplicationPreferences.applicationEventUsePriority) {
                    for (EventTimeline eventTimeline : dataWrapper.eventTimelines) {
                        int priority = dataWrapper.getEventPriority(eventTimeline._fkEvent);
                        if ((this._priority != EPRIORITY_DO_NOT_USE) &&
                                (priority != EPRIORITY_DO_NOT_USE) &&
                                (priority > this._priority)) {
                            // is running event with higher priority
                            sensorPassed = EventPreferences.SENSOR_PASSED_NOT_PASSED;
                        }
                    }
                }
                passStatusString = EventPreferences._getPassStatusString(sensorPassed, context.getString(R.string.event_preferences_priority), context);
            } else {
                passStatusString = "["+StringConstants.CHAR_HARD_SPACE_HTML + context.getString(R.string.event_preferences_priority) + StringConstants.CHAR_HARD_SPACE_HTML+"]";//+":";                //int labelColor = ContextCompat.getColor(context, R.color.activityNormalTextColor);
                //String colorString = String.format(StringConstants.STR_FORMAT_INT, labelColor).substring(2); // !!strip alpha value!!
                //passStatusString = String.format(StringConstants.TAG_FONT_COLOR_HTML/*+":"*/, colorString, "["+StringConstants.CHAR_HARD_SPACE_HTML + context.getString(R.string.event_preferences_priority) + StringConstants.CHAR_HARD_SPACE_HTML+"]");
            }

            String eventPriority;
            String[] stringArray = context.getResources().getStringArray(R.array.eventPriorityArray);
            if (_priority == Event.EPRIORITY_DO_NOT_USE)
                eventPriority = stringArray[11];
            else
                eventPriority = stringArray[Event.EPRIORITY_HIGHEST - _priority];

            int labelColor = ContextCompat.getColor(context, R.color.activityNormalTextColor);
            String colorString = String.format(StringConstants.STR_FORMAT_INT, labelColor).substring(2); // !!strip alpha value!!

            String desc = StringConstants.TAG_BOLD_START_HTML +
                    passStatusString +
                    StringConstants.TAG_BOLD_END_WITH_SPACE_HTML +
                    " " +
                    StringConstants.TAG_BOLD_START_HTML +
                    String.format(StringConstants.TAG_FONT_COLOR_HTML/*+":"*/, colorString, eventPriority) +
                    StringConstants.TAG_BOLD_END_HTML;
            _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesTime._enabled) {
            String desc = _eventPreferencesTime.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesPeriodic._enabled) {
            String desc = _eventPreferencesPeriodic.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesCalendar._enabled) {
            String desc = _eventPreferencesCalendar.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesBattery._enabled) {
            String desc = _eventPreferencesBattery.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesCall._enabled) {
            String desc = _eventPreferencesCall.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesCallScreening._enabled) {
            String desc = _eventPreferencesCallScreening.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesSMS._enabled) {
            String desc = _eventPreferencesSMS.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesRoaming._enabled) {
            String desc = _eventPreferencesRoaming.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesRadioSwitch._enabled) {
            String desc = _eventPreferencesRadioSwitch.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesVPN._enabled) {
            String desc = _eventPreferencesVPN.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesLocation._enabled) {
            String desc = _eventPreferencesLocation.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesWifi._enabled) {
            String desc = _eventPreferencesWifi.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesBluetooth._enabled) {
            String desc = _eventPreferencesBluetooth.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesMobileCells._enabled) {
            String desc = _eventPreferencesMobileCells.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesAccessories._enabled) {
            String desc = _eventPreferencesAccessories.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesScreen._enabled) {
            String desc = _eventPreferencesScreen.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesBrightness._enabled) {
            String desc = _eventPreferencesBrightness.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesNotification._enabled) {
            String desc = _eventPreferencesNotification.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesSoundProfile._enabled) {
            String desc = _eventPreferencesSoundProfile.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesVolumes._enabled) {
            String desc = _eventPreferencesVolumes.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesApplication._enabled) {
            String desc = _eventPreferencesApplication.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesOrientation._enabled) {
            String desc = _eventPreferencesOrientation.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesNFC._enabled) {
            String desc = _eventPreferencesNFC.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesAlarmClock._enabled) {
            String desc = _eventPreferencesAlarmClock.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesMusic._enabled) {
            String desc = _eventPreferencesMusic.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesDeviceBoot._enabled) {
            String desc = _eventPreferencesDeviceBoot.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_eventPreferencesActivatedProfile._enabled) {
            String desc = _eventPreferencesActivatedProfile.getPreferencesDescription(true, addPassStatus, false, context);
            if (desc != null)
                _value.append(StringConstants.TAG_LIST_START_FIRST_ITEM_HTML).append(desc).append(StringConstants.TAG_LIST_END_LAST_ITEM_HTML);
        }

        if (_value.length() == 0)
            _value.append(context.getString(R.string.event_preferences_no_sensor_is_enabled));

        //Log.e("Event.getPreferencesDescription", "desc="+_value);
        return _value.toString();
    }

    void checkSensorsPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        _eventPreferencesTime.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesBattery.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesCall.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesAccessories.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesCalendar.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesWifi.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesScreen.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesBrightness.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesBluetooth.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesSMS.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesNotification.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesApplication.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesLocation.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesOrientation.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesMobileCells.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesNFC.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesRadioSwitch.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesAlarmClock.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesDeviceBoot.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesSoundProfile.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesPeriodic.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesVolumes.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesActivatedProfile.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesRoaming.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesVPN.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesMusic.checkPreferences(prefMng, onlyCategory, context);
        _eventPreferencesCallScreening.checkPreferences(prefMng, onlyCategory, context);
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

        /*
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
        */

        DatabaseHandler.getInstance(dataWrapper.context).addEventTimeline(eventTimeline);
        eventTimelineList.add(eventTimeline);
    }

    // !!! WARNING: call it olny from EventsHandler.doHandleEvent() !!!
    void startEvent(DataWrapper dataWrapper,
                            //boolean ignoreGlobalPref,
                            //boolean interactive,
                            boolean forRestartEvents,
                            boolean manualRestart,
                            //boolean log,
                            Profile mergedProfile)
    {
        if ((!EventStatic.getGlobalEventsRunning(dataWrapper.context))/* && (!ignoreGlobalPref)*/)
            // events are globally stopped
            return;

        if (!(this.isRunnable(dataWrapper.context, true) && this.isAllConfigured(dataWrapper.context, true))) {
            // event is not runnable, no start it, but pause it

            // parameters as from EventsHandler.doHandleEvent()
            //pauseEvent(dataWrapper, true, false, false,
            //        true, mergedProfile, !forRestartEvents, forRestartEvents, manualRestart, true);

            // parameters as from DataWrapper._activateProfile() but:
            // do not allow restart events, do not activate return profile, do not call system event,
            // do npt merge to mergedProfile
            pauseEvent(dataWrapper, false, true, true,
                    true, null, false, forRestartEvents, manualRestart, true);
            return;
        }

        // remove delay alarm
        removeDelayStartAlarm(dataWrapper); // for start delay
        removeDelayEndAlarm(dataWrapper); // for end delay
        removeStartEventNotificationAlarm(dataWrapper); // for start repeating notification

        //if (ApplicationPreferences.prefEventsBlocked)
        if (EventStatic.getEventsBlocked(dataWrapper.context))
        {
            // blocked by manual profile activation
            // if application is restarted by system, ignore manual profile activation
            if ((!_ignoreManualActivation) || (!PPApplication.normalServiceStart))
                // event is not forceRun
                return;
            if (_blocked)
                // forceRun event is temporary blocked
                return;
        }

        // check activated profile
        if (!_startWhenActivatedProfile.isEmpty()) {
            long activatedProfileId = dataWrapper.getActivatedProfileId();
            if (activatedProfileId != -1) {
                boolean found = false;
                String[] splits = _startWhenActivatedProfile.split(StringConstants.STR_SPLIT_REGEX);
                for (String split : splits) {
                    if (activatedProfileId == Long.parseLong(split)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // if activated profile is not _startWhenActivatedProfile, not start event
                    return;
                }
            }
        }

        List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(false);

        // search for running event with higher priority
        if (ApplicationPreferences.applicationEventUsePriority) {
            for (EventTimeline eventTimeline : eventTimelineList) {
                int priority = dataWrapper.getEventPriority(eventTimeline._fkEvent);
                if ((this._priority != EPRIORITY_DO_NOT_USE) &&
                        (priority != EPRIORITY_DO_NOT_USE) &&
                        (priority > this._priority)) {
                    // is running event with higher priority
                    return;
                }
            }
        }

        // if application is restarted by system, ignore manual profile activation
        if (_ignoreManualActivation && PPApplication.normalServiceStart)
            EventStatic.setForceRunEventRunning(dataWrapper.context, true);

        EventTimeline eventTimeline;

    /////// delete duplicate from timeline
        boolean exists = true;
        while (exists)
        {
            //exists = false;

            //int timeLineSize = eventTimelineList.size();

            // test whenever event exists in timeline
            eventTimeline = null;
            int eventPosition = getEventTimelinePosition(eventTimelineList);
            if (eventPosition != -1)
                eventTimeline = eventTimelineList.get(eventPosition);

            exists = eventPosition != -1;

            if (exists)
            {
                // remove event from timeline
                eventTimelineList.remove(eventTimeline);
                DatabaseHandler.getInstance(dataWrapper.context).deleteEventTimeline(eventTimeline);

                /*if (eventPosition < (timeLineSize-1))
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
                */

            }
        }
    //////////////////////////////////

        addEventTimeline(dataWrapper, eventTimelineList/*, mergedProfile*/);


        setSystemEvent(dataWrapper.context, ESTATUS_RUNNING);
        int status = this._status;
        this._status = ESTATUS_RUNNING;
        DatabaseHandler.getInstance(dataWrapper.context).updateEventStatus(this);

        if (/*log && */(status != this._status)) {
            PPApplicationStatic.addActivityLog(dataWrapper.context, PPApplication.ALTYPE_EVENT_START, _name, null, "");
        }

        if (this._fkProfileStart != Profile.PROFILE_NO_ACTIVATE) {
            if (mergedProfile == null) {
                if ((PPApplication.applicationFullyStarted && PPApplication.normalServiceStart) || // normalServiceStart=true = it is not restart of application by system
                    (!this._ignoreManualActivation) ||
                    (!DataWrapperStatic.getIsManualProfileActivation(false, dataWrapper.context))) {
                    long activatedProfileId = dataWrapper.getActivatedProfileId();
                    if (this._manualProfileActivation || forRestartEvents || (this._fkProfileStart != activatedProfileId)) {
                        dataWrapper.activateProfileFromEvent(/*this._id,*/ this._fkProfileStart, false, false,
                                forRestartEvents, manualRestart,
                                _atEndDo == EATENDDO_UNDONE_PROFILE); // do not add this profile to fifo
                    } else {
//                        PPApplicationStatic.logE("[PPP_NOTIFICATION] Event.startEvent (1)", "call of updateGUI");
                        PPApplication.updateGUI(false, false, dataWrapper.context);
                    }
                }
                else {
//                    PPApplicationStatic.logE("[PPP_NOTIFICATION] Event.startEvent (2)", "call of updateGUI");
                    PPApplication.updateGUI(false, false, dataWrapper.context);
                }
            } else {
                if ((PPApplication.applicationFullyStarted && PPApplication.normalServiceStart) || // normalServiceStart=true = it is not restart of application by system
                    (!this._ignoreManualActivation) ||
                    (!DataWrapperStatic.getIsManualProfileActivation(false, dataWrapper.context))) {
                    mergedProfile.mergeProfiles(this._fkProfileStart, dataWrapper/*, true*/);
                    if (this._manualProfileActivation) {
                        DatabaseHandler.getInstance(dataWrapper.context).saveMergedProfile(mergedProfile);
                        dataWrapper.activateProfileFromEvent(/*this._id,*/ mergedProfile._id, true, true,
                                forRestartEvents, manualRestart, false);
                        mergedProfile._id = 0;
                    } else {
                        long profileId = _fkProfileStart;
                        if (_atEndDo != EATENDDO_UNDONE_PROFILE) {
                            // add this profile to fifo
                            dataWrapper.fifoAddProfile(profileId, _id);
                        }
                    }
                } else {
//                    PPApplicationStatic.logE("[PPP_NOTIFICATION] Event.startEvent (3)", "call of updateGUI");
                    PPApplication.updateGUI(false, false, dataWrapper.context);
                }
            }
        }

        //return;
    }

    private void doActivateEndProfile(DataWrapper dataWrapper,
                                        //int eventPosition,
                                        //int timeLineSize,
                                        //List<EventTimeline> eventTimelineList,
                                        //EventTimeline eventTimeline,
                                        boolean activateReturnProfile,
                                        Profile mergedProfile,
                                        boolean allowRestart,
                                        boolean forRestartEvents,
                                        boolean manualRestart,
                                        boolean updateGUI)
    {

//        if (!(eventPosition == (timeLineSize-1)))
//        {
//            // event is not in end of timeline
//
//            // check whether events behind have set _fkProfileEnd or _undoProfile
//            // when true, no activate "end profile"
//            /*for (int i = eventPosition; i < (timeLineSize-1); i++)
//            {
//                if (_fkProfileEnd != Event.PROFILE_END_NO_ACTIVATE)
//                    return;
//                if (_undoneProfile)
//                    return;
//            }*/
//            return;
//        }

        boolean profileActivated = false;
        if ((PPApplication.applicationFullyStarted && PPApplication.normalServiceStart) || // normalServiceStart=true = it is not restart of application by system
            (!this._ignoreManualActivation) ||
            (!DataWrapperStatic.getIsManualProfileActivation(false, dataWrapper.context))) {
            if (activateReturnProfile/* && canActivateReturnProfile()*/) {
                if (mergedProfile == null) {
                    // this is called for stop event, for update event parameter changes, for run/stop event
                    // and if event is not runnable or not all parameters are configured

                    long activatedProfileId = dataWrapper.getActivatedProfileId();
                    // first activate _fkProfileEnd
                    if (_fkProfileEnd != Profile.PROFILE_NO_ACTIVATE) {
                        if (_manualProfileActivationAtEnd || (_fkProfileEnd != activatedProfileId) || forRestartEvents) {
                            dataWrapper.activateProfileFromEvent(/*_id,*/ _fkProfileEnd, false, false,
                                    forRestartEvents, manualRestart,
                                    _atEndDo == EATENDDO_UNDONE_PROFILE); // do not add this profile to fifo!!!
                            activatedProfileId = _fkProfileEnd;
                            profileActivated = true;
                        }
                    }
                    // second activate when undone profile is set
                    if (_atEndDo == EATENDDO_UNDONE_PROFILE) {
                        long activateProfile;
                        /*if (_atEndHowUndo == 0) {
                            if (!(eventPosition == (timeLineSize-1))) {
                                // when in timeline list is event, get start profile from last event in timeline list
                                // because last event in timeline list may be changed
                                if (eventTimelineList.size() > 0) {
                                    // get latest running event
                                    EventTimeline _eventTimeline = eventTimelineList.get(eventTimelineList.size() - 1);
                                    if (_eventTimeline != null) {
                                        Event event = dataWrapper.getEventById(_eventTimeline._fkEvent);
                                        if (event != null)
                                            eventTimeline._fkProfileEndActivated = event._fkProfileStart;
                                    }
                                } else {
                                    long defaultProfileId = ApplicationPreferences.applicationDefaultProfile;
                                    //if (!fullyStarted)
                                    //    defaultProfileId = Profile.PROFILE_NO_ACTIVATE;
                                    if (defaultProfileId != Profile.PROFILE_NO_ACTIVATE) {
                                        eventTimeline._fkProfileEndActivated = defaultProfileId;
                                    }
                                }
                            } else
                                eventTimeline._fkProfileEndActivated = 0;
                        } else*/
                        {
//                            PPApplicationStatic.logE("[SYNCHRONIZED] Event.doActivateEndProfile", "(1) PPApplication.profileActivationMutex");
                            synchronized (PPApplication.profileActivationMutex) {
                                List<String> activateProfilesFIFO = dataWrapper.fifoGetActivatedProfiles();
                                int size = activateProfilesFIFO.size();
                                if (size > 0) {
                                    // get profile which will be undoed
                                    int index = size - 1;
                                    String fromFifo = activateProfilesFIFO.get(index);
                                    String[] splits = fromFifo.split(StringConstants.STR_SPLIT_REGEX);
                                    activateProfile = Long.parseLong(splits[0]);

                                    // and remove it
                                    //activateProfilesFIFO.remove(size - 1);
                                    //dataWrapper.fifoSaveProfiles(activateProfilesFIFO);
                                } else
                                    //eventTimeline._fkProfileEndActivated = 0;
                                    activateProfile = 0;
                            }

                            //if (eventTimeline._fkProfileEndActivated == 0) {
                            if (activateProfile == 0) {
                                long defaultProfileId = ApplicationPreferences.getApplicationDefaultProfileOnBoot();

                                //if (!fullyStarted)
                                //    defaultProfileId = Profile.PROFILE_NO_ACTIVATE;
                                if (defaultProfileId != Profile.PROFILE_NO_ACTIVATE) {
                                    //eventTimeline._fkProfileEndActivated = defaultProfileId;
                                    activateProfile = defaultProfileId;
                                }
                            }
                        }
                        //if ((eventTimeline._fkProfileEndActivated != activatedProfileId) || forRestartEvents)
                        if (_manualProfileActivationAtEnd || (activateProfile != activatedProfileId) || forRestartEvents) {
                            //if (eventTimeline._fkProfileEndActivated != 0)
                            if (activateProfile != 0) {
                                // do not save to fifo profile with event for Undo
                                dataWrapper.activateProfileFromEvent(/*0,*/ activateProfile, false, false, forRestartEvents, manualRestart, true);
                                profileActivated = true;
                            }
                        }
                    }
                } else {
                    // first activate _fkProfileEnd
                    if (_fkProfileEnd != Profile.PROFILE_NO_ACTIVATE) {
                        mergedProfile.mergeProfiles(_fkProfileEnd, dataWrapper/*, false*/);

                        if (_manualProfileActivationAtEnd) {
                            DatabaseHandler.getInstance(dataWrapper.context).saveMergedProfile(mergedProfile);
                            dataWrapper.activateProfileFromEvent(/*this._id,*/ mergedProfile._id, true, true,
                                    forRestartEvents, manualRestart,
                                    (_atEndDo == EATENDDO_UNDONE_PROFILE));
                            mergedProfile._id = 0;
                        } else {
                            long profileId = _fkProfileEnd;
                            if (_atEndDo != EATENDDO_UNDONE_PROFILE) {
                                dataWrapper.fifoAddProfile(profileId, _id);
                            }
                        }
                    }
                    // second activate when undone profile is set
                    if (_atEndDo == EATENDDO_UNDONE_PROFILE) {
                        long activateProfile;
                        /*if (_atEndHowUndo == 0) {
                            if (!(eventPosition == (timeLineSize-1))) {
                                // when in timeline list is event, get start profile from last event in timeline list
                                // because last event in timeline list may be changed
                                if (eventTimelineList.size() > 0) {
                                    // get latest running evemt
                                    EventTimeline _eventTimeline = eventTimelineList.get(eventTimelineList.size() - 1);
                                    if (_eventTimeline != null) {
                                        Event event = dataWrapper.getEventById(_eventTimeline._fkEvent);
                                        if (event != null)
                                            eventTimeline._fkProfileEndActivated = event._fkProfileStart;
                                    }
                                } else {
                                    long defaultProfileId = ApplicationPreferences.applicationDefaultProfile;
                                    //if (!fullyStarted)
                                    //    defaultProfileId = Profile.PROFILE_NO_ACTIVATE;
                                    if (defaultProfileId != Profile.PROFILE_NO_ACTIVATE) {
                                        eventTimeline._fkProfileEndActivated = defaultProfileId;
                                    }
                                }
                            } else
                                eventTimeline._fkProfileEndActivated = 0;
                        } else*/
                        {
//                            PPApplicationStatic.logE("[SYNCHRONIZED] Event.doActivateEndProfile", "(2) PPApplication.profileActivationMutex");
                            synchronized (PPApplication.profileActivationMutex) {
                                List<String> activateProfilesFIFO = dataWrapper.fifoGetActivatedProfiles();
                                int size = activateProfilesFIFO.size();
                                if (size > 0) {
                                    // get profile which will be undoed
                                    int index = size - 1;
                                    String fromFifo = activateProfilesFIFO.get(index);
                                    String[] splits = fromFifo.split(StringConstants.STR_SPLIT_REGEX);
                                    activateProfile = Long.parseLong(splits[0]);

                                    // and remove it
                                    //activateProfilesFIFO.remove(size - 1);
                                    //dataWrapper.fifoSaveProfiles(activateProfilesFIFO);
                                } else
                                    //eventTimeline._fkProfileEndActivated = 0;
                                    activateProfile = 0;
                            }

                            //if (eventTimeline._fkProfileEndActivated == 0) {
                            if (activateProfile == 0) {
                                long defaultProfileId = ApplicationPreferences.getApplicationDefaultProfileOnBoot();
                                //if (!fullyStarted)
                                //    defaultProfileId = Profile.PROFILE_NO_ACTIVATE;
                                if (defaultProfileId != Profile.PROFILE_NO_ACTIVATE) {
                                    //eventTimeline._fkProfileEndActivated = defaultProfileId;
                                    activateProfile = defaultProfileId;
                                }
                            }
                        }
                        //if (eventTimeline._fkProfileEndActivated != 0)
                        if (activateProfile != 0) {
                            //mergedProfile.mergeProfiles(eventTimeline._fkProfileEndActivated, dataWrapper/*, false*/);
                            mergedProfile.mergeProfiles(activateProfile, dataWrapper/*, false*/);

                            if (_manualProfileActivationAtEnd) {
                                DatabaseHandler.getInstance(dataWrapper.context).saveMergedProfile(mergedProfile);
                                // do not save to fifo profile with event for Undo
                                // profile is alrady in FIFO
                                dataWrapper.activateProfileFromEvent(/*0,*/ mergedProfile._id, true, true,
                                        forRestartEvents, manualRestart, true);
                                mergedProfile._id = 0;
                            }
                        }
                    }
                }

                // restart events when is set
                if ((_atEndDo == EATENDDO_RESTART_EVENTS) && allowRestart && !forRestartEvents) {
                    // test of forRestartEvents is required!!!
                    // Do not restart events when is event paused during restart events !!!
                    // do not reactivate profile to avoid infinite loop

                    dataWrapper.restartEventsWithDelay(/*false,*/ false, true, false, PPApplication.ALTYPE_UNDEFINED);

                    // keep wakelock awake 5 secods
                    // this may do restart after 5 seconds also in Doze mode
                    // removed: consume battery
                    //GlobalUtils.sleep(5000);

                    profileActivated = true;
                }

            }
        }

        if ((!profileActivated) && updateGUI)
        {
//            PPApplicationStatic.logE("[PPP_NOTIFICATION] Event.doActivateEndProfile", "call of updateGUI");
            PPApplication.updateGUI(false, false, dataWrapper.context);
        }

    }

    void pauseEvent(DataWrapper dataWrapper,
                            boolean activateReturnProfile,
                            boolean ignoreGlobalPref,
                            boolean noSetSystemEvent,
                            boolean log,
                            Profile mergedProfile,
                            boolean allowRestart,
                            boolean forRestartEvents,
                            boolean manualRestart,
                            boolean updateGUI)
    {
        //_undoCalled = false;

        // remove delay alarm
        removeDelayStartAlarm(dataWrapper); // for start delay
        removeDelayEndAlarm(dataWrapper); // for end delay
        removeStartEventNotificationAlarm(dataWrapper); // for start repeating notification

        if ((!EventStatic.getGlobalEventsRunning(dataWrapper.context)) && (!ignoreGlobalPref))
            // events are globally stopped
            return;

        // !!! Pause event even if is when not good configured !!!
        //if (!(this.isRunnable(dataWrapper.context, true) && this.isAllConfigured(dataWrapper.context, true)))
            // event is not runnable, no pause it
        //    return;

/*		if (PPApplication.getEventsBlocked(dataWrapper.context))
        {
            // blocked by manual profile activation


            if (!_forceRun)
                // event is not forceRun
                return;
        }
*/

        // unblock event when paused
        dataWrapper.setEventBlocked(this, false);

        List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(false);

        //int timeLineSize = eventTimelineList.size();

        // test whenever event exists in timeline
        int eventPosition = getEventTimelinePosition(eventTimelineList);

        boolean exists = eventPosition != -1;

        EventTimeline eventTimeline;// = null;

        if (exists)
        {
            eventTimeline = eventTimelineList.get(eventPosition);

            // remove event from timeline
            eventTimelineList.remove(eventTimeline);
            DatabaseHandler.getInstance(dataWrapper.context).deleteEventTimeline(eventTimeline);

            /*
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
            */
        }

        if (!noSetSystemEvent)
            setSystemEvent(dataWrapper.context, ESTATUS_PAUSE);
        int status = this._status;
        this._status = ESTATUS_PAUSE;
        DatabaseHandler.getInstance(dataWrapper.context).updateEventStatus(this);

        if (log && (status != this._status)) {
            doLogForPauseEvent(dataWrapper.context, allowRestart && (!forRestartEvents));
        }


        //if (_forceRun)
        //{ look for forceRun events always, not only when forceRun event is paused
            boolean forceRunRunning = false;
            for (EventTimeline _eventTimeline : eventTimelineList)
            {
                int ignoreManualActivation = dataWrapper.getEventIgnoreManualActivation(_eventTimeline._fkEvent);
                // if application is restarted by system, ignore manual profile activation
                if ((ignoreManualActivation == 1) && PPApplication.normalServiceStart)
                {
                    forceRunRunning = true;
                    break;
                }
            }

            if (!forceRunRunning)
                EventStatic.setForceRunEventRunning(dataWrapper.context, false);
        //}

        if (exists)
        {
            doActivateEndProfile(dataWrapper, /*eventPosition, timeLineSize,
                    eventTimelineList, eventTimeline,*/
                    activateReturnProfile, mergedProfile, allowRestart,
                    forRestartEvents, manualRestart, updateGUI);

        }

        //return;
    }

    void doLogForPauseEvent(Context context, boolean allowRestart) {
        int alType = PPApplication.ALTYPE_EVENT_END_NONE;
        if ((_atEndDo == EATENDDO_UNDONE_PROFILE) && /*(_atEndHowUndo == 0) &&*/ (_fkProfileEnd != Profile.PROFILE_NO_ACTIVATE))
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
        else if (_fkProfileEnd != Profile.PROFILE_NO_ACTIVATE) {
            if (allowRestart)
                alType = PPApplication.ALTYPE_EVENT_END_ACTIVATE_PROFILE;
        }

        PPApplicationStatic.addActivityLog(context.getApplicationContext(), alType, _name, null, "");
    }

    void stopEvent(DataWrapper dataWrapper,
                            boolean activateReturnProfile,
                            boolean ignoreGlobalPref,
                            boolean saveEventStatus,
                            boolean log,
                            boolean updateGUI)
    {
        // remove delay alarm
        removeDelayStartAlarm(dataWrapper); // for start delay
        removeDelayEndAlarm(dataWrapper); // for end delay
        removeStartEventNotificationAlarm(dataWrapper); // for start repeating notification

        if ((!EventStatic.getGlobalEventsRunning(dataWrapper.context)) && (!ignoreGlobalPref))
            // events are globally stopped
            return;

        if (this._status != ESTATUS_STOP)
        {
            pauseEvent(dataWrapper, activateReturnProfile, ignoreGlobalPref, true, false,
                    null, false/*allowRestart*/, false, false, updateGUI);
        }

        setSystemEvent(dataWrapper.context, ESTATUS_STOP);
        int status = this._status;
        this._status = ESTATUS_STOP;

        if (saveEventStatus)
            DatabaseHandler.getInstance(dataWrapper.context).updateEventStatus(this);

        setSensorsWaiting();
        if (saveEventStatus)
            DatabaseHandler.getInstance(dataWrapper.context).updateAllEventSensorsPassed(this);

        if (log && (status != this._status)) {
            PPApplicationStatic.addActivityLog(dataWrapper.context, PPApplication.ALTYPE_EVENT_STOP, _name, null, "");
        }

        //return;
    }

    int getStatus()
    {
        return _status;
    }

    int getStatusFromDB(Context context)
    {
        return DatabaseHandler.getInstance(context.getApplicationContext()).getEventStatus(this);
    }

    void setStatus(int status)
    {
        _status = status;
    }

    void setSensorsWaiting() {
        _eventPreferencesApplication.setSensorPassed(_eventPreferencesApplication.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesBattery.setSensorPassed(_eventPreferencesBattery.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesBluetooth.setSensorPassed(_eventPreferencesBluetooth.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesCalendar.setSensorPassed(_eventPreferencesCalendar.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesCall.setSensorPassed(_eventPreferencesCall.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesLocation.setSensorPassed(_eventPreferencesLocation.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesMobileCells.setSensorPassed(_eventPreferencesMobileCells.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesNFC.setSensorPassed(_eventPreferencesNFC.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesNotification.setSensorPassed(_eventPreferencesNotification.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesOrientation.setSensorPassed(_eventPreferencesOrientation.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesAccessories.setSensorPassed(_eventPreferencesAccessories.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesRadioSwitch.setSensorPassed(_eventPreferencesRadioSwitch.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesScreen.setSensorPassed(_eventPreferencesScreen.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesBrightness.setSensorPassed(_eventPreferencesBrightness.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesSMS.setSensorPassed(_eventPreferencesSMS.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesTime.setSensorPassed(_eventPreferencesTime.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesWifi.setSensorPassed(_eventPreferencesWifi.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesAlarmClock.setSensorPassed(_eventPreferencesAlarmClock.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesDeviceBoot.setSensorPassed(_eventPreferencesDeviceBoot.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesSoundProfile.setSensorPassed(_eventPreferencesSoundProfile.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesPeriodic.setSensorPassed(_eventPreferencesPeriodic.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesVolumes.setSensorPassed(_eventPreferencesVolumes.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesActivatedProfile.setSensorPassed(_eventPreferencesActivatedProfile.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesRoaming.setSensorPassed(_eventPreferencesRoaming.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesVPN.setSensorPassed(_eventPreferencesVPN.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesMusic.setSensorPassed(_eventPreferencesMusic.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
        _eventPreferencesCallScreening.setSensorPassed(_eventPreferencesCallScreening.getSensorPassed() | EventPreferences.SENSOR_PASSED_WAITING);
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
            _eventPreferencesAccessories.setSystemEventForStart(context);
            _eventPreferencesCalendar.setSystemEventForStart(context);
            _eventPreferencesWifi.setSystemEventForStart(context);
            _eventPreferencesScreen.setSystemEventForStart(context);
            _eventPreferencesBrightness.setSystemEventForStart(context);
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
            _eventPreferencesDeviceBoot.setSystemEventForStart(context);
            _eventPreferencesSoundProfile.setSystemEventForStart(context);
            _eventPreferencesPeriodic.setSystemEventForStart(context);
            _eventPreferencesVolumes.setSystemEventForStart(context);
            _eventPreferencesActivatedProfile.setSystemEventForStart(context);
            _eventPreferencesRoaming.setSystemEventForStart(context);
            _eventPreferencesVPN.setSystemEventForStart(context);
            _eventPreferencesMusic.setSystemEventForStart(context);
            _eventPreferencesCallScreening.setSystemEventForStart(context);
        }
        else
        if (forStatus == ESTATUS_RUNNING)
        {
            // event started
            // setup system event for pause status
            _eventPreferencesTime.setSystemEventForPause(context);
            _eventPreferencesBattery.setSystemEventForPause(context);
            _eventPreferencesCall.setSystemEventForPause(context);
            _eventPreferencesAccessories.setSystemEventForPause(context);
            _eventPreferencesCalendar.setSystemEventForPause(context);
            _eventPreferencesWifi.setSystemEventForPause(context);
            _eventPreferencesScreen.setSystemEventForPause(context);
            _eventPreferencesBrightness.setSystemEventForPause(context);
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
            _eventPreferencesDeviceBoot.setSystemEventForPause(context);
            _eventPreferencesSoundProfile.setSystemEventForPause(context);
            _eventPreferencesPeriodic.setSystemEventForPause(context);
            _eventPreferencesVolumes.setSystemEventForPause(context);
            _eventPreferencesActivatedProfile.setSystemEventForPause(context);
            _eventPreferencesRoaming.setSystemEventForPause(context);
            _eventPreferencesVPN.setSystemEventForPause(context);
            _eventPreferencesMusic.setSystemEventForPause(context);
            _eventPreferencesCallScreening.setSystemEventForPause(context);
        }
        else
        if (forStatus == ESTATUS_STOP)
        {
            // event stopped
            // remove all system events
            _eventPreferencesTime.removeSystemEvent(context);
            _eventPreferencesBattery.removeSystemEvent(context);
            _eventPreferencesCall.removeSystemEvent(context);
            _eventPreferencesAccessories.removeSystemEvent(context);
            _eventPreferencesCalendar.removeSystemEvent(context);
            _eventPreferencesWifi.removeSystemEvent(context);
            _eventPreferencesScreen.removeSystemEvent(context);
            _eventPreferencesBrightness.removeSystemEvent(context);
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
            _eventPreferencesDeviceBoot.removeSystemEvent(context);
            _eventPreferencesSoundProfile.removeSystemEvent(context);
            _eventPreferencesPeriodic.removeSystemEvent(context);
            _eventPreferencesVolumes.removeSystemEvent(context);
            _eventPreferencesActivatedProfile.removeSystemEvent(context);
            _eventPreferencesRoaming.removeSystemEvent(context);
            _eventPreferencesVPN.removeSystemEvent(context);
            _eventPreferencesMusic.removeSystemEvent(context);
            _eventPreferencesCallScreening.removeSystemEvent(context);
        }
    }

    void setDelayStartAlarm(DataWrapper dataWrapper)
    {
        removeDelayStartAlarm(dataWrapper);

        if (!EventStatic.getGlobalEventsRunning(dataWrapper.context))
            // events are globally stopped
            return;

        if (!(this.isRunnable(dataWrapper.context, true) && this.isAllConfigured(dataWrapper.context, true)))
            // event is not runnable, no pause it
            return;

        //if (ApplicationPreferences.prefEventsBlocked)
        if (EventStatic.getEventsBlocked(dataWrapper.context))
        {
            // blocked by manual profile activation


            // if application is restarted by system, ignore manual profile activation
            if ((!_ignoreManualActivation) || (!PPApplication.normalServiceStart))
                // event is not forceRun
                return;
            if (_blocked)
                // forceRun event is temporary blocked
                return;
        }

        if (getStatus() == ESTATUS_RUNNING)
            // event is already in running status
            return;

        if (this._delayStart > 0)
        {
            Context _context = dataWrapper.context;

            // delay for start is > 0
            // set alarm

            if (!PPApplicationStatic.isIgnoreBatteryOptimizationEnabled(_context)) {
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

                        Intent editorIntent = new Intent(_context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(_context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);

                        now = Calendar.getInstance();
                        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                        this._startStatusTime = now.getTimeInMillis() - gmtOffset;
                        this._isInDelayStart = true;
                    } else {
                        this._startStatusTime = 0;
                        this._isInDelayStart = false;
                    }
                } else {
                /*int keepResultsDelay = (this._delayStart * 5) / 60; // conversion to minutes
                if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
                    keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/

//                    PPApplicationStatic.logE("[MAIN_WORKER_CALL] Event.setDelayStartAlarm", "xxxxxxxxxxxxxxxxxxxx");

                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(MainWorker.class)
                                    .addTag(MainWorker.EVENT_DELAY_START_WORK_TAG + "_" + (int) this._id)
                                    .setInitialDelay(this._delayStart, TimeUnit.SECONDS)
                                    .build();
                    try {
                        if (PPApplicationStatic.getApplicationStarted(true, true)) {
                            WorkManager workManager = PPApplication.getWorkManagerInstance();
                            if (workManager != null) {
//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.EVENT_DELAY_START_TAG_WORK +"_"+(int) this._id);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                                PPApplicationStatic.logE("[WORKER_CALL] Event.setDelayStartAlarm", "xxx");
                                workManager.enqueueUniqueWork(MainWorker.EVENT_DELAY_START_WORK_TAG + "_" + (int) this._id, ExistingWorkPolicy.REPLACE, worker);
                                PPApplication.elapsedAlarmsEventDelayStartWork.add(MainWorker.EVENT_DELAY_START_WORK_TAG + "_" + (int) this._id);

                                Calendar now = Calendar.getInstance();
                                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                                this._startStatusTime = now.getTimeInMillis() - gmtOffset;
                                this._isInDelayStart = true;
                            } else {
                                this._startStatusTime = 0;
                                this._isInDelayStart = false;
                            }
                        } else {
                            this._startStatusTime = 0;
                            this._isInDelayStart = false;
                        }
                    } catch (Exception e) {
                        this._startStatusTime = 0;
                        this._isInDelayStart = false;
                    }
                }
            }
            else {
                //Intent intent = new Intent(_context, EventDelayStartBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_EVENT_DELAY_START_BROADCAST_RECEIVER);
                //intent.setClass(context, EventDelayStartBroadcastReceiver.class);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, this._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, (int) this._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {

                        Calendar now = Calendar.getInstance();
                        now.add(Calendar.SECOND, this._delayStart);
                        long alarmTime = now.getTimeInMillis();

                        Intent editorIntent = new Intent(_context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(_context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    } else {
                        long alarmTime = SystemClock.elapsedRealtime() + this._delayStart * 1000L;

                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    }

                    Calendar now = Calendar.getInstance();
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    this._startStatusTime = now.getTimeInMillis() - gmtOffset;
                    this._isInDelayStart = true;
                } else {
                    this._startStatusTime = 0;
                    this._isInDelayStart = false;
                }
            }
        }
        else {
            this._startStatusTime = 0;
            this._isInDelayStart = false;
        }

        DatabaseHandler.getInstance(dataWrapper.context).updateEventInDelayStart(this);

        if (_isInDelayStart) {
            String evenName = _name + " (" + StringConstants.EVENT_DEALY_START + " " +
                    StringFormatUtils.getDurationString(_delayStart) +")";
            PPApplicationStatic.addActivityLog(dataWrapper.context, PPApplication.ALTYPE_EVENT_START_DELAY, evenName, null, "");
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

        now.setTimeInMillis(this._startStatusTime);
        now.add(Calendar.SECOND, this._delayStart);
        long delayTime = now.getTimeInMillis();

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
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        PPApplicationStatic.cancelWork(MainWorker.EVENT_DELAY_START_WORK_TAG +"_"+((int) this._id), false);
        // moved to cancelWork
        //PPApplication.elapsedAlarmsEventDelayStartWork.remove(MainWorker.EVENT_DELAY_START_TAG_WORK +"_"+((int) this._id));

        this._isInDelayStart = false;
        this._startStatusTime = 0;
        DatabaseHandler.getInstance(dataWrapper.context).updateEventInDelayStart(this);
    }

    void setDelayEndAlarm(DataWrapper dataWrapper, boolean forRestartEvents)
    {
        removeDelayEndAlarm(dataWrapper);

        if (!EventStatic.getGlobalEventsRunning(dataWrapper.context))
            // events are globally stopped
            return;

        if (!(this.isRunnable(dataWrapper.context, true) && this.isAllConfigured(dataWrapper.context, true)))
            // event is not runnable, no pause it
            return;

        //if (ApplicationPreferences.prefEventsBlocked)
        if (EventStatic.getEventsBlocked(dataWrapper.context))
        {
            // blocked by manual profile activation

            // if application is restarted by system, ignore manual profile activation
            if ((!_ignoreManualActivation) || (!PPApplication.normalServiceStart))
                // event is not forceRun
                return;
            if (_blocked)
                // forceRun event is temporary blocked
                return;
        }

        if (getStatus() == ESTATUS_PAUSE)
            // event is already in pause status
            return;

        if (forRestartEvents)
            // for restart events do not use delayEnd
            return;

        if (this._delayEnd > 0)
        {
            Context _context = dataWrapper.context;

            // delay for end is > 0
            // set alarm

            if (!PPApplicationStatic.isIgnoreBatteryOptimizationEnabled(_context)) {
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

                        Intent editorIntent = new Intent(_context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(_context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);

                        now = Calendar.getInstance();
                        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                        this._pauseStatusTime = now.getTimeInMillis() - gmtOffset;
                        this._isInDelayEnd = true;
                    } else {
                        this._pauseStatusTime = 0;
                        this._isInDelayEnd = false;
                    }
                } else {
                /*int keepResultsDelay = (this._delayEnd * 5) / 60; // conversion to minutes
                if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
                    keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/

//                    PPApplicationStatic.logE("[MAIN_WORKER_CALL] Event.setDelayEndAlarm", "xxxxxxxxxxxxxxxxxxxx");

                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(MainWorker.class)
                                    .addTag(MainWorker.EVENT_DELAY_END_WORK_TAG + "_" + (int) this._id)
                                    .setInitialDelay(this._delayEnd, TimeUnit.SECONDS)
                                    .build();
                    try {
                        if (PPApplicationStatic.getApplicationStarted(true, true)) {
                            WorkManager workManager = PPApplication.getWorkManagerInstance();
                            if (workManager != null) {
//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.EVENT_DELAY_END_TAG_WORK +"_"+(int) this._id);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                                PPApplicationStatic.logE("[WORKER_CALL] Event.setDelayEndAlarm", "xxx");
                                workManager.enqueueUniqueWork(MainWorker.EVENT_DELAY_END_WORK_TAG + "_" + (int) this._id, ExistingWorkPolicy.REPLACE, worker);
                                PPApplication.elapsedAlarmsEventDelayEndWork.add(MainWorker.EVENT_DELAY_END_WORK_TAG + "_" + (int) this._id);

                                Calendar now = Calendar.getInstance();
                                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                                this._pauseStatusTime = now.getTimeInMillis() - gmtOffset;
                                this._isInDelayEnd = true;
                            } else {
                                this._pauseStatusTime = 0;
                                this._isInDelayEnd = false;
                            }
                        } else {
                            this._pauseStatusTime = 0;
                            this._isInDelayEnd = false;
                        }
                    } catch (Exception e) {
                        this._pauseStatusTime = 0;
                        this._isInDelayEnd = false;
                    }
                }
            }
            else {

                //Intent intent = new Intent(_context, EventDelayEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_EVENT_DELAY_END_BROADCAST_RECEIVER);
                //intent.setClass(context, EventDelayEndBroadcastReceiver.class);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, this._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, (int) this._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);

                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {

                        Calendar now = Calendar.getInstance();
                        now.add(Calendar.SECOND, this._delayEnd);
                        long alarmTime = now.getTimeInMillis(); // + 1000 * /* 60 * */ this._delayEnd;

                        Intent editorIntent = new Intent(_context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(_context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    } else {
                        long alarmTime = SystemClock.elapsedRealtime() + this._delayEnd * 1000L;

                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                        //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                    }

                    Calendar now = Calendar.getInstance();
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    this._pauseStatusTime = now.getTimeInMillis() - gmtOffset;
                    this._isInDelayEnd = true;
                } else {
                    this._pauseStatusTime = 0;
                    this._isInDelayEnd = false;
                }
            }
        }
        else {
            this._pauseStatusTime = 0;
            this._isInDelayEnd = false;
        }

        DatabaseHandler.getInstance(dataWrapper.context).updateEventInDelayEnd(this);

        if (_isInDelayEnd) {
            String evenName = _name + " (" + StringConstants.EVENT_DEALY_END + " " +
                    StringFormatUtils.getDurationString(_delayEnd) +")";
            PPApplicationStatic.addActivityLog(dataWrapper.context, PPApplication.ALTYPE_EVENT_END_DELAY, evenName, null, "");
        }

        //return;
    }

    void checkDelayEnd(/*DataWrapper dataWrapper*/) {
        if (this._pauseStatusTime == 0) {
            this._isInDelayEnd = false;
            return;
        }

        Calendar now = Calendar.getInstance();
        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
        long nowTime = now.getTimeInMillis() - gmtOffset;

        now.setTimeInMillis(this._pauseStatusTime);
        now.add(Calendar.SECOND, this._delayEnd);
        long delayTime = now.getTimeInMillis() - gmtOffset;

        /*
        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");

        String result = sdf.format(nowTime);

        result = sdf.format(this._pauseStatusTime);

        result = sdf.format(delayTime);
        */

        if (nowTime > delayTime)
            this._isInDelayEnd = false;

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
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        PPApplicationStatic.cancelWork(MainWorker.EVENT_DELAY_END_WORK_TAG +"_"+((int) this._id), false);
        // moved to cancelWork
        //PPApplication.elapsedAlarmsEventDelayEndWork.remove(MainWorker.EVENT_DELAY_END_TAG_WORK +"_"+((int) this._id));

        this._isInDelayEnd = false;
        DatabaseHandler.getInstance(dataWrapper.context).updateEventInDelayEnd(this);
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
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(dataWrapper.context);
                try {
                    notificationManager.cancel(
                            PPApplication.NOTIFY_EVENT_START_NOTIFICATION_TAG+"_"+_id,
                            PPApplication.NOTIFY_EVENT_START_NOTIFICATION_ID + (int) _id);
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
                StartEventNotificationBroadcastReceiver.removeAlarm(this, dataWrapper.context);
            //}
        }
    }

    //----------------------------------

    void notifyEventStart(Context context, /*boolean playSound,*/
                          @SuppressWarnings("SameParameterValue") boolean canPlayAlsoInSilentMode) {
        String notificationSoundStart = _notificationSoundStart;
        boolean notificationVibrateStart = _notificationVibrateStart;
        boolean playAlsoInSilentMode = false;
        if (canPlayAlsoInSilentMode)
            playAlsoInSilentMode = _notificationSoundStartPlayAlsoInSilentMode;

        if (!notificationSoundStart.isEmpty() || notificationVibrateStart) {

            if (_repeatNotificationStart) {
                NotificationCompat.Builder mBuilder;

                String nTitle = context.getString(R.string.start_event_notification_title);
                String nText = context.getString(R.string.start_event_notification_text1);
                nText = nText + StringConstants.STR_COLON_WITH_SPACE + _name;
                nText = nText + ". " + context.getString(R.string.start_event_notification_text2);
                PPApplicationStatic.createNotifyEventStartNotificationChannel(context.getApplicationContext(), false);
                mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), PPApplication.NOTIFY_EVENT_START_NOTIFICATION_CHANNEL)
                        .setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.informationColor))
                        .setSmallIcon(R.drawable.ic_ppp_notification/*ic_information_notify*/) // notification icon
                        .setLargeIcon(BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.ic_information_notification))
                        .setContentTitle(nTitle) // title for notification
                        .setContentText(nText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                        .setAutoCancel(false) // clear notification after click
                        .setOnlyAlertOnce(true);

                PendingIntent pi = PendingIntent.getActivity(context, (int) _id, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pi);
                mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                mBuilder.setCategory(NotificationCompat.CATEGORY_EVENT);
                mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                // Android 12 - this do not starts activity - OK
                Intent deleteIntent = new Intent(StartEventNotificationDeletedReceiver.ACTION_START_EVENT_NOTIFICATION_DELETED);
                deleteIntent.putExtra(PPApplication.EXTRA_EVENT_ID, _id);
                PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, (int) _id, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setDeleteIntent(deletePendingIntent);

                mBuilder.setGroup(PPApplication.NOTIFY_EVENT_START_NOTIFICATION_GROUP);

                Notification notification = mBuilder.build();

                NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
                try {
                    int notificationID = PPApplication.NOTIFY_EVENT_START_NOTIFICATION_ID + (int) _id;
                    String notificationTag = PPApplication.NOTIFY_EVENT_START_NOTIFICATION_TAG+"_"+_id;
                    //mNotificationManager.cancel(notificationTag, notificationID);
                    mNotificationManager.notify(notificationTag, notificationID, notification);
                } catch (SecurityException en) {
                    PPApplicationStatic.logException("Event.notifyEventStart", Log.getStackTraceString(en));
                } catch (Exception e) {
                    //Log.e("Event.notifyEventStart", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                }

                StartEventNotificationBroadcastReceiver.setAlarm(this, context);
            }

            //if (playSound)
            PlayRingingNotification.playNotificationSound(
                    notificationSoundStart,
                    notificationVibrateStart,
                    playAlsoInSilentMode, context);

            //return true;
        }
        //return false;
    }

    void notifyEventEnd(Context context, /*boolean playSound,*/
                           @SuppressWarnings("SameParameterValue") boolean canPlayAlsoInSilentMode) {
        String notificationSoundEnd = _notificationSoundEnd;
        boolean notificationVibrateEnd = _notificationVibrateEnd;
        boolean playAlsoInSilentMode = false;
        if (canPlayAlsoInSilentMode)
            playAlsoInSilentMode = _notificationSoundStartPlayAlsoInSilentMode;

        if (!notificationSoundEnd.isEmpty() || notificationVibrateEnd) {

            //if (playSound)
            PlayRingingNotification.playNotificationSound(
                    notificationSoundEnd,
                    notificationVibrateEnd,
                    playAlsoInSilentMode, context);

            //return true;
        }
        //return false;
    }

}

