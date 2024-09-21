package sk.henrichg.phoneprofilesplus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.List;

class EventPreferencesMusic extends EventPreferences {

    int _musicState;
    String _applications;

    static final String PREF_EVENT_MUSIC_ENABLED = "eventMusicEnabled";
    private static final String PREF_EVENT_MUSIC_MUSIC_STATE = "eventMusicMusicState";
    private static final String PREF_EVENT_MUSIC_APPLICATIONS = "eventMusicApplications";
    static final String PREF_EVENT_MUSIC_NOTIFICATION_ACCESS_SYSTEM_SETTINGS = "eventMusicNotificationsAccessSettings";
    static final String PREF_EVENT_MUSIC_NOTIFICATION_ACCESS_RESTRICTED_SETTINGS = "eventMusicNotificationsAccessSettingsRestrictedSettings";

    static final String PREF_EVENT_MUSIC_CATEGORY = "eventMusicCategoryRoot";

    EventPreferencesMusic(Event event,
                          boolean enabled,
                          int musicState,
                          String applications)
    {
        super(event, enabled);

        this._musicState = musicState;
        this._applications = applications;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesMusic._enabled;
        this._musicState = fromEvent._eventPreferencesMusic._musicState;
        this.setSensorPassed(fromEvent._eventPreferencesMusic.getSensorPassed());
    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_MUSIC_ENABLED, _enabled);
        editor.putString(PREF_EVENT_MUSIC_MUSIC_STATE, String.valueOf(this._musicState));
        editor.putString(PREF_EVENT_MUSIC_APPLICATIONS, this._applications);
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_MUSIC_ENABLED, false);
        this._musicState = Integer.parseInt(preferences.getString(PREF_EVENT_MUSIC_MUSIC_STATE, "0"));
        this._applications = preferences.getString(PREF_EVENT_MUSIC_APPLICATIONS, "");
    }

    /** @noinspection unused*/
    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_music_summary));
        } else {
            if (EventStatic.isEventPreferenceAllowed(PREF_EVENT_MUSIC_ENABLED, false, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (addBullet) {
                    _value.append(StringConstants.TAG_BOLD_START_HTML);
                    _value.append(getPassStatusString(context.getString(R.string.event_type_music), addPassStatus, DatabaseHandler.ETYPE_MUSIC, context));
                    _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
                }

                _value.append(context.getString(R.string.event_preferences_music_state));
                String[] musicSate = context.getResources().getStringArray(R.array.eventMusicStatesArray);
                _value.append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(musicSate[this._musicState], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);

                String selectedApplications = context.getString(R.string.applications_multiselect_summary_text_not_selected);
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
                                //if (app != null)
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
                _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_applications_applications)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(selectedApplications, disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
            }
        }

        return _value.toString();
    }

    /** @noinspection unused*/
    private void setSummary(PreferenceManager prefMng, String key, /*String value,*/ Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_MUSIC_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_MUSIC_MUSIC_STATE)) {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                String value = preferences.getString(key, "");
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }

        if (key.equals(PREF_EVENT_MUSIC_NOTIFICATION_ACCESS_SYSTEM_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary = context.getString(R.string.event_preferences_music_notificationAccessSystemSettings_summary);
                if (!PPNotificationListenerService.isNotificationListenerServiceEnabled(context, true)) {
                    summary = "* " + context.getString(R.string.event_preferences_music_notificationAccessSystemSettingsDisabled_summary) + "! *" + StringConstants.STR_DOUBLE_NEWLINE +
                            summary;
                } else {
                    summary = context.getString(R.string.event_preferences_music_notificationAccessSystemSettingsEnabled_summary) + StringConstants.STR_DOUBLE_NEWLINE_WITH_DOT +
                            summary;
                }
                preference.setSummary(summary);
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesMusic.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesMusic.isRunnable(context);
        //boolean isAllConfigured = event._eventPreferencesNusic.isAllConfigured(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_MUSIC_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_MUSIC_APPLICATIONS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_MUSIC_APPLICATIONS, "").isEmpty();
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

        if (key.equals(PREF_EVENT_MUSIC_ENABLED)) {
            setSummary(prefMng, key, context);
        }

        if (key.equals(PREF_EVENT_MUSIC_MUSIC_STATE) ||
                key.equals(PREF_EVENT_MUSIC_APPLICATIONS) ||
                key.equals(PREF_EVENT_MUSIC_NOTIFICATION_ACCESS_SYSTEM_SETTINGS)) {
            setSummary(prefMng, key, context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_MUSIC_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_MUSIC_MUSIC_STATE, preferences, context);
        setSummary(prefMng, PREF_EVENT_MUSIC_APPLICATIONS, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_MUSIC_ENABLED, false, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesMusic tmp = new EventPreferencesMusic(this._event, this._enabled, this._musicState, this._applications);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_MUSIC_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_ALARM_CLOCK_ENABLED, false);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_MUSIC).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(tmp.isRunnable(context) && tmp.isAllConfigured(context) && permissionGranted), false);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_MUSIC_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        StringConstants.STR_COLON_WITH_SPACE+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    boolean isRunnable(Context context)
    {
        return super.isRunnable(context);
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_MUSIC_ENABLED) != null) {
                ApplicationsMultiSelectDialogPreference applicationsPreference = prefMng.findPreference(PREF_EVENT_MUSIC_APPLICATIONS);
                if (applicationsPreference != null) {
                    //applicationsPreference.setEnabled(accessibilityEnabled);
                    applicationsPreference.setSummaryAMSDP();
                }
            }
            setSummary(prefMng, PREF_EVENT_MUSIC_ENABLED, preferences, context);
            setSummary(prefMng, PREF_EVENT_MUSIC_NOTIFICATION_ACCESS_SYSTEM_SETTINGS, preferences, context);
        }

        setCategorySummary(prefMng, preferences, context);
    }

    /*
    @Override
    void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING
    }

    @Override
    void removeSystemEvent(Context context)
    {
    }
    */

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if (EventStatic.isEventPreferenceAllowed(EventPreferencesMusic.PREF_EVENT_MUSIC_ENABLED, false, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                boolean isNotAllowedSession = false;

                // play media from PPP is ignored
                if ((RingtonePreference.mediaPlayer == null) &&
                        (VolumeDialogPreferenceFragment.mediaPlayer == null) /*&&
                        not needed to test it, it uses ALARM channel
                        (PlayRingingNotification.ringingMediaPlayer == null)*/) {
                    AudioManager audioManager = (AudioManager) eventsHandler.context.getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager != null) {
                        if (PPNotificationListenerService.isNotificationListenerServiceEnabled(eventsHandler.context, true)) {
                            // notification acces isgranted, get controloer package name

                            MediaSessionManager mediaSessionManager = (MediaSessionManager) eventsHandler.context.getSystemService(Context.MEDIA_SESSION_SERVICE);
                            if (mediaSessionManager != null) {
                                List<MediaController> activeSessions;
                                ComponentName notificationListenerComponent = new ComponentName(eventsHandler.context, PPNotificationListenerService.class);
                                activeSessions = mediaSessionManager.getActiveSessions(notificationListenerComponent);

//                                Log.e("EventPreferencesMusic.doHandleEvent", "activeSessions=" + activeSessions.size());
                                for (MediaController controller : activeSessions) {
                                    String applicationFromController = controller.getPackageName();
//                                    Log.e("EventPreferencesMusic.doHandleEvent", "controller=" + applicationFromController);
                                    if (applicationFromController.equals(PPApplication.PACKAGE_NAME_PP) ||
                                            applicationFromController.equals(PPApplication.PACKAGE_NAME) ||
                                            applicationFromController.equals(PPApplication.PACKAGE_NAME_EXTENDER) ||
                                            applicationFromController.equals(PPApplication.PACKAGE_NAME_PPPPS)) {
                                        isNotAllowedSession = true;
                                        break;
                                    }
                                    String[] splits = _applications.split(StringConstants.STR_SPLIT_REGEX);
                                    for (String split : splits) {
                                        String packageName = Application.getPackageName(split);
                                        if (applicationFromController.equals(packageName)) {
                                            //isNotAllowedSession = false;
                                            break;
                                        }
                                    }
                                }
                            } else
                                eventsHandler.notAllowedMusic = true;
                        }

                        if (!isNotAllowedSession) {
                            // allowed session for detection
                            if (_musicState == 1)
                                eventsHandler.musicPassed = !audioManager.isMusicActive();
                            else
                                eventsHandler.musicPassed = audioManager.isMusicActive();
                            //Log.e("EventPreferencesMusic.doHandleEvent", "musicPassed=" + eventsHandler.musicPassed);
                        } else
                            eventsHandler.notAllowedMusic = true;

                    } else
                        eventsHandler.notAllowedMusic = true;
                } else
                    eventsHandler.notAllowedMusic = true;

                if (!eventsHandler.notAllowedMusic) {
                    //noinspection ConstantValue
                    if (eventsHandler.musicPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedMusic = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_MUSIC);
            }
        }
    }

}
