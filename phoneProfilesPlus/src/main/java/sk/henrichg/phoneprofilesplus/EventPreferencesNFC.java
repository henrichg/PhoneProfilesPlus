package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

class EventPreferencesNFC extends EventPreferences {

    String _nfcTags;
    long _startTime;
    boolean _permanentRun;
    int _duration;

    static final String PREF_EVENT_NFC_ENABLED = "eventNFCEnabled";
    private static final String PREF_EVENT_NFC_NFC_TAGS = "eventNFCTags";
    private static final String PREF_EVENT_NFC_PERMANENT_RUN = "eventNFCPermanentRun";
    private static final String PREF_EVENT_NFC_DURATION = "eventNFCDuration";

    private static final String PREF_EVENT_NFC_CATEGORY = "eventNFCCategory";

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
        editor.commit();
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
    public String getPreferencesDescription(boolean addBullet, Context context)
    {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_nfc_summary);
        }
        else
        {
            if (addBullet) {
                descr = descr + "<b>\u2022 </b>";
                descr = descr + "<b>" + context.getString(R.string.event_type_nfc) + ": " + "</b>";
            }

            String selectedNfcTags = context.getString(R.string.event_preferences_nfc_nfcTags) + ": ";
            String[] splits = this._nfcTags.split("\\|");
            for (String _tag : splits) {
                if (_tag.isEmpty()) {
                    selectedNfcTags = selectedNfcTags + context.getString(R.string.applications_multiselect_summary_text_not_selected);
                }
                else
                if (splits.length == 1) {
                    selectedNfcTags = selectedNfcTags + _tag;
                }
                else {
                    selectedNfcTags = context.getString(R.string.applications_multiselect_summary_text_selected);
                    selectedNfcTags = selectedNfcTags + " " + splits.length;
                    break;
                }
            }
            descr = descr + selectedNfcTags + "; ";
            if (this._permanentRun)
                descr = descr + context.getString(R.string.pref_event_permanentRun);
            else
                descr = descr + context.getString(R.string.pref_event_duration) + ": " + GlobalGUIRoutines.getDurationString(this._duration);
        }

        return descr;
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        if (key.equals(PREF_EVENT_NFC_NFC_TAGS))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String[] splits = value.split("\\|");
                for (String _tag : splits) {
                    if (_tag.isEmpty()) {
                        preference.setSummary(R.string.applications_multiselect_summary_text_not_selected);
                    }
                    else
                    if (splits.length == 1) {
                        preference.setSummary(_tag);
                    }
                    else {
                        String selectedNfcTags = context.getString(R.string.applications_multiselect_summary_text_selected);
                        selectedNfcTags = selectedNfcTags + " " + splits.length;
                        preference.setSummary(selectedNfcTags);
                        break;
                    }
                }
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, false, true, false, false);
            }
        }
        if (key.equals(PREF_EVENT_NFC_PERMANENT_RUN)) {
            Preference preference = prefMng.findPreference(PREF_EVENT_NFC_DURATION);
            if (preference != null) {
                preference.setEnabled(value.equals("false"));
            }
        }
    }

    @Override
    public void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (key.equals(PREF_EVENT_NFC_NFC_TAGS) ||
            key.equals(PREF_EVENT_NFC_DURATION))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
        if (key.equals(PREF_EVENT_NFC_PERMANENT_RUN)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
    }

    @Override
    public void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_NFC_NFC_TAGS, preferences, context);
        setSummary(prefMng, PREF_EVENT_NFC_PERMANENT_RUN, preferences, context);
        setSummary(prefMng, PREF_EVENT_NFC_DURATION, preferences, context);

        if (PPApplication.isEventPreferenceAllowed(PREF_EVENT_NFC_ENABLED, context)
                != PPApplication.PREFERENCE_ALLOWED)
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
    public void setCategorySummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (PPApplication.isEventPreferenceAllowed(PREF_EVENT_NFC_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED) {
            EventPreferencesNFC tmp = new EventPreferencesNFC(this._event, this._enabled, this._nfcTags, this._permanentRun, this._duration);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_NFC_CATEGORY);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyle(preference, tmp._enabled, false, !tmp.isRunnable(context), false);
                preference.setSummary(GlobalGUIRoutines.fromHtml(tmp.getPreferencesDescription(false, context)));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_NFC_CATEGORY);
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

        boolean runable = super.isRunnable(context);

        runable = runable && (!_nfcTags.isEmpty());

        return runable;
    }

    @Override
    public void checkPreferences(PreferenceManager prefMng, Context context)
    {
        boolean enabled = PPApplication.isEventPreferenceAllowed(PREF_EVENT_NFC_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED;
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

    @Override
    public boolean activateReturnProfile()
    {
        return true;
    }

    long computeAlarm()
    {
        PPApplication.logE("EventPreferencesNFC.computeAlarm","xxx");

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

        PPApplication.logE("EventPreferencesNFC.setSystemRunningEvent","xxx");

        removeAlarm(context);
    }

    @Override
    public void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsService

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

    public void removeAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        Intent intent = new Intent(context, NFCEventEndBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null)
        {
            PPApplication.logE("EventPreferencesNFC.removeAlarm","alarm found");

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    @SuppressLint({"SimpleDateFormat", "NewApi"})
    private void setAlarm(long alarmTime, Context context)
    {
        if (!_permanentRun) {
            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
            String result = sdf.format(alarmTime);
            PPApplication.logE("EventPreferencesNFC.setAlarm", "endTime=" + result);

            Intent intent = new Intent(context, NFCEventEndBroadcastReceiver.class);
            //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) _event._id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= 23)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + PPApplication.EVENT_ALARM_TIME_OFFSET, pendingIntent);
            else if (android.os.Build.VERSION.SDK_INT >= 19)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime + PPApplication.EVENT_ALARM_TIME_OFFSET, pendingIntent);
            else
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime + PPApplication.EVENT_ALARM_TIME_OFFSET, pendingIntent);
        }
    }

    void saveStartTime(DataWrapper dataWrapper, String tagName, long startTime) {
        boolean tagFound = false;

        String[] splits = this._nfcTags.split("\\|");
        for (int i = 0; i < splits.length; i++) {
            String tag = splits[i];

            if (tag.equals(tagName))
                tagFound = true;
        }

        if (tagFound)
            this._startTime = startTime;
        else
        if (this._permanentRun)
            this._startTime = 0;

        dataWrapper.getDatabaseHandler().updateNFCStartTime(_event);

        if (tagFound) {
            if (_event.getStatus() == Event.ESTATUS_RUNNING)
                setSystemEventForPause(dataWrapper.context);
        }
    }

}
