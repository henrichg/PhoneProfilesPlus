package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.Html;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class EventPreferencesNotification extends EventPreferences {

    public String _applications;
    public long _startTime;
    public int _duration;
    public boolean _endWhenRemoved;

    static final String PREF_EVENT_NOTIFICATION_ENABLED = "eventNotificationEnabled";
    static final String PREF_EVENT_NOTIFICATION_APPLICATIONS = "eventNotificationApplications";
    static final String PREF_EVENT_NOTIFICATION_DURATION = "eventNotificationDuration";
    static final String PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED = "eventNotificationEndWhenRemoved";

    static final String PREF_EVENT_NOTIFICATION_CATEGORY = "eventNotificationCategory";

    public EventPreferencesNotification(Event event,
                                        boolean enabled,
                                        String applications,
                                        int duration,
                                        boolean endWhenRemoved)
    {
        super(event, enabled);

        this._applications = applications;
        this._duration = duration;
        this._endWhenRemoved = endWhenRemoved;

        this._startTime = 0;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = ((EventPreferencesNotification)fromEvent._eventPreferencesNotification)._enabled;
        this._applications = ((EventPreferencesNotification)fromEvent._eventPreferencesNotification)._applications;
        this._duration = ((EventPreferencesNotification)fromEvent._eventPreferencesNotification)._duration;
        this._endWhenRemoved = ((EventPreferencesNotification)fromEvent._eventPreferencesNotification)._endWhenRemoved;

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
            editor.putBoolean(PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED, _endWhenRemoved);
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
            this._endWhenRemoved = preferences.getBoolean(PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED, false);
        }
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, Context context)
    {
        String descr = "";

        if (!this._enabled)
        {
            //descr = descr + context.getString(R.string.event_type_notification) + ": ";
            //descr = descr + context.getString(R.string.event_preferences_not_enabled);
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_notifications) + ": " + "</b>";
            }

            String selectedApplications = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            if (!this._applications.isEmpty() && !this._applications.equals("-")) {
                String[] splits = this._applications.split("\\|");
                if (splits.length == 1) {
                    String packageName = splits[0];
                    if (ApplicationsCache.isShortcut(splits[0]))
                        packageName = ApplicationsCache.getPackageName(splits[0]);

                    PackageManager packageManager = context.getPackageManager();
                    ApplicationInfo app;
                    try {
                        app = packageManager.getApplicationInfo(packageName, 0);
                        if (app != null)
                            selectedApplications = packageManager.getApplicationLabel(app).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                        //e.printStackTrace();
                        selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
                    }
                }
                else
                    selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
            }
            descr = descr + context.getString(R.string.event_preferences_notifications_applications) + ": " +selectedApplications + "; ";
            if (this._endWhenRemoved)
                descr = descr + context.getString(R.string.event_preferences_notifications_end_when_removed);
            else
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
                if (preference != null) {
                    GUIData.setPreferenceTitleStyle(preference, false, true, false);
                }
            }
            /*if (key.equals(PREF_EVENT_NOTIFICATION_DURATION)) {
                Preference preference = prefMng.findPreference(key);
                String sValue = value.toString();
                //int iValue = 0;
                //if (!sValue.isEmpty())
                //    iValue = Integer.valueOf(sValue);
                preference.setSummary(sValue);
            }*/
            if (key.equals(PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED)) {
                Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_DURATION);
                if (preference != null) {
                    preference.setEnabled(value.equals("false"));
                }
            }
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
        if (key.equals(PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_APPLICATIONS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_DURATION, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        EventPreferencesNotification tmp = new EventPreferencesNotification(this._event, this._enabled, this._applications, this._duration, this._endWhenRemoved);
        if (preferences != null)
            tmp.saveSharedPreferences(preferences);

        Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CATEGORY);
        if (preference != null) {
            GUIData.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable());
            preference.setSummary(Html.fromHtml(tmp.getPreferencesDescription(false, context)));
        }
    }

    @Override
    public boolean isRunnable()
    {

        boolean runable = super.isRunnable();

        runable = runable && (!_applications.isEmpty());

        return runable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            boolean enabled =
                    PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext());
            Preference applicationsPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_APPLICATIONS);
            Preference endWhenRemovedPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED);
            Preference durationPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_DURATION);
            if (applicationsPreference != null)
                applicationsPreference.setEnabled(enabled);
            if (endWhenRemovedPreference != null)
                endWhenRemovedPreference.setEnabled(enabled);

            SharedPreferences preferences = prefMng.getSharedPreferences();
            if (preferences != null) {
                boolean endWhenRemoved = preferences.getBoolean(PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED, false);
                enabled = enabled && (!endWhenRemoved);
                if (durationPreference != null)
                    durationPreference.setEnabled(enabled);
            }

        }
        else {
            PreferenceScreen preferenceScreen = (PreferenceScreen) prefMng.findPreference("eventPreferenceScreen");
            PreferenceScreen preferenceCategory = (PreferenceScreen) prefMng.findPreference("eventNotificationCategory");
            if ((preferenceCategory != null) && (preferenceScreen != null))
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
    public void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsService

        GlobalData.logE("EventPreferencesNotification.setSystemRunningEvent","xxx");

        removeAlarm(context);
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsService

        GlobalData.logE("EventPreferencesNotification.setSystemPauseEvent","xxx");

        removeAlarm(context);

        if (!(isRunnable() && _enabled))
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
        //intent.putExtra(GlobalData.EXTRA_EVENT_ID, _event._id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 23))
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime+GlobalData.EVENT_ALARM_TIME_OFFSET, pendingIntent);
        else
        if (GlobalData.exactAlarms && (android.os.Build.VERSION.SDK_INT >= 19))
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime+GlobalData.EVENT_ALARM_TIME_OFFSET, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime+GlobalData.EVENT_ALARM_TIME_OFFSET, pendingIntent);
    }

    /*
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
    */

    public boolean isNotificationVisible(DataWrapper dataWrapper) {

        PPNotificationListenerService.getNotifiedPackages(dataWrapper.context);

        String[] splits = this._applications.split("\\|");
        for (int i = 0; i < splits.length; i++) {
            String packageName = splits[i];
            if (ApplicationsCache.isShortcut(splits[i]))
                packageName = ApplicationsCache.getPackageName(splits[i]);

            PostedNotificationData notification = PPNotificationListenerService.getNotificationPosted(dataWrapper.context, packageName);
            if (notification != null)
                return true;
        }
        return false;
    }

    public void saveStartTime(DataWrapper dataWrapper) {

        PPNotificationListenerService.getNotifiedPackages(dataWrapper.context);

        String[] splits = this._applications.split("\\|");
        for (int i = 0; i < splits.length; i++) {
            String packageName = splits[i];
            if (ApplicationsCache.isShortcut(splits[i]))
                packageName = ApplicationsCache.getPackageName(splits[i]);

            PostedNotificationData notification = PPNotificationListenerService.getNotificationPosted(dataWrapper.context, packageName);
            if (notification != null) {
                _event._eventPreferencesNotification._startTime = notification.time;
                dataWrapper.getDatabaseHandler().updateNotificationStartTime(_event);
                if (_event.getStatus() == Event.ESTATUS_RUNNING)
                    setSystemEventForPause(dataWrapper.context);
                break;
            }
        }
    }

}
