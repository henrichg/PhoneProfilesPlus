package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.PhoneNumberUtils;

import java.util.Calendar;
import java.util.List;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.WorkManager;

class EventPreferencesCall extends EventPreferences {

    int _callEvent;
    String _contacts;
    String _contactGroups;
    int _contactListType;
    long _startTime;
    boolean _permanentRun;
    int _duration;

    static final String PREF_EVENT_CALL_ENABLED = "eventCallEnabled";
    private static final String PREF_EVENT_CALL_EVENT = "eventCallEvent";
    static final String PREF_EVENT_CALL_CONTACTS = "eventCallContacts";
    static final String PREF_EVENT_CALL_CONTACT_GROUPS = "eventCallContactGroups";
    private static final String PREF_EVENT_CALL_CONTACT_LIST_TYPE = "eventCallContactListType";
    private static final String PREF_EVENT_CALL_PERMANENT_RUN = "eventCallPermanentRun";
    private static final String PREF_EVENT_CALL_DURATION = "eventCallDuration";
    static final String PREF_EVENT_CALL_INSTALL_EXTENDER = "eventCallInstallExtender";
    static final String PREF_EVENT_CALL_ACCESSIBILITY_SETTINGS = "eventCallAccessibilitySettings";
    static final String PREF_EVENT_CALL_LAUNCH_EXTENDER = "eventCallLaunchExtender";

    static final String PREF_EVENT_CALL_ENABLED_NO_CHECK_SIM = "eventCallEnabledEnabledNoCheckSim";

    private static final String PREF_EVENT_CALL_CATEGORY = "eventCallCategoryRoot";

    static final int CALL_EVENT_RINGING = 0;
    static final int CALL_EVENT_INCOMING_CALL_ANSWERED = 1;
    static final int CALL_EVENT_OUTGOING_CALL_STARTED = 2;
    static final int CALL_EVENT_MISSED_CALL = 3;
    static final int CALL_EVENT_INCOMING_CALL_ENDED = 4;
    static final int CALL_EVENT_OUTGOING_CALL_ENDED = 5;

    static final int CONTACT_LIST_TYPE_WHITE_LIST = 0;
    static final int CONTACT_LIST_TYPE_BLACK_LIST = 1;
    static final int CONTACT_LIST_TYPE_NOT_USE = 2;

    static final int PHONE_CALL_EVENT_UNDEFINED = 0;
    static final int PHONE_CALL_EVENT_INCOMING_CALL_RINGING = 1;
    //static final int PHONE_CALL_EVENT_OUTGOING_CALL_STARTED = 2;
    static final int PHONE_CALL_EVENT_INCOMING_CALL_ANSWERED = 3;
    static final int PHONE_CALL_EVENT_OUTGOING_CALL_ANSWERED = 4;
    static final int PHONE_CALL_EVENT_INCOMING_CALL_ENDED = 5;
    static final int PHONE_CALL_EVENT_OUTGOING_CALL_ENDED = 6;
    static final int PHONE_CALL_EVENT_MISSED_CALL = 7;
    static final int PHONE_CALL_EVENT_SERVICE_UNBIND = 8;

    private static final String PREF_EVENT_CALL_EVENT_TYPE = "eventCallEventType";
    private static final String PREF_EVENT_CALL_PHONE_NUMBER = "eventCallPhoneNumber";
    private static final String PREF_EVENT_CALL_EVENT_TIME = "eventCallEventTime";

    EventPreferencesCall(Event event,
                         boolean enabled,
                         int callEvent,
                         String contacts,
                         String contactGroups,
                         int contactListType,
                         boolean permanentRun,
                         int duration) {
        super(event, enabled);

        this._callEvent = callEvent;
        this._contacts = contacts;
        this._contactGroups = contactGroups;
        this._contactListType = contactListType;
        this._permanentRun = permanentRun;
        this._duration = duration;

        this._startTime = 0;
    }

    @Override
    public void copyPreferences(Event fromEvent) {
        this._enabled = fromEvent._eventPreferencesCall._enabled;
        this._callEvent = fromEvent._eventPreferencesCall._callEvent;
        this._contacts = fromEvent._eventPreferencesCall._contacts;
        this._contactGroups = fromEvent._eventPreferencesCall._contactGroups;
        this._contactListType = fromEvent._eventPreferencesCall._contactListType;
        this._permanentRun = fromEvent._eventPreferencesCall._permanentRun;
        this._duration = fromEvent._eventPreferencesCall._duration;
        this.setSensorPassed(fromEvent._eventPreferencesCall.getSensorPassed());

        this._startTime = 0;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences) {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_CALL_ENABLED, _enabled);
        editor.putString(PREF_EVENT_CALL_EVENT, String.valueOf(this._callEvent));
        editor.putString(PREF_EVENT_CALL_CONTACTS, this._contacts);
        editor.putString(PREF_EVENT_CALL_CONTACT_GROUPS, this._contactGroups);
        editor.putString(PREF_EVENT_CALL_CONTACT_LIST_TYPE, String.valueOf(this._contactListType));
        editor.putBoolean(PREF_EVENT_CALL_PERMANENT_RUN, this._permanentRun);
        editor.putString(PREF_EVENT_CALL_DURATION, String.valueOf(this._duration));
        editor.apply();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences) {
        this._enabled = preferences.getBoolean(PREF_EVENT_CALL_ENABLED, false);
        this._callEvent = Integer.parseInt(preferences.getString(PREF_EVENT_CALL_EVENT, "0"));
        this._contacts = preferences.getString(PREF_EVENT_CALL_CONTACTS, "");
        this._contactGroups = preferences.getString(PREF_EVENT_CALL_CONTACT_GROUPS, "");
        this._contactListType = Integer.parseInt(preferences.getString(PREF_EVENT_CALL_CONTACT_LIST_TYPE, "0"));
        this._permanentRun = preferences.getBoolean(PREF_EVENT_CALL_PERMANENT_RUN, false);
        this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_CALL_DURATION, "5"));
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context) {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_call_summary);
        } else {
            if (addBullet) {
                descr = descr + "<b>";
                descr = descr + getPassStatusString(context.getString(R.string.event_type_call), addPassStatus, DatabaseHandler.ETYPE_CALL, context);
                descr = descr + "</b> ";
            }

            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_CALL_ENABLED, context);
            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext());
                if (extenderVersion == 0) {
                    descr = descr + context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_not_extender_installed);
                } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0) {
                    descr = descr + context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context.getApplicationContext())) {
                    descr = descr + context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                } else {
                    descr = descr + context.getString(R.string.pref_event_call_event);
                    String[] callEvents = context.getResources().getStringArray(R.array.eventCallEventsArray);
                    descr = descr + ": <b>" + callEvents[this._callEvent] + "</b> • ";

                    descr = descr + context.getString(R.string.event_preferences_call_contact_groups) + ": ";
                    descr = descr + "<b>" + ContactGroupsMultiSelectDialogPreferenceX.getSummary(_contactGroups, context) + "</b> • ";

                    descr = descr + context.getString(R.string.event_preferences_call_contacts) + ": ";
                    descr = descr + "<b>" + ContactsMultiSelectDialogPreferenceX.getSummary(_contacts, false, context) + "</b> • ";

                    descr = descr + context.getString(R.string.event_preferences_contactListType);
                    String[] contactListTypes = context.getResources().getStringArray(R.array.eventCallContactListTypeArray);
                    descr = descr + ": <b>" + contactListTypes[this._contactListType] + "</b>";

                    if ((this._callEvent == CALL_EVENT_MISSED_CALL) ||
                            (this._callEvent == CALL_EVENT_INCOMING_CALL_ENDED) ||
                            (this._callEvent == CALL_EVENT_OUTGOING_CALL_ENDED)) {
                        if (this._permanentRun)
                            descr = descr + " • <b>" + context.getString(R.string.pref_event_permanentRun) + "</b>";
                        else
                            descr = descr + " • " + context.getString(R.string.pref_event_duration) + ": <b>" + GlobalGUIRoutines.getDurationString(this._duration) + "</b>";
                    }
                }
            }
            else {
                descr = descr + context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context);
            }
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();

        if (key.equals(PREF_EVENT_CALL_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_CALL_EVENT) || key.equals(PREF_EVENT_CALL_CONTACT_LIST_TYPE)) {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_CALL_EVENT)) {
            ListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                Preference preferenceDuration = prefMng.findPreference(PREF_EVENT_CALL_DURATION);
                Preference preferencePermanentRun = prefMng.findPreference(PREF_EVENT_CALL_PERMANENT_RUN);
                boolean enabledCall = value.equals(String.valueOf(CALL_EVENT_MISSED_CALL)) ||
                        value.equals(String.valueOf(CALL_EVENT_INCOMING_CALL_ENDED)) ||
                        value.equals(String.valueOf(CALL_EVENT_OUTGOING_CALL_ENDED));
                if (preferenceDuration != null) {
                    boolean enabled = enabledCall;
                    enabled = enabled && !preferences.getBoolean(PREF_EVENT_CALL_PERMANENT_RUN, false);
                    preferenceDuration.setEnabled(enabled);
                }
                if (preferencePermanentRun != null)
                    preferencePermanentRun.setEnabled(enabledCall);
            }
        }
        if (key.equals(PREF_EVENT_CALL_PERMANENT_RUN)) {
            SwitchPreferenceCompat permanentRunPreference = prefMng.findPreference(key);
            if (permanentRunPreference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(permanentRunPreference, true, preferences.getBoolean(key, false), false, false, false);
            }
            String callEvent = preferences.getString(PREF_EVENT_CALL_EVENT, "-1");
            if (!callEvent.equals(String.valueOf(CALL_EVENT_MISSED_CALL)) &&
                    !callEvent.equals(String.valueOf(CALL_EVENT_INCOMING_CALL_ENDED)) &&
                    !callEvent.equals(String.valueOf(CALL_EVENT_OUTGOING_CALL_ENDED))) {
                Preference preference = prefMng.findPreference(PREF_EVENT_CALL_DURATION);
                if (preference != null) {
                    preference.setEnabled(false);
                }
            } else {
                Preference preference = prefMng.findPreference(PREF_EVENT_CALL_DURATION);
                if (preference != null) {
                    preference.setEnabled(value.equals("false"));
                }
            }
        }
        if (key.equals(PREF_EVENT_CALL_DURATION)) {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 5;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 5, false, false, false);
        }
        if (key.equals(PREF_EVENT_CALL_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0)
                    preference.setSummary(R.string.event_preferences_call_PPPExtender_install_summary);
                else
                if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0)
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                else
                    preference.setSummary(R.string.event_preferences_applications_PPPExtender_upgrade_summary);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesCall.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesCall.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_CALL_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_CALL_CONTACT_GROUPS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_CALL_CONTACT_GROUPS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_CALL_CONTACTS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_CALL_CONTACTS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_CALL_CONTACT_LIST_TYPE);
        if (preference != null)
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, true, !isRunnable, false);
        boolean isAccessibilityEnabled = event._eventPreferencesCall.isAccessibilityServiceEnabled(context) == 1;
        preference = prefMng.findPreference(PREF_EVENT_CALL_ACCESSIBILITY_SETTINGS);
        if (preference != null)
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, true, !isAccessibilityEnabled, false);
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (key.equals(PREF_EVENT_CALL_ENABLED) ||
                key.equals(PREF_EVENT_CALL_PERMANENT_RUN)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true" : "false", context);
        }
        if (key.equals(PREF_EVENT_CALL_EVENT) ||
                key.equals(PREF_EVENT_CALL_CONTACT_LIST_TYPE) ||
                key.equals(PREF_EVENT_CALL_CONTACTS) ||
                key.equals(PREF_EVENT_CALL_CONTACT_GROUPS) ||
                key.equals(PREF_EVENT_CALL_DURATION) ||
                key.equals(PREF_EVENT_CALL_INSTALL_EXTENDER)) {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context) {
        setSummary(prefMng, PREF_EVENT_CALL_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_EVENT, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_CONTACT_LIST_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_CONTACTS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_CONTACT_GROUPS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_PERMANENT_RUN, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_DURATION, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_INSTALL_EXTENDER, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_INSTALL_EXTENDER, preferences, context);
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_CALL_ENABLED_NO_CHECK_SIM, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesCall tmp = new EventPreferencesCall(this._event, this._enabled, this._callEvent, this._contacts, this._contactGroups,
                    this._contactListType, this._permanentRun, this._duration);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_CALL_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_CALL_ENABLED, false);
                boolean runnable = tmp.isRunnable(context) && (tmp.isAccessibilityServiceEnabled(context) == 1);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !runnable, false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        } else {
            Preference preference = prefMng.findPreference(PREF_EVENT_CALL_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    public boolean isRunnable(Context context) {

        boolean runnable = super.isRunnable(context);

        runnable = runnable && ((_contactListType == CONTACT_LIST_TYPE_NOT_USE) ||
                (!(_contacts.isEmpty() && _contactGroups.isEmpty())));

        return runnable;
    }

    @Override
    public int isAccessibilityServiceEnabled(Context context)
    {
        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
        if (extenderVersion == 0)
            return -2;
        if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_3_0)
            return -1;
        if (PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context))
            return 1;
        return 0;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context) {
        final boolean accessibilityEnabled =
                PPPExtenderBroadcastReceiver.isEnabled(context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_3_0);

        SharedPreferences preferences = prefMng.getSharedPreferences();

        boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_CALL_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_CALL_ACCESSIBILITY_SETTINGS);
        if (preference != null)
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, true, !accessibilityEnabled, false);

        setCategorySummary(prefMng, preferences, context);
    }

    long computeAlarm() {
        //PPApplication.logE("EventPreferencesCall.computeAlarm", "xxx");

        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        calEndTime.setTimeInMillis((_startTime - gmtOffset) + (_duration * 1000));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = calEndTime.getTimeInMillis();

        return alarmTime;
    }

    @Override
    public void setSystemEventForStart(Context context) {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that will change state into RUNNING;
        // from broadcast will by called EventsHandler

        //PPApplication.logE("EventPreferencesCall.setSystemRunningEvent", "xxx");

        removeAlarm(context);
    }

    @Override
    public void setSystemEventForPause(Context context) {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that will change state into PAUSE;
        // from broadcast will by called EventsHandler

        //PPApplication.logE("EventPreferencesCall.setSystemPauseEvent", "xxx");

        removeAlarm(context);

        if (!(isRunnable(context) && _enabled))
            return;

        if ((_callEvent == CALL_EVENT_MISSED_CALL) ||
                (_callEvent == CALL_EVENT_INCOMING_CALL_ENDED) ||
                (_callEvent == CALL_EVENT_OUTGOING_CALL_ENDED))
            setAlarm(computeAlarm(), context);
    }

    @Override
    public void removeSystemEvent(Context context) {
        removeAlarm(context);

        //PPApplication.logE("EventPreferencesCall.removeSystemEvent", "xxx");
    }

    void removeAlarm(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, MissedCallEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_MISSED_CALL_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, MissedCallEventEndBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    //PPApplication.logE("EventPreferencesCall.removeAlarm", "alarm found");

                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception ignored) {}
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance(context);
            //workManager.cancelUniqueWork("elapsedAlarmsCallSensorWork_"+(int)_event._id);
            workManager.cancelAllWorkByTag("elapsedAlarmsCallSensorWork_"+(int)_event._id);
        } catch (Exception ignored) {}
    }

    @SuppressLint("NewApi")
    private void setAlarm(long alarmTime, Context context) {
        if (!_permanentRun) {
            if (_startTime > 0) {
                /*if (PPApplication.logEnabled()) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String result = sdf.format(alarmTime);
                    PPApplication.logE("EventPreferencesCall.setAlarm", "endTime=" + result);
                }*/

                /*if (ApplicationPreferences.applicationUseAlarmClock(context)) {
                    //Intent intent = new Intent(context, MissedCallEventEndBroadcastReceiver.class);
                    Intent intent = new Intent();
                    intent.setAction(PhoneProfilesService.ACTION_MISSED_CALL_EVENT_END_BROADCAST_RECEIVER);
                    //intent.setClass(context, MissedCallEventEndBroadcastReceiver.class);

                    //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null) {
                        Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                }
                else {
                    Calendar now = Calendar.getInstance();
                    long elapsedTime = (alarmTime + Event.EVENT_ALARM_TIME_OFFSET) - now.getTimeInMillis();

                    if (PPApplication.logEnabled()) {
                        long allSeconds = elapsedTime / 1000;
                        long hours = allSeconds / 60 / 60;
                        long minutes = (allSeconds - (hours * 60 * 60)) / 60;
                        long seconds = allSeconds % 60;

                        PPApplication.logE("EventPreferencesCall.setAlarm", "elapsedTime=" + hours + ":" + minutes + ":" + seconds);
                    }

                    Data workData = new Data.Builder()
                            .putString(PhoneProfilesService.EXTRA_ELAPSED_ALARMS_WORK, ElapsedAlarmsWorker.ELAPSED_ALARMS_CALL_SENSOR)
                            .build();

                    OneTimeWorkRequest worker =
                            new OneTimeWorkRequest.Builder(ElapsedAlarmsWorker.class)
                                    .addTag("elapsedAlarmsCallSensorWork_"+(int)_event._id)
                                    .setInputData(workData)
                                    .setInitialDelay(elapsedTime, TimeUnit.MILLISECONDS)
                                    .build();
                    try {
                        WorkManager workManager = WorkManager.getInstance(context);
                        PPApplication.logE("[HANDLER] EventPreferencesCall.setAlarm", "enqueueUniqueWork - elapsedTime="+elapsedTime);
                        //workManager.enqueueUniqueWork("elapsedAlarmsCallSensorWork_"+(int)_event._id, ExistingWorkPolicy.REPLACE, worker);
                        workManager.enqueue(worker);
                    } catch (Exception ignored) {}
                }*/

                //Intent intent = new Intent(context, MissedCallEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_MISSED_CALL_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, MissedCallEventEndBroadcastReceiver.class);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                    else {
                        if (android.os.Build.VERSION.SDK_INT >= 23)
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                        else //if (android.os.Build.VERSION.SDK_INT >= 19)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                        //else
                        //    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    }
                }
            }
        }
    }

    boolean isPhoneNumberConfigured(String phoneNumber/*, DataWrapper dataWrapper*/) {
        boolean phoneNumberFound = false;

        if (this._contactListType != EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) {
            // find phone number in groups
            String[] splits = this._contactGroups.split("\\|");
            for (String split : splits) {
                /*String[] projection = new String[]{ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID};
                String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=? AND "
                        + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                        + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
                String[] selectionArgs = new String[]{split};
                Cursor mCursor = dataWrapper.context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);
                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        String contactId = mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                        String[] projection2 = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                        String selection2 = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?" + " and " +
                                ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "=1";
                        String[] selection2Args = new String[]{contactId};
                        Cursor phones = dataWrapper.context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection2, selection2, selection2Args, null);
                        if (phones != null) {
                            while (phones.moveToNext()) {
                                String _phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                    phoneNumberFound = true;
                                    break;
                                }
                            }
                            phones.close();
                        }
                        if (phoneNumberFound)
                            break;
                    }
                    mCursor.close();
                }*/

                if (!split.isEmpty()) {
                    //Log.e("EventPreferencesCall.isPhoneNumberConfigured", "split=" + split);

                    ContactsCache contactsCache = PhoneProfilesService.getContactsCache();
                    if (contactsCache == null)
                        return false;

                    synchronized (PPApplication.contactsCacheMutex) {
                        List<Contact> contactList = contactsCache.getList(/*false*/);
                        if (contactList != null) {
                            for (Contact contact : contactList) {
                            /*String __phoneNumber = contact.phoneNumber;
                            boolean found = false;
                            if (PhoneNumberUtils.compare(__phoneNumber, "917994279")) {
                                found = true;
                                Log.e("EventPreferencesCall.isPhoneNumberConfigured", "_phoneNumber=" + __phoneNumber);
                                Log.e("EventPreferencesCall.isPhoneNumberConfigured", "contact.contactId=" + contact.contactId);
                                Log.e("EventPreferencesCall.isPhoneNumberConfigured", "contact.groups=" + contact.groups);
                            }*/

                                if (contact.groups != null) {
                                    long groupId = contact.groups.indexOf(Long.valueOf(split));
                                    if (groupId != -1) {
                                        // group found in contact
                                        //if (found)
                                        //    Log.e("EventPreferencesCall.isPhoneNumberConfigured", "groupId="+groupId);
                                        if (contact.phoneId != 0) {
                                            String _phoneNumber = contact.phoneNumber;
                                            if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                                phoneNumberFound = true;
                                                //if (found)
                                                //    Log.e("EventPreferencesCall.isPhoneNumberConfigured", "phoneNumberFound="+phoneNumberFound);
                                                break;
                                            }
                                        }
                                    }
                                }
                                //else
                                //    Log.e("EventPreferencesCall.isPhoneNumberConfigured", "group is null");
                            }
                        }
                    }
                }

                if (phoneNumberFound)
                    break;
            }

            if (!phoneNumberFound) {
                // find phone number in contacts
                splits = this._contacts.split("\\|");
                for (String split : splits) {
                    String[] splits2 = split.split("#");

                    /*// get phone number from contacts
                    String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER};
                    String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1' and " + ContactsContract.Contacts._ID + "=?";
                    String[] selectionArgs = new String[]{splits2[0]};
                    Cursor mCursor = dataWrapper.context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, selectionArgs, null);
                    if (mCursor != null) {
                        while (mCursor.moveToNext()) {
                            String[] projection2 = new String[]{ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.NUMBER};
                            String selection2 = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?" + " and " + ContactsContract.CommonDataKinds.Phone._ID + "=?";
                            String[] selection2Args = new String[]{splits2[0], splits2[1]};
                            Cursor phones = dataWrapper.context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection2, selection2, selection2Args, null);
                            if (phones != null) {
                                while (phones.moveToNext()) {
                                    String _phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                        phoneNumberFound = true;
                                        break;
                                    }
                                }
                                phones.close();
                            }
                            if (phoneNumberFound)
                                break;
                        }
                        mCursor.close();
                    }*/

                    if ((!split.isEmpty()) && (!splits2[0].isEmpty()) && (!splits2[1].isEmpty())) {
                        ContactsCache contactsCache = PhoneProfilesService.getContactsCache();
                        if (contactsCache == null)
                            return false;

                        synchronized (PPApplication.contactsCacheMutex) {
                            List<Contact> contactList = contactsCache.getList(/*false*/);
                            if (contactList != null) {
                                for (Contact contact : contactList) {
                                    if (contact.phoneId != 0) {
                                        if ((contact.contactId == Long.parseLong(splits2[0])) && contact.phoneId == Long.parseLong(splits2[1])) {
                                            String _phoneNumber = contact.phoneNumber;
                                            if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                                phoneNumberFound = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (phoneNumberFound)
                        break;
                }
            }

            if (this._contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_BLACK_LIST)
                phoneNumberFound = !phoneNumberFound;
        } else
            phoneNumberFound = true;

        return phoneNumberFound;
    }

    void saveStartTime(DataWrapper dataWrapper) {
        //PPApplication.logE("EventPreferencesCall.saveStartTime", "_startTime=" + _startTime);
        if (this._startTime == 0) {
            // alarm for end is not set
            if (Permissions.checkContacts(dataWrapper.context)) {
                //PPApplication.logE("EventPreferencesCall.saveStartTime", "contacts permission granted");

                int callEventType = ApplicationPreferences.prefEventCallEventType;
                long callTime = ApplicationPreferences.prefEventCallEventTime;
                String phoneNumber = ApplicationPreferences.prefEventCallPhoneNumber;
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("EventPreferencesCall.saveStartTime", "callEventType=" + callEventType);
                    PPApplication.logE("EventPreferencesCall.saveStartTime", "callTime=" + callTime);
                    PPApplication.logE("EventPreferencesCall.saveStartTime", "phoneNumber=" + phoneNumber);
                }*/

                if (((_callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL) && (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_MISSED_CALL)) ||
                    ((_callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ENDED) && (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ENDED)) ||
                    ((_callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_ENDED) && (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_OUTGOING_CALL_ENDED))) {

                    boolean phoneNumberFound = isPhoneNumberConfigured(phoneNumber/*, dataWrapper*/);

                    if (phoneNumberFound)
                        this._startTime = callTime; // + (10 * 1000);
                    else
                        this._startTime = 0;
                    //PPApplication.logE("EventPreferencesCall.saveStartTime", "_startTime=" + _startTime);

                    DatabaseHandler.getInstance(dataWrapper.context).updateCallStartTime(_event);

                    if (phoneNumberFound) {
                        //if (_event.getStatus() == Event.ESTATUS_RUNNING)
                            setSystemEventForPause(dataWrapper.context);
                    }
                }// else {
                //    PPApplication.logE("EventPreferencesCall.saveStartTime", "_startTime NOT set");
                //
                //    _startTime = 0;
                //    DatabaseHandler.getInstance(dataWrapper.context).updateCallStartTime(_event);
                //}
            } else {
                //PPApplication.logE("EventPreferencesCall.saveStartTime", "contacts permission NOT granted");

                _startTime = 0;
                DatabaseHandler.getInstance(dataWrapper.context).updateCallStartTime(_event);
            }
        }
    }

    static void getEventCallEventType(Context context) {
        synchronized (PPApplication.eventCallSensorMutex) {
            ApplicationPreferences.prefEventCallEventType = ApplicationPreferences.
                    getSharedPreferences(context).getInt(EventPreferencesCall.PREF_EVENT_CALL_EVENT_TYPE, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);
            //return ApplicationPreferences.prefEventCallEventType;
        }
    }
    static void setEventCallEventType(Context context, int type) {
        synchronized (PPApplication.eventCallSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(EventPreferencesCall.PREF_EVENT_CALL_EVENT_TYPE, type);
            editor.apply();
            ApplicationPreferences.prefEventCallEventType = type;
        }
    }

    static void getEventCallEventTime(Context context) {
        synchronized (PPApplication.eventCallSensorMutex) {
            ApplicationPreferences.prefEventCallEventTime = ApplicationPreferences.
                    getSharedPreferences(context).getLong(EventPreferencesCall.PREF_EVENT_CALL_EVENT_TIME, 0);
            //return ApplicationPreferences.prefEventCallEventTime;
        }
    }
    static void setEventCallEventTime(Context context, long time) {
        synchronized (PPApplication.eventCallSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putLong(EventPreferencesCall.PREF_EVENT_CALL_EVENT_TIME, time);
            editor.apply();
            ApplicationPreferences.prefEventCallEventTime = time;
        }
    }

    static void getEventCallPhoneNumber(Context context) {
        synchronized (PPApplication.eventCallSensorMutex) {
            ApplicationPreferences.prefEventCallPhoneNumber = ApplicationPreferences.
                    getSharedPreferences(context).getString(EventPreferencesCall.PREF_EVENT_CALL_PHONE_NUMBER, "");
            //return ApplicationPreferences.prefEventCallPhoneNumber;
        }
    }
    static void setEventCallPhoneNumber(Context context, String phoneNumber) {
        synchronized (PPApplication.eventCallSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(EventPreferencesCall.PREF_EVENT_CALL_PHONE_NUMBER, phoneNumber);
            editor.apply();
            ApplicationPreferences.prefEventCallPhoneNumber = phoneNumber;
        }
    }

}
