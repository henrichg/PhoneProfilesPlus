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
import java.util.prefs.Preferences;

public class Event {

    public long _id;
    public String _name;
    public long _fkProfileStart;
    public long _fkProfileEnd;
    //public boolean _undoneProfile;
    public int _atEndDo;
    private int _status;
    public String _notificationSound;
    public boolean _forceRun;
    public boolean _blocked;
    public int _priority;
    public int _delayStart;
    public boolean _isInDelay;
    public boolean _manualProfileActivation;
    public long _fkProfileStartWhenActivated;

    public EventPreferencesTime _eventPreferencesTime;
    public EventPreferencesBattery _eventPreferencesBattery;
    public EventPreferencesCall _eventPreferencesCall;
    public EventPreferencesPeripherals _eventPreferencesPeripherals;
    public EventPreferencesCalendar _eventPreferencesCalendar;
    public EventPreferencesWifi _eventPreferencesWifi;
    public EventPreferencesScreen _eventPreferencesScreen;
    public EventPreferencesBluetooth _eventPreferencesBluetooth;
    public EventPreferencesSMS _eventPreferencesSMS;
    public EventPreferencesNotification _eventPreferencesNotification;

    public static final int ESTATUS_STOP = 0;
    public static final int ESTATUS_PAUSE = 1;
    public static final int ESTATUS_RUNNING = 2;
    public static final int ESTATUS_NONE = 99;

    public static final int EPRIORITY_LOWEST = -5;
    public static final int EPRIORITY_VERY_LOW = -4;
    public static final int EPRIORITY_LOWER = -3;
    public static final int EPRIORITY_LOW = -1;
    public static final int EPRIORITY_LOWER_MEDIUM = -1;
    public static final int EPRIORITY_MEDIUM = 0;
    public static final int EPRIORITY_UPPER_MEDIUM = 1;
    public static final int EPRIORITY_HIGH = 2;
    public static final int EPRIORITY_HIGHER = 3;
    public static final int EPRIORITY_VERY_HIGH = 4;
    public static final int EPRIORITY_HIGHEST = 5;

    public static final int EATENDDO_NONE = 0;
    public static final int EATENDDO_UNDONE_PROFILE = 1;
    public static final int EATENDDO_RESTART_EVENTS = 2;

    static final String PREF_EVENT_ENABLED = "eventEnabled";
    static final String PREF_EVENT_NAME = "eventName";
    static final String PREF_EVENT_PROFILE_START = "eventProfileStart";
    static final String PREF_EVENT_PROFILE_END = "eventProfileEnd";
    static final String PREF_EVENT_NOTIFICATION_SOUND = "eventNotificationSound";
    static final String PREF_EVENT_FORCE_RUN = "eventForceRun";
    //static final String PREF_EVENT_UNDONE_PROFILE = "eventUndoneProfile";
    static final String PREF_EVENT_PRIORITY = "eventPriority";
    static final String PREF_EVENT_DELAY_START = "eventDelayStart";
    static final String PREF_EVENT_AT_END_DO = "eventAtEndDo";
    static final String PREF_EVENT_MANUAL_PROFILE_ACTIVATION = "manualProfileActivation";
    static final String PREF_EVENT_START_WHEN_ACTIVATED_PROFILE = "eventStartWhenActivatedProfile";

    // Empty constructor
    public Event(){

    }

    // constructor
    public Event(long id,
                 String name,
                 long fkProfileStart,
                 long fkProfileEnd,
                 int status,
                 String notificationSound,
                 boolean forceRun,
                 boolean blocked,
                 //boolean undoneProfile,
                 int priority,
                 int delayStart,
                 boolean isInDelay,
                 int atEndDo,
                 boolean manualProfileActivation,
                 long fkProfileStartWhenActivated)
    {
        this._id = id;
        this._name = name;
        this._fkProfileStart = fkProfileStart;
        this._fkProfileEnd = fkProfileEnd;
        this._status = status;
        this._notificationSound = notificationSound;
        this._forceRun = forceRun;
        this._blocked = blocked;
        //this._undoneProfile = undoneProfile;
        this._priority = priority;
        this._delayStart = delayStart;
        this._isInDelay = isInDelay;
        this._atEndDo = atEndDo;
        this._manualProfileActivation = manualProfileActivation;
        this._fkProfileStartWhenActivated = fkProfileStartWhenActivated;
        
        createEventPreferences();

        //Log.e("Event", "this._fkProfileEnd=" + this._fkProfileEnd);
    }

    // constructor
    public Event(String name,
                 long fkProfileStart,
                 long fkProfileEnd,
                 int status,
                 String notificationSound,
                 boolean forceRun,
                 boolean blocked,
                 //boolean undoneProfile,
                 int priority,
                 int delayStart,
                 boolean isInDelay,
                 int atEndDo,
                 boolean manualProfileActivation,
                 long fkProfileStartWhenActivated)
    {
        this._name = name;
        this._fkProfileStart = fkProfileStart;
        this._fkProfileEnd = fkProfileEnd;
        this._status = status;
        this._notificationSound = notificationSound;
        this._forceRun = forceRun;
        this._blocked = blocked;
        //this._undoneProfile = undoneProfile;
        this._priority = priority;
        this._delayStart = delayStart;
        this._isInDelay = isInDelay;
        this._atEndDo = atEndDo;
        this._manualProfileActivation = manualProfileActivation;
        this._fkProfileStartWhenActivated = fkProfileStartWhenActivated;

        createEventPreferences();
    }

    public void copyEvent(Event event)
    {
        this._id = event._id;
        this._name = event._name;
        this._fkProfileStart = event._fkProfileStart;
        this._fkProfileEnd = event._fkProfileEnd;
        this._status = event._status;
        this._notificationSound = event._notificationSound;
        this._forceRun = event._forceRun;
        this._blocked = event._blocked;
        //this._undoneProfile = event._undoneProfile;
        this._priority = event._priority;
        this._delayStart = event._delayStart;
        this._isInDelay = event._isInDelay;
        this._atEndDo = event._atEndDo;
        this._manualProfileActivation = event._manualProfileActivation;
        this._fkProfileStartWhenActivated = event._fkProfileStartWhenActivated;
        
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
        this._eventPreferencesCalendar = new EventPreferencesCalendar(this, false, "", 0, "", 0);
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
        this._eventPreferencesSMS = new EventPreferencesSMS(this, false, "", "", 0, 5);
    }

    private void createEventPreferencesNotification()
    {
        this._eventPreferencesNotification = new EventPreferencesNotification(this, false, "", 5);
    }

    public void createEventPreferences()
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
    }

    public void copyEventPreferences(Event fromEvent)
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
    }

    public boolean isRunnable()
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
              this._eventPreferencesNotification._enabled))
            runnable = false;
        if (this._eventPreferencesTime._enabled)
            runnable = runnable && this._eventPreferencesTime.isRunable();
        if (this._eventPreferencesBattery._enabled)
            runnable = runnable && this._eventPreferencesBattery.isRunable();
        if (this._eventPreferencesCall._enabled)
            runnable = runnable && this._eventPreferencesCall.isRunable();
        if (this._eventPreferencesPeripherals._enabled)
            runnable = runnable && this._eventPreferencesPeripherals.isRunable();
        if (this._eventPreferencesCalendar._enabled)
            runnable = runnable && this._eventPreferencesCalendar.isRunable();
        if (this._eventPreferencesWifi._enabled)
            runnable = runnable && this._eventPreferencesWifi.isRunable();
        if (this._eventPreferencesScreen._enabled)
            runnable = runnable && this._eventPreferencesScreen.isRunable();
        if (this._eventPreferencesBluetooth._enabled)
            runnable = runnable && this._eventPreferencesBluetooth.isRunable();
        if (this._eventPreferencesSMS._enabled)
            runnable = runnable && this._eventPreferencesSMS.isRunable();
        if (this._eventPreferencesNotification._enabled)
            runnable = runnable && this._eventPreferencesNotification.isRunable();
        return runnable;
    }

    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
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
        editor.commit();
    }

    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._name = preferences.getString(PREF_EVENT_NAME, "");
        this._fkProfileStart = Long.parseLong(preferences.getString(PREF_EVENT_PROFILE_START, "0"));
        this._fkProfileEnd = Long.parseLong(preferences.getString(PREF_EVENT_PROFILE_END, Long.toString(GlobalData.PROFILE_NO_ACTIVATE)));
        this._status = (preferences.getBoolean(PREF_EVENT_ENABLED, false)) ? ESTATUS_PAUSE : ESTATUS_STOP;
        this._notificationSound = preferences.getString(PREF_EVENT_NOTIFICATION_SOUND, "");
        this._forceRun = preferences.getBoolean(PREF_EVENT_FORCE_RUN, false);
        //this._undoneProfile = preferences.getBoolean(PREF_EVENT_UNDONE_PROFILE, true);
        this._priority = Integer.parseInt(preferences.getString(PREF_EVENT_PRIORITY, Integer.toString(EPRIORITY_MEDIUM)));
        this._atEndDo = Integer.parseInt(preferences.getString(PREF_EVENT_AT_END_DO, Integer.toString(EATENDDO_UNDONE_PROFILE)));
        this._manualProfileActivation = preferences.getBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION, false);
        this._fkProfileStartWhenActivated = Long.parseLong(preferences.getString(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, Long.toString(GlobalData.PROFILE_NO_ACTIVATE)));

        String sDelayStart = preferences.getString(PREF_EVENT_DELAY_START, "0");
        if (sDelayStart.isEmpty()) sDelayStart = "0";
        int iDelayStart = Integer.parseInt(sDelayStart);
        if (iDelayStart < 0) iDelayStart = 0;
        this._delayStart = iDelayStart;


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

        if (!this.isRunnable())
            this._status = ESTATUS_STOP;
    }

    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            preference.setSummary(value);
            GUIData.setPreferenceTitleStyle(preference, false, true);
        }
        if (key.equals(PREF_EVENT_PROFILE_START)||key.equals(PREF_EVENT_PROFILE_END)||
                key.equals(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE))
        {
            ProfilePreference preference = (ProfilePreference)prefMng.findPreference(key);
            String sProfileId = value;
            long lProfileId;
            try {
                lProfileId = Long.parseLong(sProfileId);
            } catch (Exception e) {
                lProfileId = 0;
            }
            preference.setSummary(lProfileId);
            if (key.equals(PREF_EVENT_PROFILE_START))
                GUIData.setPreferenceTitleStyle(preference, false, true);
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_SOUND))
        {
            String ringtoneUri = value.toString();
            if (ringtoneUri.isEmpty())
                prefMng.findPreference(key).setSummary(R.string.preferences_notificationSound_None);
            else
            {
                Uri uri = Uri.parse(ringtoneUri);
                Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                String ringtoneName;
                if (ringtone == null)
                    ringtoneName = "";
                else
                    ringtoneName = ringtone.getTitle(context);
                prefMng.findPreference(key).setSummary(ringtoneName);
            }
        }
        if (key.equals(PREF_EVENT_PRIORITY))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(value);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
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
            prefMng.findPreference(key).setSummary(value);
        }

    }

    private void setBoldOthersParametersCategory(PreferenceManager prefMng, String key, SharedPreferences preferences) {
        if (key.isEmpty() ||
                key.equals(PREF_EVENT_FORCE_RUN) ||
                key.equals(PREF_EVENT_MANUAL_PROFILE_ACTIVATION) ||
                key.equals(PREF_EVENT_NOTIFICATION_SOUND) ||
                key.equals(PREF_EVENT_DELAY_START) ||
                key.equals(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE)) {
            boolean forceRunChanged = false;
            boolean manualProfileActivationChanged = false;
            boolean profileStartWhenActivatedChanged = false;
            boolean delayStarChanged = false;
            boolean notificationSoundChanged = false;

            if (preferences == null) {
                forceRunChanged = this._forceRun;
                manualProfileActivationChanged = this._manualProfileActivation;
                profileStartWhenActivatedChanged = this._fkProfileStartWhenActivated != GlobalData.PROFILE_NO_ACTIVATE;
                delayStarChanged = this._delayStart != 0;
                notificationSoundChanged = !this._notificationSound.isEmpty();
            }
            else {
                forceRunChanged = preferences.getBoolean(PREF_EVENT_FORCE_RUN, false);
                manualProfileActivationChanged = preferences.getBoolean(PREF_EVENT_MANUAL_PROFILE_ACTIVATION, false);
                profileStartWhenActivatedChanged = !preferences.getString(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, String.valueOf(GlobalData.PROFILE_NO_ACTIVATE)).equals(String.valueOf(GlobalData.PROFILE_NO_ACTIVATE));
                delayStarChanged = !preferences.getString(PREF_EVENT_DELAY_START, "0").equals("0");
                notificationSoundChanged = !preferences.getString(PREF_EVENT_NOTIFICATION_SOUND, "").isEmpty();
            }
            boolean bold = (forceRunChanged ||
                            manualProfileActivationChanged ||
                            profileStartWhenActivatedChanged ||
                            delayStarChanged ||
                            notificationSoundChanged);
            Preference preference = prefMng.findPreference("eventStartOthersCategory");
            if (preference != null)
                GUIData.setPreferenceTitleStyle(preference, bold, false);
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
            key.equals(PREF_EVENT_AT_END_DO) ||
            key.equals(PREF_EVENT_START_WHEN_ACTIVATED_PROFILE))
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        setBoldOthersParametersCategory(prefMng, key, preferences);
        _eventPreferencesTime.setSummary(prefMng, key, preferences, context);
        _eventPreferencesTime.setBoldParametersCategory(prefMng, key, preferences);
        _eventPreferencesBattery.setSummary(prefMng, key, preferences, context);
        _eventPreferencesBattery.setBoldParametersCategory(prefMng, key, preferences);
        _eventPreferencesCall.setSummary(prefMng, key, preferences, context);
        _eventPreferencesCall.setBoldParametersCategory(prefMng, key, preferences);
        _eventPreferencesPeripherals.setSummary(prefMng, key, preferences, context);
        _eventPreferencesPeripherals.setBoldParametersCategory(prefMng, key, preferences);
        _eventPreferencesCalendar.setSummary(prefMng, key, preferences, context);
        _eventPreferencesCalendar.setBoldParametersCategory(prefMng, key, preferences);
        _eventPreferencesWifi.setSummary(prefMng, key, preferences, context);
        _eventPreferencesWifi.setBoldParametersCategory(prefMng, key, preferences);
        _eventPreferencesScreen.setSummary(prefMng, key, preferences, context);
        _eventPreferencesScreen.setBoldParametersCategory(prefMng, key, preferences);
        _eventPreferencesBluetooth.setSummary(prefMng, key, preferences, context);
        _eventPreferencesBluetooth.setBoldParametersCategory(prefMng, key, preferences);
        _eventPreferencesSMS.setSummary(prefMng, key, preferences, context);
        _eventPreferencesSMS.setBoldParametersCategory(prefMng, key, preferences);
        _eventPreferencesNotification.setSummary(prefMng, key, preferences, context);
        _eventPreferencesNotification.setBoldParametersCategory(prefMng, key, preferences);
    }

    public void setAllSummary(PreferenceManager prefMng, Context context) {

        Preference preference = prefMng.findPreference(PREF_EVENT_FORCE_RUN);
        preference.setTitle("[»] " + preference.getTitle());


        setSummary(prefMng, PREF_EVENT_NAME, _name, context);
        setSummary(prefMng, PREF_EVENT_PROFILE_START, Long.toString(this._fkProfileStart), context);
        setSummary(prefMng, PREF_EVENT_PROFILE_END, Long.toString(this._fkProfileEnd), context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_SOUND, this._notificationSound, context);
        setSummary(prefMng, PREF_EVENT_PRIORITY, Integer.toString(this._priority), context);
        setSummary(prefMng, PREF_EVENT_DELAY_START, Integer.toString(this._delayStart), context);
        setSummary(prefMng, PREF_EVENT_AT_END_DO, Integer.toString(this._atEndDo), context);
        setSummary(prefMng, PREF_EVENT_START_WHEN_ACTIVATED_PROFILE, Long.toString(this._fkProfileStartWhenActivated), context);
        setBoldOthersParametersCategory(prefMng, "", null);
        _eventPreferencesTime.setAllSummary(prefMng, context);
        _eventPreferencesTime.setBoldParametersCategory(prefMng, "", null);
        _eventPreferencesBattery.setAllSummary(prefMng, context);
        _eventPreferencesBattery.setBoldParametersCategory(prefMng, "", null);
        _eventPreferencesCall.setAllSummary(prefMng, context);
        _eventPreferencesCall.setBoldParametersCategory(prefMng, "", null);
        _eventPreferencesPeripherals.setAllSummary(prefMng, context);
        _eventPreferencesPeripherals.setBoldParametersCategory(prefMng, "", null);
        _eventPreferencesCalendar.setAllSummary(prefMng, context);
        _eventPreferencesCalendar.setBoldParametersCategory(prefMng, "", null);
        _eventPreferencesWifi.setAllSummary(prefMng, context);
        _eventPreferencesWifi.setBoldParametersCategory(prefMng, "", null);
        _eventPreferencesScreen.setAllSummary(prefMng, context);
        _eventPreferencesScreen.setBoldParametersCategory(prefMng, "", null);
        _eventPreferencesBluetooth.setAllSummary(prefMng, context);
        _eventPreferencesBluetooth.setBoldParametersCategory(prefMng, "", null);
        _eventPreferencesSMS.setAllSummary(prefMng, context);
        _eventPreferencesSMS.setBoldParametersCategory(prefMng, "", null);
        _eventPreferencesNotification.setAllSummary(prefMng, context);
        _eventPreferencesNotification.setBoldParametersCategory(prefMng, "", null);
    }

    public String getPreferencesDescription(Context context)
    {
        String description;

        description = "";

        description = description + _eventPreferencesTime.getPreferencesDescription(context);

        if (_eventPreferencesCalendar._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesCalendar.getPreferencesDescription(context);

        if (_eventPreferencesBattery._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesBattery.getPreferencesDescription(context);

        if (_eventPreferencesCall._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesCall.getPreferencesDescription(context);

        if (_eventPreferencesSMS._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesSMS.getPreferencesDescription(context);

        if (_eventPreferencesWifi._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesWifi.getPreferencesDescription(context);

        if (_eventPreferencesBluetooth._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesBluetooth.getPreferencesDescription(context);

        if (_eventPreferencesPeripherals._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesPeripherals.getPreferencesDescription(context);

        if (_eventPreferencesScreen._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesScreen.getPreferencesDescription(context);

        if (_eventPreferencesNotification._enabled && (!description.isEmpty())) description = description + "<br>"; //"\n";
        description = description + _eventPreferencesNotification.getPreferencesDescription(context);

        //description = description.replace(' ', '\u00A0');

        return description;
    }

    private boolean canActivateReturnProfile()
    {
        /*
        boolean canActivate = false;

        if (this._eventPreferencesTime._enabled)
            canActivate = canActivate || this._eventPreferencesTime.activateReturnProfile();
        if (this._eventPreferencesBattery._enabled)
            canActivate = canActivate || this._eventPreferencesBattery.activateReturnProfile();
        if (this._eventPreferencesCall._enabled)
            canActivate = canActivate || this._eventPreferencesCall.activateReturnProfile();
        if (this._eventPreferencesPeripherals._enabled)
            canActivate = canActivate || this._eventPreferencesPeripherals.activateReturnProfile();
        if (this._eventPreferencesCalendar._enabled)
            canActivate = canActivate || this._eventPreferencesCalendar.activateReturnProfile();
        if (this._eventPreferencesWifi._enabled)
            canActivate = canActivate || this._eventPreferencesWifi.activateReturnProfile();
        if (this._eventPreferencesScreen._enabled)
            canActivate = canActivate || this._eventPreferencesScreen.activateReturnProfile();
        if (this._eventPreferencesBluetooth._enabled)
            canActivate = canActivate || this._eventPreferencesBluetooth.activateReturnProfile();
        if (this._eventPreferencesSMS._enabled)
            canActivate = canActivate || this._eventPreferencesSMS.activateReturnProfile();
        if (this._eventPreferencesNotification._enabled)
            canActivate = canActivate || this._eventPreferencesNotification.activateReturnProfile();

        return canActivate;
        */
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
                                            List<EventTimeline> eventTimelineList,
                                            Profile mergedProfile)
    {
        EventTimeline eventTimeline = new EventTimeline();
        eventTimeline._fkEvent = this._id;
        eventTimeline._eorder = 0;

        Profile profile = null;
        if (eventTimelineList.size() == 0)
        {
            profile = dataWrapper.getActivatedProfile();
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

    public void startEvent(DataWrapper dataWrapper,
                            List<EventTimeline> eventTimelineList,
                            boolean ignoreGlobalPref,
                            boolean interactive,
                            boolean reactivate,
                            boolean log,
                            Profile mergedProfile)
    {
        // remove delay alarm
        removeDelayAlarm(dataWrapper, true); // for start delay

        if ((!GlobalData.getGlobalEventsRuning(dataWrapper.context)) && (!ignoreGlobalPref))
            // events are globally stopped
            return;

        if (!this.isRunnable())
            // event is not runnable, no pause it
            return;

        if (GlobalData.getEventsBlocked(dataWrapper.context))
        {
            // blocked by manual profile activation
            GlobalData.logE("Event.startEvent","event_id="+this._id+" events blocked");

            GlobalData.logE("Event.startEvent","event_id="+this._id+" forceRun="+_forceRun);
            GlobalData.logE("Event.startEvent","event_id="+this._id+" blocked="+_blocked);


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
            if ((event != null) && (event._priority > this._priority))
                // is running event with higher priority
                return;
        }

        if (_forceRun)
            GlobalData.setForceRunEventRunning(dataWrapper.context, true);

        GlobalData.logE("@@@ Event.startEvent","event_id="+this._id+"-----------------------------------");
        GlobalData.logE("@@@ Event.startEvent","-- event_name="+this._name);

        EventTimeline eventTimeline;

    /////// delete duplicate from timeline
        boolean exists = true;
        while (exists)
        {
            exists = false;

            int timeLineSize = eventTimelineList.size();

            // test whenever event exists in timeline
            eventTimeline = null;
            int eventPosition = getEventTimelinePosition(eventTimelineList);
            GlobalData.logE("Event.startEvent","eventPosition="+eventPosition);
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

        addEventTimeline(dataWrapper, eventTimelineList, mergedProfile);


        setSystemEvent(dataWrapper.context, ESTATUS_RUNNING);
        int status = this._status;
        this._status = ESTATUS_RUNNING;
        dataWrapper.getDatabaseHandler().updateEventStatus(this);

        if (log && (status != this._status)) {
            dataWrapper.getDatabaseHandler().addActivityLog(DatabaseHandler.ALTYPE_EVENTSTART, _name, null, null, 0);
        }

        long activatedProfileId = 0;
        Profile activatedProfile = dataWrapper.getActivatedProfile();
        if (activatedProfile != null)
            activatedProfileId = activatedProfile._id;

        if ((this._fkProfileStart != activatedProfileId) || this._manualProfileActivation || reactivate)
        {
            // no activate profile, when is already activated
            GlobalData.logE("Event.startEvent","event_id="+this._id+" activate profile id="+this._fkProfileStart);

            if (interactive) {
                if (mergedProfile == null)
                    dataWrapper.activateProfileFromEvent(this._fkProfileStart, interactive, false, false, _notificationSound, true);
                else {
                    mergedProfile.mergeProfiles(this._fkProfileStart, dataWrapper);
                    if (this._manualProfileActivation) {
                        dataWrapper.getDatabaseHandler().saveMergedProfile(mergedProfile);
                        dataWrapper.activateProfileFromEvent(mergedProfile._id, interactive, true, true, _notificationSound, true);
                        mergedProfile._id = 0;
                    }
                }
            }
            else {
                if (mergedProfile == null)
                    dataWrapper.activateProfileFromEvent(this._fkProfileStart, interactive, false, false, "", true);
                else {
                    mergedProfile.mergeProfiles(this._fkProfileStart, dataWrapper);
                    if (this._manualProfileActivation) {
                        dataWrapper.getDatabaseHandler().saveMergedProfile(mergedProfile);
                        dataWrapper.activateProfileFromEvent(mergedProfile._id, interactive, true, true, "", true);
                        mergedProfile._id = 0;
                    }
                }
            }
        }
        else
        {
            dataWrapper.updateNotificationAndWidgets(activatedProfile, "");
        }

        return;
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
            if (_fkProfileEnd != GlobalData.PROFILE_NO_ACTIVATE)
            {
                if (_fkProfileEnd != activatedProfileId)
                {
                    GlobalData.logE("Event.pauseEvent","activate end porfile");
                    if (mergedProfile == null)
                        dataWrapper.activateProfileFromEvent(_fkProfileEnd, false, false, false, "", true);
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
                    GlobalData.logE("Event.pauseEvent","undone profile");
                    GlobalData.logE("Event.pauseEvent","_fkProfileEndActivated="+eventTimeline._fkProfileEndActivated);
                    if (eventTimeline._fkProfileEndActivated != 0)
                    {
                        if (mergedProfile == null)
                            dataWrapper.activateProfileFromEvent(eventTimeline._fkProfileEndActivated, false, false, false, "", true);
                        else
                            mergedProfile.mergeProfiles(eventTimeline._fkProfileEndActivated, dataWrapper);
                        profileActivated = true;
                    }
                }
            }
            // restart events when is set
            if ((_atEndDo == EATENDDO_RESTART_EVENTS) && allowRestart) {
                GlobalData.logE("Event.pauseEvent","restart events");

                EventsService.restartAtEndOfEvent = true;

                dataWrapper.getDatabaseHandler().addActivityLog(DatabaseHandler.ALTYPE_RESTARTEVENTS, null, null, null, 0);

                GlobalData.logE("$$$ restartEvents", "from Event.doActivateEndProfile");
                dataWrapper.restartEventsWithDelay(3, true);
                profileActivated = true;
            }

        }

        if (!profileActivated)
        {
            dataWrapper.updateNotificationAndWidgets(activatedProfile, "");
        }

    }

    public void pauseEvent(DataWrapper dataWrapper,
                            List<EventTimeline> eventTimelineList,
                            boolean activateReturnProfile,
                            boolean ignoreGlobalPref,
                            boolean noSetSystemEvent,
                            boolean log,
                            Profile mergedProfile,
                            boolean allowRestart)
    {
        // remove delay alarm
        removeDelayAlarm(dataWrapper, true); // for start delay

        if ((!GlobalData.getGlobalEventsRuning(dataWrapper.context)) && (!ignoreGlobalPref))
            // events are globally stopped
            return;

        if (!this.isRunnable())
            // event is not runnable, no pause it
            return;

/*		if (GlobalData.getEventsBlocked(dataWrapper.context))
        {
            // blocked by manual profile activation
            GlobalData.logE("Event.pauseEvent","event_id="+this._id+" events blocked");


            if (!_forceRun)
                // event is not forceRun
                return;
        }
*/

        // unblock event when paused
        dataWrapper.setEventBlocked(this, false);

        GlobalData.logE("@@@ Event.pauseEvent","event_id="+this._id+"-----------------------------------");
        GlobalData.logE("@@@ Event.pauseEvent","-- event_name="+this._name);

        int timeLineSize = eventTimelineList.size();

        // test whenever event exists in timeline
        boolean exists = false;
        int eventPosition = getEventTimelinePosition(eventTimelineList);
        GlobalData.logE("Event.pauseEvent","eventPosition="+eventPosition);

        exists = eventPosition != -1;

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
            if ((_atEndDo == EATENDDO_UNDONE_PROFILE) && (_fkProfileEnd != GlobalData.PROFILE_NO_ACTIVATE))
                alType = DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE_UNDOPROFILE;
            if ((_atEndDo == EATENDDO_RESTART_EVENTS) && (_fkProfileEnd != GlobalData.PROFILE_NO_ACTIVATE))
                alType = DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE_RESTARTEVENTS;
            else if (_atEndDo == EATENDDO_UNDONE_PROFILE)
                alType = DatabaseHandler.ALTYPE_EVENTEND_UNDOPROFILE;
            else if (_atEndDo == EATENDDO_RESTART_EVENTS)
                alType = DatabaseHandler.ALTYPE_EVENTEND_RESTARTEVENTS;
            else if (_fkProfileEnd != GlobalData.PROFILE_NO_ACTIVATE)
                alType = DatabaseHandler.ALTYPE_EVENTEND_ACTIVATEPROFILE;

            dataWrapper.getDatabaseHandler().addActivityLog(alType, _name, null, null, 0);
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
                GlobalData.setForceRunEventRunning(dataWrapper.context, false);
        //}

        if (exists)
        {
            doActivateEndProfile(dataWrapper, eventPosition, timeLineSize,
                    eventTimelineList, eventTimeline,
                    activateReturnProfile, mergedProfile, allowRestart);

        }

        return;
    }

    public void stopEvent(DataWrapper dataWrapper,
                            List<EventTimeline> eventTimelineList,
                            boolean activateReturnProfile,
                            boolean ignoreGlobalPref,
                            boolean saveEventStatus,
                            boolean log,
                            boolean allowRestart)
    {
        // remove delay alarm
        removeDelayAlarm(dataWrapper, true); // for start delay

        if ((!GlobalData.getGlobalEventsRuning(dataWrapper.context)) && (!ignoreGlobalPref))
            // events are globally stopped
            return;

        GlobalData.logE("@@@ Event.stopEvent","event_id="+this._id+"-----------------------------------");
        GlobalData.logE("@@@ Event.stopEvent", "-- event_name=" + this._name);

        if (this._status != ESTATUS_STOP)
        {
            pauseEvent(dataWrapper, eventTimelineList, activateReturnProfile, ignoreGlobalPref, true, false, null, allowRestart);
        }

        setSystemEvent(dataWrapper.context, ESTATUS_STOP);
        //int status = this._status;
        this._status = ESTATUS_STOP;
        if (saveEventStatus)
            dataWrapper.getDatabaseHandler().updateEventStatus(this);

        /*
        if (log && (status != this._status)) {
            dataWrapper.getDatabaseHandler().addActivityLog(DatabaseHandler.ALTYPE_EVENTSTOP, _name, null, null, 0);
        }*/

        return;
    }

    public int getStatus()
    {
        return _status;
    }

    public int getStatusFromDB(DataWrapper dataWrapper)
    {
        return dataWrapper.getDatabaseHandler().getEventStatus(this);
    }

    public void setStatus(int status)
    {
        _status = status;
    }

    public void setSystemEvent(Context context, int forStatus)
    {
        if (forStatus == ESTATUS_PAUSE)
        {
            // event paused
            // setup system event for next running status
            _eventPreferencesTime.setSystemRunningEvent(context);
            _eventPreferencesBattery.setSystemRunningEvent(context);
            _eventPreferencesCall.setSystemRunningEvent(context);
            _eventPreferencesPeripherals.setSystemRunningEvent(context);
            _eventPreferencesCalendar.setSystemRunningEvent(context);
            _eventPreferencesWifi.setSystemRunningEvent(context);
            _eventPreferencesScreen.setSystemRunningEvent(context);
            _eventPreferencesBluetooth.setSystemRunningEvent(context);
            _eventPreferencesSMS.setSystemRunningEvent(context);
            _eventPreferencesNotification.setSystemRunningEvent(context);
        }
        else
        if (forStatus == ESTATUS_RUNNING)
        {
            // event started
            // setup system event for pause status
            _eventPreferencesTime.setSystemPauseEvent(context);
            _eventPreferencesBattery.setSystemPauseEvent(context);
            _eventPreferencesCall.setSystemPauseEvent(context);
            _eventPreferencesPeripherals.setSystemPauseEvent(context);
            _eventPreferencesCalendar.setSystemPauseEvent(context);
            _eventPreferencesWifi.setSystemPauseEvent(context);
            _eventPreferencesScreen.setSystemPauseEvent(context);
            _eventPreferencesBluetooth.setSystemPauseEvent(context);
            _eventPreferencesSMS.setSystemPauseEvent(context);
            _eventPreferencesNotification.setSystemPauseEvent(context);
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
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    public void setDelayAlarm(DataWrapper dataWrapper,
                              boolean forStart,
                              boolean ignoreGlobalPref,
                              boolean log)
    {
        removeDelayAlarm(dataWrapper, forStart);

        if ((!GlobalData.getGlobalEventsRuning(dataWrapper.context)) && (!ignoreGlobalPref))
            // events are globally stopped
            return;

        if (!this.isRunnable())
            // event is not runnable, no pause it
            return;

        if (GlobalData.getEventsBlocked(dataWrapper.context))
        {
            // blocked by manual profile activation
            GlobalData.logE("Event.setDelayAlarm","event_id="+this._id+" events blocked");


            if (!_forceRun)
                // event is not forceRun
                return;
            if (_blocked)
                // forceRun event is temporary blocked
                return;
        }

        GlobalData.logE("@@@ Event.setDelayAlarm","event_id="+this._id+"-----------------------------------");
        GlobalData.logE("@@@ Event.setDelayAlarm","-- event_name="+this._name);
        GlobalData.logE("@@@ Event.setDelayAlarm","-- delay="+this._delayStart);

        if (this._delayStart > 0)
        {
            // delay for start is > 0
            // set alarm

            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, this._delayStart);
            long alarmTime = now.getTimeInMillis(); // + 1000 * /* 60 * */ this._delayStart;

            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            String result = sdf.format(alarmTime);
            if (forStart)
                GlobalData.logE("Event.setDelayAlarm","startTime="+result);
            else
                GlobalData.logE("Event.setDelayAlarm","endTime="+result);

            Intent intent = new Intent(dataWrapper.context, EventDelayBroadcastReceiver.class);
            intent.putExtra(GlobalData.EXTRA_EVENT_ID, this._id);
            intent.putExtra(GlobalData.EXTRA_START_SYSTEM_EVENT, forStart);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(dataWrapper.context.getApplicationContext(), (int) this._id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager alarmManager = (AlarmManager) dataWrapper.context.getSystemService(Activity.ALARM_SERVICE);

            if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
            if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            else
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);

            this._isInDelay = true;
        }
        else
            this._isInDelay = false;

        dataWrapper.getDatabaseHandler().updateEventInDelay(this);

        if (log && _isInDelay) {
            dataWrapper.getDatabaseHandler().addActivityLog(DatabaseHandler.ALTYPE_EVENTSTARTDELAY, _name, null, null, _delayStart);
        }

        return;
    }

    public void removeDelayAlarm(DataWrapper dataWrapper, boolean forStart)
    {
        AlarmManager alarmManager = (AlarmManager) dataWrapper.context.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(dataWrapper.context, EventDelayBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(dataWrapper.context.getApplicationContext(), (int) this._id, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            GlobalData.logE("Event.removeDelayAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        this._isInDelay = false;
        dataWrapper.getDatabaseHandler().updateEventInDelay(this);
    }

}

