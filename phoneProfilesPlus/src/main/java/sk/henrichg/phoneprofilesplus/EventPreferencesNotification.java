package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class EventPreferencesNotification extends EventPreferences {

    public String _applications;
    public long _startTime;
    public int _duration;

    static final String PREF_EVENT_NOTIFICATION_ENABLED = "eventNotificationEnabled";
    static final String PREF_EVENT_NOTIFICATION_APPLICATIONS = "eventNotificationApplications";
    static final String PREF_EVENT_NOTIFICATION_DURATION = "eventNotificationDuration";

    public EventPreferencesNotification(Event event,
                                        boolean enabled,
                                        String applications,
                                        int duration)
    {
        super(event, enabled);

        this._applications = applications;
        this._duration = duration;

        this._startTime = 0;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = ((EventPreferencesNotification)fromEvent._eventPreferencesNotification)._enabled;
        this._applications = ((EventPreferencesNotification)fromEvent._eventPreferencesNotification)._applications;
        this._duration = ((EventPreferencesNotification)fromEvent._eventPreferencesNotification)._duration;

        this._startTime = 0;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Editor editor = preferences.edit();
            editor.putBoolean(PREF_EVENT_NOTIFICATION_ENABLED, _enabled);
            editor.putString(PREF_EVENT_NOTIFICATION_APPLICATIONS, this._applications);
            editor.putString(PREF_EVENT_NOTIFICATION_DURATION, String.valueOf(this._duration));
            editor.commit();
        }
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this._enabled = preferences.getBoolean(PREF_EVENT_NOTIFICATION_ENABLED, false);
            this._applications = preferences.getString(PREF_EVENT_NOTIFICATION_APPLICATIONS, "");
            this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_NOTIFICATION_DURATION, "5"));
        }
    }

    @Override
    public String getPreferencesDescription(Context context)
    {
        String descr = "";

        if (!this._enabled)
        {
            //descr = descr + context.getString(R.string.event_type_notification) + ": ";
            //descr = descr + context.getString(R.string.event_preferences_not_enabled);
        }
        else
        {
            descr = descr + "<b>" + "\u2022 " + context.getString(R.string.event_type_notifications) + ": " + "</b>";
            descr = descr + context.getString(R.string.pref_event_duration) + ": " +this._duration;
        }

        return descr;
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (key.equals(PREF_EVENT_NOTIFICATION_APPLICATIONS)) {
                Preference preference = prefMng.findPreference(key);
                GUIData.setPreferenceTitleStyle(preference, false, true);
            }
            /*if (key.equals(PREF_EVENT_NOTIFICATION_DURATION)) {
                Preference preference = prefMng.findPreference(key);
                String sValue = value.toString();
                //int iValue = 0;
                //if (!sValue.isEmpty())
                //    iValue = Integer.valueOf(sValue);
                preference.setSummary(sValue);
            }*/
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_NOTIFICATION_APPLICATIONS) ||
            key.equals(PREF_EVENT_NOTIFICATION_DURATION))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, Context context)
    {
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_APPLICATIONS, _applications, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_DURATION, Integer.toString(_duration), context);
    }

    @Override
    public boolean isRunable()
    {

        boolean runable = super.isRunable();

        runable = runable && (!_applications.isEmpty());

        return runable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final boolean enabled =
                    PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext());
            Preference applicationsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_APPLICATIONS);
            Preference durationPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_DURATION);
            applicationsPreference.setEnabled(enabled);
            durationPreference.setEnabled(enabled);
        }
        else {
            PreferenceScreen preferenceScreen = (PreferenceScreen) prefMng.findPreference("eventPreferenceScreen");
            PreferenceScreen preferenceCategory = (PreferenceScreen) prefMng.findPreference("eventNotificationCategory");
            if (preferenceCategory != null)
                preferenceScreen.removePreference(preferenceCategory);
        }
    }

    @Override
    public boolean activateReturnProfile()
    {
        return true;
    }

    public long computeAlarm()
    {
        GlobalData.logE("EventPreferencesNotification.computeAlarm","xxx");

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
    public void setSystemRunningEvent(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsService

        GlobalData.logE("EventPreferencesNotification.setSystemRunningEvent","xxx");

        removeAlarm(context);
    }

    @Override
    public void setSystemPauseEvent(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsService

        GlobalData.logE("EventPreferencesNotification.setSystemPauseEvent","xxx");

        removeAlarm(context);

        if (!(isRunable() && _enabled))
            return;

        setAlarm(computeAlarm(), context);
    }

    @Override
    public void removeSystemEvent(Context context)
    {
        removeAlarm(context);

        GlobalData.logE("EventPreferencesNotification.removeSystemEvent", "xxx");
    }

    public void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationEventEndBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            GlobalData.logE("EventPreferencesNotification.removeAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    private void setAlarm(long alarmTime, Context context)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
        String result = sdf.format(alarmTime);
        GlobalData.logE("EventPreferencesNotification.setAlarm","endTime="+result);

        Intent intent = new Intent(context, NotificationEventEndBroadcastReceiver.class);
        intent.putExtra(GlobalData.EXTRA_EVENT_ID, _event._id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        else
        if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);

        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 24 * 60 * 60 * 1000 , pendingIntent);

    }

    public void saveStartTime(DataWrapper dataWrapper, String packageName, long startTime) {
        if (packageName == null)
            return;

        String[] splits = this._applications.split("\\|");
        for (int i = 0; i < splits.length; i++) {
            if (packageName.equals(splits[i])) {
                _event._eventPreferencesNotification._startTime = startTime;
                dataWrapper.getDatabaseHandler().updateNotificationStartTime(_event);
                if (_event.getStatus() == Event.ESTATUS_RUNNING)
                    setSystemPauseEvent(dataWrapper.context);
                break;
            }
        }
    }

}
