package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.telephony.TelephonyManager;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

class EventsHandler {
    
    private final Context context;

    private String sensorType;

    private static int oldRingerMode;
    private static int oldSystemRingerMode;
    private static int oldZenMode;
    private static String oldRingtone;
    //private static String oldNotificationTone;
    private static int oldSystemRingerVolume;

    private String eventSMSPhoneNumber;
    private long eventSMSDate;
    //private String eventNotificationPostedRemoved;
    private String eventNFCTagName;
    private long eventNFCDate;
    private long eventAlarmClockDate;

    static final String SENSOR_TYPE_RADIO_SWITCH = "radioSwitch";
    static final String SENSOR_TYPE_RESTART_EVENTS = "restartEvents";
    static final String SENSOR_TYPE_RESTART_EVENTS_NOT_UNBLOCK = "restartEventsNotUnblock";
    static final String SENSOR_TYPE_PHONE_CALL = "phoneCall";
    static final String SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED = "calendarProviderChanged";
    static final String SENSOR_TYPE_SEARCH_CALENDAR_EVENTS = "searchCalendarEvents";
    static final String SENSOR_TYPE_SMS = "sms";
    static final String SENSOR_TYPE_NOTIFICATION = "notification";
    static final String SENSOR_TYPE_NFC_TAG = "nfcTag";
    static final String SENSOR_TYPE_EVENT_DELAY_START = "eventDelayStart";
    static final String SENSOR_TYPE_EVENT_DELAY_END = "eventDelayEnd";
    static final String SENSOR_TYPE_BATTERY = "battery";
    static final String SENSOR_TYPE_BLUETOOTH_CONNECTION = "bluetoothConnection";
    static final String SENSOR_TYPE_BLUETOOTH_STATE = "bluetoothState";
    static final String SENSOR_TYPE_DOCK_CONNECTION = "dockConnection";
    static final String SENSOR_TYPE_CALENDAR = "calendar";
    static final String SENSOR_TYPE_TIME = "time";
    static final String SENSOR_TYPE_APPLICATION = "application";
    static final String SENSOR_TYPE_HEADSET_CONNECTION = "headsetConnection";
    //static final String SENSOR_TYPE_NOTIFICATION_EVENT_END = "notificationEventEnd";
    static final String SENSOR_TYPE_SMS_EVENT_END = "smsEventEnd";
    static final String SENSOR_TYPE_WIFI_CONNECTION = "wifiConnection";
    static final String SENSOR_TYPE_WIFI_STATE = "wifiState";
    static final String SENSOR_TYPE_POWER_SAVE_MODE = "powerSaveMode";
    static final String SENSOR_TYPE_GEOFENCES_SCANNER = "geofenceScanner";
    static final String SENSOR_TYPE_LOCATION_MODE = "locationMode";
    static final String SENSOR_TYPE_DEVICE_ORIENTATION = "deviceOrientation";
    static final String SENSOR_TYPE_PHONE_STATE = "phoneState";
    static final String SENSOR_TYPE_NFC_EVENT_END = "nfcEventEnd";
    static final String SENSOR_TYPE_WIFI_SCANNER = "wifiScanner";
    static final String SENSOR_TYPE_BLUETOOTH_SCANNER = "bluetoothScanner";
    static final String SENSOR_TYPE_SCREEN = "screen";
    static final String SENSOR_TYPE_DEVICE_IDLE_MODE = "deviceIdleMode";
    static final String SENSOR_TYPE_PHONE_CALL_EVENT_END = "phoneCallEventEnd";
    static final String SENSOR_TYPE_ALARM_CLOCK = "alarmClock";
    static final String SENSOR_TYPE_ALARM_CLOCK_EVENT_END = "alarmClockEventEnd";

    public EventsHandler(Context context) {
        this.context = context;
    }
    
    void handleEvents(String sensorType) {
        synchronized (PPApplication.eventsHandlerMutex) {
            //CallsCounter.logCounter(context, "EventsHandler.handleEvents", "EventsHandler_handleEvents");

            //PPApplication.logE("#### EventsHandler.handleEvents", "-- start --------------------------------");

            if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return;

            //PPApplication.logE("#### EventsHandler.handleEvents", "-- application started --------------------------------");

            PhoneProfilesService ppService;

            if (PhoneProfilesService.getInstance() != null) {
                ppService = PhoneProfilesService.getInstance();
            }
            else
                return;

            //boolean interactive;

            this.sensorType = sensorType;
            //PPApplication.logE("#### EventsHandler.handleEvents", "sensorType=" + this.sensorType);
            //CallsCounter.logCounterNoInc(context, "EventsHandler.handleEvents->sensorType=" + this.sensorType, "EventsHandler_handleEvents");

            //restartAtEndOfEvent = false;

            // disabled for firstStartEvents
            //if (!PPApplication.getApplicationStarted(context))
            // application is not started
            //	return;

            //PPApplication.setApplicationStarted(context, true);

            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

            // save ringer mode, zen mode, ringtone before handle events
            // used by ringing call simulation
            oldRingerMode = ApplicationPreferences.prefRingerMode;
            oldZenMode = ApplicationPreferences.prefZenMode;
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                oldSystemRingerMode = audioManager.getRingerMode();
                oldSystemRingerVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            }

            try {
                Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
                if (uri != null)
                    oldRingtone = uri.toString();
                else
                    oldRingtone = "";
            } catch (SecurityException e) {
                Permissions.grantPlayRingtoneNotificationPermissions(context, false);
                oldRingtone = "";
            } catch (Exception e) {
                oldRingtone = "";
            }

            /*
            try {
                Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                if (uri != null)
                    oldNotificationTone = uri.toString();
                else
                    oldNotificationTone = "";
            } catch (SecurityException e) {
                Permissions.grantPlayRingtoneNotificationPermissions(context, true, false);
                oldNotificationTone = "";
            } catch (Exception e) {
                oldNotificationTone = "";
            }
            */

            /*
            if (ppService != null) {
                // start of GeofenceScanner
                if (!PhoneProfilesService.isGeofenceScannerStarted())
                    PPApplication.startGeofenceScanner(context);
                // start of CellTowerScanner
                if (!PhoneProfilesService.isPhoneStateScannerStarted()) {
                    PPApplication.logE("EventsHandler.handleEvents", "startPhoneStateScanner");
                    //PPApplication.sendMessageToService(this, PhoneProfilesService.MSG_START_PHONE_STATE_SCANNER);
                    PPApplication.startPhoneStateScanner(context);
                }
            }
            */

            if (!Event.getGlobalEventsRunning()) {
                // events are globally stopped

                doEndHandler(null);
                //dataWrapper.invalidateDataWrapper();

                //PPApplication.logE("#### EventsHandler.handleEvents", "-- end: events globally stopped --------------------------------");

                return;
            }

            /*
            // start orientation listener only when events exists
            if (ppService != null) {
                if (!PhoneProfilesService.isOrientationScannerStarted()) {
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION) > 0)
                        PPApplication.startOrientationScanner(context);
                }
            }
            */

            if (!eventsExists(sensorType, false)) {
                // events not exists

                doEndHandler(null);
                //dataWrapper.invalidateDataWrapper();

                //PPApplication.logE("#### EventsHandler.handleEvents", "-- end: not events found --------------------------------");

                return;
            }

            //PPApplication.logE("#### EventsHandler.handleEvents", "do EventsHandler");

            dataWrapper.fillEventList();

            boolean isRestart = sensorType.equals(SENSOR_TYPE_RESTART_EVENTS);

            //interactive = (!isRestart) || _interactive;

            boolean saveCalendarStartEndTime = false;
            if (isRestart) {
                if (Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, context.getApplicationContext()).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED) {
                    int eventCount = DatabaseHandler.getInstance(context.getApplicationContext())
                            .getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR, false);
                    if (eventCount > 0)
                        saveCalendarStartEndTime = true;
                }
            }
            if (sensorType.equals(SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED) ||
                    sensorType.equals(SENSOR_TYPE_SEARCH_CALENDAR_EVENTS) ||
                    sensorType.equals(SENSOR_TYPE_CALENDAR) ||
                    saveCalendarStartEndTime) {
                // search for calendar events
                //PPApplication.logE("[CALENDAR] EventsHandler.handleEvents", "search for calendar events");
                for (Event _event : dataWrapper.eventList) {
                    if ((_event._eventPreferencesCalendar._enabled) && (_event.getStatus() != Event.ESTATUS_STOP)) {
                        //PPApplication.logE("[CALENDAR] EventsHandler.handleEvents", "event._id=" + _event._id);
                        _event._eventPreferencesCalendar.saveStartEndTime(dataWrapper);
                    }
                }
            }
            /*if (sensorType.equals(SENSOR_TYPE_TIME)) {
                // search for time events
                PPApplication.logE("[TIME] EventsHandler.handleEvents", "search for time events");
                for (Event _event : dataWrapper.eventList) {
                    if ((_event._eventPreferencesTime._enabled) && (_event.getStatus() != Event.ESTATUS_STOP)) {
                        PPApplication.logE("[TIME] EventsHandler.handleEvents", "event._id=" + _event._id);
                        if (_event.getStatus() == Event.ESTATUS_RUNNING)
                            _event._eventPreferencesTime.setSystemEventForPause(dataWrapper.context);
                        if (_event.getStatus() == Event.ESTATUS_PAUSE)
                            _event._eventPreferencesTime.setSystemEventForStart(dataWrapper.context);
                    }
                }
            }*/

            if (isRestart) {
                // for restart events, set startTime to 0
                dataWrapper.clearSensorsStartTime(/*false*/);
            } else {
                if (sensorType.equals(SENSOR_TYPE_SMS)) {
                    // search for sms events, save start time
                    //PPApplication.logE("EventsHandler.handleEvents", "search for sms events");
                    for (Event _event : dataWrapper.eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesSMS._enabled) {
                                //PPApplication.logE("EventsHandler.handleEvents", "event._id=" + _event._id);
                                _event._eventPreferencesSMS.saveStartTime(dataWrapper, eventSMSPhoneNumber, eventSMSDate);
                            }
                        }
                    }
                }
                if (sensorType.equals(SENSOR_TYPE_NFC_TAG)) {
                    // search for nfc events, save start time
                    //PPApplication.logE("EventsHandler.handleEvents", "search for nfc events");
                    for (Event _event : dataWrapper.eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesNFC._enabled) {
                                //PPApplication.logE("EventsHandler.handleEvents", "event._id=" + _event._id);
                                _event._eventPreferencesNFC.saveStartTime(dataWrapper, eventNFCTagName, eventNFCDate);
                            }
                        }
                    }
                }
                if (sensorType.equals(SENSOR_TYPE_PHONE_CALL)) {
                    // search for call events, save start time
                    //PPApplication.logE("[CALL] EventsHandler.handleEvents", "search for call events");
                    for (Event _event : dataWrapper.eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesCall._enabled &&
                                    ((_event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL) ||
                                            (_event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ENDED) ||
                                            (_event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_ENDED))) {
                                //PPApplication.logE("[CALL] EventsHandler.handleEvents", "event._id=" + _event._id);
                                _event._eventPreferencesCall.saveStartTime(dataWrapper);
                            }
                        }
                    }
                }
                if (sensorType.equals(SENSOR_TYPE_ALARM_CLOCK)) {
                    // search for alarm clock events, save start time
                    //PPApplication.logE("EventsHandler.handleEvents", "search for alarm clock events");
                    for (Event _event : dataWrapper.eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesAlarmClock._enabled) {
                                //PPApplication.logE("EventsHandler.handleEvents", "event._id=" + _event._id);
                                _event._eventPreferencesAlarmClock.saveStartTime(dataWrapper, eventAlarmClockDate);
                            }
                        }
                    }
                }
            }

            boolean forDelayStartAlarm = sensorType.equals(SENSOR_TYPE_EVENT_DELAY_START);
            boolean forDelayEndAlarm = sensorType.equals(SENSOR_TYPE_EVENT_DELAY_END);

            /*if (PPApplication.logEnabled()) {
                //PPApplication.logE("@@@ EventsHandler.handleEvents","isRestart="+isRestart);
                PPApplication.logE("@@@ EventsHandler.handleEvents", "forDelayStartAlarm=" + forDelayStartAlarm);
                PPApplication.logE("@@@ EventsHandler.handleEvents", "forDelayEndAlarm=" + forDelayEndAlarm);
            }*/

            // no refresh notification and widgets
            ActivateProfileHelper.lockRefresh = true;

            Profile mergedProfile = DataWrapper.getNonInitializedProfile("", "", 0);
            Profile mergedPausedProfile = DataWrapper.getNonInitializedProfile("", "", 0);

            //Profile oldActivatedProfile = dataWrapper.getActivatedProfileFromDB(false, false);
            Profile oldActivatedProfile = Profile.getProfileFromSharedPreferences(context, PPApplication.ACTIVATED_PROFILE_PREFS_NAME);
            boolean profileChanged = false;

            //Profile activatedProfile0 = null;

            //int runningEventCount0;
            //int runningEventCountP;
            boolean activateProfileAtEnd = false;
            boolean anyEventPaused = false;
            //Event notifyEventEnd = null;
            boolean notified = false;

            boolean reactivateProfile = false;

            if (isRestart || (sensorType.equals(SENSOR_TYPE_RESTART_EVENTS_NOT_UNBLOCK))) {
                if (ppService != null) {
                    // check if exists delayed restart events
                    //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "check if exists delayed restart events");
                    boolean exists;
                    try {
                        WorkManager instance = WorkManager.getInstance(context.getApplicationContext());
                        ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag("restartEventsWithDelayWork");
                        //noinspection TryWithIdenticalCatches
                        try {
                            List<WorkInfo> workInfoList = statuses.get();
                            boolean enqueued = false;
                            for (WorkInfo workInfo : workInfoList) {
                                WorkInfo.State state = workInfo.getState();
                                enqueued = state == WorkInfo.State.ENQUEUED;
                            }
                            exists = enqueued;
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            exists = false;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            exists = false;
                        }
                    } catch (Exception e) {
                        exists = false;
                    }
                    if (!exists) {
                        // delayed work not exists
                        //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "delayed work not exists");
                        ppService.willBeDoRestartEvents = false;
                    }
                }
            }

            if (isRestart) {
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "restart events");
                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "restart events");
                }*/

                reactivateProfile = true;

                //oldActivatedProfile = null;

                // get running events count
                //List<EventTimeline> _etl = dataWrapper.getEventTimelineList(true);
                //runningEventCount0 = _etl.size();

                // 1. pause events
                dataWrapper.sortEventsByStartOrderDesc();
                for (Event _event : dataWrapper.eventList) {
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("EventsHandler.handleEvents", "state PAUSE");
                        PPApplication.logE("EventsHandler.handleEvents", "event._name=" + _event._name);
                        PPApplication.logE("EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
                    }*/

                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        // only pause events
                        // pause also paused events

                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "state PAUSE");
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "event._name=" + _event._name);
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
                        }*/

                        boolean running = _event.getStatus() == Event.ESTATUS_RUNNING;
                        dataWrapper.doHandleEvents(_event, true, true, /*interactive,*/ false, false, /*reactivateProfile,*/ mergedProfile, sensorType);
                        boolean paused = _event.getStatus() == Event.ESTATUS_PAUSE;

                        if (running && paused) {
                            anyEventPaused = true;
                            //notifyEventEnd = _event;
                            _event.notifyEventEnd(false);
                        }
                    }
                }

                //runningEventCountP = _etl.size();

                // 2. start events
                dataWrapper.sortEventsByStartOrderAsc();
                for (Event _event : dataWrapper.eventList) {
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("EventsHandler.handleEvents", "state RUNNING");
                        PPApplication.logE("EventsHandler.handleEvents", "event.name=" + _event._name);
                        PPApplication.logE("EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
                    }*/

                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        // only start events

                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "state RUNNING");
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "event._name=" + _event._name);
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
                        }*/

                        // start all events
                        boolean paused = _event.getStatus() == Event.ESTATUS_PAUSE;
                        dataWrapper.doHandleEvents(_event, false, true, /*interactive,*/ false, false, /*reactivateProfile,*/ mergedProfile, sensorType);
                        boolean running = _event.getStatus() == Event.ESTATUS_RUNNING;

                        if (running && paused) {
                            _event.notifyEventStart(context, false);
                        }
                    }
                }
            } else {
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "NO restart events");
                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "NO restart events");
                }*/

                //oldActivatedProfile = dataWrapper.getActivatedProfile();

                //activatedProfile0 = dataWrapper.getActivatedProfileFromDB();

                // get running events count
                //List<EventTimeline> _etl = dataWrapper.getEventTimelineList(true);
                //runningEventCount0 = _etl.size();

                //1. pause events
                dataWrapper.sortEventsByStartOrderDesc();
                for (Event _event : dataWrapper.eventList) {
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("$$$ EventsHandler.handleEvents", "state PAUSE");
                        PPApplication.logE("$$$ EventsHandler.handleEvents", "event._name=" + _event._name);
                        PPApplication.logE("$$$ EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
                    }*/

                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        // only pause events
                        // pause only running events

                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "state PAUSE");
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "event._name=" + _event._name);
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
                        }*/

                        boolean running = _event.getStatus() == Event.ESTATUS_RUNNING;
                        dataWrapper.doHandleEvents(_event, true, false, /*interactive,*/ forDelayStartAlarm, forDelayEndAlarm, /*reactivateProfile,*/ mergedPausedProfile, sensorType);
                        boolean paused = _event.getStatus() == Event.ESTATUS_PAUSE;

                        if (running && paused) {
                            anyEventPaused = true;
                            //notifyEventEnd = _event;
                            if (_event.notifyEventEnd(!notified))
                                notified = true;

                            if ((ppService != null) && (_event._atEndDo == Event.EATENDDO_RESTART_EVENTS)) {
                                //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "has restart events=");
                                ppService.willBeDoRestartEvents = true;
                            }
                            if ((!activateProfileAtEnd) && ((_event._atEndDo == Event.EATENDDO_UNDONE_PROFILE) || (_event._fkProfileEnd != Profile.PROFILE_NO_ACTIVATE)))
                                activateProfileAtEnd = true;

                            /*if (PPApplication.logEnabled()) {
                                if (ppService != null)
                                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "ppService.willBeDoRestartEvents=" + ppService.willBeDoRestartEvents);
                            }*/
                        }
                    }
                }

                //runningEventCountP = _etl.size();

                //2. start events
                mergedProfile.copyProfile(mergedPausedProfile);
                dataWrapper.sortEventsByStartOrderAsc();
                for (Event _event : dataWrapper.eventList) {
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("$$$ EventsHandler.handleEvents", "state RUNNING");
                        PPApplication.logE("$$$ EventsHandler.handleEvents", "event._name=" + _event._name);
                        PPApplication.logE("$$$ EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
                    }*/

                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        // only start events
                        // start only paused events

                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "state RUNNING");
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "event._name=" + _event._name);
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
                        }*/

                        boolean paused = _event.getStatus() == Event.ESTATUS_PAUSE;
                        dataWrapper.doHandleEvents(_event, false, false, /*interactive,*/ forDelayStartAlarm, forDelayEndAlarm, /*true*//*reactivateProfile,*/ mergedProfile, sensorType);
                        boolean running = _event.getStatus() == Event.ESTATUS_RUNNING;

                        if (running && paused) {
                            if (_event.notifyEventStart(context, !notified))
                                notified = true;
                        }
                    }
                }
            }

            ActivateProfileHelper.lockRefresh = false;

            /*if (mergedProfile._id == 0)
                PPApplication.logE("$$$ EventsHandler.handleEvents", "no profile for activation");
            else
                PPApplication.logE("$$$ EventsHandler.handleEvents", "profileName=" + mergedProfile._name);*/

            //if ((!restartAtEndOfEvent) || isRestart) {
            //    // No any paused events has "Restart events" at end of event

            //////////////////
            //// when no events are running or manual activation,
            //// activate background profile when no profile is activated

            // get running events count
            List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(true);
            int runningEventCountE = eventTimelineList.size();

            Profile activatedProfile = dataWrapper.getActivatedProfileFromDB(false, false);
            long backgroundProfileId = Profile.PROFILE_NO_ACTIVATE;
            boolean notifyBackgroundProfile = false;

            boolean waitForEndOfStart = true;
            if (ppService != null)
                waitForEndOfStart = ppService.getWaitForEndOfStart();

            if (!DataWrapper.getIsManualProfileActivation(false/*, context.getApplicationContext()*/)) {
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "active profile is NOT activated manually");
                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "runningEventCount0=" + runningEventCount0);
                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "runningEventCountE=" + runningEventCountE);
                    if (ppService != null)
                        PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "ppService.willBeDoRestartEvents=" + ppService.willBeDoRestartEvents);
                }*/
                // no manual profile activation
                if (runningEventCountE == 0) {
                    if ((ppService != null) && (!ppService.willBeDoRestartEvents)) {
                        // activate default profile, only when will not be do restart events from paused events

                        //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "no events running");
                        // no events running
                        backgroundProfileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile);
                        if (waitForEndOfStart)
                            backgroundProfileId = Profile.PROFILE_NO_ACTIVATE;
                        if (backgroundProfileId != Profile.PROFILE_NO_ACTIVATE) {
                            //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "default profile is set");
                            long activatedProfileId = 0;
                            if (activatedProfile != null)
                                activatedProfileId = activatedProfile._id;

                            if (ApplicationPreferences.applicationBackgroundProfileUsage) {
                                // do not activate default profile when not any event is paused and no any profile is activated
                                // for example for screen on/off broadcast, when no any event is running
                                if (!anyEventPaused && (mergedProfile._id == 0) && (mergedPausedProfile._id == 0))
                                    activateProfileAtEnd = true;

                                /*if (PPApplication.logEnabled()) {
                                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "anyEventPaused=" + anyEventPaused);
                                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "activatedProfileId=" + activatedProfileId);
                                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "mergedProfile._id=" + mergedProfile._id);
                                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "mergedPausedProfile._id=" + mergedPausedProfile._id);
                                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "isRestart=" + isRestart);
                                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "activateProfileAtEnd=" + activateProfileAtEnd);
                                }*/
                                if ((activatedProfileId == 0) ||
                                        isRestart ||
                                        // activate default profile when is not activated profile at end of events
                                        ((!activateProfileAtEnd || ((mergedProfile._id != 0) && (mergedPausedProfile._id == 0))) &&
                                                (activatedProfileId != backgroundProfileId))
                                )
                                {
                                    notifyBackgroundProfile = true;
                                    mergedProfile.mergeProfiles(backgroundProfileId, dataWrapper/*, false*/);
                                    //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "activated default profile");
                                }
                            } else {
                                if ((activatedProfileId != backgroundProfileId) || isRestart) {
                                    notifyBackgroundProfile = true;
                                    mergedProfile.mergeProfiles(backgroundProfileId, dataWrapper/*, false*/);
                                    //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "activated default profile");
                                }
                            }
                        }
                    }
                    //else
                    //if (ppService != null)
                    //    ppService.willBeDoRestartEvents = false;
                }
            } else {
                //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "active profile is activated manually");
                // manual profile activation
                backgroundProfileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile);
                if (waitForEndOfStart)
                    backgroundProfileId = Profile.PROFILE_NO_ACTIVATE;
                if (backgroundProfileId != Profile.PROFILE_NO_ACTIVATE) {
                    if (activatedProfile == null) {
                        // if not profile activated, activate Default profile
                        notifyBackgroundProfile = true;
                        mergedProfile.mergeProfiles(backgroundProfileId, dataWrapper/*, false*/);
                        //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "activated default profile");
                    }
                }
            }
            ////////////////

            //Event notifyEventStart = null;
            String backgroundProfileNotificationSound = "";
            boolean backgroundProfileNotificationVibrate = false;

            //if (/*(!isRestart) &&*/ (runningEventCountE > runningEventCountP)) {
            // only running events is increased, play event notification sound

            //EventTimeline eventTimeline = eventTimelineList.get(runningEventCountE - 1);
            //notifyEventStart = dataWrapper.getEventById(eventTimeline._fkEvent);
            //}
            //else
            if (/*(!isRestart) &&*/ (backgroundProfileId != Profile.PROFILE_NO_ACTIVATE) && notifyBackgroundProfile) {
                // only when activated is background profile, play event notification sound

                backgroundProfileNotificationSound = ApplicationPreferences.applicationBackgroundProfileNotificationSound;
                backgroundProfileNotificationVibrate = ApplicationPreferences.applicationBackgroundProfileNotificationVibrate;
            }

            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("$$$ EventsHandler.handleEvents", "mergedProfile=" + mergedProfile);
                PPApplication.logE("$$$ EventsHandler.handleEvents", "mergedProfile._id=" + mergedProfile._id);
            }*/

            boolean doSleep = false;

            if (mergedProfile._id != 0) {
                // activate merged profile
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "#### profileName=" + mergedProfile._name);
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "#### profileId=" + mergedProfile._id);
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "#### _volumeRingerMode=" + mergedProfile._volumeRingerMode);
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "#### _volumeZenMode=" + mergedProfile._volumeZenMode);
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "#### _volumeRingtone=" + mergedProfile._volumeRingtone);
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "#### _volumeNotification=" + mergedProfile._volumeNotification);
                }*/
                DatabaseHandler.getInstance(context.getApplicationContext()).saveMergedProfile(mergedProfile);

                //if (mergedProfile._id != oldActivatedProfileId)
                if (!mergedProfile.compareProfile(oldActivatedProfile))
                    profileChanged = true;

                if (profileChanged || reactivateProfile) {
                    dataWrapper.activateProfileFromEvent(mergedProfile._id, false, true, isRestart);
                    // wait for profile activation
                    doSleep = true;
                }
            } else {
                if ((ppService != null) && (!ppService.willBeDoRestartEvents)) {
                    // update only when will not be do restart events from paused events
                    //PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from EventsHandler.handleEvents");
                    dataWrapper.updateNotificationAndWidgets(false, false);
                }
            }

            /*
            PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "notifyEventStart=" + notifyEventStart);
            if (notifyEventStart != null)
                PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "notifyEventStart._name=" + notifyEventStart._name);
            PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "notifyEventEnd=" + notifyEventEnd);
            if (notifyEventEnd != null)
                PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "notifyEventEnd._name=" + notifyEventEnd._name);
            PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "backgroundProfileNotificationSound=" + backgroundProfileNotificationSound);

            // notify start of event
            boolean notify = (notifyEventStart != null) && notifyEventStart.notifyEventStart(context, !isRestart);
            if (notify)
                PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "start of event notified");
            if (!notify)
                // notify end of event
                notify = (notifyEventEnd != null) && notifyEventEnd.notifyEventEnd(!isRestart);
            if (notify)
                PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "end of event notified");
            */
            if (!notified) {
                // notify default profile
                if (!backgroundProfileNotificationSound.isEmpty() || backgroundProfileNotificationVibrate) {
                    //if (ppService != null) {
                        ppService.playNotificationSound(backgroundProfileNotificationSound, backgroundProfileNotificationVibrate);
                        //PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "default profile notified");
                        notified = true;
                    //}
                }
            }

            if (doSleep || notified) {
                //try { Thread.sleep(500); } catch (InterruptedException e) { }
                //SystemClock.sleep(500);
                PPApplication.sleep(500);
            }
            //}

            //restartAtEndOfEvent = false;

            doEndHandler(dataWrapper);

            // refresh GUI
            Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
            LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);


            //dataWrapper.invalidateDataWrapper();

            //PPApplication.logE("#### EventsHandler.handleEvents", "-- end --------------------------------");
        }
    }

    private boolean eventsExists(String broadcastReceiverType, boolean onlyRunning) {
        int eventType = 0;
        switch (broadcastReceiverType) {
            case SENSOR_TYPE_BATTERY:
            case SENSOR_TYPE_POWER_SAVE_MODE:
                eventType = DatabaseHandler.ETYPE_BATTERY;
                break;
            case SENSOR_TYPE_BLUETOOTH_CONNECTION:
            case SENSOR_TYPE_BLUETOOTH_STATE:
                eventType = DatabaseHandler.ETYPE_BLUETOOTH_CONNECTED;
                break;
            case SENSOR_TYPE_BLUETOOTH_SCANNER:
                eventType = DatabaseHandler.ETYPE_BLUETOOTH_NEARBY;
                break;
            case SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED:
            case SENSOR_TYPE_CALENDAR:
            case SENSOR_TYPE_SEARCH_CALENDAR_EVENTS:
                eventType = DatabaseHandler.ETYPE_CALENDAR;
                break;
            case SENSOR_TYPE_DOCK_CONNECTION:
            case SENSOR_TYPE_HEADSET_CONNECTION:
                eventType = DatabaseHandler.ETYPE_PERIPHERAL;
                break;
            /*case SENSOR_TYPE_EVENT_DELAY_START:
                eventType = DatabaseHandler.ETYPE_????;
                break;
            case SENSOR_TYPE_EVENT_DELAY_END:
                eventType = DatabaseHandler.ETYPE_????;
                break;*/
            case SENSOR_TYPE_TIME:
                eventType = DatabaseHandler.ETYPE_TIME;
                break;
            case SENSOR_TYPE_APPLICATION:
                eventType = DatabaseHandler.ETYPE_APPLICATION;
                break;
            case SENSOR_TYPE_NOTIFICATION:
                eventType = DatabaseHandler.ETYPE_NOTIFICATION;
                break;
            /*case SENSOR_TYPE_NOTIFICATION_EVENT_END:
                eventType = DatabaseHandler.ETYPE_NOTIFICATION;
                break;*/
            case SENSOR_TYPE_PHONE_CALL:
            case SENSOR_TYPE_PHONE_CALL_EVENT_END:
                eventType = DatabaseHandler.ETYPE_CALL;
                break;
            /*case SENSOR_TYPE_RESTART_EVENTS:
                eventType = DatabaseHandler.ETYPE_???;
                break;*/
            /*// call doEventService for all screen on/off changes
            case SENSOR_TYPE_SCREEN:
                eventType = DatabaseHandler.ETYPE_SCREEN;
                break;*/
            case SENSOR_TYPE_SMS:
            case SENSOR_TYPE_SMS_EVENT_END:
                eventType = DatabaseHandler.ETYPE_SMS;
                break;
            case SENSOR_TYPE_WIFI_CONNECTION:
            case SENSOR_TYPE_WIFI_STATE:
                eventType = DatabaseHandler.ETYPE_WIFI_CONNECTED;
                break;
            case SENSOR_TYPE_WIFI_SCANNER:
                eventType = DatabaseHandler.ETYPE_WIFI_NEARBY;
                break;
            /*case SENSOR_TYPE_DEVICE_IDLE_MODE:
                eventType = DatabaseHandler.ETYPE_????;
                break;*/
            case SENSOR_TYPE_GEOFENCES_SCANNER:
            case SENSOR_TYPE_LOCATION_MODE:
                eventType = DatabaseHandler.ETYPE_LOCATION;
                break;
            case SENSOR_TYPE_DEVICE_ORIENTATION:
                eventType = DatabaseHandler.ETYPE_ORIENTATION;
                break;
            case SENSOR_TYPE_PHONE_STATE:
                eventType = DatabaseHandler.ETYPE_MOBILE_CELLS;
                break;
            case SENSOR_TYPE_NFC_TAG:
            case SENSOR_TYPE_NFC_EVENT_END:
                eventType = DatabaseHandler.ETYPE_NFC;
                break;
            case SENSOR_TYPE_RADIO_SWITCH:
                eventType = DatabaseHandler.ETYPE_RADIO_SWITCH;
                break;
            case SENSOR_TYPE_ALARM_CLOCK:
            case SENSOR_TYPE_ALARM_CLOCK_EVENT_END:
                eventType = DatabaseHandler.ETYPE_ALARM_CLOCK;
                break;
        }

        if (eventType > 0)
            return DatabaseHandler.getInstance(context.getApplicationContext()).getTypeEventsCount(eventType, onlyRunning) > 0;
        else
            return true;
    }

    private void doEndHandler(DataWrapper dataWrapper) {
        //PPApplication.logE("EventsHandler.doEndHandler","sensorType="+sensorType);
        //PPApplication.logE("EventsHandler.doEndHandler","callEventType="+callEventType);

        if (sensorType.equals(SENSOR_TYPE_PHONE_CALL)) {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (eventsExists(sensorType, true)) {
                //PPApplication.logE("EventsHandler.doEndHandler", "running event exists");
                // doEndHandler is called even if no event exists, but ringing call simulation is only for running event with call sensor
                //if (android.os.Build.VERSION.SDK_INT >= 21) {
                    boolean inRinging = false;
                    if (telephony != null) {
                        int callState = telephony.getCallState();
                        //if (doUnlink) {
                        //if (linkUnlink == PhoneCallBroadcastReceiver.LINKMODE_UNLINK) {
                        inRinging = (callState == TelephonyManager.CALL_STATE_RINGING);
                    }
                    //PPApplication.logE("EventsHandler.doEndHandler", "inRinging="+inRinging);
                    if (inRinging) {
                        // start PhoneProfilesService for ringing call simulation
                        //PPApplication.logE("EventsHandler.doEndHandler", "start simulating ringing call");
                        try {
                            boolean simulateRingingCall = false;
                            String phoneNumber = ApplicationPreferences.prefEventCallPhoneNumber;
                            for (Event _event : dataWrapper.eventList) {
                                if (_event._eventPreferencesCall._enabled && _event.getStatus() == Event.ESTATUS_RUNNING) {
                                    //PPApplication.logE("EventsHandler.doEndHandler", "event._id=" + _event._id);
                                    if (_event._eventPreferencesCall.isPhoneNumberConfigured(phoneNumber/*, dataWrapper*/))
                                        simulateRingingCall = true;
                                }
                            }
                            if (simulateRingingCall) {
                                /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_SIMULATE_RINGING_CALL, true);
                                // add saved ringer mode, zen mode, ringtone before handle events as parameters
                                // ringing call simulator compare this with new (actual values), changed by currently activated profile
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_OLD_RINGER_MODE, oldRingerMode);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_OLD_SYSTEM_RINGER_MODE, oldSystemRingerMode);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_OLD_ZEN_MODE, oldZenMode);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_OLD_RINGTONE, oldRingtone);
                                serviceIntent.putExtra(PhoneProfilesService.EXTRA_OLD_SYSTEM_RINGER_VOLUME, oldSystemRingerVolume);
                                PPApplication.startPPService(context, serviceIntent);*/
                                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                                commandIntent.putExtra(PhoneProfilesService.EXTRA_SIMULATE_RINGING_CALL, true);
                                // add saved ringer mode, zen mode, ringtone before handle events as parameters
                                // ringing call simulator compare this with new (actual values), changed by currently activated profile
                                commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_RINGER_MODE, oldRingerMode);
                                commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_SYSTEM_RINGER_MODE, oldSystemRingerMode);
                                commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_ZEN_MODE, oldZenMode);
                                commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_RINGTONE, oldRingtone);
                                commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_SYSTEM_RINGER_VOLUME, oldSystemRingerVolume);
                                PPApplication.runCommand(context, commandIntent);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                //}
            }
            //else
            //    PPApplication.logE("EventsHandler.doEndService", "running event NOT exists");

            boolean inCall = false;
            if (telephony != null) {
                int callState = telephony.getCallState();
                //if (doUnlink) {
                //if (linkUnlink == PhoneCallBroadcastReceiver.LINKMODE_UNLINK) {
                inCall = (callState == TelephonyManager.CALL_STATE_RINGING) || (callState == TelephonyManager.CALL_STATE_OFFHOOK);
            }
            if (!inCall)
                setEventCallParameters(EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED, "", 0);
        }
        else
        if (sensorType.equals(SENSOR_TYPE_PHONE_CALL_EVENT_END)) {
            setEventCallParameters(EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED, "", 0);
        }

        /*else
        if (broadcastReceiverType.equals(SENSOR_TYPE_SMS)) {
            // start PhoneProfilesService for notification tone simulation
            Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(EXTRA_SIMULATE_NOTIFICATION_TONE, true);
            serviceIntent.putExtra(EXTRA_OLD_RINGER_MODE, oldRingerMode);
            serviceIntent.putExtra(EXTRA_OLD_SYSTEM_RINGER_MODE, oldSystemRingerMode);
            serviceIntent.putExtra(EXTRA_OLD_ZEN_MODE, oldZenMode);
            serviceIntent.putExtra(EXTRA_OLD_NOTIFICATION_TONE, oldNotificationTone);
            PPApplication.startPPService(context, serviceIntent);
        }
        else
        if (broadcastReceiverType.equals(SENSOR_TYPE_NOTIFICATION)) {
            if ((android.os.Build.VERSION.SDK_INT >= 21) && intent.getStringExtra(EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED).equals("posted")) {
                // start PhoneProfilesService for notification tone simulation
                Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(EXTRA_SIMULATE_NOTIFICATION_TONE, true);
                serviceIntent.putExtra(EXTRA_OLD_RINGER_MODE, oldRingerMode);
                serviceIntent.putExtra(EXTRA_OLD_SYSTEM_RINGER_MODE, oldSystemRingerMode);
                serviceIntent.putExtra(EXTRA_OLD_ZEN_MODE, oldZenMode);
                serviceIntent.putExtra(EXTRA_OLD_NOTIFICATION_TONE, oldNotificationTone);
                PPApplication.startPPService(context, serviceIntent);
            }
        }*/
    }

    void setEventSMSParameters(String phoneNumber, long date) {
        eventSMSPhoneNumber = phoneNumber;
        eventSMSDate = date;
    }

    /*
    void setEventNotificationParameters(String postedRemoved) {
        eventNotificationPostedRemoved = postedRemoved;
    }
    */

    void setEventNFCParameters(String tagName, long date) {
        eventNFCTagName = tagName;
        eventNFCDate = date;
    }

    void setEventAlarmClockParameters(long date) {
        eventAlarmClockDate = date;
    }

    void setEventCallParameters(int callEventType, String phoneNumber, long eventTime) {
        EventPreferencesCall.setEventCallEventType(context, callEventType);
        EventPreferencesCall.setEventCallEventTime(context, eventTime);
        EventPreferencesCall.setEventCallPhoneNumber(context, phoneNumber);
    }

}
