package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Html;

public class EventPreferencesApplication extends EventPreferences {

    public String _applications;
    //public long _startTime;
    //public int _duration;

    static final String PREF_EVENT_APPLICATION_ENABLED = "eventApplicationEnabled";
    static final String PREF_EVENT_APPLICATION_APPLICATIONS = "eventApplicationApplications";
    //static final String PREF_EVENT_NOTIFICATION_DURATION = "eventNotificationDuration";

    static final String PREF_EVENT_APPLICATION_CATEGORY = "eventApplicationCategory";

    public EventPreferencesApplication(Event event,
                                       boolean enabled,
                                       String applications/*,
                                       int duration*/)
    {
        super(event, enabled);

        this._applications = applications;
        //this._duration = duration;

        //this._startTime = 0;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = ((EventPreferencesApplication)fromEvent._eventPreferencesApplication)._enabled;
        this._applications = ((EventPreferencesApplication)fromEvent._eventPreferencesApplication)._applications;
        //this._duration = ((EventPreferencesApplication)fromEvent._eventPreferencesNotification)._duration;

        //this._startTime = 0;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Editor editor = preferences.edit();
            editor.putBoolean(PREF_EVENT_APPLICATION_ENABLED, _enabled);
            editor.putString(PREF_EVENT_APPLICATION_APPLICATIONS, this._applications);
            //editor.putString(PREF_EVENT_NOTIFICATION_DURATION, String.valueOf(this._duration));
            editor.commit();
        //}
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this._enabled = preferences.getBoolean(PREF_EVENT_APPLICATION_ENABLED, false);
            this._applications = preferences.getString(PREF_EVENT_APPLICATION_APPLICATIONS, "");
            //this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_NOTIFICATION_DURATION, "5"));
        //}
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
                descr = descr + "<b>" + context.getString(R.string.event_type_applications) + ": " + "</b>";
            }

            String selectedApplications = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            if (!this._applications.isEmpty() && !this._applications.equals("-")) {
                String[] splits = this._applications.split("\\|");
                if (splits.length == 1) {
                    String packageName = ApplicationsCache.getPackageName(splits[0]);

                    PackageManager packageManager = context.getPackageManager();
                    if (ApplicationsCache.getActivityName(splits[0]).isEmpty()) {
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
                    else {
                        Intent intent = new Intent();
                        intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                        ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                        if (info != null)
                            selectedApplications = info.loadLabel(packageManager).toString();
                    }
                }
                else
                    selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
            }
            descr = descr + selectedApplications;

            //descr = descr + context.getString(R.string.event_preferences_notifications_applications) + ": " +selectedApplications + "; ";
            //descr = descr + context.getString(R.string.pref_event_duration) + ": " +tmp._duration;
        }

        return descr;
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (key.equals(PREF_EVENT_APPLICATION_APPLICATIONS)) {
                Preference preference = prefMng.findPreference(key);
                GUIData.setPreferenceTitleStyle(preference, false, true, false);
            }
            /*if (key.equals(PREF_EVENT_NOTIFICATION_DURATION)) {
                Preference preference = prefMng.findPreference(key);
                String sValue = value.toString();
                //int iValue = 0;
                //if (!sValue.isEmpty())
                //    iValue = Integer.valueOf(sValue);
                preference.setSummary(sValue);
            }*/
        //}
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_APPLICATION_APPLICATIONS)/* ||
            key.equals(PREF_EVENT_NOTIFICATION_DURATION)*/)
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_APPLICATION_APPLICATIONS, preferences, context);
        //setSummary(prefMng, PREF_EVENT_NOTIFICATION_DURATION, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (GlobalData.isEventPreferenceAllowed(PREF_EVENT_APPLICATION_ENABLED, context) == GlobalData.PREFERENCE_ALLOWED) {
            EventPreferencesApplication tmp = new EventPreferencesApplication(this._event, this._enabled, this._applications);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_APPLICATION_CATEGORY);
            if (preference != null) {
                GUIData.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable(context));
                preference.setSummary(Html.fromHtml(tmp.getPreferencesDescription(false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_APPLICATION_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getResources().getString(GlobalData.getNotAllowedPreferenceReasonString()));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    public boolean isRunnable(Context context)
    {

        boolean runable = super.isRunnable(context);

        runable = runable && (!_applications.isEmpty());

        return runable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final boolean enabled =
                    ForegroundApplicationChangedService.isEnabled(context.getApplicationContext());
            Preference applicationsPreference = prefMng.findPreference(PREF_EVENT_APPLICATION_APPLICATIONS);
            if (applicationsPreference != null) {
                //Preference durationPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_DURATION);
                applicationsPreference.setEnabled(enabled);
                //durationPreference.setEnabled(enabled);
            }
        //}
        /*else {
            PreferenceScreen preferenceScreen = (PreferenceScreen) prefMng.findPreference("eventPreferenceScreen");
            PreferenceScreen preferenceCategory = (PreferenceScreen) prefMng.findPreference("eventNotificationCategory");
            if (preferenceCategory != null)
                preferenceScreen.removePreference(preferenceCategory);
        }*/
    }

    @Override
    public boolean activateReturnProfile()
    {
        return true;
    }

    /*
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
    */

    @Override
    public void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsService

        //GlobalData.logE("EventPreferencesNotification.setSystemRunningEvent","xxx");

        //removeAlarm(context);
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsService

        //GlobalData.logE("EventPreferencesNotification.setSystemPauseEvent","xxx");

        //removeAlarm(context);

        //if (!(isRunnable() && _enabled))
        //    return;

        //setAlarm(computeAlarm(), context);
    }

    @Override
    public void removeSystemEvent(Context context)
    {
        //removeAlarm(context);

        //GlobalData.logE("EventPreferencesNotification.removeSystemEvent", "xxx");
    }

    /*
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

        if (GlobalData.exactAlarms && (Build.VERSION.SDK_INT >= 23))
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        else
        if (GlobalData.exactAlarms && (Build.VERSION.SDK_INT >= 19))
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
    */

}
