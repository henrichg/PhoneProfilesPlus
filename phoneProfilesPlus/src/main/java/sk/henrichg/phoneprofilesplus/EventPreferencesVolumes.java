package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesVolumes extends EventPreferences {

    static boolean internalChange = false;

    String _volumeRingtone;
    String _volumeNotification;
    String _volumeMedia;
    String _volumeAlarm;
    String _volumeSystem;
    String _volumeVoice;
    String _volumeBluetoothSCO;
    String _volumeAccessibility;

    static final String PREF_EVENT_VOLUMES_ENABLED = "eventVolumesEnabled";
    static final String PREF_EVENT_VOLUMES_RINGTONE = "eventVolumesRingtone";
    static final String PREF_EVENT_VOLUMES_NOTIFICATION = "eventVolumesNotification";
    static final String PREF_EVENT_VOLUMES_MEDIA = "eventVolumesMedia";
    static final String PREF_EVENT_VOLUMES_ALARM = "eventVolumesAlarm";
    static final String PREF_EVENT_VOLUMES_SYSTEM = "eventVolumesSystem";
    static final String PREF_EVENT_VOLUMES_VOICE = "eventVolumesVoice";
    static final String PREF_EVENT_VOLUMES_BLUETOOTHSCO = "eventVolumesBluetoothSCO";
    static final String PREF_EVENT_VOLUMES_ACCESSIBILITY = "eventVolumesAccessibility";

    private static final String PREF_EVENT_VOLUMES_CATEGORY = "eventVolumesCategoryRoot";

    EventPreferencesVolumes(Event event,
                            boolean enabled,
                            String volumeRingtone,
                            String volumeNotification,
                            String volumeMedia,
                            String volumeAlarm,
                            String volumeSystem,
                            String volumeVoice,
                            String volumeBluetoothSCO,
                            String volumeAccessibility
                            )
    {
        super(event, enabled);

        this._volumeRingtone = volumeRingtone;
        this._volumeNotification = volumeNotification;
        this._volumeMedia = volumeMedia;
        this._volumeAlarm = volumeAlarm;
        this._volumeSystem = volumeSystem;
        this._volumeVoice = volumeVoice;
        this._volumeBluetoothSCO = volumeBluetoothSCO;
        this._volumeAccessibility = volumeAccessibility;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesVolumes._enabled;
        this._volumeRingtone = fromEvent._eventPreferencesVolumes._volumeRingtone;
        this._volumeNotification = fromEvent._eventPreferencesVolumes._volumeNotification;
        this._volumeMedia = fromEvent._eventPreferencesVolumes._volumeMedia;
        this._volumeAlarm = fromEvent._eventPreferencesVolumes._volumeAlarm;
        this._volumeSystem = fromEvent._eventPreferencesVolumes._volumeSystem;
        this._volumeVoice = fromEvent._eventPreferencesVolumes._volumeVoice;
        this._volumeBluetoothSCO = fromEvent._eventPreferencesVolumes._volumeBluetoothSCO;
        this._volumeAccessibility = fromEvent._eventPreferencesVolumes._volumeAccessibility;
        this.setSensorPassed(fromEvent._eventPreferencesVolumes.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_VOLUMES_ENABLED, _enabled);
        editor.putString(PREF_EVENT_VOLUMES_RINGTONE, this._volumeRingtone);
        editor.putString(PREF_EVENT_VOLUMES_NOTIFICATION, this._volumeNotification);
        editor.putString(PREF_EVENT_VOLUMES_MEDIA, this._volumeMedia);
        editor.putString(PREF_EVENT_VOLUMES_ALARM, this._volumeAlarm);
        editor.putString(PREF_EVENT_VOLUMES_SYSTEM, this._volumeSystem);
        editor.putString(PREF_EVENT_VOLUMES_VOICE, this._volumeVoice);
        editor.putString(PREF_EVENT_VOLUMES_BLUETOOTHSCO, this._volumeBluetoothSCO);
        editor.putString(PREF_EVENT_VOLUMES_ACCESSIBILITY, this._volumeAccessibility);
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_VOLUMES_ENABLED, false);
        this._volumeRingtone = preferences.getString(PREF_EVENT_VOLUMES_RINGTONE, "-1|0|0");
        this._volumeNotification = preferences.getString(PREF_EVENT_VOLUMES_NOTIFICATION, "-1|0|0");
        this._volumeMedia = preferences.getString(PREF_EVENT_VOLUMES_MEDIA, "-1|0|0");
        this._volumeAlarm = preferences.getString(PREF_EVENT_VOLUMES_ALARM, "-1|0|0");
        this._volumeSystem = preferences.getString(PREF_EVENT_VOLUMES_SYSTEM, "-1|0|0");
        this._volumeVoice = preferences.getString(PREF_EVENT_VOLUMES_VOICE, "-1|0|0");
        this._volumeBluetoothSCO = preferences.getString(PREF_EVENT_VOLUMES_BLUETOOTHSCO, "-1|0|0");
        this._volumeAccessibility = preferences.getString(PREF_EVENT_VOLUMES_ACCESSIBILITY, "-1|0|0");
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_volumes_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_VOLUMES_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_volumes), addPassStatus, DatabaseHandler.ETYPE_VOLUMES, context);
                    descr = descr + "</b> ";
                }

                boolean _addBullet = false;

                int operator = 0;
                String[] splits = this._volumeRingtone.split("\\|");
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    descr = descr + context.getString(R.string.profile_preferences_volumeRingtone) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    descr = descr + "<b>" + fields[operator] + "</b>";
                    _addBullet = true;
                }

                operator = 0;
                splits = this._volumeNotification.split("\\|");
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.profile_preferences_volumeNotification) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    descr = descr + "<b>" + fields[operator] + "</b>";
                    _addBullet = true;
                }

                operator = 0;
                splits = this._volumeMedia.split("\\|");
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.profile_preferences_volumeMedia) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    descr = descr + "<b>" + fields[operator] + "</b>";
                    _addBullet = true;
                }

                operator = 0;
                splits = this._volumeAlarm.split("\\|");
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.profile_preferences_volumeAlarm) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    descr = descr + "<b>" + fields[operator] + "</b>";
                    _addBullet = true;
                }

                operator = 0;
                splits = this._volumeSystem.split("\\|");
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.profile_preferences_volumeSystem) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    descr = descr + "<b>" + fields[operator] + "</b>";
                    _addBullet = true;
                }

                operator = 0;
                splits = this._volumeVoice.split("\\|");
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.profile_preferences_volumeVoiceCall) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    descr = descr + "<b>" + fields[operator] + "</b>";
                    _addBullet = true;
                }

                operator = 0;
                splits = this._volumeBluetoothSCO.split("\\|");
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    if (_addBullet)
                        descr = descr +  " • ";
                    descr = descr + context.getString(R.string.profile_preferences_volumeBluetoothSCO) + ": ";
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    descr = descr + "<b>" + fields[operator] + "</b>";
                    _addBullet = true;
                }

                if (Build.VERSION.SDK_INT >= 26) {
                    operator = 0;
                    splits = this._volumeAccessibility.split("\\|");
                    if (splits.length > 1) {
                        try {
                            operator = Integer.parseInt(splits[1]);
                        } catch (Exception ignored) {
                        }
                    }
                    if (operator != 0) {
                        if (_addBullet)
                            descr = descr + " • ";
                        descr = descr + context.getString(R.string.profile_preferences_volumeAccessibility) + ": ";
                        String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                        descr = descr + "<b>" + fields[operator] + "</b>";
                    }
                }

            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key,
                            @SuppressWarnings("unused") String value/*, Context context*/)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_VOLUMES_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false);
            }
        }

        /*if (key.equals(PREF_EVENT_VOLUMES_RINGTONE) ||
                key.equals(PREF_EVENT_VOLUMES_NOTIFICATION) ||
                key.equals(PREF_EVENT_VOLUMES_MEDIA) ||
                key.equals(PREF_EVENT_VOLUMES_ALARM) ||
                key.equals(PREF_EVENT_VOLUMES_SYSTEM) ||
                key.equals(PREF_EVENT_VOLUMES_VOICE) ||
                key.equals(PREF_EVENT_VOLUMES_BLUETOOTHSCO) ||
                key.equals(PREF_EVENT_VOLUMES_ACCESSIBILITY)) {
        }*/

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesVolumes.saveSharedPreferences(prefMng.getSharedPreferences());

    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences,
                    @SuppressWarnings("unused") Context context)
    {
        if (key.equals(PREF_EVENT_VOLUMES_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false"/*, context*/);
        }
        if (key.equals(PREF_EVENT_VOLUMES_RINGTONE) ||
                key.equals(PREF_EVENT_VOLUMES_NOTIFICATION) ||
                key.equals(PREF_EVENT_VOLUMES_MEDIA) ||
                key.equals(PREF_EVENT_VOLUMES_ALARM) ||
                key.equals(PREF_EVENT_VOLUMES_SYSTEM) ||
                key.equals(PREF_EVENT_VOLUMES_VOICE) ||
                key.equals(PREF_EVENT_VOLUMES_BLUETOOTHSCO) ||
                key.equals(PREF_EVENT_VOLUMES_ACCESSIBILITY)) {
            setSummary(prefMng, key, preferences.getString(key, "")/*, context*/);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_VOLUMES_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_RINGTONE, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_NOTIFICATION, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_MEDIA, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_ALARM, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_SYSTEM, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_VOICE, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_BLUETOOTHSCO, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_ACCESSIBILITY, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_VOLUMES_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesVolumes tmp = new EventPreferencesVolumes(this._event, this._enabled,
                    this._volumeRingtone, this._volumeNotification, this._volumeMedia,
                    this._volumeAlarm, this._volumeSystem, this._volumeVoice,
                    this._volumeBluetoothSCO, this._volumeAccessibility);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_VOLUMES_CATEGORY);
            if (preference != null) {
                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_VOLUMES_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_VOLUMES).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, !(tmp.isRunnable(context) && permissionGranted));
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false, false, 0, 0));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_VOLUMES_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context)
    {
        boolean runnable = super.isRunnable(context);

        int ringtoneOperator = 0;
        String[] splits = this._volumeRingtone.split("\\|");
        if (splits.length > 1) {
            try {
                ringtoneOperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        int notificationOperator = 0;
        splits = this._volumeNotification.split("\\|");
        if (splits.length > 1) {
            try {
                notificationOperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        int mediaOoperator = 0;
        splits = this._volumeMedia.split("\\|");
        if (splits.length > 1) {
            try {
                mediaOoperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        int alarmOperator = 0;
        splits = this._volumeAlarm.split("\\|");
        if (splits.length > 1) {
            try {
                alarmOperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        int systemOperator = 0;
        splits = this._volumeSystem.split("\\|");
        if (splits.length > 1) {
            try {
                systemOperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        int voiceOperator = 0;
        splits = this._volumeVoice.split("\\|");
        if (splits.length > 1) {
            try {
                voiceOperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        int bluetoothSCOOperator = 0;
        splits = this._volumeBluetoothSCO.split("\\|");
        if (splits.length > 1) {
            try {
                bluetoothSCOOperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        if (Build.VERSION.SDK_INT < 26) {
            runnable = runnable &&
                    ((ringtoneOperator != 0) || (notificationOperator != 0) || (mediaOoperator != 0) ||
                     (alarmOperator != 0) || (systemOperator != 0) || (voiceOperator != 0) ||
                     (bluetoothSCOOperator != 0));
        } else  {
            int accessibilityOperator = 0;
            splits = this._volumeAccessibility.split("\\|");
            if (splits.length > 1) {
                try {
                    accessibilityOperator = Integer.parseInt(splits[1]);
                } catch (Exception ignored) {
                }
            }

            runnable = runnable &&
                    ((ringtoneOperator != 0) || (notificationOperator != 0) || (mediaOoperator != 0) ||
                     (alarmOperator != 0) || (systemOperator != 0) || (voiceOperator != 0) ||
                     (bluetoothSCOOperator != 0) || (accessibilityOperator != 0));
        }

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        setCategorySummary(prefMng, preferences, context);
    }
/*
    @Override
    void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        //PPApplication.logE("EventPreferencesVolumes.setSystemRunningEvent","xxx");
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        //PPApplication.logE("[BOOT] EventPreferencesVolumes.setSystemPauseEvent","xxx");
    }

    @Override
    void removeSystemEvent(Context context)
    {
        //PPApplication.logE("EventPreferencesVolumes.removeSystemEvent", "xxx");
    }
*/
    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
//            PPApplication.logE("######### EventPreferencesVolumes.doHandleEvent", "xxx");
            int oldSensorPassed = getSensorPassed();
            if (Event.isEventPreferenceAllowed(EventPreferencesVolumes.PREF_EVENT_VOLUMES_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                //PPApplication.logE("[BOOT] EventPreferencesVolumes.doHandleEvent", "allowed");

            } else
                eventsHandler.notAllowedVolumes = true;

//            PPApplication.logE("######### EventPreferencesVolumes.doHandleEvent", "volumesPassed=" + eventsHandler.volumesPassed);
//            PPApplication.logE("######### EventPreferencesVolumes.doHandleEvent", "notAllowedVolumes=" + eventsHandler.notAllowedVolumes);

            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
//                PPApplication.logE("######### EventPreferencesVolumes.doHandleEvent", "volumes - sensor pass changed");
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_VOLUMES);
            }
        }
    }

}
