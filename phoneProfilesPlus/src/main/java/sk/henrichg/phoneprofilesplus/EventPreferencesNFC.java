package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
//import android.preference.CheckBoxPreference;
//import android.preference.Preference;
//import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

class EventPreferencesNFC extends EventPreferences {

    String _nfcTags;
    long _startTime;
    boolean _permanentRun;
    int _duration;

    static final String PREF_EVENT_NFC_ENABLED = "eventNFCEnabled";
    static final String PREF_EVENT_NFC_NFC_TAGS = "eventNFCTags";
    private static final String PREF_EVENT_NFC_PERMANENT_RUN = "eventNFCPermanentRun";
    private static final String PREF_EVENT_NFC_DURATION = "eventNFCDuration";

    private static final String PREF_EVENT_NFC_CATEGORY = "eventNFCCategoryRoot";

    EventPreferencesNFC(Event event,
                               boolean enabled,
                               String nfcTags,
                               boolean permanentRun,
                               int duration)
    {
        super(event, enabled);
        this._nfcTags = nfcTags;
        this._permanentRun = permanentRun;
        this._duration = duration;

        this._startTime = 0;
    }

    @Override
    public void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesNFC._enabled;
        this._nfcTags = fromEvent._eventPreferencesNFC._nfcTags;
        this._permanentRun = fromEvent._eventPreferencesNFC._permanentRun;
        this._duration = fromEvent._eventPreferencesNFC._duration;
        this.setSensorPassed(fromEvent._eventPreferencesNFC.getSensorPassed());

        this._startTime = 0;
    }

    @Override
    public void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_NFC_ENABLED, _enabled);
        editor.putString(PREF_EVENT_NFC_NFC_TAGS, _nfcTags);
        editor.putBoolean(PREF_EVENT_NFC_PERMANENT_RUN, this._permanentRun);
        editor.putString(PREF_EVENT_NFC_DURATION, String.valueOf(this._duration));
        editor.apply();
    }

    @Override
    public void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_NFC_ENABLED, false);
        this._nfcTags = preferences.getString(PREF_EVENT_NFC_NFC_TAGS, "");
        this._permanentRun = preferences.getBoolean(PREF_EVENT_NFC_PERMANENT_RUN, true);
        this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_NFC_DURATION, "5"));
    }

    @Override
    public String getPreferencesDescription(boolean addBullet, boolean addPassStatus, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_nfc_summary);
        } else {
            if (Event.isEventPreferenceAllowed(PREF_EVENT_NFC_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    descr = descr + "<b>";
                    descr = descr + getPassStatusString(context.getString(R.string.event_type_nfc), addPassStatus, DatabaseHandler.ETYPE_NFC, context);
                    descr = descr + ": </b>";
                }

                String selectedNfcTags = context.getString(R.string.event_preferences_nfc_nfcTags) + ": ";
                String[] splits = this._nfcTags.split("\\|");
                for (String _tag : splits) {
                    if (_tag.isEmpty()) {
                        //noinspection StringConcatenationInLoop
                        selectedNfcTags = selectedNfcTags + context.getString(R.string.applications_multiselect_summary_text_not_selected);
                    } else if (splits.length == 1) {
                        selectedNfcTags = selectedNfcTags + _tag;
                    } else {
                        selectedNfcTags = context.getString(R.string.applications_multiselect_summary_text_selected);
                        selectedNfcTags = selectedNfcTags + " " + splits.length;
                        break;
                    }
                }
                descr = descr + selectedNfcTags + " • ";
                if (this._permanentRun)
                    descr = descr + context.getString(R.string.pref_event_permanentRun);
                else
                    descr = descr + context.getString(R.string.pref_event_duration) + ": " + GlobalGUIRoutines.getDurationString(this._duration);
            }
        }

        return descr;
    }

    @Override
    void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_NFC_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preference.isChecked(), true, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_NFC_PERMANENT_RUN)) {
            SwitchPreferenceCompat permanentRunPreference = prefMng.findPreference(key);
            if (permanentRunPreference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(permanentRunPreference, true, permanentRunPreference.isChecked(), true, false, false, false);
            }
            Preference preference = prefMng.findPreference(PREF_EVENT_NFC_DURATION);
            if (preference != null) {
                preference.setEnabled(value.equals("false"));
            }
        }
        if (key.equals(PREF_EVENT_NFC_DURATION)) {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 0;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 5, true, false, false, false);
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesNFC.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesNFC.isRunnable(context);
        SwitchPreferenceCompat enabledPreference = prefMng.findPreference(PREF_EVENT_NFC_ENABLED);
        boolean enabled = (enabledPreference != null) && enabledPreference.isChecked();
        Preference preference = prefMng.findPreference(PREF_EVENT_NFC_NFC_TAGS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_NFC_NFC_TAGS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, true, true, !isRunnable, false);
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_NFC_ENABLED) ||
            key.equals(PREF_EVENT_NFC_PERMANENT_RUN)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
        if (key.equals(PREF_EVENT_NFC_NFC_TAGS) ||
            key.equals(PREF_EVENT_NFC_DURATION))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_NFC_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_NFC_NFC_TAGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NFC_PERMANENT_RUN, preferences, context);
        setSummary(prefMng, PREF_EVENT_NFC_DURATION, preferences, context);

        if (Event.isEventPreferenceAllowed(PREF_EVENT_NFC_ENABLED, context).allowed
                != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            Preference preference = prefMng.findPreference(PREF_EVENT_NFC_ENABLED);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_NFC_NFC_TAGS);
            if (preference != null) preference.setEnabled(false);
            preference = prefMng.findPreference(PREF_EVENT_NFC_DURATION);
            if (preference != null) preference.setEnabled(false);
        }
    }

    @Override
    public void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_NFC_ENABLED, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesNFC tmp = new EventPreferencesNFC(this._event, this._enabled, this._nfcTags, this._permanentRun, this._duration);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_NFC_CATEGORY);
            if (preference != null) {
                SwitchPreferenceCompat enabledPreference = prefMng.findPreference(PREF_EVENT_NFC_ENABLED);
                boolean enabled = (enabledPreference != null) && enabledPreference.isChecked();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, true, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, false, context), false));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_NFC_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getResources().getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    public boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

        runnable = runnable && (!_nfcTags.isEmpty());

        return runnable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context)
    {
        boolean enabled = Event.isEventPreferenceAllowed(PREF_EVENT_NFC_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED;
        Preference nfcTagsPreference = prefMng.findPreference(PREF_EVENT_NFC_NFC_TAGS);
        Preference permanentRunPreference = prefMng.findPreference(PREF_EVENT_NFC_PERMANENT_RUN);
        Preference durationPreference = prefMng.findPreference(PREF_EVENT_NFC_DURATION);
        if (nfcTagsPreference != null)
            nfcTagsPreference.setEnabled(enabled);
        if (permanentRunPreference != null)
            permanentRunPreference.setEnabled(enabled);

        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences != null) {
            boolean permanentRun = preferences.getBoolean(PREF_EVENT_NFC_PERMANENT_RUN, false);
            enabled = enabled && (!permanentRun);
            if (durationPreference != null)
                durationPreference.setEnabled(enabled);
        }
    }

    long computeAlarm()
    {
        PPApplication.logE("EventPreferencesNFC.computeAlarm","xxx");

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
    public void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        PPApplication.logE("EventPreferencesNFC.setSystemRunningEvent","xxx");

        removeAlarm(context);
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        PPApplication.logE("EventPreferencesNFC.setSystemPauseEvent","xxx");

        removeAlarm(context);

        if (!(isRunnable(context) && _enabled))
            return;

        setAlarm(computeAlarm(), context);
    }

    @Override
    public void removeSystemEvent(Context context)
    {
        removeAlarm(context);

        PPApplication.logE("EventPreferencesNFC.removeSystemEvent", "xxx");
    }

    void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            //Intent intent = new Intent(context, NFCEventEndBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_NFC_EVENT_END_BROADCAST_RECEIVER);
            //intent.setClass(context, NFCEventEndBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                PPApplication.logE("EventPreferencesNFC.removeAlarm", "alarm found");

                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    private void setAlarm(long alarmTime, Context context)
    {
        if (!_permanentRun) {
            if (_startTime > 0) {
                if (PPApplication.logEnabled()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String result = sdf.format(alarmTime);
                    PPApplication.logE("EventPreferencesNFC.setAlarm", "endTime=" + result);
                }

                //Intent intent = new Intent(context, NFCEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_NFC_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, NFCEventEndBroadcastReceiver.class);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (/*(android.os.Build.VERSION.SDK_INT >= 21) &&*/
                            ApplicationPreferences.applicationUseAlarmClock(context)) {
                        Intent editorIntent = new Intent(context, EditorProfilesActivity.class);
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

    void saveStartTime(DataWrapper dataWrapper, String tagName, long startTime) {
        if (this._startTime == 0) {
            // alarm for end is not set

            PPApplication.logE("EventPreferencesNFC.saveStartTime", "tagName=" + tagName);

            boolean tagFound = false;

            String[] splits = this._nfcTags.split("\\|");
            for (String tag : splits) {
                if (tag.equals(tagName))
                    tagFound = true;
            }

            PPApplication.logE("EventPreferencesNFC.saveStartTime", "tagFound=" + tagFound);

            if (tagFound)
                this._startTime = startTime; //  + (10 * 1000);
            else
                this._startTime = 0;

            DatabaseHandler.getInstance(dataWrapper.context).updateNFCStartTime(_event);

            if (tagFound) {
                //if (_event.getStatus() == Event.ESTATUS_RUNNING)
                    setSystemEventForPause(dataWrapper.context);
            }
        }
    }

}
