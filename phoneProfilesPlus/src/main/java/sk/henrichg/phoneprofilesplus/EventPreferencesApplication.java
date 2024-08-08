package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.format.DateFormat;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.sql.Date;
//import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
//import android.preference.CheckBoxPreference;
//import android.preference.Preference;
//import android.preference.PreferenceManager;

/** @noinspection ExtractMethodRecommender*/
class EventPreferencesApplication extends EventPreferences {

    String _applications;
    long _startTime;
    int _duration;

    static final String PREF_EVENT_APPLICATION_ENABLED = "eventApplicationEnabled";
    private static final String PREF_EVENT_APPLICATION_APPLICATIONS = "eventApplicationApplications";
    private static final String PREF_EVENT_APPLICATION_DURATION = "eventApplicationDuration";
    private static final String PREF_EVENT_APPLICATION_EXTENDER = "eventApplicationExtender";
    //static final String PREF_EVENT_APPLICATION_INSTALL_EXTENDER = "eventApplicationInstallExtender";
    //static final String PREF_EVENT_APPLICATION_ACCESSIBILITY_SETTINGS = "eventApplicationAccessibilitySettings";
    //static final String PREF_EVENT_APPLICATION_LAUNCH_EXTENDER = "eventApplicationLaunchExtender";

    static final String PREF_EVENT_APPLICATION_CATEGORY = "eventApplicationCategoryRoot";

    EventPreferencesApplication(Event event,
                                       boolean enabled,
                                       String applications,
                                       int duration)
    {
        super(event, enabled);

        this._applications = applications;
        this._duration = duration;

        this._startTime = 0;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesApplication._enabled;
        this._applications = fromEvent._eventPreferencesApplication._applications;
        this._duration = fromEvent._eventPreferencesApplication._duration;
        this.setSensorPassed(fromEvent._eventPreferencesApplication.getSensorPassed());

        this._startTime = 0;
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Editor editor = preferences.edit();
            editor.putBoolean(PREF_EVENT_APPLICATION_ENABLED, _enabled);
            editor.putString(PREF_EVENT_APPLICATION_APPLICATIONS, this._applications);
            editor.putInt(PREF_EVENT_APPLICATION_DURATION, this._duration);
            editor.apply();
        //}
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this._enabled = preferences.getBoolean(PREF_EVENT_APPLICATION_ENABLED, false);
            this._applications = preferences.getString(PREF_EVENT_APPLICATION_APPLICATIONS, "");
            this._duration = preferences.getInt(PREF_EVENT_APPLICATION_DURATION, 0);
        //}
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_application_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_APPLICATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_applications), addPassStatus, DatabaseHandler.ETYPE_APPLICATION, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                String selectedApplications = context.getString(R.string.applications_multiselect_summary_text_not_selected);
                int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext());
                if (extenderVersion == 0) {
                    selectedApplications = context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_not_extender_installed);
                } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED) {
                    selectedApplications = context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else if (!PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context.getApplicationContext(), false, true
                        /*, "EventPreferencesApplication.getPreferencesDescription"*/)) {
                    selectedApplications = context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                } else if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                    selectedApplications = context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + context.getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined);
                }
                else
                if (!this._applications.isEmpty() && !this._applications.equals("-")) {
                    String[] splits = this._applications.split(StringConstants.STR_SPLIT_REGEX);
                    if (splits.length == 1) {
                        String packageName = Application.getPackageName(splits[0]);
                        String activityName = Application.getActivityName(splits[0]);
                        PackageManager packageManager = context.getPackageManager();
                        if (activityName.isEmpty()) {
                            ApplicationInfo app;
                            try {
                                app = packageManager.getApplicationInfo(packageName, PackageManager.MATCH_ALL);
                                if (app != null)
                                    selectedApplications = packageManager.getApplicationLabel(app).toString();
                            } catch (Exception e) {
                                selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + StringConstants.STR_COLON_WITH_SPACE + splits.length;
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.setClassName(packageName, activityName);
                            ActivityInfo info = intent.resolveActivityInfo(packageManager, 0);
                            if (info != null)
                                selectedApplications = info.loadLabel(packageManager).toString();
                        }
                    } else
                        selectedApplications = context.getString(R.string.applications_multiselect_summary_text_selected) + StringConstants.STR_COLON_WITH_SPACE + splits.length;
                }
                _value.append(context.getString(R.string.event_preferences_applications_applications)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(selectedApplications, disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);

                _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_applications_duration)).append(StringConstants.STR_COLON_WITH_SPACE);
                if (this._duration == 0)
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.event_preferences_applications_duration_unlimited), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, this._duration / 60);
                    calendar.set(Calendar.MINUTE, this._duration % 60);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(
                            DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis())),
                                disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                }

                //descr = descr + context.getString(R.string.pref_event_duration) + ": " +tmp._duration;
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key/*, String value*/, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_APPLICATION_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        /*
        if (key.equals(PREF_EVENT_APPLICATION_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0) {
                    String summary = context.getString(R.string.profile_preferences_PPPExtender_not_installed_summary);// +
                            //"\n\n" + context.getString(R.string.event_preferences_applications_PPPExtender_install_summary);
                    preference.setSummary(summary);
                }
                else {
                    String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(context);
                    String summary = context.getString(R.string.profile_preferences_PPPExtender_installed_summary) +
                            " " + extenderVersionName + " (" + extenderVersion + ")\n\n";
                    if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)
                        summary = summary + context.getString(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                    else
                        summary = summary + context.getString(R.string.pppextender_pref_dialog_PPPExtender_upgrade_summary);
                    preference.setSummary(summary);
                }
            }
        }
        */

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesApplication.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesApplication.isRunnable(context);
        //boolean isAllConfigured = event._eventPreferencesApplication.isAllConfigured(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_APPLICATION_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_APPLICATION_APPLICATIONS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_APPLICATION_APPLICATIONS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_APPLICATION_DURATION);
        if (preference != null) {
            boolean bold = prefMng.getSharedPreferences().getInt(PREF_EVENT_APPLICATION_DURATION, 0) != 0;
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, false, false, false);
        }
        int _isAccessibilityEnabled = event._eventPreferencesApplication.isAccessibilityServiceEnabled(context, false);
        boolean isAccessibilityEnabled = _isAccessibilityEnabled == 1;

        ExtenderDialogPreference extenderPreference = prefMng.findPreference(PREF_EVENT_APPLICATION_EXTENDER);
        if (extenderPreference != null) {
            extenderPreference.setSummaryEDP();
            GlobalGUIRoutines.setPreferenceTitleStyleX(extenderPreference, enabled, false, false, true,
                    !(isAccessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1)), true);
        }
        /*
        preference = prefMng.findPreference(PREF_EVENT_APPLICATION_ACCESSIBILITY_SETTINGS);
        if (preference != null) {

            String summary;
            if (isAccessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1))
                summary = context.getString(R.string.accessibility_service_enabled);
            else {
                if (_isAccessibilityEnabled == -1) {
                    summary = context.getString(R.string.accessibility_service_not_used);
                    summary = summary + "\n\n" + context.getString(R.string.preference_not_used_extender_reason) + " " +
                            context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else {
                    summary = context.getString(R.string.accessibility_service_disabled);
                    summary = summary + "\n\n" + context.getString(R.string.event_preferences_applications_AccessibilitySettingsForExtender_summary);
                }
            }
            preference.setSummary(summary);

            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true,
                    !(isAccessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1)), true);
        }
        */
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_APPLICATION_ENABLED)) {
            //boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, /*value ? "true" : "false",*/ context);
        }
        if (key.equals(PREF_EVENT_APPLICATION_APPLICATIONS) ||
            key.equals(PREF_EVENT_APPLICATION_DURATION) ||
            key.equals(PREF_EVENT_APPLICATION_EXTENDER))
            //key.equals(PREF_EVENT_APPLICATION_INSTALL_ EXTENDER))
        {
            setSummary(prefMng, key, /*preferences.getString(key, ""),*/ context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_APPLICATION_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_APPLICATION_APPLICATIONS, preferences, context);
        setSummary(prefMng, PREF_EVENT_APPLICATION_DURATION, preferences, context);
        setSummary(prefMng, PREF_EVENT_APPLICATION_EXTENDER, preferences, context);
        //setSummary(prefMng, PREF_EVENT_APPLICATION_INSTALL_EXTENDER, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_APPLICATION_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesApplication tmp = new EventPreferencesApplication(this._event, this._enabled, this._applications, this._duration);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_APPLICATION_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_APPLICATION_ENABLED, false);
                boolean runnable = tmp.isRunnable(context) && tmp.isAllConfigured(context) &&
                        (tmp.isAccessibilityServiceEnabled(context, false) == 1) &&
                        (PPApplication.accessibilityServiceForPPPExtenderConnected == 1);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_APPLICATION).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(runnable && permissionGranted), true);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_APPLICATION_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        StringConstants.STR_COLON_WITH_SPACE+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context)
    {
        boolean runnable = super.isRunnable(context);

        runnable = runnable && (!_applications.isEmpty());

        return runnable;
    }

    @Override
    int isAccessibilityServiceEnabled(Context context, boolean againCheckInDelay)
    {
        int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
        if (extenderVersion == 0)
            return -2;
        if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_REQUIRED)
            return -1;
        if ((_event.getStatus() != Event.ESTATUS_STOP) && this._enabled && isRunnable(context) && isAllConfigured(context)) {
            if (PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, againCheckInDelay, true
                        /*, "EventPreferencesApplication.isAccessibilityServiceEnabled"*/))
                return 1;
        } else
            return 1;
        return 0;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_APPLICATION_ENABLED) != null) {
                final boolean accessibilityEnabled =
                        PPExtenderBroadcastReceiver.isEnabled(context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_REQUIRED, true, false
                                /*, "EventPreferencesApplication.checkPreferences"*/);
                ApplicationsMultiSelectDialogPreference applicationsPreference = prefMng.findPreference(PREF_EVENT_APPLICATION_APPLICATIONS);
                if (applicationsPreference != null) {
                    //applicationsPreference.setEnabled(accessibilityEnabled);
                    applicationsPreference.setSummaryAMSDP();
                }

                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_APPLICATION_ENABLED, false);
                Preference preference = prefMng.findPreference(PREF_EVENT_APPLICATION_EXTENDER);
                if (preference != null)
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true, !accessibilityEnabled, true);

//                preference = prefMng.findPreference(PREF_EVENT_APPLICATION_ACCESSIBILITY_SETTINGS);
//                if (preference != null)
//                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true, !accessibilityEnabled, true);

                setSummary(prefMng, PREF_EVENT_APPLICATION_ENABLED, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    private long computeAlarm()
    {
        if (_duration > 0) {
            Calendar calEndTime = Calendar.getInstance();

            int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

            // _duration is in minutes
            calEndTime.setTimeInMillis((_startTime - gmtOffset) + (_duration * 60L * 1000L));
            //calEndTime.set(Calendar.SECOND, 0);
            //calEndTime.set(Calendar.MILLISECOND, 0);

            long alarmTime;
            alarmTime = calEndTime.getTimeInMillis();

            return alarmTime;
        } else
            return 0;
    }

    @Override
    void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        removeAlarm(context);
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        removeAlarm(context);

        if (!(isRunnable(context) && isAllConfigured(context) && _enabled))
            return;

        setAlarm(computeAlarm(), context);
    }

    @Override
    void removeSystemEvent(Context context)
    {
        removeAlarm(context);
    }

    void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, ApplicationEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_APPLICATION_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, NFCEventEndBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

    private void setAlarm(long alarmTime, Context context)
    {
        if (_duration > 0) {
            if (_startTime > 0) {
                //Intent intent = new Intent(context, ApplicationEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_APPLICATION_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, ApplicationEventEndBroadcastReceiver.class);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                //SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
                //String time = sdf.format(alarmTime);
                //Log.e("EventPreferencesApplication.setAlarm", "alarmTime="+time);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                    else {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    }
                }
            }
        }
    }

    void saveStartTime(DataWrapper dataWrapper, String _packageName, long startTime) {
        if (this._startTime == 0) {
            // alarm for end is not set

            boolean packageNameFound = false;

            String[] splits = this._applications.split(StringConstants.STR_SPLIT_REGEX);
            for (String split : splits) {
                String packageName = Application.getPackageName(split);
                if (packageName.equals(_packageName)) {
                    packageNameFound = true;
                    break;
                }
            }

            if (packageNameFound)
                this._startTime = startTime; //  + (10 * 1000);
            else
                this._startTime = 0;

            DatabaseHandler.getInstance(dataWrapper.context).updateApplicationStartTime(_event);

            if (packageNameFound) {
                //if (_event.getStatus() == Event.ESTATUS_RUNNING)
                setSystemEventForPause(dataWrapper.context);
            }
        }
    }

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                eventsHandler.applicationPassed = false;

                if (PPExtenderBroadcastReceiver.isEnabled(eventsHandler.context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_REQUIRED, true, true
                        /*, "EventPreferencesApplication.doHandleEvent"*/)) {
                    String foregroundApplication = ApplicationPreferences.prefApplicationInForeground;

                    if (!foregroundApplication.isEmpty()) {
                        String[] splits = _applications.split(StringConstants.STR_SPLIT_REGEX);
                        for (String split : splits) {
                            String packageName = Application.getPackageName(split);

                            if (foregroundApplication.equals(packageName)) {
                                if (_startTime > 0) {
                                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                                    long startTime = _startTime - gmtOffset;

                                    // compute end datetime
                                    long endAlarmTime = computeAlarm();

                                    Calendar now = Calendar.getInstance();
                                    long nowAlarmTime = now.getTimeInMillis();

                                    if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_APPLICATION))
                                        eventsHandler.applicationPassed = true;
                                    else if (_duration != 0) {
                                        if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_APPLICATION_EVENT_END))
                                            eventsHandler.applicationPassed = false;
                                        else
                                            eventsHandler.applicationPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                                    } else
                                        eventsHandler.applicationPassed = nowAlarmTime >= startTime;
                                } else
                                    eventsHandler.applicationPassed = true;
                                break;
                            }
                        }
                    } else
                        eventsHandler.notAllowedApplication = true;
                } else
                    eventsHandler.notAllowedApplication = true;

                if (!eventsHandler.applicationPassed) {
                    _startTime = 0;
                    DatabaseHandler.getInstance(eventsHandler.context).updateApplicationStartTime(_event);
                }

                if (!eventsHandler.notAllowedApplication) {
                    if (eventsHandler.applicationPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedApplication = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_APPLICATION);
            }
        }
    }

}
