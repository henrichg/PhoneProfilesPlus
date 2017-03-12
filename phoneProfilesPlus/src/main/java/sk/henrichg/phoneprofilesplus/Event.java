package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

class Event {

    long _id;
    String _name;
    int _startOrder;
    long _fkProfileStart;
    long _fkProfileEnd;
    //public boolean _undoneProfile;
    int _atEndDo;
    private int _status;
    String _notificationSound;
    boolean _forceRun;
    boolean _blocked;
    int _priority;
    int _delayStart;
    boolean _isInDelayStart;
    boolean _manualProfileActivation;
    long _fkProfileStartWhenActivated;
    int _delayEnd;
    boolean _isInDelayEnd;
    long _startStatusTime;
    long _pauseStatusTime;

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
    private static final String PREF_EVENT_NOTIFICATION_SOUND = "eventNotificationSound";
    private static final String PREF_EVENT_FORCE_RUN = "eventForceRun";
    //static final String PREF_EVENT_UNDONE_PROFILE = "eventUndoneProfile";
    static final String PREF_EVENT_PRIORITY = "eventPriority";
    private static final String PREF_EVENT_DELAY_START = "eventDelayStart";
    private static final String PREF_EVENT_AT_END_DO = "eventAtEndDo";
    private static final String PREF_EVENT_MANUAL_PROFILE_ACTIVATION = "manualProfileActivation";
    private static final String PREF_EVENT_START_WHEN_ACTIVATED_PROFILE = "eventStartWhenActivatedProfile";
    private static final String PREF_EVENT_DELAY_END = "eventDelayEnd";

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
                 String notificationSound,
                 boolean forceRun,
                 boolean blocked,
                 //boolean undoneProfile,
                 int priority,
                 int delayStart,
                 boolean isInDelayStart,
                 int atEndDo,
                 boolean manualProfileActivation,
                 long fkProfileStartWhenActivated,
                 int delayEnd,
                 boolean isInDelayEnd,
                 long startStatusTime,
                 long pauseStatusTime)
    {
        this._id = id;
        this._name = name;
        this._startOrder = startOrder;
        this._fkProfileStart = fkProfileStart;
        this._fkProfileEnd = fkProfileEnd;
        this._status = status;
        this._notificationSound = notificationSound;
        this._forceRun = forceRun;
        this._blocked = blocked;
        //this._undoneProfile = undoneProfile;
        this._priority = priority;
        this._delayStart = delayStart;
        this._isInDelayStart = isInDelayStart;
        this._atEndDo = atEndDo;
        this._manualProfileActivation = manualProfileActivation;
        this._fkProfileStartWhenActivated = fkProfileStartWhenActivated;
        this._delayEnd = delayEnd;
        this._isInDelayEnd = isInDelayEnd;
        this._startStatusTime = startStatusTime;
        this._pauseStatusTime = pauseStatusTime;

        createEventPreferences();

        //Log.e("Event", "this._fkProfileEnd=" + this._fkProfileEnd);
    }

    // constructor
    Event(String name,
                 int startOrder,
                 long fkProfileStart,
                 long fkProfileEnd,
                 int status,
                 String notificationSound,
                 boolean forceRun,
                 boolean blocked,
                 //boolean undoneProfile,
                 int priority,
                 int delayStart,
                 boolean isInDelayStart,
                 int atEndDo,
                 boolean manualProfileActivation,
                 long fkProfileStartWhenActivated,
                 int delayEnd,
                 boolean isInDelayEnd,
                 long startStatusTime,
                 long pauseStatusTime)
    {
        this._name = name;
        this._startOrder = startOrder;
        this._fkProfileStart = fkProfileStart;
        this._fkProfileEnd = fkProfileEnd;
        this._status = status;
        this._notificationSound = notificationSound;
        this._forceRun = forceRun;
        this._blocked = blocked;
        //this._undoneProfile = undoneProfile;
        this._priority = priority;
        this._delayStart = delayStart;
        this._isInDelayStart = isInDelayStart;
        this._atEndDo = atEndDo;
        this._manualProfileActivation = manualProfileActivation;
        this._fkProfileStartWhenActivated = fkProfileStartWhenActivated;
        this._delayEnd = delayEnd;
        this._isInDelayEnd = isInDelayEnd;
        this._startStatusTime = startStatusTime;
        this._pauseStatusTime = pauseStatusTime;

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
        this._notificationSound = event._notificationSound;
        this._forceRun = event._forceRun;
        this._blocked = event._blocked;
        //this._undoneProfile = event._undoneProfile;
        this._priority = event._priority;
        this._delayStart = event._delayStart;
        this._isInDelayStart = event._isInDelayStart;
        this._atEndDo = event._atEndDo;
        this._manualProfileActivation = event._manualProfileActivation;
        this._fkProfileStartWhenActivated = event._fkProfileStartWhenActivated;
        this._delayEnd = event._delayEnd;
        this._isInDelayEnd = event._isInDelayEnd;
        this._startStatusTime = event._startStatusTime;
        this._pauseStatusTime = event._pauseStatusTime;

        copyEventPreferences(event);
    }

    private void createEventPreferencesTime()
    {
        this._eventPreferencesTime = new EventPreferencesTime(this, false, false, false, false, false, false, false, false, 0, 0/*, false*/);
    }

    private void createEventPreferencesBattery()
    {
        this._eventPreferencesBattery = new EventPreferencesBattery(this, false, 0, 100, false, false);
    }

    private void createEventPreferencesCall()
    {
        this._eventPreferencesCall = new EventPreferencesCall(this, false, 0, "", "", 0);
    }

    private void createEventPreferencesPeripherals()
    {
        this._eventPreferencesPeripherals = new EventPreferencesPeripherals(this, false, 0);
    }

    private void createEventPreferencesCalendar()
    {
        this._eventPreferencesCalendar = new EventPreferencesCalendar(this, false, "", 0, "", 0, false, 0);
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
        this._eventPreferencesBluetooth = new EventPreferencesBluetooth(this, false, "", 0, 0);
    }

    private void createEventPreferencesSMS()
    {
        this._eventPreferencesSMS = new EventPreferencesSMS(this, false, "", "", 0, false, 5);
    }

    private void createEventPreferencesNotification()
    {
        this._eventPreferencesNotification = new EventPreferencesNotification(this, false, "", false, 5, false);
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
        this._eventPreferencesOrientation = new EventPreferencesOrientation(this, false, "", "", 0, "");
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
    }

    public boolean isRunnable(Context context)
    {
        boolean runnable = (this._fkProfileStart != 0);
        if (!(this._eventPreferencesTime._enabled ||
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
              this._eventPreferencesRadioSwitch._enabled))
            runnable = false;
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
        return runnable;
    }

    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putLong(PREF_EVENT_ID, this._id);
        editor.putString(PREF_EVENT_NAME, this._name);
        editor.putString(PREF_EVENT_PROFILE_START, Long.toString(this._fkProfileStart));
        editor.putString(PREF_EVENT_PROFILE_END, Long.toString(this._fkProfileEnd));
        editor.putBoolean(PREF_EVENT_ENABLED, this._status != ESTATUS_STOP);
        editor.putString(PREF_EVENT_NOTIFICATION_SOUND, this._notificationSound);
        editor.putBoolean(PREF_EVENT_FORCE_RUN, this._forceRun);
        //editor.putBoolean(PREF_EVENT_UNDONE_PROFILE, this._undoneProfile);
        editor.putString(PREF_EVENT_PRIORITY, Integer.toString(this._priority));
        editor.putString(PREF_EVENT_DELAY_START, Integer.toString(this._delayStart));
        editor.putString(PREF_EVENT_AT_END_DO, Integer.toString(this._atEndDo));
        editor.putBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION, this._manualProfileActivation);
        editor.putString(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, Long.toString(this._fkProfileStartWhenActivated));
        editor.putString(PREF_EVENT_DELAY_END, Integer.toString(this._delayEnd));
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
        editor.commit();
    }

    public void saveSharedPreferences(SharedPreferences preferences, Context context)
    {
        this._name = preferences.getString(PREF_EVENT_NAME, "");
        this._fkProfileStart = Long.parseLong(preferences.getString(PREF_EVENT_PROFILE_START, "0"));
        this._fkProfileEnd = Long.parseLong(preferences.getString(PREF_EVENT_PROFILE_END, Long.toString(PPApplication.PROFILE_NO_ACTIVATE)));
        this._status = (preferences.getBoolean(PREF_EVENT_ENABLED, false)) ? ESTATUS_PAUSE : ESTATUS_STOP;
        this._notificationSound = preferences.getString(PREF_EVENT_NOTIFICATION_SOUND, "");
        this._forceRun = preferences.getBoolean(PREF_EVENT_FORCE_RUN, false);
        //this._undoneProfile = preferences.getBoolean(PREF_EVENT_UNDONE_PROFILE, true);
        this._priority = Integer.parseInt(preferences.getString(PREF_EVENT_PRIORITY, Integer.toString(EPRIORITY_MEDIUM)));
        this._atEndDo = Integer.parseInt(preferences.getString(PREF_EVENT_AT_END_DO, Integer.toString(EATENDDO_UNDONE_PROFILE)));
        this._manualProfileActivation = preferences.getBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION, false);
        this._fkProfileStartWhenActivated = Long.parseLong(preferences.getString(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, Long.toString(PPApplication.PROFILE_NO_ACTIVATE)));

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

        if (!this.isRunnable(context))
            this._status = ESTATUS_STOP;
    }

    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            preference.setSummary(value);
            GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
        }
        if (key.equals(PREF_EVENT_PROFILE_START)||key.equals(PREF_EVENT_PROFILE_END)||
                key.equals(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE))
        {
            ProfilePreference preference = (ProfilePreference)prefMng.findPreference(key);
            long lProfileId;
            try {
                lProfileId = Long.parseLong(value);
            } catch (Exception e) {
                lProfileId = 0;
            }
            preference.setSummary(lProfileId);
            if (key.equals(PREF_EVENT_PROFILE_START))
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
            if (key.equals(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE)) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, lProfileId != PPApplication.PROFILE_NO_ACTIVATE, false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_SOUND))
        {
            Preference preference = prefMng.findPreference(key);
            if (value.isEmpty()) {
                preference.setSummary(R.string.preferences_notificationSound_None);
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, false, false, false);
            }
            else
            {
                Uri uri = Uri.parse(value);
                Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                String ringtoneName;
                if (ringtone == null)
                    ringtoneName = "";
                else
                    ringtoneName = ringtone.getTitle(context);
                preference.setSummary(ringtoneName);
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, true, false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_PRIORITY))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (PPApplication.applicationEventUsePriority) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
            else {
                listPreference.setSummary(R.string.event_preferences_priority_notUse);
            }
            listPreference.setEnabled(PPApplication.applicationEventUsePriority);
        }
        if (key.equals(PREF_EVENT_AT_END_DO))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(value);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
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
            GlobalGUIRoutines.setPreferenceTitleStyle(preference, delay > 0, false, false, false);
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
            GlobalGUIRoutines.setPreferenceTitleStyle(preference, delay > 0, false, false, false);
        }
        if (key.equals(PREF_EVENT_FORCE_RUN) ||
                key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION)) {
            Preference preference = prefMng.findPreference(key);
            GlobalGUIRoutines.setPreferenceTitleStyle(preference, value.equals("true"), false, false, false);
        }

    }

    private void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (key.isEmpty() ||
                //key.equals(PREF_EVENT_FORCE_RUN) ||
                key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION) ||
                key.equals(PREF_EVENT_NOTIFICATION_SOUND) ||
                key.equals(PREF_EVENT_DELAY_START) ||
                key.equals(PREF_EVENT_DELAY_END) ||
                key.equals(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE)) {
            //boolean forceRunChanged = false;
            boolean manualProfileActivationChanged;
            boolean profileStartWhenActivatedChanged;
            boolean delayStartChanged;
            boolean delayEndChanged;
            boolean notificationSoundChanged;

            long fkProfileStartWhenActivated;
            int delayStart;
            int delayEnd;

            if (preferences == null) {
                //forceRunChanged = this._forceRun;
                manualProfileActivationChanged = this._manualProfileActivation;
                profileStartWhenActivatedChanged = this._fkProfileStartWhenActivated != PPApplication.PROFILE_NO_ACTIVATE;
                fkProfileStartWhenActivated = this._fkProfileStartWhenActivated;
                delayStartChanged = this._delayStart != 0;
                delayEndChanged = this._delayEnd != 0;
                delayStart = this._delayStart;
                delayEnd = this._delayEnd;
                notificationSoundChanged = !this._notificationSound.isEmpty();
            }
            else {
                //forceRunChanged = preferences.getBoolean(PREF_EVENT_FORCE_RUN, false);
                manualProfileActivationChanged = preferences.getBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION, false);
                fkProfileStartWhenActivated = Long.parseLong(preferences.getString(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, String.valueOf(PPApplication.PROFILE_NO_ACTIVATE)));
                profileStartWhenActivatedChanged = fkProfileStartWhenActivated != PPApplication.PROFILE_NO_ACTIVATE;
                delayStartChanged = !preferences.getString(PREF_EVENT_DELAY_START, "0").equals("0");
                delayEndChanged = !preferences.getString(PREF_EVENT_DELAY_END, "0").equals("0");
                delayStart = Integer.parseInt(preferences.getString(PREF_EVENT_DELAY_START, "0"));
                delayEnd = Integer.parseInt(preferences.getString(PREF_EVENT_DELAY_END, "0"));
                notificationSoundChanged = !preferences.getString(PREF_EVENT_NOTIFICATION_SOUND, "").isEmpty();
            }
            Preference preference = prefMng.findPreference("eventStartOthersCategory");
            if (preference != null) {
                boolean bold = (//forceRunChanged ||
                                manualProfileActivationChanged ||
                                profileStartWhenActivatedChanged ||
                                delayStartChanged ||
                                notificationSoundChanged);
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, bold, false, false, false);
                if (bold) {
                    String summary = "";
                    //if (forceRunChanged)
                    //    summary = summary + "[»] " + context.getString(R.string.event_preferences_ForceRun);
                    if (manualProfileActivationChanged) {
                        if (!summary.isEmpty()) summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_manualProfileActivation);
                    }
                    if (profileStartWhenActivatedChanged) {
                        if (!summary.isEmpty()) summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_eventStartWhenActivatedProfile);
                        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, false, 0);
                        Profile profile = dataWrapper.getProfileById(fkProfileStartWhenActivated, false);
                        if (profile != null)
                            summary = summary + ": " + profile._name;
                    }
                    if (delayStartChanged) {
                        if (!summary.isEmpty()) summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_delayStart) + ": ";
                        summary = summary + GlobalGUIRoutines.getDurationString(delayStart);
                    }
                    if (notificationSoundChanged) {
                        if (!summary.isEmpty()) summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_notificationSound);
                    }
                    preference.setSummary(summary);
                }
                else
                    preference.setSummary("");
            }
            preference = prefMng.findPreference("eventEndOthersCategory");
            if (preference != null) {
                //boolean bold = (delayEndChanged);
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, delayEndChanged, false, false, false);
                if (delayEndChanged) {
                    String summary = "";
                    //if (delayEndChanged) {
                        if (!summary.isEmpty()) summary = summary + " • ";
                        summary = summary + context.getString(R.string.event_preferences_delayStart) + ": ";
                        summary = summary + GlobalGUIRoutines.getDurationString(delayEnd);
                    //}
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
            key.equals(PREF_EVENT_NOTIFICATION_SOUND) ||
            key.equals(PREF_EVENT_PRIORITY) ||
            key.equals(PREF_EVENT_DELAY_START) ||
            key.equals(PREF_EVENT_DELAY_END) ||
            key.equals(PREF_EVENT_AT_END_DO) ||
            key.equals(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE))
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        if (key.equals(PREF_EVENT_FORCE_RUN) ||
            key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, Boolean.toString(value), context);
        }
        setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesTime.setSummary(prefMng, key, preferences, context);
        _eventPreferencesTime.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesBattery.setSummary(prefMng, key, preferences, context);
        _eventPreferencesBattery.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesCall.setSummary(prefMng, key, preferences, context);
        _eventPreferencesCall.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesPeripherals.setSummary(prefMng, key, preferences, context);
        _eventPreferencesPeripherals.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesCalendar.setSummary(prefMng, key, preferences, context);
        _eventPreferencesCalendar.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesWifi.setSummary(prefMng, key, preferences, context);
        _eventPreferencesWifi.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesScreen.setSummary(prefMng, key, preferences, context);
        _eventPreferencesScreen.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesBluetooth.setSummary(prefMng, key, preferences, context);
        _eventPreferencesBluetooth.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesSMS.setSummary(prefMng, key, preferences, context);
        _eventPreferencesSMS.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesNotification.setSummary(prefMng, key, preferences, context);
        _eventPreferencesNotification.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesApplication.setSummary(prefMng, key, preferences, context);
        _eventPreferencesApplication.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesLocation.setSummary(prefMng, key, preferences, context);
        _eventPreferencesLocation.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesOrientation.setSummary(prefMng, key, preferences, context);
        _eventPreferencesOrientation.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesMobileCells.setSummary(prefMng, key, preferences, context);
        _eventPreferencesMobileCells.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesNFC.setSummary(prefMng, key, preferences, context);
        _eventPreferencesNFC.setCategorySummary(prefMng, key, preferences, context);
        _eventPreferencesRadioSwitch.setSummary(prefMng, key, preferences, context);
        _eventPreferencesRadioSwitch.setCategorySummary(prefMng, key, preferences, context);
    }

    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context) {

        Preference preference = prefMng.findPreference(PREF_EVENT_FORCE_RUN);
        preference.setTitle("[»] " + context.getString(R.string.event_preferences_ForceRun));


        setSummary(prefMng, PREF_EVENT_NAME, preferences, context);
        setSummary(prefMng, PREF_EVENT_PROFILE_START, preferences, context);
        setSummary(prefMng, PREF_EVENT_PROFILE_END, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_SOUND, preferences, context);
        setSummary(prefMng, PREF_EVENT_PRIORITY, preferences, context);
        setSummary(prefMng, PREF_EVENT_DELAY_START, preferences, context);
        setSummary(prefMng, PREF_EVENT_DELAY_END, preferences, context);
        setSummary(prefMng, PREF_EVENT_AT_END_DO, preferences, context);
        setSummary(prefMng, PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, preferences, context);
        setSummary(prefMng, PREF_EVENT_FORCE_RUN, preferences, context);
        setSummary(prefMng, PREF_EVENT_MANUAL_PROFILE_ACTIVATION, preferences, context);
        setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesTime.setAllSummary(prefMng, preferences, context);
        _eventPreferencesTime.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesBattery.setAllSummary(prefMng, preferences, context);
        _eventPreferencesBattery.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesCall.setAllSummary(prefMng, preferences, context);
        _eventPreferencesCall.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesPeripherals.setAllSummary(prefMng, preferences, context);
        _eventPreferencesPeripherals.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesCalendar.setAllSummary(prefMng, preferences, context);
        _eventPreferencesCalendar.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesWifi.setAllSummary(prefMng, preferences, context);
        _eventPreferencesWifi.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesScreen.setAllSummary(prefMng, preferences, context);
        _eventPreferencesScreen.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesBluetooth.setAllSummary(prefMng, preferences, context);
        _eventPreferencesBluetooth.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesSMS.setAllSummary(prefMng, preferences, context);
        _eventPreferencesSMS.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesNotification.setAllSummary(prefMng, preferences, context);
        _eventPreferencesNotification.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesApplication.setAllSummary(prefMng, preferences, context);
        _eventPreferencesApplication.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesLocation.setAllSummary(prefMng, preferences, context);
        _eventPreferencesLocation.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesOrientation.setAllSummary(prefMng, preferences, context);
        _eventPreferencesOrientation.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesMobileCells.setAllSummary(prefMng, preferences, context);
        _eventPreferencesMobileCells.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesNFC.setAllSummary(prefMng, preferences, context);
        _eventPreferencesNFC.setCategorySummary(prefMng, "", preferences, context);
        _eventPreferencesRadioSwitch.setAllSummary(prefMng, preferences, context);
        _eventPreferencesRadioSwitch.setCategorySummary(prefMng, "", preferences, context);
    }

    public String getPreferencesDescription(Context context)
    {
        String description;

        description = "";

        description = description + _eventPreferencesTime.getPreferencesDescription(true, context);

        if (_eventPreferencesCalendar._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesCalendar.getPreferencesDescription(true, context);

        if (_eventPreferencesBattery._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesBattery.getPreferencesDescription(true, context);

        if (_eventPreferencesCall._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesCall.getPreferencesDescription(true, context);

        if (_eventPreferencesSMS._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesSMS.getPreferencesDescription(true, context);

        if (_eventPreferencesRadioSwitch._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesRadioSwitch.getPreferencesDescription(true, context);

        if (_eventPreferencesLocation._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesLocation.getPreferencesDescription(true, context);

        if (_eventPreferencesWifi._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesWifi.getPreferencesDescription(true, context);

        if (_eventPreferencesBluetooth._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesBluetooth.getPreferencesDescription(true, context);

        if (_eventPreferencesMobileCells._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesMobileCells.getPreferencesDescription(true, context);

        if (_eventPreferencesPeripherals._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesPeripherals.getPreferencesDescription(true, context);

        if (_eventPreferencesScreen._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesScreen.getPreferencesDescription(true, context);

        if (_eventPreferencesNotification._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesNotification.getPreferencesDescription(true, context);

        if (_eventPreferencesApplication._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesApplication.getPreferencesDescription(true, context);

        if (_eventPreferencesOrientation._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesOrientation.getPreferencesDescription(true, context);

        if (_eventPreferencesNFC._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesNFC.getPreferencesDescription(true, context);

        //description = description.replace(' ', '\u00A0');

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
    }

    private boolean canActivateReturnProfile()
    {
        return true;
    }

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

    private EventTimeline addEventTimeline(DataWrapper dataWrapper,
                                            List<EventTimeline> eventTimelineList/*,
                                            Profile mergedProfile*/)
    {
        EventTimeline eventTimeline = new EventTimeline();
        eventTimeline._fkEvent = this._id;
        eventTimeline._eorder = 0;

        if (eventTimelineList.size() == 0)
        {
            Profile profile = dataWrapper.getActivatedProfile();
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

        dataWrapper.getDatabaseHandler().addEventTimeline(eventTimeline);
        eventTimelineList.add(eventTimeline);

        return eventTimeline;
    }

    void startEvent(DataWrapper dataWrapper,
                            List<EventTimeline> eventTimelineList,
                            boolean ignoreGlobalPref,
                            boolean interactive,
                            boolean reactivate,
                            boolean log,
                            Profile mergedProfile)
    {
        // remove delay alarm
        removeDelayStartAlarm(dataWrapper); // for start delay
        removeDelayEndAlarm(dataWrapper); // for end delay

        if ((!PPApplication.getGlobalEventsRuning(dataWrapper.context)) && (!ignoreGlobalPref))
            // events are globally stopped
            return;

        if (!this.isRunnable(dataWrapper.context))
            // event is not runnable, no pause it
            return;

        if (PPApplication.getEventsBlocked(dataWrapper.context))
        {
            // blocked by manual profile activation
            PPApplication.logE("Event.startEvent","event_id="+this._id+" events blocked");

            PPApplication.logE("Event.startEvent","event_id="+this._id+" forceRun="+_forceRun);
            PPApplication.logE("Event.startEvent","event_id="+this._id+" blocked="+_blocked);


            if (!_forceRun)
                // event is not forceRun
                return;
            if (_blocked)
                // forceRun event is temporary blocked
                return;
        }

        // check activated profile
        if (_fkProfileStartWhenActivated > 0) {
            Profile activatedProfile = dataWrapper.getActivatedProfile();
            if ((activatedProfile != null) && (activatedProfile._id != _fkProfileStartWhenActivated))
                // if activated profile is not _fkProfileStartWhenActivated,
                // no start event
                return;
        }

        // search for runing event with higher priority
        for (EventTimeline eventTimeline : eventTimelineList)
        {
            Event event = dataWrapper.getEventById(eventTimeline._fkEvent);
            if ((event != null) && PPApplication.applicationEventUsePriority && (event._priority > this._priority))
                // is running event with higher priority
                return;
        }

        if (_forceRun)
            PPApplication.setForceRunEventRunning(dataWrapper.context, true);

        PPApplication.logE("@@@ Event.startEvent","event_id="+this._id+"-----------------------------------");
        PPApplication.logE("@@@ Event.startEvent","-- event_name="+this._name);

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
            PPApplication.logE("Event.startEvent","eventPosition="+eventPosition);
            if (eventPosition != -1)
                eventTimeline = eventTimelineList.get(eventPosition);

            exists = eventPosition != -1;

            if (exists)
            {
                // remove event from timeline
                eventTimelineList.remove(eventTimeline);
                dataWrapper.getDatabaseHandler().deleteEventTimeline(eventTimeline);

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
        dataWrapper.getDatabaseHandler().updateEventStatus(this);

        if (log && (status != this._status)) {
            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_EVENTSTART, _name, null, null, 0);
        }

        long activatedProfileId = 0;
        Profile activatedProfile = dataWrapper.getActivatedProfile();
        if (activatedProfile != null)
            activatedProfileId = activatedProfile._id;

        if ((this._fkProfileStart != activatedProfileId) || this._manualProfileActivation || reactivate)
        {
            // no activate profile, when is already activated
            PPApplication.logE("Event.startEvent","event_id="+this._id+" activate profile id="+this._fkProfileStart);

            if (mergedProfile == null)
                dataWrapper.activateProfileFromEvent(this._fkProfileStart, interactive, false, false);
            else {
                mergedProfile.mergeProfiles(this._fkProfileStart, dataWrapper);
                if (this._manualProfileActivation) {
                    dataWrapper.getDatabaseHandler().saveMergedProfile(mergedProfile);
                    dataWrapper.activateProfileFromEvent(mergedProfile._id, interactive, true, true);
                    mergedProfile._id = 0;
                }
            }
        }
        else
        {
            dataWrapper.updateNotificationAndWidgets(activatedProfile);
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
                                        boolean allowRestart)
    {

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
        Profile activatedProfile = dataWrapper.getActivatedProfile();
        // activate profile only when profile not already activated
        if (activateReturnProfile && canActivateReturnProfile())
        {
            long activatedProfileId = 0;
            if (activatedProfile != null)
                activatedProfileId = activatedProfile._id;
            // first activate _fkProfileEnd
            if (_fkProfileEnd != PPApplication.PROFILE_NO_ACTIVATE)
            {
                if (_fkProfileEnd != activatedProfileId)
                {
                    PPApplication.logE("Event.pauseEvent","activate end porfile");
                    if (mergedProfile == null)
                        dataWrapper.activateProfileFromEvent(_fkProfileEnd, false, false, false);
                    else
                        mergedProfile.mergeProfiles(_fkProfileEnd, dataWrapper);
                    activatedProfileId = _fkProfileEnd;
                    profileActivated = true;
                }
            }
            // second activate when undone profile is set
            if (_atEndDo == EATENDDO_UNDONE_PROFILE)
            {
                // when in timeline list is event, get start profile from last event in tlimeline list
                // because last event in timeline list may be changed
                if (eventTimelineList.size() > 0) {
                    EventTimeline _eventTimeline = eventTimelineList.get(eventTimelineList.size() - 1);
                    if (_eventTimeline != null) {
                        Event event = dataWrapper.getEventById(_eventTimeline._fkEvent);
                        if (event != null)
                            eventTimeline._fkProfileEndActivated = event._fkProfileStart;
                    }
                }

                if (eventTimeline._fkProfileEndActivated != activatedProfileId)
                {
                    PPApplication.logE("Event.pauseEvent","undone profile");
                    PPApplication.logE("Event.pauseEvent","_fkProfileEndActivated="+eventTimeline._fkProfileEndActivated);
                    if (eventTimeline._fkProfileEndActivated != 0)
                    {
                        if (mergedProfile == null)
                            dataWrapper.activateProfileFromEvent(eventTimeline._fkProfileEndActivated, false, false, false);
                        else
                            mergedProfile.mergeProfiles(eventTimeline._fkProfileEndActivated, dataWrapper);
                        profileActivated = true;
                    }
                }
            }
            // restart events when is set
            if ((_atEndDo == EATENDDO_RESTART_EVENTS) && allowRestart) {
                PPApplication.logE("Event.pauseEvent","restart events");

                //EventsService.restartAtEndOfEvent = true;

                dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_RESTARTEVENTS, null, null, null, 0);

                PPApplication.logE("$$$ restartEvents", "from Event.doActivateEndProfile");
                dataWrapper.restartEventsWithDelay(5, true, false);
                profileActivated = true;
            }

        }

        if (!profileActivated)
        {
            dataWrapper.updateNotificationAndWidgets(activatedProfile);
        }

    }

    void pauseEvent(DataWrapper dataWrapper,
                            List<EventTimeline> eventTimelineList,
                            boolean activateReturnProfile,
                            boolean ignoreGlobalPref,
                            boolean noSetSystemEvent,
                            boolean log,
                            Profile mergedProfile,
                            boolean allowRestart)
    {
        // remove delay alarm
        removeDelayStartAlarm(dataWrapper); // for start delay
        removeDelayEndAlarm(dataWrapper); // for end delay

        if ((!PPApplication.getGlobalEventsRuning(dataWrapper.context)) && (!ignoreGlobalPref))
            // events are globally stopped
            return;

        if (!this.isRunnable(dataWrapper.context))
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

        PPApplication.logE("@@@ Event.pauseEvent","event_id="+this._id+"-----------------------------------");
        PPApplication.logE("@@@ Event.pauseEvent","-- event_name="+this._name);

        int timeLineSize = eventTimelineList.size();

        // test whenever event exists in timeline
        int eventPosition = getEventTimelinePosition(eventTimelineList);
        PPApplication.logE("Event.pauseEvent","eventPosition="+eventPosition);

        boolean exists = eventPosition != -1;

        EventTimeline eventTimeline = null;

        if (exists)
        {
            eventTimeline = eventTimelineList.get(eventPosition);

            // remove event from timeline
            eventTimelineList.remove(eventTimeline);
            dataWrapper.getDatabaseHandler().deleteEventTimeline(eventTimeline);

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
        this._status = ESTATUS_PAUSE;
        dataWrapper.getDatabaseHandler().updateEventStatus(this);

        if (log && (status != this._status)) {
            int alType = DatabaseHandler.ALTYPE_EVENTEND_NONE;
            if ((_atEndDo == EATENDDO_UNDONE_PROFILE) && (_fkProfileEnd != PPApplication.PROFILE_NO_ACTIVATE))
                alType = DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE_UNDOPROFILE;
            if ((_atEndDo == EATENDDO_RESTART_EVENTS) && (_fkProfileEnd != PPApplication.PROFILE_NO_ACTIVATE))
                alType = DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE_RESTARTEVENTS;
            else if (_atEndDo == EATENDDO_UNDONE_PROFILE)
                alType = DatabaseHandler.ALTYPE_EVENTEND_UNDOPROFILE;
            else if (_atEndDo == EATENDDO_RESTART_EVENTS)
                alType = DatabaseHandler.ALTYPE_EVENTEND_RESTARTEVENTS;
            else if (_fkProfileEnd != PPApplication.PROFILE_NO_ACTIVATE)
                alType = DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE;

            dataWrapper.addActivityLog(alType, _name, null, null, 0);
        }


        //if (_forceRun)
        //{ look for forcerun events always, not only when forcerun event is paused
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
                PPApplication.setForceRunEventRunning(dataWrapper.context, false);
        //}

        if (exists)
        {
            doActivateEndProfile(dataWrapper, eventPosition, timeLineSize,
                    eventTimelineList, eventTimeline,
                    activateReturnProfile, mergedProfile, allowRestart);

        }

        //return;
    }

    void stopEvent(DataWrapper dataWrapper,
                            List<EventTimeline> eventTimelineList,
                            boolean activateReturnProfile,
                            boolean ignoreGlobalPref,
                            boolean saveEventStatus,
                            boolean log,
                            boolean allowRestart)
    {
        // remove delay alarm
        removeDelayStartAlarm(dataWrapper); // for start delay
        removeDelayEndAlarm(dataWrapper); // for end delay

        if ((!PPApplication.getGlobalEventsRuning(dataWrapper.context)) && (!ignoreGlobalPref))
            // events are globally stopped
            return;

        PPApplication.logE("@@@ Event.stopEvent","event_id="+this._id+"-----------------------------------");
        PPApplication.logE("@@@ Event.stopEvent", "-- event_name=" + this._name);

        if (this._status != ESTATUS_STOP)
        {
            pauseEvent(dataWrapper, eventTimelineList, activateReturnProfile, ignoreGlobalPref, true, false, null, allowRestart);
        }

        setSystemEvent(dataWrapper.context, ESTATUS_STOP);
        int status = this._status;
        this._status = ESTATUS_STOP;
        if (saveEventStatus)
            dataWrapper.getDatabaseHandler().updateEventStatus(this);

        if (log && (status != this._status)) {
            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_EVENTSTOP, _name, null, null, 0);
        }

        //return;
    }

    public int getStatus()
    {
        return _status;
    }

    int getStatusFromDB(DataWrapper dataWrapper)
    {
        return dataWrapper.getDatabaseHandler().getEventStatus(this);
    }

    public void setStatus(int status)
    {
        _status = status;
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
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    void setDelayStartAlarm(DataWrapper dataWrapper)
    {
        removeDelayStartAlarm(dataWrapper);

        if (!PPApplication.getGlobalEventsRuning(dataWrapper.context))
            // events are globally stopped
            return;

        if (!this.isRunnable(dataWrapper.context))
            // event is not runnable, no pause it
            return;

        if (PPApplication.getEventsBlocked(dataWrapper.context))
        {
            // blocked by manual profile activation
            PPApplication.logE("Event.setDelayStartAlarm","event_id="+this._id+" events blocked");


            if (!_forceRun)
                // event is not forceRun
                return;
            if (_blocked)
                // forceRun event is temporary blocked
                return;
        }

        PPApplication.logE("@@@ Event.setDelayStartAlarm","event_id="+this._id+"-----------------------------------");
        PPApplication.logE("@@@ Event.setDelayStartAlarm","-- event_name="+this._name);
        PPApplication.logE("@@@ Event.setDelayStartAlarm","-- delay="+this._delayStart);

        if (this._delayStart > 0)
        {
            // delay for start is > 0
            // set alarm

            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, this._delayStart);
            long alarmTime = now.getTimeInMillis(); // + 1000 * /* 60 * */ this._delayStart;

            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            String result = sdf.format(alarmTime);
            PPApplication.logE("Event.setDelayStartAlarm","startTime="+result);

            Intent intent = new Intent(dataWrapper.context, EventDelayStartBroadcastReceiver.class);
            //intent.putExtra(PPApplication.EXTRA_EVENT_ID, this._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(dataWrapper.context.getApplicationContext(), (int) this._id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager alarmManager = (AlarmManager) dataWrapper.context.getSystemService(Activity.ALARM_SERVICE);

            if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
            if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);

            now = Calendar.getInstance();
            int gmtOffset = TimeZone.getDefault().getRawOffset();
            this._startStatusTime = now.getTimeInMillis() - gmtOffset;

            this._isInDelayStart = true;
        }
        else {
            this._startStatusTime = 0;
            this._isInDelayStart = false;
        }

        dataWrapper.getDatabaseHandler().updateEventInDelayStart(this);

        if (_isInDelayStart) {
            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_EVENTSTARTDELAY, _name, null, null, _delayStart);
        }

        //return;
    }

    void checkDelayStart(/*DataWrapper dataWrapper*/) {
        if (this._startStatusTime == 0) {
            this._isInDelayStart = false;
            return;
        }

        Calendar now = Calendar.getInstance();
        int gmtOffset = TimeZone.getDefault().getRawOffset();
        long nowTime = now.getTimeInMillis() - gmtOffset;
        now.add(Calendar.SECOND, this._delayStart);
        long delayTime = now.getTimeInMillis() - gmtOffset;

        if (nowTime > delayTime)
            this._isInDelayStart = false;
    }

    void removeDelayStartAlarm(DataWrapper dataWrapper)
    {
        AlarmManager alarmManager = (AlarmManager) dataWrapper.context.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(dataWrapper.context, EventDelayStartBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(dataWrapper.context.getApplicationContext(), (int) this._id, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            PPApplication.logE("Event.removeDelayStartAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        this._isInDelayStart = false;
        this._startStatusTime = 0;
        dataWrapper.getDatabaseHandler().updateEventInDelayStart(this);
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    void setDelayEndAlarm(DataWrapper dataWrapper)
    {
        removeDelayEndAlarm(dataWrapper);

        if (!PPApplication.getGlobalEventsRuning(dataWrapper.context))
            // events are globally stopped
            return;

        if (!this.isRunnable(dataWrapper.context))
            // event is not runnable, no pause it
            return;

        if (PPApplication.getEventsBlocked(dataWrapper.context))
        {
            // blocked by manual profile activation
            PPApplication.logE("Event.setDelayEndAlarm","event_id="+this._id+" events blocked");


            if (!_forceRun)
                // event is not forceRun
                return;
            if (_blocked)
                // forceRun event is temporary blocked
                return;
        }

        PPApplication.logE("@@@ Event.setDelayEndAlarm","event_id="+this._id+"-----------------------------------");
        PPApplication.logE("@@@ Event.setDelayEndAlarm","-- event_name="+this._name);
        PPApplication.logE("@@@ Event.setDelayEndAlarm","-- delay="+this._delayEnd);

        if (this._delayEnd > 0)
        {
            // delay for end is > 0
            // set alarm

            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, this._delayEnd);
            long alarmTime = now.getTimeInMillis(); // + 1000 * /* 60 * */ this._delayEnd;

            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            String result = sdf.format(alarmTime);
            PPApplication.logE("Event.setDelayEndAlarm","endTime="+result);

            Intent intent = new Intent(dataWrapper.context, EventDelayEndBroadcastReceiver.class);
            //intent.putExtra(PPApplication.EXTRA_EVENT_ID, this._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(dataWrapper.context.getApplicationContext(), (int) this._id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager alarmManager = (AlarmManager) dataWrapper.context.getSystemService(Activity.ALARM_SERVICE);

            if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
            if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);

            now = Calendar.getInstance();
            int gmtOffset = TimeZone.getDefault().getRawOffset();
            this._pauseStatusTime = now.getTimeInMillis() - gmtOffset;

            this._isInDelayEnd = true;
        }
        else {
            this._pauseStatusTime = 0;
            this._isInDelayEnd = false;
        }

        dataWrapper.getDatabaseHandler().updateEventInDelayEnd(this);

        if (_isInDelayEnd) {
            dataWrapper.addActivityLog(DatabaseHandler.ALTYPE_EVENTENDDELAY, _name, null, null, _delayEnd);
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
        int gmtOffset = TimeZone.getDefault().getRawOffset();
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
        AlarmManager alarmManager = (AlarmManager) dataWrapper.context.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(dataWrapper.context, EventDelayEndBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(dataWrapper.context.getApplicationContext(), (int) this._id, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            PPApplication.logE("Event.removeDelayEndAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        this._isInDelayEnd = false;
        dataWrapper.getDatabaseHandler().updateEventInDelayEnd(this);
    }

}

