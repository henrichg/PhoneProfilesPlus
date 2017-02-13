package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

class EventPreferencesRadioSwitch extends EventPreferences {

    int _wifi;
    int _bluetooth;
    int _mobileData;
    int _gps;
    int _nfc;
    int _airplaneMode;
    long _startTime;
    int _duration;
    boolean _permanentRun;

    static final int RADIO_TYPE_WIFI = 1;
    static final int RADIO_TYPE_BLUETOOTH = 2;
    static final int RADIO_TYPE_MOBILE_DATA = 3;
    static final int RADIO_TYPE_GPS = 4;
    static final int RADIO_TYPE_NFC = 5;
    static final int RADIO_TYPE_AIRPLANE_MODE = 6;

    static final String PREF_EVENT_RADIO_SWITCH_ENABLED = "eventRadioSwitchEnabled";
    private static final String PREF_EVENT_RADIO_SWITCH_WIFI = "eventRadioSwitchWifi";
    private static final String PREF_EVENT_RADIO_SWITCH_BLUETOOTH = "eventRadioSwitchBluetooth";
    private static final String PREF_EVENT_RADIO_SWITCH_MOBILE_DATA = "eventRadioSwitchMobileData";
    private static final String PREF_EVENT_RADIO_SWITCH_GPS = "eventRadioSwitchGPS";
    private static final String PREF_EVENT_RADIO_SWITCH_NFC = "eventRadioSwitchNFC";
    private static final String PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE = "eventRadioSwitchAirplaneMode";
    private static final String PREF_EVENT_RADIO_SWITCH_DURATION = "eventRadioSwitchDuration";
    private static final String PREF_EVENT_RADIO_SWITCH_PERMANENT_RUN = "eventRadioSwitchPermanentRun";

    private static final String PREF_EVENT_RADIO_SWITCH_CATEGORY = "eventRadioSwitchCategory";

    EventPreferencesRadioSwitch(Event event,
                                boolean enabled,
                                int wifi,
                                int bluetooth,
                                int mobileData,
                                int gps,
                                int nfc,
                                int airplaneMode,
                                boolean permanentRun,
                                int duration)
    {
        super(event, enabled);
        this._wifi = wifi;
        this._bluetooth = bluetooth;
        this._mobileData = mobileData;
        this._gps = gps;
        this._nfc = nfc;
        this._airplaneMode = airplaneMode;
        this._permanentRun = permanentRun;
        this._duration = duration;

        this._startTime = 0;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesRadioSwitch._enabled;
        this._wifi = fromEvent._eventPreferencesRadioSwitch._wifi;
        this._bluetooth = fromEvent._eventPreferencesRadioSwitch._bluetooth;
        this._mobileData = fromEvent._eventPreferencesRadioSwitch._mobileData;
        this._gps = fromEvent._eventPreferencesRadioSwitch._gps;
        this._nfc = fromEvent._eventPreferencesRadioSwitch._nfc;
        this._airplaneMode = fromEvent._eventPreferencesRadioSwitch._airplaneMode;
        this._permanentRun = fromEvent._eventPreferencesRadioSwitch._permanentRun;
        this._duration = fromEvent._eventPreferencesRadioSwitch._duration;

        this._startTime = 0;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, _enabled);
        editor.putString(PREF_EVENT_RADIO_SWITCH_WIFI, String.valueOf(this._wifi));
        editor.putString(PREF_EVENT_RADIO_SWITCH_BLUETOOTH, String.valueOf(this._bluetooth));
        editor.putString(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, String.valueOf(this._mobileData));
        editor.putString(PREF_EVENT_RADIO_SWITCH_GPS, String.valueOf(this._gps));
        editor.putString(PREF_EVENT_RADIO_SWITCH_NFC, String.valueOf(this._nfc));
        editor.putString(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE, String.valueOf(this._airplaneMode));
        editor.putBoolean(PREF_EVENT_RADIO_SWITCH_PERMANENT_RUN, this._permanentRun);
        editor.putString(PREF_EVENT_RADIO_SWITCH_DURATION, String.valueOf(this._duration));
        editor.commit();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_RADIO_SWITCH_ENABLED, false);
        this._wifi = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_WIFI, "0"));
        this._bluetooth = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_BLUETOOTH, "0"));
        this._mobileData = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, "0"));
        this._gps = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_GPS, "0"));
        this._nfc = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_NFC, "0"));
        this._airplaneMode = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE, "0"));
        this._permanentRun = preferences.getBoolean(PREF_EVENT_RADIO_SWITCH_PERMANENT_RUN, true);
        this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_RADIO_SWITCH_DURATION, "5"));
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_radioSwitch_summary);
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_radioSwitch) + ": " + "</b>";
            }

            if (this._wifi != 0) {
                descr = descr + context.getString(R.string.profile_preferences_deviceWiFi) + ": ";
                String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                descr = descr + fields[this._wifi] + "; ";
            }

            if (this._bluetooth != 0) {
                descr = descr + context.getString(R.string.profile_preferences_deviceBluetooth) + ": ";
                String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                descr = descr + fields[this._bluetooth] + "; ";
            }

            if (this._mobileData != 0) {
                descr = descr + context.getString(R.string.profile_preferences_deviceMobileData) + ": ";
                String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                descr = descr + fields[this._mobileData] + "; ";
            }

            if (this._gps != 0) {
                descr = descr + context.getString(R.string.profile_preferences_deviceGPS) + ": ";
                String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                descr = descr + fields[this._gps] + "; ";
            }

            if (this._nfc != 0) {
                descr = descr + context.getString(R.string.profile_preferences_deviceNFC) + ": ";
                String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                descr = descr + fields[this._nfc] + "; ";
            }

            if (this._airplaneMode != 0) {
                descr = descr + context.getString(R.string.profile_preferences_deviceAirplaneMode) + ": ";
                String[] fields = context.getResources().getStringArray(R.array.eventRadioSwitchArray);
                descr = descr + fields[this._airplaneMode] + "; ";
            }

            if (this._permanentRun)
                descr = descr + context.getString(R.string.pref_event_permanentRun);
            else
                descr = descr + context.getString(R.string.pref_event_duration) + ": " + GlobalGUIRoutines.getDurationString(this._duration);
        }

        return descr;
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_RADIO_SWITCH_WIFI) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_BLUETOOTH) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_GPS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_NFC) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_RADIO_SWITCH_PERMANENT_RUN)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_DURATION);
            if (preference != null) {
                preference.setEnabled(value.equals("false"));
            }
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_RADIO_SWITCH_WIFI) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_BLUETOOTH) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_GPS) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_NFC) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE) ||
            key.equals(PREF_EVENT_RADIO_SWITCH_DURATION))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
        if (key.equals(PREF_EVENT_RADIO_SWITCH_PERMANENT_RUN)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_WIFI, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_BLUETOOTH, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_MOBILE_DATA, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_GPS, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_NFC, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_PERMANENT_RUN, preferences, context);
        setSummary(prefMng, PREF_EVENT_RADIO_SWITCH_DURATION, preferences, context);

        if (PPApplication.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED, context)
                != PPApplication.PREFERENCE_ALLOWED)
        {
            /*Preference preference = prefMng.findPreference(PREF_EVENT_NFC_ENABLED);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_NFC_NFC_TAGS);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_NFC_DURATION);
            if (preference != null) preference.setEnabled(false);*/
        }
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (PPApplication.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED) {
            EventPreferencesRadioSwitch tmp = new EventPreferencesRadioSwitch(this._event, this._enabled,
                    this._wifi, this._bluetooth, this._mobileData, this._gps, this._nfc, this._airplaneMode,
                    this._permanentRun, this._duration);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_CATEGORY);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ PPApplication.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    public boolean isRunnable(Context context)
    {

        boolean runable = super.isRunnable(context);

        runable = runable &&
                ((_wifi != 0) || (_bluetooth != 0) || (_mobileData != 0) || (_gps != 0) || (_nfc != 0) || (_airplaneMode != 0));

        return runable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context)
    {
        boolean enabled = PPApplication.isEventPreferenceAllowed(PREF_EVENT_RADIO_SWITCH_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED;

        Preference preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_WIFI);
        if (preference != null)
            preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_BLUETOOTH);
        if (preference != null)
            preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_MOBILE_DATA);
        if (preference != null)
            preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_GPS);
        if (preference != null)
            preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_NFC);
        if (preference != null)
            preference.setEnabled(enabled);
        preference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_AIRPLANE_MODE);
        if (preference != null)
            preference.setEnabled(enabled);

        Preference permanentRunPreference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_PERMANENT_RUN);
        Preference durationPreference = prefMng.findPreference(PREF_EVENT_RADIO_SWITCH_DURATION);
        if (permanentRunPreference != null)
            permanentRunPreference.setEnabled(enabled);

        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences != null) {
            boolean permanentRun = preferences.getBoolean(PREF_EVENT_RADIO_SWITCH_PERMANENT_RUN, false);
            enabled = enabled && (!permanentRun);
            if (durationPreference != null)
                durationPreference.setEnabled(enabled);
        }
    }

    @Override
    public boolean activateReturnProfile()
    {
        return true;
    }

    long computeAlarm()
    {
        PPApplication.logE("EventPreferencesRadioSwitch.computeAlarm","xxx");

        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = TimeZone.getDefault().getRawOffset();

        calEndTime.setTimeInMillis((_startTime - gmtOffset) + (_duration * 1000));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = calEndTime.getTimeInMillis();

        return alarmTime;
    }

    @Override
    public void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsService

        PPApplication.logE("EventPreferencesRadioSwitch.setSystemRunningEvent","xxx");

        removeAlarm(context);
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsService

        PPApplication.logE("EventPreferencesRadioSwitch.setSystemPauseEvent","xxx");

        removeAlarm(context);

        if (!(isRunnable(context) && _enabled))
            return;

        setAlarm(computeAlarm(), context);
    }

    @Override
    public void removeSystemEvent(Context context)
    {
        removeAlarm(context);

        PPApplication.logE("EventPreferencesRadioSwitch.removeSystemEvent", "xxx");
    }

    public void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(context, RadioSwitchEventEndBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            PPApplication.logE("EventPreferencesRadioSwitch.removeAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    private void setAlarm(long alarmTime, Context context)
    {
        if (!_permanentRun) {
            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            String result = sdf.format(alarmTime);
            PPApplication.logE("EventPreferencesRadioSwitch.setAlarm", "endTime=" + result);

            Intent intent = new Intent(context, RadioSwitchEventEndBroadcastReceiver.class);
            //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

            if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + PPApplication.EVENT_ALARM_TIME_OFFSET, pendingIntent);
            else if (PPApplication.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime + PPApplication.EVENT_ALARM_TIME_OFFSET, pendingIntent);
            else
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime + PPApplication.EVENT_ALARM_TIME_OFFSET, pendingIntent);
        }
    }

    void saveStartTime(DataWrapper dataWrapper, int radioType, boolean radioState, long startTime) {

        boolean radioConfigured = false;

        switch (radioType) {
            case RADIO_TYPE_WIFI:
                if (this._wifi != 0)
                    radioConfigured = true;
                break;
            case RADIO_TYPE_BLUETOOTH:
                if (this._bluetooth != 0)
                    radioConfigured = true;
                break;
            case RADIO_TYPE_MOBILE_DATA:
                if (this._mobileData != 0)
                    radioConfigured = true;
                break;
            case RADIO_TYPE_GPS:
                if (this._gps != 0)
                    radioConfigured = true;
                break;
            case RADIO_TYPE_NFC:
                if (this._nfc != 0)
                    radioConfigured = true;
                break;
            case RADIO_TYPE_AIRPLANE_MODE:
                if (this._airplaneMode != 0)
                    radioConfigured = true;
                break;
        }

        if (radioConfigured)
            this._startTime = startTime;
        else
            this._startTime = 0;

        dataWrapper.getDatabaseHandler().updateRadioSwitchStartTime(_event);
        if (_event.getStatus() == Event.ESTATUS_RUNNING)
            setSystemEventForPause(dataWrapper.context);
    }

}
