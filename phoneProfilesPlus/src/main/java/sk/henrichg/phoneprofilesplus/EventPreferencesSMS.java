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

public class EventPreferencesSMS extends EventPreferences {

    //public int _smsEvent;
    public String _contacts;
    public String _contactGroups;
    public int _contactListType;
    public long _startTime;
    public int _duration;

    static final String PREF_EVENT_SMS_ENABLED = "eventSMSEnabled";
    //static final String PREF_EVENT_SMS_EVENT = "eventSMSEvent";
    static final String PREF_EVENT_SMS_CONTACTS = "eventSMSContacts";
    static final String PREF_EVENT_SMS_CONTACT_GROUPS = "eventSMSContactGroups";
    static final String PREF_EVENT_SMS_CONTACT_LIST_TYPE = "eventSMSContactListType";
    static final String PREF_EVENT_SMS_DURATION = "eventSMSDuration";

    //static final int SMS_EVENT_UNDEFINED = -1;
    //static final int SMS_EVENT_INCOMING = 0;
    //static final int SMS_EVENT_OUTGOING = 1;

    static final int CONTACT_LIST_TYPE_WHITE_LIST = 0;
    static final int CONTACT_LIST_TYPE_BLACK_LIST = 1;
    static final int CONTACT_LIST_TYPE_NOT_USE = 2;

    public EventPreferencesSMS(Event event,
                                    boolean enabled,
                                    //int smsEvent,
                                    String contacts,
                                    String contactGroups,
                                    int contactListType,
                                    int duration)
    {
        super(event, enabled);

        //this._smsEvent = smsEvent;
        this._contacts = contacts;
        this._contactGroups = contactGroups;
        this._contactListType = contactListType;
        this._duration = duration;

        this._startTime = 0;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = ((EventPreferencesSMS)fromEvent._eventPreferencesSMS)._enabled;
        //this._smsEvent = ((EventPreferencesSMS)fromEvent._eventPreferencesSMS)._smsEvent;
        this._contacts = ((EventPreferencesSMS)fromEvent._eventPreferencesSMS)._contacts;
        this._contactGroups = ((EventPreferencesSMS)fromEvent._eventPreferencesSMS)._contactGroups;
        this._contactListType = ((EventPreferencesSMS)fromEvent._eventPreferencesSMS)._contactListType;
        this._duration = ((EventPreferencesSMS)fromEvent._eventPreferencesSMS)._duration;

        this._startTime = 0;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_SMS_ENABLED, _enabled);
        //editor.putString(PREF_EVENT_SMS_EVENT, String.valueOf(this._smsEvent));
        editor.putString(PREF_EVENT_SMS_CONTACTS, this._contacts);
        editor.putString(PREF_EVENT_SMS_CONTACT_GROUPS, this._contactGroups);
        editor.putString(PREF_EVENT_SMS_CONTACT_LIST_TYPE, String.valueOf(this._contactListType));
        editor.putString(PREF_EVENT_SMS_DURATION, String.valueOf(this._duration));
        editor.commit();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_SMS_ENABLED, false);
        //this._smsEvent = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_EVENT, "0"));
        this._contacts = preferences.getString(PREF_EVENT_SMS_CONTACTS, "");
        this._contactGroups = preferences.getString(PREF_EVENT_SMS_CONTACT_GROUPS, "");
        this._contactListType = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_CONTACT_LIST_TYPE, "0"));
        this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_DURATION, "5"));
    }

    @Override
    public String getPreferencesDescription(String description, Context context)
    {
        String descr = description;

        if (!this._enabled)
        {
            //descr = descr + context.getString(R.string.event_type_sms) + ": ";
            //descr = descr + context.getString(R.string.event_preferences_not_enabled);
        }
        else
        {
            descr = descr + "\u2022 " + context.getString(R.string.event_type_sms) + ": ";

            //descr = descr + context.getString(R.string.pref_event_sms_event);
            //String[] smsEvents = context.getResources().getStringArray(R.array.eventSMSEventsArray);
            //descr = descr + ": " + smsEvents[this._smsEvent] + "; ";
            descr = descr + context.getString(R.string.pref_event_sms_contactListType);
            String[] cntactListTypes = context.getResources().getStringArray(R.array.eventSMSContactListTypeArray);
            descr = descr + ": " + cntactListTypes[this._contactListType] + "; ";
            descr = descr + this._duration;
        }

        return descr;
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (/*key.equals(PREF_EVENT_SMS_EVENT) ||*/ key.equals(PREF_EVENT_SMS_CONTACT_LIST_TYPE))
        {
            ListPreference listPreference = (ListPreference)prefMng.findPreference(key);
            int index = listPreference.findIndexOfValue(value);
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            listPreference.setSummary(summary);
        }
        if (key.equals(PREF_EVENT_SMS_CONTACTS))
        {
            Preference preference = prefMng.findPreference(key);
            GUIData.setPreferenceTitleStyle(preference, false, true);
        }
        if (key.equals(PREF_EVENT_SMS_CONTACT_GROUPS))
        {
            Preference preference = prefMng.findPreference(key);
            GUIData.setPreferenceTitleStyle(preference, false, true);
        }
        if (key.equals(PREF_EVENT_SMS_DURATION)) {
            Preference preference = prefMng.findPreference(key);
            String sValue = value.toString();
            //int iValue = 0;
            //if (!sValue.isEmpty())
            //    iValue = Integer.valueOf(sValue);
            preference.setSummary(sValue);
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (/*key.equals(PREF_EVENT_SMS_EVENT) ||*/
            key.equals(PREF_EVENT_SMS_CONTACT_LIST_TYPE) ||
            key.equals(PREF_EVENT_SMS_CONTACTS) ||
            key.equals(PREF_EVENT_SMS_CONTACT_GROUPS) ||
            key.equals(PREF_EVENT_SMS_DURATION))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, Context context)
    {
        //setSummary(prefMng, PREF_EVENT_SMS_EVENT, Integer.toString(_smsEvent), context);
        setSummary(prefMng, PREF_EVENT_SMS_CONTACT_LIST_TYPE, Integer.toString(_contactListType), context);
        setSummary(prefMng, PREF_EVENT_SMS_CONTACTS, _contacts, context);
        setSummary(prefMng, PREF_EVENT_SMS_CONTACT_GROUPS, _contactGroups, context);
        setSummary(prefMng, PREF_EVENT_SMS_DURATION, Integer.toString(_duration), context);
    }

    @Override
    public boolean isRunable()
    {

        boolean runable = super.isRunable();

        runable = runable && ((_contactListType == CONTACT_LIST_TYPE_NOT_USE) ||
                              (!(_contacts.isEmpty() && _contactGroups.isEmpty())));

        return runable;
    }

    @Override
    public boolean activateReturnProfile()
    {
        return true;
    }

    public long computeAlarm()
    {
        GlobalData.logE("EventPreferencesSMS.computeAlarm","xxx");

        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = TimeZone.getDefault().getRawOffset();

        calEndTime.setTimeInMillis((_startTime - gmtOffset) + (5 * 1000));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = calEndTime.getTimeInMillis();

        return alarmTime;
    }

    @Override
    public void setSystemRunningEvent(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsService

        GlobalData.logE("EventPreferencesSMS.setSystemRunningEvent","xxx");

        removeAlarm(context);
    }

    @Override
    public void setSystemPauseEvent(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsService

        GlobalData.logE("EventPreferencesSMS.setSystemPauseEvent","xxx");

        removeAlarm(context);

        if (!(isRunable() && _enabled))
            return;

        setAlarm(computeAlarm(), context);
    }

    @Override
    public void removeSystemEvent(Context context)
    {
        removeAlarm(context);

        GlobalData.logE("EventPreferencesSMS.removeSystemEvent","xxx");
    }

    public void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(context, EventsSMSBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            GlobalData.logE("EventPreferencesSMS.removeAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    private void setAlarm(long alarmTime, Context context)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
        String result = sdf.format(alarmTime);
        GlobalData.logE("EventPreferencesSMS.setAlarm","endTime="+result);

        Intent intent = new Intent(context, EventsSMSBroadcastReceiver.class);
        intent.putExtra(GlobalData.EXTRA_EVENT_ID, _event._id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);

        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);

    }

}
