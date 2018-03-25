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

class EventPreferencesNotification extends EventPreferences {

    String _applications;
    boolean _inCall;
    boolean _missedCall;
    //long _startTime;
    //boolean _permanentRun;
    //int _duration;
    //boolean _endWhenRemoved;

    static final String PREF_EVENT_NOTIFICATION_ENABLED = "eventNotificationEnabled";
    private static final String PREF_EVENT_NOTIFICATION_APPLICATIONS = "eventNotificationApplications";
    private static final String PREF_EVENT_NOTIFICATION_IN_CALL = "eventNotificationInCall";
    private static final String PREF_EVENT_NOTIFICATION_MISSED_CALL = "eventNotificationMissedCall";
    //private static final String PREF_EVENT_NOTIFICATION_PERMANENT_RUN = "eventNotificationPermanentRun";
    //private static final String PREF_EVENT_NOTIFICATION_DURATION = "eventNotificationDuration";
    //private static final String PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED = "eventNotificationEndWhenRemoved";

    private static final String PREF_EVENT_NOTIFICATION_CATEGORY = "eventNotificationCategory";

    EventPreferencesNotification(Event event,
                                        boolean enabled,
                                        String applications,
                                        boolean inCall,
                                        boolean missedCall/*,
                                        boolean permanentRun,
                                        int duration,
                                        boolean endWhenRemoved*/)
    {
        super(event, enabled);

        this._applications = applications;
        this._inCall = inCall;
        this._missedCall = missedCall;
        //this._permanentRun = permanentRun;
        //this._duration = duration;
        //this._endWhenRemoved = endWhenRemoved;

        //this._startTime = 0;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesNotification._enabled;
        this._applications = fromEvent._eventPreferencesNotification._applications;
        this._inCall = fromEvent._eventPreferencesNotification._inCall;
        this._missedCall = fromEvent._eventPreferencesNotification._missedCall;
        //this._permanentRun = fromEvent._eventPreferencesNotification._permanentRun;
        //this._duration = fromEvent._eventPreferencesNotification._duration;
        //this._endWhenRemoved = fromEvent._eventPreferencesNotification._endWhenRemoved;

        //this._startTime = 0;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Editor editor = preferences.edit();
            editor.putBoolean(PREF_EVENT_NOTIFICATION_ENABLED, _enabled);
            editor.putString(PREF_EVENT_NOTIFICATION_APPLICATIONS, this._applications);
            editor.putBoolean(PREF_EVENT_NOTIFICATION_IN_CALL, this._inCall);
            editor.putBoolean(PREF_EVENT_NOTIFICATION_MISSED_CALL, this._missedCall);
            //editor.putBoolean(PREF_EVENT_NOTIFICATION_PERMANENT_RUN, this._permanentRun);
            //editor.putString(PREF_EVENT_NOTIFICATION_DURATION, String.valueOf(this._duration));
            //editor.putBoolean(PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED, _endWhenRemoved);
            editor.apply();
        //}
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this._enabled = preferences.getBoolean(PREF_EVENT_NOTIFICATION_ENABLED, false);
            this._applications = preferences.getString(PREF_EVENT_NOTIFICATION_APPLICATIONS, "");
            this._inCall = preferences.getBoolean(PREF_EVENT_NOTIFICATION_IN_CALL, false);
            this._missedCall = preferences.getBoolean(PREF_EVENT_NOTIFICATION_MISSED_CALL, false);
            //this._permanentRun = preferences.getBoolean(PREF_EVENT_NOTIFICATION_PERMANENT_RUN, false);
            //this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_NOTIFICATION_DURATION, "5"));
            //this._endWhenRemoved = preferences.getBoolean(PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED, false);
        //}
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_notification_summary);
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_notifications) + ": " + "</b>";
            }

            String selectedApplications = context.getString(R.string.applications_multiselect_summary_text_not_selected);
            if (!PPNotificationListenerService.isNotificationListenerServiceEnabled(context)) {
                selectedApplications = context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+context.getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings);
            }
            else {
                if (this._inCall) {
                    descr = descr + context.getString(R.string.event_preferences_notifications_inCall);
                }
                if (this._missedCall) {
                    if (this._inCall)
                        descr = descr + "; ";
                    descr = descr + context.getString(R.string.event_preferences_notifications_missedCall);
                }
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
                            } catch (Exception e) {
                                selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.setClassName(ApplicationsCache.getPackageName(splits[0]), ApplicationsCache.getActivityName(splits[0]));
                            ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                            if (info != null)
                                selectedApplications = info.loadLabel(packageManager).toString();
                        }
                    } else
                        selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + ": " + splits.length;
                }
            }
            if (this._inCall || this._missedCall)
                descr = descr + "; ";
            descr = descr + "(S) "+context.getString(R.string.event_preferences_notifications_applications) + ": " + selectedApplications;// + "; ";
            /*if (this._endWhenRemoved)
                descr = descr + context.getString(R.string.event_preferences_notifications_end_when_removed);
            else {
                if (this._permanentRun)
                    descr = descr + context.getString(R.string.pref_event_permanentRun);
                else
                    descr = descr + context.getString(R.string.pref_event_duration) + ": " + GlobalGUIRoutines.getDurationString(this._duration);
            }*/
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (key.equals(PREF_EVENT_NOTIFICATION_IN_CALL) ||
                key.equals(PREF_EVENT_NOTIFICATION_MISSED_CALL)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    GlobalGUIRoutines.setPreferenceTitleStyle(preference, value.equals("true"), false, false, false);
                }
            }
            if (key.equals(PREF_EVENT_NOTIFICATION_APPLICATIONS)) {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, false, false, true);
                }
            }
            /*if (key.equals(PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED)) {
                Preference preferencePermanentRun = prefMng.findPreference(PREF_EVENT_NOTIFICATION_PERMANENT_RUN);
                Preference preferenceDuration = prefMng.findPreference(PREF_EVENT_NOTIFICATION_DURATION);
                if (preferencePermanentRun != null)
                    preferencePermanentRun.setEnabled(value.equals("false"));
                if (preferenceDuration != null)
                    preferenceDuration.setEnabled(value.equals("false"));
            }*/
            /*if (key.equals(PREF_EVENT_NOTIFICATION_PERMANENT_RUN)) {
                Preference preferenceEndWhenRemoved = prefMng.findPreference(PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED);
                Preference preferenceDuration = prefMng.findPreference(PREF_EVENT_NOTIFICATION_DURATION);
                boolean endWhenRemoved = false;
                if (preferenceEndWhenRemoved != null) {
                    SharedPreferences preferences = prefMng.getSharedPreferences();
                    endWhenRemoved = preferences.getBoolean(PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED, false);
                }
                if (preferenceDuration != null)
                    preferenceDuration.setEnabled(value.equals("false") && (!endWhenRemoved));
            }*/
        //}
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_NOTIFICATION_APPLICATIONS)/* ||
            key.equals(PREF_EVENT_NOTIFICATION_DURATION)*/)
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
        if (key.equals(PREF_EVENT_NOTIFICATION_IN_CALL) ||
            key.equals(PREF_EVENT_NOTIFICATION_MISSED_CALL)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
        /*if (key.equals(PREF_EVENT_NOTIFICATION_PERMANENT_RUN)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }*/
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_IN_CALL, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_MISSED_CALL, preferences, context);
        setSummary(prefMng, PREF_EVENT_NOTIFICATION_APPLICATIONS, preferences, context);
        //setSummary(prefMng, PREF_EVENT_NOTIFICATION_PERMANENT_RUN, preferences, context);
        //setSummary(prefMng, PREF_EVENT_NOTIFICATION_DURATION, preferences, context);
        //setSummary(prefMng, PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        if (Event.isEventPreferenceAllowed(PREF_EVENT_NOTIFICATION_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED) {
            EventPreferencesNotification tmp = new EventPreferencesNotification(this._event, this._enabled,
                                                        this._applications, this._inCall, this._missedCall/*,
                                                        this._permanentRun, this._duration, this._endWhenRemoved*/);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CATEGORY);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_CATEGORY);
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

        boolean runnable = super.isRunnable(context);

        runnable = runnable && (_inCall || _missedCall || (!_applications.isEmpty()));

        return runnable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            boolean enabled = PPNotificationListenerService.isNotificationListenerServiceEnabled(context);
            ApplicationsMultiSelectDialogPreference applicationsPreference = (ApplicationsMultiSelectDialogPreference)prefMng.findPreference(PREF_EVENT_NOTIFICATION_APPLICATIONS);
            Preference ringingCallPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_IN_CALL);
            Preference missedCallPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_MISSED_CALL);
            //Preference endWhenRemovedPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED);
            //Preference permanentRunPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_PERMANENT_RUN);
            //Preference durationPreference = prefMng.findPreference(PREF_EVENT_NOTIFICATION_DURATION);
            if (applicationsPreference != null) {
                applicationsPreference.setEnabled(enabled);
                applicationsPreference.setSummaryAMSDP();
            }
            if (ringingCallPreference != null) {
                ringingCallPreference.setEnabled(enabled);
            }
            if (missedCallPreference != null) {
                missedCallPreference.setEnabled(enabled);
            }
            /*if (endWhenRemovedPreference != null)
                endWhenRemovedPreference.setEnabled(enabled);
            if (permanentRunPreference != null)
                permanentRunPreference.setEnabled(enabled);*/

            SharedPreferences preferences = prefMng.getSharedPreferences();
            /*if (preferences != null) {
                boolean endWhenRemoved = preferences.getBoolean(PREF_EVENT_NOTIFICATION_END_WHEN_REMOVED, false);
                enabled = enabled && (!endWhenRemoved);
                boolean permanentRun = preferences.getBoolean(PREF_EVENT_NOTIFICATION_PERMANENT_RUN, false);
                enabled = enabled && (!permanentRun);
                if (durationPreference != null)
                    durationPreference.setEnabled(enabled);
            }*/
            setCategorySummary(prefMng, preferences, context);
        /*}
        else {
            PreferenceScreen preferenceScreen = (PreferenceScreen) prefMng.findPreference("eventPreferenceScreen");
            PreferenceScreen preferenceCategory = (PreferenceScreen) prefMng.findPreference("eventNotificationCategory");
            if ((preferenceCategory != null) && (preferenceScreen != null))
                preferenceScreen.removePreference(preferenceCategory);
        }*/
    }

    /*
    long computeAlarm()
    {
        PPApplication.logE("EventPreferencesNotification.computeAlarm","xxx");

        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        calEndTime.setTimeInMillis((_startTime - gmtOffset) + (_duration * 1000));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = calEndTime.getTimeInMillis();

        return alarmTime;
    }
    */

    /*
    @Override
    public void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        PPApplication.logE("EventPreferencesNotification.setSystemEventForStart","xxx");

        removeAlarm(context);
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        PPApplication.logE("EventPreferencesNotification.setSystemEventForPause","xxx");

        removeAlarm(context);

        if (!(isRunnable(context) && _enabled))
            return;

        setAlarm(computeAlarm(), context);
    }
    */

    /*
    @Override
    public void removeSystemEvent(Context context)
    {
        removeAlarm(context);

        PPApplication.logE("EventPreferencesNotification.removeSystemEvent", "xxx");
    }

    private void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(context, NotificationEventEndBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                PPApplication.logE("EventPreferencesNotification.removeAlarm", "alarm found");

                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    private void setAlarm(long alarmTime, Context context)
    {
        if (!_endWhenRemoved && !_permanentRun) {
            if (_startTime > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                String result = sdf.format(alarmTime);
                PPApplication.logE("EventPreferencesNotification.setAlarm", "endTime=" + result);

                Intent intent = new Intent(context, NotificationEventEndBroadcastReceiver.class);
                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    else if (android.os.Build.VERSION.SDK_INT >= 19)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    else
                        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                }
            }
        }
    }
    */

    // search if any configured package names are visible in status bar
    boolean isNotificationVisible(DataWrapper dataWrapper) {
        // get all saved notifications
        PPNotificationListenerService.getNotifiedPackages(dataWrapper.context);

        // com.android.incallui - in call
        // com.samsung.android.incallui - in call
        // com.google.android.dialer - in call
        // com.android.server.telecom - missed call

        if (this._inCall) {
            // Nexus/Pixel??? stock ROM
            PostedNotificationData notification = PPNotificationListenerService.getNotificationPosted("com.google.android.dialer", false);
            if (notification != null)
                return true;
            // Samsung, MIUI, Sony
            notification = PPNotificationListenerService.getNotificationPosted("android.incallui", true);
            if (notification != null)
                return true;
        }
        if (this._missedCall) {
            // Samsung, MIUI, Nexus/Pixel??? stock ROM, Sony
            PostedNotificationData notification = PPNotificationListenerService.getNotificationPosted("com.android.server.telecom", false);
            if (notification != null)
                return true;
            // LG
            notification = PPNotificationListenerService.getNotificationPosted("com.android.phone", false);
            if (notification != null)
                return true;
        }

        String[] splits = this._applications.split("\\|");
        for (String split : splits) {
            // get only package name = remove activity
            String packageName = ApplicationsCache.getPackageName(split);
            // search for package name in saved package names
            PostedNotificationData notification = PPNotificationListenerService.getNotificationPosted(packageName, false);
            if (notification != null)
                return true;
        }
        return false;
    }

    /*
    void saveStartTime(DataWrapper dataWrapper) {
        PPApplication.logE("EventPreferencesNotification.saveStartTime", "startTime="+this._startTime);
        if (this._startTime == 0) {
            // alarm for end is not set

            PPApplication.logE("EventPreferencesNotification.saveStartTime", "this._applications=" + this._applications);

            // get all saved notifications
            PPNotificationListenerService.getNotifiedPackages(dataWrapper.context);

            boolean notificationFound = false;
            PostedNotificationData notification = null;

            String[] splits = this._applications.split("\\|");
            for (String split : splits) {
                // get only package name = remove activity
                String packageName = ApplicationsCache.getPackageName(split);

                PPApplication.logE("EventPreferencesNotification.saveStartTime", "packageName=" + packageName);

                // search for package name in saved package names
                notification = PPNotificationListenerService.getNotificationPosted(packageName);
                PPApplication.logE("EventPreferencesNotification.saveStartTime", "notification=" + notification);
                if (notification != null) {
                    notificationFound = true;
                    break;
                }
            }

            if (notificationFound)
                _startTime = notification.time;
            else
                _startTime = 0;

            dataWrapper.getDatabaseHandler().updateNotificationStartTime(_event);

            if (notificationFound) {
                if (_event.getStatus() == Event.ESTATUS_RUNNING)
                    setSystemEventForPause(dataWrapper.context);
            }
        }
    }
    */

}
