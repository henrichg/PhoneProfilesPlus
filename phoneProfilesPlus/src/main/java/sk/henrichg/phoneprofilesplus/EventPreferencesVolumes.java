package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesVolumes extends EventPreferences {

    String _volumeRingtoneFrom;
    String _volumeNotificationFrom;
    String _volumeMediaFrom;
    String _volumeAlarmFrom;
    String _volumeSystemFrom;
    String _volumeVoiceFrom;
    String _volumeBluetoothSCOFrom;
    String _volumeRingtoneTo;
    String _volumeNotificationTo;
    String _volumeMediaTo;
    String _volumeAlarmTo;
    String _volumeSystemTo;
    String _volumeVoiceTo;
    String _volumeBluetoothSCOTo;

    static final String PREF_EVENT_VOLUMES_ENABLED = "eventVolumesEnabled";
    static final String PREF_EVENT_VOLUMES_RINGTONE_FROM = "eventVolumesRingtoneFrom";
    static final String PREF_EVENT_VOLUMES_NOTIFICATION_FROM = "eventVolumesNotificationFrom";
    static final String PREF_EVENT_VOLUMES_MEDIA_FROM = "eventVolumesMediaFrom";
    static final String PREF_EVENT_VOLUMES_ALARM_FROM = "eventVolumesAlarmFrom";
    static final String PREF_EVENT_VOLUMES_SYSTEM_FROM = "eventVolumesSystemFrom";
    static final String PREF_EVENT_VOLUMES_VOICE_FROM = "eventVolumesVoiceFrom";
    static final String PREF_EVENT_VOLUMES_BLUETOOTHSCO_FROM = "eventVolumesBluetoothSCOFrom";
    static final String PREF_EVENT_VOLUMES_RINGTONE_TO = "eventVolumesRingtoneTo";
    static final String PREF_EVENT_VOLUMES_NOTIFICATION_TO = "eventVolumesNotificationTo";
    static final String PREF_EVENT_VOLUMES_MEDIA_TO = "eventVolumesMediaTo";
    static final String PREF_EVENT_VOLUMES_ALARM_TO = "eventVolumesAlarmTo";
    static final String PREF_EVENT_VOLUMES_SYSTEM_TO = "eventVolumesSystemTo";
    static final String PREF_EVENT_VOLUMES_VOICE_TO = "eventVolumesVoiceTo";
    static final String PREF_EVENT_VOLUMES_BLUETOOTHSCO_TO = "eventVolumesBluetoothSCOTo";

    private static final String PREF_EVENT_VOLUMES_CATEGORY = "eventVolumesCategoryRoot";

    EventPreferencesVolumes(Event event,
                            boolean enabled,
                            String volumeRingtoneFrom,
                            String volumeNotificationFrom,
                            String volumeMediaFrom,
                            String volumeAlarmFrom,
                            String volumeSystemFrom,
                            String volumeVoiceFrom,
                            String volumeBluetoothSCOFrom,
                            String volumeRingtoneTo,
                            String volumeNotificationTo,
                            String volumeMediaTo,
                            String volumeAlarmTo,
                            String volumeSystemTo,
                            String volumeVoiceTo,
                            String volumeBluetoothSCOTo
                            )
    {
        super(event, enabled);

        this._volumeRingtoneFrom = volumeRingtoneFrom;
        this._volumeNotificationFrom = volumeNotificationFrom;
        this._volumeMediaFrom = volumeMediaFrom;
        this._volumeAlarmFrom = volumeAlarmFrom;
        this._volumeSystemFrom = volumeSystemFrom;
        this._volumeVoiceFrom = volumeVoiceFrom;
        this._volumeBluetoothSCOFrom = volumeBluetoothSCOFrom;
        this._volumeRingtoneTo = volumeRingtoneTo;
        this._volumeNotificationTo = volumeNotificationTo;
        this._volumeMediaTo = volumeMediaTo;
        this._volumeAlarmTo = volumeAlarmTo;
        this._volumeSystemTo = volumeSystemTo;
        this._volumeVoiceTo = volumeVoiceTo;
        this._volumeBluetoothSCOTo = volumeBluetoothSCOTo;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesVolumes._enabled;
        this._volumeRingtoneFrom = fromEvent._eventPreferencesVolumes._volumeRingtoneFrom;
        this._volumeNotificationFrom = fromEvent._eventPreferencesVolumes._volumeNotificationFrom;
        this._volumeMediaFrom = fromEvent._eventPreferencesVolumes._volumeMediaFrom;
        this._volumeAlarmFrom = fromEvent._eventPreferencesVolumes._volumeAlarmFrom;
        this._volumeSystemFrom = fromEvent._eventPreferencesVolumes._volumeSystemFrom;
        this._volumeVoiceFrom = fromEvent._eventPreferencesVolumes._volumeVoiceFrom;
        this._volumeBluetoothSCOFrom = fromEvent._eventPreferencesVolumes._volumeBluetoothSCOFrom;
        this._volumeRingtoneTo = fromEvent._eventPreferencesVolumes._volumeRingtoneTo;
        this._volumeNotificationTo = fromEvent._eventPreferencesVolumes._volumeNotificationTo;
        this._volumeMediaTo = fromEvent._eventPreferencesVolumes._volumeMediaTo;
        this._volumeAlarmTo = fromEvent._eventPreferencesVolumes._volumeAlarmTo;
        this._volumeSystemTo = fromEvent._eventPreferencesVolumes._volumeSystemTo;
        this._volumeVoiceTo = fromEvent._eventPreferencesVolumes._volumeVoiceTo;
        this._volumeBluetoothSCOTo = fromEvent._eventPreferencesVolumes._volumeBluetoothSCOTo;
        this.setSensorPassed(fromEvent._eventPreferencesVolumes.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_VOLUMES_ENABLED, _enabled);
        editor.putString(PREF_EVENT_VOLUMES_RINGTONE_FROM, this._volumeRingtoneFrom);
        editor.putString(PREF_EVENT_VOLUMES_NOTIFICATION_FROM, this._volumeNotificationFrom);
        editor.putString(PREF_EVENT_VOLUMES_MEDIA_FROM, this._volumeMediaFrom);
        editor.putString(PREF_EVENT_VOLUMES_ALARM_FROM, this._volumeAlarmFrom);
        editor.putString(PREF_EVENT_VOLUMES_SYSTEM_FROM, this._volumeSystemFrom);
        editor.putString(PREF_EVENT_VOLUMES_VOICE_FROM, this._volumeVoiceFrom);
        editor.putString(PREF_EVENT_VOLUMES_BLUETOOTHSCO_FROM, this._volumeBluetoothSCOFrom);
        editor.putString(PREF_EVENT_VOLUMES_RINGTONE_TO, this._volumeRingtoneTo);
        editor.putString(PREF_EVENT_VOLUMES_NOTIFICATION_TO, this._volumeNotificationTo);
        editor.putString(PREF_EVENT_VOLUMES_MEDIA_TO, this._volumeMediaTo);
        editor.putString(PREF_EVENT_VOLUMES_ALARM_TO, this._volumeAlarmTo);
        editor.putString(PREF_EVENT_VOLUMES_SYSTEM_TO, this._volumeSystemTo);
        editor.putString(PREF_EVENT_VOLUMES_VOICE_TO, this._volumeVoiceTo);
        editor.putString(PREF_EVENT_VOLUMES_BLUETOOTHSCO_TO, this._volumeBluetoothSCOTo);
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_VOLUMES_ENABLED, false);
        this._volumeRingtoneFrom = preferences.getString(PREF_EVENT_VOLUMES_RINGTONE_FROM, "0|0|0");
        this._volumeNotificationFrom = preferences.getString(PREF_EVENT_VOLUMES_NOTIFICATION_FROM, "0|0|0");
        this._volumeMediaFrom = preferences.getString(PREF_EVENT_VOLUMES_MEDIA_FROM, "0|0|0");
        this._volumeAlarmFrom = preferences.getString(PREF_EVENT_VOLUMES_ALARM_FROM, "0|0|0");
        this._volumeSystemFrom = preferences.getString(PREF_EVENT_VOLUMES_SYSTEM_FROM, "0|0|0");
        this._volumeVoiceFrom = preferences.getString(PREF_EVENT_VOLUMES_VOICE_FROM, "0|0|0");
        this._volumeBluetoothSCOFrom = preferences.getString(PREF_EVENT_VOLUMES_BLUETOOTHSCO_FROM, "0|0|0");
        this._volumeRingtoneTo = preferences.getString(PREF_EVENT_VOLUMES_RINGTONE_TO, "0|0|0");
        this._volumeNotificationTo = preferences.getString(PREF_EVENT_VOLUMES_NOTIFICATION_TO, "0|0|0");
        this._volumeMediaTo = preferences.getString(PREF_EVENT_VOLUMES_MEDIA_TO, "0|0|0");
        this._volumeAlarmTo = preferences.getString(PREF_EVENT_VOLUMES_ALARM_TO, "0|0|0");
        this._volumeSystemTo = preferences.getString(PREF_EVENT_VOLUMES_SYSTEM_TO, "0|0|0");
        this._volumeVoiceTo = preferences.getString(PREF_EVENT_VOLUMES_VOICE_TO, "0|0|0");
        this._volumeBluetoothSCOTo = preferences.getString(PREF_EVENT_VOLUMES_BLUETOOTHSCO_TO, "0|0|0");
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_volumes_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_VOLUMES_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_volumes), addPassStatus, DatabaseHandler.ETYPE_VOLUMES, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                boolean _addBullet = false;

                int operator = 0;
                String[] splits = this._volumeRingtoneFrom.split(StringConstants.STR_SPLIT_REGEX);
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    _value.append(context.getString(R.string.profile_preferences_volumeRingtone)).append(StringConstants.STR_COLON_WITH_SPACE);
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[operator] + " " + splits[0], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    _addBullet = true;
                }

                operator = 0;
                splits = this._volumeNotificationFrom.split(StringConstants.STR_SPLIT_REGEX);
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    if (_addBullet)
                        _value.append(StringConstants.STR_BULLET);
                    _value.append(context.getString(R.string.profile_preferences_volumeNotification)).append(StringConstants.STR_COLON_WITH_SPACE);
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[operator] + " " + splits[0], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    _addBullet = true;
                }

                operator = 0;
                splits = this._volumeMediaFrom.split(StringConstants.STR_SPLIT_REGEX);
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    if (_addBullet)
                        _value.append(StringConstants.STR_BULLET);
                    _value.append(context.getString(R.string.profile_preferences_volumeMedia)).append(StringConstants.STR_COLON_WITH_SPACE);
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[operator] + " " + splits[0], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    _addBullet = true;
                }

                operator = 0;
                splits = this._volumeAlarmFrom.split(StringConstants.STR_SPLIT_REGEX);
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    if (_addBullet)
                        _value.append(StringConstants.STR_BULLET);
                    _value.append(context.getString(R.string.profile_preferences_volumeAlarm)).append(StringConstants.STR_COLON_WITH_SPACE);
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[operator] + " " + splits[0], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    _addBullet = true;
                }

                operator = 0;
                splits = this._volumeSystemFrom.split(StringConstants.STR_SPLIT_REGEX);
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    if (_addBullet)
                        _value.append(StringConstants.STR_BULLET);
                    _value.append(context.getString(R.string.profile_preferences_volumeSystem)).append(StringConstants.STR_COLON_WITH_SPACE);
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[operator] + " " + splits[0], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    _addBullet = true;
                }

                operator = 0;
                splits = this._volumeVoiceFrom.split(StringConstants.STR_SPLIT_REGEX);
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    if (_addBullet)
                        _value.append(StringConstants.STR_BULLET);
                    _value.append(context.getString(R.string.profile_preferences_volumeVoiceCall)).append(StringConstants.STR_COLON_WITH_SPACE);
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[operator] + " " + splits[0], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    _addBullet = true;
                }

                operator = 0;
                splits = this._volumeBluetoothSCOFrom.split(StringConstants.STR_SPLIT_REGEX);
                if (splits.length > 1) {
                    try {
                        operator = Integer.parseInt(splits[1]);
                    } catch (Exception ignored) {}
                }
                if (operator != 0) {
                    if (_addBullet)
                        _value.append(StringConstants.STR_BULLET);
                    _value.append(context.getString(R.string.profile_preferences_volumeBluetoothSCO)).append(StringConstants.STR_COLON_WITH_SPACE);
                    String[] fields = context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(fields[operator] + " " + splits[0], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    //_addBullet = true;
                }

                /*
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
                        descr = descr + "<b>" + getColorForChangedPreferenceValue(fields[operator], disabled, context) + "</b>";
                    }
                }
                */

            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key,
                            @SuppressWarnings("unused") String value,
                            Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_VOLUMES_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesVolumes.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesVolumes.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_VOLUMES_ENABLED, false);
        VolumeDialogPreference preference = prefMng.findPreference(PREF_EVENT_VOLUMES_RINGTONE_FROM);
        String defaultValue = "0|0|0";
        if (preference != null) {
            String[] splits = prefMng.getSharedPreferences().getString(PREF_EVENT_VOLUMES_RINGTONE_FROM, defaultValue).split(StringConstants.STR_SPLIT_REGEX);
            boolean bold =  (splits.length > 1) && (!splits[1].equals("0"));
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_VOLUMES_NOTIFICATION_FROM);
        if (preference != null) {
            String[] splits = prefMng.getSharedPreferences().getString(PREF_EVENT_VOLUMES_NOTIFICATION_FROM, defaultValue).split(StringConstants.STR_SPLIT_REGEX);
            boolean bold =  (splits.length > 1) && (!splits[1].equals("0"));
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_VOLUMES_MEDIA_FROM);
        if (preference != null) {
            String[] splits = prefMng.getSharedPreferences().getString(PREF_EVENT_VOLUMES_MEDIA_FROM, defaultValue).split(StringConstants.STR_SPLIT_REGEX);
            boolean bold =  (splits.length > 1) && (!splits[1].equals("0"));
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_VOLUMES_ALARM_FROM);
        if (preference != null) {
            String[] splits = prefMng.getSharedPreferences().getString(PREF_EVENT_VOLUMES_ALARM_FROM, defaultValue).split(StringConstants.STR_SPLIT_REGEX);
            boolean bold =  (splits.length > 1) && (!splits[1].equals("0"));
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_VOLUMES_SYSTEM_FROM);
        if (preference != null) {
            String[] splits = prefMng.getSharedPreferences().getString(PREF_EVENT_VOLUMES_SYSTEM_FROM, defaultValue).split(StringConstants.STR_SPLIT_REGEX);
            boolean bold =  (splits.length > 1) && (!splits[1].equals("0"));
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_VOLUMES_VOICE_FROM);
        if (preference != null) {
            String[] splits = prefMng.getSharedPreferences().getString(PREF_EVENT_VOLUMES_VOICE_FROM, defaultValue).split(StringConstants.STR_SPLIT_REGEX);
            boolean bold =  (splits.length > 1) && (!splits[1].equals("0"));
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_VOLUMES_BLUETOOTHSCO_FROM);
        if (preference != null) {
            String[] splits = prefMng.getSharedPreferences().getString(PREF_EVENT_VOLUMES_BLUETOOTHSCO_FROM, defaultValue).split(StringConstants.STR_SPLIT_REGEX);
            boolean bold =  (splits.length > 1) && (!splits[1].equals("0"));
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }

    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_VOLUMES_ENABLED)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
        if (key.equals(PREF_EVENT_VOLUMES_RINGTONE_FROM) ||
                key.equals(PREF_EVENT_VOLUMES_NOTIFICATION_FROM) ||
                key.equals(PREF_EVENT_VOLUMES_MEDIA_FROM) ||
                key.equals(PREF_EVENT_VOLUMES_ALARM_FROM) ||
                key.equals(PREF_EVENT_VOLUMES_SYSTEM_FROM) ||
                key.equals(PREF_EVENT_VOLUMES_VOICE_FROM) ||
                key.equals(PREF_EVENT_VOLUMES_BLUETOOTHSCO_FROM) ||
                key.equals(PREF_EVENT_VOLUMES_RINGTONE_TO) ||
                key.equals(PREF_EVENT_VOLUMES_NOTIFICATION_TO) ||
                key.equals(PREF_EVENT_VOLUMES_MEDIA_TO) ||
                key.equals(PREF_EVENT_VOLUMES_ALARM_TO) ||
                key.equals(PREF_EVENT_VOLUMES_SYSTEM_TO) ||
                key.equals(PREF_EVENT_VOLUMES_VOICE_TO) ||
                key.equals(PREF_EVENT_VOLUMES_BLUETOOTHSCO_TO)) {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_VOLUMES_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_RINGTONE_FROM, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_NOTIFICATION_FROM, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_MEDIA_FROM, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_ALARM_FROM, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_SYSTEM_FROM, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_VOICE_FROM, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_BLUETOOTHSCO_FROM, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_RINGTONE_TO, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_NOTIFICATION_TO, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_MEDIA_TO, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_ALARM_TO, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_SYSTEM_TO, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_VOICE_TO, preferences, context);
        setSummary(prefMng, PREF_EVENT_VOLUMES_BLUETOOTHSCO_TO, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_VOLUMES_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesVolumes tmp = new EventPreferencesVolumes(this._event, this._enabled,
                    this._volumeRingtoneFrom, this._volumeNotificationFrom, this._volumeMediaFrom,
                    this._volumeAlarmFrom, this._volumeSystemFrom, this._volumeVoiceFrom,
                    this._volumeBluetoothSCOFrom,
                    this._volumeRingtoneTo, this._volumeNotificationTo, this._volumeMediaTo,
                    this._volumeAlarmTo, this._volumeSystemTo, this._volumeVoiceTo,
                    this._volumeBluetoothSCOTo);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_VOLUMES_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_VOLUMES_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_VOLUMES).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false, false, false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_VOLUMES_CATEGORY);
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

        int ringtoneOperator = 0;
        String[] splits = this._volumeRingtoneFrom.split(StringConstants.STR_SPLIT_REGEX);
        if (splits.length > 1) {
            try {
                ringtoneOperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        int notificationOperator = 0;
        splits = this._volumeNotificationFrom.split(StringConstants.STR_SPLIT_REGEX);
        if (splits.length > 1) {
            try {
                notificationOperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        int mediaOoperator = 0;
        splits = this._volumeMediaFrom.split(StringConstants.STR_SPLIT_REGEX);
        if (splits.length > 1) {
            try {
                mediaOoperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        int alarmOperator = 0;
        splits = this._volumeAlarmFrom.split(StringConstants.STR_SPLIT_REGEX);
        if (splits.length > 1) {
            try {
                alarmOperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        int systemOperator = 0;
        splits = this._volumeSystemFrom.split(StringConstants.STR_SPLIT_REGEX);
        if (splits.length > 1) {
            try {
                systemOperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        int voiceOperator = 0;
        splits = this._volumeVoiceFrom.split(StringConstants.STR_SPLIT_REGEX);
        if (splits.length > 1) {
            try {
                voiceOperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        int bluetoothSCOOperator = 0;
        splits = this._volumeBluetoothSCOFrom.split(StringConstants.STR_SPLIT_REGEX);
        if (splits.length > 1) {
            try {
                bluetoothSCOOperator = Integer.parseInt(splits[1]);
            } catch (Exception ignored) {}
        }

        //if (Build.VERSION.SDK_INT < 26) {
            runnable = runnable &&
                    ((ringtoneOperator != 0) || (notificationOperator != 0) || (mediaOoperator != 0) ||
                     (alarmOperator != 0) || (systemOperator != 0) || (voiceOperator != 0) ||
                     (bluetoothSCOOperator != 0));
        /*} else  {
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
        }*/

        return runnable;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_VOLUMES_ENABLED) != null) {
                setSummary(prefMng, PREF_EVENT_VOLUMES_ENABLED, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }
/*
    @Override
    void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

    }

    @Override
    void removeSystemEvent(Context context)
    {
    }
*/
    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if (EventStatic.isEventPreferenceAllowed(EventPreferencesVolumes.PREF_EVENT_VOLUMES_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                AudioManager audioManager = (AudioManager)eventsHandler.context.getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {

                    // ringtone
                    int actualValue = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    int configuredValue = -1;
                    int configuredOperator = 0;
                    String[] splits = this._volumeRingtoneFrom.split(StringConstants.STR_SPLIT_REGEX);
                    if (splits.length > 1) {
                        try {
                            configuredValue = Integer.parseInt(splits[0]);
                            configuredOperator = Integer.parseInt(splits[1]);
                        } catch (Exception ignored) {
                        }
                    }
                    boolean ringtoneTested = configuredOperator > 0;
                    boolean ringtonePassed = false;
                    switch (configuredOperator) {
                        case 1: // equal to
                            if (actualValue == configuredValue)
                                ringtonePassed = true;
                            break;
                        case 2: // do not equal to
                            if (actualValue != configuredValue)
                                ringtonePassed = true;
                            break;
                        case 3: // is less then
                            if (actualValue < configuredValue)
                                ringtonePassed = true;
                            break;
                        case 4: // is greather then
                            if (actualValue > configuredValue)
                                ringtonePassed = true;
                            break;
                        case 5: // is less or equal to
                            if (actualValue <= configuredValue)
                                ringtonePassed = true;
                            break;
                        case 6: // is greather or equal to
                            if (actualValue >= configuredValue)
                                ringtonePassed = true;
                            break;
                    }

                    // notification
                    actualValue = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                    configuredValue = -1;
                    configuredOperator = 0;
                    splits = this._volumeNotificationFrom.split(StringConstants.STR_SPLIT_REGEX);
                    if (splits.length > 1) {
                        try {
                            configuredValue = Integer.parseInt(splits[0]);
                            configuredOperator = Integer.parseInt(splits[1]);
                        } catch (Exception ignored) {
                        }
                    }
                    boolean notificationTested = configuredOperator > 0;
                    boolean notificationPassed = false;
                    switch (configuredOperator) {
                        case 1: // equal to
                            if (actualValue == configuredValue)
                                notificationPassed = true;
                            break;
                        case 2: // do not equal to
                            if (actualValue != configuredValue)
                                notificationPassed = true;
                            break;
                        case 3: // is less then
                            if (actualValue < configuredValue)
                                notificationPassed = true;
                            break;
                        case 4: // is greather then
                            if (actualValue > configuredValue)
                                notificationPassed = true;
                            break;
                        case 5: // is less or equal to
                            if (actualValue <= configuredValue)
                                notificationPassed = true;
                            break;
                        case 6: // is greather or equal to
                            if (actualValue >= configuredValue)
                                notificationPassed = true;
                            break;
                    }

                    // media
                    actualValue = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    configuredValue = -1;
                    configuredOperator = 0;
                    splits = this._volumeMediaFrom.split(StringConstants.STR_SPLIT_REGEX);
                    if (splits.length > 1) {
                        try {
                            configuredValue = Integer.parseInt(splits[0]);
                            configuredOperator = Integer.parseInt(splits[1]);
                        } catch (Exception ignored) {
                        }
                    }
                    boolean mediaTested = configuredOperator > 0;
                    boolean mediaPassed = false;
                    switch (configuredOperator) {
                        case 1: // equal to
                            if (actualValue == configuredValue)
                                mediaPassed = true;
                            break;
                        case 2: // do not equal to
                            if (actualValue != configuredValue)
                                mediaPassed = true;
                            break;
                        case 3: // is less then
                            if (actualValue < configuredValue)
                                mediaPassed = true;
                            break;
                        case 4: // is greather then
                            if (actualValue > configuredValue)
                                mediaPassed = true;
                            break;
                        case 5: // is less or equal to
                            if (actualValue <= configuredValue)
                                mediaPassed = true;
                            break;
                        case 6: // is greather or equal to
                            if (actualValue >= configuredValue)
                                mediaPassed = true;
                            break;
                    }

                    // alarm
                    actualValue = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                    configuredValue = -1;
                    configuredOperator = 0;
                    splits = this._volumeAlarmFrom.split(StringConstants.STR_SPLIT_REGEX);
                    if (splits.length > 1) {
                        try {
                            configuredValue = Integer.parseInt(splits[0]);
                            configuredOperator = Integer.parseInt(splits[1]);
                        } catch (Exception ignored) {
                        }
                    }
                    boolean alarmTested = configuredOperator > 0;
                    boolean alarmPassed = false;
                    switch (configuredOperator) {
                        case 1: // equal to
                            if (actualValue == configuredValue)
                                alarmPassed = true;
                            break;
                        case 2: // do not equal to
                            if (actualValue != configuredValue)
                                alarmPassed = true;
                            break;
                        case 3: // is less then
                            if (actualValue < configuredValue)
                                alarmPassed = true;
                            break;
                        case 4: // is greather then
                            if (actualValue > configuredValue)
                                alarmPassed = true;
                            break;
                        case 5: // is less or equal to
                            if (actualValue <= configuredValue)
                                alarmPassed = true;
                            break;
                        case 6: // is greather or equal to
                            if (actualValue >= configuredValue)
                                alarmPassed = true;
                            break;
                    }

                    // system
                    actualValue = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                    configuredValue = -1;
                    configuredOperator = 0;
                    splits = this._volumeSystemFrom.split(StringConstants.STR_SPLIT_REGEX);
                    if (splits.length > 1) {
                        try {
                            configuredValue = Integer.parseInt(splits[0]);
                            configuredOperator = Integer.parseInt(splits[1]);
                        } catch (Exception ignored) {
                        }
                    }
                    boolean systemTested = configuredOperator > 0;
                    boolean systemPassed = false;
                    switch (configuredOperator) {
                        case 1: // equal to
                            if (actualValue == configuredValue)
                                systemPassed = true;
                            break;
                        case 2: // do not equal to
                            if (actualValue != configuredValue)
                                systemPassed = true;
                            break;
                        case 3: // is less then
                            if (actualValue < configuredValue)
                                systemPassed = true;
                            break;
                        case 4: // is greather then
                            if (actualValue > configuredValue)
                                systemPassed = true;
                            break;
                        case 5: // is less or equal to
                            if (actualValue <= configuredValue)
                                systemPassed = true;
                            break;
                        case 6: // is greather or equal to
                            if (actualValue >= configuredValue)
                                systemPassed = true;
                            break;
                    }

                    // voice
                    actualValue = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                    configuredValue = -1;
                    configuredOperator = 0;
                    splits = this._volumeVoiceFrom.split(StringConstants.STR_SPLIT_REGEX);
                    if (splits.length > 1) {
                        try {
                            configuredValue = Integer.parseInt(splits[0]);
                            configuredOperator = Integer.parseInt(splits[1]);
                        } catch (Exception ignored) {
                        }
                    }
                    boolean voiceTested = configuredOperator > 0;
                    boolean voicePassed = false;
                    switch (configuredOperator) {
                        case 1: // equal to
                            if (actualValue == configuredValue)
                                voicePassed = true;
                            break;
                        case 2: // do not equal to
                            if (actualValue != configuredValue)
                                voicePassed = true;
                            break;
                        case 3: // is less then
                            if (actualValue < configuredValue)
                                voicePassed = true;
                            break;
                        case 4: // is greather then
                            if (actualValue > configuredValue)
                                voicePassed = true;
                            break;
                        case 5: // is less or equal to
                            if (actualValue <= configuredValue)
                                voicePassed = true;
                            break;
                        case 6: // is greather or equal to
                            if (actualValue >= configuredValue)
                                voicePassed = true;
                            break;
                    }

                    // bluetooth sco
                    actualValue = audioManager.getStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO);
                    configuredValue = -1;
                    configuredOperator = 0;
                    splits = this._volumeBluetoothSCOFrom.split(StringConstants.STR_SPLIT_REGEX);
                    if (splits.length > 1) {
                        try {
                            configuredValue = Integer.parseInt(splits[0]);
                            configuredOperator = Integer.parseInt(splits[1]);
                        } catch (Exception ignored) {
                        }
                    }
                    boolean bluetoothSCOTested = configuredOperator > 0;
                    boolean bluetoothSCOPassed = false;
                    switch (configuredOperator) {
                        case 1: // equal to
                            if (actualValue == configuredValue)
                                bluetoothSCOPassed = true;
                            break;
                        case 2: // do not equal to
                            if (actualValue != configuredValue)
                                bluetoothSCOPassed = true;
                            break;
                        case 3: // is less then
                            if (actualValue < configuredValue)
                                bluetoothSCOPassed = true;
                            break;
                        case 4: // is greather then
                            if (actualValue > configuredValue)
                                bluetoothSCOPassed = true;
                            break;
                        case 5: // is less or equal to
                            if (actualValue <= configuredValue)
                                bluetoothSCOPassed = true;
                            break;
                        case 6: // is greather or equal to
                            if (actualValue >= configuredValue)
                                bluetoothSCOPassed = true;
                            break;
                    }

                    boolean passed = true;
                    if (ringtoneTested)
                        //noinspection ConstantConditions
                        passed = passed && ringtonePassed;
                    if (notificationTested)
                        passed =  passed && notificationPassed;
                    if (mediaTested)
                        passed =  passed && mediaPassed;
                    if (alarmTested)
                        passed =  passed && alarmPassed;
                    if (systemTested)
                        passed =  passed && systemPassed;
                    if (voiceTested)
                        passed =  passed && voicePassed;
                    if (bluetoothSCOTested)
                        passed =  passed && bluetoothSCOPassed;

                    eventsHandler.volumesPassed = passed;
                }
                else
                    eventsHandler.notAllowedVolumes = true;

                if (!eventsHandler.notAllowedVolumes) {
                    if (eventsHandler.volumesPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }

            } else
                eventsHandler.notAllowedVolumes = true;

            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_VOLUMES);
            }
        }
    }

}
